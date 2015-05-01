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

/* Filename = spectra.c */
/* Prefix = SP_ */
/* functions for loading, sorting and assigning spectra */

#include <ctype.h>
#include <stdlib.h>
#include <math.h>
#include <string.h>
#include <ccp.h>

#include "build.h"
#include "gfx.h"
#include "parser.h"
#include "spectra.h"
#include "setup.h"
#include "ccpn.h"
#include "variables.h"
#include "gnuplot_i.h"
#include "memops/general/Util.h"


/* Methods for spectra */

/* List spectra and prints all the existing fits (errors) as well as 1JCH and 3JHH. */
void SP_ListSpectrum(const struct BU_Struct *spectrum)
{
  printf("%s",spectrum->Node.Name);
  if (spectrum->Info[0])
    printf(" - %s", spectrum->Info);
  if (spectrum->TotFit>0)
    printf("\tdTot=%.2f", spectrum->TotFit);
  if (spectrum->CFit>0)
    printf("\tdC=%.2f (%.2f)", spectrum->CFit, spectrum->CFit/spectrum->CCnt);
  if (spectrum->HFit>0)
    printf("\tdH=%.2f (%.2f)", spectrum->HFit, spectrum->HFit/spectrum->HCnt);
  if (spectrum->ChFit>0)
    printf("\tdCH=%.2f (%.2f)", spectrum->ChFit, spectrum->ChFit/spectrum->ChCnt);
  if (spectrum->HhFit>0)
    printf("\tdHH=%.2f (%.2f)", spectrum->HhFit, spectrum->HhFit/spectrum->HhCnt);
  
  printf("\t1JCH=%d/%d", spectrum->JCH[RE_SMALL], /* spectrum->JCH[RE_MEDIUM], */
	 spectrum->JCH[RE_LARGE]);
  printf("\t3JHH=%d/%d/%d", spectrum->JHH[RE_SMALL], spectrum->JHH[RE_MEDIUM],
	 spectrum->JHH[RE_LARGE]);
  puts("");
}

/* Prints 13C-, CH-, 1H-, HH-spectra if they exist. */
void SP_PrintSpectrum(const struct BU_Struct *spectrum)
{
  int i,j;
  printf("%s",spectrum->Node.Name);
  if (spectrum->Info[0]) printf(" - %s", spectrum->Info);
  puts("");
  if (spectrum->CCnt>0)	/* Show 13C-spectrum */
    {
      printf("13C signals (%d)\n",spectrum->CCnt);
      for(i=0, j=0; i<spectrum->CCnt; i++, j++)
	{
	  printf("%6.2f ",spectrum->CShift[i]);
	  if (j>(SP_ROW_WIDTH-2)) { puts(""); j=0; };
	};
      if (j!=0) puts("");
    };
  if (spectrum->ChCnt>0)	/* Show CH-correlation */
    {
      printf("CH cross peaks (%d)\n",spectrum->ChCnt);
      for(i=0, j=0; i<spectrum->ChCnt; i++)
	{
	  printf("%6.2f -%5.2f ",spectrum->ChCShift[i], spectrum->ChHShift[i]);
	  j+=2;
	  if (j>(SP_ROW_WIDTH-2)) { puts(""); j=0; };
	};
      if (j!=0) puts("");
    };
  if (spectrum->HCnt>0)	/* Show 1H-spectrum */
    {
      printf("1H signals (%d)\n",spectrum->HCnt);
      for(i=0, j=0; i<spectrum->HCnt; i++, j++)
	{
	  printf("%6.2f ",spectrum->HShift[i]);
	  if (j>(SP_ROW_WIDTH-2)) { puts(""); j=0; };
	};
      if (j!=0) puts("");
    };
  if (spectrum->HhCnt>0)	/* Show HH-cosy */
    {
      printf("HH cross peaks (%d)\n",spectrum->HhCnt);
      for(i=0, j=0; i<spectrum->HhCnt; i++)
	{
	  /*	  printf("%6.2f -%5.2f ",spectrum->HhLoShift[i], spectrum->HhHiShift[i]);*/
	  printf("%6.2f -%5.2f ",spectrum->HhHiShift[i], spectrum->HhLoShift[i]);
	  j+=2;
	  if (j>(SP_ROW_WIDTH-2)) { puts(""); j=0; };
	};
      if (j!=0) puts("");
    };
  
  printf("1JCH=%d/%d", spectrum->JCH[RE_SMALL], /* spectrum->JCH[RE_MEDIUM], */
	 spectrum->JCH[RE_LARGE]);
  printf("\t3JHH=%d/%d/%d\n", spectrum->JHH[RE_SMALL], spectrum->JHH[RE_MEDIUM],
	 spectrum->JHH[RE_LARGE]);
}

struct ME_Method SpectrumMethod={
  BU_CreateStruct, BU_ClearStruct, BU_FreeStruct,
  SP_ListSpectrum, SP_PrintSpectrum, /*SP_SaveSpectrum*/ NULL
};

/* end of methods */

/* Read experimental spectrum */
/* SYNTAX: Ldexp <type> <name> <filename>|'-'|'*'|'/' */
int SP_LoadExp()
{
  struct BU_Struct *spectrum;
  FILE *file=0;
  char line[PA_LINE_LEN];
  char *token, *token2, *token3, *flag, expType[32];
  float shift1, shift2;
  int i, follows, addnew, nDims, cnt;
  unsigned int mode;
  Nmr_DataSource ccpn_Spectrum=0;
  ApiSet experiments;
  ApiString projName;
  Nmr_Experiment experiment;

  if(projectValid && !Global_CcpnProject)
    {
      Global_CcpnProject = newCcpnProject("exp");
      if(!Global_CcpnProject)
	{
	  printf("Could not create CCPN project.\n");
	  printRaisedException();
	  return(EXIT_FAILURE);
	}
    }

  PA_GetString;
  mode=(toupper(PA_Result.String[0])<<8)+toupper(PA_Result.String[1]);
  PA_GetString;

  /* If a '/' is supplied instead of <name> just use the current nmr project
     in the ccpn project. It will also load the shifts of the specified type 
     from this project. */
  if(PA_Result.String[0]=='/')
    {
      if(projectValid)
	{
	  if(!CC_GetNmrProject(&Global_CcpnProject,&Global_NmrProject))
	    {
	      projectValid=0;
	      Error(PA_ERR_FATAL,"Cannot open project.");
	    }
	  
	  projName=Nmr_NmrProject_GetName(Global_NmrProject);
	  spectrum=(struct BU_Struct *)
	    ME_CreateNode(&StructMethod, &StructList,
			  ApiString_Get(projName));
	  ApiObject_Free(projName);

	  if(spectrum==NULL)
	    {
	      projectValid=0;
	      Error(PA_ERR_FATAL,"Out of memory");
	    }

	  if(!CC_LoadSpectra(&Global_NmrProject, mode, spectrum))
	    {
	      return(PA_ERR_OK);
	    }
	}
      else
	{
	  Error(PA_ERR_FATAL,"Project not valid");
	}
    }

  /* Otherwise open the nmr project with the specified name */
  else
    {
      if(projectValid)
	{
	  if(!CC_GetNmrProject_Named(&Global_CcpnProject,
				     &Global_NmrProject,PA_Result.String))
	    {
	      projectValid=0;
	      Error(PA_ERR_FATAL,"Cannot create project.");
	    }
	}

      spectrum=(struct BU_Struct *)
	ME_CreateNode(&StructMethod, &StructList,PA_Result.String);
      if (spectrum==NULL)
	{
	  Error(PA_ERR_FATAL,"Out of memory");
	}

      PA_GetString;

      /* If the there is a '/' after the <name> load the shifts from the spectra
	 in the ccpn nmr project. */
      if(PA_Result.String[0]=='/')
	{
	  if(projectValid)
	    {
	      if(!CC_LoadSpectra(&Global_NmrProject, mode, spectrum))
		{
		  return(PA_ERR_OK);
		}
	    }
	  /*	  else
	    {
	      Error(PA_ERR_FATAL,"Project not valid.");
	      }*/
	}
      else
	{
	  follows=0;
	  if (PA_Result.String[0]=='-')
	    {
	      file=stdin;
	      switch (mode) {
	      case SP_ID_PROTON:	puts("Enter 1H-shifts. Enter * to finish.");
		break;
	      case SP_ID_CARBON:	puts("Enter 13C-shifts. Enter * to finish.");
		break;
	      case SP_ID_2D_CH:	puts("Enter 13C-1H shift pairs. Enter * to finish.");
		puts("(Carbon before proton)");
		break;
	      case SP_ID_2D_HH:	puts("Enter 1H-1H shift pairs. Enter * to finish.");
		puts("Do NOT enter crosspeaks from both sides of diagonal!");
		break;
		
	      default:		Error(PA_ERR_FAIL,"Unknown type of spectrum");
	      };
	    }
	  else    /* Either a file or * is specified. * means that the user isn't prompted for input*/
	    {
	      if (PA_Result.String[0]=='*') { file=PA_Status->Input; follows=1;}
	      else file=fopen(PA_Result.String,"r");
	      if (file==NULL) Error(PA_ERR_FAIL,"Can't open file");
	    }
      
	  switch (mode)
	    {
	    case SP_ID_PROTON:	
	      spectrum->HCnt=0;
	      while (!feof(file))
		{
		  if (follows) PA_Status->LineNr++;
		  if (!fgets(line,SP_LINE_LEN-1,file)) break;
		  token=(char *)strtok(line," ,\t\n");
		  while (token!=NULL)
		    {
		      /* chk that this is a number */
		      if ( !isdigit(token[0]) ) break;
		      spectrum->HShift[spectrum->HCnt++]=atof(token);
		      /*	      printf("z %f\n",atof(token)); */
		      token=(char *)strtok(NULL," ,\t\n");
		    };
		  if (token==NULL) continue;
		  if (token[0]=='*') break; /* stop at '*' */
		};
	      break;
	    case SP_ID_CARBON: 
	      spectrum->CCnt=0;
	      while (!feof(file))
		{
		  if (follows) PA_Status->LineNr++;
		  if (!fgets(line,SP_LINE_LEN-1,file)) break;
		  token=(char *)strtok(line," ,\t\n");
		  while (token!=NULL)
		    {
		      /* chk that this is a number */
		      if ( !isdigit(token[0]) ) break;
		      spectrum->CShift[spectrum->CCnt++]=atof(token);
		      token=(char *)strtok(NULL," ,\t\n");
		    };
		  if (token==NULL) continue;
		  if (token[0]=='*') break; /* stop at '*' */
		};
	      break;
	      /* Reading a CH spectrum will also give the proton spectrum */
	      /* Include option to reverse order of C/H shifts? */
	    case SP_ID_2D_CH:
	      spectrum->ChCnt=0;
	      while (!feof(file))
		{
		  if (follows) PA_Status->LineNr++;
		  if (!fgets(line,SP_LINE_LEN-1,file)) break;
		  token=(char *)strtok(line," ,\t\n-");
		  token2=(char *)strtok(NULL," ,\t\n-");
		  while (/* (token!=NULL)&& */(token2!=NULL) )
		    {
		      /* chk that this is a number */
		      if ( !isdigit(token[0]) )
			{
			  break;
			}
		      if ( !isdigit(token2[0]) )
			{
			  Error(PA_ERR_FAIL,"One shift in pair missing");
			}
		      shift1=atof(token); shift2=atof(token2);
		  
		      spectrum->ChHShift[spectrum->ChCnt]=atof(token2);
		      spectrum->ChCShift[spectrum->ChCnt++]=atof(token);
		      token=(char *)strtok(NULL," ,\t\n-");
		      token2=(char *)strtok(NULL," ,\t\n-");
		    };
		  if (token==NULL)
		    {
		      continue;
		    }
		  if (token[0]=='*') break; /* stop at '*' */
		  if (token2==NULL&&isdigit(token[0]))
		    {
		      printf("Token=%s\tToken2=%s\n",token,token2);
		      Error(PA_ERR_FAIL,"One shift in pair missing");
		    }
		};
	      break;
	    case SP_ID_2D_HH:
	      spectrum->HhCnt=0;
	      while (!feof(file))
		{
		  if (follows) PA_Status->LineNr++;
		  if (!fgets(line,SP_LINE_LEN-1,file)) break;
		  token=(char *)strtok(line," ,\t\n-");
		  token2=(char *)strtok(NULL," ,\t\n-");
		  while (/* (token!=NULL)&& */(token2!=NULL) )
		    {
		      /* chk that this is a number */
		      if ( !isdigit(token[0]) ) break;
		      if ( !isdigit(token2[0]) )
			Error(PA_ERR_FAIL,"One shift in pair missing");
		      shift1=atof(token); shift2=atof(token2);
		      spectrum->HhLoShift[spectrum->HhCnt]=Min(shift1,shift2);
		      spectrum->HhHiShift[spectrum->HhCnt++]=Max(shift1,shift2);
		      token=(char *)strtok(NULL," ,\t\n-");
		      token2=(char *)strtok(NULL," ,\t\n-");
		    };
		  if (token==NULL) continue;
		  if (token[0]=='*') break; /* stop at '*' */
		  if (token2==NULL&&isdigit(token[0]))
		    Error(PA_ERR_FAIL,"One shift in pair missing");
		};
	      break;
	      
	    default:		Error(PA_ERR_FAIL,"Unknown type of spectrum");
	    }
	}
    }
  if (file!=stdin && PA_Result.String[0]!='/' && file!=PA_Status->Input)
    {
      fclose(file);
    }

  switch(mode)
    {
    case SP_ID_PROTON:
      cnt=spectrum->HCnt;
      break;
    case SP_ID_CARBON:
      cnt=spectrum->CCnt;
      break;
    case SP_ID_2D_CH:
      cnt=spectrum->ChCnt;
      break;
    case SP_ID_2D_HH:
      cnt=spectrum->HhCnt;
      break;
      
    }

  /* Sort both 1D and 2D spectra or
     if no shifts have been added (either from user input or from spectra 
     in the nmr project)
     skip sorting and putting the added peaks into the project */
  if(cnt>0)
    {
      if(spectrum)
	{
	  SP_Sort1DSpectra(spectrum);
	  SP_Sort2DSpectra(spectrum);
	}
      
      if(PA_Result.String[0]!='/' && projectValid)
	{
	  ccpn_Spectrum=CC_NewSpectrum(&Global_CcpnProject,&Global_NmrProject, mode);
	  addnew=1;
	}
      else if(projectValid)
	{
	  switch(mode)
	    {
	    case SP_ID_PROTON:
	      nDims=1;
	      strcpy(expType,"H");
	      break;
	    case SP_ID_CARBON:
	      nDims=1;
	      strcpy(expType,"C");
	      break;
	    case SP_ID_2D_CH:
	      nDims=2;
	      strcpy(expType,"H[C]");
	      break;
	    case SP_ID_2D_HH:
	      nDims=2;
	      strcpy(expType,"H_H.TOCSY");
	      break;
	      
	    }
	  experiments=CC_GetExpOfType(&Global_NmrProject,expType);
	  if(ApiSet_Len(experiments)>0 && projectValid)
	    {
	      experiment=ApiSet_Get(experiments,0);
	      if(!CC_GetProcSpectrumOfDim(&experiment, nDims, &ccpn_Spectrum))
		{
		  ccpn_Spectrum=CC_NewSpectrum(&Global_CcpnProject,&Global_NmrProject, mode);
		}
	      ApiObject_Free(experiment);
	    }
	  else if (projectValid)
	    {
	      ccpn_Spectrum=CC_NewSpectrum(&Global_CcpnProject,&Global_NmrProject, mode);	  
	    }
	  ApiObject_Free(experiments);
	  addnew=0;
	}
      
      /*      addnew=1;*/

      if(/*PA_Result.String[0]!='/' && */projectValid)
	{
	  switch(mode)
	    {
	    case SP_ID_PROTON:
	      CC_PutPeaks(&Global_NmrProject, &ccpn_Spectrum, spectrum->HShift,
			      0, spectrum->HCnt, mode, addnew, spectrum);
	      break;
	    case SP_ID_CARBON:
	      CC_PutPeaks(&Global_NmrProject, &ccpn_Spectrum, spectrum->CShift,
			      0, spectrum->CCnt, mode, addnew, spectrum);
	      break;
	    case SP_ID_2D_CH:
	      CC_PutPeaks(&Global_NmrProject, &ccpn_Spectrum, spectrum->ChCShift,
			  spectrum->ChHShift, spectrum->ChCnt, mode, addnew, spectrum);
	      break;
	    case SP_ID_2D_HH:
	      CC_PutPeaks(&Global_NmrProject, &ccpn_Spectrum, spectrum->HhHiShift,
			  spectrum->HhLoShift, spectrum->HhCnt, mode, addnew, spectrum);
	      break;
	      
	    }
	}
      for(i=0;i<cnt;i++)
	{
	  switch(mode)
	    {
	    case SP_ID_PROTON:
	      spectrum->HShiftAtom[i].unit=0;
	      spectrum->HShiftAtom[i].atom[0]=-1;
	      spectrum->HShiftAtom[i].atom[1]=-1;
	      break;
	    case SP_ID_CARBON:
	      spectrum->CShiftAtom[i].unit=0;
	      spectrum->CShiftAtom[i].atom[0]=-1;
	      spectrum->CShiftAtom[i].atom[1]=-1;
	      break;
	    case SP_ID_2D_CH:
	      spectrum->ChShiftAtom[i].unit=0;
	      spectrum->ChShiftAtom[i].atom[0]=-1;
	      spectrum->ChShiftAtom[i].atom[1]=-1;
	      break;
	    case SP_ID_2D_HH:
	      spectrum->HhLoShiftAtom[i].unit=0;
	      spectrum->HhHiShiftAtom[i].atom[0]=-1;
	      spectrum->HhHiShiftAtom[i].atom[1]=-1;
	      spectrum->HhLoShiftAtom[i].unit=0;
	      spectrum->HhHiShiftAtom[i].atom[0]=-1;
	      spectrum->HhHiShiftAtom[i].atom[1]=-1;
	      break;
	      
	    }
	}
    }
  if(ccpn_Spectrum && !ApiObject_IsNone(ccpn_Spectrum))
    {
      ApiObject_Free(ccpn_Spectrum);
    }

  return(PA_ERR_OK);
}

/* SYNTAX: ldassign <sim> <exp>
   Loads assignments of chemical shifts (resonances) to atoms from a CCPN
   project to the CASPER structure specified by sim. */
int SP_LoadAssignments()
{
  struct BU_Struct *sim, *exp;
  ApiString name;
  char simName[NODE_NAME_LENGTH], expName[NODE_NAME_LENGTH], simPrefix[NODE_NAME_LENGTH], expPrefix[NODE_NAME_LENGTH];
  int multiFlag=0, i=1;

  if(!projectValid)
    {
      Error(PA_ERR_FATAL,"Project not valid");
    }
  
  if(strncasecmp(PA_Status->CmdLine, "multildassign", PA_Status->CmdPtr-PA_Status->CmdLine)==0)
    {
      multiFlag=1;
    }

  /* Check that sim exists. */
  PA_GetString;
  strcpy(simPrefix, PA_Result.String);
  PA_GetString;

  if(PA_Result.String[0]=='/')
    {
      if(projectValid)
	{
	  if(!CC_GetNmrProject(&Global_CcpnProject,&Global_NmrProject))
	    {
	      Error(PA_ERR_FATAL,"Cannot open project.");
	    }
	  name=Nmr_NmrProject_GetName(Global_NmrProject);
	  strcpy(expPrefix, ApiString_Get(name));
      /*	  exp=(struct BU_Struct *)
	    FindNode((struct Node *)&StructList,
	    ApiString_Get(name));*/
	  ApiObject_Free(name);
	}
      /* Find the first structure that does not start with simPrefix */
      else
	{
	  for(exp=(struct BU_Struct *)StructList.Head.Succ; exp->Node.Succ!=NULL && strncasecmp(simPrefix, exp->Node.Name, strlen(simPrefix))==0;
	      exp=(struct BU_Struct *)exp->Node.Succ)
	    {
	    }
	  if(exp->Node.Succ!=NULL)
	    {
	      if(multiFlag)
		{
		  strncpy(expPrefix, exp->Node.Name, strlen(exp->Node.Name)-1);
		  expPrefix[strlen(exp->Node.Name)-1]=0;
		}
	      else
		{
		  strcpy(expPrefix, exp->Node.Name);
		}
	    }
	  else
	    {
	      Error(PA_ERR_FATAL,"Experimental structure not found and/or project not valid.");
	    }
	}
    }
  else
    {
      strcpy(expPrefix, PA_Result.String);
    }

  if(multiFlag)
    {
      sprintf(simName, "%s%d", simPrefix, i);
      sprintf(expName, "%s%d", expPrefix, i++);
    }
  else
    {
      strcpy(simName, simPrefix);
      strcpy(expName, expPrefix);
    }
  sim=(struct BU_Struct *)FindNode((struct Node *)&StructList, simName);
  if (sim==NULL)
    {
      Error(PA_ERR_FAIL,"No such simulated spectrum");
    }
  
  exp=(struct BU_Struct *)ME_CreateNode(&StructMethod, &StructList, expName);
  if (exp==NULL)
    {
      Error(PA_ERR_FAIL,"No such experimental spectrum");
    }

  while (sim && exp)
    {
      BU_CopyStructure(exp, sim);
      strcpy(exp->CcpnChainCode, sim->CcpnChainCode);
      
      CC_LoadAssignments(&Global_NmrProject, exp);
      SP_Sort1DSpectra(exp);
      
      if(!multiFlag)
	{
	  SP_CalculateCorrections(sim, exp);
	}

      if(!multiFlag)
	{
	  break;
	}

      sprintf(simName, "%s%d", simPrefix, i);
      sprintf(expName, "%s%d", expPrefix, i++);
      sim=(struct BU_Struct *)FindNode((struct Node *)&StructList, simName);
      if(sim)
	{
	  exp=(struct BU_Struct *)ME_CreateNode(&StructMethod, &StructList, expName);
	}
    }
  if(multiFlag)
    {
      SP_CalculateMultipleCorrections(simPrefix, expPrefix);
    }

  return(PA_ERR_OK);
}

/* Completely migrates simulated shifts of the structure to the experimental shifts.
   The shifts are copied atom by atom and also added to CShifts, HShifts and ChShifts.
   Atoms which have already been assigned an experimental shift are not changed. */
int SP_MigrateSimulatedAssignments()
{
  struct BU_Struct *sim, *exp;
  struct BU_Unit *eUnit, *sUnit;
  ApiString name;
  int i, j, k=1, cFlag, hFlag[2], multiFlag=0;
  char simName[NODE_NAME_LENGTH], expName[NODE_NAME_LENGTH], simPrefix[NODE_NAME_LENGTH], expPrefix[NODE_NAME_LENGTH];

  if(strncasecmp(PA_Status->CmdLine, "multimigrateassign", PA_Status->CmdPtr-PA_Status->CmdLine)==0)
    {
      multiFlag=1;
    }

  PA_GetString;
  strcpy(simPrefix, PA_Result.String);
  PA_GetString;

  if(PA_Result.String[0]=='/')
    {
      if(projectValid)
	{
	  if(!CC_GetNmrProject(&Global_CcpnProject,&Global_NmrProject))
	    {
	      Error(PA_ERR_FATAL,"Cannot open project.");
	    }
	  name=Nmr_NmrProject_GetName(Global_NmrProject);
	  strcpy(expPrefix, ApiString_Get(name));
      /*	  exp=(struct BU_Struct *)
	    FindNode((struct Node *)&StructList,
	    ApiString_Get(name));*/
	  ApiObject_Free(name);
	}
      /* Find the first structure that does not start with simPrefix */
      else
	{
	  for(exp=(struct BU_Struct *)StructList.Head.Succ; exp->Node.Succ!=NULL && strncasecmp(simPrefix, exp->Node.Name, strlen(simPrefix))==0;
	      exp=(struct BU_Struct *)exp->Node.Succ)
	    {
	    }
	  if(exp->Node.Succ!=NULL)
	    {
	      if(multiFlag)
		{
		  strncpy(expPrefix, exp->Node.Name, strlen(exp->Node.Name)-1);
		  expPrefix[strlen(exp->Node.Name)-1]=0;
		}
	      else
		{
		  strcpy(expPrefix, exp->Node.Name);
		}
	    }
	  else
	    {
	      Error(PA_ERR_FATAL,"Experimental structure not found and/or project not valid.");
	    }
	}
    }
  else
    {
      strcpy(expPrefix, PA_Result.String);
    }

  if(multiFlag)
    {
      sprintf(simName, "%s%d", simPrefix, k);
      sprintf(expName, "%s%d", expPrefix, k++);
    }
  else
    {
      strcpy(simName, simPrefix);
      strcpy(expName, expPrefix);
    }
  /* Check that sim and exp exist. */
  sim=(struct BU_Struct *)FindNode((struct Node *)&StructList, simName);
  if (sim==NULL)
    {
      Error(PA_ERR_FAIL,"No such simulated spectrum");
    }
  
  exp=(struct BU_Struct *)ME_CreateNode(&StructMethod, &StructList, expName);
  if (exp==NULL)
    {
      Error(PA_ERR_FAIL,"No such experimental spectrum");
    }

  while (sim && exp)
    {
      for(eUnit=(struct BU_Unit *)exp->Units.Head.Succ,
	    sUnit=(struct BU_Unit *)sim->Units.Head.Succ;
	  eUnit->Node.Succ!=NULL && sUnit->Node.Succ!=NULL;
	  eUnit=(struct BU_Unit *)eUnit->Node.Succ,
	    sUnit=(struct BU_Unit *)sUnit->Node.Succ)
	{
	  for(i=0;i<sUnit->Shifts.Type->HeavyCnt && i<eUnit->Shifts.Type->HeavyCnt;i++)
	    {
	      cFlag=0;
	      hFlag[0]=0;
	      hFlag[1]=0;
	      if(eUnit->Shifts.C[i]<=0.0001 && sUnit->Shifts.C[i]>0.0001)
		{
		  eUnit->Shifts.C[i]=sUnit->Shifts.C[i];
		}
	      for(j=0; j<2; j++)
		{
		  if(eUnit->Shifts.H[i][j]<=0.0001 && sUnit->Shifts.H[i][j]>0.0001)
		    {
		      eUnit->Shifts.H[i][j]=sUnit->Shifts.H[i][j];
		    }
		}
	    }
	}
      exp->CCnt=0;
      exp->HCnt=0;
      exp->ChCnt=0;
      exp->HhCnt=0;
      
      
      SP_Calc1DSpectra(exp);
      SP_Calc2DSpectra(exp);
      
      SP_Sort1DSpectra(exp);
      SP_Sort2DSpectra(exp);
      
      /* This is removed for now. Don't add the peaks that were migrated from simulated spectra. They'll be added to
	 simulated shift list during assignments. */
      /*      if(projectValid)
	{
	  CC_AllPeaksInSpectra(&Global_CcpnProject, &Global_NmrProject, exp);
	}
      */

      if(!multiFlag)
	{
	  break;
	}

      sprintf(simName, "%s%d", simPrefix, k);
      sprintf(expName, "%s%d", expPrefix, k++);
      sim=(struct BU_Struct *)FindNode((struct Node *)&StructList, simName);
      if(sim)
	{
	  exp=(struct BU_Struct *)ME_CreateNode(&StructMethod, &StructList, expName);
	}
    }
      
  return (PA_ERR_OK);
}


