/********************************************************************/
/*                             psnd_display.c                       */
/*                                                                  */
/* Display-functions popup selection boxes                          */
/* 1998, Albert van Kuik                                            */
/********************************************************************/
#include <stdio.h>
#include <stdlib.h>
#include <ctype.h>
#include <string.h>
#include <math.h>
#include <assert.h>
#include "genplot.h"
#include "psnd.h"


int psnd_popup_select_array(int win_id, int select, char *title, 
                             char *subtitle, int icomplex)
{
    char *list[] = {
        "Real", 
        "Imaginary", 
        "Buffer A", 
        "Buffer B", 
        "Window",
        "Baseline",
        "Real+Imag" } ;
    int item_count;
    if (icomplex)
        item_count = 7;
    else
        item_count = 6;
    return g_popup_radiobox(win_id, title, 
               subtitle, item_count, list, select);
}


/*
 * Popup menu to define which data arrays should be displayed
 */
#define VIEWSEL_NO	6
#define VIEWSEL_OFFSET	20

static void viewselect_callback(G_POPUP_CHILDINFO *ci)
{
    static G_EVENT ui;
    int s_ids[]  = {S_REAL, S_IMAG, S_BUFR1, S_BUFR2,
                    S_WINDOW, S_BASELN };
    MBLOCK *mblk = (MBLOCK*) ci->userdata;
    POPUP_INFO *popinf = mblk->popinf + POP_VIEWSELECT;
    
    switch (ci->type) {
        case G_CHILD_OK:
        case G_CHILD_CANCEL:
            popinf->visible = FALSE;
            return;
    }
    if (ci->type != G_CHILD_CHECKBOX)
        return;
    mblk->spar_block[ci->id-VIEWSEL_OFFSET][s_ids[ci->item]].show = 
                                           ci->select[ci->item];
    psnd_1d_reset_connection(mblk);
    ui.event = G_COMMAND;
    ui.win_id = mblk->info->win_id;
    ui.keycode = PSND_PL;
    g_send_event(&ui);

}

/*
 * Turn array visibility on/off
 * If:
 *     doit = 0,  hide
 *     doit = 1,  show
 *     doit = -1, toggle
 *
 * return TRUE if a change has been made, FALSE otherwise;
 */
int psnd_make_visible(MBLOCK *mblk, int block_id, int s_id, int doit)
{
    int i, s_ids[]  = {S_REAL, S_IMAG, S_BUFR1, S_BUFR2,
                       S_WINDOW, S_BASELN, -1 };
    POPUP_INFO *popinf = mblk->popinf + POP_VIEWSELECT;
    SBLOCK **spar0 = mblk->spar_block;

    if (doit == TOGGLE) 
        doit =  !spar0[block_id][s_id].show;
    else
        if (spar0[block_id][s_id].show == doit)
            return FALSE;
    spar0[block_id][s_id].show = doit;
    if (popinf->cont_id) {
        for (i=0; s_ids[i] != -1;i++) {
            if (s_id == s_ids[i]) {
                g_popup_set_checkmark(popinf->cont_id, 
                                      VIEWSEL_OFFSET+block_id, i, doit);
                break;
            }
        }
    }
    return TRUE;
}

/*
 * Return TRUE if a certain array is visible, FALSE otherwise
 */
int psnd_is_visible(MBLOCK *mblk, int block_id, int s_id)
{
    return mblk->spar_block[block_id][s_id].show;
}



/*
 * When new blocks are allocated, enable here
 */
void psnd_enable_viewselect_blocks(MBLOCK *mblk)
{
    int i,id;
    POPUP_INFO *popinf = mblk->popinf + POP_VIEWSELECT;

    if (popinf->cont_id) {
        for (i=0;i<mblk->info->max_block;i++) {
            id = VIEWSEL_OFFSET+i;
            g_popup_enable_item(popinf->cont_id, id, TRUE);
        }
    }
}

/*
 * Select the arrays that are to be displayed
 */
void psnd_popup_viewselect(MBLOCK *mblk)
{
    G_POPUP_CHILDINFO ci[100];
    int i=0, j, select[MAXBLK][VIEWSEL_NO];
    int s_ids[]          = {  S_REAL, S_IMAG, S_BUFR1, S_BUFR2,
                              S_WINDOW, S_BASELN };
    char *array_select[] = { "Real", "Imaginary", "Buffer A", "Buffer B", 
                             "Window", "Baseline" } ;
    char *title[MAX_BLOCK]        = 
        { "Block1", "Block2", "Block3", "Block4", 
          "Block5", "Block6", "Block7", "Block8",
          "Block9", "Block10", "Block11", "Block12"
        } ;    int id = 0;
    POPUP_INFO *popinf = mblk->popinf + POP_VIEWSELECT;

    assert(mblk->info->max_block < 12);
    if (popinf->visible)
        return;
    popinf->visible = TRUE;
    if (!popinf->cont_id) { 
        int cont_id;

        id=-1;
#ifndef __sgi
        cont_id = g_popup_container_open(mblk->info->win_id, "Make visible",
                                     G_POPUP_KEEP|G_POPUP_SINGLEBUTTON| G_POPUP_TAB); 
#else
        cont_id = g_popup_container_open(mblk->info->win_id, "Make visible",
                                     G_POPUP_KEEP|G_POPUP_SINGLEBUTTON); 
        id++;
        g_popup_init_info(ci + id);
        ci[id].type       = G_CHILD_PANEL;
        ci[id].item       = TRUE;
        ci[id].horizontal = TRUE;
        g_popup_add_child(cont_id, ci + id);
#endif
        popinf->cont_id = cont_id;

        for (i=0;i<MAXBLK;i++) {
            char *label;
            for (j=0;j<VIEWSEL_NO;j++) 
                select[i][j] = mblk->spar_block[i][s_ids[j]].show;
#ifndef __sgi
            id++;
	    g_popup_init_info(&(ci[id]));
            ci[id].type  = G_CHILD_TAB;
            label = psnd_sprintf_temp("Block %d", i+1);
	    ci[id].label = label;
	    g_popup_add_child(cont_id, &(ci[id]));
#endif

            id++;
            g_popup_init_info(&(ci[id]));
            ci[id].type       = G_CHILD_CHECKBOX;
            ci[id].id         = VIEWSEL_OFFSET+i;
            ci[id].title      = title[i];
            ci[id].frame      = TRUE;
            ci[id].item_count = VIEWSEL_NO;
            ci[id].data       = array_select;
            ci[id].select     = select[i];
            ci[id].disabled   = (i >= mblk->info->max_block);
            ci[id].horizontal = FALSE;
            ci[id].func       = viewselect_callback;
            ci[id].userdata   = (void*) mblk;
            g_popup_add_child(cont_id, &(ci[id]));
        }
#ifdef __sgi
        id++;
        g_popup_init_info(ci + id);
        ci[id].type       = G_CHILD_PANEL;
        ci[id].item       = FALSE;
        ci[id].horizontal = TRUE;
        g_popup_add_child(cont_id, ci + id);

#endif
        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_CANCEL;
        ci[id].func       = viewselect_callback;
        ci[id].userdata   = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_OK;
        ci[id].func       = viewselect_callback;
        ci[id].userdata   = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));
    }
    assert(id<100);
    g_popup_container_show(popinf->cont_id);
}

