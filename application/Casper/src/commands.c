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

/* Filename = commands.c */
/* Prefix = CM */
/* This file defines the global commands in the parser */

#include "commands.h"
#include "parser.h"
#include "type.h"
#include "variables.h"
#include "residue.h"
#include "delta.h"
#include "ddelta.h"
#include "build.h"
#include "setup.h"
#include "spectra.h"
#include "tables.h"
#include "perm.h"
#include "ccpn.h"
#include "rule.h"

#include "plot.h"

/* 2005-08-11 */
#include "gfx.h"
#include "XML_out.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#include <libgen.h>

int CM_End();

/* These commands are the main commands. They are available when there is no special
 * command mode. */
struct PA_Command MainCmds[]={
  { "end",	CM_End,               "end",
    "Exits a script file."},
  { "type",	TY_SetType,           "type <name> { ... }",
    ""},
  { "conversion", TY_SetConversion,   "conversion <from> <to>",
    ""},
  { "residue",	RE_SetResidue,        "residue <name> [<type>] { ... }",
    "Add a residue called <name>. The commands within { and } specify the residue information."},
  { "enantiomer", RE_Enantiomer,      "enantiomer <old_name> <new_name>",
    "The enantiomer of residue <old_name> is created and called <new_name>."},
  { "dimer",	DE_SetDelta,
    "dimer <non res1>(<non pos1>-><red pos2>)<red res2> { ... }",
    "Specifies the shift correction that will be applied when the specified dimer connection is made."},
  { "trimer",	DD_SetDDelta,
    "trimer <residue1> <from1> <to1> <residue2> <from2> <to2> <centralres>",
    "Specifies the shift correction that will be applied when the specified trimer connection is made."},
  { "rule",	RU_AddRule,        "rule <name> { ... }",
    "Adds a metabolic pathway rule to be used for structure generation criteria. The rule is specified by the commands between { and }."},
  { "build",	BU_SetStruct,         "build <name> { ... }",
    "Creates a structure called <name>.\nThe structure is defined by the commands contained within the { and }."},
  { "multibuild",	BU_SetStruct,         "multibuild <name>",
    "Uses multiple chains in a loaded CCPN project to build CASPER structures. <name> is appended with numbers."},
  { "ldexp",	SP_LoadExp,           "ldexp <type> <name> <filename>|'-'|'*'",
    "Reads experimental spectrum.\n<type> is C or H depending on what type of spectrum it is.\n<name> is what the spectrum will be called.\nReads from file if <filename> is supplied.\nIf no filename is given the user must type '-' or '*'.\n'-' means that the user will be actively prompted for input. '*' lets the user enter the shifts without being prompted. Input must end with '*' in either of these modes."},
  { "ldassign", SP_LoadAssignments,   "ldassign <sim> <exp>",
    "Loads existing assignments from CCPN project. This means that assignments can be evaluated and if only a few assignments are known CASPER can help assigning the rest. It uses the structure <sim> as template for assignments."},
  { "multildassign", SP_LoadAssignments,   "multildassign <sim> <exp>",
    "Loads existing assignments from CCPN project. This means that assignments can be evaluated and if only a few assignments are known CASPER can help assigning the rest. Multiple <sim> structures will be loaded - rising numbers from 1 are appended. These will be put in multiple <exp> structures.\nmultildassign sim exp\t\t will load sim1 into exp1, sim2 into exp2 and so on."},
  { "migrateassign", SP_MigrateSimulatedAssignments, "migrateassign <sim> <exp>",
    "Copies shifts from <sim> to <exp> for atoms that have not been assigned experimental chemical shifts."},
  { "multimigrateassign", SP_MigrateSimulatedAssignments, "multimigrateassign <sim> <exp>",
    "Copies shifts from <sim> to <exp> for atoms that have not been assigned experimental chemical shifts. This will be performed for all structures starting with <sim> and <exp> followed by a rising number."},
  { "assign",	SP_Assign,            "assign <sim> <exp>",
    "Assigns experimental spectrum."},
  { "multiassign",	SP_Assign,            "multiassign <sim> <exp>",
    "Assigns experimental spectra based on simulated spectra. The names of experimental and simulated spectra start with the specified name and are suffixed by rising numbers starting from 1."},
  { "correct",	SP_Correct,           "correct <exp> [<c-off> <h-off>]",
    "Corrects experimental spectrum <exp> for errors in reference.\nIf <c-off> and <h-off> are not specified the corrections are read from the structures and applied."},
  { "multicorrect", SP_Correct,       "multicorrect <exp> [<c-off> <h-off>]",
    "Corrects multiple experimental spectra starting with <exp> and a rising number.\nIf <c-off> and <h-off> are not specified the corrections are read from the structures and applied."},
  { "gnuplot", SP_Gnuplot,            "gnuplot <type> <str1> [<str2>] [<output filename>]",
    "Plots spectra of <type> using gnuplot. If two structures are specified both will be plotted. If an output filename is specified the results will only be directed to that file."},
  { "generate",	SE_Generate,          "generate <name> <exp> { ... }",
    ""},
  { "purgelist", PE_RePurge,          "purgelist  <lower> <upper> C|H|CH|HH|CL",
    "Purges the current list of structures. It keeps a number of structures specified by <lower>. The structures are sorted according to the specified score type."},
  

