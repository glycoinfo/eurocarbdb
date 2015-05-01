

#include <stdlib.h>
#include <math.h>
#include <assert.h>
#include "complex.h"
#include "mathtool.h"

/*
CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC
C
C     Filename:   zpoly.f
C
C     Description:   FINDS THE ZEROS OF A COMPLEX POLYNOMIAL.
C
C     Ported to SunOS -  6/6/94  (C.P. Hess)
C
C     On entry:
C
C      OP      - Double precision vector of complex polynomial
C                coefficients in order of decreasing powers.
C      DEGREE  - Integer degree of polynomial.
C     
C     On return:
C
C      ZERO     - Output double precision vector of complex zeros
C      FAIL     - Logical parameter, TRUE only if leading
C                 coefficient is zero or JPOLY has found
C                 fewer than degree zeros
C
C     Reference:
C     ALGORITHM 419 COLLECTED ALGORITHMS FROM ACM.
C     ALGORITHM APPEARED IN COMM. ACM, VOL. 15, NO. 02,
C     P. 097.
C
CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC
C
*/

typedef struct poly_common {
    double *pr,*pi,*hr,*hi,*qpr,*qpi,*qhr,*qhi,*shr,*shi;
    double sr,si,tr,ti,pvr,pvi,are,mre,eta,infin;
    int nn;
} POLY_COMMON;

double cmod(double r, double i);
void mcon(double *eta,double *infiny,double *smalno,double *base);
double scaleit(int nn, double *pt, double eta, double infin, 
               double smalno, double base);
void cdivid(double ar,double ai,double br,double bi,double *cr,double *ci);                           
double cauchy(int nn, double *pt, double *q);
void  noshft(POLY_COMMON *pc, int l1) ;
void  polyev(int nn, double sr, double si, double *pr, double *pi, 
                  double *qr, double *qi, double *pvr, double *pvi);
void calct(POLY_COMMON *pc, int *bool);
void nexth(POLY_COMMON *pc, int bool);
double errev(int nn,double *qr,double *qi,
             double ms,double mp,double are,double mre);
void vrshft(POLY_COMMON *pc, int l3,double *zr,double *zi,int *conv);
void fxshft(POLY_COMMON *pc, int l2,double *zr,double *zi, int *conv); 


#define PSIZE 	150
#define NVEC	10

