
#include <stdio.h>
#include <stddef.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
#include <ctype.h>
#include <limits.h>
#include <float.h>
#include "nmrtool.h"

#ifndef M_PI
#define M_PI 3.14159265358979323846
#endif

void conv (int lx, int ifx, float *x, int ly, int ify, float *y,
	int lz, int ifz, float *z);
void hilbert (int n, float x[], float y[]);

/* Copyright (c) Colorado School of Mines, 1995.*/
/* All rights reserved.                       */

/*********************** self documentation **********************/
/*****************************************************************************
HILBERT - Compute Hilbert transform y of x

hilbert		compute the Hilbert transform

******************************************************************************
Function Prototype:
void hilbert (int n, float x[], float y[]);

******************************************************************************
Input:
n		length of x and y
x		array[n] to be Hilbert transformed

Output:
y		array[n] containing Hilbert transform of x

******************************************************************************
Notes:
The Hilbert transform is computed by convolving x with a
windowed (approximate) version of the ideal Hilbert transformer.

******************************************************************************
Author:  Dave Hale, Colorado School of Mines, 06/02/89
*****************************************************************************/
/**************** end self doc ********************************/

/* #include "cwp.h" */

#define LHHALF 30	/* half-length of Hilbert transform filter*/
#define LH 2*LHHALF+1	/* filter length must be odd */
/* Albert van Kuik */
#define LHHALFMAX	(LHHALF * 256)
#define LHMAX		2*LHHALFMAX+1

void hilbert (int n, float x[], float y[])
/*****************************************************************************
Compute Hilbert transform y of x
******************************************************************************
Input:
n		length of x and y
x		array[n] to be Hilbert transformed

Output:
y		array[n] containing Hilbert transform of x
******************************************************************************
Notes:
The Hilbert transform is computed by convolving x with a
windowed (approximate) version of the ideal Hilbert transformer.
******************************************************************************
Author:  Dave Hale, Colorado School of Mines, 06/02/89
*****************************************************************************/
{
	static int madeh=0;
	static float h[LHMAX];
	int i;
	float taper;
        /* 
         * Albert van Kuik, need more points for larger arrays 
         */
        int lh, lhhalf = LHHALF * n / 16;

        if (lhhalf > (n-1) / 2)
            lhhalf = n - 1;
        if (lhhalf > LHHALFMAX)
            lhhalf = LHHALFMAX;
        if (lhhalf < LHHALF)
            lhhalf = LHHALF;
        lh = lhhalf * 2 + 1;

	/* if not made, make Hilbert transform filter; use Hamming window */

	if (madeh != lh) {
		h[lhhalf] = 0.0;
		for (i=1; i<=lhhalf; i++) {
			taper = 0.54+0.46*cos(M_PI*(float)i/(float)(lhhalf));
			h[lhhalf+i] = taper*(-(float)(i%2)*2.0/
				(M_PI*(float)(i)));
			h[lhhalf-i] = -h[lhhalf+i];
		}
		madeh = lh;
	}

	/* convolve Hilbert transform with input array */
	conv(lh,-lhhalf,h,n,0,x,n,0,y);
        /*
         * Albert van kuik, better to negate array
         */
        for (i=0;i<n;i++)
            y[i] = -y[i];
}


