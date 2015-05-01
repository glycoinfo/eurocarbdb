/************************************************************************/
/*                               gen_xm.c                               */
/*                                                                      */
/*  Platform : Motif                                                    */
/*  Module   : Genplot plot functions                                   */
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

#ifdef   GENPLOT_
#undef   GENPLOT_
#endif
#define  GENPLOT_(x) screen_##x

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

#include "genmotif.h"

#define min(a,b)        (((a) < (b)) ? (a) : (b))
#define max(a,b)        (((a) > (b)) ? (a) : (b))

#define XWIN_X0      50
#define XWIN_Y0      50

static void LoadFont(void);
static XFontStruct *CurrentFont(void);
static XFontStruct *LoadQueryScalableFont(Display *display, int screen,
                                   char *name, int size);
static void init_fonts(void);
static void exit_fonts(void);
static void set_keylock(int win_id, int tf);

static void xx_linewidth(int width);
static void xx_linestyle(unsigned pattern);
static void xx_clipping(int clip);
static void xx_foreground(int color);


static void reset_double_buffering(int id);

XWININFO xwini[G_MAXWINDOW];

Display *INTERNAL_display = NULL;
XtAppContext INTERNAL_app_context;
int INTERNAL_screen = -1;
Window INTERNAL_rootwin;


static GC gc, gc_pix, gc_rot;
static XColor *xlib_color;
static unsigned int xlib_bpixel;
unsigned int xlib_fpixel;
static long   xlib_device_maxx;
static long   xlib_device_maxy;
static long   xlib_border  = 5;
static int    xlib_depth;

static int    xlib_font;
static int    xlib_fontsize;
static int    xlib_pixrotate;

static int    xlib_palettesize;
static int    FocusWindow;



static void reset_widget(int local_win_id)
{
    xwini[local_win_id].toplevel_widget = NULL;
    xwini[local_win_id].form_widget = NULL;
    xwini[local_win_id].draw_widget = NULL;
    xwini[local_win_id].menubar_widget = NULL;
    xwini[local_win_id].menubar2_widget = NULL;
    xwini[local_win_id].buttonbar_widget = NULL;
    xwini[local_win_id].buttonbar2_widget = NULL;
    xwini[local_win_id].scroll_widget = NULL;
    xwini[local_win_id].v_scrollbar_widget = NULL;
    xwini[local_win_id].h_scrollbar_widget = NULL;
    xwini[local_win_id].command_widget = NULL;
    xwini[local_win_id].console_widget = NULL;
    xwini[local_win_id].pane_widget = NULL;

    INTERNAL_reset_popup(local_win_id);
    reset_double_buffering(local_win_id);
}

static char *xlib_palettecolor(int color)
{
    static char xcolor[20];
    G_PALETTEENTRY g_palette;

    INTERNAL_get_palette_color(&g_palette, color);
    sprintf(xcolor, "RGBi:%1.2f/%1.2f/%1.2f",
            (float) g_palette.r / 255.0,
            (float) g_palette.g / 255.0,
            (float) g_palette.b / 255.0);
    return xcolor;
}


void INTERNAL_dispatch_message(void)
{
    static XEvent event;

    while (XtAppPending(INTERNAL_app_context)) {
        XtAppNextEvent(INTERNAL_app_context,&event);
        XtDispatchEvent(&event);
    }
}

void INTERNAL_dispatch(void)
{
    static XEvent event;

    XtAppNextEvent(INTERNAL_app_context,&event);
    XtDispatchEvent(&event);
}


/*********************************************************/



float GENPLOT_(device_width)          = 640;
float GENPLOT_(device_height)         = 480;
int   GENPLOT_(dots_per_cm)           = 1;
int   GENPLOT_(y_is_upsidedown)       = TRUE;
int   GENPLOT_(able2clip)             = FALSE;
int   GENPLOT_(multi_windows)         = TRUE;


/***************************************************************
 * Double buffering
 */

static void clear_pixmap(int id)
{
    XSetForeground(INTERNAL_display, gc_pix, xlib_bpixel);
    XFillRectangle(INTERNAL_display, xwini[id].xdraw, gc_pix, 0, 0, 
                   xwini[id].width + 1, xwini[id].height + 1);
    XSetForeground(INTERNAL_display, gc_pix, xlib_fpixel);
}

static void clear_pixarea(int id, int x, int y, int width, int height)
{
    XSetForeground(INTERNAL_display,gc_pix,xlib_bpixel);
    XFillRectangle(INTERNAL_display,xwini[id].xdraw, gc_pix, x, y, width, height);
    XSetForeground(INTERNAL_display,gc_pix,xlib_fpixel);
}

static void copy_pixmap(int id)
{
    if (gen_device != G_SCREEN || !xwini[id].double_buffering)
        return;
    INTERNAL_hide_cursor_crosshair(id);
    XCopyArea(INTERNAL_display, xwini[id].xpix, xwini[id].xwin, 
              gc_pix, 0, 0, xwini[id].width + 1, xwini[id].height + 1, 0, 0);
    INTERNAL_show_cursor_crosshair(id);
}

static void create_pixmap(int id, int do_copy)
{
    if (xwini[id].double_buffering)
        return;
    xwini[id].xpix = XCreatePixmap(INTERNAL_display, INTERNAL_rootwin, 
                              xwini[id].width+1, 
                              xwini[id].height+1, xlib_depth);
    xwini[id].xdraw = xwini[id].xpix;
    xwini[id].double_buffering = TRUE;
    if (do_copy) {
        INTERNAL_hide_cursor_crosshair(id);
        XCopyArea(INTERNAL_display, xwini[id].xwin, xwini[id].xpix, 
                  gc_pix, 0, 0, 
                  xwini[id].width + 1, 
                  xwini[id].height + 1,
                  0, 0);
        INTERNAL_show_cursor_crosshair(id);
    }
    else
        clear_pixmap(id);
}

static void destroy_pixmap(int id)
{
    if (!xwini[id].double_buffering)
        return; 
    if (xwini[id].xpix != (Pixmap) 0)
        XFreePixmap(INTERNAL_display, xwini[id].xpix);
    reset_double_buffering(id);
}

static void reset_double_buffering(int id)
{
    xwini[id].double_buffering = FALSE;
    xwini[id].xpix =  (Pixmap) 0; 
    xwini[id].xdraw = xwini[id].xwin;
}

void g_set_windowbuffer(int win_id, int doit)
{
    int id, device;

    id = GET_LOCAL_ID(win_id);
    device = GET_DEVICE(win_id);
    if (id < 0 || id >= G_MAXWINDOW)
        return;
    if (device != G_SCREEN)
        return;
    if (doit) 
        create_pixmap(id, TRUE);
    else 
        destroy_pixmap(id);
}

/****************************************************************/

/* Error handler for X protocol errors.  Continue after error */
static int ErrorHandler(Display *display, XErrorEvent *event)
{
    char errortext[100];

    XGetErrorText(display, event -> error_code, errortext, 100);
/*

Burn, kill and delete the warnings @#$%^

    printf("X Protocol error: %s\n", errortext);
    printf("XID %d serial %d major %d minor %d\n",
	   (unsigned int)event -> resourceid,
	   (unsigned int)event -> serial,
	   (unsigned int)event -> request_code,
	   (unsigned int)event -> minor_code);

*/
    return 0;
}


static void XtErrHandler(String message)
{
    fprintf(stderr,"Error: %s\n", message);
    if (strcmp(message,"Event with wrong window") != 0)
        exit(1);
}

static void XtWarnHandler(String message)
{
    message = message;
}


static String fallback_resources[] = {
    "*visibleWhenOff: False",
    "*fillOnSelect: True",
    "*childHorizontalAlignment: XmALIGNMENT_CENTER",
    "*historyVisibleItemCount: 3",
    NULL
};


