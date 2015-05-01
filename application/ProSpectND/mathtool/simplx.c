
#include <math.h>
#include "mathtool.h"

/*
 *
 * simplx optimization routine 
 * 
 * n   = number of iteration paramaters in x
 * x   = array of initial parameters on input, and new parameters on output
 * f   = output, the lowest value returned from funct
 * tol = tolerance ??
 * iw  = n+1 ??
 * ldp = size of work arrays xn, pdash, pastx, pdastx, and y
 *       the size of work array p = ldp * ldp
 * funct = an evaluation function of the form:
 *         funct(void *data, int n, float *xs, float *fc)
 *         where: data = a pointer to a user defined data block
 *                n    = the number of parameters
 *                xs   = array of the current parameters
 *                fc   = must return an evaluation value of the new
 *                       parameter. Smaller is better. 0 = perfect
 * maxcal = the max number of iterations
 */

#define P(x,y)  (p[(y) * ldp + (x)])


void simplx(int n, float *x, float *f, float tol, 
            int iw, float *xn, float *pdash, float *pastx, 
            float *pdastx, float *y, float *p, int ldp, 
            void (*funct)(void*,int,float*,float*),
            void *data, int maxcal)
{
    float yerr,yerrold=0.0,yh,yl,yast,ydast,fm,sqrt2;
    int   iyhigh,iylow;
    int   i,j,icount=0;

    for (i=0;i<n;i++)
        P(i,0) = x[i];
        
    /*
     * calculate initial pi and yi
     */
    sqrt2 = sqrt(2);
    for (i=0;i<n;i++) {
        xn[i] = x[i] * (sqrt((float)iw) + n - 1)/(sqrt2*n);
        pdash[i] = x[i] * (sqrt((float)iw) - 1.0)/(sqrt2*n);
    }

    for (i=1;i<iw;i++) {
        for (j=0;j<n;j++)
            P(j,i) = P(j,0) + pdash[j];
        P(i-1,i) +=  xn[i-1];
    }

    for (i=0;i<iw;i++) {
        for (j=0;j<n;j++)
            xn[j] = P(j,i);

        funct(data,n,xn,&(y[i]));
    }

    /*
     * determine yh and yl
     */

L110:

    yh=y[0];
    yl=y[0];
    for (i=0;i<iw;i++) {
        if (yh <= y[i]) {
            yh = y[i];
            iyhigh = i;
        }
        if (yl >= y[i]) {
            yl = y[i];
            iylow = i;
        }
    }
    /*
     *     calculate p-centroid
     */

    for (i=0;i<n;i++)
        xn[i] = 0.0;

    for (i=0;i<n;i++)
        for (j=0;j<iw;j++)
            if (j != iyhigh) 
               xn[i] += P(i,j);

    for (i=0;i<n;i++)
        pdash[i] = xn[i]/(float)n;

    /*
     * form p=(1+a)pdash-aph, give a value 1
     */
    for (i=0;i<n;i++)
        pastx[i] = 2.0 * pdash[i] - P(i,iyhigh);
    funct(data,n,pastx,&yast);

    if (yast < yl) goto L1000;
    /*
     *    test y*>yi, i neq h
     */

    for (i=0;i<iw;i++) {
        if (i == iyhigh)
            break;
        if (yast < y[i]) goto L2030;
    }
 
    goto L3000;
    /*
     *   replace ph by p*
     */
L2030:
    for (i=0;i<n;i++)
        P(i,iyhigh) = pastx[i];
    y[iyhigh] = yast;
L2040:
    fm = 0.0;
    for (i=0;i<iw;i++)
        fm += y[i];
    fm /= (float)iw;
    yerr = 0.0;
    for (i=0;i<iw;i++)
        yerr += (y[i]-fm)*(y[i]-fm);
    yerr /= (float)iw;
    for (i=0;i<n;i++)
        x[i] = P(i,iylow);
    *f = yl;
    if (yerr < tol)  
        return;
    if (fabs(yerr - yerrold) < 1.e-20) 
        return;
    yerrold = yerr;
    icount++;
    if (icount == maxcal)  
        return;
    goto L110;
    /*
     *     calculate p**
     */
L1000:
    for (i=0;i<n;i++)
        pdastx[i] = 2.0 * pastx[i] - pdash[i];

    /*
     * calculate y**
     */
     funct(data,n,pdastx,&ydast);
     if (ydast >= yl) goto L2030;
    /*
     *    replace ph by p**
     */
L1030:
    for (i=0;i<n;i++)
        P(i,iyhigh) = pdastx[i];

    y[iyhigh] = ydast;
    goto L2040;

L3000:
    if (yast <= yh) {
        for (i=0;i<n;i++)
            P(i,iyhigh) = pastx[i];
        yh = yast;
        y[iyhigh] = yh;
    }
    /*
     *  calculate p**=bph+(1-b)pdash, value b is .5
     */
    for (i=0;i<n;i++)
        pdastx[i] = 0.5 * P(i,iyhigh) + 0.5 * pdash[i];
    funct(data,n,pdastx,&ydast);

    if (ydast > (min(yast,yh))) {
        for (i=0;i<n;i++)
            xn[i] = 0.0;

        for (j=0;j<n;j++)
            for (i=0;i<iw;i++)
                xn[j] += P(j,i);

        for (i=0;i<n;i++)
            xn[i] /= (float)iw;

        for (j=0;j<iw;j++) 
            for(i=0;i<n;i++)
                P(i,j) -= xn[i] + P(i,iylow);

        for (j=0;j<iw;j++) {
            for(i=0;i<n;i++)
                xn[i] = P(i,j);
            funct(data,n,xn,&(y[j]));
        }
    }
    else
        goto L1030;

    goto L2040;
    
}

