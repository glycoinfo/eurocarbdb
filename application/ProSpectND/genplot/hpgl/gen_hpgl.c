/************************************************************************/
/*                               gen_hpgl.c                             */
/*                                                                      */
/*  Platform : All                                                      */
/*  Module   : Genplot HPGL plot functions                              */
/*                                                                      */
/*  Albert van Kuik                                                     */
/************************************************************************/
/* 400  plotter units = 1 cm   */
/* 1016 plotter units = 1 inch */
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
#include <time.h>

#ifdef DEBUG
#include "mshell.h"
#endif
#define  G_LOCAL
#include "genplot.h"
#include "g_inter.h"

#ifdef   GENPLOT_
#undef   GENPLOT_
#endif
#define  GENPLOT_(x) hpgl_##x
#include "g_inter1.h"

static float hpgl_fontsizex;
static float hpgl_fontsizey;
static int   hpgl_font;
static int   hpgl_pen;
static int   hpgl_isfirstpage;
static int   hpgl_ispen;
static int   hpgl_version;
static int   hpgl_papersize;
static int   hpgl_moveto_ok;

static void hpgl_pentest(void)
{
    if (!hpgl_ispen)
        GENPLOT_(linewidth)(INTERNAL_store_gc.linewidth);
}

#define SYMBOL_ROMAN8    277

#define SPACING_FIXED    0
#define SPACING_VAR      1

#define POS_UPRIGHT      0
#define POS_ITALIC       1

#define WEIGHT_NORM      0
#define WEIGHT_BOLD      3

#define TYPE_COURIER     3
#define TYPE_HELV        4148
#define TYPE_TIMES       4101
#define TYPE_STICK       48


typedef struct tag_hpgl_font {
    int   symbol_set;
    int   spacing;
    float pitch;
    float height;
    int   posture;
    int   stroked_weight;
    int   typeface;
} hpgl_font_type;

static hpgl_font_type hpgl_fonts[] = {
 {SYMBOL_ROMAN8, SPACING_VAR, 0.0, 0.0, POS_UPRIGHT, WEIGHT_NORM, TYPE_TIMES},
 {SYMBOL_ROMAN8, SPACING_VAR, 0.0, 0.0, POS_ITALIC, WEIGHT_NORM, TYPE_TIMES},
 {SYMBOL_ROMAN8, SPACING_VAR, 0.0, 0.0, POS_UPRIGHT, WEIGHT_BOLD, TYPE_TIMES},
 {SYMBOL_ROMAN8, SPACING_VAR, 0.0, 0.0, POS_ITALIC, WEIGHT_BOLD, TYPE_TIMES},
 {SYMBOL_ROMAN8, SPACING_VAR, 0.0, 0.0, POS_UPRIGHT, WEIGHT_NORM, TYPE_HELV},
 {SYMBOL_ROMAN8, SPACING_VAR, 0.0, 0.0, POS_ITALIC, WEIGHT_NORM, TYPE_HELV},
 {SYMBOL_ROMAN8, SPACING_VAR, 0.0, 0.0, POS_UPRIGHT, WEIGHT_BOLD, TYPE_HELV},
 {SYMBOL_ROMAN8, SPACING_VAR, 0.0, 0.0, POS_ITALIC, WEIGHT_BOLD, TYPE_HELV},
 {SYMBOL_ROMAN8, SPACING_FIXED, 0.0, 0.0, POS_UPRIGHT, WEIGHT_NORM, TYPE_COURIER},
 {SYMBOL_ROMAN8, SPACING_FIXED, 0.0, 0.0, POS_ITALIC, WEIGHT_NORM, TYPE_COURIER},
 {SYMBOL_ROMAN8, SPACING_FIXED, 0.0, 0.0, POS_UPRIGHT, WEIGHT_BOLD, TYPE_COURIER},
 {SYMBOL_ROMAN8, SPACING_FIXED, 0.0, 0.0, POS_ITALIC, WEIGHT_BOLD, TYPE_COURIER},
 {SYMBOL_ROMAN8, SPACING_FIXED, 0.0, 0.0, POS_UPRIGHT, WEIGHT_NORM, TYPE_STICK}
};


float hpgl_xy_ratio[] = { 2.3, 2.3, 2.3, 2.3,
                          2.3, 2.3, 2.3, 2.3,
                          1.6, 1.6, 1.6, 1.6,
                          1.6 };