/*****************************************************************/
/*                     variables                                 */
/*****************************************************************/

char SP_AssignMode=SP_AM_CUTOFF;	/* What kind of sorting */


/*****************************************************************/
/*                     internal fn:s                             */
/*****************************************************************/

/* descending order */
int SP_Compare(const void *a, const void *b)
{
  if (*((const float **)a)>*((const float **)b)) return (-1);
  if (*((const float **)a)<*((const float **)b)) return (1);
  return (0);
}

/* Sort spectrum */
void SP_Sort1DSpectra(struct BU_Struct *spectrum)
{
  SP_QuickSortSpectra(spectrum, 0, spectrum->CCnt-1, SE_PURGE_C);

  SP_QuickSortSpectra(spectrum, 0, spectrum->HCnt-1, SE_PURGE_H);
}

/* Sorts 2D ChShifts on the distance from origo */
void SP_Sort2DSpectra(struct BU_Struct *spectrum)
{
  SP_QuickSortSpectra(spectrum, 0, spectrum->ChCnt-1, SE_PURGE_CH);

  SP_QuickSortSpectra(spectrum, 0, spectrum->HhCnt-1, SE_PURGE_HH);
  
}

/* Quick sorting spectra (recursive sorting of partitions) gives a major speed increase over bubble sort. */
void SP_QuickSortSpectra(struct BU_Struct *str, int lPtr, int rPtr, const unsigned int mode)
{
  int mPtr;

  if(lPtr>=rPtr)
    {
      return;
    }

  /* Divide the values in two partitions. In one partition all values are
     lower than mPtr and in the other partition all values are higher than
     the value of mPtr */
  mPtr = SP_SpectraSortPartition(str, lPtr, rPtr, mode);

  if(lPtr==rPtr-1)
    {
      return;
    }

  if(lPtr<mPtr-1)
    {
      /* Sort the values before current pivot point by recursively calling this function */
      SP_QuickSortSpectra(str, lPtr, mPtr-1, mode);
    }

  if(rPtr>mPtr+1)
    {
      /* Sort the values after current pivot point by recursively calling this function */
      SP_QuickSortSpectra(str, mPtr+1, rPtr, mode);
    }
  
}

/* This is used by Quick sorting to sort a partition. It returns a pivot point. The elements are placed so that
   all values higher than the pivot are placed before (the chemical shifts are sorted from highest to lowest)
   it in the list. All values lower than the pivot is placed after it. */
int SP_SpectraSortPartition(struct BU_Struct *str, int lPtr, int rPtr, const unsigned int mode)
{
  int i, j, pivot;
  float *xShifts, *yShifts;

  switch(mode)
    {
    case SE_PURGE_C:
      xShifts=str->CShift;
      yShifts=0;
      break;
    case SE_PURGE_H:
      xShifts=str->HShift;
      yShifts=0;
      break;
    case SE_PURGE_CH:
      xShifts=str->ChCShift;
      yShifts=str->ChHShift;
      break;
    case SE_PURGE_HH:
      xShifts=str->HhHiShift;
      yShifts=str->HhLoShift;
      break;
      
    }

  /* If there are only two elements to compare check them quickly and swap them
     if necessary */
  if(lPtr==rPtr-1)
    {
      if(xShifts[lPtr]<xShifts[rPtr] || (yShifts && fabs(xShifts[lPtr]-xShifts[rPtr])<0.00001 && 
					 yShifts[lPtr]<yShifts[rPtr]))
	{
	  SP_SortSwapShifts(str, lPtr, rPtr, mode);
	}
      return lPtr;
    }

  /* Select a pivot point in the middle */
  pivot=(lPtr+rPtr)/2;

  i=lPtr;
  j=rPtr;

  while(1)
    {
      while((xShifts[j]<xShifts[pivot] || 
	     (yShifts && fabs(xShifts[j]-xShifts[pivot])<0.00001 &&
	      yShifts[j]<yShifts[pivot])) &&
	    j!=pivot)
	{
	  j--;
	  if(i==j)
	    {
	      return pivot;
	    }
	}
      while((xShifts[i]>xShifts[pivot] || 
	     (yShifts && fabs(xShifts[i]-xShifts[pivot])<0.00001 && 
	      yShifts[i]>=yShifts[pivot])) && 
	    i!=pivot)
	{
	  i++;
	  if(i==j)
	    {
	      return pivot;
	    }
	}
      /* If i has reached pivot, but j still needs to be moved it has to be
	 squeezed in before the pivot and all subsequent shifts have to be
	 moved */
      if(i==pivot)
	{
	  SP_SortInsertShift(str, j, i, mode);

	  pivot++;
	  i++;
	  j++;
	}
      /* Move i to right after the pivot */
      else if(j==pivot)
	{
	  SP_SortInsertShift(str, i, j+1, mode);

	  pivot--;
	  j--;
	  i--;
	}
      if(i!=pivot && j!=pivot)
	{
	  SP_SortSwapShifts(str, i, j, mode);
	}
      /* Go to next elements */
      if(i<pivot)
	{
	  i++;
	}
      if(j>pivot)
	{
	  j--;
	}
      if(i==j)
	{
	  return pivot;
	}
    }
}

/* This function just swaps swaps x and y chemical shifts. */
void SP_SortSwapShifts(struct BU_Struct *str, int i, int j, const unsigned int mode)
{
  float *xShifts, *yShifts, tempShift;
  struct BU_ShiftToAtom *xAssignments, *yAssignments, tempAssignment;
  int *ranges, rangeTemp;

  switch(mode)
    {
    case SE_PURGE_C:
      xShifts=str->CShift;
      yShifts=0;
      xAssignments=str->CShiftAtom;
      yAssignments=0;
      ranges=0;
      break;
    case SE_PURGE_H:
      xShifts=str->HShift;
      yShifts=0;
      xAssignments=str->HShiftAtom;
      yAssignments=0;
      ranges=0;
      break;
    case SE_PURGE_CH:
      xShifts=str->ChCShift;
      yShifts=str->ChHShift;
      xAssignments=str->ChShiftAtom;
      yAssignments=0;
      ranges=0;
      break;
    case SE_PURGE_HH:
      xShifts=str->HhHiShift;
      yShifts=str->HhLoShift;
      xAssignments=str->HhHiShiftAtom;
      yAssignments=str->HhLoShiftAtom;
      ranges=0;
      break;
      
    }

  tempShift=xShifts[i];
  xShifts[i]=xShifts[j];
  xShifts[j]=tempShift;
  BU_COPYSHIFTATOMASSIGNMENT(tempAssignment,xAssignments[i]);
  BU_COPYSHIFTATOMASSIGNMENT(xAssignments[i],xAssignments[j]);
  BU_COPYSHIFTATOMASSIGNMENT(xAssignments[j],tempAssignment);
  if(yShifts)
    {
      tempShift=yShifts[i];
      yShifts[i]=yShifts[j];
      yShifts[j]=tempShift;
      if(yAssignments)
	{
	  BU_COPYSHIFTATOMASSIGNMENT(tempAssignment,yAssignments[i]);
	  BU_COPYSHIFTATOMASSIGNMENT(yAssignments[i],yAssignments[j]);
	  BU_COPYSHIFTATOMASSIGNMENT(yAssignments[j],tempAssignment);
	}
      if(ranges)
	{
	  rangeTemp=ranges[i];
	  ranges[i]=ranges[j];
	  ranges[j]=rangeTemp;
	}
    }
}

/* This function moves a chemical shift from its current position to a new one. Other shifts
   are moved to fill the empty gap. It moves the shift at position origPos to before
   position toBefore.*/
void SP_SortInsertShift(struct BU_Struct *str, int origPos, int toBefore, const unsigned int mode)
{
  float *xShifts, *yShifts, xTemp, yTemp;
  int *ranges, rangeTemp, i;
  struct BU_ShiftToAtom *xAssignments, *yAssignments, tempXAss, tempYAss;

  switch(mode)
    {
    case SE_PURGE_C:
      xShifts=str->CShift;
      yShifts=0;
      xAssignments=str->CShiftAtom;
      yAssignments=0;
      ranges=0;
      break;
    case SE_PURGE_H:
      xShifts=str->HShift;
      yShifts=0;
      xAssignments=str->HShiftAtom;
      yAssignments=0;
      ranges=0;
      break;
    case SE_PURGE_CH:
      xShifts=str->ChCShift;
      yShifts=str->ChHShift;
      xAssignments=str->ChShiftAtom;
      yAssignments=0;
      ranges=0;
      break;
    case SE_PURGE_HH:
      xShifts=str->HhHiShift;
      yShifts=str->HhLoShift;
      xAssignments=str->HhHiShiftAtom;
      yAssignments=str->HhLoShiftAtom;
      ranges=0;
      break;
      
    }


  xTemp=xShifts[origPos];
  BU_COPYSHIFTATOMASSIGNMENT(tempXAss,xAssignments[origPos]);
  if(yShifts)
    {
      yTemp=yShifts[origPos];
      if(yAssignments)
	{
	  BU_COPYSHIFTATOMASSIGNMENT(tempYAss,yAssignments[origPos]);
	}
      if(ranges)
	{
	  rangeTemp=ranges[origPos];
	}
    }

  if(origPos>toBefore)
    {
      for(i=origPos;i>toBefore;i--)
	{
	  xShifts[i]=xShifts[i-1];
	  BU_COPYSHIFTATOMASSIGNMENT(xAssignments[i],xAssignments[i-1]);
	  if(yShifts)
	    {
	      yShifts[i]=yShifts[i-1];
	      if(yAssignments)
		{
		  BU_COPYSHIFTATOMASSIGNMENT(yAssignments[i],yAssignments[i-1]);
		}
	      if(ranges)
		{
		  ranges[i]=ranges[i-1];
		}
	    }
	}
    }
  else
    {
      for(i=origPos;i<toBefore-1;i++)
	{
	  xShifts[i]=xShifts[i+1];
	  BU_COPYSHIFTATOMASSIGNMENT(xAssignments[i],xAssignments[i+1]);
	  if(yShifts)
	    {
	      yShifts[i]=yShifts[i+1];
	      if(yAssignments)
		{
		  BU_COPYSHIFTATOMASSIGNMENT(yAssignments[i],yAssignments[i+1]);
		}
	      if(ranges)
		{
		  ranges[i]=ranges[i+1];
		}
	    }
	}      
    }

  if(origPos<toBefore)
    {
      toBefore--;
    }
  xShifts[toBefore]=xTemp;
  BU_COPYSHIFTATOMASSIGNMENT(xAssignments[toBefore],tempXAss);
  if(yShifts)
    {
      yShifts[toBefore]=yTemp;
      if(yAssignments)
	{
	  BU_COPYSHIFTATOMASSIGNMENT(yAssignments[toBefore],tempYAss);
	}
      if(ranges)
	{
	  ranges[toBefore]=rangeTemp;
	}
    }
}
/* Calculate 1D spectra */
void  SP_Calc1DSpectra(struct BU_Struct *spectrum)
{
  struct BU_Unit *unit;
  int i,j;
  spectrum->Error=0;	/* For now.. */
  spectrum->CFit=0;
  spectrum->HFit=0;
  spectrum->CCnt=0;
  spectrum->HCnt=0;
  /* some question marks such as First? */
  for(unit=(struct BU_Unit *)First(spectrum->Units);
      unit->Node.Succ!=NULL; unit=(struct BU_Unit *)unit->Node.Succ)
    {
      spectrum->Error+=unit->Error;
      for (i=0; i<unit->Residue->Shifts.Type->HeavyCnt; i++)
	{
	  if(!(unit->Residue->Shifts.Type->Atom[i].Type&TY_SILENT))
	    {
	      spectrum->CShift[spectrum->CCnt]=unit->Shifts.C[i];
	      spectrum->CShiftAtom[spectrum->CCnt].unit=unit;
	      spectrum->CShiftAtom[spectrum->CCnt].atom[0]=i;
	      spectrum->CShiftAtom[spectrum->CCnt++].atom[1]=0;
	      for (j=0; j<unit->Residue->Shifts.Type->Atom[i].HCnt; j++)
		{
		  spectrum->HShift[spectrum->HCnt]=
		    unit->Shifts.H[i][j];
		  spectrum->HShiftAtom[spectrum->HCnt].unit=unit;
		  spectrum->HShiftAtom[spectrum->HCnt].atom[0]=i;
		  spectrum->HShiftAtom[spectrum->HCnt++].atom[1]=j;
		}
	    }
	}
    }
  /* After having calculated the shifts sort them */
  SP_Sort1DSpectra(spectrum);
}

/* Calculate 2D (CH and HH) spectra. */
void SP_Calc2DSpectra(struct BU_Struct *spectrum)
{
  struct BU_Unit *unit;
  struct TY_Type *type;
  unsigned int i,k,l,m;
  spectrum->Error=0;	/* For now.. */
  spectrum->ChFit=0;
  spectrum->HhFit=0;
  spectrum->ChCnt=0;
  spectrum->HhCnt=0;
  /* some question marks such as First? */
  for(unit=(struct BU_Unit *)First(spectrum->Units);
      unit->Node.Succ!=NULL; unit=(struct BU_Unit *)unit->Node.Succ)
    {
      type=unit->Residue->Shifts.Type;
      for (i=0; i<type->HeavyCnt; i++)
	{
	  if(!(type->Atom[i].Type&TY_SILENT))
	    {
	      for (k=0; k<type->Atom[i].HCnt; k++)
		{
		  /* HSQC/HETCOR */
		  spectrum->ChCShift[spectrum->ChCnt]=unit->Shifts.C[i];
		  spectrum->ChHShift[spectrum->ChCnt]=unit->Shifts.H[i][k];
		  spectrum->ChShiftAtom[spectrum->ChCnt].unit=unit;
		  spectrum->ChShiftAtom[spectrum->ChCnt].atom[0]=i;
		  spectrum->ChShiftAtom[spectrum->ChCnt++].atom[1]=k;

		  /* HH-cosy */
		  
		  /* Only use the ring atoms - no couplings to e.g. OMe and NAc */
		  if(type->Atom[i].Label[0]>='1' && type->Atom[i].Label[0]<='9')
		    {
		      /* loop through all protons on carbon i */
		      /* geminal couplings */
		      for (l=k+1; l<type->Atom[i].HCnt; l++)
			{
			  spectrum->HhLoShift[spectrum->HhCnt]=
			    Min(unit->Shifts.H[i][k], unit->Shifts.H[i][l]);
			  spectrum->HhHiShift[spectrum->HhCnt]=
			    Max(unit->Shifts.H[i][k], unit->Shifts.H[i][l]);
			  spectrum->HhLoShiftAtom[spectrum->HhCnt].unit=unit;
			  spectrum->HhHiShiftAtom[spectrum->HhCnt].unit=unit;
			  spectrum->HhLoShiftAtom[spectrum->HhCnt].atom[0]=i;
			  spectrum->HhHiShiftAtom[spectrum->HhCnt].atom[0]=i;
			  if(unit->Shifts.H[i][k]<unit->Shifts.H[i][l])
			    {
			      spectrum->HhLoShiftAtom[spectrum->HhCnt].atom[1]=k;
			      spectrum->HhHiShiftAtom[spectrum->HhCnt++].atom[1]=l;
			    }
			  else
			    {
			      spectrum->HhLoShiftAtom[spectrum->HhCnt].atom[1]=l;
			      spectrum->HhHiShiftAtom[spectrum->HhCnt++].atom[1]=k;	      
			    }
			}
		      /* Loop through all protons on higher numbered carbons. The whole spin system will be
			 calculated, even if most peaks usually cannot be found. But this way the mixing
			 time of the TOCSY spectra does not matter. */
		      for(l=i+1;l<type->HeavyCnt;l++)
			{
			  /* Only use the ring atoms - no couplings to e.g. OMe and NAc */
			  if(type->Atom[l].Label[0]>='1' && type->Atom[l].Label[0]<='9' &&
			     !(type->Atom[l].Type&TY_SILENT))
			    {
			      for(m=0;m<type->Atom[l].HCnt;m++)
				{
				  spectrum->HhHiShift[spectrum->HhCnt]=
				    Max(unit->Shifts.H[i][k], unit->Shifts.H[l][m]);
				  spectrum->HhLoShift[spectrum->HhCnt]=
				    Min(unit->Shifts.H[i][k], unit->Shifts.H[l][m]);
				  spectrum->HhLoShiftAtom[spectrum->HhCnt].unit=unit;
				  spectrum->HhHiShiftAtom[spectrum->HhCnt].unit=unit;
				  if(unit->Shifts.H[i][k]<unit->Shifts.H[l][m])
				    {
				      spectrum->HhLoShiftAtom[spectrum->HhCnt].atom[0]=i;
				      spectrum->HhHiShiftAtom[spectrum->HhCnt].atom[0]=l;
				      spectrum->HhLoShiftAtom[spectrum->HhCnt].atom[1]=k;
				      spectrum->HhHiShiftAtom[spectrum->HhCnt++].atom[1]=m;
				    }
				  else
				    {   
				      spectrum->HhLoShiftAtom[spectrum->HhCnt].atom[0]=l;
				      spectrum->HhHiShiftAtom[spectrum->HhCnt].atom[0]=i;
				      spectrum->HhLoShiftAtom[spectrum->HhCnt].atom[1]=m;
				      spectrum->HhHiShiftAtom[spectrum->HhCnt++].atom[1]=k;
				    }
				}
			    }
			}
		    }
		}
	    }
	}
    }
  SP_Sort2DSpectra(spectrum);
}


/* Calculates the distance between two 2D positions. If only one 2D position
   is supplied the distance to origo is calculated. */
/* The switch statement looks a bit cumbersome, but is time optimised. */
float SP_2DDelta(const struct BU_Struct *spec1, const unsigned int pos1, 
		 const struct BU_Struct *spec2, const unsigned int pos2,
		 const unsigned int mode)
{
  float dx, dy;

  switch(mode)
    {
    case SE_PURGE_CH:
      if(spec2!=0)
	{
	  dx=spec1->ChCShift[pos1]-spec2->ChCShift[pos2];
	  dy=spec1->ChHShift[pos1]-spec2->ChHShift[pos2];
	}
      else
	{
	  dx=spec1->ChCShift[pos1];
	  dy=spec1->ChHShift[pos1];
	}
      return(sqrt(SP_C_SCALING*dx*dx+/*SP_H_Scaling*SP_H_Scaling**/dy*dy));
    case SE_PURGE_HH:
      if(spec2!=0)
	{
	  dx=spec1->HhLoShift[pos1]-spec2->HhLoShift[pos2];
	  dy=spec1->HhHiShift[pos1]-spec2->HhHiShift[pos2];
	}
      else
	{
	  dx=spec1->HhLoShift[pos1];
	  dy=spec1->HhHiShift[pos1];
	}
      return(sqrt(dx*dx+dy*dy));
      
    default:
      return 0;
    }
}


/* Compares the simulated spectrum to the experimental spectrum and sets
   the relevant fit values in the structures to show how well they
   match. */
void SP_RawCompare(struct BU_Struct *exp, struct BU_Struct *sim,
		   const unsigned int mode)
{
  if((mode==0 || mode==SE_PURGE_C || mode==SE_PURGE_TOT) && exp->CCnt>0)
    {
      if(SP_AssignMode==SP_AM_CUTOFF || sim->CCnt>=75 || (float)sim->nCAssignments/sim->CCnt>0.80f || ((float)exp->CCnt/sim->CCnt<0.75f && (float)exp->CCnt/sim->CCnt>0.15f))
	{
	  sim->CFit=SP_AssExpSimQuick(exp,sim,SE_PURGE_C,0,0);	  
	}
      else
	{
	  sim->CFit=SP_AssExpSim(exp,sim,SE_PURGE_C,0,0);
	}
    }
  if((mode==0 || mode==SE_PURGE_H || mode==SE_PURGE_TOT) && exp->HCnt>0)
    {
      if(SP_AssignMode==SP_AM_CUTOFF || sim->HCnt>=75 || (float)sim->nHAssignments/sim->HCnt>0.80f || ((float)exp->HCnt/sim->HCnt<0.75f && (float)exp->HCnt/sim->HCnt>0.15f))
	{
	  sim->HFit=SP_AssExpSimQuick(exp,sim,SE_PURGE_H,0,0);
	}
      else
	{
	  sim->HFit=SP_AssExpSim(exp,sim,SE_PURGE_H,0,0);
	}
    }
  sim->TotFit=SP_C_WEIGHT*sim->CFit+SP_H_WEIGHT*sim->HFit+SP_CH_WEIGHT*sim->ChFit+
    SP_HH_WEIGHT*sim->HhFit;
  
  return;
}

void SP_Raw2DCompare(struct BU_Struct *exp, struct BU_Struct *sim, const unsigned int mode)
{
  /* If SP_AssignMode is in cutoff mode the scoring will be quick, but not completely accurate.
     It is accurate enough to rank a large number of structure reliably, but then it's good
     to use the accurate mode for those (e.g. 200) structures that are kept. */
 
  if(SP_AssignMode==SP_AM_CUTOFF || ((mode==0 || mode==SE_PURGE_CH)&& sim->ChCnt>=100) || 
     (mode==SE_PURGE_HH && sim->HhCnt>=100))
    {
      if((mode==0 || mode==SE_PURGE_CH || mode==SE_PURGE_TOT) && exp->ChCnt>0)
	{
	  sim->ChFit=SP_AssExpSimQuick(exp,sim,SE_PURGE_CH,0,0);
	}
      if((mode==0 || mode==SE_PURGE_HH || mode==SE_PURGE_TOT) && exp->HhCnt>0)
	{
	  sim->HhFit=SP_AssExpSimQuick(exp,sim,SE_PURGE_HH,0,0);
	}
      sim->TotFit=SP_C_WEIGHT*sim->CFit+SP_H_WEIGHT*sim->HFit+SP_CH_WEIGHT*sim->ChFit+
	SP_HH_WEIGHT*sim->HhFit;
      return;
    }

  /* If SP_AssignMode is in accurate mode the scoring will be better but take longer time. */
  else
    {
      if((mode==0 || mode==SE_PURGE_CH || mode==SE_PURGE_TOT) && exp->ChCnt>0)
	{
	  sim->ChFit=SP_AssExpSim(exp,sim,SE_PURGE_CH,0,0);
	}
      if((mode==0 || mode==SE_PURGE_HH || mode==SE_PURGE_TOT) && exp->HhCnt>0)
	{
	  sim->HhFit=SP_AssExpSim(exp,sim,SE_PURGE_HH,0,0);
	}
      sim->TotFit=SP_C_WEIGHT*sim->CFit+SP_H_WEIGHT*sim->HFit+SP_CH_WEIGHT*sim->ChFit+
	SP_HH_WEIGHT*sim->HhFit;
      return;
    }
}


/* Correct experimental spectrum for errors in reference */
/* SYNTAX: Correct <exp> <c-off> [<h-off>] */
/* WARNING!!! This does not work with CCPN spectra. Do not use this yet. */
int SP_Correct()
{
  struct BU_Struct *exp;
  struct BU_Unit *unit;
  ApiString name;
  char expName[NODE_NAME_LENGTH], prefix[NODE_NAME_LENGTH];
  float dc, dh;
  int multiFlag=0, specifiedCorrections=0, i, j=1;

  if(strncasecmp(PA_Status->CmdLine, "multicorrect", PA_Status->CmdPtr-PA_Status->CmdLine)==0)
    {
      multiFlag=1;
    }

  dc=0; dh=0;

  PA_GetString;

  if(PA_Result.String[0]=='/')
    {
      if(projectValid)
	{
	  if(!CC_GetNmrProject(&Global_CcpnProject,&Global_NmrProject))
	    {
	      Error(PA_ERR_FATAL,"Cannot open project.");
	    }
	  name=Nmr_NmrProject_GetName(Global_NmrProject);
	  strcpy(prefix, ApiString_Get(name));
	  ApiObject_Free(name);
	}
      /* Find the first structure that does not start with "sim" */
      else
	{
	  for(exp=(struct BU_Struct *)StructList.Head.Succ; exp->Node.Succ!=NULL && strncasecmp("sim", exp->Node.Name, 3)==0;
	      exp=(struct BU_Struct *)exp->Node.Succ)
	    {
	    }
	  if(exp->Node.Succ!=NULL)
	    {
	      if(multiFlag)
		{
		  strncpy(prefix, exp->Node.Name, strlen(exp->Node.Name)-1);
		  prefix[strlen(exp->Node.Name)-1]=0;
		}
	      else
		{
		  strcpy(prefix, exp->Node.Name);
		}
	    }
	  else
	    {
	      Error(PA_ERR_FATAL,"Experimental structure not found and/or project not valid.");
	    }
	}
    }
  else
    {
      strcpy(prefix, PA_Result.String);
    }

  if(multiFlag)
    {
      sprintf(expName, "%s%d", prefix, j++);
    }
  else
    {
      strcpy(expName, prefix);
    }
  exp=(struct BU_Struct *)FindNode((struct Node *)&StructList, expName);
  if (exp==NULL)
    {
      Error(PA_ERR_FAIL,"No such simulated spectrum");
    }


  if(PA_GetArg()==PA_FLOAT)
    {
      specifiedCorrections=1;
      dc=PA_Result.Float;
      PA_GetFloat;
      dh=PA_Result.Float;
    }
  else
    {
      dc=exp->CSysErr;
      dh=exp->HSysErr;
    }

  while(exp)
    {
      for (i=0; i<exp->CCnt; i++)
	exp->CShift[i]-=dc;
      for (i=0; i<exp->HCnt; i++)
	exp->HShift[i]-=dh;
      for (i=0; i<exp->ChCnt; i++)
	{
	  exp->ChCShift[i]-=dc;
	  exp->ChHShift[i]-=dh;
	}
      for (i=0; i<exp->HhCnt; i++)
	{
	  exp->HhLoShift[i]-=dh;
	  exp->HhHiShift[i]-=dh;
	}
      
      exp->CCorrection=dc;
      exp->HCorrection=dh;
      
      for (unit=(struct BU_Unit *)exp->Units.Head.Succ;
	   unit->Node.Succ!=NULL; unit=(struct BU_Unit *)unit->Node.Succ)
	{ 
	  SH_Adjust(&(unit->Shifts), -dc, -dh);
	}
      if(!multiFlag)
	{
	  break;
	}
      else
	{
	  sprintf(expName, "%s%d", prefix, j++);
	  exp=(struct BU_Struct *)FindNode((struct Node *)&StructList, expName);
	  if(!specifiedCorrections && exp)
	    {
	      dc=exp->CSysErr;
	      dh=exp->HSysErr;
	    }
	}
    }

  return(PA_ERR_OK);
}


