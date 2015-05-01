

#include <math.h>
#include "mathtool.h"


/*
c
c     this subroutine is a translation of the algol procedure
c     cbalance, which is a complex version of balance,
c     num. math. 13, 293-304(1969) by parlett and reinsch.
c     handbook for auto. comp., vol.ii-linear algebra, 315-326(1971).
c
c     this subroutine balances a complex matrix and isolates
c     eigenvalues whenever possible.
c
c     on input
c
c        nm must be set to the row dimension of two-dimensional
c          array parameters as declared in the calling program
c          dimension statement.
c
c        n is the order of the matrix.
c
c        ar and ai contain the real and imaginary parts,
c          respectively, of the complex matrix to be balanced.
c
c     on output
c
c        ar and ai contain the real and imaginary parts,
c          respectively, of the balanced matrix.
c
c        low and igh are two integers such that ar(i,j) and ai(i,j)
c          are equal to zero if
c           (1) i is greater than j and
c           (2) j=1,...,low-1 or i=igh+1,...,n.
c
c        scale contains information determining the
c           permutations and scaling factors used.
c
c     suppose that the principal submatrix in rows low through igh
c     has been balanced, that p(j) denotes the index interchanged
c     with j during the permutation step, and that the elements
c     of the diagonal matrix used are denoted by d(i,j).  then
c        scale(j) = p(j),    for j = 1,...,low-1
c                 = d(j,j)       j = low,...,igh
c                 = p(j)         j = igh+1,...,n.
c     the order in which the interchanges are made is n to igh+1,
c     then 1 to low-1.
c
c     note that 1 is returned for igh if igh is zero formally.
c
c     the algol procedure exc contained in cbalance appears in
c     cbal  in line.  (note that the algol roles of identifiers
c     k,l have been reversed.)
c
c     arithmetic is real throughout.
c
c     questions and comments should be directed to burton s. garbow,
c     mathematics and computer science div, argonne national laboratory
c
c     this version dated august 1983.
c
c     ------------------------------------------------------------------
c
*/
#define SCALE(I) scale[(I)-1]
#define AR(I,J) ar[(I)-1 + ((J)-1)* (nm)]
#define AI(I,J) ai[(I)-1 + ((J)-1)* (nm)]

void cbal(int nm, int n, double *ar, double *ai, int *low, int *igh,
          double *scale)
{
    int i,j,k,l,m,jj,iexc;
/*    double  ar(nm,n),ai(nm,n),scale(n)*/
    double  c,f,g,r,s,b2,radix;
    int noconv;

    radix = 16.0;

    b2 = radix * radix;
    k = 1;
    l = n;
    goto L100;
    /*
     *     .......... in-line procedure for row and
     *                column exchange ..........
     */
  L20:
    SCALE(m) = j;
    if (j != m) {

        for (i = 1;i<= l;i++) {
            f = AR(i,j);
            AR(i,j) = AR(i,m);
            AR(i,m) = f;
            f = AI(i,j);
            AI(i,j) = AI(i,m);
            AI(i,m) = f;
        }

        for (i = k;i<= n;i++) {
            f = AR(j,i);
            AR(j,i) = AR(m,i);
            AR(m,i) = f;
            f = AI(j,i);
            AI(j,i) = AI(m,i);
            AI(m,i) = f;
        }

    } 
    if (iexc == 2) {
        k++;
        goto L130;
    }
    /*
     *     .......... search for rows isolating an eigenvalue
     *                and push them down ..........
     */
    if (l == 1) 
        goto L280;
    l--;
    /*
     *     .......... for j=l step -1 until 1 do -- ..........
     */
  L100:
    for (jj = 1;jj<= l; jj++) {
        int test = FALSE;
        j = l + 1 - jj;

        for (i = 1;i<= l;i++) {
            if (i == j) 
                continue;
            if (AR(j,i) != 0.0 || AI(j,i) != 0.0) {
                test = TRUE;
                break; 
            }
        }
        if (!test) {
            m = l;
            iexc = 1;
            goto L20;
        }
    } 

    /*
     *     .......... search for columns isolating an eigenvalue
     *                and push them left ..........
     */
  L130:
    for (j = k;j<= l;j++) {
        int test = FALSE;
        for ( i = k; i <= l; i++) { 
            if (i == j) 
                continue; 
            if (AR(i,j) != 0.0 || AI(i,j) != 0.0) {
                test = TRUE;
                break;
            }
        } 
        if (!test) {
            m = k;
            iexc = 2;
            goto L20;
        }
    }
    /*
     *     .......... now balance the submatrix in rows k to l ..........
     */
    for ( i = k;i<= l;i++)
        SCALE(i) = 1.0;
    /*
     *     .......... iterative loop for norm reduction ..........
     */
    do {
        noconv = FALSE;

        for (i = k;i<= l;i++) {
            c = 0.0;
            r = 0.0;

            for (j = k;j<= l;j++) {
                if (j == i) 
                    continue;
                c += fabs(AR(j,i)) + fabs(AI(j,i));
                r += fabs(AR(i,j)) + fabs(AI(i,j));
            }
            /*
             *    guard against zero c or r due to underflow ..........
             */
            if (c == 0.0 || r == 0.0) 
                continue;
            g = r / radix;
            f = 1.0;
            s = c + r;
            while (c < g) { 
                f *= radix;
                c *= b2;
            } 
            g = r * radix;
            while (c >= g) {
                f /= radix;
                c /= b2;
            }
            /*
             *     .......... now balance ..........
             */
            if ((c + r) / f >= 0.95 * s) 
                continue;
            g = 1.0 / f;
            SCALE(i) *= f;
            noconv = TRUE;

            for (j = k;j<= n;j++) {
                AR(i,j) *=  g;
                AI(i,j) *=  g;
            }

            for (j = 1; j<= l; j++) {
                AR(j,i) *= f;
                AI(j,i) *= f;
            }

        } 

    } while (noconv);
  L280 :
    *low = k;
    *igh = l;
}






