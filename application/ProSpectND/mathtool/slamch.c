


#include <math.h>
#include "complex.h"
#include "mathtool.h"


void slamc1(int *beta, int *t, int *rnd, int 
	    *ieee1);
void slamc2(int *beta, int *t, int *rnd, float *
	    eps, int *emin, float *rmin, int *emax, float *rmax);
double slamc3(float *a, float *b);
void slamc4(int *emin, float start, int base);
void slamc5(int beta, int p__, int emin, 
	    int ieee, int *emax, float *rmax);


double slamch(char *cmach)
{
/*  -- LAPACK auxiliary routine (version 2.0) --   
       Univ. of Tennessee, Univ. of California Berkeley, NAG Ltd.,   
       Courant Institute, Argonne National Lab, and Rice University   
       October 31, 1992   


    Purpose   
    =======   

    SLAMCH determines single precision machine parameters.   

    Arguments   
    =========   

    CMACH   (input) CHARACTER*1   
            Specifies the value to be returned by SLAMCH:   
            = 'E' or 'e',   SLAMCH := eps   
            = 'S' or 's ,   SLAMCH := sfmin   
            = 'B' or 'b',   SLAMCH := base   
            = 'P' or 'p',   SLAMCH := eps*base   
            = 'N' or 'n',   SLAMCH := t   
            = 'R' or 'r',   SLAMCH := rnd   
            = 'M' or 'm',   SLAMCH := emin   
            = 'U' or 'u',   SLAMCH := rmin   
            = 'L' or 'l',   SLAMCH := emax   
            = 'O' or 'o',   SLAMCH := rmax   

            where   

            eps   = relative machine precision   
            sfmin = safe minimum, such that 1/sfmin does not overflow   
            base  = base of the machine   
            prec  = eps*base   
            t     = number of (base) digits in the mantissa   
            rnd   = 1.0 when rounding occurs in addition, 0.0 otherwise   
            emin  = minimum exponent before (gradual) underflow   
            rmin  = underflow threshold - base**(emin-1)   
            emax  = largest exponent before overflow   
            rmax  = overflow threshold  - (base**emax)*(1-eps)   

   ===================================================================== 
*/
/* >>Start of File<<   
       Initialized data */
    static int first = TRUE;
    /* System generated locals */
    int i__1;
    float ret_val;
    /* Local variables */
    static float base;
    static int beta;
    static float emin, prec, emax;
    static int imin, imax;
    static int lrnd;
    static float rmin, rmax, t, rmach;
    extern int lsame_(char *, char *);
    static float small, sfmin;
    static int it;
    static float rnd, eps;



    if (first) {
	first = FALSE;
	slamc2(&beta, &it, &lrnd, &eps, &imin, &rmin, &imax, &rmax);
	base = (float) beta;
	t = (float) it;
	if (lrnd) {
	    rnd = 1.f;
	    i__1 = 1 - it;
	    eps = pow(base, i__1) / 2;
	} else {
	    rnd = 0.f;
	    i__1 = 1 - it;
	    eps = pow(base, i__1);
	}
	prec = eps * base;
	emin = (float) imin;
	emax = (float) imax;
	sfmin = rmin;
	small = 1.f / rmax;
	if (small >= sfmin) {

            /*
             *   Use SMALL plus a bit, to avoid the possibility of rounding 
             *   causing overflow when computing  1/sfmin. 
             */

	    sfmin = small * (eps + 1.f);
	}
    }

    if (lsame(cmach, "E")) {
	rmach = eps;
    } else if (lsame(cmach, "S")) {
	rmach = sfmin;
    } else if (lsame(cmach, "B")) {
	rmach = base;
    } else if (lsame(cmach, "P")) {
	rmach = prec;
    } else if (lsame(cmach, "N")) {
	rmach = t;
    } else if (lsame(cmach, "R")) {
	rmach = rnd;
    } else if (lsame(cmach, "M")) {
	rmach = emin;
    } else if (lsame(cmach, "U")) {
	rmach = rmin;
    } else if (lsame(cmach, "L")) {
	rmach = emax;
    } else if (lsame(cmach, "O")) {
	rmach = rmax;
    }

    ret_val = rmach;
    return ret_val;



} 



