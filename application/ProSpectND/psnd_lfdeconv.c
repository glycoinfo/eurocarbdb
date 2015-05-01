/*
*     psnd_lfdeconv.c
*
*     1998, Albert van Kuik
*
*     Automatic curve fitting of overlapping lines
*     Minimization by Levenberg-Marquardt routine
*/


#include <stdio.h>
#include <stdlib.h>
#include <ctype.h>
#include <string.h>
#include <math.h>
#include <sys/stat.h>
#include <unistd.h>
#include <assert.h>
#include "genplot.h"
#include "psnd.h"
#include "mathtool.h"

#define MAXFIT		100
#define MAXID		30
#define MAXIDPLUS	31

static float  peakx(float precision, float position, float width, 
                    float height, float mix, int *left, int *right,
                    int flag, float *farray, int il, int ir, int lineshape);
/*
 * Initiate parameters
 */
static void lfdec_init(LFDECONV_TYPE *lfdec, SBLOCK *spar, SBLOCK *spar2, PBLOCK *par,
                 DBLOCK *dat, DBLOCK *dat2)
{
    int i;
    
    lfdec->spar   = spar;
    lfdec->spar2  = spar2;
    lfdec->par    = par;
    lfdec->dat    = dat;
    lfdec->dat2   = dat2;
    lfdec->warray = dat2->ximag;
    lfdec->farray = dat2->xreal;
    if (!lfdec->init) {
        lfdec->init              = TRUE;
        lfdec->mxdcnv            = MAXFIT;
        for (i=0;i<FITVAL;i++) {
            lfdec->array[i] = (float*) malloc (sizeof(float) * lfdec->mxdcnv);
            assert(lfdec->array[i]);
            lfdec->ifitar[i] = (int*) malloc (sizeof(int) * lfdec->mxdcnv);
            assert(lfdec->ifitar[i]);
        }
        lfdec->linenb = 1;
        lfdec->linctr = 0;
        lfdec->nparam = 0;
        memset(lfdec->warray, 0, dat->isize * sizeof(float));
        memset(lfdec->farray, 0, dat->isize * sizeof(float));
 
        lfdec->wprec   = 20.0;
        lfdec->maxcal  = 12;
        lfdec->scaint  = 1.e7;
        lfdec->sfract  = 0.001;
        lfdec->hfract  = 0.01;
        lfdec->wfract  = 0.01;
        lfdec->lineshape = LORENTZIAN;
    } 

    lfdec->il = 1;
    lfdec->ir = dat->isize;
    if (lfdec->scaint == 0) lfdec->scaint=1.;
}

/*
 * Set all fitted data to zero, and set no. of peaks to 0
 */
static void lfdec_reset(LFDECONV_TYPE *lfdec, DBLOCK *dat)
{
    memset(lfdec->warray, 0, dat->isize * sizeof(float));
    memset(lfdec->farray, 0, dat->isize * sizeof(float));
    lfdec->linctr = 0;
}



#define TWO_TIMES_SQRT_LN_2 1.66510922231539537641 

/*
  f(x) = H * exp[ - {(x-x0)/W}^2 * 4 * ln(2)]
 */
void fgauss(float x, double a[], double *y, double dyda[], int na)
{
    int i;
    double fac,ex,arg,width;

    *y = 0.0;
    for (i=1;i<na;i+=3) {
        width = a[i+2] / TWO_TIMES_SQRT_LN_2; /*(2*sqrt(log(2)));*/
        arg = (x-a[i+1])/width;
        ex = exp(-arg*arg);
        fac = a[i] * ex * 2.0 * arg;
        *y += a[i] * ex;
        dyda[i] = ex;
        dyda[i+1] = fac / width;
        dyda[i+2] = fac * arg / width;
    }
}

/*
 f(x) = H / [4 * {(x-x0)/W}^2 + 1]
 */
void florentz(float x, double a[], double *y, double dyda[], int na)
{
    int i;
    double fac,arg;

    *y = 0.0;
    for (i=1;i<na;i+=3) {
        arg = (x-a[i+1])/a[i+2];
        fac = 1.0 / (4*arg*arg + 1);
        *y += a[i] * fac;
        dyda[i]   = fac;
        dyda[i+1] = 8 * a[i] * fac * fac * arg / a[i+2];
        dyda[i+2] = dyda[i+1] * arg;
    }
}


void fmixed(float x, double a[], double *y, double dyda[], int na)
{
    int i;
    double fac,ex,arg,width;

    *y = 0.0;
    for (i=1;i<na;i+=4) {
        arg = (x-a[i+1])/a[i+2];
        ex = 4*arg*arg;
        fac = 1.0 / (ex + 1);
        *y += a[i+3] * a[i] * fac;
        dyda[i]   = a[i+3] * fac;
        dyda[i+1] = a[i+3] * 8 * a[i] * fac * fac * arg / a[i+2];
        dyda[i+2] = dyda[i+1] * arg;
        dyda[i+3] = a[i] * fac;
    }
    for (i=1;i<na;i+=4) {
        width = a[i+2] / TWO_TIMES_SQRT_LN_2; 
        arg = (x-a[i+1])/width;
        ex = exp(-arg*arg);
        fac = (1.0 - a[i+3]) * a[i] * ex * 2.0 * arg;
        *y += (1.0 - a[i+3]) * a[i] * ex;
        dyda[i]   += (1.0 - a[i+3]) * ex;
        dyda[i+1] += fac / width;
        dyda[i+2] += fac * arg / width;
        dyda[i+3] += -a[i] * ex;
    }
}

#define LDCONFSIZE	(FITVAL * lfdec->mxdcnv)


