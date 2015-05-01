/************************************************************************/
/*                               genmpop.c                             */
/*                                                                      */
/*  Platform : Motif                                                    */
/*  Module   : Genplot User Interface functions                         */
/*                                                                      */
/*  Albert van Kuik                                                     */
/************************************************************************/

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
#include <Xm/Scale.h>
#include <Xm/TextF.h>


#if (XmVERSION >= 2) 
#include <Xm/SpinB.h>
#include <Xm/Notebook.h>
#endif
#include <Xm/ArrowB.h>

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
#include <time.h>
#include <stdarg.h>
#include <assert.h>

#ifdef DEBUG
#include "mshell.h"
#endif

#include "genplot.h"
#include "g_inter.h"
#include "genmotif.h"


char *INTERNAL_get_label(void);
void INTERNAL_add_label(char *label);

static void StructureNotifyProc2(Widget w, XtPointer tag, XEvent *xevent, Boolean *bool)
{
    XPropertyEvent *event = (XPropertyEvent*) xevent;

    if (event->type == DestroyNotify) {

        g_close_window(MAKE_GLOBAL_ID((int) tag, G_SCREEN));

    }
}
/*********************************/
/* POPUP STUFF
*/

static int popup_to_cursor;
static int PositionAtCursor(Arg *args)
{
    int    n, rootx, rooty, wx, wy;
    Window root, child;
    unsigned int mask;

    n=0;
    if (popup_to_cursor && XQueryPointer(INTERNAL_display,
                      RootWindow(INTERNAL_display,INTERNAL_screen), 
                      &root,
                      &child,
                      &rootx, &rooty,
                      &wx, &wy,
                      &mask)) {
        XtSetArg(args[n], XmNx, rootx - 50); n++;
        XtSetArg(args[n], XmNy, rooty - 50); n++;
        XtSetArg(args[n], XmNdefaultPosition, FALSE); n++;
    }                              
    else {
        XtSetArg(args[n], XmNdefaultPosition, TRUE); n++;
    }
    return n;
}

void g_popup_follow_cursor(int doit)
{
    popup_to_cursor = doit;
}



/* --- MESSAGE BOX --- */


static void destroy_message_box(int local_win_id)
{
    if (xwini[local_win_id].msgbox.popup) 
        XtDestroyWidget(xwini[local_win_id].msgbox.popup);  
}

static void init_message_box(int local_win_id)
{
    xwini[local_win_id].msgbox.popup = NULL;
}

static void mbox_ok_func(Widget w, XtPointer tag, XtPointer reason)
{
    int local_win_id = (int) tag;
    xwini[local_win_id].msgbox.status = G_POPUP_BUTTON1;
}

static void mbox_destroy_func(Widget w, XtPointer tag, XtPointer reason)
{
    int local_win_id = (int) tag;
    xwini[local_win_id].msgbox.popup = NULL;
}

static void MessageBox(int local_win_id, char *title, char *message, int wait)
{
    Arg args[10];
    int n;
    Widget button;

    n=0;
    n += PositionAtCursor(args);
    if (wait) {
        XtSetArg(args[n],XmNdialogStyle,XmDIALOG_FULL_APPLICATION_MODAL);
        n++;
    }
    xwini[local_win_id].msgbox.popup = XmCreateMessageDialog(xwini[local_win_id].form_widget,"msgbox",args,n);
    XtAddEventHandler(xwini[local_win_id].msgbox.popup,
                      StructureNotifyMask,
                      FALSE,
                      StructureNotifyProc2,
                      (XtPointer)local_win_id);
    button = XmMessageBoxGetChild(xwini[local_win_id].msgbox.popup,XmDIALOG_CANCEL_BUTTON);
    XtUnmanageChild(button);
    button = XmMessageBoxGetChild(xwini[local_win_id].msgbox.popup,XmDIALOG_HELP_BUTTON);
    XtUnmanageChild(button);
    XtAddCallback(xwini[local_win_id].msgbox.popup,
                  XmNokCallback,
                  mbox_ok_func,
                  (XtPointer) local_win_id);

    XtAddCallback(xwini[local_win_id].msgbox.popup,
                  XmNdestroyCallback,
                  mbox_destroy_func,
                  (XtPointer) local_win_id);

    n = 0;
    XtSetArg(args[n],XmNdialogTitle,
        XmStringCreateLtoR(title,XmSTRING_DEFAULT_CHARSET));
    n++;
    XtSetArg(args[n],XmNmessageString,
        XmStringCreateLtoR(message,XmSTRING_DEFAULT_CHARSET));
    n++;
    XtSetValues (xwini[local_win_id].msgbox.popup, args, n);
    XtManageChild (xwini[local_win_id].msgbox.popup);
}

void g_SCREEN_popup_messagebox(int win_id, char *title, char *message, int wait)
{
    static XEvent event;
    int local_win_id = GET_LOCAL_ID(win_id);

    destroy_message_box(local_win_id);
    xwini[local_win_id].msgbox.status = POPUP_WAIT;
    MessageBox(local_win_id,title,message,wait);
    INTERNAL_dispatch_message();
    if (!wait)
        return;
    while (!INTERNAL_is_event() && xwini[local_win_id].msgbox.status == POPUP_WAIT) {
        XtAppNextEvent(INTERNAL_app_context,&event);
        XtDispatchEvent(&event);
    }
}

/* --- YES-NO BOX --- */

static void yes_func(Widget w, XtPointer tag, XtPointer reason)
{
    int local_win_id = (int) tag;
    xwini[local_win_id].ynbox.status = G_POPUP_BUTTON1;
}

static void no_func(Widget w, XtPointer tag, XtPointer reason)
{
    int local_win_id = (int) tag;
    xwini[local_win_id].ynbox.status =  G_POPUP_BUTTON2;
}

static void yesno_destroy_func(Widget w, XtPointer tag, XtPointer reason)
{
    int local_win_id = (int) tag;
    xwini[local_win_id].ynbox.popup = NULL;
}

static void YesNoBox(int local_win_id, char *title, char *question, 
                                     char *button1text, char *button2text)
{
    Arg args[10];
    int n;
    static Widget qstnbox = NULL;
    Widget button;

    if (xwini[local_win_id].ynbox.popup == NULL) {
        n=0;
        XtSetArg(args[n],XmNdialogStyle,XmDIALOG_FULL_APPLICATION_MODAL);
        n++;
        xwini[local_win_id].ynbox.popup = XmCreateMessageDialog(xwini[local_win_id].form_widget,
                                                          "qstnbox",args,n);
        XtAddEventHandler(xwini[local_win_id].ynbox.popup,
                      StructureNotifyMask,
                      FALSE,
                      StructureNotifyProc2,
                      (XtPointer)local_win_id);
        button  = XmMessageBoxGetChild(xwini[local_win_id].ynbox.popup,XmDIALOG_HELP_BUTTON);
        XtUnmanageChild(button);
        XtAddCallback(xwini[local_win_id].ynbox.popup,XmNokCallback,
                      yes_func, (XtPointer) local_win_id);
        XtAddCallback(xwini[local_win_id].ynbox.popup,XmNcancelCallback,
                      no_func, (XtPointer) local_win_id);
        XtAddCallback(xwini[local_win_id].ynbox.popup,XmNdestroyCallback,
                      yesno_destroy_func, (XtPointer) local_win_id);
    }
    n = 0;
    n += PositionAtCursor(args);
    XtSetArg(args[n],XmNokLabelString,
        XmStringCreateLtoR(button1text,XmSTRING_DEFAULT_CHARSET));
    n++;
    XtSetArg(args[n],XmNcancelLabelString,
        XmStringCreateLtoR(button2text,XmSTRING_DEFAULT_CHARSET));
    n++;
    XtSetArg(args[n],XmNmessageString,
        XmStringCreateLtoR(question,XmSTRING_DEFAULT_CHARSET));
    n++;
    XtSetArg(args[n],XmNdialogTitle,
        XmStringCreateLtoR(title,XmSTRING_DEFAULT_CHARSET));
    n++;
    XtSetValues (xwini[local_win_id].ynbox.popup, args, n);
    XtManageChild (xwini[local_win_id].ynbox.popup);
}

static void init_message2_box(int local_win_id)
{
    xwini[local_win_id].ynbox.popup = NULL;
}

int g_SCREEN_popup_messagebox2(int win_id, char *title, char *message, 
                                char *button1text, char *button2text)
{
    static XEvent event;
    int local_win_id = GET_LOCAL_ID(win_id);

    xwini[local_win_id].ynbox.status = POPUP_WAIT;
    YesNoBox(local_win_id, title,message,button1text,button2text);
    INTERNAL_dispatch_message();

    while (!INTERNAL_is_event() && xwini[local_win_id].ynbox.status == POPUP_WAIT) {
        XtAppNextEvent(INTERNAL_app_context,&event);
        XtDispatchEvent(&event);
    }
    return xwini[local_win_id].ynbox.status;
}

/* --- YES-NO-CANCEL BOX --- */

static void yes3_func(Widget w, XtPointer tag, XtPointer reason)
{
    int local_win_id = (int) tag;
    xwini[local_win_id].yncbox.status = G_POPUP_BUTTON1;
}

static void no3_func(Widget w, XtPointer tag, XtPointer reason)
{
    int local_win_id = (int) tag;
    xwini[local_win_id].yncbox.status = G_POPUP_BUTTON2;
}

static void destroy3_func(Widget w, XtPointer tag, XtPointer reason)
{
    int local_win_id = (int) tag;
    xwini[local_win_id].yncbox.popup = NULL;
}

static void cancel3_func(Widget w, XtPointer tag, XtPointer reason)
{
    int local_win_id = (int) tag;
    xwini[local_win_id].yncbox.status = G_POPUP_BUTTON3;
    XtUnmanageChild(xwini[local_win_id].yncbox.popup);
}

static void YesNoCancelBox(int local_win_id, char *title, char *question, 
              char *button1text, char *button2text, char *button3text)
{
    Arg args[10];
    int n;

    if (xwini[local_win_id].yncbox.popup == NULL) {
        n=0;
        XtSetArg(args[n],XmNdialogStyle,XmDIALOG_FULL_APPLICATION_MODAL);
        n++;

#ifdef xxx
        xwini[local_win_id].yncbox.popup = XmCreateQuestionDialog(xwini[local_win_id].form_widget,"qstnbox3",args,n);
#endif
        xwini[local_win_id].yncbox.popup = XmCreateMessageDialog(xwini[local_win_id].form_widget,"qstnbox3",args,n);
        XtAddEventHandler(xwini[local_win_id].yncbox.popup,
                      StructureNotifyMask,
                      FALSE,
                      StructureNotifyProc2,
                      (XtPointer)local_win_id);
        XtAddCallback(xwini[local_win_id].yncbox.popup,XmNokCallback,
                      yes3_func, (XtPointer) local_win_id);
        XtAddCallback(xwini[local_win_id].yncbox.popup,XmNcancelCallback,
                      no3_func, (XtPointer) local_win_id);
        XtAddCallback(xwini[local_win_id].yncbox.popup,XmNhelpCallback,
                      cancel3_func, (XtPointer) local_win_id);
        XtAddCallback(xwini[local_win_id].yncbox.popup,XmNdestroyCallback,
                      destroy3_func, (XtPointer) local_win_id);
    }
    n = 0;
    n += PositionAtCursor(args);
    XtSetArg(args[n],XmNokLabelString,
        XmStringCreateLtoR(button1text,XmSTRING_DEFAULT_CHARSET));
    n++;
    XtSetArg(args[n],XmNcancelLabelString,
        XmStringCreateLtoR(button2text,XmSTRING_DEFAULT_CHARSET));
    n++;
    XtSetArg(args[n],XmNhelpLabelString,
        XmStringCreateLtoR(button3text,XmSTRING_DEFAULT_CHARSET));
    n++;
    XtSetArg(args[n],XmNmessageString,
        XmStringCreateLtoR(question,XmSTRING_DEFAULT_CHARSET));
    n++;
    XtSetArg(args[n],XmNdialogTitle,
        XmStringCreateLtoR(title,XmSTRING_DEFAULT_CHARSET));
    n++;
    XtSetValues (xwini[local_win_id].yncbox.popup, args, n);
    XtManageChild (xwini[local_win_id].yncbox.popup);
}


static void init_message3_box(int local_win_id)
{
    xwini[local_win_id].yncbox.popup = NULL;
}

int g_SCREEN_popup_messagebox3(int win_id, char *title, char *message, 
                  char *button1text, char *button2text, char *button3text)
{
    static XEvent event;
    int local_win_id = GET_LOCAL_ID(win_id);

    xwini[local_win_id].yncbox.status = POPUP_WAIT;    
    YesNoCancelBox(local_win_id, title,message,button1text,
                   button2text,button3text);
    INTERNAL_dispatch_message();

    while (!INTERNAL_is_event() && xwini[local_win_id].yncbox.status == POPUP_WAIT) {
        XtAppNextEvent(INTERNAL_app_context,&event);
        XtDispatchEvent(&event);
    }
    return xwini[local_win_id].yncbox.status;
}


/* --- Prompt Dialog Box --- */



static void prompt_okfunc(Widget w, XtPointer tag, XtPointer reason)
{
    int local_win_id = (int) tag;
    Widget txt;
    static char *searchfor;

    txt = XmCommandGetChild(xwini[local_win_id].prmbox.popup,XmDIALOG_COMMAND_TEXT);
    searchfor = XmTextGetString(txt);
    strncpy(xwini[local_win_id].prmbox.string, searchfor,PROMPTTEXTLENGTH);
    XtFree(searchfor);
    xwini[local_win_id].prmbox.status = PROMPT_STATUS_OK;
}

static void prompt_cancelfunc(Widget w, XtPointer tag, XtPointer reason)
{
    int local_win_id = (int) tag;
    xwini[local_win_id].prmbox.status = PROMPT_STATUS_CANCEL;
}

static void prompt_destroyfunc(Widget w, XtPointer tag, XtPointer reason)
{
    int local_win_id = (int) tag;
    xwini[local_win_id].prmbox.popup = NULL;
}

static void TestPromptKeyCode(Widget w, XtPointer popup, XKeyEvent *event,
                           Boolean *doit)
{
    unsigned int key;
    int index = 0;

    if ((event->state & ShiftMask) ||
        (event->state & LockMask))
        index = 1;
    key =  XKeycodeToKeysym(INTERNAL_display, event->keycode, index);

    if (key == XK_Right || key == XK_KP_Right)
        XmTextFieldClearSelection(w, CurrentTime);
}