void slamc1(int *beta, int *t, int *rnd, int 
	    *ieee1)
{
/*  -- LAPACK auxiliary routine (version 2.0) --   
       Univ. of Tennessee, Univ. of California Berkeley, NAG Ltd.,   
       Courant Institute, Argonne National Lab, and Rice University   
       October 31, 1992   


    Purpose   
    =======   

    SLAMC1 determines the machine parameters given by BETA, T, RND, and   
    IEEE1.   

    Arguments   
    =========   

    BETA    (output) INTEGER   
            The base of the machine.   

    T       (output) INTEGER   
            The number of ( BETA ) digits in the mantissa.   

    RND     (output) LOGICAL   
            Specifies whether proper rounding  ( RND = .TRUE. )  or   
            chopping  ( RND = .FALSE. )  occurs in addition. This may not 
  
            be a reliable guide to the way in which the machine performs 
  
            its arithmetic.   

    IEEE1   (output) LOGICAL   
            Specifies whether rounding appears to be done in the IEEE   
            'round to nearest' style.   

    Further Details   
    ===============   

    The routine is based on the routine  ENVRON  by Malcolm and   
    incorporates suggestions by Gentleman and Marovich. See   

       Malcolm M. A. (1972) Algorithms to reveal properties of   
          floating-point arithmetic. Comms. of the ACM, 15, 949-951.   

       Gentleman W. M. and Marovich S. B. (1974) More on algorithms   
          that reveal properties of floating point arithmetic units.   
          Comms. of the ACM, 17, 276-277.   

   ===================================================================== 
*/
    /* Initialized data */
    static int first = TRUE;
    /* System generated locals */
    float r__1, r__2;
    /* Local variables */
    static int lrnd;
    static float a, b, c, f;
    static int lbeta;
    static float savec;
    static int lieee1;
    static float t1, t2;
    static int lt;
    static float one, qtr;



    if (first) {
	first = FALSE;
	one = 1.f;

/*        LBETA,  LIEEE1,  LT and  LRND  are the  local values  of  BETA,   
          IEEE1, T and RND.   

          Throughout this routine  we use the function  SLAMC3  to ensure   
          that relevant values are  stored and not held in registers,  or   
          are not affected by optimizers.   

          Compute  a = 2.0**m  with the  smallest positive int m such   
          that   

             fl( a + 1.0 ) = a. */

	a = 1.f;
	c = 1.f;

L10:
	if (c == one) {
	    a *= 2;
	    c = slamc3(&a, &one);
	    r__1 = -(double)a;
	    c = slamc3(&c, &r__1);
	    goto L10;
	}
/* 

          Now compute  b = 2.0**m  with the smallest positive int m   
          such that   

             fl( a + b ) .gt. a. */

	b = 1.f;
	c = slamc3(&a, &b);


L20:
	if (c == a) {
	    b *= 2;
	    c = slamc3(&a, &b);
	    goto L20;
	}
/* 

          Now compute the base.  a and c  are neighbouring floating point   
          numbers  in the  interval  ( beta**t, beta**( t + 1 ) )  and so   
          their difference is beta. Adding 0.25 to c is to ensure that it   
          is truncated to beta and not ( beta - 1 ). */

	qtr = one / 4;
	savec = c;
	r__1 = -(double)a;
	c = slamc3(&c, &r__1);
	lbeta = c + qtr;

/*        Now determine whether rounding or chopping occurs,  by adding a   
          bit  less  than  beta/2  and a  bit  more  than  beta/2  to  a. */

	b = (float) lbeta;
	r__1 = b / 2;
	r__2 = -(double)b / 100;
	f = slamc3(&r__1, &r__2);
	c = slamc3(&f, &a);
	if (c == a) {
	    lrnd = TRUE;
	} else {
	    lrnd = FALSE;
	}
	r__1 = b / 2;
	r__2 = b / 100;
	f = slamc3(&r__1, &r__2);
	c = slamc3(&f, &a);
	if (lrnd && c == a) {
	    lrnd = FALSE;
	}

/*        Try and decide whether rounding is done in the  IEEE  'round to   
          nearest' style. B/2 is half a unit in the last place of the two   
          numbers A and SAVEC. Furthermore, A is even, i.e. has last  bit   
          zero, and SAVEC is odd. Thus adding B/2 to A should not  change   
          A, but adding B/2 to SAVEC should change SAVEC. */

	r__1 = b / 2;
	t1 = slamc3(&r__1, &a);
	r__1 = b / 2;
	t2 = slamc3(&r__1, &savec);
	lieee1 = t1 == a && t2 > savec && lrnd;

/*        Now find  the  mantissa, t.  It should  be the  integer part of   
          log to the base beta of a,  however it is safer to determine  t   
          by powering.  So we find t as the smallest positive integer for   
          which   

             fl( beta**t + 1.0 ) = 1.0. */

	lt = 0;
	a = 1.f;
	c = 1.f;

L30:
	if (c == one) {
	    ++lt;
	    a *= lbeta;
	    c = slamc3(&a, &one);
	    r__1 = -(double)a;
	    c = slamc3(&c, &r__1);
	    goto L30;
	}

    }

    *beta = lbeta;
    *t = lt;
    *rnd = lrnd;
    *ieee1 = lieee1;


} 