  { "tabres",	TA_TabulateResidues, "tabres",
    "Generates a table of residues from the databases."},
  { "tabdi",	TA_TabulateDimers,   "tabdi",
    "Generates a table of dimers from the databases."},
  { "tabtri",	TA_TabulateTrimers,  "tabtri",
    "Generates a table of trimers from the databases."},

  { "renumber",	GFX_Renumber,         "renumber <structure>",
    ""},
  { "print3d", BU_PrintStruct3DLink,  "print3d <structure>",
    "Prints a URL for generating the 3D structure of <structure> using Glycam Online Carbohydrate Builder."},
  { "pdbgen", CM_CcpnPdb, "pdbgen [<structure>]",
    "Generates a pdb file of <structure> based on coordinates specified in CCPN. If no structure is specified the first molecule system is used"},
  { "xmlstr",	XML_structure,        "xmlstr <structure>",
    "Outputs the data of a Casper structure in XML format."},

  { NULL,	NULL,                 NULL,
    NULL}
};


/* SYNTAX: System <cmd string> */
int CM_System()
{
  int status;
  PA_GetString;
  if (PA_SECURE) Error(PA_ERR_FAIL, "Security violation");
  status=system(PA_Result.String);
  if (status) Error(PA_ERR_FAIL,"System call failed");
  return(PA_ERR_OK);
}

int CM_Echo()
{
  char type;
  type=PA_GetArg();
  switch (type) {
  case PA_FLOAT:	printf("%.2f", TopValue.Float);
    break;
  case PA_STRING:	printf("%s", TopValue.String);
    break;
  default:		break; /* action? */
  };
  puts("");
  return(PA_ERR_OK);
}

/* SYNTAX: Load <file>|'-' */
/* Run a script or prompt for input (file=-) */
int CM_Load()
{
  char status;
  FILE *file;

  PA_GetString;
  if (PA_Result.String[0]=='-')	/* read stdin */
    status=PA_Parse(stdin , stdout, "Console", PA_Status->Commands/*GlobalCmds*/);
  else
    {
      file=fopen(PA_Result.String,"r");
      if (file==NULL) Error(PA_ERR_FAIL,"Can't open file for input");
      /*PA_ClrPrompt;*/
      status=PA_Parse(file , stdout, PA_Result.String, PA_Status->Commands/*GlobalCmds*/);
      fclose(file);
    };
  if (status==PA_SUCCESS) return(PA_ERR_OK);
  Error(PA_ERR_WARN,"Execution failed");
}

