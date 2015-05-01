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

/* Filename = gfx.c */
/* Prefix = GFX */
/* fn:s for shorthand display of structures */

#include "build.h"
#include "gfx.h"
#include "node.h"
#include "parser.h"
#include "variables.h"
#include <string.h>
#include <strings.h>
#include <stdio.h>

int GFX_CntSubst(struct BU_Unit *unit)
{
  int i, j, cnt, substLinked, anomerPos;
  cnt=0;
  anomerPos=BU_ANOMER_POS(unit);
  for (i=0; i<unit->Shifts.Type->HeavyCnt; i++)
    {
      if (unit->Subst[i]!=NULL)
	{
	  if(strcasecmp(unit->Subst[i]->Residue->Shifts.Type->CTsuperclass, "subst")!=0)
	    {
	      cnt++;
	    }
	  else
	    {
	      substLinked=0;
	      for(j=0;j<unit->Subst[i]->Residue->Shifts.Type->HeavyCnt;j++)
		{
		  /*		  if(j==BU_ANOMER_POS(unit->Subst[i]))
		    {
		      continue;
		      }*/
		  if(unit->Subst[i]->Subst[j]!=0 && unit->Subst[i]->Subst[j]!=unit)
		    {
		      substLinked=1;
		    }
		}
	      if(substLinked==1)
		{
		  cnt++;
		}
	    }
	}
    }
  /* don't count anomer */
  if (anomerPos!=-1 && unit->Subst[BU_ANOMER_POS(unit)]!=NULL)
    {
      cnt--;
    }
  return (cnt);
}

int GFX_CntUnits(struct BU_Struct *structure)
{
  struct BU_Unit *unit;
  int cnt;
  cnt=0;
  for (unit=(struct BU_Unit *)structure->Units.Head.Succ;
       unit->Node.Succ!=NULL;
       unit=(struct BU_Unit *)unit->Node.Succ)
    {
      if(strcasecmp(unit->Residue->Shifts.Type->CTsuperclass, "subst")!=0)
	{
	  cnt++;
	}
    }
  return (cnt);
}


/* returns reducing end of structure */
/* NULL if not found */
struct BU_Unit *GFX_FindReducing(struct BU_Struct *structure)
{
  struct BU_Unit *unit;
  int i, thisCnt=0, nextCnt=0;

  unit=(struct BU_Unit *)First(structure->Units);
  while (unit->Node.Succ!=NULL && unit->Position!=TY_VOID_ATOM)
    {
      /* See if there is e.g. a aDGlc(1->1)aDGlc linkage and if there is find which residue in the
	 linkage should be the reducing end (fewest substituents). */
      if(unit->Position == BU_ANOMER_POS(unit->Subst[BU_ANOMER_POS(unit)]))
	{
	  for(i=0; i<unit->Residue->Shifts.Type->HeavyCnt; i++)
	    {
	      if(i!=BU_ANOMER_POS(unit) && unit->Subst[i]!=0)
		{
		  thisCnt++;
		}
	    }
	  for(i=0; i<unit->Subst[BU_ANOMER_POS(unit)]->Residue->Shifts.Type->HeavyCnt; i++)
	    {
	      if(i!=unit->Position && unit->Subst[BU_ANOMER_POS(unit)]->Subst[i]!=0)
		{
		  nextCnt++;
		}
	    }
	  if(thisCnt<=nextCnt)
	    {
	      return(unit);
	    }
	  else
	    {
	      return(unit->Subst[BU_ANOMER_POS(unit)]);
	    }
	}
      unit=(struct BU_Unit *)unit->Node.Succ;
    }
  if (unit->Node.Succ==NULL)
    {
      return (NULL);	/* circular? */
    }
  return(unit);
}


int GFX_IsBackbone(struct BU_Unit *unit)
{
  struct BU_Unit *mark;
  int i, anomerPos;
  mark=unit;
  for (i=0; i<BU_MAX_UNITS; i++)
    {
      anomerPos=BU_ANOMER_POS(unit);
      if(anomerPos!=-1)
	{
	  unit=(struct BU_Unit *)unit->Subst[anomerPos];
	}
      else
	{
	  return(TRUE);
	}
      if (mark==unit)
	{
	  return (TRUE);
	}
    }
  return(FALSE);
}


