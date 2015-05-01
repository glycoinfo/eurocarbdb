/*****************************************************************************/
/*                                DATAFILE.C                                 */
/*  (2001) Compression is removed                                            */
/*  file manager                                                             */
/*  Datarecords are writen as compressed data in Variable Length Blocks.     */
/*  However, if pad_size == DATA_SIZE, all blocks are the same size          */
/*  Compression is experimental                                              */
/*  (1990) Albert van Kuik                                                   */
/*****************************************************************************/
#include <stdarg.h>
#include <stdio.h>
#include <stdlib.h>
#ifdef __MSDOS__
#include <io.h>
#endif
#ifdef unix
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#endif
#include <time.h>
#include <string.h>
#include "btree.h"

#ifdef DEBUG
#include "mshell.h"
#endif


#define max(a,b)        (((a) > (b)) ? (a) : (b))
#define EXTRA_SPACE	1000	/* compressed buffer can be larger than original */

/*
#include "mshell.h"
*/
/* Synchronization code for integrity checking */
#define SYNC_BYTE  253
#define SYNC_FIRST 254		/* First block */
/* "NULL" link code, and data padding constant */
#define NOLINK 0
/* Each block has 6 bytes of header information, can hold 250 data bytes
   byte
   0-1   = number of bytes in the block 
   2     = SYNC_FIRST if first block, else is SYNC_BYTE
   3-6   = Pointer to next block. 0 if block is last block
   7-10  = If block is first block, data size
   11..  = data
 */

#define MAX_BLK_SIZE		1024
#define DEFAULT_BLK_SIZE	256
#define DEFAULT_PAD_SIZE	(DEFAULT_BLK_SIZE - 7)

#define BLK_SIZE(f)  	hcb[(f)].fh.block_size
#define BLK_HSIZE 	7
#define BLK_HSIZE_EXTRA	(BLK_HSIZE + sizeof(INT4))
#define DATA_SIZE(f) 	(BLK_SIZE(f) - BLK_HSIZE)

/* if padsize == DATA_SIZE then blocks are fixed lenght */
int padsize = 20;	/* default padding to 20 bytes */


static int inbytes(int fp, RPTR pos, BYTE * bf, int nr);
static int outbytes(int fp, RPTR pos, BYTE * bf, int nr);
static int getvlrelem(int fp, RPTR pos, RPTR * nxtlocn, BYTE * datablk);
static int getvlr(int fp, BYTE * bf, RPTR pos);
static int getvlrsize(int fp, RPTR pos, int compression);
static int extend_file(int fp, BYTE * bf, RPTR * locn, int offset, int bflen);
static int reuse_vlr(int fp, RPTR locn, BYTE * bf, int offset, int use_fs, int bflen);
static int delvlr(int fp, RPTR locn);
static int addvlr(int fp, BYTE * bf, RPTR * locn, int bfsize);

HEADER_CONTROL_BLOCK *hcb = NULL;
static  int file_count;

static int expand_buffer(BYTE *input, BYTE **output);
static int compress_buffer(BYTE *input, BYTE *output, long insize);

/*
 * Dummy
 */
static void compress(BYTE *output, long *output_size, BYTE *input, long insize)
{
    *output_size = insize;
    memcpy(output, input, insize * sizeof(BYTE));
}

/*
 * Dummy
 */
static void uncompress(BYTE *output, long *old_size, BYTE *input, long input_size)
{
    *old_size = input_size;
    memcpy(output, input, input_size * sizeof(BYTE));
}


static int compress_buffer(BYTE *input, BYTE *output, long insize)
{
/*    unsigned long output_size = insize + (8 + insize / 1000);*/
    unsigned long output_size = insize + EXTRA_SPACE;
    unsigned long output_offset;

    output_offset = 2*sizeof(long);
    memcpy(output, (BYTE*) &insize, sizeof(long));
    bswap4(output);
    compress(output + output_offset, &output_size, input, insize);
    memcpy(output+ sizeof(long), (BYTE*) &output_size , sizeof(long));
    bswap4(output+ sizeof(long));

#ifdef DEBUG
    printf("Input size, comp size: %d %d\n",insize,  output_size);
#endif

    return output_size + output_offset;
}


