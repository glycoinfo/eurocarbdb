/********************************************************************/
/*                                 dft.c                            */
/*                                                                  */
/********************************************************************/

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
#include <assert.h>

#define FALSE	0
#define TRUE	1

#ifndef M_PI
#define M_PI 3.14159265358979323846
#endif

/*
      integer function fourt(datar,datai,nn,ndim,
     +                       ifrwd,icplx,workr,worki)
c
      dimension datar(1),datai(1),nn(1),workr(1),worki(1),ifact(20)
c
c     the cooley-tukey fast fourier transform in usasi basic fortran
c     evaluates complex fourier series for complex or real functions.
c     that is, it computes
c     ftran(j1,j2,...)=sum(data(i1,i2,...)*w1**(i1-1)*(j1-1)
c                                         *w2**(i2-1)*(j2-1)*...),
c     where w1=exp(-2*pi*sqrt(-1)/nn(1)), w2=exp(-2*pi*sqrt(-1)/nn(2)),
c     etc. and i1 and j1 run from 1 to nn(1), i2 and j2 run from 1 to
c     nn(2), etc.  there is no limit on the dimensionality (number of
c     subscripts) of the array of data.  the program will perform
c     a three-dimensional fourier transform as easily as a one-dimen-
c     sional one, tho in a proportionately greater time.  an inverse
c     transform can be performed, in which the sign in the exponentials
c     is +, instead of -.  if an inverse transform is performed upon
c     an array of transformed data, the original data will reappear,
c     multiplied by nn(1)*nn(2)*...  the array of input data may be
c     real or complex, at the programmers option, with a saving of
c     about thirty per cent in running time for real over complex.
c     (for fastest transform of real data, nn(1) should be even.)
c     the transform values are always complex, and are returned in the
c     original array of data, replacing the input data.  the length
c     of each dimension of the data array may be any integer.  the
c     program runs faster on composite integers than on primes, and is
c     particularly fast on numbers rich in factors of two.
c     timing is in fact given by the following formula.  let ntot be the
c     total number of points (real or complex) in the data array, that
c     is, ntot=nn(1)*nn(2)*...  decompose ntot into its prime factors,
c     such as 2**k2 * 3**k3 * 5**k5 * ...  let sum2 be the sum of all
c     the factors of two in ntot, that is, sum2 = 2*k2.  let sumf be
c     the sum of all other factors of ntot, that is, sumf = 3*k3+5*k5+..
c     the time taken by a multidimensional transform on these ntot data
c     is t = t0 + t1*ntot + t2*ntot*sum2 + t3*ntot*sumf.  for the par-
c     ticular implementation fortran 32 on the cdc 3300 (floating point
c     add time = six microseconds),
c     t = 3000 + 600*ntot + 50*ntot*sum2 + 175*ntot*sumf microseconds
c     on complex data.
c
c     implementation of the definition by summation will run in a time
c     proportional to ntot**2.  for highly composite ntot, the savings
c     offered by cooley-tukey can be dramatic.  a matrix 100 by 100 will
c     be transformed in time proportional to 10000*(2+2+2+2+5+5+5+5) =
c     280,000 (assuming t2 and t3 to be roughly comparable) versus
c     10000**2 = 100,000,000 for the straightforward technique.
c
c     the cooley-tukey algorithm places two restrictions upon the
c     nature of the data beyond the usual restriction that
c     the data from one cycle of a periodic function.  they are--
c     1.  the number of input data and the number of transform values
c     must be the same.
c     2. considering the data to be in the time domain,
c     they must be equi-spaced at intervals of dt.  further, the trans-
c     form values, considered to be in frequency space, will be equi-
c     spaced from 0 to 2*pi*(nn(i)-1)/(nn(i)*dt) at intervals of
c     2*pi/(nn(i)*dt) for each dimension of length nn(i).  of course,
c     dt need not be the same for every dimension.
c
c     the calling sequence is--
c     call fourt(datar,datai,nn,ndim,ifrwd,icplx,workr,worki)
c
c     datar and datai are the arrays used to hold the real and imaginary
c     parts of the input data on input and the transform values on
c     output.  they are floating point arrays, multidimensional with
c     identical dimensionality and extent.  the extent of each dimension
c     is given in the integer array nn, of length ndim.  that is,
c     ndim is the dimensionality of the arrays datar and datai.
c     ifrwd is an integer used to indicate the direction of the fourier
c     transform.  it is non-zero to indicate a forward transform
c     (exponential sign is -) and zero to indicate an inverse transform
c     (sign is +).  icplx is an integer to indicate whether the data
c     are real or complex.  it is non-zero for complex, zero for real.
c     if it is zero (real) the contents of array datai will be assumed
c     to be zero, and need not be explicitly set to zero.  as explained
c     above, the transform results are always complex and are stored
c     in datar and datai on return.  workr and worki are arrays used
c     for working storage.  they are not necessary if all the dimensions
c     of the data are powers of two.  in this case, the arrays may be
c     replaced by the number 0 in the calling sequence.  thus, use of
c     powers of two can free a good deal of storage.  if any dimension
c     is not a power of two, these arrays must be supplied.  they are
c     floating point, one dimensional of length equal to the largest
c     array dimension, that is, to the largest value of nn(i).
c     workr and worki, if supplied, must not be the same arrays as datar
c     or datai.  all subscripts of all arrays begin at 1.
c
c     example 1.  three-dimensional forward fourier transform of a
c     complex array dimensioned 100 by 16 by 13.
c     dimension datar(100,16,13),datai(100,16,13),workr(100),worki(100)
c     dimension nn(3)
c     nn(1)=100
c     nn(2)=16
c     nn(3)=13
c     call fourt(datar,datai,nn,3,1,1,workr,worki)
c
c     example 2.  one-dimensional forward transform of a real array of
c     length 64.
c     dimension datar(64),datai(64)
c     call fourt(datar,datai,64,1,1,0,0,0)
c
c     there are no error messages or error halts in this program.  the
c     program returns immediately if ndim or any nn(i) is less than one.
c
c     the sine and cosine values required for the transform are
c     generated recursively.  if double precision is available, it is
c     strongly urged that the following variables be so declared to
c     reduce accumulation of roundoff error--

      double precision twopi,theta,wstpr,wstpi,wminr,wmini,wr,wi,wtemp
     *  ,thetm,wmstr,wmsti,twowr,sr,si,oldsr,oldsi,stmpr,stmpi

c     in addition, twopi should be assigned a sufficiently precise
c     value and the various calls to the functions cos and sin
c     should be changed to dcos and dsin.
c
c     program by norman brenner from the basic algorithm by charles
c     rader (both of mit lincoln laboratory).  may 1967.  the idea
c     for the bit reversal was suggested by ralph alter (also mit ll).
c     adapted from the work of james w. cooley and john w. tukey,
c     an algorithm for the machine calculation of complex fourier
c     series, math. comput. 19, 90 (april 1965), 297-301.
c
c     fourt modified from a subroutine to an integer function by
c     c. potter (ncsa). jan 1988.
c
*/

