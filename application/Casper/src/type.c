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

/* Filename = type.c */
/* Prefix = TY */
/* Manipulation of connectivity etc. */

#include "parser.h"
#include "methods.h"
#include "type.h"
#include <string.h>
#include <ctype.h>

struct TY_Type *TY_Type;
struct TY_Conversion *TY_ConvFrom;
struct TY_Conversion *TY_ConvTo;

struct ME_Method ConversionMethod;

struct List TypeList={
  {
    NULL, &(TypeList.Tail), "Type Head"
  },
  {
    &(TypeList.Head), NULL, "Type Tail"
  }
};

/*****************************/
/* Methods for TY_Type-nodes */
/*****************************/

void *TY_CreateType(char *name)
{
  struct TY_Type *type;
  type=(struct TY_Type *)MakeNode(sizeof(struct TY_Type),name);
  if (type==NULL)
    {
      return(NULL);
    }
  InitList(&(type->Conversion));
  strcpy(type->Conversion.Head.Name,"Conversion Head");
  strcpy(type->Conversion.Tail.Name,"Conversion Tail");
  InitList(&(type->CTmods));
  strcpy(type->CTmods.Head.Name,"GlycoCT mods Head");
  strcpy(type->CTmods.Tail.Name,"GlycoCT mods Tail");
  InitList(&(type->CTsubsts));
  strcpy(type->CTsubsts.Head.Name,"GlycoCT substs Head");
  strcpy(type->CTsubsts.Tail.Name,"GlycoCT substs Tail");
  type->Anomeric=-1;
  return(type);
}

void TY_ClearType(struct TY_Type *type)
{
  int i,j;
  type->HeavyCnt=0;
  type->HCnt=0;
  type->Info[0]=0;
  type->Priority[0]=0;
  type->CTsuperclass[0]=0;
  ME_EmptyList(&ConversionMethod, &(type->Conversion));
  ME_EmptyList(&CTModifierMethod, &(type->CTmods));
  ME_EmptyList(&CTSubstsMethod, &(type->CTsubsts));
  for (i=0; i<TY_MAX_HEAVY; i++)
    {
      type->Atom[i].Type=0;
      for (j=0; j<4; j++)
	{
	  type->Atom[i].Connect[j]=TY_VOID_ATOM;
	}
    }
  type->outlinkage='o';
  type->ccpSub[0]=0;
  type->Anomeric=-1;
}

void TY_PrintType(struct TY_Type *type)
{
  int i,j;
  printf("%s", type->Node.Name);
  if ( type->Info[0]!=0 )
    {
      printf("\t%s", type->Info);
    }
  puts("");
  for (i=0; i<type->HeavyCnt; i++)
    {
      if (type->Atom[i].Connect[0]!=TY_VOID_ATOM)
	{
	  printf("%s is connected to ",type->Atom[i].Label);
	  for (j=0; (type->Atom[i].Connect[j]!=TY_VOID_ATOM)&&(j<4); j++)
	    {
	      printf("%s ",type->Atom[type->Atom[i].Connect[j]].Label);
	    }
	  puts("");
	}
      else
	{
	  printf("%s\n",type->Atom[i].Label);
	}
    }
  if(type->CTsuperclass[0]!=0)
    {
      printf("Superclass: %s\n",type->CTsuperclass);
    }
  ME_PrintList(&CTModifierMethod,&(type->CTmods));
  ME_PrintList(&CTSubstsMethod,&(type->CTsubsts));
}

