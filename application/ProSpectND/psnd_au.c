/********************************************************************/
/*                           psnd_au.c                              */
/*                                                                  */
/* 1997, Albert van Kuik                                            */
/********************************************************************/
#include <stdio.h>
#include <stdlib.h>
#include <ctype.h>
#include <string.h>
#include <stdarg.h>
#include <assert.h>
#include <math.h>
#include <unistd.h>
#include "genplot.h"
#include "psnd.h"
#include "nmrtool.h"


/***********************************************************************/
#include <errno.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <math.h>  /* For math functions, cos(), sin(), etc. */
#include "psnd_au_y.h"  /* Contains definition of `symrec'        */

#ifdef _WIN32
int towin(char *label)
{
    static char buf[PSND_STRLEN+1];
    int i,j=0;

    j=0;
    if (label == NULL || label[0] == '\0')
        return 0;
    if (label[0] == '\n') 
        buf[j++] = '\r';
    buf[j++] = label[0];
    for (i=1;label[i] && j<PSND_STRLEN-2;i++) {
        if (label[i] == '\n' && label[i-1] != '\r')
            buf[j++] = '\r';
        buf[j++] = label[i];
    }
    buf[j] = '\0';
    strcpy(label, buf);
    return j;
}

static char *towinfilename_base(char *label)
{
    static char buf[2*PSND_STRLEN+1];
    int i,j=0;

    j=0;
    if (label == NULL || label[0] == '\0')
        return NULL;
    for (i=0;label[i] && j<PSND_STRLEN-2;i++) {
        if (label[i] == '\\' || label[i] == '/') {
            buf[j++] = '\\';
            buf[j++] = '\\';
            if (i>0)
                while (label[i+1] == '\\' || label[i+1] == '/')
                    i++;
        }
        else
            buf[j++] = label[i];
    }
    buf[j] = '\0';
    return buf;
}

char *towinfilename(char *label)
{
    char *p;
    
    p = towinfilename_base(label);
    if (p)
        strcpy(label, p);
    return p;
}

static char *towinfilename_xxx(char *label)
{
    static char buf[2*PSND_STRLEN+1];
    int i,j=0;

    j=0;
    if (label == NULL || label[0] == '\0')
        return NULL;
    for (i=0;label[i] && j<PSND_STRLEN-2;i++) {
        if (label[i] == '\\' || label[i] == '/') {
            buf[j++] = '\\';
            if (i>0)
                while (label[i+1] == '\\' || label[i+1] == '/')
                    i++;
        }
        else
            buf[j++] = label[i];
    }
    buf[j] = '\0';
    return buf;
}
#else
int tounix(char *label)
{
    static char buf[PSND_STRLEN+1];
    int i,j=0;

    j=0;
    if (label == NULL || label[0] == '\0')
        return 0;
    for (i=0;label[i] && j<PSND_STRLEN-2;i++) {
        if (label[i] != '\r')
            buf[j++] = label[i];
    }
    buf[j] = '\0';
    strcpy(label, buf);
    return j;
}

char *tounixfilename_base(char *label)
{
    static char buf[2*PSND_STRLEN+1];
    int i,j=0;

    j=0;
    if (label == NULL || label[0] == '\0')
        return NULL;
    for (i=0;label[i] && j<PSND_STRLEN-2;i++) {
        if (label[i] == '\\' || label[i] == '/') {
            buf[j++] = '/';
            while (label[i+1] == '\\' || label[i+1] == '/')
                i++;
        }
        else
            buf[j++] = label[i];
    }
    buf[j] = '\0';
/*
    strcpy(label, buf);
    */
    return buf;
}

#endif

char *au_check_filename(char *name)
{
#ifdef _WIN32
    return towinfilename_xxx(name);
#else
    return tounixfilename_base(name);
#endif
}

char *au_check_openmode(char *mode)
{
    int i,j=0;
    static char buf[10];

    for (i=0,j=0; mode[i] != '\0' && i < 9; i++) {
        switch (mode[i]) {
            case 'r' :
            case 'w' :
            case 'a' :
            case '+' :
#ifdef _WIN32
            case 't' :
            case 'b' :
#endif
                buf[j] = mode[i];
                j++;
                break;
        }
    }
    buf[j] = '\0';
    return buf;
}

void au_array_shift(void *param, float *x, int n1, int n2, int n3)
{
    MBLOCK *mblk = (MBLOCK*) param;
    xxshfn(x, n1, n2, n3, mblk->info->block_size);
}

void calc_block_and_dimension(MBLOCK *mblk, int *block_id, int *dim)
{
    (*block_id)--;
    (*dim)--;
    if ((*block_id) <0 || (*block_id) >= mblk->info->max_block)
        (*block_id) = mblk->info->block_id;
    if ((*dim) < 0 || (*dim) >= MAX_DIM)
        (*dim) = mblk->info->dimension_id;
}

float au_get_spectral_width(void *param, int block_id, int dim)
{
    MBLOCK *mblk = (MBLOCK*) param;
    calc_block_and_dimension(mblk, &block_id, &dim);
    return (mblk->par[block_id] + dim)->swhold;
}

void au_set_spectral_width(void *param, int block_id, int dim, float sw)
{
    MBLOCK *mblk = (MBLOCK*) param;
    calc_block_and_dimension(mblk, &block_id, &dim);
    (mblk->par[block_id] + dim)->swhold = sw;
    if (mblk->dat[block_id]->access[0] -1 == dim)
        mblk->dat[block_id]->sw = sw;
}

float au_get_spectral_frequecy(void *param, int block_id, int dim)
{
    MBLOCK *mblk = (MBLOCK*) param;
    calc_block_and_dimension(mblk, &block_id, &dim);
    return (mblk->par[block_id] + dim)->sfd;
}

void au_set_spectral_frequecy(void *param, int block_id, int dim, float sf)
{
    MBLOCK *mblk = (MBLOCK*) param;
    calc_block_and_dimension(mblk, &block_id, &dim);
    (mblk->par[block_id] + dim)->sfd = sf;
}

float au_get_reference_channel(void *param, int block_id, int dim, int code)
{
    MBLOCK *mblk = (MBLOCK*) param;
    calc_block_and_dimension(mblk, &block_id, &dim);
    if (code == 1)
        return (mblk->par[block_id] + dim)->aref;
    if (code == 2)
        return (mblk->par[block_id] + dim)->bref;
    return (mblk->par[block_id] + dim)->xref;
}

void au_set_reference_channel(void *param, int block_id, int dim, float f, int code)
{
    MBLOCK *mblk = (MBLOCK*) param;
    calc_block_and_dimension(mblk, &block_id, &dim);
    if (code == 1)
        psnd_set_calibration_aref(f, (mblk->par[block_id] + dim), mblk->dat[block_id]);
    else if (code == 2)
        psnd_set_calibration_bref(f, (mblk->par[block_id] + dim), mblk->dat[block_id]);
    else
        psnd_set_calibration(f, (mblk->par[block_id] + dim), mblk->dat[block_id]);
}

int au_get_size(void *param, int block_id)
{
    MBLOCK *mblk = (MBLOCK*) param;
    if (block_id >= 1 && block_id <= mblk->info->max_block)
        return mblk->dat[block_id-1]->isize;
    else
        return DAT->isize;
}

void au_set_size(void *param, int block_id, int size)
{
    MBLOCK *mblk = (MBLOCK*) param;
    if (block_id >= 1 && block_id <= mblk->info->max_block)
       psnd_set_datasize(mblk, size, FALSE, mblk->dat[block_id-1]);
    else
       psnd_set_datasize(mblk, size, FALSE, DAT);
}


int au_get_dimensions(void *param, int block_id)
{
    MBLOCK *mblk = (MBLOCK*) param;
    if (block_id >= 1 && block_id <= mblk->info->max_block)
        return mblk->dat[block_id-1]->ityp;
    else
        return DAT->ityp;
}

void au_set_dimensions(void *param, int block_id, int ityp)
{
    MBLOCK *mblk = (MBLOCK*) param;
    if (block_id >= 1 && block_id <= mblk->info->max_block)
        mblk->dat[block_id-1]->ityp = ityp;
    else
        DAT->ityp = ityp;
}


int au_get_timedomain(void *param, int block_id, int dim)
{
    MBLOCK *mblk = (MBLOCK*) param;
    calc_block_and_dimension(mblk,&block_id, &dim);
    return (mblk->par[block_id] + dim)->nsiz;
}

void au_set_timedomain(void *param, int block_id, int dim, int siz)
{
    MBLOCK *mblk = (MBLOCK*) param;
    calc_block_and_dimension(mblk,&block_id, &dim);
    (mblk->par[block_id] + dim)->nsiz = siz;
}


float au_get_phase0(void *param, int block_id, int dim)
{
    MBLOCK *mblk = (MBLOCK*) param;
    calc_block_and_dimension(mblk,&block_id, &dim);
    return (mblk->par[block_id] + dim)->ahold;
}

void au_set_phase0(void *param, int block_id, int dim, float f)
{
    MBLOCK *mblk = (MBLOCK*) param;
    calc_block_and_dimension(mblk,&block_id, &dim);
    (mblk->par[block_id] + dim)->ahold = f;
}

float au_get_phase1(void *param, int block_id, int dim)
{
    MBLOCK *mblk = (MBLOCK*) param;
    calc_block_and_dimension(mblk,&block_id, &dim);
    return (mblk->par[block_id] + dim)->bhold;
}

void au_set_phase1(void *param, int block_id, int dim, float f)
{
    MBLOCK *mblk = (MBLOCK*) param;
    calc_block_and_dimension(mblk,&block_id, &dim);
    (mblk->par[block_id] + dim)->bhold = f;
}

int au_get_phase_pos(void *param, int block_id, int dim)
{
    MBLOCK *mblk = (MBLOCK*) param;
    calc_block_and_dimension(mblk,&block_id, &dim);
    return (mblk->par[block_id] + dim)->ihold;
}

void au_set_phase_pos(void *param, int block_id, int dim, int i)
{
    MBLOCK *mblk = (MBLOCK*) param;
    calc_block_and_dimension(mblk,&block_id, &dim);
    (mblk->par[block_id] + dim)->ihold = i;
}

void au_do_phase_correction(void *param)
{
    MBLOCK *mblk = (MBLOCK*) param;
    psnd_pk(mblk, PAR, DAT);
}

void au_do_auto_phase_correction(void *param)
{
    MBLOCK *mblk = (MBLOCK*) param;
    psnd_auto_phase(mblk, MODE_P0+MODE_P1);
}

void au_normalize_phase(void *param, int i0)
{
    MBLOCK *mblk = (MBLOCK*) param;
    psnd_set_phase_position(mblk, PAR, DAT, i0);
}

int au_query_data_as_spectrum(void *param, int block_id, int dim)
{
    MBLOCK *mblk = (MBLOCK*) param;
    calc_block_and_dimension(mblk,&block_id, &dim);
    return (mblk->par[block_id] + dim)->isspec;
}

void au_define_data_as_spectrum(void *param, int block_id, int dim, int def)
{
    MBLOCK *mblk = (MBLOCK*) param;
    calc_block_and_dimension(mblk,&block_id, &dim);
    if (mblk->info->block_id == block_id && mblk->info->dimension_id == dim)
        DAT->isspec = def;
    (mblk->par[block_id] + dim)->isspec = def;
}

void au_set_dspshift(void *param, int block_id, int dim, float shift)
{
    MBLOCK *mblk = (MBLOCK*) param;
    calc_block_and_dimension(mblk,&block_id, &dim);
    (mblk->par[block_id] + dim)->dspshift = max(0, shift);
}


float au_get_dspshift(void *param, int block_id, int dim)
{
    MBLOCK *mblk = (MBLOCK*) param;
    calc_block_and_dimension(mblk,&block_id, &dim);
    return (mblk->par[block_id] + dim)->dspshift;
}


void au_set_ftscale(void *param, int block_id, int dim, float scale)
{
    MBLOCK *mblk = (MBLOCK*) param;
    calc_block_and_dimension(mblk,&block_id, &dim);
    (mblk->par[block_id] + dim)->ftscale = scale;
}


float au_get_ftscale(void *param, int block_id, int dim)
{
    MBLOCK *mblk = (MBLOCK*) param;
    calc_block_and_dimension(mblk,&block_id, &dim);
    return (mblk->par[block_id] + dim)->ftscale;
}


void au_do_ft(void *param, int i, int mode)
{
    MBLOCK *mblk = (MBLOCK*) param;
    if (i >= 0)
        PAR->nzf = i;
    if (mode)
        PAR->ifft =mode;
    else if (PAR->icmplx)
        PAR->ifft = FFTCX;
    else
        PAR->ifft = FFTREA;
    psnd_do_ft(mblk, DAT->isize, PAR, DAT);
}

void au_do_dft(void *param, int mode)
{
    MBLOCK *mblk = (MBLOCK*) param;
    if (mode)
        PAR->ifft =mode;
    else if (PAR->icmplx)
        PAR->ifft = DFTCX;
    else
        PAR->ifft = DFTREA;
    psnd_do_ft(mblk, DAT->isize, PAR, DAT);
}

void au_do_if(void *param, int mode)
{
    MBLOCK *mblk = (MBLOCK*) param;
    if (mode)
        PAR->ifft =mode;
    psnd_do_if(mblk, DAT->isize, PAR, DAT);
}


void au_set_shift(void *param, float f)
{
    MBLOCK *mblk = (MBLOCK*) param;
    PAR->rsh = f;
}

void au_set_line_broadening(void *param, float f)
{
    MBLOCK *mblk = (MBLOCK*) param;
    PAR->rlb = f;
}

void au_set_gaussian_fraction(void *param, float f)
{
    MBLOCK *mblk = (MBLOCK*) param;
    PAR->gn = f;
}

void au_set_points_for_trapezian_mul(void *param, int i)
{
    MBLOCK *mblk = (MBLOCK*) param;
    PAR->itm = i;
}

void au_set_window_size(void *param, int start, int stop)
{
    MBLOCK *mblk = (MBLOCK*) param;
    PAR->iwstrt = start;
    PAR->iwstop = stop;
}

void au_do_window(void *param, int id)
{
    MBLOCK *mblk = (MBLOCK*) param;
    psnd_set_window_id(id, PAR);
    psnd_do_window(mblk, TRUE, FALSE);
}



void au_do_baseline_correction(void *param)
{
    MBLOCK *mblk = (MBLOCK*) param;
    psnd_baseline(mblk, TRUE, FALSE, mblk->cpar_hardcopy);
}

void au_set_baseline_mode(void *param, int mode)
{
    MBLOCK *mblk = (MBLOCK*) param;
    PAR->ibase = mode;
}

void au_set_baseline_terms(void *param, int terms)
{
    MBLOCK *mblk = (MBLOCK*) param;
    PAR->iterms = terms;
}

void au_set_baseline_terms2(void *param, int terms)
{
    MBLOCK *mblk = (MBLOCK*) param;
    PAR->iterms2 = terms;
}

int au_get_baseline_terms(void *param)
{
    MBLOCK *mblk = (MBLOCK*) param;
    return PAR->iterms;
}

void au_set_baseline_range(void *param, int start, int stop)
{
    MBLOCK *mblk = (MBLOCK*) param;
    PAR->ibstrt = start;
    PAR->ibstop = stop;
}


void au_set_baseline_water(void *param, int start, int stop)
{
    MBLOCK *mblk = (MBLOCK*) param;
    PAR->ibwater = (start+stop)/2;
    PAR->ibwidth = stop-start;
}


int au_do_watwa(void *param)
{
    MBLOCK *mblk = (MBLOCK*) param;
    if (PAR->iopt == 0)
        PAR->iopt = 1;
    return psnd_do_watwa(PAR, DAT);
}

