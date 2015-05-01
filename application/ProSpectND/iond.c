/* 
 * iond.c
 * IO routines for Access of nD NMR data files (sub-matrix method 'SMX')
 * 
 * Author:  Bas R. Leeflang
 *	    Bio-Organic Chemistry department,  
 *          Bijvoet Center, 
 *          Utrecht University
 *          leef@boc.chem.uu.nl
 * 
 * Goal:    complete C implementation for accessing NMR data
 *	    Data compression 
 *	    Random access to record with variable length
 * Routines to be used in applications
 *          iond_open
 *          iond_close
 *          iond_get_rectype
 *          iond_set_rectype
 *          iond_set_access
 *          iond_get_access
 *          iond_read_1d	gives a pointer to a 1d trace in memory
 *          iond_write_1d	writes data from a 1d trace to disk
 *
 * Routines to be used internally
 *
 * Version:	190897	(BL)	start of project (based on iondplane.c etc.)
 *              250897	(BL)	reading 2d plane of real data OK
 *              220298	(BL)	reading 2d plane of real data bug fixes
 *              280798	(BL)	reading 1d real + spec access
 *		....98	(BL/AK)	various modifications to working version
 *              171098	(BL)	iond_set/get_rectype functions added
 *
 */

#include "iodefs.h"
#include "iora.h"
#include "iond.h"
#include <math.h>
#include <limits.h>
#include <assert.h>
#include "iora_protos.h"

/*
 * Open a ND file for reading or writing
 *
 * filename:  the name of the file
 * option:    OPEN_NEW  create a new file
 *            OPEN_R    open an old file read-only
 *            OPENRW    open an old file for reading and writing
 * The next parameters are supplied for a new file, and returned when
 * an existing file is opened.
 * ndim:      number of dimensions in use. 
 * size:      Array of sizes for all dimensions + some extra when a new file 
 *            is created
 *            size[0]             size of the file in dimension 1 (in floats)
 *            size[1]             size of the file in dimension 2 (in floats)
 *            ....
 *            size[MAX_DIM-1]     size of the file in dimension MAX_DIM (in floats)
 *            size[PAR_SIZE_POS]  the size of the parameter record (in bytes)
 *            size[PAR_SIZE_MAX]  the maximum size of the parameter block (for ndfile->parameter)
 *            size[HIS_SIZE_POS]  the size of the history record (in bytes)
 *            size[BRK_SIZE_POS]  the size of the bruker record (in bytes)
 *            size[BUF_SIZE_NUM]  the number of ndbuffers
 *            (MAX_DIM_EXTRA is number of elements in "size")
 * complex:   array of real/complex flags. For each dimension complex[dim] 
 *            is 0 for real and 1 for complex
 *
 * returns the file handle if opened correctly
 * On error NULL is returned
 */
 
