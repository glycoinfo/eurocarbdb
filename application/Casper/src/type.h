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

/* Filename = type.h */
/* Prefix = TY */
/* Type structure */

#ifndef _TYPE_H
#define _TYPE_H

#include <strings.h>

#include "node.h"
#include "methods.h"

/* The TY_Type structure defines the 'backbone' structure of */
/* a residue. */

#define TY_INFO_LEN	50	/* Length of info */
#define TY_LABEL_LEN	6	/* Length of (heavy)atom label */
#define TY_MAX_HEAVY	16		/* Limiting number of heavy atoms */
#define TY_MAX_CARBON	16		/* Limiting number of carbons */
#define TY_MAX_PROTON	(TY_MAX_CARBON*2)	/* Limiting number of protons */
#define TY_MAX_BONDS	TY_MAX_CARBON	/* Limiting number of bonds */
#define TY_NCONNECT	255		/* Invalid connection */
/* #define TY_NCONNECT	(-1) */

#define TY_PYRANOSE	0x0010	/* Atom in pyranose ring */
#define TY_FURANOSE	0x0020	/* Atom in furanose ring */
/* Exocyclic is implied by methy, prim, sec... */
/* #define TY_EXOCYCLIC	0x0040	* Exocyclic fragment */

#define TY_METHYL	0x0000	/* Methyl group */
#define TY_PRIMARY	0x0001	/* Primary position */
#define TY_SECONDARY	0x0002	/* Secondary position */
/* #define TY_TERTIARY */

#define TY_SILENT	0x8000	/* NMR-silent (non C) heavy atom */

#define TY_FREE_POS	0x0100	/* Position may be substituted */
/* #define TY_USED_POS	0x0200	* Position may not be substituted */

#define TY_ANOMERIC	0x1000	/* 'Anomeric' position, origin of linkages */
#define TY_CLOSING      0x2000  /* "Last" atom in the ring */

/* Id:s for input */
#define TY_PYRAN_ID	(('P'<<8)+'Y')
#define TY_FURAN_ID	(('F'<<8)+'U')
/* #define TY_EXO_ID	(('E'<<8)+'X') */

#define TY_METHYL_ID	(('M'<<8)+'E')
#define TY_PRIM_ID	(('P'<<8)+'R')
#define TY_SEC_ID	(('S'<<8)+'E')
/* #define TY_TERT_ID */

#define TY_SILENT_ID	(('S'<<8)+'I')

#define TY_FREE_ID	(('F'<<8)+'R')
#define TY_USED_ID	(('U'<<8)+'S')

#define TY_ANOMER_ID	(('L'<<8)+'I')

#define TY_CLOSE_ID     (('C'<<8)+'L') /* "Last" atom in the ring */

#define TY_VOID_ATOM	(-1)	/* Does not 'connect' */

#define TY_NOT_FOUND	(-1)	/* Invalid atomnumber */

#define TY_CONV_SAME (struct TY_Conversion *)(-1)


struct TY_Atom {
  char Label[TY_LABEL_LEN];	/* Atom label */
  int Type;			/* Type of atom */
  int HCnt;		/* Number of connected protons */
  int Connect[4];	/* Index of connected carbons */
};

struct TY_Type {
  struct Node Node;	/* Link and name */
  char Info[TY_INFO_LEN];	/* Info */
  char Priority[TY_INFO_LEN];		/* Priority in sorting */
  int HeavyCnt;		/* Number of heavy atoms (~carbons) */
  int HCnt;		/* Number of hydrogens */
  int Anomeric;		/* Index of anomeric atom */
  struct TY_Atom Atom[TY_MAX_HEAVY];	/*Data for heavy atoms */
  struct List Conversion;		/* List of conversions */
  char CTsuperclass[NODE_NAME_LENGTH];  /* What the type is called in GLYCO-CT */
  struct List CTmods;             /* List of modifiers */
  struct List CTsubsts;           /* List of substituents */
  char ccpSub[TY_INFO_LEN];       /* String listing substituents according to
				     chemcomps */
  char outlinkage;      /* Determines what is replaced when forming outgoing
			   bond from linkage position. Usually 'o'. */
};

/* For specifying modifiers according to the GLYCO-CT format */
/* List of types of modifiers:
   d = deoxigenation
   keto = carbonyl function
   en,ne,ee,nn,en? = double bond
   a = acidic function
   aldi = reduced C1-carbonyl */
struct TY_CTmodifier {
  struct Node Node;
  char modifier[8];
  int position;
};

struct TY_CTsubstituent {
  struct Node Node;
  char substituent[16];
  int position[2];    /* External substituents in order [from type]
			 [to substituent], internal substituents 
			 [from position] [to position] */
  char basetypelinkage; /* Determines what has been replaced when forming the
			   bond. Usually 'o'. */
};


/* The TY_Conversion structure defines the mapping of a backbone */
/* on another. */

struct TY_Conversion {
  struct Node Node;		/* Link and 'name' */
  struct TY_Type *From, *To;	/* 'from' and 'to' types */
  int AtomNr[TY_MAX_CARBON];	/* Atom number of 'name' mapped on type */
};

void *TY_CreateType(char *name);
void TY_ClearType(struct TY_Type *type);
void TY_PrintType(struct TY_Type *type);
void TY_SaveType(struct TY_Type *type);
void *TY_FreeType(struct TY_Type *type);
void *TY_CreateConversion(char *name);
void TY_ClearConversion(struct TY_Conversion *conv);
void TY_ListConversion(struct TY_Conversion *conv);
void TY_PrintConversion(struct TY_Conversion *conv);
void TY_SaveConversion(struct TY_Conversion *conv);
void *TY_CreateCTmod(char *name);
void TY_ClearCTmod(struct TY_CTmodifier *mod);
void TY_ListCTmod(struct TY_CTmodifier *mod);
void TY_PrintCTmod(struct TY_CTmodifier *mod);
void TY_SaveCTmod(struct TY_CTmodifier *mod);
void *TY_CreateCTsubst(char *name);
void TY_ClearCTsubst(struct TY_CTsubstituent *sub);
void TY_ListCTsubst(struct TY_CTsubstituent *sub);
void TY_PrintCTsubst(struct TY_CTsubstituent *sub);
void TY_SaveCTsubst(struct TY_CTsubstituent *sub);

int TY_SetType();
int TY_FindAtom(struct TY_Type *type, char *name);
int TY_Atom();
int TY_Info();
int TY_Priority();
int TY_Connect();
int TY_SetConversion();
extern struct TY_Conversion *TY_FindConversion();
int TY_SetModification();
int TY_SetCTClass();
int TY_SetSubstituent();
int TY_SetCcpSub();
int TY_SetMap();


extern struct List TypeList;
extern struct ME_Method TypeMethod;
extern struct ME_Method ConversionMethod;
extern struct ME_Method CTModifierMethod;
extern struct ME_Method CTSubstsMethod;

extern void *TY_CreateCTmod();

#endif
