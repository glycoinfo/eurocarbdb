/* 
 * iora.c
 * IO routines for Random Access of files
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
 *
 * Use:	    Interface to Fortran by routines in ioraf.f
 * Use:	    Interface to iond.c via iosmx.c
 *
 * Version:	230797	(BL)	start of project (based on iorac.c etc.)
 * 		010897	(BL)	initial working version (single endian)
 * 		190897	(BL)	initial working version (dual endian, float*4 + char)
 *                              No float*2 and truncation working!!!!!
 *              120298	(BL)	Continuation of project, bug fixes, code cleanup
 *          26 12 1998  (BL)	bug fix for old old3d file with physical record-length < history rec.
 *
 */

#include "iora.h"
static IORA_STATUS iora_status;
static byte4_union std_long;
static unsigned char std_e0;
static unsigned char std_m0;
static unsigned char std_m1;
static unsigned char std_m2;
static unsigned char std_dbias;
/*
 * rav	   Random Access   variables
 */
static IORA_STATUS iora_status;
static char        buffer[MAX_BYTES];
static NODE1_T    *head[MAX_IORA_FILES];	    /* pointers array to list1 headers */
static NODE1_T    *tail[MAX_IORA_FILES];	    /* pointers array to list1 tails   */

int iora_read_old3d_header(int filno) ;


static void onerror(int code,int mode,int filno, char *name,int err_code) {
   if (code != 6)
      printf(" IORA error(%d) : %d   mode: %d   filno: %d file: %s   errno: %d\n"
       , errno, code, mode, filno, name, err_code);
   perror(" System message");
}

IORA_FILE *iora_get_fp(int filno) {
   if (filno < 0 || filno > MAX_IORA_FILES) 
      return NULL;  
   else
      return (iora_status.iora_fp[filno]);
}

int iora_open(char *filename, int option, int *nrecs) {
/*
 * iora_open
 * Open a Random Access file
 */
   int          ierr=0;
   int          filno;
   IORA_FILE   *iora_fp=NULL;
   IORA_HEADER *iora_header=NULL;
   
  /*
   * make sure that iora stuff has been initialized 
   */
   if (iora_status.iora_fp == NULL) {
      if (iora_init() == IORA_ERROR) {
         printf ("iora_open: iora_init error\n");
         return (IORA_ERROR);
      }
   }
   
  /*
   * check whether the file is already open 
   * if file was already open: close it first and then reopen 
   */
   if ((filno = iora_get_filno(filename)) != IORA_ERROR ) {
      iora_fp->close_option = CLOSE_KEEP;    
      iora_close(filno);
   }
   
  /* 
   * now find a free slot in the iora_fp array for this file 
   */
   if (((filno = iora_new_filno()) <0) | (filno >= MAX_IORA_FILES) ) {
      printf ("iora_open: No free slots available\n");
      return IORA_ERROR;
   }
   
  /*
   * Allocate the memory for the IORA_FILE structure; 
   * fill the IORA_FILE structure with appropriate info 
   */
   iora_fp = iora_alloc_file(filno);
   if (iora_fp  == NULL ){
      printf ("iora_open: iora_alloc_file error\n");
      return IORA_ERROR;
   }
   else {
      iora_fp->open_option   = option; 
   }
  /*
   * Allocate the memory for the IORA_HEADER structure; 
   * fill the IORA_HEADER structure with appropriate info 
   */
   if ((iora_fp->header = (IORA_HEADER*)calloc(1,sizeof(IORA_HEADER)))  == NULL ) {
      printf ("iora_open: iora header calloc error\n");
      return IORA_ERROR;
   }
   iora_header = iora_fp->header;
 
   list1_init(filno);		/* list: ptrs to RecordMap blocks */
   switch (option) {
   case OPEN_NEW:        /* New file */
       /*
        * bug in gcc ???????. O_TRUNC does not seem to work
        * be sure 'filename' has been removed, use unlink
        */
       unlink(filename);
       
      iora_fp->fd=open(filename,O_CREAT|O_TRUNC|O_RDWR|O_BINARY, 00644);
      if (iora_fp->fd < 0) {
         onerror(1,option,filno,filename,errno);
         return(IORA_ERROR);
      }
      iora_status.open_files++;
      iora_fp->read_only    = FALSE;
      iora_fp->filename     = strdup(filename);
      iora_fp->close_option = CLOSE_KEEP;
      iora_set_endian(filno, IORA_OS_ENDIAN);
#ifdef NOT_NOW
     /*
      * OK the file is now opened succesfully.
      * Now we can start setting up all that is needed for Random Access
      * of records. We must know where each record starts and stops....
      * This is done in the 'ra_buffer' data tree structure.
      * Set it up now
      */
      buffer_tree_init(filno);
#endif      
     /*
      * Allocate the memory for the recordmap structure; 
      * fill the recordmap structure with appropriate info.
      * i.e. set all entries to -1 by making all bytes 0xff.
      */
      if ((iora_fp->map = (FILE_PTR*)malloc (*nrecs * sizeof(FILE_PTR)))  == NULL ) {
         printf ("iora_open: iora map malloc error\n");
         return IORA_ERROR;
      }
      memset (iora_fp->map, 0xff, (*nrecs) * sizeof(FILE_PTR) );

      /*
       * In the first entry of the RecordMap (record_num=0) we store the current 
       * position. I.e. the first byte after the skiped records.
       * In the new setup there are no longer skipped records. So the start is
       * supposed to be '0' always. (should be the result of LTELL(filno))
       * And we set the number of records to zero.
       */
      iora_header->std_long   = std_long.i;
      iora_header->size       = sizeof(IORA_HEADER);
      sprintf (iora_header->magical, MAGICAL_STRING);
      sprintf (iora_header->creator, CREATOR_STRING);
      sprintf (iora_header->content, CONTENT_STRING);
      sprintf (iora_header->version, VERSION_STRING);
      iora_header->npars      = NPARS;
      iora_header->maxrec     = *nrecs;
      iora_header->nrecs      = 0;
      iora_header->nskip      = 0;
      iora_header->mapsize    = *nrecs;
      iora_header->nmaps      = 1;
      iora_header->type       = DEFAULT_NEW_RECORD_TYPE;
      iora_header->tresh_high = -1.0;	/* Obsolete: These values belong
      iora_header->tresh_level= 0.0;	 * in the content specific layer
      iora_header->tresh_low  = 1.0;	 * Here for compatibility 
                                         */
      iora_header->rec_length = 0;

      /*
       * By now we have setup all we need to have access to the
       * records. Lets write this to the file.
       */
      if ((ierr=iora_write_header(filno))!=OK)
	 return (IORA_ERROR);
      break;

   case OPEN_R:        /* existing file, Read-only */
      iora_fp->fd=open(filename,O_RDONLY|O_BINARY);
      if (iora_fp->fd < 0) {
         onerror(2,option,filno,filename,errno);
         iora_close(filno);
         return(IORA_ERROR);
      }
      iora_status.open_files++;
      iora_fp->read_only    = TRUE;
      iora_fp->filename     = strdup(filename);
      iora_fp->close_option = CLOSE_KEEP;

#ifdef NOT_NOW
     /*
      * OK the file is now opened succesfully.
      * Now we can start setting up all that is needed for Random Access
      * of records. We must know where each record starts and stops....
      * This is done in the 'ra_buffer' data tree structure.
      * Set it up now
      */
      buffer_tree_init(filno);
#endif
      /*
       * Read the file header
       * and start determining the file type.
       */
      
      iora_fp->eof = LEND(filno);
      REWIND(filno);
      if ((ierr=iora_read_header(filno))!=0) {
         iora_fp->close_option = CLOSE_KEEP;    
         iora_close(filno);
	 return (IORA_ERROR);
      }
      *nrecs = iora_fp->header->nrecs;
      break;

   case OPEN_RW:        /* existing file, Read-write */
      iora_fp->fd=open(filename,O_RDWR|O_BINARY);
      /*
       * AK, 19-10-1999
       * EACCES  The requested access to the file is not allowed
       * EROFS   pathname refers to a file on a read-only filesystem
       */
      if (iora_fp->fd < 0 && (errno==EACCES || errno==EROFS)) {
        /* 
         * oeps, May be due to limited privileges? 
         * Try again , but now readonly
         */
         iora_fp->fd=open(filename,O_RDONLY|O_BINARY);
         if (iora_fp->fd < 0) {
            onerror(3,option,filno,filename,errno);
            iora_close(filno);
            return(IORA_ERROR);
         }
         iora_fp->read_only    = TRUE;
      }
      else
         iora_fp->read_only    = FALSE;
      

      iora_status.open_files++;
      iora_fp->filename     = strdup(filename);
      iora_fp->close_option = CLOSE_KEEP;
#ifdef NOT_NOW
     /*
      * OK the file is now opened succesfully.
      * Now we can start setting up all that is needed for Random Access
      * of records. We must know where each record starts and stops....
      * This is done in the 'ra_buffer' data tree structure.
      * Set it up now
      */
      buffer_tree_init(filno);
#endif
      /*
       * Read the file header
       * and start determining the file type.
       */
      iora_fp->eof = LEND(filno);
      REWIND(filno);
      if ((ierr=iora_read_header(filno))!=0) {
         iora_fp->close_option = CLOSE_KEEP;    
         iora_close(filno);
	 return (IORA_ERROR);
      }
      *nrecs = iora_fp->header->nrecs;
      break;

   default:    /* unknown open flag */
      iora_fp->fd = 0;
      onerror(4, option, filno, filename, errno);
      iora_close(filno);
      return(IORA_ERROR);

   }

   return (filno);
}


int iora_close(int filno) {
/*
 * iora_close
 * option determines whether the file should be kept (CLOSE_KEEP)
 * or deleted (CLOSE_DEL) after closing the file.
 */
   int          ierr = 0;
   char        *filename;
   IORA_FILE   *iora_fp;
   IORA_HEADER *iora_header;
   
   if (filno < 0 || filno > MAX_IORA_FILES) {
      printf ("iora_close: file number %d out of valid range [0..MAX_IORA_FILES]\n", filno);
      return(0);    /* file was not opened in the first place */
   }
   
   iora_fp     = iora_status.iora_fp[filno];
   if (iora_fp == NULL) {
      printf ("iora_close: file %d was not opended.", filno);
      return(0);    /* file was not opened in the first place */
   }

   iora_header = iora_fp->header;
   filename    = iora_fp->filename;

   /* the following is probably needed to ensure that all intended changes are 
    * actually store on disk. Probably we should latyer also test whether 
    * there actualy were changes
    */
   if (!iora_fp->read_only && iora_fp->fd >= 0) {
      iora_buffer_save_all(filno);  /* write unsaved records from buffer to disk */
      ierr = iora_write_header(filno);
   }
   
   switch (iora_fp->close_option) {
      case CLOSE_DEL:
         ierr+=close(iora_fp->fd);
         if (ierr < 0) 
            onerror(5, iora_fp->close_option, filno, filename, errno);
         ierr+=unlink(filename);
         if (ierr < 0) 
            onerror(6, iora_fp->close_option, filno, filename, errno);
         break;
      case CLOSE_KEEP:
      default:		/* if wrong close option: play it safe and keep the it */
         ierr+=close(iora_fp->fd);
         if (ierr < 0) 
            onerror(7, iora_fp->close_option, filno, filename, errno);
         break;
   }
/*
   print_buffer_stat();
*/
   iora_status.open_files--;
   CHECK_FREE(iora_fp->filename);	/* free filename  */
   CHECK_FREE(iora_fp->map)		/* free RecordMap */
   CHECK_FREE(iora_fp->specific)        /* free data specific block */
   CHECK_FREE (iora_header)	        /* free header */
   CHECK_FREE(iora_fp);
   iora_status.iora_fp[filno] = NULL;
     
   list1_free_all(filno);
#ifdef NOT_NOW
   buffer_tree_kill(filno);
#endif
   if (ierr)
      return (IORA_ERROR);
   else
      return(OK);
}

