/************************************************************************/
/*                               genmcurs.c                             */
/*                                                                      */
/*  Platform : Motif                                                    */
/*  Module   : Genplot plot functions                                   */
/*                                                                      */
/*  Albert van Kuik                                                     */
/************************************************************************/

#include <X11/Xlib.h>
#include <X11/Xutil.h>
#include <X11/cursorfont.h>

#include <X11/StringDefs.h>
#include <X11/Intrinsic.h>
#include <X11/Shell.h>

#include <X11/keysym.h>

#include <Xm/Xm.h>
#include <Xm/DialogS.h>
#include <Xm/DrawingA.h>
#include <Xm/Form.h>
#include <Xm/CascadeBG.h>
#include <Xm/SeparatoG.h>
#include <Xm/PushBG.h>
#include <Xm/PushB.h>
#include <Xm/ToggleBG.h>
#include <Xm/RowColumn.h>
#include <Xm/ScrolledW.h>
#include <Xm/ScrollBar.h>
#include <Xm/Command.h>
#include <Xm/Text.h>
#include <Xm/MessageB.h>
#include <Xm/SelectioB.h>
#include <Xm/FileSB.h>
#include <Xm/List.h>
#include <Xm/Frame.h>
#include <Xm/MainW.h>
#include <Xm/ToggleB.h>
#include <Xm/Label.h>
#include <Xm/PanedW.h>
#include <Xm/DrawnB.h>

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
#include <time.h>
#include <stdarg.h>

#ifdef DEBUG
#include "mshell.h"
#endif

#include "genplot.h"
#include "g_inter.h"
#include "genmotif.h"


/**********************************************************************
* CURSOR FUNCTIONS
*/

static GC gc_pix, gc_crosshair;

static void xx_default_cursor_func(int id, int x, int y);
static void xx_rubberbox_func(int id, int x, int y);
static void xx_rubberline_func(int id, int x, int y);
static void xx_staticbox_func(int id, int x, int y);
static void Postit(Widget w, XtPointer xpopup, XEvent *xevent, Boolean *bool);
static void init_cursor(void);

static Cursor cur_cross, cur_hand, cur_wait, cur_updown;
extern int xlib_fpixel;

/*=======================================================*/
/* Move viewport with HAND cursor
*/

static Pixmap pixtemp = 0;
static int tempx, tempy;
static int tempx1, tempy1, temp_dx, temp_dy;
static int pixclamp, clampx1, clampy1, clampx2, clampy2;
static int tempbufflag;
static void copy_pixtemp(int id, int x, int y);


void exit_pixtemp(int id, int vp_id,int x, int y)
{
    float dx, dy;

    if (! xwini[id].xlib_pixcopymode)
        return;
    x -= tempx;
    y -= tempy;
    if (pixclamp) {
        x=max(clampx1,x);
        x=min(clampx2,x);
        y=max(clampy1,y);
        y=min(clampy2,y);
    }
    XFreePixmap(INTERNAL_display, pixtemp);
    xwini[id].xlib_pixcopymode = FALSE;
    if (temp_dx == 0 || temp_dy == 0)
        return;
    dx = (float) x /(float) temp_dx;
    dy = (float) y /(float) temp_dy;
    if (tempbufflag) {
        xwini[id].double_buffering  = TRUE;
        xwini[id].xdraw = xwini[id].xpix;
    }
/* dy is upside down */
    if (INTERNAL_update_scrollbars(id, vp_id, -dx, dy) == G_ERROR)
        if (xwini[id].double_buffering)
            XCopyArea(INTERNAL_display, xwini[id].xwin, xwini[id].xpix, 
                  gc_pix, tempx1, tempy1, temp_dx, temp_dy, tempx1, tempy1);
                
}


void init_pixtemp(int id, int vp_id,int x, int y, int x1, int y1, int x2, int y2)
{
    float xx1,yy1,xx2,yy2;

    tempx1 = x1;
    tempy1 = y1;
    temp_dx = x2 - x1 + 1;
    temp_dy = y2 - y1 + 1;
    tempx = x;
    tempy = y;
/* yy is upside down , so wap x1 and x2 (??) */
    if (INTERNAL_get_clampscrollbars(id,vp_id,&xx1,&yy1,&xx2,&yy2) == G_OK) {
        float tmp = xx1;
        xx1 = xx2;
        xx2 = tmp;
        clampx1 = -xx1 * temp_dx;
        clampy1 = -yy1 * temp_dy;
        clampx2 =  xx2 * temp_dx;
        clampy2 =  yy2 * temp_dy;
        pixclamp = TRUE;
/*
fprintf(stderr,"%d %d %d %d clamp\n",clampx1,clampx2,clampy1,clampy2);
*/
    }
    else
        pixclamp = FALSE;
    pixtemp = XCreatePixmap(INTERNAL_display, INTERNAL_rootwin, 
                              temp_dx, temp_dy, 
                              DefaultDepth(INTERNAL_display,INTERNAL_screen));
    XCopyArea(INTERNAL_display, xwini[id].xdraw, pixtemp, 
                  gc_pix, tempx1, tempy1, temp_dx, temp_dy, 0, 0);
    xwini[id].xlib_pixcopymode = TRUE;
    if (xwini[id].double_buffering) {
        tempbufflag = TRUE;
        xwini[id].xdraw = xwini[id].xwin;
        xwini[id].double_buffering = FALSE;
    }
    else
        tempbufflag = FALSE;
}


