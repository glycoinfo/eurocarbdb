/************************************************************************/
/*                               genmmen.c                              */
/*                                                                      */
/*  Platform : Motif                                                    */
/*  Module   : Genplot User Interface functions                         */
/*                                                                      */
/*  Albert van Kuik                                                     */
/************************************************************************/

#define MEMORYLEAK

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
#include "genmotif.h"

#define min(a,b)        (((a) < (b)) ? (a) : (b))
#define max(a,b)        (((a) > (b)) ? (a) : (b))

static void set_keylock(int win_id, int tf);
void INTERNAL_init_commandqueue(void);
void INTERNAL_exit_commandqueue(void);
static char *get_commandline(void);
static void init_labelqueue(void);
static void exit_labelqueue(void);
char *INTERNAL_get_label(void);
static void init_menu(void);
static void exit_menu(void);
void INTERNAL_init_popup(void);
static void popup_tooltip(int menu_id);
static void popup_hide_tooltip(int menu_id);



void INTERNAL_init_screen_stuff(void)
{
    init_menu();
    INTERNAL_init_scroll();
    INTERNAL_init_commandqueue();
    init_labelqueue();
    g_popup_follow_cursor(FALSE);
    INTERNAL_init_popup();
}

void INTERNAL_exit_screen_stuff(void)
{
    exit_menu();
    INTERNAL_exit_scroll();
    INTERNAL_exit_commandqueue();
    exit_labelqueue();
}

/* -- process all pending events NOW --- */
/****************************************************************
*  Menu's
*/

/* --- MENU BUTTON LABEL  QUEUE --- */
#define BUTTONQMAX 1000
static char *button_queue[BUTTONQMAX+1];
static int q_buttonhead, q_buttontail;

void INTERNAL_add_label(char *label)
{
    if (label == NULL)
        return;
    q_buttontail++;
    if (q_buttontail > BUTTONQMAX) q_buttontail = 0;
    if (q_buttontail == q_buttonhead) {
        (q_buttontail == 0) ? (q_buttontail = BUTTONQMAX) : (q_buttontail--);
        return;
    }
    if (button_queue[q_buttontail] != NULL)
        free (button_queue[q_buttontail]);
    button_queue[q_buttontail] = (char*) malloc(strlen(label) + 1);
    strcpy(button_queue[q_buttontail],label);
}

static int is_label(void)
{
    return (!(q_buttonhead == q_buttontail));
}


static void init_labelqueue(void)
{
    int i;

    for (i=0;i<=BUTTONQMAX;i++)
        button_queue[i] = NULL;
    q_buttonhead = q_buttontail = 0;
}

static void exit_labelqueue(void)
{
    int i;

    for (i=0;i<=BUTTONQMAX;i++)
        if (button_queue[i] != NULL) {
            free(button_queue[i]);
            button_queue[i] = NULL;
        }
    q_buttonhead = q_buttontail = 0;
}

char *INTERNAL_get_label(void)
{
    if (!is_label())
        return NULL;
    q_buttonhead++;
    if (q_buttonhead > BUTTONQMAX) q_buttonhead = 0;
    return  button_queue[q_buttonhead];
}


/*---------------------*/
#define IS_NOPARENT 		1
#define IS_POPUP    		2
#define IS_MENUBAR		3
#define IS_MENUBAR2  		4
#define IS_BUTTONBAR		5
#define IS_BUTTONBAR2		6
#define IS_FLOATING		7

#define MAX_IBUTTON	4
#define MAXMENU 300
typedef struct tagMENUTYPE {
    Widget  hmenu;
    Widget  cmenu;
    Widget  tooltip;
    XtIntervalId timer;
    int     return_id;
    int     isparent;
    int     isfirstchild;
    int     ispopuphandler;
    int     win_id;
    int     menu_type;
    int     isgroup;
    Pixmap  pix[MAX_IBUTTON];
    Pixmap  pixgray[MAX_IBUTTON];
    float   vp_x;
    float   vp_y;
} MENUTYPE;


static int menutype_count;
static MENUTYPE *MT;

static char get_mnemonic(char *dest, char *src)
{
    int i,j;
    char mnm = 0;

    for (i=0, j=0; src[i]; i++, j++) {
        if (src[i] == '&' && !mnm) {
            i++;
            mnm = src[i];
        }
        dest[j] = src[i];
    }
    dest[j] = '\0';
    return mnm;
}

static char *get_accellerator(char *src)
{
    char *p = src;

    for (p=src;*p != '\0'; p++) {
        if (*p == '\t') {
            *p = '\0';
            p++;
            return p;
        }
    }
    return NULL;
}

static void uncheck_radio_group(int id)
{
    int i;
    Boolean state;
    Arg args[2];
    
    for (i=0; i<menutype_count;i++)
        if (!MT[i].isparent && MT[i].isgroup == MT[id].isgroup &&
            MT[i].win_id == MT[id].win_id &&
            i != id) {
                XtSetArg(args[0], XmNset, &state);
                XtGetValues(MT[i].hmenu, args, 1);
                if (state) {
                    state = 0;
                    XtSetArg(args[0], XmNset, state);
                    XtSetValues(MT[i].hmenu, args, 1);
                }

    }

}

static void RadioButtonCB(Widget w, XtPointer client_data, XtPointer call_data)
{
    Arg args[2];
    int id = (int) client_data;
    Boolean state;

    XtSetArg(args[0], XmNset, &state);
    XtGetValues(MT[id].hmenu, args, 1);
    if (!state) {
        state = 1;
        XtSetArg(args[0], XmNset, state);
        XtSetValues(MT[id].hmenu, args, 1);
        return;
    }
    uncheck_radio_group(id);
}

static void ButtonCB(Widget w, XtPointer client_data, XtPointer call_data)
{
    Arg args[2];
    unsigned char type;
    XmString label;
    char *text;
    G_USERINPUT ui;
    int id = (int) client_data;
    MENUTYPE *m = &(MT[id]);

    XtSetArg(args[0], XmNlabelType, &type); 
    XtGetValues(m->hmenu, args, 1);

    if (type == XmSTRING) {
        XtSetArg(args[0], XmNlabelString, &label); 
        XtGetValues(m->hmenu, args, 1);
        XmStringGetLtoR(label, XmFONTLIST_DEFAULT_TAG, &text);
        INTERNAL_add_label(text);
#ifndef MEMORYLEAK
        XmStringFree(label);
        XtFree(text);
#endif
    }

    ui.win_id  = m->win_id;
    ui.vp_id   = G_ERROR;
    ui.x       = 0;
    ui.y       = 0;
    ui.event   = G_COMMAND;
    ui.keycode = m->return_id - 1;
    if (type == XmSTRING)
        ui.command = INTERNAL_get_label();
    else
        ui.command = NULL;
    INTERNAL_prep_event(&ui);
    if (INTERNAL_process_event(G_COMMAND_EVENT, &ui))
        INTERNAL_add_event(&ui);
}


static void OptionCB(Widget w, XtPointer client_data, XtPointer call_data)
{
    Arg args[2];
    unsigned char type;
    XmString label;
    char *text;
    G_USERINPUT ui;
    int id = ((int) client_data) & 0xFFFF;
    int pos =((int) client_data) >> 16;
    MENUTYPE *m = &(MT[id]);

    XtSetArg(args[0], XmNlabelType, &type); 
    XtGetValues(m->hmenu, args, 1);

    if (type == XmSTRING) {
        XtSetArg(args[0], XmNlabelString, &label); 
        XtGetValues(m->hmenu, args, 1);
        XmStringGetLtoR(label, XmFONTLIST_DEFAULT_TAG, &text);
        INTERNAL_add_label(text);
#ifndef MEMORYLEAK
        XmStringFree(label);
        XtFree(text);
#endif
    }

    ui.win_id  = m->win_id;
    ui.vp_id   = G_ERROR;
    ui.x       = 0;
    ui.y       = 0;
    ui.event   = G_OPTIONMENU;
    ui.keycode = m->return_id - 1;
    ui.item    = pos;
    if (type == XmSTRING)
        ui.command = INTERNAL_get_label();
    else
        ui.command = NULL;
    INTERNAL_prep_event(&ui);
    if (INTERNAL_process_event(G_COMMAND_EVENT, &ui))
        INTERNAL_add_event(&ui);
}


static void Postit(Widget w, XtPointer xpopup, XEvent *xevent, Boolean *bool)
{
    XButtonPressedEvent *event = (XButtonPressedEvent*) xevent;
    Widget popup = (Widget) xpopup;
    
    if (event->button != Button3)
        return;
    XmMenuPosition(popup, event);
    XtManageChild(popup);
}

