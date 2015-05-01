

#include <math.h>
#include "complex.h"
#include "mathtool.h"



void csrscl(int n__, float sa_, fcomplex *sx, int incx)
{
/*  -- LAPACK auxiliary routine (version 2.0) --   
       Univ. of Tennessee, Univ. of California Berkeley, NAG Ltd.,   
       Courant Institute, Argonne National Lab, and Rice University   
       September 30, 1994   


    Purpose   
    =======   

    CSRSCL multiplies an n-element complex vector x by the real scalar   
    1/a.  This is done without overflow or underflow as long as   
    the final result x/a does not overflow or underflow.   

    Arguments   
    =========   

    N       (input) INTEGER   
            The number of components of the vector x.   

    SA      (input) REAL   
            The scalar a which is used to divide each component of x.   
            SA must be >= 0, or the subroutine will divide by zero.   

    SX      (input/output) COMPLEX array, dimension   
                           (1+(N-1)*abs(INCX))   
            The n-element vector x.   

    INCX    (input) INTEGER   
            The increment between successive values of the vector SX.   
            > 0:  SX(1) = X(1) and SX(1+(i-1)*INCX) = x(i),     1< i<= n 
  

   ===================================================================== 
  


       Quick return if possible   

    
   Parameter adjustments   
       Function Body */
    static float cden;
    static int   done;
    static float cnum, cden1, cnum1;
    static float bignum, smlnum, mul;


#define SX(I) sx[(I)-1]


    if (n__ <= 0) {
	return ;
    }

    /*
     *     Get machine parameters 
     */

    smlnum = slamch("S");
    bignum = 1.f / smlnum;
    slabad(&smlnum, &bignum);

    /*
     *     Initialize the denominator to SA and the numerator to 1. 
     */

    cden = sa_;
    cnum = 1.f;

L10:
    cden1 = cden * smlnum;
    cnum1 = cnum / bignum;
    if (fabs(cden1) > fabs(cnum) && cnum != 0.f) {

        /*
         *    Pre-multiply X by SMLNUM if CDEN is large compared to CNUM. 
         */

	mul = smlnum;
	done = FALSE;
	cden = cden1;
    } else if (fabs(cnum1) > fabs(cden)) {

        /* 
         * Pre-multiply X by BIGNUM if CDEN is small compared to CNUM. 
         */

	mul = bignum;
	done = FALSE;
	cnum = cnum1;
    } else {

        /* 
         *  Multiply X by CNUM / CDEN and return. 
         */

	mul = cnum / cden;
	done = TRUE;
    }

    /*     
     * Scale the vector X by MUL 
     */

    csscal(n__, mul, &SX(1), incx);

    if (! done) {
	goto L10;
    }

} 

