/************************************************************************/
/*                               genwin.h                               */
/*                                                                      */
/*  Platform : Microsoft Windows                                        */
/*  Module   : Used internally by genplot                               */
/*                                                                      */
/*  Albert van Kuik                                                     */
/************************************************************************/

#ifndef _GENWIN_H
#define _GENWIN_H

#include "layoutmgr.h"


typedef struct tagBOXINFO {
    HWND hwnd;
    HWND parent_hwnd;
    WORD type;
    WORD x;
    WORD y;
    WORD cx;
    WORD cy;
    WORD id;
    WORD local_id;
    WORD pos;

    PACKINFO pinf;
    PACKINFO *pinf_stack;
    int level;
    int direction;
    int width, height;
    int marginx, marginy;
    int borderx, border_top, border_bottom;
    int flag;

    char *title;
    int  *select;

    char *label;
    int disabled;
    int item_count;
    int items_visible;

    int item;
    int horizontal;
    int frame;
    int group;
    int on;
    int range[4];
    int modal;
    char **data;
    void (*func)(G_POPUP_CHILDINFO *);
    void *userdata;

    G_POPUP_CHILDINFO *ci;

    int cont_id;
    int win_id;
    struct tagBOXINFO *next;
    struct tagBOXINFO *prev;
    struct tagBOXINFO *parent;
} BOXINFO;


#define MAX_TABBOXINFO	50

typedef struct tagTABDLGINFO { 
    HWND hwndTab;       // tab control 
    HWND hwndDisplay;   // current child dialog box 
    RECT rcDisplay;     // display rectangle for the tab control 
    int  numtabs;
    BOXINFO *bi_array[MAX_TABBOXINFO];
} TABDLGINFO; 


#define POPBUTTONLABELSIZE	 20
typedef struct CONTAINER_INFO {
    int                   cont_id;
    int                   win_id;
    int                   local_win_id;
    int                   flag;
    int                   select_status;
    HWND                  parent_hwnd;
    HWND			hwndBox;
    BOXINFO               *container;
    TABDLGINFO            *tabinfo;
    void (*okfunc)(G_POPUP_CHILDINFO *);
    void *okdata;
    char oklabel_text[POPBUTTONLABELSIZE+1];
    void (*cancelfunc)(G_POPUP_CHILDINFO *);
    void *canceldata;
    char cancellabel_text[POPBUTTONLABELSIZE+1];
    struct CONTAINER_INFO *next;
} CONTAINER_INFO;


#define POPUP_WAIT	0

typedef struct POPUP_BOX {
    int     status;
    BOXINFO *popup;
} POPUP_BOX;

#define PROMPTTEXTLENGTH	200
#define PROMPT_STATUS_WAIT	0
#define PROMPT_STATUS_OK	1
#define PROMPT_STATUS_CANCEL	2

typedef struct PROMPT_BOX {
    char      string[PROMPTTEXTLENGTH+1];
    BOXINFO   *popup;
} PROMPT_BOX;

typedef struct LIST_BOX {
    int    status;
    int    pos;
    char   string[PROMPTTEXTLENGTH+1];
    BOXINFO  *popup, list;
} LIST_BOX;

typedef struct RADIO_BOX {
    int    status;
    int    pos;
    BOXINFO  *popup;
} RADIO_BOX;


typedef struct CHECK_BOX {
    int    status;
    int    *pos;
    BOXINFO  *popup;
} CHECK_BOX;

#define SELECT_STATUS_WAIT	   0
#define SELECT_STATUS_OK     1
#define SELECT_STATUS_CANCEL 2
#define SELECT_STATUS_HIDDEN 3
#define SELECT_STATUS_NEW    4

#define MAX_LIST_ITEMS 250

#define MAX_CONT_ITEMS			255

/*******/
#define C_MENUBAR2	0
#define C_BUTTONBAR1	1
#define C_DRAWAREA	2
#define C_CONSOLE	C_DRAWAREA
#define C_BUTTONBAR2	3
#define C_BUTTONBAR3	4
#define C_BUTTONBAR4	5
#define C_COMMANDLINE	6

#define MAXWCHILDREN	7

typedef struct {
    HWND hwnd;
    int  height;
    int  rows;
    int  init;
} WCHILD;

