/************************************************************************/
/*                               gen_win.c                              */
/*                                                                      */
/*  Platform : Microsoft Windows                                        */
/*  Module   : Genplot plot functions                                   */
/*                                                                      */
/*  Albert van Kuik                                                     */
/************************************************************************/
#include <windows.h>
#include <commctrl.h>
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


#include "genwin.h"
#include "header_icon.h"


#define IS_NOPARENT 1
#define IS_POPUP    2
#define IS_MENUBAR  3
#define IS_DESTROY  4


static char *mswin_fonts[] =
{
    "Times New Roman",
    "Times New Roman Italic",
    "Times New Roman Bold",
    "Times New Roman Bold Italic",
    "Arial",
    "Arial Italic",
    "Arial Bold",
    "Arial Bold Italic",
    "Courier New",
    "Courier New Italic",
    "Courier New Bold",
    "Courier New Bold Italic",
    "Symbol",
    ""
};


/*---------*/
void genmain(int, char **);
LRESULT CALLBACK WndProc(HWND, UINT, WPARAM, LPARAM);
void DrawAll(HDC hdc);
char PlotName[] = "PlotWin";


MSWININFO WI[G_MAXWINDOW];

HINSTANCE _hInst;
static HINSTANCE _hPreInst;
static LPSTR _lpszCmdLine;

static int mswin_linestyle;
static int mswin_linewidth;
int mswin_color;
static int mswin_bkcolor;

COLORREF RGBCOLOR(int c)  
{
    G_PALETTEENTRY g_palette;

    INTERNAL_get_palette_color(&g_palette, c);
    return GetNearestColor(MSHDC,RGB(g_palette.r, 
                    g_palette.g, g_palette.b));
}

void INTERNAL_dispatch_message(void)
{
    MSG msg;

    while (PeekMessage(&msg, NULL, 0, 0, PM_REMOVE)) {
	if (msg.message == WM_QUIT)
	    return;

	TranslateMessage(&msg);
	DispatchMessage(&msg);
    }
}

void INTERNAL_dispatch(void)
{
    MSG msg;

//    if (PeekMessage(&msg, NULL, 0, 0, PM_REMOVE)) {
    if (GetMessage(&msg, NULL, 0, 0)) {
	if (msg.message == WM_QUIT)
	    return;

	TranslateMessage(&msg);
	DispatchMessage(&msg);
    }
}


static void select_clip(MSWININFO *wi)
{
    if (!wi->hdc)
	return;
    if (wi->isclipped) {
	HRGN hRgn_clip = CreateRectRgn(wi->clipx1,
						wi->clipy1,
						wi->clipx2,
						wi->clipy2);
	SelectClipRgn(wi->hdc, hRgn_clip);
       DeleteObject(hRgn_clip);
    }
    else
	SelectClipRgn(wi->hdc, (HRGN) 0);
}

static void select_pen(MSWININFO *wi)
{
    int width;
    
    if (!wi->hdc)
	return;
    if (wi->pen) {
	SelectObject(wi->hdc, wi->oldpen);
	DeleteObject(wi->pen);
    }
    if (mswin_linestyle != PS_SOLID)
	width = 1;
    else
	width = mswin_linewidth;
    wi->pen = CreatePen(mswin_linestyle,
		      width,
		      RGBCOLOR(mswin_color));
    wi->oldpen = SelectObject(wi->hdc, wi->pen);
}

void INTERNAL_check_hdc(MSWININFO *wi, int active)
{
    if (wi->hdcPaint)
        return;
    if (active && !wi->hdc) {
	wi->hdc = GetDC(wi->child[C_DRAWAREA].hwnd);
	if (wi->usepix) {
	    wi->hdcCur = wi->hdc;
	    wi->hdcPix = CreateCompatibleDC(wi->hdc);
	    wi->pixOld = SelectObject(wi->hdcPix, wi->pix);
	    wi->hdc = wi->hdcPix;
	}
	wi->hdc_flag = TRUE;
	select_pen(wi);
	select_clip(wi);
    }
    else if (!active && wi->hdc && wi->hdc_flag
	     && !INTERNAL_object_playback_flag) {
	wi->hdc_flag = FALSE;
	if (wi->pen) {
	    SelectObject(wi->hdc, wi->oldpen);
	    DeleteObject(wi->pen);
	    wi->pen = NULL;
	}
	if (wi->usepix) {
	    SelectObject(wi->hdcPix, wi->pixOld);
	    DeleteDC(wi->hdcPix);
	    wi->hdc = wi->hdcCur;
	}
	ReleaseDC(wi->child[C_DRAWAREA].hwnd, wi->hdc);
	wi->hdc = NULL;
    }
}

static void ResetWI(int id)
{
    int j;

    for (j = 0; j < MAXWCHILDREN; j++) {
	WI[id].child[j].hwnd = 0;
	WI[id].child[j].height = 0;
	WI[id].child[j].rows = 0;
	WI[id].child[j].init = 0;
    }
    WI[id].usepix = FALSE;
    WI[id].locked = FALSE;
    WI[id].hparent = 0;
    WI[id].hmenu = NULL;
    WI[id].bbar32 = FALSE;
    WI[id].hdc = NULL;
    WI[id].hdc_cross = NULL;
    WI[id].hdcPix = NULL;
    WI[id].hdcCur = NULL;
    WI[id].hdcPaint = NULL;
    WI[id].hdc_flag = FALSE;
    WI[id].noresize = FALSE;
    WI[id].scroll = FALSE;
    WI[id].cursortype = G_CURSOR_DEFAULT;
    WI[id].pen = 0;
    WI[id].isclipped = FALSE;
    WI[id].doing_toolbar_stuff = FALSE;
    INTERNAL_reset_cursor(id);
}

/**************************************************************/
float GENPLOT_(device_width)    = 639;
float GENPLOT_(device_height)   = 479;
int   GENPLOT_(dots_per_cm)     = 1;
int   GENPLOT_(y_is_upsidedown) = TRUE;
int   GENPLOT_(able2clip)       = FALSE; // ok, windows can do clippping, but 
                          // large 16 bit values fold back, so better not rely on it
int   GENPLOT_(multi_windows)   = TRUE;

#define MAXARGC     100
#define MAXCMDLINE  100
#define MAXARGLEN   100

int WINAPI WinMain(HINSTANCE hInst, HINSTANCE hPreInst,
		   LPSTR lpszCmdLine, int nCmdShow)
{
    static int argc;
    static char *args[MAXARGC];
    static char cmdline[MAXCMDLINE];
    static char cmdargs[MAXARGLEN];
    char *p;

    _hInst = hInst;
    _hPreInst = hPreInst;
    _lpszCmdLine = lpszCmdLine;
    GetModuleFileName(hInst, cmdline, MAXCMDLINE - 1);
    argc = 0;
    args[argc] = cmdline;
    argc++;
    strcpy(cmdargs, lpszCmdLine);
    p = strtok(cmdargs, " ");
    while (p && (argc < MAXARGC)) {
	args[argc] = p;
	argc++;
	p = strtok(NULL, " ");
    }
    genmain(argc, args);
    return 0;
}