void slamc2(int *beta, int *t, int *rnd, float *
	    eps, int *emin, float *rmin, int *emax, float *rmax)
{
/*  -- LAPACK auxiliary routine (version 2.0) --   
       Univ. of Tennessee, Univ. of California Berkeley, NAG Ltd.,   
       Courant Institute, Argonne National Lab, and Rice University   
       October 31, 1992   


    Purpose   
    =======   

    SLAMC2 determines the machine parameters specified in its argument   
    list.   

    Arguments   
    =========   

    BETA    (output) INTEGER   
            The base of the machine.   

    T       (output) INTEGER   
            The number of ( BETA ) digits in the mantissa.   

    RND     (output) LOGICAL   
            Specifies whether proper rounding  ( RND = .TRUE. )  or   
            chopping  ( RND = .FALSE. )  occurs in addition. This may not 
  
            be a reliable guide to the way in which the machine performs 
  
            its arithmetic.   

    EPS     (output) REAL   
            The smallest positive number such that   

               fl( 1.0 - EPS ) .LT. 1.0,   

            where fl denotes the computed value.   

    EMIN    (output) INTEGER   
            The minimum exponent before (gradual) underflow occurs.   

    RMIN    (output) REAL   
            The smallest normalized number for the machine, given by   
            BASE**( EMIN - 1 ), where  BASE  is the floating point value 
  
            of BETA.   

    EMAX    (output) INTEGER   
            The maximum exponent before overflow occurs.   

    RMAX    (output) REAL   
            The largest positive number for the machine, given by   
            BASE**EMAX * ( 1 - EPS ), where  BASE  is the floating point 
  
            value of BETA.   

    Further Details   
    ===============   

    The computation of  EPS  is based on a routine PARANOIA by   
    W. Kahan of the University of California at Berkeley.   

   ===================================================================== 
*/
    
    /* Initialized data */
    static int first = TRUE;
    static int iwarn = FALSE;
    /* System generated locals */
    int i__1;
    float r__1, r__2, r__3, r__4, r__5;
    /* Local variables */
    static int ieee;
    static float half;
    static int lrnd;
    static float leps, zero, a, b, c;
    static int i, lbeta;
    static float rbase;
    static int lemin, lemax, gnmin;
    static float small;
    static int gpmin;
    static float third, lrmin, lrmax, sixth;
    static int lieee1;
    static int lt, ngnmin, ngpmin;
    static float one, two;



    if (first) {
	first = FALSE;
	zero = 0.f;
	one = 1.f;
	two = 2.f;

/*        LBETA, LT, LRND, LEPS, LEMIN and LRMIN  are the local values of   
          BETA, T, RND, EPS, EMIN and RMIN.   

          Throughout this routine  we use the function  SLAMC3  to ensure   
          that relevant values are stored  and not held in registers,  or   
          are not affected by optimizers.   

          SLAMC1 returns the parameters  LBETA, LT, LRND and LIEEE1. 
*/

	slamc1(&lbeta, &lt, &lrnd, &lieee1);

        /*
         *        Start to find EPS. 
         */

	b = (float) lbeta;
	i__1 = -lt;
	a = pow(b, i__1);
	leps = a;

        /*
         *    Try some tricks to see whether or not this is the correct  EPS. 
         */

	b = two / 3;
	half = one / 2;
	r__1 = -(double)half;
	sixth = slamc3(&b, &r__1);
	third = slamc3(&sixth, &sixth);
	r__1 = -(double)half;
	b = slamc3(&third, &r__1);
	b = slamc3(&b, &sixth);
	b = fabs(b);
	if (b < leps) {
	    b = leps;
	}

	leps = 1.f;

L10:
	if (leps > b && b > zero) {
	    leps = b;
	    r__1 = half * leps;
            /*
             * Computing 5th power 
             */
	    r__3 = two, r__4 = r__3, r__3 *= r__3;
            /*
             * Computing 2nd power 
             */
	    r__5 = leps;
	    r__2 = r__4 * (r__3 * r__3) * (r__5 * r__5);
	    c = slamc3(&r__1, &r__2);
	    r__1 = -(double)c;
	    c = slamc3(&half, &r__1);
	    b = slamc3(&half, &c);
	    r__1 = -(double)b;
	    c = slamc3(&half, &r__1);
	    b = slamc3(&half, &c);
	    goto L10;
	}

	if (a < leps) {
	    leps = a;
	}

/*        Computation of EPS complete.   

          Now find  EMIN.  Let A = + or - 1, and + or - (1 + BASE**(-3)).   
          Keep dividing  A by BETA until (gradual) underflow occurs. This   
          is detected when we cannot recover the previous A. */

	rbase = one / lbeta;
	small = one;
	for (i = 1; i <= 3; ++i) {
	    r__1 = small * rbase;
	    small = slamc3(&r__1, &zero);
	}
	a = slamc3(&one, &small);
	slamc4(&ngpmin, one, lbeta);
	r__1 = -(double)one;
	slamc4(&ngnmin, r__1, lbeta);
	slamc4(&gpmin, a, lbeta);
	r__1 = -(double)a;
	slamc4(&gnmin, r__1, lbeta);
	ieee = FALSE;

	if (ngpmin == ngnmin && gpmin == gnmin) {
	    if (ngpmin == gpmin) {
		lemin = ngpmin;
/*            ( Non twos-complement machines, no gradual underflow;   
                e.g.,  VAX ) */
	    } else if (gpmin - ngpmin == 3) {
		lemin = ngpmin - 1 + lt;
		ieee = TRUE;
/*            ( Non twos-complement machines, with gradual underflow;   
                e.g., IEEE standard followers ) */
	    } else {
		lemin = min(ngpmin,gpmin);
/*            ( A guess; no known machine ) */
		iwarn = TRUE;
	    }

	} else if (ngpmin == gpmin && ngnmin == gnmin) {
	    if ((i__1 = ngpmin - ngnmin, abs(i__1)) == 1) {
		lemin = max(ngpmin,ngnmin);
/*            ( Twos-complement machines, no gradual underflow;   
                e.g., CYBER 205 ) */
	    } else {
		lemin = min(ngpmin,ngnmin);
/*            ( A guess; no known machine ) */
		iwarn = TRUE;
	    }

	} else if ((i__1 = ngpmin - ngnmin, abs(i__1)) == 1 && gpmin == gnmin)
		 {
	    if (gpmin - min(ngpmin,ngnmin) == 3) {
		lemin = max(ngpmin,ngnmin) - 1 + lt;
/*            ( Twos-complement machines with gradual underflow;   
                no known machine ) */
	    } else {
		lemin = min(ngpmin,ngnmin);
/*            ( A guess; no known machine ) */
		iwarn = TRUE;
	    }

	} else {
            /*
             * Computing MIN 
            */
	    i__1 = min(ngpmin,ngnmin), i__1 = min(i__1,gpmin);
	    lemin = min(i__1,gnmin);
            /*
             *         ( A guess; no known machine ) 
             */
	    iwarn = TRUE;
	}
/* **   
   Comment out this if block if EMIN is ok
	if (iwarn) {
	    first = TRUE;
	    printf("\n\n WARNING. The value EMIN may be incorrect:- ");
	    printf("EMIN = %8i\n",lemin);
	    printf("If, after inspection, the value EMIN looks acceptable");
            printf("please comment out \n the IF block as marked within the"); 
            printf("code of routine SLAMC2, \n otherwise supply EMIN"); 
            printf("explicitly.\n");
	}
* **/

/*
          Assume IEEE arithmetic if we found denormalised  numbers above,   
          or if arithmetic seems to round in the  IEEE style,  determined   
          in routine SLAMC1. A true IEEE machine should have both  things   
          true; however, faulty machines may have one or the other. */

	ieee = ieee || lieee1;

/*        Compute  RMIN by successive division by  BETA. We could compute   
          RMIN as BASE**( EMIN - 1 ),  but some machines underflow during   
          this computation. */

	lrmin = 1.f;
	i__1 = 1 - lemin;
	for (i = 1; i <= 1-lemin; ++i) {
	    r__1 = lrmin * rbase;
	    lrmin = slamc3(&r__1, &zero);
	}

        /*
         *        Finally, call SLAMC5 to compute EMAX and RMAX. 
         */

	slamc5(lbeta, lt, lemin, ieee, &lemax, &lrmax);
    }

    *beta = lbeta;
    *t = lt;
    *rnd = lrnd;
    *eps = leps;
    *emin = lemin;
    *rmin = lrmin;
    *emax = lemax;
    *rmax = lrmax;


} 



