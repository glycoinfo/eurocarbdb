/********************************************************************
 *                             window.c                       
 *                                                                  
 *
 *  gv/rb
 *===================================================================
 *
 *  wdwemx(window,isize,n1,n2,sw,rlb)
 *  wdwcdx(window,isize,n1,n2,sw,rlb)
 *  wdwsnx(window,isize,n1,n2,sh)
 *  wdwsqx(window,isize,n1,n2,sh)
 *  wdwhng(window,isize,n1,n2)
 *  wdwhmg(window,isize,n1,n2)
 *  wdwtmx(window,isize,n1,n2,i)
 *  wdwksr(window,isize,n1,n2,theta)
 *  wdwabc(window,iszie,n1,n2,a,b,c,d,e,f)
 *===================================================================
 *
 *
 ********************************************************************/
 
#include <stdio.h>
#include <stdlib.h>
#include <ctype.h>
#include <string.h>
#include <stdarg.h>
#include <math.h>
#include "mathtool.h"



void wdwemx(float *window, int isize, int n1, int n2, float sw, float rlb)
{
    if (sw != 0.0) {
        int i, i1 = max(n1,1);
        int i2 = min(n2,isize);
        float p  = acos(-1.0) * rlb/(2.0 * sw);
        for (i=i1;i<=i2;i++)
            window[i-1] = exp((float)(1-i)*p);
    }
}

void wdwcdx(float *window, int isize, int n1, int n2, float sw, float rlb)
{
    if (sw != 0.0) {
        int i, i1 = max(n1,1);
        int i2 = min(n2,isize);
        float p  = acos(-1.0) * rlb/(2.0 * sw);
        for (i=i1;i<=i2;i++)
            window[i-1] = 1.0 - exp((float)(1-i)*p);
    }
}

void wdwsnx(float *window, int isize, int n1, int n2, float rsh)
{
    if (rsh > 1.0 && isize != 0) {
        int i, i1 = max(n1,1);
        int i2 = min(n2,isize);
        float pi = acos(-1.0);
        for (i=i1;i<=i2;i++)
            window[i-1] = sin((pi/rsh)*(1.0+((float)i/isize)*(rsh-1.0)));
    }
}

void wdwsqx(float *window, int isize, int n1, int n2, float rsh)
{
    if (rsh > 1.0 && isize != 0) {
        int i, i1 = max(n1,1);
        int i2 = min(n2,isize);
        float pi = acos(-1.0);
        for (i=i1;i<=i2;i++)
           window[i-1] = pow(sin((pi/rsh)*(1.0+((float)i/isize)*(rsh-1.0))),2);
    }
}


void wdwhng(float *window, int isize, int n1, int n2)
{
    if (isize != 0) {
        int i, i1 = max(n1,1);
        int i2 = min(n2,isize);
        float pi = acos(-1.0);
        for (i=i1;i<=i2;i++)
           window[i-1] = 0.5 + 0.5 * cos(pi*((float)i/(float)isize));
    }
}


void wdwhmg(float *window, int isize, int n1, int n2)
{
    if (isize != 0) {
        int i, i1 = max(n1,1);
        int i2 = min(n2,isize);
        float pi = acos(-1.0);
        for (i=i1;i<=i2;i++)
           window[i-1] = 0.54 + 0.46 * cos(pi*((float)i/(float)isize));
    }
}

void wdwgmx(float *window, int isize, int n1, int n2, float sw,
            float rlb, float gn)
{
    float gb = gn * isize;
    if (sw != 0.0 && gb != 0.0) {
        float prn = acos(-1.0)*rlb/(2.0*sw);
        int i, i1 = max(n1,1);
        int i2 = min(n2,isize);
        for (i=i1;i<=i2;i++)
            window[i-1] = exp(-prn*((float)i-((float)(i*i))/(2.0*gb)-gb/2.0));
    }
}


void wdwtmx(float *window, int isize, int n1, int n2, int itm)
{
    if (itm != 0) {
        float step = 1.0/(float)itm;
        float w = -step;
        int i, i1 = max(n1,1);
        int i2 = min(n2,isize);
        for (i=i1;i<=itm;i++) {
            w += step;
            window[i-1] = w;
        }
        for (i=itm+1;i<=i2;i++)
            window[i-1] = 1.0;
    }
}

/*
 *.... returns the modified bessel function i0(x) for any real x
 *     source: numerical recipes, page 177
 *             w.h. press et. al.
 *             cambridge university press 1986
 */
double bessi0(double x)
{
    double y, result;
    double p1 = 1.0, p2 = 3.5156229, p3 = 3.0899424,
           p4 = 1.2067492, p5 = 0.2659732, p6 = 0.360768e-1, p7 = 0.45813e-2;
    double q1=0.39894228, q2=0.1328592e-1,
           q3=0.225319e-2, q4=-0.157565e-2, q5=0.916281e-2, q6=-0.2057706e-1,
           q7=0.2635537e-1, q8=-0.1647633e-1, q9=0.392377e-2;

    if (fabs(x) < 3.75) {
        y = pow(x/3.75,2);
        result = p1+y*(p2+y*(p3+y*(p4+y*(p5+y*(p6+y*p7)))));
    }
    else {
        double ax = fabs(x);
        y  = 3.75/ax;
        result = (exp(ax)/sqrt(ax))*(q1+y*(q2+y*(q3+y*(q4
           +y*(q5+y*(q6+y*(q7+y*(q8+y*q9))))))));
    }
    return result;
}


/*
 *=========================================================================
 *
 * valid   : 1d
 * version : 890103.0
 * purpose : calculation of kaiser window
 *
 * call    : wdwksr(window,isize,n1,n2,theta)
 *
 * refer.  : principles of nuclear magnetic resonance in one and two
 *           dimensions
 *           r.r ernst, g. bodenhausen and a. wokaun
 *           oxford science publications
 *           clarendon press, oxford 1987
 *
 * author  : geerten vuister
 *           laboratory of organic chemistry
 *           padualaan 8 , 3584 ch utrecht, the netherlands
 *
 * description
 * -----------
 * parameters : window == array of real   of sufficient size (>n2)
 *              n1..n2 == range of window to calculate
 *              theta  == parameter (typical: 1-2 pi)
 *
 * algorithm  : kaiser(n) = bessi0{theta(sqrt(1-(n/n2)**2)}
 *                         /bessi0{theta}
 *
 *              bessi0(x) == zero order modfied bessel function
 *
 *=========================================================================
 */
void wdwksr(float *window, int isize, int n1, int n2, float theta)
{
    if (isize != 0) {
        float bestht = 1.0/bessi0(theta);
        float point  = 1.0/(float)isize;
        int i,i1 = max(n1,1);
        int i2 = min(n2,isize);
        for (i=i1;i<=i2;i++)
           window[i-1] = bestht * bessi0(theta*(sqrt(1.0-pow((float)i*point,2))));
    }
}




