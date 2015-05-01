
#include <math.h>
#include "complex.h"
#include "mathtool.h"

void cgeev(char *jobvl, char *jobvr, int n__, fcomplex *a, 
	   int lda, fcomplex *w, fcomplex *vl, int ldvl, fcomplex *vr, 
	   int ldvr, fcomplex *work, int lwork, float *rwork, int *info)
{
/*  -- LAPACK driver routine (version 2.0) --   
       Univ. of Tennessee, Univ. of California Berkeley, NAG Ltd.,   
       Courant Institute, Argonne National Lab, and Rice University   
       September 30, 1994   


    Purpose   
    =======   

    CGEEV computes for an N-by-N complex nonsymmetric matrix A, the   
    eigenvalues and, optionally, the left and/or right eigenvectors.   

    The right eigenvector v(j) of A satisfies   
                     A * v(j) = lambda(j) * v(j)   
    where lambda(j) is its eigenvalue.   
    The left eigenvector u(j) of A satisfies   
                  u(j)**H * A = lambda(j) * u(j)**H   
    where u(j)**H denotes the conjugate transpose of u(j).   

    The computed eigenvectors are normalized to have Euclidean norm   
    equal to 1 and largest component real.   

    Arguments   
    =========   

    JOBVL   (input) CHARACTER*1   
            = 'N': left eigenvectors of A are not computed;   
            = 'V': left eigenvectors of are computed.   

    JOBVR   (input) CHARACTER*1   
            = 'N': right eigenvectors of A are not computed;   
            = 'V': right eigenvectors of A are computed.   

    N       (input) INTEGER   
            The order of the matrix A. N >= 0.   

    A       (input/output) COMPLEX array, dimension (LDA,N)   
            On entry, the N-by-N matrix A.   
            On exit, A has been overwritten.   

    LDA     (input) INTEGER   
            The leading dimension of the array A.  LDA >= max(1,N).   

    W       (output) COMPLEX array, dimension (N)   
            W contains the computed eigenvalues.   

    VL      (output) COMPLEX array, dimension (LDVL,N)   
            If JOBVL = 'V', the left eigenvectors u(j) are stored one   
            after another in the columns of VL, in the same order   
            as their eigenvalues.   
            If JOBVL = 'N', VL is not referenced.   
            u(j) = VL(:,j), the j-th column of VL.   

    LDVL    (input) INTEGER   
            The leading dimension of the array VL.  LDVL >= 1; if   
            JOBVL = 'V', LDVL >= N.   

    VR      (output) COMPLEX array, dimension (LDVR,N)   
            If JOBVR = 'V', the right eigenvectors v(j) are stored one   
            after another in the columns of VR, in the same order   
            as their eigenvalues.   
            If JOBVR = 'N', VR is not referenced.   
            v(j) = VR(:,j), the j-th column of VR.   

    LDVR    (input) INTEGER   
            The leading dimension of the array VR.  LDVR >= 1; if   
            JOBVR = 'V', LDVR >= N.   

    WORK    (workspace/output) COMPLEX array, dimension (LWORK)   
            On exit, if INFO = 0, WORK(1) returns the optimal LWORK.   

    LWORK   (input) INTEGER   
            The dimension of the array WORK.  LWORK >= max(1,2*N).   
            For good performance, LWORK must generally be larger.   

    RWORK   (workspace) REAL array, dimension (2*N)   

    INFO    (output) INTEGER   
            = 0:  successful exit   
            < 0:  if INFO = -i, the i-th argument had an illegal value.   
            > 0:  if INFO = i, the QR algorithm failed to compute all the 
  
                  eigenvalues, and no eigenvectors have been computed;   
                  elements and i+1:N of W contain eigenvalues which have 
  
                  converged.   

    ===================================================================== 
  


       Test the input arguments   

    
   Parameter adjustments   
       Function Body */
    /* Table of constant values */
    static int c__1 = 1;
    static int c__0 = 0;
    static int c__8 = 8;
    static int c_n1 = -1;
    static int c__4 = 4;
    
    /* System generated locals */
    int  vl_dim1,  vr_dim1,  i__1, 
	    i__2, i__3, i__4;
    float r__1, r__2;
    double d__1;
    fcomplex q__1, q__2;
    /* Local variables */
    static int ibal;
    static char side[1];
    static int maxb;
    static float anrm;
    static int ierr, itau, iwrk, nout, i, k;
    static int scalea;
    static float cscale;
    static int select[1];
    static float bignum;
    static int minwrk, maxwrk;
    static int wantvl;
    static float smlnum;
    static int hswork, irwork;
    static int wantvr;
    static int ihi;
    static float scl;
    static int ilo;
    static float dum[1], eps;
    static fcomplex tmp;



#define W(I) w[(I)-1]
#define WORK(I) work[(I)-1]
#define RWORK(I) rwork[(I)-1]

#define A(I,J) a[(I)-1 + ((J)-1)* ( lda)]
#define VL(I,J) vl[(I)-1 + ((J)-1)* ( ldvl)]
#define VR(I,J) vr[(I)-1 + ((J)-1)* ( ldvr)]

    *info = 0;
    wantvl = lsame(jobvl, "V");
    wantvr = lsame(jobvr, "V");
    if (! wantvl && ! lsame(jobvl, "N")) {
	*info = -1;
    } else if (! wantvr && ! lsame(jobvr, "N")) {
	*info = -2;
    } else if (n__ < 0) {
	*info = -3;
    } else if (lda < max(1,n__)) {
	*info = -5;
    } else if (ldvl < 1 || (wantvl && ldvl < n__)) {
	*info = -8;
    } else if (ldvr < 1 || (wantvr && ldvr < n__)) {
	*info = -10;
    }

/*     Compute workspace   
        (Note: Comments in the code beginning "Workspace:" describe the   
         minimal amount of workspace needed at that point in the code,   
         as well as the preferred amount for good performance.   
         CWorkspace refers to complex workspace, and RWorkspace to real   
         workspace. NB refers to the optimal block size for the   
         immediately following subroutine, as returned by ILAENV.   
         HSWORK refers to the workspace preferred by CHSEQR, as   
         calculated below. HSWORK is computed assuming ILO=1 and IHI=N,   
         the worst case.) */

    minwrk = 1;
    if (*info == 0 && lwork >= 1) {
	maxwrk = n__ + n__ * ilaenv(c__1, "CGEHRD", " ", n__, c__1, n__, c__0, 
		6L, 1L);
	if (! wantvl && ! wantvr) {
/* Computing MAX */
	    i__1 = 1, i__2 = n__ << 1;
	    minwrk = max(i__1,i__2);
/* Computing MAX */
	    i__1 = ilaenv(c__8, "CHSEQR", "EN", n__, c__1, n__, c_n1, 6L, 2L);
	    maxb = max(i__1,2);
/* Computing MIN   
   Computing MAX */
	    i__3 = 2, i__4 = ilaenv(c__4, "CHSEQR", "EN", n__, c__1, n__, 
		    c_n1, 6L, 2L);
	    i__1 = min(maxb,n__), i__2 = max(i__3,i__4);
	    k = min(i__1,i__2);
/* Computing MAX */
	    i__1 = k * (k + 2), i__2 = n__ << 1;
	    hswork = max(i__1,i__2);
	    maxwrk = max(maxwrk,hswork);
	} else {
/* Computing MAX */
	    i__1 = 1, i__2 = n__ << 1;
	    minwrk = max(i__1,i__2);
/* Computing MAX */
	    i__1 = maxwrk, i__2 = n__ + (n__ - 1) * ilaenv(c__1, "CUNGHR", 
		    " ", n__, c__1, n__, c_n1, 6L, 1L);
	    maxwrk = max(i__1,i__2);
/* Computing MAX */
	    i__1 = ilaenv(c__8, "CHSEQR", "SV", n__, c__1, n__, c_n1, 6L, 2L);
	    maxb = max(i__1,2);
/* Computing MIN   
   Computing MAX */
	    i__3 = 2, i__4 = ilaenv(c__4, "CHSEQR", "SV", n__, c__1, n__, 
		    c_n1, 6L, 2L);
	    i__1 = min(maxb,n__), i__2 = max(i__3,i__4);
	    k = min(i__1,i__2);
/* Computing MAX */
	    i__1 = k * (k + 2), i__2 = n__ << 1;
	    hswork = max(i__1,i__2);
/* Computing MAX */
	    i__1 = max(maxwrk,hswork), i__2 = n__ << 1;
	    maxwrk = max(i__1,i__2);
	}
	WORK(1).r = (float) maxwrk, WORK(1).i = 0.f;
    }
    if (lwork < minwrk) {
	*info = -12;
    }
    if (*info != 0) {
	i__1 = -(*info);
	return;
    }

/*     Quick return if possible */

    if (n__ == 0) {
	return ;
    }

/*     Get machine constants */

    eps = slamch("P");
    smlnum = slamch("S");
    bignum = 1.f / smlnum;
    slabad(&smlnum, &bignum);
    smlnum = sqrt(smlnum) / eps;
    bignum = 1.f / smlnum;

/*     Scale A if max element outside range [SMLNUM,BIGNUM] */

    anrm = clange("M", n__, n__, &A(1,1), lda, dum);
    scalea = FALSE;
    if (anrm > 0.f && anrm < smlnum) {
	scalea = TRUE;
	cscale = smlnum;
    } else if (anrm > bignum) {
	scalea = TRUE;
	cscale = bignum;
    }
    if (scalea) {
	clascl("G", c__0, c__0, anrm, cscale, n__, n__, &A(1,1), lda, &
		ierr);
    }

/*     Balance the matrix   
       (CWorkspace: none)   
       (RWorkspace: need N) */

    ibal = 1;
    cgebal("B", n__, &A(1,1), lda, &ilo, &ihi, &RWORK(ibal), &ierr);

/*     Reduce to upper Hessenberg form   
       (CWorkspace: need 2*N, prefer N+N*NB)   
       (RWorkspace: none) */

    itau = 1;
    iwrk = itau + n__;
    i__1 = lwork - iwrk + 1;
    cgehrd(n__, ilo, ihi, &A(1,1), lda, &WORK(itau), &WORK(iwrk), i__1,
	     &ierr);

    if (wantvl) {

/*        Want left eigenvectors   
          Copy Householder vectors to VL */

	*(unsigned char *)side = 'L';
	clacpy("L", n__, n__, &A(1,1), lda, &VL(1,1), ldvl);

/*        Generate unitary matrix in VL   
          (CWorkspace: need 2*N-1, prefer N+(N-1)*NB)   
          (RWorkspace: none) */

	i__1 = lwork - iwrk + 1;
	cunghr(n__, ilo, ihi, &VL(1,1), ldvl, &WORK(itau), &WORK(iwrk),
		 i__1, &ierr);

/*        Perform QR iteration, accumulating Schur vectors in VL   
          (CWorkspace: need 1, prefer HSWORK (see comments) )   
          (RWorkspace: none) */

	iwrk = itau;
	i__1 = lwork - iwrk + 1;
	chseqr("S", "V", n__, ilo, ihi, &A(1,1), lda, &W(1), 
                  &VL(1,1), ldvl, &WORK(iwrk), i__1, info);

	if (wantvr) {

/*           Want left and right eigenvectors   
             Copy Schur vectors to VR */

	    *(unsigned char *)side = 'B';
	    clacpy("F", n__, n__, &VL(1,1), ldvl, &VR(1,1), ldvr)
		    ;
	}

    } else if (wantvr) {

/*        Want right eigenvectors   
          Copy Householder vectors to VR */

	*(unsigned char *)side = 'R';
	clacpy("L", n__, n__, &A(1,1), lda, &VR(1,1), ldvr);

/*        Generate unitary matrix in VR   
          (CWorkspace: need 2*N-1, prefer N+(N-1)*NB)   
          (RWorkspace: none) */

	i__1 = lwork - iwrk + 1;
	cunghr(n__, ilo, ihi, &VR(1,1), ldvr, &WORK(itau), &WORK(iwrk),
		 i__1, &ierr);

/*        Perform QR iteration, accumulating Schur vectors in VR   
          (CWorkspace: need 1, prefer HSWORK (see comments) )   
          (RWorkspace: none) */

	iwrk = itau;
	i__1 = lwork - iwrk + 1;
	chseqr("S", "V", n__, ilo, ihi, &A(1,1), lda, &W(1), &VR(1,1), 
                ldvr, &WORK(iwrk), i__1, info);

    } else {

/*        Compute eigenvalues only   
          (CWorkspace: need 1, prefer HSWORK (see comments) )   
          (RWorkspace: none) */

	iwrk = itau;
	i__1 = lwork - iwrk + 1;
	chseqr("E", "N", n__, ilo, ihi, &A(1,1), lda, &W(1), &VR(1,1), 
                ldvr, &WORK(iwrk), i__1, info);
    }

/*     If INFO > 0 from CHSEQR, then quit */

    if (*info > 0) {
	goto L50;
    }

    if (wantvl || wantvr) {

/*        Compute left and/or right eigenvectors   
          (CWorkspace: need 2*N)   
          (RWorkspace: need 2*N) */

	irwork = ibal + n__;
	ctrevc(side, "B", select, n__, &A(1,1), lda, &VL(1,1), ldvl,
		 &VR(1,1), ldvr, n__, &nout, &WORK(iwrk), &RWORK(irwork), 
		&ierr);
    }

    if (wantvl) {

/*        Undo balancing of left eigenvectors   
          (CWorkspace: none)   
          (RWorkspace: need N) */

	cgebak("B", "L", n__, ilo, ihi, &RWORK(ibal), n__, &VL(1,1), 
		ldvl, &ierr);

/*        Normalize left eigenvectors and make largest component real 
*/

	i__1 = n__;
	for (i = 1; i <= n__; ++i) {
	    scl = 1.f / scnrm2(n__, &VL(1,i), c__1);
	    csscal(n__, scl, &VL(1,i), c__1);
	    i__2 = n__;
	    for (k = 1; k <= n__; ++k) {
		i__3 = k + i * vl_dim1;
/* Computing 2nd power */
		r__1 = VL(k,i).r;
/* Computing 2nd power */
		r__2 = VL(k,i).i;
		RWORK(irwork + k - 1) = r__1 * r__1 + r__2 * r__2;
	    }
	    k = isamax(n__, &RWORK(irwork), c__1);
	    r_cnjg(&q__2, &VL(k,i));
	    d__1 = sqrt(RWORK(irwork + k - 1));
	    q__1.r = q__2.r / d__1, q__1.i = q__2.i / d__1;
	    tmp.r = q__1.r, tmp.i = q__1.i;
	    cscal(n__, tmp, &VL(1,i), c__1);
	    i__2 = k + i * vl_dim1;
	    i__3 = k + i * vl_dim1;
	    d__1 = VL(k,i).r;
	    q__1.r = d__1, q__1.i = 0.f;
	    VL(k,i).r = q__1.r, VL(k,i).i = q__1.i;
	}
    }

    if (wantvr) {

/*        Undo balancing of right eigenvectors   
          (CWorkspace: none)   
          (RWorkspace: need N) */

	cgebak("B", "R", n__, ilo, ihi, &RWORK(ibal), n__, &VR(1,1), 
		ldvr, &ierr);

/*        Normalize right eigenvectors and make largest component real
 */

	i__1 = n__;
	for (i = 1; i <= n__; ++i) {
	    scl = 1.f / scnrm2(n__, &VR(1,i), c__1);
	    csscal(n__, scl, &VR(1,i), c__1);
	    i__2 = n__;
	    for (k = 1; k <= n__; ++k) {
		i__3 = k + i * vr_dim1;
/* Computing 2nd power */
		r__1 = VR(k,i).r;
/* Computing 2nd power */
		r__2 = VR(k,i).i;
		RWORK(irwork + k - 1) = r__1 * r__1 + r__2 * r__2;
	    }
	    k = isamax(n__, &RWORK(irwork), c__1);
	    r_cnjg(&q__2, &VR(k,i));
	    d__1 = sqrt(RWORK(irwork + k - 1));
	    q__1.r = q__2.r / d__1, q__1.i = q__2.i / d__1;
	    tmp.r = q__1.r, tmp.i = q__1.i;
	    cscal(n__, tmp, &VR(1,i), c__1);
	    i__2 = k + i * vr_dim1;
	    i__3 = k + i * vr_dim1;
	    d__1 = VR(k,i).r;
	    q__1.r = d__1, q__1.i = 0.f;
	    VR(k,i).r = q__1.r, VR(k,i).i = q__1.i;
	}
    }

/*     Undo scaling if necessary */

L50:
    if (scalea) {
	i__1 = n__ - *info;
/* Computing MAX */
	i__3 = n__ - *info;
	i__2 = max(i__3,1);
	clascl("G", c__0, c__0, cscale, anrm, i__1, c__1, &W(*info + 1)
		, i__2, &ierr);
	if (*info > 0) {
	    i__1 = ilo - 1;
	    clascl("G", c__0, c__0, cscale, anrm, i__1, c__1, &W(1), n__,
		     &ierr);
	}
    }

    WORK(1).r = (float) maxwrk, WORK(1).i = 0.f;


} 

