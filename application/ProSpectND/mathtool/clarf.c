

#include "complex.h"
#include "mathtool.h"



void clarf(char *side, int m__, int n__, fcomplex *v, 
	   int incv, fcomplex tau, fcomplex *c, int ldc, fcomplex *work)
{
/*  -- LAPACK auxiliary routine (version 2.0) --   
       Univ. of Tennessee, Univ. of California Berkeley, NAG Ltd.,   
       Courant Institute, Argonne National Lab, and Rice University   
       September 30, 1994   


    Purpose   
    =======   

    CLARF applies a complex elementary reflector H to a complex M-by-N   
    matrix C, from either the left or the right. H is represented in the 
  
    form   

          H = I - tau * v * v'   

    where tau is a complex scalar and v is a complex vector.   

    If tau = 0, then H is taken to be the unit matrix.   

    To apply H' (the conjugate transpose of H), supply conjg(tau) instead 
  
    tau.   

    Arguments   
    =========   

    SIDE    (input) CHARACTER*1   
            = 'L': form  H * C   
            = 'R': form  C * H   

    M       (input) INTEGER   
            The number of rows of the matrix C.   

    N       (input) INTEGER   
            The number of columns of the matrix C.   

    V       (input) COMPLEX array, dimension   
                       (1 + (M-1)*abs(INCV)) if SIDE = 'L'   
                    or (1 + (N-1)*abs(INCV)) if SIDE = 'R'   
            The vector v in the representation of H. V is not used if   
            TAU = 0.   

    INCV    (input) INTEGER   
            The increment between elements of v. INCV <> 0.   

    TAU     (input) COMPLEX   
            The value tau in the representation of H.   

    C       (input/output) COMPLEX array, dimension (LDC,N)   
            On entry, the M-by-N matrix C.   
            On exit, C is overwritten by the matrix H * C if SIDE = 'L', 
  
            or C * H if SIDE = 'R'.   

    LDC     (input) INTEGER   
            The leading dimension of the array C. LDC >= max(1,M).   

    WORK    (workspace) COMPLEX array, dimension   
                           (N) if SIDE = 'L'   
                        or (M) if SIDE = 'R'   

    ===================================================================== 
  


    
   Parameter adjustments   
       Function Body */
    /* Table of constant values */
    static fcomplex c_b1 = {1.f,0.f};
    static fcomplex c_b2 = {0.f,0.f};
    static int c__1 = 1;
    
    /* System generated locals */
    fcomplex q__1;



#define V(I) v[(I)-1]
#define WORK(I) work[(I)-1]

#define C(I,J) c[(I)-1 + ((J)-1)* ( ldc)]

    if (lsame(side, "L")) {

        /*
         *        Form  H * C 
         */

	if (tau.r != 0.f || tau.i != 0.f) {

            /*
             *           w := C' * v 
             */

	    cgemv("Conjugate transpose", m__, n__, c_b1, &C(1,1), ldc, &V(
		    1), incv, c_b2, &WORK(1), c__1);

            /* 
             *          C := C - v * w' 
             */

	    q__1.r = -(double)tau.r, q__1.i = -(double)tau.i;
	    cgerc(m__, n__, q__1, &V(1), incv, &WORK(1), c__1, &C(1,1), 
		    ldc);
	}
    } else {

        /*
         *        Form  C * H 
         */

	if (tau.r != 0.f || tau.i != 0.f) {

            /*
             *           w := C * v 
             */

	    cgemv("No transpose", m__, n__, c_b1, &C(1,1), ldc, &V(1), 
		    incv, c_b2, &WORK(1), c__1);

            /*
             *           C := C - w * v' 
             */

	    q__1.r = -(double)tau.r, q__1.i = -(double)tau.i;
	    cgerc(m__, n__, q__1, &WORK(1), c__1, &V(1), incv, &C(1,1), 
		    ldc);
	}
    }

} 

