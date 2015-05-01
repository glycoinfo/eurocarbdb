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

/* Filename = setup.c */
/* Prefix = SE_ */
/* Setting up a permutation */

#include "setup.h"
#include "parser.h"
#include "spectra.h"
#include "perm.h"
#include "node.h"
#include "delta.h"
#include "ccpn.h"
#include "rule.h"
#include <string.h>
#include <strings.h>
#include <ctype.h>

struct SE_Simulation SE_Simulation;

struct SE_BondDef SE_BondStack[SE_MAX_BONDS];
int SE_BondSP;		/* Stackpointer */
struct SE_ResidueDef SE_ResidueStack[SE_MAX_RESIDUES];
int SE_ResidueSP;


/* SE_Residue methods */
void *SE_CreateResidue(char *name)
{
  struct SE_Residue *residue;
  int i;
  residue=(struct SE_Residue *)MakeNode(sizeof(struct SE_Residue), name);
  if (residue==NULL) return(NULL);
  for (i=0; i<TY_MAX_BONDS; i++)
    residue->Free[i]=SE_OCCUPIED;
  residue->JHH=RE_UNKNOWN; /* should not be needed */
  residue->JCH=RE_UNKNOWN;
  residue->FreeCertainty=TRUE;
  return(residue);
}

struct ME_Method SE_ResidueMethod={
  SE_CreateResidue, NULL, FreeNode,
  NULL, NULL, NULL
};


/* SE_Unit methods */

void *SE_CreateUnit(char *name)
{
  struct SE_Unit *unit;
  unit=(struct SE_Unit *)MakeNode(sizeof(struct SE_Unit), name);
  if (unit==NULL)
    {
      return(NULL);
    }
  InitList(&(unit->Residues));
  strcpy(unit->Residues.Head.Name,"Residues Head");
  strcpy(unit->Residues.Tail.Name,"Residues Tail");
  unit->UsedInStructure=0;
  return(unit);
}

void *SE_FreeUnit(struct SE_Unit *unit)
{
  ME_EmptyList(&SE_ResidueMethod, &(unit->Residues) );
  return(FreeNode((struct Node *)unit));
}

struct ME_Method SE_UnitMethod={
  SE_CreateUnit, NULL, SE_FreeUnit,
  NULL, NULL, NULL
};

void *SE_CreateLinkage(char *name)
{
  return (MakeNode(sizeof(struct SE_Linkage), name) );
}
void SE_ClearLinkage(struct SE_Linkage *link)
{
  link->FromUnit=NULL;
  link->ToUnit=NULL;
  link->FromPos=TY_VOID_ATOM;
  link->ToPos=TY_VOID_ATOM;
}
struct ME_Method SE_LinkageMethod={
  SE_CreateLinkage, SE_ClearLinkage, FreeNode, NULL, NULL, NULL
};

void *SE_CreateMissingBond(char *name)
{
  return (MakeNode(sizeof(struct SE_MissingBond), name) );
}
void SE_ClearMissingBond(struct SE_MissingBond *bond)
{
  bond->dimer.Residue[0]=NULL;
  bond->dimer.Residue[1]=NULL;
}
struct ME_Method SE_MissingBondMethod={
  SE_CreateMissingBond, SE_ClearMissingBond, FreeNode, NULL, NULL, NULL
};
/* Functions */

#define SE_DEF_UPPER	100
#define SE_DEF_LOWER	50
#define SE_DEF_CRIT	SE_PURGE_C

struct SE_Unit *SE_CurrUnit;	/* Current unit */