static void PromptDialog(int local_win_id, char *title, char *message, char *text)
{
    Arg args[10];
    int n, first = FALSE;
    Widget txt, button;

    if (xwini[local_win_id].prmbox.popup == NULL) {
        first = TRUE;
        n=0;
        XtSetArg(args[n],XmNdialogStyle,XmDIALOG_FULL_APPLICATION_MODAL);
        n++;
        xwini[local_win_id].prmbox.popup = 
            XmCreatePromptDialog(xwini[local_win_id].form_widget,"prmbox",args,n);
        XtAddEventHandler(xwini[local_win_id].prmbox.popup,
                      StructureNotifyMask,
                      FALSE,
                      StructureNotifyProc2,
                      (XtPointer)local_win_id);
        button = XmSelectionBoxGetChild(xwini[local_win_id].prmbox.popup,XmDIALOG_HELP_BUTTON);
        XtUnmanageChild(button);
        XtAddCallback(xwini[local_win_id].prmbox.popup,
                      XmNokCallback,
                       prompt_okfunc,
                      (XtPointer) local_win_id);
        XtAddCallback(xwini[local_win_id].prmbox.popup,
                      XmNcancelCallback,
                       prompt_cancelfunc,
                      (XtPointer) local_win_id);
        XtAddCallback(xwini[local_win_id].prmbox.popup,
                      XmNdestroyCallback,
                       prompt_destroyfunc,
                      (XtPointer) local_win_id);
        txt = XmSelectionBoxGetChild(xwini[local_win_id].prmbox.popup,XmDIALOG_TEXT);
        XtAddEventHandler(txt,
            KeyPressMask,
            FALSE,
            (XtEventHandler)TestPromptKeyCode,
            NULL);
    }
    n = 0;
    n += PositionAtCursor(args);
    XtSetArg(args[n],XmNdialogTitle,
        XmStringCreateLtoR(title,XmSTRING_DEFAULT_CHARSET));
    n++;
    XtSetArg(args[n],XmNselectionLabelString,
        XmStringCreateLtoR(message,XmSTRING_DEFAULT_CHARSET));
    n++;
    XtSetArg(args[n],XmNtextString,
        XmStringCreateLtoR(text,XmSTRING_DEFAULT_CHARSET));
    n++;
    XtSetValues (xwini[local_win_id].prmbox.popup, args, n);
    XtManageChild (xwini[local_win_id].prmbox.popup);
    if (text) {
        XmTextPosition first = 0;
        XmTextPosition last  = (XmTextPosition) strlen(text);
        txt = XmSelectionBoxGetChild(xwini[local_win_id].prmbox.popup,XmDIALOG_TEXT);
        XmTextSetSelection(txt, first, last, CurrentTime);
    }
    if (first)
        XmProcessTraversal(xwini[local_win_id].prmbox.popup,XmTRAVERSE_NEXT_TAB_GROUP);
}

static void init_prompt_box(int local_win_id)
{
    xwini[local_win_id].prmbox.popup = NULL;
}

char *g_SCREEN_popup_promptdialog(int win_id, char *title, char *question, char *text)
{
    static XEvent event;
    int local_win_id = GET_LOCAL_ID(win_id);

    xwini[local_win_id].prmbox.status = PROMPT_STATUS_WAIT;
    PromptDialog(local_win_id, title,question,text);
    INTERNAL_dispatch_message();
    while (!INTERNAL_is_event() && xwini[local_win_id].prmbox.status == PROMPT_STATUS_WAIT) {
        XtAppNextEvent(INTERNAL_app_context,&event);
        XtDispatchEvent(&event);
    }
    if (xwini[local_win_id].prmbox.status == PROMPT_STATUS_OK) 
        return (char*) &(xwini[local_win_id].prmbox.string);
    return NULL;
}


/* --- GET FILENAME BOX --- */

static void fileselect_okfunc(Widget w, XtPointer tag, XtPointer reason)
{
    int local_win_id = (int) tag;
    char *filename;
    XmFileSelectionBoxCallbackStruct *fcb =
        (XmFileSelectionBoxCallbackStruct *) reason;

    XmStringGetLtoR(fcb->value,
        XmSTRING_DEFAULT_CHARSET,
        &filename);
    strcpy(xwini[local_win_id].filebox.string, filename);
    xwini[local_win_id].filebox.status = SELECT_STATUS_OK;
}

static void fileselect_cancelfunc(Widget w, XtPointer tag, XtPointer reason)
{
    int local_win_id = (int) tag;
    xwini[local_win_id].filebox.status = SELECT_STATUS_CANCEL;
}

static void fileselect_destroyfunc(Widget w, XtPointer tag, XtPointer reason)
{
    int local_win_id = (int) tag;
    xwini[local_win_id].filebox.popup = NULL;
}


static void OpenFileDialogBox(int local_win_id, char *title, char *mask)
{
    Arg args[10];
    int n;
    Widget button;

    if (xwini[local_win_id].filebox.popup == NULL) {
        n=0;
        XtSetArg(args[n],XmNdialogStyle,XmDIALOG_FULL_APPLICATION_MODAL);
        n++;
        XtSetArg(args[n],XmNautoUnmanage,TRUE);
        n++;
        xwini[local_win_id].filebox.popup = 
            XmCreateFileSelectionDialog(xwini[local_win_id].form_widget,"filebox",args,n);
        XtAddEventHandler(xwini[local_win_id].filebox.popup,
                      StructureNotifyMask,
                      FALSE,
                      StructureNotifyProc2,
                      (XtPointer)local_win_id);
        button = XmSelectionBoxGetChild(xwini[local_win_id].filebox.popup,XmDIALOG_HELP_BUTTON);
        XtUnmanageChild(button);
        XtAddCallback(xwini[local_win_id].filebox.popup,XmNokCallback,
                      fileselect_okfunc, (XtPointer) local_win_id);
        XtAddCallback(xwini[local_win_id].filebox.popup,XmNcancelCallback,
                      fileselect_cancelfunc, (XtPointer) local_win_id);
        XtAddCallback(xwini[local_win_id].filebox.popup,XmNdestroyCallback,
                      fileselect_destroyfunc, (XtPointer) local_win_id);
    }
    n = 0;
    n += PositionAtCursor(args);
    XtSetArg(args[n],XmNdialogTitle,
        XmStringCreateLtoR(title,XmSTRING_DEFAULT_CHARSET));
    n++;
    XtSetArg(args[n],XmNdirMask,
        XmStringCreateLtoR(mask,XmSTRING_DEFAULT_CHARSET));
    n++;
    XtSetValues (xwini[local_win_id].filebox.popup, args, n);
    XtManageChild(xwini[local_win_id].filebox.popup);
}

static void init_file_box(int local_win_id)
{
    xwini[local_win_id].filebox.popup = NULL;
}

char *g_SCREEN_popup_getfilename(int win_id, char *title, char *mask)
{
    static XEvent event;
    int local_win_id = GET_LOCAL_ID(win_id);

    xwini[local_win_id].filebox.status = SELECT_STATUS_WAIT;
    OpenFileDialogBox(local_win_id, title, mask);
    INTERNAL_dispatch_message();
    while (!INTERNAL_is_event() && xwini[local_win_id].filebox.status == SELECT_STATUS_WAIT) {
        XtAppNextEvent(INTERNAL_app_context,&event);
        XtDispatchEvent(&event);
    }
    if (xwini[local_win_id].filebox.status == SELECT_STATUS_OK) 
        return (char*) &(xwini[local_win_id].filebox.string);
    return NULL;
}


/* --- GET DIRECTORY NAME BOX --- */

static void dirselect_okfunc(Widget w, XtPointer tag, XtPointer reason)
{
    int local_win_id = (int) tag;
    char *filename;
    XmFileSelectionBoxCallbackStruct *fcb =
        (XmFileSelectionBoxCallbackStruct *) reason;

    XmStringGetLtoR(fcb->value,
        XmSTRING_DEFAULT_CHARSET,
        &filename);
    strcpy(xwini[local_win_id].dirbox.string, filename);
    xwini[local_win_id].dirbox.status = SELECT_STATUS_OK;
}

static void dirselect_cancelfunc(Widget w, XtPointer tag, XtPointer reason)
{
    int local_win_id = (int) tag;
    xwini[local_win_id].dirbox.status = SELECT_STATUS_CANCEL;
}

static void dirselect_destroyfunc(Widget w, XtPointer tag, XtPointer reason)
{
    int local_win_id = (int) tag;
    xwini[local_win_id].dirbox.popup = NULL;
}


static void OpenDirDialogBox(int local_win_id, char *title)
{
    Arg args[10];
    int n;
    Widget button;

    if (xwini[local_win_id].dirbox.popup == NULL) {
        n=0;
        XtSetArg(args[n],XmNdialogStyle,XmDIALOG_FULL_APPLICATION_MODAL);
        n++;
        XtSetArg(args[n],XmNautoUnmanage,TRUE);
        n++;
        xwini[local_win_id].dirbox.popup = 
            XmCreateFileSelectionDialog(xwini[local_win_id].form_widget,"dirbox",args,n);
        XtAddEventHandler(xwini[local_win_id].dirbox.popup,
                      StructureNotifyMask,
                      FALSE,
                      StructureNotifyProc2,
                      (XtPointer)local_win_id);
        button = XmSelectionBoxGetChild(xwini[local_win_id].dirbox.popup,XmDIALOG_HELP_BUTTON);
        XtUnmanageChild(button);
        button = XtNameToWidget(xwini[local_win_id].dirbox.popup,"ItemsListSW");
        XtUnmanageChild(button);
        button = XtNameToWidget(xwini[local_win_id].dirbox.popup,"Items");
        XtUnmanageChild(button);
        button = XtNameToWidget(xwini[local_win_id].dirbox.popup,"FilterText");
        XtUnmanageChild(button);
        button = XtNameToWidget(xwini[local_win_id].dirbox.popup,"FilterLabel");
        XtUnmanageChild(button);
        XtAddCallback(xwini[local_win_id].dirbox.popup,XmNokCallback,
                      dirselect_okfunc, (XtPointer) local_win_id);
        XtAddCallback(xwini[local_win_id].dirbox.popup,XmNcancelCallback,
                      dirselect_cancelfunc, (XtPointer) local_win_id);
        XtAddCallback(xwini[local_win_id].dirbox.popup,XmNdestroyCallback,
                      dirselect_destroyfunc, (XtPointer) local_win_id);
    }
    n = 0;
    n += PositionAtCursor(args);
    XtSetArg(args[n],XmNdialogTitle,
        XmStringCreateLtoR(title,XmSTRING_DEFAULT_CHARSET));
    n++;
    XtSetArg(args[n],XmNdirMask,
        XmStringCreateLtoR("*",XmSTRING_DEFAULT_CHARSET));
    n++;
    XtSetValues (xwini[local_win_id].dirbox.popup, args, n);
    XtManageChild(xwini[local_win_id].dirbox.popup);
}

static void init_dir_box(int local_win_id)
{
    xwini[local_win_id].dirbox.popup = NULL;
}


char *g_SCREEN_popup_getdirname(int win_id, char *title)
{
    static XEvent event;
    int local_win_id = GET_LOCAL_ID(win_id);

    xwini[local_win_id].dirbox.status = SELECT_STATUS_WAIT;
    OpenDirDialogBox(local_win_id, title);
    INTERNAL_dispatch_message();
    while (!INTERNAL_is_event() && xwini[local_win_id].dirbox.status == SELECT_STATUS_WAIT) {
        XtAppNextEvent(INTERNAL_app_context,&event);
        XtDispatchEvent(&event);
    }
    if (xwini[local_win_id].dirbox.status == SELECT_STATUS_OK) 
        return (char*) &(xwini[local_win_id].dirbox.string);
    return NULL;
}

/* --- SAVE FILE AS --- */
static void saveas_okfunc(Widget w, XtPointer tag, XtPointer reason)
{
    int local_win_id = (int) tag;
    char *filename;
    XmFileSelectionBoxCallbackStruct *fcb =
        (XmFileSelectionBoxCallbackStruct *) reason;

    XmStringGetLtoR(fcb->value,
        XmSTRING_DEFAULT_CHARSET,
        &filename);
    strcpy(xwini[local_win_id].saveasbox.string, filename);
    xwini[local_win_id].saveasbox.status = SELECT_STATUS_OK;
}

static void saveas_cancelfunc(Widget w, XtPointer tag, XtPointer reason)
{
    int local_win_id = (int) tag;
    xwini[local_win_id].saveasbox.status = SELECT_STATUS_CANCEL;
}

static void saveas_destroyfunc(Widget w, XtPointer tag, XtPointer reason)
{
    int local_win_id = (int) tag;
    xwini[local_win_id].saveasbox.popup = NULL;
}

static void SaveAsDialogBox(int local_win_id, char *title, char *filename)
{
    Arg args[10];
    int n;
    Widget button;

    if (xwini[local_win_id].saveasbox.popup == NULL) {
        n=0;
        XtSetArg(args[n],XmNdialogStyle,XmDIALOG_FULL_APPLICATION_MODAL);
        n++;
        XtSetArg(args[n],XmNautoUnmanage,TRUE);
        n++;
        xwini[local_win_id].saveasbox.popup = XmCreateFileSelectionDialog(xwini[local_win_id].form_widget,
                    "saveasbox",args,n);
        XtAddEventHandler(xwini[local_win_id].saveasbox.popup,
                      StructureNotifyMask,
                      FALSE,
                      StructureNotifyProc2,
                      (XtPointer)local_win_id);
        button = XmSelectionBoxGetChild(xwini[local_win_id].saveasbox.popup,XmDIALOG_HELP_BUTTON);
        XtUnmanageChild(button);
        XtAddCallback(xwini[local_win_id].saveasbox.popup, 
                      XmNokCallback,
                       saveas_okfunc, 
                      (XtPointer) local_win_id);
        XtAddCallback(xwini[local_win_id].saveasbox.popup, 
                      XmNcancelCallback,
                       saveas_cancelfunc, 
                      (XtPointer) local_win_id);
        XtAddCallback(xwini[local_win_id].saveasbox.popup, 
                      XmNdestroyCallback,
                       saveas_destroyfunc, 
                      (XtPointer) local_win_id);
    }
    n = 0;
    n += PositionAtCursor(args);
    XtSetArg(args[n],XmNdialogTitle,
        XmStringCreateLtoR(title, XmSTRING_DEFAULT_CHARSET));
    n++;
    XtSetArg(args[n],XmNdirSpec,
        XmStringCreateLtoR(filename,XmSTRING_DEFAULT_CHARSET));
    n++;
    XtSetValues (xwini[local_win_id].saveasbox.popup, args, n);
    XtManageChild (xwini[local_win_id].saveasbox.popup);
}

static void init_saveas_box(int local_win_id)
{
    xwini[local_win_id].saveasbox.popup = NULL;
}

char *g_SCREEN_popup_saveas(int win_id, char *title, char *filename)
{
    static XEvent event;
    int local_win_id = GET_LOCAL_ID(win_id);

    xwini[local_win_id].saveasbox.status = SELECT_STATUS_WAIT;
    SaveAsDialogBox(local_win_id, title, filename);
    INTERNAL_dispatch_message();
    while (!INTERNAL_is_event() && xwini[local_win_id].saveasbox.status == SELECT_STATUS_WAIT) {
        XtAppNextEvent(INTERNAL_app_context,&event);
        XtDispatchEvent(&event);
    }
    if (xwini[local_win_id].saveasbox.status == SELECT_STATUS_OK) 
        return (char*) &(xwini[local_win_id].saveasbox.string);
    return NULL;
}


/* --- LISTBOX ---*/