static void lfdec_optimizeline(LFDECONV_TYPE *lfdec, int is_multi, 
                                           PBLOCK *par, DBLOCK *dat)
{
    int size = sizeof(float) * LDCONFSIZE;
    void (*funcs)(float, double [], double *, double [], int);
    double alamda,chisq,**covar,**alpha,*aa,**constr;
    float *xx,*yy,*sig;
    int i,k,j,ma,ndata,mfit,*lista,maxloop,ier,**iconstr;

    maxloop = lfdec->maxcal;
    lfdec->nparam = 0;
    aa    = dvector(1, size);
    for (i=0;i<lfdec->linctr;i++) {
        /*
         * Skip junked lines
         */
        if (lfdec->ifitar[POS][i] == JUNK)
            continue;
        lfdec->nparam++;
        aa[lfdec->nparam] = lfdec->array[HIG][i];
        lfdec->nparam++;
        aa[lfdec->nparam] = lfdec->array[POS][i];
        lfdec->nparam++;
        aa[lfdec->nparam] = lfdec->array[WID][i];
        if (aa[lfdec->nparam] < 1.0)
            aa[lfdec->nparam] = 1.0;
        if (lfdec->lineshape == MIXED) {
            lfdec->nparam++;
            aa[lfdec->nparam]    = lfdec->array[MIX][i];
        }
    }
    if (lfdec->nparam == 0)
        goto out;
    if (lfdec->lineshape == GAUSSIAN) 
        funcs = fgauss;
    else if (lfdec->lineshape == LORENTZIAN) 
        funcs = florentz;
    else
        funcs = fmixed;
    ma    = lfdec->nparam;
    ndata = lfdec->ir - lfdec->il + 1;
    xx    = fvector(1, ndata);
    yy    = fvector(1, ndata);
    sig   = fvector(1, ndata);
    lista = ivector(1, ma);
    alpha = dmatrix(1, ma, 1, ma);
    covar = dmatrix(1, ma, 1, ma);
    constr  = dmatrix(1, ma, 1, 2);
    iconstr = imatrix(1, ma, 1, 2);
    mfit=0;
    for (i=0,j=1;i<lfdec->linctr;i++) {
        /*
         * Skip junked lines
         */
        if (lfdec->ifitar[POS][i] == JUNK)
            continue;
        mfit++;
        lista[mfit] = (lfdec->ifitar[HIG][i] == ON) ;
        mfit++;
        lista[mfit] = (lfdec->ifitar[POS][i] == ON) ;
        mfit++;
        lista[mfit] = (lfdec->ifitar[WID][i] == ON) ;
        if (lfdec->lineshape == MIXED) {
            mfit++;
            lista[mfit] = 1; 
        }
    }
    for (i=1;i<=ndata;i++) {
        xx[i]  = i+(lfdec->il-1)-1;
        yy[i]  = lfdec->dat->xreal[i+(lfdec->il-1)-1];
        sig[i] = 1.0;
    }
    /*
     * Constraints
     */
    for (i=1;i<=ma;i++) {
        iconstr[i][1] = 0;
        iconstr[i][2] = 0;
    }
    if (lfdec->lineshape == MIXED) {
        for (k=1;k<ma;k+=4) {
            /*
             * Left position >= il
             */
            iconstr[k+1][1] = 1;
            constr[k+1][1]  = lfdec->il;
            /*
             * Right position <= ir
             */
            iconstr[k+1][2] = 1;
            constr[k+1][2]  = lfdec->ir;
            /*
             * Width >= 1 channel
             */
            iconstr[k+2][1] = 1;
            constr[k+2][1]  = 1.0;
            /*
             * Mix between 1 and 0
             */
            iconstr[k+3][1] = 1;
            constr[k+3][1]  = 0.0;
            iconstr[k+3][2] = 1;
            constr[k+3][2]  = 1.0;
        }
    }
    else {
        for (k=1;k<ma;k+=3) {
            iconstr[k+1][1] = 1;
            constr[k+1][1]  = lfdec->il;
            iconstr[k+1][2] = 1;
            constr[k+1][2]  = lfdec->ir;
            iconstr[k+2][1] = 1;
            constr[k+2][1]  = 1.0;
        }
    }
    alamda = -1.0;
    i=0;
    chisq=0;
    ier = 1;
/* printf("alamda= "); */
    while (i < maxloop && alamda != 0.0) {
        if (i == maxloop-1 || alamda > 1e6)
            alamda = 0.0;
        i++;
        ier = mrqmin(xx, yy, sig, ndata, 
                     aa, ma, lista, covar, 
                     alpha, &chisq,funcs,&alamda,
                     iconstr, constr);
/*
printf(" %g", alamda);
fflush(stdout);
*/
        if (ier != 1)
            goto out;
    }
/* printf("\n"); */
    free_fvector(xx, 1);
    free_fvector(yy, 1);
    free_fvector(sig, 1);
    free_ivector(lista, 1);
    free_dmatrix(alpha, 1, ma, 1, ma);
    free_dmatrix(covar, 1, ma, 1, ma);
    free_dmatrix(constr, 1, ma, 1, 2);
    free_imatrix(iconstr, 1, ma, 1, 2);
    k=0;
    for (i=0;i<lfdec->linctr;i++) {
        /*
         * Skip junked lines
         */
        if (lfdec->ifitar[POS][i] == JUNK)
            continue;
        peakx(lfdec->wprec,lfdec->array[POS][i],lfdec->array[WID][i],
              lfdec->array[HIG][i], lfdec->array[MIX][i], 
              NULL, NULL, PEAKSUBTRACT,
              lfdec->farray,lfdec->il,lfdec->ir,lfdec->lineshape);
        k++;
        lfdec->array[HIG][i]  = aa[k];
        k++;
        lfdec->array[POS][i]  = aa[k];
        k++;
        lfdec->array[WID][i]  = aa[k];
        if (lfdec->lineshape == MIXED) {
            k++;
            lfdec->array[MIX][i] = aa[k];
        }
        /*
         * .....replace peak
         */
        peakx(lfdec->wprec,lfdec->array[POS][i],lfdec->array[WID][i],
              lfdec->array[HIG][i], lfdec->array[MIX][i], NULL, NULL, PEAKADD,
              lfdec->farray,lfdec->il,lfdec->ir,lfdec->lineshape);

    }
out:
    free_dvector(aa, 1);
}

/*
 * Reset a line to zero
 */
static void lfdec_junkline(MBLOCK *mblk)
{
    LFDECONV_TYPE *lfdec = mblk->lfdec;

    if (psnd_ivalin(mblk,"Hide line #",1, &(lfdec->linenb)) == 0)
        return;
    if (lfdec->linenb <= 0 || lfdec->linenb > lfdec->linctr) {
        psnd_printf(mblk," illegal line #%d\n", lfdec->linenb);
        return;
    }
    /*
     * Line is already junked
     */
    if (lfdec->ifitar[POS][lfdec->linenb-1] == JUNK)
        return;

    peakx(lfdec->wprec,
          lfdec->array[POS][lfdec->linenb-1],
          lfdec->array[WID][lfdec->linenb-1],
          lfdec->array[HIG][lfdec->linenb-1],
          lfdec->array[MIX][lfdec->linenb-1], 
          NULL, NULL,
          PEAKSUBTRACT,
          lfdec->farray,
          lfdec->il,
          lfdec->ir,
          lfdec->lineshape);

    lfdec->ifitar[POS][lfdec->linenb-1] = JUNK;
    lfdec->ifitar[WID][lfdec->linenb-1] = OFF;
    lfdec->ifitar[HIG][lfdec->linenb-1] = OFF;
}

/*
 * Recalculate the spectrum
 */
static void lfdec_recalc(LFDECONV_TYPE *lfdec, DBLOCK *dat)
{
    int i;
    
    memset(lfdec->warray, 0, dat->isize * sizeof(float));
    memset(lfdec->farray, 0, dat->isize * sizeof(float));
    for (i=0;i<lfdec->linctr;i++) {

        peakx(lfdec->wprec,
              lfdec->array[POS][i],
              lfdec->array[WID][i],
              lfdec->array[HIG][i],
              lfdec->array[MIX][i], 
              NULL, NULL,
              PEAKADD,
              lfdec->farray,
              lfdec->il,
              lfdec->ir,
              lfdec->lineshape);
    }
}


