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

/* Filename = glyco.c */
/* Prefix = GL */
/* Functions for printing structures in the GLYCO-CT format */

#include "build.h"
#include "calc.h"
#include "gfx.h"
#include "parser.h"
#include "node.h"
#include "glyco.h"
#include "type.h"
#include <ctype.h>
#include <string.h>
#include <stdlib.h>

/* Prints structure in CLYCO-CT format */
void GL_printstr(struct BU_Struct *structure)
{
  struct List GL_reslist;
  struct List GL_linklist;
  char repetition[32];

  InitList(&(GL_reslist));
  InitList(&(GL_linklist));

  repetition[0]=0;

  /* Build the list of residues and links. If no residues were found return
     without printing. */
  if(GL_build_reslist(structure,&(GL_reslist),&(GL_linklist),repetition)==PA_ERR_FAIL)
    {
        FreeList(&(GL_reslist));
	FreeList(&(GL_linklist));
	return;
    }

  if(repetition[0]!=0)
    {
      /* Print residue info for the repeating unit. CASPER only uses 1 repeating
	 unit so this is fixed for now. */
      printf("RES;\n1r:r1;\n");
      printf("REP;\n");
      printf("%s\n",repetition);
    }
  printf("RES;\n");
  ME_PrintList(&GL_ResMethod,&(GL_reslist));
  printf("LIN;\n");
  ME_PrintList(&GL_ResMethod,&(GL_linklist));
  GL_PrintAglycan(structure);
  printf("----\n");

  FreeList(&(GL_reslist));
  FreeList(&(GL_linklist)); 
}

int GL_build_reslist(struct BU_Struct *structure, struct List *GL_reslist, 
		     struct List *GL_linklist, char *repetition)
{
  struct BU_Unit *unit;
  char string[64];
  int rescnt=1,linkcnt=1;

  string[0]=0;

  unit=(struct BU_Unit *)GFX_FindReducing(structure);

  /* If no reducing position was found it is a repeating structure. Find the
     first unit in the structure. */
  if(unit==NULL)
    {
      unit=(struct BU_Unit *)FindNode((struct Node *)&structure->Units,"a");
      repetition[0]=1;
      /* Repeating units count as a residue too */
      rescnt++;
    }

  if(unit==NULL)
    return(PA_ERR_FAIL);

  GL_UnittoList(unit, GL_reslist, GL_linklist, &rescnt, &linkcnt);

  GL_FindUnit(unit, GL_reslist, GL_linklist, &rescnt, &linkcnt, repetition);

  return(PA_ERR_OK);
}

/* Find next unit by searching the linkage positions. The recursive function call
   results in that whole branches will be listed before the backbone is
   continued. The linkages are listed in order of linkage position. */
void GL_FindUnit(struct BU_Unit *unit, struct List *GL_reslist, 
		 struct List *GL_linklist, int *rescnt, int *linkcnt,
		 char *repetition)
{
  int i;
  int resnr=*rescnt;
  struct BU_Unit nextunit;
  struct GL_Info *link;
  char string[64];

  for(i=0;i<unit->Residue->Shifts.Type->HeavyCnt;i++)
    {
      /* Aglycans are handled separately. They can currently not link further. */
      /*      if(strcasecmp(unit->Residue->Shifts.Type->CTsuperclass, "aglycan")==0)
	{
	  return;
	}
	else*/ if(unit->Subst[i]!=0 && i!=unit->Shifts.Type->Anomeric)
	{
	  nextunit=(struct BU_Unit)*unit->Subst[i];
	  /* If nextunit is 'a' it is the last unit in a repeating structure */
	  if(nextunit.Node.Name[0]!='a')
	    {
	      if(strcasecmp(unit->Residue->Shifts.Type->CTsuperclass, "aglycan")!=0)	      
		{
		  /* Temporarily always use o and d linkage */
		  sprintf(string,"%d:%do(%d+%d)%dd;",*linkcnt,
			  resnr-(ListLen(&unit->Residue->Shifts.Type->CTsubsts)),
			  i+1,nextunit.Shifts.Type->Anomeric+1,*rescnt+1);
		  /*	      sprintf(string,"%d:%d%c(%d+%d)%d%c;",*linkcnt,
			      resnr-(ListLen(&unit->Residue->Shifts.Type->CTsubsts)),
			      unit->Residue->Shifts.Type->outlinkage,
			      i+1,nextunit.Shifts.Type->Anomeric+1,*rescnt+1,
			      unit->Residue->Shifts.Type->outlinkage);*/
		  *rescnt=*rescnt+1;
		  *linkcnt=*linkcnt+1;
		  link=(struct GL_Info *)ME_CreateNode(&GL_LinkMethod,GL_linklist,string);
		}
	      GL_UnittoList(&nextunit, GL_reslist, GL_linklist, rescnt, linkcnt);
	      GL_FindUnit(&nextunit, GL_reslist, GL_linklist, rescnt, linkcnt, repetition);
	    }
	  /* save the repetition information */
	  else
	    {
	      /* Linkage is specified to o and d right now. Currently number
		 of repetitions has to be specified */
	      sprintf(repetition,"REP1:%do(%d+%d)2d=5-25",
		      resnr-(ListLen(&unit->Residue->Shifts.Type->CTsubsts)),
		      i+1,nextunit.Shifts.Type->Anomeric+1);
	    }
	}
    }
}