static void reset_mt(int id)
{
    int i;
    
    MT[id].hmenu           = NULL;
    MT[id].cmenu           = NULL;
    MT[id].tooltip         = NULL;
    MT[id].timer           = 0;
    MT[id].return_id       = 0;
    MT[id].win_id          = G_ERROR;
    MT[id].isparent        = FALSE;
    MT[id].isfirstchild    = FALSE;
    MT[id].ispopuphandler  = FALSE;
    MT[id].menu_type       = 0;
    MT[id].isgroup         = FALSE;
    MT[id].vp_x            = 0;
    MT[id].vp_y            = 0;
    for (i=0;i<MAX_IBUTTON;i++) {
        MT[id].pix[i] = (Pixmap) 0;
        MT[id].pixgray[i] = (Pixmap) 0;
    }
}

static void alloc_menu(void)
{
    int i, start = menutype_count;

    menutype_count += MAXMENU;
    if ((MT = (MENUTYPE*)
        realloc(MT,menutype_count * sizeof(MENUTYPE))) == NULL) {
        fprintf(stderr,"Memory allocation error\n");
        exit(1);
    }
    for (i=start; i<menutype_count;i++) 
        reset_mt(i);
}

static void init_menu(void)
{
    int i;

    menutype_count = 0;
    MT = NULL;
    alloc_menu();
}

static void exit_menu(void)
{
    int i;

    for (i=0; i<menutype_count;i++)
        if (MT[i].hmenu != NULL) {
            if (MT[i].isparent)
                XtDestroyWidget(MT[i].hmenu);
        }
    free(MT);
    MT = NULL;
}

static int menu_next_item(void)
{
    int i;

    for (i=0; i<menutype_count;i++)
        if (MT[i].hmenu == NULL)
            break;
    if (i == menutype_count)
        alloc_menu();
    reset_mt(i);
    return i;
}


static void menu_item_destroy_func(Widget w, XtPointer tag, XtPointer reason)
{
    int menu_id = (int) tag;
    reset_mt(menu_id);
}

int g_SCREEN_menu_create_menubar(int win_id)
{
    int  local_win_id, id, n;
    char string[20];
    Arg args[5];

    local_win_id = GET_LOCAL_ID(win_id);
    if (local_win_id < 0 || local_win_id >= G_MAXWINDOW)
        return G_ERROR;
    if ((id = menu_next_item()) == G_ERROR)
        return G_ERROR;

    if (xwini[local_win_id].menubar_widget == NULL) 
        return G_ERROR;
    MT[id].win_id        = local_win_id;
    MT[id].isparent      = TRUE;
    MT[id].isfirstchild  = FALSE;
    MT[id].ispopuphandler= FALSE;
    MT[id].menu_type     = IS_MENUBAR;
    MT[id].cmenu         = NULL;
    MT[id].hmenu         = xwini[local_win_id].menubar_widget;
    XtAddCallback(MT[id].hmenu,
                  XmNdestroyCallback,
                  menu_item_destroy_func,
                  (XtPointer) id);
    return id;
}

static void menubar2_destroy_func(Widget w, XtPointer tag, XtPointer reason)
{
    int local_win_id = (int) tag;
    xwini[local_win_id].menubar2_widget = NULL;
}

int g_SCREEN_menu_create_menubar2(int win_id)
{
    int  local_win_id, id, n;
    char Name[20];
    Arg args[10];

    local_win_id = GET_LOCAL_ID(win_id);
    if (local_win_id < 0 || local_win_id >= G_MAXWINDOW)
        return G_ERROR;
    if ((id = menu_next_item()) == G_ERROR)
        return G_ERROR;

    MT[id].win_id        = local_win_id;
    MT[id].isparent      = TRUE;
    MT[id].isfirstchild  = FALSE;
    MT[id].ispopuphandler= FALSE;
    MT[id].menu_type     = IS_MENUBAR2;
    MT[id].cmenu         = NULL;

    if (xwini[local_win_id].menubar2_widget == NULL) {
        sprintf(Name, "menubar2_%d", id);
        n=0;
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
        xwini[local_win_id].menubar2_widget = 
            XmCreateSimpleMenuBar(xwini[local_win_id].pane_widget,
                                             Name, args, n);
    }
    if (!XtIsManaged(xwini[local_win_id].menubar2_widget))
        XtManageChild(xwini[local_win_id].menubar2_widget);
    MT[id].hmenu = xwini[local_win_id].menubar2_widget;
    XtAddCallback(xwini[local_win_id].menubar2_widget,
                  XmNdestroyCallback,
                  menubar2_destroy_func,
                  (XtPointer) local_win_id);
    XtAddCallback(MT[id].hmenu,
                  XmNdestroyCallback,
                  menu_item_destroy_func,
                  (XtPointer) id);
    return id;
}

static void buttonbar_destroy_func(Widget w, XtPointer tag, XtPointer reason)
{
    int local_win_id = (int) tag;
    xwini[local_win_id].buttonbar_widget = NULL;
}

static void buttonbar2_destroy_func(Widget w, XtPointer tag, XtPointer reason)
{
    int local_win_id = (int) tag;
    xwini[local_win_id].buttonbar2_widget = NULL;
}

static int create_buttonbar(int win_id, int position, int rows)
{
    int  local_win_id, id, n;
    char Name[20];
    Arg args[10];
    Widget buttonbar;
    int menubarheight = MENUBARHEIGHT;
    int marginheight  = 2;
    int spacing       = 0;
    Dimension height;
    Widget lab;

    local_win_id = GET_LOCAL_ID(win_id);
    if (local_win_id < 0 || local_win_id >= G_MAXWINDOW)
        return G_ERROR;
    if ((id = menu_next_item()) == G_ERROR)
        return G_ERROR;

    MT[id].win_id    = local_win_id;
    MT[id].isparent  = TRUE;
    MT[id].isfirstchild  = FALSE;
    MT[id].ispopuphandler= FALSE;
    if (position)
        MT[id].menu_type = IS_BUTTONBAR2;
    else
        MT[id].menu_type = IS_BUTTONBAR;
    MT[id].cmenu     = NULL;

    if (position)
        buttonbar = xwini[local_win_id].buttonbar2_widget;
    else
        buttonbar = xwini[local_win_id].buttonbar_widget;

    lab =  XmCreatePushButton(xwini[local_win_id].pane_widget, "8", args, 0);
    XtVaGetValues(lab,  XmNheight, &(height), NULL);
    if (height) 
        menubarheight = 2 * marginheight + spacing * (rows-1) + 
                        height * rows + (rows-1) * 3;
    else
        menubarheight *= rows;
#ifndef MEMORYLEAK
    XtDestroyWidget(lab);
#endif

    if (buttonbar == NULL) {
        sprintf(Name, "buttonbar_%d", id);
        n=0;
        XtSetArg(args[n], XmNspacing, spacing);
        n++;
        XtSetArg(args[n], XmNmarginHeight, marginheight);
        n++;
        XtSetArg(args[n], XmNpositionIndex, position);
        n++;
        XtSetArg(args[n], XmNorientation, XmHORIZONTAL);
        n++;
        XtSetArg(args[n], XmNresizeHeight, FALSE);
        n++;
        XtSetArg(args[n], XmNheight, menubarheight);
        n++;
        XtSetArg(args[n], XmNpaneMinimum, menubarheight);
        n++;
        if (position && rows > 1) {
            XtSetArg(args[n], XmNnumColumns, rows);
            n++;
            XtSetArg(args[n], XmNpacking, XmPACK_COLUMN);
            n++;
        }
        buttonbar = XmCreateRowColumn(xwini[local_win_id].pane_widget,
                                             Name, args, n);
        XtManageChild(buttonbar);
    }
    else if (position && rows > 1) {
        int height = menubarheight;
        n=0;
        XtSetArg(args[n], XmNspacing, spacing);
        n++;
        XtSetArg(args[n], XmNmarginHeight, marginheight);
        n++;
        XtSetArg(args[n], XmNheight, height);
        n++;
        XtSetArg(args[n], XmNpaneMinimum, height);
        n++;
        XtSetArg(args[n], XmNpaneMaximum, height);
        n++;
        XtSetArg(args[n], XmNnumColumns, rows);
        n++;
        XtSetArg(args[n], XmNpacking, XmPACK_COLUMN);
        n++;
        XtSetValues(buttonbar, args, n);
        if (!XtIsManaged(buttonbar))
            XtManageChild(buttonbar);
    }
    MT[id].hmenu = buttonbar;
    if (position) {
        xwini[local_win_id].buttonbar2_widget = buttonbar;
        XtAddCallback(xwini[local_win_id].buttonbar2_widget,
                  XmNdestroyCallback,
                  buttonbar2_destroy_func,
                  (XtPointer) local_win_id);
    }
    else {
        xwini[local_win_id].buttonbar_widget = buttonbar;
        XtAddCallback(xwini[local_win_id].buttonbar_widget,
                  XmNdestroyCallback,
                  buttonbar_destroy_func,
                  (XtPointer) local_win_id);
    }
    XtAddCallback(MT[id].hmenu,
                  XmNdestroyCallback,
                  menu_item_destroy_func,
                  (XtPointer) id);
    return id;
}

