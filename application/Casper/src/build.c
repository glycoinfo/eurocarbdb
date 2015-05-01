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

/* Filename = build.c */
/* Prefix = BU */
/* Internal structure of oligo/polymers */
/* Used for building and simulating spectra */

#include "build.h"
#include "calc.h"
#include "parser.h"
#include "gfx.h"
#include "delta.h"
#include "glyco.h"
#include "shifts.h"
#include "spectra.h"
#include "ccpn.h"
#include "type.h"
#include "residue.h"
#include "variables.h"
#include "memops/general/Util.h"
#include <ctype.h>
#include <string.h>

struct List StructList={
  {NULL, &(StructList.Tail), "Struct Head"},
  {&(StructList.Head), NULL, "Struct Tail"}
};

/* Methods for handling 'units' */

void *BU_CreateUnit(char *name)
{
  return ( MakeNode(sizeof(struct BU_Unit),name) );
}

void BU_ClearUnit(struct BU_Unit *unit)
{
  int i;
  unit->Residue=NULL; /* ?? */

  for (i=0; i<TY_MAX_HEAVY; i++) 
    {
      unit->Subst[i]=NULL;
    }

  for (i=0; i<TY_MAX_CARBON; i++)
    {
      unit->Shifts.C[i]=BU_VOID_SHIFT;
      unit->Shifts.H[i][0]=BU_VOID_SHIFT;
      unit->Shifts.H[i][1]=BU_VOID_SHIFT;
    }

  unit->Error=0;
  unit->HError=0;
  unit->CError=0;
  unit->Position=TY_VOID_ATOM;
  unit->CcpnUnitNr=-1;
}

struct ME_Method UnitMethod={
  BU_CreateUnit, BU_ClearUnit, FreeNode, NULL,
  NULL, NULL
};

/* Methods for handling 'structures' */

void *BU_CreateStruct(char *name)
{
  return ( MakeNode(sizeof(struct BU_Struct),name) );
}

void BU_ClearStruct(struct BU_Struct *structure)
{
  structure->Error=0;
  structure->TotFit=0;
  structure->CFit=0;
  structure->HFit=0;
  structure->ChFit=0;
  structure->HhFit=0;
  
  structure->CCnt=0;
  structure->HCnt=0;
  structure->HhCnt=0;
  structure->ChCnt=0;
  
  structure->CSysErr=0;
  structure->HSysErr=0;
  structure->CCorrection=0;
  structure->HCorrection=0;
  structure->JCH[RE_SMALL]=0;
  structure->JCH[RE_MEDIUM]=0;
  structure->JCH[RE_LARGE]=0;
  structure->JHH[RE_SMALL]=0;
  structure->JHH[RE_MEDIUM]=0;
  structure->JHH[RE_LARGE]=0;
  InitList(&(structure->Units));
  strcpy(structure->Units.Head.Name,"Units Head");
  strcpy(structure->Units.Tail.Name,"Units Tail");
  structure->Info[0]=0;
  structure->CcpnChainCode[0]=0;

  /*
    for (i=0;i<BU_MAX_SHIFTS;i++)
    {
      structure->CShift[i]=0;
      structure->HShift[i]=0;
      structure->ChCShift[i]=0;
      structure->ChHShift[i]=0;
      structure->HhLoShift[i]=0;
      structure->HhHiShift[i]=0;
      
      for (j=0;j<2;j++)
	{
	  structure->CAssignments[j][i]=0;
	  structure->HAssignments[j][i]=0;
	}
    }
  */
  structure->nCAssignments=0;
  structure->nHAssignments=0;
  structure->nUnassigned=0;
  structure->fractionAssigned=0;
}

void *BU_FreeStruct(struct BU_Struct *structure)
{
  ME_EmptyList(&UnitMethod, &structure->Units );
  return(FreeNode((struct Node *)structure));
}


/* Prints name, info, info about fits and a representation of the structure. */
void BU_ListStruct(struct BU_Struct *structure)
{
  struct VA_Variable *var;

  printf("%s",structure->Node.Name);
  if (structure->Info!=NULL)
    printf("\t%s",structure->Info);
  if (structure->TotFit>0)
    printf("\tdTot=%.2f",structure->TotFit);
  if (structure->CFit>0)
    printf("\tdC=%.2f (%.2f)", structure->CFit, structure->CFit/structure->CCnt);
  if (structure->HFit>0)
    printf("\tdH=%.2f (%.2f)", structure->HFit, structure->HFit/structure->HCnt);
  if (structure->ChFit>0)
    printf("\tdCH=%.2f (%.2f)", structure->ChFit, structure->ChFit/structure->ChCnt);
  if (structure->HhFit>0)
    printf("\tdHH=%.2f (%.2f)", structure->HhFit, structure->HhFit/structure->HhCnt);
  
  if (structure->TotFit>0)
  printf("\tAssignment completeness: %.2f%%",structure->fractionAssigned*100);

  printf("\n\t");
  GFX_StructGfx(structure, NULL, 0);
  /* If BU_PrintCT is set print the structure also in Glyco-CT format */
  var=(struct VA_Variable *)FindNode(&(VariableList.Head), "printct");
  if(var!=NULL && var->Value.Value.Float>0 )
    {
      GL_printstr(structure);
    }
  printf("\n");
}


/* Prints name, info, info about fits and a representation of the structure. Thereafter prints each unit at a
   time along with the errors and the spectra corresponding to it. */
