

#include "mathtool.h"

/*
 * Inverse array
 */
void invrse(float *x, int n)
{
    int i, nh, ih;
    float xdum;
    
    nh = n/2;
    ih = n;
    for (i=0;i<nh;i++) {
        ih--;
        xdum  = x[i];
        x[i]  = x[ih];
        x[ih] = xdum;
    }
}

/*
 * Inverse array from start to stop
 */
void invrse2(float *x, int start, int stop)
{
    int i, nh, ih, n, i1, i2;
    float xdum;
    
    i1 = min(start, stop);
    i2 = max(start, stop);
    x += i1 - 1;
    n  = i2 - i1 + 1;
    nh = n/2;
    ih = n;
    for (i=0;i<nh;i++) {
        ih--;
        xdum  = x[i];
        x[i]  = x[ih];
        x[ih] = xdum;
    }
}


