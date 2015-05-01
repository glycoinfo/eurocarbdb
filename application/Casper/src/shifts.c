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

/* Filename = shifts.c */
/* Prefix = SH */

#include "shifts.h"
#include "build.h"
#include "residue.h"
#include "delta.h"
#include "methods.h"
#include "parser.h"


/* Printneg is a flag to print negative shifts.
   Used for printing di- and trisaccharide
   shift changes. */
void SH_PrintShifts(struct SH_Shifts *shifts, short printneg)
{
  int i, j;
  /* Atom labels */
  for (i=0; i<shifts->Type->HeavyCnt; i++)
    {
      printf("%7s", shifts->Type->Atom[i].Label);
      for (j=1; j<shifts->Type->Atom[i].HCnt; j++)
	{
	  printf("    -  ");
	}
    }
  puts("");
  /* 13C shifts */
  for (i=0; i<shifts->Type->HeavyCnt; i++)
    {
      if (shifts->Type->Atom[i].Type&TY_SILENT)
	{
	  printf("       ");
	}
      else
	{
	  if (printneg!=1 && (shifts->C[i]==BU_VOID_SHIFT||shifts->C[i]<0.001))
	    {
	      printf("   n.d.");
	    }
	  else
	    {
	      printf("%7.2f", shifts->C[i]);
	    }
	}
      for (j=1; j<shifts->Type->Atom[i].HCnt; j++)
	{
	  printf("    -  ");
	}
    }
  puts("");
  /* 1H shifts */
  for (i=0; i<shifts->Type->HeavyCnt; i++)
    {
      for (j=0; j<shifts->Type->Atom[i].HCnt; j++)
	{
	  if (printneg!=1 && (shifts->H[i][j]==BU_VOID_SHIFT||shifts->H[i][j]<0.001))
	    {
	      printf("   n.d.");
	    }
	  else
	    {
	      printf("%7.2f", shifts->H[i][j]);
	    }
	}
      if (!shifts->Type->Atom[i].HCnt)
	{
	  printf("    -  ");
	}
    }
  puts("");
}

/* This function is similar to SH_PrintShifts, but also joins substituents to
   the residue. */