/*
 * Popup menu to define which data-array-viewports should be linked
 */
#define LINKSEL_NO	5
#define LINKOFFSET	200
#define LINKOFFSET2	100
#define FIRST_BLOCK_ID	0

static void linkselect_callback(G_POPUP_CHILDINFO *ci)
{
    static G_EVENT ui;
    int i,j;
    int s_ids[]  = { S_IMAG, S_BUFR1, S_BUFR2,
                    S_WINDOW, S_BASELN, -1 };
    MBLOCK *mblk = (MBLOCK*) ci->userdata;
    POPUP_INFO *popinf = mblk->popinf + POP_LINKSELECT;
    
    switch (ci->type) {
        case G_CHILD_OK:
        case G_CHILD_CANCEL:
            popinf->visible = FALSE;
            return;
    }

    if (ci->type != G_CHILD_CHECKBOX)
        return;
    if (ci->select[ci->item]) {
        if (ci->id >= LINKOFFSET) {
            j = ci->id - LINKOFFSET;
            mblk->spar_block[j][S_REAL].vp_id_block_master = 
                    &(mblk->spar_block[FIRST_BLOCK_ID][S_REAL].vp_id); 
            for (i=0;s_ids[i] != -1;i++)
                mblk->spar_block[j][s_ids[i]].vp_id_block_master = 
                    &(mblk->spar_block[FIRST_BLOCK_ID][s_ids[i]].vp_id_master); 
            g_popup_enable_item(ci->cont_id, j+LINKOFFSET2, FALSE);        
        }
        else {
            j = ci->id - LINKOFFSET2;
            mblk->spar_block[j][s_ids[ci->item]].vp_id_master = 
                mblk->spar_block[j][S_REAL].vp_id;
        }
    }
    else {
        if (ci->id >= LINKOFFSET) {
            j = ci->id - LINKOFFSET;
            mblk->spar_block[j][S_REAL].vp_id_block_master = NULL;
            for (i=0;s_ids[i] != -1;i++)
                mblk->spar_block[j][s_ids[i]].vp_id_block_master = NULL;
            g_popup_enable_item(ci->cont_id, j+LINKOFFSET2, TRUE);        
        }
        else {
            j = ci->id - LINKOFFSET2;
            mblk->spar_block[j][s_ids[ci->item]].vp_id_master = 0;
        }
    }
    psnd_1d_reset_connection(mblk);
    ui.event = G_COMMAND;
    ui.win_id = mblk->info->win_id;
    ui.keycode = PSND_PL;
    g_send_event(&ui);

}

/*
 * Link the scaling of one array to that of the scaling of the real data
 */
void psnd_set_viewport_master(MBLOCK *mblk, int block_id, 
                               int s_id, int doit)
{
    int i;
    int s_ids[]  = { S_IMAG, S_BUFR1, S_BUFR2,
                    S_WINDOW, S_BASELN, -1 };
    SBLOCK **spar0 = mblk->spar_block;
    POPUP_INFO *popinf = mblk->popinf + POP_LINKSELECT;

    if (block_id < 0 || block_id >= mblk->info->max_block)
        return;
    if (doit)
        spar0[block_id][s_id].vp_id_master = 
            spar0[block_id][S_REAL].vp_id;
    else 
        spar0[block_id][s_id].vp_id_master = 0;
    if (popinf->cont_id) {
        for (i=0; s_ids[i] != -1;i++) {
            if (s_id == s_ids[i]) {
                g_popup_set_checkmark(popinf->cont_id, 
                                      LINKOFFSET2+block_id, i, doit);
                break;
            }
        }
    }
}

/*
 * Link the scaling from one block onto the scaling of the first block
 */
void psnd_set_viewport_block_master(MBLOCK *mblk, int block_id, 
                               int doit)
{
    int i,j;
    int s_ids[]  = { S_IMAG, S_BUFR1, S_BUFR2,
                    S_WINDOW, S_BASELN, -1 };
    SBLOCK **spar0 = mblk->spar_block;
    POPUP_INFO *popinf = mblk->popinf + POP_LINKSELECT;


    if (block_id <= FIRST_BLOCK_ID || block_id >= mblk->info->max_block)
        return;
    if (doit) {
        spar0[block_id][S_REAL].vp_id_block_master = 
            &(spar0[FIRST_BLOCK_ID][S_REAL].vp_id);
        for (i=0;s_ids[i] != -1;i++)
            spar0[block_id][s_ids[i]].vp_id_block_master = 
                &(spar0[FIRST_BLOCK_ID][s_ids[i]].vp_id_master); 
        if (popinf->cont_id) {
            g_popup_enable_item(popinf->cont_id, block_id+LINKOFFSET2, FALSE); 
            g_popup_set_checkmark(popinf->cont_id, 
                                  LINKOFFSET+block_id, 
                                  FIRST_BLOCK_ID, doit);
        }
    }
    else {
        spar0[block_id][S_REAL].vp_id_block_master = NULL;
        for (i=0;s_ids[i] != -1;i++)
            spar0[block_id][s_ids[i]].vp_id_block_master = NULL;
        if (popinf->cont_id) {
            g_popup_enable_item(popinf->cont_id, block_id+LINKOFFSET2, TRUE); 
            g_popup_set_checkmark(popinf->cont_id, 
                                  LINKOFFSET+block_id, 
                                  FIRST_BLOCK_ID, doit);
        }
    }
}

/*
 * When new blocks are allocated, enable here
 */
