

#include "complex.h"
#include "mathtool.h"

/*
c
c     zppsl solves the complex*16 hermitian positive definite system
c     a * x = b
c     using the factors computed by zppco or zppfa.
c
c     on entry
c
c        ap      complex*16 (n*(n+1)/2)
c                the output from zppco or zppfa.
c
c        n       integer
c                the order of the matrix  a .
c
c        b       complex*16(n)
c                the right hand side vector.
c
c     on return
c
c        b       the solution vector  x .
c
c     error condition
c
c        a division by zero will occur if the input factor contains
c        a zero on the diagonal.  technically this indicates
c        singularity but it is usually caused by improper subroutine
c        arguments.  it will not occur if the subroutines are called
c        correctly and  info .eq. 0 .
c
c     to compute  inverse(a) * c  where  c  is a matrix
c     with  p  columns
c           call zppco(ap,n,rcond,z,info)
c           if (rcond is too small .or. info .ne. 0) go to ...
c           do 10 j = 1, p
c              call zppsl(ap,n,c(1,j))
c        10 continue
c
c     linpack.  this version dated 08/14/78 .
c     cleve moler, university of new mexico, argonne national lab.
c
c     subroutines and functions
c
c     blas zaxpy,zdotc
*/

void zppsl(dcomplex *ap, int n, dcomplex *b)
{
    dcomplex t;
    int k,kb,kk;

    /*
     * adjust arrays
     */
    ap--;
    b--;
    
    kk = 0;
    for (k = 1;k<= n;k++) {
        t = zdotc(k-1,ap+(kk+1),1,b+(1),1);
        kk += k;
        b[k] = Cdiv_d(Csub_d(b[k],t),ap[kk]);
    }
    for (kb = 1;kb<= n;kb++) {
        k = n + 1 - kb;
        b[k] = Cdiv_d(b[k],ap[kk]);
        kk -= k;
        t = DCmul_d(-1.0,b[k]);
        zaxpy(k-1,t,ap+(kk+1),1,b+(1),1);
    }
}



