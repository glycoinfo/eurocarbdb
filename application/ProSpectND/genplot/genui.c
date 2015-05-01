/************************************************************************/
/*                               genui.c                                */
/*                                                                      */
/*  Platform : All                                                      */
/*  Module   : Genplot common User Interface functons                   */
/*                                                                      */
/*  Albert van Kuik                                                     */
/************************************************************************/
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

/* --- Console functions --- */
int   (*g_console_printf)(int win_id, const char *format, ...);

/* --- commandline --- */

void  (*g_set_commandline)(int win_id, char *command);
void  (*g_set_commandprompt)(int win_id, char *prompt);
void  (*g_set_commandhistory)(int win_id, char *history);
void  (*g_set_commandlocking)(int win_id, int lock);
void  (*g_enable_nextcommand)(int win_id);
void  (*g_enable_commandinput)(int win_id, int enable);

/* --- menu's --- */

int   (*g_menu_create_menubar)(int win_id);
int   (*g_menu_create_menubar2)(int win_id);
int   (*g_menu_create_buttonbar)(int win_id);
int   (*g_menu_create_buttonbar2)(int win_id);
int   (*g_menu_create_buttonbox2)(int win_id, int rows);
int   (*g_menu_create_popup)(int win_id);
int   (*g_menu_create_floating)(int win_id);

void  (*g_menu_select_popup)(int menu_id);
void  (*g_menu_hide_popup)(int win_id);

void  (*g_menu_destroy_menubar_children)(int win_id);
void  (*g_menu_destroy_menubar2_children)(int win_id);
void  (*g_menu_destroy_buttonbar_children)(int win_id);
void  (*g_menu_destroy_buttonbar2_children)(int win_id);
void  (*g_menu_destroy_popup)(int win_id);
void  (*g_menu_destroy_button)(int menu_id, int item_id);
void  (*g_menu_destroy_menubar2)(int win_id);
void  (*g_menu_destroy_buttonbar)(int win_id);
void  (*g_menu_destroy_buttonbar2)(int win_id);
void  (*g_menu_destroy_floating)(int win_id);
void  (*g_menu_destroy_floating_children)(int win_id);

int   (*g_menu_append_submenu)(int menu_id, char *label);
int   (*g_menu_append_button)(int menu_id, char *label, int return_id);
int   (*g_menu_append_floating_button)(int menu_id, char *label, int return_id,
                                       float vp_x, float vp_y);
int   (*g_menu_append_toggle)(int menu_id, char *label, int return_id,
                                                  int toggle_on);
int   (*g_menu_append_pixbutton)(int menu_id, char *pixinfo[], 
                                        char *pixgrayinfo[], int return_id);
int   (*g_menu_append_multipixbutton)(int menu_id, char *pixinfo1[], 
                   char *pixinfo2[], char *pixinfo3[], char *pixinfo4[], int return_id);
int   (*g_menu_append_pixtoggle)(int menu_id, char *pixinfo[], char *pixgrayinfo[],
             char *pixselectinfo[], char *pixselectgrayinfo[], int return_id, int toggle_on);
int   (*g_menu_append_separator)(int menu_id);
int   (*g_menu_append_optionmenu)(int menu_id, char *label, 
             int return_id, int item_count, int item_select, char *data[]);

void  (*g_menu_enable_item)(int menu_id, int item_id, int enable);
int   (*g_menu_is_enabled)(int menu_id, int item_id);

void  (*g_menu_set_toggle)(int menu_id, int item_id, int toggle_on);
int   (*g_menu_get_toggle)(int menu_id, int item_id);
int   (*g_menu_set_label)(int menu_id, int item_id, char *label);
char  *(*g_menu_get_label)(int menu_id, int item_id);
int   (*g_menu_set_pixmap)(int menu_id, int item_id, int pix_id);

int   (*g_menu_set_group)(int menu_id, int group_on);
int   (*g_menu_add_tooltip)(int return_id, char *tip);

/* --- scrollbars ---*/
void  (*g_scrollbar_connect)(int win_id, int vp_id,
                         float wx1, float wy1, float wx2, float wy2, long flag);
int   (*g_scrollbar_reconnect)(int win_id, int vp_id,
                         float wx1, float wy1, float wx2, float wy2);
int   (*g_scrollbar_get_maxworld)(int win_id, int vp_id,
		         float *wx1, float *wy1, float *wx2, float *wy2);
int   (*g_scrollbar_get_zoom)(int win_id, int vp_id,
		         float *zoomx, float *zoomy);
void  (*g_scrollbar_disconnect)(int win_id);
void  (*g_scrollbar_reset)(int win_id);
int   (*g_scrollbar_retrieve)(int win_id, int cc_id);
int   (*g_scrollbar_store)(int win_id);
int   (*g_scrollbar_set_dominant_viewport)(int win_id, int vp_id);


/* --- popup --- */
void  (*g_popup_messagebox)(int win_id, char *title, char *message, int wait);
int   (*g_popup_messagebox2)(int win_id, char *title, char *message, 
                         char *button1text, char *button2text);
int   (*g_popup_messagebox3)(int win_id, char *title, char *message,
                       char *button1text, char *button2text, char *button3text);
char  *(*g_popup_promptdialog)(int win_id, char *title, char *question, char *text);
char  *(*g_popup_getfilename)(int win_id, char *title, char *mask);
char  *(*g_popup_getdirname)(int win_id, char *title);
char  *(*g_popup_saveas)(int win_id, char *title, char *filename);
int   (*g_popup_listbox)(int win_id, char *title, char *subtitle, int item_count, 
                     char *items[], int select_pos, int items_visible);
int   (*g_popup_radiobox)(int win_id, char *title, char *subtitle, int item_count, 
                      char *items[], int select_pos);
int   (*g_popup_checkbox)(int win_id, char *title, char *subtitle, int item_count, 
                      char *items[], int *select);
int   (*g_popup_container_open)(int win_id, char *title, int flag);
void  (*g_popup_container_close)(int cont_id);
int   (*g_popup_container_show)(int cont_id);
void  (*g_popup_add_child)(int cont_id, G_POPUP_CHILDINFO *ci);
void  (*g_popup_enable_item)(int cont_id, int item_id, int enable);
void  (*g_popup_set_selection)(int cont_id, int item_id, int position);
void  (*g_popup_set_checkmark)(int cont_id, int item_id, int position, int toggle_on);
void  (*g_popup_set_label)(int cont_id, int item_id, char *label);
char  *(*g_popup_get_label)(int cont_id, int item_id);
void  (*g_popup_set_title)(int cont_id, int item_id, char *title);
void  (*g_popup_append_item)(int cont_id, int item_id, char *label);
void  (*g_popup_remove_item)(int cont_id, int item_id, int pos);
void  (*g_popup_replace_item)(int cont_id, int item_id, int pos, char *label);
void  (*g_popup_set_buttonlabel)(int cont_id, char *label, int whichbutton);

