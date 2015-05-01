/********************************************************************/
/*                          psnd_regexp.c                           */
/*                                                                  */
/* 1997, Albert van Kuik                                            */
/********************************************************************/
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#include <time.h>
#include "genplot.h"
#include "psnd.h"


static int compile(char *source);
static char *cclass(char *source, char *src);
static int store(char op);
/*char *match(void);*/
static char *pmatch(char *line, char *pattern);
/*************************************************************************
*  SEARCH
*/

/*========================================================================
* Regular Expression
*/

#define LMAX    /* 512 */ PSND_STRLEN
#define PMAX	256
#define CHAR	1
#define BOL     2
#define EOL     3
#define ANY     4
#define CLASS	5
#define NCLASS	6
#define STAR	7
#define PLUS	8
#define MINUS	9
#define ALPHA	10
#define DIGIT	11
#define NALPHA	12
#define PUNCT	13
#define RANGE	14
#define ENDPAT	15

static int is_case_sensitive = TRUE;
#define TOLOWER(c)  ((is_case_sensitive) ? (c) : (tolower(c)))
static int debug = 0;			/* Set for debug code      */
static char *pp;
static char lbuf[LMAX+1];
static char pbuf[PMAX+1];

/*
* Compile the pattern into globlal pbuf[]
*/
static int compile(char *source)
{
    char *s;			/* Source string pointer	   */
    char *lp;			/* Last pattern pointer	   */
    int c;			/* Current character	   */
    int o;			/* Temp			   */
    char *spp;			/* Save beginning of pattern */

    s = source;

    if (debug)
	printf("Pattern = \"%s\"\n", s);

    pp = pbuf;
    while ((c = *s++)!=0) {
	/*
        * STAR, PLUS and MINUS are special.
        */
	if (c == '*' || c == '+' || c == '-') {
	    if (pp == pbuf ||
		(o = pp[-1]) == BOL ||
		o == EOL ||
		o == STAR ||
		o == PLUS ||
		o == MINUS)
		return FALSE;
	    if (!store(ENDPAT))
		return FALSE;
	    if (!store(ENDPAT))
		return FALSE;
	    spp = pp;		/* Save pattern end	 */
	    while (--pp > lp)	/* Move pattern down	 */
		*pp = pp[-1];	/* one byte		 */
	    *pp = (c == '*') ? STAR :
		(c == '-') ? MINUS : PLUS;
	    pp = spp;		/* Restore pattern end  */
	    continue;
	}
/*
 * All the rest.
 */
	lp = pp;		/* Remember start       */
	switch (c) {
	case '^':
	    if (!store(BOL))
		return FALSE;
	    break;
	case '$':
	    if (!store(EOL))
		return FALSE;
	    break;
	case '.':
	    if (!store(ANY))
		return FALSE;
	    break;
	case '[':
	    if ((s = cclass(source, s)) == NULL)
		return FALSE;
	    break;
	case ':':
	    if (*s) {
		c = *s++;
		switch (c) {
		case 'a':
		case 'A':
		    if (!store(ALPHA))
			return FALSE;
		    break;
		case 'd':
		case 'D':
		    if (!store(DIGIT))
			return FALSE;
		    break;
		case 'n':
		case 'N':
		    if (!store(NALPHA))
			return FALSE;
		    break;
		case ' ':
		    if (!store(PUNCT))
			return FALSE;
		    break;
		default:
		    return FALSE;
		}
		break;
	    }
	    else
		return FALSE;
	case '\\':
            if (*s) {
                switch (*s) {
                case 't' :
                    c = '\t';
                    break;
                case 'r' :
                    c = '\r';
                    break;
/*--- NEWLINE's are removed, thus can not find ---*/
                case 'n' :
                    c = '\n';
                    break;
                default:
                    c = *s;
                }
                s++;
            }

	default:
	    if (!store(CHAR))
		return FALSE;
	    if (!store(TOLOWER(c)))
		return FALSE;
	}
    }
    if (!store(ENDPAT))
	return FALSE;
    if (!store(0))
	return FALSE;		/* Terminate string     */

    if (debug) {
	for (lp = pbuf; lp < pp;) {
	    if ((c = (*lp++ & 0377)) < ' ')
		printf("\\%o ", c);
	    else
		printf("%c ", c);
	}
	printf("\n");
    }
    return TRUE;
}

static char *cclass(char *source, char *src)
/* char	   *source;    Pattern start -- for error msg.      */
/* char	   *src;       Class start	       */
/*
 * Compile a class (within [])
 */
{
    char *s;			/* Source pointer    */
    char *cp;			/* Pattern start	   */
    int c;			/* Current character */
    int o;			/* Temp		   */

    s = src;
    o = CLASS;
    if (*s == '^') {
	++s;
	o = NCLASS;
    }
    if (!store(o))
	return NULL;
    cp = pp;
    if (!store(0))
	return NULL;		/* Byte count	 */
    while ((c = *s++) && c != ']') {
	if (c == '\\') {	/* Store quoted char    */
	    if ((c = *s++) == '\0')	/* Gotta get something  */
		return NULL;
	    else if (!store(TOLOWER(c)))
		return NULL;
	}
	else if (c == '-' &&
		 (pp - cp) > 1 && *s != ']' && *s != '\0') {
	    c = pp[-1];		/* Range start     */
	    pp[-1] = RANGE;	/* Range signal    */
	    if (!store(c))
		return NULL;	/* Re-store start  */
	    c = *s++;		/* Get end char and*/
	    if (!store(TOLOWER(c)))
		return NULL;	/* Store it	    */
	}
	else {
	    if (!store(TOLOWER(c)))
		return NULL;	/* Store normal char */
	}
    }
    if (c != ']')
	return NULL;
    if ((c = (pp - cp)) >= 256)
	return NULL;
    if (c == 0)
	return NULL;
    *cp = c;
    return (s);
}