void BU_PrintStruct(struct BU_Struct *structure)
{
  int i, j, flag, substLinked, anomerPos;
  struct BU_Unit *unit;
  struct VA_Variable *var;

  printf("%s",structure->Node.Name);
  if (structure->Info!=NULL)
    printf("\t%s",structure->Info);
  if (structure->CFit>0)
    printf("\tdC=%.2f (%.2f)", structure->CFit, structure->CFit/structure->CCnt);
  if (structure->HFit>0)
    printf("\tdH=%.2f (%.2f)", structure->HFit, structure->HFit/structure->HCnt);
  if (structure->ChFit>0)
    printf("\tdCH=%.2f (%.2f)", structure->ChFit, structure->ChFit/structure->ChCnt);
  if (structure->HhFit>0)
    printf("\tdHH=%.2f (%.2f)", structure->HhFit, structure->HhFit/structure->HhCnt);
  
  if (structure->TotFit>0)
  printf("\tAssignment completeness: %.2f%%",structure->fractionAssigned*100);
  printf("\n");

  /* If BU_PrintCT is set print the structure in Glyco-CT and CASPER format */
  var=(struct VA_Variable *)FindNode(&(VariableList.Head), "printct");
  if(var!=NULL && var->Value.Value.Float>0 )
    {
      GFX_StructGfx(structure, NULL, 0);
      GL_printstr(structure);
    }
  /* Otherwise print it the CASPER way */
  else
    {
      GFX_StructGfx(structure, NULL, 0);
    }

  for (unit=(struct BU_Unit *)structure->Units.Head.Succ;
       unit->Node.Succ!=NULL; unit=(struct BU_Unit *)unit->Node.Succ)
    {
      /* If this is just a substituent rather than a sugar residue don't print it as a separate
	 entry. Print it together with the residue instead */
      if(strcasecmp(unit->Residue->Shifts.Type->CTsuperclass,"subst")==0)
	{
	  continue;
	}
      printf("\n");
      flag=0;
      anomerPos=BU_ANOMER_POS(unit);
      for (i=0; i<unit->Shifts.Type->HeavyCnt; i++)
	{
	  if (i==anomerPos) continue;
	  if (unit->Subst[i]!=NULL)
	    {
	      if(strcasecmp(unit->Subst[i]->Residue->Shifts.Type->CTsuperclass,"subst")==0)
		{
		  substLinked=0;
		  for(j=0;j<unit->Subst[i]->Residue->Shifts.Type->HeavyCnt;j++)
		    {
		      if(unit->Subst[i]->Subst[j]!=0 && unit->Subst[i]->Subst[j]!=unit)
			{
			  substLinked=1;
			}
		    }
		  if(substLinked==0)
		    {
		      continue;
		    }
		}
	      if (flag)
		printf(",%s", unit->Shifts.Type->Atom[i].Label);
	      else 
		printf ("->%s", unit->Shifts.Type->Atom[i].Label);
	      flag=1;
	    }
	}
      if (flag) printf(")");
      /*  printf("%.10s[%s]", */
      /*      printf("%s[%s]",
	      unit->Residue->Node.Name, unit->Node.Name);*/
      GFX_PrintUnit(unit, 0, 0, 1);

      if (anomerPos!=-1 && unit->Subst[anomerPos]!=NULL)
	printf("(%s->",
	       unit->Shifts.Type->Atom[anomerPos].Label);
      printf(" (error est. %0.2f, actual ", unit->Error);
      printf("%0.2f C; ",unit->CError);
      printf("%0.2f H; ",unit->HError);
      /*      printf("%0.2f CH; ",unit->CHError);
      printf("%0.2f HH; ",unit->HHError);
      */
      printf(")\n");
      
      SH_PrintShiftsWithSubstituents(unit);
    }
}

/* Copies the contents of two structures without affecting the Unit element. */
void CopyStructInfo(struct BU_Struct *a, struct BU_Struct *b)
{
  int i,j;

  strcpy(b->Node.Name, a->Node.Name);
  strcpy(b->Info, a->Info);
  b->Type=a->Type;
  b->Error=a->Error;
  b->CFit=a->CFit;
  b->HFit=a->HFit;
  b->ChFit=a->ChFit;
  b->HhFit=a->HhFit;
  b->CCnt=a->CCnt;
  b->HCnt=a->HCnt;
  b->ChCnt=a->ChCnt;
  b->HhCnt=a->HhCnt;
  strcpy(b->CcpnChainCode, a->CcpnChainCode);
  /*  b->Units.Head=a->Units.Head;
      b->Units.Tail=a->Units.Tail; */
  /*  b->Units=a->Units; */
  /*  InitList(b->Units);
  for (tempa=a->Units.Head.Succ;tempa!=&a->Units.Tail;tempa=(struct Node *)tempa->Succ)
    {
      tempb=BU_CreateUnit(tempa->Name);
      (struct BU_Unit *)tempb=(struct BU_Unit *)tempa;
      }*/
  for (i=0;i<3;i++)
    {
      b->JHH[i]=a->JHH[i];
      b->JCH[i]=a->JCH[i];
    }
  for (i=0;i<BU_MAX_SHIFTS;i++)
    {
      b->CShift[i]=a->CShift[i];
      b->HShift[i]=a->HShift[i];
      b->ChCShift[i]=a->ChCShift[i];
      b->ChHShift[i]=a->ChHShift[i];
      b->HhLoShift[i]=a->HhLoShift[i];
      b->HhHiShift[i]=a->HhHiShift[i];
      
      for(j=0;j<2;j++)
	{
	  b->CAssignments[j][i]=a->CAssignments[j][i];
	  b->HAssignments[j][i]=a->HAssignments[j][i];
	}
    }
  
}