void zpoly(dcomplex *op, int degree, dcomplex *zero, int *fail)
{
/*
c common area
      common/global/pr,pi,hr,hi,qpr,qpi,qhr,qhi,shr,shi,
     *    sr,si,tr,ti,pvr,pvi,are,mre,eta,infin,nn
      double precision sr,si,tr,ti,pvr,pvi,are,mre,eta,infin,
     *    pr(150),pi(150),hr(150),hi(150),qpr(150),qpi(150),qhr(150),
     *    qhi(150),shr(150),shi(150)
*/
    POLY_COMMON pc;
    double xx,yy,cosr,sinr,smalno,base,xxx,zr,zi,bnd;
    double *opr,*opi,*zeror,*zeroi;
    int conv;
    int cnt1,cnt2,idnn2,i;
    int psize = max(PSIZE, degree+1);

    
    /*
     * to change the size of polynomials which can be solved, replace
     * the dimension of the arrays in the common area.
     */
    pc.pr = (double*) malloc(sizeof(double)*PSIZE*NVEC);
    assert(pc.pr);
    pc.pi   = pc.pr + psize;
    pc.hr   = pc.pr + psize*2;
    pc.hi   = pc.pr + psize*3;
    pc.qpr  = pc.pr + psize*4;
    pc.qpi  = pc.pr + psize*5;
    pc.qhr  = pc.pr + psize*6;
    pc.qhi  = pc.pr + psize*7;
    pc.shr  = pc.pr + psize*8;
    pc.shi  = pc.pr + psize*9;
     
    /*
     * initialization of constants
     */
    mcon(&pc.eta,&pc.infin,&smalno,&base);
    pc.are   = pc.eta;
    pc.mre   = 2.0 * sqrt(2.0) * pc.eta;
    xx       = .70710678;
    yy       = -xx;
    cosr     = -.060756474;
    sinr     = .99756405;
    *fail    = FALSE;
    pc.nn    = degree+1;

    opr = (double*) malloc(sizeof(double)*PSIZE*4);
    assert(opr);
    opi   = opr + psize;
    zeror = opr + psize*2;
    zeroi = opr + psize*3;

    for (i=0;i<pc.nn;i++) {
        opr[i] = op[i].r;
        opi[i] = op[i].i;
    }
    /*
     * algorithm fails if the leading coefficient is zero.
     */
    if (opr[0] == 0.0 && opi[0] == 0.0) {
          *fail = TRUE;
          free(pc.pr);
          free(opr);
          return;
    }
    /*
     * remove the zeros at the origin if any.
     */
    while (opr[pc.nn-1] == 0.0 && opi[pc.nn-1] == 0.0) { 
        idnn2 = degree-pc.nn+2;
        zeror[idnn2-1] = 0.0;
        zeroi[idnn2-1] = 0.0;
        pc.nn--;
    } 
    /*
     * make a copy of the coefficients.
     */
    for (i = 0;i<pc.nn;i++) {
        pc.pr[i]  = opr[i];
        pc.pi[i]  = opi[i];
        pc.shr[i] = cmod(pc.pr[i],pc.pi[i]);
    }
    /*
     * scale the polynomial.
     */
    bnd = scaleit(pc.nn,pc.shr,pc.eta,pc.infin,smalno,base);
    if (bnd != 1.0) {
        for (i = 0;i<pc.nn;i++) {
            pc.pr[i] *= bnd;
            pc.pi[i] *= bnd;
        }
    }
    /*
     * start the algorithm for one zero .
     */
L40:
    if (pc.nn <= 2) {
        /*
         * calculate the final zero and return.
         */
        cdivid(-pc.pr[1],-pc.pi[1],pc.pr[0],pc.pi[0],zeror+degree-1,
                zeroi+degree-1);
        for (i=0;i<degree;i++)
            zero[i] = Complex_d(zeror[i],zeroi[i]);

        free(pc.pr);
        free(opr);
        return;
    }
    /*
     * calculate bnd, a lower bound on the modulus of the zeros.
     */
    for (i = 0;i<pc.nn;i++)
        pc.shr[i] = cmod(pc.pr[i],pc.pi[i]);

    bnd = cauchy(pc.nn,pc.shr,pc.shi);
    /*
     * outer loop to control 2 major passes with different sequences
     * of shifts.
     */
    for (cnt1 = 1;cnt1<=2;cnt1++) {
        /*
         * first stage calculation, no shift.
         */
        noshft(&pc, 5);
        /*
         * inner loop to select a shift.
         */
        for (cnt2 = 1;cnt2<=9;cnt2++) {
            /*
             * shift is chosen with modulus bnd and amplitude rotated by
             * 94 degrees from the previous shift
             */
            xxx = cosr*xx-sinr*yy;
            yy = sinr*xx+cosr*yy;
            xx = xxx;
            pc.sr = bnd*xx;
            pc.si = bnd*yy;
            /*
             * second stage calculation, fixed shift.
             */
            fxshft(&pc,10*cnt2,&zr,&zi,&conv);
            if (conv) { 
                /*
                 * the second stage jumps directly to the third stage iteration.
                 * if successful the zero is stored and the polynomial deflated.
                 */
                idnn2 = degree-pc.nn+2;
                zeror[idnn2-1] = zr;
                zeroi[idnn2-1] = zi;
                pc.nn--;
                for (i = 0;i<pc.nn;i++) {
                    pc.pr[i] = pc.qpr[i];
                    pc.pi[i] = pc.qpi[i];
                }
                goto L40;
            }  
            /*
             * if the iteration is unsuccessful another shift is chosen.
             */
        }
        /*
         * if 9 shifts fail, the outer loop is repeated with another
         * sequence of shifts.
         */
    }
    /*
     * the zerofinder has failed on two major passes.
     * return empty handed.
     */
    free(pc.pr);
    free(opr);
    *fail = TRUE;
}





