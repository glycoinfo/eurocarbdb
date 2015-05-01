/************************************************************************/
/*                               g_inter.h                              */
/*                                                                      */
/*  Platform : All                                                      */
/*  Module   : Used internally by genplot                               */
/*                                                                      */
/*  Albert van Kuik                                                     */
/************************************************************************/

#ifndef G_INTER_H
#define G_INTER_H

#include <math.h>
#include <limits.h>

#ifndef BYTE
#define BYTE unsigned char
#endif

#define round(f)	(((f) < 0.0) ? (ceil((f)-0.5)) : (floor((f)+0.5)))

#ifndef min
#define min(a,b)        (((a) < (b)) ? (a) : (b))
#endif
#ifndef max
#define max(a,b)        (((a) > (b)) ? (a) : (b))
#endif
#define abs(a)          (((a) < 0) ? (-a) : (a))


typedef struct GRAPHCONTEXT {
  int      isclipped;
  int      linewidth;
  unsigned linepat;
  int      fcolor;
  int      bcolor;
  int      font;
  int      fontscaling;
  int      textdirection;
  float    charsize;
} GRAPHCONTEXT;


typedef struct VIEWPORTTYPE {
  int   id;
  float wx1, wy1, wx2, wy2;
  float wx, wy;
  long  cx, cy;
  long  clip_cx, clip_cy;
  float vx1, vy1, vx2, vy2;
} VIEWPORTTYPE;

typedef struct HISTORYTYPE {
  int list;
  struct HISTORYTYPE *next;
} HISTORYTYPE;

typedef struct WINDOWTYPE {
  int  win_id;
  int  isopen;
  long x0, y0;
  long width, height;
  long flag;
  int  cursortype;
  VIEWPORTTYPE vp_init;
  GRAPHCONTEXT gc_init;
  HISTORYTYPE *first, *last;
} WINDOWTYPE;

typedef struct DEVICETYPE {
  int  isopen;
  int  isupsidedown;
  int  able2clip;
  int  multiwindows;
  int  isclipped;
  long clipxy[4];
  int  win_id;
  WINDOWTYPE win[G_MAXWINDOW];
} DEVICETYPE;

typedef struct STORETYPE {
    float ax,
          bx,
          cx,
          dx,
          ay,
          by,
          cy,
          dy;
     long vx1,
          vy1,
          vxmax,
          vymax;
} STORETYPE;

extern STORETYPE store_param;

typedef short polytype;

#define CM_PER_INCH		2.54
#define PS_UNITS_PER_INCH	72.0
#define PSHR_UNITS_PER_INCH	1200.0
#define PS_UNITS_PER_CM		(PS_UNITS_PER_INCH / CM_PER_INCH)
#define PSHR_UNITS_PER_CM	(PSHR_UNITS_PER_INCH / CM_PER_INCH)
#define HP_UNITS_PER_CM		400.0
#define HP_UNITS_PER_INCH	(HP_UNITS_PER_CM * CM_PER_INCH) 
#define PS2HP_UNITS		(PS_UNITS_PER_INCH / HP_UNITS_PER_INCH) 
#define PS2PSHR_UNITS		(PS_UNITS_PER_INCH / PSHR_UNITS_PER_INCH) 

#define A4_WIDTH_IN_CM		29.7
#define A4_HEIGHT_IN_CM		21.0
#define A3_WIDTH_IN_CM		42.0
#define A3_HEIGHT_IN_CM		29.7
#define LETTER_WIDTH_IN_CM	27.9
#define LETTER_HEIGHT_IN_CM	21.6	
#define LEGAL_WIDTH_IN_CM	35.6
#define LEGAL_HEIGHT_IN_CM	21.6	

#define A4_WIDTH_IN_PS_UNITS  		842
#define A4_HEIGHT_IN_PS_UNITS		595
#define A3_WIDTH_IN_PS_UNITS  		1191
#define A3_HEIGHT_IN_PS_UNITS		842
#define LETTER_WIDTH_IN_PS_UNITS	792
#define LETTER_HEIGHT_IN_PS_UNITS	612	
#define LEGAL_WIDTH_IN_PS_UNITS		1008
#define LEGAL_HEIGHT_IN_PS_UNITS	612	