/* --- Window Buffers --- */
static void pixclear(int id, int x1, int y1, int x2, int y2)
{
    HBITMAP xpixOld;
    HDC hdc, hdcPix;
    HBRUSH hBrush, hOBrush;
    RECT r;
    COLORREF cf;
    G_PALETTEENTRY g_palette;

INTERNAL_check_hdc(&(WI[GD.win_id]),FALSE);
    hdc = GetDC(WI[id].child[C_DRAWAREA].hwnd);
    if (WI[id].usepix) {
	hdcPix = CreateCompatibleDC(hdc);
	xpixOld = SelectObject(hdcPix, WI[id].pix);
    }
    else
	hdcPix = hdc;
    
    INTERNAL_get_palette_color(&g_palette, mswin_bkcolor);
    cf = GetNearestColor(hdcPix, RGB(g_palette.r,
				     g_palette.g, g_palette.b));
    hBrush = CreateSolidBrush(cf);
    hOBrush = SelectObject(hdcPix, hBrush);
    r.left = x1;
    r.top = y1;
    r.right = x2;
    r.bottom = y2;
    FillRect(hdcPix, &r, hBrush);
    SelectObject(hdcPix, hOBrush);
    DeleteObject(hBrush);
    if (WI[id].usepix) {
	SelectObject(hdcPix, xpixOld);
	DeleteDC(hdcPix);
    }
    ReleaseDC(WI[id].child[C_DRAWAREA].hwnd, hdc);
}

void INTERNAL_clear_pixmap(int id)
{
    pixclear(id, 0, 0, G_WIN_WIDTH(id) + 1, G_WIN_HEIGHT(id) + 1);
}

static void clear_pixarea(int id, int x1, int y1, int x2, int y2)
{
    pixclear(id, x1, y1, x2, y2);
}

static void copy_pixmap(int id)
{
    HBITMAP xpixOld;
    HDC hdc, hdcPix;

    if (gen_device != G_SCREEN || !WI[id].usepix || WI[id].locked)
	return;
    INTERNAL_hide_cursor_crosshair(id);
    INTERNAL_check_hdc(&(WI[GD.win_id]),FALSE);
    hdc = GetDC(WI[id].child[C_DRAWAREA].hwnd);
    hdcPix = CreateCompatibleDC(hdc);
    xpixOld = SelectObject(hdcPix, WI[id].pix);
    BitBlt(hdc,
	   0, 0,
	   G_WIN_WIDTH(id) + 1, G_WIN_HEIGHT(id) + 1,
	   hdcPix,
	   0, 0,
	   SRCCOPY);
    SelectObject(hdcPix, xpixOld);
    DeleteDC(hdcPix);
    ReleaseDC(WI[id].child[C_DRAWAREA].hwnd, hdc);
    INTERNAL_show_cursor_crosshair(id);
}

void copy_screen2pix(int id)
{
    HBITMAP xpixOld;
    HDC hdc, hdcPix;

    if (!WI[id].usepix || WI[id].locked)
	return;
INTERNAL_check_hdc(&(WI[GD.win_id]),FALSE);
    hdc = GetDC(WI[id].child[C_DRAWAREA].hwnd);
    hdcPix = CreateCompatibleDC(hdc);
    xpixOld = SelectObject(hdcPix, WI[id].pix);
    BitBlt(hdcPix,
	   0, 0,
	   G_WIN_WIDTH(id) + 1, G_WIN_HEIGHT(id) + 1,
	   hdc,
	   0, 0,
	   SRCCOPY);
    SelectObject(hdcPix, xpixOld);
    DeleteDC(hdcPix);
    ReleaseDC(WI[id].child[C_DRAWAREA].hwnd, hdc);
}


static void pixinit(int id)
{
    HBITMAP xpixOld;
    HDC hdc, hdcPix;

    hdc = GetDC(WI[id].child[C_DRAWAREA].hwnd);
    hdcPix = CreateCompatibleDC(hdc);
    WI[id].pix = CreateCompatibleBitmap(hdc,
					G_WIN_WIDTH(id) + 1,
					G_WIN_HEIGHT(id) + 1);
    xpixOld = SelectObject(hdcPix, WI[id].pix);
    BitBlt(hdcPix,
	   0, 0,
	   G_WIN_WIDTH(id) + 1, G_WIN_HEIGHT(id) + 1,
	   hdc,
	   0, 0,
	   SRCCOPY);
    SelectObject(hdcPix, xpixOld);
    DeleteDC(hdcPix);
    ReleaseDC(WI[id].child[C_DRAWAREA].hwnd, hdc);
}

void g_set_windowbuffer(int win_id, int doit)
{
    int id, device;
    HBITMAP xpixOld;
    HDC hdc, hdcPix;

    id = GET_LOCAL_ID(win_id);
    device = GET_DEVICE(win_id);
    if (id < 0 || id >= G_MAXWINDOW)
	return;
    if (device != G_SCREEN)
	return;
    if (doit) {
	if (WI[id].usepix)
	    return;
INTERNAL_check_hdc(&(WI[GD.win_id]),FALSE);

	INTERNAL_hide_cursor_crosshair(id);
	pixinit(id);
	INTERNAL_show_cursor_crosshair(id);
	WI[id].usepix = TRUE;
    }
    else {
	if (!WI[id].usepix)
	    return;
	DeleteObject(WI[id].pix);
	WI[id].usepix = FALSE;
    }
}

/* ---------------------------------------------------- */
#define DRAWINGAREACLASS        "DrawingArea"
LRESULT CALLBACK DrawingAreaProc(HWND, UINT, WPARAM, LPARAM);

HWND CreateDrawingArea(HWND parent, int x, int y, int width, int height,
		       int id, HINSTANCE hInstance, int flag)
{
    int i;
    HWND hwnd;
    WNDCLASSEX wndclass;
    static BOOL ClassRegistered = FALSE;
    DWORD style;

    if (!ClassRegistered) {
      memset (&wndclass, 0, sizeof(WNDCLASSEX));
	wndclass.style = CS_HREDRAW | CS_VREDRAW;
	wndclass.lpfnWndProc = DrawingAreaProc;
	wndclass.cbClsExtra = 0;
	wndclass.cbWndExtra = 0;
	wndclass.hInstance = hInstance;
	wndclass.hIcon = LoadIcon (NULL, IDI_APPLICATION);
	wndclass.hIconSm = LoadIcon (NULL, IDI_APPLICATION);
	wndclass.hCursor = LoadCursor(NULL, IDC_ARROW);
	wndclass.hbrBackground = (HBRUSH) GetStockObject(WHITE_BRUSH);
	wndclass.lpszMenuName = NULL;
	wndclass.lpszClassName = DRAWINGAREACLASS;
      wndclass.cbSize = sizeof(WNDCLASSEX);
	RegisterClassEx(&wndclass);
	ClassRegistered = TRUE;
    }

    style = WS_CHILD | WS_VISIBLE |WS_SYSMENU;
    if (flag & G_WIN_SCROLLBAR) {
	style |= SBS_VERT;
	style |= SBS_HORZ;
	WI[id].scroll = TRUE;
    }
    hwnd = CreateWindow(DRAWINGAREACLASS, "", style,
	       x, y, width, height, parent, (HMENU) id, hInstance, NULL);

    return (hwnd);
}

typedef struct tagMOUSEHOOKSTRUCT { // ms  
    POINT pt; 
    HWND  hwnd; 
    UINT  wHitTestCode; 
    DWORD dwExtraInfo; 
} MOUSEHOOKSTRUCT; 


