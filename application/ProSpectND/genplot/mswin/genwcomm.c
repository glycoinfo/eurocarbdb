/************************************************************************/
/*                               genwcomm.c                             */
/*                                                                      */
/*  Platform : Microsoft Windows                                        */
/*  Module   : Genplot Commandline functions                            */
/*                                                                      */
/*  Albert van Kuik                                                     */
/************************************************************************/
#include <stdio.h>
#include <windows.h>
#include <commctrl.h>
#include <string.h>
#include <stdarg.h>
#include <stdlib.h>
#include <math.h>
#include <time.h>
#include <assert.h>
#include "genplot.h"
#include "g_inter.h"
#include "genwin.h"

static void add_command(char *command);
static char *get_commandline(void);
static int is_command(void);
static HWND command_widget[G_MAXWINDOW];
static int q_locked[G_MAXWINDOW];

LOGFONT *get_statusfont()
{
    static NONCLIENTMETRICS ncm;
    ncm.cbSize = sizeof(NONCLIENTMETRICS);

    if (SystemParametersInfo(SPI_GETNONCLIENTMETRICS,
          sizeof(NONCLIENTMETRICS) , &ncm, 0)) {
        return &(ncm.lfStatusFont);
    }
    return NULL;
}


/* doublure */
static void GetLabelSize(HWND hWnd, SIZE * size, char *label)
{
    HDC hdc;

    hdc = GetDC(hWnd);
    GetTextExtentPoint32(hdc, label, strlen(label), size);
    ReleaseDC(hWnd, hdc);
}

/************************************************************************/
/* COMMAND WIDGET STUFF
 */

LRESULT CALLBACK CommandProc(HWND, UINT, WPARAM, LPARAM);


#define ID_ADDCOMMAND 		600
#define ID_SETCOMMAND 		601
#define ID_SETCOMMANDPROMPT	602
#define ID_SETCOMMANDHISTORY	603
#define ID_SETENABLE 		604
#define ID_SETLOCK	 	605
#define ID_INSTALL_LOCK		606



WNDPROC lpfnEditWndProc;

/******************************************************** 
 
    FUNCTION:   SubClassProc 
 
    PURPOSE:    Process TAB and ESCAPE keys, and pass all 
                other messages to the class window 
                procedure. 
 
*********************************************************/ 
 
LRESULT CALLBACK SubClassProc(hwnd, msg, wParam, lParam) 
HWND hwnd; 
UINT msg; 
WPARAM wParam; 
LPARAM lParam; 
{ 
 
    switch (msg) { 
case WM_SETFOCUS:
 
//g_bell();
break;
        case WM_KEYDOWN: 

            switch (wParam) { 
/*
                case VK_TAB: 
                    SendMessage(hwndToolbar, WM_TAB, 0, 0); 
                    return 0; 
                case VK_ESCAPE: 
                    SendMessage(hwndToolbar, WM_ESC, 0, 0); 
                    return 0; 
*/
                case VK_RETURN: 
  //                  SendMessage(hwndToolbar, WM_ENTER, 0, 0);
//SendMessage(command_widget[GD.win_id], WM_USER, ID_ADDCOMMAND, 0); 
                    SendMessage(GetParent(GetParent(hwnd)), 
                                WM_USER, ID_ADDCOMMAND, 0); 
                    return 0; 
            } 
            break; 
 
        case WM_KEYUP: 

        case WM_CHAR: 
            switch (wParam) { 
    //            case VK_TAB: 
      //          case VK_ESCAPE: 
                case VK_RETURN: 
                    return 0; 
            } 
    } 
 
    /* 
     * Call the original window procedure for default 
     * processing. 
     */ 
 
    return CallWindowProc(lpfnEditWndProc, hwnd, 
        msg, wParam, lParam); 
} 
 

typedef struct tagCOMMANDLINE {
    HWND hwndCommand;
    HWND hwndPrompt;
    HFONT hfont;
    int local_win_id;
    int locked;
    int x0, y0;
    int height_original;
    int width, height;
    int y0P, heightP;
    int history_size;
} COMMANDLINE;

#define COMMANDCLASS         "CommandLine"

