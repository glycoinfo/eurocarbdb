/********************************************************************/
/*                          psnd_bookmark.c                         */
/*                                                                  */
/* Bookmark routines for the psnd program                           */
/* 1997, Albert van Kuik                                            */
/********************************************************************/
#include <stdio.h>
#include <stdlib.h>
#include <ctype.h>
#include <string.h>
#include <stdarg.h>
#include <math.h>
#include "genplot.h"
#include "psnd.h"

/*************************************************************************
* BOOKMARK QUEUE
*/


static void bookmark_add(BOOKMARKINFO *binfo, DBLOCK *dat)
{
    if (dat->b_count >= dat->b_size) {
        dat->b_size += 100;
        dat->bookmark_queue = (BOOKMARKINFO*) 
            realloc(dat->bookmark_queue, dat->b_size *  sizeof(BOOKMARKINFO));
    }
    memcpy(&(dat->bookmark_queue[dat->b_count]), binfo, sizeof(BOOKMARKINFO));
    dat->b_count++;
}

static void bookmark_delete(int pos, DBLOCK *dat)
{
    int i;
    
    if (pos < 1 || pos >= dat->b_count)
        return;
    for (i=pos;i<dat->b_count-1;i++)
        memcpy(&(dat->bookmark_queue[i]), 
               &(dat->bookmark_queue[i+1]), 
               sizeof(BOOKMARKINFO));
    dat->b_count--;
    dat->b_select= 0;
}


static void bookmark_init(DBLOCK *dat)
{
    BOOKMARKINFO bi;
    strcpy(bi.label,"BOOKMARKS");
    bi.next  = NULL;
    bookmark_add(&bi, dat); 
    dat->b_select=0;
}


static void set_bookmark(MBLOCK *mblk, DBLOCK *dat)
{
    BOOKMARKINFO bi;
    int i;
    PBLOCK *pars[MAX_DIM];

    for (i=0;i<MAX_DIM;i++)
        pars[i] = dat->pars[i];
    pars[0] = PAR;

    if (mblk->info->plot_mode == PLOT_2D && DAT->ityp > 1) {
        if (mblk->info->contour_row == PLOT_PERPENDICULAR && 
                 DAT->ityp >= 3) {
            pars[0] = dat->pars[2];
            pars[1] = dat->pars[0];
            pars[2] = dat->pars[1];
        }
        else if (mblk->info->contour_row == PLOT_COLUMN) {
            pars[0] = dat->pars[1];
            pars[1] = dat->pars[0];
            pars[2] = dat->pars[2];
        }
    }
    else {
        ((dat->access[0]-1) == mblk->info->dimension_id) ? 
            (pars[1] = dat->pars[1]) : (pars[1] = dat->pars[0]);
    }

    if (dat->ityp > 2)
        sprintf(bi.label," T%d, Row %d, Off %d", 
                          pars[0]->icrdir, pars[1]->key, pars[2]->key);
    else
        sprintf(bi.label," T%d, Row %d", pars[0]->icrdir, pars[1]->key);

    for (i=0;i<MAX_DIM;i++) {
        bi.key[i]	= pars[i]->key;
        bi.par[i]	= pars[i];
    }
    bookmark_add(&bi,dat);

}


static void goto_bookmark(MBLOCK *mblk, BOOKMARKINFO *bi, DBLOCK *dat)
{
    G_EVENT ui;
    int i;
    
    for (i=0;i<MAX_DIM;i++)
        bi->par[i]->key = bi->key[i];

    mblk->info->dimension_id = bi->par[0]->icrdir-1;
    psnd_select_contourviewport(mblk);
    psnd_nextrec(mblk,TRUE,0,bi->par, dat);
    ui.event = G_COMMAND;
    ui.win_id = mblk->info->win_id;
    ui.keycode = PSND_PL;
    g_send_event(&ui);
}



