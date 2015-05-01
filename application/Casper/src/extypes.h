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

/* Character type definitions for expr.c */

#ifndef _EXTYPES_H
#define _EXTYPES_H

/* Controll */
/* This is the default for all non-printable characters */
#define EX_CTRL		0x00

/* White space */
/* This is for characters whose only purpose is as separators */
#define EX_SPACE	0x01
/* End-of-line */
/* End-of-line characters */
#define EX_EOL		0x02

/* Literal - 1st character */
/* Characters that can appear as the first character in a <literal> */
#define EX_1ST_LIT	0x04
/* Literal - 2nd.. character */
/* Characters that can appear in other positions */
#define EX_2ND_LIT	0x08
/* Normal type - may be either first or last */
#define EX_LITERAL	(EX_1ST_LIT|EX_2ND_LIT)

/* Symbol - 1st character */
/* For all symbols */
#define EX_1ST_SYM	0x10
/* Symbol - 2nd character */
/* For all symbols except those which have special meanings as prefixes
such as '!','+' and '-' (since they are toggles every single one must be
interpreted by itself) */
#define EX_2ND_SYM	0x20
/* Normal type - may be either first or last */
#define EX_SYMBOL	(EX_1ST_SYM|EX_2ND_SYM)

/* Quote/Parenthesis */
/* For symbols that terminate statements or expressions */
#define EX_QUOTE	0x40

/* Reserved */
#define EX_RESERVED	0x80