/* sets up default simulation */
void SE_Initialize()
{
  SE_Simulation.JHH[0]=0;
  SE_Simulation.JHH[1]=0;
  SE_Simulation.JHH[2]=0;
  SE_Simulation.JCH[0]=0;
  SE_Simulation.JCH[1]=0;
  SE_Simulation.JCH[2]=0;
  SE_Simulation.CntJHH[0]=0;
  SE_Simulation.CntJHH[1]=0;
  SE_Simulation.CntJHH[2]=0;
  SE_Simulation.CntJCH[0]=0;
  SE_Simulation.CntJCH[1]=0;
  SE_Simulation.CntJCH[2]=0;
  SE_Simulation.StrCnt=1;
  SE_Simulation.StrUpper=SE_DEF_UPPER;
  SE_Simulation.StrLower=SE_DEF_LOWER;
  SE_Simulation.StrActual=0;
  SE_Simulation.Criteria=SE_DEF_CRIT;
  /* SE_Simulation.Flags=SE_NO_CUTOFF;*/
  SE_Simulation.WorstFit=1e12;
  InitList( &(SE_Simulation.Units) );
  strcpy(SE_Simulation.Units.Head.Name,"Units Head");
  strcpy(SE_Simulation.Units.Tail.Name,"Units Tail");  
  InitList( &(SE_Simulation.Rules) );
  strcpy(SE_Simulation.Rules.Head.Name,"Rules Head");
  strcpy(SE_Simulation.Rules.Tail.Name,"Rules Tail");
  InitList( &(SE_Simulation.Linkages) );
  strcpy(SE_Simulation.Linkages.Head.Name,"Linkages Head");
  strcpy(SE_Simulation.Linkages.Tail.Name,"Linkages Tail");
  InitList( &(SE_Simulation.MissingBonds) );
  strcpy(SE_Simulation.MissingBonds.Head.Name,"Missing Bonds Head");
  strcpy(SE_Simulation.MissingBonds.Tail.Name,"Missing Bonds Tail");
}

struct PA_Command SE_Cmds[]={
  { "info",	SE_Info,   "info <text>",
    "Sets the info tag of the simulation to <text>."},
  { "unit",	SE_SetUnit,"unit <name> {",
    "Start unit specification mode. In this mode possible residues and their linkage positions can be specified."},
  { "purge",	SE_Purge,  "purge <lower> <upper> C|H|CH|HH",
    "This command determines when the generated list of structures should be purged.\nE.g.\npurge 50 150 C\nmeans that 50 structures will be saved and when 150 structures have been generated they purged until only the best 50 with best CFit remains."},
  { "jhh",	SE_JHH,    "jhh <small> <medium> <large>",
    "Sets the number of 3JHH coupling constants in the range <2Hz, 2-7Hz and >7Hz."},
  { "jch",	SE_JCH,    "jch <small> <medium> <large>",
    "Sets the number of 1JCH coupling constants in the range <169Hz (small) and >169Hz (large)."},
  { "rule",     SE_AddRule,   "rule <rulename>",
    "Specifies a required rule that has to be fulfilled, e.g. 'n glycan'."},
  { "link",     SE_AddLinkage, "link <FromUnit> <ToUnit> [<ToPos>]",
    "Specifies a linkage between two units. Position can also be specified if it is known.\nE.g.\nlink 'a' 'b'\nlink 'b' 'c' '4'"},

  { NULL,	NULL,      NULL,
    NULL}
};

struct PA_Command SE_UnitCmds[]={
  { "residue",	SE_SetResidue, "residue <residue> <pos> ... <certainty>",
    "Sets a possible residue for the unit.\nIf * is given as a residue all possible residues will be attempted.\nIf <certainty> is set to '*' all given positions do not have to be used. Otherwise all free positions will be used as binding positions."},
  { NULL,	NULL,          NULL,
    NULL}
};

/* SYNTAX: Info <text> */
int SE_Info()
{
  PA_GetString;
  strncpy(SE_Simulation.Info, PA_Result.String, SE_INFO_SIZE);
  return(PA_ERR_OK);
}

/* SYNTAX: Purge <lower> <upper> C|H|CH|HH|CL */
int SE_Purge()
{
  int mode;
  PA_GetFloat;
  SE_Simulation.StrLower=Max(TopValue.Float,1);
  PA_GetFloat;
  SE_Simulation.StrUpper=Max(TopValue.Float,2);
  PA_GetString;
  mode=(toupper(PA_Result.String[0])<<8)+toupper(PA_Result.String[1]);
  /* If no experimental shifts exist of the purge type use the next type
     instead. Otherwise sorting will not work and things will behave
     strangely. */
  switch (mode) {
  case SP_ID_PROTON:
    if(SE_Simulation.Experimental->HCnt>0)
      {
	SE_Simulation.Criteria=SE_PURGE_H;
	break;
      }
  case SP_ID_CARBON:
    if(SE_Simulation.Experimental->CCnt>0)
      {
	SE_Simulation.Criteria=SE_PURGE_C;
	break;
      }
    else if (SE_Simulation.Experimental->HCnt>0)
      {
	SE_Simulation.Criteria=SE_PURGE_H;
	break;
      }
  case SP_ID_2D_CH:
    if(SE_Simulation.Experimental->ChCnt>0)
      {
	SE_Simulation.Criteria=SE_PURGE_CH;
	break;
      }
  case SP_ID_2D_HH:
    if(SE_Simulation.Experimental->HhCnt>0)
      {
	SE_Simulation.Criteria=SE_PURGE_HH;
	break;
      }
    
  case SP_ID_TOT:
    SE_Simulation.Criteria=SE_PURGE_TOT;
    break;
  default:
    Error(PA_ERR_FAIL,"Unknown type of spectrum");
  };

  if (SE_Simulation.StrLower>SE_Simulation.StrUpper)
    {
      SE_Simulation.StrUpper=SE_Simulation.StrLower+1;
      Error(PA_ERR_WARN,"Lower limit can't exceed upper limit");
    };
  return(PA_ERR_OK);
}

