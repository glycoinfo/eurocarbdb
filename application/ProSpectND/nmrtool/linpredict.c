


#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <assert.h>
#include "mathtool.h"
#include "nmrtool.h"

#define LP_FW        0 /* Forward LP Mode.                     */
#define LP_FWBW      1 /* Mixed forward-backward Mode.         */
#define LP_BW        2 /* Backward LP Mode.                    */
#define LP_MI        3 /* Mirror image LP mode 		       */
#define LP_NMODE     4

#define LP_FIX_DEC  -1 /* Adjustment for decreasing signals.   */
#define LP_FIX_NONE  0 /* No adjustment of roots.              */
#define LP_FIX_INC   1 /* Adjustment for increasing signals.   */

#define LP_PREDICT_BEFORE   1
#define LP_PREDICT_AFTER    0

#define MINROOT 0.00001      /* Minimum root magnitude for inversion.      */

#define ZERO Complex_d(0.0,0.0)
#define ONE  Complex_d(1.0,0.0)


static int riConjD( double *srcR, double *srcI, int length );
static int riMoveD( double *srcR, double *srcI, double *destR, 
                    double *destI, int length );
static int riMoveConjD( double *srcR, double *srcI, double *destR, 
                        double *destI, int length ) ;
static int lpc_zroots( dcomplex *a, int m, dcomplex *rts );


#define MAXROW	1024
#define MAXCOL	512
/*
 *     machine precision eps determined from machar program
 *     Convex C2
 *     parameter (eps=7.45e-9,svdtol=10.0*eps)
 *     IRIS Indigo
 *     parameter (eps=1.19e-7,svdtol=10.0*eps)
 */
#define EPS_	(1.19e-7)
#define SVD_TOL	(10.0*EPS_)

static int find_lp_coefficients(float *rdata, float *idata, 
          int lpDataSize, int lpOrder, 
          double *fcoef, int forward, double tol)
{
    int i,j,nrhs,krank;
    int maxROW,maxCOL,lwork;
    int ierr;
    float *ssing,*rwork;
    fcomplex  *smat,*ftemp,*work;

    maxROW = max(lpDataSize,MAXROW);
    maxCOL = max(lpOrder,MAXCOL);
    lwork  = 30*maxROW;
    rwork  = (float*) malloc(sizeof(float)*lwork);
    assert(rwork);
    ssing  = (float*) malloc(sizeof(float)*maxCOL);
    assert(ssing);
#define SMAT(I,J) smat[(I) + (J) * (maxROW)]
    smat   = (fcomplex *) malloc(sizeof(fcomplex )*maxROW*maxCOL);
    assert(smat);
    ftemp  = (fcomplex *) malloc(sizeof(fcomplex )*maxROW);
    assert(ftemp);
    work   = (fcomplex *) malloc(sizeof(fcomplex )*lwork);
    assert(work);

    for (i=0;i<2*lpOrder;i++) 
	fcoef[i] = 0;

    if (forward) {
        for (i=0;i<lpDataSize-lpOrder;i++) {
            int k = i+lpOrder;
            ftemp[i].r = rdata[k];
            ftemp[i].i = idata[k];
            for (j=0;j<lpOrder;j++) {
                SMAT(i,j).r = rdata[k-j-1];
                SMAT(i,j).i = idata[k-j-1];
            }
        }
    }
    else {
        for (i=0;i<lpDataSize-lpOrder;i++) {
            int k = lpDataSize-lpOrder-i-1;
            ftemp[i].r = rdata[k];
            ftemp[i].i = idata[k];
            for (j=0;j<lpOrder;j++) {
                SMAT(i,j).r = rdata[k+j];
                SMAT(i,j).i = idata[k+j];
            }
        }
    }

    /*
     *     use SVD routine to solve linear system of equations
     */
    ierr = 0;
    nrhs = 1;
    /*
     *  CGELSS computes the minimum norm solution to a complex linear   
     *  least squares problem:   
     */
    cgelss(lpDataSize-lpOrder,lpOrder,nrhs,smat,maxROW,ftemp,
            maxROW,ssing,SVD_TOL,&krank,work,lwork,rwork,&ierr);
                    
    if (ierr) 
        fprintf(stderr,"Error in cgelss: %d\n",ierr);
    else for (i=0;i<krank;i++) {
	fcoef[i] = ftemp[i].r;
	fcoef[i+lpOrder] = ftemp[i].i;
    }
    free(rwork);
    free(ssing);
    free(smat);
    free(ftemp);
    free(work);
    return 0;
}


