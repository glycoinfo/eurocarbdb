

#include "complex.h"
#include "mathtool.h"


void slasrt(char *id, int n__, float *d, int *info)
{
/*  -- LAPACK routine (version 2.0) --   
       Univ. of Tennessee, Univ. of California Berkeley, NAG Ltd.,   
       Courant Institute, Argonne National Lab, and Rice University   
       September 30, 1994   


    Purpose   
    =======   

    Sort the numbers in D in increasing order (if ID = 'I') or   
    in decreasing order (if ID = 'D' ).   

    Use Quick Sort, reverting to Insertion sort on arrays of   
    size <= 20. Dimension of STACK limits N to about 2**32.   

    Arguments   
    =========   

    ID      (input) CHARACTER*1   
            = 'I': sort D in increasing order;   
            = 'D': sort D in decreasing order.   

    N       (input) INTEGER   
            The length of the array D.   

    D       (input/output) REAL array, dimension (N)   
            On entry, the array to be sorted.   
            On exit, D has been sorted into increasing order   
            (D(1) <= ... <= D(N) ) or into decreasing order   
            (D(1) >= ... >= D(N) ), depending on ID.   

    INFO    (output) INTEGER   
            = 0:  successful exit   
            < 0:  if INFO = -i, the i-th argument had an illegal value   

    ===================================================================== 
  


       Test the input paramters.   

    
   Parameter adjustments   
       Function Body */
    /* System generated locals */
    int i__1, i__2;
    /* Local variables */
    static int endd, i, j;
    static int stack[64]	/* was [2][32] */;
    static float dmnmx, d1, d2, d3;
    static int start;
    static int stkpnt, dir;
    static float tmp;


#define STACK(I) stack[(I)]
#define WAS(I) was[(I)]
#define D(I) d[(I)-1]


    *info = 0;
    dir = -1;
    if (lsame(id, "D")) {
	dir = 0;
    } else if (lsame(id, "I")) {
	dir = 1;
    }
    if (dir == -1) {
	*info = -1;
    } else if (n__ < 0) {
	*info = -2;
    }
    if (*info != 0) {
	i__1 = -(*info);
	return ;
    }

    /* 
     *    Quick return if possible 
     */

    if (n__ <= 1) {
	return ;
    }

    stkpnt = 1;
    STACK(0) = 1;
    STACK(1) = n__;
L10:
    start = STACK((stkpnt << 1) - 2);
    endd = STACK((stkpnt << 1) - 1);
    --stkpnt;
    if (endd - start <= 20 && endd - start > 0) {

        /*
         *        Do Insertion sort on D( START:ENDD ) 
         */

	if (dir == 0) {

            /*   
             *        Sort into decreasing order 
             */

	    i__1 = endd;
	    for (i = start + 1; i <= endd; ++i) {
		i__2 = start + 1;
		for (j = i; j >= start+1; --j) {
		    if (D(j) > D(j - 1)) {
			dmnmx = D(j);
			D(j) = D(j - 1);
			D(j - 1) = dmnmx;
		    } else {
			goto L30;
		    }
		}
L30:
		;
	    }

	} else {

            /*  
             *         Sort into increasing order 
             */

	    i__1 = endd;
	    for (i = start + 1; i <= endd; ++i) {
		i__2 = start + 1;
		for (j = i; j >= start+1; --j) {
		    if (D(j) < D(j - 1)) {
			dmnmx = D(j);
			D(j) = D(j - 1);
			D(j - 1) = dmnmx;
		    } else {
			goto L50;
		    }
		}
L50:
		;
	    }

	}

    } else if (endd - start > 20) {

        /*
         *  Partition D( START:ENDD ) and stack parts, largest one first
         *  Choose partition entry as median of 3 
         */

	d1 = D(start);
	d2 = D(endd);
	i = (start + endd) / 2;
	d3 = D(i);
	if (d1 < d2) {
	    if (d3 < d1) {
		dmnmx = d1;
	    } else if (d3 < d2) {
		dmnmx = d3;
	    } else {
		dmnmx = d2;
	    }
	} else {
	    if (d3 < d2) {
		dmnmx = d2;
	    } else if (d3 < d1) {
		dmnmx = d3;
	    } else {
		dmnmx = d1;
	    }
	}

	if (dir == 0) {

            /*  
             *         Sort into decreasing order 
             */

	    i = start - 1;
	    j = endd + 1;
L60:
L70:
	    --j;
	    if (D(j) < dmnmx) {
		goto L70;
	    }
L80:
	    ++i;
	    if (D(i) > dmnmx) {
		goto L80;
	    }
	    if (i < j) {
		tmp = D(i);
		D(i) = D(j);
		D(j) = tmp;
		goto L60;
	    }
	    if (j - start > endd - j - 1) {
		++stkpnt;
		STACK((stkpnt << 1) - 2) = start;
		STACK((stkpnt << 1) - 1) = j;
		++stkpnt;
		STACK((stkpnt << 1) - 2) = j + 1;
		STACK((stkpnt << 1) - 1) = endd;
	    } else {
		++stkpnt;
		STACK((stkpnt << 1) - 2) = j + 1;
		STACK((stkpnt << 1) - 1) = endd;
		++stkpnt;
		STACK((stkpnt << 1) - 2) = start;
		STACK((stkpnt << 1) - 1) = j;
	    }
	} else {

            /*  
             *         Sort into increasing order 
             */

	    i = start - 1;
	    j = endd + 1;
L90:
L100:
	    --j;
	    if (D(j) > dmnmx) {
		goto L100;
	    }
L110:
	    ++i;
	    if (D(i) < dmnmx) {
		goto L110;
	    }
	    if (i < j) {
		tmp = D(i);
		D(i) = D(j);
		D(j) = tmp;
		goto L90;
	    }
	    if (j - start > endd - j - 1) {
		++stkpnt;
		STACK((stkpnt << 1) - 2) = start;
		STACK((stkpnt << 1) - 1) = j;
		++stkpnt;
		STACK((stkpnt << 1) - 2) = j + 1;
		STACK((stkpnt << 1) - 1) = endd;
	    } else {
		++stkpnt;
		STACK((stkpnt << 1) - 2) = j + 1;
		STACK((stkpnt << 1) - 1) = endd;
		++stkpnt;
		STACK((stkpnt << 1) - 2) = start;
		STACK((stkpnt << 1) - 1) = j;
	    }
	}
    }
    if (stkpnt > 0) {
	goto L10;
    }

} 
