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

/* Filename = ddelta.h */
/* Prefix = DD */
/* Structures related to trisaccharide fragments */

#ifndef _DDELTA_H
#define _DDELTA_H

#include "type.h"
#include "residue.h"
#include "shifts.h"

#define DD_NO_CORRECT_ERR	5.0	/* Add to error if no trimer found */

#define DD_INFO_SIZE	50		/* Length of info */
#define DD_FIRST	0		/* First substituent */
#define DD_SECOND	1		/* Second */
#define DD_CENTRAL	2		/* Central residue */

struct DD_Delta {
  struct Node Node;		/* Link */
  char Info[DD_INFO_SIZE];	/* Comment */
  struct SH_Shifts Shifts[3];	/* Chemical shifts */
  struct RE_Residue *Residue[3];	/* Residues */
  float Error;			/* Accuracy */
  /* Hmm... */
  int SubstPos[2];
  int CentralPos[2];
  /* This is not necessary for finding a delta set but is */
  /* useful for printing and very tricky to find out by */
  /* other means */
  int nrpos;			/* Position in nonreducing */
};

/* structure that defines a unique ddelta set */
/* created by string->ddelta */
struct DD_TrimerDesc {
	struct RE_Residue *Residue[3];	/* residues */
	int SubstAtom[2];		/* linkage positions */
	int CentrAtom[2];		/* linkage positions in central */
	};


extern struct List DD_Branch[2][2][2][2];

extern struct ME_Method DD_DeltaMethod;
void *DD_CreateTrimer(char *name);
void DD_ListTrimer(struct DD_Delta *delta);
void DD_PrintTrimer(struct DD_Delta *delta);
int DD_SetDDelta();
int DD_Shift();
int DD_Extract();
int DD_Info();
int DD_Correct();
int DD_Error();
int DD_ReadDelta(struct DD_TrimerDesc *desc, char *string);
int DE_ReadTrimer(struct DD_TrimerDesc *desc, char *string);
/* extern int DD_SetDefault(); */


/* Different geometries of the linkages */
#define DD_ANOMER_AX	0x00
#define DD_ANOMER_EQ	0x01
#define DD_AXIAL	DD_ANOMER_AX
#define DD_EQUATORIAL	DD_ANOMER_EQ
/* Different relative chiralities */
#define DD_SAME_SAME	"SASA"
#define DD_SAME_DIFF	"SADI"
#define DD_DIFF_SAME	"DISA"
#define DD_DIFF_DIFF	"DIDI"



/* If shifts or shift diffs are provided */
#define DD_NOEXTRACT	0x00
#define DD_EXTRACT	0x01

/* order of residues */
#define DD_LO_HI	0x00
#define DD_HI_LO	0x01
#endif
