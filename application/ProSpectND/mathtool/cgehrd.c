

#include "complex.h"
#include "mathtool.h"


void cgehrd(int n__, int ilo, int ihi, fcomplex *a,
	    int lda, fcomplex *tau, fcomplex *work, int lwork, int 
	    *info)
{
/*  -- LAPACK routine (version 2.0) --   
       Univ. of Tennessee, Univ. of California Berkeley, NAG Ltd.,   
       Courant Institute, Argonne National Lab, and Rice University   
       September 30, 1994   


    Purpose   
    =======   

    CGEHRD reduces a complex general matrix A to upper Hessenberg form H 
  
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
            1 <= ILO <= IHI <= N, if N > 0; ILO=1 and IHI=0, if N=0.   

    A       (input/output) COMPLEX array, dimension (LDA,N)   
            On entry, the N-by-N general matrix to be reduced.   
            On exit, the upper triangle and the first subdiagonal of A   
            are overwritten with the upper Hessenberg matrix H, and the   
            elements below the first subdiagonal, with the array TAU,   
            represent the unitary matrix Q as a product of elementary   
            reflectors. See Further Details.   

    LDA     (input) INTEGER   
            The leading dimension of the array A.  LDA >= max(1,N).   

    TAU     (output) COMPLEX array, dimension (N-1)   
            The scalar factors of the elementary reflectors (see Further 
  
            Details). Elements 1:ILO-1 and IHI:N-1 of TAU are set to   
            zero.   

    WORK    (workspace/output) COMPLEX array, dimension (LWORK)   
            On exit, if INFO = 0, WORK(1) returns the optimal LWORK.   

    LWORK   (input) INTEGER   
            The length of the array WORK.  LWORK >= max(1,N).   
            For optimum performance LWORK >= N*NB, where NB is the   
            optimal blocksize.   

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
    static fcomplex c_b2 = {1.f,0.f};
    static int c__1 = 1;
    static int c_n1 = -1;
    static int c__3 = 3;
    static int c__2 = 2;
    static int c__65 = 65;
    
    /* System generated locals */
    int a_dim1, i__1, i__2, i__3, i__4;
    fcomplex q__1;
    /* Local variables */
    static int i;
    static fcomplex t[4160]	/* was [65][64] */;
    static int nbmin, iinfo;
    static int ib;
    static fcomplex ei;
    static int nb, nh;
    static int nx;
    static int ldwork, iws;



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
    } else if (lwork < max(1,n__)) {
	*info = -8;
    }
    if (*info != 0) {
	i__1 = -(*info);
	return ;
    }

/*     Set elements 1:ILO-1 and IHI:N-1 of TAU to zero */

    i__1 = ilo - 1;
    for (i = 1; i <= ilo-1; ++i) {
	i__2 = i;
	TAU(i).r = 0.f, TAU(i).i = 0.f;
    }
    i__1 = n__ - 1;
    for (i = max(1,ihi); i <= n__-1; ++i) {
	i__2 = i;
	TAU(i).r = 0.f, TAU(i).i = 0.f;
    }

/*     Quick return if possible */

    nh = ihi - ilo + 1;
    if (nh <= 1) {
	WORK(1).r = 1.f, WORK(1).i = 0.f;
	return ;
    }

/*     Determine the block size.   

   Computing MIN */
    i__1 = 64, i__2 = ilaenv(c__1, "CGEHRD", " ", n__, ilo, ihi, c_n1, 6L, 
	    1L);
    nb = min(i__1,i__2);
    nbmin = 2;
    iws = 1;
    if (nb > 1 && nb < nh) {

/*        Determine when to cross over from blocked to unblocked code 
  
          (last block is always handled by unblocked code).   

   Computing MAX */
	i__1 = nb, i__2 = ilaenv(c__3, "CGEHRD", " ", n__, ilo, ihi, c_n1, 
		6L, 1L);
	nx = max(i__1,i__2);
	if (nx < nh) {

/*           Determine if workspace is large enough for blocked code. */

	    iws = n__ * nb;
	    if (lwork < iws) {

/*              Not enough workspace to use optimal NB:  determine the   
                minimum value of NB, and reduce NB or force use of   
                unblocked code.   

   Computing MAX */
		i__1 = 2, i__2 = ilaenv(c__2, "CGEHRD", " ", n__, ilo, ihi, 
			c_n1, 6L, 1L);
		nbmin = max(i__1,i__2);
		if (lwork >= n__ * nbmin) {
		    nb = lwork / n__;
		} else {
		    nb = 1;
		}
	    }
	}
    }
    ldwork = n__;

    if (nb < nbmin || nb >= nh) {

/*        Use unblocked code below */

	i = ilo;

    } else {

/*        Use blocked code */

	i__1 = ihi - 1 - nx;
	i__2 = nb;
	for (i = ilo; nb < 0 ? i >= ihi-1-nx : i <= ihi-1-nx; i += nb) {
/* Computing MIN */
	    i__3 = nb, i__4 = ihi - i;
	    ib = min(i__3,i__4);

/*           Reduce columns i:i+ib-1 to Hessenberg form, returning the   
             matrices V and T of the block reflector H = I - V*T*V'   
             which performs the reduction, and also the matrix Y = A*V*T */

	    clahrd(ihi, i, ib, &A(1,i), lda, &TAU(i), t, c__65,
		     &WORK(1), ldwork);

/*           Apply the block reflector H to A(1:ihi,i+ib:ihi) from the   
             right, computing  A := A - Y * V'. V(i+ib,ib-1) must be set   
             to 1. */

	    i__3 = i + ib + (i + ib - 1) * a_dim1;
	    ei.r = A(i+ib,i+ib-1).r, ei.i = A(i+ib,i+ib-1).i;
	    i__3 = i + ib + (i + ib - 1) * a_dim1;
	    A(i+ib,i+ib-1).r = 1.f, A(i+ib,i+ib-1).i = 0.f;
	    i__3 = ihi - i - ib + 1;
	    q__1.r = -1.f, q__1.i = 0.f;
	    cgemm("No transpose", "Conjugate transpose", ihi, i__3, ib, 
		    q__1, &WORK(1), ldwork, &A(i+ib,i), lda, 
		    c_b2, &A(1,i+ib), lda);
	    i__3 = i + ib + (i + ib - 1) * a_dim1;
	    A(i+ib,i+ib-1).r = ei.r, A(i+ib,i+ib-1).i = ei.i;

/*           Apply the block reflector H to A(i+1:ihi,i+ib:n) from the   
             left */

	    i__3 = ihi - i;
	    i__4 = n__ - i - ib + 1;
	    clarfb("Left", "Conjugate transpose", "Forward", "Columnwise", 
		    i__3, i__4, ib, &A(i+1,i), lda, t, c__65, 
		    &A(i+1,i+ib), lda, &WORK(1), ldwork);
	}
    }

/*     Use unblocked code to reduce the rest of the matrix */

    cgehd2(n__, i, ihi, &A(1,1), lda, &TAU(1), &WORK(1), &iinfo);
    WORK(1).r = (float) iws, WORK(1).i = 0.f;

    return ;
} 