float GENPLOT_(device_width)          = A4_WIDTH_IN_CM;
float GENPLOT_(device_height)         = A4_HEIGHT_IN_CM;
int   GENPLOT_(dots_per_cm)           = HP_UNITS_PER_CM;
int   GENPLOT_(y_is_upsidedown)       = FALSE;
int   GENPLOT_(able2clip)             = FALSE;
int   GENPLOT_(multi_windows)         = FALSE;

int GENPLOT_(open)(void)
{
    return G_OK;
}

void GENPLOT_(close)(void)
{
    ;
}

void GENPLOT_(open_window)(int id,long x0,long y0,
               long width,long height,char *title,long flag)
{
    long device_height, device_width;

    id=id;
    width=width;
    title=title;

    if (flag & G_WIN_PCL5)
        hpgl_version = 5;
    else if (flag & G_WIN_HPGL2)
        hpgl_version = 2;
	else
        hpgl_version = 1;
    device_width     = (long) (GENPLOT_(device_width) *
                               (float)GENPLOT_(dots_per_cm));
    device_height    = (long) (GENPLOT_(device_height) *
                               (float)GENPLOT_(dots_per_cm));
    hpgl_isfirstpage = TRUE;
    hpgl_ispen       = TRUE;
    hpgl_fontsizex   = 0.0;
    hpgl_fontsizey   = 0.0;
    hpgl_pen         = 1;
    hpgl_font        = -1;
    hpgl_moveto_ok   = FALSE;
    if (hpgl_version == 5) {
        /*  Reset Printer, one copy */
        fprintf(g_outfile, "\033E\033&l1x");
        if (flag & G_WIN_PORTRAIT)
            fprintf(g_outfile, "\033&l0O");
		else
            fprintf(g_outfile, "\033&l1O");
        if (flag & G_WIN_A3 || GENPLOT_(device_width) > 35 )
            fprintf(g_outfile, "\033&l27A"); /* A3 */
        else 
            fprintf(g_outfile, "\033&l26A"); /* A4 */
        /* enter hpgl2 mode */
        fprintf(g_outfile, "\033%%0B");
    }
    fprintf(g_outfile,"IN;SP1;PU;\n");
    if (flag & G_WIN_PORTRAIT && hpgl_version != 5)
        fprintf(g_outfile,"RO90;\n");
    if (hpgl_version == 5) {
        x0 -= 200;
        y0 += 500;  
    }
    if (flag & G_WIN_PORTRAIT) {
        fprintf(g_outfile,"IP%ld,%ld,%ld,%ld;\n",
						   x0,
                           device_width - height - y0,
                           x0 + width,
                           device_width - y0);
    }
    else {
        fprintf(g_outfile,"IP%ld,%ld,%ld,%ld;\n",
                           x0,
                           device_height - height - y0,
                           x0 + width,
                           device_height - y0);
    }
    fprintf(g_outfile,"SC0,%ld,0,%ld;\n", width, height); 
    if (hpgl_version >= 2) {
        fprintf(g_outfile, "WU0;FT;NP8;TR0;\n");
    }
    GENPLOT_(linewidth)(INTERNAL_store_gc.linewidth);
    GENPLOT_(linestyle)(INTERNAL_store_gc.linepat);
}

void GENPLOT_(close_window)(int id)
{
    id=id;
    if (hpgl_isfirstpage)
        fprintf(g_outfile,"PU;SP0;");
    fprintf(g_outfile,"PU;\n");
    hpgl_ispen = FALSE;
    if (hpgl_version == 5)
        fprintf(g_outfile, "\033%%0A\033E");
}

void GENPLOT_(select_window)(int id)
{
    id = id;
}

void GENPLOT_(raise_window)(int id)
{
    id = id;
}

void GENPLOT_(moveto)(long x,long y)
{
    x=x;y=y;hpgl_moveto_ok = FALSE;
}

void GENPLOT_(line)(long x1,long y1,long x2,long y2, int isclipped)
{
    hpgl_pentest();
    if (hpgl_moveto_ok == FALSE || isclipped) {
        fprintf(g_outfile,"PU;PA%ld,%ld;\n",x1,y1);
        hpgl_moveto_ok = TRUE;
    }
    fprintf(g_outfile,"PD;PA%ld,%ld;\n",x2,y2);
}

