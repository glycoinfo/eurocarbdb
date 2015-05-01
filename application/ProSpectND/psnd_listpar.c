/********************************************************************/
/*                           psnd_listpar.c                         */
/*                                                                  */
/* List parameters                                                  */
/* 1998, Albert van Kuik                                            */
/********************************************************************/

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
#include <assert.h>

#include "iond.h"

#include "genplot.h"
#include "psnd.h"
#include "nmrtool.h"



static char *premode[] = { "      undefined",         
                           "   process real",
                           "   process imag",
                           "   process both" };
static char *set_premode(int i)
{
    if (i > 0 && i <=4)
        return premode[i];
    return premode[0];
}

static char *postmode[] = { "      automatic",         
                            "     store real",
                            "     store imag",
                            "     store both" };
static char *set_postmode(int i)
{
    if (i > 0 && i <=4)
        return postmode[i];
    return postmode[0];
}

static char *lpctype[] = {"            LPC",
                          "           HSVD"};
static char *set_lpctype(int i)
{
    if (i == 1)
        return lpctype[1];
    return lpctype[0];
}

static char *lpcmode[] = {"        forward",
                          "       backward",
                          "            gap"};
static char *set_lpcmode(int i)
{
    if (i > 0 && i <3)
        return lpcmode[i];
    return lpcmode[0];
}

static char *lpcreplace[] = {"           none",
                             "       spectrum"};
static char *set_lpcreplace(int i)
{
    if (i == 1)
        return lpcreplace[1];
    return lpcreplace[0];
}

static char *lpcroots[] = {"             no",
                           "            yes"};
static char *set_lpcroots(int i)
{
    if (i == 1)
        return lpcroots[1];
    return lpcroots[0];
}

static char *hilberttr[] = {"     no hilbert",
                            "  hilbert trans"};
static char *set_hilberttr(int i)
{
    if (i == 1)
        return hilberttr[1];
    return hilberttr[0];
}

static char *windowmode[]={"      no window",
                           "        hamming",
                           "        hanning",
                           "      sine bell",
                           "  sqr sine bell",
                           "    exp multipl",
                           "  lorentz-gauss",
                           "      conv diff",
                           "trapezian multp",
                           "         kaiser",
                           "     buffer (5)",
                           "            abc",
                           "   hamming done",
                           "   hanning done",
                           " sine bell done",
                           "  sqr sine done",
                           "   exp mul done",
                           "lrntz-gaus done",
                           " conv diff done",
                           "  trap mul done",
                           "    kaiser done",
                           "buffer (5) done",
                           "       abc done"};
static char *set_windowmode(int i)
{
    if (i > 0 && i <= 11)
        return windowmode[i];
    if (i < 0 && i >= -11)
        return windowmode[abs(i) + 11];
    return windowmode[0];
}

static char *fasemode[] = {"       no phase",
                           "       do phase",
                           " power spectrum",
                           " absolute value",
                           "     phase done"};
static char *set_fasemode(int i)
{
    if (i > 0 && i < 4)
        return fasemode[i];
    if (i<0)
        return fasemode[4];
    return fasemode[0];
}

static char *ftmode[] = {  "         no FFT",
                           "    do FFT real",
                           "    do FFT imag",
                           "      do FFT cx",
                           "    do DFT real",
                           "    do DFT imag",
                           "      do DFT cx",
                           "  FFT real done",
                           "  FFT imag done",
                           "    FFT cx done",
                           "  DFT real done",
                           "  DFT imag done",
                           "    DFT cx done"};
static char *set_ftmode(int i)
{
    if (i > 0 && i <= 6)
        return ftmode[i];
    if (i < 0 && i >= -6)
        return ftmode[abs(i) + 6];
    return ftmode[0];
}


static char *baselnmode[]={"    no baseline",
                           "  do polynomial",
                           "  do wndwd poly",
                           "   do table fit",
                           "  do wndwd tble",
                           "        do sine",
                           "  do wndwd sine",
                           "      do cosine",
                           "do wndwd cosine",
                           "      do spline",
                           "do wndwd spline",
                           " do sine-cosine",
                           "do wndwd sincos",
                           " do auto spline",
                           "do wndw aspline",
                           "polynomial done",
                           "wndwd poly done",
                           " table fit done",
                           "wndwd tble done",
                           "      sine done",
                           "wndwd sine done",
                           "    cosine done",
                           "wndwd cosi done",
                           "    spline done",
                           "wndwd spln done",
                           "   sin-cos done",
                           "wndwd sico done",
                           "   aspline done",
                           "wndw aspln done"};
