/********************************************************************/
/*                            contour.c                             */
/* routines hpcntr and contour_plot                                 */
/* if needed 'hpcntr' can be replaced by 'contour_plot'             */
/********************************************************************/
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdarg.h>
#include <math.h>
#include <assert.h>
#include "mathtool.h"
#include "nmrtool.h"


/*
c     ==============================================
c
c
c***************************************
c
c                  contour routine
c
c***************************************
c
c  a is function to be contoured, dimensioned a(m,n) or a(m*n)
c  m = number of rows of a
c  n = number of columns of a
c  bits is logical*1 array dimensioned at least 2*m*n in calling routine
c  cont  array of ncont contour levels
c  lcol  array of ncont contour level colours
c  x,y  arrays of dimension iptmax used in subroutine to store contour l
c
c  idiv = number of line segments between contour level crossings in uni
c     cell.  if >= 1, then routine finds idiv-1 coordinates between cros
c     by linear interpolation on the unit cell.
c  contur output via calls to subroutine plotfunc ()
c     x,y are coordinates with 0<=x<m-1 and 0<=y<n-1.
c  subroutine levelfunc() is called to set up plotting style for contour
c
c
*/
void hpcntr(float *a, char *bits, int m, int n, float *cont, 
            int *lcol, int ncont, int xoff, int yoff, float pxsc, float pysc,
            void (*plotfunc)(int, void*, float, float, float, float),
            void (*levelfunc)(int, int*))
{
    int   idir[4]  = {2,3,0,1},   /*{3,4,1,2}, */
          iside[5] = {0,1,2,3,0}, /*{1,2,3,4,1}, */
          ivct[4]  = {0,-1,0,1}, 
          jvct[4]  = {-1,0,1,0}, 
          kvct[4]  = {3,0,1,2}, /*{4,1,2,3}, */
          lvct[4]  = {1,2,3,0}, /*{2,3,4,1},*/
          nvct[4];
    float side[4] = {0.,0.,1.0,1.0}, c[4];
    int   i, j, nx, ny, idiv, mn, mn2, div, icont, iround;
    int   iptmax = 1024;
    float cl, clold;
    LIST  *list;
    
    nx     = m-1;
    ny     = n-1;
    clold  = 1.e28;
    idiv   = 1;
    mn     = m * n;
    mn2    = 2 * mn;
    div    = idiv;
    list = (LIST*) malloc(sizeof(LIST) * iptmax);
    assert(list);
    /*
     * loop contour levels cl
     */

    for (icont=0;icont<ncont;icont++) {
        int   k1,k2,k3,k4,k5,k6,k7;
        int   i1,i2,i3,i4;
        float c1,c2,c3,c4,c5,c6,c7,c8;
        float a1,a2,a3,a4;
        int   ipt, nn, mm;
        int   interp, irs, iexit, jexit, ient, jrtn, icnt, jcnt;
        int   isave, jsave, isub, jsub, isubsv, jsubsv, irtn, iopp;
        float xstart, ystart, xsave, ysave, xfin, yfin, xinc, yinc, 
              xbase, ybase;

        cl = cont[icont];
        /*
         * set colour index for this level
         */
        levelfunc(icont,lcol);

        for (i=0;i<mn2;i++)
            bits[i] = 'f';
        nvct[0] = 0;
        nvct[1] = mn;
        nvct[2] = m;
        nvct[3] = mn+1;
        ipt = 0;
        mm  = m-1;
        nn  = n-1;
        /*
         *     search for contour crossing between adjacent column of array a(i,j
         */
        i    = 0;
        j    = 1;
        isub = 0-1;  
        jsub = 0-1;
        irtn = 1-1;
        
        while (j <= n) {       
l110:      for (;;) {
               if (i >= mm) 
                    goto l130;
                i++;
                isub++;
                jsub++;
                if (a[isub]-cl == 0.0)
                    goto l600;
                if (a[isub]-cl < 0.0) { 
                    if (a[isub+1]-cl <= 0.0) 
                        continue;     
                }
                else if (a[isub+1]-cl >= 0.0) 
                    continue; 
                if (bits[jsub+nvct[0]] != 't')
                    break;
            }
            
            xstart = (cl-a[isub])/(a[isub+1]-a[isub]);
            ystart = 0;
            goto l200;
l130:
            i=0;
            isub++;
            jsub++;
            j++;
        }
        /*
         *     search for contour crossing between adjacent rows of array a(i,j)
         */ 
        i    = 0;
        j    = 1;
        jsub = 0-1; 
        isub = 0-1;
        irtn = 2-1;
l150:   if (j > nn) 
            continue;

l160:   for (;;) {
            if (i >= m) {
                i=0;
                j++;
                goto l150;
            }
            i++;
            isub++;
            jsub++;
            if (a[isub]-cl == 0) 
                continue;        
            if (a[isub]-cl < 0) {
                if (a[isub+m]-cl <= 0) 
                    continue;        
            }
            else if (a[isub]-cl > 0) {
                if (a[isub+m]-cl >= 0) 
                    continue;       
            }
            if (bits[jsub+nvct[1]] != 't')
                break;
        }
        ystart = (cl-a[isub])/(a[isub+m]-a[isub]);
        xstart = 0;
        /*
         * begin following contour line... save indices for return to search
         */
l200:   isave  = i;
        jsave  = j;
        isubsv = isub;
        jsubsv = jsub;
        xsave  = xstart;
        ysave  = ystart;
        list[0].x = xstart + (float)(i-1);
        list[0].y = ystart + (float)(j-1);
        ient   = irtn;
        irs    = 0;
        goto l250;
        /*
         * dump line and follow contour line on opposite side of starting pio
         * when used a second time this entry returns to search
         */
l205:   irs = 1;


l210:   if (ipt > 0) 
            plotfunc(ipt+1,(void*)list,xoff,yoff,pxsc,pysc);
        ipt    = 0;
        i      = isave;
        j      = jsave;
        isub   = isubsv;
        jsub   = jsubsv;
        xstart = xsave;
        ystart = ysave;
        list[0].x = xstart + (float)(i-1);
        list[0].y = ystart + (float)(j-1);
        if (irs != 0) {
            if (irtn == 0)
                goto l110;
            else if (irtn == 1)
                goto l160;
        }
        iexit = irtn;
        irs = 1;
        goto l240;
        /*
         *   return from following contour line through a cell
         */
l230:   if (bits[jsub+nvct[iexit]] == 't')  
            goto l205;
l240:   i += ivct[iexit];
        j += jvct[iexit];
        jsub = i + (j-1) * m;
        jsub -= 1;
        isub = jsub;
        ient = idir[iexit];
l250:   bits[jsub+nvct[ient]] = 't';
        if (i < 1 || i > mm || j < 1 || j > nn)  
            goto l210;
        /*
         *       find contour crossing in new cell
         */
        if (isub+1 >= mn || isub+m  >= mn || isub+1+m >= mn)  
            goto l210;
        c[0] = a[isub+1];
        c[1] = a[isub];
        c[2] = a[isub+m];
        c[3] = a[isub+1+m];
        jrtn = 1;
        icnt = 1;
        jcnt = 1;

        for (iround=0;iround<4;iround++) {
            if (iround == ient) 
                continue; 
            i1 = iside[iround];
            i2 = iside[iround+1];

            if (c[i1]-cl == 0.0) {
                jexit = iround;
                jcnt++;
            }
            else {
                if (c[i1]-cl < 0.0) {      
                    if (c[i2]-cl <= 0.0) 
                        continue;        
                }
                else  {
                    if (c[i2]-cl >= 0.0)
                        continue;        
                }
                iexit = iround;
                icnt++;
            }
        }
        switch (jcnt) {
        case 1:
            switch (icnt) {
            case 1:
            case 3:
                goto l210;
            case 2:
                goto l320;
            case 4:
                goto l800;
            }
        case 2:
            switch (icnt) {
            case 1:
                goto l710;
            case 2:
                goto l320;
            case 3:
            case 4:
                goto l210;
            }
        case 3:
            goto l700;
        case 4:
            goto l210;
        }

l320:
        switch (ient) {
        case 0:
            switch (iexit) {
            case 0:
                goto l210;
            case 1:
            case 3:
                goto l410;
            case 2:
                goto l500;
            }
        case 1:
            switch (iexit) {
            case 0:
            case 2:
                goto l510;
            case 1:
                goto l210;
            case 3:
                goto l400;
            }
        case 2:
            switch (iexit) {
            case 0:
                goto l500;
            case 1:
            case 3:
                goto l410;
            case 2:
                goto l210;
            }
        case 3:
            switch (iexit) {
            case 0:
            case 2:
                goto l510;
            case 1:
                goto l400;
            case 3:
                goto l210;
            }
            break;
        }
        /*
         *     follow contour line across a cell to a side
         */
l400:   xstart = side[ient];
l410:   xfin   = side[iexit];
        xinc   = (xfin-xstart)/div;
        xbase  = (float)(i-1);
        ybase  = (float)(j-1);
        a1 = cl-c[1];
        a2 = c[0]-c[1];
        a3 = c[2]-c[1];
        a4 = c[1]-c[0]+c[3]-c[2];
        for (interp=0;interp<idiv;interp++) {
            xstart += xinc;
            ystart = (a1-a2*xstart)/(a3+a4*xstart);
            if (ipt + 1 == iptmax)  {
                plotfunc(ipt+1,(void*)list,xoff,yoff,pxsc,pysc);
                list[0].x = list[ipt].x;
                list[0].y = list[ipt].y;
                ipt  = 0;
            }
            ipt++;
            list[ipt].x = xbase+xstart;
            list[ipt].y = ybase+ystart;
        }
        switch (jrtn) {
        case 1:
            goto l230;
        case 2:
            goto l210;
        case 3:
            goto l615;
        case 4:
            goto l635;
        }


l500:   ystart = side[ient];
        /*
         *       follow contour line across a cell to a top or bottom
         */
l510:   yfin  = side[iexit];
        xbase = (float)(i-1);
        yinc  = (yfin-ystart)/div;
        ybase = (float)(j-1);
        a1    = cl-c[1];
        a2    = c[2]-c[1];
        a3    = c[0]-c[1];
        a4    = c[1]-c[0]+c[3]-c[2];

        for (interp=0;interp<idiv;interp++) {
            ystart += yinc;
            xstart = (a1-a2*ystart)/(a3+a4*ystart);
            if (ipt + 1 == iptmax) {
                plotfunc(ipt+1,(void*)list,xoff,yoff,pxsc,pysc);
                list[0].x = list[ipt].x;
                list[0].y = list[ipt].y;
                ipt  = 0;
            }
            ipt++;
            list[ipt].x = xbase + xstart;
            list[ipt].y = ybase + ystart;
        }
        switch (jrtn) {
        case 1:
            goto l230;
        case 2:
            goto l210;
        case 3:
            goto l615;
        case 4:
            goto l635;
        }
        
        /*
         *       follow contour line from corner to corner
         */
l600:   k1 = isub-m;
        k2 = isub+1-m;
        k3 = isub+1;
        k4 = isub+1+m;
        k5 = isub+m;
        k6 = isub-1+m;
        k7 = isub-1;
        c1 = a[k1];
        c2 = a[k2];
        c3 = a[k3];
        c4 = a[k4];
        c5 = a[k5];
        c6 = a[k6];
        c7 = a[k7];
        c8 = a[isub];
        if (isub < (1-1)  ||  isub > (mn-1)) 
            goto l640;
        list[0].x = (float)(i-1);
        list[0].y = (float)(j-1);
        if (!((j == 1  ||  j == m)   ||
            (k1 < 0  ||  k1 >= mn)  ||
            (k2 < 0  ||  k2 >= mn)  ||
            (k3 < 0  ||  k3 >= mn)  ||
            (k4 < 0  ||  k4 >= mn)  ||
            (k5 < 0  ||  k5 >= mn)  ||
            (c3 != cl)  ||
            (c1 == cl && c2 == cl && c4 == cl && c5 == cl)))  {
                list[1].x = list[0].x + 1.0;
                list[1].y = list[0].y;
                plotfunc (2,(void*)list,xoff,yoff,pxsc,pysc);
                goto l620;
        }
        if ((j == 1)  ||
            (k1 < 0  ||  k1 >= mn)  ||
            (k2 < 0  ||  k2 >= mn)  ||
            (k3 < 0  ||  k3 >= mn)  ||
            (c2 !=  cl)  ||
            (c1 == cl  ||  c3 == cl)  ||
            (c1  >  cl && c3  >  cl  ||  c1  <  cl && c3  <  cl))  
                goto l620;
        c[0] = c2;
        c[1] = c1;
        c[2] = c8;
        c[3] = c3;
        j++;
        jrtn  = 3;
        ient  = 3-1;
        iexit = 1-1;
        goto l500;
l615:   if (ipt > 0) 
            plotfunc (ipt+1,(void*)list,xoff,yoff,pxsc,pysc);
        ipt = 0;
        j++;
        list[0].x = (float)(i-1);
        list[0].y = (float)(j-1);
l620:   if (!((j == m || i == 1)   ||
            (k3 < 0 || k3 >= mn)  ||
            (k4 < 0 || k4 >= mn)  ||
            (k5 < 0 || k5 >= mn)  ||
            (k6 < 0 || k6 >= mn)  ||
            (k7 < 0 || k7 >= mn)  ||
            (c5 != cl)           ||
            (c3 == cl && c4 == cl && c6 == cl && c7 == cl))) {   
                list[1].x = list[0].x;
                list[1].y = list[0].y + 1.0;
                plotfunc( 2,(void*)list,xoff,yoff,pxsc,pysc);
                goto l640;
        }
        if ((j == m)  ||
            (k3 < 0 || k3 >= mn)  ||
            (k4 < 0 || k4 >= mn)  ||
            (k5 < 0 || k5 >= mn)  ||
            (c4 != cl)  ||
            (c3 == cl || c5 == cl)  ||
            (c3  >  cl && c5  >  cl ||
             c3  <  cl && c5  <  cl))
                 goto l640;
        c[0]  = c3;
        c[1]  = c8;
        c[2]  = c5;
        c[3]  = c4;
        jrtn  = 4;
        ient  = 1-1;
        iexit = 3-1;
        goto l500;
l635:   if (ipt > 0) 
            plotfunc (ipt+1,(void*)list,xoff,yoff,pxsc,pysc);
        ipt = 0;
        list[0].x = (float)(i-1);
        list[0].y = (float)(j-1);
l640:   if (irtn == 0)
            goto l110;
        else if (irtn == 1)
            goto l160;
        /*
         *    follow contour line from side to corner or corners
         */
l700:   jrtn  = 2;
        iopp  = idir[ient];
        i1    = iside[iopp];
        i2    = iside[iopp+1];
        iexit = iopp;
        c[i1] = c[kvct[i1]];
        c[i2] = c[lvct[i2]];
        goto l320;
l710:   jrtn  = 2;
        iexit = jexit;
        goto l320;
        /*
         *     follow contour line through saddle point
         */
l800:   iopp = idir[ient];
        i1 = iside[ient];
        c1 = c[i1];
        i2 = iside[ient+1];
        c2 = c[i2];
        i3 = iside[iopp];
        c3 = c[i3];
        i4 = iside[iopp+1];
        c4 = c[i4];
        if ((c1-cl)/(c1-c2) != (c4-cl)/(c4-c3)) { 
            if ((c1-cl)/(c1-c4) > (c2-cl)/(c2-c3))  
                iexit = i2;
            else
                iexit = i4;
        }
        else {
            c[i3] = c[i2];
            c[i4] = c[i1];
            iexit = i3;
        }
        goto l320;

    }
    free(list);
}


