/********************************************************************/
/*                             psnd_popmouse.c                      */
/*                                                                  */
/* Mouse-functions popup selection box                              */
/* 1998, Albert van Kuik                                            */
/********************************************************************/
#include <stdio.h>
#include <stdlib.h>
#include <ctype.h>
#include <string.h>
#include <stdarg.h>
#include <assert.h>
#include <math.h>
#include "genplot.h"
#include "psnd.h"



static void update_mousebar_buttons(MBLOCK *mblk, int item)
{
    int cursor;

    cursor = g_get_cursortype(mblk->info->win_id);
    if (cursor != G_CURSOR_CROSSHAIR)
        if ((item != MOUSE_DISPLAY) &&
            (item != MOUSE_CONTOURLEVEL) &&
            (item != MOUSE_P0) && 
            (item != MOUSE_P1)) {
              g_set_cursortype(mblk->info->win_id, G_CURSOR_CROSSHAIR);
    }

    mblk->info->mouse_mode1 = item;

    switch (item) {
        case MOUSE_3D:
        case MOUSE_WATWA:
        case MOUSE_WATERFIT:
        case MOUSE_BASELINE:
        case MOUSE_LINEFIT:
        case MOUSE_TRANSITION:
            /*
             * exact mode is defined by hint
             */
            g_menu_set_toggle(mblk->info->mousebar_id, 
                          ID_MOUSE_SELECT, TRUE);
            return;

        case MOUSE_ZOOM:
            g_menu_set_toggle(mblk->info->mousebar_id, 
                          ID_MOUSE_ZOOMXY, TRUE);
            break;
        case MOUSE_ZOOMX:
            g_menu_set_toggle(mblk->info->mousebar_id, 
                          ID_MOUSE_ZOOMX, TRUE);
            break;
        case MOUSE_ZOOMY:
            g_menu_set_toggle(mblk->info->mousebar_id, 
                          ID_MOUSE_ZOOMY, TRUE);
            break;
        case MOUSE_MEASURE:
             g_menu_set_toggle(mblk->info->mousebar_id, 
                            ID_MOUSE_POS, TRUE);
            break;
        case MOUSE_DISTANCE:
            g_menu_set_toggle(mblk->info->mousebar_id, 
                            ID_MOUSE_DISTANCE, TRUE);
            break;
        case MOUSE_PEAKPICK:
            g_menu_set_toggle(mblk->info->mousebar_id, 
                            ID_MOUSE_PEAKPICK, TRUE);
            break;
        case MOUSE_INTEGRATION:
            g_menu_set_toggle(mblk->info->mousebar_id, 
                            ID_MOUSE_INTEGRATE, TRUE);
            break;
        case MOUSE_NOISE:
            g_menu_set_toggle(mblk->info->mousebar_id, 
                            ID_MOUSE_NOISE, TRUE);
            break;
        case MOUSE_CALIBRATE:
            g_menu_set_toggle(mblk->info->mousebar_id, 
                            ID_MOUSE_CALIBRATE, TRUE);
            break;
        case MOUSE_ROWCOLUMN:
            g_menu_set_toggle(mblk->info->mousebar_id, 
                            ID_MOUSE_ROWCOL, TRUE);
            break;
        case MOUSE_ADDROWCOLUMN:
            g_menu_set_toggle(mblk->info->mousebar_id, 
                            ID_MOUSE_SUM, TRUE);
            break;
        case MOUSE_PLANEROWCOLUMN:
            g_menu_set_toggle(mblk->info->mousebar_id, 
                            ID_MOUSE_ROWCOL_PLANE, TRUE);
            break;
        case MOUSE_CONTOURLEVEL:
            g_set_cursortype(mblk->info->win_id, G_CURSOR_UPDOWN);
            g_menu_set_toggle(mblk->info->mousebar_id, 
                            ID_MOUSE_SCALE, TRUE);
            break;
        case MOUSE_DISPLAY:
            g_set_cursortype(mblk->info->win_id, G_CURSOR_UPDOWN);
            g_menu_set_toggle(mblk->info->mousebar_id, 
                            ID_MOUSE_SCALE, TRUE);
            break;
        case MOUSE_I0:
            g_menu_set_toggle(mblk->info->mousebar_id, 
                            ID_MOUSE_PHASE_I0, TRUE);
            break;
        case MOUSE_BUFFER1:
            g_menu_set_toggle(mblk->info->mousebar_id, 
                            ID_MOUSE_REGION, TRUE);
            break;
        case MOUSE_BUFFER2:
                g_menu_set_toggle(mblk->info->mousebar_id, 
                            ID_MOUSE_SPLINE, TRUE);
            break;
        case MOUSE_P0:
            g_set_cursortype(mblk->info->win_id, G_CURSOR_UPDOWN);
            g_menu_set_toggle(mblk->info->mousebar_id, 
                          ID_MOUSE_PHASE_P0, TRUE);
            break;
        case MOUSE_P1:
            g_set_cursortype(mblk->info->win_id, G_CURSOR_UPDOWN);
            g_menu_set_toggle(mblk->info->mousebar_id, 
                          ID_MOUSE_PHASE_P1, TRUE);
            break;
    }

}


