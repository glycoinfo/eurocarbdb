/*
       fidhsvd - HSVD linear prediction algorithm for 1D datasets

SYNOPSIS
       fidhsvd [-r] [-m] [-n] [-v] [-x] [-iinputfile]  [-ooutput-
       file] [-porder] [-ttolerance] [-gpoints] [-soutputpoints]

DESCRIPTION
       FIDHSVD  uses  the  HSVD  linear  prediction  algorithm to
       extend 1D free induction decays  in  an  FELIX  compatible
       data  file.  Signal poles that lie outside the unit circle
       in the complex plane are reflected inside the unit  circle
       to  insure that all components of the interferogram decay.
       The program assumes that the data file is complex; if  the
       data  are  real  then the data should be stored as complex
       data with the imaginary part set to zero.

       The program is useful for analyzing individual columns  of
       a  nD  data set to optimize the values of order and toler-
       ance prior to running MATHSVD.  Individual columns can  be
       read  into FELIX using the "loa" command and written as 1D
       data files using the "wr" command

OPTIONS
       -r     input data is real, i.e.  complex  data  with  zero
              imaginary  part.  The  program  will check that all
              components of the free  induction  decay  occur  in
              complex  conjugate  pairs. After linear prediction,
              the imaginary part  of  any  predicted  data  point
              should  be  near  zero. The "red" command should be
              used in FTNMR prior to transformation.

       -m     use negative  time  extension  of  the  data.  This
              option  assumes that the data was acquired with the
              initial t1 time point equal to zero.  Any  signifi-
              cant  time  decay  in  the  interferogram should be
              removed using an exponential multiplication with  a
              negative line broadening before using this option

       -n     use  negative  time  extension  of  the  data. This
              option assumes that the data was acquired with  the
              initial  t1 time point equal to one-half the incre-
              ment. Any significant time decay in the  interfero-
              gram  should be removed using an exponential multi-
              plication with a negative  line  broadening  before
              using this option

       -v     verbose  mode.  The  values of the singular values,
              complex signal poles,  complex  signal  amplitudes,
              frequencies,   damping  constants,  amplitudes  and
              phases will be output to the standard output.

       -x     extrapolate the FID (i.e. the first  points  points
              of   the   outputfile  will  be  identical  to  the
              inputfile.  If this flag is omitted, the outputfile
              will contain completely synthetic data.

       -iinputfile
              name  of the file to be processed (including exten-
              sion)

       -ooutputfile
              name of the file  to  contain  the  predicted  data
              (including extension)

       -gpoints
              number  of  points in the FID to use in calculating
              the linear prediction coefficients.

       -soutputpoints
              number of points to be writen to the outputfile.

       -porder
              maximum number of signal poles to  calculate.  This
              number must be at least equal to the number of sig-
              nals in the FID for complex data (double the number
              of  signals  for  real  data), and not greater than
              one-half the value input for the -g flag. A  larger
              value  allows  better modelling of noise; typically
              one-third of the total number of points is  a  good
              compromise.

       -ttolerance
              only  those  singular values greater than tolerance
              are treated as significant, all other singular val-
              ues are ignored.  Ideally, tolerance is set to dis-
              criminate between signal and  noise  components  of
              the interferogram; in practice, a minimum value may
              be preferable to avoid discriminating against small
              signals.
*/

#include <stdio.h>
#include <stdlib.h>
#include <assert.h>
#include <math.h>
#include "mathtool.h"
#include "nmrtool.h"


fcomplex cexp(fcomplex z)
{
    float expx;
    fcomplex c;

    expx = exp(z.r);
    c.r = expx * cos(z.i);
    c.i = expx * sin(z.i);
    return c;
}

fcomplex clog(fcomplex z)
{
    fcomplex c;
    c.r = log(Cabs(z));
    c.i = atan2(z.i, z.r);
    return c;
}

