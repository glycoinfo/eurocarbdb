/********************************************************************/
/*                          psnd_int1d.c                            */
/*                                                                  */
/* 1998, Albert van Kuik                                            */
/********************************************************************/
#include <stdio.h>
#include <stdlib.h>
#include <ctype.h>
#include <string.h>
#include <stdarg.h>
#include <math.h>
#include <assert.h>
#include "genplot.h"
#include "psnd.h"
#include "nmrtool.h"

enum int1d_types {
    INT1D_LEFT=10,
    INT1D_RIGHT,
    INT1D_MOUSE,
    INT1D_INTEGRAL,
    INT1D_NOISE,
    INT1D_PRINT,
    INT1D_RESET,
    INT1D_WRITE
};

/*
 * add result of the latest integratin to the list
 */
static void add_integral(MBLOCK *mblk)
{
    INT1D_INFO * int1dinf, *p;
        
    p = mblk->int1dinf->next;
    while (p != NULL) {
        if (p->left == mblk->int1dinf->left &&
            p->right == mblk->int1dinf->right)
                return;;
        p = p->next;
    }
    int1dinf       = (INT1D_INFO*) calloc(sizeof(INT1D_INFO),1);
    assert(int1dinf);
    int1dinf->left    = mblk->int1dinf->left;
    int1dinf->right   = mblk->int1dinf->right;
    int1dinf->sum     = mblk->int1dinf->sum;
    int1dinf->next    = NULL;
    p = mblk->int1dinf;
    while (p->next != NULL) 
        p = p->next;
    p->next = int1dinf;
}

/*
 * Kill the list
 */
static void kill_integrals(MBLOCK *mblk)
{
    INT1D_INFO *p1, *p2;
        
    p1 = p2 = mblk->int1dinf->next;
    while (p1 != NULL) {
        p1 = p1->next;
        free(p2);
        p2 = p1;
    }
    mblk->int1dinf->next = NULL;
}

/*
 * print integrals in the list
 */
static void print_integrals(MBLOCK *mblk, FILE *outfile)
{
    INT1D_INFO *p1;
        
    p1 = mblk->int1dinf->next;
    while (p1 != NULL) {
        float ppm1, ppm2;
        ppm1 = psnd_calc_pos(p1->left, 
                             PAR->xref, 
                             PAR->aref, 
                             PAR->bref,
                             DAT->irc, 
                             TRUE, 
                             AXIS_UNITS_PPM, 
                             DAT->isize,
                             PAR->swhold, 
                             PAR->sfd, 
                             NULL, 
                             NULL);
        ppm2 = psnd_calc_pos(p1->right, 
                             PAR->xref, 
                             PAR->aref, 
                             PAR->bref,
                             DAT->irc, 
                             TRUE, 
                             AXIS_UNITS_PPM, 
                             DAT->isize,
                             PAR->swhold, 
                             PAR->sfd, 
                             NULL, 
                             NULL);
        if (outfile)
            fprintf(outfile," Range: %6d%6d,  %.4f %.4f ppm.  Area: %g\n", 
                      p1->left,
                      p1->right,ppm1,ppm2,
                      p1->sum);
        else
            psnd_printf(mblk," Range: %6d%6d,  %.4f %.4f ppm.  Area: %g\n", 
                      p1->left,
                      p1->right,ppm1,ppm2,
                      p1->sum);
        p1 = p1->next;
    }
}

static void write_integrals(MBLOCK *mblk)
{
    FILE *outfile = stdout;
    char *filename;

    if (mblk->int1dinf->next == NULL) {
        g_popup_messagebox(mblk->info->win_id, "Warning", "Empty list", FALSE);
        return;
    }

    filename = psnd_savefilename(mblk,"Write integrals to file","*");

    if (!filename) 
        return;

    if ((outfile = fopen(filename,"w")) == NULL) {
        char *label;
        label = psnd_sprintf_temp("Can not open file %s",filename);
        g_popup_messagebox(mblk->info->win_id, "Warning", label, FALSE);
        return;
    }

    print_integrals(mblk, outfile);
    fclose(outfile);
}


static void update_labels(MBLOCK *mblk)
{
    POPUP_INFO *popinf = mblk->popinf + POP_INT1D;

    if (popinf->cont_id) {
        char *label;
        label = psnd_sprintf_temp("%d", mblk->int1dinf->left);
        g_popup_set_label(popinf->cont_id, INT1D_LEFT, label);
        label = psnd_sprintf_temp("%d", mblk->int1dinf->right);
        g_popup_set_label(popinf->cont_id, INT1D_RIGHT, label);
    }
}

