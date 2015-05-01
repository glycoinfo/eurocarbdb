/********************************************************************/
/*                          psnd_int2d.c                            */
/*                                                                  */
/* Collect, store ,read and edit 2D integrated peaks                */
/*                                                                  */
/* 1998, Albert van Kuik                                            */
/*                                                                  */
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


#define MARK_FOR_REMOVAL	-1
#define INTBOX			0
#define INTFILL			1
#define INTCONVEX		2
#define INTCROSS		3
#define INTCROSS2		4

static void int2d_callback(G_POPUP_CHILDINFO *ci);

/*************************************************************************
* INT2D QUEUE
*/

#define LABEL_OFFSET	50
#define ULABEL_OFFSET	(LABEL_OFFSET+3)

static char *trim(char *s) 
{
    int i;
    
    i=strlen(s)-1;
    while ((s[i]==' ' || s[i]=='\r'  ||  s[i]=='\n' 
                      ||  s[i]=='\t') && i>=0)
        s[i--]='\0';
    return s;
}

static int int2d_add(INT2DINFO *i2d_info, CBLOCK *cpar)
{
    int i;
    
    /*
     * Check if peak is already here
     */
    for (i=1;i<=cpar->i_count;i++) {
        if (cpar->box[0] == cpar->int2d_queue[i].box[0] &&
            cpar->box[1] == cpar->int2d_queue[i].box[1] &&
            cpar->box[2] == cpar->int2d_queue[i].box[2] &&
            cpar->box[3] == cpar->int2d_queue[i].box[3])
                return FALSE;
    }
    if (cpar->i_count >= cpar->i_size) {
        cpar->i_size += 100;
        cpar->int2d_queue = (INT2DINFO*) 
            realloc(cpar->int2d_queue, cpar->i_size *  sizeof(INT2DINFO));
    }
    memcpy(&(cpar->int2d_queue[cpar->i_count]), i2d_info, sizeof(INT2DINFO));
    cpar->i_count++;
    return TRUE;
}

static void int2d_delete(int pos, CBLOCK *cpar)
{
    int i;
    
    if (pos < 1 || pos >= cpar->i_count)
        return;
    for (i=pos;i<cpar->i_count-1;i++)
        memcpy(&(cpar->int2d_queue[i]), 
               &(cpar->int2d_queue[i+1]), 
               sizeof(INT2DINFO));
    cpar->i_count--;
    cpar->i_select= 0;
}

static void int2d_init(CBLOCK *cpar)
{
    INT2DINFO i2d;
    strcpy(i2d.label,"2D INTEGRATION");
    strcpy(i2d.label+LABEL_OFFSET,"");
    strcpy(i2d.label+ULABEL_OFFSET,"");
    int2d_add(&i2d, cpar); 
    cpar->i_select=0;
    cpar->i_label=0;
}

static int label_in_use(CBLOCK *cpar, int id1, int id2)
{
    int i;
    for (i=1;i<cpar->i_count;i++) { 
        if ((int) *(cpar->int2d_queue[i].label+LABEL_OFFSET)  == id1 &&
            (int) *(cpar->int2d_queue[i].label+LABEL_OFFSET+1) == id2)
                return TRUE;
    }
    return FALSE;
}

static int set_int2d(CBLOCK *cpar, char *userlabel, float sign)
{
    INT2DINFO i2d;
    int i,j, done = FALSE;
    
    while (!done) {
        j = 'A' + cpar->i_label % 26;
        i = 'A' + cpar->i_label / 26;
        if (label_in_use(cpar, i, j))
            cpar->i_label++;
        else
            done = TRUE;
    }
    sprintf(i2d.label, "%c%c: %d %d %d %d", 
            i,j,
            cpar->box[0],
            cpar->box[1], 
            cpar->box[2], 
            cpar->box[3]);
    sprintf(i2d.label+LABEL_OFFSET, "%c%c", i,j);
    strcpy(i2d.label+ULABEL_OFFSET,  userlabel);
    i2d.box[0]    = cpar->box[0];
    i2d.box[1]    = cpar->box[1];
    i2d.box[2]    = cpar->box[2];
    i2d.box[3]    = cpar->box[3];
    i2d.dint      = cpar->dint;
    i2d.npeaks    = 1;
    i2d.centx[0]  = cpar->centx;
    i2d.centy[0]  = cpar->centy;
    i2d.level[0]  = sign * fabs(cpar->clevel[0]);
    if (int2d_add(&i2d, cpar)) {
        cpar->i_label++;
        return TRUE;
    }
    return FALSE;
}

static void plot_box(CBLOCK *cpar, int pos)
{
    char *label;
    int i;
    
    g_moveto(cpar->int2d_queue[pos].box[0] + 0.5,
             cpar->int2d_queue[pos].box[2] + 0.5);
    if (cpar->int2d_queue[pos].label+ULABEL_OFFSET) {
        label = psnd_sprintf_temp("%s %s", 
                               cpar->int2d_queue[pos].label+LABEL_OFFSET,
                               cpar->int2d_queue[pos].label+ULABEL_OFFSET);
        g_label(label);
    }
    else
        g_label(cpar->int2d_queue[pos].label+LABEL_OFFSET);
    g_rectangle(cpar->int2d_queue[pos].box[0] - 0.4,
                cpar->int2d_queue[pos].box[2] - 0.4,
                cpar->int2d_queue[pos].box[1] + 0.4 ,
                cpar->int2d_queue[pos].box[3] + 0.4);
#ifdef notnow
    if (cpar->intmod != INTCROSS) 
        return;      
    /*
     * Draw cross for cross-integration
     */
    for (i=0;i<cpar->int2d_queue[pos].npeaks;i++) {
        g_moveto(cpar->int2d_queue[pos].box[0], 
                 (float) cpar->int2d_queue[pos].centy[i]);
        g_lineto(cpar->int2d_queue[pos].box[1], 
                 (float) cpar->int2d_queue[pos].centy[i]);
        g_moveto((float) cpar->int2d_queue[pos].centx[i], 
                  cpar->int2d_queue[pos].box[2]);
        g_lineto((float) cpar->int2d_queue[pos].centx[i], 
                  cpar->int2d_queue[pos].box[3]);
    }
#endif
}

static int edit_peak(MBLOCK *mblk, int box[], char *userlabel, char *peaklabel)
{
    int cont_id;
    int i,id;
    char *label;
    G_POPUP_CHILDINFO ci[20];

    label = psnd_sprintf_temp("Edit Peak %s", peaklabel);
    cont_id = g_popup_container_open(mblk->info->win_id, label,
                                     G_POPUP_WAIT);

    id=0;
    label = psnd_sprintf_temp("%d", box[0]);
    popup_add_text2(cont_id, &(ci[id]), id, label, "Left");
    id++;
    label = psnd_sprintf_temp("%d", box[1]);
    popup_add_text2(cont_id, &(ci[id]), id, label, "Right");
    id++;
    label = psnd_sprintf_temp("%d", box[2]);
    popup_add_text2(cont_id, &(ci[id]), id, label, "Bottom");
    id++;
    label = psnd_sprintf_temp("%d", box[3]);
    popup_add_text2(cont_id, &(ci[id]), id, label, "Top");
    id++;
    popup_add_text2(cont_id, &(ci[id]), id, userlabel, "User Label");

    if (g_popup_container_show(cont_id)) {
        id=0;
        box[0]     = psnd_scan_integer(ci[id].label);id++;
        box[1]     = psnd_scan_integer(ci[id].label);id++;
        box[2]     = psnd_scan_integer(ci[id].label);id++;
        box[3]     = psnd_scan_integer(ci[id].label);id++;
        strcpy(userlabel,ci[id].label);
        return TRUE;
    }
    return FALSE;
}


typedef enum {
    INT2D_LISTBOX,
    INT2D_ADD,
    INT2D_PRINT,
    INT2D_PRINT_REF,
    INT2D_PRINT_CENTER,
    INT2D_PRINT_SCALE,
    INT2D_REMOVE,
    INT2D_CLEAR,
    INT2D_PLOTALL,
    INT2D_HIDE,
    INT2D_SHOW,
    INT2D_READ,
    INT2D_WRITE,
    INT2D_CALC,
    INT2D_EDIT,
    INT2D_MOUSE,
    INT2D_MOUSE_PP,
    INT2D_NOISE,
    INT2D_OPTIONS,
    INT2D_PEAKPICK
} popup_adjust_ids;


