/********************************************************************/
/*                         psnd_main.c                              */
/*                                                                  */
/* Main routines for the psnd program                               */
/* 1997, Albert van Kuik                                            */
/********************************************************************/
#include <stdio.h>
#include <stdlib.h>
#include <ctype.h>
#include <string.h>
#include <math.h>
#include <time.h>
#include <sys/stat.h>
#include <assert.h>
#include <signal.h>
#include <unistd.h>
#include "genplot.h"
#include "psnd.h"
#include "nmrtool.h"


char   *psnd_get_version_string(void);

static void psnd_setup_buttonbar(MBLOCK *mblk);
static void psnd_setup_mousebar(MBLOCK *mblk);

static void parse_command(char *cmmnd, int *argc, char *argv[]);
static int translate_command(char *cmmnd, int *argc, char *argv[]);
static void allocate_blocks(MBLOCK *mblk, int from, int to);

/*
static void mainloop(MBLOCK *mblk, char *ask, 
                 float *x, float *y, float *xx2, float *yy2);
                 */
static void print_usage(int argc, char *argv[]);


static void sighandler(int sig)
{
    char temp[200];
    char *p;
    
    signal(sig, SIG_IGN);
/*    fprintf(stderr,"Ladida\n");*/
    signal(sig, sighandler);
    exit(sig);
}


/*==================================================================*/


static void psnd_openwindow(MBLOCK *mblk, char *title, float x, float y,
                             float dx, float dy)
{
    int menu_id, submenu_id;
    
    mblk->info->win_id = g_open_window(G_SCREEN,x, y, dx, dy, title,
             G_WIN_BUFFER|
             G_WIN_MENUBAR|
             G_WIN_COMMANDLINE_FOCUS|
             G_WIN_COMMANDLINE_HIDEHISTORY|
             G_WIN_BUTTONBAR|
             G_WIN_BUTTONBAR2|
             G_WIN_SCROLLBAR|
             G_WIN_COMMANDLINE);


    mblk->info->menubar_id = g_menu_create_menubar(mblk->info->win_id);
    menu_id = mblk->info->menu_id[MENU_FILE_ID] = 
                    g_menu_append_submenu(mblk->info->menubar_id, "File");

    g_menu_append_button(menu_id, "Open File	GE",  PSND_FILE_OPEN);
    g_menu_append_button(menu_id, "Close File	CL",  PSND_FILE_CLOSE);
    g_menu_append_button(menu_id, "Change Direction	DR",  PSND_DR);
    g_menu_append_button(menu_id, "Read Record	RN",  PSND_RN);
    g_menu_append_button(menu_id, "Return Record	RTR",  PSND_RTR);
    g_menu_append_button(menu_id, "Read Plane	RP",  PSND_PLANE_READ);
    g_menu_append_separator(menu_id);
    g_menu_append_button(menu_id, "Write Record	OU",  PSND_OU);
    g_menu_append_button(menu_id, "Save as ...",  PSND_FILE_SAVEAS);
    g_menu_append_separator(menu_id);
    g_menu_append_button(menu_id, "Prepare Bruker/Varian File",  PSND_FILE_PREPA);
    g_menu_append_separator(menu_id);
    g_menu_append_button(menu_id, "Plot Screen",          PSND_DOHC);
    g_menu_append_button(menu_id, "Plot 1D",              PSND_PLOT_1D);
    g_menu_append_button(menu_id, "Plot 2D",              PSND_PLOT_CONTOUR);

    g_menu_append_separator(menu_id);
    g_menu_append_button(menu_id, "Read AU Script",  PSND_READ_SCRIPT);
    g_menu_append_button(menu_id, "History to AU Script",  PSND_HI_FILE);
    g_menu_append_button(menu_id, "Change Directory CHDIR",  PSND_CHANGEDIR);
    g_menu_append_separator(menu_id);
    g_menu_append_button(menu_id, "Quit",  PSND_DOKEY_Q);

    menu_id = mblk->info->menu_id[MENU_PARAM_ID] = 
               g_menu_append_submenu(mblk->info->menubar_id, "Parameters");
#ifdef __sgi
    g_menu_append_button(menu_id, "Spectral	SP",  PSND_PARAM_FREQ);
    g_menu_append_button(menu_id, "Input File	RM",  PSND_RM);
    g_menu_append_button(menu_id, "Dsp Correction	DSP", PSND_DSPSHIFT);
    g_menu_append_button(menu_id, "Linear prediction	LM",  PSND_PARAM_LINPAR);
    g_menu_append_button(menu_id, "Window	WM",  PSND_PARAM_WINDOW);
    g_menu_append_button(menu_id, "Water Wash	WW",  PSND_PARAM_WATWA);
    g_menu_append_button(menu_id, "Fourier mode	FM",  PSND_PARAM_FOURIER);
    g_menu_append_button(menu_id, "Hilbert Transform	HB",  PSND_PARAM_HILBERT);
    g_menu_append_button(menu_id, "Phase	PM",  PSND_PARAM_PHASE);
    g_menu_append_button(menu_id, "Reverse	XM",  PSND_XM);
    g_menu_append_button(menu_id, "Waterfit",  PSND_PARAM_WATERFIT);
    g_menu_append_button(menu_id, "Baseline	BM",  PSND_BM);
    g_menu_append_button(menu_id, "Output File	SM",  PSND_PARAM_OUTFILE);
    g_menu_append_separator(menu_id);
#endif
    g_menu_append_button(menu_id, "Edit all parameters	EDP",  PSND_PARAM_ALL);
    g_menu_append_button(menu_id, "Store Parameters	ST",  PSND_ST);
    g_menu_append_button(menu_id, "Reload Parameters	GP",  PSND_GP);
    g_menu_append_button(menu_id, "Change raw disk pars	CH",  PSND_PARAM_RAW);
    g_menu_append_button(menu_id, "Next processing dir	ND",  PSND_ND);
    if (mblk->info->version) 
        g_menu_append_button(menu_id, "Truncation Level",  PSND_TRUNC_LEVEL);

    g_menu_append_separator(menu_id);
    g_menu_append_button(menu_id, "Contour parameters	CM",  PSND_CM);
   
    g_menu_append_separator(menu_id);
    submenu_id = mblk->info->menu_id[MENU_PAR_LIST_ID] = 
                    g_menu_append_submenu(menu_id, "List Parameters");
    g_menu_append_button(submenu_id, "Current	LP",   PSND_LP);
    g_menu_append_button(submenu_id, "All Current	LAC",  PSND_LAC);
    g_menu_append_button(submenu_id, "All From File	LA",   PSND_LA);

    menu_id = mblk->info->menu_id[MENU_PROCESS_ID] = 
                       g_menu_append_submenu(mblk->info->menubar_id, "Process");
    g_menu_append_button(menu_id, "Linear Predict	LPC",    PSND_LPC);
    g_menu_append_button(menu_id, "Window	WIN",    PSND_DO_WINDOW);
    g_menu_append_button(menu_id, "Water Wash	WATWA",  PSND_WATWA);
    g_menu_append_button(menu_id, "Fourier	FT",     PSND_FT);
    g_menu_append_button(menu_id, "Hilbert	HT",     PSND_HILBERT);
    g_menu_append_button(menu_id, "Phase	PK",     PSND_POPUP_PHASE);
    g_menu_append_button(menu_id, "Reverse	RV",     PSND_RV);
    g_menu_append_button(menu_id, "Water Fit",     PSND_SHOW_WATERFIT);
    g_menu_append_button(menu_id, "Baseline	BC",     PSND_BASELINE);
    g_menu_append_separator(menu_id);

    g_menu_append_button(menu_id, "Size	SI",  PSND_SI);
    g_menu_append_button(menu_id, "Inverse FT	IFT", PSND_IF);
    g_menu_append_button(menu_id, "Fill Zero	FZ",  PSND_FZ);
    g_menu_append_button(menu_id, "Absolute value	AV",  PSND_AV);
    g_menu_append_button(menu_id, "Power spectrum	PS",  PSND_PS);

    menu_id = mblk->info->menu_id[MENU_DISPLAY_ID] = 
                          g_menu_append_submenu(mblk->info->menubar_id, "Display");
    g_menu_append_button(menu_id, "Axis Units",    PSND_XAXIS);
    g_menu_append_button(menu_id, "Show Data",     PSND_VIEW);
    g_menu_append_button(menu_id, "Link Scaling",  PSND_LINKVIEW);
    g_menu_append_button(menu_id, "Adjust Scaling",PSND_POPUP_ADJUST);
    g_menu_append_button(menu_id, "Zoom Settings   PX/PY", PSND_PX);
    g_menu_append_button(menu_id, "Edit Colormap", PSND_COLORMAP);

    menu_id = mblk->info->menu_id[MENU_DATA_ID] = 
                                g_menu_append_submenu(mblk->info->menubar_id, "Data");
    g_menu_append_button(menu_id, "Set Number of Blocks",      PSND_SET_NUM_BLOCKS);
    g_menu_append_button(menu_id, "Select Block",      PSND_SELECT_BLOCK);
    g_menu_append_button(menu_id, "Block Operations",  PSND_TR);
    g_menu_append_separator(menu_id);

    g_menu_append_button(menu_id, "Modify array",  PSND_ROL);
    g_menu_append_button(menu_id, "Fill array",  PSND_ZE);
#ifdef oldold
    submenu_id = mblk->info->menu_id[MENU_ARRAY_CHANGE_ID] = 
                                       g_menu_append_submenu(menu_id, "Modify array");
    g_menu_append_button(submenu_id, "Clip	IC",  PSND_IC);
    g_menu_append_button(submenu_id, "Left   Shift	LS",  PSND_LS);
    g_menu_append_button(submenu_id, "Right  Shift	RS",  PSND_RS);
    g_menu_append_button(submenu_id, "Rotate Left	ROL", PSND_ROL);
    g_menu_append_button(submenu_id, "Rotate Right	ROR", PSND_ROR);
    g_menu_append_button(submenu_id, "Reverse	RV",  PSND_RV);
    g_menu_append_button(submenu_id, "Negate	NM",  PSND_NM);
    submenu_id = mblk->info->menu_id[MENU_ARRAY_FILL_ID] = 
                                   g_menu_append_submenu(menu_id, "Fill array");
    g_menu_append_button(submenu_id, "Zero real and imag	ZE",  PSND_ZE);
   
    g_menu_append_button(submenu_id, "Fill real	ZR",  PSND_ZR);
    g_menu_append_button(submenu_id, "Fill imag	ZI",  PSND_ZI);
    g_menu_append_button(submenu_id, "Fill buffer A	ZA",  PSND_ZA);
    g_menu_append_button(submenu_id, "Fill buffer B	ZB",  PSND_ZB);
    g_menu_append_button(submenu_id, "Fill window	ZW",  PSND_ZW);
    g_menu_append_button(submenu_id, "Fill baseln bf	ZL",  PSND_ZL);
#endif

    g_menu_append_button(menu_id, "Move (copy) array	MV",  PSND_MV);
    g_menu_append_button(menu_id, "Write array	WR",  PSND_WR);
    g_menu_append_button(menu_id, "Read array	RD",  PSND_RD);

    menu_id = mblk->info->menu_id[MENU_OPTIONS_ID] = 
                            g_menu_append_submenu(mblk->info->menubar_id, "Utilities");
    g_menu_append_button(menu_id, "Peak Picking 1D",  PSND_PEAK_PICK);
    g_menu_append_button(menu_id, "Integration  1D",  PSND_POPUP_INT1D);
    g_menu_append_button(menu_id, "Line Fit",      PSND_LINEFIT);
    g_menu_append_button(menu_id, "Simulation",    PSND_SIMULA);
    g_menu_append_separator(menu_id);
    g_menu_append_button(menu_id, "Phase Tools 2D",        PSND_POPUP_PHASE_2D);
    g_menu_append_button(menu_id, "Integration 2D",  PSND_POPUP_INT2D);
    g_menu_append_button(menu_id, "Integration 2D options",   PSND_CI);
    g_menu_append_separator(menu_id);
    g_menu_append_button(menu_id, "Calibrate",   PSND_POPUP_CALIBRATE);
    g_menu_append_button(menu_id, "Edit/Run AU Script",     PSND_SCRIPT);

    g_menu_append_separator(menu_id);
    g_menu_append_button(menu_id, "Stack plot",    PSND_POPUP_STACKPLOT);

    psnd_setup_mousebar(mblk);
    psnd_setup_buttonbar(mblk);

    g_set_clipping(TRUE);
    g_set_cursortype(mblk->info->win_id, G_CURSOR_CROSSHAIR);
#ifdef _WIN32
    g_set_charsize(1.3);
#else
    g_set_charsize(0.95);
#endif
}

void psnd_enable_open_buttons(MBLOCK *mblk, int doit)
{
    int menu_id, is2d;;

    if (mblk->info->foreground == FALSE)
        return;
    
    is2d =  (mblk->info->plot_mode != PLOT_1D); 

    menu_id = mblk->info->menu_id[MENU_FILE_ID];
    g_menu_enable_item(menu_id, PSND_FILE_CLOSE, doit);
    g_menu_enable_item(menu_id, PSND_DR, doit);
    g_menu_enable_item(menu_id, PSND_RN, doit);
    g_menu_enable_item(menu_id, PSND_RTR, doit);
    /*
    g_menu_enable_item(menu_id, PSND_OU, doit);
    */
    g_menu_enable_item(menu_id, PSND_FILE_SAVEAS, doit);
    g_menu_enable_item(menu_id, PSND_DOHC, doit);
    /*
    g_menu_enable_item(menu_id, PSND_PLOT_1D, doit && !is2d);
    g_menu_enable_item(menu_id, PSND_PLOT_CONTOUR, doit && is2d);
    */
    menu_id = mblk->info->menu_id[MENU_PARAM_ID];

    g_menu_enable_item(menu_id, PSND_PARAM_ALL, doit);
    g_menu_enable_item(menu_id, PSND_ST, doit);
    g_menu_enable_item(menu_id, PSND_GP, doit);
    g_menu_enable_item(menu_id, PSND_PARAM_RAW, doit);
    g_menu_enable_item(menu_id, PSND_ND, doit);
    g_menu_enable_item(menu_id,  PSND_TRUNC_LEVEL, doit);
    g_menu_enable_item(menu_id,  PSND_CM, doit && is2d);
    
    menu_id = mblk->info->menu_id[MENU_PAR_LIST_ID];
    g_menu_enable_item(menu_id,  PSND_LAC, doit);
    g_menu_enable_item(menu_id,  PSND_LA, doit);

}

static void enable_2d_buttons(MBLOCK *mblk, int doit, int is3d)
{
    g_menu_enable_item(mblk->info->bar_id, PSND_ROW_COLUMN, doit);
    g_menu_enable_item(mblk->info->bar_id, PSND_CP, doit);
    g_menu_enable_item(mblk->info->bar_id, PSND_CPM, doit);
    g_menu_enable_item(mblk->info->bar_id, PSND_UP, doit);
    g_menu_enable_item(mblk->info->bar_id, PSND_DOWN, doit);
    g_menu_enable_item(mblk->info->bar_id, PSND_PHASE_2D, doit);
    g_menu_enable_item(mblk->info->bar_id, PSND_CROP, doit);
    g_menu_enable_item(mblk->info->bar_id, PSND_UNCROP, doit);
    g_menu_enable_item(mblk->info->bar_id, PSND_LEVELS_BOTH, doit);
    g_menu_enable_item(mblk->info->bar_id, PSND_SC_2D, doit);
    if (is3d || !doit) {
        g_menu_enable_item(mblk->info->bar_id, PSND_PLANE_UP, doit);
        g_menu_enable_item(mblk->info->bar_id, PSND_PLANE_DOWN, doit);
        g_menu_enable_item(mblk->info->bar_id, PSND_PLANE_UNDO, doit);
        g_menu_enable_item(mblk->info->bar_id, PSND_PLANE_REDO, doit);
        g_menu_enable_item(mblk->info->menu_id[MENU_FILE_ID], PSND_PLANE_READ, doit);
        g_menu_enable_item(mblk->info->mousebar_id, ID_MOUSE_ROWCOL_PLANE, doit);
    }
/*
    g_menu_enable_item(mblk->info->bar_id, PSND_SHOW_WINDOW, !doit);
    g_menu_enable_item(mblk->info->bar_id, PSND_SHOW_BASELINE, !doit);
*/

    g_menu_enable_item(mblk->info->menu_id[MENU_FILE_ID], PSND_PLOT_CONTOUR, doit);
    g_menu_enable_item(mblk->info->menu_id[MENU_FILE_ID], PSND_PLOT_1D, !doit);
    g_menu_enable_item(mblk->info->menu_id[MENU_PARAM_ID], PSND_CM, doit);
    g_menu_enable_item(mblk->info->menu_id[MENU_PARAM_ID], PSND_CC, doit);
    g_menu_enable_item(mblk->info->menu_id[MENU_PROCESS_ID], PSND_LPC, !doit);
    g_menu_enable_item(mblk->info->menu_id[MENU_BASELINE_ID], PSND_SPLINE_RESET, !doit);

    g_menu_enable_item(mblk->info->menu_id[MENU_OPTIONS_ID], PSND_CI, doit);
    g_menu_enable_item(mblk->info->menu_id[MENU_OPTIONS_ID], PSND_POPUP_INT2D, doit);
    g_menu_enable_item(mblk->info->menu_id[MENU_OPTIONS_ID], PSND_POPUP_INT1D, !doit);
    g_menu_enable_item(mblk->info->menu_id[MENU_OPTIONS_ID], PSND_LINEFIT, !doit);
    g_menu_enable_item(mblk->info->menu_id[MENU_OPTIONS_ID], PSND_SIMULA, !doit);
    g_menu_enable_item(mblk->info->menu_id[MENU_OPTIONS_ID], PSND_POPUP_STACKPLOT, doit);
    g_menu_enable_item(mblk->info->menu_id[MENU_OPTIONS_ID], PSND_POPUP_PHASE_2D, doit);

    g_menu_enable_item(mblk->info->mousebar_id, ID_MOUSE_SUM, doit);
    g_menu_enable_item(mblk->info->mousebar_id, ID_MOUSE_SCALE, doit);

}

static int psnd_old_cursor_type;
void psnd_push_waitcursor(MBLOCK *mblk)
{
    int type;
    
    type = g_get_cursortype(mblk->info->win_id);
    if (type != G_CURSOR_WAIT)
        psnd_old_cursor_type = type;
    g_set_cursortype(mblk->info->win_id, G_CURSOR_WAIT);
    g_peek_event();
}

void psnd_pop_waitcursor(MBLOCK *mblk)
{
    if (psnd_old_cursor_type == G_ERROR)
        psnd_old_cursor_type = G_CURSOR_CROSSHAIR;
    g_set_cursortype(mblk->info->win_id, psnd_old_cursor_type);
    g_peek_event();
}