int iora_buffer_save_all (int filno) {
   int record_num;
   int count;
#ifdef NOT_NOW   
   /*
    * This routine should obtain all records in the buffer for file 'filno'
    * Those record with the 'on_disk' flag on FALSE (i.e. The contents in the buffer differs
    * from that on disk) should be written to disk. The record may remain in the buffer. However, the 
    * 'on_disk' flag should be updated to TRUE
    */
   count=0;
   record_num = iora_buffer_next(filno, -1);
   while ( record_num != NOT_IN_BUFFER ) {
      count++;
      iora_buffer_save_record(filno, record_num);
      record_num = iora_buffer_next(filno, 1);
   }   
#endif
   return(OK);
}

int iora_buffer_save_record (int filno, int record_num) {

#ifdef NOT_NOW
   if (iora_buffer_get_ondisk(filno, record_num) == FALSE)
      iora_buffer_flush(filno, record_num);
#endif
   return(OK);
}


int iora_lowrite (int filno, int record_num, char *vect, int size) {
   long offset;
   int ierr=0, nbytes;
   short rec_size_s=0;
   int   rec_size=0;
   IORA_FILE   *iora_fp;
   IORA_HEADER *iora_header;
   
   iora_fp = iora_status.iora_fp[filno];
   iora_header = iora_fp->header;
   
   /*
    * Check whether the requested write does not exceed the maximum
    * number of indeces in the RecordMap.
    * If so, extend the RecordMap both in memory and on disk.
    */
   if (record_num < 0) {
      printf ("\niora_lowrite: record_num=%d; Should be non-negative\n", record_num);
      return (IORA_ERROR);
   }
   
   if (iora_fp->read_only == TRUE) {
      printf ("\niora_lowrite: File %d (%s) is opened 'read-only'\n",
               filno, iora_fp->filename);
      return (IORA_ERROR);
   }

   iora_assert_map(filno, record_num);

   if (size > 0) 
      REC_SIZE(filno, record_num) = size;
   else if (REC_SIZE(filno, record_num) != size)
      printf ("size (%d) and REC_SIZE(filno, record_num) (%d) not equal\n", size, (int)REC_SIZE(filno, record_num));
   if (REC_SIZE(filno, record_num) < 0) 
      return (IORA_ERROR);
   
   
   /*
    * Check whether this record has been written before
    * and if so,  Check if the old record is big enough to store the new
    * record.
    * If not so: forget about the old record and create a new one at EOF
    *	The old data, however, is not removed!
    *	This would require a lot of moving of data on disk.
    *	You better copy the file with 'reduce' or 'testio'.
    */
   offset = REC_OFFSET(filno,record_num);
   if (offset > 0) {
      LSEEK (filno, offset);		        /* goto known position*/

      if (iora_fp->map[record_num].size == -1 && iora_fp->type == IORA_OLD3D) {
         nbytes = sizeof(short);
         if (read(iora_fp->fd, (&rec_size_s), nbytes) < 0) {
            onerror(10, 0, filno, iora_fp->filename, errno);
            ierr = 1;
         }
         rec_size = (int)rec_size_s;
      }
      else 
         rec_size = REC_SIZE(filno, record_num);

         
      if (size > rec_size) {
	 (iora_header->nrecs)--;
	 offset = -1;
      }
   }

   if (offset < 0) {	        /* new record */
      offset = LEND(filno);
      REC_OFFSET(filno,record_num) = offset;
      (iora_header->nrecs)++;
   }
   else /* existing record with correct size*/ 
      LSEEK (filno, offset);		        /* goto known position*/

   /*
    * OK now we are at the right position to write the record to disk
    */
   if (iora_fp->map[record_num].size == -1 && iora_fp->type == IORA_OLD3D) {
      nbytes = sizeof(short);
      rec_size = (short)(size);
      iora_fp->swap2(&rec_size);
      if (write(iora_fp->fd, &rec_size, nbytes) < 0) {
         onerror(11, nbytes, filno, iora_fp->filename, errno);
         ierr += 1;
      }
   }

   if (write(iora_fp->fd, vect, size) < 0) {
      onerror(12, size, filno, iora_fp->filename, errno);
      ierr += 1;
   }

   iora_fp->eof = MAX(LTELL(filno), iora_fp->eof);

   if (ierr)
      return (IORA_ERROR);
   else
      return(OK);
}

int iora_buffer_write (int filno, int record_num, char *vect, int size) {
   long b_offset, d_offset, nextmap;
   int ierr=0, firstnew,  nbytes;
   short rec_size_s=0;
   int   rec_size=0, stored_size;
   buf_mark_t	marker_block;
   IORA_FILE   *iora_fp;
   IORA_HEADER *iora_header;
   
   iora_fp = iora_status.iora_fp[filno];
   iora_header = iora_fp->header;
   
   if (record_num < 0) {
      printf ("\niora_buffer_write: record_num=%d; Should be non-negative\n", record_num);
      return (IORA_ERROR);
   }
   
   if (size > 0) 
      REC_SIZE(filno, record_num) = size;
   else if (REC_SIZE(filno, record_num) != size)
      printf ("size (%d) and REC_SIZE(filno, record_num) (%d) not equal\n", size, (int)REC_SIZE(filno, record_num));
   if (REC_SIZE(filno, record_num) < 0) 
      return (IORA_ERROR);
   rec_size = REC_SIZE(filno, record_num);
   
   
   /*
    * First test whether the record is in ra_buffer. 
    * If so we should overwrite the buffer content with the updated record (if sizes permit this)
    * When the record_size of the new record (truncation) differs from the buffered one, then
    * we should remove the buffered one and add the new one to the buffer.
    */
#ifdef NOT_NOW
   b_offset = get_record_offset(filno, record_num);
   if (b_offset == NOT_IN_BUFFER) {	/* record not yet in ra_buffer: add it */
         iora_buffer_append (filno, record_num, vect, rec_size);
         if (get_record_offset(filno, record_num) == NOT_IN_BUFFER)
            iora_lowrite (filno, record_num, vect, rec_size);	/* write now */
   }
   else {	/* record already in the ra_buffer: overwrite it */
      /*
       * Now read in the block parameters and compare sizes.
       */
      stored_size = iora_buffer_get_size(filno, record_num);
      if (rec_size <= stored_size) {	/* rec in buffer has same size: easy overwrite	*/
         iora_buffer_set_ondisk(filno, record_num, FALSE);
         iora_buffer_replace (filno, record_num, vect, rec_size);
      }
      else {	/* rec in buffer has different size: remove old and add it*/
         remove_record_offset(filno, record_num );
         iora_buffer_append (filno, record_num, vect, rec_size);
         if (get_record_offset(filno, record_num) == NOT_IN_BUFFER)
            iora_lowrite (filno, record_num, vect, rec_size);	/* write now */
      }
   }
#else
            iora_lowrite (filno, record_num, vect, rec_size);	/* write now */
#endif
   if (ierr)
      return (IORA_ERROR);
   else
      return(OK);
}

int iora_assert_map(int filno, int record_num) {
   long nextmap, offset;
   int ierr=0, firstnew,  nbytes;
   IORA_FILE   *iora_fp;
   IORA_HEADER *iora_header;
   
   iora_fp     = iora_status.iora_fp[filno];
   iora_header = iora_fp->header;

   /*
    * Check whether the requested write does not exceed the maximum
    * number of indeces in the RecordMap.
    * If so, extend the RecordMap (iora_assert_map) both in memory and on disk.
    */

   while (record_num >= iora_header->maxrec) {
      firstnew = iora_header->maxrec;
      iora_header->maxrec += iora_header->mapsize;
      (iora_header->nmaps)++;
      nextmap = LEND(filno);	        /* goto EOF */
      list1_append(filno, nextmap);

      /*
       * write dummy offset for next RecordMap to disk
       */
      nbytes = sizeof(long);
      offset = 0;
      printf("write map\n");
      if (write(iora_fp->fd, &offset, nbytes) < 0) {
         onerror(8, nbytes, filno, iora_fp->filename, errno);
         return (IORA_ERROR);
      }

      /*
       * Increase the array size of the RecordMap 
       * and set new part of the RecordMap to 0xff (-1)
       */
      nbytes = (iora_header->maxrec) * sizeof(FILE_PTR);
      iora_fp->map = realloc(iora_fp->map, nbytes);

      nbytes = (iora_header->mapsize) * sizeof(FILE_PTR);
      memset (&iora_fp->map[firstnew], 0xff, nbytes );
      iora_swap4(filno, &iora_fp->map[firstnew], nbytes/sizeof(long));
      if (write(iora_fp->fd, &iora_fp->map[firstnew], nbytes) < 0) {
         onerror(9, nbytes, filno, iora_fp->filename, errno);
         iora_swap4(filno, &iora_fp->map[firstnew], nbytes/sizeof(long));
         return (IORA_ERROR);
      }
      iora_swap4(filno, &iora_fp->map[firstnew], nbytes/sizeof(long));
   }
   return (OK);
}

int iora_loread (int filno, int record_num, char *dest, int *size) {
   int          d_offset, rec_size;
   int          ierr=0, nbytes;
   short        old3d_rec_size;
   IORA_FILE   *iora_fp;
   IORA_HEADER *iora_header;
   
   iora_fp     = iora_status.iora_fp[filno];
   iora_header = iora_fp->header;

   if (record_num >= iora_header->maxrec) {
      printf ("\niora_loread: record_num=%d is to large for RecordMap\n", record_num);
      *size = 0;
      return (IORA_ERROR);
   }

   if (record_num < 0) {
      printf ("\niora_loread: record_num=%d; Should be non-negative\n", record_num);
      *size = 0;
      return (IORA_ERROR);
   }

   d_offset = REC_OFFSET(filno, record_num);

   if (d_offset < 0 || d_offset >= iora_fp->eof) {			        /* non existing record */
      *size = 0;
      printf("non-existing record: %d at offset %d\n", record_num, (int)d_offset);
      return (IORA_ERROR);
   }

   LSEEK (filno, d_offset);		        /* goto known position*/

   if (*size <= 0) {

      if (iora_fp->map[record_num].size == -1 && iora_fp->type == IORA_OLD3D) {

        /*
         * Read the record size from disk
         */
         nbytes = sizeof(short);
         old3d_rec_size = 0;
         if ((ierr=read(iora_fp->fd, &old3d_rec_size, nbytes)) < 0) {
            onerror(23, 0, filno, iora_fp->filename, errno);
            return (IORA_ERROR);
         }
         iora_fp->swap2(&old3d_rec_size);
         rec_size  = (int)old3d_rec_size;
            
         if ((*size < 0) || (rec_size < *size) )
	    *size  = rec_size;
      }
      else {     /* iora version 2 and later */
         *size     = iora_fp->map[record_num].size;
         rec_size  = *size;
      }
   }

   /*
    * Read the data from disk
    */
   if ((ierr=read(iora_fp->fd, dest, *size)) < 0) {
      onerror(24, 0, filno, iora_fp->filename, errno);
      return (IORA_ERROR);
   }

   return(OK);
}