void set_DUMMY_mode(void)
{
    g_console_printf = g_DUMMY_console_printf;

    g_set_commandline = g_DUMMY_set_commandline;
    g_set_commandprompt = g_DUMMY_set_commandprompt;
    g_set_commandhistory = g_DUMMY_set_commandhistory;
    g_set_commandlocking = g_DUMMY_set_commandlocking;
    g_enable_nextcommand = g_DUMMY_enable_nextcommand;
    g_enable_commandinput = g_DUMMY_enable_commandinput;
    g_menu_create_menubar = g_DUMMY_menu_create_menubar;
    g_menu_create_menubar2 = g_DUMMY_menu_create_menubar2;
    g_menu_create_buttonbar = g_DUMMY_menu_create_buttonbar;
    g_menu_create_buttonbar2 = g_DUMMY_menu_create_buttonbar2;
    g_menu_create_buttonbox2 = g_DUMMY_menu_create_buttonbox2;
    g_menu_create_popup = g_DUMMY_menu_create_popup;
    g_menu_create_floating = g_DUMMY_menu_create_floating;
    g_menu_select_popup = g_DUMMY_menu_select_popup;
    g_menu_hide_popup = g_DUMMY_menu_hide_popup;
    g_menu_destroy_menubar_children = g_DUMMY_menu_destroy_menubar_children;
    g_menu_destroy_menubar2_children = g_DUMMY_menu_destroy_menubar2_children;
    g_menu_destroy_buttonbar_children = g_DUMMY_menu_destroy_buttonbar_children;
    g_menu_destroy_buttonbar2_children = g_DUMMY_menu_destroy_buttonbar2_children;
    g_menu_destroy_popup = g_DUMMY_menu_destroy_popup;
    g_menu_destroy_button = g_DUMMY_menu_destroy_button;
    g_menu_destroy_menubar2 = g_DUMMY_menu_destroy_menubar2;
    g_menu_destroy_buttonbar = g_DUMMY_menu_destroy_buttonbar;
    g_menu_destroy_buttonbar2 = g_DUMMY_menu_destroy_buttonbar2;
    g_menu_destroy_floating = g_DUMMY_menu_destroy_floating;
    g_menu_destroy_floating_children = g_DUMMY_menu_destroy_floating_children;
    g_menu_append_submenu = g_DUMMY_menu_append_submenu;
    g_menu_append_button = g_DUMMY_menu_append_button;
    g_menu_append_floating_button = g_DUMMY_menu_append_floating_button;
    g_menu_append_toggle = g_DUMMY_menu_append_toggle;
    g_menu_append_pixbutton = g_DUMMY_menu_append_pixbutton;
    g_menu_append_multipixbutton = g_DUMMY_menu_append_multipixbutton;
    g_menu_append_pixtoggle = g_DUMMY_menu_append_pixtoggle;
    g_menu_append_separator = g_DUMMY_menu_append_separator;
    g_menu_append_optionmenu = g_DUMMY_menu_append_optionmenu;
    g_menu_enable_item = g_DUMMY_menu_enable_item;
    g_menu_is_enabled = g_DUMMY_menu_is_enabled;
    g_menu_set_toggle = g_DUMMY_menu_set_toggle;
    g_menu_get_toggle = g_DUMMY_menu_get_toggle;
    g_menu_set_label = g_DUMMY_menu_set_label;
    g_menu_get_label = g_DUMMY_menu_get_label;
    g_menu_set_pixmap = g_DUMMY_menu_set_pixmap;
    g_menu_set_group = g_DUMMY_menu_set_group;
    g_menu_add_tooltip = g_DUMMY_menu_add_tooltip;
    g_scrollbar_connect = g_DUMMY_scrollbar_connect;
    g_scrollbar_reconnect = g_DUMMY_scrollbar_reconnect;
    g_scrollbar_get_maxworld = g_DUMMY_scrollbar_get_maxworld;
    g_scrollbar_get_zoom = g_DUMMY_scrollbar_get_zoom;
    g_scrollbar_disconnect = g_DUMMY_scrollbar_disconnect;
    g_scrollbar_reset = g_DUMMY_scrollbar_reset;
    g_scrollbar_retrieve = g_DUMMY_scrollbar_retrieve;
    g_scrollbar_store = g_DUMMY_scrollbar_store;
    g_scrollbar_set_dominant_viewport = g_DUMMY_scrollbar_set_dominant_viewport;
    g_popup_messagebox = g_DUMMY_popup_messagebox;
    g_popup_messagebox2 = g_DUMMY_popup_messagebox2;
    g_popup_messagebox3 = g_DUMMY_popup_messagebox3;
    g_popup_promptdialog = g_DUMMY_popup_promptdialog;
    g_popup_getfilename = g_DUMMY_popup_getfilename;
    g_popup_getdirname = g_DUMMY_popup_getdirname;
    g_popup_saveas = g_DUMMY_popup_saveas;
    g_popup_listbox = g_DUMMY_popup_listbox;
    g_popup_radiobox = g_DUMMY_popup_radiobox;
    g_popup_checkbox = g_DUMMY_popup_checkbox;
    g_popup_container_open = g_DUMMY_popup_container_open;
    g_popup_container_close = g_DUMMY_popup_container_close;
    g_popup_container_show = g_DUMMY_popup_container_show;
    g_popup_add_child = g_DUMMY_popup_add_child;
    g_popup_enable_item = g_DUMMY_popup_enable_item;
    g_popup_set_selection = g_DUMMY_popup_set_selection;
    g_popup_set_checkmark = g_DUMMY_popup_set_checkmark;
    g_popup_set_label = g_DUMMY_popup_set_label;
    g_popup_get_label = g_DUMMY_popup_get_label;
    g_popup_set_title = g_DUMMY_popup_set_title;
    g_popup_append_item = g_DUMMY_popup_append_item;
    g_popup_remove_item = g_DUMMY_popup_remove_item;
    g_popup_replace_item = g_DUMMY_popup_replace_item;
    g_popup_set_buttonlabel = g_DUMMY_popup_set_buttonlabel;
}

void set_SCREEN_mode(void)
{
    g_console_printf = g_SCREEN_console_printf;

    g_set_commandline = g_SCREEN_set_commandline;
    g_set_commandprompt = g_SCREEN_set_commandprompt;
    g_set_commandhistory = g_SCREEN_set_commandhistory;
    g_set_commandlocking = g_SCREEN_set_commandlocking;
    g_enable_nextcommand = g_SCREEN_enable_nextcommand;
    g_enable_commandinput = g_SCREEN_enable_commandinput;
    g_menu_create_menubar = g_SCREEN_menu_create_menubar;
    g_menu_create_menubar2 = g_SCREEN_menu_create_menubar2;
    g_menu_create_buttonbar = g_SCREEN_menu_create_buttonbar;
    g_menu_create_buttonbar2 = g_SCREEN_menu_create_buttonbar2;
    g_menu_create_buttonbox2 = g_SCREEN_menu_create_buttonbox2;
    g_menu_create_popup = g_SCREEN_menu_create_popup;
    g_menu_create_floating = g_SCREEN_menu_create_floating;
    g_menu_select_popup = g_SCREEN_menu_select_popup;
    g_menu_hide_popup = g_SCREEN_menu_hide_popup;
    g_menu_destroy_menubar_children = g_SCREEN_menu_destroy_menubar_children;
    g_menu_destroy_menubar2_children = g_SCREEN_menu_destroy_menubar2_children;
    g_menu_destroy_buttonbar_children = g_SCREEN_menu_destroy_buttonbar_children;
    g_menu_destroy_buttonbar2_children = g_SCREEN_menu_destroy_buttonbar2_children;
    g_menu_destroy_popup = g_SCREEN_menu_destroy_popup;
    g_menu_destroy_button = g_SCREEN_menu_destroy_button;
    g_menu_destroy_menubar2 = g_SCREEN_menu_destroy_menubar2;
    g_menu_destroy_buttonbar = g_SCREEN_menu_destroy_buttonbar;
    g_menu_destroy_buttonbar2 = g_SCREEN_menu_destroy_buttonbar2;
    g_menu_destroy_floating = g_SCREEN_menu_destroy_floating;
    g_menu_destroy_floating_children = g_SCREEN_menu_destroy_floating_children;
    g_menu_append_submenu = g_SCREEN_menu_append_submenu;
    g_menu_append_button = g_SCREEN_menu_append_button;
    g_menu_append_floating_button = g_SCREEN_menu_append_floating_button;
    g_menu_append_toggle = g_SCREEN_menu_append_toggle;
    g_menu_append_pixbutton = g_SCREEN_menu_append_pixbutton;
    g_menu_append_multipixbutton = g_SCREEN_menu_append_multipixbutton;
    g_menu_append_pixtoggle = g_SCREEN_menu_append_pixtoggle;
    g_menu_append_separator = g_SCREEN_menu_append_separator;
    g_menu_append_optionmenu = g_SCREEN_menu_append_optionmenu;
    g_menu_enable_item = g_SCREEN_menu_enable_item;
    g_menu_is_enabled = g_SCREEN_menu_is_enabled;
    g_menu_set_toggle = g_SCREEN_menu_set_toggle;
    g_menu_get_toggle = g_SCREEN_menu_get_toggle;
    g_menu_set_label = g_SCREEN_menu_set_label;
    g_menu_get_label = g_SCREEN_menu_get_label;
    g_menu_set_pixmap = g_SCREEN_menu_set_pixmap;
    g_menu_set_group = g_SCREEN_menu_set_group;
    g_menu_add_tooltip = g_SCREEN_menu_add_tooltip;
    g_scrollbar_connect = g_SCREEN_scrollbar_connect;
    g_scrollbar_reconnect = g_SCREEN_scrollbar_reconnect;
    g_scrollbar_get_maxworld = g_SCREEN_scrollbar_get_maxworld;
    g_scrollbar_get_zoom = g_SCREEN_scrollbar_get_zoom;
    g_scrollbar_disconnect = g_SCREEN_scrollbar_disconnect;
    g_scrollbar_reset = g_SCREEN_scrollbar_reset;
    g_scrollbar_retrieve = g_SCREEN_scrollbar_retrieve;
    g_scrollbar_store = g_SCREEN_scrollbar_store;
    g_scrollbar_set_dominant_viewport = g_SCREEN_scrollbar_set_dominant_viewport;
    g_popup_messagebox = g_SCREEN_popup_messagebox;
    g_popup_messagebox2 = g_SCREEN_popup_messagebox2;
    g_popup_messagebox3 = g_SCREEN_popup_messagebox3;
    g_popup_promptdialog = g_SCREEN_popup_promptdialog;
    g_popup_getfilename = g_SCREEN_popup_getfilename;
    g_popup_getdirname = g_SCREEN_popup_getdirname;
    g_popup_saveas = g_SCREEN_popup_saveas;
    g_popup_listbox = g_SCREEN_popup_listbox;
    g_popup_radiobox = g_SCREEN_popup_radiobox;
    g_popup_checkbox = g_SCREEN_popup_checkbox;
    g_popup_container_open = g_SCREEN_popup_container_open;
    g_popup_container_close = g_SCREEN_popup_container_close;
    g_popup_container_show = g_SCREEN_popup_container_show;
    g_popup_add_child = g_SCREEN_popup_add_child;
    g_popup_enable_item = g_SCREEN_popup_enable_item;
    g_popup_set_selection = g_SCREEN_popup_set_selection;
    g_popup_set_checkmark = g_SCREEN_popup_set_checkmark;
    g_popup_set_label = g_SCREEN_popup_set_label;
    g_popup_get_label = g_SCREEN_popup_get_label;
    g_popup_set_title = g_SCREEN_popup_set_title;
    g_popup_append_item = g_SCREEN_popup_append_item;
    g_popup_remove_item = g_SCREEN_popup_remove_item;
    g_popup_replace_item = g_SCREEN_popup_replace_item;
    g_popup_set_buttonlabel = g_SCREEN_popup_set_buttonlabel;
}


