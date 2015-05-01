
#include "complex.h"
#include "mathtool.h"


/*
 *     scales a vector by a constant.
 *     jack dongarra, 3/11/78.
 *     modified 3/93 to return if incx .le. 0.
 */

void zscal(int n, dcomplex za, dcomplex *zx, int incx)
{
    int i,ix;

    /*
     * adjust array
     */
    zx--;
    
    if ( n <= 0 || incx <= 0 )
        return;
    /*
     *        code for increment equal to 1
     */
    if (incx == 1) {
        for (i = 1;i<=n;i++) 
            zx[i] = Cmul_d(za,zx[i]);
        return;
    }
    /*
     *        code for increment not equal to 1
     */
    ix = 1;
    for (i = 1;i<=n;i++) {
        zx[ix] = Cmul_d(za,zx[ix]);
        ix += incx;
    }
}


