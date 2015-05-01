/********************************************************************/
/*                            integrate.c                           */
/*                                                                  */
/********************************************************************/
#include <stdio.h>
#include <stdlib.h>
#include <ctype.h>
#include <string.h>
#include <stdarg.h>
#include <math.h>
#include "mathtool.h"
#include "nmrtool.h"

static int okarea2(int i1, int i2, int j1, int j2)
{
     return ((i2 >= i1) && (j2 >= j1) && (i1 > 0) && (j1 > 0));
}


/*
 * find the highest point of area 'fbuf' with size 'na,nb' starting 
 * from point 'cx,cy'. The currently highest point is stored in
 * 'xresult,yresult' with value 'fresult'. All points must be above
 * 'level' and must be connected to each other. If 'has_center' is TRUE,
 * there is at least one valid point. 'cbuf' is used for bookkeeping.
 */
static void flood_fill_find_center(float *fbuf, char *cbuf, int na, int nb, 
                     float level, int cx, int cy, 
                     float *fresult, int *xresult, int *yresult,
                     int *has_center)
{
    int i,j, istart, istop, jstart, jstop;

    if (fbuf[cx + cy * na] < level) {
        cbuf[cx + cy * na] = NOPEAK;
        if (*has_center)
            return;
    }
    else {
        cbuf[cx + cy * na] = ISPEAK;
        *has_center = TRUE;
    }
    if (*fresult < fbuf[cx + cy * na]) {
        *fresult = fbuf[cx + cy * na];
        *xresult = cx;
        *yresult = cy;
    }
    istart = max(0,    cx - 1);
    istop  = min(na-1, cx + 1);
    jstart = max(0,    cy - 1);
    jstop  = min(nb-1, cy + 1);
    for (i=istart;i<=istop;i++) 
        for (j=jstart;j<=jstop;j++) 
            if (i == cx || j == cy)
                if (cbuf[i + j * na] == UNKNOWN)
                    flood_fill_find_center(fbuf, cbuf, na, nb, level,
                           i, j, fresult, xresult, yresult, has_center);
}

/*
 * Turn the peak-area in 'cbuf' into a convex shape. Fill all the
 * holes and dents. Return the integral in 'fresult' and the number of
 * valid points in 'icount'.
 */
static void convex_flood_fill_integrate(float *fbuf, char *cbuf, 
                                int na, int nb, float *fresult, int *icount)
{
    int i, j, i1, i2, j1, j2;

    for (j=0;j<nb;j++) {
        i1 = i2 = 0;
        for (i=0;i<na;i++) {
            if (cbuf[i + j * na] == ISPEAK) {
                i1 = i;
                break;
            }
        }
        for (i=na-1;i>i1;i--) {
            if (cbuf[i + j * na] == ISPEAK) {
                i2 = i;
                break;
            }
        }
        if (i2 > i1 + 1) {
            for (i=i1+1;i<i2;i++) {
                if (cbuf[i + j * na] != ISPEAK) {
                    cbuf[i + j * na] = ISPEAK;
                    *fresult += fbuf[i + j * na];
                    *icount  += 1;
                }
            }
        }
    }
    for (i=0;i<na;i++) {
        j1 = j2 = 0;
        for (j=0;j<nb;j++) {
            if (cbuf[i + j * na] == ISPEAK) {
                j1 = j;
                break;
            }
        }
        for (j=nb-1;j>j1;j--) {
            if (cbuf[i + j * na] == ISPEAK) {
                j2 = j;
                break;
            }
        }
        if (j2 > j1 + 1) {
            for (j=j1+1;j<j2;j++) {
                if (cbuf[i + j * na] != ISPEAK) {
                    cbuf[i + j * na] = ISPEAK;
                    *fresult += fbuf[i + j * na];
                    *icount  += 1;
                }
            }
        }
    }
}

/*
 * Going from point 'cx,cy' find all connected points that are higher
 * than 'level'. The result is reflected in the 'bitmap' 'cbuf'.
 * 'fresult' contains the sum of all valid points and 'icount' contains 
 * the number of valid points
 */
static void flood_fill_integrate(float *fbuf, char *cbuf, int na, int nb, 
               float level, int cx, int cy, float *fresult, int *icount)
{
    int i,j, istart, istop, jstart, jstop;

    if (fbuf[cx + cy * na] < level) {
        cbuf[cx + cy * na] = NOPEAK;
        return;
    }
    cbuf[cx + cy * na] = ISPEAK;
    *fresult += fbuf[cx + cy * na];
    *icount  += 1;
    istart = max(0,    cx - 1);
    istop  = min(na-1, cx + 1);
    jstart = max(0,    cy - 1);
    jstop  = min(nb-1, cy + 1);
    for (i=istart;i<=istop;i++) 
        for (j=jstart;j<=jstop;j++) 
            if (i == cx || j == cy)
                if (cbuf[i + j * na] == UNKNOWN)
                    flood_fill_integrate(fbuf, cbuf, na, nb, level,
                           i, j, fresult, icount);
}

