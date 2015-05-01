/************************************************************************/
/*                               genwprint.c                            */
/*                                                                      */
/*  Platform : Microsoft Windows                                        */
/*  Module   : Genplot printer functions                                */
/*                                                                      */
/*  Albert van Kuik                                                     */
/************************************************************************/


#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
#include <time.h>
#include <stdarg.h>
#include <windows.h>


#ifdef DEBUG
#include "mshell.h"
#endif
#define  G_LOCAL
#include "genplot.h"
#include "g_inter.h"

#ifdef   GENPLOT_
#undef   GENPLOT_
#endif
#define  GENPLOT_(x) printer_##x


#include "genwin.h"

typedef float MATRIX3F[3][3];
typedef float VERTEXF[2];

#ifndef PI
#define PI 3.14159265358979323846
#endif



/*****************************************************/
/* 
 * Set a Transform matrix to an identity matrix. 
 * transform to operate on 
 */
static void identity_f(MATRIX3F mat)
{
    register int i, j;

    for (i = 0; i < 3; i++)
        for (j = 0; j < 3; j++)
            mat[i][j] = (i == j);
}

static void translate_f(MATRIX3F mat, float x, float y)
{
    identity_f(mat);
    mat[0][2] = x;
    mat[1][2] = y;
}

static void rotate_f(MATRIX3F mat, float angle)
{
    identity_f(mat);
    angle *= PI/180.0;
    mat[0][0] = mat[1][1] = cos(angle);
    mat[0][1] = -(mat[1][0] = sin(angle));
}

static void scale_f(MATRIX3F mat, float x, float y)
{
    identity_f(mat);
    mat[0][0] = x;
    mat[1][1] = y;
}


/* 
 * Use matrix multiplication to combine two transforms into one. 
 * Transform transform1, transform2;  the transforms to combine 
 * Transform result;                  the new combined transform
 */
static void concatenate_transforms_f(MATRIX3F mat1, MATRIX3F mat2, 
                            MATRIX3F matresult)
{
   register int i, j, k; /* index variables */
   MATRIX3F temporary;  /* a temporary result */
   /* Using a temporary result allows a single transform to be passed in
    * as both one of the original transforms and as the new result. */

   for (i = 0; i < 3; i++) {
      for (j = 0; j < 3; j++) {
         temporary[i][j] = 0.0;
         for (k = 0; k < 3; k++) {
            temporary[i][j] += mat1[i][k] * mat2[k][j];
         }
      }
   }
   for (i = 0; i < 3; i++)
      for (j = 0; j < 3; j++)
         matresult[i][j] = temporary[i][j];
}


/* 
 * Apply a Transform matrix to a point. 
 * Transform transform;  transform to apply to the point 
 * Point p;              the point to transform 
 * Point tp;             the returned point after transformation 
 */
static void transform_point_f(MATRIX3F mat, float *p, float *tp)
{
    int i, j;
    float homogeneous[3];
    float sum;

    for (i = 0; i < 3; i++) {
        sum = 0.0;
        for (j = 0; j < 2; j++)
            sum += p[j] * mat[i][j];
        homogeneous[i] = sum + mat[i][2];
    }

    for (i = 0; i < 2; i++)
        tp[i] = homogeneous[i] / homogeneous[2];
   
}

static void transform_point(MATRIX3F mat, int *x, int *y)
{
    int i, j;
    float homogeneous[3];
    float sum;

    for (i = 0; i < 3; i++) {
        sum = 0.0;
        sum += (float) (*x) * mat[i][0];
        sum += (float) (*y) * mat[i][1];
        homogeneous[i] = sum + mat[i][2];
    }
    *x = (int) (homogeneous[0] / homogeneous[2]);
    *y = (int) (homogeneous[1] / homogeneous[2]);
}

static MATRIX3F matWorld;


#define IDD_FILE	1234
#define lpstrFile "Bla die da"


