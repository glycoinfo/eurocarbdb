/********************************************************************/
/*                             baseline.c                           */
/*                                                                  */
/********************************************************************/
#include <stdio.h>
#include <stdlib.h>
#include <ctype.h>
#include <string.h>
#include <stdarg.h>
#include <math.h>
#include "mathtool.h"
#include "nmrtool.h"



void itrpl2(float *y, float *z, int npts, int nterms, float u, float v,
            float sfac, float bfac, int *ier);

/*
c....nmr_bascor.for
c
c    gv    891030 adapted bascrr
c    rb    890221
c==========================================================================
c     bascrr
c
c     itrpl1
c     itrpl2
c     itrpla
c     itrplb
c     itrsn1
c     itrsn2
c     itrcs1
c     itrcs2
c     itrtb1
c     itrtb2
c
c     ier =    0 error from bascrr
c           100n error from itrpl2
c           200n error from itrpl2
c           300n error from itrsn1
c           400n error from itrsn2
c           500n error from itrcs1
c           600n error from itrcs2
c           700n error from itrtb1
c           800n error from itrtb2
c           900n error from bcrbfx
c          1000n error from itrpla
c          1100n error from itrplb
c
c==========================================================================
*/

#define DOSINE		0
#define DOCOS		1
#define DOSINECOS	2

void bascrr(int ibase, int ibstrt, int ibstop,
            float *y, float *window, float *table, int nt1, int nt2,
            float u, float v, float sfac, float bfac, int *ier)
{
    int isize;
    
    isize = ibstop-ibstrt+1;

    *ier = 1;
    if (!(ibstrt > 0  && isize > nt1)) {
        *ier = 0;
        return;
    }
    switch (ibase) {
        case BASCS1:
            itrsncs(y+ibstrt-1,NULL,isize,nt1,nt2,u,v,sfac,bfac,ier,DOCOS);
            break;
        case BASCS2:
            itrsncs(y+ibstrt-1,window+ibstrt-1,isize,nt1,nt2,u,v,sfac,bfac,ier,
                    DOCOS);
            break;
        case BASSN1:
            itrsncs(y+ibstrt-1,NULL,isize,nt1,nt2,u,v,sfac,bfac,ier,DOSINE);
            break;
        case BASSN2:
            itrsncs(y+ibstrt-1,window+ibstrt-1,isize,nt1,nt2,u,v,sfac,bfac,ier,
                    DOSINE);
            break;
        case BASSC1:
            itrsncs(y+ibstrt-1,NULL,isize,nt1,nt2,u,v,sfac,bfac,ier,DOSINECOS);
            break;
        case BASSC2:
            itrsncs(y+ibstrt-1,window+ibstrt-1,isize,nt1,nt2,u,v,sfac,bfac,ier,
                    DOSINECOS);
            break;
        case BASPL1:
            itrpl(y+ibstrt-1,NULL,isize,nt1,u,v,sfac,bfac,ier);
            break;
        case BASPL2:
            itrpl(y+ibstrt-1,window+ibstrt-1,isize,nt1,u,v,sfac,bfac,ier);
            break;
        case BASTB1:
            itrtb(y+ibstrt-1,NULL,table+ibstrt-1,isize,u,v,sfac,bfac,ier);
            break;
        case BASTB2:
            itrtb(y+ibstrt-1,window+ibstrt-1,table+ibstrt-1,
                                  isize,u,v,sfac,bfac,ier);
            break;
            /*
        case BASBFX:
            bcrbfx(y+ibstrt-1,isize,nt1,ier);
            break;
            */
    }
}



void bcrbfx(float *y, int npts, int nt, int *ier)
{
    int i, ibstrt, ibstop, nav, iplu, imin;
    float avleft, avright, slope;

    if (nt <= 0) {
        *ier += 9000;
        return;
    }
    
    ibstrt  = 1;
    ibstop  = npts;
    nav     = (nt-1)/2;
    iplu    = ibstrt+2*nav;
    imin    = ibstop-2*nav;
    avleft  = xxsumz(y,ibstrt,iplu)/(2*nav+1);
    avright = xxsumz(y,imin,ibstop)/(2*nav+1);
    slope   = (avright-avleft)/(ibstop-ibstrt-2*nav);
    for (i=ibstrt;i<=ibstop;i++)
        y[i-1] -= (avleft+slope*(i-ibstrt-nav));
    *ier = 1;
}