typedef enum {
    BOOKMARK_LISTBOX,
    BOOKMARK_ADD,
    BOOKMARK_GOTO,
    BOOKMARK_REMOVE,
    BOOKMARK_RESET,
    BOOKMARK_PHASE1,
    BOOKMARK_PHASE2,
    BOOKMARK_PHASE3
} popup_bookmark_ids;

static void bookmark_callback(G_POPUP_CHILDINFO *ci)
{
    MBLOCK *mblk = (MBLOCK *) ci->userdata;
    CBLOCK *cpar = mblk->cpar_screen;
    DBLOCK *dat  = DAT;
    POPUP_INFO *popinf = mblk->popinf + POP_BOOKMARK;
    
    switch (ci->type) {
        case G_CHILD_LISTBOX:
            g_popup_enable_item(ci->cont_id, BOOKMARK_PHASE1, mblk->info->phase_2d);
            g_popup_enable_item(ci->cont_id, BOOKMARK_PHASE2, mblk->info->phase_2d);
            g_popup_enable_item(ci->cont_id, BOOKMARK_PHASE3, mblk->info->phase_2d);
            dat->b_select = min(dat->b_count,ci->item);
            break;
        case G_CHILD_PUSHBUTTON:
            switch (ci->id) {
                case BOOKMARK_ADD:
                    set_bookmark(mblk, dat);
                    g_popup_append_item(ci->cont_id, 0,
                        dat->bookmark_queue[dat->b_count-1].label);
                    break;
                case BOOKMARK_GOTO:
                    if (dat->b_select > 0)
                        goto_bookmark(mblk,&(dat->bookmark_queue[dat->b_select]),dat);
                    break;
                case BOOKMARK_REMOVE:
                    if (dat->b_select > 0) {
                        g_popup_remove_item(ci->cont_id, 0,
                            dat->b_select);
                        bookmark_delete(dat->b_select,dat);
                    }
                    break;
                case BOOKMARK_RESET:
                    psnd_clear_popup_bookmark(mblk, dat);
                case BOOKMARK_PHASE1:
                case BOOKMARK_PHASE2:
                case BOOKMARK_PHASE3:
                    if (dat->b_select > 0)
                        psnd_add_phase(mblk, &(dat->bookmark_queue[dat->b_select]),
                                        cpar,ci->id - BOOKMARK_PHASE1);
                    break;
            }
            break;
        case G_CHILD_OK:
        case G_CHILD_CANCEL:
            popinf->visible = FALSE;
            break;
    }
}

void psnd_clear_popup_bookmark(MBLOCK *mblk, DBLOCK *dat)
{
    POPUP_INFO *popinf = mblk->popinf + POP_BOOKMARK;

    if (!popinf->cont_id) 
        return;
    if (mblk->info->block_id != popinf->block_id)
        return;
    g_popup_remove_item(popinf->cont_id, 0, -1);
    dat->b_count = 0;
    bookmark_init(mblk->dat[popinf->block_id]);
    g_popup_append_item(popinf->cont_id, 0,
                       dat->bookmark_queue[0].label);
}

void psnd_reset_popup_bookmark(MBLOCK *mblk, CBLOCK *cpar, DBLOCK *dat)
{
    int b_count;
    POPUP_INFO *popinf = mblk->popinf + POP_BOOKMARK;
    DBLOCK *dat_old;

    if (!popinf->cont_id) 
        return;
    if (mblk->info->block_id == popinf->block_id)
        return;
    dat_old = mblk->dat[popinf->block_id];
    b_count = dat_old->b_count;
    while (b_count > 1) {
        b_count--;
        g_popup_remove_item(popinf->cont_id, 0, b_count);
    }
    popinf->block_id = mblk->info->block_id;
    b_count = 1;
    if (dat->b_count == 0)
        bookmark_init(dat);
    while (b_count < dat->b_count) {
        g_popup_append_item(popinf->cont_id, 0,
            dat->bookmark_queue[b_count].label);
        b_count++;
    }
}