static char *mswin_fonts[] =
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
    "Symbol",
    ""
};


//static HINSTANCE _hInst;
static HDC _hdc;
static HPEN __pen, _oldpen;
static HWND __hwnd;

static int mswin_linestyle;
static int mswin_linewidth;
static int mswin_bw;
static int mswin_color;
static int mswin_bkcolor;
static int mswin_printer_fontscaling;
static int mswin_rotate;

static COLORREF rgbColor(int c)  
{
    G_PALETTEENTRY g_palette;

    INTERNAL_get_palette_color(&g_palette, c);
    return GetNearestColor(_hdc,RGB(g_palette.r, 
                    g_palette.g, g_palette.b));
}

static void select_clip(int clip, int clipx1, int clipy1, int clipx2, int clipy2)
{
    if (_hdc)
	return;
    if (clip) {
	HRGN hRgn_clip = CreateRectRgn(clipx1,
						clipy1,
						clipx2,
						clipy2);
	SelectClipRgn(_hdc, hRgn_clip);
       DeleteObject(hRgn_clip);
    }
    else
	SelectClipRgn(_hdc, (HRGN) 0);
}

static void select_pen()
{
    int width;
    
    if (!_hdc)
	return;
    if ( __pen) {
	SelectObject(_hdc, _oldpen);
	DeleteObject( __pen);
    }
    if (mswin_linestyle != PS_SOLID)
	width = 1;
    else
	width = mswin_linewidth;
     __pen = CreatePen(mswin_linestyle,
		      width,
		       rgbColor(mswin_color));
     _oldpen = SelectObject(_hdc, __pen);
}



/**************************************************************/

float GENPLOT_(device_width)       = A4_WIDTH_IN_CM;
float GENPLOT_(device_height)      = A4_HEIGHT_IN_CM;
int   GENPLOT_(dots_per_cm)        = PSHR_UNITS_PER_CM;
int   GENPLOT_(y_is_upsidedown) = TRUE;
int   GENPLOT_(able2clip)       = FALSE; // ok, we can clip but large 16 bit values fold back
int   GENPLOT_(multi_windows)   = FALSE;


static HWND hdlgCancel;

static int okPrint;

void INTERNAL_set_print_job_abort_flag()
{
    okPrint = FALSE;
}

void errhandler(char *label, HWND hwnd)
{
    okPrint = FALSE;
    g_cons_printf("Error:%s\n",label);
}



LRESULT CALLBACK AbortPrintJob(  
        HWND hwndDlg,     /* window handle of dialog box     */  
        UINT message,     /* type of message                 */ 
        WPARAM wParam,    /* message-specific information    */ 
        LPARAM lParam)    /* message-specific information    */ 
{ 
    switch (message) { 
        case WM_INITDIALOG:  /* message: initialize dialog box  */ 
 
            /* Initialize the static text control. */ 
            SetDlgItemText(hwndDlg, IDD_FILE, lpstrFile); 
            return TRUE; 
 
        case WM_COMMAND:     /* message: received a command */ 
 
            /* User pressed "Cancel" button--stop print job. */ 
            MessageBox(hwndDlg, "Incoming", "WM_COMMAND", MB_OK); 
            okPrint = FALSE; 
            return TRUE; 
 
        default: 
            return FALSE;     /* didn't process a message   */ 
 
    } 
    UNREFERENCED_PARAMETER(lParam); 
    UNREFERENCED_PARAMETER(wParam); 
    UNREFERENCED_PARAMETER(message); 
} 


#ifdef meuk

Once the application registers AbortProc with Windows, 
GDI calls the function repeatedly during the printing 
process to determine whether to cancel a job. In the 
current version of the Win32 API, GDI calls this function 
approximately every two seconds until the entire job has 
been spooled. 
If the user chooses to cancel the job, GDI notifies 
the spooler that it should delete the corresponding 
journal file from the print queue and reset the printer
 to its default state. 

