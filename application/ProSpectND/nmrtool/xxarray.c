/********************************************************************/
/*                            xxarray.c                             */
/*                                                                  */
/*                                                                  */
/*  array processing routines                                       */
/*                                                                  */
/*  Albert van Kuik, 1998                                           */  
/********************************************************************/
#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <string.h>
#include "mathtool.h"
#include "nmrtool.h"

#define STRLEN	250
/*
 * Shift array elements to the left or to the right
 * n1 = first element
 * n2 = last element
 * n3 = shift size
 * n4 = array size
 */
void xxshfn(float *x, int n1, int n2, int n3, int n4)
{ 
    int i;

    /*
     * array starts at 1
     */
    x--;
    if (n2 <= n1)
        return;
    /*
     * Left shift
     */
    if (n3 > 0) {
        int n5 = n2+n3-n4;
        if (n5 > 0) {
            n2 -= n5;
            if (n2 <= n1)
                return;
            memmove(x + n1, x + n1 + n3, sizeof(float) * (n2 - n1));
            memset(x + n2, 0, sizeof(float) * n5);
        }
        else
            memmove(x + n1, x + n1 + n3, sizeof(float) * (n2 - n1));
    }
    /*
     * Right shift
     */
    else if (n3 < 0) {
        int n5 = n1+n3;
        if (n5 < 0) {
            n1 -= n5;
            if (n2 <= n1)
                return;
            memmove(x + n1, x + n1 + n3, sizeof(float) * (n2 - n1));
            memset(x + n1 + n5, 0, sizeof(float) * -n5);
        }
        else
            memmove(x + n1, x + n1 + n3, sizeof(float) * (n2 - n1));
    }
}

/*
 * Cyclic shift. If n3 > 0, all points from n1 to n2
 * are shifted n3 points to the left. Points that are
 * shifted left of n1 are added at the right. If n3 < 0
 * points are shifted to the right
 */
void xxrotate(float *x, int n1, int n2, int n3)
{
    float *tmpx;
    int n4 = n2 - n1 + 1;

    if (n4 < 2)
        return;
    n3 %= n4;
    if (n3 == 0)
        return;
    if (n3 < 0)
        n3 = n4 + n3;
    /*
     * array starts at 1
     */
    x--;
    tmpx = (float*) malloc(sizeof(float) * n3);
    memcpy(tmpx, x + n1, sizeof(float) * n3);
    memmove(x + n1, x + n1 + n3, sizeof(float) * (n4 - n3));
    memcpy(x + n2 - n3 + 1, tmpx, sizeof(float) * n3);
    free(tmpx);
}

/*
 * Add value to all elements of the array
 *
 * x   = pointer to array
 * n1  = first element
 * n2  = last element
 * val = value to add
 */
void xxaddv(float *x, int n1, int n2, float val)
{
    int i;

    /*
     * array starts at 1
     */
    x--;
    for (i=n1;i<=n2;i++)
        x[i] += val;
}


/*
 * Add all elements of the array x to the 
 * corresponding elements of array y
 *
 * x   = pointer to array x
 * y   = pointer to array y
 * n1  = first element
 * n2  = last element
 */
void xxaddx(float *x, float *y, int n1, int n2)
{
    int i;

    /*
     * array starts at 1
     */
    x--;
    y--;
    for (i=n1;i<=n2;i++)
        x[i] += y[i];
}

/*
 * Set all elements of the array to value
 *
 * x   = pointer to array
 * n1  = first element
 * n2  = last element
 * val = value to set
 */
void xxfilv(float *x, int n1, int n2, float val)
{
    int i;

    /*
     * array starts at 1
     */
    x--;
    for (i=n1;i<=n2;i++)
        x[i] = val;
}

/*
 * Multiply all elements of the array with value
 *
 * x   = pointer to array
 * n1  = first element
 * n2  = last element
 * val = value to multiply by
 */
void xxmulv(float *x, int n1, int n2, float val)
{
    int i;

    /*
     * array starts at 1
     */
    x--;
    for (i=n1;i<=n2;i++)
        x[i] *= val;
}

/*
 * Multiply all elements of the array x by the 
 * corresponding elements of array y
 *
 * x   = pointer to array x
 * y   = pointer to array y
 * n1  = first element
 * n2  = last element
 */
void xxmulx(float *x, float *y, int n1, int n2)
{
    int i;

    /*
     * array starts at 1
     */
    x--;
    y--;
    for (i=n1;i<=n2;i++)
        x[i] *= y[i];
}


/*
 * return the sum off all elements of array x
 * between points n1 and n2
 * Note: array starts at 0, but first point is denoted 1
 *       and last point is denoted nsize (the size of the array)
 */
float xxsumz(float *x, int n1, int n2)
{
    int i;
    float sum = 0.0;

    /*
     * array starts at 1
     */
    x--;
    for (i=n1;i<=n2;i++)
        sum += x[i];
    return sum;
}

/*
 * Swap all elements of the array x
 * 
 * 1234 -> 2143
 *
 * x   = pointer to array x
 * n1  = first element
 * n2  = last element
 */
