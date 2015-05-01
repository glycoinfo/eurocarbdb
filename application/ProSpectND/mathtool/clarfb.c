

#include "complex.h"
#include "mathtool.h"



void clarfb(char *side, char *trans, char *direct, char *
	    storev, int m__, int n__, int k__, fcomplex *v, int ldv, 
	    fcomplex *t, int ldt, fcomplex *c, int ldc, fcomplex *work, 
	    int ldwork)
{
/*  -- LAPACK auxiliary routine (version 2.0) --   
       Univ. of Tennessee, Univ. of California Berkeley, NAG Ltd.,   
       Courant Institute, Argonne National Lab, and Rice University   
       September 30, 1994   


    Purpose   
    =======   

    CLARFB applies a complex block reflector H or its transpose H' to a   
    complex M-by-N matrix C, from either the left or the right.   

    Arguments   
    =========   

    SIDE    (input) CHARACTER*1   
            = 'L': apply H or H' from the Left   
            = 'R': apply H or H' from the Right   

    TRANS   (input) CHARACTER*1   
            = 'N': apply H (No transpose)   
            = 'C': apply H' (Conjugate transpose)   

    DIRECT  (input) CHARACTER*1   
            Indicates how H is formed from a product of elementary   
            reflectors   
            = 'F': H = H(1) H(2) . . . H(k) (Forward)   
            = 'B': H = H(k) . . . H(2) H(1) (Backward)   

    STOREV  (input) CHARACTER*1   
            Indicates how the vectors which define the elementary   
            reflectors are stored:   
            = 'C': Columnwise   
            = 'R': Rowwise   

    M       (input) INTEGER   
            The number of rows of the matrix C.   

    N       (input) INTEGER   
            The number of columns of the matrix C.   

    K       (input) INTEGER   
            The order of the matrix T (= the number of elementary   
            reflectors whose product defines the block reflector).   

    V       (input) COMPLEX array, dimension   
                                  (LDV,K) if STOREV = 'C'   
                                  (LDV,M) if STOREV = 'R' and SIDE = 'L' 
  
                                  (LDV,N) if STOREV = 'R' and SIDE = 'R' 
  
            The matrix V. See further details.   

    LDV     (input) INTEGER   
            The leading dimension of the array V.   
            If STOREV = 'C' and SIDE = 'L', LDV >= max(1,M);   
            if STOREV = 'C' and SIDE = 'R', LDV >= max(1,N);   
            if STOREV = 'R', LDV >= K.   

    T       (input) COMPLEX array, dimension (LDT,K)   
            The triangular K-by-K matrix T in the representation of the   
            block reflector.   

    LDT     (input) INTEGER   
            The leading dimension of the array T. LDT >= K.   

    C       (input/output) COMPLEX array, dimension (LDC,N)   
            On entry, the M-by-N matrix C.   
            On exit, C is overwritten by H*C or H'*C or C*H or C*H'.   

    LDC     (input) INTEGER   
            The leading dimension of the array C. LDC >= max(1,M).   

    WORK    (workspace) COMPLEX array, dimension (LDWORK,K)   

    LDWORK  (input) INTEGER   
            The leading dimension of the array WORK.   
            If SIDE = 'L', LDWORK >= max(1,N);   
            if SIDE = 'R', LDWORK >= max(1,M).   

    ===================================================================== 
  


       Quick return if possible   

    
   Parameter adjustments   
       Function Body */
    /* Table of constant values */
    static fcomplex c_b1 = {1.f,0.f};
    static int c__1 = 1;
    
    /* System generated locals */
    int c_dim1, work_dim1, i__1, i__2, i__3, i__4, i__5;
    fcomplex q__1, q__2;
    /* Local variables */
    static int i, j;
    static char transt[1];




#define V(I,J) v[(I)-1 + ((J)-1)* ( ldv)]
#define T(I,J) t[(I)-1 + ((J)-1)* ( ldt)]
#define C(I,J) c[(I)-1 + ((J)-1)* ( ldc)]
#define WORK(I,J) work[(I)-1 + ((J)-1)* ( ldwork)]

    if (m__ <= 0 || n__ <= 0) {
	return ;
    }

    if (lsame(trans, "N")) {
	*(unsigned char *)transt = 'C';
    } else {
	*(unsigned char *)transt = 'N';
    }

    if (lsame(storev, "C")) {

	if (lsame(direct, "F")) {

             /*
              *  Let  V =  ( V1 )    (first K rows)   
              *            ( V2 )   
              *  where  V1  is unit lower triangular. 
              */

	    if (lsame(side, "L")) {

                /*
                 * Form  H * C  or  H' * C  where  C = ( C1 )   
                 *                                     ( C2 )   
                 * W := C' * V  =  (C1'*V1 + C2'*V2)  (stored in WORK)   
                 * W := C1' 
                 */

		i__1 = k__;
		for (j = 1; j <= k__; ++j) {
		    ccopy(n__, &C(j,1), ldc, &WORK(1,j), c__1);
		    clacgv(n__, &WORK(1,j), c__1);
		}

                /*
                 * W := W * V1 
                 */

		ctrmm("Right", "Lower", "No transpose", "Unit", n__, k__, c_b1, 
			&V(1,1), ldv, &WORK(1,1), ldwork);
		if (m__ > k__) {

                    /*
                     *                 W := W + C2'*V2 
                     */

		    i__1 = m__ - k__;
		    cgemm("Conjugate transpose", "No transpose", 
                             n__, k__, i__1,
			     c_b1, &C(k__+1,1), ldc, &V(k__+1,1),
                             ldv, c_b1, &WORK(1,1), ldwork);
		}

                /*
                 *              W := W * T'  or  W * T 
                 */

		ctrmm("Right", "Upper", transt, "Non-unit", n__, k__, 
                       c_b1, &T(1,1), ldt, &WORK(1,1), ldwork);

                /*
                 *              C := C - V * W' 
                 */

		if (m__ > k__) {

                    /*
                     *                 C2 := C2 - V2 * W' 
                     */
 
		    i__1 = m__ - k__;
		    q__1.r = -1.f, q__1.i = 0.f;
		    cgemm("No transpose", "Conjugate transpose",
                            i__1, n__, k__,
			     q__1, &V(k__+1,1), ldv, &WORK(1,1), ldwork, 
                             c_b1, &C(k__+1,1), ldc);
		}

                /*
                 *              W := W * V1' 
                 */

		ctrmm("Right", "Lower", "Conjugate transpose", "Unit", 
                        n__, k__,c_b1, &V(1,1), ldv, &WORK(1,1), ldwork);

                /*
                 *              C1 := C1 - W' 
                 */

		i__1 = k__;
		for (j = 1; j <= k__; ++j) {
		    i__2 = n__;
		    for (i = 1; i <= n__; ++i) {
			i__3 = j + i * c_dim1;
			i__4 = j + i * c_dim1;
			r_cnjg(&q__2, &WORK(i,j));
			q__1.r = C(j,i).r - q__2.r, q__1.i = C(j,i).i - 
				q__2.i;
			C(j,i).r = q__1.r, C(j,i).i = q__1.i;
		    }
		}

	    } else if (lsame(side, "R")) {

                /*
                 * Form  C * H  or  C * H'  where  C = ( C1  C2)
                 * W := C * V  =  (C1*V1 + C2*V2)  (stored in WORK)   
                 * W := C1 
                 */

		i__1 = k__;
		for (j = 1; j <= k__; ++j) {
		    ccopy(m__, &C(1,j), c__1, &WORK(1,j), c__1);
		}

                /*
                 *              W := W * V1 
                 */

		ctrmm("Right", "Lower", "No transpose", "Unit", 
                        m__, k__, c_b1, &V(1,1), ldv, &WORK(1,1), ldwork);
		if (n__ > k__) {

                    /*
                     *                 W := W + C2 * V2 
                     */

		    i__1 = n__ - k__;
		    cgemm("No transpose", "No transpose", m__, k__, i__1, c_b1,
			     &C(1,k__+1), ldc, &V(k__+1,1), ldv, c_b1,
                              &WORK(1,1), ldwork);
		}

                /*
                 *              W := W * T  or  W * T' 
                 */

		ctrmm("Right", "Upper", trans, "Non-unit", m__, k__, 
                       c_b1, &T(1,1), ldt, &WORK(1,1), ldwork);

                /*
                 *              C := C - W * V' 
                 */

		if (n__ > k__) {

                    /*
                     *                 C2 := C2 - W * V2' 
                     */

		    i__1 = n__ - k__;
		    q__1.r = -1.f, q__1.i = 0.f;
		    cgemm("No transpose", "Conjugate transpose", m__, i__1, k__,
			   q__1, &WORK(1,1), ldwork, &V(k__+1,1), ldv, 
                           c_b1, &C(1,k__+1),ldc);
		}

                /*
                 *              W := W * V1' 
                 */

		ctrmm("Right", "Lower", "Conjugate transpose", "Unit", 
                       m__, k__, c_b1, &V(1,1), ldv, &WORK(1,1), ldwork);

                /*
                 *              C1 := C1 - W 
                 */

		i__1 = k__;
		for (j = 1; j <= k__; ++j) {
		    i__2 = m__;
		    for (i = 1; i <= m__; ++i) {
			i__3 = i + j * c_dim1;
			i__4 = i + j * c_dim1;
			i__5 = i + j * work_dim1;
			q__1.r = C(i,j).r - WORK(i,j).r, q__1.i = C(i,j).i 
				- WORK(i,j).i;
			C(i,j).r = q__1.r, C(i,j).i = q__1.i;
		    }
		}
	    }

	} else {

            /*           Let  V =  ( V1 )   
             *                     ( V2 )    (last K rows)   
             *           where  V2  is unit upper triangular. 
             */

	    if (lsame(side, "L")) {

                /*
                 *   Form  H * C  or  H' * C  where  C = ( C1 )   
                 *                                       ( C2 )   
                 *   W := C' * V  =  (C1'*V1 + C2'*V2)  (stored in WORK)   
                 *   W := C2' 
                 */

		i__1 = k__;
		for (j = 1; j <= k__; ++j) {
		    ccopy(n__, &C(m__-k__+j,1), ldc, &WORK(1,j), c__1);
		    clacgv(n__, &WORK(1,j), c__1);
		}

                /*
                 *              W := W * V2 
                 */

		ctrmm("Right", "Upper", "No transpose", "Unit", 
                        n__, k__, c_b1, &V(m__-k__+1,1), ldv, &WORK(1,1), 
			ldwork);
		if (m__ > k__) {

                    /*
                     *                 W := W + C1'*V1 
                     */

		    i__1 = m__ - k__;
		    cgemm("Conjugate transpose", "No transpose", 
                          n__, k__, i__1,
			   c_b1, &C(1,1), ldc, &V(1,1), ldv, 
			   c_b1, &WORK(1,1), ldwork);
		}

                /*
                 *              W := W * T'  or  W * T 
                 */

		ctrmm("Right", "Lower", transt, "Non-unit", n__, k__, 
                       c_b1, &T(1,1), ldt, &WORK(1,1), ldwork);

                /*
                 *              C := C - V * W' 
                 */

		if (m__ > k__) {

                    /*
                     *                 C1 := C1 - V1 * W' 
                     */

		    i__1 = m__ - k__;
		    q__1.r = -1.f, q__1.i = 0.f;
		    cgemm("No transpose", "Conjugate transpose", i__1, n__, k__,
			   q__1, &V(1,1), ldv, &WORK(1,1), 
			   ldwork, c_b1, &C(1,1), ldc);
		}

                /*
                 *              W := W * V2' 
                 */

		ctrmm("Right", "Upper", "Conjugate transpose", "Unit", n__, k__, 
			c_b1, &V(m__-k__+1,1), ldv, &WORK(1,1), ldwork);

                /*
                 *              C2 := C2 - W' 
                 */

		i__1 = k__;
		for (j = 1; j <= k__; ++j) {
		    i__2 = n__;
		    for (i = 1; i <= n__; ++i) {
			i__3 = m__ - k__ + j + i * c_dim1;
			i__4 = m__ - k__ + j + i * c_dim1;
			r_cnjg(&q__2, &WORK(i,j));
			q__1.r = C(m__-k__+j,i).r - q__2.r, q__1.i = C(m__-k__+j,i).i - 
				q__2.i;
			C(m__-k__+j,i).r = q__1.r, C(m__-k__+j,i).i = q__1.i;
		    }
		}

	    } else if (lsame(side, "R")) {

                /*   
                 *  Form  C * H  or  C * H'  where  C = ( C1  C2 )
                 *  W := C * V  =  (C1*V1 + C2*V2)  (stored in WORK)   
                 *  W := C2 
                 */
                 

		i__1 = k__;
		for (j = 1; j <= k__; ++j) {
		    ccopy(m__, &C(1,n__-k__+j),c__1, &WORK(1,j), c__1);
		}

                /*  
                 *            W := W * V2 
                 */

		ctrmm("Right", "Upper", "No transpose", "Unit", m__, k__, c_b1, 
			&V(n__-k__+1,1), ldv, &WORK(1,1), 
			ldwork);
		if (n__ > k__) {

                    /*  
                     *               W := W + C1 * V1 
                     */

		    i__1 = n__ - k__;
		    cgemm("No transpose", "No transpose", m__, k__, i__1, c_b1,
			   &C(1,1), ldc, &V(1,1), ldv, c_b1, &WORK(1,1), ldwork);
		}

                /* 
                 *             W := W * T  or  W * T' 
                 */

		ctrmm("Right", "Lower", trans, "Non-unit", m__, k__, 
                       c_b1, &T(1,1), ldt, &WORK(1,1), ldwork);

                /* 
                 *             C := C - W * V' 
                 */

		if (n__ > k__) {

                    /* 
                     *                C1 := C1 - W * V1' 
                     */

		    i__1 = n__ - k__;
		    q__1.r = -1.f, q__1.i = 0.f;
		    cgemm("No transpose", "Conjugate transpose", m__, i__1, k__,
			   q__1, &WORK(1,1), ldwork, &V(1,1), 
			   ldv, c_b1, &C(1,1), ldc);
		}

                /* 
                 *             W := W * V2' 
                 */

		ctrmm("Right", "Upper", "Conjugate transpose", "Unit", 
                        m__, k__, c_b1, &V(n__-k__+1,1), ldv, &WORK(1,1), ldwork);

                /*  
                 *            C2 := C2 - W 
                 */

		i__1 = k__;
		for (j = 1; j <= k__; ++j) {
		    i__2 = m__;
		    for (i = 1; i <= m__; ++i) {
			i__3 = i + (n__ - k__ + j) * c_dim1;
			i__4 = i + (n__ - k__ + j) * c_dim1;
			i__5 = i + j * work_dim1;
			q__1.r = C(i,n__-k__+j).r - WORK(i,j).r, q__1.i = C(i,n__-k__+j).i 
				- WORK(i,j).i;
			C(i,n__-k__+j).r = q__1.r, C(i,n__-k__+j).i = q__1.i;
		    }
		}
	    }
	}

    } else if (lsame(storev, "R")) {

	if (lsame(direct, "F")) {

            /*
             * Let  V =  ( V1  V2 )    (V1: first K columns)   
             * where  V1  is unit upper triangular. 
             */

	    if (lsame(side, "L")) {

                /*
                 * Form  H * C  or  H' * C  where  C = ( C1 )   
                 *                                     ( C2 )   
                 * W := C' * V'  =  (C1'*V1' + C2'*V2') (stored in WORK)   
                 * W := C1' 
                 */

		i__1 = k__;
		for (j = 1; j <= k__; ++j) {
		    ccopy(n__, &C(j,1), ldc, &WORK(1,j), c__1);
		    clacgv(n__, &WORK(1,j), c__1);
		}

                /* 
                 *             W := W * V1' 
                 */

		ctrmm("Right", "Upper", "Conjugate transpose", "Unit", n__, k__, 
			c_b1, &V(1,1), ldv, &WORK(1,1), ldwork);
		if (m__ > k__) {

                    /*
                     *       W := W + C2'*V2' 
                     */

		    i__1 = m__ - k__;
		    cgemm("Conjugate transpose", "Conjugate transpose", n__, k__,
			   i__1, c_b1, &C(k__+1,1), ldc, &V(1,k__+1), ldv, c_b1, 
                           &WORK(1,1),ldwork);
		}

                /*   
                 *           W := W * T'  or  W * T 
                 */

		ctrmm("Right", "Upper", transt, "Non-unit", n__, k__, 
                       c_b1, &T(1,1), ldt, &WORK(1,1), ldwork);

                /* 
                 *             C := C - V' * W' 
                 */

		if (m__ > k__) {

                    /*
                     *                 C2 := C2 - V2' * W' 
                     */

		    i__1 = m__ - k__;
		    q__1.r = -1.f, q__1.i = 0.f;
		    cgemm("Conjugate transpose", "Conjugate transpose", 
			    i__1, n__, k__, q__1, &V(1,k__+1), ldv,
			     &WORK(1,1), ldwork, c_b1, &C(k__+1,1), ldc);
		}

                /*
                 *              W := W * V1 
                 */

		ctrmm("Right", "Upper", "No transpose", "Unit", n__, k__, c_b1, 
			&V(1,1), ldv, &WORK(1,1), ldwork);

                /*
                 *              C1 := C1 - W' 
                 */

		i__1 = k__;
		for (j = 1; j <= k__; ++j) {
		    i__2 = n__;
		    for (i = 1; i <= n__; ++i) {
			i__3 = j + i * c_dim1;
			i__4 = j + i * c_dim1;
			r_cnjg(&q__2, &WORK(i,j));
			q__1.r = C(j,i).r - q__2.r, q__1.i = C(j,i).i - 
				q__2.i;
			C(j,i).r = q__1.r, C(j,i).i = q__1.i;
		    }
		}

	    } else if (lsame(side, "R")) {

                /* 
                 *    Form  C * H  or  C * H'  where  C = ( C1  C2 )
                 *    W := C * V'  =  (C1*V1' + C2*V2')  (stored in WORK)   
                 *    W := C1 
                 */

		i__1 = k__;
		for (j = 1; j <= k__; ++j) {
		    ccopy(m__, &C(1,j), c__1, &WORK(1,j), c__1);
		}

                /*
                 *              W := W * V1' 
                 */

		ctrmm("Right", "Upper", "Conjugate transpose", "Unit", m__, k__, 
			c_b1, &V(1,1), ldv, &WORK(1,1), ldwork);
		if (n__ > k__) {

                    /*
                     *                 W := W + C2 * V2' 
                     */

		    i__1 = n__ - k__;
		    cgemm("No transpose", "Conjugate transpose", m__, k__, 
                           i__1,c_b1, &C(1,k__+1), ldc, &V(1,k__+1), ldv, c_b1, 
                           &WORK(1,1),ldwork);
		}

                /*
                 *              W := W * T  or  W * T' 
                 */

		ctrmm("Right", "Upper", trans, "Non-unit", m__, k__, 
                      c_b1, &T(1,1), ldt, &WORK(1,1), ldwork);

                /* 
                 *             C := C - W * V 
                 */

		if (n__ > k__) {

                    /*
                     *                 C2 := C2 - W * V2 
                     */

		    i__1 = n__ - k__;
		    q__1.r = -1.f, q__1.i = 0.f;
		    cgemm("No transpose", "No transpose", m__, i__1, k__, q__1,
			  &WORK(1,1), ldwork, &V(1,k__+1), ldv, c_b1, 
                          &C(1,k__+1), ldc);
		}

                /* 
                 *             W := W * V1 
                 */

		ctrmm("Right", "Upper", "No transpose", "Unit", m__, k__, c_b1, 
			&V(1,1), ldv, &WORK(1,1), ldwork);

                /* 
                 *             C1 := C1 - W 
                 */

		i__1 = k__;
		for (j = 1; j <= k__; ++j) {
		    i__2 = m__;
		    for (i = 1; i <= m__; ++i) {
			i__3 = i + j * c_dim1;
			i__4 = i + j * c_dim1;
			i__5 = i + j * work_dim1;
			q__1.r = C(i,j).r - WORK(i,j).r, q__1.i = C(i,j).i 
				- WORK(i,j).i;
			C(i,j).r = q__1.r, C(i,j).i = q__1.i;
		    }
		}

	    }

	} else {

                /*
                 *           Let  V =  ( V1  V2 )    (V2: last K columns) 
                 *           where  V2  is unit lower triangular. 
                 */
	    if (lsame(side, "L")) {

                /*
                 *
                 *  Form  H * C  or  H' * C  where  C = ( C1 )   
                 *                                       ( C2 )   
                 *   W := C' * V'  =  (C1'*V1' + C2'*V2') (stored in WORK)   
                 *   W := C2'
                 */

		i__1 = k__;
		for (j = 1; j <= k__; ++j) {
		    ccopy(n__, &C(m__-k__+j,1), ldc, &WORK(1,j), c__1);
		    clacgv(n__, &WORK(1,j), c__1);
		}

                /*
                 *              W := W * V2' 
                 */

		ctrmm("Right", "Lower", "Conjugate transpose", "Unit", n__, k__, 
			c_b1, &V(1,m__-k__+1), ldv, &WORK(1,1), ldwork);
		if (m__ > k__) {

                    /*
                     *                 W := W + C1'*V1' 
                     */

		    i__1 = m__ - k__;
		    cgemm("Conjugate transpose", "Conjugate transpose",
                            n__, k__,i__1, c_b1, &C(1,1), ldc, &V(1,1), 
			    ldv, c_b1, &WORK(1,1), ldwork);
		}

                /*
                 *              W := W * T'  or  W * T 
                 */

		ctrmm("Right", "Lower", transt, "Non-unit", n__, k__, 
                      c_b1, &T(1,1), ldt, &WORK(1,1), ldwork);

                /*
                 *              C := C - V' * W' 
                 */

		if (m__ > k__) {

                    /*
                     *                 C1 := C1 - V1' * W' 
                     */

		    i__1 = m__ - k__;
		    q__1.r = -1.f, q__1.i = 0.f;
		    cgemm("Conjugate transpose", "Conjugate transpose", 
			    i__1, n__, k__, q__1, &V(1,1), 
                            ldv, &WORK(1,1), ldwork, c_b1, &C(1,1), ldc);
		}

                /*
                 *              W := W * V2 
                 */

		ctrmm("Right", "Lower", "No transpose", "Unit", n__, k__, c_b1, 
			&V(1,m__-k__+1), ldv, &WORK(1,1), ldwork);

                /*
                 *              C2 := C2 - W' 
                 */

		i__1 = k__;
		for (j = 1; j <= k__; ++j) {
		    i__2 = n__;
		    for (i = 1; i <= n__; ++i) {
			i__3 = m__ - k__ + j + i * c_dim1;
			i__4 = m__ - k__ + j + i * c_dim1;
			r_cnjg(&q__2, &WORK(i,j));
			q__1.r = C(m__-k__+j,i).r - q__2.r, q__1.i = C(m__-k__+j,i).i - 
				q__2.i;
			C(m__-k__+j,i).r = q__1.r, C(m__-k__+j,i).i = q__1.i;
		    }
		}

	    } else if (lsame(side, "R")) {

                /*
                 *  Form  C * H  or  C * H'  where  C = ( C1  C2 )
                 *  W := C * V'  =  (C1*V1' + C2*V2')  (stored in WORK)   
                 *  W := C2 
                 */

		i__1 = k__;
		for (j = 1; j <= k__; ++j) {
		    ccopy(m__, &C(1,n__-k__+j), c__1, &WORK(1,j), c__1);
		}

                /*
                 *              W := W * V2' 
                 */

		ctrmm("Right", "Lower", "Conjugate transpose", "Unit", m__, k__, 
			c_b1, &V(1,n__-k__+1), ldv, &WORK(1,1), ldwork);
		if (n__ > k__) {

                    /* 
                     *                W := W + C1 * V1' 
                     */

		    i__1 = n__ - k__;
		    cgemm("No transpose", "Conjugate transpose", m__, k__, 
                           i__1, c_b1, &C(1,1), ldc, &V(1,1), ldv, 
			   c_b1, &WORK(1,1), ldwork);
		}

                /*
                 *              W := W * T  or  W * T' 
                 */

		ctrmm("Right", "Lower", trans, "Non-unit", m__, k__, 
                       c_b1, &T(1,1), ldt, &WORK(1,1), ldwork);

                /* 
                 *             C := C - W * V 
                 */

		if (n__ > k__) {

                    /*  
                     *               C1 := C1 - W * V1 
                     */

		    i__1 = n__ - k__;
		    q__1.r = -1.f, q__1.i = 0.f;
		    cgemm("No transpose", "No transpose", m__, i__1, k__, q__1,
			   &WORK(1,1), ldwork, &V(1,1), ldv, 
			   c_b1, &C(1,1), ldc);
		}

                /* 
                 *             W := W * V2 
                 */

		ctrmm("Right", "Lower", "No transpose", "Unit", m__, k__, c_b1, 
		      &V(1,n__-k__+1), ldv, &WORK(1,1), ldwork);

                /* 
                 *             C1 := C1 - W 
                 */

		i__1 = k__;
		for (j = 1; j <= k__; ++j) {
		    i__2 = m__;
		    for (i = 1; i <= m__; ++i) {
			i__3 = i + (n__ - k__ + j) * c_dim1;
			i__4 = i + (n__ - k__ + j) * c_dim1;
			i__5 = i + j * work_dim1;
			q__1.r = C(i,n__-k__+j).r - WORK(i,j).r, q__1.i = C(i,n__-k__+j).i 
				- WORK(i,j).i;
			C(i,n__-k__+j).r = q__1.r, C(i,n__-k__+j).i = q__1.i;
		    }
		}

	    }

	}
    }


} 