int iora_buffer_type(int filno, int record_num) {
    return (FALSE);
    
/*
   if ((REC_TYPE(filno, record_num) == FIXED) |
       (REC_TYPE(filno, record_num) == OLD3D_STRUCTURE) |
       (REC_TYPE(filno, record_num) == OLD3D_PARAMETER) |
       (REC_TYPE(filno, record_num) == OLD3D_BRUKER) |
       (REC_TYPE(filno, record_num) == OLD3D_HISTORY) |
       (REC_TYPE(filno, record_num) == ND_STRUCTURE) |
       (REC_TYPE(filno, record_num) == ND_PARAMETER) |
       (REC_TYPE(filno, record_num) == ND_BRUKER) |
       (REC_TYPE(filno, record_num) == ND_HISTORY) )
      return (FALSE);
   else
      return (TRUE);
*/
}

#ifdef NOT_NOW

int iora_buffer_read(int filno, int record_num, char *dest, int *size) {
   int		b_offset;
   int          rec_size;
   IORA_FILE   *iora_fp;
   IORA_HEADER *iora_header;
   
   iora_fp = iora_status.iora_fp[filno];

   rabuf_readcount();	/* nreads++; */

   /*
    * First test whether the record is in buffer. If not return zero.
    */

   b_offset = get_record_offset(filno, record_num);
   rec_size = *size;
 
   if (b_offset == NOT_IN_BUFFER) {	/* Read it and buffer it if needed */
      iora_loread(filno, record_num, dest, size);

      if (iora_buffer_type(filno, record_num) == FALSE)
         return (OK);
      
      iora_buffer_append(filno, record_num, dest, *size);
   }
   else {
      rabuf_hitcount();	/* nreadhits++; */

      /*
       * Now copy data from buffer to 'dest'
       */
      iora_buffer_copy(b_offset, dest, size);
   }

   return(OK);
}

#endif

int iora_buffer_read(int filno, int record_num, char *dest, int *size) 
{
   iora_loread(filno, record_num, dest, size);

   return(OK);
}

int iora_read(int filno, int record_num, char *dest, int *n, int *type) {
   int nbytes, ctype, rlen;
   int ierr=0;
   IORA_FILE   *iora_fp;
   IORA_HEADER *iora_header;
   
   if ((filno < 0) | (filno > MAX_IORA_FILES)) {
      printf("iora_read: Wrong filno: %d\n",filno);
      return(IORA_ERROR);
   }

   iora_fp = iora_status.iora_fp[filno];
   if ((iora_fp      == NULL) |
       (iora_fp->fd == 0) ){
      printf("iora_read: file not open, filno: %d\n",filno);
      return(IORA_ERROR);
   }
      
   iora_header = iora_fp->header;

   if (record_num >= iora_header->maxrec){
     /*
      printf("iora_read: Wrong record_num (%d) for filno: %d\n",record_num, filno);
      */
      return(IORA_ERROR);
   }

   ctype = REC_TYPE(filno, record_num);
   rlen  = REC_SIZE(filno, record_num);
   *type = ctype;

   switch (ctype) {
   case DATA_UNKNOWN:
      ierr = 1;
      *n = 0;
      break;

   case FIXED:
   case OLD3D_STRUCTURE:
   case OLD3D_PARAMETER:
   case OLD3D_BRUKER:
   case OLD3D_HISTORY:
   case ND_STRUCTURE:
   case ND_PARAMETER:
   case ND_BRUKER:
   case ND_HISTORY:
      nbytes = (*n) * sizeof(float);
      if (nbytes < 0) {
         nbytes = abs(nbytes);  
         if (nbytes > rlen) {
            printf ("iora_read: Read request truncated to actual record-length: %d\n", rlen);
            nbytes = rlen;
         }
         else {
            printf ("iora_read: record %d is longer (%d) than requested (%d) \n", record_num, rlen, nbytes);
         }
      }
      ierr = iora_buffer_read(filno, record_num, dest, &nbytes);
      if (ierr != 0) {
         printf("error with filno: %d\n",filno);
         return (IORA_ERROR);
      }
      switch (ctype) {
      case OLD3D_STRUCTURE:
      case OLD3D_PARAMETER:
      case OLD3D_BRUKER:
      case OLD3D_HISTORY:
      case ND_STRUCTURE:
          iora_swap4(filno, dest, nbytes/sizeof(float));
      }
      *n = (int) (nbytes / sizeof(float));
      break;

   case OLD3D_DATA_NOCOMPRESS:
   case DATA_FLOAT_4:
      nbytes = (*n) * sizeof(float);
      if (nbytes < 0)
	 nbytes  = rlen;
      ierr = iora_buffer_read(filno, record_num, dest, &nbytes);
      *n = (int) (nbytes / sizeof(float));
      iora_swap4(filno, dest, *n);
      break;

   case OLD3D_DATA_NOCOMPRESS_2:
   case DATA_FLOAT_2:
      nbytes = (*n) * sizeof(float2);
      if ((ierr = iora_buffer_read(filno, record_num, buffer, &nbytes)) == 0) {       
         ierr=f2_f4 (buffer, &nbytes, (float*)dest, n);
      }
      break;

   case DATA_FLOAT_3:
      nbytes = (*n) * sizeof(float3);
      if ((ierr = iora_buffer_read(filno, record_num, buffer, &nbytes)) == 0) {       
         ierr=f3_f4 (buffer, &nbytes, (float*)dest, n);
      }
      break;

   case OLD3D_DATA_COMPRESS_4:
   case DATA_FLOAT_4_TRUNC:
      nbytes = -1;	    /* actual value unkown yet */
      if ((ierr = iora_buffer_read(filno, record_num, buffer, &nbytes)) == 0) {
	 ierr=f4_decmprs (filno, buffer, &nbytes, (float*)dest, n, &iora_header->tresh_level);
      }
      break;

   case DATA_FLOAT_3_TRUNC:
      nbytes = -1;	    /* actual value unkown yet */
      if ((ierr = iora_buffer_read(filno, record_num, buffer, &nbytes)) == 0) {
	 ierr=f3_decmprs (filno, buffer, &nbytes, (float*)dest, n, &iora_header->tresh_level);
      }
      break;

   case OLD3D_DATA_COMPRESS_2:
   case DATA_FLOAT_2_TRUNC:
      nbytes = -1;	    /* actual value unkown yet */
      if ((ierr = iora_buffer_read(filno, record_num, buffer, &nbytes)) == 0) {
	 ierr=f2_decmprs (filno, buffer, &nbytes, (float*)dest, n, &iora_header->tresh_level);
      }
      break;

   case DATA_CHAR:
   default:
      nbytes = (*n);
      if (nbytes < 0)
	 nbytes  = rlen;
      ierr = iora_buffer_read(filno, record_num, dest, &nbytes);
      *n = nbytes;
      break;

   }
   if (ierr)
      return (IORA_ERROR);
   else
      return(OK);
}


int iora_write(int filno, int record_num, char *source, int n, int type) {
   int nbytes, rlen;
   int ierr=0;
   IORA_FILE   *iora_fp;
   IORA_HEADER *iora_header;
   
   if ((filno < 0) | (filno > MAX_IORA_FILES))
      return(IORA_ERROR);

   iora_fp = iora_status.iora_fp[filno];
   if ((iora_fp      == NULL) |
       (iora_fp->fd == 0) |
       (n < 0) ) 
      return (IORA_ERROR);
      
   iora_header = iora_fp->header;


   rlen= MAX(0,iora_header->rec_length);

   switch (type) {
   case DATA_UNKNOWN:
      ierr = 1;
      break;

   case FIXED:
   case OLD3D_STRUCTURE:
   case OLD3D_PARAMETER:
   case OLD3D_BRUKER:
   case OLD3D_HISTORY:
   case ND_STRUCTURE:
      nbytes = (n) * sizeof(float);
      iora_swap4(filno, source, nbytes/sizeof(float));
      ierr = iora_lowrite(filno, record_num, source, nbytes );
      iora_swap4(filno, source, nbytes/sizeof(float));
      break;
   case ND_PARAMETER:
   case ND_BRUKER:
   case ND_HISTORY:
      nbytes = (n) * sizeof(float);
      ierr = iora_lowrite(filno, record_num, source, nbytes );
      break;

   case OLD3D_DATA_NOCOMPRESS:
   case DATA_FLOAT_4:
      ierr = iora_buffer_write(filno, record_num, source, (n*sizeof(float)));
      break;

   case OLD3D_DATA_NOCOMPRESS_2:
   case DATA_FLOAT_2:
      nbytes = MAX_BYTES;
      if ((ierr=f4_f2 ((float*)source, &n, buffer, &nbytes)) != 0)
	 return (IORA_ERROR);
      ierr = iora_buffer_write(filno, record_num, buffer, (n*sizeof(float2)) );
      break;

   case DATA_FLOAT_3:
      nbytes = MAX_BYTES;
      if ((ierr=f4_f3 ((float*)source, &n, buffer, &nbytes)) != 0)
	 return (IORA_ERROR);
      ierr = iora_buffer_write(filno, record_num, buffer, (n*sizeof(float3)) );
      break;

   case OLD3D_DATA_COMPRESS_4:
   case DATA_FLOAT_4_TRUNC:
#ifdef NOT_NOW
      remove_record_offset (filno, record_num);
#endif
      nbytes = MAX_BYTES;
      if ((ierr=f4_cmprs (filno, (float*)source, &n, buffer, &nbytes
          , &iora_header->tresh_high
          , &iora_header->tresh_low   )) != 0)
	 return (IORA_ERROR);
      ierr = iora_buffer_write(filno, record_num, buffer, nbytes);
      break;

   case DATA_FLOAT_3_TRUNC:
#ifdef NOT_NOW
      remove_record_offset (filno, record_num);
#endif
      nbytes = MAX_BYTES;
      if ((ierr=f3_cmprs (filno, (float*)source, &n, buffer, &nbytes
          , &iora_header->tresh_high
          , &iora_header->tresh_low   )) != 0)
	 return (IORA_ERROR);
      ierr = iora_buffer_write(filno, record_num, buffer, nbytes);
      break;

   case OLD3D_DATA_COMPRESS_2:
   case DATA_FLOAT_2_TRUNC:
#ifdef NOT_NOW
      remove_record_offset (filno, record_num);
#endif
      nbytes = MAX_BYTES;
      if ((ierr=f2_cmprs (filno, (float*)source, &n, buffer, &nbytes
          , &iora_header->tresh_high
          , &iora_header->tresh_low   )) != 0)
	 return (IORA_ERROR);
      ierr = iora_buffer_write(filno, record_num, buffer, nbytes);
      break;

   case DATA_LONG:
      nbytes = (n);
      ierr = iora_buffer_write(filno, record_num, source, n*sizeof(long));
      break;

   case DATA_SHORT:
      nbytes = (n);
      ierr = iora_buffer_write(filno, record_num, source, n*sizeof(short));
      break;

   case DATA_CHAR:
   default:
      ierr = iora_buffer_write(filno, record_num, source, n);
      break;

   }
   
   if (ierr == 0) {
      REC_TYPE(filno, record_num) = type;
      REC_SIZE(filno, record_num) = n;
   }

   if (ierr)
      return (IORA_ERROR);
   else
      return(OK);
}

