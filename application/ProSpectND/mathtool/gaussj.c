/*************************************************************************/
/*  Numerical Recipes in C                                               */
/*************************************************************************/
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
#include "mathtool.h"

#define SWAP(a,b)	{double temp=(a);(a)=(b);(b)=temp;}
/*
 * Numerical Recipes in C, pg 36
 *
 * Linear equation solution by Gauss-Jordan elemination. a[1..n][1..n]
 * is an input matrix of n by n elements. b[1..n][1..m] is an input 
 * matrix of n by m containing the m right-hand side vectors. On output, a is
 * replaced by its matrix inverse, and b is replaced by the corresponding set
 * of solution vectors.
 */
int gaussj(double **a, int n, double **b, int m)
{
    int *indxc, *indxr, *ipiv, ier;
    int i, icol, irow,j, k, l, ll;
    double big, dum, pivinv;

    ier = 1;
    indxc = ivector(1,n);
    indxr = ivector(1,n);
    ipiv  = ivector(1,n);
    for (j=1;j<=n;j++)
        ipiv[j] = 0;
    /*
     * This is the main loop over the columns to be reduced
     */
    for (i=1;i<=n;i++) {
        big = 0.0;
        for (j=1;j<=n;j++)
            if (ipiv[j] != 1) 
                for (k=1;k<=n;k++) {
                    if (ipiv[k] == 0) {
                        if (fabs(a[j][k]) >= big) {
                            big = fabs(a[j][k]);
                            irow = j;
                            icol = k;
                        }
                    }
                    else {
                        if (ipiv[k] > 1) {
                            fprintf(stderr,"GAUSSJ: Singular Matrix-1\n");
                            ier = 0;
                            goto out;
                        }
                    }
                }
        ++(ipiv[icol]);
        /*
         * We now have the pivot element, so we intechange rows, if needed
         * to put the pivot element on the diagonal. The columns are not
         * physically interchanged, only relabeled: indxc[i], the column
         * of the ith pivot element, is the ith column that is reduced, while
         * indxr[i] is the row in which that pivot element was originally located.
         * If indxr[i] != indxc[i] there is an implied column interchange.
         * With this form of bookkeeping, the solution b's will end up in the
         * correct order, and the inverse matrix will be scrambled by columns
         */
        if (irow != icol) {
            for (l=1;l<=n;l++)
                SWAP(a[irow][l],a[icol][l])
            for (l=1;l<=m;l++)
                SWAP(b[irow][l],b[icol][l])
        }
        /*
         * We are now ready to divide the pivot row by the pivot element
         * located at irow and icol
         */
        indxr[i] = irow;
        indxc[i] = icol;
        if (a[icol][icol] == 0.0) {
            fprintf(stderr,"GAUSSJ: Singular Matrix-2\n");
            ier = 0;
            goto out;
        }
        pivinv = 1.0/a[icol][icol];
        a[icol][icol] = 1.0;
        for (l=1;l<=n;l++) 
            a[icol][l] *= pivinv;
        for (l=1;l<=m;l++) 
            b[icol][l] *= pivinv;
        /*
         * next, we reduce the rows ...
         * .. except for the pivot one, of course.
         */
        for (ll=1;ll<=n;ll++) {
            if (ll != icol) {
                dum = a[ll][icol];
                a[ll][icol] = 0.0;
                for (l=1;l<=n;l++) 
                    a[ll][l] -= a[icol][l]*dum;
                for (l=1;l<=m;l++) 
                    b[ll][l] -= b[icol][l]*dum;
            }
        }
    }
    /*
     * This is the end of the main loop over columns of the reduction.
     * it only remains to unscramble the solution in view of the column
     * interchanges. We do this by interchanging pairs of columns in the reverse
     * order that the permutation wa built up.
     */
    for (l=n;l>=1;l--) {
        if (indxr[l] != indxc[l])
            for (k=1;k<=n;k++)
                SWAP(a[k][indxr[l]], a[k][indxc[l]])
    }
out:
    free_ivector(ipiv,1);
    free_ivector(indxr,1);
    free_ivector(indxc,1);
    return ier;
}

