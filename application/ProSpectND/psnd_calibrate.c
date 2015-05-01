/********************************************************************/
/*                          psnd_calibrate.c                        */
/*                                                                  */
/* 1999, Albert van Kuik                                            */
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


static void update_labels(MBLOCK *mblk);

/***************************/
float psnd_calc_channels(float value, float ref, float aref, float bref,
                          int irc, int isspec, int axis_mode,
                          int nsize, float sw, float sf)
{
    ref = psnd_scale_xref(ref, aref, bref, irc);
    if (nsize == 1 || sf == 0 || sw == 0)
        axis_mode = AXIS_UNITS_NONE;
    if (axis_mode == AXIS_UNITS_AUTOMATIC) {
        if (isspec)
            axis_mode = AXIS_UNITS_PPM;
        else
            axis_mode = AXIS_UNITS_SECONDS;
    }
    switch (axis_mode) {
        case AXIS_UNITS_HERTZ:
            return ref + (1.0 - (float) nsize) * value/sw;
        case AXIS_UNITS_PPM:
            return ref + (1.0 - (float) nsize) * value * sf/sw;
        case AXIS_UNITS_SECONDS:
            return value * sw + 1;
    }
    return value;
}

float psnd_calc_pos(float value, float ref, float aref, float bref,
                     int irc, int isspec, int axis_mode, int nsize,
                     float sw, float sf, char *label, char *unit)
{
    float xq;
    
    ref = psnd_scale_xref(ref, aref, bref, irc);
    if (nsize != 1 && sf != 0 && sw != 0)
        xq = (value - ref) * sw / (1.0 - (float) nsize);
    else
        axis_mode = AXIS_UNITS_NONE;
    if (axis_mode == AXIS_UNITS_AUTOMATIC) {
        if (isspec)
            axis_mode = AXIS_UNITS_PPM;
        else
            axis_mode = AXIS_UNITS_SECONDS;
    }
    switch (axis_mode) {
        case AXIS_UNITS_HERTZ:
            if (label)
                sprintf(label,"%5.2f", xq);
            if (unit)
                strcpy(unit,"Hz");
            return xq;
        case AXIS_UNITS_PPM:
            if (unit)
                strcpy(unit,"ppm");
            if (label)
                sprintf(label,"%5.3f", xq/ sf);
            return xq/ sf;
        case AXIS_UNITS_SECONDS:
            if (unit)
                strcpy(unit,"sec");
            if (label)
                sprintf(label,"%5.2f", (value-1.0)/sw);
            return (value-1)/sw;
    }
    if (unit)
        strcpy(unit,"");
    if (label)
        sprintf(label,"%5.0f", value);
    return value;
}

float psnd_calc_pos_ex(float value, float ref, float aref, float bref,
                        int irc, int isspec, int axis_mode, int nsize,
                        float sw, float sf, char *label, char *unit,
                        float *ppm, float *hz, float *sec, int *mode)
{
    float xq;
    
    ref = psnd_scale_xref(ref, aref, bref, irc);
    if (nsize != 1 && sf != 0 && sw != 0) {
        xq = (value - ref) * sw / (1.0 - (float) nsize);
        *hz  = xq;
        *ppm = xq/ sf;
        *sec = (value-1.0)/sw;
    }
    else {
        axis_mode = AXIS_UNITS_NONE;
        *hz  = 0;
        *ppm = 0;
        *sec = 0;
    }
    if (axis_mode == AXIS_UNITS_AUTOMATIC) {
        if (isspec)
            axis_mode = AXIS_UNITS_PPM;
        else
            axis_mode = AXIS_UNITS_SECONDS;
    }
    *mode = axis_mode;
    switch (axis_mode) {
        case AXIS_UNITS_HERTZ:
            if (label)
                sprintf(label,"%5.2f", xq);
            if (unit)
                strcpy(unit,"Hz");
            return xq;
        case AXIS_UNITS_PPM:
            if (unit)
                strcpy(unit,"ppm");
            if (label)
                sprintf(label,"%5.3f", xq/ sf);
            return xq/ sf;
        case AXIS_UNITS_SECONDS:
            if (unit)
                strcpy(unit,"sec");
            if (label)
                sprintf(label,"%5.2f", (value-1.0)/sw);
            return (value-1)/sw;
    }
    if (unit)
        strcpy(unit,"");
    if (label)
        sprintf(label,"%5.0f", value);
    return value;
}