/* Ranking should be done in the following manner */
/* 1) a) reducing end of oligosaccharides has highest priority */
/*    b) the P in teichoic acids has highest priority */
/*    c) the backbone residue with highest priority (below) of a polymer or cyclic structure */
/* 2) 'largest' residue has precedence (i.e. hexose before pentose) */
/* 3) first in alphabetical order of configurational prefixes */
/* 4) D before L (R before S) */
/* 5) a before b */
/* 6) alphabetical order of modifications which are part of the residue, e.g. 2-acetamido-2-deoxy- */
/* 7) most substituted residue */

int GFX_RankUnits(struct BU_Unit *l_unit, struct BU_Unit *r_unit)
{
  int temp, anomerPos_l, anomerPos_r;
  struct BU_Unit *marker;

  marker=l_unit;
  do {
    /* Biological rules: ->3)HexNAc or ->4)HexNAc in reducing end of repeating unit.
       GlcNAc has precedence over other HexNAcs */
    if(strcasecmp(l_unit->Shifts.Type->Node.Name, "HexNAc")==0 && 
       (l_unit->Subst[2]!=NULL ||l_unit->Subst[3]!=NULL))
      {
	if(strcasecmp(r_unit->Shifts.Type->Node.Name, "HexNAc")!=0 || 
	   (r_unit->Subst[2]==NULL && r_unit->Subst[3]==NULL))
	  {
	    return (-1);
	  }
	if(strcasecmp(l_unit->Residue->Node.Name+2, "GlcNAc")==0)
	  {
	    if(strcasecmp(r_unit->Residue->Node.Name+2, "GlcNAc")!=0)
	      {
		return (-1);
	      }
	  }
	else if(strcasecmp(r_unit->Residue->Node.Name+2, "GlcNAc")==0)
	  {
	    return (1);
	  }
      }
    else if(strcasecmp(r_unit->Shifts.Type->Node.Name, "HexNAc")==0 && 
       (r_unit->Subst[2]!=NULL ||r_unit->Subst[3]!=NULL))
      {
	return (1);
      }

    /* compare type pri */
    temp=strcasecmp(r_unit->Shifts.Type->Priority,
		    l_unit->Shifts.Type->Priority);
    if(temp!=0)
      {
	return (temp);
      }
    /* compare residue names */
    temp=strcasecmp(l_unit->Residue->Node.Name, r_unit->Residue->Node.Name);
    if (temp!=0)
      {
	return (temp);
      }
    /* same residue - select lower 'to' position */
    temp=l_unit->Position-r_unit->Position;
    if (temp!=0)
      {
	return (temp);
      }
    anomerPos_l=BU_ANOMER_POS(l_unit);
    anomerPos_r=BU_ANOMER_POS(r_unit);
    if(anomerPos_l!=-1)
      {
	l_unit=l_unit->Subst[anomerPos_l];
      }
    else
      {
	l_unit=NULL;
      }
    if(anomerPos_r!=-1)
      {
	r_unit=r_unit->Subst[anomerPos_r];
      }
    else
      {
	r_unit=NULL;
      }
    if (l_unit==NULL)
      {
	return (1);
      }
    if (r_unit==NULL) 
      {
	return (-1);
      }
  } while (l_unit!=marker);
  /* OK - so difference must be in side chains */

  return(0);
}

struct BU_Unit *GFX_FindBackbone(struct BU_Struct *structure)
{
  struct BU_Unit *unit;
  struct BU_Unit *l_unit, *t_unit, *h_unit;
  int i, anomerPos;

  unit=(struct BU_Unit *)First(structure->Units);
  if(unit->Node.Succ==NULL)
    {
      return (NULL);
    }
  /* After passing BU_MAX_UNITS units we must have hit the backbone */
  for (i=0; i<BU_MAX_UNITS; i++)
    {
      anomerPos=BU_ANOMER_POS(unit);
      if(anomerPos==-1)
	{
	  return(NULL);
	}
      unit=(struct BU_Unit *)unit->Subst[anomerPos];
    }
  /* Find highest ranked residue on backbone */
  l_unit=unit;
  h_unit=l_unit;
  t_unit=l_unit;
  for (i=0; i<BU_MAX_UNITS; i++)
    {
      anomerPos=BU_ANOMER_POS(t_unit);
      if(anomerPos==-1)
	{
	  return (NULL);
	}
      t_unit=(struct BU_Unit *)t_unit->Subst[anomerPos];
      if (t_unit==l_unit)
	{
	  break;
	}
      if (GFX_RankUnits(h_unit, t_unit)>0)
	{
	  h_unit=t_unit;
	}
    }
  return (h_unit);
}


