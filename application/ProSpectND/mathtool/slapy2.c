
#include <math.h>
#include "complex.h"
#include "mathtool.h"


double slapy2(float x, float y)
{
/*  -- LAPACK auxiliary routine (version 2.0) --   
       Univ. of Tennessee, Univ. of California Berkeley, NAG Ltd.,   
       Courant Institute, Argonne National Lab, and Rice University   
       October 31, 1992   


    Purpose   
    =======   

    SLAPY2 returns sqrt(x**2+y**2), taking care not to cause unnecessary 
  
    overflow.   

    Arguments   
    =========   

    X       (input) REAL   
    Y       (input) REAL   
            X and Y specify the values x and y.   

    ===================================================================== 
*/
/* >>Start of File<<   
       System generated locals */
    float ret_val, r__1;

    /* Local variables */
    static float xabs, yabs, w, z;



    xabs = fabs(x);
    yabs = fabs(y);
    w = max(xabs,yabs);
    z = min(xabs,yabs);
    if (z == 0.f) {
	ret_val = w;
    } else {
/* Computing 2nd power */
	r__1 = z / w;
	ret_val = w * sqrt(r__1 * r__1 + 1.f);
    }
    return ret_val;

} 

