/********************************************************************/
/*                          psnd_window.c                           */
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

/************************/
/*
 * Interface with the low-lovel window routines in the
 * nmrtool.a library
 */
static void load_window(int id, int start, int stop, int size, 
                        float *window, float rlb, float gn, 
                        float rsh, int itm, float sw)
{
    int i;

    start = max(1,start);
    stop  = min(size,stop);
    if (stop==0)
        stop=size;
    for (i=start-1;i<stop;i++)
        window[i] = 0.0;
    switch (id) {
    case PSND_WN:
        for (i=start-1;i<stop;i++)
            window[i] = 1.0;
        return;
    case PSND_EM:
        wdwemx(window,
               size,
               start,
               stop,
               sw,
               rlb);
        break;
    case PSND_CD:
        wdwcdx(window,
               size,
               start,
               stop,
               sw,
               rlb);
        break;
    case PSND_SN:
        wdwsnx(window,
               size,
               start,
               stop,
               rsh);
        break;
    case PSND_SQ:
        wdwsqx(window,
               size,
               start,
               stop,
               rsh);
        break;
    case PSND_HN:
        wdwhng(window,
               size,
               start,
               stop);
        break;
    case PSND_HM:
        wdwhmg(window,
               size,
               start,
               stop);
        break;
    case PSND_KS:
        wdwksr(window,
               size,
               start,
               stop,
               rsh);
        break;
    case PSND_GM:
        wdwgmx(window,
               size,
               start,
               stop,
               sw,
               rlb,
               gn);
        break;
    case PSND_TM:
        wdwtmx(window,
               size,
               start,
               stop,
               itm);
        break;
    case PSND_WB:
        /*
        for (i=0;i<size;i++)
            window[i] = bufr[i];
            */
        break;
    }
}

/*
 * psnd-id to win-id lookup table
 */
static int winid[][2] = {
    { PSND_WN, NOWIN },
    { PSND_EM, EMWIN },
    { PSND_CD, CDWIN },
    { PSND_SN, SNWIN },
    { PSND_SQ, SQWIN },
    { PSND_HN, HNWIN },
    { PSND_HM, HMWIN },
    { PSND_KS, KSWIN },
    { PSND_GM, GMWIN },
    { PSND_TM, TMWIN },
    { PSND_WB, WBWIN },
    { -1, -1}
};


/*
 * psnd_win_id -> win-id
 */
void psnd_set_window_id(int id, PBLOCK *par)
{
    int i;
    for (i=0;winid[i][0] >=0;i++) {
        if (winid[i][0] == id) {
            par->iwindo = winid[i][1];
            break;
        }
    }
}

/*
 * win-id -> psnd_win_id
 */
int psnd_lookup_window_mode(int iwindo)
{
    int i, id=PSND_WN;

    for (i=0;winid[i][0] >=0;i++) {
        if (winid[i][1] == iwindo) {
            id = winid[i][0];
            break;
        }
    }
    return id;
}



static void do_window(int start, int stop, float *real, float *imag, 
                     float *window)
{
    int i;
    /*
     * Test for zero window. 
     * At least 1 point in window must be non-zero
     */
    for (i=start-1;i<stop;i++) {
        if (window[i] != 0.0) {
            xxmulx(real, window, start, stop);
            xxmulx(imag, window, start, stop);
            break;
        }
    }
}

#define POPUP_LABEL_ID	0
void psnd_refresh_all_window_labels(MBLOCK *mblk)
{
    POPUP_INFO *popinf = mblk->popinf + POP_WINDOW;

    if (popinf->cont_id) {
        char *label;

        label = psnd_sprintf_temp("Direction %d. %s", 
                                     PAR->icrdir, "WINDOW");
        g_popup_set_label(popinf->cont_id, POPUP_LABEL_ID, label);

        if (PAR->iwindo == 0)
            PAR->iwindo = SNWIN;
        g_popup_set_selection(popinf->cont_id, ID_NWINDO, PAR->nwindo);
        g_popup_set_selection(popinf->cont_id, ID_IWINDO, PAR->iwindo-1);

        label = psnd_sprintf_temp("%1.2f", PAR->rsh);
        g_popup_set_label(popinf->cont_id, ID_RSH, label);
        label = psnd_sprintf_temp("%1.2f", PAR->rlb);
        g_popup_set_label(popinf->cont_id, ID_RLB, label);
        label = psnd_sprintf_temp("%1.2f", PAR->gn);
        g_popup_set_label(popinf->cont_id, ID_GN, label);
        label = psnd_sprintf_temp("%d", PAR->itm);
        g_popup_set_label(popinf->cont_id, ID_ITM, label);

        label = psnd_sprintf_temp("%d", PAR->iwstrt);
        g_popup_set_label(popinf->cont_id, ID_IWSTRT, label);
        label = psnd_sprintf_temp("%d", PAR->iwstop);
        g_popup_set_label(popinf->cont_id, ID_IWSTOP, label);
    }
}