void GL_UnittoList(struct BU_Unit *unit, struct List *GL_reslist,
		   struct List *GL_linklist, int *rescnt, int *linkcnt)
{
  struct GL_Info *res;
  struct TY_CTmodifier *mod;
  char string[64];
  char temp[64];
  int j;

  if(strcasecmp(unit->Residue->Shifts.Type->CTsuperclass, "aglycan")==0)
    {
      return;
    }

  sprintf(temp,"%db:%s-",*rescnt,unit->Residue->CTname);
  strcpy(string,temp);
  
  sprintf(temp,"%s-",unit->Residue->Shifts.Type->CTsuperclass);
  strcat(string,temp);
  
  /* Find the first position in the ring (is this always the anomeric??) */
  for(j=0;j<unit->Residue->Shifts.Type->HeavyCnt;j++)
    {
      if(unit->Residue->Shifts.Type->Atom[j].Type&TY_ANOMERIC)
	{
	  sprintf(temp,"%d:",j+1);
	  j=unit->Residue->Shifts.Type->HeavyCnt;
	}
    }
  strcat(string,temp);
  
  /* Find the ring closing position */
  for(j=unit->Residue->Shifts.Type->HeavyCnt-1;j>=0;j--)
    {
      if(unit->Residue->Shifts.Type->Atom[j].Type&TY_CLOSING)
	{
	  sprintf(temp,"%d",j+1);
	  j=0;
	}
    }
  strcat(string,temp);
  
  for(mod=(struct TY_CTmodifier *)First(unit->Residue->Shifts.Type->CTmods);
      mod->Node.Succ!=NULL; mod=(struct TY_CTmodifier *)mod->Node.Succ)
    {
      sprintf(temp,"|%d:%s",mod->position,mod->modifier);
      strcat(string,temp);
    }

  strcat(string,";");
  res=(struct GL_Info *)ME_CreateNode(&GL_ResMethod,GL_reslist,string);
  
  GL_SubsttoList(unit, GL_reslist, GL_linklist, rescnt, linkcnt);
}

void GL_SubsttoList(struct BU_Unit *unit, struct List *GL_reslist,
		    struct List *GL_linklist, int *rescnt, int *linkcnt)
{
  struct TY_CTsubstituent *sub;
  struct GL_Info *res;
  struct GL_Info *link;
  char string[64];

  for(sub=(struct TY_CTsubstituent *)First(unit->Residue->Shifts.Type->CTsubsts);
      sub->Node.Succ!=NULL; sub=(struct TY_CTsubstituent *)sub->Node.Succ)
    {
      /* Temporarily always linking substituents as d and n */
      /*      sprintf(string,"%d:%dd(%d+%d)%dn;",*linkcnt,*rescnt,
	      sub->position[0],sub->position[1],*rescnt+1);*/
      sprintf(string,"%d:%d%c(%d+%d)%dn;",*linkcnt,*rescnt,sub->basetypelinkage,
	      sub->position[0],sub->position[1],*rescnt+1);
      *linkcnt=*linkcnt+1;
      *rescnt=*rescnt+1;
      link=(struct GL_Info *)ME_CreateNode(&GL_LinkMethod,GL_linklist,string);
      sprintf(string,"%ds:%s;",*rescnt,sub->substituent);
      res=(struct GL_Info *)ME_CreateNode(&GL_ResMethod,GL_reslist,string);
    }
}