/* SYNTAX: JHH <small> <medium> <large> */
int SE_JHH()
{
  PA_GetFloat;
  SE_Simulation.JHH[SE_JHH_SMALL]=TopValue.Float;
  PA_GetFloat;
  SE_Simulation.JHH[SE_JHH_MEDIUM]=TopValue.Float;
  PA_GetFloat;
  SE_Simulation.JHH[SE_JHH_LARGE]=TopValue.Float;
  return(PA_ERR_OK);
}

/* SYNTAX: JCH <small> <medium> <large> */
int SE_JCH()
{
  PA_GetFloat;
  SE_Simulation.JCH[SE_JCH_SMALL]=TopValue.Float;
  PA_GetFloat;
  SE_Simulation.JCH[SE_JCH_MEDIUM]=TopValue.Float;
  PA_GetFloat;
  SE_Simulation.JCH[SE_JCH_LARGE]=TopValue.Float;
  return(PA_ERR_OK);
}

/* SYNTAX: cutoff <max_c> <max_h> 
   int SE_Cutoff()
   {
   PA_GetFloat;
   SE_Simulation.CCutoff=TopValue.Float;
   PA_GetFloat;
   SE_Simulation.HCutoff=TopValue.Float;
   SE_Simulation.Flags|=SE_USE_CUTOFF;
   return(PA_ERR_OK);
   }
*/

/* Checks if two units are equal to each other. This can help reduce the number of permutation possibilities.
   If they are equal the function returns 1. Otherwise it returns 0. */
int SE_EquivalentUnits(struct SE_Unit *a, struct SE_Unit *b)
{
  struct SE_Residue *resA, *resB;
  if(ListLen(&a->Residues) != ListLen(&b->Residues))
    {
      return 0;
    }
  for(resA=(struct SE_Residue *)a->Residues.Head.Succ,
	resB=(struct SE_Residue *)b->Residues.Head.Succ;
      resA->Node.Succ!=NULL;
      resA=(struct SE_Residue *)resA->Node.Succ,
	resB=(struct SE_Residue *)resB->Node.Succ)
    {
      if(!SE_EquivalentResidues(resA, resB))
	{
	  return 0;
	}
    }
  return 1;
}
/* Checks if two residues are equal to each other.
   If they are equal the function returns 1. Otherwise it returns 0. */
int SE_EquivalentResidues(struct SE_Residue *a, struct SE_Residue *b)
{
  int i, n=0;

  if(a->Base!=b->Base)
    {
      return 0;
    }
  if(a->FreeCertainty!=b->FreeCertainty)
    {
      return 0;
    }
  for(i=0;i<TY_MAX_BONDS;i++)
    {
      if(a->Free[i]!=b->Free[i])
	{
	  return 0;
	}
    }

  if(n>1)
    {
      return 0;
    }

  return 1;
}

