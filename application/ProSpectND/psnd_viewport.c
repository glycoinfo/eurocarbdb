/********************************************************************/
/*                         psnd_viewport.c                          */
/*                                                                  */
/* Viewport routines for the psnd program                           */
/* Set viewports and link viewports to the scrollbars               */
/* 1997, Albert van Kuik                                            */
/********************************************************************/
#include <stdio.h>
#include <stdlib.h>
#include <ctype.h>
#include <string.h>
#include <math.h>
#include <limits.h>
#include <assert.h>
#include "genplot.h"
#include "psnd.h"


#define WINSIZEX		700
#define WINSIZEY		500
#define PHASE_2D_BORDERSIZE	4
#define BORDER_FRAC 		0.05
#define BORDER_FRAC2 		0.03


/*
 * Set the currently active viewport group ID
 */
void psnd_set_vp_group(MBLOCK *mblk, int vp_group)
{
    mblk->info->global_vp_group_id = vp_group;
}

void psnd_set_vp_id(MBLOCK *mblk, int vp_id)
{
    mblk->info->global_vp_id = vp_id;
}

/*
 * Get the currently active viewport group ID
 */
int psnd_get_vp_group(MBLOCK *mblk)
{
    return mblk->info->global_vp_group_id;
}

int psnd_get_vp_id(MBLOCK *mblk)
{
    return mblk->info->global_vp_id;
}

/*
 * Lookup the group ID of this viewport
 */
static int vp_id2group(MBLOCK *mblk, int vp_id)
{
    int i;
    
    for (i=0;i<S_MAX;i++)
        if (mblk->spar[i].vp_id == vp_id)
            return mblk->spar[i].vp_group;
    return psnd_get_vp_group(mblk);
}

/*
 * Set the dominant viewport for the current block
 */
int psnd_set_dominant_viewport(MBLOCK *mblk)
{
    int i, s_ids[] = { S_REAL, S_IMAG, S_BUFR1, S_BUFR2, S_BASELN, S_WINDOW,-1};
    SBLOCK *spar0;
    
    if (mblk->info->plot_mode == PLOT_2D) {
        return psnd_get_vp_id(mblk);
    }
    spar0 = mblk->spar_block[mblk->info->block_id];
    for (i=0;s_ids[i] != -1;i++) {
        if (spar0[s_ids[i]].show) {
            psnd_set_vp_group(mblk, vp_id2group(mblk,spar0[s_ids[i]].vp_id));
            psnd_set_vp_id(mblk, spar0[s_ids[i]].vp_id);
            return spar0[s_ids[i]].vp_id;
        }
    }
    return G_ERROR;
}

int psnd_get_dominant_viewport(MBLOCK *mblk)
{
    int i, s_ids[] = { S_REAL, S_IMAG, S_BUFR1, S_BUFR2, S_BASELN, S_WINDOW, -1};
    SBLOCK *spar0;
    
    if (mblk->info->plot_mode == PLOT_2D) {
        return psnd_get_vp_id(mblk);
    }
    spar0 = mblk->spar_block[mblk->info->block_id];
    for (i=0;s_ids[i] != -1;i++) {
        if (spar0[s_ids[i]].show) 
            return spar0[s_ids[i]].vp_id;
    }
    return G_ERROR;
}


void psnd_set_master_world(MBLOCK *mblk, int master_vp_id, 
                            float wx1, float wy1, float wx2, float wy2)
{
    int i, j,s_ids[] = { S_REAL, S_IMAG, S_BUFR1, S_BUFR2, S_BASELN, S_WINDOW, -1};
    if (mblk->info->plot_mode == PLOT_2D) 
        return;
    for (i=0;i<MAXBLK;i++) {
        for (j=0;s_ids[j] != -1;j++) {
            if (mblk->spar_block[i][s_ids[j]].vp_id == master_vp_id) {
                mblk->spar_block[i][s_ids[j]].wxy[WX1] = wx1;
                mblk->spar_block[i][s_ids[j]].wxy[WY1] = wy1;
                mblk->spar_block[i][s_ids[j]].wxy[WX2] = wx2;
                mblk->spar_block[i][s_ids[j]].wxy[WY2] = wy2;
                return;
            }
        }
    }
}

int psnd_get_master_world(MBLOCK *mblk, int master_vp_id, 
                           float *wx1, float *wy1, float *wx2, float *wy2)
{
    int i, j, s_ids[] = { S_REAL, S_IMAG, S_BUFR1, S_BUFR2, S_BASELN, S_WINDOW, -1};
    if (mblk->info->plot_mode == PLOT_2D) 
        return FALSE;
    for (i=0;i<MAXBLK;i++) {
        for (j=0;s_ids[j] != -1;j++) {
            if (mblk->spar_block[i][s_ids[j]].vp_id == master_vp_id) {
                *wx1 = mblk->spar_block[i][s_ids[j]].wxy[WX1];
                *wy1 = mblk->spar_block[i][s_ids[j]].wxy[WY1];
                *wx2 = mblk->spar_block[i][s_ids[j]].wxy[WX2];
                *wy2 = mblk->spar_block[i][s_ids[j]].wxy[WY2];
                if (*wx1 == *wx2 || *wy1 == *wy2)
                    return FALSE;
                return TRUE;
            }
        }
    }
    return FALSE;
}

void psnd_setviewport3(MBLOCK *mblk, int vp_id, float minx, float maxx, 
                                         float miny, float maxy)
{
    float x1, x2, y1, y2;

/*
 * Now calculate fractional coordinates
 * mblk->info->ismaxx, mblk->info->ismaxy are the 'original' 
 * window coords in pixels
 */
    x1 = minx / (float)mblk->info->ismaxx;
    x2 = maxx / (float)mblk->info->ismaxx;
    y1 = miny / (float)mblk->info->ismaxy;
    y2 = maxy / (float)mblk->info->ismaxy;

    g_set_viewport(vp_id, x1, y1, x2, y2);
    g_select_viewport(vp_id);
}

void psnd_getviewport3(MBLOCK *mblk, int vp_id, float *minx, float *maxx, 
                                         float *miny, float *maxy)
{
    int vp_group =  vp_id2group(mblk,vp_id);
    *minx = mblk->vp_store[vp_group].x1;
    *maxx = mblk->vp_store[vp_group].x2;
    *miny = mblk->vp_store[vp_group].y1;
    *maxy = mblk->vp_store[vp_group].y2;
}

/*
void psnd_get_viewport(int vp_id, float *minx, float *maxx, 
                                         float *miny, float *maxy)
{
    float uxxmi,uxxma,uxymi,uxyma;
    int vp_group = vp_id2group(mblk,vp_id);

    g_get_viewport(vp_id,&uxxmi,&uxymi,&uxxma,&uxyma);

    *minx = uxxmi * mblk->vp_store[vp_group].x1;
    *maxx = uxxma * mblk->vp_store[vp_group].x2;
    *miny = uxymi * mblk->vp_store[vp_group].y1;
    *maxy = uxyma * mblk->vp_store[vp_group].y2;
}

void psnd_get_groupviewport(int vp_id, float *minx, float *maxx, 
                                         float *miny, float *maxy)
{
    int vp_group = vp_id2group(mblk,vp_id);

    *minx = mblk->vp_store[vp_group].x1/ (float)mblk->info->ismaxx;
    *maxx = mblk->vp_store[vp_group].x2/ (float)mblk->info->ismaxx;
    *miny = mblk->vp_store[vp_group].y1/ (float)mblk->info->ismaxy;
    *maxy = mblk->vp_store[vp_group].y2/ (float)mblk->info->ismaxy;
}

*/

/*
 * Set the viewport coordinates of this viewport
 * according to its group
 */
