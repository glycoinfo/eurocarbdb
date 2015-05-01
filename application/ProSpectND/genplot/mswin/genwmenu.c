/************************************************************************/
/*                               genwmenu.c                             */
/*                                                                      */
/*  Platform : Microsoft Windows                                        */
/*  Module   : Genplot menu functions                                   */
/*                                                                      */
/*  Albert van Kuik                                                     */
/************************************************************************/
#include <stdio.h>
#include <windows.h>
#include <commctrl.h>
#include <string.h>
#include <stdarg.h>
#include <stdlib.h>
#include <assert.h>
#include <math.h>
#include <time.h>

#ifdef DEBUG
#include "mshell.h"
#endif

#include "genplot.h"
#include "g_inter.h"
#include "genwin.h"


#define IS_NOPARENT 	1
#define IS_POPUP    	2
#define IS_MENUBAR  	3
#define IS_MENUBAR2  	4
#define IS_BUTTONBAR	5
#define IS_BUTTONBAR2	6
#define IS_BUTTONBAR3	7
#define IS_BUTTONBAR4	8
#define IS_DESTROY  	9

#define IS_PUSHBUTTON	10
#define IS_TOGGLE	11
#define IS_PIXBUTTON	12
#define IS_PIXTOGGLE	14
#define IS_SEPARATOR	15
#define IS_EDIT		16
#define IS_LABEL	17
#define IS_FRAME	18
#define IS_SPACE	19
#define IS_MENUBUTTON	20

static void reformat_line(void);
static int compute_scroll_y(void);
static int compute_scroll_x(void);
static void init_menu(void);
static void exit_menu(void);
static void init_store_cursor(void);
void INTERNAL_init_scroll(void);
void INTERNAL_exit_scroll(void);
void INTERNAL_init_commandqueue(void);
void INTERNAL_exit_commandqueue(void);
static void init_labelqueue(void);
static void exit_labelqueue(void);

static void StripLabel(char *label)
{
    char *p, *q;

    for (p=q=label;*p;p++)
	if (*p != '&')
	    *q++ = *p;
    *q = '\0';
}

#define MAX_SYMBOLS 256
static int GetButtonStringLenght(HWND hWnd, char *string)
{
    HDC hdc;
    static int init, strbuf[MAX_SYMBOLS];
    int i, ret_val;

    if (!init) {
	HWND cbhwnd = CreateWindow("button", "", WS_CHILD | BS_PUSHBUTTON,
				   8, 2, 0, 0, hWnd, (HMENU) -1,
			  (HINSTANCE) GetWindowLong(hWnd, GWL_HINSTANCE),
				   NULL);
	hdc = GetDC(cbhwnd);
	GetCharWidth(hdc, (UINT) 0, (UINT) MAX_SYMBOLS - 1, (int FAR *) &strbuf);
	ReleaseDC(cbhwnd, hdc);
	DestroyWindow(cbhwnd);
	init = TRUE;
    }
    for (i = 0, ret_val = 0; string[i]; i++)
	ret_val += strbuf[string[i]];
    return ret_val;
}
/*
   static void GetLabelSize(HWND hWnd, SIZE *size, char *label)
   {
   HDC hdc;

   hdc = GetDC(hWnd);
   GetTextExtentPoint(hdc, label, strlen(label), size);
   ReleaseDC(hWnd,hdc);
   }
 */
void INTERNAL_init_screen_stuff(void)
{
 //   InitCommonControls();
    init_menu();
    INTERNAL_init_scroll();
    INTERNAL_init_commandqueue();
    init_labelqueue();
}

void INTERNAL_exit_screen_stuff(void)
{
    exit_menu();
    INTERNAL_exit_scroll();
    INTERNAL_exit_commandqueue();
    exit_labelqueue();
}

/****************************************************************
*  Menu's
*/

/* --- MENU BUTTON LABEL  QUEUE --- */
#define BUTTONQMAX 100
static char *button_queue[BUTTONQMAX + 1];
static int q_buttonhead, q_buttontail;

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

#define MAX_IBUTTON	4

typedef struct tagBBUTTONINFO {
    int return_id;
    int nbitmap;
    int ibitmap[MAX_IBUTTON];
    struct tagBBUTTONINFO *bbi_next;
} BBUTTONINFO;

BBUTTONINFO *new_bbuttoninfo(int return_id)
{
    BBUTTONINFO *bbut;
    int i;

    bbut = (BBUTTONINFO*)malloc(sizeof(BBUTTONINFO));
    assert(bbut);
    for (i=0;i<MAX_IBUTTON;i++)
        bbut->ibitmap[i] = 0;
    bbut->return_id = return_id;
    bbut->nbitmap = 0;
    bbut->bbi_next = NULL;
    return bbut;
}

BBUTTONINFO *del_bbuttoninfo(BBUTTONINFO *bbut, int return_id)
{
    BBUTTONINFO *tmp1, *tmp2;

    if (bbut == NULL)
        return NULL;
    tmp1 = bbut;
    if (bbut->return_id == return_id){
        bbut = bbut->bbi_next;
        free(tmp1);
        return bbut;
    }
    while (tmp1->bbi_next) {
        tmp2 = tmp1;
        tmp1 = tmp1->bbi_next;
        if (tmp1->return_id == return_id) {
            tmp2->bbi_next = tmp1->bbi_next;
            free(tmp1);
            break;
        }
    }
    return bbut;
}

void add_ibutton(BBUTTONINFO *bbut, int ibutton)
{
    if (bbut->nbitmap < MAX_IBUTTON) {
        bbut->ibitmap[bbut->nbitmap] = ibutton;
        bbut->nbitmap++;
    }
}

int find_ibutton(BBUTTONINFO *bbut, int return_id, int ipos)
{
    while (bbut) {
        if (bbut->return_id == return_id) {
            if (ipos >=0 && ipos < bbut->nbitmap)
                return bbut->ibitmap[ipos];
            break;
        }
        bbut = bbut->bbi_next;
    }
    return -1;
}




/********************
* button block 2 can have 3 rows.
* It consists, in fact, of three independent bars.
* Thus we must remember the position (=row) of each button.
*/
typedef struct tagBBUTTONPOSITION {
    int return_id;
    int position;
    struct tagBBUTTONPOSITION *bpos_next;
} BBUTTONPOSITION;

BBUTTONPOSITION *new_buttonposition(int return_id, int position)
{
    BBUTTONPOSITION *bpos;
    int i;

    bpos = (BBUTTONPOSITION*)malloc(sizeof(BBUTTONPOSITION));
    assert(bpos);
    bpos->return_id = return_id;
    bpos->position = position;
    bpos->bpos_next = NULL;
    return bpos;
}

BBUTTONPOSITION *del_buttonposition(BBUTTONPOSITION *bpos, int return_id)
{
    BBUTTONPOSITION *tmp1, *tmp2;

    if (bpos == NULL)
        return NULL;
    tmp1 = bpos;
    if (bpos->return_id == return_id){
        bpos = bpos->bpos_next;
        free(tmp1);
        return bpos;
    }
    while (tmp1->bpos_next) {
        tmp2 = tmp1;
        tmp1 = tmp1->bpos_next;
        if (tmp1->return_id == return_id) {
            tmp2->bpos_next = tmp1->bpos_next;
            free(tmp1);
            break;
        }
    }
    return bpos;
}


int find_buttonposition(BBUTTONPOSITION *bpos, int return_id)
{
    while (bpos) {
        if (bpos->return_id == return_id)
            return bpos->position;
        bpos = bpos->bpos_next;
    }
    return -1;
}


/******************
* Menu's
*/
#define PIXBORDER	3
#define PIXMARGIN_Y	2
#define NO_POSITION 	-1

#define MAXMENU 300

typedef struct tagMENUTYPE {
    HWND hwnd;
    HWND hbar[3];
    HMENU hmenu;
    int win_id;
    int isparent;
    int isfirstchild;
    int ispopuphandler;
    int menu_type;
    int bar_position;
    int parent_id;
    int button_type;
    int isgroup;
    BBUTTONINFO *bbi_next;
    BBUTTONPOSITION *bpos_next;
    int return_id;
    HMENU pmenu;
    int position;
} MENUTYPE;