void au_set_watwa_shift(void *param, float shift)
{
    MBLOCK *mblk = (MBLOCK*) param;
    PAR->wshift = shift;
}

void au_set_watwa_power(void *param, int cos_pow)
{
    MBLOCK *mblk = (MBLOCK*) param;
    PAR->iopt = cos_pow;
}

void au_set_watwa_convolution_width(void *param, float w)
{
    MBLOCK *mblk = (MBLOCK*) param;
    PAR->kc = w;
}

int au_do_waterfit(void *param)
{
    MBLOCK *mblk = (MBLOCK*) param;
    return psnd_fit_waterline(mblk, 0, NULL, TRUE);
}

void au_set_waterfit_pos(void *param, int pos)
{
    MBLOCK *mblk = (MBLOCK*) param;
    PAR->waterpos = pos;
}

void au_set_waterfit_wid(void *param, int width)
{
    MBLOCK *mblk = (MBLOCK*) param;
    PAR->waterwid = width;
}

int au_do_linear_prediction(void *param, char *mode, int num_roots)
{
    int i,m=0;
    int npoles,mroot,replace,lpcmode;

    MBLOCK *mblk = (MBLOCK*) param;
    npoles  = num_roots;
    mroot   = FALSE;
    replace = FALSE;
    lpcmode = -1;
    while (mode[m]) {
        i = toupper(mode[m++]);
        if (i == 'F')
            lpcmode = PREDICT_FORWARD;
        else if (i == 'B')
            lpcmode = PREDICT_BACKWARD;
        else if (i == 'G')
            lpcmode = PREDICT_GAP;
        else if (i == 'M')
            mroot = TRUE;
        else if (i == 'R')
            replace = TRUE;
    }
    if (lpcmode == -1)
        return FALSE;
    PAR->npoles  = npoles;
    PAR->mroot   = mroot;
    PAR->replace = replace;
    PAR->lpcmode = lpcmode;
    PAR->nlpc    = TRUE;
    if (PAR->lpcmode == PREDICT_FORWARD)
        psnd_resize_arrays(mblk, 0, PAR->nfut, 
                            NULL, DAT);
    return psnd_linpr(mblk,DAT->xreal, DAT->ximag, PAR->icmplx, DAT->isize, 
                       PAR->nfut, PAR->npoles, PAR->toler,
                       PAR->lpc,  PAR->lpcmode,
                       PAR->mroot, PAR->ngap1, PAR->ngap2, 
                       PAR->nstart,PAR->nstop, PAR->replace,
                       mblk->info->block_size);
}

void au_set_lpc_predict_mode(void *param, int mode)
{
    MBLOCK *mblk = (MBLOCK*) param;
    PAR->lpc = mode;
}

void au_set_lpc_future_size(void *param, int s)
{
    MBLOCK *mblk = (MBLOCK*) param;
    PAR->nfut = s;
}

void au_set_lpc_range(void *param, int start, int stop)
{
    MBLOCK *mblk = (MBLOCK*) param;
    PAR->nstart = start;
    PAR->nstop  = stop;
}

void au_set_lpc_gaprange(void *param, int start, int stop)
{
    MBLOCK *mblk = (MBLOCK*) param;
    PAR->ngap1 = start;
    PAR->ngap2 = stop;
}

int au_get_file(void *param, char *name)
{
    int ok,i;
    int  argi[MAX_DIM];
    char *argv[MAX_DIM];

    MBLOCK *mblk = (MBLOCK*) param;
    for (i=0;i<MAX_DIM;i++) {
        argi[i] = i+1;
        argv[i] = NULL;
    }
    /*
     * The filename must be the second argument
     */
    argv[1] = name;
    if (!psnd_open(mblk, 2, argv)) {
        psnd_enable_open_buttons(mblk, FALSE);
        return 0;
    }
    psnd_enable_open_buttons(mblk, TRUE);
    psnd_clear_popup_bookmark(mblk, DAT);
    /*
     * Open with some standard directions, otherwise we
     * can not read the parameters
     */
    if (!psnd_direction(mblk, 0, NULL, MAX_DIM, argi))  {
        psnd_close_file(mblk); 
        psnd_enable_open_buttons(mblk, FALSE);
        return 0;
    }
    psnd_getparam(mblk,FALSE);
    psnd_set_datasize(mblk, PAR->nsiz, TRUE, DAT);
    return 1;
}

int au_open_outputfile(void *param, int count, char *name[], int mode)
{
    MBLOCK *mblk = (MBLOCK*) param;
    return psnd_open_outfile(mblk, count, name, mode, PAR, DAT); 
}

void au_set_outfile_truncation(void *param, int sizeof_float, 
                               int tresh_flag, float tresh_levels1,
                               float tresh_levels2, float tresh_levels3)
{
    MBLOCK *mblk = (MBLOCK*) param;
    DAT->finfo[OUTFILE].sizeof_float      = sizeof_float; 
    DAT->finfo[OUTFILE].tresh_flag        = tresh_flag; 
    DAT->finfo[OUTFILE].tresh_levels[0]   = tresh_levels1; 
    DAT->finfo[OUTFILE].tresh_levels[1]   = tresh_levels2; 
    DAT->finfo[OUTFILE].tresh_levels[2]   = tresh_levels3; 
}

int au_close_outfile(void *param)
{
    MBLOCK *mblk = (MBLOCK*) param;
    return psnd_close_outfile(mblk,DAT->pars[0], DAT);
}

void au_close_infile(void *param)
{
    MBLOCK *mblk = (MBLOCK*) param;
    psnd_clear_popup_bookmark(mblk, DAT);
    psnd_close_file(mblk);
    psnd_enable_open_buttons(mblk, FALSE);
}

int au_set_ranges_outfile(void *param, int count, int *range, int force)
{
    int i,j;

    MBLOCK *mblk = (MBLOCK*) param;
    for (i=0,j=1;i<MAX_DIM && j<count;i++) {
        /*
         * ok, we do not know the size in the processing direction
         * because possible ft follows later.
         * So we will no longer try to be clever here.
         */
        if (range[j+1] > 0 && range[j+1] < range[j])
            return FALSE;
        if (force) {
            if (range[j] > 0)
                PAR0(i)->isstrt = range[j];
            if (range[j+1] > 0)
                PAR0(i)->isstop = range[j+1];

        }
        j+=3;
    }
    return TRUE;
}

int au_set_direction(void *param, int count, float arg[])
{
    int i,access[MAX_DIM];
    
    MBLOCK *mblk = (MBLOCK*) param;
    count = min(MAX_DIM,count);
    for (i=0;i<count;i++)
        access[i] = (int) arg[i];
    if (psnd_direction(mblk, 0, NULL, count, access))
        return 1;
    else
        return 0;    
}

void au_set_postmode(void *param, int block_id, int dim, int mode)
{
    MBLOCK *mblk = (MBLOCK*) param;
    calc_block_and_dimension(mblk,&block_id, &dim);
    (mblk->par[block_id] + dim)->ipost = mode;
}

int au_get_postmode(void *param, int block_id, int dim)
{
    MBLOCK *mblk = (MBLOCK*) param;
    calc_block_and_dimension(mblk,&block_id, &dim);
    return (mblk->par[block_id] + dim)->ipost;
}

void au_set_premode(void *param, int block_id, int dim, int mode)
{
    MBLOCK *mblk = (MBLOCK*) param;
    calc_block_and_dimension(mblk,&block_id, &dim);
    (mblk->par[block_id] + dim)->ipre = mode;
}

int au_get_premode(void *param, int block_id, int dim)
{
    MBLOCK *mblk = (MBLOCK*) param;
    calc_block_and_dimension(mblk,&block_id, &dim);
    return (mblk->par[block_id] + dim)->ipre;
}

int au_read_next_record(void *param, int num, float *keys)
{
    int i, argc, argval[MAX_DIM];
    
    MBLOCK *mblk = (MBLOCK*) param;
    argc = min(num,MAX_DIM);
    for (i=0;i<argc;i++)
        argval[i] = (int) keys[i];
    return psnd_rn(mblk,argc, argval, DAT);
}

void psnd_set_record_read_range(MBLOCK *mblk, int start, int stop, PBLOCK *par)
{
    if (start == 0)
        start = 1;
    else {
        start = max(1,start);
        start = min(par->nsiz,start);
    }
    if (stop == 0)
        stop = par->nsiz;
    else {
        stop  = max(1,stop);
        stop  = min(par->nsiz, stop);
    }
    if (stop >= start) {
        par->irstrt = start;
        par->irstop = stop;
        psnd_check_contour_limits(mblk);
    }
}

void au_set_record_read_range(void *param, int start, int stop)
{
    MBLOCK *mblk = (MBLOCK*) param;

    psnd_set_record_read_range(mblk, start, stop, DAT->pars[0]);
}


int au_return_record_size_check(void *param)
{
    MBLOCK *mblk = (MBLOCK*) param;
    return (PAR->nsiz == DAT->isize);
}

int au_return_record(void *param)
{
    MBLOCK *mblk = (MBLOCK*) param;
    return psnd_returnrec(mblk, FALSE);
}

int au_write_record(void *param, int num, float *keys)
{
    int i, argc, argval[MAX_DIM];
    
    MBLOCK *mblk = (MBLOCK*) param;
    argc = min(num,MAX_DIM);
    for (i=0;i<argc;i++)
        argval[i] = (int) keys[i];
    return psnd_rw(mblk,argc, argval, DAT);
}

void au_set_record_write_range(void *param, int start, int stop)
{
    MBLOCK *mblk = (MBLOCK*) param;
    DAT->pars[0]->isstrt = start;
    DAT->pars[0]->isstop = stop;
}


/*
 * dir = direction 1,2,3
 * return the original record size, multiplied by 2 if complex
 */
int au_get_range_stop(void *param, int dir)
{
    MBLOCK *mblk = (MBLOCK*) param;
    int size;
    if (DAT->pars[dir-1]->isstop > 0 && DAT->pars[dir-1]->isstrt > 0) 
        size = min(DAT->pars[dir-1]->isstop, DAT->pars[dir-1]->nsiz);
    else
        size = DAT->pars[dir-1]->nsiz;
    return size;
}

/*
 * dir = direction 1,2,3
 * return the original record size, multiplied by 2 if complex
 */
int au_get_range_start(void *param, int dir)
{
    MBLOCK *mblk = (MBLOCK*) param;
    int size;
    if (DAT->pars[dir-1]->isstop > 0 && DAT->pars[dir-1]->isstrt>0) 
        size = DAT->pars[dir-1]->isstrt;
    else
        size = 1;
    return size;
}

int au_get_range_complex_offset(void *param, int dir)
{
    int size;
    MBLOCK *mblk = (MBLOCK*) param;
    size = DAT->pars[dir-1]->nsiz;;
    return size;
}

/*
 * dir = direction 1,2,3
 * return the original record size, multiplied by 2 if complex
 */
int au_get_full_range(void *param, int dir)
{
    MBLOCK *mblk = (MBLOCK*) param;
    return DAT->par0[dir-1]->nsiz * (1+DAT->par0[dir-1]->icmplx);
}


/*
 * steps = number of dimensions in range
 * range = for each dimension:
 *         num_elements start_1 stop_1 start_2 stop_2 ... start_n stop_n
 *         where: num_elements = the number of starts and stops following
 *                               num_elements
 *         for example:
 *             2 0 511
 *             num_elements (is one start and one stop) = 2
 *             start = 0 (first element of range)
 *             stop  = 511 (last element of range)
 *
 * returns array of shorts with content: 
 *         pairs of 'dimension' 'key' in assending order of dimension
 *
 *         for example:
 *         1 1   2 1   3 1   3 2   3 4   2 2   3 1   3 2  ..
 *
 * NOTE: the list that is returned has been allocated here!!
 *       must be freed after use !!!!
 */
short *au_build_sorted_range(void *param, int steps, int range[])
{
    int icmplx[MAX_DIM];
    short *rlist;
    int i,j,n,nsize=1,*rangetemp,rangesize,loop_dim;

    MBLOCK *mblk = (MBLOCK*) param;
    loop_dim = 0;
    n=0;
    rangetemp = (int*)malloc(steps * sizeof(int)*2);
    assert(rangetemp);
    rangesize = steps;
    for (i=0;i<MAX_DIM;i++)
        icmplx[i] = 0;
    for (i=0,j=0;i<steps && loop_dim < MAX_DIM;i+=n+1) {
        int tmpsize;
        /*
         * Number of elements for this dimension
         */
        n = range[i];
        /*
         * Number of dimensions to loop over
         */
        loop_dim++;
        icmplx[loop_dim] = 0;
        if (n==2 && 
            (range[i+1] == UNDETERMINED || range[i+n] == UNDETERMINED) &&
            DAT->pars[loop_dim]->icmplx) 
                icmplx[loop_dim] = 1;
        if (range[i+1] == UNDETERMINED)
            range[i+1] = au_get_range_start(param,loop_dim+1)-1;
        if (range[i+n] == UNDETERMINED)
            range[i+n] = au_get_range_stop(param,loop_dim+1)-1;
        if (icmplx[loop_dim]) {
            rangetemp[j++] = 2*n;
            rangetemp[j++] = range[i+1];
            rangetemp[j++] = range[i+n];
           
            rangetemp[j++] = range[i+1]+au_get_range_complex_offset(param,loop_dim+1);
            rangetemp[j++] = range[i+n]+au_get_range_complex_offset(param,loop_dim+1);

            tmpsize = 2*range[i+n]-range[i+1]+2;
        }
        else {
            rangetemp[j++] = n;
            rangetemp[j++] = range[i+1];
            rangetemp[j++] = range[i+n];
            tmpsize = range[i+n]-range[i+1]+2;
        }
        nsize *= 1+tmpsize;
    }    

    rlist = psnd_get_sorted_list(mblk,DAT, rangetemp, range,
                                  loop_dim, nsize, icmplx, TRUE);
    free(rangetemp);
    return rlist;

}

int au_spline_read(void *param, char *name)
{
    MBLOCK *mblk = (MBLOCK*) param;
    return psnd_spline_read(mblk, name, DAT);
}

int au_spline_calc(void *param)
{   
    MBLOCK *mblk = (MBLOCK*) param;
    return psnd_spline_calc(mblk, DAT->isize, DAT->xreal, DAT->xbufr2);
}

int au_write_smxarray(void *param, char *name, float *array1, float *array2, 
                      int start, int stop)
{
    int isize;
                
    MBLOCK *mblk = (MBLOCK*) param;
    if (start > stop || start < 1 || stop > mblk->info->block_size) 
        return 0;
    isize = stop - start + 1;
    if (array2 == NULL)
        return psnd_array_out(mblk,name, 
                           array1 + start - 1,
                           DAT->work1, 
                           isize,
                           FALSE,
                           DAT->npar,
                           PAR,
                           DAT);
    else
        return psnd_array_out(mblk,name, 
                           array1 + start - 1,
                           array2 + start - 1, 
                           isize,
                           TRUE,
                           DAT->npar,
                           PAR,
                           DAT);
}

int au_read_smxarray(void *param, char *name, float *array1, float *array2,
                     int start, int stop)
{
    int isize, icomplex, newsize;
                
    MBLOCK *mblk = (MBLOCK*) param;
    /*
     * When start and stop are 0, then
     * the original data size of the 'array on file'
     * is read
     */
    if (start == 0 && stop == 0)
        isize = 0;
    else {
        if (start > stop || start < 1 || stop > mblk->info->block_size) 
            return 0;
        isize = stop - start + 1;
    }
    icomplex = psnd_array_in(mblk,name, DAT->work1, DAT->work2, &newsize);
    if (icomplex == 0)
        return FALSE;
    if (isize == 0) {
        isize = newsize;
        start = 1;
    }
    memcpy(array1+start-1,DAT->work1+start-1,sizeof(float)*isize);
    if (array2 != NULL && icomplex == 2)
        memcpy(array2+start-1,DAT->work2+start-1,sizeof(float)*isize);
    return icomplex;
}