/* SYNTAX: generate <name> <exp> [/] { */
int SE_Generate()
{
  int stat;
  struct BU_Struct *exp;
  PA_GetString;
  strncpy(SE_Simulation.Name, PA_Result.String, SE_NAME_LENGTH);
  PA_GetString;
  if(PA_Result.String[0]=='/')
    {
      if(projectValid)
	{
	  if(!CC_GetNmrProject(&Global_CcpnProject,&Global_NmrProject))
	    {
	      Error(PA_ERR_FATAL,"Cannot open project.");
	    }
	}
      else
	{
	  Error(PA_ERR_FATAL,"Cannot open project.");
	}
      exp=(struct BU_Struct *)
	FindNode((struct Node *)&StructList,
		 ApiString_Get(Nmr_NmrProject_GetName(Global_NmrProject)));
   }
  else
    {
      exp=(struct BU_Struct *)
	FindNode((struct Node *)&StructList,PA_Result.String);
    }
  if (exp==NULL)
    {
      Error(PA_ERR_FAIL,"Experimental spectrum not found");
    }
  SE_Simulation.Experimental=exp;
  SE_Initialize();
  
  if(PA_Result.String[0]=='/')
    {
      /*      printf("Generates from CCPN project molecules.\n");*/
      CC_MakeSimulationSetupFromMolecules(&Global_CcpnProject, &SE_Simulation);
    }
  else
    {
      PA_GetArg();
    }

  /* change mode */
  stat=PA_Execute(SE_Cmds);

  /* back to normal */
  SE_Simulation.UnitCnt=ListLen(&SE_Simulation.Units);
  if(SE_Simulation.UnitCnt>0)
    {
      switch(SE_Simulation.Criteria)
	{
	case SE_PURGE_C:
	  if(SE_Simulation.Experimental->CCnt<=0)
	    {
	      if(SE_Simulation.Experimental->HCnt>0)
		{
		  SE_Simulation.Criteria=SE_PURGE_H;
		}
	      else if(SE_Simulation.Experimental->ChCnt>0)
		{
		  SE_Simulation.Criteria=SE_PURGE_CH;		  
		}
	      else if(SE_Simulation.Experimental->HhCnt>0)
		{
		  SE_Simulation.Criteria=SE_PURGE_HH;
		}
	      
	    }
	  break;
	case SE_PURGE_H:
	  if(SE_Simulation.Experimental->HCnt<=0)
	    {
	      if(SE_Simulation.Experimental->CCnt>0)
		{
		  SE_Simulation.Criteria=SE_PURGE_C;
		}
	      else if(SE_Simulation.Experimental->ChCnt>0)
		{
		  SE_Simulation.Criteria=SE_PURGE_CH;		  
		}
	      else if(SE_Simulation.Experimental->HhCnt>0)
		{
		  SE_Simulation.Criteria=SE_PURGE_HH;
		}
	      
	    }
	  break;
	case SE_PURGE_CH:
	  if(SE_Simulation.Experimental->ChCnt<=0)
	    {
	      if(SE_Simulation.Experimental->CCnt>0)
		{
		  SE_Simulation.Criteria=SE_PURGE_C;
		}
	      else if(SE_Simulation.Experimental->HCnt>0)
		{
		  SE_Simulation.Criteria=SE_PURGE_H;		  
		}
	      else if(SE_Simulation.Experimental->HhCnt>0)
		{
		  SE_Simulation.Criteria=SE_PURGE_HH;
		}
	      
	    }
	  break;
	}
      PE_Generate();
    }

  ME_EmptyList(&SE_UnitMethod, &(SE_Simulation.Units) );
  return(stat);
}


/* Do we need SE_CurrUnit???*/

/* SYNTAX: Unit <name> { */
int SE_SetUnit()
{
  int stat;
  PA_GetString;
  SE_CurrUnit=(struct SE_Unit *)
    ME_CreateNode(&SE_UnitMethod, &(SE_Simulation.Units),PA_Result.String);
  if (SE_CurrUnit==NULL) Error(PA_ERR_FATAL,"Out of memory");

  /* change mode */
  stat=PA_Execute(SE_UnitCmds);

  /* back to normal */
  return(stat);
}


