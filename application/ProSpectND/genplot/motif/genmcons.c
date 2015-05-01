/************************************************************************/
/*                               genmcons.c                             */
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

/************************************************************************/
/* CONSOLE STUFF
*/



void INTERNAL_init_console(int local_id)
{
    xwini[local_id].console_lines_count = 0;
    xwini[local_id].console_buf = (char*) malloc (sizeof(char) * CONS_BUF_SIZE);
}

void INTERNAL_exit_console(int local_id)
{
    if (xwini[local_id].console_widget == NULL)
        return;
    if (xwini[local_id].console_buf)
        free(xwini[local_id].console_buf);
    xwini[local_id].console_buf = NULL;
    xwini[local_id].console_lines_count = 0;
}


static void write_console(int local_id, char *s)
{
    XmTextPosition position;
    char *p;

    if (xwini[local_id].console_widget == NULL)
        return;
    XmTextDisableRedisplay(xwini[local_id].console_widget);
    for (p=s;*p && ((p=strchr(p,'\n')) != NULL);p++)
        xwini[local_id].console_lines_count++;
    while (xwini[local_id].console_lines_count  > MAX_CONSOLE_LINES) {
        if (XmTextFindString(xwini[local_id].console_widget,  0, "\n", XmTEXT_FORWARD, &position))
            XmTextReplace(xwini[local_id].console_widget, 0, position + 1, "");
        xwini[local_id].console_lines_count--;
    }
    position = XmTextGetLastPosition(xwini[local_id].console_widget);
    XmTextInsert(xwini[local_id].console_widget, position, s);
    XmTextSetCursorPosition(xwini[local_id].console_widget, position);
    XmTextShowPosition(xwini[local_id].console_widget, position);
    XmTextEnableRedisplay(xwini[local_id].console_widget);
}

int g_SCREEN_console_printf(int win_id, const char *format, ...)
{
    va_list argp;
    int ret_val;
    int id, device;

    id = GET_LOCAL_ID(win_id);
    device = GET_DEVICE(win_id);
    if (id < 0 || id >= G_MAXWINDOW)
        return 0;
    if (device != G_SCREEN)
        return 0;
    if (xwini[id].console_widget == NULL)
        return 0;
    va_start(argp, format);
    ret_val = vsprintf(xwini[id].console_buf, format, argp);
    va_end(argp);
    write_console(id, xwini[id].console_buf);
    return ret_val;
}


