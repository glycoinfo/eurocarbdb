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

/* Filename = parser.h */
/* Prefix = PA */

#ifndef _PARSER_H
#define _PARSER_H

#include <stdio.h>
#include "expr.h"

#define PA_LINE_LEN	1024	/* Maximum command line length */
#define PA_CMD_LEN	256	/* Maximum length of command name */

struct PA_Status
{
  struct PA_Status *Caller;	/* Calling 'PA_Status' */
  struct PA_Command *Commands;	/* Array with commands */
  int Nesting;		/* Nesting level */
  char *ErrMsg;		/* Pointer to errormsg */
  int ErrCode;		/* Error code */
  FILE *Input;		/* Current input file */
  char *InStr;		/* Pointer to internal string */
  /*  FILE *Output;*/		/* Current output */
  FILE *Error;		/* Stream for error messages */
  char Filename[PA_CMD_LEN];	/* Input filename or "" if */
				/* executing internal cmds */
  char CmdLine[PA_LINE_LEN];	/* Buffer for command line */
  char *CmdPtr;		/* Pointer into CmdLine */
  char *PrevPtr;		/* Pointer to end of last read token */
  int LineNr;		/* Line number */
  int StmtNr;		/* Statement number */
  int Flags;		/* Various flags */
  char Prompt[PA_CMD_LEN];	/* Command line prompt */
};

#define PA_TRACE	0x01	/* Trace flag */
#define PA_PROMPT	0x02	/* Prompt flag */
#define PA_DEF_PROMPT	"> "	/* Default prompt */

#define PA_SetTrace	{PA_Status->Flags|=PA_TRACE;}
#define PA_ClrTrace	{PA_Status->Flags&=(~PA_TRACE);}
#define PA_TestTrace	(PA_Status->Flags&PA_TRACE)

#define PA_SetPrompt	{PA_Status->Flags|=PA_PROMPT;}
#define PA_ClrPrompt	{PA_Status->Flags&=(~PA_PROMPT);}
#define PA_TestPrompt	(PA_Status->Flags&PA_PROMPT)

extern struct PA_Status *PA_Status;
extern int PA_Safe;		/* Global safety status */
#define PA_SECURE	(PA_Safe>0)
#define PA_SECURE2	(PA_Safe>1)
#define PA_SECURE3	(PA_Safe>2)

/* Error codes for commands */
#define PA_ERR_OK	0	/* No error */
#define PA_ERR_WARN	10	/* Warning */
#define PA_ERR_FAIL	20	/* Command failed */
#define PA_ERR_EOF	200	/* Encountered eof */
#define PA_ERR_END	PA_ERR_EOF
#define PA_ERR_FATAL	255	/* Fatal error - terminate */

#define Error(state,msg)	{ PA_Status->ErrMsg=msg;\
				  PA_Status->ErrCode=state;\
				  return(state); }

/* Return codes for parser functions */
#define PA_FAILURE	0
#define PA_SUCCESS	1
#define PA_END		2	/* finished interpretation */

/* Argument types returned from PA_GetArg */
#define PA_FLOAT	'f'
#define PA_STRING	's'
#define PA_NOVAL	0

#define PA_Result	TopValue

struct PA_Command
{
  char *Name;		/* Name of command */
  int (*Action)();	/* command code */
  char *Syntax;         /* Command syntax */
  char *Desc;           /* Command help */
};

void PA_SkipSpc();
char PA_GetArg();
char PA_GetSeparator(char sep);
char PA_GetToken(char **buffer);
int PA_FindCmd(struct PA_Command cmds[]);
int PA_GetChar();
char PA_GetLine();
char PA_Scan(struct PA_Command cmds[]);
int PA_Execute(struct PA_Command cmds[]);
char PA_Parse(FILE *inp, FILE *outp, char *fname,
	      struct PA_Command cmds[]);
void PA_NextCmd();
void PA_NextLine();
int PA_SkipBlock();

#define PA_GetFloat	{ if (PA_GetArg()!=PA_FLOAT)\
			  Error(PA_ERR_FAIL,"Expecting float"); }

#define PA_GetString	{ if (PA_GetArg()!=PA_STRING)\
			  Error(PA_ERR_FAIL,"Expecting string"); }

#define PA_GetReqToken(buff)	\
			{  if (PA_GetToken(buff)!=PA_SUCCESS)\
			   Error(PA_ERR_FAIL,"Missing argument"); }

#endif