int psnd_set_datasize(MBLOCK *mblk, int nsize, int updatesw, DBLOCK *dat)
{
    if (nsize <= 0  || nsize > mblk->info->block_size) {
        psnd_printf(mblk," SI %d out of range\n", nsize);
        return FALSE;
    }
    /*
     * Update current size
     */
    dat->isize = nsize;
    /* 
     * Store offset for calibration
     */
    dat->irc   = dat->pars[0]->irstrt;
    /*
     * Calculate calibrated sw
     */
    if (dat->pars[0]->nsiz > 1 && updatesw)
        dat->sw   = dat->pars[0]->swhold * (dat->isize-1)/
                                    (dat->pars[0]->nsiz-1);
    return TRUE;
}

int psnd_set_calibration(float xref, PBLOCK *par, DBLOCK *dat)
{
    par->xref = xref;
    return TRUE;
}

int psnd_set_calibration_aref(float aref, PBLOCK *par, DBLOCK *dat)
{
    par->aref = aref;
    return TRUE;
}

int psnd_set_calibration_bref(float bref, PBLOCK *par, DBLOCK *dat)
{
    par->bref = bref;
    return TRUE;
}

/*
 * scale xref to match the current size of the data set
 */
float psnd_scale_xref(float xref, float aref, float bref, int shift)
{ 
    if (aref < 0.1)
        return xref;
    return bref-shift+1+(xref-1.0)*aref;
}

/*
 * scale xref to the size of the time domain (fixed reference)
 */
float psnd_unscale_xref(float xref, float aref, float bref, int shift)
{ 
    if (aref < 0.1)
        return xref;
    return 1.0+(xref-bref+shift-1)/aref;
}

static int calibrate1d(MBLOCK *mblk, float ref, SBLOCK *spar, PBLOCK *par, DBLOCK *dat)
{
    int nsize, modex;
    float sw;
    float qref,xref;

    xref  = psnd_scale_xref(par->xref,par->aref,par->bref,dat->irc);
    nsize = 1 - dat->isize;
    sw    = dat->sw;
  
    modex = spar[S_AXISX_MARK].show;
    if (modex == AXIS_UNITS_AUTOMATIC) {
        if (dat->isspec)
            modex = AXIS_UNITS_PPM;
        else
            modex = AXIS_UNITS_SECONDS;
    }

    switch (modex) {
        case AXIS_UNITS_CHANNELS:
            qref = ref - xref;
            if (!psnd_rvalin(mblk,"ref (channel)  ?", 1, &qref))
                return FALSE;
            xref = ref - qref;
            break;
        case AXIS_UNITS_HERTZ:
            qref=(ref - xref) * sw / nsize;
            if (!psnd_rvalin(mblk,"ref (Hertz)  ?", 1, &qref))
                return FALSE;
            xref = ref - qref * nsize / sw;
            break;
        case AXIS_UNITS_PPM:
            qref=(ref - xref) * sw / nsize;
            qref = qref / par->sfd;
            if (!psnd_rvalin(mblk,"ref (ppm)  ?", 1, &qref))
                return FALSE;
            par->pxref = qref;
            qref = qref * par->sfd;
            xref = ref - qref * nsize / sw;
            break;
        default:
            g_popup_messagebox(mblk->info->win_id, "", "Cannot calibrate seconds", FALSE);
            return FALSE;
    }
    xref = psnd_unscale_xref(xref,par->aref, par->bref, dat->irc);
    psnd_set_calibration(xref, par, dat);
    update_labels(mblk);

    if (g_popup_messagebox2(mblk->info->win_id, "Save to File",
                        "Save new values to file ?", "Yes", "No" ) 
                        == G_POPUP_BUTTON1) {
/*
        int argc=2;
        char *argv[2];
        char label1[5], label2[5];
        argv[0] = label1;
        argv[1] = label2; 
        sprintf(label1, "ST");
        sprintf(label2, "%d", par->icrdir);
        psnd_setparam(mblk, argc, argv);
*/
        psnd_update_calibration_param(mblk);

    }

    return TRUE;
}