static void integrate1d_callback(G_POPUP_CHILDINFO *ci)
{
    MBLOCK *mblk = (MBLOCK*) ci->userdata;
    POPUP_INFO *popinf = mblk->popinf + POP_INT1D;
    static G_EVENT ui;

    switch (ci->type) {
        case G_CHILD_OK:
        /*
            psnd_integrate1d(mblk, 
                              mblk->int1dinf->left,
                              mblk->int1dinf->right);
                              */
        case G_CHILD_CANCEL:
            popinf->visible = FALSE;
            kill_integrals(mblk);
            ui.event   = G_COMMAND;
            ui.win_id  = mblk->info->win_id;
            ui.keycode = PSND_PL;
            g_send_event(&ui);
            return;
    }
    switch (ci->id) {
        case INT1D_LEFT:
            mblk->int1dinf->left = psnd_scan_integer(ci->label);
            mblk->int1dinf->left = max(mblk->int1dinf->left, 1);
            mblk->int1dinf->left = min(mblk->int1dinf->left, DAT->isize);
            break;
        case INT1D_RIGHT:
            mblk->int1dinf->right = psnd_scan_integer(ci->label);
            mblk->int1dinf->right = max(mblk->int1dinf->right, 1);
            mblk->int1dinf->right = min(mblk->int1dinf->right, DAT->isize);
            break;
        case INT1D_MOUSE:
            psnd_set_cursormode(mblk, 0, MOUSE_INTEGRATION);
            break;
        case INT1D_NOISE:
            psnd_rmsnoise(mblk, 
                           mblk->int1dinf->left,
                           mblk->int1dinf->right, 
                           1, 1);
            break;
        case INT1D_INTEGRAL:
            psnd_integrate1d(mblk, 
                              mblk->int1dinf->left,
                              mblk->int1dinf->right);
            break;
        case INT1D_PRINT:
            print_integrals(mblk,NULL);
            break;

        case INT1D_WRITE:
            write_integrals(mblk);
            break;

        case INT1D_RESET:
            kill_integrals(mblk);
            ui.event   = G_COMMAND;
            ui.win_id  = mblk->info->win_id;
            ui.keycode = PSND_PL;
            g_send_event(&ui);
            break;
    }
}

/*
 * Popup menu 
 */
void psnd_int1d_popup(MBLOCK *mblk)
{
    int i, ok;
    G_POPUP_CHILDINFO ci[20];
    int id;
    char *label;
    POPUP_INFO *popinf = mblk->popinf + POP_INT1D;

    if (popinf->visible)
        return;
    popinf->visible = TRUE;
    if (!popinf->cont_id) {
        int cont_id;
        if (!mblk->int1dinf->left) {
            mblk->int1dinf->left   = 1;
            mblk->int1dinf->right  = DAT->isize;
        }
        mblk->int1dinf->left = max(mblk->int1dinf->left, 1);
        mblk->int1dinf->left = min(mblk->int1dinf->left, DAT->isize);
        mblk->int1dinf->right = max(mblk->int1dinf->right, 1);
        mblk->int1dinf->right = min(mblk->int1dinf->right, DAT->isize);
        cont_id = g_popup_container_open(mblk->info->win_id, 
                      "Integrate 1D",  G_POPUP_KEEP|G_POPUP_SINGLEBUTTON);
        popinf->cont_id = cont_id;
        label = psnd_sprintf_temp("%d", mblk->int1dinf->left);
        id=0;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_TEXTBOX;
        ci[id].id            = INT1D_LEFT;
        ci[id].title         = "Left  ";
        ci[id].func          = integrate1d_callback;
        ci[id].userdata      = (void*) mblk;
        ci[id].item_count    = 20;
        ci[id].items_visible = 10;
        ci[id].label         = label;
        ci[id].horizontal    = TRUE;
        g_popup_add_child(cont_id, &(ci[id]));

        label = psnd_sprintf_temp("%d", mblk->int1dinf->right);
        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_TEXTBOX;
        ci[id].id            = INT1D_RIGHT;
        ci[id].title         = "Right ";
        ci[id].func          = integrate1d_callback;
        ci[id].userdata      = (void*) mblk;
        ci[id].item_count    = 20;
        ci[id].items_visible = 10;
        ci[id].label         = label;
        ci[id].horizontal    = TRUE;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_PUSHBUTTON;
        ci[id].id    = INT1D_MOUSE;
        ci[id].label = "Grab Mouse Button 1";
        ci[id].func  = integrate1d_callback;
        ci[id].userdata      = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_PUSHBUTTON;
        ci[id].id    = INT1D_INTEGRAL;
        ci[id].label = "Calc Integral";
        ci[id].func  = integrate1d_callback;
        ci[id].userdata      = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));
