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

/* Filename = parser.c */
/* Prefix = PA */
/* This file contains the main commandline parser */
/* which read the input stream (or an internal string */
/* and executes all commands. */
/* It also defines the interface to some low-level I/O */
/* and expression evaluation. */


#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "ccpn.h"
#include "expr.h"
#include "exrdtok.h"
#include "parser.h"

#include "commands.h"

/* Global pointer to current parser state */
struct PA_Status *PA_Status=0;

/* Global safety status */
int PA_Safe=1;

/* PA_SkipSpc() - Skips white space =spc, tab */
void PA_SkipSpc()
{
  while((*PA_Status->CmdPtr==' ')||(*PA_Status->CmdPtr=='\t'))
    {
      (PA_Status->CmdPtr)++;
    }
}


/* PA_GetArg() - Call the expression evaluator */
/* Returns the type of the result */
char PA_GetArg()
{
  char *last;
  last=PA_Status->CmdPtr;
  if (!PA_Status->CmdPtr)
    {
      return(PA_NOVAL);
    }
  PA_SkipSpc();
  last=PA_Status->CmdPtr;
  ResetEval;
  Evaluate(&(PA_Status->CmdPtr));
  /* This hopefully catches any failures */
  if (last==PA_Status->CmdPtr)
    {
      return(PA_NOVAL);
    }
  return(TopType);
}


/* PA_GetSeparator(sep) - gets the next separator */
/* or returns PA_FAILURE */
char PA_GetSeparator(char sep)
{
  PA_SkipSpc();
  if (*PA_Status->CmdPtr!=sep)
    {
      return(PA_FAILURE);
    }
  PA_Status->CmdPtr++;
  return(PA_SUCCESS);
}


/* PA_GetToken(buffer) - gets a token */
/* or returns PA_FAILURE */
char PA_GetToken(char **buffer)
{
  PA_SkipSpc();
  ReadToken(&(PA_Status->CmdPtr), *buffer);
  if (**buffer!=0x00) 
    {
      return (PA_SUCCESS);
    }
  return(PA_FAILURE);
}


/* PA_FindCmd(cmds) - Returns offset of a command */
/* contained in string in an array cmds. */
/* Returns -1 on failure */
int PA_FindCmd(struct PA_Command cmds[])
{
  char buffer[PA_CMD_LEN], *last;
  int i=0;
  PA_SkipSpc();
  last=PA_Status->CmdPtr;
  ReadToken(&(PA_Status->CmdPtr), buffer);
  while (cmds[i].Name!=NULL)
    {
      if (strcmp(buffer, cmds[i].Name)==0) return(i);
      i++;
    }
  /* PA_Status->CmdPtr=last;	* Unget token */
  return(-1);
}

/* PA_GetChar() - reads next character from file */
/* or from the string inbuff if file==null */
int PA_GetChar()
{
  char temp;
  if (PA_Status->Filename[0]!=0)
    {
      return( fgetc(PA_Status->Input) );
    }
  temp=*PA_Status->InStr;
  PA_Status->InStr++;
  if (temp==0x00)
    {
      return(EOF);	/* end of string */
    }
  return(temp);
}