int g_SCREEN_menu_create_buttonbar(int win_id)
{
    return create_buttonbar(win_id, 0, 1);
}

int g_SCREEN_menu_create_buttonbar2(int win_id)
{
    return create_buttonbar(win_id, XmLAST_POSITION, 1);
}

int g_SCREEN_menu_create_buttonbox2(int win_id, int rows)
{
    Arg args[10];
    int ret, local_win_id = GET_LOCAL_ID(win_id);
    Dimension height, b_height=0;

    if (rows <= 0)
        return -1;
    if (xwini[local_win_id].buttonbar2_widget) {
        XtSetArg(args[0], XmNheight, &b_height);
        XtGetValues(xwini[local_win_id].buttonbar2_widget, args, 1);
    }
    ret = create_buttonbar(win_id, XmLAST_POSITION, max(1,rows));
    if (xwini[local_win_id].buttonbar2_widget) {
        Dimension c_height=0;
        XtSetArg(args[0], XmNheight, &c_height);
        XtGetValues(xwini[local_win_id].buttonbar2_widget, args, 1);
        b_height = c_height - b_height;
    }
    XtSetArg(args[0], XmNheight, &height);
    XtGetValues(xwini[local_win_id].toplevel_widget, args, 1);
    XtSetArg(args[0], XmNheight, height + b_height);
    XtSetValues(xwini[local_win_id].toplevel_widget, args, 1);

    return ret;
}

static void destroy_menu(int win_id, int type, int killparent)
{
    int i, local_win_id;

    local_win_id = GET_LOCAL_ID(win_id);
    if (local_win_id < 0 || local_win_id >= G_MAXWINDOW)
        return;

    for (i=0; i<menutype_count;i++)
        if (MT[i].hmenu != NULL && MT[i].win_id == local_win_id 
                                                && MT[i].menu_type == type) {
            if (MT[i].ispopuphandler) {
                Arg args[5];
                
                XtRemoveRawEventHandler(xwini[local_win_id].draw_widget,
                                     ButtonPressMask,
                                     FALSE,
                                     (XtEventHandler)Postit,
                                     MT[i].hmenu);
                XtSetArg(args[0], XmNpopupEnabled, FALSE); 
                XtSetValues(MT[i].hmenu, args, 1);
            }
#ifndef MEMORYLEAK
            if (type == IS_POPUP && MT[i].isparent)
                XtDestroyWidget(MT[i].hmenu);
            if (type == IS_MENUBAR && MT[i].isfirstchild) {
                if (MT[i].cmenu)
                    XtDestroyWidget(MT[i].cmenu);
                XtDestroyWidget(MT[i].hmenu);
            }
            if (!killparent && type == IS_MENUBAR2 && MT[i].isfirstchild) {
                if (MT[i].cmenu)
                    XtDestroyWidget(MT[i].cmenu);
                XtDestroyWidget(MT[i].hmenu);
            }
            if (killparent && type == IS_MENUBAR2 && MT[i].isparent) {
                XtDestroyWidget(MT[i].hmenu);
            }
            if (!killparent && type == IS_BUTTONBAR && MT[i].isfirstchild) {
                if (MT[i].cmenu)
                    XtDestroyWidget(MT[i].cmenu);
                XtDestroyWidget(MT[i].hmenu);
            }
            if (!killparent && type == IS_BUTTONBAR2 && MT[i].isfirstchild) {
                if (MT[i].cmenu)
                    XtDestroyWidget(MT[i].cmenu);
                XtDestroyWidget(MT[i].hmenu);
            }
            if (killparent && type == IS_BUTTONBAR && MT[i].isparent) {
                XtDestroyWidget(MT[i].hmenu);
            }
            if (killparent && type == IS_BUTTONBAR2 && MT[i].isparent) {
                XtDestroyWidget(MT[i].hmenu);
            }
            if (type == IS_FLOATING && MT[i].isfirstchild) {
                XtDestroyWidget(MT[i].hmenu);
            }
            if (killparent && type == IS_FLOATING && MT[i].isparent) {
                reset_mt(i);
            }
#else
            if (type == IS_POPUP && MT[i].isparent)
                XtUnmanageChild(MT[i].hmenu);
            if (type == IS_MENUBAR && MT[i].isfirstchild) {
                if (MT[i].cmenu)
                    XtUnmanageChild(MT[i].cmenu);
                XtUnmanageChild(MT[i].hmenu);
            }
            if (!killparent && type == IS_MENUBAR2 && MT[i].isfirstchild) {
                if (MT[i].cmenu)
                    XtUnmanageChild(MT[i].cmenu);
                XtUnmanageChild(MT[i].hmenu);
            }
            if (killparent && type == IS_MENUBAR2 && MT[i].isparent) {
                XtUnmanageChild(MT[i].hmenu);
/*                xwini[local_win_id].menubar2_widget = NULL;*/
            }
            if (!killparent && type == IS_BUTTONBAR && MT[i].isfirstchild) {
                if (MT[i].cmenu)
                    XtUnmanageChild(MT[i].cmenu);
                XtUnmanageChild(MT[i].hmenu);
            }
            if (!killparent && type == IS_BUTTONBAR2 && MT[i].isfirstchild) {
                if (MT[i].cmenu)
                    XtUnmanageChild(MT[i].cmenu);
                XtUnmanageChild(MT[i].hmenu);
            }
            if (killparent && type == IS_BUTTONBAR && MT[i].isparent) {
                XtUnmanageChild(MT[i].hmenu);
/*                xwini[local_win_id].buttonbar_widget = NULL;*/
            }
            if (killparent && type == IS_BUTTONBAR2 && MT[i].isparent) {
                XtUnmanageChild(MT[i].hmenu);
/*                xwini[local_win_id].buttonbar2_widget = NULL;*/
            }
            if (type == IS_FLOATING && MT[i].isfirstchild) {
                XtUnmanageChild(MT[i].hmenu);
            }
            if (killparent && type == IS_FLOATING && MT[i].isparent) {
                reset_mt(i);
            }
#endif
    }
}


void g_SCREEN_menu_destroy_popup(int win_id)
{
    destroy_menu(win_id, IS_POPUP, TRUE);
}

void g_SCREEN_menu_destroy_menubar_children(int win_id)
{
    destroy_menu(win_id, IS_MENUBAR, FALSE);
}

void g_SCREEN_menu_destroy_menubar2_children(int win_id)
{
    destroy_menu(win_id, IS_MENUBAR2, FALSE);
}

void g_SCREEN_menu_destroy_menubar2(int win_id)
{
    destroy_menu(win_id, IS_MENUBAR2, TRUE);
}

void g_SCREEN_menu_destroy_buttonbar(int win_id)
{
    destroy_menu(win_id, IS_BUTTONBAR, TRUE);
}

void g_SCREEN_menu_destroy_buttonbar2(int win_id)
{
    destroy_menu(win_id, IS_BUTTONBAR2, TRUE);
}

void g_SCREEN_menu_destroy_buttonbar_children(int win_id)
{
    destroy_menu(win_id, IS_BUTTONBAR, FALSE);
}

void g_SCREEN_menu_destroy_buttonbar2_children(int win_id)
{
    destroy_menu(win_id, IS_BUTTONBAR2, FALSE);
}

void g_SCREEN_menu_destroy_floating(int win_id)
{
    destroy_menu(win_id, IS_FLOATING, TRUE);
}

void g_SCREEN_menu_destroy_floating_children(int win_id)
{
    destroy_menu(win_id, IS_FLOATING, FALSE);
}

void g_SCREEN_menu_destroy_button(int menu_id, int item_id)
{
    int i;

    if (menu_id < 0 || menu_id >= menutype_count || MT[menu_id].hmenu == NULL)
        return;
    for (i=0; i<menutype_count;i++)
        if (MT[i].return_id - 1 == item_id &&
            MT[menu_id].win_id == MT[i].win_id) {
#ifndef MEMORYLEAK
            XtDestroyWidget(MT[i].hmenu); 
#else           
            XtUnmanageChild(MT[i].hmenu);
#endif
            return;
    }
}


