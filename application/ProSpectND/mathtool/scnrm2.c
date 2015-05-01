
/*  -- translated by f2c (version 19940927).
   You must link the resulting object file with the libraries:
	-lf2c -lm   (in that order)
*/


#include <math.h>
#include "complex.h"
#include "mathtool.h"



double scnrm2(int n__, fcomplex *x, int incx)
{
    /* System generated locals */
    int i__1, i__2, i__3;
    double ret_val, r__1;
    /* Local variables */
    static double temp, norm, scale;
    static int ix;
    static double ssq;


/*  SCNRM2 returns the euclidean norm of a vector via the function   
    name, so that   

       SCNRM2 := sqrt( conjg( x' )*x )   



    -- This version written on 25-October-1982.   
       Modified on 14-October-1993 to inline the call to CLASSQ.   
       Sven Hammarling, Nag Ltd.   


    
   Parameter adjustments   
       Function Body */
#define X(I) x[(I)-1]


    if (n__ < 1 || incx < 1) {
	norm = 0.f;
    } else {
	scale = 0.f;
	ssq = 1.f;
        /*
         *  The following loop is equivalent to this call to the LAPACK 
         *
         *  auxiliary routine:   
         *  CALL CLASSQ( N, X, INCX, SCALE, SSQ ) 
         */

	i__1 = (n__ - 1) * incx + 1;
	i__2 = incx;
	for (ix = 1; incx < 0 ? ix >= (n__-1)*incx+1 : ix <= (n__-1)*incx+1; ix += incx) {
	    i__3 = ix;
	    if (X(ix).r != 0.f) {
		i__3 = ix;
		temp = fabs(X(ix).r);
		if (scale < temp) {
                    /*
                     * Computing 2nd power 
                     */
		    r__1 = scale / temp;
		    ssq = ssq * (r__1 * r__1) + 1.f;
		    scale = temp;
		} else {
                    /*
                     * Computing 2nd power 
                     */
		    r__1 = temp / scale;
		    ssq += r__1 * r__1;
		}
	    }
	    if (X(ix).i != 0.f) {
		temp = fabs(X(ix).i);
		if (scale < temp) {
                    /* 
                     *Computing 2nd power 
                     */
		    r__1 = scale / temp;
		    ssq = ssq * (r__1 * r__1) + 1.f;
		    scale = temp;
		} else {
                    /*
                     * Computing 2nd power 
                     */
		    r__1 = temp / scale;
		    ssq += r__1 * r__1;
		}
	    }
	}
	norm = scale * sqrt(ssq);
    }

    ret_val = norm;
    return ret_val;

} 

