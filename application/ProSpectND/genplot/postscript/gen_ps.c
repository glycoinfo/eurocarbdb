/************************************************************************/
/*                               gen_ps.c                               */
/*                                                                      */
/*  Platform : All                                                      */
/*  Module   : Genplot Postscript plot function                         */
/*                                                                      */
/*  Albert van Kuik                                                     */
/************************************************************************/
/* 72 units = 1 inch                        */
/* 1 inch   = 2.54 cm                       */ 
/* A4 = 595 x 842  units = 21.0 x 29.7 cm */
/* A3 = 842 x 1191 units = 29.7 x 42.0 cm */


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
#define  GENPLOT_(x) ps_##x
#include "g_inter1.h"

static void lswap(long *x, long *y)
{
    long z; z = *x; *x = *y; *y = z;
}

#define PS_EMPTY            1
#define PS_SOLID            0

static  int  ps_fontsize;
static  int  ps_font;
static  int  ps_old_fontsize;
static  int  ps_orientation;
static  int  ps_isnewpage;
static  int  ps_eps_flag;

static long  ps_x0;
static long  ps_y0;
static long  ps_width;
static long  ps_height;

static  int  ps_isfirstpage;
static  int  ps_current_page;
static  int  ps_isclipped;

static int A4paper[] = { A4_HEIGHT_IN_PS_UNITS, A4_WIDTH_IN_PS_UNITS};
static int A3paper[] = { A3_HEIGHT_IN_PS_UNITS, A3_WIDTH_IN_PS_UNITS};
static int LetterPaper[] = { LETTER_HEIGHT_IN_PS_UNITS, LETTER_WIDTH_IN_PS_UNITS};
static int LegalPaper[] = { LEGAL_HEIGHT_IN_PS_UNITS, LEGAL_WIDTH_IN_PS_UNITS};


static  char *ps_fonts[] = {
    "Times-Roman",  
    "Times-Italic",
    "Times-Bold",
    "Times-BoldItalic",
    "Helvetica",
    "Helvetica-Oblique",
    "Helvetica-Bold",
    "Helvetica-BoldOblique",
    "Courier",
    "Courier-Oblique",
    "Courier-Bold",
    "Courier-BoldOblique",
    "Symbol"
};

static int ps_used_fonts[G_MAXFONT+1];


static char *ps_color(int color)
{
    static char pscolor[20];
    G_PALETTEENTRY g_palette;

    INTERNAL_get_palette_color(&g_palette, color);
    if (GWIN.flag & G_WIN_BLACK_ON_WHITE) {
        if (color == INTERNAL_store_gc.bcolor)
            sprintf(pscolor, "1.00 1.00 1.00");
        else
            sprintf(pscolor, "0.00 0.00 0.00");
    }
    else
        sprintf(pscolor, "%1.2f %1.2f %1.2f",
                (float) g_palette.r / 255.0,
                (float) g_palette.g / 255.0,
                (float) g_palette.b / 255.0);
    return pscolor;
}

#define NOSTROKE     0
#define DOSTROKE     1

#define NOPATH       0
#define DOPATH       1

static int  ps_newpath;
static int  ps_stroke;
static int  ps_stacksize;
static int  ps_moveto_ok;

static void teststroke(int stroke, int newpath)
{
    if (ps_stroke && stroke) {
        fprintf(g_outfile,"st ");
        ps_moveto_ok = FALSE;
        ps_stroke = 0;
        ps_newpath = FALSE;
    }
    else if (ps_newpath)
        ps_stroke++;
    if (newpath == DOPATH && !ps_newpath) {
        fprintf(g_outfile,"np ");
        ps_moveto_ok = FALSE;
        ps_newpath = TRUE;
    }
}


