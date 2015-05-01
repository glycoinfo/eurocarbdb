
#include "complex.h"
#include "mathtool.h"


void cladiv(fcomplex *ret_val, fcomplex x, fcomplex y)
{
/*  -- LAPACK auxiliary routine (version 2.0) --   
       Univ. of Tennessee, Univ. of California Berkeley, NAG Ltd.,   
       Courant Institute, Argonne National Lab, and Rice University   
       October 31, 1992   


    Purpose   
    =======   

    CLADIV := X / Y, where X and Y are complex.  The computation of X / Y 
  
    will not overflow on an intermediary step unless the results   
    overflows.   

    Arguments   
    =========   

    X       (input) COMPLEX   
    Y       (input) COMPLEX   
            The complex scalars X and Y.   

    ===================================================================== 
*/
    /* System generated locals */
    float r__1, r__2, r__3, r__4;
    fcomplex q__1;
    /* Local variables */
    static float zi, zr;



    r__1 = x.r;
    r__2 = x.i;
    r__3 = y.r;
    r__4 = y.i;
    sladiv(r__1, r__2, r__3, r__4, &zr, &zi);
    q__1.r = zr, q__1.i = zi;
    ret_val->r = q__1.r,  ret_val->i = q__1.i;


} 

