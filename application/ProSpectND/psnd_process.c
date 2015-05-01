/********************************************************************/
/*                           psnd_process.c                         */
/*                                                                  */
/* 1997, Albert van Kuik                                            */
/********************************************************************/
#include <stdio.h>
#include <stdlib.h>
#include <ctype.h>
#include <string.h>
#include <stdarg.h>
#include <math.h>
#include <assert.h>
#include "genplot.h"
#include "psnd.h"
#include "mathtool.h"
#include "nmrtool.h"




int psnd_printf(MBLOCK *mblk, const char *format, ...)
{
    va_list argp;
    int ret_val;
    char *p,buf[2*PSND_STRLEN];

    va_start(argp, format);
    ret_val = vsprintf(buf, format, argp);
    va_end(argp);
    /* for now, just print it */
    printf(buf);
    if (!mblk->info->foreground || !mblk->info->verbose)
        return ret_val;
    if ((p=strchr(buf,'\n')) != NULL)
        *p = '\0';
    psnd_setprompt(mblk, DAT->prompt, buf);
    return ret_val;
}


int psnd_puts(MBLOCK *mblk, char *buf)
{
    int ret_val=0;
    char *p,buf2[2*PSND_STRLEN+1];

    /* for now, just print it */
    fputs(buf, stdout);
    if (!mblk->info->foreground || !mblk->info->verbose)
        return ret_val;
    strncpy(buf2,buf,2*PSND_STRLEN);
    if ((p=strchr(buf2,'\n')) != NULL)
        *p = '\0';
    psnd_setprompt(mblk, DAT->prompt, buf2);
    return ret_val;
}

/*
 * Create a volatile label in a buffer that is large enough.
 * The label is valid until LABELTEMPSTEPS labels are produced,
 * then next call to psnd_sprintf_temp overwrites the first label, etc.
 */
#define LABELTEMPSIZE	(4*PSND_STRLEN)
#define LABELTEMPSTEPS	4
#define LABELSTEPSIZE	(LABELTEMPSIZE/LABELTEMPSTEPS)
char *psnd_sprintf_temp(const char *format, ...)
{
    va_list argp;
    static char buf[LABELTEMPSIZE];
    char *b;
    static int icount;

    b = buf + (icount * LABELSTEPSIZE);
    va_start(argp, format);
#ifdef _WIN32
    vsprintf(b, format, argp);
#else
    vsnprintf(b, LABELSTEPSIZE, format, argp);
#endif
    va_end(argp);
    b[LABELSTEPSIZE-1] = '\0';
    icount++;
    if (icount >= LABELTEMPSTEPS)
        icount = 0;
    return b;
}

/*
 * Read an integer value from a string.
 * If the integer is followed by the character 'k'
 * the result is multiplied by 1024. Likewise, a character
 * 'm' results in a multiplication by 1000 * 1024.
 */
int psnd_scan_integer(char *value)
{
    float result = 0;
    char  modify = 0;
    sscanf(value,"%f%c",&result, &modify);
    if (modify) {
        switch (modify) {
        case 'k':
        case 'K':
            result *= 1024;
            break;
        case 'm':
        case 'M':
            result *= 1024000;
            break;
        }
    }
    return (int)result;
}

/*
 * Read a float value from a string.
 * If the float is followed by the character 'k'
 * the result is multiplied by 1024. Likewise, a character
 * 'm' results in a multiplication by 1000 * 1024. And a 
 * character of 'g' result in a multiplication by 1000 * 1000 * 1024
 */
float psnd_scan_float(char *value)
{
    float result = 0.0;
    char  modify = 0;
    sscanf(value,"%f%c",&result, &modify);
    if (modify) {
        switch (modify) {
        case 'k':
        case 'K':
            result *= 1024.0;
            break;
        case 'm':
        case 'M':
            result *= 1024.0e3;
            break;
        case 'g':
        case 'G':
            result *= 1024.0e6;
            break;
        }
    }
    return result;
}


/***************************************************
*
*/

void psnd_integrate(MBLOCK *mblk, float x1, float x2, float y1, float y2)
{
    if (mblk->info->plot_mode == PLOT_2D)
        psnd_integrate2d(mblk, x1, x2, y1, y2);
    else
        psnd_integrate1d(mblk, x1, x2);
}

