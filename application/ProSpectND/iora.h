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
 
#ifndef _IODEFS_H
#include "iodefs.h"
#endif


/*
 * Defines: measures
 */
#define MAGICAL_STRING_LENGTH  8
#define CREATOR_STRING_LENGTH 32
#define CONTENT_STRING_LENGTH 32
#define VERSION_STRING_LENGTH 32

#define MAGICAL_STRING "==IORA"
#define CREATOR_STRING "iora default creator"
#define CONTENT_STRING "iora default content"
#define VERSION_STRING "iora version 2"
#define MAGICAL_STRING_1 "NoMagic"
#define CREATOR_STRING_1 "ND software"
#define CONTENT_STRING_1 "NMR data"
#define VERSION_STRING_1 "iora version 2"

#define MAX_IORA_FILES        16  /* #files simulataniously open */

#define MAXSTRING    257        /* see ISYSNM in IOSYS.INCL */
#define MAX_WORDS    127			        /* 2^7 -1 */
#define RA_BUFFER_MIN    32*1024	/*  32kb */
#define RA_BUFFER_DEF    512*1024	/* 512kb */
#define RA_BUFFER_MAX    1024*1024	/*   1Mb */

/*
 * NSMAX 32k bytes. In worst case we need (509/508)*32k (=32.07k) bytes.
 * This has still to do with the f77 io stuff. Maybe we can/must change it?
 * Actually MAX_BYTES is only used for a conversion buffer for weird data
 * types. We should use a dynamic memory allocation here
 * For now MAX_BYTES is increased: (was 33000)
 */
#define MAX_BYTES  128*1024			        /* >(509/508)*32k */


#define DEFAULT_NEW_RECORD_TYPE	DATA_FLOAT_4  /* COMPRESS_4 */

#define UNKOWN_SIZE     -1
/*
 * Defines: endian-ness flags
 */
#define IORA_OS_ENDIAN		0
#define IORA_BIG_ENDIAN		1
#define IORA_LITTLE_ENDIAN	2
#define IORA_CHANGE_ENDIAN	3
#define IORA_VAX_ENDIAN		4
#define IORA_LITTLE_VAX_ENDIAN	5

/*
 * Defines: iora file_type flags (Mainly for backwards compatibility)
 */
#define IORA_TYPE_UNKNOWN	0
#define IORA_OLD3D		1
#define IORA_F77OLD3D		2

/*
 * Defines: use of ra_buffer per record types
 * Uncomment the following defines in order to  place the 
 * records in the ra_buffer (entire subcubes are stored here)
 * Compressed records with a tresholt are always buffered 
 */
/*#define FIXED_TO_BUFFER*/
/*#define NOCOMPRESS_TO_BUFFER*/
/*#define NOCOMPRESS_2_TO_BUFFER*/

/*
 * ra_buffer definitions
 */
#define NOT_IN_BUFFER -1
#define RED	       1
#define BLACK          0

/*
 * Type declarations
 */
 
typedef struct RBnode_t {
    int record_number;
    int offset;
    int colour;
    struct RBnode_t *l;
    struct RBnode_t *r;
} RBnode_t;

typedef struct buf_mark_t {
    int rec;
    int filno;
    int size;
    int on_disk;
} buf_mark_t;

typedef struct OLD_FILE_PTR {
    long          offset;   /* file pointer of this record */
} OLD_FILE_PTR;

typedef struct FILE_PTR {
    long          offset;   /* file pointer of this record */
    int           type;     /* content type of this record */
    long          size;     /* size(/bytes) of this record */
} FILE_PTR;

typedef struct NODE1_T {
    long offset;	    /* file pointer */
    struct NODE1_T *next;   /* next node    */
} NODE1_T;


#define NPARS        12    /* number of int/float parameter in the header */
#define IORA1_NPARS  11
#define IORA1_HEADER_SIZE IORA1_NPARS*sizeof(float)
#define HEADER_SIZE 256    /* total size (bytes) of the file header */
#define RESERVED_SIZE  (HEADER_SIZE - (NPARS*sizeof(int) + MAGICAL_STRING_LENGTH + \
        CREATOR_STRING_LENGTH + CONTENT_STRING_LENGTH + VERSION_STRING_LENGTH))
                  

/*
struct IORA_RESERVE_HEADER {
   char  reserved[RESERVED_SIZE];        
}
*/