int SP_CalculateCorrections(struct BU_Struct *sim, struct BU_Struct *exp)
{
  struct BU_Unit *eUnit, *sUnit;
  float hError=0, cError=0;
  int i, j, hCnt=0, cCnt=0;

  for(eUnit=(struct BU_Unit *)exp->Units.Head.Succ,
	sUnit=(struct BU_Unit *)sim->Units.Head.Succ;
      eUnit->Node.Succ!=NULL && sUnit->Node.Succ!=NULL;
      eUnit=(struct BU_Unit *)eUnit->Node.Succ,
	sUnit=(struct BU_Unit *)sUnit->Node.Succ)
    {
      for(i=0;i<eUnit->Shifts.Type->HeavyCnt;i++)
	{
	  if(eUnit->Shifts.C[i]>0.1)
	    {
	      cError+=eUnit->Shifts.C[i]-sUnit->Shifts.C[i];
	      cCnt++;
	    }
	  for(j=0; j<2; j++)
	    {
	      if(eUnit->Shifts.H[i][j]>0.1)
		{
		  hError+=eUnit->Shifts.H[i][j]-sUnit->Shifts.H[i][j];
		  hCnt++;
		}
	    }
	}
    }
  if(cCnt>0)
    {
      exp->CSysErr=cError/cCnt;
    }
  if(hCnt>0)
    {
      exp->HSysErr=hError/hCnt;
    }

  return(cCnt+hCnt);
}

int SP_CalculateMultipleCorrections(char *simPrefix, char *expPrefix)
{
  struct BU_Struct *sim, *exp;
  struct BU_Unit *eUnit, *sUnit;
  char simName[NODE_NAME_LENGTH], expName[NODE_NAME_LENGTH];
  float hError=0, cError=0;
  int i, j, hCnt=0, cCnt=0, k=1;

  sprintf(simName, "%s%d", simPrefix, k);
  sprintf(expName, "%s%d", expPrefix, k++);
  sim=(struct BU_Struct *)FindNode((struct Node *)&StructList, simName);
  exp=(struct BU_Struct *)FindNode((struct Node *)&StructList, expName);

  while(sim && exp)
    {
      for(eUnit=(struct BU_Unit *)exp->Units.Head.Succ,
	    sUnit=(struct BU_Unit *)sim->Units.Head.Succ;
	  eUnit->Node.Succ!=NULL && sUnit->Node.Succ!=NULL;
	  eUnit=(struct BU_Unit *)eUnit->Node.Succ,
	    sUnit=(struct BU_Unit *)sUnit->Node.Succ)
	{
	  for(i=0;i<eUnit->Shifts.Type->HeavyCnt;i++)
	    {
	      if(eUnit->Shifts.C[i]>0.1)
		{
		  cError+=eUnit->Shifts.C[i]-sUnit->Shifts.C[i];
		  cCnt++;
		}
	      for(j=0; j<2; j++)
		{
		  if(eUnit->Shifts.H[i][j]>0.1)
		    {
		      hError+=eUnit->Shifts.H[i][j]-sUnit->Shifts.H[i][j];
		      hCnt++;
		    }
		}
	    }
	}
      sprintf(simName, "%s%d", simPrefix, k);
      sprintf(expName, "%s%d", expPrefix, k++);
      sim=(struct BU_Struct *)FindNode((struct Node *)&StructList, simName);
      exp=(struct BU_Struct *)FindNode((struct Node *)&StructList, expName);
    }

  if(cCnt+hCnt==0)
    {
      return (0);
    }
  
  cError=cError/cCnt;
  hError=hError/hCnt;

  k=1;
  sprintf(expName, "%s%d", expPrefix, k++);
  exp=(struct BU_Struct *)FindNode((struct Node *)&StructList, expName);
  while(exp)
    {
      if(cCnt>0)
	{
	  exp->CSysErr=cError;
	}
      if(hCnt>0)
	{
	  exp->HSysErr=hError;
	}
      sprintf(expName, "%s%d", expPrefix, k++);
      exp=(struct BU_Struct *)FindNode((struct Node *)&StructList, expName);      
    }

  return(cCnt+hCnt);
}

/* This function assigns all types of spectra and prints the results in a list
   if the print flag is set */
float SP_AssExpSim(struct BU_Struct *exp, struct BU_Struct *sim,
		   const unsigned int mode, const char print,
		   struct SP_assignments *nr_assign)
{
  int *usedsim, *usedexp, **globalSimAss, **globalExpAss;
  float cutoff;
  float *sys_err=0, abs_err=0, diff=0;
  float rms_err=0;
  unsigned int i, eCnt, sCnt;
  int localNUnassigned=0;

  usedexp=0;
  usedsim=0;

  switch(mode)
    {
    case SE_PURGE_C:
      sCnt=sim->CCnt;
      eCnt=exp->CCnt;
      cutoff=SP_C_CUTOFF;
      sys_err=&sim->CSysErr;
      break;
    case SE_PURGE_H:
      sCnt=sim->HCnt;
      eCnt=exp->HCnt;
      cutoff=SP_H_CUTOFF;
      sys_err=&sim->HSysErr;
      break;
    case SE_PURGE_CH:
      sCnt=sim->ChCnt;
      eCnt=exp->ChCnt;
      cutoff=SP_CH_CUTOFF;
      break;
    case SE_PURGE_HH:
      eCnt=exp->HhCnt;
      sCnt=sim->HhCnt;
      cutoff=SP_HH_CUTOFF;
      break;
      
    default:
      return 0;
    }
  if(sys_err)
    {
      *sys_err=0;
    }

  /*  usedsim=malloc (sCnt*sizeof(int));*/
  usedsim=alloca (sCnt*sizeof(int));

  /* Allocate a continuous 2D array (sCnt rows and 2 columns) */
  globalSimAss = (int **) alloca (sCnt*sizeof(int *));
  globalSimAss[0] = (int *) alloca (sCnt * 2 * sizeof(int));
  for(i=1; i<sCnt; i++)
    {
      globalSimAss[i]=globalSimAss[0] + i * 2;
    }

  for(i=0;i<sCnt;i++)
    {
      usedsim[i]=-1;
      globalSimAss[i][0]=-1;
      globalSimAss[i][1]=-1;
    }
  /*  usedexp=malloc (eCnt*sizeof(int));*/
  usedexp=alloca (eCnt*sizeof(int));

  /* Allocate a continuous 2D array (sCnt rows and 2 columns) */
  globalExpAss = (int **) alloca (eCnt*sizeof(int *));
  globalExpAss[0] = (int *) alloca (eCnt * 2 * sizeof(int));
  for(i=1; i<eCnt; i++)
    {
      globalExpAss[i]=globalExpAss[0] + i * 2;
    }

  for(i=0;i<eCnt;i++)
    {
      usedexp[i]=-1;
      globalExpAss[i][0]=-1;
      globalExpAss[i][1]=-1;
    }

  /* If the experimental structure is set we come from SP_Assign(). Check if
     any signals have already been assigned to atoms in the structure (from a
     loaded CCPN project). */
  if (exp->Units.Head.Succ!=&exp->Units.Tail)
    {
      SP_PopulateAssignments(exp, sim, usedexp, usedsim, mode);
    }

  /*  if(sim->nCAssignments==0 && sim->nHAssignments==0)
      {
      noAssignmentsFlag=1;
      }*/
  switch(mode)
    {
    case SE_PURGE_C:
    case SE_PURGE_H:
      /*abs_err=*/SP_AssExpSimfind(exp, sim, mode, usedsim, 0, usedexp, 0);
      break;
    case SE_PURGE_CH:
      SP_AssExpSimTwinHydrogen(exp, sim, usedexp, usedsim);
      SP_AssExpSimDoubleSignals(exp, sim, usedexp, usedsim, 0, 0);
    case SE_PURGE_HH:
      
      /*abs_err=*/SP_AssExpSim2Dfind(exp, sim, mode, usedsim, 0, usedexp, 0, globalSimAss, globalExpAss);
      break;
    }

  for (i=0;i<eCnt;i++)
    {
      SP_AddAssignment(exp,i,sim,usedexp[i],mode);
    }

  SP_CalcErrs(exp, sim, eCnt, usedexp, &diff, &abs_err, &rms_err, sys_err, mode);
  
      MultiExpAssignments(exp, sim, usedsim, &diff, &abs_err, &rms_err, &localNUnassigned, mode);
      
  rms_err=sqrt(rms_err/(sCnt-localNUnassigned));

  if(print==1)
    {
      SP_PrintAssignmentTable(exp, sim, usedsim, usedexp, &abs_err, sys_err, &rms_err, nr_assign, localNUnassigned, mode);
    }
  /*  if(usedexp!=0)
    {
      free(usedexp);
      usedexp=0;
    }
  if(usedsim!=0)
    {
      free(usedsim);
      usedsim=0;
      }*/
  return(abs_err);
}


/* This function is for finding the best way to match two sorted lists of shifts.
   The problem is that 2D spectra are very difficult to sort in a way that high
   positions in the list lie close to each other, which is a lot easier with 1D
   spectra. */
float SP_AssExpSimfind(const struct BU_Struct *exp, struct BU_Struct *sim,
		       const unsigned int mode, int *usedsim,
		       unsigned int sim_ptr, int *usedexp,
		       unsigned int exp_ptr)
{
  int j;
  unsigned int eCnt, sCnt, temp_exp_ptr;
  int exp_rem, sim_rem, simSkipCounter;
  float diff, altdiff;
  float abs_err, cutoff;
  const float *eShift, *sShift;
  int *temp_usedsim, *temp_usedexp;
  int matched, assignFlag;

  diff=0;
  simSkipCounter=0;
  abs_err=0;
  assignFlag=0;
  
  switch(mode)
    {
    case SE_PURGE_C:
      eCnt=exp->CCnt;
      sCnt=sim->CCnt;
      eShift=exp->CShift;
      sShift=sim->CShift;
      cutoff=SP_C_CUTOFF;
      if(sim->nCAssignments>0)
	{
	  assignFlag=1;
	}
      break;
    case SE_PURGE_H:
      eCnt=exp->HCnt;
      sCnt=sim->HCnt;
      eShift=exp->HShift;
      sShift=sim->HShift;
      cutoff=SP_H_CUTOFF;
      if(sim->nHAssignments>0)
	{
	  assignFlag=1;
	}
      break;
    default:
      return(0);
    }

  exp_rem=eCnt-exp_ptr;
  sim_rem=sCnt-sim_ptr;

  if(exp_rem<=0 || sim_rem<=0)
    {
      return 0;
    }

  /* If there are as many experimental shifts as simulated shifts left
     they are matched directly since all experimental shifts have to be
     assigned and both lists are sorted descendingly.
     If the assignments differ test which assignment would be best. */
  if(exp_rem==sim_rem)
    {
      for(;exp_ptr<eCnt;exp_ptr++,sim_ptr++)
	{
	  if(assignFlag)
	    {
	      if(usedexp[exp_ptr]!=-1)
		{
		  continue;
		}
	      while(usedsim[sim_ptr]!=-1 && sim_ptr<sCnt)
		{
		  sim_ptr++;
		  /* A counter to show how many simulated shifts have been skipped.
		     This is subtracted from sim_ptr to show which is the first
		     sim_ptr. This has the drawback that these will have to be
		     skipped every time, but on the other hand exp_rem==sim_rem
		     will still be true, which is important.
		  */
		  simSkipCounter++;		  
		}
	    }
	  if(sim_ptr>=sCnt)
	    {
	      sim_ptr=sCnt-1;
	    }

	  usedsim[sim_ptr]=exp_ptr;
	  usedexp[exp_ptr]=sim_ptr;

	  diff=fabs(eShift[exp_ptr] - sShift[sim_ptr]);
	  
	  if(assignFlag)
	    {
	      matched=SP_MatchAssignment(exp,exp_ptr,sim,sim_ptr,mode);
	      if((mode==SE_PURGE_C && !(matched&1) && matched&2) ||
		 (mode==SE_PURGE_H && !(matched&4) && matched&8))
		{
		  diff+=cutoff*SP_MISMATCH_PENALTY;
		  temp_exp_ptr=SP_FindExpAssignedToSim(exp, exp_ptr,
						       sim, sim_ptr, mode);
		  if(temp_exp_ptr>0 && usedexp[temp_exp_ptr]==-1)
		    {
		      /*		      temp_usedsim=malloc (sCnt*sizeof(int));
					      temp_usedexp=malloc (eCnt*sizeof(int));*/
		      temp_usedsim=alloca (sCnt*sizeof(int));
		      temp_usedexp=alloca (eCnt*sizeof(int));
		      memcpy(temp_usedsim,usedsim,sCnt*sizeof(int));
		      memcpy(temp_usedexp,usedexp,eCnt*sizeof(int));

		      /* Reverse loops for slight speed gain */
		      /*		      for(j=sCnt-1;j>eCnt;j--)
			{
			  temp_usedsim[j]=usedsim[j];
			}
		      temp_usedsim[j]=usedsim[j];
		      for(;j--;)
			{
			  temp_usedsim[j]=usedsim[j];
			  temp_usedexp[j]=usedexp[j];
			  }*/
		      diff=SP_AssExpSimfind(exp,sim,mode,usedsim,sim_ptr+1-simSkipCounter,usedexp,
					    exp_ptr+1)+diff;
		      
		      temp_usedsim[sim_ptr]=-1;
		      temp_usedexp[exp_ptr]=-1;
		      temp_usedsim[sim_ptr]=temp_exp_ptr;
		      temp_usedexp[temp_exp_ptr]=sim_ptr;
		      altdiff=fabs(eShift[temp_exp_ptr] - sShift[sim_ptr]);
		      altdiff=SP_AssExpSimfind(exp,sim,mode,temp_usedsim,
					       sim_ptr-simSkipCounter,temp_usedexp,
					       exp_ptr)+altdiff;
		      if(altdiff<diff)
			{
			  /* Reverse loop */
			  for(j=sCnt-1;j>eCnt;j--)
			    {
			      usedsim[j]=temp_usedsim[j];
			    }
			  usedsim[j]=temp_usedsim[j];
			  for(;j--;)
			    {
			      usedsim[j]=temp_usedsim[j];
			      usedexp[j]=temp_usedexp[j];
			    }
			  diff=altdiff;
			}
		      /*		      free(temp_usedsim);
					      free(temp_usedexp);*/
		    }
		}
	    }
	  abs_err+=diff;
	}
      return(abs_err);
    }

  temp_usedexp=0;
  temp_usedsim=0;

  diff=fabs(eShift[exp_ptr] - sShift[sim_ptr]);
  altdiff=fabs(eShift[exp_ptr] - sShift[sim_ptr+1]);


  if(assignFlag)
    {
      matched=SP_MatchAssignment(exp,exp_ptr,sim,sim_ptr,mode);
      if((mode==SE_PURGE_C && !(matched&1) && matched&2) || 
	 (mode==SE_PURGE_H && !(matched&4) && matched&8))
	{
	  diff+=cutoff*SP_MISMATCH_PENALTY;
	} 
    }

  usedsim[sim_ptr]=exp_ptr;
  usedexp[exp_ptr]=sim_ptr;

  /* If this was the last experimental (or simulated - error?) shift return
     the difference. */
  /*  if(exp_rem<=1 || sim_rem<=1)
    {
      return diff;
    }
  */
  if(altdiff<diff*10 || altdiff<cutoff*10)
    {
  
      /*      temp_usedexp=malloc (eCnt*sizeof(int));
	      temp_usedsim=malloc (sCnt*sizeof(int));*/
      temp_usedexp=alloca (eCnt*sizeof(int));
      temp_usedsim=alloca (sCnt*sizeof(int));
      memcpy(temp_usedexp,usedexp,eCnt*sizeof(int));
      memcpy(temp_usedsim,usedsim,sCnt*sizeof(int));
      /*      for(j=sCnt-1;j>eCnt;j--)
	{
	  temp_usedsim[j]=usedsim[j];
	}
      temp_usedsim[j]=usedsim[j];
      for(;j--;)
	{
	  temp_usedsim[j]=usedsim[j];
	  temp_usedexp[j]=usedexp[j];
	  }*/
      temp_usedsim[sim_ptr]=-1;
      temp_usedexp[exp_ptr]=-1;
    }
  else
    {
      altdiff=99999;
    }
  
  if(diff<altdiff*10 || diff<cutoff*10 || !temp_usedexp)
    {
      diff=SP_AssExpSimfind(exp,sim,mode,usedsim,sim_ptr+1,usedexp,
			    exp_ptr+1)+diff;
    }
  else
    {
      diff=99999;
    }

  if(temp_usedexp && temp_usedsim)
    {
      altdiff=SP_AssExpSimfind(exp,sim,mode,temp_usedsim,sim_ptr+1,
			       temp_usedexp,exp_ptr);
    }
      
  if(altdiff<diff)
    {
      memcpy(usedexp,temp_usedexp,eCnt*sizeof(int));
      memcpy(usedsim,temp_usedsim,sCnt*sizeof(int));
      /*      for(j=exp_ptr;j<eCnt;j++)
	{
	  usedexp[j]=temp_usedexp[j];
	  usedsim[j]=temp_usedsim[j];
	}
      for(;j<sCnt;j++)
	{
	  usedsim[j]=temp_usedsim[j];
	  }*/
      diff=altdiff;
    }
  
  abs_err+=diff;
  
  /*  if(temp_usedexp)
    {
      free(temp_usedexp);
    }
  if(temp_usedsim)
    {
      free(temp_usedsim);
      }*/
  return(abs_err);
} 

/* This function is a way to quickly match C6s (or other carbons with 2 protons) in a 2D spectrum.
   To avoid recursions it only matches closest experimental to closest simulated. This can cause errors,
   but this is to a large extent compensated by the fact that only shifts where the total difference
   of the two matches is lower than cutoff*0.5. So if the difference is too big there will be no match
   in this function - it will be matched later on. */
void SP_AssExpSimTwinHydrogen(struct BU_Struct *exp, struct BU_Struct *sim, int *usedexp, int *usedsim)
{
  int i, j, firstsim=9999, firstexp=9999, eCnt, sCnt, nSimTwins=0, nExpTwins=0, found;

  eCnt=exp->ChCnt;
  sCnt=sim->ChCnt;

  /* First find the first C6 pair in the simulated shifts - this will make future assignments quicker */
  for(i=0; i<sCnt-1; i++)
    {
      if(i<sCnt-1 && usedsim[i]==-1 && sim->ChCShift[i]<80 && sim->ChCShift[i]>30)
	{
	  j=SP_FindC6Twin(sim, i);

	  if(j>=0)
	    {
	      if(i<firstsim)
		{
		  firstsim=i;
		}
	      if(j>i)
		{
		  nSimTwins++;
		}
	    }
	}
    }
  for(i=0; i<eCnt-1; i++)
    {
      if(i<eCnt-1 && usedexp[i]==-1 && exp->ChCShift[i]<80 && exp->ChCShift[i]>30)
	{
	  found=0;
	  for(j=i+1; j<eCnt && fabs(exp->ChCShift[i]-exp->ChCShift[j])<0.0001; j++)
	    {
	      if(fabs(exp->ChHShift[i]-exp->ChHShift[j])>0.0001)
		{
		  if(i<firstexp)
		    {
		      firstexp=i;
		    }
		  nExpTwins++;
		  found=1;
		  j=eCnt;
		}
	    }
	  if(!found)
	    {
	      if(fabs(exp->ChCShift[i]-exp->ChCShift[i+1])<0.0001)
		{
		  if(i<firstexp)
		    {
		      firstexp=i;
		    }
		  nExpTwins++;
		  i++;
		}
	    }
	}
    }
  /*  printf("%d sim twins, %d exp twins.\n",nSimTwins,nExpTwins);*/

  SP_AssExpSimTwinHydrogenFind(exp,sim,usedexp,usedsim,firstexp,firstsim,
		       nExpTwins,nSimTwins);
  for(i=0;i<sCnt;i++)
    {
      if(usedsim[i]!=-1)
	{
	  SP_AddAssignment(exp,usedsim[i],sim,i,SE_PURGE_CH);
	  /*	  printf("%f - %f (%d) assigned to %f - %f (%d)\n", sim->ChCShift[i],sim->ChHShift[i], i, exp->ChCShift[usedsim[i]], exp->ChHShift[usedsim[i]], usedsim[i]);*/
	}
    }
}

float SP_AssExpSimTwinHydrogenFind(const struct BU_Struct *exp, struct BU_Struct *sim, int *usedexp, int *usedsim, int firstexp, int firstsim, int expRem, int simRem)
{
  int i, j, k, l, ptr=-1, lptr=-1, secondPtr, secondLptr, eCnt, sCnt, multi;
  int *temp_usedsim, *temp_usedexp;
  float minDiff=9999, secondMinDiff, diff, altDiff;

  eCnt=exp->ChCnt;
  sCnt=sim->ChCnt;

  for(i=firstsim;i<sCnt-1 && simRem>0;i++)
    {
      if(usedsim[i]==-1)
	{
	  j=SP_FindC6Twin(sim, i);
	  if(j<i)
	    {
	      continue;
	    }
	  if(j>=0 && usedsim[j]==-1)
	    {
	      minDiff=9999;
	      secondMinDiff=9999;

	      if(i>0 && j>0)
		{
		  diff=SP_2DDelta(sim,i,sim,i-1,SE_PURGE_CH)+SP_2DDelta(sim,j,sim,j-1,SE_PURGE_CH);
		  /* If there are two identical simulated shifts just skip the second one - it will be
		     assigned in void SP_AssExpSimDoubleSignals(...) instead */
		  if(diff<0.00001)
		    {
		      simRem--;
		      continue;
		    }		  
		}
	      if(i>0 && j<sCnt-1)
		{
		  diff=SP_2DDelta(sim,i,sim,i-1,SE_PURGE_CH)+SP_2DDelta(sim,j,sim,j+1,SE_PURGE_CH);
		  if(diff<0.00001)
		    {
		      simRem--;
		      continue;
		    }		  
		}

	      for(k=firstexp; k<eCnt-1 && expRem>0; k++)
		{
		  if(usedexp[k]!=-1 && expRem>=simRem)
		    {
		      continue;
		    }
		  for(l=k+1; l<eCnt && fabs(exp->ChCShift[k]-exp->ChCShift[l])<0.0001; l++)
		    {
		      if(usedexp[l]!=-1 && expRem>=simRem)
			{
			  continue;
			}
		      diff=SP_2DDelta(exp,k,sim,i,SE_PURGE_CH)+SP_2DDelta(exp,l,sim,j,SE_PURGE_CH);
		      if(diff<minDiff)
			{
			  if(minDiff<secondMinDiff-0.00001)
			    {
			      secondMinDiff=minDiff;
			    }
			  if(ptr!=-1 && lptr!=-1)
			    {
			      secondPtr=ptr;
			      secondLptr=lptr;
			    }
			  minDiff=diff;
			  ptr=k;
			  lptr=l;
			}
		      else if(diff<secondMinDiff-0.00001 && minDiff<diff-0.00001)
			{
			  secondMinDiff=diff;
			  secondPtr=k;
			  secondLptr=l;				  
			}
		    }
		}
	      if(minDiff<9998)
		{
		  simRem--;
		  /* If these experimental shifts have already been used flag it and penalise it. If this is the only
		     way the penalty will not make any difference, but it will make it more probable that most experimental shifts
		     are used before the same ones are used over and over. */
		  if(usedexp[ptr]==-1 && usedexp[lptr]==-1)
		    {
		      expRem--;
		      multi=0;
		    }
		  else
		    {
		      multi=1;
		      if(expRem>=simRem)
			{
			  minDiff+=SP_CH_CUTOFF;
			}
		    }

		  usedsim[i]=ptr;
		  usedexp[ptr]=i;
		  usedsim[j]=lptr;
		  usedexp[lptr]=j;
	      
		  temp_usedexp=alloca(eCnt*sizeof(int));
		  temp_usedsim=alloca(sCnt*sizeof(int));
		  memcpy(temp_usedexp,usedexp,eCnt*sizeof(int));
		  memcpy(temp_usedsim,usedsim,sCnt*sizeof(int));
		  temp_usedsim[i]=-1;
		  temp_usedsim[j]=-1;
		  if(!multi)
		    {
		      temp_usedexp[ptr]=-1;
		      temp_usedexp[lptr]=-1;
		    }

		  if(expRem>0 && simRem>0)
		    {
		      diff=SP_AssExpSimTwinHydrogenFind(exp,sim,usedexp,usedsim,
							firstexp,i+1,
							expRem,simRem)+minDiff;
		    }
		  else
		    {
		      diff=minDiff;
		    }

		  /* In extreme cases assignments can be skipped. But only if the score is bad. In order to make this
		     less common a punishment is added */
		  if(expRem>simRem || minDiff>SP_CH_CUTOFF*5)
		    {
		      altDiff=SP_AssExpSimTwinHydrogenFind(exp,sim,temp_usedexp,
							   temp_usedsim,firstexp,i+1,
							   expRem,simRem+1)+SP_CH_CUTOFF;
		      if(altDiff<diff)
			{
			  memcpy(usedexp,temp_usedexp,eCnt*sizeof(int));
			  memcpy(usedsim,temp_usedsim,sCnt*sizeof(int));
			  diff=altDiff;
			  expRem++;
			  simRem++;
			}
		    }
		  /* Check if the second best alternative will be better. This will make a recursive function call. */
		  else if(secondPtr!=-1 && secondLptr!=-1 && secondMinDiff<SP_CH_CUTOFF*5)
		    {
		      /* Penalise using the same experimental shifts multiple times and keep the counter correct - depending
			 on if the experimental shift used above (for the best assignment) involved using the same experimental
			 shift more than once */
		      if(usedexp[secondPtr]==-1 && usedexp[secondLptr]==-1)
			{
			  if(multi)
			    {
			      expRem--;
			    }
			}
		      else
			{
			  if(!multi)
			    {
			      expRem++;
			    }
			  if(expRem>=simRem)
			    {
			      secondMinDiff+=SP_CH_CUTOFF;
			    }
			}
		      
		      temp_usedsim[i]=secondPtr;
		      temp_usedexp[secondPtr]=i;
		      temp_usedsim[j]=secondLptr;
		      temp_usedexp[secondLptr]=j;
		      altDiff=SP_AssExpSimTwinHydrogenFind(exp,sim,temp_usedexp,
							   temp_usedsim,firstexp,i+1,
							   expRem,simRem)+secondMinDiff;
		      
		      if(altDiff<diff)
			{
			  memcpy(usedexp,temp_usedexp,eCnt*sizeof(int));
			  memcpy(usedsim,temp_usedsim,sCnt*sizeof(int));
			  diff=altDiff;
			}
		    }
		  return diff;
		}
	    }
	}
    }
  if(minDiff<9998)
    {
      return minDiff;
    }
  else
    {
      return 0;
    }
}


