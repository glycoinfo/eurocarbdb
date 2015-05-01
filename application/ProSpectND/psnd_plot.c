/********************************************************************/
/*                             psnd_plot.c                          */
/*                                                                  */
/* Plot-functions                                                   */
/* 1998, Albert van Kuik                                            */
/********************************************************************/
#include <stdio.h>
#include <stdlib.h>
#include <ctype.h>
#include <string.h>
#include <math.h>
#include "genplot.h"
#include "psnd.h"
#include "mathtool.h"


void center_y(float *xmin, float *xmax)
{
    float mi,ma;
    
    mi = fabs(*xmin);    
    ma = fabs(*xmax);
    ma = max(mi,ma);
    *xmin = -ma;
    *xmax = ma;
}

float psnd_chan2ppm(float chan, float sp, float sw, int n, float xref, 
                     float aref, float bref, int irc)
{
    xref = psnd_scale_xref(xref, aref, bref, irc);
    if (n != 1 && sp != 0.0) 
        return (xref-chan)*(sw/((float)(n-1)))/sp;
    return 0.0;
}

float psnd_ppm2chan(float ppm, float sp, float sw, int n, float xref,
                     float aref, float bref, int irc)
{
    xref = psnd_scale_xref(xref, aref, bref, irc);
    if (sw != 0.0) 
        return (xref - (ppm * sp * ((float)(n - 1))/ sw));
    return 0.0;
}


float psnd_hz2chan(float hz, float sw, int n, float xref, float aref, 
                    float bref, int irc)
{
    xref = psnd_scale_xref(xref, aref, bref, irc);
    if (sw != 0.0) 
        return (xref - (hz * ((float)(n - 1))/ sw));
    return 0.0;
}

float psnd_chan2hz(float chan, float sw, int n, float xref, float aref, 
                    float bref, int irc)
{
    xref = psnd_scale_xref(xref, aref, bref, irc);
    if (n != 1) 
        return (xref-chan)*(sw/((float)(n-1)));
    return 0.0;
}

float psnd_sec2chan(float sec, float sw)
{
    return sec * sw + 1;
}

float psnd_chan2sec(float chan, float sw)
{
    if (sw != 0.0)
        return (chan-1.0)/sw;
    return 0.0;
}

static int xgraph(MBLOCK *mblk, float *x, int n1, int n2, 
                  float lowest, float highest, float *dxy,
                  int vp_id, int vp_id_master, int *vp_id_block_master, 
                  int obj, int col, int auto_scale, 
                  int swap, int append, int clear, int keep_open)
{
    float yc1, yl1, yh1;
    float wx1, wx2, wy1, wy2;
    float xc, xd, xe, xmi,xma;
    float yc, yd, ye,  yl, yh;
    float border_space = 0.05;
    int id=1,i;
    static float store_x1, store_x2, store_y1, store_y2;

    if (n1 == n2)
        n2++;
    xmi = (float) n1;
    xma = (float) n2;
    if (clear && (auto_scale == AUTO_SCALE_ON || auto_scale == AUTO_SCALE_FULL)) {
        store_x1 = xmi;
        store_x2 = xma;
    }
    else if (auto_scale == AUTO_SCALE_COPY) {
        xmi = store_x1;
        xma = store_x2;
    }
    /*
     * First Translate
     */
    xd = (xma - xmi) * dxy[X_MOVE];
    xmi -= xd;
    xma -= xd;
    /*
     * Then scale from middle
     */
    xc = AXIS_SCALE(dxy[X_SCALE]);
    xe = (xmi+xma)/2.0;
    xmi = (xmi - xe) * xc + xe;
    xma = (xma - xe) * xc + xe;
    if (xmi == xma)
        xma++;
    if (vp_id_block_master)
        vp_id_master = *vp_id_block_master;
    if (auto_scale == AUTO_SCALE_ON || auto_scale == AUTO_SCALE_FULL) {

        if (vp_id_master && auto_scale != AUTO_SCALE_FULL) {
            if (!psnd_get_master_world(mblk, vp_id_master, &wx1, &yl, &wx2, &yh))
                minmax(x+(n1-1),n2,&yl,&yh);
            psnd_set_master_world(mblk, vp_id, (float) n1, yl, (float) n2, yh);
/*center_y(&yl,&yh);*/
        }
        else {
            if (auto_scale == AUTO_SCALE_FULL) {
                yh = highest;
                yl = lowest;
            }
            else
                minmax(x+(n1-1),n2,&yl,&yh);
/*center_y(&yl,&yh);*/
            psnd_set_master_world(mblk, vp_id, (float) n1, yl, (float) n2, yh);
        }
        if ((yh - yl) == 0) {
            yh += 1;
            yl -= 1;
        }
        yl1 = yl;
        yh1 = yh;
        if (clear) {
            store_y1 = yl1;
            store_y2 = yh1;
        }
        /*
         * First scale from 0
         */
        yc =  AXIS_SCALE(dxy[Y_SCALE]); 
        yl1 *= yc; 
        yh1 *= yc;
        /*
         * Then Translate
         */
        yd  = (yh1 - yl1) * dxy[Y_MOVE];
        yl1 = yl1-yd;
        yh1 = yh1-yd;
        ye  = (yh1 - yl1) * border_space;
        yl1 -= ye;
        yh1 += ye;
    }
    else  if (auto_scale == AUTO_SCALE_OFF_XY) 
        g_get_world(vp_id, &xmi, &yl1, &xma, &yh1);
    else if (auto_scale == AUTO_SCALE_COPY) {
        wx1 = store_x1;
        wx2 = store_x2;
        yl1 = store_y1;
        yh1 = store_y2;
        /*
         * First scale from 0
         */
        yc =  AXIS_SCALE(dxy[Y_SCALE]); 
        yl1 *= yc; 
        yh1 *= yc;
        /*
         * Then Translate
         */
        yd  = (yh1 - yl1) * dxy[Y_MOVE];
        yl1 = yl1-yd;
        yh1 = yh1-yd;
        ye  = (yh1 - yl1) * border_space;
        yl1 -= ye;
        yh1 += ye;        
    }
    else if (vp_id_master) {
        /*
        g_get_world(vp_id_master, &wx1, &yl1, &wx2, &yh1);
        */
        xmi = store_x1;
        xma = store_x2;
    }
    else 
        g_get_world(vp_id, &wx1, &yl1, &wx2, &yh1);

    if (swap) {
        /*
         * Rotate the 1D plot (for display on top of contour plot)
         */
        wy1 = xmi;
        wy2 = xma;
        if (auto_scale) {
            wx2 = yl1;
            wx1 = yh1;
        }
    }
    else {
        wx1 = xmi;
        wx2 = xma;
        wy1 = yl1;
        wy2 = yh1;
    }
    if (auto_scale == AUTO_SCALE_OFF_XY || auto_scale == AUTO_SCALE_OFF)
        /*
         * Set the world coordinates that you got before
         * = do nothing
         */
        g_set_world(vp_id, wx1, wy1, wx2, wy2);
    else
        psnd_setworld(mblk,vp_id,wx1,wx2,wy1,wy2);
    if (append) {
        g_append_object(obj);
    }
    else {
        g_delete_object(obj);
        g_open_object(obj);
    }
    psnd_set_groupviewport(mblk,vp_id);
    if (clear && !append)
        g_clear_viewport();
    g_set_foreground(col);
    if (swap) {
        g_moveto(x[n1-1],(float)n1);
        for (i=n1;i<n2;i++)
            g_lineto(x[i],(float)(i+1));
    }
    else {
        g_moveto((float)n1,x[n1-1]);
        for (i=n1;i<n2;i++)
            g_lineto((float)(i+1),x[i]);
    }
    if (!keep_open) {
        g_close_object(obj);
        g_call_object(obj);
    }
    return TRUE;
}