/* Syntax: Residue <residue> <pos> ... <certainty> */
/* Adds a residue to the list */
int SE_SetResidue(char *string)
{
  struct SE_Residue *sres;
  struct RE_Residue *rres;
  int type;		/* result type */
  int i, j, pos, npos=0, linked=1, absconfloop=0;
  char *absconfposition;
  char resstring[NODE_NAME_LENGTH];
  char allpos[SE_MAX_RESIDUES][2];
  char restypes[32];
  int certaintyflag=TRUE;
  int knowntypes=FALSE;

  PA_GetString;

  strcpy(resstring,PA_Result.String);

  /* Added 060815 */
  /* This if/else statement should make it possible to try to add 
     all residues */
  if(resstring[0]!='*' && strncasecmp(resstring,"unknown", 7)!=0)
    {
      absconfposition=strchr(resstring,'?');
      if(absconfposition!=0)
	{
	  absconfloop=1;
	}
      for(i=0;i<=absconfloop;i++)
	{
	  if(absconfloop==1)
	    {
	      if(i==0)
		{
		  *absconfposition='D';
		}
	      else
		{
		  *absconfposition='L';
		}
	    }
	  rres=(struct RE_Residue *)FindNode((struct Node *)&ResidueList, resstring);
	  if (rres==NULL) Error(PA_ERR_FAIL,"Residue not found");
	  
	  certaintyflag=TRUE;
	  
	  if(npos<=0)
	    {
	      type=PA_GetArg(string);
	      if (type!=PA_STRING)
		Error(PA_ERR_FAIL,"At least one free position is required");
	      while (*string && (type==PA_STRING))
		{
		  /* If a '*' is given the list of positions are not certain. This is
		     the case e.g. when a methylation analysis has not been done. */
		  if(PA_Result.String[0]=='*')
		    {
		      certaintyflag=FALSE;
		    }
		  else
		    {
		      strcpy(allpos[npos++],PA_Result.String);
		    }
		  type=PA_GetArg(string);
		}
	    }
	  
	  sres=(struct SE_Residue *)
	    ME_CreateNode(&SE_ResidueMethod, &(SE_CurrUnit->Residues), SE_VOID_NAME);
	  if (sres==NULL)
	    {
	      Error(PA_ERR_FATAL,"Out of memory");
	    }

	  sres->Base=rres;
	  sres->Anomeric=rres->Shifts.Type->Anomeric;
	  sres->FreeCertainty=certaintyflag;
	  
	  sres->JCH=rres->JCH;
	  sres->JHH=rres->JHH;
	  
	  for (j=0;j<npos;j++)
	    {
	      pos=TY_FindAtom(rres->Shifts.Type,allpos[j]);
	      if (pos==TY_NOT_FOUND)
		{
		  ME_RemoveNode(&SE_ResidueMethod,
				(struct Node *) Last(SE_CurrUnit->Residues));
		  Error(PA_ERR_FAIL,"No such position");
		}
	      if (!(rres->Shifts.Type->Atom[pos].Type&TY_FREE_POS) &&
		  sres->FreeCertainty==TRUE)
		{
		  ME_RemoveNode(&SE_ResidueMethod,
				(struct Node *) Last(SE_CurrUnit->Residues));
		  Error(PA_ERR_FAIL,"Position can not be substituted");
		}
	      
	      sres->Free[pos]=SE_FREE;
	      /*	      printf("Linked at position %d\n",pos);*/
	      linked++;
	      /*	      printf("%s will be added.\n", sres->Base->Node.Name);*/
	      
	    }
	}
    }
  else
    {
      SE_Simulation.Flags|=SE_UNKNOWN_RES;
      if(strlen(resstring)>7)
	{
	  for(i=7;i<38 && resstring[i]!=0;i++)
	    {
	      restypes[i-7]=resstring[i];
	    }
	  restypes[i-7]=0;
	  knowntypes=TRUE;
	}

      type=PA_GetArg(string);
      if (type!=PA_STRING)
	Error(PA_ERR_FAIL,"At least one free position is required");
      while (*string && (type==PA_STRING))
	{
	  /* If a '*' is given the list of positions are not certain. This is
	     the case e.g. when a methylation analysis has not been done. */
	  if(PA_Result.String[0]=='*')
	    {
	      certaintyflag=FALSE;
	    }
	  else
	    {
	      strcpy(allpos[npos++],PA_Result.String);
	    }
	  type=PA_GetArg(string);
	}

      for(rres=(struct RE_Residue *)ResidueList.Head.Succ;rres->Node.Succ!=NULL;
	  rres=(struct RE_Residue *)rres->Node.Succ)
	{
	  /* If a certain residuetype was specified along with
	     unknown then check that it matches or skip this residue. */
	  if(knowntypes==TRUE &&
	     strncasecmp(restypes,rres->Node.Name,strlen(restypes))!=0 &&
	     strncasecmp(restypes,rres->Shifts.Type->Node.Name,strlen(restypes))!=0)
	    {
	      continue;
	    }
	  /*	  printf("%s\n",rres->CarbBank); */
	  if(linked!=0)
	    {
	      sres=(struct SE_Residue *)
		ME_CreateNode(&SE_ResidueMethod, &(SE_CurrUnit->Residues), SE_VOID_NAME);
	      linked=0;
	    }
	  if (sres==NULL) Error(PA_ERR_FATAL,"Out of memory");

	  sres->FreeCertainty=certaintyflag;
	  sres->Base=rres;
	  sres->Anomeric=rres->Shifts.Type->Anomeric;
	  
	  sres->JCH=rres->JCH;
	  sres->JHH=rres->JHH;

	  for (i=0;i<npos;i++)
	    {
	      pos=TY_FindAtom(rres->Shifts.Type,allpos[i]);
	      if (pos==TY_NOT_FOUND)
		{
		  continue;
		}
	      if (!(rres->Shifts.Type->Atom[pos].Type&TY_FREE_POS))
		{
		  continue;
		}

	      /* If there are no delta shifts for the position don't add it */
	      /*	      if (ListLen(&rres->Delta[pos])==0&&
		  pos!=rres->Shifts.Type->Anomeric&&
		  ListLen(&rres->Defaults[pos])==0)
		{
		  continue;
		}
	      */

	      sres->Free[pos]=SE_FREE;
	      /*	      printf("Linked at position %d\n",pos);*/
	      linked++;
	      /*	      printf("%s will be added.\n", sres->Base->Node.Name);*/

	    }
	  /* If there is only one residue there must be two links. This should
	     perhaps be moved to a place where it's known how many units there
	     are. Currently this function only checks if the residue is in the
	     first unit. */
	  /*	  if (linked==1 && (struct SE_Unit *)SE_CurrUnit==
	     (struct SE_Unit *)SE_Simulation.Units.Head.Succ)
	    {
	      linked=0;
	      }*/
	  if(sres->FreeCertainty==TRUE&&linked!=npos)
	    {
	      linked=0;
	    }
	}
      if(linked==0)
	{
	  ME_RemoveNode(&SE_ResidueMethod, (struct Node *) Last(SE_CurrUnit->Residues));
	}
    }

  return(PA_ERR_OK);
}

