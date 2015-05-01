

#include <math.h>
#include "complex.h"
#include "mathtool.h"


/*

LPSVD(1)                 V User's Manual                 LPSVD(1)

NAME
       lpsvd - Kumaresan-Tufts linear prediction (LP) estimation

SYNOPSIS
       -lpsvd np noff ncomp [ mxlw: mxlw ] [ mnlw: mnlw ]

DESCRIPTION
       The  -lpsvd command uses the Kumaresan-Tufts method for LP
       estimation, known as LPSVD in the NMR literature.  A total
       of np points, beginning with an offset of noff points, are
       used to calculate the  spectral  linewidths,  frequencies,
       and  intensities  from  the  data in the working register.
       The LPSVD parameters are returned in the working register.
       This  can be used to estimate NMR spectral parameters from
       measured FID (free-induction decay) data.

       The arguments for -lpsvd are specfied as follows:

       np -      number of samples (points) to use in estimation

       noff -    use samples starting at  this  offset  into  the
                 leading dimension of data

       ncomp -   number of spectral components to fit to the data

Two optional parameters,
       mxlw and mnlw allow the user to eliminate spectral  compo-
       nents that are too wide or too narrow:

       mxlw -    discard peaks with linewidths greater than mxlw

       mnlw -    discard peaks with linewidths less than mnlw

When the working register contains multi-dimensional data, it is
       assumed  that the leading dimension is the FID time dimen-
       sion, and the LPSVD algorithm is performed on each FID.

EXAMPLES
       1.     To fit (up to) 16 components to  the  data  in  the
              working  register  using  32 data points (beginning
              with the second point in each FID) and an SVD trun-
              cation level of 10, V> -lpsvd 32 1 10

       2.     To  fit  (up  to)  24 components to the data in the
              working register using the first 48 points in  each
              FID using an SVD truncation level of 10 and to dis-
              card all peaks whose linewidths are less than  0.01
              or  greater  than  0.9, V> -lpsvd 48 0 10 mnlw:0.01
              mxlw:0.9

V Release 3.00           26 February 1997                       1

LPSVD(1)                 V User's Manual                 LPSVD(1)

RESTRICTIONS
       The -lpsvd command requires COMPLEX data.

REFERENCES
              R. Kumaresan  and  D.  W.  Tufts,  "Estimating  the
              parameters  of  exponentially  damped sinusoids and
              pole-zero modeling in noise,"  IEEE  Trans.  Acous-
              tics,  Speech, Signal Processing, vol. ASSP-30, no.
              6, pp. 833-840, December 1982.
              H. Barkhuijsen et al., "Application of linear  pre-
              diction and singular value decomposition (LPSVD) to
              determine NMR frequencies and intensities from  the
              FID,"  Magnetic  Resonance in Medicine, vol. 2, pp.
              86-89, 1985.

SEE ALSO
       hsvd, synfid, vpnls

FILES
       v_lpsvd.c

AUTHORS
       Z.-P. Liang, C. P. Hess, H. Jiang,
       J. M. Hanson, D. Everding and G. Gray
       Department of Electrical and Computer Engineering
       University of Illinois at Urbana-Champaign

COPYRIGHT
       Copyright 1994-1997 University of Illinois at Urbana-Cham-
       paign.  All rights reserved.

V Release 3.00           26 February 1997                       2


c----------------------------------------------------------------------------
c  1.  lpsvd      Kumaresan-Tufts LP estimation
c
c  Errors:
c            - 1: SVD failure
c
c $Id: lpsvd.c,v 1.1 1998/05/18 09:53:11 kuik Exp $
c----------------------------------------------------------------------------
c
*/

