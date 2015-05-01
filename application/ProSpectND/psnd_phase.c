/********************************************************************/
/*                             psnd_phase.c                         */
/*                                                                  */
/* Do interactive phase correction with buttons                     */
/* 1998, Albert van Kuik                                            */
/********************************************************************/
#include <stdio.h>
#include <stdlib.h>
#include <ctype.h>
#include <string.h>
#include <stdarg.h>
#include <assert.h>
#include <math.h>
#include "genplot.h"
#include "psnd.h"
#include "mathtool.h"
#include "nmrtool.h"

typedef enum {
    PHASE_P0_PLUS=100,
    PHASE_P0_PLUSPLUS,
    PHASE_P0_MINUS,
    PHASE_P0_MINUSMINUS,
    PHASE_P0_LABEL,
    PHASE_P1_PLUS,
    PHASE_P1_PLUSPLUS,
    PHASE_P1_MINUS,
    PHASE_P1_MINUSMINUS,
    PHASE_P1_LABEL,
    PHASE_I0_LABEL,
    PHASE_NORMALIZE,
    PHASE_SET_PAR,
    PHASE_RESET_PAR,
    PHASE_BIGGEST,
    PHASE_PK,
    PHASE_MOUSE_P0,
    PHASE_MOUSE_P1,
    PHASE_MOUSE_I0,
    PHASE_AUTO_P0,
    PHASE_AUTO_P1,
    PHASE_AUTO,
    PHASE_SUM_ROWS
} phase_ids;

static void sum_all_rows(MBLOCK *mblk);

static void update_labels(MBLOCK *mblk)
{
    POPUP_INFO *popinf = mblk->popinf + POP_PHASE1D;

    if (popinf->cont_id) {
        char *label;
        label = psnd_sprintf_temp("%4.1f", PAR->ahold);
        g_popup_set_label(popinf->cont_id, PHASE_P0_LABEL, label);
        label = psnd_sprintf_temp("%4.1f", PAR->bhold);
        g_popup_set_label(popinf->cont_id, PHASE_P1_LABEL, label);
        label = psnd_sprintf_temp("%6d", PAR->ihold);
        g_popup_set_label(popinf->cont_id, PHASE_I0_LABEL, label);
    }
}

static int biggest(int size, int vp_id, float *x, float *y)
{
    int    i, result = 1;
    float  value = 0.0;
    float  xx1,yy1,xx2,yy2;
    int    x1,x2;

    g_get_world(vp_id,&xx1,&yy1,&xx2,&yy2);
    x1  = round(xx1);
    x1  = max(x1, 1);
    x1  = min(x1, size);
    x2  = round(xx2);
    x2  = max(x2, 1);
    x2  = min(x2, size);

    for (i=x1-1;i<x2;i++) {
        float xx = fabs(x[i]) + fabs(y[i]);
        if (xx > value) {
            result = i+1;
            value  = xx;
        }
    }
    return result;
}

