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

/* Filename = function.c */
/* Prefix = FN */
/* Some built in functions and 'variables' */

#include "function.h"
#include "calc.h"
#include "spectra.h"
#include <string.h>
#include <strings.h>

extern struct List VariableList;

/* 'Error-functions' - no read/write */
int FN_NoRead()
{
  PushString("");
  Error(PA_ERR_FAIL,"Can't read from internal variable");
}

int FN_NoWrite()
{
  Error(PA_ERR_FAIL,"Can't write to internal variable");
}

/* Produces string with version info */
int FN_GetVersion()
{
  PushString(FN_VERSION);
  return(PA_ERR_OK);
}

/* Reads trace status */
int FN_GetTrace()
{
  if (PA_TestTrace)
    {
      PushString("on");
    }
  else
    {
      PushString("off") ;
    }
  return(PA_ERR_OK);
}

/* Sets/resets trace status */
int FN_SetTrace()
{
  if (TopType==EX_STRING)
    {
      if (strcasecmp(PA_Result.String,"on")==0 )
	{
	  PA_SetTrace;
	  return(PA_ERR_OK);
	}
      if (strcasecmp(PA_Result.String,"off")==0 )
	{
	  PA_ClrTrace;
	  return(PA_ERR_OK);
	}
    }
  Error(PA_ERR_FAIL,"Trace can only be one of on|off");
}


/* Reads prompt status */
int FN_GetPrompt()
{
  if (PA_TestPrompt)
    { 
      PushString(PA_Status->Prompt);
    }
  else
    {
      PushString("off");
    }
  return(PA_ERR_OK);
}

/* Sets/resets prompt status */
int FN_SetPrompt()
{
  if (TopType==EX_STRING)
    {
      if (strcasecmp(PA_Result.String,"off")==0 )
	{
	  PA_ClrPrompt;
	  return(PA_ERR_OK);
	}
      PA_SetPrompt;
      if (strcasecmp(PA_Result.String,"on")==0)
	{
	  strcpy(PA_Status->Prompt, PA_DEF_PROMPT);
	}
      else
	{
	  strncpy(PA_Status->Prompt, PA_Result.String, PA_CMD_LEN);
	}
      return(PA_ERR_OK);
    }
  Error(PA_ERR_FAIL,"Prompt must be string");
}


/* Sets stream for error messages */
int FN_SetError()
{
  FILE *error;
  if (TopType==EX_STRING)
    {
      if (strcmp(PA_Result.String,"-")==0 )
	{
	  if (PA_Status->Error!=stdout)
	    {
	      fclose(PA_Status->Error);
	      PA_Status->Error=stdout;
	    }
	  return(PA_ERR_OK);
	}
      error=fopen(PA_Result.String, "w");
      if (error==NULL)
	{
	  Error(PA_ERR_FAIL, "Can't open file");
	}
      if (PA_Status->Error!=stdout)
	{
	  fclose(PA_Status->Error);
	}
      PA_Status->Error=error;
      return(PA_ERR_OK);
    }
  Error(PA_ERR_FAIL,"Expecting file name");
}


/* Reads mode */
int FN_GetGenMode()
{
  char buffer[4];
  buffer[0]='D';
  buffer[1]='0';
  buffer[2]='S';
  buffer[3]=0;
  if (CA_Mode&CA_TRISACCH)
    {
      buffer[0]='T';
    }
  if (CA_Mode&CA_DEFAULT)
    {
      buffer[1]='1';
    }
  if (CA_Mode&CA_DOUBLE)
    {
      buffer[1]='2';
    }
  if (CA_Mode&CA_VERBOSE)
    {
      buffer[2]='V';
    }
  PushString(buffer);
  return(PA_ERR_OK);
}

