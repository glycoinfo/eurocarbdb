

#include "complex.h"
#include "mathtool.h"


void clabrd(int m__, int n__, int nb_, fcomplex *a, 
	    int lda, float *d, float *e, fcomplex *tauq, fcomplex *taup, 
            fcomplex *x, int ldx, fcomplex *y, int ldy)
{
/*  -- LAPACK auxiliary routine (version 2.0) --   
       Univ. of Tennessee, Univ. of California Berkeley, NAG Ltd.,   
       Courant Institute, Argonne National Lab, and Rice University   
       September 30, 1994   


    Purpose   
    =======   

    CLABRD reduces the first NB rows and columns of a complex general   
    m by n matrix A to upper or lower real bidiagonal form by a unitary   
    transformation Q' * A * P, and returns the matrices X and Y which   
    are needed to apply the transformation to the unreduced part of A.   

    If m >= n, A is reduced to upper bidiagonal form; if m < n, to lower 
  
    bidiagonal form.   

    This is an auxiliary routine called by CGEBRD   

    Arguments   
    =========   

    M       (input) INTEGER   
            The number of rows in the matrix A.   

    N       (input) INTEGER   
            The number of columns in the matrix A.   

    NB      (input) INTEGER   
            The number of leading rows and columns of A to be reduced.   

    A       (input/output) COMPLEX array, dimension (LDA,N)   
            On entry, the m by n general matrix to be reduced.   
            On exit, the first NB rows and columns of the matrix are   
            overwritten; the rest of the array is unchanged.   
            If m >= n, elements on and below the diagonal in the first NB 
  
              columns, with the array TAUQ, represent the unitary   
              matrix Q as a product of elementary reflectors; and   
              elements above the diagonal in the first NB rows, with the 
  
              array TAUP, represent the unitary matrix P as a product   
              of elementary reflectors.   
            If m < n, elements below the diagonal in the first NB   
              columns, with the array TAUQ, represent the unitary   
              matrix Q as a product of elementary reflectors, and   
              elements on and above the diagonal in the first NB rows,   
              with the array TAUP, represent the unitary matrix P as   
              a product of elementary reflectors.   
            See Further Details.   

    LDA     (input) INTEGER   
            The leading dimension of the array A.  LDA >= max(1,M).   

    D       (output) REAL array, dimension (NB)   
            The diagonal elements of the first NB rows and columns of   
            the reduced matrix.  D(i) = A(i,i).   

    E       (output) REAL array, dimension (NB)   
            The off-diagonal elements of the first NB rows and columns of 
  
            the reduced matrix.   

    TAUQ    (output) COMPLEX array dimension (NB)   
            The scalar factors of the elementary reflectors which   
            represent the unitary matrix Q. See Further Details.   

    TAUP    (output) COMPLEX array, dimension (NB)   
            The scalar factors of the elementary reflectors which   
            represent the unitary matrix P. See Further Details.   

    X       (output) COMPLEX array, dimension (LDX,NB)   
            The m-by-nb matrix X required to update the unreduced part   
            of A.   

    LDX     (input) INTEGER   
            The leading dimension of the array X. LDX >= max(1,M).   

    Y       (output) COMPLEX array, dimension (LDY,NB)   
            The n-by-nb matrix Y required to update the unreduced part   
            of A.   

    LDY     (output) INTEGER   
            The leading dimension of the array Y. LDY >= max(1,N).   

    Further Details   
    ===============   

    The matrices Q and P are represented as products of elementary   
    reflectors:   

       Q = H(1) H(2) . . . H(nb)  and  P = G(1) G(2) . . . G(nb)   

    Each H(i) and G(i) has the form:   

       H(i) = I - tauq * v * v'  and G(i) = I - taup * u * u'   

    where tauq and taup are complex scalars, and v and u are complex   
    vectors.   

    If m >= n, v(1:i-1) = 0, v(i) = 1, and v(i:m) is stored on exit in   
    A(i:m,i); u(1:i) = 0, u(i+1) = 1, and u(i+1:n) is stored on exit in   
    A(i,i+1:n); tauq is stored in TAUQ(i) and taup in TAUP(i).   

    If m < n, v(1:i) = 0, v(i+1) = 1, and v(i+1:m) is stored on exit in   
    A(i+2:m,i); u(1:i-1) = 0, u(i) = 1, and u(i:n) is stored on exit in   
    A(i,i+1:n); tauq is stored in TAUQ(i) and taup in TAUP(i).   

    The elements of the vectors v and u together form the m-by-nb matrix 
  
    V and the nb-by-n matrix U' which are needed, with X and Y, to apply 
  
    the transformation to the unreduced part of the matrix, using a block 
  
    update of the form:  A := A - V*Y' - X*U'.   

    The contents of A on exit are illustrated by the following examples   
    with nb = 2:   

    m = 6 and n = 5 (m > n):          m = 5 and n = 6 (m < n):   

      (  1   1   u1  u1  u1 )           (  1   u1  u1  u1  u1  u1 )   
      (  v1  1   1   u2  u2 )           (  1   1   u2  u2  u2  u2 )   
      (  v1  v2  a   a   a  )           (  v1  1   a   a   a   a  )   
      (  v1  v2  a   a   a  )           (  v1  v2  a   a   a   a  )   
      (  v1  v2  a   a   a  )           (  v1  v2  a   a   a   a  )   
      (  v1  v2  a   a   a  )   

    where a denotes an element of the original matrix which is unchanged, 
  
    vi denotes an element of the vector defining H(i), and ui an element 
  
    of the vector defining G(i).   

    ===================================================================== 
  


       Quick return if possible   

    
   Parameter adjustments   
       Function Body */
    /* Table of constant values */
    static fcomplex c_b1 = {0.f,0.f};
    static fcomplex c_b2 = {1.f,0.f};
    static int c__1 = 1;
    
    /* System generated locals */
    int a_dim1, i__1, i__2, i__3;
    fcomplex q__1;
    /* Local variables */
    static int i;
    static fcomplex alpha;



#define D(I) d[(I)-1]
#define E(I) e[(I)-1]
#define TAUQ(I) tauq[(I)-1]
#define TAUP(I) taup[(I)-1]

#define A(I,J) a[(I)-1 + ((J)-1)* ( lda)]
#define X(I,J) x[(I)-1 + ((J)-1)* ( ldx)]
#define Y(I,J) y[(I)-1 + ((J)-1)* ( ldy)]

    if (m__ <= 0 || n__ <= 0) {
	return ;
    }

    if (m__ >= n__) {

        /* 
         *       Reduce to upper bidiagonal form 
         */

	i__1 = nb_;
	for (i = 1; i <= nb_; ++i) {

            /*  
             *         Update A(i:m,i) 
             */

	    i__2 = i - 1;
	    clacgv(i__2, &Y(i,1), ldy);
	    i__2 = m__ - i + 1;
	    i__3 = i - 1;
	    q__1.r = -1.f, q__1.i = 0.f;
	    cgemv("No transpose", i__2, i__3, q__1, &A(i,1), lda, 
		   &Y(i,1), ldy, c_b2, &A(i,i), c__1)
		    ;
	    i__2 = i - 1;
	    clacgv(i__2, &Y(i,1), ldy);
	    i__2 = m__ - i + 1;
	    i__3 = i - 1;
	    q__1.r = -1.f, q__1.i = 0.f;
	    cgemv("No transpose", i__2, i__3, q__1, &X(i,1), ldx, 
                   &A(1,i), c__1, c_b2, &A(i,i), c__1);

            /*
             *           Generate reflection Q(i) to annihilate A(i+1:m,i) 
             */

	    i__2 = i + i * a_dim1;
	    alpha.r = A(i,i).r, alpha.i = A(i,i).i;
	    i__2 = m__ - i + 1;
            /* 
             *   Computing MIN 
             */
	    i__3 = i + 1;
	    clarfg(i__2, &alpha, &A(min(i+1,m__),i), c__1, &TAUQ(i));
	    i__2 = i;
	    D(i) = alpha.r;
	    if (i < n__) {
		i__2 = i + i * a_dim1;
		A(i,i).r = 1.f, A(i,i).i = 0.f;

                /* 
                 *             Compute Y(i+1:n,i) 
                 */

		i__2 = m__ - i + 1;
		i__3 = n__ - i;
		cgemv("Conjugate transpose", i__2, i__3, c_b2, &A(i,i+1),
                       lda, &A(i,i), c__1, c_b1,&Y(i+1,i), c__1);
		i__2 = m__ - i + 1;
		i__3 = i - 1;
		cgemv("Conjugate transpose", i__2, i__3, c_b2, &A(i,1), 
                       lda, &A(i,i), c__1, c_b1, &Y(1,i), c__1);
		i__2 = n__ - i;
		i__3 = i - 1;
		q__1.r = -1.f, q__1.i = 0.f;
		cgemv("No transpose", i__2, i__3, q__1, &Y(i+1,1)
			, ldy, &Y(1,i), c__1, c_b2, &Y(i+1,i), c__1);
		i__2 = m__ - i + 1;
		i__3 = i - 1;
		cgemv("Conjugate transpose", i__2, i__3, c_b2, &X(i,1), 
                       ldx, &A(i,i), c__1, c_b1, &Y(1,i), c__1);
		i__2 = i - 1;
		i__3 = n__ - i;
		q__1.r = -1.f, q__1.i = 0.f;
		cgemv("Conjugate transpose", i__2, i__3, q__1, &A(1,i+1), 
                       lda, &Y(1,i), c__1, c_b2,&Y(i+1,i), c__1);
		i__2 = n__ - i;
		cscal(i__2, TAUQ(i), &Y(i+1,i), c__1);

                /*
                 *              Update A(i,i+1:n) 
                 */

		i__2 = n__ - i;
		clacgv(i__2, &A(i,i+1), lda);
		clacgv(i, &A(i,1), lda);
		i__2 = n__ - i;
		q__1.r = -1.f, q__1.i = 0.f;
		cgemv("No transpose", i__2, i, q__1, &Y(i+1,1), 
			ldy, &A(i,1), lda, c_b2, &A(i,i+1), lda);
		clacgv(i, &A(i,1), lda);
		i__2 = i - 1;
		clacgv(i__2, &X(i,1), ldx);
		i__2 = i - 1;
		i__3 = n__ - i;
		q__1.r = -1.f, q__1.i = 0.f;
		cgemv("Conjugate transpose", i__2, i__3, q__1, &A(1,i+1),
                       lda, &X(i,1), ldx, c_b2, &A(i,i+1), lda);
		i__2 = i - 1;
		clacgv(i__2, &X(i,1), ldx);

                /*
                 * Generate reflection P(i) to annihilate A(i,i+2:n) 
                 */

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
                 *             Compute X(i+1:m,i) 
                 */

		i__2 = m__ - i;
		i__3 = n__ - i;
		cgemv("No transpose", i__2, i__3, c_b2, &A(i+1,i+1), 
                       lda, &A(i,i+1), lda, c_b1, &X(i+1,i), c__1);
		i__2 = n__ - i;
		cgemv("Conjugate transpose", i__2, i, c_b2, &Y(i+1,1), 
                       ldy, &A(i,i+1), lda, c_b1, &X(1,i), c__1);
		i__2 = m__ - i;
		q__1.r = -1.f, q__1.i = 0.f;
		cgemv("No transpose", i__2, i, q__1, &A(i+1,1), 
			lda, &X(1,i), c__1, c_b2, &X(i+1,i), c__1);
		i__2 = i - 1;
		i__3 = n__ - i;
		cgemv("No transpose", i__2, i__3, c_b2, &A(1,i+1), 
                       lda, &A(i,i+1), lda, c_b1, &X(1,i), c__1);
		i__2 = m__ - i;
		i__3 = i - 1;
		q__1.r = -1.f, q__1.i = 0.f;
		cgemv("No transpose", i__2, i__3, q__1, &X(i+1,1)
			, ldx, &X(1,i), c__1, c_b2, &X(i+1,i), c__1);
		i__2 = m__ - i;
		cscal(i__2, TAUP(i), &X(i+1,i), c__1);
		i__2 = n__ - i;
		clacgv(i__2, &A(i,i+1), lda);
	    }
	}
    } else {

        /* 
         *       Reduce to lower bidiagonal form 
         */

	i__1 = nb_;
	for (i = 1; i <= nb_; ++i) {

            /*
             *           Update A(i,i:n) 
             */

	    i__2 = n__ - i + 1;
	    clacgv(i__2, &A(i,i), lda);
	    i__2 = i - 1;
	    clacgv(i__2, &A(i,1), lda);
	    i__2 = n__ - i + 1;
	    i__3 = i - 1;
	    q__1.r = -1.f, q__1.i = 0.f;
	    cgemv("No transpose", i__2, i__3, q__1, &Y(i,1), 
                   ldy, &A(i,1), lda, c_b2, &A(i,i), lda);
	    i__2 = i - 1;
	    clacgv(i__2, &A(i,1), lda);
	    i__2 = i - 1;
	    clacgv(i__2, &X(i,1), ldx);
	    i__2 = i - 1;
	    i__3 = n__ - i + 1;
	    q__1.r = -1.f, q__1.i = 0.f;
	    cgemv("Conjugate transpose", i__2, i__3, q__1, &A(1,i), 
                   lda, &X(i,1), ldx, c_b2, &A(i,i),lda);
	    i__2 = i - 1;
	    clacgv(i__2, &X(i,1), ldx);

            /*
             *           Generate reflection P(i) to annihilate A(i,i+1:n) 
             */

	    i__2 = i + i * a_dim1;
	    alpha.r = A(i,i).r, alpha.i = A(i,i).i;
	    i__2 = n__ - i + 1;
            /* 
             * Computing MIN 
             */
	    i__3 = i + 1;
	    clarfg(i__2, &alpha, &A(i,min(i+1,n__)), lda, &TAUP(i));
	    i__2 = i;
	    D(i) = alpha.r;
	    if (i < m__) {
		i__2 = i + i * a_dim1;
		A(i,i).r = 1.f, A(i,i).i = 0.f;

               /*  
                *             Compute X(i+1:m,i) 
                */

		i__2 = m__ - i;
		i__3 = n__ - i + 1;
		cgemv("No transpose", i__2, i__3, c_b2, &A(i+1,i), 
                       lda, &A(i,i), lda, c_b1, &X(i+1,i), c__1);
		i__2 = n__ - i + 1;
		i__3 = i - 1;
		cgemv("Conjugate transpose", i__2, i__3, c_b2, &Y(i,1), 
                       ldy, &A(i,i), lda, c_b1, &X(1,i), c__1);
		i__2 = m__ - i;
		i__3 = i - 1;
		q__1.r = -1.f, q__1.i = 0.f;
		cgemv("No transpose", i__2, i__3, q__1, &A(i+1,1)
			, lda, &X(1,i), c__1, c_b2, &X(i+1,i), c__1);
		i__2 = i - 1;
		i__3 = n__ - i + 1;
		cgemv("No transpose", i__2, i__3, c_b2, &A(1,i)
			, lda, &A(i,i), lda, c_b1, &X(1,i), c__1);
		i__2 = m__ - i;
		i__3 = i - 1;
		q__1.r = -1.f, q__1.i = 0.f;
		cgemv("No transpose", i__2, i__3, q__1, &X(i+1,1)
			, ldx, &X(1,i), c__1, c_b2, &X(i+1,i),c__1);
		i__2 = m__ - i;
		cscal(i__2, TAUP(i), &X(i+1,i), c__1);
		i__2 = n__ - i + 1;
		clacgv(i__2, &A(i,i), lda);

                /*
                 *              Update A(i+1:m,i) 
                 */

		i__2 = i - 1;
		clacgv(i__2, &Y(i,1), ldy);
		i__2 = m__ - i;
		i__3 = i - 1;
		q__1.r = -1.f, q__1.i = 0.f;
		cgemv("No transpose", i__2, i__3, q__1, &A(i+1,1)
			, lda, &Y(i,1), ldy, c_b2, &A(i+1,i), c__1);
		i__2 = i - 1;
		clacgv(i__2, &Y(i,1), ldy);
		i__2 = m__ - i;
		q__1.r = -1.f, q__1.i = 0.f;
		cgemv("No transpose", i__2, i, q__1, &X(i+1,1), 
			ldx, &A(1,i), c__1, c_b2, &A(i+1,i), c__1);

                /* 
                 *   Generate reflection Q(i) to annihilate A(i+2:m,i) 
                 */

		i__2 = i + 1 + i * a_dim1;
		alpha.r = A(i+1,i).r, alpha.i = A(i+1,i).i;
		i__2 = m__ - i;
                /*
                 * Computing MIN 
                 */
		i__3 = i + 2;
		clarfg(i__2, &alpha, &A(min(i+2,m__),i), c__1, &
			TAUQ(i));
		i__2 = i;
		E(i) = alpha.r;
		i__2 = i + 1 + i * a_dim1;
		A(i+1,i).r = 1.f, A(i+1,i).i = 0.f;

                /*
                 *              Compute Y(i+1:n,i) 
                 */

		i__2 = m__ - i;
		i__3 = n__ - i;
		cgemv("Conjugate transpose", i__2, i__3, c_b2, &A(i+1,i+1), 
                      lda, &A(i+1,i), c__1, c_b1, &Y(i+1,i), c__1);
		i__2 = m__ - i;
		i__3 = i - 1;
		cgemv("Conjugate transpose", i__2, i__3, c_b2, &A(i+1,1), 
                       lda, &A(i+1,i), c__1, c_b1, &Y(1,i), c__1);
		i__2 = n__ - i;
		i__3 = i - 1;
		q__1.r = -1.f, q__1.i = 0.f;
		cgemv("No transpose", i__2, i__3, q__1, &Y(i+1,1),
		      ldy, &Y(1,i), c__1, c_b2, &Y(i+1,i), c__1);
		i__2 = m__ - i;
		cgemv("Conjugate transpose", i__2, i, c_b2, &X(i+1,1), 
                       ldx, &A(i+1,i), c__1, c_b1, &Y(1,i), c__1);
		i__2 = n__ - i;
		q__1.r = -1.f, q__1.i = 0.f;
		cgemv("Conjugate transpose", i, i__2, q__1, &A(1,i+1), 
                       lda, &Y(1,i), c__1, c_b2, &Y(i+1,i), c__1);
		i__2 = n__ - i;
		cscal(i__2, TAUQ(i), &Y(i+1,i), c__1);
	    } else {
		i__2 = n__ - i + 1;
		clacgv(i__2, &A(i,i), lda);
	    }
	}
    }

} 

