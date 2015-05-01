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

/* defines token fn:s cc -lm */
/* no handling of +/- */
/* no handling of different letters for exponents */

#include "extypes.h"
#include "exrdtok.h"
#include <ctype.h>
#include <math.h>

#define DECIMAL_SEP	'.'

int ReadSign(char *string[])
{
  int sign;
  sign=1;
  while ( ( (**string)=='+')||( (**string)=='-') )
    {
      if (**string=='-') sign=-sign;
      (*string)++;
    };
  return (sign);
}

long int ReadInteger(char *string[],int base)
{
  int sign;
  unsigned char digit;
  long int value=0;
  sign=1;
  digit=_n_base_val(**string);
  while ( (digit<base) )
    {
      value=value*base+digit;
      (*string)++;
      digit=_n_base_val(**string);
    }
  return(value);
}

double ReadFraction(char *string[],int base)
{
  unsigned char digit;
  double quotient=0, divider=1; 
  digit=_n_base_val(**string);
  while (digit<base)
    {
      quotient=quotient*base+digit;
      divider*=base;
      *string+=1;
      digit=_n_base_val(**string);
    }
  return(quotient/divider);
}


/* format <integerpart>.<fractionalpart>e<exponent> */
double ReadFloat(char *string[], int base)
{
  int sign;
  double integer, fraction, exponent;
  char *mark;
  fraction=0;
  exponent=0;
  sign=ReadSign(string);
  mark=*string;
  integer=sign*ReadInteger(string,base);
  if (**string==DECIMAL_SEP)
    {
      *string+=1;
      fraction=sign*ReadFraction(string,base);
    }
  if (*string==mark) return(0);
  if (tolower(**string)!='e')
    return(integer+fraction);
  *string+=1;
  sign=ReadSign(string);
  exponent=sign*ReadInteger(string,base);
  return( (integer+fraction)*pow(base,exponent) );
}

/* Reads a token (~unquoted string) from string into buffer */
void ReadToken(char *string[], char buffer[])
{
  int i=0;
  /* is a 'literal' */
  if (_is_begin_literal((int)**string))
    {
      buffer[i++]=**string;
      *string+=1;
      while (_is_cont_literal((int)**string))
	{
	  buffer[i++]=**string;
	  *string+=1;
	}
      buffer[i]=0;
      return;
    }
  /* is a complex symbol */
  if (_is_begin_symbol((int)**string))
    {
      buffer[i++]=**string;
      *string+=1;
      while (_is_cont_symbol((int)**string))
	{
	  buffer[i++]=**string;
	  *string+=1;
	};
      buffer[i]=0;
      return;
    };
  /* default */
  buffer[i++]=**string;
  *string+=1;
  buffer[i]=0;
}


void ReadString(char *string[], char buffer[], char quote)
{
  int i=0;
  if (**string!=quote)	/* Handle unquoted strings */
    {
      /* special for * */
      if (**string=='*')
	{
	  buffer[i++]='*';
	  *string+=1;
	}
      /* special for / */
      if (**string=='/')
	{
	  buffer[i++]='/';
	  *string+=1;
	}
      while (_is_cont_literal((int)**string))
	{
	  buffer[i++]=**string;
	  *string+=1;
	}
      /* didn't do this before! */
      buffer[i]=0;
      return;
    }
  *string+=1;
  while (**string!=quote)
    {
      if (**string==0)	/* Bail-out on string end */
	{
	  buffer[i]=0;
	  return;
	};
      if (**string==PA_ESCAPE)	/* Handle 'escapes' */
	{
	  *string+=1;
	  switch (**string)
	    {
	    case PA_ESCAPE:	buffer[i++]=PA_ESCAPE; break;
	    case PA_QUOTE:	buffer[i++]=PA_QUOTE; break;
	    case PA_ESC_NL:	buffer[i++]='\n'; break;
	    case PA_ESC_TAB:	buffer[i++]='\t'; break;
	    default:		buffer[i++]=**string;
	    } /* switch */
	}
      else buffer[i++]=**string;
      *string+=1;
    }; /* while */
  *string+=1;
  buffer[i]=0;
}