/****************************************************************************/
/* 


DDJ


June, 1992
CONTOURING DATA FIELDS

Maps are simply an application of gridded data formats

Bruce (Bear) Giles

 */

#define     NaN         0xFFFFFFFF
#define     isnanf(x)   ((x) == NaN)


#ifndef linux
typedef unsigned    int   uint;
#endif


#ifndef uchar
typedef unsigned    char    uchar;
#endif

/* Mnemonics for contour line bearings  */
#define EAST        0
#define NORTH       1
#define WEST        2
#define SOUTH       3

/* Mnemonics for relative data point positions  */
#define SAME        0
#define NEXT        1
#define OPPOSITE    2
#define ADJACENT    3

/* Bit-mapped information in 'map' field.  */
#define EW_MAP      0x01
#define NS_MAP      0x02


typedef struct {
    int     do_peakpick;
    int     dim_x;          /*  dimensions of grid array... */
    int     dim_y;
    int     off_x, off_y;
    float   scale_x, scale_y;
    float   mean;
    int     contour_mode;   /*  control variable            */
    int     nlevel;         /*  number of contour levels    */
    float   *data;          /*  pointer to grid data        */
    float   *clevel;        /*  pointer to contour levels   */
    char    *map;           /*  pointer to "in-use" map     */
    LIST    *list;          /*  used by 'Polyline()'        */
    int     *lcol;          /*  used by 'NewLevel()'        */
    void    *userdata;
    void    (*plotfunc)(int, void*, float, float, float, float);
    void    (*levelfunc)(int, int*);
    uint    count;
}   GRID;

