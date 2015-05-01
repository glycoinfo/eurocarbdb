/********************************************************************/
/*                             psnd_param.c                         */
/*                                                                  */
/* View and set parameters                                          */
/* 1997, Albert van Kuik                                            */
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


int psnd_popup_zoomrange(MBLOCK *mblk)
{
    int  id,cont_id,vp_id;
    G_POPUP_CHILDINFO ci[40];
    char *label;
    float xmin, ymin, xmax, ymax;
    SBLOCK *spar = mblk->spar;
    CBLOCK *cpar = mblk->cpar_screen;
    PBLOCK *par  = PAR;
    DBLOCK *dat  = DAT;

    vp_id = psnd_get_vp_id(mblk), 
    g_get_world(vp_id, &xmin, &ymin, &xmax, &ymax);
          

    xmax = psnd_calc_pos(xmax, par->xref, par->aref, par->bref, dat->irc,
               dat->isspec, spar[S_AXISX_MARK].show, dat->isize, 
               dat->sw, par->sfd, NULL, NULL);
    xmin = psnd_calc_pos(xmin, par->xref, par->aref, par->bref, dat->irc,
               dat->isspec, spar[S_AXISX_MARK].show, dat->isize, 
               dat->sw, par->sfd, NULL, NULL);
    if (mblk->info->plot_mode == PLOT_2D) {
        ymax = psnd_calc_pos(ymax, cpar->par2->xref, 
                              cpar->par2->aref, cpar->par2->bref,
                              cpar->ihbmin, cpar->par2->isspec, 
                              spar[S_AXISY_MARK].show, 
                              cpar->par2->nsiz, cpar->par2->swhold, 
                              cpar->par2->sfd,  NULL, NULL);
        ymin = psnd_calc_pos(ymin, cpar->par2->xref, 
                              cpar->par2->aref, cpar->par2->bref,
                              cpar->ihbmin, cpar->par2->isspec, 
                              spar[S_AXISY_MARK].show, 
                              cpar->par2->nsiz, cpar->par2->swhold, 
                              cpar->par2->sfd,  NULL, NULL);
    }

    cont_id = g_popup_container_open(mblk->info->win_id, 
                     "Zoom Settings (PX/PY)", G_POPUP_WAIT);
    id=0;
    label = psnd_sprintf_temp("%g", xmin);
    popup_add_text2(cont_id, &(ci[id]), 0, label, "Left");
    id++;
    label = psnd_sprintf_temp("%g", xmax);
    popup_add_text2(cont_id, &(ci[id]), 0, label, "Right");
    id++;
    label = psnd_sprintf_temp("%g", ymin);
    popup_add_text2(cont_id, &(ci[id]), 0, label, "Bottom");
    id++;
    label = psnd_sprintf_temp("%g", ymax);
    popup_add_text2(cont_id, &(ci[id]), 0, label, "Top");

    if (g_popup_container_show(cont_id)) {
        id=0;
        xmin = psnd_scan_float(ci[id].label);
        id++;
        xmax = psnd_scan_float(ci[id].label);
        id++;
        ymin = psnd_scan_float(ci[id].label);
        id++;
        ymax = psnd_scan_float(ci[id].label);
        xmin = psnd_calc_channels(xmin, par->xref,
                                   par->aref, par->bref, dat->irc, 
                                   dat->isspec, spar[S_AXISX_MARK].show, 
                                   dat->isize, dat->sw, par->sfd);
        xmax = psnd_calc_channels(xmax, par->xref,
                                   par->aref, par->bref, dat->irc, 
                                   dat->isspec, spar[S_AXISX_MARK].show, 
                                   dat->isize, dat->sw, par->sfd);
        if (mblk->info->plot_mode == PLOT_2D) {
            ymax = psnd_calc_channels(ymax, cpar->par2->xref, 
                              cpar->par2->aref, cpar->par2->bref, 
                              cpar->ihbmin, cpar->par2->isspec, 
                              spar[S_AXISY_MARK].show, 
                              cpar->par2->nsiz, cpar->par2->swhold, 
                              cpar->par2->sfd);
            ymin = psnd_calc_channels(ymin, cpar->par2->xref, 
                              cpar->par2->aref, cpar->par2->bref, 
                              cpar->ihbmin, cpar->par2->isspec, 
                              spar[S_AXISY_MARK].show, 
                              cpar->par2->nsiz, cpar->par2->swhold, 
                              cpar->par2->sfd);
        }
        psnd_push_zoomundo(vp_id, xmin, ymin, xmax, ymax, dat);
        psnd_scrollbars_reconnect(mblk,vp_id, xmin, ymin, xmax, ymax, TRUE);
        return TRUE;
    }
    return FALSE;
}

/***************************************************************
*
*/

int param_add_label(int cont_id, G_POPUP_CHILDINFO ci[], int id, 
                    int dir, char *lab)
{
    char *label;
    label = psnd_sprintf_temp("Direction %d. %s", dir, lab);
    popup_add_separator(cont_id, &(ci[id]));
    id++;
    popup_add_label(cont_id, &(ci[id]), label);
    return id;
}

int popup_add_text4(int cont_id, G_POPUP_CHILDINFO *ci, int id, 
                     char *title, const char *format, ...)
{
    va_list argp;
    static char buf[200];

    va_start(argp, format);
    vsprintf(buf, format, argp);
    va_end(argp);

    g_popup_init_info(ci);
    ci->type          = G_CHILD_TEXTBOX;
    ci->id            = id;
    ci->item_count    = 40;
    ci->items_visible = 20;
    ci->title         = title;
    ci->label         = buf;
    ci->horizontal    = TRUE;
    g_popup_add_child(cont_id, ci);
    return 1;
}

int popup_add_text5(MBLOCK *mblk, int cont_id, G_POPUP_CHILDINFO *ci, int id, 
                    void (*func)(G_POPUP_CHILDINFO *ci),
                    char *title, const char *format, ...)
{
    va_list argp;
    static char buf[200];

    va_start(argp, format);
    vsprintf(buf, format, argp);
    va_end(argp);

    g_popup_init_info(ci);
    ci->type          = G_CHILD_TEXTBOX;
    ci->id            = id;
    ci->item_count    = 40;
    ci->items_visible = 20;
    ci->title         = title;
    ci->label         = buf;
    ci->horizontal    = TRUE;
    ci->func          = func;
    ci->userdata      = (void*)mblk;
    g_popup_add_child(cont_id, ci);
    return 1;
}

void popup_add_option3(MBLOCK *mblk, int cont_id, G_POPUP_CHILDINFO *ci, int id, 
                           void (*func)(G_POPUP_CHILDINFO *ci),
                           char *label, int select, int item_count,
                           char **item_labels)
{
    g_popup_init_info(ci);
    ci->type          = G_CHILD_OPTIONMENU;
    ci->id            = id;
    ci->item_count    = item_count;
    ci->item          = select;
    ci->data          = item_labels;
    ci->horizontal    = TRUE;
    ci->label         = label;
    ci->func          = func;
    ci->userdata      = (void*)mblk;
    g_popup_add_child(cont_id, ci);
}


void popup_add_spin2(int cont_id, G_POPUP_CHILDINFO *ci, int id, 
                           char *label, int select, int *min_max_dec_step)
{
    g_popup_init_info(ci);
    ci->type = G_CHILD_SPINBOX;
    ci->id         = id;
    ci->item       = select;
    ci->items_visible = 5;
    ci->label      = label;
    ci->select     = min_max_dec_step;
    g_popup_add_child(cont_id, ci);

}

int popup_add_panel(int cont_id, G_POPUP_CHILDINFO *ci, int id, 
                           int horizontal, int open, int frame)
{
    g_popup_init_info(ci + id);
    ci[id].type       = G_CHILD_PANEL;
    ci[id].item       = open;
    ci[id].frame      = frame;
    ci[id].horizontal = horizontal;
    g_popup_add_child(cont_id, ci + id);
    return id;
}