#define IFACTMAX	21

static int fourt(float *datar, float *datai, int *nn, int ndim,
                 int ifrwd, int icplx, float *workr, float *worki)
{
    int ifact[IFACTMAX];
    int i,i1,i2,i3,j,j1,j2,j3,iff,ifp1,ifp2,idiv,iquot,irem,inon2;
    int imax,jmax,nhalf,jmin,j2max,j3max;
    int icase,idim,ntot,n,np1,np2,ntwo,non2p,m,mmax,mmin;
    int ifmin,np2hf,i1max,i2max,i1rng,np0,nprev,imin,istep,mstep; 
    float twopi,theta,wstpr,wstpi,wminr,wmini,wr,wi,wtemp,wmstr,wmsti,thetm;
    float oldsr,oldsi,stmpr,stmpi,sumr,sumi,twowr,sr,si,difr,difi,tempr,tempi;

    /*
     * arrays must start at 1
     */
    datar--;
    datai--;
    nn--;
    workr--;
    worki--;

    if (ndim < 1)
        return 0;
    ntot = 1;
    for (idim=1;idim<=ndim;idim++)
        ntot = ntot * nn[idim];
    twopi = M_PI * 2.0;
    /*
     *     main loop for each dimension
     */
    np1 = 1;
    for (idim=1;idim<=ndim;idim++) {
        n = nn[idim];
        np2 = np1 * n;
        if (n < 1)
            return 0;
        if (n == 1) {
            /*
             * if size is 1, skip this dimension
             */
            np0   = np1;
            np1   = np2;
            nprev = n;
            continue;
        }

        /*
         *     is n a power of two and if not, what are its factors
         */
        m = n;
        ntwo = np1;
        iff  = 1;
        idiv = 2;
        while (1) {
            iquot = m/idiv;
            irem = m-idiv*iquot;

            if (iquot < idiv) { 
                inon2 = iff;
                if (irem == 0) 
                    ntwo += ntwo;
                else
                    ifact[iff] = m;
                break;
            }
            if (irem != 0) {
                idiv = 3;
                inon2 = iff;
                while (1) {
                    iquot = m/idiv;
                    irem = m-idiv*iquot;
                    if (iquot < idiv) {
                        ifact[iff] = m;
                        break;
                    }
                    if (irem == 0) {
                        ifact[iff] = idiv;
                        iff++;
                        m = iquot;
                    }
                    else
                        idiv += 2;
                } 
                break; 
            }
            ntwo += ntwo;
            ifact[iff] = idiv;
            iff++;
            m = iquot;
        }
        non2p = np2/ntwo;
        assert(iff <= IFACTMAX);

        /*
         * separate four cases--
         *    1. complex transform
         *    2. real transform for the 2nd, 3rd, etc. dimension.  method--
         *       transform half the data, supplying the other half by con-
         *       jugate symmetry.
         *    3. real transform for the 1st dimension, n odd.  method--
         *       set the imaginary parts to zero.
         *    4. real transform for the 1st dimension, n even.  method--
         *       transform a complex array of length n/2 whose real parts
         *       are the even numbered real values and whose imaginary parts
         *       are the odd-numbered real values.  unscramble and supply
         *       the second half by conjugate symmetry.
         */
        icase = 1;
        ifmin = 1;
        if (icplx == 0) {
            icase = 2;
            if (idim <= 1) {
                icase = 3;
                if (ntwo > np1) {
                    icase = 4;
                    ifmin = 2;
                    ntwo /= 2;
                    n    /= 2;
                    np2  /= 2;
                    ntot /= 2;
                    i = 1;
                    for (j=1;j<=ntot;j++) {
                        datar[j] = datar[i];
                        datai[j] = datar[i+1];
                        i += 2;
                    }
                }
            }
        }
        /*
         *     shuffle data by bit reversal, since n=2**k.  as the shuffling
         *     can be done by simple interchange, no working array is needed
         */
        if (non2p <= 1) {
            np2hf = np2/2;
            j = 1;
            for (i2=1;i2<=np2;i2+=np1) {
                if (j < i2) {
                    i1max = i2+np1-1;
                    for (i1=i2;i1<=i1max;i1++) {
                        for (i3=i1;i3<=ntot;i3+=np2) {
                            j3 = j+i3-i2;
                            tempr     = datar[i3];
                            tempi     = datai[i3];
                            datar[i3] = datar[j3];
                            datai[i3] = datai[j3];
                            datar[j3] = tempr;
                            datai[j3] = tempi;
                        }
                    }
                }
                m = np2hf;
                while (j > m) {
                    j -= m;
                    m /= 2;
                    if (m < np1) 
                        break;
                }
                j += m;
            }
        }
        else {
            /*
             *     shuffle data by digit reversal for general n
             */
            for (i1=1;i1<=np1;i1++) {
                for (i3=i1;i3<=ntot;i3 += np2) {
                    j = i3;
                    for (i=1;i<=n;i++) {
                        if (icase != 3) {
                            workr[i] = datar[j];
                            worki[i] = datai[j];
                        }
                        else {
                            workr[i] = datar[j];
                            worki[i] = 0.0;
                        }
                        ifp2 = np2;
                        iff  = ifmin;
                        do {
                            ifp1 = ifp2/ifact[iff];
                            j += ifp1;
                            if (j-i3-ifp2 < 0) 
                                break;
                            j -= ifp2;
                            ifp2 = ifp1;
                            iff++;
                        } while (ifp2 > np1);
                    }
                    i2max = i3+np2-np1;
                    i = 1l;
                    for (i2=i3;i2<=i2max;i2+=np1) {
                        datar[i2] = workr[i];
                        datai[i2] = worki[i];
                        i++;
                    }
                }
            }
        }
        /*
         *     special case--  w=1
         */
        i1rng = np1;
    
        if (icase == 2)
            i1rng = np0 * (1+nprev/2);
        if (ntwo > np1) {  
            for (i1=1;i1<=i1rng;i1++) {
                imin  = np1+i1;
                istep = 2*np1;
                while (istep <= ntwo) {
                    j=i1;
                    for (i=imin;i<=ntot;i+=istep) {
                        tempr    = datar[i];
                        tempi    = datai[i];
                        datar[i] = datar[j] - tempr;
                        datai[i] = datai[j] - tempi;
                        datar[j] += tempr;
                        datai[j] += tempi;
                        j        += istep;
                    }
                    imin  += imin-i1;
                    istep += istep;
                }
                /*
                 *     special case--  w=-sqrt(-1)
                 */
                imin  = 3*np1+i1;
                istep = 4*np1;
                while (istep <= ntwo) {
                    j = imin-istep/2;
                    for (i=imin;i<=ntot;i+=istep) {
                        if (ifrwd != 0) { 
                            tempr =  datai[i];
                            tempi = -datar[i];
                        }
                        else {
                            tempr = -datai[i];
                            tempi =  datar[i];
                        }  
                        datar[i] = datar[j]-tempr;
                        datai[i] = datai[j]-tempi;
                        datar[j] += tempr;
                        datai[j] += tempi;
                        j        += istep;
                    }
                    imin  += imin-i1;
                    istep += istep;
                } 
            }
            /*
             *     main loop for factors of two.  
             *     w=exp(-2*pi*sqrt(-1)*m/mmax)
             */
            theta = -twopi/8.0;
            wstpr =  0.0;
            wstpi = -1.0;
            if (ifrwd == 0) {
                theta = -theta;
                wstpi = 1.0;
            }
            mmax  = 8 * np1;
        
            while (mmax <= ntwo) {
                wminr = cos(theta);
                wmini = sin(theta);
                wr    = wminr;
                wi    = wmini;
                mmin  = mmax/2+np1;
                mstep = np1+np1;
        
                for (m=mmin;m<=mmax;m+=mstep) {
                    for (i1=1;i1<=i1rng;i1++) {
                        istep = mmax;
                        imin  = m+i1;
                        do {
                            j = imin-istep/2;
                            for (i=imin;i<=ntot;i+=istep) {
                                tempr    = datar[i]*wr-datai[i]*wi;
                                tempi    = datar[i]*wi+datai[i]*wr;
                                datar[i] = datar[j]-tempr;
                                datai[i] = datai[j]-tempi;
                                datar[j] += tempr;
                                datai[j] += tempi;
                                j        += istep;
                            }
                            imin  += imin-i1;
                            istep += istep;
                        } while (istep <= ntwo);
                    }
                    wtemp = wr*wstpi;
                    wr    = wr*wstpr-wi*wstpi;
                    wi    = wi*wstpr+wtemp;
                }
                wstpr = wminr;
                wstpi = wmini;
                theta /= 2.0;
                mmax  += mmax;
            }
        }
        /*
         *     main loop for factors not equal to two.
         *     w=exp(-2*pi*sqrt(-1)*(j2-i3)/ifp2)
         */
        if (non2p > 1) {
            ifp1 = ntwo;
            iff  = inon2;
            do {
                ifp2  =  ifact[iff]*ifp1;
                theta = -twopi/((float)ifact[iff]);
                if (ifrwd == 0)
                    theta = -theta;
                thetm = theta/((float)(ifp1/np1));
                wstpr = cos(theta);
                wstpi = sin(theta);
                wmstr = cos(thetm);
                wmsti = sin(thetm);
                wminr = 1.0;
                wmini = 0.0;
                for (j1=1;j1<=ifp1;j1+=np1) {
                    i1max = j1+i1rng-1;
                    for (i1=j1;i1<=i1max;i1++) {
                        for (i3=i1;i3<=ntot;i3+=np2) {
                            i  = 1;
                            wr = wminr;
                            wi = wmini;
                            j2max = i3+ifp2-ifp1;
                            for (j2=i3;j2<=j2max;j2+=ifp1) {
                                twowr = wr+wr;
                                jmin  = i3;
                                j3max = j2+np2-ifp2;
                                for (j3=j2;j3<=j3max;j3+=ifp2) {
                                    j     = jmin+ifp2-ifp1;
                                    sr    = datar[j];
                                    si    = datai[j];
                                    oldsr = 0.0;
                                    oldsi = 0.0;
                                    j    -= ifp1;
                                    do {
                                        stmpr = sr;
                                        stmpi = si;
                                        sr    = twowr*sr-oldsr+datar[j];
                                        si    = twowr*si-oldsi+datai[j];
                                        oldsr = stmpr;
                                        oldsi = stmpi;
                                        j    -= ifp1;
                                    } while (j > jmin);
                                    workr[i] = wr*sr-wi*si-oldsr+datar[j];
                                    worki[i] = wi*sr+wr*si-oldsi+datai[j];
                                    jmin    += ifp2;
                                    i++;
                                }
                                wtemp = wr*wstpi;
                                wr    = wr*wstpr-wi*wstpi;
                                wi    = wi*wstpr+wtemp;
                            }
                            i=1;
                            for (j2=i3;j2<=j2max;j2+=ifp1) {
                                j3max = j2+np2-ifp2;
                                for (j3=j2;j3<=j3max;j3+=ifp2) {
                                    datar[j3] = workr[i];
                                    datai[j3] = worki[i];
                                    i++;
                                }
                            }
                        }
                    }
                    wtemp = wminr*wmsti;
                    wminr = wminr*wmstr-wmini*wmsti;
                    wmini = wmini*wmstr+wtemp;
                }
                iff++;
                ifp1 = ifp2;
            } while (ifp1 < np2);
        }
        /*
         *     complete a real transform in the 1st dimension, n even, by con-
         *     jugate symmetries.
         */
        if (icase == 4) {
            nhalf = n;
            n = n+n;
            theta = -twopi/(float)n;
            if (ifrwd == 0) 
                theta = -theta;
            wstpr = cos(theta);
            wstpi = sin(theta);
            wr = wstpr;
            wi = wstpi;
            imin = 2;
            jmin = nhalf;
            while (imin-jmin < 0) {    
                j=jmin;
                for (i=imin;i<=ntot;i+=np2) {
                    sumr     = (datar[i]+datar[j])/2.0;
                    sumi     = (datai[i]+datai[j])/2.0;
                    difr     = (datar[i]-datar[j])/2.0;
                    difi     = (datai[i]-datai[j])/2.0;
                    tempr    = wr*sumi+wi*difr;
                    tempi    = wi*sumi-wr*difr;
                    datar[i] = sumr+tempr;
                    datai[i] = difi+tempi;
                    datar[j] = sumr-tempr;
                    datai[j] = -difi+tempi;
                    j       += np2;
                }
                imin++;
                jmin--;
                wtemp = wr*wstpi;
                wr    = wr*wstpr-wi*wstpi;
                wi    = wi*wstpr+wtemp;
            }
            if (imin-jmin == 0) {   
                if (ifrwd != 0) {
                    for (i=imin;i<=ntot;i+=np2)
                        datai[i] = -datai[i];
                }
            }
            np2  = np2+np2;
            ntot = ntot+ntot;
            j    = ntot+1;
            imax = ntot/2+1;
            do {
                imin = imax-nhalf;
                i    = imin;
                i++;
                j--;
                while (i-imax < 0) {
                    datar[j] =  datar[i];
                    datai[j] = -datai[i];
                    i++;
                    j--;
                }
                datar[j] = datar[imin]-datai[imin];
                datai[j] = 0.0;
                if (i-j >= 0) 
                    break;
                i--;
                j--;
                while (i-imin > 0) {
                    datar[j] = datar[i];
                    datai[j] = datai[i];
                    i--;
                    j--;
                }
                datar[j] = datar[imin]+datai[imin];
                datai[j] = 0.0;
                imax = imin;
            } while (1);
            datar[1] = datar[1]+datai[1];
            datai[1] = 0.0;
        }
        else if (icase == 2) {
            /*
             *     complete a real transform for the 
             *     2nd, 3rd, etc. dimension by
             *     conjugate symmetries.
             */   
            if (nprev-2 > 0) {
                for (i3=1;i3<=ntot;i3 += np2) {
                    i2max = i3+np2-np1;
                    for (i2=i3;i2<=i2max;i2+=np1) {
                        imax = i2+np1-1;
                        imin = i2+i1rng;
                        jmax = i3+i3+np1-imin;
                        if (i2-i3 > 0)
                            jmax += np2;
                        if (idim-2 > 0) {
                            j = jmax+np0;
                            for (i=imin;i<=imax;i++) {
                                datar[i] =  datar[j];
                                datai[i] = -datai[j];
                                j       -= 1;
                            }
                        }   
                        j = jmax;
                        for (i=imin;i<=imax;i+=np0) {
                            datar[i] =  datar[j];
                            datai[i] = -datai[j];
                            j       += np0;
                        }
                    }
                }
            }
        }
        /*
         *     end of loop on each dimension
         */
        np0   = np1;
        np1   = np2;
        nprev = n;
    }      
    return 0;
}