static char *set_baselnmode(int i)
{
    if (i > 0 && i <= 14)
        return baselnmode[i];
    if (i < 0 && i >= -14)
        return baselnmode[abs(i) + 12];
    return baselnmode[0];
}

static char *outmode[] = { "       new file",
                           "      same file",
                           "       old file"};
static char *set_outmode(int i)
{
    i = abs(i);
    if (i > 0 && i <= 2)
        return outmode[i];
    return outmode[0];
}

static char *revmode[] = { "     no reverse",
                           "  reverse array",
                           "reverse records",
                           "   reverse both",
                           " array reversed",
                           "record reversed",
                           "  both reversed"};
static char *set_revmode(int i)
{
    i = max(i,-1);
    i = min(i,1);
    if (i > 0 && i <= 3)
        return revmode[i];
    if (i < 0 && i >= -3)
        return revmode[abs(i) + 3];
    return revmode[0];
}


/*
c
c..... aim: lists parameters from parms
c.....   iunit  integer       : fortran io unit
c.....   string character*(*) : string to be printed above parameters
c.....   parms  real array of iopt*ntnpar elements
c.....                        : parmeter array
c.....   iopt  == [0,1,2,3,4] : iopt=0 : main parameters
c.....                          iopt>0 : iopt times ntnpar parameters
c.....                          skip non-relevant parameters if iopt=1
c.....   ntnpar integer       : number of tn parameters
c
*/
static void parmes(MBLOCK *mblk, int iunit, char *string, float *parms, PBLOCK *dpar, 
            int iopt, int alldata)
{
    int i;
    OLD3D_PAR_RECORD *tpr;

    psnd_printf(mblk,"                     %s\n",string);

    if (iopt == 0) {
        if (parms) {
            tpr  = (OLD3D_PAR_RECORD*) parms;
            psnd_printf(mblk,"  AQ dir      =      %10d\n", tpr->iaqdir);
            psnd_printf(mblk,"  Nextproc dir=      %10d\n", tpr->next_dir);
        }
    }
    else {
        /*
         *.......spectral frequency (domain)
         */
        psnd_printf(mblk,"  Spec   Freq.=");
        for (i=0;i<iopt;i++)
            psnd_printf(mblk,"      %10.3f", (dpar+i)->sfd);
        psnd_printf(mblk,"\n");
        /*
         *.......spectral width
         */
        psnd_printf(mblk,"  Spec  Width =");
        for (i=0;i<iopt;i++)
            psnd_printf(mblk,"      %10.3f", (dpar+i)->swhold);
        psnd_printf(mblk,"\n");
        /*
         *.......reference channel
         */
        psnd_printf(mblk,"  Refer chan  =");
        for (i=0;i<iopt;i++)
            psnd_printf(mblk,"      %10.3f", (dpar+i)->xref);
        psnd_printf(mblk,"\n");
        /*
         *.......reference aref
         */
        psnd_printf(mblk,"  Refer aref  =");
        for (i=0;i<iopt;i++)
            psnd_printf(mblk,"      %10.3f", (dpar+i)->aref);
        psnd_printf(mblk,"\n");
        /*
         *.......reference aref
         */
        psnd_printf(mblk,"  Refer bref  =");
        for (i=0;i<iopt;i++)
            psnd_printf(mblk,"      %10.3f", (dpar+i)->bref);
        psnd_printf(mblk,"\n");
        /*
         *.......dsp shift
         */
        psnd_printf(mblk,"  DSP shift   =");
        for (i=0;i<iopt;i++)
            psnd_printf(mblk,"      %10.3f", (dpar+i)->dspshift);
        psnd_printf(mblk,"\n");
        /*
         *.......pre process mode
         */
        psnd_printf(mblk,"  PRE MODE    =");
        for (i=0;i<iopt;i++)
            psnd_printf(mblk," %15s", set_premode((dpar+i)->ipre));
        psnd_printf(mblk,"\n");
        psnd_printf(mblk,"  R start-stop=");
        for (i=0;i<iopt;i++)
            psnd_printf(mblk,"    %5d -%5d", (dpar+i)->irstrt, (dpar+i)->irstop);
        psnd_printf(mblk,"\n");
        /*
         * Linear prediction
         */
        if (alldata || dpar->nlpc) {
            psnd_printf(mblk,"  LP lpc type =");
            for (i=0;i<iopt;i++)
                psnd_printf(mblk," %15s", set_lpctype((dpar+i)->lpc));
            psnd_printf(mblk,"\n");
            psnd_printf(mblk,"  LP mode     =");
            for (i=0;i<iopt;i++)
                psnd_printf(mblk," %15s", set_lpcmode((dpar+i)->lpcmode));
            psnd_printf(mblk,"\n");
            psnd_printf(mblk,"  LP #poles   =");
            for (i=0;i<iopt;i++)
                psnd_printf(mblk,"           %5d", (dpar+i)->npoles);
            psnd_printf(mblk,"\n");
            psnd_printf(mblk,"  LP size     =");
            for (i=0;i<iopt;i++)
                psnd_printf(mblk,"           %5d", (dpar+i)->nfut);
            psnd_printf(mblk,"\n");
            psnd_printf(mblk,"  LP moveroots=");
            for (i=0;i<iopt;i++)
                psnd_printf(mblk," %15s", set_lpcroots((dpar+i)->mroot));
            psnd_printf(mblk,"\n");
            psnd_printf(mblk,"  LP replace  =");
            for (i=0;i<iopt;i++)
                psnd_printf(mblk," %15s", set_lpcreplace((dpar+i)->replace));
            psnd_printf(mblk,"\n");
            psnd_printf(mblk,"  LP tolerance=");
            for (i=0;i<iopt;i++)
                psnd_printf(mblk,"      %10.0g", (dpar+i)->toler);
            psnd_printf(mblk,"\n");
            psnd_printf(mblk,"  LP gap st-sp=");
            for (i=0;i<iopt;i++)
                psnd_printf(mblk,"    %5d -%5d", (dpar+i)->ngap1, (dpar+i)->ngap2);
            psnd_printf(mblk,"\n");
            psnd_printf(mblk,"  LPstart-stop=");
            for (i=0;i<iopt;i++)
                psnd_printf(mblk,"    %5d -%5d", (dpar+i)->nstart, (dpar+i)->nstop);
            psnd_printf(mblk,"\n");
        }
        /*
         *.......windows
         */
        if (iopt == 1) {
            int i=0,iwindo = abs((dpar)->iwindo);
            if (iwindo != NOWIN) {
                psnd_printf(mblk,"  Window      = %15s\n", 
                    set_windowmode((dpar+i)->iwindo));
                if (iwindo == SNWIN || iwindo == SQWIN ||
                        iwindo == KSWIN) {
                    psnd_printf(mblk,"  Shift       =      %10.3f\n", (dpar+i)->rsh);
                }
                if (iwindo == EMWIN || iwindo == CDWIN ||
                        iwindo == GMWIN) {
                    psnd_printf(mblk,"  Line  Broad =      %10.3f\n", (dpar+i)->rlb);
                }
                if (iwindo == GMWIN) {
                    psnd_printf(mblk,"  Gauss Broad =      %10.3f\n", (dpar+i)->gn);
                }
                if (iwindo == TMWIN) {
                    psnd_printf(mblk,"  Trapzium Wdw=           %5d\n", 
                                    (dpar+i)->itm);
                }
                psnd_printf(mblk,"  W start-stop=    %5d -%5d\n",
                               (dpar+i)->iwstrt,
                               (dpar+i)->iwstop); 
            }
        }
        else {
            psnd_printf(mblk,"  Window      =");
            for (i=0;i<iopt;i++)
                psnd_printf(mblk," %15s", set_windowmode((dpar+i)->iwindo));
            psnd_printf(mblk,"\n");
            psnd_printf(mblk,"  Shift       =");
            for (i=0;i<iopt;i++)
                psnd_printf(mblk,"      %10.3f", (dpar+i)->rsh);
            psnd_printf(mblk,"\n");
            psnd_printf(mblk,"  Line  Broad =");
            for (i=0;i<iopt;i++)
                psnd_printf(mblk,"      %10.3f", (dpar+i)->rlb);
            psnd_printf(mblk,"\n");
            psnd_printf(mblk,"  Gauss Broad =");
            for (i=0;i<iopt;i++)
                psnd_printf(mblk,"      %10.3f", (dpar+i)->gn);
            psnd_printf(mblk,"\n");
            psnd_printf(mblk,"  Trapzium Wdw=");
            for (i=0;i<iopt;i++)
                psnd_printf(mblk,"           %5d", (dpar+i)->itm);
            psnd_printf(mblk,"\n");
            psnd_printf(mblk,"  W start-stop=");
            for (i=0;i<iopt;i++)
                psnd_printf(mblk,"    %5d -%5d",
                             (dpar+i)->iwstrt,
                             (dpar+i)->iwstop);
            psnd_printf(mblk,"\n");
        }
        /*
         * Water Wash
         */
        if (alldata || dpar->watwa) {
            psnd_printf(mblk,"  Watwa shape =");
            for (i=0;i<iopt;i++)
                psnd_printf(mblk,"           Cos^%d", (dpar+i)->iopt);
            psnd_printf(mblk,"\n");
            psnd_printf(mblk,"  Watwa width =");
            for (i=0;i<iopt;i++)
                psnd_printf(mblk,"      %10.0f", (dpar+i)->kc);
            psnd_printf(mblk,"\n");
            psnd_printf(mblk,"  Watwa shift =");
            for (i=0;i<iopt;i++)
                psnd_printf(mblk,"      %10.2f", (dpar+i)->wshift);
            psnd_printf(mblk,"\n");
        }
        /*
         *.......fft
         */
        if (alldata || abs((dpar)->ifft) != NOFFT) {
            psnd_printf(mblk,"  FFT MODE    =");
            for (i=0;i<iopt;i++)
                psnd_printf(mblk," %15s", set_ftmode((dpar+i)->ifft));
            psnd_printf(mblk,"\n");
            psnd_printf(mblk,"  Zero Fill   =");
            for (i=0;i<iopt;i++)
                psnd_printf(mblk,"              %2d", (dpar+i)->nzf);
            psnd_printf(mblk,"\n");
            psnd_printf(mblk,"  Scale 1st   =");
            for (i=0;i<iopt;i++)
                psnd_printf(mblk,"            %.2f", (dpar+i)->ftscale);
            psnd_printf(mblk,"\n");
        }
        /*
         * Hilbert transform
         */
        if (alldata || (dpar)->hilbert) {
            psnd_printf(mblk,"               ");
            for (i=0;i<iopt;i++)
                psnd_printf(mblk," %15s", set_hilberttr((dpar+i)->hilbert));
            psnd_printf(mblk,"\n");
        }
        /*
         *.......phase
         */
        if (alldata || abs((dpar)->iphase) != NOPHA) {
            psnd_printf(mblk,"  PHASE       =");
            for (i=0;i<iopt;i++)
                psnd_printf(mblk," %15s", set_fasemode(abs((dpar+i)->iphase)));
            psnd_printf(mblk,"\n");
            if (abs((dpar)->iphase) == DOPHA) {
                psnd_printf(mblk,"  0 ord phase =");
                for (i=0;i<iopt;i++)
                    psnd_printf(mblk,"      %10.3f",(dpar+i)->ahold);
                psnd_printf(mblk,"\n");
                psnd_printf(mblk,"  1 ord phase =");
                for (i=0;i<iopt;i++)
                    psnd_printf(mblk,"      %10.3f", (dpar+i)->bhold);
                psnd_printf(mblk,"\n");
                psnd_printf(mblk,"  I0 phase pnt=");
                for (i=0;i<iopt;i++)
                    psnd_printf(mblk,"           %5d", (dpar+i)->ihold);
                psnd_printf(mblk,"\n");
            }
        }
        /*
         *......reversing
         */
        if (alldata || abs((dpar)->irever) != NOREV) {
            psnd_printf(mblk,"               ");
            for (i=0;i<iopt;i++)
                psnd_printf(mblk," %15s", set_revmode((dpar+i)->irever));
            psnd_printf(mblk,"\n");
        }
        /*
         *.......baseline
         */
        if (alldata || abs((dpar)->ibase) != NOBAS) {
            psnd_printf(mblk,"  BASEL. TYPE =");
            for (i=0;i<iopt;i++)
                psnd_printf(mblk," %15s", set_baselnmode((dpar+i)->ibase));
            psnd_printf(mblk,"\n");
            psnd_printf(mblk,"  L poly terms=");
            for (i=0;i<iopt;i++)
                psnd_printf(mblk,"           %5d", (dpar+i)->iterms2);
            psnd_printf(mblk,"\n");
            psnd_printf(mblk,"  H poly terms=");
            for (i=0;i<iopt;i++)
                psnd_printf(mblk,"           %5d", (dpar+i)->iterms);
            psnd_printf(mblk,"\n");
            psnd_printf(mblk,"  B start-stop=");
            for (i=0;i<iopt;i++)
                psnd_printf(mblk,"    %5d -%5d", 
                             (dpar+i)->ibstrt,
                             (dpar+i)->ibstop);
            psnd_printf(mblk,"\n");
        }
        /*
         * Water fit
         */
        if (alldata || (dpar)->waterfit) {
            psnd_printf(mblk,"  Waterfit pos=");
            for (i=0;i<iopt;i++)
                psnd_printf(mblk,"           %5d", (dpar+i)->waterpos);
            psnd_printf(mblk,"\n");
            psnd_printf(mblk,"  Waterfit wid=");
            for (i=0;i<iopt;i++)
                psnd_printf(mblk,"           %5d", (dpar+i)->waterwid);
            psnd_printf(mblk,"\n");
        }
        /*
         *.......post process modes
         */
        psnd_printf(mblk,"  STORE MODE  =");
        for (i=0;i<iopt;i++)
            psnd_printf(mblk," %15s", set_postmode((dpar+i)->ipost));
        psnd_printf(mblk,"\n");
        /*
         *.......files
         */
/*
        psnd_printf(mblk,"  OUTFILE MODE=");
        for (i=0;i<iopt;i++)
            psnd_printf(mblk," %15s", set_outmode((dpar+i)->ifilut));
        psnd_printf(mblk,"\n");
*/
        psnd_printf(mblk,"  S start-stop=");
        for (i=0;i<iopt;i++)
            psnd_printf(mblk,"    %5d -%5d", 
                         (dpar+i)->isstrt,
                         (dpar+i)->isstop);
        psnd_printf(mblk,"\n");
    }

}


