/************************************************************************/
/*                               genmcomm.c                             */
/*                                                                      */
/*  Platform : Motif                                                    */
/*  Module   : Genplot User Interface functions                         */
/*                                                                      */
/*  Albert van Kuik                                                     */
/************************************************************************/

#include <X11/Xlib.h>
#include <X11/Xutil.h>
#include <X11/cursorfont.h>

#include <X11/StringDefs.h>
#include <X11/Intrinsic.h>
#include <X11/Shell.h>

#include <X11/keysym.h>

#include <Xm/Xm.h>
#include <Xm/DialogS.h>
#include <Xm/DrawingA.h>
#include <Xm/Form.h>
#include <Xm/CascadeBG.h>
#include <Xm/SeparatoG.h>
#include <Xm/PushBG.h>
#include <Xm/PushB.h>
#include <Xm/ToggleBG.h>
#include <Xm/RowColumn.h>
#include <Xm/ScrolledW.h>
#include <Xm/ScrollBar.h>
#include <Xm/Command.h>
#include <Xm/Text.h>
#include <Xm/MessageB.h>
#include <Xm/SelectioB.h>
#include <Xm/FileSB.h>
#include <Xm/List.h>
#include <Xm/Frame.h>
#include <Xm/MainW.h>
#include <Xm/ToggleB.h>
#include <Xm/Label.h>
#include <Xm/PanedW.h>
#include <Xm/DrawnB.h>

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
#include <time.h>
#include <stdarg.h>

#ifdef DEBUG
#include "mshell.h"
#endif

#include "genplot.h"
#include "g_inter.h"
#include "genmotif.h"

/************************************************************************/
/* COMMAND WIDGET STUFF
*/
static void process_keyqueue(int);

/* --- COMMAND QUEUE --- */
#define COMMANDQMAX 25
static char *command_queue[COMMANDQMAX+1];
static int q_commandhead, q_commandtail;

static void add_command(char *command)
{
    if (command == NULL)
        return;
    q_commandtail++;
    if (q_commandtail > COMMANDQMAX) q_commandtail = 0;
    if (q_commandtail == q_commandhead) {
        (q_commandtail == 0) ? (q_commandtail = COMMANDQMAX) : (q_commandtail--);
        return;
    }
    if (command_queue[q_commandtail] != NULL)
        free (command_queue[q_commandtail]);
    command_queue[q_commandtail] = (char*) malloc(strlen(command) + 1);
    strcpy(command_queue[q_commandtail],command);
}

static int is_command(void)
{
    return (!(q_commandhead == q_commandtail));
}


void INTERNAL_init_commandqueue(void)
{
    int i;

    for (i=0;i<=COMMANDQMAX;i++)
        command_queue[i] = NULL;
    q_commandhead = q_commandtail = 0;
    for (i=0;i<G_MAXWINDOW;i++)
        xwini[i].command_widget = NULL;
}

void INTERNAL_exit_commandqueue(void)
{
    int i;

    for (i=0;i<=COMMANDQMAX;i++)
        if (command_queue[i] != NULL) {
            free(command_queue[i]);
            command_queue[i] = NULL;
        }
    q_commandhead = q_commandtail = 0;
}

static char *get_commandline(void)
{
    if (!is_command())
        return NULL;
    q_commandhead++;
    if (q_commandhead > COMMANDQMAX) q_commandhead = 0;
    return  command_queue[q_commandhead];
}


/* --- KEYBOARD QUEUE --- */

static void add_key(int win_id, XKeyEvent *key)
{
    xwini[win_id].q_keytail++;
    if (xwini[win_id].q_keytail > KEYQMAX) 
        xwini[win_id].q_keytail = 0;
    if (xwini[win_id].q_keytail == xwini[win_id].q_keyhead) {
        (xwini[win_id].q_keytail == 0) ? 
            (xwini[win_id].q_keytail = KEYQMAX) : (xwini[win_id].q_keytail--);
        g_bell();
        return;
    }
    memcpy(&(xwini[win_id].key_queue[xwini[win_id].q_keytail]), key, sizeof(XKeyEvent));
}