static void copy_pixtemp(int id, int x, int y)
{
    static XColor gray50;
    static unsigned int gpixel, init;
    int x0,y0,x1,y1,x_abs,y_abs;

    if (! xwini[id].xlib_pixcopymode)
        return;
    if (! init) {
        Colormap cmap = DefaultColormap(INTERNAL_display,INTERNAL_screen);
        XParseColor(INTERNAL_display, cmap, "gray50", &gray50);
        if (XAllocColor(INTERNAL_display, cmap, &gray50))
            gpixel = gray50.pixel;
        else
            gpixel = BlackPixel(INTERNAL_display, INTERNAL_screen);
        init = TRUE;
    }
    x -= tempx;
    y -= tempy;

    if (pixclamp) {
        x=max(clampx1,x);
        x=min(clampx2,x);
        y=max(clampy1,y);
        y=min(clampy2,y);
    }

    x0 = max(0,-x);
    x0 = min(x0,temp_dx);
    y0 = max(0,-y);
    y0 = min(y0,temp_dy);
    x1 = max(0,x);
    x1 = min(x1,temp_dx);
    y1 = max(0,y);
    y1 = min(y1,temp_dy);
    x_abs = abs (x);
    y_abs = abs (y);

    XSetForeground(INTERNAL_display, gc_pix, gpixel);

    if (x1) 
        XFillRectangle(INTERNAL_display,xwini[id].xdraw, gc_pix, 
                   tempx1, tempy1, x1, temp_dy);

    if (y1)
        XFillRectangle(INTERNAL_display,xwini[id].xdraw, gc_pix, 
                   tempx1, tempy1, temp_dx, y1);


    if (x0)
        XFillRectangle(INTERNAL_display,xwini[id].xdraw, gc_pix, 
                   tempx1 + (temp_dx - x_abs), tempy1, x0, temp_dy);

    if (y0)
        XFillRectangle(INTERNAL_display,xwini[id].xdraw, gc_pix, 
                   tempx1, tempy1 + (temp_dy - y_abs), temp_dx, y0);

    XSetForeground(INTERNAL_display,gc_pix,xlib_fpixel);

    XCopyArea(INTERNAL_display, pixtemp, xwini[id].xdraw, gc_pix, 
         x0, y0, temp_dx-x_abs, temp_dy-y_abs, tempx1+x1, tempy1+y1);

    if (xwini[id].double_buffering)
        XCopyArea(INTERNAL_display, xwini[id].xpix, xwini[id].xwin, 
              gc_pix, tempx1, tempy1, temp_dx, temp_dy, tempx1, tempy1);

}


/********************************
* Cursor crosshair
*/

static int device_x(int win_id, int vp_id, float x)
{
    VIEWPORTTYPE *vp;
    long vxmax, vx1;

    if (vp_id == G_ERROR)
        return G_ERROR;
    vp = INTERNAL_get_viewport(vp_id);
    vxmax = round((float) ((float) xwini[win_id].width *
            (vp->vx2 - vp->vx1)));
    vx1   = round((float) ((float) xwini[win_id].width * vp->vx1));
    return (int) ((((x - vp->wx1) / (vp->wx2 - vp->wx1)) * vxmax) + vx1);
}

static int device_y(int win_id, int vp_id, float y)
{
    VIEWPORTTYPE *vp;
    long vymax, vy1;

    if (vp_id == G_ERROR)
        return G_ERROR;
    vp = INTERNAL_get_viewport(vp_id);
    vymax = round((float) ((float) xwini[win_id].height *
            (vp->vy2 - vp->vy1)));
    vy1   = round((float) ((float) xwini[win_id].height * vp->vy1));
    return (int)xwini[win_id].height -
        (int) ((((y - vp->wy1) / (vp->wy2 - vp->wy1)) * vymax) + vy1);
}


static void xx_default_cursor_func(int id, int x, int y)
{
    XDrawLine(INTERNAL_display, xwini[id].xwin, gc_crosshair, 0, y, (int) xwini[id].width, y);
    XDrawLine(INTERNAL_display, xwini[id].xwin, gc_crosshair, x, 0, x, (int) xwini[id].height);
}


static int rbx, rby, bltr;

