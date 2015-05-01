/************************************************************************/
/*                                 psnd.h                               */
/*                                                                      */
/* 1997, Albert van Kuik                                                */
/*                                                                      */
/************************************************************************/

#ifndef PSND_H
#define PSND_H

#ifdef MSHELL
#include "mshell.h"
#endif

#ifndef M_PI
#define M_PI 3.14159265358979323846
#endif

#define min(a,b)        (((a) < (b)) ? (a) : (b))
#define max(a,b)        (((a) > (b)) ? (a) : (b))
#define round(f)        ((int)(((f) < 0.0) ? (ceil((f)-0.5)) : (floor((f)+0.5))))
#define inside(a,b,c)	(((b) > (a)) ? (((b) < (c)) ? (b) : (c)) : (a))
#define sign(a,b)       ((b)<0 ? -fabs(a) : fabs(a))
#define iswap(a,b)	{int   c=(a);(a)=(b);(b)=c;}
#define fswap(a,b)	{float c=(a);(a)=(b);(b)=c;}

#define AXIS_SCALE(a)    (((a) <= 0) ? (1-(a)) : (1/(1+(a))))
#define UN_AXIS_SCALE(a) (((a) >= 1) ? (-(a)+1) : (1/(a)-1))

#define MAX_BLOCK	12	/* hard limit */
#define MAXBLK	9
#define NUMBLK	4
#define MAXDIM	3
#ifndef OLD3D_MAX_DIM
#define OLD3D_MAX_DIM    MAXDIM
#endif
#define MAX_DIM           5



#define DEFSIZ		(32*1024)
#define MAXSIZ		(128*1024)
#define MAXSI1          (512*1024)
#define MAXSI2          (32*1024)
#define MAXSI3          (4*1024)
#define MAXDAT		MAXSI2
#define MAXCPR		(1024*257)
#define MAXLEV		32
#define MAXUNT 		99		/* maximum allowed unit number */


#define MAXPAR 		1024

#define MAXBUF 		(512*256)

#define TOGGLE		-1



#define FLAG_POSITIVE	1
#define FLAG_NOTZERO	2
#define FLAG_ATLEAST_1	3

#define CONTOUR_NEW_FILE	(1<<0)
#define CONTOUR_PLOT		(1<<2)
#define CONTOUR_PLOT_APPEND	(1<<3)
#define CONTOUR_BLOCK		(1<<4)
#define CONTOUR_BLOCK_NEW	(1<<5)
#define CONTOUR_PLOT_TO_DISK	(1<<6)
#define CONTOUR_STACK		(1<<7)
#define CONTOUR_PLOT_1X1		(1<<8)

typedef enum {
    PLOT_1D = 1,
    PLOT_2D
} plot_modes;

typedef enum {
    CPLOT_BLOCK,
    CPLOT_CONTOUR,
    CPLOT_CONTOUR_1X1
} contour_plot_modes;


#include "psnd_defs.h"

#define DEF_ITERMS	5
#define DEF_RSH		2.0
#define DEF_GN		8.029E-23
#define DEF_KC		64.0
#define DEF_IOPT	3
#define DEF_NFUT	512
#define DEF_NPOLES	5
#define DEF_TOLER	1e-7
#define DEF_FTSCALE	0.5


#ifdef abs
#undef abs
#endif
#define abs(x) 	(((x) >= 0) ? (x) : (-x))

#define PAR	((mblk->par[mblk->info->block_id])+mblk->info->dimension_id)
#define PAR0(i)	((mblk->par[mblk->info->block_id])+(i))
#define DAT	(mblk->dat[mblk->info->block_id])

/*
 * parameters in 2D mode x,y,z
 */
#define PAR1	(PAR0(DAT->access[0]-1))
#define PAR2	(PAR0(DAT->access[1]-1))
#define PAR3	(PAR0(DAT->access[2]-1))


/*
 * Parameter block, one per dimension
 */
typedef struct param_block_struct {
    int   icrdir;	/* direction				*/
    int   key;		/* current record			*/
    int   nsiz;		/* record size				*/
    int   icmplx;	/* complex flag, 1 = true, 0 = false	*/
    int   isspec;	/* spectrum flag, 0=fid, 1=spectrum	*/
    int   dspflag;	/* 0=noshift, 1=doshift			*/
    float dspshift;	/* Points to shift for ft in dsp FID	*/
    int   td;		/* Time domain				*/
    float pxref;	/* reference in ppm  			*/
/* */
    float sfd;		/* spectral frequency (each domain)	*/
    float swhold;	/* spectral width			*/
    float xref;		/* reference				*/
    float aref;		/* ref = aref * xref + bref		*/
    float bref;		/* ref = aref * xref + bref		*/
/* pre */
    int   ipre;		/* 0=no pre      1=pre on real		*/
			/* 2=pre on imag 3=pre on complex	*/
    int   irstrt;	/* read start				*/
    int   irstop;	/* read stop				*/
/* window */
    int   nwindo;	/* 0=no window 1=do window		*/
    int   iwindo;	/* 0=no window 1=window			*/
			/* -1=window performed			*/
    float rsh;		/* right shift 				*/
    float rlb;		/* line broadening			*/
    float gn;		/* gaussian broadening			*/
    int   itm;		/* # channels for trapezian window	*/
    int   iwstrt;	/* start window multiplication		*/
    int   iwstop;	/* stop window muliplication		*/
/* ff */
    int   nfft;		/* 0=no fft, 1=do fft			*/
    int   ifft;		/* 0=no fft      1=fft on real		*/
    			/* 2=fft on imag 3=fft on complex	*/
    int   nzf;		/* zero filling				*/
    int   icstrt;	/* start after FFT, for internal usage	*/
    int   icstop;	/* stop after FFT, for internal usage	*/
    float ftscale;	/* scale factor of 1st data point	*/
/* phase */
    int   iphase;      /* 0=nophase,1=phase,-1=phasedone 	*/
    float ahold;	/* frequency independent phase corr. 	*/
    float bhold;	/* frequency dependent phase corr. 	*/
    int   ihold;	/* I zero channel			*/
/* post */
    int   ipost;	/* 0=no post     1=post on real		*/
			/* 2=post on imag 3=post on complex	*/
/* reverse */
    int   irever;	/*  0= no reverse 			*/
    			/*  1= reverse array 			*/
                        /*  2= reverse records 			*/
                        /*  3= reverse both 			*/
                        /* -1= reverse array done 		*/
                        /* -2= reverse records done 		*/
                        /* -3= reverse both done		*/
/* baseline */
    int   nbase;	/* 0=nobaseline, 1=dobaseline		*/
    int   ibase;	/* 0=nobaseline				*/
    			/* <>0 baseline type ibase		*/
			/* -1*ibase=baseline type ibase done	*/
    int   iterms;	/* # polynomal baseline terms		*/
    int   iterms2;	/* # polynomal baseline terms		*/
    int   ibstrt;	/* start point baseline correction	*/
    int   ibstop;	/* stop point baseline correction	*/
    int   ibwater;
    int   ibwidth;
/* outfile */
    int   ifilut;	/* 0=new file,1=output=input,3=old file	*/
    int   isstrt;	/* store start				*/
    int   isstop;	/* store stop				*/
/* linear prediction */
    int   nlpc;		/* 0=no, 1=yes				*/
    int   lpc;		/* 0=no, 1=real, 2=complex		*/
    int   lpcmode;	/* 0=forward, 1=backward, 2=inter	*/
    int   npoles;	/* Number of roots to predict		*/
    int   nfut;		/* Size to predict			*/
    int   mroot;	/* Move roots				*/
    int   replace;	/* Replace FID with predicted values	*/
    float toler;	/* Tolerance for SVD			*/
    int   ngap1;	/* lpc gap start			*/
    int   ngap2;	/* lpc gap stop				*/
    int   nstart;	/* lpc start				*/
    int   nstop;	/* lpc stop				*/
/* watwa */
    int   watwa;	/* 0=no, 1=yes. Do Water Wash		*/
    int   iopt;		/* cos to the power of iopt+1		*/
    float kc;		/* convolution width			*/
    float wshift;	/* shift biggest peak to middle		*/
/* Hilbert Transformation */
    int   hilbert;	/* 0=no, 1=yes. Do Hilbert Transform	*/
/* Water fit */
    int   waterfit;	/* 0=no, 1=yes. Do Water Fit		*/
    int   waterpos;	/* Position of the water line		*/
    int   waterwid;	/* Width of the water line		*/
} PBLOCK;


typedef enum {
    VP_MAIN,
    VP_PHASE_2D,
    VP_PHASE1,
    VP_PHASE2,
    VP_PHASE3,
    VP_PHASE_2D_BORDER,
    VP_PHASE1_BORDER,
    VP_PHASE2_BORDER,
    VP_PHASE3_BORDER,
    VP_XAXIS,
    VP_YAXIS,
    VP_MAX
} vp_ids;

typedef struct {
    int   store_id;
    float x1,x2,y1,y2;
    float wx1,wx2,wy1,wy2;
} VP_STORE;
 