int g_SCREEN_menu_create_floating(int win_id)
{
    int  i, local_win_id, id;
    char string[20];
    Arg args[5];

    local_win_id = GET_LOCAL_ID(win_id);
    if (local_win_id < 0 || local_win_id >= G_MAXWINDOW)
        return G_ERROR;
    if ((id = menu_next_item()) == G_ERROR)
        return G_ERROR;

    sprintf(string, "floating%d", id);
    MT[id].win_id        = local_win_id;
    MT[id].isparent      = TRUE;
    MT[id].isfirstchild  = FALSE;
    MT[id].ispopuphandler= FALSE;
    MT[id].menu_type     = IS_FLOATING;
    MT[id].cmenu         = NULL;
    MT[id].hmenu         = xwini[local_win_id].draw_widget;

    return id;
}

void INTERNAL_update_floatbutton_position(int local_win_id, int width, int height)
{
    int i,x,y,n;
    Arg args[10];

    for (i=0; i<menutype_count;i++)
        if (MT[i].hmenu != NULL && MT[i].win_id == local_win_id &&
            MT[i].isfirstchild == TRUE && MT[i].menu_type == IS_FLOATING) {
                x = (int) (MT[i].vp_x * width);
                y = (int) ((1.0 - MT[i].vp_y) * height);
                n=0;
                XtSetArg(args[n], XmNx, x); n++;
                XtSetArg(args[n], XmNy, y); n++;
                XtSetValues(MT[i].hmenu, args, n);
        }
}

int g_SCREEN_menu_append_floating_button(int menu_id, char *label, int return_id,
                            float vp_x, float vp_y)
{
    int id, x, y, n;
    Arg args[10];

    if (menu_id < 0 || menu_id >= menutype_count || MT[menu_id].hmenu == NULL)
        return G_ERROR;
    if ((id = menu_next_item()) == G_ERROR)
        return G_ERROR;

    MT[id].win_id = MT[menu_id].win_id;
    MT[id].menu_type = MT[menu_id].menu_type;
    MT[id].isparent= FALSE;
    MT[id].isfirstchild= TRUE;
    MT[id].ispopuphandler= FALSE;
    MT[id].return_id = return_id + 1;
    MT[id].vp_x = vp_x;
    MT[id].vp_y = vp_y;
    
    if (vp_x < 0.0 || vp_x > 1.0)
        return G_ERROR;
    if (vp_y < 0.0 || vp_y > 1.0)
        return G_ERROR;
    x = (int) (vp_x * xwini[MT[id].win_id].width);
    y = (int) ((1.0 - vp_y) * xwini[MT[id].win_id].height);
    n=0;
    XtSetArg(args[n], XmNx, x); n++;
    XtSetArg(args[n], XmNy, y); n++;
    XtSetArg(args[n], XmNshadowThickness, 0); n++;
    XtSetArg(args[n], XmNmarginHeight, 1); n++;
    XtSetArg(args[n], XmNmarginWidth, 1); n++;
    XtSetArg(args[n], XmNtraversalOn, FALSE ); n++;
    XtSetArg(args[n], XmNlabelString, 
    XmStringCreate(label, XmSTRING_DEFAULT_CHARSET)); n++;

    MT[id].hmenu = XmCreatePushButton(MT[menu_id].hmenu,
                                                label,
                                                args,
                                                n);
    XtAddCallback(MT[id].hmenu,
                  XmNactivateCallback,
                  ButtonCB,
                  (XtPointer)id);
    XtManageChild(MT[id].hmenu);
    XtAddCallback(MT[id].hmenu,
                  XmNdestroyCallback,
                  menu_item_destroy_func,
                  (XtPointer) id);
    return G_OK;
}


int g_SCREEN_menu_create_popup(int win_id)
{
    int  i, local_win_id, id;
    char string[20];
    Arg args[5];

    local_win_id = GET_LOCAL_ID(win_id);
    if (local_win_id < 0 || local_win_id >= G_MAXWINDOW)
        return G_ERROR;
    if ((id = menu_next_item()) == G_ERROR)
        return G_ERROR;

    for (i=0; i<menutype_count;i++)
        if (MT[i].hmenu != NULL && MT[i].win_id == local_win_id &&
            MT[i].ispopuphandler && MT[i].isparent) {
                XtRemoveRawEventHandler(xwini[local_win_id].draw_widget,
                                     ButtonPressMask,
                                     FALSE,
                                     (XtEventHandler)Postit,
                                     MT[i].hmenu); 
                XtSetArg(args[0], XmNpopupEnabled, FALSE); 
                XtSetValues(MT[i].hmenu, args, 1);
                MT[i].ispopuphandler = FALSE;
                break;
    }

    sprintf(string, "popup%d", id);
    MT[id].win_id        = local_win_id;
    MT[id].isparent      = TRUE;
    MT[id].isfirstchild  = FALSE;
    MT[id].ispopuphandler= TRUE;
    MT[id].menu_type     = IS_POPUP;
    MT[id].cmenu         = NULL;
    XtSetArg(args[0], XmNpopupEnabled, TRUE); 
    MT[id].hmenu = XmCreateSimplePopupMenu(xwini[local_win_id].draw_widget,
                                             string, args, 1);

    XtAddRawEventHandler(xwini[local_win_id].draw_widget,
                      ButtonPressMask,
                      FALSE,
                      (XtEventHandler)Postit,
                      MT[id].hmenu);
    XtAddCallback(MT[id].hmenu,
                  XmNdestroyCallback,
                  menu_item_destroy_func,
                  (XtPointer) id);
    return id;
}

void g_SCREEN_menu_select_popup(int menu_id)
{
    int i;
    Arg args[5];

    if (menu_id < 0 || 
        menu_id >= menutype_count || 
        MT[menu_id].hmenu == NULL || 
        xwini[MT[menu_id].win_id].draw_widget == NULL) 
        return;                                      

    for (i=0; i<menutype_count;i++)
        if (MT[i].hmenu != NULL && MT[i].win_id == MT[menu_id].win_id  &&
            MT[i].ispopuphandler && MT[i].isparent) {
                XtRemoveRawEventHandler(xwini[MT[i].win_id].draw_widget,
                                     ButtonPressMask,
                                     FALSE,
                                     (XtEventHandler)Postit,
                                     MT[i].hmenu); 
                XtSetArg(args[0], XmNpopupEnabled, FALSE); 
                XtSetValues(MT[i].hmenu, args, 1);
                MT[i].ispopuphandler = FALSE;
                break;
    }

    XtSetArg(args[0], XmNpopupEnabled, TRUE); 
    XtSetValues(MT[menu_id].hmenu, args, 1);
    XtAddRawEventHandler(xwini[MT[menu_id].win_id].draw_widget,
                      ButtonPressMask,
                      FALSE,
                      (XtEventHandler)Postit,
                      MT[menu_id].hmenu);
    MT[menu_id].ispopuphandler = TRUE;
}


void g_SCREEN_menu_hide_popup(int win_id)
{
    int  i, local_win_id;

    local_win_id = GET_LOCAL_ID(win_id);
    if (local_win_id < 0 || local_win_id >= G_MAXWINDOW)
        return;

    for (i=0; i<menutype_count;i++)
        if (MT[i].hmenu != NULL && MT[i].win_id == local_win_id  &&
            MT[i].ispopuphandler && MT[i].isparent) {
                Arg args[5];
                XtRemoveRawEventHandler(xwini[local_win_id].draw_widget,
                                     ButtonPressMask,
                                     FALSE,
                                     (XtEventHandler)Postit,
                                     MT[i].hmenu); 
                XtSetArg(args[0], XmNpopupEnabled, FALSE); 
                XtSetValues(MT[i].hmenu, args, 1);

                MT[i].ispopuphandler = FALSE;
                return;
    }
}


