
#include <math.h>
#include "mathtool.h"

/*
 *     construct givens plane rotation.
 *     jack dongarra, linpack, 3/11/78.
 *                   modified 9/27/86.
 */


void drotg(double *da, double *db, double *c, double *s)
{
    double roe,scale,r,z;

    roe = *db;
    if (fabs(*da) > fabs(*db)) 
        roe = *da;
    scale = fabs(*da) + fabs(*db);
    if (scale == 0.0) {
        *c = 1.0;
        *s = 0.0;
        r = 0.0;
    }
    else {
        r = scale * sqrt(pow(*da/scale,2) + pow(*db/scale,2));
        r = sign(1.0, roe) * r;
        *c = *da/r;
        *s = *db/r;
    }
    z = *s;
    if (fabs(*c) > 0.0 && fabs(*c) <= *s) 
        z = 1.0/(*c);
    *da = r;
    *db = z;
}

