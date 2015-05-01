
#include "complex.h"
#include "mathtool.h"


void claset(char *uplo, int m__, int n__, fcomplex 
	    alpha, fcomplex beta, fcomplex *a, int lda)
{
/*  -- LAPACK auxiliary routine (version 2.0) --   
       Univ. of Tennessee, Univ. of California Berkeley, NAG Ltd.,   
       Courant Institute, Argonne National Lab, and Rice University   
       October 31, 1992   


    Purpose   
    =======   

    CLASET initializes a 2-D array A to BETA on the diagonal and   
    ALPHA on the offdiagonals.   

    Arguments   
    =========   

    UPLO    (input) CHARACTER*1   
            Specifies the part of the matrix A to be set.   
            = 'U':      Upper triangular part is set. The lower triangle 
  
                        is unchanged.   
            = 'L':      Lower triangular part is set. The upper triangle 
  
                        is unchanged.   
            Otherwise:  All of the matrix A is set.   

    M       (input) INTEGER   
            On entry, M specifies the number of rows of A.   

    N       (input) INTEGER   
            On entry, N specifies the number of columns of A.   

    ALPHA   (input) COMPLEX   
            All the offdiagonal array elements are set to ALPHA.   

    BETA    (input) COMPLEX   
            All the diagonal array elements are set to BETA.   

    A       (input/output) COMPLEX array, dimension (LDA,N)   
            On entry, the m by n matrix A.   
            On exit, A(i,j) = ALPHA, 1 <= i <= m, 1 <= j <= n, i.ne.j;   
                     A(i,i) = BETA , 1 <= i <= min(m,n)   

    LDA     (input) INTEGER   
            The leading dimension of the array A.  LDA >= max(1,M).   

    ===================================================================== 
  


    
   Parameter adjustments   
       Function Body */
    /* System generated locals */
    int a_dim1, i__1, i__2, i__3;
    /* Local variables */
    static int i, j;



#define A(I,J) a[(I)-1 + ((J)-1)* ( lda)]

    if (lsame(uplo, "U")) {

          /*  
           * Set the diagonal to BETA and the strictly upper triangular 
           * part of the array to ALPHA. 
           */

	i__1 = n__;
	for (j = 2; j <= n__; ++j) {
/* Computing MIN */
	    i__3 = j - 1;
	    i__2 = min(i__3,m__);
	    for (i = 1; i <= min(j-1,m__); ++i) {
		i__3 = i + j * a_dim1;
		A(i,j).r = alpha.r, A(i,j).i = alpha.i;
	    }
	}
	i__1 = min(n__,m__);
	for (i = 1; i <= min(n__,m__); ++i) {
	    i__2 = i + i * a_dim1;
	    A(i,i).r = beta.r, A(i,i).i = beta.i;
	}

    } else if (lsame(uplo, "L")) {

        /*
         *  Set the diagonal to BETA and the strictly lower triangular 
         *  part of the array to ALPHA. 
         */

	i__1 = min(m__,n__);
	for (j = 1; j <= min(m__,n__); ++j) {
	    i__2 = m__;
	    for (i = j + 1; i <= m__; ++i) {
		i__3 = i + j * a_dim1;
		A(i,j).r = alpha.r, A(i,j).i = alpha.i;
	    }
	}
	i__1 = min(n__,m__);
	for (i = 1; i <= min(n__,m__); ++i) {
	    i__2 = i + i * a_dim1;
	    A(i,i).r = beta.r, A(i,i).i = beta.i;
	}

    } else {

        /* 
         *  Set the array to BETA on the diagonal and ALPHA on the   
         *  offdiagonal. 
         */

	i__1 = n__;
	for (j = 1; j <= n__; ++j) {
	    i__2 = m__;
	    for (i = 1; i <= m__; ++i) {
		i__3 = i + j * a_dim1;
		A(i,j).r = alpha.r, A(i,j).i = alpha.i;
	    }
	}
	i__1 = min(m__,n__);
	for (i = 1; i <= min(m__,n__); ++i) {
	    i__2 = i + i * a_dim1;
	    A(i,i).r = beta.r, A(i,i).i = beta.i;
	}
    }


} 