Any application written for Windows that supports printing 
should provide an abort procedure and a modeless dialog box 
that allow a user to cancel a print job. The abort procedure 
for the sample application contains a message loop that retrieves 
messages for the modeless dialog box. 

#endif


BOOL CALLBACK AbortProc(HDC hdc, int nCode)  
{  
    MSG msg; 
 
    /* 
     * Retrieve and remove messages from the thread's message 
     * queue. 
     */ 
    while (PeekMessage((LPMSG) &msg, (HWND) NULL, 
            0, 0, PM_REMOVE)) { 
 
        /* Process any messages for the Cancel dialog box. */ 
        if (!IsDialogMessage(hdlgCancel, (LPMSG) &msg)) { 
            TranslateMessage((LPMSG) &msg); 
            DispatchMessage((LPMSG) &msg); 
        } 
    } 
    /* 
     * Return the global okPrint flag (which is set to FALSE 
     * if the user presses the Cancel button). 
     */ 
    return okPrint; 
} 

HWND INTERNAL_popup_cancelbox(HWND parent_hwnd, char *title, char *message);

UINT APIENTRY PagePaintHook(
    HWND hdlg,	// handle to the dialog box window
    UINT uiMsg,	// message identifier
    WPARAM wParam,	// message parameter
    LPARAM lParam 	// message parameter
    )
{
    RECT *rect;
    LONG swp;
 
    switch (uiMsg) {
    case WM_PSD_MARGINRECT:
        rect = (RECT*) lParam;
//        g_cons_printf("%d %d %d %d\n",rect->left,rect->right,rect->top,rect->bottom);
        if (rect->right > rect->bottom){
            swp = rect->right;
            rect->right = rect->bottom;
            rect->bottom = swp;
        }
//        g_cons_printf("%d %d %d %d\n",rect->left,rect->right,rect->top,rect->bottom);
        {
        HDC hDC;
        HPEN hPen, oldpen;
        LOGBRUSH lb;
        lb.lbStyle = BS_SOLID;
        lb.lbColor = RGB(0,0,0);
        lb.lbHatch = 0;
        hPen = ExtCreatePen(PS_COSMETIC, PS_SOLID, &lb, 0, NULL);
        hDC = (HDC) wParam;

        oldpen = SelectObject(hDC, hPen);
        Rectangle(hDC, rect->left+4,rect->top+4, 
                  rect->right-4,rect->bottom-4);

        SelectObject(hDC, oldpen);
	  DeleteObject( hPen);
        }
        return FALSE;
    case WM_PSD_GREEKTEXTRECT:
    case WM_PSD_ENVSTAMPRECT:
    case WM_PSD_YAFULLPAGERECT:
        return TRUE;
    }

    return FALSE;
} 