/*
 * Select baseline regions
 */
#define SORTCOUNT	32
float baseline_region(float *spec, float *bas, int npoints, int clip)
{
    int i,j, istep, sortcount[SORTCOUNT+1],sortmax,scount,sresult;
    float low=1e33,hig=0,sort[SORTCOUNT+1];   

    istep = npoints/200;
    istep = max(4,istep);
    istep = min(istep,8);
    istep = min(istep,npoints-1);
    /*
     * For each point, look at the difference with
     * 2*istep neighboring points. If the difference is small
     * this is a baseline point
     */
    for (i=1;i<=npoints;i++) {
        int start,stop,idiv;
        idiv = 0;
        bas[i]=0;
        /*
         * Sample points at the left of current point
         */
        start = max(1,i-istep);
        idiv += i - start;
        for (j=start;j<i;j++)
            bas[i] += fabs(spec[i] - spec[j]);
        /*
         * Sample points at the right of current point
         */
        stop  = min(npoints,i+istep);
        idiv += stop - i; 
        for (j=i+1;j<=stop;j++) 
            bas[i] += fabs(spec[i] - spec[j]);
        bas[i] /= idiv;
        /*
         * Highest point
         */
        if (bas[i] > hig)
            hig = bas[i];
        /*
         * Lowest point
         */
        if (bas[i] < low)
            low = bas[i];
    }
    /*
     * Now sort the result into N bins
     * First, determine bin sizes
     */
    sortcount[0] = 0;
    sort[0]      = hig/2;
    for (scount=1;scount<SORTCOUNT && sort[scount-1]>low;scount++) {
        sortcount[scount] = 0;
        sort[scount]      = sort[scount-1] * 0.5;
    }
    /*
     * Calculate how many points go into each bin
     */
    sort[scount]=0.0;
    for (i=1;i<=npoints;i++) {
        for (j=0;j<=scount;j++)
            if (bas[i] > sort[j]) {
                sortcount[j]++;
                break;
            }
    }
    /*
     * Select bin with the most points
     */
    sortmax=0;
    sresult=0;
    for (i=0;i<scount;i++) {
        if (sortcount[i] > sortmax) {
            sortmax = sortcount[i];
            sresult=i;
        }
    }
    sresult=max(0,sresult-1);
    /*
     * If requested clip the data in the work array
     * to the size of the largest matching bin
     */
    if (clip) for (i=1;i<=npoints;i++) {
        int ok = FALSE;
        for (j=0;j<sresult;j++) {
            if (bas[i] >= sort[j]) {
                bas[i] = sort[j];
                ok = TRUE;
                break;
            }
        }
        if (!ok)
            bas[i] = 0;
    }
    return sort[sresult];
}



static double sinefunc(double x, int term, int ioff)
{
    return sin((ioff+term)*x);
}

static double cosinefunc(double x, int term, int ioff)
{
    return cos((ioff+term)*x);
}

static double sinecosinefunc(double x, int term, int ioff)
{
    x *= (term+1)/2 + ioff;
    if (term % 2)
        return sin(x);
    return cos(x);
}

static int svd_ioff;
static void svdfunc_sine(float x, double *p,int np)
{
    int i;
    
    p[1] = 1.0;
    for (i=2;i<=np;i++)
        p[i] = sinefunc(x, i-1, svd_ioff);
}

static void svdfunc_cosine(float x, double *p, int np)
{
    int i;
    
    p[1] = 1.0;
    for (i=2;i<=np;i++)
        p[i] = cosinefunc(x, i-1, svd_ioff);
}

static void svdfunc_sinecosine(float x, double *p, int np)
{
    int i;
    
    p[1] = 1.0;
    for (i=2;i<=np;i++)
        p[i] = sinecosinefunc(x, i-1, svd_ioff);
}


/*
 *
 *   baseline correction with
 *   f(x) = a0 + a1*cos(x) + a2*cos(2*x) + 
 *       or
 *   f(x) = a0 + a1*sin(x) + a2*sin(2*x) +
 *
 *
 */
