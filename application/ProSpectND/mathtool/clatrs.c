
#include <math.h>
#include "complex.h"
#include "mathtool.h"


void clatrs(char *uplo, char *trans, char *diag, char *normin, 
	    int n__, fcomplex *a, int lda, fcomplex *x, float *scale,
	    float *cnorm, int *info)
{
/*  -- LAPACK auxiliary routine (version 2.0) --   
       Univ. of Tennessee, Univ. of California Berkeley, NAG Ltd.,   
       Courant Institute, Argonne National Lab, and Rice University   
       June 30, 1992   


    Purpose   
    =======   

    CLATRS solves one of the triangular systems   

       A * x = s*b,  A**T * x = s*b,  or  A**H * x = s*b,   

    with scaling to prevent overflow.  Here A is an upper or lower   
    triangular matrix, A**T denotes the transpose of A, A**H denotes the 
  
    conjugate transpose of A, x and b are n-element vectors, and s is a   
    scaling factor, usually less than or equal to 1, chosen so that the   
    components of x will be less than the overflow threshold.  If the   
    unscaled problem will not cause overflow, the Level 2 BLAS routine   
    CTRSV is called. If the matrix A is singular (A(j,j) = 0 for some j), 
  
    then s is set to 0 and a non-trivial solution to A*x = 0 is returned. 
  

    Arguments   
    =========   

    UPLO    (input) CHARACTER*1   
            Specifies whether the matrix A is upper or lower triangular. 
  
            = 'U':  Upper triangular   
            = 'L':  Lower triangular   

    TRANS   (input) CHARACTER*1   
            Specifies the operation applied to A.   
            = 'N':  Solve A * x = s*b     (No transpose)   
            = 'T':  Solve A**T * x = s*b  (Transpose)   
            = 'C':  Solve A**H * x = s*b  (Conjugate transpose)   

    DIAG    (input) CHARACTER*1   
            Specifies whether or not the matrix A is unit triangular.   
            = 'N':  Non-unit triangular   
            = 'U':  Unit triangular   

    NORMIN  (input) CHARACTER*1   
            Specifies whether CNORM has been set or not.   
            = 'Y':  CNORM contains the column norms on entry   
            = 'N':  CNORM is not set on entry.  On exit, the norms will   
                    be computed and stored in CNORM.   

    N       (input) INTEGER   
            The order of the matrix A.  N >= 0.   

    A       (input) COMPLEX array, dimension (LDA,N)   
            The triangular matrix A.  If UPLO = 'U', the leading n by n   
            upper triangular part of the array A contains the upper   
            triangular matrix, and the strictly lower triangular part of 
  
            A is not referenced.  If UPLO = 'L', the leading n by n lower 
  
            triangular part of the array A contains the lower triangular 
  
            matrix, and the strictly upper triangular part of A is not   
            referenced.  If DIAG = 'U', the diagonal elements of A are   
            also not referenced and are assumed to be 1.   

    LDA     (input) INTEGER   
            The leading dimension of the array A.  LDA >= max (1,N).   

    X       (input/output) COMPLEX array, dimension (N)   
            On entry, the right hand side b of the triangular system.   
            On exit, X is overwritten by the solution vector x.   

    SCALE   (output) REAL   
            The scaling factor s for the triangular system   
               A * x = s*b,  A**T * x = s*b,  or  A**H * x = s*b.   
            If SCALE = 0, the matrix A is singular or badly scaled, and   
            the vector x is an exact or approximate solution to A*x = 0. 
  

    CNORM   (input or output) REAL array, dimension (N)   

            If NORMIN = 'Y', CNORM is an input argument and CNORM(j)   
            contains the norm of the off-diagonal part of the j-th column 
  
            of A.  If TRANS = 'N', CNORM(j) must be greater than or equal 
  
            to the infinity-norm, and if TRANS = 'T' or 'C', CNORM(j)   
            must be greater than or equal to the 1-norm.   

            If NORMIN = 'N', CNORM is an output argument and CNORM(j)   
            returns the 1-norm of the offdiagonal part of the j-th column 
  
            of A.   

    INFO    (output) INTEGER   
            = 0:  successful exit   
            < 0:  if INFO = -k, the k-th argument had an illegal value   

    Further Details   
    ======= =======   

    A rough bound on x is computed; if that is less than overflow, CTRSV 
  
    is called, otherwise, specific code is used which checks for possible 
  
    overflow or divide-by-zero at every operation.   

    A columnwise scheme is used for solving A*x = b.  The basic algorithm 
  
    if A is lower triangular is   

         x[1:n] := b[1:n]   
         for j = 1, ..., n   
              x(j) := x(j) / A(j,j)   
              x[j+1:n] := x[j+1:n] - x(j) * A[j+1:n,j]   
         end   

    Define bounds on the components of x after j iterations of the loop: 
  
       M(j) = bound on x[1:j]   
       G(j) = bound on x[j+1:n]   
    Initially, let M(0) = 0 and G(0) = max{x(i), i=1,...,n}.   

    Then for iteration j+1 we have   
       M(j+1) <= G(j) / | A(j+1,j+1) |   
       G(j+1) <= G(j) + M(j+1) * | A[j+2:n,j+1] |   
              <= G(j) ( 1 + CNORM(j+1) / | A(j+1,j+1) | )   

    where CNORM(j+1) is greater than or equal to the infinity-norm of   
    column j+1 of A, not counting the diagonal.  Hence   

       G(j) <= G(0) product ( 1 + CNORM(i) / | A(i,i) | )   
                    1<=i<=j   
    and   

       |x(j)| <= ( G(0) / |A(j,j)| ) product ( 1 + CNORM(i) / |A(i,i)| ) 
  
                                     1<=i< j   

    Since |x(j)| <= M(j), we use the Level 2 BLAS routine CTRSV if the   
    reciprocal of the largest M(j), j=1,..,n, is larger than   
    max(underflow, 1/overflow).   

    The bound on x(j) is also used to determine when a step in the   
    columnwise method can be performed without fear of overflow.  If   
    the computed bound is greater than a large constant, x is scaled to   
    prevent overflow, but if the bound overflows, x is set to 0, x(j) to 
  
    1, and scale to 0, and a non-trivial solution to A*x = 0 is found.   

    Similarly, a row-wise scheme is used to solve A**T *x = b  or   
    A**H *x = b.  The basic algorithm for A upper triangular is   

         for j = 1, ..., n   
              x(j) := ( b(j) - A[1:j-1,j]' * x[1:j-1] ) / A(j,j)   
         end   

    We simultaneously compute two bounds   
         G(j) = bound on ( b(i) - A[1:i-1,i]' * x[1:i-1] ), 1<=i<=j   
         M(j) = bound on x(i), 1<=i<=j   

    The initial values are G(0) = 0, M(0) = max{b(i), i=1,..,n}, and we   
    add the constraint G(j) >= G(j-1) and M(j) >= M(j-1) for j >= 1.   
    Then the bound on x(j) is   

         M(j) <= M(j-1) * ( 1 + CNORM(j) ) / | A(j,j) |   

              <= M(0) * product ( ( 1 + CNORM(i) ) / |A(i,i)| )   
                        1<=i<=j   

    and we can safely call CTRSV if 1/M(n) and 1/G(n) are both greater   
    than max(underflow, 1/overflow).   

    ===================================================================== 
  


    
   Parameter adjustments   
       Function Body */
    /* Table of constant values */
    static int c__1 = 1;
    static float c_b36 = .5f;
    
    /* System generated locals */
    int a_dim1,  i__1, i__2, i__3, i__4, i__5;
    float r__1, r__2, r__3, r__4;
    fcomplex q__1, q__2, q__3, q__4;

    /* Local variables */
    static int jinc;
    static float xbnd;
    static int imax;
    static float tmax;
    static fcomplex tjjs;
    static float xmax, grow;
    static int i, j;
    static float tscal;
    static fcomplex uscal;
    static int jlast;
    static fcomplex csumj;
    static int upper;
    static float xj;
    static float bignum;
    static int notran;
    static int jfirst;
    static float smlnum;
    static int nounit;
    static float rec, tjj;



#define X(I) x[(I)-1]
#define CNORM(I) cnorm[(I)-1]

#define A(I,J) a[(I)-1 + ((J)-1)* ( lda)]

    *info = 0;
    upper = lsame(uplo, "U");
    notran = lsame(trans, "N");
    nounit = lsame(diag, "N");

/*     Test the input parameters. */

    if (! upper && ! lsame(uplo, "L")) {
	*info = -1;
    } else if (! notran && ! lsame(trans, "T") && ! lsame(trans, 
	    "C")) {
	*info = -2;
    } else if (! nounit && ! lsame(diag, "U")) {
	*info = -3;
    } else if (! lsame(normin, "Y") && ! lsame(normin, "N"))
	     {
	*info = -4;
    } else if (n__ < 0) {
	*info = -5;
    } else if (lda < max(1,n__)) {
	*info = -7;
    }
    if (*info != 0) {
	i__1 = -(*info);
	return;
    }

/*     Quick return if possible */

    if (n__ == 0) {
	return;
    }

/*     Determine machine dependent parameters to control overflow. */

    smlnum = slamch("Safe minimum");
    bignum = 1.f / smlnum;
    slabad(&smlnum, &bignum);
    smlnum /= slamch("Precision");
    bignum = 1.f / smlnum;
    *scale = 1.f;

    if (lsame(normin, "N")) {

/*        Compute the 1-norm of each column, not including the diagonal. */

	if (upper) {

/*           A is upper triangular. */

	    i__1 = n__;
	    for (j = 1; j <= n__; ++j) {
		i__2 = j - 1;
		CNORM(j) = scasum(i__2, &A(1,j), c__1);
	    }
	} else {

/*           A is lower triangular. */

	    i__1 = n__ - 1;
	    for (j = 1; j <= n__-1; ++j) {
		i__2 = n__ - j;
		CNORM(j) = scasum(i__2, &A(j+1,j), c__1);
	    }
	    CNORM(n__) = 0.f;
	}
    }

/*     Scale the column norms by TSCAL if the maximum element in CNORM is 
  
       greater than BIGNUM/2. */

    imax = isamax(n__, &CNORM(1), c__1);
    tmax = CNORM(imax);
    if (tmax <= bignum * .5f) {
	tscal = 1.f;
    } else {
	tscal = .5f / (smlnum * tmax);
	sscal(n__, tscal, &CNORM(1), c__1);
    }

/*     Compute a bound on the computed solution vector to see if the   
       Level 2 BLAS routine CTRSV can be used. */

    xmax = 0.f;
    i__1 = n__;
    for (j = 1; j <= n__; ++j) {
/* Computing MAX */
	i__2 = j;
	r__3 = xmax, r__4 = (r__1 = X(j).r / 2.f, fabs(r__1)) + (r__2 = 
		X(j).i / 2.f, fabs(r__2));
	xmax = max(r__3,r__4);
    }
    xbnd = xmax;

    if (notran) {

/*        Compute the growth in A * x = b. */

	if (upper) {
	    jfirst = n__;
	    jlast = 1;
	    jinc = -1;
	} else {
	    jfirst = 1;
	    jlast = n__;
	    jinc = 1;
	}

	if (tscal != 1.f) {
	    grow = 0.f;
	    goto L60;
	}

	if (nounit) {

/*           A is non-unit triangular.   

             Compute GROW = 1/G(j) and XBND = 1/M(j).   
             Initially, G(0) = max{x(i), i=1,...,n}. */

	    grow = .5f / max(xbnd,smlnum);
	    xbnd = grow;
	    i__1 = jlast;
	    i__2 = jinc;
	    for (j = jfirst; jinc < 0 ? j >= jlast : j <= jlast; j += jinc) {

/*              Exit the loop if the growth factor is too small. */

		if (grow <= smlnum) {
		    goto L60;
		}

		i__3 = j + j * a_dim1;
		tjjs.r = A(j,j).r, tjjs.i = A(j,j).i;
		tjj = (r__1 = tjjs.r, fabs(r__1)) + (r__2 = tjjs.i, 
			fabs(r__2));

		if (tjj >= smlnum) {

/*                 M(j) = G(j-1) / abs(A(j,j))   

   Computing MIN */
		    r__1 = xbnd, r__2 = min(1.f,tjj) * grow;
		    xbnd = min(r__1,r__2);
		} else {

/*                 M(j) could overflow, set XBND to 0. */

		    xbnd = 0.f;
		}

		if (tjj + CNORM(j) >= smlnum) {

/*                 G(j) = G(j-1)*( 1 + CNORM(j) / abs(A(j,j)) ) */

		    grow *= tjj / (tjj + CNORM(j));
		} else {

/*                 G(j) could overflow, set GROW to 0. */

		    grow = 0.f;
		}
	    }
	    grow = xbnd;
	} else {

/*           A is unit triangular.   

             Compute GROW = 1/G(j), where G(0) = max{x(i), i=1,...,n}.   

   Computing MIN */
	    r__1 = 1.f, r__2 = .5f / max(xbnd,smlnum);
	    grow = min(r__1,r__2);
	    i__2 = jlast;
	    i__1 = jinc;
	    for (j = jfirst; jinc < 0 ? j >= jlast : j <= jlast; j += jinc) {

/*              Exit the loop if the growth factor is too small. */

		if (grow <= smlnum) {
		    goto L60;
		}

/*              G(j) = G(j-1)*( 1 + CNORM(j) ) */

		grow *= 1.f / (CNORM(j) + 1.f);
	    }
	}
L60:

	;
    } else {

/*        Compute the growth in A**T * x = b  or  A**H * x = b. */

	if (upper) {
	    jfirst = 1;
	    jlast = n__;
	    jinc = 1;
	} else {
	    jfirst = n__;
	    jlast = 1;
	    jinc = -1;
	}

	if (tscal != 1.f) {
	    grow = 0.f;
	    goto L90;
	}

	if (nounit) {

/*           A is non-unit triangular.   

             Compute GROW = 1/G(j) and XBND = 1/M(j).   
             Initially, M(0) = max{x(i), i=1,...,n}. */

	    grow = .5f / max(xbnd,smlnum);
	    xbnd = grow;
	    i__1 = jlast;
	    i__2 = jinc;
	    for (j = jfirst; jinc < 0 ? j >= jlast : j <= jlast; j += jinc) {

/*              Exit the loop if the growth factor is too small. */

		if (grow <= smlnum) {
		    goto L90;
		}

/*              G(j) = max( G(j-1), M(j-1)*( 1 + CNORM(j) ) ) 
*/

		xj = CNORM(j) + 1.f;
/* Computing MIN */
		r__1 = grow, r__2 = xbnd / xj;
		grow = min(r__1,r__2);

		i__3 = j + j * a_dim1;
		tjjs.r = A(j,j).r, tjjs.i = A(j,j).i;
		tjj = (r__1 = tjjs.r, fabs(r__1)) + (r__2 = tjjs.i, 
			fabs(r__2));

		if (tjj >= smlnum) {

/*                 M(j) = M(j-1)*( 1 + CNORM(j) ) / abs(A(j,j)) */

		    if (xj > tjj) {
			xbnd *= tjj / xj;
		    }
		} else {

/*                 M(j) could overflow, set XBND to 0. */

		    xbnd = 0.f;
		}
	    }
	    grow = min(grow,xbnd);
	} else {

/*           A is unit triangular.   

             Compute GROW = 1/G(j), where G(0) = max{x(i), i=1,...,n}.   
   Computing MIN */
	    r__1 = 1.f, r__2 = .5f / max(xbnd,smlnum);
	    grow = min(r__1,r__2);
	    i__2 = jlast;
	    i__1 = jinc;
	    for (j = jfirst; jinc < 0 ? j >= jlast : j <= jlast; j += jinc) {

/*              Exit the loop if the growth factor is too small. */

		if (grow <= smlnum) {
		    goto L90;
		}

/*              G(j) = ( 1 + CNORM(j) )*G(j-1) */

		xj = CNORM(j) + 1.f;
		grow /= xj;
	    }
	}
L90:
	;
    }

    if (grow * tscal > smlnum) {

/*        Use the Level 2 BLAS solve if the reciprocal of the bound on
   
          elements of X is not too small. */

	ctrsv(uplo, trans, diag, n__, &A(1,1), lda, &X(1), c__1);
    } else {

/*        Use a Level 1 BLAS solve, scaling intermediate results. */

	if (xmax > bignum * .5f) {

/*           Scale X so that its components are less than or equal to   
             BIGNUM in absolute value. */

	    *scale = bignum * .5f / xmax;
	    csscal(n__, *scale, &X(1), c__1);
	    xmax = bignum;
	} else {
	    xmax *= 2.f;
	}

	if (notran) {

/*           Solve A * x = b */

	    i__1 = jlast;
	    i__2 = jinc;
	    for (j = jfirst; jinc < 0 ? j >= jlast : j <= jlast; j += jinc) {

/*              Compute x(j) = b(j) / A(j,j), scaling x if necessary. */

		i__3 = j;
		xj = (r__1 = X(j).r, fabs(r__1)) + (r__2 = X(j).i, 
			fabs(r__2));
		if (nounit) {
		    i__3 = j + j * a_dim1;
		    q__1.r = tscal * A(j,j).r, q__1.i = tscal * A(j,j).i;
		    tjjs.r = q__1.r, tjjs.i = q__1.i;
		} else {
		    tjjs.r = tscal, tjjs.i = 0.f;
		    if (tscal == 1.f) {
			goto L105;
		    }
		}
		tjj = (r__1 = tjjs.r, fabs(r__1)) + (r__2 = tjjs.i, 
			fabs(r__2));
		if (tjj > smlnum) {

/*                    abs(A(j,j)) > SMLNUM: */

		    if (tjj < 1.f) {
			if (xj > tjj * bignum) {

/*                          Scale x by 1/b(j). */

			    rec = 1.f / xj;
			    csscal(n__, rec, &X(1), c__1);
			    *scale *= rec;
			    xmax *= rec;
			}
		    }
		    i__3 = j;
		    cladiv(&q__1, X(j), tjjs);
		    X(j).r = q__1.r, X(j).i = q__1.i;
		    i__3 = j;
		    xj = (r__1 = X(j).r, fabs(r__1)) + (r__2 = X(j).i
			    , fabs(r__2));
		} else if (tjj > 0.f) {

/*                    0 < abs(A(j,j)) <= SMLNUM: */

		    if (xj > tjj * bignum) {

/*                       Scale x by (1/abs(x(j)))*abs(A(j,j))*BIGNUM   
                         to avoid overflow when dividing by A(j,j). */

			rec = tjj * bignum / xj;
			if (CNORM(j) > 1.f) {

/*                          Scale by 1/CNORM(j) to avoid overflow when   
                            multiplying x(j) times column j. */

			    rec /= CNORM(j);
			}
			csscal(n__, rec, &X(1), c__1);
			*scale *= rec;
			xmax *= rec;
		    }
		    i__3 = j;
		    cladiv(&q__1, X(j), tjjs);
		    X(j).r = q__1.r, X(j).i = q__1.i;
		    i__3 = j;
		    xj = (r__1 = X(j).r, fabs(r__1)) + (r__2 = X(j).i
			    , fabs(r__2));
		} else {

/*                    A(j,j) = 0:  Set x(1:n) = 0, x(j) = 1, and   
                      scale = 0, and compute a solution to A*x = 0. */

		    i__3 = n__;
		    for (i = 1; i <= n__; ++i) {
			i__4 = i;
			X(i).r = 0.f, X(i).i = 0.f;
		    }
		    i__3 = j;
		    X(j).r = 1.f, X(j).i = 0.f;
		    xj = 1.f;
		    *scale = 0.f;
		    xmax = 0.f;
		}
L105:

/*              Scale x if necessary to avoid overflow when adding a   
                multiple of column j of A. */

		if (xj > 1.f) {
		    rec = 1.f / xj;
		    if (CNORM(j) > (bignum - xmax) * rec) {

/*                    Scale x by 1/(2*abs(x(j))). */

			rec *= .5f;
			csscal(n__, rec, &X(1), c__1);
			*scale *= rec;
		    }
		} else if (xj * CNORM(j) > bignum - xmax) {

/*                 Scale x by 1/2. */

		    csscal(n__, c_b36, &X(1), c__1);
		    *scale *= .5f;
		}

		if (upper) {
		    if (j > 1) {

/*                    Compute the update   
                         x(1:j-1) := x(1:j-1) - x(j) * A(1:j-1,j) */

			i__3 = j - 1;
			i__4 = j;
			q__2.r = -(double)X(j).r, q__2.i = -(
				double)X(j).i;
			q__1.r = tscal * q__2.r, q__1.i = tscal * q__2.i;
			caxpy(i__3, q__1, &A(1,j), c__1, &X(1),
				 c__1);
			i__3 = j - 1;
			i = icamax(i__3, &X(1), c__1);
			i__3 = i;
			xmax = (r__1 = X(i).r, fabs(r__1)) + (r__2 = 
				X(i).i, fabs(r__2));
		    }
		} else {
		    if (j < n__) {

/*                    Compute the update   
                         x(j+1:n) := x(j+1:n) - x(j) * A(j+1:n,j) */

			i__3 = n__ - j;
			i__4 = j;
			q__2.r = -(double)X(j).r, q__2.i = -(
				double)X(j).i;
			q__1.r = tscal * q__2.r, q__1.i = tscal * q__2.i;
			caxpy(i__3, q__1, &A(j+1,j), c__1, &
				X(j + 1), c__1);
			i__3 = n__ - j;
			i = j + icamax(i__3, &X(j + 1), c__1);
			i__3 = i;
			xmax = (r__1 = X(i).r, fabs(r__1)) + (r__2 = 
				X(i).i, fabs(r__2));
		    }
		}
	    }

	} else if (lsame(trans, "T")) {

/*           Solve A**T * x = b */

	    i__2 = jlast;
	    i__1 = jinc;
	    for (j = jfirst; jinc < 0 ? j >= jlast : j <= jlast; j += jinc) {

/*              Compute x(j) = b(j) - sum A(k,j)*x(k).   
                                      k<>j */

		i__3 = j;
		xj = (r__1 = X(j).r, fabs(r__1)) + (r__2 = X(j).i, 
			fabs(r__2));
		uscal.r = tscal, uscal.i = 0.f;
		rec = 1.f / max(xmax,1.f);
		if (CNORM(j) > (bignum - xj) * rec) {

/*                 If x(j) could overflow, scale x by 1/(2*XMAX). */

		    rec *= .5f;
		    if (nounit) {
			i__3 = j + j * a_dim1;
			q__1.r = tscal * A(j,j).r, q__1.i = tscal * A(j,j)
				.i;
			tjjs.r = q__1.r, tjjs.i = q__1.i;
		    } else {
			tjjs.r = tscal, tjjs.i = 0.f;
		    }
		    tjj = (r__1 = tjjs.r, fabs(r__1)) + (r__2 = tjjs.i,
			     fabs(r__2));
		    if (tjj > 1.f) {

/*                       Divide by A(j,j) when scaling x if A(j,j) > 1.   

   Computing MIN */
			r__1 = 1.f, r__2 = rec * tjj;
			rec = min(r__1,r__2);
			cladiv(&q__1, uscal, tjjs);
			uscal.r = q__1.r, uscal.i = q__1.i;
		    }
		    if (rec < 1.f) {
			csscal(n__, rec, &X(1), c__1);
			*scale *= rec;
			xmax *= rec;
		    }
		}

		csumj.r = 0.f, csumj.i = 0.f;
		if (uscal.r == 1.f && uscal.i == 0.f) {

/*                 If the scaling needed for A in the dot product is 1,   
                   call CDOTU to perform the dot product. 
*/

		    if (upper) {
			i__3 = j - 1;
			cdotu(&q__1, i__3, &A(1,j), c__1, &X(1),
				 c__1);
			csumj.r = q__1.r, csumj.i = q__1.i;
		    } else if (j < n__) {
			i__3 = n__ - j;
			cdotu(&q__1, i__3, &A(j+1,j), c__1, &
				X(j + 1), c__1);
			csumj.r = q__1.r, csumj.i = q__1.i;
		    }
		} else {

/*                 Otherwise, use in-line code for the dot product. */

		    if (upper) {
			i__3 = j - 1;
			for (i = 1; i <= j-1; ++i) {
			    i__4 = i + j * a_dim1;
			    q__3.r = A(i,j).r * uscal.r - A(i,j).i * 
				    uscal.i, q__3.i = A(i,j).r * uscal.i + A(i,j).i * uscal.r;
			    i__5 = i;
			    q__2.r = q__3.r * X(i).r - q__3.i * X(i).i, 
				    q__2.i = q__3.r * X(i).i + q__3.i * X(
				    i).r;
			    q__1.r = csumj.r + q__2.r, q__1.i = csumj.i + 
				    q__2.i;
			    csumj.r = q__1.r, csumj.i = q__1.i;
			}
		    } else if (j < n__) {
			i__3 = n__;
			for (i = j + 1; i <= n__; ++i) {
			    i__4 = i + j * a_dim1;
			    q__3.r = A(i,j).r * uscal.r - A(i,j).i * 
				    uscal.i, q__3.i = A(i,j).r * uscal.i + A(i,j).i * uscal.r;
			    i__5 = i;
			    q__2.r = q__3.r * X(i).r - q__3.i * X(i).i, 
				    q__2.i = q__3.r * X(i).i + q__3.i * X(
				    i).r;
			    q__1.r = csumj.r + q__2.r, q__1.i = csumj.i + 
				    q__2.i;
			    csumj.r = q__1.r, csumj.i = q__1.i;
			}
		    }
		}

		q__1.r = tscal, q__1.i = 0.f;
		if (uscal.r == q__1.r && uscal.i == q__1.i) {

/*                 Compute x(j) := ( x(j) - CSUMJ ) / A(j,j) if 1/A(j,j)   
                   was not used to scale the dotproduct. 
*/

		    i__3 = j;
		    i__4 = j;
		    q__1.r = X(j).r - csumj.r, q__1.i = X(j).i - 
			    csumj.i;
		    X(j).r = q__1.r, X(j).i = q__1.i;
		    i__3 = j;
		    xj = (r__1 = X(j).r, fabs(r__1)) + (r__2 = X(j).i
			    , fabs(r__2));
		    if (nounit) {
			i__3 = j + j * a_dim1;
			q__1.r = tscal * A(j,j).r, q__1.i = tscal * A(j,j)
				.i;
			tjjs.r = q__1.r, tjjs.i = q__1.i;
		    } else {
			tjjs.r = tscal, tjjs.i = 0.f;
			if (tscal == 1.f) {
			    goto L145;
			}
		    }

/*                    Compute x(j) = x(j) / A(j,j), scaling if necessary. */

		    tjj = (r__1 = tjjs.r, fabs(r__1)) + (r__2 = tjjs.i,
			     fabs(r__2));
		    if (tjj > smlnum) {

/*                       abs(A(j,j)) > SMLNUM: */

			if (tjj < 1.f) {
			    if (xj > tjj * bignum) {

/*                             Scale X by 1/abs(x(j)). */

				rec = 1.f / xj;
				csscal(n__, rec, &X(1), c__1);
				*scale *= rec;
				xmax *= rec;
			    }
			}
			i__3 = j;
			cladiv(&q__1, X(j), tjjs);
			X(j).r = q__1.r, X(j).i = q__1.i;
		    } else if (tjj > 0.f) {

/*                       0 < abs(A(j,j)) <= SMLNUM: */

			if (xj > tjj * bignum) {

/*                          Scale x by (1/abs(x(j)))*abs(A(j,j))*BIGNUM. */

			    rec = tjj * bignum / xj;
			    csscal(n__, rec, &X(1), c__1);
			    *scale *= rec;
			    xmax *= rec;
			}
			i__3 = j;
			cladiv(&q__1, X(j), tjjs);
			X(j).r = q__1.r, X(j).i = q__1.i;
		    } else {

/*                       A(j,j) = 0:  Set x(1:n) = 0, x(j) = 1, and   
                         scale = 0 and compute a solution to A**T *x = 0. */

			i__3 = n__;
			for (i = 1; i <= n__; ++i) {
			    i__4 = i;
			    X(i).r = 0.f, X(i).i = 0.f;
			}
			i__3 = j;
			X(j).r = 1.f, X(j).i = 0.f;
			*scale = 0.f;
			xmax = 0.f;
		    }
L145:
		    ;
		} else {

/*                 Compute x(j) := x(j) / A(j,j) - CSUMJ if the dot   
                   product has already been divided by 1/A(j,j). */

		    i__3 = j;
		    cladiv(&q__2, X(j), tjjs);
		    q__1.r = q__2.r - csumj.r, q__1.i = q__2.i - csumj.i;
		    X(j).r = q__1.r, X(j).i = q__1.i;
		}
/* Computing MAX */
		i__3 = j;
		r__3 = xmax, r__4 = (r__1 = X(j).r, fabs(r__1)) + (r__2 = 
			X(j).i, fabs(r__2));
		xmax = max(r__3,r__4);
	    }

	} else {

/*           Solve A**H * x = b */

	    i__1 = jlast;
	    i__2 = jinc;
	    for (j = jfirst; jinc < 0 ? j >= jlast : j <= jlast; j += jinc) {

/*              Compute x(j) = b(j) - sum A(k,j)*x(k).   
                                      k<>j */

		i__3 = j;
		xj = (r__1 = X(j).r, fabs(r__1)) + (r__2 = X(j).i, 
			fabs(r__2));
		uscal.r = tscal, uscal.i = 0.f;
		rec = 1.f / max(xmax,1.f);
		if (CNORM(j) > (bignum - xj) * rec) {

/*                 If x(j) could overflow, scale x by 1/(2*XMAX). */

		    rec *= .5f;
		    if (nounit) {
			r_cnjg(&q__2, &A(j,j));
			q__1.r = tscal * q__2.r, q__1.i = tscal * q__2.i;
			tjjs.r = q__1.r, tjjs.i = q__1.i;
		    } else {
			tjjs.r = tscal, tjjs.i = 0.f;
		    }
		    tjj = (r__1 = tjjs.r, fabs(r__1)) + (r__2 = tjjs.i,
			     fabs(r__2));
		    if (tjj > 1.f) {

/*                       Divide by A(j,j) when scaling x if A(j,j) > 1.   

   Computing MIN */
			r__1 = 1.f, r__2 = rec * tjj;
			rec = min(r__1,r__2);
			cladiv(&q__1, uscal, tjjs);
			uscal.r = q__1.r, uscal.i = q__1.i;
		    }
		    if (rec < 1.f) {
			csscal(n__, rec, &X(1), c__1);
			*scale *= rec;
			xmax *= rec;
		    }
		}

		csumj.r = 0.f, csumj.i = 0.f;
		if (uscal.r == 1.f && uscal.i == 0.f) {

/*                 If the scaling needed for A in the dot product is 1,   
                   call CDOTC to perform the dot product. 
*/

		    if (upper) {
			i__3 = j - 1;
			cdotc(&q__1, i__3, &A(1,j), c__1, &X(1),
				 c__1);
			csumj.r = q__1.r, csumj.i = q__1.i;
		    } else if (j < n__) {
			i__3 = n__ - j;
			cdotc(&q__1, i__3, &A(j+1,j), c__1, &
				X(j + 1), c__1);
			csumj.r = q__1.r, csumj.i = q__1.i;
		    }
		} else {

/*                 Otherwise, use in-line code for the dot product. */

		    if (upper) {
			i__3 = j - 1;
			for (i = 1; i <= j-1; ++i) {
			    r_cnjg(&q__4, &A(i,j));
			    q__3.r = q__4.r * uscal.r - q__4.i * uscal.i, 
				    q__3.i = q__4.r * uscal.i + q__4.i * 
				    uscal.r;
			    i__4 = i;
			    q__2.r = q__3.r * X(i).r - q__3.i * X(i).i, 
				    q__2.i = q__3.r * X(i).i + q__3.i * X(
				    i).r;
			    q__1.r = csumj.r + q__2.r, q__1.i = csumj.i + 
				    q__2.i;
			    csumj.r = q__1.r, csumj.i = q__1.i;
			}
		    } else if (j < n__) {
			i__3 = n__;
			for (i = j + 1; i <= n__; ++i) {
			    r_cnjg(&q__4, &A(i,j));
			    q__3.r = q__4.r * uscal.r - q__4.i * uscal.i, 
				    q__3.i = q__4.r * uscal.i + q__4.i * 
				    uscal.r;
			    i__4 = i;
			    q__2.r = q__3.r * X(i).r - q__3.i * X(i).i, 
				    q__2.i = q__3.r * X(i).i + q__3.i * X(
				    i).r;
			    q__1.r = csumj.r + q__2.r, q__1.i = csumj.i + 
				    q__2.i;
			    csumj.r = q__1.r, csumj.i = q__1.i;
			}
		    }
		}

		q__1.r = tscal, q__1.i = 0.f;
		if (uscal.r == q__1.r && uscal.i == q__1.i) {

/*                 Compute x(j) := ( x(j) - CSUMJ ) / A(j,j) if 1/A(j,j)   
                   was not used to scale the dotproduct. 
*/

		    i__3 = j;
		    i__4 = j;
		    q__1.r = X(j).r - csumj.r, q__1.i = X(j).i - 
			    csumj.i;
		    X(j).r = q__1.r, X(j).i = q__1.i;
		    i__3 = j;
		    xj = (r__1 = X(j).r, fabs(r__1)) + (r__2 = X(j).i,
                           fabs(r__2));
		    if (nounit) {
			r_cnjg(&q__2, &A(j,j));
			q__1.r = tscal * q__2.r, q__1.i = tscal * q__2.i;
			tjjs.r = q__1.r, tjjs.i = q__1.i;
		    } else {
			tjjs.r = tscal, tjjs.i = 0.f;
			if (tscal == 1.f) {
			    goto L185;
			}
		    }

/*                    Compute x(j) = x(j) / A(j,j), scaling if necessary. */

		    tjj = (r__1 = tjjs.r, fabs(r__1)) + (r__2 = tjjs.i,
			     fabs(r__2));
		    if (tjj > smlnum) {

/*                       abs(A(j,j)) > SMLNUM: */

			if (tjj < 1.f) {
			    if (xj > tjj * bignum) {

/*                             Scale X by 1/abs(x(j)). */

				rec = 1.f / xj;
				csscal(n__, rec, &X(1), c__1);
				*scale *= rec;
				xmax *= rec;
			    }
			}
			i__3 = j;
			cladiv(&q__1, X(j), tjjs);
			X(j).r = q__1.r, X(j).i = q__1.i;
		    } else if (tjj > 0.f) {

/*                       0 < abs(A(j,j)) <= SMLNUM: */

			if (xj > tjj * bignum) {

/*                          Scale x by (1/abs(x(j)))*abs(A(j,j))*BIGNUM. */

			    rec = tjj * bignum / xj;
			    csscal(n__, rec, &X(1), c__1);
			    *scale *= rec;
			    xmax *= rec;
			}
			i__3 = j;
			cladiv(&q__1, X(j), tjjs);
			X(j).r = q__1.r, X(j).i = q__1.i;
		    } else {

/*                       A(j,j) = 0:  Set x(1:n) = 0, x(j) = 1, and   
                         scale = 0 and compute a solution to A**H *x = 0. */

			i__3 = n__;
			for (i = 1; i <= n__; ++i) {
			    i__4 = i;
			    X(i).r = 0.f, X(i).i = 0.f;
			}
			i__3 = j;
			X(j).r = 1.f, X(j).i = 0.f;
			*scale = 0.f;
			xmax = 0.f;
		    }
L185:
		    ;
		} else {

/*                 Compute x(j) := x(j) / A(j,j) - CSUMJ if the dot   
                   product has already been divided by 1/A(j,j). */

		    i__3 = j;
		    cladiv(&q__2, X(j), tjjs);
		    q__1.r = q__2.r - csumj.r, q__1.i = q__2.i - csumj.i;
		    X(j).r = q__1.r, X(j).i = q__1.i;
		}
/* Computing MAX */
		i__3 = j;
		r__3 = xmax, r__4 = (r__1 = X(j).r, fabs(r__1)) + (r__2 = 
			X(j).i, fabs(r__2));
		xmax = max(r__3,r__4);
	    }
	}
	*scale /= tscal;
    }

/*     Scale the column norms by 1/TSCAL for return. */

    if (tscal != 1.f) {
	r__1 = 1.f / tscal;
	sscal(n__, r__1, &CNORM(1), c__1);
    }

    return ;



} 

