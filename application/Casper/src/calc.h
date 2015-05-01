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

/* Filename = calc.h */
/* Prefix = CA */

#include "build.h"

/* CA_Mode - defines level of accuracy in calculations: */
#ifndef _CALC_H
#define _CALC_H

#define	CA_DISACCH	0x00	/* No branchpoint corrections */
#define CA_TRISACCH	0x01	/* Branchpoint corections */
#define CA_DEFAULT	0x02	/* Defaults enabled */
#define CA_DOUBLE	0x04	/* 'Double'-defaulting enabled */
#define CA_VERBOSE	0x10	/* Print all bonds and delta-sets */

int CA_SimBranch(struct BU_Unit *lo, int cent_lo, struct BU_Unit *hi,
		 int cent_hi, struct BU_Unit *cent);
extern unsigned char CA_Mode;
int CA_SimDisacch(struct BU_Unit *unit1, char pos1,
			 char pos2, struct BU_Unit *unit2);

short CA_CalcAllGlycosylations(struct BU_Struct *str);

#endif
