/*
nmrtool.h
*/

#ifndef _NMRTOOL_H
#define _NMRTOOL_H

#ifdef MSHELL
#include "mshell.h"
#endif

#define UNKNOWN	0
#define NOPEAK	1
#define ISPEAK	2

typedef struct {
    float   x, y;
}   LIST;

typedef union {
    unsigned char byte[4];
    long i;
} ENDI;

int os_big_endian(void);

void conv(int lx, int ifx, float *x, int ly, int ify, float *y,
	   int lz, int ifz, float *z);
void hilbert(int n, float x[], float y[]);

#define MAXROOT	256
#define KCMAX	512
#define PREDICT_FORWARD		0
#define PREDICT_BACKWARD	1
#define PREDICT_GAP		2

#define PREDICT_REAL		6
#define PREDICT_COMPLEX		7
#define PREDICT_DOUBLE_REAL	8
#define PREDICT_HSVD		1
#define PREDICT_LPC		0

void wdwemx(float *window, int isize, int n1, int n2, float sw, float rlb);
void wdwcdx(float *window, int isize, int n1, int n2, float sw, float rlb);
void wdwsnx(float *window, int isize, int n1, int n2, float rsh);
void wdwsqx(float *window, int isize, int n1, int n2, float rsh);
void wdwhng(float *window, int isize, int n1, int n2);
void wdwhmg(float *window, int isize, int n1, int n2);
void wdwgmx(float *window, int isize, int n1, int n2, float sw,
            float rlb, float gn);
void wdwtmx(float *window, int isize, int n1, int n2, int itm);
double bessi0(double x);
void wdwksr(float *window, int isize, int n1, int n2, float theta);
int fftrxf(float *xrr, float *xri, int isize, int ntot, int rotate);
int fftixf(float *xrr, float *xri, int isize, int ntot, int rotate);
int fftcxf(float *xrr, float *xri, int isize, int ntot, int rotate);
int fftrxb(float *xrr, float *xri, int isize, int ntot);
int fftixb(float *xrr, float *xri, int isize, int ntot);
int fftcxb(float *xrr, float *xri, int isize, int ntot);
int dftrxf(float *xrr, float *xri, int isize, int ntot, float *workr, float *worki);
int dftixf(float *xrr, float *xri, int isize, int ntot, float *workr, float *worki);
int dftcxf(float *xrr, float *xri, int isize, int ntot, float *workr, float *worki);
int dftrxb(float *xrr, float *xri, int isize, int ntot, float *workr, float *worki);
int dftixb(float *xrr, float *xri, int isize, int ntot, float *workr, float *worki);
int dftcxb(float *xrr, float *xri, int isize, int ntot, float *workr, float *worki);


#define NOBAS        0
#define BASPL1       1
#define BASPL2       2
#define BASTB1       3
#define BASTB2       4
#define BASSN1       5
#define BASSN2       6
#define BASCS1       7
#define BASCS2       8
#define BASSP1	     9
#define BASSP2	    10
#define BASSC1	    11
#define BASSC2	    12
#define BASBFX       9
#define DOBAS        9

float baseline_region(float *spec, float *bas, int npoints, int clip);
void bcrbfx(float *y, int npts, int nt, int *ier);
void itrsncs(float *y, float *z, int npts, int nt1, int nt2, float u, float v,
            float sfac, float bfac, int *ier, int dosine);
void itrpl(float *y, float *z, int npts, int nterms, float u, float v,
            float sfac, float bfac, int *ier);
void itrtb(float *y, float *z, float *tab, int npts, float u, float v,
            float sfac, float bfac, int *ier);
void bascrr(int ibase, int ibstrt, int ibstop,
            float *y, float *window, float *table, int nt1, int nt2,
            float u, float v, float sfac, float bfac, int *ier);

void phase(float *xr, float *xi, int nbl, float ahold,
           float bhold, int i0);
int watwa(int cospow, int iscmplx, float *xreal, float *ximag, int isize,
          float kc, float wshift, int dspshift);
void bacdic(float data[], int ndata, double d[], int ipoles,
            float future[], int nfut);
void ftlipr(int imode, float *data, int ndata, float *future,
            int nfut, int ipoles);
