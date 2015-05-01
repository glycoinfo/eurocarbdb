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

    Copyright 2010 Magnus Lundborg, Göran Widmalm
*/

/* This file contains commands for creating and using metabolic pathway rules */

#include <ctype.h>
#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include "rule.h"
#include "gfx.h"
#include "node.h"
#include "methods.h"
#include "parser.h"
#include "setup.h"


struct List RuleList={
  {NULL, &(RuleList.Tail), "Rules Head"},
  {&(RuleList.Head), NULL, "Rules Tail"}
};


void *RU_CreateRule(char *name)
{
  struct RU_Rule *rule;
  rule=(struct RU_Rule *)MakeNode(sizeof(struct RU_Rule),name);
  if(rule==NULL)
    return (NULL);
  InitList(&(rule->Patterns));
  InitList(&(rule->Residues));
  strcpy(rule->Patterns.Head.Name,"Patterns Head");
  strcpy(rule->Patterns.Tail.Name,"Patterns Tail");
  strcpy(rule->Residues.Head.Name,"Allowed Residues Head");
  strcpy(rule->Residues.Tail.Name,"Allowed Residues Tail");  
  return(rule);
}

void RU_ClearRule(struct RU_Rule *rule)
{
  ME_EmptyList(&RU_PatternMethod, &(rule->Patterns));
  ME_EmptyList(&RU_ResidueMethod, &(rule->Residues));
}

void *RU_FreeRule(struct RU_Rule *rule)
{
  ME_EmptyList(&RU_PatternMethod, &(rule->Patterns));
  ME_EmptyList(&RU_ResidueMethod, &(rule->Residues));
  return(FreeNode((struct Node*)rule));
}

void RU_ListRule(struct RU_Rule *rule)
{
  printf("%s\n",rule->Node.Name);
}

void RU_PrintRule(struct RU_Rule *rule)
{
  struct RU_Pattern *pattern;
  struct RU_Residue *res;

  printf("%s\n",rule->Node.Name);

  if(ListLen(&rule->Patterns)>0)
    {
      printf("Patterns:\n");
      for(pattern=(struct RU_Pattern *)rule->Patterns.Head.Succ; pattern->Node.Succ!=NULL;
	  pattern=(struct RU_Pattern *)pattern->Node.Succ)
	{
	  printf("\t%s\n", pattern->regexp);
	}
    }
  if(ListLen(&rule->Residues)>0)
    {
      printf("Allowed residues:\n");
      for(res=(struct RU_Residue *)rule->Residues.Head.Succ; res->Node.Succ!=NULL;
	  res=(struct RU_Residue *)res->Node.Succ)
	{
	  printf("\t%s\n",res->Node.Name);
	}
    }
}

void RU_SaveRule(struct RU_Rule *rule)
{
  struct RU_Pattern *pattern;
  struct RU_Residue *res;

  printf("rule '%s' {\n", rule->Node.Name);
  for(pattern=(struct RU_Pattern *)rule->Patterns.Head.Succ; pattern->Node.Succ!=NULL;
      pattern=(struct RU_Pattern *)pattern->Node.Succ)
    {
      printf("pattern '%s'\n",pattern->regexp);
    }
  for(res=(struct RU_Residue *)rule->Residues.Head.Succ; res->Node.Succ!=NULL;
      res=(struct RU_Residue *)res->Node.Succ)
    {
      printf("residue '%s'\n",res->Node.Name);
    }
  printf("}\n");
}

struct ME_Method RU_RuleMethod={
  RU_CreateRule, RU_ClearRule, RU_FreeRule,
  RU_ListRule, RU_PrintRule, RU_SaveRule
};

void *RU_CreatePattern(char *name)
{
  return(MakeNode(sizeof(struct RU_Pattern),name));
}

void RU_ClearPattern(struct RU_Pattern *pattern)
{
  strcpy(pattern->regexp,"");
}

void RU_PrintPattern(struct RU_Pattern *pattern)
{
  printf("\t%s\n",pattern->regexp);
}

struct ME_Method RU_PatternMethod={
  RU_CreatePattern, RU_ClearPattern, FreeNode,
  NULL, RU_PrintPattern, NULL
};

void *RU_CreateResidue(char *name)
{
  return(MakeNode(sizeof(struct RU_Residue),name));
}

void RU_ClearResidue(struct RU_Residue *res)
{
  res->Residue=0;
}

void RU_PrintResidue(struct RU_Residue *res)
{
  printf("\t%s\n",res->Node.Name);
}

struct ME_Method RU_ResidueMethod={
  RU_CreateResidue, RU_ClearResidue, FreeNode,
  NULL, RU_PrintResidue, NULL
};

struct PA_Command RU_Cmds[]={
  { "pattern", RU_AddPattern, "pattern '<pattern>'",
    "Adds a structure pattern that is valid for this metabolic pathway. All structures must match at least one pattern of all metabolic rules."},
  { "residue", RU_AddResidue, "residue '<residue>'",
    "Adds an allowed residue to the rule. If _any_ allowed residues are specified only those residues are allowed in a structure for it to match this rule. If no residues are specified any residues are OK."},
  { NULL, NULL, NULL, NULL}
};