void au_do_reverse(float *f, int start, int stop)
{
    invrse2(f, start, stop);
}

void au_transfer_parameters(void *param, int block1, int block2)
{
    MBLOCK *mblk = (MBLOCK*) param;
    psnd_copyparam(mblk, block1 - 1, block2 - 1);
}


void au_select_block(void *param, int id)
{
    MBLOCK *mblk = (MBLOCK*) param;
    psnd_change_block(mblk,id-1, FALSE);
}

void au_array_average(void *param)
{
    MBLOCK *mblk = (MBLOCK*) param;
    psnd_arrayaverage(DAT);
}

void au_array_power(void *param)
{
    MBLOCK *mblk = (MBLOCK*) param;
    psnd_arraypower(DAT);
}

void au_do_hilbert_transform(void *param)
{
    MBLOCK *mblk = (MBLOCK*) param;
    hilbert(DAT->isize, DAT->xreal, DAT->ximag);
}

void au_init_param(void *param)
{
    int i;
    MBLOCK *mblk = (MBLOCK*) param;
    
    psnd_param_reset(mblk,mblk->info->block_id);
    mblk->info->dimension_id = 0;
    DAT->ityp = 1;
    
    for (i=0;i<MAX_DIM;i++) {
        DAT->access[0] = i+1;
        DAT->pars[i] = PAR0(DAT->access[i]-1);
    }
}


int au_plot_1d(void *param, char *outputfile, float left, float right, 
               float bottom, float top)
{
    MBLOCK *mblk = (MBLOCK*) param;
    if (outputfile == NULL) {
        float xmin, ymin, xmax, ymax;

        if (!mblk->info->foreground)
            return TRUE;
        if (mblk->info->plot_mode == PLOT_2D) 
            /*
             * problems with threads
            psnd_switch_plotdimension();
            */
            return FALSE;
                    
        g_get_world(psnd_get_vp_id(mblk), &xmin, &ymin, &xmax, &ymax);
        psnd_update_lastzoom(psnd_get_vp_id(mblk), xmin, ymin, xmax, ymax, DAT);
        if (left != right) {
            xmin = left;
            xmax = right;

            xmin = psnd_calc_channels(xmin, PAR->xref, 
                                   PAR->aref, PAR->bref, DAT->irc, 
                                   DAT->isspec, mblk->spar[S_AXISX_MARK].show, 
                                   DAT->isize, DAT->sw, PAR->sfd);
            xmax = psnd_calc_channels(xmax, PAR->xref, 
                                   PAR->aref, PAR->bref, DAT->irc, 
                                   DAT->isspec, mblk->spar[S_AXISX_MARK].show, 
                                   DAT->isize, DAT->sw, PAR->sfd);
            xmin = max(1,xmin);
            xmin = min(xmin,DAT->isize);
            xmax = max(1,xmax);
            xmax = min(xmax,DAT->isize);
        }
        if (xmin >= xmax)
            return FALSE;
        if (top != bottom) {
            ymax = max(top,bottom);
            ymin = min(top,bottom);
        }
                        
        psnd_push_zoomundo(psnd_get_vp_id(mblk), xmin, ymin, xmax, ymax, DAT);
        psnd_scrollbars_reconnect(mblk,psnd_get_vp_id(mblk), xmin, ymin, xmax, ymax, TRUE);
        psnd_plotaxis(mblk,PAR);
                   
        psnd_plot(mblk,TRUE,mblk->info->dimension_id);
        return TRUE;
    }
    mblk->cpq_hardcopy->par1   = DAT->pars[0];
    mblk->cpq_hardcopy->par2   = DAT->pars[1];
    mblk->cpq_hardcopy->dat    = DAT;
    if (mblk->cpq_hardcopy->is_2d) 
        if (!au_set_contour_mode(param,FALSE))
            return FALSE;

    psnd_set_plot_region(mblk->cpq_hardcopy, mblk->cpar_hardcopy, left, right, bottom, top);
    return psnd_au_paperplot_1d(mblk,outputfile);
}

int au_plot_2d(void *param, char *outputfile, float left, float right, 
               float bottom, float top)
{
    MBLOCK *mblk = (MBLOCK*) param;
    if (DAT->ityp < 2)
        return FALSE;
#ifdef bb
                        mblk->cpar_screen->ilowa  = mblk->cpar_screen->ihamin;
                        mblk->cpar_screen->ihigha = mblk->cpar_screen->ihamax;
                        mblk->cpar_screen->ilowb  = mblk->cpar_screen->ihbmin;
                        mblk->cpar_screen->ihighb = mblk->cpar_screen->ihbmax;
                        /* mark change */
                        mblk->cpar_screen->mode   = CONTOUR_PLOT;
                        g_scrollbar_disconnect(mblk->info->win_id);
                        g_scrollbar_reset(mblk->info->win_id);
                        psnd_set_cropmode(0, DAT);
                        psnd_push_zoomundo(psnd_get_vp_id(mblk), 0.0, 0.0, 1.0, 1.0, DAT);
#endif
    if (outputfile == NULL) {
        float xmin, ymin, xmax, ymax;

        if (!mblk->info->foreground)
            return TRUE;
        if (mblk->info->plot_mode != PLOT_2D) {
            /*
             * problems with threads
             *
            psnd_switch_plotdimension();
            return TRUE;
            */
            return FALSE;
        }
        if (mblk->info->phase_2d)
            psnd_set_phase2d(mblk);
        g_get_world(psnd_get_vp_id(mblk), &xmin, &ymin, &xmax, &ymax);
        psnd_update_lastzoom(psnd_get_vp_id(mblk), xmin, ymin, xmax, ymax, DAT);
        if (left != right) {
            xmin = left;
            xmax = right;

            xmin = psnd_calc_channels(xmin, PAR->xref, 
                                   PAR->aref, PAR->bref, DAT->irc, 
                                   DAT->isspec, mblk->spar[S_AXISX_MARK].show, 
                                   DAT->isize, DAT->sw, PAR->sfd);
            xmax = psnd_calc_channels(xmax, PAR->xref, 
                                   PAR->aref, PAR->bref, DAT->irc, 
                                   DAT->isspec, mblk->spar[S_AXISX_MARK].show, 
                                   DAT->isize, DAT->sw, PAR->sfd);
            xmin = max(1,xmin);
            xmin = min(xmin,DAT->isize);
            xmax = max(1,xmax);
            xmax = min(xmax,DAT->isize);
        }
        if (xmin >= xmax)
            return FALSE;
        if (top != bottom) {
            ymax = top;
            ymin = bottom;
            ymax = psnd_calc_channels(ymax, 
                                       DAT->pars[1]->xref, 
                                       DAT->pars[1]->aref,
                                       DAT->pars[1]->bref, 
                                       1,
                                       DAT->pars[1]->isspec, 
                                       mblk->spar[S_AXISY_MARK].show, 
                                       DAT->pars[1]->nsiz,
                                       DAT->pars[1]->swhold, 
                                       DAT->pars[1]->sfd);
            ymin = psnd_calc_channels(ymin,
                                       DAT->pars[1]->xref, 
                                       DAT->pars[1]->aref,
                                       DAT->pars[1]->bref, 
                                       1,
                                       DAT->pars[1]->isspec, 
                                       mblk->spar[S_AXISY_MARK].show, 
                                       DAT->pars[1]->nsiz,
                                       DAT->pars[1]->swhold, 
                                       DAT->pars[1]->sfd);
                        
            ymin = max(1,ymin);
            ymin = min(ymin,DAT->pars[1]->nsiz);
            ymax = max(1,ymax);
            ymax = min(ymax,DAT->pars[1]->nsiz);
        }
        if (ymin >= ymax)
            return FALSE;
        /*
         * Reset crop and zoom
         */
        mblk->cpar_screen->ilowa  = mblk->cpar_screen->ihamin;
        mblk->cpar_screen->ihigha = mblk->cpar_screen->ihamax;
        mblk->cpar_screen->ilowb  = mblk->cpar_screen->ihbmin;
        mblk->cpar_screen->ihighb = mblk->cpar_screen->ihbmax;
        mblk->cpar_screen->mode   = CONTOUR_PLOT;
        g_scrollbar_disconnect(mblk->info->win_id);
        g_scrollbar_reset(mblk->info->win_id);
        psnd_set_cropmode(0, DAT);
        psnd_push_zoomundo(psnd_get_vp_id(mblk), 0.0, 0.0, 1.0, 1.0, DAT);
       
#ifdef notgood

        /*
         * If outside Crop limits: re-crop
         */
        if (mblk->cpar_screen->ilowa > xmin ||
            mblk->cpar_screen->ihigha < xmax ||
            mblk->cpar_screen->ilowb > ymin ||
            mblk->cpar_screen->ihighb < ymax) {

                mblk->cpar_screen->ilowa  = min(mblk->cpar_screen->ilowa,xmin);
                mblk->cpar_screen->ihigha = max(mblk->cpar_screen->ihigha,xmax);
                mblk->cpar_screen->ilowb  = min(mblk->cpar_screen->ilowb,ymin);
                mblk->cpar_screen->ihighb = max(mblk->cpar_screen->ihighb,ymax);

                psnd_set_cropmode(1, DAT);
        }
        /*
         * else Zoom
         */
        else {
            psnd_push_zoomundo(psnd_get_vp_id(mblk), xmin, ymin, xmax, ymax,DAT);
            psnd_scrollbars_reconnect(mblk,psnd_get_vp_id(mblk), xmin, ymin, xmax, ymax, TRUE);
            psnd_plotaxis(mblk,DAT->pars[0]);

            psnd_contour_mode(mblk, FALSE, mblk->cpar_screen);
            psnd_contour_mode(mblk, TRUE, mblk->cpar_screen);
            mblk->info->dimension_id = DAT->access[0] - 1;
        }
        
#endif
        psnd_cp(mblk, CONTOUR_BLOCK);
        return TRUE;
    }
    mblk->cpq_hardcopy->par1   = DAT->pars[0];
    mblk->cpq_hardcopy->par2   = DAT->pars[1];
    mblk->cpq_hardcopy->dat    = DAT;
    if (!mblk->cpq_hardcopy->is_2d) 
        if (!au_set_contour_mode(param,TRUE))
            return FALSE;

    psnd_set_plot_region(mblk->cpq_hardcopy, mblk->cpar_hardcopy, 
                          left, right, bottom, top);
    if (mblk->cpq_hardcopy->xminv >= mblk->cpq_hardcopy->xmaxv ||
            mblk->cpq_hardcopy->yminv >= mblk->cpq_hardcopy->ymaxv)
        return FALSE;
    return psnd_au_paperplot_2d(mblk, outputfile);
}

int au_set_contour_mode(void *param, int set2d)
{
    int doit = (set2d != 0);
    int oldmode;

    MBLOCK *mblk = (MBLOCK*) param;
    if (DAT->ityp <= 1) {
        if (doit)
            return FALSE;
        mblk->cpq_hardcopy->is_2d  = FALSE;
        return TRUE;
    }
    if (doit == mblk->cpq_hardcopy->is_2d)
        return TRUE;
    mblk->cpq_hardcopy->is_2d  = doit;
    oldmode = mblk->info->plot_mode;
    psnd_contour_mode(mblk, doit, mblk->cpar_hardcopy);
    if (doit)
        mblk->info->dimension_id = DAT->access[0] - 1;
    mblk->info->plot_mode = oldmode;
    return TRUE;
}

int au_write_plot_parameters(void *param, char *outputfile)
{
    MBLOCK *mblk = (MBLOCK*) param;
    mblk->cpq_hardcopy->par1   = DAT->pars[0];
    mblk->cpq_hardcopy->par2   = DAT->pars[1];
    mblk->cpq_hardcopy->dat    = DAT;

    return psnd_write_plot_parameters(mblk->cpq_hardcopy, mblk->cpar_hardcopy, outputfile);
}

int au_read_plot_parameters(void *param, char *inputfile)
{
    MBLOCK *mblk = (MBLOCK*) param;
    mblk->cpq_hardcopy->par1   = DAT->pars[0];
    mblk->cpq_hardcopy->par2   = DAT->pars[1];
    mblk->cpq_hardcopy->dat    = DAT;
    return psnd_read_plot_parameters(mblk->cpq_hardcopy, mblk->cpar_hardcopy, inputfile);
}

int au_set_plot_parameters(void *param, char *inputstring)
{
    MBLOCK *mblk = (MBLOCK*) param;
    mblk->cpq_hardcopy->par1   = DAT->pars[0];
    mblk->cpq_hardcopy->par2   = DAT->pars[1];
    mblk->cpq_hardcopy->dat    = DAT;

    return psnd_set_plot_parameters(mblk->cpq_hardcopy, mblk->cpar_hardcopy, inputstring);
}

int au_get_plotmode(void *param)
{
    MBLOCK *mblk = (MBLOCK*) param;
    return (mblk->info->plot_mode == PLOT_2D);
}

void au_integral_set_offset_correction(void *param, float doit)
{
    MBLOCK *mblk = (MBLOCK*) param;
    mblk->cpar_screen->zoint = (((int) doit) != 0);
}

float au_integrate_1d(void *param, float x1, float x2)
{
    int i;
    int i1 = round(x1);
    int i2 = round(x2);
    int lim1 = min(i1, i2);
    int lim2 = max(i1, i2);    
    float sum = 0.0;
    MBLOCK *mblk = (MBLOCK*) param;
    
    lim1 = max(1, lim1);
    lim2 = max(1, lim2);
    lim1 = min(DAT->isize, lim1);
    lim2 = min(DAT->isize, lim2);
    
    for (i=lim1-1;i<lim2;i++) 
        sum += DAT->xreal[i];
    return sum;
}

float au_integrate_2d(void *param, float x1, float x2, float y1, float y2, int *ierr)
{
    int ja1,ja2,jb1,jb2,jxc,jyc;
    float xx1,xx2,yy1,yy2;
    float sum=0.0, dum, dumlevel = 0.0, *xx;
    MBLOCK *mblk = (MBLOCK*) param;
    
    if (DAT->finfo[INFILE].fopen == FALSE || DAT->ityp < 2) {
        *ierr = TRUE;
        return sum;
    }
    *ierr = FALSE;
    xx1=min(x1,x2);
    xx2=max(x1,x2);
    yy1=min(y1,y2);
    yy2=max(y1,y2);

    ja1=round(yy1);
    ja2=round(yy2);
    jb1=round(xx1);
    jb2=round(xx2);

    ja1=max(ja1,1);
    ja2=min(ja2,DAT->pars[1]->nsiz);
    jb1=max(jb1,1);
    jb2=min(jb2,DAT->pars[0]->nsiz);

    jxc = jb1 + (jb2 - jb1)/2;
    jyc = ja1 + (ja2 - ja1)/2;
    xx = fvector(0, jb2);

    integrate2d(DAT->finfo[INFILE].ifile,
                xx,
                TRUE,
                ja1,
                ja2,
                jb1,
                jb2,
                &jyc,
                &jxc,
                1,
                mblk->cpar_screen->zoint,
                &dum,
                &dumlevel,
                &sum,
                FALSE,
                NULL,
                read2d,
                (void*)mblk);
    free_fvector(xx,0);
    return sum;

}


int au_integrate_read(void *param, char *filename)
{
    MBLOCK *mblk = (MBLOCK*) param;

    if (DAT->finfo[INFILE].fopen == FALSE || DAT->ityp < 2) {
        return FALSE;
    }
        
    psnd_contour_mode(mblk, 1, mblk->cpar_screen);
    return psnd_integrals_read(mblk, mblk->cpar_screen, filename);
}


