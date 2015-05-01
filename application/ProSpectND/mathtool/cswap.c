
/*  -- translated by f2c (version 19940927).
   You must link the resulting object file with the libraries:
	-lf2c -lm   (in that order)
*/

#include "complex.h"
#include "mathtool.h"

void cswap(int n__, fcomplex *cx, int incx, fcomplex *cy, int incy)
{


    /* System generated locals */
    int i__1, i__2, i__3;

    /* Local variables */
    static int i;
    static fcomplex ctemp;
    static int ix, iy;


/*     interchanges two vectors.   
       jack dongarra, linpack, 3/11/78.   
       modified 12/3/93, array(1) declarations changed to array(*)   


    
   Parameter adjustments   
       Function Body */
#define CY(I) cy[(I)-1]
#define CX(I) cx[(I)-1]


    if (n__ <= 0) {
	return ;
    }
    if (incx == 1 && incy == 1) {
	goto L20;
    }

    /*
     *       code for unequal increments or equal increments not equal   
     *       to 1 
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
	i__2 = ix;
	ctemp.r = CX(ix).r, ctemp.i = CX(ix).i;
	i__2 = ix;
	i__3 = iy;
	CX(ix).r = CY(iy).r, CX(ix).i = CY(iy).i;
	i__2 = iy;
	CY(iy).r = ctemp.r, CY(iy).i = ctemp.i;
	ix += incx;
	iy += incy;
    }
    return ;

    /*
     *       code for both increments equal to 1 
     */
L20:
    i__1 = n__;
    for (i = 1; i <= n__; ++i) {
	i__2 = i;
	ctemp.r = CX(i).r, ctemp.i = CX(i).i;
	i__2 = i;
	i__3 = i;
	CX(i).r = CY(i).r, CX(i).i = CY(i).i;
	i__2 = i;
	CY(i).r = ctemp.r, CY(i).i = ctemp.i;
    }

} 

