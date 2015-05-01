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

/* Expression evaluator */
/* This file contains expressions, operators, prefixes and suffixes */

#include "exrdtok.h"
#include "expr.h"
#include "parser.h"
#include "variables.h"
#include "build.h"
#include "setup.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <strings.h>

#define EX_STRINGLEN	256

#define VA_FLOAT	'f'
#define VA_STRING	's'


struct Operator {
  char Name[10];
  char *(*Action)();	/* Returns error-msg or NULL */
  int LeftArg;		/* Left operand type or OP_NONE */
  int RightArg;		/* Right operand type or OP_NONE */
  int Priority;
};

#define OP_NONE	0

struct Value VStack[100];
int VPointer;

int OStack[100];
int OPointer;
int OBottom;

#define DropOp		{OPointer--;}
#define PushOp(op)	{OStack[++OPointer]=op;}
#define TopOp		(OStack[OPointer])
/* Low pri operator as stack-bottom marker */
#define OP_MARKER		(-1)

char *OP_Add()
{
  if (RightType!=LeftType) return("add type err");
  if (RightType==VA_FLOAT)
    {
      LeftValue.Float=LeftValue.Float+RightValue.Float;
    }
  else
    {
      strncat(LeftValue.String,RightValue.String, EX_STRINGLEN-1);
    };
  DropValue;
  return(NULL);
}

char *OP_Subtract()
{
  if (RightType!=VA_FLOAT) return("sub r");
  if (LeftType!=VA_FLOAT) return("sub l");
  LeftValue.Float=LeftValue.Float-RightValue.Float;
  DropValue;
  return(NULL);
}

char *OP_Multiply()
{
  if (RightType!=VA_FLOAT) return("mult r");
  if (LeftType!=VA_FLOAT) return("mult l");
  LeftValue.Float=LeftValue.Float*RightValue.Float;
  DropValue;
  return(NULL);
}

char *OP_Divide()
{
  if (RightType!=VA_FLOAT) return("div r");
  if (LeftType!=VA_FLOAT) return("div l");
  if (RightValue.Float==0) return("Division by zero!");
  LeftValue.Float=LeftValue.Float/RightValue.Float;
  DropValue;
  return(NULL);
}

char *OP_Equal()
{
  char *bool;
  bool="false";
  if (RightType!=LeftType) return("equ type");
  if (RightType==VA_FLOAT)
    {
      if (RightValue.Float==LeftValue.Float) bool="true";
    }
  else
    {
      if (strcasecmp(RightValue.String, LeftValue.String)==0)
	bool="true";
    };
  DropValue; DropValue; PushString(bool);
  return(NULL);
}

char *OP_Less()
{
  char *bool;
  bool="false";
  if (RightType!=LeftType) return("equ type");
  if (RightType==VA_FLOAT)
    {
      if (RightValue.Float>LeftValue.Float) bool="true";
    }
  else
    {
      if (strcasecmp(RightValue.String, LeftValue.String)>0)
	bool="true";
    };
  DropValue; DropValue; PushString(bool);
  return(NULL);
}

char *OP_Greater()
{
  char *bool;
  bool="false";
  if (RightType!=LeftType) return("equ type");
  if (RightType==VA_FLOAT)
    {
      if (RightValue.Float<LeftValue.Float) bool="true";
    }
  else
    {
      if (strcasecmp(RightValue.String, LeftValue.String)<0)
	bool="true";
    };
  DropValue; DropValue; PushString(bool);
  return(NULL);
}

int OP_True()
{
  if (TopType==VA_FLOAT)
    {
      if (TopValue.Float==0) return(0);
      return(1);
    };
  if (strcasecmp(TopValue.String, "true")==0) return(1);
  if (strcasecmp(TopValue.String, "yes")==0) return(1);
  if (strcasecmp(TopValue.String, "on")==0) return(1);
  return(0);
}

char *OP_And()
{
  int rhs, lhs;
  rhs=OP_True(); DropValue;
  lhs=OP_True(); DropValue;
  if (lhs & rhs) PushString("true")
		   else PushString("false");
  return(NULL);
}

char *OP_Or()
{
  int rhs, lhs;
  rhs=OP_True(); DropValue;
  lhs=OP_True(); DropValue;
  if (lhs | rhs) PushString("true")
		   else PushString("false");
  return(NULL);
}

char *OP_Not()
{
  int bool;
  bool=OP_True(); DropValue;
  if (bool) PushString("false")
	      else PushString("true");
  return(NULL);
}


char *OP_Positive()
{
  if (TopType!=VA_FLOAT) return("un + nan");
  return(NULL);
}

char *OP_Negative()
{
  /* if (TopType!=VA_FLOAT) return("un - nan"); */
  if (TopType!=VA_FLOAT) { PushString("-"); return(NULL); };
  TopValue.Float=-TopValue.Float;
  return(NULL);
}

