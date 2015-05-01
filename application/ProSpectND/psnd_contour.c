/********************************************************************/
/*                          psnd_contour.c                          */
/*                                                                  */
/* 1997, Albert van Kuik                                            */
/********************************************************************/


#include <stdio.h>
#include <stdlib.h>
#include <ctype.h>
#include <string.h>
#include <math.h>
#include <assert.h>
#include "genplot.h"
#include "psnd.h"
#include "nmrtool.h"



static void blockplot(MBLOCK *mblk, float *xcpr, int mode, int ncpr, 
                      float *clevel, int *levcol,
                      int nlevel, int inpfil, float *x, int maxdat, 
                      int ilowa, int ihigha, int ilowb, int ihighb, int iadiv,
                      int ibdiv, CBLOCK *cpar, DBLOCK *dat);
static void cntr2d(MBLOCK *mblk, CBLOCK *cpar, float *xcpr, 
            char *bits, int ncpr, float *clevel, 
            int *levcol, int nlevel, 
            int inpfil, float *x, int maxdat, 
            int ilowa, int ihigha, int ilowb, int ihighb,
            int iadiv, int ibdiv);



static int edit_contour_levels(int win_id, int start, int addmode, CBLOCK *cpar)
{
    int i, cont_id;
    G_POPUP_CHILDINFO ci[40];
    char label[40];
    char label2[40];
    int ok,stop;

    while (start < cpar->nlevel) {
        stop  = min(start + 8, cpar->nlevel);
        cont_id = g_popup_container_open(win_id, "Contour Levels",
                                         G_POPUP_WAIT);
        for (i=start; i < stop; i++) {
            int disabled = addmode && (i < stop-1);
            sprintf(label, "%g", cpar->clevel[i]);
            sprintf(label2, "level %d", i+1);
            popup_add_text3(cont_id, &(ci[i]), 0, label, label2,
                disabled);
        }
        ok = g_popup_container_show(cont_id);
        if (ok) {
            for (i=start;i<stop;i++) {
                cpar->clevel[i] = psnd_scan_float(ci[i].label);
            }
        }
        else
            return FALSE;
        start = stop;
    }
    return TRUE;
}



void psnd_process_color_levels(MBLOCK *mblk, CBLOCK *cpar)
{
    int n;
    
    switch (cpar->colormode) {
        case 0:
            for (n=1;n<cpar->nlevel;n++) {
                cpar->levcol[n] = mblk->spar[S_CONTOUR].color;
            }
            break;
        case 1:
            for (n=0;n<cpar->nlevel;n++) {
                if (cpar->clevel[n] > 0)
                    cpar->levcol[n] = mblk->spar[S_CONTOUR].color;
                else
                    cpar->levcol[n] = mblk->spar[S_CONTOUR].color2;
            }
            break;
/*        case 3:*/
        case 2:
            {
                int step, maxcolor = g_get_palettesize();
                step = (maxcolor - 16) / cpar->nlevel;
                if (cpar->plusmin == LEVELS_POSITIVE)
                    for (n=0;n<cpar->nlevel;n++)
                        cpar->levcol[n] = 16 + n * step;
                else if (cpar->plusmin == LEVELS_NEGATIVE)
                    for (n=0;n<cpar->nlevel;n++)
                        cpar->levcol[n] = 16 + 
                            (cpar->nlevel-1-n) * step;
                else if (cpar->plusmin == LEVELS_BOTH) {
                    for (n=0;n<cpar->nlevel/2;n++)
                        cpar->levcol[n] = 16 + n * step;
                    for (n=0;n<cpar->nlevel/2;n++)
                        cpar->levcol[n+cpar->nlevel/2] = 16 + 
                            (cpar->nlevel-1-n) * step;
                }
            }
            break;
    }
}

void psnd_process_contour_levels(MBLOCK *mblk, CBLOCK *cpar)
{
      switch (cpar->mlevel) {
             case 0 :
                 if (!cpar->silent)
                     edit_contour_levels(mblk->info->win_id, 0, FALSE, cpar);
                 break;
             case 1 : 
             case 2 :
             case 3 : {
                 int n;
                 if (cpar->plusmin == LEVELS_BOTH) {
                     cpar->nlevel /= 2;
                     cpar->nlevel = max(cpar->nlevel, 1);
                 }
                 cpar->clevel[0] = fabs(cpar->clevel[0]);
                 switch (cpar->mlevel) {
                     case 1:
                         for (n=1;n<cpar->nlevel;n++) 
                             cpar->clevel[n] = fabs(cpar->clevel[n-1] * 
                                 cpar->flevel);
                         break;
                     case 2:
                         for (n=1;n<cpar->nlevel;n++) 
                             cpar->clevel[n] = fabs(cpar->clevel[n-1] + 
                                 cpar->dlevel);
                         break;
                     case 3: {
                         float x1, x2, delta;
                         x1 = min(cpar->clevel[0] , cpar->levelmax); 
                         x1 = max(x1, 1.0); 
                         x2 = max(cpar->clevel[0], cpar->levelmax); 
                         delta = (log10(x2)-log10(x1))/((float)(cpar->nlevel-1));
                         cpar->flevel = pow(10.0, delta);
                         for (n=1;n<cpar->nlevel;n++) 
                             cpar->clevel[n] = fabs(cpar->clevel[n-1] * 
                                 cpar->flevel);
                         }
                         break;
                    
                 }
                 if (cpar->plusmin == LEVELS_BOTH) {
                     for (n=0;n<cpar->nlevel;n++) 
                         cpar->clevel[n+cpar->nlevel] = -cpar->clevel[n];
                     cpar->nlevel *= 2;
                 }
                 else if (cpar->plusmin ==  LEVELS_NEGATIVE)
                     for (n=0;n<cpar->nlevel;n++) 
                         cpar->clevel[n] = -cpar->clevel[n];
                 }
                 break;
         }

#ifdef PIXBUTTONS

         if (mblk->cpar_screen->plusmin == 0)
             g_menu_set_pixmap(mblk->info->bar_id,
                   PSND_LEVELS_BOTH, 1);
         else if (mblk->cpar_screen->plusmin == 1)
             g_menu_set_pixmap(mblk->info->bar_id, 
                   PSND_LEVELS_BOTH, 0);
         else
             g_menu_set_pixmap(mblk->info->bar_id,
                   PSND_LEVELS_BOTH, 2);
                   
#else
         if (mblk->cpar_screen->plusmin == 0)
             g_menu_set_label(mblk->info->bar_id, PSND_LEVELS_BOTH, "CL + ");
         else if (mblk->cpar_screen->plusmin == 1)
             g_menu_set_label(mblk->info->bar_id, PSND_LEVELS_BOTH, "CL+/-");
         else
             g_menu_set_label(mblk->info->bar_id, PSND_LEVELS_BOTH, "CL - ");
#endif

}