static void InitList(int local_win_id, int item_count, char *items[], int select_pos)
{
    XmString list_items[MAX_LIST_ITEMS+1];
    int i;
      
    XmListDeleteAllItems(xwini[local_win_id].listbox.list);
    if (item_count == 0)  
        return;
    if (item_count > MAX_LIST_ITEMS)
        item_count = MAX_LIST_ITEMS;
    select_pos = min(select_pos, item_count);
    for (i=0; i < item_count; i++) 
        list_items[i] = XmStringCreateLtoR(items[i],XmSTRING_DEFAULT_CHARSET);
    list_items[i] = NULL;
    XmListAddItems(xwini[local_win_id].listbox.list, list_items, item_count, 0);
    XmListSetBottomPos(xwini[local_win_id].listbox.list, select_pos);
    XmListSelectPos(xwini[local_win_id].listbox.list, select_pos , FALSE);
    for (i=0; i < item_count; i++)
        XmStringFree(list_items[i]);
}

static void listbox_okfunc(Widget w, XtPointer tag, XtPointer reason)
{
    int local_win_id = (int) tag;
    xwini[local_win_id].listbox.status = SELECT_STATUS_OK;
    XtDestroyWidget(xwini[local_win_id].listbox.popup);
}

static void listbox_cancelfunc(Widget w, XtPointer tag, XtPointer reason)
{
    int local_win_id = (int) tag;
    xwini[local_win_id].listbox.pos = 0;
    xwini[local_win_id].listbox.status = SELECT_STATUS_CANCEL;
    XtDestroyWidget(xwini[local_win_id].listbox.popup);
}

static void listbox_destroyfunc(Widget w, XtPointer tag, XtPointer reason)
{
    int local_win_id = (int) tag;
    xwini[local_win_id].listbox.popup = NULL;
}

static void listbox_selectfunc(Widget w, XtPointer tag, XtPointer reason)
{
    int local_win_id = (int) tag;
    XmListCallbackStruct *cb = (XmListCallbackStruct *) reason;

    xwini[local_win_id].listbox.pos = cb->item_position;
}

static void MakeListBox(int local_win_id, char *title, char *subtitle, 
                    int items_visible)
{
    Arg args[10];
    int n;
    Widget frame[2], rowcol, button, label;

    n=0;
    n += PositionAtCursor(args);
    XtSetArg(args[n], XmNdialogTitle, 
                XmStringCreate(title, XmSTRING_DEFAULT_CHARSET)); 
    n++;
    XtSetArg(args[n], XmNdialogStyle, XmDIALOG_FULL_APPLICATION_MODAL); 
    n++;
    xwini[local_win_id].listbox.popup = XmCreateMessageDialog(xwini[local_win_id].form_widget, 
             "SelectFromList", args, n);
    XtAddEventHandler(xwini[local_win_id].listbox.popup,
                      StructureNotifyMask,
                      FALSE,
                      StructureNotifyProc2,
                      (XtPointer)local_win_id);

    n=0;

    XtSetArg(args[n], XmNtopAttachment,    XmATTACH_FORM); 
    n++;
    XtSetArg(args[n], XmNleftAttachment,   XmATTACH_FORM); 
    n++;
    XtSetArg(args[n], XmNrightAttachment,  XmATTACH_FORM); 
    n++;
    XtSetArg(args[n], XmNbottomAttachment, XmATTACH_WIDGET); 
    n++;
    XtSetArg(args[n], XmNinitialResourcesPersistent, FALSE); 
    n++;
/*
    XtSetArg(args[n], XmNmarginHeight, 5);
    n++;
*/
    frame[0] = XmCreateFrame(xwini[local_win_id].listbox.popup, "listframe0", args, n);
    XtManageChild(frame[0]);

    n=0;
    XtSetArg(args[n], XmNchildType, XmFRAME_TITLE_CHILD);
    n++;
/*
    XtSetArg(args[n], XmNchildHorizontalAlignment, XmALIGNMENT_CENTER);
    n++;
*/
    label = XmCreateLabel(frame[0], subtitle, args, n);
    XtManageChild(label);


    n=0;
    XtSetArg(args[n], XmNvisibleItemCount, items_visible); 
    n++;
    XtSetArg(args[n], XmNselectionPolicy,  XmSINGLE_SELECT); 
    n++;
    XtSetArg(args[n], XmNlistSizePolicy,  XmRESIZE_IF_POSSIBLE); 
    n++;   
    xwini[local_win_id].listbox.list = XmCreateScrolledList(frame[0], "listlist", args, n);
    XtManageChild(xwini[local_win_id].listbox.list);
    XtAddCallback(xwini[local_win_id].listbox.list,  XmNdefaultActionCallback, 
                  listbox_okfunc, (XtPointer) local_win_id);
    XtAddCallback(xwini[local_win_id].listbox.list,  XmNsingleSelectionCallback, 
                  listbox_selectfunc, (XtPointer) local_win_id);

    XtAddCallback(xwini[local_win_id].listbox.popup,  XmNdestroyCallback, 
                  listbox_destroyfunc, (XtPointer) local_win_id);

    button = XmMessageBoxGetChild(xwini[local_win_id].listbox.popup,XmDIALOG_OK_BUTTON);
    XtAddCallback(button, XmNactivateCallback, 
                 listbox_okfunc, (XtPointer) local_win_id);
    n = 0;
    XtSetArg(args[n],XmNokLabelString,
        XmStringCreateLtoR("Select",XmSTRING_DEFAULT_CHARSET));
    n++;
    XtSetValues(xwini[local_win_id].listbox.popup, args, n);

    button = XmMessageBoxGetChild(xwini[local_win_id].listbox.popup,XmDIALOG_CANCEL_BUTTON);
    XtAddCallback(button, XmNactivateCallback, 
                 listbox_cancelfunc, (XtPointer) local_win_id);
    
    button = XmMessageBoxGetChild(xwini[local_win_id].listbox.popup,XmDIALOG_HELP_BUTTON);
    XtUnmanageChild(button);

    XtManageChild(xwini[local_win_id].listbox.popup);
    XmProcessTraversal(xwini[local_win_id].listbox.popup,XmTRAVERSE_NEXT_TAB_GROUP);
    XmProcessTraversal(xwini[local_win_id].listbox.popup,XmTRAVERSE_NEXT_TAB_GROUP);
}


static void init_list_box(int local_win_id)
{
    xwini[local_win_id].listbox.popup = NULL;
}

int g_SCREEN_popup_listbox(int win_id, char *title, char *subtitle, int item_count, 
                              char **items, int select_pos, int items_visible)
{
    static XEvent event;
    int local_win_id = GET_LOCAL_ID(win_id);

    xwini[local_win_id].listbox.status = SELECT_STATUS_WAIT;
    xwini[local_win_id].listbox.pos = select_pos;
    MakeListBox(local_win_id, title, subtitle, items_visible);
    InitList(local_win_id, item_count, items, select_pos);
    INTERNAL_dispatch_message();
    while (!INTERNAL_is_event() && xwini[local_win_id].listbox.status == SELECT_STATUS_WAIT) {
        XtAppNextEvent(INTERNAL_app_context,&event);
        XtDispatchEvent(&event);
    }
    return xwini[local_win_id].listbox.pos;
}

/* --- RADIOBOX ---*/

static void radiobox_okfunc(Widget w, XtPointer tag, XtPointer reason)
{
    int local_win_id = (int) tag;
    xwini[local_win_id].radiobox.status = SELECT_STATUS_OK;
    XtDestroyWidget(xwini[local_win_id].radiobox.popup);
}

static void radiobox_cancelfunc(Widget w, XtPointer tag, XtPointer reason)
{
    int local_win_id = (int) tag;
    xwini[local_win_id].radiobox.pos = 0;
    xwini[local_win_id].radiobox.status = SELECT_STATUS_CANCEL;
    XtDestroyWidget(xwini[local_win_id].radiobox.popup);
}

static void radiobox_destroyfunc(Widget w, XtPointer tag, XtPointer reason)
{
    int local_win_id = (int) tag;
    xwini[local_win_id].radiobox.popup = NULL;
}


static void radiobutton_func(Widget w, XtPointer tag, XtPointer reason)
{
    int local_win_id = ((int) tag) >> 16;
    XmToggleButtonCallbackStruct *cb = (XmToggleButtonCallbackStruct *) reason;

    if (cb->set)
        xwini[local_win_id].radiobox.pos = ((int)tag) & 0xffff;
}

static void RadioBox(int local_win_id, char *title, char *subtitle, int item_count, 
                                                   char **items, int select_pos)
{
    Arg args[10];
    int i,n;
    Widget radio, frame[2], rowcol, button, focus, label;

    n=0;
    n += PositionAtCursor(args);
    XtSetArg(args[n], XmNdialogTitle, 
                XmStringCreate(title, XmSTRING_DEFAULT_CHARSET)); 
    n++;
    XtSetArg(args[n], XmNdialogStyle, XmDIALOG_FULL_APPLICATION_MODAL); 
    n++;
    xwini[local_win_id].radiobox.popup = 
        XmCreateMessageDialog(xwini[local_win_id].form_widget, "RadioBox", args, n);

    XtAddEventHandler(xwini[local_win_id].radiobox.popup,
                      StructureNotifyMask,
                      FALSE,
                      StructureNotifyProc2,
                      (XtPointer)local_win_id);
    n=0;
    XtSetArg(args[n], XmNinitialResourcesPersistent, FALSE); 
    n++;
    frame[0] = XmCreateFrame(xwini[local_win_id].radiobox.popup, "radioframe0", args, n);

    XtManageChild(frame[0]);

    n=0;
    XtSetArg(args[n], XmNchildType, XmFRAME_TITLE_CHILD);
    n++;
/*
    XtSetArg(args[n], XmNchildHorizontalAlignment, XmALIGNMENT_CENTER);
    n++;
*/
    label = XmCreateLabel(frame[0], subtitle, args, n);
    XtManageChild(label);

    n = 0;
    XtSetArg(args[n], XmNchildType, XmFRAME_WORKAREA_CHILD);
    n++;
    XtSetArg(args[n], XmNresizePolicy, XmRESIZE_GROW); 
    n++;
    XtSetArg(args[n], XmNradioBehavior, True); 
    n++;
    radio = XmCreateRadioBox(frame[0], "radio0", args, n);

    for (i=0;i<item_count;i++) {   
        n = 0;
        if (i == select_pos - 1) {
            XtSetArg(args[n], XmNset, True); 
            n++;
        }
/*
        XtSetArg(args[n], XmNvisibleWhenOff, False); 
        n++;
*/ 
        button = XmCreateToggleButton(radio, items[i], args, n);
        XtAddCallback(button, XmNvalueChangedCallback, 
                               radiobutton_func, 
                              (XtPointer) ((i + 1) + (local_win_id << 16)));
        XtManageChild(button);
        if (i == select_pos - 1)
            focus = button;
    }

    n=0;
    XtSetArg(args[n], XmNinitialFocus, focus);
    n++;
    XtSetValues(radio, args, n);

    XtManageChild(radio);

    button = XmMessageBoxGetChild(xwini[local_win_id].radiobox.popup,XmDIALOG_OK_BUTTON);
    XtAddCallback(button, XmNactivateCallback, 
                 radiobox_okfunc, (XtPointer) local_win_id);

    button = XmMessageBoxGetChild(xwini[local_win_id].radiobox.popup,XmDIALOG_CANCEL_BUTTON);
    XtAddCallback(button, XmNactivateCallback, 
                 radiobox_cancelfunc, (XtPointer) local_win_id);
    
    button = XmMessageBoxGetChild(xwini[local_win_id].radiobox.popup,XmDIALOG_HELP_BUTTON);
    XtUnmanageChild(button);
    
    XtAddCallback(xwini[local_win_id].radiobox.popup, XmNdestroyCallback, 
                 radiobox_destroyfunc, (XtPointer) local_win_id);

    XtManageChild(xwini[local_win_id].radiobox.popup);                              
    XmProcessTraversal(xwini[local_win_id].radiobox.popup,XmTRAVERSE_NEXT_TAB_GROUP);
    XmProcessTraversal(xwini[local_win_id].radiobox.popup,XmTRAVERSE_NEXT_TAB_GROUP);
}

static void init_radio_box(int local_win_id)
{
    xwini[local_win_id].radiobox.popup = NULL;
}

int g_SCREEN_popup_radiobox(int win_id, char *title, char *subtitle, int item_count, 
                                              char **items, int select_pos)
{
    static XEvent event;
    int local_win_id = GET_LOCAL_ID(win_id);

    xwini[local_win_id].radiobox.status = SELECT_STATUS_WAIT;
    xwini[local_win_id].radiobox.pos = select_pos;
    RadioBox(local_win_id, title, subtitle, item_count, 
             items, select_pos);
    INTERNAL_dispatch_message();
    while (!INTERNAL_is_event() && xwini[local_win_id].radiobox.status == SELECT_STATUS_WAIT) {
        XtAppNextEvent(INTERNAL_app_context,&event);
        XtDispatchEvent(&event);
    }
    return xwini[local_win_id].radiobox.pos;
}

/* --- CHECKBOX ---*/



static void checkbox_okfunc(Widget w, XtPointer tag, XtPointer reason)
{
    int local_win_id = (int) tag;
    xwini[local_win_id].checkbox.status = SELECT_STATUS_OK;
    XtDestroyWidget(xwini[local_win_id].checkbox.popup);
}

static void checkbox_cancelfunc(Widget w, XtPointer tag, XtPointer reason)
{
    int local_win_id = (int) tag;
    xwini[local_win_id].checkbox.status = SELECT_STATUS_CANCEL;
    XtDestroyWidget(xwini[local_win_id].checkbox.popup);
}

static void checkbox_destroyfunc(Widget w, XtPointer tag, XtPointer reason)
{
    int local_win_id = (int) tag;
    xwini[local_win_id].checkbox.popup = NULL;
}

static void checkbutton_func(Widget w, XtPointer tag, XtPointer reason)
{
    int local_win_id = ((int) tag) >> 16;
    XmToggleButtonCallbackStruct *cb = (XmToggleButtonCallbackStruct *) reason;

    xwini[local_win_id].checkbox.pos[((int) tag) & 0xffff] = cb->set;
}


