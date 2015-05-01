/************************************************************************/
/*                               genwscr.c                              */
/*                                                                      */
/*  Platform : Microsoft Windows                                        */
/*  Module   : Genplot User Interface functions                         */
/*                                                                      */
/*  Albert van Kuik                                                     */
/************************************************************************/

#include <stdio.h>
#include <windows.h>
#include <string.h>
#include <stdarg.h>

#ifdef DEBUG
#include "mshell.h"
#endif

#include "genplot.h"
#include "g_inter.h"
#include "genwin.h"

/***********************************************************************/
/*   SCROLLBARS
*/

int INTERNAL_ScrollBarsPresent(int id)
{
    return (WI[id].scroll);
}

int INTERNAL_GetScrollPosH(int id)
{
    SCROLLINFO sinf;
 
    sinf.cbSize = sizeof(sinf);
    sinf.fMask = SIF_POS;
    GetScrollInfo(WI[id].child[C_DRAWAREA].hwnd,
		 SB_HORZ, &sinf);
    return sinf.nPos;
}

int INTERNAL_GetScrollPosV(int id)
{
    SCROLLINFO sinf;
 
    sinf.cbSize = sizeof(sinf);
    sinf.fMask  = SIF_POS;
    GetScrollInfo(WI[id].child[C_DRAWAREA].hwnd,
		  SB_VERT, &sinf);
    return sinf.nPos;
}

void INTERNAL_SetScrollPosH(int id, int pos, int size, int notify)
{
    SCROLLINFO sinf;

    notify = TRUE;
    sinf.cbSize = sizeof(sinf);
    sinf.nPos   = pos;
    sinf.nPage  = size;
    sinf.fMask  = SIF_POS|SIF_PAGE;
    SetScrollInfo(WI[id].child[C_DRAWAREA].hwnd,
		  SB_HORZ,&sinf,notify);
}

void INTERNAL_SetScrollPosV(int id, int pos, int size, int notify)
{
    SCROLLINFO sinf;

    notify = TRUE;
    sinf.cbSize = sizeof(sinf);
    sinf.nPos   = pos;
    sinf.nPage  = size;
    sinf.fMask  = SIF_POS|SIF_PAGE;
    SetScrollInfo(WI[id].child[C_DRAWAREA].hwnd,
		  SB_VERT,&sinf,notify);
}


/*
 * returns 0 for fixed-size slider
 */
int INTERNAL_SetScrollSlider(int slider)
{
    return slider;
}

