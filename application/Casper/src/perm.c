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

/* Filename = perm.c */
/* Prefix = PE */
/* Contains procedures for structure generation */
/* generation */

#include "perm.h"
#include "parser.h"
#include "spectra.h"
#include "calc.h"
#include "gfx.h"
#include "variables.h"
#include "ccpn.h"
#include "build.h"
#include "rule.h"
#include <math.h>
#include <string.h>

/* Perhaps change so that 'next-worst' is also found */
/* This may save some time! */
struct BU_Struct *PE_FindWorstFit()
{
  float fit;
  struct BU_Struct *structure, *worst;
  structure=(struct BU_Struct *)First(StructList);
  if (structure->Node.Succ==NULL) return(NULL);
  worst=structure;
  switch (SE_Simulation.Criteria)
    {
    case SE_PURGE_C:
      fit=structure->CFit;
      break;
    case SE_PURGE_H:
      fit=structure->HFit;
      break;
    case SE_PURGE_CH:
      fit=structure->ChFit;
      break;
    case SE_PURGE_HH:
      fit=structure->HhFit;
      break;
      
    };

  while (structure->Node.Succ!=NULL)
    {
      switch (SE_Simulation.Criteria)
	{
	case SE_PURGE_C:
	  if (structure->CFit>fit)
	  {
	    fit=structure->CFit;
	    worst=structure;
	  };
	  break;
	case SE_PURGE_H:
	  if (structure->HFit>fit)
	  {
	    fit=structure->HFit;
	    worst=structure;
	  };
	  break;
	case SE_PURGE_CH:
	  if (structure->ChFit>fit)
	  {
	    fit=structure->ChFit;
	    worst=structure;
	  };
	  break;
	case SE_PURGE_HH:
	  if (structure->HhFit>fit)
	  {
	    fit=structure->HhFit;
	    worst=structure;
	  };
	  break;
	  
	};
      structure=(struct BU_Struct *)structure->Node.Succ;
    }; 

  SE_Simulation.WorstFit=fit;
  return(worst);
}

int PE_AreSameUnit(struct BU_Unit *first_u, struct BU_Unit *second_u)
{
  int pos;
  if (first_u->Residue!=second_u->Residue) return(FALSE);
  for (pos=0; pos<TY_MAX_CARBON; pos++)
    {
      if (first_u->Subst[pos]!=NULL)
	{
	  if (second_u->Subst[pos]==NULL)
	    {
	      return(FALSE);
	    }
	  if (first_u->Subst[pos]->Residue!=second_u->Subst[pos]->Residue)
	    {
	      return(FALSE);
	    }
	}
      else
	if (second_u->Subst[pos]!=NULL) return(FALSE);
    };
  return(TRUE);
}

/* should optimize */
/* Returns FALSE if slightest difference is found */
int PE_AreSameStruct(struct BU_Struct *first, struct BU_Struct *second)
{
  struct BU_Unit *first_u, *second_u;
  int flag;
  if (fabs(first->CFit-second->CFit)>0.001)
    {
      return(FALSE);
    }
  if (fabs(first->HFit-second->HFit)>0.001)
    {
      return(FALSE);
    }
  if (fabs(first->ChFit-second->ChFit)>0.001)
    {
      return(FALSE);
    }
  if (fabs(first->HhFit-second->HhFit)>0.001)
    {
      return(FALSE);
    }
  for ( first_u=(struct BU_Unit *)first->Units.Head.Succ;
	first_u->Node.Succ!=NULL;
	first_u=(struct BU_Unit *)first_u->Node.Succ )
    {
      flag=FALSE;
      for ( second_u=(struct BU_Unit *)second->Units.Head.Succ;
	    second_u->Node.Succ!=NULL;
	    second_u=(struct BU_Unit *)second_u->Node.Succ)
	{
	  if (PE_AreSameUnit(first_u,second_u))
	    {
	      flag=TRUE;
	      break;
	    }
	}
      if (flag==FALSE)
	{
	  return(FALSE);
	}
    }

  for (second_u=(struct BU_Unit *)second->Units.Head.Succ;
       second_u->Node.Succ!=NULL;
       second_u=(struct BU_Unit *)second_u->Node.Succ)
    {
      flag=FALSE;
      for ( first_u=(struct BU_Unit *)first->Units.Head.Succ;
	    first_u->Node.Succ!=NULL;
	    first_u=(struct BU_Unit *)first_u->Node.Succ)
	{
	  if (PE_AreSameUnit(first_u,second_u))
	    {
	      flag=TRUE;
	      break;
	    }
	}
      if (flag==FALSE)
	{
	  return(FALSE);
	}
    }
  return(TRUE);
}

/* Removes any duplicates */
void PE_PurgeSame()
{
  struct BU_Struct *first, *second;
  for(first=(struct BU_Struct *)First(StructList);first->Node.Succ!=NULL;
      first=(struct BU_Struct *)first->Node.Succ)
    {
      second=(struct BU_Struct *)first->Node.Succ;
      while (second->Node.Succ!=NULL)
	{
	  if (PE_AreSameStruct(first,second))
	    {
	      /* DEBUG print statements*/
	      /*	      if(fabs(second->ChFit-6.24)<0.01)
		{
		  printf("Removing ");
		  ME_PrintNode(&StructMethod,(struct Node *)second);
		  printf("Same as ");
		  ME_PrintNode(&StructMethod,(struct Node *)first);
		}
	      */
	      ME_RemoveNode(&StructMethod,(struct Node *)second);
	      SE_Simulation.StrActual--;
	      second=(struct BU_Struct *)first->Node.Succ;
	    }
	  else 
	    {
	      second=(struct BU_Struct *)second->Node.Succ;
	    }
	}
    }
}