int GENPLOT_(open) (void) 
{
    int nError;
    SIZE szMetric;
    static DEVMODE  *devmode;
    static DEVNAMES *devnames;
    DEVMODE  *devmode2;
    DEVNAMES *devnames2;
    PRINTDLG pd;
    DOCINFO di;
    PAGESETUPDLG psd;


    __hwnd = WI[GD.win_id].child[C_DRAWAREA].hwnd;

    psd.lStructSize = sizeof(PAGESETUPDLG); 
    psd.hwndOwner = __hwnd; 
    psd.hDevMode = (HANDLE) devmode; 
    psd.hDevNames = (HANDLE) devnames; 

    psd.Flags = PSD_ENABLEPAGEPAINTHOOK; 
/*
    psd.Flags = PSD_RETURNDEFAULT; //PSD_DEFAULTMINMARGINS | PSD_DISABLEPAGEPAINTING; 
    psd.ptPaperSize.x=0; 
    psd.rtMinMargin; 
    psd.rtMargin; 

 */
    psd.hInstance = (HANDLE) NULL; 
    psd.lCustData = 0L; 
    psd.lpfnPageSetupHook = (LPPAGESETUPHOOK) NULL; 
    psd.lpfnPagePaintHook = (LPPAGEPAINTHOOK) PagePaintHook; 
    psd.lpPageSetupTemplateName = (LPCTSTR)  NULL; 
    psd.hPageSetupTemplate = (HANDLE) NULL; 

    if (!PageSetupDlg(&psd))
        return G_ERROR;
    devmode  = (DEVMODE*)  psd.hDevMode;
    devnames = (DEVNAMES*) psd.hDevNames;


    /* Initialize the PRINTDLG members. */  
  
    pd.lStructSize = sizeof(PRINTDLG); 
    pd.hDevMode = (HANDLE) devmode; 
    pd.hDevNames = (HANDLE) devnames; 
    pd.Flags = PD_RETURNDC; 
    pd.hwndOwner = __hwnd; 
    pd.hDC = (HDC) NULL; 
    pd.nFromPage = 1; 
    pd.nToPage = 1; 
    pd.nMinPage = 0; 
    pd.nMaxPage = 0; 
    pd.nCopies = 1; 
    pd.hInstance = (HANDLE) NULL; 
    pd.lCustData = 0L; 
    pd.lpfnPrintHook = (LPPRINTHOOKPROC) NULL; 
    pd.lpfnSetupHook = (LPSETUPHOOKPROC) NULL; 
    pd.lpPrintTemplateName = (LPSTR) NULL; 
    pd.lpSetupTemplateName = (LPSTR)  NULL; 
    pd.hPrintTemplate = (HANDLE) NULL; 
    pd.hSetupTemplate = (HANDLE) NULL; 
 
    /* Display the PRINT dialog box. */ 
    okPrint = FALSE; 
    _hdc = NULL;

    if (!PrintDlg(&pd)) {
        return G_ERROR;
    }
    devmode = (DEVMODE*) pd.hDevMode; 
    devnames = (DEVNAMES*) pd.hDevNames; 

   
    _hdc = pd.hDC;
   /*  
     * Set the flag used by the AbortPrintJob  
     * dialog procedure. 
     */ 
    okPrint = TRUE; 
 
    /* 
     * Register the application's AbortProc 
     * function with GDI. 
     */ 
    SetAbortProc(_hdc, AbortProc); 
 
    /* Display the modeless Cancel dialog box. */ 
 
 
    hdlgCancel = INTERNAL_popup_cancelbox(__hwnd, "Abort Print Job", 
                       "Cancel Print Job");

    /* Disable the application's window. */ 
 
    EnableWindow(__hwnd, FALSE); 

    mswin_bw = FALSE;
    mswin_linestyle = PS_SOLID;
    mswin_linewidth = g_get_linewidth();
    mswin_color   = G_BLACK;
    mswin_bkcolor = G_WHITE;
    mswin_rotate = 0;

    GENPLOT_(device_width)  = (float)GetDeviceCaps(_hdc, HORZSIZE)/10.0; 
    GENPLOT_(device_height) = (float)GetDeviceCaps(_hdc, VERTSIZE)/10.0; 
    GENPLOT_(dots_per_cm) = (int)((float)GetDeviceCaps(_hdc, LOGPIXELSX)/CM_PER_INCH);
    mswin_printer_fontscaling = GetDeviceCaps(_hdc, LOGPIXELSX)/100;
    mswin_printer_fontscaling = max(1, mswin_printer_fontscaling);

    select_pen();

    /*  
     * Initialize the members of a DOCINFO  
     * structure. 
     */ 
 
    di.cbSize = sizeof(DOCINFO); 
    di.lpszDocName = "PrintJob"; 
    di.lpszOutput = (LPTSTR) NULL; 
    di.fwType = 0; 
 
    /* 
     * Begin a print job by calling the StartDoc 
     * function. 
     */ 
 
    nError = StartDoc(_hdc, &di); 
    if (nError == SP_ERROR) { 
        errhandler("StartDoc", __hwnd); 
        goto Error; 
    } 
    return;

Error: 
    /* Enable the application's window. */ 
    EnableWindow(__hwnd, TRUE); 
    /* Remove the AbortPrintJob dialog box. */ 
    DestroyWindow(hdlgCancel); 

    if ( __pen) {
        SelectObject(_hdc, _oldpen);
        DeleteObject( __pen);
         __pen = NULL;
    }
    if (_hdc) {
       /* Delete the printer DC. */ 
        DeleteDC(_hdc);
        _hdc = NULL; 
    }
    return G_OK;
}