void itrsncs(float *y, float *z, int npts, int nt1, int nt2,float u, float v,
            float sfac, float bfac, int *ier, int mode)
{
    float *xx,*yy,*si;
    double *aa,**uu,**vv,*ww, chisq;
    int ma, *ia;
    float w,fnpi;
    int   i, j, k,ioff;
    double (*func)(double,int,int);
    void (*svdfunc)(float,double*,int);
    

    
    if (nt2 > 0 && nt2 < nt1-1) {
        ioff = nt2;
        nt1 -= ioff;
    }
    else {
        ioff = 0;
    }

    if (mode == DOSINE) {
        func    = sinefunc;
        svdfunc = svdfunc_sine;
    }
    else if (mode == DOCOS) {
        func    = cosinefunc; 
        svdfunc = svdfunc_cosine;
    }
    else {
        nt1     = 1 + (nt1-1)*2;
        func    = sinecosinefunc; 
        svdfunc = svdfunc_sinecosine;
    }

    svd_ioff = ioff;

    ma = nt1+1;
    xx = fvector(1, npts);
    yy = fvector(1, npts);
    si = fvector(1, npts);

    aa = dvector(1, ma);
    uu = dmatrix(1, npts, 1, ma);
    vv = dmatrix(1, ma, 1, ma);
    ww = dvector(1, ma);
    ia = ivector(1, ma);
    for (i=1;i<=ma;i++) 
        ia[i] = 1;


    /*
     * Arrays must start at 1, not 0
     */
    y--;
    if (z)
        z--;

    fnpi   = 1.0/(float)npts;
    if (!z)
        w=baseline_region(y, si, npts, 0);

    j=0;
    for (i=1;i<=npts;i++) {
        if (!z && si[i] > w)
            continue;
        if (z && z[i] == 0.0)
            continue;
        j++;
        xx[j] = i*fnpi;
        yy[j] = y[i];
        si[j] = 1.0; 
    }

    ma = min(nt1+1,j);
    if (ma == 0) {
        *ier += 1000;
        goto out;
    }
    if (npts > 1024)
        *ier = lfit(xx, yy, si, j, aa, ia,
                    ma, vv, &chisq, svdfunc);
    else
        *ier = svdfit(xx, yy, si, j, aa,
                      ma, uu, vv, ww, &chisq, svdfunc);

    /*
     *  subtract polynome
     */

    if (*ier != 1) {
        *ier += 1000;
        goto out;
    }
    /*
     * subtract polynome
     */
    for (j=1;j<=npts;j++) {
        float xj = (float)j * fnpi;
        double ytmp = aa[1];
        for (k=2;k<=ma;k++)
            ytmp += aa[k] * func(xj,k-1,ioff);
        y[j] -= ytmp;
   }

out:

    free_fvector(xx, 1);
    free_fvector(yy, 1);
    free_fvector(si, 1);
    
    ma    = nt1+1;
    free_dvector(aa, 1);
    free_dmatrix(uu, 1, npts, 1, ma);
    free_dmatrix(vv, 1, ma, 1, ma);
    free_dvector(ww, 1);
    free_ivector(ia, 1);

}


/*
 *
 *   baseline correction with
 *   f(x) = a0 + a1*cos(x) + a2*cos(2*x) + 
 *       or
 *   f(x) = a0 + a1*sin(x) + a2*sin(2*x) +
 *
 *    adapted from:
 *
 *     bevington(1969),data reduction and error annalysis in
 *     the physical sciences, mc graw-hill, new york
 *
 *    modification:
 *
 *     exclude all (x,y) points with y outside window w
 *
 */