LRESULT CALLBACK MouseProc(int code, WPARAM wParam, LPARAM lParam)
{
    MOUSEHOOKSTRUCT *mhs = (MOUSEHOOKSTRUCT *) lParam;
    int curr_win, ok=FALSE;

    if (code < 0)
	CallNextHookEx(hMouseHook, code, wParam, lParam);

    if (mhs->wHitTestCode != HTCLIENT && mhs->hwnd){
      for (curr_win = 0, ok = FALSE; curr_win < G_MAXWINDOW; curr_win++){
	  if (mhs->hwnd == WI[curr_win].child[C_DRAWAREA].hwnd) {
	    ok = TRUE;
	    break;
	  }
      }
    }
    if (!ok)
	 INTERNAL_kill_crosshair(FALSE);
    return CallNextHookEx(hMouseHook, code, wParam, lParam);
 //   return FALSE;
}

/*
typedef struct tagMSG {     // msg  
    HWND   hwnd;	 
    UINT   message; 
    WPARAM wParam; 
    LPARAM lParam; 
    DWORD  time; 
    POINT  pt; 
} MSG; 
*/


LRESULT CALLBACK DrawingAreaProc(HWND hWnd, UINT messg, WPARAM wParam, LPARAM lParam)
{
    PAINTSTRUCT ps;
    static G_EVENT ui;
    int i, curr_win;
    static int xx[G_MAXWINDOW], yy[G_MAXWINDOW];
    HWND hwnd_parent;
    RECT rc;

    if ((hwnd_parent = GetParent(hWnd)) == NULL)
	return 0;
    curr_win = GetWindowLong(hwnd_parent, 0);
    ui.win_id = curr_win;
    ui.vp_id = G_ERROR;
    if (curr_win == G_ERROR)
	return (DefWindowProc(hWnd, messg, wParam, lParam));

    switch (messg) {

    case WM_CREATE:
	if (WI[curr_win].scroll) {
            SCROLLINFO sinf;

            sinf.cbSize    = sizeof(SCROLLINFO);
            sinf.fMask     = SIF_PAGE | SIF_POS | SIF_RANGE ;
            sinf.nMin      = 0;
            sinf.nMax      = SMAX;
            sinf.nPage     = SMAX;
            sinf.nPos      = 0;
            sinf.nTrackPos = 0;
            SetScrollInfo(hWnd, SB_HORZ, &sinf, TRUE);
            SetScrollInfo(hWnd, SB_VERT, &sinf, TRUE);    

	}
	break;

    case WM_SETCURSOR:
	if (LOWORD(lParam) == HTCLIENT && HIWORD(lParam)  && 
                WI[curr_win].cursortype == G_CURSOR_CROSSHAIR) {
	    int i;
	    for (i = 0; i < G_MAXWINDOW; i++)
		if (i != curr_win && !WI[i].cursorhidden) {
		    INTERNAL_hide_cursor_crosshair(i);
		}
          if (WI[curr_win].cursortype == G_CURSOR_CROSSHAIR) {
              SetCursor(LoadCursor(NULL, IDC_CROSS));
          }
          else if (WI[curr_win].cursortype == G_CURSOR_HAND) {
              INTERNAL_set_handcursor();
          }
          else
	       SetCursor(LoadCursor(NULL, IDC_ARROW));
 	   if (!hMouseHook)
		hMouseHook = SetWindowsHookEx(WH_MOUSE,
					      (HOOKPROC) MouseProc,
					      (HINSTANCE) _hInst,
					      (DWORD) NULL);
	}
	else if (LOWORD(lParam) == HTCLIENT && HIWORD(lParam) &&
		 WI[curr_win].cursortype == G_CURSOR_HAND) {
	    INTERNAL_kill_crosshair(FALSE);
	    INTERNAL_set_handcursor();
	}
	else if (LOWORD(lParam) == HTCLIENT && HIWORD(lParam) &&
		 WI[curr_win].cursortype == G_CURSOR_UPDOWN) {
	    INTERNAL_kill_crosshair(FALSE);
	    INTERNAL_set_updowncursor();
	}
	else if (LOWORD(lParam) == HTCLIENT && HIWORD(lParam) &&
		 WI[curr_win].cursortype == G_CURSOR_WAIT) {
	    INTERNAL_kill_crosshair(FALSE);
	    SetCursor(LoadCursor(NULL, IDC_WAIT));
       }
	else {
	    INTERNAL_kill_crosshair(FALSE);
	    return (DefWindowProc(hWnd, messg, wParam, lParam));
	}
	break;

    case WM_PAINT:
	if (WI[curr_win].locked) {
	    ValidateRect(hWnd, NULL);
	    break;
	}
	INTERNAL_hide_cursor_crosshair(curr_win);
	if (WI[curr_win].usepix)
	    copy_pixmap(curr_win);
	else {
          WI[curr_win].locked = TRUE;
           SetBkMode(WI[curr_win].hdc, TRANSPARENT);
	    INTERNAL_update_window(G_SCREEN, curr_win);
	}
	ValidateRect(hWnd, NULL);
      INTERNAL_show_cursor_crosshair(curr_win);
      WI[curr_win].locked = FALSE;
	break;

    case WM_USER:

	switch (LOWORD(wParam)) {
	case ID_KILL_CROSSHAIR:
	    INTERNAL_kill_crosshair(lParam);
	    break;

       case ID_UPDATE:
		if (WI[curr_win].usepix) {
		    INTERNAL_clear_pixmap(curr_win);
                 INTERNAL_update_window(G_SCREEN, curr_win);
	       }
		 else InvalidateRect(hWnd, NULL, TRUE);
             break;

	case ID_RESIZE:{
		int x0, y0, y1, w, h, id_child;
		y0 = y1 = x0 = 0;

            id_child = C_BUTTONBAR1;
             if (WI[curr_win].child[id_child].hwnd) {
                 if (GetWindowRect(WI[curr_win].child[id_child].hwnd, &rc)!=0)
                     WI[curr_win].child[id_child].height = 
                             rc.bottom - rc.top;
		    y0 += WI[curr_win].child[id_child].height;
             }
		for (id_child = C_DRAWAREA + 1, y1 = 0; id_child < MAXWCHILDREN; id_child++) {
             
                if (WI[curr_win].child[id_child].hwnd) {
                  RECT rc;
       
                  if (GetWindowRect(WI[curr_win].child[id_child].hwnd, &rc)!=0)
                      WI[curr_win].child[id_child].height = 
                             rc.bottom - rc.top;
//  g_cons_printf("y0 = %d y1 = %d ,i=%d, %d vis=%d\n",
//  y0,y1,id_child,WI[curr_win].child[id_child].height,
// IsWindowVisible(WI[curr_win].child[id_child].hwnd));
	             y1 += WI[curr_win].child[id_child].height;
                }

             }
		w = LOWORD(lParam) - 1;
		h = HIWORD(lParam) - 1 - y0 - y1;
		WI[curr_win].child[C_DRAWAREA].height = h;
		MoveWindow(WI[curr_win].child[C_DRAWAREA].hwnd,
			   x0,
			   y0,
			   w,
			   h,
			   TRUE);
             GetClientRect(hWnd,&rc);
		G_WIN_WIDTH(curr_win) = rc.right - rc.left;
		G_WIN_HEIGHT(curr_win) = rc.bottom - rc.top;
		if (WI[curr_win].usepix) {
		    DeleteObject(WI[curr_win].pix);
		    pixinit(curr_win);
		    INTERNAL_update_window(G_SCREEN, curr_win);
		}
            else InvalidateRect(hWnd, NULL, TRUE);
          }
          break;
	}
	break;

    case WM_HSCROLL:
	{
	    int i, update = FALSE, id = curr_win;
           static int last_request;

	    switch (LOWORD(wParam)) {
	    case SB_LINEDOWN:
		//update = INTERNAL_UpdateScrollBarsH(id, SCROLL_LINEDOWN, 0);
             last_request = SCROLL_LINEDOWN;
		break;
	    case SB_LINEUP:
		//update = INTERNAL_UpdateScrollBarsH(id, SCROLL_LINEUP, 0);
             last_request = SCROLL_LINEUP;
		break;
	    case SB_PAGEDOWN:
		//update = INTERNAL_UpdateScrollBarsH(id, SCROLL_PAGEDOWN, 0);
             last_request = SCROLL_PAGEDOWN;
		break;
	    case SB_PAGEUP:
		//update = INTERNAL_UpdateScrollBarsH(id, SCROLL_PAGEUP, 0);
             last_request = SCROLL_PAGEUP;
		break;
	    case SB_THUMBPOSITION:
             break;
	    case SB_THUMBTRACK:
		update = INTERNAL_UpdateScrollBarsH(id,
						    SCROLL_DRAG,
						    HIWORD(wParam));

           case SB_ENDSCROLL:
             if (last_request)
                update = INTERNAL_UpdateScrollBarsH(id, last_request, 0);
             last_request = 0;
             break;
	    }

          if (INTERNAL_object_playback_flag) break;
 
	    if (update) {
              SendMessage(hWnd, WM_USER, ID_UPDATE, update);
	    }
      }  
	break;

    case WM_VSCROLL:
  	{
	    int update = FALSE, id = curr_win;
           static int last_request;

	    switch (LOWORD(wParam)) {
 	    case SB_LINEDOWN:
		//update = INTERNAL_UpdateScrollBarsV(id, SCROLL_LINEDOWN, 0);
             last_request = SCROLL_LINEDOWN;
		break;
	    case SB_LINEUP:
		//update = INTERNAL_UpdateScrollBarsV(id, SCROLL_LINEUP, 0);
             last_request = SCROLL_LINEUP;
		break;
	    case SB_PAGEDOWN:
		//update = INTERNAL_UpdateScrollBarsV(id, SCROLL_PAGEDOWN, 0);
             last_request = SCROLL_PAGEDOWN;
		break;
	    case SB_PAGEUP:
		//update = INTERNAL_UpdateScrollBarsV(id, SCROLL_PAGEUP, 0);
             last_request = SCROLL_PAGEUP;
		break;
	    case SB_THUMBPOSITION:
             break;
	    case SB_THUMBTRACK:
		update = INTERNAL_UpdateScrollBarsV(id,
						    SCROLL_DRAG,
						    HIWORD(wParam));
		break;
           case SB_ENDSCROLL:
             if (last_request)
                update = INTERNAL_UpdateScrollBarsV(id, last_request, 0);
             last_request = 0;
             break;
	    }
           if (INTERNAL_object_playback_flag) break;
	    if (update) {
               SendMessage(hWnd, WM_USER, ID_UPDATE, 0);
	    }
 	}
	break;

    case WM_CHAR:
	{
	    POINT point;

	    GetCursorPos(&point);
	    ScreenToClient(hWnd, &point);
	    ui.x = point.x;
	    ui.y = point.y;
	    ui.event = G_KEYPRESS;
	    ui.keycode = (unsigned int) wParam;
	    INTERNAL_prep_event(&ui);
	    if (INTERNAL_process_event(G_KEYPRESS_EVENT, &ui))
		INTERNAL_add_event(&ui);
	}
	break;

    case WM_MBUTTONDOWN:
	ui.x = LOWORD(lParam);
	ui.y = HIWORD(lParam);
	ui.event = G_BUTTON2PRESS;
	ui.keycode = 0;
	INTERNAL_prep_event(&ui);
	if (INTERNAL_process_event(G_BUTTON2PRESS_EVENT, &ui))
	    INTERNAL_add_event(&ui);
	break;

    case WM_RBUTTONDOWN:
       if (wParam & MK_CONTROL) {
	    ui.x = LOWORD(lParam);
	    ui.y = HIWORD(lParam);
	    ui.event = G_BUTTON2PRESS;
	    ui.keycode = 0;
	    INTERNAL_prep_event(&ui);
	    if (INTERNAL_process_event(G_BUTTON2PRESS_EVENT, &ui))
	        INTERNAL_add_event(&ui);
	    break;
      }
	if (WI[curr_win].hmenu != NULL) {
	    POINT point;
	    point.x = LOWORD(lParam);
	    point.y = HIWORD(lParam);
	    ClientToScreen(hwnd_parent, &point);
	    TrackPopupMenu(WI[curr_win].hmenu,
			   TPM_LEFTALIGN | TPM_RIGHTBUTTON,
			   point.x,
			   point.y,
			   0,
			   hwnd_parent,
			   NULL);
	    break;
	}
	ui.x = LOWORD(lParam);
	ui.y = HIWORD(lParam);
	ui.event = G_BUTTON3PRESS;
	ui.keycode = 0;
	INTERNAL_prep_event(&ui);
	if (INTERNAL_process_event(G_BUTTON3PRESS_EVENT, &ui))
	    INTERNAL_add_event(&ui);
	break;

    case WM_LBUTTONDOWN:
	ui.x = LOWORD(lParam);
	ui.y = HIWORD(lParam);
	ui.event = G_BUTTON1PRESS;
	ui.keycode = 0;
	INTERNAL_prep_event(&ui);
	if (INTERNAL_process_event(G_BUTTON1PRESS_EVENT, &ui))
	    INTERNAL_add_event(&ui);
	break;

    case WM_MBUTTONUP:
	ui.x = LOWORD(lParam);
	ui.y = HIWORD(lParam);
	ui.event = G_BUTTON2RELEASE;
	ui.keycode = 0;
	INTERNAL_prep_event(&ui);
	if (INTERNAL_process_event(G_BUTTON2RELEASE_EVENT, &ui))
	    INTERNAL_add_event(&ui);
	break;

    case WM_RBUTTONUP:
       if (wParam & MK_CONTROL) {
	    ui.x = LOWORD(lParam);
	    ui.y = HIWORD(lParam);
	    ui.event = G_BUTTON2RELEASE;
	    ui.keycode = 0;
	    INTERNAL_prep_event(&ui);
	    if (INTERNAL_process_event(G_BUTTON2RELEASE_EVENT, &ui))
	        INTERNAL_add_event(&ui);
	    break;
      }
	ui.x = LOWORD(lParam);
	ui.y = HIWORD(lParam);
	ui.event = G_BUTTON3RELEASE;
	ui.keycode = 0;
	INTERNAL_prep_event(&ui);
	if (INTERNAL_process_event(G_BUTTON3RELEASE_EVENT, &ui))
	    INTERNAL_add_event(&ui);
	break;

    case WM_LBUTTONUP: 
	ui.x = LOWORD(lParam);
	ui.y = HIWORD(lParam);
	ui.event = G_BUTTON1RELEASE;
	ui.keycode = 0;
	INTERNAL_prep_event(&ui);
	if (INTERNAL_process_event(G_BUTTON1RELEASE_EVENT, &ui))
	    INTERNAL_add_event(&ui);
      break;

    case WM_MOUSEMOVE:{
	    int doit1, doit2;
	    doit1 = doit2 = FALSE;
	    if (WI[curr_win].cursortype == G_CURSOR_CROSSHAIR ||
		WI[curr_win].pixcopymode) {
		xx[curr_win] = LOWORD(lParam);
		yy[curr_win] = HIWORD(lParam);
		INTERNAL_update_crosshair(curr_win, xx[curr_win], yy[curr_win]);
	    }
	    ui.x = LOWORD(lParam);
	    ui.y = HIWORD(lParam);
	    ui.keycode = 0;
	    ui.event = G_POINTERMOTION;
	    if (wParam & MK_LBUTTON) {
		ui.event |= G_BUTTON1PRESS;
		INTERNAL_prep_event(&ui);
		if (INTERNAL_process_event(G_POINTERMOTION_EVENT, &ui))
		    doit1 = TRUE;
		if (INTERNAL_process_event(G_BUTTON1PRESS_EVENT, &ui))
		    doit2 = TRUE;
	    }
	    else if (wParam & MK_RBUTTON) {
		ui.event |= G_BUTTON3PRESS;
		INTERNAL_prep_event(&ui);
		if (INTERNAL_process_event(G_POINTERMOTION_EVENT, &ui))
		    doit1 = TRUE;
		if (INTERNAL_process_event(G_BUTTON3PRESS_EVENT, &ui))
		    doit2 = TRUE;
	    }
	    else if (wParam & MK_MBUTTON) {
		ui.event |= G_BUTTON2PRESS;
		INTERNAL_prep_event(&ui);
		if (INTERNAL_process_event(G_POINTERMOTION_EVENT, &ui))
		    doit1 = TRUE;
		if (INTERNAL_process_event(G_BUTTON2PRESS_EVENT, &ui))
		    doit2 = TRUE;
	    }
	    if (doit1 && doit2)
		INTERNAL_add_event(&ui);
	}
	break;

    case WM_DESTROY:
      if (!(WI[curr_win].flag & G_WIN_CONSOLE))
	  if (WI[curr_win].flag & G_WIN_BUFFER)
	    g_set_windowbuffer(curr_win, FALSE);
	INTERNAL_hide_cursor_crosshair(curr_win);
	PostQuitMessage(0);
	break;

    default:
	return (DefWindowProc(hWnd, messg, wParam, lParam));
    }
    return (0L);
}