void GENPLOT_(open_window) (int id, long x0, long y0, long width, long height,
				   char *title, long flag) 
{   
    int nError, physicalwidth, physicaloffsetx, physicaloffsety;
    MATRIX3F mat;
 
    if (_hdc == NULL)
        return;
    /* 
     * Inform the driver that the application is 
     * about to begin sending data. 
     */ 
    physicalwidth = GetDeviceCaps(_hdc, PHYSICALWIDTH);
    physicaloffsetx = GetDeviceCaps(_hdc, PHYSICALOFFSETX);
    physicaloffsety = GetDeviceCaps(_hdc, PHYSICALOFFSETY);
    if (flag & G_WIN_BLACK_ON_WHITE)
        mswin_bw = TRUE;

    identity_f(matWorld);
    translate_f(mat, x0 - physicaloffsetx, y0 - physicaloffsety);
    concatenate_transforms_f(mat, matWorld, matWorld);
    if (!(flag & G_WIN_PORTRAIT)) {
        rotate_f(mat, 90);
        mswin_rotate = 900;
        concatenate_transforms_f(mat, matWorld, matWorld);
        translate_f(mat, physicalwidth - 2*physicaloffsetx, 0);
        concatenate_transforms_f(mat, matWorld, matWorld);
    }

    nError = StartPage(_hdc); 
    if (nError <= 0) { 
        errhandler("StartPage", __hwnd); 
        goto Error; 
    } 
    return;

Error: 
    /* Enable the application's window. */ 
    EnableWindow(__hwnd, TRUE); 
    /* Remove the AbortPrintJob dialog box. */ 
    DestroyWindow(hdlgCancel); 

    if ( __pen) {
        SelectObject(_hdc, _oldpen);
        DeleteObject( __pen);
         __pen = NULL;
    }
    if (_hdc) {
       /* Delete the printer DC. */ 
        DeleteDC(_hdc);
        _hdc = NULL; 
    }
}


void GENPLOT_(close_window) (int id)
{
    int nError;

    if (_hdc == NULL)
        return;
    
    /* 
     * Determine whether the user has pressed 
     * the Cancel button in the AbortPrintJob 
     * dialog box; if the button has been pressed, 
     * call the AbortDoc function. Otherwise, inform 
     * the spooler that the page is complete. 
     */ 
 
    nError = EndPage(_hdc);  
    if (nError <= 0) { 
        errhandler("EndPage", __hwnd); 
        goto Error; 
    } 
    return;
 
Error: 
    /* Enable the application's window. */ 
    EnableWindow(__hwnd, TRUE); 
    /* Remove the AbortPrintJob dialog box. */ 
    DestroyWindow(hdlgCancel); 

    if ( __pen) {
        SelectObject(_hdc, _oldpen);
        DeleteObject( __pen);
         __pen = NULL;
    }
    if (_hdc) {
       /* Delete the printer DC. */ 
        DeleteDC(_hdc);
        _hdc = NULL; 
    }
}


void GENPLOT_(select_window) (int id) 
{
    id = id;
}

void GENPLOT_(raise_window) (int id) 
{
    id = id;
}