void itrsncs2(float *y, float *z, int npts, int nt1, int nt2,float u, float v,
            float sfac, float bfac, int *ier, int mode)
{
    float sighigh = 1.e6, sigmin = 0.1, pi = 3.141592654;
    float ynorm, ysum,fnpi,sigma0,sigma1,absy,w,sum,ymean,sigma,a0,yi,
          xi,xj,ybas,yim,fcx,rnorm;
    float *a, *xmean, *sigmax, *r, **array, *xwork;

    int   i, j, k, ipos, ntp1, nterms, icnt, nsum, ishift, idiv;
    int   mcnt = 100;
    int   lstop = FALSE;
    double (*func)(double x,int,int);
    
    int ioff;

    
    if (nt2 > 0 && nt2 < nt1-1) {
        ioff = nt2;
        nt1 -= ioff;
    }
    else {
        ioff = 0;
    }

    if (mode == DOSINE) {
        func = sinefunc;
        ishift = 0; 
        idiv = 1;
    }
    else if (mode == DOCOS) {
        func = cosinefunc; 
        ishift = 0; 
        idiv = 1;
    }
    else {
        nt1  = 1 + (nt1-1)*2;
        func = sinecosinefunc; 
        ishift = npts/2; 
        idiv = 2;
    }
    
    /*
     * Arrays must start at 1, not 0
     */
    y--;
    if (z)
        z--;
    /*
     * scale (x,y) points
     */
    minmax(y+1,npts,&ynorm,&ysum);
    ysum  = fabs(ysum);
    ynorm = fabs(ynorm);
    if (ysum > ynorm) 
        ynorm = ysum;
    ynorm = 1.0/ynorm;
    for (j=1;j<=npts;j++)
        y[j] *= ynorm;

    fnpi = pi/(float)npts;

    /*
     * out of loop variables
     */
    ntp1   = nt1;
    nterms = nt1-1;
    a      = fvector(1, nterms);
    xmean  = fvector(1, nterms);
    sigmax = fvector(1, nterms);
    r      = fvector(1, nterms);
    array  = fmatrix(1, ntp1, 1, nterms);

    /*
     * determine view window
     */
    icnt   = 0;
    sigma1 = sighigh;
    while (!lstop) {
        icnt++;
        if (icnt >= mcnt) {
             /*
             psnd_printf("Error bascrr: stop after #iter= %d\n",icnt);
             */
             *ier += 3000;
             break;
        }
        sigma0 = sigma1;
        nsum = 1;
        ysum = 0.0;
        for (j=1;j<=npts;j++) {
            absy = fabs(y[j]);
            if ((u * sigma0) < absy) 
                continue;
            nsum++;
            ysum += absy * absy;
        } 
        sigma1 = sqrt(ysum/nsum);
        if (ysum == 0.0) 
            sigma1 = sigmin;
        if ((sigma0-sigma1)/sigma0 > sfac) 
            continue;
        w = v * sigma1;

        /*
         *     fit within view window v*'dev. from zero'
         *     initialize
         */
        sum   = 0.0;
        ymean = 0.0;
        sigma = 0.0;
        a0    = 0.0;
        for (j=1;j<=nterms;j++) {
            xmean[j]  = 0.0;
            sigmax[j] = 0.0;
            r[j]      = 0.0;
            a[j]      = 0.0;
            for (k=1;k<=ntp1;k++)
                array[k][j] = 0.0;
        }

        /*
         * accumulate sums
         */
        for (i=1;i<=npts;i++) {
            yi   = y[i];
            if (!z && fabs(yi) > w) 
                continue;
            if (z && z[i] == 0.0) 
                continue;
            ymean += yi;
            sum   += 1.0;
            xi     = (float)i * fnpi;
            for (j=1;j<=nterms;j++)
                xmean[j] += func(xi,j,ioff);
        } 

        if (sum <= 0.0) {
             /*  
             psnd_printf("Error bascrr: stop with sum zero\n");
             */
             *ier += 4000;
             break;
         }
         ymean /= sum;
         for (j=1;j<=nterms;j++)
             xmean[j] /= sum;

        /*
         * accumlate matrices r and array
         */
        for (i=1;i<=npts;i++) {
            yi   = y[i];
            if (!z && fabs(yi) > w) 
                continue;
            if (z && z[i] == 0.0) 
                continue;
            yim    = yi - ymean;
            xi     = (float) i * fnpi;
            sigma += pow(yim,2);
            for (j=1;j<=nterms;j++) {
                fcx        = func(xi,j,ioff)-xmean[j];
                sigmax[j] += pow(fcx,2);
                r[j]      += fcx * yim;
                for (k=1;k<=j;k++)
                   array[k][j] += fcx * (func(xi,k,ioff)-xmean[k]);
            }
        }

        sigma = sqrt(sigma);
        for (j=1;j<=nterms;j++) {
            sigmax[j]      = sqrt(sigmax[j]);
            array[ntp1][j] = r[j]/(sigmax[j]*sigma);
            for (k=1;k<=j;k++) {
                array[k][j] /= (sigmax[j]*sigmax[k]);
                array[j][k]  = array[k][j];
            }
        }

        /*
         * solve equation : a*array=r
         *
         *
         * adapted from:
         *         riess & johnson, numerical analysis,
         *         addison-wesley publishing company, 1982
         */
        if (nterms > 0)
            gauss(array,a,nterms,ier,&rnorm);

        /*
         * calculate coefficients
         */
        a0 = ymean;
        for (j=1;j<=nterms;j++) {
             a[j] *= sigma/sigmax[j];
             a0   -= a[j] * xmean[j];
        }

        /*
         * subtract polynome
         */
        lstop = TRUE;
        for (j=1;j<=npts;j++) {
            xj = (float)j * fnpi;
            ybas = a0;
            for (k=1;k<=nterms;k++)
                ybas += a[k] * func(xj,k,ioff);
            y[j] -= ybas;
            if (fabs(ybas) > bfac * sigma1) 
                lstop = FALSE;
        }
    }
   
    ynorm = 1.0/ynorm;
    for (j=1;j<=npts;j++)
        y[j] *= ynorm;
    free_fvector(a       , 1);
    free_fvector(xmean   , 1);
    free_fvector(sigmax  , 1);
    free_fvector(r       , 1);
    free_fmatrix(array   , 1, ntp1, 1, nterms);
}