int GENPLOT_(open) (void) 
{
    WNDCLASS wcApp;
    int i, j;
HICON hicon;
HBITMAP hbm;
char **xpm;
HDC hdcScreen;


    set_SCREEN_mode();

    if (!_hPreInst) {

/*
typedef struct _ICONINFO { 
   BOOL    fIcon;  
   DWORD   xHotspot; 
   DWORD   yHotspot; 
   HBITMAP hbmMask; 
   HBITMAP hbmColor; 
} ICONINFO; 
*/
/*

    ICONINFO ifo;
hdcScreen = CreateDC("DISPLAY", NULL, NULL, NULL); 

xpm = INTERNAL_get_icon_pixmap(32);
hbm = INTERNAL_Compose_Bitmap(hdcScreen, xpm);

      ifo.fIcon = TRUE;
      ifo.xHotspot = 1; 
      ifo.yHotspot = 1; 
      ifo.hbmMask = hbm; 
      ifo.hbmColor = hbm; 
hicon =  CreateIconIndirect(&ifo);
*/
	wcApp.lpszClassName = PlotName;
	wcApp.hInstance = _hInst;
	wcApp.lpfnWndProc = WndProc;
	wcApp.hCursor = LoadCursor(NULL, IDC_ARROW);
	wcApp.hIcon = LoadIcon(_hInst, MAKEINTRESOURCE(ICON_ID));
	wcApp.lpszMenuName = NULL;
	wcApp.hbrBackground = GetStockObject(WHITE_BRUSH);
//	wcApp.style = CS_HREDRAW | CS_VREDRAW | CS_NOCLOSE;
	wcApp.style = CS_HREDRAW | CS_VREDRAW ;
	wcApp.cbClsExtra = 0;
	wcApp.cbWndExtra = sizeof(LONG);
DeleteDC(hdcScreen);
	if (!RegisterClass(&wcApp))
	    return G_ERROR;
#ifdef meuk
      memset (&wcApp, 0, sizeof(WNDCLASSEX));
	wcApp.style = CS_HREDRAW | CS_VREDRAW | CS_NOCLOSE;
	wcApp.lpfnWndProc = WndProc;
	wcApp.cbClsExtra = 0;
	wcApp.cbWndExtra = sizeof(long);
	wcApp.hInstance = _hInst; // hInstance;
	wcApp.hIcon = hicon ; // LoadIcon (NULL, IDI_APPLICATION);
	wcApp.hIconSm = LoadIcon (NULL, IDI_APPLICATION);
	wcApp.hCursor = LoadCursor(NULL, IDC_ARROW);
	wcApp.hbrBackground = (HBRUSH) GetStockObject(WHITE_BRUSH);
	wcApp.lpszMenuName = NULL;
	wcApp.lpszClassName = PlotName;
      wcApp.cbSize = sizeof(WNDCLASSEX);
	if (!RegisterClassEx(&wcApp))
          return G_ERROR;
#endif

    }
    mswin_linestyle = PS_SOLID;
    mswin_linewidth = 1;
    mswin_color = G_BLACK;
    mswin_bkcolor = G_WHITE;
    for (i = 0; i < G_MAXWINDOW; i++)
	ResetWI(i);
    INTERNAL_init_screen_stuff();
    INTERNAL_init_cursor();
    GENPLOT_(device_width) = (float) GetSystemMetrics(SM_CXSCREEN);
    GENPLOT_(device_height) = (float) GetSystemMetrics(SM_CYSCREEN);
    return G_OK;
}



