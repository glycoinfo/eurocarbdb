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

/* Filename = XML_out.c */
/* Prefix = XML_ */
/* Output of casper-spectra/structures in XML */

#include "build.h"
#include "parser.h"
#include "gfx.h"
#include "node.h"

/* Format:
   <casper>
   <code>
   casper structure
   </code>

   <spectrum nucleus="13C">
   <peak residue="label" atom="Cn">cs</peak>


   </spectrum>

   <spectrum nucleus="1H">

   <spectrum nucleus="1H 1H">

   <spectrum nucleus="13C 1H">
*/


void XML_code(struct BU_Struct *structure)
{
  GFX_StructGfx(structure, NULL, 0);
}

void XML_residues(struct BU_Struct *structure)
{
  struct BU_Unit *unit;
 
  for (unit=(struct BU_Unit *)structure->Units.Head.Succ;
       unit->Node.Succ!=NULL; unit=(struct BU_Unit *)unit->Node.Succ)
    {
      /* residues */
      printf("<residue label=\"%s\" type=\"CARBBANK\">%s</residue>\n",
	     unit->Node.Name, unit->Residue->CarbBank);
    };
}

void XML_linkages(struct BU_Struct *structure)
{
  struct BU_Unit *unit;
  int pos_f,pos_t;
  char *from, *to, *label_f, *label_t;
  for (unit=(struct BU_Unit *)structure->Units.Head.Succ;
       unit->Node.Succ!=NULL; unit=(struct BU_Unit *)unit->Node.Succ)
    {
      /* 1) find anomeric position */
      from=unit->Node.Name;
      pos_f=unit->Shifts.Type->Anomeric;
      label_f=unit->Shifts.Type->Atom[pos_f].Label;
      /* 2) find residue linked to */
      if (unit->Subst[pos_f]==NULL) continue; /* No link */
      to=unit->Subst[pos_f]->Node.Name;
      pos_t=unit->Position;
      label_t=unit->Subst[pos_f]->Shifts.Type->Atom[pos_t].Label;
      /* 3) print */
  
      printf("<linkage merge=\"%s:O%s %s:O%s\" ",
	     from, label_f, to, label_t );
      printf("delete=\"%s:O%sH %s:O%sH\">",
	     from, label_f, to, label_t );
      /* just to make life simple -label for printing*/
      printf("%s(%s-%s)%s</linkage>\n",
	     from, label_f, label_t, to );
    };
}


void XML_spec_c(struct BU_Struct *structure)
{
  int i;
  struct BU_Unit *unit;
 
  for (unit=(struct BU_Unit *)structure->Units.Head.Succ;
       unit->Node.Succ!=NULL; unit=(struct BU_Unit *)unit->Node.Succ)
    {
      /* 13C shifts */
      for (i=0; i<unit->Shifts.Type->HeavyCnt; i++)
	{
	  if (unit->Shifts.Type->Atom[i].Type&TY_SILENT)
	    continue;
	  if (unit->Shifts.C[i]==BU_VOID_SHIFT)
	    continue;
	  printf("<peak assignment=\"%s:C%s\">", unit->Node.Name,
		 unit->Shifts.Type->Atom[i].Label);
	  printf("%.2f</peak>\n", unit->Shifts.C[i]);
	};
    };
}
 
void XML_spec_h(struct BU_Struct *structure)
{
  int i,j;
  struct BU_Unit *unit;
 
  for (unit=(struct BU_Unit *)structure->Units.Head.Succ;
       unit->Node.Succ!=NULL; unit=(struct BU_Unit *)unit->Node.Succ)
    {
      /* 1H shifts */
      for (i=0; i<unit->Shifts.Type->HeavyCnt; i++)
	{
	  for (j=0; j<unit->Shifts.Type->Atom[i].HCnt; j++)
	    {
	      if (unit->Shifts.H[i][j]==BU_VOID_SHIFT)
		continue;
	      printf("<peak assignment=\"%s:H%s\">", unit->Node.Name,
		     unit->Shifts.Type->Atom[i].Label);
	      printf("%.2f</peak>\n", unit->Shifts.H[i][j]);
	    };
	};
    };
}

int XML_structure()
{
  struct BU_Struct *structure;
  /* find structure */
  PA_GetString;
  structure=(struct BU_Struct *)
    FindNode((struct Node *)&StructList, PA_Result.String);
  if (structure==NULL) Error(PA_ERR_FATAL,"Structure not found");
  /* emit header */
  puts("<sample>");
  printf("<name>%s</name>\n", structure->Node.Name);
  if (structure->Info!=NULL)
    printf("<comment>%s</comment>\n", structure->Info);
  /* emit structure in CASPER format */
  puts("<structure encoding=\"CASPER_1.0\">");
  XML_code(structure);
  puts("</structure>");
  /* emit structure in XML */
  puts("<structure encoding=\"XML_0.1\">");
  XML_residues(structure);
  XML_linkages(structure);
  puts("</structure>");
  /* emit 13C spectrum */
  puts("<spectrum nucleus=\"13C\">");
  XML_spec_c(structure);
  puts("</spectrum>");
  /* emit 1H spectrum */
  puts("<spectrum nucleus=\"1H\">");
  XML_spec_h(structure);
  puts("</spectrum>");
  /* emit footer */
  puts("</sample>");
  return(PA_ERR_OK);
}
