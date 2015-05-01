

#include "complex.h"
#include "mathtool.h"

void cungl2(int m__, int n__, int k__, fcomplex *a, 
	    int lda, fcomplex *tau, fcomplex *work, int *info)
{
/*  -- LAPACK routine (version 2.0) --   
       Univ. of Tennessee, Univ. of California Berkeley, NAG Ltd.,   
       Courant Institute, Argonne National Lab, and Rice University   
       September 30, 1994   


    Purpose   
    =======   

    CUNGL2 generates an m-by-n complex matrix Q with orthonormal rows,   
    which is defined as the first m rows of a product of k elementary   
    reflectors of order n   

          Q  =  H(k)' . . . H(2)' H(1)'   

    as returned by CGELQF.   

    Arguments   
    =========   

    M       (input) INTEGER   
            The number of rows of the matrix Q. M >= 0.   

    N       (input) INTEGER   
            The number of columns of the matrix Q. N >= M.   

    K       (input) INTEGER   
            The number of elementary reflectors whose product defines the 
  
            matrix Q. M >= K >= 0.   

    A       (input/output) COMPLEX array, dimension (LDA,N)   
            On entry, the i-th row must contain the vector which defines 
  
            the elementary reflector H(i), for i = 1,2,...,k, as returned 
  
            by CGELQF in the first k rows of its array argument A.   
            On exit, the m by n matrix Q.   

    LDA     (input) INTEGER   
            The first dimension of the array A. LDA >= max(1,M).   

    TAU     (input) COMPLEX array, dimension (K)   
            TAU(i) must contain the scalar factor of the elementary   
            reflector H(i), as returned by CGELQF.   

    WORK    (workspace) COMPLEX array, dimension (M)   

    INFO    (output) INTEGER   
            = 0: successful exit   
            < 0: if INFO = -i, the i-th argument has an illegal value   

    ===================================================================== 
  


       Test the input arguments   

    
   Parameter adjustments   
       Function Body */
    /* System generated locals */
    int a_dim1, i__1, i__2, i__3;
    fcomplex q__1, q__2;
    /* Local variables */
    static int i, j, l;


#define TAU(I) tau[(I)-1]
#define WORK(I) work[(I)-1]

#define A(I,J) a[(I)-1 + ((J)-1)* ( lda)]

    *info = 0;
    if (m__ < 0) {
	*info = -1;
    } else if (n__ < m__) {
	*info = -2;
    } else if (k__ < 0 || k__ > m__) {
	*info = -3;
    } else if (lda < max(1,m__)) {
	*info = -5;
    }
    if (*info != 0) {
	i__1 = -(*info);
	return ;
    }

    /*
     *     Quick return if possible 
     */

    if (m__ <= 0) {
	return ;
    }

    if (k__ < m__) {

        /*
         *        Initialise rows k+1:m to rows of the unit matrix 
         */

	i__1 = n__;
	for (j = 1; j <= n__; ++j) {
	    i__2 = m__;
	    for (l = k__ + 1; l <= m__; ++l) {
		i__3 = l + j * a_dim1;
		A(l,j).r = 0.f, A(l,j).i = 0.f;
	    }
	    if (j > k__ && j <= m__) {
		i__2 = j + j * a_dim1;
		A(j,j).r = 1.f, A(j,j).i = 0.f;
	    }
	}
    }

    for (i = k__; i >= 1; --i) {

        /*
         *        Apply H(i)' to A(i:m,i:n) from the right 
         */

	if (i < n__) {
	    i__1 = n__ - i;
	    clacgv(i__1, &A(i,i+1), lda);
	    if (i < m__) {
		i__1 = i + i * a_dim1;
		A(i,i).r = 1.f, A(i,i).i = 0.f;
		i__1 = m__ - i;
		i__2 = n__ - i + 1;
		r_cnjg(&q__1, &TAU(i));
		clarf("Right", i__1, i__2, &A(i,i), lda, q__1, 
			&A(i+1,i), lda, &WORK(1));
	    }
	    i__1 = n__ - i;
	    i__2 = i;
	    q__1.r = -(double)TAU(i).r, q__1.i = -(double)TAU(i)
		    .i;
	    cscal(i__1, q__1, &A(i,i+1), lda);
	    i__1 = n__ - i;
	    clacgv(i__1, &A(i,i+1), lda);
	}
	i__1 = i + i * a_dim1;
	r_cnjg(&q__2, &TAU(i));
	q__1.r = 1.f - q__2.r, q__1.i = 0.f - q__2.i;
	A(i,i).r = q__1.r, A(i,i).i = q__1.i;

        /*   
         *     Set A(1:i-1,i) to zero 
         */

	i__1 = i - 1;
	for (l = 1; l <= i-1; ++l) {
	    i__2 = i + l * a_dim1;
	    A(i,l).r = 0.f, A(i,l).i = 0.f;
	}
    }

}

