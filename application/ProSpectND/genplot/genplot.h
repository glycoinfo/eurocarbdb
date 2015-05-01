/************************************************************************/
/*                               genplot.h                              */
/*                                                                      */
/*  Platform : All                                                      */
/*  Module   : General include file                                     */
/*                                                                      */
/*  Albert van Kuik                                                     */
/************************************************************************/

#ifndef _GENPLOT_H
#define _GENPLOT_H

#define TRUE            1
#define FALSE           0
#define G_ERROR        -1
#define G_OK            0

/* --- Device --- */
#define G_DUMMY         0
#define G_SCREEN        1
#define G_HPGL          2
#define G_POSTSCRIPT    3
#define G_WMF		4
#ifdef _WIN32
#define G_PRINTER	5
#else
#define G_PRINTER 	G_DUMMY
#endif

/* --- Window Flags --- */
#define G_WIN_PORTRAIT      	(1L<<0)
#define G_WIN_SCROLLBAR     	(1L<<1)
#define G_WIN_NORESIZE      	(1L<<2)
#define G_WIN_EPS           	(1L<<3)
#define G_WIN_HPGL2         	(1L<<4)
#define G_WIN_CONSOLE       	(1L<<5)
#define G_WIN_MENUBAR       	(1L<<6)
#define G_WIN_PCL5          	(1L<<7)
#define G_WIN_COMMANDLINE   	(1L<<8)
#define G_WIN_COMMANDLINE_RETURN (G_WIN_COMMANDLINE | (1L<<9))
#define G_WIN_BUFFER            (1L<<10)
#define G_WIN_PRIMARY_SHELL     (1L<<11)
#define G_WIN_A4		(1L<<12)
#define G_WIN_A3		(1L<<13)
#define G_WIN_BLACK_ON_WHITE	(1L<<14)
#define G_WIN_BUTTONBAR		(1L<<15)
#define G_WIN_POINTERFOCUS	(1L<<16)
#define G_WIN_MENUBAR2       	(1L<<17)
#define G_WIN_BUTTONBAR_32    	(G_WIN_BUTTONBAR | (1L<<18))
#define G_WIN_BUTTONBAR2	(1L<<19)
#define G_WIN_CHILD		(1L<<20)
#define G_WIN_COMMANDLINE_FOCUS	(1L<<21)
#define G_WIN_COMMANDLINE_HIDEHISTORY	(1L<<22)
#define G_WIN_LETTER		(1L<<23)
#define G_WIN_LEGAL		(1L<<24)

/* --- Linestyle --- */
#define G_SOLID         0xFFFF
#define G_SHORT_DASHED  0xE0E0
#define G_LONG_DASHED   0xF8F8
#define G_DOTTED        0x1818

#define G_MAXWINDOW        10

/* --- Genplot Colors --- */
#define G_BLACK            0
#define G_BLUE             1
#define G_GREEN            2
#define G_CYAN             3
#define G_RED              4
#define G_MAGENTA          5
#define G_BROWN            6
#define G_LIGHTGRAY        7
#define G_DARKGRAY         8
#define G_LIGHTBLUE        9
#define G_LIGHTGREEN       10
#define G_LIGHTCYAN        11
#define G_LIGHTRED         12
#define G_LIGHTMAGENTA     13
#define G_YELLOW           14
#define G_WHITE            15

typedef struct {
    unsigned char r;
    unsigned char g;
    unsigned char b;
} G_PALETTEENTRY;

/* --- fonts --- */
#define G_FONT_TIMES                    0
#define G_FONT_TIMES_ITALIC             1
#define G_FONT_TIMES_BOLD               2
#define G_FONT_TIMES_BOLD_ITALIC        3
#define G_FONT_HELVETICA                4
#define G_FONT_HELVETICA_ITALIC         5
#define G_FONT_HELVETICA_BOLD           6
#define G_FONT_HELVETICA_BOLD_ITALIC    7
#define G_FONT_COURIER                  8
#define G_FONT_COURIER_ITALIC           9
#define G_FONT_COURIER_BOLD             10
#define G_FONT_COURIER_BOLD_ITALIC      11
#define G_FONT_SYMBOL                   12

