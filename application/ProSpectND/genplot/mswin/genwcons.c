/************************************************************************/
/*                               genwcons.c                             */
/*                                                                      */
/*  Platform : Microsoft Windows                                        */
/*  Module   : Genplot Console functions                                */
/*                                                                      */
/*  Albert van Kuik                                                     */
/************************************************************************/
#define G_LOCAL

#include <stdio.h>
#include <windows.h>
#include <string.h>
#include <stdarg.h>
#include <stdlib.h>
#include <math.h>
#include <time.h>
#include <assert.h>

#ifdef DEBUG
#include "mshell.h"
#endif

#include "genplot.h"
#include "g_inter.h"
#include "genwin.h"
#include "header_icon.h"

/* --- CONSOLE --- */
#define SCROLLLINES   200
#define XSCROLLJUMP   8
#define TEXTBFSIZE    1024
#define CONSOLECLASS  "GP Console"

#define ID_EXIT	110
#define ID_COPY	111

typedef struct {
    int charwt, charht,			// char width and height in pixels
      xClientView, yClientView,		// client area width and height in pixels
      poshorzscroll, posvertscroll,	// position of h and v scrollbar in char
      scrolllines, scrollwidth,		// max h and v scroll positions in char
      linecount, lastline, noscrollx, maxchar, resized, xpos, ypos,
      xoff, yoff, old_xoff, old_ypos, isinput, isoutput, no_caret;
    char *textline[SCROLLLINES], *textbf, *textbf2;
    POINTS ptsCursor;     /* coordinates of mouse cursor  */
    BOOL isselect; /* text-selection flag    */
    int x1select, y1select, x2select, y2select;
    HFONT font, oldfont;
    HWND hwnd;
} CONSOLESTRUCT;


static void reformat_line(CONSOLESTRUCT *CI);
static void update_console(HDC hdc, CONSOLESTRUCT *CI);
static int write_console(CONSOLESTRUCT *CI,const char *buffer);
static int read_console(CONSOLESTRUCT *CI);
static int compute_scroll_x(CONSOLESTRUCT *CI);
static int compute_scroll_y(CONSOLESTRUCT *CI);
static int selection_to_clipboard(CONSOLESTRUCT *CI);

#define CONS_BUF_SIZE   1000
#define MAX_CONSOLE_LINES 500

static HWND cwin;


