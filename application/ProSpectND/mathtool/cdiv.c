

#include <math.h>
#include "mathtool.h"

/*
c
c     complex division, (cr,ci) = (ar,ai)/(br,bi)
c
*/

void cdiv(double ar,double ai,double br,double bi,double *cr,double *ci)
{
    double s,ars,ais,brs,bis;

    s = fabs(br) + fabs(bi);
    ars = ar/s;
    ais = ai/s;
    brs = br/s;
    bis = bi/s;
    s = brs*brs + bis*bis;
    *cr = (ars*brs + ais*bis)/s;
    *ci = (ais*brs - ars*bis)/s;
}

