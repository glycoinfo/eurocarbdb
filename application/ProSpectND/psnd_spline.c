/********************************************************************/
/*                          psnd_spline.c                           */
/*                                                                  */
/* Spline baseline routines for the psnd program                    */
/* 1997, Albert van Kuik                                            */
/********************************************************************/
#include <stdio.h>
#include <stdlib.h>
#include <ctype.h>
#include <string.h>
#include <stdarg.h>
#include <math.h>
#include <assert.h>
#include "nmrtool.h"
#include "genplot.h"
#include "psnd.h"


/*
 * Calculate the value of the n-th basis function for the
 * interpolating trigonometric spline for parameter t; used to
 * calculate the basis curve one point at a time
 *
 * Pass through control points
 */
static float InTspline(float t, int n)
{
    float sn,cs;
    sn = sin(0.5 * M_PI * t);
    cs = cos(0.5 * M_PI * t);
    switch (n) {
        case 1:
            return (0.5 * cs * (cs-1));
        case 2:
            return (0.5 * sn * (sn+1));
        case 3:
            return (0.5 * cs * (cs+1));
        case 4:
            return (0.5 * sn * (sn-1));
    }
    return t;
} 

/*
 * Calculate the value of the n-th basis function for the
 * exterpolating trigonometric spline for parameter t; used to
 * calculate the basis curve one point at a time
 *
 * Do not pass through control points
 */
static float ExTspline(float t, int n)
{
    float sn,cs;
    sn = sin(0.5 * M_PI * t);
    cs = cos(0.5 * M_PI * t);
    switch (n) {
        case 1:
            return (0.25 * (1-cs));
        case 2:
            return (0.25 * (1+sn));
        case 3:
            return (0.25 * (1+cs));
        case 4:
            return (0.25 * (1-sn));
    }
    return t;
} 


/*
 * Add a control point to the control-point buffer and
 * update the curve
 */
static void spline_add_control_point(SPLINE_INFO *sinf, int init, 
                                      int interpolating, int jx, 
                                      float y, int nsize, float *sbuf)
{
/*
 *    _Algorithm Alley_
 *    by Robert F. Kauffmann
 *    Dr. Dobb's Journal
 *    May 1997
 *
 *    Int_Tspline: 
 *    Cubic spline which passes through all 
 *    control points.
 *    Continuity in all derivatives.
 *
 */
    int i,j, highest_x;
    
    if (init == TRUE) {
        for (i=0;i<4;i++) {
            sinf->xx[i]=0;
            sinf->yy[i]=0;
        }
        memset(sbuf,0,sizeof(float)*nsize);
        sinf->buffsize = 0;
        return;
    }
    for (j=0;j<3;j++) {
        sinf->xx[j] = sinf->xx[j+1];
        sinf->yy[j] = sinf->yy[j+1];
    }
    sinf->xx[3] = jx;
    sinf->yy[3] = y;
  
    if (sinf->buffsize != nsize) {
        /*
         * Build a pre-compiled table with sin and cos values
         */
        for (j=0;j<4;j++) {
            sinf->basis[j] = 
                (float*) realloc(sinf->basis[j], sizeof(float) * nsize);
            assert(sinf->basis[j]);
            if (interpolating)
                for (i=0;i<nsize;i++)
                    sinf->basis[j][i] = 
                        InTspline(((float)(i+1))/((float)nsize), j+1);
            else
                for (i=0;i<nsize;i++)
                    sinf->basis[j][i] = 
                        ExTspline(((float)(i+1))/((float)nsize), j+1);
        }
        sinf->buffsize = nsize;
    }
    highest_x = nsize;
    for (i=0;i<nsize;i++) {
        float x = sinf->xx[0] * sinf->basis[0][i] + 
                  sinf->xx[1] * sinf->basis[1][i] +
                  sinf->xx[2] * sinf->basis[2][i] + 
                  sinf->xx[3] * sinf->basis[3][i];
        int dx = (int) round(x) ;
        if (dx >= 0 /* && dx < highest_x */) {
            highest_x = dx;
            sbuf[dx] = sinf->yy[0] * sinf->basis[0][i] + 
                       sinf->yy[1] * sinf->basis[1][i] +
                       sinf->yy[2] * sinf->basis[2][i] + 
                       sinf->yy[3] * sinf->basis[3][i];
        }
    }
}