static int menutype_count;
static MENUTYPE *MT;

static void ResetMT(int id)
{
    int i;

    MT[id].hwnd = NULL;
    MT[id].hbar[0] = NULL;
    MT[id].hbar[1] = NULL;
    MT[id].hbar[2] = NULL;
    MT[id].hmenu = NULL;
    MT[id].win_id = G_ERROR;
    MT[id].isparent = FALSE;
    MT[id].isfirstchild = FALSE;
    MT[id].ispopuphandler = FALSE;
    MT[id].parent_id = 0;
    MT[id].menu_type = 0;
    MT[id].bar_position = 0;
    MT[id].button_type = 0;
    MT[id].isgroup = FALSE;
    MT[id].bbi_next = NULL;
    MT[id].bpos_next = NULL;
    MT[id].button_type = 0;
    MT[id].return_id = G_ERROR;
    MT[id].position = NO_POSITION;
}

static void alloc_menu(void)
{
    int i, start = menutype_count;

    menutype_count += MAXMENU;
    if ((MT = (MENUTYPE *)
	 realloc(MT, menutype_count * sizeof(MENUTYPE))) == NULL) {
	fprintf(stderr, "Memory allocation error\n");
	exit(1);
    }
    for (i = start; i < menutype_count; i++)
	ResetMT(i);
}

static void init_menu(void)
{
    menutype_count = 0;
    MT = NULL;
    alloc_menu();
}

static void exit_menu(void)
{
    int i;

    for (i = 0; i < menutype_count; i++)
	if (MT[i].hmenu != NULL) {

	    if (MT[i].menu_type == IS_POPUP)
		DestroyMenu(MT[i].hmenu);
	    ResetMT(i);
	}
    free(MT);
    MT = NULL;
}

void INTERNAL_kill_menu(int win_id)
{
    int i;

    for (i = 0; i < menutype_count; i++)
	if (MT[i].win_id == win_id) {
	    if (MT[i].menu_type == IS_POPUP)
		DestroyMenu(MT[i].hmenu);
	    ResetMT(i);
	}
}

/*
 * Get the first unused menutype structure or
 * allocate a new one
 */
static int menu_next_item(void)
{
    int i;

    for (i = 0; i < menutype_count; i++)
	if (MT[i].hmenu == NULL && MT[i].hwnd == NULL && MT[i].button_type == 0)
	    break;
    if (i == menutype_count)
	alloc_menu();
    ResetMT(i);
    return i;
}

static int get_button_box_menu_id(int local_win_id)
{
    int i;

    for (i = 0; i < menutype_count; i++)
        if (MT[i].menu_type == IS_BUTTONBAR2 && 
               MT[i].win_id == local_win_id)
	    return i;
    return G_ERROR;
}

void INTERNAL_toggle_checkmark(int id)
{
    int i;

    for (i = 0; i < menutype_count; i++)
	if (MT[i].button_type == IS_TOGGLE && MT[i].return_id == id + 1) {
	    unsigned int state;
	    HMENU hmenu = MT[MT[i].parent_id].hmenu;

	    state = GetMenuState(hmenu,
				 id,
				 MF_BYCOMMAND);
	    if (state & MF_CHECKED)
		CheckMenuItem(hmenu, id, MF_UNCHECKED);
	    else
		CheckMenuItem(hmenu, id, MF_CHECKED);
	    return;
	}
}


static void buttenbar2stuff(HWND hWnd, int curr_win, int position, int height)
{
    int i,y0,height2,height3,tbh;
    RECT rc, rctb, rccl;
    UINT flag;
 
    GetClientRect(GetParent(WI[curr_win].child[position].hwnd), &rc);
    y0 = rc.bottom;
    GetWindowRect( WI[curr_win].child[position].hwnd,&rctb);
    tbh = rctb.bottom-rctb.top;

    y0 -= tbh; 
    for (i=C_COMMANDLINE;i>position;i--) {
        if (WI[curr_win].child[i].hwnd){
            GetWindowRect( WI[curr_win].child[i].hwnd,&rccl);
            y0 -= rccl.bottom-rccl.top;
        }   
    }  

    flag = SWP_NOZORDER;
    if (!WI[curr_win].doing_toolbar_stuff)
        flag |= SWP_SHOWWINDOW;
    SetWindowPos( WI[curr_win].child[position].hwnd,
                 NULL, 0, 
                 y0, rc.right - rc.left +1, tbh,flag) ;
}

#define NUM_BUTTON_BITMAPS 1
#define NUM_BUTTONS 1

static void add_bar_button(int menu_id, HINSTANCE g_hinst, int return_id, int on,
                HBITMAP hBitmap, HBITMAP hBitmap2,
                HBITMAP hBitmap3, HBITMAP hBitmap4, 
                 char *label, int style, int width, int height)
{
    TBADDBITMAP tbab; 
    TBBUTTON tbb[3]; 
    char szBuf[16]; 
    int i,id_label,id_bitmap,id_sbitmap; 
    int position, is_visible, local_win_id, rows;
    HWND hwndTB;
    RECT rc;
    BBUTTONINFO *bbut;
    BBUTTONPOSITION *bpos;

    switch (MT[menu_id].menu_type) {
        case IS_BUTTONBAR:
            position = C_BUTTONBAR1;
            break;
        case IS_BUTTONBAR2:
            if (MT[menu_id].bar_position == 1)
                position = C_BUTTONBAR3;
            else if (MT[menu_id].bar_position == 2)
                position = C_BUTTONBAR4;
            else
                position = C_BUTTONBAR2;
                
            break;
//        case IS_BUTTONBAR3:
//            position = C_BUTTONBAR3;
//            break;
//        case IS_BUTTONBAR4:
//            position = C_BUTTONBAR4;
//            break;
        default:
            return;
    } 
    local_win_id = MT[menu_id].win_id; 
    if (WI[local_win_id].child[position].init == 0) 
        is_visible = FALSE;
    else
        is_visible = TRUE;
    WI[local_win_id].child[position].init = 1;
    rows = WI[local_win_id].child[position].rows;
    if (position == C_BUTTONBAR3)
        hwndTB = MT[menu_id].hbar[1];
    else if (position == C_BUTTONBAR4)
        hwndTB = MT[menu_id].hbar[2];
    else
        hwndTB = MT[menu_id].hwnd;
    bbut = new_bbuttoninfo(return_id);
    if (MT[menu_id].bbi_next == NULL)
        MT[menu_id].bbi_next = bbut;
    else {
        BBUTTONINFO *tmp;
        tmp = MT[menu_id].bbi_next;
        while (tmp->bbi_next)
            tmp = tmp->bbi_next;
        tmp->bbi_next = bbut;
    }
    bpos = new_buttonposition(return_id, MT[menu_id].bar_position);
    if (MT[menu_id].bpos_next == NULL)
        MT[menu_id].bpos_next = bpos;
    else {
        BBUTTONPOSITION *tmp;
        tmp = MT[menu_id].bpos_next;
        while (tmp->bpos_next)
            tmp = tmp->bpos_next;
        tmp->bpos_next = bpos;
    }

    // Send the TB_BUTTONSTRUCTSIZE message, which is required for 
    // backward compatibility. 
    SendMessage(hwndTB, TB_BUTTONSTRUCTSIZE, 
        (WPARAM) sizeof(TBBUTTON), 0); 
 
    // Add the bitmap containing button images to the toolbar. 
   id_bitmap =-1;
   if (hBitmap != NULL) {
        if (!is_visible) {
            SendMessage(hwndTB, 
                        TB_SETBITMAPSIZE, (WPARAM) 0, 
                        (LPARAM) MAKELONG(width, height)); 
        }
        tbab.hInst = NULL; 
        tbab.nID   = (UINT) hBitmap; 
        id_bitmap = SendMessage(hwndTB, TB_ADDBITMAP, 
                      (WPARAM) NUM_BUTTON_BITMAPS, 
                      (WPARAM) &tbab);
        add_ibutton(bbut, id_bitmap);
        if (hBitmap2 != NULL) {
            tbab.nID   = (UINT) hBitmap2; 
            id_sbitmap = SendMessage(hwndTB, TB_ADDBITMAP, 
                      (WPARAM) NUM_BUTTON_BITMAPS, 
                      (WPARAM) &tbab);
            add_ibutton(bbut, id_sbitmap);
        }
        if (hBitmap3 != NULL) {
            tbab.nID   = (UINT) hBitmap3; 
            id_sbitmap = SendMessage(hwndTB, TB_ADDBITMAP, 
                      (WPARAM) NUM_BUTTON_BITMAPS, 
                      (WPARAM) &tbab);
            add_ibutton(bbut, id_sbitmap);
        }
        if (hBitmap4 != NULL) {
            tbab.nID   = (UINT) hBitmap3; 
            id_sbitmap = SendMessage(hwndTB, TB_ADDBITMAP, 
                      (WPARAM) NUM_BUTTON_BITMAPS, 
                      (WPARAM) &tbab);
            add_ibutton(bbut, id_sbitmap);
        }
    }
    // Add the button strings to the toolbar. 
    //   LoadString(g_hinst, IDS_CUT, (LPSTR) &szBuf, MAX_LEN); 
    id_label = -1; 
    if (label != NULL)
        id_label = SendMessage(hwndTB, TB_ADDSTRING, 
                         0, (LPARAM) (LPSTR) label); 

    i=0;
    // Fill the TBBUTTON array with button information, and add the 
    // buttons to the toolbar. 
 
    if (style & TBSTYLE_SEP) {
        tbb[i].iBitmap = 0; 
        tbb[i].idCommand = 0; 
        tbb[i].fsState = TBSTATE_ENABLED; 
        tbb[i].fsStyle = style; 
        tbb[i].dwData  = 0; 
        tbb[i].iString = 0;
    }
    else {
        int tbstate = TBSTATE_ENABLED;
        if (on)
            tbstate |= TBSTATE_CHECKED;

        tbb[i].iBitmap   = id_bitmap; 
        tbb[i].idCommand = return_id; 
        tbb[i].fsState = tbstate; 
        tbb[i].fsStyle = style; 
        tbb[i].dwData  = 0; 
        tbb[i].iString = id_label; 
     
    }
    SendMessage(hwndTB, TB_ADDBUTTONS, (WPARAM) 1, 
        (LPARAM) (LPTBBUTTON) &tbb); 

    if (!is_visible){
        HDWP hdwp;
        int height;
        int ibutton = 0;
        UINT flag;


        SendMessage(hwndTB, TB_GETITEMRECT, 
            (WPARAM) ibutton,
            (LPARAM) (LPRECT) &rc); 

        height = rc.bottom + rc.top +
	      GetSystemMetrics(SM_CYBORDER);

        GetWindowRect(WI[local_win_id].hparent, &rc);

        MoveWindow(WI[local_win_id].hparent,
	       rc.left,
	       rc.top,
	       rc.right - rc.left,
	       rc.bottom - rc.top + height,
	       TRUE);

        WI[local_win_id].child[position].height = height;
 
        GetClientRect(WI[local_win_id].hparent, &rc);
        flag = SWP_NOZORDER;
        if (!WI[local_win_id].doing_toolbar_stuff)
            flag |= SWP_SHOWWINDOW;

        if (position == C_BUTTONBAR1)
            SetWindowPos(hwndTB,
                NULL, 0, 0, rc.right - rc.left, height, 
                  flag ) ;
        else 
            SetWindowPos(hwndTB ,
                NULL, 0, rc.bottom - rc.top , rc.right - rc.left , height, 
                    flag) ;

        if (position > C_BUTTONBAR1)
            buttenbar2stuff(WI[local_win_id].hparent, 
                                 local_win_id, position,height);
        else
            SendMessage(WI[local_win_id].child[C_BUTTONBAR1].hwnd, 
                       TB_AUTOSIZE, 0,0);
    }

}