int GENPLOT_(open)(void)
{
    static int init;
    char *INTERNAL_display_name = NULL;
    int i,temp;
    Colormap cmap;
    unsigned long valuemask = 0;
    XGCValues values;
    XVisualInfo visualinfo;
    Arg arg[10];  
    int n= 0;
    char Name[120];

    set_SCREEN_mode();

    if (init)
        return G_ERROR;
    init = TRUE;

    XtToolkitInitialize();

    INTERNAL_app_context = XtCreateApplicationContext();

    XtAppSetFallbackResources(INTERNAL_app_context, fallback_resources);

    if (INTERNAL_g_application) {
        strcpy(Name,INTERNAL_g_application);
        Name[0] = toupper(Name[0]);
        if (Name[0] == 'X')
            Name[1] = toupper(Name[1]);
    }
    else
        sprintf(Name,"Genplot");
 
    INTERNAL_display = XtOpenDisplay(INTERNAL_app_context,
                            NULL,
                            INTERNAL_g_application,
                            Name,
                            NULL,
                            0,
                            &INTERNAL_g_argc,
                            INTERNAL_g_argv);
    if (INTERNAL_display == NULL) {
        fprintf( stderr, "cannot connect to X server");
        if (getenv("DISPLAY") == NULL)
            fprintf( stderr, ", 'DISPLAY' environment variable not set.\n");
        else
            fprintf( stderr, " %s\n", XDisplayName(INTERNAL_display_name));
        exit ( -1 );
    }
    XSetErrorHandler((XErrorHandler) ErrorHandler);
    XtAppSetErrorHandler(INTERNAL_app_context, (XtErrorHandler) XtErrHandler);
    XtAppSetWarningHandler(INTERNAL_app_context, (XtErrorHandler) XtWarnHandler);
    INTERNAL_screen  = DefaultScreen(INTERNAL_display);
    INTERNAL_rootwin = RootWindow(INTERNAL_display, INTERNAL_screen);
    cmap    = DefaultColormap(INTERNAL_display,INTERNAL_screen);
    visualinfo.visual = DefaultVisual(INTERNAL_display,INTERNAL_screen);
    xlib_depth        = DefaultDepth(INTERNAL_display,INTERNAL_screen);
    GENPLOT_(device_width)  = xlib_device_maxx  = DisplayWidth(INTERNAL_display, INTERNAL_screen);
    GENPLOT_(device_height) = xlib_device_maxy  = DisplayHeight(INTERNAL_display, INTERNAL_screen);

    xlib_palettesize = g_get_palettesize();
    if ((xlib_color = (XColor*)malloc(sizeof(XColor) * xlib_palettesize)) == NULL){
        fprintf(stderr,"Memory allocation error\n");
        exit(1);
    }
    i=5;
    while (!XMatchVisualInfo(INTERNAL_display,INTERNAL_screen,xlib_depth,i--,&visualinfo));
    if (xlib_depth == 1 || i < StaticColor) {
        G_PALETTEENTRY g_palette;
        INTERNAL_get_palette_color(&g_palette, i);
        for (i=0; i < xlib_palettesize; i++) {
            if (g_palette.r == 255 &&
                g_palette.g == 255 &&
                g_palette.b == 255)
                xlib_color[i].pixel = WhitePixel(INTERNAL_display, INTERNAL_screen);
            else
                xlib_color[i].pixel = BlackPixel(INTERNAL_display, INTERNAL_screen);
        }
    }
    else {
        for (i=0; i < xlib_palettesize; i++) {
            XParseColor(INTERNAL_display,
                        cmap,
                        xlib_palettecolor(i),
                        &xlib_color[i]);
            if (!XAllocColor(INTERNAL_display,cmap,&xlib_color[i]))
                xlib_color[i].pixel = BlackPixel(INTERNAL_display, INTERNAL_screen);
        }
    }
    gc=XCreateGC(INTERNAL_display,INTERNAL_rootwin,valuemask,&values);
    gc_pix = XCreateGC(INTERNAL_display,INTERNAL_rootwin,valuemask,&values);
    gc_rot=XCreateGC(INTERNAL_display,INTERNAL_rootwin,valuemask,&values);

    valuemask = GCLineStyle | GCLineWidth | GCCapStyle  | GCJoinStyle;
    XGetGCValues(INTERNAL_display,gc,valuemask,&values);
    XSetLineAttributes(INTERNAL_display,
                       gc,
                       0,
                       values.line_style,
                       values.cap_style,
                       values.join_style);

    XSetForeground(INTERNAL_display, gc, xlib_color[INTERNAL_store_gc.fcolor].pixel);
    XSetBackground(INTERNAL_display, gc, xlib_color[INTERNAL_store_gc.bcolor].pixel);
    xlib_bpixel = xlib_color[INTERNAL_store_gc.bcolor].pixel;
    xlib_fpixel = xlib_color[INTERNAL_store_gc.fcolor].pixel;

    XSetFunction(INTERNAL_display, gc_rot, GXand);
    XSetForeground(INTERNAL_display, gc_rot, 
              WhitePixel(INTERNAL_display, INTERNAL_screen) ^ 
              BlackPixel(INTERNAL_display, INTERNAL_screen));

    init_fonts();
    INTERNAL_init_cursor(gc_pix);
    INTERNAL_init_screen_stuff();
    INTERNAL_dispatch_message();
    xlib_pixrotate = FALSE;
    for (i=0;i<G_MAXWINDOW;i++) 
        reset_widget(i);
    return G_OK;
}


static void PointerMotionProc(Widget w, XtPointer any, XEvent *xevent, Boolean *bool)
{
    G_EVENT ui;
    XMotionEvent *event = (XMotionEvent*) xevent;
    int id, doit1 = FALSE, doit2 = FALSE;

    id = ui.win_id = (int) any;

    if (!xwini[id].xlib_pixcopymode && 
        (event->state & Button1Mask ||
        event->state & Button2Mask ||
        event->state & Button3Mask)){
	    ui.x = event->x;
            ui.y = event->y;
            ui.keycode = 0;
            ui.event = G_POINTERMOTION;
            if (event->state & Button1Mask) {
		ui.event |= G_BUTTON1PRESS;
                INTERNAL_prep_event(&ui);
                if (INTERNAL_process_event(G_POINTERMOTION_EVENT, &ui))
                    doit1 = TRUE;
                if (INTERNAL_process_event(G_BUTTON1PRESS_EVENT, &ui))
                    doit2 = TRUE;
            }
            else if (event->state & Button2Mask) {
                ui.event |= G_BUTTON2PRESS;
                INTERNAL_prep_event(&ui);
                if (INTERNAL_process_event(G_POINTERMOTION_EVENT, &ui))
                    doit1 = TRUE;
                if (INTERNAL_process_event(G_BUTTON2PRESS_EVENT, &ui))
                    doit2 = TRUE;
            }
            else {
                ui.event |= G_BUTTON3PRESS;
                INTERNAL_prep_event(&ui);
                if (INTERNAL_process_event(G_POINTERMOTION_EVENT, &ui))
                    doit1 = TRUE;
                if (INTERNAL_process_event(G_BUTTON3PRESS_EVENT, &ui))
                    doit2 = TRUE;
            }
            if (doit1 && doit2)
                INTERNAL_add_event(&ui);
    }
    if (CURTYPE(id) == G_CURSOR_CROSSHAIR || xwini[id].xlib_pixcopymode) {
        INTERNAL_update_crosshair(id, event->x, event->y);
    }
}

static void LeaveWindowProc(Widget w, XtPointer any, XEvent *xevent, Boolean *bool)
{
    XCrossingEvent *event = (XCrossingEvent*) xevent;
    int curr_id = (int) any;
    if (CURTYPE(curr_id) == G_CURSOR_CROSSHAIR)
        INTERNAL_hide_crosshair(curr_id);
}

static void EnterWindowProc(Widget w, XtPointer any, XEvent *xevent, Boolean *bool)
{
    XCrossingEvent *event = (XCrossingEvent*) xevent;
    int curr_id = (int) any;
    if (CURTYPE(curr_id) == G_CURSOR_CROSSHAIR)
        INTERNAL_show_crosshair(curr_id, event->x, event->y);
}

static void expose_proc(Widget w, XtPointer client_data, XtPointer call_data)
{
    XExposeEvent *ee; 
    int win_id = (int) client_data;
    XmDrawingAreaCallbackStruct *cb = (XmDrawingAreaCallbackStruct*)call_data;

    if (xwini[win_id].draw_widget == NULL)
        return;    

    ee = (XExposeEvent*) cb->event;
    if (!cb->event || ee->count)
        return;             
    if (xwini[win_id].double_buffering)
       copy_pixmap(win_id);
    else 
       INTERNAL_update_window(G_SCREEN, win_id);
}

static void resize_proc(Widget w, XtPointer client_data, XtPointer call_data)
{
    int win_id = (int) client_data;
    XmDrawingAreaCallbackStruct *cb = (XmDrawingAreaCallbackStruct*)call_data;
    Arg args[2];
    Dimension width, height;
    int doexpose = FALSE;
    int doclear = TRUE;
    Widget wid;

    if (xwini[win_id].draw_widget == NULL)
        return;    

    XtSetArg(args[0], XmNwidth, &width);
    XtSetArg(args[1], XmNheight, &height);
    XtGetValues(w, args, 2);
    if (width - 1 <= xwini[win_id].width &&
        height - 1 <= xwini[win_id].height)
            doexpose = TRUE;
    if (width - 1 == xwini[win_id].width &&
        height - 1 == xwini[win_id].height)
            doclear = FALSE;
    if (width - 1 > 0)
        G_WIN_WIDTH(win_id)  = width - 1;
    if (height - 1 > 0)
        G_WIN_HEIGHT(win_id) = height - 1;
    xwini[win_id].width = width - 1;
    xwini[win_id].height = height - 1;
    if (xwini[win_id].double_buffering) {
        destroy_pixmap(win_id);
        create_pixmap(win_id, FALSE);
        doclear = FALSE;
        doexpose = TRUE;
    }
    wid = XtWindowToWidget(INTERNAL_display,xwini[win_id].xwin);
    if (doclear && (wid==w)) {
        INTERNAL_hide_cursor_crosshair(win_id);
        clear_pixmap(win_id);
        INTERNAL_show_cursor_crosshair(win_id);
    }
    if (doexpose) {
        INTERNAL_update_window(G_SCREEN, win_id);
        INTERNAL_update_floatbutton_position(win_id, width, height);
    }
}