int au_integrate_2d_get_numpeaks(void *param)
{
    MBLOCK *mblk = (MBLOCK*) param;

    if (DAT->finfo[INFILE].fopen == FALSE || DAT->ityp < 2) {
        return 0;
    }
    if (mblk->info->plot_mode != PLOT_2D)
        return 0;
    return psnd_integrals_get_numpeaks(mblk, mblk->cpar_screen);
}

float au_integrate_2d_peak(void *param, int peak_id)
{
    MBLOCK *mblk = (MBLOCK*) param;

    if (DAT->finfo[INFILE].fopen == FALSE || DAT->ityp < 2) {
        return 0.0;
    }
    if (mblk->info->plot_mode != PLOT_2D)
        return 0.0;
    return psnd_integral_calc(mblk, mblk->cpar_screen, peak_id);
}

float au_rmsnoise_1d(void *param, float x1, float x2)
{
    float noise = 0.0;
    int ja1, ja2, n;
    MBLOCK *mblk = (MBLOCK*) param;

    if (x2 < x1) {
        ja1=round(x2);
        ja2=round(x1);
    }
    else {
        ja1=round(x1);
        ja2=round(x2);
    }
    ja1=max(1, ja1);
    ja2=min(DAT->isize,ja2);
    n = ja2-ja1+1;

    if (n >= 2)
         noise = rmsnoi(DAT->xreal+ja1-1,n);
    return noise;
}

float au_rmsnoise_2d(void *param, float x1, float x2, float y1, float y2, int *ierr)
{
    int ja1,ja2,jb1,jb2;
    float xx1,xx2,yy1,yy2;
    float avq, rms=0.0, *xx;
    MBLOCK *mblk = (MBLOCK*) param;
    
    if (DAT->finfo[INFILE].fopen == FALSE || DAT->ityp < 2) {
        *ierr = TRUE;
        return rms;
    }
    *ierr = FALSE;
    xx1=min(x1,x2);
    xx2=max(x1,x2);
    yy1=min(y1,y2);
    yy2=max(y1,y2);

    ja1=round(yy1);
    ja2=round(yy2);
    jb1=round(xx1);
    jb2=round(xx2);

    ja1=max(ja1,1);
    ja2=min(ja2,DAT->pars[1]->nsiz);
    jb1=max(jb1,1);
    jb2=min(jb2,DAT->pars[0]->nsiz);

    xx = fvector(0, jb2);
    rmsn2d(DAT->finfo[INFILE].ifile,
           xx,
           ja1,
           ja2,
           jb1,
           jb2,
           &avq,
           &rms,
           read2d,
           (void*)mblk);
    free_fvector(xx,0);
    return rms;
}

int au_peakpick_1d(void *param, float x1, float x2, float y1, float y2, float *data,
                   int istart, int istop)
{
    int i, count = 0;
    int ja1, ja2, n;
    float yy1, yy2;
    MBLOCK *mblk = (MBLOCK*) param;

    if (x2 < x1) {
        ja1=round(x2);
        ja2=round(x1);
    }
    else {
        ja1=round(x1);
        ja2=round(x2);
    }
    ja1=max(1, ja1);
    ja2=min(DAT->isize,ja2);
    n = ja2-ja1+1;
    if (y2 == 0.0)
        y2 = 1e20;
    yy1 = min(y1,y2);
    yy2 = max(y1,y2);

    if (n >= 2) {
        int isize   = istop - istart + 1;
        int *ipeaks = ivector(0,isize);
        count = npeaks1d(DAT->xreal, ja1, ja2, yy1, yy2, isize, ipeaks);
        data += istart - 1;
        for (i=0;i<count;i++)
            data[i] = ipeaks[i];
        free_ivector(ipeaks, 0);
    }
    return count;
}

float au_unitconvert(void *param, float xx, float direction, char *what)
{
    float sfd,sw,xref,aref,bref;
    int   isize,irc,dir;
    MBLOCK *mblk = (MBLOCK*) param;

    dir = round(direction);
    if (dir < 1) {
        sfd 	= PAR->sfd;
        sw  	= DAT->sw;
        isize 	= DAT->isize;
        xref 	= PAR->xref;
        aref 	= PAR->aref;
        bref 	= PAR->bref;
        irc 	= DAT->irc;
    }
    else if (dir > 0 && dir <= MAX_DIM) {
        sfd 	= DAT->par0[dir-1]->sfd;
        sw  	= DAT->par0[dir-1]->swhold;
        isize 	= DAT->par0[dir-1]->nsiz;
        xref 	= DAT->par0[dir-1]->xref;
        aref 	= DAT->par0[dir-1]->aref;
        bref 	= DAT->par0[dir-1]->bref;
        irc 	= 1;
    }
    else
        return xx;
    switch (what[0]) {
        case 'c':
        case 'C':
          switch (what[1]) {
            case 'p':
            case 'P':
               xx = psnd_chan2ppm(xx, sfd, sw, isize, xref, aref, bref, irc);
               break;
            case 'h':
            case 'H':
               xx = psnd_chan2hz(xx, sw, isize, xref, aref, bref, irc);
               break;
            case 's':
            case 'S':
               xx = psnd_chan2sec(xx, sw);
               break;
          }
          break;
        case 'p':
        case 'P':
          switch (what[1]) {
            case 'c':
            case 'C':
               xx = psnd_ppm2chan(xx, sfd, sw, isize, xref, aref, bref, irc);
               break;
            case 'h':
            case 'H':
               xx *= sfd;
               break;
            case 's':
            case 'S':
               xx = psnd_ppm2chan(xx, sfd, sw, isize, xref, aref, bref, irc);
               xx = psnd_chan2sec(xx, sw);
               break;
          }
          break;
        case 'h':
        case 'H':
          switch (what[1]) {
            case 'c':
            case 'C':
               xx = psnd_hz2chan(xx, sw, isize, xref, aref, bref, irc);
               break;
            case 'p':
            case 'P':
               if (sfd != 0.0)
                   xx /= sfd;
               break;
            case 's':
            case 'S':
               xx = psnd_hz2chan(xx, sw, isize, xref, aref, bref, irc);
               xx = psnd_chan2sec(xx, sw);
               break;
          }
          break;
        case 's':
        case 'S':
          switch (what[1]) {
            case 'c':
            case 'C':
               xx = psnd_sec2chan(xx, sw);
               break;
            case 'p':
            case 'P':
               xx = psnd_sec2chan(xx, sw);
               xx = psnd_chan2ppm(xx, sfd, sw, isize, xref, aref, bref, irc);
               break;
            case 'h':
            case 'H':
               xx = psnd_sec2chan(xx, sw);
               xx = psnd_chan2hz(xx, sw, isize, xref, aref, bref, irc);
               break;
          }
          break;
    }
    return xx;

}

int au_3to4(float *in, float *out, int start_in, int stop_in, 
               int start_out, int stop_out, int mode)
{
    return unpack3to4((int *)in, (int *)out, start_in, stop_in, 
                      start_out, stop_out, mode);
}

void au_negstep(float *data, int start, int stop, int stepsize, int index)
{
    int size = stop - start + 1;
    data += start-1;
    negstep(data, size, stepsize, index);
}


int au_unpack_byte(float *f, int index)
{
    int *i;
    i = (int*) f;
    return unpack_byte(*i, index);
}

int au_unpack_int(float *f)
{
    int *i;
    
    i = (int*) f;
    return unpacki(*i);
}

float au_unpack_float(float *f1, float *f2)
{
    int *i1, *i2;
    
    i1 = (int*) f1;
    i2 = (int*) f2;
    return unpackf(*i1, *i2);
}

char *au_unpack8(float *f)
{
    int *i;
    
    i = (int*) f;
    return unpack8(*i);
}

char *au_unpack6(float *f)
{
    int *i;
    
    i = (int*) f;
    return unpack6(*i);
}

char *au_unpack4(float *f)
{
    int *i;
    
    i = (int*) f;
    return unpack4(*i);
}


int au_puts(void *param, char *buf)
{
    return psnd_puts((MBLOCK *)param, buf);
}



/********************************************************************/





/*
 * Run 'au' command
 * Do oldstyle, parameter-based au 
 */