void psnd_enable_linkselect_blocks(MBLOCK *mblk, int from, int to)
{
    int i,id;
    POPUP_INFO *popinf = mblk->popinf + POP_LINKSELECT;

    if (popinf->cont_id) {
        for (i=from;i<to;i++) {
            id = LINKOFFSET+i;
            g_popup_enable_item(popinf->cont_id, id, TRUE);
            id = LINKOFFSET2+i;
            g_popup_enable_item(popinf->cont_id, id, TRUE);
        }
    }
}


/*
 * Select the arrays that are to be linked to the scaling of
 * the real data
 */
void psnd_popup_linkselect(MBLOCK *mblk)
{
    G_POPUP_CHILDINFO ci[100];
    int i=0, j, blocksel[MAXBLK][1],select[MAXBLK][LINKSEL_NO];
    int s_ids[]  = { S_IMAG, S_BUFR1, S_BUFR2,
                    S_WINDOW, S_BASELN };
    char *title[] = { "Imaginary", "Buffer A", "Buffer B", 
                      "Window", "Baseline" } ;
    char *block_select[MAX_BLOCK] = 
        { "Block1", "Block2", "Block3", "Block4", 
          "Block5", "Block6", "Block7", "Block8",
          "Block9", "Block10", "Block11", "Block12"
        } ;
    char *link_select[MAX_BLOCK] = 
        { 
           "", "Whole Block", "Whole Block", "Whole Block",
           "Whole Block", "Whole Block", "Whole Block","Whole Block", 
           "Whole Block","Whole Block", "Whole Block", "Whole Block"
    } ;
    int id=0;
    POPUP_INFO *popinf = mblk->popinf + POP_LINKSELECT;

    assert(mblk->info->max_block < 12);
    if (popinf->visible)
        return;
    popinf->visible = TRUE;
    if (!popinf->cont_id) {
        int cont_id;

        id=-1;
#ifndef __sgi
        cont_id = g_popup_container_open(mblk->info->win_id, 
                                     "Link scaling to Real",
                                     G_POPUP_KEEP|G_POPUP_SINGLEBUTTON| G_POPUP_TAB); 
#else
        cont_id = g_popup_container_open(mblk->info->win_id, 
                                     "Link scaling to Real",
                                     G_POPUP_KEEP|G_POPUP_SINGLEBUTTON); 
        id++;
        g_popup_init_info(ci + id);
        ci[id].type       = G_CHILD_PANEL;
        ci[id].item       = TRUE;
        ci[id].horizontal = TRUE;
        g_popup_add_child(cont_id, ci + id);
#endif
        popinf->cont_id = cont_id;


        for (i=0;i<MAXBLK;i++) {
            char *label;
            
            blocksel[i][0] = (mblk->spar_block[i][S_REAL].vp_id_block_master != NULL);
            for (j=0;j<LINKSEL_NO;j++) 
                select[i][j] = (mblk->spar_block[i][s_ids[j]].vp_id_master != 0);

#ifndef __sgi
            id++;
	    g_popup_init_info(&(ci[id]));
            ci[id].type  = G_CHILD_TAB;
            label = psnd_sprintf_temp("Block %d", i+1);
	    ci[id].label = label;
	    g_popup_add_child(cont_id, &(ci[id]));
#endif

            g_popup_init_info(ci + id);
            ci[id].type       = G_CHILD_PANEL;
            ci[id].item       = TRUE;
            ci[id].frame      = TRUE;
            ci[id].horizontal = FALSE;
            ci[id].title      = block_select[i];
            g_popup_add_child(cont_id, ci + id);

            id++;
            g_popup_init_info(&(ci[id]));
            ci[id].type       = G_CHILD_CHECKBOX;
            ci[id].id         = LINKOFFSET+i;
            ci[id].frame      = TRUE;
            ci[id].title      = "To Block 1";
            ci[id].item_count = 1;
            ci[id].data       = link_select+i;
            ci[id].select     = blocksel[i];
            ci[id].disabled   = (i==0)||(i >= mblk->info->max_block);
            ci[id].func       = linkselect_callback;
            ci[id].userdata   = (void*) mblk;
            g_popup_add_child(cont_id, &(ci[id]));

            id++;
            g_popup_init_info(&(ci[id]));
            ci[id].type       = G_CHILD_CHECKBOX;
            ci[id].id         = LINKOFFSET2+i;
            ci[id].frame      = TRUE;
            ci[id].title      = "To Real";
            ci[id].item_count = LINKSEL_NO;
            ci[id].data       = title;
            ci[id].select     = select[i];
            ci[id].horizontal = FALSE;
            ci[id].disabled   = blocksel[i][0];
            ci[id].disabled   = (i >= mblk->info->max_block);
            ci[id].func       = linkselect_callback;
            ci[id].userdata   = (void*) mblk;
            g_popup_add_child(cont_id, &(ci[id]));

            id++;
            g_popup_init_info(ci + id);
            ci[id].type       = G_CHILD_PANEL;
            ci[id].item       = 0;
            g_popup_add_child(cont_id, ci + id);
    
            id++;
        }
        
#ifdef __sgi
        g_popup_init_info(ci + id);
        ci[id].type       = G_CHILD_PANEL;
        ci[id].item       = FALSE;
        ci[id].horizontal = TRUE;
        g_popup_add_child(cont_id, ci + id);
#endif
        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_CANCEL;
        ci[id].func       = linkselect_callback;
        ci[id].userdata   = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_OK;
        ci[id].func       = linkselect_callback;
        ci[id].userdata   = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));
    }
    assert(id<100);
    g_popup_container_show(popinf->cont_id);
}

/*
 * Adjust relative scaling and offset of the different arrays
 */
typedef enum {
    ADJUST_SENSITIVE_ID,
    ADJUST_SELECT_ID,
    ADJUST_XY_ID,
    ADJUST_BLOCK_ID,
    ADJUST_MOUSE_ID,
    ADJUST_GRAB_MOUSE_ID,
    ADJUST_SCALEX_ID,
    ADJUST_MOVEX_ID,
    ADJUST_SCALEY_ID,
    ADJUST_MOVEY_ID,
    ADJUST_2DAUTO_ID,
    ADJUST_RESET_ID
} popup_adjust_ids;

#define SCALE_ADJUST	1000
#define SCALE_ADJUST2	100
#define TEXT_OFFSET	100
#define BUTTON_OFFSET	200
#define BUTTON2_OFFSET	300


static char *compute_label(int mode, float f)
{
    char *result;
    
    switch (mode) {
        case ADJUST_SCALEX_ID:
            result = psnd_sprintf_temp("%g",1/AXIS_SCALE(f));
            break;
        case ADJUST_MOVEX_ID:
            result = psnd_sprintf_temp("%g", f);
            break;
        case ADJUST_SCALEY_ID:
            result = psnd_sprintf_temp("%g",1/AXIS_SCALE(f));
            break;
        case ADJUST_MOVEY_ID:
            result = psnd_sprintf_temp("%g", f);
            break;
    }            
    return result;
}