static int create_toolbar(int win_id, int position, int rows)
{
    int i, local_win_id, menu_id, y1, y2, at_top, at_bottom, height;
    RECT rc;
    DWORD ws;
    HWND hwndParent, hwnd;

    local_win_id = GET_LOCAL_ID(win_id);
    if (local_win_id < 0 || local_win_id >= G_MAXWINDOW)
	return G_ERROR;
    if (WI[local_win_id].child[position].hwnd)
	return G_ERROR;
        
    if (position == C_BUTTONBAR3 || position == C_BUTTONBAR4) {
        if ((menu_id = get_button_box_menu_id(local_win_id)) == G_ERROR)
	    return G_ERROR;
    }
    else if ((menu_id = menu_next_item()) == G_ERROR)
	return G_ERROR;


    hwndParent = WI[local_win_id].hparent;
    ws = WS_CHILD |  WS_VISIBLE | CCS_NOMOVEY | TBSTYLE_TOOLTIPS;
    if (position == WS_BORDER)
        ws |= CCS_NOPARENTALIGN;
    if (position > C_BUTTONBAR1)
        ws |= CCS_NOPARENTALIGN;
    if (position > C_BUTTONBAR2)
        ws |= CCS_NODIVIDER;

//    MT[menu_id].hwnd = CreateToolbarEx(hwndParent, 
      hwnd = CreateToolbarEx(hwndParent, 
                   ws,
                  menu_id,
                   0,
                  _hInst,
                   0,
                  NULL,
                  0, 0, 0, 0, 0, 0);

//    WI[local_win_id].child[position].hwnd   = MT[menu_id].hwnd;
    WI[local_win_id].child[position].hwnd   = hwnd;
    WI[local_win_id].child[position].height = 0;
    WI[local_win_id].child[position].init   = 0;
    WI[local_win_id].child[position].rows   = rows;

    MT[menu_id].win_id = local_win_id;
    MT[menu_id].isparent = TRUE;
    switch (position) {
    case C_MENUBAR2 :
	MT[menu_id].menu_type = IS_MENUBAR2;
        MT[menu_id].hwnd = hwnd;
        MT[menu_id].hbar[0] = hwnd;
	break;
    case C_BUTTONBAR2 :
	MT[menu_id].menu_type = IS_BUTTONBAR2;
        MT[menu_id].hwnd = hwnd;
        MT[menu_id].hbar[0] = hwnd;
        MT[menu_id].bar_position = 0;
	break;
    case C_BUTTONBAR3 :
//	MT[menu_id].menu_type = IS_BUTTONBAR3;
	MT[menu_id].menu_type = IS_BUTTONBAR2;
        MT[menu_id].hbar[1] = hwnd;
        MT[menu_id].bar_position = 1;
	break;
    case C_BUTTONBAR4 :
//	MT[menu_id].menu_type = IS_BUTTONBAR4;
	MT[menu_id].menu_type = IS_BUTTONBAR2;
        MT[menu_id].hbar[2] = hwnd;
        MT[menu_id].bar_position = 2;
	break;
    default :
	MT[menu_id].menu_type = IS_BUTTONBAR;
        MT[menu_id].hwnd = hwnd;
        MT[menu_id].hbar[0] = hwnd;
	break;
    }

    return menu_id;
}


static void DestroyButtonBar(int local_win_id, int position)
{
    int i,height;
    RECT rc;

    if (!WI[local_win_id].child[position].hwnd)
	return;
    GetWindowRect(WI[local_win_id].hparent, &rc);
    height = WI[local_win_id].child[position].height;
    for (i = 0; i < menutype_count; i++)
	if (MT[i].hwnd == WI[local_win_id].child[position].hwnd) {
	    if (MT[i].menu_type == IS_MENUBAR2)
		DestroyMenu(MT[i].hmenu);
	    ResetMT(i);
	    break;
	}
    DestroyWindow(WI[local_win_id].child[position].hwnd);
    WI[local_win_id].child[position].hwnd = NULL;
    WI[local_win_id].child[position].height = 0;
    MoveWindow(WI[local_win_id].hparent,
	       rc.left,
	       rc.top,
	       rc.right - rc.left,
	       rc.bottom - rc.top - height,
	       TRUE);
    GetClientRect(WI[local_win_id].hparent, &rc);
    for (i = position+1; i < MAXWCHILDREN; i++)
	if (WI[local_win_id].child[i].hwnd)
	    SendMessage(WI[local_win_id].child[i].hwnd, WM_USER, ID_RESIZE,
			(((long) (rc.bottom - rc.top + 1)) << 16) +
			 ((long) (rc.right - rc.left + 1)));
}

