/*****************************************************************************/
/*                               MSHELL.C                                    */
/*  Dr Dobbs Journal , August 1990                                           */
/*  Valid: MSDOS, VMS, UNIX (SGI)                                            */
/*  Modifications to: mem_alloc, mem_realloc                                 */
/*****************************************************************************/
#define _MSHELL_

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "mshell.h"

#define MEMTAG 0xa55a			/* Value for mh_tag  */
#define MEMEND 0x166b			/* Value for mem_end  */
#define ERR_TAG	 1
#define ERR_END  2
#define ERR_FREE 3

   typedef struct memnod {			/* MEMORY BLOCK HEADER INFO */
   unsigned int mh_tag;			/* Special ident tag        */
   size_t       mh_size;			/* Size of allocation block */
#if defined (MEM_LIST)
   struct memnod *mh_next;		/* Next memory block        */
   struct memnod *mh_prev;		/* Previous memory block    */
#endif
#if defined (MEM_WHERE)
   char          *mh_file;		/* File allocation was from */
   unsigned int  mh_line;		/* Line allocation was from */
#endif
   } 
MEMHDR;

#define ALIGN_SIZE sizeof(long)
#define HDR_SIZE   sizeof(MEMHDR)
#define END_SIZE   sizeof(int)
#define RESERVE_SIZE (((HDR_SIZE+(ALIGN_SIZE-1))/ALIGN_SIZE) \
* ALIGN_SIZE)
#define CALC_SIZE(x) ((((x) +(ALIGN_SIZE-1))/ALIGN_SIZE) \
* ALIGN_SIZE)
#define CLIENT_2_HDR(a) ((MEMHDR *) (((char *) (a)) - RESERVE_SIZE))
#define HDR_2_CLIENT(a) ((void *) (((char *) (a)) + RESERVE_SIZE))
#define GET_END(p,s)  ((int *) (((char *) (p)) + RESERVE_SIZE + (s)))

static unsigned long mem_size=0;

#if defined(MEM_LIST)
static MEMHDR *memlist=NULL;
#endif

static void mem_err(void *,int,char *,int);
#if defined(MEM_LIST)
static void mem_list_add(MEMHDR *);
static void mem_list_delete(MEMHDR *);
#endif
#if defined(MEM_WHERE)
#define Mem_Tag_Err(a)  mem_err(a,ERR_TAG,fil,lin)
#define Mem_End_Err(a)  mem_err(a,ERR_END,fil,lin)
#define Mem_Free_Err(a) mem_err(a,ERR_FREE,fil,lin)
#else
#define Mem_Tag_Err(a)  mem_err(a,ERR_TAG,__FILE__,__LINE__)
#define Mem_End_Err(a)  mem_err(a,ERR_END,__FILE__,__LINE__)
#define Mem_Free_Err(a) mem_err(a,ERR_FREE,__FILE__,__LINE__)
#endif

void *mem_alloc(
#if defined(MEM_WHERE)
size_t size, char *fil, int lin
#else
size_t size
#endif
)
{
MEMHDR *p;
int *e;

#if defined(MEM_CHECK)
Mem_Overflow_Test(fil,lin);
#endif
size = CALC_SIZE(size);
p = malloc(RESERVE_SIZE + size + END_SIZE);
if (p==NULL)
return(NULL);
p->mh_tag = MEMTAG;
p->mh_size = size;
mem_size += size;
e = GET_END(p,size);
*e = MEMEND; 
#if defined(MEM_WHERE)
p->mh_file = fil;
p->mh_line = lin;
#endif
#if defined(MEM_LIST)
mem_list_add(p);
#endif
return(HDR_2_CLIENT(p));
}


void *mem_calloc(
#if defined(MEM_WHERE)
size_t size, size_t elem, char *fil, int lin
#else
size_t size, size_t elem
#endif
)
{
void *m;

#if defined(MEM_WHERE)
m = mem_alloc(size*elem,fil,lin);
#else
m = mem_alloc(size*elem);
#endif
if (m!=NULL)
memset(m,0,size*elem);
return(m);
}

void *mem_realloc(
#if defined(MEM_WHERE)
void *ptr, size_t size, char *fil, int lin
#else
void *ptr, size_t size
#endif
)
{
MEMHDR *p;
int *e;

if (ptr == NULL)
#if defined (MEM_WHERE)
return mem_alloc(size, fil, lin);
#else
return mem_alloc(size);
#endif
#if defined(MEM_CHECK)
Mem_Overflow_Test(fil,lin);
#endif
p = CLIENT_2_HDR(ptr);
if (p->mh_tag == (unsigned) ~MEMTAG) {
Mem_Free_Err(p);
return(NULL);
}
if (p->mh_tag != MEMTAG) {
Mem_Tag_Err(p);
return(NULL);
}
e = GET_END(p,p->mh_size);
if (*e != MEMEND) {
Mem_End_Err(p);
return(NULL);
}
p->mh_tag = ~MEMTAG;
mem_size -= p->mh_size;
#if defined(MEM_LIST)
mem_list_delete(p);
#endif
size = CALC_SIZE(size);
p = (MEMHDR*)realloc(p,RESERVE_SIZE + size + END_SIZE);
if (p==NULL)
return(NULL);
p->mh_tag = MEMTAG;
p->mh_size = size;
mem_size += size;
e = GET_END(p,size);
*e = MEMEND;
#if defined(MEM_WHERE)
p->mh_file = fil;
p->mh_line = lin;
#endif
#if defined(MEM_LIST)
mem_list_add(p);
#endif
return(HDR_2_CLIENT(p));
}