#define A4_PAPER		4
#define A3_PARER		3
#define LETTER_PAPER		2
#define LEGAL_PAPER		1

#define G_MAXGCSTACKSIZE       100
#define G_MAXVPSTACKSIZE       100
#ifdef _WIN32
#define G_MAXDEVICE            6
#else
#define G_MAXDEVICE            5
#endif
#define TITLE_LEN              80

#define G_INTERNAL_BLACK	0
#define G_INTERNAL_WHITE	1

#define DEFAULT_device         G_DUMMY
#define DEFAULT_win_id         0
#define DEFAULT_viewport       0
#define DEFAULT_clip           FALSE
#define DEFAULT_linewidth      1
#define DEFAULT_linepat        G_SOLID
#define DEFAULT_fcolor         G_BLACK
#define DEFAULT_bcolor         G_WHITE
#define DEFAULT_textdirection  0
#define DEFAULT_charsize       1.0
#define DEFAULT_font           G_FONT_HELVETICA
#define DEFAULT_fontscaling    G_ABSOLUTE_FONTSCALING
#define DEFAULT_linkflag       FALSE
#define DEFAULT_cursortype     G_CURSOR_DEFAULT
#define DEFAULT_outfile        stdout
#define DEFAULT_worldx         1000
#define DEFAULT_worldy         750
#define DEFAULT_palettesize    16

/* --- EVENT HANDLERS --- */
#define G_BUTTON1PRESS_EVENT    0
#define G_BUTTON2PRESS_EVENT    1
#define G_BUTTON3PRESS_EVENT    2
#define G_BUTTON1RELEASE_EVENT  3
#define G_BUTTON2RELEASE_EVENT  4
#define G_BUTTON3RELEASE_EVENT  5
#define G_KEYPRESS_EVENT        6
#define G_POINTERMOTION_EVENT   7
#define G_COMMAND_EVENT         8
#define G_COMMANDLINE_EVENT     9
#define G_WINDOWCREATE_EVENT	10
#define G_WINDOWDESTROY_EVENT	11
#define G_OPTIONMENU_EVENT	12
#define G_WINDOWQUIT_EVENT	13
#define MAXEVENTTYPE    	14

char **INTERNAL_get_icon_pixmap(int size);

/* --- events --- */
void INTERNAL_prep_event(G_EVENT *ui);
int  INTERNAL_process_event(int event_type, G_EVENT *ui);
void INTERNAL_add_event(G_EVENT *ui);
int  INTERNAL_is_event(void);
G_EVENT *INTERNAL_get_event(void);
G_EVENT *INTERNAL_peek_event(void);
void INTERNAL_dispatch_message(void);
void INTERNAL_dispatch(void);
void INTERNAL_init_events(void);
void INTERNAL_destroy_events(void);

/* --- cursor --- */
void INTERNAL_cursortype(int local_id, int type);
int  INTERNAL_cursorpos(float *x, float *y);
void INTERNAL_warpcursor(long x, long y);

/* --- clipping --- */
int  INTERNAL_clip(float *x0,float *y0,float *x1,float *y1);
void INTERNAL_set_clip(STORETYPE *store_param);
void INTERNAL_get_clip(int *x1, int *y1, int *x2, int *y2);
void INTERNAL_set_page_clip(int clip);
int  INTERNAL_box_outside_clip_area(int clip, int x1, int y1, int x2, int y2);

/* --- objects ---*/
int INTERNAL_write_object_file(int file_id, char *label, BYTE * obj, 
                               int size, int overwrite);
int INTERNAL_read_object_file(int file_id, char *label, BYTE **obj);
int INTERNAL_find_object_file(int file_id, char *label);

/*
too many macro's in macro's

#define devicex(x)    max(0,min(INT_MAX,round((x) * store_param.ax - store_param.bx)))
#define devicey(y)    max(0,min(INT_MAX,round((y) * store_param.ay - store_param.by)))
#define device_dx(x)  max(0,min(INT_MAX,round(labs((x) * store_param.ax))))
#define device_dy(y)  max(0,min(INT_MAX,round(labs((y) * store_param.ay))))
*/
#define usercoorx(x)  ((x) * store_param.cx - store_param.dx)
#define usercoory(y)  ((y) * store_param.cy - store_param.dy)

