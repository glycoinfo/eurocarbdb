/********************************************************************/
/*                         psnd_axis.c                              */
/*                                                                  */
/* Axis routines for the psnd program                               */
/* 1997, Albert van Kuik                                            */
/********************************************************************/
#include <stdio.h>
#include <stdlib.h>
#include <ctype.h>
#include <string.h>
#include <math.h>
#include "genplot.h"
#include "psnd.h"

#define TICKLEN_SHORT	3
#define TICKLEN_MEDIUM	5
#define TICKLEN_LONG	8

/*
c
c  step size: e.g.:  0.0378  > 0.05
c                       139  > 25
c                        99  > 10
c                        24  > 2.5
c  4 < number of steps < 9
c
   x must be > 0
*/
static float get_stepsize(float x)
{
    float q=1.0;

    if (x <= 0.0)
        return 0.0;

    while (x > 10.0) {
        x /= 10.0;
        q *= 10.0;
    }
    while (x < 1.0 && x > 0.0) {
        x *= 10.0;
        q /= 10.0;
    }
    if (x<2.0)
        return q * 0.2;
    if (x<5.0)
        return q * 0.5;
    return q;
}

/*
c
c     define a nice unity
c
*/
static void get_unit(float wrldmi,float wrldma,float *divfac, char *s)
{

    float r = max(fabs(wrldma),fabs(wrldmi));
    char cu = ' ';
    *divfac = 1.0;
    if (r < 1e-12) {
        cu = 'f';
        *divfac = 1e-15;
    }
    else if (r < 1e-9) {
        cu = 'p';
        *divfac = 1e-12;
    }
    else if (r < 1e-6) {
        cu = 'n';
        *divfac = 1e-9;
    }
    else if (r < 1e-3) {
        cu = 'u';
        *divfac = 1e-6;
    }
    /*
    else if (r < 1e+0) {
        cu = 'm';
        *divfac = 1e-3;
    }
    */
    else if (r >= 1e+9) {
        cu = 'T';
        *divfac = 1e+9;
    }
    else if (r >= 1e+6) {
        cu = 'M';
        *divfac = 1e+6;
/*      }else if (r >= 1e+3) {*/
    }
    else if (r >= 9.99e+3) {
        cu = 'k';
        *divfac = 1e+3;
    }
    s[0] = cu;
    s[1] = '\0';
}

void psnd_box(MBLOCK *mblk, int vp_id, 
               float vpxmi,float vpxma,float vpymi,float vpyma,
               int isbox, int do_cross,
               int obj, int color)
{
    psnd_setworld(mblk,vp_id,vpxmi,vpxma,vpymi,vpyma);
    g_delete_object(obj);
    g_open_object(obj);
    psnd_set_groupviewport(mblk,vp_id);
    g_set_foreground(color);
    if (isbox) {
        g_moveto(vpxmi,vpymi);
        g_lineto(vpxmi,vpyma);
        g_lineto(vpxma,vpyma);
        g_lineto(vpxma,vpymi);
        g_lineto(vpxmi,vpymi);
        if (do_cross) {
            float dx = (vpxma - vpxmi)/2;
            float dy = (vpyma - vpymi)/2;
            g_moveto(vpxmi+dx,vpymi);
            g_lineto(vpxmi+dx,vpyma);
            g_moveto(vpxmi,vpymi+dy);
            g_lineto(vpxma,vpymi+dy);
        }
    }
    g_close_object(obj);
    g_call_object(obj);
}