LRESULT CALLBACK ConsoleProc(HWND hWnd, UINT messg,
			     WPARAM wParam, LPARAM lParam)
{
    PAINTSTRUCT ps;
    CONSOLESTRUCT *CI;
    HDC hdc;
    SCROLLINFO sinf;
    int i;

    switch (messg) {

    case WM_CREATE:
	{
	    static LOGFONT lf;
	    TEXTMETRIC tm;
	    int i, device_width;
           RECT rc;

	    CI = (CONSOLESTRUCT *) calloc(sizeof(CONSOLESTRUCT),1);
	    CI->textbf = (char *) calloc(TEXTBFSIZE + 1, 1);
	    CI->textbf2 = (char *) calloc(TEXTBFSIZE + 1, 1);
	    CI->noscrollx = TRUE;
          CI->isselect=FALSE;
	    hdc = GetDC(hWnd);
	    device_width = GetDeviceCaps(hdc, HORZRES);
	    lf.lfEscapement = 0;
	    lf.lfHeight = G_POINTSIZE;
	    lf.lfCharSet = DEFAULT_CHARSET;
	    lf.lfPitchAndFamily = DEFAULT_PITCH;
	    strcpy(lf.lfFaceName, "Courier");

	    CI->font = CreateFontIndirect(&lf);
	    CI->oldfont = SelectObject(hdc, CI->font);

	    GetTextMetrics(hdc, &tm);
	    CI->charwt = tm.tmAveCharWidth;
	    CI->charht = tm.tmHeight + tm.tmExternalLeading;
	    ReleaseDC(hWnd, hdc);
	    CI->maxchar = device_width / CI->charwt;
	    CI->scrollwidth = CI->maxchar;
	    for (i = 0; i < SCROLLLINES; i++)
		CI->textline[i] = (char *) calloc(CI->maxchar + 1, 1);
	    CI->hwnd = hWnd;
	    SetWindowLong(hWnd, 0, (LONG) CI);
          GetClientRect(hWnd,&rc);
	    CI->xClientView = rc.bottom - rc.top + 1;
	    CI->yClientView = rc.right - rc.left + 1;

	//    if (!CI->noscrollx) {
//		SetScrollPos(hWnd, SB_HORZ, CI->poshorzscroll, TRUE);
//		SetScrollRange(hWnd, SB_HORZ, 0, CI->scrollwidth, FALSE);
	//    }
            sinf.cbSize    = sizeof(SCROLLINFO);
            sinf.fMask     = SIF_PAGE | SIF_POS | SIF_RANGE | SIF_DISABLENOSCROLL;
            sinf.nMin      = 0;
            sinf.nMax      = SCROLLLINES;
            sinf.nPage     = SCROLLLINES; //CI->scrolllines+1;
            sinf.nPos      = 0;
            sinf.nTrackPos = 0;
            SetScrollInfo(hWnd, SB_VERT, &sinf, TRUE);

	//    SetScrollPos(hWnd, SB_VERT, CI->posvertscroll, TRUE);
	  //  SetScrollRange(hWnd, SB_VERT, 0, CI->scrolllines + 1, TRUE);
	}
	return (DefWindowProc(hWnd, messg, wParam, lParam));

    case WM_SETFOCUS:
	CI = (CONSOLESTRUCT*) GetWindowLong(hWnd, 0);
	if (!CI->no_caret) {
	    CreateCaret(hWnd, NULL, CI->charwt, 2);
	    SetCaretPos(CI->charwt, CI->charht);
	    SetCaretPos(-100, -100);
	    ShowCaret(hWnd);
	}
	break;

    case WM_KILLFOCUS:
	CI = (CONSOLESTRUCT*) GetWindowLong(hWnd, 0);
	if (!CI->no_caret)
	    DestroyCaret();
	break;

    case WM_SIZE:
	{
	    CI = (CONSOLESTRUCT*) GetWindowLong(hWnd, 0);
	    if (CI->xClientView != LOWORD(lParam))
		CI->resized = TRUE;
	    CI->xClientView = LOWORD(lParam);
	    CI->yClientView = HIWORD(lParam);
	    if (CI->noscrollx)
		CI->scrollwidth = CI->xClientView / CI->charwt;
	    else {
		CI->scrollwidth = CI->maxchar - LOWORD(lParam) /
		    CI->charwt;
		SetScrollRange(hWnd, SB_HORZ, 0, CI->scrollwidth, FALSE);
	    }
	    compute_scroll_x(CI);
	    reformat_line(CI);
	    compute_scroll_y(CI);
	    if (CI->isinput)
		InvalidateRect(hWnd, NULL, TRUE);
	}
	break;

    case WM_HSCROLL:
	CI = (CONSOLESTRUCT*) GetWindowLong(hWnd, 0);
	if (CI->noscrollx)
	    break;
	else
	    switch (LOWORD(wParam)) {
	    case SB_LINEDOWN:
		CI->poshorzscroll += 1;
		break;
	    case SB_LINEUP:
		CI->poshorzscroll -= 1;
		break;
	    case SB_PAGEDOWN:
		CI->poshorzscroll +=
		    CI->xClientView /
		    CI->charwt;
		break;
	    case SB_PAGEUP:
		CI->poshorzscroll -=
		    CI->xClientView /
		    CI->charwt;
		break;
	    case SB_THUMBTRACK:
//	    case SB_THUMBPOSITION:
		CI->poshorzscroll = HIWORD(wParam);
		break;
	    default:
		break;
	    }
	if (CI->poshorzscroll > CI->scrollwidth)
	    CI->poshorzscroll = CI->scrollwidth;
	if (CI->poshorzscroll < 0)
	    CI->poshorzscroll = 0;
	if (CI->poshorzscroll !=
	    GetScrollPos(hWnd, SB_HORZ)) {
	    SetScrollPos(hWnd, SB_HORZ, CI->poshorzscroll, TRUE);
	    InvalidateRect(hWnd, NULL, TRUE);
	}
	break;
 
  //  case WM_CHAR:
  //      if ((TCHAR)wParam == 'c' && 
  //              (GetKeyState(VK_CONTROL) & 0xFFFF)) g_bell();
           // selection_to_clipboard(CI);
  //      return (DefWindowProc(hWnd, messg, wParam, lParam));

    case WM_VSCROLL:
	CI = (CONSOLESTRUCT*) GetWindowLong(hWnd, 0);
	switch (LOWORD(wParam)) {
	case SB_LINEDOWN:
	    CI->posvertscroll += 1;
	    break;
	case SB_LINEUP:
	    CI->posvertscroll -= 1;
	    break;
	case SB_PAGEDOWN:
	    CI->posvertscroll +=
		CI->yClientView /
		CI->charht;
	    break;
	case SB_PAGEUP:
	    CI->posvertscroll -=
		CI->yClientView /
		CI->charht;
	    break;
       case SB_THUMBTRACK:
//	case SB_THUMBPOSITION:
	    CI->posvertscroll = HIWORD(wParam); //LOWORD(lParam);
	    break;
	default:
	    break;
	}
	if (CI->posvertscroll > CI->scrolllines)
	    CI->posvertscroll = CI->scrolllines;
	if (CI->posvertscroll < 0)
	    CI->posvertscroll = 0;

       sinf.cbSize = sizeof(sinf);
       sinf.fMask  = SIF_POS;
       GetScrollInfo(hWnd,SB_VERT,&sinf); 

       if (CI->posvertscroll != sinf.nPos){
//	    GetScrollPos(hWnd, SB_VERT)) {
//         SetScrollPos(hWnd, SB_VERT, CI->posvertscroll, TRUE);
           sinf.nPos = CI->posvertscroll;
           SetScrollInfo(hWnd,SB_VERT,&sinf,TRUE);
	    InvalidateRect(hWnd, NULL, TRUE);
	}
	break;

    case WM_KEYDOWN:
	CI = (CONSOLESTRUCT*) GetWindowLong(hWnd, 0);
	switch (LOWORD(wParam)) {
	case VK_PRIOR:
	    SendMessage(hWnd, WM_VSCROLL, SB_PAGEUP, 0);
	    break;

	case VK_NEXT:
	    SendMessage(hWnd, WM_VSCROLL, SB_PAGEDOWN, 0);
	    break;

	case VK_UP:
	    SendMessage(hWnd, WM_VSCROLL, SB_LINEUP, 0);
	    break;

	case VK_DOWN:
	    SendMessage(hWnd, WM_VSCROLL, SB_LINEDOWN, 0);
	    break;

	case VK_LEFT:
	    SendMessage(hWnd, WM_HSCROLL, SB_LINEUP, 0);
	    break;

	case VK_RIGHT:
	    SendMessage(hWnd, WM_HSCROLL, SB_LINEDOWN, 0);
	    break;

	}
	break;

    case WM_MOUSEMOVE: {
        int x1select,y1select;
      if (!(wParam & MK_LBUTTON))
          break;
	CI = (CONSOLESTRUCT*) GetWindowLong(hWnd, 0); 
            CI->ptsCursor = MAKEPOINTS(lParam); 
           CI->x2select = (int)(CI->ptsCursor.x / CI->charwt);
            CI->y2select = min((int)(CI->ptsCursor.y / CI->charht), 
                CI->linecount)+CI->posvertscroll; 
            CI->x2select = min(CI->x2select, strlen(CI->textline[CI->y2select]));
            if ((CI->y1select > CI->y2select)||
                ((CI->y1select == CI->y2select)&&
                 (CI->x1select > CI->x2select))) {
                x1select = CI->x2select;
                CI->x2select = CI->x1select;
                CI->x1select = x1select;
                y1select = CI->y2select;
                CI->y2select = CI->y1select;
                CI->y1select = y1select;
            }
            if (CI->x1select != CI->x2select || 
                 CI->y1select != CI->y2select) {
           //   g_cons_printf("cur line 2 = %d,%d : %d,%d\n",
            //       CI->x1select,CI->y1select,CI->x2select,CI->y2select);
              CI->isselect = TRUE;
                InvalidateRect(hWnd, NULL, TRUE);
            }
        }
        break;

    case WM_LBUTTONUP: {
        int x1select,y1select;

	CI = (CONSOLESTRUCT*) GetWindowLong(hWnd, 0); 
            CI->ptsCursor = MAKEPOINTS(lParam); 
 
            CI->x2select = (int)(CI->ptsCursor.x / CI->charwt);
            CI->y2select = min((int)(CI->ptsCursor.y / CI->charht), 
                CI->linecount)+CI->posvertscroll; 
            CI->x2select = min(CI->x2select, strlen(CI->textline[CI->y2select]));
            if ((CI->y1select > CI->y2select)||
                ((CI->y1select == CI->y2select)&&
                 (CI->x1select > CI->x2select))) {
                x1select = CI->x2select;
                CI->x2select = CI->x1select;
                CI->x1select = x1select;
                y1select = CI->y2select;
                CI->y2select = CI->y1select;
                CI->y1select = y1select;
            }
            if (CI->x1select != CI->x2select || 
                 CI->y1select != CI->y2select) {
           //   g_cons_printf("cur line 2 = %d,%d : %d,%d\n",
            //       CI->x1select,CI->y1select,CI->x2select,CI->y2select);
              CI->isselect = TRUE;
                InvalidateRect(hWnd, NULL, TRUE);
            }
        }
        break;

    case WM_LBUTTONDOWN: {

      if (wParam & MK_SHIFT)
          break;
	CI = (CONSOLESTRUCT*) GetWindowLong(hWnd, 0); 
            /* Save the current mouse-cursor coordinates. */ 
            if (CI->isselect){
                CI->isselect = FALSE;
                InvalidateRect(hWnd, NULL, TRUE);
            }
            CI->ptsCursor = MAKEPOINTS(lParam); 
 
            CI->x1select = (int)(CI->ptsCursor.x / CI->charwt);
            CI->y1select = min((int)(CI->ptsCursor.y / CI->charht), 
                CI->linecount)+CI->posvertscroll; 
            CI->x1select = min(CI->x1select, strlen(CI->textline[CI->y1select]));

  //g_cons_printf("cur line=%d,%d\n",CI->x1select,CI->y1select);
            }
            break;

     case WM_PAINT:
	CI = (CONSOLESTRUCT*) GetWindowLong(hWnd, 0);
	hdc = BeginPaint(hWnd, &ps);

 //               SetBkMode(cons_hdc, OPAQUE);

	update_console(hdc, CI);
	ValidateRect(hWnd, NULL);
	EndPaint(hWnd, &ps);
	break;

    case WM_DESTROY: {
      HDC hdc;
	CI = (CONSOLESTRUCT*) GetWindowLong(hWnd, 0);
      hdc = GetDC(hWnd);
      SelectObject(hdc, CI->oldfont);
	DeleteObject(CI->font);
      ReleaseDC(hWnd, hdc);
	for (i = 0; i < SCROLLLINES; i++)
	    free(CI->textline[i]);
	free(CI->textbf);
	free(CI->textbf2);
	free(CI);
	PostQuitMessage(0);
       cwin = 0;
       }
	break;

    case WM_COMMAND: 
        CI = (CONSOLESTRUCT*) GetWindowLong(hWnd, 0);
        switch (wParam) { 
            case ID_COPY: 
                selection_to_clipboard(CI);
                break;

            case ID_EXIT:
                g_close_console();
                break;

        }
        break;

    case WM_USER: {
	HWND hwnd_parent;
	int curr_win;
	if ((hwnd_parent = GetParent(hWnd)) == NULL)
	    return 0;
	curr_win = GetWindowLong(hwnd_parent, 0);
	switch (wParam) {
//	case ID_KILL_CROSSHAIR:
//	    mswin_kill_crosshair(lParam);
//	    break;

	case ID_FOCUS:
	    SetFocus(hWnd);
	    break;

	case ID_RESIZE:{
		int x0, y0, y1, w, h;
		InvalidateRect(hWnd, NULL, TRUE);
		x0 = 0;
		for (i = 0, y0 = 0; i < C_DRAWAREA; i++)
		    y0 += WI[curr_win].child[i].height;
		for (i = C_DRAWAREA + 1, y1 = 0; i < MAXWCHILDREN; i++)
		    y1 += WI[curr_win].child[i].height;
		w = LOWORD(lParam) - 1;
		h = HIWORD(lParam) - 1 - y0 - y1;
		WI[curr_win].child[C_DRAWAREA].height = h;
		MoveWindow(WI[curr_win].child[C_DRAWAREA].hwnd,
			   x0,
			   y0,
			   w,
			   h,
			   TRUE);

		G_WIN_WIDTH(curr_win) = w;
		G_WIN_HEIGHT(curr_win) = h;
		break;
	    }
	}
	}
	break;

    default:
	return (DefWindowProc(hWnd, messg, wParam, lParam));
    }
    return (0L);
}