static void input_proc(Widget w, XtPointer client_data, XtPointer call_data)
{
    G_EVENT ui;
    XmDrawingAreaCallbackStruct *cb = (XmDrawingAreaCallbackStruct*)call_data;


    ui.win_id  = (int) client_data;
    if (xwini[ui.win_id].draw_widget == NULL)
        return;    

    ui.vp_id   = G_ERROR;
    
    if (cb->event) {
        switch (cb->event->type) {

            case KeyPress: {
	        KeyCode kc;
                KeySym  ks=0;

                ui.x = cb->event->xkey.x;
                ui.y = cb->event->xkey.y;
                kc = cb->event->xkey.keycode;
                if (kc) {
                    int index = 0;
                    if ((cb->event->xkey.state & ShiftMask) ||
			(cb->event->xkey.state & LockMask))
                        index = 1;
                    ks = XKeycodeToKeysym(INTERNAL_display, kc, index);
                }
                ui.keycode = ks /*& 0x00FF*/;
                ui.event = G_KEYPRESS;
                INTERNAL_prep_event(&ui);
                if (INTERNAL_process_event(G_KEYPRESS_EVENT, &ui))
                    INTERNAL_add_event(&ui);
		}
                break;


            case ButtonPress:
                ui.x = cb->event->xbutton.x;
                ui.y = cb->event->xbutton.y;
                ui.keycode = 0;
                if (cb->event->xbutton.button == Button1) {
                    ui.event = G_BUTTON1PRESS;
                    INTERNAL_prep_event(&ui);
                    if (INTERNAL_process_event(G_BUTTON1PRESS_EVENT, &ui))
                        INTERNAL_add_event(&ui);
                }
                else if (cb->event->xbutton.button == Button2) {
                    ui.event = G_BUTTON2PRESS;
                    INTERNAL_prep_event(&ui);
                    if (INTERNAL_process_event(G_BUTTON2PRESS_EVENT, &ui))
                        INTERNAL_add_event(&ui);
                }
                else if (cb->event->xbutton.button == Button3) {
                    ui.event = G_BUTTON3PRESS;
                    INTERNAL_prep_event(&ui);
                    if (INTERNAL_process_event(G_BUTTON3PRESS_EVENT, &ui))
                        INTERNAL_add_event(&ui);
                }
                break;

            case ButtonRelease:
                ui.x = cb->event->xbutton.x;
                ui.y = cb->event->xbutton.y;
                ui.keycode = 0;
                if (cb->event->xbutton.button == Button1) {
                    ui.event = G_BUTTON1RELEASE;
                    INTERNAL_prep_event(&ui);
                    if (INTERNAL_process_event(G_BUTTON1RELEASE_EVENT, &ui))
                        INTERNAL_add_event(&ui);
                }
                else if (cb->event->xbutton.button == Button2) {
                    ui.event = G_BUTTON2RELEASE;
                    INTERNAL_prep_event(&ui);
                    if (INTERNAL_process_event(G_BUTTON2RELEASE_EVENT, &ui))
                        INTERNAL_add_event(&ui);
                }
                else if (cb->event->xbutton.button == Button3) {
                    ui.event = G_BUTTON3RELEASE;
                    INTERNAL_prep_event(&ui);
                    if (INTERNAL_process_event(G_BUTTON3RELEASE_EVENT, &ui))
                        INTERNAL_add_event(&ui);
                }
                break;

        }
    }
}

#include <Xm/Protocols.h>

static void UserKillsWindow(Widget w, XtPointer client_data, XtPointer call_data)
{
    int local_win_id = (int) client_data;
    G_EVENT ui;

    ui.win_id = local_win_id;
    ui.x = 0;
    ui.y = 0;
    ui.keycode = 0;
    ui.event = G_WINDOWQUIT;
    INTERNAL_prep_event(&ui);
    if (INTERNAL_process_event(G_WINDOWQUIT_EVENT, &ui))
        INTERNAL_add_event(&ui);
    INTERNAL_dispatch_message();

    /*
    g_close_window(MAKE_GLOBAL_ID((int) client_data, G_SCREEN));
    */
    
}

static void toplevel_destroy_func(Widget w, XtPointer tag, XtPointer reason)
{
    int local_win_id = (int) tag;
    G_EVENT ui;

    g_scrollbar_disconnect(MAKE_GLOBAL_ID(local_win_id, gen_device));
    INTERNAL_reset_cursor(local_win_id);
    xwini[local_win_id].xwin = (Window) NULL;

    destroy_pixmap(local_win_id);
    
    INTERNAL_reset_popup(local_win_id);
    INTERNAL_exit_console(local_win_id);
    ui.win_id = local_win_id;
    ui.x = 0;
    ui.y = 0;
    ui.keycode = 0;
    ui.event = G_WINDOWDESTROY;
    INTERNAL_prep_event(&ui);
    if (INTERNAL_process_event(G_WINDOWDESTROY_EVENT, &ui))
        INTERNAL_add_event(&ui);
    reset_widget(local_win_id);
    INTERNAL_dispatch_message();
}

static void StructureNotifyProc(Widget w, XtPointer tag, XEvent *xevent, Boolean *bool)
{
    XPropertyEvent *event = (XPropertyEvent*) xevent;

    if (event->type == DestroyNotify) {

        int local_win_id = (int) tag;
        G_EVENT ui;

        g_scrollbar_disconnect(MAKE_GLOBAL_ID(local_win_id, gen_device));
        INTERNAL_reset_cursor(local_win_id);
        xwini[local_win_id].xwin = (Window) NULL;

        destroy_pixmap(local_win_id);
    
        INTERNAL_reset_popup(local_win_id);
        INTERNAL_exit_console(local_win_id);
        ui.win_id = local_win_id;
        ui.x = 0;
        ui.y = 0;
        ui.keycode = 0;
        ui.event = G_WINDOWDESTROY;
        INTERNAL_prep_event(&ui);
        if (INTERNAL_process_event(G_WINDOWDESTROY_EVENT, &ui))
            INTERNAL_add_event(&ui);
        XSendEvent(INTERNAL_display, XtWindow(w), FALSE, 
                  NoEventMask, (XEvent*) xevent);

        reset_widget(local_win_id);
        INTERNAL_dispatch_message();

        g_close_window(MAKE_GLOBAL_ID((int) tag, G_SCREEN));

    }
}



