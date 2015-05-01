
#include <math.h>
#include "complex.h"
#include "mathtool.h"



double clanhs(char *norm, int n__, fcomplex *a, int lda, float *work)
{
/*  -- LAPACK auxiliary routine (version 2.0) --   
       Univ. of Tennessee, Univ. of California Berkeley, NAG Ltd.,   
       Courant Institute, Argonne National Lab, and Rice University   
       October 31, 1992   


    Purpose   
    =======   

    CLANHS  returns the value of the one norm,  or the Frobenius norm, or 
  
    the  infinity norm,  or the  element of  largest absolute value  of a 
  
    Hessenberg matrix A.   

    Description   
    ===========   

    CLANHS returns the value   

       CLANHS = ( max(abs(A(i,j))), NORM = 'M' or 'm'   
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
            Specifies the value to be returned in CLANHS as described   
            above.   

    N       (input) INTEGER   
            The order of the matrix A.  N >= 0.  When N = 0, CLANHS is   
            set to zero.   

    A       (input) COMPLEX array, dimension (LDA,N)   
            The n by n upper Hessenberg matrix A; the part of A below the 
  
            first sub-diagonal is not referenced.   

    LDA     (input) INTEGER   
            The leading dimension of the array A.  LDA >= max(N,1).   

    WORK    (workspace) REAL array, dimension (LWORK),   
            where LWORK >= N when NORM = 'I'; otherwise, WORK is not   
            referenced.   

   ===================================================================== 
  


    
   Parameter adjustments   
       Function Body */
    /* Table of constant values */
    static int c__1 = 1;
    
    /* System generated locals */
    int  i__1, i__2, i__3, i__4;
    float ret_val, r__1, r__2;
    /* Local variables */
    static int i, j;
    static float scale;
    static float value;
    static float sum;



#define WORK(I) work[(I)-1]

#define A(I,J) a[(I)-1 + ((J)-1)* ( lda)]

    if (n__ == 0) {
	value = 0.f;
    } else if (lsame(norm, "M")) {

/*        Find max(abs(A(i,j))). */

	value = 0.f;
	i__1 = n__;
	for (j = 1; j <= n__; ++j) {
/* Computing MIN */
	    i__3 = n__, i__4 = j + 1;
	    i__2 = min(i__3,i__4);
	    for (i = 1; i <= min(n__,j+1); ++i) {
/* Computing MAX */
		r__1 = value, r__2 = Cabs(A(i,j));
		value = max(r__1,r__2);
	    }
	}
    } else if (lsame(norm, "O") || *(unsigned char *)norm == '1') {

/*        Find norm1(A). */

	value = 0.f;
	i__1 = n__;
	for (j = 1; j <= n__; ++j) {
	    sum = 0.f;
/* Computing MIN */
	    i__3 = n__, i__4 = j + 1;
	    i__2 = min(i__3,i__4);
	    for (i = 1; i <= min(n__,j+1); ++i) {
		sum += Cabs(A(i,j));
	    }
	    value = max(value,sum);
	}
    } else if (lsame(norm, "I")) {

/*        Find normI(A). */

	i__1 = n__;
	for (i = 1; i <= n__; ++i) {
	    WORK(i) = 0.f;
	}
	i__1 = n__;
	for (j = 1; j <= n__; ++j) {
/* Computing MIN */
	    i__3 = n__, i__4 = j + 1;
	    i__2 = min(i__3,i__4);
	    for (i = 1; i <= min(n__,j+1); ++i) {
		WORK(i) += Cabs(A(i,j));
	    }
	}
	value = 0.f;
	i__1 = n__;
	for (i = 1; i <= n__; ++i) {
/* Computing MAX */
	    r__1 = value, r__2 = WORK(i);
	    value = max(r__1,r__2);
	}
    } else if (lsame(norm, "F") || lsame(norm, "E")) {

/*        Find normF(A). */

	scale = 0.f;
	sum = 1.f;
	i__1 = n__;
	for (j = 1; j <= n__; ++j) {
/* Computing MIN */
	    i__3 = n__, i__4 = j + 1;
	    i__2 = min(i__3,i__4);
	    classq(i__2, &A(1,j), c__1, &scale, &sum);
	}
	value = scale * sqrt(sum);
    }

    ret_val = value;
    return ret_val;

} 