void psnd_init_contour_par(SBLOCK *spar, CBLOCK *cpar)
{
    cpar->maxcpr = MAXCPR;
    cpar->maxlev = MAXLEV;
    cpar->maxdat = MAXDAT;
    cpar->ipre = -1;
    if (!cpar->once) {
        int i;
        
        cpar->once      = TRUE;
        cpar->mlevel    = 1;
        cpar->nlevel    = cpar->maxlev/2;
        cpar->flevel    = 1.5;
        cpar->clevel[0] = 5.0e5;
        cpar->levcol[0] = spar[S_CONTOUR].color;
        cpar->levcol[1] = spar[S_CONTOUR].color2;
        if (cpar->maxlev > 1) {
            for (i=1;i<cpar->maxlev;i++) {
                cpar->clevel[i]=cpar->flevel*cpar->clevel[i-1];
                cpar->levcol[i]=1;
            }
        }
        cpar->dlevel 	= 1.0E5;
        cpar->levelmax  = 1.0E6;
        cpar->colormode = 1;
        cpar->plusmin   = 0;
        cpar->intmod    = 0;
        cpar->intlevel  = 0;
        cpar->scale_i   = 1.0;
        cpar->zoint     = FALSE;
        cpar->ref       = 1.8;
        cpar->vref      = 1e7;
        cpar->rref      = cpar->vref * pow(cpar->ref,6.0);
        cpar->nshift    = 4;
        cpar->pinpoi    = FALSE;
        cpar->primax    = FALSE;
        cpar->pribox    = FALSE;
        for (i=0;i<4;i++) 
            cpar->box[i] = 0;
        cpar->int2d_queue = NULL;
        cpar->i_count   = 0;
        cpar->i_size    = 0;
        cpar->i_select  = 0;
        cpar->ppmode    = PEAKPICK_FIND;

        for (i=0;i<3;i++) {
            cpar->xreal[i] = NULL;
            cpar->ximag[i] = NULL;
        }
    }
}

void psnd_check_contour_limits(MBLOCK *mblk)
{
    CBLOCK *cpar = mblk->cpar_screen;
    cpar->par1 = PAR1;
    cpar->par2 = PAR2;

    cpar->ihamin = 1;
    cpar->ihamax = cpar->par1->nsiz;
    cpar->ihbmin = 1;
    cpar->ihbmax = cpar->par2->nsiz;

    cpar->ihamax = cpar->par1->irstop - cpar->par1->irstrt + 1;
    cpar->ihbmax = cpar->par2->irstop - cpar->par2->irstrt + 1;

    cpar->ihigha = min(cpar->ihamax,cpar->ihigha);
    cpar->ihighb = min(cpar->ihbmax,cpar->ihighb);
}

static void init_contour_param(MBLOCK *mblk, SBLOCK *spar, CBLOCK *cpar, PBLOCK *par1, 
                               PBLOCK *par2, DBLOCK *dat)
{
    int i;

    cpar->par1 = par1;
    cpar->par2 = par2;

    /*
     *....reset processing parameters for contouring
     */
    if (cpar->mode & CONTOUR_NEW_FILE)
        cpar->mode   = CONTOUR_PLOT + CONTOUR_NEW_FILE;
    else
        cpar->mode   = CONTOUR_PLOT;
    cpar->ihamin = 1;
    cpar->ihamax = par1->irstop - par1->irstrt + 1;
    cpar->ihbmin = 1;
    cpar->ihbmax = par2->irstop - par2->irstrt + 1;


    cpar->ilowa  = cpar->ihamin;
    cpar->ihigha = cpar->ihamax;
    cpar->ilowb  = cpar->ihbmin;
    cpar->ihighb = cpar->ihbmax;
    cpar->iadiv  = 1;
    cpar->ibdiv  = 1;
    if (cpar->maxcpr > 0) {
         i=(int) (sqrt((float)(par1->nsiz * par2->nsiz)/(float)(cpar->maxcpr))+0.5);
         if (i > 1) {
            cpar->iadiv=i;
            cpar->ibdiv=i;
            if (mblk->info->verbose)
                psnd_printf(mblk,
                    "  display reduction with x,y increments %4d %4d\n",
                    cpar->iadiv,cpar->ibdiv);
        }
    }
    cpar->doprocess = FALSE;
    /*
     *... levels
     */
    if (!cpar->once) 
        psnd_init_contour_par(spar, cpar);
}


/*
 * Set or Unset 2D contour mode
 */
void psnd_contour_mode(MBLOCK *mblk, int doit, CBLOCK *cpar)
{
    if (doit) {        
        if (mblk->info->plot_mode == PLOT_2D)
            return;
        mblk->info->plot_mode = PLOT_2D;
        /*
         * xdata is obsolete
         */
        cpar->xdata = (float*) malloc(sizeof(float) * MAXDAT);
        assert(cpar->xdata);
        cpar->bits  = (char*) malloc(sizeof(char) * 2 * MAXCPR);
        assert(cpar->bits);
        cpar->xcpr  = (float*) malloc(sizeof(float) * MAXCPR);
        assert(cpar->xcpr);
        init_contour_param(mblk,mblk->spar,cpar,PAR1,PAR2,DAT);
    }
    else {
        if (mblk->info->plot_mode == PLOT_1D)
            return;
        mblk->info->plot_mode = PLOT_1D;
        if (cpar->xdata)
            free(cpar->xdata);
        if (cpar->bits)
            free(cpar->bits);
        if (cpar->xcpr)
            free(cpar->xcpr);
        cpar->xdata	= NULL;
        cpar->bits	= NULL;
        cpar->xcpr	= NULL;
    }
    cpar->ial  = 0;
    cpar->iah  = 0;
    cpar->ibl  = 0;
    cpar->ibh  = 0;
    cpar->iadv = 0;
    cpar->ibdv = 0;
}



