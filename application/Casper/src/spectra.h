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

/* Filename = spectra.h */
/* Prefix = SP_ */
/* functions for loading, sorting and assigning spectra */

#ifndef _SPECTRA_H
#define _SPECTRA_H

#include "parser.h"
#include "type.h"
#include "build.h"
#include "gnuplot_i.h"

#define SP_ID_PROTON	(('H'<<8)+0)
#define SP_ID_CARBON	(('C'<<8)+0)
#define SP_ID_2D_CH	(('C'<<8)+'H')
#define SP_ID_2D_HH	(('H'<<8)+'H')

#define SP_ID_TOT       (('T'<<8)+0)

#define SP_LINE_LEN	PA_LINE_LEN

#define SP_CALC_ERR	(-1)

#define SP_ROW_WIDTH	10	/* number of shifts per line */

#define CMAX 175
#define HMAX 7
#define PI 3.14159265
#define SP_AM_ACC       2       /* Use extra accurate, but slow, 2D assigning */
#define SP_AM_CUTOFF	1	/* Use cutoff values */
#define SP_AM_OLD	0	/* Use 'old' method */

/* The cutoff values are the distance within which experimental
   peaks can be assigned to free simulated peaks. This only applies
   when all experimental peaks has been assigned to a simulated
   peak. All experimental shifts must be assigned to at least one
   simulated shift. */
#define SP_H_CUTOFF 0.15
#define SP_C_CUTOFF 0.50
#define SP_CH_CUTOFF 0.25
#define SP_HH_CUTOFF 0.25

/* Multiplied with the cutoff value if a simulated shift can't be matched
   with an experimental shift. */
#define SP_CUTOFF_PENALTY 1.0
/* Factor multiplied with Cutoff value for penalty when assigning
   to different signals than previously. */
#define SP_MISMATCH_PENALTY 5.0
/* Scaling for 13C in CH-correlated spectra.
   Since the C shift is not as flexible as the
   H shift in a CH-spectrum the H scaling still
   means that keeping the same C shift is even
   more important than keeping the same H shift.
   The H shift is still weighed since the shifts
   are lower the change is small.
   The scaling is actually 0.2, but only used as
   0.2*0.2 and is therefore already calculated here
   for slight speed increase. */
#define SP_C_SCALING 0.04
/* Scaling factors for the calc. of Total Fit Error. */
#define SP_H_WEIGHT 1
#define SP_C_WEIGHT 0.1
#define SP_CH_WEIGHT 5
#define SP_HH_WEIGHT 0.5

/* This struct keep track of the number of chemical shifts assigned to each specific atom. */
struct SP_assignments {
  char name[NODE_NAME_LENGTH];   /* The name of the unit */
  int c[TY_MAX_CARBON];          /* How many chemical shifts are assigned to each carbon */
  int h[TY_MAX_CARBON][2];       /* How many chemical shifts are assigned to each proton */
};

/* commands */
int SP_LoadExp();
int SP_LoadAssignments();
int SP_Correct();
int SP_Assign();
int SP_MigrateSimulatedAssignments();

extern struct ME_Method SpectrumMethod;

/* internal functions */
void SP_ListSpectrum(const struct BU_Struct *spectrum);
void SP_PrintSpectrum(const struct BU_Struct *spectrum);
int SP_Compare(const void *a, const void *b);
void SP_Sort1DSpectra(struct BU_Struct *spectrum);
void SP_Sort2DSpectra(struct BU_Struct *spectrum);
void SP_QuickSortSpectra(struct BU_Struct *str, int lPtr, int rPtr, const unsigned int mode);
int SP_SpectraSortPartition(struct BU_Struct *str, int lPtr, int rPtr, const unsigned int mode);
void SP_SortSwapShifts(struct BU_Struct *str, int i, int j, const unsigned int mode);
void SP_SortInsertShift(struct BU_Struct *str, int origPos, int toBefore, const unsigned int mode);
void  SP_Calc1DSpectra(struct BU_Struct *spectrum);
void SP_Calc2DSpectra(struct BU_Struct *spectrum);

float SP_2DDelta(const struct BU_Struct *spec1, const unsigned int pos1, 
		 const struct BU_Struct *spec2, const unsigned int pos2,
		 const unsigned int mode);
void SP_RawCompare(struct BU_Struct *exp, struct BU_Struct *sim,
		   const unsigned int mode);
void SP_Raw2DCompare(struct BU_Struct *exp, struct BU_Struct *sim,
		     const unsigned int mode);

int SP_CalculateCorrections(struct BU_Struct *sim, struct BU_Struct *exp);
int SP_CalculateMultipleCorrections(char *simPrefix, char *expPrefix);
float SP_AssExpSim(struct BU_Struct *exp, struct BU_Struct *sim,
		  const unsigned int mode, const char print,
		  struct SP_assignments *nr_assign);
float SP_AssExpSimfind (const struct BU_Struct *exp, struct BU_Struct *sim,
			const unsigned int mode,
			int *usedsim, unsigned int sim_ptr, int *usedexp,
			unsigned int exp_ptr);