static void xx_doublecross_func(int id, int x, int y)
{
    XDrawLine(INTERNAL_display, xwini[id].xwin, gc_crosshair, 0, rby, (int) xwini[id].width, rby);
    XDrawLine(INTERNAL_display, xwini[id].xwin, gc_crosshair, rbx, 0, rbx, (int) xwini[id].height);
    if (rby != y)
        XDrawLine(INTERNAL_display, xwini[id].xwin, gc_crosshair, 0, y, (int) xwini[id].width, y);
    if (rbx != x)
        XDrawLine(INTERNAL_display, xwini[id].xwin, gc_crosshair, x, 0, x, (int) xwini[id].height);
}
static void xx_rubberbox_func(int id, int x, int y)
{
    XDrawLine(INTERNAL_display, xwini[id].xwin, gc_crosshair, rbx, rby, rbx,   y);
    XDrawLine(INTERNAL_display, xwini[id].xwin, gc_crosshair, rbx,   y,   x,   y);
    if (rbx != x)
        XDrawLine(INTERNAL_display, xwini[id].xwin, gc_crosshair,   x,   y,   x, rby);
    if (rby != y)
        XDrawLine(INTERNAL_display, xwini[id].xwin, gc_crosshair,   x, rby, rbx, rby);
}

static void xx_rubberline_func(int id, int x, int y)
{
    XDrawLine(INTERNAL_display, xwini[id].xwin, gc_crosshair, rbx, rby, x, y);
}

static void xx_xline_func(int id, int x, int y)
{
    XDrawLine(INTERNAL_display, xwini[id].xwin, gc_crosshair, rbx, 0, rbx, (int) xwini[id].height);
    if (rbx != x)
        XDrawLine(INTERNAL_display, xwini[id].xwin, gc_crosshair, x, 0, x, (int) xwini[id].height);
}

static void xx_yline_func(int id, int x, int y)
{
    XDrawLine(INTERNAL_display, xwini[id].xwin, gc_crosshair, 0, rby, (int) xwini[id].width, rby);
    if (rby != y)
        XDrawLine(INTERNAL_display, xwini[id].xwin, gc_crosshair, 0, y, (int) xwini[id].width, y);
}

static void xx_dummy_func(int id, int x, int y)
{
    id=id;x=x;y=y;
}

#define BL  1
#define BR  2
#define TL  3
#define TR  4

static void xx_staticbox_func(int id, int x, int y)
{
    int dx,dy;

    switch (bltr) {
        case BL:
            dx = x+rbx;
            dy = y-rby;
            break;
        case BR:
            dx = x-rbx;
            dy = y-rby;
            break;
        case TL:
            dx = x+rbx;
            dy = y+rby;
            break;
        case TR:
            dx = x-rbx;
            dy = y+rby;
            break;
        default:
            return;
    }
    XDrawLine(INTERNAL_display, xwini[id].xwin, gc_crosshair, dx, dy, dx,  y);
    XDrawLine(INTERNAL_display, xwini[id].xwin, gc_crosshair, dx,  y,  x,  y);
    XDrawLine(INTERNAL_display, xwini[id].xwin, gc_crosshair,  x,  y,  x, dy);
    XDrawLine(INTERNAL_display, xwini[id].xwin, gc_crosshair,  x, dy, dx, dy);
}


