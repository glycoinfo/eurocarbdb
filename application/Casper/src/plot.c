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

/* Filename=plot.c */
/* Prefix=PL */
/* Graphical output to HP-plotter */

#include "plot.h"
#include "parser.h"
#include "build.h"
#include <math.h>
#include <ctype.h>
#include <string.h>
#include <strings.h>
#include <stdlib.h>

struct PL_Context PL_Context;

struct PA_Command PL_Cmds[]={
  { "frame",	PL_Frame,        "frame",
    ""},
  { "pen",	PL_SetPen,       "pen <pen>",
    ""},
  { "spectrum",	PL_PlotSpectrum, "spectrum H|C|HH|CH <spec> [<title>]",
    ""},
  { "scale",	PL_Scale,        "scale x|y u=r|d=l <mark>|* <tick>|*",
    ""},
  { "window",	PL_SetWindow,
    "window <physical x-origin> <y> <x-range> <y-range> <logical x-origin> <y> <x-range> <y-range>",
    ""},
  { "text",	PL_DrawText,     "text <x> <y> <sz> <string>",
    ""},
  { NULL,	NULL,            NULL,
    ""}
};

/* set scales */
void PL_Range(int x, int y, int range_x, int range_y)
{
  if ( (range_x==0)||(range_y==0) ) return;
  PL_Context.origin_x=x;
  PL_Context.origin_y=y;
  PL_Context.range_x=range_x;
  PL_Context.range_y=range_y;
  PL_Context.scale_x=(PL_Context.upper_x-PL_Context.lower_x)/range_x;
  PL_Context.scale_y=(PL_Context.upper_y-PL_Context.lower_y)/range_y;
}

/* sets window for plot */
void PL_Window(int x, int y, int range_x, int range_y)
{
  PL_Context.lower_x=x;
  PL_Context.lower_y=y;
  PL_Context.upper_x=x+range_x;
  PL_Context.upper_y=y+range_y;
}

int PL_Frame()
{
  switch (PL_Context.mode)
    {
    case PL_MODE_HPGL:	fprintf(PL_Context.file,"PU;PA%d,%d;",
				PL_Context.lower_x,PL_Context.lower_y);
      fprintf(PL_Context.file,"ER%d,%d;",
	      PL_Context.upper_x-PL_Context.lower_x,
	      PL_Context.upper_y-PL_Context.lower_y);
      break;
    case PL_MODE_PS:	fprintf(PL_Context.file,"%d %d %d %d rectstroke\n",
				PL_Context.lower_x,PL_Context.lower_y,
				PL_Context.upper_x-PL_Context.lower_x,
				PL_Context.upper_y-PL_Context.lower_y);
      break;
    };
  return(PA_ERR_OK);
}

int PL_Clip(float x, float y)
{
  if ((PL_XCoord(x)<PL_Context.lower_x)||(PL_XCoord(x)>PL_Context.upper_x)) return(0);
  if ((PL_YCoord(y)<PL_Context.lower_y)||(PL_YCoord(y)>PL_Context.upper_y)) return(0);
  return(1);
}

