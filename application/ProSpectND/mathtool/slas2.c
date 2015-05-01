

#include <math.h>
#include "complex.h"
#include "mathtool.h"



void slas2(float f__, float g__, float h__, float *ssmin, float *ssmax)
{
/*  -- LAPACK auxiliary routine (version 2.0) --   
       Univ. of Tennessee, Univ. of California Berkeley, NAG Ltd.,   
       Courant Institute, Argonne National Lab, and Rice University   
       September 30, 1994   


    Purpose   
    =======   

    SLAS2  computes the singular values of the 2-by-2 matrix   
       [  F   G  ]   
       [  0   H  ].   
    On return, SSMIN is the smaller singular value and SSMAX is the   
    larger singular value.   

    Arguments   
    =========   

    F       (input) REAL   
            The (1,1) element of the 2-by-2 matrix.   

    G       (input) REAL   
            The (1,2) element of the 2-by-2 matrix.   

    H       (input) REAL   
            The (2,2) element of the 2-by-2 matrix.   

    SSMIN   (output) REAL   
            The smaller singular value.   

    SSMAX   (output) REAL   
            The larger singular value.   

    Further Details   
    ===============   

    Barring over/underflow, all output quantities are correct to within   
    a few units in the last place (ulps), even in the absence of a guard 
  
    digit in addition/subtraction.   

    In IEEE arithmetic, the code works correctly if one matrix element is 
  
    infinite.   

    Overflow will not occur unless the largest singular value itself   
    overflows, or is within a few ulps of overflow. (On machines with   
    partial overflow, like the Cray, overflow may occur if the largest   
    singular value is within a factor of 2 of overflow.)   

    Underflow is harmless if underflow is gradual. Otherwise, results   
    may correspond to a matrix modified by perturbations of size near   
    the underflow threshold.   

    ==================================================================== 
*/
    /* System generated locals */
    float r__1, r__2;
    /* Builtin functions */
    static float fhmn, fhmx, c, fa, ga, ha, as, at, au;



    fa = fabs(f__);
    ga = fabs(g__);
    ha = fabs(h__);
    fhmn = min(fa,ha);
    fhmx = max(fa,ha);
    if (fhmn == 0.f) {
	*ssmin = 0.f;
	if (fhmx == 0.f) {
	    *ssmax = ga;
	} else {
            /*
             * Computing 2nd power 
             */
	    r__1 = min(fhmx,ga) / max(fhmx,ga);
	    *ssmax = max(fhmx,ga) * sqrt(r__1 * r__1 + 1.f);
	}
    } else {
	if (ga < fhmx) {
	    as = fhmn / fhmx + 1.f;
	    at = (fhmx - fhmn) / fhmx;
            /*
             *   Computing 2nd power 
             */
	    r__1 = ga / fhmx;
	    au = r__1 * r__1;
	    c = 2.f / (sqrt(as * as + au) + sqrt(at * at + au));
	    *ssmin = fhmn * c;
	    *ssmax = fhmx / c;
	} else {
	    au = fhmx / ga;
	    if (au == 0.f) {

                /*
                 *  Avoid possible harmful underflow if exponent range   
                 *  asymmetric (true SSMIN may not underflow even if   
                 *  AU underflows) 
                 */

		*ssmin = fhmn * fhmx / ga;
		*ssmax = ga;
	    } else {
		as = fhmn / fhmx + 1.f;
		at = (fhmx - fhmn) / fhmx;
                /*
                 *  Computing 2nd power 
                 */
		r__1 = as * au;
                /*
                 * Computing 2nd power 
                 */
		r__2 = at * au;
		c = 1.f / (sqrt(r__1 * r__1 + 1.f) + sqrt(r__2 * r__2 + 1.f));
		*ssmin = fhmn * c * au;
		*ssmin += *ssmin;
		*ssmax = ga / (c + c);
	    }
	}
    }

} 