void psnd_au(MBLOCK *mblk)
{
    CBLOCK *cpar = mblk->cpar_screen;
    PBLOCK *par  = PAR;
    DBLOCK *dat  = DAT;
    /*
     *   wm;ft;pk;bc for one record
     */
     
    /*
     *   Linear prediction
     */
    if (par->nlpc) {
        int newsize;
        if (par->lpcmode == PREDICT_FORWARD)
            psnd_resize_arrays(mblk, 0, par->nfut, 
                                NULL, dat);
        newsize = psnd_linpr(mblk,
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
        if (newsize)
            psnd_set_datasize(mblk, newsize, TRUE, dat);
    }

    /*
     *   do wm
     */
    if (par->nwindo) 
        psnd_do_window(mblk, TRUE, FALSE);
    /*
     *   WatWa
     */
    if (par->watwa) 
        psnd_do_watwa(par, dat);
    /*
     *   do ft
     */
    if (par->ifft != NOFFT && par->nfft)
        psnd_do_ft(mblk,dat->isize, par, dat);
    /*
     *   Hilbert transformation
     */
    if (par->hilbert)
        hilbert(dat->isize, dat->xreal, dat->ximag);

    /*
     *   do phase
     */
    if (par->iphase == DOPHA)
        psnd_pk(mblk, par,dat);
    else if (par->iphase == PSPHA)
        psnd_arraypower(dat);
    else if (par->iphase == AVPHA)
        psnd_arrayaverage(dat);
    /*
     *   do reverse
     */
    if (par->irever == REVARR ||
        par->irever == REVBOT) 
            psnd_arrayreverse(par,dat);
    /*
     *   do waterfit
     */
    if (par->waterfit)
        psnd_fit_waterline(mblk, 0, NULL, TRUE);

    /*
     *   do base
     */
    if (par->nbase)
        psnd_baseline(mblk, TRUE, FALSE, cpar);


}

/********************************************************
 * AU script stuff
 */
 
static char  *au_script = NULL;
static char  *au_args = NULL;
static char  *au_args_store = NULL;
static int    au_argcount_save;
static char **au_argval_save, *au_argval_buf;

/*
 * return the current script
 * (for history record)
 */
char *psnd_get_script()
{
    return au_script;
}

/*
 * return the size of the current script
 * (for history record)
 */
int psnd_get_script_size()
{
    if (au_script == NULL)
        return 0;
    return strlen(au_script)+1;
}

/* size of text before script arguments + extra \n\n */
#define LEADER_LEN	15
/*
 * return the arguments to the current script
 * (for history record)
 */
char *psnd_get_scriptarg()
{
    int i, arglen;

    for (i=0,arglen=0;i<au_argcount_save;i++)
        arglen += strlen(au_argval_save[i]) + 1;
    au_argval_buf = (char*) realloc(au_argval_buf, arglen+1+LEADER_LEN);
    strcpy(au_argval_buf,"# Arguments: ");
    for (i=0;i<au_argcount_save;i++) {
        if (i>0)
            strcat(au_argval_buf," ");
        strcat(au_argval_buf,au_argval_save[i]);
    }
    strcat(au_argval_buf,"\n\n");
    return au_argval_buf;
}

/*
 * return the size of arguments to the current script
 * (for history record)
 */
int psnd_get_scriptarg_size()
{
    int i, arglen;

    if (au_script == NULL)
        return 0;
    for (i=0,arglen=0;i<au_argcount_save;i++)
        arglen += strlen(au_argval_save[i]) + 1;
    return arglen+1+LEADER_LEN;
}



#ifdef USE_THREADS

#include <pthread.h>



static void run_script_in_thread(MBLOCK *mblk, char *script, 
                 int argc, char *argv[], int id)
{
    mblk->cpq_hardcopy->is_2d  = FALSE;
    au_argcount_save = argc;
    au_argval_save   = argv;
    run_au_script((void*)mblk, script, argc, argv);
    au_set_contour_mode((void*)mblk, FALSE);

    g_popup_container_close(id);
}

static const int success = 1;
static void *run_cancelbox_in_thread(void *msg)
{
    int  *cancel_cont_id;
    cancel_cont_id = (int*) msg;
    if (g_popup_container_show(*cancel_cont_id)) {
        yyabort_by_user = TRUE;
    }
    return ((void*) &success);
}

static void run_au_background(MBLOCK *mblk, char *script, int argc, char *argv[])
{
    int store;
    
    if (mblk->info->foreground) {
        psnd_push_waitcursor(mblk);
        g_peek_event();
        g_peek_event();
    }
    store = mblk->info->verbose;
    mblk->info->verbose = FALSE;
    mblk->cpq_hardcopy->is_2d  = FALSE;
    au_argcount_save = argc;
    au_argval_save   = argv;
    yyabort_by_user  = FALSE;
    run_au_script((void*)mblk, script, argc, argv);
    au_set_contour_mode((void*)mblk,FALSE);
    if (mblk->info->foreground) 
        psnd_pop_waitcursor(mblk);
    mblk->info->verbose = store;
}

static void abortcallback(G_POPUP_CHILDINFO *ci)
{
    yyabort_by_user = TRUE;
}

static void run_au(MBLOCK *mblk, char *script, int argc, char *argv[])
{
    int store;
    pthread_t thr;
    int cancel_cont_id=0;
    G_POPUP_CHILDINFO ci[20];
    int id=0;

    if (!mblk->info->foreground) {
        run_au_background(mblk, script, argc, argv);
        return;
    }
    store = mblk->info->verbose;
    mblk->info->verbose = FALSE;
    psnd_push_waitcursor(mblk);
    g_peek_event();
    g_peek_event();

    cancel_cont_id = g_popup_container_open(mblk->info->win_id, "Cancel box", 
                            G_POPUP_WAIT | G_POPUP_SINGLEBUTTON );

    g_popup_set_buttonlabel(cancel_cont_id, "Cancel", G_POPUP_BUTTON_OK);
    g_popup_init_info(&(ci[id]));
    ci[id].type  = G_CHILD_LABEL;
    ci[id].id    = 0;
    ci[id].label = "Press \'Cancel\' to abort current run";
    g_popup_add_child(cancel_cont_id, &(ci[id]));
    id++;
    g_popup_init_info(&(ci[id]));
    ci[id].type  = G_CHILD_OK;
    ci[id].func  = abortcallback; 
    ci[id].data  = (void*) mblk; 
    g_popup_add_child(cancel_cont_id, &(ci[id]));
    yyabort_by_user     = FALSE;
    g_peek_event();
    g_peek_event();

    pthread_create(&thr, NULL, run_cancelbox_in_thread, 
                   (void*)&cancel_cont_id);

    run_script_in_thread(mblk, script, argc, argv, cancel_cont_id);

    psnd_pop_waitcursor(mblk);
    mblk->info->verbose = store;
}

#else

static void run_au(MBLOCK *mblk, char *script, int argc, char *argv[])
{
    int store;
    
    if (mblk->info->foreground) {
        psnd_push_waitcursor(mblk);
        g_peek_event();
        g_peek_event();
    }
    store = mblk->info->verbose;
    mblk->info->verbose = FALSE;
/*
    au_par0 = mblk->par;
    au_dat0 = mblk->dat;
    au_par  = mblk->par[mblk->info->block_id]+mblk->info->dimension_id;
    au_dat  = mblk->dat[mblk->info->block_id];    
*/
    mblk->cpq_hardcopy->is_2d  = FALSE;
    au_argcount_save = argc;
    au_argval_save   = argv;
    yyabort_by_user  = FALSE;
    run_au_script((void*)mblk, script, argc, argv);
    au_set_contour_mode((void*)mblk,FALSE);
    if (mblk->info->foreground) 
        psnd_pop_waitcursor(mblk);
    mblk->info->verbose = store;
}

#endif

int psnd_run_au_from_commandline(MBLOCK *mblk, char *command, 
                                 int argc, char *argv[])
{
    if (command == NULL)    
        run_au(mblk, au_script, argc, argv);
    else
        run_au(mblk, command, argc, argv);
    return TRUE;
}

int psnd_au_script(MBLOCK *mblk)
{
    if (au_script == NULL)
        return FALSE;
    run_au(mblk, au_script, 0, NULL);
    return TRUE;
}


/*
 * Create default script
 */
static char *scriptbuf;
static int scriptbuf_size,scriptbuf_max;
static void defscript_printf(const char *format, ...)
{
    va_list argp;
    int len;
    char str[PSND_STRLEN+1];

    va_start(argp, format);
    vsprintf(str, format, argp);
    va_end(argp);

#ifdef _WIN32
    towin(str);
#endif
    len = strlen(str)+1;
    if (scriptbuf_size + len >= scriptbuf_max) {
        scriptbuf_max += len;
        scriptbuf = (char*) realloc(scriptbuf, scriptbuf_max * sizeof(char));
    }
    scriptbuf_size += len;
    strcat(scriptbuf, str);
    /*strcat(scriptbuf, "\n");*/
}

int psnd_guess_storemode(PBLOCK *par)
{
    int icmplx = FALSE;
    int ipost = par->ipost;
    /*
     * determine here complex or real store mode
     */
    if (ipost == NOPST) {
        /*
         * We start with complex or real data
         */
        if (par->ipre == PREBOT)
            icmplx = TRUE;
        else
            icmplx = FALSE;
        /*
         * After FT we have complex data
         */
        if (par->nfft && par->ifft != NOFFT) 
            icmplx = TRUE;
        /*
         * After phase correction, we do not need complex data
         */
        if (par->iphase != NOPHA) {
            /*
             * But check if FT has been performed
             */
            if (par->isspec || (par->nfft && par->ifft != NOFFT))
                icmplx = FALSE;
        }
        if (icmplx)
            ipost = PSTBOT;
        else
            ipost = PSTREA;
    }
    return ipost;
}

static char *create_default_script(MBLOCK *mblk,char *outfile, DBLOCK *dat, PBLOCK *par)
{
    int i,doit,isize,access[MAX_DIM],ipost;

    isize = dat->isize;
    scriptbuf_max  = 1024;
    scriptbuf_size = 0;
    scriptbuf = (char*) malloc(scriptbuf_max * sizeof(char));
    assert(scriptbuf);
    scriptbuf[0] = '\0';
    

    defscript_printf("# Script generated by prospectnd\n");

#ifdef _WIN32
    defscript_printf("%%file1 = \"%s\"\n", 
                    towinfilename(dat->finfo[INFILE].name));
    defscript_printf("%%file2 = \"%s\"\n",
                    towinfilename(outfile));
#else
    defscript_printf("%%file1 = \"%s\"\n", dat->finfo[INFILE].name);
    defscript_printf("%%file2 = \"%s\"\n", outfile);
#endif
    defscript_printf("# Open input file\n");
    doit = FALSE;

    defscript_printf("ropen %%file1\n");
    if (par->irstrt > 1 || 
              (par->irstop != 0 && par->irstop != par->nsiz)) {
        isize = par->irstop - par->irstrt + 1;
        doit = TRUE;
    }

    defscript_printf("# Set processing direction\n");
    for (i=0;i<MAX_DIM;i++)
        access[i] = dat->access[i];
    if (dat->access[0] != par->icrdir) {
        for (i=1;i<MAX_DIM;i++) {
            if (access[i] == par->icrdir) {
                access[i] = access[0];
                access[0] = par->icrdir;
                break;
            }
        }
    }
    defscript_printf("dr %d", access[0]);
    for (i=1;i<dat->ityp;i++)
        defscript_printf(", %d", access[i]);
    defscript_printf("\n");

    if (par->ipre == PREIMA) {
        defscript_printf("# Set read mode to real/imag-swapped complex\n");
        defscript_printf("rm 3\n");
    }
    /*
     * determine here complex or real store mode
     */
    ipost = psnd_guess_storemode(par);
    if (ipost == PSTBOT) {
        defscript_printf("# Set store mode to complex\n");
        defscript_printf("sm 2\n");
    }
    else if (ipost == PSTREA) {
        defscript_printf("# Set store mode to real\n");
        defscript_printf("sm 1\n");
    }
    if (dat->finfo[OUTFILE].sizeof_float != 4 ||
        dat->finfo[OUTFILE].tresh_flag) {
        defscript_printf("# Set output file truncation\n");
        defscript_printf("otrunc");
        if (dat->finfo[OUTFILE].sizeof_float != 4)
            defscript_printf(" float%d", dat->finfo[OUTFILE].sizeof_float);
        if (dat->finfo[OUTFILE].sizeof_float != 4 &&
            dat->finfo[OUTFILE].tresh_flag)
                defscript_printf(", ");
        if (dat->finfo[OUTFILE].tresh_flag)
            defscript_printf(" %g, %g, %g", 
                dat->finfo[OUTFILE].tresh_levels[0], 
                dat->finfo[OUTFILE].tresh_levels[1],
                dat->finfo[OUTFILE].tresh_levels[2]); 
        defscript_printf("\n");
    }
    if (par->nfft && par->ifft != NOFFT) {
        if (par->ftscale != 0.0 && par->ftscale != 1.0) {
            defscript_printf("# Scale first data point for ft processing\n");
            defscript_printf("ftscale %.2f\n", par->ftscale);
        }
    }
    defscript_printf("# Open output file\n");
    for (i=0;i<dat->ityp && !doit;i++) 
        if (dat->par0[i]->isstrt > 1 || dat->par0[i]->isstop != 0) 
            doit = TRUE;
    defscript_printf("wopen %%file2");
    if (doit) {
        for (i=0;i<dat->ityp;i++) {
            int isstrt, isstop, nsize;
            if (par->icrdir == dat->par0[i]->icrdir)
                nsize = isize;
            else
                nsize = dat->par0[i]->nsiz;
            isstrt = max(1, dat->par0[i]->isstrt);
            isstop = min(nsize, dat->par0[i]->isstop);
            if (isstop == 0)
                isstop = nsize;
            if (isstrt > 1) {
                isstop -= isstrt-1;
                isstrt = 1;
            }
            defscript_printf(", [%d..%d]", isstrt, isstop);
        }
    }
    defscript_printf("\n");

    if (par->nbase) {
        if (par->ibase == BASSP1 || 
                par->ibase == BASSP2) {
            char *filename = 
                psnd_getfilename(mblk,"Read file with Spline points", "*.spn");
            if (filename != NULL) {
                defscript_printf("# Read Spline controll points\n");
#ifdef _WIN32
                defscript_printf("spline read, \"%s\"\n", 
                    towinfilename(filename));
#else
                defscript_printf("spline read, \"%s\"\n", filename);
#endif
            }
        }
        if (par->ibase == BASPL2 || 
            par->ibase == BASTB2 || 
            par->ibase == BASSN2 || 
            par->ibase == BASCS2 || 
            par->ibase == BASSC2 || 
            par->ibase == BASSP2 || 
            par->ibase == BASAS2) {
            char *filename = 
                psnd_getfilename(mblk,"Read Region file in Buffer A", "*");
            if (filename != NULL) {
                defscript_printf("# Read Region file in Buffer A\n");
#ifdef _WIN32
                defscript_printf("rd @a, \"%s\"\n", 
                    towinfilename(filename));
#else
                defscript_printf("rd @a, \"%s\"\n", filename);
#endif
            }
        }
        
    }
    if (par->nwindo) {
        int id = psnd_lookup_window_mode(par->iwindo);
        
        if (id == PSND_WB) {
            char *filename = 
                psnd_getfilename(mblk,"Read Window Buffer", "*");
            if (filename != NULL) {
                defscript_printf("# Read Window Buffer\n");
#ifdef _WIN32
                defscript_printf("rd @w, \"%s\"\n", 
                    towinfilename(filename));
#else
                defscript_printf("rd @w, \"%s\"\n", filename);
#endif
            }
        }
        
    }
    if (dat->ityp > 1) {
/*
        int itotal = 1;
        for (i=1;i<dat->ityp;i++)
            itotal *= dat->pars[i]->nsiz * (dat->pars[i]->icmplx+1);
        defscript_printf("# Setup progress counter ...\n");

        defscript_printf("$itotal=%d\n",itotal);
        defscript_printf("$icount=0\n");
        defscript_printf("$percent=$itotal/100\n");
        defscript_printf("$last=0\n");
        defscript_printf("$progress=0\n");
*/
        defscript_printf("# For all records do ...\n");
        defscript_printf("for $i");
        for (i=2;i<dat->ityp;i++)
            defscript_printf(", $i%d", i);
        
        if (dat->pars[1]->isstrt > 1) {
            int isstrt, isstop;
            isstrt = max(1, dat->pars[1]->isstrt);
            isstop = min(dat->pars[1]->nsiz, dat->pars[1]->isstop);
            if (isstop == 0)
                isstop = dat->pars[1]->nsiz;

            defscript_printf(" in sorted [%d..%d]",
                                   isstrt, isstop);
        }
        else
            defscript_printf(" in sorted [..]");
        for (i=2;i<dat->ityp;i++) {
            if (dat->pars[i]->isstrt > 1) {
                int isstrt, isstop;
                isstrt = max(1, dat->pars[i]->isstrt);
                isstop = min(dat->pars[i]->nsiz, dat->pars[i]->isstop);
                if (isstop == 0)
                    isstop = dat->pars[i]->nsiz;

                defscript_printf(", [%d..%d]",
                                   isstrt, isstop);
            }
            else
                defscript_printf(", [..]");
        }
        defscript_printf(" do\n");
    }
    if (dat->ityp > 1) {
  
        defscript_printf("    print \"\\rProcessing record \" + %%i");
        for (i=2;i<dat->ityp;i++)
            defscript_printf(" + \", \" + %%i%d", i);
        defscript_printf("\n");
/*
        defscript_printf("# Progress counter\n");
        defscript_printf("    $icount += 1\n");
        defscript_printf("    if $icount >= $last then\n");
        defscript_printf("        print \"\\rProgress:  \"+%%progress+\"\%  (\"+%%icount+\"/\"+%%itotal+\")  \"\n");
        defscript_printf("        $progress += 1\n");
        defscript_printf("        $last += $percent\n");
        defscript_printf("    endif\n");
*/
    }
    else
        defscript_printf("    println \"Processing record\"\n");
    defscript_printf("# Read record\n");
    if (dat->ityp > 1) {
        defscript_printf("    rn $i");
        for (i=2;i<dat->ityp;i++)
            defscript_printf(", $i%d", i);
    }
    else
        defscript_printf("    rn");
    if (doit)
        defscript_printf(", [%d..%d]\n", par->irstrt, par->irstop);
    else
        defscript_printf("\n");
    if (par->nlpc) {
        char *lpctype, *lpctypes[] = { "lpc", "hsvd" };
        char lpcmode[4] = "  ";
        int m=0;
        
        if (par->lpc == PREDICT_LPC)
            lpctype = lpctypes[0];
        else
            lpctype = lpctypes[1];
        
        if (par->lpcmode == PREDICT_FORWARD)
            lpcmode[m++] = 'F';
        else if (par->lpcmode == PREDICT_BACKWARD)
            lpcmode[m++] = 'B';
        else if (par->lpcmode == PREDICT_GAP)
            lpcmode[m++] = 'G';

        if (par->mroot)
            lpcmode[m++] = 'M';

        if (par->replace)
            lpcmode[m++] = 'R';

        lpcmode[m] = '\0'; 

        defscript_printf("# Linear Prediction\n");
        if (par->lpcmode == PREDICT_FORWARD)
            defscript_printf("    %s \"%s\", %d, %d",
                          lpctype, lpcmode, par->npoles, par->nfut);
        else if (par->lpcmode == PREDICT_BACKWARD)
            defscript_printf("    %s \"%s\", %d, %d",
                          lpctype, lpcmode, par->npoles, par->nfut);
        else if (par->lpcmode == PREDICT_GAP)
            defscript_printf("    %s \"%s\", %d, [%d..%d]",
                          lpctype, lpcmode, par->npoles,  par->ngap1, par->ngap2);
        if (par->nstart > 1 || par->nstop != 0)
            defscript_printf(", [%d..%d]\n", par->nstart, par->nstop);
        else
            defscript_printf("\n");
    }
    if (par->nwindo) {
        int id = psnd_lookup_window_mode(par->iwindo);
        if (id != PSND_WN) {
            defscript_printf("# Window\n");
            switch (id) {
                case PSND_EM:
                    defscript_printf("    em %.2f", par->rlb);
                    break;
                case PSND_CD:
                    defscript_printf("    cd %.2f", par->rlb);
                    break;
                case PSND_SN:
                    defscript_printf("    sn %.2f", par->rsh);
                    break;
                case PSND_SQ:
                    defscript_printf("    sq %.2f", par->rsh);
                    break;
                case PSND_KS:
                    defscript_printf("    ks %.2f", par->rsh);
                    break;
                case PSND_WN:
                    defscript_printf("    wn");
                    break;
                case PSND_HM:
                    defscript_printf("    hm");
                    break;
                case PSND_HN:
                    defscript_printf("    hn");
                    break;
                case PSND_WB:
                    defscript_printf("    wb");
                    break;
                case PSND_GM:
                    defscript_printf("    gm %.2f, %.2f",par->rlb,par->gn);
                    break;
                case PSND_TM:
                    defscript_printf("    tm %d",par->itm);
                    break;
            }
            if (par->iwstrt > 1 || par->iwstop != 0)
                defscript_printf(", [%d..%d]\n", par->iwstrt, par->iwstop);
            else
                defscript_printf("\n");
        }
    }
    if (par->watwa) {
        defscript_printf("# WatWa\n");
        defscript_printf("    watwa %d, %g, %g\n",
                          par->iopt, par->kc, par->wshift);
    }
    if (par->nfft && par->ifft != NOFFT) {
        defscript_printf("# Fourier transformation\n");
        if (par->ifft == FFTREA ||
            par->ifft == FFTIMA ||
            par->ifft == FFTCX)
            defscript_printf("    ft %d\n", par->nzf);
        else if (par->ifft == DFTREA ||
                 par->ifft == DFTIMA ||
                 par->ifft == DFTCX)
            defscript_printf("    dft\n");
    }
    if (par->hilbert) {
        defscript_printf("# Hilbert transformation\n");
        defscript_printf("    ht\n");
    }
    if (par->iphase) {
        if (par->iphase == DOPHA) {
            /*
             * Normalize phase
             */
            psnd_set_phase_position(mblk, par, dat, 1);
            defscript_printf("# Phase correction\n");
            defscript_printf("    pk %.1f, %.1f, %d\n", 
                              par->ahold, par->bhold, par->ihold);
        }
        else if (par->iphase == PSPHA) {
            defscript_printf("# Power spectrum\n");
            defscript_printf("    ps\n"); 
        }
        else if (par->iphase == AVPHA) {
            defscript_printf("# Absolute value spectrum\n");
            defscript_printf("    av\n");
        }
    }
    if (par->irever) {
        defscript_printf("# Reverse\n");
        switch (par->irever) {
            case REVARR:
            /*
                defscript_printf("    rv @r\n");
                break;
            */
            case REVBOT:
                defscript_printf("    rv @c\n");
                break;
        }
    }
    if (par->waterfit) {
        defscript_printf("# Fit and remove waterline\n");
        defscript_printf("    waterfit %d, %d\n", 
                              par->waterpos, par->waterwid);
    }
    if (par->nbase) {
        defscript_printf("# Baseline correction\n");
        switch (par->ibase) {
            case BASPL1:
                defscript_printf("    bc poly, %d", par->iterms);
                break;
            case BASPL2:
                defscript_printf("    bc polyw, %d", par->iterms);
                break;
            case BASTB1:
                defscript_printf("    bc tabl");
                break;
            case BASTB2:
                defscript_printf("    bc tablw");
                break;
            case BASSN1:
                if (par->iterms2 > 0)
                    defscript_printf("    bc sine, %d, %d", 
                                             par->iterms2, par->iterms);
                else
                    defscript_printf("    bc sine, %d", par->iterms);
                break;
            case BASSN2:
                if (par->iterms2 > 0)
                    defscript_printf("    bc sinew, %d, %d", 
                                             par->iterms2, par->iterms);
                else
                    defscript_printf("    bc sinew, %d", par->iterms);
                break;
            case BASCS1:
                if (par->iterms2 > 0)
                    defscript_printf("    bc cosine, %d, %d", 
                                             par->iterms2, par->iterms);
                else
                    defscript_printf("    bc cosine, %d", par->iterms);
                break;
            case BASCS2:
                if (par->iterms2 > 0)
                    defscript_printf("    bc cosinew, %d, %d", 
                                             par->iterms2, par->iterms);
                else
                    defscript_printf("    bc cosinew, %d", par->iterms);
                break;
            case BASSC1:
                if (par->iterms2 > 0)
                    defscript_printf("    bc sincos, %d, %d", 
                                             par->iterms2, par->iterms);
                else
                    defscript_printf("    bc sincos, %d", par->iterms);
                break;
            case BASSC2:
                if (par->iterms2 > 0)
                    defscript_printf("    bc sincosw, %d, %d", 
                                             par->iterms2, par->iterms);
                else
                    defscript_printf("    bc sincosw, %d", par->iterms);
                break;
            case BASSP1:
                defscript_printf("    spline calc\n");
                defscript_printf("    bc tabl");
                break;
            case BASSP2:
                defscript_printf("    spline calc\n");
                defscript_printf("    bc tablw");
                break;
            case BASAS1:
                defscript_printf("    bc spline, %d", par->iterms);
                break;
            case BASAS2:
                defscript_printf("    bc splinew, %d", par->iterms);
                break;
        }
        if (par->ibwater) {
            int ibstrt,ibstop;
            ibstrt = max(1,par->ibstrt);
            ibstop = par->ibstop;
            if (ibstop == 0)
                ibstop = dat->isize;
            defscript_printf(", [%d..%d], [%d..%d]\n", 
                             ibstrt, par->ibwater-par->ibwidth/2,
                             par->ibwater+par->ibwidth/2,ibstop);
        }
        else if (par->ibstrt > 1 || par->ibstop != 0)
            defscript_printf(", [%d..%d]\n", par->ibstrt, par->ibstop);
        else
            defscript_printf("\n");
    }
    doit = FALSE;
    for (i=1;i<dat->ityp && !doit;i++) 
        if (dat->pars[i]->isstrt > 1) 
            doit = TRUE;
    defscript_printf("# Write result\n");
    defscript_printf("    rw");
    if (doit) {
        for (i=1;i<dat->ityp;i++) {
            if (i==1) {
                if (dat->pars[i]->isstrt > 1)
                    defscript_printf(", $i - %d", dat->pars[i]->isstrt - 1);
                else
                    defscript_printf(" $i");
            }
            else {
                if (dat->pars[i]->isstrt > 1)
                    defscript_printf(", $i%d - %d", i, dat->pars[i]->isstrt - 1);
                else
                    defscript_printf(", $i%d", i);
            }
        }
    }
    if (par->isstrt > 1 || par->isstop != 0) {
        int isstrt, isstop;
        isstrt = max(1, par->isstrt);
        isstop = min(isize, par->isstop);
        if (isstop == 0)
            isstop = isize;
        if (doit)
            defscript_printf(", [%d..%d]", isstrt, isstop);
        else
            defscript_printf(" [%d..%d]", isstrt, isstop);
    }            
    defscript_printf("\n");
    if (dat->ityp > 1) 
        defscript_printf("done\n");
/*
    if (dat->ityp > 1) {
        defscript_printf("# Update calibration positions\n");
        for (i=0;i<dat->ityp;i++)
            defscript_printf("xr:%d %g\n", i+1, PAR0(i)->xref);
    }
    else {
        defscript_printf("# Update calibration position\n");
        defscript_printf("xr %g\n", par->xref);
    }

*/
    defscript_printf("# Update calibration positions\n");
    for (i=0;i<dat->ityp;i++) {
        int isstrt, isstop, nsize, ssize;
        if (PAR0(i)->icrdir == mblk->info->dimension_id + 1)
            nsize = DAT->isize;
        else
            nsize = PAR0(i)->nsiz;
        isstrt = max(1, PAR0(i)->isstrt);
        isstop = min(nsize, PAR0(i)->isstop);
        if (isstop == 0)
            isstop = nsize;
        ssize = isstop - isstrt + 1;
        if (dat->ityp > 1) {
            defscript_printf("xr:%d %g\n", i+1, PAR0(i)->xref);
            defscript_printf("bref:%d %g\n", i+1, PAR0(i)->bref - isstrt + 1);
            defscript_printf("sw:%d %g\n", i+1, PAR0(i)->swhold * (ssize-1.0)/
                                (nsize-1.0));
        }
        else {
            defscript_printf("xr %g\n", PAR0(i)->xref);
            defscript_printf("bref %g\n", PAR0(i)->bref - isstrt + 1);
            defscript_printf("sw %g\n", PAR0(i)->swhold * (ssize-1.0)/
                                (nsize-1.0));
        }
    }

    defscript_printf("println \"\\nDone\"\n");
    defscript_printf("# Close output file\n");
    defscript_printf("wclose\n");
    defscript_printf("# Reopen the output file for reading\n");
    defscript_printf("ropen %%file2\n");
    defscript_printf("# Set direction\n");
    defscript_printf("dr %d", access[0]);
    for (i=1;i<dat->ityp;i++)
        defscript_printf(", %d", access[i]);
    defscript_printf("\n");
    defscript_printf("# Read first record\n");
    defscript_printf("rn 1");
    for (i=2;i<dat->ityp;i++)
        defscript_printf(", 1");
    defscript_printf("\n");
    defscript_printf("# Update screen\n");
    defscript_printf("if ?co == 1 then; cp; else; pl; endif\n");
    return scriptbuf;
}


static char *create_default_ioscript(MBLOCK *mblk,char *outfile, DBLOCK *dat, PBLOCK *par)
{
    int i,doit,isize,access[MAX_DIM],store_cmplx[MAX_DIM];

    isize = par->nsiz;
    scriptbuf_max  = 1024;
    scriptbuf_size = 0;
    scriptbuf = (char*) malloc(scriptbuf_max * sizeof(char));
    assert(scriptbuf);
    scriptbuf[0] = '\0';
    

    defscript_printf("# Script generated by prospectnd\n");

#ifdef _WIN32
    defscript_printf("%%file1 = \"%s\"\n", 
                    towinfilename(dat->finfo[INFILE].name));
    defscript_printf("%%file2 = \"%s\"\n",
                    towinfilename(outfile));
#else
    defscript_printf("%%file1 = \"%s\"\n", dat->finfo[INFILE].name);
    defscript_printf("%%file2 = \"%s\"\n", outfile);
#endif
    defscript_printf("# Open input file\n");
    doit = FALSE;

    defscript_printf("ropen %%file1\n");

    defscript_printf("# Set processing direction\n");
    for (i=0;i<dat->ityp;i++)
        access[i] = dat->ityp - i;

    defscript_printf("dr %d", access[0]);
    for (i=1;i<dat->ityp;i++)
        defscript_printf(", %d", access[i]);
    defscript_printf("\n");

    /*
     * determine here complex or real store mode
     */
    for (i=0;i<dat->ityp;i++) {
        if (dat->par0[i]->icmplx) {
            if (dat->par0[i]->ipost == PSTBOT || dat->par0[i]->ipost == NOPST) 
                store_cmplx[i] = 1;
            else
                store_cmplx[i] = 0;
        }
        else
            store_cmplx[i] = -1;
            
        if (store_cmplx[i] == 1) {
            defscript_printf("# Set store mode to complex\n");
            defscript_printf("sm:%d 2\n", i+1);
        }
        else if (store_cmplx[i] == 0) {
            defscript_printf("# Set store mode to real\n");
            defscript_printf("sm:%d 1\n", i+1);
        }

    }
    /*
    */
    defscript_printf("# Update calibration positions\n");
    for (i=0;i<dat->ityp;i++) {
        int isstrt, isstop, nsize, ssize;
        if (dat->par0[i]->icrdir == mblk->info->dimension_id + 1)
            nsize = dat->isize;
        else
            nsize = dat->par0[i]->nsiz;
        isstrt = max(1, dat->par0[i]->isstrt);
        isstop = min(nsize, dat->par0[i]->isstop);
        if (isstop == 0)
            isstop = nsize;
        ssize = isstop - isstrt + 1;
        if (dat->ityp > 1) {
            defscript_printf("xr:%d %g\n", i+1, dat->par0[i]->xref);
            defscript_printf("bref:%d %g\n", i+1, dat->par0[i]->bref - isstrt + 1);
            defscript_printf("sw:%d %g\n", i+1, dat->par0[i]->swhold * (ssize-1.0)/
                                    (nsize-1.0));
        }
        else {
            defscript_printf("xr %g\n", dat->par0[i]->xref);
            defscript_printf("bref %g\n", dat->par0[i]->bref - isstrt + 1);
            defscript_printf("sw %g\n", dat->par0[i]->swhold * (ssize-1.0)/
                                    (nsize-1.0));

        }
    }
    /*
    }
    else {
        defscript_printf("# Update calibration position\n");
        defscript_printf("xr %g\n", par->xref);
    }
    */
    if (dat->finfo[OUTFILE].sizeof_float != 4 ||
        dat->finfo[OUTFILE].tresh_flag) {
        defscript_printf("# Set output file truncation\n");
        defscript_printf("otrunc");
        if (dat->finfo[OUTFILE].sizeof_float != 4)
            defscript_printf(" float%d", dat->finfo[OUTFILE].sizeof_float);
        if (dat->finfo[OUTFILE].sizeof_float != 4 &&
            dat->finfo[OUTFILE].tresh_flag)
                defscript_printf(", ");
        if (dat->finfo[OUTFILE].tresh_flag)
            defscript_printf(" %g, %g, %g", 
                dat->finfo[OUTFILE].tresh_levels[0], 
                dat->finfo[OUTFILE].tresh_levels[1],
                dat->finfo[OUTFILE].tresh_levels[2]); 
        defscript_printf("\n");
    }
    defscript_printf("# Open output file\n");
    for (i=0;i<dat->ityp && !doit;i++) 
        if (dat->par0[i]->isstrt > 1 || dat->par0[i]->isstop != 0) 
            doit = TRUE;
    defscript_printf("wopen %%file2");
    if (doit) {
        for (i=0;i<dat->ityp;i++) {
            int isstrt, isstop, nsize;
            nsize = dat->par0[i]->nsiz;
            isstrt = max(1, dat->par0[i]->isstrt);
            isstop = min(nsize, dat->par0[i]->isstop);
            if (isstop == 0)
                isstop = nsize;
            if (isstrt > 1) {
                isstop -= isstrt-1;
                isstrt = 1;
            }
            defscript_printf(", [%d..%d]", isstrt, isstop);
        }
    }
    defscript_printf("\n");

    if (dat->ityp > 1) {
        int id;
        defscript_printf("# For all records do ...\n");
        defscript_printf("for $i");
        for (i=2;i<dat->ityp;i++)
            defscript_printf(", $i%d", i);
        id = dat->ityp - 2;
        if (dat->par0[id]->isstrt > 1) {
            int isstrt, isstop;
            isstrt = max(1, dat->par0[id]->isstrt);
            isstop = min(dat->par0[id]->nsiz, dat->par0[id]->isstop);
            if (isstop == 0)
                isstop = dat->par0[id]->nsiz;

            if (dat->par0[id]->icmplx && store_cmplx[id] == 1)
                defscript_printf(" in [%d..%d,%d..%d]",
                    isstrt, isstop,isstrt+dat->par0[id]->nsiz, 
                    isstop+dat->par0[id]->nsiz);
            else
                defscript_printf(" in sorted [%d..%d]",
                                   isstrt, isstop);
        }
        else
            defscript_printf(" in sorted [..]");
        for (i=2;i<dat->ityp;i++) {
            id = dat->ityp - i - 1;
            if (dat->par0[id]->isstrt > 1) {
                int isstrt, isstop;
                isstrt = max(1, dat->par0[id]->isstrt);
                isstop = min(dat->par0[id]->nsiz, dat->par0[id]->isstop);
                if (isstop == 0)
                    isstop = dat->par0[id]->nsiz;

                if (dat->par0[id]->icmplx && store_cmplx[id] == 1)
                    defscript_printf(" in [%d..%d,%d..%d]",
                        isstrt, isstop,isstrt+dat->par0[id]->nsiz, 
                        isstop+dat->par0[id]->nsiz);
                else
                    defscript_printf(" in sorted [%d..%d]",
                                   isstrt, isstop);
            }
            else
                defscript_printf(", [..]");
        }
        defscript_printf(" do\n");
    }
    if (dat->ityp > 1) {
  
        defscript_printf("    print \"\\rProcessing record \" + %%i");
        for (i=2;i<dat->ityp;i++)
            defscript_printf(" + \", \" + %%i%d", i);
        defscript_printf("\n");

    }
    else
        defscript_printf("    println \"Processing record\"");
    defscript_printf("# Read record\n");
    if (dat->ityp > 1) {
        defscript_printf("    rn $i");
        for (i=2;i<dat->ityp;i++)
            defscript_printf(", $i%d", i);
    }
    else
        defscript_printf("    rn");
    if (doit)
        defscript_printf(", [%d..%d]\n", par->irstrt, par->irstop);
    else
        defscript_printf("\n");
    doit = FALSE;
    for (i=1;i<dat->ityp && !doit;i++) 
        if (dat->par0[i]->isstrt > 1) 
            doit = TRUE;
    defscript_printf("# Write result\n");
    defscript_printf("    rw");
    if (doit) {
        for (i=dat->ityp-2;i>=0;i--) {
            int isstrt, isstop;
            isstrt = max(1, dat->par0[i]->isstrt);
            isstop = min(dat->par0[i]->nsiz, dat->par0[i]->isstop);
            if (isstop == 0)
                isstop = dat->par0[i]->nsiz;
            if (i==dat->ityp-2) {
                if (isstrt > 1)
                    defscript_printf(", $i - %d", isstrt-1);
                else
                    defscript_printf(" $i");
            }
            else {
                if (isstrt > 1)
                    defscript_printf(", $i%d - %d", i, isstrt-1);
                else
                    defscript_printf(", $i%d", i);
            }
        }
    }
    if (par->isstrt > 1 || par->isstop != 0) {
        int isstrt, isstop;
        isstrt = max(1, par->isstrt);
        isstop = min(isize, par->isstop);
        if (isstop == 0)
            isstop = isize;
        if (doit)
            defscript_printf(", [%d..%d]", isstrt, isstop);
        else
            defscript_printf(" [%d..%d]", isstrt, isstop);
    }            
    defscript_printf("\n");
    if (dat->ityp > 1) 
        defscript_printf("done\n");
    defscript_printf("println \"\\nDone\"\n");
    defscript_printf("# Close output file\n");
    defscript_printf("wclose\n");
    defscript_printf("# Reopen the output file for reading\n");
    defscript_printf("ropen %%file2\n");
    defscript_printf("# Set direction\n");
    defscript_printf("dr %d", access[0]);
    for (i=1;i<dat->ityp;i++)
        defscript_printf(", %d", access[i]);
    defscript_printf("\n");
    defscript_printf("# Read first record\n");
    defscript_printf("rn 1");
    for (i=2;i<dat->ityp;i++)
        defscript_printf(", 1");
    defscript_printf("\n");
    defscript_printf("# Update screen\n");
    defscript_printf("if ?co == 1 then; cp; else; pl; endif\n");
    return scriptbuf;
}


void free_default_script()
{
    if (scriptbuf)
        free(scriptbuf);
    scriptbuf     = NULL;
    scriptbuf_max = 1;
}

static char *getenvfilename(MBLOCK *mblk, char *title, char *mask)
{
    char *tmp, *p;
    /*
     * Remember current directory in 'dirmask'
     */
    char dirmask[500];

    if ((p = getenv("PSNDSCRIPTDIR")) == NULL) {
          char *label,string2[PSND_STRLEN + 1];
        if (g_popup_messagebox2(mblk->info->win_id, "Warning", 
                          "Environment variable \"PSNDSCRIPTDIR\" not set", 
                          "Select", "Cancel")==G_POPUP_BUTTON2)
            return NULL;
       /*
         //   string2[0] = '\0';
       //   p=g_popup_promptdialog(mblk->info->win_id, "Warning", 
       //                   "Environment variable \"PSNDSCRIPTDIR\" not set",
       //       string2);
       */
          p = g_popup_getdirname(mblk->info->win_id, 
                           "Select \"PSNDSCRIPTDIR\"");
          if (p==NULL) 
              return NULL;
          label = psnd_sprintf_temp("PSNDSCRIPTDIR=%s",p);
          putenv(label);
    }
    strcpy(dirmask, p);
#ifdef _WIN32
    if (p[strlen(p)-1] != '\\')
        strcat(dirmask,"\\");
    strcat(dirmask,mask);
    tmp = g_popup_getfilename(mblk->info->win_id, title, dirmask) ;
    if (tmp != NULL && (strlen(tmp) > 0) && (tmp[strlen(tmp)-1] != '\\') ) 
        return tmp;
#else
    if (p[strlen(p)-1] != '/')
        strcat(dirmask,"/");
    strcat(dirmask,mask);
    tmp = g_popup_getfilename(mblk->info->win_id, title, dirmask) ;
    if (tmp != NULL && (strlen(tmp) > 0) && (tmp[strlen(tmp)-1] != '/') ) 
        return tmp;
#endif
    return NULL;
}

typedef enum {
    SCRIPT_TEXT_ID = 100,
    SCRIPT_ARG_ID,
    SCRIPT_READ_ID,
    SCRIPT_ENV_ID,
    SCRIPT_DIR_ID,
    SCRIPT_WRITE_ID,
    SCRIPT_CLEAR_ID,
    SCRIPT_CREATE_ID
} script_ids;

static void au_script_callback(G_POPUP_CHILDINFO *ci)
{
    int len, size;
    char *label, *filename;
    FILE *file;
    char buff[PSND_STRLEN+1];
    MBLOCK *mblk = (MBLOCK *) ci->userdata;
    POPUP_INFO *popinf = mblk->popinf + POP_AU;

    switch (ci->type) {
        case G_CHILD_OK:
        case G_CHILD_CANCEL:
            popinf->visible = FALSE;
            return;
    }
    switch (ci->id) {

        case SCRIPT_READ_ID: 
            filename = psnd_getfilename(mblk,"Read local script file", "*.au");
            if (filename == NULL)
                break;
            file = fopen(filename, "r");
            if (file == NULL)
                break;
            len = 0;
            au_script = (char*) realloc(au_script, PSND_STRLEN);
            au_script[0] = '\0';
            while ((size=fread(buff, 1, PSND_STRLEN/2, file)) > 0) { 
                buff[size] = '\0';
#ifdef _WIN32
                size=towin(buff);
#else
                size=tounix(buff);
#endif
                len += size;
                au_script = (char*) realloc(au_script, len+1);
                strcat(au_script, buff);
            }
            g_popup_set_label(ci->cont_id, SCRIPT_TEXT_ID, au_script);
            fclose(file);
            break;

        case SCRIPT_ENV_ID: 
            filename = getenvfilename(mblk, "Read script file", "*.au");
            if (filename == NULL)
                break;
            file = fopen(filename, "r");
            if (file == NULL)
                break;
            len = 0;
            au_script = (char*) realloc(au_script, PSND_STRLEN);
            au_script[0] = '\0';
            while ((size=fread(buff, 1, PSND_STRLEN/2, file)) > 0) { 
                buff[size] = '\0';
#ifdef _WIN32
                size=towin(buff);
#else
                size=tounix(buff);
#endif
                len += size;
                au_script = (char*) realloc(au_script, len+1);
                strcat(au_script, buff);
            }
            g_popup_set_label(ci->cont_id, SCRIPT_TEXT_ID, au_script);
            fclose(file);
            break;

        case SCRIPT_DIR_ID: {
            char *label,*p,string2[PSND_STRLEN + 1];

            if ((p = getenv("PSNDSCRIPTDIR")) == NULL) 
                string2[0] = '\0';
            else 
                strncpy(string2, p, PSND_STRLEN);
/*
           // p=g_popup_promptdialog(mblk->info->win_id, "Define", 
            //              "Set Environment variable \"PSNDSCRIPTDIR\"             ",
            //              string2);
*/
            p = g_popup_getdirname(mblk->info->win_id, 
                           "Select \"PSNDSCRIPTDIR\"");
            if (p==NULL) 
                return;  
            label = psnd_sprintf_temp("PSNDSCRIPTDIR=%s",p);
            putenv(label);
            }
            break;

        case SCRIPT_WRITE_ID: 
            label = g_popup_get_label(ci->cont_id, SCRIPT_TEXT_ID);
            if (label == NULL)
                break;
            len = strlen(label);
            if (len == 0)
                break;
            filename = psnd_savefilename(mblk,"Write local script file", "*.au");
            if (filename == NULL)
                break;
            file = fopen(filename, "w");
            if (file == NULL)
                break;
            au_script = (char*) realloc(au_script, len+1);
            strcpy(au_script, label);
            fwrite(au_script, 1, len, file);
            fclose(file);
            break;

        case SCRIPT_CLEAR_ID: 
            if (g_popup_messagebox2(mblk->info->win_id, "Clear",
                        "Clear Buffer", "Yes", "No" ) 
                        == G_POPUP_BUTTON1) 
                g_popup_set_label(ci->cont_id, SCRIPT_TEXT_ID, "");
            break;

        case SCRIPT_CREATE_ID: 
            filename = psnd_savefilename(mblk,"Output NMR data file", "*");
            if (filename) {
                int ok=TRUE;
                if (DAT->ityp <= 0)
                    break;
                if (DAT->ityp == 1)
                    DAT->nextdr=1;
                else
                    ok = popup_spin(mblk->info->win_id, "Process in direction ", 
                                      &(DAT->nextdr), "Process dir", 
                                      1, DAT->ityp, 1);
                /*
                DAT->nextdr=DAT->access[0];
                */
                if (ok) {
                    char *scrpt = 
                        create_default_script(mblk,filename,
                                              DAT,
                                              PAR0(DAT->nextdr-1));
                    g_popup_set_label(ci->cont_id, SCRIPT_TEXT_ID,scrpt);
                    free_default_script();
                }
            }
            break;

    }
}

char *psnd_popup_run_script(MBLOCK *mblk, int read_script)
{
    G_POPUP_CHILDINFO ci[22];
    int i, id;
    static int id_text, id_arg;
    POPUP_INFO *popinf = mblk->popinf + POP_AU;

    popinf->visible = TRUE;
   {     int cont_id;
        cont_id = g_popup_container_open(mblk->info->win_id, "Run script",
                                      G_POPUP_WAIT);
        popinf->cont_id = cont_id;
        id=0;

	g_popup_init_info(&(ci[id]));
	ci[id].type          = G_CHILD_LABEL;
	ci[id].id            = id;
        ci[id].label = "Script";
	g_popup_add_child(cont_id, &(ci[id]));

        id++;
        id_text = id;
	g_popup_init_info(&(ci[id]));
	ci[id].type          = G_CHILD_MULTILINETEXT;
	ci[id].id            = SCRIPT_TEXT_ID;
	ci[id].item_count    = 14;
	ci[id].items_visible = 40;
        ci[id].label = "";
        if (au_script != NULL)
	    ci[id].label         = au_script;
        ci[id].func         = au_script_callback;
        ci[id].userdata     = (void*) mblk;
	g_popup_add_child(cont_id, &(ci[id]));

        id++;
	g_popup_init_info(&(ci[id]));
	ci[id].type          = G_CHILD_LABEL;
	ci[id].id            = id;
        ci[id].label = "Script arguments";
	g_popup_add_child(cont_id, &(ci[id]));

        id++;
        id_arg = id;
	g_popup_init_info(&(ci[id]));
	ci[id].type          = G_CHILD_TEXTBOX;
	ci[id].id            = SCRIPT_ARG_ID;
	ci[id].items_visible = 40;
	ci[id].item_count = 0;
        ci[id].label = "";
        if (au_args_store != NULL)
	    ci[id].label         = au_args_store;
        ci[id].func         = au_script_callback;
        ci[id].userdata     = (void*) mblk;
	 g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type         = G_CHILD_PUSHBUTTON;
        ci[id].id           = SCRIPT_READ_ID;
        ci[id].label        = "Read local script";
        ci[id].func         = au_script_callback;
        ci[id].userdata     = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type         = G_CHILD_PUSHBUTTON;
        ci[id].id           = SCRIPT_ENV_ID;
        ci[id].label        = "Read script";
        ci[id].func         = au_script_callback;
        ci[id].userdata     = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type         = G_CHILD_PUSHBUTTON;
        ci[id].id           = SCRIPT_DIR_ID;
        ci[id].label        = "Set Script Dir";
        ci[id].func         = au_script_callback;
        ci[id].userdata     = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type         = G_CHILD_PUSHBUTTON;
        ci[id].id           = SCRIPT_WRITE_ID;
        ci[id].label        = "Write local script";
        ci[id].func         = au_script_callback;
        ci[id].userdata     = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type         = G_CHILD_PUSHBUTTON;
        ci[id].id           = SCRIPT_CLEAR_ID;
        ci[id].label        = "Clear";
        ci[id].func         = au_script_callback;
        ci[id].userdata     = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type         = G_CHILD_PUSHBUTTON;
        ci[id].id           = SCRIPT_CREATE_ID;
        ci[id].label        = "Create script from Parameters";
        ci[id].func         = au_script_callback;
        ci[id].userdata     = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_OK;
        ci[id].func  = au_script_callback;
        ci[id].userdata      = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_CANCEL;
        ci[id].func  = au_script_callback;
        ci[id].userdata      = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));
    }
    if (read_script)
        g_popup_set_label(popinf->cont_id, SCRIPT_TEXT_ID, au_script);

    if (g_popup_container_show(popinf->cont_id)) {
        int len;
        static char arg[] = "script";
        
        id = id_text;
        if (ci[id].label == NULL)
            return NULL;
        len = strlen(ci[id].label) + 1;
        au_script = (char*) realloc(au_script, len);
        strcpy(au_script, ci[id].label);
        
        id = id_arg;
        if (ci[id].label != NULL) {
            len = strlen(arg) + strlen(ci[id].label) + 2;
            au_args_store = (char*) realloc(au_args_store, len);
            au_args = (char*) realloc(au_args, len);
            strcpy(au_args, arg);
            strcat(au_args, " ");
            strcat(au_args, ci[id].label);
            strcpy(au_args_store,ci[id].label);
            return au_args;        
        }
        
        return arg;        
    }
    return NULL;
}

