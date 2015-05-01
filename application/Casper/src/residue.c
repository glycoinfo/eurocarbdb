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

/* Filename = residue.c */
/* Prefix = RE */
/* The Residue-command and subcommands as well as some */
/* functions used to manipulate the residue-structure */


#include "residue.h"
#include "delta.h"
#include "methods.h"
#include "parser.h"
#include "build.h"
#include <string.h>
#include <ctype.h>

struct ME_Method ResidueMethod;
struct ME_Method DefaultMethod;

struct List ResidueList={
  {NULL, &(ResidueList.Tail), "Res Head"},
  {&(ResidueList.Head), NULL, "Res Tail"}
};


/*************************************/
/* Methods for RE_Residue-structures */
/*************************************/

void *RE_CreateResidue(char *name)
{
  struct RE_Residue *residue;
  int i;
  residue=(struct RE_Residue *)MakeNode(sizeof(struct RE_Residue),name);
  if (residue==NULL) return(NULL);
  for (i=0; i<TY_MAX_BONDS; i++) 
    {
      InitList(&(residue->Delta[i]));     
      strcpy(residue->Delta[i].Head.Name,"Delta Head");
      strcpy(residue->Delta[i].Tail.Name,"Delta Tail");
      InitList(&(residue->Defaults[i]));
      strcpy(residue->Defaults[i].Head.Name,"Defaults Head");
      strcpy(residue->Defaults[i].Tail.Name,"Defaults Tail");
    };
  return(residue);
}

void RE_ClearResidue(struct RE_Residue *residue)
{
  int i;
  residue->Info[0]=0;		/* No info */
  residue->CarbBank[0]=0;	/* No CarbBank name */
  residue->CTname[0]=0;
  residue->JHH=RE_UNKNOWN;	/* No coupling size */
  residue->JCH=RE_UNKNOWN;
  residue->Priority[0]=0;	/* No sort-name */
  residue->Error=0;		/* Reset accuracy */
  residue->Config=RE_ACHIRAL;	/* Reset chirality */
  residue->Shifts.Type=NULL;	/* Invalid type */
  for (i=0; i<TY_MAX_CARBON; i++)
    {
      residue->Shifts.C[i]=BU_VOID_SHIFT;
      residue->Shifts.H[i][0]=BU_VOID_SHIFT;
      residue->Shifts.H[i][1]=BU_VOID_SHIFT;
      residue->HvyConf[i]=RE_UNDEFINED;
    }
  residue->Enantiomer=NULL;	/* No enantiomer */
  for (i=0; i<TY_MAX_BONDS; i++) 
    {
      ME_EmptyList(&DeltaMethod, &(residue->Delta[i]) );
      ME_EmptyList(&DefaultMethod, &(residue->Defaults[i]) );
    }
}

void *RE_FreeResidue(struct RE_Residue *residue)
{
  int i;
  for (i=0; i<TY_MAX_BONDS; i++)
    {
      ME_EmptyList(&DeltaMethod, &(residue->Delta[i]) );
      ME_EmptyList(&DefaultMethod, &(residue->Defaults[i]) );
    }
  return(FreeNode((struct Node *)residue));
}

void RE_ListResidue(struct RE_Residue *residue)
{
  printf("%s\t%s\n",residue->Node.Name,residue->Info);
  /*  printf("%s\n",residue->CarbBank);*/
}

