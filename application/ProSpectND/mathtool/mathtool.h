
#ifndef _MATHTOOL_H
#define _MATHTOOL_H

#ifdef MSHELL
#include "mshell.h"
#endif

#ifndef FALSE
#define FALSE	0
#endif
#ifndef TRUE
#define TRUE	1
#endif
#ifndef sign
#define sign(a,b)       ((b)<0 ? -fabs(a) : fabs(a))
#endif
#ifndef max
#define max(a,b)        (((a) > (b)) ? (a) : (b))
#endif
#ifndef min
#define min(a,b)        (((a) < (b)) ? (a) : (b))
#endif
#ifndef round
#define round(f)        ((int)(((f) < 0.0) ? (ceil((f)-0.5)) : (floor((f)+0.5))))
#endif


#ifndef _COMPLEX_H_
#include "complex.h"
#endif

void zscal(int n, dcomplex za, dcomplex *zx, int incx);
dcomplex zdotc(int n, dcomplex *zx, int incx, dcomplex *zy, int incy);
dcomplex zdotu(int n, dcomplex *zx, int incx, dcomplex *zy, int incy);
void zaxpy(int n, dcomplex  za, dcomplex  *zx, int incx, 
                                       dcomplex *zy, int incy);
void zdrot(int n, dcomplex *zx, int incx, dcomplex *zy, int incy,
           double c, double s);
void zcopy(int n, dcomplex *zx, int incx, dcomplex *zy, int incy);

void zsvdc(dcomplex *x, int ldx, int n, int p, dcomplex *s, dcomplex *e,
           dcomplex *u, int ldu, dcomplex *v, int ldv, dcomplex *work, 
           int job, int *info);
void zsortip(dcomplex *a, int n);
void zppfa(dcomplex *ap, int n, int *info);
void zppsl(dcomplex *ap, int n, dcomplex *b);
void zswap(int n, dcomplex *zx, int incx, dcomplex *zy, int incy);

dcomplex cdexp(dcomplex z);
dcomplex csign(dcomplex a, dcomplex b);
dcomplex cdlog(dcomplex z);


double dznrm2(int n, dcomplex *zx, int incx);

void drotg(double *da, double *db, double *c, double *s);
void dscal(int n, double da, double *dx, int incx);


double pythag(double a, double b);
void csroot(double xr, double xi, double *yr, double *yi);
void cdiv(double ar,double ai,double br,double bi,double *cr,double *ci);

void cbal(int nm, int n, double *ar, double *ai, int *low, int *igh,
          double *scale);
void corth(int nm, int n, int low, int igh,
           double *ar, double *ai, double *ortr, double *orti);
void comqr(int nm, int n, int low, int igh,
           double *hr, double *hi, double *wr, double *wi, int *ierr);

int ampfit(dcomplex *fid, int np, int n0, int ncomp,
           dcomplex *zfreq, dcomplex *zamp, dcomplex *zwork);

int hsvd(dcomplex *fid, int np, int *ncomp, double *zwork, int pflag, int *err);
int lpsvd(dcomplex *fid, int np, int *ncomp, double *dzwork, int pflag, int *err);
void zpoly(dcomplex *op, int degree, dcomplex *zero, int *fail);

/*
 * CLAPACK
 */

void cbdsqr(char *uplo, int n__, int ncvt, int 
	    nru, int ncc, float *d, float *e, fcomplex *vt, int ldvt, 
	    fcomplex *u, int ldu, fcomplex *c, int ldc, float *rwork, 
	    int *info);
void cgeev(char *jobvl, char *jobvr, int n__, fcomplex *a, 
	   int lda, fcomplex *w, fcomplex *vl, int ldvl, fcomplex *vr, 
	   int ldvr, fcomplex *work, int lwork, float *rwork, int *info);
void cgesvd(char *jobu, char *jobvt, int m__, int n__, 
	    fcomplex *a, int lda, float *s, fcomplex *u, int ldu, 
	    fcomplex *vt, int ldvt, fcomplex *work, int lwork, 
	    float *rwork, int *info);
void cgelss(int m__, int n__, int nrhs, fcomplex *a,
            int lda, fcomplex *b, int ldb, float *s, float rcond, 
	    int *rank, fcomplex *work, int lwork, float *rwork, int *info);
void cgebrd(int m__, int n__, fcomplex *a, int lda,
	    float *d, float *e, fcomplex *tauq, fcomplex *taup, 
            fcomplex *work, int lwork, int *info);
void cgehrd(int n__, int ilo, int ihi, fcomplex *a,
	    int lda, fcomplex *tau, fcomplex *work, int lwork, int 
	    *info);
