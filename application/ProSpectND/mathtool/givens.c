/*************************************************************************/
/* eigenvalues and eigenvectors by the givens                            */
/*     method                                                            */
/*************************************************************************/
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
#include "mathtool.h"

#define OLD_AND_SLOW_not
#ifdef OLD_AND_SLOW

/*
 ** * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *   the parameters, eta and theta, should be adjusted by the user for
 *   his particular machine. eta is an indication of the precision of
 *   the floating point representation on the computer being used (
 *   roughly 10**(-m)), theta is an indication of the range of numbers
 *   that can be expressed in the floating point representation (rough-
 *   ly the largest positive number, whichever is smaller).
 *   some recommended values follow.
 *   for ibm 7094, univac 1108, etc. (27-bit binary fraction, 8-bit bi-
 *   nary exponent), eta=1.e-8, theta=1.e37.
 *   for control data 3600 (36-bit binary fraction, 11-bit binary expo-
 *   nent), eta=1.e-11, theta=1.e307.
 *   for control data 6600 (48-bit binary fraction, 11-bit binary expo-
 *   nent), eta=1.e-14, theta=1.e293.
 *   for ibm 360/50 and 360/65 double precision (56-bit hexadecimal
 *   fraction, 7-bit hexadecimal exponent), eta=1.e-16, theta=1.e75.
 ** * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 */
#define ETA		1.e-16
#define THETA		1.7e308
/*
 *   toler  is a factor used to determine if two roots are close
 *   enough to be considered degenerate for purposes of orthogonali-
 *   zing their vectors.  for the matrix normed to unity, if the
 *   difference between two roots is less than toler, then
 *   orthogonalization will occur.
 */
#define TOLER		(ETA * 1.e2)
/*
      initial value for pseudorandom number generator... (2**23)-3
*/
#define RPOWER		8388608.0

#define POW2(a)	((a)*(a))

/*
*     -------------------------------------------
*     eigenvalues and eigenvectors by the givens
*     method
*     -------------------------------------------
*     the parameters for the routine are...
*     nx     order of matrix
*     a      matrix stored by columns in packed upper triangular form,
*            i.e. occupying nx*(nx+1)/2 consecutive locations.
*     b      scratch array used by givens. must be at least nx*5 cells.
*     root   array to hold the eigenvalues.
*     vect   eigenvector array. each column will hold an eigenvector
*            for the corresponding root.
*     the arrays a and b are destroyed by the computation. the results
*     appear in root and vect. for proper functioning of this routine,
*     the result of a floating point underflow should be a zero. to con-
*     vert this routine to double precision, be sure that all real vari-
*     ables and function references are properly made double precision.
*/