#define GGC       g_gc[gen_device]
#define GD        g_dev[gen_device]
#define GWIN      g_dev[gen_device].win[g_dev[gen_device].win_id]

#define WINX0     GWIN.x0
#define WINY0     GWIN.y0
#define WINWIDTH  GWIN.width
#define WINHEIGHT GWIN.height
#define WINFLAG   GWIN.flag

#define G_WIN(id)        g_dev[gen_device].win[(id)]
#define G_WIN_X0(id)     g_dev[gen_device].win[(id)].x0
#define G_WIN_Y0(id)     g_dev[gen_device].win[(id)].y0
#define G_WIN_WIDTH(id)  g_dev[gen_device].win[(id)].width
#define G_WIN_HEIGHT(id) g_dev[gen_device].win[(id)].height
#define G_WIN_FLAG(id)   g_dev[gen_device].win[(id)].flag

#define TEXTROTATE INTERNAL_store_gc.textdirection
/*
#define MAKE_GLOBAL_ID(id,dev)      ((id) + (dev) * G_MAXWINDOW)
#define GET_LOCAL_ID(id)            ((id) % G_MAXWINDOW)
#define GET_DEVICE(id)              ((id) / G_MAXWINDOW)
*/

#define MAKE_GLOBAL_ID(id,dev)    (g_dev[dev].win[id].win_id)
#define GET_LOCAL_ID(id)          (INTERNAL_get_local_win_id(id))
#define GET_DEVICE(id)            (INTERNAL_get_local_device(id))

void g_DIRECT_moveto(float x, float y);
void g_DIRECT_lineto(float x, float y);
void g_DIRECT_label(char *label);
void g_DIRECT_rectangle(float x1, float y1, float x2, float y2);
void g_DIRECT_drawpoly(long numpoint, float *points);
void g_DIRECT_circle(float r);
void g_DIRECT_fillrectangle(float x1, float y1, float x2, float y2);
void g_DIRECT_fillpoly(long numpoint, float *points);
void g_DIRECT_fillcircle(float r);
void g_DIRECT_set_textdirection(int direction);
void g_DIRECT_set_linewidth(int width);
void g_DIRECT_set_linestyle(int pattern);
void g_DIRECT_set_foreground(int color);
void g_DIRECT_set_background(int color);
void g_DIRECT_set_font(int font, int scale_to);
void g_DIRECT_set_charsize(float size);
void g_DIRECT_set_clipping(int clip);
int  g_DIRECT_set_world(int no,float x1,float y1,float x2,float y2);
int  g_DIRECT_set_viewport(int no,float x1,float y1,float x2,float y2);
int  g_DIRECT_select_viewport(int no);
void g_DIRECT_clear_viewport(void);
int  g_DIRECT_call_object(int id);
int  g_DIRECT_push_gc(void);
int  g_DIRECT_pop_gc(void);
int  g_DIRECT_push_viewport(void);
int  g_DIRECT_pop_viewport(void);
int  g_DIRECT_call_object_byname(char *label);
int  g_DIRECT_set_palettesize(int size);
int  g_DIRECT_set_paletteentry(int entry_id, G_PALETTEENTRY entry);

void g_LIST_moveto(float x, float y);
void g_LIST_lineto(float x, float y);
void g_LIST_label(char *label);
void g_LIST_rectangle(float x1, float y1, float x2, float y2);
void g_LIST_drawpoly(long numpoint, float *points);
void g_LIST_circle(float r);
void g_LIST_fillrectangle(float x1, float y1, float x2, float y2);
void g_LIST_fillpoly(long numpoint, float *points);
void g_LIST_fillcircle(float r);
void g_LIST_set_textdirection(int direction);
void g_LIST_set_linewidth(int width);
void g_LIST_set_linestyle(int pattern);
void g_LIST_set_foreground(int color);
void g_LIST_set_background(int color);
void g_LIST_set_font(int font, int scale_to);
void g_LIST_set_charsize(float size);
void g_LIST_set_clipping(int clip);
int  g_LIST_set_world(int no,float x1,float y1,float x2,float y2);
int  g_LIST_set_viewport(int no,float x1,float y1,float x2,float y2);
int  g_LIST_select_viewport(int no);
void g_LIST_clear_viewport(void);
int  g_LIST_call_object(int id);
int  g_LIST_push_gc(void);
int  g_LIST_pop_gc(void);
int  g_LIST_push_viewport(void);
int  g_LIST_pop_viewport(void);
int  g_LIST_call_object_byname(char *label);
int  g_LIST_set_palettesize(int size);
int  g_LIST_set_paletteentry(int entry_id, G_PALETTEENTRY entry);

