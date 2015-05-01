/********************************************************************/
/*                             psnd_undozoom.c                      */
/*                                                                  */
/* Keep a history of records read by the program                    */
/* Undo/redo past zoom settings                                     */
/*                                                                  */
/* 1998, Albert van Kuik                                            */
/********************************************************************/
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "genplot.h"
#include "psnd.h"



static int is_undo(DBLOCK *dat)
{
    CROPUNDOINFO *cui;

    cui = &(dat->crop[dat->c_undotail]);
    return (!(cui->z_undohead == cui->z_undotail));
}

static int is_redo(DBLOCK *dat)
{
    CROPUNDOINFO *cui;

    cui = &(dat->crop[dat->c_undotail]);
    return (!(cui->z_redo == cui->z_undotail));
}

/*
 * Init the whole crop-and-undo for this data block
 */
void psnd_init_zoomundo(DBLOCK *dat)
{
    CROPUNDOINFO *cui;

    dat->c_undotail = 0;
    cui = &(dat->crop[dat->c_undotail]);
    cui->z_lock = cui->z_redo = cui->z_undohead = cui->z_undotail = 0;
}

/*
 * At the moment we have two modes, that have their own zoom-undo queue
 * mode = 0 uncropped
 * mode = 1, cropped
 */
void psnd_set_cropmode(int mode, DBLOCK *dat)
{
    CROPUNDOINFO *cui;

    if (mode == 0)
        dat->c_undotail = 0;
    else {
        dat->c_undotail = 1;
        cui = &(dat->crop[dat->c_undotail]);
        cui->z_lock = cui->z_redo = cui->z_undohead = cui->z_undotail = 0;
    }
}

/*
 * push new zoom settings on the stack
 */
void psnd_push_zoomundo(int vp_id, float x1, float y1, 
                         float x2, float y2, DBLOCK *dat)
{
    ZOOMUNDOINFO  undo;
    static int initializing;
    CROPUNDOINFO *cui;

    cui = &(dat->crop[dat->c_undotail]);    
    if (cui->z_lock || x1 == x2 || y1 == y2)
        return;
    if (cui->z_undotail == cui->z_undohead && !initializing) {
        initializing = TRUE;
        psnd_push_zoomundo(vp_id, 0.0, 0.0, 1.0, 1.0, dat);
        initializing = FALSE;
    }
    undo.vp_id = vp_id;
    undo.xy[0] = x1;
    undo.xy[1] = y1;
    undo.xy[2] = x2;
    undo.xy[3] = y2;
    cui->z_undotail++;
    if (cui->z_undotail > ZOOMUNDOQMAX) 
        cui->z_undotail = 0;
    if (cui->z_undotail == cui->z_undohead) {
        (cui->z_undohead == ZOOMUNDOQMAX) ? 
            (cui->z_undohead = 0) : (cui->z_undohead++);
    }
    cui->z_redo = cui->z_undotail;
    memcpy(&(cui->zoomundo_queue[cui->z_undotail]), 
                                 &undo, sizeof(ZOOMUNDOINFO));
}

static ZOOMUNDOINFO *pop_undo(DBLOCK *dat)
{
    ZOOMUNDOINFO *ui;
    int savetail;
    CROPUNDOINFO *cui;

    if (!is_undo(dat))
        return NULL;
    cui = &(dat->crop[dat->c_undotail]);    
    savetail = cui->z_undotail;
    cui->z_undotail--;
    if (cui->z_undotail < 0) 
        cui->z_undotail = ZOOMUNDOQMAX;
    if (!is_undo(dat)) {
        cui->z_undotail = savetail;
        return NULL;
    }
    ui = &(cui->zoomundo_queue[cui->z_undotail]);
    return ui;
}

static ZOOMUNDOINFO *pop_redo(DBLOCK *dat)
{
    ZOOMUNDOINFO *ui;
    int savetail;
    CROPUNDOINFO *cui;

    if (!is_redo(dat))
        return NULL;
    cui = &(dat->crop[dat->c_undotail]);    
    savetail = cui->z_undotail;
    cui->z_undotail++;
    if (cui->z_undotail > ZOOMUNDOQMAX)
        cui->z_undotail = 0;
    if (!is_undo(dat)) {
        cui->z_undotail = savetail;
        return NULL;
    }
    ui = &(cui->zoomundo_queue[cui->z_undotail]);
    return ui;
}

/*
 * Get the latest zoom settings
 */
static ZOOMUNDOINFO *restore_undo(DBLOCK *dat)
{
    ZOOMUNDOINFO *ui;
    CROPUNDOINFO *cui;

    if (!is_undo(dat))
        return NULL;
    cui = &(dat->crop[dat->c_undotail]);    
    ui = &(cui->zoomundo_queue[cui->z_undotail]);
    return ui;
}


/*
 * Put/get zoom settings on/from the stack
 */
int psnd_popzoom(MBLOCK *mblk, int mode)
{
    int i;
    ZOOMUNDOINFO *zui;
    CROPUNDOINFO *cui;
    DBLOCK *dat = DAT;

    cui = &(dat->crop[dat->c_undotail]);    
    if (mode == PSND_UNDO_ZOOM) 
        zui = pop_undo(dat);
    else 
        zui = pop_redo(dat);
    if (zui == NULL) {
        return 0;
    }
    cui->z_lock = TRUE;
    if (zui->xy[0] == 0.0 && zui->xy[1] == 0.0 && 
        zui->xy[2] == 1.0 && zui->xy[3] == 1.0) {
            static G_EVENT ui;

            ui.event   = G_COMMAND;
            ui.win_id  = mblk->info->win_id;
            ui.keycode = PSND_RESET_NOUNDO;
            g_send_event(&ui);
    }
    else 
        psnd_scrollbars_reconnect(mblk,zui->vp_id, zui->xy[0],
                 zui->xy[1],zui->xy[2],zui->xy[3], TRUE);
    cui->z_lock = FALSE;
    return 1;
}

/*
 * Reset to the last zoom-settings, if present.
 * Reset to full size otherwise
 */
int psnd_lastzoom(MBLOCK *mblk)
{
    int i;
    ZOOMUNDOINFO *zui;
    CROPUNDOINFO *cui;
    DBLOCK *dat = DAT;

    cui = &(dat->crop[dat->c_undotail]);    
    zui = restore_undo(dat);
    cui->z_lock = TRUE;
    if (zui == NULL ||
        (zui->xy[0] == 0.0 && zui->xy[1] == 0.0 && 
         zui->xy[2] == 1.0 && zui->xy[3] == 1.0)) {
            static G_EVENT ui;

            ui.event   = G_COMMAND;
            ui.win_id  = mblk->info->win_id;
            ui.keycode = PSND_RESET_NOUNDO;
            g_send_event(&ui);
    }
    else 
        psnd_scrollbars_reconnect(mblk,zui->vp_id, zui->xy[0],
                 zui->xy[1],zui->xy[2],zui->xy[3], TRUE);
    cui->z_lock = FALSE;
    return 1;
}

/*
 * modify the latest zoom setting on the stack
 */
int psnd_update_lastzoom(int vp_id, float x1, float y1, 
                          float x2, float y2, DBLOCK *dat)
{
    int i;
    ZOOMUNDOINFO *zui;
    CROPUNDOINFO *cui;

    cui = &(dat->crop[dat->c_undotail]);    
    zui = restore_undo(dat);
    if (zui != NULL) {
        zui->vp_id = vp_id;
        zui->xy[0] = x1;
        zui->xy[1] = y1; 
        zui->xy[2] = x2;
        zui->xy[3] = y2;
        return 1;
    }
    return 0;
}