/* --- Console functions --- */
void  g_DUMMY_open_console(long x0,long y0,long width,long height, char *title)
{ x0 = x0; y0 = y0; width = width; height = height; title = title; }
void  g_DUMMY_close_console(void)
{ ; }
int   g_DUMMY_cons_puts(const char *string)
{ string = string; return 0; }
int   g_DUMMY_cons_fputs(const char *string, FILE *file)
{ string = string; file = file; return 0; }
int   g_DUMMY_cons_fputc(char c, FILE *file)
{ c=c; file =file; return 0; }
int   g_DUMMY_cons_printf(const char *format, ...)
{ format = format; return 0; }
int   g_DUMMY_cons_fprintf(FILE *file, const char *format, ...)
{ file=file; format=format; return 0; }
char  *g_DUMMY_cons_gets(char *buffer)
{ buffer = buffer; return NULL; }
char  *g_DUMMY_cons_fgets(char *string, int maxchar, FILE *file)
{ string = string; maxchar = maxchar; file = file; return NULL; };
int   g_DUMMY_cons_fgetc(FILE *file)
{ file = file; return 0; }

int   g_DUMMY_console_printf(int win_id, const char *format, ...)
{ win_id=win_id; format = format; return 0; }
/* --- commandline --- */

void  g_DUMMY_set_commandline(int win_id, char *command)
{ win_id = win_id; command = command; }
void  g_DUMMY_set_commandprompt(int win_id, char *prompt)
{ win_id = win_id; prompt = prompt;}
void  g_DUMMY_set_commandhistory(int win_id, char *history)
{ win_id = win_id; history=history;}
void  g_DUMMY_set_commandlocking(int win_id, int lock)
{ win_id = win_id; lock = lock;}
void  g_DUMMY_enable_nextcommand(int win_id)
{ win_id = win_id; }
void  g_DUMMY_enable_commandinput(int win_id, int enable)
{ win_id = win_id; enable = enable;}

/* --- menu's --- */

int   g_DUMMY_menu_create_menubar(int win_id)
{ win_id = win_id; return G_ERROR;}
int   g_DUMMY_menu_create_menubar2(int win_id)
{ win_id = win_id; return G_ERROR;}
int   g_DUMMY_menu_create_buttonbar(int win_id)
{ win_id = win_id; return G_ERROR;}
int   g_DUMMY_menu_create_buttonbar2(int win_id)
{ win_id = win_id; return G_ERROR;}
int   g_DUMMY_menu_create_buttonbox2(int win_id, int rows)
{ win_id = win_id; rows = rows; return G_ERROR;}
int   g_DUMMY_menu_create_popup(int win_id)
{ win_id = win_id; return G_ERROR;}
int   g_DUMMY_menu_create_floating(int win_id)
{ win_id = win_id; return G_ERROR;}

void  g_DUMMY_menu_select_popup(int menu_id)
{ menu_id = menu_id; }
void  g_DUMMY_menu_hide_popup(int win_id)
{ win_id = win_id;  }

void  g_DUMMY_menu_destroy_menubar_children(int win_id)
{ win_id = win_id; }
void  g_DUMMY_menu_destroy_menubar2_children(int win_id)
{ win_id = win_id; }
void  g_DUMMY_menu_destroy_buttonbar_children(int win_id)
{ win_id = win_id; }
void  g_DUMMY_menu_destroy_buttonbar2_children(int win_id)
{ win_id = win_id; }
void  g_DUMMY_menu_destroy_popup(int win_id)
{ win_id = win_id; }
void  g_DUMMY_menu_destroy_button(int menu_id, int item_id)
{ menu_id = menu_id; item_id = item_id; }
void  g_DUMMY_menu_destroy_menubar2(int win_id)
{ win_id = win_id; }
void  g_DUMMY_menu_destroy_buttonbar(int win_id)
{ win_id = win_id; }
void  g_DUMMY_menu_destroy_buttonbar2(int win_id)
{ win_id = win_id; }
void  g_DUMMY_menu_destroy_floating(int win_id)
{ win_id = win_id; }
void  g_DUMMY_menu_destroy_floating_children(int win_id)
{ win_id = win_id; }

int   g_DUMMY_menu_append_submenu(int menu_id, char *label)
{ menu_id = menu_id; label = label; return G_ERROR; }
int   g_DUMMY_menu_append_button(int menu_id, char *label, int return_id)
{ menu_id = menu_id; label = label; return_id = return_id; return G_ERROR; }
int   g_DUMMY_menu_append_floating_button(int menu_id, char *label, int return_id,
                                       float vp_x, float vp_y)
{ menu_id = menu_id; label = label; return_id = return_id; 
  vp_x = vp_x; vp_y = vp_y; return G_ERROR; }
int   g_DUMMY_menu_append_toggle(int menu_id, char *label, int return_id,
						  int toggle_on)
{ menu_id = menu_id; label = label; return_id = return_id;
  toggle_on = toggle_on; return G_ERROR; }
int   g_DUMMY_menu_append_pixbutton(int menu_id, char *pixinfo[],
					char *pixgrayinfo[], int return_id)
{ menu_id = menu_id; pixinfo = pixinfo; pixgrayinfo = pixgrayinfo;
  return_id = return_id;  return G_ERROR; }
int g_DUMMY_menu_append_multipixbutton(int menu_id, char *pixinfo1[], 
                   char *pixinfo2[], char *pixinfo3[], char *pixinfo4[],int return_id)
{ menu_id = menu_id; pixinfo1 = pixinfo1; pixinfo2 = pixinfo2;
   pixinfo3 = pixinfo3; pixinfo4 = pixinfo4;return_id = return_id;
   return G_ERROR; }
int   g_DUMMY_menu_append_pixtoggle(int menu_id, char *pixinfo[], char *pixgrayinfo[],
             char *pixselectinfo[], char *pixselectgrayinfo[], int return_id, int toggle_on)
{ menu_id = menu_id; pixinfo = pixinfo; pixgrayinfo = pixgrayinfo;
  pixselectinfo = pixselectinfo;  pixselectgrayinfo = pixselectgrayinfo;
  return_id = return_id; toggle_on = toggle_on; return G_ERROR; }
int   g_DUMMY_menu_append_separator(int menu_id)
{ menu_id = menu_id;  return G_ERROR; }
int   g_DUMMY_menu_append_optionmenu(int menu_id, char *label, 
             int return_id, int item_count, int item_select, char *data[])
{ menu_id = menu_id;  label = label; return_id = return_id; 
item_count = item_count; item_select = item_select; data=data; return G_ERROR; }