void psnd_set_groupviewport(MBLOCK *mblk, int vp_id)
{
    float minx, maxx, miny, maxy;
    int vp_group = vp_id2group(mblk,vp_id);

    minx = mblk->vp_store[vp_group].x1/ (float)mblk->info->ismaxx;
    maxx = mblk->vp_store[vp_group].x2/ (float)mblk->info->ismaxx;
    miny = mblk->vp_store[vp_group].y1/ (float)mblk->info->ismaxy;
    maxy = mblk->vp_store[vp_group].y2/ (float)mblk->info->ismaxy;
    g_set_viewport(vp_id, minx, miny, maxx, maxy);
    g_select_viewport(vp_id);
}

void psnd_link_viewport(MBLOCK *mblk, int vp_id, int vp_code)
{
    float x1,x2,y1,y2;

    switch (vp_code) {
        case VP_PHASE_2D:
        case VP_PHASE1:
        case VP_PHASE2:
        case VP_PHASE3:
        case VP_PHASE_2D_BORDER:
        case VP_PHASE1_BORDER:
        case VP_PHASE2_BORDER:
        case VP_PHASE3_BORDER:
        case VP_MAIN:            
            x1 = mblk->vp_store[vp_code].x1 / (float)mblk->info->ismaxx;
            x2 = mblk->vp_store[vp_code].x2 / (float)mblk->info->ismaxx;
            y1 = mblk->vp_store[vp_code].y1 / (float)mblk->info->ismaxy;
            y2 = mblk->vp_store[vp_code].y2 / (float)mblk->info->ismaxy;
            break;
        default:
            return;
    }
    
    g_set_viewport(vp_id, x1, y1, x2, y2);
    g_select_viewport(vp_id);
}

/*
 * Get the x and y zoom factors of the viewport
 * if it is linked to the scrollbars
 */
void psnd_getzoom(MBLOCK *mblk, int vp_id, float *zoomx, float *zoomy)
{
    g_scrollbar_get_zoom(mblk->info->win_id, vp_id, zoomx, zoomy);
}

/*
 * If the viewport is linked to the scrollbars than
 * update the scrollbar world coordinates
 * else set new world coordinates
 */
void psnd_setworld(MBLOCK *mblk, int vp_id,float pxl,float pxh,float pyl,float pyh)
{
    int i,j;

    if (g_scrollbar_reconnect(mblk->info->win_id, vp_id, 
            pxl, pyl, pxh, pyh)) 
                return;
    g_set_world(vp_id, pxl, pyl, pxh, pyh);
}

/*
 * Get the 'unzoomed' world coordinates of the viewport
 */
void psnd_getmaxworld(MBLOCK *mblk, int vp_id, float *wcxmin, float *wcxmax, float *wcymin, float *wcymax)
{
    if (!g_scrollbar_get_maxworld(mblk->info->win_id, vp_id,
                        wcxmin, wcymin, wcxmax, wcymax))
        g_get_world(vp_id, wcxmin, wcymin, wcxmax, wcymax);
}


static void connect_contour(MBLOCK *mblk, float x1, float y1, float x2, float y2)
{
    int flag = 0;
    g_scrollbar_connect(mblk->info->win_id, mblk->spar[S_CONTOUR].vp_id,
                        x1, y1, x2, y2, 
                        0);

    flag = G_SCROLL_YAXIS;
    g_scrollbar_connect(mblk->info->win_id, mblk->spar[S_REALSWAP].vp_id,
                        x1, y1, x2, y2, flag); 
    flag = G_SCROLL_XAXIS;
    g_scrollbar_connect(mblk->info->win_id, mblk->spar[S_REAL].vp_id,
                        x1, y1, x2, y2, flag); 
}

/*
 * Highlight a 2D phase viewport by placing a green or red border
 * around it.
 */
static void highlight(MBLOCK *mblk, int s_id, int v_id, int v2_id, int color)
{
    int col,obj,vp;
    float x1,x2,y1,y2;
    float xx1,xx2,yy1,yy2;

    vp  = mblk->spar[s_id].vp_id;
    obj = mblk->spar[s_id].obj_id;
    /*
     * The viewport coordinates of the highlight viewport
     * which are slightly larger than those of the corresponding
     * phase-1d-plot viewport
     */
    x1  = mblk->vp_store[v_id].x1;
    x2  = mblk->vp_store[v_id].x2;
    y1  = mblk->vp_store[v_id].y1;
    y2  = mblk->vp_store[v_id].y2;
    /* 
     * The viewport coordinates of the phase-1d-plot viewport
     */
    xx1  = mblk->vp_store[v2_id].x1;
    xx2  = mblk->vp_store[v2_id].x2;
    yy1  = mblk->vp_store[v2_id].y1;
    yy2  = mblk->vp_store[v2_id].y2;
    if (color > 0)
        /*
         * Active
         */
        col = mblk->spar[s_id].color2;
    else
        /*
         * Inactive
         */
        col = mblk->spar[s_id].color;

    g_delete_object(obj);
    g_open_object(obj);
    g_push_viewport();
    g_push_gc();
    g_set_world(vp, x1, y1, x2, y2);
    psnd_link_viewport(mblk, vp,  v_id);
    g_set_foreground(col);
    /*
     * Four fat lines form the border around the smalles viewport
     */
    g_fillrectangle(x1, y1, x2, yy1);
    g_fillrectangle(x1, y1, xx1, y2);
    g_fillrectangle(x1, yy2, x2, y2);
    g_fillrectangle(xx2, y1, x2, y2);
    g_pop_gc();
    g_pop_viewport();
    g_close_object(obj);
    g_call_object(obj);
}

/*
 * Surround viewport vp_id with a red border,
 * the rest with green borders
 */
static void higlight_viewport(MBLOCK *mblk, int vp_id)
{
    int activated[] = { -1, -1, -1, -1 };
    if (vp_id == mblk->spar[S_PHASE1].vp_id) 
        activated[0] = 1;
    else if (vp_id == mblk->spar[S_PHASE2].vp_id) 
        activated[1] = 1;
    else if (vp_id == mblk->spar[S_PHASE3].vp_id) 
        activated[2] = 1;
    else if (vp_id == mblk->spar[S_CONTOUR].vp_id) 
        activated[3] = 1;
    highlight(mblk, S_PHASE1_BORDER, VP_PHASE1_BORDER, VP_PHASE1, activated[0]);
    highlight(mblk, S_PHASE2_BORDER, VP_PHASE2_BORDER, VP_PHASE2, activated[1]);
    highlight(mblk, S_PHASE3_BORDER, VP_PHASE3_BORDER, VP_PHASE3, activated[2]);
    highlight(mblk, S_CONTOUR_BORDER, VP_PHASE_2D_BORDER, VP_PHASE_2D, activated[3]);
}