#ifdef DOUBLEPRECISION
#define FLOAT 		double
#define COMPLEX 	dcomplex
#define CGESVD	 	zgesvd_
#define CGELSS 		zgelss_
#define CGEMM		zgemm_
#define CGEEV		zgeev_
#define CSUB		Csub_d
#define CMUL		Cmul_d
#define CDIV		Cdiv_d
#define CADD		Cadd_d
#define CPOW		Cpow_d
#define CCOMPLEX	Complex_d
#define CONJG		Conjg_d
#define CABS		Cabs_d
#else
#define FLOAT 		float
#define COMPLEX 	fcomplex
#define CGESVD	 	cgesvd
#define CGELSS 		cgelss
#define CGEMM		cgemm
#define CGEEV		cgeev
#define CSUB		Csub
#define CMUL		Cmul
#define CDIV		Cdiv
#define CADD		Cadd
#define CPOW		Cpow
#define CCOMPLEX	Complex
#define CONJG		Conjg
#define CABS		Cabs
#endif



/*
 * CLAPACK routines
 *
int cgesvd_(char *jobu, char *jobvt, int *m, int *n, 
	fcomplex *a, int *lda, float *s, fcomplex *u, int *ldu, 
        fcomplex *vt, int *ldvt, fcomplex *work, int *lwork, float *rwork, 
	int *info);
int cgelss_(int *m, int *n, int *nrhs, fcomplex *a, int *lda, 
        fcomplex *b, int *ldb, float *s, float *rcond, 
	int *rank, fcomplex *work, int *lwork, float *rwork, int *info);
int cgemm_(char *transa, char *transb, int *m, int *
	n, int *k, fcomplex *alpha, fcomplex *a, int *lda, fcomplex *b, 
	int *ldb, fcomplex *beta, fcomplex *c, int *ldc);
int cgeev_(char *jobvl, char *jobvr, int *n, fcomplex *a, 
	int *lda, fcomplex *w, fcomplex *vl, int *ldvl, fcomplex *vr, 
	int *ldvr, fcomplex *work, int *lwork, float *rwork, int *info);

int zgesvd_(char *jobu, char *jobvt, int *m, int *n, 
	dcomplex *a, int *lda, double *s, dcomplex *u, int *ldu, 
        dcomplex *vt, int *ldvt, dcomplex *work, int *lwork, double *rwork, 
	int *info);
int zgelss_(int *m, int *n, int *nrhs, dcomplex *a, int *lda, 
        dcomplex *b, int *ldb, double *s, double *rcond, 
	int *rank, dcomplex *work, int *lwork, double *rwork, int *info);
int zgemm_(char *transa, char *transb, int *m, int *
	n, int *k, dcomplex *alpha, dcomplex *a, int *lda, dcomplex *b, 
	int *ldb, dcomplex *beta, dcomplex *c, int *ldc);
int zgeev_(char *jobvl, char *jobvr, int *n, dcomplex *a, 
	int *lda, dcomplex *w, dcomplex *vl, int *ldvl, dcomplex *vr, 
	int *ldvr, dcomplex *work, int *lwork, double *rwork, int *info);
*/
static void indexx(int n, FLOAT *arr, int *indx);
static void hsvd_(COMPLEX  *sig, int n, int p, FLOAT *sing, 
                 COMPLEX  *zpoles, COMPLEX  *fcoef, int *krank,
                 FLOAT toler, int type, int rfl, int verb, 
                 int backward);
static void predic(COMPLEX  *sig, int n, int is, COMPLEX  *fpoles, 
            COMPLEX  *fcoef, int iterm);