void GENPLOT_(linewidth)(int width)
{
    if (hpgl_version >= 2) {
        /* --- default PW = 0.35 --- */
        float f;
        if (width == 1)
            f = 0.01;
        else
            f = 0.04 * (float) width;
        fprintf(g_outfile,"PW%.2f;\n", f);
    }
    else {
        if (width<=1)
            fprintf(g_outfile,"SP%d;\n",hpgl_pen);
        else
            fprintf(g_outfile,"SP%d,%d;\n",hpgl_pen,width);
    }
    hpgl_ispen = TRUE;
}

void GENPLOT_(linestyle)(unsigned pattern)
{
    switch (pattern) {
        case G_SOLID:
            fprintf(g_outfile,"LT;\n");
            break;
        case G_SHORT_DASHED:
            fprintf(g_outfile,"LT%d,%3.1f;\n",2,1.0);
            break;
        case G_LONG_DASHED:
            fprintf(g_outfile,"LT%d,%3.1f;\n",2,2.0);
            break;
        case G_DOTTED :
            fprintf(g_outfile,"LT%d,%3.1f;\n",2,0.5);
            break;
        default :
            fprintf(g_outfile,"LT%d,%d;\n",3,2);
            break;
    }
}

void GENPLOT_(label)(char *label)
{
    float size, height, pitch;

    if (!hpgl_moveto_ok) {
        fprintf(g_outfile,"PU;PA%ld,%ld;\n",
            INTERNAL_store_vp.cx,INTERNAL_store_vp.cy);
        hpgl_moveto_ok = TRUE;
    }
    if (hpgl_version >= 2) {
        if (INTERNAL_store_gc.fontscaling == G_RELATIVE_FONTSCALING)
            height  = (int)(PS2HP_UNITS * INTERNAL_store_gc.charsize * WINHEIGHT /
                         (float) G_POINTSIZE_SCALING );
        else
            height  = (int)(INTERNAL_store_gc.charsize * (float)G_POINTSIZE );
        if (hpgl_font != INTERNAL_store_gc.font || hpgl_fontsizey != height ) {
            hpgl_font      = INTERNAL_store_gc.font;
            hpgl_fontsizey = height;
            /* --- pitch = characters per inch --- */ 
            pitch = PS_UNITS_PER_INCH * 3.2 / (height * 2.0);
            if (hpgl_fonts[hpgl_font].spacing) {
                fprintf(g_outfile, "SD1,%d,2,%d,4,%.2f,5,%d,6,%d,7,%d;\n",
                    hpgl_fonts[hpgl_font].symbol_set,
                    hpgl_fonts[hpgl_font].spacing,
                    height,
                    hpgl_fonts[hpgl_font].posture,
                    hpgl_fonts[hpgl_font].stroked_weight,
                    hpgl_fonts[hpgl_font].typeface);
            }
            else {
                fprintf(g_outfile, "SD1,%d,2,%d,3,%.2f,5,%d,6,%d,7,%d;\n",
                    hpgl_fonts[hpgl_font].symbol_set,
                    hpgl_fonts[hpgl_font].spacing,
                    pitch,
                    hpgl_fonts[hpgl_font].posture,
                    hpgl_fonts[hpgl_font].stroked_weight,
                    hpgl_fonts[hpgl_font].typeface);
            }
        }
    }
    if (hpgl_version == 1) {
        if (INTERNAL_store_gc.fontscaling == G_RELATIVE_FONTSCALING)
            size  = (INTERNAL_store_gc.charsize * WINHEIGHT /
                         (G_POINTSIZE_SCALING * HP_UNITS_PER_CM * 1.5));
        else
            size  = (INTERNAL_store_gc.charsize * (float)G_POINTSIZE /
                          (PS_UNITS_PER_CM * 1.5));
        if (hpgl_fontsizey != size || hpgl_font != INTERNAL_store_gc.font) {
            hpgl_fontsizey = size;
            hpgl_font      = INTERNAL_store_gc.font;
            hpgl_fontsizex = size / hpgl_xy_ratio[hpgl_font];
            fprintf(g_outfile,"SI%.3f,%.3f;\n",
                              hpgl_fontsizex, hpgl_fontsizey);
        }
    }

    if (TEXTROTATE)
        fprintf(g_outfile,"DI-0.000,100.0;\n");
    fprintf(g_outfile,"LB%s%c;\n",label,3);
    if (TEXTROTATE)
        fprintf(g_outfile,"DI100.0,0.0;\n");
}


