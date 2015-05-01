
#ifndef SIMULA_H
#define SIMULA_H

#ifdef gna
/*
C             **********************************************
C             *                                            *
C             *  PROGRAM LAOCOON 5, COMPUTER VAX/VMS/VWS   *
C             *                                            *
C             **********************************************
C                                 BY
C        L. CASSIDEI AND O. SCIACOVELLI, DEPARTMENT OF CHEMISTRY
C                UNIVERSITY OF BARI, 70100 BARI, ITALY
C
C        CONVERTED FOR THE IBM PC/XT/AT BY
C                KENNETH J. TUPPER
C                DEPT. OF CHEMISTRY
C                INDIANA UNIVERSITY
C                BLOOMINGTON, IN, 47405
C   
C
C     LAOCN-5 IS A REVISED VERSION OF THE PROGRAMS LAOCN-3 AND LAOCN-4A
C     FOR THE ANALYSIS OF ISOTROPIC NMR SPECTRA OF SPIN-1/2 SYSTEMS.
C     THE PROGRAM IS DIMENSIONED TO ANALYZE SPIN SYSTEMS UP TO 8 MAG-
C     NETICALLY NON-EQUIVALENT NUCLEI. SPIN SYSTEMS EXIHIBITING MAG-
C     NETIC EQUIVALENCE CAN BE PROCESSED UP TO 7 NUCLEI GROUPS AND/OR
C     384 ENERGY LEVELS (I.E. A3BCDEFG).
C
*/
      INTEGER  STDIN,STDOUT,OUT1,OUT2,OUT3,OUT4
      PARAMETER (STDIN = 5, IN1 = 1, IN2 = 2)
      PARAMETER (STDOUT = 6, OUT1 = 7, OUT2 = 8, OUT3 = 9, OUT4 = 10)

      LOGICAL	calc_ok, trial_opn, exp_opn, cursor, newspc
      LOGICAL   ppm, highest

#define MAXSPIN		8
#define MAXGROUP 	(MAXSPIN-1)
#define MAXMATRIX	18
#define MAXENERGYLEVEL	384 /* (2**8 + 2**7)	*/

#define MAXEIGEN	70

    float w[MAXSPIN];		/* W(J)%    CHEMICAL SHIFTS		*/
    float cc[MAXSPIN][MAXSPIN];	/* CC(J,K)% INDIRECT COUPLING CONSTANTS	*/
    float amin;			/* int tresholt				*/
    float width;		/* line width in plot			*/
    float des;			/* lower spectr limit for simulation	*/
    float dei;			/* upper spectr limit for simulation	*/
    float f1;			/* lower plot limit			*/
    float f2;			/* upper plot limit			*/
    float size;			/* size of trial spectrum		*/
    float sf;			/* */
      sf   = 500.137

    
      REAL	w(8), cc(8,8), des, dei, amin, e(384), f(617)
      REAL      ne(36), pe(36)
      REAL      er1, tr_freq(2982), tr_int(2982), sf
      REAL      width, gfact, offset, pickw, pickf, sweep


    int ng;		/* NG%      NUMBER OF GROUPS			*/
    int nng[MAXSPIN];	/* NNG(J)%  NUMBER OF SPIN IN THE J-TH GROUP	*/
    int iso[MAXSPIN];	/* ISO(J)%  FOR LIKE ISOTOPE, THE SAME INTEGER. FOR	*/
    			/*          DIFFERENT ISOTOPE, DIFFERENT INTEGER	*/
    int npl;		/* NPL=0    WITHOUT PLOT, NPL.GT.1 WITH PLOT	*/
    int ni;		/* NI%      MAXIMUM NUMBER OF ITERATIONS	*/

    int nl;		/* number of assigned lines for iter		*/
    int il[MAXLINES]	/* IL(NL)%  TRANS. NO. OF EXPERIMENTAL FREQUENCY */
    int f[MAXLINES];	/* F(NL)%   EXPERIMENTAL FREQUENCY	*/

    int lct;		 
    
    int ia[MAXGROUP][MAXPARAM];	/* IA,IB%   PARAMETERS TO ITERATE	*/
    int ib[MAXGROUP][MAXPARAM];
    int ja[MAXGROUP][MAXPARAM];	/* JA,JB%   PARAMETERS ON WHICH THE FREQUENCY DE- */
			/*          PENDENCE IS COMPUTED		*/
    int jb[MAXGROUP][MAXPARAM];
    int nos;		/* Number of Parameter sets for ia,ib	*/
    int ios;		/* Number of Parameter sets for ja,jb	*/
    
    int iay              no. of submatrices [1...MAXMATRIX]
                         [MAXMATRIX=1,2,4,12,36,144,576,2880]
    int nsp[MAXSPIN][MAXMATRIX];
    int imar[MAXMATRIX];  order (dimension) of submatrix j
    int nmat[MAXMATRIX];  NMAT(J) NO. OF THE SUB-SUB-MATRICES IN THE IMAR(J)
                          MATRIX.
    int nsub[MAXSUBMATRIX][MAXMATRIX];	
                          NSUB(J,K) ORDER OF EACH K-TH SUB-SUB
                          MATRIX BELONGING TO THE J-TH SUBMATRIX. 

    int lfz[MAXENERGYLEVEL]	list of energy levels
    int fz[MAXSPIN][MAXENERGYLEVEL]
    ip, /* iteration parameters */ 
    nexit

      INTEGER   ng,  nng(8), iso(8), npl, ni, lct, nl, il(617)
      INTEGER   ia(37,7), ib(37,7), ja(37,7), jb(37,7), lfz(384)
      INTEGER   nsub(18,27), imar(18), nsp(18,8), nmat(18), ip, nexit
      INTEGER   kt1, iay, fz(384,8), iout, it
      INTEGER   tr_num(2982)
      INTEGER   size, lines(64), num, iunit

      CHARACTER *80  answer

      COMMON /LCN5/ ng, w, cc, nng, iso, npl, des, dei, ni, amin, lct,
     *              e, f, er1, nl, il, ia, ib, ja, jb, nos, ios,
     *              lfz, nsub, imar, nsp, nmat, ip, nexit, iay, fz,
     *              kt1, iout, it

      COMMON /PLTCMN/ f1, f2, size, igauss, width, gfact,     
     *              calc_ok, trial_opn, exp_opn, cursor, newspc,
     *              offset, sweep, pickf, pickw, lines, num, sf,
     *              iunit, ppm, highest

      COMMON /SIMUL/ tr_freq, tr_num, tr_int
