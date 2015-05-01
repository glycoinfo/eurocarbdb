/************************************************************************/
/*                               genmscr.c                             */
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

/***********************************************************************/
/*   SCROLLBARS
*/

void INTERNAL_scrollbar_proc(Widget w, XtPointer client_data, XtPointer call_data)
{
    int id, ok=FALSE, callback_id = (int) client_data;
    XmScrollBarCallbackStruct *cb = (XmScrollBarCallbackStruct*)call_data;
    for (id=0;id<G_MAXWINDOW;id++)
        if (xwini[id].h_scrollbar_widget ==w || xwini[id].v_scrollbar_widget == w)
            break;

    if (id == G_MAXWINDOW)
        return;

    switch (callback_id) {

        case ID_V_INCREMENT :
        case ID_V_DRAG :
        case ID_V_DECREMENT :
        case ID_V_PAGEINCREMENT :
        case ID_V_PAGEDECREMENT :
            ok = INTERNAL_UpdateScrollBarsV(id, SCROLL_GETVAL, 0);
            break;

        case ID_H_INCREMENT :
        case ID_H_DRAG :
        case ID_H_DECREMENT :
        case ID_H_PAGEINCREMENT :
        case ID_H_PAGEDECREMENT :
            ok = INTERNAL_UpdateScrollBarsH(id, SCROLL_GETVAL, 0);
            break;
    }
    if (ok)
        INTERNAL_update_window(G_SCREEN,id);

}

int INTERNAL_ScrollBarsPresent(int id)
{
    return (!(xwini[id].scroll_widget == NULL));
}

int INTERNAL_GetScrollPosH(int id)
{
    int pos, size, inc, page_inc;
    XmScrollBarGetValues(xwini[id].h_scrollbar_widget,
                         &pos,
                         &size,
                         &inc,
                         &page_inc);
    return pos;
}

int INTERNAL_GetScrollPosV(int id)
{
    int pos, size, inc, page_inc;
    XmScrollBarGetValues(xwini[id].v_scrollbar_widget,
                         &pos,
                         &size,
                         &inc,
                         &page_inc);
    return pos;
}

void INTERNAL_SetScrollPosH(int id, int pos, int size, int notify)
{
    notify=FALSE;
    XmScrollBarSetValues(xwini[id].h_scrollbar_widget,
                         max(0,pos),
                         size,
                         SMAX/100,
                         SMAX/10,
                         notify);
}

void INTERNAL_SetScrollPosV(int id, int pos, int size, int notify)
{
    notify=FALSE;
    XmScrollBarSetValues(xwini[id].v_scrollbar_widget,
                         max(0,pos),
/* --- size_v -1 ???? --- */                         
                         size,
                         SMAX/100,
                         SMAX/10,
                         notify);
}

/*
 * returns 0 for fixed-size slider
 */
int INTERNAL_SetScrollSlider(int slider)
{
    return slider;
}