typedef enum {
    S_CONTOUR,
    S_CONTOUR_BORDER,
    S_REAL,
    S_REALSWAP,
    S_IMAG,
    S_BUFR1,
    S_BUFR2,
    S_WINDOW,
    S_BASELN,
    S_AXISX,
    S_AXISX_MARK,
    S_AXISY,
    S_AXISY_MARK,
    S_AXISXY_MARK,
    S_BOX,
    S_GRID,
    S_PHASE1,
    S_PHASE2,
    S_PHASE3,
    S_PHASE1_BORDER,
    S_PHASE2_BORDER,
    S_PHASE3_BORDER,
    S_PEAKPICK,
    S_INT2D,
    S_SCRATCH2D,
    S_2DMARKER,
    S_UPDATE,
    S_HARDCOPY,
    S_COLORMAP,
    S_MAX
} show_ids;

typedef enum {
    X_SCALE,
    X_MOVE,
    Y_SCALE,
    Y_MOVE
} adjust_ids;

typedef enum {
    WX1,
    WY1,
    WX2,
    WY2
} master_world_coor;

typedef struct plotinfo_struct {
    int   show;
    int   obj_id;
    int   vp_id;
    int   vp_id_master;
    int   *vp_id_block_master;
    int   vp_group;
    int   scroll_flag;
    int   color;
    int   color2;
    int   color3;
    int   disable_auto_scale_2d;
    float dxy[4];
    float wxy[4];
} SBLOCK;


#define ZOOMUNDOQMAX 50

typedef struct zoom_undoinfo {
    int   vp_id;
    float xy[4];
} ZOOMUNDOINFO;

#define CROPUNDOMAX	2

typedef struct crop_undoinfo {
    ZOOMUNDOINFO zoomundo_queue[ZOOMUNDOQMAX+1];
    int   z_undohead;
    int   z_undotail;
    int   z_redo;
    int   z_lock;
} CROPUNDOINFO;

typedef enum {
    PLANE_THIS,
    PLANE_UP,
    PLANE_DOWN,
    PLANE_READ,
    PLANE_RESET,
    PLANE_UNDO,
    PLANE_REDO
} plane_read_ids;

#define UNDOQMAX 100

typedef struct undoinfo {
    int key[MAX_DIM];
    PBLOCK *par[MAX_DIM];
} UNDOINFO;

typedef struct undoplaneinfo {
    int key[MAX_DIM];
    int access[MAX_DIM];
    PBLOCK *par[MAX_DIM];
} UNDOPLANEINFO;

#define BOOKMARKQMAX 100

typedef struct bookmarkinfo {
    char   label[100];
    int    key[MAX_DIM];
    PBLOCK *par[MAX_DIM];
    struct bookmarkinfo *next;
} BOOKMARKINFO;

#define INT2DQMAX	100
#define INT2DLABELMAX	150
#define INT2DCENTMAX	4

typedef struct int2dinfo {
    char   label[INT2DLABELMAX];
    float  dint;
    int    npeaks;
    int    centx[INT2DCENTMAX], centy[INT2DCENTMAX];
    float  level[INT2DCENTMAX];
    int    box[4];
} INT2DINFO;

typedef enum {
    PEAKPICK_FIND,
    PEAKPICK_REMOVE,
    PEAKPICK_MERGE
} peakpick_ids;


#define LEVELS_POSITIVE	0
#define LEVELS_BOTH	1
#define LEVELS_NEGATIVE	2

typedef struct contour_struct {
    int    once;		/* initialization flag			*/
    int    silent;	/* run in background			*/
    int    mode;
    int    phase_direction;
    int    ipre;
    float  lowest;
    float  highest;
    float  rmsnoise;
    PBLOCK *par1, *par2;

    /* constants */
    int    maxcpr;	/* max size working array xcpr		*/
    int	   maxlev;	/* max contour levels 			*/
    int    maxdat;	/* max data in work arrays (ie, max record length)		*/

    /*  */
    int    ihamin;	/* countour bounderies 			*/
    int    ihamax;
    int    ihbmin;
    int    ihbmax;
    int    ilowa;	/* [ilowa..ihigha] = field in reading direction */
    int    ihigha;
    int    ilowb;	/* [ilowb..ihighb] = field for record keys */
    int    ihighb;
    int    iadiv;	/* reading interval in direction A	*/
    int    ibdiv;	/* key     interval in direction B	*/
    int    mlevel;	/*  level mode				*/
                        /*    0=explicit			*/
			/*    1=use factor			*/
			/*    2=equidistant			*/
			/*    3=log-range			*/
    int    nlevel;	/*  size of clevel			*/
    float  flevel;	/*  level factor: flev			*/

    /* arrays */
    float  clevel[MAXLEV];	/*  array of contour levels	*/
    int    levcol[MAXLEV];	/*  array of contour level colours	*/

    /* local params */
    float  dlevel;	/*  Distance for 'Equidistant' mode	*/
    float  levelmax;	/*  Highest level for Log-range mode	*/
    int    colormode;	/*  How are the contour colors defined 	*/
    			/*  0 = same color			*/
                        /*  1 = pos/neg				*/
                        /*  2 = define all			*/
                        /*  3 = automatic			*/
    int    plusmin;	/*  show +/- levels			*/
    			/*  0 = positive levels			*/
                        /*  1 = pos & neg levels		*/
                        /*  2 = negative levels			*/


    /* work arrays */
    float  *xdata;
    char   *bits;	/* logical array for contour algorithm	*/
    int    ncpr;	/* size of xcpr	buffer			*/
    float  *xcpr;	/* linear array containing data		*/
    int    ial, iah, ibl, ibh, iadv, ibdv;
                        /* current sizes and reading interval of xcpr buffer */

    /* phase stuff */
    int    doprocess;	/* do processing before phase		*/
    int    phase_size;
    int    phase_data_added[3];
    float  *xreal[3];
    float  *ximag[3];
    
    /* integration stuff */
    int    intmod;	/* integration flag			*/
    			/* 0 = conventional integration, with/without zero offset */
                        /* 1 = flood-fill integration		*/
                        /* 2 = cross-integration		*/
                        /* 3 = complete cross-integration with error estimate	*/
    int    intlevel;	/* integration level			*/
    			/* 0 = positive peaks 			*/
                        /* 1 = positive and negative peaks	*/
                        /* 2 = negative peaks			*/
    int    zoint;	/* logical for offset correction in integration */
    float  dint;	/* integral of field (+ or - offset)	*/
    float  scale_i;	/* divide dint by this number		*/
    float  zo;		/* offset				*/
    float  ref;		/* integral reference distance		*/
    float  vref;	/* integral reference rate		*/
    float  rref;	/* abs(vref)*(ref **6.) 		*/
    int    nshift;	/* max channel origin search		*/
    int    pinpoi;	/* user pinpoint peak maximum 		*/
    int    primax;	/* print peak maximum 			*/
    int    pribox;	/* print box coordinates		*/
    int    box[4];	/* box coordinates(channels) x1,x2,y1,y2*/
    int    centx, centy;
    INT2DINFO *int2d_queue;
    int    i_count;
    int    i_size;
    int    i_select;
    int    i_label;
    int    ppmode;	/* Peak Pick mode	*/
} CBLOCK;


#define INFILE	0
#define OUTFILE	1

typedef struct file_info {
    char  *name;		/* fortan file name			*/
    int   file_type;		/* input:  0 for OLD3D, 1 for ND	*/
    int   read_only;		/* open mode flag			*/
    int   fopen;		/* fortan file open flag		*/
    int   ifile;		/* fortan file units			*/
    void  *ndfile;		/* iond file pointer to input file	*/
    int   sizeof_float;  	/* iond file float type (4,3 or 2)	*/
    int   tresh_flag;		/* iond file uses-truncated-level-flag	*/
    float tresh_levels[3];	/* iond file ,the 3 trunc levels	*/
} FILE_INFO;

/*
 * Data block, one per process block
 */
typedef struct data_block_struct {
    int       id;		/* number of this block			*/
    int       ityp;		/* number of dimensions			*/
    int       ndim;		/* number of dimensions > 1		*/
    float     *xreal;		/* Pointer to real data			*/
    float     *ximag;		/* Pointer to imaginary data		*/
    float     *xbufr1;		/* Pointer to buffer A			*/
    float     *xbufr2;		/* Pointer to buffer B			*/
    float     *window;		/* Pointer to window buffer		*/
    float     *baseln;		/* Pointer to baseline buffer		*/
    float     *work1;		/* Pointer to shared buffer		*/
    float     *work2;		/* Pointer to shared buffer		*/
    float     sw;		/* Current sw				*/
    int       irc;		/* Current irstart			*/
    int       *access;		/* Pointer to array of directions	*/
    int       iaqdir;		/* aquisition dir			*/
    int       nextdr;		/* Process in direction			*/
    int       isize;		/* The current size of xreal		*/
    int       isspec;		/* The current fid/spec flag		*/
    int       npar;		/* Size of the parameter record	(bytes) */
    int       nt0par;		/* Size of param record header (floats)	*/
    int       ntnpar;		/* Size of 1 parameter record	(floats)*/
    float     *xpar;		/* Pointer to parameter record 		*/
    float     ylimit[2];	/* ymin and ymax			*/
    PBLOCK    *pars[MAX_DIM];	/* Parameters ordened acording to access*/
    PBLOCK    *par0[MAX_DIM];	/* Parameters ordened chronologically	*/
    int       nsizo[MAX_DIM];	/* Sizes outputfile			*/
    int       cmplxo[MAX_DIM];	/* Complex outputfile			*/
    char      *prompt;		/* Pointer to command line prompt	*/
    FILE_INFO finfo[2];
    UNDOINFO  undo_queue[UNDOQMAX+1];
    int       q_undohead;
    int       q_undotail;
    int       q_redo;
    int       q_lock;
    UNDOPLANEINFO  undo_plane_queue[UNDOQMAX+1];
    int       q_plane_undohead;
    int       q_plane_undotail;
    int       q_plane_redo;
    int       q_plane_lock;
    CROPUNDOINFO crop[CROPUNDOMAX];
    int       c_undotail;
    BOOKMARKINFO *bookmark_queue;
    int       b_count;
    int       b_size;
    int       b_select;
} DBLOCK;


