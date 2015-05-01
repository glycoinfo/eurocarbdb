

/*
 * at every index 'is' negate last 'ns' data
 */
void negstep(float *data, int n, int is, int ns)
{
    int n2,i,j;

    n2=n/is;
    for (i=1;i<=n2;i++)
        for (j=0;j<ns;j++)
            data[i*is-j-1] = -data[i*is-j-1];
}