/* finds bonds */
int GFX_NextBond(struct BU_Unit *unit,int pos)
{
  int i, substLinked, anomerPos;
  pos++;

  anomerPos=BU_ANOMER_POS(unit);

  if(pos==anomerPos)
    {
      pos++;
    }
  /* Don't count substituents */
  while ( (pos<unit->Residue->Shifts.Type->HeavyCnt) && ((unit->Subst[pos]==NULL ||
							  pos==anomerPos) || 
							 strcasecmp(unit->Subst[pos]->
								    Residue->Shifts.
								    Type->CTsuperclass,
								    "subst")==0) )
    {
      if(pos==anomerPos)
	{
	  pos++;
	  continue;
	}
      if(unit->Subst[pos]!=NULL)
	{
	  substLinked=0;
	  for(i=0;i<unit->Subst[pos]->Residue->Shifts.Type->HeavyCnt;i++)
	    {
	      if(i==BU_ANOMER_POS(unit->Subst[pos]))
		{
		  continue;
		}
	      if(unit->Subst[pos]->Subst[i]!=0 && unit->Subst[pos]->Subst[i]!=unit)
		{
		  substLinked=1;
		}
	    }
	  if(substLinked==1)
	    {
	      break;
	    }
	}
      pos++;
    }

  if (unit->Subst[pos]==NULL) return (GFX_NB_FAIL);
  return(pos);
}

/* must not be called for a back-bone unit! */
int GFX_BranchLen(struct BU_Unit *unit)
{
  int length,longest;
  int i;
  longest=0;
  for(i=0; i<unit->Residue->Shifts.Type->HeavyCnt; i++)
    {
      if (i!=BU_ANOMER_POS(unit))
	{
	  if (unit->Subst[i]!=NULL)
	    {
	      length=GFX_BranchLen(unit->Subst[i]);
	      if (length>longest) longest=length;
	    }
	}
    }
  return(longest+1);
}


struct BU_Unit *GFX_FindLongTerm(struct BU_Unit *unit)
{
  int i, length, longbranch, longlength;
  if (GFX_CntSubst(unit)==0) return (unit);	/* No more substituents on this branch */
  longlength=0;
  for (i=1; i<unit->Residue->Shifts.Type->HeavyCnt; i++)
    {
      if (unit->Subst[i]!=NULL)
	{
	  length=GFX_BranchLen(unit->Subst[i]);
	  if (length>longlength)
	    { longlength=length; longbranch=i; }
	};
    };
  return (GFX_FindLongTerm(unit->Subst[longbranch]));
}

void GFX_DoAUnit(struct BU_Unit *unit, char *str, int noSidechain, int reducing)
{
  struct VA_Variable *var;
  int i, anomerPos, pos, carbBank;

  var=(struct VA_Variable *)FindNode(&(VariableList.Head), "Print3D");
  if(var!=NULL && var->Value.Value.Float>0)
    {
      carbBank=1;
    }
  else
    {
      carbBank=0;
    }

  pos=GFX_NextBond(unit,GFX_NB_FIRST);
  if(pos==GFX_NB_FAIL)
    {
      /* Handle e.g. aDGlc(1->1)aDGlc linkages */
      anomerPos=BU_ANOMER_POS(unit);
      if(anomerPos!=-1)
	{
	  if(unit->Subst[anomerPos] && BU_ANOMER_POS(unit->Subst[anomerPos])==unit->Position && reducing)
	    {
	      pos=unit->Position;
	    }
	}
    }
  if (pos!=GFX_NB_FAIL)
    {
      if(str==NULL||noSidechain!=-1)
	{
	  if(strcmp(unit->Subst[pos]->Residue->Shifts.Type->CTsuperclass, "subst")==0)
	    {
	      for(i=0;i<unit->Subst[pos]->Residue->Shifts.Type->HeavyCnt;i++)
		{
		  if(i==BU_ANOMER_POS(unit->Subst[pos]))
		    {
		      continue;
		    }
		  if(unit->Subst[pos]->Subst[i]!=0 && unit->Subst[pos]->Subst[i]!=unit)
		    {
		      break;
		    }	      
		}
	      GFX_DoAUnit(unit->Subst[pos]->Subst[i], str, noSidechain, 0);
	      if(carbBank)
		{
		  if(str==NULL)
		    {
		      printf("-");
		    }
		  else
		    {
		      strcat(str, "-");
		    }
		}	      
	    }
	  else
	    {
	      GFX_DoAUnit(unit->Subst[pos], str, noSidechain, 0);
	      if(carbBank)
		{
		  if(str==NULL)
		    {
		      printf("-");
		    }
		  else
		    {
		      strcat(str, "-");
		    }
		}
	    }
	}
      for (pos=GFX_NextBond(unit,pos); pos!=GFX_NB_FAIL;
	   pos=GFX_NextBond(unit,pos) )
	{
	  if(strcasecmp(unit->Subst[pos]->Residue->Shifts.Type->CTsuperclass, "subst")!=0)
	    {
	      if(str==NULL||noSidechain==0)
		{
		  noSidechain=-1;
		  if(carbBank)
		    {
		      if(str==NULL)
			{
			  printf("-");
			}
		      else
			{
			  strcat(str, "-");
			}
		    }
		  if(str==NULL)
		    {
		      printf("[");
		    }
		  else
		    {
		      strcat(str, "[");
		    }
		  GFX_DoAUnit(unit->Subst[pos], str, noSidechain, 0);
		  if(str==NULL)
		    {
		      printf("]");
		    }
		  else
		    {
		      strcat(str, "]");
		    }
		}
	    }
	}
    }
  GFX_PrintUnit(unit, str, 0, 0);
}