typedef enum {
    MOUSE_MEASURE=1,
    MOUSE_SELECT,
    MOUSE_POSITION,
    MOUSE_ZOOM,
    MOUSE_ZOOMX,
    MOUSE_ZOOMY,
    MOUSE_I0,
    MOUSE_P0,
    MOUSE_P1,
    MOUSE_INTEGRATION,
    MOUSE_CALIBRATE,
    MOUSE_DISTANCE,
    MOUSE_BUFFER1,
    MOUSE_BUFFER2,
    MOUSE_ROWCOLUMN,
    MOUSE_ADDROWCOLUMN,
    MOUSE_PLANEROWCOLUMN,
    MOUSE_NOISE,
    MOUSE_CONTOURLEVEL,
    MOUSE_PEAKPICK,
    MOUSE_LINEFIT,
    MOUSE_TRANSITION,
    MOUSE_DISPLAY,
    MOUSE_3D,
    MOUSE_WATWA,
    MOUSE_WATERFIT,
    MOUSE_BASELINE
} mouse_modes;


typedef enum  {
    ID_MOUSE_POS = 500,
    ID_MOUSE_SELECT, 
    ID_MOUSE_REGION, 
    ID_MOUSE_SPLINE, 
    ID_MOUSE_ZOOMXY,
    ID_MOUSE_ZOOMX, 
    ID_MOUSE_ZOOMY,
    ID_MOUSE_PHASE_P0,
    ID_MOUSE_PHASE_P1,
    ID_MOUSE_PHASE_I0,
    ID_MOUSE_CALIBRATE,
    ID_MOUSE_INTEGRATE,
    ID_MOUSE_PEAKPICK,
    ID_MOUSE_DISTANCE,
    ID_MOUSE_SCALE,
    ID_MOUSE_NOISE, 
    ID_MOUSE_ROWCOL, 
    ID_MOUSE_SUM, 
    ID_MOUSE_ROWCOL_PLANE,
} mouse_bar_ids;


typedef enum {
    MENU_FILE_ID,
    MENU_PARAM_ID,
    MENU_PROCESS_ID,
    MENU_DISPLAY_ID,
    MENU_DATA_ID,
    MENU_OPTIONS_ID,
    MENU_ARRAY_FILL_ID,
    MENU_ARRAY_CHANGE_ID,
    MENU_PAR_LIST_ID,
    MENU_CORRECT_ID,
    MENU_BASELINE_ID,
    MENU_WINDOW_ID,
    MENU_MAX_ID
} info_menu_ids;


typedef struct local_info_struct {
    int   foreground;
    int   verbose;
    int   win_id;
    int   colorwin_id;
    int   colorblock_id;
    int   colorarray_id;
    int   block_id;
    int   block_size;
    int   max_block;
    int   global_vp_id;
    int   global_vp_group_id;
    int   menubar_id;
    int   menu_id[MENU_MAX_ID];
    int   dimension_id;
    int   plot_mode;
    int   contour_plot_mode;
    int   bar_id;
    int   mousebar_id;
    int   colorbar_id;
    int   mouse_select_hint;
    int   phase_id;
    int   mouse_mode1;
    int   mouse_mode2;
    int   mouse_mode3;
    int   auto_scale;
    int   auto_scale_2d;
    int   contour_row;
    int   append_plot;
    int   phase_2d;
    int   adjust_display_item;
    int   adjust_display_block;
    int   adjust_display_operation;
    int   adjust_display_sensitive;
    float txminx;	/*	viewport limits 	*/
    float txmaxx;
    float txminy;
    float txmaxy;

    int   ismaxx;
    int   ismaxy;
    int   version;
    int   dirmasksize;
    char  *dirmask;	/* current directory	*/
} INFO;

typedef enum  {
    AXIS_UNITS_NONE, 
    AXIS_UNITS_CHANNELS, 
    AXIS_UNITS_HERTZ, 
    AXIS_UNITS_PPM, 
    AXIS_UNITS_SECONDS, 
    AXIS_UNITS_AUTOMATIC
} axis_units;

typedef struct {
    int   flag;
    int   is_integerx;
    int   is_integery;
    float xmin, xmax; 
    float ymin, ymax; 
    int   iymode, ixmode;
    char  xtext[20];
    char  ytext[20];
} AXIS_UNIT_INFO;

#define DIRECTION_PERPENDICULAR	-4
#define DIRECTION_ROW		-2
#define DIRECTION_COLUMN	-3
#define DIRECTION_SWITCH 	-1

#define PLOT_COLUMN		0
#define PLOT_ROW		1
#define PLOT_ROW_COLUMN		2
#define PLOT_PERPENDICULAR	3

#define AUTO_SCALE_OFF		0
#define AUTO_SCALE_ON		1
#define AUTO_SCALE_FULL		2
#define AUTO_SCALE_OFF_XY	3
#define AUTO_SCALE_COPY		4

#define PLOT_XAXIS		(1<<0)
#define PLOT_YAXIS		(1<<1)
#define PLOT_BOX		(1<<2)
#define PLOT_GRID		(1<<3)
#define PLOT_ALL		(PLOT_XAXIS + PLOT_YAXIS + PLOT_BOX + PLOT_GRID)


typedef enum {
    DUMMY_INIT,
    OUTPUT_TYPE,
    POPUP_PAPER_SIZES,
    PAPER_SIZE_WIDTH,
    PAPER_SIZE_HEIGHT,
    PAPER_TYPE,
    PAPER_ORIENTATION,
    OPTIONS,
    PEN_WIDTH,
    DEV_WIDTH,
    DEV_HEIGHT,
    OFFX,
    OFFY,
    WIDTH,
    HEIGHT,
    UNITSX,
    UNITSY,
    CLOFFX,
    CLOFFY,
    XMINV,
    XMAXV,
    YMINV,
    YMAXV,
    XMINPV,
    XMAXPV,
    YMINPV,
    YMAXPV,
    HZX,
    HZY,
    AXTXT,
    AYTXT,
    AXIS_SETUP,
    PLOT_TITLE,
    ADDTO_TITLE,
    SHOW_TITLE,
    SHOW_XAXIS,
    POPUP_XAXIS,
    XLABELS_PPM,
    XTICKS_AUTOSCALE,
    XTICKS_PPM_SHORT,
    XTICKS_PPM_MEDIUM,
    XTICKS_PPM_LONG,
    XTICKLEN_SHORT,
    XTICKLEN_MEDIUM,
    XTICKLEN_LONG,
    XOFF_AXIS,
    XOFF_LABELS,
    XOFF_TITLE,
    XSTART_TITLE,
    SHOW_YAXIS,
    POPUP_YAXIS,
    YLABELS_PPM,
    YTICKS_AUTOSCALE,
    YTICKS_PPM_SHORT,
    YTICKS_PPM_MEDIUM,
    YTICKS_PPM_LONG,
    YTICKLEN_SHORT,
    YTICKLEN_MEDIUM,
    YTICKLEN_LONG,
    YOFF_AXIS,
    YOFF_LABELS,
    YOFF_TITLE,
    YSTART_TITLE,
    FIX,
    IBOX,
    IGRID,
    XGRIDLINES_PPM,
    YGRIDLINES_PPM,
    PEAKPICK,
    THRESH,
    SENS,
    SIGN,
    AVEWIDTH,
    LHEIGHT,
    LWIDTH,
    NPART,
    LYMAX,
    PREVIEW,
    SAVESETTINGS,
    READSETTINGS
} conplotq_ids;

#define AUTO_SCALE		0
#define FIXED_HZ_CM		1
#define FILL_PAPER		2

#define A4_WIDTH_IN_CM                29.7
#define A4_HEIGHT_IN_CM               21.0
#define A3_WIDTH_IN_CM                42.0
#define A3_HEIGHT_IN_CM               29.7
#define LETTER_WIDTH_IN_CM            27.94
#define LETTER_HEIGHT_IN_CM           21.59
#define LEGAL_WIDTH_IN_CM             35.56
#define LEGAL_HEIGHT_IN_CM            21.59