LRESULT CALLBACK WndProc(HWND hWnd, UINT messg,
			 WPARAM wParam, LPARAM lParam)
{
    int i, curr_win, y0;
    RECT rc, rccl;
    G_EVENT ui;

    switch (messg) {

    case WM_SIZE:

	curr_win = GetWindowLong(hWnd, 0);
	if (WI[curr_win].noresize)
	    break;
       if (WI[curr_win].child[C_BUTTONBAR1].hwnd) 
		SendMessage(WI[curr_win].child[C_BUTTONBAR1].hwnd, 
                       TB_AUTOSIZE, 0,0);
       GetClientRect(hWnd, &rc);
       y0 = rc.bottom;
       if (WI[curr_win].child[C_COMMANDLINE].hwnd) {
		SendMessage(WI[curr_win].child[C_COMMANDLINE].hwnd,
			    WM_USER, ID_RESIZE, lParam);
             GetWindowRect( WI[curr_win].child[C_COMMANDLINE].hwnd,&rccl);
             y0 -= rccl.bottom-rccl.top;
       }
       for (i=C_BUTTONBAR4;i>=C_BUTTONBAR2;i--) {
           RECT rctb;
           if (WI[curr_win].child[i].hwnd) {
               GetWindowRect( WI[curr_win].child[i].hwnd,&rctb);
               y0 -= rctb.bottom-rctb.top; 
               SetWindowPos( WI[curr_win].child[i].hwnd,
                    NULL, 0, y0, rc.right - rc.left , rctb.bottom-rctb.top, 
                    SWP_NOZORDER) ;
           }
       }
       if (WI[curr_win].child[C_DRAWAREA].hwnd) {
		SendMessage(WI[curr_win].child[C_DRAWAREA].hwnd,
			    WM_USER, ID_RESIZE, lParam);
       }
	break;

    case WM_USER:

	switch (LOWORD(wParam)) {

	case ID_RESIZE:{

	    curr_win = GetWindowLong(hWnd, 0);
	    if (WI[curr_win].noresize)
	        break;
           if (WI[curr_win].child[C_BUTTONBAR1].hwnd) 
		SendMessage(WI[curr_win].child[C_BUTTONBAR1].hwnd, 
                       TB_AUTOSIZE, 0,0);
           GetClientRect(hWnd, &rc);
           y0 = rc.bottom;
           if (WI[curr_win].child[C_COMMANDLINE].hwnd) {
		    SendMessage(WI[curr_win].child[C_COMMANDLINE].hwnd,
			    WM_USER, ID_RESIZE, lParam);
                 GetWindowRect( WI[curr_win].child[C_COMMANDLINE].hwnd,&rccl);
                 y0 -= rccl.bottom-rccl.top;
           }
           for (i=C_BUTTONBAR4;i>=C_BUTTONBAR2;i--) {
               RECT rctb;
              // UINT flag = SWP_NOZORDER;
              // if (!doingtoolbar_stuff) SWP_SHOWWINDOW | SWP_NOZORDER
               if (WI[curr_win].child[i].hwnd) {
                   GetWindowRect( WI[curr_win].child[i].hwnd,&rctb);
                   y0 -= rctb.bottom-rctb.top; 
                   SetWindowPos( WI[curr_win].child[i].hwnd,
                        NULL, 0, y0, rc.right - rc.left , rctb.bottom-rctb.top, 
                        SWP_NOZORDER) ;
               }
           }
      //    if (WI[curr_win].child[C_DRAWAREA].hwnd) 
	//	    SendMessage(WI[curr_win].child[C_DRAWAREA].hwnd,
	//		    WM_USER, ID_RESIZE, lParam);
           }
           break;
	}
	break;

    case WM_SETCURSOR:
	if (LOWORD(lParam) != HTCLIENT) {
	    curr_win = GetWindowLong(hWnd, 0);
	    SendMessage(WI[curr_win].child[C_DRAWAREA].hwnd,
			WM_USER, ID_KILL_CROSSHAIR, FALSE);
	}
	return (DefWindowProc(hWnd, messg, wParam, lParam));

    case WM_COMMAND:
	if (LOWORD(wParam)) {
	    G_EVENT ui;
	    ui.win_id = GetWindowLong(hWnd, 0);
	    ui.vp_id = G_ERROR;
	    ui.x = 0;
	    ui.y = 0;
	    ui.event = G_COMMAND;
	    ui.keycode = (unsigned int) LOWORD(wParam) - 1;
	    ui.command = g_menu_get_label(G_ERROR, ui.keycode);
	    INTERNAL_prep_event(&ui);
	    if (INTERNAL_process_event(G_BUTTON3PRESS_EVENT, &ui))
		INTERNAL_add_event(&ui);
	    INTERNAL_toggle_checkmark(ui.keycode + 1);
	}
	break;

    case WM_CHAR:
	curr_win = GetWindowLong(hWnd, 0);
	if (WI[curr_win].child[C_COMMANDLINE].hwnd){
// Special case: pass on from parent window to child
          SendMessage(WI[curr_win].child[C_COMMANDLINE].hwnd,
		               WM_USER, ID_FOCUS, 0);
	   SendMessage(WI[curr_win].child[C_COMMANDLINE].hwnd,
		    messg, wParam, lParam);
       }
	else
	    SendMessage(WI[curr_win].child[C_DRAWAREA].hwnd,
		    messg, wParam, lParam);
	break;

    case WM_DESTROY:
	PostQuitMessage(0);
	break;

    case WM_SETFOCUS:
        if (LOWORD(wParam) != WA_INACTIVE) {
            curr_win = GetWindowLong(hWnd, 0);
            if (WI[curr_win].flag & G_WIN_COMMANDLINE_FOCUS)
                SendMessage(WI[curr_win].child[C_COMMANDLINE].hwnd,
		               WM_USER, ID_FOCUS, 0);
        }    
        break;

    case WM_NOTIFY:  
        switch (((LPNMHDR) lParam)->code) { 
            case TTN_NEEDTEXT: 
            { 
                int idButton;
                char *p;
                LPTOOLTIPTEXT lpttt; 

                lpttt = (LPTOOLTIPTEXT) lParam; 

                idButton = lpttt->hdr.idFrom; 
                p = INTERNAL_get_tooltip(idButton);
                if (p != NULL)
                    strcpy(lpttt->szText,p);
            }
            break;
       }
       break;

    case WM_CLOSE:
 	  curr_win = GetWindowLong(hWnd, 0);
        ui.win_id = curr_win;
        ui.x = 0;
        ui.y = 0;
        ui.keycode = 0;
        ui.event = G_WINDOWQUIT;
        INTERNAL_prep_event(&ui);
        if (INTERNAL_process_event(G_WINDOWQUIT_EVENT, &ui))
            INTERNAL_add_event(&ui);
        INTERNAL_dispatch_message();
        break;

    default:
	return (DefWindowProc(hWnd, messg, wParam, lParam));
    }
    return (0L);
}