/* This function purges all structures of higher number than SE_Simulation.StrLower */
void PE_PurgeList()
{
  struct BU_Struct *last;

  PE_SortStructures();

  while (SE_Simulation.StrActual>SE_Simulation.StrLower)
    {
      last=(struct BU_Struct *)Last(StructList);
      ME_RemoveNode(&StructMethod,(struct Node *)last);
      SE_Simulation.StrActual--;
    }
  switch (SE_Simulation.Criteria)
    {
    case SE_PURGE_H:	
      SE_Simulation.WorstFit=((struct BU_Struct *)
			      Last(StructList))->HFit;
      break;
    case SE_PURGE_C:
      SE_Simulation.WorstFit=((struct BU_Struct *)
			      Last(StructList))->CFit;
      break;
     case SE_PURGE_CH:
       SE_Simulation.WorstFit=((struct BU_Struct *)
			       Last(StructList))->ChFit;
      break;
    case SE_PURGE_HH:
      SE_Simulation.WorstFit=((struct BU_Struct *)
			      Last(StructList))->HhFit;
      break;
      
    case SE_PURGE_TOT:
      SE_Simulation.WorstFit=((struct BU_Struct *)
			      Last(StructList))->TotFit;
      break;
   }
}


int PE_Compare(const struct BU_Struct *str1, const struct BU_Struct *str2, int mode)
{
  switch (mode)
    {
    case SE_PURGE_C:
      if (str1->CFit<str2->CFit)
	{
	  return(-1);
	}
      if (str1->CFit==str2->CFit)
	{
	  return(0);
	}
      break;
    case SE_PURGE_H:
      if (str1->HFit<str2->HFit)
	{
	  return(-1);
	}
      if (str1->HFit==str2->HFit)
	{
	  return(0);
	}
      break;
    case SE_PURGE_CH:
      if (str1->ChFit<str2->ChFit)
	{
	  return(-1);
	}
      if (str1->ChFit==str2->ChFit)
	{
	  return(0);
	}
      break;
    case SE_PURGE_HH:
      if (str1->HhFit<str2->HhFit)
	{
	  return(-1);
	}
      if (str1->HhFit==str2->HhFit)
	{
	  return(0);
	}
      break;
      
    case SE_PURGE_TOT:
      if (str1->TotFit<str2->TotFit)
	{
	  return(-1);
	}
      if (str1->TotFit==str2->TotFit)
	{
	  return(0);
	}
      break;
    }
  return(1);
}

void PE_SortStructures()
{
  /*  SortList(&StructList, PE_Compare, SE_Simulation.Criteria);*/
  if(ListLen(&StructList)==0)
    {
      return;
    }
  if(strcmp(StructList.Head.Succ->Name,"exp")==0)
    {
      QuickSort(StructList.Head.Succ->Succ,StructList.Tail.Pred,PE_Compare,SE_Simulation.Criteria);
    }
  else
    {
      QuickSort(StructList.Head.Succ,StructList.Tail.Pred,PE_Compare,SE_Simulation.Criteria);      
    }
}