void RE_PrintResidue(struct RE_Residue *residue)
{
  struct RE_Default *def;
  int i;
  printf("%s\t",residue->Node.Name);
  switch (residue->Config)
    {
    case RE_LEVO:		printf("(L)-"); break;
    case RE_DEXTRO:	printf("(D)-"); break;
    case RE_RECTUS:	printf("(R)-"); break;
    case RE_SINISTER:	printf("(S)-"); break;
    };
  if (residue->Shifts.Type->Info[0]!=0)
    printf("%s", residue->Shifts.Type->Info);
  else
    printf("%s", residue->Shifts.Type->Node.Name);
  if (residue->Info[0]!=0)
    printf("\t%s",residue->Info);
  puts("");
  SH_PrintShifts(&residue->Shifts, 0);
  if (residue->Error!=0)
    printf("error %0.2f\n",residue->Error);
  /* Enantiomer? */
  if (residue->Enantiomer)
    {
      printf("enantiomer of %s\n", residue->Enantiomer->Node.Name);
    };
  /* Defaults - perhaps only for save? */
  for (i=0; i<residue->Shifts.Type->HeavyCnt; i++)
    {
      def=(struct RE_Default *)residue->Defaults[i].Head.Succ;
      if (def->Node.Succ==NULL) continue;
      printf("default for %s: ", residue->Shifts.Type->Atom[i].Label);
      for (;def->Node.Succ!=NULL; def=(struct RE_Default *)def->Node.Succ )
	printf("%s %0.2f  ", def->Node.Name, def->Error);
      puts("");
    };
}

void RE_SaveResidue(struct RE_Residue* residue)
{
  struct RE_Default *def;
  int i,j;

  printf("residue '%s' '%s' {\n",residue->Node.Name,
	 residue->Shifts.Type->Node.Name);
  if (residue->Info[0]!=0) printf("info '%s'\n",residue->Info);
  if (residue->CarbBank[0]!=0) printf("carbbank '%s'\n",residue->CarbBank);
  if (residue->CTname[0]!=0) printf("GLYCO-CT name '%s'\n",residue->CTname);
  switch (residue->Config)
    {
    case RE_LEVO:		puts("config 'L'"); break;
    case RE_DEXTRO:	puts("config 'D'"); break;
    case RE_RECTUS:	puts("config 'R'"); break;
    case RE_SINISTER:	puts("config 'S'"); break;
    };
  switch (residue->JHH)
    {
    case 0:	puts("jhh 'small'"); break;
    case 1:	puts("jhh 'medium'"); break;
    case 2:	puts("jhh 'large'"); break;
    };
  switch (residue->JCH)
    {
    case 0:	puts("jch 'small'"); break;
    case 1:	puts("jch 'medium'"); break;
    case 2:	puts("jch 'large'"); break;
    };
  printf("priority '%s'\n", residue->Priority);

  for (i=0; i<residue->Shifts.Type->HeavyCnt; i++)
    {
      printf("shift %.2f",residue->Shifts.C[i]);
      for (j=0; j<residue->Shifts.Type->Atom[i].HCnt; j++)
	printf(" %.2f", residue->Shifts.H[i][j]);
      switch (residue->HvyConf[i])
	{
	case RE_AXIAL:	printf(" 'ax'");
	  break;
	case RE_EQUATORIAL:	printf(" 'eq'");
	  break;
	};
      puts("");
    };

  for (i=0; i<residue->Shifts.Type->HeavyCnt; i++)
    {
      def=(struct RE_Default *)residue->Defaults[i].Head.Succ;
      if (def->Node.Succ==NULL) continue;
      printf("default '%s' ", residue->Shifts.Type->Atom[i].Label);
      for (;def->Node.Succ!=NULL; def=(struct RE_Default *)def->Node.Succ )
	{
	  printf("%s %0.3f", def->Node.Name, def->Error);
	  if(def->Node.Succ->Succ!=NULL)
	    {
	      printf(", ");
	    }
	}
      puts("");
    };

  puts("}");
}

struct ME_Method ResidueMethod={
  RE_CreateResidue, RE_ClearResidue, /**/RE_FreeResidue/**//*NULL*/,
  RE_ListResidue, RE_PrintResidue, RE_SaveResidue
};

/* --- END OF RESIDUE METHODS --- */
/* --- DEFAULT METHODS --- */

void *RE_CreateDefault(char *name)
{
  return ( MakeNode(sizeof(struct RE_Default),name) );
}

struct ME_Method DefaultMethod={
  RE_CreateDefault, NULL, FreeNode,
  NULL, NULL, NULL
};

