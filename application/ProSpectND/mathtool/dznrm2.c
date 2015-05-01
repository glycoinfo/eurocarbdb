
#include <math.h>
#include "complex.h"
#include "mathtool.h"

/*
c
c     unitary norm of the complex n-vector stored in zx() with storage
c     increment incx .
c     if    n .le. 0 return with result = 0.
c     if n .ge. 1 then incx must be .ge. 1
c
c           c.l.lawson , 1978 jan 08
c     modified 3/93 to return if incx .le. 0.
c
c     four phase method     using two built-in constants that are
c     hopefully applicable to all machines.
c         cutlo = maximum of  sqrt(u/eps)  over all known machines.
c         cuthi = minimum of  sqrt(v)      over all known machines.
c     where
c         eps = smallest no. such that eps + 1. .gt. 1.
c         u   = smallest positive no.   (underflow limit)
c         v   = largest  no.            (overflow  limit)
c
c     brief outline of algorithm..
c
c     phase 1    scans zero components.
c     move to phase 2 when a component is nonzero and .le. cutlo
c     move to phase 3 when a component is .gt. cutlo
c     move to phase 4 when a component is .ge. cuthi/m
c     where m = n for x() real and m = 2*n for complex.
c
c     values for cutlo and cuthi..
c     from the environmental parameters listed in the imsl converter
c     document the limiting values are as follows..
c     cutlo, s.p.   u/eps = 2**(-102) for  honeywell.  close seconds are
c                   univac and dec at 2**(-103)
c                   thus cutlo = 2**(-51) = 4.44089e-16
c     cuthi, s.p.   v = 2**127 for univac, honeywell, and dec.
c                   thus cuthi = 2**(63.5) = 1.30438e19
c     cutlo, d.p.   u/eps = 2**(-67) for honeywell and dec.
c                   thus cutlo = 2**(-33.5) = 8.23181d-11
c     cuthi, d.p.   same as s.p.  cuthi = 1.30438d19
c     data cutlo, cuthi / 8.232d-11,  1.304d19 /
c     data cutlo, cuthi / 4.441e-16,  1.304e19 /
*/

double dznrm2(int n, dcomplex *zx, int incx)
{
    int imag, scale;
    int i, ix, next;
    double cutlo, cuthi, hitest, sum, xmax, absx, zero, one;
    double result;
    
    zero  = 0.0;
    one   = 1.0;
    cutlo = 8.232e-11;
    cuthi = 1.304e19;

    if (n < 0 || incx < 0)
        return zero;

    next = 30;
    sum = zero;
    i = 0;
    /*
     * begin main loop
     */
    for (ix = 0;ix < n; ix++) {
        absx = fabs(zx[i].r);
        imag = FALSE;

        do  {
        switch (next) {

        case 30:
            if ( absx > cutlo) goto L85;
            next = 50;
            scale = FALSE;
            /*
             *   phase 1.  sum is zero
             */
        case 50:
            if (absx == zero) goto L200;
            if (absx > cutlo) goto L85;
            /*
             *   prepare for phase 2.
             */
            next = 70;
            goto L105;
            /*
             *    prepare for phase 4.
             */
  L100:
            next = 110;
            sum = (sum / absx) / absx;
  L105:
            scale = TRUE;
            xmax = absx;
            goto L115;
            /*
             *    phase 2.  sum is small.
             *    scale to avoid destructive underflow.
             */
        case 70:
            if ( absx > cutlo ) goto L75;
            /*
             *    common code for phases 2 and 4.
             *    in phase 4 sum is large.  scale to avoid overflow.
             */
        case 110:
            if ( absx <= xmax ) goto L115;
            sum = one + sum * pow(xmax / absx, 2);
            xmax = absx;
            goto L200;
  L115:
            sum += pow(absx/xmax, 2);
            goto L200;
            /*
             *
             *   prepare for phase 3.
             */
  L75:
            sum = (sum * xmax) * xmax;
  L85:
            next  = 90;
            scale = FALSE;
            /*
             *     for real or d.p. set hitest = cuthi/n
             *     for complex      set hitest = cuthi/(2*n)
             */
            hitest = cuthi/(double)( 2*n );
            /*
             *   phase 3.  sum is mid-range.  no scaling.
             */
        case 90:
            if (absx >= hitest) goto L100;
            sum += pow(absx, 2);
  L200:
            /*
             *   control selection of real and imaginary parts.
             */
            if (imag) goto L210;
            absx = fabs(zx[i].i);
            imag = TRUE;
        }
        }while (next != 30);
  L210:
        i += incx;
    }
    /*
     *     end of main loop.
     *     compute square root and adjust for scaling.
     */
    result = sqrt(sum);
    if (scale) 
        result *= xmax;
    return result;
}













