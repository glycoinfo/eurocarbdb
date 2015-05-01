/************************************************************************/
/*                               genwcurs.c                             */
/*                                                                      */
/*  Platform : Microsoft Windows                                        */
/*  Module   : Genplot plot functions                                   */
/*                                                                      */
/*  Albert van Kuik                                                     */
/************************************************************************/


#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
#include <time.h>
#include <stdarg.h>
#include <windows.h>

#ifdef DEBUG
#include "mshell.h"
#endif

#include "genplot.h"
#include "g_inter.h"
#include "genwin.h"

/* --- */
typedef void (*CURSORFUNC) (int, int, int);
static CURSORFUNC mswin_draw_cursor_func[G_MAXWINDOW];
static void mswin_default_cursor_func(int id, int x, int y);
static void mswin_rubberbox_func(int id, int x, int y);
static void mswin_rubberline_func(int id, int x, int y);
static void mswin_dummy_func(int id, int x, int y);
static void mswin_staticbox_func(int id, int x, int y);
static void mswin_hide_crosshair(int id);
static void mswin_show_crosshair(int, int, int);
static void mswin_crosshair(int id, int x, int y);

HHOOK hMouseHook;

/**********************************************************************
* CURSOR FUNCTIONS
*/

static save_old_cursortype = G_CURSOR_CROSSHAIR;
void INTERNAL_cursortype(int win, int type)
{
    if (WI[win].child[C_DRAWAREA].hwnd)
	SetCapture(WI[win].child[C_DRAWAREA].hwnd);
    INTERNAL_kill_crosshair(FALSE);
    WI[win].cursortype = type;
    if (type == G_CURSOR_CROSSHAIR) {
	SetCursor(LoadCursor(NULL, IDC_CROSS));
        save_old_cursortype = type;
    }
    else if (type == G_CURSOR_WAIT) {
	SetCursor(LoadCursor(NULL, IDC_WAIT));
    }
    else if (type == G_CURSOR_HAND) {
        INTERNAL_set_handcursor();
    }
    else if (type == G_CURSOR_UPDOWN) {
        INTERNAL_set_updowncursor();
        save_old_cursortype = type;
    }
    else {
	SetCursor(LoadCursor(NULL, IDC_ARROW));
    }
    ReleaseCapture();
}

int INTERNAL_cursorpos(float *x, float *y)
{
    POINT point;

    GetCursorPos(&point);
    ScreenToClient(WI[GD.win_id].child[C_DRAWAREA].hwnd, &point);
    *x = usercoorx((float) point.x);
    *y = usercoorx((float) point.y);
    return TRUE;
}

void INTERNAL_warpcursor(long x, long y)
{
    POINT point;

    point.x = (int) x;
    point.y = (int) y;
    ClientToScreen(MSWIN, &point);
    INTERNAL_hide_cursor_crosshair(GD.win_id);
    SetCursorPos(point.x, point.y);
    INTERNAL_show_cursor_crosshair(GD.win_id);
}



static POINT mswin_storexy[G_MAX_STORE_CURSOR];

static void init_store_cursor(void)
{
    int i;

    for (i = 0; i <= G_MAX_STORE_CURSOR; i++)
	mswin_storexy[i].x = -1;
}

void g_storecursor(int store_id)
{
    if (store_id < 0 || store_id > G_MAX_STORE_CURSOR)
	return;
    GetCursorPos(&mswin_storexy[store_id]);
}

void g_retrievecursor(int store_id)
{
    if (store_id < 0 || store_id > G_MAX_STORE_CURSOR ||
	mswin_storexy[store_id].x == -1)
	return;
    INTERNAL_hide_cursor_crosshair(GD.win_id);
    SetCursorPos(mswin_storexy[store_id].x, mswin_storexy[store_id].y);
    INTERNAL_show_cursor_crosshair(GD.win_id);
}


/*=======================================================*/
/* Move viewport with HAND cursor
 */

static HBITMAP pixtemp = 0;
static int tempx, tempy;
static int tempx1, tempy1, temp_dx, temp_dy;
static int pixclamp, clampx1, clampy1, clampx2, clampy2;
static void copy_pixtemp(int id, int x, int y);