static int calibrate2d(MBLOCK *mblk, float xref, float yref, SBLOCK *spar, 
                        CBLOCK *cpar, DBLOCK *dat)
{
    int nsize, axis_mode;
    float sw, qref,pref;

    pref  = psnd_scale_xref(cpar->par1->xref,cpar->par1->aref,
                             cpar->par1->bref,dat->irc);
    nsize = 1 - (cpar->par1->irstop-cpar->par1->irstrt+1);
    sw    = cpar->par1->swhold * (cpar->par1->irstop-cpar->par1->irstrt)/ 
                                              (cpar->par1->nsiz-1);
    axis_mode = spar[S_AXISX_MARK].show;
    if (axis_mode == AXIS_UNITS_AUTOMATIC) {
        if (cpar->par1->isspec)
            axis_mode = AXIS_UNITS_PPM;
        else
            axis_mode = AXIS_UNITS_SECONDS;
    }

    switch (axis_mode) {
        case AXIS_UNITS_CHANNELS:
            qref = xref - pref;
            if (!psnd_rvalin(mblk,"X ref (channel)  ?", 1, &qref))
                return FALSE;
            pref = xref - qref;
            break;
        case AXIS_UNITS_HERTZ:
            qref=(xref - pref) * sw / nsize;
            if (!psnd_rvalin(mblk,"X ref (Hertz)  ?", 1, &qref))
                return FALSE;
            pref = xref - qref * nsize / sw;
            break;
        case AXIS_UNITS_PPM:
            qref=(xref - pref) * sw / nsize;
            qref = qref / cpar->par1->sfd;
            if (!psnd_rvalin(mblk,"X ref (ppm)  ?", 1, &qref))
                return FALSE;
            cpar->par1->pxref = qref;
            qref = qref * cpar->par1->sfd;
            pref = xref - qref * nsize / sw;
            break;
        default:
            g_popup_messagebox(mblk->info->win_id, "", "Cannot calibrate seconds", FALSE);
            return FALSE;
    }
    pref  = psnd_unscale_xref(pref,cpar->par1->aref,
                                           cpar->par1->bref, dat->irc);
    psnd_set_calibration(pref, cpar->par1, dat);


    pref  = psnd_scale_xref(cpar->par2->xref, cpar->par2->aref,
                             cpar->par2->bref, cpar->par2->irstrt);
    nsize = 1 - (cpar->par2->irstop-cpar->par2->irstrt+1);
    sw    = cpar->par2->swhold * (cpar->par2->irstop-cpar->par2->irstrt)/ 
                                              (cpar->par2->nsiz-1);
            
    axis_mode = spar[S_AXISY_MARK].show;
    if (axis_mode == AXIS_UNITS_AUTOMATIC) {
        if (cpar->par2->isspec)
            axis_mode = AXIS_UNITS_PPM;
        else
            axis_mode = AXIS_UNITS_SECONDS;
    }

    switch (axis_mode) {
        case AXIS_UNITS_CHANNELS:
            qref = yref - pref;
            if (!psnd_rvalin(mblk,"Y ref (channel)  ?", 1, &qref))
                return FALSE;
            pref = yref - qref;
            break;
        case AXIS_UNITS_HERTZ:
            qref=(yref - pref) * sw / nsize;
            if (!psnd_rvalin(mblk,"Y ref (Hertz)  ?", 1, &qref))
                return FALSE;
            pref = yref - qref * nsize / sw;
            break;
        case AXIS_UNITS_PPM:
            qref=(yref - pref) * sw / nsize;
            qref = qref / cpar->par2->sfd;
            if (!psnd_rvalin(mblk,"Y ref (ppm)  ?", 1, &qref))
                return FALSE;
            cpar->par2->pxref = qref;
            qref = qref * cpar->par2->sfd;
            pref = yref - qref * nsize / sw;
            break;
        default:
            g_popup_messagebox(mblk->info->win_id, "", "Cannot calibrate seconds", FALSE);
            return FALSE;
    }
    pref  = psnd_unscale_xref(pref, cpar->par2->aref,
                                           cpar->par2->bref, cpar->par2->irstrt);
    psnd_set_calibration(pref, cpar->par2, dat);
    update_labels(mblk);

    if (g_popup_messagebox2(mblk->info->win_id, "Save to File",
                        "Save new values to file ?", "Yes", "No" ) 
                        == G_POPUP_BUTTON1) {
/*
        int argc=2;
        char *argv[2];
        char label1[5], label2[5];
        argv[0] = label1;
        argv[1] = label2; 
        sprintf(label1, "ST");
        sprintf(label2, "%d", cpar->par1->icrdir);
        psnd_setparam(mblk, argc, argv);
        sprintf(label2, "%d", cpar->par2->icrdir);
        psnd_setparam(mblk, argc, argv);
*/
        psnd_update_calibration_param(mblk);

    }
    return TRUE;
}

