/********************************************************************/
/*                         psnd_stackplot.c                         */
/*                                                                  */
/* 1998, Albert van Kuik                                            */
/********************************************************************/


#include <math.h>
#include <stdio.h>
#include <stdlib.h>
#include <assert.h>
#include "genplot.h"
#include "psnd.h"
#include "transform.h"


static MATRIX4F model;            /* transform from world to modelling coordinates */
static MATRIX4F view;             /* transform from modelling to VDC coordinates */
static MATRIX4F composite;        /* transform from world to device coordinates */
static MATRIX4F motion;           /* transform for modelling motion */
/* 
 * Initialize viewing matrix. 
 */
static void init_transforms(float x1, float y1, float width, float height, 
                     int perspective)
{
    float eye_pos, focal_len, near, n_over_d;

    identity_f(view);
    identity_f(composite);

    eye_pos   = 1.0 * max(width,height);
    focal_len = 1.1 * eye_pos;

    width  /= 500;
    height /= 500;

    near     = eye_pos - max(width,height);
    n_over_d = near/ focal_len;

    width  *= n_over_d;
    height *= n_over_d;

    if (perspective)
        frustum_f(view,
              -width , width, 
              -height * 0.5, height * 1.5,
               near , 5 * focal_len);
    else    
       ortho_f(view,
              -width , width, 
              -height * 0.5 , height * 1.5,
               near , 5 * focal_len);

    look_at_f(view, 0.0, eye_pos/10, eye_pos,
                    0.0, 0.0, eye_pos-focal_len,
                    0.0, 1.0, 0.0);

}


/************************************************************/

typedef struct {
    float depth;
    float normal;
    int   skip;
    int   color;
    int   border_color;
    int   num_points;
    int   *point_id;
} POLYHEDRON;

typedef struct {
    float x;
    float y;
} POLYPOINT;

static VERTEXF *points, EE;
static VERTEXF *transformed_points;
static int *polygon_points;
static POLYHEDRON *polygons;
static int poly_count;
static int points_used;



/********************************************************************/
static void transform_all_points(float offx, float offy, float w, float h)
{
    int i;
    VERTEXF t1;
    float scalex,transx,scaley ,transy ;

    scalex = w/1000.0;
    transx = offx+w/2.0;
    scaley = h/1000.0;
    transy = offy+h/4.0;

    for (i = points_used - 1; i >= 0; i--) {
        transform_point_f(composite, points[i], t1);
        transformed_points[i][0] = t1[0] * scalex + transx ;
        transformed_points[i][1] = t1[1] * scaley + transy ;
        transformed_points[i][2] = t1[2] * scaley ;
    }
}


static void add_polygon(int poly_count, POLYHEDRON *poly, 
                int num_points, int *point_id, 
                int color, int border_color)
{
    poly[poly_count].num_points 	= num_points;
    poly[poly_count].point_id     	= point_id;
    poly[poly_count].color      	= color;
    poly[poly_count].border_color       = border_color;
    poly[poly_count].skip       	= FALSE;
}


/*
 * decide which row or column is the most distant
 * Compare the Z value of two vectors (1,0,0) and (0,0,1)
 * after transformation
 */
static void prepare_probe()
{
    points_used=0;
    points             = (VERTEXF*) realloc(points, sizeof(VERTEXF) * 5);
    transformed_points = 
        (VERTEXF*) realloc(transformed_points, sizeof(VERTEXF) * 5);
    points[points_used][0] = 0.0;
    points[points_used][1] = 0.0;
    points[points_used][2] = 0.0;
    points_used++;
    points[points_used][0] = 1.0;
    points[points_used][1] = 0.0;
    points[points_used][2] = 0.0;
    points_used++;
    points[points_used][0] = 0.0;
    points[points_used][1] = 0.0;
    points[points_used][2] = 1.0;
    points_used++;
    points[points_used][0] = -1.0;
    points[points_used][1] = 0.0;
    points[points_used][2] = 0.0;
    points_used++;
    points[points_used][0] = 0.0;
    points[points_used][1] = 0.0;
    points[points_used][2] = -1.0;
    points_used++;
}

/*
 * Return xyorder, after probe has been transformed
 * 0 = Draw rows (x1 .. xmax), highest y first
 * 1 = Draw columns (y1 .. ymax), highest x first
 * 2 = Draw rows, smallest y first
 * 3 = Draw columns, smallest x first
 */
static int get_xyorder()
{
    int xyorder = 0;
    VERTEXF v1,v2;
    if (view[3][2] > 0) {
        diff_vector_f(transformed_points[1],transformed_points[3],v1);
        normalize_f(v1);
        diff_vector_f(transformed_points[2],transformed_points[4],v2);
        normalize_f(v2);
    }
    else {
        diff_vector_f(transformed_points[0],transformed_points[1],v1);
        normalize_f(v1);
        diff_vector_f(transformed_points[0],transformed_points[2],v2);
        normalize_f(v2);
    }


    if (fabs(v2[2]) > fabs(v1[2])) {
        if (v2[2] < 0)
            xyorder = 3;
        else
            xyorder = 1;
        if (v1[2] < 0)
            xyorder *= -1.0;
    }
    else {
        if (v1[2] < 0)
            xyorder = 4;
        else
            xyorder = 2;
        if (v2[2] < 0)
            xyorder *= -1.0;
    }
    return xyorder;
}


static void prep_xaxis(
    float scalex, float scaley, float scale_tick,
    int offx,  int offy,  
    int startx,   int stopx,
    float tick1,  float tickstep)

{
    int n;
    int sizex, asizex;
    float f;
    
    sizex = stopx-startx+1;
    asizex = max(1,sizex);
    points_used=0;
    points             = (VERTEXF*) realloc(points, sizeof(VERTEXF) * (asizex * 3 + 2));
    transformed_points = 
        (VERTEXF*) realloc(transformed_points, sizeof(VERTEXF) * (asizex * 3 + 2));

    n = 1;
    points[points_used][0] = (offx + n) * scalex;
    points[points_used][1] = 0.0;
    points[points_used][2] = offy * scaley -1.0 * scale_tick;
    points_used++;
    n = sizex;
    points[points_used][0] = (offx + n) * scalex;
    points[points_used][1] = 0.0;
    points[points_used][2] = offy * scaley -1.0 * scale_tick;
    points_used++;

    for (f=tick1;f<=sizex;f+=tickstep) {
        if (f<0)
            continue;
        points[points_used][0] = (offx + f) * scalex;
        points[points_used][1] = 0.0;
        points[points_used][2] = offy * scaley -1.0 * scale_tick;
        points_used++;
        points[points_used][0] = (offx + f) * scalex;
        points[points_used][1] = 0.0;
        points[points_used][2] = offy * scaley -2.0 * scale_tick;
        points_used++;
        points[points_used][0] = (offx + f) * scalex;
        points[points_used][1] = 0.0;
        points[points_used][2] = offy * scaley -6.0 * scale_tick;
        points_used++;
    }
}