static void phasecallback(G_POPUP_CHILDINFO *ci)
{
    float a=0.0, b=0.0, *xr, *xi;
    int vp_id;
    MBLOCK *mblk = (MBLOCK*) ci->userdata;
    POPUP_INFO *popinf = mblk->popinf + POP_PHASE1D;
    static int phase_lock;

    switch (ci->type) {
        case G_CHILD_OK:
        case G_CHILD_CANCEL:
            popinf->visible = FALSE;
            return;
    }
    if (phase_lock) 
        return;
    switch (ci->id) {
        case PHASE_MOUSE_P0:
            psnd_set_cursormode(mblk, 0, MOUSE_P0);
            break;
        case PHASE_MOUSE_P1:
            psnd_set_cursormode(mblk, 0, MOUSE_P1);
            break;
        case PHASE_MOUSE_I0:
            psnd_set_cursormode(mblk, 0, MOUSE_I0);
            break;
        case PHASE_P0_PLUSPLUS:
            a=10.0;
            break;
        case PHASE_P0_PLUS:
            a=1.0;
            break;
        case PHASE_P0_MINUS:
            a=-1.0;
            break;
        case PHASE_P0_MINUSMINUS:
            a=-10.0;
            break;
        case PHASE_P1_PLUSPLUS:
            b=10.0;
            break;
        case PHASE_P1_PLUS:
            b=1.0;
            break;
        case PHASE_P1_MINUS:
            b=-1.0;
            break;
        case PHASE_P1_MINUSMINUS:
            b=-10.0;
            break;
        case PHASE_SET_PAR:
            psnd_set_param(mblk, 0, NULL, PSND_PARAM_PHASE);
            update_labels(mblk);
            return;
        case PHASE_RESET_PAR:
            psnd_pz(mblk,PAR);
            update_labels(mblk);
            return;
        case PHASE_NORMALIZE:
            psnd_set_phase_position(mblk, PAR, DAT, 1);
            return;
        case PHASE_BIGGEST:
            vp_id = psnd_get_vp_id(mblk);
            if (vp_id == mblk->spar[S_PHASE1].vp_id) {
                xr = mblk->cpar_screen->xreal[0];
                xi = mblk->cpar_screen->ximag[0];
            }
            else if (vp_id == mblk->spar[S_PHASE2].vp_id) {
                xr = mblk->cpar_screen->xreal[1];
                xi = mblk->cpar_screen->ximag[1];
            }
            else if (vp_id == mblk->spar[S_PHASE3].vp_id) {
                xr = mblk->cpar_screen->xreal[2];
                xi = mblk->cpar_screen->ximag[2];
            }
            else {
                xr = DAT->xreal;
                xi = DAT->ximag;
            }
            psnd_set_phase_position(mblk, PAR, DAT, 
                       biggest(DAT->isize, vp_id, xr, xi));
            return;
        case PHASE_AUTO_P0: 
            if (psnd_auto_phase(mblk, MODE_P0) == FALSE)
                return;
            break;
        case PHASE_AUTO_P1: 
            if (psnd_auto_phase(mblk, MODE_P1) == FALSE)
                return;
            break;
        case PHASE_AUTO: 
            if (psnd_auto_phase(mblk, MODE_P0+MODE_P1) == FALSE)
                return;
            break;
        case PHASE_SUM_ROWS:
            sum_all_rows(mblk);
            break;
        case PHASE_PK:
            psnd_pk(mblk, PAR, DAT);
            /* Fall through */
    }
    phase_lock = TRUE;
    if (ci->id != PHASE_PK && 
        ci->id != PHASE_AUTO &&
        ci->id != PHASE_AUTO_P0 &&
        ci->id != PHASE_AUTO_P1 &&
        ci->id != PHASE_SUM_ROWS)
            psnd_phase(mblk, a, b);
                    
    if (mblk->info->phase_2d) {
        psnd_plot(mblk,TRUE,mblk->info->dimension_id);
        psnd_show_2d_phase_data(mblk);
        g_plotall();
    }
    else { 
        psnd_phaseplot1d(mblk);
        g_plotall();
    }
    g_peek_event();
    phase_lock=FALSE;
}


