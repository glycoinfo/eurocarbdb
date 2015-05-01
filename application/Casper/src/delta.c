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

/* Filename = delta.c */
/* Prefix = DE */
/* Procedures related to disaccharide fragments */

#include "delta.h"
#include "parser.h"
#include "calc.h"
#include "build.h"
#include <ctype.h>
#include <string.h>

/***********************************/
/* Methods for DE_Delta	structures */
/***********************************/

void *DE_CreateDelta(char *name)
{
  return ( MakeNode(sizeof(struct DE_Delta),name) );
}

void DE_ClearDelta(struct DE_Delta *delta)
{
  int i;
  delta->Error=0;
  delta->Info[0]=0;
  delta->Source=NULL;
  delta->Residue[0]=NULL;
  delta->Residue[1]=NULL;
  for (i=0; i<TY_MAX_CARBON; i++)
    {
      delta->Shifts[0].C[i]=BU_VOID_SHIFT;
      delta->Shifts[1].C[i]=BU_VOID_SHIFT;
      delta->Shifts[0].H[i][0]=BU_VOID_SHIFT;
      delta->Shifts[1].H[i][0]=BU_VOID_SHIFT;
      delta->Shifts[0].H[i][1]=BU_VOID_SHIFT;
      delta->Shifts[1].H[i][1]=BU_VOID_SHIFT;
    }
  delta->Flags=DE_NONE;
}

void DE_ListDelta(struct DE_Delta *delta)
{
  printf("%s(%s->%s)%s",
	 delta->Residue[DE_NONRED]->Node.Name,
	 delta->Shifts[DE_NONRED].Type->Atom
	 [delta->Shifts[DE_NONRED].Type->Anomeric].Label,
	 delta->Shifts[DE_REDUCING].Type->Atom[delta->nrpos].Label, 
	 delta->Residue[DE_REDUCING]->Node.Name);
  if (delta->Source)
    {
      printf("*");
    }
  if (delta->Error>0.005)
    {
      printf("\terror ~%0.2f ppm", delta->Error);
    }
  else
    {
      printf("\t");
    }
  if (delta->Info[0]!=0)
    {
      printf("\t%s", delta->Info);
    }
  puts("");
}

void DE_PrintDelta(struct DE_Delta *delta)
{
  /* char i,j,flag; */
  if (delta->Info!=NULL)
    {
      printf("%s\n",delta->Info);
    }
  printf("%s(%s->\n",
	 delta->Residue[DE_NONRED]->Node.Name,
	 delta->Shifts[DE_NONRED].Type->Atom[
					     delta->Shifts[DE_NONRED].Type->Anomeric].Label);
  SH_PrintShifts(&(delta->Shifts[DE_NONRED]),1); /* Prints the shift and the 1
						    flag means that negative
						    shifts will be printed too. */
  printf("->%s)%s\n",
	 delta->Shifts[DE_REDUCING].Type->Atom[delta->nrpos].Label,
	 delta->Residue[DE_REDUCING]->Node.Name);
  SH_PrintShifts(&(delta->Shifts[DE_REDUCING]),1);
  printf("\nExpected error: %0.2f\n",delta->Error);
}

void DE_SaveDelta(struct DE_Delta *delta)
{
  /* char i,j,flag; */
  printf("dimer '%s(%s->%s)%s' {\n",
	 delta->Residue[DE_NONRED]->Node.Name,
	 delta->Shifts[DE_NONRED].Type->Atom[
					     delta->Shifts[DE_NONRED].Type->Anomeric].Label,
	 delta->Shifts[DE_REDUCING].Type->Atom[delta->nrpos].Label,
	 delta->Residue[DE_REDUCING]->Node.Name);
  if (delta->Info!=NULL)
    {
      printf("info '%s'\n",delta->Info);
    }
  SH_SaveShifts(&(delta->Shifts[DE_NONRED]));
  SH_SaveShifts(&(delta->Shifts[DE_REDUCING]));
  printf("error %0.2f\n",delta->Error);
  printf("}\n");
}

