
#include <math.h>
#include "mathtool.h"

/*
c
c     this subroutine is a translation of a complex analogue of
c     the algol procedure orthes, num. math. 12, 349-368(1968)
c     by martin and wilkinson.
c     handbook for auto. comp., vol.ii-linear algebra, 339-358(1971).
c
c     given a complex general matrix, this subroutine
c     reduces a submatrix situated in rows and columns
c     low through igh to upper hessenberg form by
c     unitary similarity transformations.
c
c     on input
c
c        nm must be set to the row dimension of two-dimensional
c          array parameters as declared in the calling program
c          dimension statement.
c
c        n is the order of the matrix.
c
c        low and igh are integers determined by the balancing
c          subroutine  cbal.  if  cbal  has not been used,
c          set low=1, igh=n.
c
c        ar and ai contain the real and imaginary parts,
c          respectively, of the complex input matrix.
c
c     on output
c
c        ar and ai contain the real and imaginary parts,
c          respectively, of the hessenberg matrix.  information
c          about the unitary transformations used in the reduction
c          is stored in the remaining triangles under the
c          hessenberg matrix.
c
c        ortr and orti contain further information about the
c          transformations.  only elements low through igh are used.
c
c     calls pythag for  dsqrt(a*a + b*b) .
c
c     questions and comments should be directed to burton s. garbow,
c     mathematics and computer science div, argonne national laboratory
c
c     this version dated august 1983.
c
c     ------------------------------------------------------------------
c
*/

#define ORTR(I) ortr[(I)-1]
#define ORTI(I) orti[(I)-1]
#define AR(I,J) ar[(I)-1 + ((J)-1)* (nm)]
#define AI(I,J) ai[(I)-1 + ((J)-1)* (nm)]

void corth(int nm, int n, int low, int igh,
           double *ar, double *ai, double *ortr, double *orti)
{
    int i,j,m,ii,jj,la,mp,kp1;
/*    double  ar(nm,n),ai(nm,n),ortr(igh),orti(igh);*/
    double  f,g,h,fi,fr,scale;

    la  = igh - 1;
    kp1 = low + 1;
    if (la < kp1) 
        return;

    for (m = kp1;m <= la;m++) {
        h = 0.0;
        ORTR(m) = 0.0;
        ORTI(m) = 0.0;
        scale = 0.0;
        /*
         *     .......... scale column (algol tol then not needed) ..........
         */
        for (i = m;i<= igh;i++) 
            scale += fabs(AR(i,m-1)) + fabs(AI(i,m-1));

        if (scale == 0.0) 
            continue;
        mp = m + igh;
        /*
         *  .......... for i=igh step -1 until m do -- ..........
         */
        for (ii = m;ii<= igh;ii++) {
            i = mp - ii;
            ORTR(i) = AR(i,m-1) / scale;
            ORTI(i) = AI(i,m-1) / scale;
            h = h + ORTR(i) * ORTR(i) + ORTI(i) * ORTI(i);
        }

        g = sqrt(h);
        f = pythag(ORTR(m),ORTI(m));
        if (f != 0.0) {
            h += f * g;
            g /= f;
            ORTR(m) = (1.0 + g) * ORTR(m);
            ORTI(m) = (1.0 + g) * ORTI(m);
        }
        else {
            ORTR(m) = g;
            AR(m,m-1) = scale;
        }   
        /*
         *     .......... form (i-(u*ut)/h) * a ..........
         */
        for (j = m;j<= n;j++) {
            fr = 0.0;
            fi = 0.0;
            /*
             *     .......... for i=igh step -1 until m do -- ..........
             */
            for (ii = m;ii<= igh;ii++) {
                i = mp - ii;
                fr = fr + ORTR(i) * AR(i,j) + ORTI(i) * AI(i,j);
                fi = fi + ORTR(i) * AI(i,j) - ORTI(i) * AR(i,j);
            }

            fr /= h;
            fi /= h;

            for (i = m;i<= igh;i++) {
                AR(i,j) = AR(i,j) - fr * ORTR(i) + fi * ORTI(i);
                AI(i,j) = AI(i,j) - fr * ORTI(i) - fi * ORTR(i);
            }

        }
        /*
         *     .......... form (i-(u*ut)/h)*a*(i-(u*ut)/h) ..........
         */
        for (i = 1;i<= igh;i++) {
            fr = 0.0;
            fi = 0.0;
            /*
             *     .......... for j=igh step -1 until m do -- ..........
             */
            for (jj = m;jj<=igh;jj++) {
                j = mp - jj;
                fr = fr + ORTR(j) * AR(i,j) - ORTI(j) * AI(i,j);
                fi = fi + ORTR(j) * AI(i,j) + ORTI(j) * AR(i,j);
            }

            fr /= h;
            fi /= h;

            for (j = m;j<=igh;j++) {
                AR(i,j) = AR(i,j) - fr * ORTR(j) - fi * ORTI(j);
                AI(i,j) = AI(i,j) + fr * ORTI(j) - fi * ORTR(j);
            }

         }

         ORTR(m) = scale * ORTR(m);
         ORTI(m) = scale * ORTI(m);
         AR(m,m-1) = -g * AR(m,m-1);
         AI(m,m-1) = -g * AI(m,m-1);
    }
}