int SE_AddRule()
{
  struct RU_Rule *newRule, *addedRule;
  PA_GetString;

  newRule=(struct RU_Rule *)FindNode((struct Node *)&RuleList, PA_Result.String);
  if (newRule==NULL)
    {    
      Error(PA_ERR_FAIL,"Rule not found");
    }
  addedRule=(struct RU_Rule *)
    ME_CreateNode(&RU_RuleMethod, &(SE_Simulation.Rules),PA_Result.String);
  if (addedRule==NULL)
    {
      Error(PA_ERR_FATAL,"Out of memory");
    }
  addedRule->Patterns.Head=newRule->Patterns.Head;
  addedRule->Residues.Head=newRule->Residues.Head;

  return (PA_ERR_OK);
}

/* This function adds a linkage specification. This is useful for specifying
   residues that are known to link to each other or e.g. for adding a
   substituent (OAc, P, S etc.) to a specific residue. The linkage positions
   do not have to be specified if they are not known. */
int SE_AddLinkage()
{
  char FromUnitName[32], ToUnitName[32], ToPosName[4];
  int pos;
  struct SE_Linkage *link;
  struct SE_Residue *res;

  PA_GetString;
  strcpy(FromUnitName,PA_Result.String);

  PA_GetString;
  strcpy(ToUnitName,PA_Result.String);

  link=(struct SE_Linkage *)ME_CreateNode(&SE_LinkageMethod, &(SE_Simulation.Linkages), SE_VOID_NAME);

  link->FromUnit=(struct SE_Unit *)FindNode((struct Node *)&SE_Simulation.Units, FromUnitName);
  if(link->FromUnit==NULL)
    {
      ME_RemoveNode(&SE_LinkageMethod, (struct Node*) link);
      Error(PA_ERR_FAIL,"FromUnit not found.");
    }

  link->ToUnit=(struct SE_Unit *)FindNode((struct Node *)&SE_Simulation.Units, ToUnitName);
  if(link->ToUnit==NULL)
    {
      ME_RemoveNode(&SE_LinkageMethod, (struct Node*) link);
      Error(PA_ERR_FAIL,"ToUnit not found.");
    }

  if(PA_GetArg()==PA_STRING)
    {
      strcpy(ToPosName,PA_Result.String);
      for(res=(struct SE_Residue *)link->FromUnit->Residues.Head.Succ;
	  res->Node.Succ!=NULL;
	  res=(struct SE_Residue *)res->Node.Succ)
	{
	  pos=res->Anomeric;
	  if(pos==TY_NOT_FOUND)
	    {
	      ME_RemoveNode(&SE_LinkageMethod, (struct Node*) link);
	      Error(PA_ERR_FAIL,"FromPos does not exist.");
	    }
	  if(link->FromPos!=TY_VOID_ATOM && link->FromPos!=pos)
	    {
	      ME_RemoveNode(&SE_LinkageMethod, (struct Node*) link);
	      Error(PA_ERR_FAIL,"Atom names are different between residues in the unit. Cannot add linkage specification.");
	    }
	  if(!(res->Base->Shifts.Type->Atom[pos].Type&TY_FREE_POS) ||
	     !(res->Base->Shifts.Type->Atom[pos].Type&TY_ANOMERIC))
	    {
	      ME_RemoveNode(&SE_LinkageMethod, (struct Node*) link);
	      Error(PA_ERR_FAIL,"This position cannot be linked from in all residues in the unit.");
	    }
	  link->FromPos=pos;
	}

      for(res=(struct SE_Residue *)link->ToUnit->Residues.Head.Succ;
	  res->Node.Succ!=NULL;
	  res=(struct SE_Residue *)res->Node.Succ)
	{
	  pos=TY_FindAtom(res->Base->Shifts.Type,ToPosName);
	  if(pos==TY_NOT_FOUND)
	    {
	      ME_RemoveNode(&SE_LinkageMethod, (struct Node*) link);
	      Error(PA_ERR_FAIL,"ToPos does not exist.");
	    }
	  if(link->ToPos!=TY_VOID_ATOM && link->ToPos!=pos)
	    {
	      ME_RemoveNode(&SE_LinkageMethod, (struct Node*) link);
	      Error(PA_ERR_FAIL,"Atom names are different between residues in the unit. Cannot add linkage specification.");
	    }
	  if(!(res->Base->Shifts.Type->Atom[pos].Type&TY_FREE_POS) ||
	     res->Base->Shifts.Type->Atom[pos].Type&TY_ANOMERIC)
	    {
	      ME_RemoveNode(&SE_LinkageMethod, (struct Node*) link);
	      Error(PA_ERR_FAIL,"This position cannot be linked to in all possible residues in this unit.");
	    }
	  link->ToPos=pos;
	}
    }



  return (PA_ERR_OK);
}