static void CheckBox(int local_win_id, char *title, char *subtitle, 
                     int item_count, char **items, int *select)
{
    Arg args[10];
    int i,n;
    Widget radio, frame[2], rowcol, button, label;

    n=0;
    n += PositionAtCursor(args);
    XtSetArg(args[n], XmNdialogTitle, 
                XmStringCreate(title, XmSTRING_DEFAULT_CHARSET)); 
    n++;
    XtSetArg(args[n], XmNdialogStyle, XmDIALOG_FULL_APPLICATION_MODAL); 
    n++;
    xwini[local_win_id].checkbox.popup = XmCreateMessageDialog(xwini[local_win_id].form_widget, 
              "CheckBox", args, n);

    XtAddEventHandler(xwini[local_win_id].checkbox.popup,
                      StructureNotifyMask,
                      FALSE,
                      StructureNotifyProc2,
                      (XtPointer)local_win_id);
    n=0;
    frame[0] = XmCreateFrame(xwini[local_win_id].checkbox.popup, "checkframe0", args, n);
    XtManageChild(frame[0]);

    n=0;
    XtSetArg(args[n], XmNchildType, XmFRAME_TITLE_CHILD);
    n++;
/*
    XtSetArg(args[n], XmNchildHorizontalAlignment, XmALIGNMENT_CENTER);
    n++;
*/
    label = XmCreateLabel(frame[0], subtitle, args, n);
    XtManageChild(label);

    n = 0;
    XtSetArg(args[n], XmNchildType, XmFRAME_WORKAREA_CHILD);
    n++;
    XtSetArg(args[n], XmNresizePolicy, XmRESIZE_GROW); 
    n++;
    XtSetArg(args[n], XmNradioBehavior, FALSE); 
    n++;
    radio = XmCreateRadioBox(frame[0], "check0", args, n);

    for (i=0;i<item_count;i++) {   
        n = 0;
        if (select[i]) {
            XtSetArg(args[n], XmNset, True); 
            n++;
        }
/*
        XtSetArg(args[n], XmNvisibleWhenOff, False); 
        n++;
*/ 
        button = XmCreateToggleButton(radio, items[i], args, n);
        XtAddCallback(button, XmNvalueChangedCallback, 
                              checkbutton_func, 
                              (XtPointer) (i + (local_win_id << 16)));
        XtManageChild(button);
    }
    XtManageChild(radio);

    button = XmMessageBoxGetChild(xwini[local_win_id].checkbox.popup,XmDIALOG_OK_BUTTON);
    XtAddCallback(button, XmNactivateCallback, 
                 checkbox_okfunc, (XtPointer) local_win_id);

    button = XmMessageBoxGetChild(xwini[local_win_id].checkbox.popup,XmDIALOG_CANCEL_BUTTON);
    XtAddCallback(button, XmNactivateCallback, 
                 checkbox_cancelfunc, (XtPointer) local_win_id);
    
    button = XmMessageBoxGetChild(xwini[local_win_id].checkbox.popup,XmDIALOG_HELP_BUTTON);
    XtUnmanageChild(button);

    XtAddCallback(xwini[local_win_id].checkbox.popup, XmNdestroyCallback, 
                 checkbox_destroyfunc, (XtPointer) local_win_id);

    XtManageChild(xwini[local_win_id].checkbox.popup);
    XmProcessTraversal(xwini[local_win_id].checkbox.popup,XmTRAVERSE_NEXT_TAB_GROUP);
    XmProcessTraversal(xwini[local_win_id].checkbox.popup,XmTRAVERSE_NEXT_TAB_GROUP);
}


static void init_check_box(int local_win_id)
{
    xwini[local_win_id].checkbox.popup = NULL;
}

int g_SCREEN_popup_checkbox(int win_id, char *title, char *subtitle, 
                     int item_count, char **items, int *select)
{
    static XEvent event;
    int ret_value;
    int local_win_id = GET_LOCAL_ID(win_id);

    xwini[local_win_id].checkbox.status = SELECT_STATUS_WAIT;
    xwini[local_win_id].checkbox.pos = (int*)malloc(sizeof(int) * item_count);
    memcpy(xwini[local_win_id].checkbox.pos, select, sizeof(int) * item_count);
    CheckBox(local_win_id, title, subtitle, item_count, 
             items, xwini[local_win_id].checkbox.pos);
    INTERNAL_dispatch_message();
    while (!INTERNAL_is_event() && xwini[local_win_id].checkbox.status == SELECT_STATUS_WAIT) {
        XtAppNextEvent(INTERNAL_app_context,&event);
        XtDispatchEvent(&event);
    }
    if (xwini[local_win_id].checkbox.status == SELECT_STATUS_OK) {
        memcpy(select, xwini[local_win_id].checkbox.pos, sizeof(int) * item_count);
        ret_value = TRUE;
    }
    else
        ret_value = FALSE;
    free(xwini[local_win_id].checkbox.pos);
    return ret_value;
}

/**********************************************************************/
/* CONTAINER
*/

static int container_pool = 1;

static void init_container(int local_win_id)
{
    xwini[local_win_id].cont_info = NULL;
}


static CONTAINER_INFO *cont_info_from_cont_id(int cont_id)
{
    CONTAINER_INFO *inf,*temp;
    int i;

    for (i=0;i<G_MAXWINDOW;i++) {
        if (xwini[i].xwin != (Window) NULL &&
            xwini[i].cont_info != NULL) {
                inf = xwini[i].cont_info;
                while (inf != NULL) {
                    if (inf->cont_id == cont_id)
                        return inf;
                    inf = inf->next;
                }
        }
    }
    return NULL;
}

static CONTAINER_INFO *add_container(int win_id, int local_win_id, int flag)
{
    CONTAINER_INFO *inf,*temp;

    inf = (CONTAINER_INFO*) malloc(sizeof(CONTAINER_INFO));
    inf->win_id = win_id;
    inf->local_win_id = local_win_id;
    inf->cont_id = container_pool++;
    inf->flag = flag;
    inf->cont_select_status = SELECT_STATUS_OK;
    inf->contpopup = NULL;
    inf->container = NULL;
    inf->container_child = NULL;
    inf->panel = NULL;
    inf->okfunc = NULL;
    inf->cancelfunc = NULL;
    inf->next = NULL;
    temp = xwini[local_win_id].cont_info;
    if (temp == NULL) {
        xwini[local_win_id].cont_info = inf;
        return inf;
    }
    while (temp->next != NULL)
        temp = temp->next;
    temp->next = inf;
    return inf;   
}

static Widget add_panel(CONTAINER_INFO *cont_info, int hor, Widget parent)
{
    int n;
    Arg args[20];
    CONTAINER_PANEL *p,*q;
    
    p = (CONTAINER_PANEL *) malloc(sizeof(CONTAINER_PANEL));
    assert(p);
    p->next = NULL;

    n=0;
    if (hor) {
        XtSetArg(args[n], XmNorientation, XmHORIZONTAL); 
        n++;
    }
    XtSetArg(args[n], XmNallowOverlap, TRUE); 
    n++;
    XtSetArg(args[n], XmNmarginHeight, 0);
    n++;
    XtSetArg(args[n], XmNspacing, 0);
    n++;
    if (cont_info->panel == NULL) {
        p->cp = XmCreateRowColumn(parent, 
                                       "container_panel", args, n);
        cont_info->panel = p;
    }
    else {
        p->cp = XmCreateRowColumn(parent, 
                                       "container_panel", args, n);
        q = cont_info->panel;
        while (q->next) 
            q = q->next;
        q->next = p;
    }
    assert(p->cp);
    cont_info->container = p->cp;
    XtManageChild(p->cp);
    return p->cp;
}

static int pop_panel(CONTAINER_INFO *cont_info)
{
    CONTAINER_PANEL *p,*q,*r;

    r = p = q = cont_info->panel;
    if (q == NULL)
        return FALSE;
    while (q) {
        r = p;
        p = q;
        q = q->next;
    }
    if (r != p) {
        free(p);
        r->next = NULL;
        cont_info->container = r->cp;
    }
    else {
        free(r);
        cont_info->panel = NULL;
        cont_info->container = cont_info->scrollwindow;
    }
    return TRUE;
}

static void kill_container_func2(XtPointer tag, XtIntervalId *reason)
{
    int cont_id = (int) tag;
    CONTAINER_INFO *cont_info;

    cont_info = cont_info_from_cont_id(cont_id);
    if (cont_info == NULL)
        return;
    while (pop_panel(cont_info))
        ;
    XtDestroyWidget(cont_info->contpopup);
}

static void kill_container_func(Widget w, int cont_id)
{
    XtAppAddTimeOut(XtWidgetToApplicationContext(w), 
            100, kill_container_func2, (XtPointer) cont_id);
}

static void contpopup_okfunc(Widget w, XtPointer tag, XtPointer reason)
{
    CONTAINER_INFO *cont_info = (CONTAINER_INFO *) tag;

    cont_info->cont_select_status = SELECT_STATUS_OK;
    if (cont_info->okfunc) {
        static G_POPUP_CHILDINFO ci;
        g_popup_init_info(&ci);
        ci.type 	= G_CHILD_OK;
        ci.id		= -1;
        ci.cont_id 	= cont_info->cont_id;
        ci.win_id 	= cont_info->win_id;
        ci.func   	= cont_info->okfunc;
        ci.userdata   	= cont_info->okdata;
        ci.func(&ci);
    }
/* printf(".. ok\n"); */
    XtUnmanageChild(cont_info->contpopup);
}

static void contpopup_cancelfunc(Widget w, XtPointer tag, XtPointer reason)
{
    CONTAINER_INFO *cont_info = (CONTAINER_INFO *) tag;

    if (cont_info->cont_select_status == SELECT_STATUS_OK) {
        if (!(cont_info->flag & G_POPUP_KEEP) && !(cont_info->flag & G_POPUP_WAIT))
            kill_container_func(cont_info->contpopup, cont_info->cont_id);
        return;
    }
    cont_info->cont_select_status = SELECT_STATUS_CANCEL;

/*    XtUnmanageChild(cont_info->contpopup);*/
    if (cont_info->cancelfunc) {
        static G_POPUP_CHILDINFO ci;
        g_popup_init_info(&ci);
        ci.type 	= G_CHILD_CANCEL;
        ci.id		= -1;
        ci.cont_id 	= cont_info->cont_id;
        ci.win_id 	= cont_info->win_id;
        ci.func   	= cont_info->cancelfunc;
        ci.userdata   	= cont_info->canceldata;
        ci.func(&ci);
    }
    if (!(cont_info->flag & G_POPUP_KEEP) && !(cont_info->flag & G_POPUP_WAIT))
        kill_container_func(cont_info->contpopup, cont_info->cont_id);
 /* printf(".. cancel\n"); */
}

static void contpopup_unmanagefunc(Widget w, XtPointer tag, XtPointer reason)
{
    CONTAINER_INFO *cont_info = (CONTAINER_INFO *) tag;
    XtUnmanageChild(cont_info->contpopup);
/* printf("..unmanage\n"); */
}

static void contpopup_destroyfunc(Widget w, XtPointer tag, XtPointer reason)
{
    CONTAINER_CHILD *cc, *temp;    
    CONTAINER_INFO *next, *prev, *cont_info = (CONTAINER_INFO *) tag;
    int local_win_id = cont_info->local_win_id;

    cc = cont_info->container_child;
    while (cc) {
        temp = cc;
        cc = cc->next;
        if ((temp->type == G_CHILD_TEXTBOX || 
                temp->type == G_CHILD_MULTILINETEXT ||
                temp->type == G_CHILD_GETFILENAME   ||
                temp->type == G_CHILD_GETDIRNAME) && temp->label) 
            free(temp->label);
        if (temp->c)
            free(temp->c);
        if (temp->select)
            free(temp->select);
        if (temp->binfo)
            free(temp->binfo);
        free(temp);
    }
    prev = next = xwini[local_win_id].cont_info;
    if (next == cont_info) {
        xwini[local_win_id].cont_info = next->next;
        free(next);
        return;
    }
    while (next != cont_info) {
        prev = next;
        next = next->next;
        if (next == NULL)
            return;
    }
    prev->next = next->next;
    free(next);
}

static void ci2cc(G_POPUP_CHILDINFO *ci, CONTAINER_CHILD *cc)
{
    cc->type           = ci->type;
    cc->id             = ci->id;
    cc->cont_id        = ci->cont_id;
    cc->win_id         = ci->win_id;
    cc->item_count     = ci->item_count;
    cc->items_visible  = ci->items_visible;
    cc->item           = ci->item;
    cc->disabled       = ci->disabled;
    cc->horizontal     = ci->horizontal;
    cc->frame          = ci->frame;
    cc->title          = ci->title;   
/* This is handeled elsewhere
    cc->select         = ci->select;
*/
    cc->label          = ci->label;
    cc->data           = ci->data;
    cc->func           = ci->func;
    cc->userdata       = ci->userdata;
}

static void cc2ci(CONTAINER_CHILD *cc, G_POPUP_CHILDINFO *ci)
{
    ci->type           = cc->type;
    ci->id             = cc->id;
    ci->cont_id        = cc->cont_id;
    ci->win_id         = cc->win_id;
    ci->item_count     = cc->item_count;
    ci->items_visible  = cc->items_visible;
    ci->item           = cc->item;
    ci->disabled       = cc->disabled;
    ci->horizontal     = cc->horizontal;
    ci->frame          = cc->frame;
    ci->title          = cc->title;   
/* This is handeled elsewhere
    ci->select         = cc->select;
*/
    ci->label          = cc->label;
    ci->data           = cc->data;
    ci->func           = cc->func;
    ci->userdata       = cc->userdata;
}

static Widget ContainerCreateFrame(CONTAINER_CHILD *cc)
{
    Arg args[20];
    int n;
    Widget label;
    char Name[20];


    if (! cc->frame) {
        cc->f = NULL;
        return cc->p;
    }

    n=0;
    if (cc->disabled) {
        XtSetArg(args[n], XmNsensitive, FALSE);
        n++;
    }
    sprintf(Name,"BFrame%d",cc->cc_id);
    cc->f  = XmCreateFrame(cc->p, Name, args, n);
    XtManageChild(cc->f);

    if (cc->title && strlen(cc->title)) {
        n=0;
        XtSetArg(args[n], XmNchildType, XmFRAME_TITLE_CHILD);
        n++;
/*
        XtSetArg(args[n], XmNchildHorizontalAlignment, XmALIGNMENT_CENTER);
        n++;
*/
        label = XmCreateLabel(cc->f, cc->title, args, n);
        XtManageChild(label);
        cc->t = label;
    }
    return cc->f;
}

static void ContainerAddPanel(CONTAINER_CHILD *cc,CONTAINER_INFO *cont_info)
{
    Widget frame;

    if (cc->item == 0) {
        pop_panel(cont_info);
        return;
    }

    frame = ContainerCreateFrame(cc);

    cc->w = add_panel(cont_info, cc->horizontal, frame);
}


static void contbutton_func(Widget w, XtPointer closure, XtPointer call_data)
{
    int tag = (int) closure;
    unsigned long *reason = (unsigned long *) call_data;
    XmToggleButtonCallbackStruct *cb = (XmToggleButtonCallbackStruct *) reason;
    CONTAINER_CHILD *cc;        
    static G_POPUP_CHILDINFO ci;
    int button_id, cc_id, state, cont_id;
    CONTAINER_INFO *cont_info;
    BUTTON_INFO *binfo;

    binfo 	 = (BUTTON_INFO*) tag;
    button_id    = binfo->button_id;
    cc_id        = binfo->cc_id;
    cont_id      = binfo->cont_id;
    state        = cb->set;
    cont_info    = cont_info_from_cont_id(cont_id);
    if (cont_info == NULL)
        return;
    cc = cont_info->container_child;
    while (cc) {
        if (cc->cc_id == cc_id) { 
            if ((cc->type == G_CHILD_RADIOBOX) && !cb->set)
                return;
            cc->item = button_id;
            if (cc->type == G_CHILD_CHECKBOX) { 
                 cc->select[button_id] = state;
                 ci.select = cc->select;
            }
            if (cc->func) {
                cc2ci(cc,&ci);
                cc->func(&ci);
            }
            return;
        }
        cc = cc->next;
    }
}