static void plot_area(int ioff, int joff, int na, int nb, char *cbuf)
{
    int i, j;
    
    if (cbuf == NULL) 
        return;
    for (j=nb-1;j>=0;j--) {
        int istart = FALSE, icurr = FALSE;
        for (i=0;i<na;i++) {
            if (cbuf[i + j * na] == ISPEAK) {
                if (icurr == FALSE) {
                    icurr = TRUE;
                    istart = i;
                }
            }
            else {
                if (icurr == TRUE) {
                    icurr = FALSE;
                    g_moveto((float) (istart + ioff), (float) (j+joff));
                    g_lineto((float) (i-1+ ioff), (float) (j+joff));
                }
            }
        }
        if (icurr == TRUE) {
            icurr = FALSE;
            g_moveto((float) (istart + ioff), (float) (j+joff));
            g_lineto((float) (i-1+ ioff), (float) (j+joff));
        }
    
    }
    for (i=na-1;i>=0;i--) {
        int jstart = FALSE, jcurr = FALSE;
        for (j=0;j<nb;j++) {
            if (cbuf[i + j * na] == ISPEAK) {
                if (jcurr == FALSE) {
                    jcurr = TRUE;
                    jstart = j;
                }
            }
            else {
                if (jcurr == TRUE) {
                    jcurr = FALSE;
                    g_moveto((float) (i+ioff), (float) (jstart + joff));
                    g_lineto((float) (i+ioff), (float) (j-1+ joff));

                }
            }
        }
        if (jcurr == TRUE) {
            jcurr = FALSE;
            g_moveto((float) (i+ioff), (float) (jstart + joff));
            g_lineto((float) (i+ioff), (float) (j-1+ joff));
        }
    }
}


static void export_integrals(CBLOCK *cpar, FILE *outfile)
{
    int i,j;
    char *modes[] = { "BOX", "FLOOD-FILL", "CONVEX" };
    char *levels[] = { "POSITIVE", "NEGATIVE" };
    char *noyes[] = { "NO", "YES" };

    if (cpar->i_count < 1) 
        return;
    fprintf(outfile, "#\n# ==== 2D PEAKS ====\n#\n");
    fprintf(outfile, "#\n# number of peaks\n#\n");
    fprintf(outfile, "NPEAKS    %d\n", cpar->i_count);
    fprintf(outfile, "#\n# integration mode\n#\n");
    j = cpar->intmod;
    j = max (0, j);
    j = min (2, j);
    fprintf(outfile, "MODE      %s\n", modes[j]);
    fprintf(outfile, "#\n# integration level\n#\n");
    j = cpar->intlevel;
    j = max (0, j);
    j = min (1, j);
    fprintf(outfile, "LEVELS    %s\n", levels[j]);
    fprintf(outfile, "#\n# do offset correction\n#\n");
    j = cpar->zoint;
    j = max (0, j);
    j = min (1, j);
    fprintf(outfile, "OFFSET    %s\n", noyes[j]);
    fprintf(outfile, "#\n# reference distance\n#\n");
    fprintf(outfile, "REFDIST   %g\n", cpar->ref);
    fprintf(outfile, "#\n# reference intensity\n#\n");
    fprintf(outfile, "REFINT    %g\n", cpar->vref);
    fprintf(outfile, "#\n# Peaks follow below\n#\n");
    for (i=1;i<cpar->i_count;i++) {
        fprintf(outfile, "#\nPEAK         %d\n", i);
        fprintf(outfile, "LABEL        %s\n", cpar->int2d_queue[i].label+LABEL_OFFSET);
        fprintf(outfile, "USERLABEL    %s\n", cpar->int2d_queue[i].label+ULABEL_OFFSET);
        fprintf(outfile, "BOX          %d %d %d %d\n", cpar->int2d_queue[i].box[0],
                                                   cpar->int2d_queue[i].box[1],
                                                   cpar->int2d_queue[i].box[2],
                                                   cpar->int2d_queue[i].box[3]);
        fprintf(outfile, "NSUBPEAKS    %d\n", cpar->int2d_queue[i].npeaks);
        for (j=0;j<cpar->int2d_queue[i].npeaks;j++)
            fprintf(outfile, "X_Y_LEVEL    %d %d %g\n", 
                                             cpar->int2d_queue[i].centx[j],
                                             cpar->int2d_queue[i].centy[j],
                                             cpar->int2d_queue[i].level[j]);
        fprintf(outfile, "INTEGRAL     %g\n", cpar->int2d_queue[i].dint);
        fprintf(outfile, "ENDPEAK\n");
    }
}


static void import_integrals(CBLOCK *cpar, FILE *infile)
{
    int i,j,npeaks,reading_peak,current_sub_peak,peakcount;
    int x1, y1, x2, y2, xc[INT2DCENTMAX], yc[INT2DCENTMAX], nsubpeaks;
    float level[INT2DCENTMAX];
    char buf[PSND_STRLEN+1], *p1, *p2;
    char label[3], userlabel[INT2DLABELMAX-ULABEL_OFFSET];

    npeaks       = 0;
    peakcount    = 0;
    reading_peak = FALSE;
    while (fgets(buf,PSND_STRLEN,infile)) {
        if (buf[0] == '#' || buf[0] == '\n' 
                || buf[0] == '\r' || buf[0] == '\0')
            continue;
        p1 = buf;
        while (*p1 == ' ' || *p1 == '\t')
            p1++;
        p2 = p1;
        while (*p2 != ' ' && *p2 != '\t' && 
                  *p2 != '\n' && *p2 != '\r' && *p2 != '\0')
            p2++;
        if (*p2 != '\0')
             *p2++ = '\0';
        while (*p2 == ' ' || *p2 == '\t')
            p2++;
        if (npeaks == 0) {
            if (strcmp(p1, "NPEAKS") == 0)
                sscanf(p2, "%d", &npeaks);
            npeaks = max(0, npeaks);
        }
        if (npeaks == 0)
            continue;
        if (!reading_peak) {
            if (strcmp(p1, "MODE") == 0) {
                if (p2[0] == 'F')
                    cpar->intmod = INTFILL;
                else if (p2[0] == 'C')
                    cpar->intmod = INTCONVEX;
                else
                    cpar->intmod = INTBOX;
            }
            else if (strcmp(p1, "LEVELS") == 0) {
                if (p2[0] == 'N')
                    cpar->intlevel = 1;
                else
                    cpar->intlevel = 0;
            }
            else if (strcmp(p1, "OFFSET") == 0) {
                if (p2[0] == 'Y')
                    cpar->zoint = 1;
                else
                    cpar->zoint = 0;
            }
            else if (strcmp(p1, "REFDIST") == 0) {
                sscanf(p2, "%f", &(cpar->ref));
                cpar->rref = cpar->vref * pow(cpar->ref,6.0);
            }
            else if (strcmp(p1, "REFINT") == 0) {
                sscanf(p2, "%f", &(cpar->vref));
                cpar->rref = cpar->vref * pow(cpar->ref,6.0);
            }
            else if (strcmp(p1, "PEAK") == 0) {
                reading_peak = TRUE;
                x1 = y1 = x2 = y2 = 0;
                nsubpeaks = 1;
                current_sub_peak = 0;
                label[0] = 'A';
                label[1] = 'A';
                label[2] = '\0';
                userlabel[0] = '\0';
                for (i=0; i< INT2DCENTMAX; i++) {
                    level[i] = cpar->clevel[0];
                    xc[i] = yc[i] = 0;
                }
            }
        }
        else {
            if (strcmp(p1, "BOX") == 0) {
                sscanf(p2, "%d %d %d %d", &x1, &x2, &y1, &y2);
                x1 = max(cpar->ihamin, x1);
                x2 = min(cpar->ihamax, x2);
                y1 = max(cpar->ihbmin, y1);
                y2 = min(cpar->ihbmax, y2);
                if (x2 <= x1 || y2 < y1) {
                    reading_peak = FALSE;
                    continue;
                }
            }
            else if (strcmp(p1, "NSUBPEAKS") == 0) {
                sscanf(p2, "%d", &nsubpeaks);
                nsubpeaks = max (1, nsubpeaks);
                nsubpeaks = min (INT2DCENTMAX, nsubpeaks);
            }
            else if (strcmp(p1, "X_Y_LEVEL") == 0) {
                if (current_sub_peak >= nsubpeaks)
                    continue;
                i = current_sub_peak++;
                sscanf(p2, "%d %d %f", &(xc[i]), &(yc[i]), &(level[i]));
                xc[i] = max (0, xc[i]);
                xc[i] = min (x2, xc[i]);
                yc[i] = max (0, yc[i]);
                yc[i] = min (y2, yc[i]);
            }
            else if (strcmp(p1, "INTEGRAL") == 0) {
                sscanf(p2, "%f", &(cpar->dint));
            }
            else if (strcmp(p1, "LABEL") == 0) {
                if (isalnum((int)p2[0]) && isalnum((int)p2[1])) {
                    label[0] = p2[0];
                    label[1] = p2[1];
                }
            }
            else if (strcmp(p1, "USERLABEL") == 0) {
                i=0;
                while (i<INT2DLABELMAX-ULABEL_OFFSET-1) {
                    if (*p2 == '\n' || *p2 == '\r' || *p2 == '\0')
                        break;
                    userlabel[i] = *p2;
                    p2++;
                    i++;
                }
                userlabel[i] = '\0';
                if (isalnum((int)p2[0]) && isalnum((int)p2[1])) {
                    label[0] = p2[0];
                    label[1] = p2[1];
                }
            }
            else if (strcmp(p1, "ENDPEAK") == 0) {
                INT2DINFO i2d;
                if (x1 != x2) {
                    cpar->box[0] = x1;
                    cpar->box[1] = x2;
                    cpar->box[2] = y1;
                    cpar->box[3] = y2;
                    nsubpeaks = current_sub_peak;
                    cpar->centx = xc[0];
                    cpar->centy = yc[0];

                    sprintf(i2d.label, "%s: %d %d %d %d", 
                            label,
                            cpar->box[0],
                            cpar->box[1], 
                            cpar->box[2], 
                            cpar->box[3]);
                    sprintf(i2d.label+LABEL_OFFSET, "%s", label);
                    strcpy(i2d.label+ULABEL_OFFSET,  userlabel);
                    i2d.box[0]    = cpar->box[0];
                    i2d.box[1]    = cpar->box[1];
                    i2d.box[2]    = cpar->box[2];
                    i2d.box[3]    = cpar->box[3];
                    i2d.dint      = cpar->dint;
                    i2d.npeaks    = nsubpeaks;
                    for (i=0;i<nsubpeaks;i++) {
                        i2d.centx[i]  = xc[i];
                        i2d.centy[i]  = yc[i];
                        i2d.level[i]  = level[i];
                    }
                    if (int2d_add(&i2d, cpar)) {
                        cpar->i_label++;
                        peakcount++;
                        if (peakcount >= npeaks)
                            return;
                    }
                }
                reading_peak = FALSE;
            }
        }
    }
}