void cgehd2(int n__, int ilo, int ihi, fcomplex *
	    a, int lda, fcomplex *tau, fcomplex *work, int *info);
void cgemm(char *transa, char *transb, int m__, int n__, 
           int  k__, fcomplex alpha, fcomplex *a, int lda, fcomplex *b, 
	   int  ldb, fcomplex beta, fcomplex *c, int ldc);
void cgeqrf(int m__, int n__, fcomplex *a, int lda,
	    fcomplex *tau, fcomplex *work, int lwork, int *info);
void cgeqr2(int m__, int n__, fcomplex *a, int lda,
	    fcomplex *tau, fcomplex *work, int *info);
void cgelqf(int m__, int n__, fcomplex *a, int lda,
	    fcomplex *tau, fcomplex *work, int lwork, int *info);
void cgelq2(int m__, int n__, fcomplex *a, int lda,
	    fcomplex *tau, fcomplex *work, int *info);
void cgemv(char *trans, int m__, int n__, fcomplex alpha, 
           fcomplex *a, int lda, fcomplex *x, int incx, fcomplex 
	   beta, fcomplex *y, int incy);
void cgebd2(int m__, int n__, fcomplex *a, int lda,
	    float *d, float *e, fcomplex *tauq, fcomplex *taup, fcomplex *work, 
	    int *info);
void cgebal(char *job, int n__, fcomplex *a, int lda, 
	    int *ilo, int *ihi, float *scale, int *info);
void cgebak(char *job, char *side, int n__, int ilo, 
	    int ihi, float *scale, int m__, fcomplex *v, int ldv, 
	    int *info);
void cgerc(int m__, int n__, fcomplex alpha, fcomplex *x, 
	   int incx, fcomplex *y, int incy, fcomplex *a, int lda);

void cunmqr(char *side, char *trans, int m__, int n__, 
	    int k__, fcomplex *a, int lda, fcomplex *tau, fcomplex *c, 
	    int ldc, fcomplex *work, int lwork, int *info);
void cunm2r(char *side, char *trans, int m__, int n__, 
	    int k__, fcomplex *a, int lda, fcomplex *tau, fcomplex *c, 
	    int ldc, fcomplex *work, int *info);
void cungbr(char *vect, int m__, int n__, int k__, 
	    fcomplex *a, int lda, fcomplex *tau, fcomplex *work, 
            int lwork, int *info);
void cungqr(int m__, int n__, int k__, fcomplex *a, 
	    int lda, fcomplex *tau, fcomplex *work, int lwork, int *info);
void cunglq(int m__, int n__, int k__, fcomplex *a, 
	    int lda, fcomplex *tau, fcomplex *work, int lwork, int *info);
void cungl2(int m__, int n__, int k__, fcomplex *a, 
	    int lda, fcomplex *tau, fcomplex *work, int *info);
void cung2r(int m__, int n__, int k__, fcomplex *a, 
	    int lda, fcomplex *tau, fcomplex *work, int *info);
void cunmlq(char *side, char *trans, int m__, int n__, 
	    int k__, fcomplex *a, int lda, fcomplex *tau, fcomplex *c, 
	    int ldc, fcomplex *work, int lwork, int *info);
void cunml2(char *side, char *trans, int m__, int n__, 
	    int k__, fcomplex *a, int lda, fcomplex *tau, fcomplex *c, 
	    int ldc, fcomplex *work, int *info);
void cunmbr(char *vect, char *side, char *trans, int m__, 
	    int n__, int k__, fcomplex *a, int lda, fcomplex *tau, 
	    fcomplex *c, int ldc, fcomplex *work, int lwork, int *info);
void cunghr(int n__, int ilo, int ihi, fcomplex *a, int lda, 
	    fcomplex *tau, fcomplex *work, int lwork, int *info);	    

void ccopy(int n, fcomplex *cx, int incx, fcomplex *cy, int incy);
void cdotc(fcomplex *ret_val, int n__, fcomplex *cx, int 
	   incx, fcomplex *cy, int incy);
void cdotu(fcomplex *ret_val, int n__, fcomplex *cx, int 
	   incx, fcomplex *cy, int incy);
void caxpy(int n__, fcomplex ca, fcomplex *cx, int incx, 
	   fcomplex *cy, int incy);
void clacpy(char *uplo, int m__, int n__, fcomplex *a, 
	    int lda, fcomplex *b, int ldb);
void clabrd(int m__, int n__, int nb_, fcomplex *a, 
	    int lda, float *d, float *e, fcomplex *tauq, fcomplex *taup, 
            fcomplex *x, int ldx, fcomplex *y, int ldy);
