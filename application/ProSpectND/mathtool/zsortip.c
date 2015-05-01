
#include "complex.h"
#include "mathtool.h"


/*
c----------------------------------------------------------------------------
c zsortip    Sort an array of complex numbers into ascending 
c            numerical order of their imaginary parts.
c
c V
c University of Illinois at Urbana-Champaign
c
c History:
c           - Completed V1.0 July 1994
c
c $Id: zsortip.c,v 1.1 1998/05/18 09:53:21 kuik Exp $
c----------------------------------------------------------------------------
c
*/

#define A(I)	a[(I)-1]

void zsortip(dcomplex *a, int n)
{
    dcomplex  c;
    double   ra;
    int i,j,l,ir;

    
    if (n <= 1) 
        return;
    l=n/2+1;
    ir=n;
    while (1) {
        if (l > 1) {
            l--;
            ra = A(l).i;
            c = A(l);
        }
        else {
            ra=A(ir).i;
            c=A(ir);
            A(ir)=A(1);
            ir--;
            if (ir == 1) {
                A(1)=c;
                return;
            }
        }
        i=l;
        j=l+l;
        while (j <= ir) {
            if (j < ir) 
               if (A(j).i < A(j+1).i) 
                   j++;    
            if (ra < A(j).i) {
               A(i)=A(j);
               i=j;
               j=j+j;
            }
            else
               j=ir+1;
        }
        A(i)=c;
    }
}