int psnd_integrals_read(MBLOCK *mblk, CBLOCK *cpar, char *filename)
{
    FILE *infile;
    int i;
    G_POPUP_CHILDINFO childinfo, *ci;
    POPUP_INFO *popinf = mblk->popinf + POP_INT2D;

    if (mblk->info->foreground) {
        ci = &childinfo;
        g_popup_init_info(ci);
        ci->cont_id = popinf->cont_id;
        ci->type = G_CHILD_PUSHBUTTON;
        ci->func  = int2d_callback;
        ci->userdata      = (void*) mblk;
    } 

    infile = fopen(filename, "r");
    if (!infile)
        return FALSE;
    if (cpar->i_count == 0)
        int2d_init(cpar);
    if (mblk->info->foreground) {
        if (cpar->i_count > 1) {
            ci->id = INT2D_CLEAR;
            int2d_callback(ci);
        }
    }
    else {
        cpar->i_count  = 1;
        cpar->i_select = 0;
        cpar->i_label  = 0;
    }
    import_integrals(cpar, infile);
    fclose(infile);

    if (mblk->info->foreground) {
        for (i=1;i<cpar->i_count;i++) 
            g_popup_append_item(ci->cont_id, INT2D_LISTBOX,
                                    cpar->int2d_queue[i].label);
        ci->id = INT2D_CALC;
        int2d_callback(ci);
        ci->id = INT2D_PLOTALL;
        int2d_callback(ci);
    }
    return max(0, cpar->i_count - 1);;
}

int psnd_integrals_get_numpeaks(MBLOCK *mblk, CBLOCK *cpar)
{
    return max(0, cpar->i_count - 1);
}

float psnd_integral_calc(MBLOCK *mblk, CBLOCK *cpar, int peak_id)
{
    int i;
    DBLOCK *dat  = DAT;

    int k,jx1,jx2,jy1,jy2;
    float dint = 0.0;
    int jxc[INT2DCENTMAX], jyc[INT2DCENTMAX];

    if (peak_id < 1  || peak_id >= cpar->i_count)
        return 0.0;
    i = peak_id;
    jy1 = cpar->int2d_queue[i].box[2];
    jy2 = cpar->int2d_queue[i].box[3];
    jx1 = cpar->int2d_queue[i].box[0];
    jx2 = cpar->int2d_queue[i].box[1];
    for (k=0;k<cpar->int2d_queue[i].npeaks;k++) {
        jyc[k] = cpar->int2d_queue[i].centy[k];
        jxc[k] = cpar->int2d_queue[i].centx[k];
        jyc[k] = max(jyc[k],jy1);
        jyc[k] = min(jyc[k],jy2);
        jxc[k] = max(jxc[k],jx1);
        jxc[k] = min(jxc[k],jx2);
        cpar->int2d_queue[i].centy[k] = jyc[k];
        cpar->int2d_queue[i].centx[k] = jxc[k];
        jyc[k] -= jy1;
        jxc[k] -= jx1;
    }
    integrate2d(dat->finfo[INFILE].ifile,
                cpar->xdata,
                (cpar->intmod != INTBOX),
                cpar->int2d_queue[i].box[2],
                cpar->int2d_queue[i].box[3],
                cpar->int2d_queue[i].box[0],
                cpar->int2d_queue[i].box[1],
                jyc,
                jxc,
                cpar->int2d_queue[i].npeaks,
                cpar->zoint,
                &cpar->zo,
                cpar->int2d_queue[i].level,
                &dint,
                (cpar->intmod == INTCONVEX),
                NULL,
                read2d,
                (void*)mblk);
    for (k=0;k<cpar->int2d_queue[i].npeaks;k++) {
        cpar->int2d_queue[i].centy[k] = jyc[k] + jy1;
        cpar->int2d_queue[i].centx[k] = jxc[k] + jx1;
    }
    cpar->int2d_queue[i].dint = dint;
                           
    return dint;
}