#define COMMANDMARGIN_X	5
#define COMMANDMARGIN_Y	15
#define COMMANDMARGIN_YP	10
#define COMMANDHISTORY	100
#define COMMANDHISTORYVISIBLE	5

static HWND CreateCommandLine(HWND parent, int x, int y, int width, int height,
			      int id, HINSTANCE hInstance,
			      int command_lock, int cy)
{
    int i;
    HWND hwnd, ewnd;
    WNDCLASS wndclass;
    static BOOL ClassRegistered = FALSE;
    COMMANDLINE *cl;
    DWORD baseunits;
    LOGFONT *lfont;

    if (!ClassRegistered) {
	wndclass.style = CS_HREDRAW | CS_VREDRAW;
	wndclass.lpfnWndProc = (WNDPROC) CommandProc;
	wndclass.cbClsExtra = 0;
	wndclass.cbWndExtra = 2 * sizeof(LONG);
	wndclass.hInstance = hInstance;
	wndclass.hIcon = NULL;
	wndclass.hCursor = LoadCursor(NULL, IDC_ARROW);
	wndclass.hbrBackground = (HBRUSH) (COLOR_BTNFACE + 1);
	wndclass.lpszMenuName = NULL;
	wndclass.lpszClassName = COMMANDCLASS;

	RegisterClass(&wndclass);
	ClassRegistered = TRUE;

    }

    hwnd = CreateWindow(COMMANDCLASS, "", WS_CHILD | WS_VISIBLE ,
			x, y, width, height, parent,
			(HMENU) id, hInstance, NULL);
 
    cl = (COMMANDLINE *) GetWindowLong(hwnd, sizeof(LONG));
    //GetLabelSize(hwnd, &size, "X");

  //  cy *= 3;
  //  cy /= 2;
 
    cl->height_original = height;
    cl->local_win_id = id;
    cl->locked = command_lock;
    cl->x0 = COMMANDMARGIN_X;
    cl->y0 = height - COMMANDMARGIN_Y - cy;
    cl->width = width - 2 * COMMANDMARGIN_X;
    cl->height = cy;

    cl->height *= COMMANDHISTORYVISIBLE;
    cl->history_size = COMMANDHISTORY;
    cl->y0P = cl->y0 - cl->height / COMMANDHISTORYVISIBLE - COMMANDMARGIN_YP;
    cl->heightP = COMMANDMARGIN_YP + cl->height / COMMANDHISTORYVISIBLE;

    lfont = get_statusfont();
    if (lfont) 
        cl->hfont = CreateFontIndirect(lfont);
    else
       cl->hfont = (HFONT) 0;
  
    ewnd = CreateWindow("static", "",
			SS_LEFTNOWORDWRAP | WS_CHILD | WS_VISIBLE,
			cl->x0,
			cl->y0P,
			cl->width,
			cl->heightP,
 			hwnd, NULL, hInstance, NULL);
    cl->hwndPrompt = ewnd;
    if (cl->hfont)
        SendMessage(cl->hwndPrompt, WM_SETFONT, (WPARAM) cl->hfont, 0);

    ewnd = CreateWindow("combobox", "",
			CBS_DROPDOWN | 
                    WS_CHILD |
			 WS_VISIBLE | WS_TABSTOP | WS_BORDER |CBS_AUTOHSCROLL ,
			cl->x0,
			cl->y0,
			cl->width,
			cl->height,
			hwnd, NULL, hInstance, NULL);
    cl->hwndCommand = ewnd;
    if (cl->hfont)
        SendMessage(cl->hwndCommand, WM_SETFONT, (WPARAM) cl->hfont, 0);


    /* Get the edit window handle for each combo box. */ 
    {
    POINT pt;
    HWND hwndEdit1;

    pt.x = 10; 
    pt.y = 10; 

    hwndEdit1 = ChildWindowFromPoint(cl->hwndCommand, pt); 
  
    /* 
     * Change the window procedure for both edit windows 
     * to the subclass procedure. 
     */ 
 
    lpfnEditWndProc = (WNDPROC) SetWindowLong(hwndEdit1, 
                GWL_WNDPROC, (DWORD) SubClassProc);
   }


   return (hwnd);
}

