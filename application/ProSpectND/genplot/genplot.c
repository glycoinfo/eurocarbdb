/************************************************************************/
/*                               genplot.c                              */
/*                                                                      */
/*  Platform : All                                                      */
/*  Module   : Main functions                                           */
/*                                                                      */
/*  Albert van Kuik                                                     */
/************************************************************************/
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
#include <time.h>

#ifdef DEBUG
#include "mshell.h"
#endif
#define  G_LOCAL
#include "genplot.h"
#include "g_inter.h"

static void calc_transform_param(void);
static void activate_device(int device);
static void copy_pixmap(int id);


#define  GENPLOT_(x) dummy_##x
#include "g_inter1.h"
#undef   GENPLOT_
#define  GENPLOT_(x) screen_##x
#include "g_inter1.h"
#undef   GENPLOT_
#define  GENPLOT_(x) hpgl_##x
#include "g_inter1.h"
#undef   GENPLOT_
#define  GENPLOT_(x) ps_##x
#include "g_inter1.h"
#undef   GENPLOT_
#define  GENPLOT_(x) wmf_##x
#include "g_inter1.h"
#undef   GENPLOT_
#ifdef _WIN32
#define  GENPLOT_(x) printer_##x
#include "g_inter1.h"
#undef   GENPLOT_
#endif


typedef struct PLOTFUNC {
  int   (*open)(void);
  void  (*close)(void);
  void  (*open_window)(int id,long x0,long y0,long width,long height,char *title,long flag);
  void  (*close_window)(int id);
  void  (*select_window)(int id);
  void  (*raise_window)(int id);
  void  (*moveto)(long x,long y);
  void  (*line)(long x1,long y1,long x2,long y2,int isclipped);
  void  (*linewidth)(int width);
  void  (*linestyle)(unsigned pattern);
  void  (*label)(char *label);
  void  (*rectangle)(long x1, long y1, long x2, long y2);
  void  (*drawpoly)(long numpoint, polytype *points);
  void  (*circle)(long r);
  void  (*fillrectangle)(long x1, long y1, long x2, long y2);
  void  (*fillpoly)(long numpoint, polytype *points);
  void  (*fillcircle)(long r);
  float (*fontheight)(int id);
  void  (*foreground)(int color);
  void  (*background)(int color);
  void  (*clipping)(int clip);
  void  (*newpage)(void);
  void  (*clearviewport)(void);
  float (*device_width);
  float (*device_height);
  int   (*dots_per_cm);
  int   (*y_is_upsidedown);
  int   (*able2clip);
  int   (*multi_windows);
  int   (*palettesize)(int size);
  int   (*paletteentry)(int entry_id, G_PALETTEENTRY entry);
} PLOTFUNC;

PLOTFUNC g_plotfunc[] = {
  {
#define  GENPLOT_(x) dummy_##x
#include "g_inter2.h"
#undef   GENPLOT_
},{
#define  GENPLOT_(x) screen_##x
#include "g_inter2.h"
#undef   GENPLOT_
},{
#define  GENPLOT_(x) hpgl_##x
#include "g_inter2.h"
#undef   GENPLOT_
},{
#define  GENPLOT_(x) ps_##x
#include "g_inter2.h"
#undef   GENPLOT_
},{
#define  GENPLOT_(x) wmf_##x
#include "g_inter2.h"
#undef   GENPLOT_
#ifdef _WIN32
},{
#define  GENPLOT_(x) printer_##x
#include "g_inter2.h"
#undef   GENPLOT_
#endif
  }
};

int    INTERNAL_g_argc;
char **INTERNAL_g_argv;
char  *INTERNAL_g_application;

DEVICETYPE      g_dev[G_MAXDEVICE];
GRAPHCONTEXT    g_gc[G_MAXDEVICE];

GRAPHCONTEXT    gc_stack[G_MAXGCSTACKSIZE];
VIEWPORTTYPE    vp_stack[G_MAXVPSTACKSIZE];

int             gc_stack_pointer;
int             vp_stack_pointer;
int             gen_device;
int             gen_softclip;
int             gen_dummy_window;
FILE            *g_outfile;
VIEWPORTTYPE    *g_viewport;

char		**icon16;
char		**icon32;
char		**icon48;
char		**icon64;

/*
*  The current transformation parameters
*  The current viewport in device coordinates
*/
STORETYPE store_param; 

/*
*  The current graphical context
*/
GRAPHCONTEXT INTERNAL_store_gc;
VIEWPORTTYPE INTERNAL_store_vp;


static G_PALETTEENTRY *g_palette;
static int g_palettesize, g_palette_readonly;

G_PALETTEENTRY DEFAULT_paletteentries[] = {
    {0, 0, 0},
    {0, 0, 139},
    {0, 139, 0},
    {0, 139, 139},
    {139, 0, 0},
    {139, 0, 139},
    {165, 42, 42},
    {211, 211, 211},
    {36, 36, 36},
    {0, 0, 255},
    {0, 255, 0},
    {0, 255, 255},
    {255, 0, 0},
    {255, 0, 255},
    {255, 255, 0},
    {255, 255, 255}
};

void INTERNAL_get_palette_color(G_PALETTEENTRY *g, int color)
{
    color %= g_palettesize;
    g->r = g_palette[color].r;
    g->g = g_palette[color].g;
    g->b = g_palette[color].b;
}


#undef OK
#undef ERROR
/*****************************************************************************/
/*                      DEVICE DEPENDENT PRIMITIVES                          */
/*****************************************************************************/
#ifdef jsssss

#define  GENPLOT_(x) dummy_##x
/*#include "gen_dumm.inc"*/
#undef   GENPLOT_
#define  GENPLOT_(x) screen_##x
#ifdef _Windows
#include "gen_win.inc"
#endif
#ifdef unix
/*#include "gen_xm.inc"*/
#endif
#undef   GENPLOT_
#define  GENPLOT_(x) ps_##x
/*#include "gen_ps.inc"*/
#undef   GENPLOT_
#define  GENPLOT_(x) hpgl_##x
/*#include "gen_hpgl.inc"*/
#undef   GENPLOT_
/*#define  GENPLOT_(x) wmf_##x*/
/*#include "gen_wmf.inc"*/
#undef   GENPLOT_

#endif

