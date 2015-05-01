
#include <math.h>
#include "complex.h"
#include "mathtool.h"

void slasq4(int n__, float *q, float *e, float *tau, float *sup)
{
/*  -- LAPACK routine (version 2.0) --   
       Univ. of Tennessee, Univ. of California Berkeley, NAG Ltd.,   
       Courant Institute, Argonne National Lab, and Rice University   
       September 30, 1994   


       Purpose   
       =======   

       SLASQ4 estimates TAU, the smallest eigenvalue of a matrix. This   
       routine improves the input value of SUP which is an upper bound   
       for the smallest eigenvalue for this matrix .   

       Arguments   
       =========   

    N       (input) INTEGER   
            On entry, N specifies the number of rows and columns   
            in the matrix. N must be at least 0.   

    Q       (input) REAL array, dimension (N)   
            Q array   

    E       (input) REAL array, dimension (N)   
            E array   

    TAU     (output) REAL   
            Estimate of the shift   

    SUP     (input/output) REAL   
            Upper bound for the smallest singular value   

    ===================================================================== 
  

    
   Parameter adjustments   
       Function Body */
    /* Table of constant values */
    static float c_b4 = .7f;
    
    /* System generated locals */
    int i__1;
   float r__1, r__2;
    /* Local variables */
    static float xinf, d;
    static int i;
    static float dm;
    static int ifl;



#define E(I) e[(I)-1]
#define Q(I) q[(I)-1]


    ifl = 1;
    /*
     * Computing MIN 
     */
    r__1 = min(*sup,Q(1)), r__1 = min(r__1,Q(2)), r__1 = min(r__1,Q(3)), r__2 
	    = Q(n__), r__1 = min(r__1,r__2), r__2 = Q(n__ - 1), r__1 = min(r__1,
	    r__2), r__2 = Q(n__ - 2);
    *sup = min(r__1,r__2);
    *tau = *sup * .9999f;
    xinf = 0.f;
L10:
    if (ifl == 5) {
	*tau = xinf;
	return ;
    }
    d = Q(1) - *tau;
    dm = d;
    i__1 = n__ - 2;
    for (i = 1; i <= n__-2; ++i) {
	d = d / (d + E(i)) * Q(i + 1) - *tau;
	if (dm > d) {
	    dm = d;
	}
	if (d < 0.f) {
	    *sup = *tau;
            /*
             *  Computing MAX 
             */
	    r__1 = *sup * pow(c_b4, ifl), r__2 = d + *tau;
	    *tau = max(r__1,r__2);
	    ++ifl;
	    goto L10;
	}
    }
    d = d / (d + E(n__ - 1)) * Q(n__) - *tau;
    if (dm > d) {
	dm = d;
    }
    if (d < 0.f) {
	*sup = *tau;
        /*
         * Computing MAX 
         */
	r__1 = xinf, r__2 = d + *tau;
	xinf = max(r__1,r__2);
	if (*sup * pow(c_b4, ifl) <= xinf) {
	    *tau = xinf;
	} else {
	    *tau = *sup * pow(c_b4, ifl);
	    ++ifl;
	    goto L10;
	}
    } else {
        /*
         * Computing MIN 
         */
	r__1 = *sup, r__2 = dm + *tau;
	*sup = min(r__1,r__2);
    }


} 

