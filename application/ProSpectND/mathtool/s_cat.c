

void s_cat(char *lp, char *rpp[], int rnp[], int *np, int ll)
{
    int i, n, nc;
    char *f__rp;

    n = (int)*np;
    for(i = 0 ; i < n ; ++i){
        nc = ll;
        if(rnp[i] < nc)
            nc = rnp[i];
        ll -= nc;
        f__rp = rpp[i];
        while(--nc >= 0)
            *lp++ = *f__rp++;
    }
    while(--ll >= 0)
        *lp++ = ' ';
}