static void prep_yaxis(
    float scalex, float scaley, float scale_tick,
    int offx, int offy,     
    int starty,   int stopy,
    float tick1,  float tickstep)

{
    int n;
    int sizey, asizey;
    float f;
    
    sizey = stopy-starty+1;
    asizey = max(1,sizey);
     
    points_used=0;
    points             = (VERTEXF*) realloc(points, sizeof(VERTEXF) * (asizey * 3 + 2));
    transformed_points = 
        (VERTEXF*) realloc(transformed_points, sizeof(VERTEXF) * (asizey * 3 + 2));


    n = 1;
    points[points_used][0] = offx * scalex -1.0 * scale_tick;
    points[points_used][1] = 0.0;
    points[points_used][2] = (offy + n) * scaley;
    points_used++;
    n = sizey;
    points[points_used][0] = offx * scalex -1.0 * scale_tick;
    points[points_used][1] = 0.0;
    points[points_used][2] = (offy + n) * scaley;
    points_used++;

    for (f=tick1;f<=sizey;f+=tickstep) {
        if (f<0)
            continue;
        points[points_used][0] = offx * scalex -1.0 * scale_tick;
        points[points_used][1] = 0.0;
        points[points_used][2] = (offy + f) * scaley;
        points_used++;
        points[points_used][0] = offx * scalex -2.0 * scale_tick;
        points[points_used][1] = 0.0;
        points[points_used][2] = (offy + f) * scaley;
        points_used++;
        points[points_used][0] = offx * scalex -6.0 * scale_tick;
        points[points_used][1] = 0.0;
        points[points_used][2] = (offy + f) * scaley;
        points_used++;
    }
}

static void draw_axis(int color, char *labels[], int nlabels)
{
    int i,j;
    
    g_set_foreground(color);
    g_moveto(transformed_points[0][0], 
             transformed_points[0][1]);
    g_lineto(transformed_points[1][0], 
             transformed_points[1][1]);
    for (i = 2,j=0; i < points_used; i+=3) {
        g_moveto(transformed_points[i][0], 
                 transformed_points[i][1]);
        g_lineto(transformed_points[i+1][0], 
                 transformed_points[i+1][1]);
        g_moveto(transformed_points[i+2][0], 
                 transformed_points[i+2][1]);
        if (nlabels-- > 0) {
            g_label(labels[j++]);
        }
    }
}


static int read_data_from_disk(MBLOCK *mblk, CBLOCK *cpar,
    float *buff,
    int ncpr,
    int infil,
    float *xdata,
    int maxdat,
    int numdatax, int numdatay,
    int startx,   int stopx,
    int starty,   int stopy,
    int skipx,    int skipy)
{
    int ncols, nrows;
    int iadiv = skipx+1;
    int ibdiv = skipy+1;

    mulr2d(mblk,buff,
               ncpr,
               infil,
               xdata,
               maxdat,
               startx,stopx,starty,stopy,
               &nrows,
               &ncols, 
               iadiv,
               ibdiv);
    cpar->ial  = startx;
    cpar->iah  = stopx;
    cpar->ibl  = starty;
    cpar->ibh  = stopy;
    cpar->iadv = iadiv;
    cpar->ibdv = ibdiv;
    return nrows * ncols;
}

static int read_data_from_buffer(float *buff,
    float *xdata,
    int numdatax, int numdatay,
    int startx0,  int starty0,
    int startx,   int stopx,
    int starty,   int stopy,
    int skipx,    int skipy)
{
    int i,n,count, pos = 0;
    int sizex, sizey;
    int iadiv = skipx+1;
    int ibdiv = skipy+1;

    sizex  = stopx-startx+1;
    sizex -= 1;
    sizex /= iadiv;
    sizex += 1;

    sizey  = stopy-starty+1;
    sizey -= 1;
    sizey /= ibdiv;
    sizey += 1;
    
    numdatax -= 1;
    numdatax /= iadiv;
    numdatax += 1;
    numdatay -= 1;
    numdatay /= ibdiv;
    numdatay += 1;
    
    startx = (startx-startx0)/iadiv;
    starty = (starty-starty0)/ibdiv;
    pos = (starty) * numdatax;
    for (i=0,count=0;i<sizey;i+=1) {
        for (n=0;n<sizex;n+=1) {
            buff[count++] = xdata[startx + pos+n];
        }
        pos += numdatax;
    }
    return count;
}


#define COO2(x,y)	((x) + (y) * sizex); 