double clange(char *norm, int m__, int n__, fcomplex *a, int 
	      lda, float *work);
void clascl(char *type, int kl_, int ku_, float cfrom, float cto,
            int m__, int n__, fcomplex *a, int lda, int *info);
void claset(char *uplo, int m__, int n__, fcomplex 
	    alpha, fcomplex beta, fcomplex *a, int lda);
void clarfg(int n__, fcomplex *alpha, fcomplex *x, int 
	    incx, fcomplex *tau);
void csrscl(int n__, float sa_, fcomplex *sx, int incx);
void csscal(int n__, float sa_, fcomplex *cx, int incx);
void csrot(int n__, fcomplex *cx, int incx, fcomplex *
	   cy, int incy, float c__, float s__);
void clacgv(int n__, fcomplex *x, int incx);
void clasr(char *side, char *pivot, char *direct, int m__,
	   int n__, float *c, float *s, fcomplex *a, int lda);
void classq(int n__, fcomplex *x, int incx, float *scale, float *sumsq);
void clarf(char *side, int m__, int n__, fcomplex *v, 
	   int incv, fcomplex tau, fcomplex *c, int ldc, fcomplex *work);
void clarft(char *direct, char *storev, int n__, int k__,
	    fcomplex *v, int ldv, fcomplex *tau, fcomplex *t, int ldt);
void clarfb(char *side, char *trans, char *direct, char *
	    storev, int m__, int n__, int k__, fcomplex *v, int ldv, 
	    fcomplex *t, int ldt, fcomplex *c, int ldc, fcomplex *work, 
	    int ldwork);
void clarfx(char *side, int m__, int n__, fcomplex *v, 
	    fcomplex tau, fcomplex *c, int ldc, fcomplex *work);
void cladiv(fcomplex *ret_val, fcomplex x, fcomplex y);
void clahqr(int wantt, int wantz, int n__, 
	    int ilo, int ihi, fcomplex *h, int ldh, fcomplex *w, 
	    int iloz, int ihiz, fcomplex *z, int ldz, int *info);
void clahrd(int n__, int k__, int nb_, fcomplex *a, int lda, 
	    fcomplex *tau, fcomplex *t, int ldt, fcomplex *y, int ldy);
double clanhs(char *norm, int n__, fcomplex *a, int lda, float *work);
void clatrs(char *uplo, char *trans, char *diag, char *normin, 
	    int n__, fcomplex *a, int lda, fcomplex *x, float *scale,
	    float *cnorm, int *info);
void cswap(int n__, fcomplex *cx, int incx, fcomplex *cy, int incy);
void cscal(int n__, fcomplex ca_, fcomplex *cx, int incx);
void ctrmm(char *side, char *uplo, char *transa, char *diag, 
	   int m__, int n__, fcomplex alpha, fcomplex *a, int lda, 
	   fcomplex *b, int ldb);
void ctrmv(char *uplo, char *trans, char *diag, int n__, 
	   fcomplex *a, int lda, fcomplex *x, int incx);
void ctrevc(char *side, char *howmny, int *select, 
	    int n__, fcomplex *t, int ldt, fcomplex *vl, int ldvl, 
	    fcomplex *vr, int ldvr, int mm_, int *m, fcomplex *work, 
	    float *rwork, int *info);
void ctrsv(char *uplo, char *trans, char *diag, int n__, 
	   fcomplex *a, int lda, fcomplex *x, int incx);
void chseqr(char *job, char *compz, int n__, int ilo,
	    int ihi, fcomplex *h, int ldh, fcomplex *w, fcomplex *z, 
	    int ldz, fcomplex *work, int lwork, int *info);


double slamch(char *cmach);
void slabad(float *small, float *large);
void sladiv(float a, float b, float c, float d, float *p, float *q);
void slaset(char *uplo, int m__, int n__, float alpha, 
	    float beta, float *a, int lda);
void slascl(char *type, int kl_, int ku_, float 
	    cfrom, float cto, int m__, int n__, float *a, int lda, 
	    int *info);
void slasq1(int n__, float *d, float *e, float *work, 
	    int *info);
void slasq2(int m__, float *q, float *e, float *qq, float *ee,
	    float eps, float tol2, float small2, float *sup, int *kend, 
	    int *info);
void slasq3(int *n, float *q, float *e, float *qq, float *ee,
	    float *sup, float *sigma, int *kend, int *off, int *iphase,
	    int iconv, float eps, float tol2, float small2);
