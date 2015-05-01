
#include "complex.h"
#include "mathtool.h"


void cunmqr(char *side, char *trans, int m__, int n__, 
	    int k__, fcomplex *a, int lda, fcomplex *tau, fcomplex *c, 
	    int ldc, fcomplex *work, int lwork, int *info)
{
/*  -- LAPACK routine (version 2.0) --   
       Univ. of Tennessee, Univ. of California Berkeley, NAG Ltd.,   
       Courant Institute, Argonne National Lab, and Rice University   
       September 30, 1994   


    Purpose   
    =======   

    CUNMQR overwrites the general complex M-by-N matrix C with   

                    SIDE = 'L'     SIDE = 'R'   
    TRANS = 'N':      Q * C          C * Q   
    TRANS = 'C':      Q**H * C       C * Q**H   

    where Q is a complex unitary matrix defined as the product of k   
    elementary reflectors   

          Q = H(1) H(2) . . . H(k)   

    as returned by CGEQRF. Q is of order M if SIDE = 'L' and of order N   
    if SIDE = 'R'.   

    Arguments   
    =========   

    SIDE    (input) CHARACTER*1   
            = 'L': apply Q or Q**H from the Left;   
            = 'R': apply Q or Q**H from the Right.   

    TRANS   (input) CHARACTER*1   
            = 'N':  No transpose, apply Q;   
            = 'C':  Conjugate transpose, apply Q**H.   

    M       (input) INTEGER   
            The number of rows of the matrix C. M >= 0.   

    N       (input) INTEGER   
            The number of columns of the matrix C. N >= 0.   

    K       (input) INTEGER   
            The number of elementary reflectors whose product defines   
            the matrix Q.   
            If SIDE = 'L', M >= K >= 0;   
            if SIDE = 'R', N >= K >= 0.   

    A       (input) COMPLEX array, dimension (LDA,K)   
            The i-th column must contain the vector which defines the   
            elementary reflector H(i), for i = 1,2,...,k, as returned by 
  
            CGEQRF in the first k columns of its array argument A.   
            A is modified by the routine but restored on exit.   

    LDA     (input) INTEGER   
            The leading dimension of the array A.   
            If SIDE = 'L', LDA >= max(1,M);   
            if SIDE = 'R', LDA >= max(1,N).   

    TAU     (input) COMPLEX array, dimension (K)   
            TAU(i) must contain the scalar factor of the elementary   
            reflector H(i), as returned by CGEQRF.   

    C       (input/output) COMPLEX array, dimension (LDC,N)   
            On entry, the M-by-N matrix C.   
            On exit, C is overwritten by Q*C or Q**H*C or C*Q**H or C*Q. 
  

    LDC     (input) INTEGER   
            The leading dimension of the array C. LDC >= max(1,M).   

    WORK    (workspace/output) COMPLEX array, dimension (LWORK)   
            On exit, if INFO = 0, WORK(1) returns the optimal LWORK.   

    LWORK   (input) INTEGER   
            The dimension of the array WORK.   
            If SIDE = 'L', LWORK >= max(1,N);   
            if SIDE = 'R', LWORK >= max(1,M).   
            For optimum performance LWORK >= N*NB if SIDE = 'L', and   
            LWORK >= M*NB if SIDE = 'R', where NB is the optimal   
            blocksize.   

    INFO    (output) INTEGER   
            = 0:  successful exit   
            < 0:  if INFO = -i, the i-th argument had an illegal value   

    ===================================================================== 
  


       Test the input arguments   

    
   Parameter adjustments   
       Function Body */
    /* Table of constant values */
    static int c__1 = 1;
    static int c_n1 = -1;
    static int c__2 = 2;
    static int c__65 = 65;
    
    /* System generated locals */
    char *a__1[2];
    int i__1, i__2, i__3[2], i__4, i__5;
    char ch__1[2];
    /* Builtin functions   
       Subroutine  int s_cat(char *, char **, int *, int *, ftnlen);*/
    /* Local variables */
    static int left;
    static int i;
    static fcomplex t[4160]	/* was [65][64] */;
    static int nbmin, iinfo, i1, i2, i3;
    static int ib, ic, jc, nb, mi, ni;
    static int nq, nw;
    static int notran;
    static int ldwork, iws;



#define TAU(I) tau[(I)-1]
#define WORK(I) work[(I)-1]

#define A(I,J) a[(I)-1 + ((J)-1)* ( lda)]
#define C(I,J) c[(I)-1 + ((J)-1)* ( ldc)]

    *info = 0;
    left = lsame(side, "L");
    notran = lsame(trans, "N");

    /* 
     *    NQ is the order of Q and NW is the minimum dimension of WORK 
     */

    if (left) {
	nq = m__;
	nw = n__;
    } else {
	nq = n__;
	nw = m__;
    }
    if (! left && ! lsame(side, "R")) {
	*info = -1;
    } else if (! notran && ! lsame(trans, "C")) {
	*info = -2;
    } else if (m__ < 0) {
	*info = -3;
    } else if (n__ < 0) {
	*info = -4;
    } else if (k__ < 0 || k__ > nq) {
	*info = -5;
    } else if (lda < max(1,nq)) {
	*info = -7;
    } else if (ldc < max(1,m__)) {
	*info = -10;
    } else if (lwork < max(1,nw)) {
	*info = -12;
    }
    if (*info != 0) {
	i__1 = -(*info);
	return;
    }

    /*
     *     Quick return if possible 
     */

    if (m__ == 0 || n__ == 0 || k__ == 0) {
	WORK(1).r = 1.f, WORK(1).i = 0.f;
	return;
    }

    /*
     *     Determine the block size.  NB may be at most NBMAX, where NBMAX   
     *     is used to define the local array T.   
     *
     *     Computing MIN   
     *     Writing concatenation 
     */
    i__3[0] = 1, a__1[0] = side;
    i__3[1] = 1, a__1[1] = trans;
    s_cat(ch__1, a__1, i__3, &c__2, 2L);
    i__1 = 64, i__2 = ilaenv(c__1, "CUNMQR", ch__1, m__, n__, k__, c_n1, 6L, 2L);
    nb = min(i__1,i__2);
    nbmin = 2;
    ldwork = nw;
    if (nb > 1 && nb < k__) {
	iws = nw * nb;
	if (lwork < iws) {
	    nb = lwork / ldwork;
            /* 
             * Computing MAX   
             * Writing concatenation 
             */
	    i__3[0] = 1, a__1[0] = side;
	    i__3[1] = 1, a__1[1] = trans;
	    s_cat(ch__1, a__1, i__3, &c__2, 2L);
	    i__1 = 2, i__2 = ilaenv(c__2, "CUNMQR", ch__1, m__, n__, k__, c_n1, 
		    6L, 2L);
	    nbmin = max(i__1,i__2);
	}
    } else {
	iws = nw;
    }

    if (nb < nbmin || nb >= k__) {

        /*
         *        Use unblocked code 
         */

	cunm2r(side, trans, m__, n__, k__, &A(1,1), lda, &TAU(1), &C(1,1)
		, ldc, &WORK(1), &iinfo);
    } else {

        /*
         *        Use blocked code 
         */

	if ((left && ! notran) || (! left && notran)) {
	    i1 = 1;
	    i2 = k__;
	    i3 = nb;
	} else {
	    i1 = (k__ - 1) / nb * nb + 1;
	    i2 = 1;
	    i3 = -nb;
	}

	if (left) {
	    ni = n__;
	    jc = 1;
	} else {
	    mi = m__;
	    ic = 1;
	}

	i__1 = i2;
	i__2 = i3;
	for (i = i1; i3 < 0 ? i >= i2 : i <= i2; i += i3) {
            /* 
             *   Computing MIN 
             */
	    i__4 = nb, i__5 = k__ - i + 1;
	    ib = min(i__4,i__5);

            /*
             *   Form the triangular factor of the block reflector   
             *   H = H(i) H(i+1) . . . H(i+ib-1) 
             */

	    i__4 = nq - i + 1;
	    clarft("Forward", "Columnwise", i__4, ib, &A(i,i), 
		    lda, &TAU(i), t, c__65);
	    if (left) {

                /* 
                 *   H or H' is applied to C(i:m,1:n) 
                 */

		mi = m__ - i + 1;
		ic = i;
	    } else {

                /*
                 *   H or H' is applied to C(1:m,i:n) 
                 */

		ni = n__ - i + 1;
		jc = i;
	    }

            /*
             *   Apply H or H' 
             */

	    clarfb(side, trans, "Forward", "Columnwise", mi, ni, ib, 
                   &A(i,i), lda, t, c__65, &C(ic,jc), ldc, 
		   &WORK(1), ldwork);

	}
    }
    WORK(1).r = (float) iws, WORK(1).i = 0.f;

} 