/* SYNTAX: List TYpe|VAriable|REsidue|DIsaccharide|STructure */
/* Lists int info for structures of a kind */
/* Details are dependent on which structure is 'listed' */
int CM_List()
{
  int mode;	/* What 'method'? */
  int i;

  struct RE_Residue *res;
  int cl, ch, la, ha;

  struct TY_Type *type;
  PA_GetString;	/* Determine 'method' */
  mode=(toupper(PA_Result.String[0])<<8)+toupper(PA_Result.String[1]);
  switch (mode)
    {
    case CM_ID_TYPE:	/* do 'type'-stuff */
      ME_PrintList(&TypeMethod, &TypeList);
      break;
    case CM_ID_VARS:	/* do 'variable'-stuff */
      ME_PrintList(&VariableMethod, &VariableList);
      break;
    case CM_ID_RESI:	/* do 'residue'-stuff */
      ME_PrintList(&ResidueMethod, &ResidueList);
      break;
    case CM_ID_DISA:	/* do 'delta'-stuff */
      /* residue loop */
      for (res=(struct RE_Residue *)ResidueList.Head.Succ;
	   res->Node.Succ!=NULL; res=(struct RE_Residue *)res->Node.Succ)
	{
	  for (i=0; i<TY_MAX_HEAVY; i++)
	    ME_PrintList(&DeltaMethod, &(res->Delta[i]) );
	};

      break;
    case CM_ID_STRU:	/* do structure stuff */
      ME_PrintList(&StructMethod, &StructList);
      break;
    case CM_ID_SPEC:	/* do other structure stuff */
      ME_PrintList(&SpectrumMethod, &StructList);
      break;
    case CM_ID_TRIS:	/* trisaccharide fragments */
      for (la=0; la<2; la++)	/* low anomer */
	for (ha=0; ha<2; ha++)	/* high anomer */
	  for (cl=0; cl<2; cl++)	/* center lo */
	    for (ch=0; ch<2; ch++)	/* center hi */
	      {
		printf("%d %d %d %d\n", la, ha, cl, ch);
		/* for (ddelta=(struct DD_Delta *)DD_Branch[la][ha][cl][ch].Head.Node.Succ;
		   ddelta->Node.Succ!=NULL;
		   ddelta=(struct DD_Delta *)ddelta->Node.Succ) */
		ME_PrintList(&DD_DeltaMethod, &(DD_Branch[la][ha][cl][ch]) );
	      };
      break;
    case CM_ID_CONV:	for (type=(struct TY_Type *)TypeList.Head.Succ;
			     type->Node.Succ!=NULL; type=(struct TY_Type *)type->Node.Succ)
      { ME_PrintList(&ConversionMethod, &type->Conversion); };
      break;
    case CM_ID_RULE:
      ME_PrintList(&RU_RuleMethod, &RuleList);
      break;
    default:		/* Unkown mode */
      Error(PA_ERR_FAIL,"No such type of data");
    }
  return(PA_ERR_OK);
}


/* SYNTAX: End */
/* exits a script file */
int CM_End()
{
  return(PA_ERR_END);
}


/* SYNTAX: Quit */
/* Terminates program execution */
int CM_Quit()
{
  /* This will leave all previous nesting states */
  PA_Status->Nesting=-1;
  return(PA_ERR_END);		/* keeps compiler happy */
}

