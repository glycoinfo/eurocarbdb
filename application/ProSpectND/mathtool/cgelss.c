

#include "complex.h"
#include "mathtool.h"

void cgelss(int m__, int n__, int nrhs, fcomplex *a,
            int lda, fcomplex *b, int ldb, float *s, float rcond, 
	    int *rank, fcomplex *work, int lwork, float *rwork, int *info)
{
/*  -- LAPACK driver routine (version 2.0) --   
       Univ. of Tennessee, Univ. of California Berkeley, NAG Ltd.,   
       Courant Institute, Argonne National Lab, and Rice University   
       September 30, 1994   


    Purpose   
    =======   

    CGELSS computes the minimum norm solution to a complex linear   
    least squares problem:   

    Minimize 2-norm(| b - A*x |).   

    using the singular value decomposition (SVD) of A. A is an M-by-N   
    matrix which may be rank-deficient.   

    Several right hand side vectors b and solution vectors x can be   
    handled in a single call; they are stored as the columns of the   
    M-by-NRHS right hand side matrix B and the N-by-NRHS solution matrix 
  
    X.   

    The effective rank of A is determined by treating as zero those   
    singular values which are less than RCOND times the largest singular 
  
    value.   

    Arguments   
    =========   

    M       (input) INTEGER   
            The number of rows of the matrix A. M >= 0.   

    N       (input) INTEGER   
            The number of columns of the matrix A. N >= 0.   

    NRHS    (input) INTEGER   
            The number of right hand sides, i.e., the number of columns   
            of the matrices B and X. NRHS >= 0.   

    A       (input/output) COMPLEX array, dimension (LDA,N)   
            On entry, the M-by-N matrix A.   
            On exit, the first min(m,n) rows of A are overwritten with   
            its right singular vectors, stored rowwise.   

    LDA     (input) INTEGER   
            The leading dimension of the array A. LDA >= max(1,M).   

    B       (input/output) COMPLEX array, dimension (LDB,NRHS)   
            On entry, the M-by-NRHS right hand side matrix B.   
            On exit, B is overwritten by the N-by-NRHS solution matrix X. 
  
            If m >= n and RANK = n, the residual sum-of-squares for   
            the solution in the i-th column is given by the sum of   
            squares of elements n+1:m in that column.   

    LDB     (input) INTEGER   
            The leading dimension of the array B.  LDB >= max(1,M,N).   

    S       (output) REAL array, dimension (min(M,N))   
            The singular values of A in decreasing order.   
            The condition number of A in the 2-norm = S(1)/S(min(m,n)).   

    RCOND   (input) REAL   
            RCOND is used to determine the effective rank of A.   
            Singular values S(i) <= RCOND*S(1) are treated as zero.   
            If RCOND < 0, machine precision is used instead.   

    RANK    (output) INTEGER   
            The effective rank of A, i.e., the number of singular values 
  
            which are greater than RCOND*S(1).   

    WORK    (workspace/output) COMPLEX array, dimension (LWORK)   
            On exit, if INFO = 0, WORK(1) returns the optimal LWORK.   

    LWORK   (input) INTEGER   
            The dimension of the array WORK. LWORK >= 1, and also:   
            LWORK >=  2*min(M,N) + max(M,N,NRHS)   
            For good performance, LWORK should generally be larger.   

    RWORK   (workspace) REAL array, dimension (5*min(M,N)-1)   

    INFO    (output) INTEGER   
            = 0:  successful exit   
            < 0:  if INFO = -i, the i-th argument had an illegal value.   
            > 0:  the algorithm for computing the SVD failed to converge; 
  
                  if INFO = i, i off-diagonal elements of an intermediate 
  
                  bidiagonal form did not converge to zero.   

    ===================================================================== 
  


       Test the input arguments   

    
   Parameter adjustments   
       Function Body */
    /* Table of constant values */
    static fcomplex c_b1 = {0.f,0.f};
    static fcomplex c_b2 = {1.f,0.f};
    static int c__6 = 6;
    static int c_n1 = -1;
    static int c__1 = 1;
    static int c__0 = 0;
    static float c_b78 = 0.f;
    
    /* System generated locals */
    int i__1, i__2, i__3;
    float r__1;
    /* Local variables */
    static float anrm, bnrm;
    static int itau;
    static fcomplex vdum[1];
    static int i;
    static int iascl, ibscl;
    static int chunk;
    static float sfmin;
    static int minmn, maxmn, itaup, itauq, mnthr, iwork, bl, ie, il;
    static int mm;
    static float bignum;
    static int ldwork;
    static int minwrk, maxwrk;
    static float smlnum;
    static int irwork;
    static float eps, thr;



#define S(I) s[(I)-1]
#define WORK(I) work[(I)-1]
#define RWORK(I) rwork[(I)-1]

#define A(I,J) a[(I)-1 + ((J)-1)* ( lda)]
#define B(I,J) b[(I)-1 + ((J)-1)* ( ldb)]

    *info = 0;
    minmn = min(m__,n__);
    maxmn = max(m__,n__);
    mnthr = ilaenv(c__6, "CGELSS", " ", m__, n__, nrhs, c_n1, 6L, 1L);
    if (m__ < 0) {
	*info = -1;
    } else if (n__ < 0) {
	*info = -2;
    } else if (nrhs < 0) {
	*info = -3;
    } else if (lda < max(1,m__)) {
	*info = -5;
    } else if (ldb < max(1,maxmn)) {
	*info = -7;
    }

/*     Compute workspace   
        (Note: Comments in the code beginning "Workspace:" describe the   
         minimal amount of workspace needed at that point in the code,   
         as well as the preferred amount for good performance.   
         CWorkspace refers to complex workspace, and RWorkspace refers   
         to float workspace. NB refers to the optimal block size for the   
         immediately following subroutine, as returned by ILAENV.) 
     */

    minwrk = 1;
    if (*info == 0 && lwork >= 1) {
	maxwrk = 0;
	mm = m__;
	if (m__ >= n__ && m__ >= mnthr) {

             /*
              * Path 1a - overdetermined, with many more rows than columns   
              * Space needed for CBDSQR is BDSPAC = 5*N-1 
              */

	    mm = n__;
/* Computing MAX */
	    i__1 = maxwrk, i__2 = n__ + n__ * ilaenv(c__1, "CGEQRF", " ", m__, 
		    n__, c_n1, c_n1, 6L, 1L);
	    maxwrk = max(i__1,i__2);
/* Computing MAX */
	    i__1 = maxwrk, i__2 = n__ + nrhs * ilaenv(c__1, "CUNMQR", "LT", 
		    m__, nrhs, n__, c_n1, 6L, 2L);
	    maxwrk = max(i__1,i__2);
	}
	if (m__ >= n__) {

             /* 
              * Path 1 - overdetermined or exactly determined   
              * Space needed for CBDSQR is BDSPC = 7*N+12   

   Computing MAX */
	    i__1 = maxwrk, i__2 = (n__ << 1) + (mm + n__) * ilaenv(c__1, 
		    "CGEBRD", " ", mm, n__, c_n1, c_n1, 6L, 1L);
	    maxwrk = max(i__1,i__2);
/* Computing MAX */
	    i__1 = maxwrk, i__2 = (n__ << 1) + nrhs * ilaenv(c__1, "CUNMBR",
		     "QLC", mm, nrhs, n__, c_n1, 6L, 3L);
	    maxwrk = max(i__1,i__2);
/* Computing MAX */
	    i__1 = maxwrk, i__2 = (n__ << 1) + (n__ - 1) * ilaenv(c__1, "CUN"
		    "GBR", "P", n__, n__, n__, c_n1, 6L, 1L);
	    maxwrk = max(i__1,i__2);
/* Computing MAX */
	    i__1 = maxwrk, i__2 = n__ * nrhs;
	    maxwrk = max(i__1,i__2);
	    minwrk = (n__ << 1) + max(nrhs,m__);
	}
	if (n__ > m__) {
	    minwrk = (m__ << 1) + max(nrhs,n__);
	    if (n__ >= mnthr) {

                /*
                 * Path 2a - underdetermined, with many more columns   
                 * than rows   
                 * Space needed for CBDSQR is BDSPAC = 5*M-1 
                 */

		maxwrk = m__ + m__ * ilaenv(c__1, "CGELQF", " ", m__, n__, c_n1, 
			c_n1, 6L, 1L);
/* Computing MAX */
		i__1 = maxwrk, i__2 = m__ * 3 + m__ * m__ + (m__ << 1) * ilaenv(
			c__1, "CGEBRD", " ", m__, m__, c_n1, c_n1, 6L, 1L);
		maxwrk = max(i__1,i__2);
/* Computing MAX */
		i__1 = maxwrk, i__2 = m__ * 3 + m__ * m__ + nrhs * ilaenv(
			c__1, "CUNMBR", "QLC", m__, nrhs, m__, c_n1, 6L, 3L);
		maxwrk = max(i__1,i__2);
/* Computing MAX */
		i__1 = maxwrk, i__2 = m__ * 3 + m__ * m__ + (m__ - 1) * ilaenv(
			c__1, "CUNGBR", "P", m__, m__, m__, c_n1, 6L, 1L);
		maxwrk = max(i__1,i__2);
		if (nrhs > 1) {
/* Computing MAX */
		    i__1 = maxwrk, i__2 = m__ * m__ + m__ + m__ * nrhs;
		    maxwrk = max(i__1,i__2);
		} else {
/* Computing MAX */
		    i__1 = maxwrk, i__2 = m__ * m__ + (m__ << 1);
		    maxwrk = max(i__1,i__2);
		}
/* Computing MAX */
		i__1 = maxwrk, i__2 = m__ + nrhs * ilaenv(c__1, "CUNMLQ", 
			"LT", n__, nrhs, m__, c_n1, 6L, 2L);
		maxwrk = max(i__1,i__2);
	    } else {

                /*
                 * Path 2 - underdetermined   
                 * Space needed for CBDSQR is BDSPAC = 5*M-1 
                 */

		maxwrk = (m__ << 1) + (n__ + m__) * ilaenv(c__1, "CGEBRD", 
			" ", m__, n__, c_n1, c_n1, 6L, 1L);
/* Computing MAX */
		i__1 = maxwrk, i__2 = (m__ << 1) + nrhs * ilaenv(c__1, 
			"CUNMBR", "QLT", m__, nrhs, m__, c_n1, 6L, 3L);
		maxwrk = max(i__1,i__2);
/* Computing MAX */
		i__1 = maxwrk, i__2 = (m__ << 1) + m__ * ilaenv(c__1, "CUNGBR"
			, "P", m__, n__, m__, c_n1, 6L, 1L);
		maxwrk = max(i__1,i__2);
/* Computing MAX */
		i__1 = maxwrk, i__2 = n__ * nrhs;
		maxwrk = max(i__1,i__2);
	    }
	}
	minwrk = max(minwrk,1);
	maxwrk = max(minwrk,maxwrk);
	WORK(1).r = (float) maxwrk, WORK(1).i = 0.f;
    }

    if (lwork < minwrk) {
	*info = -12;
    }
    if (*info != 0) {
	i__1 = -(*info);
	return;
    }

    /*
     *     Quick return if possible 
     */

    if (m__ == 0 || n__ == 0) {
	*rank = 0;
	return;
    }

    /*
     *     Get machine parameters 
     */

    eps = slamch("P");
    sfmin = slamch("S");
    smlnum = sfmin / eps;
    bignum = 1.f / smlnum;
    slabad(&smlnum, &bignum);

    /*
     *  Scale A if max element outside range [SMLNUM,BIGNUM] 
     */

    anrm = clange("M", m__, n__, &A(1,1), lda, &RWORK(1));
    iascl = 0;
    if (anrm > 0.f && anrm < smlnum) {

        /*
         *        Scale matrix norm up to SMLNUM 
         */

	clascl("G", c__0, c__0, anrm, smlnum, m__, n__, &A(1,1), lda, 
		info);
	iascl = 1;
    } else if (anrm > bignum) {

        /*
         *        Scale matrix norm down to BIGNUM 
         */

	clascl("G", c__0, c__0, anrm, bignum, m__, n__, &A(1,1), lda, 
		info);
	iascl = 2;
    } else if (anrm == 0.f) {

        /*
         *        Matrix all zero. Return zero solution. 
         */

	i__1 = max(m__,n__);
	claset("F", i__1, nrhs, c_b1, c_b1, &B(1,1), ldb);
	slaset("F", minmn, c__1, c_b78, c_b78, &S(1), minmn);
	*rank = 0;
	goto L70;
    }

    /*
     *     Scale B if max element outside range [SMLNUM,BIGNUM] 
     */

    bnrm = clange("M", m__, nrhs, &B(1,1), ldb, &RWORK(1));
    ibscl = 0;
    if (bnrm > 0.f && bnrm < smlnum) {

        /*
         *        Scale matrix norm up to SMLNUM 
         */
  
	clascl("G", c__0, c__0, bnrm, smlnum, m__, nrhs, &B(1,1), ldb,
		 info);
	ibscl = 1;
    } else if (bnrm > bignum) {

        /*
         *        Scale matrix norm down to BIGNUM 
         */

	clascl("G", c__0, c__0, bnrm, bignum, m__, nrhs, &B(1,1), ldb,
		 info);
	ibscl = 2;
    }

    /*
     *     Overdetermined case 
     */

    if (m__ >= n__) {

        /*
         *        Path 1 - overdetermined or exactly determined 
         */

	mm = m__;
	if (m__ >= mnthr) {

            /*
             *   Path 1a - overdetermined, with many more rows than columns 
             */

	    mm = n__;
	    itau = 1;
	    iwork = itau + n__;

            /* Compute A=Q*R   
             * (CWorkspace: need 2*N, prefer N+N*NB)   
             * (RWorkspace: none) 
             */

	    i__1 = lwork - iwork + 1;
	    cgeqrf(m__, n__, &A(1,1), lda, &WORK(itau), &WORK(iwork), i__1,
		     info);

            /*
             * Multiply B by transpose(Q)   
             * (CWorkspace: need N+NRHS, prefer N+NRHS*NB)   
             * (RWorkspace: none) 
             */

	    i__1 = lwork - iwork + 1;
	    cunmqr("L", "C", m__, nrhs, n__, &A(1,1), lda, &WORK(itau), 
                   &B(1,1), ldb, &WORK(iwork), i__1, info);

            /*
             *           Zero out below R 
             */

	    if (n__ > 1) {
		i__1 = n__ - 1;
		i__2 = n__ - 1;
		claset("L", i__1, i__2, c_b1, c_b1, &A(2,1), lda);
	    }
	}

	ie = 1;
	itauq = 1;
	itaup = itauq + n__;
	iwork = itaup + n__;

        /*  Bidiagonalize R in A   
         *  (CWorkspace: need 2*N+MM, prefer 2*N+(MM+N)*NB)   
         *  (RWorkspace: need N) 
         */

	i__1 = lwork - iwork + 1;
	cgebrd(mm, n__, &A(1,1), lda, &S(1), &RWORK(ie), &WORK(itauq),
	       &WORK(itaup), &WORK(iwork), i__1, info);

           /* 
            * Multiply B by transpose of left bidiagonalizing vectors of R
            * (CWorkspace: need 2*N+NRHS, prefer 2*N+NRHS*NB)   
            * (RWorkspace: none) 
            */

	i__1 = lwork - iwork + 1;
	cunmbr("Q", "L", "C", mm, nrhs, n__, &A(1,1), lda, &WORK(itauq), 
	        &B(1,1), ldb, &WORK(iwork), i__1, info);

        /* 
         * Generate right bidiagonalizing vectors of R in A   
         * (CWorkspace: need 3*N-1, prefer 2*N+(N-1)*NB)   
         * (RWorkspace: none) 
         */

	i__1 = lwork - iwork + 1;
	cungbr("P", n__, n__, n__, &A(1,1), lda, &WORK(itaup), &WORK(iwork),
		i__1, info);
	irwork = ie + n__;

        /*
         *  Perform bidiagonal QR iteration   
         *  multiply B by transpose of left singular vectors   
         *  compute right singular vectors in A   
         * (CWorkspace: none)   
         * (RWorkspace: need BDSPAC) 
         */

	cbdsqr("U", n__, n__, c__0, nrhs, &S(1), &RWORK(ie), &A(1,1), lda, 
		vdum, c__1, &B(1,1), ldb, &RWORK(irwork), info);
	if (*info != 0) {
	    goto L70;
	}

        /*
         *   Multiply B by reciprocals of singular values   
         *   Computing MAX 
         */
	r__1 = rcond * S(1);
	thr = max(r__1,sfmin);
	if (rcond < 0.f) {
/* Computing MAX */
	    r__1 = eps * S(1);
	    thr = max(r__1,sfmin);
	}
	*rank = 0;
	i__1 = n__;
	for (i = 1; i <= n__; ++i) {
	    if (S(i) > thr) {
		csrscl(nrhs, S(i), &B(i,1), ldb);
		++(*rank);
	    } else {
		claset("F", c__1, nrhs, c_b1, c_b1, &B(i,1), ldb);
	    }
	}

        /*
         *   Multiply B by right singular vectors   
         *   (CWorkspace: need N, prefer N*NRHS)   
         *   (RWorkspace: none) 
         */

	if (lwork >= ldb * nrhs && nrhs > 1) {
	    cgemm("C", "N", n__, nrhs, n__, c_b2, &A(1,1), lda, &B(1,1), 
                  ldb, c_b1, &WORK(1), ldb);
	    clacpy("G", n__, nrhs, &WORK(1), ldb, &B(1,1), ldb);
	} else if (nrhs > 1) {
	    chunk = lwork / n__;
	    i__1 = nrhs;
	    i__2 = chunk;
	    for (i = 1; chunk < 0 ? i >= nrhs : i <= nrhs; i += chunk) {
/* Computing MIN */
		i__3 = nrhs - i + 1;
		bl = min(i__3,chunk);
		cgemm("C", "N", n__, bl, n__, c_b2, &A(1,1), lda, &B(1,1), 
                       ldb, c_b1, &WORK(1), n__);
		clacpy("G", n__, bl, &WORK(1), n__, &B(1,1), ldb);
	    }
	} else {
	    cgemv("C", n__, n__, c_b2, &A(1,1), lda, &B(1,1), c__1,
		    c_b1, &WORK(1), c__1);
	    ccopy(n__, &WORK(1), c__1, &B(1,1), c__1);
	}

    } else /* if(complicated condition) */ {
/* Computing MAX */
	i__2 = max(m__,nrhs), i__1 = n__ - (m__ << 1);
	if (n__ >= mnthr && lwork >= m__ * 3 + m__ * m__ + max(i__2,i__1)) {

            /*
             *   Underdetermined case, M much less than N   
             *   Path 2a - underdetermined, with many more columns than rows   
             *   and sufficient workspace for an efficient algorithm 
             */

	    ldwork = m__;
/* Computing MAX */
	    i__2 = max(m__,nrhs), i__1 = n__ - (m__ << 1);
	    if (lwork >= m__ * 3 + m__ * lda + max(i__2,i__1)) {
		ldwork = lda;
	    }
	    itau = 1;
	    iwork = m__ + 1;

            /*  Compute A=L*Q   
             *  (CWorkspace: need 2*M, prefer M+M*NB)   
             *  (RWorkspace: none) 
             */

	    i__2 = lwork - iwork + 1;
	    cgelqf(m__, n__, &A(1,1), lda, &WORK(itau), &WORK(iwork), i__2,
		   info);
	    il = iwork;

            /*
             *    Copy L to WORK(IL), zeroing out above it 
             */

	    clacpy("L", m__, m__, &A(1,1), lda, &WORK(il), ldwork);
	    i__2 = m__ - 1;
	    i__1 = m__ - 1;
	    claset("U", i__2, i__1, c_b1, c_b1, &WORK(il + ldwork), 
		    ldwork);
	    ie = 1;
	    itauq = il + ldwork * m__;
	    itaup = itauq + m__;
	    iwork = itaup + m__;

            /* Bidiagonalize L in WORK(IL)   
             * (CWorkspace: need M*M+4*M, prefer M*M+3*M+2*M*NB)   
             * (RWorkspace: need M) 
             */

	    i__2 = lwork - iwork + 1;
	    cgebrd(m__, m__, &WORK(il), ldwork, &S(1), &RWORK(ie), &WORK(itauq),
		   &WORK(itaup), &WORK(iwork), i__2, info);

            /*
             * Multiply B by transpose of left bidiagonalizing vectors of L   
             * (CWorkspace: need M*M+3*M+NRHS, prefer M*M+3*M+NRHS*NB) 
             * (RWorkspace: none) 
             */

	    i__2 = lwork - iwork + 1;
	    cunmbr("Q", "L", "C", m__, nrhs, m__, &WORK(il), ldwork, 
                    &WORK(itauq), &B(1,1), ldb, &WORK(iwork), i__2, info);

            /*
             *  Generate right bidiagonalizing vectors of R in WORK(IL) 
             *  (CWorkspace: need M*M+4*M-1, prefer M*M+3*M+(M-1)*NB)   
             *  (RWorkspace: none) 
             */

	    i__2 = lwork - iwork + 1;
	    cungbr("P", m__, m__, m__, &WORK(il), ldwork, &WORK(itaup), &WORK(
		    iwork), i__2, info);
	    irwork = ie + m__;

            /* 
             * Perform bidiagonal QR iteration, computing right singular   
             * vectors of L in WORK(IL) and multiplying B by transpose of   
             * left singular vectors   
             * (CWorkspace: need M*M)   
             * (RWorkspace: need BDSPAC) 
             */

	    cbdsqr("U", m__, m__, c__0, nrhs, &S(1), &RWORK(ie), &WORK(il), 
		    ldwork, &A(1,1), lda, &B(1,1), ldb, &RWORK(
		    irwork), info);
	    if (*info != 0) {
		goto L70;
	    }

            /*
             * Multiply B by reciprocals of singular values   
             * Computing MAX 
             */
	    r__1 = rcond * S(1);
	    thr = max(r__1,sfmin);
	    if (rcond < 0.f) {
/* Computing MAX */
		r__1 = eps * S(1);
		thr = max(r__1,sfmin);
	    }
	    *rank = 0;
	    i__2 = m__;
	    for (i = 1; i <= m__; ++i) {
		if (S(i) > thr) {
		    csrscl(nrhs, S(i), &B(i,1), ldb);
		    ++(*rank);
		} else {
		    claset("F", c__1, nrhs, c_b1, c_b1, &B(i,1), 
			    ldb);
		}
	    }
	    iwork = il + m__ * ldwork;

            /* Multiply B by right singular vectors of L in WORK(IL)   
             * (CWorkspace: need M*M+2*M, prefer M*M+M+M*NRHS)   
             * (RWorkspace: none) 
             */

	    if (lwork >= ldb * nrhs + iwork - 1 && nrhs > 1) {
		cgemm("C", "N", m__, nrhs, m__, c_b2, &WORK(il), ldwork,
                      &B(1,1), ldb, c_b1, &WORK(iwork), ldb);
		clacpy("G", m__, nrhs, &WORK(iwork), ldb, &B(1,1), ldb);
	    } else if (nrhs > 1) {
		chunk = (lwork - iwork + 1) / m__;
		i__2 = nrhs;
		i__1 = chunk;
		for (i = 1; chunk < 0 ? i >= nrhs : i <= nrhs; i += chunk) {
/* Computing MIN */
		    i__3 = nrhs - i + 1;
		    bl = min(i__3,chunk);
		    cgemm("C", "N", m__, bl, m__, c_b2, &WORK(il), ldwork, 
                           &B(1,i), ldb, c_b1, &WORK(iwork), n__);
		    clacpy("G", m__, bl, &WORK(iwork), n__, &B(1,1), ldb);
		}
	    } else {
		cgemv("C", m__, m__, c_b2, &WORK(il), ldwork, &B(1,1), 
			c__1, c_b1, &WORK(iwork), c__1);
		ccopy(m__, &WORK(iwork), c__1, &B(1,1), c__1);
	    }

            /*
             *        Zero out below first M rows of B 
             */

	    i__1 = n__ - m__;
	    claset("F", i__1, nrhs, c_b1, c_b1, &B(m__+1,1), ldb);
	    iwork = itau + m__;

            /*        Multiply transpose(Q) by B   
             *        (CWorkspace: need M+NRHS, prefer M+NHRS*NB)   
             *        (RWorkspace: none) 
             */

	    i__1 = lwork - iwork + 1;
	    cunmlq("L", "C", n__, nrhs, m__, &A(1,1), lda, &WORK(itau), 
                   &B(1,1), ldb, &WORK(iwork), i__1, info);

	} else {

            /*
             *        Path 2 - remaining underdetermined cases 
             */

	    ie = 1;
	    itauq = 1;
	    itaup = itauq + m__;
	    iwork = itaup + m__;

            /* Bidiagonalize A   
             * (CWorkspace: need 3*M, prefer 2*M+(M+N)*NB)   
             * (RWorkspace: need N) 
             */

	    i__1 = lwork - iwork + 1;
	    cgebrd(m__, n__, &A(1,1), lda, &S(1), &RWORK(ie), &WORK(itauq), 
		   &WORK(itaup), &WORK(iwork), i__1, info);

            /*
             *  Multiply B by transpose of left bidiagonalizing vectors 
             *  (CWorkspace: need 2*M+NRHS, prefer 2*M+NRHS*NB)   
             *  (RWorkspace: none) 
             */

	    i__1 = lwork - iwork + 1;
	    cunmbr("Q", "L", "C", m__, nrhs, n__, &A(1,1), lda, &WORK(itauq)
		    , &B(1,1), ldb, &WORK(iwork), i__1, info);

            /*
             *   Generate right bidiagonalizing vectors in A   
             *  (CWorkspace: need 3*M, prefer 2*M+M*NB)   
             *  (RWorkspace: none) 
             */

	    i__1 = lwork - iwork + 1;
	    cungbr("P", m__, n__, m__, &A(1,1), lda, &WORK(itaup), &WORK(
		    iwork), i__1, info);
	    irwork = ie + m__;

            /* 
             * Perform bidiagonal QR iteration,   
             * computing right singular vectors of A in A and   
             * multiplying B by transpose of left singular vectors 
             * (CWorkspace: none)   
             * (RWorkspace: need BDSPAC) 
             */

	    cbdsqr("L", m__, n__, c__0, nrhs, &S(1), &RWORK(ie), &A(1,1), 
		    lda, vdum, c__1, &B(1,1), ldb, &RWORK(irwork), info);
	    if (*info != 0) {
		goto L70;
	    }

            /*
             *   Multiply B by reciprocals of singular values   
             *   Computing MAX 
             */
	    r__1 = rcond * S(1);
	    thr = max(r__1,sfmin);
	    if (rcond < 0.f) {
/* Computing MAX */
		r__1 = eps * S(1);
		thr = max(r__1,sfmin);
	    }
	    *rank = 0;
	    i__1 = m__;
	    for (i = 1; i <= m__; ++i) {
		if (S(i) > thr) {
		    csrscl(nrhs, S(i), &B(i,1), ldb);
		    ++(*rank);
		} else {
		    claset("F", c__1, nrhs, c_b1, c_b1, &B(i,1), 
			   ldb);
		}
	    }

            /*   
             *    Multiply B by right singular vectors of A   
             *   (CWorkspace: need N, prefer N*NRHS)   
             *   (RWorkspace: none) 
             */

	    if (lwork >= ldb * nrhs && nrhs > 1) {
		cgemm("C", "N", n__, nrhs, m__, c_b2, &A(1,1), lda, &B(1,1), 
                       ldb, c_b1, &WORK(1), ldb);
		clacpy("G", n__, nrhs, &WORK(1), ldb, &B(1,1), ldb);
	    } else if (nrhs > 1) {
		chunk = lwork / n__;
		i__1 = nrhs;
		i__2 = chunk;
		for (i = 1; chunk < 0 ? i >= nrhs : i <= nrhs; i += chunk) {
/* Computing MIN */
		    i__3 = nrhs - i + 1;
		    bl = min(i__3,chunk);
		    cgemm("C", "N", n__, bl, m__, c_b2, &A(1,1), lda, 
                          &B(1,i), ldb, c_b1, &WORK(1), n__);
		    clacpy("F", n__, bl, &WORK(1), n__, &B(1,i), ldb);
		}
	    } else {
		cgemv("C", m__, n__, c_b2, &A(1,1), lda, &B(1,1), 
			c__1, c_b1, &WORK(1), c__1);
		ccopy(n__, &WORK(1), c__1, &B(1,1), c__1);
	    }
	}
    }

    /*
     *     Undo scaling 
     */

    if (iascl == 1) {
	clascl("G", c__0, c__0, anrm, smlnum, n__, nrhs, &B(1,1), ldb,
		 info);
	slascl("G", c__0, c__0, smlnum, anrm, minmn, c__1, &S(1), 
		minmn, info);
    } else if (iascl == 2) {
	clascl("G", c__0, c__0, anrm, bignum, n__, nrhs, &B(1,1), ldb,
		 info);
	slascl("G", c__0, c__0, bignum, anrm, minmn, c__1, &S(1), 
		minmn, info);
    }
    if (ibscl == 1) {
	clascl("G", c__0, c__0, smlnum, bnrm, n__, nrhs, &B(1,1), ldb,
		 info);
    } else if (ibscl == 2) {
	clascl("G", c__0, c__0, bignum, bnrm, n__, nrhs, &B(1,1), ldb,
		 info);
    }
L70:
    WORK(1).r = (float) maxwrk, WORK(1).i = 0.f;
    return ;

} 