static POLYHEDRON *prepare_stackplot(MBLOCK *mblk,
    CBLOCK *cpar,
    POLYHEDRON *polygons, int triangles, int reverse_order,
    int data_in_buffer,
    float *xcpr,
    int maxcpr,
    int infil,
    float *xdata,
    int maxdat,
    float width, float height,
    float scalex, float scaley, float scalez,
    int offx,     int offy,
    int numdatax, int numdatay,
    int startx0,  int starty0,
    int startx,   int stopx,
    int starty,   int stopy,
    int skipx,    int skipy,
    int color,    int border_color)
{
    FILE *file;    float *ybuf;
    int i, j, k,n;
    int sizex;
    int sizey;
    int count;
    int stepx=0,stepy=0;

    sizex  = stopx-startx+1;
    sizey  = stopy-starty+1;

    points_used = 0;
    points             = (VERTEXF*) realloc(points, sizeof(VERTEXF) * sizex * sizey);
    transformed_points = 
        (VERTEXF*) realloc(transformed_points, sizeof(VERTEXF) * sizex * sizey);

    ybuf = (float*) malloc(sizeof(float) * sizex * sizey);
    if (data_in_buffer > 0) {
        count = read_data_from_buffer(ybuf,
            xcpr,
            numdatax, numdatay, 
            startx0,  starty0,
            startx,   stopx,
            starty,   stopy,
            skipx,    skipy);
    }
    else
        count = read_data_from_disk(mblk,
            cpar,
            ybuf,
            maxcpr,
            infil,
            xdata,
            maxdat,
            numdatax, numdatay,
            startx,   stopx,
            starty,   stopy,
            skipx,    skipy);
    assert(count <= sizex * sizey);

    count=0;    
    for (i=1;i<=sizey;i+=skipy+1) {
        stepx=0;
        for (n=1;n<=sizex;n+=skipx+1) {
            points[points_used][0] = (float)(offx + n) * scalex;
            points[points_used][1] = ybuf[count++] * scalez;
            points[points_used][2] = (float)(offy + i) * scaley;
            points_used++;
            stepx++;
        }
        stepy++;
    }
    assert(count == stepx * stepy);
    free(ybuf);

    sizex = stepx;
    sizey = stepy;

    polygon_points = 
        (int*) realloc(polygon_points, 2 * sizeof(int) * sizex * sizey * 6 + 1);
    polygons       = 
        (POLYHEDRON*) realloc(polygons, sizeof(POLYHEDRON) * sizex * sizey * 2 + 1);

    poly_count=0;
    k=0;
    if (!triangles) {
        if (reverse_order) {
        for (i=sizey-2;i>=0;i--) {
            for (n=sizex-2;n>=0;n--) {
                add_polygon(poly_count++, polygons, 4, &(polygon_points[k]), 
                    color, border_color);
                polygon_points[k++] = COO2(n+1,  i);
                polygon_points[k++] = COO2(n+1 ,i+1);
                polygon_points[k++] = COO2(n ,  i+1);
                polygon_points[k++] = COO2(n ,  i);
            }
        }
        }
        else {
        for (i=0;i<sizey-1;i++) {
            for (n=0;n<sizex-1;n++) {
                add_polygon(poly_count++, polygons, 4, &(polygon_points[k]), 
                    color, border_color);
                polygon_points[k++] = COO2(n+1,  i);
                polygon_points[k++] = COO2(n+1 ,i+1);
                polygon_points[k++] = COO2(n ,  i+1);
                polygon_points[k++] = COO2(n ,  i);
            }
        }
        }
    }
    else {
        for (i=0;i<sizey-1;i++) {
            for (n=0;n<sizex-1;n++) {
    
                if (i%2) {
                    add_polygon(poly_count++, polygons, 3, &(polygon_points[k]), 
                        color, border_color);
                    polygon_points[k++] = COO2(n ,  i);
                    polygon_points[k++] = COO2(n+1 ,i+1);
                    polygon_points[k++] = COO2(n ,  i+1);

                    add_polygon(poly_count++, polygons, 3, &(polygon_points[k]), 
                        color, border_color);
                    polygon_points[k++] = COO2(n+1 ,i);
                    polygon_points[k++] = COO2(n+1 ,i+1);
                    polygon_points[k++] = COO2(n ,i);
                }
                else {
                    add_polygon(poly_count++, polygons, 3, &(polygon_points[k]), 
                        color, border_color);
                    polygon_points[k++] = COO2(n ,  i+1);
                    polygon_points[k++] = COO2(n ,  i);
                    polygon_points[k++] = COO2(n+1 ,i+1);

                    add_polygon(poly_count++, polygons, 3, &(polygon_points[k]), 
                        color, border_color);
                    polygon_points[k++] = COO2(n+1 ,i+1);
                    polygon_points[k++] = COO2(n ,i);
                    polygon_points[k++] = COO2(n+1 ,i);
                }
            }
        }
    }
    return polygons;
}


static int compare(const void *a, const void *b)
{
    POLYHEDRON *pa, *pb;
    pa = (POLYHEDRON *)a;
    pb = (POLYHEDRON *)b;
    if (pa->depth < pb->depth)
        return -1;
    if (pa->depth > pb->depth)
        return 1;
    return 0;
}

static float calc_depth(int num_points, int *point_id)
{
    int i;
    float result = 0.0;
    
    for (i=0;i<num_points;i++)
        result += transformed_points[point_id[i]][2];
    return result/num_points;
}

static float calc_depth2(int num_points, int *point_id)
{
    int i;
    float result = transformed_points[point_id[0]][2];
    
    for (i=1;i<num_points;i++)
        result = min(result,transformed_points[point_id[i]][2]);
    return result;
}

#define POLYPOINTS	10
/* 
 * Draw the 3d object using the current composite transform. 
 */
static void draw_polygons(int poly_count, POLYHEDRON *poly, int border_mode)
{
   POLYPOINT buffer[POLYPOINTS]; 
   int id,i,j, pcount=0;                    

   /*
    * Determine the polygons that are looking away from us
    * and detrmine the depth of each polygon
    */
#ifdef meuk
   for (i=0;poly[i].num_points > 0;i++) {
        pcount++;
       
         poly[i].depth = 0;
         poly[i].skip = backface(
            transformed_points[poly[i].point_id[0]],
            transformed_points[poly[i].point_id[1]],
            transformed_points[poly[i].point_id[2]]);
/*
poly[i].skip=0;
         if (poly[i].skip)
             continue;
*/
         poly[i].depth = -calc_depth(poly[i].num_points-1,poly[i].point_id);      

   }

   /*
    * sort the polygons according to depth
    * Farest away first
    */
/*   qsort(poly, pcount, sizeof(POLYHEDRON), compare);*/
#endif
   for (i=0;i<poly_count;i++) {
        int dest = 0;
        /*
         * skip the backfaced polygons
         */
         /*
        if (poly[i].skip)
            continue;
            */
        /*
         * put points in buffer for drawpoly and fillpoly
         */
        for (j=0;j<poly[i].num_points;j++) {
            buffer[dest].x = transformed_points[poly[i].point_id[j]][0];
            buffer[dest].y = transformed_points[poly[i].point_id[j]][1];
            dest++;
        }
        assert(dest < POLYPOINTS);
        buffer[dest].x = transformed_points[poly[i].point_id[0]][0];
        buffer[dest].y = transformed_points[poly[i].point_id[0]][1];
        /*
         * fill polygons
         */
         /*
        if (poly[i].skip)
        g_set_foreground(G_GREEN);
        else
        */
        g_set_foreground(poly[i].color);
        g_fillpoly(dest, (float*) buffer);
        /*
         * draw borders
         */
        g_set_foreground(poly[i].border_color);
        /*
         * Draw borders in east-west and north-south directions
         */
        if (border_mode == 0)
            g_drawpoly(dest+1,(float*)buffer);
        /*
         * Draw borders in east-west direction 
         * Works only of polygons have 4 borders (squares)
         */
        else if (border_mode == 1) {
            g_moveto(buffer[1].x, buffer[1].y);
            g_lineto(buffer[2].x, buffer[2].y);
            g_moveto(buffer[3].x, buffer[3].y);
            g_lineto(buffer[0].x, buffer[0].y);
        }
        /*
         * Draw borders in north-south direction
         * Works only of polygons have 4 borders (squares)
         */
        else {
            g_moveto(buffer[0].x, buffer[0].y);
            g_lineto(buffer[1].x, buffer[1].y);
            g_moveto(buffer[2].x, buffer[2].y);
            g_lineto(buffer[3].x, buffer[3].y);
        }
         
    }
}