/* SYNTAX: Show|Save|Clear TYpe|VAriable  */
/* Show data for a type */
/* Details are dependent on which structure is 'shown' */
int CM_Action(char action)
{
  int mode;	/* What 'method'? */
  struct Node *node;	/* What node */
  struct ME_Method *method;
  struct DE_DimerDesc desc;
  struct DD_TrimerDesc tdesc;
  struct RE_Residue *res;
  int la,ha,cl,ch;
  int stat, i;
  PA_GetString;	/* Determine 'method' */
  mode=(toupper(PA_Result.String[0])<<8)+toupper(PA_Result.String[1]);
  PA_GetString;	/* Get node name */
  /* --------*/
  if (strcmp(PA_Result.String,"*")==0)
    {
      switch (mode)
	{
	case CM_ID_TYPE:	node=(struct Node *)TypeList.Head.Succ;
	  method=&TypeMethod;
	  break;
	case CM_ID_VARS:	node=(struct Node *)VariableList.Head.Succ;
	  method=&VariableMethod;
	  break;
	case CM_ID_RESI:	node=(struct Node *)ResidueList.Head.Succ;
	  method=&ResidueMethod;
	  break;
	case CM_ID_DISA:	/* do 'delta'-stuff */
	  for (res=(struct RE_Residue *)ResidueList.Head.Succ;
	       res->Node.Succ!=NULL; res=(struct RE_Residue *)res->Node.Succ)
	    {
	      for (i=0; i<TY_MAX_HEAVY; i++)
		{
		  for (node=(struct Node *)res->Delta[i].Head.Succ; node->Succ!=NULL;
		       node=(struct Node *)node->Succ)
		    switch(action)
		      {
		      case CM_ACTION_SHOW:	ME_PrintNode(&DeltaMethod, node);
			break;
		      case CM_ACTION_SAVE:	ME_SaveNode(&DeltaMethod, node);
			break;
		      case CM_ACTION_FREE:	ME_RemoveNode(&DeltaMethod, node);
			break;
		      };
		};
	    };
	  return(PA_ERR_OK);
	case CM_ID_TRIS:	for (la=0; la<2; la++)	/* low anomer */
	  for (ha=0; ha<2; ha++)	/* high anomer */
	    for (cl=0; cl<2; cl++)	/* center lo */
	      for (ch=0; ch<2; ch++)	/* center hi */
		{
		  for (node=(struct Node *)DD_Branch[la][ha][cl][ch].Head.Succ;
		       node->Succ!=NULL; node=(struct Node *)node->Succ)
		    switch(action)
		      {
		      case CM_ACTION_SHOW:	ME_PrintNode(&DD_DeltaMethod, node);
			break;
		      case CM_ACTION_SAVE:	ME_SaveNode(&DD_DeltaMethod, node);
			break;
		      case CM_ACTION_FREE:	ME_RemoveNode(&DD_DeltaMethod, node);
			break;
		      };
		};
	  return(PA_ERR_OK);
	case CM_ID_STRU:	node=(struct Node *)StructList.Head.Succ;
	  method=&StructMethod;
	  break;
	case CM_ID_SPEC:	node=(struct Node *)StructList.Head.Succ;
	  method=&SpectrumMethod;
	  break;
	case CM_ID_RULE:        node=(struct Node *)StructList.Head.Succ;
	  method=&RU_RuleMethod;
	  break;
	default:		Error(PA_ERR_FAIL,"No such type of data");
	};

      /* apply to all */
      for (;node->Succ!=NULL; node=node->Succ)
	{
	  switch(action)
	    {
	    case CM_ACTION_SHOW:	ME_PrintNode(method, node);
	      break;
	    case CM_ACTION_SAVE:	ME_SaveNode(method, node);
	      break;
	    case CM_ACTION_FREE:	ME_RemoveNode(method, node);
	      break;
	    };
	};
      return(PA_ERR_OK);
    };
  /* ------ */
  switch (mode)
    {
    case CM_ID_TYPE:	node=(struct Node *)FindNode(&(TypeList.Head), PA_Result.String);
      method=&TypeMethod;
      break;
    case CM_ID_VARS:	node=(struct Node *)FindNode(&(VariableList.Head), PA_Result.String);
      method=&VariableMethod;
      break;
    case CM_ID_RESI:	node=(struct Node *)FindNode(&(ResidueList.Head), PA_Result.String);
      method=&ResidueMethod;
      break;
    case CM_ID_DISA:	stat=DE_ReadDelta(&desc, PA_Result.String);
      if (stat!=PA_ERR_OK) return(stat);
      /* assumes that first res is nonreducing end */
      node=(struct Node *)
	FindNode(&(desc.Residue[DE_REDUCING]->Delta[desc.Atom[DE_REDUCING]].Head),
		 desc.Residue[DE_NONRED]->Node.Name);
      method=&DeltaMethod;
      break;
    case CM_ID_TRIS:	stat=DD_ReadDelta(&tdesc, PA_Result.String);
      if (stat!=PA_ERR_OK) return(stat);
      node=NULL;
      /*      for(la=0;la<2;la++)
	{
	  for(ha=0;ha<2;ha++)
	    {
	      for(cl=0;cl<2;cl++)
		{
		  for(ch=0;ch<2;ch++)
		    {
		      node=(struct Node *)FindNode(&(DD_Branch[la][ha][cl][ch].Head),PA_Result.String);
		      if(node)
			{
			  printf("Found it\n");
			  ch=2;
			  cl=2;
			  ha=2;
			  la=2;
			}
		    }
		}
	    }
	    }*/
      /*      node=(struct Node *)
	FindNode(&(desc.Residue[DE_REDUCING]->Delta[desc.Atom[DE_REDUCING]].Head),
	desc.Residue[DE_NONRED]->Node.Name);*/
      method=&DD_DeltaMethod;
      break;
    case CM_ID_STRU:
    case CM_ID_SPEC:
      if(PA_Result.String[0]=='/')
	{
	  if(projectValid)
	    {
	      if(!CC_GetNmrProject(&Global_CcpnProject,&Global_NmrProject))
		{
		  Error(PA_ERR_FATAL,"Cannot open project.");
		}
	      node=(struct Node *)
		FindNode((struct Node *)&StructList,
			 ApiString_Get(Nmr_NmrProject_GetName(Global_NmrProject)));
	    }
	  /* Find the first structure that does not start with "sim" */
	  else
	    {
	      for(node=StructList.Head.Succ; node->Succ!=NULL && strncasecmp("sim", node->Name, strlen("sim"))==0;
		  node=node->Succ)
		{
		}
	      if(node->Succ==NULL)
		{
		  node=NULL;
		}	      
	    }
	}
      else
	{
	  node=(struct Node *)FindNode((struct Node *)&StructList, PA_Result.String);
	}
      if(mode==CM_ID_STRU)
	{
	  method=&StructMethod;
	}
      else
	{
	  method=&SpectrumMethod;
	}
      break;
    case CM_ID_RULE:
      node=(struct Node *)FindNode((struct Node *)&RuleList, PA_Result.String);
      method=&RU_RuleMethod;
      break;
    default:		Error(PA_ERR_FAIL,"No such type of data");
    }
  if (node==NULL) Error(PA_ERR_FAIL,"Not found");
  switch(action)
    {
    case CM_ACTION_SHOW:	ME_PrintNode(method, node);
      break;
    case CM_ACTION_SAVE:	ME_SaveNode(method, node);
      break;
    case CM_ACTION_FREE:	ME_RemoveNode(method, node);
      break;
    };
  return(PA_ERR_OK);
}

