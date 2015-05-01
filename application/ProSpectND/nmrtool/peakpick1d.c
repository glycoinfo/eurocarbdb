/********************************************************************/
/*                           peakpick1d.c                           */
/*                                                                  */
/********************************************************************/
#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include "mathtool.h"
#include "nmrtool.h"

/*
 * Rotten fit, so keep n low (=3)
 * must find something better
 */
static int parfit(float *y, int ip, int *n)
{
    int    i, imax, n2,i1,i2;
    float  b, c, sumx2, sumx4, sumy, sumxy, sumx2y;

    /*
     * y already starts at 1
     */
    n2  = (*n)/2;
    *n  = 2 * n2 + 1;
    i1  = ip - n2;
    i2  = ip + n2;

    sumx2  = 0.0;
    sumx4  = 0.0;
    sumy   = 0.0;
    sumxy  = 0.0;
    sumx2y = 0.0;

    for (i=i1;i<=i2;i++) {
        int ii, ix;
        float yi;
        
        yi = y[i];
        ix = i - ip;
        ii = ix * ix;
        sumx2  += ii;
        sumx4  += ii * ii;
        sumy   += yi;
        sumxy  += ix * yi;
        sumx2y += ii * yi;
    }
    b = sumxy * ((*n) * sumx4  - sumx2 * sumx2);
    c = sumx2 * ((*n) * sumx2y - sumx2 * sumy);
    imax = ip - 0.5 * b/c + 0.5;
    if (c > 0) 
        imax = -imax;
    return imax;
}

/*
 * 1D peakpick routine
 *
 * data 	= input array with peaks
 * i1   	= start picking
 * i2   	= stop picking
 * height 	= only peaks higher than height
 * pmax 	= only peaks lower than pmax
 * mp 		= max number of peaks
 * ipeaks 	= output array with peaks
 * return	: number of peaks found
 */
int npeaks1d(float *data, int i1, int i2, float height, 
             float pmax, int mp, int *ipeaks)
{
    int ip, lup, i, np, npoint = 3;
    float prev;

    /*
     * Array starts at 1
     */
    data--;

    np  = 0;
    ip  = i1;
    lup = FALSE;
    for (;;) {
        int ii, imax, ok = TRUE;

        prev = data[ip];
        ii = ip + 1;
        for (i=ii;i<=i2;i++) {
            float d = data[i];
            if (d < prev && prev > height && 
                    (prev < pmax || pmax == 0.0) && lup) {
                ok = FALSE;
                break;
            }
            if (d > prev) 
                lup = TRUE;
            prev = d;
            ip = i;
        }
        if (ok)
            return np;

        imax = parfit(data, ip, &npoint);
        lup = FALSE;
        if (imax < 0) 
            continue;
        /*
         * ipeaks starts at 0
         */
        ipeaks[np++] = imax;
        if (np >= mp) 
            break;
    }
    return np;
}