struct ME_Method StructMethod={
  BU_CreateStruct, BU_ClearStruct, BU_FreeStruct,
  BU_ListStruct, BU_PrintStruct, /* BU_SaveStruct */ NULL
};

/* Build related commands. These can only be used inside { } after the build command. */
struct PA_Command BU_Cmds[]={
  { "info",	BU_Info,    "info <text>",
    "Sets a string of information about the structure.\nIf the string is more than one word it must be contain within ''.\nExample:\tinfo 'This is a structure'"},
  { "unit",	BU_SetUnit, "unit <label> <residue>",
    "Creates a unit called <label> and sets it to <residue>.\nExample:\tunit a bDGlcOMe\n\t\tunit b bdglc"},
  { "link",	BU_Link,    "link '<unit1>(<pos1>-><pos2>)<unit2>'",
    "Defines a link between <unit1> and <unit2>.\nThe link is created from <pos1> in <unit1> to <pos2> in <unit2>.\nExample:\tlink 'b(1->4)a'\t\tequals\t\tlink 'b(->4)a'"},
  { "ln",	BU_LineNotation, "ln <description>",
    ""}, /* Is present in ny_build.c */
  { "glycoinput", GL_GlycoInput, "glycoinput *<Glyco-CT structure block>*",
    "Builds a CASPER structure bases on a Glyco-CT structure. Can be used for importing structures."},
  { NULL,	NULL,       NULL,
    NULL}
};

/* --- END OF METHODS --- */

struct BU_Struct *BU_Struct;
struct BU_Unit *BU_Unit;


/* This function prints a URL to glycam online carbohydrate builder
   generating a 3D structure (e.g. PDB) of the specified structure.
   SYNTAX: print3D <str> */
int BU_PrintStruct3DLink()
{
  struct BU_Struct *str;
  struct VA_Variable *var;

  PA_GetString;
  str=(struct BU_Struct *)
    FindNode(&(StructList.Head), PA_Result.String);

  if(!str)
    {
      Error(PA_ERR_FAIL,"Structure not found");
    }

  var=(struct VA_Variable *)FindNode(&(VariableList.Head), "Print3D");

  if (var==NULL)
    {
      var=(struct VA_Variable *)ME_CreateNode(&VariableMethod,&VariableList, "Print3D");
    }
  var->Value.Value.Float=1;
  var->Value.Type=PA_FLOAT;

  printf("http://www.glycam.com/CCRC/carbohydrates/cb_cnpSequences.jsp?projectName=casper&text1=");
  GFX_StructGfx(str, NULL, 0);

  FreeNode((struct Node *)var);
  return (PA_ERR_OK);
}

/* Creates a stucture called <name> and executes the build commands within { } */
/* Thereafter 1D and 2D spectra for the stucture are calculated. */
/* SYNTAX: Build <name> { ... } */
int BU_SetStruct()
{
  char name[NODE_NAME_LENGTH], numberedName[NODE_NAME_LENGTH];
  int stat, i, multiBuildFlag=0;

  if(strncasecmp(PA_Status->CmdLine, "multibuild", PA_Status->CmdPtr-PA_Status->CmdLine)==0)
    {
      multiBuildFlag=1;
    }

  PA_GetString;
  strcpy(name, PA_Result.String);

  BU_Struct=(struct BU_Struct *)
    ME_CreateNode(&StructMethod, &StructList, name);
  if (BU_Struct==NULL)
    {
      Error(PA_ERR_FATAL,"Out of memory");
    }

  if(projectValid && !Global_CcpnProject)
    {
      Global_CcpnProject = newCcpnProject("exp");
      if(!Global_CcpnProject)
	{
	  printf("Could not create CCPN project.\n");
	  printRaisedException();
	  return(EXIT_FAILURE);
	}
    }
  if(projectValid && !CC_GetNmrProject(&Global_CcpnProject,&Global_NmrProject))
    {
      projectValid=0;
      Error(PA_ERR_FATAL,"Cannot open project.");
    }
  if(!multiBuildFlag)
    {
      PA_GetArg();
    }
  if(multiBuildFlag || PA_Result.String[0]=='/')
    {
      if(projectValid)
	{
	  if(CC_ResiduetoUnit(&Global_CcpnProject, BU_Struct, multiBuildFlag))
	    {
	      stat=PA_ERR_OK;
	    }
	  else
	    {
	      stat=99;
	      projectValid=0;
	    }
	}
    }
  /* change mode */
  else
    {
      stat=PA_Execute(BU_Cmds);
    }
  i=1;
  while(BU_Struct)
    {
      BU_RemoveOrphanUnits(BU_Struct);
      GFX_RenameUnits(BU_Struct);
      QuickSort(BU_Struct->Units.Head.Succ,BU_Struct->Units.Tail.Pred,NameCompare,0);
  
      if(CA_CalcAllGlycosylations(BU_Struct)!=PA_ERR_OK)
	{
	  Error(PA_ERR_FAIL, "No data for at least one bond when creating repetition");
	}  
      if(!multiBuildFlag && PA_Result.String[0]!='/')
	{
	  if(projectValid)
	    {
	      if(!CC_MakeMoleculeFromBU_Struct(&Global_CcpnProject, BU_Struct, name))
		{
		  stat=100;
		  projectValid=0;
		}
	    }
	}
      /* calculate all spectra - is this the right place? */
      SP_Calc1DSpectra(BU_Struct);
      SP_Calc2DSpectra(BU_Struct);
      

      if(stat==99)
	{
	  Error(PA_ERR_FATAL,"Cannot build the unit.");
	}
      if(stat==100)
	{
	  Error(PA_ERR_FAIL,"Cannot create the CCPN molecule for this structure. Could be caused by missing data, wrong linkages, substituent errors or chains not found.");
	}
      if(multiBuildFlag)
	{
	  sprintf(numberedName, "%s%d", name, ++i);
	  BU_Struct=FindNode((struct Node *)&StructList, numberedName);
	}
      else
	{
	  break;
	}
    }
  
  return(stat);
}