void  g_DUMMY_menu_enable_item(int menu_id, int item_id, int enable)
{ menu_id = menu_id; item_id = item_id; enable = enable; }
int   g_DUMMY_menu_is_enabled(int menu_id, int item_id)
{ menu_id = menu_id; item_id = item_id; return FALSE; }
void  g_DUMMY_menu_set_toggle(int menu_id, int item_id, int toggle_on)
{ menu_id = menu_id; item_id = item_id; toggle_on = toggle_on; }
int   g_DUMMY_menu_get_toggle(int menu_id, int item_id)
{ menu_id = menu_id; item_id = item_id; return 0; }
int   g_DUMMY_menu_set_label(int menu_id, int item_id, char *label)
{ menu_id = menu_id; item_id = item_id; label = label; return G_ERROR; }
char  *g_DUMMY_menu_get_label(int menu_id, int item_id)
{ menu_id = menu_id; item_id = item_id; return NULL; }
int   g_DUMMY_menu_set_pixmap(int menu_id, int item_id, int pix_id)
{ menu_id = menu_id; item_id = item_id; pix_id = pix_id; return G_ERROR; }
int   g_DUMMY_menu_set_group(int menu_id, int group_on)
{
  menu_id = menu_id; group_on = group_on;
  return G_OK;
}
int   g_DUMMY_menu_add_tooltip(int return_id, char *tip)
{
  return_id = return_id; tip = tip;
  return G_OK;
}


/* --- scrollbars ---*/
void  g_DUMMY_scrollbar_connect(int win_id, int vp_id,
                         float wx1, float wy1, float wx2, float wy2, long flag)
{ 
win_id = win_id; vp_id = vp_id; wx1 = wx1; wy1 = wy1; wx2 = wx2; wy2 = wy2;
  flag = flag; }
int  g_DUMMY_scrollbar_reconnect(int win_id, int vp_id,
                         float wx1, float wy1, float wx2, float wy2)
{ win_id = win_id; vp_id = vp_id; wx1 = wx1; wy1 = wy1; wx2 = wx2; wy2 = wy2;
  return FALSE;}
int  g_DUMMY_scrollbar_get_maxworld(int win_id, int vp_id,
                         float *wx1, float *wy1, float *wx2, float *wy2)
{ win_id = win_id; vp_id = vp_id; wx1 = wx1; wy1 = wy1; wx2 = wx2; wy2 = wy2;
  return FALSE;}
int  g_DUMMY_scrollbar_get_zoom(int win_id, int vp_id,
                         float *zoomx, float *zoomy)
{ win_id = win_id; vp_id = vp_id; *zoomx = 1.0; *zoomy = 1.0;
  return FALSE;}
void  g_DUMMY_scrollbar_disconnect(int win_id)
{ win_id = win_id; }
void  g_DUMMY_scrollbar_reset(int win_id)
{ win_id = win_id; }
int  g_DUMMY_scrollbar_retrieve(int win_id, int cc_id)
{ win_id = win_id; cc_id = cc_id; return G_ERROR; }
int  g_DUMMY_scrollbar_store(int win_id)
{ win_id = win_id;  return G_ERROR;}
int g_DUMMY_scrollbar_set_dominant_viewport(int win_id, int vp_id)
{ win_id = win_id; vp_id = vp_id;  return FALSE;}

/* --- popup --- */
void  g_DUMMY_popup_messagebox(int win_id, char *title, char *message, int wait)
{ win_id = win_id; title = title; message = message; wait = wait; }
int   g_DUMMY_popup_messagebox2(int win_id, char *title, char *message, 
                         char *button1text, char *button2text)
{ win_id = win_id; title = title; message = message; button1text = button1text;
  button2text = button2text; return G_POPUP_BUTTON2; }
int   g_DUMMY_popup_messagebox3(int win_id, char *title, char *message, 
                       char *button1text, char *button2text, char *button3text)
{ win_id = win_id; title = title; message = message; button1text = button1text;
  button2text = button2text; button3text = button3text; return G_POPUP_BUTTON3; }
char  *g_DUMMY_popup_promptdialog(int win_id, char *title, char *question, char *text)
{ win_id = win_id; title = title; question = question; text = text; return NULL; }
char  *g_DUMMY_popup_getfilename(int win_id, char *title, char *mask)
{ win_id = win_id; title = title; mask = mask; return NULL; }
char  *g_DUMMY_popup_getdirname(int win_id, char *title)
{ win_id = win_id; title = title; return NULL; }
char  *g_DUMMY_popup_saveas(int win_id, char *title, char *filename)
{ win_id = win_id; title = title; filename = filename; return NULL; }
int   g_DUMMY_popup_listbox(int win_id, char *title, char *subtitle, int item_count, 
                     char *items[], int select_pos, int items_visible)
{ win_id = win_id; title = title; subtitle = subtitle; item_count = item_count;
  items = items; select_pos = select_pos; items_visible = items_visible; return FALSE; }
int   g_DUMMY_popup_radiobox(int win_id, char *title, char *subtitle, int item_count, 
                      char *items[], int select_pos)
{ win_id = win_id; title = title; subtitle = subtitle; item_count = item_count;
  items = items; select_pos = select_pos; return FALSE; }
int   g_DUMMY_popup_checkbox(int win_id, char *title, char *subtitle, int item_count, 
                      char *items[], int *select)
{ win_id = win_id; title = title; subtitle = subtitle; item_count = item_count;
  items = items; select = select; return FALSE; }
int   g_DUMMY_popup_container_open(int win_id, char *title, int flag)
{ win_id = win_id; title = title; flag=flag; return FALSE;}
void  g_DUMMY_popup_container_close(int cont_id)
{ cont_id = cont_id;  }
int   g_DUMMY_popup_container_show(int cont_id)
{ cont_id = cont_id; return FALSE; }
void  g_DUMMY_popup_add_child(int cont_id, G_POPUP_CHILDINFO *ci)
{ cont_id = cont_id; ci = ci; }
void  g_DUMMY_popup_enable_item(int cont_id, int item_id, int enable)
{ cont_id = cont_id; item_id = item_id; enable = enable;}
void  g_DUMMY_popup_set_selection(int cont_id, int item_id, int position)
{ cont_id = cont_id; item_id = item_id; position = position; }
void  g_DUMMY_popup_set_checkmark(int cont_id, int item_id, int position, int toggle_on)
{ cont_id = cont_id; item_id = item_id; position = position; toggle_on = toggle_on;}
void  g_DUMMY_popup_set_label(int cont_id, int item_id, char *label)
{ cont_id = cont_id; item_id = item_id; label = label; }
char  *g_DUMMY_popup_get_label(int cont_id, int item_id)
{ cont_id = cont_id; item_id = item_id; return NULL; }
void  g_DUMMY_popup_set_title(int cont_id, int item_id, char *title)
{ cont_id = cont_id; item_id = item_id; title = title; }
void  g_DUMMY_popup_append_item(int cont_id, int item_id, char *label)
{ cont_id = cont_id; item_id = item_id; label = label; }
void  g_DUMMY_popup_remove_item(int cont_id, int item_id, int pos)
{ cont_id = cont_id; item_id = item_id; pos = pos; }
void  g_DUMMY_popup_replace_item(int cont_id, int item_id, int pos, char *label)
{ cont_id = cont_id; item_id = item_id; pos = pos; label = label; }
void g_DUMMY_popup_set_buttonlabel(int cont_id, char *label, int whichbutton)
{ cont_id = cont_id;  label = label; whichbutton = whichbutton; }


void g_popup_init_info(G_POPUP_CHILDINFO *ci)
{
    ci->type           = 0;
    ci->id             = 0;
    ci->cont_id        = 0;
    ci->win_id         = 0;
    ci->item_count     = 0;
    ci->items_visible  = 0;
    ci->item           = 0;
    ci->disabled       = 0;
    ci->horizontal     = 0;
    ci->frame          = 0;
    ci->title          = NULL;   
    ci->select         = NULL;
    ci->label          = NULL;
    ci->data           = NULL;
    ci->func           = NULL;
    ci->userdata       = NULL;
}

/*****************************************************
* SCROLLBARS
*/

static SCROLLSTRUCT *SS[G_MAXWINDOW];

typedef struct scrollstore {
    int id;
    SCROLLSTRUCT *SS;
    struct scrollstore  *next;
} SCROLLSTORESTRUCT;

static SCROLLSTORESTRUCT *SSS[G_MAXWINDOW];
static int sss_count=0;

#define NOTUSED -1

void INTERNAL_init_scroll(void)
{
    int i;

    for (i = 0; i < G_MAXWINDOW; i++) {
	SS[i] = NULL;
        SSS[i] = NULL;
        sss_count = 0;
    }
}

