/********************************************************************/
/*                          psnd_waterfit.c                         */
/*                                                                  */
/* View, fit and remove waterline                                   */
/* 1999, Albert van Kuik                                            */
/********************************************************************/
#include <stdio.h>
#include <stdlib.h>
#include <ctype.h>
#include <string.h>
#include <stdarg.h>
#include <assert.h>
#include <math.h>
#include "genplot.h"
#include "nmrtool.h"
#include "psnd.h"



enum showwaterfit_types {
    WATERFIT_PARAM=10,
    WATERFIT_CALC,
    WATERFIT_DOIT,
    WATERFIT_MOUSE
};

static void update_labels(MBLOCK *mblk)
{
    POPUP_INFO *popinf = mblk->popinf + POP_WATERFIT;

    if (popinf->cont_id) {
        char *label;
        label = psnd_sprintf_temp("%d", PAR->waterpos);
        g_popup_set_label(popinf->cont_id, ID_WATERPOS, label);
    }
}


static void showwaterfit_callback(G_POPUP_CHILDINFO *ci)
{
    G_EVENT ui;
    int ok = TRUE;
    MBLOCK *mblk = (MBLOCK*) ci->userdata;
    DBLOCK *dat = DAT;
    PBLOCK *par = PAR;
    POPUP_INFO *popinf = mblk->popinf + POP_WATERFIT;

    switch (ci->type) {
        case G_CHILD_OK:
            par->waterfit = 1;
            if (psnd_fit_waterline(mblk, 0, NULL, TRUE)) {
                static G_EVENT ui;

                ui.event   = G_COMMAND;
                ui.win_id  = mblk->info->win_id;
                ui.keycode = PSND_PL;
                g_send_event(&ui);
            }
            /* Fall through */
        case G_CHILD_CANCEL:
            if (psnd_make_visible(mblk, mblk->info->block_id, S_BASELN, FALSE))
                psnd_1d_reset_connection(mblk);
            g_menu_enable_item(mblk->info->mousebar_id, ID_MOUSE_SELECT, FALSE);
            if (mblk->info->mouse_mode1 == MOUSE_WATERFIT)
                psnd_set_cursormode(mblk, 0, 0);   
            popinf->visible = FALSE;
            return;
    }
    switch (ci->id) {

        case WATERFIT_MOUSE:
            psnd_set_cursormode(mblk, MOUSE_WATERFIT, MOUSE_WATERFIT);
            break;

        case WATERFIT_CALC:
            if (psnd_fit_waterline(mblk,0, NULL,FALSE)) {
                static G_EVENT ui;
                if (psnd_make_visible(mblk, mblk->info->block_id, S_BASELN, TRUE))
                    psnd_1d_reset_connection(mblk);

                ui.event   = G_COMMAND;
                ui.win_id  = mblk->info->win_id;
                ui.keycode = PSND_PL;
                g_send_event(&ui);
            }
            break;

        case ID_WATERPOS:
            par->waterpos = psnd_scan_integer(ci->label);
            break;
        case ID_WATERWID:
            par->waterwid = psnd_scan_integer(ci->label);
            break;
    }
}

/*
 * Popup menu to test a waterfit correction
 */
#define MAXID	30
void psnd_showwaterfit_popup(MBLOCK *mblk)
{
    G_POPUP_CHILDINFO ci[MAXID];
    int id;
    PBLOCK *par = PAR;
    DBLOCK *dat = DAT;
    POPUP_INFO *popinf = mblk->popinf + POP_WATERFIT;


    if (popinf->visible)
        return;
    popinf->visible = TRUE;
    popinf->cont_id = 0;


    if (!popinf->cont_id) {
        int cont_id;
        id = -1;
        cont_id = g_popup_container_open(mblk->info->win_id, 
                         "Waterline Removal",  G_POPUP_KEEP);

        popinf->cont_id = cont_id;
        id++;
        id = param_add_waterfit(mblk, cont_id, ci, id, 
                                showwaterfit_callback, par);

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_PUSHBUTTON;
        ci[id].id    = WATERFIT_CALC;
        ci[id].label = "Calc/Show Waterline Fit";
        ci[id].func  = showwaterfit_callback;
        ci[id].userdata = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type     = G_CHILD_PUSHBUTTON;
        ci[id].id       = WATERFIT_MOUSE;
        ci[id].label    = "Grab Mouse Button 1";
        ci[id].func     = showwaterfit_callback;
        ci[id].userdata = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_OK;
        ci[id].func  = showwaterfit_callback;
        ci[id].userdata = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_CANCEL;
        ci[id].func  = showwaterfit_callback;
        ci[id].userdata = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        assert(id<MAXID);
    }
    if (psnd_make_visible(mblk, mblk->info->block_id, 
                                                  S_BASELN, TRUE)) {
        static G_EVENT ui;

        psnd_1d_reset_connection(mblk);
        ui.event   = G_COMMAND;
        ui.win_id  = mblk->info->win_id;
        ui.keycode = PSND_PL;
        g_send_event(&ui);
    }
    psnd_set_cursormode(mblk, MOUSE_WATERFIT, 0);
    g_menu_enable_item(mblk->info->mousebar_id, ID_MOUSE_SELECT, TRUE);
    g_popup_container_show(popinf->cont_id) ;
}