void TY_SaveType(struct TY_Type *type)
{
  int i,j;
  struct TY_CTmodifier *mod;
  struct TY_CTsubstituent *sub;

  printf("type '%s' {\n", type->Node.Name);
  if ( type->Info[0]!=0 )
    {
      printf("info '%s'\n", type->Info);
    }
  printf("priority '%s'\n", type->Priority);
  for (i=0; i<type->HeavyCnt; i++)
    {
      printf("atom '%s' %d", type->Atom[i].Label, type->Atom[i].HCnt);
      if (type->Atom[i].Type&TY_PYRANOSE)
	{
	  printf(" pyranose");
	}
      if (type->Atom[i].Type&TY_FURANOSE)
	{
	  printf(" furanose");
	}
      if (type->Atom[i].Type&TY_METHYL)
	{
	  printf(" methyl");
	}
      if (type->Atom[i].Type&TY_PRIMARY)
	{
	  printf(" primary");
	}
      if (type->Atom[i].Type&TY_SECONDARY)
	{
	  printf(" secondary");
	}
      if (type->Atom[i].Type&TY_SILENT)
	{
	  printf(" silent");
	}
      if (type->Atom[i].Type&TY_CLOSING)
	{
	  printf(" closing");
	}
      if (type->Atom[i].Type&TY_FREE_POS)
	{
	  printf(" free");
	}
      else
	{
	  printf(" used");
	}
      if (type->Atom[i].Type&TY_ANOMERIC)
	{
	  printf(" link");
	}
      printf("\n");
    }
  for (i=0; i<type->HeavyCnt; i++)
    {
      if (type->Atom[i].Connect[0]!=TY_VOID_ATOM)
	{
	  for (j=0; j<4; j++)
	    {
	      if(type->Atom[i].Connect[j]!=TY_VOID_ATOM&&type->Atom[i].Connect[j]>i)
		{
		  printf("connect '%s' ",type->Atom[i].Label);
		  printf("'%s'\n",type->Atom[type->Atom[i].Connect[j]].Label);
		}
	    }
	}
    }
  if(type->CTsuperclass[0]!=0)
    {
      printf("ctclass '%s'\n",type->CTsuperclass);
    }
  for (mod=(struct TY_CTmodifier *)type->CTmods.Head.Succ;
       mod->Node.Succ!=NULL;mod=(struct TY_CTmodifier *)mod->Node.Succ)
    {
      TY_SaveCTmod(mod);
    }
  for (sub=(struct TY_CTsubstituent *)type->CTsubsts.Head.Succ;
       sub->Node.Succ!=NULL;sub=(struct TY_CTsubstituent *)sub->Node.Succ)
    {
      TY_SaveCTsubst(sub);
    }
  if(type->ccpSub[0]!=0)
    {
      printf("ccpsub '%s'\n",type->ccpSub);
    }
  printf("}\n");
}

void *TY_FreeType(struct TY_Type *type)
{
  ME_EmptyList(&ConversionMethod, &(type->Conversion) );
  ME_EmptyList(&CTModifierMethod, &(type->CTmods) );
  ME_EmptyList(&CTSubstsMethod, &(type->CTsubsts));
  return(FreeNode((struct Node *)type));
}

struct ME_Method TypeMethod={
  TY_CreateType, TY_ClearType, /**/TY_FreeType/**//*NULL*/,
  NULL, TY_PrintType, TY_SaveType
};

/* ---- END OF TYPE METHODS ---- */

/* ---- CONVERSION METHODS ---- */

void *TY_CreateConversion(char *name)
{
  return ( MakeNode(sizeof(struct TY_Conversion),name) );
}

void TY_ClearConversion(struct TY_Conversion *conv)
{
  int i;
  for (i=0; i<TY_MAX_CARBON; i++)
    {
      conv->AtomNr[i]=TY_VOID_ATOM;
    }
}

void TY_ListConversion(struct TY_Conversion *conv)
{
  printf("%s to %s\n", conv->From->Node.Name, conv->Node.Name);
}

void TY_PrintConversion(struct TY_Conversion *conv)
{
  int i;
  printf("%s to %s\n", conv->From->Node.Name, conv->Node.Name);
  for (i=0; i<conv->From->HeavyCnt; i++)
    {
      if (conv->AtomNr[i]!=TY_VOID_ATOM)
	{
	  printf("\t%s<->%s\n", conv->From->Atom[i].Label,
		 conv->To->Atom[conv->AtomNr[i]].Label);
	}
    }
  printf("\n");
}