/* Sets the info string of the structure to <text> */
/* SYNTAX: Info <text> */
int BU_Info()
{
  PA_GetString;
  strncpy(BU_Struct->Info, PA_Result.String, BU_INFO_SIZE);
  return(PA_ERR_OK);
}

/* Creates a unit called <label> and sets it to <residue>. */
/* SYNTAX: Unit <label> <residue> */
int BU_SetUnit()
{
  char name[10];
  struct RE_Residue *residue;
  PA_GetString;  /* Gets <label> */
  strncpy(name, PA_Result.String, 10);
  PA_GetString;  /* Gets <residue> */
  residue=(struct RE_Residue *)FindNode((struct Node *)&ResidueList, PA_Result.String);
  if (residue==NULL) Error(PA_ERR_FAIL,"Residue not found");
  BU_Unit=(struct BU_Unit*)ME_CreateNode(&UnitMethod, &(BU_Struct->Units), name);
  if (BU_Unit==NULL) Error(PA_ERR_FATAL,"Out of memory");
  switch (residue->JCH)
    {
    case RE_SMALL:	BU_Struct->JCH[RE_SMALL]++;
      break;
    case RE_MEDIUM:	BU_Struct->JCH[RE_MEDIUM]++;
      break;
    case RE_LARGE:	BU_Struct->JCH[RE_LARGE]++;
      break;
    };
  switch (residue->JHH)
    {
    case RE_SMALL:	BU_Struct->JHH[RE_SMALL]++;
      break;
    case RE_MEDIUM:	BU_Struct->JHH[RE_MEDIUM]++;
      break;
    case RE_LARGE:	BU_Struct->JHH[RE_LARGE]++;
      break;
    };

  SH_RAW_COPY(&(residue->Shifts), &(BU_Unit->Shifts));  /* Copy shifts from the residue to the new unit. */
  BU_Unit->Residue=residue;
  BU_Unit->Shifts.Type=residue->Shifts.Type;
  BU_Unit->Error=residue->Error;
  return(PA_ERR_OK);
}

/* extracts linkage info from a string */
/* <unit1>(<pos1>-><pos2>)<unit2> */
int BU_ReadLink(struct BU_LinkDesc *desc, char *string)
{
  int i, anomerPos;
  char unit[NODE_NAME_LENGTH];
  char atom[NODE_NAME_LENGTH];

  /* read first unit */
  for (i=0; isalnum(*string) && (i<NODE_NAME_LENGTH); i++)
    { 
      unit[i]=*string;
      string++;
    }
  unit[i]=0;
  if (*string!='(') 
    {
      Error(PA_ERR_FAIL,"Expecting (");
    }
  string++;

  /* See that first unit exists */
  desc->Unit[DE_NONRED]=(struct BU_Unit *)FindNode((struct Node *)&BU_Struct->Units, unit);
  if ( desc->Unit[DE_NONRED]==NULL)
    {
      Error(PA_ERR_FAIL, "First unit not found");
    }
  
  anomerPos=BU_ANOMER_POS(desc->Unit[DE_NONRED]);
  if(anomerPos==-1)
    {
      Error(PA_ERR_FAIL, "First unit does not have an anomeric position. Cannot link.");
    }
  /* Read the 'from' linkage position into a string */
  for (i=0; isalnum(*string) && (i<NODE_NAME_LENGTH); i++)
    {
      atom[i]=*string;
      string++;
    }
  atom[i]=0;

  if (i==0)
    {
      desc->Atom[DE_NONRED]=anomerPos;
    }
  else
    {
      desc->Atom[DE_NONRED]=TY_FindAtom(desc->Unit[DE_NONRED]->Shifts.Type, atom);
      if (desc->Atom[DE_NONRED]==TY_NOT_FOUND)
	{
	  Error(PA_ERR_FAIL, "Specified anomeric position not found.");
	}
      if (desc->Atom[DE_NONRED]!=BU_ANOMER_POS(desc->Unit[DE_NONRED]))
	{
	  Error(PA_ERR_FAIL, "Specified 'from' position is not anomeric.");
	}
    }
  if (*string!='-')
    {
      Error(PA_ERR_FAIL, "Expecting -");
    }
  string++;
  if (*string=='>')
    {
      string++;
    }
  /* Read the second linkage position */
  for (i=0; (isalnum(*string)) && (i<NODE_NAME_LENGTH); i++)
    {
      atom[i]=*string; string++;
    }
  atom[i]=0;
  
  if (*string!=')')
    {
      Error(PA_ERR_FAIL, "Expecting )");
    }
  string++;

  /* second unit */
  for (i=0; (isalnum(*string)) && (i<NODE_NAME_LENGTH); i++)
    {
      unit[i]=*string; string++;
    }
  unit[i]=0;
  if (*string)
    {
      Error(PA_ERR_FAIL, "Expecting second unit");
    }
  desc->Unit[DE_REDUCING]=(struct BU_Unit *)FindNode((struct Node *)&BU_Struct->Units, unit);
  if (desc->Unit[DE_REDUCING]==NULL)
    {
      Error(PA_ERR_FAIL, "Second unit not found");
    }
  desc->Atom[DE_REDUCING]=TY_FindAtom(desc->Unit[DE_REDUCING]->Shifts.Type, atom);
  if (desc->Atom[DE_REDUCING]==TY_NOT_FOUND)
    {
      Error(PA_ERR_FAIL, "Second position not found");
    }
  return(PA_ERR_OK);
}