static void svdfunc_poly(float x, double *p,int np)
{
    int i;
    
    p[1] = 1.0;
    for (i=2;i<=np;i++)
        p[i] = p[i-1] * x;
}


void itrpl(float *y, float *z, int npts, int nterms, float u, float v,
            float sfac, float bfac, int *ier)
{
    float *xx,*yy,*si;
    double *aa,**uu,**vv,*ww, chisq;
    int ma, *ia;
    float w,fnpi;
    int   i, j;

    ma    = nterms;

    if (nterms <= 0) {
        /*
        psnd_printf("%d polynomial terms not implemented\n", nterms);
        */
        return;
    }
    xx = fvector(1, npts);
    yy = fvector(1, npts);
    si = fvector(1, npts);

    aa = dvector(1, ma);
    uu = dmatrix(1, npts, 1, ma);
    vv = dmatrix(1, ma, 1, ma);
    ww = dvector(1, ma);
    ia = ivector(1, ma);
    for (i=1;i<=ma;i++) ia[i] = 1;


    /*
     * Arrays must start at 1, not 0
     */
    y--;
    if (z)
        z--;

    fnpi   = 1.0/(float)npts;
    if (!z)
        w=baseline_region(y, si, npts, 0);

    j=0;
    for (i=1;i<=npts;i++) {
        if (!z && si[i] > w)
            continue;
        if (z && z[i] == 0.0)
            continue;
        j++;
        xx[j] = i*fnpi;
        yy[j] = y[i];
        si[j] = 1.0;
    }
    ma = min(nterms,j);
    if (ma == 0) {
        *ier += 1000;
        goto out;
    }
    if (npts > 1024)
        *ier = lfit(xx, yy, si, j, aa,ia,
                    ma, vv, &chisq, svdfunc_poly);
    else
        *ier = svdfit(xx, yy, si, j, aa,
                      ma, uu, vv, ww, &chisq, svdfunc_poly);

    /*
     *  subtract polynome
     */

    if (*ier != 1) {
        *ier += 1000;
        goto out;
    }
    /*
     * polynomial has the form:
     * y = a1 + a2 * x + a3 * x^2 + a4 * x^3 + ..
     *
     */
    for (i=1;i<=npts;i++) {
        double ytmp = aa[ma];
        for (j=ma-1;j>=1;j--)
            ytmp = aa[j] + ytmp * i*fnpi;
        y[i] -= ytmp;
    }

out:

    free_fvector(xx, 1);
    free_fvector(yy, 1);
    free_fvector(si, 1);
    
    ma    = nterms;
    free_dvector(aa, 1);
    free_dmatrix(uu, 1, npts, 1, ma);
    free_dmatrix(vv, 1, ma, 1, ma);
    free_dvector(ww, 1);
    free_ivector(ia, 1);

}


/*
 *
 *    baseline correction with polynome subtraction
 *
 *    ref:
 *    a.  pearson(1972),  j. magn. reson.
 *    b.  bevington(1969),data reduction and error annalysis in
 *        the physical sciences, mc graw-hill, new york
 *    c.  riedel & johnson (1982), numerical analysis, addison-wesley
 *
 */