/* Sets/resets mode status */
int FN_SetGenMode()
{
  int i;
  if (TopType==EX_STRING)
    {
      for (i=0; PA_Result.String[i]!=0; i++)
	{
	  switch (PA_Result.String[i])
	    {
	    case 't':
	    case 'T':	
	      CA_Mode|=CA_TRISACCH;
	      break;
	    case 'd':
	    case 'D':
	      CA_Mode&=~CA_TRISACCH;
	      break;
	    case 'v':
	    case 'V':
	      CA_Mode|=CA_VERBOSE;
	      break;
	    case 's':
	    case 'S':
	      CA_Mode&=~CA_VERBOSE;
	      break;
	    case '0':
	      CA_Mode&=~CA_DEFAULT;
	      CA_Mode&=~CA_DOUBLE;
	      break;
	    case '1':
	      CA_Mode|=CA_DEFAULT;
	      CA_Mode&=~CA_DOUBLE;
	      break;
	    case '2':
	      CA_Mode|=CA_DEFAULT;
	      CA_Mode|=CA_DOUBLE;
	      break;
	    default:
	      Error(PA_ERR_WARN, "Invalid mode flag");
	    }
	}
      return(PA_ERR_OK);
    }
  Error(PA_ERR_FAIL,"Mode must be string of 0,1,2,D,S,T and V");
  /*
    CA_DISACCH	0x00	 No branchpoint corrections D+/-
    CA_TRISACCH	0x01	 Branchpoint corrections	T+/-
    CA_DEFAULT	0x02	 Defaults enabled R+/-
    CA_DOUBLE	0x04	 'Double'-defaulting enabled R++
    CA_VERBOSE	0x10	 Print all bonds and delta-sets P+/-
    three letter string? D/[T] 0/[1]/2 [S]/V
  */
}

int FN_GetSafe()
{
  PushFloat(PA_Safe);
  return(PA_ERR_OK);
}

/* changes security level */
/* safe=0 no restrictions */
/* safe=1 no system calls */
/* safe=2 allow only relative file paths */
/* safe=3 fix all I/O - no more redirections, no new files */
/* safe can only be increased! */
int FN_SetSafe()
{
  if (TopType==EX_FLOAT)
    {
      if (PA_Result.Float>=PA_Safe)
	{
	  PA_Safe=PA_Result.Float;
	  return(PA_ERR_OK);
	}
      Error(PA_ERR_FAIL, "Security level can not be reduced");
    }
  PA_Safe+=1;	/* if there is an error assume that increase was intended */
  Error(PA_ERR_FAIL, "Security level must be number, security increased");
}

/* Reads verbose status */
int FN_GetVerb()
{
  if (CA_Mode&CA_VERBOSE)
    {
      PushString("on");
    }
  else
    {
      PushString("off");
    }
  return(PA_ERR_OK);
}

/* switches verbose mode on/off */
int FN_SetVerb()
{
  if (TopType==EX_STRING)
    {
      if (strcasecmp(PA_Result.String,"on")==0 )
	{
	  CA_Mode|=CA_VERBOSE;
	  return(PA_ERR_OK);
	}
      if (strcasecmp(PA_Result.String,"off")==0 )
	{
	  CA_Mode&=~CA_VERBOSE;
	  return(PA_ERR_OK);
	}
    }
  Error(PA_ERR_FAIL,"Verbose can only be one of on|off");
}

/* Reads bp-correction status */
int FN_GetBranch()
{
  if (CA_Mode&CA_TRISACCH)
    {
      PushString("on");
    }
  else
    {
      PushString("off");
    }
  return(PA_ERR_OK);
}

/* switches verbose mode on/off */
int FN_SetBranch()
{
  if (TopType==EX_STRING)
    {
      if (strcasecmp(PA_Result.String,"on")==0 )
	{
	  CA_Mode|=CA_TRISACCH;
	  return(PA_ERR_OK);
	}
      if (strcasecmp(PA_Result.String,"off")==0 )
	{
	  CA_Mode&=~CA_TRISACCH;
	  return(PA_ERR_OK);
	}
    }
  Error(PA_ERR_FAIL,"Branch can only be one of on|off");
}

struct VA_Variable FN_Version={
  {			/* struct Node{ */
    &(VariableList.Head),	/* *Pred */
    &(FN_Trace.Node),	/* *Succ */ 
    "version"		/* char Name[] */
  },
  {			/* struct Value{ */
    EX_EXEC,		/* char Type; */
    {   	 	/* struct Function */
      {
	FN_GetVersion,	/* Read function */
	FN_NoWrite		/* Write function */
      }
    }
  }
};