void g_open_console(long x0, long y0, long width, long height, char *title)
{
    DWORD style;
    static int ClassRegistered;
    WNDCLASS wndclass;
    HMENU hmenu,hmenu1,hmenu2;

    /*
     * Only one console/output window
     */
    if (cwin)
	return;
    if (!ClassRegistered) {
	wndclass.style = CS_HREDRAW | CS_VREDRAW;
	wndclass.lpfnWndProc = ConsoleProc;
	wndclass.cbClsExtra = 0;
	wndclass.cbWndExtra = sizeof(LONG);
	wndclass.hInstance = _hInst;
	wndclass.hIcon = LoadIcon(_hInst, MAKEINTRESOURCE(ICON_ID));
	wndclass.hCursor = LoadCursor(NULL, IDC_ARROW);
	wndclass.hbrBackground = GetStockObject(WHITE_BRUSH);
	wndclass.lpszMenuName = NULL;
	wndclass.lpszClassName = CONSOLECLASS;
	RegisterClass(&wndclass);
	ClassRegistered = TRUE;
    }



    style = WS_OVERLAPPEDWINDOW | WS_THICKFRAME | WS_CAPTION ;
	// WS_VSCROLL;
style |= SBS_VERT;
//    if (!CI->noscrollx)
//	style |= WS_HSCROLL;
    width += 2 +
	2 * GetSystemMetrics(SM_CXFRAME) -
	GetSystemMetrics(SM_CXBORDER);
    height += 2 +
	GetSystemMetrics(SM_CYCAPTION) +
	2 * GetSystemMetrics(SM_CYFRAME) -
	2 * GetSystemMetrics(SM_CYBORDER);

    hmenu = CreateMenu();
    hmenu1 = CreatePopupMenu();
    hmenu2 = CreatePopupMenu();
    AppendMenu(hmenu, MF_POPUP, (UINT) hmenu1, "&File");
    AppendMenu(hmenu, MF_POPUP, (UINT) hmenu2, "&Edit");
    AppendMenu(hmenu1, MF_STRING, ID_EXIT, "E&xit");
    AppendMenu(hmenu2, MF_STRING, ID_COPY, "&Copy");
//    AppendMenu(hmenu2, MF_STRING, ID_COPY, "&Copy\tCtrl+C");
    cwin = CreateWindow(CONSOLECLASS,
			title,
			style,
			(int) x0,
			(int) y0,
			(int) width,
			(int) height,
			(HWND) NULL,
			(HMENU) hmenu,
			(HANDLE) _hInst,
			(LPSTR) NULL);
    
    ShowWindow(cwin, SW_SHOW);
    UpdateWindow(cwin);
    INTERNAL_dispatch_message();
}

