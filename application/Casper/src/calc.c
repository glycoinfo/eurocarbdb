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

/* Filename = calc.c */
/* Prefix = CA */
/* Calculates spectra using 1) Disaccharide corrections (Delta) */
/*                          2) Trisaccharide corrections (DeltaDelta) */
/* CA_Mode - defines level of accuracy in calculations: */
/*         - CA_DISACCH		No branchpoint corrections */
/*         - CA_TRISACCH	Branchpoint corections */
/*         - CA_DEFAULT         Defaults enabled */
/*         - CA_DOUBLE          'Double'-defaulting enabled */
/*         - CA_VERBOSE		Print all bonds and delta-sets */

#include "parser.h"
#include "delta.h"
#include "ddelta.h"
#include "build.h"
#include "calc.h"
#include "setup.h"

/* should be a 'variable' */
unsigned char CA_Mode=CA_DISACCH|CA_TRISACCH|CA_DEFAULT|CA_DOUBLE/*|CA_VERBOSE*/;

int CA_SimBranch(struct BU_Unit *lo, int cent_lo, struct BU_Unit *hi,
		 int cent_hi, struct BU_Unit *cent)
{
  char *config;
  int index1, index2, index3, index4;
  struct DD_Delta *ddelta, *temp;
  int lo_pos, hi_pos;
  float error, t_error=0;
  struct RE_Default *def;

  /* 'default' arrangement */
  lo_pos=BU_ANOMER_POS(lo);
  hi_pos=BU_ANOMER_POS(hi);

  /* cent-lo is anomeric */
  if (cent_lo==BU_ANOMER_POS(cent))
    {
      lo_pos=cent->Position;
    }

  /* cent-hi is anomeric */
  if (cent_hi==BU_ANOMER_POS(cent))
    {
      hi_pos=cent->Position;
    }

  /* Find out what type of structure this is */
  /* 1) chirality */
  if (cent->Residue->Config==lo->Residue->Config)
    {
      if (cent->Residue->Config==hi->Residue->Config)
	{
	  config=DD_SAME_SAME;
	}
      else 
	{
	  config=DD_SAME_DIFF;
	}
    }
  else
    {
      if (cent->Residue->Config==hi->Residue->Config)
	{
	  config=DD_DIFF_SAME;
	}
      else
	{
	  config=DD_DIFF_DIFF;
	}
    }

  error=DD_NO_CORRECT_ERR;

  /* 2) anomeric configs */
  if(lo_pos<0 || hi_pos<0 || cent_lo<0 || cent_hi<0)
    {
      Error(PA_ERR_FAIL,"Illegal position");
    }
  if (lo->Residue->HvyConf[lo_pos]==RE_AXIAL)
    {
      index1=DD_ANOMER_AX;
    }
  else
    {
      index1=DD_ANOMER_EQ;
    }

  if (hi->Residue->HvyConf[hi_pos]==RE_AXIAL)
    {
      index2=DD_ANOMER_AX;
    }
  else
    {
      index2=DD_ANOMER_EQ;
    }

  if (cent->Residue->HvyConf[cent_lo]==RE_AXIAL)
    {
      index3=DD_AXIAL;
    }
  else
    {
      index3=DD_EQUATORIAL;
    }

  if (cent->Residue->HvyConf[cent_hi]==RE_AXIAL)
    {
      index4=DD_AXIAL;
    }
  else
    {
      index4=DD_EQUATORIAL;
    }

  /* Now find it */
  ddelta=NULL;
  temp=(struct DD_Delta *)FindNode((struct Node *)&DD_Branch[index1][index2][index3][index4], config);
  /* No correction set found */
  if (temp==NULL)
    {
      lo->Error+=DD_NO_CORRECT_ERR;
      hi->Error+=DD_NO_CORRECT_ERR;
      cent->Error+=DD_NO_CORRECT_ERR;
      if (CA_Mode&CA_VERBOSE)
	{
	  printf("Not found\test. error=%.2f\n", error);
	}
      return(PA_ERR_OK);
    }

  /* see if there is a match somewhere */
  for (; temp!=NULL; temp=(struct DD_Delta *)FindNode((struct Node *)temp,
						      config) )
    {
      if (!( (lo_pos==temp->SubstPos[DD_FIRST])&&
	     (hi_pos==temp->SubstPos[DD_SECOND])&&
	     (cent_lo==temp->CentralPos[DD_FIRST])&&
	     (cent_hi==temp->CentralPos[DD_SECOND]) ))
	{
	  continue;
	}
      t_error=temp->Error;
      if (temp->Residue[DD_FIRST]!=lo->Residue)
	{
	  def=FindNode(&lo->Residue->Defaults[lo_pos].Head,
		       temp->Residue[DD_FIRST]->Node.Name);
	  if (def==NULL)
	    {
	      continue;
	    }
	  else
	    {
	      t_error+=def->Error/2;
	    }
	}
      if (temp->Residue[DD_SECOND]!=hi->Residue)
	{
	  def=FindNode(&hi->Residue->Defaults[hi_pos].Head,
		       temp->Residue[DD_SECOND]->Node.Name);
	  if (def==NULL)
	    {
	      continue;
	    }
	  else
	    {
	      t_error+=def->Error/2;
	    }
	}
      if (temp->Residue[DD_CENTRAL]!=cent->Residue)
	{
	  def=FindNode(&cent->Residue->Defaults[cent_lo].Head,
		       temp->Residue[DD_CENTRAL]->Node.Name);
	  if (def==NULL)
	    {
	      continue;
	    }
	  else
	    {
	      t_error+=def->Error/2;
	    }
	  def=FindNode(&cent->Residue->Defaults[cent_hi].Head,
		       temp->Residue[DD_CENTRAL]->Node.Name);
	  if (def==NULL)
	    {
	      continue;
	    }
	  else
	    {
	      t_error+=def->Error/2;
	    }
	}

      if (t_error < error)
	{
	  ddelta=temp;
	  error=t_error;
	}
    }
  /* Look for the enantiomer of the trisaccharide instead */
  if(ddelta==NULL)
    {
      temp=(struct DD_Delta *)FindNode((struct Node *)&DD_Branch[index1][index2][index3][index4], config);
      for (; temp!=NULL; temp=(struct DD_Delta *)FindNode((struct Node *)temp,
							  config) )
	{
	  if (!( (lo_pos==temp->SubstPos[DD_FIRST])&&
		 (hi_pos==temp->SubstPos[DD_SECOND])&&
		 (cent_lo==temp->CentralPos[DD_FIRST])&&
		 (cent_hi==temp->CentralPos[DD_SECOND]) ))
	    {
	      continue;
	    }
	  t_error=temp->Error;
	  if (lo->Residue->Enantiomer && 
	      temp->Residue[DD_FIRST]!=lo->Residue->Enantiomer)
	    {
	      def=FindNode(&lo->Residue->Enantiomer->Defaults[lo_pos].Head,
			   temp->Residue[DD_FIRST]->Node.Name);
	      if (def==NULL)
		{
		  continue;
		}
	      else
		{
		  t_error+=def->Error/2;
		}
	    }
	  if (hi->Residue->Enantiomer && 
	      temp->Residue[DD_SECOND]!=hi->Residue->Enantiomer)
	    {
	      def=FindNode(&hi->Residue->Enantiomer->Defaults[hi_pos].Head,
			   temp->Residue[DD_SECOND]->Node.Name);
	      if (def==NULL)
		{
		  continue;
		}
	      else
		{
		  t_error+=def->Error/2;
		}
	    }
	  if (cent->Residue->Enantiomer && 
	      temp->Residue[DD_CENTRAL]!=cent->Residue->Enantiomer)
	    {
	      def=FindNode(&cent->Residue->Enantiomer->Defaults[cent_lo].Head,
			   temp->Residue[DD_CENTRAL]->Node.Name);
	      if (def==NULL)
		{
		  continue;
		}
	      else
		{
		  t_error+=def->Error/2;
		}
	      def=FindNode(&cent->Residue->Enantiomer->Defaults[cent_hi].Head,
			   temp->Residue[DD_CENTRAL]->Node.Name);
	      if (def==NULL)
		{
		  continue;
		}
	      else
		{
		  t_error+=def->Error/2;
		}
	    }

	  if (t_error < error)
	    {
	      ddelta=temp;
	      error=t_error;
	    }
	}
    }

  if (ddelta==NULL)
    {
      lo->Error+=DD_NO_CORRECT_ERR;
      hi->Error+=DD_NO_CORRECT_ERR;
      cent->Error+=DD_NO_CORRECT_ERR;
      if (CA_Mode&CA_VERBOSE)
	{
	  if (t_error>error)
	    {
	      printf("Error too large - not used\test. error=%.2f, used %.2f\n", t_error, error);
	    }
	  else
	    {
	      printf("Not found\test. error=%.2f\n", error);
	    }
	}
      return(PA_ERR_OK);
    }

  t_error=error;

  /* do corrections */
  SH_ADD(&(ddelta->Shifts[DD_FIRST]), &(lo->Shifts));
  SH_ADD(&(ddelta->Shifts[DD_SECOND]), &(hi->Shifts));
  SH_ADD(&(ddelta->Shifts[DD_CENTRAL]), &(cent->Shifts));
  if (CA_Mode&CA_VERBOSE)
    {
      if (ddelta->CentralPos[DD_FIRST]==ddelta->Shifts[DD_CENTRAL].Type->Anomeric)
	{
	  printf("%s(%s->%s)%s(%s->%s)%s",
		 ddelta->Residue[DD_SECOND]->Node.Name,
		 ddelta->Shifts[DD_SECOND].Type->Atom[ddelta->SubstPos[DD_SECOND]].Label,
		 ddelta->Shifts[DD_CENTRAL].Type->Atom[ddelta->CentralPos[DD_SECOND]].Label,
		 ddelta->Residue[DD_CENTRAL]->Node.Name,
		 ddelta->Shifts[DD_CENTRAL].Type->Atom[ddelta->CentralPos[DD_FIRST]].Label,
		 ddelta->Shifts[DD_FIRST].Type->Atom[ddelta->SubstPos[DD_FIRST]].Label,
		 ddelta->Residue[DD_FIRST]->Node.Name);
	}
      else if (ddelta->CentralPos[DD_SECOND]==ddelta->Shifts[DD_CENTRAL].Type->Anomeric)
	{
	  printf("%s(%s->%s)%s(%s->%s)%s",
		 ddelta->Residue[DD_SECOND]->Node.Name,
		 ddelta->Shifts[DD_SECOND].Type->Atom[ddelta->SubstPos[DD_SECOND]].Label,
		 ddelta->Shifts[DD_CENTRAL].Type->Atom[ddelta->CentralPos[DD_SECOND]].Label,
		 ddelta->Residue[DD_CENTRAL]->Node.Name,
		 ddelta->Shifts[DD_CENTRAL].Type->Atom[ddelta->CentralPos[DD_FIRST]].Label,
		 ddelta->Shifts[DD_FIRST].Type->Atom[ddelta->SubstPos[DD_FIRST]].Label,
		 ddelta->Residue[DD_FIRST]->Node.Name);
	}
      else
	{
	  printf("%s(%s->%s)[%s(%s->%s)]%s",
		 ddelta->Residue[DD_FIRST]->Node.Name,
		 ddelta->Shifts[DD_FIRST].Type->Atom[ddelta->SubstPos[DD_FIRST]].Label,
		 ddelta->Shifts[DD_CENTRAL].Type->Atom[ddelta->CentralPos[DD_FIRST]].Label,
		 ddelta->Residue[DD_SECOND]->Node.Name,
		 ddelta->Shifts[DD_SECOND].Type->Atom[ddelta->SubstPos[DD_SECOND]].Label,
		 ddelta->Shifts[DD_CENTRAL].Type->Atom[ddelta->CentralPos[DD_SECOND]].Label,
		 ddelta->Residue[DD_CENTRAL]->Node.Name);
	}
      if (t_error>0.005) printf("\test. error ~%0.2f", t_error);
      printf("\n");
    }
  lo->Error+=/*ddelta->Error*/t_error;
  hi->Error+=/*ddelta->Error*/t_error;
  cent->Error+=/*ddelta->Error*/t_error;
  return(PA_ERR_OK);
}