static void int2d_callback(G_POPUP_CHILDINFO *ci)
{
    char s[PSND_STRLEN],s2[PSND_STRLEN];
    CBLOCK *cpar;
    SBLOCK *spar;
    DBLOCK *dat;
    int i, i_count_save, vp, obj, col;
    MBLOCK *mblk = (MBLOCK *) ci->userdata;
    POPUP_INFO *popinf = mblk->popinf + POP_INT2D;
    
    cpar = mblk->cpar_screen;
    spar = mblk->spar;
    dat  = DAT;
    vp   = spar[S_INT2D].vp_id,
    obj  = spar[S_INT2D].obj_id,
    col  = spar[S_INT2D].color;
    switch (ci->type) {
        case G_CHILD_LISTBOX:
            cpar->i_select = min(cpar->i_count, ci->item);
            break;
        case G_CHILD_RADIOBOX:
            switch (ci->id) {
                case INT2D_PEAKPICK:
                    cpar->ppmode = ci->item;
                    break;
            }
            break;
        case G_CHILD_PUSHBUTTON:
            switch (ci->id) {
                case INT2D_ADD:
                    spar[S_INT2D].show = TRUE;
                    if (!set_int2d(cpar,"",1.0))
                        break;
                    g_popup_append_item(ci->cont_id, INT2D_LISTBOX,
                        cpar->int2d_queue[cpar->i_count-1].label);
                    g_append_object(obj);
                    plot_box(cpar,cpar->i_count-1);
                    g_close_object(obj);
                    g_plotall();
                    break;
                case INT2D_EDIT:
                    if (cpar->i_select > 0) {
                        int box[4];
                        int k,jx1,jx2,jy1,jy2;
                        float dint = 0.0;
                        int jxc[INT2DCENTMAX], jyc[INT2DCENTMAX];

                        i = cpar->i_select;
                        for (k=0;k<4;k++)
                            box[k]=cpar->int2d_queue[i].box[k];
                        strcpy(s,cpar->int2d_queue[i].label+ULABEL_OFFSET);
                        if (edit_peak(mblk,
                                      box, 
                                      s,
                                      cpar->int2d_queue[i].label+LABEL_OFFSET)) {
                            if (box[1] < box[0]) {
                                int itmp = box[1];
                                box[1] = box[0];
                                box[0] = itmp;
                            }
                            if (box[3] < box[2]) {
                                int itmp = box[3];
                                box[3] = box[2];
                                box[2] = itmp;
                            }
                            box[0]=max(cpar->ihamin-1,box[0]);
                            box[1]=min(cpar->ihamax-1,box[1]);
                            box[2]=max(cpar->ihbmin-1,box[2]);
                            box[3]=min(cpar->ihbmax-1,box[3]);
                            for (k=0;k<4;k++)
                                cpar->int2d_queue[i].box[k] = box[k];
                            jy1 = cpar->int2d_queue[i].box[2];
                            jy2 = cpar->int2d_queue[i].box[3];
                            jx1 = cpar->int2d_queue[i].box[0];
                            jx2 = cpar->int2d_queue[i].box[1];
                            cpar->int2d_queue[i].dint  = 0.0;
                            for (k=0;k<cpar->int2d_queue[i].npeaks;k++) {
                                jyc[k] = cpar->int2d_queue[i].centy[k];
                                jxc[k] = cpar->int2d_queue[i].centx[k];
                                jyc[k] = max(jyc[k],box[2]);
                                jyc[k] = min(jyc[k],box[3]);
                                jxc[k] = max(jxc[k],box[0]);
                                jxc[k] = min(jxc[k],box[1]);
                                cpar->int2d_queue[i].centy[k] = jyc[k];
                                cpar->int2d_queue[i].centx[k] = jxc[k];
                                jyc[k] -= jy1;
                                jxc[k] -= jx1;
                            }
                            strcpy(cpar->int2d_queue[i].label+ULABEL_OFFSET,trim(s));
                            integrate2d(dat->finfo[INFILE].ifile,
                                       cpar->xdata,
                                       (cpar->intmod != INTBOX),
                                       cpar->int2d_queue[i].box[2],
                                       cpar->int2d_queue[i].box[3],
                                       cpar->int2d_queue[i].box[0],
                                       cpar->int2d_queue[i].box[1],
                                       jyc,
                                       jxc,
                                       cpar->int2d_queue[i].npeaks,
                                       cpar->zoint,
                                       &cpar->zo,
                                       cpar->int2d_queue[i].level,
                                       &dint,
                                       (cpar->intmod == INTCONVEX),
                                       NULL,
                                       read2d,
                                       (void*)mblk);
                            for (k=0;k<cpar->int2d_queue[i].npeaks;k++) {
                                cpar->int2d_queue[i].centy[k] = jyc[k] + jy1;
                                cpar->int2d_queue[i].centx[k] = jxc[k] + jx1;
                            }
                            cpar->int2d_queue[i].dint = dint;
                            sprintf(cpar->int2d_queue[i].label, 
                                    "%s: %d %d %d %d",
                                    cpar->int2d_queue[i].label+LABEL_OFFSET, 
                                    cpar->int2d_queue[i].box[0],
                                    cpar->int2d_queue[i].box[1], 
                                    cpar->int2d_queue[i].box[2], 
                                    cpar->int2d_queue[i].box[3]);
                            g_popup_replace_item(ci->cont_id, 
                                                 INT2D_LISTBOX,
                                                 i,
                                                 cpar->int2d_queue[i].label);
                            ci->id = INT2D_PLOTALL;
                            int2d_callback(ci);
                        }
                    }
                    break;
                case INT2D_CALC: 
                     if (cpar->intmod == INTBOX || 
                             cpar->intmod == INTFILL || 
                             cpar->intmod == INTCONVEX) {
                        for (i=1;i<cpar->i_count;i++) 
                            psnd_integral_calc(mblk, cpar, i);
                    }
                    break;

                case INT2D_PRINT:
                    if (cpar->i_count > 0) {
                        for (i=1;i<cpar->i_count;i++) {
                            psnd_printf(mblk,"%s: %3d %3d %3d %3d A=%15g %-20s\n",
                                cpar->int2d_queue[i].label+LABEL_OFFSET,
                                cpar->int2d_queue[i].box[0],
                                cpar->int2d_queue[i].box[2],
                                cpar->int2d_queue[i].box[1],
                                cpar->int2d_queue[i].box[3],
                                cpar->int2d_queue[i].dint,
                                cpar->int2d_queue[i].label+ULABEL_OFFSET);
                        }
                    }
                    break;
                case INT2D_PRINT_SCALE:
                    if (cpar->i_count > 0) {
                        for (i=1;i<cpar->i_count;i++) {
                            psnd_printf(mblk,"%s: %3d %3d %3d %3d A=%15g %-20s\n",
                                cpar->int2d_queue[i].label+LABEL_OFFSET,
                                cpar->int2d_queue[i].box[0],
                                cpar->int2d_queue[i].box[2],
                                cpar->int2d_queue[i].box[1],
                                cpar->int2d_queue[i].box[3],
                                cpar->int2d_queue[i].dint /cpar->scale_i,
                                cpar->int2d_queue[i].label+ULABEL_OFFSET);
                        }
                    }
                    break;
                case INT2D_PRINT_REF:
                    if (cpar->i_count > 0) {
                        for (i=1;i<cpar->i_count;i++) {
                            float r, small = 1.0e-6;
                            r = pow(cpar->rref/
                                  max(fabs(cpar->int2d_queue[i].dint),small),(1./6.));
                            psnd_printf(mblk,"%s: %3d %3d %3d %3d A=%15g R=%g\n",
                                cpar->int2d_queue[i].label+LABEL_OFFSET,
                                cpar->int2d_queue[i].box[0],
                                cpar->int2d_queue[i].box[2],
                                cpar->int2d_queue[i].box[1],
                                cpar->int2d_queue[i].box[3],
                                cpar->int2d_queue[i].dint,
                                r);
                        }
                    }
                    break;
                case INT2D_PRINT_CENTER:
                    if (cpar->i_count > 0) {
                        int k;
                        for (i=1;i<cpar->i_count;i++) {
                            for (k=0;k<cpar->int2d_queue[i].npeaks;k++) {
                                float xppm, yppm;
                                xppm = (float) cpar->int2d_queue[i].centx[k];
                                yppm = (float) cpar->int2d_queue[i].centy[k];
                                xppm = psnd_chan2ppm(xppm, cpar->par1->sfd, cpar->par1->swhold,
                                                  cpar->par1->nsiz, cpar->par1->xref,
                                                  cpar->par1->aref, cpar->par1->bref, cpar->ihamin);
                                yppm = psnd_chan2ppm(yppm, cpar->par2->sfd, cpar->par2->swhold,
                                                  cpar->par2->nsiz, cpar->par2->xref,
                                                  cpar->par2->aref, cpar->par2->bref, cpar->ihbmin);
                                if (k==0)
                                    psnd_printf(mblk,"%s: Cent x,y = %d, %d = %.3f, %.3f ppm\n",
                                               cpar->int2d_queue[i].label+LABEL_OFFSET,
                                               cpar->int2d_queue[i].centx[k],
                                               cpar->int2d_queue[i].centy[k],
                                               xppm, yppm);
                                else
                                    psnd_printf(mblk,"    Cent x,y = %d, %d = %.3f, %.3f ppm\n",
                                               cpar->int2d_queue[i].centx[k],
                                               cpar->int2d_queue[i].centy[k],
                                               xppm, yppm);
                            }
                        }
                    }
                    break;
                case INT2D_NOISE: 
                    if (cpar->i_count > 0) 
                    for (i=1;i<cpar->i_count;i++) {
                        int ja1, ja2, jb1, jb2;
                        float avg, rms;

                        ja1 = cpar->int2d_queue[i].box[0];
                        ja2 = cpar->int2d_queue[i].box[1];
                        jb1 = cpar->int2d_queue[i].box[2];
                        jb2 = cpar->int2d_queue[i].box[3];
                        if (ja2 < ja2) {
                            int tmp = ja1;
                            ja1 = ja2;
                            ja2 = tmp;
                        }
                        if (jb2 < jb1) {
                           int tmp = jb1;
                           jb1 = jb2;
                           jb2 = tmp;
                        }
                        ja1=max((float)mblk->cpar_screen->ihamin,ja1);
                        ja2=min((float)mblk->cpar_screen->ihamax,ja2);
                        jb1=max((float)mblk->cpar_screen->ihbmin,jb1);
                        jb2=min((float)mblk->cpar_screen->ihbmax,jb2);
                        rmsn2d(DAT->finfo[INFILE].ifile,
                               mblk->cpar_screen->xdata,
                               jb1,
                               jb2,
                               ja1,
                               ja2,
                               &avg,
                               &rms,
                               read2d,
                               (void*)mblk);
                        psnd_printf(mblk,"%s: %3d %3d %3d %3d Average = %10g, RMS noise = %10g\n",
                                cpar->int2d_queue[i].label+LABEL_OFFSET,
                               ja1,
                               ja2,
                               jb1,
                               jb2,
                                avg, rms);
                    }
                    break;
                case INT2D_READ:
                
                    {
                    char *filename;
                    if ((filename = psnd_getfilename(mblk,"Read Peaks", 
                                                     "*.int")) != NULL) {
                        FILE *infile;
                        infile = fopen(filename, "r");
                        if (infile) {
                            if (cpar->i_count > 1) {
                                ci->id = INT2D_CLEAR;
                                int2d_callback(ci);
                            }
                            import_integrals(cpar, infile);
                            fclose(infile);
                            for (i=1;i<cpar->i_count;i++) 
                                g_popup_append_item(ci->cont_id, INT2D_LISTBOX,
                                    cpar->int2d_queue[i].label);
                            ci->id = INT2D_CALC;
                            int2d_callback(ci);
                            ci->id = INT2D_PLOTALL;
                            int2d_callback(ci);
                        }
                    }
                    }
#ifdef blaaa
                    {
                        FILE *infile = stdin;
                        int i_maxcount = 0;
                        
                        char *filename = psnd_getfilename(mblk,"Read integrals from file","*.au");
                        if (!filename)
                            break; 

                        if ((infile = fopen(filename,"r")) == NULL) {
                            psnd_printf(mblk,"Can not open file %s\n", filename);
                            break;
                        }
                       
                        if (cpar->i_count > 1) {
                            ci->id = INT2D_CLEAR;
                            int2d_callback(ci);
                        }
                        s[0] = '\0';
                        s2[0] = '\0';
                        while (fgets(s, PSND_STRLEN, infile)) {
                            char *p,*q;
                            if ((p=strstr(s,"PeakLabel=")) != NULL){
                                p += strlen("PeakLabel=");
                                strcpy(s2,p);
                                continue;
                            }
                            strlwr(s);
                            if ((p=strstr(s,"integral")) != NULL){
                                q = p + 8;
                                if ((p=strchr(q,'(')) != NULL){
                                    p++;
                                    sscanf(p, "%d,%d,%d,%d",
                                           cpar->box,
                                           cpar->box+1,
                                           cpar->box+2,
                                           cpar->box+3);  
                                    cpar->i_label = cpar->i_count-1;                                         
                                    if ((p=strstr(p,"label=")) != NULL){
                                        int n,m,i_label=0;
                                        p += 6;
                                        if (p[0] >= 'a' && p[0] <= 'z' &&
                                            p[1] >= 'a' && p[1] <= 'z')
                                            cpar->i_label = 
                                                (p[0] - 'a') * 26 + (p[1] - 'a');
                                    }
                                    i_maxcount = max(i_maxcount, cpar->i_label);
                                    cpar->dint = 0.0;
                                    set_int2d(cpar,trim(s2),1.0);
                                }
                                else if ((p=strstr(q,"offset")) != NULL){
                                    p += 6;
                                    cpar->zoint = psnd_scan_integer(p);
                                }
                            }
                            s[0] = '\0';
                            s2[0]= '\0';
                        }
                        cpar->i_label = i_maxcount+1;
                        fclose(infile);
                        for (i=1;i<cpar->i_count;i++) 
                            g_popup_append_item(ci->cont_id, INT2D_LISTBOX,
                                cpar->int2d_queue[i].label);
                        ci->id = INT2D_CALC;
                        int2d_callback(ci);
                        ci->id = INT2D_PLOTALL;
                        int2d_callback(ci);
                    }
#endif
                    break;
                case INT2D_WRITE:
                    if (cpar->i_count > 0) {
char *filename;
                    if ((filename = psnd_savefilename(mblk,"Save Peaks", 
                                                     "*.int")) != NULL) {
                        FILE *outfile;
                        outfile = fopen(filename, "w");
                        if (outfile) {
                            export_integrals(cpar, outfile);
                            fclose(outfile);
                        }
                    }
#ifdef bla

                        int labelsize=10;
                        FILE *outfile = stdout;
                        char *filename = psnd_savefilename(mblk,"Write integrals to file","*.au");
                        if (!filename)
                            break; 

                        if ((outfile = fopen(filename,"w")) == NULL) {
                            psnd_printf(mblk,"Can not open file %s\n", filename);
                            break;
                        }

                        fprintf(outfile, "# Integrals written by prospectnd\n\n");
                        fprintf(outfile, "#####################################\n");
                        fprintf(outfile, "# DO **NOT** RUN FROM SCRIPT-EDITOR #\n");
                        fprintf(outfile, "# Run from command line with :      #\n");
                        fprintf(outfile, "# prospectnd -b 'thisscript.au'     #\n");
                        fprintf(outfile, "#####################################\n\n");
                        fprintf(outfile, "# Do NOT edit this function\n");
                        fprintf(outfile, "function buildup\n");
                        fprintf(outfile, "ropen %%1\n");
                        fprintf(outfile, "dr $2\n");
                        fprintf(outfile, "$i = $3\n");
                        for (i=1;i<cpar->i_count;i++) 
                            labelsize = max(labelsize, 
                                            (int)strlen(cpar->int2d_queue[i].label+ULABEL_OFFSET));
                        for (i=1;i<cpar->i_count;i++) {
                            fprintf(outfile, 
                                "$i += 1\n");
                            if (cpar->int2d_queue[i].label[ULABEL_OFFSET])
                                fprintf(outfile, 
                                    "# PeakLabel=%s\n",
                                    cpar->int2d_queue[i].label+ULABEL_OFFSET);
                            fprintf(outfile, 
                                "@a[$i] = integral(%3d, %3d, %3d, %3d) "
                                "# Label=%s A=%g\n",
                                cpar->int2d_queue[i].box[0],
                                cpar->int2d_queue[i].box[1],
                                cpar->int2d_queue[i].box[2],
                                cpar->int2d_queue[i].box[3],
                                cpar->int2d_queue[i].label+LABEL_OFFSET,
                                cpar->int2d_queue[i].dint);
                        }
                        fprintf(outfile, "rclose\n");
                        fprintf(outfile, "return $i\n");
                        fprintf(outfile, "end\n\n");

                        fprintf(outfile, "function labels\n");
                        fprintf(outfile, "%%label = \"\"\n");
                        fprintf(outfile, "$lstep = $1\n");
                        fprintf(outfile, "$ll = 1\n");
                        for (i=1;i<cpar->i_count;i++) {
                            if (cpar->int2d_queue[i].label[ULABEL_OFFSET])
                                fprintf(outfile, 
                                    "%%label[$ll..] = \"%s:%s\"\n$ll += $lstep\n",
                                    cpar->int2d_queue[i].label+LABEL_OFFSET,
                                    cpar->int2d_queue[i].label+ULABEL_OFFSET);
                            else
                                fprintf(outfile, 
                                    "%%label[$ll..] = \"%s\"\n$ll += $lstep\n",
                                    cpar->int2d_queue[i].label+LABEL_OFFSET);
                        }
                        fprintf(outfile, "return %%label\n");
                        fprintf(outfile, "end\n\n");
                        


                        fprintf(outfile, "########################################\n");
                        fprintf(outfile, "# buildup part\n");
                        fprintf(outfile, "# set numfiles to the number of 2d files\n");
                        fprintf(outfile, "$numfiles = 1\n");
                        fprintf(outfile, "%%file1 = \"%s\"\n", dat->finfo[INFILE].name);
                        fprintf(outfile, "# Add the other files here\n");
                        fprintf(outfile, "# %%file2 = ...\n");
                        fprintf(outfile, "$direction = %d\n", dat->pars[0]->icrdir);
                        fprintf(outfile, "#set array @a to zero\n");
                        fprintf(outfile, "@a = 0\n");
                        fprintf(outfile, "# Toggle offset correction\n");
                        fprintf(outfile, "integral offset %d\n", cpar->zoint);
                        fprintf(outfile, "$j = 0\n");
                        fprintf(outfile, "$j = $buildup(%%file1, $direction, $j)\n");
                        fprintf(outfile, "# For each file, add line here\n");
                        fprintf(outfile, "# $j = $buildup(%%file2, $direction, $j)\n\n");
                        fprintf(outfile, "$labelsize = %d\n",labelsize+4);
                        fprintf(outfile, "%%peaklabels = %%labels($labelsize)\n\n");
                        fprintf(outfile, "# For each peak, integrals will be printed here\n");
                        fprintf(outfile, "$numpeaks = $j / $numfiles\n");
                        fprintf(outfile, "for $i in [1..$numpeaks] do\n");
                        fprintf(outfile, "    # Print labels for each peak\n");
                        fprintf(outfile, "    $lstart = ($i-1)*$labelsize+1\n");
                        fprintf(outfile, "    $lstop  = $i*$labelsize\n");
                        fprintf(outfile, "    println trim(%%peaklabels[$lstart..$lstop])\n");
                        fprintf(outfile, "    for $j in [1..$numfiles] do\n");
                        fprintf(outfile, "        $k = $i + $numpeaks * ($j - 1)\n");
                        fprintf(outfile, "        printf \"%%15.3f\\n\", @a[$k]\n");
                        fprintf(outfile, "    done\n");
                        fprintf(outfile, "    println \"\"\n");
                        fprintf(outfile, "done\n");
                        fclose(outfile);
#endif
                    }
                    break;
                case INT2D_REMOVE:
                    if (cpar->i_select > 0) {
                        g_popup_remove_item(ci->cont_id, INT2D_LISTBOX,
                            cpar->i_select);
                        int2d_delete(cpar->i_select,cpar);
                        ci->id = INT2D_PLOTALL;
                        int2d_callback(ci);
                    }
                    break;
                case INT2D_CLEAR:
                    if (cpar->i_count > 1) {
                        cpar->i_count=1;
                        g_popup_remove_item(ci->cont_id, INT2D_LISTBOX,
                                                -1);
                        g_popup_append_item(ci->cont_id, INT2D_LISTBOX,
                                    "2D INTEGRATION");
                    }
                    cpar->i_select = 0;
                    cpar->i_label  = 0;
                    ci->id = INT2D_PLOTALL;
                    int2d_callback(ci);
                    break;
                case INT2D_PLOTALL:
                    if (cpar->i_count > 0) {
                        spar[S_INT2D].show = TRUE;
                        g_delete_object(obj);
                        g_open_object(obj);
                        g_select_viewport(spar[S_INT2D].vp_id);

                        g_set_foreground(col);
                        for (i=1;i<cpar->i_count;i++) 
                            plot_box(cpar, i);

                        if (cpar->i_count > 1 && 
                               (cpar->intmod == INTFILL || cpar->intmod == INTCONVEX)) {
                            char *cbuf;
                            int na,nb,ioff,joff;

                            for (i=1;i<cpar->i_count;i++) {
                                float dint = 0.0;
                                int k,jx1,jx2,jy1,jy2;
                                int jxc[INT2DCENTMAX], jyc[INT2DCENTMAX];
                                jy1 = cpar->int2d_queue[i].box[2];
                                jy2 = cpar->int2d_queue[i].box[3];
                                jx1 = cpar->int2d_queue[i].box[0];
                                jx2 = cpar->int2d_queue[i].box[1];
                                nb = jy2 - jy1 + 1;
                                na = jx2 - jx1 + 1;
                                ioff = cpar->int2d_queue[i].box[0];
                                joff = cpar->int2d_queue[i].box[2];
                                cbuf = NULL;
                                for (k=0;k<cpar->int2d_queue[i].npeaks;k++) {
                                    jyc[k] = cpar->int2d_queue[i].centy[k];
                                    jxc[k] = cpar->int2d_queue[i].centx[k];
                                    jyc[k] = max(jyc[k],jy1);
                                    jyc[k] = min(jyc[k],jy2);
                                    jxc[k] = max(jxc[k],jx1);
                                    jxc[k] = min(jxc[k],jx2);
                                    cpar->int2d_queue[i].centy[k] = jyc[k];
                                    cpar->int2d_queue[i].centx[k] = jxc[k];
                                    jyc[k] -= jy1;
                                    jxc[k] -= jx1;
                                }
                                integrate2d(dat->finfo[INFILE].ifile,
                                       cpar->xdata,
                                       (cpar->intmod != INTBOX),
                                       cpar->int2d_queue[i].box[2],
                                       cpar->int2d_queue[i].box[3],
                                       cpar->int2d_queue[i].box[0],
                                       cpar->int2d_queue[i].box[1],
                                       jyc,
                                       jxc,
                                       cpar->int2d_queue[i].npeaks,
                                       cpar->zoint,
                                       &cpar->zo,
                                       cpar->int2d_queue[i].level,
                                       &dint,
                                       (cpar->intmod == INTCONVEX),
                                       &cbuf,
                                       read2d,
                                       (void*)mblk);

                                if (cbuf != NULL) {
                                    plot_area(ioff, joff, na, nb, cbuf);
                                    free(cbuf);
                                }
                            }
                        }

                        g_close_object(obj);
                        g_call_object(obj);
                        g_plotall();
                    }
                    break;
                case INT2D_HIDE:
                    g_set_objectstatus(obj, G_SLEEP);
                    g_plotall();
                    break;
                case INT2D_SHOW:
                    g_set_objectstatus(obj, G_AWAKE);
                    g_plotall();
                    break;
                case INT2D_MOUSE:
                    {
                        psnd_set_cursormode(mblk, 0, MOUSE_INTEGRATION);
                    }
                    break;
                case INT2D_MOUSE_PP:
                    {
                        psnd_set_cursormode(mblk, 0, MOUSE_PEAKPICK);
                    }
                    break;
                case INT2D_OPTIONS:
                    psnd_set_param(mblk, 0,  NULL,  PSND_CI);
                    break;
            }
            break;
        case G_CHILD_OK:
        case G_CHILD_CANCEL:
            if (spar[S_INT2D].show == TRUE) {
                spar[S_INT2D].show = FALSE;
                g_set_objectstatus(obj, G_SLEEP);
                g_plotall();
            }
            popinf->visible = FALSE;
            break;
    }
}


