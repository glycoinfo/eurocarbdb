
#include <math.h>
#include "mathtool.h"

/*
c
c     920222/(rb) derived from bacdic, d in double precision
c     ref.:
c         w.h. press et al., numerical recipes, chap 12, cambridge univ. press
c              (1986)
c         e.t. olejniczak and h.l. eaton, j. magn. reson. 87, 628-632 (1990)
c
*/
void bacdic(float data[], int ndata, double d[], int ipoles,
            float future[], int nfut)
{
    int npoles,j,k,ii,i1,i2,j1,j2;
    double sum, *reg;

    npoles = abs(ipoles);
    reg = dvector(1,npoles);

    /*
     * backward prediction
     */
    if (ipoles < 0) {
        i1 = 1;
        i2 = 1;
        j1 = nfut;
        j2 = -1;
    }
    /*
     * forward prediction
     */
    else {
        i1 = ndata;
        i2 = -1;
        j1 = 1;
        j2 = 1;
    }


    ii = i1;
    for (j=1;j<=npoles;j++) {
        reg[j] = (double) data[ii];
        ii += i2;
    }

    ii = j1;
    for (j=1;j<=nfut;j++) {
        sum = 0.0;
        for (k=1;k<=npoles;k++)
            sum += d[k] * reg[k];
        for (k=npoles;k>=2;k--)
            reg[k] = reg[k-1];
        reg[1] = sum;
        future[ii] = (float) sum;
        ii += j2;
    }
    free_dvector(reg, 1);
}