/*
 * Add a new control point to the control-point buffer,
 * update the spline curve, recalculate if the new point
 * has an x-value less than the previous one.
 *
 * init	-	If TRUE clear the control point buffer
 * x	-	The x-value of the new control point
 * y	-	The y-value of the new control point if 'spline_ymode'
 *		is TRUE, ignored otherwise.
 * nsize -	The number of points used by the spline curve
 * xbuf	-	The original data
 * sbuf	-	The buffer for the spline curve
 */
void psnd_spline_addvalue(MBLOCK *mblk, int init, int x, float y,  
                            int nsize, float *xbuf, float *sbuf)
{
    SPLINE_INFO *sinf = mblk->sinf;
    if (init) 
        sinf->spos = 0;
    if (sinf->spos >= sinf->ssize - 10) {
        sinf->ssize += 10;
        sinf->spoint = (SPOINT*) realloc(sinf->spoint, sinf->ssize *sizeof(SPOINT));
    }
    x--;
    sinf->spoint[sinf->spos].fixed = sinf->ymode;
    sinf->spoint[sinf->spos].x = x;
    if (sinf->ymode)
        sinf->spoint[sinf->spos].y = y;
    else
        sinf->spoint[sinf->spos].y = xbuf[x];
    sinf->spos++;
    /*
     * If new point somewhere in the middle, recalc total spline curve
     */
    if (sinf->spos > 1 && (sinf->spoint[sinf->spos-1].x < sinf->spoint[sinf->spos-2].x))
        psnd_spline_calc(mblk, nsize, xbuf, sbuf);
    else
        spline_add_control_point(sinf, init, sinf->interpolating,
                      sinf->spoint[sinf->spos-1].x, sinf->spoint[sinf->spos-1].y, 
                      nsize, sbuf);
}

/*
 * Add a control point without updating the curve
 */
static void spline_add(SPLINE_INFO *sinf, int init, int x, float y, int fixed)
{
    if (init) 
        sinf->spos = 0;
    if (sinf->spos >= sinf->ssize - 10) {
        sinf->ssize += 10;
        sinf->spoint = (SPOINT*) realloc(sinf->spoint, sinf->ssize *sizeof(SPOINT));
    }
    sinf->spoint[sinf->spos].x     = x;
    sinf->spoint[sinf->spos].y     = y;
    sinf->spoint[sinf->spos].fixed = fixed;
    sinf->spos++;
}

/*
 * Used by qsort, to sort the control points
 */
static int compare(const void *a, const void *b)
{
    SPOINT *sa, *sb;
    sa = (SPOINT*) a;
    sb = (SPOINT*) b;
    return (sa->x - sb->x);
}

/*
 * Calculate the spline curve from the control points
 */
int psnd_spline_calc(MBLOCK *mblk, int nsize, float *xbuf, float *sbuf)
{
    int i, init = TRUE;
    float y;
    SPLINE_INFO *sinf  = mblk->sinf;
    
    if (sinf->spos <= 0)
        return FALSE;
    qsort(sinf->spoint, sinf->spos, sizeof(SPOINT), compare);
    for (i=0;i<sinf->spos;i++) {
        if (sinf->spoint[i].fixed)
            y = sinf->spoint[i].y;
        else
            y = xbuf[sinf->spoint[i].x];
        spline_add_control_point(sinf, init, sinf->interpolating, sinf->spoint[i].x, 
                          y, nsize, sbuf);
        init = FALSE;
    }
    return TRUE;
}

/*
 * Read control points from disk
 */