static void exit_pixtemp(int id, int vp_id, int x, int y)
{
    float dx, dy;

    if (!WI[id].pixcopymode)
	return;
    x -= tempx;
    y -= tempy;
    if (pixclamp) {
	x = max(clampx1, x);
	x = min(clampx2, x);
	y = max(clampy1, y);
	y = min(clampy2, y);
    }

    DeleteObject(pixtemp);

    WI[id].pixcopymode = FALSE;
    if (temp_dx == 0 || temp_dy == 0)
	return;
    dx = (float) x / (float) temp_dx;
    dy = (float) y / (float) temp_dy;
    if (WI[id].locked)
	WI[id].locked = FALSE;
/* dy is upside down */
    INTERNAL_clear_pixmap(id);
    if (INTERNAL_update_scrollbars(id, vp_id, -dx, dy) == G_ERROR)
	if (WI[id].usepix)
	    copy_screen2pix(id);
}


static void init_pixtemp(int id, int vp_id, int x, int y, int x1, int y1, int x2, int y2)
{
    HDC hdc, hdcPix;
    HBITMAP xpixOld;
    POINT pt;
    float xx1, yy1, xx2, yy2;

    tempx1 = x1;
    tempy1 = y1;
    temp_dx = x2 - x1 + 1;
    temp_dy = y2 - y1 + 1;
    tempx = x;
    tempy = y;
/* yy is upside down , so wap x1 and x2 (??) */
    if (INTERNAL_get_clampscrollbars(id, vp_id, &xx1, &yy1, &xx2, &yy2) == G_OK) {
        float tmp = xx1;
        xx1 = xx2;
        xx2 = tmp;
      clampx1 = -xx1 * temp_dx;
	clampy1 = -yy1 * temp_dy;
	clampx2 = xx2 * temp_dx;
	clampy2 = yy2 * temp_dy;
	pixclamp = TRUE;
    }
    else
	pixclamp = FALSE;

    hdc = GetDC(WI[id].child[C_DRAWAREA].hwnd);
    hdcPix = CreateCompatibleDC(hdc);
    pixtemp = CreateCompatibleBitmap(hdc,
				     temp_dx,
				     temp_dy);
    xpixOld = SelectObject(hdcPix, pixtemp);
    BitBlt(hdcPix,
	   0, 0,
	   temp_dx, temp_dy,
	   hdc,
	   tempx1, tempy1,
	   SRCCOPY);
    SelectObject(hdcPix, xpixOld);
    DeleteDC(hdcPix);
    ReleaseDC(WI[id].child[C_DRAWAREA].hwnd, hdc);

    WI[id].pixcopymode = TRUE;
    if (WI[id].usepix)
	WI[id].locked = TRUE;
    else
	WI[id].locked = FALSE;
}

static void FillRectangle(int x1, int y1, int width, int height,
			  HDC hdc, HBRUSH hBrush)
{
    RECT r;

    r.left = x1;
    r.top = y1;
    r.right = r.left + width + 1;
    r.bottom = r.top + height + 1;
    FillRect(hdc, &r, hBrush);
}

