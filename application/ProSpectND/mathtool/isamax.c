
/*  -- translated by f2c (version 19940927).
   You must link the resulting object file with the libraries:
	-lf2c -lm   (in that order)
*/


#include <math.h>
#include "complex.h"
#include "mathtool.h"


int isamax(int n__, float *sx, int incx)
{


    /* System generated locals */
    int ret_val, i__1;
    float r__1;

    /* Local variables */
    static float smax;
    static int i, ix;


/*     finds the index of element having max. absolute value.   
       jack dongarra, linpack, 3/11/78.   
       modified 3/93 to return if incx .le. 0.   
       modified 12/3/93, array(1) declarations changed to array(*)   


    
   Parameter adjustments   
       Function Body */
#define SX(I) sx[(I)-1]


    ret_val = 0;
    if (n__ < 1 || incx <= 0) {
	return ret_val;
    }
    ret_val = 1;
    if (n__ == 1) {
	return ret_val;
    }
    if (incx == 1) {
	goto L20;
    }

/*        code for increment not equal to 1 */

    ix = 1;
    smax = fabs(SX(1));
    ix += incx;
    i__1 = n__;
    for (i = 2; i <= n__; ++i) {
	if ((r__1 = SX(ix), fabs(r__1)) <= smax) {
	    goto L5;
	}
	ret_val = i;
	smax = (r__1 = SX(ix), fabs(r__1));
L5:
	ix += incx;
    }
    return ret_val;

/*        code for increment equal to 1 */

L20:
    smax = fabs(SX(1));
    i__1 = n__;
    for (i = 2; i <= n__; ++i) {
	if ((r__1 = SX(i), fabs(r__1)) <= smax) {
	    goto L30;
	}
	ret_val = i;
	smax = (r__1 = SX(i), fabs(r__1));
L30:
	;
    }
    return ret_val;
} 

