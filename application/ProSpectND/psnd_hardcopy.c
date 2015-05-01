/********************************************************************/
/*                            psnd_hardcopy.c                       */
/*                                                                  */
/* 1997, Albert van Kuik                                            */
/*                                                                  */
/********************************************************************/
#include <stdio.h>
#include <stdlib.h>
#include <ctype.h>
#include <string.h>
#include <assert.h>
#include <math.h>
#include "genplot.h"
#include "psnd.h"


static int  hardcopy_xaxis(MBLOCK *mblk, int win_id,CONPLOTQ_TYPE *cpq);
static int  hardcopy_yaxis(MBLOCK *mblk, int win_id,CONPLOTQ_TYPE *cpq);
static int  hardcopy_paperdef(MBLOCK *mblk, int win_id,CONPLOTQ_TYPE *cpq);
static int  hardcopy_plot_title(MBLOCK *mblk, int win_id,CONPLOTQ_TYPE *cpq);
static int  hardcopy_axis_setup(MBLOCK *mblk, int win_id,CONPLOTQ_TYPE *cpq);
static void hardcopy_callback(G_POPUP_CHILDINFO *ci);


typedef struct {
    float x0, y0;
    float width, height;
} paper_settings;

static paper_settings paper_def[] = {
    /* 
     * A4 Landscape (29.7 * 21.0)
     */
    {1.3, 1.3, 27.2, 18.0 },
    /* 
     * A4 Portrait (21.0 * 29.7)
     */
    {1.3, 1.3, 18.0, 27.2 },

    /* 
     * A3 Landscape (29.7 * 42.0)
     */
    {1.1, 1.1, 39.5, 27.9 },
    /* 
     * A3 Portrait  (42.0 * 29.7)
     */
    {1.1, 1.1, 27.9, 39.5 },

    /* 
     * Letter Landscape (27.94 * 21.59)
     */
    {1.3, 1.3, 25.2, 18.0 },
    /* 
     * Letter Portrait (21.59 * 27.94)
     */
    {1.3, 1.3, 18.0, 25.2 },

    /* 
     * Legal Landscape (21.59 * 35.56)
     */
    {1.1, 1.1, 33.0, 19.0 },
    /* 
     * Legal Portrait  (35.56 * 21.59)
     */
    {1.1, 1.1, 19.0, 33.0 },

    /* 
     * User
     */
    {1.3, 1.3, 27.2, 18.0 },
    {1.3, 1.3, 18.0, 27.2 }
};

typedef struct {
    float width, height;
} paper_size;

#define NUM_PAPER_SIZES	5
static paper_size paper_sizedef[NUM_PAPER_SIZES] = {
    {A4_WIDTH_IN_CM, A4_HEIGHT_IN_CM },
    {A3_WIDTH_IN_CM, A3_HEIGHT_IN_CM },
    {LETTER_WIDTH_IN_CM, LETTER_HEIGHT_IN_CM },
    {LEGAL_WIDTH_IN_CM, LEGAL_HEIGHT_IN_CM },
    {A4_WIDTH_IN_CM, A4_HEIGHT_IN_CM }
};


static float chan2ppm(float chan, PBLOCK *par, DBLOCK *dat, int use_xref)
{
    if (use_xref)
        return psnd_chan2ppm(chan, par->sfd, par->swhold, 
                          par->nsiz, par->xref, par->aref, par->bref, dat->irc);
    else
        return psnd_chan2ppm(chan, par->sfd, par->swhold, 
                          par->nsiz, 0.0, par->aref, par->bref, dat->irc);
}

static float ppm2chan(float ppm, PBLOCK *par, DBLOCK *dat, int use_xref)
{
    if (use_xref)
        return psnd_ppm2chan(ppm, par->sfd, par->swhold, 
                          par->nsiz, par->xref, par->aref, par->bref, dat->irc);
    else
        return psnd_ppm2chan(ppm, par->sfd, par->swhold, 
                          par->nsiz, 0.0, par->aref, par->bref, dat->irc);
}

static float chan2hz(float chan, PBLOCK *par, DBLOCK *dat, int use_xref)
{
    if (use_xref)
        return psnd_chan2hz(chan, par->swhold, 
                          par->nsiz, par->xref, par->aref, par->bref, dat->irc);
    else
        return psnd_chan2hz(chan, par->swhold, 
                          par->nsiz, 0.0, par->aref, par->bref, dat->irc);
}

static float hz2chan(float hz, PBLOCK *par, DBLOCK *dat, int use_xref)
{
    if (use_xref)
        return psnd_hz2chan(hz, par->swhold, 
                          par->nsiz, par->xref, par->aref, par->bref, dat->irc);
    else
        return psnd_hz2chan(hz, par->swhold, 
                          par->nsiz, 0.0, par->aref, par->bref, dat->irc);
}


/*
 * Calculate 'left' in channels from 'left' in the current unit
 */
static void calc_xminv(CONPLOTQ_TYPE *cpq, float left)
{
    if (cpq->unitsx == UNIT_PPM) {
        if (cpq->is_2d) 
            left   = ppm2chan(left,   cpq->par1, cpq->dat,TRUE);
        else 
            left   = psnd_ppm2chan(left, 
                cpq->par1->sfd, cpq->dat->sw, 
                cpq->dat->isize, cpq->par1->xref,
                cpq->par1->aref, cpq->par1->bref,
                cpq->dat->irc);
    }
    else if (cpq->unitsx == UNIT_HERTZ) {
        if (cpq->is_2d) 
            left   = hz2chan(left,   cpq->par1, cpq->dat,TRUE);
        else 
            left   = psnd_hz2chan(left, 
                cpq->dat->sw, 
                cpq->dat->isize, cpq->par1->xref,
                cpq->par1->aref, cpq->par1->bref,
                cpq->dat->irc);
    }
    else if (cpq->unitsx == UNIT_SEC) {
        if (cpq->is_2d) 
            left   = psnd_sec2chan(left, 
                                    cpq->par1->swhold);
        else 
            left   = psnd_sec2chan(left, 
                                    cpq->dat->sw);
    }
    if (cpq->is_2d) 
        cpq->xminv = inside(1, left,   cpq->par1->nsiz);
    else 
        cpq->xminv = inside(1, left,   cpq->dat->isize);
}


/*
 * Calculate 'right' in channels from 'right' in the current unit
 */
static void calc_xmaxv(CONPLOTQ_TYPE *cpq, float right)
{
    if (cpq->unitsx == UNIT_PPM) {
        if (cpq->is_2d) 
            right  = ppm2chan(right,  cpq->par1, cpq->dat,TRUE);
        else 
            right = psnd_ppm2chan(right, 
                cpq->par1->sfd, cpq->dat->sw, 
                cpq->dat->isize, cpq->par1->xref,
                cpq->par1->aref, cpq->par1->bref,
                cpq->dat->irc);
    }
    else if (cpq->unitsx == UNIT_HERTZ) {
        if (cpq->is_2d) 
            right  = hz2chan(right,  cpq->par1, cpq->dat,TRUE);
        else 
            right = psnd_hz2chan(right, 
                cpq->dat->sw, 
                cpq->dat->isize, cpq->par1->xref,
                cpq->par1->aref, cpq->par1->bref,
                cpq->dat->irc);
    }
    else if (cpq->unitsx == UNIT_HERTZ) {
        if (cpq->is_2d) 
            right = psnd_sec2chan(right, 
                                   cpq->par1->swhold);
        else 
            right = psnd_sec2chan(right, 
                                   cpq->dat->sw);
    }
    if (cpq->is_2d) 
        cpq->xmaxv = inside(1, right,  cpq->par1->nsiz);
    else 
        cpq->xmaxv = inside(1, right,  cpq->dat->isize);
}

/*
 * Calculate 'bottom' in channels from 'bottom' in the current unit
 */
static void calc_yminv(CONPLOTQ_TYPE *cpq, float bottom)
{
    if (cpq->unitsy == UNIT_PPM) {
        if (cpq->is_2d) 
            bottom = ppm2chan(bottom, cpq->par2, cpq->dat,TRUE);
    }
    else if (cpq->unitsy == UNIT_HERTZ) {
        if (cpq->is_2d) 
            bottom = hz2chan(bottom, cpq->par2, cpq->dat,TRUE);
    }
    else if (cpq->unitsy == UNIT_SEC) {
        if (cpq->is_2d) 
            bottom = psnd_sec2chan(bottom, 
                                    cpq->par2->swhold);
    }
    if (cpq->is_2d) 
        cpq->yminv = inside(1, bottom, cpq->par2->nsiz);
    else 
        cpq->yminv = bottom;
}

/*
 * Calculate 'top' in channels from 'top' in the current unit
 */
static void calc_ymaxv(CONPLOTQ_TYPE *cpq, float top)
{
    if (cpq->unitsy == UNIT_PPM) {
        if (cpq->is_2d) 
            top    = ppm2chan(top,    cpq->par2, cpq->dat,TRUE);
    }
    else if (cpq->unitsy == UNIT_HERTZ) {
        if (cpq->is_2d) 
            top    = hz2chan(top,    cpq->par2, cpq->dat,TRUE);
    }
    else if (cpq->unitsy == UNIT_SEC) {
        if (cpq->is_2d) 
            top    = psnd_sec2chan(top, 
                                    cpq->par2->swhold);
    }
    if (cpq->is_2d) 
        cpq->ymaxv = inside(1, top,    cpq->par2->nsiz);
    else 
        cpq->ymaxv = top;
}

/*
 * Make all plot parameters consistent
 */
static void process_settings(CONPLOTQ_TYPE *cpq, CBLOCK *cpar)
{
    cpq->option = 0;
    switch (cpq->output_type) {
        case 0: /* HPGL */
            cpq->device = G_HPGL;
            break;
        case 1: /* BW Postscript */
            cpq->device = G_POSTSCRIPT;
            if (!cpq->options[1])
                cpq->option |= G_WIN_EPS;
            cpq->option |= G_WIN_BLACK_ON_WHITE;
            break;
        case 2: /* Color Postscript */
            cpq->device = G_POSTSCRIPT;
            if (!cpq->options[1])
                cpq->option |= G_WIN_EPS;
            break;
        case 3: /* Windows Meta File */
            cpq->device = G_WMF;
            break;
#ifdef _WIN32
        case 4: /* Windows Printer */
            cpq->device = G_PRINTER;
            break;
        case 5: /* Windows Printer */
            cpq->device = G_PRINTER;
            cpq->option |= G_WIN_BLACK_ON_WHITE;
            break;
#endif
        default:
            cpq->device = G_POSTSCRIPT;
            if (!cpq->options[1])
                cpq->option |= G_WIN_EPS;
            cpq->option |= G_WIN_BLACK_ON_WHITE;
            break;
    }
    switch (cpq->paper_type) {
        case 0: /* A4 */
            if (cpq->options[1])
                cpq->option |= G_WIN_A4;
            cpq->dev_width = paper_sizedef[0].width;
            cpq->dev_height= paper_sizedef[0].height;
            break;
        case 1: /* A3 */        
            cpq->dev_width = paper_sizedef[1].width;
            cpq->dev_height= paper_sizedef[1].height;
            if (cpq->options[1])
                cpq->option |= G_WIN_A3;
            break;
        case 2: /* Letter */        
            cpq->dev_width = paper_sizedef[2].width;
            cpq->dev_height= paper_sizedef[2].height;
            if (cpq->options[1])
                cpq->option |= G_WIN_LETTER;
            break;
        case 3: /* Legal */        
            cpq->dev_width = paper_sizedef[3].width;
            cpq->dev_height= paper_sizedef[3].height;
            if (cpq->options[1])
                cpq->option |= G_WIN_LEGAL;
            break;
        default:
            cpq->dev_width = paper_sizedef[2].width;
            cpq->dev_height= paper_sizedef[2].height;
            break;
    }
    cpq->paper_id = 2 * cpq->paper_type + cpq->paper_orientation;
    if (cpq->paper_orientation==1)
        cpq->option |= G_WIN_PORTRAIT;    
    if (cpq->options[0])
        cpq->option |= G_WIN_PCL5;
    cpq->offx	= paper_def[cpq->paper_id].x0;
    cpq->offy	= paper_def[cpq->paper_id].y0;
    cpq->width	= paper_def[cpq->paper_id].width;
    cpq->height	= paper_def[cpq->paper_id].height;
    if (cpq->is_2d) {
        cpq->xminpv = psnd_chan2ppm(cpq->xminv, cpq->par1->sfd, cpq->par1->swhold, 
                          cpq->par1->nsiz, cpq->par1->xref,
                          cpq->par1->aref, cpq->par1->bref, cpar->ihamin);
        cpq->xmaxpv = psnd_chan2ppm(cpq->xmaxv, cpq->par1->sfd, cpq->par1->swhold, 
                          cpq->par1->nsiz, cpq->par1->xref,
                          cpq->par1->aref, cpq->par1->bref, cpar->ihamin);
        cpq->yminpv = psnd_chan2ppm(cpq->yminv, cpq->par2->sfd, cpq->par2->swhold, 
                          cpq->par2->nsiz, cpq->par2->xref, 
                          cpq->par2->aref, cpq->par2->bref, cpar->ihbmin);
        cpq->ymaxpv = psnd_chan2ppm(cpq->ymaxv, cpq->par2->sfd, cpq->par2->swhold, 
                          cpq->par2->nsiz, cpq->par2->xref, 
                          cpq->par2->aref, cpq->par2->bref, cpar->ihbmin);

        cpq->xminhv = psnd_chan2hz(cpq->xminv, cpq->par1->swhold, 
                          cpq->par1->nsiz, cpq->par1->xref,
                          cpq->par1->aref, cpq->par1->bref, cpar->ihamin);
        cpq->xmaxhv = psnd_chan2hz(cpq->xmaxv, cpq->par1->swhold, 
                          cpq->par1->nsiz, cpq->par1->xref,
                          cpq->par1->aref, cpq->par1->bref, cpar->ihamin);
        cpq->yminhv = psnd_chan2hz(cpq->yminv, cpq->par2->swhold, 
                          cpq->par2->nsiz, cpq->par2->xref, 
                          cpq->par2->aref, cpq->par2->bref, cpar->ihbmin);
        cpq->ymaxhv = psnd_chan2hz(cpq->ymaxv, cpq->par2->swhold, 
                          cpq->par2->nsiz, cpq->par2->xref, 
                          cpq->par2->aref, cpq->par2->bref, cpar->ihbmin);

        cpq->xminsv = psnd_chan2sec(cpq->xminv, cpq->par1->swhold);
        cpq->xmaxsv = psnd_chan2sec(cpq->xmaxv, cpq->par1->swhold);
        cpq->yminsv = psnd_chan2sec(cpq->yminv, cpq->par2->swhold);
        cpq->ymaxsv = psnd_chan2sec(cpq->ymaxv, cpq->par2->swhold);
    }
    else {
        cpq->xminpv = psnd_chan2ppm(cpq->xminv, cpq->par1->sfd, cpq->dat->sw, 
                          cpq->dat->isize, cpq->par1->xref,
                          cpq->par1->aref, cpq->par1->bref,cpq->dat->irc);
        cpq->xmaxpv = psnd_chan2ppm(cpq->xmaxv, cpq->par1->sfd, cpq->dat->sw, 
                          cpq->dat->isize, cpq->par1->xref, 
                          cpq->par1->aref, cpq->par1->bref, cpq->dat->irc);

        cpq->xminhv = psnd_chan2hz(cpq->xminv, cpq->dat->sw, 
                          cpq->dat->isize, cpq->par1->xref,
                          cpq->par1->aref, cpq->par1->bref,cpq->dat->irc);
        cpq->xmaxhv = psnd_chan2hz(cpq->xmaxv, cpq->dat->sw, 
                          cpq->dat->isize, cpq->par1->xref, 
                          cpq->par1->aref, cpq->par1->bref, cpq->dat->irc);

        cpq->xminsv = psnd_chan2sec(cpq->xminv, cpq->dat->sw);
        cpq->xmaxsv = psnd_chan2sec(cpq->xmaxv, cpq->dat->sw);

    }
    /*
     *  physical frame limits for the plotter
     */
    cpq->clentx = cpq->offx + cpq->width;
    cpq->clenty = cpq->offy + cpq->height;
    cpq->cminx  = cpq->offx;
    cpq->cminy  = cpq->offy;
    if (cpq->is_2d && cpq->show_yaxis) 
        cpq->cminx += cpq->cloffx;
    if (cpq->show_xaxis) 
        cpq->cminy += cpq->cloffy;
    cpq->cmaxx = cpq->clentx;
    cpq->cmaxy = cpq->clenty;
    /*
     *    determine fixed HZ/CM scaling
     */
    if (cpq->fix == FIXED_HZ_CM) {
        if (cpq->is_2d) {
            float sbx, sby, delx, dely, tw;
            if (cpq->show_title) 
                tw = cpq->twidth;
            else
                tw = 0.0;
   
            sbx		= cpq->par1->swhold * (cpq->xmaxv - cpq->xminv)/
                  	      (cpq->par1->nsiz - 1);
            sby		= cpq->par2->swhold * (cpq->ymaxv - cpq->yminv)/
                  	      (cpq->par2->nsiz - 1);
            delx	= sbx/cpq->hzx;
            dely	= sby/cpq->hzy;
            cpq->nqx  	= (1.+(delx)/(cpq->clentx - cpq->cminx));
            cpq->nqy  	= (1.+(dely)/(cpq->clenty - tw - cpq->cminy));
            delx 	/= cpq->nqx;
            dely 	/= cpq->nqy;
            cpq->cmaxx	= cpq->cminx + delx;
            cpq->cmaxy	= cpq->cminy + dely + tw;
        }
        else {
            float sbx, delx;
            sbx		= cpq->dat->sw * (cpq->xmaxv - cpq->xminv)/
                  	      (cpq->dat->isize - 1);
            delx	= sbx/cpq->hzx;
            cpq->nqx  	= (1.+(delx)/(cpq->clentx - cpq->cminx));
            delx 	/= cpq->nqx;
            cpq->cmaxx	= cpq->cminx + delx;
        }
    }
    /*
     *    determine full scaling
     */
    else if (cpq->fix == AUTO_SCALE) {
        cpq->nqx  	= 1;
        cpq->nqy  	= 1;
        if (cpq->is_2d) {
            float scalx, dx, dy;
            if (cpq->unitsx != UNIT_CHAN || cpq->unitsy != UNIT_CHAN) {
                dx      = cpq->xmaxpv - cpq->xminpv;
                dy      = cpq->ymaxpv - cpq->yminpv;
            }
            else {
                dx      = cpq->xmaxv - cpq->xminv;
                dy      = cpq->ymaxv - cpq->yminv;
            }
            scalx = dx/dy;
            if (cpq->paper_orientation==0) {
                /*
                 * landscape
                 */
                cpq->cmaxx	= cpq->cminx + scalx 
                                      * (cpq->clenty - cpq->cminy);
                if (cpq->cmaxx < cpq->clentx) 
                    cpq->cmaxy = cpq->clenty;
                else {
                    cpq->cmaxx = cpq->clentx;
                    cpq->cmaxy = cpq->cminy + (cpq->clentx - cpq->cminx)/scalx;
                }
            }
            else {
                /*
                 * portrait
                 */
                cpq->cmaxy	= cpq->cminy + scalx 
                                      * (cpq->clentx - cpq->cminx);
                if (cpq->cmaxy < cpq->clenty) 
                    cpq->cmaxx = cpq->clentx;
                else {
                    cpq->cmaxy = cpq->clenty;
                    cpq->cmaxx = cpq->cminx + (cpq->clenty - cpq->cminy)/scalx;
                }
            }
            if (cpq->is_2d && cpq->show_title) 
                cpq->cmaxx -= cpq->twidth;
        }
    }
    /*
     *    determine full scaling, no aspect/ratio
     *    For 1D FILL_PAPER == AUTO_SCALE
     */
    else if (cpq->fix == FILL_PAPER) {
        cpq->nqx  	= 1;
        cpq->nqy  	= 1;
        if (cpq->is_2d) {
            cpq->cmaxx = cpq->clentx;
            cpq->cmaxy = cpq->clenty;
        }
    }
    cpq->lymax = cpq->cmaxy;
    cpq->tymax = cpq->cmaxy;
    if (!cpq->is_2d) {
        if (cpq->peakpick) {
            cpq->cmaxy -= cpq->lwidth;
        }
    }
    if (cpq->show_title) {
        cpq->cmaxy -= cpq->twidth;
        cpq->lymax -= cpq->twidth;
    }
    
}