void TY_SaveConversion(struct TY_Conversion *conv)
{
  int i;
  printf("conversion '%s' '%s' {\n", conv->From->Node.Name, conv->Node.Name);
  for (i=0; i<conv->From->HeavyCnt; i++)
    {
      if (conv->AtomNr[i]!=TY_VOID_ATOM)
	{
	  printf("map '%s' '%s'\n", conv->From->Atom[i].Label,
		 conv->To->Atom[conv->AtomNr[i]].Label);
	}
    }
  printf("}\n");
}


struct ME_Method ConversionMethod={
  TY_CreateConversion, TY_ClearConversion, NULL,
  TY_ListConversion, TY_PrintConversion, TY_SaveConversion
};

/* ---- END OF CONVERSION METHODS ---- */

/* ---- GLYCO-CT MODIFIERS METHODS ---- */

void *TY_CreateCTmod(char *name)
{
  return ( MakeNode(sizeof(struct TY_CTmodifier),name) );
}

void TY_ClearCTmod(struct TY_CTmodifier *mod)
{
  mod->modifier[0]=0;
  mod->position=0;
}

void TY_ListCTmod(struct TY_CTmodifier *mod)
{
  TY_PrintCTmod(mod);
}

void TY_PrintCTmod(struct TY_CTmodifier *mod)
{
  switch(mod->modifier[0])
    {
    case 'd':
      printf("Deoxigenation ");
      break;
    case 'k':
      printf("Carbonyl function ");
      break;
    case 'e':
    case 'n':
      printf("Double bond ");
      break;
    case 'a':
      if(mod->Node.Name[1]=='l')
	{
	  printf("Reduced C1-carbonyl ");
	}
      else
	{
	  printf("Acidic function ");
	}
      break;
    default:
      printf("Unknown conversion");
      return;
    }
  printf("at %d",mod->position);
  printf("\n");
}

void TY_SaveCTmod(struct TY_CTmodifier *mod)
{
  printf("ctmod '%s' %d", mod->modifier, mod->position);
  printf("\n");
}


struct ME_Method CTModifierMethod={
  TY_CreateCTmod, TY_ClearCTmod, NULL,
  TY_ListCTmod, TY_PrintCTmod, TY_SaveCTmod
};

/* ---- END OF MODIFIER METHODS ---- */

/* ---- GLYCO-CT SUBSTITUENTS METHODS ---- */

void *TY_CreateCTsubst(char *name)
{
  return ( MakeNode(sizeof(struct TY_CTsubstituent),name) );
}

void TY_ClearCTsubst(struct TY_CTsubstituent *sub)
{
  sub->substituent[0]=0;
  sub->position[0]=0;
  sub->position[1]=0;
  sub->basetypelinkage='o';
}

void TY_ListCTsubst(struct TY_CTsubstituent *sub)
{
  TY_PrintCTsubst(sub);
}

void TY_PrintCTsubst(struct TY_CTsubstituent *sub)
{
  printf("Position %d on basetype links to position %d on %s.\n",
	 sub->position[0],sub->position[1],sub->substituent);
  printf("'%c' is replaced on the basetype when forming the bond.\n\n",
	 sub->basetypelinkage);
}

void TY_SaveCTsubst(struct TY_CTsubstituent *sub)
{
  printf("ctsub '%s' %d %d", sub->substituent, sub->position[0], sub->position[1]);
  if(sub->basetypelinkage!=0)
    {
      printf(" '%c'",sub->basetypelinkage);
    }
  printf("\n");
}


struct ME_Method CTSubstsMethod={
  TY_CreateCTsubst, TY_ClearCTsubst, NULL,
  TY_ListCTsubst, TY_PrintCTsubst, TY_SaveCTsubst
};

/* ---- END OF SUBSTITUENT METHODS ---- */