void GENPLOT_(open_window) (int id, long x0, long y0, long width, long height,
				   char *title, long flag) 
{
    DWORD style;
    RECT rc;
    
    style = WS_OVERLAPPEDWINDOW | WS_THICKFRAME | WS_CAPTION;
    if (flag & G_WIN_NORESIZE)
	WI[id].noresize = TRUE;
    if (flag & G_WIN_BUTTONBAR_32)
	WI[id].bbar32 = TRUE;
    width += 2 +
	2 * GetSystemMetrics(SM_CXFRAME) -
	GetSystemMetrics(SM_CXBORDER);
    height += 2 +
	GetSystemMetrics(SM_CYCAPTION) +
	2 * GetSystemMetrics(SM_CYFRAME) -
	2 * GetSystemMetrics(SM_CYBORDER);
    WI[id].flag = flag;
    WI[id].isvisible = FALSE;

    rc.left   = x0;
    rc.top    = y0;
    rc.right  = x0 + width - 1;
    rc.bottom = y0 + height - 1;
  //  AdjustWindowRect(&rc, style, (flag & G_WIN_MENUBAR));
    WI[id].hparent = CreateWindow(PlotName,
				  title,
				  style,
				  (int) x0,
				  (int) y0,
				  (int) rc.right - rc.left + 1,
				  (int) rc.bottom - rc.top + 1,
				  (HWND) NULL,
				  (HMENU) NULL,
				  (HANDLE) _hInst,
				  (LPSTR) NULL);

    SetWindowLong(WI[id].hparent, 0, (LONG) id);
    GetClientRect(WI[id].hparent, &rc);

    if (flag & G_WIN_CONSOLE)
	WI[id].child[C_DRAWAREA].hwnd = CreateConsole(WI[id].hparent,
						      0,
						      0,
						      width,
						      height,
						      id,
						      _hInst);
    else
	WI[id].child[C_DRAWAREA].hwnd = CreateDrawingArea(WI[id].hparent,
						      rc.left,
						      rc.top,
						      width,
						      height,
						      id,
						      _hInst,
						      flag);
    WI[id].child[C_DRAWAREA].height = height;

    if (flag & G_WIN_COMMANDLINE)
	INTERNAL_create_commandline(id, flag &
			      (G_WIN_COMMANDLINE_RETURN - G_WIN_COMMANDLINE));

    if (!WI[id].isvisible && !(flag & G_WIN_MENUBAR)) {
        ShowWindow(WI[id].hparent, SW_SHOW);
        UpdateWindow(WI[id].hparent);
        WI[id].isvisible = TRUE;
    }

    INTERNAL_dispatch_message();
    if (!(flag & G_WIN_CONSOLE))
	if (flag & G_WIN_BUFFER)
	    g_set_windowbuffer(MAKE_GLOBAL_ID(id, G_SCREEN), TRUE);

}