/* PA_GetLine() - Reads an entire 'line' */
/* from file or string into buffer */
char PA_GetLine()
{
  int cnt ,inchar;
  char *ptr;	/* pointer */
  char quote;	/* Change behaviour inside quotes */
  quote=0;	/* Set toggle to false */
  cnt=0;
  PA_Status->CmdPtr=PA_Status->CmdLine;

  /* Handle quit regardless of previous nesting */
  if(PA_Status->Nesting<0)
    {
      return(PA_SUCCESS);
    }

  /* display prompt */
  if (PA_TestPrompt && (PA_Status->Input==stdin))
    {
      if(!projectValid)
	{
	  printf("nv ");
	}
      printf("%d%s", PA_Status->Nesting, PA_Status->Prompt);
    }
  
  inchar=PA_GetChar();
  if (inchar==EOF)
    {
      return(1);	/* found eof or string end */
    }

  while (cnt<PA_LINE_LEN)
    {
      if (!quote)
	switch (inchar)
	  {
	  case '\\':	/* don't go to next line */
	    inchar=PA_GetChar();
	    if (inchar=='\r')
	      {
		inchar=PA_GetChar();
	      }
	    if (inchar=='\n')
	      {
		inchar=PA_GetChar();
		PA_Status->LineNr++;
		PA_Status->StmtNr=1;
	      }
	    PA_Status->CmdLine[cnt++]=inchar;
	    break;
	  case PA_QUOTE:
	    PA_Status->CmdLine[cnt++]=inchar;
	    quote=1;
	    break;
	  case '#':	/* skip rest of line */
	    PA_Status->CmdLine[cnt]=0;
	    while ( (inchar!='\n') && (inchar!=EOF) )
	      {
		inchar=PA_GetChar();
	      }
	    PA_Status->LineNr++;
	    PA_Status->StmtNr=1;
	    return (0);
	  case '!':	/* execute rest of line */
	    PA_Status->CmdLine[cnt++]=0;
	    ptr=&(PA_Status->CmdLine[cnt]);
	    inchar=PA_GetChar();
	    while ((inchar!=EOF)&&(inchar!='\n')&&(cnt<PA_LINE_LEN))
	      {
		PA_Status->CmdLine[cnt++]=inchar;
		inchar=PA_GetChar();
	      }
	    PA_Status->CmdLine[cnt]=0;
	    if (!PA_SECURE)
	      {
		if(system(ptr)==-1)
		  {
		    return(1);
		  }
	      }
	    PA_Status->LineNr++;
	    PA_Status->StmtNr=1;
	    return (0);
	  case '\n':	/* newline - eol */
	    PA_Status->CmdLine[cnt]=0;
	    PA_Status->LineNr++;
	    PA_Status->StmtNr=1;
	    return (0);
	  case EOF:	/* end-of-file */
	    PA_Status->CmdLine[cnt]=0;
	    return (0);
	  case '\r':	/* cr - just skip (in case of cr/nl) */
	    break;
	  default:
	    PA_Status->CmdLine[cnt++]=inchar;
	  }
      else		/* ignore some special chars inside quotes */
	switch (inchar)
	  {
	  case PA_QUOTE:
	    PA_Status->CmdLine[cnt++]=inchar;
#ifdef DBL_QUOTE_ESCAPE
	    if (PA_Status->CmdLine[cnt-2]!=PA_QUOTE)
	      {
		quote=0;
	      }
	    break;
#endif
	    quote=0;
	    break;
	  case PA_ESCAPE:
	    PA_Status->CmdLine[cnt++]=inchar;
	    inchar=PA_GetChar();
	    if (inchar=='\r')
	      {
		inchar=PA_GetChar();
	      }
	    if (inchar=='\n')
	      {
		inchar=PA_GetChar();
	      }
	    if (inchar==EOF)
	      {
		PA_Status->CmdLine[cnt]=0;
		return(0);
	      }
	    PA_Status->CmdLine[cnt++]=inchar;
	    break;
	  case '\n':	/* newline - eol */
	  case EOF:	/* end-of-file */
	    PA_Status->CmdLine[cnt]=0;
	    return (0);
	  case '\r':	/* cr - just skip (in case of cr/nl) */
	    break;
	  default:
	    PA_Status->CmdLine[cnt++]=inchar;
	  }
      inchar=PA_GetChar();
    }
  PA_Status->CmdLine[cnt]=0;	/* terminate string ??? */
  /* this should be a failure!? */
  return (1);
}