#define G_MAXFONT                       12

/* --- The default pointsize --- */
#define G_POINTSIZE                     12
#define G_POINTSIZE_SCALING             30.0

/* --- scale font in relation to ... --- */
#define G_ABSOLUTE_FONTSCALING            0
#define G_RELATIVE_FONTSCALING            1

/* --- return from g_getevent() --- */
#define G_BUTTON1PRESS    (1<<0)
#define G_BUTTON2PRESS    (1<<1)
#define G_BUTTON3PRESS    (1<<2)
#define G_BUTTON1RELEASE  (1<<3)
#define G_BUTTON2RELEASE  (1<<4)
#define G_BUTTON3RELEASE  (1<<5)
#define G_KEYPRESS        (1<<6)
#define G_POINTERMOTION   (1<<7)
#define G_COMMAND         (1<<8)
#define G_COMMANDLINE     (1<<9)
#define G_WINDOWCREATE    (1<<10)
#define G_WINDOWDESTROY   (1<<11)
#define G_OPTIONMENU      (1<<12)
#define G_WINDOWQUIT      (1<<13)

/* --- cursor types --- */
#define G_CURSOR_DEFAULT      0
#define G_CURSOR_CROSSHAIR    1
#define G_CURSOR_HAND         2
#define G_CURSOR_WAIT         3
#define G_CURSOR_UPDOWN       4

/* --- rubber box and line cursor types --- */
#define G_RUBBER_NONE            0
#define G_RUBBER_CROSSHAIR       1
#define G_RUBBER_BOX             2
#define G_RUBBER_LINE            3
#define G_RUBBER_BOX_BL          4
#define G_RUBBER_BOX_BR          5
#define G_RUBBER_BOX_TL          6
#define G_RUBBER_BOX_TR          7
#define G_RUBBER_PANNER          8
#define G_RUBBER_PANNER_BORDER   9
#define G_RUBBER_XHAIR		 10
#define G_RUBBER_YHAIR		 11
#define G_RUBBER_DOUBLECROSS	 12

#define G_MAX_STORE_CURSOR  20

/* --- object status ---*/
#define G_AWAKE               0
#define G_SLEEP               1

/* --- menu's --- */
#define G_MENU_ALLITEMS       -2

/* --- scrollbar flags --- */
#define G_SCROLL_LASTVIEWPORT	(1L<<0)
#define G_SCROLL_CLEARVIEWPORT	(1L<<1)
#define G_SCROLL_XAXIS		(1L<<2)
#define G_SCROLL_YAXIS		(1L<<3)
#define G_SCROLL_CORRESPONDING	(1L<<4)

/* --- popup keys --- */
#define G_POPUP_BUTTON1		1
#define G_POPUP_BUTTON2		2
#define G_POPUP_BUTTON3		3

/* --- popup container box buttons --- */
#define G_POPUP_BUTTON_OK	1
#define G_POPUP_BUTTON_CANCEL	2

#define G_POPUP_WAIT		(1<<1)
#define G_POPUP_KEEP		(1<<2)
#define G_POPUP_SINGLEBUTTON	(1<<3)
#define G_POPUP_SCROLL		(1<<4)
#define G_POPUP_TAB			(1<<5)

/* --- container box children --- */
#define G_CHILD_CHECKBOX	1
#define G_CHILD_RADIOBOX	2
#define G_CHILD_TEXTBOX		3
#define G_CHILD_OPTIONMENU	4
#define G_CHILD_LISTBOX		5
#define G_CHILD_LABEL		6
#define G_CHILD_SEPARATOR	7
#define G_CHILD_SCALE		8
#define G_CHILD_SPINBOX		9
#define G_CHILD_SPINBOXTEXT	10
#define G_CHILD_PUSHBUTTON	11
#define G_CHILD_GETFILENAME	12
#define G_CHILD_MULTILINETEXT	13
#define G_CHILD_OK		14
#define G_CHILD_CANCEL		15
#define G_CHILD_PANEL		16
#define G_CHILD_GETDIRNAME	17
#define G_CHILD_TAB		18