void GFX_DoBackUnit(struct BU_Unit *unit, char *str, int noSidechain)
{
  struct BU_Unit *last;
  struct VA_Variable *var;
  int subst_pos=-1, pos, carbBank;
  int anomer1=-1, anomer2=-1;

  var=(struct VA_Variable *)FindNode(&(VariableList.Head), "Print3D");
  if(var!=NULL && var->Value.Value.Float>0 )
    {
      carbBank=1;
    }
  else
    {
      carbBank=0;
    }

  last=unit;
  pos=GFX_NextBond(unit,GFX_NB_FIRST);
  while (pos!=GFX_NB_FAIL)
    {
      if (GFX_IsBackbone(unit->Subst[pos])!=FALSE)
	{
	  subst_pos=pos;
	}
      pos=GFX_NextBond(unit,pos);
    }

  anomer1=BU_ANOMER_POS(unit);
  if(anomer1!=-1)
    {
      anomer2=BU_ANOMER_POS(unit->Subst[BU_ANOMER_POS(unit)]);
    }

  /* Check if there is e.g. a aDGlc(1->1)aDGlc linkage. */
  if(subst_pos==-1)
    {
      if(unit->Subst[BU_ANOMER_POS(unit)])
	{
	  subst_pos=unit->Subst[BU_ANOMER_POS(unit)]->Position;
	}
    }

  if(carbBank==0)
    {
      if(str==NULL)
	{
	  printf("->%s)", unit->Shifts.Type->Atom[subst_pos].Label);
	}
      else
	{
	  strcat(str, "->");
	  if(anomer1!=-1 && anomer2!=-2 && strcmp(unit->Subst[anomer1]->Residue->Shifts.Type->CTsuperclass, "subst")==0)
	    {
	      strcat(str,unit->Subst[anomer1]->Subst[anomer2]->
		     Shifts.Type->Atom[unit->Subst[anomer2]->Position].Label);
	    }
	  else
	    {
	      strcat(str,unit->Shifts.Type->Atom[subst_pos].Label);
	    }
	  strcat(str, ")");
	}
    }
  while (unit->Subst[BU_ANOMER_POS(unit)]!=last)
    {
      for (pos=GFX_NextBond(unit,GFX_NB_FIRST); pos!=GFX_NB_FAIL;
	   pos=GFX_NextBond(unit,pos))
	if (pos!=subst_pos)
	  {
	    if(strcasecmp(unit->Subst[pos]->Residue->Shifts.Type->CTsuperclass, "subst")!=0)
	      {
		if(str==NULL||noSidechain==0)
		  {
		    noSidechain=-1;
		    if(carbBank)
		      {
			if(str==NULL)
			  {
			    printf("-");
			  }
			else
			  {
			    strcat(str, "-");
			  }
		      }
		    if(str==NULL)
		      {
			printf("[");
		      }
		    else
		      {
			strcat(str, "[");
		      }
		    GFX_DoAUnit(unit->Subst[pos], str, noSidechain, 0);
		    if(str==NULL)
		      {
			printf("]");
		      }
		    else
		      {
			strcat(str, "]");
		      }
		  }
	      }
	  }
      subst_pos=unit->Position;   /* position of last position */
      anomer1=BU_ANOMER_POS(unit); /*->Residue->Type->Anomeric;*/
      if(unit!=last && carbBank)
	{
	  if(str==NULL)
	    {
	      printf("-");
	    }
	  else
	    {
	      strcat(str, "-");
	    }
	}
      GFX_PrintUnit(unit, str, 0, 0);
     
      if(anomer1!=-1)
	{
	  unit=(struct BU_Unit *)unit->Subst[anomer1];
	}
    } /* while(unit!=last);*/
  /* last */
  for (pos=GFX_NextBond(unit,GFX_NB_FIRST); pos!=GFX_NB_FAIL;
       pos=GFX_NextBond(unit,pos))
    if (pos!=subst_pos)
      {
	if(str==NULL||noSidechain==0)
	  {
	    noSidechain=-1;
	    if(str==NULL)
	      {
		printf("[");
	      }
	    else
	      {
		strcat(str, "[");
	      }
	    GFX_DoAUnit(unit->Subst[pos], str, noSidechain, 0);
	    if(str==NULL)
	      {
		printf("]");
	      }
	    else
	      {
		strcat(str, "]");
	      }
	  }
      }
  if(unit!=last && carbBank)
    {
      if(str==NULL)
	{
	  printf("-");
	}
      else
	{
	  strcat(str, "-");
	}
    }
  GFX_PrintUnit(unit, str, 1, 0);
}