LRESULT CALLBACK CommandProc(HWND hwnd, UINT message, WPARAM wParam, LPARAM lParam)
{
    int i, y0, id, position, height;
    HDC hdc;
    PAINTSTRUCT ps;
    HWND child;
    RECT rect, rectp;
    COMMANDLINE *cl;

    switch (message) {

    case WM_CREATE:
	SetWindowLong(hwnd, 0, (LONG) C_COMMANDLINE);
	cl = (COMMANDLINE *) malloc(sizeof(COMMANDLINE));
      assert(cl);
	SetWindowLong(hwnd, sizeof(LONG), (LONG) cl);
	break;

    case WM_DESTROY:
	{
	    cl = (COMMANDLINE *) GetWindowLong(hwnd, sizeof(LONG));
	    command_widget[cl->local_win_id] = (HWND) 0;
          if (cl->hfont) {
              DeleteObject(cl->hfont);
              cl->hfont = (HFONT) 0;
           }
	    WI[cl->local_win_id].child[C_COMMANDLINE].hwnd = 0;
	    free(cl);
	    PostQuitMessage(0);
	}
	break;

    case WM_CHAR:
// Special case: pass on from parent window to child
       cl = (COMMANDLINE *) GetWindowLong(hwnd, sizeof(LONG));
       SendMessage(cl->hwndCommand, WM_CHAR, wParam, lParam);
       break;

    case WM_KILLFOCUS:
	cl = (COMMANDLINE *) GetWindowLong(hwnd, sizeof(LONG));
	SendMessage(cl->hwndCommand, CB_SHOWDROPDOWN, FALSE, 0);
	break;

    case WM_USER:
	switch (wParam) {
	case ID_SETCOMMAND:
	    cl = (COMMANDLINE *) GetWindowLong(hwnd, sizeof(LONG));
	    SendMessage(cl->hwndCommand, WM_SETTEXT, 0, lParam);
	    if (WI[cl->local_win_id].focus_lock == C_COMMANDLINE ||
		WI[cl->local_win_id].focus_lock == 0)
	        SetFocus(cl->hwndCommand);
	    break;

	case ID_SETCOMMANDPROMPT:
	    cl = (COMMANDLINE *) GetWindowLong(hwnd, sizeof(LONG));
	    SendMessage(cl->hwndPrompt, WM_SETTEXT, 0, lParam);
	    break;

	case ID_SETCOMMANDHISTORY:
	    cl = (COMMANDLINE *) GetWindowLong(hwnd, sizeof(LONG));
	    SendMessage(cl->hwndCommand, CB_INSERTSTRING, 0, lParam);
	    SendMessage(cl->hwndCommand, CB_DELETESTRING,
			          cl->history_size, 0);
	    SendMessage(cl->hwndCommand, CB_SETCURSEL, 0, 0);
	    SendMessage(cl->hwndCommand, CB_SETCURSEL, -1, 0);
	    break;

	case ID_SETENABLE:
	    cl = (COMMANDLINE *) GetWindowLong(hwnd, sizeof(LONG));
	    EnableWindow(cl->hwndCommand, lParam);
	    break;

	case ID_INSTALL_LOCK:
	    cl = (COMMANDLINE *) GetWindowLong(hwnd, sizeof(LONG));
	    if (cl->locked != (int) lParam) {
		cl->locked = (int) lParam;
		q_locked[cl->local_win_id] = FALSE;
	    }
	    break;

	case ID_SETLOCK:
	    cl = (COMMANDLINE *) GetWindowLong(hwnd, sizeof(LONG));
	    q_locked[cl->local_win_id] = (int) lParam;
	    if (lParam)
		break;
	    while (is_command()) {
		G_USERINPUT ui;
		ui.win_id = cl->local_win_id;
		ui.vp_id = G_ERROR;
		ui.x = 0;
		ui.y = 0;
		ui.keycode = 0;
		ui.event = G_COMMANDLINE;
		ui.command = get_commandline();
		INTERNAL_prep_event(&ui);
		if (INTERNAL_process_event(G_COMMANDLINE_EVENT, &ui))
		    INTERNAL_add_event(&ui);

	    }
	    break;

	case ID_ADDCOMMAND:{
		int len;
		char *s;
		G_USERINPUT ui;
		cl = (COMMANDLINE *) GetWindowLong(hwnd, sizeof(LONG));
		len = SendMessage(cl->hwndCommand, WM_GETTEXTLENGTH, 0, 0);
		s = (char *) malloc(len + 2);
             assert(s);
		len = SendMessage(cl->hwndCommand, WM_GETTEXT,
				  len + 1, (LPARAM) s);
		s[len + 1] = '\0';
		add_command(s);
		SendMessage(cl->hwndCommand, CB_INSERTSTRING,
			    0, (LPARAM) s);
		SendMessage(cl->hwndCommand, CB_DELETESTRING,
			    cl->history_size, 0);
		free(s);
		SendMessage(cl->hwndCommand, CB_SETCURSEL, 0, 0);
		SendMessage(cl->hwndCommand, CB_SETCURSEL, -1, 0);
		if (cl->locked && q_locked[cl->local_win_id])
		    break;
		ui.win_id = cl->local_win_id;
		ui.vp_id = G_ERROR;
		ui.x = 0;
		ui.y = 0;
		ui.keycode = 0;
		ui.event = G_COMMANDLINE;
		ui.command = get_commandline();
		INTERNAL_prep_event(&ui);
		if (INTERNAL_process_event(G_COMMANDLINE_EVENT, &ui))
		    INTERNAL_add_event(&ui);

		if (cl->locked)
		    q_locked[cl->local_win_id] = TRUE;
	    }
	    break;


	case ID_RESIZE:
	    cl = (COMMANDLINE *) GetWindowLong(hwnd, sizeof(LONG));
	    SendMessage(cl->hwndCommand, CB_SHOWDROPDOWN, FALSE, 0);
	    id = GetWindowLong(GetParent(hwnd), 0);
	    GetClientRect(GetParent(hwnd), &rectp);
	    GetWindowRect(hwnd, &rect);
	    position = GetWindowLong(hwnd, 0);
       
	    MoveWindow(hwnd,
		       0,
		       rectp.bottom - cl->height_original,
		       LOWORD(lParam),
                    cl->height_original,
		       TRUE);
	    cl = (COMMANDLINE *) GetWindowLong(hwnd, sizeof(LONG));
	    cl->width = LOWORD(lParam) - 2 * COMMANDMARGIN_X;
	    MoveWindow(cl->hwndCommand,
		       cl->x0,
		       cl->y0,
		       cl->width,
		       cl->height,
		       TRUE);
	    MoveWindow(cl->hwndPrompt,
		       cl->x0,
		       cl->y0P,
		       cl->width,
		       cl->heightP,
		       TRUE);
           break;

	case ID_FOCUS: 
            cl = (COMMANDLINE *) GetWindowLong(hwnd, sizeof(LONG));
            SetFocus(cl->hwndCommand);
            return 0;
	}
	break;

    }
    return (DefWindowProc(hwnd, message, wParam, lParam));
}