struct PA_Command TY_Cmds[]={
  { "connect",	TY_Connect,  "connect <atom1> <atom2> [<atom3> ...]",
    ""},
  { "atom",	TY_Atom,     "atom <label> MODES",
    ""},
  { "info",	TY_Info,     "info <text>",
    ""},
  { "priority",	TY_Priority, "priority <string value>",
    "Sets priority of residue for sorting."},
  { "ctclass", TY_SetCTClass, "ctclass <superclass>",
    "Sets the GLYCO-CT Superclass."},
  { "ctmod",    TY_SetModification, "ctmod <modification> <position1>",
    "Adds a GLYCO-CT modification at <position1>.\nList of types of modifiers:\n\td\t\tdeoxigenation\n\tketo\t\tcarbonyl function\n\ten|ne|ee|nn|en?\tdouble bond\n\ta\t\tacidic function\n\taldi\t\treduced C1-carbonyl"},
  { "ctsub",    TY_SetSubstituent, "ctsub <substituent> <from pos> [<to pos>] [<linkage type>]",
    "Adds a GLYCO-CT substituent at <from pos>. <substituent> is e.g. nac.\n<to pos> is the linkage position on the substituent, if it is not supplied it is set to 1.\n<linkage type> determines what is replaced when forming the bond. If no <linkage type> is specified it is assumed to be 'o'."},
  { "ccpsub", TY_SetCcpSub, "ccpsub <substituent string>",
    "Adds a description of the substituents according to the CCPN chemcomps.\nSubstituents are written in the order of the position and if there are more than one substituent they are separated by :.\nExample:\n\t'ccpsub 'C1_OMe:C2_NAc'"},
  { NULL,	NULL,        NULL,
    NULL}
};

struct PA_Command TY_ConvCmds[]={
  { "map",	TY_SetMap,   "map <from> <to>",
    ""},
  { NULL,	NULL,        NULL,
    NULL}
};

/* SYNTAX: Type <name> { */
int TY_SetType()
{
  int stat;
  PA_GetString;
  TY_Type=(struct TY_Type *)ME_CreateNode(&TypeMethod, &TypeList, PA_Result.String);
  if (TY_Type==NULL)
    {
      Error(PA_ERR_FATAL, "Out of memory");
    }
  ME_ClearNode(&TypeMethod, (struct Node *)TY_Type);

  /* change mode */
  stat=PA_Execute(TY_Cmds);
 
  /* back to normal */
  return(stat);
}


/* TY_FindAtom(type,name) - returns the number of the atom named <name> */
/* returns TY_NOT_FOUND if no such atom exists */
int TY_FindAtom(struct TY_Type *type, char *name)
{
  int i, j, match;

  /* Do not include the atom type (if it is specified) */
  if(name[0]=='C' || name[0]=='H')
    {
      name++;
    }
  for(i=0;i<type->HeavyCnt;i++)
    {
      if(strcasecmp(name,type->Atom[i].Label)==0)
	{
	  return(i); 
	}
      /* If this is an anomeric carbon the stereocode might be specified as well */
      if(strlen(name)==strlen(type->Atom[i].Label)+2 && name[strlen(name)-2]=='_')
	{
	  if(strncasecmp(name,type->Atom[i].Label, strlen(name)-2)==0)
	    {
	      return(i);
	    }
	}
    }
  /* If the atom was not found check if it is named according to CCPN naming
     where 61 and 62 corresponds to 6 and if that is the case do not compare
     the last number. */
  if(strlen(name)>1 && isdigit(name[strlen(name)-1]) && isdigit(name[strlen(name)-2]))
    {
      for(i=0;i<type->HeavyCnt;i++)
	{
	  if(strlen(name)-1!=strlen(type->Atom[i].Label))
	    {
	      continue;
	    }
	  match=1;
	  for(j=0;j<strlen(name)-1;j++)
	    {
	      if(name[j]!=type->Atom[i].Label[j])
		{
		  match=0;
		  j=strlen(name)-1;
		}
	    }
	  if(match==1)
	    {
	      return(i);
	    }
	}
    }
  /* Handle methyl substituents where each hydrogen can be numbered according
     to the CCPN atom naming, e.g. M1_2, M2_2 and M3_2 is just M_2 in CASPER */
  if(name[0]=='M')
    {
      for(i=0;i<type->HeavyCnt;i++)
	{
	  if(type->Atom[i].Label[0]!='M')
	    {
	      continue;
	    }
	  match=1;
	  for(j=1;j<strlen(name)-1;j++)
	    {
	      if(name[j+1]!=type->Atom[i].Label[j])
		{
		  match=0;
		  j=strlen(name)-1;
		}
	    }
	  if(match==1)
	    {
	      return(i);
	    }
	}
    }
  return (TY_NOT_FOUND);
}