int lpsvd(dcomplex *fid, int np, int *ncomp, double *dzwork, int pflag, int *err)
{
    int info,iptr1,iptr2,iptr3,iptr4,iptr5,iptr6,iptr7;
    dcomplex *p1,*p2,*p3,*p4,*p5,*p6,*p7;
    dcomplex *zwork;
    int i,j,i1,nc,nr,ns,ns1,job,nroot;

    /*
     *     SVD decomposition:
     *   ---------------------
     */
    nc = max((*ncomp)*4,np/4);
    nc = min(nc,np/2);
    nr = np-nc;

    iptr1 = 0;
    iptr2 = iptr1+nr*nc;		/* storage for a	*/
    iptr3 = iptr2+nr*nr;		/* storage for u	*/
    iptr4 = iptr3+nc*nc;		/* storage for v	*/
    iptr5 = iptr4+max(nc,nr);		/* storage for s	*/
    iptr6 = iptr5+max(nc,nr);		/* storage for buffer	*/
    iptr7 = iptr6+max(nc,nr);		/* storage for work	*/

    zwork = (dcomplex*) dzwork;
    p1 = (dcomplex*) (zwork+iptr1);
    p2 = (dcomplex*) (zwork+iptr2);
    p3 = (dcomplex*) (zwork+iptr3);
    p4 = (dcomplex*) (zwork+iptr4);
    p5 = (dcomplex*) (zwork+iptr5);
    p6 = (dcomplex*) (zwork+iptr6);
    p7 = (dcomplex*) (zwork+iptr7);
    for (j=0;j<nc;j++)
        zcopy(nr,fid+j+1,1,p1+j*nr,1);
    job=21;
    zsvdc(p1,nr,nr,nc,p4,p5,p2,nr,p3,nc,p6,job,&info);
    if (info != 0) {
	*err  = 1;
        return 1;
    }
    ns = *ncomp;
    ns1= ns-1;
    for (i=ns1;i>=0;i--)
        if (Cabs_d(p4[i]) == 0) 
            ns--;

    for (i=0;i<ns;i++) {
    dcomplex dc;
        i1 = i;
        dc = zdotc(nr,p2+i1*nr,1,fid,1);
        p5[i1] = DCmul_d(-1.0,dc);
        p5[i1] = Cdiv_d(p5[i1], p4[i1]);
    }
       
    for (i=0;i<nc;i++) 
        p2[i+1] = zdotu(ns,p5,1,p3+i,nc);
    p2[0] = Complex_d(1.0, 0.0);
    nroot = nc;
    /*       
     *     polynomial rooting:
     *    ----------------------
     */
    zpoly(p2,nroot,p1,&info);
    if (info != 0) {
        *err   = 2;
        return 1;
    }

    if (pflag == 100) {
        i1=0;
	for (i=0;i<nroot;i++) {
            if (Cabs_d(zwork[i]) > 0) {
                dcomplex c = zwork[i];
		zwork[i] = Complex_d(log(Cabs_d(c)), atan2(c.i, c.r));
		zwork[i].r = -zwork[i].r;
            }
            i1++;
        }
	nroot = i1;
	*ncomp = nroot;
    }

    if (pflag == 0) {
        i1=0;
	for (i=0;i<nroot;i++) {
            if (Cabs_d(zwork[i]) > 0) {
                dcomplex c = zwork[i];
		zwork[i] = Complex_d(log(Cabs_d(c)), atan2(c.i, c.r));
		if (zwork[i].r > 0.0) {
		    zwork[i1] = zwork[i];
		    i1++;
		}
            }
        }
	nroot = i1;
	*ncomp = nroot;
    }

    if (pflag == 2) {
        i1=0;
	for (i=0;i<nroot;i++) {
            if (fabs(Cabs_d(zwork[i])-1.0) < 0.025) {
                dcomplex c = zwork[i];
		zwork[i] = Complex_d(log(Cabs_d(c)), atan2(c.i, c.r));
                zwork[i1] = zwork[i];
                i1++;
	    }
        }
	nroot = i1;
	*ncomp = nroot;
    }

    zsortip(zwork, *ncomp);

    if (*ncomp <= 0) 
        return 1;
    return 0;
}