#define MAXLIST	100
void psnd_popup_int2d(MBLOCK *mblk)
{
    int i,id;
    char *list[MAXLIST];
    G_POPUP_CHILDINFO ci[28];
    CBLOCK *cpar = mblk->cpar_screen;
    POPUP_INFO *popinf = mblk->popinf + POP_INT2D;

    if (popinf->visible)
        return;
    popinf->visible = TRUE;
    if (! popinf->cont_id) {
        int cont_id;

        popinf->cont_id = g_popup_container_open(mblk->info->win_id, 
                "2D Integration", G_POPUP_KEEP|G_POPUP_SINGLEBUTTON);
        cont_id = popinf->cont_id;
        if (cpar->i_count == 0)
            int2d_init(cpar);
            
        for (i=0;i<cpar->i_count && i < MAXLIST;i++)
            list[i] = cpar->int2d_queue[i].label;

        id=0;
        g_popup_init_info(&(ci[id]));
        ci[id].type          = G_CHILD_LISTBOX;
        ci[id].id            = INT2D_LISTBOX;
        ci[id].item_count    = cpar->i_count;
        ci[id].data          = list;
        ci[id].item          = 0;
        ci[id].items_visible = 10;
        ci[id].func          = int2d_callback;
        ci[id].userdata      = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));


        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type       = G_CHILD_PANEL;
        ci[id].item       = TRUE;
        ci[id].frame      = TRUE;
        ci[id].title      = "Integrals";
        ci[id].horizontal = TRUE;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_PUSHBUTTON;
        ci[id].id    = INT2D_ADD;
        ci[id].label = "Add";
        ci[id].func  = int2d_callback;
        ci[id].userdata      = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_PUSHBUTTON;
        ci[id].id    = INT2D_EDIT;
        ci[id].label = "Edit";
        ci[id].func  = int2d_callback;
        ci[id].userdata      = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_PUSHBUTTON;
        ci[id].id    = INT2D_REMOVE;
        ci[id].label = "Remove";
        ci[id].func  = int2d_callback;
        ci[id].userdata      = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));
        
        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_PUSHBUTTON;
        ci[id].id    = INT2D_CLEAR;
        ci[id].label = "Reset";
        ci[id].func  = int2d_callback;
        ci[id].userdata      = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

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
        ci[id].title      = "Draw Boxes";
        ci[id].horizontal = TRUE;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_PUSHBUTTON;
        ci[id].id    = INT2D_PLOTALL;
        ci[id].label = "  Plot ";
        ci[id].func  = int2d_callback;
        ci[id].userdata      = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_PUSHBUTTON;
        ci[id].id    = INT2D_HIDE;
        ci[id].label = "  Hide ";
        ci[id].func  = int2d_callback;
        ci[id].userdata      = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_PUSHBUTTON;
        ci[id].id    = INT2D_SHOW;
        ci[id].label = "  Show ";
        ci[id].func  = int2d_callback;
        ci[id].userdata      = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

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
        ci[id].title      = "I/O";
        ci[id].horizontal = TRUE;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_PUSHBUTTON;
        ci[id].id    = INT2D_READ;
        ci[id].label = "   Read    ";
        ci[id].func  = int2d_callback;
        ci[id].userdata      = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_PUSHBUTTON;
        ci[id].id    = INT2D_WRITE;
        ci[id].label = "   Write   ";
        ci[id].func  = int2d_callback;
        ci[id].userdata      = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_PANEL;
        ci[id].item  = FALSE;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_PUSHBUTTON;
        ci[id].id    = INT2D_MOUSE;
        ci[id].label = "Grab Mouse Integrate";
        ci[id].func  = int2d_callback;
        ci[id].userdata      = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_PUSHBUTTON;
        ci[id].id    = INT2D_MOUSE_PP;
        ci[id].label = "Grab Mouse Peak Pick";
        ci[id].func  = int2d_callback;
        ci[id].userdata      = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        {
        static char *checklist[] = { "Peak Pick Find", 
                                     "Peak Pick Remove", 
                                     "Reak Pick Merge"};
        static int check_select[] = { 0, 0,0}; 
        check_select[0] = (cpar->ppmode == PEAKPICK_REMOVE);
        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_RADIOBOX;
        ci[id].id    = INT2D_PEAKPICK;
        ci[id].item_count = 3;
        ci[id].data       = checklist;
        ci[id].item     =  cpar->ppmode;
        ci[id].func  = int2d_callback;
        ci[id].userdata      = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));
        }

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_PUSHBUTTON;
        ci[id].id    = INT2D_PRINT;
        ci[id].label = "Print Integrals";
        ci[id].func  = int2d_callback;
        ci[id].userdata      = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_PUSHBUTTON;
        ci[id].id    = INT2D_PRINT_REF;
        ci[id].label = "Print Integrals + Distance";
        ci[id].func  = int2d_callback;
        ci[id].userdata      = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_PUSHBUTTON;
        ci[id].id    = INT2D_PRINT_CENTER;
        ci[id].label = "Print Peak Center";
        ci[id].func  = int2d_callback;
        ci[id].userdata      = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_PUSHBUTTON;
        ci[id].id    = INT2D_PRINT_SCALE;
        ci[id].label = "Print Scaled Peak";
        ci[id].func  = int2d_callback;
        ci[id].userdata      = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_PUSHBUTTON;
        ci[id].id    = INT2D_CALC;
        ci[id].label = "Re-calculate Integrals";
        ci[id].func  = int2d_callback;
        ci[id].userdata      = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));