/*
 * lpc_predict:
 *  Before modeled data region: predict using reflected ab in reverse order.
 *  After modeled data region:  predict using af in forward order.
 */

static void lpc_predict(int beforeFlag, float *rpred, float *ipred, int icmplx, int nfut,
                double *ar, double *ai, int order)
{
    int j;
    float *rPtrR, *rPtrI;
    
    if (icmplx) {
        if (beforeFlag)  {
            rPtrR = rpred + nfut - 1;
            rPtrI = ipred + nfut - 1;
      
            while ( rPtrR >= rpred ) {
                *rPtrR = 0.0; *rPtrI = 0.0;
    
                for( j = 1; j <= order; j++ )  {
                    *rPtrR += rPtrR[j] * ar[j-1] - rPtrI[j] * ai[j-1];
                    *rPtrI += rPtrR[j] * ai[j-1] + rPtrI[j] * ar[j-1];
                }
    
                rPtrR--; rPtrI--;
            }
        }
        else {
            rPtrR = rpred;
            rPtrI = ipred;
      
            while ( rPtrR < rpred + nfut) {
                *rPtrR = 0.0; *rPtrI = 0.0;
    
                for( j = 1; j <= order; j++ ) {
                    *rPtrR += rPtrR[-j] * ar[j-1] - rPtrI[-j] * ai[j-1];
                    *rPtrI += rPtrR[-j] * ai[j-1] + rPtrI[-j] * ar[j-1];
                }
    
                rPtrR++; rPtrI++;
            }
        }
    }
    else {
        if (beforeFlag)  {
            rPtrR = rpred + nfut - 1;
      
            while ( rPtrR >= rpred ) {
                *rPtrR = 0.0; 
    
                for( j = 1; j <= order; j++ )  {
                    *rPtrR += rPtrR[j] * ar[j-1];
                }
    
                rPtrR--; 
            }
        }
        else {
            rPtrR = rpred;
      
            while ( rPtrR < rpred + nfut) {
                *rPtrR = 0.0; 
    
                for( j = 1; j <= order; j++ ) {
                    *rPtrR += rPtrR[-j] * ar[j-1];
                }
    
                rPtrR++; 
            }
        }
    }
}



/*
 * lpcfixrts: unpacks and tests roots of LP coefficient for consistancy
 *            with known properties of the signal.
 */

static int lpcfixrts( double *d, int npoles, int fixmode )
{
    int     k, j;
    double  dmag;

    static dcomplex  a[MAXROOT], roots[MAXROOT], ztemp;
    static dcomplex  rts[MAXROOT], ad[MAXROOT];

    if (fixmode == LP_FIX_NONE) 
        return 0;

    /*
     * a[j]=Complex_d(-d[npoles-j],-d[2*npoles-j]);
     */
    for( k = 0, j = npoles; j > 0; j--, k++ ) {
        ad[k].r  =  a[j].r   = -d[npoles-j];
        ad[k].i  =  a[j].i   = -d[2*npoles-j];
    }
    lpc_zroots( ad, npoles, rts );
    for ( j = 0; j < npoles; j++ ) {
        roots[j].r = rts[j].r;
        roots[j].i = rts[j].i;
    }

    if (fixmode == LP_FIX_INC) {
        for( j = 0; j < npoles; j++ ) {
            if (Cabs_d(roots[j]) > 1.0) 
                roots[j] = Cdiv_d( ONE, Conjg_d(roots[j]) ); 
        }
    }
    else if (fixmode == LP_FIX_DEC) {
        for( j = 0; j < npoles; j++ ) {
            dmag = Cabs_d(roots[j]);
            if (Cabs_d(roots[j]) > 1.0) 
/*
            if (dmag > MINROOT && dmag < 1.0)
*/
                roots[j] = Cdiv_d( ONE, Conjg_d(roots[j]) );
        }
    }
    a[0] = Csub_d(ZERO,roots[0]);
    a[1] = ONE;

    for( j = 1; j < npoles; j++ ) {
        a[j] = ONE;

        for( k = j; k >= 1; k-- ) {
            /*
             * ztemp = Cmul_d(roots[j],a[k]); 
             */

            ztemp.r = roots[j].r * a[k].r - roots[j].i * a[k].i;
            ztemp.i = roots[j].r * a[k].i + roots[j].i * a[k].r;

            /*
             * a[k]=Csub_d(a[k-1],ztemp); 
             */

            a[k].r = a[k-1].r - ztemp.r;
            a[k].i = a[k-1].i - ztemp.i;
        }
        a[0] = Csub_d( ZERO, Cmul_d( roots[j], a[0] ));
    }

    for( j = 1; j <= npoles; j++ ) {
        d[npoles-j]   = -a[j-1].r;
        d[2*npoles-j] = -a[j-1].i;
    }

    return 0;
}