typedef struct {
    int   x;
    int   y;
    uchar   bearing;
}   POINT;

#define MXY_to_L(g,x,y)     ((uint) (y) * (g)->dim_x + (uint) (x) + 1)
#define XY_to_L(g,x,y)      ((uint) (y) * (g)->dim_x + (uint) (x))

int    scaleData (GRID *grid);
static void    startLine (GRID *grid);
static void    startEdge (GRID *grid, float level, uchar bearing);
static void    startInterior (GRID *grid, float level);
static void    drawLine (GRID *grid, POINT *point, float level);
static void    markInUse (GRID *grid, POINT *point, uchar face);
static uchar   faceInUse (GRID *grid, POINT *point, uchar face);
static void    initPoint (GRID *grid);
static void    lastPoint (GRID *grid);
static uchar   savePoint (GRID *grid, POINT *point, float level);
static float   getDataPoint (GRID *grid, POINT *point, uchar corner);

void contour_plot(float *data, int dim_x, int dim_y, float *clevel, int levels,
                  int off_x, int off_y, float scale_x, float scale_y, 
                  int  *lcol, void *userdata, int do_peakpick,
                  void (*plotfunc)(int, void*, float, float, float, float),
                  void (*levelfunc)(int, int*))
{
    GRID    grid;
    int     i;

    memset ((void*) &grid, 0, sizeof(GRID));
    grid.do_peakpick = do_peakpick;
    grid.data        = data;
    grid.clevel      = clevel;
    grid.nlevel      = levels;
    grid.dim_x       = dim_x;
    grid.dim_y       = dim_y;
    grid.off_x       = off_x;
    grid.off_y       = off_y;
    grid.scale_x     = scale_x;
    grid.scale_y     = scale_y;
    grid.lcol        = lcol;
    grid.userdata    = userdata;
    grid.plotfunc    = plotfunc;
    grid.levelfunc   = levelfunc;

    /* 
     * Allocate buffers used to contain contour information 
     */
    if ((grid.map = (char*) malloc ((dim_x + 1) * dim_y)) == NULL) {
        fprintf (stderr, "Contour(): unable to allocate buffer! (%d bytes)\n",
            (dim_x + 1) * dim_y * sizeof (LIST));

        return;
    }
    grid.list = (LIST *) malloc (2 * dim_x * dim_y * sizeof (LIST));
    if (grid.list == (LIST  *) NULL) {
        fprintf (stderr, "Contour(): unable to allocate buffer! (%d bytes)\n",
            2 * dim_x * dim_y * sizeof (LIST));

        free ((char *) grid.map);
        return;
    }
    /* 
     * Generate contours, if not a uniform field. 
     */
    if (scaleData (&grid))
        startLine (&grid);
    /* Release data structures. */
    free ((char *) grid.map);
    free ((char *) grid.list);
}