static void copy_pixtemp(int id, int x, int y)
{
    COLORREF cf;
    HDC hdc, hdcPix;
    HBITMAP xpixOld;
    HBRUSH hBrush, hOBrush;
    int x0, y0, x1, y1, x_abs, y_abs;

    if (!WI[id].pixcopymode)
	return;
    x -= tempx;
    y -= tempy;

    if (pixclamp) {
	x = max(clampx1, x);
	x = min(clampx2, x);
	y = max(clampy1, y);
	y = min(clampy2, y);
    }

    x0 = max(0, -x);
    x0 = min(x0, temp_dx);
    y0 = max(0, -y);
    y0 = min(y0, temp_dy);
    x1 = max(0, x);
    x1 = min(x1, temp_dx);
    y1 = max(0, y);
    y1 = min(y1, temp_dy);
    x_abs = abs(x);
    y_abs = abs(y);

    hdc = GetDC(WI[id].child[C_DRAWAREA].hwnd);
    hdcPix = CreateCompatibleDC(hdc);
    xpixOld = SelectObject(hdcPix, pixtemp);
    cf = GetNearestColor(hdc, RGB(127,127,127));
    hBrush = CreateSolidBrush(cf);
    hOBrush = SelectObject(hdc, hBrush);
    if (x1)
	FillRectangle(tempx1, tempy1, x1, temp_dy, hdc, hBrush);

    if (y1)
	FillRectangle(tempx1, tempy1, temp_dx, y1, hdc, hBrush);


    if (x0)
	FillRectangle(tempx1 + (temp_dx - x_abs),
		      tempy1, x0, temp_dy, hdc, hBrush);

    if (y0)
	FillRectangle(tempx1, tempy1 + (temp_dy - y_abs),
		      temp_dx, y0, hdc, hBrush);
    SelectObject(hdc, hOBrush);
    DeleteObject(hBrush);
    BitBlt(hdc,
	   tempx1 + x1, tempy1 + y1,
	   temp_dx - x_abs, temp_dy - y_abs,
	   hdcPix,
	   x0, y0,
	   SRCCOPY);
    SelectObject(hdcPix, xpixOld);
    DeleteDC(hdcPix);
    ReleaseDC(WI[id].child[C_DRAWAREA].hwnd, hdc);
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
    vxmax = round((float) ((float) G_WIN_WIDTH(win_id) *
			   (vp->vx2 - vp->vx1)));
    vx1 = round((float) ((float) G_WIN_WIDTH(win_id) * vp->vx1));
    return (int) ((((x - vp->wx1) / (vp->wx2 - vp->wx1)) * vxmax) + vx1);
}

static int device_y(int win_id, int vp_id, float y)
{
    VIEWPORTTYPE *vp;
    long vymax, vy1;

    if (vp_id == G_ERROR)
	return G_ERROR;
    vp = INTERNAL_get_viewport(vp_id);
    vymax = round((float) ((float) G_WIN_HEIGHT(win_id) *
			   (vp->vy2 - vp->vy1)));
    vy1 = round((float) ((float) G_WIN_HEIGHT(win_id) * vp->vy1));
    return (int) G_WIN_HEIGHT(win_id) -
	(int) ((((y - vp->wy1) / (vp->wy2 - vp->wy1)) * vymax) + vy1);
}


static void mswin_default_cursor_func(int id, int x, int y)
{
    if (x <0 || x > (int) G_WIN(id).width ||
        y < 0 || y > (int) G_WIN(id).height)
            return;
    MoveToEx(WI[id].hdc_cross, 0, y, NULL);
    LineTo(WI[id].hdc_cross, (int) G_WIN(id).width, y);
    MoveToEx(WI[id].hdc_cross, x, 0, NULL);
    LineTo(WI[id].hdc_cross, x, (int) G_WIN(id).height);
    MoveToEx(WI[id].hdc_cross, 0, 0, NULL);
}


static void mswin_doublecross_func(int id, int x, int y)
{
    MoveToEx(WI[id].hdc_cross, 0, WI[id].rby, NULL);
    LineTo(WI[id].hdc_cross, (int) G_WIN(id).width, WI[id].rby);
    MoveToEx(WI[id].hdc_cross, WI[id].rbx, 0, NULL);
    LineTo(WI[id].hdc_cross, WI[id].rbx, (int) G_WIN(id).height);
    if (WI[id].rby != y) {
        MoveToEx(WI[id].hdc_cross, 0, y, NULL);
        LineTo(WI[id].hdc_cross, (int) G_WIN(id).width, y);
    }
    if (WI[id].rbx != x) {
        MoveToEx(WI[id].hdc_cross, x, 0, NULL);
        LineTo(WI[id].hdc_cross, x, (int) G_WIN(id).height);
    }
    MoveToEx(WI[id].hdc_cross, 0, 0, NULL);
}


static void mswin_rubberbox_func(int id, int x, int y)
{
    MoveToEx(WI[id].hdc_cross, WI[id].rbx, WI[id].rby, NULL);
    LineTo(WI[id].hdc_cross, WI[id].rbx, y);
    LineTo(WI[id].hdc_cross, x, y);
    LineTo(WI[id].hdc_cross, x, WI[id].rby);
    LineTo(WI[id].hdc_cross, WI[id].rbx, WI[id].rby);
    MoveToEx(WI[id].hdc_cross, 0, 0, NULL);
}

