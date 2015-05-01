
#include "complex.h"
#include "mathtool.h"

/*
 *     forms the dot product of a vector.
 *     jack dongarra, 3/11/78.
 */


dcomplex zdotc(int n, dcomplex *zx, int incx, dcomplex *zy, int incy)
{
    dcomplex ztemp;
    int i,ix,iy;

    ztemp = Complex_d(0.0, 0.0);

    if (n <= 0)
        return ztemp;
    /*
     *        code for both increments equal to 1
     */
    if (incx == 1 && incy == 1) {
        for (i=0;i<n;i++) 
            ztemp = Cadd_d(ztemp, Cmul_d(Conjg_d(zx[i]), zy[i]));

        return ztemp;
    }


    /*
     *        code for unequal increments or equal increments
     *          not equal to 1
     */
    ix = 0;
    iy = 0;
    if (incx < 0)
        ix = (-n+1)*incx;
    if (incy < 0)
        iy = (-n+1)*incy;
    for (i = 0;i<n;i++) {
        ztemp = Cadd_d(ztemp, Cmul_d(Conjg_d(zx[ix]), zy[iy]));
        ix += incx;
        iy += incy;
    }
    return ztemp;
}