struct ME_Method DeltaMethod={
  DE_CreateDelta, DE_ClearDelta, FreeNode,
  DE_ListDelta, DE_PrintDelta, DE_SaveDelta
};

/* --- END OF DELTA METHODS --- */

struct PA_Command DE_Cmds[]={
  { "shift",	DE_Shift,   "shift <C-shift> [<H-shift> ... ]",
    ""},
  { "info",	DE_Info,    "info <string>",
    ""},
  { "extract",	DE_Extract, "extract",
    "Extracts monomer shifts from all residues."},
  { "error",	DE_Error,   "error <error>",
    ""},
  { "add",	DE_Add,     "add <res>",
    ""},
  { "sub",	DE_Sub,     "sub <res>",
    ""},
  { "copy",	DE_Copy,    "copy <res>",
    ""},
  { "correct",	DE_Correct, "correct <coff> <hoff>",
    ""},
  { NULL,	NULL,       NULL,
    NULL}
};

struct DE_Delta *DE_Delta;
int DE_DeltaType;
int DE_HvyOffset;

/* If shifts or shift diffs are included */
char DE_ExtractFlag;

/* Duplicates a delta structure */
/* makes necessary mappings between residues */
struct DE_Delta *DE_DupDelta(struct RE_Residue *nres, unsigned char npos,
			     unsigned char rpos, struct RE_Residue *rres,
			     struct DE_Delta *source)
{
  struct DE_Delta *dest;
  struct TY_Conversion *nconv, *rconv;
  int i, onpos, orpos;
  onpos=npos; orpos=rpos;
  /* compute mappings of npos and rpos */

  nconv=(struct TY_Conversion *)TY_FindConversion
    (nres->Shifts.Type, source->Shifts[DE_NONRED].Type);
  rconv=(struct TY_Conversion *)TY_FindConversion
    (rres->Shifts.Type, source->Shifts[DE_REDUCING].Type);

  if ((!nconv)||(!rconv))
    {
      return(NULL);
    }
  if (nconv!=TY_CONV_SAME)
    {
      npos=nconv->AtomNr[npos];
    }
  if (rconv!=TY_CONV_SAME)
    {
      rpos=rconv->AtomNr[rpos];
    }

  dest=(struct DE_Delta *)ME_CreateNode(&DeltaMethod,
					&(rres->Delta[rpos]), nres->Node.Name);
  if (dest==NULL)
    {
      return(NULL);
    }
  if (dest == source)
    {
      return(dest);
    }

  /* position in reducing end */
  dest->nrpos=rpos;
  /* residues */
  dest->Source=source;
  dest->Residue[DE_NONRED]=nres;
  dest->Residue[DE_REDUCING]=rres;

  /* set 'default' diff to 0 ppm */
  for (i=0; i<TY_MAX_HEAVY; i++)
    {
      dest->Shifts[DE_NONRED].C[i]=0;
      dest->Shifts[DE_NONRED].H[i][0]=0;
      dest->Shifts[DE_NONRED].H[i][1]=0;
      dest->Shifts[DE_REDUCING].C[i]=0;
      dest->Shifts[DE_REDUCING].H[i][0]=0;
      dest->Shifts[DE_REDUCING].H[i][1]=0;
    }
  
  dest->Shifts[DE_NONRED].Type=nres->Shifts.Type;
  dest->Shifts[DE_REDUCING].Type=rres->Shifts.Type;
  /* copy nonreducing data */
  SH_COPY(&(source->Shifts[DE_NONRED]), &(dest->Shifts[DE_NONRED]));
  /* copy reducing data */
  SH_COPY(&(source->Shifts[DE_REDUCING]), &(dest->Shifts[DE_REDUCING]));

