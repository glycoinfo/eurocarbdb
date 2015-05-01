
#include "complex.h"
#include "mathtool.h"

/*
 *
 *     copies a vector, x, to a vector, y.
 *     jack dongarra, linpack, 4/11/78.
 *
 */
void zcopy(int n, dcomplex *zx, int incx, dcomplex *zy, int incy)
{
    int i,ix,iy;

    if (n <= 0)
        return;
    /*
     *        code for both increments equal to 1
     */
    if (incx == 1 && incy == 1) {
        for (i=0;i<n;i++) 
            zy[i] = zx[i];
        return;
    }
    /*
     *    code for unequal increments or equal increments
     *    not equal to 1
     */
    ix = 0;
    iy = 0;
    if (incx < 0) 
        ix = (-n+1) * incx;
    if (incy < 0) 
        iy = (-n+1) * incy;
    for (i=0;i<n;i++) {
        zy[iy] = zx[ix];
        ix += incx;
        iy += incy;
    }
}

