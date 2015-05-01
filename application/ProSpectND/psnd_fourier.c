/********************************************************************/
/*                          psnd_fourier.c                          */
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



int psnd_resize_arrays(MBLOCK *mblk, int nzf, int addtosize, int *calc_size,
                        DBLOCK *dat)
{
    int ntot;
    
    ntot    = dat->isize + addtosize;
    ntot    = power(ntot);
    ntot   *= pow(2, nzf);
    /*
     * If calc_select is non-zero, store full required size
     */
    if (calc_size) 
        *calc_size = ntot;
    ntot    = min(ntot, MAXSIZ);
    if (ntot > mblk->info->block_size)
        psnd_realloc_buffers(mblk, ntot);
    memset(dat->xreal + dat->isize, 0, sizeof(float) * (ntot - dat->isize));
    memset(dat->ximag + dat->isize, 0, sizeof(float) * (ntot - dat->isize));
    return ntot;
}

static void temp_resize_arrays(MBLOCK *mblk, DBLOCK *dat, int ntot)
{
    int i;
    
    dat->xreal = (float*) realloc(dat->xreal,  sizeof(float) * ntot);
    assert(dat->xreal);
    dat->ximag = (float*) realloc(dat->ximag,  sizeof(float) * ntot);
    assert(dat->ximag);
    dat->work1 = (float*) realloc(dat->work1,  sizeof(float) * ntot);
    assert(dat->xreal);
    dat->work2 = (float*) realloc(dat->work2,  sizeof(float) * ntot);
    assert(dat->ximag);
    for (i=1;i<mblk->info->max_block;i++) {
        mblk->dat[i]->work1 = mblk->dat[0]->work1;
        mblk->dat[i]->work2 = mblk->dat[0]->work2;
    }
}

/***********************************************
 * FT stuff
 */
int power(int stop)
{
    int i,j;
    for (i=0,j=0;j<stop;i++)
        j = pow(2,i);
    return j;
}


/*
 *  Zero fill
 */
int psnd_fillzero(MBLOCK *mblk, int argc, char *argv[])
{
    int ntot;

    /*
     * If argv[1] contains the number of zero fills
     */
    if (argc >= 2) {
        int i, zerofill, ntot=0, isize;
        isize = min(DAT->isize, PAR->td);
        zerofill = psnd_scan_integer(argv[1]);
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
        PAR->nzf = i;
                
    }
    /*
     * Without arguments, ask the user for zero fill
     */
    else if (psnd_set_param(mblk, 0, NULL, PSND_FZ) == 0)
        return 0;
    ntot = psnd_resize_arrays(mblk, PAR->nzf, 0, NULL, DAT);
    DAT->isize = ntot;

    return TRUE;
}


/* probably oldsize is always storesize = dat->isize
*/
int psnd_do_ft(MBLOCK *mblk, int oldsize, PBLOCK *par, DBLOCK * dat)
{
    int ntot, storesize, ncalc;
    int rotate = 0;
    float dspphase = 0.0;
    
    ntot = dat->isize;
    storesize = ntot;
    ntot = psnd_resize_arrays(mblk, par->nzf, 0, &ncalc,dat);
    if (par->dspflag && par->dspshift != 0.0) {
        rotate   = (int) floor(par->dspshift);
        dspphase = -(par->dspshift - floor(par->dspshift)) * 360;
        par->dspflag = FALSE;
    }
    if (par->ftscale != 0.0 && par->ftscale != 1.0) {
        int ipos=0;
        if (rotate)
            ipos = rotate;
        dat->xreal[ipos] *= par->ftscale;
        dat->ximag[ipos] *= par->ftscale;
    }
    switch (par->ifft) {
    case FFTREA:
        /*
         * If we need maximum output size, then we need now
         * temporarily 2*max size workspace
         */
        if (ncalc > MAXSIZ) {
            ntot *= 2;
            temp_resize_arrays(mblk, dat, 2 * MAXSIZ);
        }
        ntot = fftrxf(dat->xreal,
                      dat->ximag,
                      oldsize,
                      ntot,
                      rotate);
        if (dspphase != 0.0)
            phase(dat->xreal, 
                  dat->ximag,
                  ntot, 
                  0.0,
                  dspphase,
                  1);
        if (ncalc > MAXSIZ) {
            temp_resize_arrays(mblk, dat, ntot);
         }
         break;
    case FFTIMA  :
        /*
         * If we need maximum output size, then we need now
         * temporarily 2*max size workspace
         */
        if (ncalc > MAXSIZ) {
            ntot *= 2;
            temp_resize_arrays(mblk, dat, 2 * MAXSIZ);
        }
        ntot = fftixf(dat->xreal,
                      dat->ximag,
                      oldsize,
                      ntot,
                      rotate);
        if (dspphase != 0.0)
            phase(dat->xreal, 
                  dat->ximag,
                  ntot, 
                  0.0,
                  dspphase,
                  1);
        if (ncalc > MAXSIZ) {
            temp_resize_arrays(mblk, dat, ntot);
         }
        break;
    case FFTCX:
        ntot = fftcxf(dat->xreal,
                      dat->ximag,
                      oldsize,
                      ntot,
                      rotate);
        if (dspphase != 0.0)
            phase(dat->xreal, 
                  dat->ximag,
                  ntot, 
                  0.0,
                  dspphase,
                  1);
        break;
    case DFTREA  :
        /*
         * If we need maximum output size, then we need now
         * temporarily 2*max size workspace
         */
        if (ncalc > MAXSIZ) {
            ntot *= 2;
            temp_resize_arrays(mblk, dat, 2 * MAXSIZ);
        }
        ntot = dftrxf(dat->xreal,
                      dat->ximag,
                      oldsize,
                      oldsize,
                      dat->work1,
                      dat->work2);
        if (ncalc > MAXSIZ) {
            temp_resize_arrays(mblk, dat, ntot);
         }
         break;
    case DFTIMA  :
        /*
         * If we need maximum output size, then we need now
         * temporarily 2*max size workspace
         */
        if (ncalc > MAXSIZ) {
            ntot *= 2;
            temp_resize_arrays(mblk, dat, 2 * MAXSIZ);
        }
        ntot = dftixf(dat->xreal,
                      dat->ximag,
                      oldsize,
                      oldsize,
                      dat->work1,
                      dat->work2);
        if (ncalc > MAXSIZ) {
            temp_resize_arrays(mblk, dat, ntot);
         }
        break;
    case DFTCX   :
        ntot = dftcxf(dat->xreal,
                      dat->ximag,
                      oldsize,
                      oldsize,
                      dat->work1,
                      dat->work2);
        break;
    default:
        return FALSE;
    }
    dat->isize  = ntot;
    par->nfft   = TRUE;
    dat->isspec = TRUE;

    if (par->td == 0)
        par->td     = par->nsiz;
    par->aref   = (float)ntot/(float)par->td;
    par->icstrt = 1;
    par->icstop = ntot;
    psnd_refresh_calibrate_labels(mblk);
    return TRUE;
}