static void scroll_reconnect(MBLOCK *mblk, int vp_id, float x1, float y1, 
                             float x2, float y2, int axis_sleep)
{
    int i,j,k;
    
    if (mblk->info->phase_2d) {
        if (vp_id == mblk->spar[S_PHASE1].vp_id ||
            vp_id == mblk->spar[S_PHASE2].vp_id ||
            vp_id == mblk->spar[S_PHASE3].vp_id ||
            vp_id == mblk->spar[S_CONTOUR].vp_id) {
                int vp_group = vp_id2group(mblk,vp_id);
                g_scrollbar_disconnect(mblk->info->win_id);
                g_set_world(vp_id,
                   mblk->vp_store[vp_group].wx1,
                   mblk->vp_store[vp_group].wy1,
                   mblk->vp_store[vp_group].wx2,
                   mblk->vp_store[vp_group].wy2);
                psnd_set_vp_group(mblk, vp_group);
                psnd_set_vp_id(mblk,vp_id);
                if (vp_id == mblk->spar[S_CONTOUR].vp_id)
                    connect_contour(mblk,x1,y1,x2,y2);
                else
                    g_scrollbar_connect(mblk->info->win_id, vp_id,
                        x1, y1, x2, y2, 
                        0);
                g_scrollbar_connect(mblk->info->win_id, 
                        mblk->spar_block[0][S_UPDATE].vp_id,
                        0, 0, 0, 0, 
                        mblk->spar_block[0][S_UPDATE].scroll_flag);
        }
        return;
        
    }
    if (mblk->info->plot_mode == PLOT_2D) {
        psnd_set_vp_group(mblk,vp_id2group(mblk,mblk->spar[S_CONTOUR].vp_id));
        psnd_set_vp_id(mblk,mblk->spar[S_CONTOUR].vp_id);
        connect_contour(mblk,x1,y1,x2,y2);

    }
    else {
        int master_vp_id, done,block_ids[MAXBLK];
        float wx1,wx2,wy1,wy2;
        int  s_ids[] = { S_REAL, S_IMAG, S_BUFR1, S_BUFR2, S_BASELN, 
                         S_WINDOW, G_ERROR };

        master_vp_id = psnd_set_dominant_viewport(mblk);
        if (master_vp_id == G_ERROR)
            return;
        psnd_getmaxworld(mblk,master_vp_id, &wx1, &wx2, &wy1, &wy2);
        if ((mblk->info->auto_scale == AUTO_SCALE_OFF) && y1 < wy1 && y2 > wy2) {
            if (x1 < x2) {
                x1 = max(x1,wx1);
                x2 = min(x2,wx2);
            }
            else { 
                x1 = min(x1,wx1);
                x2 = max(x2,wx2);
            }
            if (y1 < y2) {
                wy1 = min(wy1,y1);
                wy2 = max(wy2,y2);
            }
            else {
                wy1 = max(wy1,y1);
                wy2 = min(wy2,y2);
            }
            g_set_world(mblk->spar[S_REAL].vp_id,
                        wx1, y1, wx2, y2);
            g_set_world(mblk->spar[S_IMAG].vp_id,
                        wx1, y1, wx2, y2);
            g_scrollbar_disconnect(mblk->info->win_id);
        }
        done = FALSE;
        k=0;
        block_ids[k++] = mblk->info->block_id;
        for (i=0;i<mblk->info->max_block;i++)
            if (i != mblk->info->block_id)
                block_ids[k++] = i;
        for (k=0;k<mblk->info->max_block && !done;k++) {
            i = block_ids[k];
            for (j=0; s_ids[j] != G_ERROR && !done;j++) {
                SBLOCK *sp = &(mblk->spar_block[i][s_ids[j]]);
                if (!sp->show)
                    g_set_objectstatus(sp->obj_id, G_SLEEP);
                else if (!done) {
                    master_vp_id = sp->vp_id;
                    g_scrollbar_connect(mblk->info->win_id, 
                            sp->vp_id,
                            x1, y1, x2, y2,
                            sp->scroll_flag);
                    psnd_set_vp_id(mblk,sp->vp_id);
                    g_scrollbar_set_dominant_viewport(mblk->info->win_id, sp->vp_id);
                    done = TRUE;
                }
            }
        }
        for (i=0;i<mblk->info->max_block;i++) {
            for (j=0; s_ids[j] != G_ERROR;j++) {
                SBLOCK *sp = &(mblk->spar_block[i][s_ids[j]]);
                if (master_vp_id == sp->vp_id)
                    continue;
                if (!sp->show)
                    g_set_objectstatus(sp->obj_id, G_SLEEP);
                if (master_vp_id == sp->vp_id_master)
                    g_scrollbar_connect(mblk->info->win_id, 
                            sp->vp_id,
                            x1, y1, x2, y2,
                            sp->scroll_flag);
                else
                    g_scrollbar_connect(mblk->info->win_id, 
                            sp->vp_id,
                            0, 0, 0, 0,
                            sp->scroll_flag);
            }
        }
    }
    for (i=0;i<mblk->info->max_block;i++) {
        if (axis_sleep || i != mblk->info->block_id || !mblk->spar_block[i][S_GRID].show)
            g_set_objectstatus(mblk->spar_block[i][S_GRID].obj_id, G_SLEEP);
        g_scrollbar_connect(mblk->info->win_id, mblk->spar_block[i][S_GRID].vp_id,
                        0, 0, 0, 0, 
                        mblk->spar_block[i][S_GRID].scroll_flag);
    }

    if (mblk->spar[S_AXISY].show) {
        for (i=0;i<mblk->info->max_block;i++) {
            if (axis_sleep || i != mblk->info->block_id)
                g_set_objectstatus(mblk->spar[S_AXISY].obj_id, G_SLEEP);
            g_scrollbar_connect(mblk->info->win_id, mblk->spar[S_AXISY].vp_id,
                        0, 0, 0, 0, 
                        mblk->spar_block[i][S_AXISY].scroll_flag);
        }
    }
    if (mblk->spar[S_AXISX].show) {
        for (i=0;i<mblk->info->max_block;i++) {
            if (axis_sleep || i != mblk->info->block_id)
                g_set_objectstatus(mblk->spar_block[i][S_AXISX].obj_id, G_SLEEP);
            g_scrollbar_connect(mblk->info->win_id, mblk->spar_block[i][S_AXISX].vp_id,
                        0, 0, 0, 0, 
                        mblk->spar_block[i][S_AXISX].scroll_flag);
        }
    }
    g_scrollbar_connect(mblk->info->win_id, mblk->spar_block[0][S_UPDATE].vp_id,
                        0, 0, 0, 0, 
                        mblk->spar_block[0][S_UPDATE].scroll_flag);
}

/*
 * Connect a 2D phase block to the scrollbars
 */
static int phase2d_reset_connection(MBLOCK *mblk, int new_group_id, int vp_id)
{
    int vp_group_id = psnd_get_vp_group(mblk);

    if (new_group_id == vp_group_id)
        return G_ERROR;
    mblk->vp_store[vp_group_id].store_id = g_scrollbar_store(mblk->info->win_id);
    psnd_set_vp_group(mblk, new_group_id);
    if (mblk->vp_store[new_group_id].store_id != G_ERROR) {
        psnd_set_vp_id(mblk,vp_id);
        g_scrollbar_retrieve(mblk->info->win_id, mblk->vp_store[new_group_id].store_id);
        mblk->vp_store[new_group_id].store_id = G_ERROR;
        return TRUE;
    }
    else {
        float wx1,wy1,wx2,wy2;
        g_get_world(vp_id, &wx1, &wy1, &wx2, &wy2);
        scroll_reconnect(mblk, vp_id, wx1,wy1,wx2,wy2, TRUE);
    }
    return FALSE;
}



/*
 * Re-connect a viewport in the '4-viewport phase mode'
 */
int psnd_reset_connection(MBLOCK *mblk, int vp_id)
{
    float rx1,ry1,rx2,ry2;
    int  result = FALSE;
    char labelon[]  = "Zoom";
    char labeloff[] = "Off";

    if (!mblk->info->phase_2d) 
        return FALSE;
    higlight_viewport(mblk, vp_id);
    if (vp_id == mblk->spar[S_PHASE1].vp_id) {
        result = phase2d_reset_connection(mblk, VP_PHASE1, vp_id);
    }
    else if (vp_id == mblk->spar[S_PHASE2].vp_id) {
        result = phase2d_reset_connection(mblk, VP_PHASE2, vp_id);
    }
    else if (vp_id == mblk->spar[S_PHASE3].vp_id) {
        result = phase2d_reset_connection(mblk, VP_PHASE3, vp_id);
    }
    else if (vp_id == mblk->spar[S_CONTOUR].vp_id) {
        result = phase2d_reset_connection(mblk, VP_PHASE_2D, vp_id);
    }
    return result;
}

/*
 * Re-connect all viewports in the 1d mode'
 * Used when a new mblk->spar[].show has been added
 */
