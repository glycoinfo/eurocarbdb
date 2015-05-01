/************************************************************************/
/*                               gen_wmf.c                              */
/*                                                                      */
/*  Platform : Microsoft Windows                                        */
/*  Module   : Genplot plot functions                                   */
/*                                                                      */
/*  Albert van Kuik                                                     */
/************************************************************************/
/* a twip "twentieth of a point" = 1/1440 of an inch */
/* Thus 1440 twips equal 1 inch */

#include <stdio.h>
#include <string.h>
#include <stdarg.h>

#include <stdio.h>
#include <string.h>
#include <stdarg.h>

#ifdef _Windows
#include <windows.h>
#else

#define BYTE	unsigned char
#define SHORT	short
#define UINT 	unsigned short
#define WORD	short
#define DWORD	long

typedef struct tagMETAHEADER
{
    UINT    mtType;
    UINT    mtHeaderSize;
    UINT    mtVersion;
    DWORD   mtSize;
    UINT    mtNoObjects;
    DWORD   mtMaxRecord;
    UINT    mtNoParameters;
} METAHEADER;

#define META_SETBKCOLOR              0x0201
#define META_SETBKMODE               0x0102
#define META_SETMAPMODE              0x0103
#define META_SETROP2                 0x0104
#define META_SETRELABS               0x0105
#define META_SETPOLYFILLMODE         0x0106
#define META_SETSTRETCHBLTMODE       0x0107
#define META_SETTEXTCHAREXTRA        0x0108
#define META_SETTEXTCOLOR            0x0209
#define META_SETTEXTJUSTIFICATION    0x020A
#define META_SETWINDOWORG            0x020B
#define META_SETWINDOWEXT            0x020C
#define META_SETVIEWPORTORG          0x020D
#define META_SETVIEWPORTEXT          0x020E
#define META_OFFSETWINDOWORG         0x020F
#define META_SCALEWINDOWEXT          0x0410
#define META_OFFSETVIEWPORTORG       0x0211
#define META_SCALEVIEWPORTEXT        0x0412
#define META_LINETO                  0x0213
#define META_MOVETO                  0x0214
#define META_EXCLUDECLIPRECT         0x0415
#define META_INTERSECTCLIPRECT       0x0416
#define META_ARC                     0x0817
#define META_ELLIPSE                 0x0418
#define META_FLOODFILL               0x0419
#define META_PIE                     0x081A
#define META_RECTANGLE               0x041B
#define META_ROUNDRECT               0x061C
#define META_PATBLT                  0x061D
#define META_SAVEDC                  0x001E
#define META_SETPIXEL                0x041F
#define META_OFFSETCLIPRGN           0x0220
#define META_TEXTOUT                 0x0521
#define META_BITBLT                  0x0922
#define META_STRETCHBLT              0x0B23
#define META_POLYGON                 0x0324
#define META_POLYLINE                0x0325
#define META_ESCAPE                  0x0626
#define META_RESTOREDC               0x0127
#define META_FILLREGION              0x0228
#define META_FRAMEREGION             0x0429
#define META_INVERTREGION            0x012A
#define META_PAINTREGION             0x012B
#define META_SELECTCLIPREGION        0x012C
#define META_SELECTOBJECT            0x012D
#define META_SETTEXTALIGN            0x012E
#define META_DRAWTEXT                0x062F

#define META_CHORD                   0x0830
#define META_SETMAPPERFLAGS          0x0231
#define META_EXTTEXTOUT              0x0a32
#define META_SETDIBTODEV             0x0d33
#define META_SELECTPALETTE           0x0234
#define META_REALIZEPALETTE          0x0035
#define META_ANIMATEPALETTE          0x0436
#define META_SETPALENTRIES           0x0037
#define META_POLYPOLYGON             0x0538
#define META_RESIZEPALETTE           0x0139


#define META_DELETEOBJECT            0x01f0

