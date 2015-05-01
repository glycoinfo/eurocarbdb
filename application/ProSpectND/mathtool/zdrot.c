
#include "complex.h"
#include "mathtool.h"


/*
 *     applies a plane rotation, where the cos and sin (c and s) are
 *     double precision and the vectors zx and zy are double complex.
 *     jack dongarra, linpack, 3/11/78.
 */


void zdrot(int n, dcomplex *zx, int incx, dcomplex *zy, int incy,
           double c, double s)
{
    dcomplex ztemp;
    int i,ix,iy;

    if (n <= 0)
        return;
    /*
     *        code for both increments equal to 1
     */
    if (incx == 1 && incy == 1) {
        for (i=0;i<n;i++) {
            ztemp = Cadd_d(DCmul_d(c, zx[i]), DCmul_d(s, zy[i]));
            zy[i] = Csub_d(DCmul_d(c, zy[i]), DCmul_d(s, zx[i]));
            zx[i] = ztemp;
        }
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
        ztemp  = Cadd_d(DCmul_d(c, zx[ix]), DCmul_d(s, zy[iy]));
        zy[iy] = Csub_d(DCmul_d(c, zy[iy]), DCmul_d(s, zx[ix]));
        zx[ix] = ztemp;
        ix += incx;
        iy += incy;
    }
}