void GENPLOT_(open_window)(int id,long x0, long y0, long width, 
                                  long height, char *title, long flag)
{
    Atom wm_delete_window;
    Arg args[20];
    int n;
    char *p,*classname,Name[120];
    int extra_height = 0, scroll_h=0, scroll_v =0,primary_shell, 
        child_window, borderw;
    G_EVENT ui;

    xwini[id].width = (int) width;
    xwini[id].height = (int) height;
    if (flag & G_WIN_BLACK_ON_WHITE) {
        xlib_fpixel = BlackPixel(INTERNAL_display, INTERNAL_screen);
        xlib_bpixel = WhitePixel(INTERNAL_display, INTERNAL_screen);
        XSetForeground(INTERNAL_display, gc, xlib_fpixel);
        XSetBackground(INTERNAL_display, gc, xlib_bpixel);
    }

    /* --- ALWAYS PRIMARY SHELL --- */
    flag |= G_WIN_PRIMARY_SHELL;
    G_WIN(id).flag |= G_WIN_PRIMARY_SHELL;

    if ((flag & G_WIN_COMMANDLINE_FOCUS) && !(flag & G_WIN_COMMANDLINE))
        flag -= G_WIN_COMMANDLINE_FOCUS;
    if ((flag & G_WIN_COMMANDLINE_FOCUS) && (flag & G_WIN_POINTERFOCUS))
        flag -= G_WIN_POINTERFOCUS;
        
    if (flag & G_WIN_PRIMARY_SHELL)
        primary_shell = TRUE;
    else
        primary_shell = FALSE;

    child_window = -1;
    if (flag & G_WIN_CHILD && !primary_shell) 
        for (n=0;n<G_MAXWINDOW;n++) 
            if ((G_WIN(n).flag & G_WIN_PRIMARY_SHELL) && 
                (G_WIN(n).isopen)) {
                child_window = n;
                break;
    }
    
    n=0;
    if (!primary_shell) {
        XtSetArg(args[n], XmNmappedWhenManaged, FALSE); n++;
        XtSetArg(args[n], XmNheight, height); n++;
        XtSetArg(args[n], XmNwidth,  width); n++;
    }
    else {
        if (flag & G_WIN_POINTERFOCUS) {
            XtSetArg(args[n], XmNkeyboardFocusPolicy, XmPOINTER);
            n++;
        }
        XtSetArg(args[n], XmNargc, INTERNAL_g_argc); n++;
        XtSetArg(args[n], XmNargv, INTERNAL_g_argv); n++;
        XtSetArg(args[n], XmNx, x0);
        n++;
        XtSetArg(args[n], XmNy, y0);
        n++;
    }
    if (INTERNAL_g_application) {
        strcpy(Name,INTERNAL_g_application);
        Name[0] = toupper(Name[0]);
        if (Name[0] == 'X')
            Name[1] = toupper(Name[1]);
    }
    else
        sprintf(Name,"PrimaryShell%d",id);

    xwini[id].toplevel_widget= XtAppCreateShell(title,
                                       Name,
                                       applicationShellWidgetClass,
                                       INTERNAL_display,
                                       args,
                                       n);

    wm_delete_window = XmInternAtom(XtDisplay(xwini[id].toplevel_widget), 
                                    "WM_DELETE_WINDOW", FALSE);

    XmSetWMProtocolHooks(xwini[id].toplevel_widget,
			 wm_delete_window,
			 (XtCallbackProc) UserKillsWindow,
			 (XtPointer) id,
			 (XtCallbackProc) NULL,
			 (XtPointer)0);
                         
    sprintf(Name, "Window%d", id);
    n=0;
    XtSetArg(args[n], XmNx, x0);
    n++;
    XtSetArg(args[n], XmNy, y0);
    n++;
    if (primary_shell) {
        if ((flag & G_WIN_COMMANDLINE) || (flag & G_WIN_CONSOLE)) {
            XtSetArg(args[n], XmNcommandWindowLocation, XmCOMMAND_BELOW_WORKSPACE);
            n++;
        }
        XtSetArg(args[n], XmNshowSeparator, TRUE);
        n++;
        xwini[id].form_widget = XmCreateMainWindow(xwini[id].toplevel_widget, Name, args, n);
    }
    else {
/*
        XtSetArg(args[n], XmNtransient, FALSE);
        n++;
*/ 
        XtSetArg(args[n], XmNdefaultPosition, FALSE);
        n++;
        XtSetArg(args[n], XmNautoUnmanage, FALSE);
        n++;
        XtSetArg(args[n], XmNdialogTitle,
		      XmStringCreateLtoR(title ,XmSTRING_DEFAULT_CHARSET));
        n++;
        XtSetArg(args[n], XmNrubberPositioning, TRUE);
        n++;
        if (flag & G_WIN_NORESIZE) {
            XtSetArg(args[n], XmNnoResize, TRUE);
            n++;
        }
        if (flag & G_WIN_POINTERFOCUS) {
            XtSetArg(args[n], XmNkeyboardFocusPolicy, XmPOINTER);
            n++;
        }
        if (child_window >= 0)
            xwini[id].form_widget = XmCreateFormDialog(xwini[child_window].toplevel_widget, Name, args, n);
        else
            xwini[id].form_widget = XmCreateFormDialog(xwini[id].toplevel_widget, Name, args, n);

    }

    XtAddCallback(xwini[id].form_widget,
                  XmNdestroyCallback,
                  toplevel_destroy_func,
                  (XtPointer) id);
                  
    if (flag & G_WIN_MENUBAR) {
        sprintf(Name, "menubar_%d", id);
        n=0;
        if (flag & G_WIN_COMMANDLINE_FOCUS) {
            XtSetArg(args[n], XmNtraversalOn, FALSE);
            n++;
        }
        if (!primary_shell) {
            XtSetArg(args[n], XmNtopAttachment, XmATTACH_FORM);
            n++;
            XtSetArg(args[n], XmNleftAttachment, XmATTACH_FORM);
            n++;
            XtSetArg(args[n], XmNrightAttachment, XmATTACH_FORM);
            n++;
        }
        XtSetArg(args[n], XmNresizeHeight, FALSE);
        n++;
        XtSetArg(args[n], XmNheight, MENUBARHEIGHT);
        n++;
        xwini[id].menubar_widget = XmCreateSimpleMenuBar(xwini[id].form_widget,
                                             Name, args, n);
        XtManageChild(xwini[id].menubar_widget);
        extra_height += MENUBARHEIGHT;
    }
    else
        xwini[id].menubar_widget = NULL;

    sprintf(Name, "Pane%d", id);
    n=0;
    if (flag & G_WIN_MENUBAR && !primary_shell) {
        XtSetArg(args[n], XmNtopAttachment, XmATTACH_WIDGET);
        n++;
        XtSetArg(args[n], XmNtopWidget, xwini[id].menubar_widget);
        n++;
        XtSetArg(args[n], XmNleftAttachment, XmATTACH_FORM);
        n++;
        XtSetArg(args[n], XmNrightAttachment, XmATTACH_FORM);
        n++;
        XtSetArg(args[n], XmNbottomAttachment, XmATTACH_FORM);
        n++;
    }
    XtSetArg(args[n], XmNsashIndent, -70);
    n++;
    XtSetArg(args[n], XmNmarginWidth, 0);
    n++;
    XtSetArg(args[n], XmNmarginHeight,0);
    n++;
    if (!(flag & G_WIN_COMMANDLINE) && !(flag & G_WIN_BUTTONBAR)) {
        XtSetArg(args[n], XmNseparatorOn, FALSE);
        n++;
        XtSetArg(args[n], XmNsashHeight, 1);
        n++;
    }
    xwini[id].pane_widget = XmCreatePanedWindow(xwini[id].form_widget, Name, args, n);
    XtManageChild(xwini[id].pane_widget);

    if (flag & G_WIN_MENUBAR2) {
        sprintf(Name, "menubar2_%d", id);
        n=0;
        if (flag & G_WIN_COMMANDLINE_FOCUS) {
            XtSetArg(args[n], XmNtraversalOn, FALSE);
            n++;
        }
        XtSetArg(args[n], XmNpositionIndex, 0);
        n++;
        XtSetArg(args[n], XmNresizeHeight, FALSE);
        n++;
        XtSetArg(args[n], XmNheight, MENUBARHEIGHT);
        n++;
        XtSetArg(args[n], XmNpaneMinimum, MENUBARHEIGHT);
        n++;
        XtSetArg(args[n], XmNpaneMaximum, MENUBARHEIGHT);
        n++;
        xwini[id].menubar2_widget = XmCreateSimpleMenuBar(xwini[id].pane_widget,
                                             Name, args, n);
        XtManageChild(xwini[id].menubar2_widget);
        extra_height += MENUBARHEIGHT;
    }
    else
        xwini[id].menubar2_widget = NULL;

    if (flag & G_WIN_BUTTONBAR) {
        int extra = 0;
        if (flag & (G_WIN_BUTTONBAR_32 - G_WIN_BUTTONBAR))
            extra = BUTTONBAR32HEIGHT - MENUBARHEIGHT;
        n=0;
        if (flag & G_WIN_COMMANDLINE_FOCUS) {
            XtSetArg(args[n], XmNtraversalOn, FALSE);
            n++;
        }
        XtSetArg(args[n], XmNpositionIndex, XmLAST_POSITION);
        n++;
        XtSetArg(args[n], XmNorientation, XmHORIZONTAL);
        n++;
        XtSetArg(args[n], XmNresizeHeight, FALSE);
        n++;
        XtSetArg(args[n], XmNheight, MENUBARHEIGHT + extra);
        n++;
        XtSetArg(args[n], XmNmarginHeight, 0);
        n++;
        XtSetArg(args[n], XmNspacing, 0);
        n++;
        XtSetArg(args[n], XmNpaneMinimum, MENUBARHEIGHT + extra);
        n++;
        XtSetArg(args[n], XmNpaneMaximum, MENUBARHEIGHT + extra);
        n++;
        xwini[id].buttonbar_widget = XmCreateRowColumn(xwini[id].pane_widget,
                                             Name, args, n);
        XtManageChild(xwini[id].buttonbar_widget);
        extra_height += MENUBARHEIGHT + extra;
    }
    else
        xwini[id].buttonbar_widget = NULL;

    
    if ((flag & G_WIN_SCROLLBAR) && !(flag & G_WIN_CONSOLE)) {
        n=0;
        if (flag & G_WIN_COMMANDLINE_FOCUS) {
            XtSetArg(args[n], XmNtraversalOn, FALSE);
            n++;
        }
        XtSetArg(args[n], XmNpositionIndex, XmLAST_POSITION);
        n++;
        XtSetArg(args[n], XmNwidth, width + 1);
        n++;
        XtSetArg(args[n], XmNheight, height + 1);
        n++;
        XtSetArg(args[n], XmNallowResize, TRUE);
        n++;
        XtSetArg(args[n], XmNscrollingPolicy,  XmAPPLICATION_DEFINED);
        n++;
        sprintf(Name, "ScrollW%d", id);
        xwini[id].scroll_widget = XmCreateScrolledWindow(xwini[id].pane_widget, Name, args, n);
        XtManageChild(xwini[id].scroll_widget);

        n=0;
        XtSetArg(args[n], XmNmaximum, SMAX);
        n++;
        XtSetArg(args[n], XmNminimum, 0);
        n++;
        XtSetArg(args[n], XmNsliderSize, SMAX);
        n++;
        XtSetArg(args[n], XmNprocessingDirection, XmMAX_ON_BOTTOM);
        n++;
        XtSetArg(args[n], XmNorientation, XmVERTICAL);
        n++;
        sprintf(Name, "VScrollbar%d", id);
        xwini[id].v_scrollbar_widget = XmCreateScrollBar(xwini[id].scroll_widget, Name, args, n);
        XtManageChild(xwini[id].v_scrollbar_widget);

        XtAddCallback(xwini[id].v_scrollbar_widget, XmNdecrementCallback,
                      INTERNAL_scrollbar_proc, (XtPointer) ID_V_DECREMENT);
        XtAddCallback(xwini[id].v_scrollbar_widget, XmNdragCallback,
                      INTERNAL_scrollbar_proc, (XtPointer) ID_V_DRAG);
        XtAddCallback(xwini[id].v_scrollbar_widget, XmNincrementCallback,
                      INTERNAL_scrollbar_proc, (XtPointer) ID_V_INCREMENT);
        XtAddCallback(xwini[id].v_scrollbar_widget, XmNpageIncrementCallback,
                      INTERNAL_scrollbar_proc, (XtPointer) ID_V_PAGEINCREMENT);
        XtAddCallback(xwini[id].v_scrollbar_widget, XmNpageDecrementCallback,
                      INTERNAL_scrollbar_proc, (XtPointer)ID_V_PAGEDECREMENT);

        n=0;
        XtSetArg(args[n], XmNmaximum, SMAX);
        n++;
        XtSetArg(args[n], XmNminimum, 0);
        n++;
        XtSetArg(args[n], XmNsliderSize, SMAX);
        n++;
        XtSetArg(args[n], XmNprocessingDirection, XmMAX_ON_RIGHT);
        n++;
        XtSetArg(args[n], XmNorientation, XmHORIZONTAL);
        n++;
        sprintf(Name, "HScrollbar%d", id);
        xwini[id].h_scrollbar_widget = XmCreateScrollBar(xwini[id].scroll_widget, Name, args, n);
        XtManageChild(xwini[id].h_scrollbar_widget);
        XtAddCallback(xwini[id].h_scrollbar_widget, XmNdecrementCallback,
                      INTERNAL_scrollbar_proc, (XtPointer) ID_H_DECREMENT);
        XtAddCallback(xwini[id].h_scrollbar_widget, XmNdragCallback,
                      INTERNAL_scrollbar_proc, (XtPointer) ID_H_DRAG);
        XtAddCallback(xwini[id].h_scrollbar_widget, XmNincrementCallback,
                      INTERNAL_scrollbar_proc, (XtPointer) ID_H_INCREMENT);
        XtAddCallback(xwini[id].h_scrollbar_widget, XmNpageIncrementCallback,
                      INTERNAL_scrollbar_proc, (XtPointer) ID_H_PAGEINCREMENT);
        XtAddCallback(xwini[id].h_scrollbar_widget, XmNpageDecrementCallback,
                      INTERNAL_scrollbar_proc, (XtPointer) ID_H_PAGEDECREMENT);

        n=0;
        XtSetArg(args[n], XmNheight, &scroll_h);
        n++;
        XtGetValues(xwini[id].h_scrollbar_widget, args, n);

        n=0;
        XtSetArg(args[n], XmNwidth, &scroll_v);
        n++;
        XtGetValues(xwini[id].v_scrollbar_widget, args, n);

        n=0;
        XtSetArg(args[n], XmNheight, height + scroll_h);
        n++;
        XtSetArg(args[n], XmNwidth, width + scroll_v);
        n++;
        XtSetValues(xwini[id].scroll_widget, args, n);
    }
    else {
        xwini[id].scroll_widget = NULL;
        xwini[id].h_scrollbar_widget = NULL;
        xwini[id].v_scrollbar_widget = NULL;
    }

    if (!(flag & G_WIN_CONSOLE)) {
        sprintf(Name, "DrawingArea%d", id);
        n=0;
        if (flag & G_WIN_COMMANDLINE_FOCUS) {
            XtSetArg(args[n], XmNtraversalOn, FALSE);
            n++;
        }
        XtSetArg(args[n], XmNwidth, width + 1);
        n++;
        XtSetArg(args[n], XmNheight, height + 1);
        n++;
        XtSetArg(args[n], XmNbackground, xlib_bpixel);
        n++;
        XtSetArg(args[n], XmNforeground, xlib_fpixel);
        n++;

        if (flag & G_WIN_SCROLLBAR) {
            xwini[id].draw_widget = XmCreateDrawingArea(xwini[id].scroll_widget, Name, args, n);
        }
        else {
            XtSetArg(args[n], XmNpositionIndex, XmLAST_POSITION);
            n++;
            xwini[id].draw_widget = XmCreateDrawingArea(xwini[id].pane_widget, Name, args, n);
        }
        XtManageChild(xwini[id].draw_widget);

        XtAddCallback(xwini[id].draw_widget, XmNexposeCallback, expose_proc, 
                                                      (XtPointer) id);
        XtAddCallback(xwini[id].draw_widget, XmNinputCallback, input_proc, 
                                                      (XtPointer) id);
        XtAddCallback(xwini[id].draw_widget, XmNresizeCallback, resize_proc, 
                                                      (XtPointer) id);

        XtAddEventHandler(xwini[id].draw_widget,
                      PointerMotionMask,
                      FALSE,
                      PointerMotionProc,
                      (XtPointer)id);
        XtAddEventHandler(xwini[id].draw_widget,
                      EnterWindowMask,
                      FALSE,
                      EnterWindowProc,
                      (XtPointer) id);
        XtAddEventHandler(xwini[id].draw_widget,
                      LeaveWindowMask,
                      FALSE,
                      LeaveWindowProc,
                      (XtPointer)id);
    }
    else 
        xwini[id].draw_widget = NULL;

    if (flag & G_WIN_CONSOLE) {
        INTERNAL_init_console(id);
        n=0;
        XtSetArg(args[n], XmNpositionIndex,XmLAST_POSITION);
        n++;
        XtSetArg(args[n], XmNeditMode, XmMULTI_LINE_EDIT);
        n++;
        XtSetArg(args[n], XmNscrollingPolicy,  XmAUTOMATIC);
        n++;
        XtSetArg(args[n], XmNwidth, width);
        n++;
        XtSetArg(args[n], XmNheight, height);
        n++;
        XtSetArg(args[n], XmNwordWrap, TRUE);
        n++;
        XtSetArg(args[n], XmNeditable, FALSE);
        n++;
        XtSetArg(args[n], XmNverifyBell, FALSE); 
        n++;                              
        XtSetArg(args[n], XmNcursorPositionVisible, FALSE); 
        n++;                              
        sprintf(Name, "ScrollW%d", id);
        xwini[id].scroll_widget = XmCreateScrolledText(xwini[id].pane_widget, Name, args, n);
        XtManageChild(xwini[id].scroll_widget);
        xwini[id].console_widget = xwini[id].scroll_widget;
    }    

    if (flag & G_WIN_BUTTONBAR2) {
        n=0;
        if (flag & G_WIN_COMMANDLINE_FOCUS) {
            XtSetArg(args[n], XmNtraversalOn, FALSE);
            n++;
        }
        XtSetArg(args[n], XmNpositionIndex, XmLAST_POSITION);
        n++;
        XtSetArg(args[n], XmNorientation, XmHORIZONTAL);
        n++;
        XtSetArg(args[n], XmNresizeHeight, FALSE);
        n++;
        XtSetArg(args[n], XmNheight, MENUBARHEIGHT);
        n++;
        XtSetArg(args[n], XmNmarginHeight, 0);
        n++;
        XtSetArg(args[n], XmNspacing, 0);
        n++;
        XtSetArg(args[n], XmNpaneMinimum, MENUBARHEIGHT);
        n++;
        XtSetArg(args[n], XmNpaneMaximum, MENUBARHEIGHT);
        n++;
        xwini[id].buttonbar2_widget = XmCreateRowColumn(xwini[id].pane_widget,
                                             Name, args, n);
        XtManageChild(xwini[id].buttonbar2_widget);
        extra_height += MENUBARHEIGHT;
    }
    else
        xwini[id].buttonbar2_widget = NULL;

    if (flag & G_WIN_COMMANDLINE) {
        int temph;
        sprintf(Name, "Command%d", id);
        n=0;
        XtSetArg(args[n], XmNpositionIndex, XmLAST_POSITION);
        n++;
        XtSetArg(args[n], XmNskipAdjust, TRUE);
        n++;
        XtSetArg(args[n], XmNmarginHeight,2);
        n++;
        XtSetArg(args[n], XmNmarginWidth, 2);
        n++;
/*
        XtSetArg(args[n], XmNpromptString,
        		      XmStringCreateLtoR("" ,XmSTRING_DEFAULT_CHARSET));
        n++;
        XtSetArg(args[n], XmNhistoryVisibleItemCount, 3);
        n++;
*/
        xwini[id].command_widget = XmCreateCommand(xwini[id].pane_widget, Name, args, n);

        XtAddCallback(xwini[id].command_widget, XmNcommandEnteredCallback,
                      INTERNAL_command_proc, (XtPointer)id);
        XtAddCallback(XmCommandGetChild(xwini[id].command_widget,
                      XmDIALOG_COMMAND_TEXT),
                      XmNmodifyVerifyCallback, INTERNAL_trap_keypress_proc, 
                      (XtPointer)id);
        if (!(flag & G_WIN_COMMANDLINE_HIDEHISTORY)) {
            n=0;
            XtSetArg(args[n], XmNpaneMinimum, 110);
            n++;
            XtSetValues(xwini[id].command_widget, args, n);
        }
#if (XmVERSION < 2) 
        else {
            n=0;
            XtSetArg(args[n], XmNhistoryVisibleItemCount, 1);
            n++;
            XtSetValues(xwini[id].command_widget, args, n);
        }
#endif
        n=0;
        XtSetArg(args[n], XmNverifyBell, FALSE); 
        n++;                              
        XtSetValues(XmCommandGetChild(xwini[id].command_widget,
                    XmDIALOG_COMMAND_TEXT), args, n);
        if (flag & (G_WIN_COMMANDLINE_RETURN - G_WIN_COMMANDLINE))
            XtAddEventHandler(
                  XmCommandGetChild(xwini[id].command_widget,
                      XmDIALOG_COMMAND_TEXT),
                  KeyPressMask,
                  FALSE,
                  (XtEventHandler)INTERNAL_command_add_return_proc,
                  (XtPointer)id);
        INTERNAL_init_keyqueue(id);

        n=0;
        XtSetArg(args[n], XmNheight, &temph);
        n++;
        XtGetValues(xwini[id].command_widget, args, n);
        extra_height += temph;
        XtManageChild(xwini[id].command_widget);
#if (XmVERSION >= 2) 
        if (flag & G_WIN_COMMANDLINE_HIDEHISTORY) 
            XtUnmanageChild(XmCommandGetChild(xwini[id].command_widget,
                            XmDIALOG_HISTORY_LIST));
#endif
    }
    else
        xwini[id].command_widget = NULL;

    n=0;
    XtSetArg(args[n], XmNinitialFocus, xwini[id].command_widget);
    n++;
    XtSetValues(xwini[id].pane_widget,args, n);

    XtManageChild(xwini[id].form_widget);
    XtRealizeWidget(xwini[id].toplevel_widget);

    XtAddEventHandler(xwini[id].form_widget,
                      StructureNotifyMask,
                      FALSE,
                      StructureNotifyProc,
                      (XtPointer)id);
/*
    if (primary_shell) {
        XStoreName(INTERNAL_display, XtWindow(xwini[id].toplevel_widget), title);
        XSetIconName(INTERNAL_display, XtWindow(xwini[id].toplevel_widget), title);
    }
*/
    /*
     * Set program icon
     */
    {
        char **xpm;
        Pixmap icon;

        xpm = INTERNAL_get_icon_pixmap(0);
        if (xpm) {
            icon = INTERNAL_make_pixmap(id, xpm, 0);
            if (icon != XmUNSPECIFIED_PIXMAP)
                XtVaSetValues(xwini[id].toplevel_widget, XmNiconPixmap, icon, NULL);
        }
    }

    if (primary_shell && (flag & G_WIN_NORESIZE)){
        XSizeHints hints;

        height += extra_height + scroll_h;
        width += scroll_v;
        hints.flags = PMinSize | PMaxSize;
        hints.x = x0;
        hints.y = y0;
        hints.width = width;
        hints.height = height;
        hints.min_width = width;
        hints.min_height = height;
        hints.max_width = width;
        hints.max_height = height;
        hints.base_width = width;
        hints.base_height = height;

        XmbSetWMProperties(INTERNAL_display, 
                   XtWindow(xwini[id].toplevel_widget),
                   NULL,
                   NULL,
                   NULL,
                   0,
                   &hints,
                   NULL,
                   NULL);
    }
                   
    if (!(flag & G_WIN_CONSOLE)) {
        xwini[id].xwin = XtWindow(xwini[id].draw_widget);
        if (flag & G_WIN_BUFFER) 
            create_pixmap(id, FALSE);
        else 
            reset_double_buffering(id);
    }
    else {
        xwini[id].xwin = XtWindow(xwini[id].console_widget);
        reset_double_buffering(id);
    }
    ui.win_id = id;
    ui.x = 0;
    ui.y = 0;
    ui.keycode = 0;
    ui.event = G_WINDOWCREATE;
    INTERNAL_prep_event(&ui);
    if (INTERNAL_process_event(G_WINDOWCREATE_EVENT, &ui))
        INTERNAL_add_event(&ui);

    INTERNAL_dispatch_message();
}