static void init_palette(void)
{
    int i;

    g_palette_readonly = FALSE;
    if ((g_palette = (G_PALETTEENTRY*)
            malloc(sizeof(G_PALETTEENTRY) * DEFAULT_palettesize)) == NULL) {
        fprintf(stderr,"Memory allocation error\n");
        exit(1);
    }
    g_palettesize = DEFAULT_palettesize;
    for (i=0; i<DEFAULT_palettesize; i++) {
        g_palette[i].r = DEFAULT_paletteentries[i].r;
        g_palette[i].g = DEFAULT_paletteentries[i].g;
        g_palette[i].b = DEFAULT_paletteentries[i].b;
    }
}

static void kill_palette(void)
{
    if (g_palette)
        free(g_palette);
    g_palette = NULL;
}

/*****************************************************************************/
/*  Some private functions                                                   */
/*****************************************************************************/

static void activate_gc(GRAPHCONTEXT *gc)
{
    g_DIRECT_set_clipping(gc->isclipped);
    g_DIRECT_set_linewidth(gc->linewidth);
    g_DIRECT_set_linestyle(gc->linepat);
    g_DIRECT_set_foreground(gc->fcolor);
    g_DIRECT_set_background(gc->bcolor);
    g_DIRECT_set_charsize(gc->charsize);
    g_DIRECT_set_textdirection(gc->textdirection);
}

static void activate_device(int device)
{
    if (!g_dev[device].isopen || device >= G_MAXDEVICE)
        return;
    gen_device        = device;
    calc_transform_param();
}

static float devicex(float x)
{
    x = x * store_param.ax - store_param.bx;
    if (x<0)
        return max(INT_MIN,ceil(x+0.5));
    return min(INT_MAX,floor(x+0.5));
}

static float devicey(float y)
{
    y = y * store_param.ay - store_param.by;
    if (y<0)
        return max(INT_MIN,ceil(y+0.5));
    return min(INT_MAX,floor(y+0.5));
}

static float device_dx(float x)  
{
    x = labs(x * store_param.ax);
    return min(INT_MAX,floor(x+0.5));
}

static float device_dy(float y)
{
    y = labs(y * store_param.ay);
    return min(INT_MAX,floor(y+0.5));
}


static void calc_transform_param(void)
{
    if (GD.isopen) {
        VIEWPORTTYPE *vp = &INTERNAL_store_vp;
        STORETYPE *par = &store_param;
        if ( vp->wx2 == vp->wx1 ||
             vp->wy2 == vp->wy1 ||
             store_param.vxmax  == 0 ||
             store_param.vymax  == 0)
             return;
        par->vxmax = round((float) ((float) WINWIDTH * (vp->vx2 - vp->vx1)));
        par->vymax = round((float) ((float) WINHEIGHT * (vp->vy2 - vp->vy1)));
        par->vx1   = round((float) ((float) WINWIDTH * vp->vx1));
        par->vy1   = round((float) ((float) WINHEIGHT * vp->vy1));
        par->vxmax = max(1,par->vxmax);
        par->vymax = max(1,par->vymax);

        par->ax = (float) par->vxmax / (vp->wx2 - vp->wx1);
        par->bx = par->ax * vp->wx1 - (float) par->vx1;
        par->ay = (float) par->vymax / (vp->wy2 - vp->wy1);
        par->by = par->ay * vp->wy1 - (float) par->vy1;
        if (GD.isupsidedown) {
            par->ay *= -1;
            par->by = (float) -WINHEIGHT - par->by;
        }
        par->cx = (vp->wx2 - vp->wx1) / (float) par->vxmax;
        par->dx = par->cx * (float) par->vx1 - vp->wx1;

        par->cy = (vp->wy2 - vp->wy1) / (float) par->vymax;
        par->dy = par->cy * (float) par->vy1 - vp->wy1;
        if (GD.isupsidedown) {
            par->cy *= -1;
            par->dy += (float) WINHEIGHT * par->cy;
        }

        INTERNAL_set_clip(&store_param);
        g_DIRECT_set_clipping(INTERNAL_store_gc.isclipped);
    }
}

void INTERNAL_calc_viewport_xy(int win_id, int vp_id,
                      long xx, long yy, float *x, float *y)
{
    VIEWPORTTYPE *vp;
    int INTERNAL_store_window;

    if (vp_id == G_ERROR) {
        *x = usercoorx((float)xx);
        *y = usercoory((float)yy);
        return;
    }
    g_push_viewport();
    INTERNAL_store_window   = GD.win_id;
    GD.win_id = GET_LOCAL_ID(win_id);
    vp=INTERNAL_get_viewport(vp_id);
    memcpy(&INTERNAL_store_vp, vp, sizeof(VIEWPORTTYPE));
    calc_transform_param();
    *x = usercoorx((float)xx);
    *y = usercoory((float)yy);
    GD.win_id = INTERNAL_store_window;
    g_pop_viewport();
}

void INTERNAL_calc_corresponding_xy(int win_id, int vp_id1, int vp_id2,
                           float *x, float *y)
{
    VIEWPORTTYPE *vp;
    int INTERNAL_store_window;
    long xx,yy;

    if (vp_id1 == G_ERROR || vp_id2 == G_ERROR || vp_id1 == vp_id2) 
        return;
    g_push_viewport();
    INTERNAL_store_window   = GD.win_id;
    GD.win_id = GET_LOCAL_ID(win_id);
    vp=INTERNAL_get_viewport(vp_id2);
    memcpy(&INTERNAL_store_vp, vp, sizeof(VIEWPORTTYPE));
    calc_transform_param();
    xx = devicex(*x);
    yy = devicey(*y);
    vp=INTERNAL_get_viewport(vp_id1);
    memcpy(&INTERNAL_store_vp, vp, sizeof(VIEWPORTTYPE));
    calc_transform_param();
    *x = usercoorx((float)xx);
    *y = usercoory((float)yy);
    GD.win_id = INTERNAL_store_window;
    g_pop_viewport();
}

void INTERNAL_calc_viewport_rect(int win_id, int vp_id,
                      int *x1, int *y1, int *x2, int *y2)
{
    VIEWPORTTYPE *vp;
    int store_window;

    if (vp_id == G_ERROR) {
        *x1 = 0;
        *y1 = 0;
        *x2 = 0;
        *y2 = 0;
        return;
    }
    g_push_viewport();
    store_window   = GD.win_id;
    GD.win_id = GET_LOCAL_ID(win_id);
    vp=INTERNAL_get_viewport(vp_id);
    memcpy(&INTERNAL_store_vp, vp, sizeof(VIEWPORTTYPE));
    calc_transform_param();
    INTERNAL_get_clip(x1, y1, x2, y2);
    GD.win_id = store_window;
    g_pop_viewport();
}