int param_add_freq(int cont_id, G_POPUP_CHILDINFO ci[], int id,
                    PBLOCK *par)
{
    static char *items[] = {"FID" , "Spectrum" };
    id  = param_add_label(cont_id, ci, id, par->icrdir, "SPECTRAL INFO")+1;
    popup_add_text4(cont_id, ci + id, ID_SFD, 
                     "Spectrometer Freq", "%1.3f", par->sfd);
    id++;
    popup_add_text4(cont_id, &(ci[id]), ID_SWHOLD, 
                     "Spectral Width   ","%1.2f", par->swhold);
    id++;
    popup_add_text4(cont_id, &(ci[id]), ID_XREF, 
                     "Reference channel", "%f", par->xref);
    id++;
    popup_add_text4(cont_id, &(ci[id]), ID_AREF, 
                     "Reference scale  ", "%g", par->aref);
    id++;
    popup_add_text4(cont_id, &(ci[id]), ID_BREF, 
                     "Reference offset ", "%g", par->bref);
    id++;
    popup_add_option(cont_id, &(ci[id]), ID_RECTYPE, 
                     "File Record Type ", par->isspec, 2, items);
    id++;
    popup_add_text4(cont_id, &(ci[id]), ID_TD, 
                     "Size Time Domain ", "%d", par->td);
    return id;
}

int param_add_freq2(int cont_id, G_POPUP_CHILDINFO ci[], int id,
                    DBLOCK *dat)
{
    static char *items[] = {"FID" , "Spectrum" };
    id++;
    popup_add_option(cont_id, &(ci[id]), ID_RECTYPE_CURRENT, 
                     "This Record Type ", dat->isspec, 2, items);
    return id;
}

int param_add_pre(int cont_id, G_POPUP_CHILDINFO ci[], int id,
                    PBLOCK *par)
{
    static char *items[] = {   "Read real", 
                        "Read i/r swapped", "Read complex" };
    id = param_add_label(cont_id, ci, id, par->icrdir, "PRE-PROCESSING")+1;
    popup_add_option(cont_id, &(ci[id]), ID_IPRE, 
                     "Pre processing   ", max(1,par->ipre) - 1, 3, items);
    id++;
    popup_add_text4(cont_id, &(ci[id]), ID_IRSTRT, 
                     "Read Start       ", "%d", par->irstrt);
    id++;
    popup_add_text4(cont_id, &(ci[id]), ID_IRSTOP, 
                     "Read Stop        ", "%d", par->irstop);
    return id;
}


int param_add_dsp(int cont_id, G_POPUP_CHILDINFO ci[], int id,
                    PBLOCK *par)
{
    static char *dspitems[] = {"No DSP shift     " ,  
                        "Do DSP shift     " };
    id = param_add_label(cont_id, ci, id, par->icrdir, "DSP CORRECTION")+1;
    popup_add_option(cont_id, &(ci[id]), ID_DSPFLAG, 
                     "Do DSP correction", par->dspflag, 2, dspitems);
    id++;
    popup_add_text4(cont_id, &(ci[id]), ID_DSPSHIFT, 
                     "DSP shift        ", "%.4f", par->dspshift);
    return id;
}


int param_add_watwa(MBLOCK *mblk, int cont_id, G_POPUP_CHILDINFO ci[], int id,
                    void (*func)(G_POPUP_CHILDINFO *ci),
                    PBLOCK *par)
{
    static char *items[] = {"No WatWa" ,  
                     "Do WatWa" };
    static char *items2[] = { "Cos", 
                       "Cos^2", 
                       "Cos^3",
                       "Cos^4",
                       "Cos^5",
                       "Cos^6"} ;
    id = param_add_label(cont_id, ci, id, par->icrdir, "WATWA")+1;
    popup_add_option(cont_id, &(ci[id]), ID_WATWA, 
                     "Watwa mode       ", par->watwa, 2, items);

    id++;
    popup_add_option3(mblk, cont_id, &(ci[id]), ID_IOPT,  func,
                     "Line shape       ", par->iopt-1, 6, items2);

    id++;
    popup_add_text5(mblk, cont_id, &(ci[id]), ID_KC,  func,
                     "Convolution Width", "%.1f", par->kc);
    id++;
    popup_add_text5(mblk, cont_id, &(ci[id]), ID_WSHIFT,  func,
                     "Shift            ", "%.2f", par->wshift);
    return id;
}

int param_add_linpar(MBLOCK *mblk, int cont_id, G_POPUP_CHILDINFO ci[], int id,
                    void (*func)(G_POPUP_CHILDINFO *ci),
                    PBLOCK *par)
{
    static char *items[]  = { "LPC", "HSVD" };
    static char *items2[] = { "No" , "Yes" };
    static char *items3[] = { "Forward", "Backward", "Gap" } ;

    id = param_add_label(cont_id, ci, id, par->icrdir, "LINEAR PREDICTION")+1;
    popup_add_option3(mblk, cont_id, &(ci[id]), ID_NLPC, func,
                     "Do prediction    ", par->nlpc, 2, items2);
    id++;
    popup_add_option3(mblk, cont_id, &(ci[id]), ID_LPC,  func,
                     "Prediction type  ", par->lpc, 2, items);
    id++;
    popup_add_option3(mblk, cont_id, &(ci[id]), ID_LPCMODE,  func,
                     "Direction        ", par->lpcmode, 3, items3);
    id++;
    popup_add_text5(mblk, cont_id, &(ci[id]), ID_NPOLES,  func,
                     "Number of poles  ", "%d", par->npoles);
    id++;
    popup_add_text5(mblk, cont_id, &(ci[id]), ID_NFUT,  func,
                     "Size to predict  ", "%d", par->nfut);
    id++;
    popup_add_option3(mblk, cont_id, &(ci[id]), ID_MROOT,  func,
                     "Move roots       ", par->mroot, 2, items2);
    id++;
    popup_add_option3(mblk, cont_id, &(ci[id]), ID_REPLACE,  func,
                     "Replace fid      ", par->replace, 2, items2);
    id++;
    popup_add_text5(mblk, cont_id, &(ci[id]), ID_TOLER,  func,
                     "Tolerance        ", "%g", par->toler);
    id++;
    popup_add_text5(mblk, cont_id, &(ci[id]), ID_NGAP1,  func,
                     "Gap start        ", "%d", par->ngap1);
    id++;
    popup_add_text5(mblk, cont_id, &(ci[id]), ID_NGAP2,  func,
                     "Gap stop         ", "%d", par->ngap2);
    id++;
    popup_add_text5(mblk, cont_id, &(ci[id]), ID_NSTART,  func,
                     "Prediction Start ", "%d", par->nstart);
    id++;
    popup_add_text5(mblk, cont_id, &(ci[id]), ID_NSTOP,  func,
                     "Prediction Stop  ", "%d", par->nstop);
    return id;
}


static void add_showwin_button(MBLOCK *mblk, int cont_id, G_POPUP_CHILDINFO *ci, int id,
                    void (*func)(G_POPUP_CHILDINFO *ci))

{
    g_popup_init_info(ci);
    ci->type       = G_CHILD_PUSHBUTTON;
    ci->id         = id;
    ci->label      = "Show Window";
    ci->func       = func;
    ci->userdata   = (void*)mblk;
    g_popup_add_child(cont_id, ci);
}

int param_add_window1(MBLOCK *mblk, int cont_id, G_POPUP_CHILDINFO ci[], int id,
                    void (*func)(G_POPUP_CHILDINFO *ci),
                    PBLOCK *par)
{
    static char *items2[] = { "No window        ", 
                       "Do window        " };
    static char *items[]  = { "Hamming        HM", 
                       "Hanning        HN", 
                       "Sine bell      SN", 
                       "Sqr sine bell  SQ", 
                       "Exp multipl    EM", 
                       "Lorentz-gauss  GM",
                       "Conv diff      CD", 
                       "Trapezian mul  TM", 
                       "Kaiser         KS", 
                       "Buffer A       WB" };
    static char *items3[] = { "Hamming          ", 
                       "Hanning          ", 
                       "Sine bell        ", 
                       "Sqr sine bell    ", 
                       "Exp multipl      ", 
                       "Lorentz-gauss    ",
                       "Conv diff        ", 
                       "Trapezian mul    ", 
                       "Kaiser           ", 
                       "Buffer A         " };
    static char *labels[] = {
                       "Window           ",
                       "Window type      " };

    id = param_add_label(cont_id, ci, id, par->icrdir, "WINDOW")+1;
    popup_add_option3(mblk, cont_id, &(ci[id]), ID_NWINDO, 
                     func,
                     labels[0], par->nwindo, 2, items2);
    id++;
    if (par->iwindo == 0)
        par->iwindo = SNWIN;
    if (func) {
        popup_add_option3(mblk, cont_id, &(ci[id]), ID_IWINDO, 
                     func,
                     labels[1], par->iwindo-1, 10, items);
    }
    else
        popup_add_option(cont_id, &(ci[id]), ID_IWINDO, 
                     labels[1], par->iwindo-1, 10, items3);
    return id;
}

