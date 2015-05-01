/************************************************************************/
/*                               genwpop.c                              */
/*                                                                      */
/*  Platform : Microsoft Windows                                        */
/*  Module   : Genplot Popup Dialog Boxes                               */
/*                                                                      */
/*  Albert van Kuik                                                     */
/************************************************************************/
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <io.h>
#include <windows.h>
#include <commctrl.h>
#include <assert.h>
#ifdef DEBUG
#include "mshell.h"
#endif
#include "genplot.h"
#include "g_inter.h"
#include "genwin.h"

static CONTAINER_INFO *cont_info_from_cont_id(int cont_id);
static void destroy_container(int local_win_id, int cont_id);


#define LEFT        1
#define MIDDLE    2
#define RIGHT        3


#define HORIZONTAL    1
#define VERTICAL    0

#define BUTTON_LEFT        1
#define BUTTON_MIDDLE    2        
#define BUTTON_RIGHT    3
#define BUTTON_MIDDLE_SINGLE    1


#define DEFAULT_X   30
#define DEFAULT_Y   30

#define TAB_HEIGHT 20
#define TAB_BORDER    10

static int popup_follow_cursor=0;

#define MAX_CHECK_BUTTONS    100

/* --- MENU BUTTON LABEL  QUEUE --- */
/*
 Store alle labels and other stings here, and they will be
 freed automatically. Keep BUTTONQMAX > than the number of 
 strings currently in use
*/
#define BUTTONQMAX 500
static char *button_queue[BUTTONQMAX + 1];
static int q_buttonhead, q_buttontail;
/*
int str_len(char *label)
{
    int len = 0;
    if (label==NULL)
        return 0;
    while (label[len] != '\0')
        len++;
    return len;
}
  
*/
  
static void add_label(char *label)
{
    if (label == NULL)
    return;
    q_buttontail++;
    if (q_buttontail > BUTTONQMAX)
    q_buttontail = 0;
    if (q_buttontail == q_buttonhead) {
    (q_buttontail == 0) ? (q_buttontail = BUTTONQMAX) : (q_buttontail--);
    return;
    }
    if (button_queue[q_buttontail] != NULL)
    free(button_queue[q_buttontail]);
    button_queue[q_buttontail] = (char *) malloc(strlen(label) + 1);
    assert(button_queue[q_buttontail]);
    strcpy(button_queue[q_buttontail], label);
}

static int is_label(void)
{
    return (!(q_buttonhead == q_buttontail));
}


static void init_labelqueue(void)
{
    int i;

    for (i = 0; i <= BUTTONQMAX; i++)
    button_queue[i] = NULL;
    q_buttonhead = q_buttontail = 0;
}

static void exit_labelqueue(void)
{
    int i;

    for (i = 0; i <= BUTTONQMAX; i++)
    if (button_queue[i] != NULL) {
        free(button_queue[i]);
        button_queue[i] = NULL;
    }
    q_buttonhead = q_buttontail = 0;
}

static char *get_label(void)
{
    if (!is_label())
    return NULL;
    q_buttonhead++;
    if (q_buttonhead > BUTTONQMAX)
    q_buttonhead = 0;
    return button_queue[q_buttonhead];
}

/******************/


#define G_CHILD_FRAME        200
#define G_PARENT_TAB        201
#define G_CHILD_DIRLIST		202

/*
 * must be positive WORD
*/
#define ID_OK         (0xffff-1)
#define ID_YES        (0xffff-1)
#define ID_NO           (0xffff-2)
#define ID_CANCEL        (0xffff-3)

LRESULT APIENTRY ContainerBoxFunc(HWND, UINT, WPARAM, LPARAM);

//#define    BUTTON              0x80
//#define    EDIT                0x81
//#define    STATIC              0x82
//#define    LISTBOX             0x83
//#define    SCROLLBAR           0x84
//#define    COMBOBOX            0x85
typedef enum {
    NOCONTROL        = 0,
    BUTTON            = 0x0080,
    EDIT        = 0x0081,
    STATIC        = 0x0082,
    LISTBOX            = 0x0083,
    HSCROLL            = 0x0084,
    COMBOBOX        = 0x0085,
    SPIN,
    PROGRES,
    SLIDER,
    HOTKEY,
    LISTCTRL,
    TREECTRL,
    TABCTRL,
    ANIMATE,
    RICHEDIT,
    DATETIMEPICKER,
    MONTHCALENDER,
    IPADRESS,
    COMBOBOXEX
} DLGITEMTEMPLATECONTROLS;


#define CHILDMARGIN_X        4
//#define CHILDMARGIN_Y        4
//#define MARGIN_X        5
//#define MARGIN_Y        10
//#define FRAMEMARGIN___X        10
//#define FRAMEMARGIN___Y        12
//#define FRAMEMARGIN___Y2    8
#define EXTRAMARGIN_X        6
#define EDITMARGIN_Y        4
#define LISTBOXMARGIN_Y     2
#define TOGGLEMARGIN_Y        0
#define OPTIONMARGIN2_Y        8
#define OPTIONMARGIN_Y        2
#define SPINMARGIN_Y		4
#define PUSHBUTTONMARGIN_Y        2
//#define PUSHBUTTONMARGIN_Y        1



#define DEFAULT_TEXT_WIDTH       20

#define STATUS_WAIT        1
#define STATUS_OK        2
#define STATUS_NO             3
#define STATUS_CANCEL         4


#define BORDERX 3
#define BORDERY 4
#define BORDER_TOP 4

#define MARGINX 2
#define MARGINY 2
#define RADIOMARGIN_X	2
#define RADIOMARGIN_Y	4

#define DIALOGMARGIN_Y	8
#define PROMPTMARGIN_Y	4

int get_menufont(HWND hwnd, int *pointsize, int *weight, 
                 int *italic, TCHAR name[])
{
    HDC hdc;
    NONCLIENTMETRICS ncm;
    ncm.cbSize = sizeof(NONCLIENTMETRICS);

    if (SystemParametersInfo(SPI_GETNONCLIENTMETRICS,
          sizeof(NONCLIENTMETRICS) , &ncm, 0)) {
        hdc = GetDC(hwnd);
        *pointsize = -(ncm.lfMessageFont.lfHeight * 72)/GetDeviceCaps(hdc, LOGPIXELSY);
        strcpy(name, ncm.lfMessageFont.lfFaceName);
        *weight = ncm.lfMessageFont.lfWeight;
        *italic = ncm.lfMessageFont.lfItalic;

        ReleaseDC(hwnd, hdc);
        return TRUE;
    }
    return FALSE;
}


#define MAX_SYMBOLS 256
static void GetLabelSize(HWND hWnd, SIZE * size, char *label)
{
    HDC hdc;
    static int init, cy, strbuf[MAX_SYMBOLS];
    int i, ret_val;

    hdc = GetDC(hWnd);
    //ret_val = GetCharWidth32(hdc, (UINT) 0, (UINT) MAX_SYMBOLS - 1, 
     //                    (int FAR*) &strbuf);
    GetTextExtentPoint32(hdc, label, strlen(label), size);
    ReleaseDC(hWnd, hdc);
     //g_cons_printf("pop size x,y = %d,%d\n",size->cx,size->cy);
}

static int LocalLoadTitle(WORD * a, char *s)
{
    int i;

    for (i = 0; s && s[i]; i++)
    a[i] = (WORD) s[i];
    if (!(i % 2))
    a[i++] = (WORD) ' ';
    a[i++] = 0;
    return i;
}

static int LocalLoadString2(WORD * a, char *s)
{
    int i;

    for (i = 0; s && s[i]; i++)
    a[i] = (WORD) s[i];
    if (!(i % 2))
    a[i++] = 0;
    a[i++] = 0;
    return i;
}

static int LocalLoadString(WORD * a, char *s)
{
    int i=0,j=0;

    for (i = 0; s && s[i]; i++){
      a[i] = (WORD) s[i];
        j++;
    }
    a[i++] = 0;
    a[i++] = 0;
    return j;
}


char* GetClassNameByType(int TypeControl)
{
    switch(TypeControl) {
    case BUTTON:
        return _T("BUTTON");
    case EDIT:
        return _T("EDIT");
    case STATIC:
        return _T("STATIC");
    case LISTBOX:
        return _T("LISTBOX");
    case HSCROLL:
        return _T("SCROLLBAR");
    case COMBOBOX:
        return _T("COMBOBOX");
    case SPIN:
        return _T("msctls_updown32");
    case PROGRES:
        return _T("msctls_progress32");
    case SLIDER:
        return _T("msctls_trackbar32");
    case HOTKEY:
        return _T("msctls_hotkey32");
    case LISTCTRL:
        return _T("SysListView32");
    case TREECTRL:
        return _T("SysTreeView32");
    case TABCTRL:
        return _T("SysTabControl32");
    case ANIMATE:
        return _T("SysAnimate32");
    case RICHEDIT:
        return _T("RICHEDIT");
    case DATETIMEPICKER:
        return _T("SysDateTimePick32");
    case MONTHCALENDER:
        return _T("SysMonthCal32");
    case IPADRESS:
        return _T("SysIPAddress32");
    case COMBOBOXEX:
        return _T("ComboBoxEx32");
    }
    return _T("");
}

// typedef struct {  
//     DWORD  helpID; 
//     DWORD  exStyle; 
//     DWORD  style; 
//     short  x; 
//     short  y; 
//     short  cx; 
//     short  cy; 
//     WORD   id; 
//     sz_Or_Ord windowClass; // name or ordinal of a window class
//     sz_Or_Ord title;       // title string or ordinal of a resource
//     WORD   extraCount;     // bytes of following creation data
// } DLGITEMTEMPLATEEX; 

//typedef struct { // dlit  
//    DWORD style; 
//    DWORD dwExtendedStyle; 
//    short x; 
//    short y; 
//    short cx; 
//    short cy; 
//    WORD  id; 
//} DLGITEMTEMPLATE; 

#define titleLen    40 // just some value
#define fontLen    40 // just some value
#define sz_Or_Ord  WORD
typedef struct {   
     WORD   dlgVer; 
     WORD   signature; 
     DWORD  helpID; 
     DWORD  exStyle; 
     DWORD  style; 
     WORD   cDlgItems; 
     short  x; 
     short  y; 
     short  cx; 
     short  cy; 
     sz_Or_Ord menu;         // name or ordinal of a menu resource
     sz_Or_Ord windowClass;  // name or ordinal of a window class
     WCHAR  title[titleLen]; // title string of the dialog box
     short  pointsize;       // only if DS_SETFONT flag is set
     short  weight;          // only if DS_SETFONT flag is set
     short  bItalic;         // only if DS_SETFONT flag is set
     WCHAR  font[fontLen];   // typeface name, if DS_SETFONT is set
} DLGTEMPLATEEX; 
 
//typedef struct { // dltt  
//    DWORD style; 
//    DWORD dwExtendedStyle; 
//    WORD  cdit; 
//    short x; 
//    short y; 
//    short cx; 
//    short cy; 
//} DLGTEMPLATE; 

LPWORD lpwAlign ( LPWORD lpIn)
{
    ULONG ul;

    ul = (ULONG) lpIn;
    ul +=3;
    ul >>=2;
    ul <<=2;
    return (LPWORD) ul;
}

LPWORD lpwAlign4 ( LPWORD lpIn)
{
    ULONG ul = (ULONG) lpIn;
    if (ul & 0x3)
    *lpIn++ = 0;
    return (LPWORD) lpIn;
}

#define EXTENDED

static HWND BuildDialogBox(HINSTANCE hInstance, HWND hwnd, BOXINFO * bi_parent,
       LRESULT APIENTRY(*DialogBoxFunc) (HWND, UINT, WPARAM, LPARAM), 
         int *result)
{
    LPWORD p;
    LPWSTR lpwsz;
    HGLOBAL hgbl;
    LPDLGTEMPLATE pdlgtemplate;
    int nchar, i, extra_strlen;
    DWORD lStyle;
    DWORD lExtendedStyle;
    WORD ClassId, Menu, Class, child_height;
    BOXINFO *bi = bi_parent;
    int setfont = FALSE;
    HWND hwndResult;
    int boxx,boxy;
    WORD  dlgVer    = 0xFFFF;
    WORD  signature = 0x0001;
    DWORD helpID    = 0;
    TCHAR lfFaceName[LF_FACESIZE];
    int pointsize;
    int weight;
    int italic;

    if (get_menufont(hwnd, &pointsize, &weight, 
                 &italic, lfFaceName)) {
        setfont = TRUE;
    }

    bi = bi_parent;
    extra_strlen = 0;
    while ((bi = bi->next) != NULL) 
        if (bi->label)
            extra_strlen += strlen(bi->label);

    bi = bi_parent;
    *result = -1;
    hgbl =  GlobalAlloc(GMEM_ZEROINIT, 
        sizeof(DLGTEMPLATEEX) * 2 * bi->item_count + 2 * extra_strlen);
    if (!hgbl)
        return 0;
    pdlgtemplate = (LPDLGTEMPLATE) GlobalLock(hgbl);
    p = (WORD*) pdlgtemplate;
    lStyle = WS_VISIBLE;
    if (setfont)
    lStyle |= DS_SETFONT;
    if (bi->disabled)
    lStyle |= WS_DISABLED;

    if (bi->type == G_PARENT_TAB)
        lStyle |= WS_CHILD ;
    else
        lStyle |= DS_MODALFRAME | WS_CAPTION | WS_SYSMENU | WS_POPUP ;


//if (bi_parent->modal)
  //  lStyle |= DS_SYSMODAL;

lStyle |= DS_NOFAILCREATE;
    lExtendedStyle = WS_EX_DLGMODALFRAME;
lExtendedStyle = 0;
    Menu  = 0x0000;
    Class = 0x0000;

#ifdef EXTENDED
    *p++ = signature;
    *p++ = dlgVer;
    *p++ = LOWORD(helpID);
    *p++ = HIWORD(helpID);
    *p++ = LOWORD(lExtendedStyle);
    *p++ = HIWORD(lExtendedStyle);
#endif

    *p++ = LOWORD(lStyle);
    *p++ = HIWORD(lStyle);

#ifndef EXTENDED
    *p++ = LOWORD(lExtendedStyle);
    *p++ = HIWORD(lExtendedStyle);
#endif
    *p++ = (WORD) bi->item_count;
    *p++ = boxx = bi->x;
    *p++ = boxy = bi->y;
    *p++ = bi->cx;
    *p++ = bi->cy;
    *p++ = Menu;
    *p++ = Class;

    lpwsz = (LPWSTR) p;
    //nchar = 1+MultiByteToWideChar (CP_ACP, 0, bi->label, -1, lpwsz, 50);
    
     nchar = 1+LocalLoadString(p, bi->label);
     p += nchar;
  
    if (setfont) {
    WORD wPointsize = pointsize;
    WORD Weight  = weight;
    WORD bItalic = italic;
    *p++ = wPointsize;
#ifdef EXTENDED
      *p++ = Weight;
      *p++ = bItalic;
#endif

// nchar = 1+MultiByteToWideChar (CP_ACP, 0, "Courier", -1, (LPWSTR)p, 50);

    nchar = 1+LocalLoadString(p, lfFaceName);
    p += nchar;
    }

    while ((bi = bi->next) != NULL) {
    //ULONG l = (ULONG) p;
    //if (l % 4)
    //    *p++ = 0;

// AlignDialog Item
       p = lpwAlign (p);

lExtendedStyle =0;
       child_height = bi->height;
     switch (bi->type) {

       case G_CHILD_TAB:
         lStyle = WS_CLIPSIBLINGS;//|TCS_MULTILINE;
        ClassId = TABCTRL;
          break;

    case G_CHILD_SCALE:
        lStyle = TBS_AUTOTICKS| WS_TABSTOP;
           if (!bi->horizontal)
               lStyle |= TBS_VERT;
        ClassId = SLIDER;
        break;

    case G_CHILD_SPINBOX:
      case G_CHILD_SPINBOXTEXT:
        lStyle = WS_TABSTOP | UDS_WRAP ;
        ClassId = SPIN;
        break;

    case G_CHILD_LABEL:
        if (bi->pos == MIDDLE)
        lStyle = SS_CENTER;
        else
        lStyle = SS_LEFTNOWORDWRAP;
        ClassId = STATIC;
        break;

    case G_CHILD_SEPARATOR:
        lStyle = SS_BLACKRECT;
        ClassId = STATIC;
           lExtendedStyle = WS_EX_DLGMODALFRAME;
        break;

    case G_CHILD_DIRLIST:
        lStyle = LBS_DISABLENOSCROLL | LBS_NOTIFY | WS_BORDER
        | WS_TABSTOP | LBS_MULTICOLUMN | WS_HSCROLL;
        ClassId = LISTBOX;
        break;

    case G_CHILD_LISTBOX:
        lStyle = LBS_DISABLENOSCROLL | LBS_NOTIFY | WS_VSCROLL
        | WS_BORDER | WS_TABSTOP;
        ClassId = LISTBOX;
        break;

    case G_CHILD_OPTIONMENU:
        lStyle = CBS_AUTOHSCROLL | CBS_DROPDOWNLIST | WS_TABSTOP;
        ClassId = COMBOBOX;
        child_height += OPTIONMARGIN2_Y;
        child_height *= bi->item_count;
        break;

    case G_CHILD_PUSHBUTTON:
        if (bi->pos == 1)
         lStyle = BS_DEFPUSHBUTTON;
        else
        lStyle = BS_PUSHBUTTON;
        lStyle |= WS_TABSTOP;
        ClassId = BUTTON;
        break;

    case G_CHILD_CHECKBOX:
        lStyle = BS_AUTOCHECKBOX;
        if (!bi->pos)
        lStyle |= WS_TABSTOP;
        ClassId = BUTTON;
        break;

    case G_CHILD_RADIOBOX:
        lStyle = BS_AUTORADIOBUTTON;
        if (!bi->pos)
        lStyle |= WS_TABSTOP;
        ClassId = BUTTON;
        break;

    case G_CHILD_FRAME:
        lStyle = BS_GROUPBOX;
        ClassId = BUTTON;
        break;

       case G_CHILD_MULTILINETEXT:
        lStyle =   ES_AUTOHSCROLL| ES_MULTILINE | ES_WANTRETURN | ES_LEFT |
                     WS_BORDER |/* WS_TABSTOP | */ WS_VSCROLL ;
        ClassId = EDIT;
        break;

    case G_CHILD_TEXTBOX:
        lStyle = ES_AUTOHSCROLL | ES_LEFT | WS_BORDER | WS_TABSTOP;
        ClassId = EDIT;
        break;

    default:
        continue;

    }
    if (bi->disabled)
        lStyle |= WS_DISABLED;
    if (bi->group)
        lStyle |= WS_GROUP;
    lStyle |= WS_VISIBLE | WS_CHILD;
#ifdef EXTENDED
      *p++ = LOWORD(helpID);
      *p++ = HIWORD(helpID);
      *p++ = LOWORD(lExtendedStyle);
      *p++ = HIWORD(lExtendedStyle);
      *p++ = LOWORD(lStyle);
      *p++ = HIWORD(lStyle);
#else
    *p++ = LOWORD(lStyle);
    *p++ = HIWORD(lStyle);
    *p++ = LOWORD(lExtendedStyle);
    *p++ = HIWORD(lExtendedStyle);
#endif
    *p++ = bi->x - boxx;
    *p++ = bi->y - boxy;
    *p++ = bi->cx;
    *p++ = child_height;
    *p++ = bi->local_id;

// AlignWindow class
    p = lpwAlign (p);

    if (ClassId <= 0x85) {
      *p++ = (WORD) 0xffff;
        *p++ = (WORD) ClassId;
    }  
    else {
        LPWSTR tmp = (LPWSTR)p;
      //  nchar = 1+MultiByteToWideChar (CP_ACP, 0, GetClassNameByType(ClassId),
      //             -1, tmp, 50);
        nchar = 1+LocalLoadString(p, GetClassNameByType(ClassId));
        p += nchar;
 
    }


    {
        LPWSTR tmp = (LPWSTR)p;
        //nchar = 1+MultiByteToWideChar (CP_ACP, 0, bi->label, -1, tmp, 50);

        nchar = 1+LocalLoadString(p, bi->label);
        p += nchar;

        }
        *p++ = 0x0000;

    }

    *result = -1;
    hwndResult = NULL;
    GlobalUnlock(hgbl);
    if (bi_parent->modal)
        *result = DialogBoxIndirectParam(
                       hInstance,
                       (LPCDLGTEMPLATE) pdlgtemplate,
                       hwnd,
                       (DLGPROC) DialogBoxFunc,
                       (LPARAM) (ULONG) bi_parent);
    else
        hwndResult = CreateDialogIndirectParam(
                        hInstance,
                        (LPCDLGTEMPLATE) pdlgtemplate,
                        hwnd,
                        (DLGPROC) DialogBoxFunc,
                        (LPARAM) (ULONG) bi_parent);

    GlobalFree(hgbl);

    return hwndResult;
}

