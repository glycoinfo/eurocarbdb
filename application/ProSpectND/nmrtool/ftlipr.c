
#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include "mathtool.h"
#include "nmrtool.h"

/*
 *
 *    ftlipr   real forward/backward prediction
 */
void ftlipr(int imode, float *data, int ndata, float *future,
            int nfut, int ipoles)
{
    double *cof, dum;
    int npoles;

    data--;
    future--;
    
    npoles = abs(ipoles);
    cof = dvector(1,npoles);

    /*
     * get coefficients
     */
    memcof(data, ndata, npoles, &dum, cof);
    /*
     * fix roots
     */
    if (imode == 1 || imode == 2) 
        fixrts(cof,npoles,imode);
    /*
     * prediction
     */
    bacdic(data,ndata,cof,ipoles,future,nfut);
    free_dvector(cof, 1);
}