/*
 * Plot 1D spectrum in 2D phase block
 */
void psnd_plot1d_phase(MBLOCK *mblk, int s_id, int vp_code,
                       int isize, float *xreal,int auto_scale)
{
    int n1,n2;
    float dxy[] = { 0.0, 0.0, 0.0, 0.0 };

    n1=1;
    n2=isize;

    xgraph(mblk, 
           xreal,
           n1,
           n2,
           0,
           0,
           dxy,
           mblk->spar[s_id].vp_id,
           mblk->spar[s_id].vp_id_master,
           mblk->spar[s_id].vp_id_block_master,
           mblk->spar[s_id].obj_id,
           mblk->spar[s_id].color,
           auto_scale,
           FALSE,
           FALSE,
           TRUE,
           FALSE);
}

/*
 * Plot 1D spectrum on top of contour spectrum
 */
void psnd_plot1d_in_2d(MBLOCK *mblk, int swap, int extra_marker, int both)
{
    int  id = 0, color, x1, x2, s_id, iscale;
    float *dxyp, *xreal, dxy[] = { 0.0, 0.0, 0.0, 0.0 };
    SBLOCK *spar = mblk->spar;
    CBLOCK *cpar = mblk->cpar_screen;
    DBLOCK *dat  = DAT;

    if (cpar->mode & CONTOUR_BLOCK)
        color = spar[S_REAL].color2;
    else
        color = spar[S_REAL].color;
    if (mblk->info->auto_scale_2d)
        iscale = mblk->info->auto_scale;
    else
        iscale = AUTO_SCALE_FULL;
    dxyp = dxy;
    if (swap==1 || both) {
        int isize = cpar->ihbmax - cpar->ihbmin + 1;
        int nsiz  = dat->pars[1]->irstop - dat->pars[1]->irstrt + 1; 
        x1 = cpar->ilowb;
        x2 = cpar->ihighb;
        if (nsiz != isize) {
            float dx = (float) nsiz / (float) isize;
            x1 = round(dx * x1);
            x2 = round(dx * x2);
        }
        s_id = S_REALSWAP;
        if (!both) {
            spar[S_REAL].show = FALSE;
            g_set_objectstatus(spar[S_REAL].obj_id, G_SLEEP);
        }
        if (spar[S_REAL].disable_auto_scale_2d)
            dxyp = spar[S_REAL].dxy;
        spar[s_id].show = TRUE;
        if (both)
            xreal = dat->work1;
        else
            xreal = dat->xreal;
        xgraph(mblk,
               xreal,
           x1,
           x2,
           cpar->lowest,
           cpar->highest,
           dxyp,
           spar[s_id].vp_id,
           spar[s_id].vp_id_master,
           spar[s_id].vp_id_block_master,
           spar[s_id].obj_id,
           color,
           iscale,
           TRUE,
           FALSE,
           FALSE,
           FALSE);
    }
    if (swap != 1 || both) {
        int isize = cpar->ihamax - cpar->ihamin + 1;
        x1 = cpar->ilowa;
        x2 = cpar->ihigha;
        if (dat->isize != isize) {
            float dx = (float) dat->isize / (float) isize;
            x1 = round(dx * x1);
            x2 = round(dx * x2);
        }
        s_id = S_REAL;
        if (!both) {
            spar[S_REALSWAP].show = FALSE;
            g_set_objectstatus(spar[S_REALSWAP].obj_id, G_SLEEP);
        }
        if (spar[S_REAL].disable_auto_scale_2d)
            dxyp = spar[S_REAL].dxy;
        spar[s_id].show = TRUE;
        xgraph(mblk,
               dat->xreal,
           x1,
           x2,
           cpar->lowest,
           cpar->highest,
           dxyp,
           spar[s_id].vp_id,
           spar[s_id].vp_id_master,
           spar[s_id].vp_id_block_master,
           spar[s_id].obj_id,
           color,
           iscale,
           FALSE,
           FALSE,
           FALSE,
           FALSE);
    }
    /*
     * Draw marker line
     */
    if (cpar->mode == CONTOUR_BLOCK)
        color = spar[S_2DMARKER].color2;
    else
        color = spar[S_2DMARKER].color;
    g_delete_object(spar[S_2DMARKER].obj_id);
    g_open_object(spar[S_2DMARKER].obj_id);
    psnd_set_groupviewport(mblk,spar[S_2DMARKER].vp_id);
    g_set_foreground(color);
    g_set_linestyle(G_SHORT_DASHED);
    if (swap!=0 || both) {
        g_moveto((float)cpar->par1->key,(float)cpar->ilowb);
        g_lineto((float)cpar->par1->key,(float)cpar->ihighb);
    }
    if (swap!=1 || both) {
        g_moveto((float)cpar->ilowa,(float)cpar->par2->key);
        g_lineto((float)cpar->ihigha,(float)cpar->par2->key);
    }
    if (extra_marker > 0) {
        if (swap==1) {
            g_moveto((float)extra_marker,(float)cpar->ilowb);
            g_lineto((float)extra_marker,(float)cpar->ihighb);
        }
        else if (swap==2) {
            g_set_linestyle(G_LONG_DASHED);
            g_moveto((float)extra_marker,(float)cpar->ilowb);
            g_lineto((float)extra_marker,(float)cpar->ihighb);
        }
        else {
            g_moveto((float)cpar->ilowa,(float)extra_marker);
            g_lineto((float)cpar->ihigha,(float)extra_marker);
        }
    }
    g_set_linestyle(G_SOLID);
    g_close_object(spar[S_2DMARKER].obj_id);
    g_call_object(spar[S_2DMARKER].obj_id);
}