#define META_CREATEPALETTE           0x00f7
#define META_CREATEBRUSH             0x00F8
#define META_CREATEPATTERNBRUSH      0x01F9
#define META_CREATEPENINDIRECT       0x02FA
#define META_CREATEFONTINDIRECT      0x02FB
#define META_CREATEBRUSHINDIRECT     0x02FC
#define META_CREATEBITMAPINDIRECT    0x02FD
#define META_CREATEBITMAP            0x06FE
#define META_CREATEREGION            0x06FF


#define ETO_GRAYED      0x0001
#define ETO_OPAQUE      0x0002
#define ETO_CLIPPED     0x0004

#define LF_FACESIZE         32
typedef struct tagLOGFONT
{
    int     lfHeight;
    int     lfWidth;
    int     lfEscapement;
    int     lfOrientation;
    int     lfWeight;
    BYTE    lfItalic;
    BYTE    lfUnderline;
    BYTE    lfStrikeOut;
    BYTE    lfCharSet;
    BYTE    lfOutPrecision;
    BYTE    lfClipPrecision;
    BYTE    lfQuality;
    BYTE    lfPitchAndFamily;
    char    lfFaceName[LF_FACESIZE];
} LOGFONT;

/* weight values */
#define FW_DONTCARE         0
#define FW_THIN             100
#define FW_EXTRALIGHT       200
#define FW_LIGHT            300
#define FW_NORMAL           400
#define FW_MEDIUM           500
#define FW_SEMIBOLD         600
#define FW_BOLD             700
#define FW_EXTRABOLD        800
#define FW_HEAVY            900

#define FW_ULTRALIGHT       FW_EXTRALIGHT
#define FW_REGULAR          FW_NORMAL
#define FW_DEMIBOLD         FW_SEMIBOLD
#define FW_ULTRABOLD        FW_EXTRABOLD
#define FW_BLACK            FW_HEAVY

/* CharSet values */
#define ANSI_CHARSET        0
#define DEFAULT_CHARSET     1
#define SYMBOL_CHARSET      2
#define SHIFTJIS_CHARSET    128
#define HANGEUL_CHARSET     129
#define CHINESEBIG5_CHARSET 136
#define OEM_CHARSET         255

/* OutPrecision values */
#define OUT_DEFAULT_PRECIS      0
#define OUT_STRING_PRECIS       1
#define OUT_CHARACTER_PRECIS    2
#define OUT_STROKE_PRECIS       3

#define OUT_TT_PRECIS           4
#define OUT_DEVICE_PRECIS       5
#define OUT_RASTER_PRECIS       6
#define OUT_TT_ONLY_PRECIS      7


/* ClipPrecision values */
#define CLIP_DEFAULT_PRECIS     0x00
#define CLIP_CHARACTER_PRECIS   0x01
#define CLIP_STROKE_PRECIS      0x02
#define CLIP_MASK               0x0F

#define CLIP_LH_ANGLES          0x10
#define CLIP_TT_ALWAYS          0x20
#define CLIP_EMBEDDED           0x80


/* Quality values */
#define DEFAULT_QUALITY     0
#define DRAFT_QUALITY       1
#define PROOF_QUALITY       2

/* PitchAndFamily pitch values (low 4 bits) */
#define DEFAULT_PITCH       0x00
#define FIXED_PITCH         0x01
#define VARIABLE_PITCH      0x02

/* PitchAndFamily family values (high 4 bits) */
#define FF_DONTCARE         0x00
#define FF_ROMAN            0x10
#define FF_SWISS            0x20
#define FF_MODERN           0x30
#define FF_SCRIPT           0x40
#define FF_DECORATIVE       0x50


/* Stock fonts for use with GetStockObject() */
#define OEM_FIXED_FONT      10
#define ANSI_FIXED_FONT     11
#define ANSI_VAR_FONT       12
#define SYSTEM_FONT         13
#define DEVICE_DEFAULT_FONT 14
#define DEFAULT_PALETTE     15
#define SYSTEM_FIXED_FONT   16

/* Brush Styles */
#define BS_SOLID            0
#define BS_NULL             1
#define BS_HOLLOW           BS_NULL
#define BS_HATCHED          2
#define BS_PATTERN          3
#define BS_INDEXED          4
#define BS_DIBPATTERN       5