void GENPLOT_(close) (void) 
{
    int nError;

    if (_hdc == NULL)
        return;

    /* Inform the driver that document has ended. */ 
 
    nError = EndDoc(_hdc); 
    if (nError <= 0) 
        errhandler("EndDoc", __hwnd); 

    /* Enable the application's window. */ 
    EnableWindow(__hwnd, TRUE); 
    /* Remove the AbortPrintJob dialog box. */ 
    DestroyWindow(hdlgCancel); 

    if ( __pen) {
        SelectObject(_hdc, _oldpen);
        DeleteObject( __pen);
         __pen = NULL;
    }
    if (_hdc) {
       /* Delete the printer DC. */ 
        DeleteDC(_hdc);
        _hdc = NULL; 
    }
}

void GENPLOT_(moveto) (long x, long y) 
{
    x = x;
    y = y;
}

void GENPLOT_(line) (long x1, long y1, long x2, long y2, int isclipped) 
{
    int xx1,yy1,xx2,yy2;
    if (!okPrint)
        return;
    isclipped = isclipped;
    xx1 = x1;
    yy1 = y1;
    transform_point(matWorld, &xx1, &yy1);
    xx2 = x2;
    yy2 = y2;
    transform_point(matWorld, &xx2, &yy2);

    MoveToEx(_hdc, (int) xx1, (int) yy1, NULL);
    LineTo(_hdc, (int) xx2, (int) yy2);
}


void GENPLOT_(linewidth) (int width) 
{
    if (!okPrint)
        return;
    mswin_linewidth = width;
    select_pen();
}

void GENPLOT_(linestyle) (unsigned pattern) 
{
    if (!okPrint)
        return;
    switch (pattern) {
    case G_SOLID:
	mswin_linestyle = PS_SOLID;
	break;
    case G_SHORT_DASHED:
	mswin_linestyle = PS_DASHDOT;
	break;
    case G_LONG_DASHED:
	mswin_linestyle = PS_DASH;
	break;
    case G_DOTTED:
	mswin_linestyle = PS_DOT;
	break;
    }
    select_pen();
}

void GENPLOT_(label)(char *label) 
{
    static LOGFONT lf;
    HFONT hNFont, oldFont;
    int x1, y1;

    if (!okPrint)
        return;
    if (TEXTROTATE)
	lf.lfEscapement = 900-mswin_rotate;
    else
	lf.lfEscapement = 0-mswin_rotate;
    if (INTERNAL_store_gc.fontscaling == G_RELATIVE_FONTSCALING)
	lf.lfHeight = (int) (INTERNAL_store_gc.charsize 
                           * WINHEIGHT / G_POINTSIZE_SCALING);
    else
	lf.lfHeight = (int) (INTERNAL_store_gc.charsize * G_POINTSIZE 
                            * mswin_printer_fontscaling);
    lf.lfCharSet = DEFAULT_CHARSET;
    lf.lfPitchAndFamily = DEFAULT_PITCH;
    strcpy(lf.lfFaceName, mswin_fonts[INTERNAL_store_gc.font]);
    hNFont = CreateFontIndirect(&lf);
    oldFont = SelectObject(_hdc, hNFont);

    SetTextAlign(_hdc, TA_LEFT | TA_BOTTOM);
    SetTextColor(_hdc,  rgbColor(mswin_color));
    SetBkMode(_hdc, TRANSPARENT);
    x1 = (int) INTERNAL_store_vp.cx;
    y1 = (int) INTERNAL_store_vp.cy;
    transform_point(matWorld, &x1, &y1);
    TextOut(_hdc, (int) x1, (int) y1,
	    label, strlen(label));
    SelectObject(_hdc, oldFont);
    DeleteObject(hNFont);
}