/*
 * Start a the top of the waterline and go left or right
 * Go only lower, and thus skip peaks on the slope of the waterline
 *
 * xbuf = input array == spectrum
 * obuf = output array == new profile
 * rbuf = array with the 'slope intensity profile' of the spectrum
 * pos  = max (waterline) position
 * stop = end of profile. If stop > pos, go to right, else go to left
 *
 */
static void downhill(float *xbuf, float *obuf, float *rbuf, 
                          int pos, int stop)
{
    int i,j,idir,skip,sig;
    float y,dy,slope;
    
    if (pos > stop)
        idir = -1;
    else if (pos < stop)
        idir = 1;
    else
        return;

    y    = obuf[pos] = xbuf[pos];
    dy   = fabs(xbuf[pos+idir]-xbuf[pos]);
    dy   = max(dy,1e33);
    sig  = -1;
    skip = FALSE;
    slope = rbuf[pos];
    
    for (i=pos+idir,j=pos;i!=stop;i+=idir) {
        /*
         * Detect a change from rising to descending
         * or vice versa
         */
        if ((xbuf[i-idir]<xbuf[i]) != sig) {
            sig = (xbuf[i-idir]<xbuf[i]);
       /*     if (slope >= rbuf[pos]/44)*/
                skip = FALSE;
        }
        /*
         * A good slope point when:
         *   current slope is not larger than current
         *     'slope profile point'
         *   current intensity is not larger than intensity
         *     of previous point
         *   not in skipping mode or current dy less than
         *     previous dy value.
         */
        if (slope >= rbuf[i] && fabs(y) > fabs(xbuf[i]) && 
            (!skip || dy >= fabs(xbuf[i]-xbuf[i-idir]))) {
            /*
             * If we have skipped some peak area,
             * draw a line from here to the last good slope
             * point
             */
            if (j != i-idir) {
                int k;
                float g,f = (y-xbuf[i])/(idir * (i-j));
                for (k=i-idir,g=0.0;k!=j;k-=idir) {
                    g += f;
                    obuf[k] = xbuf[i]+g;
                }
            }
            else
                dy = fabs((xbuf[j]-xbuf[i])/(idir * (i-j)));
            y = obuf[i] = xbuf[i];
            j = i;
            skip = FALSE;
            if (slope > rbuf[i]) 
                slope = rbuf[i];

        }
        /*
         * When in a peak region, just copy 
         * the last good slope value
         */
        else {
            obuf[i] = y;
            skip = TRUE;
        }
    }
}