/* 
 * Get parameters from arg values, or ask the user
 */
int psnd_window_prep(MBLOCK *mblk, int argc, char *argv[], int id)
{
    int i, itmp;
    float tmp;
    CBLOCK *cpar = mblk->cpar_screen;
    PBLOCK *par  = PAR;
    DBLOCK *dat  = DAT;
    
    switch (id) {
    case PSND_EM:
    case PSND_CD:
        if (argc >= 2) 
            par->rlb = psnd_scan_float(argv[1]);
        else if (argc == 0)
            if (psnd_rvalin(mblk," Line broadening ?",1,&(par->rlb)) == 0)
                return FALSE;
        break;
    case PSND_SN:
    case PSND_SQ:
    case PSND_KS:
        if (argc >= 2) 
            par->rsh = psnd_scan_float(argv[1]);
        else if (argc == 0)
            if (psnd_rvalin(mblk," Shift ?",1,&(par->rsh)) == 0)
                return FALSE;
        break;
    case PSND_WN:
    case PSND_HM:
    case PSND_HN:
    case PSND_WB:
        break;
    case PSND_GM:
        tmp = par->gn;
        if (argc >= 3) {
            par->rlb = psnd_scan_float(argv[1]);
            tmp = psnd_scan_float(argv[2]);
        }
        else if (argc >= 2) {
            par->rlb = psnd_scan_float(argv[1]);
        }
        else if (argc == 0){
            if (psnd_rvalin(mblk," Line broadening ?",1,&(par->rlb)) == 0)
                return FALSE;
            if (psnd_rvalin(mblk," Gaussian broadening  (0-1)?",1,&tmp) == 0)
                return FALSE;
        }
        if (tmp <= 0 || tmp > 1) {
            g_bell();
            return FALSE;
        }
        par->gn = tmp;
        break;
    case PSND_TM:
        itmp = par->itm;
        if (argc >= 2) 
            itmp = psnd_scan_integer(argv[1]);
        else if (argc == 0)
            if (psnd_ivalin(mblk," #Channels TM window ?",1,&itmp) == 0)
                return FALSE;
        if (itmp < 1) {
            g_bell();
            return FALSE;
        }
        par->itm = itmp;
        break;
    default:
        return FALSE;
    }
    return TRUE;
}

static void show_win_callback(G_POPUP_CHILDINFO *ci)
{
    int i;
    MBLOCK *mblk = (MBLOCK*) ci->userdata;
    POPUP_INFO *popinf = mblk->popinf + POP_WINDOW;

    switch (ci->type) {
        case G_CHILD_OK:
            psnd_do_window(mblk,TRUE,FALSE);
            /* Fall THROUGH */
        case G_CHILD_CANCEL:
            if (psnd_make_visible(mblk, mblk->info->block_id, S_WINDOW, FALSE))
                psnd_1d_reset_connection(mblk);
            psnd_plot(mblk,TRUE,mblk->info->dimension_id);
            popinf->visible = FALSE;
            return;
    }

    switch (ci->id) {
    case ID_NWINDO:
        PAR->nwindo = ci->item;
        break;
    case ID_IWINDO:
        PAR->iwindo = ci->item+1;
        break;
    case ID_RSH:
        PAR->rsh = psnd_scan_float(ci->label);
        break;
    case ID_RLB:
        PAR->rlb = psnd_scan_float(ci->label);
        break;
    case ID_GN:
        PAR->gn = psnd_scan_float(ci->label);
        break;
    case ID_ITM:
        PAR->itm = psnd_scan_integer(ci->label);
        break;
    case ID_IWSTRT:
        i = psnd_scan_integer(ci->label);
        if (i>=1 && i<=DAT->isize)
            PAR->iwstrt=i;
        break;
    case ID_IWSTOP:
        i = psnd_scan_integer(ci->label);
        if (i>=1 && i<=DAT->isize)
            PAR->iwstop=i;
        break;
    default:
        if (!psnd_do_window(mblk, FALSE, FALSE))
            break;
        if (psnd_make_visible(mblk, mblk->info->block_id, S_WINDOW, TRUE))
            psnd_1d_reset_connection(mblk);
        psnd_plot(mblk,TRUE,mblk->info->dimension_id);
        g_plotall();
        g_peek_event();
    }
}
/*
 * Popup menu for window
 */