void GENPLOT_(rectangle) (long x1, long y1, long x2, long y2) 
{
    int xx1,yy1,xx2,yy2;
    if (!okPrint)
        return;
    xx1 = x1;
    yy1 = y1;
    transform_point(matWorld, &xx1, &yy1);
    xx2 = x2;
    yy2 = y2;
    transform_point(matWorld, &xx2, &yy2);
    MoveToEx(_hdc, (int) xx1, (int) yy1, NULL);
    LineTo(_hdc, (int) xx1, (int) yy2);
    LineTo(_hdc, (int) xx2, (int) yy2);
    LineTo(_hdc, (int) xx2, (int) yy1);
    LineTo(_hdc, (int) xx1, (int) yy1);
}

void GENPLOT_(drawpoly) (long numpoint, polytype * points) 
{
    int i, j;
    POINT *poly;

    if (!okPrint)
        return;
    if ((poly = (POINT *) malloc((int) numpoint * sizeof(POINT))) == NULL) {
	fprintf(stderr, "Memory allocation error\n");
	exit(1);
    }
    for (i = 0, j = 0; i < numpoint * 2; j++, i += 2) {
        int x1, y1;
        x1 = points[i];
        y1 = points[i + 1];
        transform_point(matWorld, &x1, &y1);
	  poly[j].x = x1;
	  poly[j].y = y1;
    }
    Polyline(_hdc, poly, (int) numpoint);
    free(poly);
}

void GENPLOT_(circle) (long r)
{
    int x1,y1;

    if (!okPrint)
        return;
    x1 = (int) INTERNAL_store_vp.cx;
    y1 = (int) INTERNAL_store_vp.cy;
    transform_point(matWorld, &x1, &y1);
    Arc(_hdc,
	x1 - (int) r,
	y1 - (int) r,
	x1 + (int) r,
	y1 + (int) r,
	x1 - (int) r,
	y1 - (int) r,
	x1 - (int) r,
	y1 - (int) r);
}

void GENPLOT_(fillrectangle) (long x1, long y1, long x2, long y2) 
{
    int xx1,yy1,xx2,yy2;
    RECT r;
    static HBRUSH hBrush, hOBrush;
    static HPEN hPen, hOPen;

    if (!okPrint)
        return;
    xx1 = x1;
    yy1 = y1;
    transform_point(matWorld, &xx1, &yy1);
    xx2 = x2;
    yy2 = y2;
    transform_point(matWorld, &xx2, &yy2);

    hBrush = CreateSolidBrush( rgbColor(mswin_color));
    hOBrush = SelectObject(_hdc, hBrush);
    hPen = CreatePen(PS_SOLID,
		     1,
		      rgbColor(mswin_color));
    hOPen = SelectObject(_hdc, hPen);

    r.left = (int) xx1;
    r.top = (int) yy1;
    r.right = (int) xx2;
    r.bottom = (int) yy2;
    FillRect(_hdc, &r, hBrush);
    SelectObject(_hdc, hOBrush);
    SelectObject(_hdc, hOPen);
    DeleteObject(hBrush);
    DeleteObject(hPen);
}

void GENPLOT_(fillpoly) (long numpoint, polytype * points) 
{
    int i, j;
    POINT *poly;
    static HBRUSH hBrush, hOBrush;
    static HPEN hPen, hOPen;

    if (!okPrint)
        return;
    INTERNAL_check_hdc(&(WI[GD.win_id]),TRUE);
    hBrush = CreateSolidBrush( rgbColor(mswin_color));
    hOBrush = SelectObject(_hdc, hBrush);
    hPen = CreatePen(PS_SOLID,
		     1,
		      rgbColor(mswin_color));
    hOPen = SelectObject(_hdc, hPen);

    if ((poly = (POINT *) malloc((int) numpoint * sizeof(POINT))) == NULL) {
	fprintf(stderr, "Memory allocation error\n");
	exit(1);
    }
    for (i = 0, j = 0; i < numpoint * 2; j++, i += 2) {
        int x1, y1;
        x1 = points[i];
        y1 = points[i + 1];
        transform_point(matWorld, &x1, &y1);
	  poly[j].x = x1;
	  poly[j].y = y1;
    }
    Polygon(_hdc, poly, (int) numpoint);
    free(poly);
    SelectObject(_hdc, hOBrush);
    SelectObject(_hdc, hOPen);
    DeleteObject(hBrush);
    DeleteObject(hPen);
}