  /* fix accuracy */
  dest->Error=source->Error;
  /* accuracy for defaults still needs to be added! */
  snprintf(dest->Info, DE_INFO_SIZE, "Copy of %s(%s->%s)%s",
	   source->Residue[DE_NONRED]->Node.Name,
	   source->Shifts[DE_NONRED].Type->Atom[onpos].Label,	/* translation! */
	   source->Shifts[DE_REDUCING].Type->Atom[orpos].Label,	/* -"- */
	   source->Residue[DE_REDUCING]->Node.Name);
  return(dest);
}

/* FindDelta - returns NULL if not found or not valid */
/* should perhaps return a structure and some info about */
/* defaulting and accuracy... */

/* Creates a new delta structure when using defaults! */
/* best def for reducing */
/* best def for nonreducing */
/* best def for enantiomer of reducing */
/* best def for enantiomer of nonred */
/* if found return */
/* try def - def, endef-endef */

/* Should try enantiomers as well */
/* should handle achiral substituents! */
struct DE_Delta *DE_FindDelta(struct RE_Residue *nres, unsigned char npos,
			      unsigned char rpos, struct RE_Residue *rres)
{
  struct DE_Delta *delta, *new, *temp;
  struct RE_Default *alt, *alt2;
  float error=-1;
  int pos;
  struct TY_Conversion *conv;

  /* check if there is a 'normal' delta */
  delta=(struct DE_Delta *)
    FindNode(&(rres->Delta[rpos].Head),nres->Node.Name);

  if (delta)
    {
      error=delta->Error;
      if(delta->Source)
	{
	  delta=delta->Source;
	}
      if(error<0.0001)
	{
	  return(delta);	/* found ! */
	}
    }
  if (CA_Mode&CA_VERBOSE)
    {
      printf("%s(%s->%s)%s",
	     nres->Node.Name, nres->Shifts.Type->Atom[npos].Label,
	     rres->Shifts.Type->Atom[rpos].Label, rres->Node.Name);
    }

  if(error<0)
    {
      error=1e6;	/* unreasonably high */
    }

  /* try finding enantiomer before defaults */
  /* check if there is an enantiomer of this fragment */
  if ((nres->Enantiomer!=NULL)&&(rres->Enantiomer!=NULL))
    {
      temp=(struct DE_Delta *)
	FindNode(&(rres->Enantiomer->Delta[rpos].Head),nres->Enantiomer->Node.Name);
      if (temp && temp->Error<error)
	{
	  new=DE_DupDelta(nres, npos, rpos, rres, temp);
	  if (new==NULL)
	    {
	      return(NULL);
	    }
	  if (CA_Mode&CA_VERBOSE)
	    printf(", using %s(%s->%s)%s\n",
		   nres->Enantiomer->Node.Name, nres->Enantiomer->Shifts.Type->Atom[npos].Label,
		   rres->Enantiomer->Shifts.Type->Atom[rpos].Label, rres->Enantiomer->Node.Name);
	  return(new);
	}
    }

  /* try default for non-reducing */
  for (alt=(struct RE_Default *)nres->Defaults[npos].Head.Succ;
       alt->Node.Succ!=NULL; alt=(struct RE_Default *)alt->Node.Succ)
    {
      temp=(struct DE_Delta *)
	FindNode(&(rres->Delta[rpos].Head),alt->Node.Name);
      if (temp==NULL)
	{
	  continue;	/* no delta-set */
	}
      if (temp->Source)
	{
	  continue;	/* delta-set is a copy */
	}
      if (error > (temp->Error+alt->Error) )
	{
	  delta=temp;
	  error=temp->Error+alt->Error;
	}
    }

  /* try default for reducing */
  for (alt=(struct RE_Default *)rres->Defaults[rpos].Head.Succ;
       alt->Node.Succ!=NULL; alt=(struct RE_Default *)alt->Node.Succ)
    {
      conv=(struct TY_Conversion *)
	TY_FindConversion(rres->Shifts.Type, alt->Residue->Shifts.Type);
      if (conv==NULL)
	{
	  continue;	/* Can't convert */
	}
      if (conv==TY_CONV_SAME)
	{
	  pos=rpos;
	}
      else
	{
	  pos=conv->AtomNr[rpos];
	}
      temp=(struct DE_Delta *)
	FindNode(&(alt->Residue->Delta[pos].Head),nres->Node.Name);
      if (temp==NULL)
	{
	  continue;	/* no delta-set */
	}
      if (temp->Source)
	{
	  continue;	/* delta-set is a copy */
	}
      if (error > (temp->Error+alt->Error) )
	{
	  delta=temp;
	  error=temp->Error+alt->Error;
	}
    };

  if (nres->Enantiomer&&rres->Enantiomer)
    {
      /* try default for enantiomer of non-reducing */
      for (alt=(struct RE_Default *)nres->Enantiomer->Defaults[npos].Head.Succ;
	   alt->Node.Succ!=NULL; alt=(struct RE_Default *)alt->Node.Succ)
	{
	  temp=(struct DE_Delta *)
	    FindNode(&(rres->Enantiomer->Delta[rpos].Head),alt->Node.Name);
	  if (temp==NULL)
	    {
	      continue;	/* no delta-set */
	    }
	  if (temp->Source)
	    {
	      continue;	/* delta-set is a copy */
	    }
	  if (error > (temp->Error+alt->Error) )
	    {
	      delta=temp;
	      error=temp->Error+alt->Error;
	    }
	}
      /* try default for enantiomer of reducing */
      for (alt=(struct RE_Default *)rres->Enantiomer->Defaults[rpos].Head.Succ;
	   alt->Node.Succ!=NULL; alt=(struct RE_Default *)alt->Node.Succ)
	{
	  conv=(struct TY_Conversion *)
	    TY_FindConversion(rres->Shifts.Type, alt->Residue->Shifts.Type);
	  if (conv==NULL)
	    {
	      continue;	/* Can't convert */
	    }
	  if (conv==TY_CONV_SAME)
	    {
	      pos=rpos;
	    }
	  else
	    {
	      pos=conv->AtomNr[rpos];
	    }
	  temp=(struct DE_Delta *)
	    FindNode(&(alt->Residue->Delta[pos].Head),nres->Enantiomer->Node.Name);
	  if (temp==NULL)
	    {
	      continue;	/* no delta-set */
	    }
	  if (temp->Source)
	    {
	      continue;	/* delta-set is a copy */
	    }
	  if (error > (temp->Error+alt->Error) )
	    {
	      delta=temp;
	      error=temp->Error+alt->Error;
	    }
	}
    }

  /* if no dbl defaults */
  if (!(CA_Mode&CA_DOUBLE))
    {
      if (delta)	/* found an approximation */
	{
	  new=DE_DupDelta(nres, npos, rpos, rres, delta);
	  if (new==NULL)
	    {
	      return(NULL);
	    }
	  new->Error=error;
	  /* positions are not corrected ! */
	  if (CA_Mode&CA_VERBOSE)
	    {
	      printf(", using %s(%s->%s)%s\t%.2f\n",
		     delta->Residue[DE_NONRED]->Node.Name,
		     delta->Shifts[DE_NONRED].Type->Atom[
							 delta->Shifts[DE_NONRED].Type->Anomeric/*npos*/].Label,
		     delta->Shifts[DE_REDUCING].Type->Atom[
							   delta->nrpos/*rpos*/].Label,
		     delta->Residue[DE_REDUCING]->Node.Name, error);
	    }
	  return(new);
	}
      if (CA_Mode&CA_VERBOSE) puts("");
      return(NULL);
    }

  /* do double default */
  /* go through defaults for non-red */
  for (alt=(struct RE_Default *)nres->Defaults[npos].Head.Succ;
       alt->Node.Succ!=NULL;
       alt=(struct RE_Default *)alt->Node.Succ)
    {
      /* go through defaults for reducing */
      for (alt2=(struct RE_Default *)rres->Defaults[rpos].Head.Succ;
	   alt2->Node.Succ!=NULL;
	   alt2=(struct RE_Default *)alt2->Node.Succ)
	{
	  /* try finding correction for this combination */
	  /* should take into account any translations */
	  temp=(struct DE_Delta *)
	    FindNode(&(alt2->Residue->Delta[rpos].Head),alt->Node.Name);

	  /* Check that the delta set exists and that it is not a copy. If the error is lower
	     save it as best match. */
	  if (temp!=NULL && !temp->Source && error > (temp->Error+alt->Error+alt2->Error) )
	    {
	      delta=temp;
	      error=temp->Error+alt->Error+alt2->Error;
	    }

	  /* Check for the delta in the non-red */
	  temp=(struct DE_Delta *)
	    FindNode(&(alt->Residue->Delta[npos].Head),alt2->Node.Name);

	  if (temp!=NULL && !temp->Source && error > (temp->Error+alt->Error+alt2->Error) )
	    {
	      delta=temp;
	      error=temp->Error+alt->Error+alt2->Error;
	    }
	}
    }

  if (nres->Enantiomer&&rres->Enantiomer)
    {
      /* go through defaults for enantiomer of non-red */
      for (alt=(struct RE_Default *)nres->Enantiomer->Defaults[npos].Head.Succ;
	   alt->Node.Succ!=NULL;
	   alt=(struct RE_Default *)alt->Node.Succ)
	{
	  /* go through defaults for enantiomer of reducing */
	  for (alt2=(struct RE_Default *)rres->Enantiomer->Defaults[rpos].Head.Succ;
	       alt2->Node.Succ!=NULL;
	       alt2=(struct RE_Default *)alt2->Node.Succ)
	    {
	      /* try finding correction for this combination */
	      /* should take into account any translations */
	      temp=(struct DE_Delta *)
		FindNode(&(alt2->Residue->Delta[rpos].Head),alt->Node.Name);
	      if (temp==NULL)
		{
		  continue;	/* no delta-set */
		}
	      if (temp->Source)
		{
		  continue;	/* delta-set is a copy */
		}
	      if (error > (temp->Error+alt->Error+alt2->Error) )
		{
		  delta=temp;
		  error=temp->Error+alt->Error+alt2->Error;
		}
	      temp=(struct DE_Delta *)
		FindNode(&(alt->Residue->Delta[npos].Head),alt2->Node.Name);
	      if (temp==NULL)
		{
		  continue;	/* no delta-set */
		}
	      if (temp->Source)
		{
		  continue;	/* delta-set is a copy */
		}
	      if (error > (temp->Error+alt->Error+alt2->Error) )
		{
		  delta=temp;
		  error=temp->Error+alt->Error+alt2->Error;
		}
	    }
	}
    }

  if (delta==NULL)
    {
      if (CA_Mode&CA_VERBOSE)
	{
	  puts("");
	}
      return(NULL);
    }
  new=DE_DupDelta(nres, npos, rpos, rres, delta);
  if (new==NULL)
    {
      if (CA_Mode&CA_VERBOSE)
	{
	  puts("");
	}
      return(NULL);
    }
  new->Error=error;
  /* positions are not corrected ! */
  if (CA_Mode&CA_VERBOSE)
    {
      printf(", using %s(%s->%s)%s\t%.2f\n",
	     delta->Residue[DE_NONRED]->Node.Name,
	     delta->Shifts[DE_NONRED].Type->Atom[
						 delta->Shifts[DE_NONRED].Type->Anomeric/*npos*/].Label,
	     delta->Shifts[DE_REDUCING].Type->Atom[
						   delta->nrpos/*rpos*/].Label,
	     delta->Residue[DE_REDUCING]->Node.Name, error);
    }
  /* positions need fixing
     if (CA_Mode&CA_VERBOSE)
     printf(", using %s(%s->%s)%s\n",
     delta->Residue[DE_NONRED]->Node.Name,
     delta->Shifts[DE_NONRED].Type->Atom[npos].Label,
     delta->Shifts[DE_REDUCING].Type->Atom[rpos].Label,
     delta->Residue[DE_REDUCING]->Node.Name);*/
  return(new);
}