#define MAXID		42
void psnd_popup_phase(MBLOCK *mblk)
{
    static G_POPUP_CHILDINFO ci[MAXID];
    char *label;
    POPUP_INFO *popinf = mblk->popinf + POP_PHASE1D;

    if (popinf->visible)
        return;
    popinf->visible = TRUE;
    if (popinf->cont_id==0) {
        int cont_id;
        int i,id=0,item_count,item;
        cont_id = g_popup_container_open(mblk->info->win_id, "Phase",
            G_POPUP_KEEP|G_POPUP_SINGLEBUTTON);
        popinf->cont_id = cont_id;
        /*
         * Open "p0" sub-panel
         */
        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type       = G_CHILD_PANEL;
        ci[id].item       = TRUE;
        ci[id].frame      = TRUE;
        ci[id].title      = "P0";
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type       = G_CHILD_PANEL;
        ci[id].item       = TRUE;
        ci[id].horizontal = TRUE;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type       = G_CHILD_PUSHBUTTON;
        ci[id].id         = PHASE_P0_PLUSPLUS;
        ci[id].label      = "++";
        ci[id].func       = phasecallback;
        ci[id].userdata   = (void*)mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type       = G_CHILD_PUSHBUTTON;
        ci[id].id         = PHASE_P0_PLUS;
        ci[id].label      = "+";
        ci[id].func       = phasecallback;
        ci[id].userdata   = (void*)mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type       = G_CHILD_PUSHBUTTON;
        ci[id].id         = PHASE_P0_MINUS;
        ci[id].label      = "-";
        ci[id].func       = phasecallback;
        ci[id].userdata   = (void*)mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type       = G_CHILD_PUSHBUTTON;
        ci[id].id         = PHASE_P0_MINUSMINUS;
        ci[id].label      = "--";
        ci[id].func       = phasecallback;
        ci[id].userdata   = (void*)mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        label = psnd_sprintf_temp("%4.1f", PAR->ahold);
        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type       = G_CHILD_LABEL;
        ci[id].id         = PHASE_P0_LABEL;
        ci[id].label      = label;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type       = G_CHILD_PANEL;
        ci[id].item       = FALSE;
        g_popup_add_child(cont_id, &(ci[id]));


        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type       = G_CHILD_PUSHBUTTON;
        ci[id].id         = PHASE_MOUSE_P0;
        ci[id].label      = "Grab Mouse Button 1";
        ci[id].func       = phasecallback;
        ci[id].userdata   = (void*)mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        /*
         * Close "p0" sub-panel
         */
        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type       = G_CHILD_PANEL;
        ci[id].item       = FALSE;
        g_popup_add_child(cont_id, &(ci[id]));
        /*
         * Open "p1" sub-panel
         */
        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type       = G_CHILD_PANEL;
        ci[id].item       = TRUE;
        ci[id].frame      = TRUE;
        ci[id].title      = "P1";
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type       = G_CHILD_PANEL;
        ci[id].item       = TRUE;
        ci[id].horizontal = TRUE;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type       = G_CHILD_PUSHBUTTON;
        ci[id].id         = PHASE_P1_PLUSPLUS;
        ci[id].label      = "++";
        ci[id].func       = phasecallback;
        ci[id].userdata   = (void*)mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type       = G_CHILD_PUSHBUTTON;
        ci[id].id         = PHASE_P1_PLUS;
        ci[id].label      = "+";
        ci[id].func       = phasecallback;
        ci[id].userdata   = (void*)mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type       = G_CHILD_PUSHBUTTON;
        ci[id].id         = PHASE_P1_MINUS;
        ci[id].label      = "-";
        ci[id].func       = phasecallback;
        ci[id].userdata   = (void*)mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type       = G_CHILD_PUSHBUTTON;
        ci[id].id         = PHASE_P1_MINUSMINUS;
        ci[id].label      = "--";
        ci[id].func       = phasecallback;
        ci[id].userdata   = (void*)mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        label = psnd_sprintf_temp("%4.1f", PAR->bhold);
        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type       = G_CHILD_LABEL;
        ci[id].id         = PHASE_P1_LABEL;
        ci[id].label      = label;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type       = G_CHILD_PANEL;
        ci[id].item       = FALSE;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type       = G_CHILD_PUSHBUTTON;
        ci[id].id         = PHASE_MOUSE_P1;
        ci[id].label      = "Grab Mouse Button 1";
        ci[id].func       = phasecallback;
        ci[id].userdata   = (void*)mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        /*
         * Close "p1" sub-panel
         */
        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type       = G_CHILD_PANEL;
        ci[id].item       = FALSE;
        g_popup_add_child(cont_id, &(ci[id]));

        /*
         * Open "i0" sub-panel
         */
        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type       = G_CHILD_PANEL;
        ci[id].item       = TRUE;
        ci[id].frame      = TRUE;
        ci[id].title      = "I0";
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type       = G_CHILD_PANEL;
        ci[id].item       = TRUE;
        ci[id].horizontal = TRUE;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type       = G_CHILD_PUSHBUTTON;
        ci[id].id         = PHASE_BIGGEST;
        ci[id].label      = "Biggest";
        ci[id].func       = phasecallback;
        ci[id].userdata   = (void*)mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        label = psnd_sprintf_temp("%6d", PAR->ihold);
        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type       = G_CHILD_LABEL;
        ci[id].id         = PHASE_I0_LABEL;
        ci[id].label      = label;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type       = G_CHILD_PANEL;
        ci[id].item       = FALSE;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type       = G_CHILD_PUSHBUTTON;
        ci[id].id         = PHASE_MOUSE_I0;
        ci[id].label      = "Grab Mouse Button 1";
        ci[id].func       = phasecallback;
        ci[id].userdata   = (void*)mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        /*
         * Close "i0" sub-panel
         */
        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type       = G_CHILD_PANEL;
        ci[id].item       = FALSE;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type       = G_CHILD_PUSHBUTTON;
        ci[id].id         = PHASE_SET_PAR;
        ci[id].label      = "Set parameters          ";
        ci[id].func       = phasecallback;
        ci[id].userdata   = (void*)mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type       = G_CHILD_PUSHBUTTON;
        ci[id].id         = PHASE_RESET_PAR;
        ci[id].label      = "Reset parameters      PZ";
        ci[id].func       = phasecallback;
        ci[id].userdata   = (void*)mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type       = G_CHILD_PUSHBUTTON;
        ci[id].id         = PHASE_NORMALIZE;
        ci[id].label      = "Normalize parameters  PN";
        ci[id].func       = phasecallback;
        ci[id].userdata   = (void*)mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type       = G_CHILD_PUSHBUTTON;
        ci[id].id         = PHASE_PK;
        ci[id].label      = "Phase Correction      PK";
        ci[id].func       = phasecallback;
        ci[id].userdata   = (void*)mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        /*
         * Open "Auto phase" sub-panel
         */
        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type       = G_CHILD_PANEL;
        ci[id].item       = TRUE;
        ci[id].frame      = TRUE;
        ci[id].horizontal = TRUE;
        ci[id].title      = "Auto phase";
        g_popup_add_child(cont_id, &(ci[id]));
        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type       = G_CHILD_PUSHBUTTON;
        ci[id].id         = PHASE_AUTO;
        ci[id].label      = " P0 + P1 ";
        ci[id].func       = phasecallback;
        ci[id].userdata   = (void*)mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type       = G_CHILD_PUSHBUTTON;
        ci[id].id         = PHASE_AUTO_P0;
        ci[id].label      = " P0 ";
        ci[id].func       = phasecallback;
        ci[id].userdata   = (void*)mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type       = G_CHILD_PUSHBUTTON;
        ci[id].id         = PHASE_AUTO_P1;
        ci[id].label      = " P1 ";
        ci[id].func       = phasecallback;
        ci[id].userdata   = (void*)mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type       = G_CHILD_PUSHBUTTON;
        ci[id].id         = PHASE_SUM_ROWS;
        ci[id].label      = "SumR";
        ci[id].func       = phasecallback;
        ci[id].userdata   = (void*)mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        /*
         * Close "Auto phase" sub-panel
         */
        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type       = G_CHILD_PANEL;
        ci[id].item       = FALSE;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_OK;
        ci[id].func  = phasecallback;
        ci[id].userdata      = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_CANCEL;
        ci[id].func  = phasecallback;
        ci[id].userdata      = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        assert(id<MAXID);
    }
    g_popup_container_show(popinf->cont_id);   
}