int g_SCREEN_menu_append_submenu(int menu_id, char *label)
{
    int id,n;
    char string[20];
    Arg args[5];
    char newlabel[100];
    char mnemonic, *accel;

    if (menu_id < 0 || menu_id >= menutype_count || MT[menu_id].hmenu == NULL)
        return G_ERROR;
    if ((id = menu_next_item()) == G_ERROR)
        return G_ERROR;
    sprintf(string, "submenu%d", id);
    MT[id].win_id        = MT[menu_id].win_id;
    MT[id].menu_type     = MT[menu_id].menu_type;
    MT[id].ispopuphandler= FALSE;
    MT[id].hmenu = XmCreatePulldownMenu(MT[menu_id].hmenu,
                                        string,
                                        NULL,
                                        0);
    if (MT[menu_id].isparent && 
            (MT[menu_id].menu_type == IS_MENUBAR ||
             MT[menu_id].menu_type == IS_MENUBAR2 ||
             MT[menu_id].menu_type == IS_BUTTONBAR ||
             MT[menu_id].menu_type == IS_BUTTONBAR2))
        MT[id].isfirstchild  = TRUE;
    else
        MT[id].isfirstchild  = FALSE;
    mnemonic = get_mnemonic(newlabel, label);
    n = 0;
    if (mnemonic) {
        XtSetArg(args[n], XmNmnemonic, mnemonic);
        n++;
    }
    accel = get_accellerator(newlabel);
    if (accel != NULL) {
        XtSetArg(args[n], XmNacceleratorText, 
            XmStringCreateLtoR(accel,XmSTRING_DEFAULT_CHARSET));
        n++;
    }
    XtSetArg(args[n], XmNsubMenuId, MT[id].hmenu);
    n++;
    XtSetArg(args[n], XmNlabelString, XmStringCreate(newlabel,
                                          XmSTRING_DEFAULT_CHARSET));
    n++;
    sprintf(string, "cascade%d", id);
    MT[id].cmenu = XmCreateCascadeButtonGadget(MT[menu_id].hmenu,
                                          string,
                                          args,
                                          n);
    XtManageChild(MT[id].cmenu);
    XtAddCallback(MT[id].hmenu,
                  XmNdestroyCallback,
                  menu_item_destroy_func,
                  (XtPointer) id);
    return id;
}


static void activate_tooltip_popup(XtPointer tag, XtIntervalId *reason)
{
    int menu_id = (int) tag;

    MT[menu_id].timer = 0;
    popup_tooltip(menu_id);
}

static void menu_item_enter_func(Widget w, XtPointer any, XEvent *xevent, Boolean *bool)
{
    int menu_id = (int) any;

    popup_hide_tooltip(menu_id);
    MT[menu_id].timer = XtAppAddTimeOut(XtWidgetToApplicationContext(MT[menu_id].hmenu), 
            750, activate_tooltip_popup, (XtPointer) menu_id);
}

static void menu_item_leave_func(Widget w, XtPointer any, XEvent *xevent, Boolean *bool)
{
    int menu_id = (int) any;

    if (MT[menu_id].timer) {
        XtRemoveTimeOut(MT[menu_id].timer);
        MT[menu_id].timer = 0;
    }
    else
        popup_hide_tooltip(menu_id);
}

static int menu_append_item(int menu_id, char *label, int return_id,
                            int toggle, int toggle_on,
                            int item_count, int item, char *data[])
{
    int id, n;
    char newlabel[100];
    char mnemonic, *accel;
    Boolean state;
    Arg args[10];

    if (menu_id < 0 || menu_id >= menutype_count ||
        MT[menu_id].hmenu == NULL)
            return G_ERROR;
    if ((id = menu_next_item()) == G_ERROR)
        return G_ERROR;

    MT[id].win_id = MT[menu_id].win_id;
    MT[id].menu_type = MT[menu_id].menu_type;
    MT[id].isparent= FALSE;
    if ((MT[menu_id].menu_type == IS_BUTTONBAR ||
            MT[menu_id].menu_type == IS_BUTTONBAR2) && 
            MT[menu_id].isparent)
        MT[id].isfirstchild= TRUE;
    else
        MT[id].isfirstchild= FALSE;
    MT[id].ispopuphandler= FALSE;
    MT[id].return_id = return_id + 1;

    mnemonic = get_mnemonic(newlabel, label);
    n = 0;
    if (mnemonic) {
        XtSetArg(args[n], XmNmnemonic, mnemonic);
        n++;
    }
    accel = get_accellerator(newlabel);
    if (accel != NULL) {
        XtSetArg(args[n], XmNacceleratorText, 
            XmStringCreateLtoR(accel,XmSTRING_DEFAULT_CHARSET));
        n++;
    }
    XtSetArg(args[n], XmNhighlightOnEnter, TRUE);
    n++;
    if (toggle) {
        state = toggle_on;
        XtSetArg(args[n], XmNset, state);
        n++;
/*
        XtSetArg(args[n], XmNvisibleWhenOff, False); 
        n++;
*/ 
       XtSetArg(args[n], XmNshadowThickness, 2); 
        n++;
        if (MT[id].isfirstchild) {
            XtSetArg(args[n], XmNindicatorOn, FALSE);
            n++;
            XtSetArg(args[n], XmNfillOnSelect, TRUE);
            n++;
        }
        MT[id].hmenu = XmCreateToggleButton(MT[menu_id].hmenu,
                                            newlabel,
                                            args,
                                            n);
        XtAddCallback(MT[id].hmenu,
                      XmNvalueChangedCallback,
                      ButtonCB,
                      (XtPointer)id);
    }
    else if (item_count > 0) {
        Widget button,focus,pulldown;
        int i;

        pulldown= XmCreatePulldownMenu(MT[menu_id].hmenu,
                                                newlabel,
                                                NULL,
                                                0);

        XtSetArg(args[n], XmNsubMenuId, pulldown); 
        n++;
        XtSetArg(args[n], XmNlabelString, 
                 XmStringCreate(label, XmSTRING_DEFAULT_CHARSET)); 
        n++;
        MT[id].hmenu = XmCreateOptionMenu(MT[menu_id].hmenu,
                                                newlabel,
                                                args,
                                                n);
        n = 0;
        for (i=0;i<item_count;i++) {   
            button = XmCreatePushButton(pulldown, data[i], args, n);
            XtAddCallback(button, XmNactivateCallback, 
                              OptionCB,
                              (XtPointer) (id + (i<<16)));
            XtManageChild(button);
            if (i == item)
                focus = button;
        }
        n=0;
        XtSetArg(args[n], XmNmenuHistory, focus);
        n++;
        XtSetValues(MT[id].hmenu, args, n);
    }
    else {
        MT[id].hmenu = XmCreatePushButton(MT[menu_id].hmenu,
                                          newlabel,
                                          args,
                                          n);

        XtAddCallback(MT[id].hmenu,
                      XmNactivateCallback,
                      ButtonCB,
                      (XtPointer)id);
    }
    XtManageChild(MT[id].hmenu);
    XtAddCallback(MT[id].hmenu,
                  XmNdestroyCallback,
                  menu_item_destroy_func,
                  (XtPointer) id);

    if (MT[menu_id].menu_type == IS_BUTTONBAR ||
            MT[menu_id].menu_type == IS_BUTTONBAR2) {
        XtAddEventHandler(MT[menu_id].hmenu,
                      EnterWindowMask,
                      FALSE,
                      menu_item_enter_func,
                      (XtPointer) menu_id);
                      
        XtAddEventHandler(MT[menu_id].hmenu,
                      LeaveWindowMask,
                      FALSE,
                      menu_item_leave_func,
                      (XtPointer)menu_id);

        XtAddEventHandler(MT[menu_id].hmenu,
                      ButtonPressMask,
                      FALSE,
                      menu_item_leave_func,
                      (XtPointer)menu_id);
    }           
    return G_OK;
}

int g_SCREEN_menu_append_button(int menu_id, char *label, int return_id)
{
    return menu_append_item(menu_id, label, return_id, FALSE, 
                            0, 0, 0, NULL);
}

int g_SCREEN_menu_append_toggle(int menu_id, char *label, int return_id,
                         int toggle_on)
{
    return menu_append_item(menu_id, label, return_id, TRUE,
                            toggle_on, 0, 0, NULL);
}

int g_SCREEN_menu_append_optionmenu(int menu_id, char *label, 
              int return_id, int item_count, int item, char *data[])
{
    return menu_append_item(menu_id, label, return_id, FALSE,
                            0, item_count, item, data);
}

/************************************/

typedef struct pixcachetype {
    char **xpm;
    Pixmap pix;
    int isgray;
    struct pixcachetype *next;
} PIXCACHE;

static PIXCACHE *pixcache;

static PIXCACHE *new_pixcache(void)
{
    PIXCACHE *pc;

    pc = (PIXCACHE*) malloc(sizeof(PIXCACHE));
    pc->xpm = NULL;
    pc->pix = XmUNSPECIFIED_PIXMAP;
    pc->isgray = FALSE;
    pc->next = NULL;
    return pc;
}

static Pixmap search_pixcache(char **xpm, int isgray)
{
    PIXCACHE *pc;

    if (pixcache)
        for (pc = pixcache; pc->next != NULL; pc = pc->next) 
            if (pc->xpm == xpm && pc->isgray == isgray)
                return pc->pix;
    return XmUNSPECIFIED_PIXMAP;
}

static void add_pixcache(PIXCACHE *pc)
{
    PIXCACHE *pc2;

    if (!pixcache)
        pixcache = pc;
    else {
        for (pc2 = pixcache; pc2->next != NULL; pc2 = pc2->next); 
        pc2->next = pc;
    }
}