#define max(a,b)        (((a) > (b)) ? (a) : (b))
#define min(a,b)        (((a) < (b)) ? (a) : (b))
/* 
 *                                           ver 2.0 2Jan92 AGP
 *
 *     Program reads complex 1-D raw NMR data sets and extrapolates
 *     the data using HSVD linear prediction.  For real data, the
 *     imaginary part of the complex signal should be zero. 
 *     The output is outsiz complex points, if the input data 
 *     was real, the imaginary part of
 *     the output file should be close to zero.
 *
 order  = maximum number of signal poles to  calculate.
 points = number  of  points in the FID to use in calculating
              the linear prediction coefficients.
 outsiz = The output is outsiz complex points.
       parameter (maxF2=2048,maxOR=512)

*/
void fidhsvd(int imode, int fixroots, float rdata[], float idata[], 
             int icmplx, int points, float tol, int nfut, int nfut2, 
             int order, int icomplx, int replace)
{
    int      i,nexpf,nexpf2,ipout, gp2, goodpts, itmp, maxF2, maxOR, outsiz;
    int      eflag,cdatf,negfl,vflag,rflct,megfl,backward=0;
    COMPLEX  *fpoles, *fcoef, *fid1, *fid2;
    FLOAT    *sinval, toler;
    
    vflag  = 0;
    negfl  = 0;
    megfl  = 0;
    eflag  = !replace;
    rflct  = fixroots;
    cdatf  = icomplx; /* complex flag = TRUE */
    toler  = (FLOAT) tol;
    outsiz = points;
    goodpts= points;
    maxF2  = outsiz*2;
    maxOR  = order*2;

    fpoles = (COMPLEX *) malloc(sizeof(COMPLEX )*maxOR);
    assert(fpoles);
    fcoef  = (COMPLEX *) malloc(sizeof(COMPLEX )*maxOR);
    assert(fcoef);
    sinval = (FLOAT*) malloc(sizeof(FLOAT)*maxOR);
    assert(sinval);
    fid2   =  (COMPLEX *) malloc(sizeof(COMPLEX )*2*maxF2);
    assert(fid2);
    fid1   = fid2 + maxF2;
    if (icmplx) {
        /* Backward prediction */
        if (imode==PREDICT_BACKWARD) {
            backward=TRUE;    
            for (i=0;i<points;i++) {
                fid1[i].r  = (FLOAT) rdata[points-1-i];
                fid1[i].i  = (FLOAT) -idata[points-1-i];
            }
    
        }
        else {
            for (i=0;i<points;i++) {
                fid1[i].r  = (FLOAT) rdata[i];
                fid1[i].i  = (FLOAT) idata[i];
            }
        }
    }
    else {
        /* Backward prediction */
        if (imode==PREDICT_BACKWARD) {
            backward=TRUE;    
            for (i=0;i<points;i++) {
                fid1[i].r  = (FLOAT) rdata[points-1-i];
                fid1[i].i  = 0.0;
            }
    
        }
        else {
            for (i=0;i<points;i++) {
                fid1[i].r  = (FLOAT) rdata[i];
                fid1[i].i  = 0.0;
            }
        }
    }
    itmp  = maxF2;
    gp2   = goodpts;
    ipout = outsiz;
#ifdef NOTINUSE
    /*
     *  use negative  time  extension  of  the  data.  This
     *  option  assumes that the data was acquired with the
     *  initial t1 time point equal to zero.  Any  signifi-
     *  cant  time  decay  in  the  interferogram should be
     *  removed using an exponential multiplication with  a
     *  negative line broadening before using this option
     */
    if (megfl) {
        gp2   = 2*goodpts-1;
        ipout = outsiz+goodpts-1;
        itmp  = maxF2+1-goodpts;
        for (i=1;i<goodpts;i++)
            fid2[maxF2-i] = CONJG(fid1[i]);
    }
    /*
     *  use  negative  time  extension  of  the  data. This
     *  option assumes that the data was acquired with  the
     *  initial  t1 time point equal to one-half the incre-
     *  ment. Any significant time decay in the  interfero-
     *  gram  should be removed using an exponential multi-
     *  plication with a negative  line  broadening  before
     *  using this option
     */
    if (negfl) {
        gp2   = 2*goodpts;
        ipout = outsiz+goodpts;
        itmp  = maxF2-goodpts;
        for (i=0;i<goodpts;i++)
            fid2[maxF2-1-i] = CONJG(fid1[i]);
    }
#endif
    if (imode==PREDICT_GAP) {
        hsvd_(fid2 + itmp, nfut-1, order, sinval, fpoles, fcoef, &nexpf,
               toler, cdatf, rflct, vflag, FALSE);

        if (icmplx) {
            for (i=0;i<points-nfut2;i++) {
                fid1[i+nfut2].r  = (FLOAT) rdata[points-1-i];
                fid1[i+nfut2].i  = (FLOAT) -idata[points-1-i];
            }
        }
        else {
            for (i=0;i<points-nfut2;i++) {
                fid1[i+nfut2].r  = (FLOAT) rdata[points-1-i];
                fid1[i+nfut2].i  = 0.0;
            }
        }
        hsvd_(fid2 + itmp + nfut2, points - nfut2, 
              order, sinval, fpoles+nexpf, fcoef+nexpf, &nexpf2,
              toler, cdatf, rflct, vflag, TRUE);
    }
    else {
        hsvd_(fid2 + itmp, gp2, order, sinval, fpoles, fcoef, &nexpf,
               toler, cdatf, rflct, vflag, backward);
    }
    /*
     *  extrapolate the FID (i.e. the first  points  points
     *  of   the   outputfile  will  be  identical  to  the
     *  inputfile.  If this flag is omitted, the outputfile
     *  will contain completely synthetic data.
     */
    if (!eflag) 
        gp2 = 0;

    if (imode==PREDICT_GAP) {
        if (icmplx) {
            if (nexpf2 > 0) {
                predic(fid2+itmp+nfut-1, 0, points-nfut+1, 
                       fpoles+nexpf, fcoef+nexpf, nexpf2);
                for (i=0;i<nfut2-nfut+1;i++) {
                    fid1[i+nfut-1].r  = (FLOAT) fid1[points-1-i].r;
                    fid1[i+nfut-1].i  = (FLOAT) -fid1[points-1-i].i;
                }
                for (i=nfut2;i<points;i++) {
                    fid1[i].r  = (FLOAT) rdata[i];
                    fid1[i].i  = (FLOAT) idata[i];
                }
                for (i=nfut-1;i<nfut2;i++) {
                    rdata[i] = (float) fid1[i].r;
                    idata[i] = (float) fid1[i].i;
                }
            }
            if (nexpf > 0) {
                predic(fid2+itmp, nfut-1, nfut2, fpoles, fcoef, nexpf);
                for (i=nfut-1;i<nfut2;i++) {
                    rdata[i] += (float) fid1[i].r;
                    idata[i] += (float) fid1[i].i;
                }
            }
            if (nexpf > 0 && nexpf2 > 0) {
                for (i=nfut-1;i<nfut2;i++) {
                    rdata[i] *= 0.5;
                    idata[i] *= 0.5;
                }
            }
        }
        else {
            if (nexpf2 > 0) {
                predic(fid2+itmp+nfut-1, 0, points-nfut+1, 
                       fpoles+nexpf, fcoef+nexpf, nexpf2);
                for (i=0;i<nfut2-nfut+1;i++) {
                    fid1[i+nfut-1].r  = (FLOAT) fid1[points-1-i].r;
                }
                for (i=nfut2;i<points;i++) {
                    fid1[i].r  = (FLOAT) rdata[i];
                }
                for (i=nfut-1;i<nfut2;i++) {
                    rdata[i] = (float) fid1[i].r;
                }
            }
            if (nexpf > 0) {
                predic(fid2+itmp, nfut-1, nfut2, fpoles, fcoef, nexpf);
                for (i=nfut-1;i<nfut2;i++) {
                    rdata[i] += (float) fid1[i].r;
                }
            }
            if (nexpf > 0 && nexpf2 > 0) {
                for (i=nfut-1;i<nfut2;i++) {
                    rdata[i] *= 0.5;
                }
            }
        }
    }
    else {
        ipout = outsiz+nfut;
        predic(fid2 + itmp, gp2, ipout, fpoles, fcoef, nexpf);

        if (icmplx) {
            if (imode==PREDICT_BACKWARD) {
                rdata -= nfut;
                idata -= nfut;
                for (i=0;i<ipout-gp2;i++) {
                    rdata[i] = (float) fid1[ipout-1-i].r;
                    idata[i] = (float) -fid1[ipout-1-i].i;
                }
            }
            else if (imode==PREDICT_FORWARD) {
                for (i=gp2;i<ipout;i++) {
                    rdata[i] = (float) fid1[i].r;
                    idata[i] = (float) fid1[i].i;
                }
            }
        }
        else {
            if (imode==PREDICT_BACKWARD) {
                rdata -= nfut;
                for (i=0;i<ipout-gp2;i++) {
                    rdata[i] = (float) fid1[ipout-1-i].r;
                }
            }
            else if (imode==PREDICT_FORWARD) {
                for (i=gp2;i<ipout;i++) {
                    rdata[i] = (float) fid1[i].r;
                }
            }
        }
    }
    free(fpoles);
    free(fcoef);
    free(sinval);
    free(fid2);

}