#define MAX_SYMBOLS 256

int INTERNAL_create_commandline(int local_win_id, int command_lock)
{
    int i, id, y1, y2, at_top, at_bottom, height;
    RECT rc;
    HWND hwnd;
    SIZE size;
    LOGFONT *lfont;

    if (WI[local_win_id].child[C_COMMANDLINE].hwnd)
	return G_ERROR;

    lfont = get_statusfont();
    if (lfont) {
       size.cy = -lfont->lfHeight;
    }
    else
       GetLabelSize(WI[local_win_id].hparent,&size,"AJLKXjg");	
    size.cy *= 3;
    size.cy /= 2;

    height = 4 * size.cy; //(MENUBARHEIGHT - 14 + size.cy) * 2;
    GetWindowRect(WI[local_win_id].hparent, &rc);
    MoveWindow(WI[local_win_id].hparent,
	       rc.left,
	       rc.top,
	       rc.right - rc.left,
	       rc.bottom - rc.top + height,
	       TRUE);
    WI[local_win_id].child[C_COMMANDLINE].height = height;
    GetClientRect(WI[local_win_id].hparent, &rc);
    for (i = 0, y1 = 0; i < C_COMMANDLINE; i++)
	y1 += WI[local_win_id].child[i].height;
    id = local_win_id;
    hwnd = CreateCommandLine(WI[local_win_id].hparent,
			     0,
			     y1,
			     rc.right - rc.left + 1,
			     height,
			     id,
			     _hInst,
			     command_lock,
                         size.cy);
    command_widget[local_win_id] = hwnd;
    WI[local_win_id].child[C_COMMANDLINE].hwnd = hwnd;

    return G_OK;
}