int psnd_spline_read(MBLOCK *mblk, char *filename, DBLOCK *dat)
{
    FILE *infile;
    char buf[250];
    int ok = FALSE, init = FALSE, result = FALSE;
    SPLINE_INFO *sinf  = mblk->sinf;

    if (!filename) 
        return FALSE;
    if ((infile = fopen(filename,"r")) == NULL) 
        return FALSE;
    while (fgets(buf,250,infile) != NULL) {
        char *p;
        char sep[] = " \r\n\t";
        
        /*
         * Remove comments
         */
        if ((p=strchr(buf, '#')) != NULL)
            *p = '\0';
        strupr(buf);
        p= strtok(buf, sep);
        if (p) {
            if (strcmp(p, "SPLINE_START") == 0) {
                init = TRUE;
                ok = TRUE;
            }
            else if (strcmp(p, "SPLINE_INTERPOLATING") == 0) {
                sinf->interpolating = TRUE;
            }
            else if (strcmp(p, "SPLINE_EXTERPOLATING") == 0) {
                sinf->interpolating = FALSE;
            }
            else if (ok && (strcmp(p, "SPLINE_STOP") == 0)) {
                init = FALSE;
                ok = FALSE;
            }
            else if (ok) {
                int xpos = 0, fixed = 0;
                float ypos = 0.0;
                xpos = psnd_scan_integer(p);
                if ((p= strtok(NULL, sep)) != NULL)
                    ypos = psnd_scan_float(p);
                if ((p= strtok(NULL, sep)) != NULL)
                    fixed = psnd_scan_integer(p);
                spline_add(sinf, init, xpos-1, ypos, fixed);
                init   = FALSE;
                result = TRUE;
            }
        }               
    }
    fclose(infile);
    return result;
}

/*
 * Write control points to disk
 */
static int spline_write(SPLINE_INFO *sinf, char *filename)
{
    FILE *outfile;
    char buf[250];
    int i,ok = FALSE;

    if (!filename) 
        return FALSE;
    if ((outfile = fopen(filename,"w")) == NULL) 
        return FALSE;
    fputs("SPLINE_START\n",outfile);
    if (sinf->interpolating)
        fputs("SPLINE_INTERPOLATING\n",outfile);
    else
        fputs("SPLINE_EXTERPOLATING\n",outfile);
    for (i=0;i<sinf->spos;i++) {
        fprintf(outfile, "%6d %16f  %1d\n", 
            sinf->spoint[i].x+1, sinf->spoint[i].y, sinf->spoint[i].fixed);
    }
    fputs("SPLINE_STOP\n",outfile);
    fclose(outfile);
    return TRUE;
}

/*
 * Calculate a spline curve wih the control points in cpoint_x
 * and the y-values in cpoint_y
 * Return the curve in xwork. 
 * isize =  size of xwork
 * nterms = size of cpoint_x and cpoint_y
 */
static void calc_test_spline(SPLINE_INFO *sinf, float *xwork, int isize, 
                             int *cpoint_x, float *cpoint_y, int nterms,
                             int offset)
{
    int i, interpolating = FALSE;

    spline_add_control_point(sinf, TRUE,  interpolating, 
                              cpoint_x[0]-offset, cpoint_y[0], isize, xwork);
    spline_add_control_point(sinf, FALSE, interpolating, 
                              cpoint_x[0]-offset, cpoint_y[0], isize, xwork);
    spline_add_control_point(sinf, FALSE, interpolating, 
                              cpoint_x[0]-offset, cpoint_y[0], isize, xwork);
    spline_add_control_point(sinf, FALSE, interpolating, 
                              cpoint_x[0]-offset, cpoint_y[0], isize, xwork);

    for (i=0;i<nterms;i++)
        spline_add_control_point(sinf, FALSE, interpolating, 
                                  cpoint_x[i], cpoint_y[i], isize, xwork);
    i = nterms-1;
    spline_add_control_point(sinf, FALSE, interpolating,
                              cpoint_x[i]+offset, cpoint_y[i], isize, xwork);
    spline_add_control_point(sinf, FALSE, interpolating,
                              cpoint_x[i]+offset, cpoint_y[i], isize, xwork);
    spline_add_control_point(sinf, FALSE, interpolating,
                              cpoint_x[i]+offset, cpoint_y[i], isize, xwork);
}


/*
 *
 *   automatic baseline correction with spline
 *   exclude all (x,y) points with y outside window w
 *
 */