/* Binary raster ops */
#define R2_BLACK            1
#define R2_NOTMERGEPEN      2
#define R2_MASKNOTPEN       3
#define R2_NOTCOPYPEN       4
#define R2_MASKPENNOT       5
#define R2_NOT              6
#define R2_XORPEN           7
#define R2_NOTMASKPEN       8
#define R2_MASKPEN          9
#define R2_NOTXORPEN        10
#define R2_NOP              11
#define R2_MERGENOTPEN      12
#define R2_COPYPEN          13
#define R2_MERGEPENNOT      14
#define R2_MERGEPEN         15
#define R2_WHITE            16

/* Pen Styles */
#define PS_SOLID            0
#define PS_DASH             1
#define PS_DOT              2
#define PS_DASHDOT          3
#define PS_DASHDOTDOT       4
#define PS_NULL             5
#define PS_INSIDEFRAME      6

/* Text Alignment Options */
#define TA_NOUPDATECP                0x0000
#define TA_UPDATECP                  0x0001
#define TA_LEFT                      0x0000
#define TA_RIGHT                     0x0002
#define TA_CENTER                    0x0006
#define TA_TOP                       0x0000
#define TA_BOTTOM                    0x0008
#define TA_BASELINE                  0x0018

/* Background Modes */
#define TRANSPARENT     1
#define OPAQUE          2

#endif


#ifdef DEBUG
#include "mshell.h"
#endif
#define  G_LOCAL
#include "genplot.h"
#include "g_inter.h"

#ifdef   GENPLOT_
#undef   GENPLOT_
#endif
#define  GENPLOT_(x) wmf_##x
#include "g_inter1.h"

typedef struct tagMETASPECIALHEADER
{
    DWORD  key;		/* 0x9ac6cdd7 */
    WORD   handle;	/* 0x0000 */
    SHORT  left;
    SHORT  top;
    SHORT  right;
    SHORT  bottom;
    WORD   inch;
    DWORD  reserved;	/* 0x00000000 */
    WORD   checksum;
} METASPECIALHEADER;



#define PENTYPE		1
#define BRUSHTYPE	2
#define FONTTYPE	3

/*
 * Font names are 32 bytes at most, including the '\0' character
 */
static char *wmf_fonts[] =
{
    "Times New Roman",
    "Times New Roman Italic",
    "Times New Roman Bold",
    "Times New Roman Bold Italic",
    "Arial",
    "Arial Italic",
    "Arial Bold",
    "Arial Bold Italic",
    "Courier New",
    "Courier New Italic",
    "Courier New Bold",
    "Courier New Bold Italic",
    "Symbol"
};


/*---------*/
static int wmf_line_style;
static int wmf_line_width;
static int wmf_fg_color;
static int wmf_bk_color;

static int wmf_pen_ok;
static int wmf_brush_ok;
static int wmf_isclipped;
static int wmf_clipx1, wmf_clipx2, wmf_clipy1, wmf_clipy2;

#define MAXOBJECTS 20

static UINT wmf_max_objects;
static UINT wmf_objects[MAXOBJECTS];
static int  wmf_object_size;

#define MAGIC_NUMBER 0x9AC6CDD7

/* --- write little endian --- */
static void WriteDWORD(DWORD dw)
{
    fputc((int)( dw        & 0x000000FF), g_outfile);
    fputc((int)((dw >> 8)  & 0x000000FF), g_outfile);
    fputc((int)((dw >> 16) & 0x000000FF), g_outfile);
    fputc((int)((dw >> 24) & 0x000000FF), g_outfile);
}

static void WriteWORD(WORD w)
{
    fputc((int)( w       & 0x00FF), g_outfile);
    fputc((int)((w >> 8) & 0x00FF), g_outfile);
}

static void WriteBYTE(BYTE b)
{
    fputc((int)b, g_outfile);
}

static void WriteSTRING(char *s)
{
    fputs(s, g_outfile);
}