char *OP_Sign()
{
  if (TopType!=VA_FLOAT) return("sign - nan");
  if (TopValue.Float<0) TopValue.Float=-1;
  if (TopValue.Float>0) TopValue.Float=1;
  /* No need to set the 'sign' of zero (=0) */
  return(NULL);
}

char *OP_Absolute()
{
  if (TopType!=VA_FLOAT) return("abs - nan");
  if (TopValue.Float<0) TopValue.Float=-TopValue.Float;
  return(NULL);
}

/* check if a file exists */
char *OP_FExists()
{
  FILE *file;
  if (TopType!=VA_STRING) return("fexist - nas");
  file=fopen(TopValue.String,"r");
  DropValue;
  if (file==NULL)
    {
      PushString("false");
      return(NULL);
    };
  fclose(file);
  PushString("true");
  return(NULL);
}

/* check if a structure exists */
char *OP_SExists()
{
  struct BU_Struct *structure;
  structure=(struct BU_Struct *)FindNode(&(StructList.Head), TopValue.String);
  DropValue;
  if (structure==NULL) /* No structure with that name */
    {
      PushString("false");
      return(NULL);
    };
  PushString("true");
  return(NULL);
}

/* Simple postfix test */
char *OP_Kilo()
{
  if (TopType!=VA_FLOAT) return("kilo - nan");
  TopValue.Float*=1000;
  return(NULL);
}

char *OP_Import()
{
  char *ptr;
  if (TopType!=VA_STRING) return ("import - nas");
  ptr=getenv(TopValue.String);
  if (ptr==NULL)
    {
      TopValue.String[0]=0;	/* not found */
      return(NULL);
    };
  strncpy(TopValue.String, ptr, EX_STRINGLEN);
  return(NULL);
}

char *OP_Question()
{
  if (TopType!=VA_STRING) return ("question - nas");
  printf("%s ", TopValue.String);
  scanf("%s", TopValue.String);
  return(NULL);
}

char *OP_Variable(char *string)
{
  struct VA_Variable *variable;
  variable=(struct VA_Variable *)FindNode(&(VariableList.Head), TopValue.String);
  /*DropValue;*/
  if (variable==NULL) /* No variable with that name */
    return("Variable not found");
  DropValue;
  switch (variable->Value.Type)
    {
    case EX_FLOAT:	PushFloat(variable->Value.Value.Float);
      break;
    case EX_STRING:	PushString(variable->Value.Value.String);
      break;
    case EX_EXEC:		if (variable->Value.Value.Function.Read()!=PA_ERR_OK)
      {
	VStack[++VPointer].Type=EX_NONE;
	return("Can't read variable");
      };
      break;
    case EX_FPTR:		PushFloat(*(float *)(variable->Value.Value.Pointer));
      break;
    };
  return(NULL);
}

/* special functions */
char *OP_ToString()
{
  if (TopType==VA_FLOAT)
    {
      snprintf((TopValue.String), EX_STRINGLEN,"%.2f",TopValue.Float);
      TopType=VA_STRING;
    };
  return(NULL);
}

char *OP_ToNumber()
{
  if (TopType==VA_STRING)
    {
      TopValue.Float=atof(TopValue.String);
      TopType=VA_FLOAT;
    };
  return(NULL);
}

char *OP_Fit()
{
  struct BU_Struct *structure;
  structure=(struct BU_Struct *)FindNode(&(StructList.Head), TopValue.String);
  DropValue;
  if (structure==NULL) /* No structure with that name */
    return("Structure not found");
  switch(SE_Simulation.Criteria)
    {
    case SE_PURGE_C:	PushFloat(structure->CFit);
      break;
    case SE_PURGE_H:	PushFloat(structure->HFit);
      break;
    case SE_PURGE_CH:	PushFloat(structure->ChFit);
      break;
    case SE_PURGE_HH:	PushFloat(structure->HhFit);
      break;
      
    default:		PushFloat(structure->CFit);
    };
  return(NULL);
}

struct Operator Prefix[]={
  { "$",		OP_Variable,	VA_STRING,	VA_STRING,	4 },
  { "+",		OP_Positive,	VA_FLOAT,	VA_FLOAT,	1 },
  { "-",		OP_Negative,	VA_FLOAT,	VA_FLOAT,	1 },
  { "sgn",	OP_Sign,	VA_FLOAT,	VA_FLOAT,	1 },
  { "abs",	OP_Absolute,	VA_FLOAT,	VA_FLOAT,	1 },
  { "@",		OP_Import,	VA_STRING,	VA_STRING,	4 },
  { "fexists",	OP_FExists,	VA_STRING,	VA_STRING,	1 },
  { "sexists",	OP_SExists,	VA_STRING,	VA_STRING,	1 },
  { "~",		OP_Not,		VA_STRING,	VA_STRING,	1 },
  { "string",	OP_ToString,	VA_STRING,	VA_STRING,	1 },
  { "number",	OP_ToNumber,	VA_STRING,	VA_STRING,	1 },
  { "fit",	OP_Fit,	VA_STRING,	VA_STRING,	1 },
  { "",		NULL,	0,	0,	0}
};

