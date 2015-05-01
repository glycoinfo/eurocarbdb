/*
 * Complex arithmetic form Numerical recipes in C.
 */
#include <math.h>
#include <assert.h>
#include "complex.h"



/*
 * Returns the complex sum of two complex numbers
 */
fcomplex Cadd(fcomplex a, fcomplex b)
{
    fcomplex c;
    c.r=a.r+b.r;
    c.i=a.i+b.i;
    return c;
}

/*
 * Returns the complex difference of two complex numbers
 */
fcomplex Csub(fcomplex a, fcomplex b)
{	
    fcomplex c;
    c.r=a.r-b.r;
    c.i=a.i-b.i;
    return c;
}

/*
 * Returns the complex product of two complex numbers
 */
fcomplex Cmul(fcomplex a, fcomplex b)
{
    fcomplex c;
    c.r=a.r*b.r-a.i*b.i;
    c.i=a.i*b.r+a.r*b.i;
    return c;
}

/*
 * Returns a complex number with specified real and imaginary parts.
 */
fcomplex Complex(float re,float im)
{	
    fcomplex c;
    c.r=re;
    c.i=im;
    return c;
}


/*
 * Returns the complex conjugate of a complex number.
 */
fcomplex Conjg(fcomplex z)
{	
    fcomplex c;
    c.r=z.r;
    c.i = -z.i;
    return c;
}

/*
 * Returns the quotient of two complex numbers.
 */
fcomplex Cdiv(fcomplex a,fcomplex b)
{	
    fcomplex c;
    float r,den;
    if (fabs(b.r) >= fabs(b.i)) {
	r=b.i/b.r;
	den=b.r+r*b.i;
	c.r=(a.r+r*a.i)/den;
	c.i=(a.i-r*a.r)/den;
    } else {
	r=b.r/b.i;
	den=b.i+r*b.r;
	c.r=(a.r*r+a.i)/den;
	c.i=(a.i*r-a.r)/den;
    }
    return c;
}

/*
 * Returns the absolute value (modulus) of a complex number.
 */
float Cabs(fcomplex z)
{	
    float x,y,ans,temp;
    x=fabs(z.r);
    y=fabs(z.i);
    if (x == 0.0)
	ans=y;
    else if (y == 0.0)
	ans=x;
    else if (x > y) {
	temp=y/x;
	ans=x*sqrt(1.0+temp*temp);
    } else {
	temp=x/y;
	ans=y*sqrt(1.0+temp*temp);
    }
    return ans;
}

/*
 * Returns the complex square root of a complex number.
 */
fcomplex Csqrt(fcomplex z)
{	
    fcomplex c;
    float x,y,w,r;
    if ((z.r == 0.0) && (z.i == 0.0)) {
	c.r=0.0;
	c.i=0.0;
	return c;
    } else {
	x=fabs(z.r);
	y=fabs(z.i);
	if (x >= y) {
		r=y/x;
		w=sqrt(x)*sqrt(0.5*(1.0+sqrt(1.0+r*r)));
	} else {
		r=x/y;
		w=sqrt(y)*sqrt(0.5*(r+sqrt(1.0+r*r)));
	}
        if (z.r >= 0.0) {
		c.r=w;
		c.i=z.i/(2.0*w);
	} else {
		c.i=(z.i >= 0) ? w : -w;
		c.r=z.i/(2.0*c.i);
	}
	return c;
    }
}

/*
 * Returns the complex product of a real number and a complex number.
 */
fcomplex RCmul(float x, fcomplex a)
{	
    fcomplex c;
    c.r=x*a.r;
    c.i=x*a.i;
    return c;
}



/*
 * Returns the power to n of a complex number.
 *
fcomplex Cpow(fcomplex a, int n)
{	
    fcomplex c;
    static fcomplex one = {1.0, 0.0};
    float r;
    int i,m;

    m = abs(n);
    if (m == 0) {
	c.r=1.0;
	c.i=0.0;
    }
    else {
        c.r = a.r;
        c.i = a.i;
        for (i=1;i<m;i++) {
            r   = a.r*c.r-a.i*c.i;
            c.i = a.i*c.r+a.r*c.i;
            c.r = r;
        }
    }
    if (n<0)
        return Cdiv(one,c);
    return c;
}
*/