/*
        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_PUSHBUTTON;
        ci[id].id    = INT2D_NOISE;
        ci[id].label = "Calculate RMS noise";
        ci[id].func  = int2d_callback;
        ci[id].userdata      = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));
*/
        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_PUSHBUTTON;
        ci[id].id    = INT2D_OPTIONS;
        ci[id].label = "Options";
        ci[id].func  = int2d_callback;
        ci[id].userdata      = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_OK;
        ci[id].func  = int2d_callback;
        ci[id].userdata      = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type 	= G_CHILD_CANCEL;
        ci[id].func  	= int2d_callback;
        ci[id].userdata	= (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        assert(id<28);

    }
    g_delete_object(mblk->spar[S_INT2D].obj_id);
    g_open_object(mblk->spar[S_INT2D].obj_id);
    g_select_viewport(mblk->spar[S_INT2D].vp_id);
    g_set_foreground(mblk->spar[S_INT2D].color);
    g_close_object(mblk->spar[S_INT2D].obj_id);
    g_call_object(mblk->spar[S_INT2D].obj_id);

    g_popup_container_show(popinf->cont_id);   
}

void psnd_integrate2d(MBLOCK *mblk, float x1, float x2, float y1, float y2)
{
    int jy1, jy2, jx1, jx2, jyc, jxc, nshiftx, nshifty;
    float r, small = 1.0e-6;
    SBLOCK *spar =  mblk->spar;
    CBLOCK *cpar = mblk->cpar_screen;
    DBLOCK *dat  = DAT;
    int vp  = spar[S_SCRATCH2D].vp_id,
        obj = spar[S_SCRATCH2D].obj_id,
        col = spar[S_SCRATCH2D].color;
    
    if (x2 < x1) {
        float tmp = x1;
        x1 = x2;
        x2 = tmp;
    }
    if (y2 < y1) {
        float tmp = y1;
        y1 = y2;
        y2 = tmp;
    }
    x1=max((float)cpar->ihamin,x1);
    x2=min((float)cpar->ihamax,x2);
    y1=max((float)cpar->ihbmin,y1);
    y2=min((float)cpar->ihbmax,y2);
    /*
     *         draw box
     */
    jy1=round(y1);
    jy2=round(y2);
    jx1=round(x1);
    jx2=round(x2);

    x1 = jx1 - 0.4;
    y1 = jy1 - 0.4;
    x2 = jx2 + 0.4;
    y2 = jy2 + 0.4;

    cpar->box[0] = jx1;
    cpar->box[1] = jx2;
    cpar->box[2] = jy1;
    cpar->box[3] = jy2;

    if (spar[S_SCRATCH2D].show) 
        g_append_object(obj);
    else {
        g_delete_object(obj);
        g_open_object(obj);
    }
    g_select_viewport(vp);
    g_set_foreground(col);
    g_rectangle(x1,y1,x2,y2);
    spar[S_SCRATCH2D].show = TRUE;


    if (cpar->intmod == INTBOX || 
             cpar->intmod == INTFILL || 
             cpar->intmod == INTCONVEX) {
               float level;
               char *cbuf;

               cbuf = NULL;
               jxc = 0;
               jyc = 0;
               if (cpar->intlevel < 1)
                   level = fabs(cpar->clevel[0]);
               else
                   level = -fabs(cpar->clevel[0]);

               integrate2d(dat->finfo[INFILE].ifile,
                      cpar->xdata,
                      (cpar->intmod != INTBOX),
                      jy1,
                      jy2,
                      jx1,
                      jx2,
                      &jyc,
                      &jxc,
                      0,
                      cpar->zoint,
                      &cpar->zo,
                      &level,
                      &cpar->dint,
                      (cpar->intmod == INTCONVEX),
                      &cbuf,
                      read2d,
                      (void*)mblk);

        if (cbuf != NULL) {
            int na,nb;
            nb = jy2 - jy1 + 1;
            na = jx2 - jx1 + 1;
            plot_area(jx1, jy1, na, nb, cbuf);
            free(cbuf);
        }
        r = pow(cpar->rref/max(fabs(cpar->dint),small),(1./6.));
        /*
         * If offset correction is used, the value 'zo' is
         * subtracted from the total value. This is the intesity
         * measured at the borders of the box.
         */
        if (cpar->zoint) {
            psnd_printf(mblk," OFFS A = %10.3e, R = %10.3e OFFSET = %10.3e\n", 
                cpar->dint,r,cpar->zo);
        }
        else {
            psnd_printf(mblk," FULL A = %10.3e, R = %10.3e\n", 
                cpar->dint,r);
        }
        cpar->centx = jx1 + jxc;
        cpar->centy = jy1 + jyc;
    }

    if (cpar->pribox) 
        psnd_printf(mblk," BOX (left,right,bottom,top) = %d, %d, %d, %d\n", 
                     jx1, jx2, jy1, jy2);

    g_close_object(obj);
    g_call_object(obj);
    g_plotall();
}