void psnd_get_axis_units(MBLOCK *mblk, int vp_id, int isspec, AXIS_UNIT_INFO *aui, 
    SBLOCK *spar, CBLOCK *cpar, PBLOCK *par, PBLOCK *par2, DBLOCK *dat)
{
    float wx1,wx2;
    int ysize, xsize, modex, modey;


    xsize = aui->xmax - aui->xmin+1;
    psnd_getmaxworld(mblk,vp_id,
		   &wx1, &wx2, &aui->ymin, &aui->ymax);
    aui->xmin = wx1;
    aui->xmax = wx2;
    aui->is_integerx = FALSE;
    aui->is_integery = FALSE;
    if (mblk->info->phase_2d)
        aui->flag = PLOT_BOX;

    if (aui->flag & PLOT_XAXIS || aui->flag & PLOT_GRID) {
        int irc  = dat->irc;
        /*
        float sw = dat->sw;
        */
        float sw; 
        if (mblk->info->plot_mode != PLOT_2D)
            sw = dat->sw;
        else
            sw = par->swhold * (xsize-1)/ (par->nsiz-1);
        modex = spar[S_AXISX_MARK].show;
        if (modex == AXIS_UNITS_AUTOMATIC) {
            if (isspec)
                modex = AXIS_UNITS_PPM;
            else
                modex = AXIS_UNITS_SECONDS;
        }
        switch (modex) {
        case AXIS_UNITS_CHANNELS:
            aui->ixmode=4;
            aui->is_integerx = TRUE;
            strcpy(aui->xtext, "#");
            break;
        case AXIS_UNITS_HERTZ:
            aui->ixmode=4;
            strcpy(aui->xtext, "Hz");
            aui->xmin=psnd_chan2ppm(aui->xmin,1.0, sw,
                xsize, par->xref,  par->aref,  par->bref, irc);
            aui->xmax=psnd_chan2ppm(aui->xmax,1.0, sw,
                xsize, par->xref,  par->aref,  par->bref, irc);
            break;
        case AXIS_UNITS_PPM:
            aui->ixmode=4;
            strcpy(aui->xtext, "ppm");
            aui->xmin=psnd_chan2ppm(aui->xmin, par->sfd, sw,
                xsize, par->xref, par->aref,  par->bref, irc);
            aui->xmax=psnd_chan2ppm(aui->xmax, par->sfd, sw,
                xsize, par->xref, par->aref,  par->bref, irc);
            break;
        case AXIS_UNITS_SECONDS:
            aui->ixmode=4;
            strcpy(aui->xtext, "sec");
            aui->xmin=(aui->xmin-1.0)/par->swhold;
            aui->xmax=(aui->xmax-1.0)/par->swhold;
            break;
        default:
            aui->ixmode=0;
            strcpy(aui->xtext, " ");
            break;
        }
    }

    if (aui->flag & PLOT_YAXIS || aui->flag & PLOT_GRID) {
        if (mblk->info->plot_mode == PLOT_2D && par2 != NULL) {
            int irc = par2->irstrt;
            float sw;
            ysize = par2->irstop - par2->irstrt + 1;
            sw = par2->swhold * (ysize-1)/ (par2->nsiz-1);
            modey = spar[S_AXISY_MARK].show;
            if (modey == AXIS_UNITS_AUTOMATIC) {
                if (par2->isspec)
                    modey = AXIS_UNITS_PPM;
                else
                    modey = AXIS_UNITS_SECONDS;
            }
            switch (modey) {
            case AXIS_UNITS_CHANNELS:
                aui->iymode=4;
                aui->is_integery = TRUE;
                strcpy(aui->ytext, "#");
                break;
            case AXIS_UNITS_HERTZ:
                aui->iymode=4;
                strcpy(aui->ytext, "Hz");
                strcpy(aui->xtext, "Hz");
                aui->ymin=psnd_chan2ppm(aui->ymin,1.0, sw,
                    ysize, par2->xref, par2->aref,  par2->bref, irc);
                aui->ymax=psnd_chan2ppm(aui->ymax,1.0, sw,
                    ysize, par2->xref, par2->aref,  par2->bref, irc);
                break;
            case AXIS_UNITS_PPM:
                aui->iymode=4;
                strcpy(aui->ytext, "ppm");
                aui->ymin=psnd_chan2ppm(aui->ymin, par2->sfd, sw,
                    ysize, par2->xref, par2->aref,  par2->bref, irc);
                aui->ymax=psnd_chan2ppm(aui->ymax, par2->sfd, sw,
                    ysize, par2->xref, par2->aref,  par2->bref, irc);
                break;
            case AXIS_UNITS_SECONDS:
                aui->iymode=4;
                strcpy(aui->ytext, "sec");
                aui->ymin=(aui->ymin-1.0)/sw;
                aui->ymax=(aui->ymax-1.0)/sw;
                break;
            default:
                aui->iymode=0;
                strcpy(aui->ytext, " ");
                break;
            }
        }
        else {
            if (spar[S_AXISXY_MARK].show == AXIS_UNITS_CHANNELS ) {
                aui->iymode=4;
                strcpy(aui->ytext," ");
            }
            else {
               aui->iymode=0;
               strcpy(aui->ytext," ");
            }
        }
    }

}