void g_close_console(void)
{
    int i;

    if (cwin == 0)
        return;
    INTERNAL_dispatch_message();
    DestroyWindow(cwin);
}

/*-------------------------------------------------------------------*/
/*                             CONSOLE                               */
/*-------------------------------------------------------------------*/
static int empty_lines(CONSOLESTRUCT *CI)
{
    int ypos, count;

    for (count = 0, ypos = CI->ypos; ypos != CI->lastline; count++)
	(ypos == SCROLLLINES - 1) ? (ypos = 0) : (ypos++);
    return count;
}

static void next_line(CONSOLESTRUCT *CI)
{
    int same_ypos;

    (CI->ypos == CI->lastline) ? (same_ypos = TRUE) : (same_ypos = FALSE);
    if (CI->linecount < SCROLLLINES)
	CI->linecount++;
    (CI->ypos == SCROLLLINES - 1) ? (CI->ypos = 0) : (CI->ypos++);
    if (same_ypos)
	CI->lastline = CI->ypos;
    CI->textline[CI->ypos][0] = '\0';
    CI->xoff = CI->xpos = 0;
    CI->yoff++;
}

static void prev_line(CONSOLESTRUCT *CI)
{
    CI->yoff--;
    (CI->yoff) ? (CI->xoff = 0) : (CI->xoff = CI->old_xoff);
    if (CI->linecount < SCROLLLINES)
	CI->linecount--;
    (CI->ypos == 0) ? (CI->ypos = SCROLLLINES - 1) : (CI->ypos--);
}