static int expand_buffer(BYTE *input, BYTE **output)
{
    unsigned long old_size, input_size, input_offset;
    
    memcpy((BYTE*) &old_size, input, sizeof(long));
    old_size = lswap4(old_size);
    input_offset = 2 * sizeof(long);    
    memcpy((BYTE*) &input_size, input + sizeof(long), sizeof(long));
    input_size = lswap4(input_size);
    *output = (BYTE*) malloc(old_size);
    if (*output == NULL) {
	printf("Fatal error buffer space!\n");
	exit(1);
    }

    uncompress(*output, &old_size, input + input_offset, input_size);

#ifdef DEBUG
    printf("Output size: %d\n", old_size);
#endif
    return old_size;
}



void dfile_error(int err)
{
    switch (err) {
    case D_OM:
        fprintf(stderr, "Memory error: out of memory\n");
        exit(2);
    case D_SYNC:
        fprintf(stderr, "Sync error: file corrupted\n");
        break;
    case D_FILE:
        fprintf(stderr, "File error: Incorrect file type\n");
        break;
    }
}

typedef union {
    unsigned char byte[4];
    long i;
} ENDI;
   
int is_big_endian(void)
{
    ENDI endi;
    
    endi.byte[0] = 0;
    endi.byte[1] = 0;
    endi.byte[2] = 0;
    endi.byte[3] = 1;
    if (endi.i == 1) 
         return 1;
    return 0;
}

INT2 dummy_lswap2(INT2 i)
{
    return i;
}

INT2 real_lswap2(INT2 i)
{
    char *ii=(char*)&(i);
    char *jj =ii+1; 
    *ii ^= *jj;
    *jj ^= *ii;
    *ii ^= *jj;
    return i;
}

long dummy_lswap4(long i)
{
    return i;
}

long real_lswap4(long i)
{
    char *ii=(char*)&(i);
    char *jj =ii+3; 
    *ii ^= *jj;
    *jj ^= *ii;
    *ii ^= *jj;
    ii++;
    jj--;
    *ii ^= *jj;
    *jj ^= *ii;
    *ii ^= *jj;
    return i;
}

void dummy_bswap4(BYTE *b)
{
    ;
}

void real_bswap4(BYTE *b)
{
    BYTE byte;
    
    byte = b[0];
    b[0] = b[3];
    b[3] = byte;
    byte = b[1];
    b[1] = b[2];
    b[2] = byte;
}

long  (*lswap4)(long l) = dummy_lswap4;
INT2  (*lswap2)(INT2 l) = dummy_lswap2;
void  (*bswap4)(BYTE *b) = dummy_bswap4;

static void init_swap(int endian_swap)
{
    if (endian_swap) {
        lswap2  = real_lswap2;
        lswap4  = real_lswap4;
        bswap4  = real_bswap4;
    }
    else {
        lswap2  = dummy_lswap2;
        lswap4  = dummy_lswap4;
        bswap4  = dummy_bswap4;
    }
}

static void swap_fheader(FHEADER *fh)
{
    fh->free_space    = lswap4(fh->free_space);
    fh->file_end      = lswap4(fh->file_end);
    fh->num_records   = lswap4(fh->num_records);
    fh->update        = lswap4(fh->update);
    fh->block_size    = lswap2(fh->block_size);
    fh->pad_size      = lswap2(fh->pad_size);
}

static void swap_bheader(BHEADER *bh)
{
    bh->rootnode  = lswap4(bh->rootnode);
    bh->leftmost  = lswap4(bh->leftmost);
    bh->rightmost = lswap4(bh->rightmost);
    bh->keylength = lswap4(bh->keylength);
    bh->m         = lswap4(bh->m);
}

void dfile_read_header(int fp)
{
    int i, big_endian, endian_swap;
    
    fseek(hcb[fp].handle, 0, SEEK_SET);
    fread((char *) &hcb[fp].fh, sizeof(FHEADER), 1, hcb[fp].handle);
    big_endian = is_big_endian();
    if (hcb[fp].fh.big_endian) 
        (big_endian) ? (endian_swap = FALSE) : (endian_swap = TRUE);
    else 
        (big_endian) ? (endian_swap = TRUE) : (endian_swap = FALSE);
    init_swap(endian_swap);
    swap_fheader(&(hcb[fp].fh));
    hcb[fp].endian_swap = endian_swap;
    if (!hcb[fp].fh.num_index)
        return;
    if ((hcb[fp].bh = (BHEADER*) malloc(hcb[fp].fh.num_index * sizeof(BHEADER))) == NULL)
        dfile_error(D_OM);
    fseek(hcb[fp].handle, sizeof(FHEADER), SEEK_SET);
    fread((BYTE *) hcb[fp].bh, sizeof(BHEADER), 
           hcb[fp].fh.num_index, hcb[fp].handle);
    for (i=0;i<hcb[fp].fh.num_index;i++)
        swap_bheader(&(hcb[fp].bh[i]));
}