/* 
 * scaleData--Determine necessary statistics for contouring data set: global 
 * maximum & minimum, etc. Then initialize items used by rest of module. 
 */
int scaleData (GRID *grid)
{
/*
//    if (flag_min)
//        grid->max_value   = -emin;
//    else
//        grid->max_value   = emax;
//    grid->first_level = grid->max_value - ((float) levels * inc);
//    grid->step = inc;
*/
    return  1;
}

#ifdef not_used
/* 
 * scaleData--Determine necessary statistics for contouring data set: global
 * maximum & minimum, etc. Then initialize items used by rest of module. 
 */
int scaleData (GRID *grid, double inc)
{
    uint i;
    float step, level;
    float sum, sum2, count;
    float p, *u, *v, r;
    char *s;
    int n1, n2;
    int first, n;
    long x;

    sum = sum2 = count = 0.0;

    first = 1;
    s = grid->map;
    u = grid->data;
    v = u + grid->dim_x * grid->dim_y;
    for (i = 0; i < grid->dim_x * grid->dim_y; i++, u++, v++, s++) {
        r = *u;
        sum += r;
        sum2 += r * r;
        count += 1.0;

        if (first) {
            grid->max_value = grid->min_value = r;
            first = 0;
        }
        else if (grid->max_value < r)
            grid->max_value = r;
        else if (grid->min_value > r)
            grid->min_value = r;
    }
    grid->mean = sum / count;
    if (grid->min_value == grid->max_value)
        return 0;
    grid->std = sqrt ((sum2 - sum * sum /count) / (count - 1.0));
    if (inc > 0.0) {
        /* Use specified increment */
        step = inc;
        n = (int) (grid->max_value - grid->min_value) / step + 1;

        while (n > 40) {
            step *= 2.0;
            n = (int) (grid->max_value - grid->min_value) / step + 1;
        }
    }
    else {
    /* Choose [specifiedreasonable] number of levels and normalize
    * increment to a reasonable value. */
        n = (inc == 0.0) ? DEFAULT_LEVELS : (int) fabs (inc);

        step = 4.0 * grid->std / (float) n;
        p = pow (10.0, floor (log10 ((double) step)));
        step = p * floor ((step + p / 2.0) / p);
    }
    n1 = (int) floor (log10 (fabs (grid->max_value)));
    n2 = -((int) floor (log10 (step)));

    if (n2 > 0)
        sprintf (grid->format, "%%%d.%df", n1 + n2 + 2, n2);
    else
        sprintf (grid->format, "%%%d.0f", n1 + 1);
    if (grid->max_value * grid->min_value < 0.0)
        level = step * floor (grid->mean / step); /* odd */
    else
       level = step * floor (grid->min_value / step);
    level -= step * floor ((float) (n - 1)/ 2);

    /* Back up to include add'l levels, if necessary */
    while (level - step > grid->min_value)
        level -= step;

    grid->first_level = level;
    grid->step = step;
    return 1;
}
#endif

