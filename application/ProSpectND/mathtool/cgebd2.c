

#include "complex.h"
#include "mathtool.h"


void cgebd2(int m__, int n__, fcomplex *a, int lda,
	    float *d, float *e, fcomplex *tauq, fcomplex *taup, fcomplex *work, 
	    int *info)
{
/*  -- LAPACK routine (version 2.0) --   
       Univ. of Tennessee, Univ. of California Berkeley, NAG Ltd.,   
       Courant Institute, Argonne National Lab, and Rice University   
       September 30, 1994   


    Purpose   
    =======   

    CGEBD2 reduces a complex general m by n matrix A to upper or lower   
    real bidiagonal form B by a unitary transformation: Q' * A * P = B.   

    If m >= n, B is upper bidiagonal; if m < n, B is lower bidiagonal.   

    Arguments   
    =========   

    M       (input) INTEGER   
            The number of rows in the matrix A.  M >= 0.   

    N       (input) INTEGER   
            The number of columns in the matrix A.  N >= 0.   

    A       (input/output) COMPLEX array, dimension (LDA,N)   
            On entry, the m by n general matrix to be reduced.   
            On exit,   
            if m >= n, the diagonal and the first superdiagonal are   
              overwritten with the upper bidiagonal matrix B; the   
              elements below the diagonal, with the array TAUQ, represent 
  
              the unitary matrix Q as a product of elementary   
              reflectors, and the elements above the first superdiagonal, 
  
              with the array TAUP, represent the unitary matrix P as   
              a product of elementary reflectors;   
            if m < n, the diagonal and the first subdiagonal are   
              overwritten with the lower bidiagonal matrix B; the   
              elements below the first subdiagonal, with the array TAUQ, 
  
              represent the unitary matrix Q as a product of   
              elementary reflectors, and the elements above the diagonal, 
  
              with the array TAUP, represent the unitary matrix P as   
              a product of elementary reflectors.   
            See Further Details.   

    LDA     (input) INTEGER   
            The leading dimension of the array A.  LDA >= max(1,M).   

    D       (output) REAL array, dimension (min(M,N))   
            The diagonal elements of the bidiagonal matrix B:   
            D(i) = A(i,i).   

    E       (output) REAL array, dimension (min(M,N)-1)   
            The off-diagonal elements of the bidiagonal matrix B:   
            if m >= n, E(i) = A(i,i+1) for i = 1,2,...,n-1;   
            if m < n, E(i) = A(i+1,i) for i = 1,2,...,m-1.   

    TAUQ    (output) COMPLEX array dimension (min(M,N))   
            The scalar factors of the elementary reflectors which   
            represent the unitary matrix Q. See Further Details.   

    TAUP    (output) COMPLEX array, dimension (min(M,N))   
            The scalar factors of the elementary reflectors which   
            represent the unitary matrix P. See Further Details.   

    WORK    (workspace) COMPLEX array, dimension (max(M,N))   

    INFO    (output) INTEGER   
            = 0: successful exit   
            < 0: if INFO = -i, the i-th argument had an illegal value.   

    Further Details   
    ===============   

    The matrices Q and P are represented as products of elementary   
    reflectors:   

    If m >= n,   

       Q = H(1) H(2) . . . H(n)  and  P = G(1) G(2) . . . G(n-1)   

    Each H(i) and G(i) has the form:   

       H(i) = I - tauq * v * v'  and G(i) = I - taup * u * u'   

    where tauq and taup are complex scalars, and v and u are complex   
    vectors; v(1:i-1) = 0, v(i) = 1, and v(i+1:m) is stored on exit in   
    A(i+1:m,i); u(1:i) = 0, u(i+1) = 1, and u(i+2:n) is stored on exit in 
  
    A(i,i+2:n); tauq is stored in TAUQ(i) and taup in TAUP(i).   

    If m < n,   

       Q = H(1) H(2) . . . H(m-1)  and  P = G(1) G(2) . . . G(m)   

    Each H(i) and G(i) has the form:   

       H(i) = I - tauq * v * v'  and G(i) = I - taup * u * u'   

    where tauq and taup are complex scalars, v and u are complex vectors; 
  
    v(1:i) = 0, v(i+1) = 1, and v(i+2:m) is stored on exit in A(i+2:m,i); 
  
    u(1:i-1) = 0, u(i) = 1, and u(i+1:n) is stored on exit in A(i,i+1:n); 
  
    tauq is stored in TAUQ(i) and taup in TAUP(i).   

    The contents of A on exit are illustrated by the following examples: 
  

    m = 6 and n = 5 (m > n):          m = 5 and n = 6 (m < n):   

      (  d   e   u1  u1  u1 )           (  d   u1  u1  u1  u1  u1 )   
      (  v1  d   e   u2  u2 )           (  e   d   u2  u2  u2  u2 )   
      (  v1  v2  d   e   u3 )           (  v1  e   d   u3  u3  u3 )   
      (  v1  v2  v3  d   e  )           (  v1  v2  e   d   u4  u4 )   
      (  v1  v2  v3  v4  d  )           (  v1  v2  v3  e   d   u5 )   
      (  v1  v2  v3  v4  v5 )   

    where d and e denote diagonal and off-diagonal elements of B, vi   
    denotes an element of the vector defining H(i), and ui an element of 
  
    the vector defining G(i).   

    ===================================================================== 
  


       Test the input parameters   

    
   Parameter adjustments   
       Function Body */
    /* Table of constant values */
    static int c__1 = 1;
    
    /* System generated locals */
    int a_dim1, i__1, i__2, i__3, i__4;
    fcomplex q__1;
    /* Local variables */
    static int i;
    static fcomplex alpha;



#define D(I) d[(I)-1]
#define E(I) e[(I)-1]
#define TAUQ(I) tauq[(I)-1]
#define TAUP(I) taup[(I)-1]
#define WORK(I) work[(I)-1]

#define A(I,J) a[(I)-1 + ((J)-1)* ( lda)]

    *info = 0;
    if (m__ < 0) {
	*info = -1;
    } else if (n__ < 0) {
	*info = -2;
    } else if (lda < max(1,m__)) {
	*info = -4;
    }
    if (*info < 0) {
	i__1 = -(*info);
	return ;
    }

    if (m__ >= n__) {

        /*
         *  Reduce to upper bidiagonal form 
         */

	i__1 = n__;
	for (i = 1; i <= n__; ++i) {

            /*
             * Generate elementary reflector H(i) to annihilate A(i+1:m,i) 
             */

	    i__2 = i + i * a_dim1;
	    alpha.r = A(i,i).r, alpha.i = A(i,i).i;
	    i__2 = m__ - i + 1;
            /* 
             * Computing MIN 
             */
	    i__3 = i + 1;
	    clarfg(i__2, &alpha, &A(min(i+1,m__),i), c__1, &
		    TAUQ(i));
	    i__2 = i;
	    D(i) = alpha.r;
	    i__2 = i + i * a_dim1;
	    A(i,i).r = 1.f, A(i,i).i = 0.f;

            /*
             *  Apply H(i)' to A(i:m,i+1:n) from the left 
             */

	    i__2 = m__ - i + 1;
	    i__3 = n__ - i;
	    r_cnjg(&q__1, &TAUQ(i));
	    clarf("Left", i__2, i__3, &A(i,i), c__1, q__1, &A(i,i+1), lda, &WORK(1));
	    i__2 = i + i * a_dim1;
	    i__3 = i;
	    A(i,i).r = D(i), A(i,i).i = 0.f;

	    if (i < n__) {

                /*
                 *  Generate elementary reflector G(i) to annihilate   
                 *  A(i,i+2:n) 
                 */

		i__2 = n__ - i;
		clacgv(i__2, &A(i,i+1), lda);
		i__2 = i + (i + 1) * a_dim1;
		alpha.r = A(i,i+1).r, alpha.i = A(i,i+1).i;
		i__2 = n__ - i;
                /*
                 * Computing MIN 
                 */
		i__3 = i + 2;
		clarfg(i__2, &alpha, &A(i,min(i+2,n__)), lda, &
			TAUP(i));
		i__2 = i;
		E(i) = alpha.r;
		i__2 = i + (i + 1) * a_dim1;
		A(i,i+1).r = 1.f, A(i,i+1).i = 0.f;

                /*
                 * Apply G(i) to A(i+1:m,i+1:n) from the right 
                 */

		i__2 = m__ - i;
		i__3 = n__ - i;
		clarf("Right", i__2, i__3, &A(i,i+1), lda, 
			TAUP(i), &A(i+1,i+1), lda, &WORK(1));
		i__2 = n__ - i;
		clacgv(i__2, &A(i,i+1), lda);
		i__2 = i + (i + 1) * a_dim1;
		i__3 = i;
		A(i,i+1).r = E(i), A(i,i+1).i = 0.f;
	    } else {
		i__2 = i;
		TAUP(i).r = 0.f, TAUP(i).i = 0.f;
	    }
	}
    } else {

        /*
         *  Reduce to lower bidiagonal form 
         */

	i__1 = m__;
	for (i = 1; i <= m__; ++i) {

            /*
             * Generate elementary reflector G(i) to annihilate A(i,i+1:n) 
             */

	    i__2 = n__ - i + 1;
	    clacgv(i__2, &A(i,i), lda);
	    i__2 = i + i * a_dim1;
	    alpha.r = A(i,i).r, alpha.i = A(i,i).i;
	    i__2 = n__ - i + 1;
            /*
             * Computing MIN 
             */
	    i__3 = i + 1;
	    clarfg(i__2, &alpha, &A(i,min(i+1,n__)), lda, &TAUP(
		    i));
	    i__2 = i;
	    D(i) = alpha.r;
	    i__2 = i + i * a_dim1;
	    A(i,i).r = 1.f, A(i,i).i = 0.f;

            /* 
             *  Apply G(i) to A(i+1:m,i:n) from the right 
             */

	    i__2 = m__ - i;
	    i__3 = n__ - i + 1;
            /*
             *   Computing MIN 
             */
	    i__4 = i + 1;
	    clarf("Right", i__2, i__3, &A(i,i), lda, TAUP(i), &
		    A(min(i+1,m__),i), lda, &WORK(1));
	    i__2 = n__ - i + 1;
	    clacgv(i__2, &A(i,i), lda);
	    i__2 = i + i * a_dim1;
	    i__3 = i;
	    A(i,i).r = D(i), A(i,i).i = 0.f;

	    if (i < m__) {

                /*
                 *  Generate elementary reflector H(i) to annihilate   
                 *  A(i+2:m,i) 
                 */

		i__2 = i + 1 + i * a_dim1;
		alpha.r = A(i+1,i).r, alpha.i = A(i+1,i).i;
		i__2 = m__ - i;
                /*
                 *          Computing MIN 
                 */
		i__3 = i + 2;
		clarfg(i__2, &alpha, &A(min(i+2,m__),i), c__1, &
			TAUQ(i));
		i__2 = i;
		E(i) = alpha.r;
		i__2 = i + 1 + i * a_dim1;
		A(i+1,i).r = 1.f, A(i+1,i).i = 0.f;

                /*
                 * Apply H(i)' to A(i+1:m,i+1:n) from the left 
                 */

		i__2 = m__ - i;
		i__3 = n__ - i;
		r_cnjg(&q__1, &TAUQ(i));
		clarf("Left", i__2, i__3, &A(i+1,i), c__1, 
			q__1, &A(i+1,i+1), lda, &WORK(1))
			;
		i__2 = i + 1 + i * a_dim1;
		i__3 = i;
		A(i+1,i).r = E(i), A(i+1,i).i = 0.f;
	    } else {
		i__2 = i;
		TAUQ(i).r = 0.f, TAUQ(i).i = 0.f;
	    }
	}
    }

} 