static void ContainerAddRadioBox(CONTAINER_CHILD *cc)
{
    Arg args[20];
    int i,n,ok;
    Widget frame,label,button,focus=NULL;
    char Name[20];

    frame = ContainerCreateFrame(cc);

    n=0;
    XtSetArg(args[n], XmNnavigationType, XmEXCLUSIVE_TAB_GROUP);
    n++;
    if (cc->type == G_CHILD_RADIOBOX) 
        XtSetArg(args[n], XmNradioBehavior, TRUE); 
    else
        XtSetArg(args[n], XmNradioBehavior, FALSE); 
    n++;
    if (cc->horizontal) {
        XtSetArg(args[n], XmNorientation, XmHORIZONTAL); 
        n++;
    }
    if (cc->disabled) {
        XtSetArg(args[n], XmNsensitive, FALSE);
        n++;
    }
    XtSetArg(args[n], XmNpacking, XmPACK_TIGHT);
    n++;
    XtSetArg(args[n], XmNmarginHeight, 0);
    n++;
    XtSetArg(args[n], XmNspacing, 0);
    n++;
    sprintf(Name,"R%d",cc->cc_id);
    cc->w = XmCreateRadioBox(frame, Name, args, n);

    cc->c = (Widget*) malloc(sizeof(Widget) * cc->item_count);
    if (cc->item_count > 0) 
        cc->binfo = (BUTTON_INFO*) malloc(sizeof(BUTTON_INFO)*cc->item_count);
    for (i=0;i< cc->item_count;i++) {   
        n = 0;
        if (((cc->type == G_CHILD_CHECKBOX) && cc->select[i]) ||
            ((cc->type == G_CHILD_RADIOBOX) && cc->item == i)) {
            XtSetArg(args[n], XmNset, True); 
            n++;
            ok = TRUE;
        }
        else
            ok = FALSE;
/*
        XtSetArg(args[n], XmNvisibleWhenOff, False); 
        n++;
*/
        button = XmCreateToggleButton(cc->w, cc->data[i], args, n);
        cc->binfo[i].button_id	= i;
        cc->binfo[i].cc_id	= cc->cc_id;
        cc->binfo[i].cont_id	= cc->cont_id;

        XtAddCallback(button, XmNvalueChangedCallback, 
                              contbutton_func, 
                              (XtPointer) &(cc->binfo[i]));

        XtManageChild(button);
        cc->c[i] = button;

        if (ok)
            focus = button;

    }

    if (focus) {
        n=0;
        XtSetArg(args[n], XmNinitialFocus, focus);
        n++;
        XtSetValues(cc->w, args, n);
    }

    XtManageChild(cc->w);

}

static void conttext_func(Widget w, XtPointer closure, XtPointer call_data)
{
    int tag = (int) closure;
    unsigned long *reason = (unsigned long *) call_data;
    char *text;
    CONTAINER_CHILD *cc = (CONTAINER_CHILD*) tag;        
    static G_POPUP_CHILDINFO ci;

    if (cc->text_callback_disabled)
        return;
    text = XmTextGetString(w);
    cc->label = (char*) realloc(cc->label,strlen(text)+1);
    strcpy(cc->label,text);
    XtFree(text);
    cc->item = 0;
    if (cc->func) {
        cc2ci(cc,&ci);
        cc->func(&ci);
    }
}

static void conttext_focus_func(Widget w, XtPointer closure, XtPointer call_data)
{
    int tag = (int) closure;
    unsigned long *reason = (unsigned long *) call_data;
    char *text;
return;
    text = XmTextGetString(w);
    if (strlen(text))
        XmTextSetSelection(w, 0, strlen(text), (Time) 0);
    XtFree(text);
}


static void ContainerAddTextBox(CONTAINER_CHILD *cc, int multi)
{
    Widget frame, area;
    Arg args[20];
    int n;
    char Name[20];

    if (cc->horizontal && cc->title) 
        frame = cc->p;
    else
        frame = ContainerCreateFrame(cc);

    n=0;
    if (cc->disabled) {
        XtSetArg(args[n], XmNsensitive, FALSE);
        n++;
    }
    if (cc->horizontal && cc->title) {
        Widget label;
        XtSetArg(args[n], XmNorientation, XmHORIZONTAL);
        n++;
        XtSetArg(args[n], XmNnavigationType, XmEXCLUSIVE_TAB_GROUP);
        n++;
        XtSetArg(args[n], XmNchildType, XmFRAME_WORKAREA_CHILD);
        n++;
        XtSetArg(args[n], XmNresizePolicy, XmRESIZE_GROW); 
        n++;
        XtSetArg(args[n], XmNallowOverlap, TRUE); 
        n++;
        area = XmCreateRowColumn(frame, "rowcoltext", args, n);
        XtManageChild(area);    
        n=0;
        XtSetArg(args[n], XmNlabelString, 
            XmStringCreate(cc->title, XmSTRING_DEFAULT_CHARSET)); 
        n++;
        label = XmCreateLabel(area, "arealabel", args, n );                     
        XtManageChild(label);    
        n = 0;                                                                         
    }
    else
        area = frame;
                                                  
    XtSetArg(args[n], XmNnavigationType, XmEXCLUSIVE_TAB_GROUP);
    n++;
    XtSetArg(args[n], XmNvalue, cc->label); 
    n++;
    if (cc->item_count) {
        if (!multi) {
            XtSetArg(args[n], XmNmaxLength, cc->item_count); 
            n++;
        }
        else if (cc->item_count > 1) {
            XtSetArg(args[n], XmNeditMode, XmMULTI_LINE_EDIT);
            n++;
            XtSetArg(args[n], XmNrows, cc->item_count); 
            n++;
            XtSetArg(args[n], XmNscrollingPolicy, XmAUTOMATIC); 
            n++;
            XtSetArg(args[n], XmNscrollBarDisplayPolicy, XmAS_NEEDED); 
            n++;
        }
    }
    if (cc->items_visible) {
        XtSetArg(args[n], XmNcolumns, cc->items_visible); 
        n++;
    }
    sprintf(Name,"BText%d",cc->cc_id);      
    if (multi)  
        cc->w = XmCreateScrolledText(frame, Name, args, n);
    else
        cc->w = XmCreateText(area, Name, args, n);

    cc->text_callback_disabled = FALSE;
    XtAddCallback(cc->w, XmNvalueChangedCallback, 
                         conttext_func, 
                         (XtPointer) (cc));

    if (!multi)
        XtAddCallback(cc->w, XmNfocusCallback, 
                         conttext_focus_func, 
                         (XtPointer) (cc));

    XtManageChild(cc->w);
}


static void ContainerAddLabel(CONTAINER_CHILD *cc)
{
    Widget frame;
    Arg args[20];
    int n;
   
    frame = ContainerCreateFrame(cc);

    n=0;
    if (cc->disabled) {
        XtSetArg(args[n], XmNsensitive, FALSE);
        n++;
    }
/*
    XtSetArg(args[n], XmNchildHorizontalAlignment, XmALIGNMENT_CENTER);
    n++;  
*/
    cc->w = XmCreateLabel(frame, cc->label, args, n);
    XtManageChild(cc->w);

}



static void contlistbox_func(Widget w, XtPointer closure, XtPointer call_data)
{
    int tag = (int) closure;
    unsigned long *reason = (unsigned long *) call_data;
    XmListCallbackStruct *cb = (XmListCallbackStruct *) reason;
    int i;
    CONTAINER_CHILD *cc = (CONTAINER_CHILD*) tag;        
    static G_POPUP_CHILDINFO ci;

    cc->item   = cb->item_position - 1;
    if (cc->func) {
        cc2ci(cc,&ci);
        cc->func(&ci);
    }
}

static void ContainerAddListBox(CONTAINER_CHILD *cc)
{
    Arg args[20];
    int i,n;
    Widget frame,label,button;
    char Name[20];
    XmString list_items[MAX_CONT_ITEMS+1];
    int select_pos = 1;

    frame = ContainerCreateFrame(cc);
    select_pos = cc->item + 1;
    for (i=0; i < cc->item_count; i++) 
        list_items[i] = XmStringCreateLtoR(cc->data[i],XmSTRING_DEFAULT_CHARSET);
    list_items[i] = NULL;
    if (cc->items_visible <= 0)
        cc->items_visible = cc->item_count;
    n=0;
    if (cc->disabled) {
        XtSetArg(args[n], XmNsensitive, FALSE);
        n++;
    }
    XtSetArg(args[n], XmNnavigationType, XmEXCLUSIVE_TAB_GROUP);
    n++;
    XtSetArg(args[n], XmNvisibleItemCount, cc->items_visible); 
    n++;
    XtSetArg(args[n], XmNitemCount, cc->item_count); 
    n++;
    XtSetArg(args[n], XmNitems, list_items); 
    n++;
    XtSetArg(args[n], XmNselectionPolicy,  XmSINGLE_SELECT); 
    n++;
    XtSetArg(args[n], XmNlistSizePolicy,  XmRESIZE_IF_POSSIBLE); 
    n++;   
    sprintf(Name,"BList%d",cc->cc_id);
    cc->w = XmCreateScrolledList(frame, Name, args, n);

    XtAddCallback(cc->w,  XmNsingleSelectionCallback, 
                          contlistbox_func, 
                          (XtPointer) (cc));
    XtManageChild(cc->w);

    XmListSetBottomPos(cc->w, select_pos);
    XmListSelectPos(cc->w, select_pos , FALSE);

    for (i=0; i < cc->item_count; i++)
        XmStringFree(list_items[i]);

}

static void contpushbutton_func(Widget w, XtPointer closure, XtPointer call_data)
{
    int tag = (int) closure;
    unsigned long *reason = (unsigned long *) call_data;
    CONTAINER_CHILD *cc;        
    static G_POPUP_CHILDINFO ci;
    int i, button_id, cc_id, cont_id;
    CONTAINER_INFO *cont_info;
    BUTTON_INFO *binfo;

    binfo = (BUTTON_INFO*) tag;
    button_id    = binfo->button_id;
    cc_id        = binfo->cc_id;
    cont_id      = binfo->cont_id;
    cont_info    = cont_info_from_cont_id(cont_id);
    if (cont_info == NULL)
        return;
    cc = cont_info->container_child;
    while (cc) {
        if (cc->cc_id == cc_id) { 
            cc->item = button_id;
            if (cc->func) {
                cc2ci(cc,&ci);
                cc->func(&ci);
            }
            return;
        }
        cc = cc->next;
    }
}


static void ContainerAddOptionMenu(CONTAINER_CHILD *cc)
{
    Arg args[20];
    int i,n;
    Widget frame,button,focus,pulldown;
    char Name[20];
    XmString list_items[MAX_CONT_ITEMS+1];
    int select_pos = 1;

    frame = ContainerCreateFrame(cc);
    select_pos = cc->item + 1;
    sprintf(Name,"ContPullDown%d",cc->cc_id);
    pulldown = XmCreatePulldownMenu(frame, Name, NULL, 0);

    n=0;
    if (cc->disabled) {
        XtSetArg(args[n], XmNsensitive, FALSE);
        n++;
    }
    if (!cc->horizontal) {
        XtSetArg(args[n], XmNorientation, XmVERTICAL);
        n++;
    }
    XtSetArg(args[n], XmNnavigationType, XmEXCLUSIVE_TAB_GROUP);
    n++;
    XtSetArg(args[n], XmNchildType, XmFRAME_WORKAREA_CHILD);
    n++;
    XtSetArg(args[n], XmNresizePolicy, XmRESIZE_GROW); 
    n++;
    XtSetArg(args[n], XmNsubMenuId, pulldown); 
    n++;
    XtSetArg(args[n], XmNlabelString, XmStringCreate(cc->label, XmSTRING_DEFAULT_CHARSET)); 
    n++;
    sprintf(Name,"ContOption%d",cc->cc_id);
    cc->w = XmCreateOptionMenu(frame, Name, args, n);
    XtManageChild(cc->w);

    cc->c = (Widget*) malloc(sizeof(Widget) * cc->item_count);
    if (cc->item_count > 0) 
        cc->binfo = (BUTTON_INFO*) malloc(sizeof(BUTTON_INFO)*cc->item_count);
    for (i=0;i<cc->item_count;i++) {   
        n = 0;
        button = XmCreatePushButton(pulldown, cc->data[i], args, n);
        cc->binfo[i].button_id	= i;
        cc->binfo[i].cc_id	= cc->cc_id;
        cc->binfo[i].cont_id	= cc->cont_id;
        XtAddCallback(button, XmNactivateCallback, 
                              contpushbutton_func, 
                              (XtPointer) &(cc->binfo[i]));
        XtManageChild(button);
        cc->c[i] = button;
        if (i == select_pos - 1)
            focus = button;
    }

    n=0;
    XtSetArg(args[n], XmNmenuHistory, focus);
    n++;
    XtSetValues(cc->w, args, n);
}

static void ContainerAddSeparator(CONTAINER_CHILD *cc)
{
    Arg args[2];
    int n = 0;
    char Name[20];

    ContainerCreateFrame(cc);
    sprintf(Name,"ContSep%d",cc->cc_id);
    cc->w = XmCreateSeparatorGadget(cc->p, Name, args, n);
    XtManageChild(cc->w);
}

/*
 * Scale child
 */
static void contscale_func(Widget w, XtPointer closure, XtPointer call_data)
{
    int tag = (int) closure;
    unsigned long *reason = (unsigned long *) call_data;
    XmScaleCallbackStruct *cb = (XmScaleCallbackStruct *) reason;
    CONTAINER_CHILD *cc = (CONTAINER_CHILD*) tag;        
    static G_POPUP_CHILDINFO ci;

    cc->item   = cb->value;
    if (cc->func) {
        cc2ci(cc,&ci);
        cc->func(&ci);
    }
}