/* SYNTAX: Atom <label> modes */
int TY_Atom()
{
  int flags;		/* Atom type flags */
  PA_GetString;		/* Get label */
  if (TY_Type->HeavyCnt==(TY_MAX_HEAVY-1))
    {
      Error(PA_ERR_FAIL,"Maximum number of heavy atoms exceeded");
    }
  strncpy(TY_Type->Atom[TY_Type->HeavyCnt].Label, PA_Result.String, TY_LABEL_LEN);
  /* should get type stuff here */
  /* Get number of attached hydrogens */
  PA_GetFloat;
  if (PA_Result.Float<0)
    {
      Error(PA_ERR_FAIL,"Number of hydrogens must be in range [0-3]");
    }
  if (PA_Result.Float>3)
    {
      Error(PA_ERR_FAIL,"Number of hydrogens must be in range [0-3]");
    }
  TY_Type->Atom[TY_Type->HeavyCnt].HCnt=PA_Result.Float;
  TY_Type->HCnt+=PA_Result.Float;
  /* Get type - still lacking any form of consistency check
     some of this stuff belong in residue ! */
  while (PA_GetArg()==PA_STRING)
    {
      flags=((toupper(PA_Result.String[0])<<8)+toupper(PA_Result.String[1]));
      switch (flags)
	{
	case TY_PYRAN_ID:
	  TY_Type->Atom[TY_Type->HeavyCnt].Type|=TY_PYRANOSE;
	  break;
	case TY_FURAN_ID:
	  TY_Type->Atom[TY_Type->HeavyCnt].Type|=TY_FURANOSE;
	  break;
	case TY_METHYL_ID:
	  TY_Type->Atom[TY_Type->HeavyCnt].Type|=TY_METHYL;
	  break;
	case TY_PRIM_ID:
	  TY_Type->Atom[TY_Type->HeavyCnt].Type|=TY_PRIMARY;
	  break;
	case TY_SEC_ID:
	  TY_Type->Atom[TY_Type->HeavyCnt].Type|=TY_SECONDARY;
	  break;
	case TY_SILENT_ID:
	  TY_Type->Atom[TY_Type->HeavyCnt].Type|=TY_SILENT;
	  break;
	case TY_ANOMER_ID:
	  TY_Type->Atom[TY_Type->HeavyCnt].Type|=TY_ANOMERIC;
	  TY_Type->Anomeric=TY_Type->HeavyCnt;
	  break;
	case TY_FREE_ID:
	  TY_Type->Atom[TY_Type->HeavyCnt].Type|=TY_FREE_POS;
	  break;
	case TY_USED_ID:
	  TY_Type->Atom[TY_Type->HeavyCnt].Type&=~TY_FREE_POS;
	  break;
	case TY_CLOSE_ID:
	  TY_Type->Atom[TY_Type->HeavyCnt].Type|=TY_CLOSING;
	  break;
	default:
	  Error(PA_ERR_FAIL,"Unknown atom type");
	}
    } 
  TY_Type->HeavyCnt++;
  return(PA_ERR_OK);
}