void itrpl2(float *y, float *z, int npts, int nterms, float u, float v,
            float sfac, float bfac, int *ier)
{
    float sighigh = 1.e2, sigmin = 0.01;
    float ynorm, ysum,fnpi,sigma0,sigma1,absy,w,xi,yi,xterm,yterm,ybas,rj,rjj;
    float *a, *sumx, *sumy, *r, **aug, rnorm;
    int   i, j, k, n, nmax,ntp1,icnt,nsum;
    int   mcnt=100;
    int   lstop = FALSE;

    if (nterms <= 0) {
        /*
        psnd_printf("%d polynomial terms not implemented\n", nterms);
        */
        return;
    }
    /*
     * Arrays must start at 1, not 0
     */
    y--;
    if (z)
        z--;
    /*
     * scale (x,y) points
     */
    minmax(y+1,npts,&ysum,&ynorm);
    ysum  = fabs(ysum);
    ynorm = fabs(ynorm);
    if (ysum > ynorm) 
        ynorm = ysum;
    if (ynorm == 0)
        return;
    ynorm = 1.0/ynorm;
    for (j=1;j<=npts;j++)
        y[j] *= ynorm;

    fnpi = 1.0/(float)npts;
    /*
     * out of loop variables
     */
    nmax   = 2*nterms-1;
    ntp1   = nterms+1;
    a      = fvector(1, nterms);
    sumx   = fvector(1, nmax);
    sumy   = fvector(1, nterms);
    r      = fvector(1, nterms);
    aug    = fmatrix(1, ntp1, 1, nterms);

    /*
     * determine view window
     */
    icnt   = 0;
    sigma1 = sighigh;
    while (!lstop) {
        icnt++;
        if (icnt >= mcnt) {
            /* 
             * stop after mcnt iterations 
             */
            if (z)
                *ier += 2000;
            else
                *ier += 1000;
            break;
        }
        sigma0 = sigma1;
        nsum = 1;
        ysum = 0.0;
        for (j=1;j<=npts;j++) {
            absy = fabs(y[j]);
            if ((u * sigma0) < absy) 
                continue;
            nsum++;
            ysum += absy * absy;
        } 
        sigma1 = sqrt(ysum/nsum);
        if (ysum == 0.0) 
            sigma1 = sigmin;
        if ((sigma0-sigma1)/sigma0 > sfac) 
            continue;
        /*
         * polynome fit within view window v*'dev. from zero'
         */
        w = v * sigma1;
        for (n=1;n<=nmax;n++)
            sumx[n] = 0.0;
        for (j=1;j<=nterms;j++)
            sumy[j] = 0.0;

        for (i=1;i<=npts;i++) {
            yi = y[i];
            if (!z && fabs(yi) > w) 
                continue;
            if (z && z[i] == 0.0)
                continue;
            xi=(float)i * fnpi;
            xterm = 1.0;
            for (n=1;n<=nmax;n++) {
                sumx[n] += xterm;
                xterm *= xi;
            }
            yterm = yi;
            for (n=1;n<=nterms;n++) {
                sumy[n] += yterm;
                yterm *= xi;
            }
        }

        for (j=1;j<=nterms;j++) {
            for (k=1;k<=nterms;k++) {
                n = j+k-1;
                aug[k][j] = sumx[n];
            }
            aug[ntp1][j] = sumy[j];
        }

        gauss(aug,a,nterms,ier,&rnorm);

        /*
         *  subtract polynome and check if result is within 
         *  bfac*'dev. from zero'
         */

        lstop = TRUE;
        if (*ier != 1) {
            /* write(itxerr,*)' stop after ier=',ier */
            if (z)
                *ier += 2000;
            else
                *ier += 1000;
            break;
        }
        /*
         * polynomial has the form:
         * y = a1 + a2 * x + a3 * x^2 + a4 * x^3 + ..
         */
        for (j=1;j<=npts;j++) {
            ybas = a[1];
            rj   = (float)j * fnpi;
            rjj  = 1.0;
            for (k=2;k<=nterms;k++) {
                rjj *= rj;
                ybas += a[k] * rjj;
            }
            y[j] -= ybas;
            if (fabs(ybas) > bfac*sigma1) 
                lstop = FALSE;
        }
    }
    /*
     *    rescale (x,y) points
     */
    ynorm = 1.0/ynorm;
    for (j=1;j<=npts;j++)
        y[j] *= ynorm;
    free_fvector(a       , 1);
    free_fvector(sumx    , 1);
    free_fvector(sumy    , 1);
    free_fvector(r       , 1);
    free_fmatrix(aug     , 1, ntp1, 1, nterms);
}