int iora_get_fileinfo(int filno, int request_code, int *info)
{
   IORA_FILE   *iora_fp;
   
   iora_fp = iora_status.iora_fp[filno];
   *info = 0;
   if (iora_fp == NULL) {
      return IORA_ERROR;
   }
   switch (request_code) {
   case IORA_INFO_FILNO:
       *info = iora_fp->filno;
       break;
   case IORA_INFO_IUNIT:
       *info = iora_fp->iunit;
       break;
   case IORA_INFO_TYPE:
       *info = iora_fp->type;
       break;
   case IORA_INFO_OPEN_OPTION:
       *info = iora_fp->open_option;
       break;
   case IORA_INFO_CLOSE_OPTION:
       *info = iora_fp->close_option;
       break;
   case IORA_INFO_OPENED:
       *info = iora_fp->opened;
       break;
   case IORA_INFO_CHANGED:
       *info = iora_fp->changed;
       break;
   case IORA_INFO_READ_ONLY:
       *info = iora_fp->read_only;
       break;
   case IORA_INFO_DATA_TYPE:
       *info = iora_fp->data_type;
       break;
   case IORA_INFO_SWAP4:
       *info = (iora_fp->swap4 == do_swap4);
       break;
   case IORA_INFO_SWAP2:
       *info = (iora_fp->swap2 == do_swap2);
       break;
   case IORA_INFO_STD_LONG:
       *info = (int)iora_fp->header->std_long;
       break;
   case IORA_INFO_NPARS:
       *info = iora_fp->header->npars;
       break;
   case IORA_INFO_SIZE:
       *info = iora_fp->header->size;
       break;
   case IORA_INFO_MAXREC:
       *info = iora_fp->header->maxrec;
       break;
   case IORA_INFO_NRECS:
       *info = iora_fp->header->nrecs;
       break;
   case IORA_INFO_NSKIP:
       *info = iora_fp->header->nskip;
       break;
   case IORA_INFO_MAPSIZE:
       *info = iora_fp->header->mapsize;
       break;
   case IORA_INFO_NMAPS:
       *info = iora_fp->header->nmaps;
       break;
   case IORA_INFO_HTYPE:
       *info = iora_fp->header->type;
       break;
   default:
       return IORA_ERROR;
   }
   return(OK);
}


int iora_test (int filno) {
/*   long i, ii;*/
   int i, ii;
   int ierr=0;
   int type, size;
   float buff[10];
   IORA_FILE   *iora_fp;
   IORA_HEADER *iora_header;
   FILE_PTR    *map, *ptr;
   
   iora_fp = iora_status.iora_fp[filno];

   printf ("\niora_test: Analysis of iora files.\n");
  /*
   *
   */
   if (iora_fp == NULL) {
      printf ("File not open: filno = %d\n", filno);
      return IORA_ERROR;
   }
   printf ("\nIORA_FILE data structure.\n");
   printf ("       version: %s\n", iora_fp->version) ;
   printf ("         filno: %d\n", iora_fp->filno);
   printf ("         iunit: %d\n", iora_fp->iunit);
   printf ("          type: %d\n", iora_fp->type);
   switch (iora_fp->open_option) {
   case OPEN_NEW:
      printf ("   open_option: %d (%s)\n", iora_fp->open_option, "OPEN_NEW");
      break;
   case OPEN_RW:
      printf ("   open_option: %d (%s)\n", iora_fp->open_option, "OPEN_RW");
      break;
   case OPEN_R:
      printf ("   open_option: %d (%s)\n", iora_fp->open_option, "OPEN_R");
      break;
   default:
      printf ("   open_option: %d (%s)\n", iora_fp->open_option, "Unkown");
      break;
   }
   switch (iora_fp->close_option) {
   case CLOSE_KEEP:
      printf ("  close_option: %d (%s)\n", iora_fp->open_option, "CLOSE_KEEP");
      break;
   case CLOSE_DEL:
      printf ("   lose_option: %d (%s)\n", iora_fp->open_option, "CLOSE_DEL");
      break;
   default:
      printf ("   close_option: %d (%s)\n", iora_fp->open_option, "Unkown");
      break;
   }
   printf ("      filename: %s\n", iora_fp->filename);
   printf ("        opened: %d\n", iora_fp->opened);
   printf ("       changed: %d\n", iora_fp->changed);
   printf ("     read_only: %d\n", iora_fp->read_only);
   printf ("     data_type: %d\n", iora_fp->data_type);
   printf ("            fd: %d\n", iora_fp->fd);
   printf ("           map: %d\n", (int)iora_fp->map);
   printf ("        header: %d\n", (int)iora_fp->header);
   printf ("    old header: %d\n", (int)iora_fp->old_header);
   printf ("      specific: %d\n", (int)iora_fp->specific);
   if (iora_fp->swap4 == do_swap4)
      printf ("         swap4: %d (%s)\n", (int)iora_fp->swap4, "do_swap4");
   else if (iora_fp->swap4 == dummy_swap)
      printf ("         swap4: %d (%s)\n", (int)iora_fp->swap4, "dummy_swap");
   else
      printf ("         swap4: %d (%s)\n", (int)iora_fp->swap4, "Unknown");
   if (iora_fp->swap2 == do_swap2)
      printf ("         swap2: %d (%s)\n", (int)iora_fp->swap2, "do_swap2");
   else if (iora_fp->swap2 == dummy_swap)
      printf ("         swap2: %d (%s)\n", (int)iora_fp->swap2, "dummy_swap");
   else
      printf ("         swap2: %d (%s)\n", (int)iora_fp->swap2, "Unknown");

   iora_header = iora_fp->header;
   if (iora_header == NULL) {
      printf ("No file header found (Strange): filno = %d\n", filno);
      return IORA_ERROR;
   }
   printf ("\nIORA_HEADER data structure.\n");
   printf ("       magical: %s\n", iora_header->magical);
   printf ("      std_long: %d\n", (int)iora_header->std_long);
   printf ("         npars: %d\n", iora_header->npars);
   printf ("          size: %d\n", iora_header->size);
   printf ("        maxrec: %d\n", iora_header->maxrec);
   printf ("         nrecs: %d\n", iora_header->nrecs);
   printf ("         nskip: %d\n", iora_header->nskip);
   printf ("       mapsize: %d\n", iora_header->mapsize);
   printf ("         nmaps: %d\n", iora_header->nmaps);
   switch (iora_header->type) {
   case FIXED:
      printf ("          type: %d (%s)\n", iora_header->type, "FIXED");
      break;
   case OLD3D_DATA_NOCOMPRESS:
      printf ("          type: %d (%s)\n", iora_header->type, "OLD3D_DATA_NOCOMPRESS");
      break;
   case OLD3D_DATA_COMPRESS_4:
      printf ("          type: %d (%s)\n", iora_header->type, "OLD3D_DATA_COMPRESS_4");
      break;
   case OLD3D_DATA_COMPRESS_2:
      printf ("          type: %d (%s)\n", iora_header->type, "OLD3D_DATA_COMPRESS_2");
      break;
   case OLD3D_DATA_NOCOMPRESS_2:
      printf ("          type: %d (%s)\n", iora_header->type, "OLD3D_DATA_NOCOMPRESS_2");
      break;
   case DATA_FLOAT_4 :
      printf ("          type: %d (%s)\n", iora_header->type, "DATA_FLOAT_4");
      break;
   case DATA_FLOAT_3 :
      printf ("          type: %d (%s)\n", iora_header->type, "DATA_FLOAT_3");
      break;
   case DATA_FLOAT_2 :
      printf ("          type: %d (%s)\n", iora_header->type, "DATA_FLOAT_2");
      break;
   case DATA_FLOAT_4_TRUNC :
      printf ("          type: %d (%s)\n", iora_header->type, "DATA_FLOAT_4_TRUNC");
      break;
   case DATA_FLOAT_3_TRUNC :
      printf ("          type: %d (%s)\n", iora_header->type, "DATA_FLOAT_3_TRUNC");
      break;
   case DATA_FLOAT_2_TRUNC :
      printf ("          type: %d (%s)\n", iora_header->type, "DATA_FLOAT_2_TRUNC");
      break;
   case DATA_CHAR:
      printf ("          type: %d (%s)\n", iora_header->type, "DATA_CHAR");
      break;
   case DATA_SHORT:
      printf ("          type: %d (%s)\n", iora_header->type, "DATA_SHORT");
      break;
   case DATA_LONG:
      printf ("          type: %d (%s)\n", iora_header->type, "DATA_LONG");
      break;
   default:
      printf ("          type: %d (%s)\n", iora_header->type, "Unknown");
      break;
   }
   printf ("    tresh_high: %f\n", iora_header->tresh_high);
   printf ("   tresh_level: %f\n", iora_header->tresh_level);
   printf ("     tresh_low: %f\n", iora_header->tresh_low);
   printf ("    rec_length: %d\n", iora_header->rec_length);
   printf ("       creator: %s\n", iora_header->creator);
   printf ("       content: %s\n", iora_header->content);
   printf ("       version: %s\n", iora_header->version);
   printf ("      reserved: %s (should be empty)\n", iora_header->reserved);

   map = iora_fp->map;
   if (map == NULL) {
      printf ("No record mapping found (Strange): filno = %d\n", filno);
      return IORA_ERROR;
   }
   printf ("\nFILE_PTR (map) data structure.\n");
   ptr = map;
   for (ii=0; ii<5; ii++)
      buff[ii] = 0.0;
      
   for (i=0; i<(MIN(7,iora_header->nrecs));i++) {
      size = 5;
      ierr = iora_read(filno, i, (char*)buff, &size, &type);
      if( ierr==IORA_ERROR) {
         printf ("Read error: filno: %d, rec_no: %d\n", filno,(int)i);
         break;
      }
      printf (" (%d)    offset: %d\n", (int)i, (int)ptr->offset);
      printf ("          type: %d\n", (int)ptr->type);
      printf ("          size: %d\n", (int)ptr->size);
      printf ("    data[0..5]:");
      switch (type) {
      case DATA_CHAR:
         
         printf("string length: %d, content: %s\n",size,(char*)buff);
         break;
         
      case OLD3D_STRUCTURE:
      case OLD3D_PARAMETER:
      case OLD3D_BRUKER:
      case OLD3D_HISTORY:
      case ND_STRUCTURE:
      case ND_PARAMETER:
      case ND_BRUKER:
      case ND_HISTORY:
      case DATA_FLOAT_2:
      case OLD3D_DATA_NOCOMPRESS_2:
      case DATA_FLOAT_3:
      case DATA_FLOAT_4:
      case OLD3D_DATA_NOCOMPRESS:
      case DATA_FLOAT_2_TRUNC:
      case OLD3D_DATA_COMPRESS_2:
      case DATA_FLOAT_3_TRUNC:
      case DATA_FLOAT_4_TRUNC:
      case OLD3D_DATA_COMPRESS_4:
         for (ii=0; ii<4; ii++)
            printf ("%.6e ", buff[ii]);
         printf ("\n");
         break;
      default:
         printf ("\n");
         break;
      }
      memset(buff, 0x00,size);
      ptr++;
   }
   if (ierr)
      return (IORA_ERROR);
   else
      return(OK);
}