int PE_MkStruct()
{
  char str_name[SE_NAME_LENGTH];
  struct BU_Unit *unit;
  struct BU_Struct *structure;

  struct RE_Residue *residue;
  int i_SP;
  char unit_name[SE_NAME_LENGTH]; /* Must change! */

  struct BU_Unit *unit1,*unit2;
  int pos1,pos2;
  int b_SP;
  int state, remove;
  int i, item, bond;

  remove=TRUE;

  for(item=0;item<SE_ResidueSP;item++)
    {
      if(SE_ResidueStack[item].Residue->FreeCertainty==TRUE)
	{
	  for(i=0;i<TY_MAX_BONDS;i++)
	    {
	      /*	      if(SE_ResidueStack[item].Residue->Free[i]==SE_FREE)*/
	      if (SE_ResidueStack[item].Residue->Anomeric!=i&&
		 (SE_ResidueStack[item].Residue->Free[i]-SE_OCCUPIED==SE_FREE/*||
									       SE_ResidueStack[item].Residue->Free[i]==SE_FREE*/))
		{
		  remove=TRUE;
		  for(bond=0;bond<SE_BondSP;bond++)
		    {
		      if(SE_BondStack[bond].ToRes==item && SE_BondStack[bond].ToPos==i)
			{
			  remove=FALSE;
			  break;
			}
		    }
		  if(remove==TRUE)
		    {
		      /*		      printf("Pos. %d residue %s (%s) not fully linked\n",i,
			     SE_ResidueStack[item].Residue->Base->Node.Name, 
			     SE_ResidueStack[item].UnitSpecification->Node.Name);*/
		      return(PA_ERR_OK);
		    }
		}
	      else if(SE_ResidueStack[item].Residue->Anomeric!=i &&
		      SE_ResidueStack[item].Residue->Free[i]==SE_FREE)
		{
		  /*		  printf("Pos. %d residue %s (%s) not fully linked\n",i,
			 SE_ResidueStack[item].Residue->Base->Node.Name, 
			 SE_ResidueStack[item].UnitSpecification->Node.Name);*/
		  return(PA_ERR_OK);
		}
	    }
	}
    }

  remove=FALSE;

  /* mkstruct */
  sprintf(str_name,"%.16s%d",SE_Simulation.Name,SE_Simulation.StrCnt);
  structure=(struct BU_Struct *)
    ME_CreateNode(&StructMethod, &StructList, str_name);
  if (structure==NULL) Error(PA_ERR_FATAL,"Out of memory");
  SE_Simulation.StrCnt++;


  /* mkunit */
  for(i_SP=0; i_SP<SE_ResidueSP; i_SP++)
    {
      sprintf(unit_name,"%d",i_SP+1); 
      residue=SE_ResidueStack[i_SP].Residue->Base; 
      unit=(struct BU_Unit *)
	ME_CreateNode(&UnitMethod, &(structure->Units), unit_name);
      if (unit==NULL) Error(PA_ERR_FATAL,"Out of memory");
      /*  for(i=0;i<SE_MAX_BONDS;i++)
	  unit->FreePos[i]=Item_Stack[i_SP].item->Free[i]; */
      SH_RAW_COPY(&(residue->Shifts), &(unit->Shifts));
      unit->Shifts.Type=residue->Shifts.Type;
      unit->Residue=residue;
      unit->Error=residue->Error;
      switch (residue->JCH)
	{
	case RE_SMALL:	structure->JCH[RE_SMALL]++;
	  break;
	case RE_MEDIUM:	structure->JCH[RE_MEDIUM]++;
	  break;
	case RE_LARGE:	structure->JCH[RE_LARGE]++;
	  break;
	};
      switch (residue->JHH)
	{
	case RE_SMALL:	structure->JHH[RE_SMALL]++;
	  break;
	case RE_MEDIUM:	structure->JHH[RE_MEDIUM]++;
	  break;
	case RE_LARGE:	structure->JHH[RE_LARGE]++;
	  break;
	};

      SE_ResidueStack[i_SP].Unit=unit;
    };
  /*  mkbonds   */
  for(b_SP=0; b_SP<SE_BondSP; b_SP++)
    {
      unit1=SE_ResidueStack[SE_BondStack[b_SP].FromRes].Unit;
      unit2=SE_ResidueStack[SE_BondStack[b_SP].ToRes].Unit;
      pos1=SE_BondStack[b_SP].FromPos;
      pos2=SE_BondStack[b_SP].ToPos;

      unit1->Subst[pos1]=unit2;
      unit2->Subst[pos2]=unit1;
      /* anomeric - is it needed? */
      if (pos1==unit1->Shifts.Type->Anomeric)
	unit1->Position=pos2;
      if (pos2==unit2->Shifts.Type->Anomeric)
	unit2->Position=pos1;
    }

  /* Check if metabolic pathway rules match the current structure. */
  
  if(ListLen(&SE_Simulation.Rules)>0 && !RU_MatchRules(&SE_Simulation,structure))
    {
      ME_RemoveNode(&StructMethod, (struct Node *)structure);
      return(PA_ERR_OK);
    }

  state=CA_CalcAllGlycosylations(structure);

  if(state!=PA_ERR_OK)
    {
      remove=TRUE;
      
      ME_RemoveNode(&StructMethod, (struct Node *)structure);
      return(state);
    }
  
  if((SE_Simulation.Experimental->CCnt!=0||
      SE_Simulation.Experimental->HCnt!=0)&&
     (SE_Simulation.Criteria==SE_PURGE_C||
      SE_Simulation.Criteria==SE_PURGE_H||
      SE_Simulation.Criteria==SE_PURGE_TOT))
    {
      SP_Calc1DSpectra(structure);
      /* If there are more experimental shifts than simulated shifts the
	 structure cannot be correct */
      if(SE_Simulation.Experimental->CCnt > structure->CCnt ||
	 SE_Simulation.Experimental->HCnt > structure->HCnt)
	{
	  ME_RemoveNode(&StructMethod, (struct Node *)structure);
	  return(PA_ERR_OK);
	}
      SP_RawCompare(SE_Simulation.Experimental, structure, SE_Simulation.Criteria);
    }

  if((SE_Simulation.Experimental->ChCnt!=0||
     SE_Simulation.Experimental->HhCnt!=0)&&
     (SE_Simulation.Criteria==SE_PURGE_CH||
      SE_Simulation.Criteria==SE_PURGE_HH||
      SE_Simulation.Criteria==SE_PURGE_TOT))
    {
      SP_Calc2DSpectra(structure); /* Calculate CH and HH  */
      /* If there are more experimental shifts than simulated shifts the
	 structure cannot be correct */
      if(SE_Simulation.Experimental->ChCnt > structure->ChCnt ||
	 SE_Simulation.Experimental->HhCnt > structure->HhCnt)
	{
	  ME_RemoveNode(&StructMethod, (struct Node *)structure);
	  return(PA_ERR_OK);
	}
      SP_Raw2DCompare(SE_Simulation.Experimental,structure,SE_Simulation.Criteria);
    }


  /* If there are already more structures than wanted and this is worse than the worst don't add it.
ö     This means the lists won't have to be resorted. */
  if(SE_Simulation.StrActual>SE_Simulation.StrLower &&
     ((SE_Simulation.Criteria == SE_PURGE_C && structure->CFit > SE_Simulation.WorstFit) ||
      (SE_Simulation.Criteria == SE_PURGE_H && structure->HFit > SE_Simulation.WorstFit) ||
      (SE_Simulation.Criteria == SE_PURGE_CH && structure->ChFit > SE_Simulation.WorstFit) ||
      (SE_Simulation.Criteria == SE_PURGE_HH && structure->HhFit > SE_Simulation.WorstFit)))
    {
      ME_RemoveNode(&StructMethod, (struct Node *)structure);
    }
  else
    {
      SE_Simulation.StrActual++;
      if(SE_Simulation.Criteria == SE_PURGE_C && 
	 (SE_Simulation.StrActual==1 || structure->CFit > SE_Simulation.WorstFit))
	{
	  SE_Simulation.WorstFit=structure->CFit;
	}
      else if(SE_Simulation.Criteria == SE_PURGE_H && 
	      (SE_Simulation.StrActual==1 || structure->HFit > SE_Simulation.WorstFit))
	{
	  SE_Simulation.WorstFit=structure->HFit;
	}
	else if(SE_Simulation.Criteria == SE_PURGE_CH && 
		(SE_Simulation.StrActual==1 || structure->ChFit > SE_Simulation.WorstFit))
	{
	  SE_Simulation.WorstFit=structure->ChFit;	  
	}
	else if(SE_Simulation.Criteria == SE_PURGE_HH && 
		(SE_Simulation.StrActual==1 || structure->HhFit > SE_Simulation.WorstFit))
	{
	  SE_Simulation.WorstFit=structure->HhFit;	  
	}
      if (SE_Simulation.StrActual>SE_Simulation.StrUpper)
	{
	  PE_PurgeSame();
	  PE_PurgeList();
	}
    }
  return(PA_ERR_OK);
}    

int PE_Connected()
{
  int item, flag, bond, count;
  SE_ResidueStack[0].Connected=TRUE;
  count=1;
  if(SE_ResidueSP>0)
    {
      do
	{
	  flag=FALSE;
	  for (item=0; item<SE_ResidueSP; item++)
	    {
	      if (SE_ResidueStack[item].Connected)
		{
		  for (bond=0; bond<SE_BondSP; bond++)
		    {
		      if (SE_BondStack[bond].FromRes==item)
			{
			  if (SE_ResidueStack[SE_BondStack[bond].ToRes].Connected==FALSE)
			    {
			      SE_ResidueStack[SE_BondStack[bond].ToRes].Connected=TRUE;
			      flag=TRUE; count++;
			    };
			}
		      if (SE_BondStack[bond].ToRes==item)
			{
			  if (SE_ResidueStack[SE_BondStack[bond].FromRes].Connected==FALSE)
			    {
			      SE_ResidueStack[SE_BondStack[bond].FromRes].Connected=TRUE;
			      flag=TRUE; count++;
			    }
			}
		    }
		}
	    }
	} while (flag);
    }
  for (item=0; item<SE_ResidueSP; item++) SE_ResidueStack[item].Connected=FALSE;
  if (count!=SE_ResidueSP) return (FALSE);
  return(TRUE);
}


