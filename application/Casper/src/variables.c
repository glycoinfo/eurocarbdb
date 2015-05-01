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

/* Filename = variables.c */
/* Prefix = VA */
/* This this file defines various variable routines */

#include "variables.h"
#include "parser.h"
#include "function.h"
#include "commands.h"
#include "expr.h"
#include <string.h>
#include <stdlib.h>

struct List VariableList={
  {NULL, &(FN_Version.Node), "Vars Head"},
  {&(FN_Trace.Node), NULL, "Vars Tail"}
};

/*********************************/
/* Methods for VA_Variable-nodes */
/*********************************/

void *VA_CreateVariable(char *name)
{
  return ( MakeNode(sizeof(struct VA_Variable),name) );
}

/* initialize with an empty string */
void VA_ClearVariable(struct VA_Variable *variable)
{
  variable->Value.Value.String[0]=0;
  variable->Value.Type=EX_STRING;
}

void VA_PrintVariable(struct VA_Variable *variable)
{
  printf("%s = ",variable->Node.Name);
  switch(variable->Value.Type)
    {
    case EX_FLOAT:
      printf("%.2f\n", variable->Value.Value.Float);
      break;
    case EX_STRING:
      printf("'%s'\n", variable->Value.Value.String);
      break;
    case EX_EXEC:
      if (variable->Value.Value.Function.Read)
	{
	  variable->Value.Value.Function.Read();
	}
      else
	{
	  return; /* should catch error somehow */
	}
      switch (TopType)
	{
	case PA_FLOAT:
	  printf("%.2f\n", TopValue.Float);
	  break;
	case PA_STRING:
	  printf("'%s'\n", TopValue.String);
	  break;
	default:
	  puts("Unknown type");
      }
      break;
    case EX_FPTR:
      printf("%.2f\n", (*(float *)(variable->Value.Value.Pointer)));
      break;
      /* Should not be possible */
    default:
      puts("FATAL ERROR-Unknown variable type");
      exit(1);
    }
}

void VA_SaveVariable(struct VA_Variable *variable)
{
  if (variable->Value.Type!=EX_EXEC)
    printf("Set %s ",variable->Node.Name);
  switch(variable->Value.Type)
    {
    case EX_FLOAT:
      printf("%.2f\n", variable->Value.Value.Float);
      break;
    case EX_STRING:
      printf("'%s'\n", variable->Value.Value.String);
      break;
      /*  case EX_EXEC: save for later */
    case EX_EXEC:
      if (variable->Value.Value.Function.Write==FN_NoWrite)
	{
	  return; /* Don't save if it will cause an error loading */
	}
      printf("Set %s ",variable->Node.Name);
      /* does not chk for no-read */
      if (variable->Value.Value.Function.Read)
	{
	  variable->Value.Value.Function.Read();
	}
      else
	{
	  return; /* should catch error somehow */
	}
      switch (TopType)
	{
	case PA_FLOAT:
	  printf("%.2f\n", TopValue.Float);
	  break;
	case PA_STRING:
	  printf("'%s'\n", TopValue.String);
	  break;
	default:
	  puts("Unknown type");
	}
      break;
    case EX_FPTR:
      printf("Set %s %.2f\n", variable->Node.Name,
	     (*(float *)(variable->Value.Value.Pointer)));
      break;
      /* Should not be possible */
    default:
      puts("FATAL ERROR-Unknown variable type"); exit(1);
    }
}

void *VA_FreeVariable(struct VA_Variable *variable)
{
  if (variable->Value.Type==EX_EXEC)
    {
      return(NULL);
    }
  if (variable->Value.Type==EX_FPTR)
    {
      return(NULL);
    }
  return(FreeNode((struct Node *)variable));
}

struct ME_Method VariableMethod={
  VA_CreateVariable, VA_ClearVariable, VA_FreeVariable,
  VA_PrintVariable, VA_PrintVariable, VA_SaveVariable
};

