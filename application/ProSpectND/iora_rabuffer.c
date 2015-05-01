/* 
 * iora_rabuffer.c
 * rabuffer routines for use with iora.c
 * 
 * Author:  Bas R. Leeflang
 *	    Bio-Organic Chemistry department,  
 *          Bijvoet Center, 
 *          Utrecht University
 *          b.r.leeflang@chem.uu.nl
 * 
 * Callable functions:
 *	int iora_buffer_set_ondisk(int filno, int record_num, int flag)
 *	int iora_buffer_get_size(int filno, int record_num)
 *
 * Use:	    Interface to Fortran by routines in ioraf.f
 * Use:	    Interface to iond.c via iosmx.c
 *
 * Version:	230797	(BL)	start of project (based on iorac.c etc.)
 * 		010897	(BL)	initial working version (single endian)
 * 		190897	(BL)	initial working version (dual endian, float*4 + char)
 *                              No float*2 and truncation working!!!!!
 *              120298	(BL)	Continuation of project, bug fixes, code cleanup
 *          26 12 1998  (BL)	bug fix for old triton file with physical record-length < history rec.
 *
 */

#include "iora.h"

/*
 * rav	   Random Access   variables
 */
static char        buffer[MAX_BYTES];
static char       *ra_buffer   = NULL;  /* IO buffer for (compressed) data */
static int         nreads      = 0;
static int         nreadhits   = 0;
static int         nbuff_reset = 0;
static int         RaBufflen   = 0;
static int         RaBuffnext  = 0;
static int         RaBufffree  = 0;
static int         RaBufflast  = 0;
static RBnode_t   *RBhead[MAX_IORA_FILES];
static RBnode_t   *RBtail[MAX_IORA_FILES];
static RBnode_t   *RBx, *RBg, *RBp, *RBgg;

/***************************************************/

void rabuf_readcount(void) {
   nreads++;
}   

void rabuf_hitcount(void) {
   nreadhits++;
}   

int iora_buffer_set_ondisk(int filno, int record_num, int flag) {
   int        b_offset;
   buf_mark_t marker_block;
   
   b_offset = get_record_offset(filno, record_num);
   if (b_offset == NOT_IN_BUFFER)  return(IORA_ERROR);

   memcpy((&marker_block), (ra_buffer+b_offset),  sizeof(marker_block));
   marker_block.on_disk = flag;
   memcpy((ra_buffer+b_offset), (char*)(&marker_block), sizeof(marker_block));
   
   return(OK);
}

int iora_buffer_get_ondisk(int filno, int record_num) {
   int        b_offset;
   buf_mark_t marker_block;
   
   b_offset = get_record_offset(filno, record_num);
   if (b_offset == NOT_IN_BUFFER)  return(IORA_ERROR);

   memcpy((&marker_block), (ra_buffer+b_offset),  sizeof(marker_block));
   
   return(marker_block.on_disk);
}

int iora_buffer_get_size(int filno, int record_num) {
   int        b_offset;
   buf_mark_t marker_block;
   
   b_offset = get_record_offset(filno, record_num);
   if (b_offset == NOT_IN_BUFFER)  return(IORA_ERROR);

   memcpy((&marker_block), (ra_buffer+b_offset),  sizeof(marker_block));
   
   return(marker_block.size);
}

int iora_buffer_replace (int filno, int record_num, char *vect, int size) {
   int b_offset;
   int ierr;
   buf_mark_t marker_block;
   
   b_offset = get_record_offset(filno, record_num);
   if (b_offset == NOT_IN_BUFFER) return(IORA_ERROR);
   
   marker_block.size    = size;
   marker_block.rec     = record_num;
   marker_block.filno   = filno;
   marker_block.on_disk = FALSE;		
   memcpy((ra_buffer+b_offset), (char*)(&marker_block), sizeof(marker_block));
   memcpy((ra_buffer+b_offset+sizeof(marker_block)), (char*)vect, size);
   
   return(OK);
}

int iora_buffer_append (int filno, int record_num, char *vect, int size) {
   int b_offset;
   int ierr;
   buf_mark_t marker_block;

   b_offset = alloc_in_buffer(size);
   if (b_offset == NOT_IN_BUFFER) { /* ra_buffer too small for record: write to disk now */
      return(OK);
   }
   
   ierr = store_record_offset(filno, record_num, b_offset);
   marker_block.size    = size;
   marker_block.rec     = record_num;
   marker_block.filno   = filno;
   marker_block.on_disk = FALSE;		
   memcpy((ra_buffer+b_offset), (char*)(&marker_block), sizeof(marker_block));
   memcpy((ra_buffer+b_offset+sizeof(marker_block)), (char*)vect, size);
   
   return(ierr);
}

