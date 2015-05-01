
#include <math.h>
#include "complex.h"
#include "mathtool.h"

dcomplex cdlog(dcomplex z)
{
    return Complex_d(log(Cabs_d(z)), atan2(z.i, z.r));
}