int iora_init() {
/*
 *  iora_init  
 *  do not initialise twice: test for this 
 */
   if (iora_status.iora_fp == NULL) {

    /*
     * allocate array of IORA_FILE pointers 
     */
     iora_status.iora_fp   = (IORA_FILE**)calloc(MAX_IORA_FILES,sizeof(IORA_FILE*));

     if (iora_status.iora_fp == NULL) return MEM_ERROR;
   
    /*
     * allocation succesful
     */
     iora_status.max_files  = MAX_IORA_FILES;
     iora_status.open_files = 0;
     
    /* 
     * define the test float for the endian testing
     */
/*     std_long.i= 50462976;*/
     std_long.i= 66051;
    /*
     * on big endian this corresponds to:
     * std_long.byte[0]=0x00;
     * std_long.byte[1]=0x01;
     * std_long.byte[2]=0x02;
     * std_long.byte[3]=0x03;
     * so it is an ideal test to find which byte lives where.
     */
  }
   return (os_endian_test());
}

int iora_new_filno(void) {
/*
 * This routine is to find the first empty slot in the iora_fp array    
 * It returns the filno of the first empty slot or it returns IORA_ERROR 
 */
   IORA_FILE **ptr;
   int filno=0;
  /*
   * first check whether there are still free slots 
   */
   if (iora_status.max_files == iora_status.open_files) return IORA_ERROR;
   
   filno = 0;
   ptr   = iora_status.iora_fp;
   while ( (ptr[filno] != NULL) && (filno < MAX_IORA_FILES) ) {
      filno++;
   }
   if (filno < MAX_IORA_FILES) 
      return filno;
   else
      return IORA_ERROR;  
}

int iora_set_f77unit(int filno, int iunit) {
/*
 * tool to add the f77 unit number to IORA_FILE 
 */
   IORA_FILE *iora_fp;
   
   iora_fp = iora_status.iora_fp[filno];
   if (iora_fp == NULL) return IORA_ERROR;
   
   iora_fp->iunit = iunit;
   return(OK);
 }

int iora_get_filno(char *filename) {
/*
 * This routine is to find the filno in the iora_fp array used by the 
 * filename given as argument.                                         
 * It returns this filno or it returns IORA_ERROR if not found         
 */

   IORA_FILE **ptr;
   int filno=0;

  /*
   * first check whether there are still free slots 
   */
   if (iora_status.max_files == iora_status.open_files) return IORA_ERROR;

   ptr    = iora_status.iora_fp;
/*   printf ("%s: pointer at %ld (filno: %d)\n",filename,&(ptr[filno]),filno);*/

   while ( (ptr ==NULL || 
            ptr[filno] == NULL ||
            ptr[filno]->filename == NULL || 
            strcmp(ptr[filno]->filename , filename) != 0 ) && 
           (filno < MAX_IORA_FILES-1) )   {
       filno++;
/*       printf ("%s: pointer at %ld (filno: %d)\n",filename,&(ptr[filno]),filno);*/
   }
     
return IORA_ERROR;
/*
   if (filno < MAX_IORA_FILES-1) 
      return filno;
   else
      return IORA_ERROR;  
      */
}

IORA_FILE *iora_alloc_file(int filno) {
/*
 * this routine allocates the IORA_FILE block for the file with index 'filno'
 * in case of an error 'NULL' will be returned                               
 */
   IORA_FILE *ptr;
   
   if ( (ptr=(IORA_FILE*)calloc(1,sizeof(IORA_FILE))) == NULL) return NULL;
   
   ptr->filno         = filno;
   iora_status.iora_fp[filno] = ptr;
   return ptr;
}

void iora_dealloc_file(int filno) {
/*
 * this routine allocates the IORA_FILE block for the file with index 'filno'
 * in case of an error 'NULL' will be returned                               
 */
   CHECK_FREE(iora_status.iora_fp[filno]);
}

/*
 * iora_read_header
 * Read the file header and the record_map information from disk
 */
int iora_read_header(int filno) {
   int nbytes, ierr =0, i, ii;
   long nextmap;
   OLD_FILE_PTR *old_ptr;
   IORA_FILE    *iora_fp;
   IORA_HEADER  *iora_header;
   
   iora_fp = iora_status.iora_fp[filno];
   iora_header = iora_fp->header;
   iora_fp->type = IORA_TYPE_UNKNOWN;

   if ((ierr=read(iora_fp->fd, iora_header, sizeof(IORA_HEADER))) < 0) {
      onerror(13, 0, filno, iora_fp->filename, errno);
      return (IORA_ERROR);
   }

   /*
    * test for file identity with MAGICAL
    */
   if (strcmp(iora_header->magical, MAGICAL_STRING) != 0) 
      /*
       * oups. This is either no NMR file at all, or it is an 'old' one
       */
      if (iora_read_old3d_header(filno) != OK) 
         return (IORA_ERROR);
    
   /*
    * test for endianness and (if needed) swap the header components
    */
   if (iora_header->std_long == std_long.i) 
      /*
       * OS is of same endian as the file. good.
       */
      iora_set_endian(filno, IORA_OS_ENDIAN);

   else {
      /*
       * OS has is of different endian as the file -> swap
       */
      printf ("File: different endian\n");
      iora_set_endian(filno, IORA_CHANGE_ENDIAN);
      iora_swap4_header(filno);
   }
   if (iora_header->maxrec == 0) {
      printf ("NB: maxrec == 0; maxrec = 5 !!!!\n");
      iora_header->maxrec = 5;
   }

   /*
    * Allocate the memory for the recordmap structure; 
    * fill the recordmap structure with appropriate info.
    * for existing files the recordmap must be read from the file.
    */

   /*
    * Now we know how large the RecordMap of this file is
    * So allocate the memory and set it -1 (0xff)
    */
   iora_fp->map = (FILE_PTR*)malloc ((iora_header->maxrec) * sizeof(FILE_PTR));
   if (iora_fp->map == NULL) return IORA_ERROR;
   memset (iora_fp->map, 0xff, ((iora_header->maxrec) * sizeof(FILE_PTR)) );

   old_ptr = (OLD_FILE_PTR*)malloc ((iora_header->maxrec) * sizeof(OLD_FILE_PTR));
   if (old_ptr == NULL) return IORA_ERROR;
   memset (old_ptr, 0xff, ((iora_header->maxrec) * sizeof(OLD_FILE_PTR)) );



   if (iora_header->nmaps > 0) {

      nextmap = LTELL(filno);

     /*
      * Read in the (fragmented?) RecordMap.
      */
      for (i=0; i< iora_header->nmaps; i++) {
         list1_append(filno, nextmap);
         nbytes = sizeof(int);

        /*
         * Read in the offset of the next (if any) RecordMap block.
         */
         if ((ierr=read(iora_fp->fd, &nextmap, nbytes)) < 0) {
            onerror(14, 0, filno, iora_fp->filename, errno);
            return (IORA_ERROR);
         }
         iora_fp->swap4(&nextmap);

        /*
         * Read the RecordMap block
         */
         if (iora_fp->type == IORA_OLD3D) {
            
            printf ("old3d: read map differently %d/%d\n",i, iora_header->nmaps);
            nbytes = iora_header->mapsize * sizeof(OLD_FILE_PTR);
               
           /*
            * Skip the old dummy record 0 entry
            */
            if (i==0)
               LSEEK(filno,( LTELL(filno) + sizeof(OLD_FILE_PTR) ) );
               
            if ((ierr=read(iora_fp->fd, &old_ptr[iora_header->mapsize*i], nbytes)) < 0) {
               onerror(15, 0, filno, iora_fp->filename, errno);
               return (IORA_ERROR);
               }
         }
         else {
            nbytes = iora_header->mapsize * sizeof(FILE_PTR);
            if ((ierr=read(iora_fp->fd, &iora_fp->map[iora_header->mapsize*i], nbytes)) < 0) {
               onerror(16, 0, filno, iora_fp->filename, errno);
               return (IORA_ERROR);
            }
         }
            
         LSEEK(filno, nextmap);
      }

      if (iora_fp->type == IORA_OLD3D) {
         for (ii=0; ii<iora_header->maxrec; ii++){
            iora_fp->map[ii].offset = old_ptr[ii].offset;
            iora_fp->swap4 (&iora_fp->map[ii].offset);
            switch (ii) {	/* The first 4 records are fixed length records */
            case 0:
               iora_fp->map[ii].type   = OLD3D_STRUCTURE;
               iora_fp->map[ii].size   = iora_header->rec_length;
               iora_fp->map[ii].offset = ii*iora_header->rec_length;
               break;
            case 1:
               iora_fp->map[ii].type   = OLD3D_PARAMETER;
               iora_fp->map[ii].size   = iora_header->rec_length;
               iora_fp->map[ii].offset = ii*iora_header->rec_length;
               break;
            case 2:
               iora_fp->map[ii].type   = OLD3D_BRUKER;
               iora_fp->map[ii].size   = iora_header->rec_length;
               iora_fp->map[ii].offset = ii*iora_header->rec_length;
               break;
            case 3:
               iora_fp->map[ii].type   = OLD3D_HISTORY;
               iora_fp->map[ii].size   = iora_header->rec_length;
               iora_fp->map[ii].offset = ii*iora_header->rec_length;
               break;
            default:
               iora_fp->map[ii].type   = iora_header->type;
               break;
            }
            iora_fp->swap4 (&iora_fp->map[ii].type);
            iora_fp->swap4 (&iora_fp->map[ii].size);
            iora_fp->swap4 (&iora_fp->map[ii].offset);
         }
      }
      iora_swap4(filno, iora_fp->map,iora_header->maxrec*sizeof(FILE_PTR)/sizeof(float));
   }
   else {		/* if !(iora_header->nmaps > 0) */
     /*
      *  nmaps=0; The data records have a fixed size, so the start offset
      *  off each record can be calculated. (nskip+rec_len*rec_no)
      *  This method is not used anymore. We keep track of each record:
      *  size, content-type and location. Let's simulate this!
      * NB Actually a mistake is made here when a 'fixed record' (e.g. HIST) is longer than
      * the physical record length. C.f. 'nskip' in iora_read_old3d_header!!!!
      */
      if (iora_fp->type == IORA_OLD3D) {
         iora_header->nrecs  = iora_header->maxrec;
         for (ii=0; ii<iora_header->maxrec; ii++){
            switch(ii) {
            case 0:
               iora_fp->map[ii].type   = OLD3D_STRUCTURE;
               iora_fp->map[ii].size   = iora_header->rec_length;
               iora_fp->map[ii].offset = ii*iora_header->rec_length;
               break;
            case 1:
               iora_fp->map[ii].type   = OLD3D_PARAMETER;
               iora_fp->map[ii].size   = iora_header->rec_length;
               iora_fp->map[ii].offset = ii*iora_header->rec_length;
               break;
            case 2:
               iora_fp->map[ii].type   = OLD3D_BRUKER;
               iora_fp->map[ii].size   = iora_header->rec_length;
               iora_fp->map[ii].offset = ii*iora_header->rec_length;
               break;
            case 3:
               iora_fp->map[ii].type   = OLD3D_HISTORY;
               iora_fp->map[ii].size   = iora_header->rec_length;
               iora_fp->map[ii].offset = ii*iora_header->rec_length;
               break;
            default:
               iora_fp->map[ii].type   = iora_header->type;
               if (iora_header->type == OLD3D_DATA_NOCOMPRESS_2)
                  iora_fp->map[ii].size   = iora_header->rec_length/2;
               else 
                  iora_fp->map[ii].size   = iora_header->rec_length;

               iora_fp->map[ii].offset = iora_header->nskip + 
                             iora_fp->map[ii].size * (ii-(iora_header->nskip/iora_header->rec_length));
               break;
            }
         }
      }
   }
   return (0);
} 