/*
 * lpc_zroots: finds complex roots
 */

static int lpc_zroots( dcomplex *a, int m, dcomplex *rts )
{
     int  is1,is2;
     int  i, j, status;

     static double caR[MAXROOT*MAXROOT], caI[MAXROOT*MAXROOT];
     static double eR[MAXROOT], eI[MAXROOT];

    /*
     * Construct the companion matrix.
     * Find the eigvalues.
     */

    for( i = 0; i < m; i++ ) {
        for( j = 0; j < m; j++ )  {
            if (i == 0)  {
                caR[j] = -a[j].r;
                caI[j] = -a[j].i;
            }
            else if (j == (i-1) && i > 0 ) { 
                caR[i*m+j] = 1.0;
                caI[i*m+j] = 0.0;
            }
            else  { 
                caR[i*m+j] = 0.0;
                caI[i*m+j] = 0.0;
            }
        }
    }

    cbal(m, m, caR, caI, &is1, &is2, eR);
    corth (m, m, is1, is2, caR, caI,  eR, eI);
    comqr(m, m, is1, is2, caR, caI, eR, eI, &status);

    if (status == 0)
        for( i = 0; i < m; i++ ) {
            rts[i].r = eR[i];
            rts[i].i = eI[i]; 
        }

    return 0 ;
}

/*
 * lpc: Complex Linear Prediction; models data in rdata/idata and
 *      stores result in rpred/ipred.
 *
 * Nota Bene: rpred/ipred must point to the overall data vector which
 *            contains rdata/idata inside of it.
 */

/*
 * rdata, idata  = Complex data to predict.
 * rpred, ipred  = On return, predicted data.
 *  
 *
 */
static int lpc( int lpDataSize, int lpOrder,
         float  *rdata, float  *idata, 
         int icmplx,               
         int lpPredSize,
         float  *rpred, float  *ipred,         
         int beforeFlag,
         int lpOpt,
         int lpFixMode,
         double toler)  