static void ContainerAddScale(CONTAINER_CHILD *cc)
{
    Arg args[20];
    int n, tic_mark_count;
    Widget frame;
    char Name[20];

    frame = ContainerCreateFrame(cc);
    n=0;
    if (cc->disabled) {
        XtSetArg(args[n], XmNsensitive, FALSE);
        n++;
    }
    if (cc->horizontal) {
        XtSetArg(args[n], XmNorientation, XmHORIZONTAL);
        n++;
    }
    XtSetArg(args[n], XmNnavigationType, XmEXCLUSIVE_TAB_GROUP);
    n++;
    XtSetArg(args[n], XmNchildType, XmFRAME_WORKAREA_CHILD);
    n++;
    XtSetArg(args[n], XmNresizePolicy, XmRESIZE_GROW); 
    n++;
    if (cc->label) {
        XtSetArg(args[n], XmNtitleString, 
            XmStringCreate(cc->label, XmSTRING_DEFAULT_CHARSET)); 
        n++;
    }
#if (XmVERSION < 2) 
#define XmNEAR_BORDER 1
#define XmNEAR_SLIDER 1
#endif
    XtSetArg(args[n], XmNvalue, cc->item); 
    n++;
    if (cc->select[0] > cc->select[1]) {
        if (cc->horizontal)
            XtSetArg(args[n], XmNprocessingDirection, XmMAX_ON_LEFT); 
        else
            XtSetArg(args[n], XmNprocessingDirection, XmMAX_ON_BOTTOM); 
        n++;
        XtSetArg(args[n], XmNminimum, cc->select[1]); 
        n++;
        XtSetArg(args[n], XmNmaximum, cc->select[0]); 
        n++;
    }
    else {
        XtSetArg(args[n], XmNminimum, cc->select[0]); 
        n++;
        XtSetArg(args[n], XmNmaximum, cc->select[1]); 
        n++;
    }
    if (cc->select[2] >= 0) {
        XtSetArg(args[n], XmNdecimalPoints, cc->select[2]); 
        n++;
    }
    /*
     * If decimal position is negative, do not show label at all
     */
    if (cc->select[2] < 0) {
        XtSetArg(args[n], XmNshowValue, XmNONE); 
        n++;
    }
    else {
        XtSetArg(args[n], XmNshowValue, XmNEAR_SLIDER); 
        n++;
    }
    sprintf(Name,"Scale%d",cc->cc_id);
    cc->w = XmCreateScale(frame, Name, args, n);
    /* Now build the tic marks. */
    tic_mark_count = max(cc->item_count,cc->items_visible);
    if (tic_mark_count > 0) {
        unsigned char   tic_orientation;
        Dimension       tic_long_dim = 7, tic_short_dim = 5;
        Dimension       tic_width, tic_height;
        char            tic_name[10];
        int            *tic_array;
        int i, label_count = 0, tic_count = 0;
        Widget          tics;
        tic_array = (int*) malloc(2*tic_mark_count * sizeof(int));
        for (i=0;i<tic_mark_count;i++)
            tic_array[i] = FALSE;
        if (cc->items_visible && cc->data) {
            tic_array[0] = TRUE;
            if (cc->items_visible == 2)
                tic_array[tic_mark_count-1] = TRUE;
            else if (cc->items_visible > 2) {
                int num_t = (tic_mark_count - cc->items_visible)/
                    (cc->items_visible-1);
                
                for (i=0;i<cc->items_visible-1;i++) 
                    tic_array[(i+1)*(num_t+1)] = TRUE;
            }
        }
        if (cc->horizontal) {
            tic_orientation = XmVERTICAL;
            tic_width = tic_short_dim;
            tic_height = tic_long_dim;
        } else {
            tic_orientation = XmHORIZONTAL;
            tic_width = tic_long_dim;
            tic_height = tic_short_dim;
        }
   
        n = 0;
        XtSetArg(args[n], XmNseparatorType, XmSINGLE_LINE);  n++;
        XtSetArg(args[n], XmNorientation, tic_orientation);  n++;
        XtSetArg(args[n], XmNwidth, tic_width);              n++;
        XtSetArg(args[n], XmNheight, tic_height);            n++;
        for (i = 0; i < tic_mark_count; i++) {
            if (tic_array[i] == TRUE && label_count < cc->items_visible) {
                tics = XmCreateLabel(cc->w, cc->data[label_count++], NULL, 0);
            }
            else {
                sprintf(tic_name, "tic_%d", i);
                tics = XmCreateSeparatorGadget(cc->w, tic_name,
                                           args, n);
            }
            XtManageChild(tics);
        }
        free(tic_array);
    }
    XtManageChild(cc->w);

    XtAddCallback(cc->w, XmNvalueChangedCallback, 
                         contscale_func, 
                         (XtPointer) cc);

    XtAddCallback(cc->w, XmNdragCallback, 
                         contscale_func, 
                         (XtPointer) cc);
}

/*
 * Spin child
 */
static char *MakeDeciLabel(int value, int lowest, int decipoint, int increment, char *label)
{
    char Format[20];
    float f = (float) (value - ((value - lowest) % increment));
    if (decipoint >= 0) {
        f = f / pow(10, decipoint);
        sprintf(Format, "%%.%df", decipoint);
    }
    else
        sprintf(Format, "%%f");
    sprintf(label, Format,f);
    return label;    
}


static void contspin_func(Widget w, XtPointer closure, XtPointer call_data)
{
    int tag = (int) closure;
    unsigned long *reason = (unsigned long *) call_data;
    Arg args[10];
    int n,pos;
    float f=0;
    CONTAINER_CHILD *cc = (CONTAINER_CHILD*) tag;        
    static G_POPUP_CHILDINFO ci;
    char *Label;

    if (cc->text_callback_disabled)
        return;
  
    if (cc->type == G_CHILD_SPINBOXTEXT) {
#if (XmVERSION >= 2) 
        n=0;
        XtSetArg(args[0], XmNposition, &pos);                               
        n++;
        XtGetValues(cc->w, args, n);   
        cc->item = pos;
#endif
    }
    else {
#if (XmVERSION > 2 || (XmVERSION == 2 && XmREVISION > 0) )
        n=0;
        XtSetArg(args[0], XmNposition, &pos);                               
        n++;
        XtGetValues(cc->w, args, n);   
        cc->item = pos * cc->select[3] + cc->select[0];
#else  
        int pos0;
        Label = XmTextFieldGetString(cc->w);
        sscanf(Label, "%f", &f);
        XtFree(Label);
        if (f > 0)
            pos0 = (int) (f * pow(10, cc->select[2]) + 0.5);
        else
            pos0 = (int) (f * pow(10, cc->select[2]) - 0.5);
        pos = pos0 / cc->select[3];
        pos *= cc->select[3];
        if (pos < cc->select[0] || pos > cc->select[1] || pos != pos0)
            return;
        cc->item = pos;
#endif  
    }
    if (cc->func) {
        cc2ci(cc,&ci);
        cc->func(&ci);
    }
}


#if (XmVERSION >= 2) 

static void ContainerAddSpinBox(CONTAINER_CHILD *cc)
{
    Arg args[20];
    int n,i;
    Widget frame, label, spin;
    char Name[20];
    XmString list_items[MAX_CONT_ITEMS+1];

    list_items[0] = NULL;
    frame = ContainerCreateFrame(cc);
    n=0;
    if (cc->disabled) {
        XtSetArg(args[n], XmNsensitive, FALSE);
        n++;
    }
    if (cc->horizontal) {
        XtSetArg(args[n], XmNorientation, XmHORIZONTAL);
        n++;
    }
    XtSetArg(args[n], XmNnavigationType, XmEXCLUSIVE_TAB_GROUP);
    n++;
    XtSetArg(args[n], XmNchildType, XmFRAME_WORKAREA_CHILD);
    n++;
    XtSetArg(args[n], XmNresizePolicy, XmRESIZE_GROW); 
    n++;
    spin = XmCreateSpinBox( frame, "spin", args, n );                     

    n = 0;                                                                      
    if (cc->label) {
        XtSetArg(args[n], XmNlabelString, 
            XmStringCreate(cc->label, XmSTRING_DEFAULT_CHARSET)); 
        n++;
        label = XmCreateLabel( spin, "spinlabel", args, n );                     
        XtManageChild(label);    
    }
                                                  
    n = 0;
    if (cc->type == G_CHILD_SPINBOX) {                                                                  
        int low = min(cc->select[0], cc->select[1]);
        int high = max(cc->select[0], cc->select[1]);
        cc->select[0] = low;
        cc->select[1] = high;
        cc->item = max(low, cc->item);
        cc->item = min(high, cc->item);
        XtSetArg(args[n], XmNcolumns, max(4,cc->items_visible)); n++;                                  
        XtSetArg(args[n], XmNminimumValue, cc->select[0]); n++;                             
        XtSetArg(args[n], XmNmaximumValue, cc->select[1]); n++;                             
        if (cc->select[2] >= 0) {
            XtSetArg(args[n], XmNdecimalPoints, cc->select[2]); 
            n++;
        }
        XtSetArg(args[n], XmNincrementValue, cc->select[3]); n++; 
#if (XmVERSION > 2 || (XmVERSION == 2 && XmREVISION > 0) )
        XtSetArg(args[n], XmNpositionType, XmPOSITION_INDEX); n++;                             
#endif
        XtSetArg(args[n], XmNposition, (cc->item - 
            cc->select[0])/cc->select[3]); 
        n++;
                               
        XtSetArg(args[n], XmNspinBoxChildType, XmNUMERIC); n++;  
    }
    else {
        int size = 0;
        for (i=0; i < cc->item_count; i++) {
            list_items[i] = XmStringCreateLtoR(cc->data[i],
                XmSTRING_DEFAULT_CHARSET);
            size = max(size,(int)strlen(cc->data[i]));
        }
        list_items[i] = NULL;

        XtSetArg(args[n], XmNcolumns, max(size,cc->items_visible)); n++;                                  
        XtSetArg(args[n], XmNposition, cc->item); n++; 
        XtSetArg(args[n], XmNvalues, list_items); n++; 
        XtSetArg(args[n], XmNnumValues, cc->item_count); n++; 
        XtSetArg(args[n], XmNspinBoxChildType, XmSTRING); n++;  
    }    
/*    if (cc->type != G_CHILD_SPINBOX) {*/
        XtSetArg(args[n], XmNeditable, FALSE); 
        n++;                              
    
                                                                                                                                                                                                          
    sprintf(Name,"Spin%d",cc->cc_id);
    cc->w = XmCreateTextField( spin, Name, args, n );
    XtManageChild(spin);                                                       
    XtManageChild(cc->w);
    XtAddCallback(cc->w, XmNvalueChangedCallback, 
                         contspin_func, 
                         (XtPointer) cc);
    cc->text_callback_disabled = FALSE;

    for (i=0; list_items[i]; i++)
        XmStringFree(list_items[i]);

}

#else

#define LEFT_ARROW	-1
#define RIGHT_ARROW	1
#define ARROW_REPEAT 200
static int pressing = FALSE;

static void no_arrow_func(Widget w, XtPointer closure, XtPointer call_data)
{
    int tag = (int) closure;
    unsigned long *reason = (unsigned long *) call_data;
    pressing = FALSE;
}

static void ArrowPress(XtPointer tag, int arrow)
{
    Arg args[10];
    int n;
    CONTAINER_CHILD *cc = (CONTAINER_CHILD*) tag;        
    char Label[20];

    if (cc->type == G_CHILD_SPINBOX) {
        if (arrow == LEFT_ARROW) {
            if (cc->item - cc->select[3] < cc->select[0])
                cc->item = cc->select[1];
            else
                cc->item -= cc->select[3];
        }
        else {
            if (cc->item + cc->select[3] > cc->select[1])
                cc->item = cc->select[0];
            else
                cc->item += cc->select[3];
        }
        n = 0;
        XtSetArg(args[n], XmNvalue, 
            MakeDeciLabel(cc->item, cc->select[0], cc->select[2], cc->select[3], Label));  n++;
        XtSetValues(cc->w, args, n);
    }
    else {
        if (arrow == LEFT_ARROW) {
            if (cc->item - 1 < 0)
                cc->item = cc->item_count - 1;
            else
                cc->item--;
        }
        else {
            if (cc->item + 1 > cc->item_count - 1)
                cc->item = 0;
            else
                cc->item++;
        }
        n = 0;
        XtSetArg(args[n], XmNvalue, 
            cc->data[cc->item]);  n++;
        XtSetValues(cc->w, args, n);
    }
}

static void arrow_func(XtPointer tag, XtIntervalId *reason)
{
    CONTAINER_CHILD *cc = (CONTAINER_CHILD*) tag;        
    if (!pressing)
        return;
    ArrowPress(tag, pressing);
    if (pressing)
        XtAppAddTimeOut(XtWidgetToApplicationContext(cc->w),ARROW_REPEAT, 
                        arrow_func, tag);
}


static void arrowL_func(Widget w, XtPointer closure, XtPointer call_data)
{
    pressing = LEFT_ARROW;
    arrow_func(closure, NULL);
}

static void arrowR_func(Widget w, XtPointer closure, XtPointer call_data)
{
    pressing = RIGHT_ARROW;
    arrow_func(closure, NULL);
}

static void TestKeyCode(Widget w, XtPointer tag, XKeyEvent *event,
                           Boolean *doit)
{
    unsigned int key;
    static int alt_key, ctrl_key, shift_key;
    int index = 0;

    if ((event->state & ShiftMask) ||
        (event->state & LockMask))
        index = 1;
    key =  XKeycodeToKeysym(XtDisplay(w), event->keycode, index);
    switch (key) {
        case XK_KP_Left:
        case XK_KP_Up:
        case XK_Left:
        case XK_Up:
            *doit = FALSE;
            ArrowPress(tag, LEFT_ARROW);
            break;
        case XK_KP_Down:
        case XK_KP_Right:
        case XK_Down:
        case XK_Right:
            *doit = FALSE;
            ArrowPress(tag, RIGHT_ARROW);
            break;
    } 
}

static void ContainerAddSpinBox(CONTAINER_CHILD *cc)
{
    Arg args[20];
    int n,i;
    Widget frame, label, spin, arr1, arr2;
    char Name[20],Label[20];

    frame = ContainerCreateFrame(cc);
    n=0;
    if (cc->disabled) {
        XtSetArg(args[n], XmNsensitive, FALSE);
        n++;
    }
    XtSetArg(args[n], XmNorientation, XmHORIZONTAL);
    n++;
    XtSetArg(args[n], XmNnavigationType, XmEXCLUSIVE_TAB_GROUP);
    n++;
    XtSetArg(args[n], XmNchildType, XmFRAME_WORKAREA_CHILD);
    n++;
    XtSetArg(args[n], XmNresizePolicy, XmRESIZE_GROW); 
    n++;
    XtSetArg(args[n], XmNallowOverlap, TRUE); 
    n++;
    spin = XmCreateRowColumn(frame, "container", args, n);

    n = 0;                                                                      
    if (cc->label) {
        XtSetArg(args[n], XmNlabelString, 
            XmStringCreate(cc->label, XmSTRING_DEFAULT_CHARSET)); 
        n++;
        label = XmCreateLabel( spin, "spinlabel", args, n );                     
        XtManageChild(label);    
    }
                                                  
    n = 0;                                                                         
    if (cc->type == G_CHILD_SPINBOX) {
        int size = 0;                                            
        int low = min(cc->select[0], cc->select[1]);
        int high = max(cc->select[0], cc->select[1]);
        cc->select[0] = low;
        cc->select[1] = high;
        size = strlen(MakeDeciLabel(high, cc->select[0], cc->select[2], 1, Label));
        size = max(size,strlen(MakeDeciLabel(low, cc->select[0], cc->select[2], 1, Label)));
        XtSetArg(args[n], XmNcolumns, max(size+1,cc->items_visible)); n++;                                  
        cc->item = min(high, cc->item);
        cc->item = max(low, cc->item);
        XtSetArg(args[n], XmNvalue, 
            MakeDeciLabel(cc->item, cc->select[0], cc->select[2], cc->select[3], Label));  n++;
    }
    else {
        int size = 0;
        for (i=0; i < cc->item_count; i++) 
            size = max(size,strlen(cc->data[i]));
        cc->item = min(cc->item_count-1, cc->item);
        cc->item = max(0, cc->item);
        XtSetArg(args[n], XmNvalue, cc->data[cc->item]); n++;

        XtSetArg(args[n], XmNcolumns, max(size,cc->items_visible)); n++;                                  
    }                
/*    if (cc->type != G_CHILD_SPINBOX) {*/
        XtSetArg(args[n], XmNeditable, FALSE); 
        n++;                              
                                                                                                                                                                                                          
    sprintf(Name,"Spin%d",cc->cc_id);
    cc->w = XmCreateTextField( spin, Name, args, n );
    XtAddEventHandler(cc->w,
            KeyPressMask,
            FALSE,
            (XtEventHandler)TestKeyCode,
            (XtPointer) cc);

    n=0;
    XtSetArg(args[n], XmNarrowDirection, XmARROW_LEFT); n++; 
    sprintf(Name,"ArrL%d",cc->cc_id);
    arr1 = XmCreateArrowButton(spin, Name, args, n );
    XtAddCallback(arr1, XmNarmCallback, 
                       arrowL_func, 
                       (XtPointer) cc);
    XtAddCallback(arr1, XmNdisarmCallback, 
                       no_arrow_func, 
                       (XtPointer) NULL);

    n=0;
    XtSetArg(args[n], XmNarrowDirection, XmARROW_RIGHT); n++; 
    sprintf(Name,"ArrR%d",cc->cc_id);
    arr2 = XmCreateArrowButton(spin, Name, args, n );
    XtAddCallback(arr2, XmNarmCallback, 
                       arrowR_func, 
                       (XtPointer) cc);
    XtAddCallback(arr2, XmNdisarmCallback, 
                       no_arrow_func, 
                       (XtPointer) NULL);

    XtManageChild(arr1);                                                       
    XtManageChild(arr2);                                                       
    XtManageChild(spin);                                                       
    XtManageChild(cc->w);

    XtAddCallback(cc->w, XmNvalueChangedCallback, 
                         contspin_func, 
                         (XtPointer) cc);

    cc->text_callback_disabled = FALSE;
}
#endif


