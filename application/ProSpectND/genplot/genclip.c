/************************************************************************/
/*                               genclip.c                              */
/*                                                                      */
/*  Platform : All                                                      */
/*  Module   : Soft clipping functions                                  */
/*                                                                      */
/*  Albert van Kuik                                                     */
/************************************************************************/
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#ifdef DEBUG
#include "mshell.h"
#endif
#define  G_LOCAL
#include "genplot.h"
#include "g_inter.h"

static void iswap(int *x, int *y)
{
    int z; z = *x; *x = *y; *y = z;
}

/*****************************************************************************/
/*                               LINECLIP.C                                  */
/*  An inproved implementation of the Cohen-Sutherland line clipping         */
/*  algorithm.                                                               */
/*  Victor J. Duvanenko. Dr Dobb's Journal (1990) July, 98.                  */
/*****************************************************************************/
#ifndef TRUE
#define TRUE   1
#define FALSE  0
#endif
#define OK     0
#define ERROR  -1
#define ACCEPT TRUE
#define REJECT FALSE

static float  clip_y_bottom;
static float  clip_y_top;
static float  clip_x_right;
static float  clip_x_left;

static float  page_clip_y_bottom;
static float  page_clip_y_top;
static float  page_clip_x_right;
static float  page_clip_x_left;

static float  clip_y_bottom__;
static float  clip_y_top__;
static float  clip_x_right__;
static float  clip_x_left__;


#define OUTCODES_CSD( X, Y, OUTCODE)            \
    ( OUTCODE = 0 );                            \
    if ((Y) > clip_y_top__)            OUTCODE |= 1;   \
    else if ((Y) < clip_y_bottom__ )   OUTCODE |= 2;   \
    if ((X) > clip_x_right__ )         OUTCODE |= 4;   \
    else if ((X) < clip_x_left__ )     OUTCODE |= 8;

#define SWAP_PTS_CSD( X0, Y0, OUTCODE0, X1, Y1, OUTCODE1)       \
    tmp = *X0;                                                  \
    *X0 = *X1;                                                  \
    *X1 = tmp;                                                  \
    tmp = *Y0;                                                  \
    *Y0 = *Y1;                                                  \
    *Y1 = tmp;                                                  \
    tmp_i    = OUTCODE0;                                        \
    OUTCODE0 = OUTCODE1;                                        \
    OUTCODE1 = tmp_i; 

int INTERNAL_clip(float *x0,float *y0,float *x1,float *y1)
{
  unsigned tmp_i, outcode0, outcode1;
  float tmp,dx,dy;
  int swp, count=0;

  OUTCODES_CSD(*x0,*y0,outcode0);
  OUTCODES_CSD(*x1,*y1,outcode1);
  if (outcode0 & outcode1)
    return(REJECT);                     /* Trival reject */
  if (!(outcode0 | outcode1)) {
    return(ACCEPT);                     /* Trival accept */
  }
  dx = *x1 - *x0;
  dy = *y1 - *y0;
  swp = FALSE;
  /* somtimes truncation causes a loop (Albert), count added */
  while (count++ < 4) {
    if (!outcode0) {
      SWAP_PTS_CSD(x0,y0,outcode0,x1,y1,outcode1);
      (swp) ? (swp = FALSE) : (swp = TRUE);
    }
    if (outcode0 & 1) {
      *x0 += dx * (clip_y_top__ - *y0)/dy;
      *y0  = clip_y_top__;
      outcode0 = 0;
      if (*x0 > clip_x_right__ )       outcode0 |= 4;
      else if (*x0 < clip_x_left__)    outcode0 |= 8;
    }
    else if (outcode0 & 2) {
      *x0 += dx * (clip_y_bottom__ - *y0)/dy;
      *y0  = clip_y_bottom__;
      outcode0 = 0;
      if (*x0 > clip_x_right__ )       outcode0 |= 4;
      else if (*x0 < clip_x_left__)    outcode0 |= 8;
    }
    else if (outcode0 & 4) {
      *y0 += dy * (clip_x_right__ - *x0)/dx;
      *x0  = clip_x_right__;
      outcode0 = 0;
      if (*y0 > clip_y_top__ )         outcode0 |= 1;
      else if (*y0 < clip_y_bottom__)  outcode0 |= 2;
    }
    else if (outcode0 & 8) {
      *y0 += dy * (clip_x_left__ - *x0)/dx;
      *x0  = clip_x_left__;
      outcode0 = 0;
      if (*y0 > clip_y_top__ )         outcode0 |= 1;
      else if (*y0 < clip_y_bottom__)  outcode0 |= 2;
    }
    if (outcode0 & outcode1) {
      if (swp) {
          SWAP_PTS_CSD(x0,y0,outcode0,x1,y1,outcode1);
      }
      return(REJECT);
    }
    if (!(outcode0 | outcode1)) {
      if (swp) {
          SWAP_PTS_CSD(x0,y0,outcode0,x1,y1,outcode1);
      }
      return(ACCEPT);
    }
  }
      if (swp) 
          SWAP_PTS_CSD(x0,y0,outcode0,x1,y1,outcode1);
  return ACCEPT;
}

void INTERNAL_set_clip(STORETYPE *store)
{
    if (GD.isupsidedown) {
        clip_y_top     = round(WINHEIGHT - store->vy1);
        clip_y_bottom  = round(WINHEIGHT - store->vy1 - store->vymax);
        clip_x_right   = round(store->vx1 + store->vxmax);
        clip_x_left    = round(store->vx1);
    }
    else {
        clip_y_bottom  = round(store->vy1);
        clip_y_top     = round(store->vy1 + store->vymax);
        clip_x_right   = round(store->vx1 + store->vxmax);
        clip_x_left    = round(store->vx1);
    }
    page_clip_y_bottom  = 0.0;
    page_clip_y_top     = WINHEIGHT;
    page_clip_x_right   = WINWIDTH;
    page_clip_x_left    = 0.0;
}

void INTERNAL_get_clip(int *x1, int *y1, int *x2, int *y2)
{
    *x1 = (int) clip_x_left;
    *y1 = (int) clip_y_bottom;
    *x2 = (int) clip_x_right;
    *y2 = (int) clip_y_top;
}

void INTERNAL_set_page_clip(int clip)
{
    if (clip) {
        clip_y_bottom__ = clip_y_bottom;
        clip_y_top__    = clip_y_top;
        clip_x_right__  = clip_x_right;
        clip_x_left__   = clip_x_left;
    }
    else {
        clip_y_bottom__ = page_clip_y_bottom;
        clip_y_top__    = page_clip_y_top;
        clip_x_right__  = page_clip_x_right;
        clip_x_left__   = page_clip_x_left;
    }
}

int INTERNAL_box_outside_clip_area(int clip, int x1, int y1, int x2, int y2)
{
    if (clip) {
        if (x1 > (int) clip_x_right ||
            x2 < (int) clip_x_left  ||
            y1 > (int) clip_y_top   ||
            y2 < (int) clip_y_bottom)
                return TRUE;
    }
    else {
        if (x1 > (int) page_clip_x_right ||
            x2 < (int) page_clip_x_left  ||
            y1 > (int) page_clip_y_top   ||
            y2 < (int) page_clip_y_bottom)
                return TRUE;
    }
    return FALSE;
}