void psnd_add_tooltips()
{
    g_menu_add_tooltip(PSND_XDIV2, "Zoom in (X)");
    g_menu_add_tooltip(PSND_XMUL2, "Zoom out (X)");
    g_menu_add_tooltip(PSND_YDIV2, "Zoom in (Y)");
    g_menu_add_tooltip(PSND_YMUL2, "ZOOM out (Y)");

    g_menu_add_tooltip(PSND_RESET, "Un-zoom");
    g_menu_add_tooltip(PSND_SC,"Auto scale spectrum");
    g_menu_add_tooltip(PSND_UNDO_ZOOM, "Undo zoom");
    g_menu_add_tooltip(PSND_REDO_ZOOM, "Redo zoom");

    g_menu_add_tooltip(PSND_CO, "2D mode");
    g_menu_add_tooltip(PSND_SC_2D,"Auto Scale 1D in 2D");
    g_menu_add_tooltip(PSND_ROW_COLUMN, "Row/column 1D in 2D");
    g_menu_add_tooltip(PSND_LEVELS_BOTH, "Positive/negative/both levels");
    g_menu_add_tooltip(PSND_PHASE_2D, "2D phase mode");
 
    g_menu_add_tooltip(PSND_XTERM, "Popup output window");
    g_menu_add_tooltip(PSND_DO_WINDOW, "Window funtion");
    g_menu_add_tooltip(PSND_FT, "Fourier transform");
    g_menu_add_tooltip(PSND_POPUP_PHASE, "Phase correction");
/*
    g_menu_add_tooltip(empty_xpm, NULL, 0);
*/
    g_menu_add_tooltip(PSND_BASELINE, "Baseline correction");
    g_menu_add_tooltip(PSND_PL, "Plot");
    g_menu_add_tooltip(PSND_RN, "Read record");
    g_menu_add_tooltip(PSND_DR, "Change processing direction");

    g_menu_add_tooltip(PSND_CP, "Contour plot");
    g_menu_add_tooltip(PSND_UP, "Contour level up");
    g_menu_add_tooltip(PSND_CROP, "Crop");
    g_menu_add_tooltip(PSND_PLANE_UP, "Plane up");
    g_menu_add_tooltip(PSND_PLANE_UNDO, "Undo plane selection");


    g_menu_add_tooltip(PSND_DOKEY_PLUS, "Next record");
    g_menu_add_tooltip(PSND_DOKEY_MINUS, "Previous record");
    g_menu_add_tooltip(PSND_UNDO, "Undo record selection");
    g_menu_add_tooltip(PSND_REDO, "Redo record selection");
    g_menu_add_tooltip(PSND_AU, "Automatic: do all previous commands");
    g_menu_add_tooltip(PSND_SCRIPT, "Edit/run script");
    g_menu_add_tooltip(PSND_POPUP_BOOKMARK, "Popup bookmarks");
    g_menu_add_tooltip(PSND_APPEND_PLOT, "Additive plot");

    g_menu_add_tooltip(PSND_SHOW_GRID, "Grid");
    g_menu_add_tooltip(PSND_CPM, "Contour Plot Mode: Colors/Contour 2x2/Contour 1x1");
    g_menu_add_tooltip(PSND_CP2, "2D plot");
    g_menu_add_tooltip(PSND_DOWN, "Contour level down");
    g_menu_add_tooltip(PSND_UNCROP, "Uncrop");
    g_menu_add_tooltip(PSND_PLANE_DOWN, "Plane down");
    g_menu_add_tooltip(PSND_PLANE_REDO, "Redo plane selection");

    g_menu_add_tooltip(ID_MOUSE_POS, "Measure Position");
    g_menu_add_tooltip(ID_MOUSE_SELECT, "Select (Waterline/Lines/Transitions)");
    g_menu_add_tooltip(ID_MOUSE_REGION, "Define Region");
    g_menu_add_tooltip(ID_MOUSE_SPLINE, "Define Spline control points");
    g_menu_add_tooltip(ID_MOUSE_ZOOMXY, "Zoom XY");
    g_menu_add_tooltip(ID_MOUSE_ZOOMX, "Zoom X");
    g_menu_add_tooltip(ID_MOUSE_ZOOMY, "Zoom Y");
    g_menu_add_tooltip(ID_MOUSE_PHASE_P0, "Zero order Phase Correction");
    g_menu_add_tooltip(ID_MOUSE_PHASE_P1, "First order Phase Correction");
    g_menu_add_tooltip(ID_MOUSE_PHASE_I0, "Zero order Phase Position");
    g_menu_add_tooltip(ID_MOUSE_CALIBRATE, "Calibration");
    g_menu_add_tooltip(ID_MOUSE_INTEGRATE, "Peak Integration");
    g_menu_add_tooltip(ID_MOUSE_PEAKPICK, "1D/2D Peak Picking");
    g_menu_add_tooltip(ID_MOUSE_DISTANCE, "Measure Distance");
    g_menu_add_tooltip(ID_MOUSE_SCALE, "In 1D: Adjust scaling. In 2D: Lowest Level in contour plot");
    g_menu_add_tooltip(ID_MOUSE_NOISE, "Measure Noise Level");
    g_menu_add_tooltip(ID_MOUSE_ROWCOL, "In 1D: switch Row/Column. In 2D: show Row/Column");
    g_menu_add_tooltip(ID_MOUSE_SUM, "In 2D: additive Rows/Columns");
    g_menu_add_tooltip(ID_MOUSE_ROWCOL_PLANE, "In 3D: select Planes");

}

#include "buttons_xpm.h"


static void psnd_setup_mousebar(MBLOCK *mblk)
{
    int buttonbar_id = g_menu_create_buttonbar(mblk->info->win_id);
    mblk->info->mousebar_id = buttonbar_id;
    g_menu_append_separator(buttonbar_id);
    g_menu_set_group(buttonbar_id, TRUE);
    g_menu_append_pixtoggle(buttonbar_id, mouse_pos_xpm, NULL,
				  NULL,NULL, ID_MOUSE_POS, TRUE);
    g_menu_append_pixtoggle(buttonbar_id, mouse_select_xpm, NULL,
				  NULL,NULL, ID_MOUSE_SELECT, FALSE);
    g_menu_append_pixtoggle(buttonbar_id, mouse_region_xpm, NULL,
				  NULL,NULL, ID_MOUSE_REGION, FALSE);
    g_menu_append_pixtoggle(buttonbar_id, mouse_spline_xpm, NULL,
				  NULL,NULL, ID_MOUSE_SPLINE, FALSE);
    g_menu_append_separator(buttonbar_id);
    g_menu_append_pixtoggle(buttonbar_id, mouse_zoomxy_xpm, NULL,
				  NULL,NULL, ID_MOUSE_ZOOMXY, FALSE);
    g_menu_append_pixtoggle(buttonbar_id, mouse_zoomx_xpm, NULL,
				  NULL,NULL, ID_MOUSE_ZOOMX, FALSE);
    g_menu_append_pixtoggle(buttonbar_id, mouse_zoomy_xpm, NULL,
				  NULL,NULL, ID_MOUSE_ZOOMY, FALSE);
    g_menu_append_separator(buttonbar_id);
    g_menu_append_pixtoggle(buttonbar_id, mouse_phase_p0_xpm, NULL,
				  NULL,NULL, ID_MOUSE_PHASE_P0, FALSE);
    g_menu_append_pixtoggle(buttonbar_id, mouse_phase_p1_xpm, NULL,
				  NULL,NULL, ID_MOUSE_PHASE_P1, FALSE);
    g_menu_append_pixtoggle(buttonbar_id, mouse_phase_i0_xpm, NULL,
				  NULL,NULL, ID_MOUSE_PHASE_I0, FALSE);
    g_menu_append_separator(buttonbar_id);
    g_menu_append_pixtoggle(buttonbar_id, mouse_calibrate_xpm, NULL,
				  NULL,NULL, ID_MOUSE_CALIBRATE, FALSE);
    g_menu_append_pixtoggle(buttonbar_id, mouse_integrate_xpm, NULL,
				  NULL,NULL, ID_MOUSE_INTEGRATE, FALSE);
    g_menu_append_pixtoggle(buttonbar_id, mouse_peakpick_xpm, NULL,
				  NULL,NULL, ID_MOUSE_PEAKPICK, FALSE);
    g_menu_append_pixtoggle(buttonbar_id, mouse_distance_xpm, NULL,
				  NULL,NULL, ID_MOUSE_DISTANCE, FALSE);
    g_menu_append_pixtoggle(buttonbar_id, mouse_scale_xpm, NULL,
				  NULL,NULL, ID_MOUSE_SCALE, FALSE);
    g_menu_append_pixtoggle(buttonbar_id, mouse_noise_xpm, NULL,
				  NULL,NULL, ID_MOUSE_NOISE, FALSE);
    g_menu_append_separator(buttonbar_id);
    g_menu_append_pixtoggle(buttonbar_id, mouse_rowcol_xpm, NULL,
				  NULL,NULL, ID_MOUSE_ROWCOL, FALSE);
    g_menu_append_pixtoggle(buttonbar_id, mouse_sum_xpm, NULL,
				  NULL,NULL, ID_MOUSE_SUM, FALSE);
    g_menu_append_pixtoggle(buttonbar_id, mouse_rowcol_plane_xpm, NULL,
				  NULL,NULL, ID_MOUSE_ROWCOL_PLANE, FALSE);

    g_menu_enable_item(mblk->info->mousebar_id, ID_MOUSE_REGION, FALSE);
    g_menu_enable_item(mblk->info->mousebar_id, ID_MOUSE_SPLINE, FALSE);
    g_menu_enable_item(mblk->info->mousebar_id, ID_MOUSE_SELECT, FALSE);
   
}

/* #include "pixmaps.h" */

static void psnd_setup_buttonbar(MBLOCK *mblk)
{
    mblk->info->bar_id = g_menu_create_buttonbox2(mblk->info->win_id, 3);
/* Start of Row 1 */

#ifdef PIXBUTTONS
    g_menu_append_pixbutton(mblk->info->bar_id, divx_pix, NULL, PSND_XDIV2);
    g_menu_append_pixbutton(mblk->info->bar_id, mulx_pix, NULL, PSND_XMUL2);
    g_menu_append_pixbutton(mblk->info->bar_id, divy_pix, NULL, PSND_YDIV2);
    g_menu_append_pixbutton(mblk->info->bar_id, muly_pix, NULL, PSND_YMUL2);
    g_menu_append_pixbutton(mblk->info->bar_id, full_xpm, NULL, PSND_RESET);
    g_menu_append_pixtoggle(mblk->info->bar_id, scale_xpm, NULL,NULL, NULL, PSND_SC,
                     mblk->info->auto_scale);
    g_menu_append_pixbutton(mblk->info->bar_id, zoomu_xpm, NULL, PSND_UNDO_ZOOM);
    g_menu_append_pixbutton(mblk->info->bar_id, zoomr_xpm, NULL, PSND_REDO_ZOOM);
#else
    g_menu_append_button(mblk->info->bar_id, "< X >", PSND_XDIV2);
    g_menu_append_button(mblk->info->bar_id, " >X< ", PSND_XMUL2);
    g_menu_append_button(mblk->info->bar_id, "< Y >", PSND_YDIV2);
    g_menu_append_button(mblk->info->bar_id, " >Y< ", PSND_YMUL2);
    g_menu_append_button(mblk->info->bar_id, "Full ", PSND_RESET);
    g_menu_append_toggle(mblk->info->bar_id, "Scale", PSND_SC, mblk->info->auto_scale);
    g_menu_append_button(mblk->info->bar_id, "ZoomU", PSND_UNDO_ZOOM);
    g_menu_append_button(mblk->info->bar_id, "ZoomR", PSND_REDO_ZOOM);
#endif


    g_menu_append_separator(mblk->info->bar_id);
#ifdef PIXBUTTONS
    g_menu_append_pixtoggle(mblk->info->bar_id, twod_xpm, NULL, NULL, NULL, PSND_CO, FALSE);
    g_menu_append_pixtoggle(mblk->info->bar_id, scale_xpm, NULL, NULL, NULL, PSND_SC_2D,
                     mblk->info->auto_scale_2d);
    g_menu_append_multipixbutton(mblk->info->bar_id, row_xpm, col_xpm, 
                                rowcol_xpm, rowcolperp_xpm, PSND_ROW_COLUMN);
    g_menu_append_multipixbutton(mblk->info->bar_id, pclpm_xpm, pclp_xpm, 
                                               pclm_xpm, NULL, PSND_LEVELS_BOTH);
    g_menu_append_pixtoggle(mblk->info->bar_id, phase2d_xpm, NULL, NULL, NULL, PSND_PHASE_2D, 
                            mblk->info->phase_2d);
#else
    g_menu_append_toggle(mblk->info->bar_id, " 2D  ", PSND_CO, FALSE);
    g_menu_append_toggle(mblk->info->bar_id, "Scale", PSND_SC_2D, mblk->info->auto_scale_2d);
    g_menu_append_button(mblk->info->bar_id, " Row ", PSND_ROW_COLUMN);
    g_menu_append_button(mblk->info->bar_id, "CL+/-", PSND_LEVELS_BOTH);
    g_menu_append_toggle(mblk->info->bar_id, "Phase", PSND_PHASE_2D, mblk->info->phase_2d);
#endif

    /* Start of Row 2 */  
    g_menu_create_buttonbox2(mblk->info->win_id, 0);

#ifdef PIXBUTTONS
    g_menu_append_pixbutton(mblk->info->bar_id, xterm_xpm, NULL, PSND_XTERM);
    g_menu_append_pixbutton(mblk->info->bar_id, wn_xpm, NULL, PSND_DO_WINDOW);
    g_menu_append_pixbutton(mblk->info->bar_id, ft_xpm, NULL, PSND_FT);
    g_menu_append_pixbutton(mblk->info->bar_id, pk_xpm, NULL, PSND_POPUP_PHASE);
    g_menu_append_pixbutton(mblk->info->bar_id, bc_xpm, NULL, PSND_BASELINE);
    g_menu_append_pixbutton(mblk->info->bar_id, pl_xpm, NULL, PSND_PL);
    g_menu_append_pixbutton(mblk->info->bar_id, rn_xpm, NULL, PSND_RN);
    g_menu_append_pixbutton(mblk->info->bar_id, dr_xpm, NULL, PSND_DR);

#else
    g_menu_append_button(mblk->info->bar_id, "     ", 0);
    g_menu_append_button(mblk->info->bar_id, " Win", PSND_DO_WINDOW);
    g_menu_append_button(mblk->info->bar_id, " FT", PSND_FT);
    g_menu_append_button(mblk->info->bar_id, " PK", PSND_PK);
    g_menu_append_button(mblk->info->bar_id, " BC", PSND_BASELINE);
    g_menu_append_button(mblk->info->bar_id, " PL", PSND_PL);
    g_menu_append_button(mblk->info->bar_id, " RN"  , PSND_RN);
    g_menu_append_button(mblk->info->bar_id, " DR"  , PSND_DR);
#endif

    g_menu_append_separator(mblk->info->bar_id);

#ifdef PIXBUTTONS
    g_menu_append_pixbutton(mblk->info->bar_id, cp_xpm, NULL, PSND_CP);
    g_menu_append_pixbutton(mblk->info->bar_id, up_xpm, NULL, PSND_UP);
    g_menu_append_pixbutton(mblk->info->bar_id, crop_xpm, NULL, PSND_CROP);
    g_menu_append_pixbutton(mblk->info->bar_id, planeup_xpm, NULL, PSND_PLANE_UP);
    g_menu_append_pixbutton(mblk->info->bar_id, planeu_xpm, NULL, PSND_PLANE_UNDO);
#else
    g_menu_append_button(mblk->info->bar_id, " CP", PSND_CP);
    g_menu_append_button(mblk->info->bar_id, " Up  ", PSND_UP);
    g_menu_append_button(mblk->info->bar_id, " Crop", PSND_CROP);
    g_menu_append_button(mblk->info->bar_id, "PlnUp", PSND_PLANE_UP);
    g_menu_append_button(mblk->info->bar_id, "Pln U", PSND_PLANE_UNDO);
#endif

/* Start of Row 3 */
    g_menu_create_buttonbox2(mblk->info->win_id, 0);

#ifdef PIXBUTTONS
    g_menu_append_pixbutton(mblk->info->bar_id, next_xpm, NULL, PSND_DOKEY_PLUS);
    g_menu_append_pixbutton(mblk->info->bar_id, prev_xpm, NULL, PSND_DOKEY_MINUS);
    g_menu_append_pixbutton(mblk->info->bar_id, undo_xpm, NULL, PSND_UNDO);
    g_menu_append_pixbutton(mblk->info->bar_id, redo_xpm, NULL, PSND_REDO);
    g_menu_append_pixbutton(mblk->info->bar_id, au_xpm, NULL, PSND_AU);
    g_menu_append_pixbutton(mblk->info->bar_id, script_pix, NULL, PSND_SCRIPT);
    g_menu_append_pixbutton(mblk->info->bar_id, bookm_xpm, NULL, PSND_POPUP_BOOKMARK);
    g_menu_append_pixtoggle(mblk->info->bar_id, grid_pix, NULL, NULL, NULL,
                                         PSND_SHOW_GRID, mblk->spar[S_GRID].show);
#else
    g_menu_append_button(mblk->info->bar_id, "  +  ", PSND_DOKEY_PLUS);
    g_menu_append_button(mblk->info->bar_id, "  -  ", PSND_DOKEY_MINUS);
    g_menu_append_button(mblk->info->bar_id, " Undo", PSND_UNDO);
    g_menu_append_button(mblk->info->bar_id, " Redo", PSND_REDO);
    g_menu_append_button(mblk->info->bar_id, " AU  ", PSND_AU);
    g_menu_append_button(mblk->info->bar_id, "Scrip", PSND_SCRIPT);
    g_menu_append_button(mblk->info->bar_id, "BookM", PSND_POPUP_BOOKMARK);
    g_menu_append_toggle(mblk->info->bar_id, "  #  ", PSND_SHOW_GRID, mblk->spar[S_GRID].show);
#endif

    g_menu_append_separator(mblk->info->bar_id);

#ifdef PIXBUTTONS
    g_menu_append_multipixbutton(mblk->info->bar_id, cpm2d_xpm, cpm_xpm, cpm1_xpm,NULL, PSND_CPM);
    g_menu_append_pixbutton(mblk->info->bar_id, down_xpm, NULL, PSND_DOWN);
    g_menu_append_pixbutton(mblk->info->bar_id, uncrop_xpm, NULL, PSND_UNCROP);
    g_menu_append_pixbutton(mblk->info->bar_id, planedn_xpm, NULL, PSND_PLANE_DOWN);
    g_menu_append_pixbutton(mblk->info->bar_id, planer_xpm, NULL, PSND_PLANE_REDO);
#else
    g_menu_append_button(mblk->info->bar_id, "PL 2D", PSND_CP2);
    g_menu_append_button(mblk->info->bar_id, " Down", PSND_DOWN);
    g_menu_append_button(mblk->info->bar_id, "UCrop", PSND_UNCROP);
    g_menu_append_button(mblk->info->bar_id, "PlnDn", PSND_PLANE_DOWN);
    g_menu_append_button(mblk->info->bar_id, "Pln R", PSND_PLANE_REDO);
#endif

/* End of box */ 
    g_menu_create_buttonbox2(mblk->info->win_id, -1);
    
    g_menu_enable_item(mblk->info->bar_id, PSND_CO, FALSE);
    enable_2d_buttons(mblk, FALSE, FALSE);   
    psnd_enable_open_buttons(mblk, DAT->finfo[INFILE].fopen);

    psnd_add_tooltips();
}


static int ignore_event_handler(G_USERINPUT *ui, G_CALLDATA call_data)
{
    return FALSE;
}

static void enable_motion_events(int doit)
{

    if (doit)
        g_remove_eventhandler(G_POINTERMOTION);
    else
        g_add_eventhandler(G_POINTERMOTION, 
                            ignore_event_handler,NULL);
}

void psnd_init_objects(MBLOCK *mblk)
{
    int i;
    
    for (i=0;i<S_MAX;i++) {
        if (mblk->spar[i].obj_id) {
            g_delete_object(mblk->spar[i].obj_id);
            g_open_object(mblk->spar[i].obj_id);
            g_close_object(mblk->spar[i].obj_id);
        }
    }
}

void psnd_setprompt(MBLOCK *mblk, char *prompt, char *message)
{
    static char command[PSND_STRLEN + 1];
    sprintf(command,"%s %s", prompt, message);
    g_set_commandprompt(mblk->info->win_id, command);
}


void psnd_compose_prompt(char *filename, DBLOCK *dat)
{
    if (filename != NULL) {
        dat->prompt = (char*)realloc(dat->prompt, strlen(filename) + 16 );
        assert(dat->prompt);
        sprintf(dat->prompt,"%s #%d >", filename, dat->id);
    }
    else {
        dat->prompt  = (char*) realloc(dat->prompt, 16);
        assert(dat->prompt);
        sprintf(dat->prompt,"#%d >", dat->id);
    }
}
/***********************************************************************
*/
#define CMNDMAXSIZE	10
typedef struct commands_type {
    char cmmnd[CMNDMAXSIZE];
    int  id;
} cammands_type;

