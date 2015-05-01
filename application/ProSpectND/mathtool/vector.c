/*
 vector.c
 */
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#include <math.h>
#include <assert.h>
#include "mathtool.h"


/*
 * allocate matrices
 */
int *ivector(int minx, int maxx)
{
    int *m;

    m = (int*) malloc((maxx-minx+1) * sizeof(int));
    assert(m);
    m -= minx;
    return m;
}

int *resize_ivector(int *vec, int minx, int maxx)
{
    int *m;

    m = (int*) realloc(vec+minx, (maxx-minx+1) * sizeof(int));
    assert(m);
    m -= minx;
    return m;
}

void free_ivector(int *m, int minx)
{
    free(m+minx);
}

float *fvector(int minx, int maxx)
{
    float *m;

    m = (float*) malloc((maxx-minx+1) * sizeof(float));
    assert(m);
    m -= minx;
    return m;
}

float *resize_fvector(float *vec, int minx, int maxx)
{
    float *m;

    m = (float*) realloc(vec+minx, (maxx-minx+1) * sizeof(float));
    assert(m);
    m -= minx;
    return m;
}

void free_fvector(float *m, int minx)
{
    free(m+minx);
}

double *dvector(int minx, int maxx)
{
    double *m;

    m = (double*) malloc((maxx-minx+1) * sizeof(double));
    assert(m);
    m -= minx;
    return m;
}

void free_dvector(double *m, int minx)
{
    free(m+minx);
}

int **imatrix(int minx, int maxx, int miny, int maxy)
{
    int i;
    int **m, *p;

    m = (int**) malloc((maxx-minx+1) * sizeof(int*));
    assert(m);
    m -= minx;
    p = (int*) malloc((maxx-minx+1) * (maxy-miny+1) * sizeof(int));
    assert(p);
    for (i=minx;i<=maxx;i++) {
        m[i] = p - miny;
        p += (maxy-miny+1);
    }
    return m;
}

/* only first part */
int **enlarge_imatrix(int **vec, int minx, int maxx, int oldmaxx, int miny, int maxy)
{
    int **m, *p, i;

    m = (int**) realloc(vec+minx, (maxx-minx+1) * sizeof(int));
    assert(m);
    p = m[0]+miny;
    m -= minx;
    p = (int*) realloc(p,(maxx-minx+1) * (maxy-miny+1) * sizeof(int));
    assert(p);
    for (i=minx;i<=maxx;i++) {
        m[i] = p - miny;
        p += (maxy-miny+1);
    }
    return m;
}

/* only second part */
int **resize_imatrix(int **m, int minx, int maxx, int miny, int maxy)
{
    int i,isize;
    int *p,*p1,*p2;

    p1 = m[minx];
    p2 = m[minx+1];
    isize = (int)(p2-p1);
    p = (m+minx)[0]+miny;
    p = (int*) realloc(p,(maxx-minx+1) * (maxy-miny+1) * sizeof(int));
    assert(p);
    for (i=maxx;i>minx;i--) {
        p2 = p + (i-minx) * isize;
        p1 = p + (i-minx) * (maxy-miny+1);
        memmove(p1,p2,isize * sizeof(int));
    }
    for (i=minx;i<=maxx;i++) {
        m[i] = p - miny;
        p += (maxy-miny+1);
    }
    return m;
}

void free_imatrix(int **m, int minx, int maxx, int miny, int maxy)
{
    int i;
    int *p;

    m+=minx;
    p = m[0]+miny;
    free(p);
    free(m);
}

float **fmatrix(int minx, int maxx, int miny, int maxy)
{
    int i;
    float **m,*p;

    m = (float**) malloc((maxx-minx+1) * sizeof(float*));
    assert(m);
    m -= minx;
    p = (float*) malloc((maxx-minx+1) * (maxy-miny+1) * sizeof(float));
    assert(p);
    for (i=minx;i<=maxx;i++) {
        m[i] = p - miny;
        p += (maxy-miny+1);
    }
    return m;
}

/* only first part */
float **enlarge_fmatrix(float **vec, int minx, int maxx, int oldmaxx, int miny, int maxy)
{
    float **m, *p;
    int i;

    m = (float**) realloc(vec+minx, (maxx-minx+1) * sizeof(float));
    assert(m);
    p = m[0]+miny;
    m -= minx;
    p = (float*) realloc(p,(maxx-minx+1) * (maxy-miny+1) * sizeof(float));
    assert(p);
    for (i=minx;i<=maxx;i++) {
        m[i] = p - miny;
        p += (maxy-miny+1);
    }
    return m;
}

/* only second part */
float **resize_fmatrix(float **m, int minx, int maxx, int miny, int maxy)
{
    int i,isize;
    float *p,*p1,*p2;

    p1 = m[minx];
    p2 = m[minx+1];
    isize = (int)(p2-p1);
    p = (m+minx)[0]+miny;
    p = (float*) realloc(p,(maxx-minx+1) * (maxy-miny+1) * sizeof(float));
    assert(p);
    for (i=maxx;i>minx;i--) {
        p2 = p + (i-minx) * isize;
        p1 = p + (i-minx) * (maxy-miny+1);
        memmove(p1,p2,isize * sizeof(float));
    }
    for (i=minx;i<=maxx;i++) {
        m[i] = p - miny;
        p += (maxy-miny+1);
    }
    return m;
}

void free_fmatrix(float **m, int minx, int maxx, int miny, int maxy)
{
    int i;
    float *p;

    m+=minx;
    p = m[0]+miny;
    free(p);
    free(m);
}

double **dmatrix(int minx, int maxx, int miny, int maxy)
{
    int i;
    double **m,*p;

    m = (double**) malloc((maxx-minx+1) * sizeof(double*));
    assert(m);
    m -= minx;
    p = (double*) malloc((maxx-minx+1) * (maxy-miny+1) * sizeof(double));
    assert(p);
    for (i=minx;i<=maxx;i++) {
        m[i] = p - miny;
        p += (maxy-miny+1);
    }
    return m;
}

void free_dmatrix(double **m, int minx, int maxx, int miny, int maxy)
{
    int i;
    double *p;

    m+=minx;
    p = m[0]+miny;
    free(p);
    free(m);
}