int iora_read_old3d_header(int filno) {
   int i, rlen, ierr=0, nskip;
   char         *ptr;
   float        *float_ptr;
   IORA_FILE    *iora_fp;
   IORA_HEADER  *iora_header;
   
   iora_fp       = iora_status.iora_fp[filno];
   iora_header   = iora_fp->header;
   iora_fp->type = IORA_TYPE_UNKNOWN;

   /*
    * Lets test whether we can do anthing with this header:
    */
   float_ptr = (float*) iora_header;
   if (float_ptr[0] == -1.0 && (float_ptr[4]==40.0 |float_ptr[4]==80.0)) {
      iora_fp->type = IORA_OLD3D;
      iora_set_endian(filno, IORA_OS_ENDIAN);
   }
   else {
      iora_set_endian(filno, IORA_CHANGE_ENDIAN);
      iora_swap4(filno, float_ptr, sizeof(IORA_HEADER)/sizeof(long));
      for (i=0;  i<6; i++) 
      if (float_ptr[0] == -1.0 && (float_ptr[4]==40.0 |float_ptr[4]==80.0)) 
         iora_fp->type = IORA_OLD3D;
   }
   
   if (iora_fp->type != IORA_OLD3D) {
      iora_close(filno);
      printf ("No old3d 3D NMR file. I give up\n");  
      printf ("Could be a f77 style old3d file though...(NIY)\n");  
      return (IORA_ERROR);
   } 
   else {
      /* 
       * Old 2D NMR stuff too buggy for writing!!!!
       */
      iora_fp->read_only    = TRUE;
      printf ("Old-style 3D NMR file\n"); 
      /*
       *  old3d record_size                -1*float_ptr[2]
       *  old3d data record length  float_ptr[24]
       *  old3d key of first data record   float_ptr[25]
       *  old3d key of history  record   float_ptr[20]
       * typical structure of these file:
       * first 4 records with fixed length (size = -1*float_ptr[2])
       * then the 'IOPAR_T data structure', which resembles the current file header
       * then the RecordMap, the array with filepointers to records and
       * finally the data itself.
       */
      rlen   =  sizeof(float) * (int)((-1)*float_ptr[2]);
      nskip  = 0;
      nskip += rlen * (1+ (int)(float_ptr[ 4]*sizeof(float)-1)/rlen);	/* str */
      nskip += rlen * (1+ (int)(float_ptr[ 9]*sizeof(float)-1)/rlen);	/* par */
      nskip += rlen * (1+ (int)(float_ptr[14]*sizeof(float)-1)/rlen);	/* brk */
      nskip += rlen * (1+ (int)(float_ptr[19]*sizeof(float)-1)/rlen);	/* his */

      /*
       * Read the old file header (from 0 to nskip) and (if needed) swap it.
       * Or should we store it as record 0. This record number was not used in  
       * iora version 1.
       */
      REWIND(filno);
      iora_fp->old_header = malloc (rlen);
      if ((ierr=read(iora_fp->fd, iora_fp->old_header, rlen)) < 0) {
         onerror(17, 0, filno, iora_fp->filename, errno);
         return (IORA_ERROR);
      }
      iora_swap4(filno, iora_fp->old_header, rlen/sizeof(float));
      
      /*
       * Now read the old IOPAR_T stuff.
       */
      LSEEK(filno, nskip);
      nskip += sizeof(int) + IORA1_NPARS*sizeof(int);
      ptr    = (char*)iora_header;
      ptr   += MAGICAL_STRING_LENGTH;
      if ((ierr=read(iora_fp->fd, ptr, sizeof(float)+IORA1_HEADER_SIZE)) < 0) {
         onerror(18, 0, filno, iora_fp->filename, errno);
        return (IORA_ERROR);
      }
      
      /*
       * iora_header->std_long now contains an extra copy of the (old) header size\
       * We overwrite this with the std_long for our system and swap/noswap it as we
       * just determined. 
       */
       
      iora_header->std_long = std_long.i;
      iora_fp->swap4(&iora_header->std_long);
      /*
       * Definition of nskip has changed: Used to be nr of fixed length records
       * skiped. Not it is the number of bytes skiped. So modify accordingly:
       */
      iora_header->nskip      = LTELL(filno);
      iora_fp->swap4(&iora_header->nskip);
      iora_header->rec_length = rlen;
      iora_fp->swap4(&iora_header->rec_length);
      
      /* 
       * Fill the additional parameters.
       * From here on we can treat the header as being correctly read in
       */
      sprintf (iora_header->magical, MAGICAL_STRING_1);
      sprintf (iora_header->creator, CREATOR_STRING_1);
      sprintf (iora_header->content, CONTENT_STRING_1);
      sprintf (iora_header->version, VERSION_STRING_1);
   }
   return (OK);
}

int os_endian_test() {
   byte4_union test;
   int ierr;

   switch (std_long.byte[3]) {
   case 3: /* supposedly big endian IEEE*/
/*      printf("OS big endian IEEE\n");*/
      std_e0   = 0;
      std_m0   = 1;
      std_m1   = 2;
      std_m2   = 3;
      std_dbias=0x00;
      ierr     = IORA_BIG_ENDIAN;
      break;
   case 2: /* supposedly VAX*/
/*      printf("OS VAX\n");*/
      std_e0   = 1;
      std_m0   = 0;
      std_m1   = 3;
      std_m2   = 2;
      std_dbias=0x01;
      ierr     = IORA_VAX_ENDIAN;
      break;
   case 1: /* supposedly swaped VAX (does this exist?)*/
/*      printf("OS swaped VAX (does this exist?)\n");*/
      std_e0   = 2;
      std_m0   = 3;
      std_m1   = 0;
      std_m2   = 1;
      std_dbias=0x01;
      ierr     = IORA_LITTLE_VAX_ENDIAN;
      break;
   case 0: /* supposedly little endian IEEE*/
/*      printf("OS little endian IEEE\n");*/
      std_e0   = 3;
      std_m0   = 2;
      std_m1   = 1;
      std_m2   = 0;
      std_dbias=0x00;
      ierr     = IORA_LITTLE_ENDIAN;
      break;
   }
   test.byte[std_e0]=0x00;
   test.byte[std_m0]=0x01;
   test.byte[std_m1]=0x02;
   test.byte[std_m2]=0x03;
   if (std_long.i != test.i) {
     ierr = IORA_ERROR;
     printf ("\n os_endian_test failed.\n");
   }
   
   return (ierr);
}


/*
 * iora_write_header
 * Write the file header and the record_map information from memory to disk
 */
int iora_write_header(int filno) {
   int nbytes, ierr =0, start;
   int tmp_size;
   long cur_offset, start_offset;
   NODE1_T *temp;
   IORA_FILE   *iora_fp;
   IORA_HEADER *iora_header;
   
   iora_fp     = iora_status.iora_fp[filno];
   iora_header = iora_fp->header;

   if (iora_fp->read_only == TRUE) {
      printf ("\niora_write_header: File is opened 'read-only'\n");
      return (IORA_ERROR);
   }

   cur_offset = LTELL(filno);		        /* Save current position */

   start_offset = iora_header->nskip;
   LSEEK(filno, start_offset);		/* set fileptr */

   /*
    * Write the header. First convert to endian type of the file
    * This may be a dummy swap or a real swap! After the write we must convert
    * it back to CPU-endianness.
    */
   tmp_size  = iora_header->size;
   iora_swap4_header(filno);
   if (write(iora_fp->fd, iora_header, tmp_size) < 0) {
      onerror(19, tmp_size, filno, iora_fp->filename, errno);
      return (1);
   }
   iora_swap4_header(filno);

   /*
    * If there is no RecordMap to write,  don't write one
    */
   if (iora_header->nmaps > 0) {
      temp  = head[filno]->next;
      /*
       * If this file is opened new the list of pointers is not created yet.
       * Therefor we ask for the current file position and add this to the list
       */
      if (temp->next == temp)
	 list1_append(filno, LTELL(filno));

      start = 0;
      while (temp->next != temp) {
	 LSEEK(filno, temp->offset);
	 nbytes = sizeof(long);
         iora_fp->swap4(&(temp->next->offset));
         if (write(iora_fp->fd, &(temp->next->offset), nbytes) < 0) {
            onerror(20, nbytes, filno, iora_fp->filename, errno);
            iora_fp->swap4(&(temp->next->offset));
            return (1);
         }
         iora_fp->swap4(&(temp->next->offset));

	 nbytes = iora_header->mapsize * sizeof(FILE_PTR);
         iora_swap4(filno, &iora_fp->map[start], nbytes/sizeof(long));
         if (write(iora_fp->fd, &iora_fp->map[start], nbytes) < 0) {
            onerror(21, nbytes, filno, iora_fp->filename, errno);
            iora_swap4(filno, &iora_fp->map[start], nbytes/sizeof(long));
            return (1);
         }
         iora_swap4(filno, &iora_fp->map[start], nbytes/sizeof(long));

	 start += iora_header->mapsize;
	 temp  = temp->next;
      }
   }

   LSEEK(filno, cur_offset);	/* reset to old position*/
   if (ierr)
      return (IORA_ERROR);
   else
      return(OK);
}


int cskip(int filno,int size) {
/*
 * cskip low level skip call. This is as low as we go
 * This funtion moves the file pointer for comming reads/writes
 * Is this one still needed now we removed the old 'fixed' records????
 */
   int ierr=0;
   IORA_FILE *iora_fp;
   
   iora_fp = iora_status.iora_fp[filno];
   if (lseek(iora_fp->fd,size,SEEK_CUR) < 0) {
      onerror(22, 0, filno, iora_fp->filename, errno);
      ierr = 1;
   }
   if (ierr)
      return (IORA_ERROR);
   else
      return(OK);
}