static void draw_wireframe(int poly_count, POLYHEDRON *polygons)
{
    int i,j,k;
    for (i=0;i<poly_count;i++) {
        k = polygons[i].point_id[polygons[i].num_points-1];
        g_set_foreground(polygons[i].border_color);
        g_moveto(transformed_points[k][0],
                 transformed_points[k][1]);
        for (j=0;j<polygons[i].num_points;j++) {
            k=polygons[i].point_id[j];  
            g_lineto(transformed_points[k][0],
                     transformed_points[k][1]);
        }
    }
}

static void stackplot(MBLOCK *mblk,
    CBLOCK *cpar,
    int draw_wire,
    int border_mode,
    int data_in_buffer,
    float *xcpr,
    int maxcpr,
    int infil,
    float *xdata,
    int maxdat,
    int width, int height,
    int numpointsx, int numpointsy,
    int x1, int x2 ,
    int y1, int y2,
    int skipx, int skipy,
    int show_xaxis, int show_yaxis,
    float tick_x1, float tick_y1,
    float tick_stepx, float tick_stepy,
    float tick_scalex, float tick_scaley,
    float scalez,
    int do_triangles,
    int color, int border_color,
    char *xlabels[], int nxlabels,
    char *ylabels[], int nylabels)

{
    int sizex, sizey, steps;
    int xyorder, absxyorder;
    int offx = 0, offy = 0;
    int offsetx = 0, offsety = 0;
    int i,j, x11, x22, y11, y22;
    float scalex, scaley;
    POLYHEDRON *poly = polygons;
    STACKPLOT_BLOCK *spb = mblk->spb;

    skipx = max(0,skipx);
    skipy = max(0,skipy);

    sizex = x2 - x1 + 1;
    sizex -= (skipx+2);
    sizex /= (skipx+1);
    sizex *= (skipx+1);
    sizex += (skipx+2);
    x2 = x1 + sizex - 1;
    
    sizey = y2 - y1 + 1;
    sizey -= (skipy+2);
    sizey /= (skipy+1);
    sizey *= (skipy+1);
    sizey += (skipy+2);
    y2 = y1 + sizey - 1;

    x11 = x1;
    x22 = x2;
    y11 = y1;
    y22 = y2;

    scalex = (float)width  / (float)sizex;
    scaley = (float)height / (float)sizey;

    offsetx = -sizex/2.0;
    offsety = -sizey/2.0;

    init_transforms(x11, y11, width, 
                    height, spb->perspective);

    prepare_probe();

    identity_f(model);
    /*
     * Global scaling
     */
    scale_f(motion, spb->scale_def, 
                    spb->scale_def, 
                    -spb->scale_def);
    concatenate_transforms_f(model, motion, model);
    /*
     * Global translation
     */
    translate_f(motion, spb->translate_def[0], 
                        spb->translate_def[1], 
                        spb->translate_def[2]);
    concatenate_transforms_f(model, motion, model);
    rotateX_f(motion, spb->xangle_def);
    concatenate_transforms_f(model, motion, model);
    rotateY_f(motion, spb->yangle_def);
    concatenate_transforms_f(model, motion, model);
    rotateZ_f(motion, spb->zangle_def);
    concatenate_transforms_f(model, motion, model);

    concatenate_transforms_f(view, model, composite);

    transform_all_points(x11,y11,width,height);
    xyorder = get_xyorder();
    absxyorder = abs(xyorder);
    if (absxyorder == 1 || absxyorder == 3)
        steps = (sizey-1)/(skipy+1);
    else
        steps = (sizex-1)/(skipx+1);

    if (show_xaxis && (absxyorder == 3 || xyorder == -4 || xyorder == -2)) {
        prep_xaxis(
            scalex, scaley, tick_scalex,
            offsetx, offsety, 
            x11, x22,
            tick_x1, tick_stepx);
        transform_all_points(x11,y11,width,height);
        draw_axis(border_color, xlabels, nxlabels);
    }

    if (show_yaxis && (absxyorder == 4 || xyorder == -1 ||  xyorder == -3 )){
        prep_yaxis(
            scalex, scaley, tick_scaley, 
            offsetx, offsety, 
            y11, y22,
            tick_y1, tick_stepy);
        transform_all_points(x11,y11,width,height);
        draw_axis(border_color, ylabels, nylabels);
    }

    for (j=0;j<steps;j++) {
        if (absxyorder == 1) {
            y1 = y2 - (skipy+1);
            y1 = max(y11, y1);
            offy = y1-y11;
        }
        else if (absxyorder == 2) {
            x1 = x2 - (skipx+1);
            x1 = max(x11, x1);
            offx = x1-x11;
        }
        else if (absxyorder == 3) {
            y2 = y1 + (skipy+1);
            y2 = min(y22, y2);
            offy = y1-y11;
        }
        else if (absxyorder == 4) {
            x2 = x1 + (skipx+1);
            x2 = min(x22, x2);
            offx = x1-x11;
        }
        poly = prepare_stackplot(mblk, cpar,
            poly, do_triangles, 
            (absxyorder == xyorder),
            data_in_buffer,
            xcpr,
            maxcpr,
            infil,
            xdata,
            maxdat,
            width, height,
            scalex, scaley, scalez,
            offx+offsetx, offy+offsety,
            numpointsx, numpointsy,
            x11, y11,
            x1, x2,
            y1, y2,
            skipx, skipy,
            color, border_color);
        if (absxyorder == 1)
            y2 = y1;
        else if (absxyorder == 2)
            x2 = x1;
        else if (absxyorder == 3)
            y1 = y2;
        else if (absxyorder == 4)
            x1 = x2;

        transform_all_points(x11,y11,width,height);
        if (draw_wire)
            draw_wireframe(poly_count, poly);
        else
            draw_polygons(poly_count, poly, border_mode);
    }

    if (show_xaxis && (absxyorder == 1 || xyorder == 4 || xyorder == 2)) {
        prep_xaxis(
            scalex, scaley, tick_scalex, 
            offsetx, offsety, 
            x11, x22,
            tick_x1, tick_stepx);
        transform_all_points(x11,y11,width,height);
        draw_axis(border_color, xlabels, nxlabels);
    }

    if (show_yaxis && (absxyorder == 2 || xyorder == 1 || xyorder == 3 )) {
        prep_yaxis(
            scalex, scaley, tick_scaley, 
            offsetx, offsety, 
            y11, y22,
            tick_y1, tick_stepy);
        transform_all_points(x11,y11,width,height);
        draw_axis(border_color, ylabels, nylabels);
    }
}