int psnd_1d_reset_connection(MBLOCK *mblk)
{
    if (mblk->info->plot_mode == PLOT_2D) 
        return FALSE;
    if (psnd_set_dominant_viewport(mblk) == G_ERROR)
        return FALSE;
    if (mblk->info->auto_scale == AUTO_SCALE_ON) {
        float xx1,yy1,xx2,yy2;
        int vp_id = psnd_get_vp_id(mblk);
        g_get_world(vp_id,&xx1,&yy1,&xx2,&yy2);
        psnd_scrollbars_reconnect(mblk,vp_id, xx1,yy1,xx2,yy2,TRUE);  
    }               
    return TRUE;                            
}

/*
 * Select the viewport with the contour plot
 */
void psnd_select_contourviewport(MBLOCK *mblk)
{
    if (mblk->info->phase_2d) {
        psnd_set_button_phase_block(mblk, mblk->spar[S_CONTOUR].vp_id);
        psnd_reset_connection(mblk,mblk->spar[S_CONTOUR].vp_id);
    }
}

/*
 * Update the scrollbar connections
 */
void psnd_scrollbars_reconnect(MBLOCK *mblk, int vp_id, float x1, float y1, 
                          float x2, float y2, int axis_sleep)
{
    if (mblk->info->phase_2d) {
        psnd_set_button_phase_block(mblk, vp_id);
        psnd_reset_connection(mblk,vp_id);
#ifdef _WIN32
        g_clear_viewport();
#endif
    }
    if (x1 != x2 && y1 != y2)
        scroll_reconnect(mblk, vp_id, x1, y1, x2, y2, axis_sleep);    
}

int psnd_test_zoom(MBLOCK *mblk, int vp_id, float x1, float y1, 
                          float x2, float y2)
{
    float dx,dy;
    float wcxmin, wcxmax, wcymin, wcymax;

    if (mblk->info->plot_mode == PLOT_2D) {
        wcxmin = mblk->cpar_screen->ihamin;
        wcxmax = mblk->cpar_screen->ihamax;
        wcymin = mblk->cpar_screen->ihbmin;
        wcymax = mblk->cpar_screen->ihbmax;
    }
    else 
        psnd_getmaxworld(mblk, vp_id, &wcxmin, &wcxmax, &wcymin, &wcymax);
    dx = fabs((x1-x2)/(wcxmin - wcxmax));
    dy = fabs((y1-y2)/(wcymin - wcymax));
    return (dx > 0.001 && dy > 0.001);
}

typedef struct colortable {
    char name[40];
    char label[20];
    int  color[MAX_BLOCK];
} COLORTABLE;

typedef enum {
    COLTAB_REAL,
    COLTAB_REAL2D,
    COLTAB_IMAG,
    COLTAB_BUF1,
    COLTAB_BUF2,
    COLTAB_WIND,
    COLTAB_BASL,
    COLTAB_INT1D,
    COLTAB_PEAKPICK,
    COLTAB_CONTOURPLUS,
    COLTAB_CONTOURMIN,
    COLTAB_PHASE1,
    COLTAB_PHASE2,
    COLTAB_PHASE3,
    COLTAB_PHASE2DACTIVE,
    COLTAB_PHASE2DPASSIVE,
    COLTAB_XAXIS,
    COLTAB_YAXIS,
    COLTAB_BOX,
    COLTAB_GRID,
    COLTAB_INT2D,
    COLTAB_INT2DSCRATCH,
    COLTAB_2DMARK,
    COLTAB_2DCONTOURMARK,
    COLTAB_BACKGROUND,
    COLTAB_MAX
} COLTAB_IDS;
    