typedef struct IORA_HEADER {
/*
 * This header block still needs to be restructed
 */
   char  magical[MAGICAL_STRING_LENGTH]; /*-1 make def in /usr/lib/magic  */
   long  std_long;                       /* 0 for endian-ness test        */
/*
 * From here identical data structure as IORA definition 1
 */
   int   npars;                          /* 1 number of parameters        */
   int   size;                           /* 2 header size in bytes        */
   int   maxrec;                         /* 3 maximum number of recs in RecordMap*/
   int   nrecs;                          /* 4 number of records           */
   int   nskip;	    			 /* 5 # bytes to skip (compatible)*/
   int   mapsize;                        /* 6 number of recs per RecordMap block*/
   int   nmaps;                          /* 7 number of RecordMaps        */
   int   type;                           /* 8 default record type         */
   float tresh_high;                     /* 9 upper treshold              */
   float tresh_level;                    /*10 treshold level              */
   float tresh_low;                      /*11 lower treshold              */
/*
 * until here identical data structure as IORA definition 1
 */
   int   rec_length;			 /* record_length of old iora file */
   char  creator[CREATOR_STRING_LENGTH]; /*12 Application used to create  */
   char  content[CONTENT_STRING_LENGTH]; /*13 General content description */
   char  version[VERSION_STRING_LENGTH]; /*14 version info                */
   char  reserved[RESERVED_SIZE];        /*15 rest of this header to fill up */
   /*
    * The reserved section completes the header to 256 bytes. This allows for 
    * additions to the header.
    */
} IORA_HEADER;

typedef struct IORA_FILE {
   char       *version;		/* IORA version string */
   int         filno;		/* */
   int         iunit;		/* simulated fortran unit (f77 interface) */
   int         type;		/* file type (triton/f77_triton */
   int         open_option;	/* open file options */
   int         close_option;	/* close file options */
   char       *filename;
   char        opened;
   char        changed;
   char        read_only;
   char        data_type;
   int         fd;		/* file descriptor */
   int         eof;		/* current eof (== filesize)*/
   FILE_PTR   *map;	        /* ptr to record mapping index */
   IORA_HEADER*header;	        /* ptr to copy of the file header */
   float      *old_header;	/* ptr to copy of the file header */
   void       *specific;	/* ptr to datatype specific header e.g. smx */
   void       (*swap4)(void *);	/* ptr to low level word swap fuction */
   void       (*swap2)(void *);	/* ptr to low level short swap fuction */
   } IORA_FILE;

typedef struct IORA_STATUS {
   int         max_files;	/* size of iora_ptr array */
   int         open_files;	/* number of open files */
   IORA_FILE  **iora_fp;	/* ptr to dynamic array of ptrs to IORA_FILE */
   } IORA_STATUS;


/*
 * ra	   Random Access   defines
 */
#define RANGE_OK(x, y, n) (( !((y)-(x)<0) & ((y)-(x)<(n)) ) ? 1 : 0)
#define TRESH_OK(x, th, tl) (( ((*x) >(*th)) | ((*x) <(*tl)) )  ? 1 : 0)
#define LTELL(x)    (lseek(iora_status.iora_fp[x]->fd, 0, SEEK_CUR))
#define LSEEK(x, y) (lseek(iora_status.iora_fp[x]->fd, y, SEEK_SET))
#define REWIND(x)   (lseek(iora_status.iora_fp[x]->fd, 0, SEEK_SET))
#define LEND(x)     (lseek(iora_status.iora_fp[x]->fd, 0, SEEK_END))
#define REC_OFFSET(x, y)  (iora_status.iora_fp[x]->map[y].offset)
#define REC_TYPE(x, y)    (iora_status.iora_fp[x]->map[y].type)
#define REC_SIZE(x, y)    (iora_status.iora_fp[x]->map[y].size)

typedef enum {
   IORA_INFO_FILNO,
   IORA_INFO_IUNIT,
   IORA_INFO_TYPE,
   IORA_INFO_OPEN_OPTION,
   IORA_INFO_CLOSE_OPTION,
   IORA_INFO_OPENED,
   IORA_INFO_CHANGED,
   IORA_INFO_READ_ONLY,
   IORA_INFO_DATA_TYPE,
   IORA_INFO_SWAP4,
   IORA_INFO_SWAP2,
   IORA_INFO_STD_LONG,
   IORA_INFO_NPARS,
   IORA_INFO_SIZE,
   IORA_INFO_MAXREC,
   IORA_INFO_NRECS,
   IORA_INFO_NSKIP,
   IORA_INFO_MAPSIZE,
   IORA_INFO_NMAPS,
   IORA_INFO_HTYPE
} IORA_INFO_DEFS;


/* prototypes */
#include "iora_protos.h"