static int save_old_cursortype = G_CURSOR_CROSSHAIR;
void g_set_rubbercursor(int win_id, int vp_id, float x, float y, int type)
{
    int local_id, cursor_hidden=FALSE;
    int dx, dy, x1, y1, x2, y2;

    local_id = GET_LOCAL_ID(win_id);
    if (local_id < 0 || local_id >= G_MAXWINDOW)
        return;
    if ( !(CURTYPE(local_id) == G_CURSOR_CROSSHAIR
         || CURTYPE(local_id) == G_CURSOR_UPDOWN) 
         && ! xwini[local_id].xlib_pixcopymode)
        return;
    if (!xwini[local_id].xx_hidden) {
        INTERNAL_hide_crosshair(local_id);
        cursor_hidden = TRUE;
    }
    dx = device_x(local_id, vp_id, x);
    dy = device_y(local_id, vp_id, y);
    exit_pixtemp(local_id, vp_id, dx, dy);
    switch (type) {
        case G_RUBBER_NONE:
            xwini[local_id].xx_draw_cursor_func = xx_default_cursor_func;
            INTERNAL_cursortype(local_id, save_old_cursortype);
            return;
        case G_RUBBER_CROSSHAIR:
            xwini[local_id].xx_draw_cursor_func = xx_default_cursor_func;
            INTERNAL_cursortype(local_id, G_CURSOR_CROSSHAIR);
            break;
        case G_RUBBER_DOUBLECROSS:
            rbx = dx;
            rby = dy;
            xwini[local_id].xx_draw_cursor_func = xx_doublecross_func;
            INTERNAL_cursortype(local_id, G_CURSOR_CROSSHAIR);
            break;
        case G_RUBBER_XHAIR:
            rbx = dx;
            xwini[local_id].xx_draw_cursor_func = xx_xline_func;
            INTERNAL_cursortype(local_id, G_CURSOR_CROSSHAIR);
            break;
        case G_RUBBER_YHAIR:
            rby = dy;
            xwini[local_id].xx_draw_cursor_func = xx_yline_func;
            INTERNAL_cursortype(local_id, G_CURSOR_CROSSHAIR);
            break;
        case G_RUBBER_BOX:
            rbx = dx;
            rby = dy;
            xwini[local_id].xx_draw_cursor_func = xx_rubberbox_func;
            INTERNAL_cursortype(local_id, G_CURSOR_CROSSHAIR);
            break;
        case G_RUBBER_LINE:
            rbx = dx;
            rby = dy;
            xwini[local_id].xx_draw_cursor_func = xx_rubberline_func;
            INTERNAL_cursortype(local_id, G_CURSOR_CROSSHAIR);
            break;
        case G_RUBBER_BOX_BL:
            rbx = dx;
            rby = dy;
            bltr = BL;
            xwini[local_id].xx_draw_cursor_func = xx_staticbox_func;
            INTERNAL_cursortype(local_id, G_CURSOR_CROSSHAIR);
            break;
        case G_RUBBER_BOX_BR:
            rbx = dx;
            rby = dy;
            bltr = BR;
            xwini[local_id].xx_draw_cursor_func = xx_staticbox_func;
            INTERNAL_cursortype(local_id, G_CURSOR_CROSSHAIR);
            break;
        case G_RUBBER_BOX_TL:
            rbx = dx;
            rby = dy;
            bltr = TL;
            xwini[local_id].xx_draw_cursor_func = xx_staticbox_func;
            INTERNAL_cursortype(local_id, G_CURSOR_CROSSHAIR);
            break;
        case G_RUBBER_BOX_TR:
            rbx = dx;
            rby = dy;
            bltr = TR;
            xwini[local_id].xx_draw_cursor_func = xx_staticbox_func;
            INTERNAL_cursortype(local_id, G_CURSOR_CROSSHAIR);
            break;
        case G_RUBBER_PANNER:
        case G_RUBBER_PANNER_BORDER: 
            if (vp_id != G_ERROR) {
                INTERNAL_calc_viewport_rect(win_id, vp_id, &x1,&y1,&x2,&y2);
                if (type == G_RUBBER_PANNER_BORDER)
                    init_pixtemp(local_id, vp_id, dx, dy, x1+1, y1+1, x2-1, y2-1);
                else
                    init_pixtemp(local_id, vp_id, dx, dy, x1, y1, x2, y2);
                xwini[local_id].xx_draw_cursor_func = copy_pixtemp;
            }
            else
                xwini[local_id].xx_draw_cursor_func = xx_dummy_func;
            INTERNAL_cursortype(local_id, G_CURSOR_HAND);
            break;
    }
    if (cursor_hidden &&  !(CURTYPE(local_id) == G_CURSOR_HAND 
           ||  CURTYPE(local_id) == G_CURSOR_UPDOWN)) {
        INTERNAL_show_crosshair(local_id,
                             xwini[local_id].xlib_marker_x,
                             xwini[local_id].xlib_marker_y);
    }
}



static void xx_crosshair(int id, int x, int y)
{
    XGrabServer(INTERNAL_display);
    xwini[id].xx_draw_cursor_func(id,x,y);
    XUngrabServer(INTERNAL_display);
    XFlush(INTERNAL_display);
}

void INTERNAL_show_crosshair(int id, int x, int y)
{
    if (!xwini[id].xx_hidden)
        return;
    xwini[id].xx_hidden = FALSE;
    xx_crosshair(id, x, y);
    xwini[id].xlib_marker_x = x;
    xwini[id].xlib_marker_y = y;
}

void INTERNAL_hide_crosshair(int id)
{
    if (xwini[id].xx_hidden)
        return;
    xwini[id].xx_hidden = TRUE;
    if (!xwini[id].xlib_pixcopymode)
        xx_crosshair(id, xwini[id].xlib_marker_x, xwini[id].xlib_marker_y);
}

void INTERNAL_update_crosshair(int id, int x, int y)
{
    if (xwini[id].xx_hidden) {
        INTERNAL_show_crosshair(id, x, y);
        return;
    }
    if (!xwini[id].xlib_pixcopymode)
        xx_crosshair(id, xwini[id].xlib_marker_x, xwini[id].xlib_marker_y);
    xx_crosshair(id, x, y);
    xwini[id].xlib_marker_x = x;
    xwini[id].xlib_marker_y = y;
}

void INTERNAL_hide_cursor_crosshair(int id)
{
    if (CURTYPE(id) == G_CURSOR_CROSSHAIR)
        INTERNAL_hide_crosshair(id);
}