/* --- END OF DEFAULT METHODS --- */


struct RE_Residue *RE_Residue;	/* Pointer to current residue structure */
char RE_HvyOffset;		/* Pointer to current carbon */

struct PA_Command RE_Cmds[]={
  { "shift",	RE_Shift,
    "shift <C-shift> [<H-shift> ... ] [<type>...]",
    ""},
  { "info",	RE_Info,        "info <text>",
    "Sets the residue info tag to <text>"},
  { "config",	RE_Config,      "config R|S|D|L",
    "Changes configuration of residue."},
  { "error",	RE_Error,       "error <value>",
    "Sets accuracy of residue data."},
  { "priority",	RE_Priority,    "priority <sort name>",
    "Sets name for sorting"},
  { "jhh",	RE_JHH,         "jhh S|M|L",
    "Set the size of the JHH coupling to Small, Medium or Large."},
  { "jch",	RE_JCH,         "jch S|M|L",
    "Set the size of the JCH coupling to Small, Medium or Large."},
  { "carbbank",	RE_CarbBank,    "carbbank <residue id>",
    "CarbBank name of residue."},

  { "ctname", RE_CTname, "ctname <GLYCO-CT name of residue>",
    "Sets the GLYCO-CT name of the residue. This should correspond to the namespace, but is prefixed by the configuration.\nE.g. for B-D-GlcpNAc:\nctname 'b-dglc'"},

  { "default",	RE_SetDefault,
    "default <pos> <residue> [<error>] [, <residue> [<error>]...]",
    "If there is no correction for a linkage in a structure CASPER looks at the list of default residues for each resiude involved to see if any of them can be replaced to find a correction that is similar enough. This command adds a possible replacement for this residue.\nE.g. \"default '2' aDGlcOMe, aDGalOMe 0.05\""},

  { "copy",	RE_Copy,        "copy <res>",
    ""},
  { "add",	RE_Add,         "add <res>",
    ""},
  { "sub",	RE_Sub,         "sub <res>",
    ""},
  { "correct",	RE_Correct,     "correct <coff> <hoff>",
    ""},

  { "cpydefault", RE_CpyDefault,"CpyDefault <pos> <residue> [<error incr>]",
    "Copies the default replacements of <pos> in <residue> and adds them to this residue. If <error incr> is specified that value is added to the errors of all copied replacements."},
  { "convertalldefaults", RE_ConvertAllDefaults, "convertalldefaults <residue>",
    "Converts all default replacement possibilities from one anomeric configuration into another."},
    

  { NULL,	NULL,           NULL,
    NULL}
};

/* SYNTAX: Residue <name> [<type>] { */
int RE_SetResidue()
{
  struct TY_Type *type;
  int stat;
  char buffer[256];
  RE_HvyOffset=0;
  PA_GetString;
  strncpy(buffer, PA_Result.String, 255);
  type=NULL;
  if (PA_GetArg()==PA_STRING)
    {
      type=(struct TY_Type *)FindNode((struct Node *)&TypeList, PA_Result.String);
      if (type==NULL) Error(PA_ERR_FAIL, "Type unknown");
    };
  RE_Residue=(struct RE_Residue *)ME_CreateNode(&ResidueMethod,
						&ResidueList, buffer);
  if (RE_Residue==NULL) Error(PA_ERR_FATAL, "Out of memory");
  if (type!=NULL) RE_Residue->Shifts.Type=type;
  if (RE_Residue->Shifts.Type==NULL)
    Error(PA_ERR_FAIL, "Must be declared with type");
  /* change mode */
  stat=PA_Execute(RE_Cmds);
 
  /* back to normal - should check that all shifts assigned */
  return(stat);
}

