/************************************************************************/
/*                               geneventc                              */
/*                                                                      */
/*  Platform : All                                                      */
/*  Module   : Event functions                                          */
/*                                                                      */
/*  Albert van Kuik                                                     */
/************************************************************************/
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#ifdef DEBUG
#include "mshell.h"
#endif
#define  G_LOCAL
#include "genplot.h"
#include "g_inter.h"

typedef int (*eventfunc_type)(G_EVENT*, G_CALLDATA);

static int dummy_eventfunc(G_EVENT *ui, G_CALLDATA call_data)
{
    ui = ui; call_data = call_data;
    return TRUE;
}

static eventfunc_type event_array[MAXEVENTTYPE];
static G_CALLDATA event_data[MAXEVENTTYPE];

static void init_eventhandlers(void)
{
    int i;
    for (i=0;i<MAXEVENTTYPE;i++)
        event_array[i] = dummy_eventfunc;
} 

void g_add_eventhandler(int event_mask, 
                        int (*eventfunc)(G_EVENT*,G_CALLDATA), 
                        G_CALLDATA call_data)
{
    int i;
    if (eventfunc == NULL)
        return;
    for (i=0;i<MAXEVENTTYPE;i++)
        if (event_mask & (1<<i)) {
            event_array[i] = eventfunc;
            event_data[i]  = call_data;
    }            
}

void g_remove_eventhandler(int event_mask)
{
    int i;
    for (i=0;i<MAXEVENTTYPE;i++)
        if (event_mask & (1<<i))
            event_array[i] = dummy_eventfunc;
}

/* --- EVENT QUEUE --- */
#define QMAX 1000
static G_EVENT *event_queue;
static int q_head, q_tail;

void INTERNAL_prep_event(G_EVENT *ui)
{
    int id = ui->win_id;
    if ((ui->event & G_WINDOWCREATE) 
           || (ui->event & G_WINDOWDESTROY)
           || (ui->event & G_WINDOWQUIT)) {
        ui->vp_id = 0;
        ui->command = NULL;
        ui->win_id = MAKE_GLOBAL_ID(ui->win_id, G_SCREEN);
    }
    else if (!(ui->event & G_COMMAND) && !(ui->event & G_COMMANDLINE)
             && !(ui->event & G_OPTIONMENU)) {
        ui->win_id = MAKE_GLOBAL_ID(ui->win_id, G_SCREEN);
        ui->vp_id  = INTERNAL_in_which_viewport(ui->win_id, ui->x,
                                       G_WIN_HEIGHT(id) - ui->y);

        INTERNAL_calc_viewport_xy(ui->win_id, ui->vp_id, ui->x,
                         ui->y, &ui->x, &ui->y);
        ui->command = NULL;
    }
    else
        ui->win_id = MAKE_GLOBAL_ID(ui->win_id, G_SCREEN);
}

int INTERNAL_process_event(int event_type, G_EVENT *ui)
{
    eventfunc_type func = event_array[event_type];
    return func(ui, event_data[event_type]);
}

void INTERNAL_add_event(G_EVENT *ui)
{
    q_tail++;
    if (q_tail > QMAX) q_tail = 0;
    if (q_tail == q_head) {
        (q_tail == 0) ? (q_tail = QMAX) : (q_tail--);
        return;
    }
    memcpy(&(event_queue[q_tail]), ui, sizeof(G_EVENT));
}

G_EVENT *INTERNAL_get_event(void)
{
    q_head++;
    if (q_head > QMAX) q_head = 0;
    return  (&(event_queue[q_head]));
}

G_EVENT *INTERNAL_peek_event(void)
{
    int head = q_head + 1;
    if (head > QMAX) head = 0;
    return (&(event_queue[head]));
}

static void init_queue(void)
{
    event_queue = (G_EVENT*) calloc(QMAX + 1,sizeof(G_EVENT));
    q_head = q_tail = 0;
}

static void exit_queue(void)
{
    free(event_queue);
    q_head = q_tail = 0;
}

int INTERNAL_is_event(void)
{
    return (!(q_head == q_tail));
}

void g_send_event(G_EVENT *ui)
{
    int i;
    for (i=0;i<MAXEVENTTYPE;i++)
        if (ui->event & (1<<i)) {
            eventfunc_type func = event_array[i];
            if (func(ui, event_data[i]))
                INTERNAL_add_event(ui);
            return;
        }
}

int g_peek_event(void)
{
    G_EVENT *ui;

    INTERNAL_dispatch_message();
    if (INTERNAL_is_event()) {
	ui = INTERNAL_peek_event();
	return ui->event;
    }
    return FALSE;
}


int g_get_event(G_EVENT * ui)
{
    int id;

    g_flush();
    while (!INTERNAL_is_event()) {
	INTERNAL_dispatch();
    } 
    memcpy(ui, INTERNAL_get_event(), sizeof(G_EVENT));
    if (ui->event & G_POINTERMOTION)
	return G_POINTERMOTION;
    return ui->event;
}


void INTERNAL_init_events(void)
{
    if (event_queue)
        return;
    init_eventhandlers();
    init_queue();
}

void INTERNAL_destroy_events(void)
{
    if (!event_queue)
        return;
    exit_queue();
    event_queue = NULL;
    init_eventhandlers();
}