void GENPLOT_(close_window)(int id)
{
    if (xwini[id].form_widget) {
        if (G_WIN(id).flag & G_WIN_PRIMARY_SHELL)
            XtDestroyWidget(xwini[id].toplevel_widget);
        else
            XtDestroyWidget(xwini[id].form_widget);
    }
    XFlush(INTERNAL_display); 
    INTERNAL_dispatch_message();
}

void GENPLOT_(select_window)(int id)
{
    if (G_WIN(id).flag & G_WIN_BLACK_ON_WHITE) {
        xlib_fpixel = BlackPixel(INTERNAL_display, INTERNAL_screen);
        xlib_bpixel = WhitePixel(INTERNAL_display, INTERNAL_screen);
    }
    else {
        xlib_fpixel = xlib_color[INTERNAL_store_gc.fcolor].pixel;
        xlib_bpixel = xlib_color[INTERNAL_store_gc.bcolor].pixel;
    }
    XSetForeground(INTERNAL_display, gc, xlib_fpixel);
    XSetBackground(INTERNAL_display, gc, xlib_bpixel);
}

void GENPLOT_(raise_window)(int id)
{
    Window w;

    if (xwini[id].form_widget == NULL)
        return;
    w = XtWindow(XtParent(xwini[id].form_widget));
    XRaiseWindow(INTERNAL_display,w);
}