c
#endif

#define SHOW_NONE	0
#define SHOW_ITERATED	1
#define SHOW_BEST	1
#define SHOW_ALL	2


#ifndef FALSE

#define FALSE	0
#define TRUE	1

#define sign(a,b)	((b)<0 ? -fabs(a) : fabs(a))
#define max(a,b)        (((a) > (b)) ? (a) : (b))
#define min(a,b)        (((a) < (b)) ? (a) : (b))

#endif

/* for now, max number of spins */
#define MAXSPINLIMIT 	10

#define MAXSPINSYSTEM	12
#define MAXSPIN		10	/*8*/
#define MAXGROUP 	(MAXSPIN-1)
#define MAXMATRIX	18	/* 36/2 */
#define MAXSUBMATRIX	100 /*27*/	/* 36/2 * 3 */
#define MAXENERGYLEVEL	1000 /* 384 */	/* 2**8 + 2**7		*/
#define MAXLINES	617	/* or 616 ?? */
#define MAXEIGEN	70      /* 1 8 28 56 70 56 28 8 1 */
#define MAXPARAM	(MAXSPIN*MAXSPIN)	/*36 */
#define MAXTRANSITIONS	2982
#define MAXSQN2		7
/*
#define NOITER		1077952576
#define ITER		1547714624
*/
#define NOITER		' '
#define ITER		'*'

typedef struct matrix_store_struct {
    int   size;
    int   pos;
    float *m;
} MSTYPE;


typedef struct sub_matrix_type {
    int imar;		/* order (dimension) of submatrix 		*/
    int nmat;		/* no. of the sub-sub-matrices in sub-matrix. 	*/
    int *nsub;		/* [1..MAXSUBMATRIX] order of each sub-sub-matrix */
    int *nsp;		/* [1..MAXSPIN]
                            number of spins in a spin group per matrix */
    int *sqn;		/* [1..MAXSPIN] spin quantum number */
} SUB_MATRIX_TYPE;


typedef struct transition {
    float deig;		/* [MAXTRANSITIONS]; calc frequencies		*/
    float sumsq;	/* [MAXTRANSITIONS]; intensities			*/
    float sper;		/* [MAXTRANSITIONS]; exp. frequencies		*/
    int   np;		/* [MAXTRANSITIONS]; Transition will be iterated flag	*/
    int   not;		/* [MAXTRANSITIONS]; transition numbers		*/
    int   lal;		/* [MAXTRANSITIONS]; origin from			*/
    int   lbl;		/* [MAXTRANSITIONS]; origin to			*/
} TRANSITION_TYPE;


typedef struct spintype {
    int   nng;		/* [MAXSPIN]; NUMBER OF SPIN IN THE J-TH GROUP	
                                      the number of spins in a spin group,
                                      e.g. for CH3 nng could be 3 */
    int   iso;		/* [MAXSPIN]; FOR LIKE ISOTOPE, THE SAME INTEGER. FOR	*/
    			/*          DIFFERENT ISOTOPE, DIFFERENT INTEGER	*/
    int   sqn;		/* Spin quantum number * 2			*/
    float sscale;	/* Scaling due to spin quantum number 		*/
    float w;		/* [MAXSPIN];		CHEMICAL SHIFTS		*/
    float *cc;		/* [MAXSPIN][MAXSPIN];	INDIRECT COUPLING CONSTANTS	*/
} SPIN_TYPE;