void adjust_callback(G_POPUP_CHILDINFO *ci)
{
    G_EVENT ui;
    int s_ids[]  = {S_REAL, S_IMAG, S_BUFR1, S_BUFR2,
                    S_WINDOW, S_BASELN };
    float f;
    int ib, is, io, io2, off = ADJUST_SCALEX_ID;
    MBLOCK *mblk = (MBLOCK *) ci->userdata;
    POPUP_INFO *popinf = mblk->popinf + POP_VIEWADJUST;


    switch (ci->type) {
        case G_CHILD_OK:
        case G_CHILD_CANCEL:
            g_menu_enable_item(mblk->info->mousebar_id, ID_MOUSE_SCALE, FALSE);
            if (mblk->info->mouse_mode1 == MOUSE_DISPLAY)
                psnd_set_cursormode(mblk, 0, 0);   
            popinf->visible = FALSE;
            return;
    }

    switch (ci->id) {
        case ADJUST_SENSITIVE_ID:
            mblk->info->adjust_display_sensitive = ci->item;
            break;
        case ADJUST_XY_ID:
            {
                int operation = mblk->info->adjust_display_operation & 0x3;
                mblk->info->adjust_display_operation = operation;
                mblk->info->adjust_display_operation += (ci->item > 0) ? 0x4 : 0;

            ib = mblk->info->adjust_display_block;
            is = mblk->info->adjust_display_item;
            /*
             * Y scale
             */
            io2 = io = Y_SCALE;
            if (mblk->info->adjust_display_operation & 0x4)
                io -= Y_SCALE;
            f = mblk->spar_block[ib][is].dxy[io];
            g_popup_set_label(ci->cont_id, TEXT_OFFSET+io2+off, 
                              compute_label(io2+off,f));
            f = -log10(AXIS_SCALE(f)) * SCALE_ADJUST;
            g_popup_set_selection(ci->cont_id, io2+off, f);
            /*
             * Y move
             */
            io2 = io = Y_MOVE;
            if (mblk->info->adjust_display_operation & 0x4)
                io -= Y_SCALE;
            f = mblk->spar_block[ib][is].dxy[io];
            g_popup_set_label(ci->cont_id, TEXT_OFFSET+io2+off, 
                              compute_label(io2+off,f));
            f *= SCALE_ADJUST2;
            g_popup_set_selection(ci->cont_id, io2+off, f);

            }
            break;
        case ADJUST_SELECT_ID:
        case ADJUST_BLOCK_ID:
            if (ci->id == ADJUST_SELECT_ID)
                mblk->info->adjust_display_item       = s_ids[ci->item];
            else {
                if (ci->item >= mblk->info->max_block) {
                    g_popup_set_selection(ci->cont_id, ADJUST_BLOCK_ID, 
                                       mblk->info->adjust_display_block) ;
                    break;
                }
                mblk->info->adjust_display_block = ci->item;
            }
            ib = mblk->info->adjust_display_block;
            is = mblk->info->adjust_display_item;
#ifdef xxxx
            /*
             * X scale
             */
            io = X_SCALE;
            f = mblk->spar_block[ib][is].dxy[io];
            g_popup_set_label(ci->cont_id, TEXT_OFFSET+io+off, 
                              compute_label(io+off,f));
            f = -log10(AXIS_SCALE(f)) * SCALE_ADJUST;
            g_popup_set_selection(ci->cont_id, io+off, f);
            /*
             * X move
             */
            io = X_MOVE;
            f = mblk->spar_block[ib][is].dxy[io];
            g_popup_set_label(ci->cont_id, TEXT_OFFSET+io+off, 
                              compute_label(io+off,f));
            f *= SCALE_ADJUST2;
            g_popup_set_selection(ci->cont_id, io+off, f);
#endif
            /*
             * Y scale
             */
            io2 = io = Y_SCALE;
            if (mblk->info->adjust_display_operation & 0x4)
                io -= Y_SCALE;
            f = mblk->spar_block[ib][is].dxy[io];
            g_popup_set_label(ci->cont_id, TEXT_OFFSET+io2+off, 
                              compute_label(io2+off,f));
            f = -log10(AXIS_SCALE(f)) * SCALE_ADJUST;
            g_popup_set_selection(ci->cont_id, io2+off, f);
            /*
             * Y move
             */
            io2 = io = Y_MOVE;
            if (mblk->info->adjust_display_operation & 0x4)
                io -= Y_SCALE;
            f = mblk->spar_block[ib][is].dxy[io];
            g_popup_set_label(ci->cont_id, TEXT_OFFSET+io2+off, 
                              compute_label(io2+off,f));
            f *= SCALE_ADJUST2;
            g_popup_set_selection(ci->cont_id, io2+off, f);
            break;
        case ADJUST_GRAB_MOUSE_ID:
            {
                psnd_set_cursormode(mblk, 0, MOUSE_DISPLAY);
            }
            break;
        case ADJUST_MOUSE_ID:
            {
                int operation = mblk->info->adjust_display_operation & 0x4;
                mblk->info->adjust_display_operation  = Y_SCALE + ci->item + operation;
            }
            break;
        case ADJUST_MOVEX_ID+BUTTON_OFFSET:
        case ADJUST_MOVEY_ID+BUTTON_OFFSET:
        case ADJUST_SCALEX_ID+BUTTON_OFFSET:
        case ADJUST_SCALEY_ID+BUTTON_OFFSET:
            ci->type  = G_CHILD_SCALE;
            ci->item  = 0;
            ci->id   -= BUTTON_OFFSET;
            ci->userdata = (void*) mblk;
            g_popup_set_selection(ci->cont_id, 
                                  ci->id,
                                  ci->item);
            adjust_callback(ci);
            break;
        case ADJUST_MOVEX_ID+BUTTON2_OFFSET:
        case ADJUST_MOVEY_ID+BUTTON2_OFFSET:
        case ADJUST_SCALEX_ID+BUTTON2_OFFSET:
        case ADJUST_SCALEY_ID+BUTTON2_OFFSET:
            {
            char *p =
                g_popup_get_label(ci->cont_id, 
                        ci->id - BUTTON2_OFFSET + TEXT_OFFSET);
            if (!p)
                break;
            f = psnd_scan_float(p);
            switch (ci->id) {
                case ADJUST_SCALEX_ID+BUTTON2_OFFSET:
                case ADJUST_SCALEY_ID+BUTTON2_OFFSET:            
                    f = log10(f) * SCALE_ADJUST;
                    f = max(f, -2*SCALE_ADJUST);
                    f = min(f, 2*SCALE_ADJUST);
                    break;
                default:
                    f *= SCALE_ADJUST2;
                    f  = max(f, -SCALE_ADJUST2);
                    f  = min(f, SCALE_ADJUST2);
                    break;
            }
            ci->type  = G_CHILD_SCALE;
            ci->item  = round(f);
            ci->id   -= BUTTON2_OFFSET;
            ci->userdata = (void*) mblk;
            g_popup_set_selection(ci->cont_id, 
                                  ci->id,
                                  ci->item);
            adjust_callback(ci);

            }
            break;
        case ADJUST_SCALEX_ID:
        case ADJUST_SCALEY_ID:
            ib = mblk->info->adjust_display_block;
            is = mblk->info->adjust_display_item;
            io = ci->id-off;
            if (mblk->info->adjust_display_operation & 0x4)
                io -= Y_SCALE;
            f = pow(10,(float)ci->item /SCALE_ADJUST);
            f = -UN_AXIS_SCALE(f);
            if (mblk->spar_block[ib][is].dxy[io] == f)
                break;
            g_popup_set_label(ci->cont_id, TEXT_OFFSET+ci->id, 
                              compute_label(ci->id,f));

            mblk->spar_block[ib][is].dxy[io] = f;
            ui.event = G_COMMAND;
            ui.win_id = mblk->info->win_id;
            ui.keycode = PSND_PL;
            g_send_event(&ui);
            break; 
        case ADJUST_MOVEX_ID:
        case ADJUST_MOVEY_ID:
            ib = mblk->info->adjust_display_block;
            is = mblk->info->adjust_display_item;
            io = ci->id-off;
            if (mblk->info->adjust_display_operation & 0x4)
                io -= Y_SCALE;
            f = (float)ci->item/SCALE_ADJUST2;
            if (mblk->spar_block[ib][is].dxy[io] == f)
                break;
            g_popup_set_label(ci->cont_id, TEXT_OFFSET+ci->id, 
                              compute_label(ci->id,f));
            mblk->spar_block[ib][is].dxy[io] = f;
            ui.event = G_COMMAND;
            ui.win_id = mblk->info->win_id;
            ui.keycode = PSND_PL;
            g_send_event(&ui);
            break; 
        case ADJUST_2DAUTO_ID:
            ib = mblk->info->adjust_display_block;
            is = mblk->info->adjust_display_item;
            mblk->spar_block[ib][is].disable_auto_scale_2d = ci->item;
            break;
        case ADJUST_RESET_ID:
            ib = mblk->info->adjust_display_block;
            is = mblk->info->adjust_display_item;

            io = X_SCALE;
            mblk->spar_block[ib][is].dxy[io] = 0.0;
/*
            g_popup_set_selection(ci->cont_id, io+off, 0.0);
            g_popup_set_label(ci->cont_id, TEXT_OFFSET+io+off, 
                              compute_label(io+off,0.0));
*/
            io = X_MOVE;
            mblk->spar_block[ib][is].dxy[io] = 0.0;
/*
            g_popup_set_selection(ci->cont_id, io+off, 0.0);
            g_popup_set_label(ci->cont_id, TEXT_OFFSET+io+off, 
                              compute_label(io+off,0.0));
*/
            io = Y_SCALE;
            mblk->spar_block[ib][is].dxy[io] = 0.0;
            g_popup_set_selection(ci->cont_id, io+off, 0.0);
            g_popup_set_label(ci->cont_id, TEXT_OFFSET+io+off, 
                              compute_label(io+off,0.0));
            io = Y_MOVE;
            mblk->spar_block[ib][is].dxy[io] = 0.0;
            g_popup_set_selection(ci->cont_id, io+off, 0.0);
            g_popup_set_label(ci->cont_id, TEXT_OFFSET+io+off, 
                              compute_label(io+off,0.0));
            ui.event = G_COMMAND;
            ui.win_id = mblk->info->win_id;
            ui.keycode = PSND_PL;
            g_send_event(&ui);
            break;
        default:
            /*
             * This comes from Mouse 
             */
            ib = mblk->info->adjust_display_block;
            is = mblk->info->adjust_display_item;
            io2 = io = (mblk->info->adjust_display_operation & 0x3);
        
            if (mblk->info->adjust_display_operation & 0x4)
                io -= Y_SCALE;
            switch (io) {
                case X_SCALE:
                case Y_SCALE:
                    mblk->spar_block[ib][is].dxy[io] 
                        = max(mblk->spar_block[ib][is].dxy[io], -2*SCALE_ADJUST);
                    mblk->spar_block[ib][is].dxy[io] 
                        = min(mblk->spar_block[ib][is].dxy[io], 2*SCALE_ADJUST);
                    if (popinf->cont_id) {
                        f = mblk->spar_block[ib][is].dxy[io];
                        g_popup_set_label(popinf->cont_id, 
                                      TEXT_OFFSET+io2+off, 
                                      compute_label(io2+off,f));
                        f = AXIS_SCALE(f);
                        f = -log10(f) * SCALE_ADJUST;
                        g_popup_set_selection(popinf->cont_id, 
                                          io2+off,
                                          f);
                    }
                    break;
                case X_MOVE:
                case Y_MOVE:
                    mblk->spar_block[ib][is].dxy[io] 
                        = max(mblk->spar_block[ib][is].dxy[io], -SCALE_ADJUST2);
                    mblk->spar_block[ib][is].dxy[io] 
                        = min(mblk->spar_block[ib][is].dxy[io], SCALE_ADJUST2);
                    f = mblk->spar_block[ib][is].dxy[io];
                    if (popinf->cont_id) {
                        g_popup_set_label(popinf->cont_id, 
                                      TEXT_OFFSET+io2+off, 
                                      compute_label(io2+off,f));
                        g_popup_set_selection(popinf->cont_id, 
                                          io2+off,
                                          f*SCALE_ADJUST2);
                    }
                    break;
            }
            break;
    }
}