void GENPLOT_(close)()
{
    set_DUMMY_mode();
    return;
/*
    exit_fonts();
    INTERNAL_exit_screen_stuff();
    XtCloseDisplay(INTERNAL_display);
    XtDestroyApplicationContext(INTERNAL_app_context);
*/
}


void GENPLOT_(moveto)(long x,long y)
{
    x=x; y=y;
}

void GENPLOT_(line)(long x1,long y1,long x2,long y2,int isclipped)
{
    isclipped = isclipped;
    XDrawLine(INTERNAL_display,
              xwini[GD.win_id].xdraw,
              gc,
              (int) x1,
              (int) y1,
              (int) x2,
              (int) y2);
}

void GENPLOT_(linewidth)(int width)
{
    unsigned long valuemask = GCLineStyle | GCLineWidth |
                              GCCapStyle  | GCJoinStyle;
    XGCValues values;

    if (XGetGCValues(INTERNAL_display,gc,valuemask,&values)==0)
        return;

    if (width == 1)
        width = 0;   /* --- fast draft mode --- */

    XSetLineAttributes(INTERNAL_display,
                       gc,
                       (unsigned int) width,
                       values.line_style,
                       values.cap_style,
                       values.join_style);
}

void GENPLOT_(linestyle)(unsigned pattern)
{
    unsigned long valuemask = GCLineStyle | GCLineWidth |
                              GCCapStyle  | GCJoinStyle;
    XGCValues value;
    int dash_offset = 0;
    int dash_list_lenght[] = {2, 2, 2};
    static unsigned char short_dashed[]  = { 4, 4};
    static unsigned char long_dashed[]   = { 8, 8};
    static unsigned char dotted_dashed[] = { 2, 4};
    static unsigned char *dash_list[] = {
        short_dashed,
        long_dashed,
        dotted_dashed,
    };

    if (XGetGCValues(INTERNAL_display,gc,valuemask,&value)==0)
        return;
    switch (pattern) {
        case G_SOLID        :
            value.line_style = LineSolid;
            break;
        case G_SHORT_DASHED :
            XSetDashes(INTERNAL_display, gc, dash_offset, (const char *)dash_list[0], dash_list_lenght[0]); 
            value.line_style = LineOnOffDash;
            break;
        case G_LONG_DASHED  :
            XSetDashes(INTERNAL_display, gc, dash_offset, (const char *)dash_list[1], dash_list_lenght[1]); 
            value.line_style = LineOnOffDash;
            break;
        case G_DOTTED       :
            XSetDashes(INTERNAL_display, gc, dash_offset, (const char *)dash_list[2], dash_list_lenght[2]); 
            value.line_style = LineOnOffDash;
            break;
    }
    XSetLineAttributes(INTERNAL_display,
                       gc,
                       value.line_width,
                       value.line_style,
                       value.cap_style,
                       value.join_style);
}

/****************************************************
* UNDOCUMENTED STUFF
*/

void g_set_motif_realtextrotation(int doit)
{
    xlib_pixrotate = doit;
}

void g_raise_xterm(void)
{
    int id=0;
    char *wid;
    
    if ((wid = getenv("WINDOWID")) != NULL) 
        sscanf(wid, "%d", &id);
    else
        id = FocusWindow;
    if (!id)
        return;
    XRaiseWindow(INTERNAL_display, id);
}

void g_get_xterm(void)
{
    int focusstate;
    Window focuswindow;
    Display *display;
    char Name[30];

    if (INTERNAL_display)
        display = INTERNAL_display;
    else
        display = XOpenDisplay(NULL);
    if (display == NULL)
        return;
    XGetInputFocus(display, &focuswindow, &focusstate);
    if (focuswindow != None && focuswindow != PointerRoot) 
        FocusWindow = (int) focuswindow;
    if (!INTERNAL_display)
        XCloseDisplay(display);
}

void g_lower_xterm(void)
{
    int id;
    char *wid;
    
    if ((wid = getenv("WINDOWID")) != NULL) 
        sscanf(wid, "%d", &id);
    else
        id = FocusWindow;
    if (!id)
        return;
    XLowerWindow(INTERNAL_display, id);
}

int g_get_xdepth(void)
{
    int screen, depth;
    Display *display;

    if (INTERNAL_display)
        display = INTERNAL_display;
    else
        display = XOpenDisplay(NULL);
    if (display == NULL)
        return 1;
    screen  = DefaultScreen(display);
    depth = DefaultDepth(display, screen);
    if (!INTERNAL_display)
        XCloseDisplay(display);
    return depth; 
}