/* SYNTAX: Dimer '<non res1>(<non pos1>-><red pos2>)<red res2>' { */
int DE_SetDelta()
{
  struct DE_DimerDesc desc;
  int stat;
  DE_ExtractFlag=DE_NOEXTRACT;

  PA_GetString;
  stat=DE_ReadDelta(&desc, PA_Result.String);
  if (stat!=PA_ERR_OK)
    {
      return(stat);
    }
  if (!(desc.Residue[DE_NONRED]->Shifts.Type->Atom[desc.Atom[DE_NONRED]].Type&TY_FREE_POS))
    {
      Error(PA_ERR_FAIL,"Anomeric position not free");
    }
  if (!(desc.Residue[DE_REDUCING]->Shifts.Type->Atom[desc.Atom[DE_REDUCING]].Type&TY_FREE_POS))
    {
      Error(PA_ERR_FAIL,"Second position not free");
    }

  DE_Delta=(struct DE_Delta *)ME_CreateNode(&DeltaMethod,
					    &(desc.Residue[DE_REDUCING]->Delta[desc.Atom[DE_REDUCING]]),
					    desc.Residue[DE_NONRED]->Node.Name);
  if (DE_Delta==NULL)
    {
      Error(PA_ERR_FATAL,"Out of memory");
    }
  DE_Delta->Residue[DE_NONRED]=desc.Residue[DE_NONRED];
  DE_Delta->Shifts[DE_NONRED].Type=desc.Residue[DE_NONRED]->Shifts.Type;
  DE_Delta->Residue[DE_REDUCING]=desc.Residue[DE_REDUCING];
  DE_Delta->Shifts[DE_REDUCING].Type=desc.Residue[DE_REDUCING]->Shifts.Type;
  DE_DeltaType=DE_NONRED;
 
  DE_HvyOffset=0;			/* Reset counter */
  /* added to simplify printing */
  DE_Delta->nrpos=desc.Atom[DE_REDUCING];

  stat=PA_Execute(DE_Cmds);

  /* Check if the monomers have to be subtracted */
  if (DE_ExtractFlag==DE_EXTRACT)
    {
      SH_SUB(&(DE_Delta->Residue[DE_NONRED]->Shifts), &(DE_Delta->Shifts[DE_NONRED]));
      SH_SUB(&(DE_Delta->Residue[DE_REDUCING]->Shifts), &(DE_Delta->Shifts[DE_REDUCING]));
    }

  if (stat!=PA_ERR_OK)
    {
      return(stat);
    }
  /* back to normal - should check that all shifts assigned */
  if (DE_HvyOffset<(DE_Delta->Shifts[DE_DeltaType].Type->HeavyCnt-1))
    {
      Error(PA_ERR_WARN, "Some shifts are undefined!");
    }
  return(PA_ERR_OK);
  /* return(stat);*/
}