static int iond_new_buffers(NDFILE *ndfile);

  
NDFILE  *iond_open (char *filename, int option, int *ndim, int *size, 
                    int *complex) {
 
    int filno;
    int nrecs, rec_length, rec_type;
    int ierr;
    int i,ndnumbuf;
    float data_size;
    int smx_size[MAX_DIM];
    NDFILE *ndfile;

    ndnumbuf = size[BUF_SIZE_NUM];
    if (ndnumbuf > NDMAXBUF) ndnumbuf = NDMAXBUF;
    if (ndnumbuf < 1) ndnumbuf = 1;
    if ( (ndfile = (NDFILE*)alloc_ndfile(size[PAR_SIZE_MAX],ndnumbuf)) == NULL)
        return NULL;

    ndfile->readonly     = FALSE;
    ndfile->ndmaxbuf     = NDMAXBUF;
    ndfile->ndnumbuf     = ndnumbuf;
    ndfile->last_buffer  = ndfile->ndnumbuf-1;
    for (i=0;i<NDMAXBUF;i++) {
        ndfile->buf[i].data_saved  = TRUE;
        ndfile->buf[i].buffer      = NULL;
        ndfile->buf[i].direction   = -1;
        ndfile->buf[i].buffer_size = 0;
    }

    switch (option) {
    case OPEN_NEW:
        ndfile->file_type = FILE_TYPE_ND;
        for (i=0;i<MAX_DIM;i++) 
            if (complex[i])
                size[i] *= 2;
        if ( (*ndim < 1 ) ||
             (*ndim > MAX_DIM) ){
                printf ("iond_open: Requested dimensionality (%d) impossible(%s).\n",
                      *ndim, filename);
            return (NULL);
        }
        data_size = 0.0;
        for (i=0; i< *ndim; i++) {
            if (size[i] < 1) {
                printf ("iond_open: size[%d] smaller than 1 (%d).\n",i,size[i]);
                return (NULL);
            }
            if ( (complex[i] != REAL) && (complex[i] != COMPLEX)) {
                printf ("iond_open: complex[%d] invalid (%d).\n",i,complex[i]);
                return (NULL);
            }
            data_size += (float)size[i]*sizeof(float);
        }

        if (data_size > MAX_FILE_SIZE) {
            printf ("iond_open: Requested file size (%f) exceeds maximum (MAX_FILE_SIZE)\n",
                    data_size);
            return (NULL);
        }         
 
        iond_calc_smx_sizes(*ndim, size, complex, smx_size);
        /*
         * ndfile->record_length = the size of a cube in floats
         * nrecs = the number of cubes in the file
         */
        ndfile->record_length = sizeof(char);
        nrecs = 1;
        for (i=0; i< *ndim; i++) {
            nrecs *= ceil((float)size[i] / (float)smx_size[i]);
            ndfile->record_length *= smx_size[i];
        }
        nrecs += 4; /* structure, parameter, bruker and history */

        if ( (filno = iora_open(filename, option, &nrecs)) == IORA_ERROR)
            return (NULL);     

        /*
         * Create and write the structure record
         */
        ndfile->ndim            = *ndim;
        ndfile->storage         = *ndim;
        ndfile->filno           = filno;
        ndfile->data_type       = DATA_FLOAT_4;
        ndfile->data_size       = ndfile->record_length;
        ndfile->data_start      = DATA_RECORD_NUM;
        ndfile->nrec_per_smx    = 1;     

        for (i=0; i<ndfile->ndim; i++) {
            ndfile->size[i]      = size[i];
            ndfile->complex[i]   = complex[i];
            ndfile->smx_size[i]  = smx_size[i];
            ndfile->smx_number[i]= ndfile->size[i] / ndfile->smx_size[i];
            if ((ndfile->size[i] % ndfile->smx_size[i]) != 0)
                ndfile->smx_number[i]++;       
        }
      
        for (i=ndfile->ndim; i<MAX_DIM; i++) {
            ndfile->size[i]      = 1;
            ndfile->complex[i]   = 0;
            ndfile->smx_size[i]  = 1;
            ndfile->smx_number[i]= 1;
            ndfile->smx_order[i] = 1;
        }
        /*
         * For each dimension:
         *   smx_size   = number of cubes
         *   smx_number = number of records per cube
         *   smx_order  = order of dimensions in cube. Highest dimension in use
         *                will have the lowest number (=1). Is offset 
         *                to next data point in cube
         */
        switch (ndfile->storage) {
        case 1:
            ndfile->smx_order[0]   = 1;
            ndfile->smx_order[1]   = ndfile->smx_number[0];
            ndfile->smx_order[2]   = ndfile->smx_number[0]*ndfile->smx_number[1];
            break;
        case 2:
            ndfile->smx_order[0]   = ndfile->smx_number[1];
            ndfile->smx_order[1]   = 1;
            ndfile->smx_order[2]   = ndfile->smx_number[0]*ndfile->smx_number[1];
            break;
        case 3:
        case 4:
        case 5:
        default:
            ndfile->smx_order[MAX_DIM-1] = 1;
            for (i=MAX_DIM-2; i>=0; i--)
                ndfile->smx_order[i]= ndfile->smx_order[i+1] * ndfile->smx_number[i+1];
            break;
        } 
/* BLBL */
        for (i=0; i<MAX_DIM; i++)
            update_str_record(ndfile, size[PAR_SIZE_POS], 
                              size[HIS_SIZE_POS], size[BRK_SIZE_POS]);
        update_par_record(ndfile);
        update_his_record(ndfile, NULL);
        update_brk_record(ndfile, NULL);

        break;
      
    case OPEN_R:
        ndfile->readonly  = TRUE;
    case OPEN_RW:
        filno = iora_open(filename, option, &nrecs);
        if (filno == IORA_ERROR) {
            printf ("Could not open file: %s\n", filename);
            return (NULL);
        }
        iora_get_fileinfo(filno, IORA_INFO_READ_ONLY, &(ndfile->readonly));
         
        ndfile->filno = filno;
        /*
         * Read the structure record
         */
        rec_length=sizeof(OLD3D_STR_RECORD)/sizeof(float);
        ierr = iora_read(ndfile->filno, STR_RECORD_NUM, 
                        (char*)ndfile->structure, &rec_length, &rec_type);
        if (ierr == IORA_ERROR) {
            printf ("Could not read structure record from file: %s\n", filename);
            return NULL;
        }
        proc_str_record(ndfile, ndfile->structure, rec_type);
        *ndim = ndfile->ndim;
        for (i=0; i< MAX_DIM; i++) {
            size[i]    = ndfile->size[i];
            complex[i] = ndfile->complex[i];
        }

        /*
         * Read the parameter record
         */
/*      rec_length=sizeof(OLD3D_PAR_RECORD)/sizeof(float);
        printf ("sizeof(OLD3D_PAR_RECORD) = %d, rec_length = %d\n",sizeof(OLD3D_PAR_RECORD),rec_length);

*/
        rec_length=ndfile->structure->par_size/sizeof(float);
/*
        printf ("ndfile->structure->par_size = %g, rec_length = %d\n",
                 ndfile->structure->par_size,rec_length);
*/
        ierr = iora_read(filno, PAR_RECORD_NUM, (char*)ndfile->parameter, &rec_length, &rec_type);
        if (ierr == IORA_ERROR) {
            printf ("Could not read parameter record from file: %s\n", filename);
            return (NULL);
        }
        if (ndfile->structure->his_size > 0) {
            rec_length = (int) ndfile->structure->his_size;
            ndfile->history = (char*) realloc(ndfile->history, rec_length+1);
            assert(ndfile->history);
            memset(ndfile->history, 0, rec_length+1);
            rec_length = ndfile->structure->his_size/sizeof(float);
            ierr = iora_read(filno, HIS_RECORD_NUM, (char*)ndfile->history, &rec_length, &rec_type);
            if (ierr == IORA_ERROR) {
                printf ("Could not read history record from file: %s\n", filename);
                return (NULL);
            }
        }
        if (ndfile->structure->brk_size > 0) {
            rec_length = (int) ndfile->structure->brk_size;
            ndfile->bruker = (char*) realloc(ndfile->bruker, rec_length+1);
            assert(ndfile->bruker);
            memset(ndfile->bruker, 0, rec_length+1);
            rec_length = ndfile->structure->brk_size/sizeof(float);
            ierr = iora_read(filno, BRK_RECORD_NUM, (char*)ndfile->bruker, &rec_length, &rec_type);
            if (ierr == IORA_ERROR) {
                printf ("Could not read bruker record from file: %s\n", filename);
                return (NULL);
            }
        }
        break;
      
    }
    for (i=0;i<MAX_DIM;i++) 
        if (complex[i])
            size[i] *= 0.5;
/*
    printf("Calc_smx_size: results:\n");
    for (i=0;i<MAX_DIM;i++) {
        printf(" complex(%d) = %d\n",i, complex[i]);
    }
    for (i=0;i<MAX_DIM;i++) {
        printf("    size(%d) = %d\n",i,    ndfile->size[i]);
    }
    for (i=0;i<MAX_DIM;i++) {
        printf("smx_size(%d) = %d\n",i,ndfile->smx_size[i]);
    }
    for (i=0;i<MAX_DIM;i++) {
        printf("smx_number(%d) = %d\n",i,ndfile->smx_number[i]);
    }
    for (i=0;i<MAX_DIM;i++) {
        printf("smx_order(%d) = %d\n",i,ndfile->smx_order[i]);
    }
*/
    return (ndfile);
}

int iond_flush (NDFILE *ndfile ) 
{
    int i;
    for (i=0;i<ndfile->ndnumbuf;i++)
       if (!ndfile->buf[i].data_saved && ndfile->buf[i].buffer_filled) 
          iond_write_1d(ndfile, ndfile->buf[i].vector, 0, NULL, NULL);
    return OK;
}

int iond_close (NDFILE *ndfile, int option ) 
{
    int i;
    for (i=0;i<ndfile->ndnumbuf;i++)
       if (!ndfile->buf[i].data_saved && ndfile->buf[i].buffer_filled) 
          iond_write_1d(ndfile, ndfile->buf[i].vector, 0, NULL, NULL);
    iora_close(ndfile->filno);
    free_ndfile(ndfile);
    return OK;
}

int iond_set_numbuffers(NDFILE *ndfile, int ndnumbuf ) 
{
    int i;

    if (ndnumbuf > NDMAXBUF) ndnumbuf = NDMAXBUF;
    if (ndnumbuf < 1) ndnumbuf = 1;

    if (ndnumbuf == ndfile->ndnumbuf)
        return FALSE;
    if (ndnumbuf < ndfile->ndnumbuf) {
        for (i=ndnumbuf;i<ndfile->ndnumbuf;i++) {
            CHECK_FREE(ndfile->buf[i].vector); 
            CHECK_FREE(ndfile->buf[i].buffer); 
        }
    }
    else {
        for (i=ndfile->ndnumbuf;i<ndnumbuf;i++)
            if ( (ndfile->buf[i].vector = calloc(sizeof(int), MAX_DIM)) == NULL) {
                return FALSE;
            }
    }
    ndfile->ndnumbuf     = ndnumbuf;
    ndfile->last_buffer  = ndfile->ndnumbuf-1;
    iond_new_buffers(ndfile);
    return TRUE;
}

int iond_get_numbuffers(NDFILE *ndfile ) 
{
    return ndfile->ndnumbuf ;
}

int iond_get_rectype (NDFILE *ndfile, float *tresh_high, float *tresh_level, float *tresh_low) 
{
    int type;
    type=iora_get_rectype (ndfile->filno, tresh_high, tresh_level, tresh_low);
    ndfile->data_type=type;
    return (type);
}

int iond_set_rectype (NDFILE *ndfile, int rectype, float tresh_high, float tresh_level, float tresh_low)
{
    int type;
    type=iora_set_rectype (ndfile->filno, rectype, tresh_high, tresh_level, tresh_low);
    ndfile->data_type=type;
    return (type);
}