/* only  if not G_WIN_POINTERFOCUS */
#define MAXCOUNT	10
void g_set_keyboard_focus(int win_id, int focus, int locked)
{
    int id, device, count;
    Arg args[10];

    id = GET_LOCAL_ID(win_id);
    device = GET_DEVICE(win_id);
    if (id < 0 || id >= G_MAXWINDOW)
        return;
    if (device != G_SCREEN)
        return;
    if (xwini[id].toplevel_widget == NULL)
        return;
    count=0;

    switch (focus) {
        case G_FOCUS_DRAWINGAREA:
            if (xwini[id].draw_widget == NULL)
                return;
            XtSetArg(args[0], XmNtraversalOn, !locked);
            if (xwini[id].command_widget)
                XtSetValues(xwini[id].command_widget, args, 1);
            XtSetArg(args[0], XmNtraversalOn, TRUE);
            if (xwini[id].draw_widget)
                XtSetValues(xwini[id].draw_widget, args, 1);
            while (XmGetFocusWidget(xwini[id].toplevel_widget) != xwini[id].draw_widget) {
                if (count++ > MAXCOUNT)
                    return;
                XmProcessTraversal(xwini[id].toplevel_widget,XmTRAVERSE_NEXT_TAB_GROUP);
            }
            break;
            
        case G_FOCUS_COMMANDLINE:
            if (xwini[id].command_widget == NULL)
                return;
            XtSetArg(args[0], XmNtraversalOn, !locked);
            if (xwini[id].draw_widget)
                XtSetValues(xwini[id].draw_widget, args, 1);
            if (xwini[id].console_widget)
                XtSetValues(xwini[id].console_widget, args, 1);
            XtSetArg(args[0], XmNtraversalOn, TRUE);
            if (xwini[id].command_widget)
                XtSetValues(xwini[id].command_widget, args, 1);
            while (XmGetFocusWidget(xwini[id].toplevel_widget) != 
                   XmCommandGetChild(xwini[id].command_widget,XmDIALOG_COMMAND_TEXT))  {
                if (count++ > MAXCOUNT)
                    return;
                XmProcessTraversal(xwini[id].toplevel_widget,XmTRAVERSE_NEXT_TAB_GROUP);
            }
            break;

        case G_FOCUS_CONSOLEAREA:
            if (xwini[id].console_widget == NULL)
                return;
            XtSetArg(args[0], XmNtraversalOn, !locked);
            if (xwini[id].command_widget)
                XtSetValues(xwini[id].command_widget, args, 1);
            XtSetArg(args[0], XmNtraversalOn, TRUE);
            if (xwini[id].console_widget)
                XtSetValues(xwini[id].console_widget, args, 1);
            while (XmGetFocusWidget(xwini[id].toplevel_widget) != xwini[id].console_widget) {
                if (count++ > MAXCOUNT)
                    return;
                XmProcessTraversal(xwini[id].toplevel_widget,XmTRAVERSE_NEXT_TAB_GROUP);
            }
            break;
            
    }
}

/********************************/


void GENPLOT_(label)(char *label)
{
    LoadFont();
    if (INTERNAL_store_vp.cx > SHRT_MAX ||
        INTERNAL_store_vp.cx < 0 ||
        INTERNAL_store_vp.cy > SHRT_MAX ||
        INTERNAL_store_vp.cy < 0)
            return;
    if (TEXTROTATE && !xlib_pixrotate) {
        int i, j, h, height, length, xx;

        length = strlen(label);
        height = xlib_fontsize * 8 / 10;
        xx = height / 2;
        for (i=0, h=0; i<length; i++) {
            j = length - i - 1;
            XDrawString(INTERNAL_display,
                    xwini[GD.win_id].xdraw,
                    gc,
                    (int) INTERNAL_store_vp.cx - xx,
                    (int) INTERNAL_store_vp.cy - h,
                    label + j,
                    1);
            h += height;
        }
    }
    else if (TEXTROTATE) {
        int x, y, direction, font_ascent, font_decent, width, height;
        int x0, y0, x1, y1, ww, wh;
        XCharStruct overall;
        unsigned long pixel;
        Pixmap pix,pix_rot;
        XImage *image, *image_rot;


        XTextExtents(CurrentFont(), label, strlen(label),
                               &direction, &font_ascent, &font_decent,
                               &overall);
        width    = overall.width; 
        height   = font_ascent + font_decent;

        x0 = INTERNAL_store_vp.cx - height;
        y0 = INTERNAL_store_vp.cy - width;
        x1 = INTERNAL_store_vp.cx;
        y1 = INTERNAL_store_vp.cy;
        ww = xwini[GD.win_id].width;
        wh = xwini[GD.win_id].height;

        if (x1 < 0 || y1 < 0 || x0 > ww || y0 > wh)
            return;

        pix      = XCreatePixmap(INTERNAL_display, INTERNAL_rootwin, width, height, xlib_depth);
        pix_rot  = XCreatePixmap(INTERNAL_display, INTERNAL_rootwin, height, width, xlib_depth);

        XSetForeground(INTERNAL_display, gc_pix, xlib_fpixel);
        XSetBackground(INTERNAL_display, gc_pix, xlib_bpixel);
        XSetFont(INTERNAL_display, gc_pix, CurrentFont()->fid);
        XDrawImageString(INTERNAL_display,
                    pix,
                    gc_pix,
                    0,
                    font_ascent,
                    label,
                    strlen(label));
        XCopyArea(INTERNAL_display, xwini[GD.win_id].xdraw, pix_rot, gc_pix, x0, y0, height, width, 0,0);

        image     = XGetImage(INTERNAL_display, pix, 0, 0, width, height, AllPlanes, XYPixmap);
        image_rot = XGetImage(INTERNAL_display, pix_rot, 0, 0, height, width, AllPlanes, XYPixmap);

        for (x=0;x<width;x++)
            for (y=0;y<height;y++)  {
                pixel = XGetPixel(image, x, y);
                if (pixel == xlib_fpixel)
                    XPutPixel(image_rot, y, width  - 1 - x, pixel);
        }
        XPutImage(INTERNAL_display, pix_rot, gc_pix, image_rot, 0, 0, 0, 0, height, width);
        XCopyArea(INTERNAL_display, pix_rot, xwini[GD.win_id].xdraw, gc, 0, 0,  height, width, x0, y0); 
        XFreePixmap(INTERNAL_display, pix);
        XFreePixmap(INTERNAL_display, pix_rot);
        XDestroyImage(image);
        XDestroyImage(image_rot);
    }
    else
        XDrawString(INTERNAL_display,
                    xwini[GD.win_id].xdraw,
                    gc,
                    (int) INTERNAL_store_vp.cx,
                    (int) INTERNAL_store_vp.cy,
                    label,
                    strlen(label));
}


void GENPLOT_(rectangle)(long x1, long y1, long x2, long y2)
{
    int x, y;
    unsigned int width, height;

    x = (int) min (x1,x2);
    y = (int) min (y1,y2);
    width  = (unsigned int)(max(x1,x2) - min(x1,x2));
    height = (unsigned int)(max(y1,y2) - min(y1,y2));
    XDrawRectangle(INTERNAL_display,
                   xwini[GD.win_id].xdraw,
                   gc,
                   x,
                   y,
                   width,
                   height);
}

void GENPLOT_(drawpoly)(long numpoint, polytype *points)
{
    XDrawLines(INTERNAL_display,
               xwini[GD.win_id].xdraw,
               gc,
               (XPoint*) points,
               (int) numpoint,
               CoordModeOrigin);
}

void GENPLOT_(circle)(long r)
{
    XDrawArc(INTERNAL_display,
             xwini[GD.win_id].xdraw,
             gc,
             (int) INTERNAL_store_vp.cx - (int) r,
             (int) INTERNAL_store_vp.cy - (int) r,
             (unsigned int) r * 2,
             (unsigned int) r * 2,
             0,
             360 * 64);
}

void GENPLOT_(fillrectangle)(long x1, long y1, long x2, long y2)
{
    int x, y;
    unsigned int width, height;

    x = (int) min (x1,x2);
    y = (int) min (y1,y2);
    width  = 1 + (unsigned int)(max(x1,x2) - min(x1,x2));
    height = 1 + (unsigned int)(max(y1,y2) - min(y1,y2));
    XFillRectangle(INTERNAL_display,
                   xwini[GD.win_id].xdraw,
                   gc,
                   x,
                   y,
                   width,
                   height);
}

void GENPLOT_(fillpoly)(long numpoint, polytype *points)
{
    XFillPolygon(INTERNAL_display,
                 xwini[GD.win_id].xdraw,
                 gc,
                 (XPoint*) points,
                 (int) numpoint,
                 Complex,
                 CoordModeOrigin);
}

void GENPLOT_(fillcircle)(long r)
{
    XFillArc(INTERNAL_display,
             xwini[GD.win_id].xdraw,
             gc,
             (int) INTERNAL_store_vp.cx - (int) r,
             (int) INTERNAL_store_vp.cy - (int) r,
             (unsigned int) r * 2,
             (unsigned int) r * 2,
             0,
             360 * 64);
}


void GENPLOT_(foreground)(int color)
{
    if (GWIN.flag & G_WIN_BLACK_ON_WHITE) {
        if (color == INTERNAL_store_gc.bcolor)
            xlib_fpixel = WhitePixel(INTERNAL_display, INTERNAL_screen);
        else
            xlib_fpixel = BlackPixel(INTERNAL_display, INTERNAL_screen);
    }
    else
        xlib_fpixel = xlib_color[color].pixel;
    XSetForeground(INTERNAL_display, gc, xlib_fpixel);
}

void GENPLOT_(background)(int color)
{
    if (GWIN.flag & G_WIN_BLACK_ON_WHITE)
        return;
    xlib_bpixel = xlib_color[color].pixel;
    XSetBackground(INTERNAL_display, gc, xlib_color[color].pixel);
}