/* SYNTAX: Save <type> <node> */
int CM_Save()
{
  return( CM_Action(CM_ACTION_SAVE) );
}

/* SYNTAX: ccpnsave <filename> */
int CM_Ccpnsave()
{
  char name[128];
  /*  char *tempname;*/

  if(PA_GetArg()==PA_STRING)
    {
      strcpy(name,PA_Result.String);
    }
  else
    {
      strcpy(name,"");
    }

  return(CC_SaveProject(&Global_CcpnProject, name));
  /*  tempname=basename(name);
  Impl_Project_SetPath(Global_CcpnProject,ApiString_New(tempname));
  return(Impl_Project_SaveModified(Global_CcpnProject));*/
}

/* SYNTAX: ccpnload <filename> */
int CM_Ccpnload()
{
  PA_GetString;
  if(!CC_LoadProject(&Global_CcpnProject, PA_Result.String))
    {
      Error(PA_ERR_FAIL,"Cannot open project file.");
    }
  return PA_ERR_OK;
}

int CM_Ccpnexcpt()
{
  printRaisedException();
  return PA_ERR_OK;
}

/* Flags the Ccpn project as invalid. This means that all future ccpn related actions are skipped. */
int CM_NoCcpn()
{
  projectValid=0;
  return PA_ERR_OK;
}

