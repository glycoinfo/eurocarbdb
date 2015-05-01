

/*
 * return the sum off all elements of array x
 * between points n1 and n2
 * Note: array starts at 0, but first point is denoted 1
 *       and last point is denoted nsize (the size of the array)
 */
float xxsumz(float *x, int n1, int n2)
{
    int i;
    float sum = 0.0;

    /*
     * array starts at 1
     */
    x--;
    for (i=n1;i<=n2;i++)
        sum += x[i];
    return sum;
}
