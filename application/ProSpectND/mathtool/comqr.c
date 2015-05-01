
#include <math.h>
#include "mathtool.h"

/*
c
c     this subroutine is a translation of a unitary analogue of the
c     algol procedure  comlr, num. math. 12, 369-376(1968) by martin
c     and wilkinson.
c     handbook for auto. comp., vol.ii-linear algebra, 396-403(1971).
c     the unitary analogue substitutes the qr algorithm of francis
c     (comp. jour. 4, 332-345(1962)) for the lr algorithm.
c
c     this subroutine finds the eigenvalues of a complex
c     upper hessenberg matrix by the qr method.
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
c        hr and hi contain the real and imaginary parts,
c          respectively, of the complex upper hessenberg matrix.
c          their lower triangles below the subdiagonal contain
c          information about the unitary transformations used in
c          the reduction by  corth, if performed.
c
c     on output
c
c        the upper hessenberg portions of hr and hi have been
c          destroyed.  therefore, they must be saved before
c          calling  comqr  if subsequent calculation of
c          eigenvectors is to be performed.
c
c        wr and wi contain the real and imaginary parts,
c          respectively, of the eigenvalues.  if an error
c          exit is made, the eigenvalues should be correct
c          for indices ierr+1,...,n.
c
c        ierr is set to
c          zero       for normal return,
c          j          if the limit of 30*n iterations is exhausted
c                     while the j-th eigenvalue is being sought.
c
c     calls cdiv for complex division.
c     calls csroot for complex square root.
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

#define WR(I) wr[(I)-1]
#define WI(I) wi[(I)-1]
#define HR(I,J) hr[(I)-1 + ((J)-1)* (nm)]
#define HI(I,J) hi[(I)-1 + ((J)-1)* (nm)]


void comqr(int nm, int n, int low, int igh,
           double *hr, double *hi, double *wr, double *wi, int *ierr)
{
    int i,j,l,en,ll,itn,its,lp1,enm1;
/*    double hr(nm,n),hi(nm,n),wr(n),wi(n)*/
    double si,sr,ti,tr,xi,xr,yi,yr,zzi,zzr,norm,tst1,tst2;

    *ierr = 0;
    if (low != igh) { 
        /*
         *     .......... create real subdiagonal elements ..........
         */
        l = low + 1;

        for (i = l;i<=igh;i++) {
            ll = min(i+1,igh);
            if (HI(i,i-1) == 0.0) 
                continue;
            norm = pythag(HR(i,i-1),HI(i,i-1));
            yr = HR(i,i-1) / norm;
            yi = HI(i,i-1) / norm;
            HR(i,i-1) = norm;
            HI(i,i-1) = 0.0;

            for (j = i;j<= igh;j++) {
                si = yr * HI(i,j) - yi * HR(i,j);
                HR(i,j) = yr * HR(i,j) + yi * HI(i,j);
                HI(i,j) = si;
            }

            for (j = low;j<= ll;j++) {
                si = yr * HI(j,i) + yi * HR(j,i);
                HR(j,i) = yr * HR(j,i) - yi * HI(j,i);
                HI(j,i) = si;
            }

        }
    }
    /*
     *     .......... store roots isolated by cbal ..........
     */ 
    for (i = 1;i<= n;i++) {
        if (i >= low && i <= igh) 
            continue;
        WR(i) = HR(i,i);
        WI(i) = HI(i,i);
    }

    en = igh;
    tr = 0.0;
    ti = 0.0;
    itn = 30*n;
    /*
     *     .......... search for next eigenvalue ..........
     */
    while (1) {
        if (en < low) 
            return;
        its = 0;
        enm1 = en - 1;
        /*
         *     .......... look for single small sub-diagonal element
         *                for l=en step -1 until low  -- ..........
         */
        while (1) { 
            for (ll = low;ll<= en;ll++) {
                l = en + low - ll;
                if (l == low) 
                    break;
                tst1 = fabs(HR(l-1,l-1)) + fabs(HI(l-1,l-1))
                       + fabs(HR(l,l)) + fabs(HI(l,l));
                tst2 = tst1 + fabs(HR(l,l-1));
                if (tst2 == tst1) 
                    break;
            }
            /* 
             *    .......... form shift ..........
             */
            if (l == en) 
                break; 
            if (itn == 0) 
                goto L1000;
            if (its == 10 || its == 20) {
                /*
                 *     .......... form exceptional shift ..........
                 */
                sr = fabs(HR(en,enm1)) + fabs(HR(enm1,en-2));
                si = 0.0;
            }
            else {
                sr = HR(en,en);
                si = HI(en,en);
                xr = HR(enm1,en) * HR(en,enm1);
                xi = HI(enm1,en) * HR(en,enm1);
                if (xr != 0.0 || xi != 0.0) { 
                    yr = (HR(enm1,enm1) - sr) / 2.0;
                    yi = (HI(enm1,enm1) - si) / 2.0;
                    csroot(yr*yr-yi*yi+xr,2.0*yr*yi+xi,&zzr,&zzi);
                    if (yr * zzr + yi * zzi < 0.0) {
                        zzr = -zzr;
                        zzi = -zzi;
                    }
                    cdiv(xr,xi,yr+zzr,yi+zzi,&xr,&xi);
                    sr -= xr;
                    si -= xi;
                }
            }
            for (i = low;i<= en;i++) {
                HR(i,i) -= sr;
                HI(i,i) -= si;
            }

            tr += sr;
            ti += si;
            its++;
            itn--;
            /*
             *     .......... reduce to triangle (rows) ..........
             */
            lp1 = l + 1;

            for (i = lp1;i<= en;i++) {
                sr = HR(i,i-1);
                HR(i,i-1) = 0.0;
                norm = pythag(pythag(HR(i-1,i-1),HI(i-1,i-1)),sr);
                xr = HR(i-1,i-1) / norm;
                WR(i-1) = xr;
                xi = HI(i-1,i-1) / norm;
                WI(i-1) = xi;
                HR(i-1,i-1) = norm;
                HI(i-1,i-1) = 0.0;
                HI(i,i-1) = sr / norm;

                for (j = i;j<= en;j++) {
                    yr = HR(i-1,j);
                    yi = HI(i-1,j);
                    zzr = HR(i,j);
                    zzi = HI(i,j);
                    HR(i-1,j) = xr * yr + xi * yi + HI(i,i-1) * zzr;
                    HI(i-1,j) = xr * yi - xi * yr + HI(i,i-1) * zzi;
                    HR(i,j) = xr * zzr - xi * zzi - HI(i,i-1) * yr;
                    HI(i,j) = xr * zzi + xi * zzr - HI(i,i-1) * yi;
                }

            }

            si = HI(en,en);
            if (si != 0.0) {
                norm = pythag(HR(en,en),si);
                sr = HR(en,en) / norm;
                si = si / norm;
                HR(en,en) = norm;
                HI(en,en) = 0.0;
            }
            /*
             *     .......... inverse operation (columns) ..........
             */
            for (j = lp1;j<= en;j++) {
                xr = WR(j-1);
                xi = WI(j-1);

                for (i = l;i<=j;i++) {
                    yr = HR(i,j-1);
                    yi = 0.0;
                    zzr = HR(i,j);
                    zzi = HI(i,j);
                    if (i != j) {
                        yi = HI(i,j-1);
                        HI(i,j-1) = xr * yi + xi * yr + HI(j,j-1) * zzi;
                    }       
                    HR(i,j-1) = xr * yr - xi * yi + HI(j,j-1) * zzr;
                    HR(i,j) = xr * zzr + xi * zzi - HI(j,j-1) * yr;
                    HI(i,j) = xr * zzi - xi * zzr - HI(j,j-1) * yi;
                }
 
            }

            if (si == 0.0) 
                continue;

            for (i = l;i<= en;i++) {
                yr = HR(i,en);
                yi = HI(i,en);
                HR(i,en) = sr * yr - si * yi;
                HI(i,en) = sr * yi + si * yr;
            }

        } 
        /*
         *     .......... a root found ..........
         */
        WR(en) = HR(en,en) + tr;
        WI(en) = HI(en,en) + ti;
        en = enm1;
    }
    /*
     *     .......... set error -- all eigenvalues have not
     *                converged after 30*n iterations ..........
     */
 L1000:
   *ierr = en;

}