void xxswap2(float *x, int n1, int n2)
{
    int i;
    unsigned char *p, tmp;

    /*
     * array starts at 1
     */
    p = (unsigned char*) (x - 1);
    n1 *= 4;
    n2 *= 4;
    for (i=n1;i<=n2;i+=2) {
        tmp = p[i];
        p[i] = p[i+1];
        p[i+1] = tmp;
    }
}

/*
 * Swap all elements of the array x
 * 
 * 1234 -> 4321
 *
 * x   = pointer to array x
 * n1  = first element
 * n2  = last element
 */
void xxswap4(float *x, int n1, int n2)
{
    int i;
    unsigned char *p, tmp;

    /*
     * array starts at 1
     */
    p = (unsigned char*) (x - 1);
    n1 *= 4;
    n2 *= 4;
    for (i=n1;i<=n2;i+=4) {
        tmp = p[i];
        p[i] = p[i+3];
        p[i+3] = tmp;
        tmp = p[i+1];
        p[i+1] = p[i+2];
        p[i+2] = tmp;
    }
}

/*
 * Swap all elements of the array x
 * 
 * 1234 5678 -> 8765 4321
 *
 * x   = pointer to array x
 * n1  = first element
 * n2  = last element
 */
void xxswap8(float *x, int n1, int n2)
{
    int i,j;
    unsigned char *p, tmp;

    /*
     * array starts at 1
     */
    p = (unsigned char*) (x - 1);
    n1 *= 4;
    n2 *= 4;
    for (i=n1;i<=n2;i+=8) {
        for (j=0;j<4;j++) {
            tmp = p[i+j];
            p[i+j] = p[i+7-j];
            p[i+7-j] = tmp;
        }
    }
}



/*
 * Convert all elements of the array x
 * from integers to floats
 *
 * x   = pointer to array x
 * n1  = first element
 * n2  = last element
 */
void xxi2f(float *x, int n1, int n2)
{
    int i;
    int *p;

    /*
     * array starts at 1
     */
    x--;
    p = (int*) x;
    for (i=n1;i<=n2;i++) 
        x[i] = (float) p[i];
}

/*
 * Convert all elements of the array x
 * from floats to integers
 *
 * x   = pointer to array x
 * n1  = first element
 * n2  = last element
 */
void xxf2i(float *x, int n1, int n2)
{
    int i;
    int *p;

    /*
     * array starts at 1
     */
    x--;
    p = (int*) x;
    for (i=n1;i<=n2;i++) 
        p[i] = (int) x[i];
}

/*
 * convert array of floats into ascii string
 * Non-ascii characters are converted to spaces
 *
 * x   = pointer to array x
 * n1  = first element
 * n2  = last element
 */
char *xxf2ascii(float *x, int n1, int n2)
{
    int i;
    char   *p;
    static char result[STRLEN+1];
    /*
     * array starts at 1
     */
    n1--;
    p = (char*) x;
    n2 = min(STRLEN+n1, n2);
    n1 *= 4;
    n2 *= 4;
    for (i=n1;i<n2;i++) {
        if (p[i] >= 32 && p[i] <= 126)
            result[i-n1] = p[i];
        else
            result[i-n1] = ' ';
    }
    result[i-n1] = '\0';
    return result;
}

/*
 * Split array of complex data into seperate array's of real
 * and imaginary data
 * co = complex data
 * re = real data
 * im = imaginary data
 * n1 = first point of complex array
 * n2 = last point of complex array
 *
 * co of size n splits into 2 arrays of size 1/2 n each
 */
void xxc2ri(float *co, float *re, float *ir, int n1, int n2)
{
    int i;

    /*
     * array starts at 1
     */
    co--;
    re--;
    ir--;
    for (i=n1;i<=n2;i++) {
        re[i] = co[2*i-1];
        ir[i] = co[2*i];
    }
}


/*
 * Split array of complex data into seperate array's of real
 * and imaginary data
 * co = complex data
 * re = real data
 * im = imaginary data
 * n1 = first point of complex array
 * n2 = last point of complex array
 *
 * co of size n splits into 2 arrays of size 1/2 n each
 *
 * in contrast to c2ri, re and ir are swapped and ir is
 * multiplied by -1
 */
void xxc2ir(float *co, float *ir, float *re, int n1, int n2)
{
    int i;

    /*
     * array starts at 1
     */
    co--;
    re--;
    ir--;
    for (i=n1;i<=n2;i++) {
        ir[i] = -co[2*i-1];
        re[i] = co[2*i];
    }
}


/*
 * Join seperate array's of real
 * and imaginary data into array of complex data 
 * co = complex data
 * re = real data
 * im = imaginary data
 * n1 = first point of complex array
 * n2 = last point of complex array
 *
 * re and im of size 1/n each, join into 1 array of size n
 */
void xxri2c(float *co, float *re, float *ir, int n1, int n2)
{
    int i;

    /*
     * array starts at 1
     */
    co--;
    re--;
    ir--;
    for (i=n2;i>=n1;i--) {
        co[2*i-1] = re[i];
        co[2*i]   = ir[i];
    }
}

