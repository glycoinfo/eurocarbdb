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

/* Filename = build.h */
/* Prefix = BU */
/* Internal structure of oligo/polymers */
/* Used for building and simulating spectra */

#ifndef _BUILD_H
#define _BUILD_H

#include "type.h"
#include "residue.h"
#include "methods.h"
#include "shifts.h"

struct BU_Unit {
  struct Node Node;		/* Linking */
  struct SH_Shifts Shifts;	/* Chemical shifts */
  float Error;			/* Accuracy */
  float HError;			/* Actual error H (from assign) */
  float CError;			/* Actual error C (from assign) */
  struct RE_Residue *Residue;		/* Residue */
  struct BU_Unit *Subst[TY_MAX_BONDS];	/* Substituents for positions */
  int Position;			/* position to which this unit is bonded */
/* Gfx variables ?? */
  int HorizontalOffset;		/* Distance to 'reducing end' in 'units' */
  int VerticalOffset;		/* Offset from longest branch (positive=up) */
  int GfxFlags;			/* Various flags */
  int CcpnUnitNr;
};

#define BU_VOID_SHIFT	(-1e5)		/* invalid CS */

/* Gfx flags */
#define BU_IS_BACKBONE	0x01		/* this is a backbone unit */

/* intcuts */
#define BU_ANOMER_POS(unit)		(unit->Shifts.Type->Anomeric)
#define BU_ANOMER_LABEL(unit)		(unit->Shifts.Type->Atom[BU_ANOMER_POS].Label)
#define BU_COPYSHIFTATOMASSIGNMENT(dest, src) dest.unit=src.unit; dest.atom[0]=src.atom[0]; dest.atom[1]=src.atom[1]


/* chk these defs */
#define UNIT_FREE	TY_FREE_POS	/* Unused bond */
#define UNIT_OCCUPIED	TY_USED_POS	/* Bond used by a residue defined elsewere */

#define GFX_F_PRINT	0x01		/* Print this residue */
#define GFX_F_SKIP	0x00		/* Skip this residue (Has been printed) */

/* Structure that contains spectrum and units */
#define BU_MAX_UNITS	20	/* maximum number of 'units' */

/* maximum number of chemical shifts */
#define BU_MAX_SHIFTS	(BU_MAX_UNITS*TY_MAX_PROTON)

#define BU_INFO_SIZE	50



#ifndef Min
#define Min(a,b)	(a<b?a:b)
#endif

#ifndef Max
#define Max(a,b)	(a>b?a:b)
#endif

struct BU_ShiftToAtom {
  struct BU_Unit *unit;
  int atom[2];
};

struct BU_Struct
{
  struct Node Node;
  char Info[BU_INFO_SIZE];	/* Comment */
  char Type;			/* Determines type of additional info */
  float Error;
  float TotFit;                 /* Sum of all Fitting differences */
  float CFit;			/* Fit to experimental data */
  float HFit;			/* or last comparison */
  float ChFit;
  float HhFit;
  
  int JHH[3];		/* number of s,m, and l JHH */
  int JCH[3];		/* number of s,m, and l JCH */

  int CCnt;
  float CShift[BU_MAX_SHIFTS];
  struct BU_ShiftToAtom CShiftAtom[BU_MAX_SHIFTS];
  float CCorrection;
  float CSysErr;

  int HCnt;
  float HShift[BU_MAX_SHIFTS];
  struct BU_ShiftToAtom HShiftAtom[BU_MAX_SHIFTS];
  float HCorrection;
  float HSysErr;

  int ChCnt;			/* CH-HSQC data */
  float ChCShift[BU_MAX_SHIFTS];
  float ChHShift[BU_MAX_SHIFTS];
  struct BU_ShiftToAtom ChShiftAtom[BU_MAX_SHIFTS];

  int HhCnt;			/* HH-COSY data */
  float HhLoShift[BU_MAX_SHIFTS*2];	/* lower of two shifts */
  float HhHiShift[BU_MAX_SHIFTS*2];	/* higher of two shifts */
  struct BU_ShiftToAtom HhLoShiftAtom[BU_MAX_SHIFTS*2];
  struct BU_ShiftToAtom HhHiShiftAtom[BU_MAX_SHIFTS*2];

  

  /* not used in experimental spectra */
  float CAssignments[2][BU_MAX_SHIFTS];
  int nCAssignments;
  float HAssignments[2][BU_MAX_SHIFTS];
  int nHAssignments;
  int nUnassigned; /* How many atoms are assigned */
  float fractionAssigned; /* %age of how many atoms are assigned */

  struct List Units;		/* List of contained units */
  char CcpnChainCode[BU_INFO_SIZE]; /* Name of the MolSystem Chain this structure belongs to */
};

/* structure that defines a linkage */
struct BU_LinkDesc {
	struct BU_Unit *Unit[2];	/* units */
	int Atom[2];			/* linkage positions */
};

int BU_SetStruct();
int BU_Info();
int BU_SetUnit();
int BU_ReadLink(struct BU_LinkDesc *desc, char *string);
int BU_Link();

extern struct List StructList;
extern struct ME_Method UnitMethod;
extern struct ME_Method StructMethod;

extern struct BU_Struct *BU_Struct;
extern struct BU_Unit *BU_Unit;

void *BU_CreateUnit(char *name);
void BU_ClearUnit(struct BU_Unit *unit);

/* internal fn:s - shared with spectrum.c */
void *BU_CreateStruct(char *name);
void BU_ClearStruct(struct BU_Struct *structure);
void* BU_FreeStruct(struct BU_Struct *structure);

void BU_ListStruct(struct BU_Struct *structure);
void BU_PrintStruct(struct BU_Struct *structure);
void CopyStructInfo(struct BU_Struct *a, struct BU_Struct *b);

int BU_PrintStruct3DLink();
char *BU_GetUnitName(char *string);
struct RE_Residue *BU_GetResidue(char *string);
struct BU_Unit *BU_CreateLNUnit();
int BU_GetLinkage(struct RE_Residue *residue, char *string);
struct BU_Unit *BU_Backbone(struct BU_Unit *current);
int BU_LineNotation();
int BU_DuplicateStructSegment(struct BU_Struct *str, struct BU_Unit *first, int fromPos, struct BU_Unit *last, int toPos);
struct BU_Unit *BU_DuplicateUnit(struct BU_Struct *str, struct BU_Unit *unit, int skip, struct BU_Unit *linkedFrom);
int BU_CopyStructure(struct BU_Struct *exp, struct BU_Struct *sim);
struct BU_Unit *BU_FindCcpnUnit(struct Node *node, int nr);
void BU_RemoveOrphanUnits(struct BU_Struct *str);
#endif
