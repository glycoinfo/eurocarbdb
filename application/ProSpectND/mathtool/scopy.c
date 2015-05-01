
/*  -- translated by f2c (version 19940927).
   You must link the resulting object file with the libraries:
	-lf2c -lm   (in that order)
*/

#include "complex.h"
#include "mathtool.h"

void scopy(int n__, float *sx, int incx, float *sy, int incy)
{


    /* System generated locals */
    int i__1;

    /* Local variables */
    static int i, m, ix, iy, mp1;


/*     copies a vector, x, to a vector, y.   
       uses unrolled loops for increments equal to 1.   
       jack dongarra, linpack, 3/11/78.   
       modified 12/3/93, array(1) declarations changed to array(*)   


    
   Parameter adjustments   
       Function Body */
#define SY(I) sy[(I)-1]
#define SX(I) sx[(I)-1]


    if (n__ <= 0) {
	return ;
    }
    if (incx == 1 && incy == 1) {
	goto L20;
    }

    /* 
     *    code for unequal increments or equal increments   
     *    not equal to 1 
     */

    ix = 1;
    iy = 1;
    if (incx < 0) {
	ix = (-(n__) + 1) * incx + 1;
    }
    if (incy < 0) {
	iy = (-(n__) + 1) * incy + 1;
    }
    i__1 = n__;
    for (i = 1; i <= n__; ++i) {
	SY(iy) = SX(ix);
	ix += incx;
	iy += incy;
    }
    return ;

    /*
     *  code for both increments equal to 1   
     *  clean-up loop 
     */

L20:
    m = n__ % 7;
    if (m == 0) {
	goto L40;
    }
    i__1 = m;
    for (i = 1; i <= m; ++i) {
	SY(i) = SX(i);
    }
    if (n__ < 7) {
	return ;
    }
L40:
    mp1 = m + 1;
    i__1 = n__;
    for (i = mp1; i <= n__; i += 7) {
	SY(i) = SX(i);
	SY(i + 1) = SX(i + 1);
	SY(i + 2) = SX(i + 2);
	SY(i + 3) = SX(i + 3);
	SY(i + 4) = SX(i + 4);
	SY(i + 5) = SX(i + 5);
	SY(i + 6) = SX(i + 6);
    }

} 