void GENPLOT_(fillcircle) (long r) 
{
    static HBRUSH hBrush, hOBrush;
    static HPEN hPen, hOPen;
    int x1, y1;

    if (!okPrint)
        return;
    hBrush = CreateSolidBrush( rgbColor(mswin_color));
    hOBrush = SelectObject(_hdc, hBrush);
    hPen = CreatePen(PS_SOLID,
		     1,
		      rgbColor(mswin_color));
    hOPen = SelectObject(_hdc, hPen);

    x1 = (int) INTERNAL_store_vp.cx;
    y1 = (int) INTERNAL_store_vp.cy;
    transform_point(matWorld, &x1, &y1);
    Ellipse(_hdc,
	    x1 - (int) r,
	    y1 - (int) r,
	    x1 + (int) r,
	    y1 + (int) r);
    SelectObject(_hdc, hOBrush);
    SelectObject(_hdc, hOPen);
    DeleteObject(hBrush);
    DeleteObject(hPen);
}

void GENPLOT_(foreground) (int color) 
{
    if (!okPrint)
        return;
    if (mswin_bw)
        return;
    mswin_color = color;
    select_pen();
}

void GENPLOT_(background) (int color) 
{
    if (!okPrint)
        return;
    if (mswin_bw)
        return;
    mswin_bkcolor = color;
    SetBkColor(_hdc,  rgbColor(color));
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

void GENPLOT_(clipping) (int clip) 
{
    int x1=0, y1=0, x2=0, y2=0;

    if (!okPrint)
        return;
    if (clip) {
        INTERNAL_get_clip(&x1, &y1, &x2, &y2);
    }
    transform_point(matWorld, &x1, &y1);
    transform_point(matWorld, &x2, &y2);
    select_clip(clip, x1,y1,x2,y2);
}

void GENPLOT_(newpage) (void) 
{
    int nError;

    if (_hdc == NULL)
        return;
 
    nError = EndPage(_hdc); 
    if (nError <= 0) { 
        errhandler("EndPage", __hwnd); 
        goto Error; 
    } 
    nError = StartPage(_hdc); 
    if (nError <= 0) { 
        errhandler("StartPage", __hwnd); 
        goto Error;
    } 
    return;
Error: 
    /* Enable the application's window. */ 
    EnableWindow(__hwnd, TRUE); 
    /* Remove the AbortPrintJob dialog box. */ 
    DestroyWindow(hdlgCancel); 

    if ( __pen) {
        SelectObject(_hdc, _oldpen);
        DeleteObject( __pen);
         __pen = NULL;
    }
    if (_hdc) {
       /* Delete the printer DC. */ 
        DeleteDC(_hdc);
        _hdc = NULL; 
    }

}

void GENPLOT_(clearviewport) (void) 
{
    int x1, x2, y1, y2;
    int color;
    int hdc_flag = FALSE;
return;
    if (!okPrint)
        return;
    color = mswin_color;
    mswin_color = mswin_bkcolor;
    GENPLOT_(fillrectangle) (x1, y1, x2 + 1, y2 + 1);
    mswin_color = color;
}



/****************************************************************************
* FONTS
*/
float GENPLOT_(fontheight) (int id) 
{
    float size;

    if (!okPrint)
        return 0;
    if (INTERNAL_store_gc.fontscaling == G_RELATIVE_FONTSCALING)
	size = INTERNAL_store_gc.charsize / G_POINTSIZE_SCALING;
    else
	size = (INTERNAL_store_gc.charsize * G_POINTSIZE) /
	    (float)g_dev[G_SCREEN].win[id].height;
    return size;
}

/****************/



