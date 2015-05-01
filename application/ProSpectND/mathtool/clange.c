
#include <math.h>
#include "complex.h"
#include "mathtool.h"

double clange(char *norm, int m__, int n__, fcomplex *a, int 
	      lda, float *work)
{
/*  -- LAPACK auxiliary routine (version 2.0) --   
       Univ. of Tennessee, Univ. of California Berkeley, NAG Ltd.,   
       Courant Institute, Argonne National Lab, and Rice University   
       October 31, 1992   


    Purpose   
    =======   

    CLANGE  returns the value of the one norm,  or the Frobenius norm, or 
  
    the  infinity norm,  or the  element of  largest absolute value  of a 
  
    complex matrix A.   

    Description   
    ===========   

    CLANGE returns the value   

       CLANGE = ( max(abs(A(i,j))), NORM = 'M' or 'm'   
                (   
                ( norm1(A),         NORM = '1', 'O' or 'o'   
                (   
                ( normI(A),         NORM = 'I' or 'i'   
                (   
                ( normF(A),         NORM = 'F', 'f', 'E' or 'e'   

    where  norm1  denotes the  one norm of a matrix (maximum column sum), 
  
    normI  denotes the  infinity norm  of a matrix  (maximum row sum) and 
  
    normF  denotes the  Frobenius norm of a matrix (square root of sum of 
  
    squares).  Note that  max(abs(A(i,j)))  is not a  matrix norm.   

    Arguments   
    =========   

    NORM    (input) CHARACTER*1   
            Specifies the value to be returned in CLANGE as described   
            above.   

    M       (input) INTEGER   
            The number of rows of the matrix A.  M >= 0.  When M = 0,   
            CLANGE is set to zero.   

    N       (input) INTEGER   
            The number of columns of the matrix A.  N >= 0.  When N = 0, 
  
            CLANGE is set to zero.   

    A       (input) COMPLEX array, dimension (LDA,N)   
            The m by n matrix A.   

    LDA     (input) INTEGER   
            The leading dimension of the array A.  LDA >= max(M,1).   

    WORK    (workspace) REAL array, dimension (LWORK),   
            where LWORK >= M when NORM = 'I'; otherwise, WORK is not   
            referenced.   

   ===================================================================== 
  


    
   Parameter adjustments   
       Function Body */
    /* Table of constant values */
    static int c__1 = 1;
    
    /* System generated locals */
    int i__1, i__2;
    float ret_val, r__1, r__2;
    /* Local variables */
    static int i, j;
    static float scale;
    static float value;
    static float sum;



#define WORK(I) work[(I)-1]

#define A(I,J) a[(I)-1 + ((J)-1)* ( lda)]

    if (min(m__,n__) == 0) {
	value = 0.f;
    } else if (lsame(norm, "M")) {

        /*
         *        Find max(abs(A(i,j))). 
         */

	value = 0.f;
	i__1 = n__;
	for (j = 1; j <= n__; ++j) {
	    i__2 = m__;
	    for (i = 1; i <= m__; ++i) {
/* Computing MAX */
		r__1 = value, r__2 = c_abs(&A(i,j));
		value = max(r__1,r__2);
	    }
	}
    } else if (lsame(norm, "O") || *(unsigned char *)norm == '1') {

        /*
         *        Find norm1(A). 
         */

	value = 0.f;
	i__1 = n__;
	for (j = 1; j <= n__; ++j) {
	    sum = 0.f;
	    i__2 = m__;
	    for (i = 1; i <= m__; ++i) {
		sum += c_abs(&A(i,j));
	    }
	    value = max(value,sum);
	}
    } else if (lsame(norm, "I")) {

        /*
         *        Find normI(A). 
         */

	i__1 = m__;
	for (i = 1; i <= m__; ++i) {
	    WORK(i) = 0.f;
	}
	i__1 = n__;
	for (j = 1; j <= n__; ++j) {
	    i__2 = m__;
	    for (i = 1; i <= m__; ++i) {
		WORK(i) += c_abs(&A(i,j));
	    }
	}
	value = 0.f;
	i__1 = m__;
	for (i = 1; i <= m__; ++i) {
            /* 
             *Computing MAX 
             */
	    r__1 = value, r__2 = WORK(i);
	    value = max(r__1,r__2);
	}
    } else if (lsame(norm, "F") || lsame(norm, "E")) {

        /*
         *        Find normF(A). 
         */

	scale = 0.f;
	sum = 1.f;
	i__1 = n__;
	for (j = 1; j <= n__; ++j) {
	    classq(m__, &A(1,j), c__1, &scale, &sum);
	}
	value = scale * sqrt(sum);
    }

    ret_val = value;
    return ret_val;

} 