static COLORTABLE defcolors[] = {
    { "Real", "REAL",
       G_BLUE, 
       G_BLUE, 
       G_BLUE, 
       G_BLUE, 
       G_BLUE, 
       G_BLUE, 
       G_BLUE, 
       G_BLUE, 
       G_BLUE, 
       G_BLUE, 
       G_BLUE, 
       G_BLUE }, 
    { "Real on 2D", "REAL_2D",
       G_LIGHTMAGENTA, 
       G_LIGHTMAGENTA, 
       G_LIGHTMAGENTA, 
       G_LIGHTMAGENTA, 
       G_LIGHTMAGENTA, 
       G_LIGHTMAGENTA, 
       G_LIGHTMAGENTA, 
       G_LIGHTMAGENTA, 
       G_LIGHTMAGENTA, 
       G_LIGHTMAGENTA, 
       G_LIGHTMAGENTA, 
       G_LIGHTMAGENTA }, 
    { "Imaginary", "IMAGINARY",
       G_GREEN, 
       G_GREEN, 
       G_GREEN, 
       G_GREEN, 
       G_GREEN, 
       G_GREEN, 
       G_GREEN, 
       G_GREEN, 
       G_GREEN, 
       G_GREEN, 
       G_GREEN, 
       G_GREEN },
    { "Buffer 1", "BUFFER_1",
       G_RED, 
       G_RED, 
       G_RED, 
       G_RED, 
       G_RED, 
       G_RED, 
       G_RED, 
       G_RED, 
       G_RED, 
       G_RED, 
       G_RED, 
       G_RED },
    { "Buffer 2", "BUFFER_2",
       G_LIGHTRED, 
       G_LIGHTRED, 
       G_LIGHTRED, 
       G_LIGHTRED, 
       G_LIGHTRED, 
       G_LIGHTRED, 
       G_LIGHTRED, 
       G_LIGHTRED, 
       G_LIGHTRED, 
       G_LIGHTRED, 
       G_LIGHTRED, 
       G_LIGHTRED },
    { "Window", "WINDOW",
       G_RED, 
       G_RED, 
       G_RED, 
       G_RED, 
       G_RED, 
       G_RED, 
       G_RED, 
       G_RED, 
       G_RED, 
       G_RED, 
       G_RED, 
       G_RED },
    { "Baseline", "BASELINE",
       G_MAGENTA, 
       G_MAGENTA, 
       G_MAGENTA, 
       G_MAGENTA, 
       G_MAGENTA, 
       G_MAGENTA, 
       G_MAGENTA, 
       G_MAGENTA, 
       G_MAGENTA, 
       G_MAGENTA, 
       G_MAGENTA, 
       G_MAGENTA },
    { "Integral 1D", "INTEGRAL_1D",
       G_GREEN, 
       G_GREEN, 
       G_GREEN, 
       G_GREEN, 
       G_GREEN, 
       G_GREEN, 
       G_GREEN, 
       G_GREEN, 
       G_GREEN, 
       G_GREEN, 
       G_GREEN, 
       G_GREEN },
    { "Peak pick 1D", "PEAK_PICK_1D",
       G_GREEN, 
       G_GREEN, 
       G_GREEN, 
       G_GREEN, 
       G_GREEN, 
       G_GREEN, 
       G_GREEN, 
       G_GREEN, 
       G_GREEN, 
       G_GREEN, 
       G_GREEN, 
       G_GREEN },
    { "Contour plus", "CONTOUR_PLUS",
       G_RED, 
       G_RED, 
       G_RED, 
       G_RED, 
       G_RED, 
       G_RED, 
       G_RED, 
       G_RED, 
       G_RED, 
       G_RED, 
       G_RED, 
       G_RED },
    { "Contour min", "CONTOUR_MIN",
       G_BLUE, 
       G_BLUE, 
       G_BLUE, 
       G_BLUE, 
       G_BLUE, 
       G_BLUE, 
       G_BLUE, 
       G_BLUE, 
       G_BLUE, 
       G_BLUE, 
       G_BLUE, 
       G_BLUE },
    { "Phase 2D:box1", "PHASE_2D_BOX1",
       G_RED, 
       G_RED, 
       G_RED, 
       G_RED, 
       G_RED, 
       G_RED, 
       G_RED, 
       G_RED, 
       G_RED, 
       G_RED, 
       G_RED, 
       G_RED },
    { "Phase 2D:box2", "PHASE_2D_BOX2",
       G_BLUE, 
       G_BLUE, 
       G_BLUE, 
       G_BLUE, 
       G_BLUE, 
       G_BLUE, 
       G_BLUE, 
       G_BLUE, 
       G_BLUE, 
       G_BLUE, 
       G_BLUE, 
       G_BLUE },
    { "Phase 2D:box3", "PHASE_2D_BOX3",
       G_GREEN, 
       G_GREEN, 
       G_GREEN, 
       G_GREEN, 
       G_GREEN, 
       G_GREEN, 
       G_GREEN, 
       G_GREEN, 
       G_GREEN, 
       G_GREEN, 
       G_GREEN, 
       G_GREEN },
    { "Phase 2D:off", "PHASE_2D_OFF",
       G_RED, 
       G_RED, 
       G_RED, 
       G_RED, 
       G_RED, 
       G_RED, 
       G_RED, 
       G_RED, 
       G_RED, 
       G_RED, 
       G_RED, 
       G_RED },
    { "Phase 2D:on", "PHASE_2D_ON",
       G_GREEN, 
       G_GREEN, 
       G_GREEN, 
       G_GREEN, 
       G_GREEN, 
       G_GREEN, 
       G_GREEN, 
       G_GREEN, 
       G_GREEN, 
       G_GREEN, 
       G_GREEN, 
       G_GREEN },
    { "X axis", "X_AXIS",
       G_RED, 
       G_RED, 
       G_RED, 
       G_RED, 
       G_RED, 
       G_RED, 
       G_RED, 
       G_RED, 
       G_RED, 
       G_RED, 
       G_RED, 
       G_RED },
    { "Y axis", "Y_AXIS",
       G_RED, 
       G_RED, 
       G_RED, 
       G_RED, 
       G_RED, 
       G_RED, 
       G_RED, 
       G_RED, 
       G_RED, 
       G_RED, 
       G_RED, 
       G_RED },
    { "Box", "BOX",
       G_RED, 
       G_RED, 
       G_RED, 
       G_RED, 
       G_RED, 
       G_RED, 
       G_RED, 
       G_RED, 
       G_RED, 
       G_RED, 
       G_RED, 
       G_RED },
    { "Grid", "GRID",
       G_GREEN, 
       G_GREEN, 
       G_GREEN, 
       G_GREEN, 
       G_GREEN, 
       G_GREEN, 
       G_GREEN, 
       G_GREEN, 
       G_GREEN, 
       G_GREEN, 
       G_GREEN, 
       G_GREEN },
    { "Int.2D:box", "INTEGRAL_2D_BOX",
       G_GREEN, 
       G_GREEN, 
       G_GREEN, 
       G_GREEN, 
       G_GREEN, 
       G_GREEN, 
       G_GREEN, 
       G_GREEN, 
       G_GREEN, 
       G_GREEN, 
       G_GREEN, 
       G_GREEN },
    { "Int.2D:temp","INTEGRAL_2D_TEMP",
       G_MAGENTA, 
       G_MAGENTA, 
       G_MAGENTA, 
       G_MAGENTA, 
       G_MAGENTA, 
       G_MAGENTA, 
       G_MAGENTA, 
       G_MAGENTA, 
       G_MAGENTA, 
       G_MAGENTA, 
       G_MAGENTA, 
       G_MAGENTA },
    { "2D:marker",  "MARKER_2D",
       G_LIGHTMAGENTA, 
       G_LIGHTMAGENTA, 
       G_LIGHTMAGENTA, 
       G_LIGHTMAGENTA, 
       G_LIGHTMAGENTA, 
       G_LIGHTMAGENTA, 
       G_LIGHTMAGENTA, 
       G_LIGHTMAGENTA, 
       G_LIGHTMAGENTA, 
       G_LIGHTMAGENTA, 
       G_LIGHTMAGENTA, 
       G_LIGHTMAGENTA },
    { "CP:marker",  "MARKER_CP",
       G_MAGENTA, 
       G_MAGENTA, 
       G_MAGENTA, 
       G_MAGENTA, 
       G_MAGENTA, 
       G_MAGENTA, 
       G_MAGENTA, 
       G_MAGENTA, 
       G_MAGENTA, 
       G_MAGENTA, 
       G_MAGENTA, 
       G_MAGENTA },
    { "Background", "BACKGROUND",
       G_WHITE, 
       G_WHITE, 
       G_WHITE, 
       G_WHITE, 
       G_WHITE, 
       G_WHITE, 
       G_WHITE, 
       G_WHITE, 
       G_WHITE, 
       G_WHITE, 
       G_WHITE, 
       G_WHITE },
    { "unsused", "UNUSED",
       G_BLACK, 
       G_BLACK, 
       G_BLACK, 
       G_BLACK, 
       G_BLACK, 
       G_BLACK, 
       G_BLACK, 
       G_BLACK, 
       G_BLACK, 
       G_BLACK, 
       G_BLACK, 
       G_BLACK }
};

static COLORTABLE currcolors[COLTAB_MAX];
static char *colornames[COLTAB_MAX];


static void update_colors(MBLOCK *mblk)
{
    int i;
    for (i=0;i<MAXBLK;i++) {

        mblk->spar_block[i][S_REAL].color  = 
            currcolors[COLTAB_REAL].color[i];
        mblk->spar_block[i][S_REAL].color2 = 
            currcolors[COLTAB_REAL2D].color[i];
        mblk->spar_block[i][S_REAL].color3 = 
            currcolors[COLTAB_INT1D].color[i];
        mblk->spar_block[i][S_IMAG].color  = 
            currcolors[COLTAB_IMAG].color[i];
        mblk->spar_block[i][S_BUFR1].color  = 
            currcolors[COLTAB_BUF1].color[i];
        mblk->spar_block[i][S_BUFR2].color  = 
            currcolors[COLTAB_BUF2].color[i];
        mblk->spar_block[i][S_WINDOW].color  = 
            currcolors[COLTAB_WIND].color[i];
        mblk->spar_block[i][S_BASELN].color  = 
            currcolors[COLTAB_BASL].color[i];
        mblk->spar_block[i][S_CONTOUR].color  = 
            currcolors[COLTAB_CONTOURPLUS].color[i];
        mblk->spar_block[i][S_CONTOUR].color2  = 
            currcolors[COLTAB_CONTOURMIN].color[i];
        mblk->spar_block[i][S_CONTOUR_BORDER].color2  = 
            currcolors[COLTAB_PHASE2DACTIVE].color[i];
        mblk->spar_block[i][S_CONTOUR_BORDER].color   = 
            currcolors[COLTAB_PHASE2DPASSIVE].color[i];

        mblk->spar_block[i][S_PHASE1].color  = 
            currcolors[COLTAB_PHASE1].color[i];
        mblk->spar_block[i][S_PHASE2].color  = 
            currcolors[COLTAB_PHASE2].color[i];
        mblk->spar_block[i][S_PHASE3].color  = 
            currcolors[COLTAB_PHASE3].color[i];
        mblk->spar_block[i][S_PHASE1_BORDER].color2  = 
            currcolors[COLTAB_PHASE2DACTIVE].color[i];
        mblk->spar_block[i][S_PHASE1_BORDER].color   = 
            currcolors[COLTAB_PHASE2DPASSIVE].color[i];
        mblk->spar_block[i][S_PHASE2_BORDER].color2  = 
            currcolors[COLTAB_PHASE2DACTIVE].color[i];
        mblk->spar_block[i][S_PHASE2_BORDER].color   = 
            currcolors[COLTAB_PHASE2DPASSIVE].color[i];
        mblk->spar_block[i][S_PHASE3_BORDER].color2  = 
            currcolors[COLTAB_PHASE2DACTIVE].color[i];
        mblk->spar_block[i][S_PHASE3_BORDER].color   = 
            currcolors[COLTAB_PHASE2DPASSIVE].color[i];
        
        mblk->spar_block[i][S_AXISX].color   = 
            currcolors[COLTAB_XAXIS].color[i];
        mblk->spar_block[i][S_AXISY].color   = 
            currcolors[COLTAB_YAXIS].color[i];
        mblk->spar_block[i][S_BOX].color   = 
            currcolors[COLTAB_BOX].color[i];
        mblk->spar_block[i][S_GRID].color   = 
            currcolors[COLTAB_GRID].color[i];

        mblk->spar_block[i][S_INT2D].color   = 
            currcolors[COLTAB_INT2D].color[i];
        mblk->spar_block[i][S_SCRATCH2D].color   = 
            currcolors[COLTAB_INT2DSCRATCH].color[i];
        mblk->spar_block[i][S_PEAKPICK].color   = 
            currcolors[COLTAB_PEAKPICK].color[i];

        mblk->spar_block[i][S_2DMARKER].color2   = 
            currcolors[COLTAB_2DMARK].color[i];
        mblk->spar_block[i][S_2DMARKER].color   = 
            currcolors[COLTAB_2DCONTOURMARK].color[i];

        mblk->spar_block[i][S_CONTOUR].color3   = 
            currcolors[COLTAB_BACKGROUND].color[i];
    }
    psnd_process_color_levels(mblk, mblk->cpar_screen);

}

