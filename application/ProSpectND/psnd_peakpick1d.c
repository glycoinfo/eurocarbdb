/********************************************************************/
/*                          psnd_peakpick1d.c                       */
/*                                                                  */
/* 1998, Albert van Kuik                                            */
/********************************************************************/
#include <stdio.h>
#include <stdlib.h>
#include <ctype.h>
#include <string.h>
#include <stdarg.h>
#include <math.h>
#include <assert.h>
#include "genplot.h"
#include "psnd.h"
#include "nmrtool.h"

enum peakpick1d_types {
    PEAKPICK_LEFT=10,
    PEAKPICK_RIGHT,
    PEAKPICK_BOTTOM,
    PEAKPICK_TOP,
    PEAKPICK_MOUSE
};


static void update_labels(MBLOCK *mblk)
{
    POPUP_INFO *popinf = mblk->popinf + POP_PEAKPICK;

    if (popinf->cont_id) {
        char *label;
        label = psnd_sprintf_temp("%d", mblk->ppinf->left);
        g_popup_set_label(popinf->cont_id, PEAKPICK_LEFT, label);
        label = psnd_sprintf_temp("%d", mblk->ppinf->right);
        g_popup_set_label(popinf->cont_id, PEAKPICK_RIGHT, label);
        label = psnd_sprintf_temp("%.2e", mblk->ppinf->bottom);
        g_popup_set_label(popinf->cont_id, PEAKPICK_BOTTOM, label);
        label = psnd_sprintf_temp("%.2e", mblk->ppinf->top);
        g_popup_set_label(popinf->cont_id, PEAKPICK_TOP, label);
    }
}

static void peakpick_callback(G_POPUP_CHILDINFO *ci)
{
    MBLOCK *mblk = (MBLOCK*) ci->userdata;
    POPUP_INFO *popinf = mblk->popinf + POP_PEAKPICK;
    
    switch (ci->type) {
        case G_CHILD_OK:
            psnd_peakpick(mblk, mblk->ppinf->left,mblk->ppinf->bottom,
                           mblk->ppinf->right,mblk->ppinf->top);
        case G_CHILD_CANCEL:
            popinf->visible = FALSE;
            return;
    }
    switch (ci->id) {
        case PEAKPICK_LEFT:
            mblk->ppinf->left = psnd_scan_integer(ci->label);
            break;
        case PEAKPICK_RIGHT:
            mblk->ppinf->right = psnd_scan_integer(ci->label);
            break;
        case PEAKPICK_BOTTOM:
            mblk->ppinf->bottom = psnd_scan_float(ci->label);
            break;
        case PEAKPICK_TOP:
            mblk->ppinf->top = psnd_scan_float(ci->label);
            break;
        case PEAKPICK_MOUSE:
            {
                psnd_set_cursormode(mblk, 0, MOUSE_PEAKPICK);
            }
            break;
    }
}

/*
 * Popup menu 
 */
void psnd_peakpick1d_popup(MBLOCK *mblk)
{
    int i, ok;
    static G_POPUP_CHILDINFO ci[20];
    int id;
    char *label;
    DBLOCK *dat =  DAT;
    POPUP_INFO *popinf = mblk->popinf + POP_PEAKPICK;

    if (popinf->visible)
        return;
    popinf->visible = TRUE;
    if (!popinf->cont_id) {
        int cont_id;
        if (!mblk->ppinf->bottom) {
            mblk->ppinf->left   = 1;
            mblk->ppinf->right  = dat->isize;
            mblk->ppinf->bottom = 5e6;
            mblk->ppinf->top    = 5e10;
        }
        
        cont_id = g_popup_container_open(mblk->info->win_id, 
                           "Peak Pick 1D",  G_POPUP_KEEP);
        popinf->cont_id = cont_id;
        label = psnd_sprintf_temp("%d", mblk->ppinf->left);
        id=0;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_TEXTBOX;
        ci[id].id            = PEAKPICK_LEFT;
        ci[id].title         = "Left ";
        ci[id].func          = peakpick_callback;
        ci[id].userdata      = (void*) mblk;
        ci[id].item_count    = 20;
        ci[id].items_visible = 10;
        ci[id].label         = label;
        ci[id].horizontal    = TRUE;
        g_popup_add_child(cont_id, &(ci[id]));

        label = psnd_sprintf_temp("%d", mblk->ppinf->right);
        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_TEXTBOX;
        ci[id].id            = PEAKPICK_RIGHT;
        ci[id].title         = "Right";
        ci[id].func          = peakpick_callback;
        ci[id].userdata      = (void*) mblk;
        ci[id].item_count    = 20;
        ci[id].items_visible = 10;
        ci[id].label         = label;
        ci[id].horizontal    = TRUE;
        g_popup_add_child(cont_id, &(ci[id]));

        label = psnd_sprintf_temp("%.2e", mblk->ppinf->top);
        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_TEXTBOX;
        ci[id].id            = PEAKPICK_TOP;
        ci[id].title         = "Upper";
        ci[id].func          = peakpick_callback;
        ci[id].userdata      = (void*) mblk;
        ci[id].item_count    = 20;
        ci[id].items_visible = 10;
        ci[id].label         = label;
        ci[id].horizontal    = TRUE;
        g_popup_add_child(cont_id, &(ci[id]));

        label = psnd_sprintf_temp("%.2e", mblk->ppinf->bottom);
        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_TEXTBOX;
        ci[id].id            = PEAKPICK_BOTTOM;
        ci[id].title         = "Lower";
        ci[id].func          = peakpick_callback;
        ci[id].userdata      = (void*) mblk;
        ci[id].item_count    = 20;
        ci[id].items_visible = 10;
        ci[id].label         = label;
        ci[id].horizontal    = TRUE;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_PUSHBUTTON;
        ci[id].id    = PEAKPICK_MOUSE;
        ci[id].label = "Grab Mouse Button 1";
        ci[id].func  = peakpick_callback;
        ci[id].userdata      = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_OK;
        ci[id].func  = peakpick_callback;
        ci[id].userdata      = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_CANCEL;
        ci[id].func  = peakpick_callback;
        ci[id].userdata      = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));
    }
    g_popup_container_show(popinf->cont_id) ;
}