/* PA_Scan(cmds) */
/* Scans the current file/string and executes the commands */
char PA_Scan(struct PA_Command cmds[])
{
  int state;		/* program status */
  char *last;	/* previous position */
  char *i;
  int cmdnr=-1;		/* command - set to invalid at beginning */

  /* Handle quit regardless of previous nesting */
  if(PA_Status->Nesting<0)
    {
      return(PA_SUCCESS);
    }

  PA_Status->Commands=cmds;
  PA_Status->Nesting++;
  do
    {
      while ( (*PA_Status->CmdPtr)!=0x00 )
	{
	  PA_SkipSpc();
	  /* Catch } */
	  if (*PA_Status->CmdPtr=='}')
	    {
	      PA_Status->StmtNr++;
	      PA_Status->Nesting--;
	      return(PA_SUCCESS);
	    }
	  last=PA_Status->CmdPtr;
	  /* Catch ; */
	  if (*PA_Status->CmdPtr==';')
	    {
	      PA_Status->CmdPtr++;
	      PA_Status->StmtNr++;
	    }
	  else
	    {
	      cmdnr=PA_FindCmd(cmds);	/* get command */
	    }

	  if (cmdnr<0)			/* do error stuff */
	    {
	      PA_Status->CmdPtr=last;
	      cmdnr=PA_FindCmd(GlobalCmds);  /* Try looking for global cmd */
	      if (cmdnr<0)
		{
		  if ( PA_Status->Filename[0]==0 )
		    {
		      fprintf(PA_Status->Error,"Internal");
		    }
		  else
		    {
		      fprintf(PA_Status->Error,"%s", PA_Status->Filename);
		    }
		  fprintf(PA_Status->Error,", %d:%d\t%s - (Sub)command not found\n",
			  PA_Status->LineNr, PA_Status->StmtNr, PA_Status->CmdPtr);
		  fprintf(PA_Status->Error,"%s\n", PA_Status->CmdLine);
		  for (i=PA_Status->CmdLine; i<PA_Status->CmdPtr; i++)
		    {
		      fprintf(PA_Status->Error,"~");
		    }
		  fprintf(PA_Status->Error,"^\n");
		  break;
		  /*    return(PA_FAILURE);*/
		}
	      if ( PA_TestTrace )
		{
		  if ( PA_Status->Filename[0]==0 )
		    {
		      fprintf(PA_Status->Error,"Internal");
		    }
		  else
		    {
		      fprintf(PA_Status->Error,"%s", PA_Status->Filename);
		    }
		  fprintf(PA_Status->Error,", %d:%d\t%s\n", PA_Status->LineNr,
			  PA_Status->StmtNr, PA_Status->CmdLine);
		} 
	      state=GlobalCmds[cmdnr].Action(PA_Status->CmdPtr);
	      if ( state==PA_ERR_END )
		{
		  PA_Status->Nesting--;
		  return(PA_SUCCESS);
		}
	      if ( state !=PA_ERR_OK )
		{
		  if ( PA_Status->Filename[0]==0 )
		    {
		      fprintf(PA_Status->Error,"Internal");
		    }
		  else
		    {
		      fprintf(PA_Status->Error,"%s", PA_Status->Filename);
		    }
		  fprintf(PA_Status->Error,", %d:%d\t%s - %s\n",
			  PA_Status->LineNr, PA_Status->StmtNr,
			  GlobalCmds[cmdnr].Name, PA_Status->ErrMsg);
		  fprintf(PA_Status->Error,"%s\n", PA_Status->CmdLine);
		  for (i=PA_Status->CmdLine; i<PA_Status->CmdPtr; i++)
		    {
		      fprintf(PA_Status->Error,"~");
		    }
		  fprintf(PA_Status->Error,"^\n");
		  PA_NextCmd();
		}
	    }
	  else	/* execute command */
	    {
	      if ( PA_TestTrace )
		{
		  if ( PA_Status->Filename[0]==0 )
		    {
		      fprintf(PA_Status->Error,"Internal");
		    }
		  else fprintf(PA_Status->Error,"%s", PA_Status->Filename);
		  fprintf(PA_Status->Error,", %d:%d\t%s\n", PA_Status->LineNr,
			  PA_Status->StmtNr, PA_Status->CmdLine);
		}
	      state=cmds[cmdnr].Action(PA_Status->CmdPtr);
	      if ( state ==PA_ERR_END ) 
		{
		  PA_Status->Nesting--;
		  return(PA_SUCCESS);
		}
	      if ( state !=PA_ERR_OK )
		{
		  if ( PA_Status->Filename[0]==0 )
		    {
		      fprintf(PA_Status->Error,"Internal");
		    }
		  else
		    {
		      fprintf(PA_Status->Error,"%s", PA_Status->Filename);
		    }
		  fprintf(PA_Status->Error,", %d:%d\t%s - %s\n",
			  PA_Status->LineNr, PA_Status->StmtNr,
			  cmds[cmdnr].Name, PA_Status->ErrMsg);
		  fprintf(PA_Status->Error,"%s\n", PA_Status->CmdLine);
		  for (i=PA_Status->CmdLine; i<PA_Status->CmdPtr; i++)
		    {
		      fprintf(PA_Status->Error,"~");
		    }
		  fprintf(PA_Status->Error,"^\n");
		  PA_NextCmd();
		}
	    }

	  /* Handle quit regardless of previous nesting */
	  if(PA_Status->Caller==0 || PA_Status->Nesting<0)
	    {
	      return(PA_SUCCESS);
	    }

	  PA_SkipSpc();
	  switch (*PA_Status->CmdPtr)
	    {
	    case 0:
	      break;
	      /* Next statement */
	    case ':':
	      PA_Status->CmdPtr++;
	      break;
	      /* Repeat command */
	    case ';':
	      break;   
	    case '}':
	      PA_Status->CmdPtr++;
	      PA_Status->StmtNr++;
	      break;
	    default:
	      if ( PA_Status->Filename[0]==0 )
		{
		  fprintf(PA_Status->Error,"Internal");
		}
	      else
		{
		  fprintf(PA_Status->Error,"%s", PA_Status->Filename);
		}
	      fprintf(PA_Status->Error,", %d:%d\t%s - Expecting :, ; or }\n",
		      PA_Status->LineNr, PA_Status->StmtNr, PA_Status->CmdPtr 
		      /*cmds[cmdnr].Name*/);
	      fprintf(PA_Status->Error,"%s\n", PA_Status->CmdLine);
	      for (i=PA_Status->CmdLine; i<PA_Status->CmdPtr; i++)
		{
		  fprintf(PA_Status->Error,"~");
		}
	      fprintf(PA_Status->Error,"^\n");
	      /* return(PA_FAILURE); */
	    }
	}
    } 
  while ( !PA_GetLine() );
  PA_Status->Nesting--;
  return(PA_SUCCESS);
}


