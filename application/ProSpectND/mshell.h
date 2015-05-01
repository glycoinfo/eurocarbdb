/* MSHELL.H */

#ifndef MSHELL_H
#define MSHELL_H

#if !defined(__STDIO_DEF_)
#include <stdio.h>
#endif
#if !defined(__STDLIB)
#include <stdlib.h>
#endif

/* Compilation options */
#define MEM_LIST        /* Build internal list */
#define MEM_WHERE        /* Keep track of memory block source */

#define MEM_CLEAR        /* Clear memory before free */
#if defined(MEM_WHERE)
#if defined(MEM_LIST)
#define MEM_CHECK             /* if defined with MEM_WHERE + MEM_LIST
                   MEM_CHECK checks in each routine all the
                   allocated blocks */
#endif
#endif


unsigned long     Mem_Used();
void            Mem_Display(FILE *);
void        Mem_Overflow_Test(char *,int);

#if defined(MEM_WHERE)
void     *mem_alloc(size_t,char *,int);
void     *mem_calloc(size_t,size_t,char *,int);
void     *mem_realloc(void *,size_t,char *,int);
void    mem_free(void *,char *,int);
char    *mem_strdup(char *,char *,int);
#else
void     *mem_alloc(size_t);
void     *mem_calloc(size_t,size_t);
void     *mem_realloc(void *,size_t);
void    mem_free(void *);
char     *mem_strdup(char *);
#endif

#if !defined(_MSHELL_)
#undef malloc
#undef calloc
#undef realloc
#undef free
#undef strdup
#if defined(MEM_WHERE)
#define malloc(a)    mem_alloc((a),__FILE__,__LINE__)
#define calloc(a,b)    mem_calloc((a),(b),__FILE__,__LINE__)
#define realloc(a,b)    mem_realloc((a),(b),__FILE__,__LINE__)
#define free(a)        mem_free((a),__FILE__,__LINE__)
#define strdup(a)    mem_strdup((a),__FILE__,__LINE__)
#define mem_overflow_test() Mem_Overflow_Test(__FILE__,__LINE__)
#else
#define malloc(a)    mem_alloc(a)
#define calloc(a,b)    mem_calloc((a),(b))
#define realloc(a,b)    mem_realloc((a),(b))
#define free(a)        mem_free(a)
#define strdup(a)    mem_strdup(a)
#define mem_overflow_test() Mem_Overflow_Test(" ",0)
#endif
#endif
#endif