/*
 *     maxROW=maximum number of data points
 *     maxCOL=maximum order
 */
#define MAXROW	1024
#define MAXCOL	512
/*
 *     flim is minimum frequency difference for conjugate pairs
 *     of signal poles when data is real
 */
#define FLIM	0.0001
/*
 *     machine precision eps determined from machar program
 *     Convex C2
 *     parameter (eps=7.45e-9,svdtol=10.0*eps)
 *     IRIS Indigo
 *     parameter (eps=1.19e-7,svdtol=10.0*eps)
 */
#define EPS	(1.19e-7)
#define SVDTOL	(10.0*EPS)

/*
 *
 *                                          ver 3.0 1Jul93  AGP
 *
 *     HSVD algorithm [Barkhuijsen et al JMR 73, 553 (1987)]
 *     Equation numbers refer to the original paper
 *
 *     parameters
 *     sig 	= complex fid (zero imaginary part for real data)
 *     n 	= number of points in fid
 *     p 	= model order to use
 *     sing 	= array of singular values
 *     zpoles 	= complex signal poles
 *     fcoef 	= complex signal amplitudes
 *     krank 	= number of poles
 *     toler 	= minimum significant singular value
 *     type 	= logical flag for real/complex data
 *     rfl 	= logical flag for reflection of poles
 *     verb 	= logical flag for verbose mode
 */