void givens(int nx, double **vect, double *root, double *a)
{

#ifdef COMMENT

      common/memory/ vect(70,70),root(70),a(2485),b(70,5),iii(21006)

#endif

    double eta, theta, del1, delta, small, delbig, theta1, toler;
    double rpower, rpow1, rand1;
    double factor, anorm, alimit, rootl;
    int i,j,k,jump;
    int nsize;		/* number of elements in the packed array */
    int id, ia, nm1, nm2;
    double **b;

    b = dmatrix(1,5,1,nx);
    eta    = ETA;
    theta  = THETA;
  
    del1   = eta/1.e2;
    delta  = POW2(eta) * 1.e2;
    small  = POW2(eta)/1.e2;
    delbig = theta * delta/1.e3;
    theta1 = 1.e3/theta;
    toler  = TOLER;
    rpower = RPOWER;
    rpow1  = rpower/2.0;
    rand1  = rpower-3.0;

    if (nx <= 1) {
        root[1] = a[1];
        vect[1][1] = 1.0;
        free_dmatrix(b,1,5,1,nx);
        return;
    }

      
    nsize = (nx*(nx+1))/2;

    /*
     * scale matrix to euclidean norm of 1.  scale factor is anorm.
     */
    factor = 0.0;
    for (i=1;i<=nsize;i++)
        factor = max(factor, fabs(a[i]));
    if (factor == 0) {
        fprintf(stderr,"error: Divide by 0 in givens()\n");
        free_dmatrix(b,1,5,1,nx);
        return;
    }
    anorm = 0.0;
    for (i=1,j=1,k=1;i<=nsize;i++) {
        if  (i == j) {
            anorm += POW2(a[i]/factor)/2.0;
            k++;
            j += k;
        }
        else
           anorm += POW2(a[i]/factor);
    }
    anorm = sqrt(anorm * 2.0)*factor;
    for (i=1;i<=nsize;i++)
        a[i] /= anorm;
    alimit = 1.0;
    /*
     *    -------------------------------------------
     *    1. tridia section
     *    -------------------------------------------
     */
    id = 0;
    ia = 1;
    nm1 = nx-1;
    nm2 = nx-2;

    if (nm2 != 0) {
        for (j=1;j<=nm2;j++) {
            int jp1, jp2, ii, jj, ic;
            double sum, temp, ak;
            /*
             *      j       counts row  of a-matrix to be diagonalized
             *      ia      start of non-codiagonal elements in the row
             *      id      index of codiagonal element on row being codiagonalized.
             */
            jp1 = j+1;
            jp2 = j+2;
            ia += jp2;
            id += jp1;
            /*
             *      sum squares of non-codiagonal elements in row j
             */
            ii = ia;
            sum = 0.0;
            for (i=jp2;i<=nx;i++) {
                sum += POW2(a[ii]);
                ii += i;
            }
            temp = a[id];
            
            if (sum <= small) {
                /*
                 *      no transformation necessary if all the non-codiagonal
                 *      elements are tiny.
                 */
                b[1][j] = temp;
                a[id] = 0.0;
                continue;
            }
            sum = sqrt(sum + POW2(temp));
            /*
             *      now complete the sum of off-diagonal squares
             */
            b[1][j] = -sign(sum,temp);
            /*
             *      new codiagonal element
             *      first non-zero element of this w-vector
             */
            b[2][jp1] = sqrt((1.0 + fabs(temp)/sum)/2.0);
            /*
             * form rest of the w-vector elements
             */
            temp = sign( (5.e-1)/(b[2][jp1] * sum),temp);
            ii = ia;
            for (i=jp2; i<=nx; i++) {
                b[2][i] = a[ii] * temp;
                ii += i;
            }
            /*
             *      form p-vector and scalar. p-vector=a-matrix*w-vector.
             *      scalar=w-vector*p-vector
             */
            ak=0.0;
            /*
             *      ic      location of next diagonal element
             */
            ic = id + 1;
            for (i=jp1;i<=nx;i++) {
                jj = ic;
                temp = 0.0;
                for (ii=jp1;ii<=nx;ii++) {
                    /*
                     *      i       runs over the non-zero p-elements
                     *      ii      runs over elements of w-vector
                     */
                    temp += b[2][ii] * a[jj];
                    /*
                     *      change incrementing mode at the diagonal elements.
                     */
                    if (ii >= i)
                        jj += ii;
                    else
                        jj++;
                }
                /*
                 *      build up the k-scalar (ak)
                 */
                ak += temp * b[2][i];
                b[1][i] = temp;
                /*
                 *      move ic to top of next a-matrix @row@
                 */
                ic += i;
            }
            /*
             *      form the q-vector
             */
            for (i=jp1;i<=nx;i++)
                b[1][i] -= ak * b[2][i];
            /*
             *      transform the rest of the a-matrix
             *      jj      start-1 of the rest of the a-matrix
             */
            jj = id;
            /*
             *      move w-vector into the old a-matrix locations to save space
             *      i       runs over the significant elements of the w-vector
             */
            for (i=jp1;i<=nx;i++) {
                a[jj] = b[2][i];
                for (ii=jp1;ii<=i;ii++) {
                    jj++;
                    a[jj] -= 2.0 * (b[1][i] * b[2][ii] + b[2][i] * b[1][ii]);
                }
                jj += j;
            }
        }
    }
    /*
     * move last codiagonal element out into its proper place
     */
    b[1][nm1] = a[nsize-1];
    a[nsize-1] = 0.0;
    b[1][nx] = 0.0;
    /*
     *     -------------------------------------------
     *     2.sturm section
     *     sturm sequence iteration to obtains roots
     *     of tridiagonal forms.
     *     -------------------------------------------
     *     move diagonal elements into second n elements of b-vector.
     *     this is a more convenient indexing position.
     *     also, put square of codiagonal elements in third n elements.
     */
    jump=1;
    for (j=1;j<=nx;j++) {
        b[2][j] = a[jump];
        b[3][j] = POW2(b[1][j]);
        jump += j+1;
    }
    for (i=1;i<=nx;i++)
        root[i] = alimit;
    rootl = -alimit;
    /*    
     *     isolate the roots.  the nx lowest roots are found, lowest first
     */
    for (i=1;i<=nx;i++) {
        int nomtch, nom;
        double f0, rootx, trial, bb;

        /*
         *       find current @best@ upper bound
         */
        rootx = alimit;
        for (j=i;j<=nx;j++)
            rootx = min(rootx, root[j]);
        root[i] = rootx;
        /*
         *     get improved trial root
         */
        do {
            trial = (rootl + root[i]) * 5.e-1;
            if (trial == rootl || trial == root[i]) 
                break;
        /*
         *     form sturm sequence ratios, using ortega@s algorithm (modified).
         *     nomtch is the number of roots less than the trial value.
         */
            nomtch = nx;
            j = 1;
            
            do {
                f0 = b[2][j] - trial;
                if (fabs(f0) >=  theta1) { 
                    do {     
                        if (f0 >= 0.0) 
                            nomtch--;
                        j++;
                        if (j > nx)     
                            break;
        /*
         *  since matrix is normed to unity, magnitude of b(j,3) is less than
         *  one, so overflow is not possible at the division step, since
         *  f0 is greater than theta1.
         *  f0=b(j,2) - trial - b(j-1,3)/f0
         */
                        bb = b[3][j-1]/f0;
                        if (f0 == 0.0) 
                            bb = 0.0;
                        f0 = b[2][j] - trial - bb;
                    } while (1);
                }
                else {
                    j += 2;
                    nomtch--; 
                }       
            } while (j <= nx);        
            /*
             *     fix new bounds on roots
             */
            if (nomtch < i) 
                rootl = trial;
            else {
                root[i] = trial;
                nom = min(nx,nomtch);
                root[nom] = trial;
            }
        } while (1);
    }
    /*
     *   -------------------------------------------
     *   3.trivec section
     *   quit now if no vectors were requested
     *   initialize vector array
     *   -------------------------------------------
     */
    for (j=1;j<=nx;j++)
        for (i=1;i<=nx;i++)
            vect[j][i] = 1.0;

    for (i=1;i<=nx;i++) {
        double elim1, elim2, aroot, temp;
        int l, j1, iter;

        aroot = root[i];
        /*
         *     orthogonalize if roots are close.
         */
        if ((i == 1) ||
            /*
             * the absolute value in the next test is to assure that the trivec
             * section is independent of the order of the eigenvalues.
             */
            (fabs(root[i-1]-aroot)-toler >= 0))
                ia = -1;
        ia++;
 
        elim1 = a[1] - aroot;
        elim2 = b[1][1];
        jump = 1;
        for (j=1;j<=nm1;j++) {
            jump += j+1;
            /*
             *  get the correct pivot equation for this step.
             */
            if (fabs(elim1) > fabs(b[1][j])) {
                /*
                 *  first (elim1) equation is the pivot this time.  case 1.
                 */
                b[2][j] = elim1;
                b[3][j] = elim2;
                b[4][j] = 0.0;
                temp = b[1][j]/elim1;
                elim1 = a[jump] - aroot - temp * elim2;
                elim2 = b[1][j+1];
            }
            else {
                /*
                 *  second equation is the pivot this time.  case 2.
                 */
                b[2][j] = b[1][j];
                b[3][j] = a[jump] - aroot;
                b[4][j] = b[1][j+1];
                temp = 1.0;
                if (fabs(b[1][j]) > theta1) 
                    temp = elim1/b[1][j];
                elim1 = elim2 - temp * b[3][j];
                elim2 = -temp * b[1][j+1];
            }
            /*
             *  save factor for the second iteration.
             */
            b[5][j] = temp;
        }
        b[2][nx]  = elim1;
        b[3][nx]  = 0.0;
        b[4][nx]  = 0.0;
        b[4][nm1] = 0.0;
        iter = 1;

        if (ia != 0) {
            /*
             *  produce a random vector
             */
            for (j=1;j<=nx;j++) {
        /*
         *  generate pseudorandom numbers with uniform distribution in (-1,1).
         *  this random number scheme is of the form...
         *  rand1=amod((2**12&3)*rand1,2**23)
         *  it has a period of 2**21 numbers.
         */
                rand1 = fmod(4099.0 * rand1,rpower);
                vect[i][j] = rand1/rpow1-1.0;
/* fprintf(stderr,"%f %f\n", rand1/rpow1-1.0, rand1); */
            }
        }
        /*
         *  back substitute to get this vector.
         */
        do {
            l = nx+1;
            for (j=1;j<=nx;j++) {
                l--;
                while (1) {
                    if (l - nm1 > 0)
                        elim1 = vect[i][l];
                    else if (l - nm1 == 0)
                        elim1 = vect[i][l] - vect[i][l+1] * b[3][l];
                    else
                        elim1 = vect[i][l] - vect[i][l+1] * b[3][l] 
                                          - vect[i][l+2] * b[4][l];
                /*
                 *  if overflow is conceivable, scale the vector down.
                 *  this approach is used to avoid machine-dependent and system-
                 *  dependent calls to overflow routines.
                 */
                    if (fabs(elim1) <= delbig) { 
                        temp = b[2][l];
                        if (fabs(b[2][l]) < delta) 
                            temp = delta;
                        vect[i][l] = elim1/temp;
                        break;
                    }
                    else {
                        /*
                         *  vector is too big.  scale it down.
                         */
                        for (k=1;k<=nx;k++)
                            vect[i][k] /= delbig;
                    }
                }
            }
            if (iter == 2) { 
                /*
                 *  orthogonalize this repeated-root vector 
                 *  to others with this root.
                 */
                if (ia != 0) for (j1=1;j1<=ia;j1++) {
                    k = i - j1;
                    temp = 0.0;
                    for (j=1;j<=nx;j++) 
                        temp += vect[i][j] * vect[k][j];
                    for (j=1;j<=nx;j++)
                        vect[i][j] -= temp * vect[k][j];
                }
                /* always, kuik */
                if (iter != 1)
                    break;
            }
            else
            /*
             *  second iteration.  (both iterations for repeated-root vectors).
             */
                iter++;
            elim1 = vect[i][1];
            for (j=1;j<=nm1;j++) {
                if (b[2][j] != b[1][j]) { 
                    /*
                     *  case one.
                     */
                    vect[i][j] = elim1;
                    elim1 = vect[i][j+1] - elim1 * b[5][j];
                }
                else {
                    /*
                     *  case two.
                     */
                    vect[i][j] = vect[i][j+1];
                    elim1 -= vect[i][j+1] * b[5][j];
                }
            }
            vect[i][nx] = elim1;

        } while (1);
        /*
         *  normalize the vector
         */
        elim1 = 0.0;
        for (j=1;j<=nx;j++) 
            elim1 = max(fabs(vect[i][j]),elim1);
        temp = 0.0;
        for (j=1;j<=nx;j++) {
            elim2 = vect[i][j]/elim1;
            temp += POW2(elim2);
        }
        temp = 1.0/(sqrt(temp) * elim1);
        for (j=1;j<=nx;j++) {
            vect[i][j] *= temp;
            if (fabs(vect[i][j]) < del1) 
                vect[i][j] = 0.0;
        }
    }
    /*
     *      simvec section.
     *      rotate codiagonal vectors into vectors of original array
     *      loop over all the transformation vectors
     */
    if (nm2 != 0) {
        int im;
        double temp;
        
        jump = nsize - (nx+1);
        im = nm1;
        for (i=1;i<=nm2;i++) {
            int j1 = jump;
            /*
             * move a transformation vector out into better indexing position.
             */
            for (j=im;j<=nx;j++) {
                b[2][j] = a[j1];
                j1 += j;
            }
            /*
             *   modify all requested vectors.
             */
            for (k=1;k<=nx;k++) {
                temp = 0.0;
                /*
                 *   form scalar product of transformation 
                 *   vector with eigenvector
                 */
                for (j=im;j<=nx;j++)
                    temp += b[2][j] * vect[k][j];
                temp *= 2;
                for (j=im;j<=nx;j++)
                    vect[k][j] -= temp * b[2][j];
            }
            jump -= im;
            im--;
        }
    }
    /*
     *      restore roots to their proper size.
     */
    for (i=1;i<=nx;i++) 
        root[i] *= anorm;
    free_dmatrix(b,1,5,1,nx);
/*
for (k=1;k<=nx;k++)
    printf("%d = %g\n",k, root[k]);

for (k=1;k<=nx;k++){
  for (j=1;j<=nx;j++)
    printf(" %.2f ", vect[k][j]);   
    printf("\n");
}

*/

}