int psnd_calibrate(MBLOCK *mblk, float xref, float yref)
{
    if (mblk->info->plot_mode == PLOT_2D)
        return calibrate2d(mblk, xref, yref, mblk->spar, mblk->cpar_screen, DAT);
    else
        return calibrate1d(mblk, xref, mblk->spar, PAR, DAT);
}


/********************************************************************
 *
 */
enum int1d_types {
    CALIB_MOUSE=10,
    CALIB_XREF,
    CALIB_AREF,
    CALIB_BREF,
    CALIB_TD
};


#define CALC_SHIFT(i)	(4*(i))

static void update_labels(MBLOCK *mblk)
{
    int i;
    POPUP_INFO *popinf = mblk->popinf + POP_CALIBRATE + DAT->ityp - 1;

    if (DAT->ityp < 1)
        return;
    if (popinf->cont_id) {
        char *label;
        for (i=0;i<DAT->ityp;i++) {
            label = psnd_sprintf_temp("%g", DAT->par0[i]->xref);
            g_popup_set_label(popinf->cont_id, CALIB_XREF+CALC_SHIFT(i), label);
            label = psnd_sprintf_temp("%g", DAT->par0[i]->aref);
            g_popup_set_label(popinf->cont_id, CALIB_AREF+CALC_SHIFT(i), label);
            label = psnd_sprintf_temp("%g", DAT->par0[i]->bref);
            g_popup_set_label(popinf->cont_id, CALIB_BREF+CALC_SHIFT(i), label);
            label = psnd_sprintf_temp("%d", DAT->par0[i]->td);
            g_popup_set_label(popinf->cont_id, CALIB_TD+CALC_SHIFT(i), label);
        }
    }
}

static void calibrate_callback(G_POPUP_CHILDINFO *ci)
{
    int i;
    MBLOCK *mblk = (MBLOCK*) ci->userdata;
    POPUP_INFO *popinf = mblk->popinf + POP_CALIBRATE + DAT->ityp - 1;

    if (DAT->ityp < 1)
        return;

    switch (ci->type) {
        case G_CHILD_OK:
        case G_CHILD_CANCEL:
            for (i=0;i<MAX_DIM;i++) {
                popinf = mblk->popinf + POP_CALIBRATE + i;
                popinf->visible = FALSE;
            }
            return;
    }
    for (i=0;i<DAT->ityp;i++) {
        if (ci->id > CALIB_TD)
            ci->id -= 4;
        else
            break;
    }
    switch (ci->id) {
        case CALIB_XREF:
            psnd_set_calibration(psnd_scan_float(ci->label), PAR, DAT);
            break;
        case CALIB_AREF:
            DAT->par0[i]->aref = psnd_scan_float(ci->label);
            break;
        case CALIB_BREF:
            DAT->par0[i]->bref = psnd_scan_float(ci->label);
            break;
        case CALIB_TD:
            DAT->par0[i]->td   = psnd_scan_integer(ci->label);
            break;
        case CALIB_MOUSE:
            psnd_set_cursormode(mblk, 0, MOUSE_CALIBRATE);
            break;
    }
}

#define MAXID	(10+7*MAX_DIM)
/*
 * Popup menu 
 */