char *mem_strdup(
#if defined(MEM_WHERE)
char *str, char *fil, int lin
#else
char *str
#endif
)
{
char *s;

#if defined(MEM_WHERE)
s = mem_alloc(strlen(str)+1,fil,lin);
#else
s = mem_alloc(strlen(str)+1);
#endif
if (s != NULL)
strcpy(s,str);
return(s);
}


void mem_free(
#if defined(MEM_WHERE)
void *ptr, char *fil, int lin
#else
void *ptr
#endif
)
{
MEMHDR *p;
int *e;

#if defined(MEM_CHECK)
Mem_Overflow_Test(fil,lin);
#endif
p = CLIENT_2_HDR(ptr);
if (p->mh_tag == (unsigned) ~MEMTAG) {
Mem_Free_Err(p);
return;
}
if (p->mh_tag != MEMTAG) {
Mem_Tag_Err(p);
return;
}
e = GET_END(p,p->mh_size);
if (*e != MEMEND) {
Mem_End_Err(p);
return;
}
p->mh_tag = ~MEMTAG;
mem_size -= p->mh_size;
#if defined(MEM_CLEAR)
memset(ptr,0,p->mh_size);
#endif
#if defined(MEM_LIST)
mem_list_delete(p);
#endif
free(p);
}

/*---Returns the total number of allocated bytes ---*/
unsigned long Mem_Used()
{
return(mem_size);
}

/*---Displays a list of allocated memory blocks---*/
void Mem_Display(FILE *fp)
{
#if defined(MEM_LIST)
int *e;
MEMHDR *p;
int    idx;

fprintf(fp,"MEMORY ALLOCATION TABLE :\r\n");
#if defined(MEM_WHERE)
fprintf(fp,"Index   Size  File(Line) - total size %lu\r\n",mem_size);
#else
fprintf(fp,"Index   Size - total size %lu\r\n",mem_size);
#endif

idx = 0;
p = memlist;
while (p != NULL) {
fprintf(fp,"%-5d %6u",idx++,p->mh_size);
#if defined(MEM_WHERE)
fprintf(fp," %s(%d)",p->mh_file,p->mh_line);
#endif
if (p->mh_tag == (unsigned)~MEMTAG) {
fprintf(fp," FREE ERROR");
}
else if (p->mh_tag != MEMTAG) {
fprintf(fp," INVALID");
break;
}
e = GET_END(p,p->mh_size);
if (*e != MEMEND) {
fprintf(fp," OVERFLOW");
}
fprintf(fp,"\r\n");
p=p->mh_next;
}
#else
fprintf(fp,"Memory list not compiled (MEM_LIST not defined)\r\n");
#endif
}


/*---Checks all the allocated memory blocks for memory overflow
     and crashes the program on errors found---*/
void Mem_Overflow_Test(char *fil,int lin)
{
#if defined(MEM_LIST)
int *e;
MEMHDR *p;

p = memlist;
while (p != NULL) {
if (p->mh_tag == (unsigned)~MEMTAG) {
Mem_Free_Err(p);
}
else if (p->mh_tag != MEMTAG) {
Mem_Tag_Err(p);
}
e = GET_END(p,p->mh_size);
if (*e != MEMEND) {
Mem_End_Err(p);
}
p=p->mh_next;
}
#else
fprintf(stderr,"Memory list not compiled (MEM_LIST not defined)\r\n");
#endif
}



#if defined(MEM_LIST)
static void mem_list_add(MEMHDR *p)
{
p->mh_next = memlist;
p->mh_prev = NULL;
if (memlist != NULL)
memlist->mh_prev = p;
memlist = p;
#if defined(DEBUG_LIST)
printf("mem_list_add\r\n");
Mem_Display(stdout);
#endif
}
#endif


#if defined(MEM_LIST)
static void mem_list_delete(MEMHDR *p)
{
if (p->mh_next != NULL)
p->mh_next->mh_prev = p->mh_prev;
if (p->mh_prev != NULL)
p->mh_prev->mh_next = p->mh_next;
else
memlist = p->mh_next;
#if defined(DEBUG_LIST)
printf("mem_list_delete\r\n");
Mem_Display(stdout);
#endif
}
#endif

static void print_buffer_line(void *ptr)
{
unsigned char *p,*ph,c;
int i;

ph = p = HDR_2_CLIENT((MEMHDR*)ptr);
fprintf(stderr,"Dump Charbuf : ");
for (i=0;i<20;i++) {
(*p < 32) ? (c = '.') : (c = *p);
p++;
fprintf(stderr,"%2c ",c);
}
fprintf(stderr,"\r\nDump Hexbuf  : ");
for (i=0;i<20;i++)
fprintf(stderr,"%02X ",*ph++);
fprintf(stderr,"\r\n");
}


static void mem_err(void *p,int flag,char *fil,int lin)
{
fprintf(stderr,"Crash.......\r\n");
switch (flag) {
case ERR_TAG :
fprintf(stderr,"Memory tag error - %p - %s(%d)\r\n",p,fil,lin);
break;
case ERR_END :
fprintf(stderr,"Memory overflow - %p - %s(%d)\r\n",p,fil,lin);
break;
case ERR_FREE:
fprintf(stderr,"Memory free error -%p - %s(%d)\r\n",p,fil,lin);
break;
}
print_buffer_line(p);
#if defined(MEM_LIST)
Mem_Display(stderr);
#endif
exit(1);
}