void GENPLOT_(close_window) (int id)
{
INTERNAL_check_hdc(&(WI[GD.win_id]),FALSE);
    INTERNAL_reset_cursor(id);
    g_scrollbar_disconnect(MAKE_GLOBAL_ID(id, gen_device));
    DestroyWindow(WI[id].hparent);
    INTERNAL_kill_menu(id);
    INTERNAL_dispatch_message();
    ResetWI(id);
}


void GENPLOT_(select_window) (int id) 
{
    id = id;
    INTERNAL_dispatch_message();
}

void GENPLOT_(raise_window) (int id) 
{
    SetForegroundWindow(WI[id].hparent);
    INTERNAL_dispatch_message();
}

void GENPLOT_(close) (void) 
{
    INTERNAL_kill_crosshair(TRUE);
    INTERNAL_exit_screen_stuff();
    INTERNAL_destroy_handcursor();
    INTERNAL_destroy_updowncursor();
    set_DUMMY_mode();
}

void GENPLOT_(moveto) (long x, long y) 
{
    x = x;
    y = y;
}

void GENPLOT_(line) (long x1, long y1, long x2, long y2, int isclipped) 
{
    INTERNAL_check_hdc(&(WI[GD.win_id]),TRUE);
    isclipped = isclipped;
    MoveToEx(MSHDC, (int) x1, (int) y1, NULL);
    LineTo(MSHDC, (int) x2, (int) y2);
//    INTERNAL_check_hdc(&(WI[GD.win_id]),FALSE);
}


void GENPLOT_(linewidth) (int width) 
{
    mswin_linewidth = width;
    select_pen(&(WI[GD.win_id]));
}

void GENPLOT_(linestyle) (unsigned pattern) 
{
    switch (pattern) {
    case G_SOLID:
	mswin_linestyle = PS_SOLID;
	break;
    case G_SHORT_DASHED:
	mswin_linestyle = PS_DASHDOT;
	break;
    case G_LONG_DASHED:
	mswin_linestyle = PS_DASH;
	break;
    case G_DOTTED:
	mswin_linestyle = PS_DOT;
	break;
    }
    select_pen(&(WI[GD.win_id]));
}

void GENPLOT_(label)(char *label) 
{
    static LOGFONT lf;
    HFONT hNFont, oldFont;

    INTERNAL_check_hdc(&(WI[GD.win_id]),TRUE);
    if (TEXTROTATE)
	lf.lfEscapement = 900;
    else
	lf.lfEscapement = 0;
    if (INTERNAL_store_gc.fontscaling == G_RELATIVE_FONTSCALING)
	lf.lfHeight = (int) (INTERNAL_store_gc.charsize 
                           * WINHEIGHT / G_POINTSIZE_SCALING);
    else
	lf.lfHeight = (int) (INTERNAL_store_gc.charsize * G_POINTSIZE);
    lf.lfCharSet = DEFAULT_CHARSET;
    lf.lfPitchAndFamily = DEFAULT_PITCH;
    strcpy(lf.lfFaceName, mswin_fonts[INTERNAL_store_gc.font]);
    hNFont = CreateFontIndirect(&lf);
    oldFont = SelectObject(MSHDC, hNFont);

    SetTextAlign(MSHDC, TA_LEFT | TA_BOTTOM);
    SetTextColor(MSHDC, RGBCOLOR(mswin_color));
    SetBkMode(MSHDC, TRANSPARENT);
    TextOut(MSHDC, (int) INTERNAL_store_vp.cx, (int) INTERNAL_store_vp.cy,
	    label, strlen(label));
    SelectObject(MSHDC, oldFont);
    DeleteObject(hNFont);
  //  INTERNAL_check_hdc(&(WI[GD.win_id]),FALSE);
}

void GENPLOT_(rectangle) (long x1, long y1, long x2, long y2) 
{
    INTERNAL_check_hdc(&(WI[GD.win_id]),TRUE);
    MoveToEx(MSHDC, (int) x1, (int) y1, NULL);
    LineTo(MSHDC, (int) x1, (int) y2);
    LineTo(MSHDC, (int) x2, (int) y2);
    LineTo(MSHDC, (int) x2, (int) y1);
    LineTo(MSHDC, (int) x1, (int) y1);
 //   INTERNAL_check_hdc(&(WI[GD.win_id]),FALSE);
}

void GENPLOT_(drawpoly) (long numpoint, polytype * points) 
{
    int i, j;
    POINT *poly;

    INTERNAL_check_hdc(&(WI[GD.win_id]),TRUE);
    if ((poly = (POINT *) malloc((int) numpoint * sizeof(POINT))) == NULL) {
	fprintf(stderr, "Memory allocation error\n");
	exit(1);
    }
    for (i = 0, j = 0; i < numpoint * 2; j++, i += 2) {
	poly[j].x = points[i];
	poly[j].y = points[i + 1];
    }
    Polyline(MSHDC, poly, (int) numpoint);
    free(poly);
 //   INTERNAL_check_hdc(&(WI[GD.win_id]),FALSE);
}

void GENPLOT_(circle) (long r)
{
    INTERNAL_check_hdc(&(WI[GD.win_id]),TRUE);
    Arc(MSHDC,
	(int) INTERNAL_store_vp.cx - (int) r,
	(int) INTERNAL_store_vp.cy - (int) r,
	(int) INTERNAL_store_vp.cx + (int) r,
	(int) INTERNAL_store_vp.cy + (int) r,
	(int) INTERNAL_store_vp.cx - (int) r,
	(int) INTERNAL_store_vp.cy - (int) r,
	(int) INTERNAL_store_vp.cx - (int) r,
	(int) INTERNAL_store_vp.cy - (int) r);
  //  INTERNAL_check_hdc(&(WI[GD.win_id]),FALSE);
}