typedef struct G_POPUP_CHILDINFO {
    int  type;
    int  id;
    int  cont_id;
    int  win_id;
    int  item_count;
    int  items_visible;
    int  item;
    int  disabled;
    int  horizontal;
    int  frame;
    char *title;
    int  *select;
    char *label;
    char **data;
    void (*func)(struct G_POPUP_CHILDINFO *);
    void *userdata;
} G_POPUP_CHILDINFO;

typedef void (*G_CALLDATA)();

typedef struct {
    int event;
    int win_id;
    int vp_id;
    float x, y;
    unsigned int keycode;
    unsigned int item;
    char *command;
} G_EVENT;

#define G_USERINPUT G_EVENT

/* --- keyboard focus --- */
#define G_FOCUS_DRAWINGAREA	1
#define G_FOCUS_COMMANDLINE	2
#define G_FOCUS_CONSOLEAREA	3


extern void (*g_moveto)(float x, float y);
extern void (*g_lineto)(float x, float y);
extern void (*g_label)(char *label);
extern void (*g_rectangle)(float x1, float y1, float x2, float y2);
extern void (*g_drawpoly)(long numpoint, float *points);
extern void (*g_circle)(float r);
extern void (*g_fillrectangle)(float x1, float y1, float x2, float y2);
extern void (*g_fillpoly)(long numpoint, float *points);
extern void (*g_fillcircle)(float r);
extern void (*g_set_textdirection)(int direction);
extern void (*g_set_linewidth)(int width);
extern void (*g_set_linestyle)(int pattern);
extern void (*g_set_foreground)(int color);
extern void (*g_set_background)(int color);
extern void (*g_set_font)(int font, int scale_to);
extern void (*g_set_charsize)(float size);
extern void (*g_set_clipping)(int clip);
extern int  (*g_set_world)(int no,float x1,float y1,float x2,float y2);
extern int  (*g_set_viewport)(int no,float x1,float y1,float x2,float y2);
extern int  (*g_select_viewport)(int no);
extern void (*g_clear_viewport)(void);
extern int  (*g_set_palettesize)(int size);
extern int  (*g_set_paletteentry)(int entry_id, G_PALETTEENTRY entry);


int  g_open_object(int id);
int  g_close_object(int id);
int  g_append_object(int id);
int  g_delete_object(int id);
int  g_is_object(int id);
int  g_unique_object(void);
int  g_set_objectstatus(int id, int status);
extern int (*g_call_object)(int id);
int  g_count_objects(void);
void g_list_objects(int *id, int *status);

extern int  (*g_call_object_byname)(char *label);
int   g_open_object_byname(int id, char *label);
int   g_close_object_byname(char *label);
int   g_append_object_byname(char *label);
int   g_delete_object_byname(char *label);
int   g_set_objectstatus_byname(char *label, int status);
int   g_count_objects_byname(void);
void  g_list_objects_byname(char **list);

int   g_get_object_id(char *label);
char *g_get_object_name(int id);

void  g_create_object_file(char *name, char *ident, int compress);
int   g_open_object_file(char *name, int w);
char *g_get_object_file_ident(int file_id);
void  g_close_object_file(int file_id);
int   g_read_fileobject(int file_id, int obj_id, char *label, 
                        int with_children, int replace);
int   g_write_fileobject(int file_id, char *label, int with_children, 
                         int replace);