/* This functions simulated chemical shifts of exactly the same position to the same experimental shift if there are
   more simulated shifts than experimental shifts. Since there is at least two simulated shifts matched to the same
   experimental shift this assignment has precedence over other assignments (it's more important to find the
   closest shifts when there are more shifts assigned to the same signals).
   This function is only used for CH assignments since the high precision is needed to be sure that the simulated
   shifts are equivalent. */
void SP_AssExpSimDoubleSignals(struct BU_Struct *exp, struct BU_Struct *sim, int *usedexp, int *usedsim, int *expRem, int *simRem)
{
  int exp_ptr, sim_ptr, eCnt, sCnt, temp_ptr, i;
  float ass_hi, ass_lo;

  eCnt=exp->ChCnt;
  sCnt=sim->ChCnt;

  for(sim_ptr=0; sim_ptr<sCnt-1; sim_ptr++)
    {
      if(simRem && expRem && *simRem<=*expRem)
	{
	  return;
	}

      if(SP_2DDelta(sim,sim_ptr,sim,sim_ptr+1,SE_PURGE_CH)<0.0001)
	{
	  ass_lo=9999;
	  if(usedsim[sim_ptr]==-1 && usedsim[sim_ptr+1]==-1)
	    {
	      temp_ptr=-1;
	      for(exp_ptr=0;exp_ptr<eCnt;exp_ptr++)
		{
		  if(usedexp[exp_ptr]==-1)
		    {
		      ass_hi=SP_2DDelta(exp,exp_ptr,sim,sim_ptr,SE_PURGE_CH);
		      
		      if(ass_hi<ass_lo)
			{
			  ass_lo=ass_hi;
			  temp_ptr=exp_ptr;
			}
		    }
		}
	      exp_ptr=temp_ptr;
	    }
	  else if(usedsim[sim_ptr]==-1 || usedsim[sim_ptr+1]==-1)
	    {
	      if(usedsim[sim_ptr]!=-1)
		{
		  exp_ptr=usedsim[sim_ptr];
		}
	      else
		{
		  exp_ptr=usedsim[sim_ptr+1];
		}
	      ass_lo=SP_2DDelta(exp,exp_ptr,sim,sim_ptr,SE_PURGE_CH);
	    }

	  if(ass_lo<SP_CH_CUTOFF*2)
	    {
	      usedsim[sim_ptr]=exp_ptr;
	      if(usedexp[exp_ptr]==-1)
		{
		  usedexp[exp_ptr]=sim_ptr;
		}

	      /*	      SP_AddAssignment(exp,exp_ptr,sim,sim_ptr,SE_PURGE_CH);*/

	      if(simRem && expRem)
		{
		  *simRem-=1;
		  *expRem-=1;
		}
	      /* There may be more than two equivalent shifts - look further ahead in the list of simulated shifts to
		 find more. */
	      for(i=sim_ptr+1;i<sCnt && (!simRem || !expRem || *simRem>*expRem) && SP_2DDelta(sim,sim_ptr,sim,i,SE_PURGE_CH)<0.0001;i++)
		{
		  if(usedsim[i]==-1)
		    {
		      /* Check if there are more equal experimental shifts. If there is more than one experimental
			 shift use them all before using the same more than once. */
		      if(exp_ptr<eCnt-1 && SP_2DDelta(exp,exp_ptr,exp,exp_ptr+1,SE_PURGE_CH)<0.0001)
			{
			  exp_ptr++;
			  if(expRem)
			    {
			      *expRem-=1;
			    }
			}
		      usedsim[i]=exp_ptr;
		      if(usedexp[exp_ptr]==-1)
			{
			  usedexp[exp_ptr]=i;
			}
		      if(simRem)
			{
			  *simRem-=1;
			}
		    }
		}
	      sim_ptr=i-1;
	    }
	}
    }
}

/* For matching each shift pair this function first matches the simulated shift
   closest to the experimental shift and thereafter finds the experimental shift
   closest to _that_ simulated shift. If those are the same then continue to the
   next experimental shift. Otherwise recursively call this function to see which
   of the combinations gives the best score.
   Every time a shift it matched to another they are marked as used by showing
   which shift it is closed to */
float SP_AssExpSim2Dfind(struct BU_Struct *exp, struct BU_Struct *sim,
			 const unsigned int mode,
			 int *usedsim, unsigned int firstsim, int *usedexp,
			 unsigned int firstexp, int **globalSimAss, int **globalExpAss)
{
  unsigned int exp_ptr, sim_ptr, new_exp_ptr, new_sim_ptr,  eCnt, sCnt, flag;
  int temp_sim_ptr, temp_exp_ptr, alt_temp_ptr;
  float diff, ass_lo, ass_med, ass_hi, ass_second_lo, altdiff, temp_ass_lo;
  float abs_err=0, cutoff, combDiff, minCombDiff;
  int *temp_usedsim, *temp_usedexp;
  int orig_firstexp;

  sim_ptr=0;
  diff=0;

  temp_usedsim=0;
  temp_usedexp=0;

  switch(mode)
    {
    case SE_PURGE_CH:
      eCnt=exp->ChCnt;
      sCnt=sim->ChCnt;
      cutoff=SP_CH_CUTOFF;
      break;
    case SE_PURGE_HH:
      eCnt=exp->HhCnt;
      sCnt=sim->HhCnt;
      cutoff=SP_HH_CUTOFF;
      break;
      
    default:
      return 0;
    }


  for (exp_ptr=firstexp; exp_ptr<eCnt; exp_ptr++)
    {
      /* Find the first unassigned experimental shift */
      while(usedexp[exp_ptr]!=-1 && exp_ptr<eCnt-1)
	{
	  exp_ptr++;
	}
      if(exp_ptr==eCnt-1 && usedexp[exp_ptr]!=-1) /* All experimental shifts assigned */
	{
	  break;
	}

      orig_firstexp=firstexp;

      firstexp=exp_ptr;

      sim_ptr=firstsim;

      /* Find the first unassigned simulated shift */
      while(usedsim[sim_ptr]!=-1 && sim_ptr<sCnt-1)
	{
	  sim_ptr++;
	}
      firstsim=sim_ptr;

      ass_lo=9999;
      minCombDiff=9999;

      temp_sim_ptr=sim_ptr;
      
      /* Find the simulated shift closest to the experimental
	 shift in question */
      for(;sim_ptr<sCnt;sim_ptr++)
	{
	  if(usedsim[sim_ptr]==-1)
	    {
	      
		  ass_hi=SP_2DDelta(exp,exp_ptr,sim,sim_ptr,mode);

		  if(mode==SE_PURGE_CH)
		    {
		      combDiff=SP_CombinationDiff(exp, sim, exp_ptr, sim_ptr);
		      if(combDiff>0)
			{
			  ass_hi+=combDiff;
			}
		    }
		  
		  if(ass_hi<ass_lo)
		    {
			  ass_second_lo=ass_lo;
			  ass_lo=ass_hi;
			  temp_sim_ptr=sim_ptr;
			  minCombDiff=combDiff;
		    }
		  else if(ass_hi<ass_second_lo)
		    {
		      ass_second_lo=ass_hi;
		    }
		  /* If the difference is getting too large don't continue to look */
		  else if(ass_hi>ass_lo*10 && ass_hi>cutoff*10)
		    {
		      sim_ptr=sCnt;
		    }
		  
	    }
	}
      /*      ass_lo-=tempPenalty;*/
      sim_ptr=temp_sim_ptr;

      temp_ass_lo=ass_lo;


      /* Now try to find if the experimental is the closest shift to any
	 of the simulated shifts that were skipped or any of the 5 next shifts */

      ass_lo=99999;
      
      temp_sim_ptr=-1;
      temp_exp_ptr=-1;
      
      if((float)eCnt/sCnt>0.05f)
	{
	  for(new_sim_ptr=firstsim;new_sim_ptr<sim_ptr+5 && new_sim_ptr<sCnt;new_sim_ptr++)
	    {
	      if(usedsim[new_sim_ptr]==-1/* && new_sim_ptr!=sim_ptr*/)
		{
		  alt_temp_ptr=-1;
		  ass_med=9999;
		  for(new_exp_ptr=firstexp;new_exp_ptr<eCnt;new_exp_ptr++)
		    {
		      if(usedexp[new_exp_ptr]!=-1)
			{
			  continue;
			}
		      
			  ass_hi=SP_2DDelta(exp,new_exp_ptr,sim,new_sim_ptr,mode);
			  
			  if(mode==SE_PURGE_CH)
			    {
			      combDiff=SP_CombinationDiff(exp, sim, new_exp_ptr,
							  new_sim_ptr);
			      if(combDiff>0)
				{
				  ass_hi+=combDiff;
				}
			      /* If this experimental shift did not exist 
				 in HH  add the combinated difference
				 from above */
			      else if(minCombDiff>0)
				{
				  ass_hi+=minCombDiff;
				}
			    }

			  if(ass_hi<=ass_med)
			    {
				  ass_med=ass_hi;
				  alt_temp_ptr=new_exp_ptr;
			    }
			  /* If the difference is getting too large don't continue to look */
			  else if(ass_hi>ass_med*10 && ass_hi>cutoff*10)
			    new_exp_ptr=eCnt;
			  
		    }
		  if(alt_temp_ptr>=0)
		    {
		      if(((alt_temp_ptr == exp_ptr && SP_2DDelta(sim,new_sim_ptr,sim,sim_ptr,mode)>0.01)
			  || (new_sim_ptr==sim_ptr && SP_2DDelta(sim,alt_temp_ptr,sim,exp_ptr,mode)>0.01)) && 
			 ass_med<ass_lo && 
			 (ass_med<temp_ass_lo*8 || ass_med<cutoff*8))
			{
			  if(alt_temp_ptr==exp_ptr || (temp_ass_lo-ass_med>0.5f*(ass_second_lo-temp_ass_lo)))
			    {
			      if(alt_temp_ptr!=exp_ptr || new_sim_ptr!=sim_ptr || (temp_sim_ptr==-1 || temp_sim_ptr>new_sim_ptr))
				{
				  ass_lo=ass_med;
				  temp_sim_ptr=new_sim_ptr;
				  temp_exp_ptr=alt_temp_ptr;
				}
			    }
			}
		    }
		}
	    }
	}
	  
      flag=0;
      if(temp_sim_ptr!=-1)
	{
	  if(ass_lo < temp_ass_lo + cutoff)
	    {
	      new_sim_ptr=temp_sim_ptr;
	      new_exp_ptr=temp_exp_ptr;
	      flag=1;
	    }
	  if(temp_ass_lo > ass_lo + 5*cutoff)
	    {
	      temp_exp_ptr=exp_ptr;
	      sim_ptr=new_sim_ptr;
	      exp_ptr=new_exp_ptr;
	      flag=2;
	    }
	  /* Check if these two alternatives have already been checked in an earlier iteration */
	  if(globalSimAss[sim_ptr][0]!=-1 && new_sim_ptr == sim_ptr && exp_ptr != new_exp_ptr)
	    {
	      if(globalSimAss[sim_ptr][0]==exp_ptr && globalSimAss[sim_ptr][1]==new_exp_ptr)
		{
		  /*		  printf("Sim_ptr: %d (%f - %f). Setting new_exp_ptr %d (%f - %f) to exp_ptr %d (%f - %f)\n", sim_ptr,
			 sim->ChCShift[sim_ptr],
			 sim->ChHShift[sim_ptr],
			 new_exp_ptr,
			 exp->ChCShift[new_exp_ptr],
			 exp->ChHShift[new_exp_ptr],
			 exp_ptr,
			 exp->ChCShift[exp_ptr],
			 exp->ChHShift[exp_ptr]);*/
			 new_exp_ptr=exp_ptr;
		}
	      else if (globalSimAss[sim_ptr][0]==new_exp_ptr && globalSimAss[sim_ptr][1]==exp_ptr)
		{
		  /*		  printf("Sim_ptr: %d (%f - %f). Setting exp_ptr %d (%f - %f) to new_exp_ptr %d (%f - %f)\n", sim_ptr,
			 sim->ChCShift[sim_ptr],
			 sim->ChHShift[sim_ptr],
			 exp_ptr,
			 exp->ChCShift[exp_ptr],
			 exp->ChHShift[exp_ptr],
			 new_exp_ptr,
			 exp->ChCShift[new_exp_ptr],
			 exp->ChHShift[new_exp_ptr]);*/
		  exp_ptr=new_exp_ptr;
		  flag=2;
		}
	      /* Otherwise reset this assignment. */
	      else
		{
		  globalSimAss[sim_ptr][0]=-1;
		  globalSimAss[sim_ptr][1]=-1;
		}
	    }
	  else if(globalExpAss[exp_ptr][0]!=-1 && new_exp_ptr == exp_ptr && 
		  sim_ptr != new_sim_ptr)
	    {
	      if(globalExpAss[exp_ptr][0]==sim_ptr && globalExpAss[exp_ptr][1]==new_sim_ptr)
		{
		  /*		  printf("Exp_ptr: %d (%f - %f). Setting new_sim_ptr %d (%f - %f) to sim_ptr %d (%f - %f)\n", exp_ptr,
			 exp->ChCShift[exp_ptr],
			 exp->ChHShift[exp_ptr],
			 new_sim_ptr,
			 sim->ChCShift[new_sim_ptr],
			 sim->ChHShift[new_sim_ptr],
			 sim_ptr,
			 sim->ChCShift[sim_ptr],
			 sim->ChHShift[sim_ptr]);*/
			 new_sim_ptr=sim_ptr;
		}
	      else if (globalExpAss[exp_ptr][0]==new_sim_ptr && globalExpAss[exp_ptr][1]==sim_ptr)
		{
		  /*		  printf("Exp_ptr: %d (%f - %f). Setting sim_ptr %d (%f - %f) to new_sim_ptr %d (%f - %f)\n", exp_ptr,
			 exp->ChCShift[exp_ptr],
			 exp->ChHShift[exp_ptr],
			 sim_ptr,
			 sim->ChCShift[sim_ptr],
			 sim->ChHShift[sim_ptr],
			 new_sim_ptr,
			 sim->ChCShift[new_sim_ptr],
			 sim->ChHShift[new_sim_ptr]);*/
			 sim_ptr=new_sim_ptr;
		}
	  /* Otherwise reset this assignment. */
	      else
		{
		  /*		  printf("Exp_ptr: %d. Resetting assignment\n", exp_ptr);*/
		  globalExpAss[exp_ptr][0]=-1;
		  globalExpAss[exp_ptr][1]=-1;
		}
	    }
	  if(flag==0)
	    {
	      new_sim_ptr=sim_ptr;
	      new_exp_ptr=exp_ptr;
	    }
	}
      else
	{
	  new_sim_ptr=sim_ptr;
	  new_exp_ptr=exp_ptr;
	}

      usedsim[sim_ptr]=exp_ptr;
      usedexp[exp_ptr]=sim_ptr;

      /* If there was no skipped simulated shift that had this experimental shift as closest match continue */
      if(new_sim_ptr==sim_ptr && new_exp_ptr==exp_ptr)
	{
	  if(flag==2)
	    {
	      exp_ptr=Min(0,firstexp-1); /* -1 has to be used in case the firstexp has been skipped it will be stepped over otherwise */
	    }
	  continue;
	}


      /* Otherwise it must be decided which is the best assignment */

      /* Allocate memory for temporary storage of assignments */
      temp_usedexp=alloca(eCnt*sizeof(int));
      temp_usedsim=alloca(sCnt*sizeof(int));
      memcpy(temp_usedexp,usedexp,eCnt*sizeof(int));
      memcpy(temp_usedsim,usedsim,sCnt*sizeof(int));

      temp_usedexp[exp_ptr]=-1;
      temp_usedsim[sim_ptr]=-1;
      temp_usedexp[new_exp_ptr]=new_sim_ptr;
      temp_usedsim[new_sim_ptr]=new_exp_ptr;

      /*      printf("Exp: %f - %f with %f - %f\n", exp->ChCShift[exp_ptr], exp->ChHShift[exp_ptr],
	     sim->ChCShift[sim_ptr], sim->ChHShift[sim_ptr]);
      printf("New Exp: %f - %f with %f - %f\n", exp->ChCShift[new_exp_ptr], exp->ChHShift[new_exp_ptr],
      sim->ChCShift[new_sim_ptr], sim->ChHShift[new_sim_ptr]);*/
 
      /* A recursive call to this function using the first matching.
	 After returning here all remaining shifts are assigned. */
      diff=SP_AssExpSim2Dfind(exp,sim,mode,usedsim,
			      firstsim,usedexp,firstexp,globalSimAss, globalExpAss);

      /* After that try using the other way of matching */
      altdiff=SP_AssExpSim2Dfind(exp,sim,mode,temp_usedsim,
				 firstsim,temp_usedexp,firstexp,globalSimAss, globalExpAss);



      /* Mark the best matches in the usedsim and usedexp arrays. Thereafter set the error
	 to the error from the selected match. */
      if(altdiff<diff/* || (altdiff-diff<0.10 && new_exp_ptr < eCnt-1 &&
			  SP_2DDelta(exp,new_exp_ptr,sim,temp_usedexp[new_exp_ptr],mode) < 
			  SP_2DDelta(exp,new_exp_ptr+1,sim,temp_usedexp[new_exp_ptr+1],mode) + 0.10)*/)
	 
	{
	  if(sim_ptr==new_sim_ptr)
	    {
	      /*printf("Setting Sim_ptr: %d (%f - %f) to %d (%f - %f) and %d (%f - %f). Altdiff: %f vs %f.\n", sim_ptr,
		     sim->ChCShift[sim_ptr],
		     sim->ChHShift[sim_ptr],
		     new_exp_ptr,
		     exp->ChCShift[new_exp_ptr],
		     exp->ChHShift[new_exp_ptr],
		     exp_ptr,
		     exp->ChCShift[exp_ptr],
		     exp->ChHShift[exp_ptr],altdiff, diff);*/
	      globalSimAss[sim_ptr][0]=new_exp_ptr;
	      globalSimAss[sim_ptr][1]=exp_ptr;
	    }
	  else if(exp_ptr==new_exp_ptr)
	    {
	      /*	      printf("Setting Exp_ptr: %d (%f - %f) to %d (%f - %f) and %d (%f - %f). Diff %f vs %f\n", exp_ptr,
		     exp->ChCShift[exp_ptr],
		     exp->ChHShift[exp_ptr],
		     new_sim_ptr,
		     sim->ChCShift[new_sim_ptr],
		     sim->ChHShift[new_sim_ptr],
		     sim_ptr,
		     sim->ChCShift[sim_ptr],
		     sim->ChHShift[sim_ptr], altdiff, diff);*/
	      globalExpAss[exp_ptr][0]=new_sim_ptr;
	      globalExpAss[exp_ptr][1]=sim_ptr;
	    }
	  exp_ptr=new_exp_ptr;
	  sim_ptr=new_sim_ptr;
	  memcpy(usedsim,temp_usedsim,sCnt*sizeof(int));
	  memcpy(usedexp,temp_usedexp,eCnt*sizeof(int));

	  diff=altdiff;
	  /*  *cntMismatch=tempCntMismatch;*/
	}
      else
	{
	  if(sim_ptr==new_sim_ptr)
	    {
	      /*	      printf("Setting Sim_ptr: %d (%f - %f) to %d (%f - %f) and %d (%f - %f). Diff %f vs %f\n", sim_ptr,
		     sim->ChCShift[sim_ptr],
		     sim->ChHShift[sim_ptr],
		     exp_ptr,
		     exp->ChCShift[exp_ptr],
		     exp->ChHShift[exp_ptr],
		     new_exp_ptr,
		     exp->ChCShift[new_exp_ptr],
		     exp->ChHShift[new_exp_ptr], diff, altdiff);*/
	      globalSimAss[sim_ptr][0]=exp_ptr;
	      globalSimAss[sim_ptr][1]=new_exp_ptr;
	    }
	  else if(exp_ptr==new_exp_ptr)
	    {
	      /*	      printf("Setting Exp_ptr: %d (%f - %f) to %d (%f - %f) and %d (%f - %f). Diff %f vs %f\n", exp_ptr,
		     exp->ChCShift[exp_ptr],
		     exp->ChHShift[exp_ptr],
		     sim_ptr,
		     sim->ChCShift[sim_ptr],
		     sim->ChHShift[sim_ptr],
		     new_sim_ptr,
		     sim->ChCShift[new_sim_ptr],
		     sim->ChHShift[new_sim_ptr], diff, altdiff);*/
	      globalExpAss[exp_ptr][0]=sim_ptr;
	      globalExpAss[exp_ptr][1]=new_sim_ptr;
	    }
	}
    }


  /* Sum the total differences and return the value */
  for(exp_ptr=0;exp_ptr<eCnt;exp_ptr++)
    {
      if(usedexp[exp_ptr]!=-1)
	{
	  diff=SP_2DDelta(exp,exp_ptr,sim,usedexp[exp_ptr],mode);
	  if(mode==SE_PURGE_CH)
	    {
	      combDiff=SP_CombinationDiff(exp, sim, exp_ptr, usedexp[exp_ptr]);
	      if(combDiff>0)
		{
		  diff+=combDiff;
		}
	    }
	  abs_err+=diff;
	}
    }

  return(abs_err);
}


/* Non-recursive function for matching spectra. It matches all experimental shifts
   with the closest simulated shifts, but does not take into account that this might
   cause poor assignments at a later stage. This method is quick but not very accurate.
   It can be used for discarding poor matches in a list of very many structures, but
   should be followed up by a more extensive assignment. */
float SP_AssExpSimQuick(struct BU_Struct *exp, struct BU_Struct *sim, 
			const unsigned int mode, const int print,
			struct SP_assignments *nr_assign)
{
  int exp_ptr, sim_ptr, second_exp_ptr, second_sim_ptr;
  int temp_ptr, second_temp_ptr, exp_rem, sim_rem, fail;
  unsigned int eCnt, sCnt, matched, mismatchFlag, repetition=0;
  int *usedsim, *usedexp, localNUnassigned=0, *mismatches, i;
  float diff=0, minDiff, secondMinDiff;
  float second_exp_diff, second_exp_minDiff;
  float third_exp_diff, third_exp_minDiff;
  float abs_err=0, rms_err=0, cutoff, *sys_err=0;
  const float *eShift, *sShift;


  /*  printf("DEBUG info: in SP_AssExpSimQuick.\n");*/

  switch(mode)
    {
    case SE_PURGE_C:
      eCnt=exp->CCnt;
      sCnt=sim->CCnt;
      eShift=exp->CShift;
      sShift=sim->CShift;
      cutoff=SP_C_CUTOFF;
      sys_err=&sim->CSysErr;
      break;
    case SE_PURGE_H:
      eCnt=exp->HCnt;
      sCnt=sim->HCnt;
      eShift=exp->HShift;
      sShift=sim->HShift;
      cutoff=SP_H_CUTOFF;
      sys_err=&sim->HSysErr;
      break;
    case SE_PURGE_CH:
      eCnt=exp->ChCnt;
      sCnt=sim->ChCnt;
      cutoff=SP_CH_CUTOFF;
      break;
    case SE_PURGE_HH:
      eCnt=exp->HhCnt;
      sCnt=sim->HhCnt;
      cutoff=SP_HH_CUTOFF;
      break;
      
    default:
      return 0;
    }

  /*  usedsim=malloc (sCnt*sizeof(int));
      usedexp=malloc (eCnt*sizeof(int));*/
  usedsim=alloca (sCnt*sizeof(int));
  usedexp=alloca (eCnt*sizeof(int));
  mismatches=alloca (sCnt*sizeof(int));
  /* Optimized reverse for loops for speed increase */
  for(i=sCnt;i--;)
    {
      usedsim[i]=-1;
      mismatches[i]=0;
    }
  for(i=eCnt;i--;)
    {
      usedexp[i]=-1;
    }

  sim_rem=sCnt;
  exp_rem=eCnt;

  /* If the experimental structure is set we come from SP_Assign(). Check if
     any signals have already been assigned to atoms in the structure (from a
     loaded CCPN project). */
  if (exp->Units.Head.Succ!=&exp->Units.Tail)
    {
      SP_PopulateAssignments(exp, sim, usedexp, usedsim, mode);
    }

  if(mode==SE_PURGE_CH)
    {
      SP_AssExpSimTwinHydrogen(exp,sim,usedexp,usedsim);
      SP_AssExpSimDoubleSignals(exp,sim,usedexp,usedsim, &exp_rem, &sim_rem);
    }