int iora_buffer_copy (int b_offset, char *dest, int *size) {
   int rec_size;
   buf_mark_t marker_block;
   
   if (dest == NULL) {
       return (IORA_ERROR);
       *size=0;
   }
   
   memcpy((&marker_block), ra_buffer+b_offset,  sizeof(marker_block));
   b_offset += sizeof(buf_mark_t);
   rec_size = marker_block.size;

   /*
    * Check size and copy the stuff to dest
    */
   if ((*size < 0) || (rec_size < *size) )
      *size  = rec_size;

   memcpy(dest, (ra_buffer+b_offset), rec_size);
   
   return(OK);
}

int iora_buffer_flush (int filno, int record_num) {
   int b_offset;
   buf_mark_t marker_block;
   long offset, nextmap;
   int ierr=0, firstnew,  nbytes;
   short rec_size_s=0;
   int   rec_size=0;
   IORA_FILE   *iora_fp;
   IORA_HEADER *iora_header;
   
   iora_fp     = iora_get_fp(filno);
   if (iora_fp == NULL) 
      return(IORA_ERROR);
      
   iora_header = iora_fp->header;
   

   if (iora_fp->read_only == TRUE) {
      printf ("\niora_buffer_flush: File %d (%s) is opened 'read-only'\n",
               filno, iora_fp->filename);
      return (IORA_ERROR);
   }
   b_offset = get_record_offset(filno, record_num);
   if (b_offset == NOT_IN_BUFFER)
      return (OK);
      
   memcpy((&marker_block), (ra_buffer+b_offset),  sizeof(marker_block));
   b_offset += sizeof(marker_block);
   iora_lowrite (filno,
                 record_num,
                 ra_buffer+b_offset,
                 marker_block.size);
      
   marker_block.on_disk = TRUE;		
   memcpy((ra_buffer+b_offset), (char*)(&marker_block), sizeof(marker_block));

   return(OK);
}


int buffer_tree_init(int filno) {
   if (filno < 0 || filno >= MAX_IORA_FILES)
      return (IORA_ERROR);
   if (ra_buffer == NULL)
      create_ra_buffer(RA_BUFFER_MAX);	    /* Default size is set via -1 */
   if (RBhead[filno] != NULL)
      buffer_tree_kill(filno);
   RBtail[filno]         = (RBnode_t *) malloc(sizeof (RBnode_t));
   RBtail[filno]->l      = RBtail[filno];
   RBtail[filno]->r      = RBtail[filno];
   RBtail[filno]->colour = BLACK;
   RBtail[filno]->offset = -1;
   RBhead[filno]         = (RBnode_t *) malloc(sizeof (RBnode_t));
   RBhead[filno]->r      = RBtail[filno];
   RBhead[filno]->l      = NULL;
   RBhead[filno]->colour = BLACK;
   RBhead[filno]->record_number = 0;
   return (OK);
}

int store_record_offset( int filno, int v, int offset) {
   if (filno < 0 || filno >= MAX_IORA_FILES)
      return (IORA_ERROR);
   RBx = RBhead[filno];
   RBp = RBhead[filno];
   RBg = RBhead[filno];
   if (RBx->record_number != 0)
      printf ("RBhead[filno]->recno != 0 (==%d)\n",RBx->record_number);
   while (RBx != RBtail[filno]) {
      RBgg = RBg;
      RBg  = RBp;
      RBp  = RBx;
      RBx  = (v < RBx->record_number) ? RBx->l : RBx->r;
   }
   RBx                = (RBnode_t *) malloc( sizeof *RBx);
   RBx->record_number = v;
   RBx->offset        = offset;
   RBx->l             = RBtail[filno];
   RBx->r             = RBtail[filno];
   if ( v < RBp->record_number)
      RBp->l = RBx;
   else
      RBp->r = RBx;

   split(filno, v);
   return(OK);
}

int get_record_offset(int filno, int v) {
   RBnode_t *x;
   if (filno < 0 || filno >= MAX_IORA_FILES)
      return (NOT_IN_BUFFER);
   x = RBhead[filno]->r;
   RBtail[filno]->record_number = v;
   RBtail[filno]->offset = NOT_IN_BUFFER;
   while (v != x->record_number)
      x = (v < (x->record_number)) ? x->l : x->r;

/*   RBtail[filno]->record_number = (-1);*/
   return x->offset;
}