#else

/*......................................................*/
/*
 * Numerical Recipes in C
 *
 */

#define nrerror(s)	fprintf(stderr,s)

void tred2(double **a, int n, double d[], double e[])
/*
 * Householder reduction of a real, symmetric matrix a[1..n][1..n]. On
 * output, a is replaced by the orthogonal matrix Q effecting the
 * transformation. d[1..n] returns the diagonal elements of the
 * tridiagonal matrix, and e[1..n] the off-diagonal elements, with e[1]=0.
 * Several statements, as noted in comments, can be omitted if only
 * eigenvalues are to be found, in which case a contains no useful
 * information on output. Otherwise they are to be included. 
 */
{
    int l,k,j,i;
    double scale,hh,h,g,f;

    for (i=n;i>=2;i--) {
        l=i-1;
        h=scale=0.0;
        if (l > 1) {
            for (k=1;k<=l;k++)
                scale += fabs(a[i][k]);
            /* 
             * Skip transformation. 
             */
            if (scale == 0.0)
                e[i]=a[i][l];
            else {
                for (k=1;k<=l;k++) {
                    /* 
                     * Use scaled a's for transformation. 
                     */
                    a[i][k] /= scale;     
                    /* 
                     * Form sigma in h. 
                     */
                    h += a[i][k]*a[i][k]; 
                }
                f=a[i][l];
                g=(f >= 0.0 ? -sqrt(h) : sqrt(h));
                e[i]=scale*g;
                h -= f*g; /* Now h is equation (11.2.4). */
                /* 
                 * Store u in the ith row of a. 
                 */
                a[i][l]=f-g; 
                f=0.0;
                for (j=1;j<=l;j++) {
                    /* 
                     * Next statement can be omitted if eigenvectors not wanted 
                     */
                    a[j][i]=a[i][j]/h; /* Store u/H in ith column of a. */
                    g=0.0;             /* Form an element of A . u in g. */
                    for (k=1;k<=j;k++)
                        g += a[j][k]*a[i][k];
                    for (k=j+1;k<=l;k++)
                        g += a[k][j]*a[i][k];
                    /* 
                     * Form element of p in temporarily unused element of e
                     */
                    e[j]=g/h; 
                    f += e[j]*a[i][j];
                }
                /* 
                 * Form K, equation (11.2.11). 
                 */
                hh=f/(h+h); 
                /* 
                 * Form q and store in e overwriting p. 
                 */
                for (j=1;j<=l;j++) { 
                    f=a[i][j];
                    e[j]=g=e[j]-hh*f;
                    /* 
                     * Reduce a, equation (11.2.13). 
                     */   
                    for (k=1;k<=j;k++) 
                        a[j][k] -= (f*e[k]+g*a[i][k]);
                }
            }
        } else
            e[i]=a[i][l];
        d[i]=h;
    }
    /* 
     * Next statement can be omitted if eigenvectors not wanted 
     */
    d[1]=0.0;
    e[1]=0.0;
    /* 
     * Contents of this loop can be omitted if eigenvectors not
     * wanted except for statement d[i]=a[i][i]; 
     */
    for (i=1;i<=n;i++) { /* Begin accumulation of transformation matrices.  */
        l=i-1;
        /* 
         * This block skipped when i=1. 
         */
        if (d[i] != 0.0) { 
            for (j=1;j<=l;j++) {
                g=0.0;
                /* 
                 * Use u and u=H stored in a to form P . Q. 
                 */
                for (k=1;k<=l;k++) 
                    g += a[i][k]*a[k][j];
                for (k=1;k<=l;k++)
                    a[k][j] -= g*a[k][i];
            }
        }
        /* 
         * This statement remains. 
         */
        d[i]=a[i][i]; 
        /* 
         * Reset row and column of a to identity
         * matrix for next iteration. 
         */
        a[i][i]=1.0; 
        for (j=1;j<=l;j++) a[j][i]=a[i][j]=0.0;
    }
}