/*
 * 2D integration routine
 * Peaks can consist out of more than one area.
 *
 * idev = input device
 * xdata = scratch data area
 * floodfill = if TRUE, use floodfill integration
 * ilowb, ihighb = lowest and highest channel in y direction
 * ilowa, ihigha = lowest and highest channel in x direction
 * icentb, icenta = array with max (or min) location (center)
 * ncenter = number of data in icentb, icenta
 * zoint = if TRUE, use offset correction
 * zo = return the offset used
 * level = level for floodfill method
 * sum = return integral
 * convex = if TRUE and floodfill=TRUE, use convex floodfill method
 * cbuf_return = if cbuf_return not NULL, return the bitmap cbuf in
 *               *cbuf_return (do not free it)
 * read2d = function to use for reading data
 * userdata = data used by read2d
 */
int integrate2d(int idev, float *xdata, int floodfill,
                int ilowb, int ihighb, int ilowa, int ihigha,
                int *icentb, int *icenta, int ncenter, 
                int zoint, float *zo, float *level, float *sum, 
                int convex, char **cbuf_return, 
                int (*read2d)(void*,int,float*,int,int), void *userdata)
{
    int i, j, nb, na;
    float *fbuf, fresult, fmax, border;
    char *cbuf;
    int ipos, jpos, icount, has_center;

    if (cbuf_return != NULL)
        *cbuf_return = NULL;
    *sum  = 0.0;
    *zo   = 0.0;

    if (!okarea2 (ilowb,ihighb,ilowa,ihigha))
        return FALSE;

    nb    = (ihighb-ilowb+1);
    na    = (ihigha-ilowa+1);

    border = 0.0;
    icount = 0;

    fbuf = (float*) malloc(sizeof(float) * na * nb);
    if (fbuf == NULL)
        return FALSE;
    cbuf = (char*) calloc(sizeof(char), na * nb);
    if (cbuf == NULL) {
        free (fbuf);
        return FALSE;
    }

    fmax = 0.0;
    ipos = jpos = 0;
    /*
     * Read area
     */
    for (j=ilowb; j<=ihighb; j++) {
        float *xp;
        read2d(userdata, idev, xdata, ihigha, j);
        xp = xdata + ilowa - 1;
        if (level[0] < 0.0)
            for (i=ilowa-1; i<ihigha; i++)
                xdata[i] = -xdata[i];
        if (zoint) {
            if (j == ilowb) {
                for (i=ilowa-1; i<ihigha; i++)
                    border += xdata[i];
            }
            if (j == ihighb) {
                for (i=ilowa-1; i<ihigha; i++)
                    border += xdata[i];
            }
            border += xdata[ilowa-1];
            border += xdata[ihigha-1];
        }

        for (i=0;i<na;i++) {
            fbuf[i+(j-ilowb)*na] = *xp;
            *sum += *xp;
            if (*xp > fmax) {
                fmax = *xp;
                ipos = i;
                jpos = j-ilowb;
            }
            xp++;
        }
    }
    
    fresult = 0.0;
    has_center = FALSE;
    if (*icenta <= 0 && *icentb <= 0) {
        ncenter = 0;
        ipos = na / 2;
        jpos = nb / 2;
    }
    else {
        ipos = *icenta;
        jpos = *icentb;
    }
    if (ncenter == 0) {
        flood_fill_find_center(fbuf, cbuf, na, nb, 
                     fabs(level[0]), ipos, jpos, 
                     &fresult, &ipos, &jpos,
                     &has_center);
        if (has_center)
            ncenter = 1;
        *icenta = ipos;
        *icentb = jpos;
    }
    if (floodfill) {
        
        fresult = 0.0;
        memset (cbuf, 0, sizeof(char) * na * nb);
        for (i=0;i<ncenter;i++)
            flood_fill_integrate(fbuf, cbuf, na, nb, fabs(level[i]),
                           icenta[i], icentb[i], &fresult, &icount);
        if (convex)
            convex_flood_fill_integrate(fbuf, cbuf, na, nb, 
                   &fresult, &icount);

#ifdef do_debug
        printf("\n");
        for (j=nb-1;j>=0;j--) {
            for (i=0;i<na;i++) 
                if (cbuf[i + j * na] == 2)
                    printf("+");
                else if (cbuf[i + j * na] == 1)
                    printf("-");
                else
                    printf(".");
            printf("\n");
        }
        printf("\n");
#endif

        *sum = fresult;
    }
    else
        icount = na * nb;

    if (icount > 0) {
        *zo = (icount * border)/((na + nb) * 2);
        *sum -= *zo;
    }

    if (level[0] < 0.0) {
        *zo  = -(*zo);
        *sum = -(*sum);
    }
    free(fbuf);
    if (floodfill && cbuf_return != NULL) 
        *cbuf_return = cbuf;
    else
        free(cbuf);
    return TRUE;
}