static void redraw(MBLOCK *mblk, CBLOCK *cpar, STACKPLOT_BLOCK *spb)
{
    stackplot(mblk, cpar,
                         spb->draw_wire,
                         spb->border_mode,
                         spb->data_in_buffer,
                         spb->xcpr,
                         spb->ncpr,
                         spb->inpfil,
                         spb->x,
                         spb->maxdat,
                         spb->numpointsx, spb->numpointsy,
                         spb->numpointsx, spb->numpointsy,
                         spb->x1, spb->x2,
                         spb->y1, spb->y2,
                         spb->skipx, spb->skipy,
                         spb->show_xaxis, spb->show_yaxis,
                         spb->tick_x1, spb->tick_y1,
                         spb->tick_stepx, spb->tick_stepy,
                         spb->tick_scalex, spb->tick_scaley,
                         spb->scalez,
                         spb->do_triangles,
                         spb->color, spb->border_color,
                         spb->xlabels, spb->nxlabels,
                         spb->ylabels, spb->nylabels);
}

static void xy3d(MBLOCK *mblk, float x, float y, int mode)
{
    int iangle;
    float scalex, scaley, sum;

    switch (mode) {
        /* rotate */
        case 0:
            iangle = (int) (y + mblk->spb->xangle_def);
            mblk->spb->xangle_def = (float) (iangle % 360);
            iangle = (int) (x + mblk->spb->yangle_def);
            mblk->spb->yangle_def = (float) (iangle % 360);
            break;
        /* translate */
        case 1:
            scalex = (float)mblk->spb->numpointsx/100.0;
            scaley = (float)mblk->spb->numpointsy/100.0;
            mblk->spb->translate_def[0] += x * scalex;
            mblk->spb->translate_def[1] += y * scaley;
            break;
        /* scale */
        case 2:
            sum = y + x;
            if (sum == 0.0)
                break;
            if (sum > 0.0) {
                sum = 1.0 + sum / 100.0;
                if (sum > 2.0)
                    sum = 2.0;
            }
            else {
                sum = 1.0 + sum / 100.0;
                if (sum < 0.1)
                    sum = 0.1;
            }
            mblk->spb->scale_def *= sum;
            break;
        default:
            g_bell();
            break;
    }
}

void psnd_3d_xy(MBLOCK *mblk, float x, float y)
{ 
    xy3d(mblk, x, y, mblk->spb->mouse_mode);
}

static float get_stepsize(float x)
{
    float q=1;

    while (x > 10) {
        x/=10;
        q*=10;
    }
    while (x < 1 && x > 0) {
        x*=10;
        q/=10;
    }
    if (x<2)
        return q*0.2;
    if (x<5)
        return q*0.5;
    return q;
}

#define FLOOR(a)	floor((a)+0.0001)
static int calc_label_digits(float minp, float labels_ppm)
{
    float a,b,c,d,e;
    int i, n, digits = 0;
    d = fabs(minp);
    e = 1.0/labels_ppm;
    do {
        c = 1.0;
        for (i=0;i<digits;i++)
            c *= 10; 
        a = FLOOR(c*d);
        for (i=1;i<10;i++) {
            b = FLOOR(c*(d+i*e));
            n = (int)floor(a-b);
            if (n==0)
                break;
            a=b;
        }
        if (n==0)
            digits++;
    } while (n==0);
    return digits;
}


