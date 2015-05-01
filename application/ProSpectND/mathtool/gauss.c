
#include <math.h>

/*
 *.....from:
 *     riess and johnson, numerical analysis
 *     addison-wesley publishing company, 1982
 */
void gauss(float *aug[], float *x, int n, int *ier, float *rnorm)
{
    int i,j,k,nm1,np1,ipivot,ip1;
    float pivot,temp,rsq,resi,q;
    
    /*
     * arrays 'aug' and 'x' start at 1, not 0
     */
    nm1 = n-1;
    np1 = n+1;
    for (i=1;i<=nm1;i++) {
        pivot = 0.0;
        for (j=i;j<=n;j++) {
            temp = fabs(aug[i][j]);
            if (pivot >= temp) 
                continue;
            pivot  = temp;
            ipivot = j;
        }
        if (pivot == 0.0) {
            *ier = 2;
            return;
        }
        if (ipivot != i) {
            for (k=i;k<=np1;k++) {
                temp           = aug[k][i];
                aug[k][i]      = aug[k][ipivot];
                aug[k][ipivot] = temp;
            }
        }
        ip1 = i+1;
        for (k=ip1;k<=n;k++) {
            q = -aug[i][k]/aug[i][i];
            aug[i][k] = 0.0;
            for (j=ip1;j<=np1;j++)
                aug[j][k] += q * aug[j][i];
        }
    }
    if (aug[n][n] == 0.0) {
        *ier = 2;
        return;
    }
    x[n] = aug[np1][n]/aug[n][n];
    for (k=1;k<=nm1;k++) {
        q = 0.0;
        for (j=1;j<=k;j++)
            q += aug[np1-j][n-k] * x[np1-j];
        x[n-k] =(aug[np1][n-k] - q)/aug[n-k][n-k];
    }

    rsq = 0.0;
    for (i=1;i<=n;i++) {
        q = 0.0;
        for (j=1;j<=n;j++)
            q = aug[j][i] * x[j];
        resi  = fabs(aug[np1][i]-q);
        rsq  += resi * resi;
    }
    *rnorm = sqrt(rsq);
    *ier = 1;
}