int iond_set_access(NDFILE *ndfile, int *access) {
    int idim, jdim;
    int order, dir_change=FALSE;
   
    /*
     * Trivial checks...
     */
    for (idim=0; idim<MAX_DIM; idim++) {
        if (access[idim] < 0 || access[idim] >= MAX_DIM) return (IOND_ERROR);
    }
    for (idim=0; idim<MAX_DIM; idim++) {
        for (jdim=idim+1; jdim<MAX_DIM; jdim++) {
           if (access[idim] == access[jdim] ) return (IOND_ERROR);
       }
    }
   
    /*
     * OK set the file access directions 
     * and invalidate the buffer content if needed (change of read directions)
     */
    order=1;
    for (idim=0; idim<MAX_DIM; idim++) {
        if (ndfile->access[idim] != access[idim]) {
            /*
             * Note that only read-ndfiles can change access
             */
            iond_new_buffers(ndfile);
            dir_change = TRUE;
        }
        ndfile->access[idim] = access[idim];
        if (idim == 0) 
            ndfile->order[ndfile->access[idim]] = 1;
        else if (idim == 1)
            ndfile->order[ndfile->access[idim]] = ndfile->smx_size[ndfile->access[0]]
                                             * ndfile->smx_number[ndfile->access[0]];
        else
            ndfile->order[ndfile->access[idim]] = ndfile->order[ndfile->access[idim-1]]
                                             * ndfile->smx_size[ndfile->access[idim-1]];
    }

    if (dir_change)
        make_smx_tables(ndfile);
    return (OK);           
}

int iond_get_access(NDFILE *ndfile, int *access) {
   int i;
   
   for (i=0; i<MAX_DIM; i++) {
      access[i] = ndfile->access[i];
   }
   return (OK);           
}

static int iond_new_buffers(NDFILE *ndfile)
{
    int i;
    /*
     * This is now handled by setting a direction for each buffer
     * Note that write-buffers can NOT change their access
     *
    for (i=0;i<ndfile->ndnumbuf;i++)
        ndfile->buf[i].buffer_filled = FALSE;
     */  
    /* 
     * determine buffer size 
     * BEWARE, different buffer size for each direction !!!!!!!!
     */
    ndfile->buffer_size = 1;
    for (i=0; i<ndfile->ndim; i++) { 
        if (i==ndfile->access[0] ) 
            ndfile->buffer_size *= ndfile->smx_size[i]*ndfile->smx_number[i];
        else
            ndfile->buffer_size *= ndfile->smx_size[i];
    }
    if (ndfile->size[ndfile->access[0]] <= 0) {
        printf ("iond_new_buffer: wrong record size\n");
        return FALSE;
    }
    /* (re)allocate buffer */ 
    for (i=0;i<ndfile->ndnumbuf;i++) {
        if (ndfile->buf[i].buffer_size < ndfile->buffer_size) {
            ndfile->buf[i].buffer = (float*)realloc(ndfile->buf[i].buffer, 
                                            sizeof(float) * ndfile->buffer_size);
            assert(ndfile->buf[i].buffer);
            ndfile->buf[i].buffer_size = ndfile->buffer_size;
        }
    }                      

    return TRUE;
}

static int iond_new_buffer_offset(NDFILE *ndfile, int *stored_vector, int *vector_1d)
{
    int i, buffer_offset = 0;
    int vector_dif;
/* 
 * ndfile->vector_b alsways contains 'the lower left' corner of the SMX
 * In case were the requested vector_1d[i] element is lower than 
 * ndfile->vector_b[i] this means that the requested info is not in this buffer
 * In case they differ more than the size of the SMX in any direction (except 
 * in access[0]) than the request is not in the buffer either.
 * Note that ndfile->vector_b[access[0]] and vector_1d[access[0]] are always 
 * set to zero.
 */    
    for (i=0; i<ndfile->ndim; i++) { 
        vector_dif = vector_1d[i] - stored_vector[i];
        if (vector_dif < 0 || vector_dif >= ndfile->smx_size[i]) {
           buffer_offset = -1;
           break;
        }
        else
           buffer_offset += vector_dif * ndfile->order[i]; 
    }

    return buffer_offset;
}

fcomplexp* iond_read_1d(NDFILE *ndfile,  int *vector_1d) 
{
    int i;
    float *buffer = NULL;
    int buffer_offset;
    static fcomplexp record_c;
   
 
    vector_1d[ndfile->access[0]] = 0;

    /* Check for changing axes and presence of buffer */
    for (i=0;i<ndfile->ndnumbuf;i++) {
        if ( ndfile->buf[i].buffer == NULL) { /* No buffer  */
            if (!iond_new_buffers(ndfile))
                return NULL;
            break;
        }
    }
    /* 
     * calc absolute offset for requested vector and start vector 
     */
    for (i=0;i<ndfile->ndnumbuf;i++) {
        if (ndfile->buf[i].buffer_filled &&
            ndfile->buf[i].direction == ndfile->access[0]) {
            int buffer_offset_nd = -1;
            buffer_offset_nd = iond_new_buffer_offset(ndfile, ndfile->buf[i].vector, vector_1d);
            if (buffer_offset_nd >= 0 &&
                buffer_offset_nd < ndfile->buffer_size) {
                /*
                 * record is in buffer: return correct location 
                 */
                buffer_offset = buffer_offset_nd;
                buffer        = ndfile->buf[i].buffer;
                break;
            }
        }
    }
    if (buffer==NULL) {
        /*
         * This record is not in the buffer: READ it
         */
        int ibuf;
        /*
         * Skip to fresh buffer
         */
        ndfile->last_buffer++;
        if (ndfile->last_buffer >= ndfile->ndnumbuf)
            ndfile->last_buffer = 0;
        ibuf = ndfile->last_buffer;
        for (i=0; i<ndfile->ndim; i++) { /* was MAX_DIM */
            ndfile->buf[ibuf].vector[i] = 
                   ndfile->smx_size[i] * (vector_1d[i] / ndfile->smx_size[i]);
        }
        if (smx_to_buffer_1d(ndfile, ibuf)!=OK) {
            printf ("Error in smx_to_buffer_1d\n");
            return NULL;
        }
        ndfile->buf[ibuf].direction = ndfile->access[0];
  
        /* 
         * recalculate buffer_offset for the requested record 
         */
         buffer_offset = iond_new_buffer_offset(ndfile, ndfile->buf[ibuf].vector, vector_1d);
         buffer        = ndfile->buf[ibuf].buffer;
    }
    /*
     * OK. Now we have the data. Let's copy it in the application array 
     */
    record_c.r = (buffer + buffer_offset);
    if (ndfile->complex[ndfile->access[0]])
        record_c.i = (record_c.r + ndfile->size[ndfile->access[0]]/2);
    else
        record_c.i = NULL;
    return (&record_c);
}