cammands_type commands[] =
{
    /* PLOT */
    "RE",    PSND_RE,
    "IM",    PSND_IM,
    "WD",    PSND_WD,
    "BA",    PSND_BA,
    "BB",    PSND_BB,
    "BL",    PSND_BL,
    
    "ER",    PSND_ER,

    "EX",    PSND_EX,
    "GE",    PSND_GE,
    "CL",    PSND_FILE_CLOSE,
    "ST",    PSND_ST,
    "GP",    PSND_GP,
    "RN",    PSND_RN,
    "RTR",   PSND_RTR,
    "OU",    PSND_OU,
    "DR",    PSND_DR,
    "AU",    PSND_AU,
    "RUN",   PSND_RUN_SCRIPT,

    "LPC",   PSND_LPC,
    "LM",    PSND_PARAM_LINPAR,
    "WATWA", PSND_WATWA,
    "WW",    PSND_PARAM_WATWA,
    "HT",    PSND_HT,
    "HB",    PSND_PARAM_HILBERT,
    "SP",    PSND_PARAM_FREQ,

    "PX",    PSND_PX,
    "PY",    PSND_PY,
    "SC",    PSND_SC,

    "PL",    PSND_PL,
    "BC",    PSND_BC,

    "FZ",    PSND_FZ,
    "DFT",   PSND_DFT,
    "FT",    PSND_FT,
    "IFT",   PSND_IF,
    "FTSCALE", PSND_FTSCALE,

    "SI",    PSND_SI,
    "TD",    PSND_TD,
    "AD",    PSND_AD,
    "ND",    PSND_ND,
    "SW",    PSND_SW,
    "SF",    PSND_SF,
    "XR",    PSND_XR,
    "AREF",  PSND_AREF,
    "BREF",  PSND_BREF,
    "RM",    PSND_RM,
    "WM",    PSND_WM,
    "SH",    PSND_SH,
    "LB",    PSND_LB,
    "GB",    PSND_GB,
    "IT",    PSND_IT,
    "FM",    PSND_FM,
    "ZF",    PSND_ZF,
    "SM",    PSND_SM,
    "XM",    PSND_XM,
    "BM",    PSND_BM,
    "BT",    PSND_BT,
    
    /* window */
    "WN",    PSND_WN,
    "EM",    PSND_EM,
    "CD",    PSND_CD,
    "SN",    PSND_SN,
    "SQ",    PSND_SQ,
    "HN",    PSND_HN,
    "HM",    PSND_HM,
    "KS",    PSND_KS,
    "GM",    PSND_GM,
    "TM",    PSND_TM,
    "WB",    PSND_WB,
    "WIN",   PSND_DO_WINDOW,    

    /* phase */
    "PM",    PSND_PARAM_PHASE,
    "PK",    PSND_PK,
    "APK",   PSND_APK,
    "PZ",    PSND_PZ,
    "PN",    PSND_PN,
    "PA",    PSND_PA,
    "PB",    PSND_PB,
    "I0",    PSND_I0,

    "ROL",   PSND_ROL,
    "ROR",   PSND_ROR,
    "IC",    PSND_IC,
    "LS",    PSND_LS,
    "RS",    PSND_RS,
    "RV",    PSND_RV,
    "NM",    PSND_NM,
    "ZE",    PSND_ZE,
    "Z1",    PSND_Z1,
    "ZR",    PSND_ZR,
    "ZI",    PSND_ZI,
    "ZA",    PSND_ZA,
    "ZB",    PSND_ZB,
    "ZW",    PSND_ZW,
    "ZL",    PSND_ZL,
    "AV",    PSND_AV,
    "PS",    PSND_PS,
    "IJ",    PSND_IJ,
    "AT",    PSND_AT,
    "MX",    PSND_MX,
    "TR",    PSND_TR,
    "TB",    PSND_TB,
    "TW",    PSND_TW,
    "TP",    PSND_TP,
    "MV",    PSND_MV,

    "WR",    PSND_WR,
    "RD",    PSND_RD,

    "SPEC",  PSND_SPEC,
    "FID",   PSND_FID,
    "DSP",   PSND_DSPSHIFT,

    "EDP",   PSND_PARAM_ALL,
    "CH",    PSND_PARAM_RAW,

    /* print information */
    "LP",    PSND_LP,
    "LAC",   PSND_LAC,
    "LA",    PSND_LA,
    "HI",    PSND_HI,
    "AQ",    PSND_AQ,

    "CO",    PSND_CO,
    "CI",    PSND_CI,
    "CP",    PSND_CP,
    "CP1",    PSND_CP_1X1,
    "CM",    PSND_CM,
    "CC",    PSND_CC,

    /* Toggle axis units */
    "PPM",   PSND_PPM,
    "CHAN",  PSND_CHAN,
    "HZ",    PSND_HZ,
    "SEC",   PSND_SEC,
    
    /* Switch blocks */
    "1",     PSND_1,
    "2",     PSND_2,
    "3",     PSND_3,
    "4",     PSND_4,
    "5",     PSND_5,
    "6",     PSND_6,
    "7",     PSND_7,
    "8",     PSND_8,
    "9",     PSND_9,
    "10",    PSND_10,
    "11",    PSND_11,
    "12",    PSND_12,

    "CHDIR", PSND_CHANGEDIR,
    "RP",    PSND_PLANE_READ,
    "WATERFIT", PSND_WATERFIT,

    
    "  ", 0
};



#define MAXCMD	PSND_STRLEN
/*
 * Translate user input on the command line into
 * PSND_ID + arguments
 */
static void parse_command(char *cmmnd, int *argc, char *argv[])
{
#ifdef bla
    int i;
    char *cmd, separator[] = " ,\n\r\t";

    cmd = strtok(cmmnd, separator);
    i = 0;
    while (cmd && i < MAXCMD) {
        argv[i] = cmd;
        i++;
        cmd = strtok(NULL,separator);
    }
    *argc = i;
#endif
    int i,count=0,is_new;

    count  = 0;
    is_new = TRUE;
    for (i=0;i<MAXCMD && cmmnd[i] != '\0';) {
        switch (cmmnd[i]) {
            /*
             * Inside quotes is one argument
             */
            case '\"':
                cmmnd[i] = '\0';
                i++;
                argv[count++] = &(cmmnd[i]);
                while (i<MAXCMD && cmmnd[i] != '\0' && cmmnd[i] != '\"') {
                   i++;
                }
                cmmnd[i] = '\0';
                is_new = TRUE;
                break;
            /*
             * separators
             */
            case ' ' :
            case ',' :
            case '\n' :
            case '\r' :
            case '\t' :
                is_new = TRUE;
                cmmnd[i] = '\0';
                break;
            /*
             * Content of arguments
             */
            default :
                if (is_new) {
                    argv[count++] = &(cmmnd[i]);
                    is_new = FALSE;
                }
                break;
        }
        i++;
    }
    *argc = count;

/*    
for (i=0;i<count;i++)
    printf("%d.. %s\n",i,argv[i]);
    */
}

static int translate_command(char *cmmnd, int *argc, char *argv[])
{
    int i,len1,len2;
    parse_command(cmmnd, argc, argv);
    len2 = strlen(cmmnd);
    for (i=0;commands[i].id != 0; i++)  {
        len1 = strlen(commands[i].cmmnd);
        if (strncasecmp(commands[i].cmmnd, cmmnd,
                        max(len1,len2)) == 0)
            return  commands[i].id;
    }
    return 0;
}

/*
 * The main loop
 */