int buffer_tree_kill_old (int filno) {
   RBnode_t *t, *n;
   if (RBhead[filno] == NULL) return (0);
   if (filno < 0 || filno >= MAX_IORA_FILES)
      return (1);
   n = RBhead[filno];
   while (n != RBtail[filno]) {
      t = n->r;
      CHECK_FREE (n);
      n = t;
   }
   CHECK_FREE (RBtail[filno]);
   RBhead[filno] = NULL;
   return (0);
}

static void IORA_treeKill(int filno, RBnode_t *x)
{
   if (x != RBtail[filno]) {
      IORA_treeKill(filno, x->l);
      IORA_treeKill(filno, x->r);
      CHECK_FREE(x);
   }
}

int buffer_tree_kill(int filno) {
/*   if (RBhead[filno] == NULL ||
       RBtail[filno] == NULL) return (0);
*/
   if (RBhead[filno] == NULL) return (0);
       
    IORA_treeKill(filno, RBhead[filno]->r);
    CHECK_FREE(RBhead[filno]);
/*    CHECK_FREE(RBtail[filno]);*/
   return (0);
}

int remove_record_offset (int filno, int v) {
   RBnode_t *c, *p, *x, *t;
   if (filno < 0 || filno >= MAX_IORA_FILES)
      return (1);
   RBtail[filno]->record_number = v;
   p = RBhead[filno];
   x = RBhead[filno]->r;
   while ( v != x->record_number) {
      p = x;
      x = (v < x->record_number) ? x->l : x->r;
   }
   if (x == RBtail[filno])
      return(1);
   t = x;
   if (t->r == RBtail[filno])
      x = x->l;
   else if (t->r->l == RBtail[filno]) {
      x = x->r;
      x->l = t->l;
   }
   else {
      c = x->r;
      while (c->l->l != RBtail[filno])
	 c = c->l;
      x = c->l;
      c->l = x->r;
      x->l = t->l;
      x->r = t->r;
   }
   CHECK_FREE (t);

   if (v < p->record_number)
      p->l = x;
   else
      p->r = x;
   if (RBhead[filno]->r == RBtail[filno]) {
/*      printf("RBhead[filno]->r == RBtail[filno]\n");*/
      RBhead[filno]->colour = BLACK;
   }
   return(0);
}


static int split ( int filno, int v) {
   RBx->colour = RED;
   RBx->l->colour = BLACK;
   RBx->r->colour = BLACK;
   if (RBp->colour) {
      RBg->colour = RED;
      if ((v<RBg->record_number) != (v<RBp->record_number))
	 RBp = RBrotate(v,RBg);
      RBx = RBrotate(v,RBgg);
      RBx->colour = BLACK;
   }
   RBhead[filno]->r->colour = BLACK;
   return (0);
}

static RBnode_t *RBrotate(int v, RBnode_t *y) {
   RBnode_t *c, *gc;
   c = (v < y->record_number) ? y->l : y->r;
   if (v < c->record_number) {
      gc = c->l;
      c->l = gc->r;
      gc->r = c;
   }
   else {
      gc = c->r;
      c->r = gc->l;
      gc->l = c;
   }
   if ( v < y->record_number )
      y->l = gc;
   else
      y->r = gc;

   return gc;
}

int buffer_anal(void) {
   int current, prev;
   buf_mark_t marker_block;
   int filno, rec, size, count;

   current = 0;
   count = 0;
   printf ("\nBuffer analysis at reset = %d\n", nbuff_reset);
   while (current < RaBufffree) {
      memcpy (&marker_block, (ra_buffer+current), sizeof(buf_mark_t));
      filno    = marker_block.filno;
      rec      = marker_block.rec ;
      size     = marker_block.size ;
      prev     = current;
      current += size+sizeof(buf_mark_t);
      printf("%3d, filno: %2d, rec: %6d, size: %4d, offset: %4d\n", ++count, filno, rec, size, prev);
   }
   current = RaBuffnext;
   if (current != RaBufflast && RaBuffnext != RaBufflen)
      printf ("  Gap in buffer (normal) %d\n", RaBuffnext);
   while (current < RaBufflast) {
   memcpy (&marker_block, (ra_buffer+current), sizeof(buf_mark_t));
   filno    = marker_block.filno;
   rec      = marker_block.rec ;
   size     = marker_block.size ;
   prev     = current;
   current += size+sizeof(buf_mark_t);
   printf("%3d, filno: %2d, rec: %6d, size: %4d, offset: %4d\n", ++count, filno, rec, size, prev);
   }
   return (0);
}

