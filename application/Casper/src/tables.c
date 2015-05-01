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

/* various routines for the generation of tables */
/* from the databases */
/* prefix= TAB_ */

#include "residue.h"
#include "delta.h"
#include "ddelta.h"
#include "shifts.h"
#include "parser.h"

int TA_TabulateResidues()
{
  struct RE_Residue *residue;
  for (residue=(struct RE_Residue *)ResidueList.Head.Succ;
       residue->Node.Succ!=NULL;
       residue=(struct RE_Residue *)residue->Node.Succ)
    {
      printf("%s\t",residue->Node.Name);
      if (residue->Info[0]!=0)
	printf("\t%s",residue->Info);
      puts("");
      SH_PrintShifts(&residue->Shifts,0);
      puts("");
    }
  return(PA_ERR_OK);
}

int TA_TabulateDimers()
{
  struct DE_Delta *delta;
  struct RE_Residue *nonred;
  struct SH_Shifts shifts;
  int i;
  for (nonred=(struct RE_Residue *)ResidueList.Head.Succ;
       nonred->Node.Succ!=NULL;
       nonred=(struct RE_Residue *)nonred->Node.Succ)
    {
      for (i=0; i<nonred->Shifts.Type->HeavyCnt; i++)
	{
	  for (delta=(struct DE_Delta *)nonred->Delta[i].Head.Succ;
	       delta->Node.Succ!=NULL;
	       delta=(struct DE_Delta *)delta->Node.Succ)
	    {
	      /* don't tabulate internally generated dimers */
	      if (delta->Source!=NULL) continue;
	      printf("%s(%s->%s)%s\t",
		     delta->Residue[DE_NONRED]->Node.Name,
		     delta->Shifts[DE_NONRED].Type->Atom
		     [delta->Shifts[DE_NONRED].Type->Anomeric].Label,
		     delta->Shifts[DE_REDUCING].Type->Atom[delta->nrpos].Label, 
		     delta->Residue[DE_REDUCING]->Node.Name);
	      if (delta->Info[0]!=0) printf("\t%s", delta->Info);
	      puts("");
	      /* add delta-set and residue shifts */
	      shifts.Type=delta->Shifts[DE_NONRED].Type;
	      SH_RAW_COPY(&(delta->Residue[DE_NONRED]->Shifts), &shifts);
	      SH_RAW_ADD(&(delta->Shifts[DE_NONRED]), &shifts);
	      printf("%s(%s->\n",
		     delta->Residue[DE_NONRED]->Node.Name,
		     delta->Shifts[DE_NONRED].Type->
		     Atom[delta->Shifts[DE_NONRED].Type->Anomeric].Label);
	      SH_PrintShifts(&shifts, 1);
	      /* same for reducing end */
	      shifts.Type=delta->Shifts[DE_REDUCING].Type;
	      SH_RAW_COPY(&(delta->Residue[DE_REDUCING]->Shifts), &shifts);
	      SH_RAW_ADD(&(delta->Shifts[DE_REDUCING]), &shifts);
	      printf("->%s)%s\n",
		     delta->Shifts[DE_REDUCING].Type->Atom[delta->nrpos].Label,
		     delta->Residue[DE_REDUCING]->Node.Name);
	      SH_PrintShifts(&shifts, 1);
	      puts("");
	    } /* delta */
	} /* position */
    } /* residue */
  return(PA_ERR_OK);
}

