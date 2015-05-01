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

    Copyright 2010 Magnus Lundborg, Göran Widmalm
*/


#ifndef _GL_H
#define _GL_H

#include "node.h"

struct GL_Info {
  struct Node Node;
};

void GL_printstr(struct BU_Struct *structure);
int GL_build_reslist(struct BU_Struct *structure, struct List *GL_reslist, 
		     struct List *GL_linklist, char *repetition);
void GL_FindUnit(struct BU_Unit *unit, struct List *GL_reslist, 
		 struct List *GL_linklist, int *rescnt, int *linkcnt,
		 char *repetition);
void GL_UnittoList(struct BU_Unit *unit, struct List *GL_reslist,
		   struct List *GL_linklist, int *rescnt, int *linkcnt);
void GL_SubsttoList(struct BU_Unit *unit, struct List *GL_reslist,
		    struct List *GL_linklist, int *rescnt, int *linkcnt);
int GL_PrintAglycan(struct BU_Struct *structure);

int GL_GlycoInput();

extern struct ME_Method GL_ResMethod;
extern struct ME_Method GL_LinkMethod;


#endif
