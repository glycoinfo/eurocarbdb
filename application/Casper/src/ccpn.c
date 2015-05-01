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

/* Functions for handling CCPN projects in CASPER. */



#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <strings.h>
#include <ctype.h>
#include <sys/stat.h>
#include "ccp.h"
#include "memops/general/Io.h"
#include "memops/general/Util.h"
#include "memops/universal/Io.h"
#include "memops/api/Implementation/Impl_AppDataString.h"
#include "ccp/util/Molecule.h"

#include "ccpn.h"
#include "build.h"
#include "calc.h"
#include "parser.h"
#include "node.h"
#include "spectra.h"
#include "residue.h"
#include "delta.h"
#include "setup.h"


/* This function loads the CCPN xml project file specified by *projectFileName into memory at
   the address specified by ccpnProj.
   The function returns 1 if successful and 0 if the project could not be opened. */
int CC_LoadProject(Impl_MemopsRoot *ccpnProj, char *projectFileName)
{

  *ccpnProj = loadProject(projectFileName, NULL);

  if (!*ccpnProj)
    {
      projectValid=0;
      return 0;
    }

  projectValid=1;

  return 1;
}

/* This function saves the ccpn project to the specified projectFileName.
   The necessary directories are created and all files used by the
   project are saved. */
int CC_SaveProject(Impl_MemopsRoot *ccpnProj, char *projectFileName)
{
  ApiString path=ApiString_New(projectFileName);
  ApiObject args[] = { *ccpnProj, path, NULL };

  if(!projectValid)
    {
      return(PA_ERR_FAIL);
    }
  callModuleFunction("memops.general.Io", "saveProject", args);

  ApiObject_Free(path);


  return(PA_ERR_OK);
}

/* This function loads the current or first Nmr project of the CCPN project in memory.
   *nmrProj is a pointer to the Nmr project address.
   The function returns 1 is successful and 0 if the Nmr project could not be opened. */
int CC_GetNmrProject(Impl_MemopsRoot *ccpnProj, Nmr_NmrProject *nmrProj)
{
  ApiSet projects=0;
  ApiString name;
  int numProjs;

  if(!*nmrProj||ApiObject_IsNone(*nmrProj))
    {
      *nmrProj = Impl_MemopsRoot_GetCurrentNmrProject(*ccpnProj);
    }
  if(!*nmrProj||ApiObject_IsNone(*nmrProj))
    {
      projects = Impl_MemopsRoot_GetNmrProjects(*ccpnProj);

      if(projects)
	{
	  numProjs=ApiSet_Len(projects);
	}
      else
	{
	  numProjs=0;
	}

      /* If there are nmrProjects use the first one.
	 TODO: Should there be a way to select which nmrProject to use? */
      if(projects && numProjs>0)
	{
	  *nmrProj=ApiSet_Get(projects,0);
	}
      /* If there were no nmrProjects generate a new one */
      else
	{
	  *nmrProj = Nmr_NmrProject_Init_reqd(*ccpnProj,"exp");
 
	  if(nmrProj)
	    {
	      name=Impl_MemopsRoot_GetName(*ccpnProj);
	      if(!Anal_AnalysisProject_Init_reqd(*ccpnProj,
						 ApiString_Get(name),
						 *nmrProj))
		{
		  printf("Could not create analysisProject. Line %d.\n", __LINE__);
		  printRaisedException();
		  projectValid=0;
		  ApiObject_Free(*nmrProj);
		  if(projects && !ApiObject_IsNone(projects))
		    {
		      ApiObject_Free(projects);
		    }
		  ApiObject_Free(name);
		  return 0;
		}
	      ApiObject_Free(name);
	    }
 	}
      if(projects && !ApiObject_IsNone(projects))
	{
	  ApiObject_Free(projects);
	}
    }

  if(!*nmrProj||ApiObject_IsNone(*nmrProj))
    {
      projectValid=0;
      return 0;
    }

  projectValid=1;
  return 1;
}

/* This function points *nmrProj at the Nmr project of a specific name - specified by
   the argument *wanted_name. If the project did not exist it is created.
   The function returns 1 is successful and 0 if the Nmr project could not be found. */
int CC_GetNmrProject_Named(Impl_MemopsRoot *ccpnProj, Nmr_NmrProject *nmrProj, char *wanted_name)
{
  ApiString name;
  ApiMap parameters;
  Anal_AnalysisProject analysisProj;

  parameters=ApiMap_New();
  ApiMap_SetString(parameters, "name", wanted_name);

  *nmrProj=Impl_MemopsRoot_FindFirstNmrProject(*ccpnProj,parameters);
  ApiObject_Free(parameters);

  if(*nmrProj && !ApiObject_IsNone(*nmrProj))
    {
      projectValid=1;
      return 1;
    }

  /* If the project wasn't found, create it. */
  *nmrProj = Nmr_NmrProject_Init_reqd(*ccpnProj,wanted_name);
  if(!*nmrProj || ApiObject_IsNone(*nmrProj))
    {
      projectValid=0;
      return 0;
    }

  name=Impl_MemopsRoot_GetName(*ccpnProj);

  analysisProj=Impl_MemopsRoot_FindFirstAnalysisProject_keyval1(*ccpnProj, "name", name);
  if(!analysisProj || ApiObject_IsNone(analysisProj))
    {
      analysisProj=Anal_AnalysisProject_Init_reqd(*ccpnProj,
						  ApiString_Get(name),
						  *nmrProj);
	if(!analysisProj || ApiObject_IsNone(analysisProj))
	  {
	    printRaisedException();
	    printf("Could not create analysisProject. Line %d.\n", __LINE__);
	    projectValid=0;
	    ApiObject_Free(name);
	    return 0;
	  }
    }
  ApiObject_Free(analysisProj);
  ApiObject_Free(name);
  projectValid=1;
  return 1;
}

/* This function finds experiments corresponding to a specific type. *nmrProj is the Nmr
   project that contains the experiments. *type decides what kind of experiments to look for:
   C - 1D 13C
   H - 1D 1H
   H[C] - HSQC
   H_H.TOCSY - TOCSY

   ...
   The functions returns a collection of experiments (ApiSet) that matches the criteria.
   If no matching experiments are found the function returns 0. */
ApiSet CC_GetExpOfType(Nmr_NmrProject *nmrProj, char *type)
{
  int j, numExps;
  Nmr_Experiment experiment;
  Nmrx_RefExperiment refexp;
  ApiString name;
  ApiSet exps, matchingexps;

  matchingexps = ApiSet_New();

  /* Get all experiments from the project */
  exps = Nmr_NmrProject_GetExperiments(*nmrProj);
  if(exps)
    {
      numExps = ApiSet_Len(exps);
    }
  else
    {
      printRaisedException();
      return (matchingexps);
    }


  /* Compare the name of the reference experiments to the specified name */
  for (j=0;j<numExps;j++)
    {
      experiment=ApiSet_Get(exps,j);
      refexp=Nmr_Experiment_GetRefExperiment(experiment);

      name=Nmrx_NmrExpPrototype_GetName(refexp);
      if(name && strcmp(ApiString_Get(name),type)==0)
	{
	  ApiSet_Add(matchingexps,experiment);
	}
      ApiObject_Free(name);
      ApiObject_Free(experiment);
      ApiObject_Free(refexp);
    }
  /*  ApiObject_Free(exps);*/
  return (matchingexps);
}

/* This function retrieves the first processed spectrum that has the number of dimensions
   specified by dim. *experiment is the Nmr experiment in which to search and *spectrum is
   the address to the matching spectrum. */
int CC_GetProcSpectrumOfDim(Nmr_Experiment *experiment, int dim, Nmr_DataSource *spectrum)
{
  ApiMap parameters;

  parameters=ApiMap_New();
  ApiMap_SetString(parameters,"dataType", "processed");
  ApiMap_SetInt(parameters,"numDim", dim);

  *spectrum=Nmr_Experiment_FindFirstDataSource(*experiment, parameters);
  ApiObject_Free(parameters);

  if(!*spectrum || ApiObject_IsNone(*spectrum))
    {
      /*      printf("Spectrum of that dimension (%d) not found! Line %d.\n", dim, __LINE__);*/
      return 0;
    }
  return 1;
}

int CC_GetFirstPeakList(Nmr_DataSource *spectrum, Nmr_PeakList *peakList)
{
  ApiSet peakLists;
  
  peakLists = Nmr_DataSource_GetPeakLists(*spectrum);

  if(!peakLists || ApiSet_Len(peakLists)<=0)
    {
      printf("peakLists not found! Line %d.\n", __LINE__);
      return 0;
    }
  *peakList = ApiSet_Get(peakLists,0);

  if(!*peakList || ApiObject_IsNone(*peakList))
    {
      printf("No peaklist found. Line %d.\n", __LINE__);
      ApiObject_Free(peakLists);
      return 0;
    }
  ApiObject_Free(peakLists);
  return 1;
}

/* This function gets the peak positions from *experiment and populates the firstDim[] and
   secondDim[] (if there are two dimensions in the experiment) arrays. It is used for getting
   the information from an experiment in a CCPN project and put it in the CASPER struct
   containing e.g. CShift, HShift, CHShift ... It is called once per experiment.
   The function returns the number of peaks in the experiment if successful or 0 if something
   went wrong. */
int CC_GetPeaks(Nmr_Experiment *experiment, float firstDim[], float secondDim[])
{
  ApiSet peaks, peakDims, peakDimContribs, /*atomSets, atoms,*/ resonances, shifts;
  ApiSetIterator peakIterator, peakDimContribIterator;
  ApiInteger numDim;
  ApiFloat value;
  Nmr_DataSource spectrum;
  Nmr_PeakList peakList;
  Nmr_Peak peak;
  Nmr_PeakDim peakDim;
  Nmr_PeakDimContrib peakDimContrib;
  Nmr_Resonance resonance, tempResonance;
  /*  Nmr_ResonanceSet resonanceSet;*/
  Nmr_Shift shift;
  /*  Nmr_AtomSet atomSet;
      Mols_Atom atom;*/
  int j, l, cnt=0, numDims, numPeaks=0, /*numAtoms,*/ numPeakDimContribs,
    numMultiResonances, skipflag;
  float *dim;

  resonances=ApiSet_New();

  numMultiResonances=0;

  /* Only processed spectra are of interest and there is a check to see that the numbers
     of dimensions are the same as in the experiment. */
  numDim=Nmr_Experiment_GetNumDim(*experiment);
  if (CC_GetProcSpectrumOfDim(experiment, ApiInteger_Get(numDim), &spectrum))
    {
      CC_GetFirstPeakList(&spectrum, &peakList);
      peaks = Nmr_PeakList_GetPeaks(peakList);
      
      if(peaks)
	{
	  numPeaks=ApiSet_Len(peaks);
	}
    }
  
  ApiObject_Free(numDim);
  if(numPeaks<=0)
    {
      return 0;
    }
  peakIterator=ApiSet_Iterator(peaks);
  while((peak=ApiSetIterator_Next(peakIterator)))
    {
      skipflag=0;
      /*      peak=ApiSet_Get(peaks,i);*/
      
      peakDims=Nmr_Peak_GetPeakDims(peak);
      if(peakDims)
	{
	  numDims=ApiSet_Len(peakDims);
	  for (j=0;j<numDims;j++)
	    {
	      /* Populate the float arrays that were provided in the function call.
		 To avoid writing in unallocated memory check that the array was
		 provided before writing.
		 Only two dimensions are handled. */
	      if(firstDim && j==0) 
		{
		  dim=firstDim;
		}
	      else if (secondDim && j==1)
		{
		  dim=secondDim;
		}
	      else
		{
		  continue;
		}
	      peakDim=ApiSet_Get(peakDims,j);

	      peakDimContribs=Nmr_PeakDim_GetPeakDimContribs(peakDim);

	      if(peakDimContribs)
		{
		  numPeakDimContribs=ApiSet_Len(peakDimContribs);
		}
	      else
		{
		  numPeakDimContribs=0;
		}

	      peakDimContribIterator=ApiSet_Iterator(peakDimContribs);
	      while((peakDimContrib=ApiSetIterator_Next(peakDimContribIterator)))
		{
		  resonance=Nmr_PeakDimContrib_GetResonance(peakDimContrib);
		  if(resonance)
		    {
		      /* Check if this resonance has already been added. If that is the case
			 skip to the next resonance. */
		      for(l=0;l<ApiSet_Len(resonances);l++)
			{
			  tempResonance=ApiSet_Get(resonances,l);

			  if(tempResonance==resonance)	/* Resonance already added */
			    {
			      if(skipflag==1 || numDims==1)
				{
				  if(skipflag==1)
				    {
				      cnt--;
				    }
				  l=ApiSet_Len(resonances)+9;
				}
			      else
				{
				  skipflag=1;
				  l=ApiSet_Len(resonances);
				}
			    }
			  ApiObject_Free(tempResonance);
			}
		      if(l==ApiSet_Len(resonances)+10)
			{
			  continue;
			}
		      ApiSet_Add(resonances,resonance);
		      /* If there is more than one resonance for this peak there has to be
			 a counter to keep track of how many resonances there are in total */
		      /*		      if(k>0)
			{
			  printf("Multiple peakDimContribs.\n");
			  numMultiResonances++;
			  }*/

		      shifts=Nmr_Resonance_GetShifts(resonance);

		      /* Use only the first shift list and set the shift value */
		      if(shifts&&ApiSet_Len(shifts)>0)
			{
			  if(ApiSet_Len(shifts)>0)
			    {
			      shift=ApiSet_Get(shifts,0);
			      value=Nmr_Shift_GetValue(shift);
			      dim[cnt++/numDims]=ApiFloat_Get(value);
			      /*			      Impl_DataObject_Delete(shift);*/
			      ApiObject_Free(shift);
			      ApiObject_Free(value);
			    }
			}
		      else
			{
			  value=Nmr_PeakDim_GetValue(peakDim);
			  dim[cnt++/numDims]=ApiFloat_Get(value);
			  ApiObject_Free(value);
			}
		      if(shifts && !ApiObject_IsNone(shifts))
			{
			  ApiObject_Free(shifts);
			}
		      

		      /*		      resonanceSet=Nmr_Resonance_GetResonanceSet(resonance);
		      if(resonanceSet)
			{
			  atomSets=Nmr_ResonanceSet_GetAtomSets(resonanceSet);
			  if(atomSets && ApiSet_Len(atomSets)==1)
			    {
			      atomSet=ApiSet_Get(atomSets,0);
			      atoms=Nmr_AtomSet_GetAtoms(atomSet);
			      if(atoms && !ApiObject_IsNone(atoms))
				{
				  numAtoms=ApiSet_Len(atoms);
				}
			      else
				{
				  numAtoms=0;
				}
			      for(l=0;l<numAtoms;l++)
				{
			  *//* TODO: Here the shifts in the structure must be marked as
			       assigned to these specific atoms *//*
				}
			      if(atoms)
				{
				  ApiObject_Free(atoms);
				}
			    }
			  else
			    {
			      printf("Did not find atomSets.\n");
			    }
			  if(atomSets)
			    {
			      ApiObject_Free(atomSets);
			      }
			  ApiObject_Free(resonanceSet);
			}
								  */
		      /*		      Impl_DataObject_Delete(resonance);*/
		      ApiObject_Free(resonance);
		    }
		  ApiObject_Free(peakDimContrib);
		}

	      /* If there was no resonance assigned to the peak use the peakDim value instead */
	      if(numPeakDimContribs==0)
		{
		  value=Nmr_PeakDim_GetValue(peakDim);
		  dim[cnt++/numDims]=ApiFloat_Get(value);
		  ApiObject_Free(value);
		}
	      ApiObject_Free(peakDim);
	      if(peakDimContribs && !ApiObject_IsNone(peakDimContribs))
		{
		  ApiObject_Free(peakDimContribs);
		}
	    }
	  ApiObject_Free(peakDims);
	}
      ApiObject_Free(peak);
    }
  /*  ApiObject_Free(peakIterator);*/
  
  ApiObject_Free(resonances);
  ApiObject_Free(peaks);
  ApiObject_Free(spectrum);

  return cnt/numDims;
}

/* This function gets the shift (only the first shift) of each resonance of a certain isotopeCode
   in *nmrProj. The chemical shifts are put in the float array *values.
   The function returns the number of chemical shifts that have been retrieved. */
int CC_GetShifts(Nmr_NmrProject *nmrProj, char *isotope, float *values)
{
  ApiSet resonances, shiftsInResonance;
  ApiMap parameters;
  ApiFloat apiValue;
  Nmr_Resonance resonance;
  Nmr_Shift shift;
  int cnt=0, numResonances, i;

  parameters=ApiMap_New();
  ApiMap_SetString(parameters, "isotopeCode", isotope);

  resonances=Nmr_NmrProject_FindAllResonances(*nmrProj, parameters);
  if(resonances)
    {
      numResonances=ApiSet_Len(resonances);
      for(i=0; i<numResonances; i++)
	{
	  resonance=ApiSet_Get(resonances, i);
	  if(Impl_MemopsObject_GetIsDeleted(resonance)==ApiBoolean_True())
	    {
	      continue;
	    }
	  shiftsInResonance=Nmr_Resonance_GetShifts(resonance);
	  /* Only the first shift in each resonance is used. This could potentially cause problems,
	     but adding multiple shifts per resonance could be even worse.
	     TODO: consider if this needs to be changed (average shifts?) */
	  if(shiftsInResonance && ApiSet_Len(shiftsInResonance)>0)
	    {
	      shift=ApiSet_Get(shiftsInResonance, 0);
	      apiValue=Nmr_Shift_GetValue(shift);
	      values[cnt++]=ApiFloat_Get(apiValue);
	      /*	      Impl_DataObject_Delete(shift);*/
	      ApiObject_Free(apiValue);
	      ApiObject_Free(shift);
	    }
	  ApiObject_Free(shiftsInResonance);
	  /*	  Impl_DataObject_Delete(resonance);*/
	  ApiObject_Free(resonance);
	}
      ApiObject_Free(resonances);
    }
  return cnt;
}

/* This function adds peaks in the spectrum with positions corresponding to the input arrays (one or two arrays provided).
   The position is set by setting the correct values in the peak dimensions (peakDim).
   If the function is successful it returns the number of peaks that have been added (with respective positions). If it
   fails it returns 0. */
int CC_PutPeaks(Nmr_NmrProject *nmrProj, Nmr_DataSource *spectrum, float firstDim[], float secondDim[],
		int n, int type, int addnew, struct BU_Struct *BU_Struct)
{
  ApiInteger num, apiIntFirstDim, apiIntSecondDim;
  Nmr_PeakList peakList;
  Nmr_Peak peak;
  Nmr_PeakDim peakDim;
  Nmr_DataDimRef dataDimRef;
  Nmr_FreqDataDim dataDim;
  Nmr_PeakContrib peakContrib=0;
  Nmr_Experiment experiment;
  ApiMap parameters;
  ApiMap resonanceParameters[2];
  ApiMap peakContribParameters, apiShiftListClass;
  int i, cnt, numDim, numShiftLists;
  ApiSet peakLists, peakDims, dataDimRefs, peakContribs, shiftLists;
  Nmr_ShiftList shiftList;
  char isotope[2][8];
  int *firstDimMatch=0, *secondDimMatch=0;
  float firstDimCorrection, secondDimCorrection;

  num=Nmr_DataSource_GetNumDim(*spectrum);
  numDim=ApiInteger_Get(num);
  ApiObject_Free(num);

  /*  printf("DataSource numDims: %d\n",numDim);*/

  peakList=Nmr_DataSource_GetActivePeakList(*spectrum);
  if(!peakList||ApiObject_IsNone(peakList))
    {
      peakLists=Nmr_DataSource_GetPeakLists(*spectrum);
      if(ApiSet_Len(peakLists)>0)
	{
	  peakList=ApiSet_Get(peakLists,0);
	}
    }
  if(!peakList||ApiObject_IsNone(peakList))
    {
      parameters=ApiMap_New();
      ApiMap_SetString(parameters, "details", "CASPER");
      ApiMap_SetBoolean(parameters, "isSimulated", 0);
      peakList=Nmr_DataSource_NewPeakList(*spectrum, parameters);
      ApiObject_Free(parameters);
    }
  if(!peakList||ApiObject_IsNone(peakList))
    {
      return 0;
    }
  resonanceParameters[0]=0;
  resonanceParameters[1]=0;

  apiShiftListClass = ApiMap_New();
  ApiMap_SetString(apiShiftListClass,"className","ShiftList");
  shiftLists=Nmr_NmrProject_FindAllMeasurementLists(*nmrProj,apiShiftListClass);
  ApiObject_Free(apiShiftListClass);
  experiment=Nmr_DataSource_GetExperiment(*spectrum);
  if(shiftLists)
    {
      numShiftLists=ApiSet_Len(shiftLists);
    }
  else
    {
      numShiftLists=0;
    }
  if(numShiftLists<=0)
    {
      shiftList=Nmr_NmrProject_NewShiftList_reqd(*nmrProj);
    }
  else
    {
      shiftList=Nmr_Experiment_GetShiftList(experiment);
      if(!shiftList || ApiObject_IsNone(shiftList))
	{
	  shiftList = ApiSet_Get(shiftLists,0);
	}
    }
  ApiObject_Free(shiftLists);
  if(!shiftList || ApiObject_IsNone(shiftList))
    {
      projectValid=0;
      ApiObject_Free(experiment);
      return 0;
    }
  Nmr_Experiment_SetShiftList(experiment,shiftList);
  ApiObject_Free(experiment);

  /* The array acting as a map of matches must be at least as long as the
     longest of the two lists that are matched. For safety reasons it is
     made as long as the longest list (C, H, HSQC or HH) of shifts */
  cnt=n;
  
  if(BU_Struct->CCnt>cnt)
    cnt=BU_Struct->CCnt;
  if(BU_Struct->HCnt>cnt)
    cnt=BU_Struct->HCnt;
  if(BU_Struct->ChCnt>cnt)
    cnt=BU_Struct->ChCnt;
  
  if(BU_Struct->HhCnt>cnt)
    cnt=BU_Struct->HhCnt;

  firstDimMatch=malloc(cnt*sizeof(int));
  if(!firstDimMatch)
    {
      printf("Cannot reserve memory");
      ApiObject_Free(peakList);
      ApiObject_Free(shiftList);
      return 0;
    }
  for(i=0;i<cnt;i++)
    {
      firstDimMatch[i]=-1;
    }
      
  /* Set some initial variables depending on experiment type */
  switch(type)
    {
    case SP_ID_PROTON:
      strcpy(isotope[0],"1H");
      firstDimCorrection=BU_Struct->HCorrection;
      /*      if(!CC_AddResonancesToPeaks(firstDimMatch, &shiftList, BU_Struct, firstDim, n, SP_ID_PROTON) && !addnew)
	{
	  ApiObject_Free(peakList);
	  ApiObject_Free(shiftList);
	  free(firstDimMatch);
	  return 0;
	  }*/
      break;
    case SP_ID_CARBON:
      strcpy(isotope[0],"13C");
      firstDimCorrection=BU_Struct->CCorrection;
      /*      if(!CC_AddResonancesToPeaks(firstDimMatch, &shiftList, BU_Struct, firstDim, n, SP_ID_CARBON) && !addnew)
	{
	  ApiObject_Free(peakList);
	  ApiObject_Free(shiftList);
	  free(firstDimMatch);
	  return 0;
	  }*/
      break;
    case SP_ID_2D_CH:
      
      strcpy(isotope[0],"13C");
      strcpy(isotope[1],"1H");
      firstDimCorrection=BU_Struct->CCorrection;
      secondDimCorrection=BU_Struct->HCorrection;
      secondDimMatch=malloc(cnt*sizeof(int));
      if(!secondDimMatch)
	{
	  printf("Cannot reserve memory");
	  free(firstDimMatch);
	  ApiObject_Free(peakList);
	  ApiObject_Free(shiftList);
	  return 0;  
	}
      for(i=0;i<cnt;i++)
	{
	  secondDimMatch[i]=-1;
	}
      /*      if((!CC_AddResonancesToPeaks(firstDimMatch, &shiftList, BU_Struct, firstDim, n, SP_ID_CARBON) || !CC_AddResonancesToPeaks(secondDimMatch, &shiftList, BU_Struct, secondDim, n, SP_ID_PROTON)) && !addnew)
	{
	  free(firstDimMatch);
	  free(secondDimMatch);
	  ApiObject_Free(peakList);
	  ApiObject_Free(shiftList);
	  return 0;
	  }*/
      break;
    case SP_ID_2D_HH:
      strcpy(isotope[0],"1H");
      strcpy(isotope[1],"1H");
      firstDimCorrection=BU_Struct->HCorrection;
      secondDimCorrection=BU_Struct->HCorrection;
      secondDimMatch=malloc(cnt*sizeof(int));
      if(!secondDimMatch)
	{
	  printf("Cannot reserve memory");
	  free(firstDimMatch);
	  ApiObject_Free(peakList);
	  ApiObject_Free(shiftList);
	  return 0;  
	}
      for(i=0;i<cnt;i++)
	{
	  secondDimMatch[i]=-1;
	}
      /*      if((CC_AddResonancesToPeaks(firstDimMatch, &shiftList, BU_Struct, firstDim, n, SP_ID_PROTON) || CC_AddResonancesToPeaks(secondDimMatch, &shiftList, BU_Struct, secondDim, n, SP_ID_PROTON)) && !addnew)
	{
	  free(firstDimMatch);
	  free(secondDimMatch);
	  ApiObject_Free(peakList);
	  ApiObject_Free(shiftList);
	  return 0;
	  }*/
      break;
    }

  parameters = ApiMap_New();
  ApiMap_SetFloat(parameters, "figOfMerit", 1);
  ApiMap_SetString(parameters, "details", "From CASPER");
  ApiMap_SetFloat(parameters, "constraintWeight", 1);
  resonanceParameters[0] = ApiMap_New();
  ApiMap_SetString(resonanceParameters[0], "isotopeCode", isotope[0]);
  if(secondDim)
    {
        resonanceParameters[1] = ApiMap_New();
	ApiMap_SetString(resonanceParameters[1], "isotopeCode", isotope[1]);
    }
  
  peakContribParameters=ApiMap_New();
  /* If the division in PeakContribs is not significant, but simply a way of describing 
     alternative assignments, all weights may be set to zero. */
  ApiMap_SetFloat(peakContribParameters, "weight", 0);

  apiIntFirstDim=ApiInteger_New(1);
  apiIntSecondDim=ApiInteger_New(2);

  for(i=0;i<n;i++)
    {
      peak=0;
      /* Set the starting values for the peak that will be created. */
      if(numDim==1)
	{
	  peak=CC_FindPeak(peakList, firstDim[i]+firstDimCorrection, 0, numDim, 0.00001, &apiIntFirstDim, &apiIntSecondDim);
	  /* Try with higher tolerance */
	  if(!peak)
	    {
	      peak=CC_FindPeak(peakList, firstDim[i]+firstDimCorrection, 0, numDim, 0.005, &apiIntFirstDim, &apiIntSecondDim);
	    }
	}
      else
	{
	  peak=CC_FindPeak(peakList, firstDim[i]+firstDimCorrection,
			   secondDim[i]+secondDimCorrection, numDim, 0.00001, &apiIntFirstDim, &apiIntSecondDim);
	  if(!peak)
	    {
	      peak=CC_FindPeak(peakList, firstDim[i]+firstDimCorrection,
			       secondDim[i]+secondDimCorrection, numDim, 0.005, &apiIntFirstDim, &apiIntSecondDim);	      
	    }
	}

      /* Create the peak if it was not found */
      if(!peak||ApiObject_IsNone(peak))
	{
	  peak=Nmr_PeakList_NewPeak(peakList, parameters);
	}

      if(!peak||ApiObject_IsNone(peak)||!Nmr_Peak_CheckAllValid(peak, ApiBoolean_False()))
	{
	  printf("Cannot create peak or created peak not valid. Line %d.\n", __LINE__);
	  printRaisedException();
	  if(peak && !ApiObject_IsNone(peak))
	    {
	      ApiObject_Free(peak);
	    }
	  free(firstDimMatch);
	  ApiObject_Free(resonanceParameters[0]);
	  if(secondDimMatch)
	    {
	      ApiObject_Free(resonanceParameters[1]);
	      free(secondDimMatch);
	    }
	  projectValid=0;
	  ApiObject_Free(shiftList);
	  ApiObject_Free(peakList);
	  ApiObject_Free(peakContribParameters);
	  ApiObject_Free(parameters);
	  ApiObject_Free(apiIntFirstDim);
	  ApiObject_Free(apiIntSecondDim);
	  return i;
	}
      if(addnew!=1)
	{
	  peakContribs=Nmr_Peak_GetPeakContribs(peak);
	  if(ApiSet_Len(peakContribs)>0)
	    {
	      peakContrib=ApiSet_Get(peakContribs,0);
	    }
	}
      if(!peakContrib)
	{
	  peakContrib=Nmr_Peak_NewPeakContrib(peak,peakContribParameters);
	}

      if(!peakContrib||ApiObject_IsNone(peakContrib))
	{
	  printf("PeakContrib error. Line %d.\n", __LINE__);
	  free(firstDimMatch);
	  ApiObject_Free(resonanceParameters[0]);
	  if(secondDimMatch)
	    {
	      ApiObject_Free(resonanceParameters[1]);
	      free(secondDimMatch);
	    }
	  projectValid=0;
	  ApiObject_Free(peak);
	  ApiObject_Free(shiftList);
	  ApiObject_Free(peakList);
	  ApiObject_Free(peakContribParameters);
	  ApiObject_Free(parameters);
	  ApiObject_Free(apiIntFirstDim);
	  ApiObject_Free(apiIntSecondDim);
	  return 0;
	}
      
      /* Check that there is at least one dimension in the spectrum and that the float array in the first dimension
	 is provided */
      if(firstDim && numDim>=1)
	{
	  peakDim=Nmr_Peak_FindFirstPeakDim_keyval1(peak, "dim", apiIntFirstDim);
	  
	  if(peakDim && !ApiObject_IsNone(peakDim))
	    {
	      dataDim=Nmr_PeakDim_GetDataDim(peakDim);
	      dataDimRefs=ApiSet_New();
	      
	      /* Get the correct Data Dimension Reference for this dim */
	      dataDimRefs=Nmr_FreqDataDim_GetDataDimRefs(dataDim);
	      dataDimRef=ApiSet_Get(dataDimRefs,0);
	      
	      /*	      if(!Nmr_Peak_CheckAllValid(peak, ApiBoolean_False()))
		{
		  printf("Peak not valid before setting values\n");
		  }*/

	      CC_SetPeakValues(nmrProj, shiftList, peak, peakDim, peakContrib, dataDimRef, dataDim,
			       resonanceParameters[0],firstDim[i]+firstDimCorrection, firstDimMatch[i], addnew);
	      /*	      printf("PeakDim: Dim %d. Value %f.\n", ApiInteger_Get(Nmr_PeakDim_GetDim(peakDim)), ApiFloat_Get(Nmr_PeakDim_GetValue(peakDim)));*/
	      ApiObject_Free(peakDim);
	      ApiObject_Free(dataDimRef);
	      ApiObject_Free(dataDimRefs);
	      ApiObject_Free(dataDim);

	      if(!Nmr_Peak_CheckAllValid(peak, ApiBoolean_False()))
		{
		  printf("Cannot create peak. Line %d.\n", __LINE__);
		  printRaisedException();
		  if(peak && !ApiObject_IsNone(peak))
		    {
		      ApiObject_Free(peak);
		    }
		  free(firstDimMatch);
		  ApiObject_Free(resonanceParameters[0]);
		    if(secondDimMatch)
		    {
		      ApiObject_Free(resonanceParameters[1]);
		      free(secondDimMatch);
		    }
		  projectValid=0;
		  ApiObject_Free(shiftList);
		  ApiObject_Free(peakList);
		  ApiObject_Free(peakContribParameters);
		  ApiObject_Free(parameters);
		  ApiObject_Free(apiIntFirstDim);
		  ApiObject_Free(apiIntSecondDim);
		  return 0;
		}
	    }
	  else
	    {
	      printf("Cannot find first dimension of spectrum. Line %d.\n", __LINE__);
	      if(peakDims && !ApiObject_IsNone(peakDims))
		{
		  ApiObject_Free(peakDims);
		}
	      ApiObject_Free(parameters);
	      ApiObject_Free(shiftList);
	      ApiObject_Free(peakList);
	      ApiObject_Free(peak);
	      ApiObject_Free(peakContribParameters);
	      ApiObject_Free(parameters);
	      ApiObject_Free(resonanceParameters[0]);
	      free(firstDimMatch);
	      if(secondDimMatch)
		{
		  free(secondDimMatch);
		  ApiObject_Free(resonanceParameters[1]);
		}
	      projectValid=0;
	      ApiObject_Free(apiIntFirstDim);
	      ApiObject_Free(apiIntSecondDim);
	      
	      return 0;
	    }
	}
      
      /* Do the same for the second dimension if there is an array with data provided and if the
	 spectrum has two dimensions. */
      if(secondDim && numDim>=2)
	{
	  peakDim=Nmr_Peak_FindFirstPeakDim_keyval1(peak, "dim", apiIntSecondDim);
	  if(peakDim && !ApiObject_IsNone(peakDim))
	    {
	      dataDim=Nmr_PeakDim_GetDataDim(peakDim);
	      dataDimRefs=ApiSet_New();
	      
	      /* Get the correct Data Dimension Reference for this dim */
	      dataDimRefs=Nmr_FreqDataDim_GetDataDimRefs(dataDim);
	      dataDimRef=ApiSet_Get(dataDimRefs,0);

	      /*	      if(!Nmr_Peak_CheckAllValid(peak, ApiBoolean_False()))
		{
		  printf("Peak not valid before setting values\n");
		  }*/

	      CC_SetPeakValues(nmrProj, shiftList, peak, peakDim, peakContrib, dataDimRef, dataDim,
			       resonanceParameters[1],secondDim[i]+secondDimCorrection, secondDimMatch[i], addnew);
	      /*	      printf("PeakDim: Dim %d. Value %f.\n", ApiInteger_Get(Nmr_PeakDim_GetDim(peakDim)), ApiFloat_Get(Nmr_PeakDim_GetValue(peakDim)));*/
	      ApiObject_Free(peakDim);
	      ApiObject_Free(dataDimRef);
	      ApiObject_Free(dataDimRefs);
	      ApiObject_Free(dataDim);

	      if(!Nmr_Peak_CheckAllValid(peak, ApiBoolean_False()))
		{
		  printf("Cannot create peak. Line %d.\n", __LINE__);
		  printRaisedException();
		  if(peak && !ApiObject_IsNone(peak))
		    {
		      ApiObject_Free(peak);
		    }
		  free(firstDimMatch);
		  free(secondDimMatch);
		  ApiObject_Free(resonanceParameters[0]);
		  ApiObject_Free(resonanceParameters[1]);
		  projectValid=0;
		  ApiObject_Free(shiftList);
		  ApiObject_Free(peakList);
		  ApiObject_Free(peakContribParameters);
		  ApiObject_Free(parameters);
		  ApiObject_Free(apiIntFirstDim);
		  ApiObject_Free(apiIntSecondDim);
		  return 0;
		}
	    }
	  else
	    {
	      printf("Cannot find second dimension of spectrum. Line %d.\n", __LINE__);
	      ApiObject_Free(peakDims);
	      ApiObject_Free(parameters);
	      ApiObject_Free(shiftList);
	      ApiObject_Free(peakList);
	      ApiObject_Free(peak);
	      ApiObject_Free(resonanceParameters[0]);
	      if(resonanceParameters[1] &&
		 !ApiObject_IsNone(resonanceParameters[1]))
		{
		  ApiObject_Free(resonanceParameters[1]);
		}
	      free(firstDimMatch);
	      free(secondDimMatch);
	      projectValid=0;
	      ApiObject_Free(apiIntFirstDim);
	      ApiObject_Free(apiIntSecondDim);

	      return 0;
	    }
	}
      ApiObject_Free(peakContrib);
      peakContrib=0;
    }
  ApiObject_Free(apiIntFirstDim);
  ApiObject_Free(apiIntSecondDim);
  ApiObject_Free(parameters);
  ApiObject_Free(peakContribParameters);
  if(resonanceParameters[1] && !ApiObject_IsNone(resonanceParameters[1]))
    {
      ApiObject_Free(resonanceParameters[1]);
    }
  ApiObject_Free(resonanceParameters[0]);

  ApiObject_Free(shiftList);
  ApiObject_Free(peakList);
  free(firstDimMatch);
  if(secondDimMatch)
    {
      free(secondDimMatch);
    }
  return i;
}