void psnd_process_plot_settings(CONPLOTQ_TYPE *cpq, CBLOCK *cpar)
{
    process_settings(cpq, cpar);  
}

/*
 * Plot the current window
 */
void psnd_hardcopy(MBLOCK *mblk) 
{
    int cont_id;
    int dest_id;
    int val;
    int device;
    int option=0;
    char *filename, *title, emptystring[2]=" ";
    FILE *outfile;
    float x0=0.5, y0=1.1, xd=29.0, yd=19.0;
    float dev_width=0.5, dev_height=1.1;
    float width, height, asp_ratio;
    
    int i;
#ifdef _WIN32
    char *radio_type[] = {"HPGL", "BW Postscript", "Color Postscript",
                          "Windows Meta File", "Color Printer", "BW Printer" };
    static int radio_type_default = 5;
    static int radio_type_count = 6;
#else
    char *radio_type[] = {"HPGL", "BW Postscript", "Color Postscript",
                          "Windows Meta File"};
    static int radio_type_default = 1;
    static int radio_type_count = 4;
#endif
    char *radio_paper[] = {"A4", "A3", "Letter", "Legal", "User"};
    static int radio_paper_default = 0;
    char *radio_orientation[] = {"Landscape", "Portrait"};
    static int radio_orientation_default = 0;
    char *check_options[] = {"Include PCL5 control codes", 
                             "Include paper definition",
                             "Keep aspect ratio"};
    static int check_options_select[] = {0,1,1};
    static int type_id, paper_id, orientation_id, size_id, options_id;
    static int paper_flag, aspect_flag, orient_flag;
    
    G_POPUP_CHILDINFO ci[20];
    int id,ok;
             
    cont_id = g_popup_container_open(mblk->info->win_id, "HardCopy box",
                                    G_POPUP_WAIT);
          
    id = 0;
    g_popup_init_info(&(ci[id]));
    ci[id].type  = G_CHILD_LABEL;
    ci[id].id     = id;
    ci[id].label = "Adjust Hardcopy parameters";
    g_popup_add_child(cont_id, &(ci[id]));
             
    id++;
    type_id = id;
    g_popup_init_info(&(ci[id]));
    ci[id].type = G_CHILD_OPTIONMENU;
    ci[id].id           = id;
    ci[id].title        = "Output type";
    ci[id].frame        = TRUE;
    ci[id].horizontal   = TRUE;
    ci[id].item_count   = radio_type_count;
    ci[id].data         = radio_type;
    ci[id].item         = radio_type_default;
    g_popup_add_child(cont_id, &(ci[id]));
         
    id++;
    paper_id = id;
    g_popup_init_info(&(ci[id]));
    ci[id].type = G_CHILD_RADIOBOX;
    ci[id].id            = id;
    ci[id].title        = "Paper type";
    ci[id].frame        = TRUE;
    ci[id].horizontal = TRUE;
    ci[id].item_count = NUM_PAPER_SIZES-1;
    ci[id].data         = radio_paper;
    ci[id].item      = radio_paper_default;
    g_popup_add_child(cont_id, &(ci[id]));
         
    id++;
    orientation_id = id;
    g_popup_init_info(&(ci[id]));
    ci[id].type = G_CHILD_RADIOBOX;
    ci[id].id            = id;
    ci[id].title        = "Paper Orientation";
    ci[id].frame        = TRUE;
    ci[id].horizontal = TRUE;
    ci[id].item_count = 2;
    ci[id].data         = radio_orientation;
    ci[id].item      = radio_orientation_default;
    g_popup_add_child(cont_id, &(ci[id]));
         
    id++;
    options_id = id;
    g_popup_init_info(&(ci[id]));
    ci[id].type = G_CHILD_CHECKBOX;
    ci[id].id            = id;
    ci[id].title        = "Various options";
    ci[id].frame        = TRUE;
    ci[id].horizontal = FALSE;
    ci[id].item_count = 3;
    ci[id].data         = check_options;
    ci[id].select      = check_options_select;
    g_popup_add_child(cont_id, &(ci[id]));
         
    ok = g_popup_container_show(cont_id);
    if (!ok)
        return;  
              
    for (i=0;i<=id;i++) {
/*     printf("i = %d; id = %d(%d)\n",i,ci[i].id,id);*/
     if (ci[i].id == type_id) {
         radio_type_default = ci[i].item;
         switch (ci[i].item) {
            case 0: /* HPGL */
               device = G_HPGL;
               break;
            case 1: /* BW Postscript */
               device = G_POSTSCRIPT;
               option |= G_WIN_BLACK_ON_WHITE;
               break;
            case 2: /* Color Postscript */
               device = G_POSTSCRIPT;
               break;
            case 3: /* Windows Meta File */
               device = G_WMF;
               break;
#ifdef _WIN32
            case 4: /* Windows Printer */
               device = G_PRINTER;
               break;
            case 5: /* Windows Printer */
               device = G_PRINTER;
               option |= G_WIN_BLACK_ON_WHITE;
               break;
#endif
            default:
               device = G_POSTSCRIPT;
               fprintf(stderr,"Device set to postscript. Should not be done here\n");
               break;
         }
     }
     else if (ci[i].id == paper_id) {
         radio_paper_default = ci[i].item;
         paper_flag = ci[i].item;
         dev_width = paper_sizedef[paper_flag].width;
         dev_height= paper_sizedef[paper_flag].height;
     }
     else if (ci[i].id == orientation_id) {
         radio_orientation_default = ci[i].item;
         orient_flag=ci[i].item;
     }
     else if (ci[i].id == options_id) {
         if (ci[i].select[0]) option |= G_WIN_PCL5;
         if (ci[i].select[1] && (paper_flag==0)) option |= G_WIN_A4;
         if (ci[i].select[1] && (paper_flag==1)) option |= G_WIN_A3;
         if (ci[i].select[1] && (paper_flag==2)) option |= G_WIN_LETTER;
         if (ci[i].select[1] && (paper_flag==3)) option |= G_WIN_LEGAL;
         aspect_flag=ci[i].select[2];
     }
   }
   /* Now set the dimensions alright */
   if (paper_flag==0 && orient_flag==0) {	/* A4 Landscape */
        x0=0.5;
        y0=1.3;
        xd=28.0;
        yd=18.0;
   }
   else if (paper_flag==0 && orient_flag==1) {	/* A4 Portrait */
        x0=0.5;
        y0=1.3;
        xd=20.0;
        yd=27.3;
        option |= G_WIN_PORTRAIT;
    }
    else if (paper_flag==1 && orient_flag==0) {	/* A3 Landscape */
        x0=0.5;
        y0=1.1;
        xd=40.0;
        yd=27.9;
    }
    else if (paper_flag==1 && orient_flag==1) {	/* A3 Portrait */
        x0=0.5;
        y0=1.1;
        xd=28.5;
        yd=39.5;
        option |= G_WIN_PORTRAIT;
    }
    else {
        x0=1.5;
        y0=1.5;
        if (orient_flag==0) {	
          xd=dev_width - 1.5;
          yd=dev_height - 1.5;
        }
        else {
          xd=dev_height - 1.5;
          yd=dev_width - 1.5;
          option |= G_WIN_PORTRAIT;
        }
    }
    if (aspect_flag) {
        g_get_windowsize(mblk->info->win_id, &width, &height);
        asp_ratio = width/height;
     
        if (asp_ratio > xd/yd)
            yd = xd/asp_ratio;
        else
            xd = yd * asp_ratio;
    }   
#ifdef _WIN32
    if (device == G_PRINTER) {
        if (g_open_device(G_PRINTER)== G_ERROR)
            return;
        dest_id = g_open_window(G_PRINTER, x0, y0, xd, yd,  
                      "Plot Screen", option);
        g_copy_window(dest_id, mblk->info->win_id);
        g_close_window(dest_id);
        g_close_device(G_PRINTER);
        g_select_window(mblk->info->win_id);
        return;
    }
#endif
    /* ask filename */
    filename = psnd_savefilename(mblk,"Hardcopy of current Window", "*");
    if (filename == NULL)
        return;
#ifdef _WIN32
    outfile = fopen(filename, "w+b");
#else
    outfile = fopen(filename, "w+");
#endif
    g_set_outfile(outfile);
        
    g_set_devicesize(device, dev_width, dev_height);
    dest_id = g_open_window(device, x0, y0, xd, yd, "", option);
    g_copy_window(dest_id, mblk->info->win_id);
    g_close_window(dest_id);
    fclose(outfile);
    g_select_window(mblk->info->win_id);
}

/*
CONPLOTQ_TYPE cpq_param_screen, cpq_param_hardcopy;
CONPLOTQ_TYPE *cpq_screen   = &cpq_param_screen;
CONPLOTQ_TYPE *cpq_hardcopy = &cpq_param_hardcopy;
*/
#define MAX_PEN_WIDTH	5

void psnd_init_paper_plot(CONPLOTQ_TYPE *cpq)
{
    int id=0;
    if (cpq->init)
        return;
    cpq->init 			= TRUE;
    cpq->cont_id 		= 0;
#ifdef _WIN32
    cpq->output_type		= 5;
#else
    cpq->output_type		= 1;
#endif
    cpq->pen_width		= 2;
    cpq->paper_type		= 0;
    cpq->paper_orientation	= 0;
    cpq->options[0]		= FALSE;
    cpq->options[1]		= TRUE;
    cpq->options[2]		= TRUE;
    cpq->unitsx 		= UNIT_PPM;
    cpq->unitsy 		= UNIT_PPM;
    cpq->cloffx			= 1.5;
    cpq->cloffy			= 1.5;
    cpq->xminv			= 1.0;
    cpq->xmaxv			= 1024.0;
    cpq->yminv			= 1.0;
    cpq->ymaxv			= 1024.0;
    cpq->xminv_store		= 1.0;
    cpq->xmaxv_store		= 1.0;
    cpq->yminv_store		= 1.0;
    cpq->ymaxv_store		= 1.0;
    cpq->xminpv			= 10.0;
    cpq->xmaxpv			= 0.0;
    cpq->yminpv			= 10.0;
    cpq->ymaxpv			= 0.0;
    cpq->xminhv			= 5000.0;
    cpq->xmaxhv			= 0.0;
    cpq->yminhv			= 5000.0;
    cpq->ymaxhv			= 0.0;
    cpq->xminsv			= 1.0;
    cpq->xmaxsv			= 0.0;
    cpq->yminsv			= 1.0;
    cpq->ymaxsv			= 0.0;
    cpq->fix 			= AUTO_SCALE;
    cpq->hzx			= 50.0;
    cpq->hzy			= 50.0;
    strcpy(cpq->axtxt, "PPM");
    strcpy(cpq->aytxt, "PPM");
    strcpy(cpq->plot_title, "");
    cpq->show_title		= FALSE;
    cpq->show_xaxis		= TRUE;
    cpq->xticks_autoscale	= TRUE;
    cpq->xlabels_ppm		= 1;
    cpq->xticks_ppm_short	= 10;
    cpq->xticks_ppm_medium	= 2;
    cpq->xticks_ppm_long	= 1;
    cpq->xticklen_short		= 0.15;
    cpq->xticklen_medium	= 0.22;
    cpq->xticklen_long		= 0.3;
    cpq->xoff_axis		= 0.2;
    cpq->xoff_labels		= 0.8;
    cpq->xstart_title		= 1.0;
    cpq->xoff_title		= cpq->cloffy - cpq->xoff_axis;
    cpq->show_yaxis		= TRUE;
    cpq->yticks_autoscale	= TRUE;
    cpq->ylabels_ppm		= 1;
    cpq->yticks_ppm_short	= 10;
    cpq->yticks_ppm_medium	= 2;
    cpq->yticks_ppm_long	= 1;
    cpq->yticklen_short		= 0.15;
    cpq->yticklen_medium	= 0.22;
    cpq->yticklen_long		= 0.3;
    cpq->yoff_axis		= 0.2;
    cpq->yoff_labels		= 0.4;
    cpq->ystart_title		= 1.0;
    cpq->yoff_title		= 1.0;
    cpq->ibox	  		= TRUE;
    cpq->igrid  		= 2;
    cpq->xgridlines_ppm		= 2;
    cpq->ygridlines_ppm		= 2;
    cpq->paper_id = 0;
    cpq->offx	= paper_def[id].x0;
    cpq->offy	= paper_def[id].y0;
    cpq->width	= paper_def[id].width;
    cpq->height	= paper_def[id].height;
    cpq->peakpick	= TRUE;
    cpq->thresh		= 1.0e7;
    cpq->sens		= 1.0;
    cpq->sign		= 0;
    cpq->avewidth	= 2;
    cpq->lheight	= 0.3;
    cpq->lwidth		= 6 * cpq->lheight;
    cpq->twidth		= 2 * 0.3;
    cpq->npart		= 5;
    cpq->preview_mode 	= TRUE;
}

static char *plot_truefalse[] = {
    "OFF", "ON"
};
 
static char *plot_truefalseauto[] = {
    "OFF", "ON", "AUTO"
};
 
static char *plot_outputtype[] = {
    "HPGL", "POSTSCRIPT", "POSTSCRIPT COLOR", "WMF"
};

static char *plot_papertype[] = {
    "A4", "A3", "LETTER", "LEGAL", "USER"
};

static char *plot_paperorientation[] = {
     "LANDSCAPE", "PORTRAIT"
};

static char *plot_units[] = {
     "PPM", "HERTZ", "SECONDS", "CHANNELS"
};

static char *plot_peakpicksign[] = {
     "NEGATIVE", "BOTH", "POSITIVE"
};

static char *plot_fix[] = {
     "AUTO", "FIXED", "FILL"
};

static char *plot_range[] = {
    "EXPLICIT", "FACTOR", "EQUIDISTANT", "LOGRANGE"
};

/*
 * Save Plot parameters to disk
 */
