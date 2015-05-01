

#include <math.h>
#include "complex.h"
#include "mathtool.h"


int icamax(int n__, fcomplex *cx, int incx)
{
    /* System generated locals */
    int ret_val, i__1, i__2;
    float r__1, r__2;
    /* Local variables */
    static float smax;
    static int i, ix;
/*     finds the index of element having max. absolute value.   
       jack dongarra, linpack, 3/11/78.   
       modified 3/93 to return if incx .le. 0.   
       modified 12/3/93, array(1) declarations changed to array(*)   
    
   Parameter adjustments   
       Function Body */
#define CX(I) cx[(I)-1]
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
    smax = (r__1 = CX(1).r, fabs(r__1)) + (r__2 = CX(1).i, fabs(r__2));
    ix += incx;
    i__1 = n__;
    for (i = 2; i <= n__; ++i) {
	i__2 = ix;
	if ((r__1 = CX(ix).r, fabs(r__1)) + (r__2 = CX(ix).i, fabs(
		r__2)) <= smax) {
	    goto L5;
	}
	ret_val = i;
	i__2 = ix;
	smax = (r__1 = CX(ix).r, fabs(r__1)) + (r__2 = CX(ix).i, 
		fabs(r__2));
L5:
	ix += incx;
/* L10: */
    }
    return ret_val;
/*        code for increment equal to 1 */
L20:
    smax = (r__1 = CX(1).r, fabs(r__1)) + (r__2 = CX(1).i, fabs(r__2));
    i__1 = n__;
    for (i = 2; i <= n__; ++i) {
	i__2 = i;
	if ((r__1 = CX(i).r, fabs(r__1)) + (r__2 = CX(i).i, fabs(
		r__2)) <= smax) {
	    goto L30;
	}
	ret_val = i;
	i__2 = i;
	smax = (r__1 = CX(i).r, fabs(r__1)) + (r__2 = CX(i).i, fabs(
		r__2));
L30:
	;
    }
    return ret_val;
} /* icamax_ */