void INTERNAL_add_list2history(int list)
{
    HISTORYTYPE *h;

    h = GWIN.first;
    while (h != NULL) {
        if (h->list == list)
            return;
        h = h->next;
    }
    if ((h = (HISTORYTYPE*) malloc(sizeof(HISTORYTYPE))) == NULL) {
        fprintf(stderr,"Memory allocation error\n");
        exit(1);
    }
    h->list = list;
    h->next = NULL;
    if (GWIN.last == NULL) {
        memcpy(&GWIN.vp_init, &INTERNAL_store_vp, sizeof(VIEWPORTTYPE));
        memcpy(&GWIN.gc_init, &INTERNAL_store_gc, sizeof(GRAPHCONTEXT));
        GWIN.first = h;
    }
    else
        GWIN.last->next = h;
    GWIN.last = h;
}

/* --- Only primary objects
static int get_objects_in_window(int win_id, int *obj_list)
{
    HISTORYTYPE *h;
    int count, id, device;

    id = GET_LOCAL_ID(win_id);
    device = GET_DEVICE(win_id);
    if (id < 0 || id >= G_MAXWINDOW)
        return 0;
    if (device < 0 || device >= G_MAXDEVICE)
        return 0;
    count = 0;
    h = g_dev[device].win[id].first;
    while (h != NULL) {
        if (obj_list)
            obj_list[count] = h->list;
        count++;
        h = h->next;
    }
    return count;
}

int g_count_objects_in_window(int win_id)
{
    return get_objects_in_window(win_id, NULL);
}

int g_get_objects_in_window(int win_id, int *obj_list)
{
    return get_objects_in_window(win_id, obj_list);
}
----------------- */

static void kill_history(void)
{
    HISTORYTYPE *h1, *h2;

    if (!GWIN.isopen)
        return;
    h1 = GWIN.first;
    while (h1 != NULL) {
        h2 = h1->next;
        free(h1);
        h1 = h2;
    }
    GWIN.first = NULL;
    GWIN.last  = NULL;
}


static void call_history(void)
{
    HISTORYTYPE *h;

    if (!GWIN.isopen)
        return;
    h = GWIN.first;
    while (h != NULL) {
        g_call_object(h->list);
        h = h->next;
    }
}


int INTERNAL_update_window(int device, int id)
{
    int new_win, old_win;

    if (id < 0 || id >= G_MAXWINDOW)
        return G_ERROR;
    if (device < 0 || device >= G_MAXDEVICE)
        return G_ERROR;
    if (!g_dev[device].win[id].isopen)
        return G_ERROR;
    /* --- make 'id' the active window --- */
    g_push_gc();
    g_push_viewport();
    if (device != gen_device || id != g_dev[device].win_id) {
        old_win = MAKE_GLOBAL_ID(g_dev[gen_device].win_id, gen_device);
        new_win = MAKE_GLOBAL_ID(id, device);
        g_select_window(new_win);
    }
    else
        old_win = G_ERROR;
    g_plotall();
    /* --- reset to the original window --- */
    if (old_win != G_ERROR)
        g_select_window(old_win);
    g_pop_gc();
    g_pop_viewport();
    return G_OK;
}

/*****************************************************************************/
/*                      DEVICE INDEPENDENT PRIMITIVES                        */
/*****************************************************************************/


int  g_DIRECT_push_gc(void)
{
    if (gc_stack_pointer >= G_MAXGCSTACKSIZE-1)
        return G_ERROR;
    gc_stack_pointer++;
    memcpy(&gc_stack[gc_stack_pointer], &INTERNAL_store_gc, sizeof(GRAPHCONTEXT));
    return G_OK;
}

int  g_DIRECT_pop_gc(void)
{
    if (gc_stack_pointer < 0)
        return G_ERROR;
    memcpy(&INTERNAL_store_gc, &gc_stack[gc_stack_pointer], sizeof(GRAPHCONTEXT));
    gc_stack_pointer--;
    activate_gc(&INTERNAL_store_gc);
    return G_OK;
}

int  g_DIRECT_push_viewport(void)
{
    if (vp_stack_pointer >= G_MAXVPSTACKSIZE-1)
        return G_ERROR;
    vp_stack_pointer++;
    memcpy(&vp_stack[vp_stack_pointer], &INTERNAL_store_vp, sizeof(VIEWPORTTYPE));
    return G_OK;
}

int  g_DIRECT_pop_viewport(void)
{
    if (vp_stack_pointer < 0)
        return G_ERROR;
    memcpy(&INTERNAL_store_vp, &vp_stack[vp_stack_pointer], sizeof(VIEWPORTTYPE));
    vp_stack_pointer--;
    calc_transform_param();
    return G_OK;
}


void g_get_xy(float *x, float *y)
{
    *x = INTERNAL_store_vp.wx;
    *y = INTERNAL_store_vp.wy;
}


void g_DIRECT_moveto(float x, float y)
{
    float xt,yt;

    xt = INTERNAL_store_vp.wx;
    yt = INTERNAL_store_vp.wy;
    INTERNAL_store_vp.wx = x;
    INTERNAL_store_vp.wy = y;
    x = devicex(x);
    y = devicey(y);
    INTERNAL_store_vp.cx = (long) x;
    INTERNAL_store_vp.cy = (long) y;
    if (gen_softclip) {
        xt = devicex(xt);
        yt = devicey(yt);
        if (! INTERNAL_clip(&xt,&yt,&x,&y))
            return;
        INTERNAL_store_vp.clip_cx = (long) x;
        INTERNAL_store_vp.clip_cy = (long) y;
    }
    g_plotfunc[gen_device].moveto((long) x, (long) y);
}

void g_DIRECT_lineto(float x, float y)
{
    float xclip,yclip;
    int is_clipped = FALSE;

    xclip = INTERNAL_store_vp.wx;
    yclip = INTERNAL_store_vp.wy;
    INTERNAL_store_vp.wx = x;
    INTERNAL_store_vp.wy = y;
    x = devicex(x);
    y = devicey(y);
    xclip = devicex(xclip);
    yclip = devicey(yclip);
    INTERNAL_store_vp.cx = (long) x;
    INTERNAL_store_vp.cy = (long) y;
    if (gen_softclip){
        if (! INTERNAL_clip(&xclip,&yclip,&x,&y))
            return;
        if (((long) xclip != INTERNAL_store_vp.clip_cx) || 
            ((long) yclip != INTERNAL_store_vp.clip_cy))
            is_clipped = TRUE;
        INTERNAL_store_vp.clip_cx = (long) x;
        INTERNAL_store_vp.clip_cy = (long) y;
    }
    g_plotfunc[gen_device].line(xclip, yclip, x, y, is_clipped);
}