static int popup_values(MBLOCK *mblk, int line, float *pos, 
                        float *width, float *height)
{
    int cont_id;
    int id=-1;
    char *label;
    G_POPUP_CHILDINFO ci[MAXIDPLUS];

    label = psnd_sprintf_temp("Edit line %d", line);
    cont_id = g_popup_container_open(mblk->info->win_id, label,
                                     G_POPUP_WAIT);
    id++;
    label = psnd_sprintf_temp("%.2f", *pos);
    g_popup_init_info(&(ci[id]));
    ci[id].type          = G_CHILD_TEXTBOX;
    ci[id].id            = id;
    ci[id].item_count    = 40;
    ci[id].items_visible = 10;
    ci[id].label         = label;
    ci[id].frame         = TRUE;
    ci[id].title         = "Position (Hz)";
    g_popup_add_child(cont_id, &(ci[id]));

    id++;
    label = psnd_sprintf_temp("%.2f", max(1,*width));
    g_popup_init_info(&(ci[id]));
    ci[id].type          = G_CHILD_TEXTBOX;
    ci[id].id            = id;
    ci[id].item_count    = 40;
    ci[id].items_visible = 10;
    ci[id].label         = label;
    ci[id].frame         = TRUE;
    ci[id].title         = "Width (Hz)";
    g_popup_add_child(cont_id, &(ci[id]));

    id++;
    label = psnd_sprintf_temp("%.2f", *height);
    g_popup_init_info(&(ci[id]));
    ci[id].type          = G_CHILD_TEXTBOX;
    ci[id].id            = id;
    ci[id].item_count    = 40;
    ci[id].items_visible = 10;
    ci[id].label         = label;
    ci[id].frame         = TRUE;
    ci[id].title         = "Height";
    g_popup_add_child(cont_id, &(ci[id]));

    assert(id<MAXID);        

    if (g_popup_container_show(cont_id)) {
        id=0;
        *pos    = psnd_scan_float(ci[id].label);id++;
        *pos    = max(1, *pos);
        *width  = psnd_scan_float(ci[id].label);id++;
        *width  = max(1, *width);
        *height = psnd_scan_float(ci[id].label);id++;
        return TRUE;
    }
    return FALSE;
}

/*
 * Select line
 */
static int popup_select_line(int win_id, LFDECONV_TYPE *lfdec, int select)
{
    int cont_id, result = 0;
    int i,k,id,ok,active[MAXFIT];
    char *list[2],*label;
    G_POPUP_CHILDINFO ci[2];

    if (lfdec->linctr<1)
        return 0;
        
    label = psnd_sprintf_temp("New line");
    cont_id = g_popup_container_open(win_id, "Select Line",
                       G_POPUP_WAIT);
    list[0] = label;

    id=0;
    g_popup_init_info(&(ci[id]));
    ci[id].type          = G_CHILD_LISTBOX;
    ci[id].id            = id;
    ci[id].item_count    = 1;
    ci[id].data          = list;
    ci[id].item          = 0;
    ci[id].items_visible = 10;
    g_popup_add_child(cont_id, &(ci[id]));

    for (i=0,k=0;i<lfdec->linctr;i++) {
        if (lfdec->ifitar[POS][i] == JUNK)
            continue;
        active[k++] = i+1;
        label = psnd_sprintf_temp("%d, pos = %d (chan)", i+1, 
                                   (int)lfdec->array[POS][i]);
        g_popup_append_item(cont_id, id, label);
    }
    g_popup_set_selection(cont_id, id, select);
    ok = g_popup_container_show(cont_id);
    if (ok) {
        if (ci[id].item==0)
            /*
             * New line
             */
            result = lfdec->linctr+1;
        else
            /*
             * Old line
             */
            result = active[ci[id].item-1];
    }
    return result;
}

/*
 *   enter new line or change line 
 */
static int lfdec_addline(MBLOCK *mblk, int xpos, PBLOCK *par, 
                         DBLOCK *dat)
{
    LFDECONV_TYPE *lfdec = mblk->lfdec;
    int linenb_store = lfdec->linenb;
    int is_oldline = FALSE;
    float tempos, temhig, temwid, temmix;

    tempos = lfdec->array[POS][lfdec->linenb-1];
    temwid = lfdec->array[WID][lfdec->linenb-1];
    temhig = lfdec->array[HIG][lfdec->linenb-1];
    temmix = lfdec->array[MIX][lfdec->linenb-1];
    /*
     * Peak added by cursor
     */
    if (xpos >= 0) {
        float hz,swfac;
        if (lfdec->linctr >= lfdec->mxdcnv) {
            psnd_printf(mblk," lfdcnv -- no more lines ! mxdcnv = %d\n",
                lfdec->mxdcnv);
            return FALSE;
        }
        tempos = max(0,xpos-1);
        temhig = dat->xreal[xpos];
        swfac = (float)(dat->isize-1)/par->swhold;
        hz = max(1,temwid) / swfac;
        if (psnd_rvalin(mblk,"Width  (Hz) ",1,&(hz)) == 0)
            return FALSE;
        temwid = fabs(hz * swfac);
        temwid = max(1,temwid);
        lfdec->linenb = lfdec->linctr+1;
        temmix = 0.5;
    }
    /*
     * Peak added manually
     */
    else {
        float pos, width, height, swfac;
        lfdec->linenb = max(1, lfdec->linenb);
        lfdec->linenb = min(lfdec->linctr+1, lfdec->linenb);
        if ((lfdec->linenb=popup_select_line(mblk->info->win_id, lfdec, lfdec->linenb))==0) {
            lfdec->linenb = linenb_store;
            return FALSE;
        }
        /*
         *  check for old line, recycle junked line
         */
        if (lfdec->linenb <= lfdec->linctr) {
            is_oldline = TRUE;
            if (lfdec->array[HIG][lfdec->linenb-1] != 0.) {
                tempos = lfdec->array[POS][lfdec->linenb-1];
                temwid = lfdec->array[WID][lfdec->linenb-1];
                temhig = lfdec->array[HIG][lfdec->linenb-1];
                temmix = lfdec->array[MIX][lfdec->linenb-1];
            }    
            /*
             * Remove junk label
             */
            if (lfdec->ifitar[POS][lfdec->linenb-1] == JUNK)
                lfdec->ifitar[POS][lfdec->linenb-1] = OFF;
        }
        else
            temmix = 0.5;
        /*
         * ... new entry
         */
        if (lfdec->linctr >= lfdec->mxdcnv) {
            psnd_printf(mblk," lfdcnv -- no more lines ! mxdcnv = %d\n",
                lfdec->mxdcnv);
            return FALSE;
        }
        swfac  = (float)(dat->isize-1)/par->swhold;
        pos = psnd_calc_pos(tempos, 
                             par->xref, 
                             par->aref, 
                             par->bref,
                             dat->irc, 
                             TRUE, 
                             AXIS_UNITS_HERTZ, 
                             dat->isize,
                             par->swhold, 
                             par->sfd, 
                             NULL, 
                             NULL);
        width  = temwid / swfac;
        height = temhig;
        if (popup_values(mblk, lfdec->linenb, &pos, &width, &height)==0)
            return FALSE;
        /*
         *  old line, first subtract it 
         */
        if (is_oldline)
            peakx(lfdec->wprec,
                  tempos,
                  temwid,
                  temhig,
                  temmix, 
                  NULL, NULL,
                  PEAKSUBTRACT,
                  lfdec->farray,
                  lfdec->il,
                  lfdec->ir,
                  lfdec->lineshape);
        tempos = psnd_calc_channels(pos, 
                                     par->xref, 
                                     par->aref, 
                                     par->bref,
                                     dat->irc, 
                                     TRUE, 
                                     AXIS_UNITS_HERTZ, 
                                     dat->isize,
                                     par->swhold, 
                                     par->sfd);

        temwid = width * swfac;
        temhig = height;
    }
    if (!is_oldline)
        lfdec->linctr++;
    peakx(lfdec->wprec,
          tempos,
          temwid,
          temhig,
          temmix, 
          NULL, NULL,
          PEAKADD,
          lfdec->farray,
          lfdec->il,
          lfdec->ir,
          lfdec->lineshape);
    lfdec->array[POS][lfdec->linenb-1] = tempos;
    lfdec->array[WID][lfdec->linenb-1] = temwid;
    lfdec->array[HIG][lfdec->linenb-1] = temhig;
    lfdec->array[MIX][lfdec->linenb-1] = temmix;
    if (lfdec->linenb == lfdec->linctr) {
        int j;
        for (j=0;j<3;j++)
           lfdec->ifitar[j][lfdec->linctr-1] = ON;
    }
    return TRUE;
}