/*
c computes  the derivative  polynomial as the initial h
c polynomial and computes l1 no-shift h polynomials.
c common area
*/
void  noshft(POLY_COMMON *pc, int l1) 
{                                          
    double xni,t1,t2;
    int i,j,n,nm1,jj;
    
    n   = pc->nn-1;
    nm1 = n-1;
    for (i = 0;i<n;i++) {
        xni   = pc->nn-i+1;
        pc->hr[i] = xni * pc->pr[i]/(float)n;
        pc->hi[i] = xni * pc->pi[i]/(float)n;
    }
    for (jj = 0;jj<l1;jj++) {
        if (cmod(pc->hr[n-1],pc->hi[n-1]) > 
                pc->eta * 10.0 * cmod(pc->pr[n-1],pc->pi[n-1])){
            cdivid(-pc->pr[pc->nn-1],-pc->pi[pc->nn-1],pc->hr[n-1],pc->hi[n-1],
                   &(pc->tr),&(pc->ti));
            for (i = 0;i<nm1;i++) {
                j = pc->nn-i-1;
                t1 = pc->hr[j-1];
                t2 = pc->hi[j-1];
                pc->hr[j] = pc->tr * t1 - pc->ti * t2 + pc->pr[j];
                pc->hi[j] = pc->tr * t2 + pc->ti * t1 + pc->pi[j];
            }
            pc->hr[0] = pc->pr[0];
            pc->hi[0] = pc->pi[0];
            continue; 
        }
        /*
         * if the constant term is essentially zero, shift h coefficients.
         */
        for (i = 0;i<nm1;i++) {
            j = pc->nn-i-1;
            pc->hr[j] = pc->hr[j-1];
            pc->hi[j] = pc->hi[j-1];
        }
        pc->hr[0] = 0.0;
        pc->hi[0] = 0.0;
    }
}



/*
c computes l2 fixed-shift h polynomials and tests for
c convergence.
c initiates a variable-shift iteration and returns with the
c approximate zero if successful.
c l2 - limit of fixed shift steps
c zr,zi - approximate zero if conv is .true.
c conv  - logical indicating convergence of stage 3 iteration
c common area
*/
void fxshft(POLY_COMMON *pc, int l2,double *zr,double *zi, int *conv) 
{
    double otr,oti,svsr,svsi;
    int test,pasd,bool;
    int i,j,n;
    
    n = pc->nn-1;
    /*
     * evaluate p at s.
     */
    polyev(pc->nn,pc->sr,pc->si,pc->pr,pc->pi,
           pc->qpr,pc->qpi,&(pc->pvr),&(pc->pvi));
    test = TRUE;
    pasd = FALSE;
    /*
     * calculate first t = -p(s)/h(s).
     */
    calct(pc,&bool);
    /*
     * main loop for one second stage step.
     */
    for (j = 0;j<l2;j++) {
        otr = pc->tr;
        oti = pc->ti;
        /*
         * compute next h polynomial and new t.
         */
        nexth(pc,bool);
        calct(pc,&bool);
        *zr = pc->sr + pc->tr;
        *zi = pc->si + pc->ti;
        /*
         * test for convergence unless stage 3 has failed once or this
         * is the last h polynomial .
         */
        if ( bool || !test || j == l2-1) 
            continue; 
        if (cmod(pc->tr-otr,pc->ti-oti) >= 0.5 * cmod(*zr,*zi)) {
            pasd = FALSE;
            continue;
        }
        if (!pasd) {
            pasd = TRUE;
            continue; 
        }
        /*
         * the weak convergence test has been passed twice, start the
         * third stage iteration, after saving the current h polynomial
         * and shift.
         */
        for (i = 0;i<n;i++) {
            pc->shr[i] = pc->hr[i];
            pc->shi[i] = pc->hi[i];
        }
        svsr = pc->sr;
        svsi = pc->si;
        vrshft(pc,10,zr,zi,conv);
        if (*conv) 
            return;
        /*
         * the iteration failed to converge. turn off testing and restore
         * h,s,pv and t.
         */
        test = FALSE;
        for (i = 0;i<n;i++) {
            pc->hr[i] = pc->shr[i];
            pc->hi[i] = pc->shi[i];
        }
        pc->sr = svsr;
        pc->si = svsi;
        polyev(pc->nn,pc->sr,pc->si,pc->pr,pc->pi,
               pc->qpr,pc->qpi,&(pc->pvr),&(pc->pvi));
        calct(pc,&bool);
    } 
    /*
     * attempt an iteration with final h polynomial from second stage.
     */
    vrshft(pc,10,zr,zi,conv);
}
    