/* 
 * startLine -- Locate first point of contour lines by checking edges of 
 * gridded data set, then interior points, for each contour level. 
 */
static void startLine (GRID *grid)
{
    uint  idx, i, edge;
    double  level;

/*
//    for (idx = 0, level = grid->first_level; level < grid->max_value; 
//        level += grid->step, idx++) {
*/
    for (idx = 0; idx < grid->nlevel; idx++) {
        level = grid->clevel[idx];
        if (!grid->do_peakpick)
            grid->levelfunc(idx, grid->lcol);
        /* Clear flags */
        grid->contour_mode = 1; /* (level >= grid->mean); */
        memset (grid->map, 0, grid->dim_x * grid->dim_y);

        /* Check edges */
        for (edge = 0; edge < 4; edge++)
            startEdge (grid, level, edge);
        /* Check interior points */
        startInterior (grid, level);
    }
}

/* 
 * startEdge -- For a specified contour level and edge of gridded data set,
 *  check for (properly directed) contour line. 
 */
static void startEdge (GRID *grid, float level, uchar bearing)
{
    POINT   point1, point2;
    float   last, next;
    int     i, ds;

    switch (point1.bearing = bearing) {
        case EAST:
            point1.x = 0;
            point1.y = 0;
            ds = 1;
            break;
        case NORTH:
            point1.x = 0;
            point1.y = grid->dim_y - 2;
            ds = 1;
            break;
        case WEST:
            point1.x = grid->dim_x - 2;
            point1.y = grid->dim_y - 2;
            ds = -1;
            break;
        case SOUTH:
            point1.x = grid->dim_x - 2;
            point1.y = 0;
            ds = -1;
            break;
    }
    switch (point1.bearing) {
            /* Find first point with valid data. */
        case EAST:
        case WEST:
            next = getDataPoint (grid, &point1, SAME);
            memcpy ((void *) &point2, (void *) &point1, sizeof (POINT));
            point2.x -= ds;

            for (i = 1; i < grid->dim_y; i++, point1.y = point2.y += ds) {
                last = next;
                next = getDataPoint (grid, &point1, NEXT);
                if (last >= level && level > next) {
                    drawLine (grid, &point1, level);
                    memcpy ((void *) &point1, (void *) &point2, sizeof (POINT));
                    point1.x = point2.x + ds;
                }
            }
            break;
            /* Find first point with valid data. */
        case SOUTH:
        case NORTH:
            next = getDataPoint (grid, &point1, SAME);
            memcpy ((char *) &point2, (char *) &point1, sizeof (POINT));
            point2.y += ds;

            for (i = 1; i < grid->dim_x; i++, point1.x = point2.x += ds) {
                last = next;
                next = getDataPoint (grid, &point1, NEXT);
                if (last >= level && level > next) {
                    drawLine (grid, &point1, level);

                    memcpy ((void *) &point1, (void *) &point2, sizeof (POINT));
                    point1.y = point2.y - ds;
                }
            }
            break;
    }
}