#define AXIS_LABEL_LENGTH	255
typedef struct {
    int   init;
    int   cont_id;
    int   is_2d;
    PBLOCK *par1;
    PBLOCK *par2;
    DBLOCK *dat;
    int   device;
    int   option;
    int   paper_id;
    int   pen_width;
    float dev_width;		/* paper width			*/
    float dev_height;		/* paper height			*/
    float offx;			/* paper clip offset x		*/
    float offy;			/* paper clip offset y		*/
    float width;		/* paper clip width		*/
    float height;		/* paper clip height		*/
    float clentx;		/* offx + width			*/
    float clenty;		/* offy + height		*/
    float cminx;		/* offx				*/
    float cminy;		/* offy				*/
    float cmaxx;		/* xmax of plot box		*/
    float cmaxy;		/* ymax of plot box		*/
    int   nqx;			/* number of sub-plots in x dir	*/
    int   nqy;			/* number of sub-plots in y dir	*/
    int   output_type;
    int   paper_type;
    int   paper_orientation;
    int   options[3];
    int   unitsx;
    int   unitsy;
    float cloffx;		/* y axis height		*/
    float cloffy;		/* x axis height		*/
    float xminv;		/* min x in window units	*/
    float xmaxv;		/* max x in window units	*/
    float yminv;		/* min y in window units	*/
    float ymaxv;		/* max y in window units	*/
    float xminv_store;
    float xmaxv_store;
    float yminv_store;
    float ymaxv_store;
    float xminpv;
    float xmaxpv;
    float yminpv;
    float ymaxpv;
    float xminhv;
    float xmaxhv;
    float yminhv;
    float ymaxhv;
    float xminsv;
    float xmaxsv;
    float yminsv;
    float ymaxsv;
    float hzx;
    float hzy;
    char  axtxt[AXIS_LABEL_LENGTH+1];
    char  aytxt[AXIS_LABEL_LENGTH+1];
    char  plot_title[AXIS_LABEL_LENGTH+1];
    int   show_title;
    int   show_xaxis;
    int   xticks_autoscale;
    float xlabels_ppm;
    float xticks_ppm_short;
    float xticks_ppm_medium;
    float xticks_ppm_long;
    float xticklen_short;
    float xticklen_medium;
    float xticklen_long;
    float xoff_axis;
    float xoff_labels;
    float xoff_title;
    float xstart_title;
    int   show_yaxis;
    int   yticks_autoscale;
    float ylabels_ppm;
    float yticks_ppm_short;
    float yticks_ppm_medium;
    float yticks_ppm_long;
    float yticklen_short;
    float yticklen_medium;
    float yticklen_long;
    float yoff_axis;
    float yoff_labels;
    float yoff_title;
    float ystart_title;
    int   fix;
    int   ibox;
    int   igrid;
    float xgridlines_ppm;
    float ygridlines_ppm;
    int   peakpick;
    float thresh;
    float sens;
    int   sign;
    float avewidth;
    float lheight;		/* 1d peakpick label height	*/
    float lwidth;		/* 1d peakpick box height	*/
    float twidth;		/* height of title box		*/
    int   npart;
    float lymax;		/* top of 1d peakpick label box */
    float tymax;		/* top of title box		*/
    int   preview_mode;
} CONPLOTQ_TYPE;


#define UNIT_PPM	0
#define UNIT_HERTZ	1
#define UNIT_SEC	2
#define UNIT_CHAN	3


#define LORENTZIAN	0
#define GAUSSIAN	1
#define NEGGAUSSIAN	2
#define LINES		3
#define MIXED		4

#define PEAKADD		1
#define PEAKSUBTRACT	-1
#define PEAKCALC	2

#define POS		0
#define WID		1
#define HIG		2
#define MIX		3

#define JUNK		-1
#define OFF		0
#define ON		1

#define FITVAL		4

typedef struct {
    int    init;		/* Init flag				*/
    PBLOCK *par;		/* Pointer to current parameter block 	*/
    DBLOCK *dat;		/* Pointer to current data block 	*/
    DBLOCK *dat2;		/* Pointer to fit data block 		*/
    SBLOCK *spar;		/* Pointer to current plotinfo block 	*/
    SBLOCK *spar2;		/* Pointer to fit plotinfo block 	*/
    int    mxdcnv;		/* Max number of lines to fit 		*/
    float  *array[FITVAL];	/* Array of fit values			*/
    int    *ifitar[FITVAL];	/* Array of fit flags			*/
    int    linctr;		/* Line counter				*/
    int    linenb;		/* Current line				*/
    float  wprec;		/* Precision				*/
    int    maxcal;		/* Max number of calculations		*/
    float  scaint;		/* Scale factor for integration		*/
    float  sfract;		/* factor x shift			*/
    float  hfract;		/* factor y intensity shift		*/
    float  wfract;		/* factor width shift			*/
    int    il;			/* Left x				*/
    int    ir;			/* Right x				*/
    float  *warray;		/* difference spectrum			*/
    float  *farray;		/* calculated data			*/
    int    nparam;		/* number of fit params = pos, width, height */
    int    lineshape;		/* Gaussian or Lorentzian		*/
} LFDECONV_TYPE;

#include "psnd_simula.h"

typedef struct {
    DBLOCK *dat;
    SBLOCK *spar;
    PBLOCK *par;
    SIMTYPE *st;
} SIMULA_INFO;


#define MAXLABELS	20

typedef struct {
    int   is_visible;
    int   mouse_mode;
    float scale_factor;
    float rotate[3];
    int   draw_wire;
    int   border_mode;
    int   data_in_buffer;
    float xangle, yangle;
    float xangle_def, yangle_def, zangle_def;
    float translate_def[3];
    float scale_def;
    int   ncpr, inpfil, maxdat;
    float *x, *xcpr;
    int   numpointsx, numpointsy;
    int   x1, x2;
    int   y1, y2;
    int   skipx, skipy;
    float tick_x1, tick_y1;
    float tick_stepx, tick_stepy;
    float tick_scalex,  tick_scaley;
    int   do_triangles;
    int   color, border_color;
    float scalez;
    int   show_xaxis;
    char  axislabels[2*MAXLABELS][10];
    float axisx1, axisx2;
    char *xlabels[MAXLABELS];
    int   nxlabels;
    int   show_yaxis;
    float axisy1, axisy2;
    char *ylabels[MAXLABELS];
    int   nylabels;
    int   perspective;
} STACKPLOT_BLOCK;

typedef struct INT1D_INFO_struct {
    int   left, right;
    float sum;
    struct INT1D_INFO_struct * next;
} INT1D_INFO;

typedef struct {
    int cont_id;
    int block_id;
    int visible;
} POPUP_INFO;

typedef struct {
    int   left, right;
    float bottom, top;
} PEAKPICK_INFO;

typedef struct splinepoint  {
    int   x;		/* x-value of the controll point	*/
    int   fixed;	/* If TRUE, take y-value, 		*/
    			/* otherwise take y from xbuf		*/
    float y;		/* y-value of controll point		*/
} SPOINT;

typedef struct {
    int    ymode, interpolating;
    int    buffsize;
    float  *basis[4];
    int    xx[4];	/* work space				*/
    float  yy[4];	/* work space				*/
    SPOINT *spoint;	/* controll-point buffer		*/
    int    ssize; 	/* The size that is allocated for the 	*/
			/* controll-point buffer 		*/
    int    spos;	/* The number of points in the 		*/
			/* controll-point buffer		*/
} SPLINE_INFO;

typedef struct {
    int   npeaks;
    int   biggest;
    float p0;
    int   *peaks;
} AUTOPHASE_BLOCK;

typedef enum {
    POP_BASELINE,
    POP_REGION,
    POP_WATERFIT,
    POP_WATWA,
    POP_LPC,
    POP_WINDOW,
    POP_FT,
    POP_PEAKPICK,
    POP_INT1D,
    POP_INT2D,
    POP_BOOKMARK,
    POP_PHASE1D,
    POP_PHASE2D,
    POP_SPLINE,
    POP_LINKSELECT,
    POP_VIEWADJUST,
    POP_VIEWSELECT,
    POP_AU,
    POP_SIMULA,
    POP_SIMASSIGN,
    POP_SIMEDIT,
    POP_STACKPLOT,
    POP_LFDEC,
    POP_LFDECADJUST,
    POP_MOUSE,
    POP_CALIBRATE	/* must be last	*/
} popcont_ids;

#define POP_MAX	(POP_CALIBRATE+MAX_DIM)

/*
 * Master memory block
 */
typedef struct master_block_struct {
    INFO            *info;
    CBLOCK          *cpar_screen, *cpar_hardcopy;
    CONPLOTQ_TYPE   *cpq_screen, *cpq_hardcopy;
    LFDECONV_TYPE   *lfdec;
    SIMULA_INFO     *siminf;
    STACKPLOT_BLOCK *spb;
    INT1D_INFO      *int1dinf;
    POPUP_INFO      *popinf;
    PEAKPICK_INFO   *ppinf;
    SPLINE_INFO     *sinf;
    VP_STORE        *vp_store;                   /* VP_MAX */
    AUTOPHASE_BLOCK *aphase_block;
    SBLOCK          *spar_block[MAXBLK];
    SBLOCK          *spar;
    PBLOCK          *par[MAXBLK];
    DBLOCK          *dat[MAXBLK];
} MBLOCK;



void  psnd_init_objects(MBLOCK *mblk);
void  psnd_init_undo(DBLOCK *dat);
void  psnd_push_undo(UNDOINFO *undo, DBLOCK *dat);
void  psnd_init_plane_undo(DBLOCK *dat);
void  psnd_push_plane_undo(UNDOPLANEINFO *undo, DBLOCK *dat);
int   psnd_pop_plane(MBLOCK *mblk, int undo_mode, int contour_mode);
int   psnd_plane(MBLOCK *mblk, int verbose, int plane_mode, int key,
                int contour_mode);

void  psnd_init_zoomundo(DBLOCK *dat);
void  psnd_push_zoomundo(int vp_id, float x1, float y1, float x2, float y2, 
                         DBLOCK *dat);
int   psnd_popzoom(MBLOCK *mblk, int mode);
int   psnd_lastzoom(MBLOCK *mblk);
void  psnd_set_cropmode(int mode, DBLOCK *dat);
int   psnd_update_lastzoom(int vp_id, float x1, float y1, float x2, float y2, 
                          DBLOCK *dat);