double slamc3(float *a, float *b)
{
/*  -- LAPACK auxiliary routine (version 2.0) --   
       Univ. of Tennessee, Univ. of California Berkeley, NAG Ltd.,   
       Courant Institute, Argonne National Lab, and Rice University   
       October 31, 1992   


    Purpose   
    =======   

    SLAMC3  is intended to force  A  and  B  to be stored prior to doing 
  
    the addition of  A  and  B ,  for use in situations where optimizers 
  
    might hold one of these in a register.   

    Arguments   
    =========   

    A, B    (input) REAL   
            The values A and B.   

   ===================================================================== 
*/
/* >>Start of File<<   
       System generated locals */
    float ret_val;



    ret_val = *a + *b;

    return ret_val;



} 



void slamc4(int *emin, float start, int base)
{
/*  -- LAPACK auxiliary routine (version 2.0) --   
       Univ. of Tennessee, Univ. of California Berkeley, NAG Ltd.,   
       Courant Institute, Argonne National Lab, and Rice University   
       October 31, 1992   


    Purpose   
    =======   

    SLAMC4 is a service routine for SLAMC2.   

    Arguments   
    =========   

    EMIN    (output) EMIN   
            The minimum exponent before (gradual) underflow, computed by 
  
            setting A = START and dividing by BASE until the previous A   
            can not be recovered.   

    START   (input) REAL   
            The starting point for determining EMIN.   

    BASE    (input) INTEGER   
            The base of the machine.   

   ===================================================================== 
*/
    /* System generated locals */
    int i__1;
    float r__1;
    /* Local variables */
    static float zero, a;
    static int i;
    static float rbase, b1, b2, c1, c2, d1, d2;
    static float one;



    a = start;
    one = 1.f;
    rbase = one / base;
    zero = 0.f;
    *emin = 1;
    r__1 = a * rbase;
    b1 = slamc3(&r__1, &zero);
    c1 = a;
    c2 = a;
    d1 = a;
    d2 = a;
L10:
    if (c1 == a && c2 == a && d1 == a && d2 == a) {
	--(*emin);
	a = b1;
	r__1 = a / base;
	b1 = slamc3(&r__1, &zero);
	r__1 = b1 * base;
	c1 = slamc3(&r__1, &zero);
	d1 = zero;
	i__1 = base;
	for (i = 1; i <= base; ++i) {
	    d1 += b1;
	}
	r__1 = a * rbase;
	b2 = slamc3(&r__1, &zero);
	r__1 = b2 / rbase;
	c2 = slamc3(&r__1, &zero);
	d2 = zero;
	i__1 = base;
	for (i = 1; i <= base; ++i) {
	    d2 += b2;
	}
	goto L10;
    }


}