void dfile_write_header(int fp)
{
    int i;
    
    if (!hcb[fp].open_for_writing)
        return;
    init_swap(hcb[fp].endian_swap);
    swap_fheader(&(hcb[fp].fh));
    fseek(hcb[fp].handle, 0, SEEK_SET);
    fwrite((char *) &hcb[fp].fh, sizeof(FHEADER), 1, hcb[fp].handle);
    swap_fheader(&(hcb[fp].fh));

    if (!hcb[fp].fh.num_index)
        return;
    for (i=0;i<hcb[fp].fh.num_index;i++)
        swap_bheader(&(hcb[fp].bh[i]));
    fseek(hcb[fp].handle, sizeof(FHEADER), SEEK_SET);
    fwrite((BYTE *) hcb[fp].bh, sizeof(BHEADER), 
           hcb[fp].fh.num_index, hcb[fp].handle);
    for (i=0;i<hcb[fp].fh.num_index;i++)
        swap_bheader(&(hcb[fp].bh[i]));
}

/* --------- create a file --------- */
void dfile_create(char *name, char *ident, char *version, int num_index, 
                  int endi, int compress)
{
    int i, big_endian, endian_swap, size_header;
    FHEADER hd;
    BHEADER bh;
    time_t timer;
    FILE *file;

    unlink(name);
    file = fopen(name, "w");
    memset(&hd, 0, sizeof(FHEADER));
    big_endian = is_big_endian();
    switch (endi) {
        case USE_NATIVE_ENDIAN:
            hd.big_endian = big_endian;
            endian_swap   = FALSE;
            break;
        case USE_BIG_ENDIAN:
            hd.big_endian = TRUE;
            endian_swap   = !big_endian;
            break;
        case USE_LITTLE_ENDIAN:
            hd.big_endian = FALSE;
            endian_swap   = big_endian;
            break;
    }
    hd.num_index = num_index;
    size_header = sizeof(FHEADER) + num_index * sizeof(BHEADER);
    strncpy(hd.magic, MAGIC, MAGIC_SIZE);
    memset(hd.version, 0, VERSION_SIZE);
    strncpy(hd.version, version, VERSION_SIZE);
    memset(hd.ident, 0, IDENT_SIZE);
    strncpy(hd.ident, ident, IDENT_SIZE);
    hd.free_space  = size_header;
    hd.file_end    = size_header;
    hd.num_records = 0;
    hd.block_size  = DEFAULT_BLK_SIZE;
    hd.pad_size    = DEFAULT_PAD_SIZE;
    hd.compression = compress;
    hd.pad         = 0;
    hd.update      = time(&timer);

    init_swap(endian_swap);
    swap_fheader(&hd);
    fwrite((char *) &hd, sizeof(FHEADER),1,file);
    memset(&bh, 0, sizeof(BHEADER));
    for (i=0;i<num_index;i++)
        fwrite((char *) &bh, sizeof(BHEADER),1,file);
    fclose(file);
}


/* --------- open a file --------- */
int dfile_open(char *name, int writemode)
{
    int fp;

    for (fp = 0; fp < file_count; fp++)
	if (hcb[fp].handle == NULL)
	    break;
    if (fp == file_count) {
        file_count++;
        hcb = (HEADER_CONTROL_BLOCK*) 
            realloc(hcb, file_count * sizeof(HEADER_CONTROL_BLOCK));
        if (hcb == NULL)
            dfile_error(D_OM);
        hcb[fp].handle = NULL;
        hcb[fp].bh = NULL;
    }
    if (writemode) {
	hcb[fp].open_for_writing = TRUE;
        if ((hcb[fp].handle = fopen(name, "r+")) == NULL)
	    return (ERROR);
    }
    else {
	hcb[fp].open_for_writing = FALSE;
        if ((hcb[fp].handle = fopen(name, "r")) == NULL)
	    return (ERROR);
    }
    dfile_read_header(fp);
    if (strncmp(hcb[fp].fh.magic, MAGIC, MAGIC_SIZE) != 0) {
        dfile_error(D_FILE);
        return ERROR;
    }
    return (fp);
}

