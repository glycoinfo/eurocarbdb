
#include "complex.h"
#include "mathtool.h"

/*
 *     constant times a vector plus a vector.
 *     jack dongarra, 3/11/78.
 */

void zaxpy(int n, dcomplex  za, dcomplex  *zx, int incx, 
                                       dcomplex *zy, int incy)
{
    int i, ix, iy;
    
    /*
     * adjust arrays
     */
    zx--;
    zy--;

    if (n <= 0)
        return;
    if (Cabs_d(za) == 0.0) 
        return;
    /*
     *        code for both increments equal to 1
     */
    if (incx == 1 && incy == 1) {
        for (i=1;i<=n;i++) 
            zy[i] = Cadd_d(zy[i], Cmul_d(za, zx[i]));
        return;
    }
    /*
     *        code for unequal increments or equal increments
     *          not equal to 1
     */
    ix = 1;
    iy = 1;
    if (incx < 0)
        ix = (-n+1)*incx + 1;
    if (incy < 0)
        iy = (-n+1)*incy + 1;
    for (i = 1;i<=n;i++) {
        zy[iy] = Cadd_d(zy[iy], Cmul_d(za, zx[ix]));
        ix += incx;
        iy += incy;
    }
}