#define EMPTY_LINE 1
static int compute_scroll_y(CONSOLESTRUCT *CI)
{
    int height;
    SCROLLINFO sinf;
    int yMax, yPos, nLines;

    // window height
    height = CI->yClientView / CI->charht;
    // total number of lines, but never more that SCROLLLINES
    nLines = CI->linecount + EMPTY_LINE;   // + empty_lines(CI);
    nLines = min(nLines, SCROLLLINES);
    // lines NOT visible
    CI->scrolllines = nLines - height;
    CI->scrolllines = max(CI->scrolllines, 0);

    yMax = max(height, nLines );  
    yPos = min(CI->posvertscroll, yMax);

    sinf.cbSize = sizeof(sinf);
    sinf.fMask  = SIF_POS | SIF_PAGE | SIF_RANGE;
    GetScrollInfo(CI->hwnd,SB_VERT,&sinf); 

 //   GetScrollRange(CI->hwnd, SB_VERT, &x, &y);
 //   if (CI->scrolllines && y != sinf.nPage)
//	SetScrollRange(CI->hwnd, SB_VERT, 0, CI->scrolllines, FALSE);

    CI->posvertscroll = CI->scrolllines;

    if (CI->posvertscroll != sinf.nPos || 
            CI->scrolllines != sinf.nPage) {
//	 SetScrollPos(CI->hwnd, SB_VERT, CI->posvertscroll, TRUE);
       sinf.nMin   = 0;
       sinf.nMax   = yMax;
       sinf.nPage  = height + 1;
       sinf.nPos   = CI->posvertscroll;
       SetScrollInfo(CI->hwnd,SB_VERT,&sinf,TRUE);

    }
    return CI->scrolllines;
}

static int compute_scroll_x(CONSOLESTRUCT *CI)
{
    int width, xpos;

    if (CI->noscrollx)
	return FALSE;

    width = CI->xClientView / CI->charwt;
    xpos = strlen(CI->textline[CI->ypos]) - width;

    if (width >= CI->maxchar && CI->poshorzscroll == 0)
	return FALSE;

    if (CI->poshorzscroll > xpos &&
	CI->poshorzscroll < xpos + width &&
	CI->poshorzscroll <= CI->scrollwidth)
	return FALSE;

    CI->poshorzscroll = xpos + XSCROLLJUMP;

    if (CI->poshorzscroll > CI->scrollwidth)
	CI->poshorzscroll = CI->scrollwidth;
    if (CI->poshorzscroll < 0)
	CI->poshorzscroll = 0;
    if (CI->poshorzscroll !=
	GetScrollPos(CI->hwnd, SB_HORZ))
	SetScrollPos(CI->hwnd, SB_HORZ, CI->poshorzscroll, TRUE);
    return TRUE;
}

static void reformat_line(CONSOLESTRUCT *CI)
{
    int ln, i;

    if (CI->isinput && CI->resized && CI->noscrollx) {
	CI->resized = FALSE;
	ln = CI->ypos = CI->old_ypos;
	if (CI->linecount != SCROLLLINES)
	    CI->linecount = ln;
	strcpy(CI->textbf, CI->textline[ln]);
	CI->textline[ln][0] = '\0';
	for (i = 0; i < CI->yoff; i++) {
	    (ln == SCROLLLINES - 1) ? (ln = 0) : (ln++);
	    strcat(CI->textbf, CI->textline[ln]);
	    CI->textline[ln][0] = '\0';
	}
	write_console(CI, CI->textbf);
	(CI->yoff) ? (CI->xoff = 0) : (CI->xoff = CI->old_xoff);
    }

}


static int selection_to_clipboard(CONSOLESTRUCT *CI)
{
    LPTSTR  lptstrCopy; 
    HGLOBAL hglbCopy; 
    int i, numlines,ypos,ipos; 
    int y1select, len;
 
    if (!CI->isselect) 
        return FALSE; 
    if (!OpenClipboard(CI->hwnd)) 
        return FALSE; 
    EmptyClipboard(); 

    if (CI->linecount >= SCROLLLINES) {
        y1select = CI->y1select + CI->lastline + 1;
        if (y1select >= SCROLLLINES)
            y1select -= SCROLLLINES;
    }
    else {
        y1select = CI->y1select;
    }
    numlines  = CI->y2select - CI->y1select + 1;

    ipos  = y1select;
    len   = 0;
    for (i=0; i < numlines; i++, ipos++){
        if (ipos >= SCROLLLINES)
            ipos = 0;
          
        len += strlen(CI->textline[ipos]) + 2;
    }

    hglbCopy = GlobalAlloc(GMEM_DDESHARE, 
            (len + 1) * sizeof(TCHAR)); 
    if (hglbCopy == NULL) { 
        CloseClipboard(); 
        return FALSE; 
    } 
    lptstrCopy = GlobalLock(hglbCopy); 

    ipos  = y1select;
    lptstrCopy[0] = '\0';
    for (i=0, ypos = CI->y1select; i < numlines; i++, ypos++, ipos++){
        if (ipos >= SCROLLLINES)
            ipos = 0;
          
        if (ypos == CI->y1select && CI->x1select > 0) 
            strcat(lptstrCopy, CI->textline[ipos] + CI->x1select);
        else
            strcat(lptstrCopy, CI->textline[ipos]);
        if (ypos == CI->y2select) {  
            len = strlen(CI->textline[ipos]);
            if (CI->x2select <= len) {
                int lentot = strlen(lptstrCopy);
                lptstrCopy[lentot-len+CI->x2select] = (TCHAR) 0;
            }
        }
        else
            strcat(lptstrCopy, "\r\n");
    }

    GlobalUnlock(hglbCopy); 
    SetClipboardData(CF_TEXT, hglbCopy);
    CloseClipboard(); 
 
    return TRUE; 
} 
 