/*
#define MAXCOLOR 64
*/
#define MAXCOLOR 128
Pixmap INTERNAL_make_pixmap(int win_id, char *xpm[], int isgray)
{
    unsigned long pixel;
    PIXCACHE *pc;
    Pixmap pix;
    XImage *image;
    int x, xx, y, i, j, width, height, numcolors, cpp, numtrunc;
    Colormap cmap;
    XColor backcolor,xcolor[MAXCOLOR];
    unsigned long valuemask = 0;
    XGCValues values;
    GC gc;
    char colorcode[MAXCOLOR];
    char colorstring[MAXCOLOR][40];
    Arg args[2];


    if ((pix = search_pixcache(xpm, isgray)) != XmUNSPECIFIED_PIXMAP)
        return pix;
    cmap    = DefaultColormap(INTERNAL_display,INTERNAL_screen);
    gc      = XCreateGC(INTERNAL_display,INTERNAL_rootwin,valuemask,&values);

    XtSetArg(args[0], XmNbackground, &backcolor);
    XtGetValues(xwini[win_id].form_widget, args, 1);
    
    sscanf(xpm[0], "%d %d %d %d", &width, &height, &numcolors, &cpp);

    if (width <=0 || height <= 0 || numcolors <= 0 || cpp <= 0)
        return XmUNSPECIFIED_PIXMAP;

    numtrunc = min(numcolors, MAXCOLOR);

    for (i=0;i<numtrunc;i++) {
        j = sscanf(xpm[i+1], "%c c %s",&(colorcode[i]),colorstring[i]);

        if (j<2 || strcasecmp(colorstring[i], "None")==0) {
            strcpy(colorstring[i], "");
            xcolor[i] = backcolor;
        }
        else if (isgray) {
            if (strcasecmp(colorstring[i], "black")==0 ||
                strcasecmp(colorstring[i], "#000000")==0)
                strcpy(colorstring[i],"#888888");
            else if (strcasecmp(colorstring[i], "white")==0 ||
                strcasecmp(colorstring[i], "#ffffff")==0)
                strcpy(colorstring[i],"#cccccc");
            else
                strcpy(colorstring[i],"#999999");
        }
    }
                
    for (i=0;i<numtrunc;i++) {
        if (strlen(colorstring[i]) > 0) { 
            XParseColor(INTERNAL_display,
                        cmap,
                        colorstring[i],
                        &xcolor[i]);
            if (!XAllocColor(INTERNAL_display,cmap,&xcolor[i]))
                xcolor[i].pixel = BlackPixel(INTERNAL_display, INTERNAL_screen);
        }
    }

    pix = XCreatePixmap(INTERNAL_display, INTERNAL_rootwin, width, height, 
                        DefaultDepth(INTERNAL_display,INTERNAL_screen));
    image = XGetImage(INTERNAL_display, pix, 0, 0, width, height, AllPlanes, XYPixmap);

    for (y=0;y<height;y++) {
        for (x=0,xx=0;x<width;x++, xx += cpp)  {
            for (j=0;j<numtrunc;j++) {
                if (xpm[numcolors+1+y][xx] == colorcode[j]) {
                    pixel = xcolor[j].pixel;
                    break;
                }
            }
            XPutPixel(image, x, y, pixel);
        }
    }
    XPutImage(INTERNAL_display, pix, gc, image, 0, 0, 0, 0, width, height);
    XDestroyImage(image);

    pc = new_pixcache();
    pc->xpm = xpm;
    pc->pix = pix;
    pc->isgray = isgray;
    add_pixcache(pc);
    return pix;
}


static int append_pixmap(int menu_id, char *pixinfo[], char *pixgrayinfo[], 
                         char *pixselectinfo[], char *pixselectgrayinfo[], 
                         int return_id, int ismulti, int toggle, int toggle_on)
{
    int  id, n;
    char label[20];
    Boolean state;
    Arg  args[20];
    Pixmap pix, pixgray, pixselect, pixselectgray;
    Pixmap pix2, pix2gray, pix3, pix3gray, pix4, pix4gray;

    if (menu_id < 0 || menu_id >= menutype_count || MT[menu_id].hmenu == NULL)
        return G_ERROR;
    if ((id = menu_next_item()) == G_ERROR)
        return G_ERROR;
    pix = pixgray = pixselect = pixselectgray = (Pixmap)0;
    pix2 = pix2gray = pix3 = pix3gray = pix4 = pix4gray =(Pixmap)0;
    pix = INTERNAL_make_pixmap(MT[menu_id].win_id, pixinfo, FALSE);
    if (pix == XmUNSPECIFIED_PIXMAP)
        return G_ERROR;

    if (ismulti) {
        pixgray = INTERNAL_make_pixmap(MT[menu_id].win_id, pixinfo, TRUE);
        if (pixgrayinfo && (pixinfo != pixgrayinfo)) {
            pix2 = INTERNAL_make_pixmap(MT[menu_id].win_id, pixgrayinfo, FALSE);
            if (pix2 == XmUNSPECIFIED_PIXMAP)
                return G_ERROR;
            pix2gray = INTERNAL_make_pixmap(MT[menu_id].win_id, pixgrayinfo, TRUE);
        }
        if (pixselectinfo && (pixinfo != pixselectinfo)) {
            pix3 = INTERNAL_make_pixmap(MT[menu_id].win_id, pixselectinfo, FALSE);
            if (pix3 == XmUNSPECIFIED_PIXMAP)
                return G_ERROR;
            pix3gray = INTERNAL_make_pixmap(MT[menu_id].win_id, pixselectinfo, TRUE);
        }
        if (pixselectgrayinfo && (pixinfo != pixselectgrayinfo)) {
            pix4 = INTERNAL_make_pixmap(MT[menu_id].win_id, pixselectgrayinfo, FALSE);
            if (pix4 == XmUNSPECIFIED_PIXMAP)
                return G_ERROR;
            pix4gray = INTERNAL_make_pixmap(MT[menu_id].win_id, pixselectgrayinfo, TRUE);
        }
    }
    else {
        if (pixgrayinfo && (pixinfo != pixgrayinfo)) {
            pixgray = INTERNAL_make_pixmap(MT[menu_id].win_id, pixgrayinfo, FALSE);
            if (pixgray == XmUNSPECIFIED_PIXMAP)
                return G_ERROR;
        }
        else
            pixgray = INTERNAL_make_pixmap(MT[menu_id].win_id, pixinfo, TRUE); /* pix; */

        if (pixselectinfo && (pixinfo != pixselectinfo)) {
            pixselect = INTERNAL_make_pixmap(MT[menu_id].win_id, pixselectinfo, FALSE);
            if (pixselect == XmUNSPECIFIED_PIXMAP)
                return G_ERROR;
        }
        else
            pixselect = pix;

        if (pixselectgrayinfo && (pixgrayinfo != pixselectgrayinfo)) {
            pixselectgray = INTERNAL_make_pixmap(MT[menu_id].win_id, pixselectgrayinfo, FALSE);
            if (pixselectgray == XmUNSPECIFIED_PIXMAP)
                return G_ERROR;
        }
        else
            pixselectgray = pixgray;
    }
    MT[id].win_id = MT[menu_id].win_id;
    MT[id].menu_type = MT[menu_id].menu_type;
    MT[id].isparent= FALSE;
    if ((MT[menu_id].menu_type == IS_BUTTONBAR || 
             MT[menu_id].menu_type == IS_BUTTONBAR2) && 
             MT[menu_id].isparent)
        MT[id].isfirstchild= TRUE;
    else
        MT[id].isfirstchild= FALSE;
    MT[id].ispopuphandler= FALSE;
    MT[id].return_id = return_id + 1;
    if (ismulti) {
        MT[id].pix[0] = pix;
        MT[id].pix[1] = pix2;
        MT[id].pix[2] = pix3;
        MT[id].pix[3] = pix4;
        MT[id].pixgray[0] = pixgray;
        MT[id].pixgray[1] = pix2gray;
        MT[id].pixgray[2] = pix3gray;
        MT[id].pixgray[3] = pix4gray;
    }
    else {
        MT[id].pix[0] = pix;
        MT[id].pix[1] = pixgray;
        MT[id].pix[2] = pixselect;
        MT[id].pix[3] = pixselectgray;
        MT[id].pixgray[0] = pixgray;
        MT[id].pixgray[1] = pixgray;
        MT[id].pixgray[2] = pixselectgray;
        MT[id].pixgray[3] = pixselectgray;
    }
    n = 0;
    XtSetArg(args[n], XmNhighlightOnEnter, TRUE);
    n++;
    XtSetArg(args[n], XmNpushButtonEnabled, TRUE);
    n++;
    XtSetArg(args[n], XmNlabelType, XmPIXMAP);
    n++;
    XtSetArg(args[n], XmNlabelPixmap, pix);
    n++;
    XtSetArg(args[n], XmNlabelInsensitivePixmap, pixgray);
    n++;
    sprintf(label, "PixButton%d", id);
    if (toggle) {
        state = toggle_on;
        XtSetArg(args[n], XmNset, state);
        n++;
        XtSetArg(args[n], XmNshadowThickness, 2); 
        n++;
        XtSetArg(args[n], XmNindicatorOn, FALSE);
        n++;
        XtSetArg(args[n], XmNselectPixmap, pixselect);
        n++;
        XtSetArg(args[n], XmNselectInsensitivePixmap, pixselectgray);
        n++;
        XtSetArg(args[n], XmNhighlightThickness, 1);
        n++;
        MT[id].hmenu = XmCreateToggleButton(MT[menu_id].hmenu,
                                                  label,
                                                  args,
                                                  n);
        XtAddCallback(MT[id].hmenu,
                      XmNvalueChangedCallback,
                      ButtonCB,
                      (XtPointer)id);

        if (MT[menu_id].isgroup) {
            MT[id].isgroup = MT[menu_id].isgroup;
            XtAddCallback(MT[id].hmenu,
                      XmNvalueChangedCallback,
                      RadioButtonCB,
                      (XtPointer)id);
        }
            
    }
    else {
        XtSetArg(args[n], XmNshadowThickness, 2); 
        n++;
        MT[id].hmenu = XmCreatePushButton(MT[menu_id].hmenu,
                                                label,
                                                args,
                                                n);
        XtAddCallback(MT[id].hmenu,
                      XmNactivateCallback,
                      ButtonCB,
                      (XtPointer)id);
    }
    XtManageChild(MT[id].hmenu);
    XtAddCallback(MT[id].hmenu,
                  XmNdestroyCallback,
                  menu_item_destroy_func,
                  (XtPointer) id);
                  
    XtAddEventHandler(MT[id].hmenu,
                      EnterWindowMask,
                      FALSE,
                      menu_item_enter_func,
                      (XtPointer) id);
                      
    XtAddEventHandler(MT[id].hmenu,
                      LeaveWindowMask,
                      FALSE,
                      menu_item_leave_func,
                      (XtPointer)id);

    XtAddEventHandler(MT[id].hmenu,
                      ButtonPressMask,
                      FALSE,
                      menu_item_leave_func,
                      (XtPointer)id);
                 
    return G_OK;
}