/* ---- END OF VARIABLE METHODS ---- */

/* SYNTAX: Set <var> <value> */
/* Assign a value to a variable */
int VA_SetVariable()
{
  struct VA_Variable *variable;
  char type;
  PA_GetString;
  variable=(struct VA_Variable *)FindNode(&(VariableList.Head), PA_Result.String);
  if (variable==NULL)
    {
      variable=(struct VA_Variable *)ME_CreateNode(&VariableMethod,&VariableList, PA_Result.String);
    }
  type=PA_GetArg();
  /* accessed with the set-command */
  if (variable->Value.Type==EX_EXEC)	/* Special for internal vars */
    {
      return (variable->Value.Value.Function.Write() );	/* Execute write */
    }
  /* There are more 'types' available but they can not be */
  switch (type)
    {
    case PA_FLOAT:
      switch (variable->Value.Type)
	{
	case EX_SPTR:
	  Error(PA_ERR_FAIL,"Variable can only be string");
	case EX_FPTR:
	  *(float *)(variable->Value.Value.Pointer)=PA_Result.Float;
	  break;
	default:
	  variable->Value.Value.Float=PA_Result.Float;
	  variable->Value.Type=PA_FLOAT;
	}
      break;
    case PA_STRING:
      switch (variable->Value.Type)
	{
	case EX_SPTR:
	  /* strcpy((char)*variable->Value.Value.Pointer, PA_Result.String);*/
	  break;
	case EX_FPTR:
	  Error(PA_ERR_FAIL,"Variable can only be number");
	default:
	  strcpy(variable->Value.Value.String, PA_Result.String);
	  variable->Value.Type=PA_STRING;
	}
      break;
    default:
      Error(PA_ERR_FAIL,"Invalid expression");
    }
  return(PA_ERR_OK);
}

/* SYNTAX: Pop <var> */
/* Reads a token from the previous 'file' */
int VA_Pop()
{
  struct PA_Status *current;
  struct VA_Variable *variable;
  char type;

  PA_GetString;
  variable=(struct VA_Variable *)FindNode(&(VariableList.Head), PA_Result.String);
  if (variable==NULL)
    {
      variable=(struct VA_Variable *)ME_CreateNode(&VariableMethod,&VariableList, PA_Result.String);
    }

  current=PA_Status;
  PA_Status=current->Caller;	/* Skip back */
  type=PA_GetArg();		/* Get value */
  PA_Status=current;		/* Restore */
  if (variable->Value.Type==EX_EXEC)	/* Special for internal vars */
    {
      return (variable->Value.Value.Function.Write() );	/* Execute write */
    }
  /* There are more 'types' available */
  switch (type)
    {
    case PA_FLOAT:
      if (variable->Value.Type==EX_FPTR)
	{
	  /* (float)*variable->Value.Value.Pointer=PA_Result.Float;*/
	}
      else
	{
	  variable->Value.Value.Float=PA_Result.Float;
	  variable->Value.Type=PA_FLOAT;
	}
      break;
    case PA_STRING:
      if (variable->Value.Type==EX_SPTR)
	{
	  /* strcpy((char)*variable->Value.Value.Pointer, PA_Result.String);*/
	}
      else
	{
	  strcpy(variable->Value.Value.String, PA_Result.String);
	  variable->Value.Type=PA_STRING;
	}
      break;
    default:
      strcpy(variable->Value.Value.String, "false");
      variable->Value.Type=PA_STRING;
      Error(PA_ERR_WARN,"No more variables");
    }
  return(PA_ERR_OK);
}


/* Execute a statement if an expression is true */
/* SYNTAX: If <expr> { statements } */
int VA_If()
{
  PA_GetString;
  if (OP_True())
    {
      PA_Execute(PA_Status->Commands);
      return(PA_ERR_OK);
    }
  return (PA_SkipBlock());
}
