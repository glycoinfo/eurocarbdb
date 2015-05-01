/***************************************************************************
 C
 C   ref.:
 C       W.H. Press et al., Numerical Recipes, Chap 12, Cambridge Univ. Press
 C            (1986)
 C       E.T. Olejniczak and H.L. Eaton, J. Magn. Reson. 87, 628-632 (1990)
 C
 */
#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <assert.h>
#include "mathtool.h"



/*
 * dSort2: sorts arrays da and ib according to da.
 *         From Numerical Recipes "sort2".
 */

void dSort2(int n, double *da, int *ib )
{
    int l, j, ii, i, iib;
    double dda;

    if (n < 2) 
        return;

    da--; ib--;

    l  = (n >> 1)+1;
    ii = n;

    for( ; ; ) {
        if (l > 1) {
            dda=da[--l];
            iib=ib[l];
        }
        else {
            dda=da[ii];
            iib=ib[ii];
            da[ii]=da[1];
            ib[ii]=ib[1];

            if (--ii == 1) {
                da[1]=dda;
                ib[1]=iib;
                return;
            }
        }

        i=l;
        j=l << 1;

        while (j <= ii) {
            if (j < ii && da[j] < da[j+1]) 
               ++j;
   
            if (dda < da[j]) {
                da[i]=da[j];
                ib[i]=ib[j];
                j += (i=j);
            }
            else {
               j=ii+1;
           }
        }

        da[i]=dda;
        ib[i]=iib;
    }
}


#define MAXROOT	256
#define EPSS	6.0e-10
#define MAXIT	200

/*
 * Given the degree m and the m+1 complex coefficients a[0..m] of
 * the polynomial, and given eps the desired fractional accuracy,
 * and given a complex value x, this routine improves x by Laguerre's 
 * method until it converges to a root of the given polynomial.
 * For normal use polish should be input as FALSE. When polish
 * is TRUE, the routine ignores eps and instead attempts to improve x
 * (assumed to be a good initial guess) to the achievable
 * roundoff limit
 */
void laguer(dcomplex a[], int m, dcomplex *x, double eps, int polish)
{
    int j, iter;
    double err,dxold,cdx,abx;
    dcomplex sq,h,gp,gm,g2,g,b,d,dx,f,x1;

    dxold = Cabs_d(*x);
    /*
     * Loop over iterations up to allowed maximum
     */
    for (iter=1;iter<=MAXIT;iter++) {
        b = a[m];
        err = Cabs_d(b);
        d = f = Complex_d(0.0, 0.0);
        abx = Cabs_d(*x);
        /*
         * Efficient computation of the polynomial and its
         * first two derivatives
         */
        for (j=m-1;j>=0;j--) {
            f = Cadd_d(Cmul_d(*x,f),d);
            d = Cadd_d(Cmul_d(*x,d),b);
            b = Cadd_d(Cmul_d(*x,b),a[j]);
            err = Cabs_d(b) + abx * err;
        }
        /*
         * Estimate of roundoff error in evaluating polynomial
         */
        err *= EPSS;
        if (Cabs_d(b) <= err)
            /*
             * We are on the root
             */
            return;
         /*
          * The generic case: use Laguerre's formula
          */
         g  = Cdiv_d(d,b);
         g2 = Cmul_d(g,g);
         h  = Csub_d(g2,RCmul_d(2.0,Cdiv_d(f,b)));
         sq = Csqrt_d(RCmul_d((double)(m-1), 
                      Csub_d( RCmul_d((double)m,h), g2)));
         gp = Cadd_d(g,sq);
         gm = Csub_d(g,sq);
         if (Cabs_d(gp) < Cabs_d(gm))
             gp = gm;
         dx = Cdiv_d(Complex_d((double)m, 0.0), gp);
         x1 = Csub_d(*x, dx);
/*         if (x->r >= x1.r && x->i == x1.i)*/
         if (x->r == x1.r && x->i == x1.i)
             /*
              * Converged
              */
             return;
         *x = x1;
         cdx = Cabs_d(dx);
         /*
          * Extra test
          */
         if (iter > 6 && cdx >= dxold) 
             return;
         dxold = cdx;
         if (!polish)
             if (cdx <= eps * Cabs_d(*x))
                 /*
                  * Converged
                  */
                 return;
    }
    /*
     * Very unusual - can only occur for complex roots. 
     * Try a different initial guess for the root
     */
    printf("LP Warning: Too many LAGUER iterations.\n" );
}




/*
#define EPS	6.0e-6
*/
#define EPS     2.0e-26
#define MAXM	MAXROOT

/*
 * Given the degree m and the m+1 complex coefficients a[0..m] of
 * the polynomial, this routine successively call laguer() and finds all
 * m complex roots in roots[1..m]. The logical variable polish
 * should be TRUE if polishing (also by Laguerre's method) is desired,
 * FALSE if the roots will be subsequently polished by other means.
 */