/*--------------------------------------*/
void g_SCREEN_menu_destroy_popup(int win_id)
{
    int i, local_win_id;

    local_win_id = GET_LOCAL_ID(win_id);
    if (local_win_id < 0 || local_win_id >= G_MAXWINDOW)
	return;
    WI[local_win_id].hmenu = (HMENU) 0;

    for (i = 0; i < menutype_count; i++)
	if (MT[i].win_id == local_win_id
	    && MT[i].menu_type == IS_POPUP) {
	    if (MT[i].isparent)
		DestroyMenu(MT[i].hmenu);
	    ResetMT(i);
	}
}

int g_SCREEN_menu_create_menubar(int win_id)
{
    int local_win_id, id, menu_height;
    RECT rc;

    local_win_id = GET_LOCAL_ID(win_id);
    if (local_win_id < 0 || local_win_id >= G_MAXWINDOW)
	return G_ERROR;
    if ((id = menu_next_item()) == G_ERROR)
	return G_ERROR;
    MT[id].hmenu = CreateMenu();
    SetMenu(WI[local_win_id].hparent, MT[id].hmenu);
    MT[id].win_id = local_win_id;
    MT[id].isparent = TRUE;
    MT[id].isfirstchild = FALSE;
    MT[id].ispopuphandler = FALSE;
    MT[id].menu_type = IS_MENUBAR;

    GetWindowRect(WI[local_win_id].hparent, &rc);

    if (!WI[local_win_id].isvisible) {
        ShowWindow(WI[local_win_id].hparent, SW_SHOW);
	  UpdateWindow(WI[local_win_id].hparent);
	  WI[local_win_id].isvisible = TRUE;
    }
    GetClientRect(WI[local_win_id].hparent, &rc);
    SendMessage(WI[local_win_id].child[C_DRAWAREA].hwnd, WM_USER, ID_RESIZE,
			(((long) (rc.bottom - rc.top + 1)) << 16) + 
                      ((long) (rc.right - rc.left + 1)));

    return id;
}

int g_SCREEN_menu_create_menubar2(int win_id)
{
    return G_ERROR; // create_toolbar(win_id, C_MENUBAR2, 1);
}

int g_SCREEN_menu_create_popup(int win_id)
{
    int local_win_id, id;

    local_win_id = GET_LOCAL_ID(win_id);
    if (local_win_id < 0 || local_win_id >= G_MAXWINDOW)
	return G_ERROR;
    if ((id = menu_next_item()) == G_ERROR)
	return G_ERROR;
    MT[id].hmenu = CreatePopupMenu();
    MT[id].win_id = local_win_id;
    MT[id].isparent = TRUE;
    MT[id].isfirstchild = FALSE;
    MT[id].ispopuphandler = TRUE;
    MT[id].menu_type = IS_POPUP;
    WI[local_win_id].hmenu = MT[id].hmenu;
    return id;
}

int g_SCREEN_menu_append_submenu(int menu_id, char *label)
{
    int id;

    if (menu_id < 0 || menu_id >= menutype_count)
	return G_ERROR;
    if (MT[menu_id].menu_type != IS_MENUBAR2 && MT[menu_id].hmenu == NULL)
	return G_ERROR;
    if ((id = menu_next_item()) == G_ERROR)
	return G_ERROR;
    MT[id].win_id = MT[menu_id].win_id;
    MT[id].menu_type = MT[menu_id].menu_type;
    MT[id].ispopuphandler = FALSE;
    MT[id].hmenu = CreatePopupMenu();
    if (MT[menu_id].isparent &&
	(MT[menu_id].menu_type == IS_MENUBAR ||
	 MT[menu_id].menu_type == IS_POPUP ||
	 MT[menu_id].menu_type == IS_MENUBAR2))
	MT[id].isfirstchild = TRUE;
    else
	MT[id].isfirstchild = FALSE;
/*
    if (MT[menu_id].menu_type == IS_MENUBAR2) {
	AddMenuButton(MT[menu_id].hwnd, id, label);
	return id;
    }
*/
    AppendMenu(MT[menu_id].hmenu, MF_POPUP, (UINT) MT[id].hmenu, label);
    if (MT[menu_id].menu_type == IS_MENUBAR) {
	int local_win_id = MT[menu_id].win_id;
	HWND hWnd = WI[local_win_id].hparent;
	DrawMenuBar(hWnd);
    }
    MT[id].position = GetMenuItemCount(MT[menu_id].hmenu) - 1;
    MT[id].pmenu = MT[menu_id].hmenu;

    return id;
}



int g_SCREEN_menu_append_button(int menu_id, char *label, int return_id)
{
    int id, style;

    if (menu_id < 0 || menu_id >= menutype_count)
	return G_ERROR;
    if ((id = menu_next_item()) == G_ERROR)
	return G_ERROR;
    MT[id].win_id      = MT[menu_id].win_id;
    MT[id].menu_type   = MT[menu_id].menu_type;
    MT[id].button_type = IS_PUSHBUTTON;
    MT[id].parent_id   = menu_id;
    MT[id].return_id   = return_id;
    switch (MT[menu_id].menu_type) {
	case IS_MENUBAR:
	case IS_MENUBAR2:
	case IS_POPUP:
	    if (MT[menu_id].hmenu == NULL)
		return G_ERROR;
	    AppendMenu(MT[menu_id].hmenu, MF_STRING, return_id + 1, label);
	    return G_OK;
	case IS_BUTTONBAR:
	case IS_BUTTONBAR2:
//	case IS_BUTTONBAR3:
//	case IS_BUTTONBAR4:
	    if (MT[menu_id].hwnd == NULL)
		return G_ERROR;
            style = TBSTYLE_BUTTON;
            if (MT[menu_id].isgroup)
                style |= TBSTYLE_GROUP;
            add_bar_button(menu_id, _hInst, return_id + 1, 0,
                      NULL, NULL, NULL,NULL,label, style,0,0);
	    return G_OK;
    }
    return G_ERROR;
}

int g_SCREEN_menu_append_toggle(int menu_id, char *label, int return_id, int on)
{
    int id,style;

    if (menu_id < 0 || menu_id >= menutype_count)
	return G_ERROR;
    if ((id = menu_next_item()) == G_ERROR)
	return G_ERROR;
    MT[id].win_id      = MT[menu_id].win_id;
    MT[id].menu_type   = MT[menu_id].menu_type;
    MT[id].button_type = IS_TOGGLE;
    MT[id].parent_id   = menu_id;
    MT[id].return_id   = return_id;
    switch (MT[menu_id].menu_type) {
	case IS_MENUBAR:
	case IS_MENUBAR2:
	case IS_POPUP:
	    if (MT[menu_id].hmenu == NULL)
		return G_ERROR;
	    AppendMenu(MT[menu_id].hmenu, MF_STRING, return_id + 1, label);
	    if (on)
		CheckMenuItem(MT[menu_id].hmenu, return_id + 1, MF_CHECKED);
	     return G_OK;
	case IS_BUTTONBAR:
	case IS_BUTTONBAR2:
//	case IS_BUTTONBAR3:
//	case IS_BUTTONBAR4:
            if (MT[menu_id].hwnd == NULL)
		   return G_ERROR;
            style = TBSTYLE_CHECK;
            if (MT[menu_id].isgroup)
               style |= TBSTYLE_GROUP;
            add_bar_button(menu_id, _hInst, return_id + 1, on,
                       NULL, NULL, NULL,NULL,label, style,0,0);
            return G_OK;
    }
    return G_ERROR;
}


int g_SCREEN_menu_append_separator(int menu_id)
{
    int style;

    if (menu_id < 0 || menu_id >= menutype_count)
	return G_ERROR;
    switch (MT[menu_id].menu_type) {
	case IS_MENUBAR:
	case IS_MENUBAR2:
	case IS_POPUP:
	    if (MT[menu_id].hmenu == NULL)
		return G_ERROR;
	    AppendMenu(MT[menu_id].hmenu, MF_SEPARATOR, 0, NULL);
	    return G_OK;
	case IS_BUTTONBAR:
	case IS_BUTTONBAR2:
//	case IS_BUTTONBAR3:
//	case IS_BUTTONBAR4:
            if (MT[menu_id].hwnd == NULL)
		   return G_ERROR;
            style = TBSTYLE_SEP;
            if (MT[menu_id].isgroup)
                style |= TBSTYLE_GROUP;
            add_bar_button(menu_id, _hInst, 0, 0,
                     NULL,NULL, NULL, NULL, NULL, style,0,0);
            return G_OK;
    }
    return G_ERROR;
}