float GENPLOT_(fontheight) (int id)
{
    float size;

    if (INTERNAL_store_gc.fontscaling == G_RELATIVE_FONTSCALING)
        size  = INTERNAL_store_gc.charsize / G_POINTSIZE_SCALING;
    else
        size  = (INTERNAL_store_gc.charsize * G_POINTSIZE) / 
                (PS2HP_UNITS * g_dev[G_HPGL].win[id].height);
    return size;
}

void GENPLOT_(rectangle)(long x1, long y1, long x2, long y2)
{
    hpgl_pentest();
    hpgl_moveto_ok = TRUE;
    fprintf(g_outfile,"PU;PA%ld,%ld;EA%ld,%ld;\n", x1, y1, x2, y2);
}

void GENPLOT_(drawpoly)(long numpoint, polytype *points)
{
    int  i;
    long px, py;

    hpgl_pentest();
    hpgl_moveto_ok = TRUE;
    for (i = 0; i < numpoint * 2; i += 2) {
        px = points[i];
        py = points[i+1];
        if (!i)
            fprintf(g_outfile,"PU;PA%ld,%ld;\nPD;", px, py);
        else
            fprintf(g_outfile,"PA%ld,%ld;\n", px, py);
    }
}

void GENPLOT_(circle)(long r)
{
    hpgl_pentest();
    if (!hpgl_moveto_ok) {
        fprintf(g_outfile,"PU;PA%ld,%ld;\n",
            INTERNAL_store_vp.cx,INTERNAL_store_vp.cy);
        hpgl_moveto_ok = TRUE;
    }
    fprintf(g_outfile,"CI%ld;\n", r);
}

void GENPLOT_(fillrectangle)(long x1, long y1, long x2, long y2)
{
    hpgl_pentest();
    hpgl_moveto_ok = TRUE;
    if (hpgl_version >= 2)
        fprintf(g_outfile,"PU;PA%ld,%ld;RA%ld,%ld;\n", x1, y1, x2, y2);
}

void GENPLOT_(fillpoly)(long numpoint, polytype *points)
{
    int  i;
    long px, py;

    if (hpgl_version < 2)
        return;
    hpgl_pentest();
    hpgl_moveto_ok = TRUE;
    for (i = 0; i < numpoint * 2; i += 2) {
        px = points[i];
        py = points[i+1];
        if (!i) {
            fprintf(g_outfile,"PU;PA%ld,%ld;\nPD;", px, py);
            fprintf(g_outfile, "PM0\n");
        }
        else
            fprintf(g_outfile,"PA%ld,%ld;\n", px, py);
    }
    fprintf(g_outfile, "PM2FPEP\n");
}

void GENPLOT_(fillcircle)(long r)
{
    if (hpgl_version >= 2) {
        hpgl_pentest();
        if (!hpgl_moveto_ok) {
            fprintf(g_outfile,"PU;PA%ld,%ld;\n",
                INTERNAL_store_vp.cx,INTERNAL_store_vp.cy);
            hpgl_moveto_ok = TRUE;
        }
        fprintf(g_outfile,"PM0;\n");
        fprintf(g_outfile,"CI%ld;\n", r);
        fprintf(g_outfile,"PM2FP;\n");
    }
}


void GENPLOT_(foreground)(int color)
{
    color=color;
}

void GENPLOT_(background)(int color)
{
    color = color;
}

void GENPLOT_(clipping) (int clip)
{
    int x1, y1, x2, y2;

    if (clip) {
        INTERNAL_get_clip(&x1, &y1, &x2, &y2);
        fprintf(g_outfile,"IW%d,%d,%d,%d;\n",x1, y1, x2, y2);
    }
    else
        fprintf(g_outfile,"IW;\n");
}

void GENPLOT_(newpage)()
{
    hpgl_isfirstpage = FALSE;
    hpgl_ispen = FALSE;
    hpgl_moveto_ok = FALSE;
    switch (hpgl_version) {
        case 5:
            fprintf(g_outfile,"\033%%0A\033E\n");
            break;
        case 2:
            fprintf(g_outfile,"PU;SP0;PG;\n");
            break;
        case 1:
            fprintf(g_outfile,"PU;SP0;\n");
            break;
    }
}

void GENPLOT_(clearviewport)(void)
{
    ;
}

int GENPLOT_(palettesize)(int size)
{ 
    size = size; 
    return G_OK; 
}

int GENPLOT_(paletteentry)(int entry_id, G_PALETTEENTRY entry)
{ 
    entry_id = entry_id; 
    entry = entry; 
    return G_OK; 
}