void psnd_spline_baseline(MBLOCK *mblk, float *y, float *z,  float *xwork, int npts, 
                           int nt1, float u, float v, float sfac, 
                           float bfac, int *ier)
{
    float w;
    float *cpoint_y;
    int   i, j, k, n, nterms, stepsize, *cpoint_x;
    int   nstart,nstop;
    SPLINE_INFO *sinf  = mblk->sinf;

    *ier=1;
    /*
     * out of loop variables
     */
    nt1 = min(nt1, npts/2-1);
    nt1 = max(nt1, 1);
    nterms   = nt1+1;
    cpoint_x = ivector(0, nterms);
    cpoint_y = fvector(0, nterms);

    /*
     * Initialize control points,
     * evenly spread over the mpts-data
     */
    stepsize    = npts/nt1;
    /*
     * If no user-defined baseline regions, define
     * automatically
     */
    if (!z)
        w=baseline_region(y-1, xwork-1, npts, FALSE);
    k=0;
    /*
     * All control points have their own region.
     * For each point, determine y-value
     */
    for (n=0;n<nterms;n++) {
        nstart  = n * stepsize;
        nstart -= stepsize/2;
        nstop   = nstart + stepsize;
        nstart  = max(0,nstart);
        nstop   = min(nstop,npts-1);
        nstart  = min(nstop-1,nstart);
        /* 
         * exclude all (x,y) points with y outside window w
         * initialize
         */
        for (i=nstart;i<=nstop;i++) {
            /*
             * If no window (z) has been defined, we use our own (w)
             */
            if (!z && xwork[i] > w) 
                continue;
            /*
             * Use user-defined window (z)
             */
            if (z && z[i] == 0.0)
                continue;
            cpoint_y[k]   = y[i];
            cpoint_x[k++] = i;
            break;
        }
    }
    /*
     * Determine last baseline point, to close spline
     */
    for (i=npts-1;i>=0;i--) {
        /*
         * If no window (z) has been defined, we use our own (w)
         */
         if (!z && xwork[i] > w) 
             continue;
        /*
         * Use user-defined window (z)
         */
        if (z && z[i] == 0.0)
            continue;
        cpoint_y[k]   = y[i];
        cpoint_x[k++] = npts-1;
        break;
    }
       
    if (k==0) 
        *ier = 0;
    else {
        calc_test_spline(sinf, xwork, npts, cpoint_x, cpoint_y, k, 100);

        for (j=0;j<npts;j++) 
            y[j] -= xwork[j];
    }

    free_fvector(cpoint_y, 0);
    free_ivector(cpoint_x, 0);

}

/*
 *
 *   Smooth left or right side of a peak.
 *   Choose a lot of control points close to the peak
 *   and only a few in the 'baseline' region.
 *
 */
void psnd_spline_slope(MBLOCK *mblk, float *y, float *xwork, int pos, 
                           int stop, float u, float v, float sfac, 
                           float bfac, int *ier)
{
    float *cpoint_y;
    int   i, k, n, idir, stepsize, *cpoint_x;
    int   npts, nterms, maxstep;
    SPLINE_INFO *sinf  = mblk->sinf;

    *ier=0;
    if (pos > stop)
        idir = -1;
    else if (pos < stop)
        idir = 1;
    else
        return;

    npts = (stop - pos) * idir;
    maxstep     = npts/10;
    if (npts < 40)
        return;
    *ier=1;
    stepsize    = 4;
    for (i=0,nterms=0;i<npts;) {
        nterms++;
        i+=stepsize;
        if (stepsize < maxstep)
            stepsize = (int)(1.5*stepsize);
    }
    nterms++;
    cpoint_x = ivector(0, nterms);
    cpoint_y = fvector(0, nterms);

    /*
     * Initialize control points,
     * non-evenly spread over the mpts-data
     */
    stepsize    = 4;
    n=pos;
    for (i=0,k=0;i<npts;) {
        cpoint_y[k]   = y[n];
        cpoint_x[k++] = i;
        i += stepsize;
        n += idir * stepsize;
        if (stepsize < maxstep)
            stepsize = (int)(1.5*stepsize);
    }
    /*
     * Last point, to close spline
     */
    cpoint_y[k]  = y[stop-idir];
    cpoint_x[k]  = npts;
    if (k==0) 
        *ier = 0;
    else {
        calc_test_spline(sinf, xwork, npts, 
                             cpoint_x, cpoint_y, nterms, 0);
        /*
         * copy the result into the data array
         */
        for (i=pos,k=0;i!=stop;i+=idir) 
             y[i] = xwork[k++];
           
    }

    free_fvector(cpoint_y, 0);
    free_ivector(cpoint_x, 0);

}


