
#include <stdio.h>
#include <math.h>
#include "complex.h"
#include "mathtool.h"

/*
c----------------------------------------------------------------------------
c  2. hsvd     State-space LP estimation
c
c V
c University of Illinois at Urbana-Champaign
c
c History:
c           - Completed V1.0 July 1994
c
c Errors:
c           - 1 SVD failed
c           - 2 Parameter matrix calculation failed
c           - 3 Eigenvalue computation failed
c
c $Id: hsvd.c,v 1.1 1998/05/18 09:53:08 kuik Exp $
c----------------------------------------------------------------------------
c
       hsvd - state space linear prediction (LP) estimation

SYNOPSIS
       -hsvd np noff ncomp [ mxlw: mxlw ] [ minlw: minlw ]

DESCRIPTION
       The -hsvd command uses the state-space method for LP esti-
       mation, known as HSVD in the NMR literature.  This can  be
       used  to  estimate  NMR  spectral parameters from measured
       free-induction decay (FID) data.  A total  of  np  points,
       beginning  with an offset of noff points, are used to cal-
       culate the spectral linewidths, frequencies, and  intensi-
       ties  from  the  data  in  the working register.  The HSVD
       parameters are returned  in  the  working  register.   The
       arguments for -hsvd are as follows:

       np -      number of samples (points) to use in estimation

       noff -    use  samples  starting  at  this offset into the
                 leading dimension of data

       ncomp -   number of spectral components to fit to data

Two optional parameters,
       mxlw and mnlw allow the user to eliminate  spectral  peaks
       which are too wide or too narrow:

       mxlw -    discard peaks with linewidths greater than mxlw

       mnlw -    discard peaks with linewidths less than mnlw

When the working register contains multi-dimensional data, it is
       assumed  that  the  data consists of multiple FID's, where
       the leading dimension is the FID time dimension.  The HSVD
       algorithm is performed on each FID.

EXAMPLES
       1.     To fit 7 components to the data in the working reg-
              ister using 32 points  beginning  with  the  second
              point in each FID, V> -hsvd 32 1 7

       2.     To  fit  10  components  to the data in the working
              register using the first 48 points in each FID, and
              to discard all peaks whose linewidths are less than
              0.01  or  greater  than  0.9,  V>  -hsvd  48  0  10
              mnlw:0.01 mxlw:0.9

RESTRICTIONS
       -hsvd works only on COMPLEX data.  The number of points np
       must be greater than or  equal  to  twice  the  number  of

V Release 3.00           26 February 1997                       1

HSVD(1)                  V User's Manual                  HSVD(1)

       components, ncomp.

REFERENCES
              S.  Y.  Kung,  K.  S.  Arun, and D. V. Bhaskar Rao,
              "State-space and singular-value decomposition-based
              approximation  methods  for  the harmonic retrieval
              problem," J. Opt. Soc. Am., vol. 73,  no.  12,  pp.
              1799-1811, December 1983.

*/