#define METASPECIALHEADERSIZE	22
static METASPECIALHEADER msh;
static void wmf_special_header(SHORT left, SHORT top, SHORT right, SHORT bottom)
{
    WORD *p, check;

    msh.key      = MAGIC_NUMBER;
    msh.handle   = 0;
    msh.left     = left;
    msh.top      = top;
    msh.right    = right;
    msh.bottom   = bottom;
    msh.inch     = HP_UNITS_PER_INCH;
    msh.reserved = 0;
    msh.checksum = 0;

    for (p = (WORD*) &msh; p < (WORD*) &(msh.checksum); p++)
	msh.checksum ^= *p;

    WriteDWORD(msh.key);
    WriteWORD(msh.handle);
    WriteWORD(msh.left);
    WriteWORD(msh.top);
    WriteWORD(msh.right);
    WriteWORD(msh.bottom);
    WriteWORD(msh.inch);
    WriteDWORD(msh.reserved);
    WriteWORD(msh.checksum);
}

#define METAHEADERSIZE	18
static void wmf_header(DWORD size, UINT obj_count, DWORD max_rec)
{
    METAHEADER mh;

    mh.mtType         = 1;
    mh.mtHeaderSize   = METAHEADERSIZE / sizeof(WORD);
    mh.mtVersion      = 0x0300;
    mh.mtSize         = size;
    mh.mtNoObjects    = obj_count;
    mh.mtMaxRecord    = max_rec;
    mh.mtNoParameters = 0;

    WriteWORD(mh.mtType);
    WriteWORD(mh.mtHeaderSize);
    WriteWORD(mh.mtVersion);
    WriteDWORD(mh.mtSize);
    WriteWORD(mh.mtNoObjects);
    WriteDWORD(mh.mtMaxRecord);
    WriteWORD(mh.mtNoParameters);
}


static void wmf_select_object(int type)
{
    int obj, delobj;

    for (obj=0;obj<MAXOBJECTS;obj++) {
	if (wmf_objects[obj] == 0) {
	    wmf_objects[obj] = type;
	    WriteDWORD(3+1);
            WriteWORD(META_SELECTOBJECT);
	    WriteWORD((SHORT) obj);
	    wmf_max_objects = max(wmf_max_objects, obj+1);
	    break;
	}
    }
    for (delobj=MAXOBJECTS-1;delobj>=0;delobj--) {
	if (obj != delobj && wmf_objects[delobj] == type) {
	    wmf_objects[delobj] = 0;
	    WriteDWORD(3+1);
	    WriteWORD(META_DELETEOBJECT);
	    WriteWORD((SHORT) delobj);
	    break;
	}
    }
}


static void wmf_create_pen(void)
{
    G_PALETTEENTRY g_palette;
    int width;

    if (wmf_pen_ok)
	return;
    if (wmf_line_style != PS_SOLID)
	width = 1;
    else
	width = wmf_line_width;;
    WriteDWORD(3+5);
    WriteWORD(META_CREATEPENINDIRECT);
    WriteWORD((SHORT) wmf_line_style);
    WriteWORD((SHORT) width);
    WriteWORD((SHORT) 0);
    INTERNAL_get_palette_color(&g_palette, wmf_fg_color);
    WriteBYTE(g_palette.r);
    WriteBYTE(g_palette.g);
    WriteBYTE(g_palette.b);
    WriteBYTE(0);
    wmf_select_object(PENTYPE);
    wmf_pen_ok = TRUE;
}

static void wmf_create_brush(void)
{
    G_PALETTEENTRY g_palette;

    if (wmf_brush_ok)
	return;
    WriteDWORD(3+4);
    WriteWORD(META_CREATEBRUSHINDIRECT);
    WriteWORD((SHORT) BS_SOLID);
    INTERNAL_get_palette_color(&g_palette, wmf_fg_color);
    WriteBYTE(g_palette.r);
    WriteBYTE(g_palette.g);
    WriteBYTE(g_palette.b);
    WriteBYTE(0);
    WriteWORD((SHORT) 0);
    wmf_select_object(BRUSHTYPE);
    wmf_brush_ok = TRUE;
}


static void wmf_obj_size(int size)
{
    wmf_object_size = max(wmf_object_size, size);
}