int param_add_window2(MBLOCK *mblk, int cont_id, G_POPUP_CHILDINFO ci[], int id,
                    void (*func)(G_POPUP_CHILDINFO *ci),
                    PBLOCK *par)
{
    static char *labels[] = {
        "Right shift    SH",
        "Line broad.    LB",
        "Gaussian broad GB",
        "Trapezian chan TM"};

    popup_add_text5(mblk, cont_id, &(ci[id]), ID_RSH, func,
                     labels[0], "%1.2f", par->rsh);
    
    id++;
    popup_add_text5(mblk, cont_id, &(ci[id]), ID_RLB, func,
                     labels[1], "%1.2f", par->rlb);
    
    id++;
    popup_add_text5(mblk, cont_id, &(ci[id]), ID_GN, func,
                     labels[2], "%1.2f", par->gn);

    id++;
    popup_add_text5(mblk, cont_id, &(ci[id]), ID_ITM,  func,
                     labels[3], "%d", par->itm);
    return id;
}


int param_add_window3(MBLOCK *mblk, int cont_id, G_POPUP_CHILDINFO ci[], int id,
                      void (*func)(G_POPUP_CHILDINFO *ci),
                      PBLOCK *par)
{
    popup_add_text5(mblk, cont_id, &(ci[id]), ID_IWSTRT, func,
                     "Start window     ", "%d", par->iwstrt);

    id++;
    popup_add_text5(mblk, cont_id, &(ci[id]), ID_IWSTOP, func,
                     "Stop window      ", "%d", par->iwstop);
    if (func) {
        id++;
        add_showwin_button(mblk, cont_id,  &(ci[id]), 0, func);
    }
    return id;
}

/*
#define NOFFT        0
#define FFTREA       1
#define FFTIMA       2
#define FFTCX        3
#define DFTREA       4
#define DFTIMA       5
#define DFTCX        6
*/

static int user2ft_modes[] = {
    0,
    0,
    0,
    1,
    2,
    2,
    3
};

int psnd_user2ft_modes( PBLOCK *par)
{
    return user2ft_modes[par->ifft];
}

int  param_add_fourier1(MBLOCK *mblk, int cont_id, G_POPUP_CHILDINFO ci[], int id,
                       void (*func)(G_POPUP_CHILDINFO *ci),
                        PBLOCK *par)
{
    static char *items[] = { "Do FFT real", 
                             "Do FFT complex",
                             "Do DFT real", 
                             "Do DFT complex" };
    static char *items2[] = {"No Fourier" , "Do Fourier" };

    id = param_add_label(cont_id, ci, id, par->icrdir, "FOURIER TRANSFORM")+1;
    popup_add_option3(mblk, cont_id, &(ci[id]), ID_NFFT, func,
                     "Fourier transform", par->nfft, 2, items2);
    id++;
    if (par->ifft == 0)
        par->ifft = 1;
    popup_add_option3(mblk, cont_id, &(ci[id]), ID_IFFT, func,
                     "Fourier mode     ", user2ft_modes[par->ifft], 4, items);
    return id;
}
    
int  param_add_fourier2(MBLOCK *mblk, int cont_id, G_POPUP_CHILDINFO ci[], int id,
                       void (*func)(G_POPUP_CHILDINFO *ci),
                        PBLOCK *par)
{
    int i, ntot, count=0, size, div = 1;
    static char *label[80];

    if (par->ifft == 0)
        par->ifft = FFTREA;
    if (par->ifft == FFTREA || par->ifft == FFTIMA)
        div=2;
    if (DAT->isspec == FALSE) {
        ntot = size = DAT->isize;
    }
    else
        ntot = size = par->td;
    for (i=0;ntot <= MAXSIZ && i < 80;i++) {
        int ntot2;
        ntot  = power(size);
        ntot *= pow(2,i);
        ntot2 = ntot/div;
        if (ntot2 <= MAXSIZ) {
            if (!label[i])
                label[i] = (char*)malloc(40);
            if (ntot2 > 1024)
                sprintf(label[i],"%d (size = %dk)", i, ntot2/1024);
            else
                sprintf(label[i],"%d (size = %d points)", i, ntot2);
            count++;
        }
    }
    g_popup_init_info(&(ci[id]));
    ci[id].type = G_CHILD_SPINBOXTEXT;
    ci[id].id         = ID_NZF;
    ci[id].item       = par->nzf;
    ci[id].item_count = count;
    ci[id].label      = "Zero fill     ZF ";
    ci[id].data       = label;
    ci[id].func       = func;
    ci[id].userdata   = (void*)mblk;
    g_popup_add_child(cont_id, &(ci[id]));

    id++;
    popup_add_text5(mblk, cont_id, &(ci[id]), ID_FTSCALE, func,
                     "Scale first point", "%.2f", par->ftscale);

    return id;
}
        

int param_add_hilbert(int cont_id, G_POPUP_CHILDINFO ci[], int id,
                    PBLOCK *par)
{
    static char *items[] = {"No Hilbert" ,  
                     "Do Hilbert" };

    id = param_add_label(cont_id, ci, id, par->icrdir, "HILBERT TRANSFORM")+1;
    popup_add_option(cont_id, &(ci[id]), ID_HILBERT, 
                     "Restore Imaginary", par->hilbert, 2, items);


    return id;
}

int param_add_phase(int cont_id, G_POPUP_CHILDINFO ci[], int id,
                    PBLOCK *par)
{
    static char *items[] = {"No phase" , "Do phase", "Power Spectrum",
                     "Absolute Value" };

    id = param_add_label(cont_id, ci, id, par->icrdir, "PHASE")+1;
    popup_add_option(cont_id, &(ci[id]), ID_PHASE, 
                     "Phase            ", par->iphase, 4, items);

    id++;
    popup_add_text4(cont_id, &(ci[id]), ID_A,  
                     "0 ord phase    PA", "%f", par->ahold);
    id++;
    popup_add_text4(cont_id, &(ci[id]), ID_B,  
                     "1 ord phase    PB", "%f", par->bhold);
    id++;
    popup_add_text4(cont_id, &(ci[id]), ID_I0,  
                     "I0 phase point I0", "%d", par->ihold);
    return id;
}

static int post2user_modes[] = {
    NOPST,
    PSTREA,
    PSTBOT
};

/*
#define NOPST        0
#define PSTREA       1
#define PSTIMA       2
#define PSTBOT       3
*/

static int user2post_modes[] = {
    0,
    1,
    1,
    2
};

int param_add_post(int cont_id, G_POPUP_CHILDINFO ci[], int id,
                    PBLOCK *par)
{
    static char *items[] = {"Automatic" , "Store real", 
                     "Store complex" };

    id = param_add_label(cont_id, ci, id, par->icrdir, "STORE-MODE")+1;
    popup_add_option(cont_id, &(ci[id]), ID_IPOST, 
                     "Mode             ", user2post_modes[par->ipost], 3, items);
    return id;
}


int param_add_reverse(int cont_id, G_POPUP_CHILDINFO ci[], int id,
                    PBLOCK *par)
{
    static char *items[] = {"No reverse" , "Reverse arrays", 
                     "Reverse records", "Reverse both" };

    id = param_add_label(cont_id, ci, id, par->icrdir, "REVERSE")+1;
    popup_add_option(cont_id, &(ci[id]), ID_IREVER, 
                     "Reverse          ", min(par->irever,1), 2, items);
    return id;
}

int baseline_ids[] = {
    BASPL1,
    BASTB1,
    BASSN1,
    BASCS1,
    BASSC1,
    BASSP1,
    BASAS1,
    -1
};
int baseline_wids[] = {
    BASPL2,
    BASTB2,
    BASSN2,
    BASCS2,
    BASSC2,
    BASSP2,
    BASAS2,
    -1
};

void param_convert_baseline_type(PBLOCK *par, int *select, int *window_select)
{
    int i;
    
    if (par->ibase == 0)
        par->ibase = BASPL1;
    *select = -1;
    *window_select = 0;
    for (i=0;baseline_ids[i] != -1 && *select == -1;i++) {
        if (par->ibase == baseline_ids[i])
            *select = i;
    }
    for (i=0;baseline_wids[i] != -1 && *select == -1;i++) {
        if (par->ibase == baseline_wids[i]) {
            *window_select = 1;
            *select = i;
        }
    }
}