/* SYNTAX: Shift <C-shift> [<H-shift> ... ] */
int DE_Shift()
{
  int hcnt;
  char flag;
  flag=0;
  if (DE_HvyOffset>=DE_Delta->Shifts[DE_DeltaType].Type->HeavyCnt)
    {
      if (DE_DeltaType==DE_NONRED)
	{
	  DE_HvyOffset=0;
	  DE_DeltaType=DE_REDUCING;
	}
      else
	{
	  Error(PA_ERR_FAIL, "Too many signals");
	}
    }
  /* Handle non-C heavy atom */
  if (DE_Delta->Shifts[DE_DeltaType].Type->Atom[(int)DE_HvyOffset].Type&TY_SILENT)
    {
      PA_GetString;
      if (strcmp(PA_Result.String,"-")!=0)
	{
	  Error(PA_ERR_FAIL,"Atom can't have chemical shift");
	}
      DE_HvyOffset++;
      return(PA_ERR_OK);
    }
  PA_GetFloat;
  if (DE_Delta->Shifts[DE_DeltaType].C[(int)DE_HvyOffset]!=BU_VOID_SHIFT)
    {
      flag=1;
    }
  DE_Delta->Shifts[DE_DeltaType].C[DE_HvyOffset]=PA_Result.Float;
  for(hcnt=0; hcnt<DE_Delta->Shifts[DE_DeltaType].Type->Atom[DE_HvyOffset].HCnt; hcnt++)
    {
      PA_GetFloat;
      if (DE_Delta->Shifts[DE_DeltaType].H[DE_HvyOffset][hcnt]!=BU_VOID_SHIFT) flag=1;
      DE_Delta->Shifts[DE_DeltaType].H[DE_HvyOffset][hcnt]=PA_Result.Float;
    }
  DE_HvyOffset++;
  if (flag)
    {
      Error(PA_ERR_WARN, "Chemical shift was redefined");
    }
  return(PA_ERR_OK);
}

