

#include "complex.h"
#include "mathtool.h"

void cunghr(int n__, int ilo, int ihi, fcomplex *a, int lda, 
	    fcomplex *tau, fcomplex *work, int lwork, int *info)	    
{
/*  -- LAPACK routine (version 2.0) --   
       Univ. of Tennessee, Univ. of California Berkeley, NAG Ltd.,   
       Courant Institute, Argonne National Lab, and Rice University   
       September 30, 1994   


    Purpose   
    =======   

    CUNGHR generates a complex unitary matrix Q which is defined as the   
    product of IHI-ILO elementary reflectors of order N, as returned by   
    CGEHRD:   

    Q = H(ilo) H(ilo+1) . . . H(ihi-1).   

    Arguments   
    =========   

    N       (input) INTEGER   
            The order of the matrix Q. N >= 0.   

    ILO     (input) INTEGER   
    IHI     (input) INTEGER   
            ILO and IHI must have the same values as in the previous call 
  
            of CGEHRD. Q is equal to the unit matrix except in the   
            submatrix Q(ilo+1:ihi,ilo+1:ihi).   
            1 <= ILO <= IHI <= N, if N > 0; ILO=1 and IHI=0, if N=0.   

    A       (input/output) COMPLEX array, dimension (LDA,N)   
            On entry, the vectors which define the elementary reflectors, 
  
            as returned by CGEHRD.   
            On exit, the N-by-N unitary matrix Q.   

    LDA     (input) INTEGER   
            The leading dimension of the array A. LDA >= max(1,N).   

    TAU     (input) COMPLEX array, dimension (N-1)   
            TAU(i) must contain the scalar factor of the elementary   
            reflector H(i), as returned by CGEHRD.   

    WORK    (workspace/output) COMPLEX array, dimension (LWORK)   
            On exit, if INFO = 0, WORK(1) returns the optimal LWORK.   

    LWORK   (input) INTEGER   
            The dimension of the array WORK. LWORK >= IHI-ILO.   
            For optimum performance LWORK >= (IHI-ILO)*NB, where NB is   
            the optimal blocksize.   

    INFO    (output) INTEGER   
            = 0:  successful exit   
            < 0:  if INFO = -i, the i-th argument had an illegal value   

    ===================================================================== 
  


       Test the input arguments   

    
   Parameter adjustments   
       Function Body */
    /* System generated locals */
    int a_dim1, i__1, i__2, i__3, i__4;
    /* Local variables */
    static int i, j, iinfo, nh;


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
    } else /* if(complicated condition) */ {
/* Computing MAX */
	i__1 = 1, i__2 = ihi - ilo;
	if (lwork < max(i__1,i__2)) {
	    *info = -8;
	}
    }
    if (*info != 0) {
	i__1 = -(*info);
	return ;
    }

/*     Quick return if possible */

    if (n__ == 0) {
	WORK(1).r = 1.f, WORK(1).i = 0.f;
	return ;
    }

/*     Shift the vectors which define the elementary reflectors one   
       column to the right, and set the first ilo and the last n-ihi   
       rows and columns to those of the unit matrix */

    i__1 = ilo + 1;
    for (j = ihi; j >= ilo+1; --j) {
	i__2 = j - 1;
	for (i = 1; i <= j-1; ++i) {
	    i__3 = i + j * a_dim1;
	    A(i,j).r = 0.f, A(i,j).i = 0.f;
	}
	i__2 = ihi;
	for (i = j + 1; i <= ihi; ++i) {
	    i__3 = i + j * a_dim1;
	    i__4 = i + (j - 1) * a_dim1;
	    A(i,j).r = A(i,j-1).r, A(i,j).i = A(i,j-1).i;
	}
	i__2 = n__;
	for (i = ihi + 1; i <= n__; ++i) {
	    i__3 = i + j * a_dim1;
	    A(i,j).r = 0.f, A(i,j).i = 0.f;
	}
    }
    i__1 = ilo;
    for (j = 1; j <= ilo; ++j) {
	i__2 = n__;
	for (i = 1; i <= n__; ++i) {
	    i__3 = i + j * a_dim1;
	    A(i,j).r = 0.f, A(i,j).i = 0.f;
	}
	i__2 = j + j * a_dim1;
	A(j,j).r = 1.f, A(j,j).i = 0.f;
    }
    i__1 = n__;
    for (j = ihi + 1; j <= n__; ++j) {
	i__2 = n__;
	for (i = 1; i <= n__; ++i) {
	    i__3 = i + j * a_dim1;
	    A(i,j).r = 0.f, A(i,j).i = 0.f;
	}
	i__2 = j + j * a_dim1;
	A(j,j).r = 1.f, A(j,j).i = 0.f;
    }

    nh = ihi - ilo;
    if (nh > 0) {

/*        Generate Q(ilo+1:ihi,ilo+1:ihi) */

	cungqr(nh, nh, nh, &A(ilo+1,ilo+1), lda,
               &TAU(ilo), &WORK(1), lwork, &iinfo);
    }
    return ;


} 