void g_DIRECT_label(char *label)
{
    if (label == NULL || strlen(label) == 0)
        return;
    if (gen_softclip){
        float x,y;
        x=INTERNAL_store_vp.cx;
        y=INTERNAL_store_vp.cy;
        if (! INTERNAL_clip(&x,&y,&x,&y))
            return;
    }
    g_plotfunc[gen_device].label(label);
}

void g_DIRECT_rectangle(float x1, float y1, float x2, float y2)
{
    INTERNAL_store_vp.wx = x1;
    INTERNAL_store_vp.wy = y1;
    x1 = devicex(x1);
    y1 = devicey(y1);
    x2 = devicex(x2);
    y2 = devicey(y2);
    INTERNAL_store_vp.cx = (long) x1;
    INTERNAL_store_vp.cy = (long) y1;
    if (gen_softclip) {
       int doit = -4;
       if (! INTERNAL_clip(&x1,&y1,&x1,&y2))
            doit++;
       if (! INTERNAL_clip(&x1,&y2,&x2,&y2))
            doit++;
       if (! INTERNAL_clip(&x2,&y2,&x2,&y1))
            doit++;
       if (! INTERNAL_clip(&x2,&y1,&x1,&y1))
            doit++;
       if (!doit)
           return;
       INTERNAL_store_vp.clip_cx = (long) x1;
       INTERNAL_store_vp.clip_cy = (long) y1;
    }
    g_plotfunc[gen_device].rectangle(x1, y1, x2, y2);
}

void g_DIRECT_drawpoly(long numpoint, float *points)
{
    int  i, j, accept, size;
    polytype *poly;
    float xx, yy, x1, y1, x2, y2;

    if (numpoint <= 1)
        return;
    size = (int) (numpoint*2*sizeof(polytype));
    if ((poly = (polytype*) malloc(size)) == NULL) {
        fprintf(stderr,"Memory allocation error\n");
        exit(1);
    }
    if (gen_softclip) for (i = 0, j = 0; i < numpoint * 2; i += 2) {
        if (i == 0) {
            xx = devicex(points[i]);
            yy = devicey(points[i+1]);
            i += 2;
        }
        INTERNAL_store_vp.wx = points[i];
        INTERNAL_store_vp.wy = points[i+1];
        x1 = xx;
        y1 = yy;
        xx = x2 = devicex(points[i]);
        yy = y2 = devicey(points[i+1]);
        if (!INTERNAL_clip(&x1,&y1,&x2,&y2)) {
            if (j) {
                INTERNAL_store_vp.clip_cx = (long) poly[j-2];
                INTERNAL_store_vp.clip_cy = (long) poly[j-1];
                g_plotfunc[gen_device].drawpoly(j/2, poly);
            }
            j=0;
            continue;
        }
        if (j && (poly[j-2] != (polytype) x1 || poly[j-1] != (polytype) y1)) {
            g_plotfunc[gen_device].drawpoly(j/2, poly);
            j=0;
        }
        if (j == 0) {
            poly[j++] = (polytype) x1;
            poly[j++] = (polytype) y1;
        }
        poly[j++] = (polytype) x2;
        poly[j++] = (polytype) y2;
    }
    else for (i = 0, j = 0; i < numpoint * 2; i += 2) {
        INTERNAL_store_vp.wx = points[i];
        INTERNAL_store_vp.wy = points[i+1];
        poly[j++]   = (polytype) devicex(INTERNAL_store_vp.wx);
        poly[j++]   = (polytype) devicey(INTERNAL_store_vp.wy);
    }
    if (j)  {
        INTERNAL_store_vp.cx = (long) poly[j - 2];
        INTERNAL_store_vp.cy = (long) poly[j - 1];
        g_plotfunc[gen_device].drawpoly(j/2, poly);
    }
    free(poly);
}

void g_DIRECT_circle(float r)
{
    if (gen_softclip) {
        long dx = device_dx(r);
        if (INTERNAL_box_outside_clip_area(
                GD.isclipped,
                (int) INTERNAL_store_vp.cx - dx,
                (int) INTERNAL_store_vp.cy - dx,
                (int) INTERNAL_store_vp.cx + dx,
                (int) INTERNAL_store_vp.cy + dx))
                    return;
    }
    g_plotfunc[gen_device].circle(device_dx(r));
}

void g_DIRECT_fillrectangle(float x1, float y1, float x2, float y2)
{
    INTERNAL_store_vp.wx = x1;
    INTERNAL_store_vp.wy = y1;
    x1 = devicex(x1);
    y1 = devicey(y1);
    x2 = devicex(x2);
    y2 = devicey(y2);
    INTERNAL_store_vp.cx = (long) x1;
    INTERNAL_store_vp.cy = (long) y1;
    if (gen_softclip) {
       int doit = -4;
       if (! INTERNAL_clip(&x1,&y1,&x1,&y2))
            doit++;
       if (! INTERNAL_clip(&x1,&y2,&x2,&y2))
            doit++;
       if (! INTERNAL_clip(&x2,&y2,&x2,&y1))
            doit++;
       if (! INTERNAL_clip(&x2,&y1,&x1,&y1))
            doit++;
/*
       if (!doit)
           return;
           */
       INTERNAL_store_vp.clip_cx = (long) x1;
       INTERNAL_store_vp.clip_cy = (long) y1;
    }
    g_plotfunc[gen_device].fillrectangle(x1, y1, x2, y2);
}


void g_DIRECT_fillpoly(long numpoint, float *points)
{
    int   i, size;
    polytype  *poly;

    if (numpoint <= 0)
        return;
    size = (int) (numpoint*2*sizeof(polytype));
    if ((poly = (polytype*) malloc(size)) == NULL) {
        fprintf(stderr,"Memory allocation error\n");
        exit(1);
    }

    for (i = 0; i < numpoint * 2; i += 2) {
        INTERNAL_store_vp.wx = points[i];
        INTERNAL_store_vp.wy = points[i+1];
        poly[i]   = (polytype) devicex(INTERNAL_store_vp.wx);
        poly[i+1] = (polytype) devicey(INTERNAL_store_vp.wy);
    }
    INTERNAL_store_vp.cx = (long) poly[i-2];
    INTERNAL_store_vp.cy = (long) poly[i-1];
    INTERNAL_store_vp.clip_cx = (long) poly[i-2];
    INTERNAL_store_vp.clip_cy = (long) poly[i-1];
    g_plotfunc[gen_device].fillpoly(numpoint, poly);
    free(poly);
}

