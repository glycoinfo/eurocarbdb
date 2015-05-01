/********************************************************************/
/*                            brukx32.c                             */
/*                                                                  */
/*                                                                  */
/* Prepa routines for binary Bruker ASPECT X32 acqu file            */
/*                                                                  */
/*  Albert van Kuik, 1998                                           */  
/********************************************************************/
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
#include "mathtool.h"
#include "nmrtool.h"

#include <time.h>
#define FILEMAX 8000


/*
 * Test the byte order of the operating system
 */
int os_big_endian()
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

#define INT4 int


typedef struct {
    char name[8];
    INT4 size;
    INT4 type;
    INT4 dummy;
    char data[1000];
} ACQTYPE;

enum types {T_INT,T_FLOAT,T_DOUBLE,T_STRING,T_DUMMY };

static void print_int(char *buf, char *c, int bigendian)
{
    INT4 *i4;

    i4 = (INT4*) c;
    if (bigendian)
        xxswap4((float*)i4, 1, 1);
    sprintf(buf, "%d", *i4);
}


static void print_double(char *buf, char *c, int bigendian)
{
    double *d;

    d = (double*) c;
    if (bigendian) 
        xxswap8((float*)c, 1, 2);
    sprintf(buf, "%f",*d);
}

static void print_float(char *buf, unsigned char *c, int bigendian)
{
    float *f;

    f = (float*) c;
    if (bigendian)
        xxswap4(f, 1, 1);
    sprintf(buf, "%f",*f);
}

static void print_string(char *buf, char *c)
{
    sprintf(buf, "%s", c);
}

static int type(ACQTYPE *acq)
{
    unsigned char *c;

    c = (unsigned char*) &(acq->type);
    c += 2;
    switch (*c & 0x0F) {
        case 0x08 : return(T_STRING);
        case 0x03 : return(T_FLOAT);
        case 0x04 : return(T_DOUBLE);
        case 0x01 :
        case 0x05 : return(T_INT);
    }
    return(T_DUMMY);
}

static int sizeoftype(int type)
{
    switch(type) {
        case T_INT    : return(sizeof(INT4));
        case T_FLOAT  : return(sizeof(float));
        case T_DOUBLE : return(sizeof(double));
        default       : return(1);
    }
}

static int array_max(int type,int size)
{
    if (size <= 8)
        return(1);
    switch(type) {
        case T_INT    : return(size / sizeof(INT4));
        case T_FLOAT  : return(size / sizeof(float));
        case T_DOUBLE : return(size / sizeof(double));
        default       : return(1);
    }
}

/*
 * Scan 'acqufile', grep the contents of a value,
 * stored in 'pattern', and return the result in the variable 'result'
 *
 * For example: 'pattern' is "TD" and 'result' is "4098"
 *
 * Return TRUE on success and FALSE otherwise
 */
int read_acqufile_bin(FILE *acqufile, char *result, char *pattern)
{
    char *a,*ad,name1[20],auname[80];
    ACQTYPE acq;
    int i,j,t,n,zz00 = 0,count = 0;
    int bigendian = os_big_endian();

    i = 0;
    a = (char*) &acq;
    result[0] = '\0';
    while (count++ < FILEMAX) {
        a[i++] = fgetc(acqufile);
        if (i==16 && bigendian) 
            xxswap4((float*) &(acq.size), 1, 1);
        if (i == 28) {
            /*---extended data block---*/
            while (zz00 && ((i - 20) < acq.size) && count++ < FILEMAX) {
                a[i++] = fgetc(acqufile);
                if (i==sizeof(ACQTYPE)) 
                    break;
            }
            i = 0;
            /*---Second part of ACQU file, with data > 8 bytes ---*/
            if (strcmp("ZZ00",acq.name)==0)
                zz00 = 1;
            /*----end of acqu file ---*/
            if (strcmp("ENDE",acq.name)==0)
                break;
            /*---start---*/
            if (strcmp("A000",acq.name)==0)
                continue;
            /*---value follows after 'ZZ00' in second part ACQU file---*/
            if (strcmp("ZZ00",acq.data)==0)
                continue;
            t = type(&acq);
            n = array_max(t,acq.size);
            ad = acq.data;
            j = 0;
            while (n-j) {
                if (n > 1) {
                    if (strcmp("ROUTWD1",acq.name)==0
                        || strcmp("ROUTWD2",acq.name)==0)
                        sprintf(name1,"%s-%d",acq.name,j);
                    else
                        sprintf(name1,"%s%d",acq.name,j);
                }
                else
                    strcpy(name1,acq.name);
                j++;
                /*
                 * Found a matching pair
                 */
                if (strcmp(name1, pattern) == 0) {
                    /*
                     * Copy the value into 'result' and return
                     */
                    if (strcmp("DATE",acq.name)==0)
                        print_string(result, ctime((time_t*)acq.name));
                    else {
                        switch(t) {
                            case T_STRING  : 
                                print_string(result, ad); 
                                break;
                            case T_INT     : 
                                print_int(result, ad, bigendian);
                                break;
                            case T_DOUBLE  : 
                                print_double(result, ad, bigendian);
                                break;
                            case T_FLOAT   : 
                                print_float(result, (unsigned char *)ad, bigendian);
                                break;
                        }
                    }
                    return TRUE;
                }
                ad += sizeoftype(t);
            }
        }
    }
    return FALSE;
}