void SH_PrintShiftsWithSubstituents(struct BU_Unit *unit)
{
  int i, j, k, substLinked;
  struct SH_Shifts *shifts, *substShifts;
  char substAtomName[16];

  shifts=&(unit->Shifts);

  /* Atom labels */
  for (i=0; i<shifts->Type->HeavyCnt; i++)
    {
      printf("%7s", shifts->Type->Atom[i].Label);
      for (j=1; j<shifts->Type->Atom[i].HCnt; j++) printf("    -  ");
    }
  for (i=0; i<shifts->Type->HeavyCnt; i++)
    {
      if(unit->Subst[i]!=NULL)
	{
	  if(strcasecmp(unit->Subst[i]->Residue->Shifts.Type->CTsuperclass,"subst")==0)
	    {
	      substShifts=&(unit->Subst[i]->Shifts);
	      substLinked=0;
	      for(j=0;j<substShifts->Type->HeavyCnt;j++)
		{
		  if(j==BU_ANOMER_POS(unit->Subst[i]))
		    {
		      continue;
		    }
		  if(unit->Subst[i]->Subst[j]!=0 && unit->Subst[i]->Subst[j]!=unit)
		    {
		      substLinked=1;
		    }
		}
	      if(substLinked)
		{
		  continue;
		}
	      for(j=0; j<substShifts->Type->HeavyCnt; j++)
		{
		  if(substShifts->Type->Atom[j].Type&TY_SILENT)
		    {
		      continue;
		    }
		  sprintf(substAtomName,"%d%s-%s", i+1,
			  unit->Subst[i]->Residue->Node.Name,
			  unit->Subst[i]->Residue->Shifts.Type->Atom[j].Label);
		  printf("%9s", substAtomName);
		}
	    }
	}
    }
  puts("");
  /* C-13 shifts */
  for (i=0; i<shifts->Type->HeavyCnt; i++)
    {
      if (shifts->Type->Atom[i].Type&TY_SILENT)
	{
	  printf("       ");
	}
      else
	{
	  if (shifts->C[i]==BU_VOID_SHIFT || shifts->C[i]<0.001)
	    {
	      printf("   n.d.");
	    }
	  else
	    {
	      printf("%7.2f", shifts->C[i]);
	    }
	}
      for (j=1; j<shifts->Type->Atom[i].HCnt; j++)
	{
	  printf("    -  ");
	}
    }
  for (i=0; i<shifts->Type->HeavyCnt; i++)
    {
      if(unit->Subst[i]!=NULL)
	{
	  if(strcasecmp(unit->Subst[i]->Residue->Shifts.Type->CTsuperclass,"subst")==0)
	    {
	      substShifts=&(unit->Subst[i]->Shifts);
	      substLinked=0;
	      for(j=0;j<substShifts->Type->HeavyCnt;j++)
		{
		  if(unit->Subst[i]->Subst[j]!=0 && unit->Subst[i]->Subst[j]!=unit)
		    {
		      substLinked=1;
		    }
		}
	      if(substLinked)
		{
		  continue;
		}
	      for (j=0; j<substShifts->Type->HeavyCnt; j++)
		{
		  if (substShifts->Type->Atom[j].Type&TY_SILENT)
		    {
		      continue;
		    }
		  if (substShifts->C[j]==BU_VOID_SHIFT || substShifts->C[j]<0.001)
		    {
		      printf("     n.d.");
		    }
		  else
		    {
		      printf("%9.2f", substShifts->C[j]);
		    }
		  for (k=1; k<substShifts->Type->Atom[j].HCnt; k++)
		    {
		      printf("      -  ");
		    }
		}
	    }
	}
    }
  puts("");  
  /* H-1 shifts */
  for (i=0; i<shifts->Type->HeavyCnt; i++)
    {
      if (shifts->Type->Atom[i].Type&TY_SILENT)
	printf("       ");
      else
	{
	  for (j=0; j<shifts->Type->Atom[i].HCnt; j++)
	    {
	      if (shifts->H[i][j]==BU_VOID_SHIFT || shifts->H[i][j]<0.001)
		{
		  printf("   n.d.");
		}
	      else
		{
		  printf("%7.2f", shifts->H[i][j]);
		}
	    }
	  if (!shifts->Type->Atom[i].HCnt)
	    printf("    -  ");
	}
    }
  for (i=0; i<shifts->Type->HeavyCnt; i++)
    {
      if(unit->Subst[i]!=NULL)
	{
	  if(strcasecmp(unit->Subst[i]->Residue->Shifts.Type->CTsuperclass,"subst")==0)
	    {
	      substShifts=&(unit->Subst[i]->Shifts);
	      substLinked=0;
	      for(j=0;j<substShifts->Type->HeavyCnt;j++)
		{
		  if(unit->Subst[i]->Subst[j]!=0 && unit->Subst[i]->Subst[j]!=unit)
		    {
		      substLinked=1;
		    }
		}
	      if(substLinked)
		{
		  continue;
		}
	      for (j=0; j<substShifts->Type->HeavyCnt; j++)
		{
		  if (substShifts->Type->Atom[j].Type&TY_SILENT)
		    {
		      continue;
		    }
		  for (k=0; k<substShifts->Type->Atom[j].HCnt; k++)
		    {
		      if (substShifts->H[j][k]==BU_VOID_SHIFT || substShifts->H[j][k]<0.001)
			{
			  printf("     n.d.");
			}
		      else
			{
			  printf("%9.2f", substShifts->H[j][k]);
			}
		    }
		  if (!substShifts->Type->Atom[j].HCnt)
		    printf("      -  ");
		}
	    }
	}
    }
  puts("");
}

void SH_SaveShifts(struct SH_Shifts *shifts)
{
  int i,j;
  for (i=0; i<shifts->Type->HeavyCnt; i++)
    {
      printf("shift %.2f",shifts->C[i]);
      for (j=0; j<shifts->Type->Atom[i].HCnt; j++)
	printf(" %.2f", shifts->H[i][j]);
      puts("");
    };
}


int SH_Adjust(struct SH_Shifts *dest, float c_off, float h_off)
{
  int i;
  for (i=0; i<TY_MAX_HEAVY; i++)
    {
      if (dest->C[i]!=BU_VOID_SHIFT)
	dest->C[i]+=c_off;
      if (dest->H[i][0]!=BU_VOID_SHIFT)
	dest->H[i][0]+=h_off;
      if (dest->H[i][1]!=BU_VOID_SHIFT)
	dest->H[i][1]+=h_off;
    };
  return(PA_ERR_OK);
}