enum PLOTFUNC_ID {
    G_LIST_MOVETO = 1,
    G_LIST_LINETO,
    G_LIST_LABEL,
    G_LIST_RECTANGLE,
    G_LIST_DRAWPOLY,
    G_LIST_CIRCLE,
    G_LIST_FILLRECTANGLE,
    G_LIST_FILLPOLY,
    G_LIST_FILLCIRCLE,
    G_LIST_SET_TEXTDIRECTION,
    G_LIST_SET_LINEWIDTH,
    G_LIST_SET_LINESTYLE,
    G_LIST_SET_FOREGROUND,
    G_LIST_SET_BACKGROUND,
    G_LIST_SET_FONT,
    G_LIST_SET_CHARSIZE,
    G_LIST_SET_CLIPPING,
    G_LIST_SET_WORLD,
    G_LIST_SET_VIEWPORT,
    G_LIST_SELECT_VIEWPORT,
    G_LIST_CLEAR_VIEWPORT,
    G_LIST_CALL_OBJECT,
    G_LIST_PUSH_GC,
    G_LIST_POP_GC,
    G_LIST_PUSH_VIEWPORT,
    G_LIST_POP_VIEWPORT,
    G_LIST_CALL_OBJECT_BYNAME,
    G_LIST_SET_PALETTESIZE,
    G_LIST_SET_PALETTEENTRY    
};


extern VIEWPORTTYPE INTERNAL_store_vp;
extern GRAPHCONTEXT INTERNAL_store_gc;
extern int    INTERNAL_g_argc;
extern char **INTERNAL_g_argv;
extern char  *INTERNAL_g_application;

void INTERNAL_kill_objects(void);
void INTERNAL_init_objects(void);
void INTERNAL_add_list2history(int list);
void INTERNAL_set_directmode(void);
int  INTERNAL_update_window(int device, int id);

extern int INTERNAL_object_playback_flag;

void INTERNAL_get_palette_color(G_PALETTEENTRY *g, int color);

VIEWPORTTYPE *INTERNAL_get_viewport(int id);
int INTERNAL_delete_viewport(int id);
void INTERNAL_init_viewports(void);
void INTERNAL_kill_viewports(void);
void INTERNAL_calc_viewport_xy(int win_id, int vp_id,
                      long xx, long yy, float *x, float *y);
void INTERNAL_calc_viewport_rect(int win_id, int vp_id,
                      int *x1, int *y1, int *x2, int *y2);

void INTERNAL_kill_viewport_history(int win_id);
void INTERNAL_init_viewport_history(int win_id);
void INTERNAL_reset_viewport_history(int win_id);
void INTERNAL_add_viewport_history(int win_id, VIEWPORTTYPE *vp);
int  INTERNAL_in_which_viewport(int win_id, long x, long y);

void INTERNAL_kill_winIDs(void);
void INTERNAL_init_winIDs(void);
int INTERNAL_new_global_win_id(int device, int local_id);
int INTERNAL_get_local_win_id(int win_id);
int INTERNAL_get_local_device(int win_id);
void INTERNAL_delete_win_id(int win_id);


extern DEVICETYPE      g_dev[G_MAXDEVICE];
extern GRAPHCONTEXT    g_gc[G_MAXDEVICE];

extern GRAPHCONTEXT    gc_stack[G_MAXGCSTACKSIZE];
extern VIEWPORTTYPE    vp_stack[G_MAXVPSTACKSIZE];

extern int             gc_stack_pointer;
extern int             vp_stack_pointer;
extern int             gen_device;
extern int             gen_softclip;
extern int             gen_dummy_window;
extern int             gen_font;
extern FILE            *g_outfile;
extern VIEWPORTTYPE    *g_viewport;

/* --- hide and display crosshair cursor --- */
void INTERNAL_hide_cursor_crosshair(int id);
void INTERNAL_show_cursor_crosshair(int id);