static VOID WINAPI OnSelChanged(HWND hwndDlg, BOXINFO * bi_parent) 
{ 
    int iSel,dummy;
    CONTAINER_INFO *cont_info;

    cont_info = cont_info_from_cont_id(bi_parent->cont_id);
    if (cont_info == NULL || hwndDlg == NULL || bi_parent == NULL)
         return;                                              
    iSel = TabCtrl_GetCurSel(cont_info->tabinfo->hwndTab); 

    if (cont_info->tabinfo->hwndDisplay != NULL) 
        DestroyWindow(cont_info->tabinfo->hwndDisplay);
    
    cont_info->tabinfo->hwndDisplay = BuildDialogBox(_hInst, hwndDlg, 
                       cont_info->tabinfo->bi_array[iSel],
                       ContainerBoxFunc, &dummy);
} 

static void InitBoxChildren(HWND hdlg, BOXINFO * bi_parent)
{
    BOXINFO *bi;
    int i;

    for (bi = bi_parent; bi != NULL; bi = bi->next) {
    bi->hwnd = GetDlgItem(hdlg, bi->local_id);

       if (bi->type == G_CHILD_TAB) {
           CONTAINER_INFO *cont_info;
           TC_ITEM tie; 
           int ntabs, i;

           cont_info = cont_info_from_cont_id(bi_parent->cont_id);
           if (cont_info == NULL)
               return;
           bi = bi_parent->next;
           cont_info->tabinfo->hwndTab = bi->hwnd;
           ntabs = cont_info->tabinfo->numtabs; 
           for (i=0;i<ntabs;i++) {
               tie.mask = TCIF_TEXT | TCIF_IMAGE; 
               tie.iImage = -1; 
               tie.pszText = cont_info->tabinfo->bi_array[i]->label; 
               TabCtrl_InsertItem(bi->hwnd, i, &tie); 
           }  
           OnSelChanged(hdlg, bi_parent);            
       }
    else if (bi->type == G_CHILD_CHECKBOX || bi->type == G_CHILD_RADIOBOX) {
        if (bi->on)
        SendMessage(bi->hwnd, BM_SETCHECK, 1, 0);
    }
    else if (bi->type == G_CHILD_TEXTBOX) {
        SendMessage(bi->hwnd, EM_SETLIMITTEXT, bi->item_count, 0);
    }
       else if (bi->type == G_CHILD_MULTILINETEXT) {
        ;
    }
    else if (bi->type == G_CHILD_SCALE) {
          // min. & max. positions 
          int totrange = bi->range[1] - bi->range[0];
          SendMessage(bi->hwnd, TBM_SETRANGE,TRUE,                  
            (LPARAM) MAKELONG(0, totrange));  
          SendMessage(bi->hwnd, TBM_SETPAGESIZE, 0, 1);
          // swapped scale
          if (bi->range[3])
              SendMessage(bi->hwnd, TBM_SETPOS, TRUE, 
                 totrange-(bi->on - bi->range[0]));
          else
              SendMessage(bi->hwnd, TBM_SETPOS, TRUE, bi->on - bi->range[0]);
          if (bi->item_count > 0)
              SendMessage(bi->hwnd, TBM_SETTICFREQ , 
                   totrange/bi->item_count, 0); 
    }
    else if (bi->type == G_CHILD_DIRLIST) {
        DWORD cchCurDir=MAX_PATH; 
        LPTSTR lpszCurDir; 

    HANDLE hfile;
    char FileName[MAX_PATH];	// pointer to name of file to search for  
    WIN32_FIND_DATA FindFileData; 	// pointer to returned information 

        lpszCurDir = bi->label;
        GetCurrentDirectory(cchCurDir, lpszCurDir); 

    strcpy(FileName,"*.*");
    hfile = FindFirstFile(FileName,&FindFileData);
    do {
        if (FindFileData.dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY)
           // g_cons_printf("%s\\%s\n",text,FindFileData.cFileName);
        SendMessage(bi->hwnd, LB_ADDSTRING, 0, (LPARAM) FindFileData.cFileName);
    } while (FindNextFile(hfile,&FindFileData));
    FindClose(hfile);
        SendMessage(bi->hwnd, LB_SETCURSEL, bi->on, 0);
             SendMessage(bi->next->hwnd, WM_SETTEXT, 0, (LPARAM) lpszCurDir);

/*

        DlgDirList(hdlg, lpszCurDir, bi->local_id, bi->local_id+1, 
                   DDL_EXCLUSIVE|DDL_DIRECTORY|DDL_DRIVES); 
*/
    }
    else if (bi->type == G_CHILD_LISTBOX) {
        for (i = 0; i < bi->item_count; i++)
        SendMessage(bi->hwnd, LB_ADDSTRING, 0, (LPARAM) bi->data[i]);
        SendMessage(bi->hwnd, LB_SETCURSEL, bi->on, 0);
    }
    else if (bi->type == G_CHILD_OPTIONMENU) {
        for (i = 0; i < bi->item_count; i++)
        SendMessage(bi->hwnd, CB_ADDSTRING, 0, (LPARAM) bi->data[i]);
        SendMessage(bi->hwnd, CB_SETCURSEL, bi->on, 0);
    }
    else if (bi->type == G_CHILD_SPINBOXTEXT) {
          SendMessage(bi->hwnd, UDM_SETRANGE, 
            (WPARAM) TRUE,                   // redraw flag 
            (LPARAM) MAKELONG(bi->item_count-1, 0));  
          SendMessage(bi->hwnd, UDM_SETPOS, 
             (WPARAM) TRUE,                   // redraw flag 
             (LPARAM) bi->on);
       }
    else if (bi->type == G_CHILD_SPINBOX) {
           int imin, imax, ipos, istep;;
           imin = bi->range[0];
           imax = bi->range[1];
           istep = bi->range[3];
           if (istep < 1)
               istep = 1;
           ipos = bi->on;
           SendMessage(bi->hwnd, UDM_SETRANGE, 
              (WPARAM) TRUE,                   // redraw flag 
              (LPARAM) MAKELONG(imax/istep, imin/istep));  
            SendMessage(bi->hwnd, UDM_SETPOS, 
               (WPARAM) TRUE,                   // redraw flag 
               (LPARAM) ipos/istep);
    }
    }
}


static void layout_get(PACKINFO *pinf, BOXINFO *bi_parent)
{
    pinf->level           = bi_parent->level;
    pinf->marginx         = bi_parent->marginx;
    pinf->marginy         = bi_parent->marginy;
    pinf->borderx         = bi_parent->borderx;
    pinf->border_top      = bi_parent->border_top;
    pinf->border_bottom   = bi_parent->border_bottom;
    pinf->direction       = bi_parent->direction;
    pinf->flag            = bi_parent->flag;
}

static void layout_put(PACKINFO *pinf, BOXINFO *bi_parent)
{
    bi_parent->level           = pinf->level;
    bi_parent->marginx         = pinf->marginx;
    bi_parent->marginy         = pinf->marginy;
    bi_parent->borderx         = pinf->borderx;
    bi_parent->border_top      = pinf->border_top;
    bi_parent->border_bottom   = pinf->border_bottom;
    bi_parent->direction       = pinf->direction;
    bi_parent->flag            = pinf->flag;
}


static void layout_push(BOXINFO *bi)
{
    PACKINFO *pinf, *tmp;
    pinf = (PACKINFO*) malloc(sizeof(PACKINFO));
    assert(pinf);
    
    pinf->level           = bi->level;
    pinf->marginx         = bi->marginx;
    pinf->marginy         = bi->marginy;
    pinf->borderx         = bi->borderx;
    pinf->border_top      = bi->border_top;
    pinf->border_bottom   = bi->border_bottom;
    pinf->direction       = bi->direction;
    pinf->flag            = bi->flag;
    pinf->next            = NULL;
    if (bi->pinf_stack == NULL) {
        bi->pinf_stack = pinf;
        return;
    }
    tmp = bi->pinf_stack;
    while (tmp->next)
        tmp = tmp->next;
    tmp->next = pinf;
}

static void layout_pop(BOXINFO *bi)
{
    PACKINFO *pinf, *tmp;
    
    tmp = pinf = bi->pinf_stack;
    if (pinf == NULL)
        return;
    while (pinf->next) {
        tmp  = pinf;
        pinf = pinf->next;
    }
    bi->level           = pinf->level;
    bi->marginx         = pinf->marginx;
    bi->marginy         = pinf->marginy;
    bi->borderx         = pinf->borderx;
    bi->border_top      = pinf->border_top;
    bi->border_bottom   = pinf->border_bottom;
    bi->direction       = pinf->direction;
    bi->flag            = pinf->flag;
    if (tmp == pinf)
       bi->pinf_stack = NULL;
    else
        tmp->next = NULL;
    free(pinf);
        
}

static void CalcSize(BOXINFO * bi)
{
    BOXINFO *bi_temp;
    WORD d2p_x;
    WORD d2p_y;
    DWORD BaseUnits;
    SIZE size;

    BaseUnits = GetDialogBaseUnits();
    d2p_x = LOWORD(BaseUnits) / 4;
    d2p_y = HIWORD(BaseUnits) / 8;
    if (bi->items_visible == 0)
    bi->items_visible = bi->item_count;

    switch (bi->type) {

    case G_CHILD_DIRLIST:{
        int i, len;
        GetLabelSize(bi->parent_hwnd, &size, "x");
           len = size.cx;
          bi->pinf.height = size.cy / d2p_y * bi->items_visible
                                 + LISTBOXMARGIN_Y;
          bi->pinf.width  = len / d2p_x * bi->item_count + EXTRAMARGIN_X;
           }
        break;

    case G_CHILD_LISTBOX:{
        int i, len;
        GetLabelSize(bi->parent_hwnd, &size, "XXXXX");
           len = size.cx;
           for (i = 0; i < bi->item_count; i++) {
               GetLabelSize(bi->parent_hwnd, &size, bi->data[i]);
               len = max(len, size.cx);
          }
          bi->pinf.height = size.cy / d2p_y * bi->items_visible
                                 + LISTBOXMARGIN_Y;
          bi->pinf.width  = len / d2p_x + EXTRAMARGIN_X;
           }
        break;

    case G_CHILD_OPTIONMENU:{
        int i, len=0, extralen=0;
           //GetLabelSize(bi->parent_hwnd, &size, "XXXXX");
           //len = size.cx;
           GetLabelSize(bi->parent_hwnd, &size, "XX");
           extralen = size.cx;
        for (i = 0/*, len = 0*/; i < bi->item_count; i++) {
        GetLabelSize(bi->parent_hwnd, &size, bi->data[i]);
        len = max(len, size.cx);
        }
        len += extralen;
       bi->cx = len / d2p_x;
        bi->height = size.cy / d2p_y * (bi->item_count + 2);
        bi->cy = size.cy / d2p_y;

            bi->pinf.height = size.cy / d2p_y + OPTIONMARGIN_Y;
            bi->pinf.width  = len / d2p_x + 2 * EXTRAMARGIN_X;
    }
    break;

       

    case G_CHILD_SPINBOX:
    case G_CHILD_SPINBOXTEXT:{
        GetLabelSize(bi->parent_hwnd, &size, "X");
        bi->cx = size.cx / d2p_x;
        bi->height = size.cy / d2p_y * (bi->item_count + 2);
        bi->cy = size.cy / d2p_y;

            bi->pinf.height = size.cy / d2p_y  + SPINMARGIN_Y;
            bi->pinf.width  = size.cx / d2p_x  + EXTRAMARGIN_X;
    }
    break;

    case G_CHILD_MULTILINETEXT:
        bi->items_visible = max(bi->items_visible,DEFAULT_TEXT_WIDTH);
        GetLabelSize(bi->parent_hwnd, &size, "X");
        bi->pinf.height = size.cy / d2p_y * bi->item_count + EDITMARGIN_Y;
        bi->pinf.width  = size.cx / d2p_x * bi->items_visible;
    break;

    case G_CHILD_TEXTBOX: {
        int items_visible;
        if (bi->items_visible == 0)
            bi->items_visible = DEFAULT_TEXT_WIDTH;
        items_visible = max(4, bi->items_visible);
        GetLabelSize(bi->parent_hwnd, &size, "X");
        bi->pinf.height = size.cy / d2p_y + EDITMARGIN_Y;
        bi->pinf.width  = size.cx / d2p_x * items_visible;
      }
    break;

    case G_CHILD_SCALE:
      GetLabelSize(bi->parent_hwnd, &size, "x");
        if (bi->horizontal) {
            bi->pinf.height = 3 * size.cy / d2p_y;
            bi->pinf.width  = bi->items_visible * 3 * size.cx / d2p_x;
        }
        else {
            bi->pinf.width = 3 * size.cy / d2p_y;
            bi->pinf.height  = bi->items_visible * 3 * size.cx / d2p_x;
        }   
     break;

    case G_CHILD_SEPARATOR:
        bi->pinf.height = 1;
        bi->pinf.width  = 1;
        bi->pinf.flag   = FILL_X;
    break;
/*
    case G_CHILD_FRAME:
        bi->pinf.height = 1;
        bi->pinf.width  = 1;
    break;
*/
    case G_CHILD_RADIOBOX:
    case G_CHILD_CHECKBOX:
    GetLabelSize(bi->parent_hwnd, &size, bi->label);
        bi->pinf.height = size.cy / d2p_y + TOGGLEMARGIN_Y;
        bi->pinf.width  = size.cx / d2p_x + 2 * CHILDMARGIN_X +EXTRAMARGIN_X;
        break;

    case G_CHILD_PUSHBUTTON:
        GetLabelSize(bi->parent_hwnd, &size, bi->label);
        bi->pinf.height = size.cy / d2p_y + 2 * PUSHBUTTONMARGIN_Y;
        bi->pinf.width  = size.cx / d2p_x + 2 * CHILDMARGIN_X;
//        bi->pinf.flag=ALLIGN_X_CENTER;

    break;

    case G_CHILD_LABEL: {
      int len = strlen(bi->label);
//    GetLabelSize(bi->parent_hwnd, &size, bi->label);
    GetLabelSize(bi->parent_hwnd, &size, "X");
//        bi->pinf.width = size.cx / d2p_x + CHILDMARGIN_X;
        bi->pinf.width = len * size.cx / d2p_x + CHILDMARGIN_X;
        bi->pinf.height  = size.cy / d2p_y;
      }
    break;
    }

}