/*
 * Also used for script-include files
 */
FILE *psnd_open_script_file(char *filename, char *argv0)
{
    FILE *scriptfile;
    char *p,*q;

    /*
     * First try local directory
     */
    if ((scriptfile = fopen(filename,"r")) != NULL)
        return scriptfile;
#ifdef _WIN32
    /*
     * if '\\' present then absolute path name
     * Do not try further
     */
    if (strchr(filename, '\\') != NULL)
        return NULL;
    /*
     * Now try master script directory, if available
     */
    if (argv0 != NULL) {
        if ((p=strchr(argv0, '\\')) != NULL) {
            p++;
            q = (char*) malloc(strlen(filename) + strlen(argv0) + 1);
            assert(q);
            strncpy(q, argv0, (p-argv0));
            strcat(q,filename);
            scriptfile = fopen(q,"r");
            free(q);
            if (scriptfile != NULL)
                return scriptfile;
        }
    }
    /*
     * Finally try environment variable PSNDSCRIPTDIR
     */
    if ((p = getenv("PSNDSCRIPTDIR")) == NULL)
        return NULL;
    q = (char*) malloc(strlen(filename) + strlen(p) + 2);
    assert(q);
    strcpy(q,p);
    if (p[strlen(p)-1] != '\\')
        strcat(q,"\\");
#else
    /*
     * if '/' present then absolute path name
     * Do not try further
     */
    if (strchr(filename, '/') != NULL)
        return NULL;
    /*
     * Now try master script directory, if available
     */
    if (argv0 != NULL) {
        if ((p=strchr(argv0, '/')) != NULL) {
            p++;
            q = (char*) malloc(strlen(filename) + strlen(argv0) + 1);
            assert(q);
            strncpy(q, argv0, (p-argv0));
            strcat(q,filename);
            scriptfile = fopen(q,"r");
            free(q);
            if (scriptfile != NULL)
                return scriptfile;
        }
    }
    /*
     * Finally try environment variable PSNDSCRIPTDIR
     */
    if ((p = getenv("PSNDSCRIPTDIR")) == NULL)
        return NULL;
    q = (char*) malloc(strlen(filename) + strlen(p) + 2);
    assert(q);
    strcpy(q,p);
    if (p[strlen(p)-1] != '/')
        strcat(q,"/");
#endif
    strcat(q,filename);
    scriptfile = fopen(q,"r");
    free(q);
    return scriptfile;
}