/* SYNTAX: Link '<unit1>(<pos1>-><pos2>)<unit2>' */
int BU_Link()
{
  struct BU_LinkDesc desc;
  int stat;
  PA_GetString;
  stat=BU_ReadLink(&desc, PA_Result.String);
  if (stat!=PA_ERR_OK) return(stat);
 
  if (!(desc.Unit[DE_REDUCING]->Shifts.Type->Atom[desc.Atom[DE_REDUCING]].Type & TY_FREE_POS))
    {
      Error(PA_ERR_FAIL,"Reducing end can not be substituted");
    }
  if (!(desc.Unit[DE_NONRED]->Shifts.Type->Atom[desc.Atom[DE_NONRED]].Type & TY_FREE_POS))
    {
      Error(PA_ERR_FAIL,"Anomeric position cannot be substituted");
    }
  desc.Unit[DE_NONRED]->Subst[desc.Atom[DE_NONRED]]=desc.Unit[DE_REDUCING];
  desc.Unit[DE_REDUCING]->Subst[desc.Atom[DE_REDUCING]]=desc.Unit[DE_NONRED];
  desc.Unit[DE_NONRED]->Position=desc.Atom[DE_REDUCING];
  if(desc.Atom[DE_REDUCING]==BU_ANOMER_POS(desc.Unit[DE_REDUCING]))
    {
      desc.Unit[DE_REDUCING]->Position=desc.Atom[DE_NONRED];
    }

  /* Calculate spectrum */
  /*  stat=CA_SimDisacch(desc.Unit[DE_NONRED], desc.Atom[DE_NONRED],
		     desc.Atom[DE_REDUCING],  desc.Unit[DE_REDUCING]);
  if (stat!=PA_ERR_OK)
    {
      Error(PA_ERR_FAIL, "No data for this bond");
      }*/
  return(PA_ERR_OK);
}



/* From ny_build.c */



/* 'Global' variables */
int BULN_Unit;		/* Auto-name of unit */
char *BULN_String;	/* LN string */
char *BULN_Pos;		/* Position in string */

/* reads from '[' to ']' */
char *BU_GetUnitName(char *string)
{
  static char name[NODE_NAME_LENGTH];
  int i=0;
  string++;	/* first character should be '[' */
  while ((string[i]!=']')&&(i<NODE_NAME_LENGTH))
    { name[i]=string[i]; i++; };
  name[i]=0;
  return (name);
}

/* reads a residue name */
struct RE_Residue *BU_GetResidue(char *string)
{
  char name[NODE_NAME_LENGTH];
  int i=0;
  while (isalnum(string[i])&&(i<NODE_NAME_LENGTH))
    { name[i]=string[i]; i++; };
  name[i]=0;
  /* now search for residue */
  return((struct RE_Residue *)FindNode((struct Node *)&ResidueList, name));
}

/* add new unit to structure */
struct BU_Unit *BU_CreateLNUnit()
{
  char /*added*/*u_name;				/* unit name */
  char a_name[2];			/* auto-name */
  struct BU_Unit *current;	/* current unit */
  struct RE_Residue *residue;

  a_name[0]=BULN_Unit+'a';
  a_name[1]=0;
  u_name=NULL;	/* invalid name */

  /* go past name & read it */
  if (*BULN_Pos==']')
    {
      while ((*BULN_Pos!='[')&&(BULN_Pos>BULN_String)) BULN_Pos--;
      u_name=BU_GetUnitName(BULN_Pos);
      BULN_Pos--;
    }
  else
    u_name=a_name;

  /* go past last residue and read it */
  while (isalnum(*BULN_Pos)&&(BULN_Pos>=BULN_String)) BULN_Pos--;
  residue=BU_GetResidue(BULN_Pos+1);
  if (residue==NULL) return(NULL);
  /* now create the corresponding unit */

  current=(struct BU_Unit*)ME_CreateNode(&UnitMethod, &(BU_Struct->Units), u_name);

  if (current==NULL) return(NULL);
  switch (residue->JCH)
    {
    case RE_SMALL:	BU_Struct->JCH[RE_SMALL]++;	break;
    case RE_MEDIUM:	BU_Struct->JCH[RE_MEDIUM]++; break;
    case RE_LARGE:	BU_Struct->JCH[RE_LARGE]++; break;
    };
  switch (residue->JHH)
    {
    case RE_SMALL:	BU_Struct->JHH[RE_SMALL]++; break;
    case RE_MEDIUM:	BU_Struct->JHH[RE_MEDIUM]++; break;
    case RE_LARGE:	BU_Struct->JHH[RE_LARGE]++; break;
    };
  SH_RAW_COPY(&(residue->Shifts), &(current->Shifts));
  current->Residue=residue;
  current->Shifts.Type=residue->Shifts.Type;
  current->Error=residue->Error;
  BULN_Unit++;
  return(current);
}

