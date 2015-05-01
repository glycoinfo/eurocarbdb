
/*  -- translated by f2c (version 19940927).
   You must link the resulting object file with the libraries:
	-lf2c -lm   (in that order)
*/

#include <math.h>
#include "complex.h"
#include "mathtool.h"


double scasum(int n__, fcomplex *cx, int incx)
{


    /* System generated locals */
    int i__1, i__2, i__3;
    float ret_val, r__1, r__2;

    /* Local variables */
    static int i, nincx;
    static float stemp;


/*     takes the sum of the absolute values of a complex vector and   
       returns a single precision result.   
       jack dongarra, linpack, 3/11/78.   
       modified 3/93 to return if incx .le. 0.   
       modified 12/3/93, array(1) declarations changed to array(*)   


    
   Parameter adjustments   
       Function Body */
#define CX(I) cx[(I)-1]


    ret_val = 0.f;
    stemp = 0.f;
    if (n__ <= 0 || incx <= 0) {
	return ret_val;
    }
    if (incx == 1) {
	goto L20;
    }

/*        code for increment not equal to 1 */

    nincx = n__ * incx;
    i__1 = nincx;
    i__2 = incx;
    for (i = 1; incx < 0 ? i >= nincx : i <= nincx; i += incx) {
	i__3 = i;
	stemp = stemp + (r__1 = CX(i).r, fabs(r__1)) + (r__2 = CX(i).i, 
                fabs(r__2));
    }
    ret_val = stemp;
    return ret_val;

/*        code for increment equal to 1 */

L20:
    i__2 = n__;
    for (i = 1; i <= n__; ++i) {
	i__1 = i;
	stemp = stemp + (r__1 = CX(i).r, fabs(r__1)) + (r__2 = CX(i).i, 
                fabs(r__2));
    }
    ret_val = stemp;
    return ret_val;
} 