static void init_stackplot(MBLOCK *mblk, CBLOCK *cpar,STACKPLOT_BLOCK *spb)
{
    int j, sizex, sizey, labelcount, xdigits, ydigits;
    float x,y,axisx1,axisx2,axisy1,axisy2,sign;
    char format[30];
    
    if ((float)spb->numpointsx / (spb->skipx + 1) * 
        (float)spb->numpointsy / (spb->skipy + 1) < spb->ncpr) {
        int nrows=0, ncols=0;
        sizex = spb->x2 - spb->x1 + 1;
        sizex -= (spb->skipx+2);
        sizex /= (spb->skipx+1);
        sizex *= (spb->skipx+1);
        sizex += (spb->skipx+2);
        spb->x2 = spb->x1 + sizex - 1;
        spb->numpointsx = sizex;
    
        sizey = spb->y2 - spb->y1 + 1;
        sizey -= (spb->skipy+2);
        sizey /= (spb->skipy+1);
        sizey *= (spb->skipy+1);
        sizey += (spb->skipy+2);
        spb->y2 = spb->y1 + sizey - 1;
        spb->numpointsy = sizey;

        spb->data_in_buffer = read_data_from_disk(mblk,
                                    cpar, 
                                    spb->xcpr,
                                    spb->ncpr,
                                    spb->inpfil,
                                    spb->x,
                                    spb->maxdat,
                                    nrows, ncols,
                                    spb->x1,  spb->x2,
                                    spb->y1,  spb->y2,
                                    spb->skipx, spb->skipy);
    }
    else
        spb->data_in_buffer =  0;       

    labelcount=0;
    spb->show_xaxis = FALSE;
    if (spb->axisx1 != spb->axisx2) {
        spb->show_xaxis = TRUE;
        if (spb->axisx1 > spb->axisx2) 
            sign = -1.0;
        else
            sign = 1.0;
        axisx1 = sign * spb->axisx1;
        axisx2 = sign * spb->axisx2;
        spb->tick_stepx = get_stepsize(axisx2 - axisx1);
        spb->tick_x1=floor(((float)axisx1)/spb->tick_stepx)*spb->tick_stepx;
        while (spb->tick_x1 < axisx1)
            spb->tick_x1 += spb->tick_stepx;     
        spb->tick_scalex = max(0.1, (float)spb->numpointsy/500.0);
        spb->nxlabels = 0;
        xdigits = calc_label_digits(axisx1, 1.0/spb->tick_stepx);
        sprintf(format,"%%.%df", xdigits);
        for (x=spb->tick_x1,j=0;x<=axisx2;x += spb->tick_stepx) {
            spb->xlabels[j] = spb->axislabels[labelcount++];
            sprintf(spb->xlabels[j], format, x*sign);
            j++;
            spb->nxlabels++;
            if (spb->nxlabels >= MAXLABELS)
                break;
            if (labelcount >= 2*MAXLABELS)
                break;
        }
        spb->tick_x1 -= axisx1;
        spb->tick_stepx *= (spb->x2 - spb->x1)/(axisx2 - axisx1);
        spb->tick_x1 *= (spb->x2 - spb->x1)/(axisx2 - axisx1);
    }
    spb->show_yaxis = FALSE;
    if (spb->axisy1 != spb->axisy2) {
        spb->show_yaxis = TRUE;
        if (spb->axisy1 > spb->axisy2) 
            sign = -1.0;
        else
            sign = 1.0;
        axisy1 = sign * spb->axisy1;
        axisy2 = sign * spb->axisy2;
        spb->tick_stepy = get_stepsize(axisy2 - axisy1);
        spb->tick_y1=floor(((float)axisy1)/spb->tick_stepy)*spb->tick_stepy;
        while (spb->tick_y1 < axisy1)
            spb->tick_y1 += spb->tick_stepy;     
        spb->tick_scaley = max(0.1, (float)spb->numpointsx/500.0);
        spb->nylabels = 0;
        ydigits = calc_label_digits(axisy1, 1.0/spb->tick_stepy);
        sprintf(format,"%%.%df", ydigits);
        for (y=spb->tick_y1,j=0;y<=axisy2;y += spb->tick_stepy) {
            spb->ylabels[j] = spb->axislabels[labelcount++];
            sprintf(spb->ylabels[j], format, y*sign);
            j++;
            spb->nylabels++;
            if (spb->nylabels >= MAXLABELS)
                break;
            if (labelcount >= 2*MAXLABELS)
                break;
        }
        spb->tick_y1 -= axisy1;
        spb->tick_stepy *= (spb->y2 - spb->y1)/(axisy2 - axisy1);
        spb->tick_y1 *= (spb->y2 - spb->y1)/(axisy2 - axisy1);
    }
}

void psnd_stackplot(MBLOCK *mblk, CBLOCK *cpar, float *xcpr, int ncpr, 
            int border_color, int fill_color,
            int inpfil, float *xdata, int maxdat, 
            int ilowa, int ihigha, int ilowb, int ihighb,
            float lowest, float highest, int update,
            float axisx1, float axisx2, 
            float axisy1, float axisy2)
{
    int ial,iah,ibl,ibh;
    int width, height; 
    STACKPLOT_BLOCK *spb = mblk->spb;
    POPUP_INFO *popinf = mblk->popinf + POP_STACKPLOT;
    
    if (!popinf->visible || !spb->is_visible)
        return;

    if (!update) {

        int iadiv = (int)((float)(ihigha-ilowa+1))/60.0;
        int ibdiv = (int)((float)(ihighb-ilowb+1))/60.0;
        iadiv = max(1,iadiv);
        ibdiv = max(1,ibdiv);

        if (!okarea (ilowa,ihigha,ilowb,ihighb)) {
            psnd_printf(mblk," ... error in area %6d %6d %6d %6d\n",
                 ilowa,ihigha,ilowb,ihighb);
            return;
        }
 
        if (ncpr < 10 || maxdat < 10) {
            psnd_printf(mblk,"  error - cntr2d buffer(s) too small : %2d %2d\n",
                ncpr,maxdat);
            return;
        }

        psnd_printf(mblk," ... stackplot - file %2d - area %6d %6d %6d %6d\n",
                 inpfil,ilowa,ihigha,ilowb,ihighb);


        ial = min(ilowa,ihigha);
        iah = max(ilowa,ihigha);
        ibl = min(ilowb,ihighb);
        ibh = max(ilowb,ihighb);
        ial = max(ial,1);
        ibl = max(ibl,1);

        width  = iah - ial + 1;
        height = ibh - ibl + 1;
         
        spb->scale_factor = max(width,height);
        spb->draw_wire = TRUE;

        init_transforms(ial, ibl, width, height, 
                        spb->perspective);
        spb->scale_def=1.0;

        spb->xangle = spb->xangle_def;
        spb->yangle = spb->yangle_def;

        spb->ncpr         = ncpr;
        spb->inpfil       = inpfil;
        spb->x            = xdata;
        spb->xcpr         = xcpr;
        spb->maxdat       = maxdat;
        spb->color        = fill_color;
        spb->border_color = border_color;
                          
        spb->do_triangles = FALSE;

        spb->numpointsx = width;
        spb->numpointsy = height;
        spb->x1 = ial;
        spb->x2 = iah;
        spb->y1 = ibl;
        spb->y2 = ibh;
        spb->skipx = max(0,iadiv-1);
        spb->skipy = max(0,ibdiv-1);
        spb->scalez = height/highest;

        spb->axisx1  = axisx1;
        spb->axisx2  = axisx2;
        spb->axisy1  = axisy1;
        spb->axisy2  = axisy2;

        spb->translate_def[0] = 0.0;
        spb->translate_def[1] = 0.0;
        spb->translate_def[2] = 0.0;
        spb->scale_def        = 1.0;
        spb->xangle_def       = 0.0;
        spb->yangle_def       = 0.0;
        spb->zangle_def       = 0.0;

        init_stackplot(mblk,cpar,spb);
    }
    redraw(mblk,cpar,spb);

}


typedef enum {
    STACK_PLOT = 20,
    STACK_ROTATE_X,
    STACK_ROTATE_Y,
    STACK_ROTATE_Z,
    STACK_ROTATE_X_VALUE,
    STACK_ROTATE_Y_VALUE,
    STACK_ROTATE_Z_VALUE,
    STACK_SKIP,
    STACK_SCALEY,
    STACK_SCALE,
    STACK_HIDE_LINES,
    STACK_BORDER,
    STACK_MOUSE,
    STACK_MOUSE_GRAB,
    STACK_PERSPECTIVE
} stackplot_ids;

