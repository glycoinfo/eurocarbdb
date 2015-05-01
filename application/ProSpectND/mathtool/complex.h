/*
 * complex.h
 *
 * Complex arithmetic form Numerical recipes in C.
 */
 
#ifndef _COMPLEX_H_
#define _COMPLEX_H_

#define Ccopy(a,b)	{(a).r=(b).r;(a).i=(b).i;}

typedef struct FCOMPLEX {float r, i;} fcomplex;

fcomplex Cadd(fcomplex a, fcomplex b);
fcomplex Csub(fcomplex a, fcomplex b);
fcomplex Cmul(fcomplex a, fcomplex b);
fcomplex Complex(float re,float im);
fcomplex Conjg(fcomplex z);
fcomplex Cdiv(fcomplex a,fcomplex b);
float    Cabs(fcomplex z);
fcomplex Csqrt(fcomplex z);
fcomplex RCmul(float x, fcomplex a);
fcomplex Cpow(fcomplex a, int n);

typedef struct DCOMPLEX {double r, i;} dcomplex;

dcomplex Cadd_d(dcomplex a, dcomplex b);
dcomplex Csub_d(dcomplex a, dcomplex b);
dcomplex Cmul_d(dcomplex a, dcomplex b);
dcomplex Complex_d(float re,float im);
dcomplex Conjg_d(dcomplex z);
dcomplex Cdiv_d(dcomplex a,dcomplex b);
double   Cabs_d(dcomplex z);
dcomplex Csqrt_d(dcomplex z);
dcomplex RCmul_d(float x, dcomplex a);
dcomplex DCmul_d(double x, dcomplex a);
dcomplex Cpow_d(dcomplex a, int n);

#include <ctype.h>
#define lsame(a,b)	(toupper(*a) == toupper(*b))
#define r_cnjg(a,b)	((a)->r=(b)->r,(a)->i=-(b)->i)
#define c_abs(a)	Cabs(*a)

#endif