/***********************************************
 * Phase stuff
 */
 
static void phase1d(MBLOCK *mblk, float p0, float p1, 
                    PBLOCK *par, DBLOCK *dat)
{
    int i0 = 1;
    int start = max(par->icstrt,1) - 1;

    if (p0 == 0)
        i0 = par->ihold;
    par->iphase = DOPHA;
    phase(dat->xreal + start,
          dat->ximag + start,
          dat->isize,
          p0,
          p1,
          i0);
    update_labels(mblk);
           
}

static void phase2d(MBLOCK *mblk, float p0, float p1, CBLOCK *cpar, 
                    PBLOCK *par, DBLOCK *dat)
{
    int i0 = 1,i;

    if (p0 == 0)
        i0 = par->ihold;
    par->iphase = DOPHA;
    for (i=0;i<3;i++)
        phase(cpar->xreal[i],
              cpar->ximag[i],
              dat->isize,
              p0,
              p1,
              i0);           
    update_labels(mblk);
}

void psnd_phase(MBLOCK *mblk, float p0, float p1)
{
    PBLOCK *par_phase = PAR;
    DBLOCK *dat = DAT;
    CBLOCK *cpar = mblk->cpar_screen;

    if (mblk->info->phase_2d) {
        if (cpar->phase_direction == cpar->par1->icrdir)
            par_phase = cpar->par1;
        else if (cpar->phase_direction == cpar->par2->icrdir)
            par_phase = cpar->par2;
        else
            return;
    }
    if (p1 == 0)
        par_phase->ahold += p0;
    else
        par_phase->bhold += p1;
    if (mblk->info->phase_2d) 
        phase2d(mblk, p0, p1, cpar, par_phase, dat);
    else
        phase1d(mblk, p0, p1, par_phase, dat);
}

void psnd_pk(MBLOCK *mblk, PBLOCK *par, DBLOCK *dat)
{
    phase(dat->xreal,
          dat->ximag,
          dat->isize,
          par->ahold,
          par->bhold,
          par->ihold);
    par->iphase =DOPHA;           
    update_labels(mblk);
}

int psnd_do_pk(MBLOCK *mblk, int argc, char *argv[])
{
    int win_id = mblk->info->win_id;
    CBLOCK *cpar = mblk->cpar_screen;
    PBLOCK *par = PAR;
    DBLOCK *dat = DAT;

    if (argc == 0) {
        /*
         * argc == 0 means:
         * Command from a button, NOT from command line
         */
        if (psnd_set_param(mblk, 0, NULL, PSND_PARAM_PHASE) == 0)
            return FALSE;
    }
    if (argc > 1)
        par->ahold = psnd_scan_float(argv[1]);
    if (argc > 2)
        par->bhold = psnd_scan_float(argv[2]);
    if (argc > 3)
        par->ihold = psnd_scan_integer(argv[3]);
    psnd_pk(mblk, par, dat);
    psnd_printf(mblk,"  a=%6.1f, b=%6.1f, i0=%6d\n",
        par->ahold, par->bhold, par->ihold);
    return TRUE;
}

void psnd_pz(MBLOCK *mblk, PBLOCK *par)
{
    par->ahold=0;
    par->bhold=0;
    par->ihold=1;
    par->iphase=NOPHA; 
    update_labels(mblk);
}

