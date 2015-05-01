
#include "complex.h"
#include "mathtool.h"

void clacpy(char *uplo, int m__, int n__, fcomplex *a, 
	    int lda, fcomplex *b, int ldb)
{
/*  -- LAPACK auxiliary routine (version 2.0) --   
       Univ. of Tennessee, Univ. of California Berkeley, NAG Ltd.,   
       Courant Institute, Argonne National Lab, and Rice University   
       February 29, 1992   


    Purpose   
    =======   

    CLACPY copies all or part of a two-dimensional matrix A to another   
    matrix B.   

    Arguments   
    =========   

    UPLO    (input) CHARACTER*1   
            Specifies the part of the matrix A to be copied to B.   
            = 'U':      Upper triangular part   
            = 'L':      Lower triangular part   
            Otherwise:  All of the matrix A   

    M       (input) INTEGER   
            The number of rows of the matrix A.  M >= 0.   

    N       (input) INTEGER   
            The number of columns of the matrix A.  N >= 0.   

    A       (input) COMPLEX array, dimension (LDA,N)   
            The m by n matrix A.  If UPLO = 'U', only the upper trapezium 
  
            is accessed; if UPLO = 'L', only the lower trapezium is   
            accessed.   

    LDA     (input) INTEGER   
            The leading dimension of the array A.  LDA >= max(1,M).   

    B       (output) COMPLEX array, dimension (LDB,N)   
            On exit, B = A in the locations specified by UPLO.   

    LDB     (input) INTEGER   
            The leading dimension of the array B.  LDB >= max(1,M).   

    ===================================================================== 
  


    
   Parameter adjustments   
       Function Body */
    /* System generated locals */
    int a_dim1, b_dim1, i__1, i__2, i__3, i__4;
    /* Local variables */
    static int i, j;



#define A(I,J) a[(I)-1 + ((J)-1)* ( lda)]
#define B(I,J) b[(I)-1 + ((J)-1)* ( ldb)]

    if (lsame(uplo, "U")) {
	i__1 = n__;
	for (j = 1; j <= n__; ++j) {
	    i__2 = min(j,m__);
	    for (i = 1; i <= min(j,m__); ++i) {
		i__3 = i + j * b_dim1;
		i__4 = i + j * a_dim1;
		B(i,j).r = A(i,j).r, B(i,j).i = A(i,j).i;
	    }
	}

    } else if (lsame(uplo, "L")) {
	i__1 = n__;
	for (j = 1; j <= n__; ++j) {
	    i__2 = m__;
	    for (i = j; i <= m__; ++i) {
		i__3 = i + j * b_dim1;
		i__4 = i + j * a_dim1;
		B(i,j).r = A(i,j).r, B(i,j).i = A(i,j).i;
	    }
	}

    } else {
	i__1 = n__;
	for (j = 1; j <= n__; ++j) {
	    i__2 = m__;
	    for (i = 1; i <= m__; ++i) {
		i__3 = i + j * b_dim1;
		i__4 = i + j * a_dim1;
		B(i,j).r = A(i,j).r, B(i,j).i = A(i,j).i;
	    }
	}
    }

} 

