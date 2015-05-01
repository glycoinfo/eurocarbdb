/********************************************************************/
/*                          psnd_watwa.c                            */
/*                                                                  */
/* Popup interface to water-wash (watwa) routines                   */
/* 1999, Albert van Kuik                                            */
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




/* 
 * Do ft in buffer to determine the watwa shift position
 */
static int do_watwaft(PBLOCK *par, DBLOCK * dat)
{
    int ntot, oldsize;
    int rotate = 0, mode;
    float dspphase = 0.0;

    oldsize = ntot = dat->isize;

    if (par->dspflag && par->dspshift != 0.0) {
        rotate   = (int) floor(par->dspshift);
        /*
        dspphase = -(par->dspshift - floor(par->dspshift)) * 360;
        */
    }
    
    memcpy(dat->xbufr1, dat->xreal, dat->isize*sizeof(float));
    memcpy(dat->xbufr2, dat->ximag, dat->isize*sizeof(float));
    /*
     * We can not do watwa on a spectrum
     */
    if (dat->isspec)
        return TRUE;
    /*
     * complex or real, fft or dft
     */
    if (par->icmplx) {
        if (par->ifft == DFTCX)
            mode = DFTCX;
        else
            mode = FFTCX;
    }
    else
        mode = par->ifft;
    if (dat->isize != par->td) {
        if (mode == FFTREA)
            mode = DFTREA;
        else if (mode == FFTCX)
            mode = DFTCX;
    }
    switch (mode) {
    case FFTREA:
        ntot = 2*oldsize;
        ntot = fftrxf(dat->xbufr1,
                      dat->xbufr2,
                      oldsize,
                      ntot,
                      rotate);
        if (dspphase != 0.0)
            phase(dat->xbufr1, 
                  dat->xbufr2,
                  ntot, 
                  0.0,
                  dspphase,
                  1);
        break;
    case FFTCX:
        ntot = oldsize;
        ntot = fftcxf(dat->xbufr1,
                      dat->xbufr2,
                      oldsize,
                      ntot,
                      rotate);
        if (dspphase != 0.0)
            phase(dat->xbufr1, 
                  dat->xbufr2,
                  ntot, 
                  0.0,
                  dspphase,
                  1);
        break;
    case DFTREA  :
        ntot = 2*oldsize;
        ntot = dftrxf(dat->xbufr1,
                      dat->xbufr2,
                      oldsize,
                      ntot,
                      dat->work1,
                      dat->work2);
        if (dspphase != 0.0)
            phase(dat->xbufr1, 
                  dat->xbufr2,
                  ntot, 
                  0.0,
                  dspphase,
                  1);
         break;
    case DFTCX   :
        ntot = oldsize;
        ntot = dftcxf(dat->xbufr1,
                      dat->xbufr2,
                      oldsize,
                      oldsize,
                      dat->work1,
                      dat->work2);
        break;
    default:
        return FALSE;
    }
    return TRUE;
}

#define WATWA_MOUSE	10


static void update_labels(MBLOCK *mblk)
{
    POPUP_INFO *popinf = mblk->popinf + POP_WATWA;

    if (popinf->cont_id) {
        char *label;
        label = psnd_sprintf_temp("%.2f", PAR->wshift);
        g_popup_set_label(popinf->cont_id, ID_WSHIFT, label);
    }
}

static void watwa_callback(G_POPUP_CHILDINFO *ci)
{
    G_EVENT ui;
    int ok = TRUE;
    MBLOCK *mblk = (MBLOCK*) ci->userdata;
    POPUP_INFO *popinf = mblk->popinf + POP_WATWA;

    switch (ci->type) {
        case G_CHILD_OK:
            psnd_push_waitcursor(mblk);
            PAR->watwa = TRUE;
            ok = psnd_do_watwa(PAR,DAT);
            psnd_pop_waitcursor(mblk);
            if (ok) {
                static G_EVENT ui;

                ui.event   = G_COMMAND;
                ui.win_id  = mblk->info->win_id;
                ui.keycode = PSND_PL;
                g_send_event(&ui);
            }
            /* Fall through */
        case G_CHILD_CANCEL:
            if (psnd_make_visible(mblk, 
                              mblk->info->block_id, S_BUFR1, FALSE))
                psnd_1d_reset_connection(mblk);
            g_menu_enable_item(mblk->info->mousebar_id, ID_MOUSE_SELECT, FALSE);
            if (mblk->info->mouse_mode1 == MOUSE_WATWA)
                psnd_set_cursormode(mblk, 0, 0);   
            popinf->visible = FALSE;
            return;
    }
    switch (ci->id) {
        case WATWA_MOUSE:
            ok = do_watwaft(PAR, DAT);
            if (ok) {
                static G_EVENT ui;
                if (psnd_make_visible(mblk, 
                              mblk->info->block_id, S_BUFR1, TRUE))
                    psnd_1d_reset_connection(mblk);
                ui.event   = G_COMMAND;
                ui.win_id  = mblk->info->win_id;
                ui.keycode = PSND_PL;
                g_send_event(&ui);
            }
            psnd_set_cursormode(mblk, MOUSE_WATWA, MOUSE_WATWA);
            break;
        case ID_IOPT:
            PAR->iopt = ci->item+1;
            break;
        case ID_KC:
            PAR->kc = psnd_scan_float(ci->label);
            break;
        case ID_WSHIFT:
            PAR->wshift = psnd_scan_float(ci->label);
            break;
    }
}