/*
c carries out the third stage iteration.
c l3 - limit of steps in stage 3.
c zr,zi   - on entry contains the initial iterate, if the
c iteration converges it contains the final iterate
c on exit.
c conv    -  .true. if iteration converges
*/
void vrshft(POLY_COMMON *pc, int l3,double *zr,double *zi,int *conv)
{
    double mp,ms,omp,relstp,r1,r2,tp;
    int b,bool;
    int i,j;
      
    *conv     = FALSE;
    b        = FALSE;
    pc->sr   = *zr;
    pc->si   = *zi;
    /*
     * main loop for stage three
     */
    for (i = 0;i<l3;i++); {
        /*
         * evaluate p at s and test for convergence.
         */
        polyev(pc->nn,pc->sr,pc->si,pc->pr,pc->pi,
               pc->qpr,pc->qpi,&(pc->pvr),&(pc->pvi));
        mp = cmod(pc->pvr,pc->pvi);
        ms = cmod(pc->sr,pc->si);
        if (mp <= 20.0 * errev(pc->nn,pc->qpr,pc->qpi,ms,mp,pc->are,pc->mre)) {
            /*
             * polynomial value is smaller in value than a bound on the error
             * in evaluating p, terminate the iteration.
             */
            *conv = TRUE;
            *zr = pc->sr;
            *zi = pc->si;
            return;
        }
        do {
            if (i != 0) { 
                if (!(b || mp < omp || relstp >= 0.05)) {
                    /*
                     * iteration has stalled. probably a cluster of zeros. do 5 fixed
                     * shift steps into the cluster to force one zero to dominate.
                     */
                    tp = relstp;
                    b  = TRUE;
                    if (relstp < pc->eta) 
                        tp = pc->eta;
                    r1 = sqrt(tp);
                    r2 = pc->sr * (1.0 + r1)- pc->si * r1;
                    pc->si = pc->sr * r1 + pc->si * (1.0 + r1);
                    pc->sr = r2;
                    polyev(pc->nn,pc->sr,pc->si,pc->pr,pc->pi,
                           pc->qpr,pc->qpi,&(pc->pvr),&(pc->pvi));
                    for (j = 1;j<=5;j++) {
                         calct(pc,&bool);
                         nexth(pc,bool);
                    }
                    omp = pc->infin;
                    goto L50;
                }           
                /*
                 * exit if polynomial value increases significantly.
                 */
                if (mp * 0.1 > omp) 
                    return;
     
            }     
            omp = mp;
            /*
             * calculate next iterate.
             */
   L50:
            calct(pc,&bool);
            nexth(pc,bool);
            calct(pc,&bool);
        } while (bool) ;
        relstp = cmod(pc->tr,pc->ti)/cmod(pc->sr,pc->si);
        pc->sr += pc->tr;
        pc->si += pc->ti;
    }  
}