static int boxinfo_local_id = 10;

static BOXINFO *BoxinfoNew(BOXINFO * bi_parent, HWND parent_hwnd)
{
    BOXINFO *bi;

    bi = (BOXINFO *) calloc(sizeof(BOXINFO),1);
    assert(bi);
    bi->hwnd = (HWND) 0;
    bi->parent_hwnd = parent_hwnd;
    bi->type = 0;
    bi->x = 0;
    bi->y = 0;
    bi->cx = 0;
    bi->cy = 0;
    bi->id = -1;
    bi->local_id = boxinfo_local_id++;
    bi->pos = 0;
    bi->label = NULL;
    bi->disabled = 0;
    bi->group = FALSE;
    bi->on = FALSE;
    bi->item_count = 0;
    bi->items_visible = 0;
    bi->height = 0;
    bi->modal = FALSE;
    bi->data = NULL;
    bi->ci = NULL;
    bi->func = NULL;
    bi->userdata = NULL;
    bi->cont_id = 0;
    bi->win_id = -1;
    bi->next = NULL;
    bi->prev = NULL;
    bi->parent = NULL;
    bi->pinf_stack = NULL;

    if (bi_parent) {
    BOXINFO *bi_temp = bi_parent;
       bi->parent = bi_parent;
       bi->cont_id = bi_parent->cont_id;
       bi->win_id  = bi_parent->win_id;
        while (bi_temp->next)
        bi_temp = bi_temp->next;
    bi_temp->next = bi;
    bi->prev = bi_temp;
    bi_parent->item_count++;
        bi_temp->pinf.next = &(bi->pinf);
        layout_get(&(bi->pinf), bi->parent); 
   }
    else {
        bi->parent = bi;
        bi->pinf.width        = 10;
        bi->pinf.height       = 10;
        bi->marginx           = MARGINX;
        bi->marginy           = MARGINY;
        bi->borderx           = BORDERX;
        bi->border_top        = BORDERY;
        bi->border_bottom     = BORDERY;
        bi->direction         = PACK_VERTICAL;
        bi->level             = 0;
        bi->flag              = 0;
        layout_get(&(bi->pinf), bi);
    }
    
    return bi;
}

static BOXINFO *BoxinfoWhich(BOXINFO * bi_parent, int id)
{
    BOXINFO *bi;

    bi = bi_parent;
    while (bi) {
    if (bi->local_id == id)
        return bi;
    bi = bi->next;
    }
    return NULL;
}

static void BoxinfoLabel(BOXINFO * bi, char *label)
{
    if (!label) {
    bi->label = NULL;
    return;
    }
    bi->label = (char *) malloc(strlen(label) + 1);
    assert(bi->label);
    strcpy(bi->label, label);
}


static void BoxinfoFree(BOXINFO * bi)
{
    BOXINFO *bi_temp1, *bi_temp2;

    bi_temp1 = bi;
    while (bi_temp1) {
    bi_temp2 = bi_temp1->next;
    if (bi_temp1->label)
        free(bi_temp1->label);
    free(bi_temp1);
    bi_temp1 = bi_temp2;
    }
}

static BOXINFO *OpenBox(HWND parent, int x, int y, int modal, char *title)
{
    BOXINFO *bi;

    if (popup_follow_cursor) {
    POINT point;
    DWORD BaseUnits;

    GetCursorPos(&point);
    ScreenToClient(parent, &point);
    BaseUnits = GetDialogBaseUnits();
    x = (point.x * 4)/LOWORD(BaseUnits);
    y = (point.y * 8)/HIWORD(BaseUnits);
    }
    bi = BoxinfoNew(NULL, parent);
    bi->x = x;
    bi->y = y;
    bi->modal = modal;
    BoxinfoLabel(bi, title);
    return bi;
}

static BOXINFO *AddPanel(BOXINFO *bi_parent, HWND parent_hwnd,
                         int direction)
{
    BOXINFO *bi;

    layout_push(bi_parent);
    bi = BoxinfoNew(bi_parent, parent_hwnd);
    bi->type = G_CHILD_PANEL;

    bi->pinf.width           = 1;
    bi->pinf.height          = 1;
    bi_parent->flag         |= FILL_X;
    bi_parent->borderx       = 0;
    bi_parent->border_top    = 0;
    bi_parent->border_bottom = 0;
    bi_parent->level        += 1;
    bi_parent->direction     = direction;
    return bi;
}

static BOXINFO *AddFrame(BOXINFO *bi_parent, HWND parent_hwnd, int id,
             char *title, int disabled, int direction)
{
    BOXINFO *bi;

    layout_push(bi_parent);
    bi = BoxinfoNew(bi_parent, parent_hwnd);
    bi->type = G_CHILD_FRAME;
    bi->id = id;
    bi->disabled = disabled;
    BoxinfoLabel(bi, title);
    CalcSize(bi);

    bi_parent->borderx       = BORDERX;
    bi_parent->border_top    = BORDERY+BORDER_TOP;
    bi_parent->border_bottom = BORDERY;
    bi_parent->level        += 1;
    bi_parent->direction     = direction;
    bi_parent->flag         |= FILL_X;
    return bi;
}

static char *make_spin_label(int pos, int range[], char *label)
{
    int first, last, decipoint, stepsize;

    if (range == NULL || label == NULL)
        return NULL;
    first = range[0];
    last  = range[1];
    decipoint = range[2];
    stepsize = range[3];
    if (stepsize < 1)
        stepsize = 1;
    if (last < first) {
        first = range[1];
        last  = range[0];
    }
    pos *= stepsize;
    if (pos < first)
        pos = first;
    if (pos > last)
        pos = last;
    pos -= first;
    pos /= stepsize;
    pos *= stepsize;
    pos += first;
    if (decipoint > 0) {
        int i, len;
        // just in case
        if (decipoint > 10)
            decipoint = 10;
        sprintf(label,"%d", pos);
        len = strlen(label);
        if (len <= decipoint) {
            i = 0;
            label[i++] = '0';
            label[i++] = '.';
            while (i <= decipoint - len + 1){
                label[i++] = '0';
            }
            sprintf(label+i,"%d", pos);
        }
        else {
            label[len+1] = '\0';
            if (decipoint > len)
                decipoint = len;
            for (i=len;i>0 && decipoint>0;i--){
                label[i] = label[i-1];
                decipoint--;
            }
            label[i] = '.';
        }
    }
    else
        sprintf(label,"%d", pos);
    return label;
}


static BOXINFO *AddSpinbox(BOXINFO *bi_parent, HWND parent_hwnd, 
                     int istext, char *label,
             int id, int item_count, int select[],
                     int items_visible, int item, char *data[], 
                     int hasframe, char *title, int disabled)
{
    BOXINFO *bi;
    int range[4];
    
    if (hasframe) 
      AddFrame(bi_parent, parent_hwnd, 
                            id, title, disabled, PACK_HORIZONTAL);
    else {
      AddPanel(bi_parent, parent_hwnd, 
                            PACK_HORIZONTAL);
    }
    if (label) {
       bi_parent->flag |= ALIGN_Y_CENTER; // GRID_Y;
    bi = BoxinfoNew(bi_parent, parent_hwnd);
    bi->type = G_CHILD_LABEL;
    bi->disabled = disabled;
    bi->group = TRUE;
    BoxinfoLabel(bi, label);
    CalcSize(bi);
     }
    AddPanel(bi_parent, parent_hwnd,PACK_HORIZONTAL);
    bi_parent->marginx = 0;
    bi_parent->marginy = 0;
    bi_parent->flag |= ALIGN_Y_CENTER; // GRID_Y;

    bi = BoxinfoNew(bi_parent, parent_hwnd);
    bi->disabled = disabled;
    bi->type = G_CHILD_TEXTBOX;
    bi->item_count = item_count;
    bi->items_visible = items_visible;
    if (items_visible == 0)
        bi->item_count    = DEFAULT_TEXT_WIDTH;
    else
        bi->item_count    = items_visible;
    bi->group = TRUE;
    if (istext) {
        int on = item;
        if (on < 0) 
            on = 0;
        else if (on >= item_count)
            on = item_count-1;
        BoxinfoLabel(bi, data[on]);
        item=on;
        range[0] = 0;
        range[1] = item_count;
    }
    else {
        char spinlabel[20];
        int first,last, stepsize;
        range[0] = select[0];
        range[1] = select[1];
        range[2] = select[2];
        range[3] = select[3];
 
        first = range[0];
        last  = range[1];
        stepsize = range[3];
        if (stepsize < 1)
            stepsize = 1;
        if (last < first) {
            first = range[1];
            last  = range[0];
        }
        range[0] = first;
        range[1] = last;
        range[3] = stepsize;
        spinlabel[0] = '\0';
        make_spin_label(item/stepsize, select, spinlabel);
        bi->item_count = strlen(spinlabel) + 1;
        BoxinfoLabel(bi, spinlabel);
    }
    CalcSize(bi);

    bi = BoxinfoNew(bi_parent, parent_hwnd);
    if (istext) {
        bi->type = G_CHILD_SPINBOXTEXT;
        bi->data = data;
    }
    else {
        bi->type = G_CHILD_SPINBOX;
        bi->range[0] = range[0];
        bi->range[1] = range[1];
        bi->range[2] = range[2];
        bi->range[3] = range[3];
    }
    bi->disabled = disabled;
    bi->group = TRUE;
    bi->id = id;
    bi->items_visible = items_visible;
    bi->item_count = item_count;
    if (item < range[0])
        item = range[0];
    else if (item > range[1])
        item = range[1];
    bi->on = item;
    BoxinfoLabel(bi, label);
    CalcSize(bi);

    layout_pop(bi_parent);
    layout_pop(bi_parent);
    return bi;
}


static BOXINFO *AddScale(BOXINFO *bi_parent, HWND parent_hwnd, char *label,
             int id, int item_count, int select[],
                     int items_visible, int item, char *data[], 
                     int hasframe, char *title, int hor, 
                     int disabled)
{
    BOXINFO *bi;
    int  new_direction;

    /* When direction is horizontal, packing is vertical */
    new_direction = PACK_VERTICAL;
    if (hasframe) 
      AddFrame(bi_parent, parent_hwnd, 
                            id, title, disabled, new_direction);
    else{
      AddPanel(bi_parent, parent_hwnd, 
                            new_direction);
    }
    bi_parent->marginx = 0;
    bi_parent->marginy = 0;
    if (label) {                         
    bi = BoxinfoNew(bi_parent, parent_hwnd);
    bi->type = G_CHILD_LABEL;
    bi->disabled = disabled;
    bi->group = TRUE;
    BoxinfoLabel(bi, label);
    CalcSize(bi);
    }
    bi = BoxinfoNew(bi_parent, parent_hwnd);
    bi->type = G_CHILD_SCALE;
    bi->disabled = disabled;
    bi->group = TRUE;
    bi->id = id;
    bi->range[2] = select[2];
    if (select[0] > select[1]){
        bi->range[0] = select[1];
        bi->range[1] = select[0];
        bi->range[3] = 1;
     }
    else {
        bi->range[0] = select[0];
        bi->range[1] = select[1];
        bi->range[3] = 0;
    }
    /*
     * If vertical, swap scale
     */
    if (hor == FALSE) {
        if (bi->range[3]) 
           bi->range[3] = 0;
        else
           bi->range[3] = 1; 
    }
    if (item < bi->range[0])
        item = bi->range[0];
    if (item > bi->range[1])
        item = bi->range[1];
    bi->on       = item;
    bi->items_visible = items_visible;
    bi->item_count = item_count;
    bi->data = data;
    bi->horizontal = hor;
    BoxinfoLabel(bi, label);
    CalcSize(bi);
    layout_pop(bi_parent);
    return bi;
}




static BOXINFO *AddLabel(BOXINFO *bi_parent, HWND parent_hwnd, char *label,
             int id, int hasframe, char *title,
             int pos, int disabled)
{
    BOXINFO *bi;

    if (hasframe) 
    AddFrame(bi_parent, parent_hwnd, 
                            id, title, disabled, PACK_HORIZONTAL);
    bi = BoxinfoNew(bi_parent, parent_hwnd);
    bi->type = G_CHILD_LABEL;
    bi->disabled = disabled;
    bi->group = TRUE;
    bi->id = id;
    bi->pos = pos;
    BoxinfoLabel(bi, label);
    CalcSize(bi);

    if (hasframe)
        layout_pop(bi_parent);
    return bi;
}


static BOXINFO *AddSeparator(BOXINFO * bi_parent, HWND parent_hwnd)
{
    BOXINFO *bi;

    bi = BoxinfoNew(bi_parent, parent_hwnd);
    bi->type = G_CHILD_SEPARATOR;
    CalcSize(bi);
    return bi;
}


static BOXINFO *AddPushButton(BOXINFO * bi_parent, HWND parent_hwnd, char *label,
                  int id, int hasframe, char *title,
                  int pos, int disabled)
{
    BOXINFO *bi, *bi_frame = NULL;

    if (hasframe)
    bi_frame = AddFrame(bi_parent, parent_hwnd, 
                            id, title, disabled, PACK_HORIZONTAL);
    bi = BoxinfoNew(bi_parent, parent_hwnd);
    bi->type = G_CHILD_PUSHBUTTON;
    bi->disabled = disabled;
    bi->id = id;
    bi->group = TRUE;
    bi->pos = pos;
    BoxinfoLabel(bi, label);
    CalcSize(bi);
    if (hasframe)
        layout_pop(bi_parent);
    return bi;
}


/*
 multi

    item_count - The number of rows. 
    items_visible - The number of columns. 
*/

static BOXINFO *AddEdit(BOXINFO * bi_parent, HWND parent_hwnd, int ismulti,
                        char *label, int id, int item_count, int items_visible,
            int hasframe, char *title, int hor, int disabled)
{
    BOXINFO *bi, *bi_frame = NULL;
    int do_pop = FALSE;

    if (hor && title != NULL){
        do_pop = TRUE;
        if (ismulti)
            AddPanel(bi_parent, parent_hwnd, 
                            PACK_VERTICAL);
        else
            AddPanel(bi_parent, parent_hwnd, 
                            PACK_HORIZONTAL);
       if (title) {                         
        bi = BoxinfoNew(bi_parent, parent_hwnd);
        bi->type = G_CHILD_LABEL;
        bi->disabled = disabled;
        bi->group = TRUE;
        BoxinfoLabel(bi, title);
        CalcSize(bi);
           if (!ismulti)
               bi->pinf.flag = ALIGN_Y_CENTER;// GRID_Y;
        }
    }
    else if (hasframe) {
        do_pop = TRUE;
     bi_frame = AddFrame(bi_parent, parent_hwnd, 
                            id, title, disabled, PACK_HORIZONTAL);
    }
    bi = BoxinfoNew(bi_parent, parent_hwnd);
    bi->disabled = disabled;
    if (ismulti)
        bi->type = G_CHILD_MULTILINETEXT;
    else
        bi->type = G_CHILD_TEXTBOX;
    bi->id = id;
    bi->item_count = item_count;
    bi->items_visible = items_visible;
    bi->group = TRUE;
    BoxinfoLabel(bi, label);
    CalcSize(bi);
    if (do_pop)
        layout_pop(bi_parent);
    return bi;
}


static BOXINFO *AddDirList(BOXINFO * bi_parent, HWND parent_hwnd, 
            int id, int item_count, int items_visible,
            int hasframe, char *title, int disabled)
{
    BOXINFO *bi, *bi_frame = NULL;

    if (hasframe)
        bi_frame = AddFrame(bi_parent, parent_hwnd, 
                            id, title, disabled, PACK_VERTICAL);
    bi = BoxinfoNew(bi_parent, parent_hwnd);
    bi->type = G_CHILD_DIRLIST;
    bi->disabled = disabled;
    bi->id = id;
    bi->item_count = item_count;
    bi->items_visible = items_visible;
    bi->group = TRUE;
    bi->label = (char *) malloc(MAX_PATH + 1);
    assert(bi->label);

    CalcSize(bi);
    AddLabel(bi_parent, parent_hwnd, "",
             0, FALSE, NULL,
             LEFT, FALSE);
    if (hasframe)
        layout_pop(bi_parent);
    return bi;
}


static BOXINFO *AddList(BOXINFO * bi_parent, HWND parent_hwnd, 
            int id, int item_count, int items_visible,
            int on, char *data[],
            int hasframe, char *title, int disabled)
{
    BOXINFO *bi, *bi_frame = NULL;

    if (hasframe)
    bi_frame = AddFrame(bi_parent, parent_hwnd, 
                            id, title, disabled, PACK_VERTICAL);
    bi = BoxinfoNew(bi_parent, parent_hwnd);
    bi->type = G_CHILD_LISTBOX;
    bi->disabled = disabled;
    bi->id = id;
    bi->data = data;
    bi->item_count = item_count;
    bi->items_visible = items_visible;
    bi->on = on;
    bi->group = TRUE;
    CalcSize(bi);
    if (hasframe)
        layout_pop(bi_parent);
    return bi;
}