int iond_write_1d(NDFILE *ndfile, int *vector_1d, int size, float *real, float *imag) 
{
    int i,isize;
    float *buffer = NULL;
    int buffer_offset;
    fcomplexp record_c;
   
    vector_1d[ndfile->access[0]] = 0;

    /* Check for changing axes and presence of buffer*/
    for (i=0;i<ndfile->ndnumbuf;i++) {
        if ( ndfile->buf[i].buffer == NULL) { /* No buffer  */
            if (!iond_new_buffers(ndfile))
                return IOND_ERROR;
            break;
        }
    }
    /* 
     * calc absolute offset for requested vector and start vector 
     */
    for (i=0;i<ndfile->ndnumbuf;i++) {
        if (ndfile->buf[i].buffer_filled &&
            ndfile->buf[i].direction == ndfile->access[0]) {
            int buffer_offset_nd = -1;
            buffer_offset_nd = iond_new_buffer_offset(ndfile, ndfile->buf[i].vector, vector_1d);
            if (buffer_offset_nd >= 0 &&
                buffer_offset_nd < ndfile->buffer_size) {
                /*
                 * record is in buffer: put into smx and write 
                 * size == 0 means FLUSH
                 */
                if (size == 0) {
                    ndfile->buf[i].data_saved = TRUE;
                    if (buffer_1d_to_smx(ndfile, i)!=OK) 
                        return IOND_ERROR;
                    else
                        return OK;
                }
                buffer        = ndfile->buf[i].buffer;
                buffer_offset = buffer_offset_nd;
                /*
                 * Mark record as dirty, because we are going to change it!
                 */
                ndfile->buf[i].data_saved = FALSE;
                break;
            }
        }
    }

    if (buffer==NULL) {
        int ibuf;
        /*
         * Skip to fresh buffer
         */
        ndfile->last_buffer++;
        if (ndfile->last_buffer >= ndfile->ndnumbuf)
            ndfile->last_buffer = 0;
        ibuf = ndfile->last_buffer;

        if (ndfile->buf[ibuf].buffer_filled) {  /* first store current content of b */
           if (buffer_1d_to_smx(ndfile, ibuf)!=OK)
               return (IOND_ERROR);
        }
        ndfile->buf[ibuf].data_saved = TRUE;

       /*
        * This record is not in the buffer: SO we can not write it, Read first
        */
        for (i=0; i<ndfile->ndim; i++) { /* was MAX_DIM */
            ndfile->buf[ibuf].vector[i] = 
                   ndfile->smx_size[i] * (vector_1d[i] / ndfile->smx_size[i]);
        }

        if (smx_to_buffer_1d(ndfile, ibuf)!=OK) { /* Read the SMX in buffer b */
            printf ("Error in smx_to_buffer_1d\n");
            return IOND_ERROR;
        }
  
        /* 
         * recalculate buffer_offset for the requested record 
         */
         buffer_offset = iond_new_buffer_offset(ndfile, ndfile->buf[ibuf].vector, vector_1d);
         buffer        = ndfile->buf[ibuf].buffer;
         ndfile->buf[ibuf].data_saved = FALSE;
         ndfile->buf[ibuf].direction  = ndfile->access[0];
    }
    if (ndfile->complex[ndfile->access[0]])
        size *= 2;
    isize = MIN(ndfile->size[ndfile->access[0]],size);

    record_c.r = (buffer + buffer_offset);
    if (ndfile->complex[ndfile->access[0]])
        record_c.i = (record_c.r + ndfile->size[ndfile->access[0]]/2);

    if (ndfile->complex[ndfile->access[0]]) {
        memcpy(record_c.r, 
               real,
               sizeof(float) * isize/2);
        memcpy(record_c.i,
               imag, 
               sizeof(float) * isize/2);
    }
    else {
        memcpy(record_c.r, 
               real,
               sizeof(float) * isize);
    }
    return OK;
}

int smx_to_buffer_1d(NDFILE *ndfile, int buffer_index) 
{
    static float* smx_buffer;
    static int size_save=0;
    float *buffer;
    int buffer_size;
    int smx_length=1;
    int ivect[MAX_DIM];
    int loc = 0;
    int i, ix,idim, itmp, index, sizes, smx_total_size;
    int record_num, record_offset;
    int block_length, loop_increment;

    buffer_size   = ndfile->buffer_size;
    record_offset = 0;
  
    /* The following calculation and realloc should not be done each time */
    for (i=0; i<ndfile->ndim; i++)
        smx_length *= ndfile->smx_size[i];

    if (smx_length > size_save) {
       size_save= smx_length;
       if ( (smx_buffer = (float*)realloc(smx_buffer, size_save*sizeof(float))) == NULL)
           return IOND_ERROR;
    }
      

    /* 
     * calulate the number of a subcube from the vector
     * This depends on the ordening 
     * read a subcube into the array 
     */
    buffer = ndfile->buf[buffer_index].buffer;
    ndfile->buf[buffer_index].buffer_filled = TRUE;
    for (i=0; i<ndfile->ndim; i++) {
	   record_offset += (ndfile->buf[buffer_index].vector[i]/ ndfile->smx_size[i]) 
                            * ndfile->smx_order[i] ;
    }
    if ( ndfile->smx_table[1] - ndfile->smx_table[0]  == 1) {
     /*
      * This means that consecutive float from the SMX are to be sequentially stored.
      * The 'read/write' direction in the SMX is identical to that requested.
      * So we can memcpy a few bytes at once in stead off one by one
      */
        loop_increment = ndfile->smx_size[ndfile->access[0]];
        block_length   = loop_increment*sizeof(float);
        for (ix=0; ix<ndfile->smx_number[ndfile->access[0]]; ix++) {
          record_num = record_offset + ix*ndfile->smx_order[ndfile->access[0]];

         /* 
          * now read in this sub matrix 
          */
          read_smx(ndfile, smx_buffer, smx_length, record_num);

         /* 
          * Now fill the smx from the buffer 
          */
          for (index=0; index<smx_length; index += loop_increment) {
             loc = ndfile->smx_table[index] + ndfile->smx_addition[ix];
             memcpy((void*)(&buffer[loc]), 
                    (void*)(&smx_buffer[index]),
                    block_length);
          } /* for index */ 
       } /* for ix */
    }
    else {
       for (ix=0; ix<ndfile->smx_number[ndfile->access[0]]; ix++) {
          record_num = record_offset + ix*ndfile->smx_order[ndfile->access[0]];

         /* 
          * now read in this sub matrix 
          */
          read_smx(ndfile, smx_buffer, smx_length, record_num);

         /* 
          * Now fill the smx from the buffer 
          */
          for (index=0; index<smx_length; index++) {
             loc = ndfile->smx_table[index] + ndfile->smx_addition[ix];
             buffer[loc] = smx_buffer[index];
          } /* for index */
       } /* for ix */
    }

    return (OK);
}

void make_smx_tables(NDFILE *ndfile){

    int ix, ixstep, ivect[MAX_DIM], sizes, itmp, smx_length, loc;
    int idim, i, index;
     
    smx_length = 1;
    for (i=0; i<ndfile->ndim; i++)
        smx_length *= ndfile->smx_size[i];

    ndfile->smx_table = (int*)realloc(ndfile->smx_table, 
                                      smx_length*sizeof(int));
    assert(ndfile->smx_table);
        
    ndfile->smx_addition = (int*)realloc(ndfile->smx_addition, 
                                         ndfile->smx_number[ndfile->access[0]]*sizeof(int));
    assert(ndfile->smx_addition);

    for (ix=0; ix<ndfile->smx_number[ndfile->access[0]]; ix++) {
        ndfile->smx_addition[ix]= ix * ndfile->smx_size[ndfile->access[0]];
                                     
    }
    for (index=0; index<smx_length; index++) {
         sizes          = smx_length;
         itmp           = index;
         for (idim=ndfile->ndim-1; idim>=0; idim--) {
             sizes      /= ndfile->smx_size[idim];
             ivect[idim] = itmp / sizes;
             itmp        = itmp % sizes;
         }

         loc  = 0;

         for (idim=0; idim<ndfile->ndim; idim++) 
             loc += ivect[ndfile->access[idim]] * ndfile->order[ndfile->access[idim]];            

 
         ndfile->smx_table[index]=loc;
    } /* for index */ 
}