/*
void SE_PurgeInvalid()
{
  struct SE_Unit *unit, *cmpunit;
  struct SE_Residue *residue, *cmpresidue;
  int i;

  for(unit=(struct SE_Unit *)SE_Simulation.Units.Head.Succ;
      unit!=SE_Simulation.Units.Tail;unit=(struct SE_Unit *)unit.Succ)
    {
      if(ListLen(&unit->Residues)>2)
	{
	  for(residue=(struct SE_Residue *)unit->Residues.Head.Succ;
	      residue!=unit->Residues.Tail.Pred;
	      residue=(struct SE_Residue *)residue.Succ)
	    {
	      for(cmpunit=(struct SE_Unit *)SE_Simulation.Units.Head.Succ;
		  cmpunit!=SE_Simulation.Units.Tail;
		  cmpunit=(struct SE_Unit *)cmpunit.Succ)
		{
		  if(cmpunit==unit||ListLen(&cmpunit->Residues)>2)
		    continue;
		  for(cmpresidue=(struct SE_Residue *)cmpunit->Residues.Head.Succ;
		      cmpresidue!=cmpunit->Residues.Tail.Pred;
		      cmpresidue=(struct SE_Residue *)cmpresidue.Succ)
		    {
		      for(i=0;i<TY_MAX_BONDS;i++)
			{
			  if(residue->Free[i]==SE_FREE)
			    {
			      FindNode(&(residue
			    }
			}
		    }
		}
	    }
	}
    }
}
*/
      
