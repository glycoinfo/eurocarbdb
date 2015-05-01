/************************************************************************/
/*                               g_inter1.h                             */
/*                                                                      */
/*  Platform : All                                                      */
/*  Module   : Used internally by genplot                               */
/*                                                                      */
/*  Albert van Kuik                                                     */
/************************************************************************/
int  GENPLOT_(open) (void);
void GENPLOT_(close) (void);
void GENPLOT_(open_window) (int id,long x0,long y0,long width,
				   long height,char *title,long flag);
void GENPLOT_(close_window) (int id);
void GENPLOT_(select_window) (int id);
void GENPLOT_(raise_window) (int id);
void GENPLOT_(moveto) (long x,long y);
void GENPLOT_(line) (long x1,long y1,long x2,long y2,int isclipped);
void GENPLOT_(linewidth) (int width);
void GENPLOT_(linestyle) (unsigned pattern);
void GENPLOT_(label) (char *label);
void GENPLOT_(rectangle)(long x1, long y1, long x2, long y2);
void GENPLOT_(drawpoly) (long numpoint, polytype *points);
void GENPLOT_(circle) (long r);
void GENPLOT_(fillrectangle)(long x1, long y1, long x2, long y2);
void GENPLOT_(fillpoly) (long numpoint, polytype *points);
void GENPLOT_(fillcircle) (long r);
float GENPLOT_(fontheight) (int id);
void GENPLOT_(foreground) (int color);
void GENPLOT_(background) (int color);
void GENPLOT_(clipping) (int clip);
void GENPLOT_(newpage) (void);
void GENPLOT_(clearviewport) (void);
int  GENPLOT_(palettesize)(int size);
int  GENPLOT_(paletteentry)(int entry_id, G_PALETTEENTRY entry);
extern float GENPLOT_(device_width);
extern float GENPLOT_(device_height);
extern int  GENPLOT_(dots_per_cm);
extern int  GENPLOT_(y_is_upsidedown);
extern int  GENPLOT_(able2clip);
extern int  GENPLOT_(multi_windows);