int buffer_1d_to_smx(NDFILE *ndfile, int buffer_index) 
{
    static float* smx_buffer;
    static int size_save;
    float  *buffer;
    int buffer_size;
    int smx_length=1;
    int ivect[MAX_DIM];
    int loc = 0;
    int i, ix, idim, itmp, index, sizes;
    int record_num, record_offset;
    int block_length, loop_increment;

    buffer_size   = ndfile->buffer_size;
    record_offset = 0;

    
    /* The following calculation and realloc should not be done each time */
    for (i=0; i<ndfile->ndim; i++)
        smx_length *= ndfile->smx_size[i];
      
    if (smx_length > size_save) {
        size_save= smx_length;
        if ( (smx_buffer = (float*)realloc(smx_buffer, smx_length*sizeof(float))) == NULL)
            return IOND_ERROR;
    }

    /* 
     * calulate the number of a subcube from the vector
     * This depends on the ordening 
     * read a subcube into the array 
     */
    buffer = ndfile->buf[buffer_index].buffer;
    for (i=0; i<ndfile->ndim; i++) {
	   record_offset += (ndfile->buf[buffer_index].vector[i]/ ndfile->smx_size[i]) 
                            * ndfile->smx_order[i] ;
    }
    if ( ndfile->smx_table[1] - ndfile->smx_table[0]  == 1) {
     /*
      * This means that consecutive float from the SMX are to be sequentially stored.
      * The 'read/write' direction in the SMX is identical to that requested.
      * So we can memcpy a few bytes at once in stead off one by one
      */
       loop_increment = ndfile->smx_size[ndfile->access[0]];
       block_length   = loop_increment*sizeof(float);
       for (ix=0; ix<ndfile->smx_number[ndfile->access[0]]; ix++) {
          record_num = record_offset + ix*ndfile->smx_order[ndfile->access[0]];

         /* 
          * Now fill the smx from the buffer 
          */
          for (index=0; index<smx_length; index += loop_increment) {
             loc = ndfile->smx_table[index] + ndfile->smx_addition[ix];
             memcpy((void*)(&smx_buffer[index]),
                    (void*)(&buffer[loc]),
                    block_length);
          } /* for index */ 
         /* 
          * now write in this sub matrix 
          */
          write_smx(ndfile, smx_buffer, smx_length, record_num);
       } /* for ix */
    }
    else {
       for (ix=0; ix<ndfile->smx_number[ndfile->access[0]]; ix++) {
          record_num = record_offset + ix*ndfile->smx_order[ndfile->access[0]];

         /* 
          * Now fill the smx from the buffer 
          */
          for (index=0; index<smx_length; index++) {
             loc = ndfile->smx_table[index] + ndfile->smx_addition[ix];
             smx_buffer[index] = buffer[loc];
          } /* for index */

         /* 
          * now write in this sub matrix 
          */
          write_smx(ndfile, smx_buffer, smx_length, record_num);
       } /* for ix */
    }

    return (OK);
}

int read_smx(NDFILE *ndfile, float* buffer, int size, int record_num) {
    int type, ierr;
    int i, key, idiv, irest, read_length;
    float *ptr;

    read_length = ndfile->record_length;
    if (ndfile->nrec_per_smx == 1) {
       key  = ndfile->data_start + record_num;
       ierr = iora_read(ndfile->filno, 
                        key, 
                        (char*)buffer, 
                        &size, 
                        &type);
       if (ierr == IORA_ERROR) {
          memset(buffer, 0x00, size*sizeof(float));
          return IORA_ERROR;
       }
    }
    else {
       idiv = size / ndfile->record_length;
       irest= size % ndfile->record_length;
       key = ndfile->data_start + record_num*ndfile->nrec_per_smx;
       ptr = buffer;
       for (i=0; i<(size/ndfile->record_length); i++) {
          ierr = iora_read(ndfile->filno, 
                           key++, 
                           (char*)buffer, 
                           &read_length, 
                           &type); 
          if (ierr == IORA_ERROR) {
             memset(buffer, 0x00, read_length*sizeof(float));
             return IORA_ERROR;
          }
          buffer += read_length;
       }
       if (irest != 0) 
          ierr = iora_read(ndfile->filno, 
                           key, 
                           (char*)buffer, 
                           &irest, 
                           &type); 
        if (ierr == IORA_ERROR) {
           memset(buffer, 0x00, irest*sizeof(float));
           return IORA_ERROR;
       }
    }
    return (OK);
}

#ifdef STILL_UNDER_DEVELOPMENT

GEEN GOED IDEE !!

/*
 * Currently only for 3D files
 */
int read_plane_projection(NDFILE *ndfile, float* buffer, 
                          int buff_size_x, int buff_size_y,
                          int step_x, int step_y) 
{
    int type, ierr;
    int i, j, read_length;
    int dir1, dir2, dir3;
    int dx, dy, dz;
    float *cube;
    int cube_order[MAX_DIM];
    int buf_offset_x = 0;
    int buf_offset_y = 0;
    int cdx, cdy;

    if (ndfile->ndim != 3)
        return IORA_ERROR;
    read_length = ndfile->record_length;
    for (i=0;i<buff_size_x;i++) {
        for (j=0;j<buff_size_y;j++) 
            buffer[i + (buff_size_x * j)] = 0.0;
    }
    cube = (float*) malloc(sizeof(float)*ndfile->record_length);
    assert(cube);

        /*
         * For each dimension:
         *   smx_size   = number of cubes
         *   smx_number = number of records per cube
         *   smx_order  = order of dimensions in cube. Highest dimension in use
         *                will have the lowest number (=1). Is offset 
         *                to next data point in cube
         */
    dir1 = ndfile->access[0];
    dir2 = ndfile->access[1];
    dir3 = ndfile->access[2];
    cdx  = ndfile->smx_number[dir1];
    cdy  = ndfile->smx_number[dir2];
    dx   = ndfile->size[dir1]/(1+ndfile->complex[dir1]);
    dy   = ndfile->size[dir2]/(1+ndfile->complex[dir2]);
    dz   = ndfile->size[dir3]/(1+ndfile->complex[dir3]);

    cube_order[ndfile->ndim-1] = 1;
    for (i=ndfile->ndim-2; i>=0; i--)
        cube_order[i]= cube_order[i+1] * ndfile->smx_size[i+1];
    for (buf_offset_y = 0; buf_offset_y < dy; buf_offset_y += cdy) {
        for (buf_offset_x = 0; buf_offset_x < dx; buf_offset_x += cdx) {
            int ikey, key, cube_off;
            int key_offset = ndfile->data_start + 
                (buf_offset_x/cdx * cube_order[dir1]) + 
                (buf_offset_y/cdy * cube_order[dir2]);
printf("key_off=%d %d %d %d\n",key_offset,cube_order[dir1],cube_order[dir2],cube_order[dir3]);
            /* 
             * Read all the cubes that are on top of each other
             */
            for (ikey=0,key=0;ikey<1/*dz*/;key += cube_order[dir3],ikey++) {
                int ix, iy, off_x, off_y;
                
                ierr = iora_read(ndfile->filno, 
                           key+key_offset, 
                           (char*)cube, 
                           &read_length, 
                           &type); 
                if (ierr == IORA_ERROR) {
                    printf("IOERR: %d %d %d\n",key,key_offset,ikey);
                    break;
                /*
                    free(cube);
                    return IORA_ERROR;
                    */
                }



                for (iy = 0; iy< cdy; iy+=step_y) {
                    int cube_off_y;
                    off_y =  buf_offset_y + iy;
                    if (off_y >= buff_size_y)
                        ;
                    cube_off_y = iy*ndfile->smx_order[dir1];
                    for (ix = 0; ix< cdx; ix+=step_x) {
                        off_x = buf_offset_x + ix;
                        if (off_x >= buff_size_x)
                            ;
                cube_off=cube_off_y+ix*ndfile->smx_order[dir2];
                        j = off_x/step_x + (off_y/step_y * buff_size_x);
                        for (i=0;i<ndfile->smx_number[dir3]/(1+ndfile->complex[dir3]);i++) {
                            int n = cube_off+(i*ndfile->smx_order[dir3]);
                            n=cube_off;
                            buffer[j] += cube[n];
                  break;
                        }
                    }
/*
printf("%5d %5d %5d %8.2e, %5d\n",off_x,off_y,j,buffer[j],cube_off);
*/
                }
            }
        }
    }
    free(cube);
    return OK;
}