static void stackplot_callback(G_POPUP_CHILDINFO *ci)
{
    G_EVENT ui;
    MBLOCK *mblk = (MBLOCK *) ci->userdata;
    DBLOCK *dat  = DAT;
    SBLOCK *spar = mblk->spar;
    CBLOCK *cpar = mblk->cpar_screen;
    int redraw_it = FALSE;
    STACKPLOT_BLOCK *spb = mblk->spb;
    POPUP_INFO *popinf = mblk->popinf + POP_STACKPLOT;
    
    if (mblk->info->plot_mode != PLOT_2D)
        return;
    switch (ci->type) {
        case G_CHILD_OK:
        case G_CHILD_CANCEL:
            if (points) {
                free(points);
                points = NULL;
                points_used = 0;
            }
            if (transformed_points) {
                free(transformed_points);
                transformed_points = NULL;
            }
            if (polygon_points) {
                free(polygon_points);
                polygon_points = NULL;
            }
            if (polygons) {
                free(polygons);
                polygons = NULL;
            }
            g_menu_enable_item(mblk->info->mousebar_id, ID_MOUSE_SELECT, FALSE);
            if (mblk->info->mouse_mode1 == MOUSE_3D)
                psnd_set_cursormode(mblk, 0, 0);   
            spb->is_visible = FALSE;           
            popinf->visible = FALSE;
            return;
        case G_CHILD_TEXTBOX:
            switch (ci->id) {
                case STACK_ROTATE_X_VALUE: 
                    spb->rotate[0] = psnd_scan_float(ci->label);
                    break;
                case STACK_ROTATE_Y_VALUE: 
                    spb->rotate[1] = psnd_scan_float(ci->label);
                    break;
                case STACK_ROTATE_Z_VALUE: 
                    spb->rotate[2] = psnd_scan_float(ci->label);
                    break;
            }
            break;
        case G_CHILD_RADIOBOX:
            switch (ci->id) {
                case STACK_MOUSE:
                    spb->mouse_mode = ci->item;
                    break;
                case STACK_BORDER:
                    spb->border_mode = ci->item;
                    break;
                case STACK_PERSPECTIVE: 
                    spb->perspective = ci->item;
                    redraw_it = TRUE;
                    break;
            }
            break;
        case G_CHILD_PUSHBUTTON:
            switch (ci->id) {
                case STACK_MOUSE_GRAB:
                    psnd_set_cursormode(mblk, 0, MOUSE_3D);
                    break;
                case STACK_PLOT: 
                    spb->is_visible = TRUE;
                    ui.event   = G_COMMAND;
                    ui.win_id  = mblk->info->win_id;
                    ui.keycode = PSND_STACKPLOT;
                    g_send_event(&ui);
                    break;
                case STACK_ROTATE_X: 
                    spb->xangle_def += spb->rotate[0];
                    redraw_it = TRUE;
                    break;
                case STACK_ROTATE_Y: 
                    spb->yangle_def += spb->rotate[1];
                    redraw_it = TRUE;
                    break;
                case STACK_ROTATE_Z: 
                    spb->zangle_def += spb->rotate[2];
                    redraw_it = TRUE;
                    break;
                case STACK_SKIP: {
                    int idiv[2];
                    idiv[0] = spb->skipx;
                    idiv[1] = spb->skipy;
                    if (psnd_ivalin(mblk,"Skipx, skipy",2,idiv) == 0)
                        break;
                    spb->skipx = max(0,idiv[0]);
                    spb->skipy = max(0,idiv[1]);
                    init_stackplot(mblk,cpar,spb);
                    redraw_it = TRUE;
                    }
                    break;
                case STACK_SCALE: {
                    float scale_fac = spb->scale_def ;
                    if (psnd_rvalin(mblk,"Overall scaling",1,&scale_fac) == 0)
                        break;
                    if (scale_fac > 0.000001)
                        spb->scale_def = scale_fac ;
                    redraw_it = TRUE;
                    }
                    break;
                case STACK_SCALEY: {
                    float scale_fac = spb->scalez * 1000 ;
                    if (psnd_rvalin(mblk,"Peak scaling",1,&scale_fac) == 0)
                        break;
                    spb->scalez = scale_fac * 0.001 ;
                    redraw_it = TRUE;
                    }
                    break;
                case STACK_HIDE_LINES: 
                    spb->draw_wire = FALSE;
                    psnd_push_waitcursor(mblk);
                    mblk->info->dimension_id = dat->access[0] - 1;
                    psnd_cp(mblk,CONTOUR_STACK);
                    g_plotall();
                    psnd_pop_waitcursor(mblk);
                    spb->draw_wire = TRUE;
                    break;
            }
            break;
    }
    if (redraw_it && spb->is_visible) {
        psnd_push_waitcursor(mblk);
        mblk->info->dimension_id = dat->access[0] - 1;
        psnd_cp(mblk,CONTOUR_STACK);
        g_plotall();
        psnd_pop_waitcursor(mblk);
    }
}