static void pushbutton_func(Widget w, XtPointer closure, XtPointer call_data)
{
    int tag = (int) closure;
    unsigned long *reason = (unsigned long *) call_data;
    CONTAINER_CHILD *cc = (CONTAINER_CHILD*) tag;        
    static G_POPUP_CHILDINFO ci;

    if (cc->func) {
        cc2ci(cc,&ci);
        cc->func(&ci);
    }
}

static void ContainerAddPushButton(CONTAINER_CHILD *cc)
{
    Arg args[20];
    int n;
    Widget frame;
    char Name[20];

    frame = ContainerCreateFrame(cc);
    n=0;
    if (cc->disabled) {
        XtSetArg(args[n], XmNsensitive, FALSE);
        n++;
    }
    XtSetArg(args[n], XmNorientation, XmHORIZONTAL);
    n++;
    XtSetArg(args[n], XmNnavigationType, XmEXCLUSIVE_TAB_GROUP);
    n++;
    XtSetArg(args[n], XmNchildType, XmFRAME_WORKAREA_CHILD);
    n++;
    XtSetArg(args[n], XmNresizePolicy, XmRESIZE_GROW); 
    n++;
    XtSetArg(args[n], XmNallowOverlap, TRUE); 
    n++;
    XtSetArg(args[n], XmNhighlightOnEnter, TRUE);
    n++;
    XtSetArg(args[n], XmNlabelString, 
            XmStringCreate(cc->label, XmSTRING_DEFAULT_CHARSET)); 
    n++;
    sprintf(Name,"Butt%d",cc->cc_id);
    cc->w = XmCreatePushButtonGadget(frame,Name,args,n);

    XtManageChild(cc->w);

    XtAddCallback(cc->w, XmNactivateCallback, 
                         pushbutton_func, 
                         (XtPointer) cc);
}

static void filesel_func(Widget w, XtPointer tag, XtPointer reason)
{
    CONTAINER_CHILD *cc = (CONTAINER_CHILD*) tag;        
    static G_POPUP_CHILDINFO ci;
    char *filename;
    XmFileSelectionBoxCallbackStruct *fcb =
        (XmFileSelectionBoxCallbackStruct *) reason;

    if (cc->text_callback_disabled)
        return;
    XmStringGetLtoR(fcb->value,
        XmSTRING_DEFAULT_CHARSET,
        &filename);
    cc->label = (char*) realloc(cc->label,strlen(filename)+1);
    strcpy(cc->label,filename);
    XtFree(filename);
    cc->item = 0;

    if (cc->func) {
        cc2ci(cc,&ci);
        cc->func(&ci);
    }
}

static void ContainerAddFileDialogBox(CONTAINER_CHILD *cc, int dirflag)
{
    Arg args[20];
    int n;
    Widget frame, button;
    char Name[20];

    frame = ContainerCreateFrame(cc);
    n=0;
    if (cc->disabled) {
        XtSetArg(args[n], XmNsensitive, FALSE);
        n++;
    }
    XtSetArg(args[n], XmNorientation, XmHORIZONTAL);
    n++;
    XtSetArg(args[n], XmNnavigationType, XmEXCLUSIVE_TAB_GROUP);
    n++;
    XtSetArg(args[n], XmNchildType, XmFRAME_WORKAREA_CHILD);
    n++;
    XtSetArg(args[n], XmNresizePolicy, XmRESIZE_GROW); 
    n++;
    XtSetArg(args[n], XmNallowOverlap, TRUE); 
    n++;
    XtSetArg(args[n], XmNhighlightOnEnter, TRUE);
    n++;
    sprintf(Name,"FileSel%d",cc->cc_id);
    cc->w = XmCreateFileSelectionBox(frame,Name,args,n);

    button = XmSelectionBoxGetChild(cc->w,XmDIALOG_HELP_BUTTON);
    XtUnmanageChild(button);
    button = XmSelectionBoxGetChild(cc->w,XmDIALOG_CANCEL_BUTTON);
    XtUnmanageChild(button);
    XtAddCallback(cc->w,XmNokCallback,
                  filesel_func, (XtPointer) cc);
    if (dirflag) {
        button = XtNameToWidget(cc->w,"ItemsListSW");
        XtUnmanageChild(button);
        button = XtNameToWidget(cc->w,"Items");
        XtUnmanageChild(button);
        button = XtNameToWidget(cc->w,"FilterText");
        XtUnmanageChild(button);
        button = XtNameToWidget(cc->w,"FilterLabel");
        XtUnmanageChild(button);
    }
    n = 0;
    if (cc->data && !dirflag) {
        if (strlen(cc->data[0]) > 0) {
            XtSetArg(args[n],XmNdirMask,
                XmStringCreateLtoR(cc->data[0],XmSTRING_DEFAULT_CHARSET));
            n++;
        }
    }
    if (cc->label) {
        XtSetArg(args[n],XmNdirSpec,
            XmStringCreateLtoR(cc->label,XmSTRING_DEFAULT_CHARSET));
        n++;
    }
    if (n)
        XtSetValues (cc->w, args, n);
    XtManageChild(cc->w);
}

void g_SCREEN_popup_add_child(int cont_id, G_POPUP_CHILDINFO *ci)
{
    static int i, cc_id_count;
    CONTAINER_CHILD *cc, *temp;
    int *select;
    char *label;
    CONTAINER_INFO *cont_info;

#if (XmVERSION < 2) 
    if (ci->type == G_CHILD_TAB)
        return;
#endif
    cont_info = cont_info_from_cont_id(cont_id);
    if (cont_info == NULL)
        return;
    ci->cont_id = cont_id;
    ci->win_id = cont_info->win_id;
    cc = (CONTAINER_CHILD*)calloc(sizeof(CONTAINER_CHILD),1);
    if (cont_info->container_child == NULL)
         cont_info->container_child = cc;
    else {
        temp = cont_info->container_child;
        while (temp->next)
            temp = temp->next;
        temp->next = cc;
    }
    ci2cc(ci,cc);         
    cc->cc_id = cc_id_count++;
    cc->p     = cont_info->container;
    cc->store = ci;
    cc->local_win_id = cont_info->local_win_id;
    if ((cc->type == G_CHILD_TEXTBOX || cc->type == G_CHILD_MULTILINETEXT ||
            cc->type == G_CHILD_GETFILENAME || cc->type == G_CHILD_GETDIRNAME) 
            && ci->label) {
        label = (char*)malloc(strlen(ci->label) + 1);
        strcpy(label,ci->label);
        cc->label = label;
    }
    if ((cc->type == G_CHILD_CHECKBOX) && ci->select) {
        select = (int*)malloc(sizeof(int) * ci->item_count);
        for (i=0;i<cc->item_count;i++)
            select[i] = ci->select[i];
        cc->select = select;
    }
    else if (cc->type == G_CHILD_SCALE && ci->select) {
        select = (int*)malloc(sizeof(int) * 3);
        for (i=0;i<3;i++)
            select[i] = ci->select[i];
        cc->select = select;
    }
    else if (cc->type == G_CHILD_SPINBOX && ci->select) {
        select = (int*)malloc(sizeof(int) * 4);
        for (i=0;i<4;i++)
            select[i] = ci->select[i];
        cc->select = select;
    }
    else if (cc->type == G_CHILD_OK) {
        cont_info->okfunc = cc->func;
        cont_info->okdata = cc->userdata;
    }
    else if (cc->type == G_CHILD_CANCEL) {
        cont_info->cancelfunc = cc->func;
        cont_info->canceldata = cc->userdata;
    }
    else
        cc->select = NULL;
    switch (ci->type) {

#if (XmVERSION >= 2) 
        case G_CHILD_TAB:
            if (!cont_info->flag & G_POPUP_TAB)
                break;
            add_panel(cont_info, FALSE, cont_info->tabwindow);
	    XtVaCreateManagedWidget(ci->label, xmPushButtonWidgetClass,
				    cont_info->tabwindow, NULL);
            break;
#endif
        case G_CHILD_PANEL:
            ContainerAddPanel(cc,cont_info);
            break;
        case G_CHILD_LABEL:
            ContainerAddLabel(cc);
            break;
        case G_CHILD_MULTILINETEXT:
            ContainerAddTextBox(cc, TRUE);
            break;
        case G_CHILD_TEXTBOX:
            ContainerAddTextBox(cc, FALSE);
            break;
        case G_CHILD_RADIOBOX:
            ContainerAddRadioBox(cc);
            break;
        case G_CHILD_CHECKBOX:
            ContainerAddRadioBox(cc);
            break;
        case G_CHILD_LISTBOX:
            ContainerAddListBox(cc);
            break;
        case G_CHILD_OPTIONMENU:
            ContainerAddOptionMenu(cc);
            break;
        case G_CHILD_SEPARATOR:
            ContainerAddSeparator(cc);
            break;
        case G_CHILD_SCALE:
            ContainerAddScale(cc);
            break;
        case G_CHILD_SPINBOX:
            ContainerAddSpinBox(cc);
            break;
        case G_CHILD_SPINBOXTEXT:
            ContainerAddSpinBox(cc);
            break;
        case G_CHILD_PUSHBUTTON:
            ContainerAddPushButton(cc);
            break;
        case G_CHILD_GETFILENAME:
            ContainerAddFileDialogBox(cc, FALSE);
            break;
        case G_CHILD_GETDIRNAME:
            ContainerAddFileDialogBox(cc, TRUE);
            break;
    }
}
  

void g_SCREEN_popup_container_close(int cont_id)
{
    CONTAINER_INFO *cont_info;

    cont_info = cont_info_from_cont_id(cont_id);
    if (cont_info == NULL)
        return;
/*    cont_info->cont_select_status = SELECT_STATUS_CANCEL;*/
    XtUnmanageChild(cont_info->contpopup);
/*
    if (!(cont_info->flag & G_POPUP_KEEP))
        kill_container_func(cont_info->contpopup, cont_info->cont_id);
        */
}

int g_SCREEN_popup_container_open(int win_id, char *title, int flag)
{
    Arg args[20];
    int n;
    int parent_id = GET_LOCAL_ID(win_id);
    Widget button;
    CONTAINER_INFO *cont_info;
 
    cont_info = add_container(win_id, parent_id, flag);
    n=0;
    XtSetArg(args[n], XmNdialogTitle, 
                XmStringCreate(title, XmSTRING_DEFAULT_CHARSET)); 
    n++;
    if (flag & G_POPUP_WAIT) {
        XtSetArg(args[n], XmNdialogStyle, XmDIALOG_FULL_APPLICATION_MODAL); 
        n++;
    }
        XtSetArg(args[n],XmNautoUnmanage,FALSE);
        n++;
    cont_info->contpopup = XmCreateMessageDialog(xwini[parent_id].form_widget, 
                                      "ContainerPopUp", args, n);

    XtAddEventHandler(cont_info->contpopup,
                      StructureNotifyMask,
                      FALSE,
                      StructureNotifyProc2,
                      (XtPointer)parent_id);
    XtAddCallback(cont_info->contpopup, 
                  XmNdestroyCallback, 
                  contpopup_destroyfunc, 
                 (XtPointer) cont_info);

    button = XmMessageBoxGetChild(cont_info->contpopup,XmDIALOG_OK_BUTTON);
    XtAddCallback(button, 
                  XmNactivateCallback, 
                  contpopup_okfunc, 
                 (XtPointer) cont_info);

    button = XmMessageBoxGetChild(cont_info->contpopup,XmDIALOG_CANCEL_BUTTON);
    if (flag & G_POPUP_SINGLEBUTTON)
        XtUnmanageChild(button);
    else
        XtAddCallback(button, 
                  XmNactivateCallback, 
                  contpopup_unmanagefunc, 
                 (XtPointer) cont_info);

    
    XtAddCallback(cont_info->contpopup, 
                  XmNunmapCallback, 
                  contpopup_cancelfunc, 
                 (XtPointer) cont_info);
    button = XmMessageBoxGetChild(cont_info->contpopup,XmDIALOG_HELP_BUTTON);
    XtUnmanageChild(button);

    if (flag & G_POPUP_SCROLL) {
        n=0;
        XtSetArg(args[n], XmNscrollingPolicy, XmAUTOMATIC); 
        n++;
        cont_info->scrollwindow = XmCreateScrolledWindow(cont_info->contpopup, 
                                   "container_scroll", args, n);
        XtManageChild(cont_info->scrollwindow);
    }
    else
        cont_info->scrollwindow = cont_info->contpopup;
    cont_info->tabwindow =  NULL;
#if (XmVERSION >= 2) 
    if (flag & G_POPUP_TAB) {

        n=0;
        XtSetArg(args[n], XmNbindingType, XmNONE);
        n++;
        XtSetArg(args[n], XmNbindingWidth, 0);
        n++;
        /*
        XtSetArg(args[n], XmNorientation, XmVERTICAL);
        n++;
        XtSetArg(args[n], XmNbackPagePlacement, XmTOP_RIGHT);
        n++;
        */
        cont_info->tabwindow = XmCreateNotebook(cont_info->contpopup, 
                                       "container_notebook", args, n);
        XtManageChild(cont_info->tabwindow);
    }
    else
#endif
        add_panel(cont_info, FALSE, cont_info->scrollwindow);

/*
    n=0;
    XtSetArg(args[n], XmNallowOverlap, TRUE); 
    n++;
    XtSetArg(args[n], XmNmarginHeight, 0);
    n++;
    XtSetArg(args[n], XmNspacing, 0);
    n++;
    cont_info->container = 
        XmCreateRowColumn(cont_info->scrollwindow, "container", args, n);
    XtManageChild(cont_info->container);
*/
    return cont_info->cont_id;
}

void g_SCREEN_popup_set_buttonlabel(int cont_id, char *label, int whichbutton)
{
    Arg args[2];
    Widget button;
    CONTAINER_INFO *cont_info;
    unsigned char child;
    
    if (whichbutton == G_POPUP_BUTTON_CANCEL)
        child = XmDIALOG_CANCEL_BUTTON;
    else
        child = XmDIALOG_OK_BUTTON;
    cont_info = cont_info_from_cont_id(cont_id);
    if (cont_info == NULL)
        return;
    button = XmMessageBoxGetChild(cont_info->contpopup, child);
    XtSetArg(args[0], XmNlabelString, 
             XmStringCreate(label, XmSTRING_DEFAULT_CHARSET)); 
    XtSetValues(button, args, 1);

}