/* find next to position for substitution */
int PE_GetToPos(int *item_pointer, int *pos_pointer, int from_item)
{
  int next_item, next_pos, comparison;

  if (*item_pointer==PE_NEWPOS_NEW)	/* First position */
    {
      for (next_item=0; next_item<SE_ResidueSP; next_item++)
	{
	  for (next_pos=0;
	       next_pos<SE_ResidueStack[next_item].Residue->Base->Shifts.Type->HeavyCnt;
	       next_pos++)
	    if (next_pos!=SE_ResidueStack[next_item].Residue->Anomeric)
	      {
		if (SE_ResidueStack[next_item].Residue->Free[next_pos]==SE_FREE)
		  /* Found position */
		  {
		    SE_ResidueStack[next_item].Residue->Free[next_pos]+=SE_OCCUPIED;
		    /* Block position */
		    *item_pointer=next_item; *pos_pointer=next_pos;
		    return(PA_ERR_OK);
		  }
	      }
	}
      *item_pointer=PE_NEWPOS_FAIL;
      return(PE_NEWPOS_FAIL);
    }
  if(*item_pointer>=SE_ResidueSP)
    {
      return(PE_NEWPOS_FAIL);      
    }
  SE_ResidueStack[*item_pointer].Residue->Free[*pos_pointer]=SE_FREE;
  /* Free last pos */
  *pos_pointer=*pos_pointer+1;
  for (next_item=*item_pointer; next_item<SE_ResidueSP; next_item++, *pos_pointer=0)
    {
      for (comparison=0; comparison<next_item; comparison++)
	{
	  if(from_item!=comparison && SE_EquivalentUnits(SE_ResidueStack[next_item].UnitSpecification, SE_ResidueStack[comparison].UnitSpecification))
	    {
	      if(next_item<SE_ResidueSP-1)
		{
		  next_item++;
		  *pos_pointer=0;
		}
	      else
		{
		  *item_pointer=PE_NEWPOS_FAIL;
		  return(PE_NEWPOS_FAIL);
		}
	    }
	}
      for (next_pos=*pos_pointer;
	   next_pos<SE_ResidueStack[next_item].Residue->Base->Shifts.Type->HeavyCnt;
	   next_pos++)
	{
	  if (next_pos!=SE_ResidueStack[next_item].Residue->Anomeric)
	    if (SE_ResidueStack[next_item].Residue->Free[next_pos]==SE_FREE)
	      /* Found position */
	      {
		SE_ResidueStack[next_item].Residue->Free[next_pos]+=SE_OCCUPIED;
		/* Block position */
		*item_pointer=next_item;
		*pos_pointer=next_pos;
		return(PA_ERR_OK);
	      }
	}
    }
  *item_pointer=PE_NEWPOS_FAIL;
  return(PE_NEWPOS_FAIL);		/* No more free positions */
}

/*  find next from position for substitution */
int PE_GetFromPos(int item_pointer)
{
  int next_item;
  if (item_pointer==PE_NEWPOS_NEW)	/* First position */
    {
      for (next_item=0; next_item<SE_ResidueSP; next_item++)
	{
	  if (SE_ResidueStack[next_item].Residue->Anomeric<0)
	    {
	    }
	    /*	    return(PE_NEWPOS_FAIL); */
	  if (SE_ResidueStack[next_item].Residue->
		  Free[SE_ResidueStack[next_item].Residue->Anomeric]==SE_FREE)
	    /* Found position */
	    {
	      SE_ResidueStack[next_item].Residue->
		Free[SE_ResidueStack[next_item].Residue->Anomeric]+=SE_OCCUPIED;
	      /* Block position */
	      return(next_item);
	    }
	}
      return(PE_NEWPOS_FAIL);
    }
  SE_ResidueStack[item_pointer].Residue->
    Free[SE_ResidueStack[item_pointer].Residue->Anomeric]=SE_FREE;
  /* Free last pos */
  for (next_item=item_pointer+1; next_item<SE_ResidueSP; next_item++)
    if (SE_ResidueStack[next_item].Residue->
	Free[SE_ResidueStack[next_item].Residue->Anomeric]==SE_FREE)
      /* Found position */
      {
	SE_ResidueStack[next_item].Residue->
	  Free[SE_ResidueStack[next_item].Residue->Anomeric]+=SE_OCCUPIED;
	/* Block position */
	return(next_item);
      }
  return(PE_NEWPOS_FAIL);	/* No more free positions */
}

/* This function starts with setting all links specified by the user in the
   generate block. These links are specific from one residue to another and
   may also specify the positions.
   Since positions may be unknown all possibilities have to be created by
   calling PE_IterateLinkageListPositions, which in turn calls PE_PermBonds */