typedef enum {
    SPLINE_YMODE=100,
    SPLINE_INTERPOLATING,
    SPLINE_RESET,
    SPLINE_CLOSE,
    SPLINE_CALC,
    SPLINE_READ,
    SPLINE_WRITE,
    SPLINE_MOUSE
} popup_spline_ids;


static void spline_callback(G_POPUP_CHILDINFO *ci)
{
    G_EVENT ui;
    MBLOCK *mblk = (MBLOCK *) ci->userdata;
    DBLOCK *dat = DAT;
    POPUP_INFO *popinf = mblk->popinf + POP_SPLINE;
    SPLINE_INFO *sinf  = mblk->sinf;

    switch (ci->type) {
        case G_CHILD_OK:
        case G_CHILD_CANCEL:
            if (psnd_make_visible(mblk, mblk->info->block_id, S_BUFR2, FALSE))
                psnd_1d_reset_connection(mblk);
            g_menu_enable_item(mblk->info->mousebar_id, ID_MOUSE_SPLINE, FALSE);
            if (mblk->info->mouse_mode1 == MOUSE_BUFFER2)
                psnd_set_cursormode(mblk, -1, 0);   
            popinf->visible = FALSE;
            return;
    }
    switch (ci->id) {
        case SPLINE_YMODE:
            sinf->ymode = ci->item;
            break;
        case SPLINE_INTERPOLATING:
            sinf->interpolating = ci->item;
            break;
        case SPLINE_RESET: {
            int oldmode = sinf->ymode;
            sinf->ymode = FALSE;
            psnd_spline_addvalue(mblk, 1, 1, 0, dat->isize, dat->xreal, dat->xbufr2);
            psnd_spline_addvalue(mblk, 0, 1, 0, dat->isize, dat->xreal, dat->xbufr2);
            psnd_spline_addvalue(mblk, 0, 1, 0, dat->isize, dat->xreal, dat->xbufr2);
            psnd_spline_addvalue(mblk, 0, 1, 0, dat->isize, dat->xreal, dat->xbufr2);
            sinf->ymode = oldmode;
            ui.event = G_COMMAND;
            ui.win_id = mblk->info->win_id;
            ui.keycode = PSND_PL;
            g_send_event(&ui);
            }
            break;
        case SPLINE_CLOSE:{
            int oldmode = sinf->ymode;
            sinf->ymode = FALSE;
            psnd_spline_addvalue(mblk, 0, dat->isize, 0, dat->isize, dat->xreal, dat->xbufr2);
            psnd_spline_addvalue(mblk, 0, dat->isize, 0, dat->isize, dat->xreal, dat->xbufr2);
            psnd_spline_addvalue(mblk, 0, dat->isize, 0, dat->isize, dat->xreal, dat->xbufr2);
            sinf->ymode = oldmode;
            ui.event = G_COMMAND;
            ui.win_id = mblk->info->win_id;
            ui.keycode = PSND_PL;
            g_send_event(&ui);
            }
            break;
        case SPLINE_CALC:
            psnd_spline_calc(mblk, dat->isize, dat->xreal, dat->xbufr2);
            ui.event = G_COMMAND;
            ui.win_id = mblk->info->win_id;
            ui.keycode = PSND_PL;
            g_send_event(&ui);
            break;
        case SPLINE_READ: {
                char *filename = psnd_getfilename(mblk,"Read Spline data","*.spn");
                if (filename)  {
                    psnd_spline_read(mblk, filename, dat);
                    psnd_spline_calc(mblk, dat->isize, dat->xreal, dat->xbufr2);
                }
            }
            break;
        case SPLINE_WRITE: {
                char *filename = psnd_savefilename(mblk,"Write Spline data","*.spn");
                if (filename) {
                    spline_write(sinf,filename);
                    PAR->nbase = TRUE;
                    PAR->ibase = BASSP1;
                }
            }
            break;

        case SPLINE_MOUSE:
            {
                psnd_set_cursormode(mblk, 0, MOUSE_BUFFER2);
            }
            break;
    }
}

