/********************************************************************/
/*                       psnd_plane_undo.c                          */
/*                                                                  */
/* Keep a history of planes read by the program                     */
/* Undo/redo past plane reads                                       */
/* 2000, Albert van Kuik                                            */
/*                                                                  */
/********************************************************************/
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "genplot.h"
#include "psnd.h"



static int is_plane_undo(DBLOCK *dat)
{
    return (!(dat->q_plane_undohead == dat->q_plane_undotail));
}

static int is_plane_redo(DBLOCK *dat)
{
    return (!(dat->q_plane_redo == dat->q_plane_undotail));
}


void psnd_init_plane_undo(DBLOCK *dat)
{
    dat->q_plane_lock = dat->q_plane_redo = 
        dat->q_plane_undohead = dat->q_plane_undotail = 0;
}


void psnd_push_plane_undo(UNDOPLANEINFO *undo, DBLOCK *dat)
{
    if (undo == NULL || dat->q_plane_lock)
        return;
    dat->q_plane_undotail++;
    if (dat->q_plane_undotail > UNDOQMAX) 
        dat->q_plane_undotail = 0;
    if (dat->q_plane_undotail == dat->q_plane_undohead) {
        (dat->q_plane_undohead == UNDOQMAX) ? 
            (dat->q_plane_undohead = 0) : (dat->q_plane_undohead++);
    }
    dat->q_plane_redo = dat->q_plane_undotail;
    memcpy(&(dat->undo_plane_queue[dat->q_plane_undotail]), 
                undo, sizeof(UNDOPLANEINFO));
}

static UNDOPLANEINFO *pop_plane_undo(DBLOCK *dat)
{
    UNDOPLANEINFO *ui;
    int savetail = dat->q_plane_undotail;

    if (!is_plane_undo(dat))
        return NULL;
    dat->q_plane_undotail--;
    if (dat->q_plane_undotail < 0) 
        dat->q_plane_undotail = UNDOQMAX;
    if (!is_plane_undo(dat)) {
        dat->q_plane_undotail = savetail;
        return NULL;
    }
    ui = &(dat->undo_plane_queue[dat->q_plane_undotail]);
    return ui;
}

static UNDOPLANEINFO *pop_plane_redo(DBLOCK *dat)
{
    UNDOPLANEINFO *ui;
    int savetail = dat->q_plane_undotail;

    if (!is_plane_redo(dat))
        return NULL;
    dat->q_plane_undotail++;
    if (dat->q_plane_undotail > UNDOQMAX)
        dat->q_plane_undotail = 0;
    if (!is_plane_undo(dat)) {
        dat->q_plane_undotail = savetail;
        return NULL;
    }
    ui = &(dat->undo_plane_queue[dat->q_plane_undotail]);
    return ui;
}

/*
 * Put psnd commands on the stack
 */

int psnd_pop_plane(MBLOCK *mblk, int undo_mode, int contour_mode)
{
    int i;
    UNDOPLANEINFO *rst;
    PBLOCK *par = PAR;
    DBLOCK *dat = DAT;
    int keys[MAX_DIM];

    if (undo_mode == PSND_PLANE_UNDO) 
        rst = pop_plane_undo(dat);
    else 
        rst = pop_plane_redo(dat);
    if (rst == NULL) {
        g_bell();
        return par->icrdir - 1;
    }
    for (i=0;i<MAX_DIM;i++)
        rst->par[i]->key = rst->key[i];
    for (i=0;i<dat->ityp;i++) {
        if (rst->access[i] != dat->access[i]) {
            if (!psnd_direction(mblk, 0, NULL, 3, rst->access))
                return rst->par[0]->icrdir - 1;
            break;
        }
    }
    dat->q_plane_lock = TRUE;
    psnd_plane(mblk,TRUE, PLANE_READ, DAT->pars[2]->key,contour_mode);
    dat->q_plane_lock = FALSE;

    return  rst->par[0]->icrdir - 1;
}

/*
 * Calculate the keys for the next record
 * in direction row or column
 *
 * i0 is the key increment. Can be 0 or -1
 * returns the direcion of the record
 */
int psnd_plane(MBLOCK *mblk, int verbose, int plane_mode, int key,
                int contour_mode)
{
    UNDOPLANEINFO rst;
    int k,keys[MAX_DIM];
    int use_keys = FALSE;

    if (DAT->ityp < 2) {
        g_bell();
        return FALSE;
    }
    if (DAT->ityp <= 2 && plane_mode != PLANE_THIS) {
        g_bell();
        return FALSE;
    }
    switch (plane_mode) {
    case PLANE_UP:
        if (DAT->pars[2]->key >= DAT->pars[2]->nsiz)
            return FALSE;
        for (k=0;k<DAT->ityp;k++)
            keys[k] = DAT->pars[k]->key;
        keys[2] = DAT->pars[2]->key + 1;
        use_keys = TRUE;
        break;

    case PLANE_DOWN:
        if (DAT->pars[2]->key == 1)
            return FALSE;
        for (k=0;k<DAT->ityp;k++)
            keys[k] = DAT->pars[k]->key;
        keys[2] = DAT->pars[2]->key - 1;
        use_keys = TRUE;
        break;

    case PLANE_READ:
        if (key > DAT->pars[2]->nsiz || key <= 0)
            return FALSE;
        for (k=0;k<DAT->ityp;k++)
            keys[k] = DAT->pars[k]->key;
        keys[2] = key;
        use_keys = TRUE;
        break;

    case PLANE_RESET:
        for (k=0;k<MAX_DIM;k++)
            keys[k] = 1;
        use_keys = TRUE;
        break;
        
    case PLANE_THIS:
    default:
        break;
    }

   
    if (use_keys)
        if (psnd_rn(mblk, DAT->ityp-1,keys+1,DAT) == FALSE) 
            return FALSE;
            
    if (mblk->info->phase_2d)
        psnd_set_phase2d(mblk);

    /*
     * This hack is required to switch directions in 2D mode
     */
    {
    int verbose_store = mblk->info->verbose;
    mblk->info->verbose = FALSE;
    psnd_contour_mode(mblk, FALSE, mblk->cpar_screen);
    psnd_contour_mode(mblk, TRUE,  mblk->cpar_screen);
    mblk->info->verbose = verbose_store;
    }

    mblk->info->dimension_id = DAT->access[0] - 1;
    psnd_cp(mblk, contour_mode);
    if (verbose)
        psnd_printf(mblk, "  Plane %d\n", DAT->pars[2]->key);
    /*
     * Store current plane info in undo structure
     */
    for (k=0;k<MAX_DIM;k++) {
        rst.key[k]    = DAT->pars[k]->key;
        rst.access[k] = DAT->access[k];
        rst.par[k]    = DAT->pars[k];
    }
    psnd_push_plane_undo(&rst, DAT);

    return TRUE;
}