/* --- COMMAND QUEUE --- */
#define COMMANDQMAX 25
static char *command_queue[COMMANDQMAX + 1];
static int q_commandhead, q_commandtail;

static void add_command(char *command)
{
    if (command == NULL)
	return;
    q_commandtail++;
    if (q_commandtail > COMMANDQMAX)
	q_commandtail = 0;
    if (q_commandtail == q_commandhead) {
	(q_commandtail == 0) ? (q_commandtail = COMMANDQMAX) : (q_commandtail--);
	return;
    }
    if (command_queue[q_commandtail] != NULL)
	free(command_queue[q_commandtail]);
    command_queue[q_commandtail] = (char *) malloc(strlen(command) + 1);
    assert(command_queue[q_commandtail]);
    strcpy(command_queue[q_commandtail], command);
}

static int is_command(void)
{
    return (!(q_commandhead == q_commandtail));
}


void INTERNAL_init_commandqueue(void)
{
    int i;

    for (i = 0; i <= COMMANDQMAX; i++)
	command_queue[i] = NULL;
    q_commandhead = q_commandtail = 0;
    for (i = 0; i < G_MAXWINDOW; i++)
	command_widget[i] = (HWND) 0;
}

void INTERNAL_exit_commandqueue(void)
{
    int i;

    for (i = 0; i <= COMMANDQMAX; i++)
	if (command_queue[i] != NULL) {
	    free(command_queue[i]);
	    command_queue[i] = NULL;
	}
    q_commandhead = q_commandtail = 0;
}

static char *get_commandline(void)
{
    if (!is_command())
	return NULL;
    q_commandhead++;
    if (q_commandhead > COMMANDQMAX)
	q_commandhead = 0;
    return command_queue[q_commandhead];
}

/*
   void INTERNAL_init_keyqueue(int win_id)
   {
   win_id = win_id;
   }
 */

/*
   void INTERNAL_trap_keypress_proc(Widget w, XtPointer client_data, XtPointer call_data)
   {

   }

   void INTERNAL_command_proc(Widget w, XtPointer client_data, XtPointer call_data)
   {

   }

   ------------------- */
/* --- KEYBOARD QUEUE ---
   #define KEYQMAX 100
   static XKeyEvent key_queue[G_MAXWINDOW][KEYQMAX+1];
   static int q_keyhead[G_MAXWINDOW], q_keytail[G_MAXWINDOW];
   static int q_keylock[G_MAXWINDOW];
   static int q_locking[G_MAXWINDOW];

   static void add_key(int win_id, XKeyEvent *key)
   {
   q_keytail[win_id]++;
   if (q_keytail[win_id] > KEYQMAX) q_keytail[win_id] = 0;
   if (q_keytail[win_id] == q_keyhead[win_id]) {
   (q_keytail[win_id] == 0) ? (q_keytail[win_id] = KEYQMAX) : (q_keytail[win_id]--);
   g_bell();
   return;
   }
   memcpy(&(key_queue[win_id][q_keytail[win_id]]), key, sizeof(XKeyEvent));
   }

   static int is_key(int win_id)
   {
   return (!(q_keyhead[win_id] == q_keytail[win_id]));
   }


   void INTERNAL_init_keyqueue(int win_id)
   {
   q_locking[win_id] = FALSE;
   q_keylock[win_id] = FALSE;;
   q_keyhead[win_id] = q_keytail[win_id] = 0;
   }

   static void set_keylock(int win_id, int tf)
   {
   if (!q_locking[win_id])
   return;
   q_keylock[win_id] = tf;
   if (tf == FALSE) 
   process_keyqueue(win_id);
   }

   static XKeyEvent *get_key(win_id)
   {
   if (!is_key(win_id))
   return NULL;
   q_keyhead[win_id]++;
   if (q_keyhead[win_id] > KEYQMAX) q_keyhead[win_id] = 0;
   return  &(key_queue[win_id][q_keyhead[win_id]]);
   }

   static void process_keyqueue(int win_id)
   {
   int i;
   KeySym  k;
   Widget w;    
   XKeyEvent *event;

   w = XmCommandGetChild(command_widget[win_id], XmDIALOG_COMMAND_TEXT);
   for (i=0; ((event = get_key(win_id)) != NULL); i++) {
   event->time = (Time) time(NULL);
   k = XKeycodeToKeysym(INTERNAL_display, event->keycode, 0);
   XSendEvent(INTERNAL_display, XtWindow(w), FALSE, KeyPressMask, (XEvent*) event);

   if (k == XK_Return)
   return;
   }
   }

   ---------------- */