/*
 * Popup menu to build a spline curve
 */
int psnd_spline_popup(MBLOCK *mblk)
{
    int i, ok;
    char *radio[]  = { "Follow spectrum", "Follow cursor"," " } ;
    char *radio2[] = { "Exterpolating", "Interpolating"," " } ;
    G_POPUP_CHILDINFO ci[20];
    int id;
    POPUP_INFO *popinf = mblk->popinf + POP_SPLINE;
    SPLINE_INFO *sinf  = mblk->sinf;

    if (popinf->visible)
        return FALSE;
    popinf->visible = TRUE;
    if (!popinf->cont_id) {
        int cont_id;

        cont_id = g_popup_container_open(mblk->info->win_id, "Spline Fitting", 
                      G_POPUP_KEEP|G_POPUP_SINGLEBUTTON);
        popinf->cont_id = cont_id;
        id=0;
   
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_OPTIONMENU;
        ci[id].id    = SPLINE_YMODE;
        ci[id].item_count = 2;
        ci[id].item = sinf->ymode;
        ci[id].data = radio;
        ci[id].horizontal = TRUE;
        ci[id].label = "Y mode";
        ci[id].frame = TRUE;
        ci[id].func  = spline_callback;
        ci[id].userdata = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_OPTIONMENU;
        ci[id].id    = SPLINE_INTERPOLATING;
        ci[id].item_count = 2;
        ci[id].item = sinf->interpolating;
        ci[id].data = radio2;
        ci[id].horizontal = TRUE;
        ci[id].label = "Init mode";
        ci[id].frame = TRUE;
        ci[id].func  = spline_callback;
        ci[id].userdata = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_PUSHBUTTON;
        ci[id].id    = SPLINE_RESET;
        ci[id].label = "Reset Spline";
        ci[id].func  = spline_callback;
        ci[id].userdata = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_PUSHBUTTON;
        ci[id].id    = SPLINE_CLOSE;
        ci[id].label = "Close Spline";
        ci[id].func  = spline_callback;
        ci[id].userdata = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_PUSHBUTTON;
        ci[id].id    = SPLINE_CALC;
        ci[id].label = "Re-calc Spline";
        ci[id].func  = spline_callback;
        ci[id].userdata = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_PUSHBUTTON;
        ci[id].id    = SPLINE_READ;
        ci[id].label = "Read Spline";
        ci[id].func  = spline_callback;
        ci[id].userdata = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_PUSHBUTTON;
        ci[id].id    = SPLINE_WRITE;
        ci[id].label = "Write Spline";
        ci[id].func  = spline_callback;
        ci[id].userdata = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_PUSHBUTTON;
        ci[id].id    = SPLINE_MOUSE;
        ci[id].label = "Grab Mouse Button 1";
        ci[id].func  = spline_callback;
        ci[id].userdata = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_OK;
        ci[id].func  = spline_callback;
        ci[id].userdata = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_CANCEL;
        ci[id].func  = spline_callback;
        ci[id].userdata = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

    }
    if (psnd_make_visible(mblk, mblk->info->block_id, S_BUFR2, TRUE)) {
        static G_EVENT ui;

        psnd_1d_reset_connection(mblk);
        ui.event   = G_COMMAND;
        ui.win_id  = mblk->info->win_id;
        ui.keycode = PSND_PL;
        g_send_event(&ui);
    }
    g_menu_enable_item(mblk->info->mousebar_id, ID_MOUSE_SPLINE, TRUE);
    g_popup_container_show(popinf->cont_id) ;
    return TRUE;
}