void INTERNAL_exit_scroll(void)
{
    int i;

    for (i = 0; i < G_MAXWINDOW; i++) {
	if (SS[i] != NULL) {
	    free(SS[i]);
	    SS[i] = NULL;
	}
	if (SSS[i] != NULL) {
   	    if (SSS[i]->SS != NULL) 
	        free(SSS[i]->SS);
	    free(SSS[i]);
	    SSS[i] = NULL;
	}
        sss_count = 0;
    }
}


static int alloc_scrollstruct(int win_id, int size)
{
    if ((SS[win_id] = (SCROLLSTRUCT *)
	realloc(SS[win_id], size * sizeof(SCROLLSTRUCT))) == NULL) {
	fprintf(stderr, "Memory allocation error\n");
	exit(1);
    }
    SS[win_id][0].vp_count = size;
    SS[win_id][size - 1].vp_id = NOTUSED;
    SS[win_id][0].flag = 0;
    return G_OK;
}


static void activate_scrollbars(int id)
{
    int i;
    
    INTERNAL_SetScrollPosV(id,
			   SS[id][0].posvertscroll,
			   SS[id][0].size_v,
			   TRUE);
    INTERNAL_SetScrollPosH(id,
		           SS[id][0].poshorzscroll,
                           SS[id][0].size_h,
			   TRUE);

    for (i = 0; i < SS[id][0].vp_count; i++) {
	g_set_world(SS[id][i].vp_id,
		    SS[id][i].n_x1, SS[id][i].n_y1,
		    SS[id][i].n_x1 + SS[id][i].n_dx,
		    SS[id][i].n_y1 + SS[id][i].n_dy);
    }
    g_push_viewport();
    for (i = 0; i < SS[id][0].vp_count; i++) {
	if (SS[id][i].flag & G_SCROLL_CLEARVIEWPORT) {
	    g_select_viewport(SS[id][i].vp_id);
	    g_clear_viewport();
	}
    }
    g_pop_viewport();
    INTERNAL_update_window(G_SCREEN, id);
}

int g_SCREEN_scrollbar_store(int win_id)
{
    int id, size, device, sss_id;
    SCROLLSTORESTRUCT *SSSS, *TEMP;

    id = GET_LOCAL_ID(win_id);
    device = GET_DEVICE(win_id);
    if (id < 0 || id >= G_MAXWINDOW)
	return G_ERROR;
    if (device != G_SCREEN)
	return G_ERROR;
    if (!INTERNAL_ScrollBarsPresent(id))
        return G_ERROR;
    if (SS[id] == NULL) 
        return G_ERROR;
    size = SS[id][0].vp_count * sizeof(SCROLLSTRUCT);
    if ((SSSS = (SCROLLSTORESTRUCT*) malloc (sizeof(SCROLLSTORESTRUCT))) == NULL) {
	fprintf(stderr, "Memory allocation error\n");
	exit(1);
    }
    if ((SSSS->SS = (SCROLLSTRUCT *)  malloc(size)) == NULL) {
	fprintf(stderr, "Memory allocation error\n");
	exit(1);
    }
    SSSS->next = NULL;
    SSSS->id = sss_count++;
    memcpy(SSSS->SS, SS[id], size);
    if (SSS[id] == NULL) {
        SSS[id] = SSSS;
        return SSSS->id;
    }
    TEMP = SSS[id];
    while (TEMP->next)
        TEMP = TEMP->next;
    TEMP->next = SSSS;
    return SSSS->id;
}

int g_SCREEN_scrollbar_retrieve(int win_id, int ss_id)
{
    int id, size, device, sss_id;
    SCROLLSTORESTRUCT *SSSS, *TEMP;

    id = GET_LOCAL_ID(win_id);
    device = GET_DEVICE(win_id);
    if (id < 0 || id >= G_MAXWINDOW)
	return G_ERROR;
    if (device != G_SCREEN)
	return G_ERROR;
    if (!INTERNAL_ScrollBarsPresent(id))
        return G_ERROR;
    if (SSS[id] == NULL) 
        return G_ERROR;
    for (TEMP = SSS[id], SSSS = SSS[id]; TEMP != NULL; TEMP = TEMP->next) {
        if (TEMP->id == ss_id) {
            int size = TEMP->SS[0].vp_count * sizeof(SCROLLSTRUCT);
            alloc_scrollstruct(id, size);
            memcpy(SS[id], TEMP->SS, size);
            if (SSSS != TEMP) 
                SSSS->next = TEMP->next;
            else
                SSS[id] = TEMP->next;
            free (TEMP->SS);
            free(TEMP);
            activate_scrollbars(id);
            return G_OK;
        }
        SSSS = TEMP;
    }
    return G_ERROR;
}

static void fswap(float *f1, float *f2)
{
    float f3 = *f1;
    *f1 = *f2;
    *f2 = f3;
}

int INTERNAL_get_clampscrollbars(int id, int vp_id,
			      float *x1, float *y1, float *x2, float *y2)
{
    if (SS[id] == NULL)
	return G_ERROR;
    if (SS[id][0].vp_id != vp_id)
	return G_ERROR;


    *x1 = fabs((SS[id][0].n_x1 - SS[id][0].o_x1) / SS[id][0].n_dx);
    *y1 = fabs((SS[id][0].n_y1 - SS[id][0].o_y1) / SS[id][0].n_dy);
    *x2 = fabs((SS[id][0].o_x2 - (SS[id][0].n_x1 + SS[id][0].n_dx))
	       / SS[id][0].n_dx);
    *y2 = fabs((SS[id][0].o_y2 - (SS[id][0].n_y1 + SS[id][0].n_dy))
	       / SS[id][0].n_dy);

/*
    if (SS[id][0].reverse_x < 0) {
        float f = *x1;
        *x1 = *x2;
        *x2 = f;
    }

    if (SS[id][0].reverse_y < 0) {
        float f = *y1;
        *y1 = *y2;
        *y2 = f;
    }

fprintf(stderr,"%f %f %f %f\n", *x1, *y1, *x2, *y2);
*/
    return G_OK;
}

int INTERNAL_update_scrollbars(int id, int vp_id, float shiftx, float shifty)
{
    int i;
    float dx, dy, p;

    if (SS[id] == NULL)
	return G_ERROR;
    if (SS[id][0].vp_id != vp_id)
	return G_ERROR;

/*
fprintf(stderr,"%f %f %f %f\n", shiftx,shifty,SS[id][0].reverse_x,SS[id][0].reverse_y);

    dx = SS[id][0].n_dx * shiftx * SS[id][0].reverse_x;
    dy = SS[id][0].n_dy * shifty * SS[id][0].reverse_y;
*/
    dx = SS[id][0].n_dx * shiftx;
    dy = SS[id][0].n_dy * shifty;

    SS[id][0].n_x1 += dx;
    SS[id][0].n_y1 += dy;

    if (SS[id][0].reverse_x > 0)
	SS[id][0].n_x1 = max(SS[id][0].n_x1, SS[id][0].o_x1);
    else
	SS[id][0].n_x1 = min(SS[id][0].n_x1, SS[id][0].o_x1);

    if (SS[id][0].reverse_y > 0)
	SS[id][0].n_y1 = max(SS[id][0].n_y1, SS[id][0].o_y1);
    else
	SS[id][0].n_y1 = min(SS[id][0].n_y1, SS[id][0].o_y1);

    p = ((fabs(SS[id][0].n_y1 - SS[id][0].o_y1)
		* (float) (SMAX - SS[id][0].size_v)) / SS[id][0].yrange);
    p = max(0, p);
    p = min(SMAX - SS[id][0].size_v, p);
    for (i = 0; i < SS[id][0].vp_count; i++)
	if (SS[id][0].size_v == SMAX)
	    SS[id][i].n_y1 = SS[id][i].o_y1;
	else
	    SS[id][i].n_y1 = SS[id][i].o_y1 + SS[id][i].reverse_y
		* SS[id][i].yrange *
		p / ((float) SMAX - SS[id][0].size_v);
    SS[id][0].posvertscroll = max(0,SMAX  - SS[id][0].size_v - (int) p);

    INTERNAL_SetScrollPosV(id,
			   SS[id][0].posvertscroll,
			   SS[id][0].size_v,
			   TRUE);



    p = ((fabs(SS[id][0].n_x1 - SS[id][0].o_x1)
		* (float) (SMAX - SS[id][0].size_h)) / SS[id][0].xrange);
    p = max(0, p);
    p = min(SMAX - SS[id][0].size_h, p);
    SS[id][0].poshorzscroll = (int)p;
    for (i = 0; i < SS[id][0].vp_count; i++)
	if (SS[id][0].size_h == SMAX)
	    SS[id][i].n_x1 = SS[id][i].o_x1;
	else
	    SS[id][i].n_x1 = SS[id][i].o_x1 + SS[id][i].reverse_x
		* SS[id][i].xrange *
		p /((float) SMAX - SS[id][0].size_h);
/*
    SS[id][ss_id].xrange = fabs(SS[id][ss_id].o_x2 - SS[id][ss_id].o_x1)
	- fabs(wx2 - wx1);
*/
   INTERNAL_SetScrollPosH(id,
		           SS[id][0].poshorzscroll,
                           SS[id][0].size_h,
			   TRUE);

    for (i = 0; i < SS[id][0].vp_count; i++) {
	g_set_world(SS[id][i].vp_id,
		    SS[id][i].n_x1, SS[id][i].n_y1,
		    SS[id][i].n_x1 + SS[id][i].n_dx,
		    SS[id][i].n_y1 + SS[id][i].n_dy);
    }
    g_push_viewport();
    for (i = 0; i < SS[id][0].vp_count; i++) {
	if (SS[id][i].flag & G_SCROLL_CLEARVIEWPORT) {
	    g_select_viewport(SS[id][i].vp_id);
	    g_clear_viewport();
	}
    }
    g_pop_viewport();
    INTERNAL_update_window(G_SCREEN, id);
    return G_OK;
}