void g_DIRECT_fillcircle(float r)
{
    if (gen_softclip) {
        long dx = device_dx(r);
        if (INTERNAL_box_outside_clip_area(
                GD.isclipped,
                (int) INTERNAL_store_vp.cx - dx,
                (int) INTERNAL_store_vp.cy - dx,
                (int) INTERNAL_store_vp.cx + dx,
                (int) INTERNAL_store_vp.cy + dx))
                    return;
    }
    g_plotfunc[gen_device].fillcircle(device_dx(r));
}


void g_set_outfile(FILE *outfile)
{
    g_outfile = outfile;
}

FILE *g_get_outfile(void)
{
    return g_outfile;
}

void g_DIRECT_set_textdirection(int direction)
{
    INTERNAL_store_gc.textdirection = direction;
}

int g_get_textdirection(void)
{
    return INTERNAL_store_gc.textdirection;
}


void g_DIRECT_set_linewidth(int width)
{
    INTERNAL_store_gc.linewidth = width;
    if (width == GGC.linewidth)
        return;
    GGC.linewidth = width;
    g_plotfunc[gen_device].linewidth(GGC.linewidth);
}

int g_get_linewidth(void)
{
    return INTERNAL_store_gc.linewidth;
}

void g_DIRECT_set_linestyle(int pattern)
{
    INTERNAL_store_gc.linepat = pattern;
    if ((unsigned) pattern ==  GGC.linepat)
        return;
    GGC.linepat = pattern;
    g_plotfunc[gen_device].linestyle(GGC.linepat);
}

int g_get_linestyle(void)
{
    return INTERNAL_store_gc.linepat;
}


int g_DIRECT_set_palettesize(int size)
{
    int i;

    if (/*g_palette_readonly ||*/ size < 2)
        return G_ERROR;
    if ((g_palette = (G_PALETTEENTRY*)
            realloc(g_palette, sizeof(G_PALETTEENTRY) * size)) == NULL) {
        fprintf(stderr,"Memory allocation error\n");
        exit(1);
    }
    for (i=g_palettesize; i<size; i++) {
        g_palette[i].r = 0;
        g_palette[i].g = 0;
        g_palette[i].b = 0;
    }
    g_plotfunc[gen_device].palettesize(size);
    g_palettesize = size;
    return G_OK;
}

int g_get_palettesize(void)
{
    return g_palettesize;
}

int g_DIRECT_set_paletteentry(int entry_id, G_PALETTEENTRY entry)
{
    if (/*g_palette_readonly ||*/ entry_id < 0 || entry_id >= g_palettesize)
        return G_ERROR;
    g_palette[entry_id].r = entry.r;
    g_palette[entry_id].g = entry.g;
    g_palette[entry_id].b = entry.b;
    g_plotfunc[gen_device].paletteentry(entry_id, entry);
    return G_OK;
}

int g_get_paletteentry(int entry_id, G_PALETTEENTRY *entry)
{
    if (entry_id < 0 || entry_id >= g_palettesize)
        return G_ERROR;
    entry->r = g_palette[entry_id].r;
    entry->g = g_palette[entry_id].g;
    entry->b = g_palette[entry_id].b;
    return G_OK;
}


void g_DIRECT_set_foreground(int color)
{
    color %= g_palettesize;
    INTERNAL_store_gc.fcolor = color;
/*
    if (color == GGC.fcolor)
        return;
*/
    GGC.fcolor = color;
    g_plotfunc[gen_device].foreground(GGC.fcolor);
}

int g_get_foreground(void)
{
    return INTERNAL_store_gc.fcolor;
}

void g_DIRECT_set_background(int color)
{
    color %= g_palettesize;
    INTERNAL_store_gc.bcolor = color;
/*
    if (color == GGC.bcolor)
        return;
*/
    GGC.bcolor = color;
    g_plotfunc[gen_device].background(GGC.bcolor);
}

int g_get_background(void)
{
    return INTERNAL_store_gc.bcolor;
}

void g_DIRECT_set_charsize(float size)
{
    if (size <= 0)
        size = 1.0;
    INTERNAL_store_gc.charsize = size;
    GGC.charsize = size;
}

float g_get_charsize(void)
{
    return INTERNAL_store_gc.charsize;
}

void g_DIRECT_set_font(int font, int scaling)
{
    if (font < 0 || font > G_MAXFONT)
        font = DEFAULT_font;
    if (scaling < 0 || scaling > G_RELATIVE_FONTSCALING)
        scaling = DEFAULT_fontscaling;
    INTERNAL_store_gc.font         = font;
    INTERNAL_store_gc.fontscaling  = scaling;
    GGC.font              = font;
    GGC.fontscaling       = scaling;
}

void g_get_font(int *font, int *scaling)
{
    *font    = INTERNAL_store_gc.font;
    *scaling = INTERNAL_store_gc.fontscaling;
}

float g_get_fontheight(int win_id)
{
    int id, device;

    id = GET_LOCAL_ID(win_id);
    device = GET_DEVICE(win_id);
    if (id < 0 || id >= G_MAXWINDOW)
        return 1;
    if (device < 0 || device >= G_MAXDEVICE)
        return 1;
    return g_plotfunc[device].fontheight(id);
}

void g_DIRECT_set_clipping(int clip)
{
/*
    if (GD.clipxy[0] == store_param.vx1 && GD.clipxy[1] == store_param.vxmax &&
        GD.clipxy[2] == store_param.vy1 && GD.clipxy[3] == store_param.vymax &&
        clip == GD.isclipped) {
            INTERNAL_store_gc.isclipped = clip;
            return;
    }
    */
    GD.clipxy[0] = store_param.vx1;
    GD.clipxy[1] = store_param.vxmax;
    GD.clipxy[2] = store_param.vy1;
    GD.clipxy[3] = store_param.vymax;
    if (clip == TRUE && GD.isclipped == TRUE)
        g_plotfunc[gen_device].clipping(FALSE);
    GD.isclipped = INTERNAL_store_gc.isclipped = clip;
    if (!GD.able2clip) {
        gen_softclip = TRUE;
        INTERNAL_set_page_clip(clip);
    }
    else
        gen_softclip = FALSE;
    g_plotfunc[gen_device].clipping(clip);
}