void set_DUMMY_mode(void);
void set_SCREEN_mode(void);

/* --- Console functions --- */
void  g_SCREEN_open_console(long x0,long y0,long width,long height, char *title);
void  g_SCREEN_close_console(void);
int   g_SCREEN_cons_puts(const char *string);
int   g_SCREEN_cons_fputs(const char *string, FILE *file);
int   g_SCREEN_cons_fputc(char c, FILE *file);
int   g_SCREEN_cons_printf(const char *format, ...);
int   g_SCREEN_cons_fprintf(FILE *file, const char *format, ...);
char  *g_SCREEN_cons_gets(char *buffer);
char  *g_SCREEN_cons_fgets(char *string, int maxchar, FILE *file);
int   g_SCREEN_cons_fgetc(FILE *file);

int   g_SCREEN_console_printf(int win_id, const char *format, ...);
/* --- commandline --- */

void  g_SCREEN_set_commandline(int win_id, char *command);
void  g_SCREEN_set_commandprompt(int win_id, char *prompt);
void  g_SCREEN_set_commandhistory(int win_id, char *history);
void  g_SCREEN_set_commandlocking(int win_id, int lock);
void  g_SCREEN_enable_nextcommand(int win_id);
void  g_SCREEN_enable_commandinput(int win_id, int enable);

/* --- menu's --- */

int   g_SCREEN_menu_create_menubar(int win_id);
int   g_SCREEN_menu_create_menubar2(int win_id);
int   g_SCREEN_menu_create_buttonbar(int win_id);
int   g_SCREEN_menu_create_buttonbar2(int win_id);
int   g_SCREEN_menu_create_buttonbox2(int win_id, int rows);
int   g_SCREEN_menu_create_popup(int win_id);
int   g_SCREEN_menu_create_floating(int win_id);

void  g_SCREEN_menu_select_popup(int menu_id);
void  g_SCREEN_menu_hide_popup(int win_id);

void  g_SCREEN_menu_destroy_menubar_children(int win_id);
void  g_SCREEN_menu_destroy_menubar2_children(int win_id);
void  g_SCREEN_menu_destroy_buttonbar_children(int win_id);
void  g_SCREEN_menu_destroy_buttonbar2_children(int win_id);
void  g_SCREEN_menu_destroy_popup(int win_id);
void  g_SCREEN_menu_destroy_button(int menu_id, int item_id);
void  g_SCREEN_menu_destroy_menubar2(int win_id);
void  g_SCREEN_menu_destroy_buttonbar(int win_id);
void  g_SCREEN_menu_destroy_buttonbar2(int win_id);
void  g_SCREEN_menu_destroy_floating(int win_id);
void  g_SCREEN_menu_destroy_floating_children(int win_id);

int   g_SCREEN_menu_append_submenu(int menu_id, char *label);
int   g_SCREEN_menu_append_button(int menu_id, char *label, int return_id);
int   g_SCREEN_menu_append_floating_button(int menu_id, char *label, int return_id,
                            float vp_x, float vp_y);
int   g_SCREEN_menu_append_toggle(int menu_id, char *label, int return_id,
                                                  int toggle_on);
int   g_SCREEN_menu_append_pixbutton(int menu_id, char *pixinfo[], 
                                        char *pixgrayinfo[], int return_id);
int   g_SCREEN_menu_append_multipixbutton(int menu_id, char *pixinfo1[], 
                   char *pixinfo2[], char *pixinfo3[], char *pixinfo4[],int return_id);
int   g_SCREEN_menu_append_pixtoggle(int menu_id, char *pixinfo[], char *pixgrayinfo[],
             char *pixselectinfo[], char *pixselectgrayinfo[], int return_id, int toggle_on);
int   g_SCREEN_menu_append_separator(int menu_id);
int   g_SCREEN_menu_append_optionmenu(int menu_id, char *label, 
         int return_id, int item_count, int item_select, char *data[]);

void  g_SCREEN_menu_enable_item(int menu_id, int item_id, int enable);
int   g_SCREEN_menu_is_enabled(int menu_id, int item_id);

void  g_SCREEN_menu_set_toggle(int menu_id, int item_id, int toggle_on);
int   g_SCREEN_menu_get_toggle(int menu_id, int item_id);
int   g_SCREEN_menu_set_label(int menu_id, int item_id, char *label);
char  *g_SCREEN_menu_get_label(int menu_id, int item_id);
int   g_SCREEN_menu_set_pixmap(int menu_id, int item_id, int pix_id);