/* Generates a pdb structure using coordinates from the chemcomps put together */
/* SYNTAX: pdbgen <str> */
int CM_CcpnPdb()
{
  char str[64];
  char filename[256];

  PA_GetString;
  strcpy(str, PA_Result.String);
  PA_GetString;
  strcpy(filename, PA_Result.String);
  if(!CC_CcpnPdb(&Global_CcpnProject, str, filename))
    {
      Error(PA_ERR_FAIL,"Cannot generate pdb.");
    }
  return PA_ERR_OK;
}

/* SYNTAX: Show <type> <node> */
int CM_Show()
{
  return( CM_Action(CM_ACTION_SHOW) );
}

/* SYNTAX: Clear <type> <node> */
int CM_Clear()
{
  return( CM_Action(CM_ACTION_FREE) );
}

/* These are the CASPER "system" commands. These are always available. */
struct PA_Command GlobalCmds[]={
  { "echo",	CM_Echo,        "echo <string>",
    "Outputs the argument.\nStrings are kept as strings while numerical arguments are printed as 2 decimal floats."},
  { "load",	CM_Load,        "load <file>|'-'",
    "Runs a script from <file>.\nIf no file ('-') is given prompts the user for commands."},
  { "%",        CM_Load,        "% <file>|'-'",
    "Runs a script from <file>.\nIf no file ('-') is given prompts the user for commands."},
  { "show",	CM_Show,        "show <type> <node>",
    "Shows the <node> of <type> where <type> is TYpe|VAriable|REsidue|DIsaccharide|STructure|SPectrum|RUle.\nExample:\tshow st sim\n\t\tshow va h_cutoff"},
  { "list",	CM_List,
    "list TYpe|VAriable|REsidue|DIsaccharide|TRisaccharide|STructure|SPectrum|RUle",
    "Lists short info of a given kind.\nDetails are dependent on which kind of info is 'listed'.\nExample:\tlist st\n\t\tlist va"},
  { "save",	CM_Save,        "save <type> <node>",
    "Echoes the commands required to create the <type> <node> where <type> can be TY|VA|RE|DI|TR|ST|SP|RU\nExample:\tsave re adglc"},
  { "ccpnsave", CM_Ccpnsave,    "ccpnsave <filename>",
    "Saves the current project to a ccpn project xml structure."},
  { "projectsave", CM_Ccpnsave, "projectsave <filename>",
    "Saves the current project to a ccpn project xml structure."},
  { "ccpnload", CM_Ccpnload,    "ccpnload <filename>",
    "Loads a ccpn project xml file."},
  { "projectload", CM_Ccpnload, "projectload <filename>",
    "Loads a ccpn project xml file."},
  { "ccpnexception", CM_Ccpnexcpt, "ccpnexception",
    "Prints a raised exception from CCPN. Good for investigating why a project is invalid."},
  { "disableccpn", CM_NoCcpn, "disableccpn",
    "Disables CCPN related commands. This can speed things up if CCPN compatibility is not required."},
  { "clear",	CM_Clear,       "clear <type> <node>",
    "Removes the <type> <node> where <type> can be TY|VA|RE|DI|TR|ST|SP.\nExample:\tclear re exp"},
  { "system",	CM_System,      "system <cmd string>",
    "Runs the system command provided. If more than one argument is given the string must be contained within ''.\nExample:\tsystem ls\n\t\tsystem 'ls -al'"},
  { "set",	VA_SetVariable, "set <var> <value>",
    "Assigns <value> to variable <var>."},
  { "pop",	VA_Pop,         "pop <var>",
    "Reads a token from the previous 'file'."},
  { "if",	VA_If,          "if <expr> { statements }",
    "Executes a list of statements if an expression is true.\nExample:\tif(text='text') {\n\t\tldexp C exp *\n\n\t\t*\n\t\t}"},
  { "quit",	CM_Quit,        "quit",
    "Quits Casper."},
  { "plot",	PL_Plot,        "plot ps|hpgl <file>"
    ""},
  { "?",        CM_Help,        "? [<command name>]",
    "Displays syntax and description if a command is given.\nIf no command is given lists all currently available commands with syntax."},
  { "man",      CM_Help,        "man [<command name>]",
    "Displays syntax and description if a command is given.\nIf no command is given lists all currently available commands with syntax."},

