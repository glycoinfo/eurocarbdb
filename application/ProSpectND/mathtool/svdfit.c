/*************************************************************************/
/*  Singular value decomposition routines from:                          */
/*  Numerical Recipes in C                                               */
/*************************************************************************/
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
#include "mathtool.h"



#define nerror(s)	fprintf(stderr,s)
/*
 * Numerical Recipes in C, pg 65
 *
 * Solves A.X=B for a vector X, where A is specified by the arrays 
 * u[1..m][1..n], w[1..n], v[1..n][1..n] as returned by svdcmp. m and n are
 * the dimensions of A, and will be equal for squared matrices. b[1..m]
 * is the input right-hand side. x[1..n] is the output solution vector.
 * No input quantities are destroyed, so the routine may be called
 * sequentially with different b's
 */
void svbksbd(double **u, double w[], double **v, int m, int n, 
            double b[], double x[])
{
    int jj,j,i;
    double  s,*tmp;

    tmp=dvector(1,n);
    for (j=1;j<=n;j++) {
        s=0.0;
        /*
         * Nonzero result only if w[j] is nonzero
         */
        if (w[j]) {
            for (i=1;i<=m;i++) 
                s += u[i][j]*b[i];
            s /= w[j];
        }
        tmp[j]=s;
    }
    /*
     * Matrix multiply by V to get answer
     */
    for (j=1;j<=n;j++) {
        s=0.0;
        for (jj=1;jj<=n;jj++) 
            s += v[j][jj]*tmp[jj];
        x[j]=s;
    }
    free_dvector(tmp,1);
}


static double at,bt,ct;
#define PYTHAG(a,b) ((at=fabs(a)) > (bt=fabs(b)) ? \
(ct=bt/at,at*sqrt(1.0+ct*ct)) : (bt ? (ct=at/bt,bt*sqrt(1.0+ct*ct)): 0.0))

static double  maxarg1,maxarg2;
#define MAX(a,b) (maxarg1=(a),maxarg2=(b),(maxarg1) > (maxarg2) ?\
	(maxarg1) : (maxarg2))
#define SIGN(a,b) ((b) >= 0.0 ? fabs(a) : -fabs(a))

/*
 * Numerical Recipes in C, pg 68
 *
 * Given a matrix a[1..m][1..n], this routine computes its singular value
 * decomposition, A = U.W.VT. The matrix U replaces a on output. The diagonal 
 * matrix of singular values W is output as a vector w[1..n]. The matrix V
 * (not the transpose VT) is output as v[1..n][1..n]. m must be greater or
 * equal to n; if it is smaller, then a should be filled up to square
 * with zero rows
 */
