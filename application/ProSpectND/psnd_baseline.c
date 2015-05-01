/********************************************************************/
/*                          psnd_baseline.c                         */
/*                                                                  */
/* View and set baseline correction                                 */
/* 1997, Albert van Kuik                                            */
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
#include "nmrtool.h"


/**********************************************************
*
*/

enum region_types {
    REGION_CLEAR,
    REGION_READ,
    REGION_WRITE,
    REGION_SHOW,
    REGION_MOUSE
};


static void region_callback(G_POPUP_CHILDINFO *ci)
{
    G_EVENT ui;
    int ok = TRUE;
    MBLOCK *mblk = (MBLOCK*) ci->userdata;
    DBLOCK *dat = DAT;
    POPUP_INFO *popinf = mblk->popinf + POP_REGION;

    switch (ci->type) {
        case G_CHILD_OK:
        case G_CHILD_CANCEL:
            if (psnd_make_visible(mblk, mblk->info->block_id, S_BUFR1, FALSE))
                psnd_1d_reset_connection(mblk);
            g_menu_enable_item(mblk->info->mousebar_id, ID_MOUSE_REGION, FALSE);
            if (mblk->info->mouse_mode1 == MOUSE_BUFFER1)
                psnd_set_cursormode(mblk, -1, 0);   
            popinf->visible = FALSE;
            return;
    }
    switch (ci->id) {
        case REGION_CLEAR:
            if (g_popup_messagebox2(mblk->info->win_id, "Clear Buffer",
                        "Clear Buffer A (Region buffer)", "Yes", "No" ) 
                        == G_POPUP_BUTTON1) {
                psnd_arrayfill(0,NULL,2,0.0,PAR,DAT);
                psnd_plot(mblk,TRUE,mblk->info->dimension_id);
            }
            break;
        case REGION_READ:
            {
                char *filename = psnd_getfilename(mblk,"Read Region data","*");
                int newsize;
                if (filename)  
                    ok = psnd_array_in(mblk,filename, dat->xbufr1, dat->work1,
                                        &newsize);
            }
            break;
        case REGION_WRITE: 
            {
                char *filename = psnd_savefilename(mblk,"Write Region data","*");
                if (filename)
                    ok = psnd_array_out(mblk,filename, dat->xbufr1, 
                                         dat->work1, dat->isize, FALSE,
                                         dat->npar, dat->pars[0],dat);
            }
            break;
        case REGION_SHOW:
            {
                static G_EVENT ui;

                ui.event   = G_COMMAND;
                ui.win_id  = mblk->info->win_id;
                ui.keycode = PSND_BA;
                g_send_event(&ui);
            }
            break;
        case REGION_MOUSE:
            psnd_set_cursormode(mblk, 0, MOUSE_BUFFER1);
            break;
    }
    if (!ok) 
        psnd_printf(mblk," file not opened -- \n");
}

/*
 * Popup menu to define a region
 */
void psnd_region_popup(MBLOCK *mblk)
{
    int i, ok;
    static G_POPUP_CHILDINFO ci[20];
    int id;
    CBLOCK *cpar = mblk->cpar_screen;
    DBLOCK *dat  = DAT;
    POPUP_INFO *popinf = mblk->popinf + POP_REGION;

    if (popinf->visible)
        return;
    popinf->visible = TRUE;
    if (!popinf->cont_id) {
        int cont_id;
        cont_id = g_popup_container_open(mblk->info->win_id, 
                      "Select Region", 
                      G_POPUP_KEEP|G_POPUP_SINGLEBUTTON);
        popinf->cont_id = cont_id;
        id=0;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_PUSHBUTTON;
        ci[id].id    = REGION_CLEAR;
        ci[id].label = "Clear Region";
        ci[id].func  = region_callback;
        ci[id].userdata = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_PUSHBUTTON;
        ci[id].id    = REGION_READ;
        ci[id].label = "Read Region";
        ci[id].func  = region_callback;
        ci[id].userdata = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_PUSHBUTTON;
        ci[id].id    = REGION_WRITE;
        ci[id].label = "Write Region";
        ci[id].func  = region_callback;
        ci[id].userdata = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_PUSHBUTTON;
        ci[id].id    = REGION_MOUSE;
        ci[id].label = "Grab Mouse Button 1";
        ci[id].func  = region_callback;
        ci[id].userdata = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_OK;
        ci[id].func  = region_callback;
        ci[id].userdata = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_CANCEL;
        ci[id].func  = region_callback;
        ci[id].userdata = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));
    }
    if (psnd_make_visible(mblk, mblk->info->block_id, S_BUFR1, TRUE)) {
        static G_EVENT ui;

        psnd_1d_reset_connection(mblk);
        ui.event   = G_COMMAND;
        ui.win_id  = mblk->info->win_id;
        ui.keycode = PSND_PL;
        g_send_event(&ui);
    }
    g_menu_enable_item(mblk->info->mousebar_id, ID_MOUSE_REGION, TRUE);
    g_popup_container_show(popinf->cont_id) ;
}

