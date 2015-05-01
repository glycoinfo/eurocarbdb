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

/* Filename = function.h */
/* Prefix = FN */
/* Some built in functions and 'variables' */

#ifndef _FUNCTION_H
#define _FUNCTION_H

#include "parser.h"
#include "variables.h"

#define FN_VERSION	"CASPER 5.0 prerelease 1"

int FN_NoRead();
int FN_NoWrite();
int FN_GetVersion();
int FN_GetTrace();
int FN_SetTrace();
int FN_GetPrompt();
int FN_SetPrompt();
int FN_SetError();
int FN_GetGenMode();
int FN_SetGenMode();
int FN_GetSafe();
int FN_SetSafe();
int FN_GetVerb();
int FN_SetVerb();
int FN_GetBranch();
int FN_SetBranch();
int FN_GetAssMode();
int FN_SetAssMode();

extern int FN_GetChLrRange();
extern int FN_SetChLrRange();

extern struct VA_Variable FN_Version;
extern struct VA_Variable FN_Trace;
extern struct VA_Variable FN_Prompt;
extern struct VA_Variable FN_GenMode;
extern struct VA_Variable FN_Error;
extern struct VA_Variable FN_Safe;
extern struct VA_Variable FN_Verbose;
extern struct VA_Variable FN_Branch;

extern struct VA_Variable FN_AssMode;
extern struct VA_Variable FN_HCutoff;
extern struct VA_Variable FN_CCutoff;
extern struct VA_Variable FN_HHCutoff;
extern struct VA_Variable FN_CHCutoff;

extern struct VA_Variable FN_ChLrCutoff;
extern struct VA_Variable FN_ChLrRange;

extern struct VA_Variable FN_H_Scaling;
extern struct VA_Variable FN_C_Weight;
extern struct VA_Variable FN_H_Weight;
extern struct VA_Variable FN_CH_Weight;
extern struct VA_Variable FN_HH_Weight;
extern struct VA_Variable FN_ChLr_Weight;

#endif
