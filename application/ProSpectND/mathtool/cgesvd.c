
#include <math.h>
#include "complex.h"
#include "mathtool.h"

void cgesvd(char *jobu, char *jobvt, int m__, int n__, 
	    fcomplex *a, int lda, float *s, fcomplex *u, int ldu, 
	    fcomplex *vt, int ldvt, fcomplex *work, int lwork, 
	    float *rwork, int *info)
{
/*  -- LAPACK driver routine (version 2.0) --   
       Univ. of Tennessee, Univ. of California Berkeley, NAG Ltd.,   
       Courant Institute, Argonne National Lab, and Rice University   
       September 30, 1994   


    Purpose   
    =======   

    CGESVD computes the singular value decomposition (SVD) of a complex   
    M-by-N matrix A, optionally computing the left and/or right singular 
  
    vectors. The SVD is written   

         A = U * SIGMA * conjugate-transpose(V)   

    where SIGMA is an M-by-N matrix which is zero except for its   
    min(m,n) diagonal elements, U is an M-by-M unitary matrix, and   
    V is an N-by-N unitary matrix.  The diagonal elements of SIGMA   
    are the singular values of A; they are real and non-negative, and   
    are returned in descending order.  The first min(m,n) columns of   
    U and V are the left and right singular vectors of A.   

    Note that the routine returns V**H, not V.   

    Arguments   
    =========   

    JOBU    (input) CHARACTER*1   
            Specifies options for computing all or part of the matrix U: 
  
            = 'A':  all M columns of U are returned in array U:   
            = 'S':  the first min(m,n) columns of U (the left singular   
                    vectors) are returned in the array U;   
            = 'O':  the first min(m,n) columns of U (the left singular   
                    vectors) are overwritten on the array A;   
            = 'N':  no columns of U (no left singular vectors) are   
                    computed.   

    JOBVT   (input) CHARACTER*1   
            Specifies options for computing all or part of the matrix   
            V**H:   
            = 'A':  all N rows of V**H are returned in the array VT;   
            = 'S':  the first min(m,n) rows of V**H (the right singular   
                    vectors) are returned in the array VT;   
            = 'O':  the first min(m,n) rows of V**H (the right singular   
                    vectors) are overwritten on the array A;   
            = 'N':  no rows of V**H (no right singular vectors) are   
                    computed.   

            JOBVT and JOBU cannot both be 'O'.   

    M       (input) INTEGER   
            The number of rows of the input matrix A.  M >= 0.   

    N       (input) INTEGER   
            The number of columns of the input matrix A.  N >= 0.   

    A       (input/output) COMPLEX array, dimension (LDA,N)   
            On entry, the M-by-N matrix A.   
            On exit,   
            if JOBU = 'O',  A is overwritten with the first min(m,n)   
                            columns of U (the left singular vectors,   
                            stored columnwise);   
            if JOBVT = 'O', A is overwritten with the first min(m,n)   
                            rows of V**H (the right singular vectors,   
                            stored rowwise);   
            if JOBU .ne. 'O' and JOBVT .ne. 'O', the contents of A   
                            are destroyed.   

    LDA     (input) INTEGER   
            The leading dimension of the array A.  LDA >= max(1,M).   

    S       (output) REAL array, dimension (min(M,N))   
            The singular values of A, sorted so that S(i) >= S(i+1).   

    U       (output) COMPLEX array, dimension (LDU,UCOL)   
            (LDU,M) if JOBU = 'A' or (LDU,min(M,N)) if JOBU = 'S'.   
            If JOBU = 'A', U contains the M-by-M unitary matrix U;   
            if JOBU = 'S', U contains the first min(m,n) columns of U   
            (the left singular vectors, stored columnwise);   
            if JOBU = 'N' or 'O', U is not referenced.   

    LDU     (input) INTEGER   
            The leading dimension of the array U.  LDU >= 1; if   
            JOBU = 'S' or 'A', LDU >= M.   

    VT      (output) COMPLEX array, dimension (LDVT,N)   
            If JOBVT = 'A', VT contains the N-by-N unitary matrix   
            V**H;   
            if JOBVT = 'S', VT contains the first min(m,n) rows of   
            V**H (the right singular vectors, stored rowwise);   
            if JOBVT = 'N' or 'O', VT is not referenced.   

    LDVT    (input) INTEGER   
            The leading dimension of the array VT.  LDVT >= 1; if   
            JOBVT = 'A', LDVT >= N; if JOBVT = 'S', LDVT >= min(M,N).   

    WORK    (workspace/output) COMPLEX array, dimension (LWORK)   
            On exit, if INFO = 0, WORK(1) returns the optimal LWORK.   

    LWORK   (input) INTEGER   
            The dimension of the array WORK. LWORK >= 1.   
            LWORK >=  2*MIN(M,N)+MAX(M,N).   
            For good performance, LWORK should generally be larger.   

    RWORK   (workspace) REAL array, dimension   
                                    (max(3*min(M,N),5*min(M,N)-4))   
            On exit, if INFO > 0, RWORK(1:MIN(M,N)-1) contains the   
            unconverged superdiagonal elements of an upper bidiagonal   
            matrix B whose diagonal is in S (not necessarily sorted).   
            B satisfies A = U * B * VT, so it has the same singular   
            values as A, and singular vectors related by U and VT.   

    INFO    (output) INTEGER   
            = 0:  successful exit.   
            < 0:  if INFO = -i, the i-th argument had an illegal value.   
            > 0:  if CBDSQR did not converge, INFO specifies how many   
                  superdiagonals of an intermediate bidiagonal form B   
                  did not converge to zero. See the description of RWORK 
  
                  above for details.   

    ===================================================================== 
  


       Test the input arguments   

    
   Parameter adjustments   
       Function Body */
    /* Table of constant values */
    static fcomplex c_b1 = {0.f,0.f};
    static fcomplex c_b2 = {1.f,0.f};
    static int c__6 = 6;
    static int c__0 = 0;
    static int c__2 = 2;
    static int c__1 = 1;
    static int c_n1 = -1;
    
    /* System generated locals */
    char *a__1[2];
    int  i__1[2],i__2, i__3, i__4;
    char ch__1[2];
    static fcomplex cdum[1];
    static int iscl;
    static float anrm;
    static int ierr, itau, ncvt, nrvt, i;
    static int chunk, minmn, wrkbl, itaup, itauq, mnthr, iwork;
    static int wntua, wntva, wntun, wntuo, wntvn, wntvo, wntus, wntvs;
    static int ie;
    static int ir, iu;
    static float bignum;
    static int ldwrkr, minwrk, ldwrku, maxwrk;
    static float smlnum;
    static int irwork;
    static int wntuas, wntvas;
    static int blk, ncu;
    static float dum[1], eps;
    static int nru;



#define S(I) s[(I)-1]
#define WORK(I) work[(I)-1]
#define RWORK(I) rwork[(I)-1]

#define A(I,J) a[(I)-1 + ((J)-1)* ( lda)]
#define U(I,J) u[(I)-1 + ((J)-1)* ( ldu)]
#define VT(I,J) vt[(I)-1 + ((J)-1)* ( ldvt)]

    *info = 0;
    minmn = min(m__,n__);
/* Writing concatenation */
    i__1[0] = 1, a__1[0] = jobu;
    i__1[1] = 1, a__1[1] = jobvt;
    s_cat(ch__1, a__1, i__1, &c__2, 2L);
    mnthr = ilaenv(c__6, "CGESVD", ch__1, m__, n__, c__0, c__0, 6L, 2L);
    wntua = lsame(jobu, "A");
    wntus = lsame(jobu, "S");
    wntuas = wntua || wntus;
    wntuo = lsame(jobu, "O");
    wntun = lsame(jobu, "N");
    wntva = lsame(jobvt, "A");
    wntvs = lsame(jobvt, "S");
    wntvas = wntva || wntvs;
    wntvo = lsame(jobvt, "O");
    wntvn = lsame(jobvt, "N");
    minwrk = 1;

    if (! (wntua || wntus || wntuo || wntun)) {
	*info = -1;
    } else if (! (wntva || wntvs || wntvo || wntvn) || (wntvo && wntuo)) {
	*info = -2;
    } else if (m__ < 0) {
	*info = -3;
    } else if (n__ < 0) {
	*info = -4;
    } else if (lda < max(1,m__)) {
	*info = -6;
    } else if (ldu < 1 || (wntuas && ldu < m__)) {
	*info = -9;
    } else if (ldvt < 1 || (wntva && ldvt < n__) || (wntvs && ldvt < minmn)) {
	*info = -11;
    }

/*     Compute workspace   
        (Note: Comments in the code beginning "Workspace:" describe the   
         minimal amount of workspace needed at that point in the code,   
         as well as the preferred amount for good performance.   
         CWorkspace refers to complex workspace, and RWorkspace to   
         float workspace. NB refers to the optimal block size for the   
         immediately following subroutine, as returned by ILAENV.) */

    if (*info == 0 && lwork >= 1 && m__ > 0 && n__ > 0) {
	if (m__ >= n__) {

/*           Space needed for CBDSQR is BDSPAC = MAX( 3*N, 5*N-4 )
 */

	    if (m__ >= mnthr) {
		if (wntun) {

/*                 Path 1 (M much larger than N, JOBU='N') */

		    maxwrk = n__ + n__ * ilaenv(c__1, "CGEQRF", " ", m__, n__, 
			    c_n1, c_n1, 6L, 1L);
/* Computing MAX */
		    i__2 = maxwrk, i__3 = (n__ << 1) + (n__ << 1) * ilaenv(
			    c__1, "CGEBRD", " ", n__,n__, c_n1, c_n1, 6L, 1L);
		    maxwrk = max(i__2,i__3);
		    if (wntvo || wntvas) {
/* Computing MAX */
			i__2 = maxwrk, i__3 = (n__ << 1) + (n__ - 1) * ilaenv(
				c__1, "CUNGBR", "P", n__,n__,n__, c_n1, 6L, 1L);
			maxwrk = max(i__2,i__3);
		    }
		    minwrk = n__ * 3;
		    maxwrk = max(minwrk,maxwrk);
		} else if (wntuo && wntvn) {

/*                 Path 2 (M much larger than N, JOBU='O', JOBVT='N') */

		    wrkbl = n__ + n__ * ilaenv(c__1, "CGEQRF", " ", m__, n__, 
			    c_n1, c_n1, 6L, 1L);
/* Computing MAX */
		    i__2 = wrkbl, i__3 = n__ + n__ * ilaenv(c__1, "CUNGQR", 
			    " ", m__, n__,n__, c_n1, 6L, 1L);
		    wrkbl = max(i__2,i__3);
/* Computing MAX */
		    i__2 = wrkbl, i__3 = (n__ << 1) + (n__ << 1) * ilaenv(
			    c__1, "CGEBRD", " ", n__,n__, c_n1, c_n1, 6L, 1L);
		    wrkbl = max(i__2,i__3);
/* Computing MAX */
		    i__2 = wrkbl, i__3 = (n__ << 1) + n__ * ilaenv(c__1, 
			    "CUNGBR", "Q", n__,n__,n__, c_n1, 6L, 1L);
		    wrkbl = max(i__2,i__3);
/* Computing MAX */
		    i__2 = n__ * n__ + wrkbl, i__3 = n__ * n__ + m__ * n__;
		    maxwrk = max(i__2,i__3);
		    minwrk = (n__ << 1) + m__;
		    maxwrk = max(minwrk,maxwrk);
		} else if (wntuo && wntvas) {

/*                 Path 3 (M much larger than N, JOBU='O', JOBVT='S' or   
                   'A') */

		    wrkbl = n__ + n__ * ilaenv(c__1, "CGEQRF", " ", m__, n__, 
			    c_n1, c_n1, 6L, 1L);
/* Computing MAX */
		    i__2 = wrkbl, i__3 = n__ + n__ * ilaenv(c__1, "CUNGQR", 
			    " ", m__, n__,n__, c_n1, 6L, 1L);
		    wrkbl = max(i__2,i__3);
/* Computing MAX */
		    i__2 = wrkbl, i__3 = (n__ << 1) + (n__ << 1) * ilaenv(
			    c__1, "CGEBRD", " ", n__,n__, c_n1, c_n1, 6L, 1L);
		    wrkbl = max(i__2,i__3);
/* Computing MAX */
		    i__2 = wrkbl, i__3 = (n__ << 1) + n__ * ilaenv(c__1, 
			    "CUNGBR", "Q", n__,n__,n__, c_n1, 6L, 1L);
		    wrkbl = max(i__2,i__3);
/* Computing MAX */
		    i__2 = wrkbl, i__3 = (n__ << 1) + (n__ - 1) * ilaenv(c__1,
			     "CUNGBR", "P", n__,n__,n__, c_n1, 6L, 1L);
		    wrkbl = max(i__2,i__3);
/* Computing MAX */
		    i__2 = n__ * n__ + wrkbl, i__3 = n__ * n__ + m__ * n__;
		    maxwrk = max(i__2,i__3);
		    minwrk = (n__ << 1) + m__;
		    maxwrk = max(minwrk,maxwrk);
		} else if (wntus && wntvn) {

/*                 Path 4 (M much larger than N, JOBU='S', JOBVT='N') */

		    wrkbl = n__ + n__ * ilaenv(c__1, "CGEQRF", " ", m__, n__, 
			    c_n1, c_n1, 6L, 1L);
/* Computing MAX */
		    i__2 = wrkbl, i__3 = n__ + n__ * ilaenv(c__1, "CUNGQR", 
			    " ", m__, n__,n__, c_n1, 6L, 1L);
		    wrkbl = max(i__2,i__3);
/* Computing MAX */
		    i__2 = wrkbl, i__3 = (n__ << 1) + (n__ << 1) * ilaenv(
			    c__1, "CGEBRD", " ", n__,n__, c_n1, c_n1, 6L, 1L);
		    wrkbl = max(i__2,i__3);
/* Computing MAX */
		    i__2 = wrkbl, i__3 = (n__ << 1) + n__ * ilaenv(c__1, 
			    "CUNGBR", "Q", n__,n__,n__, c_n1, 6L, 1L);
		    wrkbl = max(i__2,i__3);
		    maxwrk = n__ * n__ + wrkbl;
		    minwrk = (n__ << 1) + m__;
		    maxwrk = max(minwrk,maxwrk);
		} else if (wntus && wntvo) {

/*                 Path 5 (M much larger than N, JOBU='S', JOBVT='O') */

		    wrkbl = n__ + n__ * ilaenv(c__1, "CGEQRF", " ", m__, n__, 
			    c_n1, c_n1, 6L, 1L);
/* Computing MAX */
		    i__2 = wrkbl, i__3 = n__ + n__ * ilaenv(c__1, "CUNGQR", 
			    " ", m__, n__,n__, c_n1, 6L, 1L);
		    wrkbl = max(i__2,i__3);
/* Computing MAX */
		    i__2 = wrkbl, i__3 = (n__ << 1) + (n__ << 1) * ilaenv(
			    c__1, "CGEBRD", " ", n__,n__, c_n1, c_n1, 6L, 1L);
		    wrkbl = max(i__2,i__3);
/* Computing MAX */
		    i__2 = wrkbl, i__3 = (n__ << 1) + n__ * ilaenv(c__1, 
			    "CUNGBR", "Q", n__,n__,n__, c_n1, 6L, 1L);
		    wrkbl = max(i__2,i__3);
/* Computing MAX */
		    i__2 = wrkbl, i__3 = (n__ << 1) + (n__ - 1) * ilaenv(c__1,
			     "CUNGBR", "P", n__,n__,n__, c_n1, 6L, 1L);
		    wrkbl = max(i__2,i__3);
		    maxwrk = (n__ << 1) * n__ + wrkbl;
		    minwrk = (n__ << 1) + m__;
		    maxwrk = max(minwrk,maxwrk);
		} else if (wntus && wntvas) {

/*                 Path 6 (M much larger than N, JOBU='S', JOBVT='S' or   
                   'A') */

		    wrkbl = n__ + n__ * ilaenv(c__1, "CGEQRF", " ", m__, n__, 
			    c_n1, c_n1, 6L, 1L);
/* Computing MAX */
		    i__2 = wrkbl, i__3 = n__ + n__ * ilaenv(c__1, "CUNGQR", 
			    " ", m__, n__,n__, c_n1, 6L, 1L);
		    wrkbl = max(i__2,i__3);
/* Computing MAX */
		    i__2 = wrkbl, i__3 = (n__ << 1) + (n__ << 1) * ilaenv(
			    c__1, "CGEBRD", " ", n__,n__, c_n1, c_n1, 6L, 1L);
		    wrkbl = max(i__2,i__3);
/* Computing MAX */
		    i__2 = wrkbl, i__3 = (n__ << 1) + n__ * ilaenv(c__1, 
			    "CUNGBR", "Q", n__,n__,n__, c_n1, 6L, 1L);
		    wrkbl = max(i__2,i__3);
/* Computing MAX */
		    i__2 = wrkbl, i__3 = (n__ << 1) + (n__ - 1) * ilaenv(c__1,
			     "CUNGBR", "P", n__,n__,n__, c_n1, 6L, 1L);
		    wrkbl = max(i__2,i__3);
		    maxwrk = n__ * n__ + wrkbl;
		    minwrk = (n__ << 1) + m__;
		    maxwrk = max(minwrk,maxwrk);
		} else if (wntua && wntvn) {

/*                 Path 7 (M much larger than N, JOBU='A', JOBVT='N') */

		    wrkbl = n__ + n__ * ilaenv(c__1, "CGEQRF", " ", m__, n__, 
			    c_n1, c_n1, 6L, 1L);
/* Computing MAX */
		    i__2 = wrkbl, i__3 = n__ + m__ * ilaenv(c__1, "CUNGQR", 
			    " ",  m__,m__, n__, c_n1, 6L, 1L);
		    wrkbl = max(i__2,i__3);
/* Computing MAX */
		    i__2 = wrkbl, i__3 = (n__ << 1) + (n__ << 1) * ilaenv(
			    c__1, "CGEBRD", " ", n__,n__, c_n1, c_n1, 6L, 1L);
		    wrkbl = max(i__2,i__3);
/* Computing MAX */
		    i__2 = wrkbl, i__3 = (n__ << 1) + n__ * ilaenv(c__1, 
			    "CUNGBR", "Q", n__,n__,n__, c_n1, 6L, 1L);
		    wrkbl = max(i__2,i__3);
		    maxwrk = n__ * n__ + wrkbl;
		    minwrk = (n__ << 1) + m__;
		    maxwrk = max(minwrk,maxwrk);
		} else if (wntua && wntvo) {

/*                 Path 8 (M much larger than N, JOBU='A', JOBVT='O') */

		    wrkbl = n__ + n__ * ilaenv(c__1, "CGEQRF", " ", m__, n__, 
			    c_n1, c_n1, 6L, 1L);
/* Computing MAX */
		    i__2 = wrkbl, i__3 = n__ + m__ * ilaenv(c__1, "CUNGQR", 
			    " ",  m__,m__, n__, c_n1, 6L, 1L);
		    wrkbl = max(i__2,i__3);
/* Computing MAX */
		    i__2 = wrkbl, i__3 = (n__ << 1) + (n__ << 1) * ilaenv(
			    c__1, "CGEBRD", " ", n__,n__, c_n1, c_n1, 6L, 1L);
		    wrkbl = max(i__2,i__3);
/* Computing MAX */
		    i__2 = wrkbl, i__3 = (n__ << 1) + n__ * ilaenv(c__1, 
			    "CUNGBR", "Q", n__,n__,n__, c_n1, 6L, 1L);
		    wrkbl = max(i__2,i__3);
/* Computing MAX */
		    i__2 = wrkbl, i__3 = (n__ << 1) + (n__ - 1) * ilaenv(c__1,
			     "CUNGBR", "P", n__,n__,n__, c_n1, 6L, 1L);
		    wrkbl = max(i__2,i__3);
		    maxwrk = (n__ << 1) * n__ + wrkbl;
		    minwrk = (n__ << 1) + m__;
		    maxwrk = max(minwrk,maxwrk);
		} else if (wntua && wntvas) {

/*                 Path 9 (M much larger than N, JOBU='A', JOBVT='S' or   
                   'A') */

		    wrkbl = n__ + n__ * ilaenv(c__1, "CGEQRF", " ", m__, n__, 
			    c_n1, c_n1, 6L, 1L);
/* Computing MAX */
		    i__2 = wrkbl, i__3 = n__ + m__ * ilaenv(c__1, "CUNGQR", 
			    " ",  m__,m__, n__, c_n1, 6L, 1L);
		    wrkbl = max(i__2,i__3);
/* Computing MAX */
		    i__2 = wrkbl, i__3 = (n__ << 1) + (n__ << 1) * ilaenv(
			    c__1, "CGEBRD", " ", n__,n__, c_n1, c_n1, 6L, 1L);
		    wrkbl = max(i__2,i__3);
/* Computing MAX */
		    i__2 = wrkbl, i__3 = (n__ << 1) + n__ * ilaenv(c__1, 
			    "CUNGBR", "Q", n__,n__,n__, c_n1, 6L, 1L);
		    wrkbl = max(i__2,i__3);
/* Computing MAX */
		    i__2 = wrkbl, i__3 = (n__ << 1) + (n__ - 1) * ilaenv(c__1,
			     "CUNGBR", "P", n__,n__,n__, c_n1, 6L, 1L);
		    wrkbl = max(i__2,i__3);
		    maxwrk = n__ * n__ + wrkbl;
		    minwrk = (n__ << 1) + m__;
		    maxwrk = max(minwrk,maxwrk);
		}
	    } else {

/*              Path 10 (M at least N, but not much larger) */

		maxwrk = (n__ << 1) + (m__ + n__) * ilaenv(c__1, "CGEBRD", 
			" ", m__, n__, c_n1, c_n1, 6L, 1L);
		if (wntus || wntuo) {
/* Computing MAX */
		    i__2 = maxwrk, i__3 = (n__ << 1) + n__ * ilaenv(c__1, 
			    "CUNGBR", "Q", m__, n__,n__, c_n1, 6L, 1L);
		    maxwrk = max(i__2,i__3);
		}
		if (wntua) {
/* Computing MAX */
		    i__2 = maxwrk, i__3 = (n__ << 1) + m__ * ilaenv(c__1, 
			    "CUNGBR", "Q",  m__,m__, n__, c_n1, 6L, 1L);
		    maxwrk = max(i__2,i__3);
		}
		if (! wntvn) {
/* Computing MAX */
		    i__2 = maxwrk, i__3 = (n__ << 1) + (n__ - 1) * ilaenv(
			    c__1, "CUNGBR", "P", n__,n__,n__, c_n1, 6L, 1L);
		    maxwrk = max(i__2,i__3);
		}
		minwrk = (n__ << 1) + m__;
		maxwrk = max(minwrk,maxwrk);
	    }
	} else {

/*           Space needed for CBDSQR is BDSPAC = MAX( 3*M, 5*M-4 ) */

	    if (n__ >= mnthr) {
		if (wntvn) {

/*                 Path 1t(N much larger than M, JOBVT='N') */

		    maxwrk = m__ + m__ * ilaenv(c__1, "CGELQF", " ", m__, n__, 
			    c_n1, c_n1, 6L, 1L);
/* Computing MAX */
		    i__2 = maxwrk, i__3 = (m__ << 1) + (m__ << 1) * ilaenv(
			    c__1, "CGEBRD", " ", m__,m__, c_n1, c_n1, 6L, 1L);
		    maxwrk = max(i__2,i__3);
		    if (wntuo || wntuas) {
/* Computing MAX */
			i__2 = maxwrk, i__3 = (m__ << 1) + m__ * ilaenv(c__1, 
				"CUNGBR", "Q", m__, m__,m__, c_n1, 6L, 1L);
			maxwrk = max(i__2,i__3);
		    }
		    minwrk = m__ * 3;
		    maxwrk = max(minwrk,maxwrk);
		} else if (wntvo && wntun) {

/*                 Path 2t(N much larger than M, JOBU='N', JOBVT='O') */

		    wrkbl = m__ + m__ * ilaenv(c__1, "CGELQF", " ", m__, n__, 
			    c_n1, c_n1, 6L, 1L);
/* Computing MAX */
		    i__2 = wrkbl, i__3 = m__ + m__ * ilaenv(c__1, "CUNGLQ", 
			    " ", m__,  n__,m__, c_n1, 6L, 1L);
		    wrkbl = max(i__2,i__3);
/* Computing MAX */
		    i__2 = wrkbl, i__3 = (m__ << 1) + (m__ << 1) * ilaenv(
			    c__1, "CGEBRD", " ", m__,m__, c_n1, c_n1, 6L, 1L);
		    wrkbl = max(i__2,i__3);
/* Computing MAX */
		    i__2 = wrkbl, i__3 = (m__ << 1) + (m__ - 1) * ilaenv(c__1,
			     "CUNGBR", "P", m__, m__,m__, c_n1, 6L, 1L);
		    wrkbl = max(i__2,i__3);
/* Computing MAX */
		    i__2 = m__ * m__ + wrkbl, i__3 = m__ * m__ + m__ * n__;
		    maxwrk = max(i__2,i__3);
		    minwrk = (m__ << 1) + n__;
		    maxwrk = max(minwrk,maxwrk);
		} else if (wntvo && wntuas) {

/*                 Path 3t(N much larger than M, JOBU='S' or 'A',   
                   JOBVT='O') */

		    wrkbl = m__ + m__ * ilaenv(c__1, "CGELQF", " ", m__, n__, 
			    c_n1, c_n1, 6L, 1L);
/* Computing MAX */
		    i__2 = wrkbl, i__3 = m__ + m__ * ilaenv(c__1, "CUNGLQ", 
			    " ", m__,  n__,m__, c_n1, 6L, 1L);
		    wrkbl = max(i__2,i__3);
/* Computing MAX */
		    i__2 = wrkbl, i__3 = (m__ << 1) + (m__ << 1) * ilaenv(
			    c__1, "CGEBRD", " ", m__,m__, c_n1, c_n1, 6L, 1L);
		    wrkbl = max(i__2,i__3);
/* Computing MAX */
		    i__2 = wrkbl, i__3 = (m__ << 1) + (m__ - 1) * ilaenv(c__1,
			     "CUNGBR", "P", m__,m__,m__, c_n1, 6L, 1L);
		    wrkbl = max(i__2,i__3);
/* Computing MAX */
		    i__2 = wrkbl, i__3 = (m__ << 1) + m__ * ilaenv(c__1, 
			    "CUNGBR", "Q", m__,m__,m__, c_n1, 6L, 1L);
		    wrkbl = max(i__2,i__3);
/* Computing MAX */
		    i__2 = m__ * m__ + wrkbl, i__3 = m__ * m__ + m__ * n__;
		    maxwrk = max(i__2,i__3);
		    minwrk = (m__ << 1) + n__;
		    maxwrk = max(minwrk,maxwrk);
		} else if (wntvs && wntun) {

/*                 Path 4t(N much larger than M, JOBU='N', JOBVT='S') */

		    wrkbl = m__ + m__ * ilaenv(c__1, "CGELQF", " ", m__, n__, 
			    c_n1, c_n1, 6L, 1L);
/* Computing MAX */
		    i__2 = wrkbl, i__3 = m__ + m__ * ilaenv(c__1, "CUNGLQ", 
			    " ", m__,  n__,m__, c_n1, 6L, 1L);
		    wrkbl = max(i__2,i__3);
/* Computing MAX */
		    i__2 = wrkbl, i__3 = (m__ << 1) + (m__ << 1) * ilaenv(
			    c__1, "CGEBRD", " ", m__,m__, c_n1, c_n1, 6L, 1L);
		    wrkbl = max(i__2,i__3);
/* Computing MAX */
		    i__2 = wrkbl, i__3 = (m__ << 1) + (m__ - 1) * ilaenv(c__1,
			     "CUNGBR", "P", m__,m__,m__, c_n1, 6L, 1L);
		    wrkbl = max(i__2,i__3);
		    maxwrk = m__ * m__ + wrkbl;
		    minwrk = (m__ << 1) + n__;
		    maxwrk = max(minwrk,maxwrk);
		} else if (wntvs && wntuo) {

/*                 Path 5t(N much larger than M, JOBU='O', JOBVT='S') */

		    wrkbl = m__ + m__ * ilaenv(c__1, "CGELQF", " ", m__, n__, 
			    c_n1, c_n1, 6L, 1L);
/* Computing MAX */
		    i__2 = wrkbl, i__3 = m__ + m__ * ilaenv(c__1, "CUNGLQ", 
			    " ", m__,  n__,m__, c_n1, 6L, 1L);
		    wrkbl = max(i__2,i__3);
/* Computing MAX */
		    i__2 = wrkbl, i__3 = (m__ << 1) + (m__ << 1) * ilaenv(
			    c__1, "CGEBRD", " ", m__,m__, c_n1, c_n1, 6L, 1L);
		    wrkbl = max(i__2,i__3);
/* Computing MAX */
		    i__2 = wrkbl, i__3 = (m__ << 1) + (m__ - 1) * ilaenv(c__1,
			     "CUNGBR", "P", m__,m__,m__, c_n1, 6L, 1L);
		    wrkbl = max(i__2,i__3);
/* Computing MAX */
		    i__2 = wrkbl, i__3 = (m__ << 1) + m__ * ilaenv(c__1, 
			    "CUNGBR", "Q", m__,m__,m__, c_n1, 6L, 1L);
		    wrkbl = max(i__2,i__3);
		    maxwrk = (m__ << 1) * m__ + wrkbl;
		    minwrk = (m__ << 1) + n__;
		    maxwrk = max(minwrk,maxwrk);
		} else if (wntvs && wntuas) {

/*                 Path 6t(N much larger than M, JOBU='S' or 'A',   
                   JOBVT='S') */

		    wrkbl = m__ + m__ * ilaenv(c__1, "CGELQF", " ", m__, n__, 
			    c_n1, c_n1, 6L, 1L);
/* Computing MAX */
		    i__2 = wrkbl, i__3 = m__ + m__ * ilaenv(c__1, "CUNGLQ", 
			    " ", m__,  n__,m__, c_n1, 6L, 1L);
		    wrkbl = max(i__2,i__3);
/* Computing MAX */
		    i__2 = wrkbl, i__3 = (m__ << 1) + (m__ << 1) * ilaenv(
			    c__1, "CGEBRD", " ", m__,m__, c_n1, c_n1, 6L, 1L);
		    wrkbl = max(i__2,i__3);
/* Computing MAX */
		    i__2 = wrkbl, i__3 = (m__ << 1) + (m__ - 1) * ilaenv(c__1,
			     "CUNGBR", "P", m__,m__,m__, c_n1, 6L, 1L);
		    wrkbl = max(i__2,i__3);
/* Computing MAX */
		    i__2 = wrkbl, i__3 = (m__ << 1) + m__ * ilaenv(c__1, 
			    "CUNGBR", "Q", m__,m__,m__, c_n1, 6L, 1L);
		    wrkbl = max(i__2,i__3);
		    maxwrk = m__ * m__ + wrkbl;
		    minwrk = (m__ << 1) + n__;
		    maxwrk = max(minwrk,maxwrk);
		} else if (wntva && wntun) {

/*                 Path 7t(N much larger than M, JOBU='N',
 JOBVT='A') */

		    wrkbl = m__ + m__ * ilaenv(c__1, "CGELQF", " ", m__, n__, 
			    c_n1, c_n1, 6L, 1L);
/* Computing MAX */
		    i__2 = wrkbl, i__3 = m__ + n__ * ilaenv(c__1, "CUNGLQ", 
			    " ", n__, n__,m__, c_n1, 6L, 1L);
		    wrkbl = max(i__2,i__3);
/* Computing MAX */
		    i__2 = wrkbl, i__3 = (m__ << 1) + (m__ << 1) * ilaenv(
			    c__1, "CGEBRD", " ", m__,m__,c_n1, c_n1, 6L, 1L);
		    wrkbl = max(i__2,i__3);
/* Computing MAX */
		    i__2 = wrkbl, i__3 = (m__ << 1) + (m__ - 1) * ilaenv(c__1,
			     "CUNGBR", "P", m__,m__,m__, c_n1, 6L, 1L);
		    wrkbl = max(i__2,i__3);
		    maxwrk = m__ * m__ + wrkbl;
		    minwrk = (m__ << 1) + n__;
		    maxwrk = max(minwrk,maxwrk);
		} else if (wntva && wntuo) {

/*                 Path 8t(N much larger than M, JOBU='O', JOBVT='A') */

		    wrkbl = m__ + m__ * ilaenv(c__1, "CGELQF", " ", m__, n__, 
			    c_n1, c_n1, 6L, 1L);
/* Computing MAX */
		    i__2 = wrkbl, i__3 = m__ + n__ * ilaenv(c__1, "CUNGLQ", 
			    " ", n__, n__,m__, c_n1, 6L, 1L);
		    wrkbl = max(i__2,i__3);
/* Computing MAX */
		    i__2 = wrkbl, i__3 = (m__ << 1) + (m__ << 1) * ilaenv(
			    c__1, "CGEBRD", " ", m__,m__, c_n1, c_n1, 6L, 1L);
		    wrkbl = max(i__2,i__3);
/* Computing MAX */
		    i__2 = wrkbl, i__3 = (m__ << 1) + (m__ - 1) * ilaenv(c__1,
			     "CUNGBR", "P", m__,m__,m__, c_n1, 6L, 1L);
		    wrkbl = max(i__2,i__3);
/* Computing MAX */
		    i__2 = wrkbl, i__3 = (m__ << 1) + m__ * ilaenv(c__1, 
			    "CUNGBR", "Q", m__,m__,m__,c_n1, 6L, 1L);
		    wrkbl = max(i__2,i__3);
		    maxwrk = (m__ << 1) * m__ + wrkbl;
		    minwrk = (m__ << 1) + n__;
		    maxwrk = max(minwrk,maxwrk);
		} else if (wntva && wntuas) {

/*                 Path 9t(N much larger than M, JOBU='S' or 'A',   
                   JOBVT='A') */

		    wrkbl = m__ + m__ * ilaenv(c__1, "CGELQF", " ", m__, n__, 
			    c_n1, c_n1, 6L, 1L);
/* Computing MAX */
		    i__2 = wrkbl, i__3 = m__ + n__ * ilaenv(c__1, "CUNGLQ", 
			    " ", n__, n__,m__, c_n1, 6L, 1L);
		    wrkbl = max(i__2,i__3);
/* Computing MAX */
		    i__2 = wrkbl, i__3 = (m__ << 1) + (m__ << 1) * ilaenv(
			    c__1, "CGEBRD", " ", m__,m__, c_n1, c_n1, 6L, 1L);
		    wrkbl = max(i__2,i__3);
/* Computing MAX */
		    i__2 = wrkbl, i__3 = (m__ << 1) + (m__ - 1) * ilaenv(c__1,
			     "CUNGBR", "P", m__,m__,m__, c_n1, 6L, 1L);
		    wrkbl = max(i__2,i__3);
/* Computing MAX */
		    i__2 = wrkbl, i__3 = (m__ << 1) + m__ * ilaenv(c__1, 
			    "CUNGBR", "Q", m__,m__,m__, c_n1, 6L, 1L);
		    wrkbl = max(i__2,i__3);
		    maxwrk = m__ * m__ + wrkbl;
		    minwrk = (m__ << 1) + n__;
		    maxwrk = max(minwrk,maxwrk);
		}
	    } else {

/*              Path 10t(N greater than M, but not much larger) */

		maxwrk = (m__ << 1) + (m__ + n__) * ilaenv(c__1, "CGEBRD", 
			" ", m__, n__, c_n1, c_n1, 6L, 1L);
		if (wntvs || wntvo) {
/* Computing MAX */
		    i__2 = maxwrk, i__3 = (m__ << 1) + m__ * ilaenv(c__1, 
			    "CUNGBR", "P", m__,  n__,m__, c_n1, 6L, 1L);
		    maxwrk = max(i__2,i__3);
		}
		if (wntva) {
/* Computing MAX */
		    i__2 = maxwrk, i__3 = (m__ << 1) + n__ * ilaenv(c__1, 
			    "CUNGBR", "P", n__, n__,m__, c_n1, 6L, 1L);
		    maxwrk = max(i__2,i__3);
		}
		if (! wntun) {
/* Computing MAX */
		    i__2 = maxwrk, i__3 = (m__ << 1) + (m__ - 1) * ilaenv(
			    c__1, "CUNGBR", "Q", m__,m__,m__, c_n1, 6L, 1L);
		    maxwrk = max(i__2,i__3);
		}
		minwrk = (m__ << 1) + n__;
		maxwrk = max(minwrk,maxwrk);
	    }
	}
	WORK(1).r = (float) maxwrk, WORK(1).i = 0.f;
    }

    if (lwork < minwrk) {
	*info = -13;
    }
    if (*info != 0) {
	i__2 = -(*info);
	return ;
    }

/*     Quick return if possible */

    if (m__ == 0 || n__ == 0) {
	if (lwork >= 1) {
	    WORK(1).r = 1.f, WORK(1).i = 0.f;
	}
	return ;
    }

/*     Get machine constants */

    eps = slamch("P");
    smlnum = sqrt(slamch("S")) / eps;
    bignum = 1.f / smlnum;

/*     Scale A if max element outside range [SMLNUM,BIGNUM] */

    anrm = clange("M", m__, n__, &A(1,1), lda, dum);
    iscl = 0;
    if (anrm > 0.f && anrm < smlnum) {
	iscl = 1;
	clascl("G", c__0, c__0, anrm, smlnum, m__, n__, &A(1,1), lda, &
		ierr);
    } else if (anrm > bignum) {
	iscl = 1;
	clascl("G", c__0, c__0, anrm, bignum, m__, n__, &A(1,1), lda, &
		ierr);
    }

    if (m__ >= n__) {

/*        A has at least as many rows as columns. If A has sufficiently   
          more rows than columns, first reduce using the QR   
          decomposition (if sufficient workspace available) */

	if (m__ >= mnthr) {

	    if (wntun) {

/*              Path 1 (M much larger than N, JOBU='N')   
                No left singular vectors to be computed */

		itau = 1;
		iwork = itau + n__;

/*              Compute A=Q*R   
                (CWorkspace: need 2*N, prefer N+N*NB)   
                (RWorkspace: need 0) */

		i__2 = lwork - iwork + 1;
		cgeqrf(m__,n__, &A(1,1), lda, &WORK(itau), &WORK(iwork), 
			i__2, &ierr);

/*              Zero out below R */

		i__2 = n__ - 1;
		i__3 = n__ - 1;
		claset("L", i__2, i__3, c_b1, c_b1, &A(2,1), lda);
		ie = 1;
		itauq = 1;
		itaup = itauq + n__;
		iwork = itaup + n__;

/*              Bidiagonalize R in A   
                (CWorkspace: need 3*N, prefer 2*N+2*N*NB)   
                (RWorkspace: need N) */

		i__2 = lwork - iwork + 1;
		cgebrd(n__,n__, &A(1,1), lda, &S(1), &RWORK(ie), &WORK(
			itauq), &WORK(itaup), &WORK(iwork), i__2, &ierr);
		ncvt = 0;
		if (wntvo || wntvas) {

/*                 If right singular vectors desired, generate P'.   
                   (CWorkspace: need 3*N-1, prefer 2*N+(N-1)*NB)   
                   (RWorkspace: 0) */

		    i__2 = lwork - iwork + 1;
		    cungbr("P", n__,n__,n__, &A(1,1), lda, &WORK(itaup), &
			    WORK(iwork), i__2, &ierr);
		    ncvt = n__;
		}
		irwork = ie + n__;

/*              Perform bidiagonal QR iteration, computing right   
                singular vectors of A in A if desired   
                (CWorkspace: 0)   
                (RWorkspace: need BDSPAC) */

		cbdsqr("U", n__, ncvt, c__0, c__0, &S(1), &RWORK(ie), &A(1,1), 
                        lda, cdum, c__1, cdum, c__1, &RWORK(
			irwork), info);

/*              If right singular vectors desired in VT, copy them there */

		if (wntvas) {
		    clacpy("F", n__,n__, &A(1,1), lda, &VT(1,1), 
			    ldvt);
		}

	    } else if (wntuo && wntvn) {

/*              Path 2 (M much larger than N, JOBU='O', JOBVT='N')   
                N left singular vectors to be overwritten on A and   
                no right singular vectors to be computed */

		if (lwork >= n__ * n__ + n__ * 3) {

/*                 Sufficient workspace for a fast algorithm */

		    ir = 1;
/* Computing MAX */
		    i__2 = wrkbl, i__3 = lda * n__;
		    if (lwork >= max(i__2,i__3) + lda * n__) {

/*                    WORK(IU) is LDA by N, WORK(IR) is LDA by N */

			ldwrku = lda;
			ldwrkr = lda;
		    } else /* if(complicated condition) */ {
/* Computing MAX */
			i__2 = wrkbl, i__3 = lda * n__;
			if (lwork >= max(i__2,i__3) + n__ * n__) {

/*                    WORK(IU) is LDA by N, WORK(IR) is N by N */

			    ldwrku = lda;
			    ldwrkr = n__;
			} else {

/*                    WORK(IU) is LDWRKU by N, WORK(IR) is N by N */

			    ldwrku = (lwork - n__ * n__) / n__;
			    ldwrkr = n__;
			}
		    }
		    itau = ir + ldwrkr * n__;
		    iwork = itau + n__;

/*                 Compute A=Q*R   
                   (CWorkspace: need N*N+2*N, prefer N*N+N+N*NB)   
                   (RWorkspace: 0) */

		    i__2 = lwork - iwork + 1;
		    cgeqrf(m__,n__, &A(1,1), lda, &WORK(itau), &WORK(iwork)
			    , i__2, &ierr);

/*                 Copy R to WORK(IR) and zero out below it */

		    clacpy("U", n__,n__, &A(1,1), lda, &WORK(ir), ldwrkr);
		    i__2 = n__ - 1;
		    i__3 = n__ - 1;
		    claset("L", i__2, i__3, c_b1, c_b1, &WORK(ir + 1), 
			    ldwrkr);

/*                 Generate Q in A   
                   (CWorkspace: need N*N+2*N, prefer N*N+N+N*NB)   
                   (RWorkspace: 0) */

		    i__2 = lwork - iwork + 1;
		    cungqr(m__, n__,n__, &A(1,1), lda, &WORK(itau), &WORK(
			    iwork), i__2, &ierr);
		    ie = 1;
		    itauq = itau;
		    itaup = itauq + n__;
		    iwork = itaup + n__;

/*                 Bidiagonalize R in WORK(IR)   
                   (CWorkspace: need N*N+3*N, prefer N*N+2*N+2*N*NB)   
                   (RWorkspace: need N) */

		    i__2 = lwork - iwork + 1;
		    cgebrd(n__,n__, &WORK(ir), ldwrkr, &S(1), &RWORK(ie), &
			    WORK(itauq), &WORK(itaup), &WORK(iwork), i__2, &
			    ierr);

/*                 Generate left vectors bidiagonalizing R
   
                   (CWorkspace: need N*N+3*N, prefer N*N+2*N+N*NB)   
                   (RWorkspace: need 0) */

		    i__2 = lwork - iwork + 1;
		    cungbr("Q", n__,n__,n__, &WORK(ir), ldwrkr, &WORK(itauq), 
			    &WORK(iwork), i__2, &ierr);
		    irwork = ie + n__;

/*                 Perform bidiagonal QR iteration, computing left   
                   singular vectors of R in WORK(IR)   
                   (CWorkspace: need N*N)   
                   (RWorkspace: need BDSPAC) */

		    cbdsqr("U", n__, c__0, n__, c__0, &S(1), &RWORK(ie), cdum, 
			    c__1, &WORK(ir), ldwrkr, cdum, c__1, &RWORK(
			    irwork), info);
		    iu = itauq;

/*                 Multiply Q in A by left singular vectors of R in   
                   WORK(IR), storing result in WORK(IU) and copying to A   
                   (CWorkspace: need N*N+N, prefer N*N+M*N)   
                   (RWorkspace: 0) */

		    i__2 = m__;
		    i__3 = ldwrku;
		    for (i = 1; ldwrku < 0 ? i >= m__ : i <= m__; i += ldwrku) {
/* Computing MIN */
			i__4 = m__ - i + 1;
			chunk = min(i__4,ldwrku);
			cgemm("N", "N", chunk, n__,n__, c_b2, &A(i,1), 
				lda, &WORK(ir), ldwrkr, c_b1, &WORK(iu), 
				ldwrku);
			clacpy("F", chunk, n__, &WORK(iu), ldwrku, &A(i,1), lda);
		    }

		} else {

/*                 Insufficient workspace for a fast algorithm */

		    ie = 1;
		    itauq = 1;
		    itaup = itauq + n__;
		    iwork = itaup + n__;

/*                 Bidiagonalize A   
                   (CWorkspace: need 2*N+M, prefer 2*N+(M+N)*NB)   
                   (RWorkspace: N) */

		    i__3 = lwork - iwork + 1;
		    cgebrd(m__,n__, &A(1,1), lda, &S(1), &RWORK(ie), &WORK(
			    itauq), &WORK(itaup), &WORK(iwork), i__3, &ierr);

/*                 Generate left vectors bidiagonalizing A
   
                   (CWorkspace: need 3*N, prefer 2*N+N*NB)
   
                   (RWorkspace: 0) */

		    i__3 = lwork - iwork + 1;
		    cungbr("Q", m__, n__,n__, &A(1,1), lda, &WORK(itauq), &
			    WORK(iwork), i__3, &ierr);
		    irwork = ie + n__;

/*                 Perform bidiagonal QR iteration, computing left   
                   singular vectors of A in A   
                   (CWorkspace: need 0)   
                   (RWorkspace: need BDSPAC) */

		    cbdsqr("U", n__, c__0,m__, c__0, &S(1), &RWORK(ie), cdum, 
			    c__1, &A(1,1), lda, cdum, c__1, &RWORK(
			    irwork), info);

		}

	    } else if (wntuo && wntvas) {

/*              Path 3 (M much larger than N, JOBU='O', JOBVT='S' or 'A')   
                N left singular vectors to be overwritten on A and   
                N right singular vectors to be computed in VT 
*/

		if (lwork >= n__ * n__ + n__ * 3) {

/*                 Sufficient workspace for a fast algorithm */

		    ir = 1;
/* Computing MAX */
		    i__3 = wrkbl, i__2 = lda * n__;
		    if (lwork >= max(i__3,i__2) + lda * n__) {

/*                    WORK(IU) is LDA by N and WORK(IR) is LDA by N */

			ldwrku = lda;
			ldwrkr = lda;
		    } else /* if(complicated condition) */ {
/* Computing MAX */
			i__3 = wrkbl, i__2 = lda * n__;
			if (lwork >= max(i__3,i__2) + n__ * n__) {

/*                    WORK(IU) is LDA by N and WORK(IR) is N by N */

			    ldwrku = lda;
			    ldwrkr = n__;
			} else {

/*                    WORK(IU) is LDWRKU by N and WORK(IR) is N by N */

			    ldwrku = (lwork - n__ * n__) / n__;
			    ldwrkr = n__;
			}
		    }
		    itau = ir + ldwrkr * n__;
		    iwork = itau + n__;

/*                 Compute A=Q*R   
                   (CWorkspace: need N*N+2*N, prefer N*N+N+N*NB)   
                   (RWorkspace: 0) */

		    i__3 = lwork - iwork + 1;
		    cgeqrf(m__,n__, &A(1,1), lda, &WORK(itau), &WORK(iwork)
			    , i__3, &ierr);

/*                 Copy R to VT, zeroing out below it */

		    clacpy("U", n__,n__, &A(1,1), lda, &VT(1,1), 
			    ldvt);
		    i__3 = n__ - 1;
		    i__2 = n__ - 1;
		    claset("L", i__3, i__2, c_b1, c_b1, &VT(2,1),
			     ldvt);

/*                 Generate Q in A   
                   (CWorkspace: need N*N+2*N, prefer N*N+N+N*NB)   
                   (RWorkspace: 0) */

		    i__3 = lwork - iwork + 1;
		    cungqr(m__, n__,n__, &A(1,1), lda, &WORK(itau), &WORK(
			    iwork), i__3, &ierr);
		    ie = 1;
		    itauq = itau;
		    itaup = itauq + n__;
		    iwork = itaup + n__;

/*                 Bidiagonalize R in VT, copying result to WORK(IR)   
                   (CWorkspace: need N*N+3*N, prefer N*N+2*N+2*N*NB)   
                   (RWorkspace: need N) */

		    i__3 = lwork - iwork + 1;
		    cgebrd(n__,n__, &VT(1,1), ldvt, &S(1), &RWORK(ie), &
			    WORK(itauq), &WORK(itaup), &WORK(iwork), i__3, &
			    ierr);
		    clacpy("L", n__,n__, &VT(1,1), ldvt, &WORK(ir), 
			    ldwrkr);

/*                 Generate left vectors bidiagonalizing R in WORK(IR)   
                   (CWorkspace: need N*N+3*N, prefer N*N+2*N+N*NB)   
                   (RWorkspace: 0) */

		    i__3 = lwork - iwork + 1;
		    cungbr("Q", n__,n__,n__, &WORK(ir), ldwrkr, &WORK(itauq), &
			    WORK(iwork), i__3, &ierr);

/*                 Generate right vectors bidiagonalizing R in VT   
                   (CWorkspace: need N*N+3*N-1, prefer N*N+2*N+(N-1)*NB)   
                   (RWorkspace: 0) */

		    i__3 = lwork - iwork + 1;
		    cungbr("P", n__,n__,n__, &VT(1,1), ldvt, &WORK(itaup), 
			    &WORK(iwork), i__3, &ierr);
		    irwork = ie + n__;

/*                 Perform bidiagonal QR iteration, computing left   
                   singular vectors of R in WORK(IR) and computing right   
                   singular vectors of R in VT   
                   (CWorkspace: need N*N)   
                   (RWorkspace: need BDSPAC) */

		    cbdsqr("U", n__,n__,n__, c__0, &S(1), &RWORK(ie), 
                            &VT(1,1), ldvt, &WORK(ir), ldwrkr, cdum, c__1,
			     &RWORK(irwork), info);
		    iu = itauq;

/*                 Multiply Q in A by left singular vectors of R in   
                   WORK(IR), storing result in WORK(IU) and copying to A   
                   (CWorkspace: need N*N+N, prefer N*N+M*N)   
                   (RWorkspace: 0) */

		    i__3 = m__;
		    i__2 = ldwrku;
		    for (i = 1; ldwrku < 0 ? i >= m__ : i <= m__; i += ldwrku) {
/* Computing MIN */
			i__4 = m__ - i + 1;
			chunk = min(i__4,ldwrku);
			cgemm("N", "N", chunk, n__,n__, c_b2, &A(i,1), 
				lda, &WORK(ir), ldwrkr, c_b1, &WORK(iu), 
				ldwrku);
			clacpy("F", chunk, n__, &WORK(iu), ldwrku, &A(i,1), lda);
		    }

		} else {

/*                 Insufficient workspace for a fast algorithm */

		    itau = 1;
		    iwork = itau + n__;

/*                 Compute A=Q*R   
                   (CWorkspace: need 2*N, prefer N+N*NB) 
  
                   (RWorkspace: 0) */

		    i__2 = lwork - iwork + 1;
		    cgeqrf(m__,n__, &A(1,1), lda, &WORK(itau), &WORK(iwork)
			    , i__2, &ierr);

/*                 Copy R to VT, zeroing out below it */

		    clacpy("U", n__,n__, &A(1,1), lda, &VT(1,1), 
			    ldvt);
		    i__2 = n__ - 1;
		    i__3 = n__ - 1;
		    claset("L", i__2, i__3, c_b1, c_b1, &VT(2,1),
			     ldvt);

/*                 Generate Q in A   
                   (CWorkspace: need 2*N, prefer N+N*NB) 
  
                   (RWorkspace: 0) */

		    i__2 = lwork - iwork + 1;
		    cungqr(m__, n__,n__, &A(1,1), lda, &WORK(itau), &WORK(
			    iwork), i__2, &ierr);
		    ie = 1;
		    itauq = itau;
		    itaup = itauq + n__;
		    iwork = itaup + n__;

/*                 Bidiagonalize R in VT   
                   (CWorkspace: need 3*N, prefer 2*N+2*N*NB)   
                   (RWorkspace: N) */

		    i__2 = lwork - iwork + 1;
		    cgebrd(n__,n__, &VT(1,1), ldvt, &S(1), &RWORK(ie), &
			    WORK(itauq), &WORK(itaup), &WORK(iwork), i__2, &
			    ierr);

/*                 Multiply Q in A by left vectors bidiagonalizing R   
                   (CWorkspace: need 2*N+M, prefer 2*N+M*NB)   
                   (RWorkspace: 0) */

		    i__2 = lwork - iwork + 1;
		    cunmbr("Q", "R", "N", m__, n__,n__, &VT(1,1), ldvt, &
			    WORK(itauq), &A(1,1), lda, &WORK(iwork), 
			    i__2, &ierr);

/*                 Generate right vectors bidiagonalizing R in VT   
                   (CWorkspace: need 3*N-1, prefer 2*N+(N-1)*NB)   
                   (RWorkspace: 0) */

		    i__2 = lwork - iwork + 1;
		    cungbr("P", n__,n__,n__, &VT(1,1), ldvt, &WORK(itaup), 
			    &WORK(iwork), i__2, &ierr);
		    irwork = ie + n__;

/*                 Perform bidiagonal QR iteration, computing left   
                   singular vectors of A in A and computing right   
                   singular vectors of A in VT   
                   (CWorkspace: 0)   
                   (RWorkspace: need BDSPAC) */

		    cbdsqr("U", n__, n__,m__, c__0, &S(1), &RWORK(ie), &VT(1,1),
                            ldvt, &A(1,1), lda, cdum, c__1, 
			    &RWORK(irwork), info);

		}

	    } else if (wntus) {

		if (wntvn) {

/*                 Path 4 (M much larger than N, JOBU='S', JOBVT='N')   
                   N left singular vectors to be computed in U and   
                   no right singular vectors to be computed */

		    if (lwork >= n__ * n__ + n__ * 3) {

/*                    Sufficient workspace for a fast algorithm */

			ir = 1;
			if (lwork >= wrkbl + lda * n__) {

/*                       WORK(IR) is LDA by N */

			    ldwrkr = lda;
			} else {

/*                       WORK(IR) is N by N */

			    ldwrkr = n__;
			}
			itau = ir + ldwrkr * n__;
			iwork = itau + n__;

/*                    Compute A=Q*R   
                      (CWorkspace: need N*N+2*N, prefer N*N+N+N*NB)   
                      (RWorkspace: 0) */

			i__2 = lwork - iwork + 1;
			cgeqrf(m__,n__, &A(1,1), lda, &WORK(itau), &WORK(
				iwork), i__2, &ierr);

/*                    Copy R to WORK(IR), zeroing out below it */

			clacpy("U", n__,n__, &A(1,1), lda, &WORK(ir), 
				ldwrkr);
			i__2 = n__ - 1;
			i__3 = n__ - 1;
			claset("L", i__2, i__3, c_b1, c_b1, &WORK(ir + 1)
				, ldwrkr);

/*                    Generate Q in A   
                      (CWorkspace: need N*N+2*N, prefer N*N+N+N*NB)   
                      (RWorkspace: 0) */

			i__2 = lwork - iwork + 1;
			cungqr(m__, n__,n__, &A(1,1), lda, &WORK(itau), &
				WORK(iwork), i__2, &ierr);
			ie = 1;
			itauq = itau;
			itaup = itauq + n__;
			iwork = itaup + n__;

/*                    Bidiagonalize R in WORK(IR)   
                      (CWorkspace: need N*N+3*N, prefer N*N+2*N+2*N*NB)   
                      (RWorkspace: need N) */

			i__2 = lwork - iwork + 1;
			cgebrd(n__,n__, &WORK(ir), ldwrkr, &S(1), &RWORK(ie), &
				WORK(itauq), &WORK(itaup), &WORK(iwork), 
				i__2, &ierr);

/*                    Generate left vectors bidiagonalizing R in WORK(IR)   
                      (CWorkspace: need N*N+3*N, prefer N*N+2*N+N*NB)   
                      (RWorkspace: 0) */

			i__2 = lwork - iwork + 1;
			cungbr("Q", n__,n__,n__, &WORK(ir), ldwrkr, &WORK(itauq)
				, &WORK(iwork), i__2, &ierr);
			irwork = ie + n__;

/*                    Perform bidiagonal QR iteration, computing left   
                      singular vectors of R in WORK(IR)   
                      (CWorkspace: need N*N)   
                      (RWorkspace: need BDSPAC) */

			cbdsqr("U", n__, c__0, n__, c__0, &S(1), &RWORK(ie), 
				cdum, c__1, &WORK(ir), ldwrkr, cdum, c__1, 
				&RWORK(irwork), info);

/*                    Multiply Q in A by left singular vectors of R in   
                      WORK(IR), storing result in U   
                      (CWorkspace: need N*N)   
                      (RWorkspace: 0) */

			cgemm("N", "N", m__, n__,n__, c_b2, &A(1,1), lda, &
				WORK(ir), ldwrkr, c_b1, &U(1,1), ldu);

		    } else {

/*                    Insufficient workspace for a fast algorithm */

			itau = 1;
			iwork = itau + n__;

/*                    Compute A=Q*R, copying result to U   
                      (CWorkspace: need 2*N, prefer N+N*NB)   
                      (RWorkspace: 0) */

			i__2 = lwork - iwork + 1;
			cgeqrf(m__,n__, &A(1,1), lda, &WORK(itau), &WORK(
				iwork), i__2, &ierr);
			clacpy("L", m__, n__, &A(1,1), lda, &U(1,1), 
				ldu);

/*                    Generate Q in U   
                      (CWorkspace: need 2*N, prefer N+N*NB)   
                      (RWorkspace: 0) */

			i__2 = lwork - iwork + 1;
			cungqr(m__, n__,n__, &U(1,1), ldu, &WORK(itau), &
				WORK(iwork), i__2, &ierr);
			ie = 1;
			itauq = itau;
			itaup = itauq + n__;
			iwork = itaup + n__;

/*                    Zero out below R in A */

			i__2 = n__ - 1;
			i__3 = n__ - 1;
			claset("L", i__2, i__3, c_b1, c_b1, &A(2,1), lda);

/*                    Bidiagonalize R in A   
                      (CWorkspace: need 3*N, prefer 2*N+2*N*NB)   
                      (RWorkspace: need N) */

			i__2 = lwork - iwork + 1;
			cgebrd(n__,n__, &A(1,1), lda, &S(1), &RWORK(ie), &
				WORK(itauq), &WORK(itaup), &WORK(iwork), 
				i__2, &ierr);

/*                    Multiply Q in U by left vectors bidiagonalizing R   
                      (CWorkspace: need 2*N+M, prefer 2*N+M*NB)   
                      (RWorkspace: 0) */

			i__2 = lwork - iwork + 1;
			cunmbr("Q", "R", "N", m__, n__,n__, &A(1,1), lda, &
				WORK(itauq), &U(1,1), ldu, &WORK(iwork), 
				i__2, &ierr);
			irwork = ie + n__;

/*                    Perform bidiagonal QR iteration, computing left   
                      singular vectors of A in U   
                      (CWorkspace: 0)   
                      (RWorkspace: need BDSPAC) */

			cbdsqr("U", n__, c__0,m__, c__0, &S(1), &RWORK(ie), 
				cdum, c__1, &U(1,1), ldu, cdum, c__1, &
				RWORK(irwork), info);

		    }

		} else if (wntvo) {

/*                 Path 5 (M much larger than N, JOBU='S', JOBVT='O')   
                   N left singular vectors to be computed in U and   
                   N right singular vectors to be overwritten on A */

		    if (lwork >= (n__ << 1) * n__ + n__ * 3) {

/*                    Sufficient workspace for a fast algorithm */

			iu = 1;
			if (lwork >= wrkbl + (lda << 1) * n__) {

/*                       WORK(IU) is LDA by N and WORK(IR) is LDA by N */

			    ldwrku = lda;
			    ir = iu + ldwrku * n__;
			    ldwrkr = lda;
			} else if (lwork >= wrkbl + (lda + n__) * n__) {

/*                       WORK(IU) is LDA by N and WORK(IR) is N by N */

			    ldwrku = lda;
			    ir = iu + ldwrku * n__;
			    ldwrkr = n__;
			} else {

/*                       WORK(IU) is N by N and WORK(IR) is N by N */

			    ldwrku = n__;
			    ir = iu + ldwrku * n__;
			    ldwrkr = n__;
			}
			itau = ir + ldwrkr * n__;
			iwork = itau + n__;

/*                    Compute A=Q*R   
                      (CWorkspace: need 2*N*N+2*N, prefer 2*N*N+N+N*NB)   
                      (RWorkspace: 0) */

			i__2 = lwork - iwork + 1;
			cgeqrf(m__,n__, &A(1,1), lda, &WORK(itau), &WORK(
				iwork), i__2, &ierr);

/*                    Copy R to WORK(IU), zeroing out below it */

			clacpy("U", n__,n__, &A(1,1), lda, &WORK(iu), 
				ldwrku);
			i__2 = n__ - 1;
			i__3 = n__ - 1;
			claset("L", i__2, i__3, c_b1, c_b1, &WORK(iu + 1)
				, ldwrku);

/*                    Generate Q in A   
                      (CWorkspace: need 2*N*N+2*N, prefer 2*N*N+N+N*NB)   
                      (RWorkspace: 0) */

			i__2 = lwork - iwork + 1;
			cungqr(m__, n__,n__, &A(1,1), lda, &WORK(itau), &
				WORK(iwork), i__2, &ierr);
			ie = 1;
			itauq = itau;
			itaup = itauq + n__;
			iwork = itaup + n__;

/*                    Bidiagonalize R in WORK(IU), copying result to   
                      WORK(IR)   
                      (CWorkspace: need   2*N*N+3*N, 
  
                                   prefer 2*N*N+2*N+2*N*NB)   
                      (RWorkspace: need   N) */

			i__2 = lwork - iwork + 1;
			cgebrd(n__,n__, &WORK(iu), ldwrku, &S(1), &RWORK(ie), &
				WORK(itauq), &WORK(itaup), &WORK(iwork), 
				i__2, &ierr);
			clacpy("U", n__,n__, &WORK(iu), ldwrku, &WORK(ir), 
				ldwrkr);

/*                    Generate left bidiagonalizing vectors in WORK(IU)   
                      (CWorkspace: need 2*N*N+3*N, prefer 2*N*N+2*N+N*NB)   
                      (RWorkspace: 0) */

			i__2 = lwork - iwork + 1;
			cungbr("Q", n__,n__,n__, &WORK(iu), ldwrku, &WORK(itauq)
				, &WORK(iwork), i__2, &ierr);

/*                    Generate right bidiagonalizing vectors in WORK(IR)   
                      (CWorkspace: need   2*N*N+3*N-1,
   
                                   prefer 2*N*N+2*N+(N-1)*NB)   
                      (RWorkspace: 0) */

			i__2 = lwork - iwork + 1;
			cungbr("P", n__,n__,n__, &WORK(ir), ldwrkr, &WORK(itaup)
				, &WORK(iwork), i__2, &ierr);
			irwork = ie + n__;

/*                    Perform bidiagonal QR iteration, computing left   
                      singular vectors of R in WORK(IU) and computing   
                      right singular vectors of R in WORK(IR)   
                      (CWorkspace: need 2*N*N)   
                      (RWorkspace: need BDSPAC) */

			cbdsqr("U", n__,n__,n__, c__0, &S(1), &RWORK(ie), &WORK(
				ir), ldwrkr, &WORK(iu), ldwrku, cdum, c__1,
				 &RWORK(irwork), info);

/*                    Multiply Q in A by left singular vectors of R in   
                      WORK(IU), storing result in U   
                      (CWorkspace: need N*N)   
                      (RWorkspace: 0) */

			cgemm("N", "N", m__, n__,n__, c_b2, &A(1,1), lda, &
				WORK(iu), ldwrku, c_b1, &U(1,1), ldu);

/*                    Copy right singular vectors of R to A   
                      (CWorkspace: need N*N)   
                      (RWorkspace: 0) */

			clacpy("F", n__,n__, &WORK(ir), ldwrkr, &A(1,1), 
				lda);

		    } else {

/*                    Insufficient workspace for a fast algorithm */

			itau = 1;
			iwork = itau + n__;

/*                    Compute A=Q*R, copying result to U   
                      (CWorkspace: need 2*N, prefer N+N*NB)   
                      (RWorkspace: 0) */

			i__2 = lwork - iwork + 1;
			cgeqrf(m__,n__, &A(1,1), lda, &WORK(itau), &WORK(
				iwork), i__2, &ierr);
			clacpy("L", m__, n__, &A(1,1), lda, &U(1,1), 
				ldu);

/*                    Generate Q in U   
                      (CWorkspace: need 2*N, prefer N+N*NB)   
                      (RWorkspace: 0) */

			i__2 = lwork - iwork + 1;
			cungqr(m__, n__,n__, &U(1,1), ldu, &WORK(itau), &
				WORK(iwork), i__2, &ierr);
			ie = 1;
			itauq = itau;
			itaup = itauq + n__;
			iwork = itaup + n__;

/*                    Zero out below R in A */

			i__2 = n__ - 1;
			i__3 = n__ - 1;
			claset("L", i__2, i__3, c_b1, c_b1, &A(2,1), lda);

/*                    Bidiagonalize R in A   
                      (CWorkspace: need 3*N, prefer 2*N+2*N*NB)   
                      (RWorkspace: need N) */

			i__2 = lwork - iwork + 1;
			cgebrd(n__,n__, &A(1,1), lda, &S(1), &RWORK(ie), &
				WORK(itauq), &WORK(itaup), &WORK(iwork), 
				i__2, &ierr);

/*                    Multiply Q in U by left vectors bidiagonalizing R   
                      (CWorkspace: need 2*N+M, prefer 2*N+M*NB)   
                      (RWorkspace: 0) */

			i__2 = lwork - iwork + 1;
			cunmbr("Q", "R", "N", m__, n__,n__, &A(1,1), lda, &
				WORK(itauq), &U(1,1), ldu, &WORK(iwork), 
				i__2, &ierr);

/*                    Generate right vectors bidiagonalizing R in A   
                      (CWorkspace: need 3*N-1, prefer 2*N+(N-1)*NB)   
                      (RWorkspace: 0) */

			i__2 = lwork - iwork + 1;
			cungbr("P", n__,n__,n__, &A(1,1), lda, &WORK(itaup),
				 &WORK(iwork), i__2, &ierr);
			irwork = ie + n__;

/*                    Perform bidiagonal QR iteration, computing left   
                      singular vectors of A in U and computing right   
                      singular vectors of A in A   
                      (CWorkspace: 0)   
                      (RWorkspace: need BDSPAC) */

			cbdsqr("U", n__, n__,m__, c__0, &S(1), &RWORK(ie), 
                                &A(1,1), lda, &U(1,1), ldu, cdum, 
				c__1, &RWORK(irwork), info);

		    }

		} else if (wntvas) {

/*                 Path 6 (M much larger than N, JOBU='S', JOBVT='S'   
                           or 'A')   
                   N left singular vectors to be computed in U and   
                   N right singular vectors to be computed in VT */

		    if (lwork >= n__ * n__ + n__ * 3) {

/*                    Sufficient workspace for a fast algorithm */

			iu = 1;
			if (lwork >= wrkbl + lda * n__) {

/*                       WORK(IU) is LDA by N */

			    ldwrku = lda;
			} else {

/*                       WORK(IU) is N by N */

			    ldwrku = n__;
			}
			itau = iu + ldwrku * n__;
			iwork = itau + n__;

/*                    Compute A=Q*R   
                      (CWorkspace: need N*N+2*N, prefer N*N+N+N*NB)   
                      (RWorkspace: 0) */

			i__2 = lwork - iwork + 1;
			cgeqrf(m__,n__, &A(1,1), lda, &WORK(itau), &WORK(
				iwork), i__2, &ierr);

/*                    Copy R to WORK(IU), zeroing out below it */

			clacpy("U", n__,n__, &A(1,1), lda, &WORK(iu), 
				ldwrku);
			i__2 = n__ - 1;
			i__3 = n__ - 1;
			claset("L", i__2, i__3, c_b1, c_b1, &WORK(iu + 1)
				, ldwrku);

/*                    Generate Q in A   
                      (CWorkspace: need N*N+2*N, prefer N*N+N+N*NB)   
                      (RWorkspace: 0) */

			i__2 = lwork - iwork + 1;
			cungqr(m__, n__,n__, &A(1,1), lda, &WORK(itau), &
				WORK(iwork), i__2, &ierr);
			ie = 1;
			itauq = itau;
			itaup = itauq + n__;
			iwork = itaup + n__;

/*                    Bidiagonalize R in WORK(IU), copying result to VT   
                      (CWorkspace: need N*N+3*N, prefer N*N+2*N+2*N*NB)   
                      (RWorkspace: need N) */

			i__2 = lwork - iwork + 1;
			cgebrd(n__,n__, &WORK(iu), ldwrku, &S(1), &RWORK(ie), &
				WORK(itauq), &WORK(itaup), &WORK(iwork), 
				i__2, &ierr);
			clacpy("U", n__,n__, &WORK(iu), ldwrku, &VT(1,1),
				 ldvt);

/*                    Generate left bidiagonalizing vectors in WORK(IU)   
                      (CWorkspace: need N*N+3*N, prefer N*N+2*N+N*NB)   
                      (RWorkspace: 0) */

			i__2 = lwork - iwork + 1;
			cungbr("Q", n__,n__,n__, &WORK(iu), ldwrku, &WORK(itauq)
				, &WORK(iwork), i__2, &ierr);

/*                    Generate right bidiagonalizing vectors in VT   
                      (CWorkspace: need   N*N+3*N-1, 
  
                                   prefer N*N+2*N+(N-1)*NB)   
                      (RWorkspace: 0) */

			i__2 = lwork - iwork + 1;
			cungbr("P", n__,n__,n__, &VT(1,1), ldvt, &WORK(
				itaup), &WORK(iwork), i__2, &ierr);
			irwork = ie + n__;

/*                    Perform bidiagonal QR iteration, computing left   
                      singular vectors of R in WORK(IU) and computing   
                      right singular vectors of R in VT   
                      (CWorkspace: need N*N)   
                      (RWorkspace: need BDSPAC) */

			cbdsqr("U", n__,n__,n__, c__0, &S(1), &RWORK(ie), 
                               &VT(1,1), ldvt, &WORK(iu), ldwrku, cdum, 
				c__1, &RWORK(irwork), info);

/*                    Multiply Q in A by left singular vectors of R in   
                      WORK(IU), storing result in U   
                      (CWorkspace: need N*N)   
                      (RWorkspace: 0) */

			cgemm("N", "N", m__, n__,n__, c_b2, &A(1,1), lda, &
				WORK(iu), ldwrku, c_b1, &U(1,1), ldu);

		    } else {

/*                    Insufficient workspace for a fast algorithm */

			itau = 1;
			iwork = itau + n__;

/*                    Compute A=Q*R, copying result to U   
                      (CWorkspace: need 2*N, prefer N+N*NB)   
                      (RWorkspace: 0) */

			i__2 = lwork - iwork + 1;
			cgeqrf(m__,n__, &A(1,1), lda, &WORK(itau), &WORK(
				iwork), i__2, &ierr);
			clacpy("L", m__, n__, &A(1,1), lda, &U(1,1), 
				ldu);

/*                    Generate Q in U   
                      (CWorkspace: need 2*N, prefer N+N*NB)   
                      (RWorkspace: 0) */

			i__2 = lwork - iwork + 1;
			cungqr(m__, n__,n__, &U(1,1), ldu, &WORK(itau), &
				WORK(iwork), i__2, &ierr);

/*                    Copy R to VT, zeroing out below it */

			clacpy("U", n__,n__, &A(1,1), lda, &VT(1,1), 
				ldvt);
			i__2 = n__ - 1;
			i__3 = n__ - 1;
			claset("L", i__2, i__3, c_b1, c_b1, &VT(2,1), ldvt);
			ie = 1;
			itauq = itau;
			itaup = itauq + n__;
			iwork = itaup + n__;

/*                    Bidiagonalize R in VT   
                      (CWorkspace: need 3*N, prefer 2*N+2*N*NB)   
                      (RWorkspace: need N) */

			i__2 = lwork - iwork + 1;
			cgebrd(n__,n__, &VT(1,1), ldvt, &S(1), &RWORK(ie),
				 &WORK(itauq), &WORK(itaup), &WORK(iwork), 
				i__2, &ierr);

/*                    Multiply Q in U by left bidiagonalizing vectors   
                      in VT   
                      (CWorkspace: need 2*N+M, prefer 2*N+M*NB)   
                      (RWorkspace: 0) */

			i__2 = lwork - iwork + 1;
			cunmbr("Q", "R", "N", m__, n__,n__, &VT(1,1), ldvt, 
				&WORK(itauq), &U(1,1), ldu, &WORK(iwork),
				 i__2, &ierr);

/*                    Generate right bidiagonalizing vectors in VT   
                      (CWorkspace: need 3*N-1, prefer 2*N+(N-1)*NB)   
                      (RWorkspace: 0) */

			i__2 = lwork - iwork + 1;
			cungbr("P", n__,n__,n__, &VT(1,1), ldvt, &WORK(
				itaup), &WORK(iwork), i__2, &ierr);
			irwork = ie + n__;

/*                    Perform bidiagonal QR iteration, computing left   
                      singular vectors of A in U and computing right   
                      singular vectors of A in VT   
                      (CWorkspace: 0)   
                      (RWorkspace: need BDSPAC) */

			cbdsqr("U", n__, n__,m__, c__0, &S(1), &RWORK(ie), 
                               &VT(1,1), ldvt, &U(1,1), ldu, cdum, 
				c__1, &RWORK(irwork), info);

		    }

		}

	    } else if (wntua) {

		if (wntvn) {

/*                 Path 7 (M much larger than N, JOBU='A', JOBVT='N')   
                   M left singular vectors to be computed in U and   
                   no right singular vectors to be computed   

   Computing MAX */
		    i__2 = n__ + m__, i__3 = n__ * 3;
		    if (lwork >= n__ * n__ + max(i__2,i__3)) {

/*                    Sufficient workspace for a fast algorithm */

			ir = 1;
			if (lwork >= wrkbl + lda * n__) {

/*                       WORK(IR) is LDA by N */

			    ldwrkr = lda;
			} else {

/*                       WORK(IR) is N by N */

			    ldwrkr = n__;
			}
			itau = ir + ldwrkr * n__;
			iwork = itau + n__;

/*                    Compute A=Q*R, copying result to U   
                      (CWorkspace: need N*N+2*N, prefer N*N+N+N*NB)   
                      (RWorkspace: 0) */

			i__2 = lwork - iwork + 1;
			cgeqrf(m__,n__, &A(1,1), lda, &WORK(itau), &WORK(
				iwork), i__2, &ierr);
			clacpy("L", m__, n__, &A(1,1), lda, &U(1,1), 
				ldu);

/*                    Copy R to WORK(IR), zeroing out below it */

			clacpy("U", n__,n__, &A(1,1), lda, &WORK(ir), 
				ldwrkr);
			i__2 = n__ - 1;
			i__3 = n__ - 1;
			claset("L", i__2, i__3, c_b1, c_b1, &WORK(ir + 1)
				, ldwrkr);

/*                    Generate Q in U   
                      (CWorkspace: need N*N+N+M, prefer N*N+N+M*NB)   
                      (RWorkspace: 0) */

			i__2 = lwork - iwork + 1;
			cungqr( m__,m__, n__, &U(1,1), ldu, &WORK(itau), &
				WORK(iwork), i__2, &ierr);
			ie = 1;
			itauq = itau;
			itaup = itauq + n__;
			iwork = itaup + n__;

/*                    Bidiagonalize R in WORK(IR)   
                      (CWorkspace: need N*N+3*N, prefer N*N+2*N+2*N*NB)   
                      (RWorkspace: need N) */

			i__2 = lwork - iwork + 1;
			cgebrd(n__,n__, &WORK(ir), ldwrkr, &S(1), &RWORK(ie), 
				&WORK(itauq), &WORK(itaup), &WORK(iwork), 
				i__2, &ierr);

/*                    Generate left bidiagonalizing vectors in WORK(IR)   
                      (CWorkspace: need N*N+3*N, prefer N*N+2*N+N*NB)   
                      (RWorkspace: 0) */

			i__2 = lwork - iwork + 1;
			cungbr("Q", n__,n__,n__, &WORK(ir), ldwrkr, &WORK(itauq)
				, &WORK(iwork), i__2, &ierr);
			irwork = ie + n__;

/*                    Perform bidiagonal QR iteration, computing left   
                      singular vectors of R in WORK(IR)   
                      (CWorkspace: need N*N)   
                      (RWorkspace: need BDSPAC) */

			cbdsqr("U", n__, c__0, n__, c__0, &S(1), &RWORK(ie), 
				cdum, c__1, &WORK(ir), ldwrkr, cdum, c__1, 
				&RWORK(irwork), info);

/*                    Multiply Q in U by left singular vectors of R in   
                      WORK(IR), storing result in A   
                      (CWorkspace: need N*N)   
                      (RWorkspace: 0) */

			cgemm("N", "N", m__, n__,n__, c_b2, &U(1,1), ldu, &
				WORK(ir), ldwrkr, c_b1, &A(1,1), lda);

/*                    Copy left singular vectors of A from A to U */

			clacpy("F", m__, n__, &A(1,1), lda, &U(1,1), 
				ldu);

		    } else {

/*                    Insufficient workspace for a fast algorithm */

			itau = 1;
			iwork = itau + n__;

/*                    Compute A=Q*R, copying result to U   
                      (CWorkspace: need 2*N, prefer N+N*NB)   
                      (RWorkspace: 0) */

			i__2 = lwork - iwork + 1;
			cgeqrf(m__,n__, &A(1,1), lda, &WORK(itau), &WORK(
				iwork), i__2, &ierr);
			clacpy("L", m__, n__, &A(1,1), lda, &U(1,1), 
				ldu);

/*                    Generate Q in U   
                      (CWorkspace: need N+M, prefer N+M*NB)   
                      (RWorkspace: 0) */

			i__2 = lwork - iwork + 1;
			cungqr( m__,m__, n__, &U(1,1), ldu, &WORK(itau), &
				WORK(iwork), i__2, &ierr);
			ie = 1;
			itauq = itau;
			itaup = itauq + n__;
			iwork = itaup + n__;

/*                    Zero out below R in A */

			i__2 = n__ - 1;
			i__3 = n__ - 1;
			claset("L", i__2, i__3, c_b1, c_b1, &A(2,1), lda);

/*                    Bidiagonalize R in A   
                      (CWorkspace: need 3*N, prefer 2*N+2*N*NB)   
                      (RWorkspace: need N) */

			i__2 = lwork - iwork + 1;
			cgebrd(n__,n__, &A(1,1), lda, &S(1), &RWORK(ie), &
				WORK(itauq), &WORK(itaup), &WORK(iwork), 
				i__2, &ierr);

/*                    Multiply Q in U by left bidiagonalizing vectors   
                      in A   
                      (CWorkspace: need 2*N+M, prefer 2*N+M*NB)   
                      (RWorkspace: 0) */

			i__2 = lwork - iwork + 1;
			cunmbr("Q", "R", "N", m__, n__,n__, &A(1,1), lda, &
				WORK(itauq), &U(1,1), ldu, &WORK(iwork), 
				i__2, &ierr);
			irwork = ie + n__;

/*                    Perform bidiagonal QR iteration, computing left   
                      singular vectors of A in U   
                      (CWorkspace: 0)   
                      (RWorkspace: need BDSPAC) */

			cbdsqr("U", n__, c__0,m__, c__0, &S(1), &RWORK(ie), 
				cdum, c__1, &U(1,1), ldu, cdum, c__1, &
				RWORK(irwork), info);

		    }

		} else if (wntvo) {

/*                 Path 8 (M much larger than N, JOBU='A', JOBVT='O')   
                   M left singular vectors to be computed in U and   
                   N right singular vectors to be overwritten on A   

   Computing MAX */
		    i__2 = n__ + m__, i__3 = n__ * 3;
		    if (lwork >= (n__ << 1) * n__ + max(i__2,i__3)) {

/*                    Sufficient workspace for a fast algorithm */

			iu = 1;
			if (lwork >= wrkbl + (lda << 1) * n__) {

/*                       WORK(IU) is LDA by N and WORK(IR) is LDA by N */

			    ldwrku = lda;
			    ir = iu + ldwrku * n__;
			    ldwrkr = lda;
			} else if (lwork >= wrkbl + (lda + n__) * n__) {

/*                       WORK(IU) is LDA by N and WORK(IR) is N by N */

			    ldwrku = lda;
			    ir = iu + ldwrku * n__;
			    ldwrkr = n__;
			} else {

/*                       WORK(IU) is N by N and WORK(IR) is N by N */

			    ldwrku = n__;
			    ir = iu + ldwrku * n__;
			    ldwrkr = n__;
			}
			itau = ir + ldwrkr * n__;
			iwork = itau + n__;

/*                    Compute A=Q*R, copying result to U   
                      (CWorkspace: need 2*N*N+2*N, prefer 2*N*N+N+N*NB)   
                      (RWorkspace: 0) */

			i__2 = lwork - iwork + 1;
			cgeqrf(m__,n__, &A(1,1), lda, &WORK(itau), &WORK(
				iwork), i__2, &ierr);
			clacpy("L", m__, n__, &A(1,1), lda, &U(1,1), 
				ldu);

/*                    Generate Q in U   
                      (CWorkspace: need 2*N*N+N+M, prefer 2*N*N+N+M*NB)   
                      (RWorkspace: 0) */

			i__2 = lwork - iwork + 1;
			cungqr( m__,m__, n__, &U(1,1), ldu, &WORK(itau), &
				WORK(iwork), i__2, &ierr);

/*                    Copy R to WORK(IU), zeroing out below it */

			clacpy("U", n__,n__, &A(1,1), lda, &WORK(iu), 
				ldwrku);
			i__2 = n__ - 1;
			i__3 = n__ - 1;
			claset("L", i__2, i__3, c_b1, c_b1, &WORK(iu + 1)
				, ldwrku);
			ie = 1;
			itauq = itau;
			itaup = itauq + n__;
			iwork = itaup + n__;

/*                    Bidiagonalize R in WORK(IU), copying result to   
                      WORK(IR)   
                      (CWorkspace: need   2*N*N+3*N, 
  
                                   prefer 2*N*N+2*N+2*N*NB)   
                      (RWorkspace: need   N) */

			i__2 = lwork - iwork + 1;
			cgebrd(n__,n__, &WORK(iu), ldwrku, &S(1), &RWORK(ie), &
				WORK(itauq), &WORK(itaup), &WORK(iwork), 
				i__2, &ierr);
			clacpy("U", n__,n__, &WORK(iu), ldwrku, &WORK(ir), 
				ldwrkr);

/*                    Generate left bidiagonalizing vectors in WORK(IU)   
                      (CWorkspace: need 2*N*N+3*N, prefer 2*N*N+2*N+N*NB)   
                      (RWorkspace: 0) */

			i__2 = lwork - iwork + 1;
			cungbr("Q", n__,n__,n__, &WORK(iu), ldwrku, &WORK(itauq)
				, &WORK(iwork), i__2, &ierr);

/*                    Generate right bidiagonalizing vectors in WORK(IR)   
                      (CWorkspace: need   2*N*N+3*N-1,
   
                                   prefer 2*N*N+2*N+(N-1)*NB)   
                      (RWorkspace: 0) */

			i__2 = lwork - iwork + 1;
			cungbr("P", n__,n__,n__, &WORK(ir), ldwrkr, &WORK(itaup)
				, &WORK(iwork), i__2, &ierr);
			irwork = ie + n__;

/*                    Perform bidiagonal QR iteration, computing left   
                      singular vectors of R in WORK(IU) and computing   
                      right singular vectors of R in WORK(IR)   
                      (CWorkspace: need 2*N*N)   
                      (RWorkspace: need BDSPAC) */

			cbdsqr("U", n__,n__,n__, c__0, &S(1), &RWORK(ie), &WORK(
				ir), ldwrkr, &WORK(iu), ldwrku, cdum, c__1,
				 &RWORK(irwork), info);

/*                    Multiply Q in U by left singular vectors of R in   
                      WORK(IU), storing result in A   
                      (CWorkspace: need N*N)   
                      (RWorkspace: 0) */

			cgemm("N", "N", m__, n__,n__, c_b2, &U(1,1), ldu, &
				WORK(iu), ldwrku, c_b1, &A(1,1), lda);

/*                    Copy left singular vectors of A from A to U */

			clacpy("F", m__, n__, &A(1,1), lda, &U(1,1), 
				ldu);

/*                    Copy right singular vectors of R from WORK(IR) to A */

			clacpy("F", n__,n__, &WORK(ir), ldwrkr, &A(1,1), 
				lda);

		    } else {

/*                    Insufficient workspace for a fast algorithm */

			itau = 1;
			iwork = itau + n__;

/*                    Compute A=Q*R, copying result to U   
                      (CWorkspace: need 2*N, prefer N+N*NB)   
                      (RWorkspace: 0) */

			i__2 = lwork - iwork + 1;
			cgeqrf(m__,n__, &A(1,1), lda, &WORK(itau), &WORK(
				iwork), i__2, &ierr);
			clacpy("L", m__, n__, &A(1,1), lda, &U(1,1), 
				ldu);

/*                    Generate Q in U   
                      (CWorkspace: need N+M, prefer N+M*NB)   
                      (RWorkspace: 0) */

			i__2 = lwork - iwork + 1;
			cungqr( m__,m__, n__, &U(1,1), ldu, &WORK(itau), &
				WORK(iwork), i__2, &ierr);
			ie = 1;
			itauq = itau;
			itaup = itauq + n__;
			iwork = itaup + n__;

/*                    Zero out below R in A */

			i__2 = n__ - 1;
			i__3 = n__ - 1;
			claset("L", i__2, i__3, c_b1, c_b1, &A(2,1), lda);

/*                    Bidiagonalize R in A   
                      (CWorkspace: need 3*N, prefer 2*N+2*N*NB)   
                      (RWorkspace: need N) */

			i__2 = lwork - iwork + 1;
			cgebrd(n__,n__, &A(1,1), lda, &S(1), &RWORK(ie), &
				WORK(itauq), &WORK(itaup), &WORK(iwork), 
				i__2, &ierr);

/*                    Multiply Q in U by left bidiagonalizing vectors   
                      in A   
                      (CWorkspace: need 2*N+M, prefer 2*N+M*NB)   
                      (RWorkspace: 0) */

			i__2 = lwork - iwork + 1;
			cunmbr("Q", "R", "N", m__, n__,n__, &A(1,1), lda, &
				WORK(itauq), &U(1,1), ldu, &WORK(iwork), 
				i__2, &ierr);

/*                    Generate right bidiagonalizing vectors in A   
                      (CWorkspace: need 3*N-1, prefer 2*N+(N-1)*NB)   
                      (RWorkspace: 0) */

			i__2 = lwork - iwork + 1;
			cungbr("P", n__,n__,n__, &A(1,1), lda, &WORK(itaup),
				 &WORK(iwork), i__2, &ierr);
			irwork = ie + n__;

/*                    Perform bidiagonal QR iteration, computing left   
                      singular vectors of A in U and computing right   
                      singular vectors of A in A   
                      (CWorkspace: 0)   
                      (RWorkspace: need BDSPAC) */

			cbdsqr("U", n__, n__,m__, c__0, &S(1), &RWORK(ie), 
                                &A(1,1), lda, &U(1,1), ldu, cdum, 
				c__1, &RWORK(irwork), info);

		    }

		} else if (wntvas) {

/*                 Path 9 (M much larger than N, JOBU='A', JOBVT='S'   
                           or 'A')   
                   M left singular vectors to be computed in U and   
                   N right singular vectors to be computed in VT   

   Computing MAX */
		    i__2 = n__ + m__, i__3 = n__ * 3;
		    if (lwork >= n__ * n__ + max(i__2,i__3)) {

/*                    Sufficient workspace for a fast algorithm */

			iu = 1;
			if (lwork >= wrkbl + lda * n__) {

/*                       WORK(IU) is LDA by N */

			    ldwrku = lda;
			} else {

/*                       WORK(IU) is N by N */

			    ldwrku = n__;
			}
			itau = iu + ldwrku * n__;
			iwork = itau + n__;

/*                    Compute A=Q*R, copying result to U   
                      (CWorkspace: need N*N+2*N, prefer N*N+N+N*NB)   
                      (RWorkspace: 0) */

			i__2 = lwork - iwork + 1;
			cgeqrf(m__,n__, &A(1,1), lda, &WORK(itau), &WORK(
				iwork), i__2, &ierr);
			clacpy("L", m__, n__, &A(1,1), lda, &U(1,1), 
				ldu);

/*                    Generate Q in U   
                      (CWorkspace: need N*N+N+M, prefer N*N+N+M*NB)   
                      (RWorkspace: 0) */

			i__2 = lwork - iwork + 1;
			cungqr( m__,m__, n__, &U(1,1), ldu, &WORK(itau), &
				WORK(iwork), i__2, &ierr);

/*                    Copy R to WORK(IU), zeroing out below it */

			clacpy("U", n__,n__, &A(1,1), lda, &WORK(iu), 
				ldwrku);
			i__2 = n__ - 1;
			i__3 = n__ - 1;
			claset("L", i__2, i__3, c_b1, c_b1, &WORK(iu + 1)
				, ldwrku);
			ie = 1;
			itauq = itau;
			itaup = itauq + n__;
			iwork = itaup + n__;

/*                    Bidiagonalize R in WORK(IU), copying result to VT   
                      (CWorkspace: need N*N+3*N, prefer N*N+2*N+2*N*NB)   
                      (RWorkspace: need N) */

			i__2 = lwork - iwork + 1;
			cgebrd(n__,n__, &WORK(iu), ldwrku, &S(1), &RWORK(ie), &
				WORK(itauq), &WORK(itaup), &WORK(iwork), 
				i__2, &ierr);
			clacpy("U", n__,n__, &WORK(iu), ldwrku, &VT(1,1),
				 ldvt);

/*                    Generate left bidiagonalizing vectors in WORK(IU)   
                      (CWorkspace: need N*N+3*N, prefer N*N+2*N+N*NB)   
                      (RWorkspace: 0) */

			i__2 = lwork - iwork + 1;
			cungbr("Q", n__,n__,n__, &WORK(iu), ldwrku, &WORK(itauq)
				, &WORK(iwork), i__2, &ierr);

/*                    Generate right bidiagonalizing vectors in VT   
                      (CWorkspace: need   N*N+3*N-1, 
  
                                   prefer N*N+2*N+(N-1)*NB)   
                      (RWorkspace: need   0) */

			i__2 = lwork - iwork + 1;
			cungbr("P", n__,n__,n__, &VT(1,1), ldvt, &WORK(
				itaup), &WORK(iwork), i__2, &ierr);
			irwork = ie + n__;

/*                    Perform bidiagonal QR iteration, computing left   
                      singular vectors of R in WORK(IU) and computing   
                      right singular vectors of R in VT   
                      (CWorkspace: need N*N)   
                      (RWorkspace: need BDSPAC) */

			cbdsqr("U", n__,n__,n__, c__0, &S(1), &RWORK(ie), 
                                &VT(1,1), ldvt, &WORK(iu), ldwrku, cdum, 
				c__1, &RWORK(irwork), info);

/*                    Multiply Q in U by left singular vectors of R in   
                      WORK(IU), storing result in A   
                      (CWorkspace: need N*N)   
                      (RWorkspace: 0) */

			cgemm("N", "N", m__, n__,n__, c_b2, &U(1,1), ldu, &
				WORK(iu), ldwrku, c_b1, &A(1,1), lda);

/*                    Copy left singular vectors of A from A to U */

			clacpy("F", m__, n__, &A(1,1), lda, &U(1,1), 
				ldu);

		    } else {

/*                    Insufficient workspace for a fast algorithm */

			itau = 1;
			iwork = itau + n__;

/*                    Compute A=Q*R, copying result to U   
                      (CWorkspace: need 2*N, prefer N+N*NB)   
                      (RWorkspace: 0) */

			i__2 = lwork - iwork + 1;
			cgeqrf(m__,n__, &A(1,1), lda, &WORK(itau), &WORK(
				iwork), i__2, &ierr);
			clacpy("L", m__, n__, &A(1,1), lda, &U(1,1), 
				ldu);

/*                    Generate Q in U   
                      (CWorkspace: need N+M, prefer N+M*NB)   
                      (RWorkspace: 0) */

			i__2 = lwork - iwork + 1;
			cungqr( m__,m__, n__, &U(1,1), ldu, &WORK(itau), &
				WORK(iwork), i__2, &ierr);

/*                    Copy R from A to VT, zeroing out below it */

			clacpy("U", n__,n__, &A(1,1), lda, &VT(1,1), 
				ldvt);
			i__2 = n__ - 1;
			i__3 = n__ - 1;
			claset("L", i__2, i__3, c_b1, c_b1, &VT(2,1), ldvt);
			ie = 1;
			itauq = itau;
			itaup = itauq + n__;
			iwork = itaup + n__;

/*                    Bidiagonalize R in VT   
                      (CWorkspace: need 3*N, prefer 2*N+2*N*NB)   
                      (RWorkspace: need N) */

			i__2 = lwork - iwork + 1;
			cgebrd(n__,n__, &VT(1,1), ldvt, &S(1), &RWORK(ie),
				 &WORK(itauq), &WORK(itaup), &WORK(iwork), 
				i__2, &ierr);

/*                    Multiply Q in U by left bidiagonalizing vectors   
                      in VT   
                      (CWorkspace: need 2*N+M, prefer 2*N+M*NB)   
                      (RWorkspace: 0) */

			i__2 = lwork - iwork + 1;
			cunmbr("Q", "R", "N", m__, n__,n__, &VT(1,1), ldvt, 
				&WORK(itauq), &U(1,1), ldu, &WORK(iwork),
				 i__2, &ierr);

/*                    Generate right bidiagonalizing vectors in VT   
                      (CWorkspace: need 3*N-1, prefer 2*N+(N-1)*NB)   
                      (RWorkspace: 0) */

			i__2 = lwork - iwork + 1;
			cungbr("P", n__,n__,n__, &VT(1,1), ldvt, &WORK(
				itaup), &WORK(iwork), i__2, &ierr);
			irwork = ie + n__;

/*                    Perform bidiagonal QR iteration, computing left   
                      singular vectors of A in U and computing right   
                      singular vectors of A in VT   
                      (CWorkspace: 0)   
                      (RWorkspace: need BDSPAC) */

			cbdsqr("U", n__, n__,m__, c__0, &S(1), &RWORK(ie), 
                                &VT(1,1), ldvt, &U(1,1), ldu, cdum, 
				c__1, &RWORK(irwork), info);

		    }

		}

	    }

	} else {

/*           M .LT. MNTHR   

             Path 10 (M at least N, but not much larger)   
             Reduce to bidiagonal form without QR decomposition */

	    ie = 1;
	    itauq = 1;
	    itaup = itauq + n__;
	    iwork = itaup + n__;

/*           Bidiagonalize A   
             (CWorkspace: need 2*N+M, prefer 2*N+(M+N)*NB)   
             (RWorkspace: need N) */

	    i__2 = lwork - iwork + 1;
	    cgebrd(m__,n__, &A(1,1), lda, &S(1), &RWORK(ie), &WORK(itauq), 
		    &WORK(itaup), &WORK(iwork), i__2, &ierr);
	    if (wntuas) {

/*              If left singular vectors desired in U, copy result to U   
                and generate left bidiagonalizing vectors in U
   
                (CWorkspace: need 2*N+NCU, prefer 2*N+NCU*NB) 
  
                (RWorkspace: 0) */

		clacpy("L", m__, n__, &A(1,1), lda, &U(1,1), ldu);
		if (wntus) {
		    ncu = n__;
		}
		if (wntua) {
		    ncu = m__;
		}
		i__2 = lwork - iwork + 1;
		cungbr("Q",m__, ncu, n__, &U(1,1), ldu, &WORK(itauq), &
			WORK(iwork), i__2, &ierr);
	    }
	    if (wntvas) {

/*              If right singular vectors desired in VT, copy result to   
                VT and generate right bidiagonalizing vectors in VT   
                (CWorkspace: need 3*N-1, prefer 2*N+(N-1)*NB) 
  
                (RWorkspace: 0) */

		clacpy("U", n__,n__, &A(1,1), lda, &VT(1,1), ldvt);
		i__2 = lwork - iwork + 1;
		cungbr("P", n__,n__,n__, &VT(1,1), ldvt, &WORK(itaup), &
			WORK(iwork), i__2, &ierr);
	    }
	    if (wntuo) {

/*              If left singular vectors desired in A, generate left   
                bidiagonalizing vectors in A   
                (CWorkspace: need 3*N, prefer 2*N+N*NB)   
                (RWorkspace: 0) */

		i__2 = lwork - iwork + 1;
		cungbr("Q", m__, n__,n__, &A(1,1), lda, &WORK(itauq), &WORK(
			iwork), i__2, &ierr);
	    }
	    if (wntvo) {

/*              If right singular vectors desired in A, generate right   
                bidiagonalizing vectors in A   
                (CWorkspace: need 3*N-1, prefer 2*N+(N-1)*NB) 
  
                (RWorkspace: 0) */

		i__2 = lwork - iwork + 1;
		cungbr("P", n__,n__,n__, &A(1,1), lda, &WORK(itaup), &WORK(
			iwork), i__2, &ierr);
	    }
	    irwork = ie + n__;
	    if (wntuas || wntuo) {
		nru = m__;
	    }
	    if (wntun) {
		nru = 0;
	    }
	    if (wntvas || wntvo) {
		ncvt = n__;
	    }
	    if (wntvn) {
		ncvt = 0;
	    }
	    if (! wntuo && ! wntvo) {

/*              Perform bidiagonal QR iteration, if desired, computing   
                left singular vectors in U and computing right singular   
                vectors in VT   
                (CWorkspace: 0)   
                (RWorkspace: need BDSPAC) */

		cbdsqr("U", n__, ncvt, nru, c__0, &S(1), &RWORK(ie), 
                        &VT(1,1), ldvt, &U(1,1), ldu, cdum, c__1, &
			RWORK(irwork), info);
	    } else if (! wntuo && wntvo) {

/*              Perform bidiagonal QR iteration, if desired, computing   
                left singular vectors in U and computing right singular   
                vectors in A   
                (CWorkspace: 0)   
                (RWorkspace: need BDSPAC) */

		cbdsqr("U", n__, ncvt, nru, c__0, &S(1), &RWORK(ie), &A(1,1), 
                        lda, &U(1,1), ldu, cdum, c__1, &
			RWORK(irwork), info);
	    } else {

/*              Perform bidiagonal QR iteration, if desired, computing   
                left singular vectors in A and computing right singular   
                vectors in VT   
                (CWorkspace: 0)   
                (RWorkspace: need BDSPAC) */

		cbdsqr("U", n__, ncvt, nru, c__0, &S(1), &RWORK(ie), 
                        &VT(1,1), ldvt, &A(1,1), lda, cdum, c__1, &
			RWORK(irwork), info);
	    }

	}

    } else {

/*        A has more columns than rows. If A has sufficiently more   
          columns than rows, first reduce using the LQ decomposition (if   
          sufficient workspace available) */

	if (n__ >= mnthr) {

	    if (wntvn) {

/*              Path 1t(N much larger than M, JOBVT='N')   
                No right singular vectors to be computed */

		itau = 1;
		iwork = itau + m__;

/*              Compute A=L*Q   
                (CWorkspace: need 2*M, prefer M+M*NB)   
                (RWorkspace: 0) */

		i__2 = lwork - iwork + 1;
		cgelqf(m__,n__, &A(1,1), lda, &WORK(itau), &WORK(iwork), 
			i__2, &ierr);

/*              Zero out above L */

		i__2 = m__ - 1;
		i__3 = m__ - 1;
		claset("U", i__2, i__3, c_b1, c_b1, &A(1,2)
			, lda);
		ie = 1;
		itauq = 1;
		itaup = itauq + m__;
		iwork = itaup + m__;

/*              Bidiagonalize L in A   
                (CWorkspace: need 3*M, prefer 2*M+2*M*NB)   
                (RWorkspace: need M) */

		i__2 = lwork - iwork + 1;
		cgebrd(m__,m__, &A(1,1), lda, &S(1), &RWORK(ie), &WORK(
			itauq), &WORK(itaup), &WORK(iwork), i__2, &ierr);
		if (wntuo || wntuas) {

/*                 If left singular vectors desired, generate Q   
                   (CWorkspace: need 3*M, prefer 2*M+M*NB)
   
                   (RWorkspace: 0) */

		    i__2 = lwork - iwork + 1;
		    cungbr("Q", m__,m__,m__, &A(1,1), lda, &WORK(itauq), &
			    WORK(iwork), i__2, &ierr);
		}
		irwork = ie + m__;
		nru = 0;
		if (wntuo || wntuas) {
		    nru = m__;
		}

/*              Perform bidiagonal QR iteration, computing left singular   
                vectors of A in A if desired   
                (CWorkspace: 0)   
                (RWorkspace: need BDSPAC) */

		cbdsqr("U",m__, c__0, nru, c__0, &S(1), &RWORK(ie), cdum, 
			c__1, &A(1,1), lda, cdum, c__1, &RWORK(irwork), 
			info);

/*              If left singular vectors desired in U, copy them there */

		if (wntuas) {
		    clacpy("F", m__,m__, &A(1,1), lda, &U(1,1), ldu);
		}

	    } else if (wntvo && wntun) {

/*              Path 2t(N much larger than M, JOBU='N', JOBVT='O')   
                M right singular vectors to be overwritten on A and   
                no left singular vectors to be computed */

		if (lwork >= m__ * m__ + m__ * 3) {

/*                 Sufficient workspace for a fast algorithm */

		    ir = 1;
/* Computing MAX */
		    i__2 = wrkbl, i__3 = lda * n__;
		    if (lwork >= max(i__2,i__3) + lda * m__) {

/*                    WORK(IU) is LDA by N and WORK(IR) is LDA by M */

			ldwrku = lda;
			chunk = n__;
			ldwrkr = lda;
		    } else /* if(complicated condition) */ {
/* Computing MAX */
			i__2 = wrkbl, i__3 = lda * n__;
			if (lwork >= max(i__2,i__3) + m__ * m__) {

/*                    WORK(IU) is LDA by N and WORK(IR) is M by M */

			    ldwrku = lda;
			    chunk = n__;
			    ldwrkr = m__;
			} else {

/*                    WORK(IU) is M by CHUNK and WORK(IR) is M by M */

			    ldwrku = m__;
			    chunk = (lwork - m__ * m__) / m__;
			    ldwrkr = m__;
			}
		    }
		    itau = ir + ldwrkr * m__;
		    iwork = itau + m__;

/*                 Compute A=L*Q   
                   (CWorkspace: need M*M+2*M, prefer M*M+M+M*NB)   
                   (RWorkspace: 0) */

		    i__2 = lwork - iwork + 1;
		    cgelqf(m__,n__, &A(1,1), lda, &WORK(itau), &WORK(iwork)
			    , i__2, &ierr);

/*                 Copy L to WORK(IR) and zero out above it */

		    clacpy("L", m__,m__, &A(1,1), lda, &WORK(ir), ldwrkr);
		    i__2 = m__ - 1;
		    i__3 = m__ - 1;
		    claset("U", i__2, i__3, c_b1, c_b1, &WORK(ir + 
			    ldwrkr), ldwrkr);

/*                 Generate Q in A   
                   (CWorkspace: need M*M+2*M, prefer M*M+M+M*NB)   
                   (RWorkspace: 0) */

		    i__2 = lwork - iwork + 1;
		    cunglq(m__,n__,m__, &A(1,1), lda, &WORK(itau), &WORK(
			    iwork), i__2, &ierr);
		    ie = 1;
		    itauq = itau;
		    itaup = itauq + m__;
		    iwork = itaup + m__;

/*                 Bidiagonalize L in WORK(IR)   
                   (CWorkspace: need M*M+3*M, prefer M*M+2*M+2*M*NB)   
                   (RWorkspace: need M) */

		    i__2 = lwork - iwork + 1;
		    cgebrd(m__,m__, &WORK(ir), ldwrkr, &S(1), &RWORK(ie), &
			    WORK(itauq), &WORK(itaup), &WORK(iwork), i__2, &
			    ierr);

/*                 Generate right vectors bidiagonalizing L   
                   (CWorkspace: need M*M+3*M-1, prefer M*M+2*M+(M-1)*NB)   
                   (RWorkspace: 0) */

		    i__2 = lwork - iwork + 1;
		    cungbr("P", m__,m__,m__, &WORK(ir), ldwrkr, &WORK(itaup), &
			    WORK(iwork), i__2, &ierr);
		    irwork = ie + m__;

/*                 Perform bidiagonal QR iteration, computing right   
                   singular vectors of L in WORK(IR)   
                   (CWorkspace: need M*M)   
                   (RWorkspace: need BDSPAC) */

		    cbdsqr("U", m__,m__, c__0, c__0, &S(1), &RWORK(ie), &WORK(
			    ir), ldwrkr, cdum, c__1, cdum, c__1, &RWORK(
			    irwork), info);
		    iu = itauq;

/*                 Multiply right singular vectors of L in WORK(IR) by Q   
                   in A, storing result in WORK(IU) and copying to A                      (CWorkspace: need M*M+M, prefer M*M+M*N)   
                   (RWorkspace: 0) */

		    i__2 = n__;
		    i__3 = chunk;
		    for (i = 1; chunk < 0 ? i >= n__ : i <= n__; i += chunk) {
/* Computing MIN */
			i__4 = n__ - i + 1;
			blk = min(i__4,chunk);
			cgemm("N", "N",m__, blk,m__, c_b2, &WORK(ir), 
				ldwrkr, &A(1,i), lda, c_b1, &WORK(
				iu), ldwrku);
			clacpy("F",m__, blk, &WORK(iu), ldwrku, &A(1,i), lda);
		    }

		} else {

/*                 Insufficient workspace for a fast algorithm */

		    ie = 1;
		    itauq = 1;
		    itaup = itauq + m__;
		    iwork = itaup + m__;

/*                 Bidiagonalize A   
                   (CWorkspace: need 2*M+N, prefer 2*M+(M+N)*NB)   
                   (RWorkspace: need M) */

		    i__3 = lwork - iwork + 1;
		    cgebrd(m__,n__, &A(1,1), lda, &S(1), &RWORK(ie), &WORK(
			    itauq), &WORK(itaup), &WORK(iwork), i__3, &ierr);

/*                 Generate right vectors bidiagonalizing A   
                   (CWorkspace: need 3*M, prefer 2*M+M*NB)
   
                   (RWorkspace: 0) */

		    i__3 = lwork - iwork + 1;
		    cungbr("P", m__,  n__,m__, &A(1,1), lda, &WORK(itaup), &
			    WORK(iwork), i__3, &ierr);
		    irwork = ie + m__;

/*                 Perform bidiagonal QR iteration, computing right   
                   singular vectors of A in A   
                   (CWorkspace: 0)   
                   (RWorkspace: need BDSPAC) */

		    cbdsqr("L", m__, n__, c__0, c__0, &S(1), &RWORK(ie), &A(1,1), lda, cdum, c__1, cdum, c__1, &RWORK(
			    irwork), info);

		}

	    } else if (wntvo && wntuas) {

/*              Path 3t(N much larger than M, JOBU='S' or 'A', JOBVT='O')   
                M right singular vectors to be overwritten on A and   
                M left singular vectors to be computed in U */

		if (lwork >= m__ * m__ + m__ * 3) {

/*                 Sufficient workspace for a fast algorithm */

		    ir = 1;
/* Computing MAX */
		    i__3 = wrkbl, i__2 = lda * n__;
		    if (lwork >= max(i__3,i__2) + lda * m__) {

/*                    WORK(IU) is LDA by N and WORK(IR) is LDA by M */

			ldwrku = lda;
			chunk = n__;
			ldwrkr = lda;
		    } else /* if(complicated condition) */ {
/* Computing MAX */
			i__3 = wrkbl, i__2 = lda * n__;
			if (lwork >= max(i__3,i__2) + m__ * m__) {

/*                    WORK(IU) is LDA by N and WORK(IR) is M by M */

			    ldwrku = lda;
			    chunk = n__;
			    ldwrkr = m__;
			} else {

/*                    WORK(IU) is M by CHUNK and WORK(IR) is M by M */

			    ldwrku = m__;
			    chunk = (lwork - m__ * m__) / m__;
			    ldwrkr = m__;
			}
		    }
		    itau = ir + ldwrkr * m__;
		    iwork = itau + m__;

/*                 Compute A=L*Q   
                   (CWorkspace: need M*M+2*M, prefer M*M+M+M*NB)   
                   (RWorkspace: 0) */

		    i__3 = lwork - iwork + 1;
		    cgelqf(m__,n__, &A(1,1), lda, &WORK(itau), &WORK(iwork)
			    , i__3, &ierr);

/*                 Copy L to U, zeroing about above it */

		    clacpy("L", m__,m__, &A(1,1), lda, &U(1,1), ldu);
		    i__3 = m__ - 1;
		    i__2 = m__ - 1;
		    claset("U", i__3, i__2, c_b1, c_b1, &U(1,2), ldu);

/*                 Generate Q in A   
                   (CWorkspace: need M*M+2*M, prefer M*M+M+M*NB)   
                   (RWorkspace: 0) */

		    i__3 = lwork - iwork + 1;
		    cunglq(m__,n__,m__, &A(1,1), lda, &WORK(itau), &WORK(
			    iwork), i__3, &ierr);
		    ie = 1;
		    itauq = itau;
		    itaup = itauq + m__;
		    iwork = itaup + m__;

/*                 Bidiagonalize L in U, copying result to WORK(IR)   
                   (CWorkspace: need M*M+3*M, prefer M*M+2*M+2*M*NB)   
                   (RWorkspace: need M) */

		    i__3 = lwork - iwork + 1;
		    cgebrd(m__,m__, &U(1,1), ldu, &S(1), &RWORK(ie), &WORK(
			    itauq), &WORK(itaup), &WORK(iwork), i__3, &ierr);
		    clacpy("U", m__,m__, &U(1,1), ldu, &WORK(ir), ldwrkr);

/*                 Generate right vectors bidiagonalizing L in WORK(IR)   
                   (CWorkspace: need M*M+3*M-1, prefer M*M+2*M+(M-1)*NB)   
                   (RWorkspace: 0) */

		    i__3 = lwork - iwork + 1;
		    cungbr("P", m__,m__,m__, &WORK(ir), ldwrkr, &WORK(itaup), &
			    WORK(iwork), i__3, &ierr);

/*                 Generate left vectors bidiagonalizing L in U   
                   (CWorkspace: need M*M+3*M, prefer M*M+2*M+M*NB)   
                   (RWorkspace: 0) */

		    i__3 = lwork - iwork + 1;
		    cungbr("Q", m__,m__,m__, &U(1,1), ldu, &WORK(itauq), &
			    WORK(iwork), i__3, &ierr);
		    irwork = ie + m__;

/*                 Perform bidiagonal QR iteration, computing left   
                   singular vectors of L in U, and computing right   
                   singular vectors of L in WORK(IR)   
                   (CWorkspace: need M*M)   
                   (RWorkspace: need BDSPAC) */

		    cbdsqr("U", m__,m__,m__, c__0, &S(1), &RWORK(ie), &WORK(ir),
			     ldwrkr, &U(1,1), ldu, cdum, c__1, &RWORK(
			    irwork), info);
		    iu = itauq;

/*                 Multiply right singular vectors of L in WORK(IR) by Q   
                   in A, storing result in WORK(IU) and copying to A   
                   (CWorkspace: need M*M+M, prefer M*M+M*N
))   
                   (RWorkspace: 0) */

		    i__3 = n__;
		    i__2 = chunk;
		    for (i = 1; chunk < 0 ? i >= n__ : i <= n__; i += chunk) {
/* Computing MIN */
			i__4 = n__ - i + 1;
			blk = min(i__4,chunk);
			cgemm("N", "N",m__, blk,m__, c_b2, &WORK(ir), 
				ldwrkr, &A(1,i), lda, c_b1, &WORK(
				iu), ldwrku);
			clacpy("F",m__, blk, &WORK(iu), ldwrku, &A(1,i), lda);
		    }

		} else {

/*                 Insufficient workspace for a fast algorithm */

		    itau = 1;
		    iwork = itau + m__;

/*                 Compute A=L*Q   
                   (CWorkspace: need 2*M, prefer M+M*NB) 
  
                   (RWorkspace: 0) */

		    i__2 = lwork - iwork + 1;
		    cgelqf(m__,n__, &A(1,1), lda, &WORK(itau), &WORK(iwork)
			    , i__2, &ierr);

/*                 Copy L to U, zeroing out above it */

		    clacpy("L", m__,m__, &A(1,1), lda, &U(1,1), ldu);
		    i__2 = m__ - 1;
		    i__3 = m__ - 1;
		    claset("U", i__2, i__3, c_b1, c_b1, &U(1,2), ldu);

/*                 Generate Q in A   
                   (CWorkspace: need 2*M, prefer M+M*NB) 
  
                   (RWorkspace: 0) */

		    i__2 = lwork - iwork + 1;
		    cunglq(m__,n__,m__, &A(1,1), lda, &WORK(itau), &WORK(
			    iwork), i__2, &ierr);
		    ie = 1;
		    itauq = itau;
		    itaup = itauq + m__;
		    iwork = itaup + m__;

/*                 Bidiagonalize L in U   
                   (CWorkspace: need 3*M, prefer 2*M+2*M*NB)   
                   (RWorkspace: need M) */

		    i__2 = lwork - iwork + 1;
		    cgebrd(m__,m__, &U(1,1), ldu, &S(1), &RWORK(ie), &WORK(
			    itauq), &WORK(itaup), &WORK(iwork), i__2, &ierr);

/*                 Multiply right vectors bidiagonalizing L by Q in A   
                   (CWorkspace: need 2*M+N, prefer 2*M+N*NB)   
                   (RWorkspace: 0) */

		    i__2 = lwork - iwork + 1;
		    cunmbr("P", "L", "C", m__,  n__,m__, &U(1,1), ldu, &WORK(
			    itaup), &A(1,1), lda, &WORK(iwork), i__2, &
			    ierr);

/*                 Generate left vectors bidiagonalizing L in U   
                   (CWorkspace: need 3*M, prefer 2*M+M*NB)
   
                   (RWorkspace: 0) */

		    i__2 = lwork - iwork + 1;
		    cungbr("Q", m__,m__,m__, &U(1,1), ldu, &WORK(itauq), &
			    WORK(iwork), i__2, &ierr);
		    irwork = ie + m__;

/*                 Perform bidiagonal QR iteration, computing left   
                   singular vectors of A in U and computing right   
                   singular vectors of A in A   
                   (CWorkspace: 0)   
                   (RWorkspace: need BDSPAC) */

		    cbdsqr("U", m__,  n__,m__, c__0, &S(1), &RWORK(ie), &A(1,1), lda, &U(1,1), ldu, cdum, c__1, &
			    RWORK(irwork), info);

		}

	    } else if (wntvs) {

		if (wntun) {

/*                 Path 4t(N much larger than M, JOBU='N', JOBVT='S')   
                   M right singular vectors to be computed in VT and   
                   no left singular vectors to be computed
 */

		    if (lwork >= m__ * m__ + m__ * 3) {

/*                    Sufficient workspace for a fast algorithm */

			ir = 1;
			if (lwork >= wrkbl + lda * m__) {

/*                       WORK(IR) is LDA by M */

			    ldwrkr = lda;
			} else {

/*                       WORK(IR) is M by M */

			    ldwrkr = m__;
			}
			itau = ir + ldwrkr * m__;
			iwork = itau + m__;

/*                    Compute A=L*Q   
                      (CWorkspace: need M*M+2*M, prefer M*M+M+M*NB)   
                      (RWorkspace: 0) */

			i__2 = lwork - iwork + 1;
			cgelqf(m__,n__, &A(1,1), lda, &WORK(itau), &WORK(
				iwork), i__2, &ierr);

/*                    Copy L to WORK(IR), zeroing out above it */

			clacpy("L", m__,m__, &A(1,1), lda, &WORK(ir), 
				ldwrkr);
			i__2 = m__ - 1;
			i__3 = m__ - 1;
			claset("U", i__2, i__3, c_b1, c_b1, &WORK(ir + 
				ldwrkr), ldwrkr);

/*                    Generate Q in A   
                      (CWorkspace: need M*M+2*M, prefer M*M+M+M*NB)   
                      (RWorkspace: 0) */

			i__2 = lwork - iwork + 1;
			cunglq(m__,n__,m__, &A(1,1), lda, &WORK(itau), &
				WORK(iwork), i__2, &ierr);
			ie = 1;
			itauq = itau;
			itaup = itauq + m__;
			iwork = itaup + m__;

/*                    Bidiagonalize L in WORK(IR)   
                      (CWorkspace: need M*M+3*M, prefer M*M+2*M+2*M*NB)   
                      (RWorkspace: need M) */

			i__2 = lwork - iwork + 1;
			cgebrd(m__,m__, &WORK(ir), ldwrkr, &S(1), &RWORK(ie), &
				WORK(itauq), &WORK(itaup), &WORK(iwork), 
				i__2, &ierr);

/*                    Generate right vectors bidiagonalizing L in   
                      WORK(IR)   
                      (CWorkspace: need M*M+3*M, prefer M*M+2*M+(M-1)*NB)   
                      (RWorkspace: 0) */

			i__2 = lwork - iwork + 1;
			cungbr("P", m__,m__,m__, &WORK(ir), ldwrkr, &WORK(itaup)
				, &WORK(iwork), i__2, &ierr);
			irwork = ie + m__;

/*                    Perform bidiagonal QR iteration, computing right   
                      singular vectors of L in WORK(IR)   
                      (CWorkspace: need M*M)   
                      (RWorkspace: need BDSPAC) */

			cbdsqr("U", m__,m__, c__0, c__0, &S(1), &RWORK(ie), &
				WORK(ir), ldwrkr, cdum, c__1, cdum, c__1, &
				RWORK(irwork), info);

/*                    Multiply right singular vectors of L in WORK(IR) by   
                      Q in A, storing result in VT   
                      (CWorkspace: need M*M)   
                      (RWorkspace: 0) */

			cgemm("N", "N", m__,  n__,m__, c_b2, &WORK(ir), ldwrkr, &
				A(1,1), lda, c_b1, &VT(1,1), ldvt);

		    } else {

/*                    Insufficient workspace for a fast algorithm */

			itau = 1;
			iwork = itau + m__;

/*                    Compute A=L*Q   
                      (CWorkspace: need 2*M, prefer M+M*NB)   
                      (RWorkspace: 0) */

			i__2 = lwork - iwork + 1;
			cgelqf(m__,n__, &A(1,1), lda, &WORK(itau), &WORK(
				iwork), i__2, &ierr);

/*                    Copy result to VT */

			clacpy("U", m__, n__, &A(1,1), lda, &VT(1,1), 
				ldvt);

/*                    Generate Q in VT   
                      (CWorkspace: need 2*M, prefer M+M*NB)   
                      (RWorkspace: 0) */

			i__2 = lwork - iwork + 1;
			cunglq(m__,n__,m__, &VT(1,1), ldvt, &WORK(itau), &
				WORK(iwork), i__2, &ierr);
			ie = 1;
			itauq = itau;
			itaup = itauq + m__;
			iwork = itaup + m__;

/*                    Zero out above L in A */

			i__2 = m__ - 1;
			i__3 = m__ - 1;
			claset("U", i__2, i__3, c_b1, c_b1, &A(1,2), lda);

/*                    Bidiagonalize L in A   
                      (CWorkspace: need 3*M, prefer 2*M+2*M*NB)   
                      (RWorkspace: need M) */

			i__2 = lwork - iwork + 1;
			cgebrd(m__,m__, &A(1,1), lda, &S(1), &RWORK(ie), &
				WORK(itauq), &WORK(itaup), &WORK(iwork), 
				i__2, &ierr);

/*                    Multiply right vectors bidiagonalizing L by Q in VT   
                      (CWorkspace: need 2*M+N, prefer 2*M+N*NB)   
                      (RWorkspace: 0) */

			i__2 = lwork - iwork + 1;
			cunmbr("P", "L", "C", m__,  n__,m__, &A(1,1), lda, &
				WORK(itaup), &VT(1,1), ldvt, &WORK(
				iwork), i__2, &ierr);
			irwork = ie + m__;

/*                    Perform bidiagonal QR iteration, computing right   
                      singular vectors of A in VT   
                      (CWorkspace: 0)   
                      (RWorkspace: need BDSPAC) */

			cbdsqr("U", m__, n__, c__0, c__0, &S(1), &RWORK(ie), &
				VT(1,1), ldvt, cdum, c__1, cdum, c__1,
				 &RWORK(irwork), info);

		    }

		} else if (wntuo) {

/*                 Path 5t(N much larger than M, JOBU='O', JOBVT='S')   
                   M right singular vectors to be computed in VT and   
                   M left singular vectors to be overwritten on A */

		    if (lwork >= (m__ << 1) * m__ + m__ * 3) {

/*                    Sufficient workspace for a fast algorithm */

			iu = 1;
			if (lwork >= wrkbl + (lda << 1) * m__) {

/*                       WORK(IU) is LDA by M and WORK(IR) is LDA by M */

			    ldwrku = lda;
			    ir = iu + ldwrku * m__;
			    ldwrkr = lda;
			} else if (lwork >= wrkbl + (lda + m__) * m__) {

/*                       WORK(IU) is LDA by M and WORK(IR) is M by M */

			    ldwrku = lda;
			    ir = iu + ldwrku * m__;
			    ldwrkr = m__;
			} else {

/*                       WORK(IU) is M by M and WORK(IR) is M by M */

			    ldwrku = m__;
			    ir = iu + ldwrku * m__;
			    ldwrkr = m__;
			}
			itau = ir + ldwrkr * m__;
			iwork = itau + m__;

/*                    Compute A=L*Q   
                      (CWorkspace: need 2*M*M+2*M, prefer 2*M*M+M+M*NB)   
                      (RWorkspace: 0) */

			i__2 = lwork - iwork + 1;
			cgelqf(m__,n__, &A(1,1), lda, &WORK(itau), &WORK(
				iwork), i__2, &ierr);

/*                    Copy L to WORK(IU), zeroing out below it */

			clacpy("L", m__,m__, &A(1,1), lda, &WORK(iu), 
				ldwrku);
			i__2 = m__ - 1;
			i__3 = m__ - 1;
			claset("U", i__2, i__3, c_b1, c_b1, &WORK(iu + 
				ldwrku), ldwrku);

/*                    Generate Q in A   
                      (CWorkspace: need 2*M*M+2*M, prefer 2*M*M+M+M*NB)   
                      (RWorkspace: 0) */

			i__2 = lwork - iwork + 1;
			cunglq(m__,n__,m__, &A(1,1), lda, &WORK(itau), &
				WORK(iwork), i__2, &ierr);
			ie = 1;
			itauq = itau;
			itaup = itauq + m__;
			iwork = itaup + m__;

/*                    Bidiagonalize L in WORK(IU), copying result to   
                      WORK(IR)   
                      (CWorkspace: need   2*M*M+3*M, 
  
                                   prefer 2*M*M+2*M+2*M*NB)   
                      (RWorkspace: need   M) */

			i__2 = lwork - iwork + 1;
			cgebrd(m__,m__, &WORK(iu), ldwrku, &S(1), &RWORK(ie), &
				WORK(itauq), &WORK(itaup), &WORK(iwork), 
				i__2, &ierr);
			clacpy("L", m__,m__, &WORK(iu), ldwrku, &WORK(ir), 
				ldwrkr);

/*                    Generate right bidiagonalizing vectors in WORK(IU)   
                      (CWorkspace: need   2*M*M+3*M-1,
   
                                   prefer 2*M*M+2*M+(M-1)*NB)   
                      (RWorkspace: 0) */

			i__2 = lwork - iwork + 1;
			cungbr("P", m__,m__,m__, &WORK(iu), ldwrku, &WORK(itaup)
				, &WORK(iwork), i__2, &ierr);

/*                    Generate left bidiagonalizing vectors in WORK(IR)   
                      (CWorkspace: need 2*M*M+3*M, prefer 2*M*M+2*M+M*NB)   
                      (RWorkspace: 0) */

			i__2 = lwork - iwork + 1;
			cungbr("Q", m__,m__,m__, &WORK(ir), ldwrkr, &WORK(itauq)
				, &WORK(iwork), i__2, &ierr);
			irwork = ie + m__;

/*                    Perform bidiagonal QR iteration, computing left   
                      singular vectors of L in WORK(IR) and computing   
                      right singular vectors of L in WORK(IU)   
                      (CWorkspace: need 2*M*M)   
                      (RWorkspace: need BDSPAC) */

			cbdsqr("U", m__,m__,m__, c__0, &S(1), &RWORK(ie), &WORK(
				iu), ldwrku, &WORK(ir), ldwrkr, cdum, c__1,
				 &RWORK(irwork), info);

/*                    Multiply right singular vectors of L in WORK(IU) by   
                      Q in A, storing result in VT   
                      (CWorkspace: need M*M)   
                      (RWorkspace: 0) */

			cgemm("N", "N", m__,  n__,m__, c_b2, &WORK(iu), ldwrku, &
				A(1,1), lda, c_b1, &VT(1,1), ldvt);

/*                    Copy left singular vectors of L to A   
                      (CWorkspace: need M*M)   
                      (RWorkspace: 0) */

			clacpy("F", m__,m__, &WORK(ir), ldwrkr, &A(1,1), 
				lda);

		    } else {

/*                    Insufficient workspace for a fast algorithm */

			itau = 1;
			iwork = itau + m__;

/*                    Compute A=L*Q, copying result to VT   
                      (CWorkspace: need 2*M, prefer M+M*NB)   
                      (RWorkspace: 0) */

			i__2 = lwork - iwork + 1;
			cgelqf(m__,n__, &A(1,1), lda, &WORK(itau), &WORK(
				iwork), i__2, &ierr);
			clacpy("U", m__, n__, &A(1,1), lda, &VT(1,1), 
				ldvt);

/*                    Generate Q in VT   
                      (CWorkspace: need 2*M, prefer M+M*NB)   
                      (RWorkspace: 0) */

			i__2 = lwork - iwork + 1;
			cunglq(m__,n__,m__, &VT(1,1), ldvt, &WORK(itau), &
				WORK(iwork), i__2, &ierr);
			ie = 1;
			itauq = itau;
			itaup = itauq + m__;
			iwork = itaup + m__;

/*                    Zero out above L in A */

			i__2 = m__ - 1;
			i__3 = m__ - 1;
			claset("U", i__2, i__3, c_b1, c_b1, &A(1,2), lda);

/*                    Bidiagonalize L in A   
                      (CWorkspace: need 3*M, prefer 2*M+2*M*NB)   
                      (RWorkspace: need M) */

			i__2 = lwork - iwork + 1;
			cgebrd(m__,m__, &A(1,1), lda, &S(1), &RWORK(ie), &
				WORK(itauq), &WORK(itaup), &WORK(iwork), 
				i__2, &ierr);

/*                    Multiply right vectors bidiagonalizing L by Q in VT   
                      (CWorkspace: need 2*M+N, prefer 2*M+N*NB)   
                      (RWorkspace: 0) */

			i__2 = lwork - iwork + 1;
			cunmbr("P", "L", "C", m__,  n__,m__, &A(1,1), lda, &
				WORK(itaup), &VT(1,1), ldvt, &WORK(
				iwork), i__2, &ierr);

/*                    Generate left bidiagonalizing vectors of L in A   
                      (CWorkspace: need 3*M, prefer 2*M+M*NB)   
                      (RWorkspace: 0) */

			i__2 = lwork - iwork + 1;
			cungbr("Q", m__,m__,m__, &A(1,1), lda, &WORK(itauq),
				 &WORK(iwork), i__2, &ierr);
			irwork = ie + m__;

/*                    Perform bidiagonal QR iteration, computing left   
                      singular vectors of A in A and computing right   
                      singular vectors of A in VT   
                      (CWorkspace: 0)   
                      (RWorkspace: need BDSPAC) */

			cbdsqr("U", m__,  n__,m__, c__0, &S(1), &RWORK(ie), 
                                 &VT(1,1), ldvt, &A(1,1), lda, cdum, 
				c__1, &RWORK(irwork), info);

		    }

		} else if (wntuas) {

/*                 Path 6t(N much larger than M, JOBU='S' or 'A',   
                           JOBVT='S')   
                   M right singular vectors to be computed in VT and   
                   M left singular vectors to be computed in U */

		    if (lwork >= m__ * m__ + m__ * 3) {

/*                    Sufficient workspace for a fast algorithm */

			iu = 1;
			if (lwork >= wrkbl + lda * m__) {

/*                       WORK(IU) is LDA by N */

			    ldwrku = lda;
			} else {

/*                       WORK(IU) is LDA by M */

			    ldwrku = m__;
			}
			itau = iu + ldwrku * m__;
			iwork = itau + m__;

/*                    Compute A=L*Q   
                      (CWorkspace: need M*M+2*M, prefer M*M+M+M*NB)   
                      (RWorkspace: 0) */

			i__2 = lwork - iwork + 1;
			cgelqf(m__,n__, &A(1,1), lda, &WORK(itau), &WORK(
				iwork), i__2, &ierr);

/*                    Copy L to WORK(IU), zeroing out above it */

			clacpy("L", m__,m__, &A(1,1), lda, &WORK(iu), 
				ldwrku);
			i__2 = m__ - 1;
			i__3 = m__ - 1;
			claset("U", i__2, i__3, c_b1, c_b1, &WORK(iu + 
				ldwrku), ldwrku);

/*                    Generate Q in A   
                      (CWorkspace: need M*M+2*M, prefer M*M+M+M*NB)   
                      (RWorkspace: 0) */

			i__2 = lwork - iwork + 1;
			cunglq(m__,n__,m__, &A(1,1), lda, &WORK(itau), &
				WORK(iwork), i__2, &ierr);
			ie = 1;
			itauq = itau;
			itaup = itauq + m__;
			iwork = itaup + m__;

/*                    Bidiagonalize L in WORK(IU), copying result to U   
                      (CWorkspace: need M*M+3*M, prefer M*M+2*M+2*M*NB)   
                      (RWorkspace: need M) */

			i__2 = lwork - iwork + 1;
			cgebrd(m__,m__, &WORK(iu), ldwrku, &S(1), &RWORK(ie), &
				WORK(itauq), &WORK(itaup), &WORK(iwork), 
				i__2, &ierr);
			clacpy("L", m__,m__, &WORK(iu), ldwrku, &U(1,1), 
				ldu);

/*                    Generate right bidiagonalizing vectors in WORK(IU)   
                      (CWorkspace: need   M*M+3*M-1, 
  
                                   prefer M*M+2*M+(M-1)*NB)   
                      (RWorkspace: 0) */

			i__2 = lwork - iwork + 1;
			cungbr("P", m__,m__,m__, &WORK(iu), ldwrku, &WORK(itaup)
				, &WORK(iwork), i__2, &ierr);

/*                    Generate left bidiagonalizing vectors in U   
                      (CWorkspace: need M*M+3*M, prefer M*M+2*M+M*NB)   
                      (RWorkspace: 0) */

			i__2 = lwork - iwork + 1;
			cungbr("Q", m__,m__,m__, &U(1,1), ldu, &WORK(itauq),
				 &WORK(iwork), i__2, &ierr);
			irwork = ie + m__;

/*                    Perform bidiagonal QR iteration, computing left   
                      singular vectors of L in U and computing right   
                      singular vectors of L in WORK(IU)   
                      (CWorkspace: need M*M)   
                      (RWorkspace: need BDSPAC) */

			cbdsqr("U", m__,m__,m__, c__0, &S(1), &RWORK(ie), &WORK(
				iu), ldwrku, &U(1,1), ldu, cdum, c__1, 
				&RWORK(irwork), info);

/*                    Multiply right singular vectors of L in WORK(IU) by   
                      Q in A, storing result in VT   
                      (CWorkspace: need M*M)   
                      (RWorkspace: 0) */

			cgemm("N", "N", m__,  n__,m__, c_b2, &WORK(iu), ldwrku, &
				A(1,1), lda, c_b1, &VT(1,1), ldvt);

		    } else {

/*                    Insufficient workspace for a fast algorithm */

			itau = 1;
			iwork = itau + m__;

/*                    Compute A=L*Q, copying result to VT   
                      (CWorkspace: need 2*M, prefer M+M*NB)   
                      (RWorkspace: 0) */

			i__2 = lwork - iwork + 1;
			cgelqf(m__,n__, &A(1,1), lda, &WORK(itau), &WORK(
				iwork), i__2, &ierr);
			clacpy("U", m__, n__, &A(1,1), lda, &VT(1,1), 
				ldvt);

/*                    Generate Q in VT   
                      (CWorkspace: need 2*M, prefer M+M*NB)   
                      (RWorkspace: 0) */

			i__2 = lwork - iwork + 1;
			cunglq(m__,n__,m__, &VT(1,1), ldvt, &WORK(itau), &
				WORK(iwork), i__2, &ierr);

/*                    Copy L to U, zeroing out above it */

			clacpy("L", m__,m__, &A(1,1), lda, &U(1,1), 
				ldu);
			i__2 = m__ - 1;
			i__3 = m__ - 1;
			claset("U", i__2, i__3, c_b1, c_b1, &U(1,2), ldu);
			ie = 1;
			itauq = itau;
			itaup = itauq + m__;
			iwork = itaup + m__;

/*                    Bidiagonalize L in U   
                      (CWorkspace: need 3*M, prefer 2*M+2*M*NB)   
                      (RWorkspace: need M) */

			i__2 = lwork - iwork + 1;
			cgebrd(m__,m__, &U(1,1), ldu, &S(1), &RWORK(ie), &
				WORK(itauq), &WORK(itaup), &WORK(iwork), 
				i__2, &ierr);

/*                    Multiply right bidiagonalizing vectors in U by Q   
                      in VT   
                      (CWorkspace: need 2*M+N, prefer 2*M+N*NB)   
                      (RWorkspace: 0) */

			i__2 = lwork - iwork + 1;
			cunmbr("P", "L", "C", m__,  n__,m__, &U(1,1), ldu, &
				WORK(itaup), &VT(1,1), ldvt, &WORK(
				iwork), i__2, &ierr);

/*                    Generate left bidiagonalizing vectors in U   
                      (CWorkspace: need 3*M, prefer 2*M+M*NB)   
                      (RWorkspace: 0) */

			i__2 = lwork - iwork + 1;
			cungbr("Q", m__,m__,m__, &U(1,1), ldu, &WORK(itauq),
				 &WORK(iwork), i__2, &ierr);
			irwork = ie + m__;

/*                    Perform bidiagonal QR iteration, computing left   
                      singular vectors of A in U and computing right   
                      singular vectors of A in VT   
                      (CWorkspace: 0)   
                      (RWorkspace: need BDSPAC) */

			cbdsqr("U", m__,  n__,m__, c__0, &S(1), &RWORK(ie), 
                               &VT(1,1), ldvt, &U(1,1), ldu, cdum, 
				c__1, &RWORK(irwork), info);

		    }

		}

	    } else if (wntva) {

		if (wntun) {

/*                 Path 7t(N much larger than M, JOBU='N', JOBVT='A')   
                   N right singular vectors to be computed in VT and   
                   no left singular vectors to be computed
   

   Computing MAX */
		    i__2 = n__ + m__, i__3 = m__ * 3;
		    if (lwork >= m__ * m__ + max(i__2,i__3)) {

/*                    Sufficient workspace for a fast algorithm */

			ir = 1;
			if (lwork >= wrkbl + lda * m__) {

/*                       WORK(IR) is LDA by M */

			    ldwrkr = lda;
			} else {

/*                       WORK(IR) is M by M */

			    ldwrkr = m__;
			}
			itau = ir + ldwrkr * m__;
			iwork = itau + m__;

/*                    Compute A=L*Q, copying result to VT   
                      (CWorkspace: need M*M+2*M, prefer M*M+M+M*NB)   
                      (RWorkspace: 0) */

			i__2 = lwork - iwork + 1;
			cgelqf(m__,n__, &A(1,1), lda, &WORK(itau), &WORK(
				iwork), i__2, &ierr);
			clacpy("U", m__, n__, &A(1,1), lda, &VT(1,1), 
				ldvt);

/*                    Copy L to WORK(IR), zeroing out above it */

			clacpy("L", m__,m__, &A(1,1), lda, &WORK(ir), 
				ldwrkr);
			i__2 = m__ - 1;
			i__3 = m__ - 1;
			claset("U", i__2, i__3, c_b1, c_b1, &WORK(ir + 
				ldwrkr), ldwrkr);

/*                    Generate Q in VT   
                      (CWorkspace: need M*M+M+N, prefer M*M+M+N*NB)   
                      (RWorkspace: 0) */

			i__2 = lwork - iwork + 1;
			cunglq(n__,n__,m__, &VT(1,1), ldvt, &WORK(itau), &
				WORK(iwork), i__2, &ierr);
			ie = 1;
			itauq = itau;
			itaup = itauq + m__;
			iwork = itaup + m__;

/*                    Bidiagonalize L in WORK(IR)   
                      (CWorkspace: need M*M+3*M, prefer M*M+2*M+2*M*NB)   
                      (RWorkspace: need M) */

			i__2 = lwork - iwork + 1;
			cgebrd(m__,m__, &WORK(ir), ldwrkr, &S(1), &RWORK(ie), &
				WORK(itauq), &WORK(itaup), &WORK(iwork), 
				i__2, &ierr);

/*                    Generate right bidiagonalizing vectors in WORK(IR)   
                      (CWorkspace: need   M*M+3*M-1, 
  
                                   prefer M*M+2*M+(M-1)*NB)   
                      (RWorkspace: 0) */

			i__2 = lwork - iwork + 1;
			cungbr("P", m__,m__,m__, &WORK(ir), ldwrkr, &WORK(itaup)
				, &WORK(iwork), i__2, &ierr);
			irwork = ie + m__;

/*                    Perform bidiagonal QR iteration, computing right   
                      singular vectors of L in WORK(IR)   
                      (CWorkspace: need M*M)   
                      (RWorkspace: need BDSPAC) */

			cbdsqr("U", m__,m__, c__0, c__0, &S(1), &RWORK(ie), &
				WORK(ir), ldwrkr, cdum, c__1, cdum, c__1, &
				RWORK(irwork), info);

/*                    Multiply right singular vectors of L in WORK(IR) by   
                      Q in VT, storing result in A   
                      (CWorkspace: need M*M)   
                      (RWorkspace: 0) */

			cgemm("N", "N", m__,  n__,m__, c_b2, &WORK(ir), ldwrkr, &
				VT(1,1), ldvt, c_b1, &A(1,1), lda);

/*                    Copy right singular vectors of A from A to VT */

			clacpy("F", m__, n__, &A(1,1), lda, &VT(1,1), 
				ldvt);

		    } else {

/*                    Insufficient workspace for a fast algorithm */

			itau = 1;
			iwork = itau + m__;

/*                    Compute A=L*Q, copying result to VT   
                      (CWorkspace: need 2*M, prefer M+M*NB)   
                      (RWorkspace: 0) */

			i__2 = lwork - iwork + 1;
			cgelqf(m__,n__, &A(1,1), lda, &WORK(itau), &WORK(
				iwork), i__2, &ierr);
			clacpy("U", m__, n__, &A(1,1), lda, &VT(1,1), 
				ldvt);

/*                    Generate Q in VT   
                      (CWorkspace: need M+N, prefer M+N*NB)   
                      (RWorkspace: 0) */

			i__2 = lwork - iwork + 1;
			cunglq(n__,n__,m__, &VT(1,1), ldvt, &WORK(itau), &
				WORK(iwork), i__2, &ierr);
			ie = 1;
			itauq = itau;
			itaup = itauq + m__;
			iwork = itaup + m__;

/*                    Zero out above L in A */

			i__2 = m__ - 1;
			i__3 = m__ - 1;
			claset("U", i__2, i__3, c_b1, c_b1, &A(1,2), lda);

/*                    Bidiagonalize L in A   
                      (CWorkspace: need 3*M, prefer 2*M+2*M*NB)   
                      (RWorkspace: need M) */

			i__2 = lwork - iwork + 1;
			cgebrd(m__,m__, &A(1,1), lda, &S(1), &RWORK(ie), &
				WORK(itauq), &WORK(itaup), &WORK(iwork), 
				i__2, &ierr);

/*                    Multiply right bidiagonalizing vectors in A by Q   
                      in VT   
                      (CWorkspace: need 2*M+N, prefer 2*M+N*NB)   
                      (RWorkspace: 0) */

			i__2 = lwork - iwork + 1;
			cunmbr("P", "L", "C", m__,  n__,m__, &A(1,1), lda, &
				WORK(itaup), &VT(1,1), ldvt, &WORK(
				iwork), i__2, &ierr);
			irwork = ie + m__;

/*                    Perform bidiagonal QR iteration, computing right   
                      singular vectors of A in VT   
                      (CWorkspace: 0)   
                      (RWorkspace: need BDSPAC) */

			cbdsqr("U", m__, n__, c__0, c__0, &S(1), &RWORK(ie), &
				VT(1,1), ldvt, cdum, c__1, cdum, c__1,
				 &RWORK(irwork), info);

		    }

		} else if (wntuo) {

/*                 Path 8t(N much larger than M, JOBU='O', JOBVT='A')   
                   N right singular vectors to be computed in VT and   
                   M left singular vectors to be overwritten on A   

   Computing MAX */
		    i__2 = n__ + m__, i__3 = m__ * 3;
		    if (lwork >= (m__ << 1) * m__ + max(i__2,i__3)) {

/*                    Sufficient workspace for a fast algorithm */

			iu = 1;
			if (lwork >= wrkbl + (lda << 1) * m__) {

/*                       WORK(IU) is LDA by M and WORK(IR) is LDA by M */

			    ldwrku = lda;
			    ir = iu + ldwrku * m__;
			    ldwrkr = lda;
			} else if (lwork >= wrkbl + (lda + m__) * m__) {

/*                       WORK(IU) is LDA by M and WORK(IR) is M by M */

			    ldwrku = lda;
			    ir = iu + ldwrku * m__;
			    ldwrkr = m__;
			} else {

/*                       WORK(IU) is M by M and WORK(IR) is M by M */

			    ldwrku = m__;
			    ir = iu + ldwrku * m__;
			    ldwrkr = m__;
			}
			itau = ir + ldwrkr * m__;
			iwork = itau + m__;

/*                    Compute A=L*Q, copying result to VT   
                      (CWorkspace: need 2*M*M+2*M, prefer 2*M*M+M+M*NB)   
                      (RWorkspace: 0) */

			i__2 = lwork - iwork + 1;
			cgelqf(m__,n__, &A(1,1), lda, &WORK(itau), &WORK(
				iwork), i__2, &ierr);
			clacpy("U", m__, n__, &A(1,1), lda, &VT(1,1), 
				ldvt);

/*                    Generate Q in VT   
                      (CWorkspace: need 2*M*M+M+N, prefer 2*M*M+M+N*NB)   
                      (RWorkspace: 0) */

			i__2 = lwork - iwork + 1;
			cunglq(n__,n__,m__, &VT(1,1), ldvt, &WORK(itau), &
				WORK(iwork), i__2, &ierr);

/*                    Copy L to WORK(IU), zeroing out above it */

			clacpy("L", m__,m__, &A(1,1), lda, &WORK(iu), 
				ldwrku);
			i__2 = m__ - 1;
			i__3 = m__ - 1;
			claset("U", i__2, i__3, c_b1, c_b1, &WORK(iu + 
				ldwrku), ldwrku);
			ie = 1;
			itauq = itau;
			itaup = itauq + m__;
			iwork = itaup + m__;

/*                    Bidiagonalize L in WORK(IU), copying result to   
                      WORK(IR)   
                      (CWorkspace: need   2*M*M+3*M, 
  
                                   prefer 2*M*M+2*M+2*M*NB)   
                      (RWorkspace: need   M) */

			i__2 = lwork - iwork + 1;
			cgebrd(m__,m__, &WORK(iu), ldwrku, &S(1), &RWORK(ie), &
				WORK(itauq), &WORK(itaup), &WORK(iwork), 
				i__2, &ierr);
			clacpy("L", m__,m__, &WORK(iu), ldwrku, &WORK(ir), 
				ldwrkr);

/*                    Generate right bidiagonalizing vectors in WORK(IU)   
                      (CWorkspace: need   2*M*M+3*M-1,
   
                                   prefer 2*M*M+2*M+(M-1)*NB)   
                      (RWorkspace: 0) */

			i__2 = lwork - iwork + 1;
			cungbr("P", m__,m__,m__, &WORK(iu), ldwrku, &WORK(itaup)
				, &WORK(iwork), i__2, &ierr);

/*                    Generate left bidiagonalizing vectors in WORK(IR)   
                      (CWorkspace: need 2*M*M+3*M, prefer 2*M*M+2*M+M*NB)   
                      (RWorkspace: 0) */

			i__2 = lwork - iwork + 1;
			cungbr("Q", m__,m__,m__, &WORK(ir), ldwrkr, &WORK(itauq)
				, &WORK(iwork), i__2, &ierr);
			irwork = ie + m__;

/*                    Perform bidiagonal QR iteration, computing left   
                      singular vectors of L in WORK(IR) and computing   
                      right singular vectors of L in WORK(IU)   
                      (CWorkspace: need 2*M*M)   
                      (RWorkspace: need BDSPAC) */

			cbdsqr("U", m__,m__,m__, c__0, &S(1), &RWORK(ie), &WORK(
				iu), ldwrku, &WORK(ir), ldwrkr, cdum, c__1,
				 &RWORK(irwork), info);

/*                    Multiply right singular vectors of L in WORK(IU) by   
                      Q in VT, storing result in A   
                      (CWorkspace: need M*M)   
                      (RWorkspace: 0) */

			cgemm("N", "N", m__,  n__,m__, c_b2, &WORK(iu), ldwrku, &
				VT(1,1), ldvt, c_b1, &A(1,1), lda);

/*                    Copy right singular vectors of A from A to VT */

			clacpy("F", m__, n__, &A(1,1), lda, &VT(1,1), 
				ldvt);

/*                    Copy left singular vectors of A from WORK(IR) to A */

			clacpy("F", m__,m__, &WORK(ir), ldwrkr, &A(1,1), 
				lda);

		    } else {

/*                    Insufficient workspace for a fast algorithm */

			itau = 1;
			iwork = itau + m__;

/*                    Compute A=L*Q, copying result to VT   
                      (CWorkspace: need 2*M, prefer M+M*NB)   
                      (RWorkspace: 0) */

			i__2 = lwork - iwork + 1;
			cgelqf(m__,n__, &A(1,1), lda, &WORK(itau), &WORK(
				iwork), i__2, &ierr);
			clacpy("U", m__, n__, &A(1,1), lda, &VT(1,1), 
				ldvt);

/*                    Generate Q in VT   
                      (CWorkspace: need M+N, prefer M+N*NB)   
                      (RWorkspace: 0) */

			i__2 = lwork - iwork + 1;
			cunglq(n__,n__,m__, &VT(1,1), ldvt, &WORK(itau), &
				WORK(iwork), i__2, &ierr);
			ie = 1;
			itauq = itau;
			itaup = itauq + m__;
			iwork = itaup + m__;

/*                    Zero out above L in A */

			i__2 = m__ - 1;
			i__3 = m__ - 1;
			claset("U", i__2, i__3, c_b1, c_b1, &A(1,2), lda);

/*                    Bidiagonalize L in A   
                      (CWorkspace: need 3*M, prefer 2*M+2*M*NB)   
                      (RWorkspace: need M) */

			i__2 = lwork - iwork + 1;
			cgebrd(m__,m__, &A(1,1), lda, &S(1), &RWORK(ie), &
				WORK(itauq), &WORK(itaup), &WORK(iwork), 
				i__2, &ierr);

/*                    Multiply right bidiagonalizing vectors in A by Q   
                      in VT   
                      (CWorkspace: need 2*M+N, prefer 2*M+N*NB)   
                      (RWorkspace: 0) */

			i__2 = lwork - iwork + 1;
			cunmbr("P", "L", "C", m__,  n__,m__, &A(1,1), lda, &
				WORK(itaup), &VT(1,1), ldvt, &WORK(
				iwork), i__2, &ierr);

/*                    Generate left bidiagonalizing vectors in A   
                      (CWorkspace: need 3*M, prefer 2*M+M*NB)   
                      (RWorkspace: 0) */

			i__2 = lwork - iwork + 1;
			cungbr("Q", m__,m__,m__, &A(1,1), lda, &WORK(itauq),
				 &WORK(iwork), i__2, &ierr);
			irwork = ie + m__;

/*                    Perform bidiagonal QR iteration, computing left   
                      singular vectors of A in A and computing right   
                      singular vectors of A in VT   
                      (CWorkspace: 0)   
                      (RWorkspace: need BDSPAC) */

			cbdsqr("U", m__,  n__,m__, c__0, &S(1), &RWORK(ie), 
                               &VT(1,1), ldvt, &A(1,1), lda, cdum, 
				c__1, &RWORK(irwork), info);

		    }

		} else if (wntuas) {

/*                 Path 9t(N much larger than M, JOBU='S' or 'A',   
                           JOBVT='A')   
                   N right singular vectors to be computed in VT and   
                   M left singular vectors to be computed in U   

   Computing MAX */
		    i__2 = n__ + m__, i__3 = m__ * 3;
		    if (lwork >= m__ * m__ + max(i__2,i__3)) {

/*                    Sufficient workspace for a fast algorithm */

			iu = 1;
			if (lwork >= wrkbl + lda * m__) {

/*                       WORK(IU) is LDA by M */

			    ldwrku = lda;
			} else {

/*                       WORK(IU) is M by M */

			    ldwrku = m__;
			}
			itau = iu + ldwrku * m__;
			iwork = itau + m__;

/*                    Compute A=L*Q, copying result to VT   
                      (CWorkspace: need M*M+2*M, prefer M*M+M+M*NB)   
                      (RWorkspace: 0) */

			i__2 = lwork - iwork + 1;
			cgelqf(m__,n__, &A(1,1), lda, &WORK(itau), &WORK(
				iwork), i__2, &ierr);
			clacpy("U", m__, n__, &A(1,1), lda, &VT(1,1), 
				ldvt);

/*                    Generate Q in VT   
                      (CWorkspace: need M*M+M+N, prefer M*M+M+N*NB)   
                      (RWorkspace: 0) */

			i__2 = lwork - iwork + 1;
			cunglq(n__,n__,m__, &VT(1,1), ldvt, &WORK(itau), &
				WORK(iwork), i__2, &ierr);

/*                    Copy L to WORK(IU), zeroing out above it */

			clacpy("L", m__,m__, &A(1,1), lda, &WORK(iu), 
				ldwrku);
			i__2 = m__ - 1;
			i__3 = m__ - 1;
			claset("U", i__2, i__3, c_b1, c_b1, &WORK(iu + 
				ldwrku), ldwrku);
			ie = 1;
			itauq = itau;
			itaup = itauq + m__;
			iwork = itaup + m__;

/*                    Bidiagonalize L in WORK(IU), copying result to U   
                      (CWorkspace: need M*M+3*M, prefer M*M+2*M+2*M*NB)   
                      (RWorkspace: need M) */

			i__2 = lwork - iwork + 1;
			cgebrd(m__,m__, &WORK(iu), ldwrku, &S(1), &RWORK(ie), &
				WORK(itauq), &WORK(itaup), &WORK(iwork), 
				i__2, &ierr);
			clacpy("L", m__,m__, &WORK(iu), ldwrku, &U(1,1), 
				ldu);

/*                    Generate right bidiagonalizing vectors in WORK(IU)   
                      (CWorkspace: need M*M+3*M, prefer M*M+2*M+(M-1)*NB)   
                      (RWorkspace: 0) */

			i__2 = lwork - iwork + 1;
			cungbr("P", m__,m__,m__, &WORK(iu), ldwrku, &WORK(itaup)
				, &WORK(iwork), i__2, &ierr);

/*                    Generate left bidiagonalizing vectors in U   
                      (CWorkspace: need M*M+3*M, prefer M*M+2*M+M*NB)   
                      (RWorkspace: 0) */

			i__2 = lwork - iwork + 1;
			cungbr("Q", m__,m__,m__, &U(1,1), ldu, &WORK(itauq),
				 &WORK(iwork), i__2, &ierr);
			irwork = ie + m__;

/*                    Perform bidiagonal QR iteration, computing left   
                      singular vectors of L in U and computing right   
                      singular vectors of L in WORK(IU)   
                      (CWorkspace: need M*M)   
                      (RWorkspace: need BDSPAC) */

			cbdsqr("U", m__,m__,m__, c__0, &S(1), &RWORK(ie), &WORK(
				iu), ldwrku, &U(1,1), ldu, cdum, c__1, 
				&RWORK(irwork), info);

/*                    Multiply right singular vectors of L in WORK(IU) by   
                      Q in VT, storing result in A   
                      (CWorkspace: need M*M)   
                      (RWorkspace: 0) */

			cgemm("N", "N", m__,  n__,m__, c_b2, &WORK(iu), ldwrku, &
				VT(1,1), ldvt, c_b1, &A(1,1), lda);

/*                    Copy right singular vectors of A from A to VT */

			clacpy("F", m__, n__, &A(1,1), lda, &VT(1,1), 
				ldvt);

		    } else {

/*                    Insufficient workspace for a fast algorithm */

			itau = 1;
			iwork = itau + m__;

/*                    Compute A=L*Q, copying result to VT   
                      (CWorkspace: need 2*M, prefer M+M*NB)   
                      (RWorkspace: 0) */

			i__2 = lwork - iwork + 1;
			cgelqf(m__,n__, &A(1,1), lda, &WORK(itau), &WORK(
				iwork), i__2, &ierr);
			clacpy("U", m__, n__, &A(1,1), lda, &VT(1,1), 
				ldvt);

/*                    Generate Q in VT   
                      (CWorkspace: need M+N, prefer M+N*NB)   
                      (RWorkspace: 0) */

			i__2 = lwork - iwork + 1;
			cunglq(n__,n__,m__, &VT(1,1), ldvt, &WORK(itau), &
				WORK(iwork), i__2, &ierr);

/*                    Copy L to U, zeroing out above it */

			clacpy("L", m__,m__, &A(1,1), lda, &U(1,1), 
				ldu);
			i__2 = m__ - 1;
			i__3 = m__ - 1;
			claset("U", i__2, i__3, c_b1, c_b1, &U(1,2), ldu);
			ie = 1;
			itauq = itau;
			itaup = itauq + m__;
			iwork = itaup + m__;

/*                    Bidiagonalize L in U   
                      (CWorkspace: need 3*M, prefer 2*M+2*M*NB)   
                      (RWorkspace: need M) */

			i__2 = lwork - iwork + 1;
			cgebrd(m__,m__, &U(1,1), ldu, &S(1), &RWORK(ie), &
				WORK(itauq), &WORK(itaup), &WORK(iwork), 
				i__2, &ierr);

/*                    Multiply right bidiagonalizing vectors in U by Q   
                      in VT   
                      (CWorkspace: need 2*M+N, prefer 2*M+N*NB)   
                      (RWorkspace: 0) */

			i__2 = lwork - iwork + 1;
			cunmbr("P", "L", "C", m__,  n__,m__, &U(1,1), ldu, &
				WORK(itaup), &VT(1,1), ldvt, &WORK(
				iwork), i__2, &ierr);

/*                    Generate left bidiagonalizing vectors in U   
                      (CWorkspace: need 3*M, prefer 2*M+M*NB)   
                      (RWorkspace: 0) */

			i__2 = lwork - iwork + 1;
			cungbr("Q", m__,m__,m__, &U(1,1), ldu, &WORK(itauq),
				 &WORK(iwork), i__2, &ierr);
			irwork = ie + m__;

/*                    Perform bidiagonal QR iteration, computing left   
                      singular vectors of A in U and computing right   
                      singular vectors of A in VT   
                      (CWorkspace: 0)   
                      (RWorkspace: need BDSPAC) */

			cbdsqr("U", m__,  n__,m__, c__0, &S(1), &RWORK(ie), 
                               &VT(1,1), ldvt, &U(1,1), ldu, cdum, 
				c__1, &RWORK(irwork), info);

		    }

		}

	    }

	} else {

/*           N .LT. MNTHR   

             Path 10t(N greater than M, but not much larger)   
             Reduce to bidiagonal form without LQ decomposition */

	    ie = 1;
	    itauq = 1;
	    itaup = itauq + m__;
	    iwork = itaup + m__;

/*           Bidiagonalize A   
             (CWorkspace: need 2*M+N, prefer 2*M+(M+N)*NB)   
             (RWorkspace: M) */

	    i__2 = lwork - iwork + 1;
	    cgebrd(m__,n__, &A(1,1), lda, &S(1), &RWORK(ie), &WORK(itauq), 
		    &WORK(itaup), &WORK(iwork), i__2, &ierr);
	    if (wntuas) {

/*              If left singular vectors desired in U, copy result to U   
                and generate left bidiagonalizing vectors in U
   
                (CWorkspace: need 3*M-1, prefer 2*M+(M-1)*NB) 
  
                (RWorkspace: 0) */

		clacpy("L", m__,m__, &A(1,1), lda, &U(1,1), ldu);
		i__2 = lwork - iwork + 1;
		cungbr("Q",  m__,m__, n__, &U(1,1), ldu, &WORK(itauq), &WORK(
			iwork), i__2, &ierr);
	    }
	    if (wntvas) {

/*              If right singular vectors desired in VT, copy result to   
                VT and generate right bidiagonalizing vectors in VT   
                (CWorkspace: need 2*M+NRVT, prefer 2*M+NRVT*NB)   
                (RWorkspace: 0) */

		clacpy("U", m__, n__, &A(1,1), lda, &VT(1,1), ldvt);
		if (wntva) {
		    nrvt = n__;
		}
		if (wntvs) {
		    nrvt = m__;
		}
		i__2 = lwork - iwork + 1;
		cungbr("P", nrvt, n__,m__, &VT(1,1), ldvt, &WORK(itaup), 
			&WORK(iwork), i__2, &ierr);
	    }
	    if (wntuo) {

/*              If left singular vectors desired in A, generate left   
                bidiagonalizing vectors in A   
                (CWorkspace: need 3*M-1, prefer 2*M+(M-1)*NB) 
  
                (RWorkspace: 0) */

		i__2 = lwork - iwork + 1;
		cungbr("Q",  m__,m__, n__, &A(1,1), lda, &WORK(itauq), &WORK(
			iwork), i__2, &ierr);
	    }
	    if (wntvo) {

/*              If right singular vectors desired in A, generate right   
                bidiagonalizing vectors in A   
                (CWorkspace: need 3*M, prefer 2*M+M*NB)   
                (RWorkspace: 0) */

		i__2 = lwork - iwork + 1;
		cungbr("P", m__,  n__,m__, &A(1,1), lda, &WORK(itaup), &WORK(
			iwork), i__2, &ierr);
	    }
	    irwork = ie + m__;
	    if (wntuas || wntuo) {
		nru = m__;
	    }
	    if (wntun) {
		nru = 0;
	    }
	    if (wntvas || wntvo) {
		ncvt = n__;
	    }
	    if (wntvn) {
		ncvt = 0;
	    }
	    if (! wntuo && ! wntvo) {

/*              Perform bidiagonal QR iteration, if desired, computing   
                left singular vectors in U and computing right singular   
                vectors in VT   
                (CWorkspace: 0)   
                (RWorkspace: need BDSPAC) */

		cbdsqr("L",m__, ncvt, nru, c__0, &S(1), &RWORK(ie), &VT(1,1), ldvt, &U(1,1), ldu, cdum, c__1, &
			RWORK(irwork), info);
	    } else if (! wntuo && wntvo) {

/*              Perform bidiagonal QR iteration, if desired, computing   
                left singular vectors in U and computing right singular   
                vectors in A   
                (CWorkspace: 0)   
                (RWorkspace: need BDSPAC) */

		cbdsqr("L",m__, ncvt, nru, c__0, &S(1), &RWORK(ie), &A(1,1), lda, &U(1,1), ldu, cdum, c__1, &
			RWORK(irwork), info);
	    } else {

/*              Perform bidiagonal QR iteration, if desired, computing   
                left singular vectors in A and computing right singular   
                vectors in VT   
                (CWorkspace: 0)   
                (RWorkspace: need BDSPAC) */

		cbdsqr("L",m__, ncvt, nru, c__0, &S(1), &RWORK(ie), &VT(1,1), ldvt, &A(1,1), lda, cdum, c__1, &
			RWORK(irwork), info);
	    }

	}

    }

/*     Undo scaling if necessary */

    if (iscl == 1) {
	if (anrm > bignum) {
	    slascl("G", c__0, c__0, bignum, anrm, minmn, c__1, &S(1), 
		    minmn, &ierr);
	}
	if (*info != 0 && anrm > bignum) {
	    i__2 = minmn - 1;
	    slascl("G", c__0, c__0, bignum, anrm, i__2, c__1, &RWORK(
		    ie), minmn, &ierr);
	}
	if (anrm < smlnum) {
	    slascl("G", c__0, c__0, smlnum, anrm, minmn, c__1, &S(1), 
		    minmn, &ierr);
	}
	if (*info != 0 && anrm < smlnum) {
	    i__2 = minmn - 1;
	    slascl("G", c__0, c__0, smlnum, anrm, i__2, c__1, &RWORK(
		    ie), minmn, &ierr);
	}
    }

/*     Return optimal workspace in WORK(1) */

    WORK(1).r = (float) maxwrk, WORK(1).i = 0.f;



} 

