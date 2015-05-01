
#include <math.h>
#define max(a,b)        (((a) > (b)) ? (a) : (b))
#define min(a,b)        (((a) < (b)) ? (a) : (b))

/*
c
c     finds dsqrt(a**2+b**2) without overflow or destructive underflow
c
*/

double pythag(double a, double b)
{
    double  p,r,s,t,u;

    p = max(fabs(a),fabs(b));
    if (p == 0.0) 
        return p;
    r = pow(min(fabs(a),fabs(b))/p,2);
    while (1) { 
        t = 4.0 + r;
        if (t == 4.0) 
            break; 
        s = r/t;
        u = 1.0 + 2.0*s;
        p = u*p;
        r = pow(s/u,2) * r;
    } 
    return p;
}