static unsigned char _ex_char_table[128]=
{
	EX_CTRL,		/* 00 NUL */
	EX_CTRL,		/* 01 SOH */
	EX_CTRL,		/* 02 STX */		
	EX_CTRL,		/* 03 ETX */
	EX_CTRL,		/* 04 EOT */
	EX_CTRL,		/* 05 ENQ */
	EX_CTRL,		/* 06 ACK */
	EX_CTRL,		/* 07 BEL */
	EX_CTRL,		/* 08 BS */
	EX_SPACE,		/* 09 HT */
	EX_EOL,			/* 0A NL */
	EX_SPACE,		/* 0B VT */
	EX_CTRL,		/* 0C NP */
	EX_EOL,			/* 0D CR */
	EX_CTRL,		/* 0E SO */
	EX_CTRL,		/* 0F SI */
	EX_CTRL,		/* 10 DLE */
	EX_CTRL,		/* 11 DC1 */
	EX_CTRL,		/* 12 DC2 */
	EX_CTRL,		/* 13 DC3 */
	EX_CTRL,		/* 14 DC4 */
	EX_CTRL,		/* 15 NAK */
	EX_CTRL,		/* 16 SYN */
	EX_CTRL,		/* 17 ETB */
	EX_CTRL,		/* 18 CAN */
	EX_CTRL,		/* 19 EM */
	EX_CTRL,		/* 1A SUB */
	EX_CTRL,		/* 1B ESC */
	EX_CTRL,		/* 1C FS */
	EX_CTRL,		/* 1D GS */
	EX_CTRL,		/* 1E RS */
	EX_CTRL,		/* 1F US */
	EX_SPACE,		/* 20 space */
	EX_1ST_SYM,		/* 21 ! */
	EX_QUOTE,		/* 22 " */
	EX_SYMBOL,		/* 23 # */
/*	EX_SYMBOL,		* 24 $ */
	EX_1ST_SYM,		/* So $ can't trail any op's */
	EX_SYMBOL,		/* 25 % */
	EX_SYMBOL,		/* 26 & */
	EX_QUOTE,		/* 27 ' */
	EX_QUOTE,		/* 28 ( */
	EX_QUOTE,		/* 29 ) */
	EX_SYMBOL,		/* 2A * */
	EX_1ST_SYM,		/* 2B + */
	EX_QUOTE,		/* 2C , */
/*	EX_1ST_SYM|EX_2ND_LIT,		* 2D - */
	EX_1ST_SYM,		/* Must not be allowed to trail literal */
	EX_SYMBOL|EX_2ND_LIT,		/* 2E . */
	EX_SYMBOL,		/* 2F / */
	EX_2ND_LIT,		/* 30 0 */
	EX_2ND_LIT,		/* 31 1 */
	EX_2ND_LIT,		/* 32 2 */
	EX_2ND_LIT,		/* 33 3 */
	EX_2ND_LIT,		/* 34 4 */
	EX_2ND_LIT,		/* 35 5 */
	EX_2ND_LIT,		/* 36 6 */
	EX_2ND_LIT,		/* 37 7 */
	EX_2ND_LIT,		/* 38 8 */
	EX_2ND_LIT,		/* 39 9 */
	EX_QUOTE,		/* 3A : */
	EX_QUOTE,		/* 3B ; */
	EX_SYMBOL,		/* 3C < */
	EX_SYMBOL,		/* 3D = */
	EX_SYMBOL,		/* 3E > */
	EX_SYMBOL,		/* 3F ? */
/*	EX_SYMBOL,		* 40 @ */
	EX_1ST_SYM,		/* So +@ is two tokens.. */
	EX_LITERAL,		/* 41 A */
	EX_LITERAL,		/* 42 B */
	EX_LITERAL,		/* 43 C */
	EX_LITERAL,		/* 44 D */
	EX_LITERAL,		/* 45 E */
	EX_LITERAL,		/* 46 F */
	EX_LITERAL,		/* 47 G */
	EX_LITERAL,		/* 48 H */
	EX_LITERAL,		/* 49 I */
	EX_LITERAL,		/* 4A J */
	EX_LITERAL,		/* 4B K */
	EX_LITERAL,		/* 4C L */
	EX_LITERAL,		/* 4D M */
	EX_LITERAL,		/* 4E N */
	EX_LITERAL,		/* 4F O */
	EX_LITERAL,		/* 50 P */
	EX_LITERAL,		/* 51 Q */
	EX_LITERAL,		/* 52 R */
	EX_LITERAL,		/* 53 S */
	EX_LITERAL,		/* 54 T */
	EX_LITERAL,		/* 55 U */
	EX_LITERAL,		/* 56 V */
	EX_LITERAL,		/* 57 W */
	EX_LITERAL,		/* 58 X */
	EX_LITERAL,		/* 59 Y */
	EX_LITERAL,		/* 5A Z */
	EX_QUOTE,		/* 5B [ */
	EX_SYMBOL,		/* 5C \ */
	EX_QUOTE,		/* 5D ] */
	EX_SYMBOL,		/* 5E ^ */
	EX_LITERAL,		/* 5F _ */
	EX_QUOTE,		/* 60 ` */
	EX_LITERAL,		/* 61 a */
	EX_LITERAL,		/* 62 b */
	EX_LITERAL,		/* 63 c */
	EX_LITERAL,		/* 64 d */
	EX_LITERAL,		/* 65 e */
	EX_LITERAL,		/* 66 f */
	EX_LITERAL,		/* 67 g */
	EX_LITERAL,		/* 68 h */
	EX_LITERAL,		/* 69 i */
	EX_LITERAL,		/* 6A j */
	EX_LITERAL,		/* 6B k */
	EX_LITERAL,		/* 6C l */
	EX_LITERAL,		/* 6D m */
	EX_LITERAL,		/* 6E n */
	EX_LITERAL,		/* 6F o */
	EX_LITERAL,		/* 70 p */
	EX_LITERAL,		/* 71 q */
	EX_LITERAL,		/* 72 r */
	EX_LITERAL,		/* 73 s */
	EX_LITERAL,		/* 74 t */
	EX_LITERAL,		/* 75 u */
	EX_LITERAL,		/* 76 v */
	EX_LITERAL,		/* 77 w */
	EX_LITERAL,		/* 78 x */
	EX_LITERAL,		/* 79 y */
	EX_LITERAL,		/* 7A z */
	EX_QUOTE,		/* 7B { */
	EX_SYMBOL,		/* 7C | */
	EX_QUOTE,		/* 7D } */
	EX_1ST_SYM,		/* 7E ~ */
	EX_CTRL			/* 7F DEL */
};

#define _is_begin_literal(chr)	(_ex_char_table[chr]&EX_1ST_LIT)
#define _is_cont_literal(chr)	(_ex_char_table[chr]&EX_2ND_LIT)
#define _is_literal(chr)	(_is_begin_literal(chr)|_is_cont_literal(chr))

#define _is_begin_symbol(chr)	(_ex_char_table[chr]&EX_1ST_SYM)
#define _is_cont_symbol(chr)	(_ex_char_table[chr]&EX_2ND_SYM)
#define _is_symbol(chr)		(_is_begin_symbol(chr)|_is_cont_symbol(chr))

#define _is_quote(chr)		(_ex_char_table[chr]&EX_QUOTE)


#define _is_begin_token(chr)	(_is_begin_literal(chr)|_is_begin_symbol(chr))
#define _is_cont_token(chr)	(_is_cont_literal(chr)|_is_cont_symbol(chr))

#define _is_bin_digit(chr)	((chr=='0')||(chr=='1'))
#define _is_oct_digit(chr)	((chr>='0')&&(chr<='7'))

unsigned char _n_base_val(char chr);

#endif /* !_EXTYPES_H */
