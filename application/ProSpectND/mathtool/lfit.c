/*************************************************************************/
/*  Numerical Recipes in C                                               */
/*************************************************************************/
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
#include "mathtool.h"


#define SWAP(a,b) {swap=(a);(a)=(b);(b)=swap;}

/*
 * Expand in storage the covariance matrix covar, so as to take 
 * into account parameters that are
 * being held xed. (For the latter, return zero covariances.)
 */
void covsrt(double **covar, int ma, int ia[], int mfit)
{
    int i,j,k;
    double swap;

    for (i=mfit+1;i<=ma;i++)
        for (j=1;j<=i;j++) covar[i][j]=covar[j][i]=0.0;
    k=mfit;
    for (j=ma;j>=1;j--) {
        if (ia[j]) {
            for (i=1;i<=ma;i++) SWAP(covar[i][k],covar[i][j])
            for (i=1;i<=ma;i++) SWAP(covar[k][i],covar[j][i])
            k--;
        }
    }
}

static double sqrtarg;
#define SQR(a) (sqrtarg=(a),sqrtarg*sqrtarg)

int lfit(float x[], float y[], float sig[], int ndat, double a[], int ia[],
          int ma, double **covar, double *chisq, 
          void (*funcs)(float, double [], int))
/*
 * Given a set of data points x[1..ndat], y[1..ndat] with individual standard deviations
 * sig[1..ndat], use chi^ 2 minimization to fit for some or all of the 
 * coefficients a[1..ma] of a function that depends linearly on a, y = SUMi
 * ai x afunci(x). The input array ia[1..ma] indicates by nonzero entries 
 * those components of a that should be fitted for, and by zero entries
 * those components that should be held fixed at their input values. The program 
 * returns values for a[1..ma], chi^ 2 = chisq, and the covariance matrix 
 * covar[1..ma][1..ma]. (Parameters held fixed will return zero covariances.) 
 * The user supplies a routine funcs(x,afunc,ma) that
 * returns the ma basis functions evaluated at x = x in the array afunc[1..ma].
 */
{
    int i,j,k,l,m,mfit=0,ier;
    double ym,wt,sum,sig2i,**beta,*afunc;

    ier = 1;
    for (j=1;j<=ma;j++)
        if (ia[j]) mfit++;
    if (mfit == 0) {
        fprintf(stderr,"lfit: no parameters to be fitted\n");
        ier = 0;
        return ier;
    }
    beta=dmatrix(1,ma,1,1);
    afunc=dvector(1,ma);
    /*
     * Initialize the (symmetric) matrix.
     */
    for (j=1;j<=mfit;j++) { 
        for (k=1;k<=mfit;k++) covar[j][k]=0.0;
        beta[j][1]=0.0;
    }
    /*
     * Loop over data to accumulate coeffcients of
     * the normal equations.
     */
    for (i=1;i<=ndat;i++) { 
        (*funcs)(x[i],afunc,ma);
        ym=y[i];
        /*
         * Subtract off dependences on known pieces
         * of the fitting function. 
         */
        if (mfit < ma) { 
            for (j=1;j<=ma;j++)
                if (!ia[j]) ym -= a[j]*afunc[j];
        }
        sig2i=1.0/SQR(sig[i]);
        for (j=0,l=1;l<=ma;l++) {
            if (ia[l]) {
                wt=afunc[l]*sig2i;
                for (j++,k=0,m=1;m<=l;m++)
                    if (ia[m]) covar[j][++k] += wt*afunc[m];
                beta[j][1] += ym*wt;
            }
        }
    }
    /*
     * Fill in above the diagonal from symmetry.
     */
    for (j=2;j<=mfit;j++) 
        for (k=1;k<j;k++)
            covar[k][j]=covar[j][k];
    /*
     * Matrix solution.
     */
    ier = gaussj(covar,mfit,beta,1); 
    if (ier == 0)
        goto out;
    /*
     * Partition solution to appropriate coefficients
     * a. 
     */
    for (j=0,l=1;l<=ma;l++)
        if (ia[l]) a[l]=beta[++j][1];
    *chisq=0.0;
    /*
     * Evaluate chi^2 of the fit.
     */
    for (i=1;i<=ndat;i++) { 
        (*funcs)(x[i],afunc,ma);
        for (sum=0.0,j=1;j<=ma;j++) sum += a[j]*afunc[j];
        *chisq += SQR((y[i]-sum)/sig[i]);
    }
    /*
     * Sort covariance matrix to true order of fitting
     * coefficients. 
     */
    covsrt(covar,ma,ia,mfit);
out: 
    free_dvector(afunc,1);
    free_dmatrix(beta,1,ma,1,1);
    return ier;
}