void psnd_grid(MBLOCK *mblk, int vp_id, 
               float wx1,float wx2,float wy1,float wy2,
               int showx, int showy,
               int obj, int color)
{
    float zoomx,zoomy,sign,divfac,xwld1,xwld2,ywld1,ywld2,s1;
    float x1,y1,x2,y2;

    psnd_setworld(mblk,vp_id,wx1,wx2,wy1,wy2);

    g_delete_object(obj);
    g_open_object(obj);
    psnd_set_groupviewport(mblk,vp_id);
    g_set_foreground(color);
    g_set_linestyle(G_DOTTED);

    psnd_getzoom(mblk,vp_id, &zoomx, &zoomy);
    zoomx = min(20,zoomx);
    zoomy = min(20,zoomy);
    if (showx) {
        char cu[10];
        float x;
        if (wx1 > wx2) 
            sign = -1.0;
        else
            sign = 1.0;
        divfac = 1;
        get_unit(wx1 * sign, wx2 * sign, &divfac, cu);
        xwld1 = wx1 * sign/divfac;
        xwld2 = wx2 * sign/divfac;
        s1 = get_stepsize((xwld2-xwld1)/zoomx);
        for (x=floor(xwld1/s1)*s1; x <= xwld2; x+=s1) {
            g_moveto(x*divfac*sign,wy2);
            g_lineto(x*divfac*sign,wy1);
            
        }
    }
    if (showy) {
        char cu[10];
        float y;
        if (wy1 > wy2) 
            sign = -1.0;
        else
            sign = 1.0;
        divfac = 1;
        get_unit(wy1 * sign, wy2 * sign, &divfac, cu);
        ywld1 = wy1 * sign/divfac;
        ywld2 = wy2 * sign/divfac;
        s1 = get_stepsize((ywld2-ywld1)/zoomy);
        for (y=floor(ywld1/s1)*s1; y <= ywld2; y+=s1) {
            g_moveto(wx1,y*divfac*sign);
            g_lineto(wx2,y*divfac*sign);
            
        }
    }
    g_set_linestyle(G_SOLID);
    g_close_object(obj);
    g_call_object(obj);
}
/*
static float round_label(float x, int digits)
{ 
    int n,itrunc;

    for (n=0; n < digits; n++)
        x *= 10;
    itrunc = round(x);
    x = itrunc;
    for (n=0; n < digits; n++)
        x /= 10;
    return x;
}
*/
#define FLOOR(a)	floor((a)+0.0001)
static int calc_label_digits(float minp, float labels_ppm)
{
    float a,b,c,d,e;
    int i, n, digits = 0;
    d = fabs(minp);
    e = 1.0/labels_ppm;
    do {
        c = 1.0;
        for (i=0;i<digits;i++)
            c *= 10; 
        a = FLOOR(c*d);
        for (i=1;i<10;i++) {
            b = FLOOR(c*(d+i*e));
            n = (int)floor(a-b);
            if (n==0)
                break;
            a=b;
        }
        if (n==0)
            digits++;
    } while (n==0);
    return digits;
}

