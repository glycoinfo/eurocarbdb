/*************************************************************************/
/*  Levenberg-Marquardt method from:                                     */
/*  Numerical Recipes in C                                               */
/*************************************************************************/

#include <stdlib.h>
#include <stdio.h>
#include <math.h>
#include "mathtool.h"

/*
 *
 * Used by mrqmin to evaluate the linearized tting matrix alpha,
 * and vector beta as in (15.5.8), and calculate chi^2.
 */
void mrqcof(float x[], float y[], float sig[], int ndata, double a[], int ia[],
            int ma, double **alpha, double beta[], double *chisq,
            void (*funcs)(float, double [], double *, double [], int))
{
    int i,j,k,l,m,mfit=0;
    double ymod,wt,sig2i,dy,*dyda;

    dyda=dvector(1,ma);
    for (j=1;j<=ma;j++)
        if (ia[j]) mfit++;
    for (j=1;j<=mfit;j++) { 
        /*
         * Initialize (symmetric) alpha, beta.
         */
        for (k=1;k<=j;k++) alpha[j][k]=0.0;
        beta[j]=0.0;
    }
    *chisq=0.0;
    for (i=1;i<=ndata;i++) { 
        /*
         * Summation loop over all data.
         */
        (*funcs)(x[i],a,&ymod,dyda,ma);
        sig2i=1.0/(sig[i]*sig[i]);
        dy=y[i]-ymod;
        for (j=0,l=1;l<=ma;l++) {
            if (ia[l]) {
                wt=dyda[l]*sig2i;
                for (j++,k=0,m=1;m<=l;m++)
                    if (ia[m]) alpha[j][++k] += wt*dyda[m];
                beta[j] += dy*wt;
            }
        }
        *chisq += dy*dy*sig2i; 
        /*
         * And chi^2 .
         */
    }
    for (j=2;j<=mfit;j++) 
        /*
         * Fill in the symmetric side.
         */
        for (k=1;k<j;k++) alpha[k][j]=alpha[j][k];
    free_dvector(dyda,1);
}

/*
 * Levenberg-Marquardt method, attempting to reduce the value chi^2 of a fit
 * between a set of data points x[1..ndata], y[1..ndata] with individual
 * standard deviations sig[1..ndata], and a nonlinear function dependent on
 * ma coecients a[1..ma]. The input array ia[1..ma] indicates by nonzero
 * entries those components of a that should be fitted for, and by zero
 * entries those components that should be held fixed at their input values.
 * The program re-turns current best-fit values for the parameters
 * a[1..ma], and chi^2 = chisq. The arrays covar[1..ma][1..ma],
 * alpha[1..ma][1..ma] are used as working space during most iterations.
 * Supply a routine funcs(x,a,yfit,dyda,ma) that evaluates the fitting
 * function yfit, and its derivatives dyda[1..ma] with respect to the
 * fitting parameters a at x. On the first call provide an initial guess for
 * the parameters a, and set alamda<0 for initialization (which then sets
 * alamda=.001). If a step succeeds chisq becomes smaller and alamda de-
 * creases by a factor of 10. If a step fails alamda grows by a factor of
 * 10. You must call this routine repeatedly until convergence is achieved. Then, make
 * one final call with alamda=0, so that covar returns the covariance matrix,
 * and alpha the curvature matrix.
 *
 * AK: iconstr, and constr are array's of size 1..ma,1..2
 * if iconstr[n][0] > 0 then constr[n][0] contains a lower limit of a[n]
 * if iconstr[n][1] > 0 then constr[n][1] contains an upper limit of a[n]
 *
 */
int  mrqmin(float x[], float y[], float sig[], int ndata, double a[], int ma,
            int ia[], double **covar, double **alpha, double *chisq,
            void (*funcs)(float, double *, double *, double *, int), 
            double *alamda, int **iconstr, double **constr)
{
    int j,k,l,ier;
    static int mfit;
    static double ochisq,*atry,*beta,*da,**oneda;

    ier = 1;
    if (*alamda < 0.0) { 
        /*
         * Initialization.
         */
        atry=dvector(1,ma);
        beta=dvector(1,ma);
        da=dvector(1,ma);
        for (mfit=0,j=1;j<=ma;j++)
            if (ia[j]) mfit++;
        oneda=dmatrix(1,mfit,1,1);
        *alamda=0.001;
        mrqcof(x,y,sig,ndata,a,ia,ma,alpha,beta,chisq,funcs);
        ochisq=(*chisq);
        for (j=1;j<=ma;j++) atry[j]=a[j];
    }
    for (j=1;j<=mfit;j++) { 
        /*
         * Alter linearized tting matrix, by augmenting di-
         * agonal elements. 
         */
        for (k=1;k<=mfit;k++) covar[j][k]=alpha[j][k];
        covar[j][j]=alpha[j][j]*(1.0+(*alamda));
        oneda[j][1]=beta[j];
    }
    ier=gaussj(covar,mfit,oneda,1); 
    if (ier == 0)
        goto out;
    /*
     * Matrix solution.
     */
    for (j=1;j<=mfit;j++) da[j]=oneda[j][1];
    if (*alamda == 0.0) { 

        /*
         * Once converged, evaluate covariance matrix.
         */
        covsrt(covar,ma,ia,mfit);
out:
        free_dmatrix(oneda,1,mfit,1,1);
        free_dvector(da,1);
        free_dvector(beta,1);
        free_dvector(atry,1);
        return ier;
    }
    for (j=0,l=1;l<=ma;l++) 
        /*
         * Did the trial succeed?
         */
        if (ia[l]) atry[l]=a[l]+da[++j];

    if (iconstr && constr) for (j=0,l=1;l<=ma;l++) {
        /*
         * Constraints
         */
        if (iconstr[l][1]) 
            if (atry[l] < constr[l][1])
                atry[l] = constr[l][1];
        if (iconstr[l][2]) 
            if (atry[l] > constr[l][2])
                atry[l] = constr[l][2];
    }
    
    mrqcof(x,y,sig,ndata,atry,ia,ma,covar,da,chisq,funcs);
    if (*chisq < ochisq) { 
        /*
         * Success, accept the new solution.
         */
        *alamda *= 0.1;
        ochisq=(*chisq);
        for (j=1;j<=mfit;j++) {
            for (k=1;k<=mfit;k++) alpha[j][k]=covar[j][k];
            beta[j]=da[j];
        }
        for (l=1;l<=ma;l++) a[l]=atry[l];
    } else { 
        /* 
         * Failure, increase alamda and return.
         */
        *alamda *= 10.0;
        *chisq=ochisq;
    }
    return ier;
}