#define SIGN(a,b) ((b) >= 0.0 ? fabs(a) : -fabs(a))

void tqli(double d[], double e[], int n, double **z)
/*
 * QL algorithm with implicit shifts, to determine the eigenvalues and
 * eigenvectors of a real, symmetric, tridiagonal matrix, or of a real,
 * symmetric matrix previously reduced by tred2 (11.2). On input, d[1..n]
 * contains the diagonal elements of the tridiagonal matrix. On output, it
 * returns the eigenvalues. The vector e[1..n] inputs the subdiagonal
 * elements of the tridiagonal matrix, with e[1] arbitrary. On output e is
 * destroyed. When finding only the eigenvalues, several lines may be
 * omitted, as noted in the comments. If the eigenvectors of a tridiagonal
 * matrix are desired, the matrix z[1..n][1..n] is input as the identity
 * matrix. If the eigenvectors of a matrix that has been reduced by tred2
 * are required, then z is input as the matrix output by tred2. In either
 * case, the kth column of z returns the normalized eigenvector
 * corresponding to d[k]. 
 */
{
/*
    double pythag(double a, double b);
    */
    int m,l,iter,i,k;
    double s,r,p,g,f,dd,c,b;

    /* 
     * Convenient to renumber the elements of e
     */ 
    for (i=2;i<=n;i++) e[i-1]=e[i];
    e[n]=0.0;
    for (l=1;l<=n;l++) {
        iter=0;
        do {
            /* 
             * Look for a single small subdiagonal element 
             * to split the matrix. 
             */
            for (m=l;m<=n-1;m++) { 
                dd=fabs(d[m])+fabs(d[m+1]);
                if ((double)(fabs(e[m])+dd) == dd) break;
            }
            if (m != l) {
                if (iter++ == 30) {
                    nrerror("Too many iterations in tqli\n");
                    return;
                }
                g=(d[l+1]-d[l])/(2.0*e[l]); /* Form shift. */
                r=pythag(g,1.0);
                g=d[m]-d[l]+e[l]/(g+SIGN(r,g)); /* This is dm - ks. */
                s=c=1.0;
                p=0.0;
                /*
                 * A plane rotation as in the original QL, 
                 * followed by Givens rotations to restore 
                 * tridiagonal form.
                 */
                for (i=m-1;i>=l;i--) { 
                    f=s*e[i];
                    b=c*e[i];
                    e[i+1]=(r=pythag(f,g));
                     /*
                      * Recover from underflow. 
                      */
                    if (r == 0.0) {
                        d[i+1] -= p;
                        e[m]=0.0;
                        break;
                    }
                    s=f/r;
                    c=g/r;
                    g=d[i+1]-p;
                    r=(d[i]-g)*s+2.0*c*b;
                    d[i+1]=g+(p=s*r);
                    g=c*r-b;
                    /* 
                     * Next loop can be omitted if eigenvectors not wanted 
                     */
                    for (k=1;k<=n;k++) { /* Form eigenvectors. */
                        f=z[k][i+1];
                        z[k][i+1]=s*z[k][i]+c*f;
                        z[k][i]=c*z[k][i]-s*f;
                    }
                }
                if (r == 0.0 && i >= l) continue;
                d[l] -= p;
                e[l]=g;
                e[m]=0.0;
            }
        } while (m != l);
    }
}