/*
 * Cyclic shift. If n3 > 0, all points from n1 to n2
 * are shifted n3 points to the left. Points that are
 * shifted left of n1 are added at the right. If n3 < 0
 * points are shifted to the right
 * tmpx must have size n3
 */
static void left_rotate(float *x, int n1, int n2, int n3, float *tmpx)
{
    int n4 = n2 - n1 + 1;

    if (n4 < 2)
        return;
    n3 %= n4;
    if (n3 == 0)
        return;
    if (n3 < 0)
        n3 = n4 + n3;
    /*
     * array starts at 1
     */
    x--;
    memcpy(tmpx, x + n1, sizeof(float) * n3);
    memmove(x + n1, x + n1 + n3, sizeof(float) * (n4 - n3));
    memcpy(x + n2 - n3 + 1, tmpx, sizeof(float) * n3);
}

/*
 *.....real forward fft
 */
int dftrxf(float *xrr, float *xri, int isize, int ntot, float *workr, float *worki)
{
    /*
     * .....zerofill if necessary, between isize and ntot
     */
    if (ntot > isize) 
        memset(xrr + isize, 0, sizeof(float) * (ntot - isize));
    /*
     * .....fill imaginary array with zeros
     */
    memset(xri, 0, sizeof(float) * ntot);
    /*
     * .....normal forward dft
     */
    fourt(xrr, xri, &ntot, 1, TRUE, FALSE, workr, worki);
    /*
     * .....real dft,    transform x(0....f) to x(0....0.5f)
     */
    return ntot / 2;
}