int param_add_baseline1(MBLOCK *mblk, int cont_id, G_POPUP_CHILDINFO ci[], int id,
                       void (*func)(G_POPUP_CHILDINFO *ci),
                       PBLOCK *par)
{
    static char *items[] = { "Polynomial", "Table fit", 
                      "Sine", "Cosine", "Sine+Cosine", "Spline", "Auto Spline" };
    static char *window_items[] = {"Off" , "On" };
    int select, window_select;

    id = param_add_label(cont_id, ci, id, par->icrdir, "BASELINE")+1;
    popup_add_option3(mblk, cont_id, &(ci[id]), ID_NBASE, func,
                     "Baseline         ", par->nbase, 2, window_items);
    id++;

    param_convert_baseline_type(par, &select, &window_select);

    popup_add_option3(mblk, cont_id, &(ci[id]), ID_IBASE,  func,
                     "Baseline type    ", select, 7, items);
    id++;
    popup_add_option3(mblk, cont_id, &(ci[id]), ID_IBASE_WND,  func,
                     "Window (region)  ", window_select, 2, window_items);
    return id;
}

int param_add_baseline2(MBLOCK *mblk, int cont_id, G_POPUP_CHILDINFO ci[], int id,
                       void (*func)(G_POPUP_CHILDINFO *ci),
                       PBLOCK *par)
{
    popup_add_text5(mblk, cont_id, &(ci[id]), ID_ITERMS, func,
                     "# of poly terms  ", "%d", par->iterms);

    id++;
    popup_add_text5(mblk, cont_id, &(ci[id]), ID_ITERMS2,  func,
                     "Min sin/cos terms", "%d", par->iterms2);
    return id;
}

int param_add_baseline3(MBLOCK *mblk, int cont_id, G_POPUP_CHILDINFO ci[], int id,
                       void (*func)(G_POPUP_CHILDINFO *ci),
                       PBLOCK *par)
{
    popup_add_text5(mblk, cont_id, &(ci[id]), ID_IBSTRT, func,
                     "Start baseline   ", "%d", par->ibstrt);

    id++;
    popup_add_text5(mblk, cont_id, &(ci[id]), ID_IBSTOP, func,
                     "Stop baseline    ", "%d", par->ibstop);
    id++;
    popup_add_text5(mblk, cont_id, &(ci[id]), ID_IBWATER, func,
                     "Waterline        ", "%d", par->ibwater);
    id++;
    popup_add_text5(mblk, cont_id, &(ci[id]), ID_IBWIDTH, func,
                     "Waterline Width  ", "%d", par->ibwidth);
    return id;
}
        
int param_add_waterfit(MBLOCK *mblk, int cont_id, G_POPUP_CHILDINFO ci[], int id,
                       void (*func)(G_POPUP_CHILDINFO *ci),
                       PBLOCK *par)
{
    static char *fit_items[] = {"Off" , "On" };

    id = param_add_label(cont_id, ci, id, par->icrdir, "WATERFIT")+1;
    popup_add_option(cont_id, &(ci[id]), ID_WATERFIT, 
                     "Waterfit         ", par->waterfit, 2, fit_items);
    id++;
    popup_add_text5(mblk, cont_id, &(ci[id]), ID_WATERPOS, func,
                     "Peak position    ", "%d", par->waterpos);
    id++;
    popup_add_text5(mblk, cont_id, &(ci[id]), ID_WATERWID, func,
                     "Peak width       ", "%d", par->waterwid);
    return id;
}


int param_add_outfile(int cont_id, G_POPUP_CHILDINFO ci[], int id,
                    PBLOCK *par)
{
    char label[40];
    id = param_add_label(cont_id, ci, id, par->icrdir, "OUTFILE")+1;
#ifdef NEEHE
        {
            static char *items[] = {"New File" , 
                "Output = Input", "Old File"};
            int select = par->ifilut;
            /*
             * always new
             */
            popup_add_option(cont_id, &(ci[id]), ID_IFILUT, "Output File",
                             select, 1, items);
        }

        id++;

#endif
    popup_add_text4(cont_id, &(ci[id]), ID_ISSTRT,
                     "Store Start      ", "%d", par->isstrt);

    id++;
    popup_add_text4(cont_id, &(ci[id]), ID_ISSTOP, 
                     "Store Stop       ", "%d", par->isstop);

    return id;
}
 