/*
 * Callback func for interactive ft popup
 */
static void showft_callback(G_POPUP_CHILDINFO *ci)
{
    MBLOCK *mblk = (MBLOCK*) ci->userdata;
    POPUP_INFO *popinf = mblk->popinf + POP_FT;

    switch (ci->type) {
        case G_CHILD_OK:
            psnd_do_ft(mblk, DAT->isize, PAR,DAT);
            psnd_plot(mblk,TRUE,mblk->info->dimension_id);
            /* Fall THROUGH */
        case G_CHILD_CANCEL:
            popinf->visible = FALSE;
            return;
    }

    switch (ci->id) {
        case ID_NFFT:
            PAR->nfft = ci->item;
            break;
        case ID_IFFT:
            PAR->ifft = ft2user_modes[ci->item];
            break;
        case ID_NZF:
            PAR->nzf = ci->item;
            break;
        case ID_FTSCALE:
            PAR->ftscale = psnd_scan_float(ci->label);
            break;

    }
}


#define POPUP_LABEL_ID	0
void psnd_refresh_all_ft_labels(MBLOCK *mblk)
{
    POPUP_INFO *popinf = mblk->popinf + POP_FT;

    if (popinf->cont_id) {
        char *label;

        label = psnd_sprintf_temp("Direction %d. %s", 
                                     PAR->icrdir, "FOURIER TRANSFORM");
        g_popup_set_label(popinf->cont_id, POPUP_LABEL_ID, label);

        g_popup_set_selection(popinf->cont_id, ID_NFFT, PAR->nfft);
        if (PAR->ifft == 0)
            PAR->ifft = 1;
        g_popup_set_selection(popinf->cont_id, ID_IFFT, 
                                 psnd_user2ft_modes(PAR));
/* Can have wrong text */
        g_popup_set_selection(popinf->cont_id, ID_NZF, PAR->nzf);
                                 
        label = psnd_sprintf_temp("%.2f", PAR->ftscale);
        g_popup_set_label(popinf->cont_id, ID_FTSCALE, label);

    }
}

/*
 * Popup menu for ft
 */
#define MAXID	10
static void psnd_showft_popup(MBLOCK *mblk)
{
    static G_POPUP_CHILDINFO ci[MAXID];
    int id, cont_id;
    POPUP_INFO *popinf = mblk->popinf + POP_FT;

    if (popinf->visible)
        return;
    popinf->visible = TRUE;
    id = -1;
    popinf->cont_id = 
        g_popup_container_open(mblk->info->win_id, "Fourier transform", 0); 
    cont_id = popinf->cont_id;
    id++;
    id = param_add_fourier1(mblk, cont_id, ci, id, 
                                 showft_callback, PAR);
    id++;
    id = param_add_fourier2(mblk, cont_id, ci, id, 
                                 showft_callback, PAR);

    id++;
    g_popup_init_info(&(ci[id]));
    ci[id].type      = G_CHILD_OK;
    ci[id].func      = showft_callback;
    ci[id].userdata  = (void*) mblk;
    g_popup_add_child(cont_id, &(ci[id]));

    id++;
    g_popup_init_info(&(ci[id]));
    ci[id].type      = G_CHILD_CANCEL;
    ci[id].func      = showft_callback;
    ci[id].userdata  = (void*) mblk;
    g_popup_add_child(cont_id, &(ci[id]));

    assert(id<MAXID);

    g_popup_container_show(popinf->cont_id) ;
}