enum showbaseline_types {
    BASELINE_PARAM=10,
    BASELINE_CALC,
    BASELINE_REGION,
    BASELINE_SPLINE,
    BASELINE_DOIT,
    BASELINE_MOUSE
};


static void update_labels(MBLOCK *mblk)
{
    POPUP_INFO *popinf = mblk->popinf + POP_BASELINE;

    if (popinf->cont_id) {
        char *label;
        label = psnd_sprintf_temp("%d", PAR->ibwater);
        g_popup_set_label(popinf->cont_id, ID_IBWATER, label);
    }
}

#define POPUP_LABEL_ID	0
void psnd_refresh_all_baseline_labels(MBLOCK *mblk)
{
    POPUP_INFO *popinf = mblk->popinf + POP_BASELINE;

    if (popinf->cont_id) {
        char *label;
        int select, window_select;

        label = psnd_sprintf_temp("Direction %d. %s", 
                                     PAR->icrdir, "BASELINE");
        g_popup_set_label(popinf->cont_id, POPUP_LABEL_ID, label);

        g_popup_set_selection(popinf->cont_id, ID_NBASE, PAR->nbase);
        param_convert_baseline_type(PAR, &select, &window_select);
        g_popup_set_selection(popinf->cont_id, ID_IBASE, select);
        g_popup_set_selection(popinf->cont_id, ID_IBASE_WND, window_select);
        label = psnd_sprintf_temp("%d", PAR->iterms);
        g_popup_set_label(popinf->cont_id, ID_ITERMS, label);
        label = psnd_sprintf_temp("%d", PAR->iterms2);
        g_popup_set_label(popinf->cont_id, ID_ITERMS2, label);
        label = psnd_sprintf_temp("%d", PAR->ibstrt);
        g_popup_set_label(popinf->cont_id, ID_IBSTRT, label);
        label = psnd_sprintf_temp("%d", PAR->ibstop);
        g_popup_set_label(popinf->cont_id, ID_IBSTOP, label);
        label = psnd_sprintf_temp("%d", PAR->ibwater);
        g_popup_set_label(popinf->cont_id, ID_IBWATER, label);
        label = psnd_sprintf_temp("%d", PAR->ibwidth);
        g_popup_set_label(popinf->cont_id, ID_IBWIDTH, label);
    }
}