/**************************************************************/
float GENPLOT_(device_width)       = A4_WIDTH_IN_CM;
float GENPLOT_(device_height)      = A4_HEIGHT_IN_CM;
int   GENPLOT_(dots_per_cm)        = HP_UNITS_PER_CM;
int   GENPLOT_(y_is_upsidedown)    = TRUE;
int   GENPLOT_(able2clip)          = FALSE;
int   GENPLOT_(multi_windows)      = FALSE;


int GENPLOT_(open) (void)
{
    return G_OK;
}


void GENPLOT_(close) (void)
{
    ;
}

void GENPLOT_(open_window) (int id, long x0, long y0, long width, long height,
				   char *title, long flag)
{
    int i;

    id    = id;
    title = title;
    flag  = flag;

    wmf_special_header(1, 1, width+1, height+1);
    wmf_header(0, 0, 0);
/*
    WriteDWORD(3+2);
    WriteWORD(META_SETWINDOWORG);
    WriteWORD((SHORT) y0);
    WriteWORD((SHORT) x0);
    WriteDWORD(3+2);
    WriteWORD(META_SETWINDOWEXT);
    WriteWORD((SHORT) height+1);
    WriteWORD((SHORT) width+1);
*/
    WriteDWORD(3+1);
    WriteWORD(META_SETROP2);
    WriteWORD(R2_COPYPEN);
    for (i=0;i<MAXOBJECTS;i++)
	wmf_objects[i] = 0;

    wmf_line_style = PS_SOLID;
    wmf_line_width = 1;
    wmf_fg_color   = G_BLACK;
    wmf_bk_color   = G_WHITE;

    wmf_pen_ok    = FALSE;
    wmf_brush_ok  = FALSE;

    GENPLOT_(clipping) (FALSE);
    
    wmf_object_size = 5;


}



void GENPLOT_(close_window) (int id)
{
    long where;

    id = id;

    WriteDWORD(3);
    WriteWORD(0);
    where = ftell(g_outfile);
    fseek(g_outfile, METASPECIALHEADERSIZE, SEEK_SET);
    wmf_header(where/sizeof(WORD), wmf_max_objects, wmf_object_size);
    fseek(g_outfile, 0, SEEK_END);
}


void GENPLOT_(select_window) (int id)
{
    id = id;
}

void GENPLOT_(raise_window) (int id)
{
    id = id;
}

void GENPLOT_(moveto) (long x, long y)
{
    x = x;
    y = y;
}


void GENPLOT_(line) (long x1, long y1, long x2, long y2, int isclipped)
{
    isclipped = isclipped;

    wmf_create_pen();
    wmf_obj_size(3+2);
    WriteDWORD(3+2);
    WriteWORD(META_MOVETO);
    WriteWORD((SHORT) y1);
    WriteWORD((SHORT) x1);
    WriteDWORD(3+2);
    WriteWORD(META_LINETO);
    WriteWORD((SHORT) y2);
    WriteWORD((SHORT) x2);
}


void GENPLOT_(linewidth) (int width)
{
    wmf_line_width = width;
    wmf_pen_ok     = FALSE;
}

void GENPLOT_(linestyle) (unsigned pattern)
{
    switch (pattern) {
    case G_SOLID:
	wmf_line_style = PS_SOLID;
	break;
    case G_SHORT_DASHED:
	wmf_line_style = PS_DASHDOT;
	break;
    case G_LONG_DASHED:
	wmf_line_style = PS_DASH;
	break;
    case G_DOTTED:
	wmf_line_style = PS_DOT;
	break;
    }
    wmf_pen_ok = FALSE;
}