/* --------- close a file --------- */
void dfile_close(int fp)
{
    int i;
    
    if (hcb[fp].open_for_writing) 
        dfile_write_header(fp);
    fclose(hcb[fp].handle);
    hcb[fp].handle = NULL;
    if (hcb[fp].bh)
        free(hcb[fp].bh);
    hcb[fp].bh = NULL;
    for (i=0;i<file_count;i++)
        if (hcb[i].handle)
            return;
    free(hcb);
    hcb = NULL;
    file_count = 0;
}

/*---------- number of records in a file -------*/
RPTR dfile_num_of_rcds(int fp)
{
    return (hcb[fp].fh.num_records);
}

/*---------- Give a pointer to the ident field in the file header -------*/
char *dfile_get_ident(int fp)
{
    static char ident[IDENT_SIZE+1];
    strncpy(ident, hcb[fp].fh.ident, IDENT_SIZE);
    return ident;
}

/*---------- Give a pointer to the ident field in the file header -------*/
char *dfile_get_version(int fp)
{
    static char version[VERSION_SIZE+1];
    strncpy(version, hcb[fp].fh.version, VERSION_SIZE);
    return version;
}

/*---- Give time + date of the last file-update  -------*/
long dfile_get_last_update(int fp)
{
    return (hcb[fp].fh.update);
}

/*---- Return the number of indices in the file -------*/
int dfile_get_num_index(int fp)
{
    return ((int) hcb[fp].fh.num_index);
}

/*---- Return TRUE if the file is written 'BIG ENDIAN", FALSE otherwise -------*/
int dfile_is_big_endian(int fp)
{
    return ((int) hcb[fp].fh.big_endian);
}

/* --------- create a new record --------- */
RPTR dfile_new_record(int fp, BYTE *bf, int len)
{
    RPTR rcdno;
    time_t timer;
    BYTE *cbf=NULL, *wbf;

    if (!hcb[fp].open_for_writing)
        return 0;
    init_swap(hcb[fp].endian_swap);
    if (hcb[fp].fh.compression) {
        cbf = (BYTE*) malloc(len + EXTRA_SPACE);
        if (cbf == NULL)
            dfile_error(D_OM);
        len = compress_buffer(bf, cbf, len);
        wbf = cbf;
    }
    else
        wbf = bf;
    if (addvlr(fp, wbf, &rcdno, len) == ERROR) {
        if (cbf)
            free(cbf);        
        return 0;
    }
    hcb[fp].fh.update = time(&timer);
    if (cbf)
        free(cbf);
    return (rcdno);
}

/* --------- retrieve a record ---------- */
int dfile_get_record(int fp, RPTR rcdno, BYTE *bf)
{
    int result;
    BYTE *cbf=NULL;
    
    if (rcdno >= hcb[fp].fh.file_end)
	return (ERROR);
    init_swap(hcb[fp].endian_swap);
    if ((result = getvlr(fp, bf, rcdno)) == ERROR)
        return ERROR;
    if (hcb[fp].fh.compression) {
        result = expand_buffer(bf, &cbf);
        memmove(bf, cbf, result);
        free(cbf);
    }
    return result;
}

/* --------- retrieve a record size ---------- */
int dfile_get_record_size(int fp, RPTR rcdno)
{
    if (rcdno >= hcb[fp].fh.file_end)
	return (ERROR);
    init_swap(hcb[fp].endian_swap);
    return getvlrsize(fp, rcdno,hcb[fp].fh.compression);
}

/* ---------- rewrite a record --------- */
int dfile_put_record(int fp, RPTR rcdno, BYTE *bf, int len)
{
    int ret;
    time_t timer;
    BYTE *cbf=NULL, *wbf;

    if (!hcb[fp].open_for_writing)
        return (ERROR);
    if (rcdno >= hcb[fp].fh.file_end)
	return (ERROR);
    init_swap(hcb[fp].endian_swap);
    if (hcb[fp].fh.compression) {
        cbf = (BYTE*) malloc(len + EXTRA_SPACE);
        if (cbf == NULL)
            dfile_error(D_OM);
        len = compress_buffer(bf, cbf, len);
        wbf = cbf;
    }
    else
        wbf = bf;
    ret = reuse_vlr(fp, rcdno, wbf, 0, 1, len);
    hcb[fp].fh.update = time(&timer);
    if (cbf)
        free(cbf);
    return (ret);
}

/* --------- delete a record --------- */
int dfile_delete_record(int fp, RPTR rcdno)
{
    time_t timer;

    if (!hcb[fp].open_for_writing)
        return (ERROR);
    if (rcdno >= hcb[fp].fh.file_end)
	return (ERROR);
    hcb[fp].fh.update = time(&timer);
    init_swap(hcb[fp].endian_swap);
    return (delvlr(fp, rcdno));
}