  /* As long as the next simulated shift is greater than the
     current experimental shift look further ahead in the simulated
     shifts (they are sorted so that they get lower).
     Since some simulated shifts are skipped to find the best match
     those shifts are counted and later there is an attempt to match
     them with the neighbouring experimental shifts. I.e. with the
     previous experimental shift or with the current one (even if the
     skipped shifts were not the best matches they might still be
     within cutoff range.) */
  for(exp_ptr=0; exp_ptr<eCnt && repetition<=2; exp_ptr++)
    {
      if(usedexp[exp_ptr]!=-1)
	{
	  if(exp_ptr>=eCnt-1)
	    {
	      exp_ptr=-1;
	      repetition++;
	    }
	  continue;
	}
      /* If this experimental shift has not been assigned to _any_
	 simulated shift try to assign all other shifts first, but
	 only wait one cycle */
      if(repetition==0)
	{
	  if(!SP_FindExpAssignment(exp,exp_ptr,sim,mode))
	    {
	      if(exp_ptr>=eCnt-1)
		{
		  exp_ptr=-1;
		  repetition++;
		}
	      continue;
	    }
	}
      mismatchFlag=0;
      sim_ptr=0;
      /* If the best distance (minDiff) is too large allow simulated shifts to be used more than once.
	 This is one way to circumvent very poor assignments since recursions are not used here.
	 This function should not be used for final assignments so it is not that much of a problem. */
      while(((/*minDiff<cutoff*2 && */usedsim[sim_ptr]!=-1))
	    && sim_ptr<(sCnt-1))
	{
	  sim_ptr++;
	}
      if(repetition==0 || exp_rem!=sim_rem || (mode!=SE_PURGE_C && mode!=SE_PURGE_H))
	{
	  minDiff=9999;
	  secondMinDiff=9999;
	  temp_ptr=sim_ptr;
	      
	  while (sim_ptr<sCnt)
	    {
	      if(usedsim[sim_ptr]==-1)
		{
		  
		      switch(mode)
			{
			case SE_PURGE_C:
			case SE_PURGE_H:
			  diff=fabs(eShift[exp_ptr] - sShift[sim_ptr]);
			  break;
			default:
			  diff=SP_2DDelta(exp,exp_ptr,sim,sim_ptr,mode);
			  break;
			}
			    
		      if(repetition==0)
			{
			  matched=SP_MatchAssignment(exp,exp_ptr,sim,sim_ptr,mode);
				
			  /* Use a very high penalty for this inaccurate (quick) assignment method - this means
			     previous assignments will be used if available */
			  if(!(matched&1) && matched&2)
			    {
			      diff+=cutoff*3*SP_MISMATCH_PENALTY;
			    }
			  if(!(matched&4) && matched&8)
			    {
			      diff+=cutoff*3*SP_MISMATCH_PENALTY;
			    }
			}
			    
		      if(diff<minDiff)
			{
			  if(minDiff<secondMinDiff-0.00001)
			    {
			      secondMinDiff=minDiff;
			    }
			  if(temp_ptr!=-1)
			    {
			      second_temp_ptr=temp_ptr;
			    }
			  if(repetition==0)
			    {
			      mismatchFlag=0;
			      if(!(matched&1) && matched&2)
				{
				  mismatchFlag++;
				}
			      if(!(matched&4) && matched&8)
				{
				  mismatchFlag++;
				}
			    }
			  minDiff=diff;
			  temp_ptr=sim_ptr;
			}
		      else if(diff<secondMinDiff-0.00001 && minDiff<diff-0.00001)
			{
			  secondMinDiff=diff;
			  second_temp_ptr=sim_ptr;
			}
		      
		}
	      sim_ptr++;
	    }
	  sim_ptr=temp_ptr;
	  second_sim_ptr=second_temp_ptr;
	}
      if(repetition==1 && minDiff>cutoff/5 && secondMinDiff<cutoff*5)
	{
	  second_exp_minDiff=9999;
	  third_exp_minDiff=9999;
	  for(second_exp_ptr=exp_ptr;second_exp_ptr<eCnt;second_exp_ptr++)
	    {
	      if(usedexp[second_exp_ptr]!=-1)
		{
		  continue;
		}
	      
		  switch(mode)
		    {
		    case SE_PURGE_C:
		    case SE_PURGE_H:
		      second_exp_diff=fabs(eShift[second_exp_ptr] - sShift[sim_ptr]);
		      third_exp_diff=fabs(eShift[second_exp_ptr] - sShift[second_sim_ptr]);
		      break;
		    default:
		      second_exp_diff=SP_2DDelta(exp,second_exp_ptr,sim,sim_ptr,mode);
		      third_exp_diff=SP_2DDelta(exp,second_exp_ptr,sim,second_sim_ptr,mode);
		      break;
		    }
		  if(second_exp_diff<second_exp_minDiff)
		    {
		      second_exp_minDiff=second_exp_diff;
		      temp_ptr=second_exp_ptr;
		    }
		  if(third_exp_diff<third_exp_minDiff)
		    {
		      third_exp_minDiff=third_exp_diff;
		      second_temp_ptr=second_exp_ptr;
		    }
		  
	    }
	  if(second_temp_ptr==exp_ptr && temp_ptr!=second_temp_ptr)
	    {
	      /*	      printf("Special swap! Exp %d using sim %d instad of %d. Exp %d is closer.\n",exp_ptr,second_sim_ptr,sim_ptr,temp_ptr);*/
	      if(mode==SE_PURGE_CH)
		{
		  /*		  printf("Exp: %f - %f\tusing %f - %f \t(%f - %f) (Other exp %f - %f)\n",
			 exp->ChCShift[exp_ptr], exp->ChHShift[exp_ptr],
			 sim->ChCShift[second_sim_ptr], sim->ChHShift[second_sim_ptr],
			 sim->ChCShift[sim_ptr], sim->ChHShift[sim_ptr],
			 exp->ChCShift[temp_ptr],exp->ChHShift[temp_ptr]);*/
		}
	      sim_ptr=second_sim_ptr;
	    }
	}
      if(repetition<2)
	{
	  switch(mode)
	    {
	    case SE_PURGE_C:
	    case SE_PURGE_H:
	      diff=fabs(eShift[exp_ptr] - sShift[sim_ptr]);
	      break;
	    default:
	      diff=SP_2DDelta(exp,exp_ptr,sim,sim_ptr,mode);
	      break;
	    }
	  /* This assignment is so bad that we try to assign other shifts before this to
	     avoid that this experimental shift used a simulated shift that would be
	     better used with another experimental shift. This still means that this
	     assignment can get quite bad. */
	  if(diff>cutoff*5 && secondMinDiff<5*diff)
	    {
	      if(exp_ptr>=eCnt-1)
		{
		  exp_ptr=-1;
		  repetition++;
		}
	      continue;
	    }
	}
      usedsim[sim_ptr]=exp_ptr;
      usedexp[exp_ptr]=sim_ptr;
      mismatches[sim_ptr]=mismatchFlag;
      
      /**/SP_AddAssignment(exp,exp_ptr,sim,sim_ptr,mode);/**/
      
      sim_rem-=1;
      exp_rem-=1;

      /* Do this again for all shifts that had not been assigned previously */
      if(exp_ptr>=eCnt-1)
	{
	  exp_ptr=-1;
	  repetition++;
	}
    }

  SP_CalcErrs(exp, sim, eCnt, usedexp, &diff, &abs_err, &rms_err, sys_err, mode);
  
      MultiExpAssignments(exp, sim, usedsim, &diff, &abs_err, &rms_err, &localNUnassigned, mode);
      
  rms_err=sqrt(rms_err/(sCnt-localNUnassigned));
      /*      for(i=0;i<sCnt;i++)
	{
	  if(usedsim[i]==-1)
	    {
      */ /* Perhaps SP_FindExpAssignment should be used to see if this sim shift has already
	    been assigned to an exp shift. *//*

	      mismatchFlag=0;
	      exp_ptr=0;
	      temp_ptr=0;
	      sim_ptr=i;
	      minDiff=10000;
	      while (exp_ptr<(eCnt))
		{
		  switch (mode)
		    {
		    case SE_PURGE_C:
		      diff=fabs(exp->CShift[exp_ptr]-sim->CShift[sim_ptr]);
		      break;
		    case SE_PURGE_H:
		      diff=fabs(exp->HShift[exp_ptr]-sim->HShift[sim_ptr]);
		      break;
		    case SE_PURGE_CH:
		    case SE_PURGE_HH:
		      diff=SP_2DDelta(exp,exp_ptr,sim,sim_ptr,mode);
		      break;
		    }

		  matched=SP_MatchAssignment(exp,exp_ptr,sim,sim_ptr,mode);
		  
		  if((!(matched&1) && matched&2) ||
		     (!(matched&4) && matched&8))
		    {
					     */ /* Use a very high penalty for this inaccurate (quick) assignment method - this means
						   previous assignments will be used if available *//*
		      diff+=cutoff*SP_MISMATCH_PENALTY;
		    }

		  if(diff < minDiff || (mismatchFlag==0 && minDiff > cutoff && 
					 ((!(matched&1) && matched&2) ||
					  (!(matched&4) && matched&8)) &&
					 diff-cutoff*SP_MISMATCH_PENALTY < minDiff))
		    {
		      if(diff < cutoff || matched&2 || matched&8)
			{
			  if((!(matched&1) && matched&2) ||
			     (!(matched&4) && matched&8))
			    {
			      mismatchFlag=1;
			    }
			  else
			    {
			      mismatchFlag=0;
			    }
			  minDiff=diff;
			  temp_ptr=exp_ptr;
			}
		    }
		  exp_ptr++;
		}
	      exp_ptr=temp_ptr;
	      usedsim[i]=exp_ptr;

	      diff=minDiff;

	      if(fabs(diff-mismatchFlag*cutoff*SP_MISMATCH_PENALTY)>cutoff)
		{
		  localNUnassigned++;
		  sim->nUnassigned++;
		}
												    */ /*	      else
		{
		  SP_AddAssignment(exp,exp_ptr,sim,sim_ptr,mode);
		  }*//*
	    }
	    }
    }

		     */  /* Go through all simulated shifts and sum the differences. This has to be done here to take previously assigned
			    shifts (e.g. C6s) into account *//*
  for(sim_ptr=0; sim_ptr<sCnt; sim_ptr++)
    {
      exp_ptr=usedsim[sim_ptr];

      if(exp_ptr>=0)
	{
	  if(mode==SE_PURGE_C || mode==SE_PURGE_H)
	    {
	      diff=fabs(eShift[exp_ptr] - sShift[sim_ptr]);
	    }
	  else
	    {
	      diff=SP_2DDelta(exp,exp_ptr,sim,sim_ptr,mode);

	    }

	  if(usedexp[exp_ptr]!=sim_ptr)
	    {
	      fail=1;
	      
	      if(fabs(diff)>cutoff)
		{
		  if(diff<0)
		    {
		      diff=-cutoff*SP_CUTOFF_PENALTY;
		    }
		  else
		    {
		      diff=cutoff*SP_CUTOFF_PENALTY;
		    }
		}
	    }
	  else
	    {
	      fail=0;
	      
	      if(mode==SE_PURGE_C || mode==SE_PURGE_H)
		{
		  *sys_err+=eShift[exp_ptr] - sShift[sim_ptr];
		}
	      abs_err+=diff;
	      rms_err+=diff*diff;
	    }

							     *//* Print this assignment *//*
	  if(print==1)
	    {
	    
	      switch(mode)
		{
		case SE_PURGE_C:
		  SP_FindC(sim,exp,sim_ptr,exp_ptr,fail,nr_assign);
		  break;
		case SE_PURGE_H:
		  SP_FindH(sim,exp,sim_ptr,exp_ptr,fail,nr_assign);
		  break;
		case SE_PURGE_CH:
		  SP_FindCh(sim,exp,sim_ptr,exp_ptr,fail,nr_assign);
		  break;
		case SE_PURGE_HH:
		  SP_FindHh(sim,exp,sim_ptr,exp_ptr,fail,nr_assign);
		  break;
		  
		}
	    }

	  if(fabs(diff)<=cutoff)
	    {
	      SP_AddAssignment(exp,exp_ptr,sim,sim_ptr,mode);
	    }
	}
	}*/
  /* Print the final rows in the assignment table showing the errors. */
  if(print==1)
    {
      SP_PrintAssignmentTable(exp, sim, usedsim, usedexp, &abs_err, sys_err, &rms_err, nr_assign, localNUnassigned, mode);
    }
  /*  if(usedsim!=0)
    {
      free (usedsim);
      usedsim=0;
    }
  if(usedexp!=0)
    {
      free (usedexp);
      usedexp=0;
      }*/
  return(abs_err);
}

void SP_CalcErrs(struct BU_Struct *exp, struct BU_Struct *sim, int cnt, int *usedexp, float *err, float *abs_err, float *rms_err, float *sys_err, const unsigned int mode)
{
  int i, exp_ptr, sim_ptr;
  for(i=0;i<cnt;i++)
    {
      exp_ptr=i;
      sim_ptr=usedexp[i];
      
      if(mode==SE_PURGE_C)
	{
	  *err=fabs(exp->CShift[exp_ptr] - sim->CShift[sim_ptr]);
	  *sys_err+=exp->CShift[exp_ptr] - sim->CShift[sim_ptr];
	}
      else if (mode==SE_PURGE_H)
	{
	  *err=fabs(exp->HShift[exp_ptr] - sim->HShift[sim_ptr]);
	  *sys_err+=exp->HShift[exp_ptr] - sim->HShift[sim_ptr];
	}
      else
	{
	  *err=SP_2DDelta(exp,exp_ptr,sim,sim_ptr,mode);
	}
      *abs_err+=*err;
      *rms_err+=(*err)*(*err);     
    }  
}

void MultiExpAssignments(struct BU_Struct *exp, struct BU_Struct *sim, int *usedsim, float *err, float *abs_err, float *rms_err, int *nUnassigned, const unsigned int mode)
{
  int i, mismatchFlag, exp_ptr, sim_ptr, temp_ptr, matched, sCnt, eCnt;
  float ass_hi, ass_lo, cutoff;

  switch(mode)
    {
    case SE_PURGE_C:
      sCnt=sim->CCnt;
      eCnt=exp->CCnt;
      cutoff=SP_C_CUTOFF;
      break;
    case SE_PURGE_H:
      sCnt=sim->HCnt;
      eCnt=exp->HCnt;
      cutoff=SP_H_CUTOFF;
      break;
    case SE_PURGE_CH:
      sCnt=sim->ChCnt;
      eCnt=exp->ChCnt;
      cutoff=SP_CH_CUTOFF;
      break;
    case SE_PURGE_HH:
      eCnt=exp->HhCnt;
      sCnt=sim->HhCnt;
      cutoff=SP_HH_CUTOFF;
      break;
      
    default:
      return;
    }
  for(i=0;i<sCnt;i++)
    {
      if(usedsim[i]==-1)
	{
	  mismatchFlag=0;
	  exp_ptr=0;
	  temp_ptr=0;
	  sim_ptr=i;
	  ass_lo=10000;
	  while (exp_ptr<(eCnt))
	    {
	      switch (mode)
		{
		case SE_PURGE_C:
		  ass_hi=fabs(exp->CShift[exp_ptr]-sim->CShift[sim_ptr]);
		  break;
		case SE_PURGE_H:
		  ass_hi=fabs(exp->HShift[exp_ptr]-sim->HShift[sim_ptr]);
		  break;
		case SE_PURGE_CH:
		case SE_PURGE_HH:
		  ass_hi=SP_2DDelta(exp,exp_ptr,sim,sim_ptr,mode);
		  break;
		}
	      
	      matched=SP_MatchAssignment(exp,exp_ptr,sim,sim_ptr,mode);
	      
	      if(!(matched&1)/* && !(matched&4)*/)
		{
		  ass_hi+=cutoff*SP_MISMATCH_PENALTY;
		}
	      
	      if(ass_hi<ass_lo)
		{
		  if(!(matched&1)/* && !(matched&4)*/)
		    {
		      mismatchFlag=1;
		    }
		  else
		    {
		      mismatchFlag=0;
		    }
		  ass_lo=ass_hi;
		  temp_ptr=exp_ptr;
		}
	      exp_ptr++;
	    }
	  exp_ptr=temp_ptr;
	  usedsim[i]=exp_ptr;
	  *err=ass_lo;
	  /*  fulldiff=diff;*/
	  *err-=mismatchFlag*cutoff*SP_MISMATCH_PENALTY;
	  
	  if(fabs(*err)>cutoff)
	    {
	      *nUnassigned+=1;
	      sim->nUnassigned++;
	      
	      if(*err<0)
		{
		  *err=-cutoff*SP_CUTOFF_PENALTY;
		}
	      else
		{
		  *err=cutoff*SP_CUTOFF_PENALTY;
		}
	    }
	  else
	    {
	      SP_AddAssignment(exp,exp_ptr,sim,sim_ptr,mode);
	      *rms_err+=(*err)*(*err);
	    }
	  *abs_err+=fabs(*err);
	}
    }
}

void SP_PrintAssignmentTable(struct BU_Struct *exp, struct BU_Struct *sim, int *usedsim, int *usedexp, float *abs_err, float *sys_err, float *rms_err, struct SP_assignments *nr_assign, int nUnassigned, const unsigned int mode)
{
  int eCnt, sCnt, sim_ptr, exp_ptr, fail;

  switch(mode)
    {
    case SE_PURGE_C:
      sCnt=sim->CCnt;
      eCnt=exp->CCnt;
      break;
    case SE_PURGE_H:
      sCnt=sim->HCnt;
      eCnt=exp->HCnt;
      break;
    case SE_PURGE_CH:
      sCnt=sim->ChCnt;
      eCnt=exp->ChCnt;
      break;
    case SE_PURGE_HH:
      eCnt=exp->HhCnt;
      sCnt=sim->HhCnt;
      break;
      
    default:
      return;
    }

  for(sim_ptr=0; sim_ptr<sCnt; sim_ptr++)
    {
      exp_ptr=usedsim[sim_ptr];
      if(usedexp[exp_ptr]!=sim_ptr)
	{
	  fail=1;
	}
      else
	{
	  fail=0;
	}
      
      switch (mode)
	{
	case SE_PURGE_C:
	  SP_FindC(sim,exp,sim_ptr,exp_ptr,fail,nr_assign);
	  break;
	case SE_PURGE_H:
	  SP_FindH(sim,exp,sim_ptr,exp_ptr,fail,nr_assign);
	  break;
	case SE_PURGE_CH:
	  SP_FindCh(sim,exp,sim_ptr,exp_ptr,fail,nr_assign);
	  break;
	case SE_PURGE_HH:
	  SP_FindHh(sim,exp,sim_ptr,exp_ptr,fail,nr_assign);
	  break;
	  
	}
    }
  
  if(sys_err)
    {
      *sys_err=*sys_err/(eCnt);
    }
  
  

  printf("Error=%3.2f ppm (%3.2f/shift), RMS error=%3.2f ppm",
	 *abs_err, *abs_err/eCnt, *rms_err);
  if(mode==SE_PURGE_C || mode==SE_PURGE_H)
    {
      printf(", Systematic error=%3.2f ppm", *sys_err);
    }
  printf(".\n");
  
}

/* Assign experimental spectrum */
/* SYNTAX: Assign <sim> <exp>*/
int SP_Assign()
{
  struct BU_Struct *exp, *sim;
  struct BU_Unit *eunit, *sunit, *tempunit;
  struct VA_Variable *onlyBasic;
  int i, j, k=1, size, n, multiFlag=0;
  float error, rms_err, abs_err;
  struct SP_assignments *nr_assign;
  Mols_MolSystem expMolSystem=0;
  Nmr_ShiftList simShiftList;
  ApiString name, shiftListName;
  ApiSet shifts;
  ApiSetIterator shiftIterator;
  Nmr_Shift shift;
  char simName[NODE_NAME_LENGTH], expName[NODE_NAME_LENGTH], simPrefix[NODE_NAME_LENGTH], expPrefix[NODE_NAME_LENGTH];

  if(strncasecmp(PA_Status->CmdLine, "multiassign", PA_Status->CmdPtr-PA_Status->CmdLine)==0)
    {
      multiFlag=1;
    }

  onlyBasic=(struct VA_Variable *)FindNode(&(VariableList.Head), "onlybasicassign");

  /* Check that sim exists. */
  PA_GetString;
  strcpy(simPrefix, PA_Result.String);
  PA_GetString;

  if(PA_Result.String[0]=='/')
    {
      if(projectValid)
	{
	  if(!CC_GetNmrProject(&Global_CcpnProject,&Global_NmrProject))
	    {
	      Error(PA_ERR_FATAL,"Cannot open project.");
	    }
	  name=Nmr_NmrProject_GetName(Global_NmrProject);
	  strcpy(expPrefix, ApiString_Get(name));
      /*	  exp=(struct BU_Struct *)
	    FindNode((struct Node *)&StructList,
	    ApiString_Get(name));*/
	  ApiObject_Free(name);
	}
      /* Find the first structure that does not start with simPrefix */
      else
	{
	  for(exp=(struct BU_Struct *)StructList.Head.Succ; exp->Node.Succ!=NULL && strncasecmp(simPrefix, exp->Node.Name, strlen(simPrefix))==0;
	      exp=(struct BU_Struct *)exp->Node.Succ)
	    {
	    }
	  if(exp->Node.Succ!=NULL)
	    {
	      if(multiFlag)
		{
		  strncpy(expPrefix, exp->Node.Name, strlen(exp->Node.Name)-1);
		  expPrefix[strlen(exp->Node.Name)-1]=0;
		}
	      else
		{
		  strcpy(expPrefix, exp->Node.Name);
		}
	    }
	  else
	    {
	      Error(PA_ERR_FATAL,"Experimental structure not found and/or project not valid.");
	    }
	}
    }
  else
    {
      strcpy(expPrefix, PA_Result.String);
    }

  if(multiFlag)
    {
      sprintf(simName, "%s%d", simPrefix, k);
      sprintf(expName, "%s%d", expPrefix, k++);
    }
  else
    {
      strcpy(simName, simPrefix);
      strcpy(expName, expPrefix);
    }
  sim=(struct BU_Struct *)FindNode((struct Node *)&StructList, simName);
  if (sim==NULL)
    {
      Error(PA_ERR_FAIL,"No such simulated spectrum");
    }
  
  exp=(struct BU_Struct *)FindNode((struct Node *)&StructList, expName);
  if (exp==NULL)
    {
      Error(PA_ERR_FAIL,"No such experimental spectrum");
    }

  if(exp->CCnt>sim->CCnt || exp->HCnt>sim->HCnt || exp->ChCnt>sim->ChCnt || exp->HhCnt>sim->HhCnt )
    {
      Error(PA_ERR_FAIL, "There are more experimental shifts than simulated shifts");
    }

  /* Since multiple assignments can be run and only the "last" one should be saved in the project
     simulated shiftLists have to be emptied if they exist */
  if(projectValid)
    {
      shiftListName=ApiString_New("CASPER simulated");
      simShiftList=Nmr_NmrProject_FindFirstMeasurementList_keyval1(Global_NmrProject, "name", shiftListName);

      /* If the shiftList already exists delete all shifts in it */
      if(simShiftList && !ApiObject_IsNone(simShiftList))
	{
	  shifts=Nmr_ShiftList_GetMeasurements(simShiftList);
	  if(shifts)
	    {
	      shiftIterator=ApiSet_Iterator(shifts);
	      while((shift=ApiSetIterator_Next(shiftIterator)))
		{
		  Impl_DataObject_Delete(shift);
		  ApiObject_Free(shift);
		}
	      ApiObject_Free(shiftIterator);
	      ApiObject_Free(shifts);
	    }
	}
      else
	{
	  simShiftList=Nmr_NmrProject_NewShiftList_reqd(Global_NmrProject);
	  Nmr_ShiftList_SetName(simShiftList, shiftListName);
	  Nmr_ShiftList_SetIsSimulated(simShiftList, ApiBoolean_True());
	}
      ApiObject_Free(shiftListName);
      if(!simShiftList || ApiObject_IsNone(simShiftList) || !Nmr_ShiftList_CheckAllValid(simShiftList, ApiBoolean_False()))
	{
	  printf("shiftList could not be created or not valid. Line %d.\n", __LINE__);
	  projectValid=0;
	}
    }

  while (sim && exp)
    {
      if (sim->Units.Head.Succ==&sim->Units.Tail)
	Error(PA_ERR_FAIL, "'Simulated' spectrum has no structure");

      size=0;

      /* Initialise counter for keeping track of numbers of assignments - first
	 calculate its size. */
      for (tempunit=(struct BU_Unit *)sim->Units.Head.Succ; tempunit->Node.Succ!=NULL;
	   tempunit=(struct BU_Unit *)tempunit->Node.Succ)
	{
	  size++;
	}
      nr_assign=malloc(size*sizeof(struct SP_assignments));

      i=0;

      for (tempunit=(struct BU_Unit *)sim->Units.Head.Succ; tempunit->Node.Succ!=NULL;
	   tempunit=(struct BU_Unit *)tempunit->Node.Succ)
	{
	  strcpy(nr_assign[i].name,tempunit->Node.Name);

	  for(j=0;j<tempunit->Shifts.Type->HeavyCnt;j++)
	    {
	      nr_assign[i].c[j]=0;
	      nr_assign[i].h[j][0]=0;
	      nr_assign[i].h[j][1]=0;
	    }
	  i++;
	}

      if(projectValid)
	{
	  expMolSystem=CC_MakeMoleculeFromBU_Struct(&Global_CcpnProject,
						    sim, exp->Node.Name);
	  if(projectValid)
	    {
	      CC_CreateResonanceGroups(&Global_CcpnProject, 
				       &Global_NmrProject, expMolSystem, exp->CcpnChainCode);
	    }
	}

      BU_CopyStructure(exp, sim);
      strcpy(exp->CcpnChainCode, sim->CcpnChainCode);

      for (tempunit=(struct BU_Unit *)exp->Units.Head.Succ; tempunit->Node.Succ!=NULL;
	   tempunit=(struct BU_Unit *)tempunit->Node.Succ)
	{
	  for(i=0;i<tempunit->Shifts.Type->HeavyCnt;i++)
	    {
	      tempunit->Shifts.C[i]=0;
	      tempunit->Shifts.H[i][0]=0;
	      tempunit->Shifts.H[i][1]=0;
	    }
	}

      if(expMolSystem && !ApiObject_IsNone(expMolSystem))
	{
	  ApiObject_Free(expMolSystem);
	}

      for(i=0; i<BU_MAX_SHIFTS; i++)
	{
	  sim->CAssignments[0][i]=0;
	  sim->CAssignments[1][i]=0;
	  sim->HAssignments[0][i]=0;
	  sim->HAssignments[1][i]=0;
	}
      sim->nCAssignments=0;
      sim->nHAssignments=0;

      sim->nUnassigned=0;

      /* CH spectrum */
      if (exp->ChCnt>0)
	{
	  abs_err=0; rms_err=0;

	  printf("\nCH chemical shifts\n\t%17.17s%17.17s\n",
		 exp->Node.Name, sim->Node.Name);

	  if(SP_AssignMode==SP_AM_CUTOFF || sim->ChCnt>=100)
	    {
	      printf("WARNING: Quick Assignment is used for speed reasons\n");
	      error=SP_AssExpSimQuick(exp,sim,SE_PURGE_CH,1,nr_assign);
	    }
	  else
	    {
	      error=SP_AssExpSim(exp,sim,SE_PURGE_CH,1,nr_assign);
	    }
	  sim->ChFit=error;
	};
  
      /* HH spectrum */
      if (exp->HhCnt>0 && (onlyBasic == NULL || onlyBasic->Value.Value.Float<=0))
	{
	  abs_err=0; rms_err=0;

	  printf("\nHH chemical shifts\n\t%17.17s%17.17s\n",
		 exp->Node.Name, sim->Node.Name);

	  if(SP_AssignMode==SP_AM_CUTOFF || sim->HhCnt>=75)
	    {
	      printf("WARNING: Quick Assignment is used for speed reasons\n");
	      error=SP_AssExpSimQuick(exp,sim,SE_PURGE_HH,1,nr_assign);
	    }
	  else
	    {
	      error=SP_AssExpSim(exp,sim,SE_PURGE_HH,1,nr_assign);
	    }
	  sim->HhFit=error;
	};

      /* 1H spectrum */
      if (exp->HCnt>0)
	{
	  printf("\n1H chemical shifts\n\t%10.10s\t%10.10s\n",
		 exp->Node.Name, sim->Node.Name);

	  if(SP_AssignMode==SP_AM_CUTOFF || 
	     (float)sim->nHAssignments/sim->HCnt>0.80f ||
	     ((float)exp->HCnt/sim->HCnt<0.80f && (float)exp->HCnt/sim->HCnt>0.15f))
	    {
	      error=SP_AssExpSimQuick(exp,sim,SE_PURGE_H,1,nr_assign);
	    }
	  else
	    {
	      error=SP_AssExpSim(exp,sim,SE_PURGE_H,1,nr_assign);
	    }
	  sim->HFit=error;
	}

      /* 13C spectrum */
      if (exp->CCnt>0)
	{
	  printf("\n13C chemical shifts\n\t%10.10s\t%10.10s\n",
		 exp->Node.Name, sim->Node.Name);

	  if(SP_AssignMode==SP_AM_CUTOFF || (float)sim->nCAssignments/sim->CCnt>0.80f || 
	     ((float)exp->CCnt/sim->CCnt<0.80f && (float)exp->CCnt/sim->CCnt>0.15f))
	    {
	      error=SP_AssExpSimQuick(exp,sim,SE_PURGE_C,1,nr_assign);
	    }
	  else
	    {
	      error=SP_AssExpSim(exp,sim,SE_PURGE_C,1,nr_assign);
	    }
	  sim->CFit=error;
	}

      

      for(i=0;i<size;i++)
	{
	  eunit=(struct BU_Unit *)
	    FindNode(&(exp->Units.Head), nr_assign[i].name);
	  sunit=(struct BU_Unit *)
	    FindNode(&(sim->Units.Head), nr_assign[i].name);
	  eunit->CError=0;
	  eunit->HError=0;
	  eunit->Error=sunit->Error;
	  for(j=0;j<eunit->Shifts.Type->HeavyCnt;j++)
	    {
	      if(nr_assign[i].c[j]>0)
		{
		  eunit->Shifts.C[j]/=nr_assign[i].c[j];
		  eunit->CError+=fabs(eunit->Shifts.C[j]-sunit->Shifts.C[j]);
		  /*	      eunit->Error+=fabs(eunit->Shifts.C[j]-sunit->Shifts.C[j]);*/
		}
	      if(nr_assign[i].h[j][0]>0)
		{
		  eunit->Shifts.H[j][0]/=nr_assign[i].h[j][0];
		  eunit->HError+=fabs(eunit->Shifts.H[j][0]-sunit->Shifts.H[j][0]);
		  /*	      eunit->Error+=fabs(eunit->Shifts.H[j][0]-sunit->Shifts.H[j][0]);*/
		}
	      if(nr_assign[i].h[j][1]>0)
		{
		  eunit->Shifts.H[j][1]/=nr_assign[i].h[j][1];
		  eunit->HError+=fabs(eunit->Shifts.H[j][1]-sunit->Shifts.H[j][1]);
		  /*      eunit->Error+=fabs(eunit->Shifts.H[j][1]-sunit->Shifts.H[j][1]);*/
		}
	    }
	}
      sim->TotFit=SP_C_WEIGHT*sim->CFit+SP_H_WEIGHT*sim->HFit+SP_CH_WEIGHT*sim->ChFit+
	SP_HH_WEIGHT*sim->HhFit;

      n=0;
      if(exp->CCnt>0)
	{
	  n+=sim->CCnt;
	}
      if(exp->HCnt>0)
	{
	  n+=sim->HCnt;
	}
      if(exp->ChCnt>0)
	{
	  n+=sim->ChCnt;
	}
      if(exp->HhCnt>0 && (onlyBasic == NULL || onlyBasic->Value.Value.Float<=0))
	{
	  n+=sim->HhCnt;
	}

      exp->CSysErr=sim->CSysErr;
      exp->HSysErr=sim->HSysErr;
  
      sim->fractionAssigned=(float)1-(float)sim->nUnassigned/n;
      sim->TotFit=sim->TotFit/sim->fractionAssigned;

      if(projectValid)
	{
	  CC_AssignSimulatedShifts(&Global_CcpnProject, &Global_NmrProject, sim, simShiftList);
	}

      free(nr_assign);
      if(!multiFlag)
	{
	  break;
	}
      sprintf(simName, "%s%d", simPrefix, k);
      sprintf(expName, "%s%d", expPrefix, k++);
      sim=(struct BU_Struct *)FindNode((struct Node *)&StructList, simName);
      if(sim)
	{
	  exp=(struct BU_Struct *)ME_CreateNode(&StructMethod, &StructList, expName);
	}      
    }

  return(PA_ERR_OK);
}