void psnd_lp(MBLOCK *mblk, PBLOCK *par)
{
    int dum1 = 1;
    char string[100];
    
    psnd_printf(mblk,"                     Current T%d\n\n", par->icrdir);
    psnd_printf(mblk,"  Spec   Freq.=      %10.3f\n", par->sfd);
    psnd_printf(mblk,"  Spec  Width =      %10.3f\n", par->swhold);
    psnd_printf(mblk,"  Refer chan  =      %10.3f\n", par->xref);
    psnd_printf(mblk,"  Refer aref  =      %10.3f\n", par->aref);
    psnd_printf(mblk,"  Refer bref  =      %10.3f\n", par->bref);
    if (par->dspshift != 0.0)
        psnd_printf(mblk,"  DSP shift   =      %10.3f\n", par->dspshift);
    psnd_printf(mblk,"  PRE MODE    = %15s\n", set_premode(par->ipre));
    psnd_printf(mblk,"  R start-stop=    %5d -%5d\n", par->irstrt, par->irstop);
    if (par->nlpc) {
        psnd_printf(mblk,"  LP lpc type = %15s\n", set_lpctype(par->lpc));
        psnd_printf(mblk,"  LP mode     = %15s\n", set_lpcmode(par->lpcmode));
        psnd_printf(mblk,"  LP #poles   =           %5d\n", par->npoles);
        if (par->lpcmode != PREDICT_GAP)
            psnd_printf(mblk,"  LP size     =           %5d\n", par->nfut);
        psnd_printf(mblk,"  LP moveroots= %15s\n", set_lpcroots(par->mroot));
        if (par->lpc == PREDICT_HSVD) {
            psnd_printf(mblk,"  LP replace  = %15s\n", set_lpcreplace(par->replace));
            psnd_printf(mblk,"  LP tolerance=      %10.0g\n", par->toler);
        }
        if (par->lpcmode == PREDICT_GAP) 
            psnd_printf(mblk,"  LP gap st-sp=    %5d -%5d\n", par->ngap1, par->ngap2);
        psnd_printf(mblk,"  LPstart-stop=    %5d -%5d\n", par->nstart, par->nstop);
    }
    psnd_printf(mblk,"  Window      = %15s\n", set_windowmode(par->iwindo));
    if (par->iwindo != NOWIN) {
        if (par->iwindo == SNWIN || par->iwindo == SQWIN ||
                par->iwindo == KSWIN) 
            psnd_printf(mblk,"  Shift       =      %10.3f\n", par->rsh);
        if (par->iwindo == EMWIN || par->iwindo == CDWIN ||
                par->iwindo == GMWIN) 
            psnd_printf(mblk,"  Line  Broad =      %10.3f\n", par->rlb);
        if (par->iwindo == GMWIN) 
            psnd_printf(mblk,"  Gauss Broad =      %10.3f\n", par->gn);
        if (par->iwindo == TMWIN) 
            psnd_printf(mblk,"  Trapzium Wdw=           %5d\n", par->itm);
        psnd_printf(mblk,"  W start-stop=    %5d -%5d\n", par->iwstrt, par->iwstop);
    }
    if (par->watwa) {
        psnd_printf(mblk,"  Watwa shape =           Cos^%d\n", par->iopt);
        psnd_printf(mblk,"  Watwa width =      %10.0f\n", par->kc);
        psnd_printf(mblk,"  Watwa shift =      %10.2f\n", par->wshift);
    }
    if (par->ifft != NOFFT) {
        psnd_printf(mblk,"  FFT MODE    = %15s\n", set_ftmode(par->ifft));
        psnd_printf(mblk,"  Zero Fill   =              %2d\n", par->nzf);
        if (par->ftscale != 0.0 && par->ftscale != 1.0)
            psnd_printf(mblk,"  Scale 1st   =            %.2f\n", par->ftscale);
    }
    if (par->hilbert)
        psnd_printf(mblk,"                %15s\n", set_hilberttr(par->hilbert));
    if (par->iphase != NOPHA) {
        psnd_printf(mblk,"  PHASE       = %15s\n", set_fasemode(par->iphase));
        if  (par->iphase == DOPHA) {
            psnd_printf(mblk,"  0 ord phase =      %10.3f\n", par->ahold);
            psnd_printf(mblk,"  1 ord phase =      %10.3f\n", par->bhold);
            psnd_printf(mblk,"  I0 phase pnt=           %5d\n", par->ihold);
        }
    }
    if (premode[par->irever] == NOREV)
        psnd_printf(mblk,"                %15s\n", set_revmode(par->irever));
    if (par->ibase != NOBAS) {
        psnd_printf(mblk,"  BASEL. TYPE = %15s\n", set_baselnmode(par->ibase));
        if (par->iterms2) {
            psnd_printf(mblk,"  L poly terms=           %5d\n", par->iterms2);
            psnd_printf(mblk,"  H poly terms=           %5d\n", par->iterms);
        }
        else
            psnd_printf(mblk,"  # poly terms=           %5d\n", par->iterms);
        psnd_printf(mblk,"  B start-stop=    %5d -%5d\n", par->ibstrt, par->ibstop);
    }
    if (par->waterfit) {
        psnd_printf(mblk,"  Waterfit pos=           %5d\n", par->waterpos);
        psnd_printf(mblk,"  Waterfit wid=           %5d\n", par->waterwid);
    }
    psnd_printf(mblk,"  STORE MODE  = %15s\n", set_postmode(par->ipost));
/*
    psnd_printf(mblk,"  OUTFILE MODE= %15s\n", set_outmode(par->ifilut));
*/
    psnd_printf(mblk,"  S start-stop=    %5d -%5d\n", par->isstrt, par->isstop);

}