fcomplex Cpow(fcomplex a, int b) 	
{
int n;
float t;
fcomplex  x;
static fcomplex p, one = {1.0, 0.0};

n = b;
p.r = 1;
p.i = 0;

if(n == 0)
	return one;
if(n < 0)
	{
	n = -n;
	x=Cdiv(one, a);
	}
else
	{
	x.r = a.r;
	x.i = a.i;
	}

for( ; ; )
	{
	if(n & 01)
		{
		t = p.r * x.r - p.i * x.i;
		p.i = p.r * x.i + p.i * x.r;
		p.r = t;
		}
	if(n >>= 1)
		{
		t = x.r * x.r - x.i * x.i;
		x.i = 2 * x.r * x.i;
		x.r = t;
		}
	else
		break;
	}
        return p;
}

/********************************************************************
 * Double precision
 */

/*
 * Returns the complex sum of two complex numbers
 */
dcomplex Cadd_d(dcomplex a, dcomplex b)
{
    dcomplex c;
    c.r=a.r+b.r;
    c.i=a.i+b.i;
    return c;
}

dcomplex Csub_d(dcomplex a, dcomplex b)
{	
    dcomplex c;
    c.r=a.r-b.r;
    c.i=a.i-b.i;
    return c;
}

dcomplex Cmul_d(dcomplex a, dcomplex b)
{
    dcomplex c;
    c.r=a.r*b.r-a.i*b.i;
    c.i=a.i*b.r+a.r*b.i;
    return c;
}

dcomplex Complex_d(float re,float im)
{	
    dcomplex c;
    c.r=re;
    c.i=im;
    return c;
}

dcomplex Conjg_d(dcomplex z)
{	
    dcomplex c;
    c.r=z.r;
    c.i = -z.i;
    return c;
}

dcomplex Cdiv_d(dcomplex a,dcomplex b)
{	
    dcomplex c;
    double r,den;
    if (fabs(b.r) >= fabs(b.i)) {
	r=b.i/b.r;
	den=b.r+r*b.i;
	c.r=(a.r+r*a.i)/den;
	c.i=(a.i-r*a.r)/den;
    } else {
	r=b.r/b.i;
	den=b.i+r*b.r;
	c.r=(a.r*r+a.i)/den;
	c.i=(a.i*r-a.r)/den;
    }
    return c;
}

double Cabs_d(dcomplex z)
{	
    double x,y,ans,temp;
    x=fabs(z.r);
    y=fabs(z.i);
    if (x == 0.0)
	ans=y;
    else if (y == 0.0)
	ans=x;
    else if (x > y) {
	temp=y/x;
	ans=x*sqrt(1.0+temp*temp);
    } else {
	temp=x/y;
	ans=y*sqrt(1.0+temp*temp);
    }
    return ans;
}

dcomplex Csqrt_d(dcomplex z)
{	
    dcomplex c;
    float x,y,w,r;
    if ((z.r == 0.0) && (z.i == 0.0)) {
	c.r=0.0;
	c.i=0.0;
	return c;
    } else {
	x=fabs(z.r);
	y=fabs(z.i);
	if (x >= y) {
		r=y/x;
		w=sqrt(x)*sqrt(0.5*(1.0+sqrt(1.0+r*r)));
	} else {
		r=x/y;
		w=sqrt(y)*sqrt(0.5*(r+sqrt(1.0+r*r)));
	}
        if (z.r >= 0.0) {
		c.r=w;
		c.i=z.i/(2.0*w);
	} else {
		c.i=(z.i >= 0) ? w : -w;
		c.r=z.i/(2.0*c.i);
	}
	return c;
    }
}

dcomplex RCmul_d(float x, dcomplex a)
{	
    dcomplex c;
    c.r=x*a.r;
    c.i=x*a.i;
    return c;
}

dcomplex DCmul_d(double x, dcomplex a)
{	
    dcomplex c;
    c.r=x*a.r;
    c.i=x*a.i;
    return c;
}

/*
 * Returns the power to n of a complex number.
 */
dcomplex Cpow_d(dcomplex a, int n)
{	
    dcomplex c;
    double r;
    int i,m;
    m = abs(n);
    if (m == 0) {
	c.r=1.0;
	c.i=0.0;
    }
    else {
        c.r = a.r;
        c.i = a.i;
        for (i=1;i<m;i++) {
            r   = a.r*c.r-a.i*c.i;
            c.i = a.i*c.r+a.r*c.i;
            c.r = r;
        }
    }
    /*
    if (n<0)
        return Cinv_d(c);
        */
    return c;
}





