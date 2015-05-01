
#include <math.h>
#include "complex.h"
#include "mathtool.h"



double slapy3(float x, float y, float z)
{
/*  -- LAPACK auxiliary routine (version 2.0) --   
       Univ. of Tennessee, Univ. of California Berkeley, NAG Ltd.,   
       Courant Institute, Argonne National Lab, and Rice University   
       October 31, 1992   


    Purpose   
    =======   

    SLAPY3 returns sqrt(x**2+y**2+z**2), taking care not to cause   
    unnecessary overflow.   

    Arguments   
    =========   

    X       (input) REAL   
    Y       (input) REAL   
    Z       (input) REAL   
            X, Y and Z specify the values x, y and z.   

    ===================================================================== 
*/
/* >>Start of File<<   
       System generated locals */
    float ret_val, r__1, r__2, r__3;
    /* Local variables */
    static float xabs, yabs, zabs, w;



    xabs = fabs(x);
    yabs = fabs(y);
    zabs = fabs(z);
    /* 
     *   Computing MAX 
     */
    r__1 = max(xabs,yabs);
    w = max(r__1,zabs);
    if (w == 0.f) {
	ret_val = 0.f;
    } else {
        /* 
         *  Computing 2nd power 
         */
	r__1 = xabs / w;
        /* 
         *  Computing 2nd power 
         */
	r__2 = yabs / w;
        /* 
         *   Computing 2nd power 
         */
	r__3 = zabs / w;
	ret_val = w * sqrt(r__1 * r__1 + r__2 * r__2 + r__3 * r__3);
    }
    return ret_val;

} 