/* reads a linkage position (at reducing end) */
/* the anomeric position is discarded */
int BU_GetLinkage(struct RE_Residue *residue, char *string)
{
  static char name[NODE_NAME_LENGTH];
  int i, atom;
  /* go past anomeric position and '-'/'>' */
  i=0;		/* prevent bad input from hanging program */
  while ((i++<255)&&(*string!='-')) string++;
  string++;
  i=0;
  if (*string=='>') string++;
  while ((string[i]!=')')&&(i<NODE_NAME_LENGTH))
    { name[i]=string[i]; i++; };
  name[i]=0;
  atom=TY_FindAtom(residue->Shifts.Type, name);
 
  if (!(residue->Shifts.Type->Atom[atom].Type & TY_FREE_POS))
    return(TY_NOT_FOUND);

  return(atom);
}

/* Do a single backbone-residue (with branches) */
struct BU_Unit *BU_Backbone(struct BU_Unit *current)
{
  struct BU_Unit *preceeding, *bp;
  int to_pos, from_pos;

  /* next character may be ']' or ')' */
  while (*BULN_Pos==']'&&(BULN_Pos>BULN_String))	/* first do all branches */
    {
      BULN_Pos--;
      bp=current;
      while (*BULN_Pos!='['&&(BULN_Pos>BULN_String))	/* do all residues in branch */
	{
	  bp=BU_Backbone(bp);
	  if (bp==NULL)
	    {
	      printf("Branch fouled up\n");
	      return(NULL);
	    };
	};
      BULN_Pos--;
    };
  /* then do next backbone residue */
  if (*BULN_Pos==')')
    {
      while ((*BULN_Pos!='(')&&(BULN_Pos>BULN_String)) BULN_Pos--;
      /* get linked residue */
      if (*BULN_Pos=='(')
	{
	  /* get linkage */
	  while ((*BULN_Pos!='(')&&(BULN_Pos>BULN_String)) BULN_Pos--;
	  to_pos=BU_GetLinkage(current->Residue, BULN_Pos);
	  if (to_pos==TY_NOT_FOUND)
	    {
	      printf("Error in linkage\n");
	      return(NULL);
	    };
	  /* get 'previous residue' */
	  BULN_Pos--;

	  preceeding=BU_CreateLNUnit();

	  if (preceeding==NULL)
	    {
	      printf("Error creating residue\n");
	      return(NULL);
	    };

	  /* connect units */
	  from_pos=BU_ANOMER_POS(preceeding);
	  if(from_pos==-1)
	    {
	      printf("Cannot link from this unit\n");
	      return(NULL);
	    }
	  current->Subst[to_pos]=preceeding;
	  preceeding->Subst[from_pos]=current;
	  preceeding->Position=to_pos;
	  if(to_pos == BU_ANOMER_POS(current))
	    {
	      current->Position=from_pos;
	    }

	  /*   fprintf(PA_Status->Error," %s(%s->%s)%s\n",
	       preceeding->Residue->Node.Name,preceeding->Shifts.Type->Atom[from_pos].Label,
	       current->Shifts.Type->Atom[to_pos].Label,current->Residue->Node.Name); */

	  /*	  state=CA_SimDisacch(preceeding, from_pos, to_pos, current);
	  if (state!=PA_ERR_OK)
	    {
	      remove=TRUE;
	      fprintf(PA_Status->Error,"No data for %s(%s->%s)%s\n",
		      preceeding->Residue->Node.Name,preceeding->Shifts.Type->Atom[from_pos].Label,
		      current->Shifts.Type->Atom[to_pos].Label,current->Residue->Node.Name); 
		      }*/
	  current=preceeding;
	};
    };
  return(current);
}