/* This is a very crude way to handle peptides at the reducing end (for
   n- and o-glycans with one peptide left). It only works for aglycans at
   the reducing end and only one aglycan structure. */
int GL_PrintAglycan(struct BU_Struct *structure)
{
  struct BU_Unit *unit;

  for(unit=(struct BU_Unit *)structure->Units.Head.Succ;
      unit->Node.Succ!=NULL;
      unit=(struct BU_Unit *)unit->Node.Succ)
    {
      if(strcasecmp(unit->Residue->Shifts.Type->CTsuperclass, "aglycan")==0)
	{
	  printf("NON1\n");
	  printf("Child:1\n"); /* This is currently hard coded */
	  printf("Linkage:n(1+1)o\n");
	  printf("Peptide:%s\n",unit->Residue->CTname);
	  return 1;
	}
    }
  return 0;
}
/* This syntax reads a block of Glyco-CT information building a CASPER
   structure from it. It reads the residue and linkage information to build
   the molecule. The command can only be used as a build command.
   SYNTAX: glycoct *
           [Glyco-CT structure info]
	   *
	   */
int GL_GlycoInput()
{
  struct List GL_reslist;
  struct List GL_linklist;
  struct List GL_replist;
  struct List GL_unitsLinkingToReps;
  struct GL_Info *res, *subst;
  struct GL_Info *link, *rep;
  struct TY_CTsubstituent substProperties[8];
  struct TY_CTmodifier modProperties[8];
  struct RE_Residue *RE_Res;
  struct BU_Unit *unitLinkFrom, *unitLinkTo, *tempUnit;
  struct Node *node;
  char line[128];
  char *resline, *restemp, *linkline, *substtemp, *repline;
  char *data,*mods;
  char *linktemp;
  char mode=0, label[3];
  char residue[32], sugartype[32],closingpos;
  int linkToPos, linkToRes, linkFromPos, linkFromRes, anomerPos;
  int residuenr, nresidues, nsubsts, nmods, rescounter, modcounter, typecounter, flag, i, nReps;

  PA_GetString;
  if(PA_Result.String[0]!='*')
    Error(PA_ERR_FAIL,"Structure block must start with '*'.");


  InitList(&(GL_reslist));
  InitList(&(GL_linklist));
  InitList(&(GL_replist));
  InitList(&(GL_unitsLinkingToReps));

  if(fgets(line,127,PA_Status->Input)==NULL)
    {
      return(PA_ERR_FAIL);
    }
  /* First create a list of the residues, linkages and repetition info in the
     structure */
  while(line[0]!='*')
    {
      if(strlen(line)<=1)
	{
	  if(fgets(line,127,PA_Status->Input)==NULL)
	    {
	      return(PA_ERR_FAIL);
	    }
	  continue;
	}
      line[strlen(line)-1]=0;
      if(strcasecmp(line,"RES")==0)
	{
	  mode='R';
	  if(fgets(line,127,PA_Status->Input)==NULL)
	    {
	      return(PA_ERR_FAIL);
	    }
	  continue;
	}
      else if(strcasecmp(line,"LIN")==0)
	{
	  mode='L';
	  if(fgets(line,127,PA_Status->Input)==NULL)
	    {
	      return(PA_ERR_FAIL);
	    }
	  continue;
	}
      else if(strcasecmp(line,"REP")==0)
	{
	  mode='X';
	  if(fgets(line,127,PA_Status->Input)==NULL)
	    {
	      return(PA_ERR_FAIL);
	    }
	  continue;
	} 
      if(mode=='R')
	{
	      res=(struct GL_Info *)ME_CreateNode(&GL_ResMethod,&(GL_reslist),
						  line);
	}
      if(mode=='L')
	{
	  link=(struct GL_Info *)ME_CreateNode(&GL_LinkMethod,&(GL_linklist),line);
	}
      if(mode=='X')
	{
	  rep=(struct GL_Info *)ME_CreateNode(&GL_ResMethod,&(GL_replist),line);
	}
      if(fgets(line,127,PA_Status->Input)==NULL)
	{
	  return(PA_ERR_FAIL);
	}
    }
  nresidues=0;
  residuenr=0;

  /* Add the residues one at a time. Substituents are added to the basetypes
     by looking at the linkage information to see how they are linked together */
  for(res=(struct GL_Info *)GL_reslist.Head.Succ;res->Node.Succ!=NULL;
      res=(struct GL_Info *)res->Node.Succ)
    {
      residuenr++;
      if(res->Node.Name[1]!='b' && res->Node.Name[2]!='b')
	{
	  continue;
	}
      nresidues++;
      resline=malloc(strlen(res->Node.Name)+1);
      strcpy(resline,res->Node.Name);
      restemp=strtok(resline,":");
      restemp=strtok(NULL,":");
      closingpos=restemp[strlen(restemp)+1];
      strtok(NULL,"|");
      mods=strtok(NULL,"");
      rescounter=0;
      typecounter=0;
      flag=0;
      for(i=strlen(restemp)-1; i>0 && flag<2; i--)
	{
	  if(restemp[i]=='-')
	    {
	      flag++;
	      restemp[i]=0;
	    }
	}
      i=i+2;
      strcpy(sugartype, &restemp[i]);
      strcpy(residue, restemp);
      /*      flag=0;
      for(i=0;i<strlen(restemp) && rescounter<31 && typecounter<31;i++)
	{
	  if(restemp[i]!='-')
	    {
	      residue[rescounter++]=restemp[i];
	    }
	    }*/
      nmods=0;
      modcounter=0;
      if(mods)
	{
	  /* Make a list of the mods */
	  for(i=0;i<strlen(mods);i++)
	    {
	      if(isdigit(mods[i]))
		{
		  modProperties[nmods].position=mods[i]-'0';
		}
	      if(isalpha(mods[i]))
		{
		  modProperties[nmods].Node.Name[modcounter++]=mods[i];
		}
	      if((mods[i]==(int)';' || mods[i]==(int)'|') && strlen(mods)>2)
		{
		  modProperties[nmods].Node.Name[modcounter]=0;
		  sprintf(modProperties[nmods].Node.Name,"%s%d",modProperties[nmods].Node.Name,modProperties[nmods].position);
		  modcounter=0;
		  nmods++;
		}
	    }
	  /* If the line did not end with ';' increase the modcounter here */
	  if(mods[i-1]!=(int)';')
	    {
	      modProperties[nmods].Node.Name[modcounter]=0;
	      sprintf(modProperties[nmods].Node.Name,"%s%d",modProperties[nmods].Node.Name,modProperties[nmods].position);
	      nmods++;
	    }
	}

      nsubsts=0;
      data=(char *)strtok(line,":");

      /* Browse through the linkages to find the substituents. Make a list of
	 all substituents for this basetype. So far the links are only read
	 to create each sugar unit separately. They are also used later on
	 to link them together */
      for(link=(struct GL_Info *)GL_linklist.Head.Succ;link->Node.Succ!=NULL &&
	    nsubsts<8;
	  link=(struct GL_Info *)link->Node.Succ)
	{
	  linkline=malloc(strlen(link->Node.Name)+2);
	  strcpy(linkline,link->Node.Name);
	  strtok(linkline,":");
	  linktemp=strtok(NULL,"");
	  linkToRes=atoi(strtok(linktemp,"don"));
	  linktemp=strtok(NULL,"(");
	  linkToPos=atoi(strtok(linktemp,"+"));
	  linkFromPos=atoi(strtok(NULL,")"));
	  linkFromRes=atoi(strtok(NULL,"don"));

	  if(linkToRes==residuenr || linkFromRes==residuenr)
	    {
	      if(linkToRes==residuenr)
		{
		  subst=FindNodeNr(&(GL_reslist.Head),linkFromRes);
		}
	      else
		{
		  subst=FindNodeNr(&(GL_reslist.Head),linkToRes);
		}
	      if(!subst)
		{
		  Error(PA_ERR_FAIL,"Cannot add substituent");
		}
	      if(subst->Node.Name[1]=='s' || subst->Node.Name[2]=='s')
		{
		  substtemp=malloc(strlen(subst->Node.Name));
		  flag=0;
		  rescounter=0;
		  for(i=0;i<strlen(subst->Node.Name);i++)
		    {
		      if(flag==1)
			{
			  substtemp[rescounter++]=subst->Node.Name[i];
			}
		      else if(subst->Node.Name[i]==':')
			{
			  flag=1;
			}
		    }
		  substtemp[rescounter]=0;
		  
		  strcpy(substProperties[nsubsts].substituent,substtemp);
		  sprintf(substProperties[nsubsts].Node.Name,"%s%d",substtemp,linkToPos);
		  if(linkToRes==nresidues)
		    {
		      substProperties[nsubsts].position[0]=linkToPos;
		      substProperties[nsubsts].position[1]=linkFromPos; 
		    }
		  else
		    {
		      substProperties[nsubsts].position[0]=linkFromPos;
		      substProperties[nsubsts].position[1]=linkToPos;		      
		    }
		  nsubsts++;

		  /* Remove the links to substituents from the link list */
		  link=(struct GL_Info *)link->Node.Pred;
		  FreeNode(link->Node.Succ);
		  
		  free(substtemp);
		}
	    }

	  free(linkline);
	}
      
      for(RE_Res=(struct RE_Residue *)ResidueList.Head.Succ;
	  RE_Res->Node.Succ!=NULL;
	  RE_Res=(struct RE_Residue *)RE_Res->Node.Succ)
	{
	  /* Check if this is the right base type */
	  if(strcasecmp(residue,RE_Res->CTname)==0)
	    {
	      /* Check that the super class is correct */
	      if(strcasecmp(sugartype,RE_Res->Shifts.Type->CTsuperclass)==0)
		{
		  if(ListLen(&RE_Res->Shifts.Type->CTsubsts)==nsubsts)
		    {
		      if(ListLen(&RE_Res->Shifts.Type->CTmods)==nmods)
			{
			  flag=1;
			  for(i=0;i<nsubsts;i++)
			    {
			      if(FindNode(&RE_Res->Shifts.Type->CTsubsts.Head,
					  substProperties[i].Node.Name))
				{
				  /* Should check that the positions are right too,
				     but due to the limited number of substituents
				     in CASPER this works for now */
				}
			      else
				{
				  flag=0;
				} 
			    }
			  if(flag==1)
			    {
			      for(i=0;i<nmods;i++)
				{
				  if(FindNode(&RE_Res->Shifts.Type->CTmods.Head,
					  modProperties[i].Node.Name))
				    {
				      /* Should check that the position is right too */
				    }
				  else
				    {
				      flag=0;
				    }
				}
			    }
			  if(flag==1)
			    {
			      sprintf(label,"%c",'a'+nresidues-1);
			      BU_Unit=(struct BU_Unit*)ME_CreateNode(&UnitMethod, &(BU_Struct->Units), label);
			      if (BU_Unit==NULL)
				{
				  Error(PA_ERR_FATAL,"Out of memory");
				}
			      switch (RE_Res->JCH)
				{
				case RE_SMALL:	BU_Struct->JCH[RE_SMALL]++;
				  break;
				case RE_MEDIUM:	BU_Struct->JCH[RE_MEDIUM]++;
				  break;
				case RE_LARGE:	BU_Struct->JCH[RE_LARGE]++;
				  break;
				};
			      switch (RE_Res->JHH)
				{
				case RE_SMALL:	BU_Struct->JHH[RE_SMALL]++;
				  break;
				case RE_MEDIUM:	BU_Struct->JHH[RE_MEDIUM]++;
				  break;
				case RE_LARGE:	BU_Struct->JHH[RE_LARGE]++;
				  break;
				};
			      SH_RAW_COPY(&(RE_Res->Shifts), &(BU_Unit->Shifts));  /* Copy shifts from the residue to the new unit. */
			      BU_Unit->Residue=RE_Res;
			      BU_Unit->Shifts.Type=RE_Res->Shifts.Type;
			      BU_Unit->Error=RE_Res->Error;
			      break;
			    }
			}
		    }
		}
	    }
	}
      free(resline);
    }
  /* Browse through the linkages to create the correct linkages in the
     CASPER structure */
  for(link=(struct GL_Info *)GL_linklist.Head.Succ;link->Node.Succ!=NULL;
      link=(struct GL_Info *)link->Node.Succ)
    {
      linkline=malloc(strlen(link->Node.Name)+2);
      strcpy(linkline,link->Node.Name);
      strtok(linkline,":");
      linktemp=strtok(NULL,"");
      linkToRes=atoi(strtok(linktemp,"don"));
      linktemp=strtok(NULL,"(");
      linkToPos=atoi(strtok(linktemp,"+"));
      linkFromPos=atoi(strtok(NULL,")"));
      linkFromRes=atoi(strtok(NULL,"don"));
      linkFromPos--;
      linkToPos--;

      residuenr=-1;
      res=(struct GL_Info *)FindNodeNr(&(GL_reslist.Head),linkFromRes);
      if(!res)
	{
	  Error(PA_ERR_FAIL,"Error creating link");
	}

      /* Handle links from repeating units */
      if(res->Node.Name[1]=='r' || res->Node.Name[2]=='r')
	{
	  node=FindNodeNr(&(GL_replist.Head),(int)res->Node.Name[strlen(res->Node.Name)-1]-48);
	  repline=malloc(strlen(node->Name)+2);
	  strcpy(repline,node->Name);
	  strtok(repline,"(");
	  if(linkToPos<0)
	    {
	      linkToPos=atoi(strtok(NULL,"+"))-1;
	    }
	  else
	    {
	      strtok(NULL,"+");
	    }
	  if(linkFromPos<0)
	    {
	      linkFromPos=atoi(strtok(NULL,")"))-1;
	    }
	  else
	    {
	      strtok(NULL,")");
	    }
	  linkFromRes=atoi(strtok(NULL,"don"));
	  res=(struct GL_Info *)FindNodeNr(&(GL_reslist.Head),linkFromRes);
	  if(!res)
	    {
	      Error(PA_ERR_FAIL,"Error creating link");
	    }
	  free(repline);
	}

      for(;res->Node.Pred!=NULL;res=(struct GL_Info *)res->Node.Pred)
	{
	  if(res->Node.Name[1]=='b'||res->Node.Name[2]=='b')
	    {
	      residuenr++;
	    }
	}
      
      sprintf(label,"%c",'a'+residuenr);
      unitLinkFrom=FindNode(&(BU_Struct->Units.Head),label);
      if(!unitLinkFrom)
	{
	  Error(PA_ERR_FAIL,"Cannot find unit when creating links");
	}

      residuenr=-1;
      res=(struct GL_Info *)FindNodeNr(&(GL_reslist.Head),linkToRes);

      /* Handle links to repeating units */
      if(res->Node.Name[1]=='r' || res->Node.Name[2]=='r')
	{
	  /* Create a list of all units linking to repeating units */
	  node=MakeNode(sizeof(struct Node),label);
	  AddNode(&(GL_unitsLinkingToReps.Head),node);


	  node=FindNodeNr(&(GL_replist.Head),(int)res->Node.Name[strlen(res->Node.Name)-1]-48);
	  repline=malloc(strlen(node->Name)+2);
	  strcpy(repline,node->Name);
	  strtok(repline,":");
	  linktemp=strtok(NULL,"");
	  linkToRes=atoi(strtok(linktemp,"don"));
	  strtok(repline,"(");
	  if(linkToPos<0)
	    {
	      linkToPos=atoi(strtok(NULL,"+"))-1;
	    }
	  else
	    {
	      strtok(NULL,"+");
	    }
	  if(linkFromPos<0)
	    {
	      linkFromPos=atoi(strtok(NULL,")"))-1;
	    }
	  else
	    {
	      strtok(NULL,")");
	    }
	  res=(struct GL_Info *)FindNodeNr(&(GL_reslist.Head),linkToRes);
	  if(!res)
	    {
	      Error(PA_ERR_FAIL,"Error creating link");
	    }
	  free(repline);	  
	}

      for(;res->Node.Pred!=NULL;
	  res=(struct GL_Info *)res->Node.Pred)
	{
	  if(res->Node.Name[1]=='b'||res->Node.Name[2]=='b')
	    {
	      residuenr++;
	    }
	}

      sprintf(label,"%c",'a'+residuenr);
      unitLinkTo=FindNode(&(BU_Struct->Units.Head),label);

      if(!unitLinkTo)
	{
	  Error(PA_ERR_FAIL,"Cannot find unit when creating links");
	}
      
      if (!(unitLinkTo->Shifts.Type->Atom[linkToPos].Type & TY_FREE_POS))
	{
	  Error(PA_ERR_FAIL,"Reducing end can not be substituted");
	}
      if (!(unitLinkFrom->Shifts.Type->Atom[linkFromPos].Type & TY_FREE_POS))
	{
	  Error(PA_ERR_FAIL,"Anomeric position cannot be substituted");
	}
      unitLinkFrom->Subst[linkFromPos]=unitLinkTo;
      unitLinkTo->Subst[linkToPos]=unitLinkFrom;
      unitLinkFrom->Position=linkToPos;
      
      /* Calculate spectrum */
      /*      flag=CA_SimDisacch(unitLinkFrom, linkFromPos,
			 linkToPos, unitLinkTo);
      if (flag!=PA_ERR_OK)
	{
	  Error(PA_ERR_FAIL, "No data for at least one bond");
	}
      */
      free(linkline);
    }
  /* Handle repititions. Currently only one repition is used and it just creates
     a repeating unit. NOT YET FULLY IMPLEMENTED */
  
  for(rep=(struct GL_Info *)GL_replist.Head.Succ;rep->Node.Succ!=NULL;
      rep=(struct GL_Info *)rep->Node.Succ)
    {
      repline=malloc(strlen(rep->Node.Name)+2);
      /* First check the end of the line to see how many times this sequence
	 is repeated. If no more than two just skip it for now since it cannot
	 be considered a repeating unit. This should be fixed to be more generic */
      strcpy(repline,rep->Node.Name);
      strtok(repline,"=");
      strtok(NULL,"-");
      linktemp=strtok(NULL,"");
      if(strcmp(linktemp,"?")!=0)
	{
	  nReps=atoi(linktemp);
	  if(nReps<=1 && nReps!=-1)
	    {
	      continue;
	    }
	}


      strcpy(repline,rep->Node.Name);
      strtok(repline,":");
      linktemp=strtok(NULL,"");
      linkToRes=atoi(strtok(linktemp,"("));
      linkToPos=atoi(strtok(NULL,"+-"));
      linkFromPos=atoi(strtok(NULL,")"));
      linkFromRes=atoi(strtok(NULL,"="));
      linkFromPos--;
      linkToPos--;

      residuenr=-1;
      res=(struct GL_Info *)FindNodeNr(&(GL_reslist.Head),linkFromRes);
      if(!res)
	{
	  Error(PA_ERR_FAIL,"Error creating link");
	}
      if(res->Node.Name[1]=='b' || res->Node.Name[2]=='b')
	{
	  for(;res->Node.Pred!=NULL;res=(struct GL_Info *)res->Node.Pred)
	    {
	      if(res->Node.Name[1]=='b'||res->Node.Name[2]=='b')
		{
		  residuenr++;
		}
	    }
	}
      sprintf(label,"%c",'a'+residuenr);
      unitLinkFrom=FindNode(&(BU_Struct->Units.Head),label);
      if(!unitLinkFrom)
	{
	  Error(PA_ERR_FAIL,"Cannot find unit when creating links");
	}

      residuenr=-1;
      res=(struct GL_Info *)FindNodeNr(&(GL_reslist.Head),linkToRes);
      for(;res->Node.Pred!=NULL;
	  res=(struct GL_Info *)res->Node.Pred)
	{
	  if(res->Node.Name[1]=='b'||res->Node.Name[2]=='b')
	    {
	      residuenr++;
	    }
	}
      sprintf(label,"%c",'a'+residuenr);
      unitLinkTo=FindNode(&(BU_Struct->Units.Head),label);

      if(!unitLinkTo)
	{
	  Error(PA_ERR_FAIL,"Cannot find unit when creating links");
	}
      
      if (!(unitLinkTo->Shifts.Type->Atom[linkToPos].Type & TY_FREE_POS))
	{
	  Error(PA_ERR_FAIL,"Reducing end can not be substituted");
	}
      if (!(unitLinkFrom->Shifts.Type->Atom[linkFromPos].Type & TY_FREE_POS))
	{
	  Error(PA_ERR_FAIL,"Anomeric position cannot be substituted");
	}

      if(nReps<=3)
	{
	  /* Temporarily remove links from units linking to the repeating
	     unit, but not being part of it. Without removing them now they
	     would also be repeated. E.g. in: A->[B->C] remove the link
	     between A and B */
	  for(node=GL_unitsLinkingToReps.Head.Succ;node->Succ!=NULL;
	      node=node->Succ)
	    {
	      tempUnit=(struct BU_Unit *)FindNode(&(BU_Struct->Units.Head),node->Name);
	      if(tempUnit)
		{
		  for(i=TY_MAX_BONDS-1;i>=0;i--)
		    {
		      if(unitLinkTo->Subst[i]==tempUnit)
			{
			  unitLinkTo->Subst[i]=0;
			}
		    }
		}
	    }
	  for(i=1;i<nReps;i++)
	    {
	      if(BU_DuplicateStructSegment(BU_Struct,unitLinkFrom,linkFromPos,
					    unitLinkTo,linkToPos)!=PA_ERR_OK)
		{
		  return(PA_ERR_FAIL);
		}
	    }
	  /* Recreate the links that were broken before duplicating the
	     repeating segment */
	  for(node=GL_unitsLinkingToReps.Head.Succ;node->Succ!=NULL;
	      node=node->Succ)
	    {
	      tempUnit=(struct BU_Unit *)FindNode(&(BU_Struct->Units.Head),node->Name);
	      anomerPos=BU_ANOMER_POS(tempUnit);
	      if(anomerPos!=-1 && tempUnit->Subst[anomerPos]==unitLinkTo)
		{
		  unitLinkTo->Subst[tempUnit->Position]=tempUnit;
		}
	    }	  
	}
      /* Otherwise create an "infinite" repetition */
      else
	{

	  unitLinkFrom->Subst[linkFromPos]=unitLinkTo;
	  unitLinkTo->Subst[linkToPos]=unitLinkFrom;
	  unitLinkFrom->Position=linkToPos;
	  
	  /* Calculate spectrum */
	  /*	  flag=CA_SimDisacch(unitLinkFrom, linkFromPos,
			     linkToPos, unitLinkTo);
	  */
	}
      /*
      if (flag!=PA_ERR_OK)
	{
	  Error(PA_ERR_FAIL, "No data for at least one bond");
	}
	  */
      free(repline);
    }

  FreeList(&(GL_reslist));
  FreeList(&(GL_linklist)); 
  FreeList(&(GL_replist));

  GFX_RenameUnits(BU_Struct);
  QuickSort(BU_Struct->Units.Head.Succ,BU_Struct->Units.Tail.Pred,NameCompare,0);

  return(PA_ERR_OK);
}

/* ---- GL_Res Methods ---- */

void *GL_CreateGL_Res(char *name)
{
  return (MakeNode(sizeof(struct GL_Info), name));
}

/* void GL_ClearGL_Res(struct GL_res *res)
{
}
*/ 
void GL_PrintGL_Res(struct GL_Info *res)
{
  printf("%s\n",res->Node.Name);
}

struct ME_Method GL_ResMethod={
  GL_CreateGL_Res, /*GL_ClearGL_Res*/ NULL, NULL,
  GL_PrintGL_Res, GL_PrintGL_Res, NULL
};
/* ---- End of GL_Res Methods ---- */

/* ---- GL_Link Methods ---- */

void *GL_CreateGL_Link(char *name)
{
  return (MakeNode(sizeof(struct GL_Info), name));
}

void GL_ClearGL_Link(struct GL_Info *link)
{
}

void GL_PrintGL_Link(struct GL_Info *link)
{
  printf("%s",link->Node.Name);
}

struct ME_Method GL_LinkMethod={
  GL_CreateGL_Link, GL_ClearGL_Link, NULL,
  NULL, GL_PrintGL_Res, NULL
};
/* ---- End of GL_Res Methods ---- */