/* <new_name> is created and made the enantiomer of <old_name> if possible */
/* SYNTAX: Enantiomer <old_name> <new_name> */
int RE_Enantiomer()
{
  struct RE_Residue *old, *new;
  int i;

  PA_GetString;
  old=(struct RE_Residue *)FindNode((struct Node *)&ResidueList, PA_Result.String);
  if (old==NULL)
    Error(PA_ERR_FATAL, "Can't find residue");
  PA_GetString;
  new=(struct RE_Residue *)ME_CreateNode(&ResidueMethod,
					 &ResidueList, PA_Result.String);
  if (new==NULL)
    Error(PA_ERR_FATAL, "Out of memory");
  /* copy data */
  snprintf(new->Info, RE_INFO_LEN, "Enantiomer of %s", old->Node.Name);
  new->Shifts.Type=old->Shifts.Type;
  SH_RAW_COPY(&(old->Shifts),&(new->Shifts));
  new->Error=old->Error;
  switch (old->Config)
    {
    case RE_LEVO:		new->Config=RE_DEXTRO; break;
    case RE_DEXTRO:	new->Config=RE_LEVO; break;
    case RE_RECTUS:	new->Config=RE_SINISTER; break;
    case RE_SINISTER:	new->Config=RE_RECTUS; break;
    default:		Error(PA_ERR_WARN, "Cant have enantiomer");
    };
  new->Enantiomer=old;
  old->Enantiomer=new;
  /* copy J:s and configurations */
  new->JHH=old->JHH;
  new->JCH=old->JCH;
  for (i=0; i<TY_MAX_CARBON; i++)
    {
      new->HvyConf[i]=old->HvyConf[i];
    }

  /* Copy the CarbBank name and change from D to L or vice versa */
  if(strlen(old->CarbBank)>0)
    {
      strcpy(new->CarbBank, old->CarbBank);
      for (i=0; i<strlen(new->CarbBank)-2; i++)
	{
	  if(new->CarbBank[i]=='-' && new->CarbBank[i+2]=='-')
	    {
	      if(new->CarbBank[i+1]=='D')
		{
		  new->CarbBank[i+1]='L';
		}
	      else if(new->CarbBank[i+1]=='L')
		{
		  new->CarbBank[i+1]='D';
		}
	    }
	}
    }
  if(strlen(old->CTname)>0)
    {
      /* Copy the GlycoCT name and change from D to L or vice versa */
      strcpy(new->CTname, old->CTname);
      for (i=0; i<strlen(new->CTname)-1; i++)
	{
	  if(new->CTname[i]=='-')
	    {
	      if(new->CTname[i+1]=='d')
		{
		  new->CTname[i+1]='l';
		}
	      else if(new->CTname[i+1]=='l')
		{
		  new->CTname[i+1]='d';
		}
	    }
	}
    }

  return(PA_ERR_OK);
}

/* SYNTAX: Config R|S|D|L */
/* Changes configuration of residue */
int RE_Config()
{
  PA_GetString;
  switch (toupper(PA_Result.String[0]))
    {
    case 'D':	RE_Residue->Config=RE_DEXTRO; break;
    case 'L':	RE_Residue->Config=RE_LEVO; break;
    case 'R':	RE_Residue->Config=RE_RECTUS; break;
    case 'S':	RE_Residue->Config=RE_SINISTER; break;
    default:	Error(PA_ERR_FAIL,"Unknown chirality");
    };
  return(PA_ERR_OK);	
}

/* SYNTAX: JHH S|M|L */
/* Changes configuration of residue */
int RE_JHH()
{
  PA_GetString;
  switch (toupper(PA_Result.String[0]))
    {
    case 'S':	RE_Residue->JHH=RE_SMALL; break;
    case 'M':	RE_Residue->JHH=RE_MEDIUM; break;
    case 'L':	RE_Residue->JHH=RE_LARGE; break;
    default:	Error(PA_ERR_FAIL,"Unknown size");
    };
  return(PA_ERR_OK);	
}