int psnd_ft(MBLOCK *mblk, int argc, char *argv[])
{
    int oldsize;

    if (PAR->ifft == DFTCX)
        PAR->ifft = FFTCX;
    else if (PAR->ifft == DFTREA)
        PAR->ifft = FFTREA;
    else if (PAR->ifft == DFTIMA)
        PAR->ifft = FFTIMA;
    if (argc == 0) {
        /*
         * argc == 0 means:
         * Command from a button, NOT from command line
         */
        oldsize = DAT->isize;
        /*
        if (psnd_set_param(mblk, 0, NULL, PSND_FT) == 0)
            return 0;
        */   
        /*
         * Popup menu
         */
        psnd_showft_popup(mblk);
        return 0;
    }
    else {
        /*
         * If argv[1] contains the number of zero fills
         */
        int i, zerofill, ntot=0, isize;
        isize = min(DAT->isize, PAR->td);

        zerofill = PAR->nzf;
        if (argc > 1)
            zerofill = psnd_scan_integer(argv[1]);
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
        PAR->nzf = i;
        oldsize = DAT->isize;
    }
    return psnd_do_ft(mblk,oldsize, PAR,DAT);

}

int psnd_dft(MBLOCK *mblk, int argc, char *argv[])
{
    int oldsize;

    if (PAR->ifft == NOFFT) {
        oldsize = DAT->isize;
        if (psnd_set_param(mblk, 0, NULL, PSND_FT) == 0)
            return 0;
    }
    else {
        if (PAR->icmplx)
            PAR->ifft = DFTCX;
        else
            PAR->ifft = DFTREA;
        PAR->nzf = 0;
        oldsize = DAT->isize;
    }
    return psnd_do_ft(mblk, oldsize, PAR,DAT);

}

int psnd_do_if(MBLOCK *mblk, int oldsize, PBLOCK *par, DBLOCK * dat)
{
    int ntot,ntot2,ncalc;

    ntot2 = par->irstop - par->irstrt+1;
    if (ntot2 <= 1)
        ntot2 = par->td;

    switch (par->ifft) {
    case DFTREA  :
    case FFTREA  :
    case DFTIMA  :
    case FFTIMA  :
        ntot = psnd_resize_arrays(mblk, 1, 0, &ncalc, dat);
        /*
         * If we need maximum output size, then we need now
         * temporarily 2*max size workspace
         */
        if (ncalc > MAXSIZ) {
            ntot *= 2;
            temp_resize_arrays(mblk, dat, 2 * MAXSIZ);
        }
        switch (par->ifft) {
            case DFTREA  :
                ntot = dftrxb(dat->xreal,
                              dat->ximag,
                              oldsize,
                              oldsize,
                              dat->work1,
                              dat->work2);
                break;
            case FFTREA  :
                ntot = fftrxb(dat->xreal,
                              dat->ximag,
                              ntot,
                              oldsize);
                break;
            case DFTIMA  :
                ntot = dftixb(dat->xreal,
                              dat->ximag,
                              oldsize,
                              oldsize,
                              dat->work1,
                              dat->work2);
                break;
            case FFTIMA  :
                ntot = fftixb(dat->xreal,
                              dat->ximag,
                              oldsize,
                              oldsize);
                break;
        }
        if (ncalc > MAXSIZ) 
            temp_resize_arrays(mblk, dat, ntot);
        break;
    case DFTCX   :
        ntot = dftcxb(dat->xreal,
                      dat->ximag,
                      oldsize,
                      oldsize,
                      dat->work1,
                      dat->work2);
        break;
    case FFTCX   :
        ntot = fftcxb(dat->xreal,
                      dat->ximag,
                      oldsize,
                      oldsize);
        break;
    default:
        return FALSE;
    }
    dat->isspec = FALSE;
    dat->isize  = min(par->td, ntot2);

    return TRUE;
}

int psnd_if(MBLOCK *mblk, int argc, char *argv[])
{
    int oldsize;

    oldsize = DAT->isize;
    if (PAR->ifft == NOFFT) {
        if (psnd_set_param(mblk, 0, NULL, PSND_FT) == 0)
            return 0;
        if (PAR->ifft == NOFFT)
            return 0;
    }
    return psnd_do_if(mblk, oldsize, PAR,DAT);

}