/*
 * recalculate P0 for a different I0
 */
void psnd_set_phase_position(MBLOCK *mblk, PBLOCK *par, DBLOCK *dat, int i0)
{
    if (dat->isize > 1) 
        par->ahold += (i0 - par->ihold) * par->bhold / (dat->isize-1);
    par->ihold  = i0;
    update_labels(mblk);
}

void psnd_refresh_phase_labels(MBLOCK *mblk)
{
    update_labels(mblk);
}

/****************************************************************
* Auto Phase
*/
#define SQR(a)	((a)*(a))
#define MAXPEAKS	200
#define WIDTH 		5
#define USEPEAKS	20
#define MAXITER		100
#define TOL		1.0e-10

static void sum_all_rows(MBLOCK *mblk)
{
    int i,j, key_store, size_col, size_row;
    
    if (DAT->ityp <= 1) 
        return;
    size_row = DAT->pars[0]->nsiz;
    size_col = DAT->pars[1]->nsiz;
    memset(DAT->work1, 0, sizeof(float) * size_row);
    if (PAR->icmplx)
        memset(DAT->work2, 0, sizeof(float) * size_row);
    key_store = DAT->pars[1]->key;
    for (i=1;i<=size_col;i++) {
        mblk->info->dimension_id = 
            psnd_rec(mblk,  FALSE, DIRECTION_ROW, i, 0);
        for (j=0;j<size_row;j++)
            DAT->work1[j] += DAT->xreal[j];
        if (PAR->icmplx)
            for (j=0;j<size_row;j++)
                DAT->work2[j] += DAT->ximag[j];
    }
    for (j=0;j<size_row;j++)
        DAT->xreal[j] = DAT->work1[j]/size_col;
    if (PAR->icmplx)
        DAT->ximag[j] = DAT->work2[j]/size_col;
    DAT->pars[1]->key = key_store;
}

static int test_peak(float *data, int i1, int i2, 
             int *left, int *right)
{
    int ii,ll,rr;
    float yy, yytop, noise;

    ll = *left;
    rr = *right;
    ii = (ll + rr)/2;
    yytop = yy = data[ll];
    noise = yytop/10;
    while (ll > i1 && data[ll-1] <= yy && data[ll-1] > noise) {
        ll--;
        yy = data[ll];
    }
    yy = data[rr];
    while (rr < i2 && data[rr+1] <= yy && data[rr+1] > noise) {
        rr++;
        yy = data[rr];
    }
    if (rr-ll < WIDTH)
         return FALSE;
    if (data[ll] > yytop/2 || data[rr] > yytop/2)
         return FALSE;
    *left  = ll;
    *right = rr;
    return TRUE;
}


static int find_peaks(float *data, int i1, int i2, float height, float biggest,
             int maxpeaks, int *ipeaks)
{
    int ip, line_up, i, np;
    float prev;

    /*
     * Array starts at 1
     */
    data--;
    ip  = i1;
    line_up = FALSE;
    for (np=0;np<maxpeaks;) {
        int ii, imax, left, right, ok = TRUE;

        prev = data[ip];
        ii = ip + 1;
        for (i=ii;i<=i2;i++) {
            float d = data[i];
            if (d < prev && prev > height && line_up) {
                ok = FALSE;
                break;
            }
            if (d > prev) 
                line_up = TRUE;
            prev = d;
            ip = i;
        }
        if (ok)
            return np/2;

        imax = i-1;
        line_up = FALSE;
        if (imax < 0) 
            continue;
        left=right=imax;
        if (!test_peak(data, i1, i2,  
             &left, &right))
             continue;
        if (ipeaks[np-1] == left)
            ipeaks[np-1] = right;
        /*
         * ipeaks starts at 0
         */
        else {
            ipeaks[np++] = left;
            ipeaks[np++] = right;
        }
    }
    return np/2;
}


typedef struct {
    float height;
    int istart, istop;
} PEAKTYPE;

static int compar(const void *a, const void *b) 
{
    PEAKTYPE *p1, *p2;
    p1 = (PEAKTYPE*) a;
    p2 = (PEAKTYPE*) b;
    if ( p1->height < p2->height )
        return 1;
    if ( p1->height > p2->height )
        return -1;
    return 0;
}
 
void sort_peaks(float *data, int npeaks,
                int *ipeaks)
{
    PEAKTYPE peaks[MAXPEAKS];
    int i,j;

    for (i=0;i<npeaks;i++) {
        int istart, istop;

        istart = ipeaks[i*2];
        istop  = ipeaks[i*2+1];
        peaks[i].height = data[istart];
        peaks[i].istart = istart;
        peaks[i].istop  = istop;
        for (j=istart;j<istop;j++) {
            if (data[j] > peaks[i].height)
                peaks[i].height = data[j];
        }
    }
    qsort((void *) peaks, npeaks, sizeof(PEAKTYPE), 
              compar);
    for (i=0;i<npeaks;i++) {
        ipeaks[2*i]   = peaks[i].istart;
        ipeaks[2*i+1] = peaks[i].istop;
    }
}

