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

#ifndef _CCPN_H
#define _CCPN_H

#include "ccp.h"

#include "build.h"
#include "setup.h"
#include "residue.h"
#include "node.h"

#ifndef S_IFDIR
#define S_IFDIR  0040000
#endif

extern Impl_MemopsRoot Global_CcpnProject;
extern Nmr_NmrProject Global_NmrProject;
extern int projectValid;

struct AtomInfo {
  Coor_Atom atom;
  char name[16];
  char element[8];
  char residueName[16];
  char chainId[4];
  int seqCode;
  float x;
  float y;
  float z;
};

int CC_LoadProject(Impl_MemopsRoot *ccpnProj, char *projectFileName);
int CC_SaveProject(Impl_MemopsRoot *ccpnProj, char *projectFileName);
int CC_GetNmrProject(Impl_MemopsRoot *ccpnProj, Nmr_NmrProject *nmrProj);
int CC_GetNmrProject_Named(Impl_MemopsRoot *ccpnProj,
			   Nmr_NmrProject *nmrProj,
			   char *wanted_name);
ApiSet CC_GetExpOfType(Nmr_NmrProject *nmrProj, char *type);
int CC_GetProcSpectrumOfDim(Nmr_Experiment *experiment, int dim, Nmr_DataSource *spectrum);
int CC_GetFirstPeakList(Nmr_DataSource *spectrum, Nmr_PeakList *peakList);
int CC_GetPeaks(Nmr_Experiment *experiment, float *firstdim, float *seconddim);
int CC_GetShifts(Nmr_NmrProject *nmrProj, char *isotope, float *values);
int CC_PutPeaks(Nmr_NmrProject *nmrProj, Nmr_DataSource *spectrum, 
		float firstdim[], float seconddim[], int n, int type,
		int addnew, struct BU_Struct *BU_Struct);
Nmr_Peak CC_FindPeak(Nmr_PeakList peakList, float firstdim, float seconddim, 
		     int numDim, float tolerance, ApiInteger *apiIntFirstDim, 
		     ApiInteger *apiIntSecondDim);
ApiSet CC_FindPeaks(ApiSet peakLists, float firstdim, float seconddim, 
		    int numDim, float tolerance, ApiInteger *apiIntFirstDim, 
		    ApiInteger *apiIntSecondDim);
int CC_LoadSpectra(Nmr_NmrProject *nmrProj, unsigned int mode,
		   struct BU_Struct *spectrum);
Nmr_DataSource CC_NewSpectrum(Impl_MemopsRoot *ccpnProj, Nmr_NmrProject *nmrProj,
			      int type);
int CC_AllPeaksInSpectra(Impl_MemopsRoot *ccpnProj, Nmr_NmrProject *nmrProj,
			    struct BU_Struct *BU_Struct);
Nmr_Shift CC_NewShift(Nmr_ShiftList shiftList, Nmr_Peak peak,
		      ApiSet peakContribs, Nmr_PeakDim peakDim,
		      Nmr_Resonance resonance, float value);
int CC_FindResonance(Nmr_ShiftList shiftList, Nmr_Peak peak,
		     Nmr_PeakContrib peakContrib, Nmr_PeakDim peakDim,
		     float value);
ApiSet CC_FindResonances(Nmr_ShiftList *shiftList, float value);
void CC_SetPeakValues(Nmr_NmrProject *nmrProj, Nmr_ShiftList shiftList,
		      Nmr_Peak peak, Nmr_PeakDim peakDim,
		      Nmr_PeakContrib peakContrib, Nmr_DataDimRef dataDimRef,
		      Nmr_FreqDataDim dataDim, ApiMap resonanceParameters,
		      float value, int match, int addnew);
