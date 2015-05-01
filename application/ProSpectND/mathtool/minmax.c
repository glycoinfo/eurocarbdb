

#include "mathtool.h"

/*
 * return lowest and highest values of array x in
 * xmin and xmax, repectively
 * n is the size of array x
 */
void minmax(float *x, int size, float *xmin, float *xmax)
{
    int i;

    *xmin=x[0];
    *xmax=x[0];
    if (size <= 1)
        return;
    for (i=1;i<size;i++) { 
        *xmax = max( *xmax, x[i]);
        *xmin = min( *xmin, x[i]);
    }
}