/*
*  calculate the distance of two cursor points
*/

static void distance1d(MBLOCK *mblk, float x1, float y1, float x2, float y2)
{
    float dx, dy, ppm1, ppm2, hz1, hz2, sec1, sec2;
    int mode,size;
    char unit[20];

    if (PAR->nsiz <= 1)
        return;

    size = DAT->isize;
    x2 = psnd_calc_pos_ex(x2, PAR->xref, PAR->aref, PAR->bref, DAT->irc, 
               DAT->isspec, mblk->spar[S_AXISX_MARK].show, 
               size, DAT->sw, PAR->sfd, NULL, unit,
               &ppm1, &hz1, &sec1, &mode);
    x1 = psnd_calc_pos_ex(x1, PAR->xref,  PAR->aref, PAR->bref, DAT->irc, 
                DAT->isspec, mblk->spar[S_AXISX_MARK].show,
                size, DAT->sw, PAR->sfd, NULL, NULL,
                &ppm2, &hz2, &sec2, &mode);
    dx = x2 - x1;
    dy = y2 - y1;
    switch (mode) {
        case AXIS_UNITS_HERTZ:
            psnd_printf(mblk, " dx = %10.3f %s (%8.3f ppm), dy = %10.3e \n", 
                         dx, unit, ppm2-ppm1, dy);
            break;
        case AXIS_UNITS_PPM:
            psnd_printf(mblk, " dx = %10.3f %s (%8.3f hz), dy = %10.3e \n", 
                         dx, unit, hz2-hz1, dy);
            break;
        case AXIS_UNITS_SECONDS:
            psnd_printf(mblk, " dx = %10.3f %s, dy = %10.3e \n", dx, unit, dy);
            break;
        default:
            psnd_printf(mblk, " dx = %10.3f %s, dy = %10.3e \n", dx, unit, dy);
   }
}

static void distance2d(MBLOCK *mblk, float x1, float y1, float x2, float y2)
{
    float dx, dy;
    char xunit[20],yunit[20];
    PBLOCK *par1 = mblk->cpar_screen->par1;
    PBLOCK *par2 = mblk->cpar_screen->par2;

    x2 = psnd_calc_pos(x2, par1->xref, 
                  par1->aref, par1->bref, DAT->irc,
                  par1->isspec, mblk->spar[S_AXISX_MARK].show, 
                  DAT->isize, DAT->sw, par1->sfd, 
                  NULL, xunit);
    x1 = psnd_calc_pos(x1, par1->xref,
                  par1->aref, par1->bref, DAT->irc,
                  par1->isspec, mblk->spar[S_AXISX_MARK].show, 
                  DAT->isize, DAT->sw, par1->sfd,
                  NULL, NULL);
    y2 = psnd_calc_pos(y2, par2->xref,  
                  par2->aref, par2->bref, mblk->cpar_screen->ihbmin,
                  par2->isspec, mblk->spar[S_AXISY_MARK].show, 
                  par2->nsiz, par2->swhold, par2->sfd, 
                  NULL, yunit);
    y1 = psnd_calc_pos(y1, par2->xref,
                  par2->aref, par2->bref, mblk->cpar_screen->ihbmin,
                  par2->isspec, mblk->spar[S_AXISY_MARK].show, 
                  par2->nsiz, par2->swhold, par2->sfd, 
                  NULL, NULL);
    dx = x2 - x1;
    dy = y2 - y1;
    psnd_printf(mblk," dx = %10.3f %s, dy = %10.3f %s\n", dx, xunit, dy, yunit);
}

void psnd_distance(MBLOCK *mblk, float x1, float y1, float x2, float y2)
{
    if (mblk->info->plot_mode == PLOT_2D)
        distance2d(mblk, x1,y1,x2,y2);
    else
        distance1d(mblk, x1,y1,x2,y2);
}