static void update_console(HDC hdc, CONSOLESTRUCT *CI)
{
    int ypos, xpos;
    int caretx, carety;
    HFONT oldfont;
    COLORREF oldcol, oldbk;
    int start_ipos, ipos, numlines, screenlines;

    oldfont = SelectObject(hdc, CI->font);
    SetTextAlign(hdc, TA_LEFT | TA_TOP);
    oldcol = GetTextColor(hdc);
    oldbk  = GetBkColor(hdc);

    screenlines = CI->yClientView / CI->charht -1;
    start_ipos = CI->lastline - screenlines - CI->scrolllines + CI->posvertscroll;
    if (CI->linecount >= SCROLLLINES) {
        if (start_ipos < 0)
            start_ipos += SCROLLLINES;
        if (start_ipos >= SCROLLLINES)
            start_ipos = 0;
        numlines  = screenlines;
    }
    else {
        if (start_ipos < 0)
            start_ipos = 0;
        numlines  = min(CI->lastline,screenlines);
    }
    ipos  = start_ipos;
    for (ypos = 0,xpos=0; ypos <= numlines; ypos++, ipos++){
          int xoff=0;
          int itest = ypos + CI->posvertscroll;
          if (ipos >= SCROLLLINES)
              ipos = 0;
          if (CI->isselect) {
              int len = strlen(CI->textline[ipos]);
              if (itest == CI->y1select && CI->x1select > 0) {
                   SetTextColor(hdc, oldcol);
                   SetBkColor(hdc,oldbk);
	             TextOut(hdc, CI->charwt * (xoff+xpos),
		         CI->charht * ypos,
		         CI->textline[ipos], CI->x1select);
                   xoff = CI->x1select;
              }
              if (itest == CI->y2select && CI->x2select <= len) {
                   SetTextColor(hdc, oldbk);
                   SetBkColor(hdc,oldcol);
	             TextOut(hdc, CI->charwt * (xoff+xpos),
		         CI->charht * ypos,
		         CI->textline[ipos]+xoff, CI->x2select-xoff);
                   xoff = CI->x2select;
              }
              if (itest>=CI->y1select && itest<CI->y2select){
                   SetTextColor(hdc, oldbk);
                   SetBkColor(hdc,oldcol);
              }
              else {
                   SetTextColor(hdc, oldcol);
                   SetBkColor(hdc,oldbk);
              }
          }
#ifdef bbb
sprintf(CI->textline[ipos],
 "ipos=%d,%d ypos=%d,lastline=%d,verts=%d,numl=%d,srceen=%d",
ipos,start_ipos,CI->ypos,CI->lastline, CI->posvertscroll,numlines,
screenlines);
sprintf(CI->textline[ipos]+20,"[%d]",ipos);
#endif
          TextOut(hdc, 
                  CI->charwt * (xoff+xpos),
	            CI->charht * ypos,
		     CI->textline[ipos]+xoff, 
                  strlen(CI->textline[ipos])-xoff);
    }
    if (!CI->no_caret) {
       // ypos -= empty_lines(CI);
        caretx = CI->charwt * (strlen(CI->textline[CI->ypos]) -
				CI->poshorzscroll);
      //  carety = CI->charht * (ypos - CI->posvertscroll);
        carety = CI->charht * ypos;
        SetCaretPos(caretx, carety);
    }
    SetTextColor(hdc, oldcol);
    SetBkColor(hdc, oldbk);
    SelectObject(hdc, oldfont);
}