void PL_XScale(unsigned char mode,float label, float ticks)
{
  float x,y;
  if (PL_Context.scale_x<0) {label=-label;ticks=-ticks;};
  if (PL_Context.mode==PL_MODE_HPGL)
    {
      fprintf(PL_Context.file,"DT;PU;DI1,0;\n");
      x=PL_Context.origin_x;
      if (mode&PL_SCL_UP)
	{
	  y=PL_Context.range_y+PL_Context.origin_y;
	  fprintf(PL_Context.file,"TL0.3,0;PA%.0f,%.0f;LO14;PD;\n",PL_XCoord(x),PL_YCoord(y));
	}
      else
	{
	  y=PL_Context.origin_y;
	  fprintf(PL_Context.file,"TL0,0.3;PA%.0f,%.0f;LO16;PD;\n",PL_XCoord(x),PL_YCoord(y));
	};
      for (x=ceil(PL_Context.origin_x/ticks)*ticks; PL_Clip(x,y); x+=ticks)
	fprintf(PL_Context.file,"PA%.0f,%.0f;XT;\n",PL_XCoord(x),PL_YCoord(y));
      fprintf(PL_Context.file,"PU;");
      for (x=ceil(PL_Context.origin_x/label)*label; PL_Clip(x,y); x+=label)
	fprintf(PL_Context.file,"PA%.0f,%.0f;LB%.0f\003;\n",PL_XCoord(x),PL_YCoord(y),x);
    };
  if (PL_Context.mode==PL_MODE_PS)
    {
      if (mode&PL_SCL_UP)
	{
	  y=PL_Context.origin_y;
	  fprintf(PL_Context.file,"%d %d moveto %d %d lineto\n", PL_Context.lower_x,
		  PL_Context.upper_y, PL_Context.upper_x, PL_Context.upper_y);
	  for (x=ceil(PL_Context.origin_x/ticks)*ticks; PL_Clip(x,y); x+=ticks)
	    fprintf(PL_Context.file,"%.0f %d moveto 0 15 rlineto\n",PL_XCoord(x),PL_Context.upper_y);
	  for (x=ceil(PL_Context.origin_x/label)*label; PL_Clip(x,y); x+=label)
	    {
	      fprintf(PL_Context.file,"%.0f %d moveto\n",PL_XCoord(x),PL_Context.upper_y);
	      fprintf(PL_Context.file,"(%.0f) dup stringwidth pop -2 div\n",x);
	      fprintf(PL_Context.file,"30 rmoveto show\n");
	    };
	};
      if (mode&PL_SCL_DOWN)
	{
	  y=PL_Context.origin_y;
	  fprintf(PL_Context.file,"%d %d moveto %d %d lineto\n", PL_Context.lower_x,
		  PL_Context.lower_y, PL_Context.upper_x, PL_Context.lower_y);
	  for (x=ceil(PL_Context.origin_x/ticks)*ticks; PL_Clip(x,y); x+=ticks)
	    fprintf(PL_Context.file,"%.0f %d moveto 0 -15 rlineto\n",PL_XCoord(x),PL_Context.lower_y);
	  for (x=ceil(PL_Context.origin_x/label)*label; PL_Clip(x,y); x+=label)
	    {
	      fprintf(PL_Context.file,"%.0f %d moveto\n",PL_XCoord(x),PL_Context.lower_y);
	      fprintf(PL_Context.file,"(%.0f) dup stringwidth pop -2 div\n",x);
	      fprintf(PL_Context.file,"-100 rmoveto show\n");
	    };
	};
    };
}

void PL_YScale(unsigned char mode,float label, float ticks)
{
  float x,y;
  if (PL_Context.scale_x<0) {label=-label;ticks=-ticks;};
  if (PL_Context.mode==PL_MODE_HPGL)
    {
      fprintf(PL_Context.file,"DT;PU;DI0,-1;\n");
      y=PL_Context.origin_y;
      if (mode&PL_SCL_RIGHT)
	{
	  x=PL_Context.range_x+PL_Context.origin_x;
	  fprintf(PL_Context.file,"TL0.3,0;PA%0.f,%0.f;LO14;PD;\n",PL_XCoord(x),PL_YCoord(y));
	}
      else
	{
	  x=PL_Context.origin_x;
	  fprintf(PL_Context.file,"PA%0.f,%0.f;LO14;PD;\n",PL_XCoord(x),PL_YCoord(y));
	};
      for (y=ceil(PL_Context.origin_y/ticks)*ticks; PL_Clip(x,y); y+=ticks)
	fprintf(PL_Context.file,"PA%0.f,%0.f;YT;\n",PL_XCoord(x),PL_YCoord(y));
      fprintf(PL_Context.file,"PU;");
      for (y=ceil(PL_Context.origin_y/label)*label; PL_Clip(x,y); y+=label)
	fprintf(PL_Context.file,"PA%0.f,%0.f;LB%0.f\003;\n",PL_XCoord(x),PL_YCoord(y),y);
    };
  if (PL_Context.mode==PL_MODE_PS)
    {
      if (mode&PL_SCL_RIGHT)
	{
	  x=PL_Context.origin_x;
	  fprintf(PL_Context.file,"%d %d moveto %d %d lineto\n", PL_Context.upper_x,
		  PL_Context.lower_y, PL_Context.upper_x, PL_Context.upper_y);
	  for (y=ceil(PL_Context.origin_y/ticks)*ticks; PL_Clip(x,y); y+=ticks)
	    fprintf(PL_Context.file,"%d %.0f moveto 15 0 rlineto\n",PL_Context.upper_x,PL_YCoord(y));
	  for (y=ceil(PL_Context.origin_y/label)*label; PL_Clip(x,y); y+=label)
	    {
	      fprintf(PL_Context.file,"%d %.0f moveto\n",PL_Context.upper_x,PL_YCoord(y));
	      fprintf(PL_Context.file,"(%.0f) dup stringwidth -2 div exch\n",y);
	      fprintf(PL_Context.file,"pop 30 exch rmoveto show\n");
	    };
	};
      if (mode&PL_SCL_LEFT)
	{
	  x=PL_Context.origin_x;
	  fprintf(PL_Context.file,"%d %d moveto %d %d lineto\n", PL_Context.lower_x,
		  PL_Context.lower_y, PL_Context.lower_x, PL_Context.upper_y);
	  for (y=ceil(PL_Context.origin_y/ticks)*ticks; PL_Clip(x,y); y+=ticks)
	    fprintf(PL_Context.file,"%d %.0f moveto -15 0 rlineto\n",PL_Context.lower_x,PL_YCoord(y));
	  for (y=ceil(PL_Context.origin_y/label)*label; PL_Clip(x,y); y+=label)
	    {
	      fprintf(PL_Context.file,"%d %.0f moveto\n",PL_Context.lower_x,PL_YCoord(y));
	      fprintf(PL_Context.file,"(%.0f) dup stringwidth -2 div exch\n",y);
	      fprintf(PL_Context.file,"pop -100 exch rmoveto show\n");
	    };
	};
    };
}

