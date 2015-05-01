
#include <math.h>
#include "complex.h"
#include "mathtool.h"



void slasq2(int m__, float *q, float *e, float *qq, float *ee,
	    float eps, float tol2, float small2, float *sup, int *kend, 
	    int *info)
{
/*  -- LAPACK routine (version 2.0) --   
       Univ. of Tennessee, Univ. of California Berkeley, NAG Ltd.,   
       Courant Institute, Argonne National Lab, and Rice University   
       September 30, 1994   


       Purpose   
       =======   

       SLASQ2 computes the singular values of a real N-by-N unreduced   
       bidiagonal matrix with squared diagonal elements in Q and   
       squared off-diagonal elements in E. The singular values are   
       computed to relative accuracy TOL, barring over/underflow or   
       denormalization.   

       Arguments   
       =========   

    M       (input) INTEGER   
            The number of rows and columns in the matrix. M >= 0.   

    Q       (output) REAL array, dimension (M)   
            On normal exit, contains the squared singular values.   

    E       (workspace) REAL array, dimension (M)   

    QQ      (input/output) REAL array, dimension (M)   
            On entry, QQ contains the squared diagonal elements of the   
            bidiagonal matrix whose SVD is desired.   
            On exit, QQ is overwritten.   

    EE      (input/output) REAL array, dimension (M)   
            On entry, EE(1:N-1) contains the squared off-diagonal   
            elements of the bidiagonal matrix whose SVD is desired.   
            On exit, EE is overwritten.   

    EPS     (input) REAL   
            Machine epsilon.   

    TOL2    (input) REAL   
            Desired relative accuracy of computed eigenvalues   
            as defined in SLASQ1.   

    SMALL2  (input) REAL   
            A threshold value as defined in SLASQ1.   

    SUP     (input/output) REAL   
            Upper bound for the smallest eigenvalue.   

    KEND    (input/output) INTEGER   
            Index where minimum d occurs.   

    INFO    (output) INTEGER   
            = 0:  successful exit   
            < 0:  if INFO = -i, the i-th argument had an illegal value   
            > 0:  if INFO = i, the algorithm did not converge;  i   
                  specifies how many superdiagonals did not converge.   

    ===================================================================== 
  

    
   Parameter adjustments   
       Function Body */
    /* System generated locals */
    float r__1, r__2, r__3, r__4;
    /* Local variables */
    static float xinf;
    static int n;
    static float sigma, qemax;
    static int iconv;
    static int iphase;
    static float xx, yy;
    static int off, isp, off1;


#define EE(I) ee[(I)-1]
#define QQ(I) qq[(I)-1]
#define E(I) e[(I)-1]
#define Q(I) q[(I)-1]


    n = m__;

    /*
     *     Set the default maximum number of iterations 
     */

    off = 0;
    off1 = off + 1;
    sigma = 0.f;
    xinf = 0.f;
    iconv = 0;
    iphase = 2;

    /*
     *     Try deflation at the bottom   
     *     1x1 deflation 
     */

L10:
    if (n <= 2) {
	goto L20;
    }
    /*
     * Computing MAX 
     */
    r__1 = QQ(n), r__1 = max(r__1,xinf);
    if (EE(n - 1) <= max(r__1,small2) * tol2) {
	Q(n) = QQ(n);
	--n;
	if (*kend > n) {
	    *kend = n;
	}
        /*
         * Computing MIN 
         */
	r__1 = QQ(n), r__2 = QQ(n - 1);
	*sup = min(r__1,r__2);
	goto L10;
    }

    /*
     *     2x2 deflation 
     *    Computing MAX 
     */
    r__1 = max(xinf,small2), r__2 = QQ(n) / (QQ(n) + EE(n - 1) + QQ(n - 1)) *
	     QQ(n - 1);
    if (EE(n - 2) <= max(r__1,r__2) * tol2) {
        /*
         * Computing MAX 
         */
	r__1 = QQ(n), r__2 = QQ(n - 1), r__1 = max(r__1,r__2), r__2 = EE(n - 
		1);
	qemax = max(r__1,r__2);
	if (qemax != 0.f) {
	    if (qemax == QQ(n - 1)) {
                /*
                 * Computing 2nd power 
                 */
		r__1 = (QQ(n) - QQ(n - 1) + EE(n - 1)) / qemax;
		xx = (QQ(n) + QQ(n - 1) + EE(n - 1) + qemax * sqrt(r__1 * 
			r__1 + EE(n - 1) * 4.f / qemax)) * .5f;
	    } else if (qemax == QQ(n)) {
                /*
                 * Computing 2nd power 
                 */
		r__1 = (QQ(n - 1) - QQ(n) + EE(n - 1)) / qemax;
		xx = (QQ(n) + QQ(n - 1) + EE(n - 1) + qemax * sqrt(r__1 * 
			r__1 + EE(n - 1) * 4.f / qemax)) * .5f;
	    } else {
                /*
                 * Computing 2nd power 
                 */
		r__1 = (QQ(n) - QQ(n - 1) + EE(n - 1)) / qemax;
		xx = (QQ(n) + QQ(n - 1) + EE(n - 1) + qemax * sqrt(r__1 * 
			r__1 + QQ(n - 1) * 4.f / qemax)) * .5f;
	    }
            /*
             * Computing MAX 
             */
	    r__1 = QQ(n), r__2 = QQ(n - 1);
            /*
             * Computing MIN 
             */
	    r__3 = QQ(n), r__4 = QQ(n - 1);
	    yy = max(r__1,r__2) / xx * min(r__3,r__4);
	} else {
	    xx = 0.f;
	    yy = 0.f;
	}
	Q(n - 1) = xx;
	Q(n) = yy;
	n += -2;
	if (*kend > n) {
	    *kend = n;
	}
	*sup = QQ(n);
	goto L10;
    }

L20:
    if (n == 0) {

        /*
         *         The lower branch is finished 
         */

	if (off == 0) {

            /*
             *         No upper branch; return to SLASQ1 
             */

	    return ;
	} else {

            /*
             *         Going back to upper branch 
             */

	    xinf = 0.f;
	    if (EE(off) > 0.f) {
		isp = round(EE(off));
		iphase = 1;
	    } else {
		isp = -round(EE(off));
		iphase = 2;
	    }
	    sigma = E(off);
	    n = off - isp + 1;
	    off1 = isp;
	    off = off1 - 1;
	    if (n <= 2) {
		goto L20;
	    }
	    if (iphase == 1) {
                /*
                 * Computing MIN 
                 */
		r__1 = Q(n + off), r__2 = Q(n - 1 + off), r__1 = min(r__1,
			r__2), r__2 = Q(n - 2 + off);
		*sup = min(r__1,r__2);
	    } else {
                /*
                 * Computing MIN 
                 */
		r__1 = QQ(n + off), r__2 = QQ(n - 1 + off), r__1 = min(r__1,
			r__2), r__2 = QQ(n - 2 + off);
		*sup = min(r__1,r__2);
	    }
	    *kend = 0;
	    iconv = -3;
	}
    } else if (n == 1) {

        /*
         *     1x1 Solver 
         */

	if (iphase == 1) {
	    Q(off1) += sigma;
	} else {
	    Q(off1) = QQ(off1) + sigma;
	}
	n = 0;
	goto L20;

        /*
         *     2x2 Solver 
         */

    } else if (n == 2) {
	if (iphase == 2) {
            /*
             * Computing MAX 
             */
	    r__1 = QQ(n + off), r__2 = QQ(n - 1 + off), r__1 = max(r__1,r__2),
		     r__2 = EE(n - 1 + off);
	    qemax = max(r__1,r__2);
	    if (qemax != 0.f) {
		if (qemax == QQ(n - 1 + off)) {
                    /*
                     * Computing 2nd power 
                     */
		    r__1 = (QQ(n + off) - QQ(n - 1 + off) + EE(n - 1 + off)) /
			     qemax;
		    xx = (QQ(n + off) + QQ(n - 1 + off) + EE(n - 1 + off) + 
			    qemax * sqrt(r__1 * r__1 + EE(off + n - 1) * 4.f /
			     qemax)) * .5f;
		} else if (qemax == QQ(n + off)) {
                    /*
                     * Computing 2nd power 
                     */
		    r__1 = (QQ(n - 1 + off) - QQ(n + off) + EE(n - 1 + off)) /
			     qemax;
		    xx = (QQ(n + off) + QQ(n - 1 + off) + EE(n - 1 + off) + 
			    qemax * sqrt(r__1 * r__1 + EE(n - 1 + off) * 4.f /
			     qemax)) * .5f;
		} else {
                    /*
                     * Computing 2nd power 
                     */
		    r__1 = (QQ(n + off) - QQ(n - 1 + off) + EE(n - 1 + off)) /
			     qemax;
		    xx = (QQ(n + off) + QQ(n - 1 + off) + EE(n - 1 + off) + 
			    qemax * sqrt(r__1 * r__1 + QQ(n - 1 + off) * 4.f /
			     qemax)) * .5f;
		}
                /*
                 * Computing MAX 
                 */
		r__1 = QQ(n + off), r__2 = QQ(n - 1 + off);
                /*
                 * Computing MIN 
                 */
		r__3 = QQ(n + off), r__4 = QQ(n - 1 + off);
		yy = max(r__1,r__2) / xx * min(r__3,r__4);
	    } else {
		xx = 0.f;
		yy = 0.f;
	    }
	} else {
            /*
             * Computing MAX 
             */
	    r__1 = Q(n + off), r__2 = Q(n - 1 + off), r__1 = max(r__1,r__2), 
		    r__2 = E(n - 1 + off);
	    qemax = max(r__1,r__2);
	    if (qemax != 0.f) {
		if (qemax == Q(n - 1 + off)) {
                    /*
                     * Computing 2nd power 
                     */
		    r__1 = (Q(n + off) - Q(n - 1 + off) + E(n - 1 + off)) / 
			    qemax;
		    xx = (Q(n + off) + Q(n - 1 + off) + E(n - 1 + off) + 
			    qemax * sqrt(r__1 * r__1 + E(n - 1 + off) * 4.f / 
			    qemax)) * .5f;
		} else if (qemax == Q(n + off)) {
                    /*
                     * Computing 2nd power 
                     */
		    r__1 = (Q(n - 1 + off) - Q(n + off) + E(n - 1 + off)) / 
			    qemax;
		    xx = (Q(n + off) + Q(n - 1 + off) + E(n - 1 + off) + 
			    qemax * sqrt(r__1 * r__1 + E(n - 1 + off) * 4.f / 
			    qemax)) * .5f;
		} else {
                    /*
                     * Computing 2nd power 
                     */
		    r__1 = (Q(n + off) - Q(n - 1 + off) + E(n - 1 + off)) / 
			    qemax;
		    xx = (Q(n + off) + Q(n - 1 + off) + E(n - 1 + off) + 
			    qemax * sqrt(r__1 * r__1 + Q(n - 1 + off) * 4.f / 
			    qemax)) * .5f;
		}
                /*
                 * Computing MAX 
                 */
		r__1 = Q(n + off), r__2 = Q(n - 1 + off);
                /*
                 * Computing MIN 
                 */
		r__3 = Q(n + off), r__4 = Q(n - 1 + off);
		yy = max(r__1,r__2) / xx * min(r__3,r__4);
	    } else {
		xx = 0.f;
		yy = 0.f;
	    }
	}
	Q(n - 1 + off) = sigma + xx;
	Q(n + off) = yy + sigma;
	n = 0;
	goto L20;
    }
    slasq3(&n, &Q(off1), &E(off1), &QQ(off1), &EE(off1), sup, &sigma, kend, &
	    off, &iphase, iconv, eps, tol2, small2);
    if (*sup < 0.f) {
	*info = n + off;
	return ;
    }
    off1 = off + 1;
    goto L20;

} 