int g_SCREEN_menu_append_pixbutton(int menu_id, char *pixinfo[], 
                                      char *pixgrayinfo[], int return_id)
{
    return append_pixmap(menu_id, pixinfo, pixgrayinfo, NULL, NULL, 
                                                    return_id, FALSE, FALSE, FALSE);
}

int g_SCREEN_menu_append_multipixbutton(int menu_id, char *pixinfo1[], 
                   char *pixinfo2[], char *pixinfo3[], char *pixinfo4[],int return_id)
{
    return append_pixmap(menu_id, pixinfo1, pixinfo2, pixinfo3, pixinfo4, 
                                                    return_id, TRUE, FALSE, FALSE);
}


int g_SCREEN_menu_append_pixtoggle(int menu_id, char *pixinfo[], char *pixgrayinfo[], 
                            char *pixselectinfo[], char *pixselectgrayinfo[], 
                            int return_id, int toggle_on)
{
    return append_pixmap(menu_id, pixinfo, pixgrayinfo, pixselectinfo, 
                             pixselectgrayinfo, return_id, FALSE, TRUE, toggle_on);
}


int g_SCREEN_menu_append_separator(int menu_id)
{
    Widget separator;
    static int count;
    char string[20];
    int n,id;
    Arg  args[20];

    if (menu_id < 0 || menu_id >= menutype_count || MT[menu_id].hmenu == NULL)
        return G_ERROR;
    sprintf(string, "separator%d", count++);

    n=0;
    if ((MT[menu_id].menu_type == IS_BUTTONBAR ||
            MT[menu_id].menu_type == IS_BUTTONBAR2) && 
            MT[menu_id].isparent) {
        if ((id = menu_next_item()) == G_ERROR)
            return G_ERROR;

        MT[id].win_id = MT[menu_id].win_id;
        MT[id].menu_type = MT[menu_id].menu_type;
        MT[id].isparent= FALSE;
        MT[id].isfirstchild= TRUE;
        MT[id].ispopuphandler= FALSE;
        MT[id].return_id = 0;
/*        XtSetArg(args[n], XmNwidth, 20);*/
        XtSetArg(args[n], XmNwidth, 10);
        n++;
        XtSetArg(args[n], XmNorientation, XmVERTICAL);
        n++;
        XtSetArg(args[n], XmNseparatorType, XmNO_LINE);
        n++;
    }
    separator = XmCreateSeparatorGadget(MT[menu_id].hmenu,
                                        string,
                                        args,
                                        n);
    XtManageChild(separator);
    if (n) {
        MT[id].hmenu = separator;
        XtAddCallback(MT[id].hmenu,
                  XmNdestroyCallback,
                  menu_item_destroy_func,
                  (XtPointer) id);
    }
    return G_OK;
}

void g_SCREEN_menu_enable_item(int menu_id, int item_id, int enable)
{
    int i;

    if (menu_id < 0 || menu_id >= menutype_count || MT[menu_id].hmenu == NULL)
        return;
    if (item_id == G_MENU_ALLITEMS) {
        XtSetSensitive(MT[menu_id].hmenu, enable);
        if (MT[menu_id].cmenu)
            XtSetSensitive(MT[menu_id].cmenu, enable);
        return;
    }
    for (i=0; i<menutype_count;i++)
        if (item_id               == MT[i].return_id - 1 &&
            MT[menu_id].win_id    == MT[i].win_id &&
            MT[menu_id].menu_type == MT[i].menu_type) {
                XtSetSensitive(MT[i].hmenu, enable);
        }
}

int g_SCREEN_menu_is_enabled(int menu_id, int item_id)
{
    int i;

    for (i=0; i<menutype_count;i++)
        if (MT[i].return_id - 1 == item_id &&
            MT[menu_id].win_id == MT[i].win_id) {
                return XtIsSensitive(MT[i].hmenu);
        }
    return FALSE;
}

void g_SCREEN_menu_set_toggle(int menu_id, int item_id, int check)
{
    int i;
    Arg args[1];
    Boolean state = check;

    for (i=0; i<menutype_count;i++)
        if (MT[i].return_id - 1 == item_id &&
            MT[menu_id].win_id == MT[i].win_id) {
                XtSetArg(args[0], XmNset, state);
                XtSetValues(MT[i].hmenu, args, 1);
                if (MT[i].isgroup)
                    uncheck_radio_group(i);
        }
}

int g_SCREEN_menu_get_toggle(int menu_id, int item_id)
{
    int i;
    Boolean state;
    Arg args[1];

    for (i=0; i<menutype_count;i++)
        if (MT[i].return_id - 1 == item_id &&
            MT[menu_id].win_id == MT[i].win_id) {
                XtSetArg(args[0], XmNset, &state);
                XtGetValues(MT[i].hmenu, args, 1);
                break;
        }
    return state;
}

int g_SCREEN_menu_set_label(int menu_id, int item_id, char *label)
{
    Arg args[2];
    unsigned char type;
    int i;

    if (menu_id < 0 || menu_id >= menutype_count || MT[menu_id].hmenu == NULL)
        return G_ERROR;
    for (i=0; i<menutype_count;i++)
        if (MT[i].return_id - 1 == item_id &&
            MT[menu_id].win_id == MT[i].win_id) {
                XtSetArg(args[0], XmNlabelType, &type); 
                XtGetValues(MT[i].hmenu, args, 1); 
                if (type == XmSTRING) {
                    XtSetArg(args[0], XmNlabelString,  
		        XmStringCreateLtoR(label ,XmSTRING_DEFAULT_CHARSET));
                    XtSetValues(MT[i].hmenu, args, 1);
                    return G_OK;
                }
    }
    return G_ERROR;
}