typedef struct spinsystemstruct {
    int   init;
    int   has_data;
    int   active;
    float scale;
    /* soft sizes */
    int   maxspin;
    int   maxgroup;
    int   maxtransitions;
    int   maxlines;
    int   maxparam;
   
    /* Spins */ 
    int ng;		/* NUMBER OF GROUPS			*/
    SPIN_TYPE *spin;

    /* Iteration parameters */
    int nos;		/* Number of Parameter sets for ia,ib	(MAXPARAM-1)*/
    int **ia;		/* [MAXGROUP][MAXPARAM];	
                           IA,IB%   PARAMETERS TO ITERATE	*/
    int **ib;		/* [MAXGROUP][MAXPARAM];	*/

    /* Assigned lines */    
    int   nl;		/* number of assigned lines for iter		*/
    int  *il;		/* [1..MAXLINES] TRANS. NO. OF EXPERIMENTAL FREQUENCY */
    float *f;		/* [1..MAXLINES] EXPERIMENTAL FREQUENCY		*/

    /* Transitions */
    int lct;		/* Number of computed transitions[MAXTRANSITIONS]*/
    TRANSITION_TYPE *trans;

} SPINSYSTEM;

typedef struct simstruct {
    int   init;
    int   has_data;
    /* soft sizes */
    int   maxspin;
    int   maxgroup;
    int   maxeigen;
    int   maxmatrix;
    int   maxsubmatrix;
    int   maxenergylevel;
    int   maxtransitions;
    int   maxlines;
    int   maxparam;

    float amin;		/* int tresholt				*/
    float width;	/* line width in plot			*/
    float size;		/* size of trial spectrum		*/
    float sw;		/* */
    float sf;		/* */
    float xref;		/* */
    float aref;		/* */
    float bref;		/* */
    int   td;
    float fleft, fright;/* left and right spectrum limits in hertz	*/
    int   ppm;		/* ppm or Hz flag			*/

    int igauss;		/* Line shape gaussian, lorentzian 	*/
    int show_iterations;

    /* Iteration */
    int ni;		/* NI%      MAXIMUM NUMBER OF ITERATIONS	*/
    /* Iteration genetic algorithm */
    int nfc;		/* NI%      MAXIMUM NUMBER OF function calls	*/
    int npop;		/* np%      MAXIMUM NUMBER OF POPULATIONS	*/
    int pop;		/* pop      population size			*/
    int child;		/* Number of children			*/
    int radius;		/* Neighborhood Radius to select from 	*/
    int it;		/* it = iteration number 		*/
    float dev;		/* Percentage  deviation for iteration	*/
    float er1, er2;

    /* Iteration parameters */
    float *corr;	/* [MAXPARAM];		*/
    float *corr2;	/* [MAXPARAM]; best corrections computed by 
                                       genetic algorithm */
    float *corr3;	/* [MAXPARAM]; current corrections computed 
                                       by genetic algorithm		*/
    float **d;		/* [MAXPARAM][MAXENERGYLEVEL]; [1..nos][]		
                           Dependence (weight) of frequency for 
                           nmr parameters */
    int   *lz;		/* [MAXPARAM];
                           scratch array of parameter indices		*/
    
    /* Assigned lines */    
    int   sticky;	/* It TRUE assigned lines stick to shift intead of to line number */
    float *b;		/* [1..MAXLINES] difference in exp and calc freq
                           b[] = f[] - (e[] -e[])	   		*/
    float **dc;		/* [1..MAXPARAM][1..MAXLINES]		
                           weight of iteration parameter for a transition */

    /* Matrices */
    int iay;             /* no. of submatrices  */
    SUB_MATRIX_TYPE *submat;

    float *e;		/* [1..MAXENERGYLEVEL] 
                           energy in frequency units       */
    float *e2;		/* [1..MAXENERGYLEVEL] 
                           energy in frequency units storage */
    int *lfz;		/* [1..MAXENERGYLEVEL]
                           list of energy level numbers 	*/
    int **fz;		/* [MAXSPIN][MAXENERGYLEVEL] 
                           for each energy level, spin states x 2
                           e.g. 1 -1, 2 0 -2, 3 1 -1 -3.  
                         */

    /* Scratch */
    int   *ist;		/* [8];		*/
    int   nexit;
    int   iout;
    float **va;		/* [70][70];		*/
    float **vb;		/* [70][70];		*/
    float **vc;		/* [70][70];		*/
    double **vec;	/* [70][70];	spin functions		        */
    double *root;	/* [70];	enrgies		                */
    double *a;		/* [70+(70*70-70)/2];	= 2485 = 70*71/2	*/

    /* Eigenvectors storage areas */
    MSTYPE *kt1, *kt2;

    /* undo info */
    SPIN_TYPE *undo_queue[UNDOQMAX+1];
    int q_undohead, q_undotail, q_redo;

    /* spins systems */
    int   sps_num;			/* number of spins systems	*/
    int   sps_pos;			/* current spin system		*/
    SPINSYSTEM sps[MAXSPINSYSTEM];
} SIMTYPE;

#endif