/*
 *.....imaginary forward dft
 */
int dftixf(float *xrr, float *xri, int isize, int ntot, float *workr, float *worki)
{
    int i;

    /*
     * .....zerofill if necessary, between isize and ntot
     */
    if (ntot > isize) 
        memset(xri + isize, 0, sizeof(float) * (ntot - isize));
    /*
     * .....fill imaginary array with zeros
     */
    memset(xrr, 0, sizeof(float) * ntot);
    /*
     * .....normal forward dft
     */
    fourt(xrr, xri, &ntot, 1, TRUE, FALSE, workr, worki);
    /*
     * .....imaginary dft, transform x(0....f) to x(0....0.5f)
     */
    ntot /= 2;
    for (i=0;i<ntot;i++) {
        xrr[i]      = xrr[i+ntot];
        xri[i]      = xri[i+ntot];
    }
    return ntot;
}



/*
 *.....complex forward dft
 */
int dftcxf(float *xrr, float *xri, int isize, int ntot, float *workr, float *worki)
{
    int i, idum;

    /*
     * .....zerofill if necessary, between isize and ntot
     */
    if (ntot > isize) {
        memset(xrr + isize, 0, sizeof(float) * (ntot - isize));
        memset(xri + isize, 0, sizeof(float) * (ntot - isize));
    }
    /*
     * .....normal forward dft
     */
    for (i=0;i<ntot;i++) 
        xri[i] *= -1;
    fourt(xrr, xri, &ntot, 1, TRUE, TRUE, workr, worki);
    for (i=0;i<ntot;i++) 
        xri[i] *= -1;
    /*
     * .....complex dft, transform x(0....f) to x(-0.5f....0.5f)
     */
/*
    idum = ntot/2;
    for (i=0;i<idum;i++) {
        float xdum;

        xdum        = xrr[i];
        xrr[i]      = xrr[i+idum];
        xrr[i+idum] = xdum;
        xdum        = xri[i];
        xri[i]      = xri[i+idum];
        xri[i+idum] = xdum;
    }
*/
    left_rotate(xrr,1,ntot,ntot/2,workr);
    left_rotate(xri,1,ntot,ntot/2,worki);
    return ntot;
}


