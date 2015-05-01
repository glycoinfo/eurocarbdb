/************************************************************************/
/*                               genmotif.h                             */
/*                                                                      */
/*  Platform : Motif                                                    */
/*  Module   : Used internally by genplot                               */
/*                                                                      */
/*  Albert van Kuik                                                     */
/************************************************************************/

#define MENUBARHEIGHT 32
#define BUTTONBAR32HEIGHT 44

#define ID_MAP      1
#define ID_EXPOSE   2
#define ID_RESIZE   3
#define ID_INPUT    4
#define ID_V_INCREMENT      5
#define ID_V_DRAG           6
#define ID_V_DECREMENT      7
#define ID_V_PAGEINCREMENT  8
#define ID_V_PAGEDECREMENT  9
#define ID_H_INCREMENT      10
#define ID_H_DRAG           11
#define ID_H_DECREMENT      12
#define ID_H_PAGEINCREMENT  13
#define ID_H_PAGEDECREMENT  14

#define KEYQMAX 100

typedef void (*CURSORFUNC)(int,int,int);

#define CONS_BUF_SIZE   1000
#define MAX_CONSOLE_LINES 500


#define POPUP_WAIT	0

typedef struct POPUP_BOX {
    int status;
    Widget popup;
} POPUP_BOX;

#define PROMPTTEXTLENGTH	200
#define PROMPT_STATUS_WAIT	0
#define PROMPT_STATUS_OK	1
#define PROMPT_STATUS_CANCEL	2

typedef struct PROMPT_BOX {
    int    status;
    char   string[PROMPTTEXTLENGTH+1];
    Widget popup;
} PROMPT_BOX;

typedef struct LIST_BOX {
    int    status;
    int    pos;
    char   string[PROMPTTEXTLENGTH+1];
    Widget popup, list;
} LIST_BOX;

typedef struct RADIO_BOX {
    int    status;
    int    pos;
    Widget popup;
} RADIO_BOX;


typedef struct CHECK_BOX {
    int    status;
    int    *pos;
    Widget popup;
} CHECK_BOX;

#define SELECT_STATUS_WAIT	  0
#define SELECT_STATUS_OK     1
#define SELECT_STATUS_CANCEL 2

#define MAX_LIST_ITEMS 250

#define MAX_CONT_ITEMS			255

#define CURTYPE(id) xwini[(id)].xwin_cursortype
void INTERNAL_reset_cursor(int id);
void INTERNAL_init_cursor(GC gc_pixmap);
void INTERNAL_update_crosshair(int id, int x, int y);
void INTERNAL_hide_crosshair(int id);
void INTERNAL_show_crosshair(int , int, int);

void INTERNAL_update_floatbutton_position(int local_win_id, int width, int height);

typedef struct BUTTON_INFO {
    int button_id;
    int cc_id;
    int cont_id;
} BUTTON_INFO;

typedef struct CONTAINER_CHILD {
    Widget p;
    Widget f;
    Widget t;
    Widget w;
    Widget *c;
    int cc_id;
    int type;
    int id;
    int cont_id;
    int win_id;
    int local_win_id;
    int items_visible;
    int item_count;
    int item;
    int disabled;
    int horizontal;
    int frame;
    char *title;
    int  *select;
    char *label;
    char **data;
    void (*func)(G_POPUP_CHILDINFO *);
    void *userdata;
    G_POPUP_CHILDINFO *store;
    BUTTON_INFO *binfo;
    int text_callback_disabled;
    struct CONTAINER_CHILD *next;
} CONTAINER_CHILD;

typedef struct CONTAINER_PANEL {
    Widget cp;
    struct CONTAINER_PANEL *next;
} CONTAINER_PANEL;


typedef struct CONTAINER_INFO {
    int                   cont_id;
    int                   win_id;
    int                   local_win_id;
    int                   flag;
    int                   cont_select_status;
    Widget                contpopup;
    Widget                container;
    Widget                scrollwindow;
    Widget                tabwindow;
    CONTAINER_PANEL       *panel;
    CONTAINER_CHILD       *container_child;
    void (*okfunc)(G_POPUP_CHILDINFO *);
    void *okdata;
    void (*cancelfunc)(G_POPUP_CHILDINFO *);
    void *canceldata;
    struct CONTAINER_INFO *next;
} CONTAINER_INFO;


typedef struct XWININFOtag {
    int width;
    int height;
    Widget toplevel_widget;
    Widget form_widget;
    Widget draw_widget;
    Widget menubar_widget;
    Widget menubar2_widget;
    Widget buttonbar_widget;
    Widget buttonbar2_widget;
    Widget scroll_widget;
    Widget v_scrollbar_widget;
    Widget h_scrollbar_widget;
    Widget command_widget;
    Widget console_widget;
    Widget pane_widget;
    Window xwin;
    Pixmap xpix;
    Drawable xdraw;
    int double_buffering;
    int xwin_cursortype;
    int xlib_old_x;
    int xlib_old_y;
    int xlib_old_init;
    int xlib_pixcopymode;
    int xlib_marker_x;
    int xlib_marker_y;
    int xx_hidden;
    CURSORFUNC xx_draw_cursor_func;
    int console_lines_count;
    char *console_buf;
    XKeyEvent key_queue[KEYQMAX+1];
    int q_keyhead, q_keytail;
    int q_keylock;
    int q_locking;
    POPUP_BOX msgbox;
    POPUP_BOX ynbox;
    POPUP_BOX yncbox;
    PROMPT_BOX prmbox;
    PROMPT_BOX filebox, dirbox, saveasbox;
    LIST_BOX listbox;
    RADIO_BOX radiobox;
    CHECK_BOX checkbox;
    CONTAINER_INFO *cont_info;
} XWININFO;

extern XWININFO xwini[];


extern Display *INTERNAL_display;
extern XtAppContext INTERNAL_app_context;
extern int INTERNAL_screen;
extern Window INTERNAL_rootwin;


void INTERNAL_command_proc(Widget w, XtPointer client_data, XtPointer call_data);
void INTERNAL_scrollbar_proc(Widget w, XtPointer client_data, XtPointer call_data);
void INTERNAL_trap_keypress_proc(Widget w, XtPointer client_data, XtPointer call_data);
void INTERNAL_command_add_return_proc(Widget w, XtPointer client_data, XEvent *event);

void INTERNAL_init_console(int win_id);
void INTERNAL_exit_console(int win_id);
void INTERNAL_init_keyqueue(int win_id);
void INTERNAL_init_screen_stuff(void);
void INTERNAL_exit_screen_stuff(void);

int  INTERNAL_get_clampscrollbars(int id, int vp_id, float *x1, float *y1,
                                      float *x2, float *y2);
int  INTERNAL_update_scrollbars(int id, int vp_id, float shift_x, float shift_y);
Pixmap INTERNAL_make_pixmap(int win_id, char *xpm[], int isgray);