static void ps_print_defs(void)
{
    fprintf(g_outfile,"/gpDict 20 dict def\n");
    fprintf(g_outfile,"gpDict begin\n");
    fprintf(g_outfile,"/l {lineto} def ");
    fprintf(g_outfile,"/m {moveto} def ");
    fprintf(g_outfile,"/sc {scale} def\n");
    fprintf(g_outfile,"/t {translate} def ");
    fprintf(g_outfile,"/r {rotate} def ");
    fprintf(g_outfile,"/w {setlinewidth} def\n");
    fprintf(g_outfile,"/d {setdash} def ");
    fprintf(g_outfile,"/st {stroke} def\n");
    fprintf(g_outfile,"/cp {closepath} def ");
    fprintf(g_outfile,"/np {newpath} def ");
    fprintf(g_outfile,"/gs {gsave} def ");
    fprintf(g_outfile,"/gr {grestore} def\n");
    fprintf(g_outfile,"/fg {/blue exch def /green exch def /red exch def\n");
    fprintf(g_outfile,"np red green blue setrgbcolor} def\n");
    fprintf(g_outfile,"/f { /s exch def findfont s scalefont setfont} def\n");
    fprintf(g_outfile,"/dc { /ra exch def /y exch def /x exch def np x y ra 0 360 arc st} def\n"); 
    fprintf(g_outfile,"/fc { /ra exch def /y exch def /x exch def np x y ra 0 360 arc\n");
    fprintf(g_outfile,"cp gs fill gr} def\n");
    fprintf(g_outfile,"/dr { /y1 exch def /x1 exch def /y0 exch def /x0 exch def\n");
    fprintf(g_outfile,"np x0 y0 m x0 y1 l x1 y1 l x1 y0 l x0 y0 l st} def\n");
    fprintf(g_outfile,"/fr { /y1 exch def /x1 exch def /y0 exch def /x0 exch def\n");
    fprintf(g_outfile,"np x0 y0 m x0 y1 l x1 y1 l x1 y0 l x0 y0 l\n");
    fprintf(g_outfile,"cp gs fill gr st} def\n");
    fprintf(g_outfile,"/clp { /y1 exch def /x1 exch def /y0 exch def /x0 exch def\n");
/*    fprintf(g_outfile,"initclip np x0 y0 m x0 y1 l x1 y1 l x1 y0 l x0 y0 l cp clip np} def\n");*/
    fprintf(g_outfile,"gr gs np x0 y0 m x0 y1 l x1 y1 l x1 y0 l x0 y0 l cp clip np} def\n");
    fprintf(g_outfile,"/cvp { /y1 exch def /x1 exch def /y0 exch def /x0 exch def\n");
    fprintf(g_outfile,"/blue exch def /green exch def /red exch def\n");
    fprintf(g_outfile,"np gs red green blue fg x0 y0 m x0 y1 l x1 y1 l x1 y0 l x0 y0 l\n"); 
    fprintf(g_outfile,"cp fill gr} def\n");
}

static void ps_print_gc(void)
{
    ps_moveto_ok    = FALSE;
    ps_font         = -1;
    ps_fontsize     = -1;
    GENPLOT_(linewidth)(INTERNAL_store_gc.linewidth);
    GENPLOT_(linestyle)(INTERNAL_store_gc.linepat);
    GENPLOT_(foreground)(INTERNAL_store_gc.fcolor);
}

static void ps_testnewpage(void)
{
    long width, height, devheight, devwidth;

    if (ps_isnewpage) {
        ps_isclipped = FALSE;
        ps_isnewpage = FALSE;
        fprintf(g_outfile,"%%%%Page: %d %d\n",
                          ps_current_page,ps_current_page + 1);
        ps_current_page++;
        fprintf(g_outfile,"%0.4f %0.4f sc\n",PS2PSHR_UNITS,PS2PSHR_UNITS);
        devheight = GENPLOT_(device_height) * GENPLOT_(dots_per_cm);
        devwidth = GENPLOT_(device_width) * GENPLOT_(dots_per_cm);
        if (ps_orientation != G_WIN_PORTRAIT) {
            height = 0;
            width  = devheight;
            fprintf(g_outfile,"%ld %ld t\n", width, height);
            fprintf(g_outfile,"90 r\n");
            height = devheight - ps_height - ps_y0;
            width  = ps_x0;
            if (width != 0 || height != 0)
                fprintf(g_outfile,"%ld %ld t\n", width, height);
        }
        else {
            height = devwidth - ps_height - ps_y0;
            width  = ps_x0;
            fprintf(g_outfile,"%ld %ld t\n", width, height);
        }
        fprintf(g_outfile,"gs\n");
        ps_print_gc();
    }
}