/* SYNTAX: LN <describtion> */
int BU_LineNotation()
{
  struct BU_Unit *current, *preceeding;	/* current unit */
  /*  struct BU_Unit *branch;*/			/* branch point */
  struct BU_Unit *reducing;			/* for 'closure' in PS */
  int to_pos, from_pos;				/* bond position */
  int polymer;					/* polymer flag */
 
  reducing=NULL;

  /* Find last character in LN */
  PA_GetString;				/* get description */
  BULN_String=PA_Result.String;		/* first character in LN */
  BULN_Pos=BULN_String;
  BULN_Unit=0;
  polymer=0;
  reducing=NULL;

  while (*BULN_Pos!=0) BULN_Pos++;		/* find last character in LN */
  BULN_Pos--;

  /* go back to last residue/residue name */
  /* if the last character is '>' or '-' then this is a polymer */
  if ((*BULN_Pos=='>')||(*BULN_Pos=='-'))
    {
      /* currently this info will be ignored */
      while ((*BULN_Pos!='(')&&(BULN_Pos>BULN_String)) BULN_Pos--;
      BULN_Pos--;
      polymer=1;
    };

  current=BU_CreateLNUnit();
  if (current==NULL) Error(PA_ERR_FAIL, "Error creating residue");

  if (polymer) reducing=current;

  if (BULN_Pos<=BULN_String) return(PA_ERR_OK);	/* Encounterd start of LN */

  /* now do backbone and branches */
  /* next character may be ']' or ')' */

  do
    {
      preceeding=BU_Backbone(current);
      if (preceeding==current) break;
      current=preceeding;
    } while (preceeding!=NULL);

  if (BULN_Pos<BULN_String) return(PA_ERR_OK);
  /* is this last link in polymer? */
  if ((*BULN_Pos=='>')||(*BULN_Pos=='-'))
    {
      if (polymer==0)
	{
	  Error (PA_ERR_FAIL, "Error at rhs of polymer");
	}
      /* fix 04-05-15  from reducing to current */
      to_pos=BU_GetLinkage(current->Residue, BULN_Pos);
      if (to_pos==TY_NOT_FOUND)
	{
	  Error (PA_ERR_FAIL, "Error in linkage");
	}

      /* now tie up the polymer */
      from_pos=BU_ANOMER_POS(reducing);
      if(from_pos==-1)
	{
	  Error(PA_ERR_FAIL, "Cannot find anomeric position in linkage");
	}
      current->Subst[to_pos]=reducing;
      reducing->Subst[from_pos]=current;
      reducing->Position=to_pos;

      /*      state=CA_SimDisacch(reducing, from_pos, to_pos, current);
      if (state!=PA_ERR_OK)
	{
	  remove=TRUE;
	  fprintf(PA_Status->Error,"No data for %s(%s->%s)%s\n",
		  reducing->Residue->Node.Name,reducing->Shifts.Type->Atom[from_pos].Label,
		  current->Shifts.Type->Atom[to_pos].Label,current->Residue->Node.Name); 
		  }*/
    }
  return(PA_ERR_OK);
};

int BU_DuplicateStructSegment(struct BU_Struct *str, struct BU_Unit *first, int fromPos, struct BU_Unit *last, int toPos)
{
  struct BU_Unit *this, *prev, *next, *thisOriginal, *firstDup, *origFirstLinkedUnit;
  int origFirstPosition, anomerPos;

  origFirstPosition=first->Position;
  origFirstLinkedUnit=first->Subst[fromPos];

  prev=first;
  prev->Position=toPos;
  anomerPos=BU_ANOMER_POS(first);
  if(anomerPos==-1)
    {
      Error(PA_ERR_FAIL, "Error in struct segment. No anomeric position in reducing end");
    }
  next=first->Subst[anomerPos];

  thisOriginal=last;
  this=(struct BU_Unit *)BU_DuplicateUnit(str, last, toPos, prev);
  firstDup=this;
  
  if(!this)
    {
      Error(PA_ERR_FAIL, "Error creating repetition");
    }

  anomerPos=BU_ANOMER_POS(thisOriginal);
  if(anomerPos==-1)
    {
      Error(PA_ERR_FAIL, "Error in struct segment.");
    }

  while(this && thisOriginal!=first)
    {
      this->Subst[prev->Position]=prev;
      prev=this;

      this=(struct BU_Unit *)BU_DuplicateUnit(str, thisOriginal->Subst[anomerPos],prev->Position,prev);
      thisOriginal=prev->Subst[this->Position];
      anomerPos=BU_ANOMER_POS(prev);
      if(anomerPos==-1)
	{
	  Error(PA_ERR_FAIL, "Error in struct segment");
	}
      prev->Subst[anomerPos]=this;
    }

  this->Position=origFirstPosition;
  this->Subst[fromPos]=origFirstLinkedUnit;
  if(origFirstLinkedUnit)
    {
      origFirstLinkedUnit->Subst[toPos]=this;
    }
  first->Subst[fromPos]=firstDup;
  first->Position=toPos;
  /*  if(CA_SimDisacch(first, fromPos,
		   toPos, firstDup)!=PA_ERR_OK)
    {
      Error(PA_ERR_FAIL, "No data for at least one bond when creating repetition");
    }
  */

  return(PA_ERR_OK);
}

struct BU_Unit *BU_DuplicateUnit(struct BU_Struct *str, struct BU_Unit *unit, int skip, struct BU_Unit *linkedFrom)
{
  struct BU_Unit *dup;
  int i;
  char name[NODE_NAME_LENGTH];

  if(str==0||unit==0)
    {
      return NULL;
    }

  sprintf(name,"%c",'a'+ListLen(&str->Units));

  dup=(struct BU_Unit *)ME_CreateNode(&UnitMethod, &(str->Units), name);
  if(!dup)
    {
      return NULL;
    }
  dup->Error=unit->Error;
  dup->HError=unit->HError;
  dup->CError=unit->CError;
  /*  dup->CHError=unit->CHError;
  dup->HHError=unit->HHError;
  */
  dup->Residue=unit->Residue;
  dup->Position=unit->Position;
  dup->Shifts.Type=unit->Shifts.Type;
  
  if(skip>=0)
    {
      dup->Subst[skip]=linkedFrom;
    }
      
  for(i=TY_MAX_BONDS-1;i>=0;i--)
    {
      if(i!=skip && i!=BU_ANOMER_POS(unit))
	{
	  dup->Subst[i]=unit->Subst[i];
	  if(dup->Subst[i])
	    {
	      dup->Subst[i]=BU_DuplicateUnit(str,unit->Subst[i],-1,0);
	      dup->Subst[i]->Position=i;
	      dup->Subst[i]->Subst[BU_ANOMER_POS(dup->Subst[i])]=dup;
	    }
	}
    }

  return dup;
}