static void mswin_rubberline_func(int id, int x, int y)
{
    MoveToEx(WI[id].hdc_cross, WI[id].rbx, WI[id].rby, NULL);
    LineTo(WI[id].hdc_cross, x, y);
    MoveToEx(WI[id].hdc_cross, 0, 0, NULL);
}


static void mswin_xline_func(int id, int x, int y)
{
    MoveToEx(WI[id].hdc_cross, WI[id].rbx, 0, NULL);
    LineTo(WI[id].hdc_cross, WI[id].rbx, (int) G_WIN(id).height);
    if (WI[id].rbx != x) {
        MoveToEx(WI[id].hdc_cross, x, 0, NULL);
        LineTo(WI[id].hdc_cross, x, (int) G_WIN(id).height);
    }
    MoveToEx(WI[id].hdc_cross, 0, 0, NULL);
}

static void mswin_yline_func(int id, int x, int y)
{
    MoveToEx(WI[id].hdc_cross, 0, WI[id].rby, NULL);
    LineTo(WI[id].hdc_cross, (int) G_WIN(id).width, WI[id].rby);
    if (WI[id].rby != y) {
        MoveToEx(WI[id].hdc_cross, 0, y, NULL);
        LineTo(WI[id].hdc_cross, (int) G_WIN(id).width, y);
    }
    MoveToEx(WI[id].hdc_cross, 0, 0, NULL);
}

static void mswin_dummy_func(int id, int x, int y)
{
    id = id;
    x = x;
    y = y;
}

#define BL  1
#define BR  2
#define TL  3
#define TR  4

static void mswin_staticbox_func(int id, int x, int y)
{
    int dx, dy;

    switch (WI[id].bltr) {
    case BL:
	dx = x + WI[id].rbx;
	dy = y - WI[id].rby;
	break;
    case BR:
	dx = x - WI[id].rbx;
	dy = y - WI[id].rby;
	break;
    case TL:
	dx = x + WI[id].rbx;
	dy = y + WI[id].rby;
	break;
    case TR:
	dx = x - WI[id].rbx;
	dy = y + WI[id].rby;
	break;
    default:
	return;
    }
    MoveToEx(WI[id].hdc_cross, dx, dy, NULL);
    LineTo(WI[id].hdc_cross, dx, y);
    LineTo(WI[id].hdc_cross, x, y);
    LineTo(WI[id].hdc_cross, x, dy);
    LineTo(WI[id].hdc_cross, dx, dy);
    MoveToEx(WI[id].hdc_cross, 0, 0, NULL);
}

