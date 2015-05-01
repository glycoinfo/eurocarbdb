/********************************************************************/
/*                         psnd_phase2d.c                           */
/*                                                                  */
/* 2D Phase routines for the psnd program                           */
/* 1998, Albert van Kuik                                            */
/********************************************************************/
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
#include <assert.h>
#include "genplot.h"
#include "psnd.h"
#include "nmrtool.h"


#define PHASE_RADIO_ID	0
#define PHASE_LABEL_ID	1
#define PHASE_LABEL_ID2	2
#define PHASE_BUTTON_ID	3
#define PHASE_RESET_BUTTON_ID	4
#define PHASE_BOOKMARK_BUTTON_ID	5


void psnd_add_phase(MBLOCK *mblk, BOOKMARKINFO *bi, CBLOCK *cpar, int id)
{
    int i, s_code, vp_code;
    POPUP_INFO *popinf = mblk->popinf + POP_PHASE2D;
    
    if (!mblk->info->phase_2d) 
        return;

            
    /*
     * First spectrum sets phase direction
     */
    if (cpar->phase_direction == 0) {
        static char *list[] = {
/*
            "Pre-run AU script on data",
*/
            "Pre-run AU command on data",
            "Reset phase parameters",
            "Use Hilbert Transformation"
        };
        static int select[] = { 0, 0, 0, 0};
        int result, item_count = 3;
/*
        select[0] = FALSE;
        select[1] = FALSE;
        select[2] = TRUE;
        select[3] = FALSE;
 */
        select[0] = FALSE;
        select[1] = TRUE;
        select[2] = FALSE;

        result = g_popup_checkbox(mblk->info->win_id, "2D Phase Mode", 
                         "Setup", item_count, list, select);
        if (!result)
            return;

        cpar->doprocess = select[0]; /* + (select[1] << 1); */
        if (select[1])
            psnd_pz(mblk,bi->par[0]);
        bi->par[0]->hilbert = select[2];
        cpar->phase_direction = bi->par[0]->icrdir;
        psnd_show_2d_phase_data(mblk);
        psnd_show_2d_phase_direction(mblk, cpar->phase_direction);
    }
    else {
        if (cpar->phase_direction != bi->par[0]->icrdir) {
            psnd_printf(mblk,"Wrong direction\n");
            g_bell();
            return;
        }
    }
    for (i=0;i<MAX_DIM;i++)
        bi->par[i]->key = bi->key[i];

    psnd_nextrec(mblk,TRUE,0, bi->par, DAT);
    mblk->info->dimension_id = bi->par[0]->icrdir-1;
    switch (id) {
        case 0:
            s_code = S_PHASE1;
            vp_code = VP_PHASE1;
            break;
        case 1:
            s_code = S_PHASE2;
            vp_code = VP_PHASE2;
            break;
        case 2:
            s_code = S_PHASE3;
            vp_code = VP_PHASE3;
            break;
        default :
            return;
    }
    memcpy(cpar->xreal[id], DAT->xreal,
                sizeof(float) * bi->par[0]->nsiz);
    memcpy(cpar->ximag[id], DAT->ximag,
                sizeof(float) * bi->par[0]->nsiz);
    cpar->phase_size = bi->par[0]->nsiz;
    cpar->phase_data_added[id] = TRUE;
    if (cpar->phase_data_added[0] &&
        cpar->phase_data_added[1] &&
        cpar->phase_data_added[2])
            g_popup_enable_item(popinf->cont_id, PHASE_RADIO_ID, TRUE);
/*
    if (cpar->doprocess & (1 << 0)) {
        if (psnd_au_script(mblk)) {
            int isize = DAT->isize * sizeof(float);
            float *tmp;
            tmp = (float*) malloc(isize);
            memcpy(tmp, cpar->xreal[id],isize);
            memcpy(cpar->xreal[id], DAT->xreal,isize);
            memcpy(DAT->xreal, tmp,isize);
            memcpy(tmp, cpar->ximag[id],isize);
            memcpy(cpar->ximag[id], DAT->ximag,isize);
            memcpy(DAT->ximag, tmp,isize);
            free(tmp);
            cpar->phase_size = DAT->isize;
        }
    }
    if (cpar->doprocess & (1 << 1)) {
*/
    if (cpar->doprocess) {
            int isize;
            float *tmp;

            psnd_au(mblk);
            isize = DAT->isize * sizeof(float);
            tmp = (float*) malloc(isize);
            assert(tmp);
            memcpy(tmp, cpar->xreal[id],isize);
            memcpy(cpar->xreal[id], DAT->xreal,isize);
            memcpy(DAT->xreal, tmp,isize);
            memcpy(tmp, cpar->ximag[id],isize);
            memcpy(cpar->ximag[id], DAT->ximag,isize);
            memcpy(DAT->ximag, tmp,isize);
            free(tmp);
            cpar->phase_size = DAT->isize;
    }
    if (bi->par[0]->hilbert)
        hilbert(cpar->phase_size, cpar->xreal[id], cpar->ximag[id]);

    psnd_plot1d_phase(mblk,s_code, vp_code, cpar->phase_size, 
                       cpar->xreal[id], AUTO_SCALE_ON);
    psnd_getmaxworld(mblk,mblk->spar[s_code].vp_id,
                   &mblk->vp_store[vp_code].wx1,
                   &mblk->vp_store[vp_code].wx2,
                   &mblk->vp_store[vp_code].wy1,
                   &mblk->vp_store[vp_code].wy2);
    g_clear_viewport();
    g_plotall();
}