struct Operator Suffix[]={
  { "k",		OP_Kilo,	VA_FLOAT,	VA_FLOAT,	2 },
  { "?",		OP_Question,	VA_STRING,	VA_STRING,	4 },
  { "",		NULL,	0,	0,	0}
};

struct Operator OpTable[]={
  { "+",		OP_Add,		VA_FLOAT,	VA_FLOAT,	4 },
  { "-",		OP_Subtract,	VA_FLOAT,	VA_FLOAT,	4 },
  { "/",		OP_Divide,	VA_FLOAT,	VA_FLOAT,	8 },
  { "*",		OP_Multiply,	VA_FLOAT,	VA_FLOAT,	8 },
  { "=",		OP_Equal,	VA_STRING,	VA_STRING,	2 },
  { "&",		OP_And,		VA_STRING,	VA_STRING,	1 },
  { "|",		OP_Or,		VA_STRING,	VA_STRING,	1 },
  { "<",		OP_Less,	VA_STRING,	VA_STRING,	1 },
  { ">",		OP_Greater,	VA_STRING,	VA_STRING,	1 },
  { ")",		NULL,	0,	0,	-1},
  { "",		NULL,	0,	0,	0}
};



void EvalTerm(char **string)
{
  int i, op;
  float temp;
  char buffer[PA_LINE_LEN], *last;
  char *stat;
  int OBottSv;
  PushOp(OP_MARKER);		/* Mark bottom of stack frame */
  do
    {
      last=*string;
      ReadToken(string,buffer);
      for (i=0, op=-1; Prefix[i].Action!=NULL; i++)
	if (strcmp(buffer, Prefix[i].Name)==0)	/* Get prefix op */
	  { op=i; PushOp(i); break; }
    } while (op>-1);
  /* No more prefixes */
  *string=last;		/* Unget last 'token' */

  /* see if parenthesis */
  if (**string=='(')
    {
      OBottSv=OBottom;
      OBottom=OPointer;
      *string+=1; Evaluate(string);
      if (**string==')') *string+=1;
      OBottom=OBottSv;
    }			/* else error! */
  else { /* a value */
    /* try reading a float ... */
    temp=ReadFloat(string,10);
    if (*string!=last) { PushFloat(temp); }
    /* ... if not try with a string */
    else
      {
	ReadString(string,buffer,'\'');	/* String */
	PushString(buffer);
      };
  }; /* value */
  /* ... done */
  do
    {
      last=*string;
      ReadToken(string,buffer);
      for (i=0, op=-1; Suffix[i].Action!=NULL; i++)
	if (strcmp(buffer,Suffix[i].Name)==0)		/* Get postfix op */
	  {
	    op=i;
	    if (TopOp!=OP_MARKER)
	      while (Suffix[op].Priority<Prefix[TopOp].Priority)
		{
		  stat=Prefix[TopOp].Action();
		  DropOp;
		};
	    stat=Suffix[op].Action();
	    break;
	  };
    } while (op>-1);
  /* No more suffixes */
  *string=last;		/* Unget last 'token' */
  /* Any prefixes left? */
  /* Is this right? We have assumed that all prefixes and suffixes */
  /* have higher priorities than binary operators! */
  while (TopOp!=OP_MARKER)
    {
      stat=Prefix[TopOp].Action();
      DropOp;
    };
  DropOp;	/* Remove marker */
}

void Evaluate(char **string)
{
  int i, op;
  /*int OPointerSv; * Saved OP-pointer */
  char buffer[100], *last;
  /*OPointerSv=OPointer;*/
  last=*string;
  EvalTerm(string);
  if (last==*string) return; /* Didn't find value */
  last=*string;
  ReadToken(string,buffer);
  for (i=0, op=-1; OpTable[i].Action!=NULL; i++)	/* Get binary operators */
    if (strcmp(buffer,OpTable[i].Name)==0) { op=i; break; };
  if  (op<0)	/* No more ops - just finish all on stack */
    {
      while (OPointer>=/*0*/ /*OPointerSv*/ OBottom+1)
	{
	  OpTable[TopOp].Action();
	  DropOp;
	};
      *string=last;		/* Unget last token */
      return;
    };
  while (OPointer>=/*0*/ /*OPointerSv*/ OBottom+1)
    {
      if (OpTable[op].Priority>OpTable[TopOp].Priority) break;
      OpTable[TopOp].Action();
      DropOp;
    };
  PushOp(op);
  Evaluate(string);
}