void psnd_process_mousebar_messages(MBLOCK *mblk, int id_mouse)
{
    int hint = mblk->info->mouse_select_hint;

    switch (id_mouse) {
        case ID_MOUSE_POS:
            psnd_set_cursormode(mblk, 0, MOUSE_MEASURE);
            break;
        case ID_MOUSE_SELECT: 
            switch (hint) {
                case MOUSE_LINEFIT: 
                case MOUSE_TRANSITION:
                case MOUSE_3D:
                case MOUSE_WATWA:
                case MOUSE_WATERFIT:
                case MOUSE_BASELINE:

                    psnd_set_cursormode(mblk, 0, hint);
                    break;
            }
            break;
        case ID_MOUSE_ZOOMXY:
            psnd_set_cursormode(mblk, 0, MOUSE_ZOOM);
            break;
        case ID_MOUSE_ZOOMX: 
            psnd_set_cursormode(mblk, 0, MOUSE_ZOOMX);
            break;
        case ID_MOUSE_ZOOMY:
            psnd_set_cursormode(mblk, 0, MOUSE_ZOOMY);
            break;
        case ID_MOUSE_PHASE_P0:
            psnd_set_cursormode(mblk, 0, MOUSE_P0);
            break;
        case ID_MOUSE_PHASE_P1:
            psnd_set_cursormode(mblk, 0, MOUSE_P1);
            break;
        case ID_MOUSE_PHASE_I0:
            psnd_set_cursormode(mblk, 0, MOUSE_I0);
            break;
        case ID_MOUSE_CALIBRATE:
            psnd_set_cursormode(mblk, 0, MOUSE_CALIBRATE);
            break;
        case ID_MOUSE_INTEGRATE:
            psnd_set_cursormode(mblk, 0, MOUSE_INTEGRATION);
            break;
        case ID_MOUSE_PEAKPICK:
            psnd_set_cursormode(mblk, 0, MOUSE_PEAKPICK);
            break;
        case ID_MOUSE_DISTANCE:
            psnd_set_cursormode(mblk, 0, MOUSE_DISTANCE);
            break;
        case ID_MOUSE_SCALE:
            if (mblk->info->plot_mode == PLOT_1D)
                psnd_set_cursormode(mblk, 0, MOUSE_DISPLAY);
            else
                psnd_set_cursormode(mblk, 0, MOUSE_CONTOURLEVEL);
            break;
        case ID_MOUSE_NOISE: 
            psnd_set_cursormode(mblk, 0, MOUSE_NOISE);
            break;
        case ID_MOUSE_ROWCOL:
            psnd_set_cursormode(mblk, 0, MOUSE_ROWCOLUMN);
            break;
        case ID_MOUSE_SUM: 
            psnd_set_cursormode(mblk, 0, MOUSE_ADDROWCOLUMN);
            break;
        case ID_MOUSE_ROWCOL_PLANE:
            psnd_set_cursormode(mblk, 0, MOUSE_PLANEROWCOLUMN);
            break;
        case ID_MOUSE_REGION:
            psnd_set_cursormode(mblk, 0, MOUSE_BUFFER1);
            break;
        case ID_MOUSE_SPLINE:
            psnd_set_cursormode(mblk, 0, MOUSE_BUFFER2);
            break;
    }
}


void psnd_set_cursormode(MBLOCK *mblk, int hint, int item)
{
    int i;
    POPUP_INFO *popinf = mblk->popinf + POP_MOUSE;
    int cont_id = popinf->cont_id;
    
    /*
     * Reset to default button
     */
    if (hint <= 0 && item == 0 ) {
        if (hint == 0)
            mblk->info->mouse_select_hint = 0;
        item = MOUSE_MEASURE;
    }

    if (hint>0)
        mblk->info->mouse_select_hint = hint;
    if (item)
        update_mousebar_buttons(mblk, item);

}