char **psnd_get_colornames(int *count)
{
    *count = COLTAB_MAX;
    return colornames;
}

int psnd_get_colorvalue(MBLOCK *mblk, int id, int block_id)
{
    block_id %= mblk->info->max_block;
    id       %= COLTAB_MAX;
    return currcolors[id].color[block_id];
}

void psnd_set_colorvalue(MBLOCK *mblk, int id, int block_id, int col_id)
{
    block_id %= mblk->info->max_block;
    id       %= COLTAB_MAX;
    currcolors[id].color[block_id] = col_id;
    update_colors(mblk);
}

void psnd_set_default_colors(MBLOCK *mblk)
{
    int i,j;

    assert(mblk->info->max_block <= 12);
    for (j=0;j<COLTAB_MAX;j++) {
        strcpy(currcolors[j].name, defcolors[j].name);
        colornames[j] = defcolors[j].name;
        for (i=0;i<MAXBLK;i++) 
            currcolors[j].color[i] = defcolors[j].color[i];
    }
    update_colors(mblk);
}

void psnd_write_defaultcolors(FILE *outfile, MBLOCK *mblk)
{
    int i,j;

    fprintf(outfile,"BLOCK_COLORS_SETUP\n");
    for (j=0;j<COLTAB_MAX;j++) 
        for (i=0;i<mblk->info->max_block;i++) 
            fprintf(outfile,"%s %2d %3d\n",
                defcolors[j].label, i+1, currcolors[j].color[i]+1);
    fprintf(outfile,"BLOCK_COLORS_END\n");
}