void GFX_StructGfx(struct BU_Struct *structure, char *str, int noSidechain)
{
  struct BU_Unit *unit;
  unit=(struct BU_Unit *)GFX_FindReducing(structure);
  if (unit==NULL)
    {
      unit=(struct BU_Unit *)GFX_FindBackbone(structure);
      if (unit!=NULL)
	{
	  GFX_DoBackUnit(unit->Subst[BU_ANOMER_POS(unit)], str, noSidechain);
	}
    }
  else GFX_DoAUnit(unit, str, noSidechain, 1);
  if(str==NULL)
    {
      puts("");
    }
}

char GFX_UnitName[NODE_NAME_LENGTH];		/* Current unit name */

void GFX_RenameAUnit(struct BU_Unit *unit, int reducing)
{
  int i, anomerPos, pos;

  pos=GFX_NextBond(unit,GFX_NB_FIRST);
  if(pos==GFX_NB_FAIL)
    {
      /* Handle e.g. aDGlc(1->1)aDGlc linkages */
      anomerPos=BU_ANOMER_POS(unit);
      if(anomerPos!=-1)
	{
	  if(unit->Subst[anomerPos] && BU_ANOMER_POS(unit->Subst[anomerPos])==unit->Position && reducing)
	    {
	      pos=unit->Position;
	    }
	}
    }
  if (pos!=GFX_NB_FAIL)
    {
      if(strcmp(unit->Subst[pos]->Residue->Shifts.Type->CTsuperclass, "subst")==0)
	{
	  for(i=0;i<unit->Subst[pos]->Residue->Shifts.Type->HeavyCnt;i++)
	    {
	      if(i==BU_ANOMER_POS(unit->Subst[pos]))
		{
		  continue;
		}
	      if(unit->Subst[pos]->Subst[i]!=0 && unit->Subst[pos]->Subst[i]!=unit)
		{
		  break;
		}	      
	    }
	  GFX_RenameAUnit(unit->Subst[pos]->Subst[i], 0);
	}
      else
	{
	  GFX_RenameAUnit(unit->Subst[pos], 0);
	}
      for (pos=GFX_NextBond(unit,pos); pos!=GFX_NB_FAIL;
	   pos=GFX_NextBond(unit,pos) )
	GFX_RenameAUnit(unit->Subst[pos], 0);
    }
  if(strcasecmp(unit->Residue->Shifts.Type->CTsuperclass, "subst")!=0)
    {
      strcpy(unit->Node.Name,GFX_UnitName);
      GFX_UnitName[0]--;
    }
}


