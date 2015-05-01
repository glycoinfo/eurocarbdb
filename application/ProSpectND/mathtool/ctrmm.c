
/*  -- translated by f2c (version 19940927).
   You must link the resulting object file with the libraries:
	-lf2c -lm   (in that order)
*/



#include "complex.h"
#include "mathtool.h"



void ctrmm(char *side, char *uplo, char *transa, char *diag, 
	   int m__, int n__, fcomplex alpha, fcomplex *a, int lda, 
	   fcomplex *b, int ldb)
{


    /* System generated locals */
    int a_dim1, b_dim1,  i__1, i__2, i__3, i__4, i__5, 
	    i__6;
    fcomplex q__1, q__2, q__3;

    /* Local variables */
    static int info;
    static fcomplex temp;
    static int i, j, k;
    static int lside;
    static int nrowa;
    static int upper;
    static int noconj, nounit;


/*  Purpose   
    =======   

    CTRMM  performs one of the matrix-matrix operations   

       B := alpha*op( A )*B,   or   B := alpha*B*op( A )   

    where  alpha  is a scalar,  B  is an m by n matrix,  A  is a unit, or 
  
    non-unit,  upper or lower triangular matrix  and  op( A )  is one  of 
  

       op( A ) = A   or   op( A ) = A'   or   op( A ) = conjg( A' ).   

    Parameters   
    ==========   

    SIDE   - CHARACTER*1.   
             On entry,  SIDE specifies whether  op( A ) multiplies B from 
  
             the left or right as follows:   

                SIDE = 'L' or 'l'   B := alpha*op( A )*B.   

                SIDE = 'R' or 'r'   B := alpha*B*op( A ).   

             Unchanged on exit.   

    UPLO   - CHARACTER*1.   
             On entry, UPLO specifies whether the matrix A is an upper or 
  
             lower triangular matrix as follows:   

                UPLO = 'U' or 'u'   A is an upper triangular matrix.   

                UPLO = 'L' or 'l'   A is a lower triangular matrix.   

             Unchanged on exit.   

    TRANSA - CHARACTER*1.   
             On entry, TRANSA specifies the form of op( A ) to be used in 
  
             the matrix multiplication as follows:   

                TRANSA = 'N' or 'n'   op( A ) = A.   

                TRANSA = 'T' or 't'   op( A ) = A'.   

                TRANSA = 'C' or 'c'   op( A ) = conjg( A' ).   

             Unchanged on exit.   

    DIAG   - CHARACTER*1.   
             On entry, DIAG specifies whether or not A is unit triangular 
  
             as follows:   

                DIAG = 'U' or 'u'   A is assumed to be unit triangular.   

                DIAG = 'N' or 'n'   A is not assumed to be unit   
                                    triangular.   

             Unchanged on exit.   

    M      - INTEGER.   
             On entry, M specifies the number of rows of B. M must be at 
  
             least zero.   
             Unchanged on exit.   

    N      - INTEGER.   
             On entry, N specifies the number of columns of B.  N must be 
  
             at least zero.   
             Unchanged on exit.   

    ALPHA  - COMPLEX         .   
             On entry,  ALPHA specifies the scalar  alpha. When  alpha is 
  
             zero then  A is not referenced and  B need not be set before 
  
             entry.   
             Unchanged on exit.   

    A      - COMPLEX          array of DIMENSION ( LDA, k ), where k is m 
  
             when  SIDE = 'L' or 'l'  and is  n  when  SIDE = 'R' or 'r'. 
  
             Before entry  with  UPLO = 'U' or 'u',  the  leading  k by k 
  
             upper triangular part of the array  A must contain the upper 
  
             triangular matrix  and the strictly lower triangular part of 
  
             A is not referenced.   
             Before entry  with  UPLO = 'L' or 'l',  the  leading  k by k 
  
             lower triangular part of the array  A must contain the lower 
  
             triangular matrix  and the strictly upper triangular part of 
  
             A is not referenced.   
             Note that when  DIAG = 'U' or 'u',  the diagonal elements of 
  
             A  are not referenced either,  but are assumed to be  unity. 
  
             Unchanged on exit.   

    LDA    - INTEGER.   
             On entry, LDA specifies the first dimension of A as declared 
  
             in the calling (sub) program.  When  SIDE = 'L' or 'l'  then 
  
             LDA  must be at least  max( 1, m ),  when  SIDE = 'R' or 'r' 
  
             then LDA must be at least max( 1, n ).   
             Unchanged on exit.   

    B      - COMPLEX          array of DIMENSION ( LDB, n ).   
             Before entry,  the leading  m by n part of the array  B must 
  
             contain the matrix  B,  and  on exit  is overwritten  by the 
  
             transformed matrix.   

    LDB    - INTEGER.   
             On entry, LDB specifies the first dimension of B as declared 
  
             in  the  calling  (sub)  program.   LDB  must  be  at  least 
  
             max( 1, m ).   
             Unchanged on exit.   


    Level 3 Blas routine.   

    -- Written on 8-February-1989.   
       Jack Dongarra, Argonne National Laboratory.   
       Iain Duff, AERE Harwell.   
       Jeremy Du Croz, Numerical Algorithms Group Ltd.   
       Sven Hammarling, Numerical Algorithms Group Ltd.   



       Test the input parameters.   

    
   Parameter adjustments   
       Function Body */

#define A(I,J) a[(I)-1 + ((J)-1)* ( lda)]
#define B(I,J) b[(I)-1 + ((J)-1)* ( ldb)]

    lside = lsame(side, "L");
    if (lside) {
	nrowa = m__;
    } else {
	nrowa = n__;
    }
    noconj = lsame(transa, "T");
    nounit = lsame(diag, "N");
    upper = lsame(uplo, "U");

    info = 0;
    if (! lside && ! lsame(side, "R")) {
	info = 1;
    } else if (! upper && ! lsame(uplo, "L")) {
	info = 2;
    } else if (! lsame(transa, "N") && ! lsame(transa, "T") 
	    && ! lsame(transa, "C")) {
	info = 3;
    } else if (! lsame(diag, "U") && ! lsame(diag, "N")) {
	info = 4;
    } else if (m__ < 0) {
	info = 5;
    } else if (n__ < 0) {
	info = 6;
    } else if (lda < max(1,nrowa)) {
	info = 9;
    } else if (ldb < max(1,m__)) {
	info = 11;
    }
    if (info != 0) {
	return ;
    }

    /*   
     *    Quick return if possible. 
     */

    if (n__ == 0) {
	return ;
    }

    /* 
     *    And when  alpha.eq.zero. 
     */

    if (alpha.r == 0.f && alpha.i == 0.f) {
	i__1 = n__;
	for (j = 1; j <= n__; ++j) {
	    i__2 = m__;
	    for (i = 1; i <= m__; ++i) {
		i__3 = i + j * b_dim1;
		B(i,j).r = 0.f, B(i,j).i = 0.f;
	    }
	}
	return ;
    }

    /*   
     *  Start the operations. 
     */

    if (lside) {
	if (lsame(transa, "N")) {

            /*
             *           Form  B := alpha*A*B. 
             */

	    if (upper) {
		i__1 = n__;
		for (j = 1; j <= n__; ++j) {
		    i__2 = m__;
		    for (k = 1; k <= m__; ++k) {
			i__3 = k + j * b_dim1;
			if (B(k,j).r != 0.f || B(k,j).i != 0.f) {
			    i__3 = k + j * b_dim1;
			    q__1.r = alpha.r * B(k,j).r - alpha.i * B(k,j)
				    .i, q__1.i = alpha.r * B(k,j).i + 
				    alpha.i * B(k,j).r;
			    temp.r = q__1.r, temp.i = q__1.i;
			    i__3 = k - 1;
			    for (i = 1; i <= k-1; ++i) {
				i__4 = i + j * b_dim1;
				i__5 = i + j * b_dim1;
				i__6 = i + k * a_dim1;
				q__2.r = temp.r * A(i,k).r - temp.i * A(i,k)
					.i, q__2.i = temp.r * A(i,k).i + 
					temp.i * A(i,k).r;
				q__1.r = B(i,j).r + q__2.r, q__1.i = B(i,j)
					.i + q__2.i;
				B(i,j).r = q__1.r, B(i,j).i = q__1.i;
			    }
			    if (nounit) {
				i__3 = k + k * a_dim1;
				q__1.r = temp.r * A(k,k).r - temp.i * A(k,k)
					.i, q__1.i = temp.r * A(k,k).i + 
					temp.i * A(k,k).r;
				temp.r = q__1.r, temp.i = q__1.i;
			    }
			    i__3 = k + j * b_dim1;
			    B(k,j).r = temp.r, B(k,j).i = temp.i;
			}
		    }
		}
	    } else {
		i__1 = n__;
		for (j = 1; j <= n__; ++j) {
		    for (k = m__; k >= 1; --k) {
			i__2 = k + j * b_dim1;
			if (B(k,j).r != 0.f || B(k,j).i != 0.f) {
			    i__2 = k + j * b_dim1;
			    q__1.r = alpha.r * B(k,j).r - alpha.i * B(k,j)
				    .i, q__1.i = alpha.r * B(k,j).i + 
				    alpha.i * B(k,j).r;
			    temp.r = q__1.r, temp.i = q__1.i;
			    i__2 = k + j * b_dim1;
			    B(k,j).r = temp.r, B(k,j).i = temp.i;
			    if (nounit) {
				i__2 = k + j * b_dim1;
				i__3 = k + j * b_dim1;
				i__4 = k + k * a_dim1;
				q__1.r = B(k,j).r * A(k,k).r - B(k,j).i * 
					A(k,k).i, q__1.i = B(k,j).r * A(k,k).i + B(k,j).i * A(k,k).r;
				B(k,j).r = q__1.r, B(k,j).i = q__1.i;
			    }
			    i__2 = m__;
			    for (i = k + 1; i <= m__; ++i) {
				i__3 = i + j * b_dim1;
				i__4 = i + j * b_dim1;
				i__5 = i + k * a_dim1;
				q__2.r = temp.r * A(i,k).r - temp.i * A(i,k)
					.i, q__2.i = temp.r * A(i,k).i + 
					temp.i * A(i,k).r;
				q__1.r = B(i,j).r + q__2.r, q__1.i = B(i,j)
					.i + q__2.i;
				B(i,j).r = q__1.r, B(i,j).i = q__1.i;
			    }
			}
		    }
		}
	    }
	} else {

            /* 
             *    Form  B := alpha*B*A'   or   B := alpha*B*conjg( A' ). 
             */

	    if (upper) {
		i__1 = n__;
		for (j = 1; j <= n__; ++j) {
		    for (i = m__; i >= 1; --i) {
			i__2 = i + j * b_dim1;
			temp.r = B(i,j).r, temp.i = B(i,j).i;
			if (noconj) {
			    if (nounit) {
				i__2 = i + i * a_dim1;
				q__1.r = temp.r * A(i,i).r - temp.i * A(i,i)
					.i, q__1.i = temp.r * A(i,i).i + 
					temp.i * A(i,i).r;
				temp.r = q__1.r, temp.i = q__1.i;
			    }
			    i__2 = i - 1;
			    for (k = 1; k <= i-1; ++k) {
				i__3 = k + i * a_dim1;
				i__4 = k + j * b_dim1;
				q__2.r = A(k,i).r * B(k,j).r - A(k,i).i * 
					B(k,j).i, q__2.i = A(k,i).r * B(k,j).i + A(k,i).i * B(k,j).r;
				q__1.r = temp.r + q__2.r, q__1.i = temp.i + 
					q__2.i;
				temp.r = q__1.r, temp.i = q__1.i;
			    }
			} else {
			    if (nounit) {
				r_cnjg(&q__2, &A(i,i));
				q__1.r = temp.r * q__2.r - temp.i * q__2.i, 
					q__1.i = temp.r * q__2.i + temp.i * 
					q__2.r;
				temp.r = q__1.r, temp.i = q__1.i;
			    }
			    i__2 = i - 1;
			    for (k = 1; k <= i-1; ++k) {
				r_cnjg(&q__3, &A(k,i));
				i__3 = k + j * b_dim1;
				q__2.r = q__3.r * B(k,j).r - q__3.i * B(k,j)
					.i, q__2.i = q__3.r * B(k,j).i + 
					q__3.i * B(k,j).r;
				q__1.r = temp.r + q__2.r, q__1.i = temp.i + 
					q__2.i;
				temp.r = q__1.r, temp.i = q__1.i;
			    }
			}
			i__2 = i + j * b_dim1;
			q__1.r = alpha.r * temp.r - alpha.i * temp.i, 
				q__1.i = alpha.r * temp.i + alpha.i * 
				temp.r;
			B(i,j).r = q__1.r, B(i,j).i = q__1.i;
		    }
		}
	    } else {
		i__1 = n__;
		for (j = 1; j <= n__; ++j) {
		    i__2 = m__;
		    for (i = 1; i <= m__; ++i) {
			i__3 = i + j * b_dim1;
			temp.r = B(i,j).r, temp.i = B(i,j).i;
			if (noconj) {
			    if (nounit) {
				i__3 = i + i * a_dim1;
				q__1.r = temp.r * A(i,i).r - temp.i * A(i,i)
					.i, q__1.i = temp.r * A(i,i).i + 
					temp.i * A(i,i).r;
				temp.r = q__1.r, temp.i = q__1.i;
			    }
			    i__3 = m__;
			    for (k = i + 1; k <= m__; ++k) {
				i__4 = k + i * a_dim1;
				i__5 = k + j * b_dim1;
				q__2.r = A(k,i).r * B(k,j).r - A(k,i).i * 
					B(k,j).i, q__2.i = A(k,i).r * B(k,j).i + A(k,i).i * B(k,j).r;
				q__1.r = temp.r + q__2.r, q__1.i = temp.i + 
					q__2.i;
				temp.r = q__1.r, temp.i = q__1.i;
			    }
			} else {
			    if (nounit) {
				r_cnjg(&q__2, &A(i,i));
				q__1.r = temp.r * q__2.r - temp.i * q__2.i, 
					q__1.i = temp.r * q__2.i + temp.i * 
					q__2.r;
				temp.r = q__1.r, temp.i = q__1.i;
			    }
			    i__3 = m__;
			    for (k = i + 1; k <= m__; ++k) {
				r_cnjg(&q__3, &A(k,i));
				i__4 = k + j * b_dim1;
				q__2.r = q__3.r * B(k,j).r - q__3.i * B(k,j)
					.i, q__2.i = q__3.r * B(k,j).i + 
					q__3.i * B(k,j).r;
				q__1.r = temp.r + q__2.r, q__1.i = temp.i + 
					q__2.i;
				temp.r = q__1.r, temp.i = q__1.i;
			    }
			}
			i__3 = i + j * b_dim1;
			q__1.r = alpha.r * temp.r - alpha.i * temp.i, 
				q__1.i = alpha.r * temp.i + alpha.i * 
				temp.r;
			B(i,j).r = q__1.r, B(i,j).i = q__1.i;
		    }
		}
	    }
	}
    } else {
	if (lsame(transa, "N")) {

            /* 
             *          Form  B := alpha*B*A. 
             */

	    if (upper) {
		for (j = n__; j >= 1; --j) {
		    temp.r = alpha.r, temp.i = alpha.i;
		    if (nounit) {
			i__1 = j + j * a_dim1;
			q__1.r = temp.r * A(j,j).r - temp.i * A(j,j).i, 
				q__1.i = temp.r * A(j,j).i + temp.i * A(j,j)
				.r;
			temp.r = q__1.r, temp.i = q__1.i;
		    }
		    i__1 = m__;
		    for (i = 1; i <= m__; ++i) {
			i__2 = i + j * b_dim1;
			i__3 = i + j * b_dim1;
			q__1.r = temp.r * B(i,j).r - temp.i * B(i,j).i, 
				q__1.i = temp.r * B(i,j).i + temp.i * B(i,j)
				.r;
			B(i,j).r = q__1.r, B(i,j).i = q__1.i;
		    }
		    i__1 = j - 1;
		    for (k = 1; k <= j-1; ++k) {
			i__2 = k + j * a_dim1;
			if (A(k,j).r != 0.f || A(k,j).i != 0.f) {
			    i__2 = k + j * a_dim1;
			    q__1.r = alpha.r * A(k,j).r - alpha.i * A(k,j)
				    .i, q__1.i = alpha.r * A(k,j).i + 
				    alpha.i * A(k,j).r;
			    temp.r = q__1.r, temp.i = q__1.i;
			    i__2 = m__;
			    for (i = 1; i <= m__; ++i) {
				i__3 = i + j * b_dim1;
				i__4 = i + j * b_dim1;
				i__5 = i + k * b_dim1;
				q__2.r = temp.r * B(i,k).r - temp.i * B(i,k)
					.i, q__2.i = temp.r * B(i,k).i + 
					temp.i * B(i,k).r;
				q__1.r = B(i,j).r + q__2.r, q__1.i = B(i,j)
					.i + q__2.i;
				B(i,j).r = q__1.r, B(i,j).i = q__1.i;
			    }
			}
		    }
		}
	    } else {
		i__1 = n__;
		for (j = 1; j <= n__; ++j) {
		    temp.r = alpha.r, temp.i = alpha.i;
		    if (nounit) {
			i__2 = j + j * a_dim1;
			q__1.r = temp.r * A(j,j).r - temp.i * A(j,j).i, 
				q__1.i = temp.r * A(j,j).i + temp.i * A(j,j)
				.r;
			temp.r = q__1.r, temp.i = q__1.i;
		    }
		    i__2 = m__;
		    for (i = 1; i <= m__; ++i) {
			i__3 = i + j * b_dim1;
			i__4 = i + j * b_dim1;
			q__1.r = temp.r * B(i,j).r - temp.i * B(i,j).i, 
				q__1.i = temp.r * B(i,j).i + temp.i * B(i,j)
				.r;
			B(i,j).r = q__1.r, B(i,j).i = q__1.i;
		    }
		    i__2 = n__;
		    for (k = j + 1; k <= n__; ++k) {
			i__3 = k + j * a_dim1;
			if (A(k,j).r != 0.f || A(k,j).i != 0.f) {
			    i__3 = k + j * a_dim1;
			    q__1.r = alpha.r * A(k,j).r - alpha.i * A(k,j)
				    .i, q__1.i = alpha.r * A(k,j).i + 
				    alpha.i * A(k,j).r;
			    temp.r = q__1.r, temp.i = q__1.i;
			    i__3 = m__;
			    for (i = 1; i <= m__; ++i) {
				i__4 = i + j * b_dim1;
				i__5 = i + j * b_dim1;
				i__6 = i + k * b_dim1;
				q__2.r = temp.r * B(i,k).r - temp.i * B(i,k)
					.i, q__2.i = temp.r * B(i,k).i + 
					temp.i * B(i,k).r;
				q__1.r = B(i,j).r + q__2.r, q__1.i = B(i,j)
					.i + q__2.i;
				B(i,j).r = q__1.r, B(i,j).i = q__1.i;
			    }
			}
		    }
		}
	    }
	} else {

            /* 
             *      Form  B := alpha*B*A'   or   B := alpha*B*conjg( A' ). 
             */

	    if (upper) {
		i__1 = n__;
		for (k = 1; k <= n__; ++k) {
		    i__2 = k - 1;
		    for (j = 1; j <= k-1; ++j) {
			i__3 = j + k * a_dim1;
			if (A(j,k).r != 0.f || A(j,k).i != 0.f) {
			    if (noconj) {
				i__3 = j + k * a_dim1;
				q__1.r = alpha.r * A(j,k).r - alpha.i * A(j,k).i, q__1.i = alpha.r * A(j,k)
					.i + alpha.i * A(j,k).r;
				temp.r = q__1.r, temp.i = q__1.i;
			    } else {
				r_cnjg(&q__2, &A(j,k));
				q__1.r = alpha.r * q__2.r - alpha.i * 
					q__2.i, q__1.i = alpha.r * q__2.i + 
					alpha.i * q__2.r;
				temp.r = q__1.r, temp.i = q__1.i;
			    }
			    i__3 = m__;
			    for (i = 1; i <= m__; ++i) {
				i__4 = i + j * b_dim1;
				i__5 = i + j * b_dim1;
				i__6 = i + k * b_dim1;
				q__2.r = temp.r * B(i,k).r - temp.i * B(i,k)
					.i, q__2.i = temp.r * B(i,k).i + 
					temp.i * B(i,k).r;
				q__1.r = B(i,j).r + q__2.r, q__1.i = B(i,j)
					.i + q__2.i;
				B(i,j).r = q__1.r, B(i,j).i = q__1.i;
			    }
			}
		    }
		    temp.r = alpha.r, temp.i = alpha.i;
		    if (nounit) {
			if (noconj) {
			    i__2 = k + k * a_dim1;
			    q__1.r = temp.r * A(k,k).r - temp.i * A(k,k).i, 
				    q__1.i = temp.r * A(k,k).i + temp.i * A(k,k).r;
			    temp.r = q__1.r, temp.i = q__1.i;
			} else {
			    r_cnjg(&q__2, &A(k,k));
			    q__1.r = temp.r * q__2.r - temp.i * q__2.i, 
				    q__1.i = temp.r * q__2.i + temp.i * 
				    q__2.r;
			    temp.r = q__1.r, temp.i = q__1.i;
			}
		    }
		    if (temp.r != 1.f || temp.i != 0.f) {
			i__2 = m__;
			for (i = 1; i <= m__; ++i) {
			    i__3 = i + k * b_dim1;
			    i__4 = i + k * b_dim1;
			    q__1.r = temp.r * B(i,k).r - temp.i * B(i,k).i, 
				    q__1.i = temp.r * B(i,k).i + temp.i * B(i,k).r;
			    B(i,k).r = q__1.r, B(i,k).i = q__1.i;
			}
		    }
		}
	    } else {
		for (k = n__; k >= 1; --k) {
		    i__1 = n__;
		    for (j = k + 1; j <= n__; ++j) {
			i__2 = j + k * a_dim1;
			if (A(j,k).r != 0.f || A(j,k).i != 0.f) {
			    if (noconj) {
				i__2 = j + k * a_dim1;
				q__1.r = alpha.r * A(j,k).r - alpha.i * A(j,k).i, q__1.i = alpha.r * A(j,k)
					.i + alpha.i * A(j,k).r;
				temp.r = q__1.r, temp.i = q__1.i;
			    } else {
				r_cnjg(&q__2, &A(j,k));
				q__1.r = alpha.r * q__2.r - alpha.i * 
					q__2.i, q__1.i = alpha.r * q__2.i + 
					alpha.i * q__2.r;
				temp.r = q__1.r, temp.i = q__1.i;
			    }
			    i__2 = m__;
			    for (i = 1; i <= m__; ++i) {
				i__3 = i + j * b_dim1;
				i__4 = i + j * b_dim1;
				i__5 = i + k * b_dim1;
				q__2.r = temp.r * B(i,k).r - temp.i * B(i,k)
					.i, q__2.i = temp.r * B(i,k).i + 
					temp.i * B(i,k).r;
				q__1.r = B(i,j).r + q__2.r, q__1.i = B(i,j)
					.i + q__2.i;
				B(i,j).r = q__1.r, B(i,j).i = q__1.i;
			    }
			}
		    }
		    temp.r = alpha.r, temp.i = alpha.i;
		    if (nounit) {
			if (noconj) {
			    i__1 = k + k * a_dim1;
			    q__1.r = temp.r * A(k,k).r - temp.i * A(k,k).i, 
				    q__1.i = temp.r * A(k,k).i + temp.i * A(k,k).r;
			    temp.r = q__1.r, temp.i = q__1.i;
			} else {
			    r_cnjg(&q__2, &A(k,k));
			    q__1.r = temp.r * q__2.r - temp.i * q__2.i, 
				    q__1.i = temp.r * q__2.i + temp.i * 
				    q__2.r;
			    temp.r = q__1.r, temp.i = q__1.i;
			}
		    }
		    if (temp.r != 1.f || temp.i != 0.f) {
			i__1 = m__;
			for (i = 1; i <= m__; ++i) {
			    i__2 = i + k * b_dim1;
			    i__3 = i + k * b_dim1;
			    q__1.r = temp.r * B(i,k).r - temp.i * B(i,k).i, 
				    q__1.i = temp.r * B(i,k).i + temp.i * B(i,k).r;
			    B(i,k).r = q__1.r, B(i,k).i = q__1.i;
			}
		    }
		}
	    }
	}
    }


} 