void slamc5(int beta, int p__, int emin, 
	    int ieee, int *emax, float *rmax)
{
/*  -- LAPACK auxiliary routine (version 2.0) --   
       Univ. of Tennessee, Univ. of California Berkeley, NAG Ltd.,   
       Courant Institute, Argonne National Lab, and Rice University   
       October 31, 1992   


    Purpose   
    =======   

    SLAMC5 attempts to compute RMAX, the largest machine floating-point   
    number, without overflow.  It assumes that EMAX + abs(EMIN) sum   
    approximately to a power of 2.  It will fail on machines where this   
    assumption does not hold, for example, the Cyber 205 (EMIN = -28625, 
  
    EMAX = 28718).  It will also fail if the value supplied for EMIN is   
    too large (i.e. too close to zero), probably with overflow.   

    Arguments   
    =========   

    BETA    (input) INTEGER   
            The base of floating-point arithmetic.   

    P       (input) INTEGER   
            The number of base BETA digits in the mantissa of a   
            floating-point value.   

    EMIN    (input) INTEGER   
            The minimum exponent before (gradual) underflow.   

    IEEE    (input) LOGICAL   
            A logical flag specifying whether or not the arithmetic   
            system is thought to comply with the IEEE standard.   

    EMAX    (output) INTEGER   
            The largest exponent before overflow   

    RMAX    (output) REAL   
            The largest machine floating-point number.   

   ===================================================================== 
  


       First compute LEXP and UEXP, two powers of 2 that bound   
       abs(EMIN). We then assume that EMAX + abs(EMIN) will sum   
       approximately to the bound that is closest to abs(EMIN).   
       (EMAX is the exponent of the required number RMAX). */
    /* Table of constant values */
    static float c_b5 = 0.f;
    
    /* System generated locals */
    int i__1;
    float r__1;
    /* Local variables */
    static int lexp;
    static float oldy;
    static int uexp, i;
    static float y, z;
    static int nbits;
    static float recbas;
    static int exbits, expsum, try__;



    lexp = 1;
    exbits = 1;
L10:
    try__ = lexp << 1;
    if (try__ <= -(emin)) {
	lexp = try__;
	++exbits;
	goto L10;
    }
    if (lexp == -(emin)) {
	uexp = lexp;
    } else {
	uexp = try__;
	++exbits;
    }

    /*
     *     Now -LEXP is less than or equal to EMIN, and -UEXP is greater 
     *     than or equal to EMIN. EXBITS is the number of bits needed to   
     *     store the exponent. 
     */

    if (uexp + emin > -lexp - emin) {
	expsum = lexp << 1;
    } else {
	expsum = uexp << 1;
    }

    /*
     *     EXPSUM is the exponent range, approximately equal to 
     *     EMAX - EMIN + 1 . 
     */

    *emax = expsum + emin - 1;
    nbits = exbits + 1 + p__;

    /*
     *     NBITS is the total number of bits needed to store a 
     *     floating-point number. 
     */

    if (nbits % 2 == 1 && beta == 2) {

    /*    Either there are an odd number of bits used to store a   
     *    floating-point number, which is unlikely, or some bits are 
     *    not used in the representation of numbers, which is possible,   
     *    (e.g. Cray machines) or the mantissa has an implicit bit,   
     *    (e.g. IEEE machines, Dec Vax machines), which is perhaps the
     *    most likely. We have to assume the last alternative.   
     *    If this is true, then we need to reduce EMAX by one because 
     *    there must be some way of representing zero in an implicit-bit   
     *    system. On machines like Cray, we are reducing EMAX by one 
     *    unnecessarily. 
     */

	--(*emax);
    }

    if (ieee) {

    /*
     *        Assume we are on an IEEE machine which reserves one exponent
     *       for infinity and NaN. 
     */

	--(*emax);
    }

    /* Now create RMAX, the largest machine number, which should   
     * be equal to (1.0 - BETA**(-P)) * BETA**EMAX .   
     *
     * First compute 1.0 - BETA**(-P), being careful that the   
     * result is less than 1.0 . 
     */

    recbas = 1.f / beta;
    z = beta - 1.f;
    y = 0.f;
    i__1 = p__;
    for (i = 1; i <= p__; ++i) {
	z *= recbas;
	if (y < 1.f) {
	    oldy = y;
	}
	y = slamc3(&y, &z);
    }
    if (y >= 1.f) {
	y = oldy;
    }

    /*
     *     Now multiply by BETA**EMAX to get RMAX. 
     */

    i__1 = *emax;
    for (i = 1; i <= *emax; ++i) {
	r__1 = y * beta;
	y = slamc3(&r__1, &c_b5);
    }

    *rmax = y;

}