int iora_set_rectype(int filno, int rectype, float tresh_high, float tresh_level, float tresh_low) {
   IORA_FILE    *iora_fp;
   IORA_HEADER  *iora_header;
   
   if (filno < 0 || filno > MAX_IORA_FILES) {
      printf ("iora_set_rectype: file number %d out of valid range [0..MAX_IORA_FILES]\n", filno);
      return(0);    /* file was not opened in the first place */
   }
   iora_fp     = iora_status.iora_fp[filno];
   if (iora_fp == NULL) {
      printf ("iora_set_rectype: file %d was not opended.", filno);
      return(0);    /* file was not opened in the first place */
   }

   iora_fp = iora_status.iora_fp[filno];
   iora_header = iora_fp->header;

   /* 
    * The record type (and file type) can only be changed if no record
    * has been written yet and if the file is open.
    */
   if (iora_header->nrecs > 4) {
      printf ("iora_set_rectype: Can only define rectype before first write: " 
               "nrecs now = %d \n",iora_header->nrecs);
      return(iora_header->type);
   }
   else {
      switch (rectype) {
      case FIXED:	 /* Simulate an old ftn type file does this still work? BL*/
	 iora_header->type = FIXED;
	 iora_header->nmaps = 0;	/* especially nmaps=0 ? */
	 iora_header->tresh_high  = -1.0;
	 iora_header->tresh_level =  0.0;
	 iora_header->tresh_low   =  1.0;
	 break;
      case OLD3D_DATA_NOCOMPRESS:
      case OLD3D_DATA_NOCOMPRESS_2:
      case DATA_FLOAT_4:
      case DATA_FLOAT_3:
      case DATA_FLOAT_2:
      case DATA_CHAR:
      case DATA_LONG:
      case DATA_SHORT:
	 iora_header->type = rectype;
	 iora_header->tresh_high  = -1.0;
	 iora_header->tresh_level =  0.0;
	 iora_header->tresh_low   =  1.0;
	 break;
      case OLD3D_DATA_COMPRESS_4:
      case OLD3D_DATA_COMPRESS_2:
      case DATA_FLOAT_4_TRUNC :
      case DATA_FLOAT_3_TRUNC :
      case DATA_FLOAT_2_TRUNC :
	 iora_header->type = rectype;
	 iora_header->nmaps = 1;
	 iora_header->tresh_high  = tresh_high;
	 iora_header->tresh_level = tresh_level;
	 iora_header->tresh_low   = tresh_low;
	 break;
      default:
	 iora_header->type = DATA_FLOAT_4;
	 iora_header->tresh_high  = -1.0;
	 iora_header->tresh_level =  0.0;
	 iora_header->tresh_low   =  1.0;
	 break;
      }
   }
   return(iora_header->type);
}

int iora_get_rectype(int filno, float *tresh_high, float *tresh_level, float *tresh_low) {
   IORA_FILE    *iora_fp;
   IORA_HEADER  *iora_header;
   
   if (filno < 0 || filno > MAX_IORA_FILES) {
      printf ("iora_get_tresh: file number %d out of valid range [0..MAX_IORA_FILES]\n", filno);
      return(DATA_UNKNOWN);    /* file was not opened in the first place */
   }
   iora_fp     = iora_status.iora_fp[filno];
   if (iora_fp == NULL) {
      printf ("iora_get_tresh: file %d was not opended.", filno);
      return(DATA_UNKNOWN);    /* file was not opened in the first place */
   }

   iora_fp = iora_status.iora_fp[filno];
   iora_header = iora_fp->header;

   *tresh_high  = iora_header->tresh_high;
   *tresh_level = iora_header->tresh_level;
   *tresh_low   = iora_header->tresh_low;
   return (iora_header->type);
}

int f4_cmprs (int filno, float *f, int *nfl, char *vect, int *nbts, float *thi, float *tlo) {
   float *f1, *f2;
   char  *v1;
   char  count;

   f1 = f;
   f2 = f1;
   v1 = vect;

   if (*nbts < 1)
      return (1);

   if (*nfl < 1)
      return (1);

   while ( RANGE_OK(f, f2, *nfl) ) {
      if (TRESH_OK(f2, thi, tlo)) {
	 f1 = f2;
	 while (TRESH_OK(f2, thi, tlo) && 
	     RANGE_OK(f1, f2, MAX_WORDS) ) f2++;

	 if (!RANGE_OK(f, f2, *nfl))
	    f2 = f + (*nfl);
	 count = (char) (f2-f1);
	 if (!RANGE_OK(vect, (v1+count*sizeof(float)+1), *nbts) )
	    return (1);
	 *v1++ = count;
	 memcpy(v1, f1, ((int)count)*sizeof(float) );
	 v1 += ((int)count)*sizeof(float);
      }
      else {
	 f1 = f2;
	 while (!TRESH_OK(f2, thi, tlo) && 
	     RANGE_OK(f1, f2, MAX_WORDS) ) f2++;

	 if (!RANGE_OK(f, f2, *nfl))
	    f2 = f + (*nfl);
	 count = (char) (f2-f1);
	 count *= (-1);
	 if (!RANGE_OK(vect, (v1+1), *nbts) )
	    return (1);
	 *v1++ = count;
      }
   }
   *nbts = v1 - vect;
   return (0);
}


int f4_decmprs (int filno, char *vect, int *nbts, float *f, int *nfl, float *tle) {
   float *f2;
   char  *v1;
   char  count;
   int   maxout, i;

   maxout = *nfl;
   f2 = f;
   v1 = vect;

   if (*nbts < 1 || *nfl < 1)
      return (1);

   while ( RANGE_OK(vect, v1, *nbts) ) {
      count = *v1++;
      if (count&(1<<7)) {	 /* range of zeroes */
	 count *= (-1);		        /* unset 7th bit */
	 if (RANGE_OK(f, (f2+(int)count-1), maxout)) {  /* put one block of zero's */
	    for (i=0; i<(int)count; i++)
	       memcpy (f2++, (void *)tle, sizeof(float));
	 }
      }
      else {
	 if(RANGE_OK(f, (f2+(int)count-1), maxout)) {
	    memcpy(f2, v1, (int)count*sizeof(float) );
            iora_swap4(filno, f2, count);
	    f2 += (int)count;
	    v1 += (int)count*sizeof(float);
	 }
      }
   }
   *nfl = (f2 - f);
   return (0);
}

int f2_cmprs (int filno, float *f, int *nfl, char *vect, int *nbts, float *thi, float *tlo) {
   float *f1, *f2;
   float2 fl2;
   char  *v1;
   char  count;
   int i;

   f1 = f;
   f2 = f1;
   v1 = vect;

   if (*nbts < 1 || *nfl < 1)
      return (1);

   while ( RANGE_OK(f, f2, *nfl) ) {
      if (TRESH_OK(f2, thi, tlo)) {
	 f1 = f2;
	 while (TRESH_OK(f2, thi, tlo) && 
	     RANGE_OK(f1, f2, MAX_WORDS) ) f2++;

	 if (!RANGE_OK(f, f2, *nfl))
	    f2 = f + (*nfl);
	 count = (char) (f2-f1);
	 if (!RANGE_OK(vect, (v1+count*sizeof(float)+1), *nbts) )
	    return (1);
	 *v1++ = count;
	 for (i=0; i<(int)count; i++) {
	    float_float2(&f1[i], &fl2);
	    memcpy(v1, &fl2, sizeof(float2));
	    v1 += sizeof(float2);
	 }
      }
      else {
	 f1 = f2;
	 while (!TRESH_OK(f2, thi, tlo) && 
	     RANGE_OK(f1, f2, MAX_WORDS) ) f2++;

	 if (!RANGE_OK(f, f2, *nfl))
	    f2 = f + (*nfl);
	 count = (char) (f2-f1);
	 count *= (-1);
	 if (!RANGE_OK(vect, (v1+1), *nbts) )
	    return (1);
	 *v1++ = count;
      }
   }
   *nbts = v1 - vect;
   return (0);
}


int f2_decmprs (int filno, char *vect, int *nbts, float *f, int *nfl, float *tle) {
   float *f2;
   char  *v1;
   char  count;
   int   maxout, i;

   maxout = *nfl;
   f2 = f;
   v1 = vect;

   if (*nbts < 1 || *nfl < 1)
      return (1);

   while ( RANGE_OK(vect, v1, *nbts) ) {
      count = *v1++;
      if ( (count&(1<<7)) ) {	 /* range of zeroes */
	 count *= (-1);		        /* unset 7th bit */
	 if (RANGE_OK(f, (f2+(int)count-1), maxout)) {  /* put one block of zero's */
	    for (i=0; i<(int)count; i++)
	       memcpy (f2++, (void *)tle, sizeof(float));
	 }
      }
      else {
	 if(RANGE_OK(f, (f2+(int)count-1), maxout)) {
	    for (i=0; i<(int)count; i++) {
	       float2_float((float2*)v1, f2++);
	       v1 += sizeof(float2);
	    }
	 }
      }
   }
   *nfl = (f2 - f);
   return (0);
}

int f3_cmprs (int filno, float *f, int *nfl, char *vect, int *nbts, float *thi, float *tlo) {
   float *f1, *f2;
   float3 fl3;
   char  *v1;
   char  count;
   int i;

   f1 = f;
   f2 = f1;
   v1 = vect;

   if (*nbts < 1 || *nfl < 1)
      return (1);

   while ( RANGE_OK(f, f2, *nfl) ) {
      if (TRESH_OK(f2, thi, tlo)) {
	 f1 = f2;
	 while (TRESH_OK(f2, thi, tlo) && 
	     RANGE_OK(f1, f2, MAX_WORDS) ) f2++;

	 if (!RANGE_OK(f, f2, *nfl))
	    f2 = f + (*nfl);
	 count = (char) (f2-f1);
	 if (!RANGE_OK(vect, (v1+count*sizeof(float)+1), *nbts) )
	    return (1);
	 *v1++ = count;
	 for (i=0; i<(int)count; i++) {
	    float_float3(&f1[i], &fl3);
	    memcpy(v1, &fl3, sizeof(float3));
	    v1 += sizeof(float3);
	 }
      }
      else {
	 f1 = f2;
	 while (!TRESH_OK(f2, thi, tlo) && 
	     RANGE_OK(f1, f2, MAX_WORDS) ) f2++;

	 if (!RANGE_OK(f, f2, *nfl))
	    f2 = f + (*nfl);
	 count = (char) (f2-f1);
	 count *= (-1);
	 if (!RANGE_OK(vect, (v1+1), *nbts) )
	    return (1);
	 *v1++ = count;
      }
   }
   *nbts = v1 - vect;
   return (0);
}