static BOXINFO *AddOption(BOXINFO * bi_parent, HWND parent_hwnd, char *label,
              int id, int item_count, int on, char *data[],
              int hasframe, char *title, int hor, int disabled)
{
    BOXINFO *bi;
    int new_direction;
    if (hor)
        new_direction = PACK_HORIZONTAL;
    else
        new_direction = PACK_VERTICAL;
    if (hasframe) 
      AddFrame(bi_parent, parent_hwnd, 
                            id, title, disabled, new_direction);
    else{
      AddPanel(bi_parent, parent_hwnd, 
                            new_direction);
    }
    if (label) {                         
    bi = BoxinfoNew(bi_parent, parent_hwnd);
    bi->type = G_CHILD_LABEL;
    bi->disabled = disabled;
    bi->group = TRUE;
    BoxinfoLabel(bi, label);
    CalcSize(bi);
       if (hor)
          bi->pinf.flag = ALIGN_Y_CENTER; //GRID_Y;
    }
    bi = BoxinfoNew(bi_parent, parent_hwnd);
    bi->type = G_CHILD_OPTIONMENU;
    bi->disabled = disabled;
    bi->id = id;
    bi->data = data;
    bi->item_count = item_count;
    bi->on = on;
    bi->group = TRUE;
    CalcSize(bi);
    if (hor)
        bi->pinf.flag = ALIGN_Y_CENTER; //GRID_Y;
    layout_pop(bi_parent);
    return bi;
}



static BOXINFO *AddRadio(BOXINFO * bi_parent, HWND parent_hwnd, 
             int id, int item_count, int on, char *data[],
             int hasframe, char *title, int hor, int disabled)
{
    BOXINFO *bi, *bi_frame = NULL;
    int i, new_direction;
    int pos = 0;

    if (hor)
        new_direction = PACK_HORIZONTAL;
    else
        new_direction = PACK_VERTICAL;
    if (hasframe) 
      bi_frame = AddFrame(bi_parent, parent_hwnd, 
                            id, title, disabled, new_direction);
    else{
      bi_frame = AddPanel(bi_parent, parent_hwnd, 
                            new_direction);
    }

    if (!hor) 
        bi_parent->marginy = RADIOMARGIN_Y;
    else 
        bi_parent->marginx = RADIOMARGIN_X;
    for (i = 0; i < item_count; i++) {
    bi = BoxinfoNew(bi_parent, parent_hwnd);
    bi->type = G_CHILD_RADIOBOX;
    bi->disabled = disabled;
    bi->pos = pos++;
    bi->id = id;
    if (!i)
        bi->group = TRUE;
    if (i == on)
        bi->on = TRUE;
    BoxinfoLabel(bi, data[i]);
    CalcSize(bi);
    }
    layout_pop(bi_parent);

    return bi_frame;
}

static BOXINFO *AddToggle(BOXINFO *bi_parent, HWND parent_hwnd, 
              int id, int item_count, int *on, char *data[],
               int hasframe, char *title, int hor, int disabled)
{
    BOXINFO *bi, *bi_frame = NULL;
    int i, new_direction, pos = 0;

    if (hor)
        new_direction = PACK_HORIZONTAL;
    else
        new_direction = PACK_VERTICAL;
    if (hasframe) 
      bi_frame = AddFrame(bi_parent, parent_hwnd, 
                            id, title, disabled, new_direction);
    else{
      bi_frame = AddPanel(bi_parent, parent_hwnd, 
                            new_direction);
    }
    if (!hor) 
        bi_parent->marginy = RADIOMARGIN_Y;
    else 
        bi_parent->marginx = RADIOMARGIN_X;
    item_count = max(1, item_count);
    item_count = min(MAX_CHECK_BUTTONS, item_count);
    for (i = 0; i < item_count; i++) {

    bi = BoxinfoNew(bi_parent, parent_hwnd);
    bi->type = G_CHILD_CHECKBOX;
    bi->disabled = disabled;
    bi->id = id;
    bi->pos = pos++;
    bi->on = on[i];
       bi->item_count = item_count;
    if (!i)
        bi->group = TRUE;
    BoxinfoLabel(bi, data[i]);
    CalcSize(bi);
    }
    layout_pop(bi_parent);
    return bi_frame;
}


static void bi2ci(BOXINFO * bi, G_POPUP_CHILDINFO * ci)
{
    static char nullchar[] = "";

    g_popup_init_info(ci);
    ci->cont_id        = bi->cont_id;
    ci->win_id         = bi->win_id;

    ci->type = bi->type;
    ci->id = bi->id;
    ci->item_count = bi->item_count;
    ci->items_visible = bi->items_visible;
    ci->item = bi->on;
    ci->disabled = bi->disabled;
    ci->horizontal = 0;
    ci->frame = 0;
    ci->title = NULL;
    ci->select = NULL;
    if (bi->label) {
    add_label(bi->label);
    ci->label = get_label();
    }
    else
    ci->label = nullchar;
    ci->data = bi->data;
    if (bi->ci) {
      ci->func = bi->func;
        ci->userdata  = bi->userdata;
    }
    else {
    ci->func = NULL;
       ci->userdata  = NULL;
    }
}

static int do_container_box_exit_stuff(HWND hwnd, BOXINFO *bi, int message_id)
{
    BOXINFO *bi_tmp;
    CONTAINER_INFO *cont_info = 
        cont_info_from_cont_id(bi->cont_id);

    if (!cont_info)
        return TRUE;
    if (cont_info->select_status == SELECT_STATUS_HIDDEN)
        return TRUE;
    cont_info->select_status = SELECT_STATUS_HIDDEN;
    for (bi_tmp = bi; bi_tmp; bi_tmp = bi_tmp->next){
        if (bi_tmp->type == G_CHILD_MULTILINETEXT) {
            int len;
            char *s;
            len = SendMessage(bi_tmp->hwnd, WM_GETTEXTLENGTH,
                              0, 0);
            s = (char *) malloc(len + 1);
            assert(s);
            len = SendMessage(bi_tmp->hwnd, WM_GETTEXT,
                              len+1, (LPARAM) s);
            s[len] = '\0';
            bi_tmp->label = (char *) realloc(bi_tmp->label, len + 1);
            assert(bi_tmp->label);
            strcpy(bi_tmp->label, s);
            free(s);
        }
    }
    if (message_id == ID_OK) {
        if (cont_info->okfunc) {
            static G_POPUP_CHILDINFO ci;
            g_popup_init_info(&ci);
            ci.type       = G_CHILD_OK;
            ci.id         = -1;
            ci.cont_id    = cont_info->cont_id;
            ci.win_id     = cont_info->win_id;
            ci.func       = cont_info->okfunc;
            ci.userdata   = cont_info->okdata;
            cont_info->okfunc(&ci);
        }
        if (cont_info->flag & G_POPUP_WAIT){
            EndDialog(hwnd, STATUS_OK);
            return TRUE; 
        }
        else if (cont_info->flag & G_POPUP_KEEP){
            ShowWindow(hwnd, SW_HIDE);
            return TRUE; 
        }
        else {
            DestroyWindow(hwnd);
        } 

    }
    else {
        if (cont_info->cancelfunc) {
            static G_POPUP_CHILDINFO ci;
            g_popup_init_info(&ci);
            ci.type       = G_CHILD_CANCEL;
            ci.id         = -1;
            ci.cont_id    = cont_info->cont_id;
            ci.win_id     = cont_info->win_id;
            ci.func       = cont_info->cancelfunc;
            ci.userdata   = cont_info->canceldata;
            cont_info->cancelfunc(&ci);
        }
        if (cont_info->flag & G_POPUP_WAIT){
            EndDialog(hwnd, STATUS_CANCEL);
            return TRUE; 
        }
        else if (cont_info->flag & G_POPUP_KEEP){
            ShowWindow(hwnd, SW_HIDE);
            return TRUE; 
        } 
        else {
            DestroyWindow(hwnd);
        } 
    }

    if (!(cont_info->flag & G_POPUP_KEEP)){
        if (cont_info->flag & G_POPUP_TAB) {
            int i,ntabs = cont_info->tabinfo->numtabs;

            for (i=0; i<ntabs;i++) {
                BOXINFO *bi_tab = cont_info->tabinfo->bi_array[i];
                BoxinfoFree(bi_tab);
            }
        }
        BoxinfoFree(cont_info->container);
        destroy_container(cont_info->local_win_id, bi->cont_id);
    } 
    return TRUE; 
}

LRESULT APIENTRY ContainerBoxFunc(HWND hwnd, UINT message, 
                                  WPARAM wParam, LPARAM lParam)
{
    BOXINFO *bi, *bi_parent;

    switch (message) {
    case WM_INITDIALOG:
        bi_parent = (BOXINFO *) lParam;
        SetWindowLong(hwnd, DWL_USER, (LONG) lParam);
        InitBoxChildren(hwnd, bi_parent);
        for (bi = bi_parent; bi; bi = bi->next)
        if (bi->type == G_CHILD_TEXTBOX ||
            bi->type == G_CHILD_MULTILINETEXT ||
            bi->type == G_CHILD_PUSHBUTTON ||
            bi->type == G_CHILD_SCALE ||
            bi->type == G_CHILD_SPINBOX ||
            bi->type == G_CHILD_SPINBOXTEXT ||
            bi->type == G_CHILD_CHECKBOX ||
            bi->type == G_CHILD_RADIOBOX ||
            bi->type == G_CHILD_LISTBOX) {
                SetFocus(GetParent(bi->hwnd));
                SetFocus(bi->hwnd);
                break;
        }
        if (bi_parent->type == G_PARENT_TAB){
            RECT rc;
            CONTAINER_INFO *cont_info = 
                    cont_info_from_cont_id(bi_parent->cont_id);
            if (cont_info == NULL )
                break;                                              

            rc.right = bi_parent->x + bi_parent->width; 
            rc.bottom = bi->parent->y + bi_parent->height; 
            rc.left = bi_parent->x; 
            rc.top = bi->parent->y; 
            TabCtrl_AdjustRect(cont_info->tabinfo->hwndTab, FALSE, &rc);
            SetWindowPos(hwnd, HWND_TOP, 
                             rc.left, rc.top,  
                             0, 0, SWP_NOSIZE); 
        }
        return TRUE;

    case WM_HSCROLL:            
    case WM_VSCROLL:            
            
        bi_parent = (BOXINFO*) (GetWindowLong(hwnd, DWL_USER));
        if (bi_parent==NULL)
            break;
        bi = BoxinfoWhich(bi_parent, GetDlgCtrlID((HWND)lParam));    
        if (!bi || bi->type != G_CHILD_SCALE)
            break;

        switch (LOWORD(wParam)){
            case SB_THUMBTRACK: {
                static G_POPUP_CHILDINFO ci;
                int totrange = bi->range[1] - bi->range[0];
                int pos;
                // swapped scale
                if (bi->range[3])
                    pos = (totrange-HIWORD(wParam)) + bi->range[0];
                else
                    pos = HIWORD(wParam)+bi->range[0];
                bi->on = pos;
                if (bi->func) {
                    bi2ci(bi, &ci);
                    bi->func(&ci);
                }
                }
                break;
            }
            return TRUE;

    case WM_NOTIFY: {

        LPNMHDR pnmh = (LPNMHDR) lParam ;
        bi_parent = (BOXINFO*) (GetWindowLong(hwnd, DWL_USER));
        if (bi_parent==NULL)
            break;
        bi = BoxinfoWhich(bi_parent, LOWORD(wParam));    
        if (!bi)
            break;
        if (bi->type == G_CHILD_TAB) {
            if (pnmh->code == TCN_SELCHANGE ) 
                 OnSelChanged(hwnd, bi_parent);
        }
        else if (bi->type == G_CHILD_SPINBOX){
            static G_POPUP_CHILDINFO ci;
            static char spinlabel[20];
            NM_UPDOWN *pnmud = (NM_UPDOWN FAR *) lParam;
            int newpos = pnmud->iPos + pnmud->iDelta;

            if (newpos < bi->range[0]/bi->range[3])
                newpos = bi->range[1]/bi->range[3];
            else if (newpos > bi->range[1]/bi->range[3])
                newpos = bi->range[0]/bi->range[3];
            make_spin_label(newpos, bi->range, spinlabel);
            SendMessage(bi->prev->hwnd, WM_SETTEXT, 0, 
                             (LPARAM) spinlabel); 
            bi->on = newpos * bi->range[3];
            if (bi->func) {
                bi2ci(bi, &ci);
                bi->func(&ci);
            }
        }
        else if (bi->type == G_CHILD_SPINBOXTEXT){
            static G_POPUP_CHILDINFO ci;
            NM_UPDOWN *pnmud = (NM_UPDOWN FAR *) lParam;
            int newpos = pnmud->iPos + pnmud->iDelta;
               
            if (newpos < 0)
                newpos = bi->item_count-1;
            else if (newpos >= bi->item_count)
                newpos = 0;

            SendMessage(bi->prev->hwnd, WM_SETTEXT, 0, 
                   (LPARAM) bi->data[newpos]); 
 
            bi->on = newpos;
            if (bi->func) {
                bi2ci(bi, &ci);
                bi->func(&ci);
            }
            }
        }
        break; 

    case WM_COMMAND: 

      switch (LOWORD(wParam)) {
        case ID_OK:
        case ID_CANCEL:

          bi = (BOXINFO*) (GetWindowLong(hwnd, DWL_USER));
          if (bi) 
              return do_container_box_exit_stuff(hwnd, bi, LOWORD(wParam));
          break;

        default:{

          bi_parent = (BOXINFO*) (GetWindowLong(hwnd, DWL_USER));
          if (bi_parent==NULL)
            break;
          bi = BoxinfoWhich(bi_parent, LOWORD(wParam));    
          if (!bi)
            break;
          
          switch (bi->type) {

          case G_CHILD_PUSHBUTTON:{
            static G_POPUP_CHILDINFO ci;

            if (bi->pos > 0)
                SendMessage(hwnd, WM_COMMAND, (WORD) bi->id, 0);
            if (bi->func) {
                bi2ci(bi, &ci);
                bi->func(&ci);
            }
            }
            break;

          case G_CHILD_CHECKBOX:{
            static G_POPUP_CHILDINFO ci;
            static checked[MAX_CHECK_BUTTONS];
            BOXINFO *bi_temp;
            int item, on, i, id;

            item = bi->pos;
            id   = bi->id;
            on = SendMessage((HWND) lParam,
                     BM_GETCHECK, 0, 0);
            if (bi->on == on)
                break;
            bi->on = on;
            for (i = 0; i < MAX_CHECK_BUTTONS; i++)
                checked[i] = FALSE;
            bi_temp = bi;
            while (bi_temp && bi_temp->prev && 
                     bi_temp->type == G_CHILD_CHECKBOX)
                bi_temp = bi_temp->prev;
            while (bi_temp) {
                if (id == bi_temp->id)
                    checked[bi_temp->pos] = bi_temp->on;
                bi_temp = bi_temp->next;
            }
            if (bi->func) {
                bi2ci(bi, &ci);
                ci.id     = bi->id;
                ci.item   = item;
                ci.select = checked;
                bi->func(&ci);
            }
            }
            break;

          case G_CHILD_RADIOBOX:{
            static G_POPUP_CHILDINFO ci;
            BOXINFO *bi_temp;
            int id;

            if (bi->on == TRUE)
                break;
            id   = bi->id;
            bi_temp = bi;
            while (bi_temp->prev && bi_temp->pos > 0 &&
                                   bi_temp->type == G_CHILD_RADIOBOX){
                    bi_temp = bi_temp->prev;
            }    
            while (bi_temp &&
                         bi_temp->type == G_CHILD_RADIOBOX) {
                bi_temp->on = FALSE;
                bi_temp = bi_temp->next;
            }
            bi->on = TRUE;
            if (bi->func) {
                bi2ci(bi, &ci);
                ci.id   = bi->id;
                ci.item = bi->pos;
                bi->func(&ci);
            }
            }
            break;

          case G_CHILD_MULTILINETEXT:
            break;
          case G_CHILD_TEXTBOX:{
            static G_POPUP_CHILDINFO ci;
            int len;
            char *s;
            if (HIWORD(wParam) != EN_UPDATE)
                break;
            len = SendMessage((HWND) lParam, WM_GETTEXTLENGTH,
                      0, 0);
            s = (char *) malloc(len + 1);
            assert(s);
            len = SendMessage((HWND) lParam, WM_GETTEXT,
                      len+1, (LPARAM) s);
            s[len] = '\0';
            if (strcmp(s, bi->label) == 0) {
                free(s);
                break;
            }
            bi->label = (char *) realloc(bi->label, len + 1);
            assert(bi->label);
            strcpy(bi->label, s);
            free(s);
            if (bi->func) {
                bi2ci(bi, &ci);
                bi->func(&ci);
            }
            }
            break;

          case G_CHILD_LISTBOX:{
            static G_POPUP_CHILDINFO ci;
            int on;

            on = SendMessage((HWND) lParam,
                     LB_GETCURSEL, 0, 0);
            if (on == LB_ERR || bi->on == on) {
                break;
            }
            bi->on = on;
            if (bi->func) {
                bi2ci(bi, &ci);
                bi->func(&ci);
            }
            }
            break;

          case G_CHILD_OPTIONMENU:{
            static G_POPUP_CHILDINFO ci;
            int on;
            on = SendMessage((HWND) lParam,
                     CB_GETCURSEL, 0, 0);
            if (on == CB_ERR || bi->on == on) {
                break;
            }
            bi->on = on;
            if (bi->func) {
                bi2ci(bi, &ci);
                bi->func(&ci);
            }
            }
            break;
          default:
            g_bell();
          }
        }
        break;
      }
      break;

    case WM_SYSCOMMAND:
        if (wParam == SC_CLOSE) {
            bi = (BOXINFO*) (GetWindowLong(hwnd, DWL_USER));
            if (bi) 
                return do_container_box_exit_stuff(hwnd, bi, ID_CANCEL);
            break;
        }
        break;
    }
    return FALSE;
}


/**********************************************************/
/**********************************************************************/
/* CONTAINER
*/

static int container_pool = 1;

static void init_container(int local_win_id)
{
    WI[local_win_id].cont_info = NULL;
}


