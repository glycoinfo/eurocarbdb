

#include "complex.h"
#include "mathtool.h"

void clasr(char *side, char *pivot, char *direct, int m__,
	   int n__, float *c, float *s, fcomplex *a, int lda)
{
/*  -- LAPACK auxiliary routine (version 2.0) --   
       Univ. of Tennessee, Univ. of California Berkeley, NAG Ltd.,   
       Courant Institute, Argonne National Lab, and Rice University   
       October 31, 1992   


    Purpose   
    =======   

    CLASR   performs the transformation   

       A := P*A,   when SIDE = 'L' or 'l'  (  Left-hand side )   

       A := A*P',  when SIDE = 'R' or 'r'  ( Right-hand side )   

    where A is an m by n complex matrix and P is an orthogonal matrix,   
    consisting of a sequence of plane rotations determined by the   
    parameters PIVOT and DIRECT as follows ( z = m when SIDE = 'L' or 'l' 
  
    and z = n when SIDE = 'R' or 'r' ):   

    When  DIRECT = 'F' or 'f'  ( Forward sequence ) then   

       P = P( z - 1 )*...*P( 2 )*P( 1 ),   

    and when DIRECT = 'B' or 'b'  ( Backward sequence ) then   

       P = P( 1 )*P( 2 )*...*P( z - 1 ),   

    where  P( k ) is a plane rotation matrix for the following planes:   

       when  PIVOT = 'V' or 'v'  ( Variable pivot ),   
          the plane ( k, k + 1 )   

       when  PIVOT = 'T' or 't'  ( Top pivot ),   
          the plane ( 1, k + 1 )   

       when  PIVOT = 'B' or 'b'  ( Bottom pivot ),   
          the plane ( k, z )   

    c( k ) and s( k )  must contain the  cosine and sine that define the 
  
    matrix  P( k ).  The two by two plane rotation part of the matrix   
    P( k ), R( k ), is assumed to be of the form   

       R( k ) = (  c( k )  s( k ) ).   
                ( -s( k )  c( k ) )   

    Arguments   
    =========   

    SIDE    (input) CHARACTER*1   
            Specifies whether the plane rotation matrix P is applied to   
            A on the left or the right.   
            = 'L':  Left, compute A := P*A   
            = 'R':  Right, compute A:= A*P'   

    DIRECT  (input) CHARACTER*1   
            Specifies whether P is a forward or backward sequence of   
            plane rotations.   
            = 'F':  Forward, P = P( z - 1 )*...*P( 2 )*P( 1 )   
            = 'B':  Backward, P = P( 1 )*P( 2 )*...*P( z - 1 )   

    PIVOT   (input) CHARACTER*1   
            Specifies the plane for which P(k) is a plane rotation   
            matrix.   
            = 'V':  Variable pivot, the plane (k,k+1)   
            = 'T':  Top pivot, the plane (1,k+1)   
            = 'B':  Bottom pivot, the plane (k,z)   

    M       (input) INTEGER   
            The number of rows of the matrix A.  If m <= 1, an immediate 
  
            return is effected.   

    N       (input) INTEGER   
            The number of columns of the matrix A.  If n <= 1, an   
            immediate return is effected.   

    C, S    (input) REAL arrays, dimension   
                    (M-1) if SIDE = 'L'   
                    (N-1) if SIDE = 'R'   
            c(k) and s(k) contain the cosine and sine that define the   
            matrix P(k).  The two by two plane rotation part of the   
            matrix P(k), R(k), is assumed to be of the form   
            R( k ) = (  c( k )  s( k ) ).   
                     ( -s( k )  c( k ) )   

    A       (input/output) COMPLEX array, dimension (LDA,N)   
            The m by n matrix A.  On exit, A is overwritten by P*A if   
            SIDE = 'R' or by A*P' if SIDE = 'L'.   

    LDA     (input) INTEGER   
            The leading dimension of the array A.  LDA >= max(1,M).   

    ===================================================================== 
  


       Test the input parameters   

    
   Parameter adjustments   
       Function Body */
    /* System generated locals */
    int a_dim1, i__1, i__2, i__3, i__4;
    fcomplex q__1, q__2, q__3;
    /* Local variables */
    static int info;
    static fcomplex temp;
    static int i, j;
    static float ctemp, stemp;


#define C(I) c[(I)-1]
#define S(I) s[(I)-1]

#define A(I,J) a[(I)-1 + ((J)-1)* ( lda)]

    info = 0;
    if (! (lsame(side, "L") || lsame(side, "R"))) {
	info = 1;
    } else if (! (lsame(pivot, "V") || lsame(pivot, "T") || 
	    lsame(pivot, "B"))) {
	info = 2;
    } else if (! (lsame(direct, "F") || lsame(direct, "B")))
	     {
	info = 3;
    } else if (m__ < 0) {
	info = 4;
    } else if (n__ < 0) {
	info = 5;
    } else if (lda < max(1,m__)) {
	info = 9;
    }
    if (info != 0) {
	return ;
    }

    /*
     *     Quick return if possible 
     */

    if (m__ == 0 || n__ == 0) {
	return ;
    }
    if (lsame(side, "L")) {

        /* 
         *       Form  P * A 
         */

	if (lsame(pivot, "V")) {
	    if (lsame(direct, "F")) {
		i__1 = m__ - 1;
		for (j = 1; j <= m__-1; ++j) {
		    ctemp = C(j);
		    stemp = S(j);
		    if (ctemp != 1.f || stemp != 0.f) {
			i__2 = n__;
			for (i = 1; i <= n__; ++i) {
			    i__3 = j + 1 + i * a_dim1;
			    temp.r = A(j+1,i).r, temp.i = A(j+1,i).i;
			    i__3 = j + 1 + i * a_dim1;
			    q__2.r = ctemp * temp.r, q__2.i = ctemp * temp.i;
			    i__4 = j + i * a_dim1;
			    q__3.r = stemp * A(j,i).r, q__3.i = stemp * A(j,i).i;
			    q__1.r = q__2.r - q__3.r, q__1.i = q__2.i - 
				    q__3.i;
			    A(j+1,i).r = q__1.r, A(j+1,i).i = q__1.i;
			    i__3 = j + i * a_dim1;
			    q__2.r = stemp * temp.r, q__2.i = stemp * temp.i;
			    i__4 = j + i * a_dim1;
			    q__3.r = ctemp * A(j,i).r, q__3.i = ctemp * A(j,i).i;
			    q__1.r = q__2.r + q__3.r, q__1.i = q__2.i + 
				    q__3.i;
			    A(j,i).r = q__1.r, A(j,i).i = q__1.i;
			}
		    }
		}
	    } else if (lsame(direct, "B")) {
		for (j = m__ - 1; j >= 1; --j) {
		    ctemp = C(j);
		    stemp = S(j);
		    if (ctemp != 1.f || stemp != 0.f) {
			i__1 = n__;
			for (i = 1; i <= n__; ++i) {
			    i__2 = j + 1 + i * a_dim1;
			    temp.r = A(j+1,i).r, temp.i = A(j+1,i).i;
			    i__2 = j + 1 + i * a_dim1;
			    q__2.r = ctemp * temp.r, q__2.i = ctemp * temp.i;
			    i__3 = j + i * a_dim1;
			    q__3.r = stemp * A(j,i).r, q__3.i = stemp * A(j,i).i;
			    q__1.r = q__2.r - q__3.r, q__1.i = q__2.i - 
				    q__3.i;
			    A(j+1,i).r = q__1.r, A(j+1,i).i = q__1.i;
			    i__2 = j + i * a_dim1;
			    q__2.r = stemp * temp.r, q__2.i = stemp * temp.i;
			    i__3 = j + i * a_dim1;
			    q__3.r = ctemp * A(j,i).r, q__3.i = ctemp * A(j,i).i;
			    q__1.r = q__2.r + q__3.r, q__1.i = q__2.i + 
				    q__3.i;
			    A(j,i).r = q__1.r, A(j,i).i = q__1.i;
			}
		    }
		}
	    }
	} else if (lsame(pivot, "T")) {
	    if (lsame(direct, "F")) {
		i__1 = m__;
		for (j = 2; j <= m__; ++j) {
		    ctemp = C(j - 1);
		    stemp = S(j - 1);
		    if (ctemp != 1.f || stemp != 0.f) {
			i__2 = n__;
			for (i = 1; i <= n__; ++i) {
			    i__3 = j + i * a_dim1;
			    temp.r = A(j,i).r, temp.i = A(j,i).i;
			    i__3 = j + i * a_dim1;
			    q__2.r = ctemp * temp.r, q__2.i = ctemp * temp.i;
			    i__4 = i * a_dim1 + 1;
			    q__3.r = stemp * A(1,i).r, q__3.i = stemp * A(1,i).i;
			    q__1.r = q__2.r - q__3.r, q__1.i = q__2.i - 
				    q__3.i;
			    A(j,i).r = q__1.r, A(j,i).i = q__1.i;
			    i__3 = i * a_dim1 + 1;
			    q__2.r = stemp * temp.r, q__2.i = stemp * temp.i;
			    i__4 = i * a_dim1 + 1;
			    q__3.r = ctemp * A(1,i).r, q__3.i = ctemp * A(1,i).i;
			    q__1.r = q__2.r + q__3.r, q__1.i = q__2.i + 
				    q__3.i;
			    A(1,i).r = q__1.r, A(1,i).i = q__1.i;
			}
		    }
		}
	    } else if (lsame(direct, "B")) {
		for (j = m__; j >= 2; --j) {
		    ctemp = C(j - 1);
		    stemp = S(j - 1);
		    if (ctemp != 1.f || stemp != 0.f) {
			i__1 = n__;
			for (i = 1; i <= n__; ++i) {
			    i__2 = j + i * a_dim1;
			    temp.r = A(j,i).r, temp.i = A(j,i).i;
			    i__2 = j + i * a_dim1;
			    q__2.r = ctemp * temp.r, q__2.i = ctemp * temp.i;
			    i__3 = i * a_dim1 + 1;
			    q__3.r = stemp * A(1,i).r, q__3.i = stemp * A(1,i).i;
			    q__1.r = q__2.r - q__3.r, q__1.i = q__2.i - 
				    q__3.i;
			    A(j,i).r = q__1.r, A(j,i).i = q__1.i;
			    i__2 = i * a_dim1 + 1;
			    q__2.r = stemp * temp.r, q__2.i = stemp * temp.i;
			    i__3 = i * a_dim1 + 1;
			    q__3.r = ctemp * A(1,i).r, q__3.i = ctemp * A(1,i).i;
			    q__1.r = q__2.r + q__3.r, q__1.i = q__2.i + 
				    q__3.i;
			    A(1,i).r = q__1.r, A(1,i).i = q__1.i;
			}
		    }
		}
	    }
	} else if (lsame(pivot, "B")) {
	    if (lsame(direct, "F")) {
		i__1 = m__ - 1;
		for (j = 1; j <= m__-1; ++j) {
		    ctemp = C(j);
		    stemp = S(j);
		    if (ctemp != 1.f || stemp != 0.f) {
			i__2 = n__;
			for (i = 1; i <= n__; ++i) {
			    i__3 = j + i * a_dim1;
			    temp.r = A(j,i).r, temp.i = A(j,i).i;
			    i__3 = j + i * a_dim1;
			    i__4 = m__ + i * a_dim1;
			    q__2.r = stemp * A(m__,i).r, q__2.i = stemp * A(m__,i).i;
			    q__3.r = ctemp * temp.r, q__3.i = ctemp * temp.i;
			    q__1.r = q__2.r + q__3.r, q__1.i = q__2.i + 
				    q__3.i;
			    A(j,i).r = q__1.r, A(j,i).i = q__1.i;
			    i__3 = m__ + i * a_dim1;
			    i__4 = m__ + i * a_dim1;
			    q__2.r = ctemp * A(m__,i).r, q__2.i = ctemp * A(m__,i).i;
			    q__3.r = stemp * temp.r, q__3.i = stemp * temp.i;
			    q__1.r = q__2.r - q__3.r, q__1.i = q__2.i - 
				    q__3.i;
			    A(m__,i).r = q__1.r, A(m__,i).i = q__1.i;
			}
		    }
		}
	    } else if (lsame(direct, "B")) {
		for (j = m__ - 1; j >= 1; --j) {
		    ctemp = C(j);
		    stemp = S(j);
		    if (ctemp != 1.f || stemp != 0.f) {
			i__1 = n__;
			for (i = 1; i <= n__; ++i) {
			    i__2 = j + i * a_dim1;
			    temp.r = A(j,i).r, temp.i = A(j,i).i;
			    i__2 = j + i * a_dim1;
			    i__3 = m__ + i * a_dim1;
			    q__2.r = stemp * A(m__,i).r, q__2.i = stemp * A(m__,i).i;
			    q__3.r = ctemp * temp.r, q__3.i = ctemp * temp.i;
			    q__1.r = q__2.r + q__3.r, q__1.i = q__2.i + 
				    q__3.i;
			    A(j,i).r = q__1.r, A(j,i).i = q__1.i;
			    i__2 = m__ + i * a_dim1;
			    i__3 = m__ + i * a_dim1;
			    q__2.r = ctemp * A(m__,i).r, q__2.i = ctemp * A(m__,i).i;
			    q__3.r = stemp * temp.r, q__3.i = stemp * temp.i;
			    q__1.r = q__2.r - q__3.r, q__1.i = q__2.i - 
				    q__3.i;
			    A(m__,i).r = q__1.r, A(m__,i).i = q__1.i;
			}
		    }
		}
	    }
	}
    } else if (lsame(side, "R")) {

        /*
         *        Form A * P' 
         */

	if (lsame(pivot, "V")) {
	    if (lsame(direct, "F")) {
		i__1 = n__ - 1;
		for (j = 1; j <= n__-1; ++j) {
		    ctemp = C(j);
		    stemp = S(j);
		    if (ctemp != 1.f || stemp != 0.f) {
			i__2 = m__;
			for (i = 1; i <= m__; ++i) {
			    i__3 = i + (j + 1) * a_dim1;
			    temp.r = A(i,j+1).r, temp.i = A(i,j+1).i;
			    i__3 = i + (j + 1) * a_dim1;
			    q__2.r = ctemp * temp.r, q__2.i = ctemp * temp.i;
			    i__4 = i + j * a_dim1;
			    q__3.r = stemp * A(i,j).r, q__3.i = stemp * A(i,j).i;
			    q__1.r = q__2.r - q__3.r, q__1.i = q__2.i - 
				    q__3.i;
			    A(i,j+1).r = q__1.r, A(i,j+1).i = q__1.i;
			    i__3 = i + j * a_dim1;
			    q__2.r = stemp * temp.r, q__2.i = stemp * temp.i;
			    i__4 = i + j * a_dim1;
			    q__3.r = ctemp * A(i,j).r, q__3.i = ctemp * A(i,j).i;
			    q__1.r = q__2.r + q__3.r, q__1.i = q__2.i + 
				    q__3.i;
			    A(i,j).r = q__1.r, A(i,j).i = q__1.i;
			}
		    }
		}
	    } else if (lsame(direct, "B")) {
		for (j = n__ - 1; j >= 1; --j) {
		    ctemp = C(j);
		    stemp = S(j);
		    if (ctemp != 1.f || stemp != 0.f) {
			i__1 = m__;
			for (i = 1; i <= m__; ++i) {
			    i__2 = i + (j + 1) * a_dim1;
			    temp.r = A(i,j+1).r, temp.i = A(i,j+1).i;
			    i__2 = i + (j + 1) * a_dim1;
			    q__2.r = ctemp * temp.r, q__2.i = ctemp * temp.i;
			    i__3 = i + j * a_dim1;
			    q__3.r = stemp * A(i,j).r, q__3.i = stemp * A(i,j).i;
			    q__1.r = q__2.r - q__3.r, q__1.i = q__2.i - 
				    q__3.i;
			    A(i,j+1).r = q__1.r, A(i,j+1).i = q__1.i;
			    i__2 = i + j * a_dim1;
			    q__2.r = stemp * temp.r, q__2.i = stemp * temp.i;
			    i__3 = i + j * a_dim1;
			    q__3.r = ctemp * A(i,j).r, q__3.i = ctemp * A(i,j).i;
			    q__1.r = q__2.r + q__3.r, q__1.i = q__2.i + 
				    q__3.i;
			    A(i,j).r = q__1.r, A(i,j).i = q__1.i;
			}
		    }
		}
	    }
	} else if (lsame(pivot, "T")) {
	    if (lsame(direct, "F")) {
		i__1 = n__;
		for (j = 2; j <= n__; ++j) {
		    ctemp = C(j - 1);
		    stemp = S(j - 1);
		    if (ctemp != 1.f || stemp != 0.f) {
			i__2 = m__;
			for (i = 1; i <= m__; ++i) {
			    i__3 = i + j * a_dim1;
			    temp.r = A(i,j).r, temp.i = A(i,j).i;
			    i__3 = i + j * a_dim1;
			    q__2.r = ctemp * temp.r, q__2.i = ctemp * temp.i;
			    i__4 = i + a_dim1;
			    q__3.r = stemp * A(i,1).r, q__3.i = stemp * A(i,1).i;
			    q__1.r = q__2.r - q__3.r, q__1.i = q__2.i - 
				    q__3.i;
			    A(i,j).r = q__1.r, A(i,j).i = q__1.i;
			    i__3 = i + a_dim1;
			    q__2.r = stemp * temp.r, q__2.i = stemp * temp.i;
			    i__4 = i + a_dim1;
			    q__3.r = ctemp * A(i,1).r, q__3.i = ctemp * A(i,1).i;
			    q__1.r = q__2.r + q__3.r, q__1.i = q__2.i + 
				    q__3.i;
			    A(i,1).r = q__1.r, A(i,1).i = q__1.i;
			}
		    }
		}
	    } else if (lsame(direct, "B")) {
		for (j = n__; j >= 2; --j) {
		    ctemp = C(j - 1);
		    stemp = S(j - 1);
		    if (ctemp != 1.f || stemp != 0.f) {
			i__1 = m__;
			for (i = 1; i <= m__; ++i) {
			    i__2 = i + j * a_dim1;
			    temp.r = A(i,j).r, temp.i = A(i,j).i;
			    i__2 = i + j * a_dim1;
			    q__2.r = ctemp * temp.r, q__2.i = ctemp * temp.i;
			    i__3 = i + a_dim1;
			    q__3.r = stemp * A(i,1).r, q__3.i = stemp * A(i,1).i;
			    q__1.r = q__2.r - q__3.r, q__1.i = q__2.i - 
				    q__3.i;
			    A(i,j).r = q__1.r, A(i,j).i = q__1.i;
			    i__2 = i + a_dim1;
			    q__2.r = stemp * temp.r, q__2.i = stemp * temp.i;
			    i__3 = i + a_dim1;
			    q__3.r = ctemp * A(i,1).r, q__3.i = ctemp * A(i,1).i;
			    q__1.r = q__2.r + q__3.r, q__1.i = q__2.i + 
				    q__3.i;
			    A(i,1).r = q__1.r, A(i,1).i = q__1.i;
			}
		    }
		}
	    }
	} else if (lsame(pivot, "B")) {
	    if (lsame(direct, "F")) {
		i__1 = n__ - 1;
		for (j = 1; j <= n__-1; ++j) {
		    ctemp = C(j);
		    stemp = S(j);
		    if (ctemp != 1.f || stemp != 0.f) {
			i__2 = m__;
			for (i = 1; i <= m__; ++i) {
			    i__3 = i + j * a_dim1;
			    temp.r = A(i,j).r, temp.i = A(i,j).i;
			    i__3 = i + j * a_dim1;
			    i__4 = i + n__ * a_dim1;
			    q__2.r = stemp * A(i,n__).r, q__2.i = stemp * A(i,n__).i;
			    q__3.r = ctemp * temp.r, q__3.i = ctemp * temp.i;
			    q__1.r = q__2.r + q__3.r, q__1.i = q__2.i + 
				    q__3.i;
			    A(i,j).r = q__1.r, A(i,j).i = q__1.i;
			    i__3 = i + n__ * a_dim1;
			    i__4 = i + n__ * a_dim1;
			    q__2.r = ctemp * A(i,n__).r, q__2.i = ctemp * A(i,n__).i;
			    q__3.r = stemp * temp.r, q__3.i = stemp * temp.i;
			    q__1.r = q__2.r - q__3.r, q__1.i = q__2.i - 
				    q__3.i;
			    A(i,n__).r = q__1.r, A(i,n__).i = q__1.i;
			}
		    }
		}
	    } else if (lsame(direct, "B")) {
		for (j = n__ - 1; j >= 1; --j) {
		    ctemp = C(j);
		    stemp = S(j);
		    if (ctemp != 1.f || stemp != 0.f) {
			i__1 = m__;
			for (i = 1; i <= m__; ++i) {
			    i__2 = i + j * a_dim1;
			    temp.r = A(i,j).r, temp.i = A(i,j).i;
			    i__2 = i + j * a_dim1;
			    i__3 = i + n__ * a_dim1;
			    q__2.r = stemp * A(i,n__).r, q__2.i = stemp * A(i,n__).i;
			    q__3.r = ctemp * temp.r, q__3.i = ctemp * temp.i;
			    q__1.r = q__2.r + q__3.r, q__1.i = q__2.i + 
				    q__3.i;
			    A(i,j).r = q__1.r, A(i,j).i = q__1.i;
			    i__2 = i + n__ * a_dim1;
			    i__3 = i + n__ * a_dim1;
			    q__2.r = ctemp * A(i,n__).r, q__2.i = ctemp * A(i,n__).i;
			    q__3.r = stemp * temp.r, q__3.i = stemp * temp.i;
			    q__1.r = q__2.r - q__3.r, q__1.i = q__2.i - 
				    q__3.i;
			    A(i,n__).r = q__1.r, A(i,n__).i = q__1.i;
			}
		    }
		}
	    }
	}
    }


} 