void g_set_rubbercursor(int win_id, int vp_id, float x, float y, int type)
{
    int local_id, cursor_hidden = FALSE;
    int dx, dy, x1, y1, x2, y2;

    local_id = GET_LOCAL_ID(win_id);
    if (local_id < 0 || local_id >= G_MAXWINDOW)
	return;
    if (!(WI[local_id].cursortype == G_CURSOR_CROSSHAIR ||
        WI[local_id].cursortype == G_CURSOR_UPDOWN)
	&& ! WI[local_id].pixcopymode)
	return;
    if (!WI[local_id].cursorhidden) {
	mswin_hide_crosshair(local_id);
	cursor_hidden = TRUE;
    }
    dx = device_x(local_id, vp_id, x);
    dy = device_y(local_id, vp_id, y);
    exit_pixtemp(local_id, vp_id, dx, dy);
    switch (type) {
    case G_RUBBER_NONE:
	mswin_draw_cursor_func[local_id] = mswin_default_cursor_func;
        INTERNAL_cursortype(local_id, save_old_cursortype);
        return;
    case G_RUBBER_CROSSHAIR:
	mswin_draw_cursor_func[local_id] = mswin_default_cursor_func;
	INTERNAL_cursortype(local_id, G_CURSOR_CROSSHAIR);
	break;
    case G_RUBBER_DOUBLECROSS:
        WI[local_id].rbx = dx;
        WI[local_id].rby = dy;
        mswin_draw_cursor_func[local_id] = mswin_doublecross_func;
        INTERNAL_cursortype(local_id, G_CURSOR_CROSSHAIR);
        break;
    case G_RUBBER_XHAIR:
        WI[local_id].rbx = dx;
        mswin_draw_cursor_func[local_id] = mswin_xline_func;
        INTERNAL_cursortype(local_id, G_CURSOR_CROSSHAIR);
        break;
    case G_RUBBER_YHAIR:
        WI[local_id].rby = dy;
        mswin_draw_cursor_func[local_id] = mswin_yline_func;
        INTERNAL_cursortype(local_id, G_CURSOR_CROSSHAIR);
        break;
    case G_RUBBER_BOX:
	WI[local_id].rbx = device_x(local_id, vp_id, x);
	WI[local_id].rby = device_y(local_id, vp_id, y);
	mswin_draw_cursor_func[local_id] = mswin_rubberbox_func;
	INTERNAL_cursortype(local_id, G_CURSOR_CROSSHAIR);
	break;
    case G_RUBBER_LINE:
	WI[local_id].rbx = device_x(local_id, vp_id, x);
	WI[local_id].rby = device_y(local_id, vp_id, y);
	mswin_draw_cursor_func[local_id] = mswin_rubberline_func;
	INTERNAL_cursortype(local_id, G_CURSOR_CROSSHAIR);
	break;
    case G_RUBBER_BOX_BL:
	WI[local_id].rbx = device_x(local_id, vp_id, x);
	WI[local_id].rby = device_y(local_id, vp_id, y);
	WI[local_id].bltr = BL;
	mswin_draw_cursor_func[local_id] = mswin_staticbox_func;
	INTERNAL_cursortype(local_id, G_CURSOR_CROSSHAIR);
	break;
    case G_RUBBER_BOX_BR:
	WI[local_id].rbx = device_x(local_id, vp_id, x);
	WI[local_id].rby = device_y(local_id, vp_id, y);
	WI[local_id].bltr = BR;
	mswin_draw_cursor_func[local_id] = mswin_staticbox_func;
	INTERNAL_cursortype(local_id, G_CURSOR_CROSSHAIR);
	break;
    case G_RUBBER_BOX_TL:
	WI[local_id].rbx = device_x(local_id, vp_id, x);
	WI[local_id].rby = device_y(local_id, vp_id, y);
	WI[local_id].bltr = TL;
	mswin_draw_cursor_func[local_id] = mswin_staticbox_func;
	INTERNAL_cursortype(local_id, G_CURSOR_CROSSHAIR);
	break;
    case G_RUBBER_BOX_TR:
	WI[local_id].rbx = device_x(local_id, vp_id, x);
	WI[local_id].rby = device_y(local_id, vp_id, y);
	WI[local_id].bltr = TR;
	mswin_draw_cursor_func[local_id] = mswin_staticbox_func;
	INTERNAL_cursortype(local_id, G_CURSOR_CROSSHAIR);
	break;
    case G_RUBBER_PANNER:
    case G_RUBBER_PANNER_BORDER:
	if (vp_id != G_ERROR) {
	    INTERNAL_calc_viewport_rect(win_id, vp_id, &x1, &y1, &x2, &y2);
	    if (type == G_RUBBER_PANNER_BORDER)
		init_pixtemp(local_id, vp_id, dx, dy, x1 + 1, y1 + 1, x2 - 1, y2 - 1);
	    else
		init_pixtemp(local_id, vp_id, dx, dy, x1, y1, x2, y2);
	    mswin_draw_cursor_func[local_id] = copy_pixtemp;
	}
	else
	    mswin_draw_cursor_func[local_id] = mswin_dummy_func;
	INTERNAL_cursortype(local_id, G_CURSOR_HAND);
	break;
    }
    if (cursor_hidden && WI[local_id].cursortype != G_CURSOR_HAND
            && WI[local_id].cursortype != G_CURSOR_UPDOWN) {
	mswin_show_crosshair(local_id,
			     WI[local_id].cursorx,
			     WI[local_id].cursory);
    }
}