/* If printfail is 0 no line will be printed if the assignment is larger than the cutoff.
   1 a line will be printed, but the experimental shift will be printed as n.d. */
int SP_FindC(struct BU_Struct *sim, struct BU_Struct *exp, 
	     int s_off, int e_off, char printfail,
	     struct SP_assignments *nr_assign)
{
  Mols_Residue residue;
  ApiSet atoms;
  struct BU_Unit *unit=0, *eunit=0;
  int j, match=1, anomerPos, unitnr;
  float error;
  char unitName[NODE_NAME_LENGTH];

  unitName[0]=0;

  error=exp->CShift[e_off]-sim->CShift[s_off];
  /* Systematic error is increased before checking if the error is within
     cutoff since corrections must take full errors into account to
     work correctly */

  /* no good assignment */
  if (fabs(error)>SP_C_CUTOFF && printfail==1)
    {
      if(error>0)
	error=SP_C_CUTOFF*SP_CUTOFF_PENALTY;
      else
	error=-SP_C_CUTOFF*SP_CUTOFF_PENALTY;
      /* error=0;*/
      match=0;
      printf("\t      n.d.\t%10.2f (%5.2f)\t",sim->CShift[s_off], error);
    }
  else
    {
      printf("\t%10.2f\t%10.2f (%5.2f)\t",exp->CShift[e_off],
	     sim->CShift[s_off], error);
      /*      sim->CSysErr+=error;*/
    }

  unit=sim->CShiftAtom[s_off].unit;
  j=sim->CShiftAtom[s_off].atom[0];
  anomerPos=BU_ANOMER_POS(unit);

  if(match==0)
    printf("(");
  if(anomerPos!=-1 && strcasecmp(unit->Residue->Shifts.Type->CTsuperclass,"subst")==0)
    {
      GFX_PrintUnit(unit->Subst[anomerPos], unitName, 0, 1);
      printf("%-18s [%s] - %d%s-%s", unitName, unit->Subst[anomerPos]->Node.Name,
	     unit->Position+1, unit->Residue->Node.Name,
	     unit->Residue->Shifts.Type->Atom[j].Label);
    }
  else
    {
      GFX_PrintUnit(unit, unitName, 0, 1);
      printf("%-18s [%s] - %s", unitName, unit->Node.Name,
	     unit->Residue->Shifts.Type->Atom[j].Label);
    }
  if(match==0)
    {
      printf(")");
    }
  else
    {
      eunit=(struct BU_Unit *)
	FindNode(&(exp->Units.Head), unit->Node.Name);
      eunit->Shifts.C[j]+=exp->CShift[e_off];
      /*      eunit->CError+=fabs(error);
	      eunit->Error+=fabs(error);*/

      unitnr=0;
      while (strcmp(nr_assign[unitnr].name,unit->Node.Name)!=0 &&
	     unitnr<BU_MAX_UNITS)
	{
	  unitnr++;
	}
      nr_assign[unitnr].c[j]+=1;
      
      if(projectValid)
	{
	  /* Find the CCPN residue that corresponds to this residue. */
	  residue=CC_FindResidueNr(&Global_CcpnProject, exp->Node.Name, exp->CcpnChainCode, eunit->CcpnUnitNr);
	  
	  if(residue && !ApiObject_IsNone(residue) && projectValid)
	    {
	      /* Find the correct atom or atoms in the CCPN project. */
	      atoms=CC_FindAtomsNamed(residue,
				      unit->Residue->Shifts.Type->Atom[j].Label,"C");
	      ApiObject_Free(residue);
	      if(!atoms||ApiSet_Len(atoms)<1)
		{
		  if(atoms && !ApiObject_IsNone(atoms))
		    {
		      ApiObject_Free(atoms);
		    }
		  projectValid=0;
		  puts("");
		  Error(PA_ERR_FAIL,"Atoms not found.");
		}
	      
	      /*	      if(!CC_GetNmrProject_Named(&Global_CcpnProject,&Global_NmrProject,exp->Node.Name))*/
	      if(!CC_GetNmrProject(&Global_CcpnProject, &Global_NmrProject))
		{
		  ApiObject_Free(atoms);
		  projectValid=0;
		  puts("");
		  Error(PA_ERR_FATAL,"CCPN Nmr Project not found!");
		}

	      /* Make the assignment of the shifts to the atoms */
	      CC_AssignPeakToAtoms(&Global_NmrProject,atoms,exp->CShift[e_off]+exp->CCorrection, 0, 1, SE_PURGE_C);

	      ApiObject_Free(atoms);
	    }
	}
    }
  printf("\n");
  return (1);
}

/* If printfail is 0 no line will be printed if the assignment is larger than the cutoff.
   1 a line will be printed, but the experimental shift will be printed as n.d. */
int SP_FindH(struct BU_Struct *sim, struct BU_Struct *exp, 
	     int s_off, int e_off, char printfail,
	     struct SP_assignments *nr_assign)
{
  Mols_Residue residue;
  ApiSet atoms;
  struct BU_Unit *unit=0, *eunit=0;
  int j, k, match=1, anomerPos, unitnr;
  float error;
  char atomname[3];
  char unitName[NODE_NAME_LENGTH];

  unitName[0]=0;

  error=exp->HShift[e_off]-sim->HShift[s_off];
  /* Systematic error is increased before checking if the error is within
     cutoff since corrections must take full errors into account to
     work correctly */

  /* no good assignment */
  if (fabs(error)>SP_H_CUTOFF && printfail==1)
    {
      if(error>0)
	error=SP_H_CUTOFF*SP_CUTOFF_PENALTY;
      else
	error=-SP_H_CUTOFF*SP_CUTOFF_PENALTY;
      /*      error=0;*/
      match=0;
      
      printf("\t      n.d.\t%10.2f (%5.2f)\t",sim->HShift[s_off], error);
    }
  else
    {
      printf("\t%10.2f\t%10.2f (%5.2f)\t",exp->HShift[e_off],
	     sim->HShift[s_off], error);
      /*      sim->HSysErr+=error;*/
    }

  unit=sim->HShiftAtom[s_off].unit;
  j=sim->HShiftAtom[s_off].atom[0];
  k=sim->HShiftAtom[s_off].atom[1];

  anomerPos=BU_ANOMER_POS(unit);

  if(match==0)
    printf("(");
  if(anomerPos!=-1 && strcasecmp(unit->Residue->Shifts.Type->CTsuperclass,"subst")==0)
    {
      GFX_PrintUnit(unit->Subst[anomerPos], unitName, 0, 1);
      printf("%-18s [%s] - %d%s-%s", unitName, unit->Subst[anomerPos]->Node.Name,
	     unit->Position+1, unit->Residue->Node.Name,
	     unit->Residue->Shifts.Type->Atom[j].Label);
    }
  else
    {
      GFX_PrintUnit(unit, unitName, 0, 1);
      printf("%-18s [%s] - %s", unitName, unit->Node.Name,
	     unit->Residue->Shifts.Type->Atom[j].Label);
    }
  /*
    if(k>0)
    {
      printf("'");
    }
  */
  if(unit->Residue->Shifts.Type->Atom[j].HCnt>1)
    {
      if(unit->Shifts.H[j][k]<unit->Shifts.H[j][(k+1)%2] ||
	 (unit->Shifts.H[j][k]==unit->Shifts.H[j][(k+1)%2] && k==1))
	{
	  sprintf(atomname,"H2");
	  printf("'");
	}
      else
	{
	  sprintf(atomname,"H1");
	}
    }
  else
    {
	  sprintf(atomname,"H");
    }
  if(match==0)
    {
      printf(")");
    }
  else
    {
      eunit=(struct BU_Unit *)
	FindNode(&(exp->Units.Head), unit->Node.Name);
      eunit->Shifts.H[j][k]+=exp->HShift[e_off];
      /*      eunit->HError+=fabs(error);
	      eunit->Error+=fabs(error);*/

      unitnr=0;
      while (strcmp(nr_assign[unitnr].name,unit->Node.Name)!=0 &&
	     unitnr<BU_MAX_UNITS)
	{
	  unitnr++;
	}
      nr_assign[unitnr].h[j][k]+=1;
      
      if(projectValid)
	{
	  /* Find the CCPN residue that corresponds to this residue. */
	  residue=CC_FindResidueNr(&Global_CcpnProject, exp->Node.Name, exp->CcpnChainCode, eunit->CcpnUnitNr);
	  
	  if(residue && !ApiObject_IsNone(residue) && projectValid)
	    {
	      /* Find the correct atom or atoms in the CCPN project. */
	      /* If there are two hydrogens on the carbon a special label needs to be used
		 to show which hydrogen this is. */
	      atoms=CC_FindAtomsNamed(residue,
				      unit->Residue->Shifts.Type->Atom[j].Label,atomname);
	      
	      ApiObject_Free(residue);
	      if(!atoms||ApiSet_Len(atoms)<1)
		{
		  if(atoms && !ApiObject_IsNone(atoms))
		    {
		      ApiObject_Free(atoms);
		    }
		  projectValid=0;
		  puts("");
		  Error(PA_ERR_FAIL,"Atoms not found.");
		}
	      
	      /*	      if(!CC_GetNmrProject_Named(&Global_CcpnProject,&Global_NmrProject,exp->Node.Name))*/
	      if(!CC_GetNmrProject(&Global_CcpnProject, &Global_NmrProject))
		{
		  ApiObject_Free(atoms);
		  projectValid=0;
		  puts("");
		  Error(PA_ERR_FATAL,"CCPN Nmr Project not found!");
		}
	      /* Make the assignment of the shifts to the atoms */
	      CC_AssignPeakToAtoms(&Global_NmrProject,atoms,exp->HShift[e_off]+exp->HCorrection, 0, 1, SE_PURGE_H);
	      ApiObject_Free(atoms);
	    }
	}
    }
  printf("\n");
  return (1);
}

/* If printfail is 0 no line will be printed if the assignment is larger than the cutoff.
   1 a line will be printed, but the experimental shift will be printed as n.d. */
int SP_FindCh(struct BU_Struct *sim, struct BU_Struct *exp, 
	      int s_off, int e_off, char printfail,
	      struct SP_assignments *nr_assign)
{
  Mols_Residue residue;
  ApiSet atoms;
  struct BU_Unit *unit=0, *eunit=0;
  int j, k, match=1, anomerPos, unitnr;
  float error;
  char atomname[3];
  char unitName[NODE_NAME_LENGTH];
  
  unitName[0]=0;

  error=SP_2DDelta(exp,e_off,sim,s_off,SE_PURGE_CH);

  /* no good assignment */
  if (error>SP_CH_CUTOFF && printfail==1)
    {
      error=SP_CH_CUTOFF*SP_CUTOFF_PENALTY;
      /*      error=0;*/
      match=0;
      
      printf("\t      n.d.\t %10.2f - %2.2f (%5.2f)\t",sim->ChCShift[s_off],sim->ChHShift[s_off], error);
    }
  else
    {
      printf("\t%10.2f - %2.2f%10.2f - %2.2f (%5.2f)\t",exp->ChCShift[e_off],exp->ChHShift[e_off],
	     sim->ChCShift[s_off],sim->ChHShift[s_off], error);
    }

  unit=sim->ChShiftAtom[s_off].unit;
  j=sim->ChShiftAtom[s_off].atom[0];
  k=sim->ChShiftAtom[s_off].atom[1];

  anomerPos=BU_ANOMER_POS(unit);

  if(match==0)
    printf("(");
  if(anomerPos!=-1 && strcasecmp(unit->Residue->Shifts.Type->CTsuperclass,"subst")==0)
    {
      GFX_PrintUnit(unit->Subst[anomerPos], unitName, 0, 1);
      printf("%-18s [%s] - %d%s-%s", unitName, unit->Subst[anomerPos]->Node.Name,
	     unit->Position+1, unit->Residue->Node.Name,
	     unit->Residue->Shifts.Type->Atom[j].Label);
    }
  else
    {
      GFX_PrintUnit(unit, unitName, 0, 1);
      printf("%-18s [%s] - %s", unitName, unit->Node.Name,
	     unit->Residue->Shifts.Type->Atom[j].Label);
    }
  /*
    if(k>0)
    {
      printf("'");
    }
  */
  if(unit->Residue->Shifts.Type->Atom[j].HCnt>1)
    {
      if(unit->Shifts.H[j][k]<unit->Shifts.H[j][(k+1)%2] ||
	 (unit->Shifts.H[j][k]==unit->Shifts.H[j][(k+1)%2] && k==1))
	{
	  sprintf(atomname,"H2");
	  printf("'");
	}
      else
	{
	  sprintf(atomname,"H1");
	}
    }
  else
    {
	  sprintf(atomname,"H");
    }
  if(match==0)
    {
      printf(")");
    }
  else
    {
      eunit=(struct BU_Unit *)
	FindNode(&(exp->Units.Head), unit->Node.Name);
      eunit->Shifts.C[j]+=exp->ChCShift[e_off];
      eunit->Shifts.H[j][k]+=exp->ChHShift[e_off];
      /*      eunit->CHError+=fabs(error);
	      eunit->Error+=fabs(error);*/

      unitnr=0;
      while (strcmp(nr_assign[unitnr].name,unit->Node.Name)!=0 &&
	     unitnr<BU_MAX_UNITS)
	{
	  unitnr++;
	}
      nr_assign[unitnr].c[j]+=1;
      nr_assign[unitnr].h[j][k]+=1;
      
      if(projectValid)
	{
	  /* Find the CCPN residue that corresponds to this residue. */
	  residue=CC_FindResidueNr(&Global_CcpnProject, exp->Node.Name, exp->CcpnChainCode, eunit->CcpnUnitNr);
	  
	  if(residue && !ApiObject_IsNone(residue) && projectValid)
	    {
	      /* Find the correct atom or atoms in the CCPN project. */
	      /* Find the correct atom or atoms in the CCPN project. */
	      atoms=CC_FindAtomsNamed(residue,
				      unit->Residue->Shifts.Type->Atom[j].Label,"C");
	      if(!atoms||ApiSet_Len(atoms)<1)
		{
		  if(atoms && !ApiObject_IsNone(atoms))
		    {
		      ApiObject_Free(atoms);
		    }
		  ApiObject_Free(residue);
		  projectValid=0;
		  puts("");
		  Error(PA_ERR_FAIL,"Atoms not found.");
		}
	      
	      /*	      if(!CC_GetNmrProject_Named(&Global_CcpnProject,&Global_NmrProject,exp->Node.Name))*/
	      if(!CC_GetNmrProject(&Global_CcpnProject, &Global_NmrProject))
		{
		  ApiObject_Free(atoms);
		  ApiObject_Free(residue);
		  projectValid=0;
		  puts("");
		  Error(PA_ERR_FATAL,"CCPN Nmr Project not found!");
		}
	      /* Make the assignment of the shifts to the atoms */
	      CC_AssignPeakToAtoms(&Global_NmrProject,atoms,exp->ChCShift[e_off]+exp->CCorrection, exp->ChHShift[e_off]+exp->HCorrection, 1, SE_PURGE_CH);
		
		ApiObject_Free(atoms);
	      
	      /* Assign the hydrogen shift too. */
	      /* If there are two hydrogens on the carbon a special label needs to be used
		 to show that this is the first hydrogen. */
	      if(unit->Residue->Shifts.Type->Atom[j].HCnt==1)
		{
		  sprintf(atomname,"H");
		}
	      else
		{
		  sprintf(atomname,"H%d",k+1);
		}
	      atoms=CC_FindAtomsNamed(residue,
				      unit->Residue->Shifts.Type->Atom[j].Label,atomname);
	      
	      ApiObject_Free(residue);
	      if(!atoms||ApiSet_Len(atoms)<1)
		{
		  if(atoms && !ApiObject_IsNone(atoms))
		    {
		      ApiObject_Free(atoms);
		    }
		  projectValid=0;
		  puts("");
		  Error(PA_ERR_FAIL,"Atoms not found.");
		}
	      
	      /* Make the assignment of the shifts to the atoms */
	      if(projectValid)
		{
		  CC_AssignPeakToAtoms(&Global_NmrProject,atoms,exp->ChCShift[e_off]+exp->CCorrection, exp->ChHShift[e_off]+exp->HCorrection, 2, SE_PURGE_CH);
		}
	      ApiObject_Free(atoms);
	    }
	}
    }
  printf("\n");
  return (1);
}

/* If printfail is 0 no line will be printed if the assignment is larger than the cutoff.
   1 a line will be printed, but the experimental shift will be printed as n.d. */
int SP_FindHh(struct BU_Struct *sim, struct BU_Struct *exp, 
	      int s_off, int e_off, char printfail,
	      struct SP_assignments *nr_assign)
{
  Mols_Residue residue;
  ApiSet atoms;
  struct BU_Unit *unit=0, *eunit=0;
  int j, k, l, m, match=1, anomerPos, unitnr;
  float error;
  char atomname1[3], atomname2[3];
  char unitName[NODE_NAME_LENGTH];
  
  unitName[0]=0;

  error=SP_2DDelta(exp,e_off,sim,s_off,SE_PURGE_HH);

  /* no good assignment */
  if (error>SP_HH_CUTOFF && printfail==1)
    {
      error=SP_HH_CUTOFF*SP_CUTOFF_PENALTY;
      /*      error=0;*/
      match=0;
      
      printf("\t      n.d.\t%10.2f - %2.2f (%5.2f)\t",sim->HhHiShift[s_off],sim->HhLoShift[s_off], error);
    }
  else
    {
      printf("\t%10.2f - %2.2f%10.2f - %2.2f (%5.2f)\t",exp->HhHiShift[e_off],exp->HhLoShift[e_off],
	     sim->HhHiShift[s_off],sim->HhLoShift[s_off], error);
    }

  unit=sim->HhHiShiftAtom[s_off].unit;
  j=sim->HhHiShiftAtom[s_off].atom[0];
  k=sim->HhHiShiftAtom[s_off].atom[1];
  l=sim->HhLoShiftAtom[s_off].atom[0];
  m=sim->HhLoShiftAtom[s_off].atom[1];

  anomerPos=BU_ANOMER_POS(unit);