static int is_key(int win_id)
{
    return (!(xwini[win_id].q_keyhead == xwini[win_id].q_keytail));
}


void INTERNAL_init_keyqueue(int win_id)
{
    xwini[win_id].q_locking = FALSE;
    xwini[win_id].q_keylock = FALSE;;
    xwini[win_id].q_keyhead = xwini[win_id].q_keytail = 0;
}

static void set_keylock(int win_id, int tf)
{
    if (!xwini[win_id].q_locking)
        return;
    xwini[win_id].q_keylock = tf;
    if (tf == FALSE) 
        process_keyqueue(win_id);
}

static XKeyEvent *get_key(int win_id)
{
    if (!is_key(win_id))
        return NULL;
    xwini[win_id].q_keyhead++;
    if (xwini[win_id].q_keyhead > KEYQMAX) 
        xwini[win_id].q_keyhead = 0;
    return  (XKeyEvent *) &(xwini[win_id].key_queue[xwini[win_id].q_keyhead]);
}

static void process_keyqueue(int win_id)
{
    int i;
    KeySym  k;
    Widget w;    
    XKeyEvent *event;

    w = XmCommandGetChild(xwini[win_id].command_widget, XmDIALOG_COMMAND_TEXT);
    for (i=0; ((event = get_key(win_id)) != NULL); i++) {
        event->time = (Time) time(NULL);
        k = XKeycodeToKeysym(INTERNAL_display, event->keycode, 0);
        XSendEvent(INTERNAL_display, XtWindow(w), FALSE, KeyPressMask, (XEvent*) event);
            
        if (k == XK_Return)
            return;         
    }
}

/* ----------------- */

void INTERNAL_trap_keypress_proc(Widget w, XtPointer client_data, XtPointer call_data)
{
    XmTextVerifyCallbackStruct *cb = (XmTextVerifyCallbackStruct*)call_data;
    int id = (int) client_data;
    
    if (cb->event && cb->event->type == KeyPress) {
        if (xwini[id].q_keylock) { 
           cb->doit = FALSE;
            if (cb->text->ptr)
                add_key(id, (XKeyEvent*) cb->event);
        
        }
    }
}

void INTERNAL_command_proc(Widget w, XtPointer client_data, XtPointer call_data)
{
    XmCommandCallbackStruct *cb = (XmCommandCallbackStruct*)call_data;
    char *textline;
    G_USERINPUT ui;
    int id = (int) client_data;


    textline = NULL;
    XmStringGetLtoR(cb->value,
                    XmSTRING_DEFAULT_CHARSET,
                    &textline);
    add_command(textline);

    ui.win_id  = id;
    ui.vp_id   = G_ERROR;
    ui.x       = 0;
    ui.y       = 0;
    ui.keycode = 0;
    ui.event   = G_COMMANDLINE;
    ui.command = get_commandline();
    INTERNAL_prep_event(&ui);
    if (INTERNAL_process_event(G_COMMANDLINE_EVENT, &ui))
        INTERNAL_add_event(&ui);
}