static CONTAINER_INFO *cont_info_from_cont_id(int cont_id)
{
    CONTAINER_INFO *inf,*temp;
    int i;

    for (i=0;i<G_MAXWINDOW;i++) {
        if (WI[i].hparent != NULL &&
            WI[i].cont_info != NULL) {
                inf = WI[i].cont_info;
                while (inf != NULL) {
                    if (inf->cont_id == cont_id)
                        return inf;
                    inf = inf->next;
                }
        }
    }
    return NULL;
}

static CONTAINER_INFO *add_container(int win_id, int local_win_id, int flag)
{
    CONTAINER_INFO *inf,*temp;

// ???????????
// This is memory is not released ever for G_POPUP_KEEP

    inf = (CONTAINER_INFO*) malloc(sizeof(CONTAINER_INFO));
    assert(inf);
    inf->win_id = win_id;
    inf->local_win_id = local_win_id;
    inf->cont_id = container_pool++;
    inf->flag = flag;
    inf->select_status = SELECT_STATUS_NEW;
    inf->container = NULL;
    inf->hwndBox = NULL;
    inf->okfunc = NULL;
    inf->cancelfunc = NULL;
    strcpy(inf->oklabel_text,     "   OK   ");
    strcpy(inf->cancellabel_text, " CANCEL ");
    inf->next = NULL;
    if (flag & G_POPUP_TAB) {
        inf->tabinfo = (TABDLGINFO*) malloc(sizeof(TABDLGINFO));
        assert(inf->tabinfo);
        inf->tabinfo->hwndTab = NULL; 
        inf->tabinfo->hwndDisplay = NULL;
        inf->tabinfo->numtabs = 0;
    }
    else
        inf->tabinfo = NULL;
    temp = WI[local_win_id].cont_info;
    if (temp == NULL) {
        WI[local_win_id].cont_info = inf;
        return inf;
    }
    while (temp->next != NULL)
        temp = temp->next;
    temp->next = inf;
    return inf;   
}

static void destroy_container(int local_win_id, int cont_id)
{
    CONTAINER_INFO *info, *temp;

    temp = WI[local_win_id].cont_info;
    if (temp == NULL) 
        return;
    if (temp->tabinfo)
        free(temp->tabinfo);
    if (temp->cont_id == cont_id){
        WI[local_win_id].cont_info = temp->next;
        free(temp);
        return;
    }
    while (temp->next != NULL) {
        info = temp;
        temp = temp->next;
        if (temp->cont_id == cont_id) {
            info->next = temp->next;
            free(temp);
            return;
        }
    }
}

void g_SCREEN_popup_add_child(int cont_id, G_POPUP_CHILDINFO *ci)
{
    CONTAINER_INFO *cont_info;
    BOXINFO *bi, *bi_parent;
    int ntabs;

    cont_info = cont_info_from_cont_id(cont_id);
    if (cont_info == NULL)
        return;
    ci->cont_id = cont_id;
    ci->win_id  = cont_info->win_id;

    bi_parent = cont_info->container;
    if (cont_info->flag & G_POPUP_TAB){
        int ntabs = cont_info->tabinfo->numtabs;
        if (ntabs > 0)
            bi_parent = cont_info->tabinfo->bi_array[ntabs-1];
    }
    switch (ci->type) {

        case G_CHILD_TAB:
            if (!cont_info->flag & G_POPUP_TAB)
                break;
            if ((ntabs=cont_info->tabinfo->numtabs) >= MAX_TABBOXINFO)
                break;
            cont_info->tabinfo->numtabs++;
            bi = cont_info->tabinfo->bi_array[ntabs] = 
                OpenBox(cont_info->parent_hwnd, 
                    DEFAULT_X, DEFAULT_Y, FALSE, ci->title);
            bi->type = G_PARENT_TAB;
            BoxinfoLabel(bi, ci->label);
            bi->level=1;
            bi->cont_id = cont_info->cont_id;
            bi->win_id = cont_info->win_id;
            bi->flag = FILL_X;

            break;
        case G_CHILD_PANEL:
            if (ci->item) {
                int direction;
                if (ci->horizontal)
                    direction = PACK_HORIZONTAL;
                else
                    direction = PACK_VERTICAL;

                if (ci->frame)
                        AddFrame(bi_parent,
                             cont_info->parent_hwnd, 
                             ci->id, ci->title, ci->disabled, 
                             direction);
                else
                    AddPanel(bi_parent,
                             cont_info->parent_hwnd, 
                             direction);
                if (direction == PACK_HORIZONTAL)
                    bi_parent->flag |= ALIGN_Y_CENTER;
            }
            else
                layout_pop(bi_parent);
            break;
        case G_CHILD_PUSHBUTTON:
            bi = AddPushButton(bi_parent,
                          cont_info->parent_hwnd, 
                          ci->label, ci->id,
                          FALSE, ci->title, FALSE, ci->disabled);
            bi->ci = ci;
            bi->func = ci->func;
            bi->userdata = ci->userdata;
            break;
        case G_CHILD_SPINBOX:
            bi = AddSpinbox(bi_parent,
                          cont_info->parent_hwnd, 
                          FALSE, ci->label,
                          ci->id, ci->item_count, ci->select,
                          ci->items_visible, ci->item, ci->data,
                          ci->frame, ci->title, ci->disabled);
            bi->ci = ci;
            bi->func = ci->func;
            bi->userdata = ci->userdata;
          break;
        case G_CHILD_SPINBOXTEXT:
            bi = AddSpinbox(bi_parent,
                          cont_info->parent_hwnd, 
                          TRUE, ci->label,
                          ci->id, ci->item_count, ci->select,
                          ci->items_visible, ci->item, ci->data,
                          ci->frame, ci->title, ci->disabled);
            bi->ci = ci;
            bi->func = ci->func;
            bi->userdata = ci->userdata;
            break;
        case G_CHILD_SCALE:
            bi = AddScale(bi_parent,
                          cont_info->parent_hwnd, 
                          ci->label,
                          ci->id, ci->item_count, ci->select,
                          ci->items_visible, ci->item, ci->data,
                          ci->frame, ci->title, ci->horizontal,
                          ci->disabled);
            bi->ci = ci;
            bi->func = ci->func;
            bi->userdata = ci->userdata;
            break;
        case G_CHILD_LABEL:
            AddLabel(bi_parent,
                     cont_info->parent_hwnd, 
                     ci->label,
                     ci->id, ci->frame, ci->title,
                     LEFT, ci->disabled);
            break;
        case G_CHILD_MULTILINETEXT:
            bi = AddEdit(bi_parent,
                     cont_info->parent_hwnd, 
                     TRUE, ci->label, ci->id,
                     ci->item_count, ci->items_visible,
                     ci->frame, ci->title, ci->horizontal, ci->disabled);
            bi->ci = ci;
            bi->func = ci->func;
            bi->userdata = ci->userdata;
            break;
        case G_CHILD_GETFILENAME:
        case G_CHILD_GETDIRNAME:
        case G_CHILD_TEXTBOX:
            bi = AddEdit(bi_parent,
                     cont_info->parent_hwnd, 
                     FALSE, ci->label, ci->id,
                     ci->item_count, ci->items_visible,
                     ci->frame, ci->title, ci->horizontal, ci->disabled);
            bi->ci = ci;
            bi->func = ci->func;
            bi->userdata = ci->userdata;
            break;
        case G_CHILD_RADIOBOX:
            bi = AddRadio(bi_parent,
                     cont_info->parent_hwnd, 
                     ci->id,
                     ci->item_count,
                     ci->item, ci->data,
                     ci->frame, ci->title, ci->horizontal,
                     ci->disabled);
            bi->ci = ci;
            bi->func = ci->func;
            bi->userdata = ci->userdata;
            while (bi->next){
                bi = bi->next;
                bi->ci = ci;
                bi->func = ci->func;
                bi->userdata = ci->userdata;
            }
            break;
        case G_CHILD_CHECKBOX:
            bi = AddToggle(bi_parent,
                      cont_info->parent_hwnd, 
                      ci->id,
                      ci->item_count,
                      ci->select, ci->data,
                      ci->frame, ci->title, ci->horizontal,
                      ci->disabled);
            bi->ci = ci;
            bi->func = ci->func;
            bi->userdata = ci->userdata;
            while (bi->next){
                bi = bi->next;
                bi->ci = ci;
                bi->func = ci->func;
                bi->userdata = ci->userdata;
            }
            break;
        case G_CHILD_LISTBOX:
            bi = AddList(bi_parent,
                      cont_info->parent_hwnd, 
                      ci->id,
                      ci->item_count, ci->items_visible,
                      ci->item, ci->data, ci->frame, ci->title,
                      ci->disabled);
            bi->ci = ci;
            bi->func = ci->func;
            bi->userdata = ci->userdata;
            break;
        case G_CHILD_OPTIONMENU:
            bi = AddOption(bi_parent,
                      cont_info->parent_hwnd, 
                      ci->label, ci->id,
                      ci->item_count, ci->item, ci->data,
                      ci->frame, ci->title, ci->horizontal,
                      ci->disabled);
            bi->ci = ci;
            bi->func = ci->func;
            bi->userdata = ci->userdata;
            break;
        case G_CHILD_SEPARATOR:
            AddSeparator(bi_parent, cont_info->parent_hwnd);
            break;

        case G_CHILD_OK:
            cont_info->okfunc = ci->func;
            cont_info->okdata = ci->userdata;
            break;
   
        case G_CHILD_CANCEL:
            cont_info->cancelfunc = ci->func;
            cont_info->canceldata = ci->userdata;
            break;
    }
}



int g_SCREEN_popup_container_open(int win_id, char *title, int flag)
{
    int local_win_id = GET_LOCAL_ID(win_id);
    CONTAINER_INFO *cont_info;

    cont_info = add_container(win_id, local_win_id, flag);
    cont_info->parent_hwnd = WI[local_win_id].child[C_DRAWAREA].hwnd;
    cont_info->flag = flag;
    cont_info->container = OpenBox(cont_info->parent_hwnd, 
                    DEFAULT_X, DEFAULT_Y, (flag & G_POPUP_WAIT), title);
    cont_info->container->level=1;
    cont_info->container->cont_id = cont_info->cont_id;
    cont_info->container->win_id = win_id;
    cont_info->container->flag = FILL_X;

    if (flag & G_POPUP_TAB) {
        BOXINFO *bi = BoxinfoNew(cont_info->container, 
                        cont_info->parent_hwnd);
        bi->type = G_CHILD_TAB;
        bi->pinf.width = 100;
        bi->pinf.height = 100;
    }    
    return cont_info->cont_id;
}


void g_SCREEN_popup_container_close(int cont_id)
{
    CONTAINER_INFO *cont_info;

    cont_info = cont_info_from_cont_id(cont_id);
    if (cont_info == NULL)
        return;
    if (cont_info->select_status == SELECT_STATUS_HIDDEN)
        return;
    cont_info->select_status = SELECT_STATUS_CANCEL;
    if (cont_info->hwndBox) // && (cont_info->flag & G_POPUP_KEEP)) 
        SendMessage(cont_info->hwndBox, WM_COMMAND, ID_CANCEL, 0);
}


int g_SCREEN_popup_container_show(int cont_id)
{
    int i, ret_value = 1;    
    CONTAINER_INFO *cont_info;

    cont_info = cont_info_from_cont_id(cont_id);
    if (cont_info == NULL)
        return FALSE;
    if (cont_info->select_status == SELECT_STATUS_HIDDEN && 
            !(cont_info->flag & G_POPUP_WAIT)) {
        cont_info->select_status = SELECT_STATUS_WAIT;
        ShowWindow(cont_info->hwndBox, SW_SHOW);
        return FALSE;
    }
    if (cont_info->select_status == SELECT_STATUS_HIDDEN && 
            (cont_info->flag & G_POPUP_WAIT)) {
        // A modal + hidden dialog. This should never happen.
        g_bell();
        return FALSE;
    }
    else {
        if (cont_info->select_status != SELECT_STATUS_NEW) 
            return FALSE;
        cont_info->select_status = SELECT_STATUS_WAIT;

        if (cont_info->flag & G_POPUP_TAB) {
            int ntabs = cont_info->tabinfo->numtabs;
            int height, width;

            width =cont_info->container->next->pinf.width;
            height = cont_info->container->next->pinf.height;

            for (i=0; i<ntabs;i++) {
                BOXINFO *bi_tab = cont_info->tabinfo->bi_array[i];
                manage_layout(&bi_tab->pinf);
                {
                    BOXINFO *bi = bi_tab;
                    while (bi) {
                        bi->x = bi->pinf.x + TAB_BORDER;
                        bi->y = bi->pinf.y + TAB_BORDER;
                        bi->cx = bi->pinf.width;
                        bi->cy = bi->pinf.height;
                        bi->height = bi->pinf.height;
                        bi=bi->next;
                    }
                }
                width = max(width, bi_tab->cx);
                height = max(height, bi_tab->pinf.height);
            }
            cont_info->container->next->pinf.width  = width+TAB_BORDER*2;
            cont_info->container->next->pinf.height = height+TAB_BORDER*2;
        }
        else
            AddSeparator(cont_info->container, cont_info->parent_hwnd);
        cont_info->container->flag = FILL_X;
        AddPanel(cont_info->container, cont_info->parent_hwnd, 
              PACK_HORIZONTAL);
        cont_info->container->flag = ALIGN_X_CENTER | ALIGN_Y_CENTER;
        AddPushButton(cont_info->container, cont_info->parent_hwnd, 
              cont_info->oklabel_text,
              ID_OK, FALSE, NULL, BUTTON_LEFT, FALSE);
        if (!(cont_info->flag & G_POPUP_SINGLEBUTTON))
            AddPushButton(cont_info->container, cont_info->parent_hwnd, 
              cont_info->cancellabel_text,
              ID_CANCEL, FALSE, NULL, BUTTON_RIGHT, FALSE);

        manage_layout(&cont_info->container->pinf);
        {
            BOXINFO *bi = cont_info->container;

            while (bi) {
                bi->x = bi->pinf.x;
                bi->y = bi->pinf.y;
                bi->cx = bi->pinf.width;
                bi->cy = bi->pinf.height;
                bi->height = bi->pinf.height;
                bi=bi->next;
            }
        }
    } 
    /*
     * Modal box
     */
    if (cont_info->flag & G_POPUP_WAIT) {
        int result;
        BuildDialogBox(_hInst, cont_info->parent_hwnd, 
                       cont_info->container,
                       ContainerBoxFunc, &result);
        cont_info->select_status = result;
    }
    else {
        int result;
        cont_info->hwndBox = BuildDialogBox(_hInst, 
                       cont_info->parent_hwnd, 
                       cont_info->container,
                       ContainerBoxFunc, &result);
        cont_info->select_status = STATUS_OK;
        return FALSE;
    }
    if (cont_info->select_status == STATUS_OK) {
        BOXINFO *bi, *bi_temp;
        int isel=0;

        ret_value = TRUE; 
        bi = cont_info->container;
        while (bi) {
          switch (bi->type) {
          case G_CHILD_MULTILINETEXT: 
          case G_CHILD_TEXTBOX:
            if (bi->ci) {
                add_label(bi->label);
                bi->ci->label = get_label();
            }
            break;

          case G_CHILD_SCALE:
          case G_CHILD_OPTIONMENU:
          case G_CHILD_LISTBOX:
          case G_CHILD_SPINBOX:
          case G_CHILD_SPINBOXTEXT:
            if (bi->ci)
                bi->ci->item = bi->on;
            break;

          case G_CHILD_RADIOBOX:
            if (bi->ci && bi->on)
                bi->ci->item = bi->pos;
            break;

          case G_CHILD_CHECKBOX:
            if (bi->ci)
                bi->ci->select[bi->pos] = bi->on;
             break;

           }
           bi = bi->next;
 
             if (bi == NULL && cont_info->tabinfo) {
                if (isel < cont_info->tabinfo->numtabs)
                    bi = cont_info->tabinfo->bi_array[isel++];
            }
        }
    }
    else
        ret_value = FALSE;
    if (!(cont_info->flag & G_POPUP_KEEP)){
        if (cont_info->flag & G_POPUP_TAB) {
            int i,ntabs = cont_info->tabinfo->numtabs;

            for (i=0; i<ntabs;i++) {
                BOXINFO *bi_tab = cont_info->tabinfo->bi_array[i];
                BoxinfoFree(bi_tab);
            }
        }
        BoxinfoFree(cont_info->container);
        destroy_container(cont_info->local_win_id, cont_id);
    } 
    return ret_value;
}


void g_SCREEN_popup_enable_item(int cont_id, int item_id, int enable)
{
    BOXINFO *bi;
    CONTAINER_INFO *cont_info;
    int isel = 0;

    cont_info = cont_info_from_cont_id(cont_id);
    if (cont_info == NULL)
        return;
    bi = cont_info->container;
    while (bi) {
        if (bi->id == item_id) {
            EnableWindow(bi->hwnd, enable);
            bi->disabled = !enable;
            //return; No return, because of frames and radio buttons
        }
        bi = bi->next;
 
        if (bi == NULL && cont_info->tabinfo) {
            if (isel < cont_info->tabinfo->numtabs)
                bi = cont_info->tabinfo->bi_array[isel++];
        }
    }
}


