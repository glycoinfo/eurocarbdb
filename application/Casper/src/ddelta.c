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

/* Filename =ddelta.c */
/* Prefix = DD */
/* Functions related to trisaccharides */
/* branchpoint corrections */

#include "delta.h"
#include "ddelta.h"
#include "parser.h"
#include "build.h"
#include <ctype.h>
#include <string.h>

/************************************/
/* Methods for DD_Ddelta structures */
/************************************/

void *DD_CreateTrimer(char *name)
{
  return ( MakeNode(sizeof(struct DD_Delta),name) );
}

void DD_ClearTrimer(delta)
     struct DD_Delta *delta;
{
  int i,j;

  delta->Error=0;
  delta->Info[0]=0;
  delta->Residue[0]=NULL;
  delta->Residue[1]=NULL;
  delta->Residue[2]=NULL;
  for(i=0; i<=2; i++)
    {
      for (j=0; j<TY_MAX_CARBON; j++)
	{
	  delta->Shifts[i].C[j]=BU_VOID_SHIFT;
	  delta->Shifts[i].H[j][0]=BU_VOID_SHIFT;
	  delta->Shifts[i].H[j][1]=BU_VOID_SHIFT;
	}
    }
  delta->SubstPos[0]=0;
  delta->SubstPos[1]=0;
  delta->CentralPos[0]=0;
  delta->CentralPos[1]=0;
  delta->nrpos=0;
}