int f3_decmprs (int filno, char *vect, int *nbts, float *f, int *nfl, float *tle) {
   float *f2;
   char  *v1;
   char  count;
   int   maxout, i;

   maxout = *nfl;
   f2 = f;
   v1 = vect;

   if (*nbts < 1 || *nfl < 1)
      return (1);

   while ( RANGE_OK(vect, v1, *nbts) ) {
      count = *v1++;
      if ( (count&(1<<7)) ) {	 /* range of zeroes */
	 count *= (-1);		        /* unset 7th bit */
	 if (RANGE_OK(f, (f2+(int)count-1), maxout)) {  /* put one block of zero's */
	    for (i=0; i<(int)count; i++)
	       memcpy (f2++, (void *)tle, sizeof(float));
	 }
      }
      else {
	 if(RANGE_OK(f, (f2+(int)count-1), maxout)) {
	    for (i=0; i<(int)count; i++) {
	       float3_float((float3*)v1, f2++);
	       v1 += sizeof(float3);
	    }
	 }
      }
   }
   *nfl = (f2 - f);
   return (0);
}

int f4_f2 (float *f, int *nfl, char *vect, int *nbts) {
   char  *v1;
   int i;
   float2 fl2;

   v1 = vect;

   if (*nbts < 1 ||
       *nfl  < 1 ||
       *nbts < *nfl * (int) sizeof(float2))
      return (1);

   for (i=0; i<(*nfl); i++) {
      float_float2(&f[i], &fl2);
      memcpy (v1, &fl2, sizeof(float2));
      v1 += sizeof(float2);
   }
   *nbts = v1 - vect;
   return (0);
}


int f2_f4 (char *vect, int *nbts, float *f, int *nfl) {
   float2 fl2;
   char  *v1;
   int   i;

   v1 = vect;

   if (*nbts  < 1 ||
       *nfl   < 1 ||
       *nfl * (int) sizeof(float2) > *nbts)
      return (1);

   *nfl = *nbts / sizeof(float2);
   
   for (i=0; i<(*nfl); i++) {
      fl2 = *(float2*)v1;
      float2_float(&fl2, &f[i]);
      v1 += sizeof(float2);
   }

   return (0);
}

int f4_f3 (float *f, int *nfl, char *vect, int *nbts) {
   char  *v1;
   int i;
   float3 fl3;

   v1 = vect;

   if (*nbts < 1 ||
       *nfl  < 1 ||
       *nbts < *nfl * (int) sizeof(float3))
      return (1);

   for (i=0; i<(*nfl); i++) {
      float_float3(&f[i], &fl3);
      memcpy (v1, &fl3, sizeof(float3));
      v1 += sizeof(float3);
   }
   *nbts = v1 - vect;
   return (0);
}


int f3_f4 (char *vect, int *nbts, float *f, int *nfl) {
   float3 fl3;
   char  *v1;
   int   i;

   v1 = vect;

   if (*nbts  < 1 ||
       *nfl   < 1 ||
       *nfl * (int) sizeof(float3) > *nbts)
      return (1);

   *nfl = *nbts / sizeof(float3);
   
   for (i=0; i<(*nfl); i++) {
      fl3 = *(float3*)v1;
      float3_float(&fl3, &f[i]);
      v1 += sizeof(float3);
   }

   return (0);
}

int float_float2(float *f, float2 *f2) {
   byte4_union x;

   x.f = *f;
   f2->exponent    = (unsigned char) (x.byte[std_e0]-std_dbias);
   f2->mantissa[0] = x.byte[std_m0];
   return 1;
}

int float_float3(float *f, float3 *f3) {
   byte4_union x;

   x.f = *f;
   f3->exponent    = (unsigned char) (x.byte[std_e0]-std_dbias);
   f3->mantissa[0] = x.byte[std_m0];
   f3->mantissa[1] = x.byte[std_m1];
   return 1;
}


int float_float4(float *f, float4 *f4) {
   byte4_union x;

   x.f = *f;
   f4->exponent    = (unsigned char) (x.byte[std_e0]-std_dbias);
   f4->mantissa[0] = x.byte[std_m0];
   f4->mantissa[1] = x.byte[std_m1];
   f4->mantissa[2] = x.byte[std_m2];
   return 1;
}


int float2_float(float2 *f2, float *f) {
   byte4_union x;
   
   x.i = 0;

   x.byte[std_m0] = f2->mantissa[0];
   x.byte[std_m1] = 0x80;       /* just guessing: Best on average */ 
   x.byte[std_e0] = (unsigned char) (std_dbias + f2->exponent);

   *f = x.f;
   return 1;
}

int float3_float(float3 *f3, float *f) {
   byte4_union x;
   
   x.i = 0;
   x.byte[std_m0] = f3->mantissa[0];
   x.byte[std_m1] = f3->mantissa[1];
   x.byte[std_m2] = 0x80;        /* just guessing: Best on average */
   x.byte[std_e0] =(unsigned char) (std_dbias + f3->exponent);
   *f = x.f;
   return 1;
}

int float4_float(float4 *f4, float *f) {
   byte4_union x;
   
   x.i = 0;
   x.byte[std_m0] = f4->mantissa[0];
   x.byte[std_m1] = f4->mantissa[1];
   x.byte[std_m2] = f4->mantissa[2];
   x.byte[std_e0] = (unsigned char) (std_dbias + f4->exponent);
   *f = x.f;
   return 1;
}

void tobe_swap4(void *i) {
   byte4_union x, y;

   memcpy(&x, i, sizeof(long));		/*	x.i = (int)*i	*/
   y.byte[0] = x.byte[std_e0];
   y.byte[1] = x.byte[std_m0];
   y.byte[2] = x.byte[std_m1];
   y.byte[3] = x.byte[std_m2];
   memcpy(i, &y, sizeof(long));		/*	(int)*i = y.i	*/
}

void tole_swap4(void *i) {
   byte4_union x, y;

   memcpy(&x, i, sizeof(long));		/*	x.i = (int)*i	*/
   y.byte[3] = x.byte[std_e0];
   y.byte[2] = x.byte[std_m0];
   y.byte[1] = x.byte[std_m1];
   y.byte[0] = x.byte[std_m2];
   memcpy(i, &y, sizeof(long));		/*	(int)*i = y.i	*/
}

#ifdef domme_onzin
void do_swap4(void *i) {
   byte4_union x, y;

   memcpy(&x, i, sizeof(long));		/*	x.i = (int)*i	*/
   y.byte[0] = x.byte[3];
   y.byte[1] = x.byte[2];
   y.byte[2] = x.byte[1];
   y.byte[3] = x.byte[0];
   memcpy(i, &y, sizeof(long));		/*	(int)*i = y.i	*/
}

void do_swap2(void *i) {
   byte4_union x, y;

   memcpy(&x, i, sizeof(short));		/*	x.i = (int)*i	*/
   y.byte[0] = x.byte[1];
   y.byte[1] = x.byte[0];
   memcpy(i, &y, sizeof(short));		/*	(int)*i = y.i	*/
}
#endif

#define DO_SWAP4(i) {\
   char *p = (char*) (i);\
   char tmp;\
   tmp = p[3];\
   p[3] = p[0];\
   p[0] = tmp;\
   tmp = p[2];\
   p[2] = p[1];\
   p[1] = tmp;\
}

void do_swap4(void *i) {
   char *p = (char*) i;
   char tmp;

   tmp = p[3];
   p[3] = p[0];
   p[0] = tmp;
   tmp = p[2];
   p[2] = p[1];
   p[1] = tmp;
}

void do_swap2(void *i) {
   char *p = (char*) i;
   char tmp;

   tmp = p[1];
   p[1] = p[0];
   p[0] = tmp;
}

void dummy_swap(void *i)
{
    ; /* or 'i = i;  */
}

void iora_swap4_header(int filno) {
   IORA_FILE *iora_fp;
   IORA_HEADER *iora_header;
   
   iora_fp     = iora_status.iora_fp[filno];
   iora_header = iora_fp->header;

   iora_fp->swap4 (&iora_header->std_long);
   iora_fp->swap4 (&iora_header->npars);
   iora_fp->swap4 (&iora_header->size);
   iora_fp->swap4 (&iora_header->maxrec);
   iora_fp->swap4 (&iora_header->nrecs);
   iora_fp->swap4 (&iora_header->nskip);
   iora_fp->swap4 (&iora_header->mapsize);
   iora_fp->swap4 (&iora_header->nmaps);
   iora_fp->swap4 (&iora_header->type);
   iora_fp->swap4 (&iora_header->tresh_high);
   iora_fp->swap4 (&iora_header->tresh_level);
   iora_fp->swap4 (&iora_header->tresh_low);
   iora_fp->swap4 (&iora_header->rec_length);
}

void iora_set_endian(int filno, char flag) {
   IORA_FILE *iora_fp;
   
   iora_fp     = iora_status.iora_fp[filno];

   switch (flag) {
   case IORA_BIG_ENDIAN:
      if (os_endian_test() == IORA_BIG_ENDIAN) {
         iora_fp->swap4 = dummy_swap;
         iora_fp->swap2 = dummy_swap;
      }
      else {
         iora_fp->swap4 = do_swap4;
         iora_fp->swap2 = do_swap2;
      }
      break;
   case IORA_LITTLE_ENDIAN:
      if (os_endian_test() == IORA_LITTLE_ENDIAN) {
         iora_fp->swap4 = dummy_swap;
         iora_fp->swap2 = dummy_swap;
      }
      else {
         iora_fp->swap4 = do_swap4;
         iora_fp->swap2 = do_swap2;
      }
      break;
   case IORA_CHANGE_ENDIAN:
      iora_fp->swap4    = do_swap4;
      iora_fp->swap2    = do_swap2;
      break;  
   case IORA_OS_ENDIAN:
      iora_fp->swap4    = dummy_swap;
      iora_fp->swap2    = dummy_swap;
      break;
   }
}

void iora_swap4(int filno, void *array, int size) {
   IORA_FILE *iora_fp;
   int        i, *j;
   
   iora_fp     = iora_status.iora_fp[filno];

   if (iora_fp->swap4 == dummy_swap) 
       return;
   j=array;    
   for (i=0; i<size; i++) {
        DO_SWAP4(j);
        j++;
/*
        iora_fp->swap4 (j++);
*/
   }
}

/*
 * Linked list and Red/Black tree functions 
 */
int list1_init(int filno) {
   head[filno] = (NODE1_T *)malloc(sizeof (NODE1_T) );
   tail[filno] = (NODE1_T *)malloc(sizeof (NODE1_T) );

   head[filno]->next   = tail[filno];
   tail[filno]->next   = tail[filno];
   head[filno]->offset = 0;
   tail[filno]->offset = 0;

   return (0);
}

int list1_append(int filno, long offset) {
   NODE1_T *new;
   new = tail[filno];
   tail[filno] = (NODE1_T *)malloc(sizeof (NODE1_T) );
   new->next   = tail[filno];
   new->offset = offset;
   tail[filno]->next = tail[filno];

   return (0);
}

int list1_free_all(int filno) {
   NODE1_T *temp1,  *temp2;

   if ((temp1 = head[filno]) == NULL)
      return (0);
   else
      temp1 = temp1->next;

   while (temp1->next != temp1) {
      temp2 = temp1->next;
      CHECK_FREE (temp1);
      temp1 = temp2;
   }
   CHECK_FREE ( head[filno] );
   CHECK_FREE ( tail[filno] );

   return (0);
}