void SP_AssExpSimTwinHydrogen(struct BU_Struct *exp, struct BU_Struct *sim, int *usedexp, int *usedsim);
float SP_AssExpSimTwinHydrogenFind(const struct BU_Struct *exp, struct BU_Struct *sim, int *usedexp, int *usedsim, int firstexp, int firstsim, int expRem, int simRem);
void SP_AssExpSimDoubleSignals(struct BU_Struct *exp, struct BU_Struct *sim, int *usedexp, int *usedsim, int *expRem, int *simRem);
float SP_AssExpSim2Dfind(struct BU_Struct *exp, struct BU_Struct *sim,
			 const unsigned int mode,
			 int *usedsim, unsigned int firstsim,
			 int *usedexp, unsigned int firstexp,
			 int **globalSimAss, int **globalExpAss);
float SP_AssExpSimQuick(struct BU_Struct *exp, struct BU_Struct *sim, 
			const unsigned int mode,
			const int print,
			struct SP_assignments *nr_assign);
void SP_CalcErrs(struct BU_Struct *exp, struct BU_Struct *sim, int cnt, int *usedexp, float *err, float *abs_err, float *rms_err, float *sys_err, const unsigned int mode);
void MultiExpAssignments(struct BU_Struct *exp, struct BU_Struct *sim, int *usedsim, float *err, float *abs_err, float *rms_err, int *nUnassigned, const unsigned int mode);
void SP_PrintAssignmentTable(struct BU_Struct *exp, struct BU_Struct *sim, int *usedsim, int *usedexp, float *abs_err, float *sys_err, float *rms_err, struct SP_assignments *nr_assign, int nUnassigned, const unsigned int mode);
int SP_FindC(struct BU_Struct *sim, struct BU_Struct *exp,  
	     int s_off, int e_off, char printfail,
	     struct SP_assignments *nr_assign);
int SP_FindH(struct BU_Struct *sim, struct BU_Struct *exp,  
	     int s_off, int e_off, char printfail,
	     struct SP_assignments *nr_assign);
int SP_FindCh(struct BU_Struct *sim, struct BU_Struct *exp,  
	      int s_off, int e_off, char printfail,
	      struct SP_assignments *nr_assign);
int SP_FindHh(struct BU_Struct *sim, struct BU_Struct *exp,  
	      int s_off, int e_off, char printfail,
	      struct SP_assignments *nr_assign);

int SP_FindC6Twin(struct BU_Struct *sim, int j);
int SP_PopulateAssignments(struct BU_Struct *exp, struct BU_Struct *sim, int *usedexp, int *usedsim, const unsigned int mode);
int SP_MatchAssignment(const struct BU_Struct *exp, const unsigned int exp_ptr,
		       struct BU_Struct *sim, const unsigned int sim_ptr, unsigned int mode);
int SP_AddAssignment(struct BU_Struct *exp, const unsigned int exp_ptr,struct BU_Struct *sim,
		     const unsigned int sim_ptr, const unsigned int mode);
void SP_AssignOtherShiftsToAtom(struct BU_Struct *exp, float shift1, float shift2, struct BU_ShiftToAtom *atom1, struct BU_ShiftToAtom *atom2, const unsigned int mode);
int SP_FindExpAssignment(const struct BU_Struct *exp, const unsigned int exp_ptr,
		       struct BU_Struct *sim, const unsigned int mode);
int SP_FindExpAssignedToSim(const struct BU_Struct *exp, int exp_ptr,
			    struct BU_Struct *sim, int sim_ptr, unsigned int mode);
int QuickFind(const float *array, int first, int last, const float value);
float SP_CombinationDiff(struct BU_Struct *exp, struct BU_Struct *sim, const int exp_ptr, const int sim_ptr);
float SP_CombinationDiffHh(struct BU_Struct *exp, struct BU_Struct *sim, const int exp_ptr, const int sim_ptr);

int SP_GetHhShiftsFromAtom(struct BU_Unit *unit, int atom, int HNr, float *HiShift, float *LoShift);

int SP_FindInvolvedShifts(struct BU_Struct *str, float *HiShift, float *LoShift, int *range, float value, float value2, const unsigned int mode);
float SP_MiniAssignment(const float *expFirstDim, const float *expSecondDim,
			const int *expRange, int eCnt, 
			const float *simFirstDim, const float *simSecondDim,
			struct BU_ShiftToAtom *CAtom,
			struct BU_ShiftToAtom *HAtom,
			const int *simRange, int sCnt,
			const unsigned int mode);
int SP_Gnuplot();
int SP_Gnuplot1D(gnuplot_ctrl *plot, struct BU_Struct *sim, struct BU_Struct *exp, unsigned int mode);
int SP_Gnuplot2D(gnuplot_ctrl *plot, struct BU_Struct *sim, struct BU_Struct *exp, unsigned int mode);

extern float SP_H_Cutoff, SP_C_Cutoff, SP_CH_Cutoff, SP_HH_Cutoff;

extern float SP_H_Scaling;	/* Scaling for 1H in CH-correlated spectra */
extern char SP_AssignMode;	/* What kind of sorting */

/* Scaling factors for the calc. of rank */
extern float SP_H_Weight, SP_C_Weight, SP_CH_Weight, SP_HH_Weight;


#endif