void psnd_show_2d_phase_data(MBLOCK *mblk)
{
    char *label;
    POPUP_INFO *popinf = mblk->popinf + POP_PHASE2D;

    if (!popinf->cont_id) 
        return;
    label = psnd_sprintf_temp("%4.1f %4.1f %d", PAR->ahold, PAR->bhold, PAR->ihold);
    g_popup_set_label(popinf->cont_id, PHASE_LABEL_ID, label);
}

void psnd_show_2d_phase_direction(MBLOCK *mblk, int dir)
{
    char *label;
    POPUP_INFO *popinf = mblk->popinf + POP_PHASE2D;

    if (!popinf->cont_id) 
        return;
    if (dir == 0)
        label = psnd_sprintf_temp("    ");
    else
        label = psnd_sprintf_temp("Direction %d", dir);
    g_popup_set_label(popinf->cont_id, PHASE_LABEL_ID2, label);
}

/*
 * Callback function of the phase block selection popup
 */
static void phase_callback(G_POPUP_CHILDINFO *ci)
{
    int i,size;
    MBLOCK *mblk = (MBLOCK *) ci->userdata;
    CBLOCK *cpar  = mblk->cpar_screen;
    POPUP_INFO *popinf = mblk->popinf + POP_PHASE2D;

    if (!mblk->info->phase_2d) 
        return;
    switch (ci->type) {
	case G_CHILD_PUSHBUTTON:
            switch (ci->id) {
                case PHASE_BUTTON_ID:
                    psnd_setparam(mblk, 0, NULL);
                    break;
                case PHASE_BOOKMARK_BUTTON_ID:
                    psnd_popup_bookmark(mblk);
                    break;
                case PHASE_RESET_BUTTON_ID:
                    cpar->phase_direction = 0;
                    g_newpage();
                    psnd_cp(mblk, CONTOUR_BLOCK);
                    size = DAT->isize;
                    for (i=0;i<MAX_DIM;i++) 
                        size = max(size, DAT->pars[i]->nsiz);
                    for (i=0;i<3;i++) {
                        memset(cpar->xreal[i], 0, sizeof(float) * size);
                        memset(cpar->ximag[i], 0, sizeof(float) * size);
                        cpar->phase_data_added[i] = FALSE;
                    }
                    psnd_show_2d_phase_direction(mblk, 0);
                    psnd_reset_connection(mblk,mblk->spar[S_CONTOUR].vp_id);
                    g_plotall();
                    g_popup_set_selection(popinf->cont_id, PHASE_RADIO_ID, 0);
                    g_popup_enable_item(popinf->cont_id, PHASE_RADIO_ID, FALSE);
                    break;
            }
            break;

	case G_CHILD_RADIOBOX:
            switch (ci->item) {
                case 1:
                    psnd_reset_connection(mblk,mblk->spar[S_PHASE1].vp_id);
                    break;

                case 2:
                    psnd_reset_connection(mblk,mblk->spar[S_PHASE2].vp_id);
                    break;

                case 3:
                    psnd_reset_connection(mblk,mblk->spar[S_PHASE3].vp_id);
                    break;

                case 0:
                    psnd_reset_connection(mblk,mblk->spar[S_CONTOUR].vp_id);
                    break;
            }
            break;

        case G_CHILD_OK:
        case G_CHILD_CANCEL:
            popinf->visible = FALSE;
            break;
    }
}