/*
 * Popup menu to select watwa position with mouse button 2
 */
#define MAXID	30
static void showwatwa_popup(MBLOCK *mblk)
{
    G_POPUP_CHILDINFO ci[MAXID];
    int id;
    POPUP_INFO *popinf = mblk->popinf + POP_WATWA;

    if (popinf->visible)
        return;
    popinf->visible = TRUE;
    if (!popinf->cont_id) {
        int cont_id;

        id = -1;
        cont_id = g_popup_container_open(mblk->info->win_id, 
                      "Water Wash Correction", G_POPUP_KEEP);
        popinf->cont_id = cont_id;
        id++;
        id = param_add_watwa(mblk, cont_id, ci, id, watwa_callback, PAR);

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type     = G_CHILD_PUSHBUTTON;
        ci[id].id       = WATWA_MOUSE;
        ci[id].label    = "Grab Mouse Button 1";
        ci[id].func     = watwa_callback;
        ci[id].userdata = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_OK;
        ci[id].func  = watwa_callback;
        ci[id].userdata = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_CANCEL;
        ci[id].func  = watwa_callback;
        ci[id].userdata = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));
        assert(id<MAXID);
    }
    g_menu_enable_item(mblk->info->mousebar_id, ID_MOUSE_SELECT, TRUE);
    psnd_set_cursormode(mblk, MOUSE_WATWA, 0);
    g_popup_container_show(popinf->cont_id) ;
}



int psnd_do_watwa(PBLOCK *par, DBLOCK *dat)
{
    int ok, idspshift=0,icmplx=0;
    
    if (par->dspflag && par->dspshift != 0.0) 
        idspshift=(int)floor(par->dspshift);
    if (par->icmplx || par->ifft == FFTCX)
        icmplx = TRUE;
    ok = watwa(par->iopt, icmplx,
               dat->xreal, dat->ximag, 
               dat->isize, par->kc, par->wshift, idspshift);
    return ok;
}

/*
 * Set parameters with args, or show popup menu
 */
int psnd_waterwash(int argc, char *argv[], MBLOCK *mblk)
{
    int ok = FALSE;
    
    if (argc >= 1) {
        int i;
        float f;
        if (argc >= 2) {
            i = psnd_scan_integer(argv[1]);
            PAR->iopt = max(1,i);
        }
        if (argc >= 3) {
            f = psnd_scan_float(argv[2]);
            PAR->kc = max(1,f);
        }
        if (argc >= 4) {
            f = psnd_scan_float(argv[3]);
            PAR->wshift = f;
        }
        PAR->watwa = TRUE;
        ok = psnd_do_watwa(PAR, DAT);
    }
    else
        showwatwa_popup(mblk);
    return ok;
}

/*
 * Update new values in menu
 */
void psnd_refresh_watwa_labels(MBLOCK *mblk)
{
    update_labels(mblk);
}


#define POPUP_LABEL_ID	0
void  psnd_refresh_all_watwa_labels(MBLOCK *mblk)
{
    POPUP_INFO *popinf = mblk->popinf + POP_WATWA;

    if (popinf->cont_id) {
        char *label;

        label = psnd_sprintf_temp("Direction %d. %s", 
                                     PAR->icrdir, "WATWA");
        g_popup_set_label(popinf->cont_id, POPUP_LABEL_ID, label);

        g_popup_set_selection(popinf->cont_id, ID_WATERFIT, PAR->watwa);
        g_popup_set_selection(popinf->cont_id, ID_IOPT, PAR->iopt-1);

        label = psnd_sprintf_temp("%.1f", PAR->kc);
        g_popup_set_label(popinf->cont_id, ID_KC, label);
        label = psnd_sprintf_temp("%.2f", PAR->wshift);
        g_popup_set_label(popinf->cont_id, ID_WSHIFT, label);
    }
}