char *g_SCREEN_menu_get_label(int menu_id, int item_id)
{
    Arg args[2];
    unsigned char type;
    XmString label;
    char *text;
    int i;

    if (menu_id < 0 || menu_id >= menutype_count || MT[menu_id].hmenu == NULL)
        return NULL;
    for (i=0; i<menutype_count;i++)
        if (MT[i].return_id - 1 == item_id &&
            MT[menu_id].win_id == MT[i].win_id) {
                XtSetArg(args[0], XmNlabelType, &type); 
                XtGetValues(MT[i].hmenu, args, 1);
                if (type == XmSTRING) {
                    XtSetArg(args[0], XmNlabelString, &label); 
                    XtGetValues(MT[i].hmenu, args, 1);
                    XmStringGetLtoR(label, XmFONTLIST_DEFAULT_TAG, &text);
                    INTERNAL_add_label(text);
#ifndef MEMORYLEAK
                    XmStringFree(label);
                    XtFree(text);
#endif
                    return INTERNAL_get_label();
                }
    }
    return NULL;
}

int g_SCREEN_menu_set_pixmap(int menu_id, int item_id, int pix_id)
{
    Arg args[2];
    unsigned char type;
    int i;

    if (menu_id < 0 || menu_id >= menutype_count || MT[menu_id].hmenu == NULL)
        return G_ERROR;
    if (pix_id < 0 || pix_id > MAX_IBUTTON-1)
        return G_ERROR;
    for (i=0; i<menutype_count;i++)
        if (MT[i].return_id - 1 == item_id &&
            MT[menu_id].win_id == MT[i].win_id) {
                XtSetArg(args[0], XmNlabelType, &type); 
                XtGetValues(MT[i].hmenu, args, 1); 
                if (type == XmPIXMAP) {
                    if (MT[i].pix[pix_id] == 0)
                        break;
                    XtSetArg(args[0], XmNlabelPixmap,  
		        MT[i].pix[pix_id]);
                    XtSetArg(args[1], XmNlabelInsensitivePixmap,  
		        MT[i].pixgray[pix_id]);
                    XtSetValues(MT[i].hmenu, args, 2);
                    return G_OK;
                }
    }
    return G_ERROR;
}


int g_SCREEN_menu_set_group(int menu_id, int group_on)
{   
    static int new_group_id, old_group_id;
    
    if (menu_id < 0 || menu_id >= menutype_count)
        return G_ERROR;
    if (group_on) {
        if (old_group_id == 0) {
            new_group_id++;
            old_group_id = new_group_id;
        }
        MT[menu_id].isgroup = old_group_id;
    }
    else
        old_group_id = 0;
    return G_OK;
}

/************************************************/
typedef struct tagTOOLTIPTYPE {
    int id;
    char *tip;
} TOOLTIPTYPE;


static int numtips, maxtips;
static TOOLTIPTYPE *tips;

static int compar(const void *v1, const void *v2)
{
    TOOLTIPTYPE *t1, *t2;
    
    t1 = (TOOLTIPTYPE*) v1;
    t2 = (TOOLTIPTYPE*) v2;
    return (t1->id - t2->id);
}
 

int g_SCREEN_menu_add_tooltip(int return_id, char *tip)
{
    int len;

    if (numtips >= maxtips) {
        maxtips += 20;
        tips = (TOOLTIPTYPE*) realloc(tips, sizeof(TOOLTIPTYPE) * maxtips);
    }
    len = strlen(tip)+1;
    len = min(80,len);
    tips[numtips].tip = (char*)malloc(len+1);
    /* assert(tips[numtips].tip); */
    strncpy(tips[numtips].tip, tip, len);
    tips[numtips].tip[len] = '\0';
    tips[numtips].id = return_id+1;
    numtips++;
    qsort(tips, numtips, sizeof(TOOLTIPTYPE), compar);
    return G_OK;
}

char *INTERNAL_get_tooltip(int return_id)
{
    TOOLTIPTYPE ttt, *tt;

    ttt.id = return_id;
    if (numtips == 0)
        return NULL; 
    tt = (TOOLTIPTYPE*) bsearch(&ttt, tips, numtips, sizeof(TOOLTIPTYPE), compar);
    if (tt == NULL)
        return NULL;
    return tt->tip;
}


static void kill_tooltip_popup(XtPointer tag, XtIntervalId *reason)
{
    int menu_id = (int) tag;

    if (MT[menu_id].tooltip) {
        XtDestroyWidget(MT[menu_id].tooltip);
        MT[menu_id].tooltip = NULL;
    }
}


static void popup_tooltip(int menu_id)
{
    Arg args[10];
    XGCValues values;
    XtGCMask  valuemask;
    GC gc ;
    Colormap cmap;
    static XColor toolcolor;
    Widget draw;
    int n, direction, font_ascent, font_decent, width=100, height=30;
    int xborder=5, yborder=5, yoffset=4;
    XCharStruct overall;
    Widget parent;
    Window wchild;
    XFontStruct *font;
    Dimension  parent_width, child_height;
    int x=0, y=0, child_y, child_x;
    char *label;

    label = INTERNAL_get_tooltip(MT[menu_id].return_id);
    if (label == NULL)
        return;
    parent = xwini[MT[menu_id].win_id].form_widget;

    n=0;
    XtSetArg(args[n], XmNwidth, &parent_width);  n++;
    XtGetValues(parent, args, n);

    n=0;
    XtSetArg(args[n], XmNheight, &child_height);  n++;
    XtGetValues(MT[menu_id].hmenu, args, n);

    XTranslateCoordinates(XtDisplay (parent),
        XtWindow(MT[menu_id].hmenu), XtWindow(parent), 
        0, 0, &child_x, &child_y, &wchild);
        
    x = child_x;
    y = child_height + child_y + yoffset;

    n = 0 ;
    XtSetArg(args[n], XmNwidth, width);  n++ ;
    XtSetArg(args[n], XmNheight, height); n++ ;
    XtSetArg(args[n], XmNx, x); n++ ;
    XtSetArg(args[n], XmNy, y); n++ ;

    draw = XmCreateDrawingArea (parent, "drawtooltip", args, n);

    gc = XCreateGC (XtDisplay (draw), XtWindow(parent),  0, &values);
        
    if ((font = XLoadQueryFont(
           XtDisplay (draw),
           "-adobe-helvetica-medium-r-normal-*-12-120-100-100-p-0-iso8859-1"))== NULL){

        if ((font = XLoadQueryFont(XtDisplay (draw),"fixed")) == NULL){
            fprintf(stderr, "Display %s doesn't know font %s\nAborting ...\n",
                    XDisplayName(NULL), "fixed");
            exit(1);
        }
    }
    XSetFont(XtDisplay (draw), gc, font->fid);

    XTextExtents(font, label, strlen(label),
                  &direction, &font_ascent, &font_decent,
                  &overall);
    width    = overall.width + 2 * xborder; 
    height   = font_ascent + font_decent + 2 * yborder;
   
    n = 0 ;
    if (parent_width < width + x)
        x -= width + x - parent_width;
    XtSetArg(args[n], XmNx, x);  n++ ;
    XtSetArg(args[n], XmNwidth, width);  n++ ;
    XtSetArg(args[n], XmNheight, height); n++ ;
    XtSetArg(args[n], XmNsensitive, FALSE); n++;
    XtSetArg(args[n], XmNtraversalOn, FALSE); n++;
    XtSetArg(args[n], XmNnavigationType, XmNONE); n++;
    XtSetValues(draw, args, n);
    XtManageChild(draw);

    if (toolcolor.pixel == 0) {
        cmap    = DefaultColormap(INTERNAL_display,INTERNAL_screen);

        XParseColor(INTERNAL_display,
                    cmap,
                    "lightYellow",
                    &toolcolor);
        if (!XAllocColor(INTERNAL_display,cmap,&toolcolor))
            toolcolor.pixel = WhitePixel(INTERNAL_display, INTERNAL_screen);
    }
    XSetForeground (XtDisplay (draw), gc, toolcolor.pixel);
    XFillRectangle (XtDisplay (draw), XtWindow(draw),  gc, 0,0, width-1,height-1);
    XSetForeground (XtDisplay (draw), gc, 0x000000);
    XDrawRectangle (XtDisplay (draw), XtWindow(draw),  gc, 0,0, width-1,height-1);

    XDrawString(XtDisplay (draw),
                    XtWindow(draw),
                    gc,
                    (int) xborder,
                    (int) font_ascent+yborder,
                    label,
                    strlen(label));
    MT[menu_id].tooltip = draw;
    
    XtAppAddTimeOut(XtWidgetToApplicationContext(draw), 
            3000, kill_tooltip_popup, (XtPointer) menu_id);

    XFreeFont(XtDisplay (draw), font);
    XFreeGC(XtDisplay (draw), gc);

}

static void popup_hide_tooltip(int menu_id)
{
    if (MT[menu_id].tooltip)
        XtAppAddTimeOut(XtWidgetToApplicationContext(MT[menu_id].hmenu), 
            5, kill_tooltip_popup, (XtPointer) menu_id);
}