/*
 *  List all lines 
 */
static void lfdec_listlines(MBLOCK *mblk)
{
    LFDECONV_TYPE *lfdec = mblk->lfdec;
    int i;
    float hz, width, xintegr, ppm, swfac;
    
    swfac = (float)(lfdec->dat->isize-1)/lfdec->par->swhold;
    if (lfdec->lineshape == MIXED)
        psnd_printf(mblk," line     pos.(hz)   pos.(ppm)    width      height     integr.  mix(L)\n");
    else
        psnd_printf(mblk," line     pos.(hz)   pos.(ppm)    width      height     integr.\n");
    for (i=0;i<lfdec->linctr;i++) {
        /*
         * Skip junked lines
         */
        if (lfdec->ifitar[POS][i] == JUNK)
            continue;
        hz = psnd_calc_pos(lfdec->array[POS][i], 
                            lfdec->par->xref, 
                            lfdec->par->aref, 
                            lfdec->par->bref,
                            lfdec->dat->irc, 
                            TRUE, 
                            AXIS_UNITS_HERTZ, 
                            lfdec->dat->isize,
                            lfdec->par->swhold, 
                            lfdec->par->sfd, 
                            NULL, 
                            NULL);

        width = lfdec->array[WID][i] / swfac;
        xintegr = peakx(lfdec->wprec,
                        lfdec->array[POS][i],lfdec->array[WID][i],
                        lfdec->array[HIG][i], lfdec->array[MIX][i],
                        NULL, NULL, PEAKCALC,
                        lfdec->farray,lfdec->il,lfdec->ir,
                        lfdec->lineshape);
        xintegr /= lfdec->scaint;
        ppm   = hz/lfdec->par->sfd;
        if (lfdec->lineshape == MIXED)
            psnd_printf(mblk," %2d    %10.3f %10.3f %8.3g %13.3g %9.3g %8.3g\n" ,
                i+1,
                hz,
                ppm,
                width,
                lfdec->array[HIG][i],
                xintegr,
                lfdec->array[MIX][i]);
        else
            psnd_printf(mblk," %2d    %10.3f %10.3f %8.3g %13.3g %9.3g\n" ,
                i+1,
                hz,
                ppm,
                width,
                lfdec->array[HIG][i],
                xintegr);
    }
}


/*
 *  display difference spectrum 
 */
static void lfdec_diffspec(LFDECONV_TYPE *lfdec, SBLOCK *spar, DBLOCK *dat)
{
    int i;
    for (i=0;i<dat->isize;i++)
        lfdec->warray[i] = dat->xreal[i] - lfdec->farray[i];
}


/*
 * display synthesized spectrum
 */
static void lfdec_fitspec(MBLOCK *mblk)
{
    static G_EVENT ui;

    ui.event   = G_COMMAND;
    ui.win_id  = mblk->info->win_id;
    ui.keycode = PSND_PL;
    g_send_event(&ui);
}

/*
 * Add line with cursor
 */
void psnd_linefit(MBLOCK *mblk, int xpos)
{
    LFDECONV_TYPE *lfdec = mblk->lfdec;
    POPUP_INFO *popinf   = mblk->popinf + POP_LFDEC;
    if (!popinf->visible)
        return;

    if (lfdec_addline(mblk, xpos, lfdec->par, lfdec->dat))
        lfdec_fitspec(mblk);
}

/*
 * Plot all the fitted peaks separately, but in one object
 */
static void show_fitpeaks(MBLOCK *mblk)
{
    LFDECONV_TYPE *lfdec = mblk->lfdec;
    int i,pos,ok=FALSE;
    char *label;
    
    for (i=0;i<lfdec->linctr;i++) {
        int left, right;
        /*
         * Skip junked lines
         */
        if (lfdec->ifitar[POS][i] == JUNK)
            continue;
        ok = TRUE;
        memset(lfdec->warray,0,sizeof(float)*lfdec->dat->isize);
        /*
         * .....calc peak
         */
        peakx(lfdec->wprec,lfdec->array[POS][i],lfdec->array[WID][i],
              lfdec->array[HIG][i], lfdec->array[MIX][i], &left, &right, PEAKADD,
              lfdec->warray,lfdec->il,lfdec->ir,lfdec->lineshape);
        psnd_plot_one_array(mblk, lfdec->warray, left, right, S_IMAG,
                             AUTO_SCALE_OFF_XY, TRUE, lfdec->spar2);
        g_append_object(lfdec->spar2[S_IMAG].obj_id);
        pos = max(1,(int)lfdec->array[POS][i]-2);
        g_moveto(lfdec->array[POS][i],
                 lfdec->warray[pos]);
        label = psnd_sprintf_temp("%d", i+1);
        g_label(label);
        g_close_object(lfdec->spar2[S_IMAG].obj_id);

    }
    if (ok) {
        memset(lfdec->warray,0,sizeof(float)*lfdec->dat->isize);
        g_clear_viewport();
        g_plotall();
    }
}

/*
 * list the fit variables for multiline fitting
 */
static void lfdec_multilist(MBLOCK *mblk)
{
    LFDECONV_TYPE *lfdec = mblk->lfdec;
    int i;
    char *opt[] = { "off", "on "};
    
    psnd_printf(mblk,"  Line no.  position width height\n");
    for (i=0;i<lfdec->linctr;i++) {
        /*
         * Skip junked lines
         */
        if (lfdec->ifitar[POS][i] == JUNK)
            continue;
        psnd_printf(mblk,"  %d          %s      %s   %s\n",
                    i+1, 
                    opt[lfdec->ifitar[POS][i]],
                    opt[lfdec->ifitar[WID][i]],
                    opt[lfdec->ifitar[HIG][i]]);
    }
}

