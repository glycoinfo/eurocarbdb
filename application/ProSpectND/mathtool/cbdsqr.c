

#include <math.h>
#include "complex.h"
#include "mathtool.h"


void cbdsqr(char *uplo, int n__, int ncvt, int 
	    nru, int ncc, float *d, float *e, fcomplex *vt, int ldvt, 
	    fcomplex *u, int ldu, fcomplex *c, int ldc, float *rwork, 
	    int *info)
{
/*  -- LAPACK routine (version 2.0) --   
       Univ. of Tennessee, Univ. of California Berkeley, NAG Ltd.,   
       Courant Institute, Argonne National Lab, and Rice University   
       September 30, 1994   


    Purpose   
    =======   

    CBDSQR computes the singular value decomposition (SVD) of a real   
    N-by-N (upper or lower) bidiagonal matrix B:  B = Q * S * P' (P'   
    denotes the transpose of P), where S is a diagonal matrix with   
    non-negative diagonal elements (the singular values of B), and Q   
    and P are orthogonal matrices.   

    The routine computes S, and optionally computes U * Q, P' * VT,   
    or Q' * C, for given complex input matrices U, VT, and C.   

    See "Computing  Small Singular Values of Bidiagonal Matrices With   
    Guaranteed High Relative Accuracy," by J. Demmel and W. Kahan,   
    LAPACK Working Note #3 (or SIAM J. Sci. Statist. Comput. vol. 11,   
    no. 5, pp. 873-912, Sept 1990) and   
    "Accurate singular values and differential qd algorithms," by   
    B. Parlett and V. Fernando, Technical Report CPAM-554, Mathematics   
    Department, University of California at Berkeley, July 1992   
    for a detailed description of the algorithm.   

    Arguments   
    =========   

    UPLO    (input) CHARACTER*1   
            = 'U':  B is upper bidiagonal;   
            = 'L':  B is lower bidiagonal.   

    N       (input) INTEGER   
            The order of the matrix B.  N >= 0.   

    NCVT    (input) INTEGER   
            The number of columns of the matrix VT. NCVT >= 0.   

    NRU     (input) INTEGER   
            The number of rows of the matrix U. NRU >= 0.   

    NCC     (input) INTEGER   
            The number of columns of the matrix C. NCC >= 0.   

    D       (input/output) REAL array, dimension (N)   
            On entry, the n diagonal elements of the bidiagonal matrix B. 
  
            On exit, if INFO=0, the singular values of B in decreasing   
            order.   

    E       (input/output) REAL array, dimension (N)   
            On entry, the elements of E contain the   
            offdiagonal elements of of the bidiagonal matrix whose SVD   
            is desired. On normal exit (INFO = 0), E is destroyed.   
            If the algorithm does not converge (INFO > 0), D and E   
            will contain the diagonal and superdiagonal elements of a   
            bidiagonal matrix orthogonally equivalent to the one given   
            as input. E(N) is used for workspace.   

    VT      (input/output) COMPLEX array, dimension (LDVT, NCVT)   
            On entry, an N-by-NCVT matrix VT.   
            On exit, VT is overwritten by P' * VT.   
            VT is not referenced if NCVT = 0.   

    LDVT    (input) INTEGER   
            The leading dimension of the array VT.   
            LDVT >= max(1,N) if NCVT > 0; LDVT >= 1 if NCVT = 0.   

    U       (input/output) COMPLEX array, dimension (LDU, N)   
            On entry, an NRU-by-N matrix U.   
            On exit, U is overwritten by U * Q.   
            U is not referenced if NRU = 0.   

    LDU     (input) INTEGER   
            The leading dimension of the array U.  LDU >= max(1,NRU).   

    C       (input/output) COMPLEX array, dimension (LDC, NCC)   
            On entry, an N-by-NCC matrix C.   
            On exit, C is overwritten by Q' * C.   
            C is not referenced if NCC = 0.   

    LDC     (input) INTEGER   
            The leading dimension of the array C.   
            LDC >= max(1,N) if NCC > 0; LDC >=1 if NCC = 0.   

    RWORK   (workspace) REAL array, dimension   
              2*N  if only singular values wanted (NCVT = NRU = NCC = 0) 
  
              max( 1, 4*N-4 ) otherwise   

    INFO    (output) INTEGER   
            = 0:  successful exit   
            < 0:  If INFO = -i, the i-th argument had an illegal value   
            > 0:  the algorithm did not converge; D and E contain the   
                  elements of a bidiagonal matrix which is orthogonally   
                  similar to the input matrix B;  if INFO = i, i   
                  elements of E have not converged to zero.   

    Internal Parameters   
    ===================   

    TOLMUL  REAL, default = max(10,min(100,EPS**(-1/8)))   
            TOLMUL controls the convergence criterion of the QR loop.   
            If it is positive, TOLMUL*EPS is the desired relative   
               precision in the computed singular values.   
            If it is negative, abs(TOLMUL*EPS*sigma_max) is the   
               desired absolute accuracy in the computed singular   
               values (corresponds to relative accuracy   
               abs(TOLMUL*EPS) in the largest singular value.   
            abs(TOLMUL) should be between 1 and 1/EPS, and preferably   
               between 10 (for fast convergence) and .1/EPS   
               (for there to be some accuracy in the results).   
            Default is to lose at either one eighth or 2 of the   
               available decimal digits in each computed singular value   
               (whichever is smaller).   

    MAXITR  INTEGER, default = 6   
            MAXITR controls the maximum number of passes of the   
            algorithm through its inner loop. The algorithms stops   
            (and so fails to converge) if the number of passes   
            through the inner loop exceeds MAXITR*N**2.   

    ===================================================================== 
  


       Test the input parameters.   

    
   Parameter adjustments   
       Function Body */
    /* Table of constant values */
    static double c_b15 = -.125;
    static int c__1 = 1;
    static float c_b48 = 1.f;
    static float c_b71 = -1.f;
    
    /* System generated locals */
    int  i__1, i__2;
    float r__1, r__2, r__3, r__4;
    double d__1;
    /* Builtin functions */
    /* Local variables */
    static float abse;
    static int idir;
    static float abss;
    static int oldm;
    static float cosl;
    static int isub, iter;
    static float unfl, sinl, cosr, smin, smax, sinr;
    static int irot;
    static float f, g, h;
    static int i, j, m;
    static float r;
    static float oldcs;
    static int oldll;
    static float shift, sigmn, oldsn;
    static int maxit;
    static float sminl, sigmx;
    static int iuplo;
    static float cs;
    static int ll;
    static float sn, mu;
    static float sminoa;
    static float thresh;
    static int rotate;
    static float sminlo;
    static int nm1;
    static float tolmul;
    static int nm12, nm13, lll;
    static float eps, sll, tol;



#define D(I) d[(I)-1]
#define E(I) e[(I)-1]
#define RWORK(I) rwork[(I)-1]

#define VT(I,J) vt[(I)-1 + ((J)-1)* ( ldvt)]
#define U(I,J) u[(I)-1 + ((J)-1)* ( ldu)]
#define C(I,J) c[(I)-1 + ((J)-1)* ( ldc)]

    *info = 0;
    iuplo = 0;
    if (lsame(uplo, "U")) {
	iuplo = 1;
    }
    if (lsame(uplo, "L")) {
	iuplo = 2;
    }
    if (iuplo == 0) {
	*info = -1;
    } else if (n__ < 0) {
	*info = -2;
    } else if (ncvt < 0) {
	*info = -3;
    } else if (nru < 0) {
	*info = -4;
    } else if (ncc < 0) {
	*info = -5;
    } else if ((ncvt == 0 && ldvt < 1) || (ncvt > 0 && ldvt < max(1,n__))) {
	*info = -9;
    } else if (ldu < max(1,nru)) {
	*info = -11;
    } else if ((ncc == 0 && ldc < 1) || (ncc > 0 && ldc < max(1,n__))) {
	*info = -13;
    }
    if (*info != 0) {
	i__1 = -(*info);
	return ;
    }
    if (n__ == 0) {
	return ;
    }
    if (n__ == 1) {
	goto L150;
    }

    /*
     *     ROTATE is true if any singular vectors desired, false otherwise 
     */

    rotate = ncvt > 0 || nru > 0 || ncc > 0;

    /*
     *     If no singular vectors desired, use qd algorithm 
     */

    if (! rotate) {
	slasq1(n__, &D(1), &E(1), &RWORK(1), info);
	return ;
    }

    nm1 = n__ - 1;
    nm12 = nm1 + nm1;
    nm13 = nm12 + nm1;

    /*
     *     Get machine constants 
     */

    eps = slamch("Epsilon");
    unfl = slamch("Safe minimum");

    /*
     *     If matrix lower bidiagonal, rotate to be upper bidiagonal 
     *     by applying Givens rotations on the left 
     */

    if (iuplo == 2) {
	i__1 = n__ - 1;
	for (i = 1; i <= n__-1; ++i) {
	    slartg(D(i), E(i), &cs, &sn, &r);
	    D(i) = r;
	    E(i) = sn * D(i + 1);
	    D(i + 1) = cs * D(i + 1);
	    RWORK(i) = cs;
	    RWORK(nm1 + i) = sn;
	}

        /*
         *        Update singular vectors if desired 
         */

	if (nru > 0) {
	    clasr("R", "V", "F", nru, n__, &RWORK(1), &RWORK(n__), &U(1,1),
		     ldu);
	}
	if (ncc > 0) {
	    clasr("L", "V", "F", n__, ncc, &RWORK(1), &RWORK(n__), &C(1,1),
		     ldc);
	}
    }

    /* Compute singular values to relative accuracy TOL   
     * (By setting TOL to be negative, algorithm will compute   
     * singular values to absolute accuracy ABS(TOL)*norm(input matrix)) 
     * Computing MAX   
     * Computing MIN 
     */
    d__1 = (double) eps;
    r__3 = 100.f, r__4 = pow(d__1, c_b15);
    r__1 = 10.f, r__2 = min(r__3,r__4);
    tolmul = max(r__1,r__2);
    tol = tolmul * eps;

    /*
     *     Compute approximate maximum, minimum singular values 
     */

    smax = (r__1 = D(n__), fabs(r__1));
    i__1 = n__ - 1;
    for (i = 1; i <= n__-1; ++i) {
    /*
     * Computing MAX 
     */
	r__3 = smax, r__4 = (r__1 = D(i), fabs(r__1)), r__3 = max(r__3,r__4), 
		r__4 = (r__2 = E(i), fabs(r__2));
	smax = max(r__3,r__4);
    }
    sminl = 0.f;
    if (tol >= 0.f) {

        /*
         *        Relative accuracy desired 
         */

	sminoa = fabs(D(1));
	if (sminoa == 0.f) {
	    goto L40;
	}
	mu = sminoa;
	i__1 = n__;
	for (i = 2; i <= n__; ++i) {
	    mu = (r__1 = D(i), fabs(r__1)) * (mu / (mu + (r__2 = E(i - 1), 
		    fabs(r__2))));
	    sminoa = min(sminoa,mu);
	    if (sminoa == 0.f) {
		goto L40;
	    }
	}
L40:
	sminoa /= sqrt((float) (n__));
    /*
     * Computing MAX 
     */
	r__1 = tol * sminoa, r__2 = n__ * 6 * n__ * unfl;
	thresh = max(r__1,r__2);
    } else {

    /*
     *        Absolute accuracy desired 
     *  Computing MAX 
     */
	r__1 = fabs(tol) * smax, r__2 = n__ * 6 * n__ * unfl;
	thresh = max(r__1,r__2);
    }

    /*     Prepare for main iteration loop for the singular values   
     *     (MAXIT is the maximum number of passes through the inner   
     *     loop permitted before nonconvergence signalled.) 
     */

    maxit = n__ * 6 * n__;
    iter = 0;
    oldll = -1;
    oldm = -1;

    /*
     *     M points to last element of unconverged part of matrix 
     */

    m = n__;

    /*
     *     Begin main iteration loop 
     */

L50:

    /*
     *     Check for convergence or exceeding iteration count 
     */

    if (m <= 1) {
	goto L150;
    }
    if (iter > maxit) {
	goto L190;
    }

    /*
     *     Find diagonal block of matrix to work on 
     */

    if (tol < 0.f && (r__1 = D(m), fabs(r__1)) <= thresh) {
	D(m) = 0.f;
    }
    smax = (r__1 = D(m), fabs(r__1));
    smin = smax;
    i__1 = m;
    for (lll = 1; lll <= m; ++lll) {
	ll = m - lll;
	if (ll == 0) {
	    goto L80;
	}
	abss = (r__1 = D(ll), fabs(r__1));
	abse = (r__1 = E(ll), fabs(r__1));
	if (tol < 0.f && abss <= thresh) {
	    D(ll) = 0.f;
	}
	if (abse <= thresh) {
	    goto L70;
	}
	smin = min(smin,abss);
        /*
         * Computing MAX 
         */
	r__1 = max(smax,abss);
	smax = max(r__1,abse);
    }
L70:
    E(ll) = 0.f;

    /*
     *     Matrix splits since E(LL) = 0 
     */

    if (ll == m - 1) {

        /*
         *        Convergence of bottom singular value, return to top of loop 
         */

	--m;
	goto L50;
    }
L80:
    ++ll;

    /*
     *     E(LL) through E(M-1) are nonzero, E(LL-1) is zero 
     */

    if (ll == m - 1) {

        /*
         *        2 by 2 block, handle separately 
         */

	slasv2(D(m - 1), E(m - 1), D(m), &sigmn, &sigmx, &sinr, &cosr, &
		sinl, &cosl);
	D(m - 1) = sigmx;
	E(m - 1) = 0.f;
	D(m) = sigmn;

        /*
         *        Compute singular vectors, if desired 
         */

	if (ncvt > 0) {
	    csrot(ncvt, &VT(m-1,1), ldvt, &VT(m,1), ldvt, 
		    cosr, sinr);
	}
	if (nru > 0) {
	    csrot(nru, &U(1,m-1), c__1, &U(1,m), 
		    c__1, cosl, sinl);
	}
	if (ncc > 0) {
	    csrot(ncc, &C(m-1,1), ldc, &C(m,1), ldc, cosl, 
		    sinl);
	}
	m += -2;
	goto L50;
    }

    /*
     *     If working on new submatrix, choose shift direction 
     *     (from larger end diagonal element towards smaller) 
     */

    if (ll > oldm || m < oldll) {
	if ((r__1 = D(ll), fabs(r__1)) >= (r__2 = D(m), fabs(r__2))) {

            /*
             *   Chase bulge from top (big end) to bottom (small end) 
             */

	    idir = 1;
	} else {

            /*
             *   Chase bulge from bottom (big end) to top (small end) 
             */

	    idir = 2;
	}
    }

    /*
     *     Apply convergence tests 
     */
    if (idir == 1) {

        /*
         *        Run convergence test in forward direction 
         *        First apply standard test to bottom of matrix 
         */

	if ((r__1 = E(m - 1), fabs(r__1)) <= fabs(tol) * (r__2 = D(m), fabs(
		r__2)) || (tol < 0.f && (r__3 = E(m - 1), fabs(r__3)) <= 
		thresh)) {
	    E(m - 1) = 0.f;
	    goto L50;
	}

	if (tol >= 0.f) {

            /*
             *     If relative accuracy desired, 
             *     apply convergence criterion forward 
             */

	    mu = (r__1 = D(ll), fabs(r__1));
	    sminl = mu;
	    i__1 = m - 1;
	    for (lll = ll; lll <= m-1; ++lll) {
		if ((r__1 = E(lll), fabs(r__1)) <= tol * mu) {
		    E(lll) = 0.f;
		    goto L50;
		}
		sminlo = sminl;
		mu = (r__1 = D(lll + 1), fabs(r__1)) * (mu / (mu + (r__2 = E(
			lll), fabs(r__2))));
		sminl = min(sminl,mu);
	    }
	}

    } else {

        /*
         *        Run convergence test in backward direction 
         *        First apply standard test to top of matrix 
         */

	if ((r__1 = E(ll), fabs(r__1)) <= fabs(tol) * (r__2 = D(ll), fabs(
		r__2)) || (tol < 0.f && (r__3 = E(ll), fabs(r__3)) <= thresh)) {
	    E(ll) = 0.f;
	    goto L50;
	}

	if (tol >= 0.f) {

            /*
             * If relative accuracy desired, 
             * apply convergence criterion backward 
             */

	    mu = (r__1 = D(m), fabs(r__1));
	    sminl = mu;
	    i__1 = ll;
	    for (lll = m - 1; lll >= ll; --lll) {
		if ((r__1 = E(lll), fabs(r__1)) <= tol * mu) {
		    E(lll) = 0.f;
		    goto L50;
		}
		sminlo = sminl;
		mu = (r__1 = D(lll), fabs(r__1)) * (mu / (mu + (r__2 = E(lll),
			 fabs(r__2))));
		sminl = min(sminl,mu);
	    }
	}
    }
    oldll = ll;
    oldm = m;

    /*
     *     Compute shift.  First, test if shifting would ruin relative 
     *     accuracy, and if so set the shift to zero.   
     *     Computing MAX 
     */
    r__1 = eps, r__2 = tol * .01f;
    if (tol >= 0.f && n__ * tol * (sminl / smax) <= max(r__1,r__2)) {

    /*
     *        Use a zero shift to avoid loss of relative accuracy 
     */

	shift = 0.f;
    } else {

        /*
         *        Compute the shift from 2-by-2 block at end of matrix 
         */

	if (idir == 1) {
	    sll = (r__1 = D(ll), fabs(r__1));
	    slas2(D(m - 1), E(m - 1), D(m), &shift, &r);
	} else {
	    sll = (r__1 = D(m), fabs(r__1));
	    slas2(D(ll), E(ll), D(ll + 1), &shift, &r);
	}

        /*
         *        Test if shift negligible, and if so set to zero 
         */

	if (sll > 0.f) {
            /*
             * Computing 2nd power 
             */
	    r__1 = shift / sll;
	    if (r__1 * r__1 < eps) {
		shift = 0.f;
	    }
	}
    }

    /*
     *     Increment iteration count 
     */

    iter = iter + m - ll;

    /*
     *     If SHIFT = 0, do simplified QR iteration 
     */

    if (shift == 0.f) {
	if (idir == 1) {

            /*
             *  Chase bulge from top to bottom 
             *  Save cosines and sines for later singular vector updates 
             */

	    cs = 1.f;
	    oldcs = 1.f;
	    r__1 = D(ll) * cs;
	    slartg(r__1, E(ll), &cs, &sn, &r);
	    r__1 = oldcs * r;
	    r__2 = D(ll + 1) * sn;
	    slartg(r__1, r__2, &oldcs, &oldsn, &D(ll));
	    RWORK(1) = cs;
	    RWORK(nm1 + 1) = sn;
	    RWORK(nm12 + 1) = oldcs;
	    RWORK(nm13 + 1) = oldsn;
	    irot = 1;
	    i__1 = m - 1;
	    for (i = ll + 1; i <= m-1; ++i) {
		r__1 = D(i) * cs;
		slartg(r__1, E(i), &cs, &sn, &r);
		E(i - 1) = oldsn * r;
		r__1 = oldcs * r;
		r__2 = D(i + 1) * sn;
		slartg(r__1, r__2, &oldcs, &oldsn, &D(i));
		++irot;
		RWORK(irot) = cs;
		RWORK(irot + nm1) = sn;
		RWORK(irot + nm12) = oldcs;
		RWORK(irot + nm13) = oldsn;
	    }
	    h = D(m) * cs;
	    D(m) = h * oldcs;
	    E(m - 1) = h * oldsn;

            /*
             *           Update singular vectors 
             */

	    if (ncvt > 0) {
		i__1 = m - ll + 1;
		clasr("L", "V", "F", i__1, ncvt, &RWORK(1), &RWORK(n__), &VT(ll,1), ldvt);
	    }
	    if (nru > 0) {
		i__1 = m - ll + 1;
		clasr("R", "V", "F", nru, i__1, &RWORK(nm12 + 1), &RWORK(
			nm13 + 1), &U(1,ll), ldu);
	    }
	    if (ncc > 0) {
		i__1 = m - ll + 1;
		clasr("L", "V", "F", i__1, ncc, &RWORK(nm12 + 1), &RWORK(
			nm13 + 1), &C(ll,1), ldc);
	    }

            /*
             *           Test convergence 
             */

	    if ((r__1 = E(m - 1), fabs(r__1)) <= thresh) {
		E(m - 1) = 0.f;
	    }

	} else {

            /*
             *  Chase bulge from bottom to top 
             *  Save cosines and sines for later singular vector updates 
             */

	    cs = 1.f;
	    oldcs = 1.f;
	    r__1 = D(m) * cs;
	    slartg(r__1, E(m - 1), &cs, &sn, &r);
	    r__1 = oldcs * r;
	    r__2 = D(m - 1) * sn;
	    slartg(r__1, r__2, &oldcs, &oldsn, &D(m));
	    RWORK(m - ll) = cs;
	    RWORK(m - ll + nm1) = -(double)sn;
	    RWORK(m - ll + nm12) = oldcs;
	    RWORK(m - ll + nm13) = -(double)oldsn;
	    irot = m - ll;
	    i__1 = ll + 1;
	    for (i = m - 1; i >= ll+1; --i) {
		r__1 = D(i) * cs;
		slartg(r__1, E(i - 1), &cs, &sn, &r);
		E(i) = oldsn * r;
		r__1 = oldcs * r;
		r__2 = D(i - 1) * sn;
		slartg(r__1, r__2, &oldcs, &oldsn, &D(i));
		--irot;
		RWORK(irot) = cs;
		RWORK(irot + nm1) = -(double)sn;
		RWORK(irot + nm12) = oldcs;
		RWORK(irot + nm13) = -(double)oldsn;
	    }
	    h = D(ll) * cs;
	    D(ll) = h * oldcs;
	    E(ll) = h * oldsn;

            /*
             *           Update singular vectors 
             */

	    if (ncvt > 0) {
		i__1 = m - ll + 1;
		clasr("L", "V", "B", i__1, ncvt, &RWORK(nm12 + 1), &RWORK(
			nm13 + 1), &VT(ll,1), ldvt);
	    }
	    if (nru > 0) {
		i__1 = m - ll + 1;
		clasr("R", "V", "B", nru, i__1, &RWORK(1), &RWORK(n__), &U(1,ll), ldu);
	    }
	    if (ncc > 0) {
		i__1 = m - ll + 1;
		clasr("L", "V", "B", i__1, ncc, &RWORK(1), &RWORK(n__), &C(ll,1), ldc);
	    }

            /*
             *           Test convergence 
             */

	    if ((r__1 = E(ll), fabs(r__1)) <= thresh) {
		E(ll) = 0.f;
	    }
	}
    } else {

        /*
         *        Use nonzero shift 
         */

	if (idir == 1) {

            /*
             *   Chase bulge from top to bottom 
             *   Save cosines and sines for later singular vector updates 
             */

	    f = ((r__1 = D(ll), fabs(r__1)) - shift) * (sign(c_b48, D(ll))
		     + shift / D(ll));
	    g = E(ll);
	    slartg(f, g, &cosr, &sinr, &r);
	    f = cosr * D(ll) + sinr * E(ll);
	    E(ll) = cosr * E(ll) - sinr * D(ll);
	    g = sinr * D(ll + 1);
	    D(ll + 1) = cosr * D(ll + 1);
	    slartg(f, g, &cosl, &sinl, &r);
	    D(ll) = r;
	    f = cosl * E(ll) + sinl * D(ll + 1);
	    D(ll + 1) = cosl * D(ll + 1) - sinl * E(ll);
	    g = sinl * E(ll + 1);
	    E(ll + 1) = cosl * E(ll + 1);
	    RWORK(1) = cosr;
	    RWORK(nm1 + 1) = sinr;
	    RWORK(nm12 + 1) = cosl;
	    RWORK(nm13 + 1) = sinl;
	    irot = 1;
	    i__1 = m - 2;
	    for (i = ll + 1; i <= m-2; ++i) {
		slartg(f, g, &cosr, &sinr, &r);
		E(i - 1) = r;
		f = cosr * D(i) + sinr * E(i);
		E(i) = cosr * E(i) - sinr * D(i);
		g = sinr * D(i + 1);
		D(i + 1) = cosr * D(i + 1);
		slartg(f, g, &cosl, &sinl, &r);
		D(i) = r;
		f = cosl * E(i) + sinl * D(i + 1);
		D(i + 1) = cosl * D(i + 1) - sinl * E(i);
		g = sinl * E(i + 1);
		E(i + 1) = cosl * E(i + 1);
		++irot;
		RWORK(irot) = cosr;
		RWORK(irot + nm1) = sinr;
		RWORK(irot + nm12) = cosl;
		RWORK(irot + nm13) = sinl;
	    }
	    slartg(f, g, &cosr, &sinr, &r);
	    E(m - 2) = r;
	    f = cosr * D(m - 1) + sinr * E(m - 1);
	    E(m - 1) = cosr * E(m - 1) - sinr * D(m - 1);
	    g = sinr * D(m);
	    D(m) = cosr * D(m);
	    slartg(f, g, &cosl, &sinl, &r);
	    D(m - 1) = r;
	    f = cosl * E(m - 1) + sinl * D(m);
	    D(m) = cosl * D(m) - sinl * E(m - 1);
	    ++irot;
	    RWORK(irot) = cosr;
	    RWORK(irot + nm1) = sinr;
	    RWORK(irot + nm12) = cosl;
	    RWORK(irot + nm13) = sinl;
	    E(m - 1) = f;

            /*
             *           Update singular vectors 
             */

	    if (ncvt > 0) {
		i__1 = m - ll + 1;
		clasr("L", "V", "F", i__1, ncvt, &RWORK(1), &RWORK(n__), &VT(ll,1), ldvt);
	    }
	    if (nru > 0) {
		i__1 = m - ll + 1;
		clasr("R", "V", "F", nru, i__1, &RWORK(nm12 + 1), &RWORK(
			nm13 + 1), &U(1,ll), ldu);
	    }
	    if (ncc > 0) {
		i__1 = m - ll + 1;
		clasr("L", "V", "F", i__1, ncc, &RWORK(nm12 + 1), &RWORK(
			nm13 + 1), &C(ll,1), ldc);
	    }

            /*
             *           Test convergence 
             */

	    if ((r__1 = E(m - 1), fabs(r__1)) <= thresh) {
		E(m - 1) = 0.f;
	    }

	} else {

            /*
             *  Chase bulge from bottom to top 
             *  Save cosines and sines for later singular vector updates 
             */

	    f = ((r__1 = D(m), fabs(r__1)) - shift) * (sign(c_b48, D(m)) 
		    + shift / D(m));
	    g = E(m - 1);
	    slartg(f, g, &cosr, &sinr, &r);
	    f = cosr * D(m) + sinr * E(m - 1);
	    E(m - 1) = cosr * E(m - 1) - sinr * D(m);
	    g = sinr * D(m - 1);
	    D(m - 1) = cosr * D(m - 1);
	    slartg(f, g, &cosl, &sinl, &r);
	    D(m) = r;
	    f = cosl * E(m - 1) + sinl * D(m - 1);
	    D(m - 1) = cosl * D(m - 1) - sinl * E(m - 1);
	    g = sinl * E(m - 2);
	    E(m - 2) = cosl * E(m - 2);
	    RWORK(m - ll) = cosr;
	    RWORK(m - ll + nm1) = -(double)sinr;
	    RWORK(m - ll + nm12) = cosl;
	    RWORK(m - ll + nm13) = -(double)sinl;
	    irot = m - ll;
	    i__1 = ll + 2;
	    for (i = m - 1; i >= ll+2; --i) {
		slartg(f, g, &cosr, &sinr, &r);
		E(i) = r;
		f = cosr * D(i) + sinr * E(i - 1);
		E(i - 1) = cosr * E(i - 1) - sinr * D(i);
		g = sinr * D(i - 1);
		D(i - 1) = cosr * D(i - 1);
		slartg(f, g, &cosl, &sinl, &r);
		D(i) = r;
		f = cosl * E(i - 1) + sinl * D(i - 1);
		D(i - 1) = cosl * D(i - 1) - sinl * E(i - 1);
		g = sinl * E(i - 2);
		E(i - 2) = cosl * E(i - 2);
		--irot;
		RWORK(irot) = cosr;
		RWORK(irot + nm1) = -(double)sinr;
		RWORK(irot + nm12) = cosl;
		RWORK(irot + nm13) = -(double)sinl;
	    }
	    slartg(f, g, &cosr, &sinr, &r);
	    E(ll + 1) = r;
	    f = cosr * D(ll + 1) + sinr * E(ll);
	    E(ll) = cosr * E(ll) - sinr * D(ll + 1);
	    g = sinr * D(ll);
	    D(ll) = cosr * D(ll);
	    slartg(f, g, &cosl, &sinl, &r);
	    D(ll + 1) = r;
	    f = cosl * E(ll) + sinl * D(ll);
	    D(ll) = cosl * D(ll) - sinl * E(ll);
	    --irot;
	    RWORK(irot) = cosr;
	    RWORK(irot + nm1) = -(double)sinr;
	    RWORK(irot + nm12) = cosl;
	    RWORK(irot + nm13) = -(double)sinl;
	    E(ll) = f;

            /*
             *           Test convergence 
             */

	    if ((r__1 = E(ll), fabs(r__1)) <= thresh) {
		E(ll) = 0.f;
	    }

            /*
             *           Update singular vectors if desired 
             */

	    if (ncvt > 0) {
		i__1 = m - ll + 1;
		clasr("L", "V", "B", i__1, ncvt, &RWORK(nm12 + 1), &RWORK(
			nm13 + 1), &VT(ll,1), ldvt);
	    }
	    if (nru > 0) {
		i__1 = m - ll + 1;
		clasr("R", "V", "B", nru, i__1, &RWORK(1), &RWORK(n__), &U(1,ll), ldu);
	    }
	    if (ncc > 0) {
		i__1 = m - ll + 1;
		clasr("L", "V", "B", i__1, ncc, &RWORK(1), &RWORK(n__), &C(ll,1), ldc);
	    }
	}
    }

    /*
     *     QR iteration finished, go back and check convergence 
     */

    goto L50;

    /*
     *     All singular values converged, so make them positive 
     */

L150:
    i__1 = n__;
    for (i = 1; i <= n__; ++i) {
	if (D(i) < 0.f) {
	    D(i) = -(double)D(i);

            /*
             *   Change sign of singular vectors, if desired 
             */

	    if (ncvt > 0) {
		csscal(ncvt, c_b71, &VT(i,1), ldvt);
	    }
	}
    }

    /*
     *     Sort the singular values into decreasing order (insertion sort on 
     *     singular values, but only one transposition per singular vector) 
     */

    i__1 = n__ - 1;
    for (i = 1; i <= n__-1; ++i) {

        /*
         *        Scan for smallest D(I) 
         */

	isub = 1;
	smin = D(1);
	i__2 = n__ + 1 - i;
	for (j = 2; j <= n__+1-i; ++j) {
	    if (D(j) <= smin) {
		isub = j;
		smin = D(j);
	    }
	}
	if (isub != n__ + 1 - i) {

            /*
             *           Swap singular values and vectors 
             */

	    D(isub) = D(n__ + 1 - i);
	    D(n__ + 1 - i) = smin;
	    if (ncvt > 0) {
		cswap(ncvt, &VT(isub,1), ldvt, &VT(n__+1-i,1), ldvt);
	    }
	    if (nru > 0) {
		cswap(nru, &U(1,isub), c__1, &U(1,n__+1-i), c__1);
	    }
	    if (ncc > 0) {
		cswap(ncc, &C(isub,1), ldc, &C(n__+1-i,1), 
			ldc);
	    }
	}
    }
    goto L210;

    /*
     *     Maximum number of iterations exceeded, failure to converge 
     */

L190:
    *info = 0;
    i__1 = n__ - 1;
    for (i = 1; i <= n__-1; ++i) {
	if (E(i) != 0.f) {
	    ++(*info);
	}
    }
L210:
    return;
}