#endif
int write_smx(NDFILE *ndfile, float* buffer, int size, int record_num) {
    int i, key, idiv, irest;
    float *ptr;
    if (ndfile->nrec_per_smx == 1) {
      key = ndfile->data_start + record_num;
      return (iora_write(ndfile->filno, 
              key, 
              (char*)buffer, 
              size, 
              ndfile->data_type));
    }
   else {
      idiv = size / ndfile->record_length;
      irest= size % ndfile->record_length;
      key = ndfile->data_start + record_num*ndfile->nrec_per_smx;
      ptr = buffer;
      for (i=0; i<(size/ndfile->record_length); i++) {
         iora_write(ndfile->filno, 
                    key++, 
                    (char*)buffer, 
                    ndfile->record_length, 
                    ndfile->data_type); 
         buffer += ndfile->record_length;
      }
      if (irest != 0) 
         iora_write(ndfile->filno,
                    key, 
                    (char*)buffer, 
                    irest, 
                    ndfile->data_type); 
    }
    return (OK);
}

int iond_calc_smx_sizes (int ndim, int *size, int *complex, int *smx_size){
   int done, i, buffer_size, factor, count;
   int largest, largest_size;
   int smallest, smallest_size;
   int ratio;
   int smx_number[MAX_DIM];

   if (ndim == 1) {	/* 1D NMR */
      smx_size[0] = MIN(MAX_SMX_SIZE_1D, size[0]);
      return OK;
   }
   
  /*
   * set smallest smx_size and largest smx_number
   * and find directions with largest number of smx's (largest size)
   */
   smallest      = 0;
   smallest_size = size[smallest];
   largest       = 0;
   largest_size  = size[largest];
   for (i=0; i<ndim; i++) {
      smx_size[i]      = 1;
      smx_number[i]    = size[i];
      buffer_size     += size[i];
      if (size[i] > largest_size) {
         largest       = i;
         largest_size  = size[largest];
      }
      if (size[i] < smallest_size) {
         smallest      = i;
         smallest_size = size[smallest];
      }
   }
   
   factor      = 1;
   count       = 0;
   buffer_size = 0;
   done        = FALSE;
   while (!done ) {
     for (i=0; i<ndim; i++) {
         ratio = ( ((int)size[i]) / ((int)smallest_size) );
         if (ratio > MAX_SMX_RATIO) 
             ratio=MAX_SMX_RATIO;
         smx_size[i]  = MIN( MAX_SMX_SIZE, factor*pow(2, ratio ) );
         smx_size[i]  = MAX( smx_size[i], MIN_SMX_SIZE);
         smx_size[i]  = MIN(smx_size[i], size[i]);
/*
         printf("ratio:%d %d %d %d %d\n",i,ratio,size[i],smx_size[i],smallest_size);
*/
     }
         
    /*
     * Test whether all dimensions have reached MAX_SMX_SIZE
     * if so, signal to stop (Typically 2D NMR)
     */
     for (i=0; i<ndim; i++) {
        if (smx_size[i] == MAX_SMX_SIZE)
           count ++;
        if (smx_size[i] >= size[i]) {
           smx_size[i] = size[i];
           count ++;
        }
     }
     if (count == ndim)
        done = TRUE;
        
     if (count > ndim) {
        done = TRUE;
      }
        
     buffer_size = largest_size;
     for (i=0; i<ndim; i++) {
        if (i!=largest)
           buffer_size *= smx_size[i];
     }
     if ((unsigned int)buffer_size <= (unsigned int)MAX_BUFFER_SIZE/sizeof(float))
        factor *= 2;
     else {
        factor /=2;
        for (i=0; i<ndim; i++) {
           ratio = ( ((int)size[i]) / ((int)smallest_size) );
           if (ratio > MAX_SMX_RATIO) 
               ratio=MAX_SMX_RATIO;
           smx_size[i]  = MIN( MAX_SMX_SIZE, factor*pow(2, ratio ));
           smx_size[i]  = MAX( smx_size[i], MIN_SMX_SIZE);
           smx_size[i]  = MIN(smx_size[i], size[i]);
        }
        done = TRUE;
     }
  }
/*
     printf("Calc_smx_size: results:\n");
     for (i=0;i<ndim;i++) {
        printf(" complex(%d) = %d\n",i, complex[i]);
     }
     for (i=0;i<ndim;i++) {
        printf("    size(%d) = %d\n",i,    size[i]);
     }
     for (i=0;i<ndim;i++) {
        printf("smx_size(%d) = %d\n",i,smx_size[i]);
     }
*/
   return (OK);
}

