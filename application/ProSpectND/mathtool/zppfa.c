

#include <math.h>
#include "complex.h"
#include "mathtool.h"

/*
c
c     zppfa factors a complex*16 hermitian positive definite matrix
c     stored in packed form.
c
c     zppfa is usually called by zppco, but it can be called
c     directly with a saving in time if  rcond  is not needed.
c     (time for zppco) = (1 + 18/n)*(time for zppfa) .
c
c     on entry
c
c        ap      complex*16 (n*(n+1)/2)
c                the packed form of a hermitian matrix  a .  the
c                columns of the upper triangle are stored sequentially
c                in a one-dimensional array of length  n*(n+1)/2 .
c                see comments below for details.
c
c        n       integer
c                the order of the matrix  a .
c
c     on return
c
c        ap      an upper triangular matrix  r , stored in packed
c                form, so that  a = ctrans(r)*r .
c
c        info    integer
c                = 0  for normal return.
c                = k  if the leading minor of order  k  is not
c                     positive definite.
c
c
c     packed storage
c
c          the following program segment will pack the upper
c          triangle of a hermitian matrix.
c
c                k = 0
c                do 20 j = 1, n
c                   do 10 i = 1, j
c                      k = k + 1
c                      ap(k) = a(i,j)
c             10    continue
c             20 continue
c
c     linpack.  this version dated 08/14/78 .
c     cleve moler, university of new mexico, argonne national lab.
c
c     subroutines and functions
c
c     blas zdotc
c     fortran dcmplx,dconjg,dsqrt
c
*/

void zppfa(dcomplex *ap, int n, int *info)
{
    dcomplex t;
    double s;
    int j,jj,jm1,k,kj,kk;

    /*
     * adjust array
     */
    ap--;
    
    jj = 0;
    for (j = 1;j<= n;j++) {
        *info = j;
        s     = 0.0;
        jm1   = j - 1;
        kj    = jj;
        kk    = 0;
        if (jm1 >= 1) {
            for (k = 1;k<= jm1;k++) {
                kj++;
                t = Csub_d(ap[kj], zdotc(k-1,ap+(kk+1),1,ap+(jj+1),1));
                kk += k;
                t = Cdiv_d(t,ap[kk]);
                ap[kj] = t;
                t = Cmul_d(t,Conjg_d(t));
                s += t.r;
            }
        }
        jj += j;
        s = ap[jj].r - s;
        if (s <= 0.0 || ap[jj].i != 0.0) 
            return;
        ap[jj] = Complex_d(sqrt(s),0.0);
    }
    *info = 0;
}
