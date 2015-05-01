
#include <math.h>
#include "mathtool.h"

/*
c
c     (yr,yi) = complex sqrt(xr,xi) 
c     branch chosen so that yr >= 0.0 and sign(yi) == sign(xi)
c
*/

void csroot(double xr, double xi, double *yr, double *yi)
{
    double s,tr,ti;

    tr = xr;
    ti = xi;
    s = sqrt(0.5*(pythag(tr,ti) + fabs(tr)));
    if (tr >= 0.0) 
        *yr = s;
    if (ti <  0.0) 
        s = -s;
    if (tr <= 0.0) 
        *yi = s;
    if (tr <  0.0) 
        *yr = 0.5*(ti/(*yi));
    if (tr >  0.0) 
        *yi = 0.5*(ti/(*yr));
}