static void showbaseline_callback(G_POPUP_CHILDINFO *ci)
{
    G_EVENT ui;
    int ok = TRUE;
    MBLOCK *mblk = (MBLOCK*) ci->userdata;
    POPUP_INFO *popinf = mblk->popinf + POP_BASELINE;
    DBLOCK *dat = DAT; 
    PBLOCK *par = PAR; 
    CBLOCK *cpar = mblk->cpar_screen; 

    switch (ci->type) {
        case G_CHILD_OK:
            par->nbase = 1;
            if (psnd_baseline(mblk, TRUE, FALSE, cpar)) {
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
            if (mblk->info->mouse_mode1 == MOUSE_BASELINE)
                psnd_set_cursormode(mblk, 0, 0);   
            popinf->visible = FALSE;
            return;
    }

    switch (ci->id) {
        case BASELINE_MOUSE:
            psnd_set_cursormode(mblk, MOUSE_BASELINE, MOUSE_BASELINE);
            break;

        case BASELINE_PARAM:
            if (par->ibase == NOBAS)
                par->ibase = BASPL1;
            if (par->iterms == 0)
                 par->iterms = DEF_ITERMS;
            if (psnd_set_param(mblk, 0, NULL, PSND_BM)==0)
                return;
            break;

        case BASELINE_CALC:
            if (psnd_baseline(mblk, FALSE, FALSE, cpar)) {
                static G_EVENT ui;
                if (psnd_make_visible(mblk, mblk->info->block_id, S_BASELN, TRUE))
                    psnd_1d_reset_connection(mblk);

                ui.event   = G_COMMAND;
                ui.win_id  = mblk->info->win_id;
                ui.keycode = PSND_PL;
                g_send_event(&ui);
            }
            break;

        case BASELINE_REGION:
            psnd_region_popup(mblk);
            break;

        case BASELINE_SPLINE:
            psnd_spline_popup(mblk);
            break;

        case BASELINE_DOIT:
            if (psnd_baseline(mblk, TRUE, FALSE, cpar)) {
                static G_EVENT ui;
                if (psnd_make_visible(mblk, mblk->info->block_id, S_BASELN, TRUE))
                    psnd_1d_reset_connection(mblk);

                ui.event   = G_COMMAND;
                ui.win_id  = mblk->info->win_id;
                ui.keycode = PSND_PL;
                g_send_event(&ui);
            }
            break;

        case ID_NBASE:
            par->nbase = ci->item;
            break;
        case ID_IBASE_WND:
            {
            int wid = (par->ibase+1) % 2;
            if (wid != ci->item) {
                if (ci->item)
                    par->ibase += 1;
                else
                    par->ibase -= 1;
            }
            } 
            break;
        case ID_IBASE:
            {
            int wid = (par->ibase+1) % 2;
            par->ibase = baseline_ids[ci->item] + wid;
            }
            break;
        case ID_ITERMS:
            par->iterms = psnd_scan_integer(ci->label);
            break;
        case ID_ITERMS2:
            par->iterms2 = psnd_scan_integer(ci->label);
            break;
        case ID_IBSTRT:
            par->ibstrt = psnd_scan_integer(ci->label);
            break;
        case ID_IBSTOP:
            par->ibstop = psnd_scan_integer(ci->label);
            break;
        case ID_IBWATER:
            par->ibwater = psnd_scan_integer(ci->label);
            break;
        case ID_IBWIDTH:
            par->ibwidth = psnd_scan_integer(ci->label);
            break;
    }
}

/*
 * Popup menu to test a baseline correction
 */
#define MAXID	30
void psnd_showbaseline_popup(MBLOCK *mblk)
{
    static G_POPUP_CHILDINFO ci[MAXID];
    int id;
    POPUP_INFO *popinf = mblk->popinf + POP_BASELINE;


    if (popinf->visible)
        return;
    popinf->visible = TRUE;
popinf->cont_id=0;
    if (!popinf->cont_id) {
        int cont_id;
        id = -1;
        popinf->cont_id = 
            g_popup_container_open(mblk->info->win_id, 
                                  "Baseline Correction", 
                                   0);
        cont_id = popinf->cont_id;
        id++;
        id = param_add_baseline1(mblk, cont_id, ci, id, 
                                 showbaseline_callback, PAR);
        id++;
        id = param_add_baseline2(mblk, cont_id, ci, id, 
                                 showbaseline_callback, PAR);
        id++;
        id = param_add_baseline3(mblk, cont_id, ci, id, 
                                 showbaseline_callback, PAR);
        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type      = G_CHILD_PUSHBUTTON;
        ci[id].id        = BASELINE_CALC;
        ci[id].label     = "Calc/Show Baseline";
        ci[id].func      = showbaseline_callback;
        ci[id].userdata  = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type      = G_CHILD_PUSHBUTTON;
        ci[id].id        = BASELINE_REGION;
        ci[id].label     = "Define Window (region)";
        ci[id].func      = showbaseline_callback;
        ci[id].userdata  = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type      = G_CHILD_PUSHBUTTON;
        ci[id].id        = BASELINE_SPLINE;
        ci[id].label     = "Define Spline Fit";
        ci[id].func      = showbaseline_callback;
        ci[id].userdata  = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type     = G_CHILD_PUSHBUTTON;
        ci[id].id       = BASELINE_MOUSE;
        ci[id].label    = "Grab Mouse Button 1 (Water select)";
        ci[id].func     = showbaseline_callback;
        ci[id].userdata = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));
         
        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type      = G_CHILD_OK;
        ci[id].func      = showbaseline_callback;
        ci[id].userdata  = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type      = G_CHILD_CANCEL;
        ci[id].func      = showbaseline_callback;
        ci[id].userdata  = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        assert(id<MAXID);
    }
    if (psnd_make_visible(mblk, mblk->info->block_id, S_BASELN, TRUE)) {
        static G_EVENT ui;

        psnd_1d_reset_connection(mblk);
        ui.event   = G_COMMAND;
        ui.win_id  = mblk->info->win_id;
        ui.keycode = PSND_PL;
        g_send_event(&ui);
    }
    g_menu_enable_item(mblk->info->mousebar_id, ID_MOUSE_SELECT, TRUE);
    psnd_set_cursormode(mblk, MOUSE_BASELINE, 0);
    g_popup_container_show(popinf->cont_id) ;
}