void INTERNAL_show_cursor_crosshair(int id)
{
    int    rootx, rooty;
    Window root, child;
    unsigned int mask;
    int xx,  yy;

    if (CURTYPE(id) == G_CURSOR_CROSSHAIR) {
        if (XQueryPointer(INTERNAL_display,
                           XtWindow(xwini[id].draw_widget),
                           &root,
                           &child,
                           &rootx, &rooty,
                           &xx, &yy,
                           &mask))
/*
printf("%d %d %d %d %d\n",root,child,xx,yy, XtWindow(xwini[id].draw_widget));

        if (child == XtWindow(xwini[id].draw_widget)) {

           if (xwini[id].menubar_widget)
                INTERNAL_show_crosshair(id, xx, yy - MENUBARHEIGHT);
           else
*/
       if (xx >=0 && yy >= 0 && xx <= xwini[id].width && yy <= xwini[id].height) {
                INTERNAL_show_crosshair(id, xx, yy);
       }
    }

}



void INTERNAL_cursortype(int local_id, int type)
{
    if (local_id == GD.win_id) {
        INTERNAL_hide_cursor_crosshair(local_id);
        if (type == G_CURSOR_CROSSHAIR) {
            XDefineCursor(INTERNAL_display, xwini[local_id].xwin, cur_cross);
            save_old_cursortype = type;
        }
        else if (type == G_CURSOR_WAIT) {
            XDefineCursor(INTERNAL_display, xwini[local_id].xwin, cur_wait);
        }
        else if (type == G_CURSOR_HAND) {
            XDefineCursor(INTERNAL_display, xwini[local_id].xwin, cur_hand);
        }
        else if (type == G_CURSOR_UPDOWN) {
            XDefineCursor(INTERNAL_display, xwini[local_id].xwin, cur_updown);
            save_old_cursortype = type;
        }
        else
            XUndefineCursor(INTERNAL_display, xwini[local_id].xwin);
    }
    xwini[local_id].xwin_cursortype = type;
    if (local_id == GD.win_id)
        INTERNAL_show_cursor_crosshair(local_id);
}


int INTERNAL_cursorpos(float *x, float *y)
{
    int    rootx, rooty, wx, wy, ret;
    Window root, child;
    unsigned int mask;
    Widget wid;
    
    *x = *y = 0.0;
    if ((wid=XtWindowToWidget(INTERNAL_display,xwini[GD.win_id].xwin))==NULL)
        return FALSE;
    if (XtIsManaged(wid)==FALSE)
        return FALSE;
    if (XmGetVisibility(wid) == XmVISIBILITY_FULLY_OBSCURED)
        return FALSE;
    ret = (int) XQueryPointer(INTERNAL_display,
                              xwini[GD.win_id].xwin,
                              &root,
                              &child,
                              &rootx, &rooty,
                              &wx, &wy,
                              &mask);
    *x = usercoorx((float) wx);
    *y = usercoory((float) wy);
    return ret;
}

void INTERNAL_warpcursor(long x, long y)
{
    int destx, desty;
    Widget wid;

    if ((wid=XtWindowToWidget(INTERNAL_display,xwini[GD.win_id].xwin))==NULL)
        return;
    if (XtIsManaged(wid)==FALSE)
        return;
    if (XmGetVisibility(wid) == XmVISIBILITY_FULLY_OBSCURED)
        return;
    destx = (int) x;
    desty = (int) y;
    XWarpPointer(INTERNAL_display,
                 None,
                 xwini[GD.win_id].xwin,
                 0, 0,
                 0, 0,
                 destx, desty);
    xwini[GD.win_id].xlib_old_x = destx;
    xwini[GD.win_id].xlib_old_y = desty;
}


typedef struct tagStoreCursor {
    int x, y;
    int rootx, rooty;
    Window r,w;
} StoreCursor;

static StoreCursor SC[G_MAX_STORE_CURSOR+1];

static void init_store_cursor(void)
{
    int i;

    for (i=0;i<=G_MAX_STORE_CURSOR;i++)
        SC[i].w = 0;
}

void g_storecursor(int store_id)
{
    int focusstate;
    Window root, child;
    unsigned int mask;

    if (store_id < 0 || store_id > G_MAX_STORE_CURSOR)
        return;
    XGetInputFocus(INTERNAL_display, &SC[store_id].w, &focusstate);
    if (SC[store_id].w == None || SC[store_id].w == PointerRoot)
        SC[store_id].w = INTERNAL_rootwin;
    XQueryPointer(INTERNAL_display,
                       SC[store_id].w,
                       &root,
                       &child,
                       &SC[store_id].rootx, &SC[store_id].rooty,
                       &SC[store_id].x, &SC[store_id].y,
                       &mask);
}