/* SYNTAX: JCH S|M|L */
/* Changes configuration of residue */
int RE_JCH()
{
  PA_GetString;
  switch (toupper(PA_Result.String[0]))
    {
    case 'S':	RE_Residue->JCH=RE_SMALL; break;
    case 'M':	RE_Residue->JCH=RE_MEDIUM; break;
    case 'L':	RE_Residue->JCH=RE_LARGE; break;
    default:	Error(PA_ERR_FAIL,"Unknown size");
    };
  return(PA_ERR_OK);	
}

/* SYNTAX: Error <value> */
/* Sets accuracy of residue data */
int RE_Error()
{
  PA_GetFloat;
  RE_Residue->Error=PA_Result.Float;
  return(PA_ERR_OK);	
}

/* SYNTAX: Priority <sort name> */
/* Sets name for sorting */
int RE_Priority()
{
  PA_GetString;
  strncpy(RE_Residue->Priority, PA_Result.String, RE_INFO_LEN);
  return(PA_ERR_OK);
}

/* SYNTAX: Shift <C-shift> [<H-shift> ... ] [<type>...] */
int RE_Shift()
{
  int hcnt, flags;
  /* Handle non-C heavy atom */
  if (RE_Residue->Shifts.Type->Atom[(int)RE_HvyOffset].Type&TY_SILENT)
    {
      PA_GetString;
      if (strcmp(PA_Result.String,"-")!=0)
	Error(PA_ERR_FAIL,"Atom can't have chemical shift");
      RE_HvyOffset++;
      return(PA_ERR_OK);
    };
  PA_GetFloat;
  RE_Residue->Shifts.C[(int)RE_HvyOffset]=PA_Result.Float;
  for(hcnt=0; hcnt<RE_Residue->Shifts.Type->Atom[(int)RE_HvyOffset].HCnt; hcnt++)
    {
      PA_GetFloat;
      RE_Residue->Shifts.H[(int)RE_HvyOffset][(int)hcnt]=PA_Result.Float;
    };
  /* type stuff */
  RE_Residue->HvyConf[(int)RE_HvyOffset]=RE_UNDEFINED;
  while (PA_GetArg()==PA_STRING)
    {
      flags=((toupper(PA_Result.String[0])<<8)+toupper(PA_Result.String[1]));
      switch (flags)
	{
	case RE_AX_ID:	RE_Residue->HvyConf[(int)RE_HvyOffset]=RE_AXIAL;
	  break;
	case RE_EQ_ID:	RE_Residue->HvyConf[(int)RE_HvyOffset]=RE_EQUATORIAL;
	  break;
	case RE_DEOXY_ID:	RE_Residue->HvyConf[(int)RE_HvyOffset]=RE_DEOXY;
	  break;
	default:		Error(PA_ERR_FAIL,"Unknown atom type");
	};
    }; 
  /* end of type stuff */
  RE_HvyOffset++;
  return(PA_ERR_OK);
}

/* SYNTAX: Info <text> */
int RE_Info()
{
  PA_GetString;
  strncpy(RE_Residue->Info, PA_Result.String, RE_INFO_LEN);
  return(PA_ERR_OK);
}

/* SYNTAX: CarbBank <residue-id> */
/* CarbBank name of residue */
int RE_CarbBank()
{
  PA_GetString;
  strncpy(RE_Residue->CarbBank, PA_Result.String, RE_INFO_LEN);
  return(PA_ERR_OK);
}

/* SYNTAX: CTname <GLYCO-CT name of residue> */
int RE_CTname()
{
  PA_GetString;
  strncpy(RE_Residue->CTname, PA_Result.String, RE_INFO_LEN);
  return(PA_ERR_OK);  
}

/* void RE_ActionCopy(); */