void param_process_input(MBLOCK *mblk, int id, int dim, int numdim, 
                                G_POPUP_CHILDINFO ci[], 
                                CBLOCK *cpar, PBLOCK *par0[],  DBLOCK *dat)       
{
    int i,dim_size;
    PBLOCK *par;
    int   geti;
    float getf;

    dim_size = id/numdim;
    for (i=0;i<id;i++) {
        if (i/dim_size >= numdim)
            break;
        par = par0[mblk->info->block_id] + i/dim_size + dim;
        
        if (ci[i].type != G_CHILD_SEPARATOR &&
                ci[i].type  != G_CHILD_PANEL &&
                ci[i].type  != G_CHILD_LABEL) {
                switch (ci[i].id) {

                    case ID_TRUNC_LEVEL:
                        dat->finfo[OUTFILE].tresh_flag = ci[i].item;
                        break;
                    case ID_TRUNC_LEVEL1:
                        dat->finfo[OUTFILE].tresh_levels[0] = psnd_scan_float(ci[i].label);
                        break;
                    case ID_TRUNC_OFFSET:
                        dat->finfo[OUTFILE].tresh_levels[1] = psnd_scan_float(ci[i].label);
                        break;
                    case ID_TRUNC_LEVEL2:
                        dat->finfo[OUTFILE].tresh_levels[2] = psnd_scan_float(ci[i].label);
                        break;
                    case ID_TRUNC_FLOAT:
                        dat->finfo[OUTFILE].sizeof_float = ci[i].item+2;
                        break;
                    case ID_NEXTDIR:
                        dat->nextdr = ci[i].item;
                        break;
                    case ID_AQDIR:
                        dat->iaqdir = ci[i].item;
                        break;
                    case ID_IPOST:
                        par->ipost = post2user_modes[ci[i].item];
                        break;
                    case ID_IREVER:
                        par->irever = ci[i].item;
                        break;
                    case ID_SFD:
                        par->sfd = psnd_scan_float(ci[i].label);
                        break;
                    case ID_SWHOLD:
                        par->swhold = psnd_scan_float(ci[i].label);
                        if (mblk->info->dimension_id == dim)
                            dat->sw = par->swhold;
                        break;
                    case ID_TD:
                        geti = psnd_scan_integer(ci[i].label);
                        if (geti > 0)
                            par->td = geti;
                        break;
                    case ID_XREF:
                        getf = psnd_scan_float(ci[i].label);
                        psnd_set_calibration(getf, par, dat);
                        break;
                    case ID_AREF:
                        getf = psnd_scan_float(ci[i].label);
                        psnd_set_calibration_aref(getf, par, dat);
                        break;
                    case ID_BREF:
                        getf = psnd_scan_float(ci[i].label);
                        psnd_set_calibration_bref(getf, par, dat);
                        break;
                    case ID_RECTYPE:
                        par->isspec = ci[i].item;
                        /*
                        if (dat->par0[0] == par)
                            dat->isspec = ci[i].item;
                            */
                        break;
                    case ID_RECTYPE_CURRENT:
                        dat->isspec = ci[i].item;
                        break;
                    case ID_DSPSHIFT:
                        par->dspshift = psnd_scan_float(ci[i].label);
                        par->dspshift = max(0,par->dspshift);
                        break;
                    case ID_DSPFLAG:
                        par->dspflag = ci[i].item;
                        break;
                    case ID_IPRE:
                        par->ipre = ci[i].item+1;
                        break;
                    case ID_IRSTRT:
                        {
                        int stop;
                        int start = psnd_scan_integer(ci[i].label);
                        stop = max(start, par->irstop);
                        psnd_set_record_read_range(mblk, start, stop, par);
                        }
                        break;
                    case ID_IRSTOP:
                        {
                        int start;
                        int stop = psnd_scan_integer(ci[i].label);
                        start = par->irstrt;
                        psnd_set_record_read_range(mblk, start, stop, par);
                        }
                        break;
                    case ID_NFFT:
                        par->nfft = ci[i].item;
                        break;
                    case ID_IFFT:
                        par->ifft = ft2user_modes[ci[i].item];
                        break;
                    case ID_NZF:
                        par->nzf = ci[i].item;
                        break;
                    case ID_FTSCALE:
                        par->ftscale = psnd_scan_float(ci[i].label);
                        break;
                    case ID_WATWA:
                        par->watwa = ci[i].item;
                        break;
                    case ID_IOPT:
                        par->iopt = ci[i].item+1;
                        break;
                    case ID_KC:
                        par->kc = psnd_scan_float(ci[i].label);
                        break;
                    case ID_WSHIFT:
                        par->wshift = psnd_scan_float(ci[i].label);
                        break;
                    case ID_HILBERT:
                        par->hilbert = ci[i].item;
                        break;
                    case ID_PHASE:
                        if (ci[i].item == 0 && par->iphase < 0)
                            break;
                        par->iphase = ci[i].item;
                        break;
                    case ID_A:
                        par->ahold = psnd_scan_float(ci[i].label);
                        break;
                    case ID_B:
                        par->bhold = psnd_scan_float(ci[i].label);
                        break;
                    case ID_I0:
                        par->ihold = psnd_scan_integer(ci[i].label);
                        break;
                    case ID_NWINDO:
                        par->nwindo = ci[i].item;
                        break;
                    case ID_IWINDO:
                        par->iwindo = ci[i].item+1;
                        break;
                    case ID_RSH:
                        par->rsh = psnd_scan_float(ci[i].label);
                        break;
                    case ID_RLB:
                        par->rlb = psnd_scan_float(ci[i].label);
                        break;
                    case ID_GN:
                        par->gn = psnd_scan_float(ci[i].label);
                        break;
                    case ID_ITM:
                        par->itm = psnd_scan_integer(ci[i].label);
                        break;
                    case ID_IWSTRT:
                        par->iwstrt = psnd_scan_integer(ci[i].label);
                        break;
                    case ID_IWSTOP:
                        par->iwstop = psnd_scan_integer(ci[i].label);
                        break;
                    case ID_NBASE:
                        par->nbase = ci[i].item;
                        break;
                    case ID_IBASE_WND:
                        if (ci[i].item && par->ibase)
                            par->ibase++;
                        break;
                    case ID_IBASE:
                        par->ibase = baseline_ids[ci[i].item];
                        break;
                    case ID_ITERMS:
                        par->iterms = psnd_scan_integer(ci[i].label);
                        break;
                    case ID_ITERMS2:
                        par->iterms2 = psnd_scan_integer(ci[i].label);
                        break;
                    case ID_IBSTRT:
                        par->ibstrt = psnd_scan_integer(ci[i].label);
                        break;
                    case ID_IBSTOP:
                        par->ibstop = psnd_scan_integer(ci[i].label);
                        break;
                    case ID_IBWATER:
                        par->ibwater = psnd_scan_integer(ci[i].label);
                        break;
                    case ID_IBWIDTH:
                        par->ibwidth = psnd_scan_integer(ci[i].label);
                        break;
                    case ID_WATERFIT:
                        par->waterfit = ci[i].item;
                        break;
                    case ID_WATERPOS:
                        par->waterpos = psnd_scan_integer(ci[i].label);
                        break;
                    case ID_WATERWID:
                        par->waterwid = psnd_scan_integer(ci[i].label);
                        break;
/*
                    case ID_IFILUT:
                        par->ifilut = ci[i].item;
                        break;
*/
                    case ID_ISSTRT:
                        par->isstrt = psnd_scan_integer(ci[i].label);
                        break;
                    case ID_ISSTOP:
                        par->isstop = psnd_scan_integer(ci[i].label);
                        break;

                    case ID_NLPC:
                        par->nlpc = ci[i].item;
                        break;
                    case ID_LPC:
                        par->lpc = ci[i].item;
                        break;
                    case ID_LPCMODE:
                        par->lpcmode = ci[i].item;
                        break;
                    case ID_NFUT:
                        par->nfut = psnd_scan_integer(ci[i].label);
                        break;
                    case ID_NPOLES:
                        par->npoles = psnd_scan_integer(ci[i].label);
                        break;
                    case ID_MROOT:
                        par->mroot = ci[i].item;
                        break;
                    case ID_REPLACE:
                        par->replace = ci[i].item;
                        break;
                    case ID_TOLER:
                        par->toler = psnd_scan_float(ci[i].label);
                        break;
                    case ID_NGAP1:
                        par->ngap1 = psnd_scan_integer(ci[i].label);
                        break;
                    case ID_NGAP2:
                        par->ngap2 = psnd_scan_integer(ci[i].label);
                        break;
                    case ID_NSTART:
                        par->nstart = psnd_scan_integer(ci[i].label);
                        break;
                    case ID_NSTOP:
                        par->nstop = psnd_scan_integer(ci[i].label);
                        break;

                    case ID_ILOWA:
                        geti = psnd_scan_integer(ci[i].label);
                        geti = max(geti, cpar->ihamin);
                        cpar->ilowa = min(geti, cpar->ihamax);
                        /* mark change */
                        cpar->mode   = CONTOUR_PLOT;
                        break;
                    case ID_IHIGHA:
                        geti = psnd_scan_integer(ci[i].label);
                        geti = max(geti, cpar->ihamin);
                        cpar->ihigha = min(geti, cpar->ihamax);
                        cpar->mode   = CONTOUR_PLOT;
                        break;
                    case ID_ILOWB:
                        geti = psnd_scan_integer(ci[i].label);
                        geti = max(geti, cpar->ihbmin);
                        cpar->ilowb = min(geti, cpar->ihbmax);
                        cpar->mode   = CONTOUR_PLOT;
                        break;
                    case ID_IHIGHB:
                        geti = psnd_scan_integer(ci[i].label);
                        geti = max(geti, cpar->ihbmin);
                        cpar->ihighb = min(geti, cpar->ihbmax);
                        cpar->mode   = CONTOUR_PLOT;
                        break;
                    case ID_IADIV:
                        geti = psnd_scan_integer(ci[i].label);
                        geti = max(geti, 1);
                        cpar->iadiv = min(geti, cpar->ihamax/2);
                        break;
                    case ID_IBDIV:
                        geti = psnd_scan_integer(ci[i].label);
                        geti = max(geti, 1);
                        cpar->ibdiv = min(geti, cpar->ihbmax/2);
                        break;
                    case ID_MLEVEL:
                        cpar->mlevel = ci[i].item;
                        break;
                    case ID_PLUSMIN:
                        cpar->plusmin = ci[i].item;
                        break;
                    case ID_NLEVEL:
                        cpar->nlevel = ci[i].item;
                        break;
                    case ID_CLEVEL:
                        cpar->clevel[0] = psnd_scan_float(ci[i].label);
                        break;
                    case ID_FLEVEL:
                        cpar->flevel = psnd_scan_float(ci[i].label);
                        break;
                    case ID_DLEVEL:
                        cpar->dlevel = psnd_scan_float(ci[i].label);
                        break;
                    case ID_LEVELMAX:
                        cpar->levelmax = psnd_scan_float(ci[i].label);
                        break;
                    case ID_LEVCOL:
                        cpar->colormode = ci[i].item;
                        break;
                    case ID_INTMOD:
                        cpar->intmod = ci[i].item;
                        break;
                    case ID_INTLEVEL:
                        cpar->intlevel = ci[i].item;
                        break;
                    case ID_SCALE_I:
                        getf = psnd_scan_float(ci[i].label);
                        if (getf < -1.0e-10 || getf > 1.0e-10)
                            cpar->scale_i = getf;
                        break;
                    case ID_REV:
                        getf = psnd_scan_float(ci[i].label);
                        cpar->ref = fabs(getf);
                        cpar->rref = cpar->vref * pow(cpar->ref,6.0);
                        break;
                    case ID_VREV: 
                        getf = psnd_scan_float(ci[i].label);
                        cpar->vref = fabs(getf);
                        cpar->rref = cpar->vref * pow(cpar->ref,6.0);
                        break;
                    case ID_ZOINT:
                        cpar->zoint = ci[i].item;
                        break;
                    case ID_NSHIFT:
                        geti = psnd_scan_integer(ci[i].label);
                        cpar->nshift = geti;
                        break;
                    case ID_PINPOI:
                        cpar->pinpoi = ci[i].item;
                        break;
                    case ID_PRIMAX: 
                        cpar->primax = ci[i].item;
                        break;
                    case ID_PRIBOX: 
                        cpar->pribox = ci[i].item;
                        break;
                    case 0:
                        break;
                    default:
                        printf("ERROR %d: id = %d: %s\n",i,ci[i].id,ci[i].label);
                        break;
                }
            }
       }
}