{
    int    i, j,  result=0;
    double *afR, *afI, *abR, *abI;
    double *af, *ab;

    if (lpOrder > MAXROOT) {
        /*
        psnd_printf("Order set to %d ( Maximum Order )\n", MAXROOT );
        */
        lpOrder = MAXROOT;
    }
    /*
     *    Abort for null-size prediction.
     *    Add zeros for null-signal or null-order prediction.
     */
    

    if (lpPredSize < 1) 
        return 0;

    if (lpOrder == 0){
        for ( i = 0; i < lpPredSize; i++ ) {
            *rpred++ = 0.0;
            *ipred++ = 0.0;
        }
        return 0;
   }


    af = dvector( 0, 2 * lpOrder );
    ab = dvector( 0, 2 * lpOrder );
    
    if (beforeFlag) {
        if (lpOpt == LP_BW || lpOpt == LP_FWBW) {

           if (find_lp_coefficients(rdata, idata, lpDataSize, lpOrder, 
                     ab, TRUE, toler) > 0)
                 result = 2;
        }
        if (result == 0 && (lpOpt == LP_FW || lpOpt == LP_FWBW ||
	    lpOpt == LP_MI)) {
           if (find_lp_coefficients(rdata, idata, lpDataSize, lpOrder,
                     af, FALSE, toler) > 0)
                 result = 2;
        }
    }
    else {
        if (lpOpt == LP_FW || lpOpt == LP_FWBW ||
	    lpOpt == LP_MI) {
           if (find_lp_coefficients(rdata, idata, lpDataSize, lpOrder,
                     af, TRUE, toler) > 0)
                 result = 2;
        }

        if (result == 0 && (lpOpt == LP_BW || lpOpt == LP_FWBW)) { 
           if (find_lp_coefficients(rdata, idata, lpDataSize, lpOrder,
                     ab, FALSE, toler) > 0)
                 result = 2;
        }
    }
    if (result != 0) {
        free_dvector( af, 0);
        free_dvector( ab, 0);
        return result;
    }

/*
 * Root fixing:
 *  Negating imaginaries makes backward roots into forward ones.
 *
 *  Forward:  fix roots of forward coefficients.
 *  Backward: fix roots of negated backward values as if they were forward.
 *  Mixed:    fix forward roots, fix backward ones as above, average af + ab.
 *
 * For prediction before data region: make sure "ab" has valid coefficients.
 * For prediction after data region:  make sure "af" has valid coefficients.
 */

    afR = af;
    afI = af + lpOrder;
    abR = ab;
    abI = ab + lpOrder;


    if (lpOpt == LP_FW || lpOpt == LP_MI) {
     
        lpcfixrts( af, lpOrder, lpFixMode );
        if (beforeFlag) 
            riMoveD( afR, afI, abR, abI, lpOrder );
    }
    else if (lpOpt == LP_BW)  {
        if (beforeFlag) {
            lpcfixrts( ab, lpOrder, lpFixMode );
            riConjD( abR, abI, lpOrder );
        }
        else {
            riMoveConjD( abR, abI, afR, afI, lpOrder );
            lpcfixrts( af, lpOrder, lpFixMode );
        }
    }
    else {
        if (!beforeFlag) 
            riConjD( abR, abI, lpOrder );

        lpcfixrts( af, lpOrder, lpFixMode );
        lpcfixrts( ab, lpOrder, lpFixMode );

        if (beforeFlag) 
            riConjD( abR, abI, lpOrder );

        for( j = 0; j < lpOrder; j++ )  {
            abR[j] =  (abR[j] + afR[j])*0.5;
            abI[j] =  (abI[j] + afI[j])*0.5;
        }
    }


/* 
 * Prediction:
 *  Before modeled data region: predict using reflected ab in reverse order.
 *  After modeled data region:  predict using af in forward order.
 */
    if (beforeFlag)  
        lpc_predict(beforeFlag, rpred, ipred, icmplx, lpPredSize,
                   abR, abI, lpOrder);
    else
        lpc_predict(beforeFlag, rpred, ipred, icmplx, lpPredSize,
                   afR, afI, lpOrder);
    free_dvector( af, 0);
    free_dvector( ab, 0);
    return result;
}




/* 
 * riConjD: conjugate the complex data in srcR srcI.
 */

static int riConjD( double *srcR, double *srcI, int length )
{
    while( length-- )
       {
        *srcI = -(*srcI);
        srcI++;
       }

    return( 0 );
}


/* 
 * riMoveD: move complex data in srcR srcI to destR destI.
 */

static int riMoveD( double *srcR, double *srcI, double *destR, 
                    double *destI, int length )
{
    while( length-- )
       {
        *destR++ = *srcR++;
        *destI++ = *srcI++;
       }

    return( 0 );
}

/* 
 * riMoveConjD: move complex conjugate of srcR srcI to destR destI.
 */