void g_retrievecursor(int store_id)
{
    Widget wid;
    Window w;
    int x,y;

    if (store_id < 0 || store_id > G_MAX_STORE_CURSOR || SC[store_id].w == 0)
        return;

    wid = XtWindowToWidget(INTERNAL_display,SC[store_id].w);
    if (wid && XtIsManaged(wid) &&
        XmGetVisibility(wid) != XmVISIBILITY_FULLY_OBSCURED) {
        w = SC[store_id].w;
        x = SC[store_id].x;
        y = SC[store_id].y;
    }
    else {
        w = INTERNAL_rootwin;
        x = SC[store_id].rootx;
        y = SC[store_id].rooty;
    }
    XWarpPointer(INTERNAL_display,
                 None,
                 w,
                 0, 0,
                 0, 0,
                 x, y);
    XFlush(INTERNAL_display);
}

#define updown_width 16
#define updown_height 16
static unsigned char updown_bits[] = {
  0x00, 0x06, 0x70, 0x0a, 0x50, 0x12, 0x50, 0x22, 0x50, 0x42, 0x50, 0x7a,
  0x50, 0x0a, 0x50, 0x0a, 0x50, 0x0a, 0x50, 0x0a, 0x5e, 0x0a, 0x42, 0x0a,
  0x44, 0x0a, 0x48, 0x0a, 0x50, 0x0e, 0x60, 0x00, };
#define updownmask_width 16
#define updownmask_height 16
static unsigned char updownmask_bits[] = {
  0x00, 0x06, 0x70, 0x0e, 0x70, 0x1e, 0x70, 0x3e, 0x70, 0x7e, 0x70, 0x7e,
  0x70, 0x0e, 0x70, 0x0e, 0x70, 0x0e, 0x70, 0x0e, 0x7e, 0x0e, 0x7e, 0x0e,
  0x7c, 0x0e, 0x78, 0x0e, 0x70, 0x0e, 0x60, 0x00, };
#define updown32_width 32
#define updown32_height 32
static unsigned char updown32_bits[] = {
  0x00, 0x60, 0x00, 0x00, 0x00, 0x50, 0x00, 0x00, 0x00, 0x48, 0x00, 0x00,
  0x00, 0x44, 0x00, 0x00, 0x00, 0x42, 0x12, 0x00, 0x00, 0x41, 0x12, 0x00,
  0x80, 0x40, 0x12, 0x00, 0x40, 0x40, 0x12, 0x00, 0xc0, 0x4f, 0x12, 0x00,
  0x00, 0x48, 0x12, 0x00, 0x00, 0x48, 0x12, 0x00, 0x00, 0x48, 0x12, 0x00,
  0x00, 0x48, 0x12, 0x00, 0x00, 0x48, 0x12, 0x00, 0x00, 0x48, 0x12, 0x00,
  0x00, 0x48, 0x12, 0x00, 0x00, 0x48, 0x12, 0x00, 0x00, 0x48, 0x12, 0x00,
  0x00, 0x48, 0x12, 0x00, 0x00, 0x48, 0x12, 0x00, 0x00, 0x48, 0x12, 0x00,
  0x00, 0x48, 0x12, 0x00, 0x00, 0x48, 0x12, 0x00, 0x00, 0x48, 0xf2, 0x03,
  0x00, 0x48, 0x02, 0x02, 0x00, 0x48, 0x02, 0x01, 0x00, 0x48, 0x82, 0x00,
  0x00, 0x48, 0x42, 0x00, 0x00, 0x00, 0x22, 0x00, 0x00, 0x00, 0x12, 0x00,
  0x00, 0x00, 0x0a, 0x00, 0x00, 0x00, 0x06, 0x00, };
#define updownmask32_width 32
#define updownmask32_height 32
static unsigned char updownmask32_bits[] = {
  0x00, 0x60, 0x00, 0x00, 0x00, 0x70, 0x00, 0x00, 0x00, 0x78, 0x00, 0x00,
  0x00, 0x7c, 0x00, 0x00, 0x00, 0x7e, 0x1e, 0x00, 0x00, 0x7f, 0x1e, 0x00,
  0x80, 0x7f, 0x1e, 0x00, 0xc0, 0x7f, 0x1e, 0x00, 0xc0, 0x7f, 0x1e, 0x00,
  0x00, 0x78, 0x1e, 0x00, 0x00, 0x78, 0x1e, 0x00, 0x00, 0x78, 0x1e, 0x00,
  0x00, 0x78, 0x1e, 0x00, 0x00, 0x78, 0x1e, 0x00, 0x00, 0x78, 0x1e, 0x00,
  0x00, 0x78, 0x1e, 0x00, 0x00, 0x78, 0x1e, 0x00, 0x00, 0x78, 0x1e, 0x00,
  0x00, 0x78, 0x1e, 0x00, 0x00, 0x78, 0x1e, 0x00, 0x00, 0x78, 0x1e, 0x00,
  0x00, 0x78, 0x1e, 0x00, 0x00, 0x78, 0x1e, 0x00, 0x00, 0x78, 0xfe, 0x03,
  0x00, 0x78, 0xfe, 0x03, 0x00, 0x78, 0xfe, 0x01, 0x00, 0x78, 0xfe, 0x00,
  0x00, 0x78, 0x7e, 0x00, 0x00, 0x00, 0x3e, 0x00, 0x00, 0x00, 0x1e, 0x00,
  0x00, 0x00, 0x0e, 0x00, 0x00, 0x00, 0x06, 0x00, };