static void simplex_eval_p0(void *data, int corrsize, float x[], float *result)
{
    MBLOCK *mblk = (MBLOCK *) data;
    PBLOCK *par = PAR;
    DBLOCK *dat = DAT;
    CBLOCK *cpar = mblk->cpar_screen;
    float p0, p1, rnorm, inorm, norm,xx, yy, ww;
    int i,j, npeaks;
    
    p0 = x[0]; 
    p1 = 0.0; 
/*
    if (corrsize > 1)
        p1 += x[1];
*/
    
    for (i=0;i<dat->isize;i++) {
        dat->work1[i] = dat->xreal[i];
        dat->work2[i] = dat->ximag[i];
    }
    phase(dat->work1,
          dat->work2,
          dat->isize,
          p0,
          p1,
          par->ihold);
          

    *result = 0.0;
    npeaks = mblk->aphase_block->npeaks;
    for (j=0;j<npeaks;j++) {
        int istart,istop;

        istart = mblk->aphase_block->peaks[2*j];
        istop  = mblk->aphase_block->peaks[2*j+1];
        xx     = yy    = 0.0;
        rnorm  = inorm = 0;
        for (i = istart; i < istop; i++) {
            xx    += dat->work1[i];
            rnorm += fabs(dat->work1[i]);
            yy    += dat->work2[i];
            inorm += fabs(dat->work2[i]);
        }
        xx /= rnorm;
        yy /= inorm;
        *result +=  1.0 - ( SQR(xx) - SQR(yy) );
    }

    ww = 1.0 + 5*(float) (((int)fabs(x[0]+par->ahold))/360);
    *result *= ww;

/*   printf("p0=%g %g \n",*result,x[0]); */
}



static void simplex_eval_p1(void *data, int corrsize, float x[], float *result)
{
    MBLOCK *mblk = (MBLOCK *) data;
    PBLOCK *par = PAR;
    DBLOCK *dat = DAT;
    CBLOCK *cpar = mblk->cpar_screen;
    float p0, p1, norm,rnorm, inorm, xx, yy, ww;
    int i,j, npeaks;
    
    p0 = mblk->aphase_block->p0;
    p1 = x[0]; 

    for (i=0;i<dat->isize;i++) {
        dat->work1[i] = dat->xreal[i];
        dat->work2[i] = dat->ximag[i];
    }
    phase(dat->work1,
          dat->work2,
          dat->isize,
          p0,
          p1,
          par->ihold);

    *result = 0.0;
    npeaks = mblk->aphase_block->npeaks;
    for (j=1;j<npeaks;j++) {
        int istart,istop;

        istart = mblk->aphase_block->peaks[2*j];
        istop  = mblk->aphase_block->peaks[2*j+1];
        xx     = yy    = 0.0;
        rnorm  = inorm = 0;
        for (i = istart; i < istop; i++) {
            xx    += dat->work1[i];
            rnorm += fabs(dat->work1[i]);
            yy    += dat->work2[i];
            inorm += fabs(dat->work2[i]);
        }
        xx /= rnorm;
        yy /= inorm;
        *result +=  1.0 - ( SQR(xx) - SQR(yy) );
    }

    ww = 1.0 + 5*(float) (((int)fabs(x[0]+par->bhold))/360);

    *result *= ww;

    /* printf("res p1=%g %g %d\n",*result,x[0],npeaks);  */
}


static void eval_p1(void *data, float p0, int npeaks)
{
    MBLOCK *mblk = (MBLOCK *) data;
    PBLOCK *par = PAR;
    DBLOCK *dat = DAT;
    CBLOCK *cpar = mblk->cpar_screen;
    float p1, norm,rnorm, inorm, xx, yy, ww,result;
    float best_p1, best_val;
    int i,j,n;
    
    p1 = -361; 
best_p1 = 0;
best_val = 1000000;
    for (i=0;i<dat->isize;i++) {
        dat->work1[i] = dat->xreal[i];
        dat->work2[i] = dat->ximag[i];
    }
    phase(dat->work1,
          dat->work2,
          dat->isize,
          p0,
          p1,
          par->ihold);

    for (n=0;n<360*2;n++) {
    p1 += 1.0;
    phase(dat->work1,
          dat->work2,
          dat->isize,
          0.0,
          1.0,
          par->ihold);

    result = 0.0;
    for (j=1;j<npeaks;j++) {
        int istart,istop;

        istart = mblk->aphase_block->peaks[2*j];
        istop  = mblk->aphase_block->peaks[2*j+1];
        xx     = yy    = 0.0;
        rnorm  = inorm = 0;
        for (i = istart; i < istop; i++) {
            xx    += dat->work1[i];
            rnorm += fabs(dat->work1[i]);
            yy    += dat->work2[i];
            inorm += fabs(dat->work2[i]);
        }
        xx /= rnorm;
        yy /= inorm;
        result +=  1.0 - ( SQR(xx) - SQR(yy) );
      /*  result +=  ( SQR(xx) - SQR(yy) );*/
        
    }
    if (result < best_val) {
        best_p1 = p1;
        best_val = result;
    }
    printf("%g %g\n",p1, result); 
}

    printf("best result = %g %g\n",best_val, best_p1); 
}