void psnd_axis(MBLOCK *mblk, int vp_id, int isspec, int flag, int dimension, 
                float xmin, float xmax, SBLOCK *spar, CBLOCK *cpar, 
                PBLOCK *par, PBLOCK *par2, DBLOCK *dat)
{
    float uxxmi,uxxma,uxymi,uxyma;
    AXIS_UNIT_INFO aui;

    aui.xmin = xmin;
    aui.xmax = xmax;
    aui.flag = flag;
    psnd_get_axis_units(mblk, vp_id, isspec, &aui, spar, cpar, par, par2,dat);

    if (dimension > 1) {
        char *p = aui.xtext + strlen(aui.xtext);
        sprintf(p, " [%d]", par->icrdir);
        if (mblk->info->plot_mode == PLOT_2D && par2 != NULL) {
            p = aui.ytext + strlen(aui.ytext);
            sprintf(p, "[%d]", par2->icrdir);
        }
    }
    
    if (aui.flag & PLOT_BOX) {
        psnd_getviewport3(mblk,spar[S_BOX].vp_id,&uxxmi,&uxxma,&uxymi,&uxyma);
        if (spar[S_BOX].show) {
            psnd_link_viewport(mblk,spar[S_BOX].vp_id, VP_MAIN);
            psnd_box(mblk, spar[S_BOX].vp_id, 
               uxxmi,uxxma,uxymi,uxyma,
               spar[S_BOX].show,
               mblk->info->phase_2d,
               spar[S_BOX].obj_id,
               spar[S_BOX].color);
        }
    }

    if (aui.flag & PLOT_XAXIS) {
        psnd_getviewport3(mblk,spar[S_BOX].vp_id,&uxxmi,&uxxma,&uxymi,&uxyma);
        if (spar[S_AXISX].show)
            psnd_xaxis(mblk,spar[S_AXISX].vp_id,
                 uxxmi,uxxma,uxymi,uxyma,
                 aui.xmin,aui.xmax,
                 aui.is_integerx,
                 spar[S_BOX].show,
                 aui.ixmode,
                 aui.xtext,
                 spar[S_AXISX].obj_id,
                 spar[S_AXISX].color);
    }

    if (aui.flag & PLOT_YAXIS) {
        if (spar[S_AXISY].show)
            psnd_yaxis(mblk,spar[S_AXISY].vp_id, 
                 uxxmi,uxxma,uxymi,uxyma,
                 aui.ymin,aui.ymax,
                 aui.is_integery,
                 spar[S_BOX].show,
                 aui.iymode,
                 aui.ytext,
                 spar[S_AXISY].obj_id,
                 spar[S_AXISY].color);
    }
    if (aui.flag & PLOT_GRID) {
        if (spar[S_GRID].show)
            psnd_grid(mblk,spar[S_GRID].vp_id, 
                  aui.xmin,aui.xmax,aui.ymin,aui.ymax,
                  spar[S_GRID].show, spar[S_GRID].show,
                  spar[S_GRID].obj_id, spar[S_GRID].color);
    }
}

/*
 * Plot all the 1D stuff for one block
 */
