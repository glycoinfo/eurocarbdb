
#include <math.h>
#include "complex.h"
#include "mathtool.h"



void clarfg(int n__, fcomplex *alpha, fcomplex *x, int 
	    incx, fcomplex *tau)
{
/*  -- LAPACK auxiliary routine (version 2.0) --   
       Univ. of Tennessee, Univ. of California Berkeley, NAG Ltd.,   
       Courant Institute, Argonne National Lab, and Rice University   
       September 30, 1994   


    Purpose   
    =======   

    CLARFG generates a complex elementary reflector H of order n, such   
    that   

          H' * ( alpha ) = ( beta ),   H' * H = I.   
               (   x   )   (   0  )   

    where alpha and beta are scalars, with beta real, and x is an   
    (n-1)-element complex vector. H is represented in the form   

          H = I - tau * ( 1 ) * ( 1 v' ) ,   
                        ( v )   

    where tau is a complex scalar and v is a complex (n-1)-element   
    vector. Note that H is not hermitian.   

    If the elements of x are all zero and alpha is real, then tau = 0   
    and H is taken to be the unit matrix.   

    Otherwise  1 <= real(tau) <= 2  and  abs(tau-1) <= 1 .   

    Arguments   
    =========   

    N       (input) INTEGER   
            The order of the elementary reflector.   

    ALPHA   (input/output) COMPLEX   
            On entry, the value alpha.   
            On exit, it is overwritten with the value beta.   

    X       (input/output) COMPLEX array, dimension   
                           (1+(N-2)*abs(INCX))   
            On entry, the vector x.   
            On exit, it is overwritten with the vector v.   

    INCX    (input) INTEGER   
            The increment between elements of X. INCX > 0.   

    TAU     (output) COMPLEX   
            The value tau.   

    ===================================================================== 
  


    
   Parameter adjustments   
       Function Body */
    /* Table of constant values */
    static fcomplex c_b5 = {1.f,0.f};
    
    /* System generated locals */
    int i__1;
    float r__1;
    double d__1, d__2;
    fcomplex q__1, q__2;
    /* Local variables */
    static float beta;
    static int j;
    static float alphi, alphr, xnorm;
    static float safmin, rsafmn;
    static int knt;



#define X(I) x[(I)-1]


    if (n__ <= 0) {
	tau->r = 0.f, tau->i = 0.f;
	return ;
    }

    i__1 = n__ - 1;
    xnorm = scnrm2(i__1, &X(1), incx);
    alphr = alpha->r;
    alphi = alpha->i;

    if (xnorm == 0.f && alphi == 0.f) {

        /*
         *        H  =  I 
         */

	tau->r = 0.f, tau->i = 0.f;
    } else {

        /*
         *        general case 
         */

	r__1 = slapy3(alphr, alphi, xnorm);
	beta = -(double)sign(r__1, alphr);
	safmin = slamch("S") / slamch("E");
	rsafmn = 1.f / safmin;

	if (fabs(beta) < safmin) {

            /* 
             *  XNORM, BETA may be inaccurate; scale X and recompute them 
             */

	    knt = 0;
L10:
	    ++knt;
	    i__1 = n__ - 1;
	    csscal(i__1, rsafmn, &X(1), incx);
	    beta *= rsafmn;
	    alphi *= rsafmn;
	    alphr *= rsafmn;
	    if (fabs(beta) < safmin) {
		goto L10;
	    }

            /*
             *  New BETA is at most 1, at least SAFMIN 
             */

	    i__1 = n__ - 1;
	    xnorm = scnrm2(i__1, &X(1), incx);
	    q__1.r = alphr, q__1.i = alphi;
	    alpha->r = q__1.r, alpha->i = q__1.i;
	    r__1 = slapy3(alphr, alphi, xnorm);
	    beta = -(double)sign(r__1, alphr);
	    d__1 = (beta - alphr) / beta;
	    d__2 = -(double)alphi / beta;
	    q__1.r = d__1, q__1.i = d__2;
	    tau->r = q__1.r, tau->i = q__1.i;
	    q__2.r = alpha->r - beta, q__2.i = alpha->i;
	    cladiv(&q__1, c_b5, q__2);
	    alpha->r = q__1.r, alpha->i = q__1.i;
	    i__1 = n__ - 1;
	    cscal(i__1, *alpha, &X(1), incx);

            /*
             *  If ALPHA is subnormal, it may lose relative accuracy 
             */

	    alpha->r = beta, alpha->i = 0.f;
	    i__1 = knt;
	    for (j = 1; j <= knt; ++j) {
		q__1.r = safmin * alpha->r, q__1.i = safmin * alpha->i;
		alpha->r = q__1.r, alpha->i = q__1.i;
	    }
	} else {
	    d__1 = (beta - alphr) / beta;
	    d__2 = -(double)alphi / beta;
	    q__1.r = d__1, q__1.i = d__2;
	    tau->r = q__1.r, tau->i = q__1.i;
	    q__2.r = alpha->r - beta, q__2.i = alpha->i;
	    cladiv(&q__1, c_b5, q__2);
	    alpha->r = q__1.r, alpha->i = q__1.i;
	    i__1 = n__ - 1;
	    cscal(i__1, *alpha, &X(1), incx);
	    alpha->r = beta, alpha->i = 0.f;
	}
    }


} 