int   g_SCREEN_menu_set_group(int menu_id, int group_on);
int   g_SCREEN_menu_add_tooltip(int return_id, char *tip);


/* --- scrollbars ---*/

typedef struct {
    int   vp_count;
    int   vp_id;
    float o_x1, o_y1, o_x2, o_y2;
    float n_x1, n_y1, n_dx, n_dy;
    float xrange, yrange;
    int   poshorzscroll,posvertscroll;
    int   size_h, size_v;
    float reverse_x, reverse_y;
    long  flag;
} SCROLLSTRUCT;

#define SCROLL_GETVAL		0
#define SCROLL_LINEUP		1
#define SCROLL_LINEDOWN		2
#define SCROLL_PAGEUP		3
#define SCROLL_PAGEDOWN		4
#define SCROLL_DRAG		5
#define SMAX 10000

int  INTERNAL_ScrollBarsPresent(int id);
int  INTERNAL_GetScrollPosV(int id);
int  INTERNAL_GetScrollPosH(int id);
void INTERNAL_SetScrollPosH(int id, int pos, int size, int notify);
void INTERNAL_SetScrollPosV(int id, int pos, int size, int notify);
int  INTERNAL_SetScrollSlider(int slider);
int  INTERNAL_UpdateScrollBarsH(int,int,int);
int  INTERNAL_UpdateScrollBarsV(int,int,int);

void  g_SCREEN_scrollbar_connect(int win_id, int vp_id,
			 float wx1, float wy1, float wx2, float wy2, long flag);
int   g_SCREEN_scrollbar_reconnect(int win_id, int vp_id,
			 float wx1, float wy1, float wx2, float wy2);
int   g_SCREEN_scrollbar_get_maxworld(int win_id, int vp_id,
		         float *wx1, float *wy1, float *wx2, float *wy2);
int   g_SCREEN_scrollbar_get_zoom(int win_id, int vp_id,
		         float *zoomx, float *zoomy);
void  g_SCREEN_scrollbar_disconnect(int win_id);
void  g_SCREEN_scrollbar_reset(int win_id);
int   g_SCREEN_scrollbar_retrieve(int win_id, int ss_id);
int   g_SCREEN_scrollbar_store(int win_id);
int   g_SCREEN_scrollbar_set_dominant_viewport(int win_id, int vp_id);


/* --- popup --- */
void  INTERNAL_reset_popup(int id);
void  g_SCREEN_popup_messagebox(int win_id, char *title, char *message, int wait);
int   g_SCREEN_popup_messagebox2(int win_id, char *title, char *message,
			 char *button1text, char *button2text);
int   g_SCREEN_popup_messagebox3(int win_id, char *title, char *message,
		       char *button1text, char *button2text, char *button3text);
char  *g_SCREEN_popup_promptdialog(int win_id, char *title, char *question, char *text);
char  *g_SCREEN_popup_getfilename(int win_id, char *title, char *mask);
char  *g_SCREEN_popup_getdirname(int win_id, char *title);
char  *g_SCREEN_popup_saveas(int win_id, char *title, char *filename);
int   g_SCREEN_popup_listbox(int win_id, char *title, char *subtitle, int item_count,
		     char *items[], int select_pos, int items_visible);
int   g_SCREEN_popup_radiobox(int win_id, char *title, char *subtitle, int item_count,
		      char *items[], int select_pos);
int   g_SCREEN_popup_checkbox(int win_id, char *title, char *subtitle, int item_count,
		      char *items[], int *select);