static void export_plotsettings(CONPLOTQ_TYPE *cpq, CBLOCK *cpar, FILE *outfile)
{
    int i;
    
    if (cpq->is_2d) 
        fprintf(outfile, "#\n# ==== 2D PLOT PARAMETERS ====\n#\n");
    else
        fprintf(outfile, "#\n# ==== 1D PLOT PARAMETERS ====\n#\n");
    fprintf(outfile, "#\n# Language: HPGL, Postscript, Postscript color\n#\n");
    fprintf(outfile, "LANGUAGE           %s\n", plot_outputtype[cpq->output_type]);
    fprintf(outfile, "#\n# Paper type: A4, A3, Letter, Legal or user\n#\n");
    fprintf(outfile, "PAPER TYPE         %s\n", plot_papertype[cpq->paper_type]);
    fprintf(outfile, "#\n# Paper orientation: landscape or portrait\n#\n");
    fprintf(outfile, "PAPER ORIENTATION  %s\n", plot_paperorientation[cpq->paper_orientation]);
    fprintf(outfile, "#\n# Paper size in cm\n#\n");
    fprintf(outfile, "PAPER WIDTH        %.2f\n", cpq->dev_width);
    fprintf(outfile, "PAPER HEIGHT       %.2f\n", cpq->dev_height);
    fprintf(outfile, "#\n# Clip region of paper in cm\n");
    fprintf(outfile, "# For Landscape width > height\n");
    fprintf(outfile, "# For Portrait  width < height\n#\n");
    fprintf(outfile, "PAPER CLIP XOFFSET  %.2f\n", cpq->offx);
    fprintf(outfile, "PAPER CLIP WIDTH    %.2f\n", cpq->width);
    fprintf(outfile, "PAPER CLIP YOFFSET  %.2f\n", cpq->offy);
    fprintf(outfile, "PAPER CLIP HEIGHT   %.2f\n", cpq->height);
    if (cpq->output_type == 0) {
        fprintf(outfile, "#\n# Enclose HPGL in PCL5 code\n#\n");
        fprintf(outfile, "PAPER PCL5CODE   %s\n", plot_truefalse[cpq->options[0]]);
        fprintf(outfile, "#\n# Include (PostScript) paper definition\n#\n");
        fprintf(outfile, "#PAPER DEFINITION %s\n", plot_truefalse[cpq->options[1]]);
    }
    else {
        fprintf(outfile, "#\n# Enclose HPGL in PCL5 code\n#\n");
        fprintf(outfile, "#PAPER PCL5CODE   %s\n", plot_truefalse[cpq->options[0]]);
        fprintf(outfile, "#\n# Include (PostScript) paper definition\n#\n");
        fprintf(outfile, "PAPER DEFINITION %s\n", plot_truefalse[cpq->options[1]]);
    }
    fprintf(outfile, "#\n# Pen width in 1/1200 inch or 1/472 cm\n");
    fprintf(outfile, "# 0=0.0 1=0.002, 2=0.004, 3=0.008, and 4=0.017 cm\n#\n");
    fprintf(outfile, "PEN WIDTH     %d\n", cpq->pen_width);
    fprintf(outfile, "#\n# Plot offset x axis from PAPER CLIP YOFFSET in cm\n#\n");
    fprintf(outfile, "PLOT YOFFSET  %.2f\n", cpq->cloffy);
    if (cpq->is_2d) {
        fprintf(outfile, "#\n# Plot offset y axis from PAPER CLIP XOFFSET in cm\n#\n");
        fprintf(outfile, "PLOT XOFFSET  %.2f\n", cpq->cloffx);
    }
    fprintf(outfile, "#\n# Part of the spectrum that will be plotted\n#");
    fprintf(outfile, " units: ppm, hertz, seconds, channels\n#\n");
    fprintf(outfile, "XUNITS        %s\n", plot_units[cpq->unitsx]);
    if (cpq->unitsx == UNIT_PPM) {
        fprintf(outfile, "REGION LEFT   %g\n", cpq->xminpv);
        fprintf(outfile, "REGION RIGHT  %g\n", cpq->xmaxpv);
    }
    else if (cpq->unitsx == UNIT_HERTZ) {
        fprintf(outfile, "REGION LEFT   %g\n", cpq->xminhv);
        fprintf(outfile, "REGION RIGHT  %g\n", cpq->xmaxhv);
    }
    else if (cpq->unitsx == UNIT_SEC) {
        fprintf(outfile, "REGION LEFT   %g\n", cpq->xminsv);
        fprintf(outfile, "REGION RIGHT  %g\n", cpq->xmaxsv);
    }
    else if (cpq->unitsx == UNIT_CHAN) {
        fprintf(outfile, "REGION LEFT   %g\n", cpq->xminv);
        fprintf(outfile, "REGION RIGHT  %g\n", cpq->xmaxv);
    }
    if (cpq->is_2d) {
        fprintf(outfile, "YUNITS        %s\n", plot_units[cpq->unitsy]);
        if (cpq->unitsy == UNIT_PPM) {
            fprintf(outfile, "REGION BOTTOM %g\n", cpq->yminpv);
            fprintf(outfile, "REGION TOP    %g\n", cpq->ymaxpv);
        }
        else if (cpq->unitsy == UNIT_HERTZ) {
            fprintf(outfile, "REGION BOTTOM %g\n", cpq->yminhv);
            fprintf(outfile, "REGION TOP    %g\n", cpq->ymaxhv);
        }
        else if (cpq->unitsy == UNIT_SEC) {
            fprintf(outfile, "REGION BOTTOM %g\n", cpq->yminsv);
            fprintf(outfile, "REGION TOP    %g\n", cpq->ymaxsv);
        }
        else if (cpq->unitsy == UNIT_CHAN) {
            fprintf(outfile, "REGION BOTTOM %g\n", cpq->yminv);
            fprintf(outfile, "REGION TOP    %g\n", cpq->ymaxv);
        }
    }
    else {
        fprintf(outfile, "REGION BOTTOM %g\n", cpq->yminv);
        fprintf(outfile, "REGION TOP    %g\n", cpq->ymaxv);
    }
    fprintf(outfile, "#\n# Plot scale settings: auto, fixed, fill\n#\n");
    fprintf(outfile, "SCALE PLOT   %s\n", plot_fix[cpq->fix]);
    fprintf(outfile, "#\n# Only used if plot scale settings is fixed\n#\n");
    fprintf(outfile, "XSCALE HZ/CM %.2f\n", cpq->hzx);
    if (cpq->is_2d) 
        fprintf(outfile, "YSCALE HZ/CM %.2f\n", cpq->hzy);
    fprintf(outfile, "#\n# Show title: on or off\n#\n");
    fprintf(outfile, "TITLE SHOW   %s\n", plot_truefalse[cpq->show_title]);
    fprintf(outfile, "#\n# Text of title\n#\n");
    fprintf(outfile, "TITLE TEXT   \"%s\"\n", cpq->plot_title);
    fprintf(outfile, "#\n# Text below X axis\n#\n");
    fprintf(outfile, "XAXIS TEXT   \"%s\"\n", cpq->axtxt);
    fprintf(outfile, "#\n# Show X axis: on or off\n#\n");
    fprintf(outfile, "XAXIS SHOW   %s\n", plot_truefalse[cpq->show_xaxis]);
    fprintf(outfile, "#\n# Automatically place tick marks and labels: on or off\n#\n");
    fprintf(outfile, "XAXIS AUTO   %s\n", plot_truefalse[cpq->xticks_autoscale]);
    fprintf(outfile, "#\n# Tickmarks(labels)/unit (ppm,hz..)\n#\n");
    fprintf(outfile, "XAXIS LABELS       %.2f\n", cpq->xlabels_ppm);
    fprintf(outfile, "XAXIS TICKS SHORT  %.2f\n", cpq->xticks_ppm_short);
    fprintf(outfile, "XAXIS TICKS MEDIUM %.2f\n", cpq->xticks_ppm_medium);
    fprintf(outfile, "XAXIS TICKS LONG   %.2f\n", cpq->xticks_ppm_long);
    fprintf(outfile, "#\n# Tickmark length in cm\n#\n");
    fprintf(outfile, "XAXIS TICKLENGTH SHORT  %.2f\n", cpq->xticklen_short);
    fprintf(outfile, "XAXIS TICKLENGTH MEDIUM %.2f\n", cpq->xticklen_medium);
    fprintf(outfile, "XAXIS TICKLENGTH LONG   %.2f\n", cpq->xticklen_long);
    fprintf(outfile, "#\n# Offsets in cm\n#\n");
    fprintf(outfile, "XAXIS OFFSET AXIS       %.2f\n", cpq->xoff_axis);
    fprintf(outfile, "XAXIS OFFSET LABELS     %.2f\n", cpq->xoff_labels);
    fprintf(outfile, "XAXIS OFFSET TEXT       %.2f\n", cpq->xoff_title);
    fprintf(outfile, "XAXIS START  TEXT       %.2f\n", cpq->xstart_title);
    if (cpq->is_2d) {
        fprintf(outfile, "#\n# Text below Y axis\n#\n");
        fprintf(outfile, "YAXIS TEXT   \"%s\"\n", cpq->aytxt);
        fprintf(outfile, "#\n# Show Y axis: on or off\n#\n");
        fprintf(outfile, "YAXIS SHOW   %s\n", plot_truefalse[cpq->show_yaxis]);
        fprintf(outfile, "#\n# Automatically place tick marks and labels: on or off\n#\n");
        fprintf(outfile, "YAXIS AUTO   %s\n", plot_truefalse[cpq->yticks_autoscale]);
        fprintf(outfile, "#\n# Tickmarks(labels)/unit (ppm,hz..)\n#\n");
        fprintf(outfile, "YAXIS LABELS %.2f\n", cpq->ylabels_ppm);
        fprintf(outfile, "YAXIS TICKS SHORT  %.2f\n", cpq->yticks_ppm_short);
        fprintf(outfile, "YAXIS TICKS MEDIUM %.2f\n", cpq->yticks_ppm_medium);
        fprintf(outfile, "YAXIS TICKS LONG   %.2f\n", cpq->yticks_ppm_long);
        fprintf(outfile, "#\n# Tickmark length in cm\n#\n");
        fprintf(outfile, "YAXIS TICKLENGTH SHORT  %.2f\n", cpq->yticklen_short);
        fprintf(outfile, "YAXIS TICKLENGTH MEDIUM %.2f\n", cpq->yticklen_medium);
        fprintf(outfile, "YAXIS TICKLENGTH LONG   %.2f\n", cpq->yticklen_long);
        fprintf(outfile, "#\n# Offsets in cm\n#\n");
        fprintf(outfile, "YAXIS OFFSET AXIS       %.2f\n", cpq->yoff_axis);
        fprintf(outfile, "YAXIS OFFSET LABELS     %.2f\n", cpq->yoff_labels);
        fprintf(outfile, "YAXIS OFFSET TEXT       %.2f\n", cpq->yoff_title);
        fprintf(outfile, "YAXIS START  TEXT       %.2f\n", cpq->ystart_title);
        fprintf(outfile, "#\n# Box for 2D plots: off, on\n#\n");
        fprintf(outfile, "BOX  SHOW   %s\n", plot_truefalse[cpq->ibox]);
        fprintf(outfile, "#\n# Grid for 2D plots: off, on, auto\n#\n");
        fprintf(outfile, "GRID SHOW   %s\n", plot_truefalseauto[cpq->igrid]);
        fprintf(outfile, "#\n# Lines/unit (ppm, hz, ..)\n#\n");
        fprintf(outfile, "GRID XLINES %.2f\n", cpq->xgridlines_ppm);
        fprintf(outfile, "GRID YLINES %.2f\n", cpq->ygridlines_ppm);

        fprintf(outfile, "#\n# ==== CONTOUR PARAMETERS ====\n");
        fprintf(outfile, "# Lowest contour level\n#\n");
        fprintf(outfile, "CONTOUR LOWEST    %.2f\n", cpar->clevel[0]);
        fprintf(outfile, "#\n# Number of levels (max = %d)\n#\n", cpar->maxlev);
        fprintf(outfile, "CONTOUR NUMLEVELS %d\n", cpar->nlevel);
        fprintf(outfile, "#\n# Level sign: negative, both, positive\n#\n");
        fprintf(outfile, "CONTOUR SIGN      %s\n", plot_peakpicksign[2-cpar->plusmin]);
        fprintf(outfile, "#\n# Level mode: explicit, factor, equidistant, logrange\n#\n");
        fprintf(outfile, "CONTOUR MODE      %s\n", plot_range[cpar->mlevel]);
        fprintf(outfile, "#\n# Factor for \'Use factor\' mode\n#\n");
        fprintf(outfile, "CONTOUR FACTOR    %1.2f\n", cpar->flevel);
        fprintf(outfile, "#\n# Distance for \'Equidistant\' mode\n#\n");
        fprintf(outfile, "CONTOUR DISTANCE  %g\n", cpar->dlevel);
        fprintf(outfile, "#\n# Highest level for \'Logrange\' mode\n#\n");
        fprintf(outfile, "CONTOUR HIGHEST   %g\n", cpar->levelmax);
        if (cpar->mlevel == 0) {
            fprintf(outfile, "#\n# Levels for \'Explicit\' mode\n#\n");
            for (i=0;i<cpar->nlevel;i++) 
                fprintf(outfile, "CONTOUR LEVEL     %-5d %g\n", i+1, cpar->clevel[i]);
        }
    }
    else {
        fprintf(outfile, "#\n# Peak picking for 1D plots\n#\n");
        fprintf(outfile, "PEAKPICK %s\n", plot_truefalse[cpq->peakpick]);
        fprintf(outfile, "#\n# threshold: for negative peaks, this value is negated\n#\n");
        fprintf(outfile, "PEAKPICK THRESHOLD %g\n", cpq->thresh);
        fprintf(outfile, "#\n# sign: negative, both, positive\n#\n");
        fprintf(outfile, "PEAKPICK SIGN %s\n", plot_peakpicksign[cpq->sign+1]);
    }
}

#define MAXPARSE	10
static int parse_string(char *buf, char *array[MAXPARSE])
{
    int i,count;
    char *p;
    char sep[] = " \r\n\t";

    /*
     * Remove comments
     */
    if ((p=strchr(buf, '#')) != NULL)
        *p = '\0';
    /*
     * Upcase
     */
    strupr(buf);
    p= strtok(buf, sep);
    for (i=0, count=0;i<MAXPARSE;i++) {
        array[i] = p;
        if (!p) 
            break;
        p= strtok(NULL, sep);
        count++;
    }
    return count;
}


static int analarg(int count, char *array[MAXPARSE], int argpos, char *arg)
{
    if (argpos >= count)
         return FALSE;
    if (strcmp(array[argpos], arg) != 0) 
         return FALSE;
    return TRUE;
}

/*
 * read the next plot parameter from file or from buffer
 */
static char *get_next_parameter(char *buf, int maxsize, FILE *infile, 
                                char *inbuf, int init)
{
    static char *p;
    
    /*
     * If infile not is NULL, read from file
     */
    if (infile)
        return fgets(buf,maxsize,infile);
    /*
     * else read from inbuf
     */
    if (inbuf) {
        if (init) 
            p = inbuf;
        if (p == NULL || *p == '\0')
            return NULL;
        strncpy(buf, p, maxsize);
        buf[maxsize-1] = '\0';
        p = strchr(buf, '\n');
        if (p) {
            *p = '\0';
            p++;
        }
        return buf;
    }
    return NULL;
}

/*
 * Read Plot parameters from disk
 */