/*
        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_PUSHBUTTON;
        ci[id].id    = INT1D_NOISE;
        ci[id].label = "Calc Noise";
        ci[id].func  = integrate1d_callback;
        ci[id].userdata      = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));
*/
        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_PUSHBUTTON;
        ci[id].id    = INT1D_PRINT;
        ci[id].label = "Print Integrals";
        ci[id].func  = integrate1d_callback;
        ci[id].userdata      = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_PUSHBUTTON;
        ci[id].id    = INT1D_WRITE;
        ci[id].label = "Write to disk";
        ci[id].func  = integrate1d_callback;
        ci[id].userdata      = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_PUSHBUTTON;
        ci[id].id    = INT1D_RESET;
        ci[id].label = "Clear list";
        ci[id].func  = integrate1d_callback;
        ci[id].userdata      = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_OK;
        ci[id].func  = integrate1d_callback;
        ci[id].userdata      = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_CANCEL;
        ci[id].func  = integrate1d_callback;
        ci[id].userdata      = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));
    }
    mblk->int1dinf->left = max(mblk->int1dinf->left, 1);
    mblk->int1dinf->left = min(mblk->int1dinf->left, DAT->isize);
    mblk->int1dinf->right = max(mblk->int1dinf->right, 1);
    mblk->int1dinf->right = min(mblk->int1dinf->right, DAT->isize);
    g_popup_container_show(popinf->cont_id) ;
}

/*
* Integrate peaks
*/
void psnd_integrate1d(MBLOCK *mblk, float x1, float x2)
{
    int i;
    int i1 = round(x1);
    int i2 = round(x2);
    int lim1 = min(i1, i2);
    int lim2 = max(i1, i2);    
    int xmin, xmax;
    float sum0, sum1, sum, ymin, ymax, yrange,xx;
    char *label;
    POPUP_INFO *popinf = mblk->popinf + POP_INT1D;
    
    lim1 = max(1, lim1);
    lim2 = max(1, lim2);
    lim1 = min(DAT->isize, lim1);
    lim2 = min(DAT->isize, lim2);

    mblk->int1dinf->left  = min(lim1,lim2);
    mblk->int1dinf->right = max(lim1,lim2);
    update_labels(mblk);

    lim1 = mblk->int1dinf->left;
    lim2 = mblk->int1dinf->right;
    /*
     * ... determine integral over visible area (and min and max)
     */
    for (i=0,sum0=0;i<DAT->isize;i++)
        sum0 += DAT->xreal[i];
    if (sum0 == 0)
        sum0 = 1;
    /*        
     *
     * ... determine integral over selected area (and min and max)
     * ... and draw the intensity lines
     *
     */
    g_append_object(mblk->spar[S_REAL].obj_id);
    g_set_foreground(mblk->spar[S_REAL].color);
    
    for (i=lim1,sum=0,xmin=lim1,xmax=lim1;i<=lim2;i++) {
        sum += DAT->xreal[i-1];
        g_moveto((float) i, DAT->xreal[i-1]);
        g_lineto((float) i, 0.0);
    }
    /*
     *
     * ...  calc integral
     *
     */
    g_set_foreground(mblk->spar[S_REAL].color3);
    minmax(DAT->xreal,DAT->isize,&(DAT->ylimit[0]),&(DAT->ylimit[1]));
    sum1=0;
    yrange = (DAT->ylimit[1] - DAT->ylimit[0])/ sum0;
    for (i=lim1;i<lim2;i++) {
        sum1 += yrange * DAT->xreal[i-1];
        DAT->work1[i-1] = sum1;
    }
    /*
     * Try to move integral on top of peak
     */
    xx=DAT->ylimit[0];
    for (i=lim1;i<lim2;i++) {
        if (xx < (DAT->xreal[i-1]-DAT->work1[i-1]))
            xx = DAT->xreal[i-1]-DAT->work1[i-1];
    }
    /*
     * But not outside window
     */
    if (DAT->work1[lim2-2]+xx>DAT->ylimit[1])
        xx -= -DAT->ylimit[1] + (DAT->work1[lim2-2] + xx);
    xx += (DAT->ylimit[1] - DAT->ylimit[0])/100;
    for (i=lim1;i<lim2;i++) {
        DAT->work1[i-1] += xx;
    }
    /*
     * Draw integral
     */
    g_moveto((float) lim1, DAT->work1[lim1-1]);
    for (i=lim1;i<lim2;i++) {
        g_lineto((float)i, DAT->work1[i-1]);
    }
    /*
     * Print label
     */
    g_moveto((float) lim1, DAT->work1[lim1-1]+(DAT->work1[lim2-2]-DAT->work1[lim1-1])/2);
    label = psnd_sprintf_temp("%.2e", sum);
/*    g_set_motif_realtextrotation(1);
    g_set_textdirection(90);*/
    g_label(label);
/*    g_set_textdirection(0);*/
    g_set_foreground(mblk->spar[S_REAL].color);
    g_close_object(mblk->spar[S_REAL].obj_id);
    g_plotall();
/*    g_set_motif_realtext rotation(0);*/
    mblk->int1dinf->sum = sum;
    if (popinf->visible)
        add_integral(mblk);
    psnd_printf(mblk," Range: %6d%6d Area: %g\n", lim1, lim2, sum);
}


