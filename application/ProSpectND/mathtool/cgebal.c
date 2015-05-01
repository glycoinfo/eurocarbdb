

#include <math.h>
#include "complex.h"
#include "mathtool.h"

void cgebal(char *job, int n__, fcomplex *a, int lda, 
	    int *ilo, int *ihi, float *scale, int *info)
{
/*  -- LAPACK routine (version 2.0) --   
       Univ. of Tennessee, Univ. of California Berkeley, NAG Ltd.,   
       Courant Institute, Argonne National Lab, and Rice University   
       September 30, 1994   


    Purpose   
    =======   

    CGEBAL balances a general complex matrix A.  This involves, first,   
    permuting A by a similarity transformation to isolate eigenvalues   
    in the first 1 to ILO-1 and last IHI+1 to N elements on the   
    diagonal; and second, applying a diagonal similarity transformation   
    to rows and columns ILO to IHI to make the rows and columns as   
    close in norm as possible.  Both steps are optional.   

    Balancing may reduce the 1-norm of the matrix, and improve the   
    accuracy of the computed eigenvalues and/or eigenvectors.   

    Arguments   
    =========   

    JOB     (input) CHARACTER*1   
            Specifies the operations to be performed on A:   
            = 'N':  none:  simply set ILO = 1, IHI = N, SCALE(I) = 1.0   
                    for i = 1,...,N;   
            = 'P':  permute only;   
            = 'S':  scale only;   
            = 'B':  both permute and scale.   

    N       (input) INTEGER   
            The order of the matrix A.  N >= 0.   

    A       (input/output) COMPLEX array, dimension (LDA,N)   
            On entry, the input matrix A.   
            On exit,  A is overwritten by the balanced matrix.   
            If JOB = 'N', A is not referenced.   
            See Further Details.   

    LDA     (input) INTEGER   
            The leading dimension of the array A.  LDA >= max(1,N).   

    ILO     (output) INTEGER   
    IHI     (output) INTEGER   
            ILO and IHI are set to ints such that on exit   
            A(i,j) = 0 if i > j and j = 1,...,ILO-1 or I = IHI+1,...,N.   
            If JOB = 'N' or 'S', ILO = 1 and IHI = N.   

    SCALE   (output) REAL array, dimension (N)   
            Details of the permutations and scaling factors applied to   
            A.  If P(j) is the index of the row and column interchanged   
            with row and column j and D(j) is the scaling factor   
            applied to row and column j, then   
            SCALE(j) = P(j)    for j = 1,...,ILO-1   
                     = D(j)    for j = ILO,...,IHI   
                     = P(j)    for j = IHI+1,...,N.   
            The order in which the interchanges are made is N to IHI+1,   
            then 1 to ILO-1.   

    INFO    (output) INTEGER   
            = 0:  successful exit.   
            < 0:  if INFO = -i, the i-th argument had an illegal value.   

    Further Details   
    ===============   

    The permutations consist of row and column interchanges which put   
    the matrix in the form   

               ( T1   X   Y  )   
       P A P = (  0   B   Z  )   
               (  0   0   T2 )   

    where T1 and T2 are upper triangular matrices whose eigenvalues lie   
    along the diagonal.  The column indices ILO and IHI mark the starting 
  
    and ending columns of the submatrix B. Balancing consists of applying 
  
    a diagonal similarity transformation inv(D) * B * D to make the   
    1-norms of each row of B and its corresponding column nearly equal.   
    The output matrix is   

       ( T1     X*D          Y    )   
       (  0  inv(D)*B*D  inv(D)*Z ).   
       (  0      0           T2   )   

    Information about the permutations P and the diagonal matrix D is   
    returned in the vector SCALE.   

    This subroutine is based on the EISPACK routine CBAL.   

    ===================================================================== 
  


       Test the input parameters   

    
   Parameter adjustments   
       Function Body */
    /* Table of constant values */
    static int c__1 = 1;
    
    /* System generated locals */
    int a_dim1, i__1, i__2, i__3;
    float r__1, r__2;
    /* Local variables */
    static int iexc;
    static float c, f, g;
    static int i, j, k, l, m;
    static float r, s;
    static float sfmin1, sfmin2, sfmax1, sfmax2, ca, ra;
    static int noconv;
    static int ica, ira;



#define SCALE(I) scale[(I)-1]

#define A(I,J) a[(I)-1 + ((J)-1)* ( lda)]

    *info = 0;
    if (! lsame(job, "N") && ! lsame(job, "P") && ! lsame(
	    job, "S") && ! lsame(job, "B")) {
	*info = -1;
    } else if (n__ < 0) {
	*info = -2;
    } else if (lda < max(1,n__)) {
	*info = -4;
    }
    if (*info != 0) {
	i__1 = -(*info);
	return ;
    }

    k = 1;
    l = n__;

    if (n__ == 0) {
	goto L210;
    }

    if (lsame(job, "N")) {
	i__1 = n__;
	for (i = 1; i <= n__; ++i) {
	    SCALE(i) = 1.f;
	}
	goto L210;
    }

    if (lsame(job, "S")) {
	goto L120;
    }

/*     Permutation to isolate eigenvalues if possible */

    goto L50;

/*     Row and column exchange. */

L20:
    SCALE(m) = (float) j;
    if (j == m) {
	goto L30;
    }

    cswap(l, &A(1,j), c__1, &A(1,m), c__1);
    i__1 = n__ - k + 1;
    cswap(i__1, &A(j,k), lda, &A(m,k), lda);

L30:
    switch (iexc) {
	case 1:  goto L40;
	case 2:  goto L80;
    }

/*     Search for rows isolating an eigenvalue and push them down. */

L40:
    if (l == 1) {
	goto L210;
    }
    --l;

L50:
    for (j = l; j >= 1; --j) {

	i__1 = l;
	for (i = 1; i <= l; ++i) {
	    if (i == j) {
		goto L60;
	    }
	    i__2 = j + i * a_dim1;
	    if (A(j,i).r != 0.f || A(j,i).i != 0.f) {
		goto L70;
	    }
L60:
	    ;
	}

	m = l;
	iexc = 1;
	goto L20;
L70:
	;
    }

    goto L90;

/*     Search for columns isolating an eigenvalue and push them left. */

L80:
    ++k;

L90:
    i__1 = l;
    for (j = k; j <= l; ++j) {

	i__2 = l;
	for (i = k; i <= l; ++i) {
	    if (i == j) {
		goto L100;
	    }
	    i__3 = i + j * a_dim1;
	    if (A(i,j).r != 0.f || A(i,j).i != 0.f) {
		goto L110;
	    }
L100:
	    ;
	}

	m = k;
	iexc = 2;
	goto L20;
L110:
	;
    }

L120:
    i__1 = l;
    for (i = k; i <= l; ++i) {
	SCALE(i) = 1.f;
    }

    if (lsame(job, "P")) {
	goto L210;
    }

/*     Balance the submatrix in rows K to L.   

       Iterative loop for norm reduction */

    sfmin1 = slamch("S") / slamch("P");
    sfmax1 = 1.f / sfmin1;
    sfmin2 = sfmin1 * 10.f;
    sfmax2 = 1.f / sfmin2;
L140:
    noconv = FALSE;

    i__1 = l;
    for (i = k; i <= l; ++i) {
	c = 0.f;
	r = 0.f;

	i__2 = l;
	for (j = k; j <= l; ++j) {
	    if (j == i) {
		goto L150;
	    }
	    i__3 = j + i * a_dim1;
	    c += (r__1 = A(j,i).r, fabs(r__1)) + (r__2 = A(j,i).i, fabs(r__2));
	    i__3 = i + j * a_dim1;
	    r += (r__1 = A(i,j).r, fabs(r__1)) + (r__2 = A(i,j).i, fabs(r__2));
L150:
	    ;
	}
	ica = icamax(l, &A(1,i), c__1);
	ca = Cabs(A(ica,i));
	i__2 = n__ - k + 1;
	ira = icamax(i__2, &A(i,k), lda);
	ra = Cabs(A(i,ira+k-1));

/*        Guard against zero C or R due to underflow. */

	if (c == 0.f || r == 0.f) {
	    goto L200;
	}
	g = r / 10.f;
	f = 1.f;
	s = c + r;
L160:
/* Computing MAX */
	r__1 = max(f,c);
/* Computing MIN */
	r__2 = min(r,g);
	if (c >= g || max(r__1,ca) >= sfmax2 || min(r__2,ra) <= sfmin2) {
	    goto L170;
	}
	f *= 10.f;
	c *= 10.f;
	ca *= 10.f;
	r /= 10.f;
	g /= 10.f;
	ra /= 10.f;
	goto L160;

L170:
	g = c / 10.f;
L180:
/* Computing MIN */
	r__1 = min(f,c), r__1 = min(r__1,g);
	if (g < r || max(r,ra) >= sfmax2 || min(r__1,ca) <= sfmin2) {
	    goto L190;
	}
	f /= 10.f;
	c /= 10.f;
	g /= 10.f;
	ca /= 10.f;
	r *= 10.f;
	ra *= 10.f;
	goto L180;

/*        Now balance. */

L190:
	if (c + r >= s * .95f) {
	    goto L200;
	}
	if (f < 1.f && SCALE(i) < 1.f) {
	    if (f * SCALE(i) <= sfmin1) {
		goto L200;
	    }
	}
	if (f > 1.f && SCALE(i) > 1.f) {
	    if (SCALE(i) >= sfmax1 / f) {
		goto L200;
	    }
	}
	g = 1.f / f;
	SCALE(i) *= f;
	noconv = TRUE;

	i__2 = n__ - k + 1;
	csscal(i__2, g, &A(i,k), lda);
	csscal(l, f, &A(1,i), c__1);

L200:
	;
    }

    if (noconv) {
	goto L140;
    }

L210:
    *ilo = k;
    *ihi = l;



} 