/* SYNTAX: Info <text> */
int TY_Info()
{
  PA_GetString;
  strncpy(TY_Type->Info, PA_Result.String, TY_INFO_LEN);
  return(PA_ERR_OK);
}

/* SYNTAX: Priority <string value> */
/* Sets priority of residue for sorting */
int TY_Priority()
{
  PA_GetString;
  strncpy(TY_Type->Priority, PA_Result.String, TY_INFO_LEN);
  return(PA_ERR_OK);
}

/* SYNTAX: Connect <atom1> <atom2> [<atom3>...] */
int TY_Connect()
{
  int first,second;
  int i,j;

  PA_GetString;
  first=TY_FindAtom(TY_Type, PA_Result.String);
  if (first==TY_NOT_FOUND)
    {
      Error(PA_ERR_FAIL, "First atom not found");
    }
  PA_GetString;
  second=TY_FindAtom(TY_Type, PA_Result.String);
  if (second==TY_NOT_FOUND)
    {
      Error(PA_ERR_FAIL, "Second atom not found");
    }
  for (i=0; (TY_Type->Atom[first].Connect[i]!=TY_VOID_ATOM)&&(i<4); i++);
  if (TY_Type->Atom[first].Connect[i]!=TY_VOID_ATOM)
    {
      Error(PA_ERR_FAIL, "First atom can't have more than four bonds");
    }
  for (j=0; (TY_Type->Atom[second].Connect[j]!=TY_VOID_ATOM)&&(j<4); j++);
  if (TY_Type->Atom[second].Connect[j]!=TY_VOID_ATOM)
    {
      Error(PA_ERR_FAIL, "Second atom can't have more than four bonds");
    }
  TY_Type->Atom[first].Connect[i]=second;
  TY_Type->Atom[second].Connect[j]=first;

  /* more ? */
  /* first=second;*/
  while (PA_GetArg()==PA_STRING)
    {
      first=second; second=TY_FindAtom(TY_Type, PA_Result.String);
      if (second==TY_NOT_FOUND)
	{
	  Error(PA_ERR_FAIL, "Atom not found");
	}
      for (i=0; (TY_Type->Atom[first].Connect[i]!=TY_VOID_ATOM)&&(i<4); i++);
      if (TY_Type->Atom[first].Connect[i]!=TY_VOID_ATOM)
	{
	  Error(PA_ERR_FAIL, "Atom can't have more than four bonds");
	}
      for (j=0; (TY_Type->Atom[second].Connect[j]!=TY_VOID_ATOM)&&(j<4); j++);
      if (TY_Type->Atom[second].Connect[j]!=TY_VOID_ATOM)
	{
	  Error(PA_ERR_FAIL, "Atom can't have more than four bonds");
	}
      TY_Type->Atom[first].Connect[i]=second;
      TY_Type->Atom[second].Connect[j]=first;
    }
  return(PA_ERR_OK);
}

/* SYNTAX: Conversion <from> <to> */
int TY_SetConversion()
{
  struct TY_Type *from, *to;
  int stat;

  PA_GetString;
  from=(struct TY_Type *)FindNode((struct Node *)&TypeList, PA_Result.String);
  if (from==NULL)
    {
      Error(PA_ERR_FAIL,"Can't find first type");
    }
  PA_GetString;
  to=(struct TY_Type *)FindNode((struct Node *)&TypeList, PA_Result.String);
  if (to==NULL)
    {
      Error(PA_ERR_FAIL,"Can't find second type");
    }
  TY_ConvFrom=(struct TY_Conversion *)
    ME_CreateNode(&ConversionMethod,&(from->Conversion), to->Node.Name);
  TY_ConvTo=(struct TY_Conversion *)
    ME_CreateNode(&ConversionMethod,&(to->Conversion), from->Node.Name);
  if ((TY_ConvTo==NULL)||(TY_ConvFrom==NULL))
    {
      Error(PA_ERR_FATAL, "Out of memory");
    }
  ME_ClearNode(&ConversionMethod, (struct Node *)TY_ConvTo);
  ME_ClearNode(&ConversionMethod, (struct Node *)TY_ConvFrom);
  TY_ConvFrom->From=from;
  TY_ConvFrom->To=to;
  TY_ConvTo->From=to;
  TY_ConvTo->To=from;
  /* do conversion stuff */
  stat=PA_Execute(TY_ConvCmds);

  /* back to normal */
  return(stat);
}