static void hsvd_(COMPLEX  *sig, int n, int p, FLOAT *sing, 
                 COMPLEX  *zpoles, COMPLEX  *fcoef, int *krank,
                 FLOAT toler, int type, int rfl, int verb, 
                 int backward)
{
    int m,i,j,itemp,nrhs;
    int maxROW,maxCOL,lwork;
    int ierr, *pind;
    char transa,transb,jobu,jobvt,jobvl,jobvr;
    FLOAT *freq, *ssing,*rwork;
    FLOAT flim, eps, svdtol, twopi;
    COMPLEX  alpha,beta,denom,*smat,*umat,*zmat,*ftemp,*work;


    flim   = FLIM;
    eps    = EPS;
    svdtol = SVDTOL;
    maxROW = max(n,MAXROW);
    maxCOL = max(p,MAXCOL);
    lwork  = 30*maxROW;
    pind   = (int*) malloc(sizeof(int)*maxCOL);
    assert(pind);
    rwork  = (FLOAT*) malloc(sizeof(FLOAT)*lwork);
    assert(rwork);
    freq   = (FLOAT*) malloc(sizeof(FLOAT)*maxCOL);
    assert(freq);
    ssing  = (FLOAT*) malloc(sizeof(FLOAT)*maxCOL);
    assert(ssing);
#define SMAT(I,J) smat[(I) + (J) * (maxROW)]
    smat   = (COMPLEX *) malloc(sizeof(COMPLEX )*maxROW*maxCOL);
    assert(smat);
#define UMAT(I,J) umat[(I) + (J) * (maxROW)]
    umat   = (COMPLEX *) malloc(sizeof(COMPLEX )*maxROW*maxCOL);
    assert(umat);
#define ZMAT(I,J) zmat[(I) + (J) * (maxCOL)]
    zmat   = (COMPLEX *) malloc(sizeof(COMPLEX )*maxCOL*maxCOL);
    assert(zmat);
    ftemp  = (COMPLEX *) malloc(sizeof(COMPLEX )*maxROW);
    assert(ftemp);
    work   = (COMPLEX *) malloc(sizeof(COMPLEX )*lwork);
    assert(work);

    /*
     *     set up data matrix [Eq. 3]
     */
    for (i=0;i<p;i++)
        for (j=0;j<n-p+1;j++)
            SMAT(j,i) = sig[i+j];

    /*
     *     calculate Singular Value Decomposition [Eq. 7]
     *     calculate left singular vectors
     */
    jobu  = 'O';
    /*
     *     do not calculate right singular vectors
     */
    jobvt = 'N';
    ierr  = 0;
    m     = n-p+1;
    /*
     *   CGESVD computes the singular value decomposition (SVD) of a complex   
     *   M-by-N matrix A, optionally computing the left and/or right singular 
     *   vectors.   
     */
    CGESVD(&jobu,&jobvt,m,p,smat,maxROW,sing,smat,maxROW,
            smat,maxROW,work,lwork,rwork,&ierr);
    if (ierr) 
        fprintf(stderr,"Error in cgesvd: %d\n",ierr);

    if ((int)(work[0].r) > lwork) {
        lwork  = (int)work[0].r;
        rwork  = (FLOAT*) realloc(rwork, sizeof(FLOAT)*lwork);
        assert(rwork);
        work   = (COMPLEX *) realloc(work,sizeof(COMPLEX )*lwork);
        assert(work);
    }
    /*
     *     keep only singular values greater than toler
     */
    *krank = 0;
    itemp  = min(m,p);
    toler *= sing[0];
    for (i=0;i<itemp;i++) 
        if (sing[i] >= toler) 
            *krank = i+1;

    /*
     *     calculate z' [Eq. 12] 
     */
    for (i=1;i<n-p+1;i++)
        for (j=0;j<(*krank);j++)
            UMAT(i-1,j) = SMAT(i,j);

    /*
     *     multiply complex matrices using blas3 routine
     */
    transa = 'c';
    transb = 'n';
    alpha  = CCOMPLEX(1.0, 0.0);
    beta   = CCOMPLEX(0.0, 0.0);
    m      = n - p;
    /*
     *  CGEMM  performs one of the matrix-matrix operations   
     *        C := alpha*op( A )*op( B ) + beta*C,   
     *        TRANSA = 'C' or 'c',  op( A ) = conjg( A' ).   
     *        TRANSB = 'N' or 'n',  op( B ) = B.   
     */
    CGEMM(&transa,&transb,*krank,*krank,m,alpha,smat,maxROW,
                 umat,maxROW,beta,zmat,maxCOL);

    denom = CCOMPLEX(1.0, 0.0);
    for (i=0;i<(*krank);i++)
        denom = CSUB(denom, CMUL(CONJG(SMAT(n-p,i)), SMAT(n-p,i)));

    for (i=0;i<(*krank);i++) {
        for (j=0;j<(*krank);j++) {
            UMAT(i,j) = CMUL(CONJG(SMAT(n-p,i)),SMAT(n-p,j));
            UMAT(i,j) = CDIV(UMAT(i,j),denom);
        }
        UMAT(i,i).r += 1.0;
    }

    transa = 'n';
    transb = 'n';
    CGEMM(&transa,&transb,*krank,*krank,*krank,alpha,umat,maxROW,
                 zmat,maxCOL,beta,smat,maxROW);
    /*
     *     diagonalize umat to find complex poles
     *     do not calculate any eigenvectors
     */
    jobvl = 'N';
    jobvr = 'N';
    ierr  = 0;
    /*
     *  CGEEV computes for an N-by-N complex nonsymmetric matrix A, the   
     *  eigenvalues and, optionally, the left and/or right eigenvectors.   
     */
    CGEEV(&jobvl,&jobvr,*krank,smat,maxROW,zpoles,
                 zmat,maxCOL,zmat,maxCOL,work,lwork,rwork,&ierr);
    if (ierr) {
        fprintf(stderr, "error in diagonalization\n");
        goto L990;
    }
    if ((int)(work[0].r) > lwork) {
        lwork  = (int)work[0].r;
        rwork  = (FLOAT*) realloc(rwork, sizeof(FLOAT)*lwork);
        assert(rwork);
        work   = (COMPLEX *) realloc(work,sizeof(COMPLEX )*lwork);
        assert(work);
    }

    /*
     *     reflect roots that lie outside unit circle
     */
{
int ll=0;
    if (rfl && backward)
        ll=0;
    else
        ll = *krank;

    if (rfl) {
        COMPLEX one = CCOMPLEX(1.0, 0.0);
        for (j=0;j<(*krank);j++) {
            if (backward) {
                if (CABS(zpoles[j]) > 1.0) 
                    zpoles[ll++] = zpoles[j];
            }
            else {
                if (CABS(zpoles[j]) > 1.0) 
                    zpoles[j] = CONJG(CDIV(one,zpoles[j]));     
            }          
        }
    }
    *krank = ll;
}
    /*
     *     now solve sig=smat*x for the complex amplitudes x 
     *     by least squares
     */
    for (i=0;i<n;i++) {
        ftemp[i] = sig[i];
        for (j=0;j<(*krank);j++) 
            SMAT(i,j) = CPOW(zpoles[j],i);
    }
    /*
     *     use SVD routine to solve linear system of equations
     */
    ierr = 0;
    m    = *krank;
    nrhs = 1;
    /*
     *  CGELSS computes the minimum norm solution to a complex linear   
     *  least squares problem:   
     */
    CGELSS(n,m,nrhs,smat,maxROW,ftemp,maxROW,ssing,svdtol,
                    krank,work,lwork,rwork,&ierr);
    if (ierr) 
        fprintf(stderr,"Error in cgelss: %d\n",ierr);
    for (i=0;i<(*krank);i++)
	fcoef[i] = ftemp[i];

    /*
     *     if real data we should have an even number of singular values
     */
    if (!type) {
        COMPLEX  *ztemp;

        ztemp  = (COMPLEX *) malloc(sizeof(COMPLEX )*maxCOL);
        assert(ztemp);
        for (i=0;i<(*krank);i++)
            freq[i] = fabs(atan2(zpoles[i].i, zpoles[i].r));
        /*
         *        sort using pointer array pind
         */
        indexx(*krank, freq, pind);
        for (i=0;i<(*krank);i++) {
   	    Ccopy(ztemp[i], zpoles[pind[i]]);
	    Ccopy(ftemp[i], fcoef[pind[i]]);
        }
        i = 0;
        j = -1;
        do {
            if (fabs(ztemp[i].r - ztemp[i+1].r) <= flim) {
                j += 2;
                Ccopy(zpoles[j-1], ztemp[i]);
                Ccopy(zpoles[j]  , ztemp[i+1]);
                Ccopy(fcoef[j-1] , ftemp[i]);
                Ccopy(fcoef[j]   , ftemp[i+1]);
                i += 2;
            }
            else
                i++;
        }
        while (i+1 < *krank);
        *krank = j+1;
        free(ztemp);
    }
    /*
     *     if verbose mode, send output to standard output
     */
    if (verb) {
        printf("The %d largest singular values\n", *krank);
        for (i=0;i<(*krank);i++)
            printf("%4d %18f\n", i+1, sing[i]);

        printf("\n");
        printf("                          Poles                   Amplitudes\n");
        for (i=0;i<(*krank);i++)
            printf("%5d  %14.3f +%14.3f i  %14.3f +%14.3f i\n", 
                    i+1, zpoles[i].r, zpoles[i].i, 
                    fcoef[i].r, fcoef[i].i);


        printf("\n");
        printf("Frequencies  Damping factors  Amplitudes       Phases\n");
        twopi=8*atan(1.);
        for (i=0;i<(*krank);i++)
            printf("%8d %16g %14g %12g %12g\n",
                i+1, 
                atan2(zpoles[i].i, zpoles[i].r)/twopi,
                log10(CABS(zpoles[i])),
                CABS(fcoef[i]), 
                atan2(fcoef[i].i,fcoef[i].r));
    }


L990:
    free(pind);
    free(rwork);
    free(freq);
    free(ssing);
    free(smat);
    free(umat);
    free(zmat);
    free(ftemp);
    free(work);
}