int proc_str_record(NDFILE *ndfile, OLD3D_STR_RECORD *str_record, int rec_type) 
{
   int i;
   /*
    * First translate the needed structure parameters to our internal 
    * data structure. Here we have to descriminate between 1/2/3D data files
    * with a structure record of only 40 floats, and the nD structure record 
    * with 80 floats
    */

   ndfile->storage           = ROUND(str_record->smx_order);
   ndfile->data_start        = ROUND(str_record->data_start) -1;
   ndfile->data_size         = ROUND(str_record->data_size);
   ndfile->record_length     = fabs(ROUND(str_record->rec_size));

   for (i=0; i<MAX_DIM; i++) {
      ndfile->size[i]        = 1;
      ndfile->smx_size[i]    = 1;
      ndfile->smx_number[i]  = 1;
      ndfile->complex[i]     = REAL;
   }
       
   if (rec_type == OLD3D_STRUCTURE) {
      /*
       * OLD3D 1/2/3D data
       */
      ndfile->file_type = FILE_TYPE_OLD3D;
      for (i=0; i<OLD3D_MAX_DIM; i++) {
         ndfile->size[i]     = ROUND(str_record->old_size[i]);
         ndfile->size[i]     = MAX(1, ndfile->size[i]);

         ndfile->smx_size[i] = ROUND(str_record->old_smx_size[i]);
         ndfile->smx_size[i] = MAX(1, ndfile->smx_size[i]);

         ndfile->complex[i]  = ROUND(str_record->old_complex[i]);
      }
   }
   else if (rec_type == ND_STRUCTURE) {
      /*
       * nD data
       */
       
       ndfile->file_type = FILE_TYPE_ND;
       for (i=0; i<MAX_DIM; i++) {
          ndfile->size[i]    = ROUND(str_record->nd_size[i]);
          ndfile->size[i]    = MAX(1, ndfile->size[i]);

          ndfile->smx_size[i]= ROUND(str_record->nd_smx_size[i]);
          ndfile->smx_size[i]= MAX(1, ndfile->smx_size[i]);

          ndfile->complex[i] = ROUND(str_record->nd_complex[i]);
       }
   }
   else {
      printf ("Invalid length of structure record.\n");
      return (IOND_ERROR);
   }

   ndfile->ndim              = 0;
   ndfile->nrec_per_smx      = (int) ceil((float)ndfile->data_size / 
                                          (float)ndfile->record_length);
   for (i=0; i<MAX_DIM; i++) {
      ndfile->smx_order[i] = 1;
      if (ndfile->size[i] > 1)
         (ndfile->ndim) = i+1;
             
      ndfile->smx_number[i]  = (ndfile->size[i] / ndfile->smx_size[i]);
      if ((ndfile->size[i] % ndfile->smx_size[i]) != 0)
         ndfile->smx_number[i]++;       
   }
       
   switch (ndfile->storage) {
   case 1:
      ndfile->smx_order[0]   = 1;
      ndfile->smx_order[1]   = ndfile->smx_number[0];
      ndfile->smx_order[2]   = ndfile->smx_number[0]*ndfile->smx_number[1];
      break;
   case 2:
      ndfile->smx_order[0]   = ndfile->smx_number[1];
      ndfile->smx_order[1]   = 1;
      ndfile->smx_order[2]   = ndfile->smx_number[0]*ndfile->smx_number[1];
      break;
   case 3:
   case 4:
   case 5:
   default:
      ndfile->smx_order[MAX_DIM-1] = 1;
      for (i=MAX_DIM-2; i>=0; i--)
         ndfile->smx_order[i]= ndfile->smx_order[i+1] * ndfile->smx_number[i+1];
      break;
   } 

   return (OK);     
}

int update_str_record (NDFILE *ndfile, int par_size, int his_size, int brk_size) {

   int i;
   OLD3D_STR_RECORD *str_record;
   /*
    * First translate the needed structure parameters to our internal 
    * data structure. Here we have to descriminate between 1/2/3D data files
    * with a structure record of only 40 floats, and the nD structure record 
             * with 80 floats
    */

   if (ndfile == NULL || ndfile->structure == NULL)
      return (IOND_ERROR);
   str_record             = ndfile->structure;
     
   str_record->io_id      = -1;
   str_record->rec_size   = (float) ndfile->record_length;

   str_record->str_size   = STR_RECORD_SIZE;
   str_record->str_start  = STR_RECORD_NUM;
   str_record->str_type   = 0;

   str_record->par_size   = par_size;
   str_record->par_start  = PAR_RECORD_NUM;
   str_record->par_type   = 0;

   brk_size = (1 + brk_size/sizeof(float)) * sizeof(float);
   str_record->brk_size   = brk_size;
   str_record->brk_start  = BRK_RECORD_NUM;
   str_record->brk_type   = 0;

   his_size = (1 + his_size/sizeof(float)) * sizeof(float);
   str_record->his_size   = his_size;
   str_record->his_start  = HIS_RECORD_NUM;
   str_record->his_type   = 0;

   str_record->data_size  = (float) ndfile->data_size;
    /* simulate old 3d counting (proc_str_record)*/
   str_record->data_start = DATA_RECORD_NUM + 1;
   str_record->data_type  = (float)ndfile->data_type;

   str_record->smx_order  = (float) ndfile->storage;

   /*
    * nD data
    */
   if (ndfile->ndim <OLD3D_MAX_DIM) {
      for (i=0; i<OLD3D_MAX_DIM; i++) {
         str_record->old_size[i]     = (float) ndfile->size[i];
         str_record->old_smx_size[i] = (float) ndfile->smx_size[i];
         str_record->old_complex[i]  = (float) ndfile->complex[i];
      }
   }
        
   for (i=0; i<MAX_DIM; i++) {
      str_record->nd_size[i]     = (float) ndfile->size[i];
      str_record->nd_smx_size[i] = (float) ndfile->smx_size[i];
      str_record->nd_complex[i]  = (float) ndfile->complex[i];
   }

   ndfile->ndim              = 0;
   ndfile->nrec_per_smx      = (int) ceil((float)ndfile->data_size / 
                                          (float)ndfile->record_length);
   for (i=0; i<MAX_DIM; i++) {
      ndfile->smx_order[i] = 1;
      if (ndfile->size[i] > 1)
         (ndfile->ndim) = i+1;
             
      ndfile->smx_number[i]  = (ndfile->size[i] / ndfile->smx_size[i]);
      if ( (ndfile->size[i] % ndfile->smx_size[i]) != 0)
         ndfile->smx_number[i]++;       
   }
       
   switch (ndfile->storage) {
   case 1:
      ndfile->smx_order[0]   = 1;
      ndfile->smx_order[1]   = ndfile->smx_number[0];
      ndfile->smx_order[2]   = ndfile->smx_number[0]*ndfile->smx_number[1];
      break;
   case 2:
      ndfile->smx_order[0]   = ndfile->smx_number[1];
      ndfile->smx_order[1]   = 1;
      ndfile->smx_order[2]   = ndfile->smx_number[0]*ndfile->smx_number[1];
      break;
   case 3:
   case 4:
   case 5:
   default:
      ndfile->smx_order[MAX_DIM-1] = 1;
      for (i=MAX_DIM-2; i>=0; i--)
         ndfile->smx_order[i]= ndfile->smx_order[i+1] * ndfile->smx_number[i+1];
      break;
   }
   
/*
 * Now write the structure record to disk
 */
   iora_write(ndfile->filno,
              STR_RECORD_NUM, 
              (char*)str_record, 
              STR_RECORD_SIZE, 
              (ndfile->file_type==FILE_TYPE_ND) ? ND_STRUCTURE : OLD3D_STRUCTURE); 
 
   return (OK);     
}

int update_par_record (NDFILE *ndfile) {

/*
 * Now write the structure record to disk
 */
   iora_write(ndfile->filno,
              PAR_RECORD_NUM, 
              (char*)ndfile->parameter, 
              ndfile->structure->par_size/sizeof(float), 
              (ndfile->file_type==FILE_TYPE_ND) ? ND_PARAMETER : OLD3D_PARAMETER); 
 
   return (OK);     
}

int update_his_record (NDFILE *ndfile, char *history) 
{
    char string[80]="History Record: Not used as yet.";
    if (history == NULL)
        return OK;
    /*
     * Now write the structure record to disk
     */
    iora_write(ndfile->filno,
               HIS_RECORD_NUM, 
               (char*)history, 
               ndfile->structure->his_size/sizeof(float), 
               (ndfile->file_type==FILE_TYPE_ND) ? ND_HISTORY : OLD3D_HISTORY); 
 
    return (OK);     
}


int update_brk_record (NDFILE *ndfile, char *bruker) 
{
    char string[80]="Bruker Record: Not used as yet. ";
    if (bruker == NULL)
        return OK;
   
    /*
     * Now write the structure record to disk
     */
    iora_write(ndfile->filno,
               BRK_RECORD_NUM, 
               (char*)bruker, 
               ndfile->structure->brk_size/sizeof(float), 
               (ndfile->file_type==FILE_TYPE_ND) ? ND_BRUKER : OLD3D_BRUKER); 
 
    return (OK);     
}