char  *psnd_getfilename(MBLOCK *mblk, char *title, char *mask);
char  *psnd_savefilename(MBLOCK *mblk, char *title, char *filename);
char  *psnd_getdirname(MBLOCK *mblk, char *title);
void  psnd_enable_open_buttons(MBLOCK *mblk, int doit);
void  psnd_enable_viewselect_blocks(MBLOCK *mblk);
void  psnd_popup_viewselect(MBLOCK *mblk);
void  psnd_enable_linkselect_blocks(MBLOCK *mblk, int from, int to);
void  psnd_popup_linkselect(MBLOCK *mblk);
int   psnd_popup_select_array(int win_id, int select, char *title, 
                             char *subtitle, int icomplex);
void  psnd_param_init(PBLOCK *par, int dir, int nsiz, int cmplx,
                       float sf, float sw, float dspshift);
int   psnd_set_param(MBLOCK *mblk, int argc, char *argv[], int param_id);
void  psnd_update_raw_param(MBLOCK *mblk);
void  psnd_setprompt(MBLOCK *mblk, char *prompt, char *message);
void  psnd_compose_prompt(char *filename, DBLOCK *dat);
void  psnd_get_axis_units(MBLOCK *mblk, int vp_id, int isspec, AXIS_UNIT_INFO *aui, 
        SBLOCK *spar, CBLOCK *cpar, PBLOCK *par, PBLOCK *par2, DBLOCK *dat);
void  psnd_axis(MBLOCK *mblk, int vp_id, int isspec, int flag, int dimension, 
        float xmin, float xmax,  SBLOCK *spar, CBLOCK *cpar, 
        PBLOCK *par, PBLOCK *par2, DBLOCK *dat);
int   psnd_popup_axis(int win_id, int plot_mode, SBLOCK *spar);
void  psnd_plotgrid(MBLOCK *mblk);
void  psnd_scrollbars_reconnect(MBLOCK *mblk, int vp_id, float x1, float y1, 
                                 float x2, float y2,int axis_sleep);
void  psnd_box(MBLOCK *mblk, int vp_id, 
               float vpxmi,float vpxma,float vpymi,float vpyma,
               int ixbox, int do_cross,
               int obj, int color);
void  psnd_xaxis(MBLOCK *mblk, int vp_id, 
                float vpxmi,float vpxma,float vpymi,float vpyma,
                float xwldmi,float xwldma,
                int is_integerx,
                int ixbox,
                int ixcode, 
                char *label,
                int obj, int color);
void  psnd_yaxis(MBLOCK *mblk, int vp_id, 
                float vpxmi,float vpxma,float vpymi,float vpyma,
                float ywldmi,float ywldma,
                int is_integery,
                int ixbox,
                int iycode, 
                char *label,
                int obj, int color);
void  psnd_grid(MBLOCK *mblk, int vp_id, 
               float wx1,float wx2,float wy1,float wy2,
               int showx, int showy,
               int obj, int color);
void  psnd_setworld(MBLOCK *mblk, int vp_id,float pxl,float pxh,float pyl,float pyh);
void  psnd_setviewport3(MBLOCK *mblk, int vp_id, float minx, float maxx, 
                                         float miny, float maxy);
void  psnd_getviewport3(MBLOCK *mblk, int vp_id,float *minx, float *maxx, 
                                         float *miny, float *maxy);
void  psnd_set_groupviewport(MBLOCK *mblk, int vp_id);
void  psnd_link_viewport(MBLOCK *mblk, int vp_id, int vp_code);
void  psnd_getzoom(MBLOCK *mblk, int vp_id, float *zoomx, float *zoomy);
void  psnd_getmaxworld(MBLOCK *mblk, int vp_id, float *wcxmin, float *wcxmax, 
                       float *wcymin, float *wcymax);
int   psnd_test_zoom(MBLOCK *mblk, int vp_id, float x1, float y1, 
                          float x2, float y2);

void  psnd_set_vp_group(MBLOCK *mblk, int vp_group);
void  psnd_set_vp_id(MBLOCK *mblk, int vp_id);
int   psnd_get_vp_group(MBLOCK *mblk);
int   psnd_get_vp_id(MBLOCK *mblk);
int   psnd_set_dominant_viewport(MBLOCK *mblk);
int   psnd_get_dominant_viewport(MBLOCK *mblk);
int   psnd_get_master_world(MBLOCK *mblk, int master_vp_id, 
                      float *wx1, float *wy1, float *wx2, float *wy2);
void  psnd_set_master_world(MBLOCK *mblk, int master_vp_id, 
                      float wx1, float wy1, float wx2, float wy2);
int   psnd_reset_connection(MBLOCK *mblk, int vp_id);
int   psnd_1d_reset_connection(MBLOCK *mblk);
void  psnd_select_contourviewport(MBLOCK *mblk);
void  psnd_init_viewports(MBLOCK *mblk);

void  psnd_plotlevel(MBLOCK *mblk, int argc, char *argv[]);
void  psnd_init_contour_par(SBLOCK *spar, CBLOCK *cpar);
void  psnd_process_contour_levels(MBLOCK *mblk, CBLOCK *cpar);
void  psnd_process_color_levels(MBLOCK *mblk, CBLOCK *cpar);
void  psnd_edit_contour_colors(int win_id, int mode, CBLOCK *cpar);
void  psnd_contour_mode(MBLOCK *mblk, int doit, CBLOCK *cpar);
void  psnd_check_contour_limits(MBLOCK *mblk);
int   psnd_cp(MBLOCK *mblk, int mode);
void  psnd_get_plotarea(MBLOCK *mblk, int *lowx, int *highx, 
                                      int *lowy, int *highy);


void  psnd_realloc_buffers(MBLOCK *mblk, int newsize);

void  psnd_assign_transition(MBLOCK *mblk, float hz, float left, float right);
void  psnd_popup_simula(MBLOCK *mblk, SBLOCK *spar, PBLOCK *par, DBLOCK *dat);
void  psnd_linefit(MBLOCK *mblk, int xpos);
void  psnd_popup_linefit(MBLOCK *mblk, SBLOCK *spar2, DBLOCK *dat2);

void  psnd_copyparam(MBLOCK *mblk, int to, int from);

int   psnd_hardcopy_param1d2d(MBLOCK *mblk, int is_2d);
void  psnd_hardcopy(MBLOCK *mblk);
void  psnd_set_plot_region(CONPLOTQ_TYPE *cpq, CBLOCK *cpar,
                   float left, float right, float bottom, float top);
int   psnd_au_paperplot_1d(MBLOCK *mblk, char *filename);
int   psnd_au_paperplot_2d(MBLOCK *mblk, char *filename);
int   psnd_write_plot_parameters(CONPLOTQ_TYPE *cpq, CBLOCK *cpar, char *outputfile);
int   psnd_read_plot_parameters(CONPLOTQ_TYPE *cpq, CBLOCK *cpar, char *inputfile);
int   psnd_set_plot_parameters(CONPLOTQ_TYPE *cpq, CBLOCK *cpar, char *inputstring);
void  psnd_process_plot_settings(CONPLOTQ_TYPE *cpq, CBLOCK *cpar);
void  psnd_init_paper_plot(CONPLOTQ_TYPE *cpq);

float psnd_chan2ppm(float chan, float sp, float sw, int n, float xref, 
                     float aref, float bref, int irc);
float psnd_ppm2chan(float ppm, float sp, float sw, int n, float xref,
                     float aref, float bref, int irc);
float psnd_hz2chan(float hz, float sw, int n, float xref, float aref,
                    float bref, int irc);
float psnd_chan2hz(float chan, float sw, int n, float xref, float aref, 
                    float bref, int irc);
float psnd_sec2chan(float sec, float sw);
float psnd_chan2sec(float chan, float sw);

int   psnd_write_contpar(char *filename, CBLOCK *cpar);

int   psnd_paperplot_contour(MBLOCK *mblk, int preview);
void  psnd_paperplot_xaxis(int vp, int autoscale, char *axislabel,
                float minp, float maxp,
                float ticks_ppm, float medium_ticks_ppm, float long_ticks_ppm, 
                float labels_ppm, float off_axis, float off_label, 
                float start_title, float off_title,
                float ticklen1, float ticklen2, float ticklen3,
                float width,  float height,
                float vx1, float vy1, float vx2, float vy2);
void  psnd_paperplot_yaxis(int vp, int autoscale, char *axislabel,
                float minp, float maxp, 
                float ticks_ppm, float medium_ticks_ppm, float long_ticks_ppm, 
                float labels_ppm, float off_axis, float off_label, 
                float start_title, float off_title,
                float ticklen1, float ticklen2, float ticklen3,
                float width, float height,
                float vx1, float vy1, float vx2, float vy2);
void  psnd_paperplot_grid(int vp,  int autoscalex, int autoscaley,
                float xminp, float xmaxp, float yminp, float ymaxp,
                float xlines_ppm, float ylines_ppm);
void  psnd_paperplot_labels(int vp, int vp2, float *xreal, 
                float xmin, float xmax, float ymin, float ymax,
                float cminx, float cmaxx, float cminy, float cmaxy,
                float thresh, float sens, float avewidth,
                int sign, int npart,
                float lheight, float lymax,
                float sfd, float swhold, int nsiz, float xref,
                float aref, float bref, int irc, int unitx);