void g_SCREEN_menu_enable_item(int menu_id, int item_id, int enable)
{
    HMENU hMenu;
    UINT uMenuItem, uMenuFlags;
    HWND hwnd;
    int pos;

    if (menu_id < 0 || menu_id >= menutype_count)
	return;
    switch (MT[menu_id].menu_type) {
	case IS_MENUBAR:
	case IS_MENUBAR2:
	case IS_POPUP:
	    if (MT[menu_id].hmenu == NULL)
		return;
	    if (item_id == G_MENU_ALLITEMS) {
		if (MT[menu_id].position == NO_POSITION)
		    return;
		uMenuItem = MT[menu_id].position;
		uMenuFlags = MF_BYPOSITION;
		hMenu = MT[menu_id].pmenu;
	    }
	    else {
		uMenuItem = item_id + 1;
		uMenuFlags = MF_BYCOMMAND;
		hMenu = MT[menu_id].hmenu;
	    }
	    if (enable)
		uMenuFlags |= MF_ENABLED;
	    else
		uMenuFlags |= MF_DISABLED | MF_GRAYED;
	    EnableMenuItem(hMenu, uMenuItem, uMenuFlags);
	    break;

	case IS_BUTTONBAR:
	case IS_BUTTONBAR2:
//	case IS_BUTTONBAR3:
//	case IS_BUTTONBAR4:
            pos = find_buttonposition(MT[menu_id].bpos_next, item_id+1);
            if (pos >= 0) 
                hwnd = MT[menu_id].hbar[pos];
            else
                hwnd = MT[menu_id].hwnd;
            SendMessage(hwnd, TB_ENABLEBUTTON, 
                             (WPARAM) item_id + 1, MAKELONG(enable,0)); 
	break;
    }
}

int g_SCREEN_menu_is_enabled(int menu_id, int item_id)
{
    int state;
    HWND hwnd;
    int pos;

    if (menu_id < 0 || menu_id >= menutype_count)
	return TRUE;
    if (item_id == G_MENU_ALLITEMS && MT[menu_id].position == NO_POSITION)
	return TRUE;
    switch (MT[menu_id].menu_type) {
	case IS_MENUBAR:
	case IS_MENUBAR2:
	case IS_POPUP:
	    if (MT[menu_id].hmenu == NULL)
		return G_ERROR;
	    state = GetMenuState(MT[menu_id].pmenu, MT[menu_id].position,
			 MF_BYPOSITION);
	    if (item_id == G_MENU_ALLITEMS)
		return !(state & MF_GRAYED);
	    if (state & MF_GRAYED)
		return FALSE;
	    state = GetMenuState(MT[menu_id].hmenu, item_id + 1, MF_BYCOMMAND);
	    return !(state & MF_GRAYED);

	case IS_BUTTONBAR:
	case IS_BUTTONBAR2:
//	case IS_BUTTONBAR3:
//	case IS_BUTTONBAR4:
            pos = find_buttonposition(MT[menu_id].bpos_next, item_id+1);
            if (pos >= 0) 
                hwnd = MT[menu_id].hbar[pos];
            else
                hwnd = MT[menu_id].hwnd;
            return SendMessage(hwnd, TB_ISBUTTONENABLED, 
                             (WPARAM) item_id + 1, 0); 

    }
    return TRUE;
}

void g_SCREEN_menu_set_toggle(int menu_id, int item_id, int check)
{
    HWND hwnd;
    int pos;

    if (menu_id < 0 || menu_id >= menutype_count)
	return;
    switch (MT[menu_id].menu_type) {
	case IS_MENUBAR:
	case IS_MENUBAR2:
	case IS_POPUP:
	    if (MT[menu_id].hmenu == NULL)
		return;
	    if (check)
		CheckMenuItem(MT[menu_id].hmenu, item_id + 1, MF_CHECKED);
	    else
		CheckMenuItem(MT[menu_id].hmenu, item_id + 1, MF_UNCHECKED);
	    break;

	case IS_BUTTONBAR:
	case IS_BUTTONBAR2:
//	case IS_BUTTONBAR3:
//	case IS_BUTTONBAR4:
            pos = find_buttonposition(MT[menu_id].bpos_next, item_id+1);
            if (pos >= 0) 
                hwnd = MT[menu_id].hbar[pos];
            else
                hwnd = MT[menu_id].hwnd;
            SendMessage(hwnd, TB_CHECKBUTTON, 
                             (WPARAM) item_id + 1, MAKELONG(check,0)); 
	    break;
    }
}

int g_SCREEN_menu_get_toggle(int menu_id, int item_id)
{
    HWND hwnd;
    int pos;
    unsigned int state;

    if (menu_id < 0 || menu_id >= menutype_count)
	return TRUE;
    switch (MT[menu_id].menu_type) {
	case IS_MENUBAR:
	case IS_MENUBAR2:
	case IS_POPUP:
	    if (MT[menu_id].hmenu == NULL)
		return TRUE;
	    state = GetMenuState(MT[menu_id].hmenu, item_id + 1, MF_BYCOMMAND);
	    return (state & MF_CHECKED);

	case IS_BUTTONBAR:
	case IS_BUTTONBAR2:
//	case IS_BUTTONBAR3:
//	case IS_BUTTONBAR4:
            pos = find_buttonposition(MT[menu_id].bpos_next, item_id+1);
            if (pos >= 0) 
                hwnd = MT[menu_id].hbar[pos];
            else
                hwnd = MT[menu_id].hwnd;
            return SendMessage(hwnd, TB_ISBUTTONCHECKED, 
                             (WPARAM) item_id + 1, 0); 
    }
    return TRUE;
}


int g_SCREEN_menu_create_buttonbar(int win_id)
{
    return create_toolbar(win_id, C_BUTTONBAR1, 1);
}


int g_SCREEN_menu_create_buttonbar2(int win_id)
{
    return create_toolbar(win_id, C_BUTTONBAR2, 1);
}

int g_SCREEN_menu_create_buttonbox2(int win_id, int rows)
{
    int local_win_id = GET_LOCAL_ID(win_id);

    // Special coding, initializing button block, first row
    if (rows > 0) {
        if (WI[local_win_id].doing_toolbar_stuff)
            return -1;
        WI[local_win_id].doing_toolbar_stuff = 1;
        WI[local_win_id].noresize=1;
        return create_toolbar(win_id, C_BUTTONBAR2, 1);
    }
    // Special coding, end of button block
    else if (rows == -1) {
        RECT rc;
        if (WI[local_win_id].doing_toolbar_stuff==0)
            return -1;
        WI[local_win_id].doing_toolbar_stuff = 0;
        WI[local_win_id].noresize=0;
        GetClientRect(WI[local_win_id].hparent, &rc);
        SendMessage(WI[local_win_id].hparent, WM_USER, ID_RESIZE,
                MAKELPARAM(rc.right - rc.left + 1,rc.bottom - rc.top + 1));
        return -1;
    }
    // next row
    else if (rows == 0) {
        if (WI[local_win_id].doing_toolbar_stuff <= 0 || 
            WI[local_win_id].doing_toolbar_stuff >= 3)
                return -1;
        WI[local_win_id].doing_toolbar_stuff++;
        if (WI[local_win_id].doing_toolbar_stuff == 3)
            return create_toolbar(win_id, C_BUTTONBAR4, 1);
        if (WI[local_win_id].doing_toolbar_stuff == 2)
            return create_toolbar(win_id, C_BUTTONBAR3, 1);
        return create_toolbar(win_id, C_BUTTONBAR2, 1);
    }
    return -1;
}

