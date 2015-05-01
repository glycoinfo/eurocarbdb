/********************************************************************/
/*                                fourier.c                         */
/*                                                                  */
/********************************************************************/
#include <stdio.h>
#include <stdlib.h>
#include <ctype.h>
#include <string.h>
#include <stdarg.h>
#include <math.h>
#include "mathtool.h"
#include "nmrtool.h"

/*
C  nmr_ffts.for
C
C  890103 / rb
C==========================================================================
C
C  fft utilities:
C   fftrxf(xr,xi,n,ntot) .. forward  real    fft
C   fftcxf(xr,xi,n,ntot) .. forward  complex fft
C   fftrxb(xr,xi,n,ntot) .. backward real    fft
C   fftcxb(xr,xi,n,ntot) .. backward complex fft
C
C  basic fft's:
C   fft(fi,ntot,xr,xi)
C
C==========================================================================
C     FFT routines
C
C            FFT(FI,NTOT,XR,XI)     a unscaled complex FFT
C            where:
C            FI    1. = forward   transform
C                 -1. = backward  transform
C            NTOT  array length
C            XR    array containing the real response, overwritten with result
C            XI    array containing the imag response, overwritten with result
C
C
C==========================================================================
C
*/

static void fft(float fi, int ntot, float *xreal, float *ximag)
{
    int n1,n2,n3,n4,n6,n7,n8;
    float pi,t1,t2,*xr,*xi;
    
    /*
     * arrays must start at 1
     */
    xr = xreal - 1;
    xi = ximag - 1;
    
    n1 = 1;
    n2 = ntot;
    pi = acos(-1.0);
    for (n3=2;n3<=n2;n3++) {
        n4 = ntot;
        do {
            n4 /= 2;
        } while ((n1+n4) > n2);
        n1 = n1-((n1-1)/n4)*n4+n4;
        if (n1 <= n3) 
            continue;
        t1     = xr[n3];
        xr[n3] = xr[n1];
        xr[n1] = t1;
        t2     = xi[n3];
        xi[n3] = xi[n1];
        xi[n1] = t2;
    }

    n4=1;
    do {
        n6 = 2*n4;
        for (n3=1;n3<=n4;n3++) {
            float af,cf,sf;
            af = fi*(n3-1)*pi/n4;
            cf = cos(af);
            sf = sin(af);
            for (n7=n3; n7<=ntot; n7 += n6) {
                n8 = n7 + n4;
                t1 = cf * xr[n8] - sf * xi[n8];
                t2 = cf * xi[n8] + sf * xr[n8];
                xr[n8] = xr[n7] - t1;
                xi[n8] = xi[n7] - t2;
                xr[n7] += t1;
                xi[n7] += t2;
            }
        }
        n4 = n6;
    } while (n4 < ntot);
}



/*
 *.....real forward fft
 */
int fftrxf(float *xrr, float *xri, int isize, int ntot, int rotate)
{
    /*
     * .....zerofill if necessary, between isize and ntot
     */
    if (ntot > isize) 
        memset(xrr + isize, 0, sizeof(float) * (ntot - isize));
    /*
     * .....fill imaginary array with zeros
     */
    memset(xri, 0, sizeof(float) * ntot);
    /*
     * Left rotate data, if requested
     */
    if (rotate) 
        xxrotate(xrr, 1, ntot, rotate);
    /*
     * .....normal forward fft
     */
    fft(1.0,ntot,xrr,xri);
    /*
     * .....real fft,    transform x(0....f) to x(0....0.5f)
     */
    return ntot / 2;
}


/*
 *.....imaginary forward fft
 */
int fftixf(float *xrr, float *xri, int isize, int ntot, int rotate)
{
    int i;

    /*
     * .....zerofill if necessary, between isize and ntot
     */
    if (ntot > isize) 
        memset(xri + isize, 0, sizeof(float) * (ntot - isize));
    /*
     * .....fill imaginary array with zeros
     */
    memset(xrr, 0, sizeof(float) * ntot);
    /*
     * Left rotate data, if requested
     */
    if (rotate) 
        xxrotate(xri, 1, ntot, rotate);
    /*
     * .....normal forward fft
     */
    fft(1.0,ntot,xrr,xri);
    /*
     * .....imaginary fft, transform x(0....f) to x(0....0.5f)
     */
    ntot /= 2;
    for (i=0;i<ntot;i++) {
        xrr[i]      = xrr[i+ntot];
        xri[i]      = xri[i+ntot];
    }
    return ntot;
}