static void mainloop(MBLOCK *mblk)
{
    int argc;
    char *argv[MAXCMD];
    char argspace[MAXCMD*4];
    int main_ok, k, from_commandline;
    G_EVENT ui;
    static float xxx, yyy, xx3d, yy3d, a0, b0;
    static int zoom, zoom_vp_id, a0_active;
    static float xxx1,yyy1,xxx2,yyy2,dy;

    main_ok = FALSE;
    psnd_setprompt(mblk,"","");
    do {
        g_get_event(&ui);
        if (ui.win_id != mblk->info->win_id) {
            if (ui.win_id == mblk->info->colorwin_id) {
                psnd_colormap_window_event(mblk, &ui);
#ifdef _WIN32
                if (ui.event == G_WINDOWDESTROY)
                    g_close_window(ui.win_id);
#endif          
            }
            continue;
        }
        argc=0;
        from_commandline = FALSE;
        if (ui.vp_id != G_ERROR  && mblk->info->plot_mode != PLOT_2D) {
            int vp_id,i,j,ok = FALSE;
            /*
             * Check if vp_id is the currently active viewport
             * If not, it is a viewport with a lower number
             * and vp x,y must be translated
             */
             vp_id = psnd_get_dominant_viewport(mblk);
            /*
             * recalc vp x,y
             */
            if (vp_id != G_ERROR && vp_id != ui.vp_id) {
                g_translate_viewport_xy(ui.win_id, vp_id, ui.vp_id,
                       &ui.x, &ui.y);
                ui.vp_id = vp_id;
            }
        }
        switch (ui.event) {

        case G_WINDOWDESTROY:
            main_ok=TRUE;
            break;

        case G_WINDOWQUIT:
            if (g_popup_messagebox2(ui.win_id, "",
		     "Really Quit?","YES","NO") == G_POPUP_BUTTON1)
                      main_ok=TRUE;
            break;

        case G_KEYPRESS:
            break;

        case G_COMMANDLINE:
            from_commandline = TRUE;
            strcpy(argspace,ui.command);
            if ((k = translate_command(argspace, &argc, argv)) > 0) {
                ui.keycode = k;
                /*
                 * if au command has arguments, it is a script
                 * In this case the first argument is 'au' and
                 * the second argument is the script
                 */
                if (k == PSND_AU && argc > 1) {
                    strcpy(argspace,ui.command);
                    argv[0] = "AU";
                    argv[1] = argspace;
                    argc    = 2;
                }
            }
            else {
                if (strncmp(ui.command,"QUIT",4) != 0 && strncmp(ui.command,"quit",4) != 0 )
                    psnd_printf(mblk, "Unknown command = %s\n",ui.command);
                else
                    main_ok = TRUE;
                break;
            }

        case G_COMMAND:
	    switch (ui.keycode) {
                    
                case PSND_WATERFIT:
                    if (psnd_fit_waterline(mblk, argc, argv, TRUE)) 
                        psnd_plot(mblk,TRUE,mblk->info->dimension_id);
                    break;
                case PSND_SHOW_WATERFIT:
                    psnd_showwaterfit_popup(mblk);
                    break;
                case PSND_SIMULA:
                    {
                        int id=mblk->info->block_id+2;
                        POPUP_INFO *popinf = mblk->popinf + POP_SIMULA;

                        if (!popinf->visible) {
                        
                            if (id == mblk->info->max_block) 
                                id = 1;
                            id = psnd_popup_block_select(mblk, mblk->info->win_id, id);
                            id--;
                            if (id < 0) 
                                break;
                            if (id != mblk->info->block_id) {
                                psnd_copyparam(mblk, id, mblk->info->block_id);
                            }
                            if (psnd_make_visible(mblk, id, S_REAL, TRUE))
                                psnd_1d_reset_connection(mblk);
                            psnd_plotaxis(mblk,PAR);
                        }
                        psnd_popup_simula(mblk, mblk->spar_block[id], 
                            mblk->dat[id]->par0[mblk->info->dimension_id], 
                            mblk->dat[id]);
                    }
                    break;
                case PSND_LINEFIT:
                    {
                        int id=mblk->info->block_id+2;
                        POPUP_INFO *popinf   = mblk->popinf + POP_LFDEC;
                        if (popinf->visible)
                            return;
                        if (id >= mblk->info->max_block) 
                            id = 1;
                        id = psnd_popup_block_select(mblk, mblk->info->win_id, id);
                        id--;
                        if (id <= mblk->info->block_id) {
                            g_bell();
                            break;
                        }
                        psnd_copyparam(mblk, id, mblk->info->block_id);
                        psnd_make_visible(mblk, id, S_REAL, TRUE);
                        psnd_make_visible(mblk, id, S_IMAG, TRUE);
                        psnd_set_viewport_block_master(mblk, id, TRUE); 
                        psnd_set_viewport_master(mblk, mblk->info->block_id, 
                               S_IMAG, TRUE);
                        psnd_1d_reset_connection(mblk);
                        psnd_popup_linefit(mblk, mblk->spar_block[id],
                                            mblk->dat[id]);
                    }
                    break;
                case PSND_HILBERT:
                    if (psnd_set_param(mblk, 0, NULL, PSND_PARAM_HILBERT) != 0) {
                        psnd_push_waitcursor(mblk);
                        PAR->hilbert = TRUE;
                        hilbert(DAT->isize, DAT->xreal, DAT->ximag);
                        psnd_pop_waitcursor(mblk);
                        psnd_plot(mblk,TRUE,mblk->info->dimension_id);
                    }
                    break;
                case PSND_HT:
                    psnd_push_waitcursor(mblk);
                    PAR->hilbert = TRUE;
                    hilbert(DAT->isize, DAT->xreal, DAT->ximag);
                    psnd_pop_waitcursor(mblk);
                    psnd_plot(mblk,TRUE,mblk->info->dimension_id);
                    break;
		case PSND_XDIV2: 
		case PSND_XMUL2: 
		case PSND_YDIV2:
		case PSND_YMUL2: {
                    float xx_1,yy_1,xx_2,yy_2,dx,dy;
                    float xxx_1,yyy_1,xxx_2,yyy_2;
                    int vp_id = psnd_get_vp_id(mblk);
                    g_get_world(vp_id,&xx_1,&yy_1,&xx_2,&yy_2);
                    g_get_world(vp_id,&xxx_1,&yyy_1,&xxx_2,&yyy_2);
                    dy = yy_2-yy_1;
                    dx = xx_2-xx_1;
                    switch (ui.keycode) {
   		        case PSND_XDIV2:
                            xx_1 += dx/4; 
                            xx_2 -= dx/4;
                            break; 
		        case PSND_XMUL2: 
                            xx_1 -= dx/2; 
                            xx_2 += dx/2;
                            break; 
		        case PSND_YDIV2:
                            if (yy_1 < 0 && yy_2 > 0) {
                                yy_1 = -dy/4;
                                yy_2 = dy/4;
                            }
                            else  {
                                yy_1 += dy/4;
                                yy_2 -= dy/4;
                            }
                            break;
		        case PSND_YMUL2: 
                            yy_1 -= dy/2;
                            yy_2 += dy/2;
                            break;
                        }
                        if (!psnd_test_zoom(mblk, ui.vp_id, xx_1,yy_1,xx_2,yy_2))
                            break;
                        psnd_update_lastzoom(vp_id, xxx_1, yyy_1, xxx_2, yyy_2, DAT);
                        psnd_push_zoomundo(vp_id, xx_1,yy_1,xx_2,yy_2,DAT);
                        psnd_scrollbars_reconnect(mblk,vp_id, xx_1,yy_1,xx_2,yy_2,TRUE);
                        psnd_plotaxis(mblk,PAR);
                    }
                    break;
		case PSND_CROP: {
                        int xlow, ylow, xhigh, yhigh;
                        int mode;

                        psnd_get_plotarea(mblk, &xlow, &xhigh, &ylow, &yhigh);
                        mblk->cpar_screen->ilowa  = xlow;
                        mblk->cpar_screen->ihigha = xhigh;
                        mblk->cpar_screen->ilowb  = ylow;
                        mblk->cpar_screen->ihighb = yhigh;

                        mode = mblk->cpar_screen->mode;
                        if (mode == CONTOUR_STACK)
                            mode = CONTOUR_BLOCK;
                        /* mark change */
                        mblk->cpar_screen->mode   = CONTOUR_PLOT;
                        g_scrollbar_disconnect(mblk->info->win_id);
                        g_scrollbar_reset(mblk->info->win_id);
                        psnd_cp(mblk, mode);
                        psnd_set_cropmode(1, DAT);

                    }
                    break;
		case PSND_UNCROP: {
                        int mode;
                        mblk->cpar_screen->ilowa  = mblk->cpar_screen->ihamin;
                        mblk->cpar_screen->ihigha = mblk->cpar_screen->ihamax;
                        mblk->cpar_screen->ilowb  = mblk->cpar_screen->ihbmin;
                        mblk->cpar_screen->ihighb = mblk->cpar_screen->ihbmax;
                        mode = mblk->cpar_screen->mode;
                        if (mode == CONTOUR_STACK)
                            mode = CONTOUR_BLOCK;
                        /* mark change */
                        mblk->cpar_screen->mode   = CONTOUR_PLOT;
                        g_scrollbar_disconnect(mblk->info->win_id);
                        g_scrollbar_reset(mblk->info->win_id);
                        psnd_cp(mblk, mode);
                        psnd_set_cropmode(0, DAT);
                        psnd_push_zoomundo(ui.vp_id, 0.0, 0.0, 1.0, 1.0, DAT);
                    }
                    break;
                case PSND_RESET_NOUNDO:
                case PSND_RESET:
                    g_set_objectstatus(mblk->spar[S_GRID].obj_id, G_SLEEP);
                    g_set_objectstatus(mblk->spar[S_AXISX].obj_id, G_SLEEP);
                    g_set_objectstatus(mblk->spar[S_AXISY].obj_id, G_SLEEP);
                    g_scrollbar_reset(mblk->info->win_id);
                    g_scrollbar_disconnect(mblk->info->win_id);
                    if (!mblk->info->phase_2d)
                        psnd_plotaxis(mblk,PAR); 
                    if (ui.keycode == PSND_RESET)
                        psnd_push_zoomundo(ui.vp_id, 0.0,0.0,1.0,1.0,DAT);
                    break;
                case PSND_REDO_ZOOM :
                case PSND_UNDO_ZOOM :
                    if (psnd_popzoom(mblk, ui.keycode))
                        psnd_plotaxis(mblk, PAR);
                    else
                        g_bell();
                    break;

		case PSND_DOKEY_Q:
                    g_scrollbar_disconnect(mblk->info->win_id);
                    main_ok = TRUE;
                    break;
		case PSND_BUFFER_EDIT:
                    psnd_region_popup(mblk);
		    break;
                case PSND_AX:
                    psnd_plotaxis(mblk,PAR);
                    break;
                /*
                 * Define X zoom settings
                 */
                case PSND_PX:
                    if (argc >= 3) {
                        float xmin, ymin, xmax, ymax;
                        g_get_world(psnd_get_vp_id(mblk), 
                                    &xmin, &ymin, &xmax, &ymax);
                        psnd_update_lastzoom(psnd_get_vp_id(mblk), 
                                              xmin, ymin, xmax, ymax, DAT);
                        xmin = psnd_scan_float(argv[1]);
                        xmax = psnd_scan_float(argv[2]);
                        xmin = psnd_calc_channels(xmin, PAR->xref, 
                                   PAR->aref, PAR->bref, DAT->irc, 
                                   DAT->isspec, mblk->spar[S_AXISX_MARK].show, 
                                   DAT->isize, DAT->sw, PAR->sfd);
                        xmax = psnd_calc_channels(xmax, PAR->xref, 
                                   PAR->aref, PAR->bref, DAT->irc, 
                                   DAT->isspec, mblk->spar[S_AXISX_MARK].show, 
                                   DAT->isize, DAT->sw, PAR->sfd);
                        psnd_push_zoomundo(psnd_get_vp_id(mblk), 
                                            xmin, ymin, xmax, ymax,DAT);
                        psnd_scrollbars_reconnect(mblk,psnd_get_vp_id(mblk), 
                                            xmin, ymin, xmax, ymax, TRUE);
                        psnd_plotaxis(mblk,PAR);
                    }
                    else
                        if (psnd_popup_zoomrange(mblk))
                            psnd_plotaxis(mblk,PAR);
                    break;
                /*
                 * Define Y zoom settings
                 */
                case PSND_PY:
                    if (argc >= 3) {
                        float xmin, ymin, xmax, ymax;
                        g_get_world(psnd_get_vp_id(mblk), &xmin, &ymin, &xmax, &ymax);
                        psnd_update_lastzoom(psnd_get_vp_id(mblk), xmin, ymin, xmax, ymax, DAT);
                        ymin = psnd_scan_float(argv[1]);
                        ymax = psnd_scan_float(argv[2]);
                        if (mblk->info->plot_mode == PLOT_2D) {
                            ymax = psnd_calc_channels(ymax, 
                                       mblk->cpar_screen->par2->xref, 
                                       mblk->cpar_screen->par2->aref,
                                       mblk->cpar_screen->par2->bref, 
                                       mblk->cpar_screen->ihbmin,
                                       mblk->cpar_screen->par2->isspec, 
                                       mblk->spar[S_AXISY_MARK].show, 
                                       mblk->cpar_screen->par2->nsiz,
                                       mblk->cpar_screen->par2->swhold, 
                                       mblk->cpar_screen->par2->sfd);
                            ymin = psnd_calc_channels(ymin,
                                       mblk->cpar_screen->par2->xref, 
                                       mblk->cpar_screen->par2->aref,
                                       mblk->cpar_screen->par2->bref, 
                                       mblk->cpar_screen->ihbmin,
                                       mblk->cpar_screen->par2->isspec, 
                                       mblk->spar[S_AXISY_MARK].show, 
                                       mblk->cpar_screen->par2->nsiz,
                                       mblk->cpar_screen->par2->swhold, 
                                       mblk->cpar_screen->par2->sfd);
                        }
                        psnd_push_zoomundo(psnd_get_vp_id(mblk), xmin, ymin, xmax, ymax,DAT);
                        psnd_scrollbars_reconnect(mblk, psnd_get_vp_id(mblk), 
                                             xmin, ymin, xmax, ymax, TRUE);
                        psnd_plotaxis(mblk,PAR);
                    }
                    else
                        if (psnd_popup_zoomrange(mblk))
                            psnd_plotaxis(mblk,PAR);
                    break;
                case PSND_APPEND_PLOT:
                    mblk->info->append_plot = !mblk->info->append_plot;
                    g_menu_set_toggle(mblk->info->bar_id, PSND_APPEND_PLOT, mblk->info->append_plot);
                    break;
                case PSND_SC:
                    mblk->info->auto_scale = !mblk->info->auto_scale;
                    g_menu_set_toggle(mblk->info->bar_id, PSND_SC, mblk->info->auto_scale);
                    g_menu_set_label(mblk->info->bar_id, PSND_SC, "Scale");
                    break;
                case PSND_SC_2D:
                    mblk->info->auto_scale_2d = !mblk->info->auto_scale_2d;
                    g_menu_set_toggle(mblk->info->bar_id, PSND_SC_2D, mblk->info->auto_scale_2d);
                    g_menu_set_label(mblk->info->bar_id, PSND_SC_2D, "Scale");
                    break;
                case PSND_PHASE_2D:
                    psnd_set_phase2d(mblk);        
                    g_menu_set_toggle(mblk->info->bar_id, PSND_PHASE_2D, mblk->info->phase_2d);
                    break;
                case PSND_POPUP_PHASE_2D:
                    if (!mblk->info->phase_2d) 
                        psnd_set_phase2d(mblk);
                    else
                        psnd_popup_2dphase_selectionbox(mblk);
                    break;
                case PSND_POPUP_INT2D:
                    psnd_popup_int2d(mblk);
                    break;
                case PSND_POPUP_CALIBRATE:
                    psnd_calibrate_popup(mblk);
                    break;
                case PSND_CO:
                    if (DAT->finfo[INFILE].fopen == FALSE)
                        break;
                    psnd_push_waitcursor(mblk);
                    psnd_switch_plotdimension(mblk);
                    psnd_pop_waitcursor(mblk);
                    break;
                case PSND_LEVELS_BOTH:
                    mblk->cpar_screen->plusmin++;
                    if (mblk->cpar_screen->plusmin >= 3)
                        mblk->cpar_screen->plusmin=0;
                    psnd_process_contour_levels(mblk,mblk->cpar_screen);
                    psnd_process_color_levels(mblk, mblk->cpar_screen);
                    break;
                case PSND_ROW_COLUMN:
                    mblk->info->contour_row++;
                    if (DAT->ityp > 2) {
                        if (mblk->info->contour_row > PLOT_PERPENDICULAR)
                            mblk->info->contour_row=PLOT_COLUMN;
                    }
                    else {
                        if (mblk->info->contour_row > PLOT_ROW_COLUMN)
                            mblk->info->contour_row=PLOT_COLUMN;
                    }
#ifdef PIXBUTTONS
                    switch (mblk->info->contour_row) {
                    case PLOT_PERPENDICULAR:
                        g_menu_set_pixmap(mblk->info->bar_id,PSND_ROW_COLUMN, 3);
                        break;
                    case PLOT_COLUMN:
                        g_menu_set_pixmap(mblk->info->bar_id, PSND_ROW_COLUMN, 1);
                        break;
                    case PLOT_ROW:
                        g_menu_set_pixmap(mblk->info->bar_id, PSND_ROW_COLUMN, 0);
                        break;
                    default:
                        g_menu_set_pixmap(mblk->info->bar_id, PSND_ROW_COLUMN, 2);
                        break;
                    }
#else
                    switch (mblk->info->contour_row) {
                    case PLOT_PERPENDICULAR:
                        g_menu_set_label(mblk->info->bar_id, PSND_ROW_COLUMN, " _|_ ");
                        break;
                    case PLOT_COLUMN:
                        g_menu_set_label(mblk->info->bar_id, PSND_ROW_COLUMN, " Col ");
                        break;
                    case PLOT_ROW:
                        g_menu_set_label(mblk->info->bar_id, PSND_ROW_COLUMN, " Row ");
                        break;
                    default:
                        g_menu_set_label(mblk->info->bar_id, PSND_ROW_COLUMN, " R/C ");
                        break;
                    }
#endif
                    break;

                case PSND_CP:
                    if (DAT->ityp > 1) {
                        int mode;
                        if (mblk->info->contour_plot_mode == CPLOT_BLOCK)
                            mode = CONTOUR_BLOCK;
                        else if (mblk->info->contour_plot_mode == CPLOT_CONTOUR)
                            mode = CONTOUR_PLOT;
                        else if (mblk->info->contour_plot_mode == CPLOT_CONTOUR_1X1)
                            mode = CONTOUR_PLOT|CONTOUR_PLOT_1X1;
                        else
                            break;

                        psnd_push_waitcursor(mblk);
                        mblk->info->dimension_id = DAT->access[0] - 1;
                        psnd_cp(mblk, mode);
                        psnd_pop_waitcursor(mblk);
                    }
                    break;
                case PSND_CP_1X1:
                    if (DAT->ityp > 1) {
                        psnd_push_waitcursor(mblk);
                        mblk->info->dimension_id = DAT->access[0] - 1;
                        psnd_cp(mblk, CONTOUR_PLOT | CONTOUR_PLOT_1X1);
                        psnd_pop_waitcursor(mblk);
                    }
                    break;
                case PSND_CP2:
                    if (DAT->ityp > 1) {
                        psnd_push_waitcursor(mblk);
                        mblk->info->dimension_id = DAT->access[0] - 1;
                        psnd_cp(mblk, CONTOUR_BLOCK);
                        psnd_pop_waitcursor(mblk);
                    }
                    break;
                case PSND_CPM:
                    mblk->info->contour_plot_mode++;
                    if (mblk->info->contour_plot_mode>2)
                        mblk->info->contour_plot_mode = 0;

                    switch (mblk->info->contour_plot_mode) {
                    case CPLOT_BLOCK:
                        g_menu_set_pixmap(mblk->info->bar_id, PSND_CPM, 0);
                        break;
                    case CPLOT_CONTOUR:
                        g_menu_set_pixmap(mblk->info->bar_id, PSND_CPM, 1);
                        break;
                    case CPLOT_CONTOUR_1X1:
                        g_menu_set_pixmap(mblk->info->bar_id, PSND_CPM, 2);
                        break;
                    }   
                    break;
                case PSND_POPUP_STACKPLOT:
                    if (mblk->info->plot_mode == PLOT_2D)
                        psnd_stackplot_popup(mblk);
                    break;
                case PSND_STACKPLOT:
                    if (mblk->info->plot_mode == PLOT_2D) {
                        int xlow, ylow, xhigh, yhigh;

                        psnd_get_plotarea(mblk, &xlow, &xhigh, &ylow, &yhigh);
                        mblk->cpar_screen->ilowa  = xlow;
                        mblk->cpar_screen->ihigha = xhigh;
                        mblk->cpar_screen->ilowb  = ylow;
                        mblk->cpar_screen->ihighb = yhigh;

                        /* mark change */
                        mblk->cpar_screen->mode   = CONTOUR_PLOT;
                        g_scrollbar_disconnect(mblk->info->win_id);
                        g_scrollbar_reset(mblk->info->win_id);
                        psnd_push_waitcursor(mblk);
                        mblk->info->dimension_id = DAT->access[0] - 1;
                        psnd_cp(mblk, CONTOUR_STACK);
                        psnd_pop_waitcursor(mblk);
                    }
                    break;

                case PSND_DFT:
                    if (psnd_dft(mblk, argc, argv))
                        psnd_plot(mblk,TRUE,mblk->info->dimension_id);
                    break;
                case PSND_FT:
                    if (psnd_ft(mblk, argc, argv)) 
                        psnd_plot(mblk,TRUE,mblk->info->dimension_id);
                    break;
                case PSND_IF:                    
                    if (argc == 0)   {
                        if (g_popup_messagebox2(ui.win_id, "Inverse Fourier Transform",
		             "Do Inverse Fourier Transformation ?","YES","NO") == G_POPUP_BUTTON2)
                              break;
                    }

                    if (psnd_if(mblk, argc, argv))
                        psnd_plot(mblk,TRUE,mblk->info->dimension_id);
                    break;
                case PSND_WN:
                case PSND_EM:
                case PSND_CD:
                case PSND_SN:
                case PSND_SQ:
                case PSND_HN:
                case PSND_HM:
                case PSND_KS:
                case PSND_GM:
                case PSND_TM:
                case PSND_WB:
                    if (psnd_window_prep(mblk, argc, argv, ui.keycode)) {
                        psnd_set_window_id(ui.keycode, PAR);
                        if (psnd_do_window(mblk,TRUE, FALSE)) {
                            if (psnd_make_visible(mblk, mblk->info->block_id, S_WINDOW, FALSE))
                                psnd_1d_reset_connection(mblk);
                        }
                        psnd_plot(mblk,TRUE,mblk->info->dimension_id);
                    }
                    break;
                case PSND_DO_WINDOW:
                    psnd_showwindow_popup(mblk);
                    break;
#ifdef oldold
                    
                    if (!psnd_do_window(mblk,TRUE,TRUE))
                        break; 
                    if (psnd_make_visible(mblk, mblk->info->block_id, S_WINDOW, FALSE))
                        psnd_1d_reset_connection(mblk);
                    psnd_plot(mblk,TRUE,mblk->info->dimension_id);
                    break;
                case PSND_SHOW_WINDOW:
                    if (psnd_do_window(mblk,FALSE,TRUE)) {
                        if (psnd_make_visible(mblk, mblk->info->block_id, S_WINDOW, TRUE))
                            psnd_1d_reset_connection(mblk);
                        psnd_plot(mblk,TRUE,mblk->info->dimension_id);
                        break;
                    }
                    /* Fall through */
                case PSND_HIDE_WINDOW:
                    if (psnd_make_visible(mblk, mblk->info->block_id, S_WINDOW, FALSE)) {
                        psnd_1d_reset_connection(mblk);
                        psnd_plot(mblk,TRUE,mblk->info->dimension_id);
                    }
                    break;
#endif
                case PSND_SHOW_BASELINE:
                case PSND_BASELINE:
                    psnd_showbaseline_popup(mblk);
                    break;
                case PSND_BC:
                    if (psnd_baseline(mblk, TRUE, FALSE, mblk->cpar_screen)) 
                        psnd_plot(mblk,TRUE,mblk->info->dimension_id);
                    break;


                /* plot */
                case PSND_PL:
                    if (mblk->info->plot_mode == PLOT_2D) { 
                        int vp_id = psnd_get_vp_id(mblk);
                        if (vp_id == mblk->spar[S_PHASE1].vp_id) {
                            psnd_set_button_phase_block(mblk, vp_id);
                            psnd_reset_connection(mblk,vp_id);
                            psnd_plot1d_phase(mblk, S_PHASE1, VP_PHASE1, 
                                mblk->cpar_screen->phase_size, 
                                mblk->cpar_screen->xreal[0],mblk->info->auto_scale);
                        }
                        else if (vp_id == mblk->spar[S_PHASE2].vp_id) {
                            psnd_set_button_phase_block(mblk, vp_id);
                            psnd_reset_connection(mblk,vp_id);
                            psnd_plot1d_phase(mblk, S_PHASE2, VP_PHASE2, 
                                mblk->cpar_screen->phase_size, 
                                mblk->cpar_screen->xreal[1],mblk->info->auto_scale);
                        }
                        else if (vp_id == mblk->spar[S_PHASE3].vp_id)  {
                            psnd_set_button_phase_block(mblk, vp_id);
                            psnd_reset_connection(mblk,vp_id);
                            psnd_plot1d_phase(mblk, S_PHASE3, VP_PHASE3,
                                mblk->cpar_screen->phase_size, 
                                mblk->cpar_screen->xreal[2],mblk->info->auto_scale);
                        }
                        else {
                            psnd_set_button_phase_block(mblk, vp_id);
                            psnd_select_contourviewport(mblk);
                            psnd_plot1d_in_2d(mblk, (PAR->icrdir!=DAT->access[0]),
                                                0,FALSE);
                        }
                        g_clear_viewport();
                        g_plotall();
                        break;
                    }                   
                    psnd_plot(mblk,TRUE,mblk->info->dimension_id);
                    break;
                /* toggle plot real */
                case PSND_RE:
                    psnd_make_visible(mblk, mblk->info->block_id, S_REAL, TOGGLE);
                    psnd_1d_reset_connection(mblk);
                    psnd_plot(mblk,TRUE,mblk->info->dimension_id);
                    break;
                /* toggle plot imaginary */
                case PSND_IM:
                    psnd_make_visible(mblk, mblk->info->block_id, S_IMAG, TOGGLE);
                    psnd_1d_reset_connection(mblk);
                    psnd_plot(mblk,TRUE,mblk->info->dimension_id);
                    break;
                /* toggle plot buffer A */
                case PSND_BA:
                    psnd_make_visible(mblk, mblk->info->block_id, S_BUFR1, TOGGLE);
                    psnd_1d_reset_connection(mblk);
                    psnd_plot(mblk,TRUE,mblk->info->dimension_id);
                    break;
                /* toggle plot buffer B */
                case PSND_BB:
                    psnd_make_visible(mblk, mblk->info->block_id, S_BUFR2, TOGGLE);
                    psnd_1d_reset_connection(mblk);
                    psnd_plot(mblk,TRUE,mblk->info->dimension_id);
                    break;
                /* toggle plot window */
                case PSND_WD:
                    psnd_make_visible(mblk, mblk->info->block_id, S_WINDOW, TOGGLE);
                    psnd_1d_reset_connection(mblk);
                    psnd_plot(mblk,TRUE,mblk->info->dimension_id);
                    break;
                /* toggle baseline */
                case PSND_BL:
                    psnd_make_visible(mblk, mblk->info->block_id, S_BASELN, TOGGLE);
                    psnd_1d_reset_connection(mblk);
                    psnd_plot(mblk,TRUE,mblk->info->dimension_id);
                    break;
                /* erase */
                case PSND_ER:
                    if (mblk->info->append_plot) 
                        psnd_init_objects(mblk);
                    g_newpage();
                    break;

                /* throw away part of real and imag array */
                case PSND_IC:
                    if (argc > 2 && ui.keycode == PSND_IC)  {
                        if (psnd_arrayclip(mblk,argc, argv, 0, 0))
                            psnd_plot(mblk,TRUE,mblk->info->dimension_id);
                        break;
                    }

                /* left rotate complex array */
                case PSND_ROL: 
                    if (argc > 1 && ui.keycode == PSND_ROL)  {
                        if (psnd_rotate_array(argc,argv,1,0,DAT))
                            psnd_plot(mblk,TRUE,mblk->info->dimension_id);
                        break;
                    }

                /* right rotate complex array */
                case PSND_ROR: 
                    if (argc > 1 && ui.keycode == PSND_ROR)  {
                        if (psnd_rotate_array(argc,argv,-1,0,DAT))
                            psnd_plot(mblk,TRUE,mblk->info->dimension_id);
                        break;
                    }

                /* left shift complex array */
                case PSND_LS: 
                    if (argc > 1 && ui.keycode == PSND_LS)  {
                        if (psnd_shift_array(argc,argv,1,0,DAT))
                            psnd_plot(mblk,TRUE,mblk->info->dimension_id);
                        break;
                    }

                /* right shift complex array */
                case PSND_RS: 
                    if (argc > 1 && ui.keycode == PSND_RS)  {
                        if (psnd_shift_array(argc,argv,-1,0,DAT))
                            psnd_plot(mblk,TRUE,mblk->info->dimension_id);
                        break;
                    }

                /* reverse real and imag array */
                case PSND_RV:
                    if (argc > 0 && ui.keycode == PSND_RV)  {
                        psnd_arrayreverse(PAR,DAT);
                        psnd_plot(mblk,TRUE,mblk->info->dimension_id);
                        break;
                    }
                /* negate real and imag array */
                case PSND_NM:
                    if (argc > 0 && ui.keycode == PSND_NM)  {
                        psnd_arraynegate(DAT);
                        psnd_plot(mblk,TRUE,mblk->info->dimension_id);
                        break;
                    }

                    if (psnd_array_popup(mblk, ui.keycode))
                        psnd_plot(mblk,TRUE,mblk->info->dimension_id);
                    break;
#ifdef oldarray
                /*
                 * Array manipulation
                 */
                /* throw away part of real and imag array */
                case PSND_IC:
                    if (psnd_arrayclip(mblk,argc, argv, 0, 0))
                        psnd_plot(mblk,TRUE,mblk->info->dimension_id);
                    break;
                /* left rotate complex array */
                case PSND_ROL: {
                    int ival = 1;
                    if (argc == 0 && psnd_ivalin(mblk, "ROR: number of points",1, &ival) == 0)
                        break;
                    if (psnd_rotate_array(argc,argv,1,ival,DAT))
                        psnd_plot(mblk,TRUE,mblk->info->dimension_id);
                    }
                    break;
                /* right rotate complex array */
                case PSND_ROR: {
                    int ival = 1;
                    if (argc == 0 && psnd_ivalin(mblk, "ROR: number of points",1, &ival) == 0)
                        break;
                    if (psnd_rotate_array(argc,argv,-1,ival,DAT))
                        psnd_plot(mblk,TRUE,mblk->info->dimension_id);
                    }
                    break;
                /* left shift complex array */
                case PSND_LS: {
                    int ival = 1;
                    if (argc == 0 && psnd_ivalin(mblk, "LS: number of points",1, &ival) == 0)
                        break;
                    if (psnd_shift_array(argc,argv,1,ival,DAT))
                        psnd_plot(mblk,TRUE,mblk->info->dimension_id);
                    }
                    break;
                /* right shift complex array */
                case PSND_RS: {
                    int ival = 1;
                    if (argc == 0 && psnd_ivalin(mblk, "RS: number of points",1, &ival) == 0)
                        break;
                    if (psnd_shift_array(argc,argv,-1,ival,DAT))
                        psnd_plot(mblk,TRUE,mblk->info->dimension_id);
                    }
                    break;
                /* reverse real and imag array */
                case PSND_RV:
                    if (argc == 0 && psnd_set_param(mblk, 0,  NULL,  PSND_XM) == 0)
                        break;
                    psnd_arrayreverse(PAR,DAT);
                    psnd_plot(mblk,TRUE,mblk->info->dimension_id);
                    break;
                /* negate real and imag array */
                case PSND_NM:
                    psnd_arraynegate(DAT);
                    psnd_plot(mblk,TRUE,mblk->info->dimension_id);
                    break;
#endif
                /* zero real and imag array */
                case PSND_ZE:
                    if (argc > 0 && ui.keycode == PSND_ZE)  {
                        psnd_arrayfill(0,NULL,0, 0.0,PAR,DAT);
                        psnd_arrayfill(0,NULL,1, 0.0,PAR,DAT);
                        psnd_plot(mblk,TRUE,mblk->info->dimension_id);
                        break;
                    }
                    
                /* fill real and imag array with 1*/
                case PSND_Z1:
                    if (argc > 0 && ui.keycode == PSND_Z1)  {
                        psnd_arrayfill(0,NULL,0, 1.0,PAR,DAT);
                        psnd_arrayfill(0,NULL,1, 1.0,PAR,DAT);
                        psnd_plot(mblk,TRUE,mblk->info->dimension_id);
                        break;
                    }
                    
                /* fill real array with value */
                case PSND_ZR:
                    if (argc > 0 && ui.keycode == PSND_ZR)  {
                        float val = 0.0;
                        /*
                        if (argc < 2)
                            if (psnd_rvalin(mblk, " set real array ", 1, &val) == 0)
                               break;
                        */
                        if (argc > 1)
                            sscanf(argv[1], "%g", &val);
                        psnd_arrayfill(argc, argv, 0, val,PAR,DAT);
                        psnd_plot(mblk,TRUE,mblk->info->dimension_id);
                        break;
                    }

                /* fill imaginary array with value */
                case PSND_ZI:
                    if (argc > 0 && ui.keycode == PSND_ZI)  {
                        float val = 0.0;
                        /*
                        if (argc < 2)
                            if (psnd_rvalin(mblk, " set imag array ", 1, &val) == 0)
                                break;     
                        */                      
                        psnd_arrayfill(argc, argv, 1, val,PAR,DAT);
                        if (argc > 1)
                            sscanf(argv[1], "%g", &val);
                        psnd_plot(mblk,TRUE,mblk->info->dimension_id);
                        break;
                       
                    }

                /* fill buffer array with value */
                case PSND_ZA:
                case PSND_ZB:
                    if (argc > 0 && (ui.keycode == PSND_ZA || ui.keycode == PSND_ZB))  {
                        float val = 0.0;
                        int buff_id;
                        if (ui.keycode==PSND_ZA)
                            buff_id = 2;
                        else
                            buff_id = 3;
                            /*
                        if (argc < 2)
                            if (psnd_rvalin(mblk, " set buffer array ", 1, &val) == 0)
                                break;
                                */
                        if (argc > 1)
                            sscanf(argv[1], "%g", &val);
                        psnd_arrayfill(0,NULL,buff_id, val,PAR,DAT);
                        psnd_plot(mblk,TRUE,mblk->info->dimension_id);
                        break;
                    }

                /* fill window array with value */
                case PSND_ZW:
                    if (argc > 0 && ui.keycode == PSND_ZW)  {
                        float val = 0.0;
                        /*
                        if (argc < 2)
                            if (psnd_rvalin(mblk, " set window array ", 1, &val) == 0)
                                break;
                                */
                        if (argc > 1)
                            sscanf(argv[1], "%g", &val);
                        psnd_arrayfill(argc, argv ,4, val,PAR,DAT);
                        psnd_plot(mblk,TRUE,mblk->info->dimension_id);
                        break;
                    }
                   
                /* fill baseLine array with value */
                case PSND_ZL:
                    if (argc > 0 && ui.keycode == PSND_ZL)  {
                        float val = 0.0;
                        /*
                        if (argc < 2)
                            if (psnd_rvalin(mblk, " set baseline array ", 1, &val) == 0)
                                break;
                                */
                        if (argc > 1)
                            sscanf(argv[1], "%g", &val);
                        psnd_arrayfill(argc, argv ,5, val,PAR,DAT);
                        psnd_plot(mblk,TRUE,mblk->info->dimension_id);
                        break;
                    }

                    if (psnd_arrayfill_popup(mblk, ui.keycode))
                        psnd_plot(mblk,TRUE,mblk->info->dimension_id);
                    break;

                /* move array */
                case PSND_MV:
                    psnd_arraymove(mblk);
                    psnd_plot(mblk,TRUE,mblk->info->dimension_id);
                    break;
                /* average real and imag array, absolute value */
                case PSND_AV:
                    if (argc == 0)   {
                        if (g_popup_messagebox2(ui.win_id, "Absolute Value",
		             "sqrt ( r * r + i * i )","YES","NO") == G_POPUP_BUTTON2)
                              break;
                    }
                    psnd_arrayaverage(DAT);
                    psnd_plot(mblk,TRUE,mblk->info->dimension_id);
                    break;
                /* power spectrum */
                case PSND_PS:
                    if (argc == 0)   {
                        if (g_popup_messagebox2(ui.win_id, "Power Spectrum",
		             " ( r * r + i * i ) / r_max","YES","NO") == G_POPUP_BUTTON2)
                              break;
                    }
                    psnd_arraypower(DAT);
                    psnd_plot(mblk,TRUE,mblk->info->dimension_id);
                    break;
                /* transfer parameters i to j */
                case PSND_TP:
                /* transfer window i to j */
                case PSND_TW:
                /* transfer buffer i to j */
                case PSND_TB:
                /* transfer block (=real + imag)i to j */
                case PSND_TR:
                /* multpily block (=real + imag) i with j */
                case PSND_MX:
                /* additive transver */
                case PSND_AT:
                /* block swap */
                case PSND_IJ:
                    if (psnd_blockoperations(mblk, ui.keycode))
                        psnd_plot(mblk,TRUE,mblk->info->dimension_id);
                    break;
                /* Allocate more blocks */
                case PSND_SET_NUM_BLOCKS:
                    {
                        int num_blks = psnd_popup_numblocks(mblk);
                        num_blks = min(num_blks, MAXBLK);
                        if (num_blks > mblk->info->max_block) {
                            int i,from = mblk->info->max_block;
                            allocate_blocks(mblk, from, num_blks);
                            mblk->info->max_block = num_blks;
                            for (i=from;i<num_blks;i++)
                                psnd_param_reset(mblk, i);
                            psnd_realloc_buffers(mblk, mblk->info->block_size);
                            psnd_enable_viewselect_blocks(mblk);
                            psnd_enable_linkselect_blocks(mblk, from, num_blks);
                        }
                    }
                    break;
                /* Select Block */
                case PSND_SELECT_BLOCK:
                    {
                        int id = psnd_popup_block_select(mblk, mblk->info->win_id, 
                                                              mblk->info->block_id+1);
                        if (id > 0)
                            psnd_change_block(mblk, id-1, TRUE);
                    }
                    break;
                case PSND_OU:
                    psnd_arraywrite(mblk, TRUE, argc, argv, DAT);
                    break;
                case PSND_WR:
                    psnd_arraywrite(mblk, FALSE, 0, NULL, DAT);
                    break;
                case PSND_RD:
                    psnd_arrayread(mblk, DAT);
                    break;
                case PSND_1:
                case PSND_2:
                case PSND_3:
                case PSND_4:
                case PSND_5:
                case PSND_6:
                case PSND_7:
                case PSND_8:
                case PSND_9:
                case PSND_10:
                case PSND_11:
                case PSND_12:
                    if (ui.keycode - PSND_1 + 1 > mblk->info->max_block)
                        break;
                    if (mblk->info->plot_mode == PLOT_2D)
                        g_bell();
                    else
                        psnd_change_block(mblk, ui.keycode - PSND_1, TRUE);
                    break;
                case PSND_GP:
                    psnd_getparam(mblk, TRUE);
                    break;
                case PSND_GE:
                case PSND_FILE_OPEN:
                    {
                    int k,keys[MAX_DIM];
                    if (!psnd_open(mblk, argc, argv)) {
                        psnd_enable_open_buttons(mblk, DAT->finfo[INFILE].fopen);
                        break;
                    }
                    psnd_push_waitcursor(mblk);
                    for (k=0;k<DAT->ityp;k++)
                        DAT->access[k] = DAT->ityp-k;
                    if (argc > 1) {
                        if (!psnd_direction(mblk, argc-1, argv+1, 0, NULL)) {
                            psnd_close_file(mblk);
                            psnd_pop_waitcursor(mblk);
                            psnd_enable_open_buttons(mblk, DAT->finfo[INFILE].fopen);
                            break;
                        }
                    }
                    else
                        if (!psnd_direction(mblk, 0, NULL, 0, NULL)) {
                            psnd_close_file(mblk);
                            psnd_pop_waitcursor(mblk);
                            psnd_enable_open_buttons(mblk, DAT->finfo[INFILE].fopen);
                            break;  
                        }                  
                    if (!psnd_getparam(mblk, FALSE)) {
                        psnd_pop_waitcursor(mblk);
                        psnd_enable_open_buttons(mblk, DAT->finfo[INFILE].fopen);
                        break;
                    }
                    for (k=0;k<MAX_DIM;k++)
                        keys[k] =1;

                    if (!psnd_rn(mblk, DAT->ityp, keys, DAT)){
                        psnd_pop_waitcursor(mblk);
                        break;
                    }
                    if (mblk->info->plot_mode == PLOT_2D) {
                        /*
                        psnd_push_waitcursor(mblk);
                        */
                        psnd_plane(mblk, TRUE, PLANE_THIS, 0,
                                    CONTOUR_BLOCK);
                        psnd_pop_waitcursor(mblk);
                        psnd_enable_open_buttons(mblk, DAT->finfo[INFILE].fopen);
                        break;
                    }
                    psnd_plot(mblk,TRUE,mblk->info->dimension_id);
                    psnd_pop_waitcursor(mblk);
                    psnd_clear_popup_bookmark(mblk, DAT);
                    psnd_enable_open_buttons(mblk, DAT->finfo[INFILE].fopen);
                    }
                    break;
                case PSND_FILE_CLOSE:
                    psnd_clear_popup_bookmark(mblk, DAT);
                    psnd_close_file(mblk);
                    psnd_enable_open_buttons(mblk, DAT->finfo[INFILE].fopen);
                    break;
                case PSND_DR:
                    if (!psnd_direction(mblk, argc, argv, 0, NULL))
                        break;
                    if (mblk->info->plot_mode == PLOT_2D) {


                        psnd_push_waitcursor(mblk);
                        psnd_plane(mblk, TRUE, PLANE_THIS, 0,
                                    CONTOUR_BLOCK_NEW);
                        psnd_pop_waitcursor(mblk);
                        psnd_nextrec(mblk, TRUE, 0,DAT->pars,DAT);
                        break;
                    }
                    psnd_nextrec(mblk, TRUE,0,DAT->pars,DAT);
                    psnd_plot(mblk,TRUE,mblk->info->dimension_id);
                    psnd_adjust_labels_to_new_direction(mblk);
                    break;
                case PSND_RTR: 
                    psnd_returnrec(mblk, TRUE);
                    break;
                case PSND_RN: {
                    int key3 = DAT->pars[2]->key;
                    if (psnd_read_record(mblk, argc, argv, DAT)) {
                        if (mblk->info->plot_mode == PLOT_2D) {
                            if (key3 != DAT->pars[2]->key) {

                                psnd_push_waitcursor(mblk);
                                psnd_plane(mblk, TRUE, PLANE_READ, key3,
                                            CONTOUR_BLOCK_NEW);
                                psnd_pop_waitcursor(mblk);
                            }
                        }
                        psnd_plot(mblk,TRUE,mblk->info->dimension_id);
                    }
                    }
                    break;
                case PSND_ST:
                    /*
                     * Store parameters on disk:
                     * when issued from commandline with no arguments
                     * stores in default direction
                     */
                    if (from_commandline && argc == 1) {
                        argc++;
                        argv[1] = argspace + strlen(argv[0]) + 1;
                        sprintf(argv[1], "%d", mblk->info->dimension_id+1);
                    }
                    psnd_setparam(mblk, argc, argv);
                    break;

        case ID_MOUSE_POS:
        case ID_MOUSE_SELECT: 
        case ID_MOUSE_REGION: 
        case ID_MOUSE_SPLINE: 
        case ID_MOUSE_ZOOMXY:
        case ID_MOUSE_ZOOMX: 
        case ID_MOUSE_ZOOMY:
        case ID_MOUSE_PHASE_P0:
        case ID_MOUSE_PHASE_P1:
        case ID_MOUSE_PHASE_I0:
        case ID_MOUSE_CALIBRATE:
        case ID_MOUSE_INTEGRATE:
        case ID_MOUSE_PEAKPICK:
        case ID_MOUSE_DISTANCE:
        case ID_MOUSE_SCALE:
        case ID_MOUSE_NOISE: 
        case ID_MOUSE_ROWCOL:
        case ID_MOUSE_SUM: 
        case ID_MOUSE_ROWCOL_PLANE:
                    psnd_process_mousebar_messages(mblk, ui.keycode);
                    break;

                /* set axis units */
                case PSND_CHAN:
                    mblk->spar[S_AXISX_MARK].show = AXIS_UNITS_CHANNELS;
                    mblk->spar[S_AXISY_MARK].show = AXIS_UNITS_CHANNELS;
                    mblk->spar[S_AXISX].show = TRUE;
                    mblk->spar[S_AXISY].show = TRUE;
                    psnd_plotaxis(mblk,PAR);
                    g_plotall();
                    break;
                case PSND_PPM:
                    mblk->spar[S_AXISX_MARK].show = AXIS_UNITS_PPM;
                    mblk->spar[S_AXISY_MARK].show = AXIS_UNITS_PPM;
                    mblk->spar[S_AXISX].show = TRUE;
                    mblk->spar[S_AXISY].show = TRUE;
                    psnd_plotaxis(mblk,PAR);
                    g_plotall();
                    break;
                case PSND_HZ:
                    mblk->spar[S_AXISX_MARK].show = AXIS_UNITS_HERTZ;
                    mblk->spar[S_AXISY_MARK].show = AXIS_UNITS_HERTZ;
                    mblk->spar[S_AXISX].show = TRUE;
                    mblk->spar[S_AXISY].show = TRUE;
                    psnd_plotaxis(mblk,PAR);
                    g_plotall();
                    break;
                case PSND_SEC:
                    mblk->spar[S_AXISX_MARK].show = AXIS_UNITS_SECONDS;
                    mblk->spar[S_AXISY_MARK].show = AXIS_UNITS_SECONDS;
                    mblk->spar[S_AXISX].show = TRUE;
                    mblk->spar[S_AXISY].show = TRUE;
                    psnd_plotaxis(mblk,PAR);
                    g_plotall();
                    break;
                case PSND_XAXIS:
                    if (psnd_popup_axis(mblk->info->win_id, mblk->info->plot_mode, mblk->spar))
                        g_plotall();
                    break;
                case PSND_FID:
                    PAR->isspec = FALSE;
                    DAT->isspec = FALSE;
                    psnd_plotaxis(mblk,PAR);
                    g_plotall();
                    break;
                case PSND_SPEC:
                    PAR->isspec = TRUE;
                    DAT->isspec = TRUE;
                    psnd_plotaxis(mblk,PAR);
                    g_plotall();
                    break;
                case PSND_DSPSHIFT:
                    if (argc > 1) {
                        PAR->dspshift = psnd_scan_float(argv[1]);
                        PAR->dspshift = max(0,PAR->dspshift);
                    }
                    else
                        psnd_set_param(mblk, 0,  NULL, ui.keycode);
                    break;
                case PSND_SI:
                    if (psnd_set_param(mblk,  argc,  argv, ui.keycode))
                        psnd_plot(mblk,TRUE,mblk->info->dimension_id);
                    break;
                case PSND_TD:
                    if (psnd_set_param(mblk,  argc,  argv, ui.keycode))
                        psnd_plot(mblk,TRUE,mblk->info->dimension_id);
                    break;
                case PSND_CC:
                case PSND_CI:
                case PSND_CM:
                    if (mblk->info->plot_mode == PLOT_1D)
                        break;
                case PSND_PARAM_HILBERT:
                case PSND_PARAM_WATWA:
                case PSND_PARAM_LINPAR:
                case PSND_PARAM_POST:
                case PSND_PARAM_FOURIER:
                case PSND_PARAM_WINDOW:
                case PSND_PARAM_OUTFILE:
                case PSND_PARAM_WATERFIT:
                case PSND_AD:
                case PSND_ND:
                case PSND_SW:
                case PSND_SF:
                case PSND_RM:
                case PSND_WM:
                case PSND_SH:
                case PSND_LB:
                case PSND_GB:
                case PSND_IT:
                case PSND_FM:
                case PSND_FTSCALE:
                case PSND_ZF:
                case PSND_SM:
                case PSND_XM:
                case PSND_BM:
                case PSND_BT:
                case PSND_TRUNC_LEVEL:
                    psnd_set_param(mblk, argc,  argv,  ui.keycode);
                    break;
                case PSND_XR:
                case PSND_AREF:
                case PSND_BREF:
                case PSND_PARAM_FREQ:
                    psnd_set_param(mblk, argc,  argv, ui.keycode);
                    psnd_refresh_calibrate_labels(mblk);
                    break;
                case PSND_PA:
                case PSND_PB:
                case PSND_I0:
                case PSND_PARAM_PHASE:
                    psnd_set_param(mblk, argc,  argv, ui.keycode);
                    psnd_refresh_phase_labels(mblk);
                    break;
                case PSND_PARAM_ALL:
                    psnd_edit_all_param(mblk);
                    psnd_refresh_phase_labels(mblk);
                    psnd_refresh_calibrate_labels(mblk);
                    break;
                case PSND_PARAM_RAW:
                    if (psnd_edit_raw_param(mblk)) {
                        psnd_update_raw_param(mblk);
                        psnd_getparam(mblk, TRUE);
                        psnd_refresh_phase_labels(mblk);
                        psnd_refresh_calibrate_labels(mblk);
                    }
                    break;
                case PSND_POPUP_PHASE:
                    psnd_popup_phase(mblk);
                    break;
                case PSND_PK:
                    if (psnd_do_pk(mblk, argc, argv))
                        psnd_plot(mblk,TRUE,mblk->info->dimension_id);
                    break;
                case PSND_APK:
                    if (psnd_auto_phase(mblk, MODE_P0+MODE_P1))
                        psnd_plot(mblk,TRUE,mblk->info->dimension_id);
                    break;
                case PSND_PZ:
                    psnd_pz(mblk, PAR);
                    break;
                case PSND_PN:
                    psnd_set_phase_position(mblk, PAR, DAT, 1);
                    break;
		case PSND_DOHC:
                    psnd_hardcopy(mblk);
		    break;

		case PSND_PLOT_CONTOUR:
                    if (mblk->info->plot_mode == PLOT_2D && DAT->ityp > 1) {
                        int mode = mblk->cpar_screen->mode;
                        int ok = psnd_hardcopy_param1d2d(mblk, TRUE);
                        if (ok)
                            psnd_paperplot_contour(mblk, ok-1);
                        mblk->cpar_screen->mode   = mode;
                    }
		    break;

		case PSND_PLOT_1D:
                    if (mblk->info->plot_mode == PLOT_1D /* && DAT->ityp > 0 */) {
                        int ok = psnd_hardcopy_param1d2d(mblk, FALSE);
                        if (ok)
                            psnd_paperplot_1d(mblk,ok-1);
                    }
		    break;

                case PSND_LPC:
                    /*
                     * From menu
                     */
                    if (argc == 0)
                        psnd_showlpc_popup(mblk);
                    /*
                     * From command line
                     */
                    else
                        if (psnd_linearprediction(mblk))
                            psnd_plot(mblk,TRUE,mblk->info->dimension_id);
                    break;

                case PSND_WATWA:
                    if (psnd_waterwash(argc, argv, mblk))
                        psnd_plot(mblk,TRUE,mblk->info->dimension_id);
                    break;

                case PSND_FZ:
                    if (psnd_fillzero(mblk, argc, argv))
                        psnd_plot(mblk,TRUE,mblk->info->dimension_id);
                    break;

                case PSND_COLORMAP:
                    psnd_show_colormap(mblk, g_get_palettesize());
                    break;

                case PSND_EX: {
                        char buf[225], *p;
                        int k;
                        p = buf;
                        for (k=1;k<argc;k++) {
                            sprintf(p, "%s ", argv[k]);
                            p += strlen(p);
                        }
                        system(buf);
                    }
                    break;

                case PSND_POPUP_ADJUST:
                    psnd_popup_viewadjust(mblk);
                    break;
                    
                case PSND_VIEW:
                    psnd_popup_viewselect(mblk);
                    break;
                    
                case PSND_LINKVIEW:
                    psnd_popup_linkselect(mblk);
                    break;
                    
                case PSND_PLANE_READ:
                    if (mblk->info->plot_mode == PLOT_2D && DAT->ityp > 2) {
                        int k,key,keys[MAX_DIM];
                        if (argc <2) {
                            key = DAT->pars[2]->key;
                            if (psnd_ivalin(mblk, "Plane number",1,&key) == 0)
                                break;
                        }
                        else
                            key =psnd_scan_integer(argv[1]);
                        psnd_push_waitcursor(mblk);
                        psnd_plane(mblk, TRUE, PLANE_READ, key,
                                    CONTOUR_BLOCK_NEW);
                        psnd_pop_waitcursor(mblk);
                        break;
                    }
                    break;

                case PSND_PLANE_UP:
                    if (mblk->info->plot_mode == PLOT_2D && DAT->ityp > 2) {
                        
                        psnd_push_waitcursor(mblk);
                        psnd_plane(mblk, TRUE, PLANE_UP, DAT->pars[2]->key + 1,
                                    CONTOUR_BLOCK_NEW);
                        psnd_pop_waitcursor(mblk);
                        break;
                    }
                    break;

                case PSND_PLANE_DOWN:
                    if (mblk->info->plot_mode == PLOT_2D && DAT->ityp > 2) {
                        psnd_push_waitcursor(mblk);
                        psnd_plane(mblk, TRUE, PLANE_DOWN, DAT->pars[2]->key - 1,
                                    CONTOUR_BLOCK_NEW);
                        psnd_pop_waitcursor(mblk);
                        break;
                    }
                    break;

                case PSND_PLANE_REDO :
                case PSND_PLANE_UNDO :
                    if (mblk->info->plot_mode == PLOT_2D && DAT->ityp > 1) {
                        psnd_push_waitcursor(mblk);
                        psnd_pop_plane(mblk, ui.keycode, CONTOUR_BLOCK_NEW);
                        psnd_pop_waitcursor(mblk);
                    }
                    break;

                case PSND_DOKEY_PLUS :
                    {
                    int k;
                    PBLOCK *pars[MAX_DIM];
                    for (k=2;k<MAX_DIM;k++)
                        pars[k] = DAT->pars[k];
                    if (DAT->access[0]-1 == mblk->info->dimension_id) {
                        pars[0] = DAT->pars[0];
                        pars[1] = DAT->pars[1];
                    }
                    else if (DAT->access[1]-1 == mblk->info->dimension_id) {
                        pars[0] = DAT->pars[1];
                        pars[1] = DAT->pars[0];
                    }
                    else {
                        pars[0] = DAT->pars[2];
                        pars[1] = DAT->pars[0];
                        pars[2] = DAT->pars[1];
                    }
                    psnd_nextrec(mblk, TRUE,1,pars,DAT);
                    if (mblk->info->plot_mode == PLOT_2D) { 
                        psnd_select_contourviewport(mblk);
                        psnd_plot1d_in_2d(mblk, (PAR->icrdir!=DAT->access[0]),0,FALSE);
                        g_clear_viewport();
                        g_plotall();
                        break;
                    }                   
                    psnd_plot(mblk,TRUE,mblk->info->dimension_id);
                    }
                    break;
                    
                case PSND_DOKEY_MINUS :
                    {
                    int k;
                    PBLOCK *pars[MAX_DIM];
                    for (k=2;k<MAX_DIM;k++)
                        pars[k] = DAT->pars[k];
                    if (DAT->access[0]-1 == mblk->info->dimension_id) {
                        pars[0] = DAT->pars[0];
                        pars[1] = DAT->pars[1];
                    }
                    else if (DAT->access[1]-1 == mblk->info->dimension_id) {
                        pars[0] = DAT->pars[1];
                        pars[1] = DAT->pars[0];
                    }
                    else {
                        pars[0] = DAT->pars[2];
                        pars[1] = DAT->pars[0];
                        pars[2] = DAT->pars[1];
                    }
                    psnd_nextrec(mblk, TRUE,-1,pars,DAT);
                    if (mblk->info->plot_mode == PLOT_2D) { 
                        psnd_select_contourviewport(mblk);
                        psnd_plot1d_in_2d(mblk,(PAR->icrdir!=DAT->access[0]),0,FALSE);
                        g_clear_viewport();
                        g_plotall();
                        break;
                    }                   
                    psnd_plot(mblk,TRUE,mblk->info->dimension_id);
                    }
                    break;

                case PSND_UP :
                    if (mblk->info->plot_mode == PLOT_2D && DAT->ityp > 1) { 
                        psnd_select_contourviewport(mblk);
                        mblk->cpar_screen->clevel[0] *= 1.5;
                        psnd_push_waitcursor(mblk);
                        psnd_process_contour_levels(mblk,mblk->cpar_screen);
                        psnd_process_color_levels(mblk, mblk->cpar_screen);
                        psnd_cp(mblk,mblk->cpar_screen->mode);
                        psnd_pop_waitcursor(mblk);
                    }                   
                    break;
                    
                case PSND_DOWN :
                    if (mblk->info->plot_mode == PLOT_2D && DAT->ityp > 1) { 
                        psnd_select_contourviewport(mblk);
                        mblk->cpar_screen->clevel[0] *= 0.67;
                        psnd_push_waitcursor(mblk);
                        psnd_process_contour_levels(mblk,mblk->cpar_screen);
                        psnd_process_color_levels(mblk, mblk->cpar_screen);
                        psnd_cp(mblk,mblk->cpar_screen->mode);
                        psnd_pop_waitcursor(mblk);
                    }                   
                    break;

                case PSND_REDO :
                case PSND_UNDO :
                    if (mblk->info->plot_mode == PLOT_2D && DAT->ityp > 1) {
                        mblk->info->dimension_id = psnd_poprec(mblk, ui.keycode);
                        psnd_select_contourviewport(mblk);
                        psnd_plot1d_in_2d(mblk,(PAR->icrdir!=DAT->access[0]),0,FALSE);
                        g_clear_viewport();
                        g_plotall();
                    }
                    else {
                        mblk->info->dimension_id = psnd_poprec(mblk, ui.keycode);
                        psnd_plot(mblk,TRUE,mblk->info->dimension_id);
                        psnd_adjust_labels_to_new_direction(mblk);
                    }
                    break;

                case PSND_LP :
                    psnd_lp(mblk, PAR);
                    break;
                    
                case PSND_LAC :
                    psnd_list_all_current_parameters(mblk,DAT->ityp,TRUE,DAT,PAR0(0));
                    break;
                    
                case PSND_LA :
                    psnd_la(mblk,DAT->ityp,1,TRUE,DAT);
                    break;
                    
                case PSND_HI :
                    psnd_hi(mblk, argc, argv, (argc >= 2));
                    break;
                    
                case PSND_HI_FILE :
                    psnd_hi(mblk, argc, argv, TRUE);
                    break;
                    
                case PSND_AQ :
                    psnd_list_aquisition_parameters(mblk);
                    break;
                    
                case PSND_AU :
                    psnd_push_waitcursor(mblk);
                    psnd_au(mblk);
                    psnd_plot(mblk,TRUE,mblk->info->dimension_id);
                    psnd_pop_waitcursor(mblk);
                    break;
                    
                case PSND_RUN_SCRIPT:
                    {
                    int ok = FALSE;
                    if (argc <= 1)
                        ok = psnd_au_script(mblk);
                    else {
                        if (psnd_read_script_in_buffer(argv[1])) {
                            ok = psnd_run_au_from_commandline(mblk,NULL, 
                                    argc-1,argv+1);
                        }
                        else
                            psnd_printf(mblk, "Can not open file: %s\n",argv[1]);
                    }
                    if (ok)
                        psnd_plot(mblk,TRUE,mblk->info->dimension_id);
                    }
                    break;
                      
                case PSND_FILE_PREPA:
                    if (!psnd_popup_prepa_setup(mblk))
                        break;
                    /* Fall through */
                case PSND_FILE_SAVEAS:
                    if (ui.keycode == PSND_FILE_SAVEAS) {
                        if (!DAT->finfo[INFILE].fopen)
                            break;
                        if (!psnd_popup_output_setup(mblk))
                            break;
                    }
                    /* Fall through */
                case PSND_SCRIPT:
                    {
                    char *p;
                    int ok = FALSE;
                    if ((p=psnd_popup_run_script(mblk, FALSE))!=NULL) {
                        parse_command(p, &argc, argv);
                        if (argc <= 1)
                             ok=psnd_au_script(mblk);
                        else 
                             ok=psnd_run_au_from_commandline(mblk,NULL, 
                                    argc,argv);
                    }
                    if (ok)
                        psnd_plot(mblk,TRUE,mblk->info->dimension_id);
                    }
                    break;
                    
                case PSND_READ_SCRIPT:
                    if (psnd_read_script(mblk)) {
                        psnd_au_script(mblk);
                        psnd_plot(mblk,TRUE,mblk->info->dimension_id);
                    }
                    break;
                    
                case PSND_POPUP_BOOKMARK :
                    psnd_popup_bookmark(mblk);
                    break;
                    
                case PSND_SHOW_GRID:
                    mblk->spar[S_GRID].show   = !mblk->spar[S_GRID].show;
                    g_menu_set_toggle(mblk->info->bar_id, PSND_SHOW_GRID, mblk->spar[S_GRID].show);

                    if (!mblk->spar[S_GRID].show) 
                        g_set_objectstatus(mblk->spar[S_GRID].obj_id, G_SLEEP);
                    else
                        psnd_plotgrid(mblk);
                    g_plotall();
                    break;

                case PSND_SPLINE_RESET:
                    psnd_spline_popup(mblk);
                    break;

                case PSND_PEAK_PICK :
                    if (mblk->info->plot_mode != PLOT_2D) 
                        psnd_peakpick1d_popup(mblk);
                    break;

                case PSND_POPUP_INT1D:
                    psnd_int1d_popup(mblk);
                    break;

                case PSND_CHANGEDIR : {
                    int err = 0;
                    char *dirname;
                    if (argc >= 2) 
                        dirname = argv[1];
                    else {
                        dirname = psnd_getdirname(mblk, "Get Directory");
                        if (dirname == NULL)
                            break;
                    }
                    err = chdir(dirname);
                    if (err)
                        psnd_printf(mblk, "  Failed to chdir to %s\n", dirname);
                    else
                        psnd_printf(mblk, "  Changed to %s\n", dirname);
                    }
                    break;


                case PSND_XTERM:
#ifdef _WIN32
                    g_open_console(10,10,600,200,"OUTPUT");
#else
                    g_raise_xterm();
#endif
                    break;

		default: break;
	    }
            break;

        case G_POINTERMOTION + G_BUTTON1PRESS: 
            switch (mblk->info->mouse_mode1) {
                case MOUSE_DISPLAY: {
                    G_POPUP_CHILDINFO ci;
                    int ib = mblk->info->adjust_display_block;
                    int is = mblk->info->adjust_display_item;
                    int io = mblk->info->adjust_display_operation & 0x3;
                    float fsens = pow(10,(float) mblk->info->adjust_display_sensitive-1);
                    if (ui.vp_id == mblk->spar[S_CONTOUR].vp_id && 
                        !mblk->spar[S_REAL].disable_auto_scale_2d)
                            break;

                    if (mblk->info->adjust_display_operation & 0x4)
                        io -= Y_SCALE;

                    g_translate_viewport_xy(ui.win_id, mblk->spar[S_BOX].vp_id, 
                                            ui.vp_id,
                                            &ui.x, &ui.y);
                    if (!a0_active) {
                        a0 = ui.y;
                        a0_active = TRUE;
                        break;
                    }
                    enable_motion_events(FALSE);
                    dy = (a0 - ui.y)/(yyy2-yyy1);
                    dy /= fsens;
                    a0 = ui.y;
                    mblk->spar_block[ib][is].dxy[io] -= dy; 
                    psnd_plot(mblk,TRUE,mblk->info->dimension_id);
                    g_popup_init_info(&ci);
                    ci.id = -1;
                    ci.userdata = (void*) mblk;
                    adjust_callback(&ci);
                    while (g_peek_event() & G_POINTERMOTION)
                        g_get_event(&ui);
                    enable_motion_events(TRUE);
                    }
                    break;
                case MOUSE_ROWCOLUMN: {
                    int swap = FALSE, both = FALSE;
                    if (mblk->info->plot_mode != PLOT_2D) 
                        break;
                    if (ui.vp_id != mblk->spar[S_CONTOUR].vp_id)
                        break;
                    if (mblk->info->contour_row == PLOT_PERPENDICULAR) 
                        break;
                    if (mblk->info->contour_row == PLOT_ROW) {
                        if (a0 == ui.y)
                            break;
                        a0 = ui.y;
                    }
                    else if (mblk->info->contour_row == PLOT_COLUMN) {
                        if (a0 == ui.x)
                            break;
                        a0 = ui.x;
                    }
                    enable_motion_events(FALSE);
                    psnd_select_contourviewport(mblk);
                    if (mblk->info->contour_row == PLOT_ROW) {
                        mblk->info->dimension_id = 
                            psnd_rec(mblk, FALSE, DIRECTION_ROW, round(ui.y),0);
                    }
                    else if (mblk->info->contour_row == PLOT_COLUMN) {
                        mblk->info->dimension_id = 
                            psnd_rec(mblk, FALSE, DIRECTION_COLUMN, round(ui.x),0);
                        swap = TRUE;
                    }
                    else  {
                        mblk->info->dimension_id = 
                            psnd_rec(mblk, FALSE, DIRECTION_COLUMN, round(ui.x),0);
                        memcpy(DAT->work1,DAT->xreal,sizeof(float)*DAT->pars[1]->nsiz);
                        mblk->info->dimension_id = 
                            psnd_rec(mblk, FALSE, DIRECTION_ROW, round(ui.y),0);
                        swap = TRUE;
                        both = TRUE;
                    }
                    psnd_plot1d_in_2d(mblk,swap,0,both);
                    g_clear_viewport();
                    g_plotall();
                    while (g_peek_event() & G_POINTERMOTION)
                        g_get_event(&ui);
                    enable_motion_events(TRUE);
                    }
                    break;
                case MOUSE_CONTOURLEVEL: {
                    float fmax, level, yy;
                    if (ui.vp_id != mblk->spar[S_CONTOUR].vp_id)
                        break;
                    /*
                     * Re-calculate coordinates to independent
                     * reference frame, e.g. S_BOX
                     */
                    g_translate_viewport_xy(ui.win_id, mblk->spar[S_BOX].vp_id, 
                                            ui.vp_id,
                                            &ui.x, &ui.y);
                    if (!a0_active) {
                        a0 = ui.y;
                        a0_active = TRUE;
                        break;
                    }
                    enable_motion_events(FALSE);
                    dy = (a0 - ui.y)/(yyy2-yyy1);
                    a0 = ui.y;
                    level = fabs(mblk->cpar_screen->clevel[0]);
                    if (mblk->cpar_screen->plusmin == LEVELS_NEGATIVE) 
                        fmax = fabs(mblk->cpar_screen->lowest);
                    else
                        fmax = fabs(mblk->cpar_screen->highest);
                    yy = dy * min(level, fmax - level);
                    /*
                     * Up is faster than down
                     */
                    if ((yy < 0 && level < fmax/2) ||
                        (yy > 0 && level > fmax/2))
                        yy *= 2;
                    level -= yy;
                    /*
                     * Level between noise and max
                     */
                    level = max(level, mblk->cpar_screen->rmsnoise/5);
                    level = min(level, fmax);
                    if (mblk->cpar_screen->clevel[0] != level) {
                        if (mblk->cpar_screen->plusmin == LEVELS_NEGATIVE) 
                            mblk->cpar_screen->clevel[0] = -level;
                        else
                            mblk->cpar_screen->clevel[0] = level;
                        psnd_process_contour_levels(mblk,mblk->cpar_screen);
                        psnd_process_color_levels(mblk, mblk->cpar_screen);
                        psnd_cp(mblk,CONTOUR_BLOCK);
                    }
                    while (g_peek_event() & G_POINTERMOTION)
                        g_get_event(&ui);
                    enable_motion_events(TRUE);
                    }
                    break;
                case MOUSE_P0:
                case MOUSE_P1:
                    {
                    float yy;
                    if (ui.vp_id != mblk->spar[S_REAL].vp_id &&
                        ui.vp_id != mblk->spar[S_PHASE1].vp_id &&
                        ui.vp_id != mblk->spar[S_PHASE2].vp_id &&
                        ui.vp_id != mblk->spar[S_PHASE3].vp_id)
                        break;
                    /*
                     * Re-calculate coordinates to independent
                     * reference frame, e.g. S_BOX
                     */
                    g_translate_viewport_xy(ui.win_id, mblk->spar[S_BOX].vp_id, 
                                            ui.vp_id,
                                            &ui.x, &ui.y);
                    /*
                     * First point is reference
                     */
                    if (!a0_active) {
                        a0 = ui.y;
                        a0_active = TRUE;
                        break;
                    }
                    /*
                     * Disable motion event handler
                     * All motion events are ignored
                     */
                    enable_motion_events(FALSE);
                    dy = (a0 - ui.y)/(yyy2-yyy1);
                    if (dy == 0) {
                        enable_motion_events(TRUE);
                        break;
                    }
                    a0 = ui.y;
                    yy = dy*100;
                    if (mblk->info->mouse_mode1 == MOUSE_P0) 
                        psnd_phase(mblk, yy, 0);
                    else 
                        psnd_phase(mblk, 0, yy);
                    if (mblk->info->phase_2d) {
                        psnd_plot(mblk,TRUE, mblk->info->dimension_id);
                    }
                    else { 
                        psnd_phaseplot1d(mblk);
                        g_plotall();
                    }
                    /*
                     * Remove pending motion events
                     */
                    while (g_peek_event() & G_POINTERMOTION)
                        g_get_event(&ui);
                    /*
                     * Enable motion event handler again
                     */
                    enable_motion_events(TRUE);
                    if (mblk->info->phase_2d) 
                        psnd_show_2d_phase_data(mblk);
                    }
                    break;
                case MOUSE_3D:
                    /*
                     * Re-calculate coordinates to independent
                     * reference frame, e.g. S_BOX
                     */
                    g_translate_viewport_xy(ui.win_id, mblk->spar[S_BOX].vp_id, 
                                            ui.vp_id,
                                            &ui.x, &ui.y);
                    if (ui.x == xx3d && ui.y == yy3d)
                        break;
                    enable_motion_events(FALSE);
                    psnd_3d_xy(mblk, ui.x - xx3d, ui.y - yy3d);
                    xx3d = ui.x;
                    yy3d = ui.y;
                    psnd_push_waitcursor(mblk);
                    mblk->info->dimension_id = DAT->access[0] - 1;
                    psnd_cp(mblk,CONTOUR_STACK);
                    psnd_pop_waitcursor(mblk);
                    /*
                     * Remove pending motion events
                     */
                    while (g_peek_event() & G_POINTERMOTION)
                        g_get_event(&ui);
                    enable_motion_events(TRUE);
                    break;
            }
            break;

        case G_BUTTON1PRESS:
            g_set_rubbercursor(ui.win_id, ui.vp_id, ui.x, ui.y, 
                               G_RUBBER_NONE);
            switch (mblk->info->mouse_mode1) {
            case MOUSE_ZOOM:
            case MOUSE_ZOOMX:
            case MOUSE_ZOOMY:
            case MOUSE_BUFFER1:
            case MOUSE_BUFFER2:
            case MOUSE_SELECT:
            case MOUSE_NOISE:
            case MOUSE_DISTANCE:
            case MOUSE_PEAKPICK:
            case MOUSE_INTEGRATION:
            case MOUSE_ADDROWCOLUMN:
                if (!zoom) {
                    xxx = ui.x;
                    yyy = ui.y;
                    zoom_vp_id = ui.vp_id;
                    zoom = TRUE;
                    if ((mblk->info->mouse_mode1 == MOUSE_ZOOM)  ||
                        (mblk->info->mouse_mode1 ==  MOUSE_NOISE &&
                            mblk->info->plot_mode == PLOT_2D))
                        g_set_rubbercursor(ui.win_id, ui.vp_id, ui.x,
                                           ui.y, G_RUBBER_DOUBLECROSS);
                    else if (mblk->info->mouse_mode1 == MOUSE_DISTANCE)
                        g_set_rubbercursor(ui.win_id, ui.vp_id, ui.x,
                                           ui.y, G_RUBBER_DOUBLECROSS);
                    else if (mblk->info->mouse_mode1 == MOUSE_PEAKPICK) 
                        g_set_rubbercursor(ui.win_id, ui.vp_id, ui.x,
                                           ui.y, G_RUBBER_DOUBLECROSS);
                    else if (mblk->info->mouse_mode1 == MOUSE_ADDROWCOLUMN) 
                        g_set_rubbercursor(ui.win_id, ui.vp_id, ui.x,
                                           ui.y, G_RUBBER_DOUBLECROSS);
                    else if (mblk->info->mouse_mode1 == MOUSE_INTEGRATION &&
                             mblk->info->plot_mode == PLOT_2D) 
                        g_set_rubbercursor(ui.win_id, ui.vp_id, ui.x,
                                           ui.y, G_RUBBER_BOX);
                    else if (mblk->info->mouse_mode1 == MOUSE_ZOOMY) 
                        g_set_rubbercursor(ui.win_id, ui.vp_id, ui.x,
                                           ui.y, G_RUBBER_YHAIR);
                    else
                        g_set_rubbercursor(ui.win_id, ui.vp_id, ui.x,
                                           ui.y, G_RUBBER_XHAIR);
                }
                break;
            case MOUSE_ROWCOLUMN:
            case MOUSE_DISPLAY:
            case MOUSE_CONTOURLEVEL:
            case MOUSE_P0:
            case MOUSE_P1:
                enable_motion_events(TRUE);
                g_get_world(mblk->spar[S_BOX].vp_id,&xxx1,&yyy1,&xxx2,&yyy2);
                break;
            case MOUSE_MEASURE:
                psnd_position(mblk, ui.x, ui.y);
                break;
            
            case MOUSE_I0:
                psnd_set_phase_position(mblk, PAR, DAT, round(ui.x));
                psnd_printf(mblk, "  i0=%6d\n", PAR->ihold);
                if (mblk->info->phase_2d) 
                    psnd_show_2d_phase_data(mblk);
                break;
            case MOUSE_CALIBRATE:
                if (psnd_calibrate(mblk,ui.x,ui.y)) {
                    psnd_plotaxis(mblk,PAR);
                    g_plotall();
                }
                break;
            case MOUSE_LINEFIT:
                psnd_linefit(mblk, round(ui.x));
                break;
            case MOUSE_WATWA:
                if (DAT->isize <= PAR->td) {
                    PAR->wshift = (ui.x - (float)DAT->isize/2.0 + 1.0);
                }
                else {
                    PAR->wshift = (ui.x - (float)DAT->isize/2.0 + 1.0) 
                              * (float)PAR->td/(float)DAT->isize;
                }
                psnd_refresh_watwa_labels(mblk);
                psnd_printf(mblk, "  watwa shift = %.2f\n", PAR->wshift);
                break;
            case MOUSE_WATERFIT:
                PAR->waterpos = round(ui.x);
                psnd_refresh_waterfit_labels(mblk);
                psnd_printf(mblk, "  waterfit pos = %d\n", PAR->waterpos);
                break;
            case MOUSE_BASELINE:
                PAR->ibwater = round(ui.x);
                psnd_refresh_baseline_labels(mblk);
                psnd_printf(mblk, "  water pos = %d\n", PAR->ibwater);
                break;
            case MOUSE_TRANSITION:
                if (mblk->siminf->dat) {
                    float pickf, fright, fleft;
                    fright  = psnd_chan2hz(DAT->isize, 
                                      DAT->sw,
                                      DAT->isize, 
                                      PAR->xref,
                                      PAR->aref, 
                                      PAR->bref, 
                                      DAT->irc);
                    fleft   = psnd_chan2hz(1, 
                                      DAT->sw, 
                                      DAT->isize, 
                                      PAR->xref,
                                      PAR->aref, 
                                      PAR->bref, 
                                      DAT->irc);
                    pickf   = psnd_chan2hz(ui.x, 
                                      DAT->sw, 
                                      DAT->isize, 
                                      PAR->xref,
                                      PAR->aref, 
                                      PAR->bref, 
                                      DAT->irc);
                                      /*
                    pickf = psnd_calc_pos(ui.x, 
                             PAR->xref, 
                             PAR->aref,
                             PAR->bref, 
                             DAT->irc,
                             TRUE, 
                             AXIS_UNITS_HERTZ,
                             DAT->isize, 
                             DAT->sw,
                             PAR->sfd, 
                             NULL, 
                             NULL);
                             */
                    psnd_assign_transition(mblk, pickf, fleft, fright);
                }
                break;
            case MOUSE_POSITION:
                psnd_where(mblk, ui.x, ui.y);
                break;
            case MOUSE_3D:
                g_translate_viewport_xy(ui.win_id, mblk->spar[S_BOX].vp_id, 
                                            ui.vp_id,
                                            &ui.x, &ui.y);
                g_set_cursortype(mblk->info->win_id, G_CURSOR_DEFAULT);
                xx3d = ui.x;
                yy3d = ui.y;
                g_get_world(mblk->spar[S_BOX].vp_id,&xxx1,&yyy1,&xxx2,&yyy2);
                enable_motion_events(TRUE);
                break;
            }
            break;

        case G_BUTTON1RELEASE:
            switch (mblk->info->mouse_mode1) {
            case MOUSE_MEASURE:
            case MOUSE_ZOOM:
            case MOUSE_ZOOMX:
            case MOUSE_ZOOMY:
            case MOUSE_BUFFER1:
            case MOUSE_BUFFER2:
            case MOUSE_INTEGRATION:
            case MOUSE_DISTANCE:
            case MOUSE_NOISE:
            case MOUSE_PEAKPICK:
            case MOUSE_ADDROWCOLUMN:
               if (zoom) {
                    float xx1,yy1,xx2,yy2,dx,dy;
                    int vp_id ;
                    zoom = FALSE;
                    if (zoom_vp_id != ui.vp_id)
                        break;
                    switch (mblk->info->mouse_mode1) {
                    case MOUSE_ADDROWCOLUMN:
                        if (mblk->info->plot_mode == PLOT_2D) {
                            int i,j,i1,i2,ii,jj, dir,size,verbose;
                            if (ui.vp_id != mblk->spar[S_CONTOUR].vp_id)
                                break;
                            if (mblk->info->contour_row == PLOT_ROW) {
                                dir  = DIRECTION_ROW;
                                size = DAT->pars[0]->nsiz;
                                ii   = round(ui.y);
                                jj   = round(yyy);
                            }
                            else if (mblk->info->contour_row == PLOT_COLUMN){
                                dir  = DIRECTION_COLUMN;
                                size = DAT->pars[1]->nsiz;
                                ii   = round(ui.x);
                                jj   = round(xxx);
                            }
                            else
                                break;
                            i1 = min(ii,jj);
                            i2 = max(ii,jj);
                            memset(DAT->work1, 0, sizeof(float) * size);
                            if (PAR->icmplx)
                                memset(DAT->work2, 0, sizeof(float) * size);
                            for (i=i1;i<=i2;i++) {
                                if (i==i1 || i==i2)
                                    verbose = TRUE;
                                else
                                    verbose = FALSE;
                                mblk->info->dimension_id = 
                                    psnd_rec(mblk, verbose, dir, i, 0);
                                for (j=0;j<size;j++)
                                    DAT->work1[j] += DAT->xreal[j];
                                if (PAR->icmplx)
                                    for (j=0;j<size;j++)
                                        DAT->work2[j] += DAT->ximag[j];
                            }
                            memcpy(DAT->xreal, DAT->work1, sizeof(float) * size);
                            if (PAR->icmplx)
                                memcpy(DAT->ximag, DAT->work2, sizeof(float) * size);
                            psnd_select_contourviewport(mblk);
                            psnd_plot1d_in_2d(mblk,(PAR->icrdir!=DAT->access[0]),i1,FALSE);
                            g_clear_viewport();
                            g_plotall();
                        }
                        break;
                    case MOUSE_BUFFER1:
                        if (psnd_make_visible(mblk, mblk->info->block_id, S_BUFR1, TRUE))
                            psnd_1d_reset_connection(mblk);
                        psnd_cb_addvalue(round(xxx),round(ui.x),DAT);
                        psnd_plot(mblk,TRUE,mblk->info->dimension_id);
                        break;
                    case MOUSE_BUFFER2:
                        if (psnd_make_visible(mblk, mblk->info->block_id, S_BUFR2, TRUE))
                            psnd_1d_reset_connection(mblk);
                        psnd_spline_addvalue(mblk, 0, round(ui.x), ui.y, 
                                    DAT->isize, DAT->xreal, DAT->xbufr2); 
                        psnd_plot(mblk, TRUE, mblk->info->dimension_id);
                        break;
                    case MOUSE_INTEGRATION:
                        psnd_integrate(mblk, xxx, ui.x, yyy, ui.y);
                        /*psnd_plotaxis(mblk,PAR);*/
                        break;
                    case MOUSE_DISTANCE:
                        psnd_distance(mblk, xxx, yyy, ui.x, ui.y);
                        break;
                    case MOUSE_NOISE:
                        psnd_rmsnoise(mblk, xxx, ui.x, yyy, ui.y);
                        break;
                    case MOUSE_PEAKPICK:
                        if (mblk->info->plot_mode != PLOT_2D) {
                            psnd_peakpick(mblk, xxx, yyy, ui.x, ui.y);
                            psnd_set_peakpick_threshold(mblk->cpq_screen, min(yyy,ui.y));
                        }
                        else {
                            psnd_peakpick_area(mblk, xxx, yyy, ui.x, ui.y);
                        }
                        break;
                    case MOUSE_ZOOMX: 
                        vp_id = psnd_get_vp_id(mblk);
                        g_get_world(vp_id,&xx1,&yy1,&xx2,&yy2);
                        if (!psnd_test_zoom(mblk, ui.vp_id, xxx,yy1,ui.x, yy2))
                            break;
                        psnd_update_lastzoom(ui.vp_id, xx1, yy1, xx2, yy2, DAT);
                        psnd_push_zoomundo(ui.vp_id, xxx, yy1, ui.x, yy2,DAT);
                        psnd_scrollbars_reconnect(mblk, ui.vp_id, xxx, yy1, ui.x, yy2, TRUE);
                        psnd_plotaxis(mblk,PAR);
                        break;
                    case MOUSE_ZOOMY: 
                        vp_id = psnd_get_vp_id(mblk);
                        g_get_world(vp_id,&xx1,&yy1,&xx2,&yy2);
                        if (!psnd_test_zoom(mblk, ui.vp_id, xx1,yyy,xx2, ui.y))
                            break;
                        psnd_update_lastzoom(ui.vp_id, xx1, yy1, xx2, yy2, DAT);
                        psnd_push_zoomundo(ui.vp_id, xx1, yyy, xx2, ui.y,DAT);
                        psnd_scrollbars_reconnect(mblk, ui.vp_id, xx1, yyy, xx2, ui.y, TRUE);
                        psnd_plotaxis(mblk,PAR);
                        break;
                    case MOUSE_ZOOM:
                    default:
                        vp_id = psnd_get_vp_id(mblk);
                        if (!psnd_test_zoom(mblk, ui.vp_id, xxx,yyy,ui.x, ui.y))
                            break;
                        g_get_world(vp_id,&xx1,&yy1,&xx2,&yy2);
                        psnd_update_lastzoom(ui.vp_id, xx1, yy1, xx2, yy2, DAT);
                        psnd_push_zoomundo(ui.vp_id, xxx,yyy,ui.x, ui.y, DAT);
                        psnd_scrollbars_reconnect(mblk,ui.vp_id, xxx,yyy,ui.x, ui.y, TRUE);
                        psnd_plotaxis(mblk,PAR);
                        break;
                    }
                    g_set_rubbercursor(ui.win_id, ui.vp_id, ui.x,
                                            ui.y, G_RUBBER_NONE);
                }
                break;
/* new */
            case MOUSE_3D:
                g_set_cursortype(mblk->info->win_id, G_CURSOR_CROSSHAIR);
                enable_motion_events(FALSE);
                break;

            case MOUSE_P0:
            case MOUSE_P1:
            case MOUSE_DISPLAY:
            case MOUSE_CONTOURLEVEL:
                enable_motion_events(FALSE);
                break;
            case MOUSE_ROWCOLUMN:
                enable_motion_events(FALSE);
                if (mblk->info->plot_mode == PLOT_2D && DAT->ityp > 1) {
                    int swap = FALSE, both = FALSE, mark=0;
                    if (ui.vp_id != mblk->spar[S_CONTOUR].vp_id)
                        break;
                    if (mblk->info->contour_row == PLOT_ROW) {
                        mblk->info->dimension_id = 
                            psnd_rec(mblk, TRUE, DIRECTION_ROW, round(ui.y),0);
                    }
                    else if (mblk->info->contour_row == PLOT_PERPENDICULAR) {
                        if (DAT->ityp < 3)
                            break;
                        mblk->info->dimension_id = 
                            psnd_rec(mblk, TRUE, DIRECTION_PERPENDICULAR, 
                                      round(ui.x), round(ui.y));
                        mblk->info->dimension_id = DAT->access[0]-1;
                        swap = 2;
                        mark = DAT->pars[2]->key * DAT->pars[0]->nsiz/DAT->isize;
                    }
                    else if (mblk->info->contour_row == PLOT_COLUMN) {
                        mblk->info->dimension_id = 
                            psnd_rec(mblk, TRUE, DIRECTION_COLUMN, round(ui.x),0);
                        swap = TRUE;
                    }
                    else {
                        mblk->info->dimension_id = 
                            psnd_rec(mblk, TRUE, DIRECTION_COLUMN, round(ui.x),0);
                        memcpy(DAT->work1,DAT->xreal,sizeof(float)*DAT->pars[1]->nsiz);
                        mblk->info->dimension_id = 
                            psnd_rec(mblk, TRUE, DIRECTION_ROW, round(ui.y),0);
                        swap = TRUE;
                        both = TRUE;
                    }
                    psnd_select_contourviewport(mblk);
                    psnd_plot1d_in_2d(mblk,swap,mark,both);
                    g_clear_viewport();
                    g_plotall();
                }
                else {
                    mblk->info->dimension_id = 
                        psnd_rec(mblk, TRUE, DIRECTION_SWITCH, round(ui.x),0);
                    psnd_plot(mblk, TRUE, mblk->info->dimension_id);
                    psnd_adjust_labels_to_new_direction(mblk);
                }
                break;
            case MOUSE_PLANEROWCOLUMN:
                if (mblk->info->plot_mode == PLOT_2D && DAT->ityp > 2) {
                    int keys[3];
                    if (ui.vp_id != mblk->spar[S_CONTOUR].vp_id)
                        break;
                    if (mblk->info->phase_2d)
                        break;
                    
                    if (mblk->info->contour_row == PLOT_ROW) {
                        keys[0] = DAT->access[0];
                        keys[1] = DAT->access[2];
                        keys[2] = DAT->access[1];
                    }
                    else if (mblk->info->contour_row == PLOT_COLUMN){
                        keys[0] = DAT->access[2];
                        keys[1] = DAT->access[1];
                        keys[2] = DAT->access[0];
                    }
                    else
                        break;

                    if (!psnd_direction(mblk, 0, NULL, 3, keys))
                        break;

                    psnd_push_waitcursor(mblk);
                    psnd_plane(mblk, TRUE, PLANE_READ, round(ui.y),
                                    CONTOUR_BLOCK_NEW);
                    psnd_pop_waitcursor(mblk);

                    psnd_nextrec(mblk, TRUE,0,DAT->pars,DAT);
                }
                break;
            default:
                break;
            }
            a0 = 0.0;
            a0_active = FALSE;
            break;
            
        case G_BUTTON3PRESS: 
            g_set_rubbercursor(ui.win_id, ui.vp_id, ui.x, ui.y, 
                               G_RUBBER_NONE);
            {
            float zoomx=1,zoomy=1;
            if (g_scrollbar_get_zoom(mblk->info->win_id, ui.vp_id, &zoomx, &zoomy)) 
                if (zoomx != 1 || zoomy != 1)
              	    g_set_rubbercursor(ui.win_id, ui.vp_id, ui.x, ui.y, 
                            G_RUBBER_PANNER_BORDER);
            }
	    break;

        case G_BUTTON3RELEASE:
            g_set_rubbercursor(ui.win_id, ui.vp_id, ui.x, ui.y, 
                               G_RUBBER_NONE);
            break;

       default:
	        break;
        }
    } while (! main_ok);
    /*
     * Just to be sure!
     */
    psnd_close_file(mblk);
}