/* --------- scan to the next sequential record --------- */
RPTR dfile_next_record(int fp, RPTR recno, BYTE *bf)
{
    BYTE datablk[MAX_BLK_SIZE];

    init_swap(hcb[fp].endian_swap);
    do {
	if (recno == 0L)
            recno = sizeof(FHEADER) + hcb[fp].fh.num_index * sizeof(BHEADER);
	else {
	    if (inbytes(fp, recno, datablk, 2) == ERROR)
		return (ERROR);
	    if (!(datablk[1] == SYNC_FIRST || datablk[1] == SYNC_BYTE)) {
		dfile_error(D_SYNC);
                return ERROR;
	    }
	    recno += datablk[0] + 1;
	}
	if (inbytes(fp, recno, datablk, BLK_SIZE(fp)) == ERROR)
	    return (ERROR);
	if (!(datablk[1] == SYNC_FIRST || datablk[1] == SYNC_BYTE)) {
            dfile_error(D_SYNC);
            return ERROR;
	}
    } while (datablk[1] != SYNC_FIRST);
    if (dfile_get_record(fp, recno, bf) == ERROR)
	return (ERROR);
    return (recno);
}

/****************************************************************************/
/*                          LOCAL FUNCTIONS                                 */
/****************************************************************************/

/*---------------------------------------------------------------------------*/
/* inbytes() reads 'nr' bytes in buffer 'bf' from file 'fp' starting at      */
/* position 'pos'. Returns the number of bytes read.                         */
/*---------------------------------------------------------------------------*/
static int inbytes(int fp, RPTR pos, BYTE * bf, int nr)
{
    fseek(hcb[fp].handle, pos, SEEK_SET);
    return (fread(bf, nr, 1, hcb[fp].handle));
}

/*---------------------------------------------------------------------------*/
/* outbytes() writes 'nr' bytes from buffer 'bf' to file 'fp' at position    */
/* pos. Returns the number of bytes written                                  */
/*---------------------------------------------------------------------------*/
static int outbytes(int fp, RPTR pos, BYTE * bf, int nr)
{
    fseek(hcb[fp].handle, pos, SEEK_SET);
    return (fwrite(bf, nr, 1, hcb[fp].handle));
}


static void write_link2blk(BYTE *datablk, RPTR link)
{
    link = lswap4(link);
    memmove(datablk + 3, (BYTE *) &link, sizeof(INT4));
}

static RPTR read_blk2link(BYTE *datablk)
{
    RPTR link;
    memmove((BYTE *) &link, datablk + 3, sizeof(INT4));
    return lswap4(link);
}

static void write_len2blk(BYTE *datablk, INT4 len)
{
    len = lswap4(len);
    memmove(datablk + BLK_HSIZE, (BYTE*) &len, sizeof(INT4));
}

static INT4 read_blk2len(BYTE *datablk)
{
    INT4 len;
    memmove((BYTE*) &len, datablk + BLK_HSIZE, sizeof(INT4));
    return lswap4(len);
}

static void write_size2blk(BYTE *datablk, INT2 size)
{
    size = lswap2(size);
    memmove(datablk, (BYTE*) &size, sizeof(INT2));
}

static INT2 read_blk2size(BYTE *datablk)
{
    INT2 size;
    memmove((BYTE*) &size, datablk, sizeof(INT2));
    return lswap2(size);
}

static INT4 read_blk2len_compression(BYTE *datablk)
{
    INT4 len;
    memmove((BYTE*) &len, datablk + BLK_HSIZE + sizeof(long), sizeof(INT4));
    return lswap4(len);
}

/*---------------------------------------------------------------------------*/
/* getvlrelem()  reads up to BLK_SIZE bytes of a block of al vlr. Returns    */
/* number of bytes read, or ERROR if an error has occurred                   */
/*---------------------------------------------------------------------------*/
static int getvlrelem(int fp, RPTR pos, RPTR * nxtlocn, BYTE * datablk)
{
    int bytesread;

    inbytes(fp, pos, datablk, BLK_SIZE(fp));
    bytesread = read_blk2size(datablk);
    if (!(datablk[2] == SYNC_BYTE || datablk[2] == SYNC_FIRST)) {
	dfile_error(D_SYNC);
	return (ERROR);
    }
    *nxtlocn = read_blk2link(datablk);
    return (bytesread);
}