struct RU_Rule *RU_Rule;

/* SYNTAX: rule '<rule name>' { ... }
   Adds a set of biological rules. Each rule contains a number of structure
   patterns, of which at least one must match a structure for the structure
   to be considered valid. */
int RU_AddRule()
{
  int stat;
  char buffer[32];

  PA_GetString;
  strncpy(buffer, PA_Result.String, 31);
  RU_Rule=(struct RU_Rule *)ME_CreateNode(&RU_RuleMethod,&RuleList, buffer);
  if(RU_Rule==NULL)
    {
      Error(PA_ERR_FATAL, "Out of memory");
    }
  stat=PA_Execute(RU_Cmds);

  return(stat);
}
/* SYNTAX: pattern '<pattern>'
   Adds a pattern to a set of rules. In the pattern '.' can be used as one-
   character wild cards and '^' and '$' are used as beginning of line and
   end of line anchors. */
int RU_AddPattern()
{
  struct RU_Pattern *pattern;
  char buffer[30];
  int len=ListLen(&RU_Rule->Patterns);
  sprintf(buffer,"Pattern %d",len);

  PA_GetString;
  pattern=(struct RU_Pattern *)ME_CreateNode(&RU_PatternMethod, &RU_Rule->Patterns, buffer);
  if(pattern==NULL)
    {
      Error(PA_ERR_FATAL, "Out of memory");
    }
  strncpy(pattern->regexp,PA_Result.String, 255);
  return (PA_ERR_OK);
}

/* SYNTAX: residue '<residue>'
   Adds an allowed residue to the rule. If _any_ allowed residues are specified
   only those residues are allowed in a structure for it to match this rule.
   If no residues are specified any residues are OK.*/
int RU_AddResidue()
{
  struct RE_Residue *residueData;
  struct RU_Residue *res;

  PA_GetString;
  residueData=(struct RE_Residue *)FindNode((struct Node*)&ResidueList,PA_Result.String);
  if(residueData==NULL)
    {
      Error(PA_ERR_FAIL, "Residue not found");
    }
  res=(struct RU_Residue *)ME_CreateNode(&RU_ResidueMethod, &RU_Rule->Residues, residueData->Node.Name);
  if(res==NULL)
    {
      Error(PA_ERR_FATAL, "Out of memory");      
    }
  res->Residue=residueData;
  return (PA_ERR_OK);
}

/* Check if the structure in *str matches at least one of the patterns in each
   selected biological rule in *sim. This can quickly discard many structures
   that does not fulfill the rules. */
int RU_MatchRules(struct SE_Simulation *sim, struct BU_Struct *str)
{
  struct RU_Rule *rule;
  struct RU_Pattern *pattern;
  char structure[256], matched, branchedStructure[256];
  int cnt;

  structure[0]=0;
  branchedStructure[0]=0;

  /* Retain the structure backbone. Since branches are not always used in
     the rule patterns two different structure strings are generated. One
     contains no branches while the other contains branches. */
  GFX_StructGfx(str,structure, 1);
  GFX_StructGfx(str,branchedStructure, 0);

  for(rule=(struct RU_Rule *)sim->Rules.Head.Succ; rule->Node.Succ!=NULL;
      rule=(struct RU_Rule *)rule->Node.Succ)
    {
      matched=0;
      cnt=0;

      /* If there is a list of allowed residues the residues in the structure
	 must be in that list. If there is no list of residues all residues are
	 allowed. */
      /* This is already done with generating the residue stack. Perhaps it
	 should be removed */
      /*      if(ListLen(&rule->Residues)>0)
	{
	  for(unit=(struct BU_Unit *)str->Units.Head.Succ;
	      unit->Node.Succ!=NULL;
	      unit=(struct BU_Unit *)unit->Node.Succ)
	    {
	      if(!FindNode((struct Node *)&rule->Residues,
			   unit->Residue->Node.Name))
		{
		  return 0;
		}
	    }
	    }*/
      for(pattern=(struct RU_Pattern *)rule->Patterns.Head.Succ;
	  pattern->Node.Succ!=NULL; 
	  pattern=(struct RU_Pattern *)pattern->Node.Succ)
	{
	  cnt++;
	  if(matched==0)
	    {
	      /* If there are branches in the pattern use the branched structure
		 string for matching. */
	      if(strchr(pattern->regexp,(int) '[')!=0 || strchr(pattern->regexp,(int) ']')!=0)
		{
		  matched=RU_Compare(pattern->regexp,branchedStructure);
		}
	      else
		{
		  matched=RU_Compare(pattern->regexp,structure);
		}
	    }
	}
      /* Only return 0 if there were patterns in the rule and they were not matched*/
      if(cnt>0 && matched==0)
	{
	  return(0);
	}
    }
  return(1);
}


/* This compares two strings returning 1 if they match.
   '.' can be used as one-character wild card and '^' and '$' as anchors
   for marking the beginning and end of the line.
   This string matching is quick (and does not leak memory which stdex.h does) */