#define MAXPEAKS	200
#define MAXPEAKY	6
void psnd_peakpick(MBLOCK *mblk, float x1, float y1, float x2, float y2)
{
    int i, npeaks, maxpks = MAXPEAKS, ipeaks[MAXPEAKS];
    int size, lim[2];
    float ythigh[2]; 
    SBLOCK *spar= mblk->spar;
    PBLOCK *par = PAR;
    DBLOCK *dat = DAT;
    
    size = dat->isize;
    if (x1 != x2) {
        int i1,i2;
        if (y2 == 0.0)
            y2 = 1e20;
        ythigh[0] = min(y1,y2);
        ythigh[1] = max(y1,y2);
        i1 = round(x1);
        i2 = round(x2);
        lim[0] = min(i1,i2);
        lim[1] = max(i1,i2);
    }
    else {
        lim[0] = 1;
        lim[1] = dat->isize;
        ythigh[0] = mblk->ppinf->bottom;
        ythigh[1] = mblk->ppinf->top;
        if (psnd_ivalin(mblk," il,ir  ?", 2, lim) == 0)
            return;
        if (psnd_rvalin(mblk," height,max ?",2, ythigh) == 0)
            return;
    }
    lim[0] = max(1, lim[0]);
    lim[1] = max(1, lim[1]);
    lim[0] = min(dat->isize, lim[0]);
    lim[1] = min(dat->isize, lim[1]);

    mblk->ppinf->left   = lim[0];
    mblk->ppinf->right  = lim[1];
    mblk->ppinf->bottom = ythigh[0];
    mblk->ppinf->top    = ythigh[1];
    update_labels(mblk);
    
    npeaks = npeaks1d(dat->xreal,
                      lim[0],
                      lim[1],
                      mblk->ppinf->bottom,
                      mblk->ppinf->top,
                      maxpks,
                      ipeaks);
    if (npeaks > 0) {
        float wx1,wx2,wy1,wy2, dy,dy2,dy4;
        float peak_y[MAXPEAKY];
        psnd_getmaxworld(mblk,spar[S_PEAKPICK].vp_id,&wx1,&wx2,&wy1,&wy2);
        dy = wy2-wy1;
        dy2 = dy/20;
        dy4 = dy2/2;
        g_append_object(spar[S_PEAKPICK].obj_id);
        g_select_viewport(spar[S_PEAKPICK].vp_id);
        g_set_foreground(spar[S_PEAKPICK].color);

        for (i=0;i<MAXPEAKY;i++) 
            peak_y[i] = 0.0;
        for (i=0;i<npeaks;i++) {
            char label[100];
            int j, ix = ipeaks[i];
            float yy = dat->xreal[ix-1] + dy2 + dy4;
            yy = min(wy2-dy4, yy);
            /*
             * Calculate label placement
             * First shift old label positions in buffer
             */
            for (j=1;j<MAXPEAKY;j++)
                peak_y[j-1] = peak_y[j];
            /*
             * Add position of current label at the end
             */
            peak_y[MAXPEAKY-1] = yy;
            /*
             * If labels overlap, shift last label position
             */
            for (j=MAXPEAKY-2;j>=0;j--) {
                float pmin = peak_y[MAXPEAKY-1] - dy2/4;
                float pmax = peak_y[MAXPEAKY-1] + dy2/4;
                if (peak_y[j] < pmin || peak_y[j] > pmax)
                    continue;
                peak_y[MAXPEAKY-1] += dy4;
                j=MAXPEAKY-1;
            }
            /*
             * Draw tick mark
             */
            g_moveto((float) ix, yy-dy2);
            g_lineto((float) ix, yy);
            /*
             * Channels to ppm
             */
            psnd_calc_pos((float) ipeaks[i], par->xref, 
                            par->aref, par->bref, dat->irc, dat->isspec,
                            spar[S_AXISX_MARK].show , size, 
                            dat->sw, par->sfd, label, NULL);

            if (!(i % 6)) {
                if (i)
                    psnd_printf(mblk,"\n");
                psnd_printf(mblk," peaks : ");
            }

            psnd_printf(mblk,"%s ", label);
            /*
             * plot label
             */
            g_moveto((float) ix, peak_y[MAXPEAKY-1]);
            g_label(label);
        }
        psnd_printf(mblk,"\n");

        g_close_object(spar[S_PEAKPICK].obj_id);
        g_plotall();
    }
}

