

#include "complex.h"
#include "mathtool.h"

void csrot(int n__, fcomplex *cx, int incx, fcomplex *
	   cy, int incy, float c__, float s__)
{
    /* System generated locals */
    int i__1, i__2, i__3, i__4;
    fcomplex q__1, q__2, q__3;
    /* Local variables */
    static int i;
    static fcomplex ctemp;
    static int ix, iy;
/*     applies a plane rotation, where the cos and sin (c and s) are real 
  
       and the vectors cx and cy are complex.   
       jack dongarra, linpack, 3/11/78.   
    ===================================================================== 
  
    
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
     *        code for unequal increments or equal increments not equal   
     *        to 1 
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
	q__2.r = c__ * CX(ix).r, q__2.i = c__ * CX(ix).i;
	i__3 = iy;
	q__3.r = s__ * CY(iy).r, q__3.i = s__ * CY(iy).i;
	q__1.r = q__2.r + q__3.r, q__1.i = q__2.i + q__3.i;
	ctemp.r = q__1.r, ctemp.i = q__1.i;
	i__2 = iy;
	i__3 = iy;
	q__2.r = c__ * CY(iy).r, q__2.i = c__ * CY(iy).i;
	i__4 = ix;
	q__3.r = s__ * CX(ix).r, q__3.i = s__ * CX(ix).i;
	q__1.r = q__2.r - q__3.r, q__1.i = q__2.i - q__3.i;
	CY(iy).r = q__1.r, CY(iy).i = q__1.i;
	i__2 = ix;
	CX(ix).r = ctemp.r, CX(ix).i = ctemp.i;
	ix += incx;
	iy += incy;
    }
    return ;
    /*
     *        code for both increments equal to 1 
     */
L20:
    i__1 = n__;
    for (i = 1; i <= n__; ++i) {
	i__2 = i;
	q__2.r = c__ * CX(i).r, q__2.i = c__ * CX(i).i;
	i__3 = i;
	q__3.r = s__ * CY(i).r, q__3.i = s__ * CY(i).i;
	q__1.r = q__2.r + q__3.r, q__1.i = q__2.i + q__3.i;
	ctemp.r = q__1.r, ctemp.i = q__1.i;
	i__2 = i;
	i__3 = i;
	q__2.r = c__ * CY(i).r, q__2.i = c__ * CY(i).i;
	i__4 = i;
	q__3.r = s__ * CX(i).r, q__3.i = s__ * CX(i).i;
	q__1.r = q__2.r - q__3.r, q__1.i = q__2.i - q__3.i;
	CY(i).r = q__1.r, CY(i).i = q__1.i;
	i__2 = i;
	CX(i).r = ctemp.r, CX(i).i = ctemp.i;
    }

} 