static void import_plotsettings(CONPLOTQ_TYPE *cpq, CBLOCK *cpar, 
                                FILE *infile, char *inbuf)
{
    char buf[PSND_STRLEN], buf2[PSND_STRLEN], *array[MAXPARSE];
    int i, parse_units[4], init;
    float unit[4], dev_width, dev_height;

    dev_width  = cpq->dev_width;
    dev_height = cpq->dev_height;
    for (i=0;i<4;i++)
        parse_units[i] = FALSE;
    init = TRUE;
    while (get_next_parameter(buf, PSND_STRLEN, infile, inbuf, init) != NULL) {
        int count;
        init = FALSE;
        /*
         * Save the original text
         */
        strcpy(buf2,buf);
        count = parse_string(buf, array);
        if (analarg(count, array, 0, "LANGUAGE")) {
            if (analarg(count, array, 1, "HPGL"))
                cpq->output_type = 0;
            else if (analarg(count, array, 1, "POSTSCRIPT")) {
                cpq->output_type = 1;
                if (analarg(count, array, 2, "COLOR"))
                    cpq->output_type = 2;
            }
        }
        else if (analarg(count, array, 0, "PAPER")) {
            if (analarg(count, array, 1, "TYPE")) {
                if (analarg(count, array, 2, "A4"))
                    cpq->paper_type = 0;
                else if (analarg(count, array, 2, "A3"))
                    cpq->paper_type = 1;
                else if (analarg(count, array, 2, "LETTER"))
                    cpq->paper_type = 2;
                else if (analarg(count, array, 2, "LEGAL"))
                    cpq->paper_type = 3;
                else if (analarg(count, array, 2, "USER"))
                    cpq->paper_type = 4;
            }
            else if (analarg(count, array, 1, "ORIENTATION")) {
                if (analarg(count, array, 2, "LANDSCAPE"))
                    cpq->paper_orientation = 0;
                else if (analarg(count, array, 2, "PORTRAIT"))
                    cpq->paper_orientation = 1;
            }
            else if (analarg(count, array, 1, "WIDTH")) {
                if (count >= 3) 
                    dev_width = psnd_scan_float(array[2]);
            }
            else if (analarg(count, array, 1, "HEIGHT")) {
                if (count >= 3) 
                    dev_height = psnd_scan_float(array[2]);
            }
            else if (analarg(count, array, 1, "CLIP")) {
                if (analarg(count, array, 2, "XOFFSET")) {
                    if (count >= 4) 
                        cpq->offx = psnd_scan_float(array[3]);
                }
                else if (analarg(count, array, 2, "WIDTH")) {
                    if (count >= 4) 
                        cpq->width = psnd_scan_float(array[3]);
                }
                else if (analarg(count, array, 2, "YOFFSET")) {
                    if (count >= 4) 
                        cpq->offy = psnd_scan_float(array[3]);
                }
                else if (analarg(count, array, 2, "HEIGHT")) {
                    if (count >= 4) 
                        cpq->height = psnd_scan_float(array[3]);
                }
            }
            else if (analarg(count, array, 1, "PCL5CODE")) {
                cpq->options[0] = FALSE;
                if (analarg(count, array, 2, "ON")) {
                    cpq->options[0] = TRUE;
                }
            }
            else if (analarg(count, array, 1, "DEFINITION")) {
                cpq->options[1] = FALSE;
                if (analarg(count, array, 2, "ON")) {
                    cpq->options[1] = TRUE;
                }
            }
        }
        else if (analarg(count, array, 0, "PEN")) {
            if (analarg(count, array, 1, "WIDTH")) {
                if (count >= 3) {
                    cpq->pen_width = psnd_scan_integer(array[2]);
                    if (cpq->pen_width < 0)
                        cpq->pen_width = 0;
                    else if (cpq->pen_width > MAX_PEN_WIDTH)
                        cpq->pen_width = MAX_PEN_WIDTH;
                }
            }
        }
        else if (analarg(count, array, 0, "PLOT")) {
            if (analarg(count, array, 1, "XOFFSET")) {
                if (count >= 3) 
                    cpq->cloffx = psnd_scan_float(array[2]);
            }
            else if (analarg(count, array, 1, "YOFFSET")) {
                if (count >= 3) 
                    cpq->cloffy = psnd_scan_float(array[2]);
            }
        }
        else if (analarg(count, array, 0, "XUNITS")) {
            if (analarg(count, array, 1, "PPM")) 
                cpq->unitsx = UNIT_PPM;
            else if (analarg(count, array, 1, "HERTZ")) 
                cpq->unitsx = UNIT_HERTZ;
            else if (analarg(count, array, 1, "SECONDS")) 
                cpq->unitsx = UNIT_SEC;
            else if (analarg(count, array, 1, "CHANNELS")) 
                cpq->unitsx = UNIT_CHAN;
        }
        else if (analarg(count, array, 0, "YUNITS")) {
            if (analarg(count, array, 1, "PPM")) 
                cpq->unitsy = UNIT_PPM;
            else if (analarg(count, array, 1, "HERTZ")) 
                cpq->unitsy = UNIT_HERTZ;
            else if (analarg(count, array, 1, "SECONDS")) 
                cpq->unitsy = UNIT_SEC;
            else if (analarg(count, array, 1, "CHANNELS")) 
                cpq->unitsy = UNIT_CHAN;
        }
        else if (analarg(count, array, 0, "REGION")) {
            if (count >= 3) {
                if (analarg(count, array, 1, "LEFT")) {
                    unit[0] = psnd_scan_float(array[2]);
                    parse_units[0] = TRUE;
                }
                else if (analarg(count, array, 1, "RIGHT")) {
                    unit[1] = psnd_scan_float(array[2]);
                    parse_units[1] = TRUE;
                }
                else if (analarg(count, array, 1, "BOTTOM")) {
                    unit[2] = psnd_scan_float(array[2]);
                    parse_units[2] = TRUE;
                }
                else if (analarg(count, array, 1, "TOP")) {
                    unit[3] = psnd_scan_float(array[2]);
                    parse_units[3] = TRUE;
                }
            }
        }
        else if (analarg(count, array, 0, "SCALE")) {
            if (analarg(count, array, 1, "PLOT")) {
                if (analarg(count, array, 2, "AUTO")) 
                    cpq->fix = 0;
                else if (analarg(count, array, 2, "FIXED")) 
                    cpq->fix = 1;
                else if (analarg(count, array, 2, "FILL")) 
                    cpq->fix = 2;
            }
        }   
        else if (analarg(count, array, 0, "XSCALE")) {
            if (analarg(count, array, 1, "HZ/CM")) {
                if (count >= 3) {
                    float hzx;
                    hzx = psnd_scan_float(array[2]);             
                    if (hzx != 0.0)
                        cpq->hzx = hzx;
                }
            }
        }
        else if (analarg(count, array, 0, "YSCALE")) {
            if (analarg(count, array, 1, "HZ/CM")) {
                if (count >= 3) {
                    float hzy;
                    hzy = psnd_scan_float(array[2]);             
                    if (hzy != 0.0)
                        cpq->hzy = hzy;
                }
            }
        }
        else if (analarg(count, array, 0, "TITLE")) {
            if (analarg(count, array, 1, "TEXT")) {
                if (count >= 2) {
                    int j=0;
                    char *p = buf2;
                    while (*p != '\0' && *p != '\"')
                        p++;
                    if (*p == '\"')
                        p++;
                    while (*p != '\0' && *p != '\"') {
                        cpq->plot_title[j++] = *p;
                        if (j == AXIS_LABEL_LENGTH)
                            break;
                        p++;
                    }
                    cpq->plot_title[j] = '\0';
                }
            }
            else if (analarg(count, array, 1, "SHOW")) {
                cpq->show_title = FALSE;
                if (analarg(count, array, 2, "ON")) {
                    cpq->show_title = TRUE;
                }
            }
        }
        else if (analarg(count, array, 0, "XAXIS")) {
            if (analarg(count, array, 1, "TEXT")) {
                if (count >= 2) {
                    int j=0;
                    char *p = buf2;
                    while (*p != '\0' && *p != '\"')
                        p++;
                    if (*p == '\"')
                        p++;
                    while (*p != '\0' && *p != '\"') {
                        cpq->axtxt[j++] = *p;
                        if (j == AXIS_LABEL_LENGTH)
                            break;
                        p++;
                    }
                    cpq->axtxt[j] = '\0';
                }
            }
            else if (analarg(count, array, 1, "SHOW")) {
                cpq->show_xaxis = FALSE;
                if (analarg(count, array, 2, "ON")) {
                    cpq->show_xaxis = TRUE;
                }
            }
            else if (analarg(count, array, 1, "AUTO")) {
                cpq->xticks_autoscale = FALSE;
                if (analarg(count, array, 2, "ON")) {
                    cpq->xticks_autoscale = TRUE;
                }
            }
            else if (analarg(count, array, 1, "LABELS")) {
                if (count >= 3) 
                    cpq->xlabels_ppm = psnd_scan_float(array[2]);            
            }
            else if (analarg(count, array, 1, "TICKS")) {
                if (count >= 4) {
                    if (analarg(count, array, 2, "SHORT")) 
                        cpq->xticks_ppm_short = psnd_scan_float(array[3]);
                    else if (analarg(count, array, 2, "MEDIUM")) 
                        cpq->xticks_ppm_medium = psnd_scan_float(array[3]);
                    else if (analarg(count, array, 2, "LONG")) 
                        cpq->xticks_ppm_long = psnd_scan_float(array[3]);
                }
            }
            else if (analarg(count, array, 1, "TICKLENGTH")) {
                if (count >= 4) {
                    if (analarg(count, array, 2, "SHORT")) 
                        cpq->xticklen_short = psnd_scan_float(array[3]);
                    else if (analarg(count, array, 2, "MEDIUM")) 
                        cpq->xticklen_medium = psnd_scan_float(array[3]);
                    else if (analarg(count, array, 2, "LONG")) 
                        cpq->xticklen_long = psnd_scan_float(array[3]);
                }
            }
            else if (analarg(count, array, 1, "OFFSET")) {
                if (count >= 4) {
                    if (analarg(count, array, 2, "AXIS")) 
                        cpq->xoff_axis = psnd_scan_float(array[3]);
                    else if (analarg(count, array, 2, "LABELS")) 
                        cpq->xoff_labels = psnd_scan_float(array[3]);
                    else if (analarg(count, array, 2, "TEXT")) 
                        cpq->xoff_title = psnd_scan_float(array[3]);
                }
            }
            else if (analarg(count, array, 1, "START")) {
                if (count >= 4) 
                    if (analarg(count, array, 2, "TEXT")) 
                        cpq->xstart_title = psnd_scan_float(array[3]);
            }
        }
        else if (analarg(count, array, 0, "YAXIS")) {
            if (analarg(count, array, 1, "TEXT")) {
                if (count >= 2) {
                    int j=0;
                    char *p = buf2;
                    while (*p != '\0' && *p != '\"')
                        p++;
                    if (*p == '\"')
                        p++;
                    while (*p != '\0' && *p != '\"') {
                        cpq->aytxt[j++] = *p;
                        if (j == AXIS_LABEL_LENGTH)
                            break;
                        p++;
                    }
                    cpq->aytxt[j] = '\0';
                }
            }
            else if (analarg(count, array, 1, "SHOW")) {
                cpq->show_yaxis = FALSE;
                if (analarg(count, array, 2, "ON")) {
                    cpq->show_yaxis = TRUE;
                }
            }
            else if (analarg(count, array, 1, "AUTO")) {
                cpq->yticks_autoscale = FALSE;
                if (analarg(count, array, 2, "ON")) {
                    cpq->yticks_autoscale = TRUE;
                }
            }
            else if (analarg(count, array, 1, "LABELS")) {
                if (count >= 3) 
                    cpq->ylabels_ppm = psnd_scan_float(array[2]);            
            }
            else if (analarg(count, array, 1, "TICKS")) {
                if (count >= 4) {
                    if (analarg(count, array, 2, "SHORT")) 
                        cpq->yticks_ppm_short = psnd_scan_float(array[3]);
                    else if (analarg(count, array, 2, "MEDIUM")) 
                        cpq->yticks_ppm_medium = psnd_scan_float(array[3]);
                    else if (analarg(count, array, 2, "LONG")) 
                        cpq->yticks_ppm_long = psnd_scan_float(array[3]);
                }
            }
            else if (analarg(count, array, 1, "TICKLENGTH")) {
                if (count >= 4) {
                    if (analarg(count, array, 2, "SHORT")) 
                        cpq->yticklen_short = psnd_scan_float(array[3]);
                    else if (analarg(count, array, 2, "MEDIUM")) 
                        cpq->yticklen_medium = psnd_scan_float(array[3]);
                    else if (analarg(count, array, 2, "LONG")) 
                        cpq->yticklen_long = psnd_scan_float(array[3]);
                }
            }
            else if (analarg(count, array, 1, "OFFSET")) {
                if (count >= 4) {
                    if (analarg(count, array, 2, "AXIS")) 
                        cpq->yoff_axis = psnd_scan_float(array[3]); 
                    else if (analarg(count, array, 2, "LABELS")) 
                        cpq->yoff_labels = psnd_scan_float(array[3]);
                    else if (analarg(count, array, 2, "TEXT")) 
                        cpq->yoff_title = psnd_scan_float(array[3]);
                }
            }
            else if (analarg(count, array, 1, "START")) {
                if (count >= 4) 
                    if (analarg(count, array, 2, "TEXT")) 
                        cpq->ystart_title = psnd_scan_float(array[3]);
            }
        }
        else if (analarg(count, array, 0, "BOX")) {
            if (analarg(count, array, 1, "SHOW")) {
                cpq->ibox = FALSE;
                if (analarg(count, array, 2, "ON")) {
                    cpq->ibox = TRUE;
                }
            }
        }
        else if (analarg(count, array, 0, "GRID")) {
            if (analarg(count, array, 1, "SHOW")) {
                cpq->igrid = 0;
                if (analarg(count, array, 2, "ON")) {
                    cpq->igrid = 1;
                }
                else if (analarg(count, array, 2, "AUTO")) {
                    cpq->igrid = 2;
                }
            }
            else if (analarg(count, array, 1, "XLINES")) {
                if (count >=3)
                    cpq->xgridlines_ppm = psnd_scan_float(array[2]);
            }
            else if (analarg(count, array, 1, "YLINES")) {
                if (count >=3)
                    cpq->ygridlines_ppm = psnd_scan_float(array[2]);
            }
        }
        else if (analarg(count, array, 0, "PEAKPICK")) {
            if (analarg(count, array, 1, "ON")) 
                cpq->peakpick = TRUE;
            else if (analarg(count, array, 1, "OFF")) 
                cpq->peakpick = FALSE;
            else if (analarg(count, array, 1, "THRESHOLD")) {
                if (count >=3)
                    cpq->thresh = psnd_scan_float(array[2]);
            }
            else if (analarg(count, array, 1, "SIGN")) {
                if (analarg(count, array, 2, "NEGATIVE")) {
                    cpq->sign = -1;
                }
                else if (analarg(count, array, 2, "BOTH")) {
                    cpq->sign = 0;
                }
                else if (analarg(count, array, 2, "POSITIVE")) {
                    cpq->sign = 1;
                }
            }
        }
        else if (analarg(count, array, 0, "CONTOUR")) {
            if (analarg(count, array, 1, "LOWEST")) {
                if (count >=3)
                    cpar->clevel[0] = psnd_scan_float(array[2]);
            }
            else if (analarg(count, array, 1, "NUMLEVELS")) {
                if (count >=3) {
                    int nlevel;
                    nlevel = psnd_scan_integer(array[2]);
                    if (nlevel < 1)
                        nlevel = 1;
                    if (nlevel > cpar->maxlev)
                        nlevel = cpar->maxlev;
                    cpar->nlevel = nlevel;
                }
            }
            else if (analarg(count, array, 1, "SIGN")) {
                if (analarg(count, array, 2, "NEGATIVE")) {
                    cpar->plusmin = LEVELS_NEGATIVE;
                }
                else if (analarg(count, array, 2, "BOTH")) {
                    cpar->plusmin = LEVELS_BOTH;
                }
                else if (analarg(count, array, 2, "POSITIVE")) {
                    cpar->plusmin = LEVELS_POSITIVE;
                }
            }
            else if (analarg(count, array, 1, "MODE")) {
                if (analarg(count, array, 2, "EXPLICIT")) {
                    cpar->mlevel = 0;
                }
                else if (analarg(count, array, 2, "FACTOR")) {
                    cpar->mlevel = 1;
                }
                else if (analarg(count, array, 2, "EQUIDISTANT")) {
                    cpar->mlevel = 2;
                }
                else if (analarg(count, array, 2, "LOGRANGE")) {
                    cpar->mlevel = 3;
                }
            }
            else if (analarg(count, array, 1, "FACTOR")) {
                if (count >=3)
                    cpar->flevel = psnd_scan_float(array[2]);
            }
            else if (analarg(count, array, 1, "DISTANCE")) {
                if (count >=3)
                    cpar->dlevel = psnd_scan_float(array[2]);
            }
            else if (analarg(count, array, 1, "HIGHEST")) {
                if (count >=3)
                    cpar->levelmax = psnd_scan_float(array[2]);
            }
            else if (analarg(count, array, 1, "LEVEL")) {
                if (count >=4) {
                    int level;
                    level = psnd_scan_integer(array[2]);
                    if (level > 0 && level <= cpar->maxlev)
                        cpar->clevel[level-1] = psnd_scan_float(array[3]);
                }
            }
        }
    }
    cpq->paper_id = cpq->paper_type * 2 + cpq->paper_orientation;
    cpq->dev_width  = max(dev_width, dev_height);
    cpq->dev_height = min(dev_width, dev_height);
    paper_sizedef[cpq->paper_type].width = cpq->dev_width;
    paper_sizedef[cpq->paper_type].height= cpq->dev_height;
    paper_def[cpq->paper_id].x0 	= cpq->offx;
    paper_def[cpq->paper_id].y0 	= cpq->offy;
    paper_def[cpq->paper_id].width 	= cpq->width;
    paper_def[cpq->paper_id].height 	= cpq->height;
    if (parse_units[0])
        calc_xminv(cpq, unit[0]);
    if (parse_units[1])
        calc_xmaxv(cpq, unit[1]);
    if (parse_units[2])
        calc_yminv(cpq, unit[2]);
    if (parse_units[3])
        calc_ymaxv(cpq, unit[3]);
}

void psnd_set_plot_region(CONPLOTQ_TYPE *cpq, CBLOCK *cpar,
                           float left, float right, 
                           float bottom, float top)
{
    psnd_init_paper_plot(cpq);
    if (left != right) {
        calc_xminv(cpq, left);
        calc_xmaxv(cpq, right);
    }
    if (bottom != top) {
        calc_yminv(cpq, bottom);
        calc_ymaxv(cpq, top);
    }
    process_settings(cpq, cpar);
}

static void units2xlabels(CONPLOTQ_TYPE *cpq, int is_2d, 
                          char *label1, char *label2)
{
    if (cpq->unitsx == UNIT_PPM) {
        if (is_2d) {
            sprintf(label1,  "%5.2f", 
                    chan2ppm(cpq->xminv, cpq->par1, cpq->dat,TRUE));
            sprintf(label2, "%5.2f", 
                    chan2ppm(cpq->xmaxv, cpq->par1, cpq->dat,TRUE));
        }
        else {
            sprintf(label1 , "%5.2f", psnd_chan2ppm(cpq->xminv, 
                            cpq->par1->sfd, 
                            cpq->dat->sw, 
                            cpq->dat->isize, cpq->par1->xref,
                            cpq->par1->aref, cpq->par1->bref,
                            cpq->dat->irc));
            sprintf(label2, "%5.2f", psnd_chan2ppm(cpq->xmaxv, 
                            cpq->par1->sfd,
                            cpq->dat->sw, 
                            cpq->dat->isize, cpq->par1->xref,
                            cpq->par1->aref, cpq->par1->bref,
                            cpq->dat->irc));
        }
    }
    else if (cpq->unitsx == UNIT_HERTZ) {
        if (is_2d) {
            sprintf(label1,  "%5.2f", 
                    chan2hz(cpq->xminv, cpq->par1, cpq->dat,TRUE));
            sprintf(label2, "%5.2f", 
                    chan2hz(cpq->xmaxv, cpq->par1, cpq->dat,TRUE));
        }
        else {
            sprintf(label1 , "%5.2f", psnd_chan2hz(cpq->xminv, 
                            cpq->dat->sw, 
                            cpq->dat->isize, cpq->par1->xref,
                            cpq->par1->aref, cpq->par1->bref,
                            cpq->dat->irc));
            sprintf(label2, "%5.2f", psnd_chan2hz(cpq->xmaxv, 
                            cpq->dat->sw, 
                            cpq->dat->isize, cpq->par1->xref,
                            cpq->par1->aref, cpq->par1->bref,
                            cpq->dat->irc));
        }
    }
    else if (cpq->unitsx == UNIT_SEC) {
        if (is_2d) {
            sprintf(label1,  "%5.2f", 
                             psnd_chan2sec(cpq->xminv, cpq->par1->swhold));
            sprintf(label2, "%5.2f", 
                             psnd_chan2sec(cpq->xmaxv, cpq->par1->swhold));
        }
        else {
            sprintf(label1 , "%5.2f", psnd_chan2sec(cpq->xminv, 
                                                    cpq->dat->sw));
            sprintf(label2, "%5.2f", psnd_chan2sec(cpq->xmaxv, 
                                                    cpq->dat->sw));
        }
    }
    else {
        sprintf(label1 , "%5.0f", cpq->xminv);
        sprintf(label2, "%5.0f", cpq->xmaxv);
    }
}


static void units2ylabels(CONPLOTQ_TYPE *cpq, int is_2d, char *label3, 
                          char *label4)
{
    if (cpq->unitsy == UNIT_PPM) {
        if (is_2d) {
            sprintf(label3, "%5.2f", 
                    chan2ppm(cpq->yminv, cpq->par2, cpq->dat,TRUE));
            sprintf(label4, "%5.2f", 
                    chan2ppm(cpq->ymaxv, cpq->par2, cpq->dat,TRUE));
        }
        else {
            sprintf(label3, "%5.1g", cpq->yminv);
            sprintf(label4, "%5.1g", cpq->ymaxv);
        }
    }
    else if (cpq->unitsy == UNIT_HERTZ) {
        if (is_2d) {
            sprintf(label3, "%5.2f", 
                    chan2hz(cpq->yminv, cpq->par2, cpq->dat,TRUE));
            sprintf(label4, "%5.2f", 
                    chan2hz(cpq->ymaxv, cpq->par2, cpq->dat,TRUE));
        }
        else {
            sprintf(label3, "%5.1g", cpq->yminv);
            sprintf(label4, "%5.1g", cpq->ymaxv);
        }
    }
    else if (cpq->unitsy == UNIT_SEC) {
        if (is_2d) {
            sprintf(label3, "%5.2f", 
                    psnd_chan2sec(cpq->yminv, cpq->par2->swhold));
            sprintf(label4, "%5.2f", 
                    psnd_chan2sec(cpq->ymaxv, cpq->par2->swhold));
        }
        else {
            sprintf(label3, "%5.1g", cpq->yminv);
            sprintf(label4, "%5.1g", cpq->ymaxv);
        }
    }
    else {
        if (is_2d) {
            sprintf(label3, "%5.0f", cpq->yminv);
            sprintf(label4, "%5.0f", cpq->ymaxv);
        }
        else {
            sprintf(label3, "%5.1g", cpq->yminv);
            sprintf(label4, "%5.1g", cpq->ymaxv);
        }
    }
}

/*
 * make 4 labels, left, right, top and bottom that
 * hold the current plotting region in the current units
 */
static void units2labels(CONPLOTQ_TYPE *cpq, int is_2d, 
                         char *label, char *label2,
                         char *label3, char *label4)
{
    if (is_2d) {
        cpq->xminv = inside(1, cpq->xminv, cpq->par1->nsiz);
        cpq->xmaxv = inside(1, cpq->xmaxv, cpq->par1->nsiz);
        cpq->yminv = inside(1, cpq->yminv, cpq->par2->nsiz);
        cpq->ymaxv = inside(1, cpq->ymaxv, cpq->par2->nsiz);
    }
    else {
        cpq->xminv = inside(1, cpq->xminv, cpq->dat->isize);
        cpq->xmaxv = inside(1, cpq->xmaxv, cpq->dat->isize);
    }
    units2xlabels(cpq, is_2d, label, label2);
    units2ylabels(cpq, is_2d, label3, label4);
}

/*
 * Write current plot parameters to file
 */
int psnd_write_plot_parameters(CONPLOTQ_TYPE *cpq, CBLOCK *cpar,
                                char *outputfile)
{
    FILE *outfile;
    outfile = fopen(outputfile, "w");
    if (outfile == NULL) 
        return FALSE;
    psnd_init_paper_plot(cpq);
    process_settings(cpq, cpar);  
    export_plotsettings(cpq, cpar, outfile);
    fclose(outfile);
    return TRUE;
}


/*
 * Read plot parameters from file
 */