int   g_erase_fileobject(int file_id, char *label);
int   g_first_fileobject(int file_id, char *label);
int   g_next_fileobject(int file_id, char *label);
int   g_prev_fileobject(int file_id, char *label);
int   g_last_fileobject(int file_id, char *label);


void  g_set_outfile(FILE *outfile);
void  g_set_cursortype(int win, int type);
void  g_set_cursorposition(float x, float y);
int   g_set_devicesize(int device, float width, float height);
void  g_set_rubbercursor(int win_id, int vp_id, float x, float y, int type);
void  g_set_keyboard_focus(int win_id, int focus, int locked);

FILE *g_get_outfile(void);
int   g_get_textdirection(void);
void  g_get_world(int vp_id,float *x1,float *y1,float *x2,float *y2);
void  g_get_viewport(int vp_id,float *x1,float *y1,float *x2,float *y2);
int   g_get_linewidth(void);
int   g_get_linestyle(void);
int   g_get_palettesize(void);
int   g_get_paletteentry(int entry_id, G_PALETTEENTRY *entry);
int   g_get_foreground(void);
int   g_get_background(void);
float g_get_charsize(void);
int   g_get_clipping(void);
int   g_get_cursortype(int win);
int   g_get_cursorposition(float *x, float *y);
int   g_get_windowsize(int win_id, float *width, float *height);
int   g_get_devicesize(int device, float *width, float *height);
int   g_get_devicenr(void);
int   g_get_windownr(void);
int   g_get_viewportnr(void);
void  g_get_xy(float *x, float *y);
void  g_get_font(int *font, int *scale_type);
float g_get_fontheight(int win_id);

int   g_peek_event(void);
#define g_peek_userinput g_peek_event
int   g_get_event(G_EVENT *ui);
#define g_userinput g_get_event
void  g_send_event(G_EVENT *ui);
void  g_add_eventhandler(int event_mask, 
                         int (*eventfunc)(G_EVENT *ui,G_CALLDATA), 
                         G_CALLDATA call_data);
void  g_remove_eventhandler(int event_mask);

void  g_set_motif_realtextrotation(int doit);

void  g_bell(void);
void  g_storecursor(int store_id);
void  g_retrievecursor(int store_id);
void  g_flush(void);
void  g_plotall(void);
void  g_newpage(void);
int   g_open_window(int device,float x0,float y0,float width,
                    float height,char *title,long flag);
void  g_close_window(int id);
int   g_select_window(int id);
int   g_raise_window(int id);
int   g_copy_window(int dest, int src);
void  g_set_windowbuffer(int win_id, int doit);
int   g_open_device(int device);
void  g_close_device(int device);
void  g_init(int argc, char **argv);
void  g_end(void);
void  g_set_icon_pixmap(char *icon_64[], char *icon_48[],
                       char *icon_32[], char *icon_16[]);
 
int   g_count_viewports_in_window(int win_id);
int   g_get_viewports_in_window(int win_id, int *vp_list);
void  g_translate_viewport_xy(int win_id, int vp_id_to, int vp_id_from,
                           float *x, float *y);

extern int (*g_push_gc)(void);
extern int (*g_pop_gc)(void);
extern int (*g_push_viewport)(void);
extern int (*g_pop_viewport)(void);

#ifdef _WIN32
/* --- Console functions --- */
void  g_open_console(long x0,long y0,long width,long height, char *title);
void  g_close_console(void);
int   g_cons_puts(const char *string);
int   g_cons_fputs(const char *string, FILE *file);
int   g_cons_fputc(char c, FILE *file);
int   g_cons_printf(const char *format, ...);
int   g_cons_fprintf(FILE *file, const char *format, ...);
char  *g_cons_gets(char *buffer);
char  *g_cons_fgets(char *string, int maxchar, FILE *file);
int   g_cons_fgetc(FILE *file);


#ifndef G_LOCAL
#define puts            g_cons_puts
#define fputs           g_cons_fputs
#define fputc           g_cons_fputc
#define printf          g_cons_printf
#define fprintf         g_cons_fprintf
#define gets            g_cons_gets
#define fgets           g_cons_fgets
#define fgetc           g_cons_fgetc