void psnd_add2d_peak(int sign, void *userdata, 
                     float xmin, float xmax, float ymin, float ymax ) 
{
    CBLOCK *cpar;
    
    cpar = (CBLOCK*) userdata;
    cpar->box[0] = xmin;
    cpar->box[1] = xmax;
    cpar->box[2] = ymin;
    cpar->box[3] = ymax;
    cpar->centx = 0;
    cpar->centy = 0;
    set_int2d(cpar, "", (float) sign);
}

#ifdef meuk

#define TESTSIZE	10
#define TESTBUFSIZE	((2 * TESTSIZE + 1) * (2 * TESTSIZE + 1))
float peaktest2d(int idev, float *xx, 
                 int ilowb, int ihighb, 
                 int ilowa, int ihigha,
                 int ilowb2, int ihighb2, 
                 int ilowa2, int ihigha2,
                 int (*read2d)(void*,int,float*,int,int),
                 void *userdata)
{
    int i, j, k1, k2;
    int ial,iah,ibl,ibh;
    float pmax1, pmax2;
    int xmax1, ymax1, xmax2, ymax2;
    float buf1[TESTBUFSIZE],buf2[TESTBUFSIZE];
    float xlen, ylen, prod;
    int sizex, sizey;

    pmax1 = 0.0;
    xmax1 = ilowa;
    ymax1 = ilowb;
    for (i=ilowb; i<=ihighb; i++) {
        read2d(userdata, idev, xx, ihigha, i);
        for (j=ilowa-1; j<ihigha; j++) {
            if (xx[j] > pmax1) {
                pmax1 = xx[j];
                ymax1 = i;
                xmax1 = j;
            }
        }
    }
    sizex = TESTSIZE;
    sizey = TESTSIZE;
    sizex = min(sizex, xmax1 - ilowa);
    sizex = min(sizex, -xmax1 + ihigha);
    sizey = min(sizey, ymax1 - ilowb);
    sizey = min(sizey, -ymax1 + ihighb);

    pmax2 = 0.0;
    xmax2 = ilowa2;
    ymax2 = ilowb2;
    for (i=ilowb2; i<=ihighb2; i++) {
        read2d(userdata, idev, xx, ihigha2, i);
        for (j=ilowa2-1; j<ihigha2; j++) {
            if (xx[j] > pmax2) {
                pmax2 = xx[j];
                ymax2 = i;
                xmax2 = j;
            }
        }
    }
    sizex = min(sizex, xmax2 - ilowa2);
    sizex = min(sizex, -xmax2 + ihigha2);
    sizey = min(sizey, ymax2 - ilowb2);
    sizey = min(sizey, -ymax2 + ihighb2);

    ial = max(ilowa,  xmax1-sizex);
    iah = min(ihigha, xmax1+sizex);
    ibl = max(ilowb,  ymax1-sizey);
    ibh = min(ihighb, ymax1+sizey);

    k1 = 0;
    for (i=ibl; i<=ibh; i++) {
        read2d(userdata, idev, xx, iah, i);
        for (j=ial-1; j<iah; j++) {
            buf1[k1++] = xx[j];
        }
    }

    ial = max(ilowa2,  xmax2-sizex);
    iah = min(ihigha2, xmax2+sizex);
    ibl = max(ilowb2,  ymax2-sizey);
    ibh = min(ihighb2, ymax2+sizey);

    k2 = 0;
    for (i=ibl; i<=ibh; i++) {
        read2d(userdata, idev, xx, iah, i);
        for (j=ial-1; j<iah; j++) {
            buf2[k2++] = xx[j];
        }
    }

    prod = 0.0;
    if (k1 > 0 && sizex > 0 && sizey > 0) { 
        xlen = 0.0;
        ylen = 0.0;
        prod = 0.0;
        for (i=0;i<k1;i++) {
            xlen += buf1[i] * buf1[i];
            ylen += buf2[i] * buf2[i];
            prod += buf1[i] * buf2[i];
        }
        prod = prod/(sqrt(xlen)*sqrt(ylen));
    }
 printf("%d %g\n", k1, prod);
    return prod;
}