char RU_Compare(const char *pattern, const char *str)
{
  int i,j=0,patternlen,stlen;
  char start=0, end=0, matched=0, firstmatch=0;


  /* First check if the strings are already identical. */
  if(strcasecmp(pattern,str)==0)
    {
      return (1);
    }
  /* Check if a start anchor is used. */
  if(pattern[0]=='^')
    {
      start=1;
    }
  patternlen=strlen(pattern);
  stlen=strlen(str);

  /* If the structure string is shorter than the required pattern stop here */
  if(stlen<(patternlen-(start+end)))
    {
      return (0);
    }

  /* If an end anchor is used make a reverse search. No gaps can be allowed
     except for wildcards ('.'). */
  if(pattern[patternlen-1]=='$')
    {
      /* If the structure string is shorter than the required pattern stop here */
      if(stlen<(patternlen-(start+end)))
	{
	  return (0);
	}
      for(i=1;i<patternlen-(1+start);i++)
	{
	  if(pattern[patternlen-(i+1)]!='.')
	    {
	      if(str[stlen-i]!=pattern[patternlen-(i+1)] && 
		 (((str[stlen-i]>=65 && str[stlen-i]<=90)||
		   (str[stlen-i]>=97 && str[stlen-i]<=122))
		  && abs(str[stlen-i]-pattern[patternlen-(i+1)])!=32))
		{
		  return(0);
		}
	    }
	}
      return(1);
    }

  for(i=start;i<patternlen;i++)
    {
      /* Wildcard */
      if(pattern[i]=='.')
	{
	      j++;
	  continue;
	}

      for(;j<stlen;j++)
	{
	  if(pattern[i]=='$' && str[j]!=0)
	    {
	      return (0);
	    }
	  if(str[j]==pattern[i] || 
	     ((str[j]>=65 && str[j]<=90 && str[j]-pattern[i]==-32) ||
	      (str[j]>=97 && str[j]<=122 && str[j]-pattern[i]==32)) ||
	     pattern[i]=='.')
	    {
	      j++;
	      if(matched==0)
		{
		  /* Mark the next character from where this match started.
		     If the whole pattern cannot be matched from here restart
		     search from this position. */
		  firstmatch=j;
		  matched=1;
		}
	      break;
	    }
	  else
	    {
	      /* If the start anchoring tag is used there can be no gaps until
		 the whole pattern has been matched. */
	      if(start==1)
		{
		  return(0);
		}
	      i=start;
	      if(matched==1)
		{
		  /* Restart from the position after where this match started. */
		  j=firstmatch;
		}
	      matched=0;
	    }
	  /* If there are not enough characters remaining in the structure
	     string to fit the whole pattern. */
	  if(matched==0 && stlen-(j)<=patternlen-(i+start+end))
	    {
	      return (0);
	    }
	}
    }
  return(matched);
}

/* This function reads a pattern string and sets *residue to the last residue
   in the pattern and sets all linkage positions in *linkages. If there are
   no linkages (residue in the end of the pattern) return 0, because it cannot
   be determined what residue to put there since the linkage positions are not
   included. To prevent mistakes it's better not to set these residues. */
int RU_NextResidueFromRedEnd(char *pattern, char *residue, char *linkages)
{
  int patternlen, linkCnt, resCnt, branched=0;
  char *resPos;

  patternlen=strlen(pattern);
  linkCnt=0;

  if(patternlen<2)
    {
      return (0);
    }
  if(strlen(residue)==0)
    {
      if(pattern[patternlen-1]!='$' || !isalpha(pattern[patternlen-2]))
	{
	  return (0);
	}
      else
	{
	  pattern[patternlen-1]=0;
	  patternlen--;
	}
    }
  /* Find where the last residue begins */
  resPos=strrchr(pattern, ')');
  if(resPos!=0)
    {
      if(isdigit(*(resPos-1)))
	{
	  linkages[linkCnt++]=*(resPos-1);
	}

      resPos++;
      if(*resPos==']')
	{
	  branched=1;
	  resPos++;
	}
      else if(*resPos=='[')
	{
	  resPos++;
	}
    }
  else
    {
      resPos=pattern;
    }

  resCnt=0;
  for(;resPos<=pattern+patternlen;resPos++)
    {
      residue[resCnt++]=*resPos;
    }

  resPos=strrchr(pattern, '(');
  if(resPos)
    {
      *resPos=0;
    }
  else
    {
      pattern[0]=0;
    }
  if(branched==1)
    {
      while(branched==1 && linkCnt<6)
	{
	  branched=0;
	  while(*(--resPos)!=')' && resPos>=pattern)
	    {
	    }
	  if(isdigit(*(resPos-1)))
	    {
	      linkages[linkCnt++]=*(resPos-1);
	    }
	  if(*resPos==']')
	    {
	      branched=1;
	    }
	}
      while(*(--resPos)!='(' && resPos>=pattern)
	{
	}
      if(*resPos=='(')
	{
	  *resPos=0;
	}
    }
  linkages[linkCnt]=0;
  if(strlen(linkages)==0)
    {
      return(0);
    }
  return (1);
}
