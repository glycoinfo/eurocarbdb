/*  -- translated by f2c (version 19940927).
   You must link the resulting object file with the libraries:
	-lf2c -lm   (in that order)
*/

#include "complex.h"
#include "mathtool.h"

void cdotu(fcomplex *ret_val, int n__, fcomplex *cx, int 
	   incx, fcomplex *cy, int incy)
{
    /* System generated locals */
    int i__1, i__2, i__3;
    fcomplex q__1, q__2;

    /* Local variables */
    static int i;
    static fcomplex ctemp;
    static int ix, iy;


/*     forms the dot product of two vectors.   
       jack dongarra, linpack, 3/11/78.   
       modified 12/3/93, array(1) declarations changed to array(*)   


    
   Parameter adjustments */
    --cy;
    --cx;

    /* Function Body */
    ctemp.r = 0.f, ctemp.i = 0.f;
     ret_val->r = 0.f,  ret_val->i = 0.f;
    if (n__ <= 0) {
	return ;
    }
    if (incx == 1 && incy == 1) {
	goto L20;
    }

/*        code for unequal increments or equal increments   
            not equal to 1 */

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
	i__3 = iy;
	q__2.r = cx[ix].r * cy[iy].r - cx[ix].i * cy[iy].i, q__2.i = 
		cx[ix].r * cy[iy].i + cx[ix].i * cy[iy].r;
	q__1.r = ctemp.r + q__2.r, q__1.i = ctemp.i + q__2.i;
	ctemp.r = q__1.r, ctemp.i = q__1.i;
	ix += incx;
	iy += incy;
    }
     ret_val->r = ctemp.r,  ret_val->i = ctemp.i;
    return ;

/*        code for both increments equal to 1 */

L20:
    i__1 = n__;
    for (i = 1; i <= n__; ++i) {
	i__2 = i;
	i__3 = i;
	q__2.r = cx[i].r * cy[i].r - cx[i].i * cy[i].i, q__2.i = 
		cx[i].r * cy[i].i + cx[i].i * cy[i].r;
	q__1.r = ctemp.r + q__2.r, q__1.i = ctemp.i + q__2.i;
	ctemp.r = q__1.r, ctemp.i = q__1.i;
    }
     ret_val->r = ctemp.r,  ret_val->i = ctemp.i;

} 