NDFILE *alloc_ndfile(int parsize, int ndnumbuf) 
{
    NDFILE *ndfile;
    int i;

    if ( (ndfile = calloc(sizeof(NDFILE),1)) == NULL)
        return NULL;

    if ( (ndfile->size = calloc(sizeof(int), MAX_DIM)) == NULL) {
        free_ndfile(ndfile);
        return NULL;
    }
   
    if ( (ndfile->smx_size = calloc(sizeof(int), MAX_DIM)) == NULL) {
        free_ndfile(ndfile);
        return NULL;
    }
   
    if ( (ndfile->smx_number = calloc(sizeof(int), MAX_DIM)) == NULL) {
        free_ndfile(ndfile);
        return NULL;
    }
   
    if ( (ndfile->smx_order = calloc(sizeof(int), MAX_DIM)) == NULL) {
        free_ndfile(ndfile);
        return NULL;
    }
   
    if ( (ndfile->complex = calloc(sizeof(int), MAX_DIM)) == NULL) {
        free_ndfile(ndfile);
        return NULL;
    }
    for (i=0;i<ndnumbuf;i++)
        if ( (ndfile->buf[i].vector = calloc(sizeof(int), MAX_DIM)) == NULL) {
            free_ndfile(ndfile);
            return NULL;
        }
    if ( (ndfile->access = calloc(sizeof(int), MAX_DIM)) == NULL) {
        free_ndfile(ndfile);
        return NULL;
    }

    if ( (ndfile->order = calloc(sizeof(int), MAX_DIM)) == NULL) {
        free_ndfile(ndfile);
        return NULL;
    }

    if ( (ndfile->structure = (OLD3D_STR_RECORD*)calloc(sizeof(OLD3D_STR_RECORD), 1)) == NULL) {
        free_ndfile(ndfile);
        return NULL;
    }
    if ( (ndfile->parameter = (char*)calloc(parsize, 1)) == NULL) {
        free_ndfile(ndfile);
        return NULL;
    }
    ndfile->history = NULL;
    ndfile->bruker  = NULL;
    return (ndfile);
}

void free_ndfile (NDFILE *ndfile) {
   int i;
/* 
 * completly free all entries in the NDFILE data structure
 */
   if (ndfile == NULL)
      return;
      
   CHECK_FREE(ndfile->filename);
   CHECK_FREE(ndfile->size);
   CHECK_FREE(ndfile->smx_size);
   CHECK_FREE(ndfile->smx_number);
   CHECK_FREE(ndfile->smx_order);
   CHECK_FREE(ndfile->complex);
   CHECK_FREE(ndfile->access);
   CHECK_FREE(ndfile->order);
   CHECK_FREE(ndfile->smx_addition);
   CHECK_FREE(ndfile->smx_table);
   for (i=0;i<ndfile->ndnumbuf;i++) {
       CHECK_FREE(ndfile->buf[i].vector); 
       CHECK_FREE(ndfile->buf[i].buffer); 
   }
   CHECK_FREE(ndfile->structure); 
   CHECK_FREE(ndfile->parameter);
   CHECK_FREE(ndfile->history);
   CHECK_FREE(ndfile->bruker);
   
   CHECK_FREE(ndfile);
}

static short *fill_array(short *list, int access_inx, int loop_dim, int ranges[], int limits[])
{
    int i,j;
    int istart, istop;
    int inx;

    inx = access_inx*2;    
    for (j=1;j<=ranges[0];j+=2) {
        istart = MAX(ranges[j], limits[inx]);
        istop  = MIN(ranges[j+1], limits[inx+1]);
        if (istart <= istop) {
            for (i=istart;i<=istop;i++) {
                *list++ = access_inx;
                *list++ = i+1;
                if (access_inx < loop_dim)
                    list = fill_array(list, access_inx + 1, loop_dim,
                                      ranges + ranges[0] + 1,
                                      limits);
            }
        }
    }
    return list;
}

static short *fill_array_smx(short *list, int access_inx, int loop_dim, int ranges[])
{
    int i,j;

    for (j=1;j<=ranges[0];j+=2) {
        for (i=ranges[j];i<=ranges[j+1];i++) {
            *list++ = access_inx;
            *list++ = i;
            if (access_inx < loop_dim)
                list = fill_array_smx(list, access_inx + 1, loop_dim, ranges + ranges[0] + 1);
        }
    }
    return list;
}

short *iond_get_sorted_list (NDFILE *ndfile, short *pp, int *ranges){

    short *rr2, *pp2;
    short *rr;
    int *rangesmx, tmpsize, rr2size;
    int ok, i, j, ndim;
    int jstart, jstop;
    int nsize, loop_dim, access_inx;
    
    rr = pp;
    /*
     * For 1D files it is simple
     */
    if (ndfile->ndim == 1) {
        *pp++ = 1;
        *pp++ = 1;
        *pp++ = ENDOFRANGE;
        *pp++ = ENDOFRANGE;
        return rr;
    };
    /*
     * First create a range list based on SMX coord in stead of records
     */
    nsize = 0;
    for (i=1; i<ndfile->ndim; i++) {
        nsize += ranges[nsize]+1;
    }
    
    rangesmx = (int*)malloc(MAX(nsize,MAX_DIM*2) * sizeof(int));
    assert(rangesmx);

    rr2size  = 1;
    tmpsize  = 0;
    jstop    = -1;
    for (i=1; i<ndfile->ndim; i++) {
        jstart = jstop + 1; 
        jstop  = jstart + ranges[jstart];
        rangesmx[jstart] = ranges[jstart];
        tmpsize += ranges[jstop]-ranges[jstart]+2;
        for (j=jstart+1;j<=jstop;j++) {
            rangesmx[j] = ranges[j] / ndfile->smx_size[ndfile->access[i]];
        } 
        rr2size *= 1+tmpsize;      
    }
    rr2 = (short*) malloc(rr2size * ndfile->ndim-1 * sizeof(short));
    assert(rr2);
    pp2 = rr2;



    /*
     * First create a 'list' describing how to loop through the spectrum on a 
     * SMX hypercube basis
     */
    access_inx = 1;
    loop_dim   = ndfile->ndim-1;
    pp2=fill_array_smx(pp2, access_inx, loop_dim, rangesmx);
    *pp2++ = ENDOFRANGE;
    *pp2++ = ENDOFRANGE;
    pp2 = rr2;
    
    /*
     * Now create a 'list' how to loop through the SMX's in buffer on a record basis
     */
    ok = FALSE;
    if (*pp2 == ENDOFRANGE)
        ok = TRUE;

    while (!ok) {
       if (*pp2 == ENDOFRANGE)
           ok = TRUE;
       else {
           access_inx = *pp2++; 
           i          = access_inx*2;
           rangesmx[i]   = *pp2++  * ndfile->smx_size[ndfile->access[access_inx]];
           rangesmx[i+1] = rangesmx[i] 
                         + ndfile->smx_size[ndfile->access[access_inx]] -1;
           if (access_inx==loop_dim) {
               access_inx = 1;
               pp=fill_array(pp, access_inx, loop_dim, ranges, rangesmx);
           }
       }
    }
    
    free(rr2);
    free(rangesmx);
    *pp++ = ENDOFRANGE;
    *pp++ = ENDOFRANGE;
    return rr;
}