/*
 * reset the fit variables for multiline fitting
 */
static void lfdec_multireset(LFDECONV_TYPE *lfdec)
{
    int i,j;

    for (i=0;i<lfdec->linctr;i++) {
        /*
         * Skip junked lines
         */
        if (lfdec->ifitar[POS][i] == JUNK)
            continue;
        for (j=0;j<3;j++)
           lfdec->ifitar[j][i] = ON;
   }
}


/*
 * add/subract peak
 */
static float peakx(float precision, float position, float width, 
                   float height, float mix, int *left, int *right,
                   int flag, float *farray, int il, int ir, int lineshape)
{
    float th, wsq, a, result = 0.0;
    int i, iw1, iw2, isw;

    if (width == 0.0 || height == 0.0)
        return result;
    if (flag == PEAKADD || flag == PEAKCALC)
        a = 1.0;
    else if (flag == PEAKSUBTRACT)
        a = -1.0;
    width = fabs(width);
    isw = 0.5 * width * precision;
    iw1 = max(il,((int)position)-isw);
    iw2 = min(ir,((int)position)+isw);
    if (left)
        *left = iw1;
    if (right)
        *right = iw2;
    if (iw2 < iw1)
        return result;
    if (lineshape == LORENTZIAN || lineshape == MIXED) {   
        float arg;
        wsq = 0.25 * width*width;
        th  = height * wsq * a;
        if (lineshape == MIXED)
            th *= mix;
        if (flag == PEAKCALC)
            for (i=iw1-1;i<iw2;i++) {
                arg = (float)i-position;
                result += th / (wsq + arg*arg);
            }
        else
            for (i=iw1-1;i<iw2;i++) {
                arg = (float)i-position;
                farray[i] += th / (wsq + arg*arg);
            }
    }
    if (lineshape == GAUSSIAN || lineshape == MIXED) {
        float sigma = width /TWO_TIMES_SQRT_LN_2; /*(2*sqrt(log(2)));*/
        float ssigma = sigma*sigma;
        float arg;
        th  = a * height;
        if (lineshape == MIXED)
            th *= 1.0-mix;
        if (flag == PEAKCALC)
            for (i=iw1-1;i<iw2;i++) {
                arg = (float)i-position;
                result += th * exp(-(arg*arg)/ssigma);
            }
        else
            for (i=iw1-1;i<iw2;i++) {
                arg = (float)i-position;
                farray[i] += th * exp(-(arg*arg)/ssigma);
            }
    }
    return result;
}



static void lfdec_multisetup(MBLOCK *mblk)
{
    LFDECONV_TYPE *lfdec = mblk->lfdec;
    char *labels[] = { "Position", "Width", "Height"};
    char *subtitle;
    int cont_id;
    int i, *select;
    G_POPUP_CHILDINFO *ci;

    if (lfdec->linctr == 0)
        return;
    cont_id = g_popup_container_open(mblk->info->win_id, "Setup Iteration Options",
                                     G_POPUP_WAIT);

    select = (int*) malloc(sizeof(int) * 3 * lfdec->linctr);
    assert(select);
    ci = (G_POPUP_CHILDINFO *) malloc(sizeof(G_POPUP_CHILDINFO) * lfdec->linctr);
    assert(ci);

    for (i=0;i<lfdec->linctr;i++) {
        select[3*i]   = lfdec->ifitar[POS][i];
        select[3*i+1] = lfdec->ifitar[WID][i];
        select[3*i+2] = lfdec->ifitar[HIG][i];
        /*
         * Skip junked lines
         */
        if (lfdec->ifitar[POS][i] == JUNK)
            continue;
        subtitle = psnd_sprintf_temp("Line %d", i+1);
        g_popup_init_info(&(ci[i]));
        ci[i].type       = G_CHILD_CHECKBOX;
        ci[i].id         = i;
        ci[i].title      = subtitle;
        ci[i].frame      = TRUE;
        ci[i].item_count = 3;
        ci[i].data       = labels;
        ci[i].select     = &(select[3*i]);
        ci[i].horizontal = TRUE;
        g_popup_add_child(cont_id, &(ci[i]));
    }
    if (g_popup_container_show(cont_id)) {
        for (i=0;i<lfdec->linctr;i++) {
            lfdec->ifitar[POS][i] = select[3*i];
            lfdec->ifitar[WID][i] = select[3*i+1];
            lfdec->ifitar[HIG][i] = select[3*i+2];
        }
    }
    free(ci);
    free(select);
}


/*
 * Edit parameters
 */
static int lfdec_setup(MBLOCK *mblk)
{
    LFDECONV_TYPE *lfdec = mblk->lfdec;
    int cont_id;
    int i,id;
    char *label;
    G_POPUP_CHILDINFO ci[MAXIDPLUS];
    char *shape[] = { "Lorentzian", "Gaussian", "Mixed" };

    cont_id = g_popup_container_open(mblk->info->win_id, "Setup Linefit",
                                     G_POPUP_WAIT);

    id=0;

    label = psnd_sprintf_temp("%d", lfdec->maxcal);
    popup_add_text2(cont_id, &(ci[id]), id, label, "Max Calculations");
    id++;
    label = psnd_sprintf_temp("%-5.1f", lfdec->wprec);
    popup_add_text2(cont_id, &(ci[id]), id, label, "Footprint (= n x width)");
    id++;
    label = psnd_sprintf_temp("%g", lfdec->scaint);
    popup_add_text2(cont_id, &(ci[id]), id, label, "Integration scale");

    id++;
    g_popup_init_info(ci + id);
    ci[id].type          = G_CHILD_PANEL;
    ci[id].item          = TRUE;
    ci[id].frame         = TRUE;
    ci[id].title 	 = "Fit Range (Channels)";
    ci[id].horizontal    = TRUE;
    g_popup_add_child(cont_id, ci + id);

    label = psnd_sprintf_temp("%d", lfdec->il);
    id++;
    g_popup_init_info(ci+id);
    ci[id].type  	= G_CHILD_TEXTBOX;
    ci[id].id    	= id;
    ci[id].item_count 	= 40;
    ci[id].items_visible= 8;
    ci[id].label 	= label;
    ci[id].frame 	= TRUE;
    ci[id].title 	= "From";
    g_popup_add_child(cont_id, ci+id);
    label = psnd_sprintf_temp("%d", lfdec->ir);
    id++;
    g_popup_init_info(ci+id);
    ci[id].type  	= G_CHILD_TEXTBOX;
    ci[id].id    	= id;
    ci[id].item_count 	= 40;
    ci[id].items_visible= 8;
    ci[id].label 	= label;
    ci[id].frame 	= TRUE;
    ci[id].title 	= "To";
    g_popup_add_child(cont_id, ci+id);
    id++;
    g_popup_init_info(ci + id);
    ci[id].type          = G_CHILD_PANEL;
    ci[id].item          = FALSE;
    g_popup_add_child(cont_id, ci + id);
    id++;
    g_popup_init_info(&(ci[id]));
    ci[id].type       = G_CHILD_RADIOBOX;
    ci[id].id         = id;
    ci[id].title      = "Lineshape";
    ci[id].data       = shape;
    ci[id].item_count = 3;
    ci[id].horizontal = FALSE;
    ci[id].frame      = TRUE;
    ci[id].item       = lfdec->lineshape;
    g_popup_add_child(cont_id, &(ci[id]));

    assert(id<MAXID);        

    if (g_popup_container_show(cont_id)) {
        id=0;
        lfdec->maxcal = psnd_scan_integer(ci[id].label);id++;
        lfdec->wprec = psnd_scan_float(ci[id].label);id++;
        lfdec->scaint = psnd_scan_float(ci[id].label);id++;
        id++;
        lfdec->il = psnd_scan_integer(ci[id].label);id++;
        lfdec->il = max(1,lfdec->il);
        lfdec->il = min(lfdec->dat->isize,lfdec->il);
        lfdec->ir = psnd_scan_integer(ci[id].label);id++;
        lfdec->ir = max(1,lfdec->ir);
        lfdec->ir = min(lfdec->dat->isize,lfdec->ir);
        id++;
        if (lfdec->scaint==0) 
            lfdec->scaint=1.;
        lfdec->lineshape = ci[id++].item;
        return TRUE;
    }
    return FALSE;
}


