

#include "complex.h"
#include "mathtool.h"

void cgebrd(int m__, int n__, fcomplex *a, int lda,
	    float *d, float *e, fcomplex *tauq, fcomplex *taup, 
            fcomplex *work, int lwork, int *info)
{
/*  -- LAPACK routine (version 2.0) --   
       Univ. of Tennessee, Univ. of California Berkeley, NAG Ltd.,   
       Courant Institute, Argonne National Lab, and Rice University   
       September 30, 1994   


    Purpose   
    =======   

    CGEBRD reduces a general complex M-by-N matrix A to upper or lower   
    bidiagonal form B by a unitary transformation: Q**H * A * P = B.   

    If m >= n, B is upper bidiagonal; if m < n, B is lower bidiagonal.   

    Arguments   
    =========   

    M       (input) INTEGER   
            The number of rows in the matrix A.  M >= 0.   

    N       (input) INTEGER   
            The number of columns in the matrix A.  N >= 0.   

    A       (input/output) COMPLEX array, dimension (LDA,N)   
            On entry, the M-by-N general matrix to be reduced.   
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

    WORK    (workspace/output) COMPLEX array, dimension (LWORK)   
            On exit, if INFO = 0, WORK(1) returns the optimal LWORK.   

    LWORK   (input) INTEGER   
            The length of the array WORK.  LWORK >= max(1,M,N).   
            For optimum performance LWORK >= (M+N)*NB, where NB   
            is the optimal blocksize.   

    INFO    (output) INTEGER   
            = 0:  successful exit.   
            < 0:  if INFO = -i, the i-th argument had an illegal value.   

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

    where tauq and taup are complex scalars, and v and u are complex   
    vectors; v(1:i) = 0, v(i+1) = 1, and v(i+2:m) is stored on exit in   
    A(i+2:m,i); u(1:i-1) = 0, u(i) = 1, and u(i+1:n) is stored on exit in 
  
    A(i,i+1:n); tauq is stored in TAUQ(i) and taup in TAUP(i).   

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
    static fcomplex c_b1 = {1.f,0.f};
    static int c__1 = 1;
    static int c_n1 = -1;
    static int c__3 = 3;
    static int c__2 = 2;
    
    /* System generated locals */
    int a_dim1, i__1, i__2, i__3, i__4, i__5;
    fcomplex q__1;

    /* Local variables */
    static int i, j;
    static int nbmin, iinfo, minmn;
    static int nb;
    static int nx;
    static float ws;
    static int ldwrkx, ldwrky;



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
    } else /* if(complicated condition) */ {
/* Computing MAX */
	i__1 = max(1,m__);
	if (lwork < max(i__1,n__)) {
	    *info = -10;
	}
    }
    if (*info < 0) {
	i__1 = -(*info);
	return;
    }

    /*
     *     Quick return if possible 
     */

    minmn = min(m__,n__);
    if (minmn == 0) {
	WORK(1).r = 1.f, WORK(1).i = 0.f;
	return ;
    }

    ws = (float) max(m__,n__);
    ldwrkx = m__;
    ldwrky = n__;

    /*
     *     Set the block size NB and the crossover point NX.   
     *     Computing MAX 
     */
    i__1 = 1, i__2 = ilaenv(c__1, "CGEBRD", " ", m__, n__, c_n1, c_n1, 6L, 1L)
	    ;
    nb = max(i__1,i__2);

    if (nb > 1 && nb < minmn) {

        /*
         *        Determine when to switch from blocked to unblocked code.   
         *        Computing MAX 
         */
	i__1 = nb, i__2 = ilaenv(c__3, "CGEBRD", " ", m__, n__, c_n1, c_n1, 
		6L, 1L);
	nx = max(i__1,i__2);
	if (nx < minmn) {
	    ws = (float) ((m__ + n__) * nb);
	    if ((float) (lwork) < ws) {

                /*
                 *  Not enough work space for the optimal NB, 
                 *  consider using a smaller block size. 
                 */

		nbmin = ilaenv(c__2, "CGEBRD", " ", m__, n__, c_n1, c_n1, 6L, 
			       1L);
		if (lwork >= (m__ + n__) * nbmin) {
		    nb = lwork / (m__ + n__);
		} else {
		    nb = 1;
		    nx = minmn;
		}
	    }
	}
    } else {
	nx = minmn;
    }

    i__1 = minmn - nx;
    i__2 = nb;
    for (i = 1; nb < 0 ? i >= minmn-nx : i <= minmn-nx; i += nb) {

        /*
         *  Reduce rows and columns i:i+ib-1 to bidiagonal form and return   
         *  the matrices X and Y which are needed to update the unreduced   
         *  part of the matrix 
         */

	i__3 = m__ - i + 1;
	i__4 = n__ - i + 1;
	clabrd(i__3, i__4, nb, &A(i,i), lda, &D(i), &E(i), &
               TAUQ(i), &TAUP(i), &WORK(1), ldwrkx, &WORK(ldwrkx * nb + 1), 
		ldwrky);

        /*
         * Update the trailing submatrix A(i+ib:m,i+ib:n), using   
         * an update of the form  A := A - V*Y' - X*U' 
         */

	i__3 = m__ - i - nb + 1;
	i__4 = n__ - i - nb + 1;
	q__1.r = -1.f, q__1.i = 0.f;
	cgemm("No transpose", "Conjugate transpose", i__3, i__4, nb, 
		q__1, &A(i+nb,i), lda, &WORK(ldwrkx * nb + nb + 
		1), ldwrky, c_b1, &A(i+nb,i+nb), lda);
	i__3 = m__ - i - nb + 1;
	i__4 = n__ - i - nb + 1;
	q__1.r = -1.f, q__1.i = 0.f;
	cgemm("No transpose", "No transpose", i__3, i__4, nb, q__1, &
		WORK(nb + 1), ldwrkx, &A(i,i+nb), lda, c_b1, 
		&A(i+nb,i+nb), lda);

        /*
         *   Copy diagonal and off-diagonal elements of B back into A 
         */

	if (m__ >= n__) {
	    i__3 = i + nb - 1;
	    for (j = i; j <= i+nb-1; ++j) {
		i__4 = j + j * a_dim1;
		i__5 = j;
		A(j,j).r = D(j), A(j,j).i = 0.f;
		i__4 = j + (j + 1) * a_dim1;
		i__5 = j;
		A(j,j+1).r = E(j), A(j,j+1).i = 0.f;
	    }
	} else {
	    i__3 = i + nb - 1;
	    for (j = i; j <= i+nb-1; ++j) {
		i__4 = j + j * a_dim1;
		i__5 = j;
		A(j,j).r = D(j), A(j,j).i = 0.f;
		i__4 = j + 1 + j * a_dim1;
		i__5 = j;
		A(j+1,j).r = E(j), A(j+1,j).i = 0.f;
	    }
	}
    }

    /*
     *     Use unblocked code to reduce the remainder of the matrix 
     */

    i__2 = m__ - i + 1;
    i__1 = n__ - i + 1;
    cgebd2(i__2, i__1, &A(i,i), lda, &D(i), &E(i), &TAUQ(i), &
	    TAUP(i), &WORK(1), &iinfo);
    WORK(1).r = ws, WORK(1).i = 0.f;
    return;

} 

