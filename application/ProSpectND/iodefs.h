/* iora.h 	(called by iora.c)
 * IO routines for Random Access of files
 * 
 * Author:  Bas R. Leeflang
 *	    Bio-Organic Chemistrt department,  
 *          Bijvoet Center, 
 *          Utrecht University
 *          leef@boc.chem.uu.nl
 * 
 */


#define _IODEFS_H

/*
 * SYS_dependent header files
 */
#ifdef unix
#define SYSOK
#include <fcntl.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <errno.h>
/*#include <malloc.h>*/
#include <sys/types.h>
#include <sys/stat.h>
#include <unistd.h>
#include <math.h>
#endif

#ifdef _WIN32
#define SYSOK
#include <fcntl.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <errno.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <unistd.h>
/*#include <io.h>*/
#include <math.h>
#endif


#ifdef VMS
#define SYSOK
#include <unixio.h>
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <file.h>
#include <math.h>
#endif

#ifdef __TURBOC__
#define SYSOK
#include <fcntl.h>
#include <stdio.h>
#include <string.h>
#include <alloc.h>
#include <sys/types.h>
#include <io.h>
#include <math.h>
#endif
 

#ifndef SYSOK		        /* try these header files */
#include <fcntl.h>
#include <stdio.h>
#include <string.h>
#include <alloc.h>
#include <sys/types.h>
#include <unistd.h>
#include <stdlib.h>
#include <math.h>
#endif

#ifndef O_BINARY
#define O_BINARY 0
#endif

#ifdef MSHELL
#include "mshell.h"
#endif

#define MIN(a,b) (((a)<(b))?(a):(b))
#define MAX(a,b) (((a)>(b))?(a):(b))
#define ROUND(f)        ((int)(((f) < 0.0) ? (ceil((f)-0.5)) : (floor((f)+0.5))))
#define INSIDE(a,b,c)	(((b) > (a)) ? (((b) < (c)) ? (b) : (c)) : (a))
#define SIGN(a,b)       ((b)<0 ? -fabs(a) : fabs(a))

#define SWAP(a,b,c) {c=a; a=b; b=c;}
#define POW2(a) ((a)*(a))
#define POW3(a) ((a)*(a)*(a))
#define CHECK_FREE(x)     {if ((x)!=NULL) {free(x);(x)=NULL;}}

#define ul unsigned long

#define IORA_ERROR      255
#define MEM_ERROR       254
#define IOND_ERROR      253
#define OK                0
#define TRUE              1
#define FALSE             0
#define ENDOFRANGE       -1
/*
 * Defines: compression record types
 *    range [1..10] for (old) 3d files specifically
 *    range [11...] new iora_files
 */
#define DATA_UNKNOWN             -1
#define FIXED                     1
#define OLD3D_DATA_NOCOMPRESS    2
#define OLD3D_DATA_COMPRESS_4    3
#define OLD3D_DATA_COMPRESS_2    4
#define OLD3D_DATA_NOCOMPRESS_2  5
#define OLD3D_STRUCTURE          6
#define OLD3D_PARAMETER          7
#define OLD3D_BRUKER             8
#define OLD3D_HISTORY            9
#define OLD3D_DATA              10	/*useful?*/

#define ND_STRUCTURE             16
#define ND_PARAMETER             17
#define ND_BRUKER                18
#define ND_HISTORY               19

#define DATA_FLOAT_4             111
#define DATA_FLOAT_3             112
#define DATA_FLOAT_2             113
#define DATA_FLOAT_4_TRUNC       114
#define DATA_FLOAT_3_TRUNC       115
#define DATA_FLOAT_2_TRUNC       116
#define DATA_CHAR                117
#define DATA_SHORT               118
#define DATA_LONG                119

#define FILE_TYPE_OLD3D		0
#define FILE_TYPE_ND		1

/*
 * Defines: file handling flags
 */
#define OPEN_NEW    1
#define OPEN_RW	    2
#define OPEN_R	    3
#define OPEN_NEW_FORCE    4
#define CLOSE_KEEP  1
#define CLOSE_DEL   2

/*
 * The following definitions are used for the float to shorter float
 * conversions and visa versa and for endian-ness conversions.
 */
typedef struct FCOMPLEXP {float *r, *i;} fcomplexp;

typedef union byte4_union {
  float f;
  long  i;
  unsigned char byte[4];
} byte4_union;

typedef struct float2 {
  unsigned char exponent;
  unsigned char mantissa[1];
} float2;

typedef struct float3 {
  unsigned char exponent;
  unsigned char mantissa[2];
} float3;

typedef struct float4 {
  unsigned char exponent;
  unsigned char mantissa[3];
} float4;