int svdcmpd(double **a, int m, int n, double *w, double **v)
{
    int flag,i,its,j,jj,k,l,nm;
    double c=0.0,f=0.0,h=0.0,s=0.0,x=0.0,y=0.0,z=0.0;
    double anorm=0.0,g=0.0,scale=0.0;
    double *rv1;

    at = bt = ct = 0.0;
    if (m < n) {
        nerror("SVDCMP: You must augment A with extra zero rows");
        return 0;
    }
    rv1=dvector(1,n);
    /*
     * Householder reduction to bidiogonal form
     */
    for (i=1;i<=n;i++) {
        l=i+1;
        rv1[i]=scale*g;
        g=s=scale=0.0;
        if (i <= m) {
            for (k=i;k<=m;k++) scale += fabs(a[k][i]);
            if (scale) {
                for (k=i;k<=m;k++) {
                    a[k][i] /= scale;
                    s += a[k][i]*a[k][i];
                }
                f=a[i][i];
                g = -SIGN(sqrt(s),f);
                h=f*g-s;
                a[i][i]=f-g;
                if (i != n) {
                    for (j=l;j<=n;j++) {
                        for (s=0.0,k=i;k<=m;k++) s += a[k][i]*a[k][j];
                        f=s/h;
                        for (k=i;k<=m;k++) a[k][j] += f*a[k][i];
                    }
                }
                for (k=i;k<=m;k++) a[k][i] *= scale;
            }
        }
        w[i]=scale*g;
        g=s=scale=0.0;
        if (i <= m && i != n) {
            for (k=l;k<=n;k++) scale += fabs(a[i][k]);
            if (scale) {
                for (k=l;k<=n;k++) {
                    a[i][k] /= scale;
                    s += a[i][k]*a[i][k];
                }
                f=a[i][l];
                g = -SIGN(sqrt(s),f);
                h=f*g-s;
                a[i][l]=f-g;
                for (k=l;k<=n;k++) rv1[k]=a[i][k]/h;
                if (i != m) {
                    for (j=l;j<=m;j++) {
                        for (s=0.0,k=l;k<=n;k++) s += a[j][k]*a[i][k];
                        for (k=l;k<=n;k++) a[j][k] += s*rv1[k];
                    }
                }
                for (k=l;k<=n;k++) a[i][k] *= scale;
            }
        }
        anorm=MAX(anorm,(fabs(w[i])+fabs(rv1[i])));
    }
    /*
     * Accumulation of right-hand transformations.
     */
    for (i=n;i>=1;i--) {
        if (i < n) {
            if (g) {
                /*
                 * Double division to avoid possible underflow.
                 */
                for (j=l;j<=n;j++)
                    v[j][i]=(a[i][j]/a[i][l])/g;
                for (j=l;j<=n;j++) {
                    for (s=0.0,k=l;k<=n;k++) s += a[i][k]*v[k][j];
                    for (k=l;k<=n;k++) v[k][j] += s*v[k][i];
                }
            }
            for (j=l;j<=n;j++) v[i][j]=v[j][i]=0.0;
        }
        v[i][i]=1.0;
        g=rv1[i];
        l=i;
    }
    /*
     * Accumulation of left-hand transformations.
     */
    for (i=n;i>=1;i--) {
        l=i+1;
        g=w[i];
        if (i < n)
            for (j=l;j<=n;j++) a[i][j]=0.0;
        if (g) {
            g=1.0/g;
            if (i != n) {
                for (j=l;j<=n;j++) {
                    for (s=0.0,k=l;k<=m;k++) s += a[k][i]*a[k][j];
                    f=(s/a[i][i])*g;
                    for (k=i;k<=m;k++) a[k][j] += f*a[k][i];
                }
            }
            for (j=i;j<=m;j++) a[j][i] *= g;
        } else {
            for (j=i;j<=m;j++) a[j][i]=0.0;
        }
        ++a[i][i];
    }
    /*
     * Diagonalization of the bidiagonal form.
     */
    /*
     * Loop over singular values
     */
    for (k=n;k>=1;k--) {
        /*
         * Loop over allowed iterations
         */
        for (its=1;its<=80;its++) {
            flag=1;
            /*
             * Test for splitting
             */
            for (l=k;l>=1;l--) {
                nm=l-1;
                /*
                 * Note that rv1[1]  is always zero.
                 */
                if (fabs(rv1[l])+anorm == anorm) {
                    flag=0;
                    break;
                }
                if (fabs(w[nm])+anorm == anorm) break;
            }
            if (flag) {
                /*
                 * Cancellation of rv1[l], if l > 1.
                 */
                c=0.0;
                s=1.0;
                for (i=l;i<=k;i++) {
                    f=s*rv1[i];
                    rv1[i]=c*rv1[i];
                    if (fabs(f)+anorm == anorm) break;
                 /*   if (fabs(f)+anorm != anorm) {*/
                        g=w[i];
                        h=PYTHAG(f,g);
                        w[i]=h;
                        h=1.0/h;
                        c=g*h;
                        s=(-f*h);
                        for (j=1;j<=m;j++) {
                            y=a[j][nm];
                            z=a[j][i];
                            a[j][nm]=y*c+z*s;
                            a[j][i]=z*c-y*s;
                        }
               /*     }*/
                }
            }
            z=w[k];
            /*
             * Convergence.
             */
            if (l == k) {
                /*
                 * Singular value is made non-negative.
                 */
                if (z < 0.0) {
                    w[k] = -z;
                    for (j=1;j<=n;j++) v[j][k]=(-v[j][k]);
                }
                break;
            }
            if (its == 80){ 
                nerror("SVDCMP: no convergence in 80 SVD iteration\n");
                return 0;
            }
            /*
             * Shift from bottom 2-by-2 minor:
             */
            x=w[l];
            nm=k-1;
            y=w[nm];
            g=rv1[nm];
            h=rv1[k];
            f=((y-z)*(y+z)+(g-h)*(g+h))/(2.0*h*y);
            g=PYTHAG(f,1.0);
            f=((x-z)*(x+z)+h*((y/(f+SIGN(g,f)))-h))/x;
            /*
             * Next QR transformation
             */
            c=s=1.0;
            for (j=l;j<=nm;j++) {
                i=j+1;
                g=rv1[i];
                y=w[i];
                h=s*g;
                g=c*g;
                z=PYTHAG(f,h);
                rv1[j]=z;
                c=f/z;
                s=h/z;
                f=x*c+g*s;
                g=g*c-x*s;
                h=y*s;
                y=y*c;
                for (jj=1;jj<=n;jj++) {
                    x=v[jj][j];
                    z=v[jj][i];
                    v[jj][j]=x*c+z*s;
                    v[jj][i]=z*c-x*s;
                }
                z=PYTHAG(f,h);
                /*
                 * Rotation can be arbirtary if Z=0.
                 */
                w[j]=z;
                if (z) {
                    z=1.0/z;
                    c=f*z;
                    s=h*z;
                }
                f=(c*g)+(s*y);
                x=(c*y)-(s*g);
                for (jj=1;jj<=m;jj++) {
                    y=a[jj][j];
                    z=a[jj][i];
                    a[jj][j]=y*c+z*s;
                    a[jj][i]=z*c-y*s;
                }
            }
            rv1[l]=0.0;
            rv1[k]=f;
            w[k]=x;
        }
    }
    free_dvector(rv1,1);
    return 1;
}


