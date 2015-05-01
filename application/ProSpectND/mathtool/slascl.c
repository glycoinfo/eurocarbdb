
#include <math.h>
#include "complex.h"        
#include "mathtool.h"


void slascl(char *type, int kl_, int ku_, float 
	    cfrom, float cto, int m__, int n__, float *a, int lda, 
	    int *info)
{
/*  -- LAPACK auxiliary routine (version 2.0) --   
       Univ. of Tennessee, Univ. of California Berkeley, NAG Ltd.,   
       Courant Institute, Argonne National Lab, and Rice University   
       February 29, 1992   


    Purpose   
    =======   

    SLASCL multiplies the M by N real matrix A by the real scalar   
    CTO/CFROM.  This is done without over/underflow as long as the final 
  
    result CTO*A(I,J)/CFROM does not over/underflow. TYPE specifies that 
  
    A may be full, upper triangular, lower triangular, upper Hessenberg, 
  
    or banded.   

    Arguments   
    =========   

    TYPE    (input) CHARACTER*1   
            TYPE indices the storage type of the input matrix.   
            = 'G':  A is a full matrix.   
            = 'L':  A is a lower triangular matrix.   
            = 'U':  A is an upper triangular matrix.   
            = 'H':  A is an upper Hessenberg matrix.   
            = 'B':  A is a symmetric band matrix with lower bandwidth KL 
  
                    and upper bandwidth KU and with the only the lower   
                    half stored.   
            = 'Q':  A is a symmetric band matrix with lower bandwidth KL 
  
                    and upper bandwidth KU and with the only the upper   
                    half stored.   
            = 'Z':  A is a band matrix with lower bandwidth KL and upper 
  
                    bandwidth KU.   

    KL      (input) INTEGER   
            The lower bandwidth of A.  Referenced only if TYPE = 'B',   
            'Q' or 'Z'.   

    KU      (input) INTEGER   
            The upper bandwidth of A.  Referenced only if TYPE = 'B',   
            'Q' or 'Z'.   

    CFROM   (input) REAL   
    CTO     (input) REAL   
            The matrix A is multiplied by CTO/CFROM. A(I,J) is computed   
            without over/underflow if the final result CTO*A(I,J)/CFROM   
            can be represented without over/underflow.  CFROM must be   
            nonzero.   

    M       (input) INTEGER   
            The number of rows of the matrix A.  M >= 0.   

    N       (input) INTEGER   
            The number of columns of the matrix A.  N >= 0.   

    A       (input/output) REAL array, dimension (LDA,M)   
            The matrix to be multiplied by CTO/CFROM.  See TYPE for the   
            storage type.   

    LDA     (input) INTEGER   
            The leading dimension of the array A.  LDA >= max(1,M).   

    INFO    (output) INTEGER   
            0  - successful exit   
            <0 - if INFO = -i, the i-th argument had an illegal value.   

    ===================================================================== 
  


       Test the input arguments   

    
   Parameter adjustments   
       Function Body */
    /* System generated locals */
    int i__1, i__2, i__3, i__4, i__5;
    /* Local variables */
    static int done;
    static float ctoc;
    static int i, j;
    static int itype, k1, k2, k3, k4;
    static float cfrom1;
    static float cfromc;
    static float bignum, smlnum, mul, cto1;



#define A(I,J) a[(I)-1 + ((J)-1)* ( lda)]

    *info = 0;

    if (lsame(type, "G")) {
	itype = 0;
    } else if (lsame(type, "L")) {
	itype = 1;
    } else if (lsame(type, "U")) {
	itype = 2;
    } else if (lsame(type, "H")) {
	itype = 3;
    } else if (lsame(type, "B")) {
	itype = 4;
    } else if (lsame(type, "Q")) {
	itype = 5;
    } else if (lsame(type, "Z")) {
	itype = 6;
    } else {
	itype = -1;
    }

    if (itype == -1) {
	*info = -1;
    } else if (cfrom == 0.f) {
	*info = -4;
    } else if (m__ < 0) {
	*info = -6;
    } else if (n__ < 0 || (itype == 4 && n__ != m__) || (itype == 5 && n__ != m__)) {
	*info = -7;
    } else if (itype <= 3 && lda < max(1,m__)) {
	*info = -9;
    } else if (itype >= 4) {
        /*
         * Computing MAX 
         */
	i__1 = m__ - 1;
	if (kl_ < 0 || kl_ > max(i__1,0)) {
	    *info = -2;
	} else /* if(complicated condition) */ {
            /*
             * Computing MAX 
             */
	    i__1 = n__ - 1;
	    if (ku_ < 0 || ku_ > max(i__1,0) || ((itype == 4 || itype == 5) && 
		    kl_ != ku_)) {
		*info = -3;
	    } else if ((itype == 4 && lda < kl_ + 1) || (itype == 5 && lda < 
		    ku_ + 1) || (itype == 6 && lda < (kl_ << 1) + ku_ + 1)) {
		*info = -9;
	    }
	}
    }

    if (*info != 0) {
	i__1 = -(*info);
	return ;
    }

    /*
     *     Quick return if possible 
     */

    if (n__ == 0 || m__ == 0) {
	return ;
    }

    /*
     *     Get machine parameters 
     */

    smlnum = slamch("S");
    bignum = 1.f / smlnum;

    cfromc = cfrom;
    ctoc = cto;

L10:
    cfrom1 = cfromc * smlnum;
    cto1 = ctoc / bignum;
    if (fabs(cfrom1) > fabs(ctoc) && ctoc != 0.f) {
	mul = smlnum;
	done = FALSE;
	cfromc = cfrom1;
    } else if (fabs(cto1) > fabs(cfromc)) {
	mul = bignum;
	done = FALSE;
	ctoc = cto1;
    } else {
	mul = ctoc / cfromc;
	done = TRUE;
    }

    if (itype == 0) {

        /*
         *        Full matrix 
         */

	i__1 = n__;
	for (j = 1; j <= n__; ++j) {
	    i__2 = m__;
	    for (i = 1; i <= m__; ++i) {
		A(i,j) *= mul;
	    }
	}

    } else if (itype == 1) {

        /*
         *        Lower triangular matrix 
         */

	i__1 = n__;
	for (j = 1; j <= n__; ++j) {
	    i__2 = m__;
	    for (i = j; i <= m__; ++i) {
		A(i,j) *= mul;
	    }
	}

    } else if (itype == 2) {

        /*
         *        Upper triangular matrix 
         */

	i__1 = n__;
	for (j = 1; j <= n__; ++j) {
	    i__2 = min(j,m__);
	    for (i = 1; i <= min(j,m__); ++i) {
		A(i,j) *= mul;
	    }
	}

    } else if (itype == 3) {

        /*
         *        Upper Hessenberg matrix 
         */

	i__1 = n__;
	for (j = 1; j <= n__; ++j) {
            /*
             * Computing MIN 
             */
	    i__3 = j + 1;
	    i__2 = min(i__3,m__);
	    for (i = 1; i <= min(j+1,m__); ++i) {
		A(i,j) *= mul;
	    }
	}

    } else if (itype == 4) {

        /*
         *        Lower half of a symmetric band matrix 
         */

	k3 = kl_ + 1;
	k4 = n__ + 1;
	i__1 = n__;
	for (j = 1; j <= n__; ++j) {
            /*
             * Computing MIN 
             */
	    i__3 = k3, i__4 = k4 - j;
	    i__2 = min(i__3,i__4);
	    for (i = 1; i <= min(k3,k4-j); ++i) {
		A(i,j) *= mul;
	    }
	}

    } else if (itype == 5) {

        /*
         *        Upper half of a symmetric band matrix 
         */

	k1 = ku_ + 2;
	k3 = ku_ + 1;
	i__1 = n__;
	for (j = 1; j <= n__; ++j) {
            /*
             * Computing MAX 
             */
	    i__2 = k1 - j;
	    i__3 = k3;
	    for (i = max(k1-j,1); i <= k3; ++i) {
		A(i,j) *= mul;
	    }
	}

    } else if (itype == 6) {

        /*
         *        Band matrix 
         */

	k1 = kl_ + ku_ + 2;
	k2 = kl_ + 1;
	k3 = (kl_ << 1) + ku_ + 1;
	k4 = kl_ + ku_ + 1 + m__;
	i__1 = n__;
	for (j = 1; j <= n__; ++j) {
            /*
             * Computing MAX 
             */
	    i__3 = k1 - j;
            /*
             * Computing MIN 
             */
	    i__4 = k3, i__5 = k4 - j;
	    i__2 = min(i__4,i__5);
	    for (i = max(k1-j,k2); i <= min(k3,k4-j); ++i) {
		A(i,j) *= mul;
	    }
	}

    }

    if (! done) {
	goto L10;
    }


} 

