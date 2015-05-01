


#include <math.h>
#include "complex.h"
#include "mathtool.h"

void ctrevc(char *side, char *howmny, int *select, 
	    int n__, fcomplex *t, int ldt, fcomplex *vl, int ldvl, 
	    fcomplex *vr, int ldvr, int mm_, int *m, fcomplex *work, 
	    float *rwork, int *info)
{
/*  -- LAPACK routine (version 2.0) --   
       Univ. of Tennessee, Univ. of California Berkeley, NAG Ltd.,   
       Courant Institute, Argonne National Lab, and Rice University   
       September 30, 1994   


    Purpose   
    =======   

    CTREVC computes some or all of the right and/or left eigenvectors of 
  
    a complex upper triangular matrix T.   

    The right eigenvector x and the left eigenvector y of T corresponding 
  
    to an eigenvalue w are defined by:   

                 T*x = w*x,     y'*T = w*y'   

    where y' denotes the conjugate transpose of the vector y.   

    If all eigenvectors are requested, the routine may either return the 
  
    matrices X and/or Y of right or left eigenvectors of T, or the   
    products Q*X and/or Q*Y, where Q is an input unitary   
    matrix. If T was obtained from the Schur factorization of an   
    original matrix A = Q*T*Q', then Q*X and Q*Y are the matrices of   
    right or left eigenvectors of A.   

    Arguments   
    =========   

    SIDE    (input) CHARACTER*1   
            = 'R':  compute right eigenvectors only;   
            = 'L':  compute left eigenvectors only;   
            = 'B':  compute both right and left eigenvectors.   

    HOWMNY  (input) CHARACTER*1   
            = 'A':  compute all right and/or left eigenvectors;   
            = 'B':  compute all right and/or left eigenvectors,   
                    and backtransform them using the input matrices   
                    supplied in VR and/or VL;   
            = 'S':  compute selected right and/or left eigenvectors,   
                    specified by the logical array SELECT.   

    SELECT  (input) LOGICAL array, dimension (N)   
            If HOWMNY = 'S', SELECT specifies the eigenvectors to be   
            computed.   
            If HOWMNY = 'A' or 'B', SELECT is not referenced.   
            To select the eigenvector corresponding to the j-th   
            eigenvalue, SELECT(j) must be set to .TRUE..   

    N       (input) INTEGER   
            The order of the matrix T. N >= 0.   

    T       (input/output) COMPLEX array, dimension (LDT,N)   
            The upper triangular matrix T.  T is modified, but restored   
            on exit.   

    LDT     (input) INTEGER   
            The leading dimension of the array T. LDT >= max(1,N).   

    VL      (input/output) COMPLEX array, dimension (LDVL,MM)   
            On entry, if SIDE = 'L' or 'B' and HOWMNY = 'B', VL must   
            contain an N-by-N matrix Q (usually the unitary matrix Q of   
            Schur vectors returned by CHSEQR).   
            On exit, if SIDE = 'L' or 'B', VL contains:   
            if HOWMNY = 'A', the matrix Y of left eigenvectors of T;   
            if HOWMNY = 'B', the matrix Q*Y;   
            if HOWMNY = 'S', the left eigenvectors of T specified by   
                             SELECT, stored consecutively in the columns 
  
                             of VL, in the same order as their   
                             eigenvalues.   
            If SIDE = 'R', VL is not referenced.   

    LDVL    (input) INTEGER   
            The leading dimension of the array VL.  LDVL >= max(1,N) if   
            SIDE = 'L' or 'B'; LDVL >= 1 otherwise.   

    VR      (input/output) COMPLEX array, dimension (LDVR,MM)   
            On entry, if SIDE = 'R' or 'B' and HOWMNY = 'B', VR must   
            contain an N-by-N matrix Q (usually the unitary matrix Q of   
            Schur vectors returned by CHSEQR).   
            On exit, if SIDE = 'R' or 'B', VR contains:   
            if HOWMNY = 'A', the matrix X of right eigenvectors of T;   
            if HOWMNY = 'B', the matrix Q*X;   
            if HOWMNY = 'S', the right eigenvectors of T specified by   
                             SELECT, stored consecutively in the columns 
  
                             of VR, in the same order as their   
                             eigenvalues.   
            If SIDE = 'L', VR is not referenced.   

    LDVR    (input) INTEGER   
            The leading dimension of the array VR.  LDVR >= max(1,N) if   
             SIDE = 'R' or 'B'; LDVR >= 1 otherwise.   

    MM      (input) INTEGER   
            The number of columns in the arrays VL and/or VR. MM >= M.   

    M       (output) INTEGER   
            The number of columns in the arrays VL and/or VR actually   
            used to store the eigenvectors.  If HOWMNY = 'A' or 'B', M   
            is set to N.  Each selected eigenvector occupies one   
            column.   

    WORK    (workspace) COMPLEX array, dimension (2*N)   

    RWORK   (workspace) REAL array, dimension (N)   

    INFO    (output) INTEGER   
            = 0:  successful exit   
            < 0:  if INFO = -i, the i-th argument had an illegal value   

    Further Details   
    ===============   

    The algorithm used in this program is basically backward (forward)   
    substitution, with scaling to make the the code robust against   
    possible overflow.   

    Each eigenvector is normalized so that the element of largest   
    magnitude has magnitude 1; here the magnitude of a complex number   
    (x,y) is taken to be |x| + |y|.   

    ===================================================================== 
  


       Decode and test the input parameters   

    
   Parameter adjustments   
       Function Body */
    /* Table of constant values */
    static fcomplex c_b2 = {1.f,0.f};
    static int c__1 = 1;
    
    /* System generated locals */
    int t_dim1, vl_dim1, vr_dim1, i__1, 
	    i__2, i__3, i__4, i__5;
    float r__1, r__2, r__3;
    fcomplex q__1, q__2;

    /* Local variables */
    static int allv;
    static float unfl, ovfl, smin;
    static int over;
    static int i, j, k;
    static float scale;
    static float remax;
    static int leftv, bothv, somev;
    static int ii, ki;
    static int is;
    static int rightv;
    static float smlnum, ulp;



#define SELECT(I) select[(I)-1]
#define WORK(I) work[(I)-1]
#define RWORK(I) rwork[(I)-1]

#define T(I,J) t[(I)-1 + ((J)-1)* ( ldt)]
#define VL(I,J) vl[(I)-1 + ((J)-1)* ( ldvl)]
#define VR(I,J) vr[(I)-1 + ((J)-1)* ( ldvr)]

    bothv = lsame(side, "B");
    rightv = lsame(side, "R") || bothv;
    leftv = lsame(side, "L") || bothv;

    allv = lsame(howmny, "A");
    over = lsame(howmny, "B") || lsame(howmny, "O");
    somev = lsame(howmny, "S");

/*     Set M to the number of columns required to store the selected   
       eigenvectors. */

    if (somev) {
	*m = 0;
	i__1 = n__;
	for (j = 1; j <= n__; ++j) {
	    if (SELECT(j)) {
		++(*m);
	    }
	}
    } else {
	*m = n__;
    }

    *info = 0;
    if (! rightv && ! leftv) {
	*info = -1;
    } else if (! allv && ! over && ! somev) {
	*info = -2;
    } else if (n__ < 0) {
	*info = -4;
    } else if (ldt < max(1,n__)) {
	*info = -6;
    } else if (ldvl < 1 || (leftv && ldvl < n__)) {
	*info = -8;
    } else if (ldvr < 1 || (rightv && ldvr < n__)) {
	*info = -10;
    } else if (mm_ < *m) {
	*info = -11;
    }
    if (*info != 0) {
	i__1 = -(*info);
	return ;
    }

/*     Quick return if possible. */

    if (n__ == 0) {
	return ;
    }

/*     Set the constants to control overflow. */

    unfl = slamch("Safe minimum");
    ovfl = 1.f / unfl;
    slabad(&unfl, &ovfl);
    ulp = slamch("Precision");
    smlnum = unfl * (n__ / ulp);

/*     Store the diagonal elements of T in working array WORK. */

    i__1 = n__;
    for (i = 1; i <= n__; ++i) {
	i__2 = i + n__;
	i__3 = i + i * t_dim1;
	WORK(i+n__).r = T(i,i).r, WORK(i+n__).i = T(i,i).i;
    }

/*     Compute 1-norm of each column of strictly upper triangular   
       part of T to control overflow in triangular solver. */

    RWORK(1) = 0.f;
    i__1 = n__;
    for (j = 2; j <= n__; ++j) {
	i__2 = j - 1;
	RWORK(j) = scasum(i__2, &T(1,j), c__1);
    }

    if (rightv) {

/*        Compute right eigenvectors. */

	is = *m;
	for (ki = n__; ki >= 1; --ki) {

	    if (somev) {
		if (! SELECT(ki)) {
		    goto L80;
		}
	    }
/* Computing MAX */
	    i__1 = ki + ki * t_dim1;
	    r__3 = ulp * ((r__1 = T(ki,ki).r, fabs(r__1)) + (r__2 = T(ki,ki).i, fabs(r__2)));
	    smin = max(r__3,smlnum);

	    WORK(1).r = 1.f, WORK(1).i = 0.f;

/*           Form right-hand side. */

	    i__1 = ki - 1;
	    for (k = 1; k <= ki-1; ++k) {
		i__2 = k;
		i__3 = k + ki * t_dim1;
		q__1.r = -(double)T(k,ki).r, q__1.i = -(double)T(k,ki)
			.i;
		WORK(k).r = q__1.r, WORK(k).i = q__1.i;
	    }

/*           Solve the triangular system:   
                (T(1:KI-1,1:KI-1) - T(KI,KI))*X = SCALE*WORK. */

	    i__1 = ki - 1;
	    for (k = 1; k <= ki-1; ++k) {
		i__2 = k + k * t_dim1;
		i__3 = k + k * t_dim1;
		i__4 = ki + ki * t_dim1;
		q__1.r = T(k,k).r - T(ki,ki).r, q__1.i = T(k,k).i - T(ki,ki)
			.i;
		T(k,k).r = q__1.r, T(k,k).i = q__1.i;
		i__2 = k + k * t_dim1;
		if ((r__1 = T(k,k).r, fabs(r__1)) + (r__2 = T(k,k).i, fabs(r__2)) < smin) {
		    i__3 = k + k * t_dim1;
		    T(k,k).r = smin, T(k,k).i = 0.f;
		}
	    }

	    if (ki > 1) {
		i__1 = ki - 1;
		clatrs("Upper", "No transpose", "Non-unit", "Y", 
                         i__1, &T(1,1), ldt, &WORK(1), &scale, &RWORK(1), info);
		i__1 = ki;
		WORK(ki).r = scale, WORK(ki).i = 0.f;
	    }

/*           Copy the vector x or Q*x to VR and normalize. */

	    if (! over) {
		ccopy(ki, &WORK(1), c__1, &VR(1,is), c__1);

		ii = icamax(ki, &VR(1,is), c__1);
		i__1 = ii + is * vr_dim1;
		remax = 1.f / ((r__1 = VR(ii,is).r, fabs(r__1)) + (r__2 = 
			VR(ii,is).i, fabs(r__2)));
		csscal(ki, remax, &VR(1,is), c__1);

		i__1 = n__;
		for (k = ki + 1; k <= n__; ++k) {
		    i__2 = k + is * vr_dim1;
		    VR(k,is).r = 0.f, VR(k,is).i = 0.f;
		}
	    } else {
		if (ki > 1) {
		    i__1 = ki - 1;
		    q__1.r = scale, q__1.i = 0.f;
		    cgemv("N", n__, i__1, c_b2, &VR(1,1), ldvr, &WORK(
			    1), c__1, q__1, &VR(1,ki), c__1);
		}

		ii = icamax( n__, &VR(1,ki), c__1);
		i__1 = ii + ki * vr_dim1;
		remax = 1.f / ((r__1 = VR(ii,ki).r, fabs(r__1)) + (r__2 = 
			VR(ii,ki).i, fabs(r__2)));
		csscal( n__, remax, &VR(1,ki), c__1);
	    }

/*           Set back the original diagonal elements of T. */

	    i__1 = ki - 1;
	    for (k = 1; k <= ki-1; ++k) {
		i__2 = k + k * t_dim1;
		i__3 = k + n__;
		T(k,k).r = WORK(k+n__).r, T(k,k).i = WORK(k+n__).i;
	    }

	    --is;
L80:
	    ;
	}
    }

    if (leftv) {

/*        Compute left eigenvectors. */

	is = 1;
	i__1 = n__;
	for (ki = 1; ki <= n__; ++ki) {

	    if (somev) {
		if (! SELECT(ki)) {
		    goto L130;
		}
	    }
/* Computing MAX */
	    i__2 = ki + ki * t_dim1;
	    r__3 = ulp * ((r__1 = T(ki,ki).r, fabs(r__1)) + (r__2 = T(ki,ki).i, fabs(r__2)));
	    smin = max(r__3,smlnum);

	    i__2 = n__;
	    WORK(n__).r = 1.f, WORK(n__).i = 0.f;

/*           Form right-hand side. */

	    i__2 = n__;
	    for (k = ki + 1; k <= n__; ++k) {
		i__3 = k;
		r_cnjg(&q__2, &T(ki,k));
		q__1.r = -(double)q__2.r, q__1.i = -(double)q__2.i;
		WORK(k).r = q__1.r, WORK(k).i = q__1.i;
	    }

/*           Solve the triangular system:   
                (T(KI+1:N,KI+1:N) - T(KI,KI))'*X = SCALE*WORK. */

	    i__2 = n__;
	    for (k = ki + 1; k <= n__; ++k) {
		i__3 = k + k * t_dim1;
		i__4 = k + k * t_dim1;
		i__5 = ki + ki * t_dim1;
		q__1.r = T(k,k).r - T(ki,ki).r, q__1.i = T(k,k).i - T(ki,ki)
			.i;
		T(k,k).r = q__1.r, T(k,k).i = q__1.i;
		i__3 = k + k * t_dim1;
		if ((r__1 = T(k,k).r, fabs(r__1)) + (r__2 = T(k,k).i, fabs(r__2)) < smin) {
		    i__4 = k + k * t_dim1;
		    T(k,k).r = smin, T(k,k).i = 0.f;
		}
	    }

	    if (ki < n__) {
		i__2 = n__ - ki;
		clatrs("Upper", "Conjugate transpose", "Non-unit", "Y", 
			i__2, &T(ki+1,ki+1), ldt, &WORK(ki + 
			1), &scale, &RWORK(1), info);
		i__2 = ki;
		WORK(ki).r = scale, WORK(ki).i = 0.f;
	    }

/*           Copy the vector x or Q*x to VL and normalize. */

	    if (! over) {
		i__2 = n__ - ki + 1;
		ccopy(i__2, &WORK(ki), c__1, &VL(ki,is), c__1)
			;

		i__2 = n__ - ki + 1;
		ii = icamax(i__2, &VL(ki,is), c__1) + ki - 1;
		i__2 = ii + is * vl_dim1;
		remax = 1.f / ((r__1 = VL(ii,is).r, fabs(r__1)) + (r__2 = 
			VL(ii,is).i, fabs(r__2)));
		i__2 = n__ - ki + 1;
		csscal(i__2, remax, &VL(ki,is), c__1);

		i__2 = ki - 1;
		for (k = 1; k <= ki-1; ++k) {
		    i__3 = k + is * vl_dim1;
		    VL(k,is).r = 0.f, VL(k,is).i = 0.f;
		}
	    } else {
		if (ki < n__) {
		    i__2 = n__ - ki;
		    q__1.r = scale, q__1.i = 0.f;
		    cgemv("N", n__, i__2, c_b2, &VL(1,ki+1), 
			  ldvl, &WORK(ki + 1), c__1, q__1, &VL(1,ki), c__1);
		}

		ii = icamax( n__, &VL(1,ki), c__1);
		i__2 = ii + ki * vl_dim1;
		remax = 1.f / ((r__1 = VL(ii,ki).r, fabs(r__1)) + (r__2 = 
			VL(ii,ki).i, fabs(r__2)));
		csscal( n__, remax, &VL(1,ki), c__1);
	    }

/*           Set back the original diagonal elements of T. */

	    i__2 = n__;
	    for (k = ki + 1; k <= n__; ++k) {
		i__3 = k + k * t_dim1;
		i__4 = k + n__;
		T(k,k).r = WORK(k+n__).r, T(k,k).i = WORK(k+n__).i;
	    }

	    ++is;
L130:
	    ;
	}
    }

} 