  if(match==0)
    printf("(");
  if(anomerPos!=-1 && strcasecmp(unit->Residue->Shifts.Type->CTsuperclass,"subst")==0)
    {
      GFX_PrintUnit(unit->Subst[anomerPos], unitName, 0, 1);
      printf("%-18s [%s] - %d%s-%s", unitName, unit->Subst[anomerPos]->Node.Name,
	     unit->Position+1, unit->Residue->Node.Name,
	     unit->Residue->Shifts.Type->Atom[j].Label);
    }
  else
    {
      GFX_PrintUnit(unit, unitName, 0, 1);
      printf("%-18s [%s] - %s", unitName, unit->Node.Name,
	     unit->Residue->Shifts.Type->Atom[j].Label);
    }
  /*
  if(k>0)
    {
      printf("'");
    }
  else
    {
      printf(" ");
    }
  */
  if(unit->Residue->Shifts.Type->Atom[j].HCnt>1)
    {
      if(unit->Shifts.H[j][k]<unit->Shifts.H[j][(k+1)%2] ||
	 (unit->Shifts.H[j][k]==unit->Shifts.H[j][(k+1)%2] && k==1))
	{
	  sprintf(atomname1,"H2");
	  printf("'");
	}
      else
	{
	  sprintf(atomname1,"H1");
	  printf(" ");
	}
    }
  else
    {
      sprintf(atomname1,"H");
      printf(" ");
    }
  printf(" ");
  if(strcasecmp(unit->Residue->Shifts.Type->CTsuperclass,"subst")==0)
    {
      printf("- %d%s-%s", unit->Position+1, unit->Residue->Node.Name,
	     unit->Residue->Shifts.Type->Atom[l].Label);
    }
  else
    {
      printf("- %s", unit->Residue->Shifts.Type->Atom[l].Label);
    }
  /*
  if(m>0)
    {
      printf("'");
    }
  */
  if(unit->Residue->Shifts.Type->Atom[l].HCnt>1)
    {
      if(unit->Shifts.H[l][m]<unit->Shifts.H[l][(m+1)%2] ||
	 (unit->Shifts.H[l][m]==unit->Shifts.H[l][(m+1)%2] && m==1))
	{
	  sprintf(atomname2,"H2");
	  printf("'");
	}
      else
	{
	  sprintf(atomname2,"H1");
	}
    }
  else
    {
	  sprintf(atomname2,"H");
    }
  if(match==0)
    {
      printf(")");
    }
  else
    {
      eunit=(struct BU_Unit *)
	FindNode(&(exp->Units.Head), unit->Node.Name);
      eunit->Shifts.H[j][k]+=exp->HhHiShift[e_off];
      eunit->Shifts.H[l][m]+=exp->HhLoShift[e_off];
      /*      eunit->HHError+=fabs(error);
	      eunit->Error+=fabs(error);*/

      unitnr=0;
      while (strcmp(nr_assign[unitnr].name,unit->Node.Name)!=0 &&
	     unitnr<BU_MAX_UNITS)
	{
	  unitnr++;
	}
      nr_assign[unitnr].h[j][k]+=1;
      nr_assign[unitnr].h[l][m]+=1;
      
      if(projectValid)
	{
	  /* Find the CCPN residue that corresponds to this residue. */
	  residue=CC_FindResidueNr(&Global_CcpnProject, exp->Node.Name, exp->CcpnChainCode, eunit->CcpnUnitNr);
	  
	  if(residue && !ApiObject_IsNone(residue) && projectValid)
	    {
	      /* Find the correct atom or atoms in the CCPN project. */
	      atoms=CC_FindAtomsNamed(residue,
				      unit->Residue->Shifts.Type->Atom[j].Label,atomname1);
	      if(!atoms||ApiSet_Len(atoms)<1)
		{
		  if(atoms && !ApiObject_IsNone(atoms))
		    {
		      ApiObject_Free(atoms);
		    }
		  ApiObject_Free(residue);
		  projectValid=0;
		  puts("");
		  Error(PA_ERR_FAIL,"Atoms not found.");
		}
	      
	      /* Make the assignment of the shifts to the atoms */
	      CC_AssignPeakToAtoms(&Global_NmrProject,atoms,exp->HhHiShift[e_off]+exp->HCorrection, exp->HhLoShift[e_off]+exp->HCorrection, 1, SE_PURGE_HH);
	      ApiObject_Free(atoms);
	      
	      /* Assign the other hydrogen shift too. */
	      atoms=CC_FindAtomsNamed(residue,
				      unit->Residue->Shifts.Type->Atom[l].Label,atomname2);
	      
	      ApiObject_Free(residue);
	      if(!atoms||ApiSet_Len(atoms)<1)
		{
		  if(atoms && !ApiObject_IsNone(atoms))
		    {
		      ApiObject_Free(atoms);
		    }
		  projectValid=0;
		  puts("");
		  Error(PA_ERR_FAIL,"Atoms not found.");
		}
	      
	      /* Make the assignment of the shifts to the atoms */
	      CC_AssignPeakToAtoms(&Global_NmrProject,atoms,exp->HhHiShift[e_off]+exp->HCorrection, exp->HhLoShift[e_off]+exp->HCorrection, 2, SE_PURGE_HH);
	      ApiObject_Free(atoms);
	    }
	}
    }

  printf("\n");
  return (1);
}




/* This function returns the index (in ChCShift (and ChHShift)) to the twin
   atom of the atom corresponding to the index j in that list of CH shifts */
int SP_FindC6Twin(struct BU_Struct *sim, int j)
{
  struct BU_Unit *unit;
  int atom, HCnt, nr, ptr;

  unit=sim->ChShiftAtom[j].unit;
  atom=sim->ChShiftAtom[j].atom[0];
  nr=sim->ChShiftAtom[j].atom[1];

  HCnt=unit->Shifts.Type->Atom[atom].HCnt;

  if(HCnt<2)
    {
      return (-1);
    }

  /* Look for the _other_ atom in the twin pair */
  if(nr==0)
    {
      nr++;
    }
  else if(nr==1)
    {
      nr--;
    }

  ptr=j;
  /* Find the first C chemical shift with the same value as this C chemical
     shift. That will be the starting point when looking for the twin. */
  while(ptr>0 && fabs(sim->ChCShift[ptr]-sim->ChCShift[j])<0.00001)
    {
      ptr--;
    }
  ptr++;
  while(ptr<sim->ChCnt && fabs(sim->ChCShift[ptr]-sim->ChCShift[j])<0.00001)
    {
      /* Check if this is the unit and atom we are looking for */
      if(unit==sim->ChShiftAtom[ptr].unit &&
	 atom==sim->ChShiftAtom[ptr].atom[0] &&
	 nr==sim->ChShiftAtom[ptr].atom[1])
	{
	  return (ptr);
	}
      ptr++;
    }
  /* No twin found. */
  return (-1);
}

/* Uses chemical shifts of specific atoms in the *exp structure to set the
   assignments of *usedexp and *usedsim - showing which shifts have been
   connected. This means that signals specified beforehand, e.g. from a
   CCPN project are automatically matched with their simulated counterparts */
int SP_PopulateAssignments(struct BU_Struct *exp, struct BU_Struct *sim, int *usedexp, int *usedsim, const unsigned int mode)
{
  int i, j;

  if(mode==SE_PURGE_C)
    {
      for(i=0; i<exp->CCnt; i++)
	{
	  if(usedexp[i]!=-1 || exp->CShiftAtom[i].unit==0 || exp->CShiftAtom[i].atom[0]==-1)
	    {
	      continue;
	    }
	  for(j=0; j<sim->CCnt; j++)
	    {
	      if(usedsim[j]!=-1)
		{
		  continue;
		}
	      if(i<exp->CCnt && j<sim->CCnt &&
		 strcmp(exp->CShiftAtom[i].unit->Node.Name,sim->CShiftAtom[j].unit->Node.Name)==0 &&
		 exp->CShiftAtom[i].atom[0]==sim->CShiftAtom[j].atom[0])
		{
		  usedexp[i]=j;
		  usedsim[j]=i;
		  j=sim->CCnt;
		}
	    }
	}
    }
  else if(mode==SE_PURGE_H)
    {
      for(i=0; i<exp->HCnt; i++)
	{
	  if(usedexp[i]!=-1 || exp->HShiftAtom[i].unit==0 || exp->HShiftAtom[i].atom[0]==-1)
	    {
	      continue;
	    }
	  for(j=0; j<sim->HCnt; j++)
	    {
	      if(usedsim[j]!=-1)
		{
		  continue;
		}
	      if(i<exp->HCnt && j<sim->HCnt &&
		    strcmp(exp->HShiftAtom[i].unit->Node.Name,sim->HShiftAtom[j].unit->Node.Name)==0 &&
		    exp->HShiftAtom[i].atom[0]==sim->HShiftAtom[j].atom[0] &&
		    exp->HShiftAtom[i].atom[1]==sim->HShiftAtom[j].atom[1])
		{
		  usedexp[i]=j;
		  usedsim[j]=i;
		  j=sim->HCnt;
		}
	    }
	}
    }
  else if(mode==SE_PURGE_CH)
    {
      for(i=0; i<exp->ChCnt; i++)
	{
	  if(usedexp[i]!=-1 || exp->ChShiftAtom[i].unit==0 || exp->ChShiftAtom[i].atom[0]==-1)
	    {
	      continue;
	    }
	  for(j=0; j<sim->ChCnt; j++)
	    {
	      if(usedsim[j]!=-1)
		{
		  continue;
		}
	      if(i<exp->ChCnt && j<sim->ChCnt &&
		    strcmp(exp->ChShiftAtom[i].unit->Node.Name,sim->ChShiftAtom[j].unit->Node.Name)==0 &&
		    exp->ChShiftAtom[i].atom[0]==sim->ChShiftAtom[j].atom[0] &&
		    exp->ChShiftAtom[i].atom[1]==sim->ChShiftAtom[j].atom[1])
		{
		  usedexp[i]=j;
		  usedsim[j]=i;
		  j=sim->ChCnt;
		}
	    }
	}
    }
  
  else if(mode==SE_PURGE_HH)
    {
      for(i=0; i<exp->HhCnt; i++)
	{
	  if(usedexp[i]!=-1|| exp->HhHiShiftAtom[i].unit==0 || exp->HhHiShiftAtom[i].atom[0]==-1 ||
	     exp->HhLoShiftAtom[i].unit==0 || exp->HhLoShiftAtom[i].atom[0]==-1)
	    {
	      continue;
	    }
	  for(j=0; j<sim->HhCnt; j++)
	    {
	      if(usedsim[j]!=-1)
		{
		  continue;
		}
	      if(i<exp->HhCnt && j<sim->HhCnt &&
		 strcmp(exp->HhHiShiftAtom[i].unit->Node.Name,sim->HhHiShiftAtom[j].unit->Node.Name)==0 &&
		 strcmp(exp->HhLoShiftAtom[i].unit->Node.Name,sim->HhLoShiftAtom[j].unit->Node.Name)==0 &&
		 exp->HhHiShiftAtom[i].atom[0]==sim->HhHiShiftAtom[j].atom[0] &&
		 exp->HhHiShiftAtom[i].atom[1]==sim->HhHiShiftAtom[j].atom[1] &&
		 exp->HhLoShiftAtom[i].atom[0]==sim->HhLoShiftAtom[j].atom[0] &&
		 exp->HhLoShiftAtom[i].atom[1]==sim->HhLoShiftAtom[j].atom[1])
		{
		  usedexp[i]=j;
		  usedsim[j]=i;
		  j=sim->HhCnt;
		}
	    }
	}
    }
  return(PA_ERR_OK);
}


/* This function checks if the simulated shift is assigned to this experimental shift or to another experimental
   shift. */
int SP_MatchAssignment(const struct BU_Struct *exp, const unsigned int exp_ptr,
		       struct BU_Struct *sim, const unsigned int sim_ptr, unsigned int mode)
{
  float eCShift=0, eHShift=0, sCShift=0, sHShift=0;
  int i;
  char flag=0; /* 1=The simulated CShift (or HhHi) is assigned to this experimental CShift
		  2=CShift (or HhHi) assigned to another experimental CShift
		  4=The simulated HShift (or HhLo) is assigned to this experimental HShift
		  8=HShift (or HhLo) assigned to another experimental HShift
	       */

  switch(mode)
    {
    case SE_PURGE_C:
      if(sim->nCAssignments <=0)
	{
	  return 0;
	}
      eCShift=exp->CShift[exp_ptr];
      sCShift=sim->CShift[sim_ptr];
      break;
    case SE_PURGE_H:
      if(sim->nHAssignments <=0)
	{
	  return 0;
	}
      eHShift=exp->HShift[exp_ptr];
      sHShift=sim->HShift[sim_ptr];
      break;
    case SE_PURGE_CH:
      if(sim->nCAssignments <=0 && sim->nHAssignments <=0)
	{
	  return 0;
	}
      eCShift=exp->ChCShift[exp_ptr];
      eHShift=exp->ChHShift[exp_ptr];
      sCShift=sim->ChCShift[sim_ptr];
      sHShift=sim->ChHShift[sim_ptr];
      break;
    case SE_PURGE_HH:
      if(sim->nHAssignments <=0)
	{
	  return 0;
	}
      eCShift=exp->HhHiShift[exp_ptr];
      eHShift=exp->HhLoShift[exp_ptr];
      sCShift=sim->HhHiShift[sim_ptr];
      sHShift=sim->HhLoShift[sim_ptr];
      break;
      
    default:
      return 0;
    }

  if(mode==SE_PURGE_C||mode==SE_PURGE_CH)
    {
      i=QuickFind(sim->CAssignments[1],0,sim->nCAssignments-1,sCShift);
      if(i!=-1)
	{
	  for(;i<sim->nCAssignments && fabs(sim->CAssignments[1][i]-sCShift)<0.0001 && !(flag&1);
	      i++)
	    {
	      if(fabs(sim->CAssignments[0][i]-eCShift)<0.0001)
		{
		  flag=flag|1; /* Already assigned to this shift */
		}
	      else
		{
		  flag=flag|2; /* Assigned to another shift */
		}	      
	    }
	}
    }
  if(mode==SE_PURGE_H||mode==SE_PURGE_HH||mode==SE_PURGE_CH)
    {
      i=QuickFind(sim->HAssignments[1],0,sim->nHAssignments-1,sHShift);
      if(i!=-1)
	{
	  for(;i<sim->nHAssignments && fabs(sim->HAssignments[1][i]-sHShift)<0.0001 && !(flag&4);
	      i++)
	    {
	      if(fabs(sim->HAssignments[0][i]-eHShift)<0.0001)
		{
		  flag=flag|4; /* Already assigned to this shift */
		}
	      else
		{
		  flag=flag|8; /* Assigned to another shift */
		}
	    }
	}
    }

  return(flag);
}

int SP_AddAssignment(struct BU_Struct *exp, const unsigned int exp_ptr, struct BU_Struct *sim,
		     const unsigned int sim_ptr, const unsigned int mode)
{
  float eCShift, eHShift, eHShift2, sCShift, sHShift, sHShift2;
  struct BU_ShiftToAtom *eCShiftAtom=0, *eHShiftAtom=0, *eHShiftAtom2=0;
  struct BU_ShiftToAtom *sCShiftAtom=0, *sHShiftAtom=0, *sHShiftAtom2=0;
  int i=0,j, found;

  switch(mode)
    {
    case SE_PURGE_C:
      eCShift=exp->CShift[exp_ptr];
      sCShift=sim->CShift[sim_ptr];
      eCShiftAtom=&exp->CShiftAtom[exp_ptr];
      sCShiftAtom=&sim->CShiftAtom[sim_ptr];
      break;
    case SE_PURGE_H:
      eHShift=exp->HShift[exp_ptr];
      sHShift=sim->HShift[sim_ptr];
      eHShiftAtom=&exp->HShiftAtom[exp_ptr];
      sHShiftAtom=&sim->HShiftAtom[sim_ptr];
      break;
    case SE_PURGE_CH:
      eCShift=exp->ChCShift[exp_ptr];
      eHShift=exp->ChHShift[exp_ptr];
      sCShift=sim->ChCShift[sim_ptr];
      sHShift=sim->ChHShift[sim_ptr];
      eCShiftAtom=&exp->ChShiftAtom[exp_ptr];
      eHShiftAtom=&exp->ChShiftAtom[exp_ptr];
      sCShiftAtom=&sim->ChShiftAtom[sim_ptr];
      sHShiftAtom=&sim->ChShiftAtom[sim_ptr];
      break;
    case SE_PURGE_HH:
      eHShift=exp->HhHiShift[exp_ptr];
      eHShift2=exp->HhLoShift[exp_ptr];
      sHShift=sim->HhHiShift[sim_ptr];
      sHShift2=sim->HhLoShift[sim_ptr];
      eHShiftAtom=&exp->HhHiShiftAtom[exp_ptr];
      eHShiftAtom2=&exp->HhLoShiftAtom[exp_ptr];
      sHShiftAtom=&sim->HhHiShiftAtom[sim_ptr];
      sHShiftAtom2=&sim->HhLoShiftAtom[sim_ptr];
      break;
      
    default:
      return 0;
      break;
    }
  
  if(sCShiftAtom!=0)
    {
      eCShiftAtom=sCShiftAtom;
    }
  if(sHShiftAtom!=0)
    {
      eHShiftAtom=sHShiftAtom;
    }
  if(sHShiftAtom2!=0)
    {
      eHShiftAtom2=sHShiftAtom2;
    }

  if(mode==SE_PURGE_C||mode==SE_PURGE_CH)
    {
      found=0;
      i=QuickFind(sim->CAssignments[1],0,sim->nCAssignments-1,sCShift);
      if(i!=-1)
	{
	  /* Check that the assignment doesn't already exist. */
	  for(j=i;j<sim->nCAssignments && fabs(sim->CAssignments[1][j]-sCShift)<0.0001;j++)
	    {
	      if(fabs(sim->CAssignments[0][j]-eCShift)<0.0001)
		{
		  found=1;
		}
	    }
	}
      else
	{
	  for(i=sim->nCAssignments;i>0 && sCShift>sim->CAssignments[1][i-1];i--);
	}
      if(found==0 && sim->nCAssignments < BU_MAX_SHIFTS-1)
	{
	  j=sim->nCAssignments;
	  while(j>i)
	    {
	      sim->CAssignments[0][j]=sim->CAssignments[0][j-1];
	      sim->CAssignments[1][j]=sim->CAssignments[1][j-1];
	      j--;
	    }
	  sim->CAssignments[0][i]=eCShift;
	  sim->CAssignments[1][i]=sCShift;
	  sim->nCAssignments++;
	}
    }
  if(mode!=SE_PURGE_C)
    {
      found=0;
      i=QuickFind(sim->HAssignments[1],0,sim->nHAssignments-1,sHShift);
      if(i!=-1)
	{
	  /* Check that the assignment doesn't already exist. */
	  for(j=i;j<sim->nHAssignments && fabs(sim->HAssignments[1][j]-sHShift)<0.0001;j++)
	    {
	      if(fabs(sim->HAssignments[0][j]-eHShift)<0.0001)
		{
		  found=1;
		}
	    }
	}
      else
	{
	  for(i=sim->nHAssignments;i>0 && sHShift>sim->HAssignments[1][i-1];i--);
	}
      if(found==0 && sim->nHAssignments < BU_MAX_SHIFTS-1)
	{
	  j=sim->nHAssignments;
	  while(j>i)
	    {
	      sim->HAssignments[0][j]=sim->HAssignments[0][j-1];
	      sim->HAssignments[1][j]=sim->HAssignments[1][j-1];
	      j--;
	    }
	  sim->HAssignments[0][i]=eHShift;
	  sim->HAssignments[1][i]=sHShift;
	  sim->nHAssignments++;
	}
    }
  switch (mode)
    {
    case SE_PURGE_C:
      /*      SP_AssignOtherShiftsToAtom(exp, eCShift, 0, eCShiftAtom, 0, mode);*/
      break;
    case SE_PURGE_H:
      /*      SP_AssignOtherShiftsToAtom(exp, eHShift, 0, eHShiftAtom, 0, mode);*/
      break;
    case SE_PURGE_CH:
      /*      SP_AssignOtherShiftsToAtom(exp, eCShift, eHShift, eCShiftAtom, eHShiftAtom, mode);*/
      break;
    case SE_PURGE_HH:
      /*      SP_AssignOtherShiftsToAtom(exp, eHShift, eHShift2, eHShiftAtom, eHShiftAtom2, mode);*/
      break;
      
    }
  return(i);
}

/* This function makes chemical shift-atom assignments in other shift lists than the one used when
   calling this function.
   Shift1 (and shift2) is already assigned to atom1 (and atom2 respectively) - at least in the shift list
   specified by mode.
   This function then looks for this chemical shift in the other shift lists (experimental methods) and
   assigns identical chemical shifts to this atom.
   Important to remember that mode is the mode used when calling this function, which means that it is
   all other shift lists that will be assigned. */
void SP_AssignOtherShiftsToAtom(struct BU_Struct *exp, float shift1, float shift2, struct BU_ShiftToAtom *atom1, struct BU_ShiftToAtom *atom2, const unsigned int mode)
{
  float *list;
  int cnt, ptr, i, flag, nC=0, nH=0, nH2=0, nTemp;

  if(atom2==0)
    {
      atom2=atom1;
    }

  /* Assign CShift */
  if(mode!=SE_PURGE_C && shift1>10)
    {
      list=exp->CShift;
      cnt=exp->CCnt;

      ptr=QuickFind(list, 0, cnt-1, shift1);

      if(ptr>=0)
	{
	  i=-1;
	  flag=0;
	  nTemp=0;
	  /* Find the first chemical shift of this value that has not been assigned to anything else */
	  for(; ptr<cnt && fabs(shift1-list[ptr])<0.00001; ptr++)
	    {
	      nTemp++;
	      if(exp->CShiftAtom[ptr].unit==0 && exp->CShiftAtom[ptr].atom[0]==-1)
		{
		  i=ptr;
		}
	      else if(exp->CShiftAtom[ptr].unit==atom1->unit && exp->CShiftAtom[ptr].atom[0]==atom1->atom[0])
		{
		  flag=1;
		}
	    }
	  if(nTemp>nC)
	    {
	      nC=nTemp;
	    }
	  if(i>=0)
	    {
	      ptr=i;
	      /* Check that we still point at a chemical shift identical to the one we're looking for */
	      if(ptr<cnt && exp->CShiftAtom[ptr].unit==0 && exp->CShiftAtom[ptr].atom[0]==-1 && ptr>=0 && 
		 flag==0 && fabs(shift1-list[ptr])<0.00001)
		{
		  exp->CShiftAtom[ptr].unit=atom1->unit;
		  exp->CShiftAtom[ptr].atom[0]=atom1->atom[0];
		  exp->CShiftAtom[ptr].atom[1]=atom1->atom[1];
		}
	    }
	}
    }
  /* Assign HShift */
  if(mode!=SE_PURGE_H)
    {
      list=exp->HShift;
      cnt=exp->HCnt;
      /* This (shift1) is actually only for HH 
	 TODO: consider if this part and the next (shift2<10) should be put in a separate function */
      if(shift1<10)
	{
	  ptr=QuickFind(list, 0, cnt-1, shift1);
	  
	  if(ptr>=0)
	    {
	      i=-1;
	      flag=0;
	      nTemp=0;
	      /* Find the first chemical shift of this value that has not been assigned to anything else */
	      for(; ptr<cnt && fabs(shift1-list[ptr])<0.00001; ptr++)
		{
		  nTemp++;
		  if(exp->HShiftAtom[ptr].unit==0 && exp->HShiftAtom[ptr].atom[0]==-1)
		    {
		      i=ptr;
		    }
		  else if(exp->HShiftAtom[ptr].unit==atom1->unit && exp->HShiftAtom[ptr].atom[0]==atom1->atom[0] &&
			  exp->HShiftAtom[ptr].atom[1]==atom1->atom[1])
		    {
		      flag=1;
		    }
		}
	      if(nTemp>nH)
		{
		  nH=nTemp;
		}
	      if(i>=0)
		{
		  ptr=i;
		  /* Check that we still point at a chemical shift identical to the one we're looking for */
		  if(ptr<cnt && exp->HShiftAtom[ptr].unit==0 && exp->HShiftAtom[ptr].atom[0]==-1 && ptr>=0 &&
		     flag==0 && fabs(shift1-list[ptr])<0.00001)
		    {
		      exp->HShiftAtom[ptr].unit=atom1->unit;
		      exp->HShiftAtom[ptr].atom[0]=atom1->atom[0];
		      exp->HShiftAtom[ptr].atom[1]=atom1->atom[1];
		    }
		}
	    }
	}
      if(shift2<10)
	{
	  ptr=QuickFind(list, 0, cnt-1, shift2);
	  
	  if(ptr>=0)
	    {
	      i=-1;
	      flag=0;
	      nTemp=0;
	      /* Find the first chemical shift of this value that has not been assigned to anything else */
	      for(; ptr<cnt && fabs(shift2-list[ptr])<0.00001; ptr++)
		{
		  if(exp->HShiftAtom[ptr].unit==0 && exp->HShiftAtom[ptr].atom[0]==-1)
		    {
		      i=ptr;
		    }
		  else if(exp->HShiftAtom[ptr].unit==atom2->unit && exp->HShiftAtom[ptr].atom[0]==atom2->atom[0] &&
			  exp->HShiftAtom[ptr].atom[1]==atom2->atom[1])
		    {
		      flag=1;
		    }
		}
	      if(nTemp>nH2)
		{
		  nH2=nTemp;
		}
	      if(i>=0)
		{
		  ptr=i;
		  /* Check that we still point at a chemical shift identical to the one we're looking for */
		  if(ptr<cnt && exp->HShiftAtom[ptr].unit==0 && exp->HShiftAtom[ptr].atom[0]==-1 && ptr>=0 &&
		     flag==0 && fabs(shift2-list[ptr])<0.00001)
		    {
		      exp->HShiftAtom[ptr].unit=atom2->unit;
		      exp->HShiftAtom[ptr].atom[0]=atom2->atom[0];
		      exp->HShiftAtom[ptr].atom[1]=atom2->atom[1];
		    }
		}
	    }
	}
    }
  /* Assign ChShift */
  if(mode!=SE_PURGE_CH && shift1>10 && shift2<10)
    {
      list=exp->ChCShift;
      cnt=exp->ChCnt;

      ptr=QuickFind(list, 0, cnt-1, shift1);

      if(ptr>=0)
	{
	  i=-1;
	  flag=0;
	  nTemp=0;
	  /* Check if this Ch shift is larger than the proton shift we are looking for. If it is continue
	     looking */
	  for(;ptr<cnt && exp->ChHShift[ptr]>shift2 && fabs(shift1-list[ptr])<0.00001;ptr++)
	    {
	    }
	    
	  /* Find the first chemical shift of this value that has not been assigned to anything else */
	  for(;	ptr<cnt && fabs(shift1-list[ptr])<0.00001 && fabs(shift2-exp->ChHShift[ptr])<0.00001; ptr++)
	    {
	      nTemp++;
	      if(exp->ChShiftAtom[ptr].unit==0 && exp->ChShiftAtom[ptr].atom[0]==-1)
		{
		  i=ptr;
		}
	      else if(exp->ChShiftAtom[ptr].unit==atom1->unit && exp->ChShiftAtom[ptr].atom[0]==atom1->atom[0] &&
		      exp->ChShiftAtom[ptr].atom[1]==atom1->atom[1])
		{
		  flag=1;
		}
	    }
	  /* If this atom had two hydrogens two carbon entries with the same chemical shifts would be
	     expected in the shift list and does not mean that the actual number of carbons are higher */
	  nTemp-=(atom1->unit->Shifts.Type->Atom[atom1->atom[0]].HCnt-1);
	  /* Only the nC can easily be counted - otherwise the whole list must be checked */
	  if(nTemp>nC)
	    {
	      nC=nTemp;
	    }
	  if(i>=0)
	    {
	      ptr=i;
	      /* Check that we still point at a chemical shift identical to the one we're looking for */
	      if(ptr<cnt && exp->ChShiftAtom[ptr].unit==0 && exp->ChShiftAtom[ptr].atom[0] && ptr>=0 &&
		 flag==0 && fabs(shift1-list[ptr])<0.00001 && fabs(shift2-exp->ChHShift[ptr])<0.00001)
		{
		  exp->ChShiftAtom[ptr].unit=atom1->unit;
		  exp->ChShiftAtom[ptr].atom[0]=atom1->atom[0];
		  exp->ChShiftAtom[ptr].atom[1]=atom1->atom[1];
		}
	    }
	}
    }
  /* Assign HhShift */
  /* TODO: Add this functionality */
  if(mode!=SE_PURGE_HH)
    {
    }
  
}

int SP_FindExpAssignment(const struct BU_Struct *exp, const unsigned int exp_ptr,
			 struct BU_Struct *sim, const unsigned int mode)
{
  float eCShift=0, eHShift=0;
  int i, found=0;