void psnd_read_defaultcolors(FILE *infile, MBLOCK *mblk)
{
    int i,j,icol,ncolors;
    char buf[PSND_STRLEN+1];

    ncolors = g_get_palettesize();
    while (fgets(buf, PSND_STRLEN, infile) != NULL) {
        char *p;
        char sep[] = " \r\n\t";
        
        if (buf[0] == '#')
            continue;
        strupr(buf);
        p= strtok(buf, sep);
        if (p) {
            if (strcmp(p, "BLOCK_COLORS_END") == 0) {
                break;
            }
            else {
                for (j=0;j<COLTAB_MAX;j++) {
                    if (strcmp(p, defcolors[j].label) == 0) {
                        p = strtok(NULL, sep);
                        if (p) {
                            i = psnd_scan_integer(p);
                            if (i > 0 && i <= mblk->info->max_block) {
                                p = strtok(NULL, sep);
                                if (p) {
                                    icol = psnd_scan_integer(p);
                                    if (icol > 0 && icol <= ncolors) {
                                        currcolors[j].color[i-1] = icol-1;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }         
    update_colors(mblk);
}

/*
 * Initiate the viewports and viewport groups
 */
void psnd_init_viewports(MBLOCK *mblk)
{
    int i,j, obj_count, vp_count;
    float dx, dy,ddx,ddy,frac,fracm;
    
    obj_count = 500;
    /*
     * Viewport order is important !!
     * The Viewport that has the lowest vp_id is selected by the cursor
     *     when viewports overlap
     * Just start with some random numbers
     */
    vp_count = 200;
    for (j=0;j<MAXBLK;j++) {
        mblk->spar_block[j] = (SBLOCK*) malloc(sizeof(SBLOCK)*S_MAX);
        mblk->spar = mblk->spar_block[j];
        for (i=0;i<S_MAX;i++) {  
            mblk->spar[i].vp_group = VP_MAIN;
            mblk->spar[i].vp_id_master = 0;
            mblk->spar[i].vp_id_block_master = NULL;
            mblk->spar[i].scroll_flag = 0;
            mblk->spar[i].color  = G_BLACK;
            mblk->spar[i].color2 = G_BLACK;
            mblk->spar[i].color3 = G_BLACK;
            mblk->spar[i].disable_auto_scale_2d = TRUE;
            mblk->spar[i].dxy[0] = 0.0;
            mblk->spar[i].dxy[1] = 0.0;
            mblk->spar[i].dxy[2] = 0.0;
            mblk->spar[i].dxy[3] = 0.0;
            mblk->spar[i].wxy[0] = 0.0;
            mblk->spar[i].wxy[1] = 0.0;
            mblk->spar[i].wxy[2] = 0.0;
            mblk->spar[i].wxy[3] = 0.0;
        }
        mblk->spar[S_CONTOUR].show   = TRUE;
        mblk->spar[S_CONTOUR].obj_id = obj_count;
        mblk->spar[S_CONTOUR].vp_id  = vp_count;
        obj_count += 10;
        vp_count  += 10;
        mblk->spar[S_CONTOUR_BORDER].show   = FALSE;
        mblk->spar[S_CONTOUR_BORDER].obj_id = obj_count;
        mblk->spar[S_CONTOUR_BORDER].vp_id  = vp_count;
        mblk->spar[S_CONTOUR_BORDER].vp_group = VP_PHASE_2D;
        obj_count += 10;
        vp_count  += 10;
        if (j==0)
            mblk->spar[S_REAL].show   = TRUE;
        else {
            mblk->spar[S_REAL].show   = FALSE;
        }
        mblk->spar[S_REAL].obj_id = obj_count;
        mblk->spar[S_REAL].vp_id  = vp_count; 
        mblk->spar[S_REAL].scroll_flag = G_SCROLL_CORRESPONDING;
        obj_count += 10;
        vp_count  += 10;
        mblk->spar[S_REALSWAP].show   = FALSE;
        mblk->spar[S_REALSWAP].obj_id = obj_count;
        mblk->spar[S_REALSWAP].vp_id  = vp_count; 
        obj_count += 10;
        vp_count  += 10;
        mblk->spar[S_IMAG].show   = FALSE;
        mblk->spar[S_IMAG].obj_id = obj_count;
        mblk->spar[S_IMAG].vp_id  = vp_count; 
        mblk->spar[S_IMAG].scroll_flag = G_SCROLL_CORRESPONDING;
        obj_count += 10;
        vp_count  += 10;
        mblk->spar[S_BUFR1].show   = FALSE;
        mblk->spar[S_BUFR1].obj_id = obj_count;
        mblk->spar[S_BUFR1].vp_id  = vp_count; 
        mblk->spar[S_BUFR1].scroll_flag = G_SCROLL_XAXIS;
        obj_count += 10;
        vp_count  += 10;
        mblk->spar[S_BUFR2].show   = FALSE;
        mblk->spar[S_BUFR2].obj_id = obj_count;
        mblk->spar[S_BUFR2].vp_id_master  = mblk->spar[S_REAL].vp_id;
        mblk->spar[S_BUFR2].vp_id  = vp_count;
        mblk->spar[S_BUFR2].scroll_flag = G_SCROLL_CORRESPONDING;
        obj_count += 10;
        vp_count  += 10;
        mblk->spar[S_WINDOW].show   = FALSE;
        mblk->spar[S_WINDOW].obj_id = obj_count;
        mblk->spar[S_WINDOW].vp_id  = vp_count;
        mblk->spar[S_WINDOW].scroll_flag = G_SCROLL_CORRESPONDING;
        obj_count += 10;
        vp_count  += 10;
        mblk->spar[S_BASELN].show   = FALSE;
        mblk->spar[S_BASELN].obj_id = obj_count;
        mblk->spar[S_BASELN].vp_id_master  = mblk->spar[S_REAL].vp_id;
        mblk->spar[S_BASELN].vp_id  = vp_count; 
        mblk->spar[S_BASELN].scroll_flag = G_SCROLL_CORRESPONDING;
        obj_count += 10;
        vp_count  += 10;
        mblk->spar[S_PHASE1].show   = FALSE;
        mblk->spar[S_PHASE1].obj_id = obj_count;
        mblk->spar[S_PHASE1].vp_id  = vp_count;
        mblk->spar[S_PHASE1].vp_group = VP_PHASE1;
        obj_count += 10;
        vp_count  += 10;
        mblk->spar[S_PHASE1_BORDER].show   = FALSE;
        mblk->spar[S_PHASE1_BORDER].obj_id = obj_count;
        mblk->spar[S_PHASE1_BORDER].vp_id  = vp_count;
        mblk->spar[S_PHASE1_BORDER].vp_group = VP_PHASE1;
        obj_count += 10;
        vp_count  += 10;
        mblk->spar[S_PHASE2].show   = FALSE;
        mblk->spar[S_PHASE2].obj_id = obj_count;
        mblk->spar[S_PHASE2].vp_id  = vp_count;
        mblk->spar[S_PHASE2].vp_group = VP_PHASE2;
        obj_count += 10;
        vp_count  += 10;
        mblk->spar[S_PHASE2_BORDER].show   = FALSE;
        mblk->spar[S_PHASE2_BORDER].obj_id = obj_count;
        mblk->spar[S_PHASE2_BORDER].vp_id  = vp_count;
        mblk->spar[S_PHASE2_BORDER].vp_group = VP_PHASE2;
        obj_count += 10;
        vp_count  += 10;
        mblk->spar[S_PHASE3].show   = FALSE;
        mblk->spar[S_PHASE3].obj_id = obj_count;
        mblk->spar[S_PHASE3].vp_id  = vp_count;
        mblk->spar[S_PHASE3].vp_group = VP_PHASE3;
        obj_count += 10;
        vp_count  += 10;
        mblk->spar[S_PHASE3_BORDER].show   = FALSE;
        mblk->spar[S_PHASE3_BORDER].obj_id = obj_count;
        mblk->spar[S_PHASE3_BORDER].vp_id  = vp_count;
        mblk->spar[S_PHASE3_BORDER].vp_group = VP_PHASE3;
        obj_count += 10;
        vp_count  += 10;
        mblk->spar[S_AXISX].show   = TRUE;
        mblk->spar[S_AXISX].obj_id = obj_count;
        mblk->spar[S_AXISX].vp_id  = vp_count;
        mblk->spar[S_AXISX].vp_group = VP_XAXIS;
        mblk->spar[S_AXISX].scroll_flag = G_SCROLL_XAXIS;
        obj_count += 10;
        vp_count  += 10;
        mblk->spar[S_AXISX_MARK].show   = AXIS_UNITS_AUTOMATIC;
        mblk->spar[S_AXISX_MARK].obj_id = 0;
        mblk->spar[S_AXISX_MARK].vp_id  = mblk->spar[S_AXISX].vp_id;
        mblk->spar[S_AXISX_MARK].vp_group    = VP_XAXIS;
        obj_count += 10;
        vp_count  += 10;
        mblk->spar[S_AXISY].show   = TRUE;
        mblk->spar[S_AXISY].obj_id = obj_count;
        if (j==0)
            mblk->spar[S_AXISY].vp_id  = vp_count;
        else
            mblk->spar[S_AXISY].vp_id  = mblk->spar_block[0][S_AXISY].vp_id;
        mblk->spar[S_AXISY].vp_group = VP_YAXIS;
        mblk->spar[S_AXISY].scroll_flag = G_SCROLL_YAXIS;
        obj_count += 10;
        vp_count  += 10;
        mblk->spar[S_AXISY_MARK].show   = AXIS_UNITS_AUTOMATIC;
        mblk->spar[S_AXISY_MARK].obj_id = 0;
        mblk->spar[S_AXISY_MARK].vp_id  = mblk->spar[S_AXISY].vp_id;
        mblk->spar[S_AXISY_MARK].vp_group    = VP_YAXIS;
        obj_count += 10;
        vp_count  += 10;
        mblk->spar[S_AXISXY_MARK].show   = AXIS_UNITS_CHANNELS;
        mblk->spar[S_AXISXY_MARK].obj_id = 0;
        mblk->spar[S_AXISXY_MARK].vp_id  = mblk->spar[S_AXISY].vp_id;
        mblk->spar[S_AXISXY_MARK].vp_group    = VP_YAXIS;
        obj_count += 10;
        vp_count  += 10;
        mblk->spar[S_BOX].show   = TRUE;
        mblk->spar[S_BOX].obj_id = obj_count;
        mblk->spar[S_BOX].vp_id  = vp_count;
        obj_count += 10;
        vp_count  += 10;
        mblk->spar[S_GRID].show   = FALSE;
        mblk->spar[S_GRID].obj_id = obj_count;
        mblk->spar[S_GRID].vp_id  = vp_count;
        mblk->spar[S_GRID].scroll_flag = G_SCROLL_CORRESPONDING;
        obj_count += 10;
        vp_count  += 10;
        mblk->spar[S_PEAKPICK].show   = FALSE;
        mblk->spar[S_PEAKPICK].obj_id = mblk->spar[S_REAL].obj_id;
        mblk->spar[S_PEAKPICK].vp_id  = mblk->spar[S_REAL].vp_id;
        mblk->spar[S_PEAKPICK].vp_group = mblk->spar[S_REAL].vp_group;
        obj_count += 10;
        vp_count  += 10;
        mblk->spar[S_INT2D].show   = FALSE;
        mblk->spar[S_INT2D].obj_id = obj_count;
        mblk->spar[S_INT2D].vp_id  = mblk->spar[S_CONTOUR].vp_id;
        mblk->spar[S_INT2D].vp_group = mblk->spar[S_CONTOUR].vp_group;
        obj_count += 10;
        vp_count  += 10;
        mblk->spar[S_SCRATCH2D].show   = FALSE;
        mblk->spar[S_SCRATCH2D].obj_id = obj_count;
        mblk->spar[S_SCRATCH2D].vp_id  = mblk->spar[S_CONTOUR].vp_id;
        mblk->spar[S_SCRATCH2D].vp_group = mblk->spar[S_CONTOUR].vp_group;
        obj_count += 10;
        vp_count  += 10;
        mblk->spar[S_2DMARKER].show   = FALSE;
        mblk->spar[S_2DMARKER].obj_id = obj_count;
        mblk->spar[S_2DMARKER].vp_id    = mblk->spar[S_CONTOUR].vp_id;
        mblk->spar[S_2DMARKER].vp_group = mblk->spar[S_CONTOUR].vp_group;

        obj_count += 10;
        vp_count  += 10;
        mblk->spar[S_HARDCOPY].show     = FALSE;
        mblk->spar[S_HARDCOPY].obj_id   = obj_count;
        mblk->spar[S_HARDCOPY].vp_id    = vp_count;

        obj_count += 10;
        vp_count  += 10;
        mblk->spar[S_COLORMAP].show     = FALSE;
        mblk->spar[S_COLORMAP].obj_id   = obj_count;
        mblk->spar[S_COLORMAP].vp_id    = vp_count;
    }
    mblk->spar = mblk->spar_block[0];
    obj_count += 10;
    vp_count  += 10;
    mblk->spar[S_UPDATE].obj_id = obj_count;
    mblk->spar[S_UPDATE].vp_id  = vp_count; 
    mblk->spar[S_UPDATE].scroll_flag = 
        G_SCROLL_CORRESPONDING | G_SCROLL_LASTVIEWPORT;

    mblk->info->ismaxx = WINSIZEX;
    mblk->info->ismaxy = WINSIZEY;

    frac = BORDER_FRAC;
    fracm = BORDER_FRAC2;
    mblk->info->txminx=   (frac+fracm) * WINSIZEX;
    mblk->info->txmaxx=(1.-frac+fracm) * WINSIZEX;
    mblk->info->txminy=   (frac+fracm) * WINSIZEY;
    mblk->info->txmaxy=(1.-frac+fracm) * WINSIZEY;

    dx = mblk->info->txmaxx - mblk->info->txminx;
    dy = mblk->info->txmaxy - mblk->info->txminy;
    ddx = PHASE_2D_BORDERSIZE;
    ddy = PHASE_2D_BORDERSIZE;
    /*
     * Initiate the viewport groups
     */
    mblk->vp_store[VP_MAIN].x1 = mblk->info->txminx;
    mblk->vp_store[VP_MAIN].x2 = mblk->info->txmaxx;
    mblk->vp_store[VP_MAIN].y1 = mblk->info->txminy;
    mblk->vp_store[VP_MAIN].y2 = mblk->info->txmaxy;
    mblk->vp_store[VP_PHASE_2D].x1 = mblk->info->txminx + ddx;
    mblk->vp_store[VP_PHASE_2D].x2 = mblk->info->txmaxx - dx/2 - ddx;
    mblk->vp_store[VP_PHASE_2D].y1 = mblk->info->txminy + dy/2 + ddy;
    mblk->vp_store[VP_PHASE_2D].y2 = mblk->info->txmaxy + ddy - ddy;
    mblk->vp_store[VP_PHASE_2D_BORDER].x1 = mblk->info->txminx;
    mblk->vp_store[VP_PHASE_2D_BORDER].x2 = mblk->info->txmaxx - dx/2;
    mblk->vp_store[VP_PHASE_2D_BORDER].y1 = mblk->info->txminy + dy/2;
    mblk->vp_store[VP_PHASE_2D_BORDER].y2 = mblk->info->txmaxy;
    mblk->vp_store[VP_PHASE1].x1 = mblk->info->txminx + dx/2 + ddx;
    mblk->vp_store[VP_PHASE1].x2 = mblk->info->txmaxx - ddx;
    mblk->vp_store[VP_PHASE1].y1 = mblk->info->txminy + dy/2 + ddy;
    mblk->vp_store[VP_PHASE1].y2 = mblk->info->txmaxy - ddy;
    mblk->vp_store[VP_PHASE1_BORDER].x1 = mblk->info->txminx + dx/2;
    mblk->vp_store[VP_PHASE1_BORDER].x2 = mblk->info->txmaxx;
    mblk->vp_store[VP_PHASE1_BORDER].y1 = mblk->info->txminy + dy/2;
    mblk->vp_store[VP_PHASE1_BORDER].y2 = mblk->info->txmaxy;
    mblk->vp_store[VP_PHASE2].x1 = mblk->info->txminx + ddx;
    mblk->vp_store[VP_PHASE2].x2 = mblk->info->txmaxx - dx/2 - ddx;
    mblk->vp_store[VP_PHASE2].y1 = mblk->info->txminy + ddy;
    mblk->vp_store[VP_PHASE2].y2 = mblk->info->txmaxy - dy/2 - ddy;
    mblk->vp_store[VP_PHASE2_BORDER].x1 = mblk->info->txminx;
    mblk->vp_store[VP_PHASE2_BORDER].x2 = mblk->info->txmaxx - dx/2;
    mblk->vp_store[VP_PHASE2_BORDER].y1 = mblk->info->txminy;
    mblk->vp_store[VP_PHASE2_BORDER].y2 = mblk->info->txmaxy - dy/2;
    mblk->vp_store[VP_PHASE3].x1 = mblk->info->txminx + dx/2 + ddx;
    mblk->vp_store[VP_PHASE3].x2 = mblk->info->txmaxx - ddx;
    mblk->vp_store[VP_PHASE3].y1 = mblk->info->txminy + ddy;
    mblk->vp_store[VP_PHASE3].y2 = mblk->info->txmaxy - dy/2 - ddy;
    mblk->vp_store[VP_PHASE3_BORDER].x1 = mblk->info->txminx + dx/2;
    mblk->vp_store[VP_PHASE3_BORDER].x2 = mblk->info->txmaxx;
    mblk->vp_store[VP_PHASE3_BORDER].y1 = mblk->info->txminy;
    mblk->vp_store[VP_PHASE3_BORDER].y2 = mblk->info->txmaxy - dy/2;
    mblk->vp_store[VP_XAXIS].x1  = mblk->vp_store[VP_MAIN].x1;
    mblk->vp_store[VP_XAXIS].x2  = mblk->vp_store[VP_MAIN].x2;
    mblk->vp_store[VP_XAXIS].y1  = 0.0;
    mblk->vp_store[VP_XAXIS].y2  = mblk->vp_store[VP_MAIN].y1 - 1;
    mblk->vp_store[VP_YAXIS].x1  = 0.0;
    mblk->vp_store[VP_YAXIS].x2  = mblk->vp_store[VP_MAIN].x1 - 2;
    mblk->vp_store[VP_YAXIS].y1  = mblk->vp_store[VP_MAIN].y1;
    mblk->vp_store[VP_YAXIS].y2  = mblk->vp_store[VP_MAIN].y2;
    psnd_set_vp_group(mblk, VP_MAIN);
    psnd_set_vp_id(mblk,mblk->spar[S_REAL].vp_id);
    psnd_set_default_colors(mblk);
}

/*
 * return the coordinates of the current screen plotting 
 * area (integers)
 */
void psnd_get_plotarea(MBLOCK *mblk, int *lowx, int *highx, 
                                     int *lowy, int *highy)
{
    float xx_1,yy_1,xx_2,yy_2;
    int vp_id;

    vp_id = psnd_get_vp_id(mblk);
    g_get_world(vp_id,&xx_1,&yy_1,&xx_2,&yy_2);

    if (mblk->info->plot_mode == PLOT_2D) {
        *lowx  = round(xx_1);
        *lowx  = max(*lowx, mblk->cpar_screen->ihamin);
        *lowx  = min(*lowx, mblk->cpar_screen->ihamax);
        *highx = round(xx_2);
        *highx = max(*highx, mblk->cpar_screen->ihamin);
        *highx = min(*highx, mblk->cpar_screen->ihamax);
        *lowy  = round(yy_1);
        *lowy  = max(*lowy, mblk->cpar_screen->ihbmin);
        *lowy  = min(*lowy, mblk->cpar_screen->ihbmax);
        *highy = round(yy_2);
        *highy = max(*highy, mblk->cpar_screen->ihbmin);
        *highy = min(*highy, mblk->cpar_screen->ihbmax);
    }
    else {
        *lowx  = round(xx_1);
        *lowx  = max(*lowx, 1);
        *lowx  = min(*lowx, DAT->isize);
        *highx = round(xx_2);
        *highx = max(*highx, 1);
        *highx = min(*highx, DAT->isize);
        if (yy_1 <= (float) INT_MIN)
            *lowy = INT_MIN;
        else
            *lowy = (int) yy_1; 
        if (yy_2 >= (float) INT_MAX)
            *highy = INT_MAX;
        else
            *highy = (int) yy_2; 
    }
}