void psnd_xaxis(MBLOCK *mblk, int vp_id, 
                float vpxmi,float vpxma,float vpymi,float vpyma,
                float xwldmi,float xwldma,
                int is_integerx,
                int ixbox,
                int ixcode,
                char *label,
                int obj, int color)
{
    int obj2 = obj + 1;
    int vp_id2 = vp_id + 1;
    int ticklen = TICKLEN_SHORT, ticklen2 = TICKLEN_LONG, xdigits=0;
    float tickstep = 10;
    float sign, divfac, s1, s2, s3, x;
    float xwld1, xwld2, y0 = 0.0;
    float wx1,wx2,wy1,wy2;
    float zoomx = 1.0, zoomy;
    char cu[2];
    /*
     *    do x-ax:
     */

    wx1 = xwldmi;
    wx2 = xwldma;
    wy1 = y0;
    wy2 = vpymi-1;
    psnd_setworld(mblk,vp_id,wx1,wx2,wy1,wy2);
    g_delete_object(obj);
    g_open_object(obj);
    psnd_set_groupviewport(mblk,vp_id);
    psnd_getzoom(mblk,vp_id, &zoomx, &zoomy);
    zoomx = min(20,zoomx);
    g_clear_viewport();
    if (ixcode > 0) {
        float start1,start3;
        int diffa,diffb;
        int xcount;
        char format[20];
        g_set_foreground(color);
        /*
         * set marks
         * define wrldsteps per pixel
         */
        if (wx1 > wx2) 
            sign = -1.0;
        else
            sign = 1.0;
        divfac = 1;
        if (ixcode == 4)
            get_unit(wx1 * sign, wx2 * sign, &divfac, cu);
        xwld1 = wx1 * sign/divfac;
        xwld2 = wx2 * sign/divfac;
        /*
         * draw axis
         */
        if (ixbox == 0) {
            g_moveto(wx1,wy2);
            g_lineto(wx2,wy2);
        }
        /*
         * define stepsize of marks
         */
        s1 = get_stepsize((xwld2-xwld1)/(tickstep * zoomx));
        s2 = get_stepsize((xwld2-xwld1)/(2.0 * zoomx));
        s3 = get_stepsize((xwld2-xwld1)/(zoomx));
        if (is_integerx) {
            float f = 1/divfac;
            s1 = max(f,s1);
            s2 = max(f,s2);
            s3 = max(f,s3);
        }
        start3=floor(xwld1/s3)*s3;
        start1=floor(xwld1/s1)*s1;
        diffa = round(s3/s1);
        diffb = round(s2/s1);
        while (diffa % diffb)
            diffb /=2;
        xcount = round(fabs((start3-start1)/s1));
        if (ixcode >= 2) {
            xdigits=calc_label_digits(xwld1, 0.1/s1);
            sprintf(format,"%%.%df", xdigits);
        }
        for (x=start1; x <= xwld2; x+=s1) {
            float posx = x*divfac*sign;
            g_moveto(posx,wy2);
            if (!(xcount % diffa)) {
                g_lineto(posx,wy2-TICKLEN_LONG);
                /*
                 *  set tick labels
                 */
                if (ixcode >= 2) {
                    char str[40];
                    sprintf(str,format, sign * x);
                    g_moveto(posx,wy2-23.0);
                    g_label(str);
                }
            }
            else if (!(xcount % diffb))
                g_lineto(posx,wy2-TICKLEN_MEDIUM);
            else
                g_lineto(posx,wy2-TICKLEN_SHORT);
            xcount++;
        }
        g_close_object(obj);
        g_call_object(obj);
        /*
         *  set unity label
         */
        psnd_setworld(mblk,vp_id2,vpxmi,vpxma,wy1,wy2);
        g_delete_object(obj2);
        g_open_object(obj2);
        psnd_setviewport3(mblk,vp_id2,vpxmi,vpxma,y0,vpymi - 1);
        if (ixcode >= 3) {
            /*
             * draw arrow
             */
             /*
            g_moveto(vpxma-75.,wy2-25.);
            g_lineto(vpxma-55.,wy2-25.);
            g_lineto(vpxma-60.,wy2-20.);
            g_moveto(vpxma-55.,wy2-25.);
            g_lineto(vpxma-60.,wy2-30.);
            */
            g_moveto(vpxma-40.,wy2-30.);
            g_label(label);
        }
        if (ixcode >= 4) {
             /*
              *  insert unity character
             */
             g_moveto(vpxma-52.,wy2-30.);
             g_label(cu);
        }
        g_close_object(obj2);
        g_call_object(obj2);
    }
    else {
        g_close_object(obj);
        g_call_object(obj);
    }
}


