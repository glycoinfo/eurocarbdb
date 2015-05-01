

#include "complex.h"
#include "mathtool.h"

void clarfx(char *side, int m__, int n__, fcomplex *v, 
	    fcomplex tau, fcomplex *c, int ldc, fcomplex *work)
{
/*  -- LAPACK auxiliary routine (version 2.0) --   
       Univ. of Tennessee, Univ. of California Berkeley, NAG Ltd.,   
       Courant Institute, Argonne National Lab, and Rice University   
       September 30, 1994   


    Purpose   
    =======   

    CLARFX applies a complex elementary reflector H to a complex m by n   
    matrix C, from either the left or the right. H is represented in the 
  
    form   

          H = I - tau * v * v'   

    where tau is a complex scalar and v is a complex vector.   

    If tau = 0, then H is taken to be the unit matrix   

    This version uses inline code if H has order < 11.   

    Arguments   
    =========   

    SIDE    (input) CHARACTER*1   
            = 'L': form  H * C   
            = 'R': form  C * H   

    M       (input) INTEGER   
            The number of rows of the matrix C.   

    N       (input) INTEGER   
            The number of columns of the matrix C.   

    V       (input) COMPLEX array, dimension (M) if SIDE = 'L'   
                                          or (N) if SIDE = 'R'   
            The vector v in the representation of H.   

    TAU     (input) COMPLEX   
            The value tau in the representation of H.   

    C       (input/output) COMPLEX array, dimension (LDC,N)   
            On entry, the m by n matrix C.   
            On exit, C is overwritten by the matrix H * C if SIDE = 'L', 
  
            or C * H if SIDE = 'R'.   

    LDC     (input) INTEGER   
            The leading dimension of the array C. LDA >= max(1,M).   

    WORK    (workspace) COMPLEX array, dimension (N) if SIDE = 'L'   
                                              or (M) if SIDE = 'R'   
            WORK is not referenced if H has order < 11.   

    ===================================================================== 
  


    
   Parameter adjustments   
       Function Body */
    /* Table of constant values */
    static fcomplex c_b1 = {0.f,0.f};
    static fcomplex c_b2 = {1.f,0.f};
    static int c__1 = 1;
    
    /* System generated locals */
    int c_dim1,  i__1, i__2, i__3, i__4, i__5, i__6, i__7, i__8, 
	    i__9, i__10, i__11;
    fcomplex q__1, q__2, q__3, q__4, q__5, q__6, q__7, q__8, q__9, q__10, 
	    q__11, q__12, q__13, q__14, q__15, q__16, q__17, q__18, q__19;
    /* Local variables */
    static int j;
    static fcomplex t1, t2, t3, t4, t5, t6, t7, t8, t9, v1, v2, v3, v4, v5, v6,
	     v7, v8, v9, t10, v10, sum;



#define V(I) v[(I)-1]
#define WORK(I) work[(I)-1]

#define C(I,J) c[(I)-1 + ((J)-1)* ( ldc)]

    if (tau.r == 0.f && tau.i == 0.f) {
	return ;
    }
    if (lsame(side, "L")) {

/*        Form  H * C, where H has order m. */

	switch (m__) {
	    case 1:  goto L10;
	    case 2:  goto L30;
	    case 3:  goto L50;
	    case 4:  goto L70;
	    case 5:  goto L90;
	    case 6:  goto L110;
	    case 7:  goto L130;
	    case 8:  goto L150;
	    case 9:  goto L170;
	    case 10:  goto L190;
	}

/*        Code for general M   

          w := C'*v */

	cgemv("Conjugate transpose", m__, n__, c_b2, &C(1,1), ldc, &V(1), 
		c__1, c_b1, &WORK(1), c__1);

/*        C := C - tau * v * w' */

	q__1.r = -(double)tau.r, q__1.i = -(double)tau.i;
	cgerc(m__, n__, q__1, &V(1), c__1, &WORK(1), c__1, &C(1,1), ldc);
	goto L410;
L10:

/*        Special code for 1 x 1 Householder */

	q__3.r = tau.r * V(1).r - tau.i * V(1).i, q__3.i = tau.r * V(1).i 
		+ tau.i * V(1).r;
	r_cnjg(&q__4, &V(1));
	q__2.r = q__3.r * q__4.r - q__3.i * q__4.i, q__2.i = q__3.r * q__4.i 
		+ q__3.i * q__4.r;
	q__1.r = 1.f - q__2.r, q__1.i = 0.f - q__2.i;
	t1.r = q__1.r, t1.i = q__1.i;
	i__1 = n__;
	for (j = 1; j <= n__; ++j) {
	    i__2 = j * c_dim1 + 1;
	    i__3 = j * c_dim1 + 1;
	    q__1.r = t1.r * C(1,j).r - t1.i * C(1,j).i, q__1.i = t1.r * C(1,j).i + t1.i * C(1,j).r;
	    C(1,j).r = q__1.r, C(1,j).i = q__1.i;

	}
	goto L410;
L30:

/*        Special code for 2 x 2 Householder */

	r_cnjg(&q__1, &V(1));
	v1.r = q__1.r, v1.i = q__1.i;
	r_cnjg(&q__2, &v1);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t1.r = q__1.r, t1.i = q__1.i;
	r_cnjg(&q__1, &V(2));
	v2.r = q__1.r, v2.i = q__1.i;
	r_cnjg(&q__2, &v2);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t2.r = q__1.r, t2.i = q__1.i;
	i__1 = n__;
	for (j = 1; j <= n__; ++j) {
	    i__2 = j * c_dim1 + 1;
	    q__2.r = v1.r * C(1,j).r - v1.i * C(1,j).i, q__2.i = v1.r * C(1,j).i + v1.i * C(1,j).r;
	    i__3 = j * c_dim1 + 2;
	    q__3.r = v2.r * C(2,j).r - v2.i * C(2,j).i, q__3.i = v2.r * C(2,j).i + v2.i * C(2,j).r;
	    q__1.r = q__2.r + q__3.r, q__1.i = q__2.i + q__3.i;
	    sum.r = q__1.r, sum.i = q__1.i;
	    i__2 = j * c_dim1 + 1;
	    i__3 = j * c_dim1 + 1;
	    q__2.r = sum.r * t1.r - sum.i * t1.i, q__2.i = sum.r * t1.i + 
		    sum.i * t1.r;
	    q__1.r = C(1,j).r - q__2.r, q__1.i = C(1,j).i - q__2.i;
	    C(1,j).r = q__1.r, C(1,j).i = q__1.i;
	    i__2 = j * c_dim1 + 2;
	    i__3 = j * c_dim1 + 2;
	    q__2.r = sum.r * t2.r - sum.i * t2.i, q__2.i = sum.r * t2.i + 
		    sum.i * t2.r;
	    q__1.r = C(2,j).r - q__2.r, q__1.i = C(2,j).i - q__2.i;
	    C(2,j).r = q__1.r, C(2,j).i = q__1.i;

	}
	goto L410;
L50:

/*        Special code for 3 x 3 Householder */

	r_cnjg(&q__1, &V(1));
	v1.r = q__1.r, v1.i = q__1.i;
	r_cnjg(&q__2, &v1);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t1.r = q__1.r, t1.i = q__1.i;
	r_cnjg(&q__1, &V(2));
	v2.r = q__1.r, v2.i = q__1.i;
	r_cnjg(&q__2, &v2);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t2.r = q__1.r, t2.i = q__1.i;
	r_cnjg(&q__1, &V(3));
	v3.r = q__1.r, v3.i = q__1.i;
	r_cnjg(&q__2, &v3);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t3.r = q__1.r, t3.i = q__1.i;
	i__1 = n__;
	for (j = 1; j <= n__; ++j) {
	    i__2 = j * c_dim1 + 1;
	    q__3.r = v1.r * C(1,j).r - v1.i * C(1,j).i, q__3.i = v1.r * C(1,j).i + v1.i * C(1,j).r;
	    i__3 = j * c_dim1 + 2;
	    q__4.r = v2.r * C(2,j).r - v2.i * C(2,j).i, q__4.i = v2.r * C(2,j).i + v2.i * C(2,j).r;
	    q__2.r = q__3.r + q__4.r, q__2.i = q__3.i + q__4.i;
	    i__4 = j * c_dim1 + 3;
	    q__5.r = v3.r * C(3,j).r - v3.i * C(3,j).i, q__5.i = v3.r * C(3,j).i + v3.i * C(3,j).r;
	    q__1.r = q__2.r + q__5.r, q__1.i = q__2.i + q__5.i;
	    sum.r = q__1.r, sum.i = q__1.i;
	    i__2 = j * c_dim1 + 1;
	    i__3 = j * c_dim1 + 1;
	    q__2.r = sum.r * t1.r - sum.i * t1.i, q__2.i = sum.r * t1.i + 
		    sum.i * t1.r;
	    q__1.r = C(1,j).r - q__2.r, q__1.i = C(1,j).i - q__2.i;
	    C(1,j).r = q__1.r, C(1,j).i = q__1.i;
	    i__2 = j * c_dim1 + 2;
	    i__3 = j * c_dim1 + 2;
	    q__2.r = sum.r * t2.r - sum.i * t2.i, q__2.i = sum.r * t2.i + 
		    sum.i * t2.r;
	    q__1.r = C(2,j).r - q__2.r, q__1.i = C(2,j).i - q__2.i;
	    C(2,j).r = q__1.r, C(2,j).i = q__1.i;
	    i__2 = j * c_dim1 + 3;
	    i__3 = j * c_dim1 + 3;
	    q__2.r = sum.r * t3.r - sum.i * t3.i, q__2.i = sum.r * t3.i + 
		    sum.i * t3.r;
	    q__1.r = C(3,j).r - q__2.r, q__1.i = C(3,j).i - q__2.i;
	    C(3,j).r = q__1.r, C(3,j).i = q__1.i;

	}
	goto L410;
L70:

/*        Special code for 4 x 4 Householder */

	r_cnjg(&q__1, &V(1));
	v1.r = q__1.r, v1.i = q__1.i;
	r_cnjg(&q__2, &v1);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t1.r = q__1.r, t1.i = q__1.i;
	r_cnjg(&q__1, &V(2));
	v2.r = q__1.r, v2.i = q__1.i;
	r_cnjg(&q__2, &v2);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t2.r = q__1.r, t2.i = q__1.i;
	r_cnjg(&q__1, &V(3));
	v3.r = q__1.r, v3.i = q__1.i;
	r_cnjg(&q__2, &v3);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t3.r = q__1.r, t3.i = q__1.i;
	r_cnjg(&q__1, &V(4));
	v4.r = q__1.r, v4.i = q__1.i;
	r_cnjg(&q__2, &v4);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t4.r = q__1.r, t4.i = q__1.i;
	i__1 = n__;
	for (j = 1; j <= n__; ++j) {
	    i__2 = j * c_dim1 + 1;
	    q__4.r = v1.r * C(1,j).r - v1.i * C(1,j).i, q__4.i = v1.r * C(1,j).i + v1.i * C(1,j).r;
	    i__3 = j * c_dim1 + 2;
	    q__5.r = v2.r * C(2,j).r - v2.i * C(2,j).i, q__5.i = v2.r * C(2,j).i + v2.i * C(2,j).r;
	    q__3.r = q__4.r + q__5.r, q__3.i = q__4.i + q__5.i;
	    i__4 = j * c_dim1 + 3;
	    q__6.r = v3.r * C(3,j).r - v3.i * C(3,j).i, q__6.i = v3.r * C(3,j).i + v3.i * C(3,j).r;
	    q__2.r = q__3.r + q__6.r, q__2.i = q__3.i + q__6.i;
	    i__5 = j * c_dim1 + 4;
	    q__7.r = v4.r * C(4,j).r - v4.i * C(4,j).i, q__7.i = v4.r * C(4,j).i + v4.i * C(4,j).r;
	    q__1.r = q__2.r + q__7.r, q__1.i = q__2.i + q__7.i;
	    sum.r = q__1.r, sum.i = q__1.i;
	    i__2 = j * c_dim1 + 1;
	    i__3 = j * c_dim1 + 1;
	    q__2.r = sum.r * t1.r - sum.i * t1.i, q__2.i = sum.r * t1.i + 
		    sum.i * t1.r;
	    q__1.r = C(1,j).r - q__2.r, q__1.i = C(1,j).i - q__2.i;
	    C(1,j).r = q__1.r, C(1,j).i = q__1.i;
	    i__2 = j * c_dim1 + 2;
	    i__3 = j * c_dim1 + 2;
	    q__2.r = sum.r * t2.r - sum.i * t2.i, q__2.i = sum.r * t2.i + 
		    sum.i * t2.r;
	    q__1.r = C(2,j).r - q__2.r, q__1.i = C(2,j).i - q__2.i;
	    C(2,j).r = q__1.r, C(2,j).i = q__1.i;
	    i__2 = j * c_dim1 + 3;
	    i__3 = j * c_dim1 + 3;
	    q__2.r = sum.r * t3.r - sum.i * t3.i, q__2.i = sum.r * t3.i + 
		    sum.i * t3.r;
	    q__1.r = C(3,j).r - q__2.r, q__1.i = C(3,j).i - q__2.i;
	    C(3,j).r = q__1.r, C(3,j).i = q__1.i;
	    i__2 = j * c_dim1 + 4;
	    i__3 = j * c_dim1 + 4;
	    q__2.r = sum.r * t4.r - sum.i * t4.i, q__2.i = sum.r * t4.i + 
		    sum.i * t4.r;
	    q__1.r = C(4,j).r - q__2.r, q__1.i = C(4,j).i - q__2.i;
	    C(4,j).r = q__1.r, C(4,j).i = q__1.i;

	}
	goto L410;
L90:

/*        Special code for 5 x 5 Householder */

	r_cnjg(&q__1, &V(1));
	v1.r = q__1.r, v1.i = q__1.i;
	r_cnjg(&q__2, &v1);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t1.r = q__1.r, t1.i = q__1.i;
	r_cnjg(&q__1, &V(2));
	v2.r = q__1.r, v2.i = q__1.i;
	r_cnjg(&q__2, &v2);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t2.r = q__1.r, t2.i = q__1.i;
	r_cnjg(&q__1, &V(3));
	v3.r = q__1.r, v3.i = q__1.i;
	r_cnjg(&q__2, &v3);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t3.r = q__1.r, t3.i = q__1.i;
	r_cnjg(&q__1, &V(4));
	v4.r = q__1.r, v4.i = q__1.i;
	r_cnjg(&q__2, &v4);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t4.r = q__1.r, t4.i = q__1.i;
	r_cnjg(&q__1, &V(5));
	v5.r = q__1.r, v5.i = q__1.i;
	r_cnjg(&q__2, &v5);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t5.r = q__1.r, t5.i = q__1.i;
	i__1 = n__;
	for (j = 1; j <= n__; ++j) {
	    i__2 = j * c_dim1 + 1;
	    q__5.r = v1.r * C(1,j).r - v1.i * C(1,j).i, q__5.i = v1.r * C(1,j).i + v1.i * C(1,j).r;
	    i__3 = j * c_dim1 + 2;
	    q__6.r = v2.r * C(2,j).r - v2.i * C(2,j).i, q__6.i = v2.r * C(2,j).i + v2.i * C(2,j).r;
	    q__4.r = q__5.r + q__6.r, q__4.i = q__5.i + q__6.i;
	    i__4 = j * c_dim1 + 3;
	    q__7.r = v3.r * C(3,j).r - v3.i * C(3,j).i, q__7.i = v3.r * C(3,j).i + v3.i * C(3,j).r;
	    q__3.r = q__4.r + q__7.r, q__3.i = q__4.i + q__7.i;
	    i__5 = j * c_dim1 + 4;
	    q__8.r = v4.r * C(4,j).r - v4.i * C(4,j).i, q__8.i = v4.r * C(4,j).i + v4.i * C(4,j).r;
	    q__2.r = q__3.r + q__8.r, q__2.i = q__3.i + q__8.i;
	    i__6 = j * c_dim1 + 5;
	    q__9.r = v5.r * C(5,j).r - v5.i * C(5,j).i, q__9.i = v5.r * C(5,j).i + v5.i * C(5,j).r;
	    q__1.r = q__2.r + q__9.r, q__1.i = q__2.i + q__9.i;
	    sum.r = q__1.r, sum.i = q__1.i;
	    i__2 = j * c_dim1 + 1;
	    i__3 = j * c_dim1 + 1;
	    q__2.r = sum.r * t1.r - sum.i * t1.i, q__2.i = sum.r * t1.i + 
		    sum.i * t1.r;
	    q__1.r = C(1,j).r - q__2.r, q__1.i = C(1,j).i - q__2.i;
	    C(1,j).r = q__1.r, C(1,j).i = q__1.i;
	    i__2 = j * c_dim1 + 2;
	    i__3 = j * c_dim1 + 2;
	    q__2.r = sum.r * t2.r - sum.i * t2.i, q__2.i = sum.r * t2.i + 
		    sum.i * t2.r;
	    q__1.r = C(2,j).r - q__2.r, q__1.i = C(2,j).i - q__2.i;
	    C(2,j).r = q__1.r, C(2,j).i = q__1.i;
	    i__2 = j * c_dim1 + 3;
	    i__3 = j * c_dim1 + 3;
	    q__2.r = sum.r * t3.r - sum.i * t3.i, q__2.i = sum.r * t3.i + 
		    sum.i * t3.r;
	    q__1.r = C(3,j).r - q__2.r, q__1.i = C(3,j).i - q__2.i;
	    C(3,j).r = q__1.r, C(3,j).i = q__1.i;
	    i__2 = j * c_dim1 + 4;
	    i__3 = j * c_dim1 + 4;
	    q__2.r = sum.r * t4.r - sum.i * t4.i, q__2.i = sum.r * t4.i + 
		    sum.i * t4.r;
	    q__1.r = C(4,j).r - q__2.r, q__1.i = C(4,j).i - q__2.i;
	    C(4,j).r = q__1.r, C(4,j).i = q__1.i;
	    i__2 = j * c_dim1 + 5;
	    i__3 = j * c_dim1 + 5;
	    q__2.r = sum.r * t5.r - sum.i * t5.i, q__2.i = sum.r * t5.i + 
		    sum.i * t5.r;
	    q__1.r = C(5,j).r - q__2.r, q__1.i = C(5,j).i - q__2.i;
	    C(5,j).r = q__1.r, C(5,j).i = q__1.i;

	}
	goto L410;
L110:

/*        Special code for 6 x 6 Householder */

	r_cnjg(&q__1, &V(1));
	v1.r = q__1.r, v1.i = q__1.i;
	r_cnjg(&q__2, &v1);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t1.r = q__1.r, t1.i = q__1.i;
	r_cnjg(&q__1, &V(2));
	v2.r = q__1.r, v2.i = q__1.i;
	r_cnjg(&q__2, &v2);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t2.r = q__1.r, t2.i = q__1.i;
	r_cnjg(&q__1, &V(3));
	v3.r = q__1.r, v3.i = q__1.i;
	r_cnjg(&q__2, &v3);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t3.r = q__1.r, t3.i = q__1.i;
	r_cnjg(&q__1, &V(4));
	v4.r = q__1.r, v4.i = q__1.i;
	r_cnjg(&q__2, &v4);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t4.r = q__1.r, t4.i = q__1.i;
	r_cnjg(&q__1, &V(5));
	v5.r = q__1.r, v5.i = q__1.i;
	r_cnjg(&q__2, &v5);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t5.r = q__1.r, t5.i = q__1.i;
	r_cnjg(&q__1, &V(6));
	v6.r = q__1.r, v6.i = q__1.i;
	r_cnjg(&q__2, &v6);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t6.r = q__1.r, t6.i = q__1.i;
	i__1 = n__;
	for (j = 1; j <= n__; ++j) {
	    i__2 = j * c_dim1 + 1;
	    q__6.r = v1.r * C(1,j).r - v1.i * C(1,j).i, q__6.i = v1.r * C(1,j).i + v1.i * C(1,j).r;
	    i__3 = j * c_dim1 + 2;
	    q__7.r = v2.r * C(2,j).r - v2.i * C(2,j).i, q__7.i = v2.r * C(2,j).i + v2.i * C(2,j).r;
	    q__5.r = q__6.r + q__7.r, q__5.i = q__6.i + q__7.i;
	    i__4 = j * c_dim1 + 3;
	    q__8.r = v3.r * C(3,j).r - v3.i * C(3,j).i, q__8.i = v3.r * C(3,j).i + v3.i * C(3,j).r;
	    q__4.r = q__5.r + q__8.r, q__4.i = q__5.i + q__8.i;
	    i__5 = j * c_dim1 + 4;
	    q__9.r = v4.r * C(4,j).r - v4.i * C(4,j).i, q__9.i = v4.r * C(4,j).i + v4.i * C(4,j).r;
	    q__3.r = q__4.r + q__9.r, q__3.i = q__4.i + q__9.i;
	    i__6 = j * c_dim1 + 5;
	    q__10.r = v5.r * C(5,j).r - v5.i * C(5,j).i, q__10.i = v5.r * C(5,j).i + v5.i * C(5,j).r;
	    q__2.r = q__3.r + q__10.r, q__2.i = q__3.i + q__10.i;
	    i__7 = j * c_dim1 + 6;
	    q__11.r = v6.r * C(6,j).r - v6.i * C(6,j).i, q__11.i = v6.r * C(6,j).i + v6.i * C(6,j).r;
	    q__1.r = q__2.r + q__11.r, q__1.i = q__2.i + q__11.i;
	    sum.r = q__1.r, sum.i = q__1.i;
	    i__2 = j * c_dim1 + 1;
	    i__3 = j * c_dim1 + 1;
	    q__2.r = sum.r * t1.r - sum.i * t1.i, q__2.i = sum.r * t1.i + 
		    sum.i * t1.r;
	    q__1.r = C(1,j).r - q__2.r, q__1.i = C(1,j).i - q__2.i;
	    C(1,j).r = q__1.r, C(1,j).i = q__1.i;
	    i__2 = j * c_dim1 + 2;
	    i__3 = j * c_dim1 + 2;
	    q__2.r = sum.r * t2.r - sum.i * t2.i, q__2.i = sum.r * t2.i + 
		    sum.i * t2.r;
	    q__1.r = C(2,j).r - q__2.r, q__1.i = C(2,j).i - q__2.i;
	    C(2,j).r = q__1.r, C(2,j).i = q__1.i;
	    i__2 = j * c_dim1 + 3;
	    i__3 = j * c_dim1 + 3;
	    q__2.r = sum.r * t3.r - sum.i * t3.i, q__2.i = sum.r * t3.i + 
		    sum.i * t3.r;
	    q__1.r = C(3,j).r - q__2.r, q__1.i = C(3,j).i - q__2.i;
	    C(3,j).r = q__1.r, C(3,j).i = q__1.i;
	    i__2 = j * c_dim1 + 4;
	    i__3 = j * c_dim1 + 4;
	    q__2.r = sum.r * t4.r - sum.i * t4.i, q__2.i = sum.r * t4.i + 
		    sum.i * t4.r;
	    q__1.r = C(4,j).r - q__2.r, q__1.i = C(4,j).i - q__2.i;
	    C(4,j).r = q__1.r, C(4,j).i = q__1.i;
	    i__2 = j * c_dim1 + 5;
	    i__3 = j * c_dim1 + 5;
	    q__2.r = sum.r * t5.r - sum.i * t5.i, q__2.i = sum.r * t5.i + 
		    sum.i * t5.r;
	    q__1.r = C(5,j).r - q__2.r, q__1.i = C(5,j).i - q__2.i;
	    C(5,j).r = q__1.r, C(5,j).i = q__1.i;
	    i__2 = j * c_dim1 + 6;
	    i__3 = j * c_dim1 + 6;
	    q__2.r = sum.r * t6.r - sum.i * t6.i, q__2.i = sum.r * t6.i + 
		    sum.i * t6.r;
	    q__1.r = C(6,j).r - q__2.r, q__1.i = C(6,j).i - q__2.i;
	    C(6,j).r = q__1.r, C(6,j).i = q__1.i;

	}
	goto L410;
L130:

/*        Special code for 7 x 7 Householder */

	r_cnjg(&q__1, &V(1));
	v1.r = q__1.r, v1.i = q__1.i;
	r_cnjg(&q__2, &v1);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t1.r = q__1.r, t1.i = q__1.i;
	r_cnjg(&q__1, &V(2));
	v2.r = q__1.r, v2.i = q__1.i;
	r_cnjg(&q__2, &v2);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t2.r = q__1.r, t2.i = q__1.i;
	r_cnjg(&q__1, &V(3));
	v3.r = q__1.r, v3.i = q__1.i;
	r_cnjg(&q__2, &v3);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t3.r = q__1.r, t3.i = q__1.i;
	r_cnjg(&q__1, &V(4));
	v4.r = q__1.r, v4.i = q__1.i;
	r_cnjg(&q__2, &v4);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t4.r = q__1.r, t4.i = q__1.i;
	r_cnjg(&q__1, &V(5));
	v5.r = q__1.r, v5.i = q__1.i;
	r_cnjg(&q__2, &v5);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t5.r = q__1.r, t5.i = q__1.i;
	r_cnjg(&q__1, &V(6));
	v6.r = q__1.r, v6.i = q__1.i;
	r_cnjg(&q__2, &v6);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t6.r = q__1.r, t6.i = q__1.i;
	r_cnjg(&q__1, &V(7));
	v7.r = q__1.r, v7.i = q__1.i;
	r_cnjg(&q__2, &v7);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t7.r = q__1.r, t7.i = q__1.i;
	i__1 = n__;
	for (j = 1; j <= n__; ++j) {
	    i__2 = j * c_dim1 + 1;
	    q__7.r = v1.r * C(1,j).r - v1.i * C(1,j).i, q__7.i = v1.r * C(1,j).i + v1.i * C(1,j).r;
	    i__3 = j * c_dim1 + 2;
	    q__8.r = v2.r * C(2,j).r - v2.i * C(2,j).i, q__8.i = v2.r * C(2,j).i + v2.i * C(2,j).r;
	    q__6.r = q__7.r + q__8.r, q__6.i = q__7.i + q__8.i;
	    i__4 = j * c_dim1 + 3;
	    q__9.r = v3.r * C(3,j).r - v3.i * C(3,j).i, q__9.i = v3.r * C(3,j).i + v3.i * C(3,j).r;
	    q__5.r = q__6.r + q__9.r, q__5.i = q__6.i + q__9.i;
	    i__5 = j * c_dim1 + 4;
	    q__10.r = v4.r * C(4,j).r - v4.i * C(4,j).i, q__10.i = v4.r * C(4,j).i + v4.i * C(4,j).r;
	    q__4.r = q__5.r + q__10.r, q__4.i = q__5.i + q__10.i;
	    i__6 = j * c_dim1 + 5;
	    q__11.r = v5.r * C(5,j).r - v5.i * C(5,j).i, q__11.i = v5.r * C(5,j).i + v5.i * C(5,j).r;
	    q__3.r = q__4.r + q__11.r, q__3.i = q__4.i + q__11.i;
	    i__7 = j * c_dim1 + 6;
	    q__12.r = v6.r * C(6,j).r - v6.i * C(6,j).i, q__12.i = v6.r * C(6,j).i + v6.i * C(6,j).r;
	    q__2.r = q__3.r + q__12.r, q__2.i = q__3.i + q__12.i;
	    i__8 = j * c_dim1 + 7;
	    q__13.r = v7.r * C(7,j).r - v7.i * C(7,j).i, q__13.i = v7.r * C(7,j).i + v7.i * C(7,j).r;
	    q__1.r = q__2.r + q__13.r, q__1.i = q__2.i + q__13.i;
	    sum.r = q__1.r, sum.i = q__1.i;
	    i__2 = j * c_dim1 + 1;
	    i__3 = j * c_dim1 + 1;
	    q__2.r = sum.r * t1.r - sum.i * t1.i, q__2.i = sum.r * t1.i + 
		    sum.i * t1.r;
	    q__1.r = C(1,j).r - q__2.r, q__1.i = C(1,j).i - q__2.i;
	    C(1,j).r = q__1.r, C(1,j).i = q__1.i;
	    i__2 = j * c_dim1 + 2;
	    i__3 = j * c_dim1 + 2;
	    q__2.r = sum.r * t2.r - sum.i * t2.i, q__2.i = sum.r * t2.i + 
		    sum.i * t2.r;
	    q__1.r = C(2,j).r - q__2.r, q__1.i = C(2,j).i - q__2.i;
	    C(2,j).r = q__1.r, C(2,j).i = q__1.i;
	    i__2 = j * c_dim1 + 3;
	    i__3 = j * c_dim1 + 3;
	    q__2.r = sum.r * t3.r - sum.i * t3.i, q__2.i = sum.r * t3.i + 
		    sum.i * t3.r;
	    q__1.r = C(3,j).r - q__2.r, q__1.i = C(3,j).i - q__2.i;
	    C(3,j).r = q__1.r, C(3,j).i = q__1.i;
	    i__2 = j * c_dim1 + 4;
	    i__3 = j * c_dim1 + 4;
	    q__2.r = sum.r * t4.r - sum.i * t4.i, q__2.i = sum.r * t4.i + 
		    sum.i * t4.r;
	    q__1.r = C(4,j).r - q__2.r, q__1.i = C(4,j).i - q__2.i;
	    C(4,j).r = q__1.r, C(4,j).i = q__1.i;
	    i__2 = j * c_dim1 + 5;
	    i__3 = j * c_dim1 + 5;
	    q__2.r = sum.r * t5.r - sum.i * t5.i, q__2.i = sum.r * t5.i + 
		    sum.i * t5.r;
	    q__1.r = C(5,j).r - q__2.r, q__1.i = C(5,j).i - q__2.i;
	    C(5,j).r = q__1.r, C(5,j).i = q__1.i;
	    i__2 = j * c_dim1 + 6;
	    i__3 = j * c_dim1 + 6;
	    q__2.r = sum.r * t6.r - sum.i * t6.i, q__2.i = sum.r * t6.i + 
		    sum.i * t6.r;
	    q__1.r = C(6,j).r - q__2.r, q__1.i = C(6,j).i - q__2.i;
	    C(6,j).r = q__1.r, C(6,j).i = q__1.i;
	    i__2 = j * c_dim1 + 7;
	    i__3 = j * c_dim1 + 7;
	    q__2.r = sum.r * t7.r - sum.i * t7.i, q__2.i = sum.r * t7.i + 
		    sum.i * t7.r;
	    q__1.r = C(7,j).r - q__2.r, q__1.i = C(7,j).i - q__2.i;
	    C(7,j).r = q__1.r, C(7,j).i = q__1.i;

	}
	goto L410;
L150:

/*        Special code for 8 x 8 Householder */

	r_cnjg(&q__1, &V(1));
	v1.r = q__1.r, v1.i = q__1.i;
	r_cnjg(&q__2, &v1);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t1.r = q__1.r, t1.i = q__1.i;
	r_cnjg(&q__1, &V(2));
	v2.r = q__1.r, v2.i = q__1.i;
	r_cnjg(&q__2, &v2);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t2.r = q__1.r, t2.i = q__1.i;
	r_cnjg(&q__1, &V(3));
	v3.r = q__1.r, v3.i = q__1.i;
	r_cnjg(&q__2, &v3);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t3.r = q__1.r, t3.i = q__1.i;
	r_cnjg(&q__1, &V(4));
	v4.r = q__1.r, v4.i = q__1.i;
	r_cnjg(&q__2, &v4);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t4.r = q__1.r, t4.i = q__1.i;
	r_cnjg(&q__1, &V(5));
	v5.r = q__1.r, v5.i = q__1.i;
	r_cnjg(&q__2, &v5);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t5.r = q__1.r, t5.i = q__1.i;
	r_cnjg(&q__1, &V(6));
	v6.r = q__1.r, v6.i = q__1.i;
	r_cnjg(&q__2, &v6);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t6.r = q__1.r, t6.i = q__1.i;
	r_cnjg(&q__1, &V(7));
	v7.r = q__1.r, v7.i = q__1.i;
	r_cnjg(&q__2, &v7);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t7.r = q__1.r, t7.i = q__1.i;
	r_cnjg(&q__1, &V(8));
	v8.r = q__1.r, v8.i = q__1.i;
	r_cnjg(&q__2, &v8);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t8.r = q__1.r, t8.i = q__1.i;
	i__1 = n__;
	for (j = 1; j <= n__; ++j) {
	    i__2 = j * c_dim1 + 1;
	    q__8.r = v1.r * C(1,j).r - v1.i * C(1,j).i, q__8.i = v1.r * C(1,j).i + v1.i * C(1,j).r;
	    i__3 = j * c_dim1 + 2;
	    q__9.r = v2.r * C(2,j).r - v2.i * C(2,j).i, q__9.i = v2.r * C(2,j).i + v2.i * C(2,j).r;
	    q__7.r = q__8.r + q__9.r, q__7.i = q__8.i + q__9.i;
	    i__4 = j * c_dim1 + 3;
	    q__10.r = v3.r * C(3,j).r - v3.i * C(3,j).i, q__10.i = v3.r * C(3,j).i + v3.i * C(3,j).r;
	    q__6.r = q__7.r + q__10.r, q__6.i = q__7.i + q__10.i;
	    i__5 = j * c_dim1 + 4;
	    q__11.r = v4.r * C(4,j).r - v4.i * C(4,j).i, q__11.i = v4.r * C(4,j).i + v4.i * C(4,j).r;
	    q__5.r = q__6.r + q__11.r, q__5.i = q__6.i + q__11.i;
	    i__6 = j * c_dim1 + 5;
	    q__12.r = v5.r * C(5,j).r - v5.i * C(5,j).i, q__12.i = v5.r * C(5,j).i + v5.i * C(5,j).r;
	    q__4.r = q__5.r + q__12.r, q__4.i = q__5.i + q__12.i;
	    i__7 = j * c_dim1 + 6;
	    q__13.r = v6.r * C(6,j).r - v6.i * C(6,j).i, q__13.i = v6.r * C(6,j).i + v6.i * C(6,j).r;
	    q__3.r = q__4.r + q__13.r, q__3.i = q__4.i + q__13.i;
	    i__8 = j * c_dim1 + 7;
	    q__14.r = v7.r * C(7,j).r - v7.i * C(7,j).i, q__14.i = v7.r * C(7,j).i + v7.i * C(7,j).r;
	    q__2.r = q__3.r + q__14.r, q__2.i = q__3.i + q__14.i;
	    i__9 = j * c_dim1 + 8;
	    q__15.r = v8.r * C(8,j).r - v8.i * C(8,j).i, q__15.i = v8.r * C(8,j).i + v8.i * C(8,j).r;
	    q__1.r = q__2.r + q__15.r, q__1.i = q__2.i + q__15.i;
	    sum.r = q__1.r, sum.i = q__1.i;
	    i__2 = j * c_dim1 + 1;
	    i__3 = j * c_dim1 + 1;
	    q__2.r = sum.r * t1.r - sum.i * t1.i, q__2.i = sum.r * t1.i + 
		    sum.i * t1.r;
	    q__1.r = C(1,j).r - q__2.r, q__1.i = C(1,j).i - q__2.i;
	    C(1,j).r = q__1.r, C(1,j).i = q__1.i;
	    i__2 = j * c_dim1 + 2;
	    i__3 = j * c_dim1 + 2;
	    q__2.r = sum.r * t2.r - sum.i * t2.i, q__2.i = sum.r * t2.i + 
		    sum.i * t2.r;
	    q__1.r = C(2,j).r - q__2.r, q__1.i = C(2,j).i - q__2.i;
	    C(2,j).r = q__1.r, C(2,j).i = q__1.i;
	    i__2 = j * c_dim1 + 3;
	    i__3 = j * c_dim1 + 3;
	    q__2.r = sum.r * t3.r - sum.i * t3.i, q__2.i = sum.r * t3.i + 
		    sum.i * t3.r;
	    q__1.r = C(3,j).r - q__2.r, q__1.i = C(3,j).i - q__2.i;
	    C(3,j).r = q__1.r, C(3,j).i = q__1.i;
	    i__2 = j * c_dim1 + 4;
	    i__3 = j * c_dim1 + 4;
	    q__2.r = sum.r * t4.r - sum.i * t4.i, q__2.i = sum.r * t4.i + 
		    sum.i * t4.r;
	    q__1.r = C(4,j).r - q__2.r, q__1.i = C(4,j).i - q__2.i;
	    C(4,j).r = q__1.r, C(4,j).i = q__1.i;
	    i__2 = j * c_dim1 + 5;
	    i__3 = j * c_dim1 + 5;
	    q__2.r = sum.r * t5.r - sum.i * t5.i, q__2.i = sum.r * t5.i + 
		    sum.i * t5.r;
	    q__1.r = C(5,j).r - q__2.r, q__1.i = C(5,j).i - q__2.i;
	    C(5,j).r = q__1.r, C(5,j).i = q__1.i;
	    i__2 = j * c_dim1 + 6;
	    i__3 = j * c_dim1 + 6;
	    q__2.r = sum.r * t6.r - sum.i * t6.i, q__2.i = sum.r * t6.i + 
		    sum.i * t6.r;
	    q__1.r = C(6,j).r - q__2.r, q__1.i = C(6,j).i - q__2.i;
	    C(6,j).r = q__1.r, C(6,j).i = q__1.i;
	    i__2 = j * c_dim1 + 7;
	    i__3 = j * c_dim1 + 7;
	    q__2.r = sum.r * t7.r - sum.i * t7.i, q__2.i = sum.r * t7.i + 
		    sum.i * t7.r;
	    q__1.r = C(7,j).r - q__2.r, q__1.i = C(7,j).i - q__2.i;
	    C(7,j).r = q__1.r, C(7,j).i = q__1.i;
	    i__2 = j * c_dim1 + 8;
	    i__3 = j * c_dim1 + 8;
	    q__2.r = sum.r * t8.r - sum.i * t8.i, q__2.i = sum.r * t8.i + 
		    sum.i * t8.r;
	    q__1.r = C(8,j).r - q__2.r, q__1.i = C(8,j).i - q__2.i;
	    C(8,j).r = q__1.r, C(8,j).i = q__1.i;

	}
	goto L410;
L170:

/*        Special code for 9 x 9 Householder */

	r_cnjg(&q__1, &V(1));
	v1.r = q__1.r, v1.i = q__1.i;
	r_cnjg(&q__2, &v1);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t1.r = q__1.r, t1.i = q__1.i;
	r_cnjg(&q__1, &V(2));
	v2.r = q__1.r, v2.i = q__1.i;
	r_cnjg(&q__2, &v2);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t2.r = q__1.r, t2.i = q__1.i;
	r_cnjg(&q__1, &V(3));
	v3.r = q__1.r, v3.i = q__1.i;
	r_cnjg(&q__2, &v3);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t3.r = q__1.r, t3.i = q__1.i;
	r_cnjg(&q__1, &V(4));
	v4.r = q__1.r, v4.i = q__1.i;
	r_cnjg(&q__2, &v4);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t4.r = q__1.r, t4.i = q__1.i;
	r_cnjg(&q__1, &V(5));
	v5.r = q__1.r, v5.i = q__1.i;
	r_cnjg(&q__2, &v5);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t5.r = q__1.r, t5.i = q__1.i;
	r_cnjg(&q__1, &V(6));
	v6.r = q__1.r, v6.i = q__1.i;
	r_cnjg(&q__2, &v6);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t6.r = q__1.r, t6.i = q__1.i;
	r_cnjg(&q__1, &V(7));
	v7.r = q__1.r, v7.i = q__1.i;
	r_cnjg(&q__2, &v7);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t7.r = q__1.r, t7.i = q__1.i;
	r_cnjg(&q__1, &V(8));
	v8.r = q__1.r, v8.i = q__1.i;
	r_cnjg(&q__2, &v8);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t8.r = q__1.r, t8.i = q__1.i;
	r_cnjg(&q__1, &V(9));
	v9.r = q__1.r, v9.i = q__1.i;
	r_cnjg(&q__2, &v9);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t9.r = q__1.r, t9.i = q__1.i;
	i__1 = n__;
	for (j = 1; j <= n__; ++j) {
	    i__2 = j * c_dim1 + 1;
	    q__9.r = v1.r * C(1,j).r - v1.i * C(1,j).i, q__9.i = v1.r * C(1,j).i + v1.i * C(1,j).r;
	    i__3 = j * c_dim1 + 2;
	    q__10.r = v2.r * C(2,j).r - v2.i * C(2,j).i, q__10.i = v2.r * C(2,j).i + v2.i * C(2,j).r;
	    q__8.r = q__9.r + q__10.r, q__8.i = q__9.i + q__10.i;
	    i__4 = j * c_dim1 + 3;
	    q__11.r = v3.r * C(3,j).r - v3.i * C(3,j).i, q__11.i = v3.r * C(3,j).i + v3.i * C(3,j).r;
	    q__7.r = q__8.r + q__11.r, q__7.i = q__8.i + q__11.i;
	    i__5 = j * c_dim1 + 4;
	    q__12.r = v4.r * C(4,j).r - v4.i * C(4,j).i, q__12.i = v4.r * C(4,j).i + v4.i * C(4,j).r;
	    q__6.r = q__7.r + q__12.r, q__6.i = q__7.i + q__12.i;
	    i__6 = j * c_dim1 + 5;
	    q__13.r = v5.r * C(5,j).r - v5.i * C(5,j).i, q__13.i = v5.r * C(5,j).i + v5.i * C(5,j).r;
	    q__5.r = q__6.r + q__13.r, q__5.i = q__6.i + q__13.i;
	    i__7 = j * c_dim1 + 6;
	    q__14.r = v6.r * C(6,j).r - v6.i * C(6,j).i, q__14.i = v6.r * C(6,j).i + v6.i * C(6,j).r;
	    q__4.r = q__5.r + q__14.r, q__4.i = q__5.i + q__14.i;
	    i__8 = j * c_dim1 + 7;
	    q__15.r = v7.r * C(7,j).r - v7.i * C(7,j).i, q__15.i = v7.r * C(7,j).i + v7.i * C(7,j).r;
	    q__3.r = q__4.r + q__15.r, q__3.i = q__4.i + q__15.i;
	    i__9 = j * c_dim1 + 8;
	    q__16.r = v8.r * C(8,j).r - v8.i * C(8,j).i, q__16.i = v8.r * C(8,j).i + v8.i * C(8,j).r;
	    q__2.r = q__3.r + q__16.r, q__2.i = q__3.i + q__16.i;
	    i__10 = j * c_dim1 + 9;
	    q__17.r = v9.r * C(9,j).r - v9.i * C(9,j).i, q__17.i = v9.r * 
		    C(9,j).i + v9.i * C(9,j).r;
	    q__1.r = q__2.r + q__17.r, q__1.i = q__2.i + q__17.i;
	    sum.r = q__1.r, sum.i = q__1.i;
	    i__2 = j * c_dim1 + 1;
	    i__3 = j * c_dim1 + 1;
	    q__2.r = sum.r * t1.r - sum.i * t1.i, q__2.i = sum.r * t1.i + 
		    sum.i * t1.r;
	    q__1.r = C(1,j).r - q__2.r, q__1.i = C(1,j).i - q__2.i;
	    C(1,j).r = q__1.r, C(1,j).i = q__1.i;
	    i__2 = j * c_dim1 + 2;
	    i__3 = j * c_dim1 + 2;
	    q__2.r = sum.r * t2.r - sum.i * t2.i, q__2.i = sum.r * t2.i + 
		    sum.i * t2.r;
	    q__1.r = C(2,j).r - q__2.r, q__1.i = C(2,j).i - q__2.i;
	    C(2,j).r = q__1.r, C(2,j).i = q__1.i;
	    i__2 = j * c_dim1 + 3;
	    i__3 = j * c_dim1 + 3;
	    q__2.r = sum.r * t3.r - sum.i * t3.i, q__2.i = sum.r * t3.i + 
		    sum.i * t3.r;
	    q__1.r = C(3,j).r - q__2.r, q__1.i = C(3,j).i - q__2.i;
	    C(3,j).r = q__1.r, C(3,j).i = q__1.i;
	    i__2 = j * c_dim1 + 4;
	    i__3 = j * c_dim1 + 4;
	    q__2.r = sum.r * t4.r - sum.i * t4.i, q__2.i = sum.r * t4.i + 
		    sum.i * t4.r;
	    q__1.r = C(4,j).r - q__2.r, q__1.i = C(4,j).i - q__2.i;
	    C(4,j).r = q__1.r, C(4,j).i = q__1.i;
	    i__2 = j * c_dim1 + 5;
	    i__3 = j * c_dim1 + 5;
	    q__2.r = sum.r * t5.r - sum.i * t5.i, q__2.i = sum.r * t5.i + 
		    sum.i * t5.r;
	    q__1.r = C(5,j).r - q__2.r, q__1.i = C(5,j).i - q__2.i;
	    C(5,j).r = q__1.r, C(5,j).i = q__1.i;
	    i__2 = j * c_dim1 + 6;
	    i__3 = j * c_dim1 + 6;
	    q__2.r = sum.r * t6.r - sum.i * t6.i, q__2.i = sum.r * t6.i + 
		    sum.i * t6.r;
	    q__1.r = C(6,j).r - q__2.r, q__1.i = C(6,j).i - q__2.i;
	    C(6,j).r = q__1.r, C(6,j).i = q__1.i;
	    i__2 = j * c_dim1 + 7;
	    i__3 = j * c_dim1 + 7;
	    q__2.r = sum.r * t7.r - sum.i * t7.i, q__2.i = sum.r * t7.i + 
		    sum.i * t7.r;
	    q__1.r = C(7,j).r - q__2.r, q__1.i = C(7,j).i - q__2.i;
	    C(7,j).r = q__1.r, C(7,j).i = q__1.i;
	    i__2 = j * c_dim1 + 8;
	    i__3 = j * c_dim1 + 8;
	    q__2.r = sum.r * t8.r - sum.i * t8.i, q__2.i = sum.r * t8.i + 
		    sum.i * t8.r;
	    q__1.r = C(8,j).r - q__2.r, q__1.i = C(8,j).i - q__2.i;
	    C(8,j).r = q__1.r, C(8,j).i = q__1.i;
	    i__2 = j * c_dim1 + 9;
	    i__3 = j * c_dim1 + 9;
	    q__2.r = sum.r * t9.r - sum.i * t9.i, q__2.i = sum.r * t9.i + 
		    sum.i * t9.r;
	    q__1.r = C(9,j).r - q__2.r, q__1.i = C(9,j).i - q__2.i;
	    C(9,j).r = q__1.r, C(9,j).i = q__1.i;

	}
	goto L410;
L190:

/*        Special code for 10 x 10 Householder */

	r_cnjg(&q__1, &V(1));
	v1.r = q__1.r, v1.i = q__1.i;
	r_cnjg(&q__2, &v1);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t1.r = q__1.r, t1.i = q__1.i;
	r_cnjg(&q__1, &V(2));
	v2.r = q__1.r, v2.i = q__1.i;
	r_cnjg(&q__2, &v2);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t2.r = q__1.r, t2.i = q__1.i;
	r_cnjg(&q__1, &V(3));
	v3.r = q__1.r, v3.i = q__1.i;
	r_cnjg(&q__2, &v3);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t3.r = q__1.r, t3.i = q__1.i;
	r_cnjg(&q__1, &V(4));
	v4.r = q__1.r, v4.i = q__1.i;
	r_cnjg(&q__2, &v4);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t4.r = q__1.r, t4.i = q__1.i;
	r_cnjg(&q__1, &V(5));
	v5.r = q__1.r, v5.i = q__1.i;
	r_cnjg(&q__2, &v5);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t5.r = q__1.r, t5.i = q__1.i;
	r_cnjg(&q__1, &V(6));
	v6.r = q__1.r, v6.i = q__1.i;
	r_cnjg(&q__2, &v6);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t6.r = q__1.r, t6.i = q__1.i;
	r_cnjg(&q__1, &V(7));
	v7.r = q__1.r, v7.i = q__1.i;
	r_cnjg(&q__2, &v7);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t7.r = q__1.r, t7.i = q__1.i;
	r_cnjg(&q__1, &V(8));
	v8.r = q__1.r, v8.i = q__1.i;
	r_cnjg(&q__2, &v8);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t8.r = q__1.r, t8.i = q__1.i;
	r_cnjg(&q__1, &V(9));
	v9.r = q__1.r, v9.i = q__1.i;
	r_cnjg(&q__2, &v9);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t9.r = q__1.r, t9.i = q__1.i;
	r_cnjg(&q__1, &V(10));
	v10.r = q__1.r, v10.i = q__1.i;
	r_cnjg(&q__2, &v10);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t10.r = q__1.r, t10.i = q__1.i;
	i__1 = n__;
	for (j = 1; j <= n__; ++j) {
	    i__2 = j * c_dim1 + 1;
	    q__10.r = v1.r * C(1,j).r - v1.i * C(1,j).i, q__10.i = v1.r * C(1,j).i + v1.i * C(1,j).r;
	    i__3 = j * c_dim1 + 2;
	    q__11.r = v2.r * C(2,j).r - v2.i * C(2,j).i, q__11.i = v2.r * C(2,j).i + v2.i * C(2,j).r;
	    q__9.r = q__10.r + q__11.r, q__9.i = q__10.i + q__11.i;
	    i__4 = j * c_dim1 + 3;
	    q__12.r = v3.r * C(3,j).r - v3.i * C(3,j).i, q__12.i = v3.r * C(3,j).i + v3.i * C(3,j).r;
	    q__8.r = q__9.r + q__12.r, q__8.i = q__9.i + q__12.i;
	    i__5 = j * c_dim1 + 4;
	    q__13.r = v4.r * C(4,j).r - v4.i * C(4,j).i, q__13.i = v4.r * C(4,j).i + v4.i * C(4,j).r;
	    q__7.r = q__8.r + q__13.r, q__7.i = q__8.i + q__13.i;
	    i__6 = j * c_dim1 + 5;
	    q__14.r = v5.r * C(5,j).r - v5.i * C(5,j).i, q__14.i = v5.r * C(5,j).i + v5.i * C(5,j).r;
	    q__6.r = q__7.r + q__14.r, q__6.i = q__7.i + q__14.i;
	    i__7 = j * c_dim1 + 6;
	    q__15.r = v6.r * C(6,j).r - v6.i * C(6,j).i, q__15.i = v6.r * C(6,j).i + v6.i * C(6,j).r;
	    q__5.r = q__6.r + q__15.r, q__5.i = q__6.i + q__15.i;
	    i__8 = j * c_dim1 + 7;
	    q__16.r = v7.r * C(7,j).r - v7.i * C(7,j).i, q__16.i = v7.r * C(7,j).i + v7.i * C(7,j).r;
	    q__4.r = q__5.r + q__16.r, q__4.i = q__5.i + q__16.i;
	    i__9 = j * c_dim1 + 8;
	    q__17.r = v8.r * C(8,j).r - v8.i * C(8,j).i, q__17.i = v8.r * C(8,j).i + v8.i * C(8,j).r;
	    q__3.r = q__4.r + q__17.r, q__3.i = q__4.i + q__17.i;
	    i__10 = j * c_dim1 + 9;
	    q__18.r = v9.r * C(9,j).r - v9.i * C(9,j).i, q__18.i = v9.r * 
		    C(9,j).i + v9.i * C(9,j).r;
	    q__2.r = q__3.r + q__18.r, q__2.i = q__3.i + q__18.i;
	    i__11 = j * c_dim1 + 10;
	    q__19.r = v10.r * C(10,j).r - v10.i * C(10,j).i, q__19.i = 
		    v10.r * C(10,j).i + v10.i * C(10,j).r;
	    q__1.r = q__2.r + q__19.r, q__1.i = q__2.i + q__19.i;
	    sum.r = q__1.r, sum.i = q__1.i;
	    i__2 = j * c_dim1 + 1;
	    i__3 = j * c_dim1 + 1;
	    q__2.r = sum.r * t1.r - sum.i * t1.i, q__2.i = sum.r * t1.i + 
		    sum.i * t1.r;
	    q__1.r = C(1,j).r - q__2.r, q__1.i = C(1,j).i - q__2.i;
	    C(1,j).r = q__1.r, C(1,j).i = q__1.i;
	    i__2 = j * c_dim1 + 2;
	    i__3 = j * c_dim1 + 2;
	    q__2.r = sum.r * t2.r - sum.i * t2.i, q__2.i = sum.r * t2.i + 
		    sum.i * t2.r;
	    q__1.r = C(2,j).r - q__2.r, q__1.i = C(2,j).i - q__2.i;
	    C(2,j).r = q__1.r, C(2,j).i = q__1.i;
	    i__2 = j * c_dim1 + 3;
	    i__3 = j * c_dim1 + 3;
	    q__2.r = sum.r * t3.r - sum.i * t3.i, q__2.i = sum.r * t3.i + 
		    sum.i * t3.r;
	    q__1.r = C(3,j).r - q__2.r, q__1.i = C(3,j).i - q__2.i;
	    C(3,j).r = q__1.r, C(3,j).i = q__1.i;
	    i__2 = j * c_dim1 + 4;
	    i__3 = j * c_dim1 + 4;
	    q__2.r = sum.r * t4.r - sum.i * t4.i, q__2.i = sum.r * t4.i + 
		    sum.i * t4.r;
	    q__1.r = C(4,j).r - q__2.r, q__1.i = C(4,j).i - q__2.i;
	    C(4,j).r = q__1.r, C(4,j).i = q__1.i;
	    i__2 = j * c_dim1 + 5;
	    i__3 = j * c_dim1 + 5;
	    q__2.r = sum.r * t5.r - sum.i * t5.i, q__2.i = sum.r * t5.i + 
		    sum.i * t5.r;
	    q__1.r = C(5,j).r - q__2.r, q__1.i = C(5,j).i - q__2.i;
	    C(5,j).r = q__1.r, C(5,j).i = q__1.i;
	    i__2 = j * c_dim1 + 6;
	    i__3 = j * c_dim1 + 6;
	    q__2.r = sum.r * t6.r - sum.i * t6.i, q__2.i = sum.r * t6.i + 
		    sum.i * t6.r;
	    q__1.r = C(6,j).r - q__2.r, q__1.i = C(6,j).i - q__2.i;
	    C(6,j).r = q__1.r, C(6,j).i = q__1.i;
	    i__2 = j * c_dim1 + 7;
	    i__3 = j * c_dim1 + 7;
	    q__2.r = sum.r * t7.r - sum.i * t7.i, q__2.i = sum.r * t7.i + 
		    sum.i * t7.r;
	    q__1.r = C(7,j).r - q__2.r, q__1.i = C(7,j).i - q__2.i;
	    C(7,j).r = q__1.r, C(7,j).i = q__1.i;
	    i__2 = j * c_dim1 + 8;
	    i__3 = j * c_dim1 + 8;
	    q__2.r = sum.r * t8.r - sum.i * t8.i, q__2.i = sum.r * t8.i + 
		    sum.i * t8.r;
	    q__1.r = C(8,j).r - q__2.r, q__1.i = C(8,j).i - q__2.i;
	    C(8,j).r = q__1.r, C(8,j).i = q__1.i;
	    i__2 = j * c_dim1 + 9;
	    i__3 = j * c_dim1 + 9;
	    q__2.r = sum.r * t9.r - sum.i * t9.i, q__2.i = sum.r * t9.i + 
		    sum.i * t9.r;
	    q__1.r = C(9,j).r - q__2.r, q__1.i = C(9,j).i - q__2.i;
	    C(9,j).r = q__1.r, C(9,j).i = q__1.i;
	    i__2 = j * c_dim1 + 10;
	    i__3 = j * c_dim1 + 10;
	    q__2.r = sum.r * t10.r - sum.i * t10.i, q__2.i = sum.r * t10.i + 
		    sum.i * t10.r;
	    q__1.r = C(10,j).r - q__2.r, q__1.i = C(10,j).i - q__2.i;
	    C(10,j).r = q__1.r, C(10,j).i = q__1.i;

	}
	goto L410;
    } else {

/*        Form  C * H, where H has order n. */

	switch (n__) {
	    case 1:  goto L210;
	    case 2:  goto L230;
	    case 3:  goto L250;
	    case 4:  goto L270;
	    case 5:  goto L290;
	    case 6:  goto L310;
	    case 7:  goto L330;
	    case 8:  goto L350;
	    case 9:  goto L370;
	    case 10:  goto L390;
	}

/*        Code for general N   

          w := C * v */

	cgemv("No transpose", m__, n__, c_b2, &C(1,1), ldc, &V(1), c__1, 
		c_b1, &WORK(1), c__1);

/*        C := C - tau * w * v' */

	q__1.r = -(double)tau.r, q__1.i = -(double)tau.i;
	cgerc(m__, n__, q__1, &WORK(1), c__1, &V(1), c__1, &C(1,1), ldc);
	goto L410;
L210:

/*        Special code for 1 x 1 Householder */

	q__3.r = tau.r * V(1).r - tau.i * V(1).i, q__3.i = tau.r * V(1).i 
		+ tau.i * V(1).r;
	r_cnjg(&q__4, &V(1));
	q__2.r = q__3.r * q__4.r - q__3.i * q__4.i, q__2.i = q__3.r * q__4.i 
		+ q__3.i * q__4.r;
	q__1.r = 1.f - q__2.r, q__1.i = 0.f - q__2.i;
	t1.r = q__1.r, t1.i = q__1.i;
	i__1 = m__;
	for (j = 1; j <= m__; ++j) {
	    i__2 = j + c_dim1;
	    i__3 = j + c_dim1;
	    q__1.r = t1.r * C(j,1).r - t1.i * C(j,1).i, q__1.i = t1.r * C(j,1).i + t1.i * C(j,1).r;
	    C(j,1).r = q__1.r, C(j,1).i = q__1.i;

	}
	goto L410;
L230:

/*        Special code for 2 x 2 Householder */

	v1.r = V(1).r, v1.i = V(1).i;
	r_cnjg(&q__2, &v1);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t1.r = q__1.r, t1.i = q__1.i;
	v2.r = V(2).r, v2.i = V(2).i;
	r_cnjg(&q__2, &v2);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t2.r = q__1.r, t2.i = q__1.i;
	i__1 = m__;
	for (j = 1; j <= m__; ++j) {
	    i__2 = j + c_dim1;
	    q__2.r = v1.r * C(j,1).r - v1.i * C(j,1).i, q__2.i = v1.r * C(j,1).i + v1.i * C(j,1).r;
	    i__3 = j + (c_dim1 << 1);
	    q__3.r = v2.r * C(j,2).r - v2.i * C(j,2).i, q__3.i = v2.r * C(j,2).i + v2.i * C(j,2).r;
	    q__1.r = q__2.r + q__3.r, q__1.i = q__2.i + q__3.i;
	    sum.r = q__1.r, sum.i = q__1.i;
	    i__2 = j + c_dim1;
	    i__3 = j + c_dim1;
	    q__2.r = sum.r * t1.r - sum.i * t1.i, q__2.i = sum.r * t1.i + 
		    sum.i * t1.r;
	    q__1.r = C(j,1).r - q__2.r, q__1.i = C(j,1).i - q__2.i;
	    C(j,1).r = q__1.r, C(j,1).i = q__1.i;
	    i__2 = j + (c_dim1 << 1);
	    i__3 = j + (c_dim1 << 1);
	    q__2.r = sum.r * t2.r - sum.i * t2.i, q__2.i = sum.r * t2.i + 
		    sum.i * t2.r;
	    q__1.r = C(j,2).r - q__2.r, q__1.i = C(j,2).i - q__2.i;
	    C(j,2).r = q__1.r, C(j,2).i = q__1.i;

	}
	goto L410;
L250:

/*        Special code for 3 x 3 Householder */

	v1.r = V(1).r, v1.i = V(1).i;
	r_cnjg(&q__2, &v1);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t1.r = q__1.r, t1.i = q__1.i;
	v2.r = V(2).r, v2.i = V(2).i;
	r_cnjg(&q__2, &v2);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t2.r = q__1.r, t2.i = q__1.i;
	v3.r = V(3).r, v3.i = V(3).i;
	r_cnjg(&q__2, &v3);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t3.r = q__1.r, t3.i = q__1.i;
	i__1 = m__;
	for (j = 1; j <= m__; ++j) {
	    i__2 = j + c_dim1;
	    q__3.r = v1.r * C(j,1).r - v1.i * C(j,1).i, q__3.i = v1.r * C(j,1).i + v1.i * C(j,1).r;
	    i__3 = j + (c_dim1 << 1);
	    q__4.r = v2.r * C(j,2).r - v2.i * C(j,2).i, q__4.i = v2.r * C(j,2).i + v2.i * C(j,2).r;
	    q__2.r = q__3.r + q__4.r, q__2.i = q__3.i + q__4.i;
	    i__4 = j + c_dim1 * 3;
	    q__5.r = v3.r * C(j,3).r - v3.i * C(j,3).i, q__5.i = v3.r * C(j,3).i + v3.i * C(j,3).r;
	    q__1.r = q__2.r + q__5.r, q__1.i = q__2.i + q__5.i;
	    sum.r = q__1.r, sum.i = q__1.i;
	    i__2 = j + c_dim1;
	    i__3 = j + c_dim1;
	    q__2.r = sum.r * t1.r - sum.i * t1.i, q__2.i = sum.r * t1.i + 
		    sum.i * t1.r;
	    q__1.r = C(j,1).r - q__2.r, q__1.i = C(j,1).i - q__2.i;
	    C(j,1).r = q__1.r, C(j,1).i = q__1.i;
	    i__2 = j + (c_dim1 << 1);
	    i__3 = j + (c_dim1 << 1);
	    q__2.r = sum.r * t2.r - sum.i * t2.i, q__2.i = sum.r * t2.i + 
		    sum.i * t2.r;
	    q__1.r = C(j,2).r - q__2.r, q__1.i = C(j,2).i - q__2.i;
	    C(j,2).r = q__1.r, C(j,2).i = q__1.i;
	    i__2 = j + c_dim1 * 3;
	    i__3 = j + c_dim1 * 3;
	    q__2.r = sum.r * t3.r - sum.i * t3.i, q__2.i = sum.r * t3.i + 
		    sum.i * t3.r;
	    q__1.r = C(j,3).r - q__2.r, q__1.i = C(j,3).i - q__2.i;
	    C(j,3).r = q__1.r, C(j,3).i = q__1.i;

	}
	goto L410;
L270:

/*        Special code for 4 x 4 Householder */

	v1.r = V(1).r, v1.i = V(1).i;
	r_cnjg(&q__2, &v1);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t1.r = q__1.r, t1.i = q__1.i;
	v2.r = V(2).r, v2.i = V(2).i;
	r_cnjg(&q__2, &v2);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t2.r = q__1.r, t2.i = q__1.i;
	v3.r = V(3).r, v3.i = V(3).i;
	r_cnjg(&q__2, &v3);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t3.r = q__1.r, t3.i = q__1.i;
	v4.r = V(4).r, v4.i = V(4).i;
	r_cnjg(&q__2, &v4);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t4.r = q__1.r, t4.i = q__1.i;
	i__1 = m__;
	for (j = 1; j <= m__; ++j) {
	    i__2 = j + c_dim1;
	    q__4.r = v1.r * C(j,1).r - v1.i * C(j,1).i, q__4.i = v1.r * C(j,1).i + v1.i * C(j,1).r;
	    i__3 = j + (c_dim1 << 1);
	    q__5.r = v2.r * C(j,2).r - v2.i * C(j,2).i, q__5.i = v2.r * C(j,2).i + v2.i * C(j,2).r;
	    q__3.r = q__4.r + q__5.r, q__3.i = q__4.i + q__5.i;
	    i__4 = j + c_dim1 * 3;
	    q__6.r = v3.r * C(j,3).r - v3.i * C(j,3).i, q__6.i = v3.r * C(j,3).i + v3.i * C(j,3).r;
	    q__2.r = q__3.r + q__6.r, q__2.i = q__3.i + q__6.i;
	    i__5 = j + (c_dim1 << 2);
	    q__7.r = v4.r * C(j,4).r - v4.i * C(j,4).i, q__7.i = v4.r * C(j,4).i + v4.i * C(j,4).r;
	    q__1.r = q__2.r + q__7.r, q__1.i = q__2.i + q__7.i;
	    sum.r = q__1.r, sum.i = q__1.i;
	    i__2 = j + c_dim1;
	    i__3 = j + c_dim1;
	    q__2.r = sum.r * t1.r - sum.i * t1.i, q__2.i = sum.r * t1.i + 
		    sum.i * t1.r;
	    q__1.r = C(j,1).r - q__2.r, q__1.i = C(j,1).i - q__2.i;
	    C(j,1).r = q__1.r, C(j,1).i = q__1.i;
	    i__2 = j + (c_dim1 << 1);
	    i__3 = j + (c_dim1 << 1);
	    q__2.r = sum.r * t2.r - sum.i * t2.i, q__2.i = sum.r * t2.i + 
		    sum.i * t2.r;
	    q__1.r = C(j,2).r - q__2.r, q__1.i = C(j,2).i - q__2.i;
	    C(j,2).r = q__1.r, C(j,2).i = q__1.i;
	    i__2 = j + c_dim1 * 3;
	    i__3 = j + c_dim1 * 3;
	    q__2.r = sum.r * t3.r - sum.i * t3.i, q__2.i = sum.r * t3.i + 
		    sum.i * t3.r;
	    q__1.r = C(j,3).r - q__2.r, q__1.i = C(j,3).i - q__2.i;
	    C(j,3).r = q__1.r, C(j,3).i = q__1.i;
	    i__2 = j + (c_dim1 << 2);
	    i__3 = j + (c_dim1 << 2);
	    q__2.r = sum.r * t4.r - sum.i * t4.i, q__2.i = sum.r * t4.i + 
		    sum.i * t4.r;
	    q__1.r = C(j,4).r - q__2.r, q__1.i = C(j,4).i - q__2.i;
	    C(j,4).r = q__1.r, C(j,4).i = q__1.i;

	}
	goto L410;
L290:

/*        Special code for 5 x 5 Householder */

	v1.r = V(1).r, v1.i = V(1).i;
	r_cnjg(&q__2, &v1);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t1.r = q__1.r, t1.i = q__1.i;
	v2.r = V(2).r, v2.i = V(2).i;
	r_cnjg(&q__2, &v2);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t2.r = q__1.r, t2.i = q__1.i;
	v3.r = V(3).r, v3.i = V(3).i;
	r_cnjg(&q__2, &v3);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t3.r = q__1.r, t3.i = q__1.i;
	v4.r = V(4).r, v4.i = V(4).i;
	r_cnjg(&q__2, &v4);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t4.r = q__1.r, t4.i = q__1.i;
	v5.r = V(5).r, v5.i = V(5).i;
	r_cnjg(&q__2, &v5);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t5.r = q__1.r, t5.i = q__1.i;
	i__1 = m__;
	for (j = 1; j <= m__; ++j) {
	    i__2 = j + c_dim1;
	    q__5.r = v1.r * C(j,1).r - v1.i * C(j,1).i, q__5.i = v1.r * C(j,1).i + v1.i * C(j,1).r;
	    i__3 = j + (c_dim1 << 1);
	    q__6.r = v2.r * C(j,2).r - v2.i * C(j,2).i, q__6.i = v2.r * C(j,2).i + v2.i * C(j,2).r;
	    q__4.r = q__5.r + q__6.r, q__4.i = q__5.i + q__6.i;
	    i__4 = j + c_dim1 * 3;
	    q__7.r = v3.r * C(j,3).r - v3.i * C(j,3).i, q__7.i = v3.r * C(j,3).i + v3.i * C(j,3).r;
	    q__3.r = q__4.r + q__7.r, q__3.i = q__4.i + q__7.i;
	    i__5 = j + (c_dim1 << 2);
	    q__8.r = v4.r * C(j,4).r - v4.i * C(j,4).i, q__8.i = v4.r * C(j,4).i + v4.i * C(j,4).r;
	    q__2.r = q__3.r + q__8.r, q__2.i = q__3.i + q__8.i;
	    i__6 = j + c_dim1 * 5;
	    q__9.r = v5.r * C(j,5).r - v5.i * C(j,5).i, q__9.i = v5.r * C(j,5).i + v5.i * C(j,5).r;
	    q__1.r = q__2.r + q__9.r, q__1.i = q__2.i + q__9.i;
	    sum.r = q__1.r, sum.i = q__1.i;
	    i__2 = j + c_dim1;
	    i__3 = j + c_dim1;
	    q__2.r = sum.r * t1.r - sum.i * t1.i, q__2.i = sum.r * t1.i + 
		    sum.i * t1.r;
	    q__1.r = C(j,1).r - q__2.r, q__1.i = C(j,1).i - q__2.i;
	    C(j,1).r = q__1.r, C(j,1).i = q__1.i;
	    i__2 = j + (c_dim1 << 1);
	    i__3 = j + (c_dim1 << 1);
	    q__2.r = sum.r * t2.r - sum.i * t2.i, q__2.i = sum.r * t2.i + 
		    sum.i * t2.r;
	    q__1.r = C(j,2).r - q__2.r, q__1.i = C(j,2).i - q__2.i;
	    C(j,2).r = q__1.r, C(j,2).i = q__1.i;
	    i__2 = j + c_dim1 * 3;
	    i__3 = j + c_dim1 * 3;
	    q__2.r = sum.r * t3.r - sum.i * t3.i, q__2.i = sum.r * t3.i + 
		    sum.i * t3.r;
	    q__1.r = C(j,3).r - q__2.r, q__1.i = C(j,3).i - q__2.i;
	    C(j,3).r = q__1.r, C(j,3).i = q__1.i;
	    i__2 = j + (c_dim1 << 2);
	    i__3 = j + (c_dim1 << 2);
	    q__2.r = sum.r * t4.r - sum.i * t4.i, q__2.i = sum.r * t4.i + 
		    sum.i * t4.r;
	    q__1.r = C(j,4).r - q__2.r, q__1.i = C(j,4).i - q__2.i;
	    C(j,4).r = q__1.r, C(j,4).i = q__1.i;
	    i__2 = j + c_dim1 * 5;
	    i__3 = j + c_dim1 * 5;
	    q__2.r = sum.r * t5.r - sum.i * t5.i, q__2.i = sum.r * t5.i + 
		    sum.i * t5.r;
	    q__1.r = C(j,5).r - q__2.r, q__1.i = C(j,5).i - q__2.i;
	    C(j,5).r = q__1.r, C(j,5).i = q__1.i;

	}
	goto L410;
L310:

/*        Special code for 6 x 6 Householder */

	v1.r = V(1).r, v1.i = V(1).i;
	r_cnjg(&q__2, &v1);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t1.r = q__1.r, t1.i = q__1.i;
	v2.r = V(2).r, v2.i = V(2).i;
	r_cnjg(&q__2, &v2);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t2.r = q__1.r, t2.i = q__1.i;
	v3.r = V(3).r, v3.i = V(3).i;
	r_cnjg(&q__2, &v3);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t3.r = q__1.r, t3.i = q__1.i;
	v4.r = V(4).r, v4.i = V(4).i;
	r_cnjg(&q__2, &v4);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t4.r = q__1.r, t4.i = q__1.i;
	v5.r = V(5).r, v5.i = V(5).i;
	r_cnjg(&q__2, &v5);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t5.r = q__1.r, t5.i = q__1.i;
	v6.r = V(6).r, v6.i = V(6).i;
	r_cnjg(&q__2, &v6);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t6.r = q__1.r, t6.i = q__1.i;
	i__1 = m__;
	for (j = 1; j <= m__; ++j) {
	    i__2 = j + c_dim1;
	    q__6.r = v1.r * C(j,1).r - v1.i * C(j,1).i, q__6.i = v1.r * C(j,1).i + v1.i * C(j,1).r;
	    i__3 = j + (c_dim1 << 1);
	    q__7.r = v2.r * C(j,2).r - v2.i * C(j,2).i, q__7.i = v2.r * C(j,2).i + v2.i * C(j,2).r;
	    q__5.r = q__6.r + q__7.r, q__5.i = q__6.i + q__7.i;
	    i__4 = j + c_dim1 * 3;
	    q__8.r = v3.r * C(j,3).r - v3.i * C(j,3).i, q__8.i = v3.r * C(j,3).i + v3.i * C(j,3).r;
	    q__4.r = q__5.r + q__8.r, q__4.i = q__5.i + q__8.i;
	    i__5 = j + (c_dim1 << 2);
	    q__9.r = v4.r * C(j,4).r - v4.i * C(j,4).i, q__9.i = v4.r * C(j,4).i + v4.i * C(j,4).r;
	    q__3.r = q__4.r + q__9.r, q__3.i = q__4.i + q__9.i;
	    i__6 = j + c_dim1 * 5;
	    q__10.r = v5.r * C(j,5).r - v5.i * C(j,5).i, q__10.i = v5.r * C(j,5).i + v5.i * C(j,5).r;
	    q__2.r = q__3.r + q__10.r, q__2.i = q__3.i + q__10.i;
	    i__7 = j + c_dim1 * 6;
	    q__11.r = v6.r * C(j,6).r - v6.i * C(j,6).i, q__11.i = v6.r * C(j,6).i + v6.i * C(j,6).r;
	    q__1.r = q__2.r + q__11.r, q__1.i = q__2.i + q__11.i;
	    sum.r = q__1.r, sum.i = q__1.i;
	    i__2 = j + c_dim1;
	    i__3 = j + c_dim1;
	    q__2.r = sum.r * t1.r - sum.i * t1.i, q__2.i = sum.r * t1.i + 
		    sum.i * t1.r;
	    q__1.r = C(j,1).r - q__2.r, q__1.i = C(j,1).i - q__2.i;
	    C(j,1).r = q__1.r, C(j,1).i = q__1.i;
	    i__2 = j + (c_dim1 << 1);
	    i__3 = j + (c_dim1 << 1);
	    q__2.r = sum.r * t2.r - sum.i * t2.i, q__2.i = sum.r * t2.i + 
		    sum.i * t2.r;
	    q__1.r = C(j,2).r - q__2.r, q__1.i = C(j,2).i - q__2.i;
	    C(j,2).r = q__1.r, C(j,2).i = q__1.i;
	    i__2 = j + c_dim1 * 3;
	    i__3 = j + c_dim1 * 3;
	    q__2.r = sum.r * t3.r - sum.i * t3.i, q__2.i = sum.r * t3.i + 
		    sum.i * t3.r;
	    q__1.r = C(j,3).r - q__2.r, q__1.i = C(j,3).i - q__2.i;
	    C(j,3).r = q__1.r, C(j,3).i = q__1.i;
	    i__2 = j + (c_dim1 << 2);
	    i__3 = j + (c_dim1 << 2);
	    q__2.r = sum.r * t4.r - sum.i * t4.i, q__2.i = sum.r * t4.i + 
		    sum.i * t4.r;
	    q__1.r = C(j,4).r - q__2.r, q__1.i = C(j,4).i - q__2.i;
	    C(j,4).r = q__1.r, C(j,4).i = q__1.i;
	    i__2 = j + c_dim1 * 5;
	    i__3 = j + c_dim1 * 5;
	    q__2.r = sum.r * t5.r - sum.i * t5.i, q__2.i = sum.r * t5.i + 
		    sum.i * t5.r;
	    q__1.r = C(j,5).r - q__2.r, q__1.i = C(j,5).i - q__2.i;
	    C(j,5).r = q__1.r, C(j,5).i = q__1.i;
	    i__2 = j + c_dim1 * 6;
	    i__3 = j + c_dim1 * 6;
	    q__2.r = sum.r * t6.r - sum.i * t6.i, q__2.i = sum.r * t6.i + 
		    sum.i * t6.r;
	    q__1.r = C(j,6).r - q__2.r, q__1.i = C(j,6).i - q__2.i;
	    C(j,6).r = q__1.r, C(j,6).i = q__1.i;

	}
	goto L410;
L330:

/*        Special code for 7 x 7 Householder */

	v1.r = V(1).r, v1.i = V(1).i;
	r_cnjg(&q__2, &v1);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t1.r = q__1.r, t1.i = q__1.i;
	v2.r = V(2).r, v2.i = V(2).i;
	r_cnjg(&q__2, &v2);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t2.r = q__1.r, t2.i = q__1.i;
	v3.r = V(3).r, v3.i = V(3).i;
	r_cnjg(&q__2, &v3);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t3.r = q__1.r, t3.i = q__1.i;
	v4.r = V(4).r, v4.i = V(4).i;
	r_cnjg(&q__2, &v4);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t4.r = q__1.r, t4.i = q__1.i;
	v5.r = V(5).r, v5.i = V(5).i;
	r_cnjg(&q__2, &v5);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t5.r = q__1.r, t5.i = q__1.i;
	v6.r = V(6).r, v6.i = V(6).i;
	r_cnjg(&q__2, &v6);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t6.r = q__1.r, t6.i = q__1.i;
	v7.r = V(7).r, v7.i = V(7).i;
	r_cnjg(&q__2, &v7);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t7.r = q__1.r, t7.i = q__1.i;
	i__1 = m__;
	for (j = 1; j <= m__; ++j) {
	    i__2 = j + c_dim1;
	    q__7.r = v1.r * C(j,1).r - v1.i * C(j,1).i, q__7.i = v1.r * C(j,1).i + v1.i * C(j,1).r;
	    i__3 = j + (c_dim1 << 1);
	    q__8.r = v2.r * C(j,2).r - v2.i * C(j,2).i, q__8.i = v2.r * C(j,2).i + v2.i * C(j,2).r;
	    q__6.r = q__7.r + q__8.r, q__6.i = q__7.i + q__8.i;
	    i__4 = j + c_dim1 * 3;
	    q__9.r = v3.r * C(j,3).r - v3.i * C(j,3).i, q__9.i = v3.r * C(j,3).i + v3.i * C(j,3).r;
	    q__5.r = q__6.r + q__9.r, q__5.i = q__6.i + q__9.i;
	    i__5 = j + (c_dim1 << 2);
	    q__10.r = v4.r * C(j,4).r - v4.i * C(j,4).i, q__10.i = v4.r * C(j,4).i + v4.i * C(j,4).r;
	    q__4.r = q__5.r + q__10.r, q__4.i = q__5.i + q__10.i;
	    i__6 = j + c_dim1 * 5;
	    q__11.r = v5.r * C(j,5).r - v5.i * C(j,5).i, q__11.i = v5.r * C(j,5).i + v5.i * C(j,5).r;
	    q__3.r = q__4.r + q__11.r, q__3.i = q__4.i + q__11.i;
	    i__7 = j + c_dim1 * 6;
	    q__12.r = v6.r * C(j,6).r - v6.i * C(j,6).i, q__12.i = v6.r * C(j,6).i + v6.i * C(j,6).r;
	    q__2.r = q__3.r + q__12.r, q__2.i = q__3.i + q__12.i;
	    i__8 = j + c_dim1 * 7;
	    q__13.r = v7.r * C(j,7).r - v7.i * C(j,7).i, q__13.i = v7.r * C(j,7).i + v7.i * C(j,7).r;
	    q__1.r = q__2.r + q__13.r, q__1.i = q__2.i + q__13.i;
	    sum.r = q__1.r, sum.i = q__1.i;
	    i__2 = j + c_dim1;
	    i__3 = j + c_dim1;
	    q__2.r = sum.r * t1.r - sum.i * t1.i, q__2.i = sum.r * t1.i + 
		    sum.i * t1.r;
	    q__1.r = C(j,1).r - q__2.r, q__1.i = C(j,1).i - q__2.i;
	    C(j,1).r = q__1.r, C(j,1).i = q__1.i;
	    i__2 = j + (c_dim1 << 1);
	    i__3 = j + (c_dim1 << 1);
	    q__2.r = sum.r * t2.r - sum.i * t2.i, q__2.i = sum.r * t2.i + 
		    sum.i * t2.r;
	    q__1.r = C(j,2).r - q__2.r, q__1.i = C(j,2).i - q__2.i;
	    C(j,2).r = q__1.r, C(j,2).i = q__1.i;
	    i__2 = j + c_dim1 * 3;
	    i__3 = j + c_dim1 * 3;
	    q__2.r = sum.r * t3.r - sum.i * t3.i, q__2.i = sum.r * t3.i + 
		    sum.i * t3.r;
	    q__1.r = C(j,3).r - q__2.r, q__1.i = C(j,3).i - q__2.i;
	    C(j,3).r = q__1.r, C(j,3).i = q__1.i;
	    i__2 = j + (c_dim1 << 2);
	    i__3 = j + (c_dim1 << 2);
	    q__2.r = sum.r * t4.r - sum.i * t4.i, q__2.i = sum.r * t4.i + 
		    sum.i * t4.r;
	    q__1.r = C(j,4).r - q__2.r, q__1.i = C(j,4).i - q__2.i;
	    C(j,4).r = q__1.r, C(j,4).i = q__1.i;
	    i__2 = j + c_dim1 * 5;
	    i__3 = j + c_dim1 * 5;
	    q__2.r = sum.r * t5.r - sum.i * t5.i, q__2.i = sum.r * t5.i + 
		    sum.i * t5.r;
	    q__1.r = C(j,5).r - q__2.r, q__1.i = C(j,5).i - q__2.i;
	    C(j,5).r = q__1.r, C(j,5).i = q__1.i;
	    i__2 = j + c_dim1 * 6;
	    i__3 = j + c_dim1 * 6;
	    q__2.r = sum.r * t6.r - sum.i * t6.i, q__2.i = sum.r * t6.i + 
		    sum.i * t6.r;
	    q__1.r = C(j,6).r - q__2.r, q__1.i = C(j,6).i - q__2.i;
	    C(j,6).r = q__1.r, C(j,6).i = q__1.i;
	    i__2 = j + c_dim1 * 7;
	    i__3 = j + c_dim1 * 7;
	    q__2.r = sum.r * t7.r - sum.i * t7.i, q__2.i = sum.r * t7.i + 
		    sum.i * t7.r;
	    q__1.r = C(j,7).r - q__2.r, q__1.i = C(j,7).i - q__2.i;
	    C(j,7).r = q__1.r, C(j,7).i = q__1.i;
	}
	goto L410;
L350:

/*        Special code for 8 x 8 Householder */

	v1.r = V(1).r, v1.i = V(1).i;
	r_cnjg(&q__2, &v1);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t1.r = q__1.r, t1.i = q__1.i;
	v2.r = V(2).r, v2.i = V(2).i;
	r_cnjg(&q__2, &v2);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t2.r = q__1.r, t2.i = q__1.i;
	v3.r = V(3).r, v3.i = V(3).i;
	r_cnjg(&q__2, &v3);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t3.r = q__1.r, t3.i = q__1.i;
	v4.r = V(4).r, v4.i = V(4).i;
	r_cnjg(&q__2, &v4);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t4.r = q__1.r, t4.i = q__1.i;
	v5.r = V(5).r, v5.i = V(5).i;
	r_cnjg(&q__2, &v5);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t5.r = q__1.r, t5.i = q__1.i;
	v6.r = V(6).r, v6.i = V(6).i;
	r_cnjg(&q__2, &v6);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t6.r = q__1.r, t6.i = q__1.i;
	v7.r = V(7).r, v7.i = V(7).i;
	r_cnjg(&q__2, &v7);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t7.r = q__1.r, t7.i = q__1.i;
	v8.r = V(8).r, v8.i = V(8).i;
	r_cnjg(&q__2, &v8);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t8.r = q__1.r, t8.i = q__1.i;
	i__1 = m__;
	for (j = 1; j <= m__; ++j) {
	    i__2 = j + c_dim1;
	    q__8.r = v1.r * C(j,1).r - v1.i * C(j,1).i, q__8.i = v1.r * C(j,1).i + v1.i * C(j,1).r;
	    i__3 = j + (c_dim1 << 1);
	    q__9.r = v2.r * C(j,2).r - v2.i * C(j,2).i, q__9.i = v2.r * C(j,2).i + v2.i * C(j,2).r;
	    q__7.r = q__8.r + q__9.r, q__7.i = q__8.i + q__9.i;
	    i__4 = j + c_dim1 * 3;
	    q__10.r = v3.r * C(j,3).r - v3.i * C(j,3).i, q__10.i = v3.r * C(j,3).i + v3.i * C(j,3).r;
	    q__6.r = q__7.r + q__10.r, q__6.i = q__7.i + q__10.i;
	    i__5 = j + (c_dim1 << 2);
	    q__11.r = v4.r * C(j,4).r - v4.i * C(j,4).i, q__11.i = v4.r * C(j,4).i + v4.i * C(j,4).r;
	    q__5.r = q__6.r + q__11.r, q__5.i = q__6.i + q__11.i;
	    i__6 = j + c_dim1 * 5;
	    q__12.r = v5.r * C(j,5).r - v5.i * C(j,5).i, q__12.i = v5.r * C(j,5).i + v5.i * C(j,5).r;
	    q__4.r = q__5.r + q__12.r, q__4.i = q__5.i + q__12.i;
	    i__7 = j + c_dim1 * 6;
	    q__13.r = v6.r * C(j,6).r - v6.i * C(j,6).i, q__13.i = v6.r * C(j,6).i + v6.i * C(j,6).r;
	    q__3.r = q__4.r + q__13.r, q__3.i = q__4.i + q__13.i;
	    i__8 = j + c_dim1 * 7;
	    q__14.r = v7.r * C(j,7).r - v7.i * C(j,7).i, q__14.i = v7.r * C(j,7).i + v7.i * C(j,7).r;
	    q__2.r = q__3.r + q__14.r, q__2.i = q__3.i + q__14.i;
	    i__9 = j + (c_dim1 << 3);
	    q__15.r = v8.r * C(j,8).r - v8.i * C(j,8).i, q__15.i = v8.r * C(j,8).i + v8.i * C(j,8).r;
	    q__1.r = q__2.r + q__15.r, q__1.i = q__2.i + q__15.i;
	    sum.r = q__1.r, sum.i = q__1.i;
	    i__2 = j + c_dim1;
	    i__3 = j + c_dim1;
	    q__2.r = sum.r * t1.r - sum.i * t1.i, q__2.i = sum.r * t1.i + 
		    sum.i * t1.r;
	    q__1.r = C(j,1).r - q__2.r, q__1.i = C(j,1).i - q__2.i;
	    C(j,1).r = q__1.r, C(j,1).i = q__1.i;
	    i__2 = j + (c_dim1 << 1);
	    i__3 = j + (c_dim1 << 1);
	    q__2.r = sum.r * t2.r - sum.i * t2.i, q__2.i = sum.r * t2.i + 
		    sum.i * t2.r;
	    q__1.r = C(j,2).r - q__2.r, q__1.i = C(j,2).i - q__2.i;
	    C(j,2).r = q__1.r, C(j,2).i = q__1.i;
	    i__2 = j + c_dim1 * 3;
	    i__3 = j + c_dim1 * 3;
	    q__2.r = sum.r * t3.r - sum.i * t3.i, q__2.i = sum.r * t3.i + 
		    sum.i * t3.r;
	    q__1.r = C(j,3).r - q__2.r, q__1.i = C(j,3).i - q__2.i;
	    C(j,3).r = q__1.r, C(j,3).i = q__1.i;
	    i__2 = j + (c_dim1 << 2);
	    i__3 = j + (c_dim1 << 2);
	    q__2.r = sum.r * t4.r - sum.i * t4.i, q__2.i = sum.r * t4.i + 
		    sum.i * t4.r;
	    q__1.r = C(j,4).r - q__2.r, q__1.i = C(j,4).i - q__2.i;
	    C(j,4).r = q__1.r, C(j,4).i = q__1.i;
	    i__2 = j + c_dim1 * 5;
	    i__3 = j + c_dim1 * 5;
	    q__2.r = sum.r * t5.r - sum.i * t5.i, q__2.i = sum.r * t5.i + 
		    sum.i * t5.r;
	    q__1.r = C(j,5).r - q__2.r, q__1.i = C(j,5).i - q__2.i;
	    C(j,5).r = q__1.r, C(j,5).i = q__1.i;
	    i__2 = j + c_dim1 * 6;
	    i__3 = j + c_dim1 * 6;
	    q__2.r = sum.r * t6.r - sum.i * t6.i, q__2.i = sum.r * t6.i + 
		    sum.i * t6.r;
	    q__1.r = C(j,6).r - q__2.r, q__1.i = C(j,6).i - q__2.i;
	    C(j,6).r = q__1.r, C(j,6).i = q__1.i;
	    i__2 = j + c_dim1 * 7;
	    i__3 = j + c_dim1 * 7;
	    q__2.r = sum.r * t7.r - sum.i * t7.i, q__2.i = sum.r * t7.i + 
		    sum.i * t7.r;
	    q__1.r = C(j,7).r - q__2.r, q__1.i = C(j,7).i - q__2.i;
	    C(j,7).r = q__1.r, C(j,7).i = q__1.i;
	    i__2 = j + (c_dim1 << 3);
	    i__3 = j + (c_dim1 << 3);
	    q__2.r = sum.r * t8.r - sum.i * t8.i, q__2.i = sum.r * t8.i + 
		    sum.i * t8.r;
	    q__1.r = C(j,8).r - q__2.r, q__1.i = C(j,8).i - q__2.i;
	    C(j,8).r = q__1.r, C(j,8).i = q__1.i;
	}
	goto L410;
L370:

/*        Special code for 9 x 9 Householder */

	v1.r = V(1).r, v1.i = V(1).i;
	r_cnjg(&q__2, &v1);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t1.r = q__1.r, t1.i = q__1.i;
	v2.r = V(2).r, v2.i = V(2).i;
	r_cnjg(&q__2, &v2);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t2.r = q__1.r, t2.i = q__1.i;
	v3.r = V(3).r, v3.i = V(3).i;
	r_cnjg(&q__2, &v3);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t3.r = q__1.r, t3.i = q__1.i;
	v4.r = V(4).r, v4.i = V(4).i;
	r_cnjg(&q__2, &v4);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t4.r = q__1.r, t4.i = q__1.i;
	v5.r = V(5).r, v5.i = V(5).i;
	r_cnjg(&q__2, &v5);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t5.r = q__1.r, t5.i = q__1.i;
	v6.r = V(6).r, v6.i = V(6).i;
	r_cnjg(&q__2, &v6);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t6.r = q__1.r, t6.i = q__1.i;
	v7.r = V(7).r, v7.i = V(7).i;
	r_cnjg(&q__2, &v7);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t7.r = q__1.r, t7.i = q__1.i;
	v8.r = V(8).r, v8.i = V(8).i;
	r_cnjg(&q__2, &v8);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t8.r = q__1.r, t8.i = q__1.i;
	v9.r = V(9).r, v9.i = V(9).i;
	r_cnjg(&q__2, &v9);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t9.r = q__1.r, t9.i = q__1.i;
	i__1 = m__;
	for (j = 1; j <= m__; ++j) {
	    i__2 = j + c_dim1;
	    q__9.r = v1.r * C(j,1).r - v1.i * C(j,1).i, q__9.i = v1.r * C(j,1).i + v1.i * C(j,1).r;
	    i__3 = j + (c_dim1 << 1);
	    q__10.r = v2.r * C(j,2).r - v2.i * C(j,2).i, q__10.i = v2.r * C(j,2).i + v2.i * C(j,2).r;
	    q__8.r = q__9.r + q__10.r, q__8.i = q__9.i + q__10.i;
	    i__4 = j + c_dim1 * 3;
	    q__11.r = v3.r * C(j,3).r - v3.i * C(j,3).i, q__11.i = v3.r * C(j,3).i + v3.i * C(j,3).r;
	    q__7.r = q__8.r + q__11.r, q__7.i = q__8.i + q__11.i;
	    i__5 = j + (c_dim1 << 2);
	    q__12.r = v4.r * C(j,4).r - v4.i * C(j,4).i, q__12.i = v4.r * C(j,4).i + v4.i * C(j,4).r;
	    q__6.r = q__7.r + q__12.r, q__6.i = q__7.i + q__12.i;
	    i__6 = j + c_dim1 * 5;
	    q__13.r = v5.r * C(j,5).r - v5.i * C(j,5).i, q__13.i = v5.r * C(j,5).i + v5.i * C(j,5).r;
	    q__5.r = q__6.r + q__13.r, q__5.i = q__6.i + q__13.i;
	    i__7 = j + c_dim1 * 6;
	    q__14.r = v6.r * C(j,6).r - v6.i * C(j,6).i, q__14.i = v6.r * C(j,6).i + v6.i * C(j,6).r;
	    q__4.r = q__5.r + q__14.r, q__4.i = q__5.i + q__14.i;
	    i__8 = j + c_dim1 * 7;
	    q__15.r = v7.r * C(j,7).r - v7.i * C(j,7).i, q__15.i = v7.r * C(j,7).i + v7.i * C(j,7).r;
	    q__3.r = q__4.r + q__15.r, q__3.i = q__4.i + q__15.i;
	    i__9 = j + (c_dim1 << 3);
	    q__16.r = v8.r * C(j,8).r - v8.i * C(j,8).i, q__16.i = v8.r * C(j,8).i + v8.i * C(j,8).r;
	    q__2.r = q__3.r + q__16.r, q__2.i = q__3.i + q__16.i;
	    i__10 = j + c_dim1 * 9;
	    q__17.r = v9.r * C(j,9).r - v9.i * C(j,9).i, q__17.i = v9.r * 
		    C(j,9).i + v9.i * C(j,9).r;
	    q__1.r = q__2.r + q__17.r, q__1.i = q__2.i + q__17.i;
	    sum.r = q__1.r, sum.i = q__1.i;
	    i__2 = j + c_dim1;
	    i__3 = j + c_dim1;
	    q__2.r = sum.r * t1.r - sum.i * t1.i, q__2.i = sum.r * t1.i + 
		    sum.i * t1.r;
	    q__1.r = C(j,1).r - q__2.r, q__1.i = C(j,1).i - q__2.i;
	    C(j,1).r = q__1.r, C(j,1).i = q__1.i;
	    i__2 = j + (c_dim1 << 1);
	    i__3 = j + (c_dim1 << 1);
	    q__2.r = sum.r * t2.r - sum.i * t2.i, q__2.i = sum.r * t2.i + 
		    sum.i * t2.r;
	    q__1.r = C(j,2).r - q__2.r, q__1.i = C(j,2).i - q__2.i;
	    C(j,2).r = q__1.r, C(j,2).i = q__1.i;
	    i__2 = j + c_dim1 * 3;
	    i__3 = j + c_dim1 * 3;
	    q__2.r = sum.r * t3.r - sum.i * t3.i, q__2.i = sum.r * t3.i + 
		    sum.i * t3.r;
	    q__1.r = C(j,3).r - q__2.r, q__1.i = C(j,3).i - q__2.i;
	    C(j,3).r = q__1.r, C(j,3).i = q__1.i;
	    i__2 = j + (c_dim1 << 2);
	    i__3 = j + (c_dim1 << 2);
	    q__2.r = sum.r * t4.r - sum.i * t4.i, q__2.i = sum.r * t4.i + 
		    sum.i * t4.r;
	    q__1.r = C(j,4).r - q__2.r, q__1.i = C(j,4).i - q__2.i;
	    C(j,4).r = q__1.r, C(j,4).i = q__1.i;
	    i__2 = j + c_dim1 * 5;
	    i__3 = j + c_dim1 * 5;
	    q__2.r = sum.r * t5.r - sum.i * t5.i, q__2.i = sum.r * t5.i + 
		    sum.i * t5.r;
	    q__1.r = C(j,5).r - q__2.r, q__1.i = C(j,5).i - q__2.i;
	    C(j,5).r = q__1.r, C(j,5).i = q__1.i;
	    i__2 = j + c_dim1 * 6;
	    i__3 = j + c_dim1 * 6;
	    q__2.r = sum.r * t6.r - sum.i * t6.i, q__2.i = sum.r * t6.i + 
		    sum.i * t6.r;
	    q__1.r = C(j,6).r - q__2.r, q__1.i = C(j,6).i - q__2.i;
	    C(j,6).r = q__1.r, C(j,6).i = q__1.i;
	    i__2 = j + c_dim1 * 7;
	    i__3 = j + c_dim1 * 7;
	    q__2.r = sum.r * t7.r - sum.i * t7.i, q__2.i = sum.r * t7.i + 
		    sum.i * t7.r;
	    q__1.r = C(j,7).r - q__2.r, q__1.i = C(j,7).i - q__2.i;
	    C(j,7).r = q__1.r, C(j,7).i = q__1.i;
	    i__2 = j + (c_dim1 << 3);
	    i__3 = j + (c_dim1 << 3);
	    q__2.r = sum.r * t8.r - sum.i * t8.i, q__2.i = sum.r * t8.i + 
		    sum.i * t8.r;
	    q__1.r = C(j,8).r - q__2.r, q__1.i = C(j,8).i - q__2.i;
	    C(j,8).r = q__1.r, C(j,8).i = q__1.i;
	    i__2 = j + c_dim1 * 9;
	    i__3 = j + c_dim1 * 9;
	    q__2.r = sum.r * t9.r - sum.i * t9.i, q__2.i = sum.r * t9.i + 
		    sum.i * t9.r;
	    q__1.r = C(j,9).r - q__2.r, q__1.i = C(j,9).i - q__2.i;
	    C(j,9).r = q__1.r, C(j,9).i = q__1.i;
	}
	goto L410;
L390:

/*        Special code for 10 x 10 Householder */

	v1.r = V(1).r, v1.i = V(1).i;
	r_cnjg(&q__2, &v1);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t1.r = q__1.r, t1.i = q__1.i;
	v2.r = V(2).r, v2.i = V(2).i;
	r_cnjg(&q__2, &v2);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t2.r = q__1.r, t2.i = q__1.i;
	v3.r = V(3).r, v3.i = V(3).i;
	r_cnjg(&q__2, &v3);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t3.r = q__1.r, t3.i = q__1.i;
	v4.r = V(4).r, v4.i = V(4).i;
	r_cnjg(&q__2, &v4);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t4.r = q__1.r, t4.i = q__1.i;
	v5.r = V(5).r, v5.i = V(5).i;
	r_cnjg(&q__2, &v5);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t5.r = q__1.r, t5.i = q__1.i;
	v6.r = V(6).r, v6.i = V(6).i;
	r_cnjg(&q__2, &v6);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t6.r = q__1.r, t6.i = q__1.i;
	v7.r = V(7).r, v7.i = V(7).i;
	r_cnjg(&q__2, &v7);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t7.r = q__1.r, t7.i = q__1.i;
	v8.r = V(8).r, v8.i = V(8).i;
	r_cnjg(&q__2, &v8);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t8.r = q__1.r, t8.i = q__1.i;
	v9.r = V(9).r, v9.i = V(9).i;
	r_cnjg(&q__2, &v9);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t9.r = q__1.r, t9.i = q__1.i;
	v10.r = V(10).r, v10.i = V(10).i;
	r_cnjg(&q__2, &v10);
	q__1.r = tau.r * q__2.r - tau.i * q__2.i, q__1.i = tau.r * q__2.i 
		+ tau.i * q__2.r;
	t10.r = q__1.r, t10.i = q__1.i;
	i__1 = m__;
	for (j = 1; j <= m__; ++j) {
	    i__2 = j + c_dim1;
	    q__10.r = v1.r * C(j,1).r - v1.i * C(j,1).i, q__10.i = v1.r * C(j,1).i + v1.i * C(j,1).r;
	    i__3 = j + (c_dim1 << 1);
	    q__11.r = v2.r * C(j,2).r - v2.i * C(j,2).i, q__11.i = v2.r * C(j,2).i + v2.i * C(j,2).r;
	    q__9.r = q__10.r + q__11.r, q__9.i = q__10.i + q__11.i;
	    i__4 = j + c_dim1 * 3;
	    q__12.r = v3.r * C(j,3).r - v3.i * C(j,3).i, q__12.i = v3.r * C(j,3).i + v3.i * C(j,3).r;
	    q__8.r = q__9.r + q__12.r, q__8.i = q__9.i + q__12.i;
	    i__5 = j + (c_dim1 << 2);
	    q__13.r = v4.r * C(j,4).r - v4.i * C(j,4).i, q__13.i = v4.r * C(j,4).i + v4.i * C(j,4).r;
	    q__7.r = q__8.r + q__13.r, q__7.i = q__8.i + q__13.i;
	    i__6 = j + c_dim1 * 5;
	    q__14.r = v5.r * C(j,5).r - v5.i * C(j,5).i, q__14.i = v5.r * C(j,5).i + v5.i * C(j,5).r;
	    q__6.r = q__7.r + q__14.r, q__6.i = q__7.i + q__14.i;
	    i__7 = j + c_dim1 * 6;
	    q__15.r = v6.r * C(j,6).r - v6.i * C(j,6).i, q__15.i = v6.r * C(j,6).i + v6.i * C(j,6).r;
	    q__5.r = q__6.r + q__15.r, q__5.i = q__6.i + q__15.i;
	    i__8 = j + c_dim1 * 7;
	    q__16.r = v7.r * C(j,7).r - v7.i * C(j,7).i, q__16.i = v7.r * C(j,7).i + v7.i * C(j,7).r;
	    q__4.r = q__5.r + q__16.r, q__4.i = q__5.i + q__16.i;
	    i__9 = j + (c_dim1 << 3);
	    q__17.r = v8.r * C(j,8).r - v8.i * C(j,8).i, q__17.i = v8.r * C(j,8).i + v8.i * C(j,8).r;
	    q__3.r = q__4.r + q__17.r, q__3.i = q__4.i + q__17.i;
	    i__10 = j + c_dim1 * 9;
	    q__18.r = v9.r * C(j,9).r - v9.i * C(j,9).i, q__18.i = v9.r * 
		    C(j,9).i + v9.i * C(j,9).r;
	    q__2.r = q__3.r + q__18.r, q__2.i = q__3.i + q__18.i;
	    i__11 = j + c_dim1 * 10;
	    q__19.r = v10.r * C(j,10).r - v10.i * C(j,10).i, q__19.i = 
		    v10.r * C(j,10).i + v10.i * C(j,10).r;
	    q__1.r = q__2.r + q__19.r, q__1.i = q__2.i + q__19.i;
	    sum.r = q__1.r, sum.i = q__1.i;
	    i__2 = j + c_dim1;
	    i__3 = j + c_dim1;
	    q__2.r = sum.r * t1.r - sum.i * t1.i, q__2.i = sum.r * t1.i + 
		    sum.i * t1.r;
	    q__1.r = C(j,1).r - q__2.r, q__1.i = C(j,1).i - q__2.i;
	    C(j,1).r = q__1.r, C(j,1).i = q__1.i;
	    i__2 = j + (c_dim1 << 1);
	    i__3 = j + (c_dim1 << 1);
	    q__2.r = sum.r * t2.r - sum.i * t2.i, q__2.i = sum.r * t2.i + 
		    sum.i * t2.r;
	    q__1.r = C(j,2).r - q__2.r, q__1.i = C(j,2).i - q__2.i;
	    C(j,2).r = q__1.r, C(j,2).i = q__1.i;
	    i__2 = j + c_dim1 * 3;
	    i__3 = j + c_dim1 * 3;
	    q__2.r = sum.r * t3.r - sum.i * t3.i, q__2.i = sum.r * t3.i + 
		    sum.i * t3.r;
	    q__1.r = C(j,3).r - q__2.r, q__1.i = C(j,3).i - q__2.i;
	    C(j,3).r = q__1.r, C(j,3).i = q__1.i;
	    i__2 = j + (c_dim1 << 2);
	    i__3 = j + (c_dim1 << 2);
	    q__2.r = sum.r * t4.r - sum.i * t4.i, q__2.i = sum.r * t4.i + 
		    sum.i * t4.r;
	    q__1.r = C(j,4).r - q__2.r, q__1.i = C(j,4).i - q__2.i;
	    C(j,4).r = q__1.r, C(j,4).i = q__1.i;
	    i__2 = j + c_dim1 * 5;
	    i__3 = j + c_dim1 * 5;
	    q__2.r = sum.r * t5.r - sum.i * t5.i, q__2.i = sum.r * t5.i + 
		    sum.i * t5.r;
	    q__1.r = C(j,5).r - q__2.r, q__1.i = C(j,5).i - q__2.i;
	    C(j,5).r = q__1.r, C(j,5).i = q__1.i;
	    i__2 = j + c_dim1 * 6;
	    i__3 = j + c_dim1 * 6;
	    q__2.r = sum.r * t6.r - sum.i * t6.i, q__2.i = sum.r * t6.i + 
		    sum.i * t6.r;
	    q__1.r = C(j,6).r - q__2.r, q__1.i = C(j,6).i - q__2.i;
	    C(j,6).r = q__1.r, C(j,6).i = q__1.i;
	    i__2 = j + c_dim1 * 7;
	    i__3 = j + c_dim1 * 7;
	    q__2.r = sum.r * t7.r - sum.i * t7.i, q__2.i = sum.r * t7.i + 
		    sum.i * t7.r;
	    q__1.r = C(j,7).r - q__2.r, q__1.i = C(j,7).i - q__2.i;
	    C(j,7).r = q__1.r, C(j,7).i = q__1.i;
	    i__2 = j + (c_dim1 << 3);
	    i__3 = j + (c_dim1 << 3);
	    q__2.r = sum.r * t8.r - sum.i * t8.i, q__2.i = sum.r * t8.i + 
		    sum.i * t8.r;
	    q__1.r = C(j,8).r - q__2.r, q__1.i = C(j,8).i - q__2.i;
	    C(j,8).r = q__1.r, C(j,8).i = q__1.i;
	    i__2 = j + c_dim1 * 9;
	    i__3 = j + c_dim1 * 9;
	    q__2.r = sum.r * t9.r - sum.i * t9.i, q__2.i = sum.r * t9.i + 
		    sum.i * t9.r;
	    q__1.r = C(j,9).r - q__2.r, q__1.i = C(j,9).i - q__2.i;
	    C(j,9).r = q__1.r, C(j,9).i = q__1.i;
	    i__2 = j + c_dim1 * 10;
	    i__3 = j + c_dim1 * 10;
	    q__2.r = sum.r * t10.r - sum.i * t10.i, q__2.i = sum.r * t10.i + 
		    sum.i * t10.r;
	    q__1.r = C(j,10).r - q__2.r, q__1.i = C(j,10).i - q__2.i;
	    C(j,10).r = q__1.r, C(j,10).i = q__1.i;
	}
	goto L410;
    }
L410:
    return ;



} 