typedef enum {
    FIT_LINENO_ID=100,
    FIT_SHIFT_ID,
    FIT_HEIGHT_ID,
    FIT_WIDTH_ID,
    FIT_LEFT_ID,
    FIT_RIGHT_ID,
    FIT_HIGHER_ID,
    FIT_LOWER_ID,
    FIT_WIDER_ID,
    FIT_NARROWER_ID
} popup_linefit_ids;

static void lineadjust_callback(G_POPUP_CHILDINFO *ci)
{
    MBLOCK *mblk         = (MBLOCK *) ci->userdata;
    LFDECONV_TYPE *lfdec = mblk->lfdec;
    PBLOCK *par          = lfdec->par;
    DBLOCK *dat          = lfdec->dat;
    SBLOCK *spar         = lfdec->spar;
    float f;
    POPUP_INFO *popinf = mblk->popinf + POP_LFDECADJUST;

    
    switch (ci->type) {
        case G_CHILD_OK:
        case G_CHILD_CANCEL:
            popinf->visible = FALSE;
            return;
        case G_CHILD_SPINBOX:
        case G_CHILD_TEXTBOX:
        case G_CHILD_PUSHBUTTON:
        switch (ci->id) {
        case FIT_LINENO_ID:
            lfdec->linenb = ci->item;
            break;
        case FIT_SHIFT_ID:
            lfdec->sfract = psnd_scan_float(ci->label);
            break;
        case FIT_HEIGHT_ID:
            lfdec->hfract = psnd_scan_float(ci->label);
            break;
        case FIT_WIDTH_ID:
            lfdec->wfract = psnd_scan_float(ci->label);
            break;
        case FIT_LEFT_ID:
            f = lfdec->array[POS][lfdec->linenb-1] * lfdec->sfract;
            lfdec->array[POS][lfdec->linenb-1] -= f;
            lfdec_recalc(lfdec, dat);
            lfdec_fitspec(mblk);
            break;
        case FIT_RIGHT_ID:
            f = lfdec->array[POS][lfdec->linenb-1] * lfdec->sfract;
            lfdec->array[POS][lfdec->linenb-1] += f;
            lfdec_recalc(lfdec, dat);
            lfdec_fitspec(mblk);
            break;
        case FIT_HIGHER_ID:
            f = lfdec->array[HIG][lfdec->linenb-1] * lfdec->hfract;
            lfdec->array[HIG][lfdec->linenb-1] += f;
            lfdec_recalc(lfdec, dat);
            lfdec_fitspec(mblk);
            break;
        case FIT_LOWER_ID:
            f = lfdec->array[HIG][lfdec->linenb-1] * lfdec->hfract;
            lfdec->array[HIG][lfdec->linenb-1] -= f;
            lfdec_recalc(lfdec, dat);
            lfdec_fitspec(mblk);
            break;
        case FIT_WIDER_ID:
            f = lfdec->array[WID][lfdec->linenb-1] * lfdec->wfract;
            lfdec->array[WID][lfdec->linenb-1] += f;
            lfdec_recalc(lfdec, dat);
            lfdec_fitspec(mblk);
            break;
        case FIT_NARROWER_ID:
            f = lfdec->array[WID][lfdec->linenb-1] * lfdec->wfract;
            lfdec->array[WID][lfdec->linenb-1] -= f;
            lfdec_recalc(lfdec, dat);
            lfdec_fitspec(mblk);
            break;
        }
        break;
    }
}