int g_get_clipping(void)
{
    return INTERNAL_store_gc.isclipped;
}

void g_newpage(void)
{
    kill_history();
    INTERNAL_reset_viewport_history(MAKE_GLOBAL_ID(GD.win_id, gen_device));
    g_plotfunc[gen_device].newpage();
}

void g_plotall(void)
{
    /* --- restore the state when the window was created --- */
    memcpy(&INTERNAL_store_vp, &GWIN.vp_init, sizeof(VIEWPORTTYPE));
    calc_transform_param();
    activate_gc(&GWIN.gc_init);
    /* --- play the history list of the window --- */
    INTERNAL_object_playback_flag = TRUE;
    call_history();
    INTERNAL_object_playback_flag = FALSE;
    g_flush();
}

void g_set_cursortype(int win, int type)
{
    int win_id, device;

    win_id = GET_LOCAL_ID(win);
    device = GET_DEVICE(win);
    if (win_id < 0 || win_id >= G_MAXWINDOW)
        return;
    if (device < 0 || device >= G_MAXDEVICE)
        return;
    g_dev[device].win_id = win_id;
    g_dev[device].win[g_dev[device].win_id].cursortype = type;

    if (!g_dev[device].isopen)
        return;
    if (!g_dev[device].win[g_dev[device].win_id].isopen)
        return;

    INTERNAL_cursortype(g_dev[device].win_id, type);
}

int g_get_cursortype(int win)
{
    int win_id, device;

    if (!g_dev[G_SCREEN].isopen)
        return G_ERROR;
    win_id = GET_LOCAL_ID(win);
    device = GET_DEVICE(win);
    if (win_id < 0 || win_id >= G_MAXWINDOW)
        return G_ERROR;
    if (device < 0 || device >= G_MAXDEVICE)
        return G_ERROR;
    return g_dev[device].win[g_dev[device].win_id].cursortype;
}

void g_set_cursorposition(float x, float y)
{
    if (!g_dev[G_SCREEN].isopen)
        return;
    x = devicex(x);
    y = devicey(y);
    INTERNAL_warpcursor((long) x, (long) y);
}

int g_get_cursorposition(float *x, float *y)
{
    if (!g_dev[G_SCREEN].isopen)
        return FALSE;
    return INTERNAL_cursorpos(x, y);
}

int g_DIRECT_set_world(int no,float x1,float y1,float x2,float y2)
{
    if (no < 0)
        return G_ERROR;
    g_viewport = INTERNAL_get_viewport(no);
    g_viewport->wx1 = x1;
    g_viewport->wy1 = y1;
    g_viewport->wx2 = x2;
    g_viewport->wy2 = y2;
    g_viewport->wx  = x1;
    g_viewport->wy  = y1;
    g_viewport->cx  = x1;
    g_viewport->cy  = y1;
    if (g_viewport->id == no) {
        memcpy(&INTERNAL_store_vp, g_viewport, sizeof(VIEWPORTTYPE));
        calc_transform_param();
    }
    return G_OK;
}

void g_get_world(int vp_id, float *x1,float *y1,float *x2,float *y2)
{
    VIEWPORTTYPE *vp;

    vp = INTERNAL_get_viewport(vp_id);
    *x1 = vp->wx1;
    *y1 = vp->wy1;
    *x2 = vp->wx2;
    *y2 = vp->wy2;
}

int g_DIRECT_set_viewport(int no,float x1,float y1,float x2,float y2)
{
    if (no < 0 || x1 == x2 || y1 == y2)
        return G_ERROR;
    if (x1 < 0.0 || x1 > 1.0)
        return G_ERROR;
    if (y1 < 0.0 || y1 > 1.0)
        return G_ERROR;
    if (x2 < 0.0 || x2 > 1.0)
        return G_ERROR;
    if (y2 < 0.0 || y2 > 1.0)
        return G_ERROR;
    g_viewport = INTERNAL_get_viewport(no);
    g_viewport->vx1 = min(x1, x2);
    g_viewport->vy1 = min(y1, y2);
    g_viewport->vx2 = max(x1, x2);
    g_viewport->vy2 = max(y1, y2);
    if (g_viewport->id == no)  {
        memcpy(&INTERNAL_store_vp, g_viewport, sizeof(VIEWPORTTYPE));
        calc_transform_param();
    }
    return G_OK;
}

void g_get_viewport(int vp_id,float *x1,float *y1,float *x2,float *y2)
{
    VIEWPORTTYPE *vp;

    vp = INTERNAL_get_viewport(vp_id);
    *x1 = vp->vx1;
    *y1 = vp->vy1;
    *x2 = vp->vx2;
    *y2 = vp->vy2;
}

int g_DIRECT_select_viewport(int no)
{
    if (no < 0)
        return G_ERROR;
    g_viewport = INTERNAL_get_viewport(no);
    memcpy(&INTERNAL_store_vp, g_viewport, sizeof(VIEWPORTTYPE));
    calc_transform_param();
    INTERNAL_add_viewport_history(MAKE_GLOBAL_ID(GD.win_id, gen_device), g_viewport);
    return G_OK;
}

int g_get_viewportnr(void)
{
    return g_viewport->id;
}

void g_translate_viewport_xy(int win_id, int vp_id1, int vp_id2,
                           float *x, float *y)
{
    INTERNAL_calc_corresponding_xy(win_id, vp_id1, vp_id2, x, y);
}

int g_delete_viewport(int id)
{
    if (id == DEFAULT_viewport)
        return G_ERROR;
    if (g_viewport->id == id)
        g_select_viewport(DEFAULT_viewport);
    return INTERNAL_delete_viewport(id);
}

void g_DIRECT_clear_viewport(void)
{
    g_plotfunc[gen_device].clearviewport();
}

int g_get_windownr(void)
{
    return MAKE_GLOBAL_ID(g_dev[gen_device].win_id, gen_device);
}