/*
 *.....complex forward fft
 */
int fftcxf(float *xrr, float *xri, int isize, int ntot, int rotate)
{
    int i, idum;

    /*
     * .....zerofill if necessary, between isize and ntot
     */
    if (ntot > isize) {
        memset(xrr + isize, 0, sizeof(float) * (ntot - isize));
        memset(xri + isize, 0, sizeof(float) * (ntot - isize));
    }
    /*
     * Left rotate data, if requested
     */
    if (rotate) {
        xxrotate(xrr, 1, ntot, rotate);
        xxrotate(xri, 1, ntot, rotate);
    }
    /*
     * .....normal forward fft
     */
    fft(1.0,ntot,xrr,xri);
    /*
     * .....complex fft, transform x(0....f) to x(-0.5f....0.5f)
     */
    idum = ntot/2;
    for (i=0;i<idum;i++) {
        float xdum;

        xdum        = xrr[i];
        xrr[i]      = xrr[i+idum];
        xrr[i+idum] = xdum;
        xdum        = xri[i];
        xri[i]      = xri[i+idum];
        xri[i+idum] = xdum;
    }
    return ntot;
}


/*
 * .....real backward fft
 */
int fftrxb(float *xrr, float *xri, int isize, int ntot)
{
    int i, ntotp1;
    float scale;

    /*
     * .....zerofill if necessary, between isize and ntot
     */
    if (ntot > isize) 
        memset(xrr + isize, 0, sizeof(float) * (ntot - isize));

    /*
     * .....fill imaginary array with zeros
     */
    memset(xri, 0, sizeof(float) * ntot * 2);
    /*
     * .....real    fft, transform x(0...0.5f) to x(0....f)
     */
    ntotp1 = 2 * ntot;
    xrr[ntot]=xrr[ntot-1];
    for (i=1;i<ntot;i++) {
         xrr[ntotp1-i] = xrr[i];
/*         xri[ntotp1-i] = -xri[i];*/
    }
    /*
     * .....reverse fft
     */
    fft(-1.0, ntot*2, xrr, xri);

    scale = 1.0/ntot;
    for (i=0;i<isize;i++) {
        xrr[i] *= scale;
        xri[i] *= scale;
    }
    return ntot;
}

/*
 * ....imag  backward fft
 */
int fftixb(float *xrr, float *xri, int isize, int ntot)
{
    int i, ntotp1;
    float scale;

    /*
     * .....zerofill if necessary, between isize and ntot
     */
    if (ntot > isize) 
        memset(xri + isize, 0, sizeof(float) * (ntot - isize));
    /*
     * .....fill imaginary array with zeros
     */
    memset(xrr, 0, sizeof(float) * ntot * 2);
    /*
     * .....imag    fft, transform x(0...0.5f) to x(0....f)
     */
    ntotp1 = 2 * ntot - 1;
    for (i=0;i<ntot;i++) {
         xrr[ntotp1-i] =  xrr[i];
         xri[ntotp1-i] = -xri[i];
    }
    /*
     * .....reverse fft
     */
    fft(-1.0, ntot*2, xrr, xri);
    for (i=0;i<ntot;i++) {
         xrr[i] = xrr[i+ntot];
         xri[i] = xri[i+ntot];
    }
    scale = 1.0/ntot;
    for (i=0;i<ntot;i++) {
        xrr[i] *= scale;
        xri[i] *= scale;
    }
    return ntot;
}



/*
 *.....complex backward fft
 */
int fftcxb(float *xrr, float *xri, int isize, int ntot)
{
    int i, idum;
    float scale;

    /*
     * .....zerofill if necessary, between isize and ntot
     */
    if (ntot > isize) {
        memset(xrr + isize, 0, sizeof(float) * (ntot - isize));
        memset(xri + isize, 0, sizeof(float) * (ntot - isize));
    }

    /*
     * .....complex fft, transform x(-0.5f....0.5f) to x(0....f)
     */
    idum = ntot/2;
    for (i=0;i<idum;i++) {
        float xdum;

        xdum        = xrr[i];
        xrr[i]      = xrr[i+idum];
        xrr[i+idum] = xdum;
        xdum        = xri[i];
        xri[i]      = xri[i+idum];
        xri[i+idum] = xdum;
    }
    /*
     * .....reverse fft
     */
    fft(-1.0, ntot, xrr, xri);
    scale = 1.0/ntot;
    for (i=0;i<ntot;i++) {
        xrr[i] *= scale;
        xri[i] *= scale;
    }
    return ntot;
}