#define MAXCI	(110*MAX_DIM)
int psnd_edit_all_param(MBLOCK *mblk)
{
    int i, id, dir, ok, cont_id;
    G_POPUP_CHILDINFO ci[MAXCI];
    char *label;
    PBLOCK *par;
    SBLOCK *spar = mblk->spar;
    CBLOCK *cpar = mblk->cpar_screen;
    DBLOCK *dat  = DAT;

    if (dat->ityp==0)
        return 0;
    for (i=0;i<MAXCI;i++)
	g_popup_init_info(&(ci[i]));

    dir = 1;
    id = 0;
#ifndef __sgi
    cont_id = g_popup_container_open(mblk->info->win_id, "Edit Parameters",
                                     G_POPUP_WAIT | G_POPUP_TAB);
#else
    cont_id = g_popup_container_open(mblk->info->win_id, "Edit Parameters",
                                     G_POPUP_WAIT | G_POPUP_SCROLL);
    id = popup_add_panel(cont_id, ci, id, TRUE, TRUE, FALSE)+1;
#endif
    for (i=0;i<dat->ityp;i++) {
        par = mblk->par[mblk->info->block_id]+i;
#ifndef __sgi
        id++;
	g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_TAB;
        label = psnd_sprintf_temp("Dir %d", i+1);
	ci[id].label = label;
	g_popup_add_child(cont_id, &(ci[id]));
#else
        id = popup_add_panel(cont_id, ci, id, FALSE, TRUE, TRUE)+1;
#endif
        id = param_add_freq(cont_id, ci, id, par)+1;
        id = param_add_pre(cont_id, ci, id, par)+1;
        id = param_add_dsp(cont_id, ci, id, par)+1;
#ifndef __sgi
      id++;
	g_popup_init_info(&(ci[id]));
	ci[id].type  = G_CHILD_TAB;
	ci[id].label = "Predict";
	g_popup_add_child(cont_id, &(ci[id]));
#endif
        id = param_add_linpar(mblk ,cont_id, ci, id, NULL, par)+1;
#ifndef __sgi
      id++;
	g_popup_init_info(&(ci[id]));
	ci[id].type  = G_CHILD_TAB;
	ci[id].label = "Window";
	g_popup_add_child(cont_id, &(ci[id]));
#endif
        id = param_add_window1(mblk, cont_id, ci, id, NULL, par)+1;
        id = param_add_window2(mblk,cont_id, ci, id, NULL, par)+1;
        id = param_add_window3(mblk, cont_id, ci, id, NULL, par)+1;
#ifndef __sgi
      id++;
	g_popup_init_info(&(ci[id]));
	ci[id].type  = G_CHILD_TAB;
	ci[id].label = "Watwa/Ft";
	g_popup_add_child(cont_id, &(ci[id]));
#endif
        id = param_add_watwa(mblk, cont_id, ci, id, NULL, par)+1;
        id = param_add_fourier1(mblk,cont_id, ci, id, NULL, par)+1;
        id = param_add_fourier2(mblk,cont_id, ci, id, NULL, par)+1;
#ifndef __sgi
      id++;
	g_popup_init_info(&(ci[id]));
	ci[id].type  = G_CHILD_TAB;
	ci[id].label = "Phase/Rev";
	g_popup_add_child(cont_id, &(ci[id]));
#endif
        id = param_add_hilbert(cont_id, ci, id, par)+1;
        id = param_add_phase(cont_id, ci, id, par)+1;
        id = param_add_reverse(cont_id, ci, id, par)+1;
#ifndef __sgi
      id++;
	g_popup_init_info(&(ci[id]));
	ci[id].type  = G_CHILD_TAB;
	ci[id].label = "Baseline";
	g_popup_add_child(cont_id, &(ci[id]));
#endif
        id = param_add_baseline1(mblk,cont_id, ci, id, NULL, par)+1;
        id = param_add_baseline2(mblk,cont_id, ci, id, NULL, par)+1;
        id = param_add_baseline3(mblk,cont_id, ci, id, NULL, par)+1;
#ifndef __sgi
      id++;
	g_popup_init_info(&(ci[id]));
	ci[id].type  = G_CHILD_TAB;
	ci[id].label = "Post";
	g_popup_add_child(cont_id, &(ci[id]));
#endif
        id = param_add_waterfit(mblk,cont_id, ci, id, NULL, par)+1;
        id = param_add_post(cont_id, ci, id, par)+1;
        id = param_add_outfile(cont_id, ci, id, par)+1;
        id = popup_add_panel(cont_id, ci, id, FALSE, FALSE, FALSE)+1;
        dir++;
    }
    assert(id < MAXCI);
    ok = g_popup_container_show(cont_id);
    if (ok)
        param_process_input(mblk, id, 0, dat->ityp, ci, cpar, mblk->par, dat);
    return ok;

}

