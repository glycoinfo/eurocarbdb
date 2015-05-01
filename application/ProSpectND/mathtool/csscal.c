
/*  -- translated by f2c (version 19940927).
   You must link the resulting object file with the libraries:
	-lf2c -lm   (in that order)
*/

#include "complex.h"
#include "mathtool.h"


void csscal(int n__, float sa_, fcomplex *cx, int incx)
{


    /* System generated locals */
    int i__1, i__3, i__4;
    double d__1, d__2;
    fcomplex q__1;

    /* Local variables */
    static int i, nincx;


/*     scales a complex vector by a real constant.   
       jack dongarra, linpack, 3/11/78.   
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
    for (i = 1; incx < 0 ? i >= nincx : i <= nincx; i += incx) {
	i__3 = i;
	i__4 = i;
	d__1 = sa_ * CX(i).r;
	d__2 = sa_ * CX(i).i;
	q__1.r = d__1, q__1.i = d__2;
	CX(i).r = q__1.r, CX(i).i = q__1.i;
    }
    return ;

    /*
     *        code for increment equal to 1 
     */

L20:
    for (i = 1; i <= n__; ++i) {
	i__1 = i;
	i__3 = i;
	d__1 = sa_ * CX(i).r;
	d__2 = sa_ * CX(i).i;
	q__1.r = d__1, q__1.i = d__2;
	CX(i).r = q__1.r, CX(i).i = q__1.i;
    }

} 