void psnd_yaxis(MBLOCK *mblk, int vp_id, 
                float vpxmi,float vpxma,float vpymi,float vpyma,
                float ywldmi,float ywldma,
                int is_integery,
                int ixbox,
                int iycode,
                char *label,
                int obj, int color)
{
    int obj2 = obj + 1;
    int vp_id2 = vp_id + 1;
    int ticklen = TICKLEN_SHORT, ticklen2 = TICKLEN_LONG,ydigits=0;
    float tickstep = 10;
    float sign, divfac, s1, s2, s3, y;
    float ywld1, ywld2, x0 = 0.0;
    float wx1,wx2,wy1,wy2;
    float zoomx = 1.0, zoomy;
    char cu[2];
    /*
     *    do y-ax:
     */

    wx1 = x0; 
    wx2 = vpxmi-1; 
    wy1 = ywldmi;
    wy2 = ywldma;
    psnd_setworld(mblk,vp_id,wx1,wx2,wy1,wy2);
    g_delete_object(obj);
    g_open_object(obj);
    psnd_set_groupviewport(mblk,vp_id);
    psnd_getzoom(mblk,vp_id, &zoomx, &zoomy);
    zoomy = min(20,zoomy);
    g_clear_viewport();
    if (iycode > 0) {
        float start1,start3;
        int diffa,diffb;
        int ycount;
        char format[20];
        g_set_foreground(color);
        /*
         * set marks
         * define wrldsteps per pixel
         */
        if (wy1 > wy2) 
            sign = -1.0;
        else
            sign = 1.0;
        divfac = 1;
        if (iycode == 4)
            get_unit(wy1 * sign, wy2 * sign, &divfac, cu);
        ywld1 = wy1 * sign/divfac;
        ywld2 = wy2 * sign/divfac;
        /*
         * draw axis
         */
        if (ixbox == 0) {
            g_moveto(wx2,wy1);
            g_lineto(wx2,wy2);
        }
        /*
         * define stepsize of marks
         */
        s1 = get_stepsize((ywld2-ywld1)/(tickstep * zoomy));
        s2 = get_stepsize((ywld2-ywld1)/(2.0 * zoomy));
        s3 = get_stepsize((ywld2-ywld1)/(zoomy));
        if (is_integery) {
            float f = 1/divfac;
            s1 = max(f,s1);
            s2 = max(f,s2);
            s3 = max(f,s3);
        }
        start3=floor(ywld1/s3)*s3;
        start1=floor(ywld1/s1)*s1;
        diffa = round(s3/s1);
        diffb = round(s2/s1);
        while (diffa % diffb)
            diffb /=2;
        ycount = round(fabs((start3-start1)/s1));
        if (iycode >= 2) {
            ydigits=calc_label_digits(ywld1, 0.1/s1);
            sprintf(format,"%%.%df", ydigits);
        }
        for (y=start1; y <= ywld2; y+=s1) {
            float posy = y*divfac*sign; 
            g_moveto(wx2,posy);
            if (!(ycount % diffa)) {
                g_lineto(wx2-TICKLEN_LONG,posy);
                /*
                 *  set tick labels
                 */
                if (iycode >= 2) {
                    char str[40];
                    float ypos;
                    sprintf(str,format, y*sign);
                    ypos = strlen(str);
                    ypos = min(3,ypos);
                    ypos = max(1, ypos);
                    ypos = ypos*10 + 10;
                    g_moveto(wx2-ypos,posy);
                    g_label(str);
                }
            }
            else if (!(ycount % diffb))
                g_lineto(wx2-TICKLEN_MEDIUM,posy);
            else
                g_lineto(wx2-TICKLEN_SHORT,posy);
            ycount++;
        }
        g_close_object(obj);
        g_call_object(obj);
        /*
         *  set unity label
         */
        psnd_setworld(mblk,vp_id2,wx1,wx2,vpymi,vpyma);
        g_delete_object(obj2);
        g_open_object(obj2);
        psnd_setviewport3(mblk,vp_id2,x0,vpxmi - 2, vpymi,vpyma);
        if (iycode >= 3) {
            /*
             * draw arrow
             */
             /*
            g_moveto(wx2-25.,vpyma-55.);
            g_lineto(wx2-25.,vpyma-35.);
            g_lineto(wx2-20.,vpyma-40.);
            g_moveto(wx2-25.,vpyma-35.);
            g_lineto(wx2-30.,vpyma-40.);
            */
            g_moveto(wx1+10.,vpyma-25.);
            g_label(label);
        }
        if (iycode >= 4) {
             /*
              *  insert unity character
             */
             g_moveto(wx2-52.,vpyma-30.);
             g_label(cu);
        }
        g_close_object(obj2);
        g_call_object(obj2);
    }
    else {
        g_close_object(obj);
        g_call_object(obj);
    }
}