#define MAX_ID	42
/*
 * Scale or move arrays
 */
void psnd_popup_viewadjust(MBLOCK *mblk)
{
    static int select_item, cont_id;
    G_POPUP_CHILDINFO ci[MAX_ID];
    char labelstring[100];
    int i=0, j, id;
    float f;
    int s_ids[]          = { S_REAL, S_IMAG, S_BUFR1, S_BUFR2,
                             S_WINDOW, S_BASELN };
    char *yx[]           = { "Y", "X" } ;
    char *radio[]        = { "Real", "Imaginary", "Buffer A", 
                             "Buffer B", "Window", "Baseline" } ;
    char *block_select[MAX_BLOCK] = { "Block1", "Block2", "Block3", "Block4",
                             "Block5", "Block6", "Block7", "Block8",
                             "Block9", "Block10", "Block11", "Block12" } ;
    char *sens[]         = { "High", "Medium", "Low" } ;
    char *operation[]    = { "Scale", "Move" } ;
/*
    char *operation[]    = { "Scale X", "Move X", "Scale Y", "Move Y" } ;
*/
    char *yes_no[]       = { "Yes", "No" };
    int min_max_dec[]    = { -SCALE_ADJUST2, SCALE_ADJUST2, 2 };
    char *tics[]         = { "1", "0.5", "0", "-0.5", "-1" };
    int min_max_dec2[]   = { -2*SCALE_ADJUST, 2*SCALE_ADJUST, -1};
    char *tics2[]        = { "100", "10", "1", "0.1", "0.01" };
    int ib               = mblk->info->adjust_display_block;
    int is               = mblk->info->adjust_display_item;
    int io               = mblk->info->adjust_display_operation & 0x3;
    int num_ticks        = 9;
    int num_labels       = 5;
    POPUP_INFO *popinf = mblk->popinf + POP_VIEWADJUST;

    assert(mblk->info->max_block < 12);
    min_max_dec[2] = log10(SCALE_ADJUST2);
    
    if (popinf->visible)
        return;
    popinf->visible = TRUE;
    if (!popinf->cont_id) {
        int cont_id;

        cont_id = g_popup_container_open(mblk->info->win_id, "Adjust Scaling",
                                         G_POPUP_KEEP|G_POPUP_SINGLEBUTTON);
        popinf->cont_id = cont_id;
        for (i=0;i<6;i++)
            if (mblk->info->adjust_display_item == s_ids[i]) {
                select_item=i;
                break;
            }
        id=-1;

        /*
         * Open Mouse panel
         */
        id++;
        g_popup_init_info(ci + id);
        ci[id].type         	= G_CHILD_PANEL;
        ci[id].item         	= TRUE;
        ci[id].frame 		= TRUE;
        ci[id].title 		= "Mouse";
        ci[id].horizontal   	= FALSE; /* TRUE; */
        g_popup_add_child(cont_id, ci + id);

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  		= G_CHILD_OPTIONMENU;
        ci[id].id    		= ADJUST_SENSITIVE_ID;
        ci[id].item_count 	= 3;
        ci[id].item 		= mblk->info->adjust_display_sensitive;
        ci[id].data 		= sens;
        ci[id].horizontal 	= TRUE;
        ci[id].label 		= "Sensitivety";
        ci[id].func       	= adjust_callback;
        ci[id].userdata  	= (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  		= G_CHILD_OPTIONMENU;
        ci[id].id    		= ADJUST_MOUSE_ID;
        ci[id].item_count 	= 2;
        ci[id].item 		= io - Y_SCALE;
        ci[id].data 		= operation;
        ci[id].horizontal 	= TRUE;
        ci[id].label 		= "Operation  ";
        ci[id].func       	= adjust_callback;
        ci[id].userdata  	= (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(ci + id);
        ci[id].type       = G_CHILD_PANEL;
        ci[id].item       = FALSE;
        g_popup_add_child(cont_id, ci + id);
 
        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_PUSHBUTTON;
        ci[id].id    = ADJUST_GRAB_MOUSE_ID;
        ci[id].label = "Grab Mouse Button 1";
        ci[id].func  = adjust_callback;
        ci[id].userdata = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));
 
        /*
         * Open Select panel
         */
        id++;
        g_popup_init_info(ci + id);
        ci[id].type         	= G_CHILD_PANEL;
        ci[id].item         	= TRUE;
        ci[id].frame 		= TRUE;
        ci[id].title 		= "Select";
        ci[id].horizontal   	= FALSE; /* TRUE;*/
        g_popup_add_child(cont_id, ci + id);
    
        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  		= G_CHILD_OPTIONMENU;
        ci[id].id    		= ADJUST_BLOCK_ID;
        ci[id].item_count 	= MAXBLK; /*mblk->info->max_block;*/
        ci[id].item 		= mblk->info->adjust_display_block;
        ci[id].data 		= block_select;
        ci[id].horizontal 	= TRUE;
        ci[id].label 		= "Block      ";
        ci[id].func       	= adjust_callback;
        ci[id].userdata  	= (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));
    
        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  		= G_CHILD_OPTIONMENU;
        ci[id].id    		= ADJUST_SELECT_ID;
        ci[id].item_count 	= 6;
        ci[id].item 		= select_item;
        ci[id].data 		= radio;
        ci[id].horizontal 	= TRUE;
        ci[id].label 		= "Array      ";
        ci[id].func       	= adjust_callback;
        ci[id].userdata  	= (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));
    
        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  		= G_CHILD_OPTIONMENU;
        ci[id].id    		= ADJUST_XY_ID;
        ci[id].item_count 	= 2;
        ci[id].item 		= mblk->info->adjust_display_operation & 0x4;
        ci[id].data 		= yx;
        ci[id].horizontal 	= TRUE;
        ci[id].label 		= "Y or X     ";
        ci[id].func       	= adjust_callback;
        ci[id].userdata  	= (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));
    
        id++;
        g_popup_init_info(ci + id);
        ci[id].type       = G_CHILD_PANEL;
        ci[id].item       = FALSE;
        g_popup_add_child(cont_id, ci + id);
    
    
        /*
         * Open Scale panel
         */
        id++;
        g_popup_init_info(ci + id);
        ci[id].type       = G_CHILD_PANEL;
        ci[id].item       = TRUE;
        ci[id].horizontal = TRUE;
        g_popup_add_child(cont_id, ci + id);