static int wait_status(int cont_id)
{
    CONTAINER_INFO *cont_info;

    cont_info = cont_info_from_cont_id(cont_id);
    if (cont_info == NULL)
        return SELECT_STATUS_CANCEL;
    return cont_info->cont_select_status;
}

int g_SCREEN_popup_container_show(int cont_id)
{
    static XEvent event;
    Arg args[10];
    int i,n,ret_value,status;
    CONTAINER_CHILD *cc, *temp;    
    CONTAINER_INFO *cont_info;

    cont_info = cont_info_from_cont_id(cont_id);
    if (cont_info == NULL)
        return FALSE;
    cont_info->cont_select_status = SELECT_STATUS_WAIT;
    n = PositionAtCursor(args);
    XtSetValues(cont_info->contpopup, args, n);
    XtManageChild(cont_info->contpopup);
    if (XmGetFocusWidget(cont_info->contpopup) == NULL)
        XmProcessTraversal(cont_info->contpopup,XmTRAVERSE_NEXT_TAB_GROUP);
    XmProcessTraversal(cont_info->contpopup,XmTRAVERSE_NEXT_TAB_GROUP);
    if (cont_info->flag & G_POPUP_SCROLL) {
        Dimension defw = 300, defh = 500;
        Dimension h,w,hs,ws,sp,mh,mw;
        Widget hsb,wsb;

        n=0;
        XtSetArg(args[n], XmNwidth, &w); 
        n++;
        XtSetArg(args[n], XmNheight, &h); 
        n++;
        XtGetValues(cont_info->container, args, n);

        n=0;
        XtSetArg(args[n], XmNspacing, &sp); 
        n++;
        XtSetArg(args[n], XmNscrolledWindowMarginHeight, &mh); 
        n++;
        XtSetArg(args[n], XmNscrolledWindowMarginWidth, &mw); 
        n++;
        XtGetValues(cont_info->scrollwindow, args, n);

        hsb = XtNameToWidget(cont_info->scrollwindow, "HorScrollBar");
        wsb = XtNameToWidget(cont_info->scrollwindow, "VertScrollBar");

        n=0;
        XtSetArg(args[n], XmNheight, &hs); 
        n++;
        XtGetValues(hsb, args, n);

        n=0;
        XtSetArg(args[n], XmNwidth, &ws); 
        n++;
        XtGetValues(wsb, args, n);

        w = w+sp+2*mw;
        h = h+sp+2*mh;
        if (h > defh)
            w += ws+2*sp;
        if (w > defw)
            h += hs+2*sp;

        w = min(w, defw);
        h = min(h, defh);

        n=0;
        XtSetArg(args[n], XmNwidth, w); 
        n++;
        XtSetArg(args[n], XmNheight, h); 
        n++;
        XtSetValues(cont_info->scrollwindow, args, n);
    }
    INTERNAL_dispatch_message();
    if (!(cont_info->flag & G_POPUP_WAIT))
        return FALSE;
/*    while (!INTERNAL_is_event() && */
    while ((status=wait_status(cont_id)) == SELECT_STATUS_WAIT) {
        XtAppNextEvent(INTERNAL_app_context,&event);
        XtDispatchEvent(&event);
    }
    if (status == SELECT_STATUS_OK) {
        ret_value = TRUE;
        cc = cont_info->container_child;
        while (cc) {
            if ((cc->type == G_CHILD_CHECKBOX) && cc->select) {
                for (i=0;i<cc->item_count;i++)
                    cc->store->select[i] = cc->select[i];
            }
            if ((cc->type == G_CHILD_TEXTBOX          || 
                    cc->type == G_CHILD_MULTILINETEXT ||
                    cc->type == G_CHILD_GETFILENAME   ||
                    cc->type == G_CHILD_GETDIRNAME) && cc->label) {
                /*
                 * Beware: No more than BUTTONQMAX labels in one container
                 */
                INTERNAL_add_label(cc->label); 
                cc2ci(cc, cc->store);
                cc->store->label = INTERNAL_get_label();
            }
            else
                cc2ci(cc, cc->store);
            cc = cc->next;
        }     

    }
    else
        ret_value = FALSE;

    if (!(cont_info->flag & G_POPUP_KEEP))
        kill_container_func(cont_info->contpopup, cont_info->cont_id);
    return ret_value;
}


void g_SCREEN_popup_enable_item(int cont_id, int item_id, int enable)
{
    CONTAINER_CHILD *cc;
    int i;
    CONTAINER_INFO *cont_info;

    cont_info = cont_info_from_cont_id(cont_id);
    if (cont_info == NULL)
        return;
    cc = cont_info->container_child;
    while (cc) {
        if (cc->id == item_id) {
            if (cc->f) 
                XtSetSensitive(cc->f, enable);
            XtSetSensitive(cc->w, enable);
            cc->disabled = !enable;
            return;
        }
        cc = cc->next;
    }
}

void g_SCREEN_popup_set_selection(int cont_id, int item_id, int position) 
{
    Arg args[2];
    int n;
    CONTAINER_CHILD *cc;
    CONTAINER_INFO *cont_info;

    cont_info = cont_info_from_cont_id(cont_id);
    if (cont_info == NULL)
        return;
    cc = cont_info->container_child;
    while (cc) {
        if (cc->id == item_id) {
            if (position >= cc->item_count && 
                  cc->type != G_CHILD_SCALE &&
                  cc->type != G_CHILD_SPINBOX)
                return;
            switch (cc->type) {
            case G_CHILD_RADIOBOX:
                for (n=0;n< cc->item_count;n++)
                    XmToggleButtonSetState(cc->c[cc->item], False, False);
                XmToggleButtonSetState(cc->c[position], True, False);
                cc->item = position;
                return;
            case G_CHILD_OPTIONMENU:
                XtSetArg(args[0], XmNmenuHistory, cc->c[position]);
                XtSetValues(cc->w, args, 1);
                cc->item = position;
                return;
            case G_CHILD_LISTBOX:
                XmListSelectPos(cc->w, position + 1, False);
                cc->item = position;
                return;
            case G_CHILD_SCALE:
                XmScaleSetValue(cc->w, position);
                cc->item = position;
                return;
            case G_CHILD_SPINBOX:
                {
                char Label[20];
                cc->text_callback_disabled = TRUE;
                cc->item = position;
                n=0;
                XtSetArg(args[n], XmNvalue, 
                    MakeDeciLabel(cc->item, cc->select[0], cc->select[2], cc->select[3], Label)); 
                n++;
#if (XmVERSION >= 2) 
                XtSetArg(args[n], XmNposition, 
                    (position - min(cc->select[0], cc->select[1]))/cc->select[3]);                               
                n++;
#endif
                XtSetValues(cc->w, args, n);
                cc->text_callback_disabled = FALSE;
                }
                return;
            case G_CHILD_SPINBOXTEXT:
                cc->text_callback_disabled = TRUE;
                cc->item = position;
                cc->item = min(cc->item_count-1, cc->item);
                cc->item = max(0, cc->item);
                n=0;
                XtSetArg(args[n], XmNvalue, 
                    cc->data[cc->item]); n++;
#if (XmVERSION >= 2) 
                XtSetArg(args[n], XmNposition, cc->item); n++;
#endif
                XtSetValues(cc->w, args, n);
                cc->text_callback_disabled = FALSE;
                return;
            }
        }
        cc = cc->next;
    }
}

void g_SCREEN_popup_set_checkmark(int cont_id, int item_id, int position, int toggle_on) 
{
    Arg args[2];
    int n;
    CONTAINER_CHILD *cc;
    CONTAINER_INFO *cont_info;

    cont_info = cont_info_from_cont_id(cont_id);
    if (cont_info == NULL)
        return;
    cc = cont_info->container_child;
    while (cc) {
        if (cc->id == item_id) {
            if (position >= cc->item_count)
                return;
            switch (cc->type) {
            case G_CHILD_CHECKBOX:
                XmToggleButtonSetState(cc->c[position], toggle_on, False);
                cc->select[position] = toggle_on;
                return;
            }
        }
        cc = cc->next;
    }
}


void g_SCREEN_popup_set_label(int cont_id, int item_id, char *label) 
{
    Arg args[2];
    int n;
    CONTAINER_CHILD *cc;
    CONTAINER_INFO *cont_info;

    cont_info = cont_info_from_cont_id(cont_id);
    if (cont_info == NULL)
        return;
    cc = cont_info->container_child;
    while (cc) {
        if (cc->id == item_id) {
            switch (cc->type) {
            case G_CHILD_MULTILINETEXT:
            case G_CHILD_TEXTBOX:
                cc->text_callback_disabled = TRUE;
                cc->label = (char*) realloc(cc->label,strlen(label)+1);
                strcpy(cc->label,label);
                XmTextSetString(cc->w, label);
                cc->text_callback_disabled = FALSE;
                return;
            case G_CHILD_LABEL:
            case G_CHILD_OPTIONMENU:
                XtSetArg(args[0], XmNlabelString, 
                    XmStringCreate(label, XmSTRING_DEFAULT_CHARSET)); 
                XtSetValues(cc->w, args, 1);
                return;
            }
        }
        cc = cc->next;
    }
}

char  *g_SCREEN_popup_get_label(int cont_id, int item_id) 
{
    Arg args[2];
    int n;
    CONTAINER_CHILD *cc;
    CONTAINER_INFO *cont_info;
    char *text;

    cont_info = cont_info_from_cont_id(cont_id);
    if (cont_info == NULL)
        return NULL;
    cc = cont_info->container_child;
    while (cc) {
        if (cc->id == item_id) {
            switch (cc->type) {
            case G_CHILD_MULTILINETEXT:
            case G_CHILD_TEXTBOX:
                text = XmTextGetString(cc->w);
                cc->label = (char*) realloc(cc->label,strlen(text)+1);
                strcpy(cc->label,text);
                XtFree(text);
                return cc->label;
            case G_CHILD_LABEL:
                return cc->label;
            }
        }
        cc = cc->next;
    }
    return NULL;
}


void g_SCREEN_popup_set_title(int cont_id, int item_id, char *title) 
{
    Arg args[2];
    int n;
    CONTAINER_CHILD *cc;
    CONTAINER_INFO *cont_info;

    cont_info = cont_info_from_cont_id(cont_id);
    if (cont_info == NULL)
        return;
    cc = cont_info->container_child;
    while (cc) {
        if (cc->id == item_id) {
            if (cc->f) { 
                XtSetArg(args[0], XmNlabelString, 
                    XmStringCreate(title, XmSTRING_DEFAULT_CHARSET)); 
                XtSetValues(cc->t, args, 1);
                return;
            }
        }
        cc = cc->next;
    }
}


void g_SCREEN_popup_set_focus(int cont_id, int item_id)
{
    CONTAINER_CHILD *cc;
    int count = 0;
    CONTAINER_INFO *cont_info;

    cont_info = cont_info_from_cont_id(cont_id);
    if (cont_info == NULL)
        return;
    if (XmGetFocusWidget(cont_info->contpopup) == NULL)
        count++;
    cc = cont_info->container_child;
    while (cc) {
        switch(cc->type) {
        case G_CHILD_MULTILINETEXT:
        case G_CHILD_TEXTBOX:
        case G_CHILD_RADIOBOX:
        case G_CHILD_CHECKBOX:
        case G_CHILD_LISTBOX:
        case G_CHILD_SCALE:
        case G_CHILD_SPINBOX:
        case G_CHILD_SPINBOXTEXT:
        case G_CHILD_OPTIONMENU:
        case G_CHILD_PUSHBUTTON:
        case G_CHILD_GETFILENAME:
        case G_CHILD_GETDIRNAME:
            count++;
            break;
        }
        if (cc->id == item_id &&
            cc->type != G_CHILD_SEPARATOR &&
            cc->type != G_CHILD_LABEL)
               break;
        cc = cc->next;
    }
    if (cc)
        while (count--)
            XmProcessTraversal(cont_info->contpopup,XmTRAVERSE_NEXT_TAB_GROUP);
}

void g_SCREEN_popup_append_item(int cont_id, int item_id, char *label) 
{
    CONTAINER_CHILD *cc;
    CONTAINER_INFO *cont_info;
    XmString list_item;

    cont_info = cont_info_from_cont_id(cont_id);
    if (cont_info == NULL)
        return;
    cc = cont_info->container_child;
    while (cc) {
        if (cc->id == item_id) {
            switch (cc->type) {
            case G_CHILD_LISTBOX:
                cc->text_callback_disabled = TRUE;
                list_item = XmStringCreateLtoR(label,XmSTRING_DEFAULT_CHARSET);
                XmListAddItem (cc->w, list_item, 0);
                cc->item_count++;
                XmStringFree(list_item);
                cc->text_callback_disabled = FALSE;
                return;
            }
        }
        cc = cc->next;
    }
}

void g_SCREEN_popup_remove_item(int cont_id, int item_id, int pos) 
{
    CONTAINER_CHILD *cc;
    CONTAINER_INFO *cont_info;

    cont_info = cont_info_from_cont_id(cont_id);
    if (cont_info == NULL)
        return;
    cc = cont_info->container_child;
    while (cc) {
        if (cc->id == item_id) {
            switch (cc->type) {
            case G_CHILD_LISTBOX:
                cc->text_callback_disabled = TRUE;
                if (pos < 0) {
                    XmListDeleteAllItems(cc->w);
                    cc->item_count = 0;
                }
                else {
                    if (cc->item_count>0) {
                        XmListDeletePos(cc->w, pos+1);
                        cc->item_count--;
                    }
                }
                cc->text_callback_disabled = FALSE;
                return;
            }
        }
        cc = cc->next;
    }
}

void g_SCREEN_popup_replace_item(int cont_id, int item_id, int pos, char *label) 
{
    CONTAINER_CHILD *cc;
    CONTAINER_INFO *cont_info;
    XmString list_item;

    if (pos < 0)
        return;
    cont_info = cont_info_from_cont_id(cont_id);
    if (cont_info == NULL)
        return;
    cc = cont_info->container_child;
    while (cc) {
        if (cc->id == item_id) {
            switch (cc->type) {
            case G_CHILD_LISTBOX:
                cc->text_callback_disabled = TRUE;
                list_item = XmStringCreateLtoR(label,XmSTRING_DEFAULT_CHARSET);
                XmListReplaceItemsPos(cc->w, &list_item, 1, pos+1);
                XmStringFree(list_item);
                cc->text_callback_disabled = FALSE;
                return;
            }
        }
        cc = cc->next;
    }
}

/* General popup */

void INTERNAL_reset_popup(int id)
{
    init_message_box(id);
    init_message2_box(id);
    init_message3_box(id);
    init_prompt_box(id);
    init_file_box(id);
    init_dir_box(id);
    init_saveas_box(id);
    init_list_box(id);
    init_radio_box(id);
    init_check_box(id);
    init_container(id);
}

void INTERNAL_init_popup(void)
{
    int i;
    
    for (i=0;i<G_MAXWINDOW;i++) 
        INTERNAL_reset_popup(i);
}