static int write_console(CONSOLESTRUCT *CI, const char *buffer)
{
    int i;
    int scrollok = FALSE, wrap;

    CI->isoutput = TRUE;
    CI->yoff = 0;
    CI->xpos = strlen(CI->textline[CI->ypos]);
    if (!CI->isinput)
	INTERNAL_dispatch_message();
    for (i = 0; buffer[i]; i++) {
	switch (buffer[i]) {

	case '\r':
          if (buffer[i+1] != '\n')
              CI->xpos = 0;
	    break;

	case '\n':
	    CI->textline[CI->ypos][CI->xpos] = '\0';
	    next_line(CI);
	    scrollok = TRUE;
           if (CI->linecount >= SCROLLLINES) {
               CI->y1select--;
               CI->y2select--;
               if (CI->y2select<0) {
                   CI->isselect=FALSE;
               } 
               else if (CI->y1select<0) {
                   CI->y1select=0;
                   CI->x1select=0;
               }
           }
	    break;

	default:
	    CI->textline[CI->ypos][CI->xpos] = buffer[i];
	    CI->xpos++;
	    if (CI->noscrollx)
		wrap = (CI->xpos >= CI->scrollwidth);
	    else
		wrap = (CI->xpos >= CI->maxchar);
	    if (wrap) {
		CI->textline[CI->ypos][CI->xpos] = '\0';
		next_line(CI);
		scrollok = TRUE;
	    }
	    break;
	}
    }
    CI->textline[CI->ypos][CI->xpos] = '\0';

    if (!CI->isinput) {
	if (compute_scroll_y(CI) && scrollok)
	    InvalidateRect(CI->hwnd, NULL, TRUE);
	else
	    InvalidateRect(CI->hwnd, NULL, FALSE);
	INTERNAL_dispatch_message();
    }
    CI->isoutput = FALSE;
    return strlen(buffer);
}

static int read_console(CONSOLESTRUCT *CI)
{
    int ok = FALSE, wrap;
    G_USERINPUT ui;
    int i, ln, do_scroll;

    CI->isinput = TRUE;
    g_flush();
    CI->old_xoff = CI->xoff = CI->xpos = strlen(CI->textline[CI->ypos]);
    CI->yoff = 0;
    CI->old_ypos = CI->ypos;
    CI->resized = FALSE;
    INTERNAL_dispatch_message();
    do {
	INTERNAL_dispatch_message();
	do_scroll = FALSE;
	if (INTERNAL_is_event()) {
	    memcpy(&ui, INTERNAL_get_event(), sizeof(G_USERINPUT));
	    if (ui.event == G_KEYPRESS) {
		INTERNAL_dispatch_message();

		switch (ui.keycode) {
		case 0:
		    break;

		case '\b':
		    if (CI->xpos == CI->xoff && CI->yoff > 0) {
			if (CI->noscrollx)
			    CI->xpos = CI->scrollwidth;
			else
			    CI->xpos = CI->maxchar;
			prev_line(CI);
			if (compute_scroll_x(CI))
			    do_scroll = TRUE;
			if (compute_scroll_y(CI))
			    do_scroll = TRUE;
		    }
		    if (CI->xpos > CI->xoff) {
			CI->xpos--;
			CI->textline[CI->ypos][CI->xpos] = '\0';
			if (compute_scroll_x(CI))
			    do_scroll = TRUE;
			if (!do_scroll) {
			    RECT rect;
			    rect.left = CI->charwt *
				(CI->xpos - CI->poshorzscroll);
			    rect.right = rect.left + CI->charwt;
			    if (CI->linecount >= SCROLLLINES)
				rect.top = CI->charht *
				    (SCROLLLINES - CI->posvertscroll
				     - empty_lines(CI) - 1);
			    else
				rect.top = CI->charht *
				    (CI->ypos - CI->posvertscroll);
			    rect.bottom = rect.top + CI->charht;
			    InvalidateRect(CI->hwnd, &rect, TRUE);
			}
		    }
		    if (do_scroll)
			InvalidateRect(CI->hwnd, NULL, TRUE);
		    break;

		case '\r':
		    CI->textline[CI->ypos][CI->xpos] = '\0';
		    next_line(CI);
		    ok = TRUE;
		    compute_scroll_x(CI);
		    if (compute_scroll_y(CI))
			InvalidateRect(CI->hwnd, NULL, TRUE);
		    break;

		default:
		    if (CI->yoff == SCROLLLINES - 1)
			break;
		    CI->textline[CI->ypos][CI->xpos] = ui.keycode;
		    CI->textline[CI->ypos][CI->xpos + 1] = '\0';
		    CI->xpos++;
		    if (CI->noscrollx)
			wrap = (CI->xpos >= CI->scrollwidth);
		    else
			wrap = (CI->xpos >= CI->maxchar);
		    if (wrap) {
			next_line(CI);
			if (compute_scroll_y(CI))
			    do_scroll = TRUE;
		    }
		    if (compute_scroll_x(CI))
			do_scroll = TRUE;
		    if (do_scroll)
			InvalidateRect(CI->hwnd, NULL, TRUE);
		    else
			InvalidateRect(CI->hwnd, NULL, FALSE);
		    break;
		}
	    }
	}
    } while (!ok);

    ln = CI->old_ypos;
    strcpy(CI->textbf, CI->textline[ln] + CI->old_xoff);
    for (i = 0; i < CI->yoff; i++) {
	(ln == SCROLLLINES - 1) ? (ln = 0) : (ln++);
	strncat(CI->textbf, CI->textline[ln],
		TEXTBFSIZE - strlen(CI->textbf));
    }
    CI->isinput = FALSE;
    return strlen(CI->textbf);
}

/**********************
* Console IO
*/


int g_cons_puts(const char *string)
{
    CONSOLESTRUCT *CI;

    if (!cwin)
	return EOF;
    CI = (CONSOLESTRUCT*) GetWindowLong(cwin, 0);
    write_console(CI,string);
    write_console(CI,"\n");
    return '\n';
}

int g_cons_fputs(const char *string, FILE * file)
{
    int ret;
    CONSOLESTRUCT *CI;

    if (file != stdout && file != stderr)
	return fputs(string, file);
    if (!cwin)
	return EOF;
    CI = (CONSOLESTRUCT*) GetWindowLong(cwin, 0);
    ret = write_console(CI,string);
    ret = max(ret - 1, 0);
    return string[ret];
}

