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

/* Filename = setup.h */
/* Prefix = SE_ */
/* Setting up a permutation */

#ifndef _SETUP_H
#define _SETUP_H

#include "node.h"
#include "build.h"
#include "type.h"
#include "delta.h"



#define SE_FREE		'F'
#define SE_OCCUPIED	'O'

#define SE_JHH_SMALL	0
#define SE_JHH_MEDIUM	1
#define SE_JHH_LARGE	2
#define SE_JCH_SMALL	0
#define SE_JCH_MEDIUM	1
#define SE_JCH_LARGE	2

#define SE_PURGE_C	'c'
#define SE_PURGE_H	'h'
#define SE_PURGE_CH	'C'
#define SE_PURGE_HH	'H'
#define SE_PURGE_CHLR   'x'
#define SE_PURGE_TOT    'A'
#define SE_NO_CUTOFF	0x00
#define SE_USE_CUTOFF	0x01
#define SE_UNKNOWN_RES  0x02

#define SE_INFO_SIZE	50
#define SE_NAME_LENGTH	30
#define SE_MAX_BONDS	50
#define SE_MAX_RESIDUES	50

#define SE_VOID_NAME	NULL




struct SE_Residue {
  struct Node Node;
  struct RE_Residue *Base;	/* what sugar */
  int Anomeric;			/* anomeric position */
  int Free[TY_MAX_BONDS];	/* which pos are free */
  int FreeCertainty;  /* If we know all Free positions are used */
  /* determine from orientation of anomeric H */
  int JHH, JCH;			/* type of JHH/JCH */
};

struct SE_Unit {
  struct Node Node;
  struct List Residues;	/* alternative residues */
  int UsedInStructure;
};

struct SE_ResidueDef {
  struct Node Node;
  struct SE_Residue *Residue;
  struct BU_Unit *Unit;
  struct SE_Unit *UnitSpecification;
  unsigned char Connected;		/* flag for tracing of connectivity */
};

struct SE_BondDef {
  int FromRes, FromPos;
  int ToRes, ToPos;
};

struct SE_Linkage {
  struct Node Node;
  struct SE_Unit *FromUnit;
  struct SE_Unit *ToUnit;
  int FromPos;
  int ToPos;
};

struct SE_MissingBond {
  struct Node Node;
  struct DE_DimerDesc dimer;
};

struct SE_Simulation {
  char Info[SE_INFO_SIZE];	/* description */
  float CCutoff, HCutoff;	/* cut-off values for errors */
  int JHH[3];		/* number of s,m, and l JHH */
  int JCH[3];		/* number of s,m, and l JCH */
  int CntJHH[3];		/* Current count */
  int CntJCH[3];		/* Current count */
  char Name[SE_NAME_LENGTH];	/* name of simulated structures */
  struct BU_Struct *Experimental;	/* experimental spectrum */
  int StrCnt;		/* number of structures (for name) */
  int StrUpper;		/* highest number of structures to keep */
  int StrLower;		/* lowerst number of structures to keep */
  int StrActual;		/* current number of generated structures */
  float WorstFit;		/* fit of worst fitting structure */
  unsigned char Criteria;		/* Criteria for sorting */
  int Flags;		/* various flags */
  int UnitCnt;		/* Number of units */
  struct List Units;	/* List of included 'units' */
  struct List Rules;    /* Rules that have to be fulfilled, e.g. metabolic pathway rules */
  struct List Linkages; /* List of linkages (SE_Linkage) that are known to occur.
			   They specify involved residues and may specify
			   positions too. */
  struct List MissingBonds; /* Bond combinations that do not exist in CASPER's
			       data are listed here as soon as they appear in
			       the permutation. When structures are generated
			       the building blocks are matched against this
			       list and the structure is discarded at an early
			       stage if the bond cannot be calculated. */
};

extern struct SE_BondDef SE_BondStack[SE_MAX_BONDS];
extern int SE_BondSP;		/* Stackpointer */
extern struct SE_ResidueDef SE_ResidueStack[SE_MAX_RESIDUES];
extern int SE_ResidueSP;


/* Fn:s */
void *SE_CreateResidue(char *name);
void *SE_CreateUnit(char *name);
void *SE_FreeUnit(struct SE_Unit *unit);
void *SE_CreateLinkage(char *name);
void SE_ClearLinkage(struct SE_Linkage *link);
void SE_Initialize();
int SE_Info();
int SE_Purge();
int SE_JHH();
int SE_JCH();
int SE_EquivalentUnits(struct SE_Unit *a, struct SE_Unit *b);
int SE_EquivalentResidues(struct SE_Residue *a, struct SE_Residue *b);
int SE_Generate();
int SE_SetUnit();
int SE_SetResidue();
int SE_AddRule();
int SE_AddLinkage();
/* extern int SE_Link(); */
/* int SE_Cutoff();*/
/* void SE_PurgeInvalid();*/


extern struct ME_Method SE_UnitMethod;
extern struct ME_Method SE_ResidueMethod;
extern struct ME_Method SE_LinkageMethod;
extern struct ME_Method SE_MissingBondMethod;


extern struct SE_Simulation SE_Simulation;


#endif
