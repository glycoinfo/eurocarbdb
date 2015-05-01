/* iond.h 	(called by iond.c)
 * IO routines for Access of NMR (SMX) files
 * 
 * Author:  Bas R. Leeflang
 *	    Bio-Organic Chemistrt department,  
 *          Bijvoet Center, 
 *          Utrecht University
 *          leef@boc.chem.uu.nl
 * 
 */
 
#ifndef _IODEFS_H
#include "iodefs.h"
#endif

/*
 *  MAX_FILE_SIZE is expressed in bytes. 4*1024*1024*1024 (4Gb) means
 *  1Gb float data points. If complex datapoints are stored in one dimensions
 *  512Mb complex datapoints can be stored.
 */
/*#define MAX_FILE_SIZE    2*1024*1024*1024-1	*//* 4Gb bytes */
#define MAX_FILE_SIZE    UINT_MAX	/* see limits.h */
/*
 * MAX_BUFFER_SIZE is the maximum allowable buffer size. The sizes of the 
 * submatrices depend on this parameter.
 * It is NOT used as a hard limit for buffer allocation. The buffer is actually
 * allocated for the size determined in the file (which was calculated with
 * this MAX_BUFFER_SIZE. This value may change over time.)
 */
#define MAX_BUFFER_SIZE   256*1024		/* 256kb bytes */
#define MIN_SMX_SIZE      4
#define MAX_SMX_SIZE	 32
#define MAX_SMX_SIZE_1D   8*1024
#define MAX_SMX_RATIO	  4

#define OLD3D_MAX_DIM     3
#define MAX_DIM           5
#define ABSOLUTE_MAX_DIM 10
#define REAL              0
#define COMPLEX           1

#define STR_RECORD_NUM 0        /* first record of the file */
#define STR_RECORD_SIZE 80      /* Old 1/2/3D files 40, multinmr 80 */

#define PAR_RECORD_NUM 1        /* 2nd record of the file */
#define PAR_RECORD_SIZE 1024    /* old 3d/multinmr nr of floats!!! */
#define PAR_SIZE_POS	(MAX_DIM)
#define PAR_SIZE_MAX	(MAX_DIM+1)

#define BRK_RECORD_NUM 2        /* 3rd record of the file */
#define BRK_RECORD_SIZE 1024    /* ?????old 3d/multinmr nr of floats!!! */
#define BRK_SIZE_POS	(MAX_DIM+3)

#define HIS_RECORD_NUM 3        /* 4th record of the file */
#define HIS_RECORD_SIZE 1024    /* ?????old 3d/multinmr nr of floats!!! */
#define HIS_SIZE_POS	(MAX_DIM+2)

#define DATA_RECORD_NUM 4        /* 5th record of the file */
#define DATA_RECORD_SIZE 1024    /* ?????old 3d/multinmr nr of floats!!! */
#define BUF_SIZE_NUM	(MAX_DIM+4)
#define MAX_DIM_EXTRA	(MAX_DIM+5)

typedef struct OLD3D_STR_RECORD {
   float io_id; 	/*  0 */
   float version; 	/*  1 */
   float rec_size; 	/*  2 */
   float dum3; 		/*  3 */
   float str_size; 	/*  4 */
   float str_start; 	/*  5 */
   float str_type; 	/*  6 */
   float dum7; 		/*  7 */
   float dum8; 		/*  8 */
   float par_size; 	/*  9 */
   float par_start; 	/* 10 */
   float par_type; 	/* 11 */
   float dum12; 	/* 12 */
   float dum13; 	/* 13 */
   float brk_size; 	/* 14 */
   float brk_start; 	/* 15 */
   float brk_type; 	/* 16 */
   float dum17; 	/* 17 */
   float dum18; 	/* 18 */
   float his_size; 	/* 19 */
   float his_start; 	/* 20 */
   float his_type; 	/* 21 */
   float dum22; 	/* 22 */
   float dum23; 	/* 23 */
   float data_size; 	/* 24 */
   float data_start; 	/* 25 */
   float data_type; 	/* 26 */
   float data_nrec; 	/* 27 */
   float dum28; 	/* 28 */
   float smx_order; 	/* 29 */
   float old_size    [OLD3D_MAX_DIM];	/* 30 */
   float old_smx_size[OLD3D_MAX_DIM];	/* 33 */
   float old_complex [OLD3D_MAX_DIM];	/* 36 */
   float dum39;		/* 39 */

   float nd_size[MAX_DIM];	/* 40 */
   float dum4549[ABSOLUTE_MAX_DIM - MAX_DIM]; 	/* 45 - 49*/

   float nd_smx_size[MAX_DIM];	/* 50 */
   float dum5559[ABSOLUTE_MAX_DIM - MAX_DIM]; 	/* 55 - 59*/

   float nd_complex[MAX_DIM];	/* 50 */
   float dum6569[ABSOLUTE_MAX_DIM - MAX_DIM]; 	/* 65 - 69*/

   float dum70; 	/* 70 */
   float dum71; 	/* 71 */
   float dum72; 	/* 72 */
   float dum73; 	/* 73 */
   float dum74; 	/* 74 */
   float dum75; 	/* 75 */
   float dum76; 	/* 76 */
   float dum77; 	/* 77 */
   float dum78; 	/* 78 */
   float dum79; 	/* 79 */
} OLD3D_STR_RECORD;