void PL_Line(float x1, float y1, float x2, float y2)
{
  if ((!PL_Clip(x1,y1))||(!PL_Clip(x2,y2))) return;
  if (PL_Context.mode==PL_MODE_HPGL)
    {
      fprintf(PL_Context.file,"PU;PA%.0f,%.0f;PD;PA%.0f,%.0f;PU;\n", \
	      PL_XCoord(x1),PL_YCoord(y1),PL_XCoord(x2),PL_YCoord(y2));
    };
  if (PL_Context.mode==PL_MODE_PS)
    {
      fprintf(PL_Context.file,"%.0f %.0f moveto %.0f %.0f lineto\n", \
	      PL_XCoord(x1),PL_YCoord(y1),PL_XCoord(x2),PL_YCoord(y2));
    };
}

void PL_Pen(unsigned char color)
{
  if (PL_Context.mode==PL_MODE_HPGL)
    fprintf(PL_Context.file,"SP%d;",color);
}

void PL_Mark(float x, float y, unsigned char token)
{
  if (!PL_Clip(x,y)) return;
  if (PL_Context.mode==PL_MODE_HPGL)
    {
      fprintf(PL_Context.file,"DI;SR0.5,1;LO15;PU;PA%.0f,%.0f;SA;LB%c\003;SS;SR;\n",\
	      PL_XCoord(x),PL_YCoord(y),token);
    };
  if (PL_Context.mode==PL_MODE_PS)
    {
      /* ignores token */
      fprintf(PL_Context.file,"%.0f %.0f moveto\n (x) show\n",PL_XCoord(x),PL_YCoord(y));
    };
}

void PL_Text(float x, float y, float size,char *text)
{
  if (!PL_Clip(x,y)) return;
  if (PL_Context.mode==PL_MODE_HPGL)
    {
      fprintf(PL_Context.file,"DI;SR%.2f,%.2f;LO13;PU;PA%.0f,%.0f;LB%s\003;SR;\n",
	      size*0.75,size*1.5,PL_XCoord(x), PL_YCoord(y),text);
    };
  if (PL_Context.mode==PL_MODE_PS)
    {
      /* ignores size */
      fprintf(PL_Context.file,"%.0f %.0f moveto\n",PL_XCoord(x)+25,PL_YCoord(y)-100);
      fprintf(PL_Context.file,"(%s) show\n",text);
    };
}

/*************************************/
/* Functions accessable from outside */
/*************************************/

/* Pen <pen> */
int PL_SetPen()
{
  PA_GetFloat;
  PL_Context.pen=PA_Result.Float;
  if (PL_Context.mode==PL_MODE_HPGL)
    fprintf(PL_Context.file,"SP%d\n",PL_Context.pen);
  return(PA_ERR_OK);
}