int hsvd(dcomplex *fid, int np, int *ncomp, double *zwork, int pflag, int *err)
{
    dcomplex z, *p1, *p2, *p3, *p4, *p5, *p6, *p7;
    double   rr, *dp1, *dp2, *dp3, *dp4, *dp5, *dp6, *dp7;
    int      nc,nr,ns,i,j,job,iptr1,iptr2,iptr3,iptr4,iptr5,iptr6,iptr7;
    int      i1,i2,is1,is2,ioff,info;


    nc    = np/2;
    nr    = np-nc;
    iptr1 = 0;
    iptr2 = iptr1+2*nr*nc;		/* storage for a	*/
    iptr3 = iptr2+2*nr*nr;		/* storage for u	*/
    iptr4 = iptr3+2*nc*nc;		/* storage for v	*/
    iptr5 = iptr4+2*max(nc,nr);		/* storage for s	*/
    iptr6 = iptr5+2*max(nc,nr);		/* storage for buffer	*/
    iptr7 = iptr6+2*max(nc,nr);		/* storage for work	*/

    dp1 = zwork+iptr1;
    dp2 = zwork+iptr2;
    dp3 = zwork+iptr3;
    dp4 = zwork+iptr4;
    dp5 = zwork+iptr5;
    dp6 = zwork+iptr6;
    dp7 = zwork+iptr7;
    p1 = (dcomplex*) dp1;
    p2 = (dcomplex*) dp2;
    p3 = (dcomplex*) dp3;
    p4 = (dcomplex*) dp4;
    p5 = (dcomplex*) dp5;
    p6 = (dcomplex*) dp6;
    p7 = (dcomplex*) dp7;
    for (j=0;j<nc;j++)
        zcopy(nr,fid+j,1,p1+j*nr,1);

    job=1;
    zsvdc(p1,nr,nr,nc,p4,p5,p2,nr,p3,nc,p6,job,&info);
    if (info != 0) {
	*err  = 1;
        return 1;
    }

    ns= *ncomp;
    zcopy(ns,p3+(nc-1),nc,p6,1);
    rr = dznrm2(ns,p6,1);
    rr = 1.0-rr*rr;
    if (rr == 0) {        
        *err = 2;
        return 1;
    }
    for (i=0;i<ns;i++) {
        zcopy(nc-1,p3+i*nc,1,p4,1);
        for (j=0;j<ns;j++) {
	    z = zdotc(nc-1,p4,1,p3+j*nc+1,1);
	    dp2[2*i+j*2*ns]    = z.r;
            dp2[2*i+j*2*ns+1]  = z.i;
        }
    }

    for (j=0;j<2*ns;j+=2)
        dp6[j+1] = -dp6[j+1];

    for (j=0;j<ns;j++) {
        z=zdotc(ns,p6,1,p2+j*ns,1);
        dp5[2*j]   = z.r;
        dp5[2*j+1] = z.i;
    }
    rr=1.0/rr;
    dscal(2*ns,rr,dp6,1);
    for (j=0;j<ns;j++) {
        zcopy(ns,p6,1,p3+j*ns,1);
        z = Complex_d(dp5[2*j],dp5[2*j+1]);
        zscal(ns,z,p3+j*ns,1);
    }

    ioff=ns*ns;
    for (j=0;j<ns;j++) {
        for (i=0;i<ns;i++) {
            i1=2*i;
            dp1[i+j*ns]      = dp2[i1+j*2*ns]+
                               dp3[i1+j*2*ns];
            dp1[i+ioff+j*ns] = dp2[i1+1+j*2*ns]+
                               dp3[i1+1+j*2*ns];
        }
    }
    /*
     *     calculating the eigenvalues:
     *   -------------------------------
     */
    cbal(ns,ns,dp1,dp1+(ioff),&is1,&is2,dp5);
    corth(ns,ns,is1,is2,dp1,dp1+(ioff),dp5,dp6);
    comqr(ns,ns,is1,is2,dp1,dp1+(ioff),dp5,dp6,&info);
    if (info != 0) {
        *err =3;
        return 1;
    }

    i1=-1;
    for (i=0;i< (*ncomp);i++) {
        z=Complex_d(dp5[i],dp6[i]);
	if (Cabs_d(z) > 0) {
	    i1++;
	    z = cdlog(z); /* Complex_d(log(Cabs_d(z)), atan2(z.i, z.r)); */
	    dp1[2*i1]   = -z.r;
	    dp1[2*i1+1] = z.i;
        }
    }                   
    *ncomp=i1+1;
    /*
     *  sorting based on imaginary part
     */
    zsortip(p1, *ncomp);       

    if (pflag == 0) {
	i2=0;
	for (i=0;i<(*ncomp);i++) {
	    i1=2*i;
	    if (dp1[i1] > 0.0) {
	        dp1[2*i2]   = dp1[i1];
	        dp1[2*i2+1] = dp1[i1+1];
	        i2 += 1;
	    }
        }
        *ncomp=i2;
    }
for (i=0;i<*ncomp;i++)
    printf("%4d   %14.3f, %14.3f\n",i,p1[i].r,p1[i].i);

    if (*ncomp <= 0) {
        *err=3;
        return 1;
    }
    return 0;
}