/*---------------------------------------------------------------------------*/
/* getvlr() reads in a variable_lenth record into *bf from file fp, at pos'n */
/* pos. Returns number of bytes in the record, else returns ERROR if an error */
/* occurred.                                                                 */
/*---------------------------------------------------------------------------*/
static int getvlrsize(int fp, RPTR pos, int compression)
{
    BYTE datablk[MAX_BLK_SIZE];
    INT4 actual_len;

    if (!pos) 
        return 0;
    if ((getvlrelem(fp, pos, &pos, datablk)) == ERROR) 
        return 0;
    if (compression) {
        int size;
        size = read_blk2len_compression(datablk);
        return max(size, read_blk2len(datablk));
    }
    else
        return read_blk2len(datablk);
}

/*---------------------------------------------------------------------------*/
/* getvlr() reads in a variable_lenth record into *bf from file fp, at pos'n */
/* pos. Returns number of bytes in the record, else returns ERROR if an error */
/* occurred.                                                                 */
/*---------------------------------------------------------------------------*/
static int getvlr(int fp, BYTE * bf, RPTR pos)
{
    BYTE datablk[MAX_BLK_SIZE];
    int  offset, data_left, data_size, data_indx;
    INT4 actual_len;
    int  nblks;

    offset = 0;
    nblks  = 0;
    while (pos != 0) {
	if ((getvlrelem(fp, pos, &pos, datablk)) == ERROR) {
	    return (ERROR);
	}
	else {			/*add data to the buffer */
	    if (++nblks == 1) {	/*if first block, then get actual data length */
                data_left = actual_len = read_blk2len(datablk);
		data_indx = BLK_HSIZE_EXTRA;
		data_size = read_blk2size(datablk) - BLK_HSIZE_EXTRA;	/* compute the amount of data in block */
	    }
	    else {
		data_indx = BLK_HSIZE;
		data_size = read_blk2size(datablk) - BLK_HSIZE;
	    }
            if (data_size > data_left)
                data_size = data_left;
            data_left -= data_size;
	    memmove(bf + offset, datablk + data_indx, data_size);
	    offset += data_size;
	}
    }
    return (actual_len);
}

/*---------------------------------------------------------------------------*/
/* extend_file() extends the file by adding the data in *bf to the file,     */
/* starting with the bytes at the specific offset. The locn of the start     */
/* of the data is returned in *locn.                                         */
/* Returns OK on success, or ERROR otherwise                                 */
/*---------------------------------------------------------------------------*/
static int extend_file(int fp, BYTE * bf, RPTR * locn, int offset, int bflen)
{
    RPTR fe, link;
    int  i, nb, nl;
    INT4 len;
    BYTE datablk[MAX_BLK_SIZE];

    len = bflen - offset;
    if (offset == 0)
	len += sizeof(INT4);
    fe = hcb[fp].fh.file_end;
    nb = len / DATA_SIZE(fp);
    nl = len - nb * DATA_SIZE(fp);
    link = fe;
    *locn = fe;
    /* Fill up full blocks first. If we exhaust the data then link the
     * last block to nowhere.
     */
    if (nb) {
	for (i = 1; i <= nb; i++) {
	    if (i == nb && nl == 0)
		link = 0;
	    else
		link += BLK_SIZE(fp);
            write_link2blk(datablk, link);
	    write_size2blk(datablk, BLK_SIZE(fp));
	    if (offset == 0) {
		/* stuff in data length byte on first block */
		datablk[2] = SYNC_FIRST;
		len -= sizeof(INT4);
                write_len2blk(datablk, len);
		memmove(datablk + BLK_HSIZE_EXTRA, bf, DATA_SIZE(fp) - sizeof(INT4));
		offset += DATA_SIZE(fp) - sizeof(INT4);
	    }
	    else {
		datablk[2] = SYNC_BYTE;
		memmove(datablk + BLK_HSIZE, bf + offset, DATA_SIZE(fp));
		offset += DATA_SIZE(fp);
	    }
	    if (outbytes(fp, fe, datablk, BLK_SIZE(fp)) == ERROR)
		return (ERROR);
	    fe += BLK_SIZE(fp);
	}
    }
    /* For the last non-full block, link it to nowhere, and add up to
     * padsize extra bytes to it, (to help minimize fragmentation when
     * reusing the block).
     */
    if (nl) {
        int nl_nopad = nl;
	link = NOLINK;

        memset(datablk + BLK_HSIZE, 0, DATA_SIZE(fp));
	nl += hcb[fp].fh.pad_size;
	if (nl > DATA_SIZE(fp)) 
	    nl = DATA_SIZE(fp);
	write_size2blk(datablk, (INT2) (nl + BLK_HSIZE));
        write_link2blk(datablk, link);
	if (offset == 0) {
	    /* stuff in data length byte on first block */
	    datablk[2] = SYNC_FIRST;
	    len -= sizeof(INT4);
            write_len2blk(datablk, len);
	    memmove(datablk + BLK_HSIZE_EXTRA, bf, nl_nopad - sizeof(INT4));
	}
	else {
	    datablk[2] = SYNC_BYTE;
	    memmove(datablk + BLK_HSIZE, bf + offset, nl_nopad);
	}
	if (outbytes(fp, fe, datablk, nl + BLK_HSIZE) == ERROR)
	    return (ERROR);
	fe += nl + BLK_HSIZE;
    }
    /* Set freespace = file_end, which means no freespace */
    hcb[fp].fh.free_space = fe;
    hcb[fp].fh.file_end   = fe;
    return (OK);
}