/* Plot ps|hpgl <file> */ 
int PL_Plot()
{
  FILE *file;
  int stat;
  PA_GetString;
  PL_Context.mode=PL_MODE_NONE;
  if (strcasecmp("ps", PA_Result.String)==0)
    PL_Context.mode=PL_MODE_PS;
  if (strcasecmp("hpgl", PA_Result.String)==0)
    PL_Context.mode=PL_MODE_HPGL;
  if (PL_Context.mode==PL_MODE_NONE)
    Error(PA_ERR_FAIL, "Must be PS or HPGL");
  PA_GetString;
  file=fopen(PA_Result.String,"w");
  if (file==NULL)
    Error(PA_ERR_FAIL,"Can't open file");
  PL_Context.file=file;
  switch (PL_Context.mode)
    {
    case PL_MODE_PS:	fprintf(PL_Context.file,"%%!PS-Adobe-2.0\ninitgraphics\n");
      fprintf(PL_Context.file,"1 setlinecap 1 setlinejoin 1 setlinewidth\n");
      fprintf(PL_Context.file,"50 50 translate 0.07 0.07 scale -90 rotate\n");
      fprintf(PL_Context.file,"0 setgray -10500 -800 translate\n");
      fprintf(PL_Context.file,"/Helvetica findfont 100 scalefont setfont\n");
      break;
    case PL_MODE_HPGL:	fprintf(PL_Context.file,"IN;CS10;CA5;SP1;IP;SC;\n");
      break;
    };
  /* change mode */
  stat=PA_Execute(PL_Cmds);
  switch (PL_Context.mode)
    {
    case PL_MODE_HPGL:	fprintf(PL_Context.file,"AH;\n");
      break;
    case PL_MODE_PS:	fprintf(PL_Context.file,"stroke\ngrestore\nshowpage\n");
      break;
    };
  fclose(PL_Context.file);
  /* back to normal */
  return(stat); 
} 


/* Syntax: Window <physical x-origin> <y> <x-range> <y-range>
   <logical x-origin> <y> <x-range> <y-range> */
int PL_SetWindow()
{
  /* float */ int p_x, p_y, p_xr, p_yr;
  /* float */ int l_x, l_y, l_xr, l_yr;
  PA_GetFloat; p_x=PA_Result.Float;
  PA_GetFloat; p_y=PA_Result.Float;
  PA_GetFloat; p_xr=PA_Result.Float;
  PA_GetFloat; p_yr=PA_Result.Float;
  PA_GetFloat; l_x=PA_Result.Float;
  PA_GetFloat; l_y=PA_Result.Float;
  PA_GetFloat; l_xr=PA_Result.Float;
  PA_GetFloat; l_yr=PA_Result.Float;
  if ((l_xr==0)||(l_yr==0)||(p_xr==0)||(p_yr==0))
    Error(PA_ERR_FAIL,"Invalid range");
  PL_Window(p_x, p_y, p_xr, p_yr);
  PL_Range(l_x, l_y, l_xr, l_yr);
  return(PA_ERR_OK);
}

/* Syntax: Scale x|y u=r|d=l <mark>|* <tick>|* */
int PL_Scale()
{
  int direction;
  int axis;
  float mark, tick;

  PA_GetString;
  axis=toupper(PA_Result.String[0]);
  PA_GetString;
  switch ( toupper(PA_Result.String[0]) )
    {
    case 'U':
    case 'R':	direction=PL_SCL_UP; break;
    case 'D':
    case 'L':	direction=PL_SCL_DOWN; break;
    default:	Error(PA_ERR_FAIL,"Scale must be up, down, left or right");
    };
  switch (axis)
    {
    case 'Y':	switch (PA_GetArg())
      {
      case PA_STRING:	if (PA_Result.String[0]!='*')
	Error(PA_ERR_FAIL,"Invalid argument");
	mark=abs(PL_Context.range_y/5 );
	if (mark>=5) mark=10;
	if ( (mark<5)&&(mark>0.5) ) mark=1;
	if ( (mark<1)&&(mark>0.05) ) mark=0.1;
	if (mark<0.1) mark=0.01;
	break;
      case PA_FLOAT:		mark=PA_Result.Float;
	break;
      default:		Error(PA_ERR_FAIL,"Invalid argument");
      };
      PA_GetArg();
      switch (PA_GetArg())
	{
	case PA_STRING:	if (PA_Result.String[0]!='*')
	  Error(PA_ERR_FAIL,"Invalid argument");
	  tick=mark/10;
	  break;
	case PA_FLOAT:		tick=PA_Result.Float;
	  break;
	default:		Error(PA_ERR_FAIL,"Invalid argument");
	};
      PL_YScale(direction|PL_SCL_R_TO_L,mark,tick);
      break;
    case 'X':	switch (PA_GetArg())
      {
      case PA_STRING:	if (PA_Result.String[0]!='*')
	Error(PA_ERR_FAIL,"Invalid argument");
	mark=abs(PL_Context.range_x/5 );
	if (mark>=5) mark=10;
	if ( (mark<5)&&(mark>0.5) ) mark=1;
	if ( (mark<1)&&(mark>0.05) ) mark=0.1;
	if (mark<0.1) mark=0.01;
	break;
      case PA_FLOAT:		mark=PA_Result.Float;
	break;
      default:		Error(PA_ERR_FAIL,"Invalid argument");
      };
      switch (PA_GetArg())
	{
	case PA_STRING:	if (PA_Result.String[0]!='*')
	  Error(PA_ERR_FAIL,"Invalid argument");
	  tick=mark/10;
	  break;
	case PA_FLOAT:		tick=PA_Result.Float;
	  break;
	default:		Error(PA_ERR_FAIL,"Invalid argument");
	};
      PL_XScale(direction|PL_SCL_R_TO_L,mark,tick);
      break;
    default:	Error(PA_ERR_FAIL,"Specify x- or y-axis");
    };
  return(PA_ERR_OK);
}

