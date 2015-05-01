#include <stdio.h>
#include <math.h>
#include "complex.h"
#include "mathtool.h"



static double dmax1(double a, double b, double c, double e, double f)
{
    double d;
    d = max(a,b);
    d = max(d,c);
    d = max(d,e);
    d = max(d,f);
    return d;
}


static dcomplex zinv(dcomplex e)
{
    dcomplex one = Complex_d(1.0,0.0);
    return Cdiv_d(one, e);
}


static double cabs1(dcomplex e)
{
    return fabs(e.i)+fabs(e.r);
}





/*
      complex*16 csign,zdum,zdum1,zdum2
      double precision cabs1
      double precision dreal,dimag
      complex*16 zdumr,zdumi
      dreal(zdumr) = zdumr
      dimag(zdumi) = (0.0d0,-1.0d0)*zdumi
      cabs1(zdum) = dabs(dreal(zdum)) + dabs(dimag(zdum))
      csign(zdum1,zdum2) = cdabs(zdum1)*(zdum2/cdabs(zdum2))
*/
/*
c
c
c     zsvdc is a subroutine to reduce a complex*16 nxp matrix x by
c     unitary transformations u and v to diagonal form.  the
c     diagonal elements s(i) are the singular values of x.  the
c     columns of u are the corresponding left singular vectors,
c     and the columns of v the right singular vectors.
c
c     on entry
c
c         x         complex*16(ldx,p), where ldx.ge.n.
c                   x contains the matrix whose singular value
c                   decomposition is to be computed.  x is
c                   destroyed by zsvdc.
c
c         ldx       integer.
c                   ldx is the leading dimension of the array x.
c
c         n         integer.
c                   n is the number of rows of the matrix x.
c
c         p         integer.
c                   p is the number of columns of the matrix x.
c
c         ldu       integer.
c                   ldu is the leading dimension of the array u
c                   (see below).
c
c         ldv       integer.
c                   ldv is the leading dimension of the array v
c                   (see below).
c
c         work      complex*16(n).
c                   work is a scratch array.
c
c         job       integer.
c                   job controls the computation of the singular
c                   vectors.  it has the decimal expansion ab
c                   with the following meaning
c
c                        a.eq.0    do not compute the left singular
c                                  vectors.
c                        a.eq.1    return the n left singular vectors
c                                  in u.
c                        a.ge.2    returns the first min(n,p)
c                                  left singular vectors in u.
c                        b.eq.0    do not compute the right singular
c                                  vectors.
c                        b.eq.1    return the right singular vectors
c                                  in v.
c
c     on return
c
c         s         complex*16(mm), where mm=min(n+1,p).
c                   the first min(n,p) entries of s contain the
c                   singular values of x arranged in descending
c                   order of magnitude.
c
c         e         complex*16(p).
c                   e ordinarily contains zeros.  however see the
c                   discussion of info for exceptions.
c
c         u         complex*16(ldu,k), where ldu.ge.n.  if joba.eq.1
c                                   then k.eq.n, if joba.ge.2 then
c
c                                   k.eq.min(n,p).
c                   u contains the matrix of left singular vectors.
c                   u is not referenced if joba.eq.0.  if n.le.p
c                   or if joba.gt.2, then u may be identified with x
c                   in the subroutine call.
c
c         v         complex*16(ldv,p), where ldv.ge.p.
c                   v contains the matrix of right singular vectors.
c                   v is not referenced if jobb.eq.0.  if p.le.n,
c                   then v may be identified whth x in the
c                   subroutine call.
c
c         info      integer.
c                   the singular values (and their corresponding
c                   singular vectors) s(info+1),s(info+2),...,s(m)
c                   are correct (here m=min(n,p)).  thus if
c                   info.eq.0, all the singular values and their
c                   vectors are correct.  in any event, the matrix
c                   b = ctrans(u)*x*v is the bidiagonal matrix
c                   with the elements of s on its diagonal and the
c                   elements of e on its super-diagonal (ctrans(u)
c                   is the conjugate-transpose of u).  thus the
c                   singular values of x and b are the same.
c
c     linpack. this version dated 03/19/79 .
c              correction to shift calculation made 2/85.
c     g.w. stewart, university of maryland, argonne national lab.
c
c     zsvdc uses the following functions and subprograms.
c
c     external zdrot
c     blas zaxpy,zdotc,zscal,zswap,dznrm2,drotg
c     fortran dabs,dmax1,cdabs,dcmplx
c     fortran dconjg,max0,min0,mod,dsqrt
*/