int   g_SCREEN_popup_container_open(int win_id, char *title, int flag);
void  g_SCREEN_popup_container_close(int cont_id);
int   g_SCREEN_popup_container_show(int cont_id);
void  g_SCREEN_popup_add_child(int cont_id, G_POPUP_CHILDINFO *ci);
void  g_SCREEN_popup_enable_item(int cont_id, int item_id, int enable);
void  g_SCREEN_popup_set_selection(int cont_id, int item_id, int position);
void  g_SCREEN_popup_set_checkmark(int cont_id, int item_id, int position, int toggle_on);
void  g_SCREEN_popup_set_label(int cont_id, int item_id, char *label);
char  *g_SCREEN_popup_get_label(int cont_id, int item_id);
void  g_SCREEN_popup_set_title(int cont_id, int item_id, char *title);
void  g_SCREEN_popup_append_item(int cont_id, int item_id, char *label);
void  g_SCREEN_popup_remove_item(int cont_id, int item_id, int pos);
void  g_SCREEN_popup_replace_item(int cont_id, int item_id, int pos, char *label);
void  g_SCREEN_popup_set_buttonlabel(int cont_id, char *label, int whichbutton);

/********************************************************************************/
/* --- Console functions --- */
void  g_DUMMY_open_console(long x0,long y0,long width,long height, char *title);
void  g_DUMMY_close_console(void);
int   g_DUMMY_cons_puts(const char *string);
int   g_DUMMY_cons_fputs(const char *string, FILE *file);
int   g_DUMMY_cons_fputc(char c, FILE *file);
int   g_DUMMY_cons_printf(const char *format, ...);
int   g_DUMMY_cons_fprintf(FILE *file, const char *format, ...);
char  *g_DUMMY_cons_gets(char *buffer);
char  *g_DUMMY_cons_fgets(char *string, int maxchar, FILE *file);
int   g_DUMMY_cons_fgetc(FILE *file);

int   g_DUMMY_console_printf(int win_id, const char *format, ...);
/* --- commandline --- */

void  g_DUMMY_set_commandline(int win_id, char *command);
void  g_DUMMY_set_commandprompt(int win_id, char *prompt);
void  g_DUMMY_set_commandhistory(int win_id, char *history);
void  g_DUMMY_set_commandlocking(int win_id, int lock);
void  g_DUMMY_enable_nextcommand(int win_id);
void  g_DUMMY_enable_commandinput(int win_id, int enable);

/* --- menu's --- */

int   g_DUMMY_menu_create_menubar(int win_id);
int   g_DUMMY_menu_create_menubar2(int win_id);
int   g_DUMMY_menu_create_buttonbar(int win_id);
int   g_DUMMY_menu_create_buttonbar2(int win_id);
int   g_DUMMY_menu_create_buttonbox2(int win_id, int rows);
int   g_DUMMY_menu_create_popup(int win_id);
int   g_DUMMY_menu_create_floating(int win_id);

void  g_DUMMY_menu_select_popup(int menu_id);
void  g_DUMMY_menu_hide_popup(int win_id);

void  g_DUMMY_menu_destroy_menubar_children(int win_id);
void  g_DUMMY_menu_destroy_menubar2_children(int win_id);
void  g_DUMMY_menu_destroy_buttonbar_children(int win_id);
void  g_DUMMY_menu_destroy_buttonbar2_children(int win_id);
void  g_DUMMY_menu_destroy_popup(int win_id);
void  g_DUMMY_menu_destroy_button(int menu_id, int item_id);
void  g_DUMMY_menu_destroy_menubar2(int win_id);
void  g_DUMMY_menu_destroy_buttonbar(int win_id);
void  g_DUMMY_menu_destroy_buttonbar2(int win_id);
void  g_DUMMY_menu_destroy_floating(int win_id);
void  g_DUMMY_menu_destroy_floating_children(int win_id);

int   g_DUMMY_menu_append_submenu(int menu_id, char *label);
int   g_DUMMY_menu_append_button(int menu_id, char *label, int return_id);
int   g_DUMMY_menu_append_floating_button(int menu_id, char *label, int return_id,
                            float vp_x, float vp_y);
int   g_DUMMY_menu_append_toggle(int menu_id, char *label, int return_id,
                                                  int toggle_on);
int   g_DUMMY_menu_append_pixbutton(int menu_id, char *pixinfo[], 
                                        char *pixgrayinfo[], int return_id);
int   g_DUMMY_menu_append_multipixbutton(int menu_id, char *pixinfo1[], 
                   char *pixinfo2[], char *pixinfo3[], char *pixinfo4[],int return_id);
int   g_DUMMY_menu_append_pixtoggle(int menu_id, char *pixinfo[], char *pixgrayinfo[],
             char *pixselectinfo[], char *pixselectgrayinfo[], int return_id, int toggle_on);