static int pause(void)
{
    static G_EVENT ui;
    int result;

    ui.keycode = 0;
    do {
        result = g_get_event(&ui);
    } while (result != G_KEYPRESS && result != G_BUTTON1PRESS && result !=
           G_COMMAND && result != G_WINDOWDESTROY);
    if (ui.keycode == 'q')
        return TRUE;
    return FALSE;
}

static int paperplot_contour(MBLOCK *mblk, int preview, int silent, FILE *outfile, 
              CONPLOTQ_TYPE *cpq, CBLOCK *cpar)
{
    int vp, obj, col;
    int  ixdbug = 0, nlevel, *levcol, done = FALSE, iclip;
    float *clevel;
    char *filename;
    int i,j,dest_id;
    int ilowa, ihigha, ilowb, ihighb;
    float xmin, xmax, ymin, ymax, xdnq, ydnq;
    float txmin, tymin, txmax, tymax;
    float xminptot, xmaxptot, yminptot, ymaxptot;
    int   iadiv = 1;
    int   ibdiv = 1;
    float xdiv  = (float) iadiv;
    float ydiv  = (float) ibdiv;
    DBLOCK *dat = DAT;
    SBLOCK *spar= mblk->spar;

    vp = spar[S_HARDCOPY].vp_id;
    obj= spar[S_HARDCOPY].obj_id;
    col= spar[S_HARDCOPY].color;
#ifdef _WIN32
    if (!(preview || (cpq->device == G_PRINTER))) {
#else
    if (!preview) {
#endif
        g_set_outfile(outfile);
        g_set_devicesize(cpq->device, cpq->dev_width, cpq->dev_height);
    }    
    iclip = g_get_clipping();
    xmax = cpq->xminv;
    xdnq = (cpq->xmaxv - cpq->xminv)/cpq->nqx;
    ydnq = (cpq->ymaxv - cpq->yminv)/cpq->nqy;
#ifdef _WIN32
    if (!preview && (cpq->device == G_PRINTER)) {
        if (g_open_device(G_PRINTER)== G_ERROR)
            return FALSE;
    }
#endif
    for (i=0;i<cpq->nqx && !done;i++) {
        float xminp, xmaxp, temp1, temp2;
        float xminp_store, xmaxp_store, xmin_store;

        xmin = xmax;
        xmax = xmin + xdnq;
        if (cpq->unitsx == UNIT_PPM) {
            temp1 = psnd_chan2ppm(xmin, cpar->par1->sfd, cpar->par1->swhold, 
                          cpar->par1->nsiz, cpar->par1->xref,
                          cpar->par1->aref, cpar->par1->bref, cpar->ihamin);
            temp2 = psnd_chan2ppm(xmax, cpar->par1->sfd, cpar->par1->swhold, 
                          cpar->par1->nsiz, cpar->par1->xref,
                          cpar->par1->aref, cpar->par1->bref, cpar->ihamin);
            xminptot = cpq->xminpv;
            xmaxptot = cpq->xmaxpv;
        }
        else if (cpq->unitsx == UNIT_HERTZ) {
            temp1 = psnd_chan2hz(xmin, cpar->par1->swhold, 
                          cpar->par1->nsiz, cpar->par1->xref,
                          cpar->par1->aref, cpar->par1->bref, cpar->ihamin);
            temp2 = psnd_chan2hz(xmax, cpar->par1->swhold, 
                          cpar->par1->nsiz, cpar->par1->xref,
                          cpar->par1->aref, cpar->par1->bref, cpar->ihamin);
            xminptot = cpq->xminhv;
            xmaxptot = cpq->xmaxhv;
        }
        else if (cpq->unitsx == UNIT_SEC) {
            temp1 = psnd_chan2sec(xmin, cpar->par1->swhold);
            temp2 = psnd_chan2sec(xmax, cpar->par1->swhold);
            xminptot = cpq->xminsv;
            xmaxptot = cpq->xmaxsv;
        }
        else {
            temp1 = xmin;
            temp2 = xmax;
            xminptot = cpq->xminv;
            xmaxptot = cpq->xmaxv;
        }
        xminp_store=temp1;
        xmaxp_store=temp2;
        ymax = cpq->yminv;
        for (j=0;j<cpq->nqy && !done;j++) {
            float yminp, ymaxp, tmp1, tmp2;
            float vx1,vx2,vy1,vy2;
            float width,height,hshift,scale;

            xminp=xminp_store;
            xmaxp=xmaxp_store;
            
            if (preview) {
                if (cpq->dev_width > 30)
                    scale = 20/sqrt(2);
                else
                    scale = 20;
                if (cpq->paper_orientation==0) {
                    /*
                     * Landscape
                     */
                    width  = cpq->dev_width;
                    height = cpq->dev_height;
                    hshift = 0.0;
                }
                else {
                    /*
                     * Portrait
                     */
                    height = cpq->dev_width;
                    width  = cpq->dev_height;
                    hshift = height - cpq->tymax - cpq->offy;
                }
            }
            ymin = ymax;
            ymax = ymin + ydnq;
            if (cpq->unitsy == UNIT_PPM) {
                temp1 = psnd_chan2ppm(ymin, cpar->par2->sfd, cpar->par2->swhold, 
                          cpar->par2->nsiz, cpar->par2->xref,
                          cpar->par2->aref, cpar->par2->bref, cpar->ihbmin);
                temp2 = psnd_chan2ppm(ymax, cpar->par2->sfd, cpar->par2->swhold, 
                          cpar->par2->nsiz, cpar->par2->xref,
                          cpar->par2->aref, cpar->par2->bref, cpar->ihbmin);
                yminptot = cpq->yminpv;
                ymaxptot = cpq->ymaxpv;
            }
            else if (cpq->unitsy == UNIT_HERTZ) {
                temp1 = psnd_chan2hz(ymin, cpar->par2->swhold, 
                          cpar->par2->nsiz, cpar->par2->xref,
                          cpar->par2->aref, cpar->par2->bref, cpar->ihbmin);
                temp2 = psnd_chan2hz(ymax, cpar->par2->swhold, 
                          cpar->par2->nsiz, cpar->par2->xref,
                          cpar->par2->aref, cpar->par2->bref, cpar->ihbmin);
                yminptot = cpq->yminhv;
                ymaxptot = cpq->ymaxhv;
            }
            else if (cpq->unitsy == UNIT_SEC) {
                temp1 = psnd_chan2sec(ymin, cpar->par2->swhold);
                temp2 = psnd_chan2sec(ymax, cpar->par2->swhold);
                yminptot = cpq->yminsv;
                ymaxptot = cpq->ymaxsv;
            }
            else {
                temp1 = ymin;
                temp2 = ymax;
                yminptot = cpq->yminv;
                ymaxptot = cpq->ymaxv;
            }
            yminp = temp1;
            ymaxp = temp2;
            if (!silent) {
                psnd_printf(mblk," plot   :%8d    loop: %8d %8d\n",i*cpq->nqy+j+1,i+1,j+1);
                psnd_printf(mblk," scales :\n");
                psnd_printf(mblk," cm     :%8.2f %8.2f %8.2f %8.2f\n",
                    cpq->cminx,cpq->cmaxx,cpq->cminy,cpq->tymax);
                psnd_printf(mblk," all:\n");
                psnd_printf(mblk,"        :%8.2f %8.2f %8.2f %8.2f\n",
                    cpq->xminv ,cpq->xmaxv ,cpq->yminv ,cpq->ymaxv);
                psnd_printf(mblk,"        :%8.2f %8.2f %8.2f %8.2f\n",
                    xminptot,xmaxptot,yminptot,ymaxptot);
                psnd_printf(mblk," current:\n");
                psnd_printf(mblk,"        :%8.2f %8.2f %8.2f %8.2f\n",
                    xmin ,xmax ,ymin ,ymax);
                psnd_printf(mblk,"        :%8.2f %8.2f %8.2f %8.2f\n",
                    xminp,xmaxp,yminp,ymaxp);
                psnd_printf(mblk,"\n");
            }
            ilowa  = round(xmin);
            ihigha = round(xmax);
            ilowb  = round(ymin);
            ihighb = round(ymax);
            /*
             * When printing to paper, the window coordinates specifiy
             * the size and the position of the window on the paper. When
             * printing to the screen in preview mode, this behaviour
             * is emulated by redefining the viewport coordinates
             */
            if (preview) {
                if (i==0 && j==0) {
                    dest_id = g_open_window(G_SCREEN, cpq->offx, cpq->offy, 
                               scale*width,
                               scale*height, 
                               "Preview: Contour Plot, anykey=next, q=quit", cpq->option);
                }
                else
                    g_newpage();
            }
            else {
                int penw ;
                penw = psnd_calc_ps_penwidth(cpq->pen_width);
                g_set_linewidth(penw);
                dest_id = g_open_window(cpq->device, cpq->offx, cpq->offy, 
                              cpq->cmaxx - cpq->offx, cpq->tymax - cpq->offy, 
                              "Contour Plot", cpq->option);
#ifdef _WIN32
                g_set_linewidth(penw);
#endif
           }
            g_delete_object(obj);
            g_open_object(obj);
            g_set_foreground(col);
            if (preview) 
                g_set_motif_realtextrotation(1);
            /*
             * xaxis
             */
            if (cpq->show_xaxis) {
                float vp0=0.0, title_start, axis_width;
                /*
                 * cm to ppm
                 */
                title_start = xminp + cpq->xstart_title *
                         (xmaxp-xminp)/(cpq->cmaxx - cpq->cminx);
                if (preview) {
                    if (cpq->show_yaxis)
                        vx1=(cpq->cloffx+cpq->offx)/width;
                    else
                        vx1=cpq->offx/width;
                    vx2=cpq->cmaxx/width;
                    vy1=(hshift+cpq->offy)/height;
                    vy2=(hshift+cpq->offy+cpq->cloffy)/height;
                }
                else {
                    if (cpq->show_yaxis)
                        vp0 = cpq->cloffx/(cpq->cmaxx - cpq->offx);
                    vx1=vp0;
                    vx2=1.0;
                    vy1=0.0;
                    vy2=cpq->cloffy/(cpq->tymax - cpq->offy);
                }
                /*
                 * If vy2 < 0 or vy1 > 1, plot is outside
                 * window, so abort
                 */
                if (vy2 > 0.0 && vy1 < 1.0) {
                    axis_width = fabs(xmaxp-xminp);
                    /*
                     * Suppose plot is wider than paper size:
                     * This results in right viewport coordinate > 1
                     * Set vpx2 to 1 and correct right world-coordinate
                     * instead
                     */
                    tymin = 0;
                    tymax = cpq->cloffy;
                    txmin = xminp;
                    txmax = xmaxp;
                    psnd_get_clipped_viewwin(&vx1, &vy1, &vx2, &vy2,
                                    &txmin, &tymin, &txmax, &tymax);
                    g_set_clipping(FALSE);
                    psnd_paperplot_xaxis(vp, cpq->xticks_autoscale,
                        cpq->axtxt, txmin, txmax, 
                        cpq->xticks_ppm_short, cpq->xticks_ppm_medium, 
                        cpq->xticks_ppm_long, cpq->xlabels_ppm,
                        cpq->xoff_axis, cpq->xoff_labels,
                        title_start, cpq->xoff_title,  
                        cpq->xticklen_short,cpq->xticklen_medium, 
                        cpq->xticklen_long, axis_width, cpq->cloffy, 
                        vx1, vy1, vx2, vy2);
                }
            }
            /*
             * y axis
             */
            if (cpq->show_yaxis) {
                float vp0=0.0, title_start, axis_height;
                /*
                 * cm to ppm
                 */
                title_start = yminp + cpq->ystart_title *
                         (ymaxp-yminp)/(cpq->cmaxy - cpq->cminy);
                if (preview) {
                    vx1=cpq->offx/width;
                    vx2=(cpq->offx+cpq->cloffx)/width;
                    if (cpq->show_xaxis)
                        vy1=(hshift+cpq->offy+cpq->cloffy)/height;
                    else
                        vy1=(hshift+cpq->offy)/height;
                    vy2=(hshift+cpq->cmaxy)/height;
                }
                else {
                    if (cpq->show_xaxis)
                        vp0 = cpq->cloffy/(cpq->tymax - cpq->offy);
                    vx1=0.0;
                    vx2=cpq->cloffx/(cpq->cmaxx - cpq->offx);
                    vy1=vp0;
                    vy2=(cpq->cmaxy - cpq->offy)/(cpq->tymax - cpq->offy);;
                }
                /*
                 * If vx2 < 0 or vx1 > 1, plot is outside
                 * window, so abort
                 */
                if (vx2 > 0.0 && vx1 < 1.0) {
                    axis_height = fabs(ymaxp-yminp);
                    /*
                     * Suppose plot is higher than paper size:
                     * This results in max viewport coordinate > 1
                     * Set vpy2 to 1 and correct top world-coordinate
                     * instead
                     */
                    txmin = 0;
                    txmax = cpq->cloffx;
                    tymin = yminp;
                    tymax = ymaxp;
                    psnd_get_clipped_viewwin(&vx1, &vy1, &vx2, &vy2,
                                    &txmin, &tymin, &txmax, &tymax);
                    g_set_clipping(FALSE);
                    psnd_paperplot_yaxis(vp, cpq->yticks_autoscale, 
                        cpq->aytxt, tymin, tymax,
                        cpq->yticks_ppm_short, cpq->yticks_ppm_medium,
                        cpq->yticks_ppm_long, cpq->ylabels_ppm, 
                        cpq->yoff_axis, cpq->yoff_labels, 
                        title_start, cpq->yoff_title, 
                        cpq->yticklen_short, cpq->yticklen_medium, 
                        cpq->yticklen_long, cpq->cloffx, axis_height,
                        vx1, vy1, vx2, vy2);
                }
            }
            if (preview) {
                vx1=cpq->cminx/width;
                vx2=cpq->cmaxx/width;
                vy1=(hshift+cpq->cminy)/height;
                vy2=(hshift+cpq->cmaxy)/height;
            }
            else {
                vx1=(cpq->cminx - cpq->offx)/(cpq->cmaxx - cpq->offx);
                vx2=1.0;
                vy1=(cpq->cminy - cpq->offy)/(cpq->tymax - cpq->offy);
                vy2=(cpq->cmaxy - cpq->offy)/(cpq->tymax - cpq->offy);
            }
            txmin = vx1;
            txmax = vx2;
            tymin = vy1;
            tymax = vy2;
            psnd_get_clipped_viewwin(&txmin, &tymin, &txmax, &tymax,
                                      &xminp, &yminp, &xmaxp, &ymaxp);
            /*
             * box
             */
            g_set_world(vp, xminp, yminp, xmaxp, ymaxp);
            g_set_viewport(vp,txmin, tymin, txmax, tymax);
            g_set_clipping(TRUE);
            if (cpq->ibox) 
                g_rectangle(xminp, yminp, xmaxp, ymaxp);
            /*
             * grid
             */
            if (cpq->igrid) 
                psnd_paperplot_grid(vp,  (cpq->igrid == 2),
                          (cpq->igrid == 2),
                          xminp, xmaxp, yminp, ymaxp, 
                          cpq->xgridlines_ppm, cpq->ygridlines_ppm);
            /*
             * Main Plot
             */
            txmin = xmin;
            txmax = xmax;
            tymin = ymin;
            tymax = ymax;
            psnd_get_clipped_viewwin(&vx1, &vy1, &vx2, &vy2,
                                      &txmin, &tymin, &txmax, &tymax);

            g_set_world(vp, txmin, tymin, txmax, tymax);
            g_set_viewport(vp, vx1, vy1, vx2, vy2);
            g_set_clipping(TRUE);

            clevel=cpar->clevel;
            levcol=cpar->levcol;
            nlevel=cpar->nlevel;
            cntr2d(mblk,
                    cpar,
                    cpar->xcpr,
                    cpar->bits,
                    cpar->maxcpr,
                    clevel,
                    levcol,
                    nlevel,
                    dat->finfo[INFILE].ifile,
                    cpar->xdata,
                    cpar->maxdat,
                    ilowa,
                    ihigha,
                    ilowb,
                    ihighb,
                    iadiv,
                    ibdiv);

            g_set_foreground(col);
        /*
         * Show Title
         */
        if (cpq->show_title) {
            int vp2 = vp+2;
            float pymin,pymax;
            if (preview) {
                vx1=cpq->cminx/width;
                vx2=cpq->cmaxx/width;
                vy1=(hshift+cpq->cmaxy)/height;
                vy2=(hshift+cpq->tymax)/height;
            }
            else {
                vx1=(cpq->cminx - cpq->offx)/(cpq->cmaxx - cpq->offx);
                vx2=1.0;
                vy1=(cpq->cmaxy - cpq->offy)/(cpq->tymax - cpq->offy);
                vy2=1.0;
            }
            pymin=0;
            pymax = cpq->tymax - cpq->lymax;
            if (vy1 < 1.0 && vy2 > 0.0) {
                float dumx1, dumx2;
                dumx1 = xmin;
                dumx2 = xmax;
                psnd_get_clipped_viewwin(&vx1, &vy1, &vx2, &vy2,
                            &dumx1, &pymin, &dumx2, &pymax);
                g_set_world(vp2, 0, 0, 100, 100);
                g_set_viewport(vp2, vx1,vy1,vx2,vy2);
/* g_rectangle(0, 0, 100, 100); */
                g_set_clipping(FALSE);
                g_moveto(0,30);
                g_label(cpq->plot_title);
            }
        }
            g_close_object(obj);
            g_call_object(obj);
            if (preview)
                if (pause())
                    /*
                     * abort
                     */
                    done = TRUE;
            if (!preview || (i>=cpq->nqx-1 && j>=cpq->nqy-1) || done)
                g_close_window(dest_id);
            if (preview) {
                static G_EVENT ui;
                while (g_peek_event() & G_WINDOWDESTROY)
                    g_get_event(&ui);
            }
            g_delete_object(obj);
        }
    }
    if (outfile)
        fclose(outfile);
#ifdef _WIN32
    if (!preview && (cpq->device == G_PRINTER))
        g_close_device(G_PRINTER);
#endif
    g_set_clipping(iclip);
    g_set_linewidth(1);
    return TRUE;
}


int psnd_au_paperplot_2d(MBLOCK *mblk, char *filename)
{
    FILE *outfile=NULL;

    if ((outfile = fopen(filename, "w+")) == NULL)
        return FALSE;
    if (mblk->info->win_id == G_ERROR) {
        g_init(0, NULL);
        psnd_init_colormap();
        psnd_init_objects(mblk);
    }
    psnd_process_contour_levels(mblk, mblk->cpar_hardcopy);
    psnd_process_color_levels(mblk, mblk->cpar_hardcopy);
    psnd_init_paper_plot(mblk->cpq_hardcopy);
    psnd_process_plot_settings(mblk->cpq_hardcopy, mblk->cpar_hardcopy);
    paperplot_contour(mblk,FALSE, TRUE, outfile, 
           mblk->cpq_hardcopy, mblk->cpar_hardcopy); 
    if (mblk->info->win_id != G_ERROR) {
        g_set_motif_realtextrotation(0);
        g_select_window(mblk->info->win_id);
    }
    else
        g_end();
    return TRUE;  
}

int psnd_paperplot_contour(MBLOCK *mblk, int preview)
{
    char *filename;
    FILE *outfile=NULL;

#ifdef _WIN32
    if (!(preview || (mblk->cpq_screen->device == G_PRINTER))) {
#else
    if (!preview) {
#endif
        filename = psnd_savefilename(mblk,"Output file for contour plot", "*");
        if (filename == NULL)
            return FALSE;
        if ((outfile = fopen(filename, "w+")) == NULL)
            return FALSE;
    }   
    paperplot_contour(mblk, preview, FALSE, outfile, 
        mblk->cpq_screen, mblk->cpar_screen); 
    g_set_motif_realtextrotation(0);
    g_select_window(mblk->info->win_id);
    mblk->cpar_screen->mode = CONTOUR_PLOT;
    return TRUE;  
}


int psnd_cp(MBLOCK *mblk, int mode)
{
    float x, y, xwldmi, xwldma, ywldmi, ywldma;
    float uxxmi,uxxma,uxymi,uxyma;
    int vp, obj, col;
    int  ixdbug = 0, nlevel, *levcol;
    float *clevel;
    int is_newpage = FALSE;
    SBLOCK *spar = mblk->spar;
    CBLOCK *cpar = mblk->cpar_screen;
    DBLOCK *dat  = DAT;

    vp = spar[S_CONTOUR].vp_id;
    obj= spar[S_CONTOUR].obj_id;
    col= spar[S_CONTOUR].color;
    g_set_background(spar[S_CONTOUR].color3);
    if (cpar->ipre != dat->pars[0]->ipre) {
        cpar->ipre = dat->pars[0]->ipre;
        if (mode & CONTOUR_BLOCK || mode & CONTOUR_BLOCK_NEW)
            mode = CONTOUR_BLOCK_NEW;
        else
            mode = CONTOUR_NEW_FILE;
    }

    xwldmi = (float)cpar->ilowa;
    xwldma = (float)cpar->ihigha;
    ywldmi = (float)cpar->ilowb;
    ywldma = (float)cpar->ihighb;

    psnd_getviewport3(mblk,vp,&uxxmi,&uxxma,&uxymi,&uxyma);
    if (mode != CONTOUR_PLOT_APPEND && !mblk->info->phase_2d) {
        spar[S_SCRATCH2D].show = FALSE;
        g_newpage();
        is_newpage = TRUE;
    }
    psnd_setworld(mblk,vp,xwldmi,xwldma,ywldmi,ywldma);

    psnd_getviewport3(mblk,vp,&uxxmi,&uxxma,&uxymi,&uxyma);
    if (mode == CONTOUR_PLOT_APPEND)
        g_append_object(obj);
    else {
        g_delete_object(obj);
        g_open_object(obj);
        psnd_setviewport3(mblk,vp,uxxmi,uxxma,uxymi,uxyma);
        g_clear_viewport();
        g_set_foreground(col);
    }

    if (mode == CONTOUR_PLOT_APPEND) {
        clevel=&(cpar->clevel[cpar->nlevel-1]);
        levcol=&(cpar->levcol[cpar->nlevel-1]);
        nlevel=1;
    }
    else {
        clevel=cpar->clevel;
        levcol=cpar->levcol;
        nlevel=cpar->nlevel;
    }

    if (mode & CONTOUR_BLOCK || mode & CONTOUR_BLOCK_NEW) {
        int iadiv = (int)((float)(cpar->ihigha-cpar->ilowa+1))/200.0;
        int ibdiv = (int)((float)(cpar->ihighb-cpar->ilowb+1))/200.0;

        if (mode == CONTOUR_BLOCK_NEW)
            cpar->mode = CONTOUR_BLOCK_NEW;
        blockplot(mblk,
           cpar->xcpr,
           cpar->mode,
           cpar->maxcpr,
           cpar->clevel,
           cpar->levcol,
           cpar->nlevel,
           dat->finfo[INFILE].ifile,
           cpar->xdata,
           cpar->maxdat,
           cpar->ilowa,
           cpar->ihigha,
           cpar->ilowb,
           cpar->ihighb,
           iadiv,
           ibdiv,
           cpar,
           dat);
         mode = cpar->mode = CONTOUR_BLOCK;
    }
    else if (mode == CONTOUR_STACK) {
        AXIS_UNIT_INFO aui;

        aui.xmin = xwldmi;
        aui.xmax = xwldma;
        aui.flag = PLOT_ALL;
        psnd_get_axis_units(mblk, vp, dat->isspec, &aui, 
                             spar, cpar, cpar->par1, cpar->par2, dat);
        if (aui.ixmode == 0) {
           aui.xmin = 0;
           aui.xmax = 0;
        }
        if (aui.iymode == 0) {
           aui.ymin = 0;
           aui.ymax = 0;
        }
        psnd_stackplot(mblk,
           cpar,
           cpar->xcpr,
           cpar->maxcpr,
           G_BLACK,
           G_WHITE,
           dat->finfo[INFILE].ifile,
           cpar->xdata,
           cpar->maxdat,
           cpar->ilowa,
           cpar->ihigha,
           cpar->ilowb,
           cpar->ihighb,
           cpar->lowest,
           cpar->highest,
           (cpar->mode == CONTOUR_STACK),
           aui.xmin,
           aui.xmax,
           aui.ymin,
           aui.ymax);
        cpar->mode = CONTOUR_STACK;
    }
    else {
        int iadiv, ibdiv;
        iadiv = cpar->iadiv;
        ibdiv = cpar->ibdiv;
        if (mode & CONTOUR_PLOT_1X1) {
            iadiv = ibdiv = 1;
        }

        cntr2d(mblk,
           cpar,
           cpar->xcpr,
           cpar->bits,
           cpar->maxcpr,
           clevel,
           levcol,
           nlevel,
           dat->finfo[INFILE].ifile,
           cpar->xdata,
           cpar->maxdat,
           cpar->ilowa,
           cpar->ihigha,
           cpar->ilowb,
           cpar->ihighb,
           iadiv,
           ibdiv);
           
         cpar->mode = CONTOUR_PLOT;
     }

    g_close_object(obj);
    g_call_object(obj);

    /*
     * If this is a new page AND if the integrate 2D
     * boxes are active, we have to re-activate this
     * 2D-integrate-object in the viewport history, by calling
     * g_call_object
     */
    if (is_newpage && spar[S_INT2D].show == TRUE)
        g_call_object(spar[S_INT2D].obj_id);

    /*
     * Just to make sure that after reading partial-planes
     * the full size is restored again
     */
    psnd_set_datasize(mblk, 
        cpar->ihigha - cpar->ilowa + 1, TRUE, dat);

    psnd_axis(mblk,vp,dat->isspec,PLOT_ALL,dat->ityp,
               xwldmi,xwldma,spar,cpar,cpar->par1,cpar->par2,dat);
    cpar->mode = mode;

    return TRUE;
}


/*
 * return color for level 'level'
 */
static int ifac(float level, float fac, float offset, int step)
{
    int i;
    for (i=0;i<step;i++) {
        if (offset > level)
            return i;
        offset *=  fac;
    }
    return step -1;
}

static void blockplot(MBLOCK *mblk, float *xcpr, int mode, int ncpr, float *clevel, int *levcol,
                      int nlevel, int inpfil, float *x, int maxdat, 
                      int ilowa, int ihigha, int ilowb, int ihighb, int iadiv,
                      int ibdiv, CBLOCK *cpar, DBLOCK *dat)
{
    int colors_used, color_offset = 16, maxcolor;
    float offset = fabs(clevel[0]);
    float delta, ffac, ymax;
    int ial,iah,ibl,ibh,nr,nr1,nc,nc1;
    static int iacpr,ibcpr;
    ial = min(ilowa,ihigha);
    iah = max(ilowa,ihigha);
    ibl = min(ilowb,ihighb);
    ibh = max(ilowb,ihighb);
    ial = max(ial,0);
    ibl = max(ibl,0);

    if (ncpr < 10 || maxdat < 10) {
        psnd_printf(mblk,"  error - blockplot buffer(s) too small %d %d\n",
            ncpr,maxdat);
        return;
    }

    iadiv = max(1,iadiv);
    ibdiv = max(1,ibdiv);
    do {
        nr=(iah-ial+1);
        nr1=nr/iadiv;
        nc=(ibh-ibl+1);
        nc1=nc/ibdiv;
        if ((nr1*nc1) <= ncpr) 
            break;
        ibh=ibh-(nc-1)/2;
    } while (1);

    /*
     * For a new file, get ymin and ymax
     */
    if (mode & CONTOUR_NEW_FILE) {
        /*
         * calculate rms noise to determine lowest level
         */
        float avg,rms;
        int ja1 = 1, ja2 = iah/20, jb1 = 1, jb2 = ibh/20;

        /*
         * First check if we have a truncated level (= noise set to zero). 
         * If so, the rmsnoise will probably also be zero, thus use this
         * truncation level instead
         */
        if (dat->finfo[INFILE].tresh_flag) {
            rms = dat->finfo[INFILE].tresh_levels[0];
        }
        else
            rmsn2d(inpfil,
               cpar->xdata,
               ja1,
               ja2,
               jb1,
               jb2,
               &avg,
               &rms,
               read2d,
               (void*) mblk);
        rms = max (fabs(rms), 1e3); 
        /*
         * first level is 5 x rms noise
         */
        offset = clevel[0] = 5 * fabs(rms);
        cpar->rmsnoise = rms;
        psnd_process_contour_levels(mblk, cpar);
        psnd_process_color_levels(mblk,cpar);
    }
    if ((mode & CONTOUR_NEW_FILE) || (mode & CONTOUR_BLOCK_NEW)) {
        /*
         * Get lowest and highest value
         */
        cpar->lowest = 0;
        cpar->highest = 0;
        do {
            int tmp,i,j;
            if (!( mode & CONTOUR_BLOCK) || 
                   cpar->ial != ial || cpar->iah != iah ||
                   cpar->ibl != ibl || cpar->ibh != ibh ||
                   cpar->iadv != iadiv || cpar->ibdv != ibdiv) {
                if (mulr2d(mblk,xcpr,
                           ncpr,
                           inpfil,
                           x,
                           maxdat,
                           ial,iah,ibl,ibh,
                           &iacpr,
                           &ibcpr,
                           iadiv,
                           ibdiv)==FALSE)
                       return;
                cpar->ial  = ial;
                cpar->iah  = iah;
                cpar->ibl  = ibl;
                cpar->ibh  = ibh;
                cpar->iadv = iadiv;
                cpar->ibdv = ibdiv;
            }
            for (j=0;j<ibcpr;j++) {
                int jj = iacpr * j;
                for (i=0;i<iacpr;i++) {
                    float xx = xcpr[i + jj];
                    cpar->lowest = min(cpar->lowest, xx);
                    cpar->highest = max(cpar->highest, xx);
                }
            }
            if (ibh == ihighb) 
                break;
            ibl=ibh;
            tmp = (ibh+nc-1);
            ibh=min(tmp,ihighb);
        } while (1);
        if (5*offset > fabs(cpar->highest) && 5*offset < fabs(cpar->lowest)) {
            float rms = max(fabs(cpar->highest),fabs(cpar->lowest));
            rms /= 20;
            offset = clevel[0] = 5 * fabs(rms);
            cpar->rmsnoise = rms;
            psnd_process_contour_levels(mblk, cpar);
            psnd_process_color_levels(mblk,cpar);
        }
        mode = CONTOUR_BLOCK;
    }

    /*
     * Determine the level factor
     */
    maxcolor    = g_get_palettesize()-color_offset;
    colors_used = maxcolor;
    if (cpar->plusmin == LEVELS_BOTH) {
        ymax = fabs(cpar->lowest);
        ymax = max(ymax, cpar->highest);
        colors_used = (colors_used * 3)/8;
    }
    else if (cpar->plusmin == LEVELS_NEGATIVE) 
        ymax = fabs(cpar->lowest);
    else
        ymax = fabs(cpar->highest);
    colors_used = max(2, colors_used);
    offset      = max(1.0, offset);
    ymax        = max(ymax, offset);
    delta       = (log10(ymax) - log10(offset))/((float)(colors_used-1));
    ffac        = pow(10.0, delta);

    /*
     * Plot
     */
    do {
        int tmp,i,j, iadiv2 = iadiv/2, ibdiv2= ibdiv/2;
        /*
         * Get data from disk if needed
         */
        if (!( mode & CONTOUR_BLOCK) || 
                   cpar->ial != ial || cpar->iah != iah ||
                   cpar->ibl != ibl || cpar->ibh != ibh ||
                   cpar->iadv != iadiv || cpar->ibdv != ibdiv) {
            if (mulr2d(mblk,xcpr,
                       ncpr,
                       inpfil,
                       x,
                       maxdat,
                       ial,iah,ibl,ibh,
                       &iacpr,
                       &ibcpr,
                       iadiv,
                       ibdiv) == FALSE)
                   return;
                cpar->ial  = ial;
                cpar->iah  = iah;
                cpar->ibl  = ibl;
                cpar->ibh  = ibh;
                cpar->iadv = iadiv;
                cpar->ibdv = ibdiv;
        }
        /*
         * Black background
         */
        g_set_foreground(G_BLACK);
        g_fillrectangle(ial, ibl, iah, ibh);
        for (j=0;j<ibcpr;j++) {
            float bb = 0.5 + ibl - 1 - ibdiv2 + j * ibdiv;
            for (i=0;i<iacpr;i++) {
                float aa, xx = xcpr[i + (iacpr * j)];
                int color;
                
                if (cpar->plusmin == LEVELS_BOTH) {
                    if (xx < 0) {
                        color = ifac(-xx, ffac, offset, colors_used);
                        if (color < 1)
                            continue;
                        color = maxcolor - color - 1;
                    }
                    else
                        color = ifac(xx, ffac, offset, colors_used);
                }
                else if (cpar->plusmin == LEVELS_NEGATIVE) {
                    color = ifac(-xx, ffac, offset, colors_used);
                    if (color < 1)
                        continue;
                    color = maxcolor - color - 1;
                }
                else 
                    color = ifac(xx, ffac, offset, colors_used);
                if (color < 1)
                    continue;
                g_set_foreground(color + color_offset - 1);
                aa = 0.5 + ial - 1 - iadiv2 + i * iadiv;
                g_fillrectangle(aa, bb, aa+iadiv, bb+ibdiv);
            }
        }
        if (ibh == ihighb) 
            break;
        ibl=ibh;
        tmp = (ibh+nc-1);
        ibh=min(tmp,ihighb);
    } while (1);
}




void NewLevel(int level, int *lcol)
{
    g_set_foreground(lcol[level]);
}


void Polyline (int n, void *data, float xoff, float yoff, float pxsc, float pysc)
{
    int i;
    LIST *list = (LIST*) data;
 
    g_moveto(pxsc * list[0].x + xoff,
             pysc * list[0].y + yoff);
    for (i=1;i<n;i++) 
        g_lineto(pxsc * list[i].x + xoff,
                 pysc * list[i].y + yoff);

}

int okarea(int i1, int i2, int j1, int j2)
{
     return ((i2 > i1) && (j2 > j1) && (i1 > 0) && (j1 > 0));
}


static void cntr2d(MBLOCK *mblk, CBLOCK *cpar, float *xcpr,  
            char *bits, int ncpr, float *clevel, 
            int *levcol, int nlevel, 
            int inpfil, float *x, int maxdat, 
            int ilowa, int ihigha, int ilowb, int ihighb,
            int iadiv, int ibdiv)
{
    int ial,iah,ibl,ibh,nr,nr1,nc,nc1;
    static int iacpr,ibcpr;

    ial = min(ilowa,ihigha);
    iah = max(ilowa,ihigha);
    ibl = min(ilowb,ihighb);
    ibh = max(ilowb,ihighb);
    ial = max(ial,0);
    ibl = max(ibl,0);

    psnd_printf(mblk," ... cntr2d - file %2d - area %6d %6d %6d %6d\n",
                 inpfil,ilowa,ihigha,ilowb,ihighb);

    if (!okarea (ilowa,ihigha,ilowb,ihighb))
        return;

    if (ncpr < 10 || maxdat < 10) {
        psnd_printf(mblk,"  error - cntr2d buffer(s) too small : %2d %2d\n",
            ncpr,maxdat);
        return;
    }

    iadiv = max(1,iadiv);
    ibdiv = max(1,ibdiv);

    do {
        int nr, nr1, nc1;
        
        nr  = (iah-ial+1);
        nr1 = nr/iadiv;
        nc  = (ibh-ibl+1);
        nc1 = nc/ibdiv;
        if ((nr1*nc1) <= ncpr) 
            break;
        ibh = ibh-(nc-1)/2;
    } while (1);

    do {

        psnd_printf(mblk," ... reading data - %6d %6d %6d %6d\n",
                        ial,iah,ibl,ibh);
        if (mulr2d(mblk,xcpr,
                   ncpr,
                   inpfil,
                   x,
                   maxdat,
                   ial,iah,ibl,ibh,
                   &iacpr,
                   &ibcpr,
                   iadiv,
                   ibdiv) == FALSE)
                return;
        cpar->ial  = ial;
        cpar->iah  = iah;
        cpar->ibl  = ibl;
        cpar->ibh  = ibh;
        cpar->iadv = iadiv;
        cpar->ibdv = ibdiv;
        psnd_printf(mblk," ... searching contours\n");

/*
        contour_plot(xcpr, iacpr,ibcpr, clevel, nlevel,
                  ial, ibl, (float)iadiv, (float)ibdiv, levcol, 
                 (void*) cpar, FALSE, Polyline, NewLevel);
*/               
        hpcntr(xcpr,bits,iacpr,ibcpr,clevel,levcol,nlevel, 
                  ial, ibl, (float) iadiv, (float) ibdiv,Polyline, NewLevel);
        if (ibh == ihighb) 
            break;
        ibl = ibh;
        ibh = min((ibh+nc-1),ihighb);
    } while (1);

}