/*
 * Bounding Box for EPS is expressed in default PostScript
 * coordinate system (72 units = 1 inch) from the lower left
 * corner of the sheet of paper (portrait).
*/

float GENPLOT_(device_width)       = A4_WIDTH_IN_CM;
float GENPLOT_(device_height)      = A4_HEIGHT_IN_CM;
int   GENPLOT_(dots_per_cm)        = PSHR_UNITS_PER_CM;
int   GENPLOT_(y_is_upsidedown)    = FALSE;
int   GENPLOT_(able2clip)          = FALSE;
int   GENPLOT_(multi_windows)      = FALSE;

int GENPLOT_(open)(void)
{
    return G_OK;
}

void GENPLOT_(close)(void)
{
    ;
}

void GENPLOT_(open_window)(int id,long x0,long y0,long width,
                                 long height,char *title,long flag)
{
    time_t t;
    float cm2units = PS_UNITS_PER_CM / HP_UNITS_PER_CM;
    int i;

    id=id;
    if (flag & G_WIN_EPS)
        ps_eps_flag = TRUE;
    else
        ps_eps_flag = FALSE;
    ps_x0     = x0;
    ps_y0     = y0;
    ps_width  = width;
    ps_height = height;
    time(&t);
    ps_current_page = 0;
    ps_isnewpage    = TRUE;
    ps_newpath      = FALSE;
    ps_stroke       = 0;
    ps_stacksize    = 20;
    ps_old_fontsize = 0;
    ps_font         = -1;
    ps_fontsize     = -1;
    ps_moveto_ok    = FALSE;
    for (i=0;i<=G_MAXFONT;i++)
        ps_used_fonts[i] = FALSE;
    if (ps_eps_flag) {
        fprintf(g_outfile,"%%!PS-Adobe-3.0 EPSF-3.0\n");
        if (title != NULL && title[0] != '\0')
            fprintf(g_outfile,"%%%%Title: (%s)\n", title);
        if (flag & G_WIN_PORTRAIT) 
            y0 = (long)(GENPLOT_(dots_per_cm) * GENPLOT_(device_width) - (float) (height + y0));        
        else {
            lswap(&x0, &y0);
            lswap(&width, &height);
        }
        fprintf(g_outfile,"%%%%BoundingBox: %ld %ld %ld %ld\n",
                           (long) ((float)x0 * cm2units),
                           (long) ((float)y0 * cm2units),
                           (long) ((float)(x0+width) * cm2units),
                           (long) ((float)(y0+height) * cm2units));
        fprintf(g_outfile,"%%%%Pages: 0\n");
    }
    else {
        cm2units = cm2units;
        fprintf(g_outfile,"%%!PS-Adobe-3.0\n");
        if (title != NULL && title[0] != '\0')
            fprintf(g_outfile,"%%%%Title: (%s)\n", title);
        fprintf(g_outfile,"%%%%Pages: (atend)\n");
    }
    if (INTERNAL_g_application) 
        fprintf(g_outfile,"%%%%Creator: %s\n", INTERNAL_g_application);
    else
        fprintf(g_outfile,"%%%%Creator: Kuik Software\n");

    fprintf(g_outfile,"%%%%CreationDate: %s",ctime(&t));
    fprintf(g_outfile,"%%%%DocumentNeededResources: (atend)\n");
    if (flag & G_WIN_PORTRAIT) {
        ps_orientation  = (int)G_WIN_PORTRAIT;
        fprintf(g_outfile,"%%%%Orientation: Portrait\n");
    }
    else {
        ps_orientation  = 0;
        fprintf(g_outfile,"%%%%Orientation: Landscape\n");
    }
    fprintf(g_outfile,"%%%%EndComments\n");
    fprintf(g_outfile,"save\n");
    ps_print_defs();
    fprintf(g_outfile,"%%%%EndProglog\n");

    if (flag & G_WIN_A4 || flag & G_WIN_A3 ||
        flag & G_WIN_LETTER || flag & G_WIN_LEGAL) {
        fprintf(g_outfile,"%%%%BeginSetup\n");
        if (flag & G_WIN_A4) {
            fprintf(g_outfile,"%%%%BeginFeature: *PageSize A4\n");
            fprintf(g_outfile,
              "<</PageSize [%d %d] /ImagingBBox null>> setpagedevice\n",
              A4_HEIGHT_IN_PS_UNITS, A4_WIDTH_IN_PS_UNITS);
        }
        else if (flag & G_WIN_A3) {
            fprintf(g_outfile,"%%%%BeginFeature: *PageSize A3\n");
            fprintf(g_outfile,
              "<</PageSize [%d %d] /ImagingBBox null>> setpagedevice\n",
              A3_HEIGHT_IN_PS_UNITS, A3_WIDTH_IN_PS_UNITS);
        }
        else if (flag & G_WIN_LETTER) {
            fprintf(g_outfile,"%%%%BeginFeature: *PageSize Letter\n");
            fprintf(g_outfile,
              "<</PageSize [%d %d] /ImagingBBox null>> setpagedevice\n",
              LETTER_HEIGHT_IN_PS_UNITS, LETTER_WIDTH_IN_PS_UNITS);
        }
        else if (flag & G_WIN_LEGAL) {
            fprintf(g_outfile,"%%%%BeginFeature: *PageSize Legal\n");
            fprintf(g_outfile,
              "<</PageSize [%d %d] /ImagingBBox null>> setpagedevice\n",
              LEGAL_HEIGHT_IN_PS_UNITS, LEGAL_WIDTH_IN_PS_UNITS);
        }
        fprintf(g_outfile,"%%%%EndFeature\n");
        fprintf(g_outfile,"%%%%EndSetup\n");
    }
    ps_testnewpage();
    ps_isfirstpage = TRUE;
}