void psnd_change_block(MBLOCK *mblk, int id, int interactive)
{
    int vp_id;
    int i, s_ids[]  = {S_REAL, S_IMAG, S_BUFR1, S_BUFR2,
                       S_WINDOW, S_BASELN, -1 };

    if (id < 0 || id >= mblk->info->max_block)
        return;
    if (interactive) {
        /*
         * can not change in contour mode
         */
        if (mblk->info->plot_mode == PLOT_2D)
            return;
        /*
         * swap the visibility between the current block
         * and the requested block
         */
        for (i=0;s_ids[i]!=-1;i++) {
            int res1, res2;
            res1 = psnd_is_visible(mblk, mblk->info->block_id, s_ids[i]);
            res2 = psnd_is_visible(mblk, id, s_ids[i]);
            if (res1 != res2) {
                psnd_make_visible(mblk, id, s_ids[i], res1);
                psnd_make_visible(mblk, mblk->info->block_id, s_ids[i], res2);
            }
        }
        /*
         * if we change blocks, reset some parameters
         */
        if (id != mblk->info->block_id) {
            mblk->cpar_screen->mode = CONTOUR_NEW_FILE;
        }


        mblk->info->block_id     = id;
        mblk->info->dimension_id = DAT->access[0] - 1;
        mblk->spar              = mblk->spar_block[mblk->info->block_id];
        vp_id             = psnd_set_dominant_viewport(mblk);
        g_scrollbar_set_dominant_viewport(mblk->info->win_id, vp_id);
        g_menu_set_toggle(mblk->info->bar_id, PSND_SHOW_GRID, mblk->spar[S_GRID].show);
        g_menu_set_label(mblk->info->bar_id, PSND_SHOW_GRID, "  #  ");
        psnd_1d_reset_connection(mblk);
        psnd_lastzoom(mblk);
        psnd_plot(mblk, TRUE,mblk->info->dimension_id);
        psnd_plotaxis(mblk,PAR);
        psnd_printf(mblk, "  block %d\n", mblk->info->block_id+1);
        /*
         * Set1D/2D toggle buttons
         */
        if (DAT->ityp > 1)
            g_menu_enable_item(mblk->info->bar_id, PSND_CO, TRUE);
        else
            g_menu_enable_item(mblk->info->bar_id, PSND_CO, FALSE);
        psnd_reset_popup_bookmark(mblk, mblk->cpar_screen, DAT);
        psnd_refresh_phase_labels(mblk);
        psnd_refresh_calibrate_labels(mblk);

    }
    else {
        mblk->info->block_id     = id;
        mblk->info->dimension_id = DAT->access[0] - 1;
        mblk->spar              = mblk->spar_block[mblk->info->block_id];
    }
}