/*
 * Sorts eigenvalues into assending order
 */
void eigsrt(double d[], double **v, int nx)
{
    int k,i,j;
    double p;
    
    for (i=1;i<nx;i++) {
        p = d[k=i];
        for (j=i+1;j<=nx;j++) 
            if (d[j] < p)
                p = d[k=j];
        if (k != i) {
            d[k] = d[i];
            d[i] = p;
            for (j=1;j<=nx;j++) {
                p = v[j][i];
                v[j][i] = v[j][k];
                v[j][k] = p;
            }
        }
    }
}

#define ROTATE(a,i,j,k,l) g=a[i][j];h=a[k][l];a[i][j]=g-s*(h+g*tau);\
a[k][l]=h+s*(g-h*tau);

void jacobi(double **a, int n, double d[], double **v, int *nrot)
/*
 * Computes all eigenvalues and eigenvectors of a real symmetric matrix
 * a[1..n][1..n]. On output, elements of a above the diagonal are
 * destroyed. d[1..n] returns the eigenvalues of a. v[1..n][1..n] is a
 * matrix whose columns contain, on output, the normalized eigenvectors of
 * a. nrot returns the number of Jacobi rotations that were required.
 */
{
    int j,iq,ip,i;
    double tresh,theta,tau,t,sm,s,h,g,c,*b,*z;

    b=dvector(1,n);
    z=dvector(1,n);
    for (ip=1;ip<=n;ip++) { /* Initialize to the identity matrix. */
        for (iq=1;iq<=n;iq++) v[ip][iq]=0.0;
        v[ip][ip]=1.0;
    }
    for (ip=1;ip<=n;ip++) { /* Initialize b and d to the diagonal of a.  */
        b[ip]=d[ip]=a[ip][ip];
        z[ip]=0.0; /* This vector will accumulate terms 
                      of the form tapq as in equation (11.1.14).*/
    }
    *nrot=0;
    for (i=1;i<=50;i++) {
        sm=0.0;
        for (ip=1;ip<=n-1;ip++) { /* Sum off-diagonal elements. */
            for (iq=ip+1;iq<=n;iq++)
                sm += fabs(a[ip][iq]);
        }
        /* 
         * The normal return, which relies on quadratic convergence
         * to machine underflow.
         */
        if (sm == 0.0) { 
            free_dvector(b,1);
            free_dvector(z,1);
            return;
        }
        if (i < 4)
            tresh=0.2*sm/(n*n); /* ...on the first three sweeps. */
        else
            tresh=0.0; /* ...thereafter. */
        for (ip=1;ip<=n-1;ip++) {
            for (iq=ip+1;iq<=n;iq++) {
                g=100.0*fabs(a[ip][iq]);
                /*
                 * After four sweeps, skip the rotation 
                 * if the off-diagonal element is small.
                 */
                if (i > 4 && (float)(fabs(d[ip])+g) == (float)fabs(d[ip])
                    && (float)(fabs(d[iq])+g) == (float)fabs(d[iq]))
                        a[ip][iq]=0.0;
                else if (fabs(a[ip][iq]) > tresh) {
                    h=d[iq]-d[ip];
                    if ((float)(fabs(h)+g) == (float)fabs(h))
                        t=(a[ip][iq])/h;  /* t = 1/(2 theta) */
                    else {
                        theta=0.5*h/(a[ip][iq]); /* Equation (11.1.10). */
                        t=1.0/(fabs(theta)+sqrt(1.0+theta*theta));
                        if (theta < 0.0) t = -t;
                    }
                    c=1.0/sqrt(1+t*t);
                    s=t*c;
                    tau=s/(1.0+c);
                    h=t*a[ip][iq];
                    z[ip] -= h;
                    z[iq] += h;
                    d[ip] -= h;
                    d[iq] += h;
                    a[ip][iq]=0.0;
                    /*
                     * Case of rotations 1 <= j < p.
                     */
                    for (j=1;j<=ip-1;j++) { 
                        ROTATE(a,j,ip,j,iq)
                    }
                    /*
                     * Case of rotations p < j < q.
                     */
                    for (j=ip+1;j<=iq-1;j++) { 
                        ROTATE(a,ip,j,j,iq)
                    }
                    /*
                     * Case of rotations q < j <= n.
                     */
                    for (j=iq+1;j<=n;j++) { 
                        ROTATE(a,ip,j,iq,j)
                    }
                    for (j=1;j<=n;j++) {
                        ROTATE(v,j,ip,j,iq)
                    }
                    ++(*nrot);
                }
            }
        }
        for (ip=1;ip<=n;ip++) {
            b[ip] += z[ip];
            d[ip]=b[ip]; /* Update d with the sum of tapq, and reinitialize z. */
            z[ip]=0.0; 
        }
    }
    nrerror("Too many iterations in routine jacobi");
    free_dvector(b,1);
    free_dvector(z,1);
}