/* SYNTAX: Default <pos> <residue> [<error>] [, <residue> [<error>]...] */
int RE_SetDefault()
{
  int pos, status;
  struct RE_Default *defnode;
  struct RE_Residue *defres;
  PA_GetString;
  pos=TY_FindAtom(RE_Residue->Shifts.Type, PA_Result.String);
  if (pos==TY_NOT_FOUND)
    Error(PA_ERR_FAIL, "Linkage position does not exist");
  if ( !(RE_Residue->Shifts.Type->Atom[pos].Type&TY_FREE_POS) )
    Error(PA_ERR_FAIL, "Position may not be linked");
  status=0;

  do {
    PA_GetString;
    defres=(struct RE_Residue *)FindNode((struct Node *)&ResidueList, PA_Result.String);
    if (defres==NULL)
      Error(PA_ERR_FAIL, "Residue not found");
    /* Create an entry in the default list */
    defnode=(struct RE_Default *)ME_CreateNode(&DefaultMethod,
					       &(RE_Residue->Defaults[pos]), defres->Node.Name);
    if (defnode==NULL)
      Error(PA_ERR_FATAL, "Out of memory");
    defnode->Residue=defres;
    /* Allow for the later addition of a conversion */
    /* but do warn that this will not work yet! */
    if ( !TY_FindConversion(defres->Shifts.Type, RE_Residue->Shifts.Type) )
      status=1;
    /* optional accuracy */
    if (PA_GetArg()==PA_FLOAT)
      defnode->Error=PA_Result.Float;
    else
      defnode->Error=0;
  } while ( PA_GetSeparator(',') );
  if (status)
    Error(PA_ERR_WARN, "Do not know how to convert between residues");
  return(PA_ERR_OK);
}

/* SYNTAX: CpyDefault <pos> <residue> [<error incr>] */
int RE_CpyDefault()
{
  int dest_pos, src_pos, status;
  struct RE_Default *dest, *src;
  struct RE_Residue *src_res;
  struct TY_Conversion *conversion;
  float incr;
  PA_GetString;
  dest_pos=TY_FindAtom(RE_Residue->Shifts.Type, PA_Result.String);
  if (dest_pos==TY_NOT_FOUND)
    Error(PA_ERR_FAIL, "Linkage position does not exist");
  if ( !(RE_Residue->Shifts.Type->Atom[dest_pos].Type&TY_FREE_POS) )
    Error(PA_ERR_FAIL, "Position may not be linked");
  status=0;
  PA_GetString;
  src_res=(struct RE_Residue *)FindNode((struct Node *)&ResidueList, PA_Result.String);
  /* optional accuracy */
  if (PA_GetArg()==PA_FLOAT)
    incr=PA_Result.Float;
  else
    incr=0;
  if (src_res==NULL)
    Error(PA_ERR_FAIL, "Residue not found");

  conversion=TY_FindConversion(src_res->Shifts.Type, RE_Residue->Shifts.Type);
  /* Fail if no conversion exists */
  if ( !conversion )
    {
      Error(PA_ERR_FAIL, "Can't convert between residues");
    }
  if (conversion==TY_CONV_SAME)
    {
      src_pos=dest_pos;
    }
  else
    {
      src_pos=conversion->AtomNr[dest_pos];
    }
  
  /* Add the residue that is used for finding the default residues as a default
     residue too - if it isn't already added. */
  dest=(struct RE_Default *)FindNode((struct Node *)&RE_Residue->Defaults[dest_pos], src_res->Node.Name);
  if (dest==NULL)
    {
      dest=(struct RE_Default *)ME_CreateNode(&DefaultMethod,
					      &(RE_Residue->Defaults[dest_pos]),
					      src_res->Node.Name);
      if (dest==NULL)
	{
	  Error(PA_ERR_FATAL, "Out of memory");
	}
      dest->Residue=src_res;
      dest->Error=incr;
    }

  for(src=(struct RE_Default *)src_res->Defaults[src_pos].Head.Succ;src->Node.Succ!=NULL;src=(struct RE_Default *)src->Node.Succ)
    {
      if (RE_Residue == src->Residue)
	{
	  continue;
	}
      /* See if it already exists (then do not increase the error) */
      dest=(struct RE_Default *)FindNode((struct Node *)&RE_Residue->Defaults[dest_pos], src->Node.Name);
      if (dest==NULL)
	{
	  /* Create an entry in the default list */
	  dest=(struct RE_Default *)ME_CreateNode(&DefaultMethod,
						  &(RE_Residue->Defaults[dest_pos]),
						  src->Node.Name);
	  if (dest==NULL)
	    {
	      Error(PA_ERR_FATAL, "Out of memory");
	    }
	  dest->Residue=src->Residue;
	  dest->Error=src->Error+incr;
	}
    }
  return(PA_ERR_OK);
}