/* SYNTAX: Extract */
/* Subtracts monomer shifts from all residues */
int DE_Extract()
{
  DE_ExtractFlag=DE_EXTRACT;
  return(PA_ERR_OK);
}

/* SYNTAX: Info <string> */
int DE_Info()
{
  PA_GetString;
  strncpy(DE_Delta->Info, PA_Result.String, DE_INFO_SIZE);
  return(PA_ERR_OK);
}

/* SYNTAX: Error <err> */
int DE_Error()
{
  PA_GetFloat;
  DE_Delta->Error=PA_Result.Float;
  return(PA_ERR_OK);
}

/* extracts dimer info from a string */
/* <res1>(<pos1>-><pos2>)<res2> */
int DE_ReadDelta(struct DE_DimerDesc *desc, char *string)
{
  int i;
  char residue[NODE_NAME_LENGTH];
  char atom[NODE_NAME_LENGTH];

  /* first residue */
  for (i=0; isalnum(*string) && (i<NODE_NAME_LENGTH); i++)
    {
      residue[i]=*string; string++;
    }
  residue[i]=0;	/* terminate string */
  if (*string!='(')
    {
      Error(PA_ERR_FAIL,"Expecting (");
    }
  string++;
  desc->Residue[DE_NONRED]=(struct RE_Residue *)FindNode((struct Node *)&ResidueList, residue);
  if ( desc->Residue[DE_NONRED]==NULL)
    {
      Error(PA_ERR_FAIL, "First residue not found");
    }
  /* anomeric position */
  for (i=0; isalnum(*string) && (i<NODE_NAME_LENGTH); i++)
    {
      atom[i]=*string; string++;
    }
  atom[i]=0;	/* terminate string */
  if (i==0)
    {
      desc->Atom[DE_NONRED]=desc->Residue[DE_NONRED]->Shifts.Type->Anomeric;
    }
  else
    {
      desc->Atom[DE_NONRED]=TY_FindAtom(desc->Residue[DE_NONRED]->Shifts.Type, atom);
      if (desc->Atom[DE_NONRED]==TY_NOT_FOUND)
	{
	  Error(PA_ERR_FAIL, "Anomeric position not found");
	}
      if (desc->Atom[DE_NONRED]!=desc->Residue[DE_NONRED]->Shifts.Type->Anomeric)
	{
	  Error(PA_ERR_FAIL, "Expecting an anomeric position");
	}
    }
  if (*string=='<')
    {
      string++;
    }
  if (*string!='-')
    {
      Error(PA_ERR_FAIL, "Expecting -");
    }
  string++;
  if (*string=='>')
    {
      string++;
    }
  /* second linkage position */
  for (i=0; (isalnum(*string)) && (i<NODE_NAME_LENGTH); i++)
    {
      atom[i]=*string; string++;
    }
  atom[i]=0;	/* terminate string */
  if (*string!=')')
    {
      Error(PA_ERR_FAIL, "Expecting )");
    }
  string++;
  /* second residue */
  for (i=0; (isalnum(*string)) && (i<NODE_NAME_LENGTH); i++)
    {
      residue[i]=*string; string++;
    }
  residue[i]=0;	/* terminate string */
  if (*string)
    {
      Error(PA_ERR_FAIL, "Expecting second residue");
    }
  desc->Residue[DE_REDUCING]=(struct RE_Residue *)FindNode((struct Node *)&ResidueList, residue);
  if (desc->Residue[DE_REDUCING]==NULL)
    {
      Error(PA_ERR_FAIL, "Second residue not found");
    }
  desc->Atom[DE_REDUCING]=TY_FindAtom(desc->Residue[DE_REDUCING]->Shifts.Type, atom);
  if (desc->Atom[DE_REDUCING]==TY_NOT_FOUND)
    {
      Error(PA_ERR_FAIL, "Second position not found");
    }

  return(PA_ERR_OK);
}
 