int g_open_window(int device, float x0,float y0,float width,float height,
                  char *title,long flag)
{
    int id;

    if (!g_dev[device].isopen)
        if (g_open_device(device) == G_ERROR)
            return G_ERROR;
    if (width <= 0)
        width = *g_plotfunc[device].device_width;
    if (height <= 0)
        height = *g_plotfunc[device].device_height;
    for (id=0; id<G_MAXWINDOW; id++)
        if (g_dev[device].win[id].isopen == FALSE)
            break;
    if ((id == G_MAXWINDOW) ||  (id > 0 && !GD.multiwindows)) {
        fprintf(stderr, "Error: No more windows available\n");
        return G_ERROR;
    }
    g_dev[device].win[id].win_id    = INTERNAL_new_global_win_id(device, id);
    g_dev[device].win[id].x0        = x0 * (*g_plotfunc[device].dots_per_cm);
    g_dev[device].win[id].y0        = y0 * (*g_plotfunc[device].dots_per_cm);
    g_dev[device].win[id].width     = width * (*g_plotfunc[device].dots_per_cm);
    g_dev[device].win[id].height    = height * (*g_plotfunc[device].dots_per_cm);
    g_dev[device].win[id].flag      = flag;
    g_dev[device].win[id].first     = NULL;
    g_dev[device].win[id].last      = NULL;
    g_plotfunc[device].open_window(id,
                                   x0 * (*g_plotfunc[device].dots_per_cm),
                                   y0 * (*g_plotfunc[device].dots_per_cm),
                                   width * (*g_plotfunc[device].dots_per_cm),
				   height * (*g_plotfunc[device].dots_per_cm),
				   title,
				   flag);
    /* --- activate the window --- */
    g_dev[device].win_id = id;
    activate_device(device);
    g_plotfunc[device].select_window(id);
    activate_device(device);
    INTERNAL_init_viewport_history(MAKE_GLOBAL_ID(id, device));
    g_dev[device].win[id].isopen    = TRUE;
    return MAKE_GLOBAL_ID(id, device);
}

void g_close_window(int id)
{
    int win_id, device;
    HISTORYTYPE *h1, *h2;

    win_id = GET_LOCAL_ID(id);
    device = GET_DEVICE(id);
    if (win_id < 0 || win_id >= G_MAXWINDOW)
        return;
    if (device < 0 || device >= G_MAXDEVICE)
        return;
    if (!g_dev[device].win[win_id].isopen)
        return;
    g_plotfunc[device].close_window(win_id);
    g_dev[device].win[win_id].isopen = FALSE;
    h1 = g_dev[device].win[win_id].first;
    while (h1 != NULL) {
        h2 = h1->next;
        free(h1);
        h1 = h2;
    }
    INTERNAL_kill_viewport_history(id);
    g_dev[device].win[g_dev[device].win_id].cursortype = DEFAULT_cursortype;
    if (device != G_DUMMY && device == gen_device && 
        g_dev[device].win_id == win_id)
            g_select_window(gen_dummy_window);
    INTERNAL_delete_win_id(id);
}

int g_select_window(int id)
{
    int win_id, device;

    win_id = GET_LOCAL_ID(id);
    device = GET_DEVICE(id);
    if (win_id < 0 || win_id >= G_MAXWINDOW)
        return G_ERROR;
    if (device < 0 || device >= G_MAXDEVICE)
        return G_ERROR;
    if (!g_dev[device].win[win_id].isopen)
        return G_ERROR;
    g_push_gc();
    g_dev[device].win_id = win_id;
    activate_device(device);
    g_plotfunc[device].select_window(win_id);
    g_pop_gc();
    calc_transform_param();
    return G_OK;
}

int g_raise_window(int id)
{
    int win_id, device;

    win_id = GET_LOCAL_ID(id);
    device = GET_DEVICE(id);
    if (win_id < 0 || win_id >= G_MAXWINDOW)
        return G_ERROR;
    if (device < 0 || device >= G_MAXDEVICE)
        return G_ERROR;
    if (!g_dev[device].win[win_id].isopen)
        return G_ERROR;
    g_plotfunc[device].raise_window(win_id);
    return G_OK;
}

#define SWIN g_dev[src_device].win[src_id]
#define DWIN g_dev[dest_device].win[dest_id]

int g_copy_window(int dest, int src)
{
    int src_id, dest_id, old_win;
    int src_device, dest_device;
    HISTORYTYPE *h;

    if (dest == src)
        return G_ERROR;
    src_id      = GET_LOCAL_ID(src);
    src_device  = GET_DEVICE(src);
    dest_id     = GET_LOCAL_ID(dest);
    dest_device = GET_DEVICE(dest);
    /* --- are the numbers in range --- */
    if (src_id < 0 || src_id >= G_MAXWINDOW ||
        dest_id < 0 || dest_id >= G_MAXWINDOW)
        return G_ERROR;
    if (src_device < 0 || src_device >= G_MAXDEVICE ||
        dest_device < 0 || dest_device >= G_MAXDEVICE)
        return G_ERROR;
    /* --- are the windows open --- */
    if (!SWIN.isopen)
        return G_ERROR;
    if (!DWIN.isopen)
        return G_ERROR;
    /* --- make 'dest' the active window --- */
    if (dest_device != gen_device || dest_id != g_dev[dest_device].win_id) {
        old_win = MAKE_GLOBAL_ID(g_dev[gen_device].win_id, gen_device);
        g_select_window(dest);
    }
    else
        old_win = G_ERROR;
    /* --- clear the destination window --- */
    if (DWIN.first != NULL)
        g_newpage();
    memcpy(&DWIN.vp_init,
           &SWIN.vp_init,
           sizeof(VIEWPORTTYPE));
    calc_transform_param();
    memcpy(&DWIN.gc_init,
           &SWIN.gc_init,
           sizeof(GRAPHCONTEXT));
    activate_gc(&DWIN.gc_init);
    /* --- plot and copy the history --- */
    h = g_dev[src_device].win[src_id].first;
    while (h != NULL) {
        g_call_object(h->list);
        h = h->next;
    }
    /* --- reset to the original window --- */
    if (old_win != G_ERROR)
        g_select_window(old_win);
    return G_OK;
}

int g_get_windowsize(int win_id, float *width, float *height)
{
    int id, device;

    id     = GET_LOCAL_ID(win_id);
    device = GET_DEVICE(win_id);
    if (id < 0 || id >= G_MAXWINDOW ||
        device < 0 || device >= G_MAXDEVICE) {
        *width = 0.0;
        *height = 0.0;
        return G_ERROR;
    }
    *width  = (float)g_dev[device].win[id].width / (*g_plotfunc[device].dots_per_cm);
    *height = (float)g_dev[device].win[id].height/ (*g_plotfunc[device].dots_per_cm);
    return G_OK;
}