void g_SCREEN_menu_destroy_menubar_children(int win_id)
{
    int i, local_win_id, count;
    HMENU hmenu = 0;

    local_win_id = GET_LOCAL_ID(win_id);
    if (local_win_id < 0 || local_win_id >= G_MAXWINDOW)
	return;
    if ((hmenu = GetMenu(WI[local_win_id].hparent))== NULL)
	return;
    if ((count = GetMenuItemCount(hmenu)) == 0)
	return;
    for (i=0;i<count;i++)
	DeleteMenu(hmenu, 0, MF_BYPOSITION);
    for (i = 0; i < menutype_count; i++)
	if (MT[i].menu_type == IS_MENUBAR && MT[i].win_id == local_win_id) {
	    if (MT[i].hmenu != NULL && !MT[i].isparent)
		ResetMT(i);
	    if (MT[i].button_type)
		ResetMT(i);
	}
    DrawMenuBar(WI[local_win_id].hparent);
}

// We do not have a second menu bar
void g_SCREEN_menu_destroy_menubar2_children(int win_id)
{
    int i;

    int local_win_id = GET_LOCAL_ID(win_id);
    if (local_win_id < 0 || local_win_id >= G_MAXWINDOW)
	return;
    for (i = 0; i < menutype_count; i++)
	if (MT[i].menu_type == IS_MENUBAR2 &&
	    MT[i].win_id    == local_win_id &&
	    MT[i].button_type)
		ResetMT(i);
}

// We do not have a second menu bar
void g_SCREEN_menu_destroy_menubar2(int win_id)
{
    int i;

    int local_win_id = GET_LOCAL_ID(win_id);
    if (local_win_id < 0 || local_win_id >= G_MAXWINDOW)
	return;
    DestroyButtonBar(local_win_id, C_MENUBAR2);
    for (i = 0; i < menutype_count; i++)
	if (MT[i].menu_type == IS_MENUBAR2 &&
	    MT[i].win_id    == local_win_id &&
	    MT[i].button_type)
		ResetMT(i);
}

//
//
// MOET NOG
//
//
static void destroy_buttonbar(int win_id, int position, int type)
{
    int i;

    int local_win_id = GET_LOCAL_ID(win_id);
    if (local_win_id < 0 || local_win_id >= G_MAXWINDOW)
	return;
    DestroyButtonBar(local_win_id, position);
    for (i = 0; i < menutype_count; i++)
	if (MT[i].menu_type == type &&
	    MT[i].win_id == local_win_id &&
	    MT[i].button_type)
		ResetMT(i);
}

void g_SCREEN_menu_destroy_buttonbar(int win_id)
{
    destroy_buttonbar(win_id, C_BUTTONBAR1, IS_BUTTONBAR);
}

void g_SCREEN_menu_destroy_buttonbar2(int win_id)
{
    destroy_buttonbar(win_id, C_BUTTONBAR2, IS_BUTTONBAR2);
}


static void destroy_buttonbar_children(int win_id, int position, int type)
{
    int i;

    int local_win_id = GET_LOCAL_ID(win_id);
    if (local_win_id < 0 || local_win_id >= G_MAXWINDOW)
	return;
    i = SendMessage(WI[local_win_id].child[position].hwnd,
                    TB_BUTTONCOUNT, 0, 0); 
    while (i > 0) {
        SendMessage(WI[local_win_id].child[position].hwnd, 
                    TB_DELETEBUTTON, i-1, 0); 
        i--;
    }

    for (i = 0; i < menutype_count; i++)
	if (MT[i].menu_type == type &&
	    MT[i].win_id == local_win_id &&
	    MT[i].button_type)
		ResetMT(i);
}

void g_SCREEN_menu_destroy_buttonbar_children(int win_id)
{
    destroy_buttonbar_children(win_id, C_BUTTONBAR1, IS_BUTTONBAR);
}

void g_SCREEN_menu_destroy_buttonbar2_children(int win_id)
{
    destroy_buttonbar_children(win_id, C_BUTTONBAR2, IS_BUTTONBAR2);
}


void g_SCREEN_menu_destroy_button(int menu_id, int item_id)
{
    int i, pos;
    HWND hwnd;

    if (menu_id < 0 || menu_id >= menutype_count)
	return;
    switch (MT[menu_id].menu_type) {
	case IS_MENUBAR:
	case IS_MENUBAR2:
	case IS_POPUP:
	    if (MT[menu_id].hmenu == NULL)
		return;
	    DeleteMenu(MT[menu_id].hmenu, item_id + 1, MF_BYCOMMAND);
	    break;
	case IS_BUTTONBAR:
	case IS_BUTTONBAR2:
//	case IS_BUTTONBAR3:
//	case IS_BUTTONBAR4:
            pos = find_buttonposition(MT[menu_id].bpos_next, item_id+1);
            if (pos >= 0) 
                hwnd = MT[menu_id].hbar[pos];
            else
                hwnd = MT[menu_id].hwnd;
            // Can not destroy bitmaps or text
            MT[menu_id].bbi_next  = del_bbuttoninfo(MT[menu_id].bbi_next, item_id+1);
            MT[menu_id].bpos_next = del_buttonposition(MT[menu_id].bpos_next, item_id+1);
            i = SendMessage(hwnd, TB_COMMANDTOINDEX, 
                             (WPARAM) item_id + 1, 0); 
            SendMessage(hwnd, TB_DELETEBUTTON, i, 0); 
	    break;
    }
    for (i = 0; i < menutype_count; i++)
	if (MT[i].parent_id == menu_id && MT[i].return_id == item_id)
	    ResetMT(i);
}


void g_SCREEN_menu_select_popup(int menu_id)
{
    int i;

    if (menu_id < 0 ||
	menu_id >= menutype_count ||
	MT[menu_id].hmenu == NULL)
	return;

    for (i = 0; i < menutype_count; i++)
	if (MT[i].hmenu != NULL && MT[i].win_id == MT[menu_id].win_id &&
	    MT[i].ispopuphandler && MT[i].isparent) {
	    MT[i].ispopuphandler = FALSE;
	    break;
	}
    WI[MT[menu_id].win_id].hmenu = MT[menu_id].hmenu;
    MT[menu_id].ispopuphandler = TRUE;
}


void g_SCREEN_menu_hide_popup(int win_id)
{
    int i, local_win_id;

    local_win_id = GET_LOCAL_ID(win_id);
    if (local_win_id < 0 || local_win_id >= G_MAXWINDOW)
	return;
    for (i = 0; i < menutype_count; i++)
	if (MT[i].hmenu != NULL && MT[i].win_id == local_win_id &&
	    MT[i].ispopuphandler && MT[i].isparent) {
	    MT[i].ispopuphandler = FALSE;
	    break;
	}
    WI[local_win_id].hmenu = (HMENU) 0;
}

/********************************************************************/
#include "color_lookup.h"

#define UNSPECIFIED_PIXMAP ((HBITMAP) 0)

typedef struct pixcachetype {
    char **xpm;
    HBITMAP pix;
    struct pixcachetype *next;
} PIXCACHE;

static PIXCACHE *pixcache;

static PIXCACHE *new_pixcache(void)
{
    PIXCACHE *pc;

    pc = (PIXCACHE *) malloc(sizeof(PIXCACHE));
    assert(pc);
    pc->xpm = NULL;
    pc->pix = UNSPECIFIED_PIXMAP;
    pc->next = NULL;
    return pc;
}

static HBITMAP search_pixcache(char **xpm)
{
    PIXCACHE *pc;

    if (pixcache)
	for (pc = pixcache; pc->next != NULL; pc = pc->next)
	    if (pc->xpm == xpm)
		return pc->pix;
    return UNSPECIFIED_PIXMAP;
}

static void add_pixcache(PIXCACHE * pc)
{
    PIXCACHE *pc2;

    if (!pixcache)
	pixcache = pc;
    else {
	for (pc2 = pixcache; pc2->next != NULL; pc2 = pc2->next);
	pc2->next = pc;
    }
}

#define MAXCOLOR 64
#define LINELEN  32