void g_SCREEN_scrollbar_connect(int win_id, int vp_id,
		   float wx1, float wy1, float wx2, float wy2, long flag)
{
    int id, oldflag, device, ss_id;
    VIEWPORTTYPE *vp;

    id = GET_LOCAL_ID(win_id);
    device = GET_DEVICE(win_id);
    if (id < 0 || id >= G_MAXWINDOW)
	return;
    if (device != G_SCREEN)
	return;
    if ((wx1 == wx2 && wy1 == wy2) &&
       (!(flag & G_SCROLL_XAXIS)) &&
       (!(flag & G_SCROLL_YAXIS)) &&
       (!(flag & G_SCROLL_CORRESPONDING)))
	    return;
    if (!INTERNAL_ScrollBarsPresent(id))
        return;
    if (SS[id] == NULL) {
	ss_id = 0;
	alloc_scrollstruct(id, ss_id + 1);
    }
    else {
	for (ss_id = 0; ss_id < SS[id][0].vp_count; ss_id++)
	    if (SS[id][ss_id].vp_id == vp_id)
		break;
	if (ss_id == SS[id][0].vp_count)
	    alloc_scrollstruct(id, ss_id + 1);
    }

    vp = INTERNAL_get_viewport(vp_id);
    /* --- store old viewport world-coordinates --- */
    if (SS[id][ss_id].vp_id == NOTUSED) {
	SS[id][ss_id].o_x1 = vp->wx1;
	SS[id][ss_id].o_y1 = vp->wy1;
	SS[id][ss_id].o_x2 = vp->wx2;
	SS[id][ss_id].o_y2 = vp->wy2;
    }
    SS[id][ss_id].vp_id = vp_id;
    oldflag = SS[id][ss_id].flag;
    SS[id][ss_id].flag = oldflag | flag;
    if ((wy1 == wy2 || (flag & G_SCROLL_XAXIS)) &&
        !(flag & G_SCROLL_CORRESPONDING)){
	wy1 = SS[id][ss_id].o_y1;
	wy2 = SS[id][ss_id].o_y2;
    }
    if (ss_id > 0 && ((flag & G_SCROLL_XAXIS) ||
        (flag & G_SCROLL_CORRESPONDING))){
        wx1 = (SS[id][0].n_x1-SS[id][0].o_x1) * 
                (SS[id][ss_id].o_x2 - SS[id][ss_id].o_x1)/ 
                (SS[id][0].o_x2 - SS[id][0].o_x1) 
                + SS[id][ss_id].o_x1;
        wx2 = (SS[id][0].n_x1 + SS[id][0].n_dx -SS[id][0].o_x1) * 
                (SS[id][ss_id].o_x2 - SS[id][ss_id].o_x1)/ 
                (SS[id][0].o_x2 - SS[id][0].o_x1) + 
                SS[id][ss_id].o_x1;
    }
    if ((wx1 == wx2 || (flag & G_SCROLL_YAXIS)) &&
        !(flag & G_SCROLL_CORRESPONDING)){
	wx1 = SS[id][ss_id].o_x1;
	wx2 = SS[id][ss_id].o_x2;
    }
    if (ss_id > 0 && ((flag & G_SCROLL_YAXIS) ||
        (flag & G_SCROLL_CORRESPONDING))){
        wy1 = (SS[id][0].n_y1-SS[id][0].o_y1) * 
                (SS[id][ss_id].o_y2 - SS[id][ss_id].o_y1)/ 
                (SS[id][0].o_y2 - SS[id][0].o_y1) 
                + SS[id][ss_id].o_y1;
        wy2 = (SS[id][0].n_y1 + SS[id][0].n_dy - SS[id][0].o_y1) * 
                (SS[id][ss_id].o_y2 - SS[id][ss_id].o_y1)/ 
                (SS[id][0].o_y2 - SS[id][0].o_y1) + 
                SS[id][ss_id].o_y1;
    }

    /* --- clamp new world coordinates inside the original ones --- */
    if (SS[id][ss_id].o_x1 <= SS[id][ss_id].o_x2) {
	if (wx1 > wx2)
	    fswap(&wx1, &wx2);
	wx1 = max(wx1, SS[id][ss_id].o_x1);
	wx2 = min(wx2, SS[id][ss_id].o_x2);
	SS[id][ss_id].reverse_x = 1.0;
    }
    else {
	if (wx1 < wx2)
	    fswap(&wx1, &wx2);
	wx1 = min(wx1, SS[id][ss_id].o_x1);
	wx2 = max(wx2, SS[id][ss_id].o_x2);
	SS[id][ss_id].reverse_x = -1.0;
    }
    if (SS[id][ss_id].o_y1 <= SS[id][ss_id].o_y2) {
	if (wy1 > wy2)
	    fswap(&wy1, &wy2);
	wy1 = max(wy1, SS[id][ss_id].o_y1);
	wy2 = min(wy2, SS[id][ss_id].o_y2);
	SS[id][ss_id].reverse_y = 1.0;
    }
    else {
	if (wy1 < wy2)
	    fswap(&wy1, &wy2);
	wy1 = min(wy1, SS[id][ss_id].o_y1);
	wy2 = max(wy2, SS[id][ss_id].o_y2);
	SS[id][ss_id].reverse_y = -1.0;
    }
    SS[id][ss_id].n_dx = wx2 - wx1;
    SS[id][ss_id].xrange = fabs(SS[id][ss_id].o_x2 - SS[id][ss_id].o_x1)
	- fabs(wx2 - wx1);
    SS[id][ss_id].n_x1 = wx1;
    if (ss_id == 0) {
        float h,p;
	h = SMAX * (fabs(wx2 - wx1) /
	    fabs(SS[id][0].o_x2 - SS[id][0].o_x1));
	h = max(SMAX / 500, h);
	SS[id][0].size_h = INTERNAL_SetScrollSlider((int) h);

	p = ((fabs(SS[id][0].n_x1 - SS[id][0].o_x1)
		* (float) (SMAX - SS[id][0].size_h)) / SS[id][0].xrange);
	p = max(0, p);
	p = min((float)(SMAX - SS[id][0].size_h), p);
        SS[id][0].poshorzscroll = (int) p;
    }
    SS[id][ss_id].n_dy = wy2 - wy1;
    SS[id][ss_id].yrange = fabs(SS[id][ss_id].o_y2 - SS[id][ss_id].o_y1)
	- fabs(wy2 - wy1);
    SS[id][ss_id].n_y1 = wy1;
    if (ss_id == 0) {
        float v,p;
	v = SMAX * (fabs(wy2 - wy1) /
	     fabs(SS[id][0].o_y2 - SS[id][0].o_y1));
	v = max(SMAX / 500, v);

	SS[id][0].size_v = INTERNAL_SetScrollSlider((int)v);

	p =  ((fabs(SS[id][0].n_y1 - SS[id][0].o_y1)
		* (float) (SMAX - SS[id][0].size_v)) / SS[id][0].yrange);
	p = max(0, p);
	p = min((float)(SMAX - SS[id][0].size_v), p);

	SS[id][0].posvertscroll = max(0,SMAX - SS[id][0].size_v - (int) p);
    }

    g_set_world(SS[id][ss_id].vp_id, SS[id][ss_id].n_x1, SS[id][ss_id].n_y1,
		SS[id][ss_id].n_x1 + SS[id][ss_id].n_dx,
		SS[id][ss_id].n_y1 + SS[id][ss_id].n_dy);

    if (ss_id == 0) {
	INTERNAL_SetScrollPosV(id,
			       SS[id][0].posvertscroll,
			       SS[id][0].size_v,
			       TRUE);
	INTERNAL_SetScrollPosH(id,
			       SS[id][0].poshorzscroll,
			       SS[id][0].size_h,
			       TRUE);
    }
    if (flag | G_SCROLL_LASTVIEWPORT) {
	oldflag = SS[id][0].flag;
	SS[id][0].flag = flag | oldflag;
    }
    if ((ss_id == SS[id][0].vp_count - 1) && 
                  (SS[id][0].flag & G_SCROLL_LASTVIEWPORT)) {
	int i;
	g_push_viewport();
	for (i = 0; i < SS[id][0].vp_count; i++) {
	    if (SS[id][i].flag & G_SCROLL_CLEARVIEWPORT) {
		g_select_viewport(SS[id][i].vp_id);
		g_clear_viewport();
	    }
	}
	g_pop_viewport();
	INTERNAL_update_window(G_SCREEN, id);
    }
}


