
#include <math.h>
#include "complex.h"
#include "mathtool.h"


void clahqr(int wantt, int wantz, int n__, 
	    int ilo, int ihi, fcomplex *h, int ldh, fcomplex *w, 
	    int iloz, int ihiz, fcomplex *z, int ldz, int *info)
{
/*  -- LAPACK auxiliary routine (version 2.0) --   
       Univ. of Tennessee, Univ. of California Berkeley, NAG Ltd.,   
       Courant Institute, Argonne National Lab, and Rice University   
       September 30, 1994   


    Purpose   
    =======   

    CLAHQR is an auxiliary routine called by CHSEQR to update the   
    eigenvalues and Schur decomposition already computed by CHSEQR, by   
    dealing with the Hessenberg submatrix in rows and columns ILO to IHI. 
  

    Arguments   
    =========   

    WANTT   (input) LOGICAL   
            = .TRUE. : the full Schur form T is required;   
            = .FALSE.: only eigenvalues are required.   

    WANTZ   (input) LOGICAL   
            = .TRUE. : the matrix of Schur vectors Z is required;   
            = .FALSE.: Schur vectors are not required.   

    N       (input) INTEGER   
            The order of the matrix H.  N >= 0.   

    ILO     (input) INTEGER   
    IHI     (input) INTEGER   
            It is assumed that H is already upper triangular in rows and 
  
            columns IHI+1:N, and that H(ILO,ILO-1) = 0 (unless ILO = 1). 
  
            CLAHQR works primarily with the Hessenberg submatrix in rows 
  
            and columns ILO to IHI, but applies transformations to all of 
  
            H if WANTT is .TRUE..   
            1 <= ILO <= max(1,IHI); IHI <= N.   

    H       (input/output) COMPLEX array, dimension (LDH,N)   
            On entry, the upper Hessenberg matrix H.   
            On exit, if WANTT is .TRUE., H is upper triangular in rows   
            and columns ILO:IHI, with any 2-by-2 diagonal blocks in   
            standard form. If WANTT is .FALSE., the contents of H are   
            unspecified on exit.   

    LDH     (input) INTEGER   
            The leading dimension of the array H. LDH >= max(1,N).   

    W       (output) COMPLEX array, dimension (N)   
            The computed eigenvalues ILO to IHI are stored in the   
            corresponding elements of W. If WANTT is .TRUE., the   
            eigenvalues are stored in the same order as on the diagonal   
            of the Schur form returned in H, with W(i) = H(i,i).   

    ILOZ    (input) INTEGER   
    IHIZ    (input) INTEGER   
            Specify the rows of Z to which transformations must be   
            applied if WANTZ is .TRUE..   
            1 <= ILOZ <= ILO; IHI <= IHIZ <= N.   

    Z       (input/output) COMPLEX array, dimension (LDZ,N)   
            If WANTZ is .TRUE., on entry Z must contain the current   
            matrix Z of transformations accumulated by CHSEQR, and on   
            exit Z has been updated; transformations are applied only to 
  
            the submatrix Z(ILOZ:IHIZ,ILO:IHI).   
            If WANTZ is .FALSE., Z is not referenced.   

    LDZ     (input) INTEGER   
            The leading dimension of the array Z. LDZ >= max(1,N).   

    INFO    (output) INTEGER   
            = 0: successful exit   
            > 0: if INFO = i, CLAHQR failed to compute all the   
                 eigenvalues ILO to IHI in a total of 30*(IHI-ILO+1)   
                 iterations; elements i+1:ihi of W contain those   
                 eigenvalues which have been successfully computed.   

    ===================================================================== 
  


    
   Parameter adjustments   
       Function Body */
    /* Table of constant values */
    static int c__2 = 2;
    static int c__1 = 1;
    
    /* System generated locals */
    float r__1, r__2, r__3, r__4, r__5, r__6;
    double d__1;
    fcomplex q__1, q__2, q__3, q__4;
    /* Builtin functions */

    /* Local variables */
    static float unfl, ovfl;
    static fcomplex temp;
    static int i, j, k, l, m;
    static float s;
    static fcomplex t, u, v[2], x, y;
    static float rtemp;
    static int i1, i2;
    static float rwork[1];
    static fcomplex t1;
    static float t2;
    static fcomplex v2;
    static float h10;
    static fcomplex h11;
    static float h21;
    static fcomplex h22;
    static int nh;
    static int nz;
    static float smlnum;
    static fcomplex h11s;
    static int itn, its;
    static float ulp;
    static fcomplex sum;
    static float tst1;



#define W(I) w[(I)-1]

#define H(I,J) h[(I)-1 + ((J)-1)* ( ldh)]
#define Z(I,J) z[(I)-1 + ((J)-1)* ( ldz)]

    *info = 0;

/*     Quick return if possible */

    if (n__ == 0) {
	return ;
    }
    if (ilo == ihi) {
	W(ilo).r = H(ilo,ilo).r, W(ilo).i = H(ilo,ilo).i;
	return ;
    }

    nh = ihi - ilo + 1;
    nz = ihiz - iloz + 1;

/*     Set machine-dependent constants for the stopping criterion.   
       If norm(H) <= sqrt(OVFL), overflow should not occur. */

    unfl = slamch("Safe minimum");
    ovfl = 1.f / unfl;
    slabad(&unfl, &ovfl);
    ulp = slamch("Precision");
    smlnum = unfl * (nh / ulp);

/*     I1 and I2 are the indices of the first row and last column of H   
       to which transformations must be applied. If eigenvalues only are 
  
       being computed, I1 and I2 are set inside the main loop. */

    if (wantt) {
	i1 = 1;
	i2 = n__;
    }

/*     ITN is the total number of QR iterations allowed. */

    itn = nh * 30;

/*     The main loop begins here. I is the loop index and decreases from 
  
       IHI to ILO in steps of 1. Each iteration of the loop works   
       with the active submatrix in rows and columns L to I.   
       Eigenvalues I+1 to IHI have already converged. Either L = ILO, or 
  
       H(L,L-1) is negligible so that the matrix splits. */

    i = ihi;
L10:
    if (i < ilo) {
	goto L130;
    }

/*     Perform QR iterations on rows and columns ILO to I until a   
       submatrix of order 1 splits off at the bottom because a   
       subdiagonal element has become negligible. */

    l = ilo;
    for (its = 0; its <= itn; ++its) {

/*        Look for a single small subdiagonal element. */

	for (k = i; k >= l+1; --k) {
	    tst1 = (r__1 = H(k-1,k-1).r, fabs(r__1)) + (r__2 = H(k-1,k-1).i, fabs(r__2)) + ((r__3 = H(k,k).r, 
		    fabs(r__3)) + (r__4 = H(k,k).i, fabs(
		    r__4)));
	    if (tst1 == 0.f) {
		tst1 = clanhs("1", i - l + 1, &H(l,l), ldh, rwork);
	    }
/* Computing MAX */
	    r__2 = ulp * tst1;
	    if ((r__1 = H(k,k-1).r, fabs(r__1)) <= max(r__2,smlnum)) {
		goto L30;
	    }
	}
L30:
	l = k;
	if (l > ilo) {

/*           H(L,L-1) is negligible */

	    H(l,l-1).r = 0.f, H(l,l-1).i = 0.f;
	}

/*        Exit from loop if a submatrix of order 1 has split off. */

	if (l >= i) {
	    goto L120;
	}

/*        Now the active submatrix is in rows and columns L to I. If 
  
          eigenvalues only are being computed, only the active submatrix   
          need be transformed. */

	if (! (wantt)) {
	    i1 = l;
	    i2 = i;
	}

	if (its == 10 || its == 20) {

/*           Exceptional shift. */

	    d__1 = (r__1 = H(i,i-1).r, fabs(r__1)) + (r__2 = H(i-1,i-2).r, fabs(
		    r__2));
	    t.r = d__1, t.i = 0.f;
	} else {

/*           Wilkinson's shift. */

	    t.r = H(i,i).r, t.i = H(i,i).i;
	    d__1 = H(i,i-1).r;
	    q__1.r = d__1 * H(i-1,i).r, q__1.i = d__1 * H(i-1,i).i;
	    u.r = q__1.r, u.i = q__1.i;
	    if (u.r != 0.f || u.i != 0.f) {
		q__2.r = H(i-1,i-1).r - t.r, q__2.i = H(i-1,i-1).i - t.i;
		q__1.r = q__2.r * .5f, q__1.i = q__2.i * .5f;
		x.r = q__1.r, x.i = q__1.i;
		q__3.r = x.r * x.r - x.i * x.i, q__3.i = x.r * x.i + x.i * 
			x.r;
		q__2.r = q__3.r + u.r, q__2.i = q__3.i + u.i;
		q__1 = Csqrt(q__2);
		y.r = q__1.r, y.i = q__1.i;
		if (x.r * y.r + x.i * y.i < 0.f) {
		    q__1.r = -(double)y.r, q__1.i = -(double)y.i;
		    y.r = q__1.r, y.i = q__1.i;
		}
		q__3.r = x.r + y.r, q__3.i = x.i + y.i;
		cladiv(&q__2, u, q__3);
		q__1.r = t.r - q__2.r, q__1.i = t.i - q__2.i;
		t.r = q__1.r, t.i = q__1.i;
	    }
	}

/*        Look for two consecutive small subdiagonal elements. */

	for (m = i - 1; m >= l; --m) {

/*           Determine the effect of starting the single-shift QR 
  
             iteration at row M, and see if this would make H(M,M-1)   
             negligible. */

	    h11.r = H(m,m).r, h11.i = H(m,m).i;
	    h22.r = H(m+1,m+1).r, h22.i = H(m+1,m+1).i;
	    q__1.r = h11.r - t.r, q__1.i = h11.i - t.i;
	    h11s.r = q__1.r, h11s.i = q__1.i;
	    h21 = H(m+1,m).r;
	    s = (r__1 = h11s.r, fabs(r__1)) + (r__2 = h11s.i, fabs(
		    r__2)) + fabs(h21);
	    q__1.r = h11s.r / s, q__1.i = h11s.i / s;
	    h11s.r = q__1.r, h11s.i = q__1.i;
	    h21 /= s;
	    v[0].r = h11s.r, v[0].i = h11s.i;
	    v[1].r = h21, v[1].i = 0.f;
	    if (m == l) {
		goto L50;
	    }
	    h10 = H(m,m-1).r;
	    tst1 = ((r__1 = h11s.r, fabs(r__1)) + (r__2 = h11s.i, fabs(
		    r__2))) * ((r__3 = h11.r, fabs(r__3)) + (r__4 = 
		    h11.i, fabs(r__4)) + ((r__5 = h22.r, fabs(r__5)) + (r__6 = 
		    h22.i, fabs(r__6))));
	    if ((r__1 = h10 * h21, fabs(r__1)) <= ulp * tst1) {
		goto L50;
	    }
	}
L50:

/*        Single-shift QR step */

	for (k = m; k <= i-1; ++k) {

/*           The first iteration of this loop determines a reflection G   
             from the vector V and applies it from left and right to H,   
             thus creating a nonzero bulge below the subdiagonal. 
  

             Each subsequent iteration determines a reflection G to   
             restore the Hessenberg form in the (K-1)th column, and thus   
             chases the bulge one step toward the bottom of the active   
             submatrix.   

             V(2) is always real before the call to CLARFG, and hence   
             after the call T2 ( = T1*V(2) ) is also real. */

	    if (k > m) {
		ccopy(c__2, &H(k,k-1), c__1, v, c__1);
	    }
	    clarfg(c__2, v, &v[1], c__1, &t1);
	    if (k > m) {
		H(k,k-1).r = v[0].r, H(k,k-1).i = v[0].i;
		H(k+1,k-1).r = 0.f, H(k+1,k-1).i = 0.f;
	    }
	    v2.r = v[1].r, v2.i = v[1].i;
	    q__1.r = t1.r * v2.r - t1.i * v2.i, q__1.i = t1.r * v2.i + t1.i * 
		    v2.r;
	    t2 = q__1.r;

/*           Apply G from the left to transform the rows of the matrix   
             in columns K to I2. */

	    for (j = k; j <= i2; ++j) {
		r_cnjg(&q__3, &t1);
		q__2.r = q__3.r * H(k,j).r - q__3.i * H(k,j).i, q__2.i = 
			q__3.r * H(k,j).i + q__3.i * H(k,j).r;
		q__4.r = t2 * H(k+1,j).r, q__4.i = t2 * H(k+1,j).i;
		q__1.r = q__2.r + q__4.r, q__1.i = q__2.i + q__4.i;
		sum.r = q__1.r, sum.i = q__1.i;
		q__1.r = H(k,j).r - sum.r, q__1.i = H(k,j).i - sum.i;
		H(k,j).r = q__1.r, H(k,j).i = q__1.i;
		q__2.r = sum.r * v2.r - sum.i * v2.i, q__2.i = sum.r * v2.i + 
			sum.i * v2.r;
		q__1.r = H(k+1,j).r - q__2.r, q__1.i = H(k+1,j).i - q__2.i;
		H(k+1,j).r = q__1.r, H(k+1,j).i = q__1.i;
	    }

/*           Apply G from the right to transform the columns of the   
             matrix in rows I1 to min(K+2,I).   

   Computing MIN */
	    for (j = i1; j <= min(k+2,i); ++j) {
		q__2.r = t1.r * H(j,k).r - t1.i * H(j,k).i, q__2.i = t1.r * 
			H(j,k).i + t1.i * H(j,k).r;
		q__3.r = t2 * H(j,k+1).r, q__3.i = t2 * H(j,k+1).i;
		q__1.r = q__2.r + q__3.r, q__1.i = q__2.i + q__3.i;
		sum.r = q__1.r, sum.i = q__1.i;
		q__1.r = H(j,k).r - sum.r, q__1.i = H(j,k).i - sum.i;
		H(j,k).r = q__1.r, H(j,k).i = q__1.i;
		r_cnjg(&q__3, &v2);
		q__2.r = sum.r * q__3.r - sum.i * q__3.i, q__2.i = sum.r * 
			q__3.i + sum.i * q__3.r;
		q__1.r = H(j,k+1).r - q__2.r, q__1.i = H(j,k+1).i - q__2.i;
		H(j,k+1).r = q__1.r, H(j,k+1).i = q__1.i;
	    }

	    if (wantz) {

/*              Accumulate transformations in the matrix Z */

		for (j = iloz; j <= ihiz; ++j) {
		    q__2.r = t1.r * Z(j,k).r - t1.i * Z(j,k).i, q__2.i = 
			    t1.r * Z(j,k).i + t1.i * Z(j,k).r;
		    q__3.r = t2 * Z(j,k+1).r, q__3.i = t2 * Z(j,k+1).i;
		    q__1.r = q__2.r + q__3.r, q__1.i = q__2.i + q__3.i;
		    sum.r = q__1.r, sum.i = q__1.i;
		    q__1.r = Z(j,k).r - sum.r, q__1.i = Z(j,k).i - sum.i;
		    Z(j,k).r = q__1.r, Z(j,k).i = q__1.i;
		    r_cnjg(&q__3, &v2);
		    q__2.r = sum.r * q__3.r - sum.i * q__3.i, q__2.i = sum.r *
			     q__3.i + sum.i * q__3.r;
		    q__1.r = Z(j,k+1).r - q__2.r, q__1.i = Z(j,k+1).i - q__2.i;
		    Z(j,k+1).r = q__1.r, Z(j,k+1).i = q__1.i;
		}
	    }

	    if (k == m && m > l) {

/*              If the QR step was started at row M > L because two   
                consecutive small subdiagonals were found, then extra   
                scaling must be performed to ensure that H(M,M-1) remains   
                real. */

		q__1.r = 1.f - t1.r, q__1.i = 0.f - t1.i;
		temp.r = q__1.r, temp.i = q__1.i;
		r__1 = temp.r;
		r__2 = temp.i;
		d__1 = slapy2(r__1, r__2);
		q__1.r = temp.r / d__1, q__1.i = temp.i / d__1;
		temp.r = q__1.r, temp.i = q__1.i;
		r_cnjg(&q__2, &temp);
		q__1.r = H(m+1,m).r * q__2.r - H(m+1,m).i * q__2.i, q__1.i = H(m+1,m).r * q__2.i + H(m+1,m).i * q__2.r;
		H(m+1,m).r = q__1.r, H(m+1,m).i = q__1.i;
		if (m + 2 <= i) {
		    q__1.r = H(m+2,m+1).r * temp.r - H(m+2,m+1).i * temp.i, q__1.i =
			     H(m+2,m+1).r * temp.i + H(m+2,m+1).i * temp.r;
		    H(m+2,m+1).r = q__1.r, H(m+2,m+1).i = q__1.i;
		}
		for (j = m; j <= i; ++j) {
		    if (j != m + 1) {
			if (i2 > j) {
			    cscal(i2 - j, temp, &H(j,j+1), ldh);
			}
			r_cnjg(&q__1, &temp);
			cscal(j - i1, q__1, &H(i1,j), c__1);
			if (wantz) {
			    r_cnjg(&q__1, &temp);
			    cscal(nz, q__1, &Z(iloz,j), c__1);
			}
		    }
		}
	    }
	}

/*        Ensure that H(I,I-1) is real. */

	temp.r = H(i,i-1).r, temp.i = H(i,i-1).i;
	if (temp.i != 0.f) {
	    r__1 = temp.r;
	    r__2 = temp.i;
	    rtemp = slapy2(r__1, r__2);
	    H(i,i-1).r = rtemp, H(i,i-1).i = 0.f;
	    q__1.r = temp.r / rtemp, q__1.i = temp.i / rtemp;
	    temp.r = q__1.r, temp.i = q__1.i;
	    if (i2 > i) {
		r_cnjg(&q__1, &temp);
		cscal(i2 - i, q__1, &H(i,i+1), ldh);
	    }
	    cscal(i - i1, temp, &H(i1,i), c__1);
	    if (wantz) {
		cscal(nz, temp, &Z(iloz,i), c__1);
	    }
	}

    }

/*     Failure to converge in remaining number of iterations */

    *info = i;
    return ;

L120:

/*     H(I,I-1) is negligible: one eigenvalue has converged. */

    W(i).r = H(i,i).r, W(i).i = H(i,i).i;

/*     Decrement number of remaining iterations, and return to start of   
       the main loop with new value of I. */

    itn -= its;
    i = l - 1;
    goto L10;

L130:
    return;

} 