#define MAXID	20
void psnd_showwindow_popup(MBLOCK *mblk)
{
    static G_POPUP_CHILDINFO ci[MAXID];
    int id, cont_id;
    POPUP_INFO *popinf = mblk->popinf + POP_WINDOW;

    if (popinf->visible)
        return;
    popinf->visible = TRUE;
    id = -1;
    popinf->cont_id = 
        g_popup_container_open(mblk->info->win_id, "Window functions", 0); 
    cont_id = popinf->cont_id;
    id++;
    id = param_add_window1(mblk, cont_id, ci, id, 
                                 show_win_callback, PAR);
    id++;
    id = param_add_window2(mblk, cont_id, ci, id, 
                                 show_win_callback, PAR);
    id++;
    id = param_add_window3(mblk, cont_id, ci, id, 
                                 show_win_callback, PAR);

    id++;
    g_popup_init_info(&(ci[id]));
    ci[id].type      = G_CHILD_OK;
    ci[id].func      = show_win_callback;
    ci[id].userdata  = (void*) mblk;
    g_popup_add_child(cont_id, &(ci[id]));

    id++;
    g_popup_init_info(&(ci[id]));
    ci[id].type      = G_CHILD_CANCEL;
    ci[id].func      = show_win_callback;
    ci[id].userdata  = (void*) mblk;
    g_popup_add_child(cont_id, &(ci[id]));

    assert(id<MAXID);

    g_popup_container_show(popinf->cont_id) ;
}


/*
 * Perform a window operation
 * if 'doit' is TRUE, apply to real and imag data
 * otherwise only load the window curve
 *
 * if 'ask' is TRUE, popup window box
 */
int psnd_do_window(MBLOCK *mblk, int doit, int ask)
{
    int id,iwstrt,iwstop,idspshift=0.0;
    PBLOCK *par = PAR;
    DBLOCK *dat = DAT;

    if (ask) {
        if (doit) {
            if (psnd_set_param(mblk, 0, NULL, PSND_DO_WINDOW) == 0)
                return FALSE;
        }
        else {
            if (psnd_set_param(mblk, 0, NULL, PSND_PARAM_WINDOW) == 0)
                return FALSE;
        }
    }
    id = psnd_lookup_window_mode(par->iwindo);
    if (id == PSND_WN)
        return FALSE;
    iwstrt = max(1,par->iwstrt);
    iwstop = min(dat->isize,par->iwstop);
    if (iwstop == 0)
        iwstop = dat->isize;
    if (iwstrt > iwstop)
        return FALSE;

    if (par->dspflag && par->dspshift != 0.0) 
        idspshift = (int) floor(par->dspshift);
    load_window(id, iwstrt, iwstop, dat->isize - idspshift, 
                dat->window + idspshift, par->rlb, par->gn, 
                par->rsh, par->itm, par->swhold);
    /*
     * The window is not calculated for the 'idspshift' part
     * of the data array. This region is set to the first 
     * 'non-dspshift-point' of the window
     */
    if (idspshift) {
        int i;
        for (i=0;i<idspshift;i++)
            dat->window[i] = dat->window[idspshift];
    }
    if (doit) {
        do_window(iwstrt, iwstop, dat->xreal, 
                        dat->ximag, dat->window);
        par->nwindo = TRUE;
    }
    return TRUE;
}