static void mswin_crosshair(int id, int x, int y)
{
    HPEN mswin_cursorpen;
    HPEN oldpen;
    HDC  hdc;
    int oldmode;
    G_PALETTEENTRY g_palette;

    if (WI[id].hdc_cross) {
	g_bell();
	return;
    }
    INTERNAL_get_palette_color(&g_palette, G_WHITE);
    WI[id].hdc_cross = GetDC(WI[id].child[C_DRAWAREA].hwnd);

    mswin_cursorpen = CreatePen(PS_SOLID,
				1,
				RGB(g_palette.r,
				    g_palette.g,
				    g_palette.b));

    oldmode = GetROP2(WI[id].hdc_cross);
    SetROP2(WI[id].hdc_cross, R2_XORPEN);
    oldpen = SelectObject(WI[id].hdc_cross, mswin_cursorpen);

    mswin_draw_cursor_func[id] (id, x, y);

    SelectObject(WI[id].hdc_cross, oldpen);
    DeleteObject(mswin_cursorpen);
    SetROP2(WI[id].hdc_cross, oldmode);

    ReleaseDC(WI[id].child[C_DRAWAREA].hwnd, WI[id].hdc_cross);
    WI[id].hdc_cross = NULL;
}

static void mswin_show_crosshair(int id, int x, int y)
{
    if (!WI[id].cursorhidden || WI[id].hdc_cross)
	return;
    WI[id].cursorhidden = FALSE;
    mswin_crosshair(id, x, y);
    WI[id].cursorx = x;
    WI[id].cursory = y;
}

static void mswin_hide_crosshair(int id)
{
    if (WI[id].cursorhidden || WI[id].hdc_cross)
	return;
    WI[id].cursorhidden = TRUE;
    mswin_crosshair(id, WI[id].cursorx, WI[id].cursory);
}

void INTERNAL_update_crosshair(int id, int x, int y)
{
    if (WI[id].hdc_cross)
	return;
    if (WI[id].cursorhidden) {
	mswin_show_crosshair(id, x, y);
	return;
    }
    mswin_crosshair(id, WI[id].cursorx, WI[id].cursory);
    mswin_crosshair(id, x, y);
    WI[id].cursorx = x;
    WI[id].cursory = y;
}

void INTERNAL_kill_crosshair(int reset)
{
    int i;

    if (hMouseHook) {
	UnhookWindowsHookEx(hMouseHook);
	hMouseHook = (HHOOK) 0;
    }
    for (i = 0; i < G_MAXWINDOW; i++)
	if (!WI[i].cursorhidden) {
	    mswin_hide_crosshair(i);
	}
    if (reset)
	SetCursor(LoadCursor(NULL, IDC_ARROW));
}

/*********************************************************/

void INTERNAL_hide_cursor_crosshair(int id)
{
    if (WI[id].cursortype == G_CURSOR_CROSSHAIR)
	mswin_hide_crosshair(id);
}

void INTERNAL_show_cursor_crosshair(int id)
{
    POINT point;
    int flag = INTERNAL_object_playback_flag;

    INTERNAL_object_playback_flag = FALSE;
    INTERNAL_check_hdc(&(WI[id]), FALSE);
    INTERNAL_object_playback_flag = flag;

    if (WI[id].cursortype == G_CURSOR_CROSSHAIR) {
	GetCursorPos(&point);
	ScreenToClient(WI[id].child[C_DRAWAREA].hwnd, &point);
	if (point.x >= 0 && point.y >= 0 && point.x <= G_WIN_WIDTH(id)
	    && point.y <= G_WIN_HEIGHT(id) && hMouseHook) {
	        mswin_show_crosshair(id, point.x, point.y);
      }
    }
}


