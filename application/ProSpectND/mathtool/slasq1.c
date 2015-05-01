
#include <math.h>
#include "complex.h"
#include "mathtool.h"


void slasq1(int n__, float *d, float *e, float *work, 
	    int *info)
{
/*  -- LAPACK routine (version 2.0) --   
       Univ. of Tennessee, Univ. of California Berkeley, NAG Ltd.,   
       Courant Institute, Argonne National Lab, and Rice University   
       September 30, 1994   


       Purpose   
       =======   

       SLASQ1 computes the singular values of a real N-by-N bidiagonal   
       matrix with diagonal D and off-diagonal E. The singular values are 
  
       computed to high relative accuracy, barring over/underflow or   
       denormalization. The algorithm is described in   

       "Accurate singular values and differential qd algorithms," by   
       K. V. Fernando and B. N. Parlett,   
       Numer. Math., Vol-67, No. 2, pp. 191-230,1994.   

       See also   
       "Implementation of differential qd algorithms," by   
       K. V. Fernando and B. N. Parlett, Technical Report,   
       Department of Mathematics, University of California at Berkeley,   
       1994 (Under preparation).   

       Arguments   
       =========   

    N       (input) INTEGER   
            The number of rows and columns in the matrix. N >= 0.   

    D       (input/output) REAL array, dimension (N)   
            On entry, D contains the diagonal elements of the   
            bidiagonal matrix whose SVD is desired. On normal exit,   
            D contains the singular values in decreasing order.   

    E       (input/output) REAL array, dimension (N)   
            On entry, elements E(1:N-1) contain the off-diagonal elements 
  
            of the bidiagonal matrix whose SVD is desired.   
            On exit, E is overwritten.   

    WORK    (workspace) REAL array, dimension (2*N)   

    INFO    (output) INTEGER   
            = 0:  successful exit   
            < 0:  if INFO = -i, the i-th argument had an illegal value   
            > 0:  if INFO = i, the algorithm did not converge;  i   
                  specifies how many superdiagonals did not converge.   

    ===================================================================== 
  

    
   Parameter adjustments   
       Function Body */
    /* Table of constant values */
    static double c_b8 = .125;
    static int c__1 = 1;
    static int c__0 = 0;
    
    /* System generated locals */
    int i__1, i__2;
    float r__1, r__2, r__3, r__4;
    double d__1;
    /* Local variables */
    static int kend, ierr;
    static int i, j, m;
    static float sfmin, sigmn, sigmx;
    static float small2;
    static int ke;
    static float dm, dx;
    static int ny;
    static float thresh;
    static float tolmul;
    static int restrt;
    static float scl, eps, tol, sig1, sig2, tol2;



#define WORK(I) work[(I)-1]
#define E(I) e[(I)-1]
#define D(I) d[(I)-1]


    *info = 0;
    if (n__ < 0) {
	*info = -2;
	i__1 = -(*info);
	return ;
    } else if (n__ == 0) {
	return ;
    } else if (n__ == 1) {
	D(1) = fabs(D(1));
	return ;
    } else if (n__ == 2) {
	slas2(D(1), E(1), D(2), &sigmn, &sigmx);
	D(1) = sigmx;
	D(2) = sigmn;
	return ;
    }

    /* 
     *    Estimate the largest singular value 
     */

    sigmx = 0.f;
    i__1 = n__ - 1;
    for (i = 1; i <= n__-1; ++i) {
        /*
         * Computing MAX 
         */
	r__2 = sigmx, r__3 = (r__1 = E(i), fabs(r__1));
	sigmx = max(r__2,r__3);
    }

    /*
     *     Early return if sigmx is zero (matrix is already diagonal) 
     */

    if (sigmx == 0.f) {
	goto L70;
    }

    i__1 = n__;
    for (i = 1; i <= n__; ++i) {
	D(i) = (r__1 = D(i), fabs(r__1));
        /*
         *   Computing MAX 
         */
	r__1 = sigmx, r__2 = D(i);
	sigmx = max(r__1,r__2);
    }

    /*
     *     Get machine parameters 
     */

    eps = slamch("EPSILON");
    sfmin = slamch("SAFE MINIMUM");

   /*  Compute singular values to relative accuracy TOL   
       It is assumed that tol**2 does not underflow.   

   Computing MAX   
   Computing MIN */
    d__1 = (double) eps;
    r__3 = 100.f, r__4 = pow(d__1, c_b8);
    r__1 = 10.f, r__2 = min(r__3,r__4);
    tolmul = max(r__1,r__2);
    tol = tolmul * eps;
    /*
     *   Computing 2nd power 
     */
    r__1 = tol;
    tol2 = r__1 * r__1;

    thresh = sigmx * sqrt(sfmin) * tol;

    /* 
     *    Scale matrix so the square of the largest element is   
     *    1 / ( 256 * SFMIN ) 
     */

    scl = sqrt(1.f / (sfmin * 256.f));
    /*
     * Computing 2nd power 
     */
    r__1 = tolmul;
    small2 = 1.f / (r__1 * r__1 * 256.f);
    scopy(n__, &D(1), c__1, &WORK(1), c__1);
    i__1 = n__ - 1;
    scopy(i__1, &E(1), c__1, &WORK(n__ + 1), c__1);
    slascl("G", c__0, c__0, sigmx, scl, n__, c__1, &WORK(1), n__, &ierr)
	    ;
    i__1 = n__ - 1;
    i__2 = n__ - 1;
    slascl("G", c__0, c__0, sigmx, scl, i__1, c__1, &WORK(n__ + 1), 
	    i__2, &ierr);

    /*
     *     Square D and E (the input for the qd algorithm) 
     */

    i__1 = (n__ << 1) - 1;
    for (j = 1; j <= (n__<<1)-1; ++j) {
        /*
         * Computing 2nd power 
         */
	r__1 = WORK(j);
	WORK(j) = r__1 * r__1;
    }

    /*
     *     Apply qd algorithm 
     */

    m = 0;
    E(n__) = 0.f;
    dx = WORK(1);
    dm = dx;
    ke = 0;
    restrt = FALSE;
    i__1 = n__;
    for (i = 1; i <= n__; ++i) {
	if ((r__1 = E(i), fabs(r__1)) <= thresh || WORK(n__ + i) <= tol2 * (dm 
		/ (float) (i - m))) {
	    ny = i - m;
	    if (ny == 1) {
		goto L50;
	    } else if (ny == 2) {
		slas2(D(m + 1), E(m + 1), D(m + 2), &sig1, &sig2);
		D(m + 1) = sig1;
		D(m + 2) = sig2;
	    } else {
		kend = ke + 1 - m;
		slasq2(ny, &D(m + 1), &E(m + 1), &WORK(m + 1), &WORK(m + n__ 
			+ 1), eps, tol2, small2, &dm, &kend, info);

                /*
                 *      Return, INFO = number of unconverged superdiagonals 
                 */

		if (*info != 0) {
		    *info += i;
		    return ;
		}

                /*
                 *                 Undo scaling 
                 */

		i__2 = m + ny;
		for (j = m + 1; j <= m+ny; ++j) {
		    D(j) = sqrt(D(j));
		}
		slascl("G", c__0,c__0, scl, sigmx, ny, c__1, &D(m + 1)
			, ny, &ierr);
	    }
L50:
	    m = i;
	    if (i != n__) {
		dx = WORK(i + 1);
		dm = dx;
		ke = i;
		restrt = TRUE;
	    }
	}
	if (i != n__ && ! restrt) {
	    dx = WORK(i + 1) * (dx / (dx + WORK(n__ + i)));
	    if (dm > dx) {
		dm = dx;
		ke = i;
	    }
	}
	restrt = FALSE;
    }
    kend = ke + 1;

    /*
     *      Sort the singular values into decreasing order 
     */

L70:
    slasrt("D", n__, &D(1), info);

} 