typedef struct DIM_PAR {
   float sw; 		/*  0 */	/* Sweep Width (Hertz)              */
   float xref; 	        /*  1 */	/* reference                        */
   char wintyp[2]; 	/*  2a */	/* window_type                      */
   char dum_char[2]; 	/*  2b */	/* written as floats, so bytes left */
   float rsh; 		/*  3 */	/* right shift                      */
   float rlb; 		/*  4 */	/* line broadening                  */
   float gn; 		/*  5 */	/* gaussian broadening              */
   int   nzf; 		/*  6 */	/* zero-filling                     */
   int   iphase;	/*  7 */	/* 0=nophase,1=phase,-1=phasedone   */
   float ahold; 	/*  8 */	/* frequency independent phase corr.*/ 
   float bhold; 	/*  9 */	/* frequency dependent phase corr.  */ 
   int   i0hold; 	/* 10 */	/* pivot point (i0) phase corr      */
   int   ipost; 	/* 11 */	/* 0=no post     1=post on real     */
                                        /* 2=post on imag 3=post on complex */
   int   itm;	 	/* 12 */	/* # channels for trapezian window  */
   int   itd;	 	/* 13 */	/* read stop                        */
   int   icstrt;	/* 14 */	/* start after FFT                  */
   int   icstop;	/* 15 */	/* stop after FFT                   */
   int   dum16;        	/* 16 */
   int   ioff1;		/* 17 */	/* offset 1                         */
   int   ioff2;		/* 18 */	/* offset 2                         */
   int   ifft;		/* 19 */	/* 0=no fft      1=fft on real      */
                                        /* 2=fft on imag 3=fft on complex   */
   int   iwindo;	/* 20 */	/* 0=no window 1=window             */
                                        /* -1= window performed             */
   int   iwstrt;	/* 21 */	/* start window multiplication      */
   int   iwstop;	/* 22 */	/* stop window multiplication       */
   int   dum23;        	/* 23 */
   int   irever;	/* 24 */	/* 0=no reverse 1=reverse array 2=reverse*/
                                        /* records 3=reverse both -1=reverse*/
                                        /* array done -2=reverse records done*/
                                        /* -3= reverse both done            */
   int   dum2528[4];    /* 25 .. 28 */
   int   ibase;		/* 29 */	/* 0=nobaseline, >0 baseline type ibase */
                                        /* -1*ibase=baseline type ibase done*/
   int   dum30;     	/* 30 */
   int   iterms;	/* 31 */	/* # polynomal baseline terms       */
   int   iterms2;	/* 32 */	/* AK lower limit baseline terms    */
   int   ibstrt;	/* 33 */	/* start point baseline correction  */
   int   ibstop;	/* 34 */	/* stop point baseline correction   */
   int   dum3537[3];   	/* 35 .. 37 */
   float sfd;		/* 38 */	/* spectral frequency (each domain) */
   int   ipre;		/* 39 */	/* 0=no pre      1=pre on real      */
                                        /* 2=pre on imag 3=pre on complex   */
   int   irstrt;	/* 40 */	/* read start                       */
   float aref;		/* 41 */	/* AK size independent refa         */
   float bref;		/* 42 */	/* AK size independent refb         */
   int   dspshift;	/* 43 */	/* AK dsp shift			    */
   int   ifilut;	/* 44 */	/* 0=new file,1=same file,3=old file*/
   int   isstrt;	/* 45 */	/* store start                      */
   int   isstop;	/* 46 */	/* store stop                       */
   int   isspec;	/* 47 */	/* AK data is spectrum flag         */
   int   td;		/* 48 */	/* AK time domain, orig. data size  */
   int   dum49;		/* 49 */	/* AK */
   
} DIM_PAR;

typedef struct OLD3D_PAR_RECORD {
   float nblok; 	/*  0 */	/* Used in pre-history (IOMS)*/
   float nrec; 	        /*  1 */	/* Used in pre-history (IOMS)*/
   int   LT1T2; 	/*  2 */	/* Used in pre-history (IOMS)*/
   float spfreq; 	/*  3 */
   float carrier; 	/*  4 */
   int   nt0par; 	/*  5 */
   int   ntnpar; 	/*  6 */
   float dum7; 		/*  7 */
   int   iaqdir; 	/*  8 */
   int   next_dir; 	/*  9 */
   int   dummy[40];	/* 10...49 */
   DIM_PAR dim[MAX_DIM];		/* pars per dimension */
} OLD3D_PAR_RECORD;

#define NDMAXBUF 64
typedef struct NDBUFFER {
  int               *vector;
  float             *buffer;
  int               data_saved; /* If TRUE, data has been saved to disk            */
  int               buffer_filled;
  int               buffer_size;
  int               direction;
} NDBUFFER;

typedef struct NDFILE {
/*
 * file specific stuff
 */
  int        file_type; /* 0 for old 3d, 1 for nd */
  int        ndim;
  char     **filename;
  int        filno;
  int        readonly;
  int        record_length;
  int        data_type;
  int        data_size;
  int        nrec_per_smx;
  int        data_start;
  int       *size;	 /* ptr to array with global sizes of the hypercube */
  int       *smx_size;   /* ptr to array with submatrix sizes               */
  int       *smx_number; /* ptr to array number of submatrices per dim      */
  int       *smx_order;  /* ptr to array smx_order per dim                  */
  int       *smx_table;  /* ptr to array smx_order per dim                  */
  int       *smx_addition;  /* ptr to array smx_order per dim                  */
  int        storage;
  int       *complex;	 /* ptr to data type (real/complex....int / char?)  */
  char      *history;	/* ptr to history record	*/
  char      *bruker;	/* ptr to bruker record		*/
/*
 * 2D buffer specific stuff
 */
  int               *access;
  int               *order;
  OLD3D_STR_RECORD *structure;
  char              *parameter;
/*
 * 1D buffer specific stuff
 */
  int               buffer_size;
  int               last_buffer;
  int               ndmaxbuf;
  int               ndnumbuf;
  NDBUFFER          buf[NDMAXBUF];
} NDFILE;

/* prototypes */
#include "iond_protos.h"