static int riMoveConjD( double *srcR, double *srcI, double *destR, 
                        double *destI, int length ) 
{
    while( length-- )
       {
        *destR++ = *srcR++;
        *destI++ = -(*srcI++);
       }

    return( 0 );
}





/*
 * Complex prediction
 */
void ftlipc(int imode, int fixroots, float rdata[],float idata[],int icmplx, int ndata,
             float rfuture[],float ifuture[],int nfut,int iorder, double toler)
{ 
    int fix = LP_FIX_NONE;
    int opt, before;
    int ok = 0;

    
    do {
        if (iorder >= ndata/2) {
            iorder = ndata/2-1;
            /*
            psnd_printf("Number of roots scaled back to: %d\n", iorder);
            */
        }
    
        if (imode == PREDICT_FORWARD) {
            if (fixroots)
                fix = LP_FIX_INC;
            opt     = LP_FW;
            before  = LP_PREDICT_AFTER;
        }
        else if (imode == PREDICT_BACKWARD) {
            if (fixroots)
                fix = LP_FIX_DEC;
            opt     = LP_BW;
            before  = LP_PREDICT_BEFORE;
        }
        else 
            return;
    
        ok = lpc(ndata, iorder, rdata, idata, icmplx,
                 nfut, rfuture, ifuture, before, opt,fix, toler);
    
        if (ok == 2) {
            iorder /= 2;
            if (iorder < 5)
                break;
            /*
            psnd_printf("Number of roots scaled back to: %d\n", iorder);
            */
        }
    }
    while (ok == 2);
}

/*
 * Complex gap prediction
 * ndata = total spectrum size
 * nfut1 = 1st point gap
 * nfut2 = last point gap
 */
void ftlipc_gap(int fixroots, float rdata[], float idata[], int icmplx, int ndata,
             int nfut1, int nfut2, int iorder, double toler)
{ 
    int nfut, ok1 = TRUE, ok2 = TRUE, 
        i, fix = LP_FIX_NONE;
    float *tmpr, *tmpi;

    nfut = nfut2-nfut1+1;
    tmpr = fvector(0, nfut);
    tmpi = fvector(0, nfut);

    if (nfut1 > nfut * 4) {
        int iord = iorder;
        if (iord >= nfut1/2) {
            iord = nfut1/2-1;
        }
        if (fixroots)
            fix = LP_FIX_INC;
        ok1 = lpc(nfut1-1, iord, rdata, idata, icmplx,
                  nfut, rdata+nfut1-1, idata+nfut1-1, 
                  LP_PREDICT_AFTER, LP_FW, fix, toler);

    }

    if (ok1==0) {
        for (i=0;i<nfut;i++) {
            int j = nfut1-1+i;
            tmpr[i] = rdata[j];
            tmpi[i] = idata[j];
        }
    }
    
    if (ndata - nfut2 > nfut * 4) {
        int iord = iorder;
        if (iord >= (ndata - nfut2)/2) {
            iord = (ndata - nfut2)/2-1;
        }
        if (fixroots)
            fix = LP_FIX_DEC;
        ok2 = lpc(ndata-nfut2, iord, rdata+nfut2, idata+nfut2, icmplx,
                  nfut, rdata+nfut1-1, idata+nfut1-1, 
                  LP_PREDICT_BEFORE, LP_BW, fix, toler);
    }
    if (ok1 == 0 &&  ok2 ==0) {
        float ffut = (float) nfut;
        if (icmplx) {
            for (i=0;i<nfut;i++) {
                int j = nfut1-1+i;
                float bscale = (float) (i+1) / (ffut+1.0);
                float fscale = 1.0 - bscale;
                rdata[j] = rdata[j] * bscale + tmpr[i] * fscale;
                idata[j] = idata[j] * bscale + tmpi[i] * fscale;
            }
        }
        else {
            for (i=0;i<nfut;i++) {
                int j = nfut1-1+i;
                float bscale = (float) (i+1) / (ffut+1.0);
                float fscale = 1.0 - bscale;
                rdata[j] = rdata[j] * bscale + tmpr[i] * fscale;
            }
        }
    }
    free_fvector(tmpr, 0);
    free_fvector(tmpi, 0);
}