void GFX_RenameBackUnit(struct BU_Unit *unit)
{
  struct BU_Unit *last;
  int subst_pos, pos, anomerPos;

  unit=(struct BU_Unit *)unit->Subst[BU_ANOMER_POS(unit)];
  last=unit;

  pos=GFX_NextBond(unit,GFX_NB_FIRST);
  while (pos!=GFX_NB_FAIL)
    {
      if (GFX_IsBackbone(unit->Subst[pos])!=FALSE)
	{
	  subst_pos=pos;
	}
      pos=GFX_NextBond(unit,pos);
    }
  if(strcasecmp(unit->Residue->Shifts.Type->CTsuperclass, "subst")!=0)
    {
      strcpy(unit->Node.Name,GFX_UnitName);
      GFX_UnitName[0]--;
    }
  do
    {
      for (pos=GFX_NextBond(unit,GFX_NB_FIRST); pos!=GFX_NB_FAIL;
	   pos=GFX_NextBond(unit,pos))
	{
	  if (pos!=subst_pos)
	    {
	      GFX_RenameAUnit(unit->Subst[pos], 0);
	    }
	}
      subst_pos=unit->Position; /* position of last position */
      if(strcasecmp(unit->Residue->Shifts.Type->CTsuperclass, "subst")!=0)
	{
	  strcpy(unit->Node.Name,GFX_UnitName);
	  GFX_UnitName[0]--;
	}
      anomerPos=BU_ANOMER_POS(unit);
      if(anomerPos!=-1)
	{
	  unit=(struct BU_Unit *)unit->Subst[anomerPos];
	}
    } while (unit!=last);
}


void GFX_RenameUnits(struct BU_Struct *structure)
{
  int i, anomerPos;
  struct BU_Unit *unit=0, *nextUnit=0;
  char name[2];

  strcpy(name, "a");

  GFX_UnitName[0]='a'+GFX_CntUnits(structure);
  GFX_UnitName[1]=0;

  i=0;
  while(!unit && i<25)
    {
      unit=FindNode(&(structure->Units.Head), name);
      name[0]+=1;
      i++;
    }
  if(unit)
    {
      anomerPos=BU_ANOMER_POS(unit);
      if(anomerPos >= 0 && unit->Subst[anomerPos]==NULL && 
	 GFX_NextBond(unit,GFX_NB_FIRST)==GFX_NB_FAIL && GFX_CntUnits(structure)>1)
	{
	  while(!nextUnit && i<25)
	    {
	      nextUnit = (struct BU_Unit *)FindNode(&(structure->Units.Head), name);
	      name[0]+=1;
	      i++;
	    }
	  if(nextUnit)
	    {
	      anomerPos=BU_ANOMER_POS(nextUnit);
	      if(anomerPos!=-1 && nextUnit->Subst[anomerPos]==NULL)
		{
		  for(i=0; i<unit->Shifts.Type->HeavyCnt; i++)
		    {
		      if(unit->Shifts.Type->Atom[i].Type & TY_FREE_POS)
			{
			  nextUnit->Position=i;
			  unit->Subst[i]=nextUnit;
			  nextUnit->Subst[anomerPos]=unit;
			}
		    }
		}
	    }
	}
    }

  unit=GFX_FindReducing(structure);
  if (unit==NULL)
    {
      unit=GFX_FindBackbone(structure);
      if (unit!=NULL) GFX_RenameBackUnit(unit);
    }
  else
    {
      GFX_UnitName[0]--;	/* one less unit in oligosaccharides */
      GFX_RenameAUnit(unit, 1);
    };
  i=1;
  for (unit=(struct BU_Unit *)structure->Units.Head.Succ;
       unit->Node.Succ!=NULL; unit=(struct BU_Unit *)unit->Node.Succ)
    {
      if(strcasecmp(unit->Residue->Shifts.Type->CTsuperclass, "subst")==0)
	{
	  sprintf(unit->Node.Name, "sub%d",i++);
	}
    }
}

/* 2005-08-17 */
/* Rename acc. to CarbBank/Utrecht rules for oligo */
void GFX_RenumberAUnit(struct BU_Unit *unit, char *string)
{
  int pos;
  char label[NODE_NAME_LENGTH];

  if (string[0]==0)
    strcpy(unit->Node.Name, "re");
  else
    strcpy(unit->Node.Name, string);
  
  for (pos=GFX_NextBond(unit,GFX_NB_FIRST); pos!=GFX_NB_FAIL;
       pos=GFX_NextBond(unit,pos) )
    {
      if (string[0]==0)
	snprintf(label, NODE_NAME_LENGTH-1, "%s", unit->Shifts.Type->Atom[pos].Label);
      else
	snprintf(label, NODE_NAME_LENGTH-1, "%s,%s", unit->Shifts.Type->Atom[pos].Label, string);
      GFX_RenumberAUnit(unit->Subst[pos], label);
    }
}