static int store(char op)
{
    if (pp >= &pbuf[PMAX])
	return FALSE;
    *pp++ = op;
    return TRUE;
}

static char *match(int *len)
{
    char *l, *p;

    for (l = lbuf; *l; l++) {
	p = pmatch(l, pbuf);
	if (p) {
            *len = (int)(p - l);
 	    return (p);
         }
    }
    return (NULL);
}

static char *matchrev(int *len)
{
    char *l, *p;
    int end;
    
    end = strlen(lbuf);
    for (l = lbuf + end; l != lbuf; l--) {
	p = pmatch(l, pbuf);
	if (p) {
            *len = (int)(p - l);
 	    return (p);
         }
    }
    return (NULL);
}

static char *pmatch(char *line, char *pattern)
/* char		   *line;     (partial) line to match      */
/* char		   *pattern;  (partial) pattern to match   */
{
    char *l;			/* Current line pointer	      */
    char *p;			/* Current pattern pointer      */
    char c;			/* Current character	      */
    char *e;			/* End for STAR and PLUS match  */
    int op;			/* Pattern operation	      */
    int n;			/* Class counter		      */
    char *are;			/* Start of STAR match	      */

    l = line;

    if (debug > 1)
	printf("pmatch(\"%s\")\n", line);

    p = pattern;
    while ((op = *p++) != ENDPAT) {

	if (debug > 1)
	    printf("byte[%d] = 0%o, '%c', op = 0%o\n",
		   l - line, *l, *l, op);

	switch (op) {
	case CHAR:
	    if (TOLOWER(*l) != *p++)
		return (NULL);
	    l++;
	    break;
	case BOL:
	    if (l != lbuf)
		return (NULL);
	    break;
	case EOL:
	    if (*l != '\0')
		return (NULL);
	    break;
	case ANY:
	    if (*l++ == '\0')
		return (NULL);
	    break;
	case DIGIT:
	    if ((c = *l++) < '0' || (c > '9'))
		return (NULL);
	    break;
	case ALPHA:
	    c = tolower(*l);
	    l++;
	    if (c < 'a' || c > 'z')
		return (NULL);
	    break;
	case NALPHA:
	    c = tolower(*l);
	    l++;
	    if (c >= 'a' && c <= 'z')
		break;
	    else if (c < '0' || c > '9')
		return (NULL);
	    break;
	case PUNCT:
	    c = *l++;
	    if (c == 0 || c > ' ')
		return (NULL);
	    break;
	case CLASS:
	case NCLASS:
	    c = TOLOWER(*l);
	    l++;
	    n = *p++ & 0377;
	    do {
		if (*p == RANGE) {
		    p += 3;
		    n -= 2;
		    if (c >= p[-2] && c <= p[-1])
			break;
		}
		else if (c == *p++)
		    break;
	    } while (--n > 1);
	    if ((op == CLASS) == (n <= 1))
		return (NULL);
	    if (op == CLASS)
		p += n - 2;
	    break;
	case MINUS:
	    e = pmatch(l, p);	/* Look for a match	*/
	    while (*p++ != ENDPAT);	/* Skip over pattern	*/
	    if (e)		/* Got a match?	*/
		l = e;		/* Yes, update string	*/
	    break;		/* Always succeeds	*/
	case PLUS:		/* One or more ...	*/
	    if ((l = pmatch(l, p)) == 0)
		return (NULL);	/* Gotta have a match	*/
	case STAR:		/* Zero or more ...	*/
	    are = l;		/* Remember line start */
	    while (*l && (e = pmatch(l, p)))
		l = e;		/* Get longest match	*/
	    while (*p++ != ENDPAT);	/* Skip over pattern	*/
	    while (l >= are) {	/* Try to match rest	*/
		if ((e = pmatch(l, p))!=NULL)
		    return (e);
		--l;		/* Nope, try earlier	*/
	    }
	    return (NULL);	/* Nothing else worked */
	default:
	    printf("Bad op code %d\n", op);
	    perror("Cannot happen -- match\n");
	}
    }
    return (l);
}


char *psnd_grep(char *s1, char *s2, int *mlen)
{
    char *p2;
    static char *p1;
    int len;

    if (s1 == NULL || s2 == NULL)
	return NULL;
    if (!compile(s2)) {
        /*
	ErrorMessage("Can not compile pattern.");
        */
	return NULL;
    }
    while (s1) {
	if ((p1 = strchr(s1, '\n')) == NULL) {
	    len = strlen(s1);
	    if (len == 0)
		return NULL;
	}
	else
	    len = (int) (p1 - s1);
	if (len > LMAX)
	    return NULL;
	strncpy(lbuf, s1, len);
	lbuf[len] = '\0';

	if ((p2 = match(mlen)) != NULL) {
	    p1 = s1 + (int) (p2 - lbuf);
	    return p1 - *mlen;
	}
	s1 += len + 1;
    }
    return NULL;
}