int RE_ConvertAllDefaults()
{
  struct RE_Residue *src_res, *conv_res;
  struct RE_Default *dest, *src;
  struct TY_Conversion *conversion;
  char name[NODE_NAME_LENGTH];
  int i;

  PA_GetString;
  src_res=(struct RE_Residue *)FindNode((struct Node *)&ResidueList, PA_Result.String);
  conversion=TY_FindConversion(src_res->Shifts.Type, RE_Residue->Shifts.Type);
  /* Fail if no conversion exists */
  if ( !conversion )
    {
      Error(PA_ERR_FAIL, "Can't convert between residues");
    }
  if (conversion!=TY_CONV_SAME)
    {
      Error(PA_ERR_FAIL, "Residues must be equivalent");
    }
  for(i=0; i<src_res->Shifts.Type->HeavyCnt; i++)
    {
      for(src=(struct RE_Default *)src_res->Defaults[i].Head.Succ;src->Node.Succ!=NULL;src=(struct RE_Default *)src->Node.Succ)
	{
	  strcpy(name, src->Node.Name);
	  /* Convert from alpha to beta and vice versa */
	  if(name[0]=='a')
	    {
	      name[0]='b';
	    }
	  else if(name[0]=='b')
	    {
	      name[0]='a';
	    }
	  conv_res=(struct RE_Residue *)FindNode((struct Node *)&ResidueList, name);
	  /* If this residue was not found just skip to the next one */
	  if(conv_res==NULL)
	    {
	      continue;
	    }
	  /* First check if this conversion already exists in the destination residue. If it does just skip it to preserve the expected error. */
	  dest=(struct RE_Default *)FindNode((struct Node *)&RE_Residue->Defaults[i], name);
	  if(dest!=NULL)
	    {
	      continue;
	    }
	  dest=(struct RE_Default *)ME_CreateNode(&DefaultMethod,
						  &(RE_Residue->Defaults[i]),
						  name);
	  if(dest==NULL)
	    {
	      Error(PA_ERR_FATAL, "Out of memory");
	    }
	  dest->Residue=conv_res;
	  dest->Error=src->Error;
	}
    }
  return(PA_ERR_OK);
}

int RE_Manipulate(int action)
{
  struct RE_Residue *off;
  /* struct TY_Conversion *conv; */
  /* char i,j; */
  PA_GetString;
  off=(struct RE_Residue *)FindNode((struct Node *)&ResidueList, PA_Result.String);
  if (off==NULL) Error(PA_ERR_FAIL,"Residue not found");
  if (action==SH_SHIFT_COPY)
    RE_Residue->Error=off->Error;
  else
    RE_Residue->Error+=off->Error;
  return( SH_Manipulate(action, &(off->Shifts), &(RE_Residue->Shifts)) );
}

/* SYNTAX: Add <res> */
int RE_Add()
{
  return(RE_Manipulate(SH_SHIFT_ADD));
}

/* SYNTAX: Sub <res> */
int RE_Sub()
{
  return(RE_Manipulate(SH_SHIFT_SUB));
}

/* SYNTAX: Copy <res> */
int RE_Copy()
{
  return(RE_Manipulate(SH_SHIFT_COPY));
}

/* SYNTAX: Correct <coff> <hoff> */
int RE_Correct()
{
  float coff, hoff;
  PA_GetFloat; coff=PA_Result.Float;
  PA_GetFloat; hoff=PA_Result.Float;
  SH_Adjust(&(RE_Residue->Shifts), coff, hoff);
  return(PA_ERR_OK);
}