void INTERNAL_command_add_return_proc(Widget w, XtPointer client_data, 
                                                            XEvent *event)
{
    KeySym ks = 0;
    XKeyEvent *keyevent = (XKeyEvent*) event;
    int id = (int) client_data;
    char *txt;
    int reset_history = FALSE;
    
    ks = XKeycodeToKeysym(INTERNAL_display, keyevent->keycode, 0);
    if (ks == XK_Right || ks == XK_KP_Right)
        XmTextFieldClearSelection(w, CurrentTime);
    if (ks == XK_BackSpace && xwini[id].q_keylock) {
        add_key(id, keyevent);
        return;
    }
    if (ks == XK_Return || ks == XK_KP_Enter) {
        if (xwini[id].q_keylock) {
            add_key(id, keyevent);
            return;
        }
        txt = XmTextGetString(w);
        if (strlen(txt) == 0) {
            G_USERINPUT ui;

            add_command("");
            ui.win_id  = id;
            ui.vp_id   = G_ERROR;
            ui.x       = 0;
            ui.y       = 0;
            ui.keycode = 0;
            ui.event   = G_COMMANDLINE;
            ui.command = get_commandline();
            INTERNAL_prep_event(&ui);
            if (INTERNAL_process_event(G_COMMANDLINE_EVENT, &ui))
                INTERNAL_add_event(&ui);
        }
        XtFree(txt);
        set_keylock(id, TRUE);
        reset_history = TRUE;        		      
    }
    if (ks == XK_Escape) 
        reset_history = TRUE;
    if (reset_history) {
        XmListSetBottomPos(XmCommandGetChild(XtParent(w), 
                                             XmDIALOG_HISTORY_LIST), 0);
        XmListSelectPos(XmCommandGetChild(XtParent(w), 
                                          XmDIALOG_HISTORY_LIST), 0, FALSE);
        XmListDeselectPos(XmCommandGetChild(XtParent(w), 
                                            XmDIALOG_HISTORY_LIST), 0);
    }
}    


/*-------------------*/
void g_SCREEN_set_commandline(int win_id, char *command)
{
    int n, id;
    Arg args[10];

    id = GET_LOCAL_ID(win_id);
    if (xwini[id].command_widget == NULL || command == NULL)
        return;
    XmCommandSetValue(xwini[id].command_widget,
                      XmStringCreateLtoR(command ,XmSTRING_DEFAULT_CHARSET));
    n = 0;
    XtSetArg(args[n], XmNcursorPosition, strlen(command));
    n++;
    XtSetValues(XmCommandGetChild(xwini[id].command_widget,XmDIALOG_COMMAND_TEXT),args, n);
    XmTextSetSelection(XmCommandGetChild(xwini[id].command_widget,XmDIALOG_COMMAND_TEXT),
                       0, strlen(command), CurrentTime);
}

void g_SCREEN_set_commandprompt(int win_id, char *prompt)
{
    int n, id;
    Arg args[10];

    id = GET_LOCAL_ID(win_id);
    if (xwini[id].command_widget == NULL)
        return;
    n=0;
    XtSetArg(args[n], XmNpromptString,
             XmStringCreateLtoR(prompt ,XmSTRING_DEFAULT_CHARSET));
    n++;
    XtSetValues(xwini[id].command_widget, args, n);
}


void g_SCREEN_set_commandhistory(int win_id, char *history)
{
    int id;
    Widget list;

    id = GET_LOCAL_ID(win_id);
    if (xwini[id].command_widget == NULL || history == NULL)
        return;
    list = XmCommandGetChild(xwini[id].command_widget, XmDIALOG_HISTORY_LIST);
    XmListAddItemUnselected(list,
                            XmStringCreateLtoR(history,
                                               XmSTRING_DEFAULT_CHARSET),
                            0);
    XmListSetBottomPos(list, 0); 
    XmListSelectPos(list, 0, FALSE);
    XmListDeselectPos(list, 0);
}


void g_SCREEN_enable_nextcommand(int win_id)
{
    int id = GET_LOCAL_ID(win_id);
    set_keylock(id, FALSE);
}


void g_SCREEN_set_commandlocking(int win_id, int tf)
{
    int id = GET_LOCAL_ID(win_id);
    xwini[id].q_locking = tf;
    xwini[id].q_keylock = tf;
    if (tf == FALSE) 
        process_keyqueue(id);
}


void g_SCREEN_enable_commandinput(int win_id, int enable)
{
    int id = GET_LOCAL_ID(win_id);

    if (xwini[id].command_widget)
        XtSetSensitive(xwini[id].command_widget, enable);
}