void psnd_la(MBLOCK *mblk, int ndim, int dim, int alldata, DBLOCK *dat)
{
    char *p,string[100];
    PBLOCK *par0;
    int idir;
    
    ndim = min(ndim, MAX_DIM);
    if (dim > MAX_DIM)
        return;
    par0 = (PBLOCK*) calloc(sizeof(PBLOCK),MAX_DIM);
    assert(par0);
    p = string;
    p[0] = '\0';
    if (ndim == 1) {
        dim = max(1,dim);
        par0->icrdir = dim;
        psnd_pargtt(dat->xpar, dim, par0);
        sprintf(p, "T%d", dim);
    }
    else {
        for (idir=0;idir<ndim;idir++) {
            par0[idir].icrdir = idir+1;
            psnd_pargtt(dat->xpar, idir+1, par0 + idir);
            if (idir+1 == ndim)
                sprintf(p, "T%d", idir+1);
            else
                sprintf(p, "T%d              ", idir+1);
            p += strlen(p);
        }
    }

    parmes(mblk,dat->finfo[INFILE].ifile,
           string,
           dat->xpar,
           par0,
           ndim,
           alldata);

    free(par0);
}

void psnd_list_all_current_parameters(MBLOCK *mblk, int ndim, int alldata, DBLOCK *dat, PBLOCK *par0)
{
    char *p,string[100];
    int idir;
    
    ndim = min(ndim, MAX_DIM);

    p = string;
    p[0] = '\0';
    for (idir=0;idir<ndim;idir++) {
        if (idir+1 == ndim)
            sprintf(p, "T%d", idir+1);
        else
            sprintf(p, "T%d              ", idir+1);
        p += strlen(p);
    }
    parmes(mblk,dat->finfo[INFILE].ifile,
           string,
           dat->xpar,
           par0,
           ndim,
           alldata);

}