int g_cons_fputc(char c, FILE * file)
{
    char bf[2];
    CONSOLESTRUCT *CI;

    if (file != stdout && file != stderr)
	return fputc(c, file);
    if (!cwin)
	return EOF;
    CI = (CONSOLESTRUCT*) GetWindowLong(cwin, 0);
    bf[0] = c;
    bf[1] = '\0';
    write_console(CI,bf);
    return (int) c;
}


int g_cons_printf(const char *format,...)
{
    va_list argp;
    int ret_val;
    CONSOLESTRUCT *CI;

    if (!cwin)
	return EOF;
    CI = (CONSOLESTRUCT*) GetWindowLong(cwin, 0);
    INTERNAL_dispatch_message();
    va_start(argp, format);
    ret_val = vsprintf(CI->textbf2, format, argp);
    va_end(argp);
    write_console(CI, CI->textbf2);
    return ret_val;
}


int g_cons_fprintf(FILE * file, const char *format,...)
{
    va_list argp;
    int ret_val;
    CONSOLESTRUCT *CI;

    if (!cwin)
	return EOF;
    CI = (CONSOLESTRUCT*) GetWindowLong(cwin, 0);
    INTERNAL_dispatch_message();
    va_start(argp, format);
    if (file == stdout || file == stderr) {
	ret_val = vsprintf(CI->textbf2, format, argp);
	write_console(CI,CI->textbf2);
    }
    else
	ret_val = vfprintf(file, format, argp);
    va_end(argp);
    return ret_val;
}

char *g_cons_gets(char *buffer)
{
    CONSOLESTRUCT *CI;

    if (!cwin)
	return NULL;
    CI = (CONSOLESTRUCT*) GetWindowLong(cwin, 0);
    read_console(CI);
    strcpy(buffer, CI->textbf);
    return buffer;
}

char *g_cons_fgets(char *string, int maxchar, FILE * file)
{
    int i;
    CONSOLESTRUCT *CI;

    if (file != stdin)
	return fgets(string, maxchar, file);
    if (!cwin)
	return NULL;
    CI = (CONSOLESTRUCT*) GetWindowLong(cwin, 0);
    i = read_console(CI);
    if (i < maxchar) {
	strcat(CI->textbf, "\n");
	i++;
    }
    else
	i = maxchar;
    CI->textbf[i] = '\0';
    strcpy(string, CI->textbf);
    return string;
}

int g_cons_fgetc(FILE * file)
{
    CONSOLESTRUCT *CI;

    if (file != stdin)
	return fgetc(file);
    if (!cwin)
	return EOF;
    CI = (CONSOLESTRUCT*) GetWindowLong(cwin, 0);
    read_console(CI);
    return (CI->textbf[0]);
}

/************************************************************************/
/* CONSOLE STUFF
*/
#define INTERNALCONSOLECLASS   "InternalConsole"
HWND CreateConsole(HWND parent, int x, int y, int width, int height,
		       int id, HINSTANCE hInstance)
{
    DWORD style;
    static int ClassRegistered;
    WNDCLASS wndclass;
    HWND hwnd;
    CONSOLESTRUCT *CI;

    if (!ClassRegistered) {
	wndclass.style = CS_HREDRAW | CS_VREDRAW;
	wndclass.lpfnWndProc = ConsoleProc;
	wndclass.cbClsExtra = 0;
	wndclass.cbWndExtra = sizeof(LONG);
	wndclass.hInstance = _hInst;
	wndclass.hIcon = LoadIcon(NULL, IDI_APPLICATION);
	wndclass.hCursor = LoadCursor(NULL, IDC_ARROW);
	wndclass.hbrBackground = GetStockObject(WHITE_BRUSH);
	wndclass.lpszMenuName = NULL;
	wndclass.lpszClassName = INTERNALCONSOLECLASS;
	RegisterClass(&wndclass);
	ClassRegistered = TRUE;
    }

    style = WS_CHILD | WS_VISIBLE | WS_VSCROLL;

    hwnd = CreateWindow(INTERNALCONSOLECLASS,
			"",
			style,
			x,
			y,
			width,
			height,
			parent,
			(HMENU) id,
			(HANDLE) hInstance,
			(LPSTR) NULL);

    CI = (CONSOLESTRUCT*) GetWindowLong(hwnd, 0);
    CI->no_caret = TRUE;
    return (hwnd);
}

int g_SCREEN_console_printf(int win_id, const char *format,...)
{
    va_list argp;
    int ret_val;
    int id, device;
    CONSOLESTRUCT *CI;

    id = GET_LOCAL_ID(win_id);
    device = GET_DEVICE(win_id);
    if (id < 0 || id >= G_MAXWINDOW)
	return 0;
    if (device != G_SCREEN)
	return 0;
    CI = (CONSOLESTRUCT*) GetWindowLong(WI[id].child[C_DRAWAREA].hwnd, 0);
    INTERNAL_dispatch_message();
    va_start(argp, format);
    ret_val = vsprintf(CI->textbf2, format, argp);
    va_end(argp);
    write_console(CI, CI->textbf2);
    return ret_val;
}