void GENPLOT_(close_window)(int id)
{
    int i, fontcount;

    id = id;
    if (ps_isfirstpage) {
        teststroke(DOSTROKE, NOPATH);
        fprintf(g_outfile,"showpage\ngr\n");
    }
    ps_isnewpage = FALSE;
    fprintf(g_outfile,"%%%%Trailer\n");
    fprintf(g_outfile,"end\nrestore\n");
    for (i=0, fontcount = 0;i<=G_MAXFONT;i++)
        if (ps_used_fonts[i])
            fontcount++;
    if (fontcount) {
        int first_font = TRUE;
        fprintf(g_outfile,"%%%%DocumentNeededResources:");
        for (i=0;i<=G_MAXFONT;i++) {
            if (ps_used_fonts[i]) {
                if (!first_font)
                    fprintf(g_outfile,"%%%%+");
                first_font = FALSE;
                fprintf(g_outfile," font %s\n", ps_fonts[i]);
            }
        }
    }
    if (!ps_eps_flag)
        fprintf(g_outfile,"%%%%Pages: %d\n",ps_current_page);
    fprintf(g_outfile,"%%%%EOF\n");
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
    x=x;y=y;ps_moveto_ok = FALSE;
}

void GENPLOT_(line)(long x1,long y1,long x2, long y2,int isclipped)
{
    ps_testnewpage();
    if (ps_moveto_ok == FALSE || isclipped || ps_stroke > ps_stacksize) {
        teststroke(DOSTROKE, DOPATH);
        fprintf(g_outfile,"%ld %ld m\n",x1,y1);
        ps_moveto_ok = TRUE;
    }
    teststroke(NOSTROKE, NOPATH);
    fprintf(g_outfile,"%ld %ld l\n",x2,y2);
}