HBITMAP INTERNAL_Compose_Bitmap(HDC hdcLocal, char *xpm[])
{
    int x, xx, y, i, j, width, height, numcolors, cpp, numtrunc, pixel;
    char colorcode[MAXCOLOR];
    char colorstring[MAXCOLOR][LINELEN];
    HANDLE hloc;
    PBITMAPINFO pbmi;
    PIXCACHE *pc;
    HBITMAP hbm;
    BYTE *aBits;
    RGBQUAD *argbq;
    DWORD bcolor;

    if (xpm == NULL)
	return 0;

    if ((hbm = search_pixcache(xpm)) != UNSPECIFIED_PIXMAP)
	return hbm;
    sscanf(xpm[0], "%d %d %d %d", &width, &height, &numcolors, &cpp);

    if (width <= 0 || height <= 0 || numcolors <= 0 || cpp <= 0)
	return 0;

    numtrunc = min(numcolors, MAXCOLOR);
    aBits = (BYTE *) malloc(width * height * sizeof(BYTE));
    assert(aBits);
    for (i = 0; i < numtrunc; i++) {
        /*
         * The first character is the color identifier
         */
        colorcode[i] = xpm[i + 1][0];
        colorstring[i][0] = '\0';
        for (j=1;j<32;j++) {
            if (xpm[i + 1][j] != ' ' &&
                xpm[i + 1][j] != '\t')
                    break;
        }
        /*
         * If this is 'c', a color follows
         */
        if (xpm[i + 1][j] == 'c') {
            int k;
            j++;
            while (xpm[i + 1][j] == ' ' ||
                   xpm[i + 1][j] == '\t')
                       j++;
            for (k=0;k<LINELEN-1;k++,j++){
                if (xpm[i + 1][j] == '\0')
                    break;
                colorstring[i][k] = xpm[i + 1][j];
            }
            colorstring[i][k] = '\0';
        }
	    }

    argbq = (RGBQUAD *) malloc(numtrunc * sizeof(RGBQUAD));
    assert(argbq);
    bcolor = GetSysColor(COLOR_BTNFACE);
    for (i = 0; i < numtrunc; i++) {
        int red, green, blue;

        argbq[i].rgbBlue  = (bcolor >> 16) & 0xff; //192;
        argbq[i].rgbGreen = (bcolor >> 8)  & 0xff; // 192;
        argbq[i].rgbRed   = (bcolor >> 0)  & 0xff; //192;
        argbq[i].rgbReserved = 0;
        if (colorstring[i][0] != '\0') {
            if (colorstring[i][0] == '#') {
                int icol;
                for (icol=0;icol<12;icol++)
                    if (!isxdigit(colorstring[i][icol+1]))
                        break;
                if (icol < 12)
		       sscanf(colorstring[i], "#%2X%2X%2X", &red, &green, &blue);
                else {
		       sscanf(colorstring[i], "#%4X%4X%4X", &red, &green, &blue);
                    red   = red * 0xff / 0xffff;
                    green = green * 0xff / 0xffff;
                    blue  = blue * 0xff / 0xffff;
                }
		   argbq[i].rgbBlue = (BYTE) blue;
		   argbq[i].rgbGreen = (BYTE) green;
		   argbq[i].rgbRed = (BYTE)red;
            }
            else {
                int idef;
                for (idef=0;color_def_lookup[idef].label[0] != '\0';idef++) {
 		      if (stricmp(color_def_lookup[idef].label, colorstring[i]) == 0) {
			  argbq[i].rgbBlue  = color_def_lookup[idef].b;
			  argbq[i].rgbGreen = color_def_lookup[idef].g;
			  argbq[i].rgbRed   = color_def_lookup[idef].r;
			  break;
		      }
                }
           }
	 }
    }
    for (y = 0, i = width * height - 1, pixel = 0; y < height && i >= 0; y++) {
        for (x = 0, xx = width*cpp-1; x < width; x++, xx -= cpp) {
            for (j = 0; j < numtrunc; j++) {
                if (xpm[numcolors + 1 + y][xx] == colorcode[j]) {
                    pixel = j;
                    break;
                }
            }
            aBits[i--] = pixel;
            if (i<0) break;
        }
    }


    hloc = LocalAlloc(LMEM_ZEROINIT | LMEM_MOVEABLE,
		      sizeof(BITMAPINFOHEADER) + (sizeof(RGBQUAD) * 256));
    assert(hloc);
    pbmi = (PBITMAPINFO) LocalLock(hloc);

    pbmi->bmiHeader.biSize = sizeof(BITMAPINFOHEADER);
    pbmi->bmiHeader.biWidth = width;
    pbmi->bmiHeader.biHeight = height;
    pbmi->bmiHeader.biPlanes = 1;

    pbmi->bmiHeader.biBitCount = 8;
    pbmi->bmiHeader.biCompression = BI_RGB;

    pbmi->bmiHeader.biSizeImage = 0; 
    pbmi->bmiHeader.biXPelsPerMeter = 0; 
    pbmi->bmiHeader.biYPelsPerMeter = 0; 
    pbmi->bmiHeader.biClrUsed = numtrunc; 
    pbmi->bmiHeader.biClrImportant = 0; 

    memcpy(pbmi->bmiColors, argbq, sizeof(RGBQUAD) * numtrunc);

    hbm = CreateDIBitmap(hdcLocal, (BITMAPINFOHEADER FAR *) pbmi, CBM_INIT,
			 aBits, pbmi, DIB_RGB_COLORS);
    LocalFree(hloc);
    free(aBits);
    free(argbq);

    pc = new_pixcache();
    pc->xpm = xpm;
    pc->pix = hbm;
    add_pixcache(pc);
    return pc->pix;
}


static int append_pixmap(int menu_id, char *pixinfo[], char *pixgrayinfo[],
			 char *pixselectinfo[], char *pixselectgrayinfo[],
			 int return_id, int toggle, int toggle_on)
{
    int x0, width, height, numcolors, cpp;
    HWND hwndChild;
    HDC hdc;
    HBITMAP info, grayinfo, select, grayselect;
    int style;

    if (menu_id < 0 || menu_id >= menutype_count || MT[menu_id].hwnd == NULL)
	return G_ERROR;
    if (pixinfo == NULL)
	return G_ERROR;
    sscanf(pixinfo[0], "%d %d %d %d", &width, &height, &numcolors, &cpp);
    if (width <= 0 || height <= 0 || numcolors <= 0 || cpp <= 0)
	return G_ERROR;

    hdc = GetDC(MT[menu_id].hwnd);
    info = INTERNAL_Compose_Bitmap(hdc, (char **) pixinfo);
    if (pixgrayinfo == NULL)
	grayinfo = NULL;
    else
	grayinfo = INTERNAL_Compose_Bitmap(hdc, (char **) pixgrayinfo);
    if (pixselectinfo == NULL)
	select = NULL;
    else
	select = INTERNAL_Compose_Bitmap(hdc, (char **) pixselectinfo);
    if (pixselectgrayinfo == NULL)
	grayselect = NULL;
    else
	grayselect = INTERNAL_Compose_Bitmap(hdc, (char **) pixselectgrayinfo);
    ReleaseDC(MT[menu_id].hwnd, hdc);


    if (toggle) 
       style = TBSTYLE_CHECK; 
    else 
       style = TBSTYLE_BUTTON;
    if (MT[menu_id].isgroup)
        style |= TBSTYLE_GROUP;
    
    add_bar_button(menu_id, _hInst, return_id, toggle_on,
               info, grayinfo, select, grayselect, NULL, style, width, height);

    return G_OK;
}

int g_SCREEN_menu_append_pixbutton(int menu_id, char *pixinfo[],
			    char *pixgrayinfo[], int return_id)
{
    if (menu_id < 0 || menu_id >= menutype_count)
	return G_ERROR;
    switch (MT[menu_id].menu_type) {
	case IS_BUTTONBAR:
	case IS_BUTTONBAR2:
//	case IS_BUTTONBAR3:
//	case IS_BUTTONBAR4:
	    return append_pixmap(menu_id, pixinfo, pixgrayinfo, NULL, NULL,
			 return_id + 1, FALSE, FALSE);
    }
    return G_ERROR;
}

int g_SCREEN_menu_append_multipixbutton(int menu_id, char *pixinfo1[],
	 char *pixinfo2[], char *pixinfo3[], char *pixinfo4[], int return_id)
{
    if (menu_id < 0 || menu_id >= menutype_count)
	return G_ERROR;
    switch (MT[menu_id].menu_type) {
	case IS_BUTTONBAR:
	case IS_BUTTONBAR2:
//	case IS_BUTTONBAR3:
//	case IS_BUTTONBAR4:
	    return append_pixmap(menu_id, pixinfo1, pixinfo2, pixinfo3, pixinfo4,
			 return_id + 1, FALSE, FALSE);
    }
    return G_ERROR;
}