void treeprint(int filno) {
   printf ("Storage tree for filno %d\n", filno);
   if (RBhead[filno]->r == RBtail[filno]) {
      printf ("RBhead[filno]->r = RBtail[filno]\n");
      printf ("RBhead[filno]->l = %d\n", (int)RBhead[filno]->l);
      if (RBhead[filno]->colour == RED)
         printf ("RBhead[filno]->colour = RED\n");
      else
         printf ("RBhead[filno]->colour = BLACK\n");
      printf ("RBhead[filno]->offset = %d\n", RBhead[filno]->offset);
      printf ("RBhead[filno]->record_number = %d\n", RBhead[filno]->record_number);
   }
   else
      printf ("RBhead[filno]->r = %d\n", (int)RBhead[filno]->r);
   treeprintr(filno, RBhead[filno]->r);
}

void treeprintr(int filno, RBnode_t *x) {
   if (x != RBtail[filno]) {
      treeprintr(filno, x->l);
      printnode(filno, x);
      treeprintr(filno, x->r);
   }
}

void printnode(int filno, RBnode_t *x) {
   printf ("filno = %d, rec = %d, offset = %d\n", filno, x->record_number,  x->offset);
}

int create_ra_buffer(int size) {
   int old_size;
   int i;
   char *tmp=ra_buffer;

   if (size < 0)
      size = RA_BUFFER_DEF;
   else if (size < RA_BUFFER_MIN)
      size = RA_BUFFER_MIN;
   else if (size > RA_BUFFER_MAX)
      size = RA_BUFFER_MAX;

   if (ra_buffer == NULL)
      ra_buffer = (char *)malloc(size);
   else
      ra_buffer = realloc( ra_buffer, size);

   if (ra_buffer == NULL) {
      ra_buffer = tmp;
      printf ("WARNING: create_ra_buffer could not change the buffer size\n");
      return (1);
   }

   old_size = RaBufflen;
   if (size < RaBufflen) {  /* for safety remove all entries to the buffer */
      /* There may be smarter ways to do this */
      old_size = 0;
      for (i=1;i<=MAX_IORA_FILES;i++)
	 if (RBhead[i] != NULL)
	    buffer_tree_kill(i);
   }
   RaBufffree = old_size;
   RaBufflen  = size;
   RaBuffnext = size;
   RaBufflast = 0;
   return (0);
}