/* 
 * startInterior -- For a specified contour level, check for (properly 
 * directed) contour line for all interior data points. Do _not_ follow contour 
 * lines detected by the 'startEdge' routine. 
 */
static void  startInterior (GRID *grid, float level)
{
    POINT   point;
    uint    x, y;
    float   next, last;
    for (x = 1; x < grid->dim_x - 1; x++) {
        point.x = x;
        point.y = 0;
        point.bearing = EAST;
        next = getDataPoint (grid, &point, SAME);
        for (y = point.y; y < grid->dim_y; y++, point.y++) {
            last = next;
            next = getDataPoint (grid, &point, NEXT);
            if (last >= level && level > next) {
                if (!faceInUse (grid, &point, WEST)) {
                    drawLine (grid, &point, level);
                    point.x = x;
                    point.y = y;
                    point.bearing = EAST;
                }
            }
        }
    }
}

/* 
 * drawLine -- Given in initial contour point by either 'startEdge' or 
 * 'startInterior', follow contour line until it encounters either an edge 
 * or previously contoured cell.
 */
static void drawLine (GRID *grid, POINT *point, float level)
{
    uchar   exit_bearing;
    uchar   adj, opp;
    float   fadj, fopp;

    initPoint (grid);

    for ( ;; ) {
        /* 
         * Add current point to vector list. If either of the points is 
         * missing, return immediately (open contour). 
         */
        if (!savePoint (grid, point, level)) {
            lastPoint (grid);
            return;
        }
        /*
         * Has this face of this cell been marked in use? If so, then this is 
         * a closed contour. 
         */
        if (faceInUse (grid, point, WEST)) {
            lastPoint (grid);
            return;
        }
        /* 
         * Examine adjacent and opposite corners of cell; determine 
         * appropriate action. 
         */
        markInUse (grid, point, WEST);

        fadj = getDataPoint (grid, point, ADJACENT);
        fopp = getDataPoint (grid, point, OPPOSITE);

        /* 
         * If either point is missing, return immediately (open contour). 
         */
        if (isnanf (fadj) || isnanf (fopp)) {
            lastPoint (grid);
            return;
        }
        adj = (fadj <= level) ? 2 : 0;
        opp = (fopp >= level) ? 1 : 0;
        switch (adj + opp) {
            /* Exit EAST face. */
            case 0: 
                markInUse (grid, point, NORTH);
                markInUse (grid, point, SOUTH);
                exit_bearing = EAST;
                break;
            /* Exit SOUTH face. */
            case 1:
                markInUse (grid, point, NORTH);
                markInUse (grid, point, EAST);
                exit_bearing = SOUTH;
                break;
            /* Exit NORTH face. */
            case 2:
                markInUse (grid, point, EAST);
                markInUse (grid, point, SOUTH);
                exit_bearing = NORTH;
                break;
            /* Exit NORTH or SOUTH face, depending upon contour level. */
            case 3:
                exit_bearing = (grid->contour_mode) ? NORTH : SOUTH;
                break;
        }
        /* Update face number, coordinate of defining corner. */
        point->bearing = (point->bearing + exit_bearing) % 4;
        switch (point->bearing) {
            case EAST :
                point->x++;
                break;
            case NORTH:
                point->y--;
                break;
            case WEST :
                point->x--;
                break;
            case SOUTH:
                point->y++;
                break;
        }
    }
}

