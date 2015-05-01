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

/* Filename = Variables.h */
/* Prefix = VA */

#ifndef _VARIABLES_H
#define _VARIABLES_H

#include "expr.h"
#include "methods.h"
#include "node.h"

/* Original definition */
struct VA_Variable {
		struct Node Node;	/* Link and name */
		struct Value Value;	/* Actual value */
		};

void *VA_CreateVariable(char *name);
void VA_ClearVariable(struct VA_Variable *variable);
void VA_PrintVariable(struct VA_Variable *variable);
void VA_SaveVariable(struct VA_Variable *variable);
void *VA_FreeVariable(struct VA_Variable *variable);

int VA_SetVariable();
int VA_Pop();
int VA_If();

extern struct List VariableList;
extern struct ME_Method VariableMethod;

#endif