/*
c computes  t = -p(s)/h(s).
c bool   - logical, set true if h(s) is essentially zero.
*/
void calct(POLY_COMMON *pc, int *bool)
{
    double hvr,hvi;
    int n;

    n = pc->nn-1;
    /*
     * evaluate h(s).
     */
    polyev(n,pc->sr,pc->si,pc->hr,pc->hi,
           pc->qhr,pc->qhi,&hvr,&hvi);
    *bool = (cmod(hvr,hvi) <= pc->are * 10.0 * cmod(pc->hr[n-1],pc->hi[n-1]));
    if (!(*bool)) {
        cdivid(-pc->pvr,-pc->pvi,hvr,hvi,&(pc->tr),&(pc->ti));
        return;
    } 
    pc->tr = 0.0;
    pc->ti = 0.0;
}


/*
c calculates the next shifted h polynomial.
c bool   -  logical, if .true. h(s) is essentially zero
*/
void nexth(POLY_COMMON *pc, int bool)
{
    double t1,t2;
    int j,n,nm1;

    n = pc->nn-1;
    nm1 = n-1;
    if (!bool) {
        for (j = 1;j<n;j++) {
            t1 = pc->qhr[j-1];
            t2 = pc->qhi[j-1];
            pc->hr[j] = pc->tr * t1 - pc->ti * t2 + pc->qpr[j];
            pc->hi[j] = pc->tr * t2 + pc->ti * t1 + pc->qpi[j];
        }
        pc->hr[0] = pc->qpr[0];
        pc->hi[0] = pc->qpi[0];
        return;
    }
    /*
     * if h(s) is zero replace h with qh.
     */
    for (j = 1;j<n;j++) {
        pc->hr[j] = pc->qhr[j-1];
        pc->hi[j] = pc->qhi[j-1];
    }
    pc->hr[0] = 0.0;
    pc->hi[0] = 0.0;
}



/*
c evaluates a polynomial  p  at  s  by the horner recurrence
c placing the partial sums in q and the computed value in pv.
*/
void polyev(int nn, double sr, double si, double *pr, double *pi, 
            double *qr, double *qi, double *pvr, double *pvi)
{
    double t;
    int i;

    qr[0] = pr[0];
    qi[0] = pi[0];
    *pvr  = qr[0];
    *pvi  = qi[0];
    for (i = 1;i<nn;i++) {
        t     = (*pvr) * sr - (*pvi) * si + pr[i];
        *pvi  = (*pvr) * si + (*pvi) * sr + pi[i];
        *pvr  = t;
        qr[i] = *pvr;
        qi[i] = *pvi;
    }
}      


/*
c bounds the error in evaluating the polynomial by the horner
c recurrence.
c qr,qi - the partial sums
c ms    -modulus of the point
c mp    -modulus of polynomial value
c are, mre -error bounds on complex addition and multiplication
*/
double errev(int nn,double *qr,double *qi,
             double ms,double mp,double are,double mre)
{
    double e;
    int i;

    e = cmod(qr[0],qi[0])*mre/(are+mre);
    for (i = 0;i<nn;i++)
        e = e * ms + cmod(qr[i],qi[i]);
    return e*(are+mre)-mp*mre;
}      

/*
c cauchy computes a lower bound on the moduli of the zeros of a
c polynomial - pt is the modulus of the coefficients.
*/
double cauchy(int nn, double *pt, double *q)
{
    double x,xm,f,dx,df;
    int i,n;

    pt[nn-1] = -pt[nn-1];
    /*
     * compute upper estimate of bound.
     */
    n = nn-1;
    x = exp( (log(-pt[nn-1]) - log(pt[0]))/(float)n) ;
    if (pt[n-1] != 0.0) {
        /*
         * if newton step at the origin is better, use it.
         */
         xm = -pt[nn-1]/pt[n-1];
         if (xm < x) 
             x = xm;
    }
    /*
     * chop the interval (0,x) until f le 0.
     */
    while (1) {
        xm = x * 0.1;
        f = pt[0];
        for (i = 1;i<nn;i++)
            f = f * xm + pt[i];
        if (f <= 0.0) 
            break;
        x = xm;
    }
    dx = x;
    /*
     * do newton iteration until x converges to two decimal places.
     */
    while (fabs(dx/x) > 0.005) {
        q[0] = pt[0];
        for (i = 1;i<nn;i++)
            q[i] = q[i-1] * x + pt[i];
        f  = q[nn-1];
        df = q[0];
        for (i = 1;i<n;i++)
            df = df * x + q[i];
        dx = f/df;
        x -= dx;
    }
    return x;
}
      