void GENPLOT_(label) (char *label)
{
    G_PALETTEENTRY g_palette;
    static LOGFONT lf;
    int i, pad, count;

    if (strlen(label) == 0)
        return;
    wmf_create_pen();
    if (TEXTROTATE)
	lf.lfEscapement = 900;
    else
	lf.lfEscapement = 0;
    if (INTERNAL_store_gc.fontscaling == G_RELATIVE_FONTSCALING)
	lf.lfHeight = (int) (INTERNAL_store_gc.charsize * WINHEIGHT / G_POINTSIZE_SCALING);
    else  
	lf.lfHeight = (int) (INTERNAL_store_gc.charsize * G_POINTSIZE * PS2HP_UNITS);
    if (strstr(wmf_fonts[INTERNAL_store_gc.font], "Italic"))
	lf.lfItalic = TRUE;
    else
	lf.lfItalic = 0;
    if (strstr(wmf_fonts[INTERNAL_store_gc.font], "Bold"))
	lf.lfWeight = FW_BOLD;
    else
	lf.lfWeight = 0;
    lf.lfCharSet    = DEFAULT_CHARSET;
    lf.lfPitchAndFamily = DEFAULT_PITCH;

    memset(lf.lfFaceName, 0, LF_FACESIZE);
    strcpy(lf.lfFaceName, wmf_fonts[INTERNAL_store_gc.font]);
    count = strlen(lf.lfFaceName);
    /*
    pad   = (count+1) % 2;
    wmf_obj_size(3+5+4+pad+(count+1)/2);
    WriteDWORD(3+5+4+pad+(count+1)/2);
    */
    pad   = 32-count;
    wmf_obj_size(3+5+4+32/2);
    WriteDWORD(3+5+4+32/2);
    WriteWORD(META_CREATEFONTINDIRECT);
    WriteWORD((SHORT) lf.lfHeight);
    WriteWORD((SHORT) lf.lfWidth);
    WriteWORD((SHORT) lf.lfEscapement);
    WriteWORD((SHORT) lf.lfOrientation);
    WriteWORD((SHORT) lf.lfWeight);
    WriteBYTE((BYTE)  lf.lfItalic);
    WriteBYTE((BYTE)  lf.lfUnderline);
    WriteBYTE((BYTE)  lf.lfStrikeOut);
    WriteBYTE((BYTE)  lf.lfCharSet);
    WriteBYTE((BYTE)  lf.lfOutPrecision);
    WriteBYTE((BYTE)  lf.lfClipPrecision);
    WriteBYTE((BYTE)  lf.lfQuality);
    WriteBYTE((BYTE)  lf.lfPitchAndFamily);
    WriteSTRING(lf.lfFaceName);
/*    pad++;*/
    for (i=0;i< pad;i++)
	WriteBYTE(0);
    wmf_select_object(FONTTYPE);

    WriteDWORD(3+1);
    WriteWORD(META_SETTEXTALIGN);
    WriteWORD(TA_LEFT | TA_BASELINE);
    WriteDWORD(3+2);
    WriteWORD(META_SETTEXTCOLOR);
    INTERNAL_get_palette_color(&g_palette, wmf_fg_color);
    WriteBYTE(g_palette.r);
    WriteBYTE(g_palette.g);
    WriteBYTE(g_palette.b);
    WriteBYTE(0);
    WriteDWORD(3+1);
    WriteWORD(META_SETBKMODE);
    WriteWORD(TRANSPARENT);

    count = strlen(label);
    pad   = count % 2;
    wmf_obj_size(3+3+(pad+count)/2);
    WriteDWORD(3+3+(pad+count)/2);
    WriteWORD(META_TEXTOUT);
    WriteWORD((SHORT) count+pad);
    WriteSTRING(label);
    for (i=0;i< pad;i++)
	WriteBYTE(' ');
    WriteWORD((SHORT)INTERNAL_store_vp.cy);
    WriteWORD((SHORT)INTERNAL_store_vp.cx);
}

void GENPLOT_(rectangle) (long x1, long y1, long x2, long y2)
{
    wmf_create_pen();
    wmf_obj_size(3+1+10);
    WriteDWORD(3+1+10);
    WriteWORD(META_POLYLINE);
    WriteWORD((SHORT) 5);
    WriteWORD((SHORT) x1);
    WriteWORD((SHORT) y1);
    WriteWORD((SHORT) x1);
    WriteWORD((SHORT) y2);
    WriteWORD((SHORT) x2);
    WriteWORD((SHORT) y2);
    WriteWORD((SHORT) x2);
    WriteWORD((SHORT) y1);
    WriteWORD((SHORT) x1);
    WriteWORD((SHORT) y1);
}