#define MAXLIST	100
void psnd_popup_bookmark(MBLOCK *mblk)
{
    int i,id;
    char *list[MAXLIST];
    G_POPUP_CHILDINFO ci[20];
    CBLOCK *cpar = mblk->cpar_screen;
    DBLOCK *dat  = DAT;
    POPUP_INFO *popinf = mblk->popinf + POP_BOOKMARK;

    if (popinf->visible)
        return;
    popinf->visible = TRUE;
    if (!popinf->cont_id) {
        popinf->cont_id = g_popup_container_open(mblk->info->win_id, 
                   "Bookmark options",  G_POPUP_KEEP|G_POPUP_SINGLEBUTTON);
        if (dat->b_count ==0)
            bookmark_init(dat);
            
        for (i=0;i<dat->b_count && i < MAXLIST;i++)
            list[i] = dat->bookmark_queue[i].label;

        id=0;
        g_popup_init_info(&(ci[id]));
        ci[id].type          = G_CHILD_LISTBOX;
        ci[id].id            = BOOKMARK_LISTBOX;
        ci[id].item_count    = dat->b_count;
        ci[id].data          = list;
        ci[id].item          = 0;
        ci[id].items_visible = 10;
        ci[id].func          = bookmark_callback;
        ci[id].userdata      = (void *) mblk;
        g_popup_add_child(popinf->cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_PUSHBUTTON;
        ci[id].id    = BOOKMARK_ADD;
        ci[id].label = "Add Bookmark";
        ci[id].func  = bookmark_callback;
        ci[id].userdata      = (void *) mblk;
        g_popup_add_child(popinf->cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_PUSHBUTTON;
        ci[id].id    = BOOKMARK_GOTO;
        ci[id].label = "Goto Bookmark";
        ci[id].func  = bookmark_callback;
        ci[id].userdata      = (void *) mblk;
        g_popup_add_child(popinf->cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_PUSHBUTTON;
        ci[id].id    = BOOKMARK_REMOVE;
        ci[id].label = "Remove Bookmark";
        ci[id].func  = bookmark_callback;
        ci[id].userdata      = (void *) mblk;
        g_popup_add_child(popinf->cont_id, &(ci[id]));
        
        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_PUSHBUTTON;
        ci[id].id    = BOOKMARK_RESET;
        ci[id].label = "Reset Bookmarks";
        ci[id].func  = bookmark_callback;
        ci[id].userdata      = (void *) mblk;
        g_popup_add_child(popinf->cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_PUSHBUTTON;
        ci[id].id    = BOOKMARK_PHASE1;
        ci[id].disabled = !mblk->info->phase_2d;
        ci[id].label = "Add to PhaseBlock 1";
        ci[id].func  = bookmark_callback;
        ci[id].userdata      = (void *) mblk;
        g_popup_add_child(popinf->cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_PUSHBUTTON;
        ci[id].id    = BOOKMARK_PHASE2;
        ci[id].disabled = !mblk->info->phase_2d;
        ci[id].label = "Add to PhaseBlock 2";
        ci[id].func  = bookmark_callback;
        ci[id].userdata      = (void *) mblk;
        g_popup_add_child(popinf->cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_PUSHBUTTON;
        ci[id].id    = BOOKMARK_PHASE3;
        ci[id].disabled = !mblk->info->phase_2d;
        ci[id].label = "Add to PhaseBlock 3";
        ci[id].func  = bookmark_callback;
        ci[id].userdata      = (void *) mblk;
        g_popup_add_child(popinf->cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_OK;
        ci[id].func  = bookmark_callback;
        ci[id].userdata      = (void *) mblk;
        g_popup_add_child(popinf->cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_CANCEL;
        ci[id].func  = bookmark_callback;
        ci[id].userdata      = (void *) mblk;
        g_popup_add_child(popinf->cont_id, &(ci[id]));

        popinf->block_id = mblk->info->block_id;

    }
    /*
    else
        psnd_reset_popup_bookmark(mblk, cpar, dat);
        */
    g_popup_container_show(popinf->cont_id);   
}