int   psnd_paperplot_1d(MBLOCK *mblk, int preview);
int   psnd_calc_ps_penwidth(int no);

void  psnd_set_peakpick_threshold(CONPLOTQ_TYPE *cpq, float thresh);


void  psnd_get_clipped_viewwin(float *vx1, float *vy1, float *vx2, float *vy2,
                float *wx1, float *wy1, float *wx2, float *wy2);

int   psnd_is_visible(MBLOCK *mblk, int block_id, int s_id);
int   psnd_make_visible(MBLOCK *mblk, int block_id, int s_id, int doit);
void  psnd_set_viewport_master(MBLOCK *mblk, int block_id, 
                               int s_id, int doit);
void  psnd_set_viewport_block_master(MBLOCK *mblk, int block_id, 
                               int doit);
void  psnd_stackplot(MBLOCK *mblk, CBLOCK *cpar, float *xcpr, int ncpr, 
            int border_color, int fill_color,
            int inpfil, float *xdata, int maxdat, 
            int ilowa, int ihigha, int ilowb, int ihighb,
            float lowest, float highest, int update,
            float axisx1, float axisx2, 
            float axisy1, float axisy2);

void  psnd_plot1d_phase(MBLOCK *mblk, int s_id, int vp_code,
                         int isize, float *xreal,int auto_scale);
void  psnd_plot1d_in_2d(MBLOCK *mblk, int swap, int extra_marker, int both);
void  psnd_plot(MBLOCK *mblk, int erase,   int dim);
void  psnd_plotaxis(MBLOCK *mblk, PBLOCK *par);
void  psnd_phaseplot1d(MBLOCK *mblk);

void  psnd_plot_one_array(MBLOCK *mblk, float *xdata, int n1, int n2, int s_id,
                           int scale, int append, SBLOCK *spar);
int   psnd_popup_zoomrange(MBLOCK *mblk);
float psnd_calc_pos(float value, float ref, float aref, float bref,
                     int irc, int isspec, int axis_mode, int nsize,
                     float sw, float sf, char *label, char *unit);
float psnd_calc_pos_ex(float value, float ref, float aref, float bref,
                        int irc, int isspec, int axis_mode, int nsize,
                        float sw, float sf, char *label, char *unit,
                        float *ppm, float *hz, float *sec, int *mode);
float psnd_calc_channels(float value, float ref, float aref, float bref,
                          int irc, int isspec, int axis_mode,
                          int nsize, float sw, float sf);

void  psnd_add_phase(MBLOCK *mblk, BOOKMARKINFO *bi, CBLOCK *cpar, int id);
void  psnd_popup_phase(MBLOCK *mblk);
void  psnd_phase(MBLOCK *mblk, float p0, float p1);
void  psnd_set_phase_position(MBLOCK *mblk, PBLOCK *par, DBLOCK *dat, int i0);
int   psnd_do_pk(MBLOCK *mblk, int argc, char *argv[]);
void  psnd_pk(MBLOCK *mblk, PBLOCK *par, DBLOCK *dat);
void  psnd_pz(MBLOCK *mblk, PBLOCK *par);
int   psnd_auto_phase(MBLOCK *mblk, int mode);
void  psnd_refresh_phase_labels(MBLOCK *mblk);
void  psnd_set_button_phase_block(MBLOCK *mblk, int vp_id);
void  psnd_set_phase2d(MBLOCK *mblk);
void  psnd_show_2d_phase_data(MBLOCK *mblk);
void  psnd_show_2d_phase_direction(MBLOCK *mblk, int dir);
void  psnd_popup_2dphase_selectionbox(MBLOCK *mblk);

int   psnd_poprec(MBLOCK *mblk, int mode);
void  psnd_nextrec(MBLOCK *mblk, int verbose, int incr, PBLOCK *pars[MAX_DIM], DBLOCK *dat);
int   psnd_rec(MBLOCK *mblk, int, int, int, int);
void  psnd_3d_xy(MBLOCK *mblk, float x, float y);

int   psnd_set_datasize(MBLOCK *mblk, int nsize, int updatesw, DBLOCK *dat);
int   psnd_set_calibration(float xref, PBLOCK *par, DBLOCK *dat);
int   psnd_set_calibration_aref(float aref, PBLOCK *par, DBLOCK *dat);
int   psnd_set_calibration_bref(float bref, PBLOCK *par, DBLOCK *dat);
float psnd_scale_xref(float xref, float aref, float bref, int shift);
float psnd_unscale_xref(float xref, float aref, float bref, int shift);
void  psnd_calibrate_popup(MBLOCK *mblk);
int   psnd_calibrate(MBLOCK *mblk, float xref, float yref);
void  psnd_refresh_calibrate_labels(MBLOCK *mblk);
void  psnd_update_calibration_param(MBLOCK *mblk);

void  psnd_integrate1d(MBLOCK *mblk, float x1, float x2);
void  psnd_integrate2d(MBLOCK *mblk, float x1, float x2, float y1, float y2);
void  psnd_integrate(MBLOCK *mblk, float x1, float x2, float y1, float y2);
int   psnd_integrals_read(MBLOCK *mblk, CBLOCK *cpar, char *filename);
float psnd_integral_calc(MBLOCK *mblk, CBLOCK *cpar, int peak_id);
int   psnd_integrals_get_numpeaks(MBLOCK *mblk, CBLOCK *cpar);

void  psnd_distance(MBLOCK *mblk, float x1, float y1, float x2, float y2);
void  psnd_position(MBLOCK *mblk, float x, float y);
void  psnd_where(MBLOCK *mblk, float x, float y);
void  psnd_plotblock(SBLOCK *spar, CBLOCK *cpar,PBLOCK *par, DBLOCK *dat);
void  psnd_peakpick(MBLOCK *mblk, float x1, float y1, float x2, float y2);
void  psnd_peakpick_area(MBLOCK *mblk, float x1, float y1, float x2, float y2);
void  psnd_peakpick1d_popup(MBLOCK *mblk);
void  psnd_peakpick2d(MBLOCK *mblk, CBLOCK *cpar, float *xcpr,
            int ncpr, float clevel,  
            int inpfil, float *x, int maxdat, 
            int ilowa, int ihigha, int ilowb, int ihighb,
            int iadiv, int ibdiv);
void  psnd_add2d_peak(int sign, void *userdata, 
                     float xmin, float xmax, float ymin, float ymax );
void  psnd_rmsnoise(MBLOCK *mblk, float x1, float x2, float y1, float y2);
void  psnd_parfit(int i0, DBLOCK *dat);

int   psnd_blockoperations(MBLOCK *mblk, int id);
void  psnd_cb_addvalue(int,int, DBLOCK *dat);
void  psnd_cw_addvalue(int jxdo, int jxdd, float ydo, float ydd, DBLOCK *dat);
void  psnd_arrayfill(int argc, char *argv[], int id, float value, PBLOCK *par, DBLOCK *dat);
void  psnd_arrayreverse(PBLOCK *par, DBLOCK *dat);
void  psnd_arraynegate(DBLOCK *dat);
int   psnd_arrayclip(MBLOCK *mblk, int argc, char *argv[], int start, int stop);
void  psnd_arrayaverage(DBLOCK *dat);
void  psnd_arraypower(DBLOCK *dat);
void  psnd_arraymove(MBLOCK *mblk);
int   psnd_array_out(MBLOCK *mblk, char *filename, float *array1, float *array2, int isize, 
                    int icomplex, int npar, PBLOCK *par, DBLOCK *dat);
int   psnd_array_in(MBLOCK *mblk, char *filename, float *array1, float *array2,
                    int *size);
void  psnd_arrayread(MBLOCK *mblk, DBLOCK *dat);
void  psnd_arraywrite(MBLOCK *mblk, int thisrecord, int argc, char *argv[], DBLOCK *dat);

int   psnd_rotate_array(int argc, char *argv[], int sign, int value, DBLOCK *dat);
int   psnd_shift_array(int argc, char *argv[], int sign, int value, DBLOCK *dat);
int   psnd_array_popup(MBLOCK *mblk, int operation);
int   psnd_arrayfill_popup(MBLOCK *mblk, int mode);

void  psnd_dn(MBLOCK *mblk, DBLOCK *dat);
void  psnd_d2(DBLOCK *dat);
int   psnd_dd(int ndc, int isize, float *xreal);
int   psnd_dc(int ndc, int isize, float *xreal);

void  psnd_showbaseline_popup(MBLOCK *mblk);
void  psnd_refresh_baseline_labels(MBLOCK *mblk);
void  psnd_refresh_all_baseline_labels(MBLOCK *mblk);
int   psnd_baseline(MBLOCK *mblk, int, int, CBLOCK *cpar);
void  psnd_spline_baseline(MBLOCK *mblk, float *y, float *z,  float *xwork, int npts, int nt1, 
               float u, float v, float sfac, float bfac, int *ier);
void  psnd_spline_slope(MBLOCK *mblk, float *y, float *xwork, int pos, 
               int stop, float u, float v, float sfac, 
               float bfac, int *ier);


int   psnd_lookup_window_mode(int iwindo);
int   psnd_window_prep(MBLOCK *mblk, int argc, char *argv[], int id);
int   psnd_do_window(MBLOCK *mblk, int doit, int ask);
void  psnd_set_window_id(int id, PBLOCK *par);
void  psnd_showwindow_popup(MBLOCK *mblk);
void  psnd_refresh_all_window_labels(MBLOCK *mblk);