void g_SCREEN_popup_set_selection(int cont_id, int item_id, int position)
{
    BOXINFO *bi;
    CONTAINER_INFO *cont_info;
    int isel =0;

    cont_info = cont_info_from_cont_id(cont_id);
    if (cont_info == NULL)
        return;
    bi = cont_info->container;
    while (bi) {
    if (bi->id == item_id && bi->hwnd) {
        switch (bi->type) {
        case G_CHILD_RADIOBOX:
        if (bi->pos != position){
            bi->on = FALSE;
            SendMessage(bi->hwnd, BM_SETCHECK, BST_UNCHECKED, 0);
             }
        else{
            bi->on = TRUE;
            SendMessage(bi->hwnd, BM_SETCHECK, BST_CHECKED, 0);
             }
        break;
        case G_CHILD_OPTIONMENU:
        bi->on = position;
        SendMessage(bi->hwnd, CB_SETCURSEL, bi->on, 0);
        return;
        case G_CHILD_LISTBOX:
        bi->on = position;
        SendMessage(bi->hwnd, LB_SETCURSEL, bi->on, 0);
        return;

        case G_CHILD_SCALE: { 
             int totrange = bi->range[1] - bi->range[0];
             int pos;
             // swap scale
             if (bi->range[3])
                pos = totrange - (position - bi->range[0]);
             else
                pos = position - bi->range[0];
        SendMessage(bi->hwnd, TBM_SETPOS, TRUE, pos);
             bi->on = pos;
             }
        return;

        case G_CHILD_SPINBOX: {
             static char spinlabel[20];
                        
             if (position < bi->range[0] || 
                 position > bi->range[1])
                 return;
             make_spin_label(position/bi->range[3], bi->range, spinlabel);
             bi->on = position;
             SendMessage(bi->hwnd, UDM_SETPOS, TRUE, bi->on);
             SendMessage(bi->prev->hwnd, WM_SETTEXT, 0, 
                             (LPARAM) spinlabel); 
             }
        return;

        case G_CHILD_SPINBOXTEXT:
             if (position < 0 || position >= bi->item_count)
                   return;
        bi->on = position;
             SendMessage(bi->hwnd, UDM_SETPOS, TRUE, bi->on);
             SendMessage(bi->prev->hwnd, WM_SETTEXT, 0, 
                   (LPARAM) bi->data[position]);
        return;
        }
    }
    bi = bi->next;
 
        if (bi == NULL && cont_info->tabinfo) {
            if (isel < cont_info->tabinfo->numtabs)
                bi = cont_info->tabinfo->bi_array[isel++];
        }

    }
}


void g_SCREEN_popup_set_checkmark(int cont_id, int item_id, int position, int toggle_on) 
{
    CONTAINER_INFO *cont_info;
    BOXINFO *bi;
    int isel = 0;

    cont_info = cont_info_from_cont_id(cont_id);
    if (cont_info == NULL)
        return;
    bi = cont_info->container;
    while (bi) {
 
       if (bi->id == item_id && bi->type == G_CHILD_CHECKBOX) {
            if (position >= bi->item_count)
                return;
            if (bi->pos == position) {
                /*
                 * This prevents from calling user func
                 */
                bi->on = toggle_on;
                SendMessage(bi->hwnd, BM_SETCHECK, bi->on, 0);
                return;
            }
        }
        bi = bi->next;
 
        if (bi == NULL && cont_info->tabinfo) {
            if (isel < cont_info->tabinfo->numtabs)
                bi = cont_info->tabinfo->bi_array[isel++];
        }
    }
}


void g_SCREEN_popup_set_label(int cont_id, int item_id, char *label) 
{
    BOXINFO *bi;
    CONTAINER_INFO *cont_info;
    int isel=0;

    cont_info = cont_info_from_cont_id(cont_id);
    if (cont_info == NULL)
        return;

    bi = cont_info->container;
    while (bi) {
        if (bi->id == item_id) {
            switch (bi->type) {
            case G_CHILD_OPTIONMENU:
                bi = bi->prev;
                if (bi->type != G_CHILD_LABEL)
                    break;
                /*
                 * Fall through
                 */
            case G_CHILD_LABEL:
            case G_CHILD_MULTILINETEXT:
            case G_CHILD_TEXTBOX:
             bi->label = (char *) realloc(bi->label, strlen(label) + 1);
               assert(bi->label);
             strcpy(bi->label, label);
             SendMessage(bi->hwnd, WM_SETTEXT, 0, (LPARAM) label);
             return;
            }
        }
        bi = bi->next;
 
        if (bi == NULL && cont_info->tabinfo) {
            if (isel < cont_info->tabinfo->numtabs)
                bi = cont_info->tabinfo->bi_array[isel++];
        }

    }
}


char  *g_SCREEN_popup_get_label(int cont_id, int item_id) 
{
    BOXINFO *bi;
    CONTAINER_INFO *cont_info;
    int isel=0;

    cont_info = cont_info_from_cont_id(cont_id);
    if (cont_info == NULL)
        return;
    bi = cont_info->container;
    while (bi) {
        if (bi->id == item_id) {
            switch (bi->type) {
            case G_CHILD_OPTIONMENU:
                bi = bi->prev;
                if (bi->type != G_CHILD_LABEL)
                    return NULL;
                /*
                 * Fall through
                 */
            case G_CHILD_LABEL:
                return bi->label;

            case G_CHILD_MULTILINETEXT:
            case G_CHILD_TEXTBOX: {
                int len;
                len = SendMessage(bi->hwnd, WM_GETTEXTLENGTH, 0, 0);
                if (len <= 0)
                    return NULL;
                bi->label = (char *) realloc(bi->label, len + 1);
                assert(bi->label);
                SendMessage(bi->hwnd, WM_GETTEXT, 
                            (WPARAM) len+1, (LPARAM) bi->label);
                bi->label[len] = '\0';
                return bi->label;
                }
            }
        }
        bi = bi->next;
 
        if (bi == NULL && cont_info->tabinfo) {
            if (isel < cont_info->tabinfo->numtabs)
                bi = cont_info->tabinfo->bi_array[isel++];
        }
    }
    return NULL;
}

void g_SCREEN_popup_set_title(int cont_id, int item_id, char *title) 
{
    BOXINFO *bi;
    CONTAINER_INFO *cont_info;
    int isel=0;

    cont_info = cont_info_from_cont_id(cont_id);
    if (cont_info == NULL)
        return;
    bi = cont_info->container;
    while (bi) {
        if (bi->id == item_id) {
            if (bi->type == G_CHILD_FRAME) {
; // Seems not possible to change label of a group box
            }
            return;
        }
        bi = bi->next;
 
        if (bi == NULL && cont_info->tabinfo) {
            if (isel < cont_info->tabinfo->numtabs)
                bi = cont_info->tabinfo->bi_array[isel++];
        }
    }
}



void g_SCREEN_popup_set_focus(int cont_id, int item_id)
{
    BOXINFO *bi;
    CONTAINER_INFO *cont_info;
    int isel=0;

    cont_info = cont_info_from_cont_id(cont_id);
    if (cont_info == NULL)
        return;
    bi = cont_info->container;
    while (bi) {
        if (bi->id == item_id) {
          switch (bi->type) {
          case G_CHILD_MULTILINETEXT:
          case G_CHILD_TEXTBOX:
          case G_CHILD_RADIOBOX:
          case G_CHILD_CHECKBOX:
          case G_CHILD_LISTBOX:
          case G_CHILD_SCALE:
          case G_CHILD_SPINBOX:
          case G_CHILD_SPINBOXTEXT:
          case G_CHILD_OPTIONMENU:
          case G_CHILD_PUSHBUTTON:
          case G_CHILD_GETFILENAME:
          case G_CHILD_GETDIRNAME:
               if (GetFocus() != bi->hwnd)
                   SetFocus(bi->hwnd);
               return;
          }
        }
        bi = bi->next;
 
        if (bi == NULL && cont_info->tabinfo) {
            if (isel < cont_info->tabinfo->numtabs)
                bi = cont_info->tabinfo->bi_array[isel++];
        }
    }
}

void g_SCREEN_popup_set_buttonlabel(int cont_id, char *label, 
                                                        int whichbutton)
{
    CONTAINER_INFO *cont_info;
    int len;

    cont_info = cont_info_from_cont_id(cont_id);
    if (cont_info == NULL)
        return;
    len = strlen(label);
    if (len > POPBUTTONLABELSIZE)
        label[POPBUTTONLABELSIZE] = '\0';
    if (whichbutton == G_POPUP_BUTTON_CANCEL)
        strcpy(cont_info->cancellabel_text, label);
    else
        strcpy(cont_info->oklabel_text,label);
}

void g_SCREEN_popup_append_item(int cont_id, int item_id, char *label) 
{
    BOXINFO *bi;
    CONTAINER_INFO *cont_info;
    int isel=0;

    cont_info = cont_info_from_cont_id(cont_id);
    if (cont_info == NULL)
        return;
    bi = cont_info->container;
    while (bi) {
        if (bi->id == item_id) {
            switch (bi->type) {
              case G_CHILD_LISTBOX:
           SendMessage(bi->hwnd, LB_ADDSTRING, 0, (LPARAM) label);
                bi->item_count++;
                return;
            }
        }
        bi = bi->next;
 
        if (bi == NULL && cont_info->tabinfo) {
            if (isel < cont_info->tabinfo->numtabs)
                bi = cont_info->tabinfo->bi_array[isel++];
        }
    }
}


void g_SCREEN_popup_remove_item(int cont_id, int item_id, int pos) 
{
    BOXINFO *bi;
    CONTAINER_INFO *cont_info;
    int isel=0;

    cont_info = cont_info_from_cont_id(cont_id);
    if (cont_info == NULL)
        return;
    bi = cont_info->container;
    while (bi) {
        if (bi->id == item_id) {
            switch (bi->type) {
              case G_CHILD_LISTBOX:
                /*
                 * pos < 1 means: delete all
                 */
                if (pos < 1) {
                    while (SendMessage(bi->hwnd, LB_DELETESTRING, 0, 0) != LB_ERR);
                    break;
                }
                if (pos >= 0  && pos < bi->item_count) {
               SendMessage(bi->hwnd, LB_DELETESTRING, pos, 0);
                    bi->item_count--;
                }
                return;
            }
        }
        bi = bi->next;
 
        if (bi == NULL && cont_info->tabinfo) {
            if (isel < cont_info->tabinfo->numtabs)
                bi = cont_info->tabinfo->bi_array[isel++];
        }
    }
}

void g_SCREEN_popup_replace_item(int cont_id, int item_id, 
                  int pos, char *label)
{
    BOXINFO *bi;
    CONTAINER_INFO *cont_info;
    int isel=0;

    cont_info = cont_info_from_cont_id(cont_id);
    if (cont_info == NULL)
        return;
    bi = cont_info->container;
    while (bi) {
        if (bi->id == item_id) {
            switch (bi->type) {
              case G_CHILD_LISTBOX:
                if (pos >= 0  && pos < bi->item_count) {
               SendMessage(bi->hwnd, LB_INSERTSTRING, pos, (LPARAM) label);
               SendMessage(bi->hwnd, LB_DELETESTRING, pos+1, 0);
                }
                return;
            }
        }
        bi = bi->next;
 
        if (bi == NULL && cont_info->tabinfo) {
            if (isel < cont_info->tabinfo->numtabs)
                bi = cont_info->tabinfo->bi_array[isel++];
        }
    }
} 

/*================================================================

=================================================================*/
/* --- GET FILENAME BOX --- */
#define MAXDIRLEN	256
char *g_SCREEN_popup_getfilename(int win_id, char *title, char *mask)
{
    OPENFILENAME fname;
    static char fn[MAXDIRLEN];
    char init_dir[MAXDIRLEN];
    static char filter[140];
    char *p;
    int local_win_id, ok;
    HWND parent_hwnd;
    local_win_id = GET_LOCAL_ID(win_id);
    parent_hwnd = WI[local_win_id].child[C_DRAWAREA].hwnd;
  
/*  

Pointer to a buffer containing pairs of null-terminated filter strings. 
The last string in the buffer must be terminated by two NULL characters. 

The first string in each pair is a display string that describes 
the filter (for example, "Text Files"), and the second string 
specifies the filter pattern (for example, "*.TXT"). To specify 
multiple filter patterns for a single display string, use a semicolon 
to separate the patterns (for example, "*.TXT;*.DOC;*.BAK"). A pattern 
string can be a combination of valid filename characters and the 
asterisk (*) wildcard character. Do not include spaces in the pattern string.
*/
    memset(filter, '\0', 40);
    p = filter;
    ok = FALSE;

    if (mask) {
        char *pm;
        if ((pm=strrchr(mask, '\\')) != NULL) 
            pm = strrchr(mask, '\\')+1;
        else
            pm = mask;
        if (strcmp(pm,"*.*")== 0 || strcmp(pm, "*")==0)
            ok = TRUE;
        sprintf(p, "Filetype (%s)", pm);
        p += strlen(p)+1;
        strcpy(p, pm);
        p += strlen(p)+1;
    }
    if (!ok) {
        strcpy(p, "All (*.*)");
        p += strlen(p)+1;
        strcpy(p, "*.*");
        p += strlen(p)+1;
    }
    *p++ = '\0';
    *p++ = '\0';
    *p++ = '\0';
    *p++ = '\0';

    memset(fn, '\0', MAXDIRLEN);

    init_dir[0] = '\0';
    init_dir[1] = '\0';
    {
        char *buf,*p;
        if ((p=strrchr(mask, '\\')) != NULL) {
            strcpy(init_dir,mask);
            p = strrchr(init_dir, '\\');
            p++;
            *p = '\0';
        }
        else {
            buf = (char*)malloc(MAXDIRLEN);
            assert(buf);
            p = getcwd(buf, MAXDIRLEN);
            if (p!=NULL) {
                strcpy(init_dir, buf);
           // init_dir[strlen(init_dir)-1] = '\0';
            }
            free(buf);
        }
    }
    memset(&fname, 0, sizeof(OPENFILENAME));
    fname.lStructSize = sizeof(OPENFILENAME);
    fname.hwndOwner = parent_hwnd;
    fname.lpstrFilter = filter;
    fname.nFilterIndex = 1;
    fname.lpstrFile = fn;
    fname.nMaxFile = MAXDIRLEN;
    fname.lpstrFileTitle = NULL;
    fname.nMaxFileTitle = 0;
    fname.lpstrInitialDir = init_dir;
    fname.Flags = OFN_HIDEREADONLY | OFN_NOCHANGEDIR;
    fname.lpstrTitle = title;

    if (GetOpenFileName(&fname))
        return fn;
    else
        return NULL;
}


/* --- GET DIRNAME BOX --- */

static UINT APIENTRY dirhookproc(
    HWND hdlg,	// handle to child dialog window
    UINT uiMsg,	// message identifier
    WPARAM wParam,	// message parameter
    LPARAM lParam 	// message parameter
   )
{
    switch (uiMsg) {
        case WM_INITDIALOG:
            CommDlg_OpenSave_HideControl(GetParent(hdlg), stc3);
            CommDlg_OpenSave_HideControl(GetParent(hdlg), edt1);
            CommDlg_OpenSave_HideControl(GetParent(hdlg), stc2);
            CommDlg_OpenSave_HideControl(GetParent(hdlg), cmb1);
            CommDlg_OpenSave_SetControlText(GetParent(hdlg), IDOK, "Select");
            break;
    }
    return FALSE;
}

char *g_SCREEN_popup_getdirname(int win_id, char *title)
{
    OPENFILENAME fname;
    static char fn[MAXDIRLEN];
    char init_dir[MAXDIRLEN];
    static char filter[40];
    char *p;
    int local_win_id;
    HWND parent_hwnd;
    DWORD cchCurDir=MAX_PATH; 
    LPTSTR lpszCurDir; 
    /*
     * dummy extension to filter out all
     * regular files
     */
    char mask[] = ".___";

    local_win_id = GET_LOCAL_ID(win_id);
    parent_hwnd = WI[local_win_id].child[C_DRAWAREA].hwnd;
    lpszCurDir = init_dir;
    if (GetCurrentDirectory(cchCurDir, lpszCurDir)==0)
        return FALSE; 

    memset(filter, '\0', 40);
    p = filter;
    sprintf(p, "Filetype (%s)", mask);
    p += strlen(p)+1;
    strcat(p,mask);
    p += strlen(p)+1;
    *p++ = '\0';
    *p++ = '\0';
    *p++ = '\0';
    *p++ = '\0';

    memset(fn, '\0', MAXDIRLEN);
    /*
     * dummy file name, otherwise we can not
     * press 'ok'
     */
    fn[0] = 'a';
    memset(&fname, 0, sizeof(OPENFILENAME));
    fname.lStructSize = sizeof(OPENFILENAME);
    fname.hwndOwner = parent_hwnd;
    fname.lpstrFilter = filter;
    fname.nFilterIndex = 1;
    fname.lpstrFile = fn;
    fname.nMaxFile = MAXDIRLEN;
    fname.lpstrFileTitle = NULL; 
    fname.nMaxFileTitle = 0; 
    fname.lpstrInitialDir = init_dir;
    fname.Flags = OFN_HIDEREADONLY | OFN_NOCHANGEDIR|OFN_ENABLEHOOK | OFN_EXPLORER ;
    fname.lpstrTitle = title;
    fname.lpfnHook = dirhookproc;

    if (!GetOpenFileName(&fname))
        return NULL;
    /*
     * remove filename
     */
    if ((p=strrchr(fn, '\\')) != NULL) 
        *p = '\0';
    return fn;
}