struct VA_Variable FN_Trace={
  {			/* struct Node{ */
    &(FN_Version.Node),    /* *Pred */
    &(FN_Prompt.Node),	/* *Succ */ 
    "trace"		/* char Name[] */
  },
  {			/* struct Value{ */
    EX_EXEC,		/* char Type; */
    {			/* struct Function */
      {
	FN_GetTrace,		/* Read function */
	FN_SetTrace		/* Write function */
      }
    }
  }
};

struct VA_Variable FN_Prompt={
  {			/* struct Node{ */
    &(FN_Trace.Node),      /* *Pred */
    &(FN_GenMode.Node),	/* *Succ */ 
    "prompt"		/* char Name[] */
  },
  {			/* struct Value{ */
    EX_EXEC,		/* char Type; */
    {			/* struct Function */
      {
	FN_GetPrompt,		/* Read function */
	FN_SetPrompt		/* Write function */
      }
    }
  }
};


struct VA_Variable FN_GenMode={
  {			/* struct Node{ */
    &(FN_Prompt.Node),     /* *Pred */
    &(FN_Error.Node),	/* *Succ */ 
    "mode"		 	/* char Name[] */
  },
  {			/* struct Value{ */
    EX_EXEC,		/* char Type; */
    {		 	/* struct Function */
      {
	FN_GetGenMode,	/* Read function */
	FN_SetGenMode		/* Write function */
      }
    }
  }
};

struct VA_Variable FN_Error={
  {			/* struct Node { */
    &(FN_GenMode.Node),	/* *Pred */
    &(FN_Safe.Node),	/* *Succ */ 
    "error"		/* char Name[] */
  },
  {			/* struct Value{ */
    EX_EXEC,		/* char Type; */
    {			/* struct Function */
      {
	FN_NoRead,		/* Read function */
	FN_SetError		/* Write function */
      }
    }
  }
};

struct VA_Variable FN_Safe={
  {			/* struct Node { */
    &(FN_Error.Node),	/* *Pred */
    &(FN_Verbose.Node),	/* *Succ */ 
    "safe"			/* char Name[] */
  },
  {			/* struct Value{ */
    EX_EXEC,		/* char Type; */
    {			/* struct Function */
      {
	FN_GetSafe,		/* Read function */
	FN_SetSafe		/* Write function */
      }
    }
  }
};

struct VA_Variable FN_Verbose={
  {			/* struct Node { */
    &(FN_Safe.Node),	/* *Pred */
    &(FN_Branch.Node),	/* *Succ */ 
    "verbose"			/* char Name[] */
  },
  {			/* struct Value{ */
    EX_EXEC,		/* char Type; */
    {			/* struct Function */
      {
	FN_GetVerb,		/* Read function */
	FN_SetVerb		/* Write function */
      }
    }
  }
};

struct VA_Variable FN_Branch={
  {			/* struct Node { */
    &(FN_Verbose.Node),	/* *Pred */
    &(FN_AssMode.Node),	/* *Succ */ 
    "branch"			/* char Name[] */
  },
  {			/* struct Value{ */
    EX_EXEC,		/* char Type; */
    {			/* struct Function */
      {
	FN_GetBranch,		/* Read function */
	FN_SetBranch		/* Write function */
      }
    }
  }
};

/* int FN_GetHCutoff()
   { PushFloat(SP_H_Cutoff); return(PA_ERR_OK);}

   int FN_SetHCutoff()
   { if (TopType==EX_STRING) Error(PA_ERR_FAIL, "Must be number");
   SP_H_Cutoff=PA_Result.Float;
   return(PA_ERR_OK); } */

int FN_GetAssMode()
{
  if (SP_AssignMode==SP_AM_ACC)
    {
      PushString("accurate");
    }
  else
    {
      PushString("cutoff");
    }
  return(PA_ERR_OK);
}

int FN_SetAssMode()
{
  if (TopType==EX_STRING)
    {
      if (strcasecmp(PA_Result.String,"accurate")==0 )
	{
	  SP_AssignMode=SP_AM_ACC;
	  return(PA_ERR_OK);
	}
      if (strcasecmp(PA_Result.String,"cutoff")==0 )
	{
	  SP_AssignMode=SP_AM_CUTOFF;
	  return(PA_ERR_OK);
	}
    }
  Error(PA_ERR_FAIL,"A_Mode can only be ACCURATE or CUTOFF");
}