/*
 *  markInUse -- Mark the specified cell face as contoured. This is necessary to
 *  prevent infinite processing of closed contours. see also:  faceInUse 
 */
static void markInUse (GRID *grid, POINT *point, uchar face)
{
    face = (point->bearing + face) % 4;
    switch (face) {
        case NORTH:
        case SOUTH:
            grid->map[MXY_to_L (grid,
                point->x, point->y + (face == SOUTH ? 1 : 0))] |= NS_MAP;
            break;
        case EAST:
        case WEST:
            grid->map[MXY_to_L (grid,
                point->x + (face == EAST ? 1 : 0), point->y)] |= EW_MAP;
            break;
    }
}

/*
 * faceInUse -- Determine if the specified cell face has been marked as 
 * contoured. This is necessary to prevent infinite processing of closed 
 * contours. see also: markInUse
 */
static uchar faceInUse (GRID *grid, POINT *point, uchar face)
{
    uchar   r;
    face = (point->bearing + face) % 4;
    switch (face) {
        case NORTH:
        case SOUTH:
            r = grid->map[MXY_to_L (grid,
                    point->x, point->y + (face == SOUTH ? 1 : 0))] & NS_MAP;
            break;
        case EAST:
        case WEST:
            r = grid->map[MXY_to_L (grid,
                    point->x + (face == EAST ? 1 : 0), point->y)] & EW_MAP;
            break;
    }
    return r;
}


/* 
 * initPoint -- Initialize the contour point list. 
 *  see also: savePoint, lastPoint  
 */
static void initPoint (GRID *grid)
{
    grid->count = 0;
}


/*
 * lastPoint -- Generate the actual contour line from the contour point list.
 *  see also:  savePoint, initPoint
 */