void psnd_switch_plotdimension(MBLOCK *mblk)
{
    if (DAT->ityp > 1 && mblk->info->plot_mode == PLOT_1D) {
        g_set_objectstatus(mblk->spar[S_GRID].obj_id, G_SLEEP);
        g_set_objectstatus(mblk->spar[S_AXISX].obj_id, G_SLEEP);
        g_set_objectstatus(mblk->spar[S_AXISY].obj_id, G_SLEEP);
        g_scrollbar_reset(mblk->info->win_id);
        g_scrollbar_disconnect(mblk->info->win_id);
        g_menu_set_toggle(mblk->info->bar_id, PSND_CO, (mblk->info->plot_mode==PLOT_1D));
        enable_2d_buttons(mblk, TRUE, DAT->ityp > 2); 
        psnd_contour_mode(mblk, TRUE, mblk->cpar_screen);
        psnd_set_vp_group(mblk,VP_MAIN);
        psnd_set_vp_id(mblk,mblk->spar[S_CONTOUR].vp_id);
        mblk->info->dimension_id = DAT->access[0] - 1;
        psnd_process_contour_levels(mblk,mblk->cpar_screen);
        psnd_process_color_levels(mblk, mblk->cpar_screen);
        psnd_plane(mblk, FALSE, PLANE_THIS, 0,
                                    CONTOUR_BLOCK);
    }
    else if (mblk->info->plot_mode == PLOT_2D) {
        if (mblk->info->phase_2d)
            psnd_set_phase2d(mblk);
        g_menu_set_toggle(mblk->info->bar_id, PSND_PHASE_2D, FALSE);
        g_set_objectstatus(mblk->spar[S_GRID].obj_id, G_SLEEP);
        g_set_objectstatus(mblk->spar[S_AXISX].obj_id, G_SLEEP);
        g_set_objectstatus(mblk->spar[S_AXISY].obj_id, G_SLEEP);
        psnd_set_cropmode(0, DAT);
        g_scrollbar_reset(mblk->info->win_id);
        g_scrollbar_disconnect(mblk->info->win_id);
        g_menu_set_toggle(mblk->info->bar_id, PSND_CO, (mblk->info->plot_mode==PLOT_1D));
        enable_2d_buttons(mblk, FALSE, FALSE); 
        mblk->spar[S_REALSWAP].show = FALSE;
        g_set_objectstatus(mblk->spar[S_REALSWAP].obj_id, G_SLEEP);
        psnd_make_visible(mblk, mblk->info->block_id, S_REAL, TRUE);
        g_set_objectstatus(mblk->spar[S_REALSWAP].obj_id, G_AWAKE);
        if (mblk->info->plot_mode==PLOT_2D)
            psnd_contour_mode(mblk, FALSE, mblk->cpar_screen);
        psnd_set_vp_group(mblk,VP_MAIN);
        psnd_set_vp_id(mblk,mblk->spar[S_REAL].vp_id);
        psnd_plot(mblk, TRUE, mblk->info->dimension_id);
    }
    psnd_enable_open_buttons(mblk, DAT->finfo[INFILE].fopen);
}



