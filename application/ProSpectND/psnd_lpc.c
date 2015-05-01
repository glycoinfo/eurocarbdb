/********************************************************************/
/*                             psnd_lpc.c                           */
/*                                                                  */
/* Linear prediction                                                */
/* 1998, Albert van Kuik                                            */
/********************************************************************/
#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <assert.h>
#include "genplot.h"
#include "psnd.h"
#include "nmrtool.h"



int psnd_linpr(MBLOCK *mblk, float *xreal, float *ximag, int icmplx,
                int size, int nfut, int npoles,
                float toler, int lpctype, int mode, int moveroots, int gapstart,
                int gapstop, int start, int stop, int replace, int cursize)
{
    int newsize,oldsize = size;
    start = max(1,start);
    if (stop <= 0 || stop > size)
        stop = size;
    if (start >= stop)
        return FALSE;
    newsize = stop + nfut;
    if (mode == PREDICT_FORWARD && size + nfut > cursize) {
        psnd_printf(mblk,"Data size too large for array (max %d)\n", cursize);
        return FALSE;
    }
    if (abs(npoles) > MAXROOT) {
        psnd_printf(mblk,"%d NPOLES too large for array (max = %d)\n", npoles, MAXROOT);
        npoles = MAXROOT;
        psnd_printf(mblk,"Number of roots scaled back to: %d\n", npoles);
    }
    if (abs(npoles) >= size) {
        psnd_printf(mblk,"%d NPOLES too large for %d NDATA\n", npoles, size);
        npoles = size -1;
        psnd_printf(mblk,"Number of roots scaled back to: %d\n", npoles);
    }
    if (nfut > MAXSI3) {
        psnd_printf(mblk,"NFUT too large for array\n");
        return FALSE;
    }
    xreal += (start-1);
    ximag += (start-1);
    size   = (stop-start+1);
    if (mode == PREDICT_FORWARD){
        if (lpctype == PREDICT_HSVD) {
            int   icomplx = TRUE;
            fidhsvd(PREDICT_FORWARD,
                   moveroots,
                   xreal,
                   ximag,
                   icmplx,
                   size,
                   toler,
                   nfut,
                   0,
                   npoles,
                   icomplx,
                   replace);
        }
        else if (lpctype == PREDICT_LPC) {
            ftlipc(PREDICT_FORWARD,
                   moveroots,
                   xreal,
                   ximag,
                   icmplx,
                   size,
                   xreal + size,
                   ximag + size,
                   nfut,
                   npoles,
                   toler);
        }
        else {
            ftlipr(moveroots,
                   xreal,
                   size,
                   xreal + size,
                   nfut,
                   npoles);
            if (lpctype == PREDICT_DOUBLE_REAL)
                ftlipr(moveroots,
                   ximag,
                   size,
                   ximag + size,
                   nfut,
                   npoles);
        }
    }
    else if (mode == PREDICT_BACKWARD) {
        if (lpctype == PREDICT_HSVD) {
            int   icomplx = TRUE;
            fidhsvd(PREDICT_BACKWARD,
                   moveroots,
                   xreal + nfut,
                   ximag + nfut,
                   icmplx,
                   size - nfut,
                   toler,
                   nfut,
                   0,
                   npoles,
                   icomplx,
                   replace);
        }
        else if (lpctype == PREDICT_LPC)
            ftlipc(PREDICT_BACKWARD,
                   moveroots,
                   xreal + nfut,
                   ximag + nfut,
                   icmplx,
                   size - nfut,
                   xreal,
                   ximag,
                   nfut,
                   npoles,
                   toler);
        else {
            ftlipr(moveroots,
                   xreal + nfut,
                   size - nfut,
                   xreal,
                   nfut,
                   -npoles);

            if (lpctype == PREDICT_DOUBLE_REAL)
                ftlipr(moveroots,
                   ximag + nfut,
                   size - nfut,
                   ximag,
                   nfut,
                   -npoles);
        }
        return oldsize;
    }
    else if (mode == PREDICT_GAP){
        int nfut1 = gapstart, nfut2 = gapstop;
        int nfut  = nfut2-nfut1+1;

        nfut1 -= (start-1);
        nfut2 -= (start-1);

        nfut1 = max(1,nfut1);
        nfut2 = min(nfut2,size);
        if (nfut1 > nfut2)
            return 0;
        if (lpctype == PREDICT_LPC) {
        
            ftlipc_gap(moveroots,
                   xreal,
                   ximag,
                   icmplx,
                   size,
                   nfut1,
                   nfut2,
                   npoles,
                   toler);
        }     
        else if (lpctype == PREDICT_HSVD) {
            int   icomplx = TRUE;
            fidhsvd(PREDICT_GAP,
                   moveroots,
                   xreal,
                   ximag,
                   icmplx,
                   size,
                   toler,
                   nfut1,
                   nfut2,
                   npoles,
                   icomplx,
                   replace);
        }
        else {
            int ipoles, merge = 0;
            float *tmpr = fvector(0, nfut);
            float *tmpi = fvector(0, nfut);

            ipoles = min(nfut1-1-1,npoles);
            if (nfut1 > 0) {
                ftlipr(moveroots,
                   xreal,
                   nfut1,
                   tmpr,
                   nfut,
                   ipoles);
                if (lpctype == PREDICT_DOUBLE_REAL) 
                    ftlipr(moveroots,
                   ximag,
                   nfut1,
                   tmpi,
                   nfut,
                   ipoles);
                merge = 1;
            }

            ipoles = min(size-nfut2-1,npoles);
            if (size-nfut2-1 > 0) {
                ftlipr(moveroots,
                   xreal + nfut2,
                   size  - nfut2,
                   xreal + nfut1 - 1,
                   nfut,
                   -ipoles);
                if (lpctype == PREDICT_DOUBLE_REAL) 
                    ftlipr(moveroots,
                   ximag + nfut2,
                   size  - nfut2,
                   ximag + nfut1 - 1,
                   nfut,
                   -ipoles);
                 merge += 2;
            }

            
            if (merge == 1) 
                memcpy(xreal + nfut1 - 1, tmpr, nfut * sizeof(float));
            else if (merge == 3) {
                xxaddx(xreal,tmpr-nfut1+1,nfut1,nfut2);
                xxmulv(xreal,nfut1,nfut2,0.5);
            }

            if (lpctype == PREDICT_DOUBLE_REAL) {
                if (merge == 1) 
                    memcpy(ximag + nfut1 - 1, tmpi, nfut * sizeof(float));
                else if (merge == 3)  {
                    xxaddx(ximag,tmpi-nfut1+1,nfut1,nfut2);
                    xxmulv(ximag,nfut1,nfut2,0.5);
                }
            }
            free_fvector(tmpr, 0);
            free_fvector(tmpi, 0);
          }
          return oldsize;
    }
    return newsize;
}