void GENPLOT_(drawpoly) (long numpoint, polytype * points)
{
    int i;

    if (numpoint < 2)
	return;
    wmf_create_pen();
    wmf_obj_size(3+1+numpoint*2);
    WriteDWORD(3+1+numpoint*2);
    WriteWORD(META_POLYLINE);
    WriteWORD((SHORT) numpoint);
    for (i=0; i < numpoint*2; i += 2) {
	WriteWORD((SHORT) points[i]);
	WriteWORD((SHORT) points[i+1]);
    }
}

void GENPLOT_(circle) (long r)
{
    wmf_create_pen();
    wmf_obj_size(3+8);
    WriteDWORD(3+8);
    WriteWORD(META_ARC);
    WriteWORD((SHORT) INTERNAL_store_vp.cy - (SHORT) r);
    WriteWORD((SHORT) INTERNAL_store_vp.cx - (SHORT) r);
    WriteWORD((SHORT) INTERNAL_store_vp.cy + (SHORT) r);
    WriteWORD((SHORT) INTERNAL_store_vp.cx + (SHORT) r);
    WriteWORD((SHORT) INTERNAL_store_vp.cy + (SHORT) r);
    WriteWORD((SHORT) INTERNAL_store_vp.cx + (SHORT) r);
    WriteWORD((SHORT) INTERNAL_store_vp.cy - (SHORT) r);
    WriteWORD((SHORT) INTERNAL_store_vp.cx - (SHORT) r);
    wmf_obj_size(3+8);
    WriteDWORD(3+8);
    WriteWORD(META_ARC);
    WriteWORD((SHORT) INTERNAL_store_vp.cy + (SHORT) r);
    WriteWORD((SHORT) INTERNAL_store_vp.cx + (SHORT) r);
    WriteWORD((SHORT) INTERNAL_store_vp.cy - (SHORT) r);
    WriteWORD((SHORT) INTERNAL_store_vp.cx - (SHORT) r);
    WriteWORD((SHORT) INTERNAL_store_vp.cy + (SHORT) r);
    WriteWORD((SHORT) INTERNAL_store_vp.cx + (SHORT) r);
    WriteWORD((SHORT) INTERNAL_store_vp.cy - (SHORT) r);
    WriteWORD((SHORT) INTERNAL_store_vp.cx - (SHORT) r);
}

void GENPLOT_(fillrectangle) (long x1, long y1, long x2, long y2)
{
    int oldstyle, oldwidth;

    oldstyle = wmf_line_style;
    oldwidth = wmf_line_width;
    wmf_line_style = PS_SOLID;
    wmf_line_width = 1;
    wmf_pen_ok    = FALSE;
    wmf_create_pen();
    wmf_create_brush();
    wmf_obj_size(3+1+8);
    WriteDWORD(3+1+8);
    WriteWORD(META_POLYGON);
    WriteWORD((SHORT) 4);
    WriteWORD((SHORT) x1);
    WriteWORD((SHORT) y1);
    WriteWORD((SHORT) x1);
    WriteWORD((SHORT) y2);
    WriteWORD((SHORT) x2);
    WriteWORD((SHORT) y2);
    WriteWORD((SHORT) x2);
    WriteWORD((SHORT) y1);
    wmf_line_style = oldstyle;
    wmf_line_width = oldwidth;
/*    wmf_delete_object(BRUSHTYPE); */
    wmf_pen_ok    = FALSE;
    wmf_brush_ok  = FALSE;
}