int GENPLOT_(palettesize)(int size)
{ 
    int i;
    
    if ((xlib_color = (XColor*)realloc(xlib_color, 
                               sizeof(XColor)*size)) == NULL){
        fprintf(stderr,"Memory allocation error\n");
        exit(1);
    }
    for (i=xlib_palettesize;i<size;i++)
        xlib_color[i].pixel = BlackPixel(INTERNAL_display, INTERNAL_screen);
    xlib_palettesize = size;
    return G_OK; 
}

int GENPLOT_(paletteentry)(int entry_id, G_PALETTEENTRY entry)
{ 
    Colormap cmap;
    unsigned long pixarray[1];
    
    entry = entry; 
    cmap    = DefaultColormap(INTERNAL_display,INTERNAL_screen);
    if (xlib_color[entry_id].pixel != BlackPixel(INTERNAL_display, INTERNAL_screen)) {
        pixarray[0] = xlib_color[entry_id].pixel;
    
        XFreeColors(INTERNAL_display, 
                    cmap, 
                    pixarray, 
                    1, 
                    0);
    }
    XParseColor(INTERNAL_display,
                cmap,
                xlib_palettecolor(entry_id),
                &xlib_color[entry_id]);
    if (!XAllocColor(INTERNAL_display,cmap,&xlib_color[entry_id]))
        xlib_color[entry_id].pixel = BlackPixel(INTERNAL_display, INTERNAL_screen);
    return G_OK; 
}

void GENPLOT_(clipping) (int clip)
{
    int x1, y1, x2, y2;
    XRectangle rectangle[1];

    if (clip) {
        INTERNAL_get_clip(&x1, &y1, &x2, &y2);
        rectangle[0].x = (short) 0;
        rectangle[0].y = (short) 0;
        rectangle[0].width  =  (unsigned short) (x2 - x1 + 1);
        rectangle[0].height =  (unsigned short) (y2 - y1 + 1);
    }

    else {
        x1 = 0;
        y1 = 0;
        rectangle[0].x = (short) 0;
        rectangle[0].y = (short) 0;
        rectangle[0].width  =  (unsigned short) xlib_device_maxx;
        rectangle[0].height =  (unsigned short) xlib_device_maxy;
    }

    XSetClipRectangles(INTERNAL_display,
                       gc,
                       (int) x1,
                       (int) y1,
                       rectangle,
                       1,
                       YXSorted);
}

void GENPLOT_(newpage)()
{
    if (xwini[GD.win_id].double_buffering) 
        clear_pixmap(GD.win_id);
    else {
        INTERNAL_hide_cursor_crosshair(GD.win_id); 
        clear_pixmap(GD.win_id);
        INTERNAL_show_cursor_crosshair(GD.win_id);
        XFlush(INTERNAL_display);
    }
}

void GENPLOT_(clearviewport)(void)
{
    int x1,x2,y1,y2;

    INTERNAL_get_clip(&x1, &y1, &x2, &y2);
    if (xwini[GD.win_id].double_buffering) 
        clear_pixarea(GD.win_id, (int)x1,(int)y1,(int)(x2-x1)+1,(int)(y2-y1)+1);
    else {
        INTERNAL_hide_cursor_crosshair(GD.win_id);
        clear_pixarea(GD.win_id, (int)x1,(int)y1,(int)(x2-x1)+1,(int)(y2-y1)+1);
        INTERNAL_show_cursor_crosshair(GD.win_id);
    }
}

/*****************/

void g_flush(void)
{
    if (xwini[GD.win_id].double_buffering)
        copy_pixmap(GD.win_id);
    else {
        INTERNAL_dispatch_message();    
        XFlush(INTERNAL_display);
    }
}

void g_bell(void)
{
    if (INTERNAL_screen == -1)
        putchar(7);
    else
        XBell(INTERNAL_display, 0);
}

/****************************************************************************
* FONTS
*/

static  char *xlib_fonts[] = {
    "-adobe-times-medium-r-normal--0-0-100-100-p-0-iso8859-1",
    "-adobe-times-medium-i-normal--0-0-100-100-p-0-iso8859-1",
    "-adobe-times-bold-r-normal--0-0-100-100-p-0-iso8859-1",
    "-adobe-times-bold-i-normal--0-0-100-100-p-0-iso8859-1",
    "-adobe-helvetica-medium-r-normal--0-0-100-100-p-0-iso8859-1",
    "-adobe-helvetica-medium-o-normal--0-0-100-100-p-0-iso8859-1",
    "-adobe-helvetica-bold-r-normal--0-0-100-100-p-0-iso8859-1",
    "-adobe-helvetica-bold-o-normal--0-0-100-100-p-0-iso8859-1",
    "-adobe-courier-medium-r-normal--0-0-100-100-m-0-iso8859-1",
    "-adobe-courier-medium-o-normal--0-0-100-100-m-0-iso8859-1",
    "-adobe-courier-bold-r-normal--0-0-100-100-m-0-iso8859-1",
    "-adobe-courier-bold-o-normal--0-0-100-100-m-0-iso8859-1",
    "-adobe-symbol-medium-r-normal--0-0-100-100-p-0-adobe-fontspecific"
};

#define MAX_STORE_FONT  10

typedef struct tagFONTINFO {
    XFontStruct *font;
    int font_id;
    int font_size;
} FONTINFO;

static FONTINFO font_list[MAX_STORE_FONT];
static int font_list_pointer;
static int curr_font;

static XFontStruct *LoadQueryScalableFont(Display *INTERNAL_display, int INTERNAL_screen,
                                   char *name, int size)
{
    int i,j,field;
    char newname[100];
    int res_x, res_y;

    if ((name == NULL) || (name[0] != '-'))
        return NULL;

    for (i=j=field=0; name[i] != '\0' && field <= 14; i++) {
        newname[j++] = name[i];
        if (name[i] == '-') {
            field++;
            switch (field) {
            case 8:
                newname[j] = '*';
                j++;
                if (name[i+1] != '\0')
                    i++;
                break;
            case 7:
                sprintf(&newname[j], "%d", size);
                while (newname[j] != '\0')
                    j++;
                if (name[i+1] != '\0')
                    i++;
                break;
            }
        }
    }
    newname[j] = '\0';
    if (field != 14)
        return NULL;
    return XLoadQueryFont(INTERNAL_display, newname);
}

static void init_fonts(void)
{
    int i;

    xlib_font = -1;
    xlib_fontsize = -1;

    font_list_pointer = 0;
    for (i=0;i<MAX_STORE_FONT;i++) {
        font_list[i].font = NULL;
        font_list[i].font_id = -2;
        font_list[i].font_size = -2;
    }
}

static void exit_fonts(void)
{
    int i;

    for (i=0;i<MAX_STORE_FONT;i++)
        if (font_list[i].font != NULL)
            XFreeFont(INTERNAL_display, font_list[i].font);
}

static int is_font_in_list(int font_id, int size)
{
    int i;

    for (i=0;i<MAX_STORE_FONT;i++)
        if (font_list[i].font_id == font_id &&
            font_list[i].font_size == size) {
                XSetFont(INTERNAL_display, gc, font_list[i].font->fid);
                curr_font = i;
                return TRUE;
            }
    return FALSE;
}

#define LPF  font_list[font_list_pointer].font
static void add_font_to_list(int font_id, int size)
{
    char *name;

    if (LPF != NULL)
        XFreeFont(INTERNAL_display, LPF);

    name = xlib_fonts[font_id];
    if ((LPF = LoadQueryScalableFont(
                           INTERNAL_display,
                           INTERNAL_screen,
                           name,
                           size))== NULL){
/*
        fprintf(stderr, "Display %s doesn't know font %s\n",
                   XDisplayName(NULL), name);
*/
        if((LPF = XLoadQueryFont(INTERNAL_display,"fixed")) == NULL){
            fprintf(stderr, "Display %s doesn't know font %s\nAborting ...\n",
                    XDisplayName(NULL), "fixed");
            exit(1);
        }
    }
    XSetFont(INTERNAL_display, gc, LPF->fid);
    font_list[font_list_pointer].font_id = font_id;
    font_list[font_list_pointer].font_size = size;
    curr_font = font_list_pointer;    
    (font_list_pointer == MAX_STORE_FONT - 1) ?
        font_list_pointer = 0 :
        font_list_pointer++;
}

static void LoadFont(void)
{
    char *name;
    int  size;

    if (INTERNAL_store_gc.fontscaling == G_RELATIVE_FONTSCALING)
        size  = (int) (INTERNAL_store_gc.charsize * WINHEIGHT / G_POINTSIZE_SCALING);
    else
        size  = (int) (INTERNAL_store_gc.charsize * G_POINTSIZE);
    if (xlib_font == INTERNAL_store_gc.font && xlib_fontsize == size)
        return;
    xlib_font     = INTERNAL_store_gc.font;
    xlib_fontsize = size;
    if (is_font_in_list(INTERNAL_store_gc.font, size))
        return;
    add_font_to_list(INTERNAL_store_gc.font, size);

}

static XFontStruct *CurrentFont(void)
{
    return (font_list[curr_font].font);
}

float GENPLOT_(fontheight) (int id)
{
    float size;

    if (INTERNAL_store_gc.fontscaling == G_RELATIVE_FONTSCALING)
        size  = INTERNAL_store_gc.charsize / G_POINTSIZE_SCALING;
    else
        size  = (INTERNAL_store_gc.charsize * G_POINTSIZE) / 
                g_dev[G_SCREEN].win[id].height;
    return size;
}