/* Applies disaccharide corrections to a pair of sugars */
/* and trisaccharide/branchpoint corrections if applicable */
/* Normally 1 is the nonreducing end */
int CA_SimDisacch(struct BU_Unit *unit1, char pos1,
		  char pos2, struct BU_Unit *unit2)
{
  struct DE_Delta *delta;
  int anomerPos1, anomerPos2;

  delta=(struct DE_Delta *)DE_FindDelta(unit1->Residue, pos1, pos2, unit2->Residue);
  if (delta==NULL) 
    {
      if (CA_Mode&CA_VERBOSE) 
	{
	  printf(" - can't simulate\n");
	}
      Error(PA_ERR_FAIL,"Can't simulate linkage");
    }
  unit1->Error+=delta->Error;
  unit2->Error+=delta->Error;

  anomerPos1=BU_ANOMER_POS(unit1);
  if(anomerPos1==-1)
    {
      Error(PA_ERR_FAIL,"Linkage not possible cannot find anomeric position");
    }
  anomerPos2=BU_ANOMER_POS(unit2);

  /* Now recalculate shifts */
  SH_RAW_ADD(&(delta->Shifts[DE_NONRED]), &(unit1->Shifts));
  SH_RAW_ADD(&(delta->Shifts[DE_REDUCING]), &(unit2->Shifts));

  if (! (CA_Mode&CA_TRISACCH) )
    {
      return(PA_ERR_OK);
    }

  /* Now do trimer corrections */
  /* is non reducing '1,2' substituted ? */
  if (pos1==anomerPos1)
    {
      if (pos1>0)
	{
	  if (unit1->Subst[pos1-1]!=NULL)
	    {
	      if (CA_Mode&CA_VERBOSE)
		{
		  /* might need to be fixed */
		  printf("Vicinal correction for %s(%s->%s)%s(%s->%s)%s\n",
			 unit1->Subst[(int)pos1-1]->Residue->Node.Name,
			 unit1->Subst[(int)pos1-1]->Shifts.Type->Atom[unit1->Subst[(int)pos1-1]->Shifts.Type->Anomeric].Label,
			 unit1->Shifts.Type->Atom[(int)pos1-1].Label,
			 unit1->Residue->Node.Name,
			 unit1->Shifts.Type->Atom[(int)pos1].Label,
			 unit2->Shifts.Type->Atom[unit1->Position].Label,
			 unit2->Residue->Node.Name);
		}
	      /* pattern is lo, cent_lo, hi, cent_hi, center */
	      CA_SimBranch(unit1->Subst[(int)pos1-1], pos1-1, unit2, pos1, unit1);
	    }
	}
      if (pos1<unit1->Shifts.Type->HeavyCnt)
	{
	  if (unit1->Subst[pos1+1]!=NULL)
	    {
	      if (CA_Mode&CA_VERBOSE)
		{
		  printf("Vicinal correction for %s(%s->%s)%s(%s->%s)%s\t",
			 unit1->Subst[(int)pos1+1]->Residue->Node.Name,
			 unit1->Subst[(int)pos1+1]->Shifts.Type->Atom[BU_ANOMER_POS(unit1->Subst[(int)pos1+1])].Label,
			 unit1->Shifts.Type->Atom[(int)pos1+1].Label,
			 unit1->Residue->Node.Name,
			 unit1->Shifts.Type->Atom[(int)pos1].Label,
			 unit2->Shifts.Type->Atom[unit1->Position].Label,
			 unit2->Residue->Node.Name);
		}
	      /* pattern is lo, cent_lo, hi, cent_hi, center */
	      CA_SimBranch(unit2, pos1, unit1->Subst[(int)pos1+1], pos1+1, unit1);
	    }
	}
    }

  /*
    if ( (unit2->Subst[unit2->Shifts.Type->Anomeric]!=NULL) &&
    (pos2==unit2->Shifts.Type->Anomeric+1))
    {
    if (CA_Mode&CA_VERBOSE)
    printf("Seaching for vicinal correction A");
    CA_SimBranch(unit2->Subst[unit2->Shifts.Type->Anomeric],
    unit2->Shifts.Type->Anomeric,
    unit1, pos1, unit2);
    };
    if ( (unit2->Subst[unit2->Shifts.Type->Anomeric]!=NULL) &&
    (pos2==unit2->Shifts.Type->Anomeric-1))
    {
    if (CA_Mode&CA_VERBOSE)
    printf("Seaching for vicinal correction B");
    CA_SimBranch(unit1, pos1,
    unit2->Subst[unit2->Shifts.Type->Anomeric],
    unit2->Shifts.Type->Anomeric, unit2);
    };
  */

  /* pattern is lo=red hi=subst[1] center=non */

  /* vicinal to lower pos in reducing ? */
  if (pos2!=0)
    {
      if (unit2->Subst[(int)pos2-1]!=NULL && pos2-1!=BU_ANOMER_POS(unit2))
	{
	  if (CA_Mode&CA_VERBOSE)
	    {
	      printf("Branch point correction for %s(%s->%s)[%s(%s->%s)]%s\t",
		     unit2->Subst[(int)pos2-1]->Residue->Node.Name,
		     unit2->Subst[(int)pos2-1]->Shifts.Type->Atom[BU_ANOMER_POS(unit2->Subst[(int)pos2-1])].Label,
		     unit2->Shifts.Type->Atom[(int)pos2-1].Label,
		     unit2->Subst[(int)pos2]->Residue->Node.Name,
		     unit2->Subst[(int)pos2]->Shifts.Type->Atom[BU_ANOMER_POS(unit2->Subst[(int)pos2])].Label,
		     unit2->Shifts.Type->Atom[(int)pos2].Label,
		     unit2->Residue->Node.Name);
	    }
	  /* pattern is lo=rsubst[pos-1] hi=rsubst[pos] center=red */
	  CA_SimBranch(unit2->Subst[(int)pos2-1],pos2-1,unit2->Subst[(int)pos2],pos2,unit2);
	}
    }
  /* vicinal to higher pos in reducing ? */
  /* This is already covered by the above branch point. This will create
     duplicate corrections. */
  /*  if (pos2<unit2->Shifts.Type->HeavyCnt)
    {
      if (unit2->Subst[(int)pos2+1]!=NULL)
	{
	  if (CA_Mode&CA_VERBOSE)
	    {
	      printf("Branch point correction for Z %s(%s->%s)[%s(%s->%s)]%s\t",
		     unit2->Subst[(int)pos2]->Residue->Node.Name,
		     unit2->Subst[(int)pos2]->Shifts.Type->Atom[BU_ANOMER_POS(unit2->Subst[(int)pos2])].Label,
		     unit2->Shifts.Type->Atom[(int)pos2].Label,
		     unit2->Subst[(int)pos2+1]->Residue->Node.Name,
		     unit2->Subst[(int)pos2+1]->Shifts.Type->Atom[BU_ANOMER_POS(unit2->Subst[(int)pos2+1])].Label,
		     unit2->Shifts.Type->Atom[(int)pos2+1].Label,
		     unit2->Residue->Node.Name);
	    };
  *//* pattern is lo=rsubst[pos] hi=rsubst[pos+1] center=red *//*
	  CA_SimBranch(unit2->Subst[(int)pos2],pos2,unit2->Subst[(int)pos2+1],pos2+1,unit2);
	  };
	  };*/
  return(PA_ERR_OK);
}    

