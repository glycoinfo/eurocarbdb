
#include <math.h>
#include "complex.h"
#include "mathtool.h"


void cgebak(char *job, char *side, int n__, int ilo, 
	    int ihi, float *scale, int m__, fcomplex *v, int ldv, 
	    int *info)
{
/*  -- LAPACK routine (version 2.0) --   
       Univ. of Tennessee, Univ. of California Berkeley, NAG Ltd.,   
       Courant Institute, Argonne National Lab, and Rice University   
       September 30, 1994   


    Purpose   
    =======   

    CGEBAK forms the right or left eigenvectors of a complex general   
    matrix by backward transformation on the computed eigenvectors of the 
  
    balanced matrix output by CGEBAL.   

    Arguments   
    =========   

    JOB     (input) CHARACTER*1   
            Specifies the type of backward transformation required:   
            = 'N', do nothing, return immediately;   
            = 'P', do backward transformation for permutation only;   
            = 'S', do backward transformation for scaling only;   
            = 'B', do backward transformations for both permutation and   
                   scaling.   
            JOB must be the same as the argument JOB supplied to CGEBAL. 
  

    SIDE    (input) CHARACTER*1   
            = 'R':  V contains right eigenvectors;   
            = 'L':  V contains left eigenvectors.   

    N       (input) INTEGER   
            The number of rows of the matrix V.  N >= 0.   

    ILO     (input) INTEGER   
    IHI     (input) INTEGER   
            The ints ILO and IHI determined by CGEBAL.   
            1 <= ILO <= IHI <= N, if N > 0; ILO=1 and IHI=0, if N=0.   

    SCALE   (input) REAL array, dimension (N)   
            Details of the permutation and scaling factors, as returned   
            by CGEBAL.   

    M       (input) INTEGER   
            The number of columns of the matrix V.  M >= 0.   

    V       (input/output) COMPLEX array, dimension (LDV,M)   
            On entry, the matrix of right or left eigenvectors to be   
            transformed, as returned by CHSEIN or CTREVC.   
            On exit, V is overwritten by the transformed eigenvectors.   

    LDV     (input) INTEGER   
            The leading dimension of the array V. LDV >= max(1,N).   

    INFO    (output) INTEGER   
            = 0:  successful exit   
            < 0:  if INFO = -i, the i-th argument had an illegal value.   

    ===================================================================== 
  


       Decode and Test the input parameters   

    
   Parameter adjustments   
       Function Body */
    /* System generated locals */
    int  i__1;
    /* Local variables */
    static int i, k;
    static float s;
    static int leftv;
    static int ii;
    static int rightv;


#define SCALE(I) scale[(I)-1]

#define V(I,J) v[(I)-1 + ((J)-1)* ( ldv)]

    rightv = lsame(side, "R");
    leftv = lsame(side, "L");

    *info = 0;
    if (! lsame(job, "N") && ! lsame(job, "P") && ! lsame(
	    job, "S") && ! lsame(job, "B")) {
	*info = -1;
    } else if (! rightv && ! leftv) {
	*info = -2;
    } else if (n__ < 0) {
	*info = -3;
    } else if (ilo < 1 || ilo > max(1,n__)) {
	*info = -4;
    } else if (ihi < min(ilo,n__) || ihi > n__) {
	*info = -5;
    } else if (m__ < 0) {
	*info = -7;
    } else if (ldv < max(1,n__)) {
	*info = -9;
    }
    if (*info != 0) {
	i__1 = -(*info);
	return ;
    }

/*     Quick return if possible */

    if (n__ == 0) {
	return ;
    }
    if (m__ == 0) {
	return ;
    }
    if (lsame(job, "N")) {
	return ;
    }

    if (ilo == ihi) {
	goto L30;
    }

/*     Backward balance */

    if (lsame(job, "S") || lsame(job, "B")) {

	if (rightv) {
	    i__1 = ihi;
	    for (i = ilo; i <= ihi; ++i) {
		s = SCALE(i);
		csscal(m__, s, &V(i,1), ldv);
	    }
	}

	if (leftv) {
	    i__1 = ihi;
	    for (i = ilo; i <= ihi; ++i) {
		s = 1.f / SCALE(i);
		csscal(m__, s, &V(i,1), ldv);
	    }
	}

    }

/*     Backward permutation   

       For  I = ILO-1 step -1 until 1,   
                IHI+1 step 1 until N do -- */

L30:
    if (lsame(job, "P") || lsame(job, "B")) {
	if (rightv) {
	    i__1 = n__;
	    for (ii = 1; ii <= n__; ++ii) {
		i = ii;
		if (i >= ilo && i <= ihi) {
		    goto L40;
		}
		if (i < ilo) {
		    i = ilo - ii;
		}
		k = SCALE(i);
		if (k == i) {
		    goto L40;
		}
		cswap(m__, &V(i,1), ldv, &V(k,1), ldv);
L40:
		;
	    }
	}

	if (leftv) {
	    i__1 = n__;
	    for (ii = 1; ii <= n__; ++ii) {
		i = ii;
		if (i >= ilo && i <= ihi) {
		    goto L50;
		}
		if (i < ilo) {
		    i = ilo - ii;
		}
		k = SCALE(i);
		if (k == i) {
		    goto L50;
		}
		cswap(m__, &V(i,1), ldv, &V(k,1), ldv);
L50:
		;
	    }
	}
    }

    return ;



} 