#define TOL 1.0e-5

/*
 * Numerical Recipes in C, pg 537
 *
 * Given a set of points x[1..ndata], y[1..ndata] with individual standard
 * deviations given by sig[1..ndata], use chi-square minimization to determine
 * the coefficients a[1..ma] of the fitting function y = SIGMA ai x afunci(x).
 * Here we solve the fitting equations using singular value decomposition
 * of the ndata x ma matrix. Arrays u[1..ndata][1..ma], v[1..ma][1..ma],
 * and w[1..ma] provide workspace on input; on output they define the singular
 * value decomposition, and can be used to obtain the covariance matrix. The
 * program returns values for the ma fit parameters a, and chi-square, chisq.
 * the user supplies a routine funcs(x,afunc,ma) that returns the ma basis 
 * functions evaluated as x =x in the array afunc[1..ma].
 */
int  svdfit(float x[], float y[], float sig[], int ndata, double a[],
            int ma, double **u, double **v, double *w,
            double *chisq, void (*funcs)(float,double*,int))
{
    int i,j,ier;
    double wmax,tmp,thresh,sum,*b,*afunc;
    
    b = dvector(1,ndata);
    afunc = dvector(1,ma);
    /*
     * Accumulate coefficients of the fitting matrix.
     */
    for (i=1;i<=ndata;i++) {
        (*funcs)(x[i],afunc,ma);
        tmp = 1.0/sig[i];
        for (j=1;j<=ma;j++) u[i][j]=afunc[j]*tmp;
        b[i]=y[i]*tmp;
    }
    ier = svdcmpd(u,ndata,ma,w,v);
    if (ier != 1)
        goto out;
    /*
     * Edit the singular values
     */
    wmax=0.0;
    for (j=1;j<=ma;j++)
        if (w[j] > wmax)
            wmax = w[j];
    thresh =TOL*wmax;
    for (j=1;j<=ma;j++)
        if (w[j] < thresh)
           w[j] = 0.0;
    
    svbksbd(u,w,v,ndata,ma,b,a);
    /*
     * Evaluate chi-square.
     */
    *chisq=0.0;
    for (i=1;i<=ndata;i++) {
        (*funcs)(x[i],afunc,ma);
        for (sum=0.0,j=1;j<=ma;j++) sum += a[j]*afunc[j];
        *chisq += (tmp=(y[i]-sum)/sig[i],tmp*tmp);
    }
out:
    free_dvector(afunc,1);
    free_dvector(b,1);
    return ier;
}