static void lfdec_adjust(MBLOCK *mblk)
{
    int i,id;
    char *label;
    G_POPUP_CHILDINFO ci[MAXIDPLUS];
    int min_max_dec_step[] = { 1, 1, 0, 1 };
    LFDECONV_TYPE *lfdec = mblk->lfdec;
    POPUP_INFO *popinf = mblk->popinf + POP_LFDECADJUST;

    if (lfdec->linctr == 0)
        return;
    if (popinf->visible)
        return;
    popinf->visible = TRUE;
    if (!popinf->cont_id) {
        int cont_id;
        
        cont_id = popinf->cont_id = 
          g_popup_container_open(mblk->info->win_id, 
                                 "Adjust Line",
                                 G_POPUP_KEEP|G_POPUP_SINGLEBUTTON);
    
        lfdec->linenb = max(1, lfdec->linenb);
        lfdec->linenb = min(lfdec->linctr, lfdec->linenb);
        min_max_dec_step[1] = lfdec->linctr;
    
        id=0;
        g_popup_init_info(&(ci[id]));
        ci[id].type          = G_CHILD_SPINBOX;
        ci[id].id            = FIT_LINENO_ID;
        ci[id].frame         = TRUE;
        ci[id].item          = lfdec->linenb;
        ci[id].items_visible = 5;
        ci[id].label         = "Line #";
        ci[id].select        = min_max_dec_step;
        ci[id].func          = lineadjust_callback;
        ci[id].userdata      = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));
    
    
        id++;
        g_popup_init_info(ci + id);
        ci[id].type          = G_CHILD_PANEL;
        ci[id].item          = TRUE;
        ci[id].frame         = TRUE;
        ci[id].title 	 = "Fraction Change";
        ci[id].horizontal    = FALSE;
        g_popup_add_child(cont_id, ci + id);
    
        id++;
        label = psnd_sprintf_temp("%-5.4f", lfdec->sfract);
        g_popup_init_info(&(ci[id]));
        ci[id].type          = G_CHILD_TEXTBOX;
        ci[id].id            = FIT_SHIFT_ID;
        ci[id].item_count    = 40;
        ci[id].items_visible = 10;
        ci[id].label         = label;
        ci[id].horizontal    = TRUE;
        ci[id].title         = "Shift ";
        ci[id].func          = lineadjust_callback;
        ci[id].userdata      = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));
    
        id++;
        label = psnd_sprintf_temp("%-5.3f", lfdec->hfract);
        g_popup_init_info(&(ci[id]));
        ci[id].type          = G_CHILD_TEXTBOX;
        ci[id].id            = FIT_HEIGHT_ID;
        ci[id].item_count    = 40;
        ci[id].items_visible = 10;
        ci[id].label         = label;
        ci[id].horizontal    = TRUE;
        ci[id].title         = "Height";
        ci[id].func          = lineadjust_callback;
        ci[id].userdata      = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));
    
        id++;
        label = psnd_sprintf_temp("%-5.3f", lfdec->wfract);
        g_popup_init_info(&(ci[id]));
        ci[id].type          = G_CHILD_TEXTBOX;
        ci[id].id            = FIT_WIDTH_ID;
        ci[id].item_count    = 40;
        ci[id].items_visible = 10;
        ci[id].label         = label;
        ci[id].horizontal    = TRUE;
        ci[id].title         = "Width ";
        ci[id].func          = lineadjust_callback;
        ci[id].userdata      = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));
    
        id++;
        g_popup_init_info(ci + id);
        ci[id].type          = G_CHILD_PANEL;
        ci[id].item          = FALSE;
        g_popup_add_child(cont_id, ci + id);
    
        id++;
        g_popup_init_info(ci + id);
        ci[id].type          = G_CHILD_PANEL;
        ci[id].item          = TRUE;
        ci[id].frame         = TRUE;
        ci[id].title 	 = "Shift";
        ci[id].horizontal    = TRUE;
        g_popup_add_child(cont_id, ci + id);
    
        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_PUSHBUTTON;
        ci[id].id    = FIT_LEFT_ID;
        ci[id].label = "Left    ";
        ci[id].func  = lineadjust_callback;
        ci[id].userdata      = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));
    
        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_PUSHBUTTON;
        ci[id].id    = FIT_RIGHT_ID;
        ci[id].label = "Right   ";
        ci[id].func  = lineadjust_callback;
        ci[id].userdata      = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));
    
        id++;
        g_popup_init_info(ci + id);
        ci[id].type          = G_CHILD_PANEL;
        ci[id].item          = FALSE;
        g_popup_add_child(cont_id, ci + id);
    
        id++;
        g_popup_init_info(ci + id);
        ci[id].type          = G_CHILD_PANEL;
        ci[id].item          = TRUE;
        ci[id].frame         = TRUE;
        ci[id].title 	 = "Height";
        ci[id].horizontal    = TRUE;
        g_popup_add_child(cont_id, ci + id);
    
        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_PUSHBUTTON;
        ci[id].id    = FIT_HIGHER_ID;
        ci[id].label = "Higher  ";
        ci[id].func  = lineadjust_callback;
        ci[id].userdata      = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));
    
        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_PUSHBUTTON;
        ci[id].id    = FIT_LOWER_ID;
        ci[id].label = "Lower   ";
        ci[id].func  = lineadjust_callback;
        ci[id].userdata      = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));
    
        id++;
        g_popup_init_info(ci + id);
        ci[id].type          = G_CHILD_PANEL;
        ci[id].item          = FALSE;
        g_popup_add_child(cont_id, ci + id);
    
        id++;
        g_popup_init_info(ci + id);
        ci[id].type          = G_CHILD_PANEL;
        ci[id].item          = TRUE;
        ci[id].frame         = TRUE;
        ci[id].title 	 = "Width";
        ci[id].horizontal    = TRUE;
        g_popup_add_child(cont_id, ci + id);
    
        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_PUSHBUTTON;
        ci[id].id    = FIT_WIDER_ID;
        ci[id].label = "Wider   ";
        ci[id].func  = lineadjust_callback;
        ci[id].userdata      = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));
    
        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_PUSHBUTTON;
        ci[id].id    = FIT_NARROWER_ID;
        ci[id].label = "Narrower";
        ci[id].func  = lineadjust_callback;
        ci[id].userdata      = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));
    
        id++;
        g_popup_init_info(ci + id);
        ci[id].type          = G_CHILD_PANEL;
        ci[id].item          = FALSE;
        g_popup_add_child(cont_id, ci + id);
    
        id++;
        g_popup_init_info(ci + id);
        ci[id].type          = G_CHILD_OK;
        ci[id].func          = lineadjust_callback;
        ci[id].userdata      = (void*) mblk;
        g_popup_add_child(cont_id, ci + id);
    
        id++;
        g_popup_init_info(ci + id);
        ci[id].type          = G_CHILD_CANCEL;
        ci[id].func          = lineadjust_callback;
        ci[id].userdata      = (void*) mblk;
        g_popup_add_child(cont_id, ci + id);

        assert(id<MAXID);
    }
    g_popup_container_show(popinf->cont_id);

}

typedef enum {
    FIT_LIST_ID=100,
    FIT_EDIT_ID,
    FIT_JUNK_ID,
    FIT_ADJUST_ID,
    FIT_OPTIMIZE_MULTI_ID,
    FIT_RESET_ID,
    FIT_SETUP_ID,
    FIT_OPTIONS_ID,
    FIT_LIST_MULTI_ID,
    FIT_RESET_MULTI_ID,
    FIT_DIFF_ID,
    FIT_CALC_ID,
    FIT_GRAB_MOUSE_ID,
    FIT_SHOW_FITPEAKS_ID
} popup_fit_ids;

static void linefit_callback(G_POPUP_CHILDINFO *ci)
{
    MBLOCK *mblk         = (MBLOCK *) ci->userdata;
    LFDECONV_TYPE *lfdec = mblk->lfdec;
    PBLOCK *par          = lfdec->par;
    DBLOCK *dat          = lfdec->dat;
    SBLOCK *spar         = lfdec->spar;
    SBLOCK *spar2        = lfdec->spar2;
    POPUP_INFO *popinf   = mblk->popinf + POP_LFDEC;

    switch (ci->type) {
        case G_CHILD_OK:
        case G_CHILD_CANCEL:
            g_menu_enable_item(mblk->info->mousebar_id, ID_MOUSE_SELECT, FALSE);
            if (mblk->info->mouse_mode1 == MOUSE_LINEFIT)
                psnd_set_cursormode(mblk, 0, 0);   
            popinf->visible = FALSE;
            break;
        default:
            switch (ci->id) {
                case FIT_LIST_ID:
                    lfdec_listlines(mblk);
                    break;
                case FIT_EDIT_ID:
                    if (lfdec_addline(mblk, -1, par, dat))
                        lfdec_fitspec(mblk);
                    break;
                case FIT_JUNK_ID:
                    lfdec_junkline(mblk);
                    lfdec_fitspec(mblk);
                    break;
                case FIT_ADJUST_ID:
                    lfdec_adjust(mblk);
                    break;
                case FIT_SHOW_FITPEAKS_ID:
                    show_fitpeaks(mblk);
                    break;
                case FIT_OPTIMIZE_MULTI_ID:
                    psnd_push_waitcursor(mblk);
                    lfdec_optimizeline(lfdec, TRUE, par, dat);
                    lfdec_fitspec(mblk);
                    psnd_pop_waitcursor(mblk);
                    break;
                case FIT_RESET_ID:
                    lfdec_reset(lfdec, dat);
                    lfdec_fitspec(mblk);
                    break;
                case FIT_SETUP_ID:
                    if (lfdec_setup(mblk)) {
                        lfdec_recalc(lfdec, dat);
                        lfdec_fitspec(mblk);
                    }
                    break;
                case FIT_OPTIONS_ID:
                    lfdec_multisetup(mblk);
                    break;
                case FIT_LIST_MULTI_ID:
                    lfdec_multilist(mblk);
                    break;
                case FIT_RESET_MULTI_ID:
                    lfdec_multireset(lfdec);
                    break;
                case FIT_DIFF_ID:
                    lfdec_diffspec(lfdec, spar, dat);
                    lfdec_fitspec(mblk);
                    break;
                case FIT_CALC_ID:
                    lfdec_recalc(lfdec, dat);
                    lfdec_fitspec(mblk);
                    break;
                case FIT_GRAB_MOUSE_ID:
                    psnd_set_cursormode(mblk, MOUSE_LINEFIT, MOUSE_LINEFIT);
                    break;
            }
            break;

    }
}