void g_SCREEN_set_commandline(int win_id, char *command)
{
    int local_win_id;

    local_win_id = GET_LOCAL_ID(win_id);
    if (command_widget[local_win_id])
	SendMessage(command_widget[local_win_id],
		    WM_USER, ID_SETCOMMAND, (LPARAM) command);
}

void g_SCREEN_set_commandprompt(int win_id, char *prompt)
{
    int local_win_id;

    local_win_id = GET_LOCAL_ID(win_id);
    if (command_widget[local_win_id])
	SendMessage(command_widget[local_win_id],
		    WM_USER, ID_SETCOMMANDPROMPT, (LPARAM) prompt);
}

void g_SCREEN_set_commandhistory(int win_id, char *history)
{
    int local_win_id;

    local_win_id = GET_LOCAL_ID(win_id);
    if (command_widget[local_win_id])
	SendMessage(command_widget[local_win_id],
		    WM_USER, ID_SETCOMMANDHISTORY, (LPARAM) history);
}



void g_SCREEN_enable_nextcommand(int win_id)
{
    int local_win_id;

    local_win_id = GET_LOCAL_ID(win_id);
    if (command_widget[local_win_id])
	SendMessage(command_widget[local_win_id],
		    WM_USER, ID_SETLOCK, (LPARAM) FALSE);
}


void g_SCREEN_set_commandlocking(int win_id, int tf)
{
    int local_win_id;

    local_win_id = GET_LOCAL_ID(win_id);
    if (command_widget[local_win_id])
       SendMessage(command_widget[local_win_id],
		    WM_USER, ID_INSTALL_LOCK, (LPARAM) tf);
}

void g_SCREEN_enable_commandinput(int win_id, int enable)
{
    int local_win_id;

    local_win_id = GET_LOCAL_ID(win_id);
    if (command_widget[local_win_id])
	SendMessage(command_widget[local_win_id],
		    WM_USER, ID_SETENABLE, (LPARAM) enable);
}


void g_set_keyboard_focus(int win_id, int focus, int locked)
{
    int id, device;
    COMMANDLINE *cl;

    id = GET_LOCAL_ID(win_id);
    device = GET_DEVICE(win_id);
    if (id < 0 || id >= G_MAXWINDOW)
	return;
    if (device != G_SCREEN)
	return;
    if (!WI[id].hparent)
	return;
    switch (focus) {
	case G_FOCUS_DRAWINGAREA:
	    if (WI[id].child[C_DRAWAREA].hwnd)
		SendMessage(WI[id].child[C_DRAWAREA].hwnd,
			    WM_USER, ID_FOCUS, 0);
	    if (locked)
		WI[id].focus_lock = C_DRAWAREA;
	    break;
	case G_FOCUS_COMMANDLINE:
	    if (WI[id].child[C_COMMANDLINE].hwnd)
		SendMessage(WI[id].child[C_COMMANDLINE].hwnd,
			    WM_USER, ID_FOCUS, 0);
	    if (locked)
		WI[id].focus_lock = C_COMMANDLINE;
	    break;
	case G_FOCUS_CONSOLEAREA:
	    if (WI[id].child[C_CONSOLE].hwnd)
		SendMessage(WI[id].child[C_CONSOLE].hwnd,
			    WM_USER, ID_FOCUS, 0);
	    if (locked)
		WI[id].focus_lock = C_CONSOLE;
	    break;
    }
    if (!locked)
	WI[id].focus_lock = locked;
}