/* This function returns the first peak in peakList at the position specified by the values
   of firstDim and secondDim. numDim specifies how many dimensions there are. */
Nmr_Peak CC_FindPeak(Nmr_PeakList peakList, float firstDim, float secondDim, int numDim, float tolerance,
		     ApiInteger *apiIntFirstDim, ApiInteger *apiIntSecondDim)
{
  ApiSet peaks;
  ApiSetIterator peakIterator;
  ApiList isotopeCodes;
  ApiString isotopeCode;
  ApiFloat value;
  float firstPeakDimValue, secondPeakDimValue;
  Nmr_Peak peak=0;
  Nmr_PeakDim firstPeakDim, secondPeakDim;
  Nmr_DataDimRef dataDimRef;
  Nmr_ExpDimRef expDimRef;
  int numPeaks;

  peaks=0;
  if(peakList&&!ApiObject_IsNone(peakList))
    {
      peaks=Nmr_PeakList_GetPeaks(peakList);
    }
  if(!peaks)
    {
      return 0;
    }
  else
    {
      numPeaks=ApiSet_Len(peaks);
    }
  if(numPeaks<=0)
    {
      return 0;
    }

  peakIterator=ApiSet_Iterator(peaks);
  /* Check the first peak and at the same time check that firstDim is the first dimenstion in the peak. In CASPER firstDim
     is the highest. In CCPN it can vary. Only do this check for the first peak. */
  if(numDim==2)
    {
      peak=ApiSetIterator_Next(peakIterator);
      secondPeakDim=Nmr_Peak_FindFirstPeakDim_keyval1(peak, "dim", *apiIntSecondDim);
      dataDimRef=Nmr_PeakDim_GetDataDimRef(secondPeakDim);
      if(dataDimRef && !ApiObject_IsNone(dataDimRef))
	{
	  expDimRef=Nmr_DataDimRef_GetExpDimRef(dataDimRef);
	  if(expDimRef)
	    {
	      isotopeCodes=Nmr_ExpDimRef_GetIsotopeCodes(expDimRef);
	      if(isotopeCodes && ApiList_Len(isotopeCodes)>0)
		{
		  isotopeCode=ApiList_Get(isotopeCodes, 0);
		  if(strcmp(ApiString_Get(isotopeCode), "13C")==0)
		    {
		      ApiObject_Free(*apiIntFirstDim);
		      ApiObject_Free(*apiIntSecondDim);
		      *apiIntFirstDim=ApiInteger_New(2);
		      *apiIntSecondDim=ApiInteger_New(1);
		    }
		  ApiObject_Free(isotopeCode);
		  ApiObject_Free(isotopeCodes);
		}
	      ApiObject_Free(expDimRef);
	    }
	  ApiObject_Free(dataDimRef);
	}
      peakIterator=ApiSet_Iterator(peaks);
    }

  /* Iterate through all peaks in the peakList */
  while((peak=ApiSetIterator_Next(peakIterator)))
    {
      firstPeakDim=Nmr_Peak_FindFirstPeakDim_keyval1(peak, "dim", *apiIntFirstDim);
      if(firstPeakDim)
	{
	  value=Nmr_PeakDim_GetValue(firstPeakDim);
	  ApiObject_Free(firstPeakDim);
	  firstPeakDimValue=ApiFloat_Get(value);
	  ApiObject_Free(value);
	  if(fabs(firstPeakDimValue-firstDim)<tolerance)
	    {
	      /* If there is one more dimension check that one too */
	      if(numDim>=2)
		{
		  secondPeakDim=Nmr_Peak_FindFirstPeakDim_keyval1(peak, "dim", *apiIntSecondDim);
		  if(secondPeakDim)
		    {
		      value=Nmr_PeakDim_GetValue(secondPeakDim);
		      ApiObject_Free(secondPeakDim);
		      secondPeakDimValue=ApiFloat_Get(value);
		      ApiObject_Free(value);
		      if(fabs(secondPeakDimValue-secondDim)<tolerance)
			{
			  break;
			}
		      else
			{
			  ApiObject_Free(peak);
			  peak=0;
			}
		    }
		  else
		    {
		      ApiObject_Free(peak);
		      peak=0;
		    }
		}
	      else
		{
		  break;
		}
	    }
	  else
	    {
	      ApiObject_Free(peak);
	      peak=0;
	    }
	}
      else
	{
	  ApiObject_Free(peak);
	  peak=0;
	}
    }
  ApiObject_Free(peaks);
  return peak;
}

/* This function returns all peaks in a collection of peakLists at the position specified by the values
   of firstDim and secondDim. numDim specifies how many dimensions there are. */
ApiSet CC_FindPeaks(ApiSet peakLists, float firstDim, float secondDim, int numDim, float tolerance,
		    ApiInteger *apiIntFirstDim, ApiInteger *apiIntSecondDim)
{
  ApiSet peaks, foundPeaks;
  ApiSetIterator peakIterator, peakListIterator;
  ApiList isotopeCodes;
  ApiString isotopeCode;
  ApiFloat value;
  float firstPeakDimValue, secondPeakDimValue;
  Nmr_Peak peak=0;
  Nmr_PeakDim firstPeakDim, secondPeakDim;
  Nmr_PeakList peakList;
  Nmr_DataDimRef dataDimRef;
  Nmr_ExpDimRef expDimRef;

  foundPeaks=ApiSet_New();

  if(!peakLists || ApiObject_IsNone(peakLists))
    {
      return 0;
    }
  peakListIterator=ApiSet_Iterator(peakLists);
  while((peakList=ApiSetIterator_Next(peakListIterator)))
    {
      peaks=Nmr_PeakList_GetPeaks(peakList);
      if(ApiSet_Len(peaks)<=0)
	{
	  continue;
	}

      peakIterator=ApiSet_Iterator(peaks);
      /* Check the first peak and at the same time check that firstDim is the first dimenstion in the peak. In CASPER firstDim
	 is the highest. In CCPN it can vary. Only do this check for the first peak in each peakList. */
      if(numDim==2)
	{
	  peak=ApiSetIterator_Next(peakIterator);
	  secondPeakDim=Nmr_Peak_FindFirstPeakDim_keyval1(peak, "dim", *apiIntSecondDim);
	  dataDimRef=Nmr_PeakDim_GetDataDimRef(secondPeakDim);
	  if(dataDimRef && !ApiObject_IsNone(dataDimRef))
	    {
	      expDimRef=Nmr_DataDimRef_GetExpDimRef(dataDimRef);
	      if(expDimRef)
		{
		  isotopeCodes=Nmr_ExpDimRef_GetIsotopeCodes(expDimRef);
		  if(isotopeCodes && ApiList_Len(isotopeCodes)>0)
		    {
		      isotopeCode=ApiList_Get(isotopeCodes, 0);
		      if(strcmp(ApiString_Get(isotopeCode), "13C")==0)
			{
			  if(ApiInteger_Get(*apiIntFirstDim)==1)
			    {
			      ApiObject_Free(*apiIntFirstDim);
			      ApiObject_Free(*apiIntSecondDim);
			      *apiIntFirstDim=ApiInteger_New(2);
			      *apiIntSecondDim=ApiInteger_New(1);
			    }
			  else
			    {
			      ApiObject_Free(*apiIntFirstDim);
			      ApiObject_Free(*apiIntSecondDim);
			      *apiIntFirstDim=ApiInteger_New(1);
			      *apiIntSecondDim=ApiInteger_New(2);
			    }
			}
		      ApiObject_Free(isotopeCode);
		      ApiObject_Free(isotopeCodes);
		    }
		  ApiObject_Free(expDimRef);
		}
	      ApiObject_Free(dataDimRef);
	    }
	  peakIterator=ApiSet_Iterator(peaks);
	}

      /* Iterate through all peaks in the peakList */
      while((peak=ApiSetIterator_Next(peakIterator)))
	{
	  firstPeakDim=Nmr_Peak_FindFirstPeakDim_keyval1(peak, "dim", *apiIntFirstDim);
	  if(firstPeakDim)
	    {
	      value=Nmr_PeakDim_GetValue(firstPeakDim);
	      ApiObject_Free(firstPeakDim);
	      firstPeakDimValue=ApiFloat_Get(value);
	      ApiObject_Free(value);
	      if(fabs(firstPeakDimValue-firstDim)<tolerance)
		{
		  /* If there is one more dimension check that one too */
		  if(numDim>=2)
		    {
		      secondPeakDim=Nmr_Peak_FindFirstPeakDim_keyval1(peak, "dim", *apiIntSecondDim);
		      if(secondPeakDim)
			{
			  value=Nmr_PeakDim_GetValue(secondPeakDim);
			  ApiObject_Free(secondPeakDim);
			  secondPeakDimValue=ApiFloat_Get(value);
			  ApiObject_Free(value);
			  if(fabs(secondPeakDimValue-secondDim)<tolerance)
			    {
			      ApiSet_Add(foundPeaks, peak);
			    }
			}
		    }
		  else
		    {
		      ApiSet_Add(foundPeaks, peak);
		    }
		}
	    }
	  ApiObject_Free(peak);
	}
      ApiObject_Free(peaks);
      ApiObject_Free(peakList);
    }
  return foundPeaks;
}
 
/* This function reads the peak positions of experiments in the CCPN project and sets
   the shifts in the CASPER BU_Struct that spectrum points to.
   The types of spectra that are used are: C, H, HSQC, TOCSY  and the
   positions are put into experiment->CShifts, experiment->HShifts etc.
   The function returns the number of loaded spectra and 0 if it fails. */
int CC_LoadSpectra(Nmr_NmrProject *nmrProj, unsigned int mode,
		   struct BU_Struct *spectrum)
{
  ApiSet exps;
  Nmr_Experiment experiment=0;
  /*  struct BU_Struct *spectrum;*/
  int numExps, nSpectra=0;

  /* ME_CreateNode creates a node if it doesn't exist or just returns the address
     of the node if it is already there */
  
  if(mode==0||mode==SP_ID_CARBON)
    {
      exps = CC_GetExpOfType(nmrProj,"C");
      if(exps)
	{
	  numExps=ApiSet_Len(exps);
	  if(numExps>0)
	    {
	      experiment = ApiSet_Get(exps,0);
	      spectrum->CCnt=CC_GetPeaks(&experiment, spectrum->CShift, 0);
	      nSpectra++;
	    }
	}
      /* If no shifts from 13C spectra were found see if there are any 13C resonances with shifts.
	 This means that chemical shifts can be loaded from projects that do not contain any spectra */
      /* DISABLED */
      /*      if(spectrum->CCnt==0)
	{
	  spectrum->CCnt=CC_GetShifts(nmrProj, "13C", spectrum->CShift);
	  nSpectra++;
	  }*/
    }

  if(mode==0||mode==SP_ID_PROTON)
    {
      exps = CC_GetExpOfType(nmrProj,"H");
      if(exps)
	{
	  numExps=ApiSet_Len(exps);
	  if(numExps>0)
	    {
	      experiment = ApiSet_Get(exps,0);
	      spectrum->HCnt=CC_GetPeaks(&experiment, spectrum->HShift, 0);
	      nSpectra++;
	    }
	}
      /* If no shifts from 1H spectra were found see if there are any 1H resonances with shifts.
	 This means that chemical shifts can be loaded from projects that do not contain any spectra */
      /* DISABLED */
      /*      if(spectrum->HCnt==0)
	{
	  spectrum->HCnt=CC_GetShifts(nmrProj, "1H", spectrum->HShift);
	  nSpectra++;
	  }*/
    }
  
  if(mode==0||mode==SP_ID_2D_CH)
    {
      exps = CC_GetExpOfType(nmrProj,"H[C]");
      if(exps)
	{
	  numExps=ApiSet_Len(exps);
	  if(numExps>0)
	    {
	      experiment = ApiSet_Get(exps,0);
	      spectrum->ChCnt=CC_GetPeaks(&experiment, spectrum->ChCShift, spectrum->ChHShift);
	      nSpectra++;
	    }
	}
    }

  if(mode==0||mode==SP_ID_2D_HH)
    {
      exps = CC_GetExpOfType(nmrProj,"H_H.TOCSY");
      if(exps)
	{
	  numExps=ApiSet_Len(exps);
	  if(numExps>0)
	    {
	      experiment = ApiSet_Get(exps,0);
	      spectrum->HhCnt=CC_GetPeaks(&experiment, spectrum->HhHiShift, spectrum->HhLoShift);
	      nSpectra++;
	    }
	}
    }

  
  if(exps && !ApiObject_IsNone(exps))
    {
      ApiObject_Free(exps);
    }

  if(experiment)
    {
      if(!ApiObject_IsNone(experiment))
	{
	  ApiObject_Free(experiment);
	}
    }
  /*  if(nSpectra==0)
    {
      ME_RemoveNode(&StructMethod,spectrum);
      return 0;
    }
  */

  return nSpectra;
}

/* This function creates a new spectrum (new Nmr_DataSource) in the specified NmrProject.
   To create an Nmr_DataSource an Nmr_Experiment first has to be created.
   To be able to add peaks with positions Data Dimensions (Nmr_FreqDataDim)with
   References (Nmr_DataDimRef) are created.
   The newly created Nmr_DataSource is returned if the function is
   successful. */
Nmr_DataSource CC_NewSpectrum(Impl_MemopsRoot *ccpnProj, Nmr_NmrProject *nmrProj, int type)
{
  ApiMap parameters = ApiMap_New();
  ApiString str;
  Nmr_Experiment experiment;
  Nmr_DataSource spectrum;
  char name[NODE_NAME_LENGTH];
  char refExpName[24];
  char category[16];
  char isotope[2][16];
  int numDim, i,j; 
  int numPointsOrig[2], pointOffset[2];
  float valuePerPoint[2], sf[2], refValue[2];
  Nmr_FreqDataDim dataDimension[2];
  Nmr_DataDimRef refDimension[2];
  Nmr_ExpDim expDim;
  Nmr_ExpDimRef expDimRef;
  ApiSet refExperiments, expPrototypes, expDims;
  Nmrx_NmrExpPrototype expPrototype;
  Nmrx_RefExperiment refExperiment;
  int nRefExps, nExpProts, typefound=0;

  /* Set the parameters for creating the spectrum depending on type. */
  switch(type)
    {
    case SP_ID_PROTON:
      strcpy(name,"H");
      numDim=1;
      /* TODO: These values are just approximated from experimental spectra. Might need to be adjusted. */
      numPointsOrig[0]=32768;
      pointOffset[0]=0;
      valuePerPoint[0]=0.15;
      sf[0]=500.130;
      refValue[0]=6;
      strcpy(isotope[0],"1H");
      strcpy(refExpName,"H");
      strcpy(category,"other");
      break;
    case SP_ID_CARBON:
      strcpy(name,"C");
      numDim=1;
      /* TODO: These values are just approximated from experimental spectra. Might need to be adjusted. */
      numPointsOrig[0]=32768;
      pointOffset[0]=0;
      valuePerPoint[0]=0.40;
      sf[0]=125.758;
      refValue[0]=100;
      strcpy(isotope[0],"13C");     
      strcpy(refExpName,"C");
      strcpy(category,"other");
      break;
    case SP_ID_2D_CH:
      strcpy(name,"HSQC");
      numDim=2;
      /* TODO: These values are just approximated from experimental spectra. Might need to be adjusted. */
      numPointsOrig[0]=2048;
      pointOffset[0]=0;
      valuePerPoint[0]=11.1;
      sf[0]=125.758;
      refValue[0]=100;
      numPointsOrig[1]=8192;
      pointOffset[1]=0;
      valuePerPoint[1]=0.9;
      sf[1]=500.130;
      refValue[1]=6;
      strcpy(isotope[0],"13C");
      strcpy(isotope[1],"1H");     
      strcpy(refExpName,"H[C]");
      strcpy(category,"through-bond");
      break;
    case SP_ID_2D_HH:
      strcpy(name,"TOCSY");
      numDim=2;
      /* TODO: These values are just approximated from experimental spectra. Might need to be adjusted. */
      numPointsOrig[0]=8192;
      pointOffset[0]=0;
      valuePerPoint[0]=0.8;
      sf[0]=500.130;
      refValue[0]=6;
      numPointsOrig[1]=2048;
      pointOffset[1]=0;
      valuePerPoint[1]=3.2;
      sf[1]=500.130;
      refValue[1]=6;
      strcpy(isotope[0],"1H");
      strcpy(isotope[1],"1H");
      strcpy(refExpName,"H_H.TOCSY");
      strcpy(category,"through-bond");
      break;
      
    }

  ApiMap_SetString(parameters, "name", name);
  ApiMap_SetInt(parameters, "numDim", numDim);
  ApiMap_SetString(parameters, "details", "User input from CASPER");
  experiment=Nmr_NmrProject_NewExperiment(*nmrProj, parameters);

  if(!experiment)
    {
      printRaisedException();
      printf("Cannot create experiment. Line %d.\n", __LINE__);
      ApiObject_Free(parameters);
      projectValid=0;
      return 0;
    }

  /*  if(!Nmr_NmrProject_CheckAllValid(*nmrProj, ApiBoolean_False()))
      printf("NmrProject not valid after adding peaks.\n"); */

  ApiMap_SetString(parameters, "dataType", "processed");
  ApiMap_SetBoolean(parameters, "isSimulated", 0);
  spectrum=Nmr_Experiment_NewDataSource(experiment, parameters);

  ApiObject_Free(parameters);

  expDims = Nmr_Experiment_GetExpDims(experiment);

  if(!expDims)
    {
      printf("Experiment error. Line %d.", __LINE__);
      projectValid=0;
      return 0;
    }

  for(i=0;i<ApiSet_Len(expDims);i++)
    {
      expDim=ApiSet_Get(expDims,i);
      
      /* Create the DataDims using experiment specific parameters */
      parameters = ApiMap_New();
      ApiMap_SetInt(parameters, "dim", i+1);
      ApiMap_SetInt(parameters, "numPointsOrig", numPointsOrig[i]);
      ApiMap_SetFloat(parameters, "valuePerPoint", valuePerPoint[i]);
      ApiMap_SetInt(parameters, "numPoints", numPointsOrig[i]);
      ApiMap_SetBoolean(parameters, "isComplex", 0);
      ApiMap_SetItem(parameters, "expDim", expDim);
      ApiMap_SetString(parameters, "unit", "ppm");
      
      dataDimension[i]=Nmr_DataSource_NewFreqDataDim(spectrum, parameters);
      
      ApiObject_Free(parameters);
      parameters = ApiMap_New();
      ApiMap_SetFloat(parameters, "sf", sf[i]);
      ApiMap_SetString(parameters, "unit", "ppm");
      expDimRef=Nmr_ExpDim_NewExpDimRef(expDim, parameters);
      ApiObject_Free(parameters);

      /*      ApiSet isotopes=Nmr_ExpDimRef_GetIsotopeCodes(expDimRef);*/

      str=ApiString_New(isotope[i]);
      Nmr_ExpDimRef_AddIsotopeCode(expDimRef,str);
      ApiObject_Free(str);
      
      parameters = ApiMap_New();
      ApiMap_SetItem(parameters, "expDimRef", expDimRef);
      ApiMap_SetFloat(parameters, "refPoint", 1.0);
      ApiMap_SetFloat(parameters, "refValue", refValue[i]);
      refDimension[i]=Nmr_FreqDataDim_NewDataDimRef(dataDimension[i],parameters);

      ApiObject_Free(parameters);
      ApiObject_Free(expDim);
      ApiObject_Free(dataDimension[i]);
      ApiObject_Free(expDimRef);
      ApiObject_Free(refDimension[i]);
   }
  ApiObject_Free(expDims);

  /* Check if the Experiment Prototypes corresponding to the type of the new experiment already exists.
     If it is found the RefExperiment of the current Experiment is set */

  parameters=ApiMap_New();
  ApiMap_SetString(parameters, "name", refExpName);

  expPrototypes=Impl_MemopsRoot_FindAllNmrExpPrototypes(*ccpnProj,parameters);

  ApiObject_Free(parameters);

  if(expPrototypes)
    {
      nExpProts=ApiSet_Len(expPrototypes);
      
      for(i=0;i<nExpProts;i++)
	{
	  expPrototype=ApiSet_Get(expPrototypes,i);
	  str=Nmrx_NmrExpPrototype_GetName(expPrototype);
	  /*	  printf("ExpPrototype Name:%s\n",ApiString_Get(str));*/
	  ApiObject_Free(str);

	  refExperiments=Nmrx_NmrExpPrototype_GetRefExperiments(expPrototype);
	  if(refExperiments)
	    {
	      nRefExps=ApiSet_Len(refExperiments);
	      for(j=0;j<nRefExps;j++)
		{
		  refExperiment=ApiSet_Get(refExperiments,j);
		  str=Nmrx_RefExperiment_GetName(refExperiment);
		  /*		  printf("RefExp Name:%s\n",ApiString_Get(str));*/
		  if(strcmp(ApiString_Get(str),refExpName)==0)
		    {
		      Nmr_Experiment_SetRefExperiment(experiment,refExperiment);
		      typefound=1;
		      j=nRefExps;
		      i=nExpProts;
		    }
		  ApiObject_Free(str);
		}
	    }
	  ApiObject_Free(refExperiments);
	  ApiObject_Free(expPrototype);
	}
    }
  /* If the experiment type is not found the Experiment Prototype has to be created. */
  if(!typefound)
    {
      parameters = ApiMap_New();
      ApiMap_SetString(parameters, "name", refExpName);
      ApiMap_SetString(parameters, "category", category);

      expPrototype=Impl_MemopsRoot_NewNmrExpPrototype(*ccpnProj,parameters);

      ApiObject_Free(parameters);
      parameters = ApiMap_New();
      ApiMap_SetString(parameters, "name", refExpName);

      refExperiment=Nmrx_NmrExpPrototype_NewRefExperiment(expPrototype,parameters);
      Nmr_Experiment_SetRefExperiment(experiment,refExperiment);

      ApiObject_Free(parameters);
    }

  return spectrum;
}