/*
c returns a scale factor to multiply the coefficients of the
c polynomial. the scaling is done to avoid overflow and to avoid
c undetected underflow interfering with the convergence
c criterion.  the factor is a power of the base.
c pt - modulus of coefficients of p
c eta,infin,smalno,base - constants describing the
c floating point arithmetic.
*/
double scaleit(int nn, double *pt, double eta, double infin, 
               double smalno, double base)
{
    double hi,lo,dmax,dmin,x,sc,l;
    int i;
    /*
     * find largest and smallest moduli of coefficients.
     */
    hi   = sqrt(infin);
    lo   = smalno/eta;
    dmax = 0.0;
    dmin = infin;
    for (i = 0;i<nn;i++) {
        x = pt[i];
        if (x > dmax) 
            dmax = x;
        if (x != 0.0 && x < dmin) 
            dmin = x;
    }
    /*
     * scale only if there are very large or very small components.
     */
    if (dmin >= lo && dmax <= hi) 
        return 1.0;
    x = lo/dmin;
    if (x <= 1.0) 
        sc = 1.0/(sqrt(dmax)*sqrt(dmin));
    else {
        sc = x;
        if (infin/sc > dmax) 
            sc = 1.0;
    }
    l = log(sc)/log(base) + 0.500;
    return pow(base,l);
}      


/*
 * complex division c = a/b, avoiding overflow.
 */
void cdivid(double ar,double ai,double br,double bi,double *cr,double *ci)                             
{
    double r,d,t,infin;

    if (br == 0.0  && bi == 0.0) {
        /*
         * division by zero, c = infinity.
         */
        mcon (&t,&infin,&t,&t);
        *cr = infin;
        *ci = infin;
        return;
    }
    if (fabs(br) < fabs(bi)) {
        r = br/bi;
        d = bi+r*br;
        *cr = (ar*r+ai)/d;
        *ci = (ai*r-ar)/d;
        return;
    } 
    r  = bi/br;
    d  = br+r*bi;
    *cr = (ar+ai*r)/d;
    *ci = (ai-ar*r)/d;
}
      

/*
 * modulus of a complex number avoiding overflow.
 */
 
double cmod(double r, double i)
{
    double ar,ai;
    ar = fabs(r);
    ai = fabs(i);
    if (ar < ai)
        return ai * sqrt(1.0+pow(ar/ai,2));
    if (ar > ai) 
        return ar * sqrt(1.0+pow(ai/ar,2));
    return ar * sqrt(2.0);
}
      

/*
c mcon provides machine constants used in various parts of the
c program. the user may either set them directly or use the
c statements below to compute them. the meaning of the four
c constants are -
c eta       the maximum relative representation error
c which can be described as the smallest positive
c floating-point number such that 1.0 + eta is
c greater than 1.0.
c infiny    the largest floating-point number
c smalno    the smallest positive floating-point number
c base      the base of the floating-point number system used
c let t be the number of base-digits in each floating-point
c number(double precision). then eta is either .5*b**(1-t)
c or b**(1-t) depending on whether rounding or truncation
c is used.
c let m be the largest exponent and n the smallest exponent
c in the number system. then infiny is (1-base**(-t))*base**m
c and smalno is base**n.
c the values for base,t,m,n below correspond to the ibm/360.
*/
void mcon(double *eta,double *infiny,double *smalno,double *base)
{
    int t = 8;
    
    *base = 16.0;
    *eta = pow(*base,1-t);
    /*
     * infiny = 1.797693134862315708e+308
     * smalno = 4.94065645841246544e-324
     * hong just testing for sgi compatable 8/22/94
     */
    *infiny = 1.797693134862315708e+150;
    *smalno = 4.94065645841246544e-150;
}







