

#include "complex.h"
#include "mathtool.h"


void cunmbr(char *vect, char *side, char *trans, int m__, 
	    int n__, int k__, fcomplex *a, int lda, fcomplex *tau, 
	    fcomplex *c, int ldc, fcomplex *work, int lwork, int *info)
{
/*  -- LAPACK routine (version 2.0) --   
       Univ. of Tennessee, Univ. of California Berkeley, NAG Ltd.,   
       Courant Institute, Argonne National Lab, and Rice University   
       September 30, 1994   


    Purpose   
    =======   

    If VECT = 'Q', CUNMBR overwrites the general complex M-by-N matrix C 
  
    with   
                    SIDE = 'L'     SIDE = 'R'   
    TRANS = 'N':      Q * C          C * Q   
    TRANS = 'C':      Q**H * C       C * Q**H   

    If VECT = 'P', CUNMBR overwrites the general complex M-by-N matrix C 
  
    with   
                    SIDE = 'L'     SIDE = 'R'   
    TRANS = 'N':      P * C          C * P   
    TRANS = 'C':      P**H * C       C * P**H   

    Here Q and P**H are the unitary matrices determined by CGEBRD when   
    reducing a complex matrix A to bidiagonal form: A = Q * B * P**H. Q   
    and P**H are defined as products of elementary reflectors H(i) and   
    G(i) respectively.   

    Let nq = m if SIDE = 'L' and nq = n if SIDE = 'R'. Thus nq is the   
    order of the unitary matrix Q or P**H that is applied.   

    If VECT = 'Q', A is assumed to have been an NQ-by-K matrix:   
    if nq >= k, Q = H(1) H(2) . . . H(k);   
    if nq < k, Q = H(1) H(2) . . . H(nq-1).   

    If VECT = 'P', A is assumed to have been a K-by-NQ matrix:   
    if k < nq, P = G(1) G(2) . . . G(k);   
    if k >= nq, P = G(1) G(2) . . . G(nq-1).   

    Arguments   
    =========   

    VECT    (input) CHARACTER*1   
            = 'Q': apply Q or Q**H;   
            = 'P': apply P or P**H.   

    SIDE    (input) CHARACTER*1   
            = 'L': apply Q, Q**H, P or P**H from the Left;   
            = 'R': apply Q, Q**H, P or P**H from the Right.   

    TRANS   (input) CHARACTER*1   
            = 'N':  No transpose, apply Q or P;   
            = 'C':  Conjugate transpose, apply Q**H or P**H.   

    M       (input) INTEGER   
            The number of rows of the matrix C. M >= 0.   

    N       (input) INTEGER   
            The number of columns of the matrix C. N >= 0.   

    K       (input) INTEGER   
            If VECT = 'Q', the number of columns in the original   
            matrix reduced by CGEBRD.   
            If VECT = 'P', the number of rows in the original   
            matrix reduced by CGEBRD.   
            K >= 0.   

    A       (input) COMPLEX array, dimension   
                                  (LDA,min(nq,K)) if VECT = 'Q'   
                                  (LDA,nq)        if VECT = 'P'   
            The vectors which define the elementary reflectors H(i) and   
            G(i), whose products determine the matrices Q and P, as   
            returned by CGEBRD.   

    LDA     (input) INTEGER   
            The leading dimension of the array A.   
            If VECT = 'Q', LDA >= max(1,nq);   
            if VECT = 'P', LDA >= max(1,min(nq,K)).   

    TAU     (input) COMPLEX array, dimension (min(nq,K))   
            TAU(i) must contain the scalar factor of the elementary   
            reflector H(i) or G(i) which determines Q or P, as returned   
            by CGEBRD in the array argument TAUQ or TAUP.   

    C       (input/output) COMPLEX array, dimension (LDC,N)   
            On entry, the M-by-N matrix C.   
            On exit, C is overwritten by Q*C or Q**H*C or C*Q**H or C*Q   
            or P*C or P**H*C or C*P or C*P**H.   

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
    /* System generated locals */
    int i__1, i__2;
    /* Local variables */
    static int left;
    static int iinfo, i1, i2, mi, ni, nq, nw;
    static int notran;
    static int applyq;
    static char transt[1];


#define TAU(I) tau[(I)-1]
#define WORK(I) work[(I)-1]

#define A(I,J) a[(I)-1 + ((J)-1)* ( lda)]
#define C(I,J) c[(I)-1 + ((J)-1)* ( ldc)]

    *info = 0;
    applyq = lsame(vect, "Q");
    left = lsame(side, "L");
    notran = lsame(trans, "N");

    /*
     *  NQ is the order of Q or P and NW is the minimum dimension of WORK 
     */

    if (left) {
	nq = m__;
	nw = n__;
    } else {
	nq = n__;
	nw = m__;
    }
    if (! applyq && ! lsame(vect, "P")) {
	*info = -1;
    } else if (! left && ! lsame(side, "R")) {
	*info = -2;
    } else if (! notran && ! lsame(trans, "C")) {
	*info = -3;
    } else if (m__ < 0) {
	*info = -4;
    } else if (n__ < 0) {
	*info = -5;
    } else if (k__ < 0) {
	*info = -6;
    } else /* if(complicated condition) */ {
        /*
         * Computing MAX 
         */
	i__1 = 1, i__2 = min(nq,k__);
	if ((applyq && lda < max(1,nq)) || (! applyq && lda < max(i__1,i__2))) {
	    *info = -8;
	} else if (ldc < max(1,m__)) {
	    *info = -11;
	} else if (lwork < max(1,nw)) {
	    *info = -13;
	}
    }
    if (*info != 0) {
	i__1 = -(*info);
	return ;
    }

    /*
     *     Quick return if possible 
     */

    WORK(1).r = 1.f, WORK(1).i = 0.f;
    if (m__ == 0 || n__ == 0) {
	return ;
    }

    if (applyq) {

            /*
             *        Apply Q 
             */

	if (nq >= k__) {

            /*
             *           Q was determined by a call to CGEBRD with nq >= k 
             */

	    cunmqr(side, trans, m__, n__, k__, &A(1,1), lda, &TAU(1), &C(1,1), 
                   ldc, &WORK(1), lwork, &iinfo);
	} else if (nq > 1) {

            /*
             *           Q was determined by a call to CGEBRD with nq < k 
             */

	    if (left) {
		mi = m__ - 1;
		ni = n__;
		i1 = 2;
		i2 = 1;
	    } else {
		mi = m__;
		ni = n__ - 1;
		i1 = 1;
		i2 = 2;
	    }
	    i__1 = nq - 1;
	    cunmqr(side, trans, mi, ni, i__1, &A(2,1), lda, &TAU(1)
		    , &C(i1,i2), ldc, &WORK(1), lwork, &iinfo);
	}
    } else {

        /*
         *        Apply P 
         */

	if (notran) {
	    *(unsigned char *)transt = 'C';
	} else {
	    *(unsigned char *)transt = 'N';
	}
	if (nq > k__) {

            /*
             *           P was determined by a call to CGEBRD with nq > k 
             */

	    cunmlq(side, transt, m__, n__, k__, &A(1,1), lda, 
                   &TAU(1), &C(1,1), ldc, &WORK(1), lwork, &iinfo);
	} else if (nq > 1) {

            /*
             *           P was determined by a call to CGEBRD with nq <= k 
             */

	    if (left) {
		mi = m__ - 1;
		ni = n__;
		i1 = 2;
		i2 = 1;
	    } else {
		mi = m__;
		ni = n__ - 1;
		i1 = 1;
		i2 = 2;
	    }
	    i__1 = nq - 1;
	    cunmlq(side, transt, mi, ni, i__1, &A(1,2), lda,
		    &TAU(1), &C(i1,i2), ldc, &WORK(1), lwork, &
		    iinfo);
	}
    }

} 

