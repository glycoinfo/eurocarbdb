

#include "complex.h"
#include "mathtool.h"



void clacgv(int n__, fcomplex *x, int incx)
{
/*  -- LAPACK auxiliary routine (version 2.0) --   
       Univ. of Tennessee, Univ. of California Berkeley, NAG Ltd.,   
       Courant Institute, Argonne National Lab, and Rice University   
       October 31, 1992   


    Purpose   
    =======   

    CLACGV conjugates a complex vector of length N.   

    Arguments   
    =========   

    N       (input) INTEGER   
            The length of the vector X.  N >= 0.   

    X       (input/output) COMPLEX array, dimension   
                           (1+(N-1)*abs(INCX))   
            On entry, the vector of length N to be conjugated.   
            On exit, X is overwritten with conjg(X).   

    INCX    (input) INTEGER   
            The spacing between successive elements of X.   

   ===================================================================== 
  


    
   Parameter adjustments   
       Function Body */
    /* System generated locals */
    int i__1, i__2;
    fcomplex q__1;
    /* Local variables */
    static int ioff, i;


#define X(I) x[(I)-1]


    if (incx == 1) {
	i__1 = n__;
	for (i = 1; i <= n__; ++i) {
	    i__2 = i;
	    r_cnjg(&q__1, &X(i));
	    X(i).r = q__1.r, X(i).i = q__1.i;
	}
    } else {
	ioff = 1;
	if (incx < 0) {
	    ioff = 1 - (n__ - 1) * incx;
	}
	i__1 = n__;
	for (i = 1; i <= n__; ++i) {
	    i__2 = ioff;
	    r_cnjg(&q__1, &X(ioff));
	    X(ioff).r = q__1.r, X(ioff).i = q__1.i;
	    ioff += incx;
	}
    }

} 