int PA_Execute(struct PA_Command cmds[])
{
  int stat;
  struct PA_Command *save;
  PA_SkipSpc();
  while (*PA_Status->CmdPtr==0)
    {
      PA_GetLine();
      PA_SkipSpc();
    }
  if (*PA_Status->CmdPtr!='{')
    {
      Error(PA_ERR_FAIL, "Expecting {");
    }
  (PA_Status->CmdPtr)++;
  save=PA_Status->Commands;
  stat=PA_Scan(cmds);
  PA_Status->Commands=save;
  if (stat==PA_FAILURE)
    {
      return(PA_ERR_WARN);
    }
  /* if (*PA_Status->CmdPtr!='}')
     Error(PA_ERR_FAIL, "Expecting }");
     PA_Status->CmdPtr++; */
  return(PA_ERR_OK);	/* hmmm... */
}

/* PA_Parse(inp, outp, fname, cmds) */
/* Parses a file or string and executes the commands */
/* FILE *inp, *outp - input and output devices */
/* char *fname      - filename of input */
/* struct PA_Command cmds[] - applicable commands */
char PA_Parse(FILE *inp, FILE *outp, char *fname,
	      struct PA_Command cmds[])
{
  struct PA_Status *Current, *Previous;	/* Parser status */

  Current=malloc(sizeof(struct PA_Status));