int psnd_read_plot_parameters(CONPLOTQ_TYPE *cpq, CBLOCK *cpar,
                               char *inputfile)
{
    FILE *infile;
    infile = fopen(inputfile, "r");
    if (infile == NULL) 
        return FALSE;
    psnd_init_paper_plot(cpq);
    import_plotsettings(cpq, cpar, infile, NULL);
    fclose(infile);
    process_settings(cpq, cpar);
    return TRUE;
}

/*
 * Read plot parameters from string
 */
int psnd_set_plot_parameters(CONPLOTQ_TYPE *cpq, CBLOCK *cpar,
                              char *inputstring)
{
    if (inputstring == NULL) 
        return FALSE;
    psnd_init_paper_plot(cpq);
    import_plotsettings(cpq, cpar, NULL, inputstring);
    process_settings(cpq, cpar);
    return TRUE;
}

/*
 * After plot parameters have been read from disk
 * update the proper buttons in the main plot popup
 */
static void update_buttons(CONPLOTQ_TYPE *cpq)
{
    char label[255], *label2, *label3, *label4;

    if (!cpq->cont_id)
        return;

    g_popup_set_selection(cpq->cont_id, OUTPUT_TYPE, cpq->output_type);

    g_popup_set_checkmark(cpq->cont_id, OPTIONS, 0, cpq->options[0]);
    g_popup_set_checkmark(cpq->cont_id, OPTIONS, 1, cpq->options[1]);

    g_popup_set_selection(cpq->cont_id, PEN_WIDTH, cpq->pen_width);

    g_popup_set_selection(cpq->cont_id, PAPER_TYPE, cpq->paper_type);
    g_popup_set_selection(cpq->cont_id, PAPER_ORIENTATION, 
                          cpq->paper_orientation);

    sprintf(label, "%5.2f", cpq->dev_width);
    g_popup_set_label(cpq->cont_id, PAPER_SIZE_WIDTH, label);
    sprintf(label, "%5.2f", cpq->dev_height);
    g_popup_set_label(cpq->cont_id, PAPER_SIZE_HEIGHT, label);
    sprintf(label, "%5.2f", cpq->offx);
    g_popup_set_label(cpq->cont_id, OFFX, label);
    sprintf(label, "%5.2f", cpq->offy);
    g_popup_set_label(cpq->cont_id, OFFY, label);
    sprintf(label, "%5.2f", cpq->width);
    g_popup_set_label(cpq->cont_id, WIDTH, label);
    sprintf(label, "%5.2f", cpq->height);
    g_popup_set_label(cpq->cont_id, HEIGHT, label);
       
    label2 = label+40;
    label3 = label2+40;
    label4 = label3+40;

    /*
     * Spectral plotting limits
     */
    units2labels(cpq, cpq->is_2d, label, label2, label3, label4);
    g_popup_set_label(cpq->cont_id, XMINV, label);
    g_popup_set_label(cpq->cont_id, XMAXV, label2);
    g_popup_set_label(cpq->cont_id, YMINV, label3);
    g_popup_set_label(cpq->cont_id, YMAXV, label4);

    if (cpq->is_2d) {
        g_popup_set_selection(cpq->cont_id, SHOW_XAXIS,
                              cpq->show_xaxis + 2 * cpq->show_yaxis);
        g_popup_set_selection(cpq->cont_id, UNITSX, cpq->unitsx);
        g_popup_set_selection(cpq->cont_id, UNITSY, cpq->unitsy);
        /*
         * Spectrum scaling
         */
        g_popup_set_selection(cpq->cont_id, FIX, cpq->fix);
        sprintf(label, "%5.2f", cpq->hzx);
        g_popup_set_label(cpq->cont_id, HZX, label);
        sprintf(label, "%5.2f", cpq->hzy);
        g_popup_set_label(cpq->cont_id, HZY, label);

        /*
         * Grid
         */
        g_popup_set_selection(cpq->cont_id,IGRID, cpq->igrid);
        sprintf(label, "%.2f", cpq->xgridlines_ppm);
        g_popup_set_label(cpq->cont_id, XGRIDLINES_PPM, label);
        sprintf(label, "%.2f", cpq->ygridlines_ppm);
        g_popup_set_label(cpq->cont_id, YGRIDLINES_PPM, label);

    }
    else {
        g_popup_set_selection(cpq->cont_id, SHOW_XAXIS,
                              cpq->show_xaxis);
        g_popup_set_selection(cpq->cont_id, UNITSX, cpq->unitsx);
        
        /*
         * Spectrum scaling
         */
        g_popup_set_selection(cpq->cont_id, FIX, cpq->fix);
        sprintf(label, "%5.2f", cpq->hzx);
        g_popup_set_label(cpq->cont_id, HZX, label);

        /*
         * Peak pick
         */
        g_popup_set_selection(cpq->cont_id, PEAKPICK, cpq->peakpick);
        g_popup_set_selection(cpq->cont_id, SIGN, cpq->sign+1);

        sprintf(label, "%5g", cpq->thresh);
        g_popup_set_label(cpq->cont_id, THRESH, label);
    }

}


/*
 * process plot settings from the popup menu
 */
static void hardcopy_callback(G_POPUP_CHILDINFO *ci)
{
    char *label, label1[PSND_STRLEN], *filename, *label2, *label3, *label4;
    CONPLOTQ_TYPE *cpq;
    CBLOCK *cpar;
    int i;
    MBLOCK *mblk = (MBLOCK *) ci->userdata;

    cpq  = mblk->cpq_screen;
    cpar = mblk->cpar_screen;
    label2 = label1+40;
    label3 = label2+40;
    label4 = label3+40;
    switch (ci->type) {
        case G_CHILD_PUSHBUTTON:
            switch (ci->id) {
                case AXIS_SETUP:
                    hardcopy_axis_setup(mblk, ci->win_id,cpq);
                    break;

                case PLOT_TITLE:
                    hardcopy_plot_title(mblk, ci->win_id,cpq);
                    break;

                case POPUP_PAPER_SIZES:
                    hardcopy_paperdef(mblk, ci->win_id,cpq);
                    break;

                case ADDTO_TITLE:
                    strncat(cpq->plot_title,
                            cpq->dat->finfo[INFILE].name,
                            AXIS_LABEL_LENGTH);
                    g_popup_set_label(ci->cont_id, PLOT_TITLE, 
                                      cpq->plot_title);
                    break;

                case POPUP_XAXIS:
                    hardcopy_xaxis(mblk, ci->win_id,cpq);
                    break;

                case POPUP_YAXIS:
                    hardcopy_yaxis(mblk, ci->win_id,cpq);
                    break;

                case SAVESETTINGS:
                    if ((filename = psnd_savefilename(mblk,"Save Parameters", 
                                                     "*.plp")) != NULL) {
                        FILE *outfile;
                        outfile = fopen(filename, "w");
                        if (outfile) {
                            process_settings(cpq, cpar);  
                            export_plotsettings(cpq, cpar, outfile);
                            fclose(outfile);
                        }
                    }
                    break;

                case READSETTINGS:
                    if ((filename = psnd_getfilename(mblk,"Read Parameters", "*.plp")) != NULL) {
                        FILE *infile;
                        infile = fopen(filename, "r");
                        if (infile) {
                            import_plotsettings(cpq, cpar, infile, NULL);
                            fclose(infile);
                            process_settings(cpq, cpar);
                            update_buttons(cpq);
                        }
                    }
                    break;
            }
            break;

        case G_CHILD_CHECKBOX:
            switch (ci->id) {
                case OPTIONS:
                    cpq->options[0] = ci->select[0];
                    cpq->options[1] = ci->select[1];
                    cpq->options[2] = ci->select[2];
                    break;
            }
            break;

        case G_CHILD_OPTIONMENU:
            switch (ci->id) {
                case SHOW_TITLE:
                    cpq->show_title = ci->item;
                    break;

                case UNITSX:
                    cpq->unitsx = ci->item;
                    units2xlabels(cpq,cpq->is_2d, label1, label2);
                    g_popup_set_label(ci->cont_id, XMINV, label1);
                    g_popup_set_label(ci->cont_id, XMAXV, label2);
                    break;

                case UNITSY:
                    cpq->unitsy = ci->item;
                    units2ylabels(cpq,cpq->is_2d, label3, label4);
                    g_popup_set_label(ci->cont_id, YMINV, label3);
                    g_popup_set_label(ci->cont_id, YMAXV, label4);
                    break;

                case OUTPUT_TYPE:
                    cpq->output_type = ci->item;
                    break;

                case PEN_WIDTH:
                    cpq->pen_width = ci->item;
                    break;

                case PAPER_TYPE:
                    cpq->paper_type = ci->item;
                    cpq->paper_id = cpq->paper_type * 2 + cpq->paper_orientation;
                    cpq->offx	= paper_def[cpq->paper_id].x0;
                    cpq->offy	= paper_def[cpq->paper_id].y0;
                    cpq->width	= paper_def[cpq->paper_id].width;
                    cpq->height	= paper_def[cpq->paper_id].height;
                    label = psnd_sprintf_temp("%5.2f", cpq->offx);
                    g_popup_set_label(ci->cont_id, OFFX, label);
                    label = psnd_sprintf_temp("%5.2f",cpq->offy);
                    g_popup_set_label(ci->cont_id, OFFY, label);
                    label = psnd_sprintf_temp("%5.2f", cpq->width);
                    g_popup_set_label(ci->cont_id, WIDTH, label);
                    label = psnd_sprintf_temp("%5.2f", cpq->height);
                    g_popup_set_label(ci->cont_id, HEIGHT, label);

                    cpq->dev_width = paper_sizedef[cpq->paper_type].width;
                    label = psnd_sprintf_temp("%5.2f", cpq->dev_width);
                    g_popup_set_label(ci->cont_id, PAPER_SIZE_WIDTH, label);
                    cpq->dev_height= paper_sizedef[cpq->paper_type].height;
                    label = psnd_sprintf_temp("%5.2f", cpq->dev_height);
                    g_popup_set_label(ci->cont_id, PAPER_SIZE_HEIGHT, label);
                    break;

                case PAPER_ORIENTATION:
                    cpq->paper_orientation = ci->item;
                    cpq->paper_id = cpq->paper_type * 2 + cpq->paper_orientation;
                    cpq->offx	= paper_def[cpq->paper_id].x0;
                    cpq->offy	= paper_def[cpq->paper_id].y0;
                    cpq->width	= paper_def[cpq->paper_id].width;
                    cpq->height	= paper_def[cpq->paper_id].height;
                    label = psnd_sprintf_temp("%5.2f", cpq->offx);
                    g_popup_set_label(ci->cont_id, OFFX, label);
                    label = psnd_sprintf_temp("%5.2f",cpq->offy);
                    g_popup_set_label(ci->cont_id, OFFY, label);
                    label = psnd_sprintf_temp("%5.2f", cpq->width);
                    g_popup_set_label(ci->cont_id, WIDTH, label);
                    label = psnd_sprintf_temp("%5.2f", cpq->height);
                    g_popup_set_label(ci->cont_id, HEIGHT, label);
                    break;

                case SHOW_XAXIS:
                    cpq->show_xaxis = ci->item & 0x01;
                    if (cpq->is_2d)
                        cpq->show_yaxis = ((ci->item & 0x02) != 0);
                    break;

                case FIX:
                    cpq->fix = ci->item;
                    break;

                case IGRID:
                    cpq->igrid = ci->item;
                    break;

                case PEAKPICK:
                    cpq->peakpick = ci->item;
                    break;

                case SIGN:
                    cpq->sign = ci->item - 1;
                    break;

                case PREVIEW:
                    cpq->preview_mode = ci->item;
                    break;

                case XTICKS_AUTOSCALE:
                    cpq->xticks_autoscale = ci->item;
                    break;

                case YTICKS_AUTOSCALE:
                    cpq->yticks_autoscale = ci->item;
                    break;

            }
            break;
        case G_CHILD_TEXTBOX:
            switch (ci->id) {
                case PLOT_TITLE: 
                    strncpy(cpq->plot_title, ci->label, AXIS_LABEL_LENGTH);
                    break;   

                case HZX: {
                    float hzx=0.0;
                    hzx = psnd_scan_float(ci->label);
                    if (hzx != 0.0)
                        cpq->hzx = hzx;
                    }
                    break;

                case HZY: {
                    float hzy=0.0;
                    if (cpq->is_2d) 
                        hzy = psnd_scan_float(ci->label);
                    if (hzy != 0.0)
                        cpq->hzy = hzy;
                    }
                    break;

                case XGRIDLINES_PPM: 
                    cpq->xgridlines_ppm = psnd_scan_float(ci->label);
                    break;

                case YGRIDLINES_PPM: 
                    cpq->ygridlines_ppm = psnd_scan_float(ci->label);
                    break;

                case OFFX: 
                    cpq->offx = psnd_scan_float(ci->label);
                    paper_def[cpq->paper_id].x0 	= cpq->offx;
                    break;
   
                case OFFY: 
                    cpq->offy = psnd_scan_float(ci->label);
                    paper_def[cpq->paper_id].y0 	= cpq->offy;
                    break;
   
                case WIDTH: 
                    cpq->width = psnd_scan_float(ci->label);
                    paper_def[cpq->paper_id].width 	= cpq->width;
                    break;
   
                case HEIGHT: 
                    cpq->height = psnd_scan_float(ci->label);
                    paper_def[cpq->paper_id].height 	= cpq->height;
                    break;
   
                case PAPER_SIZE_WIDTH: 
                    cpq->dev_width = psnd_scan_float(ci->label);
                    paper_sizedef[cpq->paper_type].width = cpq->dev_width;
                    break;
     
                case PAPER_SIZE_HEIGHT: 
                    cpq->dev_height = psnd_scan_float(ci->label);
                    paper_sizedef[cpq->paper_type].height= cpq->dev_height;
                    break;
     
                case XMINV: {
                    float left;
                    left = psnd_scan_float(ci->label);
                    calc_xminv(cpq, left);
                    }
                    break;
     
                case XMAXV: {
                    float right;
                    right = psnd_scan_float(ci->label);
                    calc_xmaxv(cpq, right);
                    }
                    break;
     
                case YMINV: {
                    float bottom;
                    bottom = psnd_scan_float(ci->label);
                    calc_yminv(cpq, bottom);
                    }
                    break;
     
                case YMAXV: {
                    float top;
                    top = psnd_scan_float(ci->label);
                    calc_ymaxv(cpq, top);
                    }
                    break;
     
                case XTICKS_PPM_SHORT: 
                    cpq->xticks_ppm_short = psnd_scan_float(ci->label);
                    break;
     
                case XTICKS_PPM_MEDIUM: 
                    cpq->xticks_ppm_medium = psnd_scan_float(ci->label);
                    break;
     
                case XTICKS_PPM_LONG: 
                    cpq->xticks_ppm_long = psnd_scan_float(ci->label);
                    break;
     
                case XTICKLEN_SHORT: 
                    cpq->xticklen_short = psnd_scan_float(ci->label);
                    break;

                case XTICKLEN_MEDIUM: 
                    cpq->xticklen_medium = psnd_scan_float(ci->label);
                    break;

                case XTICKLEN_LONG: 
                    cpq->xticklen_long = psnd_scan_float(ci->label);
                    break;

                case XLABELS_PPM: 
                    cpq->xlabels_ppm = psnd_scan_float(ci->label);
                    break;

                case XOFF_AXIS: 
                    cpq->xoff_axis = psnd_scan_float(ci->label);
                    break;

                case XOFF_LABELS: 
                    cpq->xoff_labels = psnd_scan_float(ci->label);
                    break;

                case XSTART_TITLE: 
                    cpq->xstart_title = psnd_scan_float(ci->label);
                    break;

                case XOFF_TITLE: 
                    cpq->xoff_title = psnd_scan_float(ci->label);
                    break;

                case AXTXT: 
                    strncpy(cpq->axtxt, ci->label, AXIS_LABEL_LENGTH);
                    break;   
     
                case YTICKS_PPM_SHORT: 
                    cpq->yticks_ppm_short = psnd_scan_float(ci->label);
                    break;
     
                case YTICKS_PPM_MEDIUM: 
                    cpq->yticks_ppm_medium = psnd_scan_float(ci->label);
                    break;
     
                case YTICKS_PPM_LONG: 
                    cpq->xticks_ppm_long = psnd_scan_float(ci->label);
                    break;
     
                case YTICKLEN_SHORT: 
                    cpq->yticklen_short = psnd_scan_float(ci->label);
                    break;

                case YTICKLEN_MEDIUM: 
                    cpq->yticklen_medium = psnd_scan_float(ci->label);
                    break;

                case YTICKLEN_LONG: 
                    cpq->yticklen_long = psnd_scan_float(ci->label);
                    break;

                case YLABELS_PPM: 
                    cpq->ylabels_ppm = psnd_scan_float(ci->label);
                    break;

                case YOFF_AXIS: 
                    cpq->yoff_axis = psnd_scan_float(ci->label);
                    break;

                case YOFF_LABELS: 
                    cpq->yoff_labels = psnd_scan_float(ci->label);
                    break;

                case YSTART_TITLE: 
                    cpq->ystart_title = psnd_scan_float(ci->label);
                    break;

                case YOFF_TITLE: 
                    cpq->yoff_title = psnd_scan_float(ci->label);
                    break;

                case AYTXT: 
                    strncpy(cpq->aytxt, ci->label, AXIS_LABEL_LENGTH);
                    break;

                case THRESH: 
                    cpq->thresh = psnd_scan_float(ci->label);
                    break;

                case SENS: 
                    cpq->sens = psnd_scan_float(ci->label);
                    break;

            }
            break;
    }
}


static void popuphardcopy_add_option(MBLOCK *mblk, int cont_id, 
                           G_POPUP_CHILDINFO *ci, int id, 
                           char *label, int select, int item_count,
                           char **item_labels)
{
    g_popup_init_info(ci);
    ci->type  		= G_CHILD_OPTIONMENU;
    ci->id    		= id;
    ci->item_count 	= item_count;
    ci->item 		= select;
    ci->data 		= item_labels;
    ci->label 		= label;
    ci->horizontal 	= TRUE;
    ci->frame 		= TRUE;
    ci->func  		= hardcopy_callback;
    ci->userdata	= (void*) mblk;
    g_popup_add_child(cont_id, ci);
}

static void popuphardcopy_add_option2(MBLOCK *mblk, int cont_id, 
                           G_POPUP_CHILDINFO *ci, int id, 
                           char *label, int select, int item_count,
                           char **item_labels)
{
    g_popup_init_info(ci);
    ci->type  		= G_CHILD_OPTIONMENU;
    ci->id    		= id;
    ci->item_count 	= item_count;
    ci->item 		= select;
    ci->data 		= item_labels;
    ci->horizontal 	= TRUE;
    ci->frame 		= TRUE;
    ci->title 		= label;
    ci->func  		= hardcopy_callback;
    ci->userdata	= (void*) mblk;
    g_popup_add_child(cont_id, ci);
}


