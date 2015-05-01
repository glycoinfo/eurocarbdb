

#include "complex.h"
#include "mathtool.h"

void cungqr(int m__, int n__, int k__, fcomplex *a, 
	int lda, fcomplex *tau, fcomplex *work, int lwork, int *info)
{
/*  -- LAPACK routine (version 2.0) --   
       Univ. of Tennessee, Univ. of California Berkeley, NAG Ltd.,   
       Courant Institute, Argonne National Lab, and Rice University   
       September 30, 1994   


    Purpose   
    =======   

    CUNGQR generates an M-by-N complex matrix Q with orthonormal columns, 
  
    which is defined as the first N columns of a product of K elementary 
  
    reflectors of order M   

          Q  =  H(1) H(2) . . . H(k)   

    as returned by CGEQRF.   

    Arguments   
    =========   

    M       (input) INTEGER   
            The number of rows of the matrix Q. M >= 0.   

    N       (input) INTEGER   
            The number of columns of the matrix Q. M >= N >= 0.   

    K       (input) INTEGER   
            The number of elementary reflectors whose product defines the 
  
            matrix Q. N >= K >= 0.   

    A       (input/output) COMPLEX array, dimension (LDA,N)   
            On entry, the i-th column must contain the vector which   
            defines the elementary reflector H(i), for i = 1,2,...,k, as 
  
            returned by CGEQRF in the first k columns of its array   
            argument A.   
            On exit, the M-by-N matrix Q.   

    LDA     (input) INTEGER   
            The first dimension of the array A. LDA >= max(1,M).   

    TAU     (input) COMPLEX array, dimension (K)   
            TAU(i) must contain the scalar factor of the elementary   
            reflector H(i), as returned by CGEQRF.   

    WORK    (workspace/output) COMPLEX array, dimension (LWORK)   
            On exit, if INFO = 0, WORK(1) returns the optimal LWORK.   

    LWORK   (input) INTEGER   
            The dimension of the array WORK. LWORK >= max(1,N).   
            For optimum performance LWORK >= N*NB, where NB is the   
            optimal blocksize.   

    INFO    (output) INTEGER   
            = 0:  successful exit   
            < 0:  if INFO = -i, the i-th argument has an illegal value   

    ===================================================================== 
  


       Test the input arguments   

    
   Parameter adjustments   
       Function Body */
    /* Table of constant values */
    static int c__1 = 1;
    static int c_n1 = -1;
    static int c__3 = 3;
    static int c__2 = 2;
    
    /* System generated locals */
    int a_dim1, i__1, i__2, i__3, i__4;
    /* Local variables */
    static int i, j, l, nbmin, iinfo;
    static int ib, nb, ki, kk;
    static int nx;
    static int ldwork, iws;



#define TAU(I) tau[(I)-1]
#define WORK(I) work[(I)-1]

#define A(I,J) a[(I)-1 + ((J)-1)* ( lda)]

    *info = 0;
    if (m__ < 0) {
	*info = -1;
    } else if (n__ < 0 || n__ > m__) {
	*info = -2;
    } else if (k__ < 0 || k__ > n__) {
	*info = -3;
    } else if (lda < max(1,m__)) {
	*info = -5;
    } else if (lwork < max(1,n__)) {
	*info = -8;
    }
    if (*info != 0) {
	i__1 = -(*info);
	return ;
    }

    /*
     *     Quick return if possible 
     */

    if (n__ <= 0) {
	WORK(1).r = 1.f, WORK(1).i = 0.f;
	return ;
    }

    /*
     *     Determine the block size. 
     */

    nb = ilaenv(c__1, "CUNGQR", " ", m__, n__, k__, c_n1, 6L, 1L);
    nbmin = 2;
    nx = 0;
    iws = n__;
    if (nb > 1 && nb < k__) {

        /*
         *        Determine when to cross over from blocked to unblocked code.
         * Computing MAX 
         */
	i__1 = 0, i__2 = ilaenv(c__3, "CUNGQR", " ", m__, n__, k__, c_n1, 6L, 1L)
		;
	nx = max(i__1,i__2);
	if (nx < k__) {

            /*
             * Determine if workspace is large enough for blocked code. 
             */

	    ldwork = n__;
	    iws = ldwork * nb;
	    if (lwork < iws) {

                /* 
                 * Not enough workspace to use optimal NB:  reduce NB and   
                 * determine the minimum value of NB. 
                 */

		nb = lwork / ldwork;
/* Computing MAX */
		i__1 = 2, i__2 = ilaenv(c__2, "CUNGQR", " ", m__, n__, k__, c_n1,
			 6L, 1L);
		nbmin = max(i__1,i__2);
	    }
	}
    }

    if (nb >= nbmin && nb < k__ && nx < k__) {

        /*
         *        Use blocked code after the last block.   
         *  The first kk columns are handled by the block method. 
         */

	ki = (k__ - nx - 1) / nb * nb;
/* Computing MIN */
	i__1 = k__, i__2 = ki + nb;
	kk = min(i__1,i__2);

        /*
         *        Set A(1:kk,kk+1:n) to zero. 
         */

	i__1 = n__;
	for (j = kk + 1; j <= n__; ++j) {
	    i__2 = kk;
	    for (i = 1; i <= kk; ++i) {
		i__3 = i + j * a_dim1;
		A(i,j).r = 0.f, A(i,j).i = 0.f;
	    }
	}
    } else {
	kk = 0;
    }

    /*
     *     Use unblocked code for the last or only block. 
     */

    if (kk < n__) {
	i__1 = m__ - kk;
	i__2 = n__ - kk;
	i__3 = k__ - kk;
	cung2r(i__1, i__2, i__3, &A(kk+1,kk+1), lda, &
		TAU(kk + 1), &WORK(1), &iinfo);
    }

    if (kk > 0) {

        /*
         *        Use blocked code 
         */

	i__1 = -nb;
	for (i = ki + 1; -nb < 0 ? i >= 1 : i <= 1; i += -nb) {
/* Computing MIN */
	    i__2 = nb, i__3 = k__ - i + 1;
	    ib = min(i__2,i__3);
	    if (i + ib <= n__) {

                /*
                 *       Form the triangular factor of the block reflector   
                 *       H = H(i) H(i+1) . . . H(i+ib-1) 
                 */

		i__2 = m__ - i + 1;
		clarft("Forward", "Columnwise", i__2, ib, &A(i,i), lda, 
                       &TAU(i), &WORK(1), ldwork);

                /*
                 *              Apply H to A(i:m,i+ib:n) from the left 
                 */

		i__2 = m__ - i + 1;
		i__3 = n__ - i - ib + 1;
		clarfb("Left", "No transpose", "Forward", "Columnwise", 
			i__2, i__3, ib, &A(i,i), lda, &WORK(1), 
			ldwork, &A(i,i+ib), lda, &WORK(ib + 1),
			ldwork);
	    }

            /*
             *           Apply H to rows i:m of current block 
             */

	    i__2 = m__ - i + 1;
	    cung2r(i__2, ib, ib, &A(i,i), lda, &TAU(i), &WORK(
		    1), &iinfo);

            /*
             *           Set rows 1:i-1 of current block to zero 
             */

	    i__2 = i + ib - 1;
	    for (j = i; j <= i+ib-1; ++j) {
		i__3 = i - 1;
		for (l = 1; l <= i-1; ++l) {
		    i__4 = l + j * a_dim1;
		    A(l,j).r = 0.f, A(l,j).i = 0.f;
		}
	    }
	}
    }

    WORK(1).r = (float) iws, WORK(1).i = 0.f;

} 