int g_SCREEN_scrollbar_reconnect(int win_id, int vp_id,
		   float wx1, float wy1, float wx2, float wy2)
{
    int id, device, ss_id, i;
    float fx1,fy1,fx2,fy2;
    float odx,ody,ndx,ndy;
    float xx1,yy1,xx2,yy2;

    id = GET_LOCAL_ID(win_id);
    device = GET_DEVICE(win_id);
    if (id < 0 || id >= G_MAXWINDOW)
	return FALSE;
    if (device != G_SCREEN)
	return FALSE;
    if (wx1 == wx2 && wy1 == wy2)
	return FALSE;
    if (SS[id] == NULL) 
	return FALSE;
    if (!INTERNAL_ScrollBarsPresent(id))
        return FALSE;

    ss_id = -1;
    for (i = 0; i < SS[id][0].vp_count; i++) {
        if (SS[id][i].vp_id == vp_id) {
            ss_id = i;
            break;
        }
    }
    if (ss_id == -1) 
        return FALSE;

    if (wy1 == wy2) {
	wy1 = SS[id][ss_id].o_y1;
	wy2 = SS[id][ss_id].o_y2;
    }
    if (wx1 == wx2) {
	wx1 = SS[id][ss_id].o_x1;
	wx2 = SS[id][ss_id].o_x2;
    }
    odx = SS[id][ss_id].o_x2 - SS[id][ss_id].o_x1;
    ndx = wx2 - wx1;
    ody = SS[id][ss_id].o_y2 - SS[id][ss_id].o_y1;
    ndy = wy2 - wy1;
    xx1 = SS[id][ss_id].n_x1;
    xx2 = SS[id][ss_id].n_dx + xx1;
    yy1 = SS[id][ss_id].n_y1;
    yy2 = SS[id][ss_id].n_dy + yy1;

    fx1 = (xx1 - SS[id][ss_id].o_x1) * ndx/odx + wx1;
    fx2 = (xx2 - SS[id][ss_id].o_x1) * ndx/odx + wx1;
    fy1 = (yy1 - SS[id][ss_id].o_y1) * ndy/ody + wy1;
    fy2 = (yy2 - SS[id][ss_id].o_y1) * ndy/ody + wy1;

    if (ndx > 0) {
	if (fx1 > fx2)
	    fswap(&fx1, &fx2);
	SS[id][ss_id].reverse_x = 1.0;
    }
    else {
	if (fx1 < fx2)
	    fswap(&fx1, &fx2);
	SS[id][ss_id].reverse_x = -1.0;
    }
    if (ndy > 0) {
	if (fy1 > fy2)
	    fswap(&fy1, &fy2);
	SS[id][ss_id].reverse_y = 1.0;
    }
    else {
	if (fy1 < fy2)
	    fswap(&fy1, &fy2);
	SS[id][ss_id].reverse_y = -1.0;
    }

    SS[id][ss_id].n_x1 = fx1;
    SS[id][ss_id].n_y1 = fy1;
    SS[id][ss_id].n_dx = fx2 - fx1;
    SS[id][ss_id].n_dy = fy2 - fy1;
    SS[id][ss_id].o_x1 = wx1;
    SS[id][ss_id].o_y1 = wy1;
    SS[id][ss_id].o_x2 = wx2;
    SS[id][ss_id].o_y2 = wy2;
    SS[id][ss_id].xrange = fabs(ndx) - fabs(fx2 - fx1);
    SS[id][ss_id].yrange = fabs(ndy) - fabs(fy2 - fy1);
    g_set_world(SS[id][ss_id].vp_id, fx1, fy1, fx2, fy2);
    return TRUE;
}

int g_SCREEN_scrollbar_get_maxworld(int win_id, int vp_id,
		   float *wx1, float *wy1, float *wx2, float *wy2)
{
    int id, device, ss_id, i;
    float fx1,fy1,fx2,fy2;
    float odx,ody,ndx,ndy;
    float xx1,yy1,xx2,yy2;

    id = GET_LOCAL_ID(win_id);
    device = GET_DEVICE(win_id);
    if (id < 0 || id >= G_MAXWINDOW)
	return FALSE;
    if (device != G_SCREEN)
	return FALSE;
    if (SS[id] == NULL) 
	return FALSE;
    if (!INTERNAL_ScrollBarsPresent(id))
        return FALSE;

    for (i = 0; i < SS[id][0].vp_count; i++) {
        if (SS[id][i].vp_id == vp_id) {
            ss_id = i;
            *wx1 =  SS[id][ss_id].o_x1;       
            *wy1 =  SS[id][ss_id].o_y1;       
            *wx2 =  SS[id][ss_id].o_x2;       
            *wy2 =  SS[id][ss_id].o_y2;
            return TRUE;       
        }
    }
    return FALSE;
}

int g_SCREEN_scrollbar_get_zoom(int win_id, int vp_id,
		   float *zoomx, float *zoomy)
{
    int id, device, ss_id, i;
    float fx1,fy1,fx2,fy2;
    float odx,ody,ndx,ndy;
    float xx1,yy1,xx2,yy2;

    *zoomx = 1.0;
    *zoomy = 1.0;
    id = GET_LOCAL_ID(win_id);
    device = GET_DEVICE(win_id);
    if (id < 0 || id >= G_MAXWINDOW)
	return FALSE;
    if (device != G_SCREEN)
	return FALSE;
    if (SS[id] == NULL) 
	return FALSE;
    if (!INTERNAL_ScrollBarsPresent(id))
        return FALSE;

    for (i = 0; i < SS[id][0].vp_count; i++) {
        if (SS[id][i].vp_id == vp_id) {
            ss_id = i;
            *zoomx = fabs((SS[id][ss_id].o_x2 - SS[id][ss_id].o_x1)/
                         SS[id][ss_id].n_dx);
            *zoomy = fabs((SS[id][ss_id].o_y2 - SS[id][ss_id].o_y1)/
                         SS[id][ss_id].n_dy);
            return TRUE;       
        }
    }
    return FALSE;
}



void g_SCREEN_scrollbar_reset(int win_id)
{
    int id, device, ss_id, i;

    id = GET_LOCAL_ID(win_id);
    device = GET_DEVICE(win_id);
    if (id < 0 || id >= G_MAXWINDOW)
	return;
    if (device != G_SCREEN)
	return;
    if (SS[id] == NULL)
	return;
    if (!INTERNAL_ScrollBarsPresent(id))
        return;

    for (ss_id = 0; ss_id < SS[id][0].vp_count; ss_id++) {
	SS[id][ss_id].n_x1 = SS[id][ss_id].o_x1;
	SS[id][ss_id].n_y1 = SS[id][ss_id].o_y1;
	SS[id][ss_id].n_dx = SS[id][ss_id].o_x2 - SS[id][ss_id].o_x1;
	SS[id][ss_id].n_dy = SS[id][ss_id].o_y2 - SS[id][ss_id].o_y1;
	SS[id][ss_id].xrange = 1;
	SS[id][ss_id].yrange = 1;
	SS[id][ss_id].poshorzscroll = 0;
	SS[id][ss_id].size_h = SMAX;
	SS[id][ss_id].posvertscroll = 0;
	SS[id][ss_id].size_v = SMAX;
	g_set_world(SS[id][ss_id].vp_id,
		    SS[id][ss_id].o_x1, SS[id][ss_id].o_y1,
		    SS[id][ss_id].o_x2, SS[id][ss_id].o_y2);
    }
    INTERNAL_SetScrollPosV(id,
			   0,
			   SMAX,
			   FALSE);
    INTERNAL_SetScrollPosH(id,
			   0,
			   SMAX,
			   FALSE);

    g_push_viewport();
    for (i = 0; i < SS[id][0].vp_count; i++) {
	if (SS[id][i].flag & G_SCROLL_CLEARVIEWPORT) {
	    g_select_viewport(SS[id][i].vp_id);
	    g_clear_viewport();
	}
    }
    g_pop_viewport();
    INTERNAL_update_window(G_SCREEN, id);
}