short CA_CalcAllGlycosylations(struct BU_Struct *str)
{
  struct BU_Unit *unit, *searchUnit;
  struct SE_MissingBond *bond;
  char name[NODE_NAME_LENGTH];
  int anomerPos;

  /* First reset all shifts */
  for(unit=(struct BU_Unit *)str->Units.Head.Succ;unit->Node.Succ!=NULL;
      unit=(struct BU_Unit *)unit->Node.Succ)
    {
        SH_RAW_COPY(&(unit->Residue->Shifts), &(unit->Shifts));
    }
  /* Apply all corrections */
  for(unit=(struct BU_Unit *)str->Units.Head.Succ; unit->Node.Succ!=NULL;
      unit=(struct BU_Unit *)unit->Node.Succ)
    {
      anomerPos=BU_ANOMER_POS(unit);
      if(anomerPos!=-1 && unit->Subst[anomerPos] && unit->Position!=TY_VOID_ATOM)
	{
	  if(unit!=(struct BU_Unit *)str->Units.Head.Succ && unit->Position==BU_ANOMER_POS(unit->Subst[anomerPos]))
	    {
	      for(searchUnit=(struct BU_Unit *)unit->Node.Pred; searchUnit->Node.Pred!=NULL && 
		    searchUnit!=unit->Subst[anomerPos]; searchUnit=(struct BU_Unit *)searchUnit->Node.Pred)
		{
		}
	      if(searchUnit->Node.Pred!=NULL)
		{
		  continue;
		}
	    }
	  if(CA_SimDisacch(unit, anomerPos,
			   unit->Position, unit->Subst[anomerPos])!=PA_ERR_OK)
	    {
	      sprintf(name,"%s(%s->%s)%s", unit->Residue->Node.Name,
		      unit->Shifts.Type->Atom[anomerPos].Label,
		      unit->Subst[anomerPos]->Shifts.Type->Atom[unit->Position].Label,
		      unit->Subst[anomerPos]->Residue->Node.Name);
	      fprintf(PA_Status->Error,"No data for %s\n",name);

	      if(SE_Simulation.MissingBonds.Head.Succ!=0 &&
		 SE_Simulation.MissingBonds.Tail.Pred!=0)
		{
		  /* Add this bond to list of missing bonds to be able to early
		     discard structures containing this linkage - and also not
		     print the missing bond info more than once per linkage. */
		  bond=(struct SE_MissingBond *)
		    ME_CreateNode(&SE_MissingBondMethod,
				  &(SE_Simulation.MissingBonds), name);
		  bond->dimer.Residue[0]=unit->Residue;
		  bond->dimer.Residue[1]=unit->Subst[anomerPos]->Residue;
		  bond->dimer.Atom[0]=anomerPos;
		  bond->dimer.Atom[1]=unit->Position;
		}

	      Error(PA_ERR_FAIL, "No data for at least one bond when creating structure");
	    }
	}
    }
  return (PA_ERR_OK);
}