/* Renumber all attached units */
int GFX_Renumber()
{
  struct BU_Struct *structure;
  struct BU_Unit *unit;
 
  PA_GetString;
  structure=(struct BU_Struct *)FindNode(&(StructList.Head), PA_Result.String);
  if (structure==NULL) Error(PA_ERR_FAIL, "Structure not found");
  unit=GFX_FindReducing(structure);
  if (unit==NULL) Error(PA_ERR_FAIL, "Not an oligosaccharide");
  GFX_RenumberAUnit(unit, "");
  return(PA_ERR_OK);
}

int GFX_PrintUnit(struct BU_Unit *unit, char *str, int last, int inShiftList)
{
  int anomerPos1, anomerPos2, i, j, n, usedCnt, found;
  char substituent[NODE_NAME_LENGTH], *OMeFlag;
  int used[10], carbBank, OMeLen;
  struct VA_Variable *var;
  char *name;

  var=(struct VA_Variable *)FindNode(&(VariableList.Head), "Print3D");
  if(var!=NULL && var->Value.Value.Float>0 )
    {
      name=unit->Residue->CarbBank;
      carbBank=1;
    }
  else
    {
      name=unit->Residue->Node.Name;
      carbBank=0;
    }

  used[0]=0;
  usedCnt=0;

  anomerPos1=BU_ANOMER_POS(unit);

  if(!carbBank)
    {
      OMeFlag=strstr(name, "OMe");
      OMeLen=3;
    }
  else
    {
      OMeFlag=strstr(name, "-(1-1)-Methyl");
      OMeLen=13;
    }

  if(str==NULL)
    {
      if(!OMeFlag)
	{
	  printf("%s", name);
	}
      else
	{
	  for(i=0;i<strlen(name)-OMeLen;i++)
	    {
	      putchar(name[i]);
	    }
	}
    }
  else
    {
      if(!OMeFlag)
	{
	  strcat(str, name);
	}
      else
	{
	  strncat(str, name, strlen(name)-OMeLen); 
	}
    }
  /* Print substituents together with the unit */
  for (i=0; i<unit->Residue->Shifts.Type->HeavyCnt; i++)
    {
      if(i==anomerPos1)
	{
	  continue;
	}
      n=0;
      if(unit->Subst[i]!=NULL)
	{
	  if(strcasecmp(unit->Subst[i]->Residue->Shifts.Type->CTsuperclass, "subst")==0)
	    {
	      /* Check if this substituent has already been added as one substituent of a polysubstituent,
		 e.g. 6Ac in 4,6diAc */
	      found=0;
	      for(j=0; j<usedCnt; j++)
		{
		  if(used[j]==i)
		    {
		      found=1;
		    }
		}
	      /* Already added */
	      if(found==1)
		{
		  continue;
		}

	      /* Check if this is a bridging substituent, e.g.
		 a phosphodiester. In this case it is added to the residue
		 linking to it instead. */
	      anomerPos2=BU_ANOMER_POS(unit->Subst[i]);
	      for(j=0; j<unit->Subst[i]->Residue->Shifts.Type->HeavyCnt; j++)
		{
		  if(j!=anomerPos2 && unit->Subst[i]->Subst[j]!=0)
		    {
		      found=1;
		    }
		}
	      if(found==1)
		{
		  continue;
		}


	      n++;
	      if(str==NULL)
		{
		  printf("%d", i+1);
		}
	      else
		{
		  sprintf(str, "%s%d", str, i+1); 	      
		}
	      strcpy(substituent,unit->Subst[i]->Residue->Node.Name);
	      /* Look for more substituents of this kind to keep them together in the naming */
	      for(j=i+1; j<unit->Residue->Shifts.Type->HeavyCnt; j++)
		{
		  if(unit->Subst[j]!=NULL)
		    {
		      if(strcasecmp(unit->Subst[j]->Residue->Shifts.Type->CTsuperclass, "subst")==0)
			{
			  if(strcasecmp(unit->Subst[j]->Residue->Node.Name, substituent)==0)
			    {
			      n++;
			      used[usedCnt++]=j;
			      if(str==NULL)
				{
				  printf(",%d",j+1);
				}
			      else
				{
				  sprintf(str, "%s,%d", str, j+1); 
				}
			    }
			}
		    }
		}
	      switch(n)
		{
		case 2:
		  if(str==NULL)
		    {
		      printf("di");
		    }
		  else
		    {
		      strcat(str, "di");
		    }
		  break;
		case 3:
		  if(str==NULL)
		    {
		      printf("tri");
		    }
		  else
		    {
		      strcat(str, "tri");
		    }
		  break;
		case 4:
		  if(str==NULL)
		    {
		      printf("tetra");
		    }
		  else
		    {
		      strcat(str, "tetra");
		    }
		  break;
		}
	      if(str==NULL)
		{
		  printf("%s",substituent);
		}
	      else
		{
		  strcat(str,substituent);
		}
	    }
	}
    }

  if(anomerPos1!=-1 && unit->Subst[anomerPos1]!=0 && strcasecmp(unit->Subst[anomerPos1]->Residue->Shifts.Type->CTsuperclass, "subst")==0)
    {
      printf("%s",unit->Subst[anomerPos1]->Residue->Node.Name);
    }

  if(OMeFlag)
    {
      if(str==NULL)
	{
	  if(!carbBank)
	    {
	      printf("OMe");
	    }
	  else
	    {
	      printf("-%s-OME", unit->Shifts.Type->Atom[anomerPos1].Label);
	    }
	}
      else
	{
	  if(!carbBank)
	    {
	      strcat(str, "OMe");
	    }
	  else
	    {
	      sprintf(str, "%s-%s-OME", str, unit->Shifts.Type->Atom[anomerPos1].Label);		  
	    }
	}
    }
  if(str==NULL && !carbBank)
    {
      printf("[%s]", unit->Node.Name);
    }

  if(!inShiftList)
    {
      if(anomerPos1!=-1)
	{
	  if(unit->Subst[anomerPos1]!=NULL && (strcasecmp(unit->Node.Name, "a")!=0 || 
		     unit->Position!=BU_ANOMER_POS(unit->Subst[anomerPos1])))
	    {
	      if(str==NULL)
		{
		  if(!carbBank)
		    {
		      printf("(%s->",
			     unit->Shifts.Type->Atom[anomerPos1].Label);
		    }
		  /* If this is the first unit do not print the linkage if in carbBank mode instead print a
		     replacement OH terminal*/
		  else if(strcasecmp(unit->Node.Name, "a")!=0)
		    {
		      printf("-(%s-",
			     unit->Shifts.Type->Atom[anomerPos1].Label);
		    }
		  else
		    {
		      printf("-%s-OH", unit->Shifts.Type->Atom[anomerPos1].Label);
		    }
		}
	      else
		{
		  if(!carbBank)
		    {
		      sprintf(str, "%s(%s->",str,unit->Shifts.Type->Atom[anomerPos1].Label);
		    }
		  /* If this is the first unit do not print the linkage if in carbBank mode instead print a
		     replacement OH terminal */
		  else if(strcasecmp(unit->Node.Name, "a")!=0)
		    {
		      sprintf(str, "%s-(%s-",str, unit->Shifts.Type->Atom[anomerPos1].Label);
		    }
		  else
		    {
		      sprintf(str, "%s-%s-OH", str, unit->Shifts.Type->Atom[anomerPos1].Label);
		    }
		}
	      if(!last)
		{
		  if(str==NULL)
		    {
		      if(strcmp(unit->Subst[anomerPos1]->Residue->Shifts.Type->CTsuperclass, "subst")==0 &&
			 unit->Subst[anomerPos1]->Subst[BU_ANOMER_POS(unit->Subst[anomerPos1])]!=NULL)
			{
			  printf("%s)",unit->Subst[anomerPos1]->Subst[anomerPos2]->Shifts.Type->Atom[unit->Subst[anomerPos1]->Position].Label);
			}
		      else
			{
			  printf("%s)",
				 unit->Subst[anomerPos1]->Shifts.Type->Atom[unit->Position].Label);
			}
		    }
		  else
		    {
		      if(strcmp(unit->Subst[anomerPos1]->Residue->Shifts.Type->CTsuperclass, "subst")==0)
			{
			  strcat(str,unit->Subst[anomerPos1]->Subst[anomerPos2]->Shifts.Type->Atom[unit->Subst[anomerPos1]->Position].Label);
			  strcat(str, ")");
			}
		      else
			{
			  strcat(str,unit->Subst[anomerPos1]->Shifts.Type->Atom[unit->Position].Label);
			  strcat(str, ")");
			}
		    }	  
		}
	    }
	  else if(carbBank && !OMeFlag)
	    {
	      if(str==NULL)
		{
		  printf("-%s-OH", unit->Shifts.Type->Atom[anomerPos1].Label);
		}
	      else
		{
		  sprintf(str, "%s-%s-OH", str, unit->Shifts.Type->Atom[anomerPos1].Label);
		}
	    }
	}
    }
  return(PA_ERR_OK);
}
