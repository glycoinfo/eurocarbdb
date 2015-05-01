/************************************************************************/
/*                               gen_dumm.c                             */
/*                                                                      */
/*  Platform : All                                                      */
/*  Module   : Null device                                              */
/*                                                                      */
/*  Albert van Kuik                                                     */
/************************************************************************/
#include <stdio.h>
#include <stdlib.h>

#include "genplot.h"
#include "g_inter.h"

#ifdef   GENPLOT_
#undef   GENPLOT_
#endif
#define  GENPLOT_(x) dummy_##x


int GENPLOT_(open) (void)
{ return G_OK; }
void GENPLOT_(close) (void)
{ ; }
void GENPLOT_(open_window)(int id,long x0,long y0,long width,long height,
                          char *title,long flag)
{ id=id;x0=x0;y0=y0;width=width;height=height;
  title=title;flag=flag;}
void GENPLOT_(close_window)(int id)
{ id = id; }
void GENPLOT_(select_window)(int id)
{ id = id; }
void GENPLOT_(raise_window)(int id)
{ id = id; }
void GENPLOT_(moveto) (long x,long y)
{ x=x; y=y; }
void GENPLOT_(line) (long x1,long y1,long x2,long y2,int isclipped)
{ x1=x1; y1=y1; x2=x2; y2=y2; isclipped=isclipped; }
void GENPLOT_(linewidth) (int width)
{ width = width; }
void GENPLOT_(linestyle) (unsigned pattern)
{ pattern = pattern; }
void GENPLOT_(label) (char *label)
{ label = label; }
void GENPLOT_(rectangle)(long x1, long y1, long x2, long y2)
{ x1=x1; y1=y1; x2=x2; y2=y2; }
void GENPLOT_(drawpoly) (long numpoint, polytype *points)
{ numpoint = numpoint; points = points; }
void GENPLOT_(circle) (long r)
{ r = r; }
void GENPLOT_(fillrectangle)(long x1, long y1, long x2, long y2)
{ x1=x1; y1=y1; x2=x2; y2=y2; }
void GENPLOT_(fillpoly) (long numpoint, polytype *points)
{ numpoint = numpoint; points = points; }
void GENPLOT_(fillcircle) (long r)
{ r=r; }
float GENPLOT_(fontheight) (int id)
{ id = id; return 1.0;}
void GENPLOT_(foreground) (int color)
{ color=color; }
void GENPLOT_(background) (int color)
{ color=color; }
void GENPLOT_(clipping) (int clip)
{ clip = clip; }
void GENPLOT_(newpage) (void)
{ ; }
void GENPLOT_(clearviewport) (void)
{ ; }
int GENPLOT_(palettesize)(int size)
{ size = size; return G_OK; }
int GENPLOT_(paletteentry)(int entry_id, G_PALETTEENTRY entry)
{ entry_id = entry_id; entry = entry; return G_OK; }

float GENPLOT_(device_width)          = 100;
float GENPLOT_(device_height)         = 100;
int   GENPLOT_(dots_per_cm)           = 100;
int   GENPLOT_(y_is_upsidedown)       = FALSE;
int   GENPLOT_(able2clip)             = TRUE;
int   GENPLOT_(multi_windows)         = TRUE;


