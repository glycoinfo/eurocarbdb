

#include "complex.h"
#include "mathtool.h"



void clarft(char *direct, char *storev, int n__, int k__,
	    fcomplex *v, int ldv, fcomplex *tau, fcomplex *t, int ldt)
{
/*  -- LAPACK auxiliary routine (version 2.0) --   
       Univ. of Tennessee, Univ. of California Berkeley, NAG Ltd.,   
       Courant Institute, Argonne National Lab, and Rice University   
       September 30, 1994   


    Purpose   
    =======   

    CLARFT forms the triangular factor T of a complex block reflector H   
    of order n, which is defined as a product of k elementary reflectors. 
  

    If DIRECT = 'F', H = H(1) H(2) . . . H(k) and T is upper triangular; 
  

    If DIRECT = 'B', H = H(k) . . . H(2) H(1) and T is lower triangular. 
  

    If STOREV = 'C', the vector which defines the elementary reflector   
    H(i) is stored in the i-th column of the array V, and   

       H  =  I - V * T * V'   

    If STOREV = 'R', the vector which defines the elementary reflector   
    H(i) is stored in the i-th row of the array V, and   

       H  =  I - V' * T * V   

    Arguments   
    =========   

    DIRECT  (input) CHARACTER*1   
            Specifies the order in which the elementary reflectors are   
            multiplied to form the block reflector:   
            = 'F': H = H(1) H(2) . . . H(k) (Forward)   
            = 'B': H = H(k) . . . H(2) H(1) (Backward)   

    STOREV  (input) CHARACTER*1   
            Specifies how the vectors which define the elementary   
            reflectors are stored (see also Further Details):   
            = 'C': columnwise   
            = 'R': rowwise   

    N       (input) INTEGER   
            The order of the block reflector H. N >= 0.   

    K       (input) INTEGER   
            The order of the triangular factor T (= the number of   
            elementary reflectors). K >= 1.   

    V       (input/output) COMPLEX array, dimension   
                                 (LDV,K) if STOREV = 'C'   
                                 (LDV,N) if STOREV = 'R'   
            The matrix V. See further details.   

    LDV     (input) INTEGER   
            The leading dimension of the array V.   
            If STOREV = 'C', LDV >= max(1,N); if STOREV = 'R', LDV >= K. 
  

    TAU     (input) COMPLEX array, dimension (K)   
            TAU(i) must contain the scalar factor of the elementary   
            reflector H(i).   

    T       (output) COMPLEX array, dimension (LDT,K)   
            The k by k triangular factor T of the block reflector.   
            If DIRECT = 'F', T is upper triangular; if DIRECT = 'B', T is 
  
            lower triangular. The rest of the array is not used.   

    LDT     (input) INTEGER   
            The leading dimension of the array T. LDT >= K.   

    Further Details   
    ===============   

    The shape of the matrix V and the storage of the vectors which define 
  
    the H(i) is best illustrated by the following example with n = 5 and 
  
    k = 3. The elements equal to 1 are not stored; the corresponding   
    array elements are modified but restored on exit. The rest of the   
    array is not used.   

    DIRECT = 'F' and STOREV = 'C':         DIRECT = 'F' and STOREV = 'R': 
  

                 V = (  1       )                 V = (  1 v1 v1 v1 v1 ) 
  
                     ( v1  1    )                     (     1 v2 v2 v2 ) 
  
                     ( v1 v2  1 )                     (        1 v3 v3 ) 
  
                     ( v1 v2 v3 )   
                     ( v1 v2 v3 )   

    DIRECT = 'B' and STOREV = 'C':         DIRECT = 'B' and STOREV = 'R': 
  

                 V = ( v1 v2 v3 )                 V = ( v1 v1  1       ) 
  
                     ( v1 v2 v3 )                     ( v2 v2 v2  1    ) 
  
                     (  1 v2 v3 )                     ( v3 v3 v3 v3  1 ) 
  
                     (     1 v3 )   
                     (        1 )   

    ===================================================================== 
  


       Quick return if possible   

    
   Parameter adjustments   
       Function Body */
    /* Table of constant values */
    static fcomplex c_b2 = {0.f,0.f};
    static int c__1 = 1;
    
    /* System generated locals */
    int t_dim1, v_dim1, i__1, i__2, i__3, i__4;
    fcomplex q__1;
    /* Local variables */
    static int i, j;
    static fcomplex vii;



#define TAU(I) tau[(I)-1]

#define V(I,J) v[(I)-1 + ((J)-1)* ( ldv)]
#define T(I,J) t[(I)-1 + ((J)-1)* ( ldt)]

    if (n__ == 0) {
	return;
    }

    if (lsame(direct, "F")) {
	i__1 = k__;
	for (i = 1; i <= k__; ++i) {
	    i__2 = i;
	    if (TAU(i).r == 0.f && TAU(i).i == 0.f) {

/*              H(i)  =  I */

		i__2 = i;
		for (j = 1; j <= i; ++j) {
		    i__3 = j + i * t_dim1;
		    T(j,i).r = 0.f, T(j,i).i = 0.f;
		}
	    } else {

/*              general case */

		i__2 = i + i * v_dim1;
		vii.r = V(i,i).r, vii.i = V(i,i).i;
		i__2 = i + i * v_dim1;
		V(i,i).r = 1.f, V(i,i).i = 0.f;
		if (lsame(storev, "C")) {

/*                 T(1:i-1,i) := - tau(i) * V(i:n,1:i-1)' * V(i:n,i) */

		    i__2 = n__ - i + 1;
		    i__3 = i - 1;
		    i__4 = i;
		    q__1.r = -(double)TAU(i).r, q__1.i = -(double)
			    TAU(i).i;
		    cgemv("Conjugate transpose", i__2, i__3, q__1,
                           &V(i,1), ldv, &V(i,i), c__1, c_b2, &
			    T(1,i), c__1);
		} else {

/*                 T(1:i-1,i) := - tau(i) * V(1:i-1,i:n) * V(i,i:n)' */

		    if (i < n__) {
			i__2 = n__ - i;
			clacgv(i__2, &V(i,i+1), ldv);
		    }
		    i__2 = i - 1;
		    i__3 = n__ - i + 1;
		    i__4 = i;
		    q__1.r = -(double)TAU(i).r, q__1.i = -(double)
			    TAU(i).i;
		    cgemv("No transpose", i__2, i__3, q__1, &V(1,i), 
                            ldv, &V(i,i), ldv, c_b2, &T(1,i), c__1);
		    if (i < n__) {
			i__2 = n__ - i;
			clacgv(i__2, &V(i,i+1), ldv);
		    }
		}
		i__2 = i + i * v_dim1;
		V(i,i).r = vii.r, V(i,i).i = vii.i;

/*              T(1:i-1,i) := T(1:i-1,1:i-1) * T(1:i-1,i) */

		i__2 = i - 1;
		ctrmv("Upper", "No transpose", "Non-unit", i__2, &T(1,1), 
                         ldt, &T(1,i), c__1);
		i__2 = i + i * t_dim1;
		i__3 = i;
		T(i,i).r = TAU(i).r, T(i,i).i = TAU(i).i;
	    }
	}
    } else {
	for (i = k__; i >= 1; --i) {
	    i__1 = i;
	    if (TAU(i).r == 0.f && TAU(i).i == 0.f) {

/*              H(i)  =  I */

		i__1 = k__;
		for (j = i; j <= k__; ++j) {
		    i__2 = j + i * t_dim1;
		    T(j,i).r = 0.f, T(j,i).i = 0.f;
		}
	    } else {

/*              general case */

		if (i < k__) {
		    if (lsame(storev, "C")) {
			i__1 = n__ - k__ + i + i * v_dim1;
			vii.r = V(n__-k__+i,i).r, vii.i = V(n__-k__+i,i).i;
			i__1 = n__ - k__ + i + i * v_dim1;
			V(n__-k__+i,i).r = 1.f, V(n__-k__+i,i).i = 0.f;

/*                    T(i+1:k,i) :=   
                              - tau(i) * V(1:n-k+i,i+1:k)' * V(1:n-k+i,i) */

			i__1 = n__ - k__ + i;
			i__2 = k__ - i;
			i__3 = i;
			q__1.r = -(double)TAU(i).r, q__1.i = -(
				double)TAU(i).i;
			cgemv("Conjugate transpose", i__1, i__2, q__1, 
                                &V(1,i+1), ldv, &V(1,i)
				, c__1, c_b2, &T(i+1,i), c__1);
			i__1 = n__ - k__ + i + i * v_dim1;
			V(n__-k__+i,i).r = vii.r, V(n__-k__+i,i).i = vii.i;
		    } else {
			i__1 = i + (n__ - k__ + i) * v_dim1;
			vii.r = V(i,n__-k__+i).r, vii.i = V(i,n__-k__+i).i;
			i__1 = i + (n__ - k__ + i) * v_dim1;
			V(i,n__-k__+i).r = 1.f, V(i,n__-k__+i).i = 0.f;

/*                    T(i+1:k,i) :=   
                              - tau(i) * V(i+1:k,1:n-k+i) * V(i,1:n-k+i)' */

			i__1 = n__ - k__ + i - 1;
			clacgv(i__1, &V(i,1), ldv);
			i__1 = k__ - i;
			i__2 = n__ - k__ + i;
			i__3 = i;
			q__1.r = -(double)TAU(i).r, q__1.i = -(
				double)TAU(i).i;
			cgemv("No transpose", i__1, i__2, q__1, &V(i+1,1), 
                               ldv, &V(i,1), ldv, c_b2, &
			       T(i+1,i), c__1);
			i__1 = n__ - k__ + i - 1;
			clacgv(i__1, &V(i,1), ldv);
			i__1 = i + (n__ - k__ + i) * v_dim1;
			V(i,n__-k__+i).r = vii.r, V(i,n__-k__+i).i = vii.i;
		    }

/*                 T(i+1:k,i) := T(i+1:k,i+1:k) * T(i+1:k,i) */

		    i__1 = k__ - i;
		    ctrmv("Lower", "No transpose", "Non-unit", i__1, &T(i+1,i+1), 
                             ldt, &T(i+1,i), c__1);
		}
		i__1 = i + i * t_dim1;
		i__2 = i;
		T(i,i).r = TAU(i).r, T(i,i).i = TAU(i).i;
	    }
	}
    }

} 