/*int CC_MergeSameResonances(Nmr_NmrProject *nmrProject, Nmr_Resonance *resonance);*/
int CC_ResiduetoUnit(Impl_MemopsRoot *ccpnProject, struct BU_Struct *BU_Struct, int multiBuildFlag);
int CC_ProteinToBU_Struct(Mols_Residue *residue, struct BU_Struct *BU_Struct);
Mols_MolSystem CC_MakeMoleculeFromBU_Struct(Impl_MemopsRoot *ccpnProject,
					    struct BU_Struct *inp, char *name);
int CC_ProteinToCCPNMolecule(Impl_MemopsRoot *ccpnProject, Mole_Molecule *molecule, struct BU_Unit *unit, int n);
int CC_CreateResonanceGroups(Impl_MemopsRoot *ccpnProject, Nmr_NmrProject *nmrProject, Mols_MolSystem molSystem, char *chainCode);
int CC_MakeSimulationSetupFromMolecules(Impl_MemopsRoot *ccpnProject, struct SE_Simulation *SE_Sim);
struct RE_Residue* CC_FindCcpCodeInResidueList(struct List *residues, char *ccpCode, char *stereo);
Mols_Residue CC_FindResidueNr(Impl_MemopsRoot *ccpnProject, char *molSystemName, char *chainCode, int nr);
ApiSet CC_FindAtomsNamed(Mols_Residue residue, char *name, char *type);
ApiSet CC_FindShifts(Nmr_NmrProject *nmrProj, float value);
ApiSet CC_FindShiftsFromPeaks(Nmr_NmrProject *nmrProj, ApiSet peaks, int wantedDim);
int CC_AssignPeakToAtoms(Nmr_NmrProject *nmrProj, ApiSet atoms, float firstdim, float seconddim, int assignDim, const char mode);
/*int CC_AssignShiftToAtoms(Nmr_NmrProject *nmrProj, ApiSet atoms, float value, const char mode);*/
ApiSet CC_GetAtomSetCollectionOfAtom(Nmr_NmrProject *nmrProj, Mols_Atom atom, Mols_Residue residue);
int CC_AssignSimulatedShifts(Impl_MemopsRoot *ccpnProject, Nmr_NmrProject *nmrProject, struct BU_Struct *str, Nmr_ShiftList shiftList);
Nmr_Shift CC_CreateShiftAssignedToAtoms(Nmr_NmrProject *nmrProject, Nmr_ShiftList shiftList, float value, ApiSet atoms);
Nmr_AtomSet CC_CreateAtomSet(Nmr_NmrProject *nmrProject, ApiSet atoms, char *name);
int CC_AddResonancesToPeaks(int *match, Nmr_ShiftList *shiftList,
			    struct BU_Struct *str, float *newShifts,
			    int newCnt, unsigned int mode);
float CC_MatchShifts(float *newShifts, float *currentShifts, int newCnt,
		     int currentCnt, int newFinished, int currentFinished,
		     int *match);
int CC_SetResonanceGroup(Nmr_NmrProject *nmrProj, Nmr_Resonance resonance, Mols_Residue residue);
int CC_LoadAssignments(Nmr_NmrProject *proj, struct BU_Struct *str);
void CC_MergeShiftsInResonance(Nmr_NmrProject *nmrProj, Nmr_Resonance resonance);
void CC_MergeShiftWithShiftsInResonance(Nmr_NmrProject *nmrProj, Nmr_Shift shift, Nmr_Resonance resonance);
int CC_CcpnPdb(Impl_MemopsRoot *ccpnProj, char *str, char *filename);
int CC_AtomsCmp(const void *a, const void *b);
int CC_IntCmp(const void *a, const void *b);
void CC_PrintPdbLinkages(struct AtomInfo *atoms, int n, int max, FILE *file);
int CC_FindAtomNumber(struct AtomInfo *atoms, Chem_ChemAtom atom, int i, int max);
int CC_FindAtomNumberFromLinkCode(struct AtomInfo *atoms, int seqCode, char *linkCode, const int max);

PyObject* ApiSet_Get(ApiSet set, int n);
PyObject* ApiSet_AddSet(ApiSet set1, ApiSet set2);

#endif
