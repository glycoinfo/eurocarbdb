
#include <math.h>

/*
 *    phase correction
 *
 *    note:
 *        sin(a)cos(b)+cos(a)sin(b) = sin(a+b)
 *        cos(a)cos(b)-sin(a)sin(b) = cos(a+b)
 *
 */
void phase(float *xr, float *xi, int nbl, float ahold,
           float bhold, int i0)
{
    int j;
    float pi, a, b;

    if (nbl <= 1)
        return;
    pi = acos(-1.0);
    a  = ahold * pi/180.0;
    b  = bhold * pi/(180.0 * (float)(nbl-1));
    i0 -= 1;
    for (j=0;j<nbl;j++) {
        float beta, bsum, bdif, c;

        beta  = b * (float)(j - i0);
        bsum  = sin(a+beta); 
        bdif  = cos(a+beta);
        c     = xr[j] * bdif - xi[j] * bsum;
        xi[j] = xi[j] * bdif + xr[j] * bsum;
        xr[j] = c;
    }
}