/*
 * .....real backward dft
 */
int dftrxb(float *xrr, float *xri, int isize, int ntot, float *workr, float *worki)
{
    int i, ntotp1;
    float scale;
    
    /*
     * .....zerofill if necessary, between isize and ntot
     */
    if (ntot > isize) 
        memset(xrr + isize, 0, sizeof(float) * (ntot - isize));
    /*
     * .....fill imaginary array with zeros
     */
    memset(xri, 0, sizeof(float) * ntot * 2);
    /*
     * .....real    dft, transform x(0...0.5f) to x(0....f)
     */
    ntotp1 = 2 * ntot;
    xrr[ntot]=xrr[ntot-1];
    for (i=1;i<ntot;i++) {
         xrr[ntotp1-i] =  xrr[i];
         xri[ntotp1-i] = -xri[i];
    }
    /*
     * .....reverse dft
     */
    ntotp1 = ntot*2;
    fourt(xrr, xri, &ntotp1, 1, FALSE, FALSE, workr, worki);
    scale = 1.0/ntot;
    for (i=0;i<ntotp1;i++) {
        xrr[i] *= scale;
        xri[i] *= scale;
    }
    return ntot;
}

/*
 * ....imag  backward dft
 */
int dftixb(float *xrr, float *xri, int isize, int ntot, float *workr, float *worki)
{
    int i, ntotp1;
    float scale;

    /*
     * .....zerofill if necessary, between isize and ntot
     */
    if (ntot > isize) 
        memset(xri + isize, 0, sizeof(float) * (ntot - isize));
    /*
     * .....fill imaginary array with zeros
     */
    memset(xrr, 0, sizeof(float) * ntot * 2);
    /*
     * .....imag    dft, transform x(0...0.5f) to x(0....f)
     */
    ntotp1 = 2 * ntot - 1;
    for (i=0;i<ntot;i++) {
         xrr[ntotp1-i] =  xrr[i];
         xri[ntotp1-i] = -xri[i];
    }
    /*
     * .....reverse dft
     */
    ntotp1 = ntot*2;
    fourt(xrr, xri, &ntotp1, 1, FALSE, FALSE, workr, worki);
    for (i=0;i<ntot;i++) {
         xrr[i] = xrr[i+ntot];
         xri[i] = xri[i+ntot];
    }
    scale = 1.0/ntot;
    for (i=0;i<ntot;i++) {
        xrr[i] *= scale;
        xri[i] *= scale;
    }
    return ntot;
}



/*
 *.....complex backward dft
 */
int dftcxb(float *xrr, float *xri, int isize, int ntot, float *workr, float *worki)
{
    int i, idum;
    float scale;

    /*
     * .....zerofill if necessary, between isize and ntot
     */
    if (ntot > isize) {
        memset(xrr + isize, 0, sizeof(float) * (ntot - isize));
        memset(xri + isize, 0, sizeof(float) * (ntot - isize));
    }

    /*
     * .....complex dft, transform x(-0.5f....0.5f) to x(0....f)
     */
    left_rotate(xrr,1,ntot,-ntot/2,workr);
    left_rotate(xri,1,ntot,-ntot/2,worki);
    /*
     * .....reverse dft
     */
    for (i=0;i<ntot;i++) 
        xri[i] *= -1;
    fourt(xrr, xri, &ntot, 1, FALSE, TRUE, workr, worki);
    for (i=0;i<ntot;i++) 
        xri[i] *= -1;
    scale = 1.0/ntot;
    for (i=0;i<ntot;i++) {
        xrr[i] *= scale;
        xri[i] *= scale;
    }
    return ntot;
}