#define updown32_width 32
#define updown32_height 32
static unsigned char updown32_bits[] = {
   0xff, 0xf9, 0xff, 0xff,
   0xff, 0xf1, 0xff, 0xff,
   0xff, 0xe1, 0xff, 0xff,
   0xff, 0xc1, 0xff, 0xff,
   0xff, 0x81, 0x87, 0xff,
   0xff, 0x01, 0x87, 0xff,
   0xfe, 0x01, 0x87, 0xff,
   0xfc, 0x01, 0x87, 0xff,
   0xfc, 0x01, 0x87, 0xff,
   0xff, 0xe1, 0x87, 0xff,
   0xff, 0xe1, 0x87, 0xff,
   0xff, 0xe1, 0x87, 0xff,
   0xff, 0xe1, 0x87, 0xff,
   0xff, 0xe1, 0x87, 0xff,
   0xff, 0xe1, 0x87, 0xff,
   0xff, 0xe1, 0x87, 0xff,
   0xff, 0xe1, 0x87, 0xff,
   0xff, 0xe1, 0x87, 0xff,
   0xff, 0xe1, 0x87, 0xff,
   0xff, 0xe1, 0x87, 0xff,
   0xff, 0xe1, 0x87, 0xff,
   0xff, 0xe1, 0x87, 0xff,
   0xff, 0xe1, 0x87, 0xff,
   0xff, 0xe1, 0x80, 0x3f,
   0xff, 0xe1, 0x80, 0x3f,
   0xff, 0xe1, 0x80, 0x7f,
   0xff, 0xe1, 0x80, 0xff,
   0xff, 0xe1, 0x81, 0xff,
   0xff, 0xff, 0x83, 0xff,
   0xff, 0xff, 0x87, 0xff,
   0xff, 0xff, 0x8f, 0xff,
   0xff, 0xff, 0x9f, 0xff};
#define updownmask32_width 32
#define updownmask32_height 32
static unsigned char updownmask32_bits[] = {
   0x00, 0x00, 0x00, 0x00,
   0x00, 0x04, 0x00, 0x00,
   0x00, 0x0c, 0x00, 0x00,
   0x00, 0x1c, 0x00, 0x00,
   0x00, 0x3c, 0x30, 0x00,
   0x00, 0x7c, 0x30, 0x00,
   0x00, 0xfc, 0x30, 0x00,
   0x01, 0xfc, 0x30, 0x00,
   0x00, 0x0c, 0x30, 0x00,
   0x00, 0x0c, 0x30, 0x00,
   0x00, 0x0c, 0x30, 0x00,
   0x00, 0x0c, 0x30, 0x00,
   0x00, 0x0c, 0x30, 0x00,
   0x00, 0x0c, 0x30, 0x00,
   0x00, 0x0c, 0x30, 0x00,
   0x00, 0x0c, 0x30, 0x00,
   0x00, 0x0c, 0x30, 0x00,
   0x00, 0x0c, 0x30, 0x00,
   0x00, 0x0c, 0x30, 0x00,
   0x00, 0x0c, 0x30, 0x00,
   0x00, 0x0c, 0x30, 0x00,
   0x00, 0x0c, 0x30, 0x00,
   0x00, 0x0c, 0x30, 0x00,
   0x00, 0x0c, 0x30, 0x00,
   0x00, 0x0c, 0x3f, 0x80,
   0x00, 0x0c, 0x3f, 0x00,
   0x00, 0x0c, 0x3e, 0x00,
   0x00, 0x0c, 0x3c, 0x00,
   0x00, 0x00, 0x38, 0x00,
   0x00, 0x00, 0x30, 0x00,
   0x00, 0x00, 0x20, 0x00,
   0x00, 0x00, 0x00, 0x00};