#ifdef nnnn
        /*
         * Scale X sub-panel
         */
        id++;
        g_popup_init_info(ci + id);
        ci[id].type          = G_CHILD_PANEL;
        ci[id].item          = TRUE;
        ci[id].frame         = TRUE;
        ci[id].title 	     = "Scale X";
        ci[id].horizontal    = FALSE;
        g_popup_add_child(cont_id, ci + id);
    
        id++;
        f = -log10(AXIS_SCALE(mblk->spar_block[ib][is].dxy[X_SCALE])) * SCALE_ADJUST;
        g_popup_init_info(&(ci[id]));
        ci[id].type          = G_CHILD_SCALE;
        ci[id].id            = ADJUST_SCALEX_ID;
        ci[id].item          = f;
        ci[id].item_count    = num_ticks;
        ci[id].data          = tics2;
        ci[id].items_visible = num_labels;
        ci[id].horizontal    = FALSE;
        ci[id].select        = min_max_dec2;
        ci[id].func          = adjust_callback;
        ci[id].userdata  	= (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));
    
        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type          = G_CHILD_PUSHBUTTON;
        ci[id].id            = BUTTON_OFFSET + ADJUST_SCALEX_ID;
        ci[id].label         = "Reset";
        ci[id].func          = adjust_callback;
        ci[id].userdata  	= (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));
    
        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type          = G_CHILD_PUSHBUTTON;
        ci[id].id            = BUTTON2_OFFSET + ADJUST_SCALEX_ID;
        ci[id].label         = "Set";
        ci[id].func          = adjust_callback;
        ci[id].userdata  	= (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));
    
        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type          = G_CHILD_TEXTBOX;
        ci[id].id            = TEXT_OFFSET + ADJUST_SCALEX_ID;
        ci[id].item_count    = 40;
        ci[id].items_visible = 8;
        ci[id].label         = compute_label(ADJUST_SCALEX_ID,f);
        g_popup_add_child(cont_id, &(ci[id]));
    
        id++;
        g_popup_init_info(ci + id);
        ci[id].type       = G_CHILD_PANEL;
        ci[id].item       = FALSE;
        g_popup_add_child(cont_id, ci + id);
    
        /*
         * Move X sub-panel
         */
        id++;
        g_popup_init_info(ci + id);
        ci[id].type          = G_CHILD_PANEL;
        ci[id].item          = TRUE;
        ci[id].frame         = TRUE;
        ci[id].title 	     = "Move X";
        ci[id].horizontal    = FALSE;
        g_popup_add_child(cont_id, ci + id);
    
        id++;
        f = mblk->spar_block[ib][is].dxy[X_MOVE] * SCALE_ADJUST2;
        g_popup_init_info(&(ci[id]));
        ci[id].type          = G_CHILD_SCALE;
        ci[id].id            = ADJUST_MOVEX_ID;
        ci[id].item          = f;
        ci[id].item_count    = num_ticks;
        ci[id].data          = tics;
        ci[id].items_visible = num_labels;
        ci[id].horizontal    = FALSE;
        ci[id].select        = min_max_dec;
        ci[id].func          = adjust_callback;
        ci[id].userdata  	= (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));
    
        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type          = G_CHILD_PUSHBUTTON;
        ci[id].id            = BUTTON_OFFSET + ADJUST_MOVEX_ID;
        ci[id].label         = "Reset";
        ci[id].func          = adjust_callback;
        ci[id].userdata  	= (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));
    
        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type          = G_CHILD_PUSHBUTTON;
        ci[id].id            = BUTTON2_OFFSET + ADJUST_MOVEX_ID;
        ci[id].label         = "Set";
        ci[id].func          = adjust_callback;
        ci[id].userdata  	= (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));
    
        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type          = G_CHILD_TEXTBOX;
        ci[id].id            = TEXT_OFFSET + ADJUST_MOVEX_ID;
        ci[id].item_count    = 40;
        ci[id].items_visible = 8;
        ci[id].label         = compute_label(ADJUST_MOVEX_ID,f);
        g_popup_add_child(cont_id, &(ci[id]));
    
        id++;
        g_popup_init_info(ci + id);
        ci[id].type       = G_CHILD_PANEL;
        ci[id].item       = FALSE;
        g_popup_add_child(cont_id, ci + id);