/*
 *     This subroutine uses the hsvd poles and amplitudes to calculate the
 *     length of a time domain signal to an arbitrary length
 *
 *     Model parameters
 *     sig    = calculated complex signal is points long
 *     n      = number of goodpts in fid (0 for complete replacement of fid) 
 *     is     = output file size
 *     fpoles = forward hsvd poles
 *     fcoef  = forward hsvd coefficients
 *     iterm  = number of terms from hsvd
 */

static void predic(COMPLEX  *sig, int n, int is, COMPLEX  *fpoles, 
            COMPLEX  *fcoef, int iterm)
{
    int i,k;
    COMPLEX  ctemp;

    for (i=n;i<is;i++) {
	ctemp = CCOMPLEX(0.0, 0.0);
        if (iterm > 0) {
	    for (k=0;k<iterm;k++)
	        ctemp = CADD(ctemp, CMUL(fcoef[k], CPOW(fpoles[k],i))); 
        }
	Ccopy(sig[i], ctemp);
    }
}




#define NSTACK	50
#define M	7

static void indexx(int n, FLOAT *arr, int *indx)
{
    int i,indxt,ir,itemp,j,jstack,k,l,*istack,nstack;
    FLOAT a;
    
    nstack = NSTACK;
    istack = (int*) malloc(sizeof(int)*nstack);
    assert(istack);

    /*
     * Arrays start at 1
     */
    istack--;
    arr--;
    indx--;
    
    for (j=1;j<=n;j++)
        indx[j] = j;

    jstack = 0;
    l      = 1;
    ir     = n;
    while (1) {
        if (ir-l < M) {
            for (j=l+1;j<=ir;j++) {
                indxt = indx[j];
                a     = arr[indxt];
                for (i=j-1;i>=1;i--) {
                    if (arr[indx[i]] <= a)
                        break;
                    indx[i+1] = indx[i];
                }
                indx[i+1] = indxt;
            }
            if (jstack == 0) {
                istack++;
                free(istack);
                return;
            }
            ir      = istack[jstack];
            l       = istack[jstack-1];
            jstack -= 2;
        }
        else {
            k         = (l+ir)/2;
            itemp     = indx[k];
            indx[k]   = indx[l+1];
            indx[l+1] = itemp;
            if (arr[indx[l+1]] > arr[indx[ir]]) {
                itemp     = indx[l+1];
                indx[l+1] = indx[ir];
                indx[ir]  = itemp;
            }
            if (arr[indx[l]] > arr[indx[ir]]) {
                itemp     = indx[l];
                indx[l]   = indx[ir];
                indx[ir]  = itemp;
            }
            if (arr[indx[l+1]] > arr[indx[l]]) {
                itemp     = indx[l+1];
                indx[l+1] = indx[l];
                indx[l]   = itemp;
            }
            i = l + 1;
            j = ir;
            indxt = indx[l];
            a     = arr[indxt];
            while (1) {
                i++;
                if (arr[indx[i]] < a) 
                    continue;
                do {
                    j--;
                }
                while (arr[indx[j]] > a);
                if (j < i) 
                    break;
                itemp   = indx[i];
                indx[i] = indx[j];
                indx[j] = itemp;
            }
           
            indx[l] = indx[j];
            indx[j] = indxt;
            jstack += 2;
            if (jstack > nstack) {
                nstack *= 2;
                istack++;
                istack = (int*) realloc(istack, sizeof(int)*nstack);
                assert(istack);
                istack--;
            }
            if (ir-i+1 >= j-l) {
                istack[jstack]   = ir;
                istack[jstack-1] = i;
                ir = j-1;
            }
            else {
                istack[jstack]   = j-1;
                istack[jstack-1] = l;
                l = i;
            }
        }
    }
}