void GENPLOT_(fillrectangle) (long x1, long y1, long x2, long y2) 
{
    RECT r;
    static HBRUSH hBrush, hOBrush;
    static HPEN hPen, hOPen;

    INTERNAL_check_hdc(&(WI[GD.win_id]),TRUE);
    hBrush = CreateSolidBrush(RGBCOLOR(mswin_color));
    hOBrush = SelectObject(MSHDC, hBrush);
    hPen = CreatePen(PS_SOLID,
		     1,
		     RGBCOLOR(mswin_color));
    hOPen = SelectObject(MSHDC, hPen);

    r.left = (int) x1;
    r.top = (int) y1;
    r.right = (int) x2;
    r.bottom = (int) y2;
    FillRect(MSHDC, &r, hBrush);
    SelectObject(MSHDC, hOBrush);
    SelectObject(MSHDC, hOPen);
    DeleteObject(hBrush);
    DeleteObject(hPen);
  //  INTERNAL_check_hdc(&(WI[GD.win_id]),FALSE);
}

void GENPLOT_(fillpoly) (long numpoint, polytype * points) 
{
    int i, j;
    POINT *poly;
    static HBRUSH hBrush, hOBrush;
    static HPEN hPen, hOPen;

    INTERNAL_check_hdc(&(WI[GD.win_id]),TRUE);
    hBrush = CreateSolidBrush(RGBCOLOR(mswin_color));
    hOBrush = SelectObject(MSHDC, hBrush);
    hPen = CreatePen(PS_SOLID,
		     1,
		     RGBCOLOR(mswin_color));
    hOPen = SelectObject(MSHDC, hPen);

    if ((poly = (POINT *) malloc((int) numpoint * sizeof(POINT))) == NULL) {
	fprintf(stderr, "Memory allocation error\n");
	exit(1);
    }
    for (i = 0, j = 0; i < numpoint * 2; j++, i += 2) {
	poly[j].x = points[i];
	poly[j].y = points[i + 1];
    }
    Polygon(MSHDC, poly, (int) numpoint);
    free(poly);
    SelectObject(MSHDC, hOBrush);
    SelectObject(MSHDC, hOPen);
    DeleteObject(hBrush);
    DeleteObject(hPen);
    // INTERNAL_check_hdc(&(WI[GD.win_id]),FALSE);
}

void GENPLOT_(fillcircle) (long r) 
{
    static HBRUSH hBrush, hOBrush;
    static HPEN hPen, hOPen;

    INTERNAL_check_hdc(&(WI[GD.win_id]),TRUE);
    hBrush = CreateSolidBrush(RGBCOLOR(mswin_color));
    hOBrush = SelectObject(MSHDC, hBrush);
    hPen = CreatePen(PS_SOLID,
		     1,
		     RGBCOLOR(mswin_color));
    hOPen = SelectObject(MSHDC, hPen);

    Ellipse(MSHDC,
	    (int) INTERNAL_store_vp.cx - (int) r,
	    (int) INTERNAL_store_vp.cy - (int) r,
	    (int) INTERNAL_store_vp.cx + (int) r,
	    (int) INTERNAL_store_vp.cy + (int) r);
    SelectObject(MSHDC, hOBrush);
    SelectObject(MSHDC, hOPen);
    DeleteObject(hBrush);
    DeleteObject(hPen);
 //   INTERNAL_check_hdc(&(WI[GD.win_id]),FALSE);
}

void GENPLOT_(foreground) (int color) 
{
    if (WI[GD.win_id].flag & G_WIN_BLACK_ON_WHITE)
        mswin_color = G_BLACK;
    else
        mswin_color = color;
    select_pen(&(WI[GD.win_id]));
}

void GENPLOT_(background) (int color) 
{
    INTERNAL_check_hdc(&(WI[GD.win_id]),TRUE);
    if (WI[GD.win_id].flag & G_WIN_BLACK_ON_WHITE)
        mswin_bkcolor = G_WHITE;
    else
        mswin_bkcolor = color;
    SetBkColor(MSHDC, RGBCOLOR(color));
    INTERNAL_check_hdc(&(WI[GD.win_id]),FALSE);
}

int GENPLOT_(palettesize)(int size)
{ 
    size = size; 
    return G_OK; 
}

int GENPLOT_(paletteentry)(int entry_id, G_PALETTEENTRY entry)
{ 
    entry_id = entry_id; 
    entry = entry; 
    return G_OK; 
}

void GENPLOT_(clipping) (int clip) 
{
    int x1, y1, x2, y2;

    WI[GD.win_id].isclipped = clip;
    if (clip) {
        INTERNAL_get_clip(&x1, &y1, &x2, &y2);
	WI[GD.win_id].clipx1 = (int) x1;
	WI[GD.win_id].clipy1 = (int) y1;
	WI[GD.win_id].clipx2 = (int) x2 + 1;
	WI[GD.win_id].clipy2 = (int) y2 + 1;
    }
    select_clip(&(WI[GD.win_id]));
}

void GENPLOT_(newpage) (void) 
{
    int color;

    INTERNAL_dispatch_message();
    if (WI[GD.win_id].usepix) {
	INTERNAL_clear_pixmap(GD.win_id);
	return;
    }
    INTERNAL_check_hdc(&(WI[GD.win_id]),FALSE);
    INTERNAL_hide_cursor_crosshair(GD.win_id);
    MSHDC = GetDC(MSWIN);
    color = mswin_color;
    mswin_color = mswin_bkcolor;
    GENPLOT_(fillrectangle) (0, 0, WINWIDTH, WINHEIGHT);
    mswin_color = color;
    ReleaseDC(MSWIN, MSHDC);
    MSHDC = NULL;
    INTERNAL_show_cursor_crosshair(GD.win_id);
}

void GENPLOT_(clearviewport) (void) 
{
    int x1, x2, y1, y2;
    int color;
    int hdc_flag = FALSE;

    INTERNAL_dispatch_message();
    if (MSHDC)
	hdc_flag = TRUE;
    INTERNAL_get_clip(&x1, &y1, &x2, &y2);
    if (WI[GD.win_id].usepix) {
	clear_pixarea(GD.win_id, x1, y1, x2 + 1, y2 + 1);
	return;
    }
INTERNAL_check_hdc(&(WI[GD.win_id]),FALSE);
    if (!hdc_flag)
	INTERNAL_hide_cursor_crosshair(GD.win_id);
    color = mswin_color;
    mswin_color = mswin_bkcolor;
    if (!hdc_flag)
	MSHDC = GetDC(MSWIN);
    GENPLOT_(fillrectangle) (x1, y1, x2 + 1, y2 + 1);
    if (!hdc_flag) {
	ReleaseDC(MSWIN, MSHDC);
	MSHDC = NULL;
    }
    mswin_color = color;
    if (!hdc_flag)
	INTERNAL_show_cursor_crosshair(GD.win_id);
}


/******************************/
void g_flush(void)
{
    INTERNAL_dispatch_message();
    if (WI[GD.win_id].usepix){
        copy_pixmap(GD.win_id);
    }
}

void g_bell(void)
{
    MessageBeep(-1);
}

/****************************************************************************
* FONTS
*/
float GENPLOT_(fontheight) (int id) 
{
    float size;

    if (INTERNAL_store_gc.fontscaling == G_RELATIVE_FONTSCALING)
	size = INTERNAL_store_gc.charsize / G_POINTSIZE_SCALING;
    else
	size = (INTERNAL_store_gc.charsize * G_POINTSIZE) /
	    (float)g_dev[G_SCREEN].win[id].height;
    return size;
}

/****************/
void g_lower_xterm(void)
{;
}
void g_raise_xterm(void)
{;
}
void g_set_motif_realtextrotation(int t)
{
    t = t;
}
int g_get_xterm(void)
{
    return 0;
}