/* This function copies the structural information of sim to exp. It does not
   copy chemical shifts etc. */
int BU_CopyStructure(struct BU_Struct *exp, struct BU_Struct *sim)
{
  int i, pos, identical=1;
  struct BU_Unit *unit, *eunit;
  /* check that original does exist */
  if (sim->Units.Head.Succ==&sim->Units.Tail)
    Error(PA_ERR_FAIL, "'Simulated' spectrum has no structure");

  if(ListLen(&sim->Units)==ListLen(&exp->Units))
    {
      for(unit=(struct BU_Unit *)sim->Units.Head.Succ,
	    eunit=(struct BU_Unit *)exp->Units.Head.Succ;
	  unit->Node.Succ!=NULL && eunit->Node.Succ!=NULL;
	  unit=(struct BU_Unit *)unit->Node.Succ,
	    eunit=(struct BU_Unit *)eunit->Node.Succ)
	{
	  /* Check that the residues are the same and that they bind to the same
	     position in the next residue. */
	  if(unit->Residue!=eunit->Residue ||
	     unit->Position!=eunit->Position)
	    {
	      identical=0;
	      break;
	    }
	  /* Check the bonds */
	  for(i=0;i<TY_MAX_BONDS;i++)
	    {
	      if(unit->Subst[i]!=0 && eunit->Subst[i]!=0)
		{
		  /* Just compare the names of the substituents since they are just
		     copies (if they are the same) - they are not identical */
		  if(strcmp(unit->Subst[i]->Node.Name,eunit->Subst[i]->Node.Name)!=0)
		    {
		      identical=0;
		      break;
		    }
		}
	      else if((unit->Subst[i]==0 && eunit->Subst[i]!=0) ||
		      (unit->Subst[i]!=0 && eunit->Subst[i]==0))
		{
		  identical=0;
		  break;
		}
	    }
	}
    }
  else
    {
      identical=0;
    }
  if(identical==1)
    {
      return(PA_ERR_OK);
    }

  /* clear experimental structure */
  FreeList(&exp->Units);

  for(i=0;i<3;i++)
    {
      exp->JHH[i]=sim->JHH[i];
      exp->JCH[i]=sim->JCH[i];
    }

  for (unit=(struct BU_Unit *)sim->Units.Head.Succ; unit->Node.Succ!=NULL;
       unit=(struct BU_Unit *)unit->Node.Succ)
    {
      eunit=ME_CreateNode(&UnitMethod, &(exp->Units), unit->Node.Name);
      if (eunit==NULL) Error(PA_ERR_FATAL, "Out of memory");
      eunit->Residue = unit->Residue;
      eunit->Shifts.Type = unit->Shifts.Type;
      for(i=0;i<TY_MAX_CARBON;i++)
	{
	  eunit->Shifts.C[i]=BU_VOID_SHIFT;
	  eunit->Shifts.H[i][0]=BU_VOID_SHIFT;
	  eunit->Shifts.H[i][1]=BU_VOID_SHIFT;
	}
      eunit->CcpnUnitNr = unit->CcpnUnitNr;
    }  

  /* copy linking info */
  for (unit=(struct BU_Unit*)sim->Units.Head.Succ; unit->Node.Succ!=NULL;
       unit=(struct BU_Unit *)unit->Node.Succ)
    {
      eunit=(struct BU_Unit *)FindNode(&(exp->Units.Head), unit->Node.Name);
      eunit->Position=unit->Position;	/* copy position info */
      for (pos=0; pos<TY_MAX_HEAVY; pos++)	/* copy every 'substituent' */
	{
	  if (unit->Subst[pos]==NULL) continue;
	  eunit->Subst[pos]=(struct BU_Unit*)
	    FindNode(&(exp->Units.Head),unit->Subst[pos]->Node.Name);
	}
    }
  return(PA_ERR_OK);
}

struct BU_Unit* BU_FindCcpnUnit(struct Node *node, int nr)
{
  struct BU_Unit *unit;
  while(node->Succ!=NULL)
    {
      node=node->Succ;
      unit=(struct BU_Unit *)node;
      if(unit->CcpnUnitNr==nr)
	{
	  return unit;
	}
    }
  return (NULL);
}

void BU_RemoveOrphanUnits(struct BU_Struct *str)
{
  int i, numberResiduesInStructure=0, numberUnits=0, removeFlag;
  struct BU_Unit *unit, *prevUnit;

  for(unit=(struct BU_Unit *)str->Units.Head.Succ;
       unit->Node.Succ!=NULL;
       unit=(struct BU_Unit *)unit->Node.Succ)
    {
      if(strcasecmp(unit->Residue->Shifts.Type->CTsuperclass, "subst")!=0)
	{
	  numberUnits++;
	}
    }
  numberResiduesInStructure=GFX_CntUnits(str);
  unit=(struct BU_Unit *)str->Units.Head.Succ;
  while(numberResiduesInStructure<numberUnits && unit->Node.Succ!=NULL)
    {
      removeFlag=1;
      for(i=0; i<unit->Shifts.Type->HeavyCnt; i++)
	{
	  if(unit->Subst[i]!=0)
	    {
	      removeFlag=0;
	    }
	}
      if(removeFlag)
	{
	  numberUnits--;
	  FreeNode((struct Node *)unit);
	  unit=prevUnit;
	}
      prevUnit=unit;
      unit=(struct BU_Unit *)unit->Node.Succ;
    }
  
}