void DD_ListTrimer(struct DD_Delta *delta)
{
  /* wrong for 1->2 */
  /* is lower central pos an anomeric position? */
  if (delta->CentralPos[DD_FIRST]==delta->Shifts[DD_CENTRAL].Type->Anomeric)
    {
      printf("%s(%s->%s)%s(%s->%s)%s",
	     delta->Residue[DD_SECOND]->Node.Name,
	     delta->Shifts[DD_SECOND].Type->Atom[delta->SubstPos[DD_SECOND]].Label,
	     delta->Shifts[DD_CENTRAL].Type->Atom[delta->CentralPos[DD_SECOND]].Label,
	     delta->Residue[DD_CENTRAL]->Node.Name,
	     delta->Shifts[DD_CENTRAL].Type->Atom[delta->CentralPos[DD_FIRST]].Label,
	     delta->Shifts[DD_FIRST].Type->Atom[delta->SubstPos[DD_FIRST]].Label,
	     delta->Residue[DD_FIRST]->Node.Name);
    }
  else if (delta->CentralPos[DD_SECOND]==delta->Shifts[DD_CENTRAL].Type->Anomeric)
    {
      printf("%s(%s->%s)%s(%s->%s)%s",
	     delta->Residue[DD_SECOND]->Node.Name,
	     delta->Shifts[DD_SECOND].Type->Atom[delta->SubstPos[DD_SECOND]].Label,
	     delta->Shifts[DD_CENTRAL].Type->Atom[delta->CentralPos[DD_SECOND]].Label,
	     delta->Residue[DD_CENTRAL]->Node.Name,
	     delta->Shifts[DD_CENTRAL].Type->Atom[delta->CentralPos[DD_FIRST]].Label,
	     delta->Shifts[DD_FIRST].Type->Atom[delta->SubstPos[DD_FIRST]].Label,
	     delta->Residue[DD_FIRST]->Node.Name);
    }
  else
    {
      printf("%s(%s->%s)[%s(%s->%s)]%s",
	     delta->Residue[DD_FIRST]->Node.Name,
	     delta->Shifts[DD_FIRST].Type->Atom[delta->SubstPos[DD_FIRST]].Label,
	     delta->Shifts[DD_CENTRAL].Type->Atom[delta->CentralPos[DD_FIRST]].Label,
	     delta->Residue[DD_SECOND]->Node.Name,
	     delta->Shifts[DD_SECOND].Type->Atom[delta->SubstPos[DD_SECOND]].Label,
	     delta->Shifts[DD_CENTRAL].Type->Atom[delta->CentralPos[DD_SECOND]].Label,
	     delta->Residue[DD_CENTRAL]->Node.Name);
    }
  if (delta->Error>0.005)
    {
      printf("\terror ~%0.2f", delta->Error);
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

void DD_PrintTrimer(struct DD_Delta *delta)
{
  /*  printf("%s(%s->%s)[%s(%s->%s)]%s\taccuracy: ~%0.2f ppm",
      delta->Residue[DD_SECOND]->Node.Name,
      delta->Shifts[DD_SECOND].Type->Atom[delta->SubstPos[DD_SECOND]].Label,
      delta->Shifts[DD_CENTRAL].Type->Atom[delta->CentralPos[DD_SECOND]].Label,
      delta->Residue[DD_FIRST]->Node.Name,
      delta->Shifts[DD_FIRST].Type->Atom[delta->SubstPos[DD_FIRST]].Label,
      delta->Shifts[DD_CENTRAL].Type->Atom[delta->CentralPos[DD_FIRST]].Label,
      delta->Residue[DD_CENTRAL]->Node.Name,
      delta->Error);
      if (delta->Info[0]!=0) printf("\t%s", delta->Info);
      puts(""); */
  /* Should also mention actual (delta-shift/shift) values */

  if (delta->CentralPos[DD_FIRST]==delta->Shifts[DD_CENTRAL].Type->Anomeric)
    {
      printf("%s(%s->%s)%s(%s->%s)%s",
	     delta->Residue[DD_SECOND]->Node.Name,
	     delta->Shifts[DD_SECOND].Type->Atom[delta->SubstPos[DD_SECOND]].Label,
	     delta->Shifts[DD_CENTRAL].Type->Atom[delta->CentralPos[DD_SECOND]].Label,
	     delta->Residue[DD_CENTRAL]->Node.Name,
	     delta->Shifts[DD_CENTRAL].Type->Atom[delta->CentralPos[DD_FIRST]].Label,
	     delta->Shifts[DD_FIRST].Type->Atom[delta->SubstPos[DD_FIRST]].Label,
	     delta->Residue[DD_FIRST]->Node.Name);
      /* info */
      if (delta->Info[0]!=0)
	{
	  printf("\t%s", delta->Info);
	}
      puts("");
      /* second */
      printf("%s(%s->\n", delta->Residue[DD_SECOND]->Node.Name,
	     delta->Shifts[DD_SECOND].Type->Atom[delta->SubstPos[DD_SECOND]].Label);
      SH_PrintShifts(&(delta->Shifts[DD_SECOND]),1); /* The 1 flag means that
							negative shifts will
							also be printed. */
      /* central */
      printf("->%s)%s(%s->\n",
	     delta->Shifts[DD_CENTRAL].Type->Atom[delta->CentralPos[DD_SECOND]].Label,
	     delta->Residue[DD_CENTRAL]->Node.Name,
	     delta->Shifts[DD_CENTRAL].Type->Atom[delta->CentralPos[DD_FIRST]].Label);
      SH_PrintShifts(&(delta->Shifts[DD_CENTRAL]),1); /*Print also neg shifts */
      /* first */
      printf("->%s)%s\n",
	     delta->Shifts[DD_FIRST].Type->Atom[delta->SubstPos[DD_FIRST]].Label,
	     delta->Residue[DD_FIRST]->Node.Name);
      SH_PrintShifts(&(delta->Shifts[DD_FIRST]),1);
      puts("");
    }
  else if (delta->CentralPos[DD_SECOND]==delta->Shifts[DD_CENTRAL].Type->Anomeric)
    {
      printf("%s(%s->%s)%s(%s->%s)%s",
	     delta->Residue[DD_SECOND]->Node.Name,
	     delta->Shifts[DD_SECOND].Type->Atom[delta->SubstPos[DD_SECOND]].Label,
	     delta->Shifts[DD_CENTRAL].Type->Atom[delta->CentralPos[DD_SECOND]].Label,
	     delta->Residue[DD_CENTRAL]->Node.Name,
	     delta->Shifts[DD_CENTRAL].Type->Atom[delta->CentralPos[DD_FIRST]].Label,
	     delta->Shifts[DD_FIRST].Type->Atom[delta->SubstPos[DD_FIRST]].Label,
	     delta->Residue[DD_FIRST]->Node.Name);
    }
  else
    {
      printf("%s(%s->%s)[%s(%s->%s)]%s",
	     delta->Residue[DD_FIRST]->Node.Name,
	     delta->Shifts[DD_FIRST].Type->Atom[delta->SubstPos[DD_FIRST]].Label,
	     delta->Shifts[DD_CENTRAL].Type->Atom[delta->CentralPos[DD_FIRST]].Label,
	     delta->Residue[DD_SECOND]->Node.Name,
	     delta->Shifts[DD_SECOND].Type->Atom[delta->SubstPos[DD_SECOND]].Label,
	     delta->Shifts[DD_CENTRAL].Type->Atom[delta->CentralPos[DD_SECOND]].Label,
	     delta->Residue[DD_CENTRAL]->Node.Name);
      /* info */
      if (delta->Info[0]!=0)
	{
	  printf("\t%s", delta->Info);
	}
      puts("");
      /* first */
      printf("%s(%s->\n", delta->Residue[DD_FIRST]->Node.Name,
	     delta->Shifts[DD_FIRST].Type->Atom[delta->SubstPos[DD_FIRST]].Label);
      SH_PrintShifts(&(delta->Shifts[DD_FIRST]),1); /* Print neg shifts */
      /* second */
      printf("%s(%s->\n", delta->Residue[DD_SECOND]->Node.Name,
	     delta->Shifts[DD_SECOND].Type->Atom[delta->SubstPos[DD_SECOND]].Label);
      SH_PrintShifts(&(delta->Shifts[DD_SECOND]),1);
      /* central */
      printf("->%s,%s)%s\n",
	     delta->Shifts[DD_CENTRAL].Type->Atom[delta->CentralPos[DD_FIRST]].Label,
	     delta->Shifts[DD_CENTRAL].Type->Atom[delta->CentralPos[DD_SECOND]].Label,
	     delta->Residue[DD_CENTRAL]->Node.Name);
      SH_PrintShifts(&(delta->Shifts[DD_CENTRAL]),1);
      puts("");
    }

}


struct ME_Method DD_DeltaMethod={
  DD_CreateTrimer, DD_ClearTrimer, FreeNode,
  DD_ListTrimer, DD_PrintTrimer, NULL/* DD_SaveTrimer*/
};

/* --- END OF DELTA METHODS --- */

struct PA_Command DD_Cmds[]={
  { "shift",	DD_Shift,    "shift <C-shift> [<H-shift> ... ]",
    ""},
  { "info",	DD_Info,     "info <string>",
    ""},
  { "extract",	DD_Extract,  "extract",
    "Extracts monomer shifts from all residues."},
  { "correct",	DD_Correct,  "correct <coff> <hoff>",
    ""},
  { "error",	DD_Error,    "error <error>",
    ""},
  { NULL,	NULL,        NULL,
    NULL}
};
/* SYNTAX:	Copy|Add|Sub <non res> <non pos> <red pos> <red res> */
/* SYNTAX:	AddShift|SubShift <res> <pos> <13C> [<1Ha> [<1Hb>]] */
/* SYNTAX:	CopyShift|SwapShift <res> <pos> <res> <pos> */


struct List DD_Branch[2][2][2][2]={
  {
    {
      {
	{
	  {NULL, &(DD_Branch[0][0][0][0].Tail), "H0000"},
	  {&(DD_Branch[0][0][0][0].Head), NULL, "T0000"}
	},
	{
	  {NULL, &(DD_Branch[0][0][0][1].Tail), "H0001"},
	  {&(DD_Branch[0][0][0][1].Head), NULL, "T0001"}
	}
      },
      {
	{
	  {NULL, &(DD_Branch[0][0][1][0].Tail), "H0010"},
	  {&(DD_Branch[0][0][1][0].Head), NULL, "T0010"}
	},
	{
	  {NULL, &(DD_Branch[0][0][1][1].Tail), "H0011"},
	  {&(DD_Branch[0][0][1][1].Head), NULL, "T0011"}
	}
      }
    },
    {
      {
	{
	  {NULL, &(DD_Branch[0][1][0][0].Tail), "H0100"},
	  {&(DD_Branch[0][1][0][0].Head), NULL, "T0100"}
	},
	{
	  {NULL, &(DD_Branch[0][1][0][1].Tail), "H0101"},
	  {&(DD_Branch[0][1][0][1].Head), NULL, "T0101"}
	}
      },
      {
	{
	  {NULL, &(DD_Branch[0][1][1][0].Tail), "H0110"},
	  {&(DD_Branch[0][1][1][0].Head), NULL, "T0110"}
	},
	{
	  {NULL, &(DD_Branch[0][1][1][1].Tail), "H0111"},
	  {&(DD_Branch[0][1][1][1].Head), NULL, "T0111"}
	}
      }
    }
  },
  {
    {
      {
	{
	  {NULL, &(DD_Branch[1][0][0][0].Tail), "H1000"},
	  {&(DD_Branch[1][0][0][0].Head), NULL, "T1000"}
	},
	{
	  {NULL, &(DD_Branch[1][0][0][1].Tail), "H1001"},
	  {&(DD_Branch[1][0][0][1].Head), NULL, "T1001"}
	}
      },
      {
	{
	  {NULL, &(DD_Branch[1][0][1][0].Tail), "H1010"},
	  {&(DD_Branch[1][0][1][0].Head), NULL, "T1010"}
	},
	{
	  {NULL, &(DD_Branch[1][0][1][1].Tail), "H1011"},
	  {&(DD_Branch[1][0][1][1].Head), NULL, "T1011"}
	}
      }
    },
    {
      {
	{
	  {NULL, &(DD_Branch[1][1][0][0].Tail), "H1100"},
	  {&(DD_Branch[1][1][0][0].Head), NULL, "T1100"}
	},
	{
	  {NULL, &(DD_Branch[1][1][0][1].Tail), "H1101"},
	  {&(DD_Branch[1][1][0][1].Head), NULL, "T1101"}
	}
      },
      {
	{
	  {NULL, &(DD_Branch[1][1][1][0].Tail), "H1110"},
	  {&(DD_Branch[1][1][1][0].Head), NULL, "T1110"}
	},
	{
	  {NULL, &(DD_Branch[1][1][1][1].Tail), "H1111"},
	  {&(DD_Branch[1][1][1][1].Head), NULL, "T1111"}
	}
      }
    }
  }
};

struct DD_Delta *DD_Delta;
char DD_DeltaType;
char DD_DeltaOrder;
char DD_HvyOffset;

/* If shifts or shift diffs are included */
char DD_ExtractFlag;


/* SYNTAX: */
/* trimer <residue1> <from1> <to1> <residue2> <from2> <to2> <centralres> */
/* trimer <2-linked> <fr-2> <2> <n-linked> <fr-n> <to-n> <middle> */
int DD_SetDDelta()
{
  struct RE_Residue *res_lo, *res_hi, *res_cent, *res_temp;
  int pos_lo, pos_hi, cent_lo, cent_hi, pos_temp;
  char label_lo[TY_LABEL_LEN], label_hi[TY_LABEL_LEN];
  int stat;
  int index1, index2, index3, index4;
  char *config;
  struct DD_Delta *search;
  struct DE_Delta *dimer;

  DD_ExtractFlag=DD_NOEXTRACT;
  /* First (lo) residue */
  PA_GetString;
  res_lo=(struct RE_Residue *)FindNode((struct Node *)&ResidueList, PA_Result.String);
  if (res_lo==NULL)
    {
      Error(PA_ERR_FAIL, "First residue not found");
    }
  PA_GetString;
  pos_lo=TY_FindAtom(res_lo->Shifts.Type, PA_Result.String);
  if (pos_lo==TY_NOT_FOUND)
    {
      Error(PA_ERR_FAIL, "First position not found");
    }
  if (!(res_lo->Shifts.Type->Atom[pos_lo].Type&TY_FREE_POS))
    {
      Error(PA_ERR_FAIL,"First position not free");
    }
  PA_GetString;
  /* Must find residue before getting pos - save for later */
  strncpy(label_lo, PA_Result.String, TY_LABEL_LEN);
  /* Second (hi) residue */
  PA_GetString;
  res_hi=(struct RE_Residue *)FindNode((struct Node *)&ResidueList, PA_Result.String);
  if (res_hi==NULL)
    {
      Error(PA_ERR_FAIL, "Second residue not found");
    }
  PA_GetString;
  pos_hi=TY_FindAtom(res_hi->Shifts.Type, PA_Result.String);
  if (pos_hi==TY_NOT_FOUND)
    {
      Error(PA_ERR_FAIL, "Second position not found");
    }
  if (!(res_hi->Shifts.Type->Atom[pos_hi].Type&TY_FREE_POS))
    {
      Error(PA_ERR_FAIL,"Second position not free");
    }
  PA_GetString;
  /* Must find residue before getting pos - save for later */
  strncpy(label_hi, PA_Result.String, TY_LABEL_LEN);
  /* Third (cent) residue */
  PA_GetString;
  res_cent=(struct RE_Residue *)FindNode((struct Node *)&ResidueList, PA_Result.String);
  if (res_cent==NULL)
    {
      Error(PA_ERR_FAIL, "Third residue not found");
    }
  cent_lo=TY_FindAtom(res_cent->Shifts.Type, label_lo);
  if (cent_lo==TY_NOT_FOUND)
    {
      Error(PA_ERR_FAIL, "First position in central residue not found");
    }
  if (!(res_cent->Shifts.Type->Atom[cent_lo].Type&TY_FREE_POS))
    {
      Error(PA_ERR_FAIL, "First position in central residue not free");
    }

  cent_hi=TY_FindAtom(res_cent->Shifts.Type, label_hi);
  if (cent_hi==TY_NOT_FOUND)
    {
      Error(PA_ERR_FAIL, "Second position in central residue not found");
    }
  if (!(res_cent->Shifts.Type->Atom[cent_hi].Type&TY_FREE_POS))
    {
      Error(PA_ERR_FAIL, "Second position in central residue not free");
    }

  DD_DeltaOrder=DD_LO_HI;
  /* Lower position substituent is res_lo and higher position is res_hi */
  if (cent_lo>cent_hi)
    {
      pos_temp=cent_hi; cent_hi=cent_lo; cent_lo=pos_temp;
      pos_temp=pos_lo; pos_lo=pos_hi; pos_hi=pos_temp;
      res_temp=res_lo; res_lo=res_hi; res_hi=res_temp;
      DD_DeltaOrder=DD_HI_LO;
    }

  /* Findout what type of structure this is */
  /* 1) chirality */
  if (res_cent->Config==res_lo->Config)
    {
      if (res_cent->Config==res_hi->Config)
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
      if (res_cent->Config==res_hi->Config)
	{
	  config=DD_DIFF_SAME;
	}
      else
	{
	  config=DD_DIFF_DIFF;
	}
    }

  /* 2) anomeric configs */
  if (res_lo->HvyConf[pos_lo]==RE_AXIAL)
    {
      index1=DD_ANOMER_AX;
    }
  else
    {
      index1=DD_ANOMER_EQ;
    }
  if (res_hi->HvyConf[pos_hi]==RE_AXIAL)
    {
      index2=DD_ANOMER_AX;
    }
  else
    {
      index2=DD_ANOMER_EQ;
    }
  /* 3) central configs */
  if (res_cent->HvyConf[cent_lo]==RE_AXIAL)
    {
      index3=DD_AXIAL;
    }
  else
    {
      index3=DD_EQUATORIAL;
    }
  if (res_cent->HvyConf[cent_hi]==RE_AXIAL) index4=DD_AXIAL; else index4=DD_EQUATORIAL;
  /* Now find it */
  DD_Delta=NULL;
  search=(struct DD_Delta *)
    FindNode((struct Node *)&DD_Branch[index1][index2][index3][index4], config);
  while ((search!=NULL)&&(DD_Delta==NULL))
    {
      if ( (pos_lo==search->SubstPos[DD_FIRST])&&(pos_hi==search->SubstPos[DD_SECOND])&&
	   (cent_lo==search->CentralPos[DD_FIRST])&&(cent_hi==search->CentralPos[DD_SECOND]) )
	{
	  if ( (search->Residue[DD_FIRST]==res_lo) && (search->Residue[DD_SECOND]==res_hi) &&
	       (search->Residue[DD_CENTRAL]==res_cent) )
	    {
	      DD_Delta=search;
	    }
	}
      search=(struct DD_Delta *)FindNode((struct Node *)search, config);
    }
  /* Currently only searches for first hit */
  if (DD_Delta==NULL) /* Must create new deltadelta */
    {
      DD_Delta=(struct DD_Delta *) 
	ME_CreateNode(&DD_DeltaMethod, &(DD_Branch[index1][index2][index3][index4]), NULL);
      /* Must trick CreateNode not to find node with same name again */
      /* so that several corrections may be used according to fit */
      if (DD_Delta==NULL)
	{
	  Error(PA_ERR_FATAL,"Out of memory");
	}
      strcpy(DD_Delta->Node.Name, config);
    }
  DD_HvyOffset=0;

  if (DD_DeltaOrder==DD_LO_HI)
    {
      DD_DeltaType=DD_FIRST;
    }
  else
    {
      DD_DeltaType=DD_SECOND;
    }
  DD_Delta->Residue[DD_FIRST]=res_lo;
  DD_Delta->SubstPos[DD_FIRST]=pos_lo;
  DD_Delta->CentralPos[DD_FIRST]=cent_lo;
  DD_Delta->Residue[DD_SECOND]=res_hi;
  DD_Delta->SubstPos[DD_SECOND]=pos_hi;
  DD_Delta->CentralPos[DD_SECOND]=cent_hi;
  DD_Delta->Residue[DD_CENTRAL]=res_cent;
  DD_Delta->Error=0;
  DD_Delta->Shifts[DD_FIRST].Type=res_lo->Shifts.Type;
  DD_Delta->Shifts[DD_SECOND].Type=res_hi->Shifts.Type;
  DD_Delta->Shifts[DD_CENTRAL].Type=res_cent->Shifts.Type;

  stat=PA_Execute(DD_Cmds);

  /* Check if the monomers have to be subtracted */

  /* subtract monomer */
  if (DD_ExtractFlag==DD_EXTRACT)
    {
      SH_RAW_SUB( &(DD_Delta->Residue[DD_FIRST]->Shifts),&(DD_Delta->Shifts[DD_FIRST]) );
      SH_RAW_SUB( &(DD_Delta->Residue[DD_SECOND]->Shifts),&(DD_Delta->Shifts[DD_SECOND]) );
      SH_RAW_SUB( &(DD_Delta->Residue[DD_CENTRAL]->Shifts),&(DD_Delta->Shifts[DD_CENTRAL]) );

      /* should extract dimer SCS if not vicinal disubstituted */
      /* assumptions about anomeric pos? */
      if (cent_lo==res_cent->Shifts.Type->Anomeric)
	{
	  dimer=(struct DE_Delta *)DE_FindDelta(res_cent, cent_lo, pos_lo, res_lo);
	  if (!dimer)
	    {
	      Error(PA_ERR_FAIL, "Can't correct for linkage");
	    }
	  SH_RAW_SUB( &(dimer->Shifts[DE_NONRED]), &(DD_Delta->Shifts[DD_CENTRAL]));
	  SH_RAW_SUB( &(dimer->Shifts[DE_REDUCING]), &(DD_Delta->Shifts[DD_FIRST]));
	}
      if (cent_hi==res_cent->Shifts.Type->Anomeric)
	{
	  dimer=(struct DE_Delta *)DE_FindDelta(res_cent, cent_hi, pos_hi, res_hi);
	  if (!dimer)
	    {
	      Error(PA_ERR_FAIL, "Can't correct for linkage");
	    }
	  SH_RAW_SUB( &(dimer->Shifts[DE_NONRED]), &(DD_Delta->Shifts[DD_CENTRAL]));
	  SH_RAW_SUB( &(dimer->Shifts[DE_REDUCING]), &(DD_Delta->Shifts[DD_SECOND]));
	}
      if (pos_lo==res_lo->Shifts.Type->Anomeric)
	{
	  dimer=(struct DE_Delta *)DE_FindDelta(res_lo, pos_lo, cent_lo, res_cent);
	  if (!dimer)
	    {
	      Error(PA_ERR_FAIL, "Can't correct for linkage");
	    }
	  SH_RAW_SUB( &(dimer->Shifts[DE_NONRED]), &(DD_Delta->Shifts[DD_FIRST]));
	  SH_RAW_SUB( &(dimer->Shifts[DE_REDUCING]), &(DD_Delta->Shifts[DD_CENTRAL]));
	}
      if (pos_hi==res_hi->Shifts.Type->Anomeric)
	{
	  dimer=(struct DE_Delta *)DE_FindDelta(res_hi, pos_hi, cent_hi, res_cent);
	  if (!dimer)
	    {
	      Error(PA_ERR_FAIL, "Can't correct for linkage");
	    }
	  SH_RAW_SUB( &(dimer->Shifts[DE_NONRED]), &(DD_Delta->Shifts[DD_SECOND]));
	  SH_RAW_SUB( &(dimer->Shifts[DE_REDUCING]), &(DD_Delta->Shifts[DD_CENTRAL]));
	}
    }

  /* back to normal - should check that all shifts assigned */
  return(stat);
}

/* SYNTAX: Shift <C-shift> [<H-shift> ... ] */
int DD_Shift()
{
  unsigned char hcnt;
  /* time to change residue? */
  if (DD_HvyOffset>=DD_Delta->Shifts[(int)DD_DeltaType].Type->HeavyCnt)
    {
      DD_HvyOffset=0;
      if (DD_DeltaType==DD_CENTRAL)
	{
	  Error(PA_ERR_FAIL, "Too many signals");
	}
      if (DD_DeltaOrder==DD_LO_HI)
	{
	  if (DD_DeltaType==DD_FIRST)
	    {
	      DD_DeltaType=DD_SECOND;
	    }
	  else /* DD_DeltaType==DD_SECOND */
	    {
	      DD_DeltaType=DD_CENTRAL;
	    }
	}
      else /* DD_DeltaOrder==DD_HI_LO */
	{
	  if (DD_DeltaType==DD_FIRST)
	    {
	      DD_DeltaType=DD_CENTRAL;
	    }
	  else /* DD_DeltaType==DD_SECOND */
	    {
	      DD_DeltaType=DD_FIRST;
	    }
	}
    }
  /* Handle non-C heavy atom */
  if (DD_Delta->Shifts[(int)DD_DeltaType].Type->Atom[(int)DD_HvyOffset].Type&TY_SILENT)
    {
      PA_GetString;
      if (strcmp(PA_Result.String,"-")!=0)
	{
	  Error(PA_ERR_FAIL,"Atom can't have chemical shift");
	}
      DD_HvyOffset++;
      return(PA_ERR_OK);
    }
  PA_GetFloat;
  DD_Delta->Shifts[(int)DD_DeltaType].C[(int)DD_HvyOffset]=PA_Result.Float;
  for(hcnt=0; hcnt<DD_Delta->Shifts[(int)DD_DeltaType].Type->Atom[(int)DD_HvyOffset].HCnt; hcnt++)
    {
      PA_GetFloat;
      DD_Delta->Shifts[(int)DD_DeltaType].H[(int)DD_HvyOffset][hcnt]=PA_Result.Float;
    }
  DD_HvyOffset++;
  return(PA_ERR_OK);
}

/* SYNTAX: Extract */
/* Subtracts monomer shifts from all residues */
int DD_Extract()
{
  DD_ExtractFlag=DD_EXTRACT;
  return(PA_ERR_OK);
}

/* SYNTAX: Info <string> */
int DD_Info()
{
  PA_GetString;
  strncpy(DD_Delta->Info, PA_Result.String, DD_INFO_SIZE);
  return(PA_ERR_OK);
}


/* SYNTAX: Correct <coff> <hoff> */
int DD_Correct()
{
  float coff, hoff;
  PA_GetFloat;
  coff=PA_Result.Float;
  PA_GetFloat;
  hoff=PA_Result.Float;
  SH_Adjust(&(DD_Delta->Shifts[DD_FIRST]), coff, hoff);
  SH_Adjust(&(DD_Delta->Shifts[DD_SECOND]), coff, hoff);
  SH_Adjust(&(DD_Delta->Shifts[DD_CENTRAL]), coff, hoff);
  return(PA_ERR_OK);
}

/* SYNTAX: Error <error> */
int DD_Error()
{
  PA_GetFloat;
  DD_Delta->Error=PA_Result.Float;
  return(PA_ERR_OK);
}

/* format is <r1>(<f1>-><t1>)[<r2>(<f2>-><t2>)]<rc> */
/* or        <r1>(<f1>-><t1>)<rc>(<t2>-><f2>)<r2> */
int DD_ReadDelta(struct DD_TrimerDesc *desc, char *string)
{
  unsigned char i;
  char residue[NODE_NAME_LENGTH];
  char atom[NODE_NAME_LENGTH], atom2[NODE_NAME_LENGTH];
  struct RE_Residue *res1, *res2, *temp_r;
  int from1, to1, from2, to2, temp_p;

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
  res1=(struct RE_Residue *)FindNode((struct Node *)&ResidueList, residue);
  if (res1==NULL)
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
      from1=res1->Shifts.Type->Anomeric;
    }
  else
    {
      from1=TY_FindAtom(res1->Shifts.Type, atom);
      if (from1==TY_NOT_FOUND)
	{
	  Error(PA_ERR_FAIL, "First anomeric position not found");
	}
      if (from1!=res1->Shifts.Type->Anomeric)
	{
	  Error(PA_ERR_FAIL, "Not an anomeric position");
	}
    }
  if (*string=='-')
    {
      string++;
    }
  if (*string!='>')
    {
      Error(PA_ERR_FAIL, "Expecting >");
    }
  string++;
  /* second linkage position */
  for (i=0; (isalnum(*string)) && (i<NODE_NAME_LENGTH); i++)
    {
      atom[i]=*string; string++;
    }
  atom[i]=0;	/* terminate string */
  if (*string!=')')
    {
      Error(PA_ERR_FAIL, "Expecting )");;
    }
  string++;

  if (*string=='[')
    {
      string++;
      /* second residue */
      for (i=0; (isalnum(*string)) && (i<NODE_NAME_LENGTH); i++)
	{
	  residue[i]=*string; string++;
	}
      residue[i]=0;	/* terminate string */
      res2=(struct RE_Residue *)FindNode((struct Node *)&ResidueList, residue);
      if (res2==NULL)
	{
	  Error(PA_ERR_FAIL, "Second residue not found");
	}
      if (*string!='(')
	{
	  Error(PA_ERR_FAIL,"Expecting (");
	}
      string++;
      /* anomeric position */
      for (i=0; isalnum(*string) && (i<NODE_NAME_LENGTH); i++)
	{
	  atom2[i]=*string; string++; 
	}
      atom2[i]=0;	/* terminate string */
      if (i==0)
	{
	  from2=res2->Shifts.Type->Anomeric;
	}
      else
	{
	  from2=TY_FindAtom(res2->Shifts.Type, atom2);
	  if (from2==TY_NOT_FOUND)
	    {
	      Error(PA_ERR_FAIL, "Second anomeric position not found");
	    }
	  if (from2!=res2->Shifts.Type->Anomeric)
	    {
	      Error(PA_ERR_FAIL, "Not an anomeric position");
	    }
	}
      if (*string=='-')
	{
	  string++;
	}
      if (*string!='>')
	{
	  Error(PA_ERR_FAIL, "Expecting >");
	}
      string++;
      /* second linkage position */
      for (i=0; (isalnum(*string)) && (i<NODE_NAME_LENGTH); i++)
	{
	  atom2[i]=*string; string++;
	}
      atom2[i]=0;	/* terminate string */
      if (*string!=')')
	{
	  Error(PA_ERR_FAIL, "Expecting )");
	}
      string++;
      if (*string!=']')
	{
	  Error(PA_ERR_FAIL, "Expecting ]");
	}
      string++;
      /* now read central residue */
      for (i=0; (isalnum(*string)) && (i<NODE_NAME_LENGTH); i++)
	{
	  residue[i]=*string; string++;
	}
      residue[i]=0;	/* terminate string */
      desc->Residue[DD_CENTRAL]=(struct RE_Residue *)FindNode((struct Node *)&ResidueList, residue);
      if (desc->Residue[DD_CENTRAL]==NULL)
	{
	  Error(PA_ERR_FAIL, "Central residue not found");
	}
      to1=TY_FindAtom(desc->Residue[DD_CENTRAL]->Shifts.Type, atom);
      if (to1==TY_NOT_FOUND)
	{
	  Error(PA_ERR_FAIL, "First linkage position not found");
	}
      to2=TY_FindAtom(desc->Residue[DD_CENTRAL]->Shifts.Type, atom2);
      if (to2==TY_NOT_FOUND)
	{
	  Error(PA_ERR_FAIL, "Second linkage position not found");
	}
      /*  if (to1>to2) * reverse order? *
	  {
	  temp_p=to1; to1=to2; to2=temp_p;
	  temp_p=from1; from1=from2; from2=temp_p;
	  temp_r=res1; res1=res2; res2=temp_r;
	  };
	  desc->Residue[DD_FIRST]=res1;
	  desc->SubstAtom[DD_FIRST]=from1;
	  desc->CentrAtom[DD_FIRST]=to1;
	  desc->Residue[DD_SECOND]=res2;
	  desc->SubstAtom[DD_SECOND]=from2;
	  desc->CentrAtom[DD_SECOND]=to2; */
    }
  else
    {
      /* now read central residue */
      for (i=0; (isalnum(*string)) && (i<NODE_NAME_LENGTH); i++)
	{
	  residue[i]=*string; string++;
	}
      residue[i]=0;	/* terminate string */
      desc->Residue[DD_CENTRAL]=(struct RE_Residue *)FindNode((struct Node *)&ResidueList, residue);
      if (desc->Residue[DD_CENTRAL]==NULL)
	{
	  Error(PA_ERR_FAIL, "Central residue not found");
	}
      to1=TY_FindAtom(desc->Residue[DD_CENTRAL]->Shifts.Type, atom);
      if (to1==TY_NOT_FOUND)
	{
	  Error(PA_ERR_FAIL, "First linkage position not found");
	}
      /* now read next linkage */
      if (*string!='(')
	{
	  Error(PA_ERR_FAIL,"Expecting (");
	}
      string++;
      /* anomeric position */
      for (i=0; isalnum(*string) && (i<NODE_NAME_LENGTH); i++)
	{
	  atom[i]=*string; string++;
	}
      atom[i]=0;	/* terminate string */
      if (i==0)
	{
	  to2=desc->Residue[DD_CENTRAL]->Shifts.Type->Anomeric;
	}
      else
	{
	  to2=TY_FindAtom(desc->Residue[DD_CENTRAL]->Shifts.Type, atom);
	  if (to2==TY_NOT_FOUND)
	    {
	      Error(PA_ERR_FAIL, "Second anomeric position not found");
	    }
	  if (to2!=desc->Residue[DD_CENTRAL]->Shifts.Type->Anomeric)
	    {
	      Error(PA_ERR_FAIL, "Not an anomeric position");
	    }
	}
      if (*string=='-')
	{
	  string++;
	}
      if (*string!='>')
	{
	  Error(PA_ERR_FAIL, "Expecting >");
	}
      string++;
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
      /* read reducing end */
      for (i=0; (isalnum(*string)) && (i<NODE_NAME_LENGTH); i++)
	{
	  residue[i]=*string; string++;
	}
      residue[i]=0;	/* terminate string */
      res2=(struct RE_Residue *)FindNode((struct Node *)&ResidueList, residue);
      if (res2==NULL)
	{
	  Error(PA_ERR_FAIL, "Reducing residue not found");
	}
      from2=TY_FindAtom(res2->Shifts.Type, atom);
      if (from2==TY_NOT_FOUND)
	{
	  Error(PA_ERR_FAIL, "Second linkage position not found");
	}
    }
  if (to1>to2) /* reverse order? */
    {
      temp_p=to1; to1=to2; to2=temp_p;
      temp_p=from1; from1=from2; from2=temp_p;
      temp_r=res1; res1=res2; res2=temp_r;
    }
  desc->Residue[DD_FIRST]=res1;
  desc->SubstAtom[DD_FIRST]=from1;
  desc->CentrAtom[DD_FIRST]=to1;
  desc->Residue[DD_SECOND]=res2;
  desc->SubstAtom[DD_SECOND]=from2;
  desc->CentrAtom[DD_SECOND]=to2;

  return(PA_ERR_OK);
}

#ifdef qwerty
/* extracts dimer info from a string */
/* <res1>(<pos1>-><pos2>)<res2> */
/* <res1>(<ano1>-><pos1>)[<res2>(<ano2>-><pos2>)]<res3> */
/* <res1>(<ano1>-><pos1>)<res2>(<ano2>-><pos2>)<res3> */
int DE_ReadTrimer(struct DE_TrimerDesc *desc, char *string)
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
  desc->Residue[DD_FIRST]=(struct RE_Residue *)FindNode(&ResidueList, residue);
  if ( desc->Residue[DD_FIRST]==NULL)
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
      desc->Atom[DD_FIRST]=desc->Residue[DD_FIRST]->Shifts.Type->Anomeric;
    }
  else
    {
      desc->SubstAtom[DD_FIRST]=TY_FindAtom(desc->Residue[DD_FIRST]->Shifts.Type, atom);
      if (desc->SubstAtom[DD_FIRST]==TY_NOT_FOUND)
	{
	  Error(PA_ERR_FAIL, "Anomeric position not found");
	}
      if (desc->SubstAtom[DD_FIRST]!=desc->Residue[DD_FIRST]->Shifts.Type->Anomeric)
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
      Error(PA_ERR_FAIL, "Expecting )");;
    }
  string++;
  /* is next residue the 'central' residue? */
  if (*string!='[')
    {
      /* central residue */
      for (i=0; (isalnum(*string)) && (i<NODE_NAME_LENGTH); i++)
	{
	  residue[i]=*string; string++;
	}
      residue[i]=0;	/* terminate string */
      if (*string)
	{
	  Error(PA_ERR_FAIL, "Expecting central residue");
	}
      desc->Residue[DD_CENTRAL]=(struct RE_Residue *)FindNode(&ResidueList, residue);
      if (desc->Residue[DD_CENTRAL]==NULL)
	{
	  Error(PA_ERR_FAIL, "Central residue not found");
	}
      desc->CentrAtom[DD_FIRST]=TY_FindAtom(desc->Residue[DD_CENTRAL]->Shifts.Type, atom);
      if (desc->CentrAtom[DD_FIRST]==TY_NOT_FOUND)
	{
	  Error(PA_ERR_FAIL, "Second position not found");
	}
      if (*string!='(')
	{
	  Error(PA_ERR_FAIL,"Expecting (");
	}
      string++;
      /* anomeric position */
      for (i=0; isalnum(*string) && (i<NODE_NAME_LENGTH); i++)
	{
	  atom[i]=*string; string++;
	}
      atom[i]=0;	/* terminate string */
      if (i==0)
	{
	  desc->CentrAtom[DD_SECOND]=desc->Residue[DD_CENTRAL]->Shifts.Type->Anomeric;
	}
      else
	{
	  desc->CentrAtom[DD_SECOND]=TY_FindAtom(desc->Residue[DD_CENTRAL]->Shifts.Type, atom);
	  if (desc->CentrAtom[DD_SECOND]==TY_NOT_FOUND)
	    {
	      Error(PA_ERR_FAIL, "Anomeric position not found");
	    }
	  if (desc->CentrAtom[DD_SECOND]!=desc->Residue[DD_CENTRAL]->Shifts.Type->Anomeric)
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
	  Error(PA_ERR_FAIL, "Expecting )");;
	}
      string++;
      /* last residue */
      for (i=0; (isalnum(*string)) && (i<NODE_NAME_LENGTH); i++)
	{
	  residue[i]=*string; string++;
	}
      residue[i]=0;	/* terminate string */
      if (*string)
	{
	  Error(PA_ERR_FAIL, "Expecting reducing residue");
	}
      desc->Residue[DD_SECOND]=(struct RE_Residue *)FindNode(&ResidueList, residue);
      if (desc->Residue[DD_SECOND]==NULL)
	{
	  Error(PA_ERR_FAIL, "Reducing residue not found");
	}
      desc->SubstAtom[DD_SECOND]=TY_FindAtom(desc->Residue[DD_SECOND]->Shifts.Type, atom);
      if (desc->SubstAtom[DD_SECOND]==TY_NOT_FOUND)
	{
	  Error(PA_ERR_FAIL, "Last position not found");
	}
      printf("%s(%s->%s)%s(%s->%s)\n",
	     desc->Residue[DD_FIRST]->Node.Name,
	     desc->Residue[DD_FIRST]->Shifts.Type->Atom[desc->SubstAtom[DD_FIRST]].Label,
	     desc->Residue[DD_CENTRAL]->Shifts.Type->Atom[desc->CentrAtom[DD_FIRST]].Label,
	     desc->Residue[DD_CENTRAL]->Node.Name,
	     desc->Residue[DD_CENTRAL]->Shifts.Type->Atom[desc->CentrAtom[DD_SECOND]].Label,
	     desc->Residue[DD_SECOND]->Shifts.Type->Atom[desc->SubstAtom[DD_SECOND]].Label,
	     desc->Residue[DD_SECOND]->Node.Name);
    }
  else
    {
      puts("###");
    }
  return(PA_ERR_OK);
}

#endif