int g_SCREEN_menu_append_pixtoggle(int menu_id, char *pixinfo[], char *pixgrayinfo[],
			char *pixselectinfo[], char *pixselectgrayinfo[],
			    int return_id, int toggle_on)
{
    if (menu_id < 0 || menu_id >= menutype_count)
	return G_ERROR;
    switch (MT[menu_id].menu_type) {
	case IS_BUTTONBAR:
	case IS_BUTTONBAR2:
//	case IS_BUTTONBAR3:
//	case IS_BUTTONBAR4:
            if (pixselectinfo)
                return append_pixmap(menu_id, pixinfo, pixgrayinfo, pixselectinfo,
		      pixselectgrayinfo, return_id + 1, FALSE, FALSE);
            else
                return append_pixmap(menu_id, pixinfo, pixgrayinfo, pixselectinfo,
		      pixselectgrayinfo, return_id + 1, TRUE, toggle_on);
    }
    return G_ERROR;
}

int g_SCREEN_menu_set_group(int menu_id, int group_on)
{
    if (menu_id < 0 || menu_id >= menutype_count)
	  return G_ERROR;
    MT[menu_id].isgroup = group_on;
    return G_OK;
}

int g_SCREEN_menu_set_label(int menu_id, int item_id, char *label)
{
    if (menu_id < 0 || menu_id >= menutype_count)
	return G_ERROR;
    switch (MT[menu_id].menu_type) {
	case IS_MENUBAR:
	case IS_MENUBAR2:
	case IS_POPUP:
	    if (MT[menu_id].hmenu == NULL)
		return G_ERROR;
	    ModifyMenu(MT[menu_id].hmenu, item_id + 1,
		       MF_BYCOMMAND | MF_STRING, item_id + 1, label);
	    return G_OK;

	case IS_BUTTONBAR:
	case IS_BUTTONBAR2: {
//	case IS_BUTTONBAR3:
//	case IS_BUTTONBAR4: {
            int ipos = label[0] - '0';
            int ibutton = find_ibutton(MT[menu_id].bbi_next, item_id+1, ipos);
            if (ibutton >= 0) {
                HWND hwnd;
                int pos = find_buttonposition(MT[menu_id].bpos_next, item_id+1);
                if (pos >= 0) 
                    hwnd = MT[menu_id].hbar[pos];
                else
                    hwnd = MT[menu_id].hwnd;
                SendMessage(hwnd, TB_CHANGEBITMAP, 
                             (WPARAM) item_id + 1, 
                             (LPARAM) MAKELPARAM(ibutton, 0));
           }
           }
           return G_OK;
    }
    return G_ERROR;
}


#define M_LABEL_LEN	100
char *g_SCREEN_menu_get_label(int menu_id, int item_id)
{
    char *buf;
    int  len, pos;
    HWND hwnd;

    if (menu_id == G_ERROR) {
	int i;
	for (i=0;i<menutype_count;i++) {
	    if (MT[i].button_type && MT[i].return_id == item_id) {
		menu_id = MT[i].parent_id;
		break;
	    }
	}
    }
    if (menu_id < 0 || menu_id >= menutype_count)
	return NULL;
    switch (MT[menu_id].menu_type) {
	case IS_MENUBAR:
	case IS_MENUBAR2:
	case IS_POPUP:
	    if (MT[menu_id].hmenu == NULL)
		return NULL;
	    buf = (char *) malloc(M_LABEL_LEN+1);
            assert(buf);
	    len = GetMenuString(MT[menu_id].hmenu, item_id + 1,
			  buf, M_LABEL_LEN, MF_BYCOMMAND);
	    buf[len] = '\0';
	    StripLabel(buf);
	    add_label(buf);
	    free(buf);
	    return get_label();

	case IS_BUTTONBAR:
	case IS_BUTTONBAR2:
//	case IS_BUTTONBAR3:
//	case IS_BUTTONBAR4:
	    if (MT[menu_id].hmenu == NULL)
		return NULL;
            pos = find_buttonposition(MT[menu_id].bpos_next, item_id+1);
            if (pos >= 0) 
                hwnd = MT[menu_id].hbar[pos];
            else
                hwnd = MT[menu_id].hwnd;
	
	    buf = (char *) malloc(M_LABEL_LEN+1);
            assert(buf);
            len = SendMessage(hwnd, TB_GETBUTTONTEXT, 
                             (WPARAM) item_id + 1, 
                             (LPARAM) (LPSTR) buf);
	    buf[len] = '\0';
	    StripLabel(buf);
	    add_label(buf);
	    free(buf);
	    return get_label();
    //    return GetButtonBarLabel(MT[menu_id].hwnd, item_id + 1);
    }
    return NULL;
}


int g_SCREEN_menu_set_pixmap(int menu_id, int item_id, int pix_id)
{
    if (menu_id < 0 || menu_id >= menutype_count)
	return G_ERROR;
    switch (MT[menu_id].menu_type) {
	case IS_BUTTONBAR:
	case IS_BUTTONBAR2:{
//	case IS_BUTTONBAR3:
//	case IS_BUTTONBAR4: {
            int ipos = pix_id;
            int ibutton = find_ibutton(MT[menu_id].bbi_next, item_id+1, ipos);
            if (ibutton >= 0) {
                HWND hwnd;
                int pos = find_buttonposition(MT[menu_id].bpos_next, item_id+1);
                if (pos >= 0) 
                    hwnd = MT[menu_id].hbar[pos];
                else
                    hwnd = MT[menu_id].hwnd;
                SendMessage(hwnd, TB_CHANGEBITMAP, 
                             (WPARAM) item_id + 1, 
                             (LPARAM) MAKELPARAM(ibutton, 0));
           }
           }
           return G_OK;
    }
    return G_ERROR;
}



/*************************
 * Not used
 */

int   g_SCREEN_menu_create_floating(int win_id)
{
    return G_ERROR;
}
void  g_SCREEN_menu_destroy_floating(int win_id)
{}
void  g_SCREEN_menu_destroy_floating_children(int win_id)
{}
int   g_SCREEN_menu_append_floating_button(int menu_id, char *label, int return_id,
                            float vp_x, float vp_y)
{
    return G_ERROR;
}
int   g_SCREEN_menu_append_optionmenu(int menu_id, char *label, 
         int return_id, int item_count, int item_select, char *data[])
{
    return G_ERROR;
}



/************************************************/
typedef struct tagTOOLTIPTYPE {
    int id;
    char *tip;
} TOOLTIPTYPE;


static int numtips, maxtips;
static TOOLTIPTYPE *tips;

static int compar(const void *v1, const void *v2)
{
    TOOLTIPTYPE *t1, *t2;
    
    t1 = (TOOLTIPTYPE*) v1;
    t2 = (TOOLTIPTYPE*) v2;
    return (t1->id - t2->id);
}
 

int g_SCREEN_menu_add_tooltip(int return_id, char *tip)
{
    int len;
    if (numtips >= maxtips) {
        maxtips += 20;
        tips = (TOOLTIPTYPE*) realloc(tips, sizeof(TOOLTIPTYPE) * maxtips);
        assert(tips);
    }
    len = strlen(tip)+1;
    len = min(80,len);
    tips[numtips].tip = (char*)malloc((len+1)*sizeof(char));
    assert(tips[numtips].tip);
    strncpy(tips[numtips].tip, tip, len);
    tips[numtips].tip[len] = '\0';
    tips[numtips].id = return_id+1;
    numtips++;
    qsort(tips, numtips, sizeof(TOOLTIPTYPE), compar);
    return G_OK;
}

char *INTERNAL_get_tooltip(int return_id)
{
    TOOLTIPTYPE ttt, *tt;
    char *p = NULL;
    int i;
    ttt.id = return_id;

    if (numtips == 0)
        return NULL; 

    tt = (TOOLTIPTYPE*) bsearch(&ttt, tips, numtips, sizeof(TOOLTIPTYPE), compar);
    if (tt == NULL)
        return NULL;
    return tt->tip;
}