void ftlipc_gap(int fixroots, float rdata[],float idata[],int icmplx, int ndata,
             int nfut1, int nfut2, int iorder, double toler);
void ftlipc(int imode, int fixroots, float rdata[],float idata[],
             int icmplx, int ndata,  float rfuture[],float ifuture[],
             int nfut,int iorder, double toler);
void fidhsvd(int imode, int fixroots, float rdata[],float idata[],
             int icmplx, int points, float tol, int nfut, int nfut2, 
             int order, int icomplx, int replace);
int intg2d(int idev, float *xx, int ilowb, int ihighb, int ilowa, int ihigha,
           int zoint, float *dint, float *zo,  
           int (*read2d)(void *,int,float*,int,int), void *userdata);
int intx2d (int idev, float *xx, int ilowb, int ihighb, int ilowa, int ihigha,
             int idmaxb, int idmaxa, int *icentb, int *icenta, int zoint,
             float *sum, int (*read2d)(void *,int,float*,int,int), void *userdata);
int inta2d (int idev, float *xx, int ilowb, int ihighb, int ilowa, int ihigha,
             int idmaxb, int idmaxa, int *icentb, int *icenta, int zoint,
             float *ave, float *sdv, int *nmeas, int (*read2d)(void *, int,float*,int,int), 
             void *userdata);
int integrate2d (int idev, float *xdata, int floodfill,
                 int ilowb, int ihighb, int ilowa, int ihigha,
                 int *icentb, int *icenta, int ncenter, 
                 int zoint, float *zo, float *level, float *sum, 
                 int convex, char **cbuf_return, 
                 int (*read2d)(void*,int,float*,int,int), void *userdata);
            
int npeaks1d(float *data, int i1, int i2, float height, float pmax, int mp, int *ipeaks);

float rmsnoi(float *x, int n);
int   rmsn2d(int idev, float *x, int ilowb, int ihighb,
            int ilowa,int ihigha, float *avg, float *rnoise,
            int (*read2d)(void *,int,float*,int,int), void *userdata);

#define MODE0123	0
#define MODE3210	1
#define MODE0321	2
#define MODE1230	3

char  *unpack4(int jb);
char  *unpack6(int jb);
char  *unpack8(int jb);
float unpackf(int jb1, int jb2);
int   unpacki(int jb);
int   unpack3to4(int *in, int *out, int start_in, int stop_in, 
                 int start_out, int stop_out, int mode);
int   unpack_byte(int jb, int index);
void  negstep(float *data, int n, int is, int ns);

int read_acqufile_bin(FILE *acqufile, char *result, char *pattern);


void xxshfn(float *x, int n1, int n2, int n3, int n4);
void xxrotate(float *x, int n1, int n2, int n3);
void xxaddv(float *x, int n1, int n2, float val);
void xxaddx(float *x, float *y, int n1, int n2);
void xxfilv(float *x, int n1, int n2, float val);
void xxmulv(float *x, int n1, int n2, float val);
void xxmulx(float *x, float *y, int n1, int n2);
float xxsumz(float *x, int n1, int n2);
void xxswap2(float *x, int n1, int n2);
void xxswap4(float *x, int n1, int n2);
void xxswap8(float *x, int n1, int n2);
void xxi2f(float *x, int n1, int n2);
void xxf2i(float *x, int n1, int n2);
char *xxf2ascii(float *x, int n1, int n2);
void xxc2ri(float *co, float *re, float *ir, int n1, int n2);
void xxc2ir(float *co, float *re, float *ir, int n1, int n2);
void xxri2c(float *co, float *re, float *ir, int n1, int n2);

void hpcntr(float *a, char *bits, int m, int n, float *cont, 
            int *lcol, int ncont, int xoff, int yoff, float pxsc, float pysc,
            void (*plotfunc)(int, void*, float, float, float, float),
            void (*levelfunc)(int, int*));
void contour_plot(float *data, int dim_x, int dim_y, float *clevel, int levels,
                  int off_x, int off_y, float scale_x, float scale_y, 
                  int  *lcol, void *userdata, int do_peakpick,
                  void (*plotfunc)(int, void*, float, float, float, float),
                  void (*levelfunc)(int, int*));
#endif