static void lastPoint (GRID *grid)
{
    int   i,j;
    uint  offset;
    float value;
    float xmin,ymin, xmax,ymax;
    float xlow,yhigh, xhigh,ylow;
    float highest, lowest;

    if (!grid->do_peakpick)      {
        Polyline (grid->count, grid->list, grid->off_x, grid->off_y, 
                  grid->scale_x, grid->scale_y);
        return;
    }
    
    if (grid->count) {
        LIST *list = grid->list;
        /*
         * bounding box
         */
        if (list[0].x == list[grid->count-1].x &&
            list[0].y == list[grid->count-1].y) {
            xmin = xmax = list[0].x;
            ymin = ymax = list[0].y;
            for (i=1;i<grid->count;i++) {
                if (list[i].x < xmin)
                    xmin = list[i].x;
                else if (list[i].x > xmax)
                    xmax = list[i].x;
                if (list[i].y < ymin)
                    ymin = list[i].y;
                else if (list[i].y > ymax)
                    ymax = list[i].y;
            }
        }
        else
            return;
        /*
         * get interger grid points
         */
        xmin = floor(xmin);
        xmax = ceil(xmax);
        ymin = floor(ymin);
        ymax = ceil(ymax);
        if (xmin < 0)
            xmin = 0;
        if (xmax >= grid->dim_x)
            xmax = grid->dim_x - 1;
        if (ymin < 0)
            ymin = 0;
        if (ymax >= grid->dim_y)
            ymax = grid->dim_y - 1;

        /*
         * highest and lowest position & value
         */
        xlow = xhigh = xmin;
        ylow = yhigh = ymin;
        offset = XY_to_L(grid, xlow, ylow);
        lowest = highest = grid->data[offset];
        for (i=xmin;i<xmax;i++) {
            for (j=ymin;j<ymax;j++) {
                offset = XY_to_L(grid, i, j);
                value =  grid->data[offset];
                if (value < lowest) {
                    lowest = value;
                    xlow = i;
                    ylow = j;
                }
                else if (value > highest) {
                    highest = value;
                    xhigh = i;
                    yhigh = j;
                }
            }

        }

        /*
         * if value of bounding-box > level
         * this is not a peak but a hole
         */
        offset = XY_to_L(grid, xmin, ymin);
        value =  grid->data[offset];
        if (value > grid->clevel[0])
            return;

        /*
         * At least 2 contour lines to call this a peak
         */
        if ( highest > 2 * grid->clevel[0]) {

            xmin = grid->scale_x * xmin + grid->off_x;
            xmax = grid->scale_x * xmax + grid->off_x;
            ymin = grid->scale_y * ymin + grid->off_y;
            ymax = grid->scale_y * ymax + grid->off_y;

            xhigh = grid->scale_x * xhigh + grid->off_x;
            yhigh = grid->scale_y * yhigh + grid->off_y;

            /*
             * Add peak
             */
            grid->plotfunc((int)grid->clevel[1],grid->userdata,xmin,xmax,ymin,ymax);
        }
    }
}

/* 
 * savePoints -- Add specified point to the contour point list.
 *  see also:  initPoint, lastPoint 
 */
static uchar savePoint (GRID *grid, POINT *point, float level)
{
    float   last, next;
    float   x, y, ds;

    last = getDataPoint (grid, point, SAME);
    next = getDataPoint (grid, point, NEXT);

    /* Are the points the same value? */
    if (last == next) {
/*
        fprintf (stderr, "(%2d, %2d, %d)  ", point->x, point->y,
            point->bearing);
        fprintf (stderr, "%8g  %8g  ", last, next);
        fprintf (stderr, "potential divide-by-zero!\n");

        return 0;
*/

        ds = 0.0;
    }
    else
        ds = (float) ((last - level) / (last - next));

    x = (float) point->x;
    y = (float) point->y;

    switch (point->bearing)  {
        case EAST :
            y += ds;
            break;
        case NORTH:
            x += ds;
            y += 1.0;
            break;
        case WEST :
            x += 1.0;
            y += 1.0 - ds;
            break;
        case SOUTH:
            x += 1.0 - ds;
            break;
    }

    /* Update to contour point list */
    /*
//    grid->list[grid->count].x = x / (float) (grid->dim_x - 1);
//    grid->list[grid->count].y = y / (float) (grid->dim_y - 1);
*/
    grid->list[grid->count].x = x;
    grid->list[grid->count].y = y;

    /* Update counter */
    grid->count++;

    return 1;
}

/*
 *  getDataPoint -- Return the value of the data point in specified corner of 
 *  specified cell (the 'point' parameter contains the address of the
 *  top-left corner of this cell).
 */
static float getDataPoint (GRID *grid, POINT *point, uchar corner)
{
    uint  dx, dy;
    uint  offset;

    switch ((point->bearing + corner) % 4) {
        case SAME:
            dx = 0;
            dy = 0;
            break;
        case NEXT:
            dx = 0;
            dy = 1;
            break;
        case OPPOSITE:
            dx = 1;
            dy = 1;
            break;
        case ADJACENT:
            dx = 1;
            dy = 0;
            break;
    }
    offset = XY_to_L (grid, point->x + dx, point->y + dy);
    if ((int) (point->x + dx) >= grid->dim_x || 
        (int) (point->y + dy) >= grid->dim_y || 
        (int) (point->x + dx) < 0 ||
        (int) (point->y + dy) < 0) {
            return  NaN;
    }
    else
        return grid->data[offset];
}

