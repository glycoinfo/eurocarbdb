
#include "complex.h"
#include "mathtool.h"

/*
 *     interchanges two vectors.
 *     jack dongarra, 3/11/78.
 *
 */

void  zswap(int n, dcomplex *zx, int incx, dcomplex *zy, int incy)
{
    dcomplex ztemp;
    int i,ix,iy;
    
    /*
     * adjust arrays
     */
    zx--;
    zy--;
    
    if (n <= 0)
        return;
    /*
     *        code for both increments equal to 1
     */
    if (incx == 1 && incy == 1) {
        for (i=1;i<=n;i++) {
            ztemp = zx[i];
            zx[i] = zy[i];
            zy[i] = ztemp;
        }
        return;
    }
    /*
     *    code for unequal increments or equal increments
     *    not equal to 1
     */
    ix = 1;
    iy = 1;
    if (incx < 0) 
        ix = (-n+1) * incx + 1;
    if (incy < 0) 
        iy = (-n+1) * incy + 1;
    for (i=1;i<=n;i++) {
        ztemp  = zx[ix];
        zx[ix] = zy[iy];
        zy[iy] = ztemp;
        ix += incx;
        iy += incy;
    }
}