/***************/
/*
void psnd_clearbuf(int id, DBLOCK *dat)
{
    if (id == 0)
        memset(dat->xbufr1, 0, sizeof(float) * mblk->info->block_size);
    else 
        memset(dat->xbufr2, 0, sizeof(float) * mblk->info->block_size);
}
*/

/*
 * Mark parts of the real/imag array by setting
 * the corrsponding parts (regions) in buffer1 to
 * the value of 1
 *
 * if left < right: set region to 1
 * if left > right: set region to 0
 */
void psnd_cb_addvalue(int left, int right, DBLOCK *dat)
{
    int nsize = dat->isize;
    float bsign = 1;

    left  = max(1, left);
    right = max(1, right);
    left  = min(nsize, left);
    right = min(nsize, right);
    if (right < left) {
        int tmp = left;
        left    = right;
        right   = tmp;
        bsign   = 0;
    }

    xxfilv(dat->xbufr1, left, right, bsign);
}

static int baselinefit(MBLOCK *mblk, int ibstrt, int ibstop, PBLOCK *par, DBLOCK *dat,
                       float sfac, float bfac, float u, float v)
{
    int ier=0;
    /*
     * Automatic spline fit goes to spline section
     */
    if (par->ibase == BASAS1) {
        /*
         * First pre-fit to a strait line
         *
        bascrr(BASPL1,
               ibstrt,
               ibstop,
               dat->baseln,
               dat->xbufr1,
               dat->xbufr2,
               1,
               0,
               u,
               v,
               sfac,
               bfac,
               &ier);
               */
        psnd_spline_baseline(mblk,
                              dat->baseln+ibstrt-1, 
                              NULL, 
                              dat->work1, 
                              ibstop-ibstrt+1, 
                              par->iterms, 
                              u, 
                              v, 
                              sfac, 
                              bfac, 
                              &ier);
    }
    else if (par->ibase == BASAS2) {
        /*
         * First pre-fit to a strait line
         *
         */
        psnd_spline_baseline(mblk,
                              dat->baseln+ibstrt-1, 
                              dat->xbufr1+ibstrt-1, 
                              dat->work1, 
                              ibstop-ibstrt+1, 
                              par->iterms, 
                              u, 
                              v, 
                              sfac, 
                              bfac, 
                              &ier);
    }
    else {
        int ibase;
        /*
         * Pre-calculted spline is fitted by table-fit
         */
        if (par->ibase == BASSP1)
            ibase = BASTB1;
        else if (par->ibase == BASSP2) 
            ibase = BASTB2;
        else
            ibase = par->ibase;
        /*
         * Rest goes to baseline fit routines
         */
        bascrr(ibase,
               ibstrt,
               ibstop,
               dat->baseln,
               /* window */
               dat->xbufr1,
               /* table */
               dat->xbufr2,
               par->iterms,
               par->iterms2,
               u,
               v,
               sfac,
               bfac,
               &ier);
    }
    return ier;
}