int   g_DUMMY_menu_append_separator(int menu_id);
int   g_DUMMY_menu_append_optionmenu(int menu_id, char *label, 
         int return_id, int item_count, int item_select, char *data[]);

void  g_DUMMY_menu_enable_item(int menu_id, int item_id, int enable);
int   g_DUMMY_menu_is_enabled(int menu_id, int item_id);

void  g_DUMMY_menu_set_toggle(int menu_id, int item_id, int toggle_on);
int   g_DUMMY_menu_get_toggle(int menu_id, int item_id);
int   g_DUMMY_menu_set_label(int menu_id, int item_id, char *label);
char  *g_DUMMY_menu_get_label(int menu_id, int item_id);
int   g_DUMMY_menu_set_pixmap(int menu_id, int item_id, int pix_id);

int   g_DUMMY_menu_set_group(int mneu_id, int group_on);
int   g_DUMMY_menu_add_tooltip(int return_id, char *tip);


/* --- scrollbars ---*/
void  g_DUMMY_scrollbar_connect(int win_id, int vp_id,
                         float wx1, float wy1, float wx2, float wy2, long flag);
int   g_DUMMY_scrollbar_reconnect(int win_id, int vp_id,
                         float wx1, float wy1, float wx2, float wy2);
int   g_DUMMY_scrollbar_get_maxworld(int win_id, int vp_id,
		         float *wx1, float *wy1, float *wx2, float *wy2);
int   g_DUMMY_scrollbar_get_zoom(int win_id, int vp_id,
		         float *zoomx, float *zoomy);
void  g_DUMMY_scrollbar_disconnect(int win_id);
void  g_DUMMY_scrollbar_reset(int win_id);
int   g_DUMMY_scrollbar_retrieve(int win_id, int ss_id);
int   g_DUMMY_scrollbar_store(int win_id);
int   g_DUMMY_scrollbar_set_dominant_viewport(int win_id, int vp_id);


/* --- popup --- */
void  g_DUMMY_popup_messagebox(int win_id, char *title, char *message, int wait);
int   g_DUMMY_popup_messagebox2(int win_id, char *title, char *message, 
                         char *button1text, char *button2text);
int   g_DUMMY_popup_messagebox3(int win_id, char *title, char *message, 
                       char *button1text, char *button2text, char *button3text);
char  *g_DUMMY_popup_promptdialog(int win_id, char *title, char *question, char *text);
char  *g_DUMMY_popup_getfilename(int win_id, char *title, char *mask);
char  *g_DUMMY_popup_getdirname(int win_id, char *title);
char  *g_DUMMY_popup_saveas(int win_id, char *title, char *filename);
int   g_DUMMY_popup_listbox(int win_id, char *title, char *subtitle, int item_count, 
                     char *items[], int select_pos, int items_visible);
int   g_DUMMY_popup_radiobox(int win_id, char *title, char *subtitle, int item_count, 
                      char *items[], int select_pos);
int   g_DUMMY_popup_checkbox(int win_id, char *title, char *subtitle, int item_count, 
                      char *items[], int *select);
int   g_DUMMY_popup_container_open(int win_id, char *title, int flag);
void  g_DUMMY_popup_container_close(int cont_id);
int   g_DUMMY_popup_container_show(int cont_id);
void  g_DUMMY_popup_add_child(int cont_id, G_POPUP_CHILDINFO *ci);
void  g_DUMMY_popup_enable_item(int cont_id, int item_id, int enable);
void  g_DUMMY_popup_set_selection(int cont_id, int item_id, int position);
void  g_DUMMY_popup_set_checkmark(int cont_id, int item_id, int position, int toggle_on);
void  g_DUMMY_popup_set_label(int cont_id, int item_id, char *label);
char  *g_DUMMY_popup_get_label(int cont_id, int item_id);
void  g_DUMMY_popup_set_title(int cont_id, int item_id, char *title);
void  g_DUMMY_popup_append_item(int cont_id, int item_id, char *label);
void  g_DUMMY_popup_remove_item(int cont_id, int item_id, int pos);
void  g_DUMMY_popup_replace_item(int cont_id, int item_id, int pos, char *label);
void  g_DUMMY_popup_set_buttonlabel(int cont_id, char *label, int whichbutton);


#endif