/* SYNTAX: ctmod '<modifier>' <from>*/
int TY_SetModification()
{
  char name[NODE_NAME_LENGTH], node_name[NODE_NAME_LENGTH];
  int pos;
  struct TY_CTmodifier *mod;

  PA_GetString;
  strcpy(name,PA_Result.String);
  PA_GetFloat;
  pos=(int)TopValue.Float;

  sprintf(node_name,"%s%d",name,pos);

  mod=(struct TY_CTmodifier *)ME_CreateNode(&CTModifierMethod,
					    &(TY_Type->CTmods),
					    node_name);
  strcpy(mod->modifier,name);
  mod->position=pos;
  return(PA_ERR_OK);
}

/* SYNTAX: ctclass <'class'> */
int TY_SetCTClass()
{
  PA_GetString;
  strcpy(TY_Type->CTsuperclass,PA_Result.String);
  return(PA_ERR_OK);
}


/* SYNTAX: ctsub <'substituent'> <from position> [<to position>] [<linkage type>]*/
int TY_SetSubstituent()
{
  char name[NODE_NAME_LENGTH], node_name[NODE_NAME_LENGTH];
  int pos;
  struct TY_CTsubstituent *sub;

  PA_GetString;
  strcpy(name,PA_Result.String);
  PA_GetFloat;
  pos=(int)TopValue.Float;

  sprintf(node_name,"%s%d",name,pos);

  sub=(struct TY_CTsubstituent *)ME_CreateNode(&CTSubstsMethod,
					       &(TY_Type->CTsubsts),
					       node_name);
  strcpy(sub->substituent,name);
  sub->position[0]=pos;

  if(PA_GetArg()==PA_FLOAT)
    {
      sub->position[1]=(int)TopValue.Float;
      if(PA_GetArg()==PA_STRING)
	{
	  sub->basetypelinkage=PA_Result.String[0];
	}
    }
  else
    {
      sub->position[1]=1;
    }
  return(PA_ERR_OK);
}

/* SYNTAX: ccpsub <'substituent string'> */
int TY_SetCcpSub()
{
  PA_GetString;
  strcpy(TY_Type->ccpSub,PA_Result.String);
  return(PA_ERR_OK);
}

/* SYNTAX: Map <from> <to> */
int TY_SetMap()
{
  int from, to;
  PA_GetString;
  from=TY_FindAtom(TY_ConvFrom->From, PA_Result.String);
  if (from==TY_NOT_FOUND)
    {
      Error(PA_ERR_FAIL, "Can't find first atom");
    }
  PA_GetString;
  to=TY_FindAtom(TY_ConvFrom->To, PA_Result.String);
  if (to==TY_NOT_FOUND)
    {
      Error(PA_ERR_FAIL, "Can't find second atom");
    }
  /* TY_ConvFrom->AtomNr[from]=to;
     TY_ConvTo->AtomNr[to]=from;*/
  TY_ConvFrom->AtomNr[to]=from;
  TY_ConvTo->AtomNr[from]=to;
  return(PA_ERR_OK);
}


struct TY_Conversion *TY_FindConversion(from,to)
     struct TY_Type *from, *to;
{
  if (from==to)
    {
      return (TY_CONV_SAME);
    }
  if (from==0 || to==0)
    {
      return 0;
    }
  return ( (struct TY_Conversion *)FindNode(&(from->Conversion.Head), to->Node.Name) );
}