#endif
        /*
         * Scale Y sub-panel
         */
        id++;
        g_popup_init_info(ci + id);
        ci[id].type          = G_CHILD_PANEL;
        ci[id].item          = TRUE;
        ci[id].frame         = TRUE;
        ci[id].title 	     = "Scale";
        /*
        ci[id].title 	     = "Scale Y";
        */
        ci[id].horizontal    = FALSE;
        g_popup_add_child(cont_id, ci + id);
    
        id++;
        f = -log10(AXIS_SCALE(mblk->spar_block[ib][is].dxy[Y_SCALE])) * SCALE_ADJUST;
        g_popup_init_info(&(ci[id]));
        ci[id].type          = G_CHILD_SCALE;
        ci[id].id            = ADJUST_SCALEY_ID;
        ci[id].item          = f;
        ci[id].item_count    = num_ticks;
        ci[id].data          = tics2;
        ci[id].items_visible = num_labels;
        ci[id].horizontal    = FALSE;
        ci[id].select        = min_max_dec2;
        ci[id].func          = adjust_callback;
        ci[id].userdata  	= (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));
    
        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type          = G_CHILD_PUSHBUTTON;
        ci[id].id            = BUTTON_OFFSET + ADJUST_SCALEY_ID;
        ci[id].label         = "Reset";
        ci[id].func          = adjust_callback;
        ci[id].userdata  	= (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));
    
        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type          = G_CHILD_PUSHBUTTON;
        ci[id].id            = BUTTON2_OFFSET + ADJUST_SCALEY_ID;
        ci[id].label         = "Set";
        ci[id].func          = adjust_callback;
        ci[id].userdata  	= (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));
    
        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type          = G_CHILD_TEXTBOX;
        ci[id].id            = TEXT_OFFSET + ADJUST_SCALEY_ID;
        ci[id].item_count    = 40;
        ci[id].items_visible = 8;
        ci[id].label         = compute_label(ADJUST_SCALEY_ID,f);
        g_popup_add_child(cont_id, &(ci[id]));
    
        id++;
        g_popup_init_info(ci + id);
        ci[id].type       = G_CHILD_PANEL;
        ci[id].item       = FALSE;
        g_popup_add_child(cont_id, ci + id);
    
        /*
         * Move Y sub-panel
         */
        id++;
        g_popup_init_info(ci + id);
        ci[id].type          = G_CHILD_PANEL;
        ci[id].item          = TRUE;
        ci[id].frame         = TRUE;
        /*
        ci[id].title 	     = "Move Y";
        */
        ci[id].title 	     = "Move";
        ci[id].horizontal    = FALSE;
        g_popup_add_child(cont_id, ci + id);
    
        id++;
        g_popup_init_info(&(ci[id]));
        f = mblk->spar_block[ib][is].dxy[Y_MOVE] * SCALE_ADJUST2;
        ci[id].type          = G_CHILD_SCALE;
        ci[id].id            = ADJUST_MOVEY_ID;
        ci[id].item          = f;
        ci[id].item_count    = num_ticks;
        ci[id].data          = tics;
        ci[id].items_visible = num_labels;
        ci[id].horizontal    = FALSE;
        ci[id].select        = min_max_dec;
        ci[id].func          = adjust_callback;
        ci[id].userdata  	= (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));
    
    
        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type          = G_CHILD_PUSHBUTTON;
        ci[id].id            = BUTTON_OFFSET + ADJUST_MOVEY_ID;
        ci[id].label         = "Reset";
        ci[id].func          = adjust_callback;
        ci[id].userdata  	= (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));
    
        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type          = G_CHILD_PUSHBUTTON;
        ci[id].id            = BUTTON2_OFFSET + ADJUST_MOVEY_ID;
        ci[id].label         = "Set";
        ci[id].func          = adjust_callback;
        ci[id].userdata  	= (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));
    
        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type          = G_CHILD_TEXTBOX;
        ci[id].id            = TEXT_OFFSET + ADJUST_MOVEY_ID;
        ci[id].item_count    = 40;
        ci[id].items_visible = 8;
        ci[id].label         = compute_label(ADJUST_MOVEY_ID,f);
        g_popup_add_child(cont_id, &(ci[id]));
    
        id++;
        g_popup_init_info(ci + id);
        ci[id].type       = G_CHILD_PANEL;
        ci[id].item       = FALSE;
        g_popup_add_child(cont_id, ci + id);
    
        /*
         * Close Scale panel
         */
        id++;
        g_popup_init_info(ci + id);
        ci[id].type       = G_CHILD_PANEL;
        ci[id].item       = FALSE;
        g_popup_add_child(cont_id, ci + id);
    
        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type         = G_CHILD_PUSHBUTTON;
        ci[id].id           = ADJUST_RESET_ID;
        /*
        ci[id].label        = "Reset X and Y, Move and Scale";
        */
        ci[id].label        = "Reset X, Y, Move and Scale";
        ci[id].func         = adjust_callback;
        ci[id].userdata  	= (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));
    
        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  		= G_CHILD_OPTIONMENU;
        ci[id].id    		= ADJUST_2DAUTO_ID;
        ci[id].item_count 	= 2;
        ci[id].item 		= mblk->spar_block[ib][is].disable_auto_scale_2d;
        ci[id].data 		= yes_no;
        ci[id].horizontal 	= TRUE;
        /*
        ci[id].label 		= "Disable \'Adjust Scaling\' for 2D";
        */
        ci[id].label 		= "Disable for 2D";
        ci[id].frame 		= TRUE;
        ci[id].func       	= adjust_callback;
        ci[id].userdata  	= (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_CANCEL;
        ci[id].func       = adjust_callback;
        ci[id].userdata   = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_OK;
        ci[id].func       = adjust_callback;
        ci[id].userdata   = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));
    
        assert(id < MAX_ID);
    }        
    g_menu_enable_item(mblk->info->mousebar_id, ID_MOUSE_SCALE, TRUE);
    g_popup_container_show(popinf->cont_id);
}