int g_get_devicesize(int device, float *width, float *height)
{
    if (device < 0 || device >= G_MAXDEVICE) {
        *width = 0.0;
        *height = 0.0;
        return G_ERROR;
    }
    if (!g_dev[device].isopen)
        if (g_open_device(device) == G_ERROR) {
            *width = 0.0;
            *height = 0.0;
            return G_ERROR;
    }
    *width  =  *g_plotfunc[device].device_width;
    *height =  *g_plotfunc[device].device_height;
    return G_OK;
}

int g_set_devicesize(int device, float width, float height)
{
    if (g_dev[device].isopen) {
        int i;
        for (i=0;i<G_MAXWINDOW;i++)
            if (g_dev[device].win[i].isopen)
                return G_ERROR;
        g_close_device(device);
    }
    *g_plotfunc[device].device_width  = width;
    *g_plotfunc[device].device_height = height;
    return G_OK;
}


int g_get_devicenr(void)
{
    return gen_device;
}

int g_open_device(int device)
{
    int old_device = gen_device;

    if (device >= G_MAXDEVICE || g_dev[device].isopen)
        return G_ERROR;
    if (g_dev[device].isopen == TRUE)
        return G_OK;
    if (device != G_DUMMY)
        g_palette_readonly = TRUE;
    gen_device = device;
    if (g_plotfunc[device].open()==G_ERROR){
        gen_device = old_device;
        return G_ERROR;
    }
    gen_device = old_device;
    g_dev[device].isopen = TRUE;
    return G_OK;
}

void g_close_device(int device)
{
    if (g_dev[device].isopen) {
        int i;

        for (i=0;i<G_MAXWINDOW;i++)
            if (g_dev[device].win[i].isopen == TRUE)
                g_close_window(MAKE_GLOBAL_ID(i, device));
        g_plotfunc[device].close();
        g_dev[device].isopen = FALSE;
    }
}

void  g_set_icon_pixmap(char *icon_64[], char *icon_48[],
                        char *icon_32[], char *icon_16[])
{
    icon16	= icon_16;
    icon32	= icon_32;
    icon48	= icon_48;
    icon64	= icon_64;
}

char **INTERNAL_get_icon_pixmap(int size)
{
    if (size == 16)
        return icon16;
    if (size == 32)
        return icon32;
    if (size == 48)
        return icon48;
    if (size == 64)
        return icon64;
    if (icon64 != NULL)
        return icon64;
    if (icon48 != NULL)
        return icon48;
    if (icon32 != NULL)
        return icon32;
    if (icon16 != NULL)
        return icon16;
    return NULL;
}

void g_init(int Argc, char **Argv)
{
    int i,j;
    char *p;

    INTERNAL_g_argc = Argc;
    INTERNAL_g_argv = Argv;

    if (INTERNAL_g_argc) {
        INTERNAL_g_application = p = INTERNAL_g_argv[0];
        while ((p=strchr(p,'/')) != NULL) {
           p++;
           INTERNAL_g_application = p;
        } 
    }
    else
        INTERNAL_g_application = NULL;

    INTERNAL_init_viewports();
    g_viewport = INTERNAL_get_viewport(DEFAULT_viewport);
    for (i=0;i<G_MAXDEVICE;i++) {
        g_gc[i].isclipped     = DEFAULT_clip;
        g_gc[i].linewidth     = DEFAULT_linewidth;
        g_gc[i].linepat       = DEFAULT_linepat;
        g_gc[i].fcolor        = DEFAULT_fcolor;
        g_gc[i].bcolor        = DEFAULT_bcolor;
        g_gc[i].font          = DEFAULT_font;
        g_gc[i].fontscaling   = DEFAULT_fontscaling;
        g_gc[i].textdirection = DEFAULT_textdirection;
        g_gc[i].charsize      = DEFAULT_charsize;
        g_dev[i].isopen       = FALSE;
        g_dev[i].isupsidedown = *g_plotfunc[i].y_is_upsidedown;
        g_dev[i].able2clip    = *g_plotfunc[i].able2clip;
        g_dev[i].multiwindows = *g_plotfunc[i].multi_windows;
        g_dev[i].isclipped    = FALSE;
        for (j=0;j<4;j++)
            g_dev[i].clipxy[j]= 0;
        g_dev[i].win_id       = DEFAULT_win_id;
        for (j=0;j<G_MAXWINDOW;j++) {
            g_dev[i].win[j].isopen     = FALSE;
            g_dev[i].win[j].x0         = 0;
            g_dev[i].win[j].y0         = 0;
            g_dev[i].win[j].width      = *g_plotfunc[i].device_width;
            g_dev[i].win[j].height     = *g_plotfunc[i].device_height;
            g_dev[i].win[j].flag       = 0;
            g_dev[i].win[j].cursortype = DEFAULT_cursortype;
            memcpy(&g_dev[i].win[j].gc_init, &g_gc[i], sizeof(GRAPHCONTEXT));
            memcpy(&g_dev[i].win[j].vp_init, g_viewport, sizeof(VIEWPORTTYPE));
            g_dev[i].win[j].first      = NULL;
            g_dev[i].win[j].last       = NULL;
        }
    }
    memcpy(&INTERNAL_store_gc, &g_gc[0], sizeof(GRAPHCONTEXT));
    memcpy(&INTERNAL_store_vp, g_viewport, sizeof(VIEWPORTTYPE));
    store_param.vx1             = 0;
    store_param.vy1             = 0;
    store_param.vxmax           = *g_plotfunc[G_DUMMY].device_width;
    store_param.vymax           = *g_plotfunc[G_DUMMY].device_height;
    gen_device            = DEFAULT_device;
    gen_softclip          = DEFAULT_clip;
    g_outfile             = DEFAULT_outfile;
    INTERNAL_init_winIDs();
    INTERNAL_init_objects();
    init_palette();
    INTERNAL_init_events();
    INTERNAL_set_directmode();
    set_DUMMY_mode();
    gc_stack_pointer = -1;
    vp_stack_pointer = -1;
    icon16 = icon32 = icon48 = icon64 = NULL;
    g_open_device(G_DUMMY);
    gen_dummy_window = g_open_window(G_DUMMY,0,0,0,0,"",FALSE);
}

void g_end(void)
{
    int i;

    for (i=G_MAXDEVICE-1;i>0;i--)
        if (g_dev[i].isopen)
            g_close_device(i);
    INTERNAL_destroy_events();
    kill_palette();
    INTERNAL_kill_objects();
    INTERNAL_kill_viewports();
    INTERNAL_kill_winIDs();
}