#ifdef getc
#undef getc
#endif
#ifdef getchar
#undef getchar
#endif
#ifdef putchar
#undef putchar
#endif
#ifdef putc
#undef putc
#endif

#define getc(f)         g_cons_fgetc(f)
#define getchar()       g_cons_fgetc(stdin)
#define putchar(c)      g_cons_fputc((c), stdout)
#define putc(c, f)      g_cons_fputc((c),(f))

#endif  /* G_LOCAL */

#endif /* _WIN32 */

/* --- console --- */
extern int (*g_console_printf)(int win_id, const char *format, ...);

/* --- commandline --- */

extern void  (*g_set_commandline)(int win_id, char *command);
extern void  (*g_set_commandprompt)(int win_id, char *prompt);
extern void  (*g_set_commandhistory)(int win_id, char *history);
extern void  (*g_set_commandlocking)(int win_id, int lock);
extern void  (*g_enable_nextcommand)(int win_id);
extern void  (*g_enable_commandinput)(int win_id, int enable);

/* --- menu's --- */
extern int   (*g_menu_create_menubar)(int win_id);
extern int   (*g_menu_create_menubar2)(int win_id);
extern int   (*g_menu_create_buttonbar)(int win_id);
extern int   (*g_menu_create_buttonbar2)(int win_id);
extern int   (*g_menu_create_buttonbox2)(int win_id, int rows);
extern int   (*g_menu_create_popup)(int win_id);
extern int   (*g_menu_create_floating)(int win_id);

extern void  (*g_menu_select_popup)(int menu_id);
extern void  (*g_menu_hide_popup)(int win_id);

extern void  (*g_menu_destroy_menubar_children)(int win_id);
extern void  (*g_menu_destroy_menubar2_children)(int win_id);
extern void  (*g_menu_destroy_buttonbar_children)(int win_id);
extern void  (*g_menu_destroy_buttonbar2_children)(int win_id);
extern void  (*g_menu_destroy_popup)(int win_id);
extern void  (*g_menu_destroy_button)(int menu_id, int item_id);
extern void  (*g_menu_destroy_menubar2)(int win_id);
extern void  (*g_menu_destroy_buttonbar)(int win_id);
extern void  (*g_menu_destroy_buttonbar2)(int win_id);
extern void  (*g_menu_destroy_floating)(int win_id);
extern void  (*g_menu_destroy_floating_children)(int win_id);


extern int   (*g_menu_append_submenu)(int menu_id, char *label);
extern int   (*g_menu_append_button)(int menu_id, char *label, int return_id);
extern int   (*g_menu_append_floating_button)(int menu_id, char *label, int return_id,
                                       float vp_x, float vp_y);
extern int   (*g_menu_append_toggle)(int menu_id, char *label, int return_id,
                                                  int toggle_on);
extern int   (*g_menu_append_pixbutton)(int menu_id, char *pixinfo[], 
                                        char *pixgrayinfo[], int return_id);
extern int   (*g_menu_append_multipixbutton)(int menu_id, char *pixinfo1[], 
                   char *pixinfo2[], char *pixinfo3[], char *pixinfo4[], int return_id);
extern int   (*g_menu_append_pixtoggle)(int menu_id, char *pixinfo[], char *pixgrayinfo[],
             char *pixselectinfo[], char *pixselectgrayinfo[], int return_id, int toggle_on);
extern int   (*g_menu_append_separator)(int menu_id);
extern int   (*g_menu_append_optionmenu)(int menu_id, char *label, 
             int return_id, int item_count, int item_select, char *data[]);

extern void  (*g_menu_enable_item)(int menu_id, int item_id, int enable);
extern int   (*g_menu_is_enabled)(int menu_id, int item_id);