/* assignment and ranking */
struct VA_Variable FN_AssMode={
  {			/* struct Node { */
    &(FN_Branch.Node),	/* *Pred */
    &(FN_ChLrRange.Node),	/* *Succ */ 
    "a_mode"			/* char Name[] */
  },
  {			/* struct Value{ */
    EX_EXEC,		/* char Type; */
    {			/* struct Function */
      {
	FN_GetAssMode,	/* Read function */
	FN_SetAssMode		/* Write function */
      }
    }
  }
};


/*struct VA_Variable FN_HCutoff={
  { &(FN_AssMode.Node), &(FN_CCutoff.Node), 
    "h_cutoff" },
  { EX_FPTR,
    {{ NULL, NULL }, 0, "", &SP_H_Cutoff} } };

struct VA_Variable FN_CCutoff={
  { &(FN_HCutoff.Node), &(FN_CHCutoff.Node), 
    "c_cutoff" },
  { EX_FPTR,
    {{ NULL, NULL }, 0, "", &SP_C_Cutoff} } };

struct VA_Variable FN_CHCutoff={
  { &(FN_CCutoff.Node), &(FN_HHCutoff.Node), 
    "ch_cutoff" },
  { EX_FPTR,
    {{ NULL, NULL }, 0, "", &SP_CH_Cutoff} } };

struct VA_Variable FN_HHCutoff={
  { &(FN_CHCutoff.Node), &(FN_ChLrCutoff.Node), 
    "hh_cutoff" },
  { EX_FPTR,
    {{ NULL, NULL }, 0, "", &SP_HH_Cutoff} } };

struct VA_Variable FN_ChLrCutoff={
  { &(FN_HHCutoff.Node), &(FN_ChLrRange.Node), 
    "chlr_cutoff" },
  { EX_FPTR,
    {{ NULL, NULL }, 0, "", &SP_ChLr_Cutoff} } };
*/

int FN_GetChLrRange()
{
  
  return(PA_ERR_OK);
}

int FN_SetChLrRange()
{
  
	  return(PA_ERR_OK);
	  
}

struct VA_Variable FN_ChLrRange={
  {			/* struct Node { */
    &FN_AssMode.Node,	/* *Pred */
    &VariableList.Tail,	/* *Succ */ 
    "chlrrange"		/* char Name[] */
  },
  {			/* struct Value{ */
    EX_EXEC,		/* char Type; */
    {			/* struct Function */
      {
	FN_GetChLrRange,	/* Read function */
	FN_SetChLrRange		/* Write function */
      }
    }
  }
};

/*struct VA_Variable FN_H_Scaling={
  { &FN_ChLrRange.Node, &FN_C_Weight.Node, 
    "h_scaling" },
  { EX_FPTR,
    {{ NULL, NULL }, 0, "", &SP_H_Scaling} } };

struct VA_Variable FN_C_Weight={
  { &FN_H_Scaling.Node, &FN_H_Weight.Node, 
    "c_weight" },
  { EX_FPTR,
    {{ NULL, NULL }, 0, "", &SP_C_Weight} } };

struct VA_Variable FN_H_Weight={
  { &FN_C_Weight.Node, &FN_CH_Weight.Node, 
    "h_weight" },
  { EX_FPTR,
    {{ NULL, NULL }, 0, "", &SP_H_Weight} } };

struct VA_Variable FN_CH_Weight={
  { &FN_C_Weight.Node, &FN_HH_Weight.Node, 
    "ch_weight" },
  { EX_FPTR,
    {{ NULL, NULL }, 0, "", &SP_CH_Weight} } };

struct VA_Variable FN_HH_Weight={
  { &FN_CH_Weight.Node, &FN_ChLr_Weight.Node, 
    "hh_weight" },
  { EX_FPTR,
    {{ NULL, NULL }, 0, "", &SP_HH_Weight} } };

struct VA_Variable FN_ChLr_Weight={
  { &FN_HH_Weight.Node, &VariableList.Tail, 
    "chlr_weight" },
  { EX_FPTR,
    {{ NULL, NULL }, 0, "", &SP_ChLr_Weight} } };

*/