static int plot_full_block(MBLOCK *mblk, float *xreal,float *ximg, float *buffa, float *buffb, 
             float *baseln, float *win, int n1, int n2, int done, int auto_scale,
             SBLOCK *spar, PBLOCK *par)
{
    if (spar[S_REAL].show) {
        psnd_link_viewport(mblk,spar[S_REAL].vp_id, VP_MAIN);
        xgraph(mblk,xreal,n1,n2, 0,0,
            spar[S_REAL].dxy,
            spar[S_REAL].vp_id,
            spar[S_REAL].vp_id_master,
            spar[S_REAL].vp_id_block_master,
            spar[S_REAL].obj_id,
            spar[S_REAL].color,
            auto_scale,
            FALSE,
            mblk->info->append_plot,
            !done,
            FALSE);
        done = TRUE;
    }
    if (spar[S_IMAG].show) {
        psnd_link_viewport(mblk,spar[S_IMAG].vp_id, VP_MAIN);
        xgraph(mblk,ximg,n1,n2,  0,0,
            spar[S_IMAG].dxy,
            spar[S_IMAG].vp_id,
            spar[S_IMAG].vp_id_master,
            spar[S_IMAG].vp_id_block_master,
            spar[S_IMAG].obj_id,
            spar[S_IMAG].color,
            auto_scale,
            FALSE,
            mblk->info->append_plot,
            !done,
            FALSE);
        done = TRUE;
    }
    if (spar[S_BUFR1].show) {
        psnd_link_viewport(mblk,spar[S_BUFR1].vp_id, VP_MAIN);
        xgraph(mblk,buffa,n1,n2,  0,0,
            spar[S_BUFR1].dxy,
            spar[S_BUFR1].vp_id,
            spar[S_BUFR1].vp_id_master,
            spar[S_BUFR1].vp_id_block_master,
            spar[S_BUFR1].obj_id,
            spar[S_BUFR1].color,
            auto_scale,
            FALSE,
            FALSE,
            !done,
            FALSE);
        done = TRUE;
    }
    if (spar[S_BUFR2].show) {
        psnd_link_viewport(mblk,spar[S_BUFR2].vp_id, VP_MAIN);
        xgraph(mblk,buffb,n1,n2,  0,0,
            spar[S_BUFR2].dxy,
            spar[S_BUFR2].vp_id,
            spar[S_BUFR2].vp_id_master,
            spar[S_BUFR2].vp_id_block_master,
            spar[S_BUFR2].obj_id,
            spar[S_BUFR2].color,
            auto_scale,
            FALSE,
            FALSE,
            !done,
            FALSE);
        done = TRUE;
    }
    if (spar[S_WINDOW].show) {
        int auto_scale2 = AUTO_SCALE_FULL;
        if (mblk->info->auto_scale == AUTO_SCALE_ON)
            auto_scale2 = AUTO_SCALE_FULL;
        else
            auto_scale2 = mblk->info->auto_scale;
        psnd_link_viewport(mblk,spar[S_WINDOW].vp_id, VP_MAIN);
        xgraph(mblk,win,n1,n2,  0.0, 1.0,
            spar[S_WINDOW].dxy,
            spar[S_WINDOW].vp_id,
            spar[S_WINDOW].vp_id_master,
            spar[S_WINDOW].vp_id_block_master,
            spar[S_WINDOW].obj_id,
            spar[S_WINDOW].color,
            auto_scale2,
            FALSE,
            FALSE,
            !done,
            TRUE);
        /*
         * Draw lines at top and bottom of window
         */
        g_set_foreground(spar[S_WINDOW].color2);
        g_moveto((float)n1,0);
        g_lineto((float)n2,0);
        g_moveto((float)n1,1);
        g_lineto((float)n2,1);
        g_close_object(spar[S_WINDOW].obj_id);
        g_call_object(spar[S_WINDOW].obj_id);
        done = TRUE;
    }
    if (spar[S_BASELN].show) {
        xgraph(mblk,baseln,n1,n2,  0,0,
            spar[S_BASELN].dxy,
            spar[S_BASELN].vp_id,
            spar[S_BASELN].vp_id_master,
            spar[S_BASELN].vp_id_block_master,
            spar[S_BASELN].obj_id,
            spar[S_BASELN].color,
            auto_scale,
            FALSE,
            FALSE,
            FALSE,
            FALSE);
        done = TRUE;
    }
    return done;
    
}

static void do_plot_1d(MBLOCK *mblk, int dim)
{
    int i, done;
    int auto_scale;
    int xleft,xright;
    DBLOCK *dat;
    PBLOCK *par;
    SBLOCK *spar;

    auto_scale = mblk->info->auto_scale;
    i      = mblk->info->block_id;
    done   = FALSE;
    dat    = mblk->dat[i];
    par    = (mblk->par[i]+dim);
    spar   = mblk->spar_block[i];
    xleft  = 1;
    xright = dat->isize;

    done = plot_full_block(mblk,
                      dat->xreal,
                      dat->ximag,
                      dat->xbufr1,
                      dat->xbufr2,
                      dat->baseln,
                      dat->window,
                      xleft,
                      xright,
                      done,
                      mblk->info->auto_scale,
                      spar,
                      par);

    if (/*i == mblk->info->block_id &&*/ mblk->info->plot_mode == PLOT_1D /* && done */) 
            psnd_axis(mblk,psnd_get_vp_id(mblk), dat->isspec, PLOT_ALL, dat->ityp, 
                       xleft, xright, spar, mblk->cpar_screen, par, NULL, dat);
        
    for (i=0;i<mblk->info->max_block;i++) {
        if (i == mblk->info->block_id)
            continue;
        dat = mblk->dat[i];
        par = (mblk->par[i]+dim);
        spar = mblk->spar_block[i];
        xleft  = 1;
        xright = dat->isize;
        done = plot_full_block(mblk,
                      dat->xreal,
                      dat->ximag,
                      dat->xbufr1,
                      dat->xbufr2,
                      dat->baseln,
                      dat->window,
                      xleft,
                      xright,
                      done,
                      AUTO_SCALE_COPY,
                      spar,
                      par);

    }
}


void psnd_plot(MBLOCK *mblk, int erase,int dim)
{
    CBLOCK *cpar = mblk->cpar_screen;

    g_set_background(mblk->spar[S_CONTOUR].color3);
    if (mblk->info->plot_mode == PLOT_2D) {
        if (!mblk->info->phase_2d && mblk->spar[S_REAL].disable_auto_scale_2d) {
            psnd_plot1d_in_2d(mblk, (PAR->icrdir!=DAT->access[0]),
                               0,FALSE);
            g_clear_viewport();
            g_plotall();
            return;
        }
        if (mblk->spar[S_PHASE1].show) 
            psnd_plot1d_phase(mblk, S_PHASE1, VP_PHASE1, 
                cpar->phase_size, cpar->xreal[0],AUTO_SCALE_OFF_XY);
        if (mblk->spar[S_PHASE2].show) 
            psnd_plot1d_phase(mblk, S_PHASE2, VP_PHASE2, 
                cpar->phase_size, cpar->xreal[1],AUTO_SCALE_OFF_XY);
        if (mblk->spar[S_PHASE3].show) 
            psnd_plot1d_phase(mblk, S_PHASE3, VP_PHASE3, 
                cpar->phase_size, cpar->xreal[2],AUTO_SCALE_OFF_XY);
        return;
    }
    if (erase)
        g_newpage();
    do_plot_1d(mblk, dim);
}

/*
 * Fast 1d screen update for interactive phase correction
 */