  Previous=PA_Status;	/* save pointer to previous status */
  if (Previous)		/* Retain all flags */
    {
      Current->Flags=Previous->Flags;
      Current->Error=Previous->Error;
      strcpy(Current->Prompt, Previous->Prompt); /* Carry old prompt */
      Current->Nesting=0;
    }
  else
    {
      Current->Flags=0x00;	/* default */
      Current->Error=stdout;
      strcpy(Current->Prompt,"");
      Current->Nesting=0;
      Current->Caller=0;
    }

  /* Reset counters */
  Current->LineNr=0;
  Current->StmtNr=1;

  if (fname)
    {
      strcpy(Current->Filename, fname);
    }
  else
    {
      if (Previous)
	{
	  strcpy(Current->Filename, Previous->Filename);
	}
      else Current->Filename[0]=0x00;
    }
  if (inp!=NULL)
    {
      Current->InStr=(char *)inp;	/* for use as internal string */
      Current->Input=inp;		/* for use as file */
    }
  else
    {
      Current->InStr=(char *)Previous->InStr;
      Current->Input=Previous->Input;
    }
  /*  if (outp)
    Current.Output=outp;
  else 
  Current.Output=Previous->Output;*/
  Current->Caller=Previous;
  PA_Status=Current;	/* Set new status */
  PA_GetLine();		/* Get first line */
  PA_Scan(cmds);


  PA_Status=Previous;	/* restore status */
  free(Current);
  return(PA_SUCCESS);
}

/* New stuff */
/* skip to next command */
/* i.e. find ':',';','{','}' or newline */
void PA_NextCmd()
{
  do
    {
      switch (*PA_Status->CmdPtr)
	{
	case '{':
	  PA_SkipBlock();
	  PA_Status->CmdPtr++;
	  return;
	  /* found next statement */
	case ':':
	case ';':
	  PA_Status->CmdPtr++; /* fix???*/
	  /*   case '{':*/
	case '}':
	  return;
	case '\n':
	  /* (PA_Status->CmdPtr)++; */
	  return;
	  /* found comment - just return as next 'command' */
	case '#':
	  /* end-of-line */
	case 0:	return;
	  /* skip through string (ignoring escaped characters) */
	case PA_QUOTE:
	  do
	    {
	      if ((*PA_Status->CmdPtr)==PA_ESCAPE)
		{
		  PA_Status->CmdPtr++;
		  if ((*PA_Status->CmdPtr)==0)
		    {
		      return;
		    }
		}
	      (PA_Status->CmdPtr)++;
	      if ((*PA_Status->CmdPtr)==0)
		{
		  return;
		}
	    }
	  while ((*PA_Status->CmdPtr)!=PA_QUOTE);
	  (PA_Status->CmdPtr)++;
	  break;
	default:
	  (PA_Status->CmdPtr)++;
	};
      /*(PA_Status->CmdPtr)++;*/
    }
  while(*PA_Status->CmdPtr!=0);
}

/* skip to end of line */
/* actually just force parser to read a new line */
void PA_NextLine()
{
  PA_Status->CmdPtr="\n";
}


/* skip 'block' */
int PA_SkipBlock()
{
  PA_SkipSpc();
  /* find opening '{' */
  while (*PA_Status->CmdPtr==0)
    {
      if (PA_GetLine())
	{
	  Error(PA_ERR_FAIL,"Expecting {");
	}
      PA_SkipSpc();
    }
  /* no opening '{'? OK just skip next statement */
  if (*PA_Status->CmdPtr!='{')
    {
      Error(PA_ERR_FAIL,"Expecting {");
    }
  PA_Status->CmdPtr++;	/* skip { */
  /* now find closing '}' */
  do
    {
    do
      {
	PA_SkipSpc();
	if (*PA_Status->CmdPtr=='}')
	  {
	    return(PA_ERR_OK);
	  }
	PA_NextCmd();
      } 
    while (*PA_Status->CmdPtr!=0);
    } 
  while ( !PA_GetLine() );
  Error(PA_ERR_FAIL,"Expecting }");
}