int alloc_in_buffer(int size) {
   int free_size, offset;
   buf_mark_t marker_block;
   int old_filno, old_rec, ierr;

/* KUIK */
   return (NOT_IN_BUFFER); 

   if (size < 0) {
      printf("alloc_in_buffer: size <0\n");
      return (NOT_IN_BUFFER);
    }
      
   size += sizeof(buf_mark_t);
   if (size > RaBufflen) {	/* trivial reject */
      return (NOT_IN_BUFFER);
   }
    
   free_size = RaBuffnext - RaBufffree;
   if (free_size >= size) {	    /* The easy case */
      offset = RaBufffree;
      RaBufffree += size;
      if (RaBufffree > RaBufflast)
         RaBufflast = RaBufffree;
      return (offset);
   }
   else {   /* Override blocks in the buffer */

      if ( ((RaBufflen) - RaBufffree) < size ) { /* Reset to start of buffer */
	 nbuff_reset ++;

         /*
          * First remove the records between RaBuffnext and RaBufflast
	  * ===> Cleanup the last part of the buffer!
          */
         while (RaBuffnext < RaBufflen) {
            memcpy (&marker_block, (ra_buffer+RaBuffnext), sizeof(buf_mark_t));
            old_filno   = marker_block.filno;
            old_rec     = marker_block.rec ;
            if (marker_block.on_disk == FALSE)
               iora_lowrite (marker_block.filno,
                             marker_block.rec,
                             ra_buffer+RaBuffnext+sizeof(buf_mark_t),
                             marker_block.size);
            ierr = remove_record_offset(old_filno, old_rec);  
            if (ierr != 0) 
               return (NOT_IN_BUFFER);
            RaBuffnext += marker_block.size+sizeof(buf_mark_t);
	    if ( (RaBuffnext == RaBufflast) ) {	
	       RaBuffnext = RaBufflen;
	       RaBufflast = RaBufffree;  
	    }
         }

         /*
          * First remove the first record in the buffer and reset the buffer pars
          */
	 memcpy (&marker_block, ra_buffer, sizeof(buf_mark_t));
	 RaBuffnext = marker_block.size+sizeof(buf_mark_t);
	 old_filno  = marker_block.filno;
	 old_rec    = marker_block.rec ;
		    	
         if (marker_block.on_disk == FALSE)
            iora_lowrite (marker_block.filno,
                          marker_block.rec,
                          ra_buffer+sizeof(buf_mark_t),
                          marker_block.size);
         ierr = remove_record_offset(old_filno, old_rec);   /* remove first entry now */
         if (ierr != 0) 
	    return (IORA_ERROR);
 	 RaBufffree = 0;
	 if ( (RaBuffnext == RaBufflast) ) 
	    RaBuffnext = RaBufflen;
      }

      /*
       * Now override as many blocks in buffer as needed
       */
      if (RaBuffnext == RaBufflen) {
	    RaBufflast = RaBufffree + size;	  
            free_size = RaBuffnext - RaBufffree;
      }
      while (free_size < size) {
	 memcpy (&marker_block, (ra_buffer+RaBuffnext), sizeof(buf_mark_t));
	 old_filno  = marker_block.filno;
	 old_rec    = marker_block.rec ;
         if (marker_block.on_disk == FALSE)
            iora_lowrite (marker_block.filno,
                          marker_block.rec,
                          ra_buffer+RaBuffnext+sizeof(buf_mark_t),
                          marker_block.size);
	 ierr = remove_record_offset(old_filno, old_rec);   /* remove entry now */
         if (ierr != 0) 
	    return (IORA_ERROR);
	 RaBuffnext += marker_block.size+sizeof(buf_mark_t);
	 if (RaBuffnext == RaBufflast) {
	    RaBuffnext = RaBufflen;
	    RaBufflast = RaBufffree + size;
	 }

	 free_size = RaBuffnext - RaBufffree;
      }
   }
   offset = RaBufffree;
   RaBufffree += size;
   if (RaBufffree > RaBufflast)
      printf("RaBufflast should not have been reset here\n");

   return (offset);
}

int print_buffer_stat(void) {
   printf ("\n____ ra buffering statistics ____\n");
   printf ("____ # reads absolute: %6d\n", nreads);
   printf ("____ # reads buffered: %6d\n", nreadhits);
   printf ("____ # buffer resets : %6d\n", nbuff_reset);
   if (nreads > 0)
      printf ("____ buffer hit rate : %6d\n", (int)(100*nreadhits/nreads));
   printf ("____ buffer filled   : %6d\n", (int)(100*RaBufffree/RaBufflen));
   return (0);
}

static void RBtreecount(int filno, RBnode_t *x, int *counter) {
   if (x != RBtail[filno]) {
      RBtreecount(filno, x->l, counter);
      (*counter)++;
      RBtreecount(filno, x->r, counter);
   }
}

int RBcount_records(int filno) {
   int counter=0;
   
   RBtreecount(filno, RBhead[filno]->r, &counter);
   if (counter == 0)
      return NOT_IN_BUFFER;
   else
      return (counter);
}

static void RBtreelist_r(int filno, RBnode_t *x, int *list, int *index) {
         
   if (x != RBtail[filno]) {
      RBtreelist_r(filno, x->l, list, index);
      list[*index] = x->record_number;
      (*index)++;
      RBtreelist_r(filno, x->r, list, index);
   }
   else
      list[*index] = NOT_IN_BUFFER;
}

void RBtreelist(int filno, int *list) { 
   int index = 0;
   
   RBtreelist_r(filno, RBhead[filno]->r, list, &index);
}

int iora_buffer_next(int filno, int flag) {
   static int *list=NULL, *ptr;
   static int index, length;
   static int last_file=-1;
   int        record_num;
   
   if (filno < 0 || filno >= MAX_IORA_FILES)
      return (NOT_IN_BUFFER);

   if (last_file != filno | flag < 0) { 	/* initialise */
      length = RBcount_records(filno);
      if (length == NOT_IN_BUFFER) 
         return(NOT_IN_BUFFER);
         
      list = realloc(list, (length+1)*sizeof(int));
      ptr  = list;
      RBtreelist(filno, ptr);
      index     = 0;
      last_file = filno;
   }
   record_num = list[index++];

   return(record_num);
}