/*
 * Set axis units
 */
int psnd_popup_axis(int win_id, int plot_mode, SBLOCK *spar)
{
    int cont_id;
    G_POPUP_CHILDINFO ci[20];
    char *radio[] = { "None", 
                      "Channels   CHAN", 
                      "Hertz      HZ  ", 
                      "PPM        PPM ", 
                      "Seconds    SEC ", 
                      "Automatic    " } ;
    char *radio2[] = { "None", 
                       "Unit", 
                       "Hertz", 
                       "PPM", 
                       "Seconds" } ;
    int id=0;

    cont_id = g_popup_container_open(win_id, "Label Axis", 
                                     G_POPUP_WAIT );

    for (id=0;id<2;id++) {
        g_popup_init_info(&(ci[id]));
        ci[id].type       = G_CHILD_RADIOBOX;
        ci[id].id         = id;
        ci[id].data       = radio;
        if (id==0) {
            ci[id].title      = "Units X axis";
            ci[id].item       = spar[S_AXISX_MARK].show;
            ci[id].item_count = 6;
        }
        else {
            ci[id].title      = "Units Y axis";
            if (plot_mode < 2) {
                ci[id].data       = radio2;
                ci[id].item_count = 2;
                ci[id].item       = spar[S_AXISXY_MARK].show;
            }
            else {
                ci[id].item_count = 6;
                ci[id].item       = spar[S_AXISY_MARK].show;
            }
        }
        ci[id].frame      = TRUE;
        ci[id].horizontal = FALSE;
        g_popup_add_child(cont_id, &(ci[id]));
    }
    {
        static int check[] = { 0, 0 };
        char *check_label[] = { "1D/2D Mode", "2D Mode" };

        g_popup_init_info(&(ci[id]));
        ci[id].type       = G_CHILD_CHECKBOX;
        ci[id].id         = id;
        ci[id].title      = "Draw box";
        ci[id].frame      = TRUE;
        ci[id].item_count = 1;
        ci[id].data       = check_label;
        check[0]          = spar[S_BOX].show;
        ci[id].select     = check;
        g_popup_add_child(cont_id, &(ci[id]));
    }

    if (g_popup_container_show(cont_id)) {
        G_EVENT ui;
        for (id=0;id<2;id++) {
            if (id==0)
                spar[S_AXISX_MARK].show = ci[id].item;
            else {
                if (plot_mode < 2)
                    spar[S_AXISXY_MARK].show = ci[id].item;
                else
                    spar[S_AXISY_MARK].show = ci[id].item;
            }
        }
        spar[S_AXISX].show = (spar[S_AXISX_MARK].show != 0);
        spar[S_AXISY].show = (spar[S_AXISXY_MARK].show != 0);
        spar[S_AXISY].show = (spar[S_AXISY_MARK].show != 0);
        spar[S_BOX].show = ci[id].select[0];
        ui.win_id = win_id;
        ui.event = G_COMMAND;
        ui.keycode = PSND_AX;
        g_set_objectstatus(spar[S_GRID].obj_id, G_SLEEP);
        g_send_event(&ui);
        return TRUE;
    }
    return FALSE;
}