#define X(I,J)	x[(I)-1+((J)-1)*(ldx)]
#define U(I,J)	u[(I)-1+((J)-1)*(ldu)]
#define V(I,J)	v[(I)-1+((J)-1)*(ldv)]

void zsvdc(dcomplex *x, int ldx, int n, int p, dcomplex *s, dcomplex *e,
           dcomplex *u, int ldu, dcomplex *v, int ldv, dcomplex *work, 
           int job, int *info)
{
/*
      complex*16 x(ldx,1),s(1),e(1),u(ldu,1),v(ldv,1),work(1)
*/
    int i,iter,j,jobu,k,kase,kk,l,ll,lls,lm1,lp1,ls,lu,m,maxit,
             mm,mm1,mp1,nct,nctp1,ncu,nrt,nrtp1;
    dcomplex t,r;
    double  b,c,cs,el,emm1,f,g,scale,shift,sl,sm,sn,
                      smm1,t1,test,ztest;
    int wantu,wantv;

    /*
     * adjust arrays
     */
    s--;
    e--;
    work--;
    
    /*
     *     set the maximum number of iterations.
     */
    maxit = 30;
    /*
     *     determine what is to be computed.
     */
    wantu = FALSE;
    wantv = FALSE;
    jobu = (job % 100)/10;
    ncu = n;
    if (jobu > 1) 
        ncu = min(n,p);
    if (jobu != 0) 
        wantu = TRUE;
    if ((job % 10) != 0) 
        wantv = TRUE;
    /*
     *     reduce x to bidiagonal form, storing the diagonal elements
     *     in s and the super-diagonal elements in e.
     */
    *info = 0;
    nct = min(n-1,p);
    nrt = min(p-2,n);
    nrt = max(0,nrt);
    lu  = max(nct,nrt);
    if (lu >= 1) { 
        for (l = 1;l <= lu;l++) {
            lp1 = l + 1;
            if (l <= nct) { 
                /*
                 *   compute the transformation for the l-th column and
                 *   place the l-th diagonal in s(l).
                 */
                s[l] = Complex_d(dznrm2(n-l+1,&(X(l,l)),1), 0.0);
                if (cabs1(s[l]) != 0.0) { 
                   if (cabs1(X(l,l)) != 0.0) 
                       s[l] = csign(s[l],X(l,l));
                   zscal(n-l+1,zinv(s[l]),&(X(l,l)),1);
                   X(l,l) = Cadd_d(Complex_d(1.0,0.0), X(l,l));
                } 
                s[l] = DCmul_d(-1.0,s[l]);
            } 
            if (p >= lp1) {
                for (j = lp1;j <= p; j++) {
                    if (!((l > nct) || (cabs1(s[l]) == 0.0))) { 
                        /*
                         *  apply the transformation.
                         */
                        t = Cdiv_d(DCmul_d(-1.0,zdotc(n-l+1,&(X(l,l)),1,&(X(l,j)),1)),X(l,l));
                        zaxpy(n-l+1,t,&(X(l,l)),1,&(X(l,j)),1);
                    } 
                    /*
                     *       place the l-th row of x into  e for the
                     *       subsequent calculation of the row transformation.
                     */
                    e[j] = Conjg_d(X(l,j));
                }
            } 
            if (wantu && l <= nct) { 
                /*
                 *   place the transformation in u for subsequent back
                 *   multiplication.
                 */
                for (i = l;i <= n;i++)
                    U(i,l) = X(i,l);

            } 
            if (l <= nrt) { 
                /*
                 *   compute the l-th row transformation and place the
                 *   l-th super-diagonal in e(l).
                 */
                e[l] = Complex_d(dznrm2(p-l,&(e[lp1]),1),0.0);
                if (cabs1(e[l]) != 0.0) {
                    if (cabs1(e[lp1]) != 0.0) 
                        e[l] = csign(e[l],e[lp1]);
                    zscal(p-l,zinv(e[l]),&(e[lp1]),1);
                    e[lp1] = Cadd_d(Complex_d(1.0,0.0), e[lp1]);
                } 
                e[l] = DCmul_d(-1.0,Conjg_d(e[l]));
                if (lp1 <= n && cabs1(e[l]) != 0.0) { 
                    /*
                     *   apply the transformation.
                     */
                    for (i = lp1;i<= n;i++)
                        work[i] = Complex_d(0.0,0.0);
                    for (j = lp1;j<= p;j++)
                        zaxpy(n-l,e[j],&(X(lp1,j)),1,&(work[lp1]),1);
                    for (j = lp1;j<= p;j++)
                        zaxpy(n-l,Conjg_d(Cdiv_d(DCmul_d(-1.0,e[j]),e[lp1])),
                              &(work[lp1]),1,&(X(lp1,j)),1);
                } 
                if (wantv) 
                    /*
                     *   place the transformation in v for subsequent
                     *   back multiplication.
                     */
                    for (i = lp1;i<= p;i++)
                        V(i,l) = e[i];
            }
        }
    }
    /*
     *     set up the final bidiagonal matrix or order m.
     */
    m = min(p,n+1);
    nctp1 = nct + 1;
    nrtp1 = nrt + 1;
    if (nct < p) 
        s[nctp1] = X(nctp1,nctp1);
    if (n < m) 
        s[m] = Complex_d(0.0,0.0);
    if (nrtp1 < m) 
        e[nrtp1] = X(nrtp1,m);
    e[m] = Complex_d(0.0,0.0);
    /*
     *     if required, generate u.
     */
    if (wantu) {
        if (ncu >= nctp1) { 
            for (j = nctp1;j<= ncu;j++) {
                for (i = 1;i<= n;i++)
                    U(i,j) = Complex_d(0.0,0.0);
                U(j,j) = Complex_d(1.0,0.0);
            }
        } 
        if (nct >= 1) {
            for (ll = 1;ll<= nct;ll++) {
                l = nct - ll + 1;
                if (cabs1(s[l]) == 0.0) {
                    for (i = 1;i<= n;i++)
                        U(i,l) = Complex_d(0.0,0.0);
                    U(l,l) = Complex_d(1.0,0.0);
                }
                else {
                    lp1 = l + 1;
                    if (ncu >= lp1) {
                        for (j = lp1;j<=ncu;j++) {
                            t = DCmul_d(-1.0,Cdiv_d(zdotc(n-l+1,&(U(l,l)),1,&(U(l,j)),1),U(l,l)));
                            zaxpy(n-l+1,t,&(U(l,l)),1,&(U(l,j)),1);
                        }
                    } 
                    zscal(n-l+1,Complex_d(-1.0,0.0),&(U(l,l)),1);
                    U(l,l) = Cadd_d(Complex_d(1.0,0.0), U(l,l));
                    lm1 = l - 1;
                    if (lm1 >= 1) 
                        for (i = 1;i<= lm1;i++)
                            U(i,l) = Complex_d(0.0,0.0);
                }
            }
        }
    }
    /*  
     *     if it is required, generate v.
     */
    if (wantv) {
        for (ll = 1;ll<= p;ll++) {
            l = p - ll + 1;
            lp1 = l + 1;
            if ((l <= nrt) && (cabs1(e[l]) != 0.0)) {
                for (j = lp1;j<= p;j++) {
                    dcomplex dc,dd;
                    dc = zdotc(p-l,&(V(lp1,l)),1,&(V(lp1,j)),1);
                    dd = V(lp1,l);
                    t = Cdiv_d(DCmul_d(-1.0,dc),dd);
                    zaxpy(p-l,t,&(V(lp1,l)),1,&(V(lp1,j)),1);
                }
            }
            for (i = 1;i<= p;i++)
                V(i,l) = Complex_d(0.0,0.0);
            V(l,l) = Complex_d(1.0,0.0);
        }
    }
    /*
     *     transform s and e so that they are double precision.
     */
    for (i = 1;i<= m;i++) {
        if (cabs1(s[i]) != 0.0) { 
            t = Complex_d(Cabs_d(s[i]),0.0);
            r = Cdiv_d(s[i],t);
            s[i] = t;
            if (i < m) 
                e[i] = Cdiv_d(e[i],r);
            if (wantu) 
                zscal(n,r,&(U(1,i)),1);
        } 
        /*
         *     ...exit
         */
        if (i == m) 
            break; 
        if (cabs1(e[i]) != 0.0) {
            t = Complex_d(Cabs_d(e[i]),0.0);
            r = Cdiv_d(t,e[i]);
            e[i] = t;
            s[i+1] = Cmul_d(s[i+1],r);
            if (wantv) 
                zscal(p,r,&(V(1,i+1)),1);
        }
    } 
    /*
     *     main iteration loop for the singular values.
     */
    mm = m;
    iter = 0;
    while (1) { 
        /*
         *        quit if all the singular values have been found.
         *
         *     ...exit
         */
        if (m == 0) {
            break; 
        }
        /*
         *        if too many iterations have been performed, set
         *        flag and return.
         */
        if (iter >= maxit) {
            *info = m;
            /*
             *     ......exit
             */
            break; 
        } 
        /*
         *    this section of the program inspects for
         *    negligible elements in the s and e arrays.  on
         *    completion the variables kase and l are set as follows.
         *
         *       kase = 1     if s[m] and e(l-1) are negligible and l<m
         *       kase = 2     if s(l) is negligible and l<m
         *       kase = 3     if e(l-1) is negligible, l<m, and
         *                    s(l), ..., s[m] are not negligible (qr step).
         *       kase = 4     if e(m-1) is negligible (convergence).
         */
        for (ll = 1;ll<= m;ll++) {
            l = m - ll;
            if (l == 0) 
                break; 
            test = Cabs_d(s[l]) + Cabs_d(s[l+1]);
            ztest = test + Cabs_d(e[l]);
            if (ztest == test) { 
                e[l] = Complex_d(0.0,0.0);
                break;
            }
        }
        if (l == m - 1) {
            kase = 4;
        }
        else {
            lp1 = l + 1;
            mp1 = m + 1;
            for (lls = lp1;lls<= mp1;lls++) {
                ls = m - lls + lp1;
                if (ls == l) 
                    break;
                test = 0.0;
                if (ls != m) 
                    test += Cabs_d(e[ls]);
                if (ls != l + 1) 
                    test += Cabs_d(e[ls-1]);
                ztest = test + Cabs_d(s[ls]);
                if (ztest == test) { 
                    s[ls] = Complex_d(0.0,0.0);
                    break;
                } 
            } 
            if (ls == l) { 
                kase = 3;
            }
            else {
                if (ls == m) { 
                    kase = 1;
                }
                else {
                    kase = 2;
                    l = ls;
                }
            }  
        } 
        l++;
        /*
         *        perform the task indicated by kase.
         */
        switch (kase) {
        /*
         *        deflate negligible s[m].
         */
        case 1 :   
            mm1 = m - 1;
            f = e[m-1].r;
            e[m-1] = Complex_d(0.0,0.0);
            for (kk = l;kk<= mm1;kk++) {
                k = mm1 - kk + l;
                t1 = s[k].r;
                drotg(&t1,&f,&cs,&sn);
                s[k] = Complex_d(t1,0.0);
                if (k != l) {
                    f = -sn * e[k-1].r;
                    e[k-1] = DCmul_d(cs, e[k-1]);
                }
                if (wantv) 
                    zdrot(p,&(V(1,k)),1,&(V(1,m)),1,cs,sn);
            }
            break; 
            /*
             *        split at negligible s(l).
             */
        case 2 :   
            f = e[l-1].r;
            e[l-1] = Complex_d(0.0,0.0);
            for (k = l;k<= m;k++) {
                t1 = s[k].r;
                drotg(&t1,&f,&cs,&sn);
                s[k] = Complex_d(t1,0.0);
                f = -sn*e[k].r;
                e[k] = DCmul_d(cs,e[k]);
                if (wantu) 
                    zdrot(n,&(U(1,k)),1,&(U(1,l-1)),1,cs,sn);
            }
            break; 
            /*
             *        perform one qr step.
             */
        case 3 :   
            /*
             *        calculate the shift.
             */
            scale = dmax1(Cabs_d(s[m]),Cabs_d(s[m-1]),Cabs_d(e[m-1]),
                          Cabs_d(s[l]),Cabs_d(e[l]));
            sm    = s[m].r/scale;
            smm1  = s[m-1].r/scale;
            emm1  = e[m-1].r/scale;
            sl    = s[l].r/scale;
            el    = e[l].r/scale;
            b     = ((smm1 + sm)*(smm1 - sm) + emm1*emm1)/2.0;
            c     = pow(sm*emm1,2);
            shift = 0.0;
            if (b != 0.0 || c != 0.0) { 
                shift = sqrt(b*b+c);
                if (b < 0.0) 
                    shift = -shift;
                shift = c/(b + shift);
            } 
            f = (sl + sm)*(sl - sm) + shift;
            g = sl*el;
            /*
             *           chase zeros.
             */
            mm1 = m - 1;
            for (k = l;k<= mm1;k++) {
                drotg(&f,&g,&cs,&sn);
                if (k != l) 
                    e[k-1] = Complex_d(f,0.0);
                f = cs * s[k].r + sn * e[k].r;
                e[k] = Csub_d(DCmul_d(cs,e[k]), DCmul_d(sn,s[k]));
                g = sn * s[k+1].r;
                s[k+1] = DCmul_d(cs,s[k+1]);
                if (wantv) 
                    zdrot(p,&(V(1,k)),1,&(V(1,k+1)),1,cs,sn);
                drotg(&f,&g,&cs,&sn);
                s[k] = Complex_d(f,0.0);
                f = cs * e[k].r + sn * s[k+1].r;
                s[k+1] = Cadd_d(DCmul_d(-sn,e[k]), DCmul_d(cs,s[k+1]));
                g = sn * e[k+1].r;
                e[k+1] = DCmul_d(cs,e[k+1]);
                if (wantu && k < n)
                    zdrot(n,&(U(1,k)),1,&(U(1,k+1)),1,cs,sn);
            }
            e[m-1] = Complex_d(f,0.0);
            iter++;
            break; 
            /*
             *        convergence.
             */
        case 4 :    
            /*
             *           make the singular value  positive
             */
            if (s[l].r < 0.0) {
               s[l] = DCmul_d(-1.0,s[l]);
               if (wantv) 
                   zscal(p,Complex_d(-1.0,0.0),&(V(1,l)),1);
            }
            /*
             *           order the singular value.
             */
            while (l != mm) {
               if (s[l].r >= s[l+1].r) 
                   break;  
               t = s[l];
               s[l] = s[l+1];
               s[l+1] = t;
               if (wantv && l < p)
                   zswap(p,&(V(1,l)),1,&(V(1,l+1)),1);
               if (wantu && l < n)
                   zswap(n,&(U(1,l)),1,&(U(1,l+1)),1);
               l++;
            } 
            iter = 0;
            m--;
        } 
    }     
}






