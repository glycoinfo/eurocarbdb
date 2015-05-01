/********************************************************************/
/*                          psnd_peakpick2d.c                       */
/*                                                                  */
/* 2001, Albert van Kuik                                            */
/********************************************************************/

 
#include <stdio.h>
#include <stdlib.h>
#include <ctype.h>
#include <string.h>
#include <stdarg.h>
#include <assert.h>
#include <math.h>
#include "genplot.h"
#include "psnd.h"
#include "mathtool.h"
#include "nmrtool.h"


void psnd_peakpick2d(MBLOCK *mblk, CBLOCK *cpar, float *xcpr,
            int ncpr, float clevel, 
            int inpfil, float *xdata, int maxdat, 
            int ilowa, int ihigha, int ilowb, int ihighb,
            int iadiv, int ibdiv)
{
    int ial,iah,ibl,ibh,nc;
    int iacpr,ibcpr;

    ial = min(ilowa,ihigha);
    iah = max(ilowa,ihigha);
    ibl = min(ilowb,ihighb);
    ibh = max(ilowb,ihighb);
    ial = max(ial,0);
    ibl = max(ibl,0);

    psnd_printf(mblk," ... peakpick2d - area %6d %6d %6d %6d\n",
                 ial,iah,ibl,ibh);

    if (!okarea (ial,iah,ibl,ibh))
        return;

    if (ncpr < 10 || maxdat < 10) {
        psnd_printf(mblk,"  error - peakpick2d buffer(s) too small : %2d %2d\n",
            ncpr, maxdat);
        return;
    }

    iadiv = max(1,iadiv);
    ibdiv = max(1,ibdiv);
    /*
     * Max resolution
     */
    iadiv  = 1;
    ibdiv  = 1;
    ihighb = ibh;
    do {
        int nr, nr1, nc1;
        
        nr  = (iah-ial+1);
        nr1 = nr/iadiv;
        nc  = (ibh-ibl+1);
        nc1 = nc/ibdiv;
        if ((nr1*nc1) <= ncpr) 
            break;
        ibh -= (nc-1)/2;
    } while (1);

    do {
        float cclevel[2];
        int nlevel = 1;

        psnd_printf(mblk," ... reading data -    %6d %6d %6d %6d\n",
                        ial,iah,ibl,ibh);
        if (mulr2d(mblk,xcpr,
                   ncpr,
                   inpfil,
                   xdata,
                   maxdat,
                   ial,iah,ibl,ibh,
                   &iacpr,
                   &ibcpr,
                   iadiv,
                   ibdiv) == FALSE)
                return;
        cpar->ial  = ial;
        cpar->iah  = iah;
        cpar->ibl  = ibl;
        cpar->ibh  = ibh;
        cpar->iadv = iadiv;
        cpar->ibdv = ibdiv;

        psnd_printf(mblk," ... searching peaks\n");
        /*
         * cclevel[0] contains the value of the level
         * cclevel[1] contains the sign
         */
        cclevel[0] = clevel;
        cclevel[1] = 1.0;

        if (clevel < 0.0) {
            int i;
            for (i=0;i<(iah-ial+1)*(ibh-ibl+1);i++)
                xcpr[i] *= -1.0;
    
            cclevel[0] = -clevel;
            cclevel[1] = -1.0;
        }
        /*
         * This funtion sends all closed contours to
         * 'psnd_add2d_peak'
         */
        contour_plot(xcpr, iacpr, ibcpr, cclevel, nlevel,
                  ial, ibl, iadiv, ibdiv, cpar->levcol,
                  (void  *) mblk->cpar_screen, TRUE, psnd_add2d_peak, NULL);
                 

        if (ibh == ihighb) 
            break;
        /*
         * We need closed contour lines, so if plane is split into
         * more than 1 piece, we must read an overlapping plane
         * to be sure. Thus, go back 1/2 plane.
         * Peaks that are added twice are ignored anyway.
         */
        ibh -= (nc-1)/2;

        ibl = ibh;
        ibh = min((ibh+nc-1),ihighb);
    } while (1);

}




