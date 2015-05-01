
#include "complex.h"
#include "mathtool.h"


void cungbr(char *vect, int m__, int n__, int k__, 
	    fcomplex *a, int lda, fcomplex *tau, fcomplex *work, 
            int lwork, int *info)
{
/*  -- LAPACK routine (version 2.0) --   
       Univ. of Tennessee, Univ. of California Berkeley, NAG Ltd.,   
       Courant Institute, Argonne National Lab, and Rice University   
       September 30, 1994   


    Purpose   
    =======   

    CUNGBR generates one of the complex unitary matrices Q or P**H   
    determined by CGEBRD when reducing a complex matrix A to bidiagonal   
    form: A = Q * B * P**H.  Q and P**H are defined as products of   
    elementary reflectors H(i) or G(i) respectively.   

    If VECT = 'Q', A is assumed to have been an M-by-K matrix, and Q   
    is of order M:   
    if m >= k, Q = H(1) H(2) . . . H(k) and CUNGBR returns the first n   
    columns of Q, where m >= n >= k;   
    if m < k, Q = H(1) H(2) . . . H(m-1) and CUNGBR returns Q as an   
    M-by-M matrix.   

    If VECT = 'P', A is assumed to have been a K-by-N matrix, and P**H   
    is of order N:   
    if k < n, P**H = G(k) . . . G(2) G(1) and CUNGBR returns the first m 
  
    rows of P**H, where n >= m >= k;   
    if k >= n, P**H = G(n-1) . . . G(2) G(1) and CUNGBR returns P**H as   
    an N-by-N matrix.   

    Arguments   
    =========   

    VECT    (input) CHARACTER*1   
            Specifies whether the matrix Q or the matrix P**H is   
            required, as defined in the transformation applied by CGEBRD: 
  
            = 'Q':  generate Q;   
            = 'P':  generate P**H.   

    M       (input) INTEGER   
            The number of rows of the matrix Q or P**H to be returned.   
            M >= 0.   

    N       (input) INTEGER   
            The number of columns of the matrix Q or P**H to be returned. 
  
            N >= 0.   
            If VECT = 'Q', M >= N >= min(M,K);   
            if VECT = 'P', N >= M >= min(N,K).   

    K       (input) INTEGER   
            If VECT = 'Q', the number of columns in the original M-by-K   
            matrix reduced by CGEBRD.   
            If VECT = 'P', the number of rows in the original K-by-N   
            matrix reduced by CGEBRD.   
            K >= 0.   

    A       (input/output) COMPLEX array, dimension (LDA,N)   
            On entry, the vectors which define the elementary reflectors, 
  
            as returned by CGEBRD.   
            On exit, the M-by-N matrix Q or P**H.   

    LDA     (input) INTEGER   
            The leading dimension of the array A. LDA >= M.   

    TAU     (input) COMPLEX array, dimension   
                                  (min(M,K)) if VECT = 'Q'   
                                  (min(N,K)) if VECT = 'P'   
            TAU(i) must contain the scalar factor of the elementary   
            reflector H(i) or G(i), which determines Q or P**H, as   
            returned by CGEBRD in its array argument TAUQ or TAUP.   

    WORK    (workspace/output) COMPLEX array, dimension (LWORK)   
            On exit, if INFO = 0, WORK(1) returns the optimal LWORK.   

    LWORK   (input) INTEGER   
            The dimension of the array WORK. LWORK >= max(1,min(M,N)).   
            For optimum performance LWORK >= min(M,N)*NB, where NB   
            is the optimal blocksize.   

    INFO    (output) INTEGER   
            = 0:  successful exit   
            < 0:  if INFO = -i, the i-th argument had an illegal value   

    ===================================================================== 
  


       Test the input arguments   

    
   Parameter adjustments   
       Function Body */
    /* System generated locals */
    int a_dim1, i__1, i__2, i__3;
    /* Local variables */
    static int i, j;
    static int iinfo;
    static int wantq;


#define TAU(I) tau[(I)-1]
#define WORK(I) work[(I)-1]

#define A(I,J) a[(I)-1 + ((J)-1)* ( lda)]

    *info = 0;
    wantq = lsame(vect, "Q");
    if (! wantq && ! lsame(vect, "P")) {
	*info = -1;
    } else if (m__ < 0) {
	*info = -2;
    } else if (n__ < 0 || (wantq && (n__ > m__ || n__ < min(m__,k__))) || 
              (! wantq && (m__ > n__ || m__ < min(n__,k__)))) {
	*info = -3;
    } else if (k__ < 0) {
	*info = -4;
    } else if (lda < max(1,m__)) {
	*info = -6;
    } else /* if(complicated condition) */ {
        /* 
         * Computing MAX 
         */
	i__1 = 1, i__2 = min(m__,n__);
	if (lwork < max(i__1,i__2)) {
	    *info = -9;
	}
    }
    if (*info != 0) {
	i__1 = -(*info);
	return ;
    }

    /*
     *     Quick return if possible 
     */

    if (m__ == 0 || n__ == 0) {
	WORK(1).r = 1.f, WORK(1).i = 0.f;
	return;
    }

    if (wantq) {

          /* 
           * Form Q, determined by a call to CGEBRD to reduce an m-by-k 
           * matrix 
           */

	if (m__ >= k__) {

            /*
             *           If m >= k, assume m >= n >= k 
             */

	    cungqr(m__, n__, k__, &A(1,1), lda, &TAU(1), &WORK(1), lwork, &
		   iinfo);

	} else {

            /*
             * If m < k, assume m = n   
             *
             * Shift the vectors which define the elementary reflectors one   
             * column to the right, and set the first row and column of Q   
             * to those of the unit matrix 
             */

	    for (j = m__; j >= 2; --j) {
		i__1 = j * a_dim1 + 1;
		A(1,j).r = 0.f, A(1,j).i = 0.f;
		i__1 = m__;
		for (i = j + 1; i <= m__; ++i) {
		    i__2 = i + j * a_dim1;
		    i__3 = i + (j - 1) * a_dim1;
		    A(i,j).r = A(i,j-1).r, A(i,j).i = A(i,j-1).i;
		}
	    }
	    i__1 = a_dim1 + 1;
	    A(1,1).r = 1.f, A(1,1).i = 0.f;
	    i__1 = m__;
	    for (i = 2; i <= m__; ++i) {
		i__2 = i + a_dim1;
		A(i,1).r = 0.f, A(i,1).i = 0.f;
	    }
	    if (m__ > 1) {

                /*
                 *              Form Q(2:m,2:m) 
                 */

		i__1 = m__ - 1;
		i__2 = m__ - 1;
		i__3 = m__ - 1;
		cungqr(i__1, i__2, i__3, &A(2,2), lda, 
		       &TAU(1), &WORK(1), lwork, &iinfo);
	    }
	}
    } else {

        /*
         *  Form P', determined by a call to CGEBRD to reduce a k-by-n 
         *  matrix 
         */

	if (k__ < n__) {

            /*
             *  If k < n, assume k <= m <= n 
             */

	    cunglq(m__, n__, k__, &A(1,1), lda, &TAU(1), &WORK(1), lwork, &
		    iinfo);

	} else {

            /* 
             * If k >= n, assume m = n   
             * Shift the vectors which define the elementary reflectors one   
             * row downward, and set the first row and column of P' to   
             * those of the unit matrix 
             */

	    i__1 = a_dim1 + 1;
	    A(1,1).r = 1.f, A(1,1).i = 0.f;
	    i__1 = n__;
	    for (i = 2; i <= n__; ++i) {
		i__2 = i + a_dim1;
		A(i,1).r = 0.f, A(i,1).i = 0.f;
	    }
	    i__1 = n__;
	    for (j = 2; j <= n__; ++j) {
		for (i = j - 1; i >= 2; --i) {
		    i__2 = i + j * a_dim1;
		    i__3 = i - 1 + j * a_dim1;
		    A(i,j).r = A(i-1,j).r, A(i,j).i = A(i-1,j).i;
		}
		i__2 = j * a_dim1 + 1;
		A(1,j).r = 0.f, A(1,j).i = 0.f;
	    }
	    if (n__ > 1) {

                /*
                 *   Form P'(2:n,2:n) 
                 */

		i__1 = n__ - 1;
		i__2 = n__ - 1;
		i__3 = n__ - 1;
		cunglq(i__1, i__2, i__3, &A(2,2), lda, &TAU(1), 
			&WORK(1), lwork, &iinfo);
	    }
	}
    }

} 