void psnd_popup_linefit(MBLOCK *mblk, SBLOCK *spar2, DBLOCK *dat2)
{
    int i,id;
    G_POPUP_CHILDINFO ci[MAXID];
    POPUP_INFO *popinf = mblk->popinf + POP_LFDEC;

    if (popinf->visible)
        return;
    lfdec_init(mblk->lfdec,mblk->spar,spar2,PAR,DAT,dat2);
    popinf->visible = TRUE;
    if (!popinf->cont_id) {
        int cont_id;
        
        cont_id = g_popup_container_open(mblk->info->win_id, "Linefit",
             G_POPUP_KEEP|G_POPUP_SINGLEBUTTON);
        popinf->cont_id = cont_id;
        id=-1;

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_PUSHBUTTON;
        ci[id].id    = FIT_SETUP_ID;
        ci[id].label = "Setup Parameters";
        ci[id].func  = linefit_callback;
        ci[id].userdata  = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));
        
        id++;
        g_popup_init_info(ci + id);
        ci[id].type          = G_CHILD_PANEL;
        ci[id].item          = TRUE;
        ci[id].frame         = TRUE;
        ci[id].title 	     = "Line";
        ci[id].horizontal    = FALSE;
        g_popup_add_child(cont_id, ci + id);

        id++;
        g_popup_init_info(ci + id);
        ci[id].type          = G_CHILD_PANEL;
        ci[id].item          = TRUE;
        ci[id].horizontal    = TRUE;
        g_popup_add_child(cont_id, ci + id);

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_PUSHBUTTON;
        ci[id].id    = FIT_EDIT_ID;
        ci[id].label = "Add/Edit";
        ci[id].func  = linefit_callback;
        ci[id].userdata  = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));
        
        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_PUSHBUTTON;
        ci[id].id    = FIT_JUNK_ID;
        ci[id].label = "Delete  ";
        ci[id].func  = linefit_callback;
        ci[id].userdata  = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));
        
        id++;
        g_popup_init_info(ci + id);
        ci[id].type          = G_CHILD_PANEL;
        ci[id].item          = FALSE;
        g_popup_add_child(cont_id, ci + id);

        id++;
        g_popup_init_info(ci + id);
        ci[id].type          = G_CHILD_PANEL;
        ci[id].item          = TRUE;
        ci[id].horizontal    = TRUE;
        g_popup_add_child(cont_id, ci + id);

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_PUSHBUTTON;
        ci[id].id    = FIT_ADJUST_ID;
        ci[id].label = "Adjust  ";
        ci[id].func  = linefit_callback;
        ci[id].userdata  = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));
        
        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_PUSHBUTTON;
        ci[id].id    = FIT_LIST_ID;
        ci[id].label = "List    ";
        ci[id].func  = linefit_callback;
        ci[id].userdata  = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(ci + id);
        ci[id].type          = G_CHILD_PANEL;
        ci[id].item          = FALSE;
        g_popup_add_child(cont_id, ci + id);

        id++;
        g_popup_init_info(ci + id);
        ci[id].type          = G_CHILD_PANEL;
        ci[id].item          = FALSE;
        g_popup_add_child(cont_id, ci + id);
        
        id++;
        g_popup_init_info(ci + id);
        ci[id].type          = G_CHILD_PANEL;
        ci[id].item          = TRUE;
        ci[id].frame         = TRUE;
        ci[id].title 	     = "Fit on ...";
        ci[id].horizontal    = TRUE;
        g_popup_add_child(cont_id, ci + id);

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_PUSHBUTTON;
        ci[id].id    = FIT_OPTIONS_ID;
        ci[id].label = "Set  ";
        ci[id].func  = linefit_callback;
        ci[id].userdata  = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));
        
        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_PUSHBUTTON;
        ci[id].id    = FIT_LIST_MULTI_ID;
        ci[id].label = "List ";
        ci[id].func  = linefit_callback;
        ci[id].userdata  = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));
        
        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_PUSHBUTTON;
        ci[id].id    = FIT_RESET_MULTI_ID;
        ci[id].label = "Reset";
        ci[id].func  = linefit_callback;
        ci[id].userdata  = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));
        
        id++;
        g_popup_init_info(ci + id);
        ci[id].type          = G_CHILD_PANEL;
        ci[id].item          = FALSE;
        g_popup_add_child(cont_id, ci + id);

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_PUSHBUTTON;
        ci[id].id    = FIT_OPTIMIZE_MULTI_ID;
        ci[id].label = "Optimize Multiline";
        ci[id].func  = linefit_callback;
        ci[id].userdata  = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_PUSHBUTTON;
        ci[id].id    = FIT_SHOW_FITPEAKS_ID;
        ci[id].label = "Show All Fitted Peaks";
        ci[id].func  = linefit_callback;
        ci[id].userdata  = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_PUSHBUTTON;
        ci[id].id    = FIT_RESET_ID;
        ci[id].label = "Remove All Lines";
        ci[id].func  = linefit_callback;
        ci[id].userdata  = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_PUSHBUTTON;
        ci[id].id    = FIT_DIFF_ID;
        ci[id].label = "Difference spectrum";
        ci[id].func  = linefit_callback;
        ci[id].userdata  = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));
        
        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type       = G_CHILD_PUSHBUTTON;
        ci[id].id         = FIT_GRAB_MOUSE_ID;
        ci[id].label      = "Grab Mouse Button 1";
        ci[id].func       = linefit_callback;
        ci[id].userdata  = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_OK;
        ci[id].func  = linefit_callback;
        ci[id].userdata  = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_CANCEL;
        ci[id].func  = linefit_callback;
        ci[id].userdata  = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        assert(id<MAXID);
    }
    psnd_set_cursormode(mblk, MOUSE_LINEFIT, 0);
    g_menu_enable_item(mblk->info->mousebar_id, ID_MOUSE_SELECT, TRUE);
    g_popup_container_show(popinf->cont_id);
}