/* --- SAVE FILE AS --- */
char *g_SCREEN_popup_saveas(int win_id, char *title, char *mask)
{
    OPENFILENAME fname;
//    char filename2[64];
    static char fn[MAXDIRLEN];
    //char mask[] = "*.*";
    char init_dir[MAXDIRLEN];
    char *p, filter[140];
    int local_win_id, ok;
    HWND parent_hwnd;

    local_win_id = GET_LOCAL_ID(win_id);
    parent_hwnd = WI[local_win_id].child[C_DRAWAREA].hwnd;

    memset(filter, '\0', 40);
    p = filter;
    ok = FALSE;
    if (mask) {
        char *pm;
        if ((pm=strrchr(mask, '\\')) != NULL) 
            pm = strrchr(mask, '\\')+1;
        else
            pm = mask;
        if (strcmp(pm,"*.*")== 0 || strcmp(pm, "*")==0)
            ok = TRUE;
        sprintf(p, "Filetype (%s)", pm);
        p += strlen(p)+1;
        strcpy(p, pm);
        p += strlen(p)+1;
    }
    if (!ok) {
        strcpy(p, "All (*.*)");
        p += strlen(p)+1;
        strcpy(p, "*.*");
        p += strlen(p)+1;
    }
    *p++ = '\0';
    *p++ = '\0';
    *p++ = '\0';
    *p++ = '\0';

  //  if (strchr(filename, '*') == NULL)
  //      strcpy(fn, filename);
  //  else
        fn[0] = '\0';
    init_dir[0] = '\0';
    init_dir[1] = '\0';
    {
        char *buf,*p;
        if ((p=strrchr(mask, '\\')) != NULL) {
            strcpy(init_dir,mask);
            p = strrchr(init_dir, '\\');
            p++;
            *p = '\0';
        }
        else {
            buf = (char*)malloc(MAXDIRLEN);
            assert(buf);
            p = getcwd(buf, MAXDIRLEN);
            if (p!=NULL) {
                strcpy(init_dir, buf);
           // init_dir[strlen(init_dir)-1] = '\0';
            }
            free(buf);
        }
    }
    memset(&fname, 0, sizeof(OPENFILENAME));
    fname.lStructSize = sizeof(OPENFILENAME);
    fname.hwndOwner = parent_hwnd;
    fname.lpstrFilter = filter;
    fname.nFilterIndex = 1;
    fname.lpstrFile = fn;
    fname.nMaxFile = MAXDIRLEN;
    fname.lpstrFileTitle = NULL;
    fname.nMaxFileTitle = 0;
    fname.lpstrTitle = title;
    fname.lpstrInitialDir = init_dir;
    fname.Flags = OFN_HIDEREADONLY | OFN_NOCHANGEDIR;
    if (GetSaveFileName(&fname))
        return fn;
    else
        return NULL;
}




/*********************************************************************/
/* --- CHECKBOX --- */

static LRESULT APIENTRY ModalBoxFunc(HWND hwnd, UINT message,
                     WPARAM wParam, LPARAM lParam)
{
    BOXINFO *bi_parent;
    BOXINFO *bi;

    switch (message) {
        case WM_INITDIALOG:
            bi_parent = (BOXINFO *) lParam;
            SetWindowLong(hwnd, DWL_USER, (LONG) lParam);
            InitBoxChildren(hwnd, bi_parent);
            for (bi = bi_parent; bi; bi = bi->next)
            if (bi->type == G_CHILD_TEXTBOX ||
                bi->type == G_CHILD_PUSHBUTTON ||
                bi->type == G_CHILD_CHECKBOX ||
                bi->type == G_CHILD_RADIOBOX ||
                bi->type == G_CHILD_DIRLIST ||
                bi->type == G_CHILD_LISTBOX) {
                    SetFocus(GetParent(bi->hwnd));
                    SetFocus(bi->hwnd);
                break;
            }
            break;

    case WM_COMMAND: 

        switch (HIWORD(wParam)) {
        case LBN_DBLCLK:
            bi_parent = (BOXINFO*) (GetWindowLong(hwnd, DWL_USER));
            if (bi_parent==NULL)
                break;

            bi = BoxinfoWhich(bi_parent, LOWORD(wParam));
            if (!bi)
                break;
            if (bi->type == G_CHILD_DIRLIST) {
                DWORD cchCurDir=MAX_PATH; 
                LPTSTR lpszCurDir; 

                lpszCurDir = bi->label;
                DlgDirSelectEx(hwnd, bi->label, cchCurDir, bi->local_id);
                DlgDirList(hwnd, lpszCurDir, bi->local_id, bi->local_id+1, 
                           DDL_EXCLUSIVE|DDL_DIRECTORY|DDL_DRIVES); 
                break;
            }
        }

        switch (LOWORD(wParam)) {
        case ID_OK:
            EndDialog(hwnd, STATUS_OK);
            return TRUE;

        case ID_NO:
            EndDialog(hwnd, STATUS_NO);
            return TRUE;

        case ID_CANCEL:
            EndDialog(hwnd, STATUS_CANCEL);
            return TRUE;

        default:{
            bi_parent = (BOXINFO*) (GetWindowLong(hwnd, DWL_USER));
            if (bi_parent==NULL)
                break;

            bi = BoxinfoWhich(bi_parent, LOWORD(wParam));
            if (!bi)
                break;
            switch (bi->type) {
            case G_CHILD_PUSHBUTTON:
                SendMessage(hwnd, WM_COMMAND, (WORD) bi->id, 0);
                break;

            case G_CHILD_CHECKBOX:
                bi->on = SendMessage((HWND) lParam,
                             BM_GETCHECK, 0, 0);
                break;

            case G_CHILD_RADIOBOX:{
                BOXINFO *bi_temp;
                bi->on = SendMessage((HWND) lParam,
                             BM_GETCHECK, 0, 0);
                if (!bi->on)
                    break;
                bi_temp = bi_parent;
                while (bi_temp) {
                    if (bi_temp->type == G_CHILD_RADIOBOX)
                        bi_temp->on = FALSE;
                    bi_temp = bi_temp->next;
                }
                bi->on = TRUE;
                }
                break;

            case G_CHILD_LISTBOX:
                bi->on = SendMessage((HWND) lParam,
                         LB_GETCURSEL, 0, 0);
                break;

            case G_CHILD_TEXTBOX:{
                int len;
                char *s;
                if (HIWORD(wParam) != EN_UPDATE)
                    break;
                len = SendMessage((HWND) lParam, WM_GETTEXTLENGTH,
                          0, 0);
                s = (char *) malloc(len + 1);
                assert(s);
                len = SendMessage((HWND) lParam, WM_GETTEXT,
                          len+1, (LPARAM) s);
                s[len] = '\0';
                if (strcmp(s, bi->label) != 0) {
                    bi->label = (char *) realloc(bi->label, len + 1);
                    assert(bi->label);
                    strcpy(bi->label, s);
                }
                free(s);
                }
                break;
            }
            }
            break;
        }
        break;

    case WM_SYSCOMMAND:
        if (wParam == SC_CLOSE) {
            EndDialog(hwnd, STATUS_CANCEL);
            return TRUE;
        }
        break;
    }
    return FALSE;
}

static LRESULT APIENTRY ModelessBoxFunc(HWND hwnd, UINT message,
                    WPARAM wParam, LPARAM lParam)
{
    BOXINFO *bi_parent;

    switch (message) {
        case WM_INITDIALOG:
            bi_parent = (BOXINFO *) lParam;
                SetWindowLong(hwnd, DWL_USER, (LONG) lParam);
            InitBoxChildren(hwnd, bi_parent);
            break;

        case WM_COMMAND:
            switch (LOWORD(wParam)) {
                case ID_CANCEL:
                case ID_OK:
                    bi_parent = (BOXINFO*) (GetWindowLong(hwnd, DWL_USER));
                    DestroyWindow(hwnd); 
                    if (bi_parent==NULL)
                        break;
                    BoxinfoFree(bi_parent);
                    return TRUE;

            default:{
                BOXINFO *bi;
                bi_parent = (BOXINFO*) (GetWindowLong(hwnd, DWL_USER));
                if (bi_parent==NULL)
                    break;
                bi = BoxinfoWhich(bi_parent, LOWORD(wParam));
                if (bi && bi->type == G_CHILD_PUSHBUTTON)
                    SendMessage(hwnd, WM_COMMAND, bi->id, 0);
                }
                break;
            }
            break;

        case WM_SYSCOMMAND:
            if (wParam == SC_CLOSE) {
                SendMessage(hwnd, WM_COMMAND, ID_CANCEL, 0);
                return TRUE;
            }
            break;
    }
    return FALSE;
}


static int CheckBox(int local_win_id, char *title, char *subtitle, int item_count,
            char **items, int *selected_check_pos)
{
    BOXINFO *simplepopup;
    HWND parent_hwnd;
    int result;

    parent_hwnd = WI[local_win_id].child[C_DRAWAREA].hwnd;
    simplepopup = OpenBox(parent_hwnd,DEFAULT_X, DEFAULT_Y, TRUE, title);
    WI[local_win_id].checkbox.popup = simplepopup;
    simplepopup->level=1;
    simplepopup->flag = GRID_X;

    AddToggle(simplepopup, parent_hwnd, 0,
          item_count,
          selected_check_pos, items,
          TRUE, subtitle, FALSE, FALSE);
    AddSeparator(simplepopup, parent_hwnd);
    simplepopup->flag = FILL_X;
    AddPanel(simplepopup, parent_hwnd, PACK_HORIZONTAL);
    simplepopup->flag = ALIGN_X_CENTER | ALIGN_Y_CENTER;
    AddPushButton(simplepopup, parent_hwnd, "   OK   ",
          ID_OK, FALSE, NULL, BUTTON_LEFT, FALSE);
    AddPushButton(simplepopup, parent_hwnd, " CANCEL ",
          ID_CANCEL, FALSE, NULL, BUTTON_RIGHT, FALSE);

    manage_layout(&simplepopup->pinf);
    {
        BOXINFO *bi = simplepopup;

        while (bi) {
            bi->x = bi->pinf.x;
            bi->y = bi->pinf.y;
            bi->cx = bi->pinf.width;
            bi->cy = bi->pinf.height;
            bi->height = bi->pinf.height;
            bi=bi->next;
        }
    }
    BuildDialogBox(_hInst, parent_hwnd, simplepopup, ModalBoxFunc, &result);
    return result;
}


int g_SCREEN_popup_checkbox(int win_id, char *title, char *subtitle,
             int item_count, char **items, int *select)
{
    int ret_value, status;
    int local_win_id = GET_LOCAL_ID(win_id);

    if (WI[local_win_id].checkbox.popup)
        return FALSE;
    status = CheckBox(local_win_id, title, subtitle, item_count,
              items, select);
    if (status == STATUS_OK) {
        BOXINFO *bi;

        bi = WI[local_win_id].checkbox.popup;
        while (bi) {
            if (bi->type == G_CHILD_CHECKBOX)
                select[bi->pos] = bi->on;
            bi = bi->next;
        }
        ret_value = TRUE;
    }
    else
        ret_value = FALSE;

    BoxinfoFree(WI[local_win_id].checkbox.popup);
    WI[local_win_id].checkbox.popup = NULL;
    return ret_value;
}

/* --- RADIOBOX --- */
static int RadioBox(int local_win_id, char *title, char *subtitle, int item_count,
            char **items, int selected_pos)
{
    BOXINFO *simplepopup;
    HWND parent_hwnd;
    int result;

    parent_hwnd = WI[local_win_id].child[C_DRAWAREA].hwnd;
    simplepopup = OpenBox(parent_hwnd,DEFAULT_X, DEFAULT_Y, TRUE, title);
    WI[local_win_id].radiobox.popup = simplepopup;
    simplepopup->level=1;
    simplepopup->flag = GRID_X;
    AddRadio(simplepopup, parent_hwnd, 0,
         item_count,
         selected_pos - 1, items,
         TRUE, subtitle, FALSE, FALSE);
    AddSeparator(simplepopup, parent_hwnd);
    simplepopup->flag = FILL_X;
    AddPanel(simplepopup, parent_hwnd, PACK_HORIZONTAL);
    simplepopup->flag = ALIGN_X_CENTER | ALIGN_Y_CENTER;
    AddPushButton(simplepopup, parent_hwnd, "   OK   ",
          ID_OK, FALSE, NULL, BUTTON_LEFT, FALSE);
    AddPushButton(simplepopup, parent_hwnd, " CANCEL ",
          ID_CANCEL, FALSE, NULL, BUTTON_RIGHT, FALSE);
    manage_layout(&simplepopup->pinf);
    {
        BOXINFO *bi = simplepopup;

        while (bi) {
            bi->x = bi->pinf.x;
            bi->y = bi->pinf.y;
            bi->cx = bi->pinf.width;
            bi->cy = bi->pinf.height;
            bi->height = bi->pinf.height;
            bi=bi->next;
        }
    }

    BuildDialogBox(_hInst, parent_hwnd, simplepopup, ModalBoxFunc, &result);
    return result;

}

int g_SCREEN_popup_radiobox(int win_id, char *title, char *subtitle, int item_count,
             char **items, int select_pos)
{
    int ret_value, status;
    int local_win_id = GET_LOCAL_ID(win_id);

    if (WI[local_win_id].radiobox.popup)
        return FALSE;
    status = RadioBox(local_win_id, title, subtitle, item_count,
              items, select_pos);
    if (status == STATUS_OK) {
    BOXINFO *bi;

    bi = WI[local_win_id].radiobox.popup;
    ret_value = 1;
    while (bi) {
        if (bi->type == G_CHILD_RADIOBOX)
        if (bi->on) {
            ret_value = bi->pos + 1;
            break;
        }
        bi = bi->next;
    }
    }
    else
    ret_value = 0;

    BoxinfoFree(WI[local_win_id].radiobox.popup);
    WI[local_win_id].radiobox.popup = NULL;
    return ret_value;
}

/* --- LISTBOX --- */
static int ListBox(int local_win_id, char *title, char *subtitle, int item_count,
           int items_visible, char **items, int selected_pos)
{
    BOXINFO *simplepopup;
    HWND parent_hwnd;
    int result;

    parent_hwnd = WI[local_win_id].child[C_DRAWAREA].hwnd;
    simplepopup = OpenBox(parent_hwnd,DEFAULT_X, DEFAULT_Y, TRUE, title);
    WI[local_win_id].listbox.popup = simplepopup;
    simplepopup->level=1;

    AddList(simplepopup, parent_hwnd, 0,
        item_count, items_visible, selected_pos,
        items, TRUE, subtitle, FALSE);
    AddSeparator(simplepopup, parent_hwnd);
    simplepopup->flag = FILL_X;
    AddPanel(simplepopup, parent_hwnd, PACK_HORIZONTAL);
    simplepopup->flag = ALIGN_X_CENTER | ALIGN_Y_CENTER;
    AddPushButton(simplepopup, parent_hwnd, "   OK   ",
          ID_OK, FALSE, NULL, BUTTON_LEFT, FALSE);
    AddPushButton(simplepopup, parent_hwnd, " CANCEL ",
          ID_CANCEL, FALSE, NULL, BUTTON_RIGHT, FALSE);
    manage_layout(&simplepopup->pinf);
    {
        BOXINFO *bi = simplepopup;

        while (bi) {
            bi->x = bi->pinf.x;
            bi->y = bi->pinf.y;
            bi->cx = bi->pinf.width;
            bi->cy = bi->pinf.height;
            bi->height = bi->pinf.height;
            bi=bi->next;
        }
    }

   BuildDialogBox(_hInst, parent_hwnd, simplepopup, ModalBoxFunc, &result);
   return result;
}

int g_SCREEN_popup_listbox(int win_id, char *title, char *subtitle, int item_count,
            char **items, int select_pos, int items_visible)
{
    int ret_value, status;
    int local_win_id = GET_LOCAL_ID(win_id);
   
    if (WI[local_win_id].listbox.popup)
        return FALSE;
    status = ListBox(local_win_id, title, subtitle, item_count,
             items_visible, items, select_pos - 1);
    if (status == STATUS_OK) {
    BOXINFO *bi;

    bi = WI[local_win_id].listbox.popup;
    ret_value = 1;
    while (bi) {
        if (bi->type == G_CHILD_LISTBOX) {
        ret_value = bi->on + 1;
        break;
        }
        bi = bi->next;
    }
    }
    else
    ret_value = 0;

    BoxinfoFree(WI[local_win_id].listbox.popup);
    WI[local_win_id].listbox.popup = NULL;
    return ret_value;
}

/* --- OK BOX --- */
/*
static void destroy_message_box(int local_win_id)
{
    if (WI[local_win_id].msgbox.popup) 
        XtDestroyWidget(WI[local_win_id].msgbox.popup);  
}

static void init_message_box(int local_win_id)
{
    WI[local_win_id].msgbox.popup = NULL;
}


static void mbox_ok_func(Widget w, XtPointer tag, XtPointer reason)
{
    int local_win_id = (int) tag;
    WI[local_win_id].msgbox.status = G_POPUP_BUTTON1;
}

static void mbox_destroy_func(Widget w, XtPointer tag, XtPointer reason)
{
    int local_win_id = (int) tag;
    WI[local_win_id].msgbox.popup = NULL;
}



void g_SCREEN_popup_messagebox(int win_id, char *title, char *message, int modal)
{
    BOXINFO *modelesspopup;
    HWND parent_hwnd;
    int local_win_id;

    if (hwnd_modeless)
    SendMessage(hwnd_modeless, WM_COMMAND, ID_CANCEL, 0);
    local_win_id = GET_LOCAL_ID(win_id);
    parent_hwnd = WI[local_win_id].child[C_DRAWAREA].hwnd;
    modelesspopup = OpenBox(parent_hwnd,DEFAULT_X, DEFAULT_Y, modal, title);
    WI[local_win_id].msgbox.popup = modelesspopup;

    AddLabel(modelesspopup, parent_hwnd, message, 0,
         FALSE, "", MIDDLE, FALSE);
    simplepopup->flag = FILL_X;
    AddPanel(simplepopup, parent_hwnd, PACK_HORIZONTAL);
    simplepopup->flag = ALIGN_X_CENTER | ALIGN_Y_CENTER;
    AddPushButton(modelesspopup, parent_hwnd, "   OK   ",
          ID_OK, FALSE, NULL, BUTTON_MIDDLE_SINGLE, FALSE);
    if (modal)
    BuildDialogBox(_hInst, parent_hwnd, modelesspopup, ModalBoxFunc);
    else
    BuildDialogBox(_hInst, parent_hwnd, modelesspopup, ModelessBoxFunc);
}
*/