#define hand32_width 32
#define hand32_height 32
static unsigned char hand32_bits[] =
{
    0xff, 0xff, 0xff, 0xff,
    0xff, 0xf1, 0x9f, 0xff,
    0xff, 0xe0, 0x0f, 0xff,
    0xff, 0x80, 0x07, 0xff,
    0xff, 0x00, 0x07, 0xff,
    0xff, 0x00, 0x00, 0x7f,
    0xff, 0x00, 0x00, 0x3f,
    0xff, 0x00, 0x00, 0x1f,
    0xff, 0x00, 0x00, 0x1f,
    0xff, 0x80, 0x00, 0x0f,
    0xff, 0x80, 0x00, 0x0f,
    0xff, 0x80, 0x00, 0x07,
    0xff, 0xc0, 0x00, 0x07,
    0xff, 0xc0, 0x00, 0x07,
    0xe3, 0xc0, 0x00, 0x03,
    0xc1, 0xe0, 0x00, 0x03,
    0xc1, 0xe0, 0x00, 0x03,
    0xc1, 0xe0, 0x00, 0x03,
    0xc0, 0xe0, 0x00, 0x03,
    0xc0, 0xe0, 0x00, 0x03,
    0xc0, 0x60, 0x00, 0x03,
    0xe0, 0x20, 0x00, 0x03,
    0xe0, 0x00, 0x00, 0x07,
    0xf0, 0x00, 0x00, 0x07,
    0xf0, 0x00, 0x00, 0x0f,
    0xf8, 0x00, 0x00, 0x0f,
    0xfc, 0x00, 0x00, 0x1f,
    0xfe, 0x00, 0x00, 0x3f,
    0xff, 0x00, 0x00, 0x3f,
    0xff, 0x80, 0x00, 0x3f,
    0xff, 0x80, 0x00, 0x7f,
    0xff, 0xc0, 0x00, 0xff
};
static unsigned char handm32_bits[] =
{
    0x00, 0x00, 0x00, 0x00,
    0x00, 0x00, 0x00, 0x00,
    0x00, 0x0e, 0x60, 0x00,
    0x00, 0x0f, 0x70, 0x00,
    0x00, 0x6f, 0x70, 0x00,
    0x00, 0x77, 0x78, 0x00,
    0x00, 0x77, 0xbb, 0x80,
    0x00, 0x7b, 0xbd, 0xc0,
    0x00, 0x7b, 0xde, 0xc0,
    0x00, 0x3b, 0xde, 0xe0,
    0x00, 0x3d, 0xef, 0x60,
    0x00, 0x3d, 0xef, 0x70,
    0x00, 0x1d, 0xf7, 0x70,
    0x00, 0x1e, 0xf7, 0xb0,
    0x00, 0x1e, 0xf7, 0xb8,
    0x1c, 0x0e, 0xf7, 0xb8,
    0x1c, 0x0f, 0x7b, 0xb8,
    0x1c, 0x0f, 0x7b, 0xf8,
    0x1e, 0x0f, 0x7f, 0xf8,
    0x1e, 0x0f, 0xff, 0xf8,
    0x1f, 0x0f, 0xff, 0xf8,
    0x0f, 0x8f, 0xff, 0xf8,
    0x0f, 0xdf, 0xff, 0xf0,
    0x07, 0xff, 0xff, 0xf0,
    0x07, 0xff, 0xff, 0xe0,
    0x03, 0xff, 0xff, 0xe0,
    0x01, 0xff, 0xff, 0xc0,
    0x00, 0xff, 0xff, 0x80,
    0x00, 0x7f, 0xff, 0x80,
    0x00, 0x3f, 0xff, 0x80,
    0x00, 0x3f, 0xff, 0x00,
    0x00, 0x3f, 0xff, 0x00
};

void INTERNAL_reset_cursor(int id)
{
    mswin_draw_cursor_func[id] = mswin_default_cursor_func;
}

static HCURSOR hcursorHand, hcursorUpdown;

void INTERNAL_destroy_handcursor()
{
    if (hcursorHand)
        DestroyCursor(hcursorHand);
    hcursorHand = 0;
}

void INTERNAL_set_handcursor()
{
    if (hcursorHand)
        SetCursor(hcursorHand);
}

void INTERNAL_destroy_updowncursor()
{
    if (hcursorUpdown)
        DestroyCursor(hcursorUpdown);
    hcursorUpdown = 0;
}

void INTERNAL_set_updowncursor()
{
    if (hcursorUpdown)
        SetCursor(hcursorUpdown);
}

void INTERNAL_init_cursor(void)
{
    unsigned int hot_x, hot_y;
    int i;

    if (hcursorHand)
	return;
    hMouseHook = NULL;
    init_store_cursor();
    
    for (i = 0; i < G_MAXWINDOW; i++) {
	mswin_draw_cursor_func[i] = mswin_default_cursor_func;
	WI[i].pixcopymode = FALSE;
    }
    hot_x = 8;
    hot_y = 12;
    hcursorHand = CreateCursor(_hInst, hot_x, hot_y, hand32_width,
			       hand32_height, hand32_bits, handm32_bits);
    hot_x = 16;
    hot_y = 16;
    hcursorUpdown = CreateCursor(_hInst, hot_x, hot_y, updown32_width,
             updown32_height, updown32_bits, updownmask32_bits);
}