/*
 * Table fit
 *
 * fit to f(x) = a0 + a * tab(x)
 *
 */
void itrtb(float *y, float *z, float *tab, int npts, float u, float v,
            float sfac, float bfac, int *ier)
{
    float sighigh = 1.e6, sigmin = 0.1;
    float ynorm, ysum, sigma0, sigma1, tnorm, sum, absy, sumf, sumff, sumy, 
          sumfy, a, a0, yi,ybas,w,fi;
    int   i, j, icnt, nsum;
    int   mcnt = 100;
    int   lstop = FALSE;

    /*
     * Arrays must start at 1, not 0
     */
    y--;
    tab--;
    if (z)
        z--;
    /*
     * scale (x,y) points
     */
    minmax(y+1,npts,&ysum,&ynorm);
    ysum  = fabs(ysum);
    ynorm = fabs(ynorm);
    if (ysum > ynorm) 
        ynorm = ysum;
    minmax(tab+1,npts,&ysum,&tnorm);
    ysum  = fabs(ysum);
    tnorm = fabs(tnorm);
    if (ysum > tnorm) 
        tnorm = ysum;
    if (ynorm == 0  || tnorm == 0)
        return;
    ynorm = 1.0/ynorm;
    tnorm = 1.0/tnorm;
    for (j=1;j<=npts;j++) {
        y[j]   *= ynorm;
        tab[j] *= tnorm;
    }
    sigma1 = sighigh;
    icnt   = 0;
    while (!lstop) {
        icnt++;
        if (icnt >= mcnt) {
            /* write(itxerr,*)' stop after #iter=',icnt */
            if (z)
                *ier += 8000;
            else
                *ier += 7000;
            break;
        }
        sigma0 = sigma1;
        sum    = 0.0;
        ysum   = 0.0;
        for (j=1;j<=npts;j++) {
            absy = fabs(y[j]);
            if ((u*sigma0) < absy) 
                continue;
            sum  += 1.0;
            ysum += absy*absy;
        }
        sigma1 = sqrt(ysum/sum);
        if (ysum == 0.0) 
            sigma1 = sigmin;
        if ((sigma0-sigma1)/sigma0 > sfac) 
            continue;
        w = v * sigma1;

        /*
         * fit to f(x) = a0 + a * tab(x)
         *
         * exclude all (x,y) points with y outside window w
         * initialize
         */
        nsum  = 0;
        sumf  = 0.0;
        sumff = 0.0;
        sumy  = 0.0;
        sumfy = 0.0;
        a     = 0.0;
        a0    = 0.0;
        /*
         * accumulate sums
         */
        for (i=1;i<=npts;i++) {
            yi   = y[i];
            if (!z && fabs(yi) > w) 
                continue;
            if (z && z[i] == 0.0)
                continue;
            fi     = tab[i];
            nsum  += 1;
            sumf  += fi;
            sumff += fi*fi;
            sumy  += yi;
            sumfy += fi*yi;
        }
        if (nsum <= 0) {
            /* write(itxerr,*)' stop with nsum zero' */
            if (z)
                *ier += 8000;
            else
                *ier += 7000;
            break;
        }
        /*
         * calculate coefficients
         */
        sum = (float) nsum;
        a   = (sumf*sumy-sum*sumfy)/(sumf*sumf-sum*sumff);
        a0  = (sumy-a*sumf)/sum;

        lstop = TRUE;
        for (j=1;j<=npts;j++) {
            /*
             *  y[j] = y[j] - a0 - a * tab[j];
             */
            ybas  = a0 + a * tab[j];
            y[j] -= ybas;
            if (fabs(ybas) > bfac*sigma1) 
                lstop = FALSE;
              
        }
    }
    ynorm = 1.0/ynorm;
    tnorm = 1.0/tnorm;
    for (j=1;j<=npts;j++) {
        y[j]   *= ynorm;
        tab[j] *= tnorm;
    }
}