int SH_RawManipulate(int action, struct SH_Shifts *source, struct SH_Shifts *dest)
{
  int i;
  for (i=0; i<TY_MAX_HEAVY; i++)
    {
      switch (action)
	{
	case SH_SHIFT_ADD:	dest->C[i]+=source->C[i];
	  dest->H[i][0]+=source->H[i][0];
	  dest->H[i][1]+=source->H[i][1];
	  break;
	case SH_SHIFT_SUB:	dest->C[i]-=source->C[i];
	  dest->H[i][0]-=source->H[i][0];
	  dest->H[i][1]-=source->H[i][1];
	  break;
	case SH_SHIFT_COPY:	dest->C[i]=source->C[i];
	  dest->H[i][0]=source->H[i][0];
	  dest->H[i][1]=source->H[i][1];
	  break;
	}; /* switch */
    };
  return(PA_ERR_OK);
}

int SH_Manipulate(int action, struct SH_Shifts *source, struct SH_Shifts *dest)
{
  struct TY_Conversion *conv;
  int i,j;
  conv=(struct TY_Conversion *)TY_FindConversion(source->Type, dest->Type);
  if (conv==NULL) Error(PA_ERR_FAIL,"Don't know how to convert between types");
  /* do a raw conversion? */
  if (conv==TY_CONV_SAME)
    return( SH_RawManipulate(action, source, dest) );
#ifdef DEBUG
  printf("converting from %s to %s\n",
	 source->Type->Node.Name, dest->Type->Node.Name);
#endif
 
  for (i=0; i<dest->Type->HeavyCnt; i++)
    {
      if (conv->AtomNr[i]==TY_VOID_ATOM) continue;
#ifdef DEBUG
      printf("%s-%s << %s-%s\n", dest->Type->Node.Name, dest->Type->Atom[i].Label,
	     source->Type->Node.Name, source->Type->Atom[conv->AtomNr[i]].Label);
#endif
      switch (action)
	{
	case SH_SHIFT_ADD:
	  /* If this atom was not present in the destination structure make sure
	     the shifts are set to 0 before adding the source chemical shifts
	     otherwise the shifts will still be negative and treated as
	     non-existant. */
	  if(dest->C[i]<=-9999)
	    {
	      dest->C[i]=0;
	    }
	  for(j=0;j<dest->Type->Atom[i].HCnt;j++)
	    {
	      if(dest->H[i][j]<=-9999)
		{
		  dest->H[i][j]=0;
		}
	    }
	  dest->C[i]+=source->C[conv->AtomNr[i]];
	  switch (source->Type->Atom[conv->AtomNr[i]].HCnt)
	    {
	    case 2:
	      dest->H[i][0]+=source->H[conv->AtomNr[i]][0];
	      dest->H[i][1]+=source->H[conv->AtomNr[i]][1];
	      break;
	    case 1:
	      dest->H[i][0]+=source->H[conv->AtomNr[i]][0];
	      if (dest->Type->Atom[i].HCnt==2)
		{
		  dest->H[i][1]+=source->H[conv->AtomNr[i]][0];
		}
	    case 0:
	      break;
	    };
	  break; /* case */
	case SH_SHIFT_SUB:
	  dest->C[i]-=source->C[conv->AtomNr[i]];
	  switch (source->Type->Atom[conv->AtomNr[i]].HCnt)
	    {
	    case 2:	dest->H[i][0]-=source->H[conv->AtomNr[i]][0];
	      dest->H[i][1]-=source->H[conv->AtomNr[i]][1];
	      break;
	    case 1:	dest->H[i][0]-=source->H[conv->AtomNr[i]][0];
	      /*if (dest->Type->Atom[i].HCnt==2)*/
	      dest->H[i][1]-=source->H[conv->AtomNr[i]][0];
	    case 0:	break;
	    };
	  break; /*case */
	case SH_SHIFT_COPY:
	  dest->C[i]=source->C[conv->AtomNr[i]];
	  switch (source->Type->Atom[conv->AtomNr[i]].HCnt)
	    {
	    case 2:	dest->H[i][0]=source->H[conv->AtomNr[i]][0];
	      dest->H[i][1]=source->H[conv->AtomNr[i]][1];
	      break;
	    case 1:	dest->H[i][0]=source->H[conv->AtomNr[i]][0];
	      /* if (dest->Type->Atom[i].HCnt==2)*/
	      dest->H[i][1]=source->H[conv->AtomNr[i]][0];
	    case 0:	/* best solution if copying deltas ?
			   dest->H[i][0]=0;
			   dest->H[i][1]=0;*/
	      break;
	    };
	  break; /*case */
	}; /* switch action */
    }; /* for */
  return(PA_ERR_OK);
}