/*
*     -------------------------------------------
*     eigenvalues and eigenvectors by the givens
*     method
*     -------------------------------------------
*     the parameters for the routine are...
*     nx     order of matrix
*     a      matrix stored by columns in packed upper triangular form,
*            i.e. occupying nx*(nx+1)/2 consecutive locations.
*     root   array to hold the eigenvalues.
*     vect   eigenvector array. each column will hold an eigenvector
*            for the corresponding root.
*     The results appear in root and vect. 
*/


void givens(int nx, double **vect, double *root, double *a)
{
    int l,j,k;
    double *e;

    if (nx <= 1) {
        root[1] = a[1];
        vect[1][1] = 1.0;
        return;
    }

    for (k=1,l=1;k<=nx;k++) {
        for (j=1;j<k;j++) {
             vect[k][j] = a[l];
             vect[j][k] = a[l++];
        }
        vect[k][k] = a[l++];
    }

#ifdef do_jacobi
{
    int nrot;
    double **aa;
    aa=dmatrix(1,nx,1,nx);
    for (k=1,l=1;k<=nx;k++) {
        for (j=1;j<k;j++) {
             aa[k][j] = a[l];
             aa[j][k] = a[l++];
        }
        aa[k][k] = a[l++];
    }
    jacobi(aa, nx, root, vect, &nrot);
    free_dmatrix(aa,1,nx,1,nx);
   // eigsrt(root, vect, nx);
}

#endif

    e = dvector(1, nx);
    tred2(vect, nx, root, e);
    tqli(root, e, nx, vect);
    free_dvector(e,1);

    for (k=1;k<=nx;k++)
        for (j=1;j<k;j++) {
            double p = vect[k][j];
            vect[k][j] = vect[j][k]; 
            vect[j][k] = p;
        } 
/*

for (k=1;k<=nx;k++)
    printf("%d = %g\n",k, root[k]);

for (k=1;k<=nx;k++){
  for (j=1;j<=nx;j++)
    printf(" %.2f ", vect[k][j]);   
  printf("\n");
}
*/

}

#endif

