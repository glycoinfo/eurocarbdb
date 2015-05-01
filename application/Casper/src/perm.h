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

/* Filename = perm.h */
/* Prefix = PE */
/* Contains definitions relating to structure */
/* generation */

#ifndef _PERM_H
#define _PERM_H

#include "setup.h"

#define PE_MAX_RESIDUES	50
#define PE_MAX_BONDS	(PE_MAX_RESIDUES+20)
#define PE_SKIP_STRUCT -999


struct PE_ResDef {
	struct Node Node;
	struct SE_Residue Residue;
	struct SE_Unit *Unit;
	unsigned char Connected;	/* flag for tracing of connectivity */
        };

struct PE_BondDef {
	int FromItem, FromPos;
	int ToItem, ToPos;
	};

/* #define PE_FROMPOS	0 */
#define PE_NEWPOS_NEW	(-1)	/* Start flag GNP_NEW */
#define PE_NEWPOS_FAIL	(-1)	/* GNP_FAIL */
#define PE_START	(-2)	/* Start generating bonds PB_NEW */

struct BU_Struct *PE_FindWorstFit();
int PE_AreSameUnit(struct BU_Unit *first_u, struct BU_Unit *second_u);
int PE_AreSameStruct(struct BU_Struct *first, struct BU_Struct *second);
void PE_PurgeSame();
int PE_Compare(const struct BU_Struct *str1, const struct BU_Struct *str2,
	       int mode);
void PE_SortStructures();
int PE_MkStruct();
int PE_Connected();
int PE_GetToPos(int *item_pointer, int *pos_pointer, int from_item);
int PE_GetFromPos(int item_pointer);
int PE_ApplyLinkages();
int PE_IterateLinkageListPositions(int ptr, int cnt, int *unknownPosPtr);
int PE_PermBonds(int prev_item);
int PE_PermResidue(struct SE_Unit *Unit);
int PE_StaticReducingEnd();
void PE_PermUnknownConfigs(int *array, int n, int ptr);
int PE_Generate();
int PE_RePurge();
int PE_Max(int a, int b);
int PE_CheckBond(int From_item, int To_item, int To_pos);

#endif