/*---------------------------------------------------------------------------*/
/* reuse_vlr() reuses the vlr starting at byte pos'n "locn" in file fp, by   */
/* storing the data in *bf at the specified offset. If we run out of room    */
/* the vlr list, then the list is extended by first using up freespace, and  */
/* if we run out of that, bytes are appended to the file.                    */
/* If we do have to use freespace,then this routine is called recursively    */
/* with "locn" set to the freespace itself, the new vstr offset, and the     */
/* "use_fs" flag set to 1.This flag tells us whether to try using freespace  */
/* or to append to the file when we need more room.                          */
/*                                                                           */
/* NOTE: Never call reuse_vlr directley with offset != 0. This condition is  */
/*       meant for recursive calls only. Always use offset = 0.              */
/*                                                                           */
/* If we have any free blocks left over from the old vlr, they are put on the */
/* freespace list. Note however, that the blocks themselves are never split  */
/* up once the are made.                                                     */
/* Returns OK if successful, ERROR otherwise                                 */
/*---------------------------------------------------------------------------*/
static int reuse_vlr(int fp, RPTR locn, BYTE * bf, int offset, 
                     int use_fs, int bflen)
{
    int  nb, nu, ls;
    INT4 len;
    BYTE datablk[MAX_BLK_SIZE];
    RPTR oldfs, nxtlocn, fs, fe, dmy;

    len = bflen;
    if (offset == 0)
	len += sizeof(INT4);
    fs = hcb[fp].fh.free_space;
    fe = hcb[fp].fh.file_end;
    do {
	if (getvlrelem(fp, locn, &nxtlocn, datablk) == ERROR)
	    return (ERROR);
	nb = read_blk2size(datablk);
	if (nb <= BLK_HSIZE)		
            /* We have a block with no room in it, so move on */
	    locn = nxtlocn;
	else {
	    nu = nb - BLK_HSIZE;
	    ls = len - offset;
	    if (nu >= ls) {
		/* What is left of our string fits in block , so null at nexlocn
		 * since at end, and then copy the data into the block. If
		 * we're just staring the vlr, then ad the lenth bytes first.
		 */
                write_link2blk(datablk, NOLINK);
		if (offset == 0) {
		    len -= sizeof(INT4);
                    write_len2blk(datablk, len);
		    memmove(datablk + BLK_HSIZE_EXTRA, bf, ls - sizeof(INT4));
		}
		else
		    memmove(datablk + BLK_HSIZE, bf + offset, ls);
		if (outbytes(fp, locn, datablk, read_blk2size(datablk)) == ERROR)
		    return (ERROR);
		/* Now, save the old free space pointer, and point freespace
		 * to the start of any remaining blocks in the chain. If
		 * we were reusing freespace and we're at it's end, set it
		 * equal to the file end.
		 */
		oldfs = fs;
		if (nxtlocn)
		    fs = nxtlocn;
		else if (use_fs)
		    fs = fe;
		/* Now, if not already using freespace, and there's links
		 * left in the chain, scan down the chain to the end and
		 * link the end to the old freespace. If fs== fe, then ther
		 * really was no freespace, so just leave the end link's
		 * nxtlcn null. Then, break out of loop.
		 */
		if (!use_fs && (nxtlocn != 0)) {
		    do {
			locn = nxtlocn;
			if (getvlrelem(fp, locn, &nxtlocn, datablk) == ERROR)
			    return (ERROR);
		    } while (nxtlocn);
		    if (oldfs != fe) {
                        write_link2blk(datablk, oldfs);
			if (outbytes(fp, locn, datablk, read_blk2size(datablk)) == ERROR)
			    return (ERROR);
		    }
		}
		hcb[fp].fh.free_space = fs;
		hcb[fp].fh.file_end = fe;
		break;
	    }
	    else {
		/* String doesn't fit into block. So add what you can. If at the
		 * start of the vlr, then add the length bytes to the front
		 */
		if (offset == 0) {
		    len -= sizeof(INT4);
                    write_len2blk(datablk, len);
		    memmove(datablk + BLK_HSIZE_EXTRA, bf, nu - sizeof(INT4));
		    offset += nu - sizeof(INT4);
		}
		else {
		    memmove(datablk + BLK_HSIZE, bf + offset, nu);
		    offset += nu;
		}
	    }
	    if (nxtlocn) {
		if (outbytes(fp, locn, datablk, read_blk2size(datablk)) == ERROR)
		    return (ERROR);
		locn = nxtlocn;
	    }
	}
    } while (nxtlocn);
    /* End of block walk loop */
    if (nu < ls) {
	/* We still have some data left, so we need to extend the
	 * vlr. Test to see whether to use freespace or append to
	 * file, and set out link to the file-end, or to freespace.
	 * Note that even if it says use freespace, there may not be
	 * any freespace, but this is checked later.
	 */
	if (use_fs)
            write_link2blk(datablk, fe);
	else
            write_link2blk(datablk, fs);
	if (outbytes(fp, locn, datablk, read_blk2size(datablk)) == ERROR)
	    return (ERROR);
	/* We may want to append to file, or may have to cause there
	 * really isn't any freespace. Otherwise, recursively reuse the
	 * freespace vlr.
	 */
	if (use_fs || (fs == fe)) {
	    if (extend_file(fp, bf, &dmy, offset, bflen) == ERROR)
		return (ERROR);
	}
	else {
	    if (reuse_vlr(fp, fs, bf, offset, 1, bflen) == ERROR)
		return (ERROR);
	}
    }
    return (OK);
}