int psnd_baseline(MBLOCK *mblk, int doit, int ask, CBLOCK *cpar)
{
    int i,ier=1,ibase,ibstrt,ibstop,ibwater=0,ibwidth=0;
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
    PBLOCK *par = PAR;
    DBLOCK *dat = DAT;
    
    if (!ask) {
        if (par->ibase == NOBAS)
            par->ibase = BASPL1;
        if (par->iterms == 0)
             par->iterms = DEF_ITERMS;
    }
    else  {
        if (par->ibase == NOBAS)
            par->ibase = BASPL1;
        if (par->iterms == 0)
             par->iterms = DEF_ITERMS;
        if (psnd_set_param(mblk, 0, NULL, PSND_BM)==0)
            return FALSE;
    }
    if (doit)
        par->nbase = TRUE;
    ibstrt = max(1,par->ibstrt);
    ibstop = par->ibstop;
    if (ibstop == 0)
        ibstop = dat->isize;
    memcpy(dat->baseln, dat->xreal, mblk->info->block_size * sizeof(float));

    if (par->ibwater) {
        int start,stop;
        ibwater = par->ibwater;
        ibwidth = max(0,par->ibwidth/2);
        start = ibstrt;
        stop  = ibwater-ibwidth;
        if (stop <= start || start < 1 || stop > dat->isize)
            ier = 0; 
        if (ier)           
            ier = baselinefit(mblk,start,stop,
                          par,dat,sfac, bfac, u, v);
        if (ier) {
            start = ibwater+ibwidth;
            stop  = ibstop;

            if (stop <= start || start < 1 || stop > dat->isize)
                ier = 0;            
            if (ier)
                invrse(dat->baseln+start-1, stop-start+1);
            if (ier)
                invrse(dat->xbufr1+start-1, stop-start+1);
            if (ier)
                invrse(dat->xbufr2+start-1, stop-start+1);
            if (ier)
                ier = baselinefit(mblk,start,stop,
                              par,dat,sfac, bfac, u, v);
            if (ier)
                invrse(dat->baseln+start-1, stop-start+1);
            if (ier)
                invrse(dat->xbufr1+start-1, stop-start+1);
            if (ier)
                invrse(dat->xbufr2+start-1, stop-start+1);
        }
    }
    else
        ier = baselinefit(mblk,ibstrt,ibstop,par,dat,sfac, bfac, u, v);
    
    if (ier != 1) {
        psnd_printf(mblk,"Error in baseline correction\n");
        memset(dat->baseln, 0, mblk->info->block_size * sizeof(float));
        return FALSE;
    }
        
    if (doit) {
        /*
         * Remember: baseline correction is only done on the real data
         * It can NOT be done on the imaginary data, because 
         * there is no baseline to fit (out of phase)!!
         */
        par->nbase = TRUE;
        memcpy(dat->xreal, dat->baseln, mblk->info->block_size * sizeof(float));
        memset(dat->baseln, 0, mblk->info->block_size * sizeof(float));
    }
    else {
        for (i=0;i<ibstrt-1;i++)
            dat->baseln[i] = 0;
        if (ibwater) {
            for (i=ibstrt-1;i<ibwater-ibwidth-1;i++)
                dat->baseln[i] = dat->xreal[i] - dat->baseln[i];
            for (i=ibwater-ibwidth-1;i<ibwater+ibwidth-1;i++)
                dat->baseln[i] = 0;
            for (i=ibwater+ibwidth-1;i<ibstop;i++)
                dat->baseln[i] = dat->xreal[i] - dat->baseln[i];
        }
        else 
            for (i=ibstrt-1;i<ibstop;i++)
                dat->baseln[i] = dat->xreal[i] - dat->baseln[i];
        for (i=ibstop;i<dat->isize;i++)
            dat->baseln[i] = 0;
    }
    return TRUE;
}

/*
 * Update new values in menu
 */
void psnd_refresh_baseline_labels(MBLOCK *mblk)
{
    update_labels(mblk);
}


