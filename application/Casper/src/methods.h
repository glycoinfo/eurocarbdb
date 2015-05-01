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

/* Filename = methods.h */
/* Prefix = ME */

#ifndef _METHODS_H
#define _METHODS_H

#include "node.h"

/* Defines standard actions for lists */
struct ME_Method {
		void * (*Create)( /* name */ );	
		void (*Clear)( /* node */ );
		void * (*Remove)( /*node */ );
		void (*List)( /* node */ );
		void (*Print)( /* node */ );
		void (*Save)( /* node */ );
	      };

/* May benefit from inclusion of */
/* 1) pointer to default structure - to copy when initializing */
/* 2) pointer to list (where applicable) */

void ME_ClearNode(struct ME_Method *method, struct Node *node);
void ME_RemoveNode(struct ME_Method *method, struct Node *node);
void ME_EmptyList(struct ME_Method *method, struct List *list);
void ME_PrintNode(struct ME_Method *method, struct Node *node);
void ME_PrintList(struct ME_Method *method, struct List *list);
void *ME_CreateNode(struct ME_Method *method, struct List *list, char *name);
void ME_SaveNode(struct ME_Method *method, struct Node *node);
void ME_SaveList(struct ME_Method *method, struct List *list);



#endif
