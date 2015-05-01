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

/* Filename = expr.h */
/* Prefix = EX */

#ifndef _EXPR_H
#define _EXPR_H


#include <stdio.h>

#define EX_STRINGLEN	256

#define EX_FLOAT	'f'	/* float */
#define EX_STRING	's'	/* string <256 chars */
#define EX_NONE		' '
/* Special - for use in variables */
#define EX_FPTR		'F'	/* pointer to float */
#define EX_SPTR		'S'	/* pointer to string */
#define EX_EXEC		'E'	/* pointer to read/write fn */

/* Sun kludge */
struct Value {
	unsigned char Type;
	struct {
	struct {		/* Pointer to read/write fn */
	 int (*Read)();
	 int (*Write)();
	} Function;
	float Float;		/* Floating point value */
	char String[EX_STRINGLEN];	/* String value */
/* Special - for use in variables */
	void *Pointer;		/* Pointer to internal variable */
	} Value;
	};



extern struct Value VStack[];
extern int VPointer;

#define ResetEval	{ OPointer=-1; OBottom=-1; VPointer=-1; }

#define RightValue	VStack[VPointer].Value
#define LeftValue	VStack[VPointer-1].Value
#define TopValue	RightValue
#define DropValue	{VPointer--;}
#define PushFloat(val)	{VStack[++VPointer].Value.Float=val; VStack[VPointer].Type=EX_FLOAT;}
#define PushString(val)	{strncpy(VStack[++VPointer].Value.String, val, EX_STRINGLEN);\
			 VStack[VPointer].Type=EX_STRING;}

#define RightType	VStack[VPointer].Type
#define LeftType	VStack[VPointer-1].Type
#define TopType		RightType

char *OP_Add();
char *OP_Subtract();
char *OP_Multiply();
char *OP_Divide();
char *OP_Equal();
char *OP_Less();
char *OP_Greater();
int OP_True();
char *OP_And();
char *OP_Or();
char *OP_Not();
char *OP_Positive();
char *OP_Negative();
char *OP_Sign();
char *OP_Absolute();
char *OP_FExists();
char *OP_SExists();
char *OP_Kilo();
char *OP_Import();
char *OP_Question();
char *OP_Variable(char *string);
char *OP_ToString();
char *OP_ToNumber();
char *OP_Fit();
void EvalTerm(char **string);

void Evaluate(char **string);

int OP_True();

extern int OPointer;
extern int OBottom;

#endif
