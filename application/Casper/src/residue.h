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

/* Filename = residue.h */
/* Prefix = RE */
/* Def's of the Residue comand and subcommands and some public fns */
/* The Residue-structure */

#ifndef _RESIDUE_H
#define _RESIDUE_H

#include "node.h"
#include "type.h"
#include "shifts.h"

#define RE_INFO_LEN	50		/* Length of info */

/* The Residue structure contains base shifts and links delta shift lists */

struct RE_Residue {
  struct Node Node;		/* Linking */
  char Info[RE_INFO_LEN];		/* Additional information */
  char CarbBank[RE_INFO_LEN];	/* CarbBank residue name */
  char CTname[RE_INFO_LEN];     /* GLYCO-CT name */
  char Priority[RE_INFO_LEN];	/* Residue name for 'sorting' in structures */
  int JHH, JCH;	/* small, medium or large */
  char HvyConf[TY_MAX_CARBON];	/* 'Configuration' at hvy atom */
  struct SH_Shifts Shifts;	/* 13C and 1H chemical shifts */
  float Error;			/* Error approx. ppm 13C/atom */
  struct List Delta[TY_MAX_BONDS];	/* List of all delta shift values */
  struct List Defaults[TY_MAX_BONDS];	/* Residues to use for defaults */
  char Config;			/* Absolute configuration */
  struct RE_Residue *Enantiomer;	/* Enantiomer (if applicable) */
};

/* Node in list of default residues */
struct RE_Default {
  struct Node Node;
  struct RE_Residue *Residue;	/* Residue to used instead */
  float Error;			/* Accuracy of approximation */
};

/* Coupling constants */
#define RE_UNKNOWN	-1
#define RE_SMALL	0
#define RE_MEDIUM	1
#define RE_LARGE	2

/* conformation rather than configuration ? */
/* 1C4 vs 4C1 ??? */
#define RE_LEVO		'L'
#define RE_DEXTRO	'D'
#define RE_RECTUS	'R'	/* For R/S convension */
#define RE_SINISTER	'S'
#define RE_ACHIRAL	' '
/* ONLY L & D make sense at present */

#define	RE_AXIAL	0x01	/* Axial position on pyranose ring */
#define RE_EQUATORIAL	0x02	/* Equatorial position on pyranose ring */
/* deoxy might not be a wise choice */
#define RE_DEOXY	0x00	/* Neither ax or eq */
#define	RE_AX_ID	(('A'<<8)+'X')
#define RE_EQ_ID	(('E'<<8)+'Q')
#define RE_DEOXY_ID	(('D'<<8)+'E')
/* default - unknown */
#define RE_UNDEFINED		0x00

void *RE_CreateResidue(char *name);
void RE_ClearResidue(struct RE_Residue *residue);
void *RE_FreeResidue(struct RE_Residue *residue);
void RE_ListResidue(struct RE_Residue *residue);
void RE_PrintResidue(struct RE_Residue *residue);
void RE_SaveResidue(struct RE_Residue* residue);
void *RE_CreateDefault(char *name);
int RE_SetResidue();
int RE_Enantiomer();
int RE_Config();
int RE_JHH();
int RE_JCH();
int RE_Error();
int RE_Priority();
int RE_Shift();
int RE_Info();
int RE_CarbBank();
int RE_CTname();
int RE_SetDefault();
int RE_CpyDefault();
int RE_ConvertAllDefaults();
int RE_Manipulate(int action);
int RE_Add();
int RE_Sub();
int RE_Copy();
int RE_Correct();





extern struct List ResidueList;
extern struct ME_Method ResidueMethod;
extern struct ME_Method DefaultMethod;

#endif
