

#include <math.h>
#include "complex.h"
#include "mathtool.h"

dcomplex cdexp(dcomplex z)
{
    double expx;
    dcomplex c;

    expx = exp(z.r);
    c.r = expx * cos(z.i);
    c.i = expx * sin(z.i);
    return c;
}