int PE_ApplyLinkages()
{
  struct SE_Linkage *link;
  int fromRes, toRes, tempBondSP, unknownPosCnt=0, unknownPosPtr[SE_MAX_RESIDUES];

  tempBondSP=SE_BondSP;

  /* Go through the list of linkages */
  for(link=(struct SE_Linkage *)SE_Simulation.Linkages.Head.Succ;
      link->Node.Succ!=NULL;
      link=(struct SE_Linkage *)link->Node.Succ)
    {
      /* Find the residue to link from */
      for(fromRes=0;fromRes<SE_ResidueSP && link->FromUnit!=SE_ResidueStack[fromRes].UnitSpecification;fromRes++)
	{
	}
      /* Find the residue to link to */
      for(toRes=0;toRes<SE_ResidueSP && link->ToUnit!=SE_ResidueStack[toRes].UnitSpecification;toRes++)
	{
	}
      if(fromRes>=SE_ResidueSP || toRes>=SE_ResidueSP)
	{
	  return(PA_ERR_FAIL);
	}

      /* If the from position is not specified use the anomeric atom */
      if(link->FromPos==TY_VOID_ATOM)
	{
	  link->FromPos=SE_ResidueStack[fromRes].Residue->Anomeric;
	}
      /* If this position was marked as free make it occupied */
      if(SE_ResidueStack[fromRes].Residue->Free[link->FromPos]==SE_FREE)
	{
	  SE_ResidueStack[fromRes].Residue->Free[link->FromPos]+=SE_OCCUPIED;
	}
      if(link->ToPos!=TY_VOID_ATOM)
	{
	  if(SE_ResidueStack[toRes].Residue->Free[link->ToPos]==SE_FREE)
	    {
	      SE_ResidueStack[toRes].Residue->Free[link->ToPos]+=SE_OCCUPIED;
	    }
	}
      else
	{
	  unknownPosPtr[unknownPosCnt++]=SE_BondSP;
	}

      SE_BondStack[SE_BondSP].FromRes=fromRes;
      SE_BondStack[SE_BondSP].FromPos=link->FromPos;
      SE_BondStack[SE_BondSP].ToRes=toRes;
      SE_BondStack[SE_BondSP].ToPos=link->ToPos;
      SE_BondSP++;
    }

  PE_IterateLinkageListPositions(0, unknownPosCnt, unknownPosPtr);

  SE_BondSP=tempBondSP;

  return(PA_ERR_OK);
}

/* This function recursively calls itself to generate all possible combinations
   of links when the user has specifically defined linkages between residues,
   but not the exact positions. The function calls PE_PermBonds to generate
   the rest of the bond permutations between all residues.
   If there are not specific linkages with unspecific positions this function
   only calls PE_PermBonds to generate all linkages. */
int PE_IterateLinkageListPositions(int ptr, int cnt, int *unknownPosPtr)
{
  int i, pos, flag;

  /* If there are linkages with unknown positions iterate through them to
     generate all possible combinations. */
  if(ptr<cnt)
    {
      for(;ptr<cnt && ptr<SE_MAX_RESIDUES; ptr++)
	{
	  pos=SE_BondStack[unknownPosPtr[ptr]].ToRes;
	  for(i=0;i<SE_ResidueStack[pos].Residue->Base->Shifts.Type->HeavyCnt;i++)
	    {
	      /* Check that this position can be free and that it is not
		 anomeric */
	      if(SE_ResidueStack[pos].Residue->Base->Shifts.Type->Atom[i].Type&TY_FREE_POS && 
		 !(SE_ResidueStack[pos].Residue->Base->Shifts.Type->Atom[i].Type&TY_ANOMERIC))
		{
		  flag=0;
		  if(SE_ResidueStack[pos].Residue->Free[i]==SE_FREE)
		    {
		      flag=1;
		      SE_ResidueStack[pos].Residue->Free[i]+=SE_OCCUPIED;
		    }
		  SE_BondStack[unknownPosPtr[ptr]].ToPos=i;
		  PE_IterateLinkageListPositions(ptr+1, cnt, unknownPosPtr);
		  if(flag==1)
		    {
		      SE_ResidueStack[pos].Residue->Free[i]-=SE_OCCUPIED;      
		    }
		}
	    }
	}
    }
  else
    {
      /* Generate all other bond permutations. This point is reached regardless
	 of whether the user has specified specific links or not. */
      PE_PermBonds(PE_START);      
    }
  return (PA_ERR_OK);
}

/* generate all bond combinations */
int PE_PermBonds(int prev_item)
{
  int From_item, To_item, To_pos, stat;

  if (prev_item==PE_START)
    {
      From_item=PE_GetFromPos(PE_NEWPOS_NEW);
    }
  else
    {
      From_item=PE_GetFromPos(prev_item);
    }

  if (From_item!=PE_NEWPOS_FAIL)	/* More from positions */
    {
      /* Loop through all to positions */
      for(To_item=PE_NEWPOS_NEW, PE_GetToPos(&To_item,&To_pos, From_item);
	  To_item!=PE_NEWPOS_FAIL; PE_GetToPos(&To_item,&To_pos, From_item))
	{
	  /* Check if this bond has already been reported as missing. Discarding this combination
	     early makes the structure generation and spectra calculations quicker. */
	  if(ListLen(&SE_Simulation.MissingBonds)>0)
	    {
	      if(!PE_CheckBond(From_item, To_item, To_pos))
		{
		  continue;
		}
	    }
	  SE_BondStack[SE_BondSP].FromRes=From_item;
	  SE_BondStack[SE_BondSP].FromPos=SE_ResidueStack[From_item].Residue->Anomeric;
	  SE_BondStack[SE_BondSP].ToRes=To_item;
	  SE_BondStack[SE_BondSP].ToPos=To_pos;
	  SE_BondSP++;
	  stat=PE_PermBonds(From_item); /* Do rest of bonds */
	  SE_BondSP--;
	  if(stat==PA_ERR_FAIL && SE_BondSP>0)
	    {
	      /* If it was this bond that failed go to the next possible combination at this
		 position */
	      if(!PE_CheckBond(SE_BondStack[SE_BondSP].FromRes,
			       SE_BondStack[SE_BondSP].ToRes,
			       SE_BondStack[SE_BondSP].ToPos))
		{
		  continue;
		}
	      /* Otherwise free the from and to positions of this bond and go back to the
		 previous bond and check if it was that one that failed */
	      else
		{
		  SE_ResidueStack[From_item].Residue->
		    Free[SE_ResidueStack[From_item].Residue->Anomeric]=SE_FREE;
		  SE_ResidueStack[To_item].Residue->
		    Free[To_pos]=SE_FREE;
		  return(PA_ERR_FAIL);
		}
	    }
	}
      SE_ResidueStack[From_item].Residue->
	Free[SE_ResidueStack[From_item].Residue->Anomeric]=SE_FREE;
      if(SE_ResidueStack[From_item].Residue->FreeCertainty==0)
	{
	  PE_PermBonds(From_item);
	}
    }
  else
    {
      if (PE_Connected())
	{
	  /*	  printf("Making structure.\n");*/
	  stat=PE_MkStruct(); /* Done all permutations */
	  return (stat);
	}
    }
  return(PA_ERR_OK);			/* Make structures and return */
}


