

#include <stdio.h>
#include "complex.h"
#include "mathtool.h"

/*
c----------------------------------------------------------------------------
c ampfit.f   Calculate the amplitudes for a Lorentzian model of
c            nmr spectroscopy data.
c
c V
c University of Illinois at Urbana-Champaign
c
c Notes:    dimension of zwork should be > (np+ncomp)*ncomp
c History:
c
c $Id: ampfit.c,v 1.1 1998/05/18 09:52:47 kuik Exp $
c----------------------------------------------------------------------------
c
*/

int ampfit(dcomplex *fid, int np, int n0, int ncomp,
           dcomplex *zfreq, dcomplex *zamp, dcomplex *zwork)
{
    int info,iptr,index,k,i,j,ic,jc;
    
    if (np <= 0 || ncomp <= 0) 
        return 0;
    index = 0;
    for (jc=0;jc<ncomp;jc++) {
        for (ic=0;ic<np;ic++) {
            zwork[index] = cdexp(DCmul_d(-(ic+n0),zfreq[jc]) );
            index++;              
        }
    }

    iptr = np * ncomp;
    k=0;
    for (j=0;j<ncomp;j++) {
        for (i=0;i<=j;i++) {
            zwork[k+iptr] = zdotc(np,zwork+(i*np),1,zwork+(j*np),1);
            k++;
        }
        zamp[j] = zdotc(np, zwork+(j*np),1,fid,1);
    }

    printf("index=%d k=%d ncomp=%d np=%d\n", index,k,ncomp,np);
for (i=0;i<ncomp;i++)
    printf("ampfit:%4d   %14.3f, %14.3f\n",i,zamp[i].r,zamp[i].i);

    info = 0;
    zppfa(zwork+iptr,ncomp,&info);
    if (info != 0) 
        return 1;
    zppsl(zwork+iptr,ncomp,zamp);
    for (ic=0;ic<ncomp;ic++)
        zamp[ic] = Cmul_d(zamp[ic], cdexp(Complex_d(0.0, zfreq[ic].i * n0)));

for (i=0;i<ncomp;i++)
    printf("ampfit:%4d   %14.3f, %14.3f\n",i,zamp[i].r,zamp[i].i);

    return 0;
}