void slasq4(int n__, float *q, float *e, float *tau, float *sup);
void slartg(float f__, float g__, float *cs, float *sn, float *r);
void slasrt(char *id, int n__, float *d, int *info);
void slasv2(float f__, float g__, float h__, float *ssmin, float *
	    ssmax, float *snr, float *csr, float *snl, float *csl);
void slas2(float f__, float g__, float h__, float *ssmin, float *ssmax);
void scopy(int n__, float *sx, int incx, float *sy, int incy);
void sscal(int n__, float sa_, float *sx, int incx);
double scasum(int n__, fcomplex *cx, int incx);
double scnrm2(int n__, fcomplex *x, int incx);
double slapy2(float x, float y);
double slapy3(float x, float y, float z);
int ilaenv(int ispec, char *name, char *opts, int n1_, int 
	   n2, int n3_, int n4_, int name_len, int opts_len);
int isamax(int n__, float *sx, int incx);
int icamax(int n__, fcomplex *cx, int incx);
void s_cat(char *lp, char *rpp[], int rnp[], int *np, int ll);
void s_copy(char *a, char *b, int la, int lb);
int s_cmp(char *a0, char *b0, int la, int lb);

/***************************************************************************
 C
 C   ref.:
 C       W.H. Press et al., Numerical Recipes, Chap 12, Cambridge Univ. Press
 C            (1986)
 C       E.T. Olejniczak and H.L. Eaton, J. Magn. Reson. 87, 628-632 (1990)
 C
 */
void laguer(dcomplex a[], int m, dcomplex *x, double eps, int polish);
void zroots(dcomplex a[], int m, dcomplex roots[], int polish);
void fixrts(double *d, int npoles, int ifmode);
void memcof(float data[], int n, int m, double *pm, double cof[]);
void dSort2(int n, double *da, int *ib );
void svbksbd(double **u, double w[], double **v, int m, int n, 
             double b[], double x[]);
int  svdcmpd(double **a, int m, int n, double *w, double **v);
int  svdfit(float x[], float y[], float sig[], int ndata, double a[],
            int ma, double **u, double **v, double *w,
            double *chisq, void (*funcs)(float,double*,int));
void covsrt(double **covar, int ma, int lista[], int mfit);
int  lfit(float x[], float y[], float sig[], int ndat, double a[], int ia[],
          int ma, double **covar, double *chisq, 
          void (*funcs)(float, double [], int));
int  gaussj(double **a, int n, double **b, int m);
void mrqcof(float x[], float y[], float sig[], int ndata, double a[], int ia[],
            int ma, double **alpha, double beta[], double *chisq,
            void (*funcs)(float, double [], double *, double [], int));
int  mrqmin(float x[], float y[], float sig[], int ndata, double a[], int ma,
            int ia[], double **covar, double **alpha, double *chisq,
            void (*funcs)(float, double *, double *, double *, int), 
            double *alamda, int **iconstr, double **constr);
void gauss(float *aug[], float *x, int n, int *ier, float *rnorm);
void minmax(float *x, int size, float *xmin, float *xmax);

void simplx(int n, float *x, float *f, float tol, 
            int iw, float *xn, float *pdash, float *pastx, 
            float *pdastx, float *y, float *p, int ldp, 
            void (*funct)(void*,int,float*,float*),
            void *data, int maxcal);
void givens(int nx, double **vect, double *root, double *a);


float xxsumz(float *x, int n1, int n2);
void invrse(float *x, int n);
void invrse2(float *x, int start, int stop);
void intrlv(float *ri, float *r, float *i, int n, int iopt);



int *ivector(int minx, int maxx);
int *resize_ivector(int *vec, int minx, int maxx);
void free_ivector(int *m, int minx);
float *fvector(int minx, int maxx);
float *resize_fvector(float *vec, int minx, int maxx);
void free_fvector(float *m, int minx);
double *dvector(int minx, int maxx);
void free_dvector(double *m, int minx);
int **imatrix(int minx, int maxx, int miny, int maxy);
int **enlarge_imatrix(int **vec, int minx, int maxx, int oldmaxx, int miny, int maxy);
int **resize_imatrix(int **m, int minx, int maxx, int miny, int maxy);
void free_imatrix(int **m, int minx, int maxx, int miny, int maxy);
float **fmatrix(int minx, int maxx, int miny, int maxy);
float **enlarge_fmatrix(float **vec, int minx, int maxx, int oldmaxx, int miny, int maxy);
float **resize_fmatrix(float **m, int minx, int maxx, int miny, int maxy);
void free_fmatrix(float **m, int minx, int maxx, int miny, int maxy);
double **dmatrix(int minx, int maxx, int miny, int maxy);
void free_dmatrix(double **m, int minx, int maxx, int miny, int maxy);

#endif