void GENPLOT_(fillpoly) (long numpoint, polytype * points)
{
    int i;
    int oldstyle, oldwidth;

    if (numpoint < 2)
	return;
    oldstyle = wmf_line_style;
    oldwidth = wmf_line_width;
    wmf_line_style = PS_SOLID;
    wmf_line_width = 1;
    wmf_pen_ok    = FALSE;
    wmf_create_pen();
    wmf_create_brush();
    wmf_obj_size(3+1+numpoint*2);
    WriteDWORD(3+1+numpoint*2);
    WriteWORD(META_POLYGON);
    WriteWORD((SHORT) numpoint);
    for (i=0; i < numpoint*2; i += 2) {
	WriteWORD((SHORT) points[i]);
	WriteWORD((SHORT) points[i+1]);
    }
    wmf_line_style = oldstyle;
    wmf_line_width = oldwidth;
/*    wmf_delete_object(BRUSHTYPE);*/
    wmf_pen_ok    = FALSE;
    wmf_brush_ok  = FALSE;
}

void GENPLOT_(fillcircle) (long r)
{
    int oldstyle, oldwidth;

    oldstyle = wmf_line_style;
    oldwidth = wmf_line_width;
    wmf_line_style = PS_SOLID;
    wmf_line_width = 1;
    wmf_pen_ok    = FALSE;
    wmf_create_pen();
    wmf_create_brush();
    wmf_obj_size(3+4);
    WriteDWORD(3+4);
    WriteWORD(META_ELLIPSE);
    WriteWORD((SHORT) INTERNAL_store_vp.cy + (SHORT) r);
    WriteWORD((SHORT) INTERNAL_store_vp.cx + (SHORT) r);
    WriteWORD((SHORT) INTERNAL_store_vp.cy - (SHORT) r);
    WriteWORD((SHORT) INTERNAL_store_vp.cx - (SHORT) r);
    wmf_line_style = oldstyle;
    wmf_line_width = oldwidth;
/*    wmf_delete_object(BRUSHTYPE);*/
    wmf_pen_ok    = FALSE;
    wmf_brush_ok  = FALSE;

}

void GENPLOT_(foreground) (int color)
{
    wmf_fg_color  = color;
    wmf_pen_ok    = FALSE;
}

void GENPLOT_(background) (int color)
{
    G_PALETTEENTRY g_palette;

    wmf_bk_color = color;
    wmf_create_pen();
    wmf_obj_size(3+2);
    WriteDWORD(3+2);
    WriteWORD(META_SETBKCOLOR);
    INTERNAL_get_palette_color(&g_palette, wmf_fg_color);
    WriteBYTE(g_palette.r);
    WriteBYTE(g_palette.g);
    WriteBYTE(g_palette.b);
    WriteBYTE(0);
    wmf_pen_ok = FALSE;
}

void GENPLOT_(clipping) (int clip)
{
    int x1, y1, x2, y2;

    wmf_isclipped = clip;
    if (clip) {
        INTERNAL_get_clip(&x1, &y1, &x2, &y2);
	wmf_clipx1 = (int) x1;
	wmf_clipy1 = (int) y1;
	wmf_clipx2 = (int) x2 + 1;
	wmf_clipy2 = (int) y2 + 1;
    }
    else {
	wmf_clipy2 = msh.bottom;
	wmf_clipx2 = msh.right;
	wmf_clipy1 = msh.top;
	wmf_clipx1 = msh.left;
    }
}

void GENPLOT_(newpage) (void)
{
    int color;
return;
/*
    color = wmf_fg_color;
    wmf_fg_color = wmf_bk_color;
    GENPLOT_(fillrectangle) (0, 0, WINWIDTH, WINHEIGHT);
    wmf_fg_color = color;
    */
}

void GENPLOT_(clearviewport) (void)
{
    int x1, x2, y1, y2;
    int color;

    INTERNAL_get_clip(&x1, &y1, &x2, &y2);
    color = wmf_fg_color;
    wmf_fg_color = wmf_bk_color;
    GENPLOT_(fillrectangle) (x1, y1, x2 + 1, y2 + 1);
    wmf_fg_color = color;
}

float GENPLOT_(fontheight) (int id)
{
    float size;

    if (INTERNAL_store_gc.fontscaling == G_RELATIVE_FONTSCALING)
	size = INTERNAL_store_gc.charsize / G_POINTSIZE_SCALING;
    else
	size = (INTERNAL_store_gc.charsize * G_POINTSIZE) /
	    g_dev[G_SCREEN].win[id].height;
    return size;
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