void psnd_phaseplot1d(MBLOCK *mblk)
{
    int n1,n2, done;
    SBLOCK *spar = mblk->spar;
    DBLOCK *dat  = DAT;

    n1   = 1;
    n2   = dat->isize;
    done = FALSE;
    g_set_background(spar[S_CONTOUR].color3);
    if (spar[S_REAL].show) {
        psnd_link_viewport(mblk,spar[S_REAL].vp_id, VP_MAIN);
        xgraph(mblk,dat->xreal,n1,n2, 0,0,
            spar[S_REAL].dxy,
            spar[S_REAL].vp_id,
            spar[S_REAL].vp_id_master,
            spar[S_REAL].vp_id_block_master,
            spar[S_REAL].obj_id,
            spar[S_REAL].color,
            AUTO_SCALE_OFF_XY,
            FALSE,
            FALSE,
            !done,
            FALSE);
        done = TRUE;
    }
    if (spar[S_IMAG].show) {
        psnd_link_viewport(mblk,spar[S_IMAG].vp_id, VP_MAIN);
        xgraph(mblk,dat->ximag,n1,n2,  0,0,
            spar[S_IMAG].dxy,
            spar[S_IMAG].vp_id,
            spar[S_IMAG].vp_id_master,
            spar[S_IMAG].vp_id_block_master,
            spar[S_IMAG].obj_id,
            spar[S_IMAG].color,
            AUTO_SCALE_OFF_XY,
            FALSE,
            FALSE,
            !done,
            FALSE);
    }
}

/*
 * Plot one array (s_id) from n1 to n2, set append mode
 */
void psnd_plot_one_array(MBLOCK *mblk, float *xdata, int n1, int n2, int s_id,
              int scale, int append, SBLOCK *spar)
{
    if (spar[s_id].show) {
        psnd_link_viewport(mblk,spar[s_id].vp_id, VP_MAIN);
        xgraph(mblk,xdata,n1,n2, 0, 0,
            spar[s_id].dxy,
            spar[s_id].vp_id,
            spar[s_id].vp_id_master,
            spar[s_id].vp_id_block_master,
            spar[s_id].obj_id,
            spar[s_id].color,
            scale,
            FALSE,
            append,
            FALSE,
            FALSE);
    }
}

static void plotaxis(MBLOCK *mblk, int flag, PBLOCK *par)
{
    int xleft=1,xright = DAT->isize;
    int vp_id = psnd_get_vp_id(mblk);

    if (mblk->info->plot_mode == PLOT_2D) {
        if (mblk->info->phase_2d)
            flag = PLOT_BOX;

        psnd_axis(mblk, vp_id, DAT->isspec, flag, DAT->ityp, 
                   mblk->cpar_screen->ilowa, mblk->cpar_screen->ihigha, 
                   mblk->spar, mblk->cpar_screen, 
                   mblk->cpar_screen->par1, mblk->cpar_screen->par2, DAT);
    }
    else
        psnd_axis(mblk,vp_id, DAT->isspec, flag, DAT->ityp, xleft, xright,
                   mblk->spar, mblk->cpar_screen, par, NULL, DAT);
}

void psnd_plotaxis(MBLOCK *mblk, PBLOCK *par)
{
    int flag = PLOT_ALL;
    plotaxis(mblk,flag,par);
}

void psnd_plotgrid(MBLOCK *mblk)
{
    int flag = PLOT_GRID + PLOT_BOX;
    plotaxis(mblk,flag,PAR);
}
/*
void psnd_toggle_xaxis(SBLOCK *spar)
{
    spar[S_AXISX_MARK].show++;
    if (spar[S_AXISX_MARK].show == 5)
        spar[S_AXISX_MARK].show = 0;
}

void psnd_toggle_yaxis(MBLOCK *mblk, SBLOCK *spar)
{
    if (mblk->info->plot_mode == PLOT_2D) {
        spar[S_AXISY_MARK].show++;
        if (spar[S_AXISY_MARK].show == AXIS_UNITS_AUTOMATIC)
            spar[S_AXISY_MARK].show = AXIS_UNITS_NONE;
    }
    else {
        spar[S_AXISXY_MARK].show++;
        if (spar[S_AXISXY_MARK].show == AXIS_UNITS_HERTZ)
            spar[S_AXISXY_MARK].show = AXIS_UNITS_NONE;
    }
}
*/

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

/*
 * Suppose plot is wider than paper size:
 * This results in right viewport coordinate > 1
 * Set vpx2 to 1 and correct right world-coordinate
 * instead, etc.
 */
void psnd_get_clipped_viewwin(float *vx1, float *vy1, float *vx2, float *vy2,
                         float *wx1, float *wy1, float *wx2, float *wy2)
{
    float nwx1 = *wx1;
    float nwx2 = *wx2;
    float nwy1 = *wy1;
    float nwy2 = *wy2;
    float nvx1 = min(*vx1,*vx2);
    float nvx2 = max(*vx1,*vx2);
    float nvy1 = min(*vy1,*vy2);
    float nvy2 = max(*vy1,*vy2);
    
    if (nvx1 < 0.0) {
        nvx1 = 0.0;
        nwx1 =  (nvx1 - (*vx1)) * (*wx2 - *wx1)/ (*vx2 - *vx1) + *wx1;
    }
    if (nvx2 > 1.0) {
        nvx2 = 1.0;
        nwx2 =  (nvx2 - (*vx1)) * (*wx2 - *wx1)/ (*vx2 - *vx1) + *wx1;
    }
    if (nvy1 < 0.0) {
        nvy1 = 0.0;
        nwy1 =  (nvy1 - (*vy1)) * (*wy2 - *wy1)/ (*vy2 - *vy1) + *wy1;
    }
    if (nvy2 > 1.0) {
        nvy2 = 1.0;
        nwy2 =  (nvy2 - (*vy1)) * (*wy2 - *wy1)/ (*vy2 - *vy1) + *wy1;
    }
    *wx1 = nwx1;
    *wx2 = nwx2;
    *wy1 = nwy1;
    *wy2 = nwy2;
    *vx1 = nvx1;
    *vx2 = nvx2;
    *vy1 = nvy1;
    *vy2 = nvy2;

 }