/* generate all residue combinations */
int PE_PermResidue(struct SE_Unit *Unit)
{
  struct SE_Residue *residue;
  struct RU_Rule *rule;
  int flag;

  if (Unit->Node.Succ!=NULL)
    {
      if(Unit->UsedInStructure==0)
	{
	  Unit->UsedInStructure=1;
	  for (residue=(struct SE_Residue *)First(Unit->Residues);
	       residue->Node.Succ!=NULL;
	       residue=(struct SE_Residue *)residue->Node.Succ )
	    {
	      /* If there are rules check if they contain lists of allowed
		 residues.
		 If the current residue is not allowed do not add it to the
		 ResidueStack. */
	      if (ListLen(&SE_Simulation.Rules)>0)
		{
		  flag=1;
		  for(rule=(struct RU_Rule *)SE_Simulation.Rules.Head.Succ;
		      rule->Node.Succ!=NULL;
		      rule=(struct RU_Rule *)rule->Node.Succ)
		    {
		      if(ListLen(&rule->Residues)>0)
			{
			  if(!FindNode((struct Node *)&rule->Residues,
				       residue->Base->Node.Name))
			    {
			      /*			      printf("Skipping residue %s\n",residue->Base->Node.Name);*/
			      flag=0;
			      break;
			    }
			}
		    }
		  if(flag==0)
		    {
		      continue;
		    }
		}
		
	      if (residue->JHH!=RE_UNKNOWN)
		{
		  SE_Simulation.CntJHH[residue->JHH]++;
		}
	      if (residue->JCH!=RE_UNKNOWN)
		{
		  SE_Simulation.CntJCH[residue->JCH]++;
		}
	      flag=1;
	      
	      if(PE_Max(SE_Simulation.CntJHH[0],SE_Simulation.JHH[0])+
		 PE_Max(SE_Simulation.CntJHH[1],SE_Simulation.JHH[1])+
		 PE_Max(SE_Simulation.CntJHH[2],SE_Simulation.JHH[2])>SE_Simulation.UnitCnt)
		{
		  flag=0;
		}
	      
	      if(PE_Max(SE_Simulation.CntJCH[0],SE_Simulation.JCH[0])+
		 PE_Max(SE_Simulation.CntJCH[1],SE_Simulation.JCH[1])+
		 PE_Max(SE_Simulation.CntJCH[2],SE_Simulation.JCH[2])>SE_Simulation.UnitCnt)
		{
		  flag=0;
		}
	      
	      if (flag)
		{
		  SE_ResidueStack[SE_ResidueSP].Residue=residue;
		  SE_ResidueStack[SE_ResidueSP].UnitSpecification=Unit;
		  SE_ResidueStack[SE_ResidueSP].Connected=FALSE;
		  SE_ResidueSP++;
		  
		  PE_PermResidue((struct SE_Unit *)Unit->Node.Succ);
		  
		  SE_ResidueSP--;
		}
	      
	      if (residue->JHH!=RE_UNKNOWN)
		{
		  SE_Simulation.CntJHH[residue->JHH]--;
		}
	      if (residue->JCH!=RE_UNKNOWN)
		{
		  SE_Simulation.CntJCH[residue->JCH]--;
		}
	    }
	  Unit->UsedInStructure=0;
	}
      else
	{
	  PE_PermResidue((struct SE_Unit *)Unit->Node.Succ); 
	}
    }
  else
    {
      PE_ApplyLinkages();
    }
  return(PA_ERR_OK);
}

/* This function assembles the reducing end of e.g. n- and o-glycans so that the
   reducing end is not randomly generated and later on filtered if it does not match
   the rules. The function currently only takes the three first patterns in the
   first rule into consideration. For o-glycans the variation possibilities are so
   large that this function only builds the protein residue (if present) and the 
   first GalNAc. */