/* Syntax: Spectrum H|C|HH|CH <spec> [<title>] */
int PL_PlotSpectrum()
{
  int i, mode;
  struct BU_Struct *spectrum;
  PA_GetString;
  mode=(toupper(PA_Result.String[0])<<8)+toupper(PA_Result.String[1]);
  PA_GetString;
  spectrum=(struct BU_Struct *)
    FindNode(&(StructList.Head), PA_Result.String);
  if (spectrum==NULL)
    Error(PA_ERR_FAIL,"Can't find spectrum");
  if (PA_GetArg()==PA_STRING)
    PL_Text(PL_Context.origin_x,PL_Context.origin_y+PL_Context.range_y,
	    0.75,PA_Result.String);
  switch (mode)
    {
    case ('H'<<8):	for (i=0; i<spectrum->HCnt; i++)
      PL_Line(spectrum->HShift[i], PL_Context.origin_y, spectrum->HShift[i],
	      PL_Context.origin_y+PL_Context.range_y*0.7);
      break;
    case ('C'<<8):	for (i=0; i<spectrum->CCnt; i++)
      PL_Line(spectrum->CShift[i], PL_Context.origin_y, spectrum->CShift[i],
	      PL_Context.origin_y+PL_Context.range_y*0.7);
      break;
    case (('H'<<8)+'H'):	PL_Frame();
      for (i=0; i<spectrum->HhCnt; i++)
	{
	  PL_Mark(spectrum->HhLoShift[i],spectrum->HhHiShift[i],PL_TOK_STAR);
	  PL_Mark(spectrum->HhHiShift[i],spectrum->HhLoShift[i],PL_TOK_STAR);
	};
      for (i=0; i<spectrum->HCnt; i++)
	PL_Mark(spectrum->HShift[i],spectrum->HShift[i],PL_TOK_STAR);
      break;
    case (('C'<<8)+'H'):
    case (('H'<<8)+'C'):	PL_Frame();
      for (i=0; i<spectrum->ChCnt; i++)
	PL_Mark(spectrum->ChCShift[i],spectrum->ChHShift[i],PL_TOK_STAR);
      break;
    default:		Error(PA_ERR_FAIL, "Unknown type of spectrum");
    };
  return(PA_ERR_OK);
}

/* Syntax: Text <x> <y> <sz> <string> */
int PL_DrawText()
{
  float x,y;
  float size;
  PA_GetFloat; x=PA_Result.Float;
  PA_GetFloat; y=PA_Result.Float;
  PA_GetFloat; size=PA_Result.Float;
  PA_GetString;
  if (!PL_Clip(x,y)) Error(PA_ERR_WARN,"Outside window");
  if (PL_Context.mode==PL_MODE_HPGL)
    {
      fprintf(PL_Context.file,"DI;SR%.2f,%.2f;LO13;PU;PA%.0f,%.0f;LB%s\003;SR;\n",
	      size*0.75,size*1.5,PL_XCoord(x), PL_YCoord(y),PA_Result.String);
    };
  if (PL_Context.mode==PL_MODE_PS)
    {
      /* ignores size */
      fprintf(PL_Context.file,"%.0f %.0f moveto\n",PL_XCoord(x)+25,PL_YCoord(y)-100);
      fprintf(PL_Context.file,"(%s) show\n",PA_Result.String);
    };
  return(PA_ERR_OK);
}