  { NULL,	NULL,           NULL,
    NULL}
};


/* Displays Syntax and description if a command is given. */
/* If no command is given lists all currently available commands with syntax.*/
/* SYNTAX: ?   [<command name>]   */
/*         man [<command name>]   */
int CM_Help()
{
  int i=-1,j=-1,cnt;
  char *prevpos;
  
  /* See if there is an argument following the command */
  PA_SkipSpc();
  if(*PA_Status->CmdPtr!=0)
    {
      prevpos=PA_Status->CmdPtr; /* Keep the position for the next search */
      i=PA_FindCmd(GlobalCmds);  /* Look in GlobalCmds */
      if(i!=-1)
	{
	  printf("%s\nSyntax: %s\n",GlobalCmds[i].Name,GlobalCmds[i].Syntax);
	  printf("%s\n",GlobalCmds[i].Desc);
	}
      else
	{
	  PA_Status->CmdPtr=prevpos;
	  i=PA_FindCmd(PA_Status->Commands);
	  if(i!=-1)
	    {
	      printf("%s\nSyntax: %s\n",PA_Status->Commands[i].Name,
		                        PA_Status->Commands[i].Syntax);
	      printf("%s\n",PA_Status->Commands[i].Desc);
	    }
	}
    }
  /* If the user command was just man or ? without an argument */
  else
    {
      printf("Global Commands:\n");
      while (GlobalCmds[i+1].Name!=NULL)
	{
	  printf("%s",GlobalCmds[i+1].Name);
	  cnt=strlen(GlobalCmds[i+1].Name);
	  while(cnt<20)
	    {
	      printf(" ");
	      cnt++;
	    }
	  printf("Syntax: %s\n",GlobalCmds[i+1].Syntax);
	  i++;    
	}
      if(i!=-1)
	printf("\n\n");
      while (PA_Status->Commands[j+1].Name!=NULL)
	{
	  if(j==-1)    /* First row */
	    printf("(Sub)commands:\n");
	  printf("%s",PA_Status->Commands[j+1].Name);
	  cnt=strlen(PA_Status->Commands[j+1].Name);
	  while(cnt<20)
	    {
	      printf(" ");
	      cnt++;
	    }
	  printf("Syntax: %s\n",PA_Status->Commands[j+1].Syntax);
	  j++;
	};
    };
  if(i!=-1||j!=-1)
    return(PA_ERR_OK);
  else
    Error(PA_ERR_FAIL,"No matching commands found");
}

/*
  "compare",CompareSpec,2,CT_NONE,	"Compare <simulated> <experimental>");;
  "bestfit",BestFitSpec,2,CT_NONE,	"BestFit <experimental> <simulated>");
*/