int PE_StaticReducingEnd()
{
  struct SE_Unit *unit;
  struct SE_Residue *residue;
  struct RU_Rule *rule;
  struct RU_Pattern *pattern;
  char currentRegexp[256], residueName[NODE_NAME_LENGTH], linkages[6];
  int i, j, found, unknownConfig[8], nUnknownConfig=0, flag=0, linkToPos;
  
  /* If there are no rules create all permutations and return OK */
  if(ListLen(&SE_Simulation.Rules)<=0)
    {
      PE_PermResidue((struct SE_Unit *)First(SE_Simulation.Units));
      return(PA_ERR_OK);
    }
  rule=(struct RU_Rule *)First(SE_Simulation.Rules);
  /* Loop through patterns, but no more than 3. */
  for(pattern=(struct RU_Pattern *)rule->Patterns.Head.Succ, i=0;
      pattern->Node.Succ!=NULL && i<3; 
      pattern=(struct RU_Pattern *)pattern->Node.Succ, i++)
    {
      strcpy(currentRegexp,pattern->regexp);
      residueName[0]=0;
      while(RU_NextResidueFromRedEnd(currentRegexp, residueName, linkages))
	{
	  for (unit=(struct SE_Unit *)First(SE_Simulation.Units);
	       unit->Node.Succ!=NULL;
	       unit=(struct SE_Unit *)unit->Node.Succ)
	    {
	      if(unit->UsedInStructure)
		{
		  continue;
		}
	      for (residue=(struct SE_Residue *)First(unit->Residues);
		   residue->Node.Succ!=NULL;
		   residue=(struct SE_Residue *)residue->Node.Succ)
		{
		  flag=0;
		  found=0;
		  if(residueName[0]=='.')
		    {
		      residueName[0]='a';
		      unknownConfig[nUnknownConfig++]=SE_ResidueSP;
		    }
		  if(strcmp(residueName,residue->Base->Node.Name)==0)
		    {
		      for(j=0;j<strlen(linkages);j++)
			{
			  if(residue->Free[(int)(linkages[j]-'1')]==SE_FREE)
			    {
			      found=1;
			    }
			  else
			    {
			      found=0;
			      j=strlen(linkages);
			    }
			}
		      if(found==1)
			{
			  if (residue->JHH!=RE_UNKNOWN)
			    {
			      SE_Simulation.CntJHH[residue->JHH]++;
			    }
			  if (residue->JCH!=RE_UNKNOWN)
			    {
			      SE_Simulation.CntJCH[residue->JCH]++;
			    }
			  flag=1;
			  
			  if(PE_Max(SE_Simulation.CntJHH[0],SE_Simulation.JHH[0])+
			     PE_Max(SE_Simulation.CntJHH[1],SE_Simulation.JHH[1])+
			     PE_Max(SE_Simulation.CntJHH[2],SE_Simulation.JHH[2])>SE_Simulation.UnitCnt)
			    {
			      flag=0;
			    }
			  
			  if(PE_Max(SE_Simulation.CntJCH[0],SE_Simulation.JCH[0])+
			     PE_Max(SE_Simulation.CntJCH[1],SE_Simulation.JCH[1])+
			     PE_Max(SE_Simulation.CntJCH[2],SE_Simulation.JCH[2])>SE_Simulation.UnitCnt)
			    {
			      flag=0;
			    }
			  
			  if (flag)
			    {
			      unit->UsedInStructure=1;
			      SE_ResidueStack[SE_ResidueSP].Residue=residue;
			      SE_ResidueStack[SE_ResidueSP].UnitSpecification=unit;
			      SE_ResidueStack[SE_ResidueSP].Connected=FALSE;
			      
			      if(SE_ResidueSP>0)
				{
				  SE_BondStack[SE_BondSP].FromRes=SE_ResidueSP;
				  SE_BondStack[SE_BondSP].FromPos=SE_ResidueStack[SE_ResidueSP].Residue->Anomeric;
				  SE_BondStack[SE_BondSP].ToRes=SE_ResidueSP-1;
				  SE_BondStack[SE_BondSP].ToPos=linkToPos;
				  
				  SE_ResidueStack[SE_ResidueSP].Residue->
				    Free[SE_ResidueStack[SE_ResidueSP].Residue->Anomeric]+=SE_OCCUPIED;
				  SE_ResidueStack[SE_ResidueSP-1].Residue->
				    Free[linkToPos]+=SE_OCCUPIED;

				  SE_BondSP++;
				}
			      SE_ResidueSP++;
			    }
			  else
			    {
			      if (residue->JHH!=RE_UNKNOWN)
				{
				  SE_Simulation.CntJHH[residue->JHH]--;
				}
			      if (residue->JCH!=RE_UNKNOWN)
				{
				  SE_Simulation.CntJCH[residue->JCH]--;
				}
			    }
			}
		    }
		  if(flag)
		    {
		      break;
		    }
		}
	      if(flag)
		{
		  break;
		}
	    }
	  if(!flag)
	    {
	      SE_ResidueSP=0;
	      SE_BondSP=0;
	      break;
	    }
	  linkToPos=(int)(linkages[strlen(linkages)-1]-'1');
	}
      if(flag)
	{
	  break;
	}
    }
  PE_PermResidue((struct SE_Unit *)First(SE_Simulation.Units));
  for(i=0;i<nUnknownConfig;i++)
    {
      PE_PermUnknownConfigs(unknownConfig, nUnknownConfig, i);
    }
  return(PA_ERR_OK);
}

void PE_PermUnknownConfigs(int *array, int n, int ptr)
{
  struct SE_Unit *unit;
  struct SE_Residue *oldResidue, *newResidue;
  char  residueName[NODE_NAME_LENGTH];
  int i, flag, found;

  unit=SE_ResidueStack[array[ptr]].UnitSpecification;
  oldResidue=SE_ResidueStack[array[ptr]].Residue;
  strcpy(residueName,oldResidue->Base->Node.Name);
  residueName[0]='b';
  
  for (newResidue=(struct SE_Residue *)First(unit->Residues);
       newResidue->Node.Succ!=NULL;
       newResidue=(struct SE_Residue *)newResidue->Node.Succ)
    {
      found=0;

      if(strcmp(residueName,newResidue->Base->Node.Name)==0 &&
	 unit->UsedInStructure==0)
	{
	  if (oldResidue->JHH!=RE_UNKNOWN)
	    {
	      SE_Simulation.CntJHH[oldResidue->JHH]--;
	    }
	  if (oldResidue->JCH!=RE_UNKNOWN)
	    {
	      SE_Simulation.CntJCH[oldResidue->JCH]--;
	    }

	  if (newResidue->JHH!=RE_UNKNOWN)
	    {
	      SE_Simulation.CntJHH[newResidue->JHH]++;
	    }
	  if (newResidue->JCH!=RE_UNKNOWN)
	    {
	      SE_Simulation.CntJCH[newResidue->JCH]++;
	    }
	  flag=1;

	  if(PE_Max(SE_Simulation.CntJHH[0],SE_Simulation.JHH[0])+
	     PE_Max(SE_Simulation.CntJHH[1],SE_Simulation.JHH[1])+
	     PE_Max(SE_Simulation.CntJHH[2],SE_Simulation.JHH[2])>SE_Simulation.UnitCnt)
	    {
	      flag=0;
	    }
	  
	  if(PE_Max(SE_Simulation.CntJCH[0],SE_Simulation.JCH[0])+
	     PE_Max(SE_Simulation.CntJCH[1],SE_Simulation.JCH[1])+
	     PE_Max(SE_Simulation.CntJCH[2],SE_Simulation.JCH[2])>SE_Simulation.UnitCnt)
	    {
	      flag=0;
	    }
	  
	  if (flag)
	    {
	      SE_ResidueStack[array[ptr]].Residue=newResidue;
	      memcpy(newResidue->Free,oldResidue->Free,TY_MAX_BONDS*sizeof(int));

	      PE_PermResidue((struct SE_Unit *)First(SE_Simulation.Units));

	      for(i=ptr+1;i<n;i++)
		{
		  PE_PermUnknownConfigs(array, n, i);
		}

	      if (newResidue->JHH!=RE_UNKNOWN)
		{
		  SE_Simulation.CntJHH[newResidue->JHH]--;
		}
	      if (newResidue->JCH!=RE_UNKNOWN)
		{
		  SE_Simulation.CntJCH[newResidue->JCH]--;
		}
	      if (oldResidue->JHH!=RE_UNKNOWN)
		{
		  SE_Simulation.CntJHH[oldResidue->JHH]++;
		}
	      if (oldResidue->JCH!=RE_UNKNOWN)
		{
		  SE_Simulation.CntJCH[oldResidue->JCH]++;
		}
	    }
	}
    }
}