/*
 * Initialize the parameter blocks
 */
void psnd_param_reset(MBLOCK *mblk, int block)
{
    int i,j, startblock, stopblock;
    if (block < 0) {
        startblock = 0;
        stopblock  = mblk->info->max_block;
    }
    else {
        startblock = block;
        stopblock  = block+1;
    }
    for (i=startblock;i<stopblock;i++) 
        for (j=0;j<MAX_DIM;j++) 
            psnd_param_init((mblk->par[i])+j, j+1, 1, FALSE,
                         1.0, 1.0, 0.0);
   
}

void psnd_realloc_buffers(MBLOCK *mblk, int newsize)
{
    int i;
    for (i=0;i<mblk->info->max_block;i++) {
        if (i==0) {
            mblk->dat[i]->work1 = (float*) realloc(mblk->dat[i]->work1,  sizeof(float) * newsize);
            assert(mblk->dat[i]->work1);
            mblk->dat[i]->work2 = (float*) realloc(mblk->dat[i]->work2,  sizeof(float) * newsize);
            assert(mblk->dat[i]->work2);
        }
        else {
            mblk->dat[i]->work1 = mblk->dat[0]->work1;
            mblk->dat[i]->work2 = mblk->dat[0]->work2;
        }
        mblk->dat[i]->xreal	= (float*) realloc(mblk->dat[i]->xreal,  sizeof(float) * newsize);
        assert(mblk->dat[i]->xreal);
        mblk->dat[i]->ximag	= (float*) realloc(mblk->dat[i]->ximag,  sizeof(float) * newsize);
        assert(mblk->dat[i]->ximag);
        mblk->dat[i]->xbufr1	= (float*) realloc(mblk->dat[i]->xbufr1, sizeof(float) * newsize);
        assert(mblk->dat[i]->xbufr1);
        mblk->dat[i]->xbufr2	= (float*) realloc(mblk->dat[i]->xbufr2, sizeof(float) * newsize);
        assert(mblk->dat[i]->xbufr2);
        mblk->dat[i]->window	= (float*) realloc(mblk->dat[i]->window, sizeof(float) * newsize);
        assert(mblk->dat[i]->window);
        mblk->dat[i]->baseln	= (float*) realloc(mblk->dat[i]->baseln, sizeof(float) * newsize);
        assert(mblk->dat[i]->baseln);
    }
    mblk->info->block_size = newsize;
}