void g_SCREEN_scrollbar_disconnect(int win_id)
{
    int id, device, ss_id;

    id = GET_LOCAL_ID(win_id);
    device = GET_DEVICE(win_id);
    if (id < 0 || id >= G_MAXWINDOW)
	return;
    if (device != G_SCREEN)
	return;
    if (SS[id] == NULL)
	return;
    if (!INTERNAL_ScrollBarsPresent(id))
	return;
    INTERNAL_SetScrollPosH(id,
		           0,
		           SMAX,
			   FALSE);
    INTERNAL_SetScrollPosV(id,
			   0,
			   SMAX,
			   FALSE);
    free(SS[id]);
    SS[id] = NULL;
}

int g_SCREEN_scrollbar_set_dominant_viewport(int win_id, int vp_id)
{
    int i, id, device;
    SCROLLSTRUCT SS_TMP;    

    id = GET_LOCAL_ID(win_id);
    device = GET_DEVICE(win_id);
    if (id < 0 || id >= G_MAXWINDOW)
	return FALSE;
    if (device != G_SCREEN)
	return FALSE;
    if (SS[id] == NULL)
	return FALSE;
    if (!INTERNAL_ScrollBarsPresent(id))
	return FALSE;
    if (SS[id][0].vp_id == vp_id)
        return TRUE;
    for (i = 1; i < SS[id][0].vp_count; i++) {
        if (SS[id][i].vp_id == vp_id) {
            memcpy(&SS_TMP,        &(SS[id][0]),   sizeof(SCROLLSTRUCT));
            memcpy(&(SS[id][0]),   &(SS[id][i]),   sizeof(SCROLLSTRUCT));
            memcpy(&(SS[id][i]),   &SS_TMP,        sizeof(SCROLLSTRUCT));
	    SS[id][0].vp_count      = SS_TMP.vp_count;
	    SS[id][0].size_v        = SS_TMP.size_v;
	    SS[id][0].posvertscroll = SS_TMP.posvertscroll;
	    SS[id][0].size_h        = SS_TMP.size_h;
	    SS[id][0].poshorzscroll = SS_TMP.poshorzscroll;
            SS_TMP.flag             = SS[id][i].flag;
            SS[id][i].flag          = SS[id][0].flag;
            SS[id][0].flag          = SS_TMP.flag;
/*
            if (SS_TMP.flag & G_SCROLL_CLEARVIEWPORT) {
                SS[id][i].flag -= G_SCROLL_CLEARVIEWPORT;
                if (!(SS[id][0].flag & G_SCROLL_CLEARVIEWPORT))
                    SS[id][0].flag +=  G_SCROLL_CLEARVIEWPORT;
            }
*/
            return TRUE;       
        }
    }
    return FALSE;
}

int INTERNAL_UpdateScrollBarsH(int id, int message, int where)
{
    int i;
    if (id == G_MAXWINDOW)
        return FALSE;
    if (SS[id]== NULL)
	return FALSE;
    if (!(SS[id][0].flag & G_SCROLL_LASTVIEWPORT))
        return FALSE;
    if (INTERNAL_object_playback_flag)
        return FALSE;

    if (message == SCROLL_GETVAL) 
        SS[id][0].poshorzscroll =
	    INTERNAL_GetScrollPosH(id);
    else {
        switch (message) {

	case SCROLL_LINEDOWN:
	    SS[id][0].poshorzscroll += SMAX / 100;
	    break;
	case SCROLL_LINEUP:
	    SS[id][0].poshorzscroll -= SMAX / 100;
	    break;
	case SCROLL_PAGEDOWN:
	    SS[id][0].poshorzscroll += SMAX / 10;
	    break;
	case SCROLL_PAGEUP:
	    SS[id][0].poshorzscroll -= SMAX / 10;
	    break;
	case SCROLL_DRAG:
	    SS[id][0].poshorzscroll = where;
	    break;
        }
        SS[id][0].poshorzscroll = max(0,SS[id][0].poshorzscroll);
        SS[id][0].poshorzscroll = min(SMAX - SS[id][0].size_h,
	 			  SS[id][0].poshorzscroll);
        if (SS[id][0].poshorzscroll != 
	    INTERNAL_GetScrollPosH(id)) {
            INTERNAL_SetScrollPosH(id,
			       SS[id][0].poshorzscroll,
			       SS[id][0].size_h,
			       TRUE);
        }
        else
            return FALSE;

    }
    for (i = 0; i < SS[id][0].vp_count; i++) {
	if (SS[id][0].size_h == SMAX)
	    SS[id][i].n_x1 = SS[id][i].o_x1;
	else
	    SS[id][i].n_x1 = SS[id][i].o_x1 + SS[id][i].reverse_x *
			SS[id][i].xrange *
			(float) SS[id][0].poshorzscroll /
			((float) SMAX - SS[id][0].size_h);
	g_set_world(SS[id][i].vp_id,
		    SS[id][i].n_x1, SS[id][i].n_y1,
		    SS[id][i].n_x1 + SS[id][i].n_dx,
		    SS[id][i].n_y1 + SS[id][i].n_dy);
    }
    g_push_viewport();
    for (i=0; i<SS[id][0].vp_count;i++) {
       if (SS[id][i].flag & G_SCROLL_CLEARVIEWPORT) {
	   g_select_viewport(SS[id][i].vp_id);
	   g_clear_viewport();
       }
    }
    g_pop_viewport();
    return TRUE;
}

int INTERNAL_UpdateScrollBarsV(int id, int message, int where)
{
    int i;
    if (id == G_MAXWINDOW)
	return FALSE;
    if (SS[id]== NULL)
	return FALSE;
    if (!(SS[id][0].flag & G_SCROLL_LASTVIEWPORT))
	return FALSE;
    if (INTERNAL_object_playback_flag)
	return FALSE;
    if (message == SCROLL_GETVAL) 
        SS[id][0].posvertscroll =
	    INTERNAL_GetScrollPosV(id);
    else {
        switch (message) {

	case SCROLL_LINEDOWN:
	    SS[id][0].posvertscroll += SMAX / 100;
	    break;
	case SCROLL_LINEUP:
	    SS[id][0].posvertscroll -= SMAX / 100;
	    break;
	case SCROLL_PAGEDOWN:
	    SS[id][0].posvertscroll += SMAX / 10;
	    break;
	case SCROLL_PAGEUP:
	    SS[id][0].posvertscroll -= SMAX / 10;
	    break;
	case SCROLL_DRAG:
	    SS[id][0].posvertscroll = where;
	    break;
        }
        SS[id][0].posvertscroll = max(0,SS[id][0].posvertscroll);
        SS[id][0].posvertscroll = min(SMAX - SS[id][0].size_v,
				  SS[id][0].posvertscroll);
       if (SS[id][0].posvertscroll !=
	    INTERNAL_GetScrollPosV(id)) {
	    INTERNAL_SetScrollPosV(id,
			       SS[id][0].posvertscroll,
			       SS[id][0].size_v,
			       TRUE);
        }
        else
            return FALSE;
    }
    for (i = 0; i < SS[id][0].vp_count; i++) {
	if (SS[id][0].size_v == SMAX)
	     SS[id][i].n_y1 = SS[id][i].o_y1;
	else
	    SS[id][i].n_y1 = SS[id][i].o_y1 + SS[id][i].reverse_y *
			     SS[id][i].yrange *
			     (float) (SMAX - SS[id][0].size_v - SS[id][0].posvertscroll) /
			     ((float) SMAX - SS[id][0].size_v);
	g_set_world(SS[id][i].vp_id,
		    SS[id][i].n_x1, SS[id][i].n_y1,
		    SS[id][i].n_x1 + SS[id][i].n_dx,
		    SS[id][i].n_y1 + SS[id][i].n_dy);
    }
    g_push_viewport();
    for (i=0; i<SS[id][0].vp_count;i++) {
       if (SS[id][i].flag & G_SCROLL_CLEARVIEWPORT) {
	   g_select_viewport(SS[id][i].vp_id);
	   g_clear_viewport();
       }
    }
    g_pop_viewport();
    return TRUE;
}