static int paperplot_1d(MBLOCK *mblk, int preview, int silent, FILE *outfile, 
                        CONPLOTQ_TYPE *cpq, 
                        SBLOCK *spar, PBLOCK *par, DBLOCK *dat)
{
    int vp = spar[S_HARDCOPY].vp_id,
        obj= spar[S_HARDCOPY].obj_id,
        col= spar[S_HARDCOPY].color;
    int i,j,dest_id,done=FALSE;
    int ilowa, ihigha, iclip;
    float xmin, xmax, ymin, ymax, xdnq, txmin, tymin, txmax, tymax;

#ifdef _WIN32
    if (!(preview || (cpq->device == G_PRINTER))) {
#else
    if (!preview) {
#endif
        g_set_outfile(outfile);
        g_set_devicesize(cpq->device, cpq->dev_width, cpq->dev_height);
    }
    iclip =  g_get_clipping();
    ymin = cpq->yminv;
    ymax = cpq->ymaxv;
    xmax = cpq->xminv;
    xdnq = (cpq->xmaxv - cpq->xminv)/cpq->nqx;
#ifdef _WIN32
    if (!preview && (cpq->device == G_PRINTER)) {
        if (g_open_device(G_PRINTER)== G_ERROR)
            return FALSE;
    }
#endif
    for (i=0;i<cpq->nqx && !done;i++) {
        float tmp1, tmp2;
        float xminp, xmaxp, xminptot, xmaxptot;
        float vx1,vx2,vy1,vy2;
        float width,height,hshift,scale;
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
        xmin = xmax;
        xmax = xmin + xdnq;
        if (cpq->unitsx == UNIT_PPM) {
            xminp = psnd_chan2ppm(xmin, par->sfd, dat->sw, 
                          dat->isize, par->xref,
                          par->aref, par->bref, dat->irc);
            xmaxp = psnd_chan2ppm(xmax, par->sfd, dat->sw, 
                          dat->isize, par->xref,
                          par->aref, par->bref, dat->irc);
            xminptot = cpq->xminpv;
            xmaxptot = cpq->xmaxpv;
        }
        else if (cpq->unitsx == UNIT_HERTZ) {
            xminp = psnd_chan2hz(xmin, dat->sw, 
                          dat->isize, par->xref,
                          par->aref, par->bref, dat->irc);
            xmaxp = psnd_chan2hz(xmax, dat->sw, 
                          dat->isize, par->xref,
                          par->aref, par->bref, dat->irc);
            xminptot = cpq->xminhv;
            xmaxptot = cpq->xmaxhv;
        }
        else if (cpq->unitsx == UNIT_SEC) {
            xminp = psnd_chan2sec(xmin, dat->sw);
            xmaxp = psnd_chan2sec(xmax, dat->sw);
            xminptot = cpq->xminsv;
            xmaxptot = cpq->xmaxsv;
        }
        else {
            xminp = xmin;
            xmaxp = xmax;
            xminptot = cpq->xminv;
            xmaxptot = cpq->xmaxv;
        }
        if (!silent) {
            psnd_printf(mblk," plot   :%8d    loop: %8d\n",i+1,i+1);
            psnd_printf(mblk," scales :\n");
            psnd_printf(mblk," cm     :%8.2f %8.2f %8.2f %8.2f\n",
                cpq->cminx,cpq->cmaxx,cpq->cminy,cpq->tymax);
            psnd_printf(mblk," all:\n");
            psnd_printf(mblk,"        :%8.2f %8.2f %8.2f %8.2f\n",
                cpq->xminv ,cpq->xmaxv ,cpq->yminv ,cpq->ymaxv);
            if (cpq->unitsx != UNIT_CHAN) 
                psnd_printf(mblk,"        :%8.2f %8.2f\n",
                    xminptot,xmaxptot);
            psnd_printf(mblk," current:\n");
            psnd_printf(mblk,"        :%8.2f %8.2f %8.2f %8.2f\n",
                xmin ,xmax ,ymin ,ymax);
            if (cpq->unitsx != UNIT_CHAN) 
                psnd_printf(mblk,"        :%8.2f %8.2f\n",
                    xminp,xmaxp);
            psnd_printf(mblk,"\n");
        }
        ilowa  = round(xmin);
        ihigha = round(xmax);
        /*
         * When printing to paper, the window coordinates specifiy
         * the size and the position of the window on the paper. When
         * printing to the screen in preview mode, this behaviour
         * is emulated by redefining the viewport coordinates
         */
        if (preview) {
            if (i==0)
                dest_id = g_open_window(G_SCREEN, cpq->offx, cpq->offy, 
                                       scale*width, scale*height, 
                        "Preview: 1D Plot, anykey=next, q=quit", cpq->option);
            else
                g_newpage();
        }
        else {
            int penw ;
            penw = psnd_calc_ps_penwidth(cpq->pen_width);
            g_set_linewidth(penw);
            dest_id = g_open_window(cpq->device, cpq->offx, cpq->offy, 
                              cpq->cmaxx - cpq->offx, cpq->tymax - cpq->offy, 
                              "1D Plot", cpq->option);
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
            float title_start, axis_width;
            /*
             * cm to ppm
             */
            title_start = xminp + cpq->xstart_title *
                         (xmaxp-xminp)/(cpq->cmaxx - cpq->cminx);
            if (preview) {
                vx1=cpq->offx/width;
                vx2=cpq->cmaxx/width;
                vy1=(hshift+cpq->offy)/height;
                vy2=(hshift+cpq->cloffy+cpq->offy)/height;
            }
            else {
                vx1=0.0;
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
                psnd_get_clipped_viewwin(&vx1, &vy1, &vx2, &vy2,
                                &xminp, &tymin, &xmaxp, &tymax);
                g_set_clipping(FALSE);
                psnd_paperplot_xaxis(vp, cpq->xticks_autoscale,
                    cpq->axtxt, xminp, xmaxp, 
                    cpq->xticks_ppm_short, cpq->xticks_ppm_medium, 
                    cpq->xticks_ppm_long, cpq->xlabels_ppm,
                    cpq->xoff_axis, cpq->xoff_labels,
                    title_start, cpq->xoff_title,  
                    cpq->xticklen_short,cpq->xticklen_medium, 
                    cpq->xticklen_long, axis_width, cpq->cloffy, 
                    vx1,vy1,vx2,vy2);
            }
        }
        /*
         * main plot
         */
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
        txmin = xmin;
        txmax = xmax;
        tymin = ymin;
        tymax = ymax;
        psnd_get_clipped_viewwin(&vx1, &vy1, &vx2, &vy2,
                            &txmin, &tymin, &txmax, &tymax);

        g_set_world(vp, txmin, tymin, txmax, tymax);
        g_set_viewport(vp, vx1,vy1,vx2,vy2);
/*
g_rectangle(txmin, tymin, txmax, tymax);
*/
        g_set_clipping(TRUE);
        g_moveto((float)ilowa, dat->xreal[ilowa-1]);
        for (j=ilowa;j<ihigha;j++)
            g_lineto((float)(j+1), dat->xreal[j]);
        /*
         * Peak pick
         */
        if (cpq->peakpick) {
            int vp2 = vp+1;
            float pymin,pymax;
            if (preview) {
                vx1=cpq->cminx/width;
                vx2=cpq->cmaxx/width;
                vy1=(hshift+cpq->cmaxy)/height;
                vy2=(hshift+cpq->lymax)/height;
            }
            else {
                vx1=(cpq->cminx - cpq->offx)/(cpq->cmaxx - cpq->offx);
                vx2=1.0;
                vy1=(cpq->cmaxy - cpq->offy)/(cpq->tymax - cpq->offy);
                vy2=(cpq->lymax - cpq->offy)/(cpq->tymax - cpq->offy);
            }
            pymin=0;
            pymax = cpq->lymax - cpq->cmaxy;
            if (vy1 < 1.0 && vy2 > 0.0) {
                float dumx1, dumx2;
            /*
            if (vy2 > 1.0) {
                pymax = pymax/vy2;
                vy2 = 1.0;
            }
            */
                dumx1 = xmin;
                dumx2 = xmax;
                psnd_get_clipped_viewwin(&vx1, &vy1, &vx2, &vy2,
                            &dumx1, &pymin, &dumx2, &pymax);
                g_set_world(vp2, dumx1, pymin, dumx2, pymax);
                g_set_viewport(vp2, vx1,vy1,vx2,vy2);
                
/* g_rectangle(dumx1, pymin, dumx2, pymax); */
                psnd_paperplot_labels(vp, vp2, dat->xreal, 
                    txmin, txmax, tymin, tymax,
                    cpq->cminx, cpq->cmaxx, cpq->cminy, cpq->cmaxy,
                    cpq->thresh, cpq->sens, cpq->avewidth,
                    cpq->sign, cpq->npart,
                    cpq->lheight, pymax,
                    par->sfd, dat->sw, dat->isize, par->xref,
                    par->aref, par->bref, dat->irc, cpq->unitsx);
            }
        }
        /*
         * Show Title
         */
        if (cpq->show_title) {
            int vp2 = vp+2;
            float pymin,pymax;
            if (preview) {
                vx1=cpq->cminx/width;
                vx2=cpq->cmaxx/width;
                vy1=(hshift+cpq->lymax)/height;
                vy2=(hshift+cpq->tymax)/height;
            }
            else {
                vx1=(cpq->cminx - cpq->offx)/(cpq->cmaxx - cpq->offx);
                vx2=1.0;
                vy1=(cpq->lymax - cpq->offy)/(cpq->tymax - cpq->offy);
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
                g_set_clipping(FALSE);
/* g_rectangle(0, 0, 100, 100); */
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
        if (!preview || i>=cpq->nqx-1 || done)
            g_close_window(dest_id);
/*
        if (preview) {
            static G_EVENT ui;
            while (g_peek_event() & G_WINDOWDESTROY)
                g_get_event(&ui);
        }
*/
        g_delete_object(obj);
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

int psnd_paperplot_1d(MBLOCK *mblk, int preview)
{
    char *filename;
    FILE *outfile=NULL;

#ifdef _WIN32
    if (!(preview || (mblk->cpq_screen->device == G_PRINTER))) {
#else
    if (!preview) {
#endif
        filename = psnd_savefilename(mblk,"Output file for 1D plot", "*");
        if (filename == NULL)
            return FALSE;
#ifdef _WIN32
        if ((outfile = fopen(filename, "w+b")) == NULL)
#else
        if ((outfile = fopen(filename, "w+")) == NULL)
#endif
            return FALSE;
    }   
    paperplot_1d(mblk,preview, FALSE, outfile, mblk->cpq_screen, mblk->spar, PAR, DAT); 
    g_set_motif_realtextrotation(0);
    g_select_window(mblk->info->win_id);
    return TRUE;  
}


int psnd_au_paperplot_1d(MBLOCK *mblk, char *filename)
{
    FILE *outfile=NULL;
    float xminv, yminv, xmaxv, ymaxv;


#ifdef _WIN32
    if ((outfile = fopen(filename, "w+b")) == NULL)
#else
    if ((outfile = fopen(filename, "w+")) == NULL)
#endif
        return FALSE;
    if (mblk->info->win_id == G_ERROR) {
        g_init(0, NULL);
        psnd_init_colormap();
        psnd_init_objects(mblk);
    }
    psnd_init_paper_plot(mblk->cpq_hardcopy);
    /*
    if (mblk->info->win_id != G_ERROR) {
        g_get_world(spar[S_REAL].vp_id,
                &xminv,
                &yminv,
                &xmaxv,
                &ymaxv);
        cpq->xminv = xminv;
        cpq->yminv = yminv;
        cpq->xmaxv = xmaxv;
        cpq->ymaxv = ymaxv;
    }
    */
    psnd_process_plot_settings(mblk->cpq_hardcopy, mblk->cpar_hardcopy);
    paperplot_1d(mblk, FALSE, TRUE, outfile, mblk->cpq_hardcopy, 
                 mblk->spar, PAR, DAT); 
    if (mblk->info->win_id != G_ERROR) {
        g_set_motif_realtextrotation(0);
        g_select_window(mblk->info->win_id);
    }
    else
        g_end();
    return TRUE;  
}