void zroots(dcomplex a[], int m, dcomplex roots[], int polish)
{
    int jj,j,i;
    dcomplex x,b,c,ad[MAXM];
    /*
     * Copy of coefficients for successive deflation
     */
    for (j=0;j<=m;j++)
        ad[j] = a[j];
    /*
     * Loop over each root to be found
     */
    for (j=m;j>=1;j--) {
        /*
         * Start at zero to favor convergence to smallest remaining root
         */
        x = Complex_d(0.0, 0.0);
        /*
         * Find the root
         */
        laguer(ad, j, &x, EPS, 0);
        if (fabs(x.i) <= (2.0 * EPS * fabs(x.r)))
            x.i = 0.0;
        roots[j] = x;
        b = ad[j];
        /*
         * Forward deflation
         */
        for (jj=j-1;jj>=0;jj--) {
            c = ad[jj];
            ad[jj] = b;
            b = Cadd_d(Cmul_d(x,b),c);
        }
    }
    if (polish)
        /*
         * Polish the roots using the undeflated coefficients
         */
        for (j=1;j<=m;j++)
            laguer(a, m, &roots[j], EPS, TRUE);
    /*
     * Sort roots by their real parts by straight insertion
     */
    for (j=2;j<=m;j++) {
        x = roots[j];
        for (i=j-1;i>=1;i--) {
            if (roots[i].r <= x.r)
                break;
            roots[i+1] = roots[i];
        }
        roots[i+1] = x;
    }
}


#define NPMAX	MAXROOT
/*
 * Given the LP coefficients d[1..npoles], this routine finds
 * all roots of the characteristic polynomial, reflects any roots 
 * that are outside the unit circle back inside, and then returns
 * a modified set of d's.
 */
void fixrts(double *d, int npoles, int ifmode)
{
    int i,j,polish;
    dcomplex a[NPMAX], roots[NPMAX];
    
    a[npoles] = Complex_d(1.0, 0.0);
    /*
     * Set up complex coefficients for polynonial root finder
     */
    for (j=npoles-1;j>=0;j--)
        a[j] = Complex_d(-d[npoles-j], 0.0);
    polish = TRUE;
    /*
     * Find all roots
     */
    zroots(a,npoles,roots,polish);
    for (j=1;j<=npoles;j++)
        /*
         * Look for a root outside the unit circle
         */
        if (Cabs_d(roots[j]) > 1.0) {
            /*
             * and reflect it back inside
             */
            if (ifmode == 0)
                roots[j] = Cdiv_d(Complex_d(1.0, 0.0), Conjg_d(roots[j]));
            else
                roots[j] = RCmul_d(1.0/Cabs_d(roots[j]), roots[j]);
        }
    /*
     * Now reflect back the polynomial coefficients
     */
    a[0] = Csub_d(Complex_d(0.0, 0.0),roots[1]);
    a[1] = Complex_d(1.0, 0.0);
    /*
     * by looping over the roots
     */
    for (j=2;j<=npoles;j++) {
        a[j] = Complex_d(0.0, 0.0);
        /*
         * and synthetically multiplying
         */
        for (i=j;i>=2;i--)
            a[i-1] = Csub_d(a[i-1],Cmul_d(roots[i],a[i-1]));
        a[0] = Csub_d(Complex_d(0.0, 0.0), Cmul_d(roots[j],a[0]));
    }
    /*
     * The polynonial coefficients are guaranteed to be real,
     * so we need only return the real part as new LP coefficients
     */
    for (j=0;j<=npoles-1;j++)
        d[npoles-j] = -a[j].r;
}


/*
 * Given a real vector data[1..n], and given m, this routine returns a vector
 * cof[1..m] with cof[j] = aj, and a scalar pm = a0, which are the
 * coefficients for Maximum Entropy Method spectral estimation
 */
void memcof(float data[], int n, int m, double *pm, double cof[])
{
    int k,j,i;
    double  p = 0.0, *wk1, *wk2, *wkm;

    wk1 = (double*) malloc(sizeof(double)*n);
    assert(wk1);
    wk1--;
    wk2 = (double*) malloc(sizeof(double)*n);
    assert(wk2);
    wk2--;
    wkm = (double*) malloc(sizeof(double)*m);
    assert(wkm);
    wkm--;
    for (j=1;j<=n;j++)
        p += data[j] * data[j];
    *pm = p/m;
    wk1[1] = data[1];
    wk2[n-1] = data[n];
    for (j=2;j<=n-1;j++) {
        wk1[j] = data[j];
        wk2[j-1] = data[j];
    }
    for (k=1;k<=m;k++) {
        double  num = 0.0, denom = 0.0;
        for (j=1;j<=(n-k);j++) {
            num += wk1[j] * wk2[j];
            denom += wk1[j] * wk1[j] + wk2[j] * wk2[j];
        }
        /*
         * Can occurr, so test for it
         */
        if (denom == 0.0) 
            break;
        cof[k] = 2.0 * num/denom;
        *pm *= (1.0 - cof[k] * cof[k]);
        for (i=1;i<=(k-1);i++) 
            cof[i] = wkm[i] - cof[k] * wkm[k-i];
        if (k == m) 
            break;
        for (i=1;i<=k;i++) 
            wkm[i] = cof[i];
        for (j=1;j<=(n-k-1);j++) {
            wk1[j] -= wkm[k] * wk2[j];
            wk2[j]  = wk2[j+1] - wkm[k] * wk1[j+1];
        }
    }   
    wk1++;
    free(wk1);
    wk2++;
    free(wk2);
    wkm++;
    free(wkm);
}

