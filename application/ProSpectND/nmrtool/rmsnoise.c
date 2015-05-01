/********************************************************************/
/*                           rmsnoise.c                             */
/*                                                                  */
/********************************************************************/
#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include "mathtool.h"
#include "nmrtool.h"

static int okarea2(int i1, int i2, int j1, int j2)
{
     return ((i2 >= i1) && (j2 >= j1) && (i1 > 0) && (j1 > 0));
}


/*
 *       sqrt ( sum((x-avg)**2) / (n-1))
 */
float rmsnoi(float *x, int n)
{
    int i;
    float avg, rms;

    rms = 0.0;
    if (n <= 1) 
        return rms;
    avg = 0.0;
    for (i=0;i<n;i++)
        avg += x[i];
    avg /= n;
    for (i=0;i<n;i++)
        rms += pow(x[i]-avg,2);
    return sqrt(rms / (n-1));
}

/*
 *...................io2d summation routine................................
 */
static float suma2d(int idev, float *x, int ilowb, int ihighb, int ilowa, 
            int ihigha, int (*read2d)(void*,int,float*,int,int),
            void *userdata)
{
    int i,j;
    float sum = 0.0;

    if (!okarea2 (ilowb,ihighb,ilowa,ihigha))
        return sum;

    /*
     *          sum area
     */
    for (i=ilowb;i<=ihighb;i++) {
        read2d(userdata,
               idev,
               x,
               ihigha,
               i);
        for (j=ilowa;j<=ihigha;j++)
            sum += x[j-1];
    }
    return sum;
}

/*
 *...................io2d deviation routine................................
 */
static float chsq2d(int idev, float *x, int ilowb, int ihighb, int ilowa, 
            int ihigha, float avg, int (*read2d)(void*,int,float*,int,int),
            void *userdata)
{
    int i,j;
    float svd = 0.0;

    if (!okarea2 (ilowb,ihighb,ilowa,ihigha))
        return svd;
    /*
     *          chi square
     */
    for (i=ilowb;i<=ihighb;i++) {
        read2d(userdata,
               idev,
               x,
               ihigha,
               i);
        for (j=ilowa;j<=ihigha;j++)
            svd += pow(x[j-1]-avg,2);
    }
    return svd;
}

/*
 * ...................rms noise routine................................
 */
int rmsn2d(int idev, float *x, int ilowb, int ihighb,
            int ilowa,int ihigha, float *avg, float *rnoise,
            int (*read2d)(void*,int,float*,int,int),
            void *userdata)
{
    int narea;
    float xarea;

    if (!okarea2 (ilowb,ihighb,ilowa,ihigha))
        return FALSE;

    *avg = suma2d(idev,x,ilowb,ihighb,ilowa,ihigha,read2d,userdata);
    narea = (ihighb-ilowb+1)*(ihigha-ilowa+1);
    xarea = (float) narea;
    if (narea <= 0) {
        *avg    = 0.0;
        *rnoise = 0.0;
    }
    else if (narea <= 2) {
        *avg    /= xarea;
        *rnoise = 0.0;
    }
    else {
        *avg    /= xarea;
        *rnoise = chsq2d(idev,x,ilowb,ihighb,ilowa,ihigha,
                         *avg,read2d,userdata);
        *rnoise = sqrt(*rnoise / xarea);
    }
    return TRUE;
}