int psnd_write_resources(MBLOCK *mblk)
{
    FILE *outfile;
    char *myhome, *filename;

#ifdef _WIN32
    myhome = getenv("PSNDPROGRAMDIR");
    if (myhome == NULL) {
        psnd_printf(mblk, "Can not find environment variable PSNDPROGRAMDIR\n");
        return FALSE;
    }
    filename = psnd_sprintf_temp("%s\\psndrc.txt", myhome);

#else
    myhome = getenv("HOME");
    if (myhome == NULL) {
        psnd_printf(mblk, "Can not find environment variable HOME\n");
        return FALSE;
    }
    filename = psnd_sprintf_temp("%s/.psndrc", myhome);
#endif
    if ((outfile = fopen(filename,"w")) == NULL) {
        psnd_printf(mblk, "Can not open file %s\n", filename);
        return FALSE;
    }
    fprintf(outfile, "# DO NOT EDIT\n");
    fprintf(outfile, "COLOR_SETUP\n");
    psnd_write_colormap(outfile);
    psnd_write_defaultcolors(outfile, mblk);
    fprintf(outfile, "COLOR_SETUP_END\n");
    fclose(outfile);
    return TRUE;
}

int psnd_read_resources(MBLOCK *mblk, int verbose)
{
    char buf[PSND_STRLEN+1], *myhome, *filename;
    int i, ok;
    FILE *infile;

#ifdef _WIN32
    myhome = getenv("PNSDPROGRAMDIR");
    if (myhome == NULL) {
        if (verbose)
            psnd_printf(mblk, "Can not find environment variable PSNDPROGRAMDIR\n");
        return FALSE;
    }
    filename = psnd_sprintf_temp("%s\\psndrc.txt", myhome);
#else
    myhome = getenv("HOME");
    if (myhome == NULL) {
        if (verbose)
            psnd_printf(mblk, "Can not find environment variable HOME\n");
        return FALSE;
    }
    filename = psnd_sprintf_temp("%s/.psndrc", myhome);
#endif
    if ((infile = fopen(filename,"r")) == NULL) {
        if (verbose)
            psnd_printf(mblk, "Can not open file %s\n", filename);
        return FALSE;
    }
    ok = FALSE;
    while (fgets(buf, PSND_STRLEN, infile) != NULL) {
        char *p;
        char sep[] = " \r\n\t";
        
        if (buf[0] == '#')
            continue;
        strupr(buf);
        p= strtok(buf, sep);
        if (p && !ok) {
            if (strcmp(p, "COLOR_SETUP") == 0) {
                ok = TRUE;
            }
        }
        else if (p && ok) {
            if (strcmp(p, "COLOR_SETUP_END") == 0) {
                ok = FALSE;
                break;
            }
            else if (strcmp(p, "PALETTE_SIZE") == 0) {
                p = strtok(NULL, sep);
                if (p) {
                    i = psnd_scan_integer(p);
                    if (i>=2)
                        psnd_read_colormap(infile, i);
                }
            }
            else if (strcmp(p, "BLOCK_COLORS_SETUP") == 0) {
                psnd_read_defaultcolors(infile, mblk);
            }
        }
    }
    fclose(infile);
    return TRUE;
}

static void allocate_blocks(MBLOCK *mblk, int from, int to)
{
    int i,j;
    
    for (i=from;i<to;i++) {
        mblk->dat[i] = (DBLOCK*) calloc(sizeof(DBLOCK),1);
        assert(mblk->dat[i]);
        mblk->dat[i]->access	= (int*) calloc(sizeof(int) , MAX_DIM);
        assert(mblk->dat[i]->access);
        mblk->par[i] = (PBLOCK*) calloc(sizeof(PBLOCK),MAX_DIM);
        assert(mblk->par[i]);
        for (j=0;j<MAX_DIM;j++) {
            mblk->dat[i]->access[j] = j+1;
        }
        mblk->dat[i]->id      = i+1;
        for (j=0;j<2;j++) {
            mblk->dat[i]->finfo[j].name = (char*)  calloc(sizeof(char), PSND_STRLEN);
            mblk->dat[i]->finfo[j].file_type		= 0;
            mblk->dat[i]->finfo[j].fopen 		= FALSE;
            mblk->dat[i]->finfo[j].read_only 		= FALSE;
            if (j==INFILE)
                mblk->dat[i]->finfo[j].ifile		= NINP1+i;
            else
                mblk->dat[i]->finfo[j].ifile		= NOUT1;
            mblk->dat[i]->finfo[j].ndfile 		= NULL;
            mblk->dat[i]->finfo[j].sizeof_float 	= 4;
            mblk->dat[i]->finfo[j].tresh_flag 	= FALSE;
            mblk->dat[i]->finfo[j].tresh_levels[0] 	= 0.0;
            mblk->dat[i]->finfo[j].tresh_levels[1] 	= 0.0;
            mblk->dat[i]->finfo[j].tresh_levels[2] 	= 0.0;
        }
        mblk->dat[i]->sw      = 5000;
        mblk->dat[i]->irc     = 1;
        mblk->dat[i]->isize   = 1024;
        mblk->dat[i]->isspec  = FALSE;
        mblk->dat[i]->xreal	 = (float*) malloc(sizeof(float) * DEFSIZ);
        mblk->dat[i]->ximag	 = (float*) malloc(sizeof(float) * DEFSIZ);
        mblk->dat[i]->xbufr1	 = (float*) malloc(sizeof(float) * DEFSIZ);
        mblk->dat[i]->xbufr2	 = (float*) malloc(sizeof(float) * DEFSIZ);
        mblk->dat[i]->window	 = (float*) malloc(sizeof(float) * DEFSIZ);
        mblk->dat[i]->baseln	 = (float*) malloc(sizeof(float) * DEFSIZ);
        if (i==0) {
            mblk->dat[i]->work1 = (float*) malloc(sizeof(float) * DEFSIZ);
            mblk->dat[i]->work2 = (float*) malloc(sizeof(float) * DEFSIZ);
        }
        else {
            mblk->dat[i]->work1 = mblk->dat[0]->work1;
            mblk->dat[i]->work2 = mblk->dat[0]->work2;
        }
        if (mblk->info->version)
            mblk->dat[i]->xpar    = NULL;
        else
            mblk->dat[i]->xpar    = (float*) malloc(sizeof(float) * MAXPAR);
        for (j=0;j<MAX_DIM;j++) {
            mblk->dat[i]->pars[j]   = (mblk->par[i])+(mblk->dat[i]->access[j]-1);
            mblk->dat[i]->par0[j]   = (mblk->par[i])+j;
            mblk->dat[i]->nsizo[j]  = 1;
            mblk->dat[i]->cmplxo[j] = 0;
        }
        mblk->dat[i]->nt0par  = 50;
        mblk->dat[i]->ntnpar  = 50;
        mblk->dat[i]->iaqdir  = 1;
        mblk->dat[i]->nextdr  = 1;
        mblk->dat[i]->prompt  = NULL;
        psnd_compose_prompt(NULL, mblk->dat[i]);
        psnd_init_undo(mblk->dat[i]);
        psnd_init_plane_undo(mblk->dat[i]);
        psnd_init_zoomundo(mblk->dat[i]);
        mblk->dat[i]->bookmark_queue = NULL;
        mblk->dat[i]->b_count  = 0;
        mblk->dat[i]->b_size   = 0;
        mblk->dat[i]->b_select = 0;
    }
}

static void print_usage(int argc, char *argv[])
{
    printf("\n");
    printf("Usage: %s [-h] [-b <script.au> [<arguments>]] [{-t|-o} <nmrfile> [<access>]]\n", argv[0]);
    printf("\n");
    printf("Options:\n");
    printf("  -h: help. Print this message.\n");
    printf("  -b: background mode. Process the scriptfile <script.au> as a batch job.\n");
    printf("      Everything that follows this command goes as arguments to <script.au>.\n");
    printf("          Example: %s -b prepa_bruker.au -i data/1/ -o noesy.p\n",argv[0]);   
    printf("  <nmrfile>: load this file on opening program.\n");
    printf("      Access directions may follow the file name.\n");
    printf("          Example: %s noesy.p 2\n",argv[0]);   
    printf("\n");
    exit(0);
}

#ifdef _WIN32
void genmain(int argc, char *argv[])
#else
int main(int argc, char *argv[])
#endif
{
    int open_on_init = 0;
    int jchar,i, j, run_in_background,iargc;
    float x0, y0, window_width, window_height;
    float device_width, device_height, border_offset = 10;
    G_EVENT ui;
    MBLOCK mblock, *mblk;
    mblk = &mblock;
    
#ifndef _WIN32
#ifdef SIGHUP  
    signal(SIGHUP,   sighandler);  
#endif 
#ifdef SIGKILL 
    signal(SIGKILL,   sighandler);  
#endif 
#ifndef DEBUG          
#ifdef SIGILL
    signal(SIGILL,   sighandler);                     
#endif
#ifdef SIGBUS
    signal(SIGBUS,   sighandler);                      
#endif
#ifdef SIGSEGV 
    signal(SIGSEGV,  sighandler);                      
#endif
#ifdef SIGSYS 
    signal(SIGSYS,   sighandler);  
#endif          
#endif
#ifdef SIGPIPE
    signal(SIGPIPE,  sighandler);                    
#endif
#ifdef SIGTERM 
    signal(SIGTERM,  sighandler);                      
#endif
#ifdef SIGUSR1 
    signal(SIGUSR1,  sighandler);                      
#endif
#ifdef SIGUSR2
    signal(SIGUSR2,  sighandler);                      
#endif
#endif
    {
        char *label, *p;
        p = strrchr(argv[0], '\\');
        if (p) {
            label = psnd_sprintf_temp("PSNDSCRIPTDIR=%s",argv[0]);
            p = strrchr(label, '\\');
            if (p) {
                p++;
                strcpy(p, "scripts");
                putenv(label);
            }
            label = psnd_sprintf_temp("PSNDPROGRAMDIR=%s",argv[0]);
            p = strrchr(label, '\\');
            if (p) {
                *p = '\0';
                putenv(label);
            }
        }
    }

    mblk->info   = (INFO*) calloc(sizeof(INFO),1);
    assert(mblk->info);

    mblk->vp_store = (VP_STORE*) calloc(sizeof(VP_STORE),VP_MAX);
    assert(mblk->vp_store);
    for (i=0;i<MAXBLK;i++) {
        mblk->spar_block[i] = (SBLOCK*) calloc(sizeof(SBLOCK),1);
        assert(mblk->spar_block[i]);
    }
    /*
     * The NMR-file-type 'version'
     * version == 1 is the only valid version now
     * (version == 0 was the old insp format)
     */
    mblk->info->version = 1;
    mblk->info->win_id = G_ERROR;;
    mblk->info->block_id = 0;
    mblk->info->dimension_id = 0;
    run_in_background = FALSE;
    for (iargc=1;iargc<argc;iargc++) {
        /*
         * run au script in background
         */
        if (strcmp(argv[iargc], "-b") == 0) {
            run_in_background = TRUE;
            break;
        }
        else if (strcmp(argv[iargc], "-h") == 0) {
            print_usage(argc, argv);
        }
        else if (argv[iargc][0] == '-') {
            print_usage(argc, argv);
        }
        else if (open_on_init == 0) {
            open_on_init = iargc;
            break;
        }
    }
    mblk->cpar_screen   = (CBLOCK*) calloc(sizeof(CBLOCK),1);
    assert(mblk->cpar_screen);
    mblk->cpar_hardcopy = (CBLOCK*) calloc(sizeof(CBLOCK),1);
    assert(mblk->cpar_hardcopy);

    mblk->lfdec        = (LFDECONV_TYPE*) calloc(sizeof(LFDECONV_TYPE),1);
    assert(mblk->lfdec);
    mblk->lfdec->init = FALSE;

    mblk->siminf        = (SIMULA_INFO*) calloc(sizeof(SIMULA_INFO),1);
    assert(mblk->siminf);
    mblk->siminf->st    = (SIMTYPE*) calloc(sizeof(SIMTYPE),1);
    assert(mblk->siminf->st);

    mblk->spb           = (STACKPLOT_BLOCK*) 
                                       calloc(sizeof(STACKPLOT_BLOCK),1);
    assert(mblk->spb);
    mblk->spb->xangle_def = -50;
    mblk->spb->yangle_def = 30;

    mblk->int1dinf       = (INT1D_INFO*) calloc(sizeof(INT1D_INFO),1);
    assert(mblk->int1dinf);
    mblk->int1dinf->left    = 0;
    mblk->int1dinf->right   = 0;
    mblk->int1dinf->sum     = 0.0;
    mblk->int1dinf->next    = NULL;

    mblk->popinf       = (POPUP_INFO*) calloc(sizeof(POPUP_INFO),POP_MAX);
    assert(mblk->popinf);
    for (i=0;i<POP_MAX;i++) {
        mblk->popinf[i].cont_id = 0;
        mblk->popinf[i].visible = FALSE;
    }

    mblk->ppinf        = (PEAKPICK_INFO*) calloc(sizeof(PEAKPICK_INFO),1);
    assert(mblk->ppinf);

    mblk->sinf        = (SPLINE_INFO*) calloc(sizeof(SPLINE_INFO),1);
    assert(mblk->sinf);

    mblk->aphase_block= (AUTOPHASE_BLOCK*) calloc(sizeof(AUTOPHASE_BLOCK),1);
    assert(mblk->aphase_block);

    mblk->cpq_screen   = (CONPLOTQ_TYPE*) calloc(sizeof(CONPLOTQ_TYPE),1);
    assert(mblk->cpq_screen);
    mblk->cpq_hardcopy = (CONPLOTQ_TYPE*) calloc(sizeof(CONPLOTQ_TYPE),1);
    assert(mblk->cpq_hardcopy);

    allocate_blocks(mblk, 0, NUMBLK);

    mblk->info->block_size     = DEFSIZ;
    mblk->info->max_block      = NUMBLK;
    mblk->info->global_vp_group_id    = VP_MAIN;
    psnd_param_reset(mblk, -1);
    mblk->info->plot_mode      = PLOT_1D;
    mblk->info->contour_plot_mode      = CPLOT_BLOCK;
    mblk->info->auto_scale     = AUTO_SCALE_ON;
    mblk->info->auto_scale_2d  = AUTO_SCALE_OFF;
    mblk->info->append_plot    = FALSE;
    mblk->info->contour_row    = PLOT_ROW;
    mblk->info->mouse_select_hint = 0;
    mblk->info->mouse_mode1    = MOUSE_MEASURE;
    mblk->info->mouse_mode2    = 0;
    mblk->info->mouse_mode3    = 0;
    mblk->info->phase_2d       = FALSE;
    mblk->info->adjust_display_block		=	0;
    mblk->info->adjust_display_item		=	S_REAL;
    mblk->info->adjust_display_operation	=	Y_MOVE; /* Y_MOVE or Y_SCALE */
    mblk->info->adjust_display_sensitive	=	1;
    mblk->info->foreground	= TRUE;
    mblk->info->verbose	        = TRUE;
    mblk->info->colorwin_id     = 0;
    mblk->info->colorblock_id   = 0;
    mblk->info->colorarray_id   = 0;
    mblk->info->dirmasksize     = 0;
    mblk->info->dirmask         = NULL;
    psnd_init_viewports(mblk);
    mblk->cpar_screen->once     = FALSE;
    mblk->cpar_screen->silent   = FALSE;
    mblk->cpar_hardcopy->once   = FALSE;
    mblk->cpar_hardcopy->silent = TRUE;
    if (run_in_background) {
        char *script = NULL;
        int  ok = FALSE;
            
        iargc++;
        script = psnd_read_script_in_buffer(argv[iargc]);
        if (script == NULL) {
            fprintf(stderr,"Can not open file %s\n", argv[iargc]);
            exit (1);
        }
        mblk->info->foreground = FALSE;
        ok = psnd_run_au_from_commandline(mblk, NULL, 
                        argc - iargc, argv + iargc);
        exit(!ok);
    }
    g_init(1, argv);

    g_set_icon_pixmap(NULL, NULL, psnd_icon32_xpm, 
                      psnd_icon16_xpm);
    psnd_init_colormap();
    g_open_device(G_SCREEN);
    g_get_devicesize(G_SCREEN, &device_width, &device_height);
    window_width  = mblk->info->txmaxx;
    window_height = mblk->info->txmaxy;
    
    if (device_width < 800 || device_height < 600){
        window_width  = 500;
        window_height = 300;
        x0 = border_offset;
        y0 = border_offset;
    }
    else if (device_width < 1000 || device_height < 700){
        window_width  = 600;
        window_height = 350;
    x0 = min(200, device_width - window_width - 2*border_offset);
    x0 = max(x0, border_offset);
        y0 = border_offset;
    }
    else {
    x0 = min(200, device_width - window_width - 2*border_offset);
    x0 = max(x0, border_offset);
    y0 = min(60, device_height - window_height - 2*border_offset);
    y0 = max(y0, border_offset);
    }
#ifdef _WIN32
    g_open_console(8, 8, 600, 200, "OUTPUT");
#endif
    enable_motion_events(FALSE);
    psnd_openwindow(mblk, psnd_get_version_string(), 
          x0, y0, window_width, window_height);
    psnd_init_objects(mblk);
    psnd_init_paper_plot(mblk->cpq_screen);
    psnd_init_contour_par(mblk->spar, mblk->cpar_screen);
    psnd_read_resources(mblk, FALSE);
    mblk->cpar_screen->mode = CONTOUR_NEW_FILE;
    if (open_on_init) {
         ui.event   = G_COMMANDLINE;
         ui.win_id  = mblk->info->win_id;
         if (argc > open_on_init+3)
             ui.command = psnd_sprintf_temp("GE %s %s %s %s", 
                                             argv[open_on_init],
                                             argv[open_on_init+1],
                                             argv[open_on_init+2],
                                             argv[open_on_init+3]);
         else if (argc > open_on_init+2)
             ui.command = psnd_sprintf_temp("GE %s %s %s", 
                                             argv[open_on_init],
                                             argv[open_on_init+1],
                                             argv[open_on_init+2]);
         else if (argc > open_on_init+1)
             ui.command = psnd_sprintf_temp("GE %s %s", 
                                             argv[open_on_init],
                                             argv[open_on_init+1]);
         else
             ui.command = psnd_sprintf_temp("GE %s", 
                                             argv[open_on_init]);
         g_send_event(&ui);
    }
    mainloop(mblk);
#ifndef _WIN32
    exit(0);
#endif

    return 0;

}





