/* SYNTAX:	Copy|Add|Sub <non res> <non pos> <red pos> <red res> */
int DE_Manipulate(int action)
{
  struct DE_Delta *delta;
  struct DE_DimerDesc desc;
  int stat;
  PA_GetString;
  stat=DE_ReadDelta(&desc, PA_Result.String);
  if (stat!=PA_ERR_OK)
    {
      return(stat);
    }
  delta=DE_FindDelta(desc.Residue[DE_NONRED], desc.Atom[DE_NONRED],
		     desc.Atom[DE_REDUCING], desc.Residue[DE_REDUCING]);
  if (action==SH_SHIFT_COPY)
    {
      DE_Delta->Error=delta->Error;
    }
  else
    {
      DE_Delta->Error+=delta->Error;
    }
  stat=SH_Manipulate(action, &(delta->Shifts[DE_NONRED]),
		     &(DE_Delta->Shifts[DE_NONRED]));
  if (stat!=PA_ERR_OK)
    {
      return(stat);
    }
  return( SH_Manipulate(action, &(delta->Shifts[DE_REDUCING]),
			&(DE_Delta->Shifts[DE_REDUCING])) );
}

/* SYNTAX: Add <res> */
int DE_Add()
{
  return(DE_Manipulate(SH_SHIFT_ADD));
}

/* SYNTAX: Sub <res> */
int DE_Sub()
{
  return(DE_Manipulate(SH_SHIFT_SUB));
}

/* SYNTAX: Copy <res> */
int DE_Copy()
{
  return(DE_Manipulate(SH_SHIFT_COPY));
}

/* SYNTAX: Correct <coff> <hoff> */
int DE_Correct()
{
  float coff, hoff;
  PA_GetFloat; coff=PA_Result.Float;
  PA_GetFloat; hoff=PA_Result.Float;
  SH_Adjust(&(DE_Delta->Shifts[DE_NONRED]), coff, hoff);
  SH_Adjust(&(DE_Delta->Shifts[DE_REDUCING]), coff, hoff);
  return(PA_ERR_OK);
}

/* SYNTAX: Predict */
/* calculates all the chemical shifts for the dimer */
/* prepares for the use of 'sparse' data */
/*
  int DE_Predict()
  {
  struct DE_Delta *delta;
  delta=DE_FindDelta();
  if (!delta) Error();
  DE_Delta->Flags|=DE_PREDICTED;
  DE_DupDelta(  ,delta)
  return(PA_ERR_OK);
  }
*/