extern void  (*g_menu_set_toggle)(int menu_id, int item_id, int toggle_on);
extern int   (*g_menu_get_toggle)(int menu_id, int item_id);
extern int   (*g_menu_set_label)(int menu_id, int item_id, char *label);
extern char  *(*g_menu_get_label)(int menu_id, int item_id);
extern int   (*g_menu_set_pixmap)(int menu_id, int item_id, int pix_id);

extern int   (*g_menu_set_group)(int menu_id, int group_on);
extern int   (*g_menu_add_tooltip)(int return_id, char *tip);

/* --- scrollbars ---*/
extern void  (*g_scrollbar_connect)(int win_id, int vp_id,
                         float wx1, float wy1, float wx2, float wy2, long flag);
extern int   (*g_scrollbar_reconnect)(int win_id, int vp_id,
                         float wx1, float wy1, float wx2, float wy2);
extern int   (*g_scrollbar_get_maxworld)(int win_id, int vp_id,
                         float *wx1, float *wy1, float *wx2, float *wy2);
extern int   (*g_scrollbar_get_zoom)(int win_id, int vp_id,
                         float *zoomx, float *zoomy);
extern void  (*g_scrollbar_disconnect)(int win_id);
extern void  (*g_scrollbar_reset)(int win_id);
extern int   (*g_scrollbar_retrieve)(int win_id, int cc_id);
extern int   (*g_scrollbar_store)(int win_id);
extern int   (*g_scrollbar_set_dominant_viewport)(int win_id, int vp_id);


/* --- popup --- */
extern void  (*g_popup_messagebox)(int win_id, char *title, char *message, int wait);
extern int   (*g_popup_messagebox2)(int win_id, char *title, char *message, 
                         char *button1text, char *button2text);
extern int   (*g_popup_messagebox3)(int win_id, char *title, char *message, 
                       char *button1text, char *button2text, char *button3text);
extern char  *(*g_popup_promptdialog)(int win_id, char *title, char *question, char *text);
extern char  *(*g_popup_getfilename)(int win_id, char *title, char *mask);
extern char  *(*g_popup_getdirname)(int win_id, char *title);
extern char  *(*g_popup_saveas)(int win_id, char *title, char *filename);
extern int   (*g_popup_listbox)(int win_id, char *title, char *subtitle, int item_count, 
                     char *items[], int select_pos, int items_visible);
extern int   (*g_popup_radiobox)(int win_id, char *title, char *subtitle, int item_count, 
                      char *items[], int select_pos);
extern int   (*g_popup_checkbox)(int win_id, char *title, char *subtitle, int item_count, 
                      char *items[], int *select);
extern int   (*g_popup_container_open)(int win_id, char *title, int flag);
extern void  (*g_popup_container_close)(int cont_id);
extern int   (*g_popup_container_show)(int cont_id);
extern void  (*g_popup_add_child)(int cont_id, G_POPUP_CHILDINFO *ci);
void           g_popup_init_info(G_POPUP_CHILDINFO *ci);
extern void  (*g_popup_enable_item)(int cont_id, int item_id, int enable);
extern void  (*g_popup_set_selection)(int cont_id, int item_id, int position);
extern void  (*g_popup_set_checkmark)(int cont_id, int item_id, int position, int toggle_on);
extern void  (*g_popup_set_label)(int cont_id, int item_id, char *label);
extern char  *(*g_popup_get_label)(int cont_id, int item_id);
extern void  (*g_popup_set_title)(int cont_id, int item_id, char *title);
extern void  (*g_popup_append_item)(int cont_id, int item_id, char *label);
extern void  (*g_popup_remove_item)(int cont_id, int item_id, int pos);
extern void  (*g_popup_replace_item)(int cont_id, int item_id, int pos, char *label);
extern void  (*g_popup_set_buttonlabel)(int cont_id, char *label, int whichbutton);
void           g_popup_follow_cursor(int doit);

#endif  /* _GENPLOT_H */