char *psnd_read_script_in_buffer(char *filename)
{
    FILE *file;
    char buff[PSND_STRLEN+1];
    int len, size;
    char *script = NULL;
    /*
    file = fopen(filename, "r");
    */
    file = psnd_open_script_file(filename, NULL);
    if (file == NULL)
        return NULL;
    len=0;
    script = (char*) realloc(script, PSND_STRLEN);
    script[0] = '\0';
    while ((size=fread(buff, 1, PSND_STRLEN/2, file)) > 0) { 
        buff[size] = '\0';
#ifdef _WIN32
        size=towin(buff);
#else
        size=tounix(buff);
#endif
        len += size;
        script = (char*) realloc(script, len+1);
        strcat(script, buff);
    }
    fclose(file);
    if (au_script)
        free(au_script);
    au_script = script;
    return script;
}

int psnd_read_script(MBLOCK *mblk)
{
    int len, size;
    char *label, *filename;
    FILE *file;
    char buff[PSND_STRLEN+1];

    filename = psnd_getfilename(mblk,"Read script file", "*.au");
    if (filename == NULL)
        return FALSE;
    if (psnd_read_script_in_buffer(filename) == NULL)
        return FALSE;
    return (psnd_popup_run_script(mblk, TRUE) != NULL);
}

#ifdef blabla

   println "Prepa.au argument options"
   println "-i file_or_directory_name   - Define input file or directory"
   println "-o output_file              - Define output file"
   println "-d ndim                     - Define the dimensionality of the file"
   println "-a acqorder                 - Define acq. order for 3d: 321 or 312"
   println "-m n mode                   - Define datatype in direction n (n=1,2,3,4)"
   println " -m n 0                     - Set mode for tn from file (default) (n=1,2)"
   println " -m n c or -m n s or -m n 1 - Set mode for tn to States (i.e. complex)  (n=1,2)"
   println " -m n r or -m n t or -m n 2 - Set mode for tn to TPPI (i.e. real)       (n=1,2)"
   println " -m n e or -m n 3           - Set mode for tn to echo-antiecho          (n=1,2)"
   println " -m n z or -m n 4           - Set mode for tn to States-TPPI            (n=1,2)"
   println "-s n size                   -   Define size in direction n (n=1,2,3,4)"
   println "-h                          -   This help overview"
   exit