#define DEFAULT_TOP_WIDTH	12
int  psnd_fit_waterline(MBLOCK *mblk, int argc, char *argv[], int doit)
{
    int i,topwidth, pos,pos_left, pos_right;
    int pos_low, pos_high;
    float height, peak_low, peak_high;
    int position, width;
    PBLOCK *par = PAR;
    DBLOCK *dat = DAT;
    /* general baseline params 
       sfac	Improvement when "(sigma0-sigma1)/sigma0 > sfac "
       bfac	OK if result is within "bfac * sigma1 "
       u	Signals below "u * sigma0" are not used 
                to calc next sigma1
       v	Fit within view window "v * sigma1"
       where:  sigma0 = old 'dev. from zero'
               sigma1 = new 'dev. from zero'
    */
    float sfac = 0.05;
    float bfac = 0.125;
    float u = 4.0;
    float v = 3.0;
    int ier,left,right;


    position =  par->waterpos;
    width    =  par->waterwid;                      

    baseline_region(dat->xreal-1, dat->work2-1, dat->isize, TRUE);
    if (argc >= 2) {
        position = psnd_scan_integer(argv[1])-1;
        if (argc >= 3) {
            width = psnd_scan_integer(argv[2])-1;
        }
    }
    topwidth  = DEFAULT_TOP_WIDTH;
    topwidth += width;
    if (position <= topwidth/2 || position >= dat->isize-topwidth/2)
        return FALSE;

    par->waterpos = position;
    par->waterwid = width;
    /*
     * array starts at 0, not 1
     */
    position--;
    
    /*
     * Calculate position of highest position.
     * First we go looking for the position of the highest
     * peak in the 'position + or - topwidth/4' and
     * we take this as the REAL position of the waterline
     */
    height = fabs(dat->xreal[position]);
    pos = position;
    for (i=position-topwidth/4;i<=position+topwidth/4;i++) {
        if (height < fabs(dat->xreal[i])) {
            height = fabs(dat->xreal[i]);
            pos = i;
        }
    }
    position = pos;
    
    /*
     * Calculate max and min positions
     */
    peak_low = peak_high = dat->xreal[position];
    pos_low  = pos_high  = position;
    for (i=position-topwidth/2;i<=position+topwidth/2;i++) {
        if (dat->xreal[i] > peak_high) {
            peak_high = dat->xreal[i];
            pos_high = i;
        }
        if (dat->xreal[i] < peak_low) {
            peak_low = dat->xreal[i];
            pos_low = i;
        }
    }

    /*
     * check for disperse peak
     * This is when a peak with a different sign
     * is VERY close to our waterpeak
     */
    pos_left = pos_right = position;
    /*
     * Negative peak
     */
    if (dat->xreal[position] < 0.0) {
        /*
         * Check for positive disperse peak
         */
        if (dat->xreal[pos_high] > 0.0) {
            if (pos_high > position)
                pos_right = pos_high;
            else
                pos_left = pos_high;
        }
    }
    /*
     * Positive peak
     */
    else {
        /*
         * Check for negative disperse peak
         */
        if (dat->xreal[pos_low] < 0.0) {
            if (pos_low > position)
                pos_right = pos_low;
            else
                pos_left = pos_low;
        }
    }
    pos_left  = min(pos_left, position-width/2);
    pos_left  = max(2,pos_left);
    pos_right = max(pos_right,position+width/2);
    pos_right = min(dat->isize-2,pos_right);
    /*
     * Now construct a raw profile
     * Start a the top of the waterline and go left or right
     * Go only lower, and thus skip peaks on the slope of the waterline
     */
    downhill(dat->xreal, dat->work1, dat->work2, pos_left, -1);
    downhill(dat->xreal, dat->work1, dat->work2, pos_right, dat->isize);

    /*
     * Do not put a spline through the top of the water line.
     * Because of the high intensity, small errors have a huge
     * inpact. Start the fit at 1/8th height
     */
    left = pos_left;
    for (i=pos_left-1;i>0;i--) {
        if (dat->work2[i] < dat->work2[pos_left]/8)
            break;
        left = i;
    }
    right = pos_right;
    for (i=pos_right+1;i<dat->isize;i++) {
        if (dat->work2[i] < dat->work2[pos_right]/8)
            break;
        right = i;
    }
    psnd_spline_slope(mblk,  dat->work1, 
                              dat->work2, 
                              left, 
                              -1,
                              u, 
                              v, 
                              sfac, 
                              bfac, 
                              &ier);
    psnd_spline_slope(mblk,  dat->work1, 
                              dat->work2, 
                              right, 
                              dat->isize, 
                              u, 
                              v, 
                              sfac, 
                              bfac, 
                              &ier);
   
    /*
     * Do not try to fit the top of the waterline,
     * just copy it (will be set to zero)
     */
    for (i=pos_left;i<=pos_right;i++) 
        dat->work1[i] = dat->xreal[i];

    if (!doit) {
        memcpy(dat->baseln, dat->work1, sizeof(float) * dat->isize);
        return TRUE;
    }
    else {
        par->waterfit = TRUE;
        for (i=0;i<dat->isize;i++) 
            dat->xreal[i] -= dat->work1[i];
        memset(dat->baseln, 0, mblk->info->block_size * sizeof(float));
        return TRUE;
    }
}


/*
 * Update new values in menu
 */
void psnd_refresh_waterfit_labels(MBLOCK *mblk)
{
    update_labels(mblk);
}


#define POPUP_LABEL_ID	0
void  psnd_refresh_all_waterfit_labels(MBLOCK *mblk)
{
    POPUP_INFO *popinf = mblk->popinf + POP_WATERFIT;

    if (popinf->cont_id) {
        char *label;

        label = psnd_sprintf_temp("Direction %d. %s", 
                                     PAR->icrdir, "WATERFIT");
        g_popup_set_label(popinf->cont_id, POPUP_LABEL_ID, label);

        g_popup_set_selection(popinf->cont_id, ID_WATERFIT, PAR->waterfit);

        label = psnd_sprintf_temp("%d", PAR->waterpos);
        g_popup_set_label(popinf->cont_id, ID_WATERPOS, label);
        label = psnd_sprintf_temp("%d", PAR->waterwid);
        g_popup_set_label(popinf->cont_id, ID_WATERWID, label);
    }
}