static void popuphardcopy_add_longtext(MBLOCK *mblk, int cont_id, 
                           G_POPUP_CHILDINFO *ci, int id, 
                           char *label, char *title, int items_visible)
{
    g_popup_init_info(ci);
    ci->type  		= G_CHILD_TEXTBOX;
    ci->id    		= id;
    ci->item_count 	= AXIS_LABEL_LENGTH;
    ci->items_visible 	= items_visible;
    ci->label 		= label;
    ci->frame 		= TRUE;
    ci->title 		= title;
    ci->func  		= hardcopy_callback;
    ci->userdata	= (void*) mblk;
    g_popup_add_child(cont_id, ci);
}     

static void popuphardcopy_add_text(MBLOCK *mblk, int cont_id, 
                           G_POPUP_CHILDINFO *ci, int id, 
                           char *label, char *title, int items_visible)
{
    g_popup_init_info(ci);
    ci->type  		= G_CHILD_TEXTBOX;
    ci->id    		= id;
    ci->item_count 	= 40;
    ci->items_visible 	= items_visible;
    ci->label 		= label;
    ci->frame 		= TRUE;
    ci->title 		= title;
    ci->func  		= hardcopy_callback;
    ci->userdata	= (void*) mblk;
    g_popup_add_child(cont_id, ci);
}     

static void popuphardcopy_add_button(MBLOCK *mblk, int cont_id, 
                           G_POPUP_CHILDINFO *ci, int id, char *label)
{
    g_popup_init_info(ci);
    ci->type  		= G_CHILD_PUSHBUTTON;
    ci->id    		= id;
    ci->label 		= label;
    ci->frame 		= FALSE;
    ci->func  		= hardcopy_callback;
    ci->userdata	= (void*) mblk;
    g_popup_add_child(cont_id, ci);
}     


static void popuphardcopy_add_panel(int cont_id, G_POPUP_CHILDINFO *ci, 
                           char *title, int horizontal, int open, int frame)
{
    g_popup_init_info(ci);
    ci->type       = G_CHILD_PANEL;
    ci->item       = open;
    ci->frame      = frame;
    if (title)
        ci->title  = title;
    ci->horizontal = horizontal;
    g_popup_add_child(cont_id, ci);
}



#define MAXCHILD	100

static int hardcopy_add_xaxis_stuff(MBLOCK *mblk, int cont_id,
           CONPLOTQ_TYPE *cpq, int id, G_POPUP_CHILDINFO ci[])
{
    static char *label, *radio_autoscale[] = { "OFF", "ON" };
    
          
    id++;
    popuphardcopy_add_option(mblk, cont_id, &(ci[id]), XTICKS_AUTOSCALE, 
                           "Autoscale tick mark", cpq->xticks_autoscale, 2,
                            radio_autoscale);
       
    id++;
    popuphardcopy_add_panel(cont_id, &(ci[id]), "Tick marks/unit",
                           TRUE, TRUE, TRUE);
    id++;
    label = psnd_sprintf_temp("%.2f", cpq->xticks_ppm_short);
    popuphardcopy_add_text(mblk, cont_id, &(ci[id]), XTICKS_PPM_SHORT, 
                           label, "Short", 6);

    id++;     
    label = psnd_sprintf_temp("%.2f", cpq->xticks_ppm_medium);
    popuphardcopy_add_text(mblk, cont_id, &(ci[id]), XTICKS_PPM_MEDIUM, 
                           label, "Medium", 6);
     
    id++;     
    label = psnd_sprintf_temp("%.2f", cpq->xticks_ppm_long);
    popuphardcopy_add_text(mblk, cont_id, &(ci[id]), XTICKS_PPM_LONG, 
                           label, "Long", 6);
     
    id++;
    popuphardcopy_add_panel(cont_id, &(ci[id]), NULL,
                            FALSE,  FALSE, FALSE);

    id++;
    popuphardcopy_add_panel(cont_id, &(ci[id]), "Tick length (cm)",
                           TRUE, TRUE, TRUE);
    id++;     
    label = psnd_sprintf_temp("%.2f", cpq->xticklen_short);
    popuphardcopy_add_text(mblk, cont_id, &(ci[id]), XTICKLEN_SHORT,
                           label, "Short", 6);
     
    id++;     
    label = psnd_sprintf_temp("%.2f", cpq->xticklen_medium);
    popuphardcopy_add_text(mblk, cont_id, &(ci[id]), XTICKLEN_MEDIUM,
                           label, "Medium", 6);
     
    id++;     
    label = psnd_sprintf_temp("%.2f", cpq->xticklen_long);
    popuphardcopy_add_text(mblk, cont_id, &(ci[id]), XTICKLEN_LONG,
                           label, "Long", 6);
     
    id++;
    popuphardcopy_add_panel(cont_id, &(ci[id]), NULL,
                            FALSE,  FALSE, FALSE);
    id++;     
    label = psnd_sprintf_temp("%.2f", cpq->xlabels_ppm);
    popuphardcopy_add_text(mblk, cont_id, &(ci[id]), XLABELS_PPM, 
                           label, "# Labels/unit", 10);
     
    id++;
    popuphardcopy_add_panel(cont_id, &(ci[id]), "Y offset",
                           TRUE, TRUE, TRUE);
    id++;     
    label = psnd_sprintf_temp("%.2f", cpq->xoff_axis);
    popuphardcopy_add_text(mblk, cont_id, &(ci[id]), XOFF_AXIS, 
                           label, "Axis (cm)", 12);
     
    id++;     
    label = psnd_sprintf_temp("%.2f", cpq->xoff_labels);
    popuphardcopy_add_text(mblk, cont_id, &(ci[id]), XOFF_LABELS, 
                           label, "Labels (cm)", 12);
          
    id++;
    popuphardcopy_add_panel(cont_id, &(ci[id]), NULL,
                            FALSE,  FALSE, FALSE);
    id++;
    popuphardcopy_add_panel(cont_id, &(ci[id]), "Offset text (cm)",
                           TRUE, TRUE, TRUE);
    id++;     
    label = psnd_sprintf_temp("%.2f", cpq->xstart_title);
    popuphardcopy_add_text(mblk, cont_id, &(ci[id]), XSTART_TITLE, 
                           label, "X", 12);

    id++;     
    label = psnd_sprintf_temp("%.2f", cpq->xoff_title);
    popuphardcopy_add_text(mblk, cont_id, &(ci[id]), XOFF_TITLE, 
                           label, "Y", 12);

    id++;
    popuphardcopy_add_panel(cont_id, &(ci[id]), NULL,
                            FALSE,  FALSE, FALSE);
    assert(id <MAXCHILD);   
    return id;
}

static int hardcopy_xaxis(MBLOCK *mblk, int win_id,CONPLOTQ_TYPE *cpq)
{
    int cont_id;
    static G_POPUP_CHILDINFO ci[MAXCHILD];
    int id,ok;
          
    cont_id = g_popup_container_open(win_id, "X axis parameters",
                                     G_POPUP_WAIT);
          
    id = -1;
    id = hardcopy_add_xaxis_stuff(mblk, cont_id, cpq, id, ci);
    ok = g_popup_container_show(cont_id);
/*    if (ok)
        ;   */
    return ok;
}

static int hardcopy_add_yaxis_stuff(MBLOCK *mblk, int cont_id,
           CONPLOTQ_TYPE *cpq, int id, G_POPUP_CHILDINFO ci[])
{
    static char *label, *radio_autoscale[] = { "OFF", "ON" };
          
    id++;
    popuphardcopy_add_option(mblk, cont_id, &(ci[id]), YTICKS_AUTOSCALE, 
                           "Autoscale tick mark", cpq->yticks_autoscale, 2,
                            radio_autoscale);
       
    id++;
    popuphardcopy_add_panel(cont_id, &(ci[id]), "Tick marks/unit",
                           TRUE, TRUE, TRUE);
    id++;     
    label = psnd_sprintf_temp("%.2f", cpq->yticks_ppm_short);
    popuphardcopy_add_text(mblk, cont_id, &(ci[id]), YTICKS_PPM_SHORT, 
                           label, "Short", 6);

    id++;     
    label = psnd_sprintf_temp("%.2f", cpq->yticks_ppm_medium);
    popuphardcopy_add_text(mblk, cont_id, &(ci[id]), YTICKS_PPM_MEDIUM, 
                           label, "Medium", 6);
     
    id++;     
    label = psnd_sprintf_temp("%.2f", cpq->yticks_ppm_long);
    popuphardcopy_add_text(mblk, cont_id, &(ci[id]), YTICKS_PPM_LONG, 
                           label, "Long", 6);
     
    id++;
    popuphardcopy_add_panel(cont_id, &(ci[id]), NULL,
                            FALSE,  FALSE, FALSE);

    id++;
    popuphardcopy_add_panel(cont_id, &(ci[id]), "Tick length (cm)",
                           TRUE, TRUE, TRUE);
    id++;     
    label = psnd_sprintf_temp("%.2f", cpq->yticklen_short);
    popuphardcopy_add_text(mblk, cont_id, &(ci[id]), YTICKLEN_SHORT,
                           label, "Short", 6);
     
    id++;     
    label = psnd_sprintf_temp("%.2f", cpq->yticklen_medium);
    popuphardcopy_add_text(mblk, cont_id, &(ci[id]), YTICKLEN_MEDIUM,
                           label, "Medium", 6);
     
    id++;     
    label = psnd_sprintf_temp("%.2f", cpq->yticklen_long);
    popuphardcopy_add_text(mblk, cont_id, &(ci[id]), YTICKLEN_LONG,
                           label, "Long", 6);
     
    id++;
    popuphardcopy_add_panel(cont_id, &(ci[id]), NULL,
                            FALSE,  FALSE, FALSE);
    id++;     
    label = psnd_sprintf_temp("%.2f", cpq->ylabels_ppm);
    popuphardcopy_add_text(mblk, cont_id, &(ci[id]), YLABELS_PPM, 
                           label, "# Labels/unit", 10);
     
    id++;
    popuphardcopy_add_panel(cont_id, &(ci[id]), "X offset",
                           TRUE, TRUE, TRUE);
    id++;     
    label = psnd_sprintf_temp("%.2f", cpq->yoff_axis);
    popuphardcopy_add_text(mblk, cont_id, &(ci[id]), YOFF_AXIS, 
                           label, "Axis (cm)", 12);
     
    id++;     
    label = psnd_sprintf_temp("%.2f", cpq->yoff_labels);
    popuphardcopy_add_text(mblk, cont_id, &(ci[id]), YOFF_LABELS, 
                           label, "Labels (cm)", 12);
          
    id++;
    popuphardcopy_add_panel(cont_id, &(ci[id]), NULL,
                            FALSE,  FALSE, FALSE);
    id++;
    popuphardcopy_add_panel(cont_id, &(ci[id]), "Offset text (cm)",
                           TRUE, TRUE, TRUE);
    id++;     
    label = psnd_sprintf_temp("%.2f", cpq->ystart_title);
    popuphardcopy_add_text(mblk, cont_id, &(ci[id]), YSTART_TITLE, 
                           label, "Y", 12);

    id++;     
    label = psnd_sprintf_temp("%.2f", cpq->yoff_title);
    popuphardcopy_add_text(mblk, cont_id, &(ci[id]), YOFF_TITLE, 
                           label, "X", 12);

    id++;
    popuphardcopy_add_panel(cont_id, &(ci[id]), NULL,
                            FALSE,  FALSE, FALSE);
     
    assert(id <MAXCHILD);   
    return id;
}

static int hardcopy_yaxis(MBLOCK *mblk, int win_id,CONPLOTQ_TYPE *cpq)
{
    int cont_id;
    static G_POPUP_CHILDINFO ci[MAXCHILD];
    int id,ok;
          
    cont_id = g_popup_container_open(win_id, "Y axis parameters",
                                     G_POPUP_WAIT);
          
    id = -1;
    id = hardcopy_add_yaxis_stuff(mblk, cont_id, cpq, id, ci);
          
    ok = g_popup_container_show(cont_id);
/*    if (ok)
        ;  */
    return ok;
}



static int hardcopy_paperdef(MBLOCK *mblk, int win_id,CONPLOTQ_TYPE *cpq)
{
    int cont_id;
    char *label, *radio_autoscale[] = { "OFF", "ON" };
    G_POPUP_CHILDINFO ci[MAXCHILD];
    int id,ok;
          
    cont_id = g_popup_container_open(win_id, "Paper Size Definitions",
                                     G_POPUP_WAIT);
          
    id = -1;

    id++;
    popuphardcopy_add_panel(cont_id, &(ci[id]), NULL,
                           TRUE, TRUE, FALSE);

    id++;
    cpq->dev_width = paper_sizedef[cpq->paper_type].width;
    label = psnd_sprintf_temp("%5.2f", cpq->dev_width);
    popuphardcopy_add_text(mblk, cont_id, &(ci[id]), PAPER_SIZE_WIDTH, 
                           label, "Paper Width", 16);
       
    id++;
    cpq->dev_height= paper_sizedef[cpq->paper_type].height;
    label = psnd_sprintf_temp("%5.2f", cpq->dev_height);
    popuphardcopy_add_text(mblk, cont_id, &(ci[id]), PAPER_SIZE_HEIGHT, 
                           label, "Paper Height", 16);
       
    id++;
    popuphardcopy_add_panel(cont_id, &(ci[id]), NULL,
                            FALSE, FALSE, FALSE);

    id++;
    popuphardcopy_add_panel(cont_id, &(ci[id]), NULL,
                           TRUE, TRUE, FALSE);
    id++;
    cpq->offx	= paper_def[cpq->paper_id].x0;
    label = psnd_sprintf_temp("%5.2f", cpq->offx);
    popuphardcopy_add_text(mblk, cont_id, &(ci[id]), OFFX, 
                           label, "Off X", 6);
       
    id++;
    cpq->offy	= paper_def[cpq->paper_id].y0;
    label = psnd_sprintf_temp("%5.2f", cpq->offy);
    popuphardcopy_add_text(mblk, cont_id, &(ci[id]), OFFY, 
                           label, "Off Y", 6);
       
    id++;
    cpq->width	= paper_def[cpq->paper_id].width;
    label = psnd_sprintf_temp("%5.2f", cpq->width);
    popuphardcopy_add_text(mblk, cont_id, &(ci[id]), WIDTH, 
                           label, "Width", 6);
       
    id++;
    cpq->height	= paper_def[cpq->paper_id].height;
    label = psnd_sprintf_temp("%5.2f", cpq->height);
    popuphardcopy_add_text(mblk, cont_id, &(ci[id]), HEIGHT, 
                           label, "Height", 6);
       
    id++;
    popuphardcopy_add_panel(cont_id, &(ci[id]), NULL,
                            FALSE, FALSE, FALSE);


    assert(id <MAXCHILD);   
          
    ok = g_popup_container_show(cont_id);
/*    if (ok)
        ;  */
    return ok;
}

static int  hardcopy_add_axis_stuff(MBLOCK *mblk, int cont_id, 
              CONPLOTQ_TYPE *cpq, int id, G_POPUP_CHILDINFO ci[])
{
    static char *radio_units[] = {"PPM", "Hertz", "Seconds", "Channels"};
    static char *xyaxis[] = { "None", "X", "Y", "XY" };
    char *label;
          
    id++;
    popuphardcopy_add_panel(cont_id, &(ci[id]), NULL,
                           FALSE, TRUE, FALSE);
    if (cpq->is_2d) {
        id++;
        popuphardcopy_add_option(mblk, cont_id, &(ci[id]), SHOW_XAXIS, 
                           "Axis ", cpq->show_xaxis + 2 * cpq->show_yaxis, 
                           4, xyaxis);
        id++;
        popuphardcopy_add_panel(cont_id, &(ci[id]), "Axis Units",
                           TRUE, TRUE, TRUE);
        id++;
        popuphardcopy_add_option(mblk, cont_id, &(ci[id]), UNITSX, 
                               "X", cpq->unitsx, 4,
                                radio_units);
        id++;
        popuphardcopy_add_option(mblk, cont_id, &(ci[id]), UNITSY, 
                               "Y", cpq->unitsy, 4,
                                radio_units);
        id++;
        popuphardcopy_add_panel(cont_id, &(ci[id]), NULL,
                           FALSE, FALSE, FALSE);
        id++;
        label = psnd_sprintf_temp("%s", cpq->axtxt);
        popuphardcopy_add_longtext(mblk, cont_id, &(ci[id]), AXTXT, 
                           label, "Text for x axis", 30);
        id++;
        label = psnd_sprintf_temp("%s", cpq->aytxt);
        popuphardcopy_add_longtext(mblk, cont_id, &(ci[id]), AYTXT, 
                           label, "Text for y axis", 30);
        id++;
        popuphardcopy_add_panel(cont_id, &(ci[id]), NULL,
                           TRUE, TRUE, TRUE);
#ifdef _WIN32bla
        id++;
        popuphardcopy_add_panel(cont_id, &(ci[id]), NULL,
                           FALSE, TRUE, FALSE);
        id = hardcopy_add_xaxis_stuff(mblk, cont_id, cpq, id,  ci);
        id++;
        popuphardcopy_add_panel(cont_id, &(ci[id]), NULL,
                           FALSE, FALSE,FALSE);
        id++;
        popuphardcopy_add_panel(cont_id, &(ci[id]), NULL,
                           FALSE, TRUE, FALSE);
        id = hardcopy_add_yaxis_stuff(mblk, cont_id, cpq, id,  ci);
        id++;
        popuphardcopy_add_panel(cont_id, &(ci[id]), NULL,
                           FALSE, FALSE,FALSE);
     
#else
        id++;
        popuphardcopy_add_button(mblk, cont_id, &(ci[id]), POPUP_XAXIS, 
                           "Parameters X axis ");
        id++;
        popuphardcopy_add_button(mblk, cont_id, &(ci[id]), POPUP_YAXIS, 
                           "Parameters Y axis ");
#endif
        id++;
        popuphardcopy_add_panel(cont_id, &(ci[id]), NULL,
                           FALSE, FALSE, FALSE);
    }
    else {
        id++;
        popuphardcopy_add_panel(cont_id, &(ci[id]), NULL,
                           TRUE, TRUE, FALSE);
        id++;
        popuphardcopy_add_option(mblk, cont_id, &(ci[id]), SHOW_XAXIS, 
                           "Axis ", cpq->show_xaxis, 
                           2, xyaxis);
        id++;
        popuphardcopy_add_option(mblk, cont_id, &(ci[id]), UNITSX, 
                               "Units", cpq->unitsx, 4,
                                radio_units);
        id++;
        popuphardcopy_add_panel(cont_id, &(ci[id]), NULL,
                           FALSE, FALSE, FALSE);
        id++;
        label = psnd_sprintf_temp("%s", cpq->axtxt);
        popuphardcopy_add_longtext(mblk, cont_id, &(ci[id]), AXTXT, 
                           label, "Text for x axis", 30);
     
#ifdef _WIN32bla
        id = hardcopy_add_xaxis_stuff(mblk, cont_id, cpq, id,  ci);
#else
        id++;
        popuphardcopy_add_button(mblk, cont_id, &(ci[id]), POPUP_XAXIS, 
                           "Parameters X axis");
#endif
    }
    assert(id <MAXCHILD);   
          
    return id;

}