/*
 * Synchronize the radio buttons of the 'select phase block menu'
 * with the currently active phase block
 */
void psnd_set_button_phase_block(MBLOCK *mblk, int vp_id)
{
    int item_id = 0;
    POPUP_INFO *popinf = mblk->popinf + POP_PHASE2D;

    if (popinf->cont_id) {
        if (vp_id == mblk->spar[S_PHASE1].vp_id) 
            item_id = 1;
        else if (vp_id == mblk->spar[S_PHASE2].vp_id) 
            item_id = 2;
        else if (vp_id == mblk->spar[S_PHASE3].vp_id)  
            item_id = 3;
        g_popup_set_selection(popinf->cont_id, PHASE_RADIO_ID, item_id);
    }
}

/*
 * Select which phase block should be connected to the scroll bars
 */
static void popup_phase_selection(MBLOCK *mblk)
{
    G_POPUP_CHILDINFO ci[20];
    int id;
    static char *radiolist[] = {
         "2D viewport",
	 "Phase block 1",
	 "Phase block 2",
	 "Phase block 3",
    };
    static char title1[] = "Select Viewport";
    static char title2[] = "Set phase in:";
    static char title3[] = "Phase: P0 P1 I0";
    POPUP_INFO *popinf = mblk->popinf + POP_PHASE2D;

    if (popinf->visible)
        return;
    popinf->visible = TRUE;

    if (!popinf->cont_id) {
#ifdef _WIN32
        static char empty[] = "                  ", 
#else
        static char empty[] = "    ", 
#endif
             saveit[] = "Store Parameters",
             resetit[] = "Reset Phase Block";
        popinf->cont_id = g_popup_container_open(mblk->info->win_id, 
                        "Select", G_POPUP_KEEP|G_POPUP_SINGLEBUTTON);
        id=0;
        g_popup_init_info(&(ci[id]));
        ci[id].type       = G_CHILD_RADIOBOX;
        ci[id].id         = PHASE_RADIO_ID;
        ci[id].title      = title1;
        ci[id].frame      = TRUE;
        ci[id].item_count = 4;
        ci[id].data       = radiolist;
        ci[id].item       = 0;
        ci[id].horizontal = FALSE;
        ci[id].disabled   = TRUE;
        ci[id].func       = phase_callback;
        ci[id].userdata   = (void*) mblk;
        g_popup_add_child(popinf->cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type       = G_CHILD_LABEL;
        ci[id].id         = PHASE_LABEL_ID2;
        ci[id].title      = title2;
        ci[id].label      = empty;
        ci[id].frame      = TRUE;
        g_popup_add_child(popinf->cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type       = G_CHILD_LABEL;
        ci[id].id         = PHASE_LABEL_ID;
        ci[id].title      = title3;
        ci[id].label      = empty;
        ci[id].frame      = TRUE;
        g_popup_add_child(popinf->cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type       = G_CHILD_PUSHBUTTON;
        ci[id].id         = PHASE_BUTTON_ID;
        ci[id].label      = saveit;
        ci[id].frame      = FALSE;
        ci[id].func       = phase_callback;
        ci[id].userdata   = (void*) mblk;
        g_popup_add_child(popinf->cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type       = G_CHILD_PUSHBUTTON;
        ci[id].id         = PHASE_RESET_BUTTON_ID;
        ci[id].label      = resetit;
        ci[id].frame      = FALSE;
        ci[id].func       = phase_callback;
        ci[id].userdata   = (void*) mblk;
        g_popup_add_child(popinf->cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type       = G_CHILD_PUSHBUTTON;
        ci[id].id         = PHASE_BOOKMARK_BUTTON_ID;
        ci[id].label      = "Popup Bookmarks";
        ci[id].frame      = FALSE;
        ci[id].func       = phase_callback;
        ci[id].userdata   = (void*) mblk;
        g_popup_add_child(popinf->cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_OK;
        ci[id].func  = phase_callback;
        ci[id].userdata      = (void*) mblk;
        g_popup_add_child(popinf->cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_CANCEL;
        ci[id].func  = phase_callback;
        ci[id].userdata      = (void*) mblk;
        g_popup_add_child(popinf->cont_id, &(ci[id]));
    }
    g_popup_container_show(popinf->cont_id);

}

void psnd_popup_2dphase_selectionbox(MBLOCK *mblk)
{
    POPUP_INFO *popinf = mblk->popinf + POP_PHASE2D;

    if (!mblk->info->phase_2d) 
        return;
    popup_phase_selection(mblk);
}

void psnd_set_phase2d(MBLOCK *mblk)
{
    int i;
    static int store_dimension_id;
    CBLOCK *cpar = mblk->cpar_screen;
    POPUP_INFO *popinf = mblk->popinf + POP_PHASE2D;

    if (mblk->info->plot_mode == PLOT_1D)
        return;

    cpar->phase_direction = 0;
    if (!mblk->info->phase_2d) {
        float vp_x, vp_y;
        int size;

        mblk->info->phase_2d = TRUE;
        store_dimension_id = mblk->info->dimension_id;
        mblk->vp_store[VP_PHASE_2D].store_id = -1;
        mblk->vp_store[VP_PHASE1].store_id = -1;
        mblk->vp_store[VP_PHASE2].store_id = -1;
        mblk->vp_store[VP_PHASE3].store_id = -1;
        mblk->spar[S_BOX].show             = FALSE;
        mblk->spar[S_PHASE1].show          = TRUE;
        mblk->spar[S_PHASE2].show          = TRUE;
        mblk->spar[S_PHASE3].show          = TRUE;
        mblk->spar[S_PHASE1_BORDER].show   = TRUE;
        mblk->spar[S_PHASE2_BORDER].show   = TRUE;
        mblk->spar[S_PHASE3_BORDER].show   = TRUE;
        mblk->spar[S_CONTOUR_BORDER].show  = TRUE;
        mblk->spar[S_CONTOUR].vp_group = VP_PHASE_2D;
        mblk->spar[S_REAL].vp_group = VP_PHASE_2D;
        mblk->spar[S_REALSWAP].vp_group = VP_PHASE_2D;
        psnd_set_vp_group(mblk,VP_PHASE_2D);
        psnd_set_vp_id(mblk,mblk->spar[S_CONTOUR].vp_id);
        psnd_link_viewport(mblk,mblk->spar[S_CONTOUR].vp_id, VP_PHASE_2D);
        psnd_link_viewport(mblk,mblk->spar[S_REAL].vp_id, VP_PHASE_2D);
        psnd_link_viewport(mblk,mblk->spar[S_REALSWAP].vp_id, VP_PHASE_2D);
        g_newpage();
        psnd_cp(mblk,CONTOUR_BLOCK);
        psnd_getmaxworld(mblk,mblk->spar[S_CONTOUR].vp_id,
                   &mblk->vp_store[VP_PHASE_2D].wx1,
                   &mblk->vp_store[VP_PHASE_2D].wx2,
                   &mblk->vp_store[VP_PHASE_2D].wy1,
                   &mblk->vp_store[VP_PHASE_2D].wy2);
        size = DAT->isize;
        for (i=0;i<MAX_DIM;i++) 
            size = max(size, DAT->pars[i]->nsiz);
        for (i=0;i<3;i++) {
            cpar->xreal[i] = (float*) calloc(sizeof(float) , size);
            cpar->ximag[i] = (float*) calloc(sizeof(float) , size);
            cpar->phase_data_added[i] = FALSE;
        }
        popup_phase_selection(mblk);
        psnd_reset_connection(mblk,mblk->spar[S_CONTOUR].vp_id);
        g_popup_container_show(popinf->cont_id);
        /*
        mblk->info->phase_id = g_menu_create_floating(mblk->info->win_id);
        vp_x = mblk->vp_store[VP_PHASE1].x1 / (float)mblk->info->ismaxx;
        vp_y = mblk->vp_store[VP_PHASE1].y2 / (float)mblk->info->ismaxy;
        g_menu_append_floating_button(mblk->info->phase_id, "Off", PSND_SELECT1, vp_x, vp_y);
        vp_x = mblk->vp_store[VP_PHASE2].x1 / (float)mblk->info->ismaxx;
        vp_y = mblk->vp_store[VP_PHASE2].y2 / (float)mblk->info->ismaxy;
        g_menu_append_floating_button(mblk->info->phase_id, "Off", PSND_SELECT2, vp_x, vp_y);
        vp_x = mblk->vp_store[VP_PHASE3].x1 / (float)mblk->info->ismaxx;
        vp_y = mblk->vp_store[VP_PHASE3].y2 / (float)mblk->info->ismaxy;
        g_menu_append_floating_button(mblk->info->phase_id, "Off", PSND_SELECT3, vp_x, vp_y);
        vp_x = mblk->vp_store[VP_PHASE_2D].x1 / (float)mblk->info->ismaxx;
        vp_y = mblk->vp_store[VP_PHASE_2D].y2 / (float)mblk->info->ismaxy;
        g_menu_append_floating_button(mblk->info->phase_id, "Off", PSND_SELECT4, vp_x, vp_y);
        */

    }
    else {
        mblk->info->phase_2d = FALSE;
        mblk->info->dimension_id = store_dimension_id;

        if (popinf->cont_id) {
            g_popup_set_selection(popinf->cont_id, PHASE_RADIO_ID, 0);
            g_popup_enable_item(popinf->cont_id, PHASE_RADIO_ID ,FALSE);
            g_popup_container_close(popinf->cont_id);
        }
        g_scrollbar_reset(mblk->info->win_id);
        g_scrollbar_disconnect(mblk->info->win_id);
        psnd_reset_connection(mblk,mblk->spar[S_CONTOUR].vp_id);
        /*
        g_menu_destroy_floating(mblk->info->win_id);
        */
        mblk->spar[S_BOX].show             = TRUE;
        mblk->spar[S_PHASE1].show          = FALSE;
        mblk->spar[S_PHASE2].show          = FALSE;
        mblk->spar[S_PHASE3].show          = FALSE;
        mblk->spar[S_PHASE1_BORDER].show   = FALSE;
        mblk->spar[S_PHASE2_BORDER].show   = FALSE;
        mblk->spar[S_PHASE3_BORDER].show   = FALSE;
        mblk->spar[S_CONTOUR_BORDER].show  = FALSE;
        psnd_set_vp_group(mblk,VP_MAIN);
        mblk->spar[S_CONTOUR].vp_group = VP_MAIN;
        mblk->spar[S_REAL].vp_group = VP_MAIN;
        mblk->spar[S_REALSWAP].vp_group = VP_MAIN;
        psnd_set_vp_id(mblk,mblk->spar[S_CONTOUR].vp_id);
        psnd_link_viewport(mblk,mblk->spar[S_CONTOUR].vp_id, VP_MAIN);
        psnd_link_viewport(mblk,mblk->spar[S_REAL].vp_id, VP_MAIN);
        psnd_link_viewport(mblk,mblk->spar[S_REALSWAP].vp_id, VP_MAIN);
        for (i=0;i<3;i++) {
           free(cpar->xreal[i]);
           free(cpar->ximag[i]);
           cpar->xreal[i] = NULL;
           cpar->ximag[i] = NULL;
        }
        g_newpage();
        psnd_cp(mblk,CONTOUR_BLOCK);
        psnd_getmaxworld(mblk,mblk->spar[S_CONTOUR].vp_id,
                   &mblk->vp_store[VP_MAIN].wx1,
                   &mblk->vp_store[VP_MAIN].wx2,
                   &mblk->vp_store[VP_MAIN].wy1,
                   &mblk->vp_store[VP_MAIN].wy2);
    }
    g_menu_set_toggle(mblk->info->bar_id, PSND_PHASE_2D, popinf->cont_id);
#ifndef PIXBUTTONS
    g_menu_set_label(mblk->info->bar_id, PSND_PHASE_2D, "Phase");
#endif
}

