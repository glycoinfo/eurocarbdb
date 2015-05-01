
/*  -- translated by f2c (version 19940927).
   You must link the resulting object file with the libraries:
	-lf2c -lm   (in that order)
*/

#include "complex.h"
#include "mathtool.h"

void sscal(int n__, float sa_, float *sx, int incx)
{


    /* System generated locals */
    int i__1, i__2;

    /* Local variables */
    static int i, m, nincx, mp1;


/*     scales a vector by a constant.   
       uses unrolled loops for increment equal to 1.   
       jack dongarra, linpack, 3/11/78.   
       modified 3/93 to return if incx .le. 0.   
       modified 12/3/93, array(1) declarations changed to array(*)   


    
   Parameter adjustments   
       Function Body */
#define SX(I) sx[(I)-1]


    if (n__ <= 0 || incx <= 0) {
	return ;
    }
    if (incx == 1) {
	goto L20;
    }

/*        code for increment not equal to 1 */

    nincx = n__ * incx;
    i__1 = nincx;
    i__2 = incx;
    for (i = 1; incx < 0 ? i >= nincx : i <= nincx; i += incx) {
	SX(i) = sa_ * SX(i);
    }
    return ;

/*        code for increment equal to 1   


          clean-up loop */

L20:
    m = n__ % 5;
    if (m == 0) {
	goto L40;
    }
    i__2 = m;
    for (i = 1; i <= m; ++i) {
	SX(i) = sa_ * SX(i);
    }
    if (n__ < 5) {
	return ;
    }
L40:
    mp1 = m + 1;
    i__2 = n__;
    for (i = mp1; i <= n__; i += 5) {
	SX(i) = sa_ * SX(i);
	SX(i + 1) = sa_ * SX(i + 1);
	SX(i + 2) = sa_ * SX(i + 2);
	SX(i + 3) = sa_ * SX(i + 3);
	SX(i + 4) = sa_ * SX(i + 4);
    }

} 

