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

/* Headerfile for exrdtok.c */

#include "extypes.h"
#include <ctype.h>
#include <math.h>

#define CHAR_QUOTE		'\''
/* #define DBL_QUOTE_ESCAPE */
#define DECIMAL_SEP		'.'
#define	PA_QUOTE	'\''	/* Quote character */
#define PA_ESCAPE	'\\'	/* Escape character */
#define PA_ESC_NL	'n'	/* Esc for newline */
#define PA_ESC_TAB	't'	/* Esc for tab */

int ReadSign(char *string[]);
long int ReadInteger(char *string[], int base);
double ReadFraction(char *string[], int base);
double ReadFloat(char *string[], int base);
void ReadToken(char *string[], char buffer[]);
void ReadString(char *string[], char buffer[], char quote);