int TA_TabulateTrimers()
{
  struct DD_Delta *trimer;
  struct DE_Delta *dimer;
  int la, ha, cl, ch;

  struct SH_Shifts shifts;

  for (la=0; la<2; la++)	/* low anomer */
    for (ha=0; ha<2; ha++)	/* high anomer */
      for (cl=0; cl<2; cl++)	/* center lo */
	for (ch=0; ch<2; ch++)	/* center hi */
	  {
	    printf("%d %d %d %d\n", la, ha, cl, ch);
	    for (trimer=(struct DD_Delta *)DD_Branch[la][ha][cl][ch].Head.Succ;
		 trimer->Node.Succ!=NULL;
		 trimer=(struct DD_Delta *)trimer->Node.Succ)
	      {
		if (trimer->CentralPos[DD_FIRST]==trimer->Shifts[DD_CENTRAL].Type->Anomeric)
		  {
		    printf("%s(%s->%s)%s(%s->%s)%s",
			   trimer->Residue[DD_SECOND]->Node.Name,
			   trimer->Shifts[DD_SECOND].Type->
			   Atom[trimer->SubstPos[DD_SECOND]].Label,
			   trimer->Shifts[DD_CENTRAL].Type->
			   Atom[trimer->CentralPos[DD_SECOND]].Label,
			   trimer->Residue[DD_CENTRAL]->Node.Name,
			   trimer->Shifts[DD_CENTRAL].Type->
			   Atom[trimer->CentralPos[DD_FIRST]].Label,
			   trimer->Shifts[DD_FIRST].Type->
			   Atom[trimer->SubstPos[DD_FIRST]].Label,
			   trimer->Residue[DD_FIRST]->Node.Name);
		    /* info */
		    if (trimer->Info[0]!=0) printf("\t%s", trimer->Info);
		    puts("");
		    /* second */
		    printf("%s(%s->\n", trimer->Residue[DD_SECOND]->Node.Name,
			   trimer->Shifts[DD_SECOND].Type
			   ->Atom[trimer->SubstPos[DD_SECOND]].Label);
		    shifts.Type=trimer->Shifts[DD_SECOND].Type;
		    SH_RAW_COPY(&(trimer->Residue[DD_SECOND]->Shifts), &shifts);
		    SH_RAW_ADD(&(trimer->Shifts[DD_SECOND]), &shifts);
		    dimer=(struct DE_Delta *)DE_FindDelta(
							  trimer->Residue[DD_SECOND], trimer->SubstPos[DD_SECOND],
							  trimer->CentralPos[DD_SECOND], trimer->Residue[DD_CENTRAL]);
		    SH_RAW_ADD(&(dimer->Shifts[DE_NONRED]), &shifts);
		    SH_PrintShifts(&shifts, 1);
		    /* central */
		    printf("->%s)%s(%s->\n",
			   trimer->Shifts[DD_CENTRAL].Type->Atom[trimer->CentralPos[DD_SECOND]].Label,
			   trimer->Residue[DD_CENTRAL]->Node.Name,
			   trimer->Shifts[DD_CENTRAL].Type->Atom[trimer->CentralPos[DD_FIRST]].Label);
		    shifts.Type=trimer->Shifts[DD_CENTRAL].Type;
		    SH_RAW_COPY(&(trimer->Residue[DD_CENTRAL]->Shifts), &shifts);
		    SH_RAW_ADD(&(trimer->Shifts[DD_CENTRAL]), &shifts);
		    dimer=(struct DE_Delta *)DE_FindDelta(
							  trimer->Residue[DD_SECOND], trimer->SubstPos[DD_SECOND],
							  trimer->CentralPos[DD_SECOND], trimer->Residue[DD_CENTRAL]);
		    SH_RAW_ADD(&(dimer->Shifts[DE_REDUCING]), &shifts);
		    dimer=(struct DE_Delta *)DE_FindDelta(
							  trimer->Residue[DD_CENTRAL], trimer->CentralPos[DD_FIRST/*CENTRAL*/],
							  trimer->SubstPos[DD_FIRST], trimer->Residue[DD_FIRST]);
		    SH_RAW_ADD(&(dimer->Shifts[DE_NONRED]), &shifts);
		    SH_PrintShifts(&shifts, 1);
		    /* first */
		    printf("->%s)%s\n",
			   trimer->Shifts[DD_FIRST].Type->Atom[trimer->SubstPos[DD_FIRST]].Label,
			   trimer->Residue[DD_FIRST]->Node.Name);
		    shifts.Type=trimer->Shifts[DD_FIRST].Type;
		    SH_RAW_COPY(&(trimer->Residue[DD_FIRST]->Shifts), &shifts);
		    SH_RAW_ADD(&(trimer->Shifts[DD_FIRST]), &shifts);
		    dimer=(struct DE_Delta *)DE_FindDelta(
							  trimer->Residue[DD_CENTRAL], trimer->CentralPos[DD_FIRST],
							  trimer->SubstPos[DD_FIRST], trimer->Residue[DD_FIRST]);
		    SH_RAW_ADD(&(dimer->Shifts[DE_REDUCING]), &shifts);
		    SH_PrintShifts(&shifts, 1);
		    puts("");
		  }
		else if (trimer->CentralPos[DD_SECOND]==trimer->Shifts[DD_CENTRAL].Type->Anomeric)
		  {
		    printf("%s(%s->%s)%s(%s->%s)%s",
			   trimer->Residue[DD_SECOND]->Node.Name,
			   trimer->Shifts[DD_SECOND].Type->Atom[trimer->SubstPos[DD_SECOND]].Label,
			   trimer->Shifts[DD_CENTRAL].Type->Atom[trimer->CentralPos[DD_SECOND]].Label,
			   trimer->Residue[DD_CENTRAL]->Node.Name,
			   trimer->Shifts[DD_CENTRAL].Type->Atom[trimer->CentralPos[DD_FIRST]].Label,
			   trimer->Shifts[DD_FIRST].Type->Atom[trimer->SubstPos[DD_FIRST]].Label,
			   trimer->Residue[DD_FIRST]->Node.Name);
		  }
		else
		  {
		    printf("%s(%s->%s)[%s(%s->%s)]%s",
			   trimer->Residue[DD_FIRST]->Node.Name,
			   trimer->Shifts[DD_FIRST].Type->Atom[trimer->SubstPos[DD_FIRST]].Label,
			   trimer->Shifts[DD_CENTRAL].Type->Atom[trimer->CentralPos[DD_FIRST]].Label,
			   trimer->Residue[DD_SECOND]->Node.Name,
			   trimer->Shifts[DD_SECOND].Type->Atom[trimer->SubstPos[DD_SECOND]].Label,
			   trimer->Shifts[DD_CENTRAL].Type->Atom[trimer->CentralPos[DD_SECOND]].Label,
			   trimer->Residue[DD_CENTRAL]->Node.Name);
		    /* info */
		    if (trimer->Info[0]!=0) printf("\t%s", trimer->Info);
		    puts("");
		    /* first */
		    printf("%s(%s->\n", trimer->Residue[DD_FIRST]->Node.Name,
			   trimer->Shifts[DD_FIRST].Type->Atom[trimer->SubstPos[DD_FIRST]].Label);
		    shifts.Type=trimer->Shifts[DD_FIRST].Type;
		    SH_RAW_COPY(&(trimer->Residue[DD_FIRST]->Shifts), &shifts);
		    SH_RAW_ADD(&(trimer->Shifts[DD_FIRST]), &shifts);
		    dimer=(struct DE_Delta *)DE_FindDelta(
							  trimer->Residue[DD_FIRST], trimer->SubstPos[DD_FIRST],
							  trimer->CentralPos[DD_FIRST], trimer->Residue[DD_CENTRAL]);
		    SH_RAW_ADD(&(dimer->Shifts[DE_NONRED]), &shifts);
		    SH_PrintShifts(&shifts, 1);
		    /* second */
		    printf("%s(%s->\n", trimer->Residue[DD_SECOND]->Node.Name,
			   trimer->Shifts[DD_SECOND].Type->Atom[trimer->SubstPos[DD_SECOND]].Label);
		    shifts.Type=trimer->Shifts[DD_SECOND].Type;
		    SH_RAW_COPY(&(trimer->Residue[DD_SECOND]->Shifts), &shifts);
		    SH_RAW_ADD(&(trimer->Shifts[DD_SECOND]), &shifts);
		    dimer=(struct DE_Delta *)DE_FindDelta(
							  trimer->Residue[DD_SECOND], trimer->SubstPos[DD_SECOND],
							  trimer->CentralPos[DD_SECOND], trimer->Residue[DD_CENTRAL]);
		    SH_RAW_ADD(&(dimer->Shifts[DE_NONRED]), &shifts);
		    SH_PrintShifts(&shifts, 1);
		    /* central */
		    printf("->%s,%s)%s\n",
			   trimer->Shifts[DD_CENTRAL].Type->Atom[trimer->CentralPos[DD_FIRST]].Label,
			   trimer->Shifts[DD_CENTRAL].Type->Atom[trimer->CentralPos[DD_SECOND]].Label,
			   trimer->Residue[DD_CENTRAL]->Node.Name);
		    shifts.Type=trimer->Shifts[DD_CENTRAL].Type;
		    SH_RAW_COPY(&(trimer->Residue[DD_CENTRAL]->Shifts), &shifts);
		    SH_RAW_ADD(&(trimer->Shifts[DD_CENTRAL]), &shifts);
		    dimer=(struct DE_Delta *)DE_FindDelta(
							  trimer->Residue[DD_SECOND], trimer->SubstPos[DD_SECOND],
							  trimer->CentralPos[DD_SECOND], trimer->Residue[DD_CENTRAL]);
		    SH_RAW_ADD(&(dimer->Shifts[DE_REDUCING]), &shifts);
		    dimer=(struct DE_Delta *)DE_FindDelta(
							  trimer->Residue[DD_FIRST], trimer->SubstPos[DD_FIRST],
							  trimer->CentralPos[DD_FIRST], trimer->Residue[DD_CENTRAL]);
		    SH_RAW_ADD(&(dimer->Shifts[DE_REDUCING]), &shifts);
		    SH_PrintShifts(&shifts, 1);
		    puts("");
		  }
	      } /* for delta */
	  } /* for ch */
  return(PA_ERR_OK);
}
