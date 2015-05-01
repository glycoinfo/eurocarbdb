
#include <math.h>
#include "complex.h"
#include "mathtool.h"


void sladiv(float a, float b, float c, float d, float *p, float *q)
{
/*  -- LAPACK auxiliary routine (version 2.0) --   
       Univ. of Tennessee, Univ. of California Berkeley, NAG Ltd.,   
       Courant Institute, Argonne National Lab, and Rice University   
       October 31, 1992   


    Purpose   
    =======   

    SLADIV performs complex division in  real arithmetic   

                          a + i*b   
               p + i*q = ---------   
                          c + i*d   

    The algorithm is due to Robert L. Smith and can be found   
    in D. Knuth, The art of Computer Programming, Vol.2, p.195   

    Arguments   
    =========   

    A       (input) REAL   
    B       (input) REAL   
    C       (input) REAL   
    D       (input) REAL   
            The scalars a, b, c, and d in the above expression.   

    P       (output) REAL   
    Q       (output) REAL   
            The scalars p and q in the above expression.   

    ===================================================================== 
*/
    static float e, f;

    if (fabs(d) < fabs(c)) {
	e = d / c;
	f = c + d * e;
	*p = (a + b * e) / f;
	*q = (b - a * e) / f;
    } else {
	e = c / d;
	f = d + c * e;
	*p = (b + a * e) / f;
	*q = (-(double)(a) + b * e) / f;
    }


} 