int psnd_set_param(MBLOCK *mblk, int argc, char *argv[], int param_id)
{
    int i, cont_id;
    G_POPUP_CHILDINFO ci[40];
    char *label, *label2;
    int   id=0,ok;
    int   geti;
    float getf;
    int nsize;          
    PBLOCK *par  = PAR;
    CBLOCK *cpar = mblk->cpar_screen;
    DBLOCK *dat  = DAT;

    nsize = dat->isize;
    label = psnd_sprintf_temp("Parameters dir %d", par->icrdir);
    cont_id = g_popup_container_open(mblk->info->win_id, label, G_POPUP_WAIT);
 
    /* --- prepare popup box --- */
    switch (param_id) {

    case PSND_SI:
        if (argc >= 2) {
            nsize = psnd_scan_integer(argv[1]);
        }
        else {
            label = psnd_sprintf_temp(" ? SI ( max %d)", mblk->info->block_size);
            if (psnd_ivalin(mblk, label, 1 ,&(nsize)) == 0)
                return 0;
        }
        return psnd_set_datasize(mblk,nsize, FALSE, dat);

    case PSND_TD:
        nsize = par->nsiz;
        if (argc >= 2) {
            nsize = psnd_scan_integer(argv[1]);
        }
        else {
            label = psnd_sprintf_temp(" ? TD");
            if (psnd_ivalin(mblk, label, 1 ,&(nsize)) == 0)
                return 0;
        }
        if (nsize <= 0  || nsize > mblk->info->block_size) {
            psnd_printf(mblk," TD %d out of range\n", nsize);
            return FALSE;
        }
        par->nsiz = nsize;
        return TRUE;

    case PSND_ND: 
        if (argc >= 2) {
            dat->nextdr = psnd_scan_integer(argv[1]);
            return TRUE;
        }
        else {
            int min_max_dec_step[] = { 0, 0, 0, 1};

            min_max_dec_step[1] = dat->ityp;
            popup_add_spin(cont_id, &(ci[id]), ID_NEXTDIR,
                       "Process in direction ", 
                       dat->nextdr, min_max_dec_step);
        }
        break;

    case PSND_TRUNC_LEVEL: 
        {
           static char *yesnolabels[] = { "No", "Yes", " " };
	    static char *radio[] = {"Truncated (Float 2)",
                             "Truncated (Float 3)",
                             "Normal    (Float 4)"};
            id=0;
            g_popup_init_info(&(ci[id]));
            ci[id].type  = G_CHILD_LABEL;
            ci[id].id    = id;
            ci[id].label = "COMPRESSED OUTPUT FILE OPTIONS";
            g_popup_add_child(cont_id, &(ci[id]));  

            id++;
            g_popup_init_info(&(ci[id]));
            ci[id].type = G_CHILD_SEPARATOR;
            g_popup_add_child(cont_id, &(ci[id]));

            id++;
   	    g_popup_init_info(&(ci[id]));
	    ci[id].type       = G_CHILD_SPINBOXTEXT;
	    ci[id].id         = ID_TRUNC_FLOAT;
	    ci[id].frame      = TRUE;
	    ci[id].item       = dat->finfo[OUTFILE].sizeof_float - 2;
	    ci[id].item_count = 3;
	    ci[id].label      = "Type of float";
	    ci[id].data       = radio;
	    g_popup_add_child(cont_id, &(ci[id]));

            id++;
            popup_add_option(cont_id, &(ci[id]), ID_TRUNC_LEVEL, 
                         "Use truncated levels", 
                         dat->finfo[OUTFILE].tresh_flag, 2,
                         yesnolabels);


            id++;
            label = psnd_sprintf_temp("%g", dat->finfo[OUTFILE].tresh_levels[0]);
            popup_add_text2(cont_id, &(ci[id]), ID_TRUNC_LEVEL1, label, 
                            "Positive truncation level");

            id++;
            label = psnd_sprintf_temp("%g", dat->finfo[OUTFILE].tresh_levels[1]);
            popup_add_text2(cont_id, &(ci[id]), ID_TRUNC_OFFSET, label, 
                            "Offset level");

            id++;
            label = psnd_sprintf_temp("%g", dat->finfo[OUTFILE].tresh_levels[2]);
            popup_add_text2(cont_id, &(ci[id]), ID_TRUNC_LEVEL2, label, 
                            "Negative truncation level");

        }
        break;
        
    case PSND_AD: 
        if (argc >= 2) {
            dat->iaqdir = psnd_scan_integer(argv[1]);
            return TRUE;
        }
        else {
            int min_max_dec_step[] = { 0, 0, 0, 1};

            min_max_dec_step[1] = dat->ityp;
            popup_add_spin(cont_id, &(ci[id]), ID_AQDIR,
                       "Acquisition direction ", 
                       dat->iaqdir, min_max_dec_step);
        }
        break;
        

    case PSND_SW:
        if (argc >= 2) {
            par->swhold = psnd_scan_float(argv[1]);
            dat->sw = par->swhold;
            return TRUE;
        }
        if (psnd_rvalin2(mblk, " Spectral Width ?",&(par->swhold),
                            FLAG_POSITIVE)) {
            dat->sw = par->swhold;
            return TRUE;
        }
        else
            return FALSE;

    case PSND_SF:
        if (argc >= 2) {
            par->sfd = psnd_scan_float(argv[1]);
            return TRUE;
        }
        return psnd_rvalin2(mblk, " Spectral Frequency ?",&(par->sfd),
            FLAG_POSITIVE);

    case PSND_XR:
        if (argc >= 2) {
            getf = psnd_scan_float(argv[1]);
            psnd_set_calibration(getf, par, dat);
            return TRUE;
        }
        getf = par->xref;
        if (psnd_rvalin(mblk, " Reference ?", 1, &getf)==0)
            return FALSE;
        psnd_set_calibration(getf, par, dat);
        return TRUE;

    case PSND_AREF:
        if (argc >= 2) {
            par->aref = psnd_scan_float(argv[1]);
            return TRUE;
        }
        return psnd_rvalin(mblk, "A Reference ?", 1, &(par->aref));

    case PSND_BREF:
        if (argc >= 2) {
            par->bref = psnd_scan_float(argv[1]);
            return TRUE;
        }
        return psnd_rvalin(mblk, "B Reference ?", 1, &(par->bref));

    case PSND_SH:
        if (argc >= 2) {
            par->rsh = psnd_scan_float(argv[1]);
            return TRUE;
        }
        return psnd_rvalin(mblk, " Shift ?", 1, &(par->rsh));

    case PSND_LB:
        if (argc >= 2) {
            par->rlb = psnd_scan_float(argv[1]);
            return TRUE;
        }
        return psnd_rvalin(mblk, " Line broadening ?", 1, &(par->rlb));

    case PSND_GB:
        if (argc >= 2) {
            par->gn = psnd_scan_float(argv[1]);
            return TRUE;
        }
        return psnd_rvalin2(mblk, " Gaussian broadening (0-1) ?", 
            &(par->gn), FLAG_NOTZERO);

    case PSND_IT:
        if (argc >= 2) {
            par->itm = psnd_scan_integer(argv[1]);
            par->itm = max(1,par->itm);
            return TRUE;
        }
        return psnd_ivalin2(mblk, " #Channels TM window ?", 
            &(par->itm), FLAG_ATLEAST_1);

    case PSND_ZF:
        if (argc >= 2) {
            par->nzf = psnd_scan_integer(argv[1]);
            return TRUE;
        }
        return psnd_ivalin(mblk, " Zero Fill ?", 1,  &(par->nzf));

    case PSND_PA:
        if (argc >= 2) {
            par->ahold = psnd_scan_float(argv[1]);
            return TRUE;
        }
        return psnd_rvalin(mblk, " 0 Order phase ?", 1, &(par->ahold));

    case PSND_PB:
        if (argc >= 2) {
            par->bhold = psnd_scan_float(argv[1]);
            return TRUE;
        }
        return psnd_rvalin(mblk, " 1st Order phase ?", 1, &(par->bhold));

    case PSND_I0:
        if (argc >= 2) {
            par->ihold = psnd_scan_integer(argv[1]);
            return TRUE;
        }
        return psnd_ivalin(mblk, " I0 phase point ?", 1, &(par->ihold));
/*
    case PSND_BP:
        return psnd_rvalin(mblk, " fit parameters ?", 4, dat->param);
*/
        return 0;

        
    case PSND_PARAM_FREQ:
        id=0;
        id = param_add_freq(cont_id, ci, id, par);
        id = param_add_freq2(cont_id, ci, id, dat);
        break;

    case PSND_RM:
        id = 0;
        id = param_add_pre(cont_id, ci, id, par);
        break;
    
    case PSND_DSPSHIFT:
        id = 0;
        id = param_add_dsp(cont_id, ci, id, par);
        break;
    
    case PSND_PARAM_WATWA:
        id=0;
        id = param_add_watwa(mblk, cont_id, ci, id, NULL, par);
        break;

    case PSND_PARAM_LINPAR:
        id=0;
        id = param_add_linpar(mblk, cont_id, ci, id, NULL, par);
        break;
/*
    case PSND_DO_WINDOW:
        id = 0;
        id = param_add_window1(mblk, cont_id, ci, id, showwincallback, par);
        id++;
        id = param_add_window2(mblk,cont_id, ci, id, showwincallback, par);
        id++;
        id = param_add_window3(mblk, cont_id, ci, id, showwincallback, par);
        break;
  */  
    case PSND_WM:
    case PSND_PARAM_WINDOW:
        id = 0;
        id = param_add_window1(mblk, cont_id, ci, id, NULL,par);
        if (param_id == PSND_PARAM_WINDOW) {
            id++;
            id = param_add_window2(mblk,cont_id, ci, id, NULL, par);
        }
        id++;
        id = param_add_window3(mblk, cont_id, ci, id, NULL, par);
        break;

    case PSND_FTSCALE:
        if (argc >= 2) {
            par->ftscale = psnd_scan_float(argv[1]);
            return TRUE;
        }
        /* fall through */
    case PSND_FZ:
    case PSND_FT:
    case PSND_FM:
    case PSND_PARAM_FOURIER:
        id = 0;
        if (param_id != PSND_FZ) {
            id = param_add_fourier1(mblk,cont_id, ci, id, NULL, par);
            id++;
        }
        id = param_add_fourier2(mblk,cont_id, ci, id, NULL, par);
        break;

    case PSND_PARAM_HILBERT:
        id=0;
        id = param_add_hilbert(cont_id, ci, id, par);
        break;

    case PSND_PARAM_PHASE:
        id = 0;
        id = param_add_phase(cont_id, ci, id, par);
        break;

    case PSND_XM:
        id = 0;
        id = param_add_reverse(cont_id, ci, id, par);
        break;

    case PSND_BT:
    case PSND_BM:
        id = 0;
        if (param_id == PSND_BM) {
            id = param_add_baseline1(mblk,cont_id, ci, id, NULL, par);
            id++;
        }
        id = param_add_baseline2(mblk,cont_id, ci, id, NULL, par);
        if (param_id == PSND_BT) 
            break;
        id++;
        id = param_add_baseline3(mblk,cont_id, ci, id, NULL, par);
        break;
        
    case PSND_PARAM_WATERFIT:
        id = 0;
        id = param_add_waterfit(mblk,cont_id, ci, id, NULL, par);
        break;

    case PSND_PARAM_POST:
    case PSND_SM:
    case PSND_PARAM_OUTFILE:
        id = 0;
        id = param_add_post(cont_id, ci, id, par);
        id++;
        id = param_add_outfile(cont_id, ci, id, par);
        break;
        
    case PSND_CI: {
        static char *yesnolabels[] = { "No", "Yes", " " };
        static char *plusminuslabels[] = { "Positive", "Negative" };
        static char *items[] = {
                "Box" , 
                "Flood-fill",
                "Convex flood-fill",
                "Cross-integration", 
                "Cross-integr & error estimate" };
        id = 0;
        popup_add_option(cont_id, &(ci[id]), ID_INTMOD, 
                "Integration Mode", cpar->intmod, 3, items);
        id++;
        popup_add_option(cont_id, &(ci[id]), ID_INTLEVEL, 
                "Levels for peak-picking", cpar->intlevel, 2, plusminuslabels);
/*
        id++;
        popup_add_panel(cont_id, &(ci[id]), 0, TRUE, TRUE, FALSE);
*/

        id++;
        label = psnd_sprintf_temp("%g", cpar->ref);
        popup_add_text2(cont_id, &(ci[id]), ID_REV, label, 
                        "Reference distance");

        id++;
        label = psnd_sprintf_temp("%g", cpar->vref);
        popup_add_text2(cont_id, &(ci[id]), ID_VREV, label, 
                        "Reference peak intensity");

/*
        id++;
        popup_add_panel(cont_id, &(ci[id]), 0, TRUE, FALSE, FALSE);
        */

        id++;
        popup_add_option(cont_id, &(ci[id]), ID_ZOINT, 
                         "Use box for offset correction", cpar->zoint, 2,
                         yesnolabels);

        id++;
        label = psnd_sprintf_temp("%g", cpar->scale_i);
        popup_add_text2(cont_id, &(ci[id]), ID_SCALE_I, label, 
                        "Scale factor (Divide by this number)");
/*
        id++;
        popup_add_option(cont_id, &(ci[id]), ID_PRIBOX, 
                         "Print box coordinates", cpar->pribox , 2,
                         yesnolabels);
        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type = G_CHILD_SEPARATOR;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_LABEL;
        ci[id].id    = id;
        ci[id].label = "For cross integration :";
        g_popup_add_child(cont_id, &(ci[id]));  

        id++;
        label = psnd_sprintf_temp("%d", cpar->nshift);
        popup_add_text2(cont_id, &(ci[id]), ID_NSHIFT, label, 
                        "Max channel origin search");

        id++;
        popup_add_option(cont_id, &(ci[id]), ID_PINPOI, 
                         "User pinpoint peak maximum", cpar->pinpoi , 2,
                         yesnolabels);

        id++;
        popup_add_option(cont_id, &(ci[id]), ID_PRIMAX, 
                         "Print peak maximum        ", cpar->primax , 2,
                         yesnolabels);
*/
        }
        break;

    case PSND_CM:
        id = -1;

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type       = G_CHILD_PANEL;
        ci[id].item       = TRUE;
        ci[id].frame      = TRUE;
        ci[id].title      = "X axis";
        ci[id].horizontal = TRUE;
        g_popup_add_child(cont_id, &(ci[id]));

        label = psnd_sprintf_temp("%d", cpar->ilowa);
        label2 = psnd_sprintf_temp("Left >= %d", cpar->ihamin);
        popup_add_text2(cont_id, &(ci[id]), ID_ILOWA, label, label2);

        id++;
        label = psnd_sprintf_temp("%d", cpar->ihigha);
        label2 = psnd_sprintf_temp("Right <= %d", cpar->ihamax);
        popup_add_text2(cont_id, &(ci[id]), ID_IHIGHA, label, label2);

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_PANEL;
        ci[id].item  = FALSE;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type       = G_CHILD_PANEL;
        ci[id].item       = TRUE;
        ci[id].frame      = TRUE;
        ci[id].title      = "Y axis";
        ci[id].horizontal = TRUE;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        label = psnd_sprintf_temp("%d", cpar->ilowb);
        label2 = psnd_sprintf_temp("Bottom >= %d", cpar->ihbmin);
        popup_add_text2(cont_id, &(ci[id]), ID_ILOWB, label, label2);

        id++;
        label = psnd_sprintf_temp("%d", cpar->ihighb);
        label2 = psnd_sprintf_temp("Top <= %d", cpar->ihbmax);
        popup_add_text2(cont_id, &(ci[id]), ID_IHIGHB, label, label2);

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_PANEL;
        ci[id].item  = FALSE;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type       = G_CHILD_PANEL;
        ci[id].item       = TRUE;
        ci[id].frame      = TRUE;
        ci[id].title      = "Step size";
        ci[id].horizontal = TRUE;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        label = psnd_sprintf_temp("%d", cpar->iadiv);
        popup_add_text2(cont_id, &(ci[id]), ID_IADIV, label, "X increment");

        id++;
        label = psnd_sprintf_temp("%d", cpar->ibdiv);
        popup_add_text2(cont_id, &(ci[id]), ID_IBDIV, label, "Y increment");

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_PANEL;
        ci[id].item  = FALSE;
        g_popup_add_child(cont_id, &(ci[id]));

        {
            static char *items[] = {"Explicit", "Use factor" , 
                             "Equidistant", "Log-range"};
            int select = cpar->mlevel;
            id++;
            popup_add_option(cont_id, &(ci[id]), ID_MLEVEL, "Level mode  ",
                             select, 4, items);
        }
        {
            static char *items[] = {"All levels same color", 
                             "Pos/Neg levels" , 
                             "Automatic" };
            int select = cpar->colormode;
            id++;
            popup_add_option(cont_id, &(ci[id]), ID_LEVCOL, "Level colors",
                             select, 3, items);
        }
        {
            static char *items[] = {"Positive", "Positive and negative" , 
                             "Negative"};
            int select = cpar->plusmin;
            id++;
            popup_add_option(cont_id, &(ci[id]), ID_PLUSMIN,"Show levels ",
                             select, 3, items);
        }
        {
            static int min_max_dec_step[4] = { 1, 0, 0, 1 };
            int select = cpar->nlevel;
            min_max_dec_step[1] = cpar->maxlev;
            id++;
            popup_add_spin(cont_id, &(ci[id]), ID_NLEVEL,
                       "Number of levels", select, min_max_dec_step);
        }
        id++;
        label = psnd_sprintf_temp("%g", cpar->clevel[0]);
        popup_add_text2(cont_id, &(ci[id]), ID_CLEVEL, label, 
            "Lowest Level");
        id++;
        label = psnd_sprintf_temp("%1.2f", cpar->flevel);
        popup_add_text2(cont_id, &(ci[id]), ID_FLEVEL, label, 
            "Factor for \'Use factor\' mode");
        id++;
        label = psnd_sprintf_temp("%g", cpar->dlevel);
        popup_add_text2(cont_id, &(ci[id]), ID_DLEVEL, label, 
                           "Distance for \'Equidistant\' mode");
        id++;
        label = psnd_sprintf_temp("%g", cpar->levelmax);
        popup_add_text2(cont_id, &(ci[id]), ID_LEVELMAX, label, 
            "Highest level for \'Log-range\' mode");

        break;
#ifdef gggggg
    case PSND_CC:
        id=0;
        {
            static char *items[] = {"All levels same color", 
                             "Pos/Neg levels" , 
/*                             "Define each level",*/
                             "Automatic" };
            int select = cpar->colormode;
            popup_add_option(cont_id, &(ci[id]), ID_LEVCOL, "Level colors",
                             select, 4-1, items);
        }
        break;
#endif
    }
    ok = g_popup_container_show(cont_id);

    /* if ok, get input */
    if (ok) {
       id++;
       param_process_input(mblk,id, mblk->info->dimension_id, 1, ci, cpar, mblk->par, dat);
       /* process results */
       switch (param_id) {
       case PSND_CM:
          psnd_process_contour_levels(mblk,cpar);
          psnd_process_color_levels(mblk, cpar);
          break;
#ifdef nooo
       case PSND_CC:
          switch (cpar->colormode) {
             case 0 : 
                 psnd_edit_contour_colors(win_id, cpar->colormode, cpar);
                 break;
             case 1 : 
                 cpar->levcol[0] = spar[S_CONTOUR].color;
                 cpar->levcol[1] = spar[S_CONTOUR].color2;
                 psnd_edit_contour_colors(win_id, cpar->colormode, cpar);
                 spar[S_CONTOUR].color  = cpar->levcol[0];
                 spar[S_CONTOUR].color2 = cpar->levcol[1];
                 break;
             case 2 :
                 /*
                 psnd_edit_contour_colors(win_id, cpar->colormode, cpar);
                 */
                 break;
             case 3:
                 break;
          }
          psnd_process_color_levels(mblk, cpar);
          break;
#endif
       }
    }
    return ok;
}


