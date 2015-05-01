
#include "complex.h"
#include "mathtool.h"

#define cdabs(a)	Cabs_d(a)

/*
 *    csign(zdum1,zdum2) = cdabs(zdum1)*(zdum2/cdabs(zdum2))
 */
dcomplex csign(dcomplex a, dcomplex b)
{
    dcomplex c;
    double da,db;
    da = cdabs(a);
    db = cdabs(b);
    c.r = da*b.r/db;
    c.i = da*b.i/db;
    return c;
}

