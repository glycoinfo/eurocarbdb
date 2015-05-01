/*****************************************************************************/
/*                                 btree.h                                   */
/*****************************************************************************/
#define ERROR -1
#define OK     0

#ifndef TRUE
#define TRUE 1
#define FALSE 0
#endif

#include <errno.h>


#define NODE 494   /* length of a B-tree node */
#define RPTR long  /* Record pointer-type  */
#define ADR  sizeof(RPTR)

#define BYTE	unsigned char
#define INT4	long
#define INT2	short

#define MXKEYLEN	128

/*---------- dbms error codes for errno return ----------*/
#define D_NF      1  /* record not found                 */
#define D_PRIOR   2  /* no prior record for this request */
#define D_EOF     3  /* end of file                      */
#define D_BOF     4  /* begin of file                    */
#define D_DUPL    5  /* primary key already exists       */
#define D_OM      6  /* out of memory                    */
#define D_INDXC   7  /* index corrupted                  */
#define D_IOERR   8  /* i/o error                        */
#define D_BFO     9  /* buffer overflow                  */
#define D_SYNC   10  /* error in sync byte               */
#define D_FILE   11  /* error in file type               */

/* --------- declaration of functions used in btree.c --------- */
void btree_init(int fp);
void btree_build(int fp, int index, int len);
RPTR btree_locate(int fp, int index, char *k);
RPTR btree_locate_near(int fp, int index, char *k);
int  btree_deletekey(int fp, int index, char *x, RPTR ad);
int  btree_insertkey(int fp, int index, char *x, RPTR ad, int unique);
RPTR btree_nextkey(int fp, int index);
RPTR btree_prevkey(int fp, int index);
RPTR btree_firstkey(int fp, int index);
RPTR btree_lastkey(int fp, int index);
void btree_keyval(int fp, int index, char *ky);
RPTR btree_currkey(int fp, int index);

/* ---------- function declarations from datafile .c -------- */
void dfile_read_header(int fp);
void dfile_write_header(int fp);
void dfile_create(char *name, char *ident, char *version, 
                  int num_index, int endi, int compress);
int  dfile_open(char *name,int openmode);
void dfile_close(int fp);
long dfile_num_of_rcds(int fp);
char *dfile_get_ident(int fp);
char *dfile_get_version(int fp);
long dfile_get_last_update(int fp);
int  dfile_get_num_index(int fp);
int  dfile_is_big_endian(int fp);
RPTR dfile_new_record(int fp, BYTE *bf, int len);
RPTR dfile_next_record(int fp,RPTR recno,BYTE *bf);
int  dfile_get_record(int fp,RPTR rcdno,BYTE *bf);
int  dfile_get_record_size(int fp,RPTR rcdno);
int  dfile_put_record(int fp,RPTR rcdno,BYTE *bf,int len);
int  dfile_delete_record(int fp,RPTR rcdno);
void dfile_error(int err);

#define USE_NATIVE_ENDIAN	0
#define USE_BIG_ENDIAN		1
#define USE_LITTLE_ENDIAN	2

int  is_big_endian(void);
extern long (*lswap4)(long l);
extern void (*bswap4)(BYTE *b);

/* --------- the btree node structure --------- */
typedef struct treenode {
    long nonleaf;		/* 0 if leaf, 1 if non-leaf          */
    RPTR prntnode;		/* parent node                       */
    RPTR lfsib;			/* left sibling node                 */
    RPTR rtsib;			/* right sibling node                */
    long keyct;			/* number of keys                    */
    RPTR key0;			/* node # of keys < 1st key this node */
    char keyspace[NODE - ((sizeof(long) * 2) + (ADR * 4))];
    char spil[MXKEYLEN];	/* for insertion excess */
} BTREE;

/* ----- the structure of the btree header node ----- */
typedef struct treehdr {
    RPTR rootnode;		/* root node number     */
    long keylength;		/* the length of a key  */
    long m;			/* max key/node         */
    RPTR leftmost;		/* left-most node       */
    RPTR rightmost;		/* right-most node      */
} BHEADER;

#define IDENT_SIZE	16
#define VERSION_SIZE	4
#define MAGIC_SIZE	4
#define MAGIC		"AK95"
/*---------- file header ----------*/
typedef struct fhdr {
    char magic[MAGIC_SIZE];	/*  Datafile identifier */
    char version[VERSION_SIZE];	/*  Program identifier */
    char ident[IDENT_SIZE];	/*  User identifier */
    RPTR free_space;		/*  Pointer to the first deleted record */
    RPTR file_end;		/*  Pointer to the end of the file      */
    RPTR num_records;		/*  Number of records in the database   */
    long update;		/*  Time and date of last update        */
    INT2 block_size;		/*  Length of a record in bytes         */
    INT2 pad_size;
    BYTE big_endian;
    BYTE num_index;
    BYTE compression;
    BYTE pad;
} FHEADER;

typedef struct {
    FILE    *handle;
    FHEADER fh;
    BHEADER *bh;
    int     open_for_writing;
    int     endian_swap;
} HEADER_CONTROL_BLOCK;

extern HEADER_CONTROL_BLOCK *hcb;

