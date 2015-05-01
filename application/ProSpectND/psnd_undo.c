/********************************************************************/
/*                             psnd_undo.c                          */
/*                                                                  */
/* Keep a history of records read by the program                    */
/* Undo/redo past record reads                                      */
/* 1998, Albert van Kuik                                            */
/*                                                                  */
/********************************************************************/
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "genplot.h"
#include "psnd.h"



static int is_undo(DBLOCK *dat)
{
    return (!(dat->q_undohead == dat->q_undotail));
}

static int is_redo(DBLOCK *dat)
{
    return (!(dat->q_redo == dat->q_undotail));
}


void psnd_init_undo(DBLOCK *dat)
{
    dat->q_lock = dat->q_redo = dat->q_undohead = dat->q_undotail = 0;
}


void psnd_push_undo(UNDOINFO *undo, DBLOCK *dat)
{
    if (undo == NULL || dat->q_lock)
        return;
    dat->q_undotail++;
    if (dat->q_undotail > UNDOQMAX) 
        dat->q_undotail = 0;
    if (dat->q_undotail == dat->q_undohead) {
        (dat->q_undohead == UNDOQMAX) ? 
            (dat->q_undohead = 0) : (dat->q_undohead++);
    }
    dat->q_redo = dat->q_undotail;
    memcpy(&(dat->undo_queue[dat->q_undotail]), undo, sizeof(UNDOINFO));
}

static UNDOINFO *pop_undo(DBLOCK *dat)
{
    UNDOINFO *ui;
    int savetail = dat->q_undotail;

    if (!is_undo(dat))
        return NULL;
    dat->q_undotail--;
    if (dat->q_undotail < 0) 
        dat->q_undotail = UNDOQMAX;
    if (!is_undo(dat)) {
        dat->q_undotail = savetail;
        return NULL;
    }
    ui = &(dat->undo_queue[dat->q_undotail]);
    return ui;
}

static UNDOINFO *pop_redo(DBLOCK *dat)
{
    UNDOINFO *ui;
    int savetail = dat->q_undotail;

    if (!is_redo(dat))
        return NULL;
    dat->q_undotail++;
    if (dat->q_undotail > UNDOQMAX)
        dat->q_undotail = 0;
    if (!is_undo(dat)) {
        dat->q_undotail = savetail;
        return NULL;
    }
    ui = &(dat->undo_queue[dat->q_undotail]);
    return ui;
}

/*
 * Put psnd commands on the stack
 */

int psnd_poprec(MBLOCK *mblk, int mode)
{
    int i;
    UNDOINFO *rst;
    PBLOCK *par = PAR;
    DBLOCK *dat = DAT;

    if (mode == PSND_UNDO) 
        rst = pop_undo(dat);
    else 
        rst = pop_redo(dat);
    if (rst == NULL) {
        g_bell();
        return par->icrdir - 1;
    }
    for (i=0;i<MAX_DIM;i++)
        rst->par[i]->key = rst->key[i];

    dat->q_lock = TRUE;
    psnd_nextrec(mblk,TRUE,0, rst->par, dat);
    dat->q_lock = FALSE;

    return  rst->par[0]->icrdir - 1;
}

/*
 * Calculate the keys for the next record
 * in direction row or column
 *
 * i0 is the key increment. Can be 0 or -1
 * returns the direcion of the record
 */
int psnd_rec(MBLOCK *mblk, int verbose, int direction, int i0, int i1)
{
    int i, ierr;
    PBLOCK *pars[MAX_DIM];
    PBLOCK *par = PAR;
    DBLOCK *dat = DAT;

    if (dat->ityp <= 1) {
        g_bell();
        return par->icrdir - 1;
    }
    for (i=0;i<MAX_DIM;i++)
        pars[i] = dat->pars[i];
    switch (direction) {
    case DIRECTION_SWITCH:
        if (par->icrdir == dat->access[0]) {
            pars[0] = dat->pars[1];
            pars[1] = dat->pars[0];
        }
        else {
            pars[0] = dat->pars[0];
            pars[1] = dat->pars[1];
        }
        break;
    case DIRECTION_ROW:
        pars[0] = dat->pars[0];
        pars[1] = dat->pars[1];
        break;
    case DIRECTION_COLUMN:
        pars[0] = dat->pars[1];
        pars[1] = dat->pars[0];
        break;
    case DIRECTION_PERPENDICULAR:
        pars[0] = dat->pars[2];
        pars[1] = dat->pars[0];
        pars[2] = dat->pars[1];
        pars[2]->key = min(i1, pars[2]->nsiz);
        pars[2]->key = max(1,  pars[2]->key);
        break;
    default:
        return par->icrdir -1;
    }

    pars[1]->key = min(i0, pars[1]->nsiz);
    pars[1]->key = max(1,  pars[1]->key);

    psnd_nextrec(mblk,verbose, 0, pars, dat);

    return pars[0]->icrdir - 1;
}


void psnd_adjust_labels_to_new_direction(MBLOCK *mblk)
{
    psnd_refresh_phase_labels(mblk);
    psnd_refresh_all_baseline_labels(mblk);
    psnd_refresh_all_window_labels(mblk);
    psnd_refresh_all_ft_labels(mblk);
    psnd_refresh_all_waterfit_labels(mblk);
    psnd_refresh_all_watwa_labels(mblk);
    psnd_refresh_all_lpc_labels(mblk);
}