int psnd_linearprediction(MBLOCK *mblk)
{
    int ok;
    PBLOCK *par = PAR;
    DBLOCK *dat = DAT;

    psnd_push_waitcursor(mblk);
    par->nlpc = TRUE;
    if (par->lpcmode == PREDICT_FORWARD)
        psnd_resize_arrays(mblk, 0, par->nfut, NULL, dat);
    ok = psnd_linpr(mblk,
                    dat->xreal,
                    dat->ximag,
                    par->icmplx,
                    dat->isize,
                    par->nfut,
                    par->npoles,
                    par->toler,
                    par->lpc,
                    par->lpcmode,
                    par->mroot,
                    par->ngap1,
                    par->ngap2,
                    par->nstart,
                    par->nstop,
                    par->replace,
                    mblk->info->block_size);
    if (ok>0)
        psnd_set_datasize(mblk, ok, TRUE, dat);
    psnd_pop_waitcursor(mblk);
    return TRUE;
}

/*
 * Callback func for interactive lpc popup
 */
static void showlpc_callback(G_POPUP_CHILDINFO *ci)
{
    MBLOCK *mblk = (MBLOCK*) ci->userdata;
    POPUP_INFO *popinf = mblk->popinf + POP_LPC;

    switch (ci->type) {
        case G_CHILD_OK:
            if (psnd_linearprediction(mblk))
                psnd_plot(mblk,TRUE,mblk->info->dimension_id);
            /* Fall THROUGH */
        case G_CHILD_CANCEL:
            popinf->visible = FALSE;
            return;
    }

    switch (ci->id) {
        case ID_NLPC:
            PAR->nlpc = ci->item;
            break;
        case ID_LPC:
            PAR->lpc = ci->item;
            break;
        case ID_LPCMODE:
            PAR->lpcmode = ci->item;
            break;
        case ID_NFUT:
            PAR->nfut = psnd_scan_integer(ci->label);
            break;
        case ID_NPOLES:
            PAR->npoles = psnd_scan_integer(ci->label);
            break;
        case ID_MROOT:
            PAR->mroot = ci->item;
            break;
        case ID_REPLACE:
            PAR->replace = ci->item;
            break;
        case ID_TOLER:
            PAR->toler = psnd_scan_float(ci->label);
            break;
        case ID_NGAP1:
            PAR->ngap1 = psnd_scan_integer(ci->label);
            break;
        case ID_NGAP2:
            PAR->ngap2 = psnd_scan_integer(ci->label);
            break;
        case ID_NSTART:
            PAR->nstart = psnd_scan_integer(ci->label);
            break;
        case ID_NSTOP:
            PAR->nstop = psnd_scan_integer(ci->label);
            break;

    }
}