int CC_AllPeaksInSpectra(Impl_MemopsRoot *ccpnProj, Nmr_NmrProject *nmrProj,
			    struct BU_Struct *BU_Struct)
{
  unsigned int mode;
  ApiSet experiments;
  Nmr_Experiment experiment;
  Nmr_DataSource ccpn_Spectrum=0;

  mode=SP_ID_PROTON;
  experiments=CC_GetExpOfType(&Global_NmrProject,"H");
  if(ApiSet_Len(experiments)>0 && projectValid)
    {
      experiment=ApiSet_Get(experiments,0);
      if(!CC_GetProcSpectrumOfDim(&experiment, 1, &ccpn_Spectrum))
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
  /*  ccpn_Spectrum=CC_NewSpectrum(ccpnProj, nmrProj, mode);*/
  if(ccpn_Spectrum)
    {
      if(!CC_PutPeaks(nmrProj, &ccpn_Spectrum, BU_Struct->HShift,
		      0, BU_Struct->HCnt, mode, 1, BU_Struct))
	{
	  ApiObject_Free(ccpn_Spectrum);
	  return 0;
	}
      ApiObject_Free(ccpn_Spectrum);
    }
  else
    {
      return 0;
    }
  mode=SP_ID_CARBON;
  experiments=CC_GetExpOfType(&Global_NmrProject,"C");
  if(ApiSet_Len(experiments)>0 && projectValid)
    {
      experiment=ApiSet_Get(experiments,0);
      if(!CC_GetProcSpectrumOfDim(&experiment, 1, &ccpn_Spectrum))
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
  /*  ccpn_Spectrum=CC_NewSpectrum(ccpnProj,nmrProj, mode);*/
  if(ccpn_Spectrum)
    {
      if(!CC_PutPeaks(nmrProj, &ccpn_Spectrum, BU_Struct->CShift,
		      0, BU_Struct->CCnt, mode, 1, BU_Struct))
	{
	  ApiObject_Free(ccpn_Spectrum);
	  return 0;
	}
      ApiObject_Free(ccpn_Spectrum);
    }
  else
    {
      return 0;
    }
  mode=SP_ID_2D_CH;
  experiments=CC_GetExpOfType(&Global_NmrProject,"H[C]");
  if(ApiSet_Len(experiments)>0 && projectValid)
    {
      experiment=ApiSet_Get(experiments,0);
      if(!CC_GetProcSpectrumOfDim(&experiment, 2, &ccpn_Spectrum))
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
  /*  ccpn_Spectrum=CC_NewSpectrum(ccpnProj,nmrProj, mode);*/
  if(ccpn_Spectrum)
    {
      if(!CC_PutPeaks(nmrProj, &ccpn_Spectrum, BU_Struct->ChCShift,
		      BU_Struct->ChHShift, BU_Struct->ChCnt, mode, 1, BU_Struct))
	{
	  ApiObject_Free(ccpn_Spectrum);
	  return 0;
	}
      ApiObject_Free(ccpn_Spectrum);
    }
  else
    {
      return 0;
    }
  mode=SP_ID_2D_HH;
  experiments=CC_GetExpOfType(&Global_NmrProject,"H_H.TOCSY");
  if(ApiSet_Len(experiments)>0 && projectValid)
    {
      experiment=ApiSet_Get(experiments,0);
      if(!CC_GetProcSpectrumOfDim(&experiment, 2, &ccpn_Spectrum))
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
  /*  ccpn_Spectrum=CC_NewSpectrum(ccpnProj,nmrProj, mode);*/
  if(ccpn_Spectrum)
    {
      if(!CC_PutPeaks(nmrProj, &ccpn_Spectrum, BU_Struct->HhHiShift,
		      BU_Struct->HhLoShift, BU_Struct->HhCnt, mode, 1, BU_Struct))
	{
	  ApiObject_Free(ccpn_Spectrum);
	  return 0;
	}
      ApiObject_Free(ccpn_Spectrum);
    }
  else
    {
      return 0;
    }
  
  return 1;
}

/* Adds a new shift to a shift list. The new shift is created with the specified
   resonance, peak, peakContrib, peakDim and value (shift in ppm). */
Nmr_Shift CC_NewShift(Nmr_ShiftList shiftList, Nmr_Peak peak, ApiSet peakContribs,
		      Nmr_PeakDim peakDim, Nmr_Resonance resonance, float value)
{
  ApiMap shiftParameters, peakDimContribParameters;
  Nmr_PeakDimContrib peakDimContrib;
  Nmr_Shift shift;
  
  shiftParameters=ApiMap_New();

  ApiMap_SetFloat(shiftParameters, "value", value);
  ApiMap_SetItem(shiftParameters, "resonance", resonance);

  shift=Nmr_ShiftList_NewShift(shiftList,shiftParameters);
  if(!shift)
    {
      printf("Could not create shift. Line %d.\n", __LINE__);
      printRaisedException();
      projectValid=0;
      return 0;
    }

  ApiObject_Free(shiftParameters);

  Nmr_Shift_AddPeak(shift,peak);

  Nmr_Shift_AddPeakDim(shift,peakDim);


  peakDimContribParameters=ApiMap_New();
  ApiMap_SetItem(peakDimContribParameters, "peakContribs", peakContribs);
  ApiMap_SetItem(peakDimContribParameters, "resonance", resonance);
  /*  printf("Making peakDimContrib\n"); */
  peakDimContrib=Nmr_PeakDim_NewPeakDimContrib(peakDim,peakDimContribParameters);

  if(!Nmr_Peak_CheckAllValid(peak, ApiBoolean_False()))
    {
      projectValid=0;
      printRaisedException();
      printf("Peak not valid after adding the new shift. Line %d.\n", __LINE__);
    }

  ApiObject_Free(peakDimContribParameters);
  ApiObject_Free(peakDimContrib);

  return shift;
}

/* Looks for all shifts in the specified shiftlist for those that have a shift (in ppm)
   close to the value specified in the function call. The peak is added to the shift
   and the resonance of the shift is added to the peakContrib */
int CC_FindResonance(Nmr_ShiftList shiftList, Nmr_Peak peak, Nmr_PeakContrib peakContrib,
			 Nmr_PeakDim peakDim, float value)
{
  ApiSet shifts, peakContribs;
  ApiMap peakDimContribParameters;
  Nmr_Shift shift;
  Nmr_Resonance resonance;
  Nmr_PeakDimContrib peakDimContrib;
  float shiftValue, tolerance;
  int i,n, cnt=0;


  tolerance=0.01; /* Same tolerance regardless of isotope. This might need to be changed. */

  shifts=Nmr_ShiftList_GetMeasurements(shiftList);
  if(shifts)
    {
      /*      similarShifts=ApiSet_New();*/
      n=ApiSet_Len(shifts);
      printf("n=%d\n",n);
      for(i=0;i<n;i++)
	{
	  shift=ApiSet_Get(shifts,i);
	  shiftValue=ApiFloat_Get(Nmr_Shift_GetValue(shift));
	  if(!Nmr_Shift_CheckAllValid(shift, ApiBoolean_False()))
	    {
	      projectValid=0;
	      printf("Shift not valid. Line %d.\n", __LINE__);
	      return 0;
	    }
	  
	  if(fabs(shiftValue-value)<tolerance)
	    {
	      resonance=Nmr_Shift_GetResonance(shift);
	      
	      shift=Nmr_ShiftList_NewShift_reqd(shiftList,resonance, value);
	      
	      Nmr_Shift_AddPeak(shift,peak);
	      
	      Nmr_Shift_AddPeakDim(shift,peakDim);

	      ApiObject_Free(resonance);
	      
	      resonance=Nmr_Shift_GetResonance(shift);
	      
	      peakContribs=ApiSet_New();
	      ApiSet_Add(peakContribs,peakContrib);
	      
	      peakDimContribParameters=ApiMap_New();
	      ApiMap_SetItem(peakDimContribParameters, "peakContribs", peakContribs);
	      ApiMap_SetItem(peakDimContribParameters, "resonance", resonance);
	      peakDimContrib=Nmr_PeakDim_NewPeakDimContrib(peakDim,peakDimContribParameters);
	      
	      
	      ApiObject_Free(resonance);
	      ApiObject_Free(shift);
	      ApiObject_Free(peakDimContrib);
	      ApiObject_Free(peakContribs);
	      ApiObject_Free(peakDimContribParameters);
	      
	      cnt++;
	    }
	  ApiObject_Free(shift);
	}
      /*  if(!Nmr_Peak_CheckAllValid(peak, ApiBoolean_False()))
	  printf("New peak not valid after adding shift to resonance.\n");*/
      ApiObject_Free(shifts);
    }
  return (cnt);
}

/* This function finds all shift in the shiftlist with a shift equal to
   value. All the resonances of those shifts are returned. */
ApiSet CC_FindResonances(Nmr_ShiftList *shiftList, float value)
{
  ApiSet shifts, resonances;
  Nmr_Shift shift;
  Nmr_Resonance resonance;
  float shiftValue, tolerance;
  int i,n;

  resonances=ApiSet_New();

  tolerance=0.00001; /* Same tolerance regardless of isotope. This might need to be changed. */

  shifts=Nmr_ShiftList_GetMeasurements(*shiftList);
  if(shifts)
    {
      n=ApiSet_Len(shifts);
    }
  else
    {
      n=0;
    }
  /*  printf("n=%d\n",n); */
  for(i=0;i<n;i++)
    {
      shift=ApiSet_Get(shifts,i);
      shiftValue=ApiFloat_Get(Nmr_Shift_GetValue(shift));

      if(fabs(shiftValue-value)<tolerance)
	{
	  resonance=Nmr_Shift_GetResonance(shift);

	  ApiSet_Add(resonances,resonance);
	  ApiObject_Free(resonance);
	}
      ApiObject_Free(shift);
    }
  ApiObject_Free(shifts);

  return (resonances);
}

void CC_SetPeakValues(Nmr_NmrProject *nmrProj, Nmr_ShiftList shiftList, Nmr_Peak peak,
		      Nmr_PeakDim peakDim, Nmr_PeakContrib peakContrib,
		      Nmr_DataDimRef dataDimRef, Nmr_FreqDataDim dataDim,
		      ApiMap resonanceParameters, float value,
		      int match, int addnew)
{
  ApiMap localResonanceParameters, peakDimContribParameters, measurementParameters;
  ApiSet shifts=0, peakContribs;
  ApiFloat apiValue;
  Nmr_Shift shift;
  Nmr_Resonance resonance;
  Nmr_PeakDimContrib peakDimContrib;
  int i, numShifts=0;

  /* The dataDimRef is not automatically assigned to the peakDim. This has to be done
     before the peak position in ppm (Value) can be set. */
  Nmr_PeakDim_SetDataDimRef(peakDim,dataDimRef);
  apiValue=ApiFloat_New(value);
  Nmr_PeakDim_SetValue(peakDim, apiValue);
  ApiObject_Free(apiValue);

  /* If this shift has already been matched to the serial number of
     an existing resonance */
  if(match>=0)
    {
      localResonanceParameters=ApiMap_New();
      ApiMap_SetInt(localResonanceParameters,"serial",match);
      resonance=Nmr_NmrProject_FindFirstResonance(*nmrProj,localResonanceParameters);
      ApiObject_Free(localResonanceParameters);
      if(resonance && !ApiObject_IsNone(resonance) && Impl_MemopsObject_GetIsDeleted(resonance)==ApiBoolean_False())
	{

	  peakContribs=ApiSet_New();
	  ApiSet_Add(peakContribs,peakContrib);

	  peakDimContribParameters=ApiMap_New();

	  ApiMap_SetItem(peakDimContribParameters, "resonance", resonance);
	  ApiMap_SetItem(peakDimContribParameters, "peakContribs",
			 peakContribs);

	  peakDimContrib=Nmr_PeakDim_FindFirstPeakDimContrib(peakDim,peakDimContribParameters);
	  if(!peakDimContrib||ApiObject_IsNone(peakDimContrib))
	    {
	      peakDimContrib=Nmr_PeakDim_NewPeakDimContrib(peakDim,peakDimContribParameters);
	    }
	  ApiObject_Free(peakDimContribParameters);
	  if(!Nmr_Peak_CheckAllValid(peak, ApiBoolean_False()))
	    {
	      printf("New peak not valid. Line %d.\n", __LINE__);
	      if(peakDimContrib && !ApiObject_IsNone(peakDimContrib))
		{
		  ApiObject_Free(peakDimContrib);
		}
	      ApiObject_Free(peakContribs);
	      ApiObject_Free(resonance);
	      projectValid=0;
	      return;
	    }

	  shifts=Nmr_Resonance_GetShifts(resonance);
	  if(shifts)
	    {
	      numShifts=ApiSet_Len(shifts);
	    }
	  else
	    {
	      numShifts=0;
	    }
	  for(i=0;i<numShifts;i++)
	    {
	      shift=ApiSet_Get(shifts,i);
	      Nmr_Shift_AddPeak(shift,peak);
	      Nmr_Shift_AddPeakDim(shift,peakDim);
	      ApiObject_Free(shift);
	    }
	}
      else
	{
	  resonance=Nmr_NmrProject_NewResonance(*nmrProj, resonanceParameters);
	  shift=CC_NewShift(shiftList, peak, peakContribs, peakDim, resonance, value);
	}
    }
  /* If the priority is to not add new resonances try to see if there are any
     close enough to the given chemical shift even if none had been matched
     beforehand. */
  else if(addnew!=1)
    {
      shifts=CC_FindShifts(nmrProj, value);
      if(shifts)
	{
	  numShifts=ApiSet_Len(shifts);
	}
      else
	{
	  numShifts=0;
	}
      measurementParameters=ApiMap_New();
      ApiMap_SetString(measurementParameters,"className","Shift");
      ApiObject_Free(measurementParameters);
      /*	  printf("NumShifts: %d\n",numShifts);*/
      for(i=0;i<numShifts;i++)
	{
	  shift=ApiSet_Get(shifts,i);
	  resonance=Nmr_Shift_GetResonance(shift);
	  
	  peakContribs=ApiSet_New();
	  ApiSet_Add(peakContribs,peakContrib);
	  peakDimContribParameters=ApiMap_New();
	  ApiMap_SetItem(peakDimContribParameters, "resonance", resonance);
	  ApiMap_SetItem(peakDimContribParameters, "peakContribs",
			 peakContribs);
	  peakDimContrib=Nmr_PeakDim_FindFirstPeakDimContrib(peakDim,peakDimContribParameters);
	  
	  if(!peakDimContrib||ApiObject_IsNone(peakDimContrib))
	    {
	      peakDimContrib=Nmr_PeakDim_NewPeakDimContrib(peakDim,peakDimContribParameters);
	    }
	  ApiObject_Free(peakDimContribParameters);
	  ApiObject_Free(peakContribs);
	  ApiObject_Free(shift);
	  ApiObject_Free(resonance);
	}
    }
  else
    {
      resonance=Nmr_NmrProject_NewResonance(*nmrProj, resonanceParameters);
      peakContribs=ApiSet_New();
      ApiSet_Add(peakContribs,peakContrib);
      /*	  peakDimContribParameters=ApiMap_New();
		  ApiMap_SetItem(peakDimContribParameters, "resonance", resonance);
		  ApiMap_SetItem(peakDimContribParameters, "peakContribs",
		  peakContribs);
		  peakDimContrib=Nmr_PeakDim_FindFirstPeakDimContrib(peakDim,peakDimContribParameters);
		  
		  if(!peakDimContrib||ApiObject_IsNone(peakDimContrib))
		  {
		  peakDimContrib=Nmr_PeakDim_NewPeakDimContrib(peakDim,peakDimContribParameters);
		  }
		  ApiObject_Free(peakDimContribParameters);
      */
      shift=CC_NewShift(shiftList, peak, peakContribs, peakDim, resonance, value);
      ApiObject_Free(shift);
    }
  ApiObject_Free(peakContrib);
  ApiObject_Free(shiftList);
  ApiObject_Free(peakContribs);
  ApiObject_Free(peakDim);
  
  if(shifts && !ApiObject_IsNone(shifts))
    {
      ApiObject_Free(shifts);
    }
  if(!Nmr_Peak_CheckAllValid(peak, ApiBoolean_False()))
    {
      printf("New peak not valid. Line %d.\n", __LINE__);
      ApiObject_Free(peak);
      projectValid=0;
      return;
    }
  ApiObject_Free(peak);
}

/* Populates a BU_Struct (CASPER structure) using the first molSystem in
   the project. If multiBuildFlag is set all chains in the molSystem will
   be built and put in subsequent BU_Structs in the list. */
int CC_ResiduetoUnit(Impl_MemopsRoot *ccpnProject, struct BU_Struct *BU_Struct, int multiBuildFlag)
{
  Mols_MolSystem molSystem;
  Mols_Chain chain;
  Mole_Molecule molecule;
  Mols_Residue residue;
  Mole_MolResLink molResLink;
  Mole_MolResLinkEnd molResLinkEnd;
  ApiSet chains, allResidues, allMolResLinks, allMolResLinkEnds;
  ApiSetIterator chainIterator, residueIterator, molResLinkIterator;
  ApiInteger nr;
  ApiString string;
  int i, j, cnt=0, allMolSystemsLen, nmods, flag, repeatFlag;
  int currResPos, fromResPos, toResPos, numChains, repeatFromPos, repeatToPos;
  char residuename[NODE_NAME_LENGTH], descriptor[32], stereo[4], *basetype, *basetype_tok, *type, *ringAndMods, *mods;
  char *substs, *mod, name[4], linkCode[16], structBaseName[NODE_NAME_LENGTH];
  struct TY_CTmodifier modProperties[8];
  struct RE_Residue *res;
  struct BU_Unit *BU_Unit, *repeatFromUnit=0, *repeatToUnit=0;
  struct BU_LinkDesc linkDesc;


  molSystem = Impl_MemopsRoot_GetCurrentMolSystem(*ccpnProject);

  if (!molSystem||ApiObject_IsNone(molSystem))
    {
      ApiSet allMolSystems = Impl_MemopsRoot_GetMolSystems(*ccpnProject);
      if(allMolSystems)
	{
	  allMolSystemsLen = ApiSet_Len(allMolSystems);
	}
      else
	{
	  allMolSystemsLen=0;
	}
      
      if (allMolSystemsLen) 
	{
	  molSystem = ApiSet_Get(allMolSystems,0);
	}
      else
	{
	  printRaisedException();
	  printf("No molecular system found - aborting. Line %d.\n", __LINE__);
	  return 0;
	}
      ApiObject_Free(allMolSystems);
    }

  /* A chain is required to make sure the residues are linked together. */
  chains=Mols_MolSystem_GetChains(molSystem);
  if(!chains || ApiSet_Len(chains)==0)
    {
      printRaisedException();
      printf("No chains found - aborting. Line %d.\n", __LINE__);
      return 0;
    }
  if(multiBuildFlag)
    {
      numChains=ApiSet_Len(chains);
      strcpy(structBaseName, BU_Struct->Node.Name);
      strcat(BU_Struct->Node.Name, "1");
    }
  else
    {
      numChains=1;
    }
  chainIterator=ApiSet_Iterator(chains);
  for(i=0; i<numChains; i++)
    {
      if(i>0)
	{
	  BU_Struct=(struct BU_Struct *)
	    ME_CreateNode(&StructMethod, &StructList, structBaseName);
	  sprintf(BU_Struct->Node.Name, "%s%d", structBaseName, i+1);
	  if(!BU_Struct)
	    {
	      ApiObject_Free(chains);
	      Error(PA_ERR_FATAL,"Out of memory");
	    }
	}
      chain=ApiSetIterator_Next(chainIterator);

      string=Mols_Chain_GetCode(chain);
      strcpy(BU_Struct->CcpnChainCode, ApiString_Get(string));
      ApiObject_Free(string);

      molecule=Mols_Chain_GetMolecule(chain);

      allResidues=Mols_Chain_GetResidues(chain);
      if(!allResidues)
	{
	  if(molecule && !ApiObject_IsNone(molecule))
	    {
	      printf("No residues found in molecule - aborting. Line %d.\n", __LINE__);
	    }
	  ApiObject_Free(chain);
	  ApiObject_Free(chains);
	  return 0;
	}
      residueIterator=ApiSet_Iterator(allResidues);
      while((residue=ApiSetIterator_Next(residueIterator)))
	{
	  /* Get the ccpCode and store it for later use */
	  string=Mols_Residue_GetCcpCode(residue);
	  basetype=malloc(strlen(ApiString_Get(string))+2);
	  strcpy(basetype,ApiString_Get(string));
	  ApiObject_Free(string);
	  /* "Repeat" chemcomps are just a marker to indicate the linkage in respective ends of a repeating unit */
	  if(strcasecmp(basetype,"repeat")==0)
	    {
	      continue;
	    }

	  /* Special case for protein residues (in glycans). Use a special function for that */
	  string=Mols_Residue_GetMolType(residue);
	  if(strcmp(ApiString_Get(string),"protein")==0)
	    {
	      if(!CC_ProteinToBU_Struct(&residue, BU_Struct))
		{
		  projectValid=0;
		  free(basetype);
		  ApiObject_Free(string);
		  ApiObject_Free(chain);
		  ApiObject_Free(chains);
		  return 0;
		}
	      continue;
	    }
	  ApiObject_Free(string);

	  /* The descriptor tells the stereochemistry */
	  string=Mols_Residue_GetDescriptor(residue);
	  strcpy(descriptor,ApiString_Get(string));
	  ApiObject_Free(string);

	  if(descriptor[7]=='1')
	    {
	      strcpy(stereo,"a-");
	    }
	  else if(descriptor[7]=='2')
	    {
	      strcpy(stereo,"b-");
	    }
	  else
	    {
	      printf("Cannot determine stereochemistry. Aborting. Line %d.\n", __LINE__);
	      free(basetype);
	      ApiObject_Free(chain);
	      ApiObject_Free(chains);
	      return 0;
	    }

	  /* Translate the CcpCode into a CASPER interpretable format */
	  basetype_tok=strtok(basetype,"-");
	  type=strtok(NULL,"-");
	  ringAndMods=strtok(NULL,":");
	  if(strlen(ringAndMods)>4)
	    {
	      mods=ringAndMods+4;
	    }
	  else
	    {
	      mods=0;
	    }
	  substs=strtok(NULL,"");
	  if(!substs)
	    {
	      substs=alloca(2); /* This way it doesn't have to be freed later so it doesn't matter
				   if substs is pointing at specifically allocated memory or not */
	      *substs=0;
	    }
	  /*      while(subst)
		  {
		  strcpy(substProperties[nsubsts].Node.Name, subst);
		  strcpy(substProperties[nsubsts].substituent, subst);
		  substProperties[nsubsts].position[0]=atoi(&subst[1]);
		  nsubsts++;
		  subst=strtok(NULL,":");
		  }*/
	  /*      if(substone)
		  {
		  substonepos=atoi(substone[1]);
		  nsubsts=1;
		  substtwo=strtok(NULL,":");
		  if(substtwo)
		  {
		  substtwopos=atoi(substtwo[1]);
		  nsubsts=2;
		  substthree=strtok(NULL,":");
		  if(substthree)
		  {
		  substthreepos=atoi(substthree[1]);
		  nsubsts=3;
		  }
		  }
		  }*/
	  mod=strtok(mods,"-");
	  nmods=0;
	  while(mod)
	    {
	      modProperties[nmods].position=atoi(&mod[0]);
	      strcpy(modProperties[nmods].modifier, mod+1);
	      sprintf(modProperties[nmods].Node.Name, "%s%d", modProperties[nmods].modifier, modProperties[nmods].position);
	      nmods++;
	      mod=strtok(NULL,"-");
	    }
	  sprintf(residuename,"%s%s",stereo, basetype_tok);
	  /*      if(substone)
		  {
		  strtok(substone,"_");
		  substone=strtok(NULL,"");
		  if(substtwo)
		  {
		  strtok(substtwo,"_");
		  substtwo=strtok(NULL,"");
		  if(strcmp(substone,substtwo)>=1)
		  {
		  strcpy(temp,substone);
		  strcpy(substone,substtwo);
		  strcpy(substtwo,temp);
		  }
		  if(substthree)
		  {
		  strtok(substthree,"_");
		  substthree=strtok(NULL,"");
		  if(strcmp(substtwo,substthree)>=1)
		  {
		  strcpy(temp,substtwo);
		  strcpy(substtwo,substthree);
		  strcpy(substthree,temp);
		  if(strcmp(substone,substtwo)>=1)
		  {
		  strcpy(temp,substone);
		  strcpy(substone,substtwo);
		  strcpy(substtwo,temp);
		  }
		  }
		  }
		  }
		  }*/
	  /*      if(substone)
		  {
		  residuename=strcat(residuename,substone);
		  if(substtwo)
		  {
		  residuename=strcat(residuename,substtwo);
		  if(substthree)
		  {
		  residuename=strcat(residuename,substthree);
		  }
		  }
		  }*/
	  /* Find the residue in CASPER's list of residues. */
	  for(res=(struct RE_Residue *)ResidueList.Head.Succ;
	      res->Node.Succ!=NULL;
	      res=(struct RE_Residue *)res->Node.Succ)
	    {
	      flag=0;
	      /* Check if this is the right base type */
	      if(strcasecmp(residuename,res->CTname)==0)
		{
		  /* Check that the super class is correct */
		  if(strcasecmp(type,res->Shifts.Type->CTsuperclass)==0)
		    {
		      if(ListLen(&res->Shifts.Type->CTmods)==nmods)
			{
			  flag=1;
			  if(strcasecmp(res->Shifts.Type->ccpSub, substs)!=0)
			    {
			      flag=0;
			    }
			  if(flag==1)
			    {
			      for(j=0;j<nmods;j++)
				{
				  if(!FindNode(&res->Shifts.Type->CTmods.Head,
					       modProperties[j].Node.Name))
				    {
				      flag=0;
				    }
				}
			    }
			  if(flag==1)
			    {
			      cnt++;

			      nr=Mols_Residue_GetSeqCode(residue);
			      name[0]=ApiInteger_Get(nr) + 96;
			      name[1]=0;
			      BU_Unit=(struct BU_Unit*)ME_CreateNode(&UnitMethod, &(BU_Struct->Units), name);
			      if (BU_Unit==NULL) Error(PA_ERR_FATAL,"Out of memory");
			      switch (res->JCH)
				{
				case RE_SMALL:	BU_Struct->JCH[RE_SMALL]++;
				  break;
				case RE_MEDIUM:	BU_Struct->JCH[RE_MEDIUM]++;
				  break;
				case RE_LARGE:	BU_Struct->JCH[RE_LARGE]++;
				  break;
				};
			      switch (res->JHH)
				{
				case RE_SMALL:	BU_Struct->JHH[RE_SMALL]++;
				  break;
				case RE_MEDIUM:	BU_Struct->JHH[RE_MEDIUM]++;
				  break;
				case RE_LARGE:	BU_Struct->JHH[RE_LARGE]++;
				  break;
				};
			      SH_RAW_COPY(&(res->Shifts), &(BU_Unit->Shifts));  /* Copy shifts from the residue to the new unit. */
			      BU_Unit->Residue=res;
			      BU_Unit->Shifts.Type=res->Shifts.Type;
			      BU_Unit->Error=res->Error;
			      BU_Unit->CcpnUnitNr=ApiInteger_Get(nr);
			      ApiObject_Free(nr);
			      break;
			    }
			}
		    }
		}
	    }
	  free(basetype);
	  if(res->Node.Succ==NULL)
	    {
	      printf("Could not add residue. Line %d.\n", __LINE__);
	      ApiObject_Free(chain);
	      ApiObject_Free(chains);
	      return 0;
	    }
	}
      allMolResLinks=Mole_Molecule_GetMolResLinks(molecule);
      if(!allMolResLinks)
	{
	  printf("No linkages found - aborting. Line %d.\n", __LINE__);
	  ApiObject_Free(chain);
	  ApiObject_Free(chains);
	  return 0;
	}
      molResLinkIterator=ApiSet_Iterator(allMolResLinks);
      while((molResLink=ApiSetIterator_Next(molResLinkIterator)))
	{
	  allMolResLinkEnds=Mole_MolResLink_GetMolResLinkEnds(molResLink);
	  if(!allMolResLinkEnds || ApiSet_Len(allMolResLinkEnds)!=2)
	    {
	      printf("Needs two ends to a link - aborting. Line %d.\n", __LINE__);
	      ApiObject_Free(molResLink);
	      ApiObject_Free(allMolResLinks);
	      ApiObject_Free(chain);
	      ApiObject_Free(chains);
	      return 0;
	    }
	  fromResPos=-1;
	  toResPos=-1;
	  repeatFlag=0;
	  for(j=0;j<2;j++)
	    {
	      molResLinkEnd=ApiSet_Get(allMolResLinkEnds, j);
	      residue=Mole_MolResLinkEnd_GetMolResidue(molResLinkEnd);
	      string=Mols_Residue_GetCcpCode(residue);

	      /* If this is either end of a repeating unit handle it in a special way */
	      if(strcmp(ApiString_Get(string), "repeat")==0)
		{
		  repeatFlag=1;
		  ApiObject_Free(molResLinkEnd);
		  if(j==0)
		    {
		      molResLinkEnd=ApiSet_Get(allMolResLinkEnds, 1);
		      j=1;
		    }
		  else
		    {
		      molResLinkEnd=ApiSet_Get(allMolResLinkEnds, 0);
		    }
		  ApiObject_Free(residue);
		  residue=Mole_MolResLinkEnd_GetMolResidue(molResLinkEnd);
		}

	      ApiObject_Free(string);
	      string=Mole_MolResLinkEnd_GetLinkCode(molResLinkEnd);
	      strcpy(linkCode,ApiString_Get(string));
	      ApiObject_Free(string);
	      nr=Mols_Residue_GetSeqCode(residue);
	      ApiObject_Free(residue);
	      BU_Unit=BU_FindCcpnUnit((struct Node *)&BU_Struct->Units, ApiInteger_Get(nr));
	      ApiObject_Free(nr);
	      if(!BU_Unit)
		{
		  printf("Unit not found. Line %d.\n", __LINE__);
		  ApiObject_Free(molResLink);
		  ApiObject_Free(allMolResLinks);
		  ApiObject_Free(molResLinkEnd);
		  ApiObject_Free(allMolResLinkEnds);
		  ApiObject_Free(chain);
		  ApiObject_Free(chains);
		  return 0;
		}
	      /* Special case: Asn */
	      if(strncmp(linkCode,"ND",2)==0)
		{
		  currResPos=3;
		}
	      /* Special case: Ser, Thr */
	      else if(strncmp(linkCode,"OG",2)==0)
		{
		  currResPos=2;
		}
	      /* Normal cases */
	      else
		{
		  /* Ignore how the linkage is specified - just look for the atom. */
		  if(linkCode[0]=='O')
		    {
		      currResPos=TY_FindAtom(BU_Unit->Shifts.Type, linkCode+1);
		    }
		  else
		    {
		      currResPos=TY_FindAtom(BU_Unit->Shifts.Type, linkCode);
		    }
		}
	      if(currResPos==BU_Unit->Shifts.Type->Anomeric)
		{
		  if(!(BU_Unit->Shifts.Type->Atom[currResPos].Type & TY_FREE_POS))
		    {
		      printf("Anomeric position cannot be substituted. Line %d.\n", __LINE__);
		      ApiObject_Free(molResLink);
		      ApiObject_Free(allMolResLinks);
		      ApiObject_Free(molResLinkEnd);
		      ApiObject_Free(allMolResLinkEnds);
		      ApiObject_Free(chain);
		      ApiObject_Free(chains);
		      return 0;
		    }
		  if(fromResPos==-1)
		    {
		      if(repeatFlag)
			{
			  repeatFromUnit=BU_Unit;
			  repeatFromPos=currResPos;
			}
		      else
			{
			  fromResPos=currResPos;
			  linkDesc.Unit[DE_NONRED]=BU_Unit;
			  linkDesc.Atom[DE_NONRED]=fromResPos;
			}
		    }
		  else
		    {
		      if(repeatFlag)
			{
			  repeatToUnit=BU_Unit;
			  repeatToPos=currResPos;
			}
		      else
			{
			  toResPos=currResPos;
			  linkDesc.Unit[DE_REDUCING]=BU_Unit;
			  linkDesc.Atom[DE_REDUCING]=toResPos;
			}
		    }
		}
	      else
		{
		  if(!repeatFlag && toResPos!=-1)
		    {
		      printf("Incorrect linkage - no anomeric position in linkage. Line %d.\n", __LINE__);
		      ApiObject_Free(molResLink);
		      ApiObject_Free(allMolResLinks);
		      ApiObject_Free(molResLinkEnd);
		      ApiObject_Free(allMolResLinkEnds);
		      ApiObject_Free(chain);
		      ApiObject_Free(chains);
		      return 0;
		    }
		  if(!(BU_Unit->Shifts.Type->Atom[currResPos].Type & TY_FREE_POS))
		    {
		      /* Some extra comparisons for proteins in glycans */
		      if(strcasecmp(BU_Unit->Shifts.Type->Node.Name,"Asn")==0 &&
			 BU_Unit->Shifts.Type->Atom[3].Type & TY_FREE_POS)
			{
			  currResPos=3;
			}
		      else if((strcasecmp(BU_Unit->Shifts.Type->Node.Name,"Ser")==0 ||
			       strcasecmp(BU_Unit->Shifts.Type->Node.Name,"Thr")==0) &&
			      BU_Unit->Shifts.Type->Atom[2].Type & TY_FREE_POS)
			{
			  currResPos=2;
			}
		      else
			{
			  printf("%d, %s",currResPos,name);
			  printf("Reducing end cannot be substituted. Line %d.\n", __LINE__);
			  ApiObject_Free(molResLink);
			  ApiObject_Free(allMolResLinks);
			  ApiObject_Free(molResLinkEnd);
			  ApiObject_Free(allMolResLinkEnds);
			  ApiObject_Free(chain);
			  ApiObject_Free(chains);
			  return 0;
			}
		    }
		  if(repeatFlag)
		    {
		      repeatToUnit=BU_Unit;
		      repeatToPos=currResPos;
		    }
		  else
		    {
		      toResPos=currResPos;
		      linkDesc.Unit[DE_REDUCING]=BU_Unit;
		      linkDesc.Atom[DE_REDUCING]=toResPos;
		    }
		}
	      ApiObject_Free(molResLinkEnd);
	    }
	  ApiObject_Free(allMolResLinkEnds);
	  if(!repeatFlag)
	    {
	      linkDesc.Unit[DE_NONRED]->Subst[linkDesc.Atom[DE_NONRED]]=linkDesc.Unit[DE_REDUCING];
	      linkDesc.Unit[DE_REDUCING]->Subst[linkDesc.Atom[DE_REDUCING]]=linkDesc.Unit[DE_NONRED];
	      linkDesc.Unit[DE_NONRED]->Position=linkDesc.Atom[DE_REDUCING];
	      /* If both positions are anomeric, e.g. aDGlc(1->1)aDGlc Position has to be specified in both units. */
	      if(linkDesc.Atom[DE_REDUCING]==BU_Unit->Shifts.Type->Anomeric)
		{
		  linkDesc.Unit[DE_REDUCING]->Position=linkDesc.Atom[DE_NONRED];
		}
	    }
	  ApiObject_Free(molResLink);
	  /*      linkDesc.Unit[DE_REDUCING]->Shifts.Type->
		  Atom[linkDesc.Atom[DE_REDUCING]].Type ^= TY_FREE_POS;*/
	  /*      if(CA_SimDisacch(linkDesc.Unit[DE_NONRED], linkDesc.Atom[DE_NONRED],
		  linkDesc.Atom[DE_REDUCING], 
		  linkDesc.Unit[DE_REDUCING])!=PA_ERR_OK)
		  {
		  Error(PA_ERR_FAIL,"No data for this bond\n");
		  }*/
	}
      if(repeatFromUnit && repeatToUnit)
	{
	  repeatFromUnit->Subst[repeatFromPos]=repeatToUnit;
	  repeatFromUnit->Position=repeatToPos;
	  repeatToUnit->Subst[repeatToPos]=repeatFromUnit;
	}
      ApiObject_Free(allMolResLinks);
      ApiObject_Free(allResidues);
      ApiObject_Free(chain);
    }

  ApiObject_Free(chains);

  return cnt;
}

/* Adds a protein residue to BU_Struct. */
int CC_ProteinToBU_Struct(Mols_Residue *residue, struct BU_Struct *BU_Struct)
{
  struct RE_Residue *res;
  struct BU_Unit *unit;
  ApiString string;
  ApiInteger intValue;
  char *basetype, nr[8];
  int j, CcpnUnitNr;

  string=Mols_Residue_GetCcpCode(*residue);
  basetype=malloc(strlen(ApiString_Get(string))+2);
  ApiObject_Free(string);
  string=Mols_Residue_GetCcpCode(*residue);
  strcpy(basetype,ApiString_Get(string));
  ApiObject_Free(string);
  
  res=(struct RE_Residue *)FindNode((struct Node *)&ResidueList, basetype);
  if(res==NULL)
    {
      printf("Warning: Could not find residue %s. Line %d.\n", basetype, __LINE__);
      free(basetype);
      return 1;
    }
  free(basetype);
  intValue=Mols_Residue_GetSeqCode(*residue);
  CcpnUnitNr=ApiInteger_Get(intValue);
  sprintf(nr,"%d",CcpnUnitNr);
  ApiObject_Free(intValue);
  for(j=0;j<strlen(nr);j++)
    {
      nr[j]+=48;
    }
  unit=(struct BU_Unit*)ME_CreateNode(&UnitMethod, &(BU_Struct->Units), nr);
  if (unit==NULL)
    {
      printf("Could not add unit. Line %d.\n", __LINE__);
      return 0;
    }
  SH_RAW_COPY(&(res->Shifts), &(unit->Shifts));  /* Copy shifts from the residue to the new unit. */
  unit->Residue=res;
  unit->Shifts.Type=res->Shifts.Type;
  unit->Error=res->Error;
  unit->CcpnUnitNr=CcpnUnitNr;

  return 1;
}

/* This function makes a new CCPN molSystem with a new chain and molecules.
   The molecules are created from a BU_Struct containing a structure described
   in the format used in CASPER. The created Mols_MolSystem is returned.*/
Mols_MolSystem CC_MakeMoleculeFromBU_Struct(Impl_MemopsRoot *ccpnProject, struct BU_Struct *inp, char *name)
{
  Mols_MolSystem molSystem;
  Mols_Chain chain=0;
  Mole_Molecule molecule;
  Mole_MolResidue residue, toResidue, repeatResidue;
  Chem_ChemComp currChemComp;
  ApiMap objectParameters, searchMap;
  ApiList chains;
  ApiString ApiStrMolType, ApiStrCcpCode;
  ApiInteger unitNr;
  struct BU_Unit *unit, *toUnit;
  struct TY_CTmodifier *ctmod;
  char ctname[RE_INFO_LEN], *ccpCode, stereo[16], resLinking[32];
  char descriptor[16], fromPos[8], toPos[8], *temp;
  int i, j, n=0, closingPos, linked, anomerPos;

  objectParameters=ApiMap_New();
  ApiMap_SetString(objectParameters, "code", name);

  /* If a molSystem with that name already exists use it */
  molSystem=Impl_MemopsRoot_FindFirstMolSystem(*ccpnProject,objectParameters);
  if(!molSystem || ApiObject_IsNone(molSystem))
    {
      molSystem=Impl_MemopsRoot_GetCurrentMolSystem(*ccpnProject);
      /*if(!molSystem || ApiObject_IsNone(molSystem))
      {
	  molSystem=Impl_MemopsRoot_NewMolSystem(*ccpnProject, objectParameters);
	  if(!molSystem)
	    {
	      printf("Cannot find or create molSystem. Line %d\n", __LINE__);
	      projectValid=0;
	      ApiObject_Free(objectParameters);
	      return 0;
	    }
	    }*/
    }
  ApiObject_Free(objectParameters);
  
  /* If the input structure has a chain specified clear this chain. Otherwise it will be assigned a new chain
     later */
  if(strlen(inp->CcpnChainCode)>0)
    {
      searchMap=ApiMap_New();
      ApiMap_SetString(searchMap, "code", inp->CcpnChainCode);
      chain=Mols_MolSystem_FindFirstChain(molSystem, searchMap);
      ApiObject_Free(searchMap);
      if(chain && !ApiObject_IsNone(chain))
	{
	  molecule=Mols_Chain_GetMolecule(chain);	  
	  if(molecule && !ApiObject_IsNone(molecule))
	    {
	      ApiObject_Free(molecule);
	      ApiObject_Free(chain);
	      return molSystem;
	    }
	  else
	    {
	      Impl_DataObject_Delete(chain);
	      ApiObject_Free(chain);	      
	    }
	}
    }
  objectParameters=ApiMap_New();
  ApiMap_SetString(objectParameters, "name", name);
  molecule=Impl_MemopsRoot_NewMolecule(*ccpnProject, objectParameters);
      
  ApiObject_Free(objectParameters);

  if(!molecule)
    {
      printRaisedException();
      printf("Cannot find molecule or create new molecule. Line %d.\n", __LINE__);
      projectValid=0;
      return 0;
    }

  for(unit=(struct BU_Unit *)inp->Units.Head.Succ; unit->Node.Succ!=NULL;
      unit=(struct BU_Unit *)unit->Node.Succ)
    {
      linked=0;
      n++;

      /* If this unit is the amino acid part of a glycan treat it in a special
	 way. */
      if(strcmp(unit->Residue->Shifts.Type->CTsuperclass, "prot")==0)
	{
	  if(!CC_ProteinToCCPNMolecule(ccpnProject,&molecule, unit, n))
	    {
	      printf("Cannot handle amino acid part of glycan properly. Line %d.\n", __LINE__);
	      ApiObject_Free(molecule);
	      projectValid=0;
	      return 0;
	    }

	  continue;
	}
      sprintf(stereo,"stereo_%d:C%d",unit->Residue->Node.Name[0]-96,
	      unit->Residue->Shifts.Type->Anomeric+1);
      strcpy(ctname, unit->Residue->CTname);
      temp=strtok(ctname,"-");
      ccpCode=strtok(NULL,"");
      if(!ccpCode)
	{
	  printf("Cannot generate ccpCode. Line %d.\n", __LINE__);
	  ApiObject_Free(molecule);
	  projectValid=0;
	  return 0;
	}
      closingPos=0;
      for(i=unit->Residue->Shifts.Type->HeavyCnt-1;i>=0;i--)
	{
	  if(unit->Residue->Shifts.Type->Atom[i].Type&TY_CLOSING)
	    {
	      closingPos=i+1;
	      i=0;
	    }
	}
      /* Print basetype, superclass and ring closing positions,
	 e.g. dglc-hex-1-5 */
      if(strcmp(unit->Residue->Shifts.Type->CTsuperclass, "subst")!=0)
	{
	  sprintf(ccpCode,"%s-%s-%d-%d",ccpCode,
		  unit->Residue->Shifts.Type->CTsuperclass,
		  unit->Residue->Shifts.Type->Anomeric+1,
		  closingPos);
	}
      else
	{
	  /*	  projectValid=0;
		  return 0;*/
	  printf("DEBUG: Using substituent. Not yet implemented. Line %d.\n", __LINE__);
	  continue;
	}
      /* Add modifiers, e.g. d, keto, a etc. */
      for(ctmod=(struct TY_CTmodifier *)
	    unit->Residue->Shifts.Type->CTmods.Head.Succ;
	  ctmod->Node.Succ!=NULL;
	  ctmod=(struct TY_CTmodifier *)ctmod->Node.Succ)
	{
	  sprintf(ccpCode,"%s-%d%s",ccpCode,ctmod->position,ctmod->modifier);
	}
      /* If there are substituents (e.g. OMe, NAc) add them */
      if(strlen(unit->Residue->Shifts.Type->ccpSub)>0)
	{
	  sprintf(ccpCode,"%s:%s",ccpCode,unit->Residue->Shifts.Type->ccpSub);
	}
      /*      printf("CcpCode: %s\n",ccpCode); */

      ApiStrMolType=ApiString_New("carbohydrate");
      ApiStrCcpCode=ApiString_New(ccpCode);
      currChemComp=Impl_MemopsRoot_FindFirstChemComp_keyval2(*ccpnProject,"molType",ApiStrMolType,"ccpCode",ApiStrCcpCode);
      ApiObject_Free(ApiStrMolType);
      ApiObject_Free(ApiStrCcpCode);

      if(!currChemComp || ApiObject_IsNone(currChemComp))
	{
	  projectValid=0;
	  printRaisedException();
	  printf("Cannot find chemComp %s. Line %d.\n", ccpCode, __LINE__);
	  ApiObject_Free(molecule);
	  return 0;
	}

      anomerPos=BU_ANOMER_POS(unit);
      /* If the anomeric position is linked add a linking descriptor string.
	 There is also a special case with unlinked, but free anomeric positions - they have to be specified
	 as linked otherwise CCPN wants it to be neutral. CASPER does not deal with neutral stereochemistry. */
      if(unit->Position!=-1 || (anomerPos!=-1 && unit->Residue->Shifts.Type->Atom[anomerPos].Type & TY_FREE_POS))
	{
	  sprintf(resLinking,"link:C%d_%d",
		  unit->Residue->Shifts.Type->Anomeric+1,
		  unit->Residue->Node.Name[0]-96);
	  linked=1;
	}
      else
	{
	  sprintf(resLinking,"link:");
	}
      for (j=0; j<unit->Residue->Shifts.Type->HeavyCnt; j++)	/* look for all linkages */
	{
	  if (j==unit->Residue->Shifts.Type->Anomeric)
	    {
	      continue;
	    }
	  if (unit->Subst[j]!=NULL)
	    {
	      if(linked!=0)
		{
		  sprintf(resLinking,"%s,",resLinking);
		}
	      sprintf(resLinking,"%sO%d",resLinking,j+1);
	      linked=1;
	    }
	}
      if(linked==0)
	{
	  strcpy(resLinking,"none");
	}
      if(linked!=0 || anomerPos!=-1)
	{
	  strcpy(descriptor,stereo);
	}
      else
	{
	  strcpy(descriptor,"neutral");
	}
      if(!Mole_Molecule_CheckAllValid(molecule,ApiBoolean_False()))
	{
	  printRaisedException();
	  return 0;
	}
      residue=createMolResidue(molecule,n,currChemComp,resLinking,descriptor);
      if(!residue || ApiObject_IsNone(residue))
	{
	  printf("Cannot create residue. Line %d.\n", __LINE__);
	  ApiObject_Free(molecule);
	  ApiObject_Free(currChemComp);
	  projectValid=0;
	  return 0;	  
	}
      unitNr=Mole_MolResidue_GetSeqCode(residue);
      unit->CcpnUnitNr=ApiInteger_Get(unitNr);
      ApiObject_Free(currChemComp);
      ApiObject_Free(unitNr);
      ApiObject_Free(residue);
    }
  /* Create the linkages too */
  for(toUnit=(struct BU_Unit *)inp->Units.Head.Succ; toUnit->Node.Succ!=NULL;
      toUnit=(struct BU_Unit *)toUnit->Node.Succ)
    {
      for(j=0;j<toUnit->Residue->Shifts.Type->HeavyCnt;j++)
	{
	  if (j==toUnit->Residue->Shifts.Type->Anomeric)
	    {
	      continue;
	    }
	  if(toUnit->Subst[j]!=NULL)
	    {
	      unit=(struct BU_Unit *)inp->Units.Head.Succ;
	      while(unit->Node.Succ!=NULL &&
		    strcasecmp(unit->Node.Name,toUnit->Subst[j]->Node.Name)!=0)
		{
		  unit=(struct BU_Unit *)unit->Node.Succ;
		}
	      sprintf(fromPos,"C%d_%d",
		      unit->Residue->Shifts.Type->Anomeric+1,
		      unit->Residue->Node.Name[0]-96);
	      /* Handle the amino acids */
	      if(strcmp(toUnit->Residue->Node.Name,"Asn")==0)
		{
		  sprintf(toPos,"ND2");
		}
	      if(strcmp(toUnit->Residue->Node.Name,"Ser")==0)
		{
		  sprintf(toPos,"OG");
		}
	      if(strcmp(toUnit->Residue->Node.Name,"Thr")==0)
		{
		  sprintf(toPos,"OG1");
		}
	      else
		{
		  sprintf(toPos,"O%d",j+1);
		}
	      searchMap=ApiMap_New();
	      ApiMap_SetInt(searchMap,"seqCode",unit->CcpnUnitNr);
	      residue = Mole_Molecule_FindFirstMolResidue(molecule,searchMap);
	      ApiObject_Free(searchMap);
	      if(!residue || ApiObject_IsNone(residue))
		{
		  printf("Residue not found. Line %d.\n", __LINE__);
		  ApiObject_Free(molecule);
		  projectValid=0;
		  return 0;
		}
	      searchMap=ApiMap_New();
	      ApiMap_SetInt(searchMap,"seqCode",toUnit->CcpnUnitNr);
	      toResidue = Mole_Molecule_FindFirstMolResidue(molecule,searchMap);
	      ApiObject_Free(searchMap);
	      if(!toResidue)
		{
		  printf("Residue not found. Line %d.\n", __LINE__);
		  ApiObject_Free(molecule);
		  projectValid=0;
		  return 0;
		}
	      /*	      printf("%s %s %s %s\n",ApiString_Get(Mole_MolResidue_GetCcpCode(residue)), fromPos, ApiString_Get(Mole_MolResidue_GetCcpCode(toResidue)), toPos);*/
	      /* Handle repeats */
	      if(strcmp(unit->Node.Name,"a")==0)
		{
		  /* Get the "repeat" chemComp */
		  ApiStrMolType=ApiString_New("carbohydrate");
		  ApiStrCcpCode=ApiString_New("repeat");
		  currChemComp=Impl_MemopsRoot_FindFirstChemComp_keyval2(*ccpnProject,"molType",ApiStrMolType,"ccpCode",ApiStrCcpCode);
		  ApiObject_Free(ApiStrMolType);
		  ApiObject_Free(ApiStrCcpCode);
		  if(!currChemComp || ApiObject_IsNone(currChemComp))
		    {
		      projectValid=0;
		      printRaisedException();
		      printf("Cannot find chemComp %s. Line %d.\n", "repeat", __LINE__);
		      ApiObject_Free(molecule);
		      return 0;
		    }

		  /* Create the first repeat MolResidue */
		  sprintf(resLinking,"link:O%d", j+1);
		  strcpy(descriptor,"neutral");
		  repeatResidue=createMolResidue(molecule,++n,currChemComp,resLinking,descriptor);
		  if(!repeatResidue)
		    {
		      printf("Cannot create repeating MolResidue. Line %d.\n", __LINE__);
		      ApiObject_Free(molecule);
		      ApiObject_Free(currChemComp);
		      projectValid=0;
		      return 0;	  
		    }
		  /* Create link to "repeat" in "reducing" end */
		  setMolResLink(residue,fromPos,repeatResidue,toPos);
		  ApiObject_Free(repeatResidue);

		  /* Create the second repeat MolResidue */
		  sprintf(resLinking,"link:C%d", unit->Residue->Shifts.Type->Anomeric+1);
		  strcpy(descriptor,"neutral");
		  repeatResidue=createMolResidue(molecule,++n,currChemComp,resLinking,descriptor);
		  ApiObject_Free(currChemComp);
		  if(!repeatResidue)
		    {
		      printf("Cannot create repeating MolResidue. Line %d.\n", __LINE__);
		      ApiObject_Free(molecule);
		      projectValid=0;
		      return 0;	  
		    }
		  /* Since the "repeat" is neutral stereochemical info has
		     to be removed from the linkage. */
		  temp=strstr(fromPos,"_");
		  if(temp)
		    {
		      *temp=0;
		    }
		  /* Create link from "repeat" in "non-reducing" end */
		  setMolResLink(repeatResidue,fromPos,toResidue,toPos);
		  ApiObject_Free(repeatResidue);
		}
	      else
		{
		  setMolResLink(residue,fromPos,toResidue,toPos);
		}
	      ApiObject_Free(residue);
	      ApiObject_Free(toResidue);
	    }
	}
    }
  /*  if(!Impl_MemopsRoot_CheckAllValid(*ccpnProject,ApiBoolean_False()))
    {
      printf("Project not valid.\n");
      projectValid=0;
      return 0;
    }
  */

  if(!molSystem||ApiObject_IsNone(molSystem))
    {
      objectParameters=ApiMap_New();
      ApiMap_SetString(objectParameters,"code",name);
      ApiMap_SetString(objectParameters,"name",name);
      molSystem=Mols_MolSystem_Init(*ccpnProject,objectParameters);

      ApiObject_Free(objectParameters);
    }

  if(!molSystem||ApiObject_IsNone(molSystem))
    {
      /*      printf("Molecular system could not be created.\n");*/
      molSystem = Impl_MemopsRoot_GetCurrentMolSystem(*ccpnProject);

      if (!molSystem||ApiObject_IsNone(molSystem))
	{
	  ApiSet allMolSystems = Impl_MemopsRoot_GetMolSystems(*ccpnProject);
	  int allMolSystemsLen = ApiSet_Len(allMolSystems);
	  /*	  printf("%d MolSystems.\n",allMolSystemsLen);*/
	  
	  if (allMolSystemsLen)
	    {
	      molSystem = ApiSet_Get(allMolSystems,0);
	    }
	  
	  else
	    {
	      printRaisedException();
	      printf("No molecular system found - aborting. Line %d.\n", __LINE__);
	      ApiObject_Free(molecule);
	      projectValid=0;
	      return 0;
	    }
	  ApiObject_Free(allMolSystems);
	}
    }
  if(strlen(inp->CcpnChainCode)<=0)
    {
      chains=Mols_MolSystem_SortedChains(molSystem);
      if(ApiList_Len(chains)>0)
	{
	  chain=ApiList_Get(chains, ApiList_Len(chains)-1);
	  strcpy(inp->CcpnChainCode, ApiString_Get(Mols_Chain_GetCode(chain)));
	  inp->CcpnChainCode[strlen(inp->CcpnChainCode)-1]++;
	  ApiObject_Free(chain);
	}
      else
	{
	  strcpy(inp->CcpnChainCode, "A");
	}
      ApiObject_Free(chains);
    }
  objectParameters=ApiMap_New();
  ApiMap_SetString(objectParameters, "code", inp->CcpnChainCode);
  ApiMap_SetItem(objectParameters,"molecule",molecule);
  chain=Mols_Chain_Init(molSystem,objectParameters);
  ApiObject_Free(objectParameters);

  if(!chain)
    {
      printRaisedException();
      printf("Chain could not be created. Line %d.\n", __LINE__);
      ApiObject_Free(molecule);
      projectValid=0;
      return 0;
    }

  Impl_MemopsRoot_SetCurrentMolSystem(*ccpnProject, molSystem);
  Impl_MemopsRoot_SetCurrentMolecule(*ccpnProject, molecule);

  ApiObject_Free(molecule);
  ApiObject_Free(chain);

  /*  if(!Impl_MemopsRoot_CheckAllValid(*ccpnProject, ApiBoolean_False()))
    {
      printRaisedException();
      printf("MemopsRoot not valid. Line %d.\n", __LINE__);
      projectValid=0;
    }
  */
  return molSystem;
}

int CC_ProteinToCCPNMolecule(Impl_MemopsRoot *ccpnProject, Mole_Molecule *molecule, struct BU_Unit *unit, int n)
{
  ApiString molType, ccpCode;
  ApiInteger CcpnUnitNr;
  Chem_ChemComp currChemComp;
  Mole_MolResidue residue;
  char desc[16], resLinking[32], name[16];
  int i;

  strcpy(name,unit->Residue->Node.Name);
  
  for(i=0;i<strlen(name);i++)
    {
      name[i]=toupper(name[i]);
    }

  molType=ApiString_New("protein");
  ccpCode=ApiString_New(name);

  currChemComp=Impl_MemopsRoot_FindFirstChemComp_keyval2(*ccpnProject,"moltype",molType,"ccpCode",ccpCode);
  ApiObject_Free(ccpCode);
  ApiObject_Free(molType);

  if(!currChemComp || ApiObject_IsNone(currChemComp))
    {
      printf("Cannot find chemComp. Line %d.\n", __LINE__);
      return 0;
    }

  if(unit->Subst[2]!=NULL || unit->Subst[3]!=NULL)
    {
      if(strcmp(unit->Residue->Node.Name,"Asn")==0)
	{
	  sprintf(desc,"link:ND2");
	}
      else if(strcmp(unit->Residue->Node.Name,"Ser")==0)
	{
	  sprintf(desc,"link:OG");
	}
      else if(strcmp(unit->Residue->Node.Name,"Thr")==0)
	{
	  sprintf(desc,"link:OG1");
	}
      /* Is it middle for all three?? */
      sprintf(resLinking,"middle");
    }
  else
    {
      sprintf(resLinking,"none");
    }

  residue=createMolResidue(*molecule,n,currChemComp,resLinking,desc);
  if(!residue || ApiObject_IsNone(residue))
    {
      printf("Cannot create protein residue. Line %d.\n", __LINE__);
      return 0;
    }
  CcpnUnitNr=Mole_MolResidue_GetSeqCode(residue);
  unit->CcpnUnitNr=ApiInteger_Get(CcpnUnitNr);
  ApiObject_Free(CcpnUnitNr);
  ApiObject_Free(currChemComp);
  ApiObject_Free(residue);
  

  return 1;
}


int CC_CreateResonanceGroups(Impl_MemopsRoot *ccpnProject, Nmr_NmrProject *nmrProject,
			     Mols_MolSystem molSystem, char *chainCode)
{
  ApiMap objectParameters;
  ApiSet chains, residues, resonanceGroups, analProjs, resonances;
  ApiString string;
  ApiInteger seqId;
  ApiSetIterator resonanceIterator, resonanceGroupIterator, residueIterator;
  Anal_AnalysisProject analProj;
  Mols_Chain chain;
  Mols_Residue residue;
  Nmr_Resonance resonance;
  int i;
  char ccpCode[32], name[128], molType[64];
  Nmr_ResonanceGroup resonanceGroup;

  analProj=Impl_MemopsRoot_GetCurrentAnalysisProject(*ccpnProject);
  if(!analProj || ApiObject_IsNone(analProj))
    {
      analProj=Nmr_NmrProject_GetAnalysisProject(*nmrProject);
      if(!analProj || ApiObject_IsNone(analProj))
	{
	  analProjs=Impl_MemopsRoot_GetAnalysisProjects(*ccpnProject);
	  if(ApiSet_Len(analProjs)>0)
	    {
	      analProj=ApiSet_Get(analProjs,0);
	      Impl_MemopsRoot_SetCurrentAnalysisProject(*ccpnProject, analProj);
	    }
	  ApiObject_Free(analProjs);
	}
    }
  if(!analProj || ApiObject_IsNone(analProj))
    {
      string=Nmr_NmrProject_GetName(*nmrProject);
      analProj=Anal_AnalysisProject_Init_reqd(*ccpnProject, ApiString_Get(string), *nmrProject);
      ApiObject_Free(string);
      if(!analProj || ApiObject_IsNone(analProj))
	{
	  printf("Cannot create Analysis Project. Line %d.\n", __LINE__);
	  printRaisedException();
	  projectValid=0;
	  return 0;	  
	}
    }

  objectParameters=ApiMap_New();
  if(strlen(chainCode)>0)
    {
      ApiMap_SetString(objectParameters, "code", chainCode);
    }
  else
    {
      ApiMap_SetString(objectParameters, "code", "A");
    }
  chain=Mols_MolSystem_FindFirstChain(molSystem, objectParameters);
  ApiObject_Free(objectParameters);

  if(!chain || ApiObject_IsNone(chain))
    {
      chains=Mols_MolSystem_GetChains(molSystem);

      if(chains && ApiSet_Len(chains)>0)
	{
	  chain=ApiSet_Get(chains,0);
	  ApiObject_Free(chains);
	}
      else
	{
	  printf("No chains found. Line %d.\n", __LINE__);
	  return 0;
	}
    }

  residues=Mols_Chain_GetResidues(chain);
  ApiObject_Free(chain);

  residueIterator=ApiSet_Iterator(residues);
  while((residue=ApiSetIterator_Next(residueIterator)))
    {
      /* Delete all resonances in the resonance groups of this residue */
      resonanceGroups=Mols_Residue_GetResonanceGroups(residue);
      if(resonanceGroups && ApiSet_Len(resonanceGroups)>0)
	{
	  resonanceGroupIterator=ApiSet_Iterator(resonanceGroups);
	  while((resonanceGroup=ApiSetIterator_Next(resonanceGroupIterator)))
	    {
	      resonances=Nmr_ResonanceGroup_GetResonances(resonanceGroup);
	      resonanceIterator=ApiSet_Iterator(resonances);
	      while((resonance=ApiSetIterator_Next(resonanceIterator)))
		{
		  Impl_DataObject_Delete(resonance);
		  ApiObject_Free(resonance);
		}
	      ApiObject_Free(resonances);
	      ApiObject_Free(resonanceGroup);
	    }
	}
      else
	{
	  string=Mols_Residue_GetCcpCode(residue);
	  if(!string || ApiObject_IsNone(string))
	    {
	      printf("No CcpCode in Residue. Line %d.\n", __LINE__);
	      printRaisedException();
	      projectValid=0;
	      return 0;	  	      
	    }
	  strcpy(ccpCode,ApiString_Get(string));
	  ApiObject_Free(string);
	  string=Mols_Residue_GetMolType(residue);
	  strcpy(molType,ApiString_Get(string));
	  ApiObject_Free(string);
	  
	  objectParameters=ApiMap_New();
	  ApiMap_SetString(objectParameters,"molType",molType);
	  ApiMap_SetString(objectParameters,"ccpCode",ccpCode);
	  ApiMap_SetItem(objectParameters,"residue",residue);
	  
	  /*      resonanceGroup=Nmr_NmrProject_FindFirstResonanceGroup(*nmrProject,objectParameters);*/

	  /*	  printf("New ResonanceGroup.\n");*/
	  resonanceGroup=Nmr_NmrProject_NewResonanceGroup(*nmrProject,objectParameters);
	  seqId=Mols_Residue_GetSeqId(residue);
	  snprintf(name, 128, "%d%s", ApiInteger_Get(seqId),ccpCode);
	  string=ApiString_New(name);
	  Nmr_ResonanceGroup_SetName(resonanceGroup, string);
	  /*	  printf("ResonanceGroup name set to: %s\n", name);*/
	  ApiObject_Free(string);
	  ApiObject_Free(seqId);
	  {
	    ApiObject args[] = { residue, NULL };
	    /* Also generate the atomSets */
	    callModuleFunction("ccpnmr.analysis.util.CasperBasic", "getResidueMapping", args);
	  }
	  ApiObject_Free(objectParameters);
	  ApiObject_Free(resonanceGroup);
	}

      ApiObject_Free(residue);

      /*      if(!Nmr_ResonanceGroup_CheckAllValid(resonanceGroup, ApiBoolean_False()))
	{
	  printf("ResonanceGroup not valid. Line %d.\n", __LINE__);
	  printRaisedException();
	  ApiObject_Free(resonanceGroup);
	  projectValid=0;
	  return 0;
	}
      */
      ApiObject_Free(resonanceGroups);
    }
  ApiObject_Free(residues);

  return i;
}

int CC_MakeSimulationSetupFromMolecules(Impl_MemopsRoot *ccpnProject, struct SE_Simulation *SE_Sim)
{
  ApiSet allMolecules, allResidues, chains;
  ApiSetIterator residueIterator, moleculeIterator;
  ApiInteger nr;
  ApiString string;
  Mole_Molecule molecule;
  Mole_MolResidue residue;
  struct RE_Residue *rres;
  struct SE_Residue *sres;
  struct SE_Unit *SE_CurrUnit;
  int k, cnt;
  int pos, currResidueCnt=0, currUnitCnt=0;
  int seqCode;
  char stereo, stereostr[4], unitName[4], linking[50];
  char *temp;

  allMolecules = Impl_MemopsRoot_GetMolecules(*ccpnProject);
  if(!allMolecules)
    {
      return 0;
    }

  moleculeIterator=ApiSet_Iterator(allMolecules);
  while((molecule=ApiSetIterator_Next(moleculeIterator)))
    {
      chains=Mole_Molecule_GetChains(molecule);

      /* Don't take this molecule into account if it is already part of a chain. */
      if(ApiSet_Len(chains)>0)
	{
	  ApiObject_Free(chains);
	  continue;
	}
      ApiObject_Free(chains);

      allResidues=Mole_Molecule_GetMolResidues(molecule);
      if(!allResidues)
	{
	  return 0;
	}

      /* Iterate through all residues in the molecule */
      residueIterator=ApiSet_Iterator(allResidues);
      while((residue=ApiSetIterator_Next(residueIterator)))
	{
	  if(!residue||ApiObject_IsNone(residue))
	    {
	      printf("Could not get residue. Line %d.\n", __LINE__);
	      continue;
	    }
	  nr=Mole_MolResidue_GetSeqCode(residue);
	  seqCode=ApiInteger_Get(nr);
	  ApiObject_Free(nr);

	  /*	  printf("%d\n",seqCode);*/

	  sprintf(unitName,"%d",seqCode);

	  for(k=0;k<strlen(unitName);k++)
	    {
	      unitName[k]+=48;
	    }
	  SE_CurrUnit=(struct SE_Unit *)
	    ME_CreateNode(&SE_UnitMethod, &SE_Sim->Units,unitName);
	  currResidueCnt=0;

	  /* Don't pay attention to the stereochemistry of the residue in the
	     CCPN molecule. Generate both a and b variants anyhow. */
	  for(stereo='a';stereo<='b';stereo++)
	    {
	      sprintf(stereostr,"%c",stereo);

	      /*	      printf("stereostr: %s\n",stereostr);
			      printf("%s\n",ApiString_Get(Mole_MolResidue_GetCcpCode(residue)));*/

	      /* Generate a residue name that is compatible with the CASPER naming system */

	      string=Mole_MolResidue_GetCcpCode(residue);
	      rres=CC_FindCcpCodeInResidueList(&ResidueList,ApiString_Get(string),stereostr);
	      ApiObject_Free(string);

	      if(rres!=NULL)
		{
		  sres=(struct SE_Residue *)
		    ME_CreateNode(&SE_ResidueMethod,
				  &(SE_CurrUnit->Residues), SE_VOID_NAME);
		  
		  if (sres==NULL)
		    {
		      Error(PA_ERR_FATAL,"Out of memory");
		    }


		  sres->Base=rres;
		  sres->Anomeric=rres->Shifts.Type->Anomeric;
		  
		  sres->JCH=rres->JCH;
		  sres->JHH=rres->JHH;
		  /* Assume that all provided linkage positions are possible,
		     but not yet confirmed */
		  sres->FreeCertainty=FALSE;
		}
	      else
		{
		  printf("Residue not found. Line %d.\n", __LINE__);
		}
	      
	      /* Generate the possible linking locations */
	      string=Mole_MolResidue_GetLinking(residue);
	      strcpy(linking,ApiString_Get(string));
	      ApiObject_Free(string);

	      if(strlen(linking)>0)
		{
		  temp=strtok(linking,"CO");
		  temp=strtok(NULL,"CO");
		  while(temp!=NULL)
		    {
		      sprintf(temp,"%c",temp[0]);
		      pos=TY_FindAtom(rres->Shifts.Type,temp);
		      if(pos==TY_NOT_FOUND)
			{
			  printf("No such position. Line %d.\n", __LINE__);
			  continue;
			  /*			  return(cnt);*/
			}
		      if (!(rres->Shifts.Type->Atom[pos].Type&TY_FREE_POS))
			{
			  printf("Position can not be substituted. Line %d.\n", __LINE__);
			  continue;
			  /*			  return(cnt);*/
			}
		      sres->Free[pos]=SE_FREE;
		      cnt++;
		      temp=strtok(NULL,"CO");
		    }
		}
	      currResidueCnt++;
	      /*	      if(!Mole_MolResidue_CheckAllValid(residue,ApiBoolean_False()))
		{
		  printf("Residue not valid.\n");
		}
	      else
		{
		  printf("Residue valid\n");
		  }*/
	    }
	  if(currResidueCnt==0)
	    {
	      ME_RemoveNode(&SE_UnitMethod,
			    (struct Node *) Last(&SE_Sim->Units));

	    }
	  else
	    {
	      currUnitCnt++;
	    }
	}
    }
  ApiObject_Free(allMolecules);
  return (currUnitCnt);
}

struct RE_Residue* CC_FindCcpCodeInResidueList(struct List *residues, char *ccpCode, char *stereo)
{
  struct RE_Residue *res;
  char *basetype, *basetype_tok, *type, ring[4], *tmp, *mods, residuename[50];
  char *substone, *substone_name=0, *substone_name_tok;
  char *substtwo, *substtwo_name=0, *substtwo_name_tok;
  char *substthree, *substthree_name=0, *substthree_name_tok;
  char *temp, substs[50], modname[24];
  struct List modifiers;
  struct TY_CTmodifier *CTmod, *CTmod_res;
  int i=0, j, modpos;

  InitList(&(modifiers));
  strcpy(modifiers.Head.Name,"Modifiers Head");
  strcpy(modifiers.Tail.Name,"Modifiers Tail");

  basetype=malloc(strlen(ccpCode)+2);
  strcpy(basetype,ccpCode);
  basetype_tok=strtok(basetype,"-");
  sprintf(residuename,"%s%s",stereo,basetype_tok);
  type=strtok(NULL,"-");
  tmp=strtok(NULL,"");
  strncpy(ring,tmp,3);
  ring[3]=0;
  tmp+=3;
  tmp=strtok(tmp,"-:");
  if(tmp&&isdigit(tmp[0]))
    {
      /* mods is a string containing all modifiers, e.g. 6d */
      mods=strtok(tmp,":");
      substone=strtok(NULL,":");
      /* Make a list of all modifiers */
      while((int)mods[i]!=0)
	{
	  j=0;
	  /* Discard e.g. '-' (between modifiers) */
	  while(!isdigit(mods[i]))
	    {
	      i++;
	    }
	  /* Get the position of the modifier (as integer) */
	  modpos=mods[i++]-48;
	  /* Find the name of the modifier */
	  while(isalpha(mods[i]))
	  {
	    modname[j++]=mods[i++];
	  }
	  modname[j]=0;
	  /* Add the modifier to the list of modifiers for this residue */
	  CTmod=(struct TY_CTmodifier *)ME_CreateNode(&CTModifierMethod,&modifiers,modname);
	  CTmod->position=modpos;
	}
    }
  else
    {
      substone=strtok(tmp,":");
    }
  if(substone)
    {
      substone_name=malloc(strlen(substone)+1);
      strcpy(substone_name,substone);
      strtok(substone_name,"_");
      substone_name_tok=strtok(NULL,"");

      substtwo=strtok(NULL,":");
      if(substtwo)
	{
	  substtwo_name=malloc(strlen(substtwo)+1);
	  strcpy(substtwo_name,substtwo);
	  strtok(substtwo_name,"_");
	  substtwo_name_tok=strtok(NULL,"");

	  if(strcmp(substone_name,substtwo_name)>=1)
	    {
	      temp=malloc(strlen(substone)+2);
	      strcpy(temp,substone);
	      strcpy(substone,substtwo);
	      strcpy(substtwo,temp);
	      strcpy(temp,substone_name_tok);
	      strcpy(substone_name_tok,substtwo_name_tok);
	      strcpy(substtwo_name_tok,temp);
	      free(temp);
	    }

	  substthree=strtok(NULL,":");
	  if(substthree)
	    {
	      substthree_name=malloc(strlen(substthree)+1);
	      strcpy(substthree_name,substthree);
	      strtok(substthree_name,"_");
	      substthree_name_tok=strtok(NULL,"");

	      if(strcmp(substone_name,substtwo_name)>=1)
		{
		  temp=malloc(strlen(substtwo)+2);
		  strcpy(temp,substtwo);
		  strcpy(substtwo,substthree);
		  strcpy(substthree,temp);
		  strcpy(temp,substtwo_name_tok);
		  strcpy(substtwo_name_tok,substthree_name_tok);
		  strcpy(substthree_name_tok,temp);
		  free(temp);
		}
	    }
	}
    }
  if(substone)
    {
      sprintf(residuename,"%s%s",residuename,substone_name_tok);
      sprintf(substs,"%s",substone);
      free(substone_name);
      if(substtwo)
	{
	  sprintf(residuename,"%s%s",residuename,substtwo_name_tok);
	  sprintf(substs,"%s%s",substs,substtwo);
	  free(substtwo_name);
	  if(substthree)
	    {
	      sprintf(residuename,"%s%s",residuename,substthree_name_tok);
	      sprintf(substs,"%s%s",substs,substthree);
	      free(substthree_name);
	    }
	}
    }
  else
    substs[0]=0;

  temp=malloc(16);

  for(res=(struct RE_Residue *)residues->Head.Succ;res->Node.Succ!=NULL;
      res=(struct RE_Residue *)res->Node.Succ)
    {
      if(strcasecmp(residuename,res->Node.Name)==0)
	{
	  free(temp);
	  free(basetype);
	  return res;
	}
      else if((res->Node.Name[0]==stereo[0])&&
	      (strcasecmp(type,res->Shifts.Type->CTsuperclass)==0))
	{
	  sprintf(temp,"%s-%s",stereo,basetype);
	  if((strcasecmp(temp,res->CTname)==0)&&
	     (strcasecmp(substs,res->Shifts.Type->ccpSub)==0))
	    {

	      /* Iterate through all modifiers we are looking for */
	      for(CTmod=(struct TY_CTmodifier *)modifiers.Head.Succ;CTmod->Node.Succ!=NULL;
		  CTmod=(struct TY_CTmodifier *)CTmod->Node.Succ)
		{

		  /* Iterate through all modifiers on this candidate residue */
		  CTmod_res=(struct TY_CTmodifier *)FindNode((struct Node *)&res->Shifts.Type->CTmods,CTmod->Node.Name);

		  /* If there were no matching modifiers or if they had the wrong position keep looking */
		  if(!CTmod_res||CTmod_res->position!=CTmod->position)
		    {
		      continue;
		    }
		}
	      free(temp);
	      free(basetype);
	      return res;
	    }
	}
    }
  free(temp);
  free(basetype);
  return 0;
}

/* Returns the a residue from the molSystem. The residue is specified by nr. */
Mols_Residue CC_FindResidueNr(Impl_MemopsRoot *ccpnProject, char *molSystemName, char *chainCode, int nr)
{
  Mols_MolSystem molSystem;
  Mols_Chain chain;
  Mols_Residue residue;
  
  ApiMap searchParameters;
  ApiSet chains;
  int allMolSystemsLen;

  searchParameters=ApiMap_New();
  ApiMap_SetString(searchParameters, "code", molSystemName);

  molSystem = Impl_MemopsRoot_FindFirstMolSystem(*ccpnProject,searchParameters);

  ApiObject_Free(searchParameters);

  if (!molSystem||ApiObject_IsNone(molSystem))
    {
      ApiSet allMolSystems = Impl_MemopsRoot_GetMolSystems(*ccpnProject);
      if(allMolSystems)
	{
	  allMolSystemsLen = ApiSet_Len(allMolSystems);
	}
      else
	{
	  printRaisedException();
	  printf("No molecular system found - aborting. Line %d.\n", __LINE__);
	  projectValid=0;
	  return 0;
	}
      
      if (allMolSystemsLen > 0)
	{
	  molSystem = ApiSet_Get(allMolSystems,0);
	}
      else
	{
	  printRaisedException();
	  printf("No molecular system found - aborting. Line %d.\n", __LINE__);
	  projectValid=0;
	  ApiObject_Free(allMolSystems);
	  return 0;
	}
      ApiObject_Free(allMolSystems);
    }
  if(strlen(chainCode)>0)
    {
      searchParameters=ApiMap_New();
      ApiMap_SetString(searchParameters, "code", chainCode);
      chain=Mols_MolSystem_FindFirstChain(molSystem, searchParameters);
      ApiObject_Free(searchParameters);
    }
  else
    {
      chains=Mols_MolSystem_GetChains(molSystem);
      if(chains && ApiSet_Len(chains)>0)
	{
	  chain=ApiSet_Get(chains,0);
	}
      else
	{
	  if(chains && !ApiObject_IsNone(chains))
	    {
	      ApiObject_Free(chains);
	    }
	  printf("No chain found - aborting. Line %d.\n", __LINE__);
	  projectValid=0;
	  return 0;
	}
      ApiObject_Free(chains);
    }
  if(!chain||ApiObject_IsNone(chain))
    {
      projectValid=0;
      printf("No chain found - aborting. Line %d.\n", __LINE__);
      return 0;
    }

  searchParameters=ApiMap_New();
  ApiMap_SetInt(searchParameters, "seqId", nr);

  residue=Mols_Chain_FindFirstResidue(chain, searchParameters);

  ApiObject_Free(searchParameters);

  return residue;  
}

/* This function looks for atoms in a CCPN residue. The name is prefixed with
   *type and if *name is just number it is just used as a specifier.
   Special kinds of atoms and groups of atoms are treated a bit differently.
   The matching atom or atoms are returned if the function is succesful. */
ApiSet CC_FindAtomsNamed(Mols_Residue residue, char *name, char *type)
{
  Mols_Atom atom;
  ApiSet atoms;
  ApiMap searchParameters;
  char ccpnAtomName[6], localType[8], localName[6];
  int i, j, ptr;

  /* Use local variables to avoid manipulating the input parameters */
  strcpy(localType,type);
  strcpy(localName,name);

  atoms=ApiSet_New();

  /* If we are looking for the second hydrogen on the carbon append a 2 
     to the atom name and remove the 2 from the type. */
  if(strcmp(localType,"H1")==0)
    {
      sprintf(localName,"%s1",localName);
      localType[1]='\0';
    }
  else if(strcmp(localType,"H2")==0)
    {
      sprintf(localName,"%s2",localName);
      localType[1]='\0';
    }
  /* Handle methyl hydrogens - all three must be returned */
  if(type[0]=='H' && name[0]=='M')
    {
      ccpnAtomName[0]='H';
      j=1;
      for(i=0;i<strlen(name);j++,i++)
	{
	  ccpnAtomName[j]=name[i];
	  if(name[i+1]=='_')
	    {
	      j++;
	      ptr=j;
	    }
	}
      ccpnAtomName[j]=0;
      for(i='1';i<'4';i++)
	{
	  ccpnAtomName[ptr]=i;
	  searchParameters=ApiMap_New();
	  ApiMap_SetString(searchParameters,"name",ccpnAtomName);
	  atom=Mols_Residue_FindFirstAtom(residue,searchParameters);
	  if(atom&&!ApiObject_IsNone(atom))
	    {
	      ApiSet_Add(atoms,atom);
	    }
	}
      if(!atoms||ApiSet_Len(atoms)<1)
	{
	  return 0;
	}
      else
	{
	  return atoms;
	}
    }
  /* Otherwise just prefix the atom type to the name */
  sprintf(ccpnAtomName,"%s%s",localType,localName);
  
  searchParameters=ApiMap_New();
  ApiMap_SetString(searchParameters,"name",ccpnAtomName);

  atoms=Mols_Residue_FindAllAtoms(residue,searchParameters);
  ApiObject_Free(searchParameters);

  if((!atoms || ApiSet_Len(atoms)<1) && type[0]=='H' &&
     strlen(ccpnAtomName)==2)
    {
      /* See if it can be ambiguous H's, e.g. H6 in Fucp, which can be
	 H61, H62 and H63 in one signal. */
      if(atoms && !ApiObject_IsNone(atoms))
	{
	  ApiObject_Free(atoms);
	  atoms=ApiSet_New();
	}

      for(i=1;i<4;i++)
	{
	  if(i==1)
	    {
	      sprintf(ccpnAtomName,"%s%d",ccpnAtomName,i);
	    }
	  else
	    {
	      ccpnAtomName[2]++;
	    }
	  searchParameters=ApiMap_New();
	  ApiMap_SetString(searchParameters,"name",ccpnAtomName);
	  atom=Mols_Residue_FindFirstAtom(residue,searchParameters);
	  if(atom&&!ApiObject_IsNone(atom))
	    {
	      ApiSet_Add(atoms,atom);
	    }
	  else
	    {
	      printRaisedException();
	    }

	  ApiObject_Free(searchParameters);
	}
      ccpnAtomName[2]=0;
      

      if(atoms && ApiSet_Len(atoms)>0)
	{
	  return atoms;
	}

      if(atoms && !ApiObject_IsNone(atoms))
	{
	  ApiObject_Free(atoms);
	}
      /* Protein residues use a different naming system and will not have found any
	 matches yet. Handle the special cases here: */
      if(ccpnAtomName[1]=='1')
	{
	  for(i=1;i<strlen(ccpnAtomName);i++)
	    {
	      ccpnAtomName[i]=ccpnAtomName[i+1];
	    }
	}
      else if(ccpnAtomName[1]>'1' && ccpnAtomName[1]<'9')
	{
	  switch(ccpnAtomName[1])
	    {
	    case '4':
	      ccpnAtomName[1]='G';
	      break;
	    default:
	      ccpnAtomName[1]='A'+ccpnAtomName[1]-'2';
	      break;
	    }
	}
      /* For some reasons hydrogens on amino acids are numbered 2 and 3 */
      if(type[0]=='H' && strlen(ccpnAtomName)==3)
	{
	  ccpnAtomName[2]+=1;
	}

      /*      printf("Searching for: %s\n",ccpnAtomName);*/

      searchParameters=ApiMap_New();
      ApiMap_SetString(searchParameters,"name",ccpnAtomName);
      
      atoms=Mols_Residue_FindAllAtoms(residue,searchParameters);
      ApiObject_Free(searchParameters);

      if(atoms && ApiSet_Len(atoms)>0)
	{
	  return atoms;
	}

      return 0;
    }
  return atoms;
}

/* This function finds the shifts in the first shift list that have the
   chemical shift equal to the supplied value.
   The function returns the matching shifts. */   
ApiSet CC_FindShifts(Nmr_NmrProject *nmrProj, float value)
{
  ApiSet shiftLists=0, allShifts=0, matchingShifts=0, allPeakDims=0;
  ApiSetIterator shiftIterator, peakDimIterator;
  ApiMap apiShiftListClass;
  ApiFloat floatValue;
  Nmr_PeakDim peakDim=0;
  Nmr_ShiftList shiftList=0;
  Nmr_Shift shift=0;
  int i, j, numShiftLists, numShifts, numPeakDims, lowDiffNumber;
  float shiftValue, currPeakDimValue, lowDiff;

  lowDiffNumber=-1;
  lowDiff=1000;

  apiShiftListClass = ApiMap_New();
  ApiMap_SetString(apiShiftListClass,"className","ShiftList");
  shiftLists=Nmr_NmrProject_FindAllMeasurementLists(*nmrProj,apiShiftListClass);
  ApiObject_Free(apiShiftListClass);
  /* Get the first shift list or create a new one. */
  if(shiftLists)
    {
      numShiftLists=ApiSet_Len(shiftLists);
    }
  else
    {
      printRaisedException();
      numShiftLists=0;
    }
  if(numShiftLists<=0)
    {
      shiftList=Nmr_NmrProject_NewShiftList_reqd(*nmrProj);
    }
  else
    {
      shiftList = ApiSet_Get(shiftLists,0);
    }
  if(!shiftList)
    {
      printf("Cannot find shiftList. Line %d.\n", __LINE__);
      projectValid=0;
      return 0;
    }
  /*  ApiObject_Free(shiftLists);*/

  /* Get all shifts in the list */
  allShifts=Nmr_ShiftList_GetMeasurements(shiftList);
  if(allShifts && !ApiObject_IsNone(allShifts))
    {
      numShifts=ApiSet_Len(allShifts);
    }
  else
    {
      numShifts=0;
    }
  /*  ApiObject_Free(shiftList);*/
  ApiObject_Free(shiftLists);

  /*  printf("%d shifts in shiftlist\n",numShifts); */
  matchingShifts=ApiSet_New();
  if(numShifts>0)
    {
      shiftIterator=ApiSet_Iterator(allShifts);
    }
  for(i=0;i<numShifts;i++)
    {
      /*      shift=ApiSet_Get(allShifts,i);*/
      shift=ApiSetIterator_Next(shiftIterator);

      if(ApiObject_IsNone(shift) || Impl_MemopsObject_GetIsDeleted(shift)==ApiBoolean_True())
	{
	  ApiObject_Free(shift);
	  continue;
	}

      /* If the value of the shift matches the wanted value consider the shift
	 matching. */
      floatValue=Nmr_Shift_GetValue(shift);
      shiftValue=ApiFloat_Get(floatValue);
      ApiObject_Free(floatValue);
      if(fabs(shiftValue-value)<0.00001)
	{
	  ApiSet_Add(matchingShifts,shift);
	}

      /* Otherwise go through all the peakDims of the shift if the shift
	 is close enough. 1% difference is allowed. */
      else if (fabs(shiftValue-value)<value*0.01)
	{
	  allPeakDims=Nmr_Shift_GetPeakDims(shift);
	  numPeakDims=ApiSet_Len(allPeakDims);
	  if(numPeakDims>0)
	    {
	      peakDimIterator=ApiSet_Iterator(allPeakDims);
	    }
	  for(j=0;j<numPeakDims;j++)
	    {
	      /*	      peakDim=ApiSet_Get(allPeakDims,j);*/
	      peakDim=ApiSetIterator_Next(peakDimIterator);
	      floatValue=Nmr_PeakDim_GetValue(peakDim);
	      currPeakDimValue=ApiFloat_Get(floatValue);
	      ApiObject_Free(floatValue);
	      
	      /* If there is a peakdim that matches the wanted shift append it
		 to the list of matches. */
	      if(fabs(currPeakDimValue-value)<0.00001) /* Error margin for float comparison */
		{
		  ApiSet_Add(matchingShifts,shift);
		}
	      /*	      ApiObject_Free(peakDim);*/
	    }
	  ApiObject_Free(allPeakDims);
	}
      ApiObject_Free(shift);
    }

  if(allShifts && !ApiObject_IsNone(allShifts))
    {
      numShifts=ApiSet_Len(allShifts);
    }
  else
    {
      numShifts=0;
    }

  /* If there has been no matches yet try again, but with higher tolerance.
     This can happen e.g. if shifts have been merged. It is better to find a
     match than to invalidate the project by finding no matches. */
  if(ApiSet_Len(matchingShifts)==0)
    {
      if(numShifts>0)
	{
	  shiftIterator=ApiSet_Iterator(allShifts);
	}
      for(i=0;i<numShifts;i++)
	{
	  /*	  shift=ApiSet_Get(allShifts,i);*/
	  shift=ApiSetIterator_Next(shiftIterator);

	  if(ApiObject_IsNone(shift) || Impl_MemopsObject_GetIsDeleted(shift)==ApiBoolean_True())
	    {
	      ApiObject_Free(shift);
	      continue;
	    }
	  
	  /* If the value of the shift matches the wanted value consider the shift
	     matching. */

	  allPeakDims=Nmr_Shift_GetPeakDims(shift);
	  numPeakDims=ApiSet_Len(allPeakDims);

	  /* If this shift is made up from two merged shifts there must be
	     at least two peakDims. */
	  if(numPeakDims>1)
	    {
	      floatValue=Nmr_Shift_GetValue(shift);
	      shiftValue=ApiFloat_Get(floatValue);
	      ApiObject_Free(floatValue);
	      if(fabs(shiftValue-value)<lowDiff)
		{
		  lowDiff=fabs(shiftValue-value);
		  lowDiffNumber=i;
		}
	    }
	  ApiObject_Free(allPeakDims);
	  ApiObject_Free(shift);
	}
      
      /* Use the best match available. */
      if(lowDiffNumber!=-1 && lowDiff<=1)
	{
	  shift=ApiSet_Get(allShifts, lowDiffNumber);
	  ApiSet_Add(matchingShifts, shift);
	}
    }
  /* Freeing allShifts might disturb things. */
  /*  if(allShifts && !ApiObject_IsNone(allShifts))
    {
      ApiObject_Free(allShifts);
      }*/
  return matchingShifts;
}

/* This function returns all shifts in *nmrProj that link to the peakDim specified
   by wantedDim in a set of peaks */
ApiSet CC_FindShiftsFromPeaks(Nmr_NmrProject *nmrProj, ApiSet peaks, int wantedDim)
{
  ApiSet shiftLists, allShifts, matchingShifts, peakDims, shiftPeakDims;
  ApiSetIterator peakIterator, shiftListIterator, shiftIterator, peakDimIterator;
  ApiMap apiShiftListClass;
  ApiInteger dim;
  Nmr_Peak peak;
  Nmr_ShiftList shiftList=0;
  Nmr_Shift shift=0;
  Nmr_PeakDim peakDim;
  int numShiftLists, numShifts;

  apiShiftListClass = ApiMap_New();
  ApiMap_SetString(apiShiftListClass,"className","ShiftList");
  shiftLists=Nmr_NmrProject_FindAllMeasurementLists(*nmrProj,apiShiftListClass);
  ApiObject_Free(apiShiftListClass);

  dim=ApiInteger_New(wantedDim);

  if(shiftLists)
    {
      numShiftLists=ApiSet_Len(shiftLists);
    }
  else
    {
      numShiftLists=0;
    }
  if(numShiftLists<=0)
    {
      printRaisedException();
      printf("No shiftLists found. Line %d.\n", __LINE__);
      projectValid=0;
      return 0;
    }

  peakDims=ApiSet_New();
  /* First make a set of all peakDims that are relevant */
  peakIterator=ApiSet_Iterator(peaks);
  while((peak=ApiSetIterator_Next(peakIterator)))
    {
      peakDim=Nmr_Peak_FindFirstPeakDim_keyval1(peak, "dim", dim);
      ApiSet_Add(peakDims, peakDim);
      ApiObject_Free(peakDim);
      ApiObject_Free(peak);
    }

  matchingShifts=ApiSet_New();  

  shiftListIterator=ApiSet_Iterator(shiftLists);
  while((shiftList = ApiSetIterator_Next(shiftListIterator)))
    {
      /* Get all shifts in the list */
      allShifts=Nmr_ShiftList_GetMeasurements(shiftList);
      if(allShifts && !ApiObject_IsNone(allShifts))
	{
	  numShifts=ApiSet_Len(allShifts);
	}
      else
	{
	  numShifts=0;
	}

      shiftIterator=ApiSet_Iterator(allShifts);
      while((shift=ApiSetIterator_Next(shiftIterator)))
	{
	  if(ApiObject_IsNone(shift) || Impl_MemopsObject_GetIsDeleted(shift)==ApiBoolean_True())
	    {
	      ApiObject_Free(shift);
	      continue;
	    }
	  /* Get all peakDims from the shift and loop through them to see if any of them are
	     present in peakDims */
	  shiftPeakDims=Nmr_Shift_GetPeakDims(shift);
	  peakDimIterator=ApiSet_Iterator(shiftPeakDims);
	  while((peakDim=ApiSetIterator_Next(peakDimIterator)))
	    {
	      /* If this shift has a peakDim that is also present in peakDims (the peakDims
		 we are interested in add it to matchingShifts */
	      if(ApiSet_Contains(peakDims, peakDim))
		{
		  ApiSet_Add(matchingShifts, shift);
		  ApiObject_Free(peakDim);
		  break;
		}
	      ApiObject_Free(peakDim);
	    }
	  ApiObject_Free(shiftPeakDims);
	  ApiObject_Free(shift);
	}
      ApiObject_Free(shiftList);
    }
  ApiObject_Free(shiftLists);
  ApiObject_Free(peakDims);
  ApiObject_Free(dim);
  return matchingShifts;
}

int CC_AssignPeakToAtoms(Nmr_NmrProject *nmrProj, ApiSet atoms, float firstDim, float secondDim, int assignDim, const char mode)
{
  ApiSet shifts, atomSetCollection, resonanceSets;
  ApiSet dataSourceCollection, peakListCollection;
  ApiSet peakLists, peakDims;
  ApiSet peaks, shiftPeakDims, peakPeakDims, experiments, resonanceSetsInAtomSet;
  ApiSetIterator peakIterator, primShiftIterator, peakDimIterator,
    experimentIterator, resonanceSetIterator, atomIterator;
  ApiFloat tolerance, value;
  Mols_Atom atom;
  Mols_Residue residue;
  ApiMap parameters;
  ApiString string;
  ApiInteger apiIntDim[2];
  Nmr_Shift shift=0, newShift;
  Nmr_ShiftList shiftList;
  Nmr_AtomSet atomSet;
  Nmr_Resonance resonance=0, resonanceInShift, targetResonance=0;
  Nmr_ResonanceSet resonanceSet, resonanceSetAlt;
  Nmr_Peak peak;
  Nmr_PeakDim peakDim, shiftPeakDim, peakPeakDim;
  Nmr_PeakDimContrib peakDimContrib;
  Nmr_Experiment experiment;
  Nmr_DataSource dataSource;
  int i, j, numShifts, numPeaks, wantedNumPeakDims;
  int unassignedShiftNr=999, assignedShiftNr, numPeakPeakDims, numAtomSets;
  int numExperiments, numDataSources, numAtoms, numShiftPeakDims, numPeakDims;
  char name[16];
  float *primDim, *secDim;

  numAtoms=ApiSet_Len(atoms);

  if(!atoms || numAtoms<1)
    {
      projectValid=0;
      return 0;
    }

  /* Since all atoms should be equal use the first one to find or make the
     atomset */
  atomIterator=ApiSet_Iterator(atoms);
  atom=ApiSetIterator_Next(atomIterator);
  atomSet=Mols_Atom_GetAtomSet(atom);
  residue=Mols_Atom_GetResidue(atom);

  numAtoms=ApiSet_Len(atoms);
  string=Mols_Atom_GetName(atom);
  strcpy(name,ApiString_Get(string));
  ApiObject_Free(string);

  /* If there is more than one atom in the set use a wildcard to specify
     the name of the set. */
  if(numAtoms>1)
    {
      name[2]='*';
    }

  /*  printf("Obj. count on line %d: %d\n",__LINE__, getGlobalObjectCount());*/

  if(!atomSet||ApiObject_IsNone(atomSet))
    {
      atomSet=CC_CreateAtomSet(nmrProj, atoms, name);
    }
  ApiObject_Free(atoms);

  resonanceSetsInAtomSet=Nmr_AtomSet_GetResonanceSets(atomSet);
  if(!resonanceSetsInAtomSet)
    {
      printRaisedException();
      printf("AtomSet broken? Line %d.\n", __LINE__);
      projectValid=0;
      return 0;
    }

  /* If the atomSet has been linked to at least one resonanceSet use the first one */
  if(ApiSet_Len(resonanceSetsInAtomSet)>0)
    {
      resonanceSetIterator=ApiSet_Iterator(resonanceSetsInAtomSet);
      resonanceSet=ApiSetIterator_Next(resonanceSetIterator);
      /*      resonances=Nmr_ResonanceSet_SortedResonances(resonanceSet);*/
      /*      atomSetsInResonanceSet=Nmr_ResonanceSet_GetAtomSets(resonanceSet);*/
      /*      numResonances=ApiList_Len(resonances);*/

      parameters=ApiMap_New();
      ApiMap_SetString(parameters, "details", name);
      targetResonance=Nmr_ResonanceSet_FindFirstResonance(resonanceSet, parameters);
      ApiObject_Free(parameters);

      if(ApiObject_IsNone(targetResonance))
	{
	  targetResonance=0;
	  /*	  printf("No matching resonance found in resonanceSet\n");*/
	}
    }
  ApiObject_Free(resonanceSetsInAtomSet);

  apiIntDim[0]=ApiInteger_New(1);
  apiIntDim[1]=ApiInteger_New(2);

  if(mode==SE_PURGE_C || mode==SE_PURGE_H)
    {
      wantedNumPeakDims=1;
    }
  else
    {
      wantedNumPeakDims=2;
    }
  if(assignDim==1)
    {
      primDim=&firstDim;
      secDim=&secondDim;
      if(mode==SE_PURGE_H || mode==SE_PURGE_HH)
	{
	  tolerance=ApiFloat_New(SP_H_CUTOFF/10);
	}
      else
	{
	  tolerance=ApiFloat_New(SP_C_CUTOFF/10);
	}
    }
  else
    {
      primDim=&secondDim;
      secDim=&firstDim;
      tolerance=ApiFloat_New(SP_H_CUTOFF/10);
    }
  
  switch (mode)
    {
    case SE_PURGE_C:
      experiments=CC_GetExpOfType(nmrProj, "C");
      break;
    case SE_PURGE_H:
      experiments=CC_GetExpOfType(nmrProj, "H");
      break;
    case SE_PURGE_CH:
      experiments=CC_GetExpOfType(nmrProj, "H[C]");
      break;
    case SE_PURGE_HH:
      experiments=CC_GetExpOfType(nmrProj, "H_H.TOCSY");
      break;
      
    default:
      experiments=ApiSet_New();
      break;
    }
  numExperiments=ApiSet_Len(experiments);

  /* Make a set of peaklists present in the matching experiments */
  peakListCollection=ApiSet_New();
  if(numExperiments>0)
    {
      /*      printf("Obj. count on line %d: %d\n",__LINE__, getGlobalObjectCount());*/
      experimentIterator=ApiSet_Iterator(experiments);
      for(i=0;i<numExperiments;i++)
	{
	  experiment=ApiSetIterator_Next(experimentIterator);
	  /* Get the shiftList of the first experiment for later use */
	  if(i==0)
	    {
	      shiftList=Nmr_Experiment_GetShiftList(experiment);
	    }
	  dataSourceCollection=Nmr_Experiment_GetDataSources(experiment);
	  if(dataSourceCollection)
	    {
	      numDataSources=ApiSet_Len(dataSourceCollection);
	    }
	  else
	    {
	      numDataSources=0;
	      printRaisedException();
	    }
	  for(j=0;j<numDataSources;j++)
	    {
	      dataSource=ApiSet_Get(dataSourceCollection,j);
	      peakLists=Nmr_DataSource_GetPeakLists(dataSource);
	      ApiSet_AddSet(peakListCollection, peakLists);
	      ApiObject_Free(peakLists);
	      ApiObject_Free(dataSource);
	    }
	  ApiObject_Free(dataSourceCollection);
	  ApiObject_Free(experiment);
	}
    }
  ApiObject_Free(experiments);

  /* Make sets of shifts and peaks that are relevant (hopefully just one shift and one peak) */
  peaks=CC_FindPeaks(peakListCollection, firstDim, secondDim, wantedNumPeakDims, 0.001, &apiIntDim[0], &apiIntDim[1]);
  numPeaks=ApiSet_Len(peaks);
  if(numPeaks==0)
    {
      peaks=CC_FindPeaks(peakListCollection, firstDim, secondDim, wantedNumPeakDims, 0.01, &apiIntDim[0], &apiIntDim[1]);
    }
  shifts=CC_FindShiftsFromPeaks(nmrProj, peaks, ApiInteger_Get(apiIntDim[assignDim-1]));

  ApiObject_Free(peakListCollection);

  if(!shifts || !peaks)
    {
      return 0;
    }
  numShifts=ApiSet_Len(shifts);
  numPeaks=ApiSet_Len(peaks);
  peakDims=ApiSet_New();

  if(numPeaks<=0)
    {
      printRaisedException();
      printf("No peaks found. Line %d.", __LINE__);
      return 0;
    }

  if(numShifts>0)
    {
      assignedShiftNr=-1;
      primShiftIterator=ApiSet_Iterator(shifts);
      for(i=0;i<numShifts;i++)
	{
	  /*	  shift=ApiSet_Get(shifts,i);*/
	  shift=ApiSetIterator_Next(primShiftIterator);

	  resonanceInShift=Nmr_Shift_GetResonance(shift);
	  resonanceSetAlt=Nmr_Resonance_GetResonanceSet(resonanceInShift);
	  string=Nmr_Resonance_GetName(resonanceInShift);

	  if(targetResonance && !ApiObject_IsNone(targetResonance) && resonanceInShift == targetResonance)
	    {
	      assignedShiftNr=i;
	      /*	      i=numShifts;*/
	    }
	  /* If this resonance has been assigned to this resonanceSet the
	     assignment is already done. */
	  else if(resonanceSetAlt == resonanceSet)
	    {
	      /* But check if it is e.g. H61 or H62 etc. 
		 In that case a new assignment might have to be made. */
	      if(strlen(name)<=2)
		{
		  assignedShiftNr=i;
		}
		else if(name[strlen(name)-1]=='1' || name[strlen(name)-1]=='2')
		  {
		    ApiObject args[] = { resonanceInShift, NULL };
		    callModuleFunction("ccpnmr.analysis.util.CasperBasic", "initResonance", args);
		    /* TODO: Should the details really be changed here? It might complicate further assignments. */
		    /*		    ApiObject_Free(string);
		    string=ApiString_New(name);
		    Nmr_Resonance_SetDetails(resonanceInShift, string);*/
		  }
	    }
	  else if(!resonanceSetAlt || ApiObject_IsNone(resonanceSetAlt))
	    {
	      unassignedShiftNr=i;
	    }
	  if(resonanceSetAlt)
	    {
	      ApiObject_Free(resonanceSetAlt);
	    }
	  ApiObject_Free(shift);
	  ApiObject_Free(resonanceInShift);
	  ApiObject_Free(string);
	}

      if(unassignedShiftNr<999)
	{
	  /*	  printf("Using unassigned shift\n");*/
	  shift=ApiSet_Get(shifts,unassignedShiftNr);
	}

      else if(assignedShiftNr>=0)
	{
	  /*	  printf("Using assigned shift\n");*/
	  return 1;
	  /*	  shift=ApiSet_Get(shifts,assignedShiftNr);*/
	}

      /* If there is already a resonance (assigned to this atom) just use that one */
      if(unassignedShiftNr<999 && targetResonance)
	{
	  resonanceInShift=Nmr_Shift_GetResonance(shift);

	  {
	    ApiObject args[] = { resonanceInShift, targetResonance, NULL };
	    callModuleFunction("ccpnmr.analysis.util.CasperBasic", "mergeResonances", args);
	  }

	  ApiObject_Free(resonanceInShift);
	  ApiObject_Free(shift);
	  if(!Nmr_Resonance_CheckAllValid(targetResonance,ApiBoolean_True()))
	    {
	      projectValid=0;
	      printRaisedException();
	      printf("Resonance invalid after merge. Line %d.\n", __LINE__);
	      ApiObject_Free(targetResonance);
	      projectValid=0;
	      return 0;	      
	    }
	  
	  ApiObject_Free(targetResonance);
	  ApiObject_Free(peakDims);
	  ApiObject_Free(peaks);
	  return 1;
	}

      if(shift && !ApiObject_IsNone(shift))
	{
	  shiftPeakDims=Nmr_Shift_GetPeakDims(shift);
	  if(numPeaks>0)
	    {
	      peakIterator=ApiSet_Iterator(peaks);
	      while((peak=ApiSetIterator_Next(peakIterator)))
		{
		  peakPeakDims=Nmr_Peak_GetPeakDims(peak);
		  if(peakPeakDims && ApiSet_Len(peakPeakDims)>0)
		    {
		      numShiftPeakDims=ApiSet_Len(shiftPeakDims);
		      if(numShiftPeakDims>0)
			{
			  peakDimIterator=ApiSet_Iterator(shiftPeakDims);
			  while((shiftPeakDim=ApiSetIterator_Next(peakDimIterator)))
			    {
			      if(ApiSet_Contains(peakPeakDims, shiftPeakDim))
				{
				  ApiSet_Add(peakDims, shiftPeakDim);
				}
			      ApiObject_Free(shiftPeakDim);
			    }
			}
		    }
		  ApiObject_Free(peakPeakDims);
		  ApiObject_Free(peak);
		}
	    }
	  ApiObject_Free(shiftPeakDims);
	  numPeakDims=ApiSet_Len(peakDims);
	}

      if(resonance && !ApiObject_IsNone(resonance))
	{
	  ApiObject_Free(resonance);
	}
    }

  if((unassignedShiftNr>=999 && assignedShiftNr<0) || !shift || ApiObject_IsNone(shift))
    {
      if(!shift || ApiObject_IsNone(shift))
	{
	  printf("No shift found at all.\n");
	}
	 
      /*      printf("Creating new shift.\n");*/
      
      parameters=ApiMap_New();
      if(name[0]=='H')
	{
	  ApiMap_SetString(parameters,"isotopeCode","1H");
	}
      else
	{
	  ApiMap_SetString(parameters,"isotopeCode","13C");
	}
      resonance=Nmr_NmrProject_NewResonance(*nmrProj,parameters);
      ApiObject_Free(parameters);
      
      parameters=ApiMap_New();
      ApiMap_SetFloat(parameters, "value", *primDim);
      ApiMap_SetItem(parameters,"resonance",resonance);
      if(numPeaks>0)
	{
	  /*	  printf(" Adding peaks. ");*/
	  ApiMap_SetItem(parameters,"peaks", peaks);
	}
      if(numPeakDims<=0 && numPeaks>0)
	{
	  peakIterator=ApiSet_Iterator(peaks);
	  while((peak=ApiSetIterator_Next(peakIterator)))
	    {
	      peakPeakDims=Nmr_Peak_GetPeakDims(peak);
	      numPeakPeakDims=ApiSet_Len(peakPeakDims);
	      peakDimIterator=ApiSet_Iterator(peakPeakDims);
	      while((peakPeakDim=ApiSetIterator_Next(peakDimIterator)))
		{
		  value=Nmr_PeakDim_GetValue(peakPeakDim);
		  if(ApiFloat_Get(value)-*primDim<ApiFloat_Get(tolerance))
		    {
		      ApiSet_Add(peakDims, peakPeakDim);
		    }
		  ApiObject_Free(peakPeakDim);
		}
	      ApiObject_Free(peakPeakDims);
	      ApiObject_Free(peak);
	    }
	  numPeakDims=ApiSet_Len(peakDims);
	}
      if(numPeakDims<=0)
	{
	  printf("No peakDims! Line %d.\n", __LINE__);
	  printRaisedException();
	  projectValid=0;
	  return 0;
	}
      ApiMap_SetItem(parameters,"peakDims", peakDims);

      newShift=Nmr_ShiftList_NewShift(shiftList,parameters);

      if(!newShift || !Nmr_Shift_CheckAllValid(newShift, ApiBoolean_False()))
	{
	  printf("Cannot create new shift. Line %d.\n", __LINE__);
	  printRaisedException();
	  projectValid=0;
	  return 0;
	}
      
      ApiObject_Free(shiftList);
      ApiObject_Free(parameters);
      ApiObject_Free(newShift);
      if(shift && !ApiObject_IsNone(shift))
	{
	  ApiObject_Free(shift);
	}
      if(targetResonance && !ApiObject_IsNone(targetResonance))
	{
	  /*	  printf("Merging resonances\n");*/
	  /*	  mergeResonances(targetResonance, resonance);*/
	  {
	    ApiObject args[] = { resonanceInShift, targetResonance, NULL };
	    callModuleFunction("ccpnmr.analysis.util.CasperBasic", "mergeResonances", args);
	  }
	  ApiObject_Free(resonance);
	  if(!Nmr_Resonance_CheckAllValid(targetResonance, ApiBoolean_False()))
	    {
	      printf("Resonance invalid after merge. Line %d.\n", __LINE__);
	      ApiObject_Free(targetResonance);
	      projectValid=0;
	      return 0;
	    }
	  ApiObject_Free(targetResonance);
	  return 1;
	}
    }
  else
    {
      resonance=Nmr_Shift_GetResonance(shift);
    }

  ApiObject_Free(peaks);
  if(assignedShiftNr<0)
    {
      /*      printf("Getting resonanceSet from atomSet\n");*/
      resonanceSets=Nmr_AtomSet_GetResonanceSets(atomSet);
      if(resonanceSets && !ApiObject_IsNone(resonanceSets) && ApiSet_Len(resonanceSets)>0)
	{
	  resonanceSet=ApiSet_Get(resonanceSets,0);
	  /*	  printf("Setting resonanceSet in resonance\n");*/
	  Nmr_Resonance_SetResonanceSet(resonance,resonanceSet);
	  ApiObject_Free(resonanceSet);
	}
      ApiObject_Free(resonanceSets);
    }

  atomSetCollection=CC_GetAtomSetCollectionOfAtom(nmrProj, atom, residue);

  ApiObject_Free(residue);
  ApiObject_Free(atom);
  ApiObject_Free(atomSet);

  numAtomSets=ApiSet_Len(atomSetCollection);

  if(numAtomSets==0)
    {
      printf("No atomSets!\n");
      projectValid=0;
      printRaisedException();
      ApiObject_Free(atomSetCollection);
      ApiObject_Free(tolerance);
      ApiObject_Free(resonance);
      return 0;
    }

  for(j=0;j<numPeakDims;j++)
    {
      peakDim=ApiSet_Get(peakDims, j);
      /*      printf("PeakDim %d: Dim %d. Value %f.\n", j, ApiInteger_Get(Nmr_PeakDim_GetDim(peakDim)), ApiFloat_Get(Nmr_PeakDim_GetValue(peakDim)));*/
      ApiObject args[] = { resonance, peakDim, atomSetCollection, ApiObject_None(), tolerance, NULL };
      peakDimContrib=callModuleFunction("ccpnmr.analysis.util.CasperBasic", "assignPeakDim", args);
      /*      ApiObject_Free(peakDim);*/
      ApiObject_Free(peakDimContrib);
    }
  ApiObject_Free(atomSetCollection);
  ApiObject_Free(tolerance);
  ApiObject_Free(peakDims);

  if(!Nmr_Resonance_CheckAllValid(resonance,ApiBoolean_False()))
    {
      projectValid=0;
      printRaisedException();
      printf("Resonance invalid. Line %d.\n", __LINE__);
      ApiObject_Free(resonance);
      return 0;	      
    }

  {
    ApiObject args[] = { resonance, NULL };
    callModuleFunction("ccpnmr.analysis.util.CasperBasic", "initResonance", args);
  }

  /* Set the atom name in the details field. This means that if the function was called to assign e.g. H61 the resonance
     will be assigned to both H61 and H62 (since they are ambiguous), but CASPER can then later still know which atom
     has been used. This means that when assigning H61 the next time the same resonance can be used (even if there
     can be two resonances in that resonanceSet a new one will not be created), but if H62 is assigned then a new
     resonance will be put in the resonanceSet. */
  string=ApiString_New(name);
  Nmr_Resonance_SetDetails(resonance, string);
  ApiObject_Free(string);

  /*  if(!Nmr_Resonance_CheckAllValid(resonance,ApiBoolean_False()))
    {
      projectValid=0;
      printRaisedException();
      printf("Resonance invalid. Line %d.\n", __LINE__);
      ApiObject_Free(resonance);
      return 0;	      
    }
  */        
  ApiObject_Free(resonance);
  
  /*  printf("Obj. count after: %d\n",getGlobalObjectCount());*/

  return 1;
}

ApiSet CC_GetAtomSetCollectionOfAtom(Nmr_NmrProject *nmrProj, Mols_Atom atom, Mols_Residue residue)
{
  ApiSet atomSetCollection, chemAtoms, atoms;
  ApiMap parameters;
  Mols_Atom atomAlt;
  Nmr_AtomSet atomSet, atomSetAlt;
  Chem_ChemAtomSet chemAtomSet;
  Chem_ChemAtom chemAtom, chemAtomAlt;
  ApiString string;
  char tempName[16];
  int j, numChemAtoms;

  atomSetCollection=ApiSet_New();

  chemAtom=Mols_Atom_GetChemAtom(atom);
  if(!chemAtom)
    {
      printf("No chemAtom in atom. Line %d.\n", __LINE__);
      printRaisedException();
      projectValid=0;
      return 0;
    }
  chemAtomSet=Chem_ChemAtom_GetChemAtomSet(chemAtom);

  atomSet=Mols_Atom_GetAtomSet(atom);

  if(chemAtomSet && !ApiObject_IsNone(chemAtomSet))
    {
      chemAtoms=Chem_ChemAtomSet_GetChemAtoms(chemAtomSet);
      numChemAtoms=ApiSet_Len(chemAtoms);
      for(j=0;j<numChemAtoms;j++)
	{
	  chemAtomAlt=ApiSet_Get(chemAtoms,j);
	  parameters=ApiMap_New();
	  ApiMap_SetItem(parameters,"chemAtom",chemAtomAlt);
	  atomAlt=Mols_Residue_FindFirstAtom(residue,parameters);
	  if(atomAlt && !ApiObject_IsNone(atomAlt))
	    {
	      atomSetAlt=Mols_Atom_GetAtomSet(atomAlt);
	      /* If this atomSet does not exist yet create it */
	      if(!atomSetAlt || ApiObject_IsNone(atomSetAlt))
		{
		  atoms=Mols_Residue_FindAllAtoms(residue,parameters);
		  string=Mols_Atom_GetName(atomAlt);
		  strcpy(tempName, ApiString_Get(string));
		  ApiObject_Free(string);
		  if(ApiSet_Len(atoms)>1)
		    {
		      tempName[2]='*';		      
		    }
		  parameters=ApiMap_New();
		  ApiMap_SetItem(parameters,"atoms",atoms);
		  ApiMap_SetString(parameters,"name",tempName);
		  atomSetAlt=Nmr_NmrProject_NewAtomSet(*nmrProj, parameters);
		  ApiObject_Free(parameters);
		  ApiObject_Free(atoms);
		}
	      /* Add this atomSet to the collection of atomSets to which this
		 resonance will be assigned. */
	      if(atomSetAlt && !ApiObject_IsNone(atomSetAlt))
		{
		  ApiSet_Add(atomSetCollection, atomSetAlt);
		  ApiObject_Free(atomSetAlt);
		}
	      ApiObject_Free(atomAlt);
	    }
	  /*	  ApiObject_Free(chemAtomAlt);*/
	  ApiObject_Free(parameters);
	}
      ApiObject_Free(chemAtoms);
      ApiObject_Free(chemAtomSet);
    }
  else if(atomSet && !ApiObject_IsNone(atomSet))
    {
      ApiSet_Add(atomSetCollection, atomSet);
    }
  ApiObject_Free(chemAtom);

  return atomSetCollection;
}

/* This function adds all 13C and 1H shifts in *str to shiftList.
   It does not deal with peaks and annotation. It is meant to populate the shiftList with simulated shifts. */
int CC_AssignSimulatedShifts(Impl_MemopsRoot *ccpnProject, Nmr_NmrProject *nmrProject, struct BU_Struct *str, Nmr_ShiftList shiftList)
{
  struct BU_Unit *unit;
  Mols_Residue residue;
  Nmr_Shift shift;
  ApiSet cAtoms, hAtoms;
  ApiFloat merit;
  int i;
  
  for (unit=(struct BU_Unit *)str->Units.Head.Succ;
       unit->Node.Succ!=NULL;
       unit=(struct BU_Unit *)unit->Node.Succ)
    {
      residue=CC_FindResidueNr(ccpnProject, str->Node.Name, str->CcpnChainCode, unit->CcpnUnitNr);
      if(!residue || ApiObject_IsNone(residue))
	{
	  projectValid=0;
	  return 0;
	}
      /* When calculating the merit let expected error 5 (or higher) give merit 0. */
      merit=ApiFloat_New(Max(0, 1-0.2*unit->Error));
      for(i=0; i<unit->Residue->Shifts.Type->HeavyCnt; i++)
	{
	  cAtoms=CC_FindAtomsNamed(residue, unit->Residue->Shifts.Type->Atom[i].Label, "C");
	  shift=CC_CreateShiftAssignedToAtoms(nmrProject, shiftList, unit->Shifts.C[i], cAtoms);
	  Nmr_Shift_SetFigOfMerit(shift, merit);
	  ApiObject_Free(cAtoms);
	  ApiObject_Free(shift);
	  if(unit->Residue->Shifts.Type->Atom[i].HCnt==1)
	    {
	      hAtoms=CC_FindAtomsNamed(residue, unit->Residue->Shifts.Type->Atom[i].Label, "H");
	      shift=CC_CreateShiftAssignedToAtoms(nmrProject, shiftList, unit->Shifts.H[i][0], hAtoms);
	      Nmr_Shift_SetFigOfMerit(shift, merit);
	      ApiObject_Free(hAtoms);
	      ApiObject_Free(shift);
	    }
	  else if(unit->Residue->Shifts.Type->Atom[i].HCnt==2)
	    {
	      hAtoms=CC_FindAtomsNamed(residue, unit->Residue->Shifts.Type->Atom[i].Label, "H1");
	      shift=CC_CreateShiftAssignedToAtoms(nmrProject, shiftList, unit->Shifts.H[i][0], hAtoms);	      
	      Nmr_Shift_SetFigOfMerit(shift, merit);
	      ApiObject_Free(hAtoms);
	      ApiObject_Free(shift);
	      hAtoms=CC_FindAtomsNamed(residue, unit->Residue->Shifts.Type->Atom[i].Label, "H2");
	      shift=CC_CreateShiftAssignedToAtoms(nmrProject, shiftList, unit->Shifts.H[i][1], hAtoms);
	      Nmr_Shift_SetFigOfMerit(shift, merit);
	      ApiObject_Free(hAtoms);
	      ApiObject_Free(shift);
	    }
	}
      ApiObject_Free(merit);
    }
  return 1;
}

/* This function creates a shift and assigns it to atoms (should be equivalent). It creates atomSet, resonance and resonanceSet
   if they do not already exist. */
/* FIXME: Contains some code from AssignPeakToAtoms - make separate functions? */
Nmr_Shift CC_CreateShiftAssignedToAtoms(Nmr_NmrProject *nmrProject, Nmr_ShiftList shiftList, float value, ApiSet atoms)
{
  Mols_Atom atom;
  Mols_Residue residue;
  Nmr_ResonanceSet resonanceSet=0;
  Nmr_AtomSet atomSet;
  Nmr_Resonance resonance=0, resonanceAlt;
  Nmr_Shift shift;
  ApiString string;
  ApiFloat apiValue;
  ApiSet atomSets, resonances, resonanceSetsInAtomSet;
  ApiSetIterator atomIterator, resonanceSetIterator, resonanceIterator;
  ApiMap parameters;
  char name[16];
  int numAtoms, numResonances;

  numAtoms=ApiSet_Len(atoms);

  if(!atoms || numAtoms<1)
    {
      projectValid=0;
      return 0;
    }

  /* Since all atoms should be equal use the first one to find or make the
     atomset */
  atomIterator=ApiSet_Iterator(atoms);
  atom=ApiSetIterator_Next(atomIterator);
  atomSet=Mols_Atom_GetAtomSet(atom);
  residue=Mols_Atom_GetResidue(atom);

  string=Mols_Atom_GetName(atom);
  strcpy(name,ApiString_Get(string));
  ApiObject_Free(string);
  /* If there is more than one atom in the set use a wildcard to specify
     the name of the set. */
  if(numAtoms>1)
    {
      name[2]='*';
    }
  if(!atomSet || ApiObject_IsNone(atomSet))
    {
      atomSet=CC_CreateAtomSet(nmrProject, atoms, name);
    }
  ApiObject_Free(atoms);

  resonanceSetsInAtomSet=Nmr_AtomSet_GetResonanceSets(atomSet);
  if(!resonanceSetsInAtomSet)
    {
      printRaisedException();
      printf("AtomSet broken? Line %d.\n", __LINE__);
      projectValid=0;
      return 0;
    }
  
  /* If the atomSet has been linked to at least one resonanceSet use the first one */
  if(ApiSet_Len(resonanceSetsInAtomSet)>0)
    {
      resonanceSetIterator=ApiSet_Iterator(resonanceSetsInAtomSet);
      resonanceSet=ApiSetIterator_Next(resonanceSetIterator);
      ApiObject_Free(resonanceSetIterator);

      parameters=ApiMap_New();
      ApiMap_SetString(parameters, "details", name);
      resonances=Nmr_ResonanceSet_FindAllResonances(resonanceSet, parameters);
      ApiObject_Free(parameters);

      numResonances=ApiSet_Len(resonances);

      resonanceIterator=ApiSet_Iterator(resonances);
      resonance=ApiSetIterator_Next(resonanceIterator);

      /*      if(numResonances>=2)
	{
	  while((resonanceAlt=ApiSetIterator_Next(resonanceIterator)))
	    {
	      if(resonance!=resonanceAlt)
		{
		  ApiObject args[] = {resonanceAlt, resonance, NULL};
		  resonance=callModuleFunction("ccpnmr.analysis.util.CasperBasic", "mergeResonances", args);
		  printf("Merged resonances.\n");
		}
	    }
	}
      */
      if(ApiObject_IsNone(resonance))
	{
	  resonance=0;
	  printf("No matching resonance found in resonanceSet. Looking for %s.\n", name);
	}
    }
  if(!resonance)
    {
      parameters=ApiMap_New();
      ApiMap_SetString(parameters, "details", name);
      if(name[0]=='H')
	{
	  ApiMap_SetString(parameters,"isotopeCode","1H");
	}
      else
	{
	  ApiMap_SetString(parameters,"isotopeCode","13C");
	}
      resonance=Nmr_NmrProject_NewResonance(*nmrProject, parameters);
      string=ApiString_New(name);
      Nmr_Resonance_AddAssignName(resonance, string);
      {
	ApiObject args[] = { resonance, NULL };
	callModuleFunction("ccpnmr.analysis.util.CasperBasic", "initResonance", args);
      }
      ApiObject_Free(string);
      ApiObject_Free(parameters);
      
      atomSets=CC_GetAtomSetCollectionOfAtom(nmrProject, atom, residue);

      if(resonanceSet)
	{
	  ApiObject args[] = {atomSets, resonance, resonanceSet, NULL};
	  callModuleFunction("ccpnmr.analysis.util.CasperBasic", "assignAtomsToRes", args);
	}
      else
	{
	  ApiObject args[] = {atomSets, resonance, NULL};
	  callModuleFunction("ccpnmr.analysis.util.CasperBasic", "assignAtomsToRes", args);
	}
      
      /*      if(!resonanceSet)
	{
	  parameters=ApiMap_New();
	  atomSets=ApiSet_New();
	  resonances=ApiSet_New();
	  ApiSet_Add(atomSets, atomSet);
	  ApiSet_Add(resonances, resonance);
	  ApiMap_SetItem(parameters, "atomSets", atomSets);
	  ApiMap_SetItem(parameters, "resonances", resonances);
	  resonanceSet=Nmr_NmrProject_NewResonanceSet(*nmrProject, parameters);
	  ApiObject_Free(resonances);
	  ApiObject_Free(atomSets);
	  ApiObject_Free(parameters);
	  if(!resonanceSet || ApiObject_IsNone(resonanceSet))
	    {
	      printf("Cannot create resonanceSet. Line %d\n", __LINE__);
	      projectValid=0;
	      return 0;
	    }
	    }
           else
	{
	  Nmr_Resonance_SetResonanceSet(resonance, resonanceSet);
	}
      */
    }
  if(!resonance || ApiObject_IsNone(resonance))
    {
      printf("No resonance. Line %d.\n", __LINE__);
      projectValid=0;
      return 0;
    }
  if(!Nmr_Resonance_CheckAllValid(resonance, ApiBoolean_False()))
    {
      printf("Resonance not valid. Line %d.\n", __LINE__);
    }
  parameters=ApiMap_New();
  apiValue=ApiFloat_New(value);
  ApiMap_SetItem(parameters, "value", apiValue);
  ApiMap_SetItem(parameters, "resonance", resonance);
  shift=Nmr_ShiftList_NewShift(shiftList, parameters);
  ApiObject_Free(apiValue);
  ApiObject_Free(parameters);
  ApiObject_Free(resonance);
  ApiObject_Free(resonanceSetsInAtomSet);

  if(!shift || ApiObject_IsNone(shift))
    {
      printf("Could not create shift. Line %d.\n", __LINE__);
      projectValid=0;
      return 0;
    }
  if(!Nmr_Shift_CheckAllValid(shift, ApiBoolean_False()))
    {
      printf("Shift not valid. Line %d.\n", __LINE__);
      projectValid=0;
    }
  return shift;
}

/* Creates and atomSet for atoms */
Nmr_AtomSet CC_CreateAtomSet(Nmr_NmrProject *nmrProject, ApiSet atoms, char *name)
{
  Nmr_AtomSet atomSet;
  ApiMap parameters;

  parameters=ApiMap_New();
  ApiMap_SetItem(parameters,"atoms",atoms);
  ApiMap_SetString(parameters,"name",name);
  atomSet=Nmr_NmrProject_NewAtomSet(*nmrProject, parameters);
  ApiObject_Free(parameters);
  if(!atomSet || ApiObject_IsNone(atomSet))
    {
      printRaisedException();
      if(atomSet && !ApiObject_IsNone(atomSet))
	{
	  ApiObject_Free(atomSet);
	}
      projectValid=0;
      printf("Could not create AtomSet. Line %d.\n", __LINE__);
      return 0;
    }
  return atomSet;
}

/* This function does not work at this stage.
   FIXME: Remove? */
int CC_AddResonancesToPeaks(int *match, Nmr_ShiftList *shiftList, struct BU_Struct *str,
			    float *newShifts, int newCnt, unsigned int mode)
{
  int currentCnt, i/*, j, k, *tempMatch=0, cnt, found*/;
  float *currentShifts=0, *newShiftsSorted=0, *currentShiftsSorted=0;
  /*  Nmr_Resonance resonance=0;*/
  ApiSet resonances=0;

  switch(mode)
    {
    case SP_ID_PROTON:
      if(str->HCnt>0)
	{
	  if(newShifts==str->HShift)
	    {
	      return 0;
	    }
	  currentCnt=str->HCnt;
	  currentShifts=str->HShift;
	}
      if(str->ChCnt>0)
	{
	  if(newShifts==str->ChHShift)
	    {
	      return 0;
	    }
	  currentCnt=str->ChCnt;
	  currentShifts=str->ChHShift;
	}
      
      if(currentShifts==0)
	{
	  return 0;
	}
      break;
    case SP_ID_CARBON:
      if(str->CCnt>0)
	{
	  if(newShifts==str->CShift)
	    {
	      return 0;
	    }
	  currentCnt=str->CCnt;
	  currentShifts=str->CShift;
	}
      if(str->ChCnt>0)
	{
	  if(newShifts==str->ChCShift)
	    {
	      return 0;
	    }
	  currentCnt=str->ChCnt;
	  currentShifts=str->ChCShift;
	}
      
      if(currentShifts==0)
	{
	  return 0;
	}
      break;
    }


  /* If the shifts to match (either the new shifts or the already
     registered shifts) are 2D they might have to be resorted before
     matching them. 2D shifts are sorted on their distance to origo.
     To match them easily each dimension has to be sorted numerically. */
  newShiftsSorted=malloc(sizeof(float)*newCnt);
  currentShiftsSorted=malloc(sizeof(float)*currentCnt);
  for(i=0;i<newCnt;i++)
    {
      newShiftsSorted[i]=newShifts[i];
    }
  for(i=0;i<currentCnt;i++)
    {
      currentShiftsSorted[i]=currentShifts[i];
    }

  if(newShifts==str->ChCShift || newShifts==str->ChHShift ||
     
     newShifts==str->HhLoShift || newShifts==str->HhHiShift)
    {
      qsort(newShiftsSorted,newCnt,sizeof(float),SP_Compare);
    }
  if(currentShifts==str->ChCShift || currentShifts==str->ChHShift ||
     
     currentShifts==str->HhLoShift || currentShifts==str->HhHiShift)
    {
      qsort(currentShiftsSorted,currentCnt,sizeof(float),SP_Compare);
    }


  /*  CC_MatchShifts(newShiftsSorted, currentShiftsSorted, newCnt, currentCnt, 0,
      0, match);*/

  /* If the shifts were 2D and thus resorted before the matching the matches
     have to be restructured to match the order of the unsorted lists. */
  /*  if(newShifts==str->ChCShift || newShifts==str->ChHShift ||
     
     newShifts==str->HhLoShift || newShifts==str->HhHiShift)
    {
      tempMatch=malloc(sizeof(int)*newCnt);
      for(i=0;i<newCnt;i++)
	{
	  if(fabs(newShiftsSorted[i]-newShifts[i])>0.00001)
	    {
  *//* If there is more than one entry with the same shift check
       which number we are looking for *//*
	      cnt=1;
	      while(i-cnt>=0 && fabs(newShiftsSorted[i]-newShiftsSorted[i-cnt])<0.00001)
		{
		  cnt++;
		}
	      found=0;
	      j=0;
	      while(found<cnt && j<newCnt)
		{
		  if(fabs(newShiftsSorted[i]-newShifts[j])<0.00001)
		    {
		      found++;
		    }
		  j++;
		}
	      j--;
	      *//* This should always be true *//*
	      if(j<newCnt && found==cnt)
		{
		  tempMatch[j]=match[i];
		}
	    }
	  else
	    {
	      tempMatch[i]=match[i];
	    }
	}
      for(i=0;i<newCnt;i++)
	{
	  match[i]=tempMatch[i];
	}
      free(tempMatch);
      }*/
  /*  if(currentShifts==str->ChCShift || currentShifts==str->ChHShift ||
     
     currentShifts==str->HhLoShift || currentShifts==str->HhHiShift)
    {
      tempMatch=malloc(sizeof(int)*currentCnt);
      for(i=0;i<currentCnt;i++)
	{
	  if(fabs(currentShiftsSorted[i]-currentShifts[i])>0.00001)
	    {
	      *//* If there is more than one entry with the same shift check
	      which number we are looking for *//*
	      cnt=1;
	      while(i-cnt>=0 && fabs(currentShiftsSorted[i]-currentShiftsSorted[i-cnt])<0.00001)
		{
		  cnt++;
		}
	      found=0;
	      j=0;
	      while(found<cnt && j<currentCnt)
		{
		  if(fabs(currentShiftsSorted[i]-currentShifts[j])<0.00001)
		    {
		      found++;
		    }
		  j++;
		}
	      j--;
						*//* This should always be true *//*
	      if(j<currentCnt && found==cnt)
		{
		  for(k=0;k<currentCnt;k++)
		    {
		      if(match[k]==i)
			{
			  tempMatch[k]=j;
			  k=currentCnt;
			}
		    }
		}
	    }
	  else
	    {
	      tempMatch[i]=match[i];
	    }
	}
      for(i=0;i<currentCnt;i++)
	{
	  match[i]=tempMatch[i];
	}
      free(tempMatch);
    }*/

/*
  for(i=0;i<newCnt;i++)
    {
      if(match[i]>=0)
	{
	  resonances=CC_FindResonances(shiftList, currentShifts[match[i]]);
	  j=0;
	  while(i-(j+1)>=0 && fabs(currentShifts[match[i]]-currentShifts[match[i]-(j+1)])<=0.00001)
	    {
	      j++;
	    }
	  
	  if(resonances)
	    {
	      if(ApiSet_Len(resonances)>j)
		{
		  resonance=ApiSet_Get(resonances,j);
		}
	      else if(ApiSet_Len(resonances)>0)
		{
		  resonance=ApiSet_Get(resonances,0);
		}
	      if(ApiSet_Len(resonances)>0 && resonance && !ApiObject_IsNone(resonance))
		{
		  match[i]=(int)ApiInteger_Get(Nmr_Resonance_GetSerial(resonance));
		}
	      else
		{
		  match[i]=-1;
		}
	    }
	  else
	    {
	      match[i]=-1;
	    }
	}
	}*/
  if(resonances && !ApiObject_IsNone(resonances))
    {
      ApiObject_Free(resonances);
    }
  if(newShiftsSorted)
    {
      free(newShiftsSorted);
    }
  if(currentShiftsSorted)
    {
      free(currentShiftsSorted);
    }
  return 1;
}

/* This function matches two sorted lists of shifts. If they are of the same
   length the matching is very straightforward 1:1. If either of the lists
   is longer than the other there is a recursive call to this function to
   find out which of the shifts in the longer list would be best not to match */
float CC_MatchShifts(float *newShifts, float *currentShifts, int newCnt, int currentCnt,
		     int newFinished, int currentFinished, int *match)
{
  int *tempMatch;
  int newRemaining, currentRemaining, i;
  float thisAssignment, useAssignment, skipAssignment;

  newRemaining=newCnt-newFinished;
  currentRemaining=currentCnt-currentFinished;
  if(newRemaining<=0||currentRemaining<=0)
    {
      return 0;
    }

  /* Match directly */
  thisAssignment=fabs(newShifts[newFinished]-currentShifts[currentFinished]);

  /* If the numbers are the same just match them granted that the match is
     at least reasonably good. Otherwise there will have to be recursive calls
     to see if a better match can be acquired. */
  if(newRemaining==currentRemaining/* && thisAssignment<1 */)
    {
      match[newFinished]=currentFinished;
      while(newRemaining>1)
	{
	  newFinished++;
	  currentFinished++;
	  newRemaining--;
	  currentRemaining--;
	  thisAssignment+=fabs(newShifts[newFinished]-currentShifts[currentFinished]);
	  match[newFinished]=currentFinished;
	}
      return(thisAssignment);
    }

  tempMatch=malloc(newCnt*sizeof(int));
  for(i=0;i<newCnt;i++)
    {
      tempMatch[i]=match[i];
    }
  match[newFinished]=currentFinished;

  if(newRemaining>currentRemaining)
    {
      /* See if it's good to use the direct match from above */
      useAssignment=CC_MatchShifts(newShifts, currentShifts, newCnt, currentCnt,
				   newFinished+1, currentFinished+1, match)+thisAssignment;
      /* See if it's better to skip one position in newShifts */
      skipAssignment=CC_MatchShifts(newShifts, currentShifts, newCnt, currentCnt,
				    newFinished+1, currentFinished, tempMatch);
      if(useAssignment<=skipAssignment)
	{
	  free(tempMatch);
	  return(useAssignment);
	}
      else
	{
	  for(i=0;i<newCnt;i++)
	    {
	      match[i]=tempMatch[i];
	    }
	  free(tempMatch);
	  return(skipAssignment);
	}
    }

  if(newRemaining<currentRemaining)
    {
      /* See if it's good to use the direct match from above */
      useAssignment=CC_MatchShifts(newShifts, currentShifts, newCnt, currentCnt,
				   newFinished+1, currentFinished+1, match)+thisAssignment;
      /* See if it's better to skip one position in currentShifts */
      skipAssignment=CC_MatchShifts(newShifts, currentShifts, newCnt, currentCnt,
				    newFinished, currentFinished+1, tempMatch);
      if(useAssignment<=skipAssignment)
	{
	  free(tempMatch);
	  return(useAssignment);
	}
      else
	{
	  for(i=0;i<newCnt;i++)
	    {
	      match[i]=tempMatch[i];
	    }
	  free(tempMatch);
	  return(skipAssignment);
	}
    }

  /* If there is an equal number of shifts in both lists, but they don't seem to match
     very well see if a better match can be acquired without matching directly. */
  if(thisAssignment>=1)
    {
      /* See if it's good to use the direct match from above */
      useAssignment=CC_MatchShifts(newShifts, currentShifts, newCnt, currentCnt,
				   newFinished+1, currentFinished+1, match)+thisAssignment;
      /* Check in which list there could potentially be a "gap" */
      if(newShifts[newFinished]-currentShifts[currentFinished]>0)
	{
	  /* See if it's better to skip one position in newShifts */
	  skipAssignment=CC_MatchShifts(newShifts, currentShifts, newCnt, currentCnt,
					newFinished+1, currentFinished, tempMatch);
	}
      else
	{
	  /* See if it's better to skip one position in currentShifts */
	  skipAssignment=CC_MatchShifts(newShifts, currentShifts, newCnt, currentCnt,
					newFinished, currentFinished+1, tempMatch);
	}
	
      if(useAssignment<=skipAssignment)
	{
	  free(tempMatch);
	  return(useAssignment);
	}
      else
	{
	  for(i=0;i<newCnt;i++)
	    {
	      match[i]=tempMatch[i];
	    }
	  free(tempMatch);
	  return(skipAssignment);
	}
    }
  

  return 0; /* This line should never be executed, but the compiler wants a return in the end of a non-void function. */
}

/* Sets the resonanceGroup of the resonance to the first resonanceGroup of
   the residue */
int CC_SetResonanceGroup(Nmr_NmrProject *nmrProj, Nmr_Resonance resonance, Mols_Residue residue)
{
  Nmr_ResonanceGroup resonanceGroup;
  ApiSet resonanceGroupCollection;
  ApiMap parameters;
  ApiString string;
  int numResonanceGroups;

  /* Find out which resonanceGroups this residue belongs to.
     In most cases each residue should only be in one resonanceGroup.
     Therefore we assume that we can set the resonanceGroup of the
     resonance to the first in the residue. This might have to be
     adjusted. */

  resonanceGroupCollection=Mols_Residue_GetResonanceGroups(residue);
  if(resonanceGroupCollection)
    {
      numResonanceGroups=ApiSet_Len(resonanceGroupCollection);
    }
  else
    {
      numResonanceGroups=0;
    }
  if(numResonanceGroups>0)
    {
      resonanceGroup=ApiSet_Get(resonanceGroupCollection,0);
      printf("ResonanceGroup: %s (%d)\n", ApiString_Get(Nmr_ResonanceGroup_GetCcpCode(resonanceGroup)), ApiInteger_Get(Nmr_ResonanceGroup_GetSerial(resonanceGroup)));
      ApiList assNames = Nmr_Resonance_GetAssignNames(resonance);
      int i;
      for (i=0;i<ApiList_Len(assNames);i++)
	{
	  printf("Assignment name %d: %s\n", i, ApiString_Get(ApiList_Get(assNames,i)));
	}
      Nmr_Resonance_SetResonanceGroup(resonance,resonanceGroup);
      ApiObject_Free(resonanceGroup);
    }
  else
    {
      parameters=ApiMap_New();
      resonanceGroup=Nmr_NmrProject_NewResonanceGroup(*nmrProj, parameters);
      ApiObject_Free(parameters);
      Nmr_ResonanceGroup_SetResidue(resonanceGroup, residue);
      string=Mols_Residue_GetCcpCode(residue);
      Nmr_ResonanceGroup_SetCcpCode(resonanceGroup, string);
      ApiObject_Free(string);
      string=Mols_Residue_GetMolType(residue);
      Nmr_ResonanceGroup_SetMolType(resonanceGroup, string);
      ApiObject_Free(string);
      Nmr_Resonance_SetResonanceGroup(resonance, resonanceGroup);
      /*      Mols_Residue_AddResonanceGroup(residue, resonanceGroup);*/
      ApiObject_Free(resonanceGroup);
    }
  if(resonanceGroupCollection && !ApiObject_IsNone(resonanceGroupCollection))
    {
      ApiObject_Free(resonanceGroupCollection);
    }
  if(!Nmr_ResonanceGroup_CheckAllValid(resonanceGroup, ApiBoolean_False()))
    {
      printf("ResonanceGroup not valid. Line %d.\n", __LINE__);
      printRaisedException();
      return 0;
    }
  if(!Mols_Residue_CheckAllValid(residue, ApiBoolean_False()))
    {
      printf("Residue not valid. Line %d.\n", __LINE__);
      printRaisedException();
      return 0;
    }
  return 1;
}

/* This function loads chemical shifts from resonances assigned to atoms from the CCPN project and
   sets the chemical shifts of the corresponding atoms in *str */
int CC_LoadAssignments(Nmr_NmrProject *proj, struct BU_Struct *str)
{
  ApiSet resonances, atoms, shifts, atomSets;
  ApiSetIterator resonanceIterator, atomIterator, atomSetIterator;
  ApiInteger id;
  ApiString string;
  ApiFloat shiftValue;
  Nmr_Resonance resonance;
  Nmr_ResonanceSet resonanceSet;
  Nmr_AtomSet atomSet;
  Nmr_Shift shift;
  Mols_Atom atom;
  Mols_Residue residue;
  Mols_Chain chain;
  struct BU_Unit *unit;
  struct BU_ShiftToAtom shiftToAtom;
  int i, j, atomSetCnt, atomNr, protonNumber;
  char name[16];
  float value, tempShift;

  resonances=Nmr_NmrProject_GetResonances(*proj);
  if(!resonances)
    {
      printRaisedException();
      printf("Cannot find resonances. Line %d.\n", __LINE__);
      return(PA_ERR_FAIL);
    }

  /* Loop through all resonances */
  resonanceIterator=ApiSet_Iterator(resonances);
  while((resonance=ApiSetIterator_Next(resonanceIterator)))
    {
      /* Get the chemical shift of this resonance */
      shifts=Nmr_Resonance_GetShifts(resonance);
      shift=ApiSet_Get(shifts,0);
      ApiObject_Free(shifts);
      /* If this is a resonance without a shift just continue to the next one. */
      if(!shift)
	{
	  continue;
	}
      shiftValue=Nmr_Shift_GetValue(shift);
      value=ApiFloat_Get(shiftValue);
      ApiObject_Free(shiftValue);
      ApiObject_Free(shift);

      resonanceSet=Nmr_Resonance_GetResonanceSet(resonance);
      if(!resonanceSet || ApiObject_IsNone(resonanceSet))
	{
	  ApiObject_Free(resonance);
	  continue;
	}
      atomSets=Nmr_ResonanceSet_GetAtomSets(resonanceSet);
      if(atomSets && !ApiObject_IsNone(atomSets))
	{
	  atomSetCnt=ApiSet_Len(atomSets);
	}
      else
	{
	  atomSetCnt=0;
	}
      atomSetIterator=ApiSet_Iterator(atomSets);

      /* Loop through all atomSets */
      for(j=0;j<atomSetCnt;j++)
	{
	  atomSet=ApiSetIterator_Next(atomSetIterator);
      
	  atoms=Nmr_AtomSet_GetAtoms(atomSet);

	  /* Loop through all atoms */
	  atomIterator=ApiSet_Iterator(atoms);
	  while((atom=ApiSetIterator_Next(atomIterator)))
	    {
	      residue=Mols_Atom_GetResidue(atom);
	      /* Check that this atom is from the right chain - otherwise continue to the next atom */
	      chain=Mols_Residue_GetChain(residue);
	      string=Mols_Chain_GetCode(chain);
	      ApiObject_Free(chain);
	      if(strcmp(str->CcpnChainCode, ApiString_Get(string))!=0)
		{
		  ApiObject_Free(string);
		  ApiObject_Free(residue);
		  ApiObject_Free(atom);
		  continue;
		}
	      ApiObject_Free(string);
	      id=Mols_Residue_GetSeqCode(residue);
	      /* In CASPER units are named from 'a', but in CCPN from 1. Translate it */
	      atomNr=-1;

	      /* Get the CASPER unit corresponding to this CCPN residue */
	      unit=BU_FindCcpnUnit((struct Node *)&str->Units, ApiInteger_Get(id));
	      ApiObject_Free(id);
	      if(!unit)
		{
		  printf("Cannot find unit. Line %d.\n", __LINE__);
		  return(PA_ERR_FAIL);
		}
	      string=Mols_Atom_GetName(atom);
	      strncpy(name,ApiString_Get(string),15);
	      ApiObject_Free(string);

	      /* Find the atom in the CASPER structure */
	      if(name[0]=='C' || name[0]=='H')
		{
		  atomNr=TY_FindAtom(unit->Shifts.Type,name+1);
		}
	      else
		{
		  atomNr=TY_FindAtom(unit->Shifts.Type,name);
		}
	      if(strlen(name)==2)
		{
		  protonNumber=0;
		}
	      else if(name[0]=='H')
		{
		  if(name[2]=='2' && unit->Shifts.Type->Atom[atomNr].HCnt>1)
		    {
		      protonNumber=1;
		    }
		  else
		    {
		      protonNumber=0;
		    }
		}
	      /* TODO: Add methods to find e.g. atoms in substituents */
	      if(atomNr==-1)
		{
		  printf("Cannot find atom %s in unit %s. Line %d.\n", name, unit->Node.Name, __LINE__);
		  return(PA_ERR_FAIL);
		}
	      /* Set the chemical shift of the atom - either 13C or 1H */
	      switch(name[0])
		{
		case 'C':
		  /* First check that this has not already been assigned (another resonance in the
		     same resonance set - should only be of use for hydrogens actually. */
		  if(unit->Shifts.C[atomNr]-value<0.1)
		    {
		      unit->Shifts.C[atomNr]=value;
		      /* Stop after assigning one of the atoms in the atom set. Other resonances 
			 should match the other one */
		      j=atomSetCnt;
		      str->CShiftAtom[str->CCnt].unit=unit;
		      str->CShiftAtom[str->CCnt].atom[0]=atomNr;
		      str->CShiftAtom[str->CCnt].atom[1]=0;
		      str->CShift[str->CCnt++]=value;
		    }
		  break;
		case 'H':
		  /* First check that this has not already been assigned (another resonance in the
		     same resonance set. */
		  if(unit->Shifts.H[atomNr][protonNumber]<0.1)
		    {
		      unit->Shifts.H[atomNr][protonNumber]=value;
		      /* Stop after assigning one of the atoms in the atom set. Other resonances 
			 should match the other one */
		      j=atomSetCnt;
		      str->HShiftAtom[str->HCnt].unit=unit;
		      str->HShiftAtom[str->HCnt].atom[0]=atomNr;
		      str->HShiftAtom[str->HCnt].atom[1]=protonNumber;
		      str->HShift[str->HCnt++]=value;
		      if(unit->Shifts.Type->Atom[atomNr].HCnt>1)
			{
			  /* If there are two protons on one carbon the first one should have lower
			     chemical shifts in CASPER. */
			  if(unit->Shifts.H[atomNr][0]>unit->Shifts.H[atomNr][1] && 
			     unit->Shifts.H[atomNr][1]>0.1)
			    {
			      tempShift=unit->Shifts.H[atomNr][0];
			      unit->Shifts.H[atomNr][0]=unit->Shifts.H[atomNr][1];
			      unit->Shifts.H[atomNr][1]=tempShift;
			    }
			}
		    }
		  break;
		default:
		  printf("Wrong atom type. Line %d.\n", __LINE__);
		  return(PA_ERR_FAIL);
		}

	      ApiObject_Free(residue);
	      ApiObject_Free(atom);
	    }
	  ApiObject_Free(atomSet);
	}
      ApiObject_Free(atoms);
      ApiObject_Free(atomSets);
      ApiObject_Free(resonanceSet);
      ApiObject_Free(resonance);
    }
  ApiObject_Free(resonances);

  /* Now that all chemical shifts are set in the CASPER units loop through them to set the atoms
     correctly in the shift lists. The reason why this is not done in the loop above is that it
     is good to make sure both C and H shifts are set so that assignments are less ambiguous. */
  for(unit=(struct BU_Unit *)str->Units.Head.Succ; unit->Node.Succ!=NULL;
      unit=(struct BU_Unit *)unit->Node.Succ)
    {
      for(i=0; i<unit->Shifts.Type->HeavyCnt; i++)
	{
	  for(j=0; j<unit->Shifts.Type->Atom[i].HCnt; j++)
	    {
	      if(unit->Shifts.C[i]>0 || unit->Shifts.H[i][j]>0)
		{
		  shiftToAtom.unit=unit;
		  shiftToAtom.atom[0]=i;
		  shiftToAtom.atom[1]=j;
		  SP_AssignOtherShiftsToAtom(str, unit->Shifts.C[i], unit->Shifts.H[i][j], &shiftToAtom, 0, 0);
		}
	    }
	}
    }

  return (PA_ERR_OK);
}

/* Merge shifts from the same shiftlist in a resonance */
void CC_MergeShiftsInResonance(Nmr_NmrProject *nmrProj, Nmr_Resonance resonance)
{
  ApiSet shiftLists, shifts;
  Nmr_Shift shift, secShift;
  Nmr_ShiftList shiftList, secShiftList;
  int i, j, k, numShiftLists, numShifts;

  {
    ApiObject args[] = { *nmrProj, NULL };
    shiftLists=callModuleFunction("ccpnmr.analysis.util.CasperBasic", "getShiftLists", args);
  }
  numShiftLists=ApiSet_Len(shiftLists);
  
  for(i=0;i<numShiftLists;i++)
    {
      shifts=Nmr_Resonance_GetShifts(resonance);
      numShifts=ApiSet_Len(shifts);
      if(numShifts>i+1)
	{
	  for(j=i;j<numShifts;j++)
	    {
	      shift=ApiSet_Get(shifts,j);
	      for(k=i+1;k<numShifts;k++)
		{
		  secShift=ApiSet_Get(shifts,k);
		  shiftList=Nmr_Shift_GetParentList(shift);
		  secShiftList=Nmr_Shift_GetParentList(secShift);
		  if(shiftList==secShiftList)
		    {
		      ApiObject args[] = { secShift, shift, NULL };
		      shift=callModuleFunction("ccpnmr.analysis.util.CasperBasic", "mergeObjects", args);
		      args[0]=shift;
		      args[1]=NULL;
		      callModuleFunction("ccpnmr.analysis.util.CasperBasic", "averageShiftValue", args);
		    }
		  ApiObject_Free(secShift);
		  ApiObject_Free(shiftList);
		  ApiObject_Free(secShiftList);
		}
	      ApiObject_Free(shift);
	    }
	}
      ApiObject_Free(shifts);
    }
}

/* Merge a specific with shifts from the same shiftlist in a resonance */
void CC_MergeShiftWithShiftsInResonance(Nmr_NmrProject *nmrProj, Nmr_Shift shift, Nmr_Resonance resonance)
{
  ApiSet shifts;
  Nmr_Shift secShift;
  Nmr_ShiftList shiftList, secShiftList;
  int i, numShifts;

  shiftList=Nmr_Shift_GetParentList(shift);
  
  shifts=Nmr_Resonance_GetShifts(resonance);
  numShifts=ApiSet_Len(shifts);

  for(i=0;i<numShifts;i++)
    {
      secShift=ApiSet_Get(shifts,i);
      if(shift!=secShift)
	{
	  secShiftList=Nmr_Shift_GetParentList(secShift);
	  if(shiftList==secShiftList)
	    {
	      ApiObject args[] = { secShift, shift, NULL };
	      shift=callModuleFunction("ccpnmr.analysis.util.CasperBasic", "mergeObjects", args);
	      args[0]=shift;
	      args[1]=NULL;
	      callModuleFunction("ccpnmr.analysis.util.CasperBasic", "averageShiftValue", args);
	    }
	  ApiObject_Free(secShiftList);
	}
      ApiObject_Free(secShift);
    }
  ApiObject_Free(shifts);
}

/* Generate a pdb description of the structure *str. The coordinates are based on the chemcomp coordinates and the linkages
   between the atoms are based on the intraresidue atom bonds specified in the chemcomp as well as the interresidue linkages
   specified in the molecule. */
int CC_CcpnPdb(Impl_MemopsRoot *ccpnProj, char *str, char *filename)
{
  ApiString name, sourceName, atomName, residueName, chainId, element;
  Mols_MolSystem molSystem;
  Mols_Residue molResidue;
  Mols_Chain chain;
  Coor_StructureEnsemble structureEnsemble;
  Coor_Model model;
  Coor_Coord coord;
  Coor_Atom atom;
  Coor_Residue residue;
  ApiList chains;
  ApiSet models, coords;
  ApiFloat x, y, z;
  ApiInteger seqCode;
  struct AtomInfo *atoms;
  int i, j, numChains, numCoords;
  FILE *file;
  

  name=ApiString_New(str);
  molSystem=Impl_MemopsRoot_FindFirstMolSystem_keyval1(*ccpnProj, "name", name);

  if(!molSystem || ApiObject_IsNone(molSystem))
    {
      if(molSystem)
	{
	  ApiObject_Free(molSystem);
	}
      Error(PA_ERR_FAIL,"Cannot find molSystem");
    }

  file=fopen(filename, "w");
  if(!file)
    {
      Error(PA_ERR_FAIL,"Cannot open file for writing");
    }
  
  sourceName=ApiString_New("EUROCarbDB");

  chains=Mols_MolSystem_SortedChains(molSystem);
  numChains=ApiList_Len(chains);

  /* Usually only one chain is used, but more than one should be possible as well */
  for(i=0;i<numChains;i++)
    {
      chain=ApiList_Get(chains,i);
      chainId=Mols_Chain_GetCode(chain);

      /* Create the structureEnsemble which contains atom coordinates */
      ApiObject args[]= { chain, /*sourceName, */NULL };
      structureEnsemble = callModuleFunction("ccp.util.Molecule", "createMolStructureFromChemCompCoords", args);
      if(!structureEnsemble || ApiObject_IsNone(structureEnsemble))
	{
	  if(structureEnsemble)
	    {
	      ApiObject_Free(structureEnsemble);
	    }
	  ApiObject_Free(chain);
	  ApiObject_Free(chains);
	  ApiObject_Free(sourceName);
	  ApiObject_Free(molSystem);
	  ApiObject_Free(name);
	  printf("Could not generate the coordinates for the structure. Line %d.\n", __LINE__);
	  printRaisedException();
	  Error(PA_ERR_FAIL, "Cannot generate PDB file");
	}
      /* Use only the first model - could be increased later on, but should be enough in CASPER */
      models=Coor_StructureEnsemble_GetModels(structureEnsemble);
      model=ApiSet_Get(models,0);
      ApiObject_Free(models);

      coords=Coor_Model_GetCoords(model);
      numCoords=ApiSet_Len(coords);
      atoms=malloc(numCoords*sizeof(struct AtomInfo));
      /* Populate the array of atom information */
      for(j=0; j<numCoords; j++)
	{
	  coord=ApiSet_Get(coords,j);
	  atom=Coor_Coord_GetAtom(coord);

	  x=Coor_Coord_GetX(coord);
	  y=Coor_Coord_GetY(coord);
	  z=Coor_Coord_GetZ(coord);
	  atomName=Coor_Atom_GetName(atom);
	  residue=Coor_Atom_GetResidue(atom);
	  molResidue=Coor_Residue_GetResidue(residue);
	  residueName=Mols_Residue_GetCcpCode(molResidue);
	  seqCode=Coor_Residue_GetSeqCode(residue);
	  element=Coor_Atom_GetElementSymbol(atom);

	  atoms[j].atom=atom;
	  strncpy(atoms[j].name, ApiString_Get(atomName), 15);
	  strncpy(atoms[j].element, ApiString_Get(element), 7);
	  strncpy(atoms[j].residueName, ApiString_Get(residueName)+1, 15); 
	  strncpy(atoms[j].chainId, ApiString_Get(chainId), 3);
	  atoms[j].seqCode=ApiInteger_Get(seqCode);
	  atoms[j].x=ApiFloat_Get(x);
	  atoms[j].y=ApiFloat_Get(y);
	  atoms[j].z=ApiFloat_Get(z);
	  ApiObject_Free(atomName);
	  ApiObject_Free(residue);
	  ApiObject_Free(molResidue);
	  ApiObject_Free(residueName);
	  ApiObject_Free(x);
	  ApiObject_Free(y);
	  ApiObject_Free(z);
	}

      /* PDB output should be sorted according to residues and atoms in each residue */
      qsort(atoms, numCoords, sizeof (struct AtomInfo), CC_AtomsCmp);

      /* Print the atom info, including coordinates, residue and element */
      for(j=0; j<numCoords; j++)
	{
	  fprintf(file, "%-6.6s%5d %-4.4s%1s%-3.3s %1s%4d%1s   %8.3f%8.3f%8.3f%6.2f%6.2f      %4s%2s%2s\n",
		 "HETATM", j+1, atoms[j].name, "", atoms[j].residueName, atoms[j].chainId,
		 atoms[j].seqCode, " ", atoms[j].x, atoms[j].y, atoms[j].z, 1.00, 0.00,
		 "", atoms[j].element, "");
	}

      /* Print the linkages for each atom */
      for(j=0; j<numCoords; j++)
	{
	  CC_PrintPdbLinkages(atoms, j, numCoords, file);
	}

      ApiObject_Free(coords);
      ApiObject_Free(model);
      ApiObject_Free(models);
      ApiObject_Free(structureEnsemble);
      ApiObject_Free(chainId);
      ApiObject_Free(chain);
      free(atoms);
    }
  fprintf(file, "END\n");

  fclose(file);

  ApiObject_Free(chains);
  ApiObject_Free(sourceName);
  ApiObject_Free(molSystem);
  ApiObject_Free(name);

  return 1;
}

/* A comparison function for sorting the AtomInfo array.
   Primarily sorts on residue seqCode and secondarily on atom name. */
int CC_AtomsCmp(const void *a, const void *b)
{
  struct AtomInfo *aI = (struct AtomInfo *)a;
  struct AtomInfo *bI = (struct AtomInfo *)b;

  if(aI->seqCode < bI->seqCode)
    {
      return -1;
    }
  else if(aI->seqCode > bI->seqCode)
    {
      return 1;
    }

  return strcmp(aI->name, bI->name);
}

/* A comparison function for sorting integers */
int CC_IntCmp(const void *a, const void *b)
{
  const int *ia = (const int *)a;
  const int *ib = (const int *)b;
  return *ia  - *ib; 
}

/* Prints the connectivities of the atom n in the AtomInfo array *atoms to a pdb file */
void CC_PrintPdbLinkages(struct AtomInfo *atoms, const int n, const int max, FILE *file)
{
  Coor_Atom coordAtom;
  Mols_Atom mAtom;
  Mols_Residue molSysResidue;
  Mole_MolResidue residue, otherResidue;
  Chem_ChemAtom cAtom, firstBondAtom, secondBondAtom;
  Chem_ChemBond cBond;
  Mole_MolResLinkEnd thisLinkEnd, otherLinkEnd;
  Mole_MolResLink molResLink;
  ApiSet cBonds, bondAtoms, linkEnds, linkLinkEnds;
  ApiString ApiLinkCode;
  ApiInteger ApiSeqCode;
  char linkCode[16];
  int i, nr, linkages[6], numLinkages=0, numCBonds, numLinkEnds, seqCode;

  coordAtom=atoms[n].atom;
  cAtom=Coor_Atom_GetChemAtom(coordAtom);
  mAtom=Coor_Atom_GetAtom(coordAtom);
  molSysResidue=Mols_Atom_GetResidue(mAtom);
  residue=Mols_Residue_GetMolResidue(molSysResidue);

  /* First find the interresidue linkages */
  linkEnds=Mole_MolResidue_GetMolResLinkEnds(residue);
  numLinkEnds=ApiSet_Len(linkEnds);
  for(i=0; i<numLinkEnds; i++)
    {
      thisLinkEnd=ApiSet_Get(linkEnds, i);
      ApiLinkCode=Mole_MolResLinkEnd_GetLinkCode(thisLinkEnd);
      strcpy(linkCode, ApiString_Get(ApiLinkCode));
      /* If the atom we are looking for is in this linkEnd */
      if(CC_FindAtomNumberFromLinkCode(atoms, atoms[n].seqCode, strtok(linkCode,"_"), max)==n)
	{
	  molResLink=Mole_MolResLinkEnd_GetMolResLink(thisLinkEnd);
	  linkLinkEnds=Mole_MolResLink_GetMolResLinkEnds(molResLink);
	  otherLinkEnd=ApiSet_Get(linkLinkEnds,1);
	  if(otherLinkEnd==thisLinkEnd)
	    {
	      otherLinkEnd=ApiSet_Get(linkLinkEnds,0);
	    }
	  ApiLinkCode=Mole_MolResLinkEnd_GetLinkCode(otherLinkEnd);
	  strcpy(linkCode, ApiString_Get(ApiLinkCode));
	  otherResidue=Mole_MolResLinkEnd_GetMolResidue(otherLinkEnd);
	  ApiSeqCode=Mole_MolResidue_GetSeqCode(otherResidue);
	  seqCode=ApiInteger_Get(ApiSeqCode);
	  nr=CC_FindAtomNumberFromLinkCode(atoms, seqCode, strtok(linkCode,"_"), max);
	  if(nr>=0)
	    {
	      linkages[numLinkages++]=nr;
	    }
	}
    }

  /* Then handle the intraresidue linkages (how the atoms in the residue are linked together) */
  cBonds=Chem_ChemAtom_GetChemBonds(cAtom);
  numCBonds=ApiSet_Len(cBonds);
  for(i=0; i<numCBonds; i++)
    {
      cBond=ApiSet_Get(cBonds, i);
      bondAtoms=Chem_ChemBond_GetChemAtoms(cBond);
      firstBondAtom=ApiSet_Get(bondAtoms,0);
      secondBondAtom=ApiSet_Get(bondAtoms,1);
      nr=0;
      if(firstBondAtom==cAtom)
	{
	  nr=CC_FindAtomNumber(atoms, secondBondAtom, 0, max);
	  while(nr!=-1 && nr<max && atoms[n].seqCode!=atoms[nr].seqCode)
	    {
	      nr=CC_FindAtomNumber(atoms, secondBondAtom, nr+1, max);
	    }
	  if(nr>=0)
	    {
	      linkages[numLinkages++]=nr;
	    }
	}
      else
	{
	  nr=CC_FindAtomNumber(atoms, firstBondAtom, 0, max);
	  while(nr!=-1 && nr<max && atoms[n].seqCode!=atoms[nr].seqCode)
	    {
	      nr=CC_FindAtomNumber(atoms, firstBondAtom, nr+1, max);
	    }
	  if(nr>=0)
	    {
	      linkages[numLinkages++]=nr;
	    }
	}
    }
  if(numLinkages>0)
    {
      /* Sort the linkages and print them */
      qsort(linkages, numLinkages, sizeof(int), CC_IntCmp);
      fprintf(file, "%6s%5d", "CONECT", n+1);
      for(i=0; i<numLinkages; i++)
	{
	  fprintf(file, "%5d", linkages[i]+1);
	}
      fprintf(file, "\n");
    }
}

/* Looks for the Chem_Atom atom in the AtomInfo array *atoms and returns the index. It starts
   searching at index i and max specifies the number of entries in the array */
int CC_FindAtomNumber(struct AtomInfo *atoms, Chem_ChemAtom atom, int i, const int max)
{
  Chem_ChemAtom cAtom;

  for(; i<max; i++)
    {
      cAtom=Coor_Atom_GetChemAtom(atoms[i].atom);
      if(cAtom==atom)
	{
	  return i;
	}
    }
  return -1;
}

/* Looks for an atom name in the AtomInfo *atoms that matches *linkCode and the residue specified
   by seqCode. The number of entries in the array is specified by the integer max. */
int CC_FindAtomNumberFromLinkCode(struct AtomInfo *atoms, int seqCode, char *linkCode, const int max)
{
  int i;

  for(i=0; i<max; i++)
    {
      if(atoms[i].seqCode==seqCode && strcmp(atoms[i].name, linkCode)==0)
	{
	  return i;
	}
    }

  return -1;
}

/* This function is just a replacement for ApiCollection_Get, which returned
   the object of a specified index number. Such a function does not exist for
   ApiSet - but this function works the same way.
   NOTE: This function is convenient, but it is not quick since it iterates all the way
   until the wanted instance is found. It is not suitable for use in for loops - there
   it is better to use the iterator in the loop instead of calling this function
   for each iteration. */
PyObject* ApiSet_Get(ApiSet set, int n)
{
  ApiSetIterator iter;
  PyObject *obj;
  int i;

  iter=ApiSet_Iterator(set);
  obj=ApiSetIterator_Next(iter);

  for(i=0; i<n && obj; i++)
    {
      ApiObject_Free(obj);
      obj=ApiSetIterator_Next(iter);
    }
  return obj;
}

PyObject* ApiSet_AddSet(ApiSet set1, ApiSet set2)
{
  ApiObject obj;
  ApiSetIterator iter;

  iter=ApiSet_Iterator(set2);
  while((obj = ApiSetIterator_Next(iter)))
    {
      ApiSet_Add(set1, obj);
      ApiObject_Free(obj);
    }
  return set1;
}