#define hand_width 16
#define hand_height 16
static unsigned char hand_bits[] = {
  0x00, 0x00, 0xb8, 0x01, 0x44, 0x0e, 0x9c, 0x14, 0x22, 0x29, 0x4c, 0x52,
  0x90, 0x40, 0x20, 0x40, 0x4c, 0x40, 0x52, 0x40, 0x52, 0x40, 0x24, 0x40,
  0x08, 0x40, 0x08, 0x20, 0xf0, 0x20, 0x40, 0x10, };
#define handmask_width 16
#define handmask_height 16
static unsigned char handmask_bits[] = {
  0xb8, 0x01, 0xfc, 0x0f, 0xfe, 0x1f, 0xfe, 0x3f, 0xfe, 0x7f, 0xfe, 0xff,
  0xfc, 0xff, 0xfc, 0xff, 0xfe, 0xff, 0xff, 0xff, 0xff, 0xff, 0xfe, 0xff,
  0xfc, 0xff, 0xfc, 0x7f, 0xf8, 0x7f, 0xf0, 0x3f, };
#define hand32_width 32
#define hand32_height 32
static unsigned char hand32_bits[] = {
  0x00, 0x00, 0x00, 0x00, 0x00, 0x70, 0x06, 0x00, 0x00, 0x88, 0x09, 0x00,
  0x00, 0x0e, 0x11, 0x00, 0x00, 0x09, 0x11, 0x00, 0x00, 0x11, 0xe1, 0x01,
  0x00, 0x11, 0x22, 0x02, 0x00, 0x21, 0x42, 0x04, 0x00, 0x21, 0x84, 0x04,
  0x00, 0x22, 0x84, 0x08, 0x00, 0x42, 0x08, 0x09, 0x00, 0x42, 0x08, 0x11,
  0x00, 0x44, 0x10, 0x11, 0x00, 0x84, 0x10, 0x12, 0x38, 0x84, 0x10, 0x22,
  0x44, 0x88, 0x10, 0x22, 0x44, 0x08, 0x21, 0x22, 0x44, 0x08, 0x21, 0x20,
  0x84, 0x08, 0x01, 0x20, 0x84, 0x08, 0x00, 0x20, 0x04, 0x09, 0x00, 0x20,
  0x08, 0x0a, 0x00, 0x20, 0x08, 0x04, 0x00, 0x10, 0x10, 0x00, 0x00, 0x10,
  0x10, 0x00, 0x00, 0x08, 0x20, 0x00, 0x00, 0x08, 0x40, 0x00, 0x00, 0x04,
  0x80, 0x00, 0x00, 0x02, 0x00, 0x01, 0x00, 0x02, 0x00, 0x02, 0x00, 0x02,
  0x00, 0x02, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, };
#define handm32_width 32
#define handm32_height 32
static unsigned char handm32_bits[] = {
  0x00, 0x00, 0x00, 0x00, 0x00, 0x70, 0x06, 0x00, 0x00, 0xf8, 0x0f, 0x00,
  0x00, 0xfe, 0x1f, 0x00, 0x00, 0xff, 0x1f, 0x00, 0x00, 0xff, 0xff, 0x01,
  0x00, 0xff, 0xff, 0x03, 0x00, 0xff, 0xff, 0x07, 0x00, 0xff, 0xff, 0x07,
  0x00, 0xfe, 0xff, 0x0f, 0x00, 0xfe, 0xff, 0x0f, 0x00, 0xfe, 0xff, 0x1f,
  0x00, 0xfc, 0xff, 0x1f, 0x00, 0xfc, 0xff, 0x1f, 0x38, 0xfc, 0xff, 0x3f,
  0x7c, 0xf8, 0xff, 0x3f, 0x7c, 0xf8, 0xff, 0x3f, 0x7c, 0xf8, 0xff, 0x3f,
  0xfc, 0xf8, 0xff, 0x3f, 0xfc, 0xf8, 0xff, 0x3f, 0xfc, 0xf9, 0xff, 0x3f,
  0xf8, 0xfb, 0xff, 0x3f, 0xf8, 0xff, 0xff, 0x1f, 0xf0, 0xff, 0xff, 0x1f,
  0xf0, 0xff, 0xff, 0x0f, 0xe0, 0xff, 0xff, 0x0f, 0xc0, 0xff, 0xff, 0x07,
  0x80, 0xff, 0xff, 0x03, 0x00, 0xff, 0xff, 0x03, 0x00, 0xfe, 0xff, 0x03,
  0x00, 0xfe, 0xff, 0x01, 0x00, 0xfc, 0xff, 0x00, };