/*
 * Popup menu for linear prediction
 */
#define MAXID	20
void psnd_showlpc_popup(MBLOCK *mblk)
{
    static G_POPUP_CHILDINFO ci[MAXID];
    int id, cont_id;
    POPUP_INFO *popinf = mblk->popinf + POP_LPC;

    if (popinf->visible)
        return;
    popinf->visible = TRUE;
    id = -1;
    popinf->cont_id = 
        g_popup_container_open(mblk->info->win_id, "Linear prediction", 0); 
    cont_id = popinf->cont_id;
    id++;
    id = param_add_linpar(mblk, cont_id, ci, id, 
                                 showlpc_callback, PAR);

    id++;
    g_popup_init_info(&(ci[id]));
    ci[id].type      = G_CHILD_OK;
    ci[id].func      = showlpc_callback;
    ci[id].userdata  = (void*) mblk;
    g_popup_add_child(cont_id, &(ci[id]));

    id++;
    g_popup_init_info(&(ci[id]));
    ci[id].type      = G_CHILD_CANCEL;
    ci[id].func      = showlpc_callback;
    ci[id].userdata  = (void*) mblk;
    g_popup_add_child(cont_id, &(ci[id]));

    assert(id<MAXID);

    g_popup_container_show(popinf->cont_id) ;
}

#define POPUP_LABEL_ID	0
void  psnd_refresh_all_lpc_labels(MBLOCK *mblk)
{
    POPUP_INFO *popinf = mblk->popinf + POP_LPC;

    if (popinf->cont_id) {
        char *label;

        label = psnd_sprintf_temp("Direction %d. %s", 
                                     PAR->icrdir, "LINEAR PREDICTION");
        g_popup_set_label(popinf->cont_id, POPUP_LABEL_ID, label);

        g_popup_set_selection(popinf->cont_id, ID_NLPC, PAR->nlpc);
        g_popup_set_selection(popinf->cont_id, ID_LPC, PAR->lpc);
        g_popup_set_selection(popinf->cont_id, ID_LPCMODE, PAR->lpcmode);

        label = psnd_sprintf_temp("%d", PAR->npoles);
        g_popup_set_label(popinf->cont_id, ID_NPOLES, label);
        label = psnd_sprintf_temp("%d", PAR->nfut);
        g_popup_set_label(popinf->cont_id, ID_NFUT, label);

        g_popup_set_selection(popinf->cont_id, ID_MROOT, PAR->mroot);
        g_popup_set_selection(popinf->cont_id, ID_REPLACE, PAR->replace);

        label = psnd_sprintf_temp("%g", PAR->toler);
        g_popup_set_label(popinf->cont_id, ID_TOLER, label);
        label = psnd_sprintf_temp("%d", PAR->ngap1);
        g_popup_set_label(popinf->cont_id, ID_NGAP1, label);
        label = psnd_sprintf_temp("%d", PAR->ngap2);
        g_popup_set_label(popinf->cont_id, ID_NGAP2, label);
        label = psnd_sprintf_temp("%d", PAR->nstart);
        g_popup_set_label(popinf->cont_id, ID_NSTART, label);
        label = psnd_sprintf_temp("%d", PAR->nstop);
        g_popup_set_label(popinf->cont_id, ID_NSTOP, label);
    }
}