void psnd_stackplot_popup(MBLOCK *mblk)
{
    int i,id;
    G_POPUP_CHILDINFO ci[40];
    STACKPLOT_BLOCK *spb = mblk->spb;
    int win_id = mblk->info->win_id;
    SBLOCK *spar = mblk->spar;
    CBLOCK *cpar = mblk->cpar_screen;
    DBLOCK *dat = DAT;
    POPUP_INFO *popinf = mblk->popinf + POP_STACKPLOT;

    if (popinf->visible)
        return;
    popinf->visible = TRUE;
    if (! popinf->cont_id) {      
        char labelbuf[80];
        int cont_id;
            
        spb->perspective = TRUE;
        spb->rotate[0] = 5.0;
        spb->rotate[1] = 5.0;
        spb->rotate[2] = 5.0;

        cont_id = g_popup_container_open(win_id, "Stackplot",
                                         G_POPUP_KEEP|G_POPUP_SINGLEBUTTON);
        popinf->cont_id = cont_id;
        id=0;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_PUSHBUTTON;
        ci[id].id    = STACK_PLOT;
        ci[id].label = "Stack Plot/Reset";
        ci[id].func  = stackplot_callback;
        ci[id].userdata = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(ci + id);
        ci[id].type          = G_CHILD_PANEL;
        ci[id].item          = TRUE;
        ci[id].frame         = TRUE;
        ci[id].title 	     = "Rotate";
        ci[id].horizontal    = FALSE;
        g_popup_add_child(cont_id, ci + id);

        id++;
        g_popup_init_info(ci + id);
        ci[id].type          = G_CHILD_PANEL;
        ci[id].item          = TRUE;
        ci[id].horizontal    = TRUE;
        g_popup_add_child(cont_id, ci + id);

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type          = G_CHILD_TEXTBOX;
        ci[id].id            = STACK_ROTATE_X_VALUE;
        ci[id].item_count    = 10;
        ci[id].items_visible = 7;
        sprintf(labelbuf,"%g", spb->rotate[0]);
        ci[id].label         = labelbuf;
        ci[id].func  = stackplot_callback;
        ci[id].userdata = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type          = G_CHILD_TEXTBOX;
        ci[id].id            = STACK_ROTATE_Y_VALUE;
        ci[id].item_count    = 10;
        ci[id].items_visible = 7;
        sprintf(labelbuf,"%g", spb->rotate[1]);
        ci[id].label         = labelbuf;
        ci[id].func  = stackplot_callback;
        ci[id].userdata = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));
    
    
        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type          = G_CHILD_TEXTBOX;
        ci[id].id            = STACK_ROTATE_Z_VALUE;
        ci[id].item_count    = 10;
        ci[id].items_visible = 7;
        sprintf(labelbuf,"%g", spb->rotate[2]);
        ci[id].label         = labelbuf;
        ci[id].func  = stackplot_callback;
        ci[id].userdata = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));
    
        id++;
        g_popup_init_info(ci + id);
        ci[id].type       = G_CHILD_PANEL;
        ci[id].item       = FALSE;
        g_popup_add_child(cont_id, ci + id);
    
        id++;
        g_popup_init_info(ci + id);
        ci[id].type          = G_CHILD_PANEL;
        ci[id].item          = TRUE;
        ci[id].horizontal    = TRUE;
        g_popup_add_child(cont_id, ci + id);

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_PUSHBUTTON;
        ci[id].id    = STACK_ROTATE_X;
        ci[id].label = "Rotate x";
        ci[id].func  = stackplot_callback;
        ci[id].userdata = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_PUSHBUTTON;
        ci[id].id    = STACK_ROTATE_Y;
        ci[id].label = "Rotate y";
        ci[id].func  = stackplot_callback;
        ci[id].userdata = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_PUSHBUTTON;
        ci[id].id    = STACK_ROTATE_Z;
        ci[id].label = "Rotate z";
        ci[id].func  = stackplot_callback;
        ci[id].userdata = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(ci + id);
        ci[id].type       = G_CHILD_PANEL;
        ci[id].item       = FALSE;
        g_popup_add_child(cont_id, ci + id);
    
        id++;
        g_popup_init_info(ci + id);
        ci[id].type       = G_CHILD_PANEL;
        ci[id].item       = FALSE;
        g_popup_add_child(cont_id, ci + id);
    
        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_PUSHBUTTON;
        ci[id].id    = STACK_SKIP;
        ci[id].label = "Skipx, Skipy";
        ci[id].func  = stackplot_callback;
        ci[id].userdata = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_PUSHBUTTON;
        ci[id].id    = STACK_SCALE;
        ci[id].label = "Overall Scaling";
        ci[id].func  = stackplot_callback;
        ci[id].userdata = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_PUSHBUTTON;
        ci[id].id    = STACK_SCALEY;
        ci[id].label = "Scale peak height";
        ci[id].func  = stackplot_callback;
        ci[id].userdata = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(ci + id);
        ci[id].type          = G_CHILD_PANEL;
        ci[id].item          = TRUE;
        ci[id].frame         = TRUE;
        ci[id].title 	     = "Hidden line removal";
        ci[id].horizontal    = FALSE;
        g_popup_add_child(cont_id, ci + id);

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_PUSHBUTTON;
        ci[id].id    = STACK_HIDE_LINES;
        ci[id].label = "Hide lines";
        ci[id].func  = stackplot_callback;
        ci[id].userdata = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        {
        char *radio_type[] = { "XZ", "X", "Z" };
        g_popup_init_info(&(ci[id]));
        ci[id].type = G_CHILD_RADIOBOX;
        ci[id].id            = STACK_BORDER;
        ci[id].title         = "Stackplot Type";
        ci[id].frame         = TRUE;
        ci[id].horizontal    = TRUE;
        ci[id].item_count    = 3;
        ci[id].data          = radio_type;
        ci[id].item          = spb->border_mode;
        ci[id].func          = stackplot_callback;
        ci[id].userdata = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));
        }
         
        id++;
        g_popup_init_info(ci + id);
        ci[id].type       = G_CHILD_PANEL;
        ci[id].item       = FALSE;
        g_popup_add_child(cont_id, ci + id);
    
        id++;
        {
        char *radio_type[] = { "Rotate", "Translate", "Zoom" };
        g_popup_init_info(&(ci[id]));
        ci[id].type = G_CHILD_RADIOBOX;
        ci[id].id            = STACK_MOUSE;
        ci[id].title         = "Mouse function";
        ci[id].frame         = TRUE;
        ci[id].horizontal    = TRUE;
        ci[id].item_count    = 3;
        ci[id].data          = radio_type;
        ci[id].item          = spb->mouse_mode;
        ci[id].func          = stackplot_callback;
        ci[id].userdata = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));
        }
         
        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type          = G_CHILD_PUSHBUTTON;
        ci[id].id            = STACK_MOUSE_GRAB;
        ci[id].label         = "Grab Mouse Button 1";
        ci[id].func          = stackplot_callback;
        ci[id].userdata = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        {
        char *radio_type[] = { "Off", "On" };
        g_popup_init_info(&(ci[id]));
        ci[id].type = G_CHILD_RADIOBOX;
        ci[id].id            = STACK_PERSPECTIVE;
        ci[id].title         = "Perspective";
        ci[id].frame         = TRUE;
        ci[id].horizontal    = TRUE;
        ci[id].item_count    = 2;
        ci[id].data          = radio_type;
        ci[id].item          = spb->perspective;
        ci[id].func          = stackplot_callback;
        ci[id].userdata = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));
        }
         
        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_OK;
        ci[id].func  = stackplot_callback;
        ci[id].userdata = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_CANCEL;
        ci[id].func  = stackplot_callback;
        ci[id].userdata = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        assert(id<40);
    }
    g_menu_enable_item(mblk->info->mousebar_id, ID_MOUSE_SELECT, TRUE);
    psnd_set_cursormode(mblk, MOUSE_3D, 0);
    g_popup_container_show(popinf->cont_id);   

}