#endif

typedef enum {
    PREPA_DATA_ID = 100,
    PREPA_DATA_TEXT_ID,
    PREPA_OUTFILE_ID,
    PREPA_OUTFILE_TEXT_ID,
    PREPA_SCRIPT_ID,
    PREPA_SCRIPT_TEXT_ID
} prepa_ids;

static void au_prepa_setup_callback(G_POPUP_CHILDINFO *ci)
{
    char *filename;
    MBLOCK *mblk = (MBLOCK *) ci->userdata;

    switch (ci->id) {

        case PREPA_DATA_ID: 
            filename = psnd_getfilename(mblk,"NMR input data", "*");
            if (filename == NULL)
                break;
            g_popup_set_label(ci->cont_id, PREPA_DATA_TEXT_ID, filename);
            break;

        case PREPA_OUTFILE_ID: 
            filename = psnd_getfilename(mblk,"Output File", "*");
            if (filename == NULL)
                break;
            g_popup_set_label(ci->cont_id, PREPA_OUTFILE_TEXT_ID, filename);
            break;

        case PREPA_SCRIPT_ID: 
            filename = getenvfilename(mblk, "Read script file", "*.au");
            if (filename == NULL)
                break;
            g_popup_set_label(ci->cont_id, PREPA_SCRIPT_TEXT_ID, filename);
            break;
    }
}

#define MAXBUFLEN 256
int psnd_popup_prepa_setup(MBLOCK *mblk)
{
    G_POPUP_CHILDINFO ci[22];
    int i, id;
    static int id_dir, id_output, id_mode, id_dim, id_acq, id_script;
    int cont_id;
    static char *acqorderlabels[] ={ "321   ", "312   " };
    static char *modelabels[] ={ "Autodetect", "States", "TPPI", 
                                 "Echo-antiecho", "States-TPPI" };
    static char outfile[MAXBUFLEN], 
           datadir[MAXBUFLEN], scriptfile[MAXBUFLEN];
    int min_max_dec_step[] = { 1, 3, 0, 1 };


    cont_id = g_popup_container_open(mblk->info->win_id, "Prepa setup",
                                      G_POPUP_WAIT);

    id=0;

    g_popup_init_info(&(ci[id]));
    ci[id].type          = G_CHILD_LABEL;
    ci[id].id            = id;
    ci[id].label = "Conversion of Bruker or Varian data sets";
    g_popup_add_child(cont_id, &(ci[id]));

    id++;
    g_popup_init_info(&(ci[id]));
    ci[id].type          = G_CHILD_SEPARATOR;
    ci[id].id            = id;
    g_popup_add_child(cont_id, &(ci[id]));

    id++;
    g_popup_init_info(&(ci[id]));
    ci[id].type          = G_CHILD_LABEL;
    ci[id].id            = id;
    ci[id].label = "Data dir";
    g_popup_add_child(cont_id, &(ci[id]));

    id++;
    id_dir = id;
    g_popup_init_info(&(ci[id]));
    ci[id].type          = G_CHILD_TEXTBOX;
    ci[id].id            = PREPA_DATA_TEXT_ID;
    ci[id].items_visible = 40;
    ci[id].label         = datadir;
    ci[id].func          = au_prepa_setup_callback;
    ci[id].userdata      = (void*) mblk;
    g_popup_add_child(cont_id, &(ci[id]));
 
    id++;
    g_popup_init_info(&(ci[id]));
    ci[id].type         = G_CHILD_PUSHBUTTON;
    ci[id].id           = PREPA_DATA_ID;
    ci[id].label        = "Browse NMR data";
    ci[id].func         = au_prepa_setup_callback;
    ci[id].userdata     = (void*) mblk;
    g_popup_add_child(cont_id, &(ci[id]));

    g_popup_init_info(&(ci[id]));
    ci[id].type          = G_CHILD_LABEL;
    ci[id].id            = id;
    ci[id].label = "Output file Name";
    g_popup_add_child(cont_id, &(ci[id]));

    id++;
    id_output = id;
    g_popup_init_info(&(ci[id]));
    ci[id].type          = G_CHILD_TEXTBOX;
    ci[id].id            = PREPA_OUTFILE_TEXT_ID;
    ci[id].items_visible = 40;
    ci[id].label        = outfile;
    ci[id].func         = au_prepa_setup_callback;
    ci[id].userdata     = (void*) mblk;
    g_popup_add_child(cont_id, &(ci[id]));
   
    id++;
    g_popup_init_info(&(ci[id]));
    ci[id].type         = G_CHILD_PUSHBUTTON;
    ci[id].id           = PREPA_OUTFILE_ID;
    ci[id].label        = "Browse ...";
    ci[id].func         = au_prepa_setup_callback;
    ci[id].userdata     = (void*) mblk;
    g_popup_add_child(cont_id, &(ci[id]));

    g_popup_init_info(&(ci[id]));
    ci[id].type          = G_CHILD_LABEL;
    ci[id].id            = id;
    ci[id].label = "Prepa script";
    g_popup_add_child(cont_id, &(ci[id]));

    id++;
    id_script = id;
    g_popup_init_info(&(ci[id]));
    ci[id].type          = G_CHILD_TEXTBOX;
    ci[id].id            = PREPA_SCRIPT_TEXT_ID;
    ci[id].items_visible = 40;
    ci[id].label        = scriptfile;
    ci[id].func         = au_prepa_setup_callback;
    ci[id].userdata     = (void*) mblk;
    g_popup_add_child(cont_id, &(ci[id]));
   
    id++;
    g_popup_init_info(&(ci[id]));
    ci[id].type         = G_CHILD_PUSHBUTTON;
    ci[id].id           = PREPA_SCRIPT_ID;
    ci[id].label        = "Browse Prepa Script...";
    ci[id].func         = au_prepa_setup_callback;
    ci[id].userdata     = (void*) mblk;
    g_popup_add_child(cont_id, &(ci[id]));

    id++;
    id_mode = id;
    popup_add_option2(cont_id, &(ci[id]), id, 
                           "Bruker modes", 0, 5,
                           modelabels);

    id++;
    id_acq = id;
    popup_add_option2(cont_id, &(ci[id]), id, 
                           "Bruker 3D acq. order", 0, 2,
                           acqorderlabels);

    id++;
    id_dim = id;
    popup_add_spin(cont_id, &(ci[id]), id, 
                   "Dimension NMR data", 0, min_max_dec_step);

    assert(id<22);
    if (g_popup_container_show(cont_id)) {
        static char temp_buf[4*PSND_STRLEN ];
        char *label,*p;
        id = id_dir;
        if (ci[id].label != NULL) {
            strncpy(datadir, ci[id].label, MAXBUFLEN);
            datadir[MAXBUFLEN-1] = '\0';
        }
        id = id_output;
        if (ci[id].label != NULL) {
            strncpy(outfile, ci[id].label, MAXBUFLEN);
            outfile[MAXBUFLEN-1] = '\0';
        }
        id = id_script;
        if (ci[id].label != NULL) {
            strncpy(scriptfile, ci[id].label, MAXBUFLEN);
            scriptfile[MAXBUFLEN-1] = '\0';
        }
#ifdef _WIN32
        p = strrchr(datadir, '\\');
#else
        p = strrchr(datadir, '/');
#endif 
        if (p) {
            p++;
            if ((*p != '\0') &&
                (strcmp(p, "fid") == 0 ||
                 strcmp(p, "ser") == 0))
                        *p = '\0';
        }

        if (ci[id_script].label != NULL)
            psnd_read_script_in_buffer(ci[id_script].label);


        sprintf(temp_buf,"-i \"%s\" -o \"%s\"",datadir, outfile);
        if (ci[id_mode].item>0) {
            int dim = max (1,ci[id_dim].item-1);
            label = psnd_sprintf_temp(" -m %d %d ",
                   dim, ci[id_mode].item);
            strcat(temp_buf, label);
        }
        if (ci[id_dim].item > 2) 
            label = psnd_sprintf_temp(" -d %d -a %s",
                    ci[id_dim].item,
                    acqorderlabels[ci[id_acq].item]);
        else
            label = psnd_sprintf_temp(" -d %d", ci[id_dim].item);
        strcat(temp_buf, label);
        label = temp_buf;

        if (label != NULL) {
            int len = strlen(label) + 2;
            au_args_store = (char*) realloc(au_args_store, len);
            assert(au_args_store);
            strcpy(au_args_store, label);
        }
        label = psnd_sprintf_temp("%s", outfile);
#ifdef _WIN32
        p = strrchr(label, '\\');
#else
        p = strrchr(label, '/');
#endif 
        if (p) {
            int err;
            p++;
            if (*p != '\0')
                *p = '\0';

            err = chdir(label);
            if (err)
                psnd_printf(mblk, "  Failed to chdir to %s\n", label);
            else
                psnd_printf(mblk, "  Changed to %s\n", label);

        }


        return TRUE;
    }
    return FALSE;
}


/****************
 * output setup
 */
typedef enum {
    OUTPUT_DATA_ID = 100,
    OUTPUT_DATA_TEXT_ID,
    OUTPUT_OUTFILE_ID,
    OUTPUT_OUTFILE_TEXT_ID,
    OUTPUT_SCRIPT_ID,
    OUTPUT_SCRIPT_TEXT_ID
} output_ids;

static void au_output_setup_callback(G_POPUP_CHILDINFO *ci)
{
    char *filename;
    MBLOCK *mblk = (MBLOCK *) ci->userdata;

    switch (ci->id) {

        case OUTPUT_DATA_ID: 
            filename = psnd_getfilename(mblk,"NMR input data", "*");
            if (filename == NULL)
                break;
            g_popup_set_label(ci->cont_id, OUTPUT_DATA_TEXT_ID, filename);
            break;

    }
}

int psnd_popup_output_setup(MBLOCK *mblk)
{
    G_POPUP_CHILDINFO ci[22];
    int i, j,id;
    static int id_dir, id_output, id_last, id_dim, id_first, id_script;
    int cont_id;
    int result = FALSE;
    int xlow, ylow, xhigh, yhigh;
    int xlow_store, ylow_store, xhigh_store, yhigh_store;
    static char outfile[MAXBUFLEN], 
           filename[MAXBUFLEN], scriptfile[MAXBUFLEN];
    PBLOCK *par = PAR;
    SBLOCK *spar = mblk->spar;
    CBLOCK *cpar = mblk->cpar_screen;
    DBLOCK *dat  = DAT;


    cont_id = g_popup_container_open(mblk->info->win_id, "Output setup",
                                      G_POPUP_WAIT);

    id=0;

    g_popup_init_info(&(ci[id]));
    ci[id].type          = G_CHILD_LABEL;
    ci[id].id            = id;
    ci[id].label = "Save current file as...";
    g_popup_add_child(cont_id, &(ci[id]));

    id++;
    g_popup_init_info(&(ci[id]));
    ci[id].type          = G_CHILD_SEPARATOR;
    ci[id].id            = id;
    g_popup_add_child(cont_id, &(ci[id]));

    id++;
    g_popup_init_info(&(ci[id]));
    ci[id].type          = G_CHILD_LABEL;
    ci[id].id            = id;
    ci[id].label         = "New filename";
    g_popup_add_child(cont_id, &(ci[id]));

    id++;
    id_dir = id;
    g_popup_init_info(&(ci[id]));
    ci[id].type          = G_CHILD_TEXTBOX;
    ci[id].id            = OUTPUT_DATA_TEXT_ID;
    ci[id].items_visible = 40;
    ci[id].label         = filename;
    ci[id].func          = au_output_setup_callback;
    ci[id].userdata      = (void*) mblk;
    g_popup_add_child(cont_id, &(ci[id]));
 
    id++;
    g_popup_init_info(&(ci[id]));
    ci[id].type         = G_CHILD_PUSHBUTTON;
    ci[id].id           = OUTPUT_DATA_ID;
    ci[id].label        = "Browse NMR data";
    ci[id].func         = au_output_setup_callback;
    ci[id].userdata     = (void*) mblk;
    g_popup_add_child(cont_id, &(ci[id]));

    id_first = id;


    psnd_get_plotarea(mblk, &xlow, &xhigh, &ylow, &yhigh);
/*
    if (dat->ityp == 2 && mblk->info->dimension_id == 0) {
        int tmp = xlow;
        xlow    = xhigh;
        xhigh   = tmp;
        tmp     = ylow;
        ylow    = yhigh;
        yhigh   = tmp;
    }

*/
    for (j=0;j<dat->ityp;j++) {
        par = PAR0(j);
        if (par->icmplx) {
            id = param_add_post(cont_id, ci, id, par)+1;
        }
        else {
            /*
             * dummy entry
             */
            g_popup_init_info(&(ci[id]));
            ci[id].type          = G_CHILD_SEPARATOR;
            ci[id].id            = 0;
           /* g_popup_add_child(cont_id, &(ci[id]));*/
            id++;
        }
        if (dat->ityp == 2 && mblk->info->plot_mode == PLOT_2D) {
            if (j == mblk->info->dimension_id) {
                xlow_store  = par->isstrt;
                xhigh_store = par->isstop;
                par->isstrt = xlow;
                par->isstop = xhigh;
            }
            else {
                ylow_store  = par->isstrt;
                yhigh_store = par->isstop;
                par->isstrt = ylow;
                par->isstop = yhigh;
            }
        }
        else if (dat->ityp == 1) {
            xlow_store  = par->isstrt;
            xhigh_store = par->isstop;
            par->isstrt = xlow;
            par->isstop = xhigh;
        }
        id = param_add_outfile(cont_id, ci, id, par)+1;
    }
    id_last = id;

    par = PAR;
    g_popup_init_info(&(ci[id]));

    assert(id<22);
    
    if (g_popup_container_show(cont_id)) {
        static char temp_buf[4*PSND_STRLEN ];
        char *label, *p, *scrpt;


        id = id_dir;
        if (ci[id].label != NULL) {
            strncpy(filename, ci[id].label, MAXBUFLEN);
            filename[MAXBUFLEN-1] = '\0';
        }

        DAT->nextdr = DAT->ityp;

        param_process_input(mblk, id_last - id_first, 
            0, dat->ityp, 
            ci + id_first + 1, cpar, mblk->par, dat);


        scrpt = create_default_ioscript(mblk,filename,
                                        DAT, PAR0(DAT->nextdr-1));
        if (scrpt) {
            if (au_script)
                free(au_script);
            au_script = scrpt;
        }
        result = TRUE;
    }
    if (dat->ityp == 2 && mblk->info->plot_mode == PLOT_2D) {
        for (j=0;j<dat->ityp;j++) {
            par = PAR0(j);
            if (j == mblk->info->dimension_id) {
                par->isstrt = xlow_store;
                par->isstop = xhigh_store;
            }
            else {
                par->isstrt = ylow_store;
                par->isstop = yhigh_store;
            }
        }
    }
    else if (dat->ityp == 1) {
        par = mblk->par[mblk->info->block_id];
        par->isstrt = xlow_store;
        par->isstop = xhigh_store;
    }
    return result;
}
