

/* assign strings:  a = b */

void s_copy(char *a, char *b, int la, int lb)
{
    char *aend, *bend;

    aend = a + la;

    if(la <= lb)
	while(a < aend)
		*a++ = *b++;

    else {
	bend = b + lb;
	while(b < bend)
		*a++ = *b++;
	while(a < aend)
		*a++ = ' ';
    }
}