/*
#include "hand.xbm"
#include "handmask.xbm"
#include "hand32.xbm"
#include "handm32.xbm"
*/

void INTERNAL_reset_cursor(int id)
{
    xwini[id].xwin_cursortype = DEFAULT_cursortype;
    xwini[id].xx_hidden = TRUE;
    xwini[id].xx_draw_cursor_func = xx_default_cursor_func;
    xwini[id].xlib_pixcopymode = FALSE;
}

void INTERNAL_init_cursor(GC gc_pixmap)
{
    unsigned long valuemask = 0;
    XGCValues values;
    Cursor cursor;
    Pixmap hand, handmask;
    Pixmap updown, updownmask;
    unsigned long fg, bg;
    unsigned int width, height; 
    unsigned int hot_x, hot_y, hot2_x, hot2_y;
    int i;
    Colormap cmap;
    XColor black, white;

    init_store_cursor();

    for (i=0;i<G_MAXWINDOW;i++) 
        INTERNAL_reset_cursor(i);
    gc_pix = gc_pixmap;
    gc_crosshair= XCreateGC(INTERNAL_display,INTERNAL_rootwin,valuemask,&values);
    valuemask = GCLineStyle | GCLineWidth | GCCapStyle  | GCJoinStyle;
    XGetGCValues(INTERNAL_display,gc_pix,valuemask,&values);
    XSetLineAttributes(INTERNAL_display,
                       gc_pix,
                       0,
                       values.line_style,
                       values.cap_style,
                       values.join_style);
    XSetLineAttributes(INTERNAL_display,
                       gc_crosshair,
                       0,
                       values.line_style,
                       values.cap_style,
                       values.join_style);
    XSetFunction(INTERNAL_display, gc_crosshair, GXxor);
    XSetForeground(INTERNAL_display, gc_crosshair, 
              WhitePixel(INTERNAL_display, INTERNAL_screen) ^ 
              BlackPixel(INTERNAL_display, INTERNAL_screen));

    cur_cross = XCreateFontCursor(INTERNAL_display, XC_crosshair);
    cur_wait  = XCreateFontCursor(INTERNAL_display, XC_watch);
    XQueryBestCursor(INTERNAL_display, INTERNAL_rootwin, 32, 32, &width, &height);

    fg = 1;
    bg = 0;
    if (width >= 32 && height >= 32) {
        hand = XCreatePixmapFromBitmapData(INTERNAL_display, INTERNAL_rootwin,
               (char*)hand32_bits, hand32_width, hand32_height, fg, bg, 1); 
        handmask = XCreatePixmapFromBitmapData(INTERNAL_display, INTERNAL_rootwin,
               (char*)handm32_bits, handm32_width, handm32_height, fg, bg, 1);
        hot_x = 8;
        hot_y = 12;
        updown = XCreatePixmapFromBitmapData(INTERNAL_display, INTERNAL_rootwin,
               (char*)updown32_bits, updown32_width, updown32_height, fg, bg, 1); 
        updownmask = XCreatePixmapFromBitmapData(INTERNAL_display, INTERNAL_rootwin,
               (char*)updownmask32_bits, updownmask32_width, updownmask32_height, fg, bg, 1);
        hot2_x = 16;
        hot2_y = 16;
    }
    else { 
        hand = XCreatePixmapFromBitmapData(INTERNAL_display, INTERNAL_rootwin,
               (char*)hand_bits, hand_width, hand_height, fg, bg, 1); 
        handmask = XCreatePixmapFromBitmapData(INTERNAL_display, INTERNAL_rootwin,
               (char*)handmask_bits, handmask_width, handmask_height, fg, bg, 1);
        hot_x = 4;
        hot_y = 6;
        updown = XCreatePixmapFromBitmapData(INTERNAL_display, INTERNAL_rootwin,
               (char*)updown_bits, updown_width, updown_height, fg, bg, 1); 
        updownmask = XCreatePixmapFromBitmapData(INTERNAL_display, INTERNAL_rootwin,
               (char*)updownmask_bits, updownmask_width, updownmask_height, fg, bg, 1);
        hot2_x = 8;
        hot2_y = 8;
    }
    cmap    = DefaultColormap(INTERNAL_display,INTERNAL_screen);
    XParseColor(INTERNAL_display, cmap, "black", &black);
    XAllocColor(INTERNAL_display, cmap, &black);
    XParseColor(INTERNAL_display, cmap, "white", &white);
    XAllocColor(INTERNAL_display, cmap, &white);
 
    cur_hand = XCreatePixmapCursor(INTERNAL_display, hand, handmask, 
           &black, &white, hot_x, hot_y);
    cur_updown = XCreatePixmapCursor(INTERNAL_display, updown, updownmask, 
           &black, &white, hot2_x, hot2_y);

    XFreePixmap(INTERNAL_display, hand);
    XFreePixmap(INTERNAL_display, handmask);
}