#endif

void psnd_peakpick_area(MBLOCK *mblk, float x1, float y1, float x2, float y2)
{
    G_POPUP_CHILDINFO childinfo, *ci;
    CBLOCK *cpar;
    int i,j, i_count_save, i1,i2,j1,j2;
    POPUP_INFO *popinf = mblk->popinf + POP_INT2D;

    if (x1 == x2 || y1 == y2)
        return;

    if (!popinf->visible)
        psnd_popup_int2d(mblk);
     /* return; */

    i1 = (int) min(x1,x2);
    i2 = (int) max(x1,x2);
    j1 = (int) min(y1,y2);
    j2 = (int) max(y1,y2);
    cpar = mblk->cpar_screen;
    ci = &childinfo;
    g_popup_init_info(ci);
    ci->cont_id = popinf->cont_id;
    ci->type = G_CHILD_PUSHBUTTON;
    ci->func  = int2d_callback;
    ci->userdata      = (void*) mblk;
    
    /*
     * Delete all boxes inside range i1,i2,j1,j2
     */
    if (cpar->ppmode == PEAKPICK_REMOVE) {
        /*
         * Mark boxes
         */
        for (i=1;i<cpar->i_count;i++) {
            if (cpar->int2d_queue[i].box[0] >= i1 &&
                cpar->int2d_queue[i].box[0] <= i2 &&
                cpar->int2d_queue[i].box[1] >= i1 &&
                cpar->int2d_queue[i].box[1] <= i2 &&
                cpar->int2d_queue[i].box[2] >= j1 &&
                cpar->int2d_queue[i].box[2] <= j2 &&
                cpar->int2d_queue[i].box[3] >= j1 &&
                cpar->int2d_queue[i].box[3] <= j2 )
            cpar->int2d_queue[i].centx[0] = MARK_FOR_REMOVAL;
        }
        /*
         * Remove marked boxes
         */
        for (i=1,j=1;i<cpar->i_count;i++) {
            if (cpar->int2d_queue[i].centx[0] == MARK_FOR_REMOVAL) {
                g_popup_remove_item(ci->cont_id, INT2D_LISTBOX,
                        j);
                continue;
            }
            if (j < i) {
                memcpy(&(cpar->int2d_queue[j]), 
                       &(cpar->int2d_queue[i]), 
                       sizeof(INT2DINFO));
            }
            j++;
        }
        if (j != cpar->i_count) {
            cpar->i_count = j;
            cpar->i_select= 0;
            ci->id = INT2D_PLOTALL;
            int2d_callback(ci);
        }
    }
    /*
     * PeakPick inside range i1,i2,j1,j2
     */
    else if (cpar->ppmode == PEAKPICK_FIND) {
        float clevel = fabs(cpar->clevel[0]);
        i_count_save = cpar->i_count;
        if (cpar->intlevel == 0)
            psnd_peakpick2d(mblk, cpar, 
                         cpar->xcpr,
                         cpar->maxcpr,
                         clevel,
                         DAT->finfo[INFILE].ifile,
                         cpar->xdata,
                         cpar->maxdat,
                         i1,
                         i2,
                         j1,
                         j2,
                         cpar->iadiv,
                         cpar->ibdiv);           
        else
            psnd_peakpick2d(mblk, cpar, 
                         cpar->xcpr,
                         cpar->maxcpr,
                         -clevel,
                         DAT->finfo[INFILE].ifile,
                         cpar->xdata,
                         cpar->maxdat,
                         i1,
                         i2,
                         j1,
                         j2,
                         cpar->iadiv,
                         cpar->ibdiv);           
        ci->id = INT2D_CALC;
        int2d_callback(ci);
        ci->id = INT2D_PLOTALL;
        int2d_callback(ci);
        for (i=i_count_save;i<cpar->i_count;i++) {
            g_popup_append_item(ci->cont_id, INT2D_LISTBOX,
                cpar->int2d_queue[i].label);
                
/*

if (i_count_save> 2) {
int n;
for (n=1;n<3;n++)
    peaktest2d(DAT->finfo[INFILE].ifile,
                                   cpar->xdata,
                                   cpar->int2d_queue[i].box[2],
                                   cpar->int2d_queue[i].box[3],
                                   cpar->int2d_queue[i].box[0],
                                   cpar->int2d_queue[i].box[1],
                                   cpar->int2d_queue[n].box[2],
                                   cpar->int2d_queue[n].box[3],
                                   cpar->int2d_queue[n].box[0],
                                   cpar->int2d_queue[n].box[1],
                                   read2d,
                                   (void*)mblk);
}
*/
        }
    }
    /*
     * Merge all peaks inside range i1,i2,j1,j2
     */
    else if (cpar->ppmode == PEAKPICK_MERGE) {
        int box[4] = {0,0,0,0};
        int select_box = -1, select_count = 0;
        /*
         * Mark boxes
         */
        for (i=1;i<cpar->i_count;i++) {
            if (cpar->int2d_queue[i].box[0] >= i1 &&
                cpar->int2d_queue[i].box[0] <= i2 &&
                cpar->int2d_queue[i].box[1] >= i1 &&
                cpar->int2d_queue[i].box[1] <= i2 &&
                cpar->int2d_queue[i].box[2] >= j1 &&
                cpar->int2d_queue[i].box[2] <= j2 &&
                cpar->int2d_queue[i].box[3] >= j1 &&
                cpar->int2d_queue[i].box[3] <= j2 ) {
                if (select_box == -1) {
                    select_box = i;
                    box[0] = cpar->int2d_queue[i].box[0];
                    box[1] = cpar->int2d_queue[i].box[1];
                    box[2] = cpar->int2d_queue[i].box[2];
                    box[3] = cpar->int2d_queue[i].box[3];
                }
                else {
                    int k, npeaks;
                    box[0] = min(box[0],cpar->int2d_queue[i].box[0]);
                    box[1] = max(box[1],cpar->int2d_queue[i].box[1]);
                    box[2] = min(box[2],cpar->int2d_queue[i].box[2]);
                    box[3] = max(box[3],cpar->int2d_queue[i].box[3]);
                    npeaks = cpar->int2d_queue[select_box].npeaks;
                    for (k=0;k<cpar->int2d_queue[i].npeaks;k++) {
                        int n, ok = TRUE;
                        if (npeaks >= INT2DCENTMAX) 
                            break;
                        for (n=0;n<npeaks;n++) {
                            if (cpar->int2d_queue[select_box].centx[n] == 
                                    cpar->int2d_queue[i].centx[k] &&
                                cpar->int2d_queue[select_box].centy[n] == 
                                    cpar->int2d_queue[i].centy[k]) {
                                ok = FALSE;
                                break;
                                
                            }
                        }
                        if (ok) {
                            cpar->int2d_queue[select_box].centx[npeaks] = 
                                cpar->int2d_queue[i].centx[k];
                            cpar->int2d_queue[select_box].centy[npeaks] = 
                                cpar->int2d_queue[i].centy[k];
                            cpar->int2d_queue[select_box].level[npeaks] = 
                                cpar->int2d_queue[i].level[k];
                            cpar->int2d_queue[select_box].npeaks++;
                            npeaks++;
                        }
                    }
                    cpar->int2d_queue[i].centx[0] = MARK_FOR_REMOVAL;
                }
                select_count++;
            }
        }
        if (select_count <= 1)
            return;
        cpar->int2d_queue[select_box].box[0] = box[0];
        cpar->int2d_queue[select_box].box[1] = box[1];
        cpar->int2d_queue[select_box].box[2] = box[2];
        cpar->int2d_queue[select_box].box[3] = box[3];
        /*
         * Remove marked boxes
         */
        for (i=1,j=1;i<cpar->i_count;i++) {
            if (cpar->int2d_queue[i].centx[0] == MARK_FOR_REMOVAL) {
                g_popup_remove_item(ci->cont_id, INT2D_LISTBOX,
                        j);
                continue;
            }
            if (j < i) {
                memcpy(&(cpar->int2d_queue[j]), 
                       &(cpar->int2d_queue[i]), 
                       sizeof(INT2DINFO));
            }
            j++;
        }
        if (j != cpar->i_count) {
            cpar->i_count = j;
            cpar->i_select= 0;
            ci->id = INT2D_CALC;
            int2d_callback(ci);
            ci->id = INT2D_PLOTALL;
            int2d_callback(ci);
        }
    }
}