/*
*  Calculate current cursor position
*/
static void position1d(MBLOCK *mblk, float x, float y)
{
    float ypos, ppm, hz, sec;
    int size, xpos, mode;
    char label[20],unit[20];


    if (PAR->nsiz <= 1)
        return;
    size = DAT->isize;
    ypos = DAT->xreal[round(x-1)];
    xpos = psnd_calc_pos_ex(x, PAR->xref, PAR->aref, PAR->bref, DAT->irc,
                             DAT->isspec, mblk->spar[S_AXISX_MARK].show, size, 
                             DAT->sw, PAR->sfd, label, unit, 
                             &ppm, &hz, &sec, &mode);
    switch (mode) {
        case AXIS_UNITS_HERTZ:
            psnd_printf(mblk," X = %s %s (%8.3f ppm, CX = %5d) Y = %10.3e (CY = %10.3e)\n", 
                 label, unit, ppm, round(x), ypos, y);
            break;
        case AXIS_UNITS_PPM:
            psnd_printf(mblk," X = %s %s (%8.2f hz, CX = %5d) Y = %10.3e (CY = %10.3e)\n", 
                 label, unit, hz, round(x), ypos, y);
            break;
        case AXIS_UNITS_SECONDS:
            psnd_printf(mblk," X = %s %s  Y = %10.3e (CX = %5d, CY = %10.3e)\n", 
                 label, unit, ypos, round(x), y);
            break;
        default:
            psnd_printf(mblk," X = %s %s  Y = %10.3e (CX = %5d, CY = %10.3e)\n", 
                 label, unit, ypos, round(x), y);
   }
}

static void position2d(MBLOCK *mblk, float x, float y)
{
    char xlabel[20],ylabel[20];
    char xunit[20],yunit[20];
    float xpos, ypos, sw;
    int isize;
    PBLOCK *par1 = mblk->cpar_screen->par1;
    PBLOCK *par2 = mblk->cpar_screen->par2;

    isize = par1->irstop-par1->irstrt+1;
    sw    = par1->swhold * (isize-1)/ (par1->nsiz-1);
    xpos = psnd_calc_pos(x, par1->xref,
                 par1->aref, par1->bref, DAT->irc,
                 par1->isspec, mblk->spar[S_AXISX_MARK].show, 
                 isize, sw, par1->sfd, 
                 xlabel, xunit);
    isize = par2->irstop-par2->irstrt+1;
    sw    = par2->swhold * (isize-1)/ (par2->nsiz-1);
    ypos = psnd_calc_pos(y, par2->xref, 
                 par2->aref, par2->bref, par2->irstrt,
                 par2->isspec, mblk->spar[S_AXISY_MARK].show, 
                 isize, sw, par2->sfd, 
                 ylabel, yunit);
    psnd_printf(mblk, " X = %s %s  Y = %s %s (CX = %5d, CY = %5d)\n", 
                  xlabel, xunit, ylabel, yunit, round(x), round(y));

}


void psnd_position(MBLOCK *mblk, float x, float y)
{
    if (mblk->info->plot_mode == PLOT_2D)
        position2d(mblk, x, y);
    else
        position1d(mblk, x, y);
}

/*
*  Just print cursor x,y
*/

void psnd_where(MBLOCK *mblk, float x, float y)
{
    psnd_printf(mblk, " X = %10.4g, Y = %10.3e \n", x, y);
}

/*
* Calculate the rms noise ove an area
*/

/*
 *       sqrt ( sum((x-avg)**2) / (n-1))
 */
static void rmsnoise1d(MBLOCK *mblk, float x1, float x2)
{
    float noise;
    int ja1, ja2, n;

    if (x2 < x1) {
        ja1=round(x2);
        ja2=round(x1);
    }
    else {
        ja1=round(x1);
        ja2=round(x2);
    }
    ja1=max(1, ja1);
    ja2=min(DAT->isize,ja2);
    n = ja2-ja1+1;

    if (n < 2)
        return;
    noise = rmsnoi(DAT->xreal+ja1-1,n);
                   
    psnd_printf(mblk," RMS noise = %10.3e\n", noise);

}