/*---------------------------------------------------------------------------*/
/* delvlr() deletes the vlr which starts at byte pos'n "locn" in file fp.    */
/* The record is deleted by placing it on the freespase list.                */
/* Returns OK if successful, ERROR otherwise                                 */
/*---------------------------------------------------------------------------*/
static int delvlr(int fp, RPTR locn)
{
    BYTE datablk[MAX_BLK_SIZE];
    RPTR newfs, nxtlocn, fs, fe;
    int first = 1;

    fs = hcb[fp].fh.free_space;
    fe = hcb[fp].fh.file_end;
    newfs = locn;
    nxtlocn = locn;
    do {
	locn = nxtlocn;
	if (getvlrelem(fp, locn, &nxtlocn, datablk) == ERROR)
	    return (ERROR);
	if (first) {
	    datablk[2] = SYNC_BYTE;
	    if (outbytes(fp, locn, datablk, 3) == ERROR)
		return (ERROR);
	    first = 0;
	}
    } while (nxtlocn);
    if (fs != fe) {
        write_link2blk(datablk, fs);
	if (outbytes(fp, locn, datablk, read_blk2size(datablk)) == ERROR)
	    return (ERROR);
    }
    hcb[fp].fh.free_space = newfs;
    hcb[fp].fh.num_records--;
    return (OK);
}

/*---------------------------------------------------------------------------*/
/*  addvlr() takes the data stored in bf and adds it as a vlr to the file    */
/*  fp, and returns the location of the vlr in *locn. If there is freespace  */
/*  that is used before appending to the file. Returns OK if successful,     */
/*  ERROR otherwise.                                                         */
/*---------------------------------------------------------------------------*/
static int addvlr(int fp, BYTE * bf, RPTR * locn, int bfsize)
{
    RPTR oldfs, fs, fe;

    oldfs = hcb[fp].fh.free_space;
    fe    = hcb[fp].fh.file_end;
    if (oldfs == fe) {
	if (extend_file(fp, bf, &fs, 0, bfsize) == ERROR)
	    return (ERROR);
    }
    else {
	if (reuse_vlr(fp, oldfs, bf, 0, 1, bfsize) == ERROR)
	    return (ERROR);
    }
    hcb[fp].fh.num_records++;
    *locn = oldfs;
    return (OK);
}


