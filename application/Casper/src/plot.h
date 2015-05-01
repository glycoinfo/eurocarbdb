/* 
This file is part of CASPER.

    CASPER is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    CASPER is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with CASPER.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2010 Roland Stenutz, Magnus Lundborg, Göran Widmalm
*/

/* Filename=plot.h */
/* Prefix=PL */
/* Graphical output to HP-plotter */

#ifndef _PLOT_H
#define _PLOT_H

#include <stdio.h>

/* output 'modes' */
#define PL_MODE_HPGL		0x00
#define PL_MODE_PS		0x01
#define PL_MODE_NONE		0xFF

struct PL_Context {
/* Physical coordinates */
	int lower_x, lower_y;		/* window for ploting */
	int upper_x, upper_y;
/* Logical coordinates */
	float origin_x, origin_y;	/* x,y origin */
	float scale_x, scale_y;		/* x,y scale */
	float range_x, range_y;		/* total x,y range */
	FILE *file;			/* where to write */
	int mode;			/* PS or HP-GL */
	unsigned char pen;			/* Pen/Colour */
	unsigned char symbol;			/* Symbol for x,y plots */
	};


#define PL_XCoord(X)	(PL_Context.scale_x*(X-PL_Context.origin_x)+PL_Context.lower_x)
#define PL_YCoord(Y)	(PL_Context.scale_y*(Y-PL_Context.origin_y)+PL_Context.lower_y)

#define PL_SCL_UP	0x01
#define PL_SCL_DOWN	0x02
#define PL_SCL_RIGHT	PL_SCL_UP
#define PL_SCL_LEFT	PL_SCL_DOWN
#define PL_SCL_L_TO_R	0x10
#define PL_SCL_R_TO_L	0x20
#define PL_SCL_B_TO_T	PL_SCL_L_TO_R
#define PL_SCL_T_TO_B	PL_SCL_R_TO_L

#define PL_PEN_DRAFT		1
#define PL_PEN_BLACK		2
#define PL_PEN_RED		3
#define PL_PEN_BLUE		4
#define PL_PEN_GREEN		5
#define PL_PEN_VIOLET		6
#define PL_PEN_AQUA		7
#define PL_PEN_REDVIOLET	8

#define PL_TOK_SQUARE		65
#define PL_TOK_CIRCLE		66
#define PL_TOK_TRIANGLE		67
#define PL_TOK_PLUS		68
#define PL_TOK_X		69
#define	PL_TOK_DIAMOND		70
#define PL_TOK_STAR		120



void PL_Range(int x, int y, int range_x, int range_y);
void PL_Window(int x, int y, int range_x, int range_y);
int PL_Frame();
int PL_Clip(float x, float y);
void PL_XScale(unsigned char mode,float label, float ticks);
void PL_YScale(unsigned char mode,float label, float ticks);
void PL_Line(float x1, float y1, float x2, float y2);
void PL_Pen(unsigned char color);
void PL_Mark(float x, float y, unsigned char token);
void PL_Text(float x, float y, float size,char *text);

/*************************************/
/* Functions accessable from outside */
/*************************************/

int PL_SetPen();
int PL_Plot();
int PL_SetWindow();
int PL_Scale();
int PL_PlotSpectrum();
int PL_DrawText();

#endif