  switch(mode)
    {
    case SE_PURGE_C:
      eCShift=exp->CShift[exp_ptr];
      break;
    case SE_PURGE_H:
      eHShift=exp->HShift[exp_ptr];
      break;
    case SE_PURGE_CH:
      eCShift=exp->ChCShift[exp_ptr];
      eHShift=exp->ChHShift[exp_ptr];
      break;
    case SE_PURGE_HH:
      eCShift=exp->HhHiShift[exp_ptr];
      eHShift=exp->HhLoShift[exp_ptr];
      break;
      
    default:
      return 0;
    }
  
  if(mode==SE_PURGE_C||mode==SE_PURGE_CH)
    {
      for(i=0;i<sim->nCAssignments;i++)
	{
	  if(fabs(sim->CAssignments[0][i]-eCShift)<0.0001)
	    {
	      found=1;
	      i=sim->nCAssignments;
	    }
	}
      if(found==0)
	{
	  return 0;
	}
    }
  if(mode==SE_PURGE_H||mode==SE_PURGE_HH||mode==SE_PURGE_CH)
    {
      for(i=0;i<sim->nHAssignments;i++)
	{
	  if(fabs(sim->HAssignments[0][i]-eHShift)<0.0001)
	    {
	      found=1;
	      i=sim->nHAssignments;
	    }
	}
    }
  return found;
}

int SP_FindExpAssignedToSim(const struct BU_Struct *exp, int exp_ptr,
			    struct BU_Struct *sim, int sim_ptr, unsigned int mode)
{
  const float *eCShift=0, *eHShift=0, *sCShift=0, *sHShift=0;
  int i,j,k,cnt,found=-1;

  switch(mode)
    {
    case SE_PURGE_C:
      eCShift=exp->CShift;
      sCShift=sim->CShift;
      cnt=exp->CCnt;
      break;
    case SE_PURGE_H:
      eHShift=exp->HShift;
      sHShift=sim->HShift;
      cnt=exp->HCnt;
      break;
    case SE_PURGE_CH:
      eCShift=exp->ChCShift;
      eHShift=exp->ChHShift;
      sCShift=sim->ChCShift;
      sHShift=sim->ChHShift;
      cnt=exp->ChCnt;
      break;
    case SE_PURGE_HH:
      eCShift=exp->HhHiShift;
      eHShift=exp->HhLoShift;
      sCShift=sim->HhHiShift; /* n.b. */
      sHShift=sim->HhLoShift; /* n.b. */
      cnt=exp->HhCnt;
      break;
      
    default:
      return 0;
    }
  if(mode==SE_PURGE_C||mode==SE_PURGE_CH)
    {
      i=QuickFind(sim->CAssignments[1],0,sim->nCAssignments-1,sCShift[sim_ptr]);
      if(i==-1)
	{
	  return i;
	}
      for(;i<sim->nCAssignments && fabs(sim->CAssignments[1][i]-sCShift[sim_ptr])<0.0001;i++)
	{
	  for(j=0;j<cnt;j++)
	    {
	      if(fabs(sim->CAssignments[0][i]-eCShift[j])<0.0001)
		{
		  if(mode==SE_PURGE_CH)
		    {
		      for(k=0;k<sim->nHAssignments;k++)
			{
			  if(fabs(sim->HAssignments[1][k]-sHShift[sim_ptr])<0.0001)
			    {
			      if(fabs(sim->HAssignments[0][k]-eHShift[j])<0.0001)
				{
				  found=j;
				  k=sim->nHAssignments;
				  j=cnt;
				  i=sim->nCAssignments;
				}
			    }
			}
		    }
		  else
		    {
		      found=j;
		      j=cnt;
		      i=sim->nCAssignments;
		    }
		}
	    }
	  i=sim->nCAssignments;
	}
      if(found==-1)
	{
	  return found;
	}
    }
  else
    {
      i=QuickFind(sim->HAssignments[1],0,sim->nHAssignments-1,sHShift[sim_ptr]);
      if(i==-1)
	{
	  return i;
	}
      for(;i<sim->nHAssignments && fabs(sim->HAssignments[1][i]-sHShift[sim_ptr])<0.0001;i++)
	{
	  for(j=0;j<cnt;j++)
	    {
	      if(fabs(sim->HAssignments[0][i]-eHShift[j])<0.0001)
		{
		  if(mode==SE_PURGE_HH)
		    {
		      for(k=0;k<sim->nHAssignments;k++)
			{
			  /* Just to cause confusion CShift is used to
			     point at HhHiShift */
			  if(fabs(sim->HAssignments[1][i]-sCShift[sim_ptr])<0.0001)
			    {
			      if(fabs(sim->HAssignments[0][k]-eCShift[j])<0.0001)
				{
				  found=j;
				  k=sim->nHAssignments;
				  j=cnt;
				  i=sim->nCAssignments;
				}
			    }
			}
		    }
		  else
		    {
		      found=j;
		      j=cnt;
		      i=sim->nCAssignments;
		    }
		} 
	    }
	}
    }
  return (found);
}

int QuickFind(const float *array, int first, int last, const float value)
{
  int pivot;

  if(last<first || array[last]>value)
    {
      return -1;
    }

  /* Bitwise shift one step is faster than division by two. */
  pivot=(first+last+1)>>1;

  while(pivot>first && pivot<last)
    {
      if(fabs(array[pivot]-value)<=0.0001)
	{
	  /* Find the first entry with this value and return it */
	  while(pivot>first && fabs(array[pivot-1]-value)<=0.0001)
	    {
	      pivot--;
	    }
	  return pivot;
	}
      if(array[pivot]<value)
	{
	  last=pivot;
	}
      else
	{
	  first=pivot;
	}
      pivot=(first+last+1)>>1;
    }
  if(fabs(array[pivot]-value)<=0.0001)
    {
      return pivot;
    }
  if(pivot==last && pivot > first)
    {
      pivot--;
      if(fabs(array[pivot]-value)<=0.0001)
	{
	  return pivot;
	}
    }
  return -1;
}

float SP_CombinationDiff(struct BU_Struct *exp, struct BU_Struct *sim, const int exp_ptr, const int sim_ptr)
{
  float diff=0, combDiff, flag=0;

  if(exp->HhCnt>0)
    {
      combDiff=SP_CombinationDiffHh(exp, sim, exp_ptr, sim_ptr);
      if(combDiff>0)
	{
	  diff+=combDiff;
	}
      else
	{
	  flag=1;
	}
    }
  
  return diff;
}

float SP_CombinationDiffHh(struct BU_Struct *exp, struct BU_Struct *sim, const int exp_ptr, const int sim_ptr)
{
  int possibleSimShiftsCnt, possibleExpShiftsCnt, possibleCHShiftsCnt;
  float simHiShift[32], simLoShift[32], expHiShift[32], expLoShift[32], diff=0;

  possibleSimShiftsCnt=SP_GetHhShiftsFromAtom(sim->ChShiftAtom[sim_ptr].unit,
					      sim->ChShiftAtom[sim_ptr].atom[0],
					      sim->ChShiftAtom[sim_ptr].atom[1],
					      simHiShift, simLoShift);

  possibleExpShiftsCnt=SP_FindInvolvedShifts(exp, expHiShift, expLoShift, 0, exp->ChHShift[exp_ptr], 0, SE_PURGE_HH);

  if(possibleExpShiftsCnt==0)
    {
      return -1;
    }

  /* Check how many CH shifts there are that can potentially contribute to the long range correlations. If there
     are more CH shifts than long range shifts then it is not certain that this CH shift is actually the one that
     is involved in the long range correlation - then there will be an upper limit to how high the diff can get. */
  possibleCHShiftsCnt=SP_FindInvolvedShifts(exp, 0, 0, 0, exp->ChCShift[exp_ptr], exp->ChHShift[exp_ptr], SE_PURGE_CH);

  diff=SP_MiniAssignment(expHiShift, expLoShift, 0, possibleExpShiftsCnt, 
			 simHiShift, simLoShift, 0, 0, 0, possibleSimShiftsCnt, SE_PURGE_HH);

  if(possibleCHShiftsCnt > possibleExpShiftsCnt)
    {
      diff=Min(diff, 1);
    }

  return diff;
}

/* This function takes a unit and an atom index number as well as two empty arrays. It finds
   all TOCSY correlations from this atom and puts them in the arrays.
   It sets the number of correlations found (the size of the arrays) in cnt. */
int SP_GetHhShiftsFromAtom(struct BU_Unit *unit, int atom, int HNr, float *HiShift, float *LoShift)
{
  struct TY_Type *type;
  int i, j, cnt=0;

  type=unit->Residue->Shifts.Type;

  /* Only use the ring atoms - no couplings to e.g. OMe and NAc */
  if(type->Atom[atom].Label[0]>='1' && type->Atom[atom].Label[0]<='9')
    {
      /* Loop through remaining hydrogens on this carbon */
      for(i=HNr+1; i<type->Atom[atom].HCnt; i++)
	{
	  HiShift[cnt]=Max(unit->Shifts.H[atom][HNr], unit->Shifts.H[atom][i]);
	  LoShift[cnt++]=Min(unit->Shifts.H[atom][HNr], unit->Shifts.H[atom][i]);
	}
      /* Loop through all protons on all other carbons. */
      for(i=0; i<type->HeavyCnt; i++)
	{
	  /* Only use the ring atoms - no couplings to e.g. OMe and NAc */
	  if(i!= atom && type->Atom[i].Label[0]>='1' && type->Atom[i].Label[0]<='9')
	    {
	      for(j=0; j<type->Atom[i].HCnt; j++)
		{
		  HiShift[cnt]=Max(unit->Shifts.H[atom][HNr], unit->Shifts.H[i][j]);
		  LoShift[cnt++]=Min(unit->Shifts.H[atom][HNr], unit->Shifts.H[i][j]);
		}
	    }
	}
    }
  return cnt;
}

int SP_FindInvolvedShifts(struct BU_Struct *str, float *HiShift, float *LoShift, int *range, float value, float value2, const unsigned int mode)
{
  int cntFound=0, n, ptr, *strRange;
  float *firstDim, *secondDim;

  switch (mode)
    {
    case SE_PURGE_CH:
      firstDim=str->ChCShift;
      secondDim=str->ChHShift;
      n=str->ChCnt;
      break;
    case SE_PURGE_HH:
      firstDim=str->HhHiShift;
      secondDim=str->HhLoShift;
      n=str->HhCnt;
      break;
      
    }

  /* First find the pairs of chemical shifts where value is in the first dimension - the easiest part first */
  ptr=QuickFind(firstDim, 0, n-1, value);
  if(ptr==-1)
    {
      return 0;
    }
  while(ptr<n && fabs(firstDim[ptr]-value)<0.00001)
    {
      if(HiShift != 0 && LoShift != 0)
	{
	  HiShift[cntFound]=firstDim[ptr];
	  LoShift[cntFound]=secondDim[ptr];
	  if(range && strRange)
	    {
	      range[cntFound]=strRange[ptr];
	    }
	}
      cntFound++;
      ptr++;
    }
  
  /* If a second value is submitted it is the H chemical shift from HSQC. Use that one for finding the
     hydrogen shift. */
  if(value2>0.01)
    {
      value=value2;
    }

  /* Then find the pairs of chemical shifts where value is in the second dimension - slower since it needs to
     go through the whole list */
  for(ptr=0; ptr<n; ptr++)
    {
      if(fabs(secondDim[ptr]-value)<0.00001)
	{
	  if(HiShift != 0 && LoShift != 0)
	    {
	      HiShift[cntFound]=firstDim[ptr];
	      LoShift[cntFound]=secondDim[ptr];
	      if(range && strRange)
		{
		  range[cntFound]=strRange[ptr];
		}
	    }
	  cntFound++;
	}
    }
  return cntFound;
}

/* Non-recursive function for matching very few shifts. It is used for calculating the combinated differences
   of Hh  to assist in assigning Ch. */
float SP_MiniAssignment(const float *expFirstDim, const float *expSecondDim, const int *expRange, int eCnt, 
			const float *simFirstDim, const float *simSecondDim, 
			struct BU_ShiftToAtom *CAtom, struct BU_ShiftToAtom *HAtom, 
			const int *simRange, int sCnt,
			const unsigned int mode)
{
  int i, exp_ptr, sim_ptr, temp_ptr,  *usedexp, *usedsim;
  float diff=0, minDiff, abs_err=0, dx, dy, scaling;

  usedexp=alloca(eCnt*sizeof(int));
  usedsim=alloca(sCnt*sizeof(int));

  for(i=eCnt; i--;)
    {
      usedexp[i]=-1;
    }
  for(i=sCnt; i--;)
    {
      usedsim[i]=-1;
    }
  
      scaling=1;
      

  /* As long as the next simulated shift is greater than the
     current experimental shift look further ahead in the simulated
     shifts (they are sorted so that they get lower).
     Since some simulated shifts are skipped to find the best match
     those shifts are counted and later there is an attempt to match
     them with the neighbouring experimental shifts. I.e. with the
     previous experimental shift or with the current one (even if the
     skipped shifts were not the best matches they might still be
     within cutoff range.) */
  for(exp_ptr=0; exp_ptr<eCnt; exp_ptr++)
    {
      sim_ptr=0;

      minDiff=9999;
      temp_ptr=sim_ptr;
	      
      while (sim_ptr<sCnt)
	{
	  if(usedsim[sim_ptr]==-1)
	    {
	      
		  dx=expFirstDim[exp_ptr]-simFirstDim[sim_ptr];
		  dy=expSecondDim[exp_ptr]-simSecondDim[sim_ptr];

		  diff=sqrt(scaling*dx*dx+dy*dy);

		  
		  if(diff<minDiff)
		    {
		      minDiff=diff;
		      temp_ptr=sim_ptr;
		    }
		  
	    }
	  sim_ptr++;
	}
      if(minDiff<9998)
	{
	  sim_ptr=temp_ptr;
	  abs_err+=minDiff;
	  
	  usedsim[sim_ptr]=exp_ptr;
	  usedexp[exp_ptr]=sim_ptr;
	}
    }

  return(abs_err);
}

/* Plots one or two spectra using gnuplot */
/* SYNTAX: gnuplot <type> <structure 1> <structure 2> [<output filename>] */
int SP_Gnuplot()
{
  struct BU_Struct *exp, *sim;
  gnuplot_ctrl *plot;
  int stat;
  unsigned int mode;
  char outputfile[128];
  char plotstr[192];

  outputfile[0]=0;

  PA_GetString;
  mode=(toupper(PA_Result.String[0])<<8)+toupper(PA_Result.String[1]);
  PA_GetString;
  sim=(struct BU_Struct *)FindNode((struct Node *)&StructList, PA_Result.String);
  if(sim==NULL)
    {
      Error(PA_ERR_FAIL,"No such structure");
    }
  if(PA_GetArg()==PA_STRING)
    {
      if(PA_Result.String[0]=='/')
	{
	  if(projectValid)
	    {
	      if(!CC_GetNmrProject(&Global_CcpnProject,&Global_NmrProject))
		{
		  Error(PA_ERR_FATAL,"Cannot open project.");
		}
	      exp=(struct BU_Struct *)
		FindNode((struct Node *)&StructList,
			 ApiString_Get(Nmr_NmrProject_GetName(Global_NmrProject)));
	    }
	  else
	    {
	      for(exp=(struct BU_Struct *)StructList.Head.Succ; exp->Node.Succ!=NULL && 
		    strncasecmp(sim->Node.Name, exp->Node.Name, strlen(sim->Node.Name))==0;
		  exp=(struct BU_Struct *)exp->Node.Succ)
		{
		}
	      if(exp->Node.Succ==NULL)
		{
		  exp=0;
		}
	    }
	}
      else
	{
	  exp=(struct BU_Struct *)FindNode((struct Node *)&StructList, PA_Result.String);
	}

      /* Check that the chosen experiment type exists in exp. Otherwise set
	 exp to 0 so that only the simulated spectrum will be plotted */
      if(exp)
	{
	  switch(mode)
	    {
	    case SP_ID_CARBON:
	      if(exp->CCnt<=0)
		{
		  exp=0;
		}
	      break;
	    case SP_ID_PROTON:
	      if(exp->HCnt<=0)
		{
		  exp=0;
		}
	      break;
	    case SP_ID_2D_CH:
	      if(exp->ChCnt<=0)
		{
		  exp=0;
		}
	      break;
	    case SP_ID_2D_HH:
	      if(exp->HhCnt<=0)
		{
		  exp=0;
		}
	      break;
	      
	    }
	}

      if(PA_GetArg()==PA_STRING)
	{
	  strcpy(outputfile,PA_Result.String);
	}
    }
  else
    {
      exp=0;
    }
      
  
  plot=gnuplot_init();
  if(strlen(outputfile)>0)
    {
      if(strlen(outputfile)>4 && strcasecmp(outputfile+strlen(outputfile)-4,".eps")==0)
	{
	  gnuplot_cmd(plot, "set terminal post eps color enh");
	}
      else
	{
	  gnuplot_cmd(plot, "set terminal png");
	}
      sprintf(plotstr,"set output \"%s\"",outputfile);
      gnuplot_cmd(plot, plotstr);
    }

  switch(mode)
    {
    case SP_ID_CARBON:
    case SP_ID_PROTON:
      stat=SP_Gnuplot1D(plot, sim, exp, mode);
      break;
    case SP_ID_2D_CH:
    case SP_ID_2D_HH:
      
      stat=SP_Gnuplot2D(plot, sim, exp, mode);
      break;
    default:
      Error(PA_ERR_FAIL, "Illegal type");
    }

  if(strlen(outputfile)==0)
    {
      printf("Press <enter> to close the plot window and continue.\n");
      getchar();
    }
  
  gnuplot_close(plot);

  return (stat);
}

int SP_Gnuplot1D(gnuplot_ctrl *plot, struct BU_Struct *sim, struct BU_Struct *exp, unsigned int mode)
{
  float *eXShift, *sXShift, xMin, xMax, yMin, yMax;
  double *deXShift=0, *deYShift=0, *dsXShift=0, *dsYShift=0;
  float xTics;
  int sCnt, eCnt, i;
  char name[32],command[128];

  switch(mode)
    {
    case SP_ID_CARBON:
      sCnt=sim->CCnt;
      sXShift=sim->CShift;
      if(exp)
	{
	  eCnt=exp->CCnt;
	  eXShift=exp->CShift;
	}
      strcpy(name,"13C");
      xTics=10;
      break;
    case SP_ID_PROTON:
      sCnt=sim->HCnt;
      sXShift=sim->HShift;
      if(exp)
	{
	  eCnt=exp->HCnt;
	  eXShift=exp->HShift;	  
	}
      strcpy(name,"1H");
      xTics=0.5;
      break;

    }
  xMin=sXShift[sCnt-1];
  xMax=sXShift[0];
  yMin=0;
  yMax=1.1;
  if(exp)
    {
      xMin=Min(eXShift[eCnt-1],sXShift[sCnt-1]);
      xMax=Max(eXShift[0],sXShift[0]);
    }

  xMin=xMin-xTics;
  xMax=xMax+xTics;

  dsXShift=alloca(sCnt*sizeof(double));
  dsYShift=alloca(sCnt*sizeof(double));
  for(i=sCnt;i--;)
    {
      dsXShift[i]=sXShift[i];
      dsYShift[i]=1;
    }
  if(exp)
    {
      deXShift=alloca(eCnt*sizeof(double));
      deYShift=alloca(eCnt*sizeof(double));
      for(i=eCnt;i--;)
	{
	  deXShift[i]=eXShift[i];
	  deYShift[i]=1;
	}
    }

  if(exp)
    {
      gnuplot_cmd(plot, "set size 1,1");
      gnuplot_cmd(plot, "set origin 0, 0");
      gnuplot_cmd(plot, "set multiplot");
      gnuplot_cmd(plot, "set size 1,0.5");
      gnuplot_cmd(plot, "set origin 0, 0");
      gnuplot_cmd(plot, "set tmargin 0");
    }

  sprintf(command, "set xtics %f out nomirror",xTics);
  gnuplot_cmd(plot, command);
  gnuplot_setstyle(plot,"impulses");
  gnuplot_cmd(plot, "set key inside");
  sprintf(command, "set y2range [%f:%f]", yMin, yMax);
  gnuplot_cmd(plot, command);
  sprintf(command, "set xrange [%f:%f] reverse", xMin, xMax);
  gnuplot_cmd(plot, command);
  gnuplot_cmd(plot, "unset ytics");

  switch(mode)
    {
    case SP_ID_PROTON:
      gnuplot_set_xlabel(plot, "1H chemical shift/ppm");
      break;
    case SP_ID_CARBON:
      gnuplot_set_xlabel(plot, "13C chemical shift/ppm");
      break;
    }
  gnuplot_plot_xy(plot, dsXShift, dsYShift, sCnt, sim->Node.Name);

  if(exp)
    {
      gnuplot_cmd(plot, "set size 1, 0.5");
      gnuplot_cmd(plot, "set origin 0, 0.5");
      gnuplot_cmd(plot, "set xtics out nomirror");
      gnuplot_cmd(plot, "set format \"\"");
      gnuplot_cmd(plot, "unset xlabel");
      gnuplot_cmd(plot, "set bmargin 0");
      gnuplot_cmd(plot, "set tmargin 1");
      gnuplot_cmd(plot, "set key inside");
      plot->nplots=0;
      gnuplot_plot_xy_2(plot, deXShift, deYShift, eCnt, "exp"/*exp->Node.Name*/);	  
      gnuplot_cmd(plot, "unset multiplot");	  
    }      


  /*  if(dsXShift)
    {
      free(dsXShift);
    }
  if(dsYShift)
    {
      free(dsYShift);
    }
  if(deXShift)
    {
      free(deXShift);
    }
  if(deYShift)
    {
      free(deYShift);
      }*/
  return (PA_ERR_OK);
}
  
int SP_Gnuplot2D(gnuplot_ctrl *plot, struct BU_Struct *sim, struct BU_Struct *exp, unsigned int mode)
{
  float *eXShift, *eYShift, *sXShift, *sYShift, xMin, xMax, yMin, yMax;
  double *deXShift=0, *deYShift=0, *dsXShift=0, *dsYShift=0;
  float yTics;
  int sCnt, eCnt, i;
  char name[32],command[128];

  switch(mode)
    {
    case SP_ID_2D_CH:
      sCnt=sim->ChCnt;
      sXShift=sim->ChHShift;
      sYShift=sim->ChCShift;
      if(exp)
	{
	  eCnt=exp->ChCnt;
	  eXShift=exp->ChHShift;
	  eYShift=exp->ChCShift;
	}
      strcpy(name,"HSQC/HETCOR 13C-1H");
      yTics=10;
      break;
    case SP_ID_2D_HH:
      sCnt=sim->HhCnt;
      sXShift=sim->HhHiShift;
      sYShift=sim->HhLoShift;
      if(exp)
	{
	  eCnt=exp->HhCnt;
	  eXShift=exp->HhHiShift;
	  eYShift=exp->HhLoShift;
	}
      strcpy(name,"TOCSY 1H-1H");
      yTics=0.5;
      break;
      
    default:
      Error(PA_ERR_FAIL, "Illegal type");
    }

  xMin=sXShift[sCnt-1]-0.5;
  xMax=sXShift[0]+1.1;
  yMin=sYShift[sCnt-1];
  yMax=sYShift[0];
  if(exp)
    {
      xMin=Min(eXShift[eCnt-1],sXShift[sCnt-1])-0.5;
      xMax=Max(eXShift[0],sXShift[0])+1.1;
      yMin=Min(eYShift[eCnt-1],sYShift[sCnt-1]);
      yMax=Max(eYShift[0],sYShift[0]);
    }

  yMin=yMin-yTics;
  yMax=yMax+yTics;

  dsXShift=alloca(sCnt*sizeof(double));
  dsYShift=alloca(sCnt*sizeof(double));
  for(i=sCnt;i--;)
    {
      dsXShift[i]=sXShift[i];
      dsYShift[i]=sYShift[i];
    }
  if(exp)
    {
      deXShift=alloca(eCnt*sizeof(double));
      deYShift=alloca(eCnt*sizeof(double));
      for(i=eCnt;i--;)
	{
	  deXShift[i]=eXShift[i];
	  deYShift[i]=eYShift[i];
	}
    }

  if(exp)
    {
      gnuplot_cmd(plot, "set size 1,1");
      gnuplot_cmd(plot, "set origin 0, 0");
      gnuplot_cmd(plot, "set multiplot");
      gnuplot_cmd(plot, "set size 1,1");
      gnuplot_cmd(plot, "set origin 0, 0");
    }

  sprintf(command, "set xtics 0.5 out nomirror");
  gnuplot_cmd(plot, command);
  gnuplot_set_xlabel(plot, "1H chemical shift /ppm");
  gnuplot_cmd(plot, "set y2tics %f out",yTics);
  gnuplot_cmd(plot, "set key outside");
  sprintf(command, "set y2range [%f:%f] reverse", yMin, yMax);
  gnuplot_cmd(plot, command);
  sprintf(command, "set xrange [%f:%f] reverse", xMin, xMax);
  gnuplot_cmd(plot, command);
  gnuplot_cmd(plot, "unset ytics");

  switch(mode)
    {
    case SP_ID_2D_HH:
      gnuplot_set_ylabel(plot, "1H chemical shift /ppm");
    case SP_ID_2D_CH:
      
      gnuplot_set_ylabel(plot, "13C chemical shift /ppm");
      break;
    }
  gnuplot_plot_xy(plot, dsXShift, dsYShift, sCnt, sim->Node.Name);
  if(exp)
    {
      /*      gnuplot_cmd(plot, "unset xtics");
      gnuplot_cmd(plot, "unset ytics");
      sprintf(command, "set y2range [%f:%f] reverse", yMin, yMax);
      gnuplot_cmd(plot, command);
      sprintf(command, "set xrange [%f:%f] reverse", xMin, xMax);
      gnuplot_cmd(plot, command);*/
      gnuplot_plot_xy_2(plot, deXShift, deYShift, eCnt, "exp"/*exp->Node.Name*/);	  
      gnuplot_cmd(plot, "unset multiplot");	  
    }      

  /*  if(dsXShift)
    {
      free(dsXShift);
    }
  if(dsYShift)
    {
      free(dsYShift);
    }
  if(deXShift)
    {
      free(deXShift);
    }
  if(deYShift)
    {
      free(deYShift);
      }*/
  return(PA_ERR_OK);
}
