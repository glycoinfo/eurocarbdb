

#include <math.h>
#include "mathtool.h"

/*
c
c     scales a vector by a constant.
c     uses unrolled loops for increment equal to one.
c     jack dongarra, linpack, 3/11/78.
c     modified 3/93 to return if incx .le. 0.
c
*/

void dscal(int n, double da, double *dx, int incx)
{
    int i,m,mp1,nincx=1;


    /*
     * adjust array
     */
    dx--;
    
    if ( n <= 0 || incx <= 0 )
        return;
    if (incx != 1) {
        /*
         *        code for increment not equal to 1
         */
        nincx *= n;
        for (i = 1;i<=nincx;i+=incx)
            dx[i] *= da;

        return;
    }
    /*
     *        code for increment equal to 1
     *
     *        clean-up loop
     */
    m = n % 5;
    if ( m != 0 ) { 
        for (i = 1;i<=m;i++)
            dx[i] *= da;
        if ( n < 5 ) 
            return;
    }
    mp1 = m + 1;
    for (i = mp1;i<=n;i+=5) {
        dx[i]     *= da;
        dx[i + 1] *= da;
        dx[i + 2] *= da;
        dx[i + 3] *= da;
        dx[i + 4] *= da;
    }
}