void  psnd_showlpc_popup(MBLOCK *mblk);
int   psnd_linearprediction(MBLOCK *mblk);
void  psnd_refresh_all_lpc_labels(MBLOCK *mblk);
int   psnd_fillzero(MBLOCK *mblk, int argc, char *argv[]);
int   psnd_dft(MBLOCK *mblk, int argc, char *argv[]);
int   psnd_ft(MBLOCK *mblk, int argc, char *argv[]);
int   psnd_if(MBLOCK *mblk, int argc, char *argv[]);
int   psnd_do_if(MBLOCK *mblk, int oldsize, PBLOCK *par, DBLOCK * dat);
int   psnd_do_ft(MBLOCK *mblk, int oldsize, PBLOCK *par, DBLOCK * dat);
void  psnd_refresh_all_ft_labels(MBLOCK *mblk);

void  psnd_popup_bookmark(MBLOCK *mblk);
void  psnd_reset_popup_bookmark(MBLOCK *mblk, CBLOCK *cpar, DBLOCK *dat);
void  psnd_clear_popup_bookmark(MBLOCK *mblk, DBLOCK *dat);
void  psnd_region_popup(MBLOCK *mblk);
void  psnd_spline_addvalue(MBLOCK *mblk, int init, int jxdd, float y, int nsize,
                           float *xbuf, float *sbuf);
void  psnd_popup_int2d(MBLOCK *mblk);
void  psnd_int1d_popup(MBLOCK *mblk);
                
int   psnd_spline_popup(MBLOCK *mblk);
int   psnd_spline_read(MBLOCK *mblk, char *filename, DBLOCK *dat);
int   psnd_spline_calc(MBLOCK *mblk, int nsize, float *xbuf, float *sbuf);
int   psnd_spline_read(MBLOCK *mblk, char *filename, DBLOCK *dat);

int   psnd_linpr(MBLOCK *mblk, float *xreal, float *ximag, 
                int icmplx, int size, int nfut, int npoles,
                float toler, int cmplx, int mode, int moveroots, 
                int gapstart, int gapstop, int start, int stop, 
                int replace, int cursize);
int   psnd_watwa(int iopt, int iscmplx,
                float *xreal, float *ximag, int isize,
                float kc, float wshift);
int   psnd_do_watwa(PBLOCK *par, DBLOCK *dat);
int   psnd_waterwash(int argc, char *argv[], MBLOCK *mblk);
void  psnd_refresh_watwa_labels(MBLOCK *mblk);
void  psnd_refresh_all_watwa_labels(MBLOCK *mblk);
int   psnd_fit_waterline(MBLOCK *mblk, int argc, char *argv[], int doit);
void  psnd_showwaterfit_popup(MBLOCK *mblk);
void  psnd_refresh_waterfit_labels(MBLOCK *mblk);
void  psnd_refresh_all_waterfit_labels(MBLOCK *mblk);
int   psnd_resize_arrays(MBLOCK *mblk, int nzf, int addtosize, int *calc_size,
                        DBLOCK *dat);

void  psnd_stackplot_popup(MBLOCK *mblk);
void  psnd_adjust_labels_to_new_direction(MBLOCK *mblk);


int   power(int);

void  psnd_close_file(MBLOCK *mblk);
int   psnd_open(MBLOCK *mblk, int argc, char *argv[]);
int   psnd_open_outfile(MBLOCK *mblk, int count, char *namelist[MAX_DIM+1],
                       int isold, PBLOCK *par, DBLOCK *dat);
int   psnd_direction(MBLOCK *mblk, int argc, char *argv[], int keycount, int *keys);
int   psnd_getparam(MBLOCK *mblk, int);
int   psnd_returnrec(MBLOCK *mblk, int flush);
int   psnd_rn(MBLOCK *mblk, int keycount, int *keys, DBLOCK *dat);
void  psnd_set_record_read_range(MBLOCK *mblk, int start, int stop, PBLOCK *par);
int   psnd_read_record(MBLOCK *mblk, int argc, char *argv[], DBLOCK *dat);
int   psnd_guess_storemode(PBLOCK *par);
int   psnd_rw(MBLOCK *mblk, int keycount, int *keys, DBLOCK *dat);
short *psnd_get_sorted_list(MBLOCK *mblk, DBLOCK *dat, int rangetemp[], int range[], 
                             int loop_dim, int nsize, int icmplx[],
                             int is_input);
void  psnd_setparam(MBLOCK *mblk, int argc, char *argv[]);
int   psnd_edit_all_param(MBLOCK *mblk);
int   psnd_edit_raw_param(MBLOCK *mblk);
void  psnd_lp(MBLOCK *mblk, PBLOCK *par);
void  psnd_la(MBLOCK *mblk, int ndim, int dim, int alldata, DBLOCK *dat);
void  psnd_list_all_current_parameters(MBLOCK *mblk, int ndim, int alldata, DBLOCK *dat, PBLOCK *par0);
void  psnd_list_aquisition_parameters(MBLOCK *mblk);
void  psnd_hi(MBLOCK *mblk, int argc, char *argv[], int to_file);
int   psnd_copy_file_to_oldfile(MBLOCK *mblk, char *filename);

char  *psnd_get_script(void);
int   psnd_get_script_size(void);
char  *psnd_get_scriptarg(void);
int   psnd_get_scriptarg_size(void);

void  psnd_au(MBLOCK *mblk);
int   psnd_au_script(MBLOCK *mblk);
int   psnd_run_au_from_commandline(MBLOCK *mblk, char *command, 
                                    int argc, char *argv[]);
int   psnd_read_script(MBLOCK *mblk);
char  *psnd_read_script_in_buffer(char *filename);
char  *psnd_popup_run_script(MBLOCK *mblk, int read_script);
FILE  *psnd_open_script_file(char *filename, char *argv0);

int   psnd_popup_prepa_setup(MBLOCK *mblk);
int   psnd_popup_output_setup(MBLOCK *mblk);

void  psnd_popup_cursormode(MBLOCK *mblk);
void  psnd_set_cursormode(MBLOCK *mblk, int id, int item);
void  psnd_push_waitcursor(MBLOCK *mblk);
void  psnd_pop_waitcursor(MBLOCK *mblk);
void  psnd_process_mousebar_messages(MBLOCK *mblk, int id_mouse);

void  psnd_switch_plotdimension(MBLOCK *mblk);
int   psnd_close_outfile(MBLOCK *mblk, PBLOCK *par, DBLOCK *dat);
int   psnd_printf(MBLOCK *mblk, const char *format, ...);
int   psnd_puts(MBLOCK *mblk, char *buf);
char  *psnd_sprintf_temp(const char *format, ...);
int   psnd_scan_integer(char *value);
float psnd_scan_float(char *value);


void  popup_add_spin(int cont_id, G_POPUP_CHILDINFO *ci, int id, 
                           char *label, int select, int *min_max_dec_step);
int   popup_spin_double(int win_id, char *title, int *val1, char *label1, 
        int *val2, char *label2, int min, int max);
void  popup_add_label(int cont_id, G_POPUP_CHILDINFO *ci, char *label);
void  popup_add_separator(int cont_id, G_POPUP_CHILDINFO *ci);
void  popup_add_text(int cont_id, G_POPUP_CHILDINFO *ci, int id, 
                           char *label);
void  popup_add_text2(int cont_id, G_POPUP_CHILDINFO *ci, int id, 
                           char *label, char *title);
void  popup_add_text3(int cont_id, G_POPUP_CHILDINFO *ci, int id, 
                           char *label, char *title, int disabled);
int   popup_spin(int win_id, char *title, int *val1, char *label1, 
        int min, int max, int step);
int   popup_option(int win_id, char *title, int *val1, char *label, 
                   int item_count, char **item_labels);
void  popup_add_option(int cont_id, G_POPUP_CHILDINFO *ci, int id, 
                           char *label, int select, int item_count,
                           char **item_labels);
void  popup_add_option2(int cont_id, G_POPUP_CHILDINFO *ci, int id, 
                           char *label, int select, int item_count,
                           char **item_labels);

void  param_process_input(MBLOCK *mblk, int id, int dim, int numdim, 
                          G_POPUP_CHILDINFO ci[], 
                          CBLOCK *cpar, PBLOCK *par0[],  DBLOCK *dat);    


int   param_add_linpar(MBLOCK *mblk, int cont_id, G_POPUP_CHILDINFO ci[], int id,
                    void (*func)(G_POPUP_CHILDINFO *ci),
                    PBLOCK *par);
int   param_add_watwa(MBLOCK *mblk, int cont_id, G_POPUP_CHILDINFO ci[], int id,
                    void (*func)(G_POPUP_CHILDINFO *ci),
                    PBLOCK *par);
int   param_add_window1(MBLOCK *mblk, int cont_id, G_POPUP_CHILDINFO ci[], int id,
                    void (*func)(G_POPUP_CHILDINFO *ci),
                    PBLOCK *par);
int   param_add_window2(MBLOCK *mblk, int cont_id, G_POPUP_CHILDINFO ci[], int id,
                    void (*func)(G_POPUP_CHILDINFO *ci),
                    PBLOCK *par);
int   param_add_window3(MBLOCK *mblk, int cont_id, G_POPUP_CHILDINFO ci[], int id,
                    void (*func)(G_POPUP_CHILDINFO *ci),
                    PBLOCK *par);