void GENPLOT_(linewidth)(int width)
{
    ps_testnewpage();
    teststroke(DOSTROKE, NOPATH);
    fprintf(g_outfile,"%d w\n", width);
}

void GENPLOT_(linestyle)(unsigned pattern)
{
    ps_testnewpage();
    teststroke(DOSTROKE, NOPATH);
    switch (pattern) {
        case G_SHORT_DASHED :
            fprintf(g_outfile,"[40 40] 0 d\n");
            break;
        case G_LONG_DASHED  :
            fprintf(g_outfile,"[80 80] 0 d\n");
            break;
        case G_DOTTED       :
            fprintf(g_outfile,"[20 40] 0 d\n");
            break;
        default             :
            fprintf(g_outfile,"[] 0 d\n");
            break;
    }
}

void GENPLOT_(label)(char *label)
{
    int i, size, len;
    int x1, y1, x2, y2;

    len = strlen(label);
    if (INTERNAL_store_gc.fontscaling == G_RELATIVE_FONTSCALING)
        size  = (int) (INTERNAL_store_gc.charsize * WINHEIGHT / G_POINTSIZE_SCALING);
    else
        size  = (int) (INTERNAL_store_gc.charsize *((float)G_POINTSIZE / PS2HP_UNITS));
    x1 = INTERNAL_store_vp.cx;
    y1 = INTERNAL_store_vp.cy;
    x2 = x1 + size * len;
    y2 = y1 + size;
    if (TEXTROTATE) {
        if (INTERNAL_box_outside_clip_area(ps_isclipped,
                           x1 - y2 + y1, y1, x1, y1 + x2 - x1))
            return;
    }
    else {
        if (INTERNAL_box_outside_clip_area(ps_isclipped,
                                       x1, y1, x2, y2))
            return;
    }
    ps_testnewpage();
    teststroke(DOSTROKE, NOPATH);
    if (!ps_moveto_ok) {
        fprintf(g_outfile,"%d %d m\n", x1, y1);
        ps_moveto_ok = TRUE;
    }
    if (ps_font != INTERNAL_store_gc.font || ps_fontsize != size) {
        ps_font     = INTERNAL_store_gc.font;
        ps_fontsize = size;
        ps_used_fonts[INTERNAL_store_gc.font] = TRUE;
        fprintf(g_outfile,
            "/%s %d f\n",
            ps_fonts[INTERNAL_store_gc.font],
            size);
    }
    if (TEXTROTATE)
        fprintf(g_outfile,"90 r\n");
    fprintf(g_outfile,"(");
    for (i=0;i<len;i++) {
        switch (label[i]) {
        case '\\' :
        case ')'  :
        case '('  :
            fprintf(g_outfile,"\\");
        default   :
            fprintf(g_outfile,"%c", label[i]);
        }
    }
    fprintf(g_outfile,") show\n");
    if (TEXTROTATE)
        fprintf(g_outfile,"-90 r\n");
}


float GENPLOT_(fontheight) (int id)
{
    float size;

    if (INTERNAL_store_gc.fontscaling == G_RELATIVE_FONTSCALING)
        size  = INTERNAL_store_gc.charsize / G_POINTSIZE_SCALING;
    else
        size  = (INTERNAL_store_gc.charsize * G_POINTSIZE) / 
                (PS2HP_UNITS * g_dev[G_POSTSCRIPT].win[id].height);
    return size;
}