static int  hardcopy_axis_setup(MBLOCK *mblk, int win_id,CONPLOTQ_TYPE *cpq)
{
    int cont_id;
    G_POPUP_CHILDINFO ci[MAXCHILD];
    int id,ok;
          
    cont_id = g_popup_container_open(win_id, "Axis Setup",
                                     G_POPUP_WAIT);
          
    id = -1;
    id = hardcopy_add_axis_stuff(mblk, cont_id, cpq, id, ci);


          
    ok = g_popup_container_show(cont_id);
/*    if (ok)
        ;  */
    return ok;

}

static int hardopy_add_title_stuff(MBLOCK *mblk, int cont_id, 
                                   CONPLOTQ_TYPE *cpq, int id)
{
    char *label;
    static char *radio_plot[] = { "OFF", "ON" };
    G_POPUP_CHILDINFO ci[MAXCHILD];

    id++;
    popuphardcopy_add_panel(cont_id, &(ci[id]), NULL,
                           TRUE, TRUE, FALSE);

    id++;
    popuphardcopy_add_option(mblk, cont_id, &(ci[id]), SHOW_TITLE, 
                           "Plot Title", cpq->show_title, 2,
                            radio_plot);
    id++;
    popuphardcopy_add_button(mblk, cont_id, &(ci[id]), ADDTO_TITLE, 
                           "  Add filename  ");
    id++;
    popuphardcopy_add_panel(cont_id, &(ci[id]), NULL,
                           FALSE, FALSE, FALSE);

    id++;
    label = psnd_sprintf_temp("%s", cpq->plot_title);
    popuphardcopy_add_longtext(mblk, cont_id, &(ci[id]), PLOT_TITLE, 
                           label, "Title", 40);

    return id;
}

static int hardcopy_plot_title(MBLOCK *mblk, int win_id,CONPLOTQ_TYPE *cpq)
{
    int cont_id;
    int id, ok;
          
    cont_id = g_popup_container_open(win_id, "Plot Title",
                                     G_POPUP_WAIT);
          
    id = -1;

    id = hardopy_add_title_stuff(mblk, cont_id,
                                   cpq, id);
    assert(id <MAXCHILD);   
          
    ok = g_popup_container_show(cont_id);
/*    if (ok)
        ;  */
    return ok;
}
                        
int psnd_hardcopy_param1d2d(MBLOCK *mblk, int is_2d) 
{
    int cont_id;
    int dest_id;
    int val;
    int device;
    int option=0;
    char *filename, *title, emptystring[2]=" ";
    FILE *outfile;
    float width, height, asp_ratio;
    float xminv, yminv, xmaxv, ymaxv;
    char *label,label1[PSND_STRLEN],*label2,*label3,*label4;
#ifdef _WIN32
    char *radio_type[] = {"HPGL", "BW Postscript", "Color Postscript", 
                          "Windows Meta File", "Color Printer", "BW Printer"};
    int num_output_type = 6;
    static char *outputto[] = { "Plot to file/printer", "Preview mode" };
#else
    char *radio_type[] = {"HPGL", "BW Postscript", "Color Postscript", 
                          "Windows Meta File"};
    int num_output_type = 4;
    static char *outputto[] = { "Plot to file    ", "Preview mode" };
#endif
    char *radio_paper[] = {"A4", "A3", "Letter", "Legal", "User"};
    char *radio_orientation[] = {"Landscape", "Portrait"};
/*    char *radio_units[] = {"PPM", "Hertz", "Seconds", "Channels"};*/
    char *check_options[] = {"Include PCL5 control codes", 
                             "Include paper definition",
                             "Keep aspect ratio"};
    static int check_options_select[] = {0,0,1};
    static int paper_flag, aspect_flag, orient_flag;
    static char *yesno[] = { "No", "Yes" };
    static char *yesnoauto[] = { "No", "Yes", "Auto" };
    static char *autoscale[] = { "Auto scale", "Fixed Hz/cm", "Fill Paper" };
    static char *autoscale2d[] = { "Auto", "Hz/cm", "Fill   " };
/*    static char *xyaxis[] = { "None", "X", "Y", "XY" }; */
    static char *ppsign[] = { "-" , "+/-", "+"};
    static char *penwidth[] = { "0.0", "0.002" , "0.004", "0.008", "0.017" };
    G_POPUP_CHILDINFO ci[MAXCHILD];
    int i,id,ok;
    CONPLOTQ_TYPE *cpq = mblk->cpq_screen;
    CBLOCK *cpar = mblk->cpar_screen;
    DBLOCK *dat = DAT;
                        
    label2 = label1+40;
    label3 = label2+40;
    label4 = label3+40;
    cpq->is_2d = is_2d;
    if (is_2d) {
        cpq->par1 = mblk->cpar_screen->par1;
        cpq->par2 = mblk->cpar_screen->par2;
    }
    else {
        cpq->par1 = PAR;
        cpq->par2 = NULL;
    }
    cpq->dat  = dat;
    
    if (is_2d) {
        g_get_world(mblk->spar[S_CONTOUR].vp_id,
                &xminv,
                &yminv,
                &xmaxv,
                &ymaxv);
        cont_id = g_popup_container_open(mblk->info->win_id, 
                                         "Hardcopy Contour parameters",
                                         G_POPUP_WAIT|G_POPUP_TAB);
    }
    else {
        g_get_world(mblk->spar[S_REAL].vp_id,
                &xminv,
                &yminv,
                &xmaxv,
                &ymaxv);
#ifndef __sgi
        cont_id = g_popup_container_open(mblk->info->win_id, 
                                         "Hardcopy 1D parameters",
                                         G_POPUP_WAIT|G_POPUP_TAB);
#else
        cont_id = g_popup_container_open(mblk->info->win_id, 
                                         "Hardcopy 1D parameters",
                                         G_POPUP_WAIT);
#endif
    }
    /*
     * If zoom range has not been changed
     * do not change user-defined settings
     */
    if (!(cpq->xminv_store == xminv &&
          cpq->yminv_store == yminv &&
          cpq->xmaxv_store == xmaxv &&
          cpq->ymaxv_store == ymaxv)) {

        cpq->xminv = xminv;
        cpq->yminv = yminv;
        cpq->xmaxv = xmaxv;
        cpq->ymaxv = ymaxv;

        cpq->xminv_store = cpq->xminv;
        cpq->yminv_store = cpq->yminv;
        cpq->xmaxv_store = cpq->xmaxv;
        cpq->ymaxv_store = cpq->ymaxv;
    }
          
    id = 0;
#ifndef __sgi
    g_popup_init_info(&(ci[id]));
    ci[id].type = G_CHILD_TAB;
    ci[id].label = "Output/Paper";
    g_popup_add_child(cont_id, &(ci[id]));
    id++;
#endif

    popuphardcopy_add_option(mblk, cont_id, &(ci[id]), OUTPUT_TYPE, 
                           "Output type", cpq->output_type, num_output_type,
                            radio_type);

    id++;
    popuphardcopy_add_option(mblk, cont_id, &(ci[id]), PREVIEW, 
                           "Output to  ", cpq->preview_mode, 2,
                            outputto);
    id++;
    check_options_select[0] = cpq->options[0];
    check_options_select[1] = cpq->options[1];
    check_options_select[2] = cpq->options[2];
    g_popup_init_info(&(ci[id]));
    ci[id].type = G_CHILD_CHECKBOX;
    ci[id].id            = OPTIONS;
    ci[id].title         = "Options";
    ci[id].frame         = TRUE;
    ci[id].horizontal    = FALSE;
    ci[id].item_count    = 2;
    ci[id].data          = check_options;
    ci[id].select        = check_options_select;
    ci[id].func		 = hardcopy_callback;
    ci[id].userdata	= (void*) mblk;
    g_popup_add_child(cont_id, &(ci[id]));


    id++;
    popuphardcopy_add_option(mblk, cont_id, &(ci[id]), PEN_WIDTH, 
                           "Pen width (cm)", cpq->pen_width, MAX_PEN_WIDTH,
                            penwidth);

    id++;
    popuphardcopy_add_panel(cont_id, &(ci[id]), "Paper definition (cm)",
                           FALSE, TRUE, TRUE);

    id++;
    popuphardcopy_add_panel(cont_id, &(ci[id]), NULL,
                           TRUE, TRUE, FALSE);

    id++;
    popuphardcopy_add_option(mblk, cont_id, &(ci[id]), PAPER_TYPE, 
                           "Type   ", cpq->paper_type, NUM_PAPER_SIZES,
                            radio_paper);
       
    id++;
    popuphardcopy_add_option(mblk, cont_id, &(ci[id]), PAPER_ORIENTATION, 
                           " ", cpq->paper_orientation, 2,
                            radio_orientation);
       
    id++;
    popuphardcopy_add_panel(cont_id, &(ci[id]), NULL,
                            FALSE, FALSE, FALSE);

    id++;
    popuphardcopy_add_button(mblk, cont_id, &(ci[id]), POPUP_PAPER_SIZES, 
                           "Paper Size Definitions");

    id++;
    popuphardcopy_add_panel(cont_id, &(ci[id]), NULL,
                            FALSE, FALSE, FALSE);
 
#ifdef __sgi    
    id++;
    popuphardcopy_add_button(mblk, cont_id, &(ci[id]), AXIS_SETUP, 
                           "Axis Setup");
#else
    id++;
    g_popup_init_info(&(ci[id]));
    ci[id].type = G_CHILD_TAB;
    ci[id].label = "Axis";
    g_popup_add_child(cont_id, &(ci[id]));

    id = hardcopy_add_axis_stuff(mblk, cont_id, cpq, id, ci);

#endif

#ifndef __sgi

    id++;
    g_popup_init_info(&(ci[id]));
    ci[id].type = G_CHILD_TAB;
    ci[id].label = "Parameters";
    g_popup_add_child(cont_id, &(ci[id]));

    id = hardopy_add_title_stuff(mblk, cont_id,
                                   cpq, id);
#else
    id++;
    popuphardcopy_add_button(mblk, cont_id, &(ci[id]), PLOT_TITLE, 
                           "Plot Title");
#endif

    units2labels(cpq, is_2d, label1, label2, label3, label4);
    id++;
    popuphardcopy_add_panel(cont_id, &(ci[id]), 
                           "Spectral Plotting Limits",
                           FALSE, TRUE, TRUE);
    id++;
    popuphardcopy_add_panel(cont_id, &(ci[id]), NULL,
                           TRUE, TRUE, FALSE);
    id++;
    popuphardcopy_add_text(mblk, cont_id, &(ci[id]), XMINV, 
                           label1, "Left", 16);
    id++;
    popuphardcopy_add_text(mblk, cont_id, &(ci[id]), XMAXV, 
                           label2, "Right", 16);
    id++;
    popuphardcopy_add_panel(cont_id, &(ci[id]), NULL,
                            FALSE, FALSE, FALSE);
    id++;
    popuphardcopy_add_panel(cont_id, &(ci[id]), NULL,
                           TRUE, TRUE, FALSE);
    id++;
    popuphardcopy_add_text(mblk, cont_id, &(ci[id]), YMINV, 
                           label3, "Bottom", 16);
    id++;
    popuphardcopy_add_text(mblk, cont_id, &(ci[id]), YMAXV, 
                           label4, "Top", 16);

    id++;
    popuphardcopy_add_panel(cont_id, &(ci[id]), NULL,
                            FALSE, FALSE, FALSE);
    id++;
    popuphardcopy_add_panel(cont_id, &(ci[id]), NULL,
                            FALSE, FALSE, FALSE);
     
    if (is_2d) {
        id++;
        popuphardcopy_add_panel(cont_id, &(ci[id]), "Spectrum Scaling",
                           TRUE, TRUE, TRUE);
        id++;
        popuphardcopy_add_option2(mblk, cont_id, &(ci[id]), FIX, 
                            "Scaling", cpq->fix, 3,
                            autoscale2d);
       
        id++;
        label = psnd_sprintf_temp("%5.2f", cpq->hzx);
        popuphardcopy_add_text(mblk, cont_id, &(ci[id]), HZX, 
                           label, "X Hz/cm", 6);
        id++;
        label = psnd_sprintf_temp("%5.2f", cpq->hzy);
        popuphardcopy_add_text(mblk, cont_id, &(ci[id]), HZY, 
                           label, "Y Hz/cm", 6);
        id++;
        popuphardcopy_add_panel(cont_id, &(ci[id]), NULL,
                            FALSE, FALSE, FALSE);

    }
    else {
        id++;
        popuphardcopy_add_panel(cont_id, &(ci[id]), "Spectrum Scaling",
                           TRUE, TRUE, TRUE);
        id++;
        popuphardcopy_add_option2(mblk, cont_id, &(ci[id]), FIX, 
                            "Type", cpq->fix, 3,
                            autoscale);
       
        id++;
        label = psnd_sprintf_temp("%5.2f", cpq->hzx);
        popuphardcopy_add_text(mblk, cont_id, &(ci[id]), HZX, 
                           label, "X Hz/cm", 16);
        id++;
        popuphardcopy_add_panel(cont_id, &(ci[id]), NULL,
                            FALSE, FALSE, FALSE);
    }
         
    if (is_2d) {
        id++;
        popuphardcopy_add_panel(cont_id, &(ci[id]), "Grid Options",
                           TRUE, TRUE, TRUE);
        id++;
        popuphardcopy_add_option2(mblk, cont_id, &(ci[id]), IGRID, 
                            "Grid", cpq->igrid, 3,
                            yesnoauto);
        id++;
        label = psnd_sprintf_temp("%.2f", cpq->xgridlines_ppm);
        popuphardcopy_add_text(mblk, cont_id, &(ci[id]), XGRIDLINES_PPM, 
                           label, "Xline/unit", 6);
     
        id++;
        label = psnd_sprintf_temp("%.2f", cpq->ygridlines_ppm);
        popuphardcopy_add_text(mblk, cont_id, &(ci[id]), YGRIDLINES_PPM, 
                           label, "Yline/unit", 6);
        id++;
        popuphardcopy_add_panel(cont_id, &(ci[id]), NULL,
                            FALSE, FALSE, FALSE);
    }
    
    if (!is_2d) {
        id++;
        popuphardcopy_add_panel(cont_id, &(ci[id]), "Peak Picking",
                           TRUE, TRUE, TRUE);
        id++;
        popuphardcopy_add_option2(mblk, cont_id, &(ci[id]), PEAKPICK, 
                            "Peak Pick", cpq->peakpick, 2,
                            yesno);
        id++;
        popuphardcopy_add_option2(mblk, cont_id, &(ci[id]), SIGN, 
                            "Sign", cpq->sign+1, 3,
                            ppsign);

        id++;
        label = psnd_sprintf_temp("%5g", cpq->thresh);
        popuphardcopy_add_text(mblk, cont_id, &(ci[id]), THRESH, 
                           label, "Threshold", 10);

        id++;
        popuphardcopy_add_panel(cont_id, &(ci[id]), NULL,
                            FALSE,  FALSE, FALSE);
/*
        id++;
        label = psnd_sprintf_temp("%5.1f", cpq->sens);
        popuphardcopy_add_text(cont_id, &(ci[id]), SENS, 
                           label, "Sensitivity", 10);
*/
    }
    id++;
    popuphardcopy_add_panel(cont_id, &(ci[id]), "Plot Parameters",
                           TRUE, TRUE, TRUE);
    id++;
    popuphardcopy_add_button(mblk,cont_id, &(ci[id]), SAVESETTINGS, 
                            "Save Parameters");

    id++;
    popuphardcopy_add_button(mblk, cont_id, &(ci[id]), READSETTINGS, 
                            "Read Parameters");

    id++;
    popuphardcopy_add_panel(cont_id, &(ci[id]), NULL,
                            FALSE,  FALSE, FALSE);
    assert(id < MAXCHILD); 
    cpq->cont_id = cont_id;
    ok = g_popup_container_show(cont_id);
    cpq->cont_id = 0;
    if (ok) {
        process_settings(cpq, cpar);  
        ok += cpq->preview_mode;
    }
    return ok;
}

/*
 * pen width= 0, 1, 2, 4, 8 ..
 */
int psnd_calc_ps_penwidth(int no)
{
    int i,width;
    no = max(no, 0);
    no = min(MAX_PEN_WIDTH, no);
    if (no <= 2)
        return no;
    width = 2;
    for (i=0;i<no-2;i++)
        width *= 2;
    return width;
}

/*
 * calculate the number of long ticks per unit
 */
static float get_long_ticks(float x)
{
    float q=1;

    x = fabs(x);
    while (x > 10) {
        x/=10;
        q/=10;
    }
    while (x < 1 && x > 0) {
        x*=10;
        q*=10;
    }
    return q;
}

/*
 * calculate the number of labels per unit
 */
static float get_label_ticks(float x)
{
    float q=1.0;

    if (x == 0.0)
        return 0.0;
    x = fabs(x);
    while (x > 10) {
        x/=10;
        q/=10;
    }
    while (x < 0.5) {
        x*=10;
        q*=10;
    }
    while (x < 4 && x >= 2) {
        x*=2;
        q*=2;
    }
    while (x < 2) {
        x*=5;
        q*=5;
    }
    return q;
}


/*
 * calculate the number of digits in a label
 */
static int calc_label_digits(float start, float stop, 
                             float labels_ppm, float steps_ppm)
{
    float a,b,c;
    int j, k, ia, ib, init = FALSE, digits = 0;

    if (labels_ppm == 0 || steps_ppm == 0)
        return 1;
    k = round(steps_ppm/labels_ppm);
    if (k < 1)
        k = 1;
    c = 1.0;
    for (j=start;j>=stop;j--) {
        if (!(j % k)) {
            b = ((float) j)/steps_ppm;
            if (!init) 
                init = TRUE;
            else {
                do {
                    ia = (int)(c*a);
                    ib = (int)(c*b);
                    if (ia==ib) {
                        c *= 10;
                        digits++;
                    }
                } while (ia==ib);
            }
            a = b;
        }
    }
    return digits;
}

