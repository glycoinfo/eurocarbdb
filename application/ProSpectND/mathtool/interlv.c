
/*
 *  merge (iopt=1) or disentangle (iopt=-1) r,i into/from ri
 */
void intrlv(float *ri, float *r, float *i, int n, int iopt)
{
    int n2,i1,i2;

    if (iopt == 1) {
        for (i1=1;i1<=n;i1++) {
             i2 = i1 * 2;
             ri[i2-1] = r[i1];
             ri[i2]   = i[i1];
        }
    }
    else if (iopt == -1) {
        n2=n/2;
        for (i1=1;i1<=n2;i1++) {
             i2 = i1 * 2;
             r[i1] = ri[i2-1];
             i[i1] = ri[i2];
        }
    }
}