void GENPLOT_(rectangle)(long x1, long y1, long x2, long y2)
{
    ps_testnewpage();
    teststroke(DOSTROKE, NOPATH);
    fprintf(g_outfile,"%ld %ld %ld %ld dr\n", x1, y1, x2, y2);
}

void GENPLOT_(drawpoly)(long numpoint, polytype *points)
{
    int  i;
    long px, py;

    ps_testnewpage();
    ps_moveto_ok = TRUE;
    px = points[0];
    py = points[1];
    teststroke(DOSTROKE, NOPATH);
    fprintf(g_outfile,"np %ld %ld m\n", px, py);
    for (i = 2; i < numpoint * 2; i += 2) {
        px = points[i];
        py = points[i+1];
        fprintf(g_outfile,"%ld %ld l\n", px, py);
    }
    fprintf(g_outfile,"st ");
}


void GENPLOT_(circle)(long r)
{
    long x,y;

    ps_testnewpage();
    x = INTERNAL_store_vp.cx;
    y = INTERNAL_store_vp.cy;
    teststroke(DOSTROKE, NOPATH);
    fprintf(g_outfile,"%ld %ld %ld dc\n", x, y, r);
}

void GENPLOT_(fillrectangle)(long x1, long y1, long x2, long y2)
{
    ps_testnewpage();
    teststroke(DOSTROKE, NOPATH);
    fprintf(g_outfile,"%ld %ld %ld %ld fr\n", x1, y1, x2, y2);
}

void GENPLOT_(fillpoly)(long numpoint, polytype *points)
{
    int i;
    long px, py;

    ps_testnewpage();
    ps_moveto_ok = TRUE;
    px = points[0];
    py = points[1];
    teststroke(DOSTROKE, DOPATH);
    fprintf(g_outfile,"%ld %ld m\n", px, py);
    for (i = 2; i < numpoint * 2; i += 2) {
        px = points[i];
        py = points[i+1];
        fprintf(g_outfile,"%ld %ld l\n", px, py);
    }
    fprintf(g_outfile,"cp gs fill gr\n");
}

void GENPLOT_(fillcircle)(long r)
{
    long x,y;

    ps_testnewpage();
    x = INTERNAL_store_vp.cx;
    y = INTERNAL_store_vp.cy;
    teststroke(DOSTROKE, NOPATH);
    fprintf(g_outfile,"%ld %ld %ld fc\n", x, y, r);
}

void GENPLOT_(foreground)(int color)
{
    teststroke(DOSTROKE, NOPATH);
    fprintf(g_outfile, "%s fg\n", ps_color(color));
}

void GENPLOT_(background)(int color)
{
    color = color;
}

void GENPLOT_(clipping) (int clip)
{
    int x1, y1, x2, y2;

    ps_testnewpage();
    if (clip) {
        INTERNAL_get_clip(&x1, &y1, &x2, &y2);
        teststroke(DOSTROKE, NOPATH);
        ps_isclipped = TRUE;
        fprintf(g_outfile,"%d %d %d %d clp\n", x1, y1, x2, y2);
    }
    else {
        if (!ps_isclipped)
            return;
        teststroke(DOSTROKE, NOPATH);
        ps_isclipped = FALSE; 
        fprintf(g_outfile,"gr gs\n");  
    }
    ps_print_gc();
}

void GENPLOT_(newpage)()
{
    ps_isfirstpage = FALSE;
    teststroke(DOSTROKE, NOPATH);
    fprintf(g_outfile,"showpage\ngr\n");
    ps_isnewpage = TRUE;
}

void GENPLOT_(clearviewport)(void)
{
    int x1, y1, x2, y2;
    int color = GGC.bcolor;

    ps_testnewpage();
    teststroke(DOSTROKE, NOPATH);
    INTERNAL_get_clip(&x1, &y1, &x2, &y2);
    fprintf(g_outfile, "%s %d %d %d %d cvp\n", ps_color(color), x1, y1, x2, y2);
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