static int calc_zerofill(MBLOCK *mblk, int zerofill)
{
    int i=0, ntot=0, isize;
    isize = min(DAT->isize, PAR->td);

    for (i=0; i < 6 ;i++) {
        ntot = power(isize);
        ntot *= pow(2,i);
        if (PAR->ifft != FFTCX)
            ntot /= 2;
        /*
         * multiple of 2 necessary for FFT
         */
        if (ntot >= MAXSIZ) {
            ntot = MAXSIZ;
            break;
        }
        if (i >= zerofill) 
            break;
    }
    return i;
}


int psnd_auto_phase(MBLOCK *mblk, int mode)
{
    PBLOCK *par = PAR;
    DBLOCK *dat = DAT;
    CBLOCK *cpar = mblk->cpar_screen;
    int i, j, i0, ibiggest;
    float biggest, p0, p1, w, p180;
    double sum;
    int npeaks, ipeaks[MAXPEAKS];
    int nn = 1;
    int l,iw = nn + 1;
    int ni = MAXITER;
    float f, w1[2],w2[2],w3[2],w4[2],w5[2],w6[3*3];
    int nsize = nn;
    int size = sizeof(float) * nsize;
    float tol     = TOL;
    float xs[3];
    float *xreal = NULL, *ximag = NULL;
    int isize = 0;
    int i1 = 1;
    int nzf_store,xref_store,aref_store,bref_store;

    if (dat->isize <= 1)
        return FALSE;
    ibiggest = 1;
    biggest  = 0.0;
    nzf_store = PAR->nzf;
    xref_store = PAR->xref;
    aref_store = PAR->aref;
    bref_store = PAR->bref;
    
    /*
     * Backup current spectrum
     */
    xreal = (float*) malloc(sizeof(float) * dat->isize);
    assert(xreal);
    memcpy(xreal, dat->xreal, dat->isize * sizeof(float));
    ximag = (float*) malloc(sizeof(float)*dat->isize);
    assert(ximag);
    memcpy(ximag, dat->ximag, dat->isize * sizeof(float));
    isize = dat->isize;

    /*
     * If phase 2d mode, combine the three spectra
     */
    if (mblk->info->phase_2d) {
        for (i=0;i<dat->isize;i++)  {
            dat->xreal[i] = 0.0;
            dat->ximag[i] = 0.0;
            for (j=0;j<3;j++) {
                dat->xreal[i] += cpar->xreal[j][i];
                dat->ximag[i] += cpar->ximag[j][i];
            }
        }
    }

    par->ahold += (i1 - par->ihold) * par->bhold / (dat->isize-1);
    par->ihold  = i1;
    /*
     * If size < 1k, we need more points to get any accuracy
     */
    if (dat->isize <= 4096) {
        if (psnd_if(mblk, 0, NULL)) {
            int nwindo = par->nwindo;
            int iwindo = par->iwindo;
            float ftscale = par->ftscale;
            char *argv[] = {"ft","6" };
            char label[20];
            int zf;

            par->iwindo = SNWIN;
            par->ftscale = 1.0;
            zf = calc_zerofill(mblk, 16384);
            sprintf(label, "%d", zf);
            argv[1] = label;
            if (psnd_do_window(mblk,TRUE, FALSE)) 
                if (psnd_ft(mblk, 2, argv)) 
                    ;
             par->nwindo = nwindo;
             par->iwindo = iwindo;
             par->ftscale = ftscale;
        }
    }

    for (i=0;i<dat->isize;i++) {
        dat->work2[i] = sqrt(SQR(dat->xreal[i]) + 
                                 SQR(dat->ximag[i]));
        if (dat->work2[i] > biggest) {
            ibiggest = i;
            biggest  = dat->work2[i];
        }
    }

    if (biggest == 0.0) {
        memcpy(dat->xreal, xreal, isize * sizeof(float));
        memcpy(dat->ximag, ximag, isize * sizeof(float));
        dat->isize = isize;
        free(xreal);
        free(ximag);
        PAR->nzf = nzf_store;
        PAR->xref = xref_store;
        PAR->aref = aref_store;
        PAR->bref = bref_store;
        return FALSE;
    }
        
    i0 = ibiggest - 1;
    par->ihold  = i0;
  /*  if (mode & MODE_P0) 
    {
        i0 = ibiggest - 1;
        par->ahold += (i0 - par->ihold) * par->bhold / (dat->isize-1);
        par->ihold  = i0;
    }
    */
    npeaks = find_peaks(dat->work2, 1, dat->isize, biggest/10, biggest,
                        MAXPEAKS/2, ipeaks);
    if (npeaks == 0) {
        memcpy(dat->xreal, xreal, isize * sizeof(float));
        memcpy(dat->ximag, ximag, isize * sizeof(float));
        dat->isize = isize;
        free(xreal);
        free(ximag);
        PAR->nzf = nzf_store;
        PAR->xref = xref_store;
        PAR->aref = aref_store;
        PAR->bref = bref_store;
        return FALSE;
    }
    sort_peaks(dat->work2, npeaks,ipeaks);
    if (npeaks > USEPEAKS)
        npeaks = USEPEAKS;
/* printf("npeaks = %d\n",npeaks); */
    mblk->aphase_block->peaks = (int*)malloc(sizeof(int)*npeaks*4);
    assert(mblk->aphase_block->peaks);
    mblk->aphase_block->npeaks  = npeaks;
    mblk->aphase_block->biggest = biggest;
    mblk->aphase_block->p0 = 0.0;
    
    for (i=0;i<npeaks;i++) {
        mblk->aphase_block->peaks[2*i]   = ipeaks[2*i]-1;
        mblk->aphase_block->peaks[2*i+1] = ipeaks[2*i+1]-1;
    }


    f  = 0.0;
    p0 = p1 =0.0;
    if (mode & MODE_P0) {
        mblk->aphase_block->npeaks = 1;
        mblk->aphase_block->p0 = 0.0;
        xs[0] = 1.0;
        xs[1] = 0.0;
        simplx(nn, xs, &f, tol, iw, w1, w2, w3, w4, w5, w6,
               nsize, simplex_eval_p0, (void*)mblk, ni);
        p0 = xs[0];
    }
/*
eval_p1(mblk, p0, npeaks);
*/
    if (mode & MODE_P1) {
        xs[0] = 1.0;
        mblk->aphase_block->npeaks = npeaks;
        mblk->aphase_block->p0 = p0;
        simplx(nn, xs, &f, tol, iw, w1, w2, w3, w4, w5, w6,
               nsize, simplex_eval_p1, (void*)mblk, ni);
        p1 = xs[0];
    }
    
    phase(dat->xreal,
          dat->ximag,
          dat->isize,
          p0,
          p1,
          par->ihold);
/*
    sum=0.0;
    for (i=0;i<dat->isize;i++) 
        sum += dat->ximag[i]/dat->isize;
    printf("sum i = %g,",sum); 
    
*/
    sum=0.0;
    for (i=0;i<dat->isize;i++) 
        sum += dat->xreal[i]/dat->isize;
/* printf("sum r = %g\n",sum); */

    /*
     * If spectrum is negative,
     * add 180 to p0
     */
    if ((mode & MODE_P0) && sum < 0) {
       
        if (p0 > 0)
            p180 = -180.0;
        else
            p180 = 180.0;
           
           phase(dat->xreal,
                  dat->ximag,
                  dat->isize,
                  p180,
                  0.0,
                  par->ihold);
        p0 += p180;
    }

    /*
     * Normalize
     */
    if (dat->isize > 1) 
        p0 += (i1 - par->ihold) * p1 / (dat->isize-1);
    par->ihold  = i1;
    memcpy(dat->xreal, xreal, isize * sizeof(float));
    memcpy(dat->ximag, ximag, isize * sizeof(float));
    dat->isize = isize;
    free(xreal);
    free(ximag);

    if (mblk->info->phase_2d) {
        for (i=0;i<3;i++)
            phase(cpar->xreal[i],
                  cpar->ximag[i],
                  dat->isize,
                  p0,
                  p1,
                  par->ihold);           
    }
    else
        phase(dat->xreal,
              dat->ximag,
              dat->isize,
              p0,
              p1,
              par->ihold);
    
    
    par->iphase = DOPHA;           
    par->ahold += p0;
    par->bhold += p1;

    update_labels(mblk);
    free(mblk->aphase_block->peaks);
    PAR->nzf = nzf_store;
    PAR->xref = xref_store;
    PAR->aref = aref_store;
    PAR->bref = bref_store;
    return TRUE;
}