void  param_convert_baseline_type(PBLOCK *par, int *select, int *window_select);
int   param_add_baseline1(MBLOCK *mblk, int cont_id, G_POPUP_CHILDINFO ci[], int id,
                       void (*func)(G_POPUP_CHILDINFO *ci),
                       PBLOCK *par);
int   param_add_baseline2(MBLOCK *mblk, int cont_id, G_POPUP_CHILDINFO ci[], int id,
                       void (*func)(G_POPUP_CHILDINFO *ci),
                       PBLOCK *par);
int   param_add_baseline3(MBLOCK *mblk, int cont_id, G_POPUP_CHILDINFO ci[], int id,
                       void (*func)(G_POPUP_CHILDINFO *ci),
                       PBLOCK *par);
int   param_add_waterfit(MBLOCK *mblk, int cont_id, G_POPUP_CHILDINFO ci[], int id,
                       void (*func)(G_POPUP_CHILDINFO *ci),
                       PBLOCK *par);

static int ft2user_modes[] = {
    FFTREA,
    FFTCX,
    DFTREA,
    DFTCX
};

int   psnd_user2ft_modes( PBLOCK *par);
int   param_add_fourier1(MBLOCK *mblk, int cont_id, G_POPUP_CHILDINFO ci[], int id,
                       void (*func)(G_POPUP_CHILDINFO *ci),
                        PBLOCK *par);
int   param_add_fourier2(MBLOCK *mblk, int cont_id, G_POPUP_CHILDINFO ci[], int id,
                       void (*func)(G_POPUP_CHILDINFO *ci),
                        PBLOCK *par);

typedef enum {
    ID_NWINDO=1000,
    ID_IWINDO,
    ID_RSH,
    ID_RLB,
    ID_GN,
    ID_ITM,
    ID_IWSTRT,
    ID_IWSTOP,
    ID_HILBERT,
    ID_WATWA,
    ID_IOPT, 
    ID_KC, 
    ID_WSHIFT, 
    ID_PHASE,
    ID_A,
    ID_B,
    ID_I0,
    ID_NBASE,
    ID_IBASE,
    ID_IBASE_WND,
    ID_ITERMS,
    ID_ITERMS2,
    ID_IBSTRT,
    ID_IBSTOP,
    ID_IBWATER,
    ID_IBWIDTH,
    ID_ILOWA,
    ID_IHIGHA,
    ID_ILOWB,
    ID_IHIGHB,
    ID_IADIV,
    ID_IBDIV,
    ID_MLEVEL,
    ID_NLEVEL,
    ID_CLEVEL,
    ID_FLEVEL,
    ID_DLEVEL,
    ID_LEVELMIN,
    ID_LEVELMAX,
    ID_LEVCOL,
    ID_PLUSMIN,
    
    ID_INTMOD, 
    ID_INTLEVEL, 
    ID_REV, 
    ID_VREV, 
    ID_SCALE_I, 
    ID_ZOINT,
    ID_NSHIFT,
    ID_PINPOI,
    ID_PRIMAX, 
    ID_PRIBOX, 

    ID_SFD,
    ID_SWHOLD,
    ID_TD,
    ID_XREF,
    ID_AREF,
    ID_BREF,
    ID_RECTYPE,
    ID_RECTYPE_CURRENT,
    ID_DSPFLAG,
    ID_DSPSHIFT,
    ID_IPRE,
    ID_IRSTRT,
    ID_IRSTOP,
    ID_NFFT,
    ID_IFFT,
    ID_NZF,
    ID_FTSCALE,
    ID_IPOST,
    ID_IREVER,
    ID_IFILUT,
    ID_IOFF1,
    ID_IOFF2,
    ID_ISSTRT,
    ID_ISSTOP,

    ID_NLPC,
    ID_LPC,
    ID_LPCMODE,
    ID_NFUT,
    ID_NPOLES,
    ID_MROOT,
    ID_REPLACE,
    ID_TOLER,
    ID_NGAP1,
    ID_NGAP2,
    ID_NSTART,
    ID_NSTOP,
    ID_WATERFIT,
    ID_WATERPOS,
    ID_WATERWID,
    
    ID_NEXTDIR,
    ID_AQDIR,
    ID_TRUNC_FLOAT,
    ID_TRUNC_LEVEL,
    ID_TRUNC_LEVEL1,
    ID_TRUNC_LEVEL2,
    ID_TRUNC_OFFSET
    
} param_ids;



int   psnd_rvalin(MBLOCK *mblk, char *ask, int items, float *result);
int   psnd_rvalin2(MBLOCK *mblk, char *ask, float *result, int flag);
int   psnd_ivalin2(MBLOCK *mblk, char *ask, int *result, int flag);
int   psnd_ivalin(MBLOCK *mblk, char *ask, int items, int *result);

void  psnd_show_colormap(MBLOCK *mblk, int numcol);
void  psnd_init_colormap(void);
int   psnd_colormap_window_event(MBLOCK *mblk, G_EVENT *ui);
char  **psnd_get_colornames(int *count);
int   psnd_get_colorvalue(MBLOCK *mblk, int id, int block_id);
void  psnd_set_colorvalue(MBLOCK *mblk, int id, int block_id, int col_id);
void  psnd_set_default_colors(MBLOCK *mblk);
void  psnd_write_defaultcolors(FILE *outfile, MBLOCK *mblk);
void  psnd_read_defaultcolors(FILE *infile, MBLOCK *mblk);
void  psnd_write_colormap(FILE *outfile);
void  psnd_read_colormap(FILE *infile, int ncolors);

int   psnd_write_resources(MBLOCK *mblk);
int   psnd_read_resources(MBLOCK *mblk, int verbose);

extern int baseline_ids[];
extern int baseline_wids[];


#include "mathtool.h"


char  *scanfile(FILE *file, char *pattern1, int skip, char *pattern2, 
                int isregexp);
char  *scanbinfile(FILE *file, char *pattern);
char  *psnd_grep(char *origin, char *pattern, int *mlen);

void  phase(float *xr, float *xi, int nbl, float ahold,
            float bhold, int i0);
float redf(int i);
void  invrse(float *x, int n);
void  invrse2(float *x, int start, int stop);



void  psnd_param_reset(MBLOCK *mblk, int block);
void  psnd_change_block(MBLOCK *mblk, int id, int verbose);

#define MODE_NEW	2
#define MODE_OLD	3

int   opefcx(MBLOCK *mblk,int ifile, int isnew, char *filename, int *npar,
           int nsiz[], int cmplx[]);
void  opefmx(MBLOCK *mblk,int iunit, int option, char *name,
            int *npar, int *nsiz1, int *nsiz2, int *nsiz3, int *ierr);
void  writmx(MBLOCK *mblk,int iunit, int label, int keya, int keyb,
            float *array, int npoint, int *ierr);
void  writcx(MBLOCK *mblk,int iunit, int label, int keya, int keyb,
            float *arrayr, float *arrayi, int npoint, int *ierr);
void  readmx(MBLOCK *mblk,int iunit, int label, int keya, int keyb,
            float *array, int npoint, int *ierr);
void  readcx(MBLOCK *mblk,int iunit, int label, int keya, int keyb,
            float *arrayr, float *arrayi, int npoint, int *ierr);
int   closmx(MBLOCK *mblk, int ifile);
int   ityp3d(MBLOCK *mblk, int iunit);
void  cnvpar(float *parms);
void  rdparm(MBLOCK *mblk, int iunit, float *x, int n, int *ierr);
void  wrparm(MBLOCK *mblk, int iunit, float *x, int n, int *ierr);

void  addhis(MBLOCK *mblk, int iunit, char *prgnam, float version,
            float *pars, int npar);
void  shhis(MBLOCK *mblk, int iunit);
void  trsall(MBLOCK *mblk, int iunit1, int iunit2);

int   mulr2d(MBLOCK *mblk, float *xcpr,int ncpr,int io,float *x,int maxdat,
            int ial,int iah,int ibl,int ibh,int *iacpr,int *ibcpr,
            int id1,int jd1);
int   read2d(void *mblk, int idev, float *xdata, int nsize, int key1);


void  psnd_parstt(float *xpar, int idir, PBLOCK *par, int create);
void  psnd_pargtt(float *xpar, int idir, PBLOCK *par);
void  psnd_pargtm(int file_type, float *parms, int *nt0par, int *ntnpar, 
                          int *iaqdir, int *nextdr);
void  psnd_parstm(int file_type, float *parms, int nt0par, int ntnpar, 
            int iaqdir, int nextdr);

char  *psnd_pargtt_text(float *xpar, int idir_start, int idir_stop);
int   psnd_parstt_text(float *xpar, char *label);
int   iond_par_get_size(int dim);

char  *strupr(char *s);
char  *strlwr(char *s);
int   psnd_popup_block_select(MBLOCK *mblk, int win_id, int block_id);
int   psnd_popup_numblocks(MBLOCK *mblk);
void  psnd_popup_viewadjust(MBLOCK *mblk);
void  adjust_callback(G_POPUP_CHILDINFO *ci);
int   okarea(int i1, int i2, int j1, int j2);
void  matsiz(MBLOCK *mblk, int iunit, int idir, int *mszdir, int *msiza, int *msizb);
void  getmsz(MBLOCK *mblk, int iunit, int *nsiz1, int *nsiz2, int *nsiz3);
int   os_big_endian(void);


#endif