void g_SCREEN_popup_messagebox(int win_id, char *title, char *message, int modal)
{
    HWND parent_hwnd = WI[GET_LOCAL_ID(win_id)].child[C_DRAWAREA].hwnd;

    if (modal)
        MessageBox(parent_hwnd, message, title, MB_OK|MB_APPLMODAL);
    else
        MessageBox(parent_hwnd, message, title, MB_OK);
}




/* --- YES-NO BOX --- */
static int YesNoBox(int local_win_id, char *title, char *message,
            char *button1text, char *button2text)
{
    HWND parent_hwnd;
    BOXINFO *simplepopup;
    int result;

    parent_hwnd = WI[local_win_id].child[C_DRAWAREA].hwnd;
    simplepopup = OpenBox(parent_hwnd,DEFAULT_X, DEFAULT_Y, TRUE, title);
    WI[local_win_id].ynbox.popup = simplepopup;
    simplepopup->level=1;

    AddPanel(simplepopup, parent_hwnd, PACK_VERTICAL);
    simplepopup->border_top    += DIALOGMARGIN_Y;
    simplepopup->border_bottom += DIALOGMARGIN_Y;
    AddLabel(simplepopup, parent_hwnd, message, 0,
         FALSE, "", MIDDLE, FALSE);
    layout_pop(simplepopup);

    AddSeparator(simplepopup,  parent_hwnd);
    simplepopup->flag = FILL_X;
    AddPanel(simplepopup, parent_hwnd, PACK_HORIZONTAL);
    simplepopup->flag = ALIGN_X_CENTER | ALIGN_Y_CENTER;
    AddPushButton(simplepopup, parent_hwnd, button1text,
          ID_OK, FALSE, NULL, BUTTON_LEFT, FALSE);
    AddPushButton(simplepopup, parent_hwnd, button2text,
          ID_CANCEL, FALSE, NULL, BUTTON_RIGHT, FALSE);
    manage_layout(&simplepopup->pinf);
    {
        BOXINFO *bi = simplepopup;

        while (bi) {
            bi->x = bi->pinf.x;
            bi->y = bi->pinf.y;
            bi->cx = bi->pinf.width;
            bi->cy = bi->pinf.height;
            bi->height = bi->pinf.height;
            bi=bi->next;
        }
    }

    BuildDialogBox(_hInst, parent_hwnd, simplepopup, ModalBoxFunc, &result);
    return result;
}

int g_SCREEN_popup_messagebox2(int win_id, char *title, char *message,
            char *button1text, char *button2text)
{
    int status;
    int local_win_id = GET_LOCAL_ID(win_id);

    if (WI[local_win_id].ynbox.popup)
        return FALSE;
    status = YesNoBox(local_win_id, title, message,
                button1text, button2text);
    BoxinfoFree(WI[local_win_id].ynbox.popup);
    WI[local_win_id].ynbox.popup =  NULL;
    if (status == STATUS_OK)
    return G_POPUP_BUTTON1;
    return G_POPUP_BUTTON2;
}

/* --- YES-NO-CANCEL BOX --- */
static int YesNoCancelBox(int local_win_id, char *title, char *message,
         char *button1text, char *button2text, char *button3text)
{
    HWND parent_hwnd;
    BOXINFO *simplepopup;
    int result;

    parent_hwnd = WI[local_win_id].child[C_DRAWAREA].hwnd;
    simplepopup = OpenBox(parent_hwnd,DEFAULT_X, DEFAULT_Y, TRUE, title);
    WI[local_win_id].yncbox.popup = simplepopup;
    simplepopup->level=1;

    AddPanel(simplepopup, parent_hwnd, PACK_VERTICAL);
    simplepopup->border_top    += DIALOGMARGIN_Y;
    simplepopup->border_bottom += DIALOGMARGIN_Y;
    AddLabel(simplepopup, parent_hwnd, message, 0,
         FALSE, "", MIDDLE, FALSE);
    layout_pop(simplepopup);

    AddSeparator(simplepopup,  parent_hwnd);
    simplepopup->flag = FILL_X;
    AddPanel(simplepopup, parent_hwnd, PACK_HORIZONTAL);
    simplepopup->flag = ALIGN_X_CENTER | ALIGN_Y_CENTER;
    AddPushButton(simplepopup, parent_hwnd, button1text,
          ID_OK, FALSE, NULL, BUTTON_LEFT, FALSE);
    AddPushButton(simplepopup, parent_hwnd, button2text,
          ID_NO, FALSE, NULL, BUTTON_MIDDLE, FALSE);
    AddPushButton(simplepopup,  parent_hwnd, button3text,
          ID_CANCEL, FALSE, NULL, BUTTON_RIGHT, FALSE);
    manage_layout(&simplepopup->pinf);
    {
        BOXINFO *bi = simplepopup;

        while (bi) {
            bi->x = bi->pinf.x;
            bi->y = bi->pinf.y;
            bi->cx = bi->pinf.width;
            bi->cy = bi->pinf.height;
            bi->height = bi->pinf.height;
            bi=bi->next;
        }
    }

    BuildDialogBox(_hInst, parent_hwnd, simplepopup, ModalBoxFunc, &result);
    return result;
}

int g_SCREEN_popup_messagebox3(int win_id, char *title, char *message,
         char *button1text, char *button2text, char *button3text)
{
    int status;
    int local_win_id = GET_LOCAL_ID(win_id);

    if (WI[local_win_id].yncbox.popup)
        return FALSE;
    status = YesNoCancelBox(local_win_id, title, message,
                button1text, button2text, button3text);
    BoxinfoFree(WI[local_win_id].yncbox.popup);
    WI[local_win_id].yncbox.popup = NULL;
    if (status == STATUS_OK)
    return G_POPUP_BUTTON1;
    if (status == STATUS_NO)
    return G_POPUP_BUTTON2;
    return G_POPUP_BUTTON3;
}


/* --- Prompt Dialog Box --- */
#define PROMPT_STRING_LEN    256
static int PromptBox(int local_win_id, char *title, char *question,
             char *text, int length)
{
    HWND parent_hwnd;
    BOXINFO *simplepopup;
    int result;

    parent_hwnd = WI[local_win_id].child[C_DRAWAREA].hwnd;
    simplepopup = OpenBox(parent_hwnd, DEFAULT_X, DEFAULT_Y, TRUE, title);
    WI[local_win_id].prmbox.popup = simplepopup;
    simplepopup->level=1;
    simplepopup->flag = ALIGN_X_CENTER ;

    AddPanel(simplepopup, parent_hwnd, PACK_VERTICAL);
    simplepopup->border_top    += PROMPTMARGIN_Y;
    simplepopup->border_bottom += PROMPTMARGIN_Y;
    AddLabel(simplepopup, parent_hwnd, question, 0,
         FALSE, NULL, LEFT, FALSE);
    AddEdit(simplepopup, parent_hwnd, FALSE, text, 0,
        PROMPT_STRING_LEN, max(strlen(text), length),
        FALSE, NULL, FALSE, FALSE);
    layout_pop(simplepopup);


    AddSeparator(simplepopup, parent_hwnd);
    simplepopup->flag = FILL_X;
    AddPanel(simplepopup, parent_hwnd, PACK_HORIZONTAL);
    simplepopup->flag = ALIGN_X_CENTER | ALIGN_Y_CENTER;
    AddPushButton(simplepopup, parent_hwnd, "   OK   ",
          ID_OK, FALSE, NULL, BUTTON_LEFT, FALSE);
    AddPushButton(simplepopup, parent_hwnd, " CANCEL ",
          ID_CANCEL, FALSE, NULL, BUTTON_RIGHT, FALSE);
    manage_layout(&simplepopup->pinf);
    {
        BOXINFO *bi = simplepopup;

        while (bi) {
            bi->x = bi->pinf.x;
            bi->y = bi->pinf.y;
            bi->cx = bi->pinf.width;
            bi->cy = bi->pinf.height;
            bi->height = bi->pinf.height;
            bi=bi->next;
        }
    }
    BuildDialogBox(_hInst, parent_hwnd, simplepopup, ModalBoxFunc, &result);
    return result;
}

char *g_SCREEN_popup_promptdialog(int win_id, char *title, char *question, char *text)
{
    int status,len;
    int local_win_id = GET_LOCAL_ID(win_id);
    char *retval;

    if (WI[local_win_id].prmbox.popup)
        return FALSE;

    retval = NULL;
    len = max(30, strlen(question));
    len = max(len, strlen(text));
    status = PromptBox(local_win_id, title, question, text, len);
    if (status == STATUS_OK) {
        BOXINFO *bi;

        bi = WI[local_win_id].prmbox.popup;
        while (bi) {
            if (bi->type == G_CHILD_TEXTBOX) {
        
                add_label(bi->label);
                retval = get_label();
                    break;
            }
            bi = bi->next;
        }
    }
    BoxinfoFree(WI[local_win_id].prmbox.popup);
    WI[local_win_id].prmbox.popup = NULL;
    if (status == STATUS_OK) 
        return (char*) retval; 
    return NULL;
}

/******************************/
void INTERNAL_set_print_job_abort_flag();

static LRESULT APIENTRY CancelBoxFunc(HWND hwnd, UINT message,
                    WPARAM wParam, LPARAM lParam)
{
    switch (message) {

       case WM_INITDIALOG: {
        BOXINFO *bi_parent = (BOXINFO *) lParam;
        InitBoxChildren(hwnd, bi_parent);
           BoxinfoFree(bi_parent);
    }
    return TRUE;

       case WM_COMMAND:     /* message: received a command */ 
 
            /* User pressed "Cancel" button--stop print job. */ 
           // MessageBox(hwnd, "Incoming", "WM_COMMAND", MB_OK); 
            INTERNAL_set_print_job_abort_flag();
          //  okPrint = FALSE; 
            return TRUE; 
    }
    return FALSE;
}


HWND INTERNAL_popup_cancelbox(HWND parent_hwnd, char *title, char *message)
{
    HWND hwndDlg;
    BOXINFO *simplepopup;
    int result;

    simplepopup = OpenBox(parent_hwnd, DEFAULT_X, DEFAULT_Y, FALSE, title);
    simplepopup->level=1;

    AddLabel(simplepopup, parent_hwnd, message, 0,
              FALSE, "", MIDDLE, FALSE);
    AddSeparator(simplepopup,  parent_hwnd);
    simplepopup->flag = FILL_X;
    AddPanel(simplepopup, parent_hwnd, PACK_HORIZONTAL);
    simplepopup->flag = ALIGN_X_CENTER | ALIGN_Y_CENTER;
    AddPushButton(simplepopup, parent_hwnd, "Cancel",
          ID_CANCEL, FALSE, NULL, BUTTON_LEFT, FALSE);
    manage_layout(&simplepopup->pinf);
    {
        BOXINFO *bi = simplepopup;

        while (bi) {
            bi->x = bi->pinf.x;
            bi->y = bi->pinf.y;
            bi->cx = bi->pinf.width;
            bi->cy = bi->pinf.height;
            bi->height = bi->pinf.height;
            bi=bi->next;
        }
    }

    hwndDlg = BuildDialogBox(_hInst, parent_hwnd, simplepopup, 
                                        CancelBoxFunc, &result);
    return hwndDlg;
}

void g_popup_follow_cursor(int doit)
{
    popup_follow_cursor = doit;
}

/**********************************************/
/* --- Prompt Dialog Box --- */
#define PROMPT_STRING_LEN    256
static int DirBox(int local_win_id, char *title, char *question,
             char *text, int length)
{
    HWND parent_hwnd;
    BOXINFO *simplepopup;
    int result;
#ifdef ggg
   typedef struct _WIN32_FIND_DATA { // wfd  
    DWORD dwFileAttributes; 
    FILETIME ftCreationTime; 
    FILETIME ftLastAccessTime; 
    FILETIME ftLastWriteTime; 
    DWORD    nFileSizeHigh; 
    DWORD    nFileSizeLow; 
    DWORD    dwReserved0; 
    DWORD    dwReserved1; 
    TCHAR    cFileName[ MAX_PATH ]; 
    TCHAR    cAlternateFileName[ 14 ]; 
} WIN32_FIND_DATA; 
#endif
    HANDLE hfile;
    char FileName[MAX_PATH];	// pointer to name of file to search for  
    WIN32_FIND_DATA FindFileData; 	// pointer to returned information 
    strcpy(FileName,text);
    strcat(FileName,"\\*.*");
    hfile = FindFirstFile(FileName,&FindFileData);
    do {
        if (FindFileData.dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY)
            g_cons_printf("%s\\%s\n",text,FindFileData.cFileName);
    } while (FindNextFile(hfile,&FindFileData));
    FindClose(hfile);

    parent_hwnd = WI[local_win_id].child[C_DRAWAREA].hwnd;
    simplepopup = OpenBox(parent_hwnd, DEFAULT_X, DEFAULT_Y, TRUE, title);
    WI[local_win_id].prmbox.popup = simplepopup;
    simplepopup->level=1;
    simplepopup->flag = ALIGN_X_CENTER ;

    AddLabel(simplepopup, parent_hwnd, question, 0,
         FALSE, NULL, MIDDLE, FALSE);
//    AddEdit(simplepopup, parent_hwnd, FALSE, text, 0,
  //      PROMPT_STRING_LEN, max(strlen(text), length),
    //    FALSE, NULL, FALSE, FALSE);

    AddDirList(simplepopup, parent_hwnd, 
            0, length, 10,
            TRUE, NULL, FALSE);
    AddSeparator(simplepopup, parent_hwnd);
    simplepopup->flag = FILL_X;
    AddPanel(simplepopup, parent_hwnd, PACK_HORIZONTAL);
    simplepopup->flag = ALIGN_X_CENTER | ALIGN_Y_CENTER;
    AddPushButton(simplepopup, parent_hwnd, "   OK   ",
          ID_OK, FALSE, NULL, BUTTON_LEFT, FALSE);
    AddPushButton(simplepopup, parent_hwnd, " CANCEL ",
          ID_CANCEL, FALSE, NULL, BUTTON_RIGHT, FALSE);
    manage_layout(&simplepopup->pinf);
    {
        BOXINFO *bi = simplepopup;

        while (bi) {
            bi->x = bi->pinf.x;
            bi->y = bi->pinf.y;
            bi->cx = bi->pinf.width;
            bi->cy = bi->pinf.height;
            bi->height = bi->pinf.height;
            bi=bi->next;
        }
    }
    BuildDialogBox(_hInst, parent_hwnd, simplepopup, ModalBoxFunc, &result);
    return result;
}

char *g_SCREEN_popup_getdirname2(int win_id, char *title)
{
    int status;
    static char init_dir[MAX_PATH+1],*p;
    int local_win_id = GET_LOCAL_ID(win_id);
    char *retval;
    DWORD cchCurDir=MAX_PATH; 
    LPTSTR lpszCurDir; 

    lpszCurDir = init_dir;
    if (GetCurrentDirectory(cchCurDir, lpszCurDir)==0)
        return FALSE; 

    if (WI[local_win_id].prmbox.popup)
        return FALSE;

    retval = NULL;
    p = getcwd(init_dir, MAXDIRLEN);
    if (p==NULL) 
        init_dir[0] = '\0';
    status = DirBox(local_win_id, title, "Change Directory", init_dir, 60);
    if (status == STATUS_OK) {
        BOXINFO *bi;

        bi = WI[local_win_id].prmbox.popup;
        while (bi) {
           // if (bi->type == G_CHILD_TEXTBOX) {        
           //     add_label(bi->label);
           //     retval = get_label();
                 //   break;
           // }
            if (bi->type == G_CHILD_DIRLIST) {  
                DWORD cchCurDir=MAX_PATH; 
                LPTSTR lpszCurDir; 

                lpszCurDir = bi->label;
                GetCurrentDirectory(cchCurDir, lpszCurDir); 
                add_label(bi->label);
                retval = get_label();
/*
GetFullPathName(

    LPCTSTR lpFileName,	// address of name of file to find path for 
    DWORD nBufferLength,	// size, in characters, of path buffer 
    LPTSTR lpBuffer,	// address of path buffer 
    LPTSTR *lpFilePart 	// address of filename in path 
   );
*/
                break;
            }      
            bi = bi->next;
        }
    }
    lpszCurDir = init_dir;
    SetCurrentDirectory(lpszCurDir);
    BoxinfoFree(WI[local_win_id].prmbox.popup);
    WI[local_win_id].prmbox.popup = NULL;
    if (status == STATUS_OK) 
        return (char*) retval; 
    return NULL;
}


/* General popup */

void INTERNAL_reset_popup(int id)
{

    WI[id].checkbox.popup = NULL;
    WI[id].radiobox.popup = NULL;
    WI[id].listbox.popup = NULL;

    WI[id].ynbox.popup = NULL;
    WI[id].yncbox.popup = NULL;
    WI[id].prmbox.popup = NULL;
   
    init_container(id);
}

void INTERNAL_init_popup(void)
{
    int i;

    InitCommonControls();

    init_labelqueue();
    for (i=0;i<G_MAXWINDOW;i++) 
        INTERNAL_reset_popup(i);
}




  