static void rmsnoise2d(MBLOCK *mblk, float x1, float x2, float y1, float y2)
{
    int ja1, ja2, jb1, jb2;
    float avg, rms;
    int vp = mblk->spar[S_CONTOUR].vp_id,
        obj=mblk->spar[S_CONTOUR].obj_id,
        col=mblk->spar[S_CONTOUR].color3;
    
    if (x2 < x1) {
        float tmp = x1;
        x1 = x2;
        x2 = tmp;
    }
    if (y2 < y1) {
        float tmp = y1;
        y1 = y2;
        y2 = tmp;
    }
    x1=max((float)mblk->cpar_screen->ihamin,x1);
    x2=min((float)mblk->cpar_screen->ihamax,x2);
    y1=max((float)mblk->cpar_screen->ihbmin,y1);
    y2=min((float)mblk->cpar_screen->ihbmax,y2);
/*
*         draw box
*/
    g_append_object(obj);
    g_set_foreground(col);
    g_rectangle(x1,y1,x2,y2);
    g_close_object(obj);
    g_plotall();
    ja1=round(y1);
    ja2=round(y2);
    jb1=round(x1);
    jb2=round(x2);
    

    rmsn2d(DAT->finfo[INFILE].ifile,
           mblk->cpar_screen->xdata,
           ja1,
           ja2,
           jb1,
           jb2,
           &avg,
           &rms,
           read2d,
           (void*)mblk);

    psnd_printf(mblk, " Average   = %10.3e,  RMS noise = %10.3e\n", avg, rms);

}

void psnd_rmsnoise(MBLOCK *mblk, float x1, float x2, float y1, float y2)
{
    if (mblk->info->plot_mode == PLOT_2D)
        rmsnoise2d(mblk, x1,x2,y1,y2);
    else
        rmsnoise1d(mblk, x1,x2);
}




/*********************************************************************
*
*/


float redf(int i)
{
    int n = 4;

    return (float) (sign(1,(1-fmod((i-1),n))));
}

#define MAXCHAR PSND_STRLEN
char *scanfile(FILE *file, char *pattern1, int skip, char *pattern2,
               int isregexp)
{
    static char resultbuf[MAXCHAR+1];
    char *p, pattern[MAXCHAR+1], buf[MAXCHAR+1];
    int len, skip_store, ok = FALSE, mode = 1;
    long pos = ftell(file);
    rewind(file);
    if (skip == 0) {
        ok = TRUE;
        mode = 0;
    }
    skip_store = skip;
    strncpy(pattern, pattern2,MAXCHAR);
    pattern[MAXCHAR] = '\0';
    len = strlen(pattern);
    if (len >= 2) {
        p = pattern + len - 2;
        if (p[0] == '%' && p[1] == 's')
            *p = '\0';
    }
    while (fgets(buf,MAXCHAR,file) != NULL) {
        buf[MAXCHAR] = '\0';
        if (mode) {
            if (!ok) {
                if (isregexp) {
                    if (psnd_grep(buf, pattern1, &len)) 
                        ok = TRUE;
                }
                else {
                    if (strstr(buf, pattern1) != NULL) 
                        ok = TRUE;
                }
            }
            else 
                skip--;
            if (!ok || skip != 0) 
                continue;
        }
        if (isregexp) {
            if (psnd_grep(buf, pattern, &len)) {
                resultbuf[0] = '\0';
                if (sscanf(buf+len, "%s", resultbuf)) {
                    fseek(file, pos, SEEK_SET);
                    return resultbuf;
                }
            }
        }
        else {
            char *p;
            if ((p=strstr(buf, pattern)) != NULL) {
                len = strlen(pattern);
                resultbuf[0] = '\0';
                if (sscanf(p+len, "%s", resultbuf)) {
                    fseek(file, pos, SEEK_SET);
                    return resultbuf;
                }
            }
        }
        ok   = FALSE;
        skip = skip_store;
    }
    fseek(file, pos, SEEK_SET);
    return NULL;
}



/*
 * read from binary Bruker ASPECT X32 acqu file
 */
char *scanbinfile(FILE *file, char *pattern1)
{
    static char resultbuf[MAXCHAR+1];
    char *p, pattern[MAXCHAR+1], buf[MAXCHAR+1];
    int len,ok;
    long pos = ftell(file);
    rewind(file);
    strncpy(pattern, pattern1,MAXCHAR);
    pattern[MAXCHAR] = '\0';
    len = strlen(pattern);
    if (len >= 2) {
        p = pattern + len - 2;
        if (p[0] == '%' && p[1] == 's')
            *p = '\0';
    }
    resultbuf[0] = '\0';
    ok=read_acqufile_bin(file, resultbuf, pattern);
    fseek(file, pos, SEEK_SET);
    if (ok)
        return resultbuf;
    return NULL;
}
















