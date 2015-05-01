
#include "complex.h"
#include "mathtool.h"

void cgehd2(int n__, int ilo, int ihi, fcomplex *
	    a, int lda, fcomplex *tau, fcomplex *work, int *info)
{
/*  -- LAPACK routine (version 2.0) --   
       Univ. of Tennessee, Univ. of California Berkeley, NAG Ltd.,   
       Courant Institute, Argonne National Lab, and Rice University   
       September 30, 1994   


    Purpose   
    =======   

    CGEHD2 reduces a complex general matrix A to upper Hessenberg form H 
  
    by a unitary similarity transformation:  Q' * A * Q = H .   

    Arguments   
    =========   

    N       (input) INTEGER   
            The order of the matrix A.  N >= 0.   

    ILO     (input) INTEGER   
    IHI     (input) INTEGER   
            It is assumed that A is already upper triangular in rows   
            and columns 1:ILO-1 and IHI+1:N. ILO and IHI are normally   
            set by a previous call to CGEBAL; otherwise they should be   
            set to 1 and N respectively. See Further Details.   
            1 <= ILO <= IHI <= max(1,N).   

    A       (input/output) COMPLEX array, dimension (LDA,N)   
            On entry, the n by n general matrix to be reduced.   
            On exit, the upper triangle and the first subdiagonal of A   
            are overwritten with the upper Hessenberg matrix H, and the   
            elements below the first subdiagonal, with the array TAU,   
            represent the unitary matrix Q as a product of elementary   
            reflectors. See Further Details.   

    LDA     (input) INTEGER   
            The leading dimension of the array A.  LDA >= max(1,N).   

    TAU     (output) COMPLEX array, dimension (N-1)   
            The scalar factors of the elementary reflectors (see Further 
  
            Details).   

    WORK    (workspace) COMPLEX array, dimension (N)   

    INFO    (output) INTEGER   
            = 0:  successful exit   
            < 0:  if INFO = -i, the i-th argument had an illegal value.   

    Further Details   
    ===============   

    The matrix Q is represented as a product of (ihi-ilo) elementary   
    reflectors   

       Q = H(ilo) H(ilo+1) . . . H(ihi-1).   

    Each H(i) has the form   

       H(i) = I - tau * v * v'   

    where tau is a complex scalar, and v is a complex vector with   
    v(1:i) = 0, v(i+1) = 1 and v(ihi+1:n) = 0; v(i+2:ihi) is stored on   
    exit in A(i+2:ihi,i), and tau in TAU(i).   

    The contents of A are illustrated by the following example, with   
    n = 7, ilo = 2 and ihi = 6:   

    on entry,                        on exit,   

    ( a   a   a   a   a   a   a )    (  a   a   h   h   h   h   a )   
    (     a   a   a   a   a   a )    (      a   h   h   h   h   a )   
    (     a   a   a   a   a   a )    (      h   h   h   h   h   h )   
    (     a   a   a   a   a   a )    (      v2  h   h   h   h   h )   
    (     a   a   a   a   a   a )    (      v2  v3  h   h   h   h )   
    (     a   a   a   a   a   a )    (      v2  v3  v4  h   h   h )   
    (                         a )    (                          a )   

    where a denotes an element of the original matrix A, h denotes a   
    modified element of the upper Hessenberg matrix H, and vi denotes an 
  
    element of the vector defining H(i).   

    ===================================================================== 
  


       Test the input parameters   

    
   Parameter adjustments   
       Function Body */
    /* Table of constant values */
    static int c__1 = 1;
    
    /* System generated locals */
    int a_dim1, i__1, i__2, i__3;
    fcomplex q__1;
    /* Local variables */
    static int i;
    static fcomplex alpha;


#define TAU(I) tau[(I)-1]
#define WORK(I) work[(I)-1]

#define A(I,J) a[(I)-1 + ((J)-1)* ( lda)]

    *info = 0;
    if (n__ < 0) {
	*info = -1;
    } else if (ilo < 1 || ilo > max(1,n__)) {
	*info = -2;
    } else if (ihi < min(ilo,n__) || ihi > n__) {
	*info = -3;
    } else if (lda < max(1,n__)) {
	*info = -5;
    }
    if (*info != 0) {
	i__1 = -(*info);
	return ;
    }

    i__1 = ihi - 1;
    for (i = ilo; i <= ihi-1; ++i) {

/*        Compute elementary reflector H(i) to annihilate A(i+2:ihi,i)
 */

	i__2 = i + 1 + i * a_dim1;
	alpha.r = A(i+1,i).r, alpha.i = A(i+1,i).i;
	i__2 = ihi - i;
/* Computing MIN */
	i__3 = i + 2;
	clarfg(i__2, &alpha, &A(min(i+2,n__),i), c__1, &TAU(i));
	i__2 = i + 1 + i * a_dim1;
	A(i+1,i).r = 1.f, A(i+1,i).i = 0.f;

/*        Apply H(i) to A(1:ihi,i+1:ihi) from the right */

	i__2 = ihi - i;
	clarf("Right", ihi, i__2, &A(i+1,i), c__1, TAU(i), &
		A(1,i+1), lda, &WORK(1));

/*        Apply H(i)' to A(i+1:ihi,i+1:n) from the left */

	i__2 = ihi - i;
	i__3 = n__ - i;
	r_cnjg(&q__1, &TAU(i));
	clarf("Left", i__2, i__3, &A(i+1,i), c__1, q__1, &A(i+1,i+1), 
              lda, &WORK(1));

	i__2 = i + 1 + i * a_dim1;
	A(i+1,i).r = alpha.r, A(i+1,i).i = alpha.i;

    }

    return ;

} 