static float calc_steps(float longt, float mediumt, float shortt, float labels)
{
    float steps, ticks[4], maxsteps = 10000.0, small_number = 1e-02;
    int i,j, significant_digits = 2;

    ticks[0] = longt;
    ticks[1] = mediumt;
    ticks[2] = shortt;
    ticks[3] = labels;
    steps    = 1.0;
    for (i=0;i<4;i++) {
        if (ticks[i] > small_number) {
            for (j=0;j<significant_digits;j++)
                if (fmod(ticks[i],1.0))
                    ticks[i] *= 10;
            steps *= ticks[i];
        }
    }
    while (steps > maxsteps)
        steps /= 10;
    return steps;
}


void psnd_paperplot_xaxis(int vp, int autoscale, char *axislabel, 
                float minp, float maxp,
                float ticks_ppm, float medium_ticks_ppm, float long_ticks_ppm, 
                float labels_ppm, float off_axis, float off_label, 
                float start_title, float off_title,
                float ticklen1, float ticklen2, float ticklen3,
                float width, float height, 
                float vx1, float vy1, float vx2, float vy2)
{
    float tmp1, tmp2, off, steps_ppm;
    int n, labeldiv, tickdiv1, tickdiv2, tickdiv3;
    int start, stop, xdigits;
    char format[40];

    if (autoscale) {
        long_ticks_ppm   = get_long_ticks(width);
        medium_ticks_ppm = long_ticks_ppm * 2;
        ticks_ppm        = long_ticks_ppm * 10;
        labels_ppm       = get_label_ticks(width);
    }
    steps_ppm = calc_steps(long_ticks_ppm, medium_ticks_ppm,
                           ticks_ppm,labels_ppm);

    if (labels_ppm <= 0)
        labeldiv = 0;
    else
        labeldiv = round(steps_ppm/labels_ppm);
    if (ticks_ppm <= 0)
        tickdiv1 = 0;
    else
        tickdiv1  = round(steps_ppm/ticks_ppm);
    if (medium_ticks_ppm <= 0)
        tickdiv2 = 0;
    else
        tickdiv2  = round(steps_ppm/medium_ticks_ppm);
    if (long_ticks_ppm <= 0)
        tickdiv3 = 0;
    else
        tickdiv3 = round(steps_ppm/long_ticks_ppm);
    off   = height - off_axis;

    tmp1  = minp * steps_ppm;
    tmp2  = maxp * steps_ppm;
    start = ceil(max(tmp1,tmp2));
    stop  = floor(min(tmp1,tmp2));
    g_set_world(vp, tmp1, 0, tmp2, height);
    g_set_viewport(vp, vx1, vy1, vx2, vy2);
    g_select_viewport(vp);

    g_moveto(tmp1,off);
    g_lineto(tmp2,off);
/* g_rectangle(tmp1, 0, tmp2, height); */

    xdigits = calc_label_digits(start, stop, (float) labels_ppm, steps_ppm);
    sprintf(format,"%%.%df", xdigits);
    for (n=start;n>=stop;n--) {
        g_moveto(n, off);
        if (tickdiv3 && !(n % tickdiv3)) 
            g_lineto(n, off-ticklen3);
        else if (tickdiv2 && !(n % tickdiv2)) 
            g_lineto(n, off-ticklen2);
        else if (tickdiv1 && !(n % tickdiv1))
            g_lineto(n, off-ticklen1);
        if (labeldiv && !(n % labeldiv)) {
            char *label;
            g_moveto(n, off-off_label);
            label = psnd_sprintf_temp(format, (float)n/steps_ppm);
            g_label(label);
        }
    }
    if (axislabel) {
        g_moveto(start_title * steps_ppm, off-off_title);
        g_label(axislabel);
    }   
}

void psnd_paperplot_yaxis(int vp,  int autoscale, char *axislabel, float minp, float maxp, 
                float ticks_ppm, float medium_ticks_ppm, float long_ticks_ppm, 
                float labels_ppm, float off_axis, float off_label, 
                float start_title, float off_title,
                float ticklen1, float ticklen2, float ticklen3,
                float width, float height,
                float vx1, float vy1, float vx2, float vy2)
{
    float tmp1, tmp2, off, steps_ppm;
    int n, labeldiv, tickdiv1, tickdiv2, tickdiv3, start, stop, ydigits;
    char format[40];

    if (autoscale) {
        long_ticks_ppm   = get_long_ticks(height);
        medium_ticks_ppm = long_ticks_ppm * 2;
        ticks_ppm        = long_ticks_ppm * 10;
        labels_ppm       = get_label_ticks(height);
    }
    steps_ppm = calc_steps(long_ticks_ppm, medium_ticks_ppm,
                           ticks_ppm,labels_ppm);
    if (labels_ppm <= 0)
        labeldiv = 0;
    else
        labeldiv = round(steps_ppm/labels_ppm);
    if (ticks_ppm <= 0)
        tickdiv1 = 0;
    else
        tickdiv1  = round(steps_ppm/ticks_ppm);
    if (medium_ticks_ppm <= 0)
        tickdiv2 = 0;
    else
        tickdiv2  = round(steps_ppm/medium_ticks_ppm);
    if (long_ticks_ppm <= 0)
        tickdiv3 = 0;
    else
        tickdiv3 = round(steps_ppm/long_ticks_ppm);
    tmp1  = minp * steps_ppm;
    tmp2  = maxp * steps_ppm;
    start = ceil(max(tmp1,tmp2));
    stop  = floor(min(tmp1,tmp2));
    off   = width - off_axis;

    g_set_world(vp, 0, tmp1, width, tmp2);
    g_set_viewport(vp, vx1, vy1, vx2, vy2);
    g_select_viewport(vp);
    g_moveto(off,tmp1);
    g_lineto(off,tmp2);
    ydigits = calc_label_digits(start, stop, (float) labels_ppm, steps_ppm);
    sprintf(format,"%%.%df", ydigits);
    for (n= start;n>=stop;n--) {
        g_moveto(off, n);
        if (tickdiv3 && !(n % tickdiv3)) 
            g_lineto(off-ticklen3,n);
        else if (tickdiv2 && !(n % tickdiv2)) 
            g_lineto(off-ticklen2,n);
        else if (tickdiv1 && !(n % tickdiv1)) 
            g_lineto(off-ticklen1,n);
        if (labeldiv && !(n % labeldiv)) {
            char *label;
            g_moveto(off-off_label,n);
            label = psnd_sprintf_temp(format, (float)n/steps_ppm);
            g_set_textdirection(90);
            g_label(label);
            g_set_textdirection(0);
        }
    }
    if (axislabel) {
        g_moveto(off - off_title, start_title * steps_ppm);
        g_set_textdirection(90);
        g_label(axislabel);
        g_set_textdirection(0);
    }   
}

void psnd_paperplot_grid(int vp, int autoscalex, int autoscaley,
               float xminp, float xmaxp, float yminp, float ymaxp,
               float xlines_ppm, float ylines_ppm)
{
    float xtmp1, xtmp2, ytmp1, ytmp2;
    int n, start, stop;

    if (autoscalex) {
        float len = fabs(xminp-xmaxp);
        xlines_ppm = get_long_ticks(len);
    }
    if (autoscaley) {
        float len = fabs(yminp-ymaxp);
        ylines_ppm = get_long_ticks(len);
    }
    xtmp1  = xminp * xlines_ppm;
    xtmp2  = xmaxp * xlines_ppm;
    ytmp1  = yminp * ylines_ppm;
    ytmp2  = ymaxp * ylines_ppm;
    start = ceil(max(xtmp1,xtmp2));
    stop  = floor(min(xtmp1,xtmp2));
    g_set_world(vp, xtmp1, ytmp1, xtmp2, ytmp2);
    for (n= start;n>=stop;n--) {
        g_moveto(n, ytmp1);
        g_lineto(n, ytmp2);
    }
    start = ceil(max(ytmp1,ytmp2));
    stop  = floor(min(ytmp1,ytmp2));
    for (n= start;n>=stop;n--) {
        g_moveto(xtmp1, n);
        g_lineto(xtmp2, n);
    }
}



/*
 * Peak picking
 */

#define MAXLABEL	512

typedef struct {
    int   x;
    float y;
} pickedpeaks_type;

/*
 * sort on peak height
 */
static int sort1(const void *a, const void *b)
{
    pickedpeaks_type *aa, *bb;
    
    aa = (pickedpeaks_type*) a;
    bb = (pickedpeaks_type*) b;
    return (aa->y < bb->y) ? 1 : ((aa->y > bb->y) ? -1 : 0);
}

/*
 * sort on peak position
 */
static int sort2(const void *a, const void *b)
{
    pickedpeaks_type *aa, *bb;
    
    aa = (pickedpeaks_type*) a;
    bb = (pickedpeaks_type*) b;
    return (aa->x > bb->x) ? 1 : ((aa->x < bb->x) ? -1 : 0);
}


static void pkpurge(int *picked, int *purged, int *npick, int *npurged,
             int nlabel, float *spec)
{
    int   i,tpurged;
    pickedpeaks_type *pickedpeaks;

    pickedpeaks = (pickedpeaks_type*) malloc(sizeof(pickedpeaks_type) * *npick);
    for (i=0;i< *npick;i++) {
        pickedpeaks[i].x = picked[i];
        pickedpeaks[i].y = spec[pickedpeaks[i].x];
    }
    /*
     * sort on intensity, biggest first
     */
    qsort(pickedpeaks, *npick, sizeof(pickedpeaks_type), sort1);
    tpurged = *npick - nlabel;
    *npick = nlabel;
    /*
     * Get the first nlabel elements, and re-sort on order, smallest first
     */
    qsort(pickedpeaks, *npick, sizeof(pickedpeaks_type), sort2);
    for (i=0;i< *npick;i++) 
        picked[i] = pickedpeaks[i].x;

    for (i=0;i<tpurged && (*npurged + i < MAXLABEL-1);i++)
        purged[*npurged + i] = pickedpeaks[ *npick + i].x;
    *npurged = tpurged;
    free(pickedpeaks);

}

static void pkpick(float *spec, float chl, float chr, int avewidth, 
            float thresh, float sensi,
            int sign, int *tpick, int *picked, int *purged, 
            int npurged, int nlabel, int npart)
{
    int istart, istop, n, i, j, npick, nplabel;
    int *tpicked;

    tpicked = (int*) malloc(sizeof(int) * MAXLABEL);
/*    sens = sensi/10;*/
    nplabel = 0;
    *tpick = 0;
    npick = 0;
    if (avewidth < 2) 
        avewidth = 2;

    istart = round(chl) + avewidth - 1;
    istop  = istart;
    /*
     * Array starts at 1
     */
    spec--;
    /*
     * peak pick per cluster
     */
    for (n=0;n<npart;n++) {
        istart = istop + 1;
        istop = istop + round((chr - chl)/npart);
        if (istop > round(chr) - avewidth) 
            istop = round(chr) - avewidth;

        for (i=istart;i<istop && npick < MAXLABEL-1;i++) {
            /*
             * Pos peaks
             */
            if (sign >= 1) {
                if (spec[i] > thresh) {
                    if ( (spec[i-1] < spec[i]) &&
                          (spec[i+1] < spec[i]) ) {
                        tpicked[npick++] =  i;
                    }
                }
            }
            /*
             * All peaks
             */
            else if (sign == 0) {
                if (fabs(spec[i]) > thresh) {
                    if ( (fabs(spec[i-1]) < fabs(spec[i])) &&
                          (fabs(spec[i+1]) < fabs(spec[i])) ) {
                        tpicked[npick++] =  i;
                    }
                }
            }
            /*
             * Neg peaks
             */
            else if (sign <= -1) {
                if (spec[i] < (-1) * thresh) {
                    if ( (spec[i-1] > spec[i]) && 
                         (spec[i+1] > spec[i]) ) {
                        tpicked[npick++] =  i;
                    }
                }
            }
        }
        nplabel = (int)(nplabel + nlabel/npart);
        if (npick > nplabel) 
            pkpurge(tpicked,purged,&npick,&npurged,
                      nplabel,spec);
        nplabel -= npick;
        for (j=0;j<npick && (*tpick + j < MAXLABEL-1);j++)
            picked[*tpick + j] = tpicked[j];
        *tpick += npick;
        npick = 0;
    }
    free (tpicked);
}

/*
*    Routine to expand labels of closely spaced peaks.
*/
static void expand_label(int *picked, int npick, int *labpos,
                  int nlabel, float chl, float chr)
{
    int i, j, *labno;
    int rfree, lfree;
    int suml, sumr;
    float scalch;

    labno = (int*) malloc(sizeof(int) * (npick+1));
    for (i=0;i<nlabel;i++)
        labpos[i] = 0;
    scalch = ((float)(nlabel+1))/(chr-chl);
    for (i=0;i<npick;i++) {
        labno[i] = (int) (((float)picked[i] - chl) * scalch ) + 1;
        if (labno[i] >= nlabel) 
            labno[i] = nlabel-1;
    }
    for (i=0;i<npick;i++) {
        int ipos = labno[i];
        if ( labpos[ipos] == 0) 
            labpos[ipos] = i+1;
        else {
            j=0;
            while ( (labpos[ipos+j] != 0) && (ipos+j <= nlabel) )
               j++;
            rfree = ipos + j;
            if (rfree >= nlabel) 
                rfree = 0;
            j=0;
            while ( (labpos[ipos+j] != 0) && (ipos+j >= 0) )
                j--;
            lfree = ipos + j;
            if (lfree < 1) 
                lfree = 0;
            if (lfree == 0) 
                labpos[rfree] = i+1;
            else {
                if (rfree == 0) {
                    for (j=lfree;j<nlabel-1;j++)
                        labpos[j] = labpos[j+1];
                    labpos[nlabel-1] = i+1;
                }
                else {
                    /*
                     * cluster to the left or text 
                     * to the right side of cluster???
                     */
                    suml = 0;
                    sumr = 0;
                    for (j = lfree+1;j < rfree-1; j++) {
                        suml += fabs( labno[labpos[j]] -j +1);
                        sumr += fabs( labno[labpos[j]] -j);
                    }
                    suml += fabs( labno[labpos[i]] -rfree +1);
                    sumr += fabs( labno[labpos[i]] -rfree);
                }
                if (sumr < suml) 
                    labpos[rfree] = i+1;
                else {
                    for (j=lfree; j<rfree-1;j++) 
                        labpos[j] = labpos[j+1];
                    labpos[rfree-1] = i+1;
                }
            }
        }
    }
    free(labno);
}

static float convl(float x, float min, float max,
                   float cminx, float cmaxx)
{
    float result, scale = (cmaxx-cminx)/(max-min);

    result = cminx + (x-min) * scale;
    if (result < cminx) 
        result = cminx;
    if (result > cmaxx) 
        result = cmaxx;
    return result;
}

static void labplot(int vp, int vp_box, int *picked, 
                     int *labpos, float *ppml,
                     int i,int nlabel, float lheight, 
                     float box_height, float *xr, float xmin, float xmax,
                     float ymin, float ymax)
{
    float fac, p, xp, yp;
    float ticlength, lval, lx, charsize;
    char *label;

    g_select_viewport(vp_box);
    p = box_height/10;
    lx = convl((float)(i+1), 1.0, (float) nlabel, xmin, xmax);
    if (lx == xmin || lx == xmax)
        return;
    lval = ppml[labpos[i]-1];

    g_moveto(lx+0.5*lheight, box_height/2);
    g_set_textdirection(90);
    charsize = g_get_charsize();
    g_set_charsize(2*lheight);	
    label = psnd_sprintf_temp("%6.4f", lval);
    g_label(label);
    g_set_charsize(charsize);
    /*
    call number(lx+0.5*lheight,box_height,lheight*0.75,lval,90.,5)
    */
    g_set_textdirection(0);

    g_moveto(lx,box_height/2);
    xp = lx;
    yp = box_height/2 - p;
    g_lineto(xp,yp);
    xp = (float)picked[labpos[i]-1];
    yp = p;
    g_lineto(xp,yp);
    yp = 0;
    g_lineto(xp,yp);

    g_select_viewport(vp);
    yp = xr[picked[labpos[i]-1]-1];
    ticlength = (ymax-ymin)/100;
    if (yp >  0.0) 
        fac = 1.0;
    else
        fac = -1.0;
    /*
     *   plot tic mark if picked
     */

    yp += fac * ticlength;
    g_moveto(xp,yp);
    yp += fac * ticlength;
    g_lineto(xp,yp);
      
}

void psnd_set_peakpick_threshold(CONPLOTQ_TYPE *cpq, float thresh)
{
    cpq->thresh = thresh;
}
    

void psnd_paperplot_labels(int vp, int vp_box, float *xreal, 
                float xmin, float xmax, float ymin, float ymax,
                float cminx, float cmaxx, float cminy, float cmaxy,
                float thresh, float sens, float avewidth,
                int sign, int npart,
                float lheight, float box_height,
                float sfd, float swhold, int nsiz, float xref,
                float aref, float bref, int irc, int unitx)
{
    int   i, npick, npurged = 0;
    int   *labpos, *picked, *purged;
    float *ppml;
    int nlabel;

    labpos = (int*) malloc(sizeof(int) * MAXLABEL); 
    /*
     * array 'picked' contains channel numbers, thus
     * it starts at 0, but contains values starting at 1
     */
    picked = (int*) malloc(sizeof(int) * MAXLABEL); 
    purged = (int*) malloc(sizeof(int) * MAXLABEL); 
    ppml = (float*) malloc(sizeof(float) * MAXLABEL); 
    /*
     * determine number op labels-slots
     */
    nlabel	= (int) floor((cmaxx - cminx)/(lheight));
    pkpick(xreal, xmin-1, xmax-1, avewidth, thresh, sens,sign, &npick, 
           picked, purged, npurged, nlabel, npart);
    expand_label(picked, npick, labpos,
           nlabel, xmin-1, xmax-1);
    /*
     *       calculate pick frequencies
     */
    if (unitx == UNIT_PPM)
        for (i=0;i<npick;i++)
            ppml[i] = psnd_chan2ppm(picked[i], sfd, swhold, nsiz, 
                                     xref, aref, bref, irc);
    else if (unitx == UNIT_HERTZ)
        for (i=0;i<npick;i++)
            ppml[i] = psnd_chan2hz(picked[i], swhold, nsiz, 
                                     xref, aref, bref, irc);
    else if (unitx == UNIT_SEC)
        for (i=0;i<npick;i++)
            ppml[i] = psnd_chan2sec(picked[i], swhold);
    else 
        for (i=0;i<npick;i++)
            ppml[i] = picked[i];
    /*
     *   draw labels
     */
    for (i=0;i<nlabel;i++)
        if (labpos[i] != 0) 
            labplot(vp, vp_box, picked, labpos, ppml,
                    i, nlabel, lheight, 
                    box_height, xreal, xmin, xmax, ymin, ymax);
    free(labpos); 
    free(picked); 
    free(purged); 
    free(ppml); 
}