typedef struct {
    HWND hparent;
    HMENU hmenu;
    WCHILD child[MAXWCHILDREN];
    int flag;
    HDC hdc;
    HDC hdc_cross;
    int hdc_flag;
    HDC hdcPix;
    HDC hdcCur;
    HDC hdcPaint;
    HBITMAP pix;
    HBITMAP pixOld;
    HPEN pen, oldpen;
    int usepix;
    int locked;
    int focus_lock;
    int bbar32;
    int noresize;
    int isclipped;
    int clipx1, clipy1, clipx2, clipy2;
    int isvisible;
    int scroll;
    int cursortype;
    int cursorhidden;
    int cursorx, cursory;
    int pixcopymode;
    int rbx, rby, bltr;
    int doing_toolbar_stuff;
    POPUP_BOX msgbox;
    POPUP_BOX ynbox;
    POPUP_BOX yncbox;
    PROMPT_BOX prmbox;
    PROMPT_BOX filebox, dirbox, saveasbox;
    LIST_BOX listbox;
    RADIO_BOX radiobox;
    CHECK_BOX checkbox;
    CONTAINER_INFO *cont_info;
} MSWININFO;

extern MSWININFO WI[G_MAXWINDOW];

extern HINSTANCE _hInst;
extern HHOOK hMouseHook;

HWND CreateConsole(HWND parent, int x, int y, int width, int height,
		       int id, HINSTANCE hInstance);

extern int mswin_console_id;
extern int mswin_color;
extern int mswin_caretx, mswin_carety;
/*
   static int   mswin_linestyle;
   static int   mswin_linewidth;

   static int   mswin_bkcolor;
   static int   mswin_creating;

 */

#define MSPEN  WI[GD.win_id].pen
#define MSOLDPEN  WI[GD.win_id].oldpen
#define MSWIN  WI[GD.win_id].child[C_DRAWAREA].hwnd
//#define CWIN   WI[mswin_console_id].child[C_DRAWAREA].hwnd
#define MSHDC  WI[GD.win_id].hdc
#define PIXHDC  WI[GD.win_id].hdcPix
#define CURHDC  WI[GD.win_id].hdcCur
//#define CHDC   WI[mswin_console_id].hdc
//#define RGBCOLOR(c)  GetNearestColor(MSHDC,RGB(g_palette[(c)].r,  \
//    g_palette[(c)].g,  \
//    g_palette[(c)].b))

//#define MENUBARHEIGHT 32
#define BUTTONBARMARGIN_X	4
#define BUTTONBARMARGIN_Y	3
#define BUTTONBARMARGIN_LABEL	6
#define MENUBARHEIGHT 28
#define BUTTONBAR32HEIGHT (32 + 2 * BUTTONBARMARGIN_Y)

#define POPUPMARGIN_X	4
#define POPUPMARGIN_Y	6

#define BUTTONLABELMARGIN_X	4
#define BUTTONLABELMARGIN_Y	4

#define FRAMEMARGIN_X	10
#define FRAMEMARGIN_Y	4

#define CHECKBOXMARGIN_X	16

enum USERID {
    ID_MAP = 1,
    ID_EXPOSE,
    ID_RESIZE,
    ID_INPUT,
    ID_FOCUS,
    ID_UPDATE,
    ID_V_INCREMENT,
    ID_V_DRAG,
    ID_V_DECREMENT,
    ID_V_PAGEINCREMENT,
    ID_V_PAGEDECREMENT,
    ID_H_INCREMENT,
    ID_H_DRAG,
    ID_H_DECREMENT,
    ID_H_PAGEINCREMENT,
    ID_H_PAGEDECREMENT,
    ID_KILL_CROSSHAIR
};

void INTERNAL_init_console(int win_id);
void INTERNAL_exit_console(void);
void INTERNAL_init_keyqueue(int win_id);
void INTERNAL_destroy_window_menus(int win_id);
void INTERNAL_init_screen_stuff(void);
void INTERNAL_exit_screen_stuff(void);

int  INTERNAL_get_clampscrollbars(int id, int vp_id, float *x1, float *y1,
				 float *x2, float *y2);
int  INTERNAL_update_scrollbars(int id, int vp_id, float shift_x, float shift_y);
void INTERNAL_toggle_checkmark(int id);
int  INTERNAL_create_commandline(int win_id, int command_lock);

void INTERNAL_kill_menu(int win_id);
void INTERNAL_check_hdc(MSWININFO *wi, int active);

HBITMAP INTERNAL_Compose_Bitmap(HDC hdcLocal, char *xpm[]);

void INTERNAL_init_cursor(void);
void INTERNAL_reset_cursor(int id);
void INTERNAL_kill_crosshair(int reset);
void INTERNAL_update_crosshair(int id, int x, int y);
void INTERNAL_destroy_handcursor();
void INTERNAL_set_handcursor();
void INTERNAL_destroy_updowncursor();
void INTERNAL_set_updowncursor();

void INTERNAL_clear_pixmap(int id);
char *INTERNAL_get_tooltip(int return_id);

#endif