/* Generates permutations of items in include list */
/* The structures are called <name>1 etc... */
int PE_Generate()
{
  struct BU_Struct *structure;
  int new_number, i;
  char tempAssMode;
  float bestFit=9999;

  tempAssMode=SP_AssignMode;

  SP_AssignMode=SP_AM_CUTOFF;

  SE_ResidueSP=0;				/* Clear stack */

  PE_StaticReducingEnd(); /* Start checking if there is a recurring reducing end.
			     This function then calls PermResidue to generate the
			     residue permutations. */

  /*  PE_PermResidue((struct SE_Unit *)First(SE_Simulation.Units));*/

  SP_AssignMode=tempAssMode;

  if(ListLen(&StructList)==0)
    {
      return(PA_ERR_OK);
    }

  PE_PurgeSame();
  PE_PurgeList();
  new_number=1;
  for (structure=(struct BU_Struct *)StructList.Head.Succ;
       structure->Node.Succ!=NULL;
       structure=(struct BU_Struct *)structure->Node.Succ)
    {
      /* rename units */
      GFX_RenameUnits(structure);
      /* rename simulated structures */
      if ( (strncmp(structure->Node.Name,
		    SE_Simulation.Name,strlen(SE_Simulation.Name))==0)&&
	   (structure!=SE_Simulation.Experimental) )
	sprintf(structure->Node.Name,"%.16s%d", SE_Simulation.Name, 
		new_number++);

      if(structure!=SE_Simulation.Experimental)
	{
	  if(structure->CCnt==0 ||
	     structure->HCnt==0)
	    {
	      SP_Calc1DSpectra(structure);
	    }
	  if(structure->ChCnt==0 ||
	     structure->HhCnt==0)
	    {
	      SP_Calc2DSpectra(structure);
	    }

	  for(i=0;i<BU_MAX_SHIFTS; i++)
	    {
	      structure->CAssignments[0][i]=0;
	      structure->CAssignments[1][i]=0;
	      structure->HAssignments[0][i]=0;
	      structure->HAssignments[1][i]=0;
	    }
	  structure->nCAssignments=0;
	  structure->nHAssignments=0;

	  structure->nUnassigned=0;
	  
	  SP_Raw2DCompare(SE_Simulation.Experimental,structure,0);
	  SP_RawCompare(SE_Simulation.Experimental,structure,0);
	  
	  
	  i=0;
	  if(SE_Simulation.Experimental->CCnt>0)
	    {
	      i+=structure->CCnt;
	    }
	  if(SE_Simulation.Experimental->HCnt>0)
	    {
	      i+=structure->HCnt;
	    }
	  if(SE_Simulation.Experimental->ChCnt>0)
	    {
	      i+=structure->ChCnt;
	    }
	  if(SE_Simulation.Experimental->HhCnt>0)
	    {
	      i+=structure->HhCnt;
	    }
	  structure->fractionAssigned=(float)1-(float)structure->nUnassigned/i;
	  structure->TotFit=structure->TotFit/structure->fractionAssigned;
	  if(structure->TotFit<bestFit)
	    {
	      bestFit=structure->TotFit;
	    }
	}
    }
  /* Repurge the list since a different accuracy level has been used */
  PE_PurgeList();
  new_number=1;
  for (structure=(struct BU_Struct *)StructList.Head.Succ;
       structure->Node.Succ!=NULL;
       structure=(struct BU_Struct *)structure->Node.Succ)
    {
      /* rename simulated structures */
      if ( (strncmp(structure->Node.Name,
		    SE_Simulation.Name,strlen(SE_Simulation.Name))==0)&&
	   (structure!=SE_Simulation.Experimental) )
	sprintf(structure->Node.Name,"%.16s%d", SE_Simulation.Name, 
		new_number++);
      structure->TotFit=structure->TotFit/bestFit;
    }

  return(PA_ERR_OK); 
}

/* SYNTAX: purgelist <lower> <upper> <type> */
int PE_RePurge()
{
  struct BU_Struct *structure;
  int new_number;

  if(SE_Purge()==PA_ERR_OK)
    {
      PE_PurgeList();
      new_number=1;
      for (structure=(struct BU_Struct *)StructList.Head.Succ;
	   structure->Node.Succ!=NULL;
	   structure=(struct BU_Struct *)structure->Node.Succ)
	{
	  /* rename units */
	  GFX_RenameUnits(structure);
	  /* rename simulated structures */
	  if ( (strncmp(structure->Node.Name,
			SE_Simulation.Name,strlen(SE_Simulation.Name))==0)&&
	       (structure!=SE_Simulation.Experimental) )
	    sprintf(structure->Node.Name,"%.16s%d", SE_Simulation.Name, 
		    new_number++);
	}
      return(PA_ERR_OK);
    }
  else
    Error(PA_ERR_WARN,"Invalid purge settings");
}

int PE_Max(int a, int b)
{
  if (b>a)
    return (b);
  else
    return (a);
}

int PE_CheckBond(int From_item, int To_item, int To_pos)
{
  struct SE_MissingBond *bond;

  for(bond=(struct SE_MissingBond *)SE_Simulation.MissingBonds.Head.Succ;
      bond->Node.Succ!=NULL; bond=(struct SE_MissingBond *)bond->Node.Succ)
    {
      /* Check if this bond is specified as missing. The From position does not need to be
	 checked since the anomeric position is always the same if the residue is the same. */
      if(SE_ResidueStack[From_item].Residue->Base==bond->dimer.Residue[0] &&
	 SE_ResidueStack[To_item].Residue->Base==bond->dimer.Residue[1] &&
	 To_pos==bond->dimer.Atom[1])
	{
	  /*	  printf("Removing %s(->%d)%s\n",SE_ResidueStack[From_item].Residue->Base->Node.Name,To_pos+1,SE_ResidueStack[To_item].Residue->Base->Node.Name);*/
	  return 0;
	}
    }
  return 1;
}
