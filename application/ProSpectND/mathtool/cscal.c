
/*  -- translated by f2c (version 19940927).
   You must link the resulting object file with the libraries:
	-lf2c -lm   (in that order)
*/



#include "complex.h"
#include "mathtool.h"


void cscal(int n__, fcomplex ca_, fcomplex *cx, int incx)
{


    /* System generated locals */
    int i__1, i__2, i__3, i__4;
    fcomplex q__1;

    /* Local variables */
    static int i, nincx;


/*     scales a vector by a constant.   
       jack dongarra, linpack,  3/11/78.   
       modified 3/93 to return if incx .le. 0.   
       modified 12/3/93, array(1) declarations changed to array(*)   


    
   Parameter adjustments   
       Function Body */
#define CX(I) cx[(I)-1]


    if (n__ <= 0 || incx <= 0) {
	return ;
    }
    if (incx == 1) {
	goto L20;
    }

    /*
     *        code for increment not equal to 1 
     */

    nincx = n__ * incx;
    i__1 = nincx;
    i__2 = incx;
    for (i = 1; incx < 0 ? i >= nincx : i <= nincx; i += incx) {
	i__3 = i;
	i__4 = i;
	q__1.r = ca_.r * CX(i).r - ca_.i * CX(i).i, q__1.i = ca_.r * CX(
		i).i + ca_.i * CX(i).r;
	CX(i).r = q__1.r, CX(i).i = q__1.i;
    }
    return ;

    /*
     *        code for increment equal to 1 
     */

L20:
    i__2 = n__;
    for (i = 1; i <= n__; ++i) {
	i__1 = i;
	i__3 = i;
	q__1.r = ca_.r * CX(i).r - ca_.i * CX(i).i, q__1.i = ca_.r * CX(
		i).i + ca_.i * CX(i).r;
	CX(i).r = q__1.r, CX(i).i = q__1.i;
    }

} 