void psnd_calibrate_popup(MBLOCK *mblk)
{
    int i, ok;
    static G_POPUP_CHILDINFO ci[MAXID];
    int id;
    char *label;
    int cont_id = 0;
    DBLOCK *dat = DAT;
    POPUP_INFO *popinf = mblk->popinf + POP_CALIBRATE + dat->ityp - 1;

    if (dat->ityp < 1)
        return;
    for (i=0;i<MAX_DIM;i++) {
        popinf = mblk->popinf + POP_CALIBRATE + i;
        if (popinf->visible)
            return;
    }
    popinf = mblk->popinf + POP_CALIBRATE + dat->ityp - 1;
    popinf->visible = TRUE;
    if (!popinf->cont_id) {
        int cont_id;
        
        cont_id = g_popup_container_open(mblk->info->win_id, "Calibrate", 
                           G_POPUP_KEEP|G_POPUP_SINGLEBUTTON);

        popinf->cont_id = cont_id;
        id = -1;

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type          = G_CHILD_LABEL;
        ci[id].id            = 0;
        ci[id].label         = "ref (0 ppm) = xref * aref + bref - 1";
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type          = G_CHILD_LABEL;
        ci[id].id            = 0;
        ci[id].label         = "where: aref = size/td and bref = store_start";
        g_popup_add_child(cont_id, &(ci[id]));

        for (i=0;i<dat->ityp;i++) {

        label = psnd_sprintf_temp("Direction %d", i+1);
        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type       = G_CHILD_PANEL;
        ci[id].item       = TRUE;
        ci[id].frame      = TRUE;
        ci[id].horizontal = TRUE;
        ci[id].title      = label;
        g_popup_add_child(cont_id, &(ci[id]));

        label = psnd_sprintf_temp("%g", dat->par0[i]->xref);
        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_TEXTBOX;
        ci[id].id            = CALIB_XREF+CALC_SHIFT(i);
        ci[id].title         = "xref";
        ci[id].func          = calibrate_callback;
        ci[id].userdata      = (void*) mblk;
        ci[id].item_count    = 20;
        ci[id].items_visible = 10;
        ci[id].label         = label;
        ci[id].horizontal    = TRUE;
        g_popup_add_child(cont_id, &(ci[id]));

        label = psnd_sprintf_temp("%g", dat->par0[i]->aref);
        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_TEXTBOX;
        ci[id].id            = CALIB_AREF+CALC_SHIFT(i);
        ci[id].title         = "aref";
        ci[id].func          = calibrate_callback;
        ci[id].userdata      = (void*) mblk;
        ci[id].item_count    = 20;
        ci[id].items_visible = 4;
        ci[id].label         = label;
        ci[id].horizontal    = TRUE;
        g_popup_add_child(cont_id, &(ci[id]));


        label = psnd_sprintf_temp("%g", dat->par0[i]->bref);
        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_TEXTBOX;
        ci[id].id            = CALIB_BREF+CALC_SHIFT(i);
        ci[id].title         = "bref";
        ci[id].func          = calibrate_callback;
        ci[id].userdata      = (void*) mblk;
        ci[id].item_count    = 20;
        ci[id].items_visible = 8;
        ci[id].label         = label;
        ci[id].horizontal    = TRUE;
        g_popup_add_child(cont_id, &(ci[id]));

        label = psnd_sprintf_temp("%d", dat->par0[i]->td);
        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_TEXTBOX;
        ci[id].id            = CALIB_TD+CALC_SHIFT(i);
        ci[id].title         = "td";
        ci[id].func          = calibrate_callback;
        ci[id].userdata      = (void*) mblk;
        ci[id].item_count    = 20;
        ci[id].items_visible = 8;
        ci[id].label         = label;
        ci[id].horizontal    = TRUE;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type       = G_CHILD_PANEL;
        ci[id].item       = FALSE;
        g_popup_add_child(cont_id, &(ci[id]));
        }
        
        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_PUSHBUTTON;
        ci[id].id    = CALIB_MOUSE;
        ci[id].label = "Grab Mouse Button 1";
        ci[id].func  = calibrate_callback;
        ci[id].userdata      = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_OK;
        ci[id].func  = calibrate_callback;
        ci[id].userdata      = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_CANCEL;
        ci[id].func  = calibrate_callback;
        ci[id].userdata      = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        assert (id<MAXID);
    }
    update_labels(mblk);
    g_popup_container_show(popinf->cont_id) ;
}

void psnd_refresh_calibrate_labels(MBLOCK *mblk)
{
    update_labels(mblk);
}


