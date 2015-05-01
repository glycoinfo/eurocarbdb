/********************************************************************/
/*                             psnd_block.c                         */
/*                                                                  */
/* 1997, Albert van Kuik                                            */
/********************************************************************/
#include <stdio.h>
#include <stdlib.h>
#include <ctype.h>
#include <string.h>
#include <stdarg.h>
#include <math.h>
#include <assert.h>
#include "genplot.h"
#include "nmrtool.h"
#include "psnd.h"



int psnd_popup_block_select(MBLOCK *mblk, int win_id, int block_id)
{
    int min_max_dec_step[] = { 1, MAXBLK, 0, 1};
    int cont_id;
    G_POPUP_CHILDINFO ci[20];
    int id=0;


    min_max_dec_step[1] = mblk->info->max_block;
    cont_id = g_popup_container_open(win_id, "Select", 
            G_POPUP_WAIT );
    popup_add_spin(cont_id, &(ci[id]), id,
                   "New block : ", block_id, min_max_dec_step);
    if (g_popup_container_show(cont_id)) {
        return ci[0].item;
    }
    return FALSE;
}

int psnd_popup_numblocks(MBLOCK *mblk)
{
    int min_max_dec_step[] = { NUMBLK, MAXBLK, 0, 1};
    int cont_id;
    G_POPUP_CHILDINFO ci[20];
    char *label;
    int id=0;

    min_max_dec_step[0] = mblk->info->max_block;
    cont_id = g_popup_container_open(mblk->info->win_id, "Add blocks", 
            G_POPUP_WAIT );
    label = psnd_sprintf_temp("Set number of blocks (%d-%d) : ", 
                              mblk->info->max_block, MAXBLK);
    popup_add_spin(cont_id, &(ci[id]), id,
                   label, 0, min_max_dec_step);
    if (g_popup_container_show(cont_id)) {
        return ci[0].item;
    }
    return FALSE;
}


#define SWAP		0
#define TRANSFER	1
#define ADD		2
#define MULTI		3
#define DIVIDE		4
#define NONE		5

void psnd_copyparam(MBLOCK *mblk, int to, int from)
{
    int i;
    char *p;
    /*
     * Copy Parameters 
     */
    int j = to, k = from;
    
    memcpy(mblk->par[j], mblk->par[k] ,sizeof(PBLOCK)*MAX_DIM);
    mblk->dat[j]->ityp		= mblk->dat[k]->ityp;
    mblk->dat[j]->ndim		= mblk->dat[k]->ndim;
    mblk->dat[j]->sw		= mblk->dat[k]->sw;
    mblk->dat[j]->irc		= mblk->dat[k]->irc;
    mblk->dat[j]->iaqdir	= mblk->dat[k]->iaqdir;
    mblk->dat[j]->nextdr	= mblk->dat[k]->nextdr;

    mblk->dat[j]->isize		= mblk->dat[k]->isize;
    mblk->dat[j]->npar		= mblk->dat[k]->npar;
    mblk->dat[j]->nt0par	= mblk->dat[k]->nt0par;
    mblk->dat[j]->ntnpar	= mblk->dat[k]->ntnpar;

    if (!mblk->info->version)
        memcpy(mblk->dat[j]->xpar,     mblk->dat[k]->xpar,sizeof(float) * MAXPAR);
    mblk->dat[j]->ylimit[0]	= mblk->dat[k]->ylimit[0];
    mblk->dat[j]->ylimit[1]	= mblk->dat[k]->ylimit[1];

    memcpy(mblk->dat[j]->access, mblk->dat[k]->access, sizeof(int) * MAX_DIM);
    memcpy(mblk->dat[j]->nsizo,  mblk->dat[k]->nsizo, sizeof(int) * MAX_DIM);
    memcpy(mblk->dat[j]->cmplxo, mblk->dat[k]->cmplxo, sizeof(int) * MAX_DIM);

    for (i=0;i<MAX_DIM;i++) {
        mblk->dat[j]->pars[i]    = (mblk->par[j])+(mblk->dat[j]->access[i]-1);
    }
    psnd_compose_prompt(NULL, mblk->dat[j]);
    psnd_init_undo(mblk->dat[j]);
    psnd_init_plane_undo(mblk->dat[j]);
}

#define ARRAYITEMS	6
#define OPERATIONITEMS	5
#define ARRAYITEMSPAR	(ARRAYITEMS+1)

int  psnd_blockoperations(MBLOCK *mblk, int iid)
{ 
    static float addpar[3] = {1.0, 1.0, 1.0};
    int i, ok, size, min_max_dec_step[] = { 1, MAXBLK, 0, 1};
    char *check[] = { "Real", "Imaginary", "Buffer A", "Buffer B", 
                      "Window", "Baseline", "Parameters (Copy)" } ;
    char *check2[] = {  "Parameters", "" } ;
    static int check_select[] = { 0, 0, 0, 0, 0, 0, 0, 0};
    static int check_select2[] = { 0, 0 };
    char *radio[] = { "Swap", "Transfer", "Additive Transfer",
                      "Multiply Transfer", "Divide Transfer" ," " } ;
    static int from=1, to=1, operation=TRANSFER;
    float *data_from[ARRAYITEMSPAR], *data_to[ARRAYITEMSPAR];
    static int cont_id;
    G_POPUP_CHILDINFO ci[20];
    int id;

    min_max_dec_step[1] = mblk->info->max_block;
    cont_id = g_popup_container_open(mblk->info->win_id, "Block Operations", 
                                       G_POPUP_WAIT);

    id=0;

    popup_add_spin(cont_id, &(ci[id]), id,
                       "From Block :", from, min_max_dec_step);
    
    id++;
    switch (iid) {
        case PSND_IJ:
            operation = SWAP;
            break;
        case PSND_TB:
        case PSND_TW:
        case PSND_TR:
            operation = TRANSFER;
            break;
        case PSND_AT:
            operation = ADD;
            break;
        case PSND_MX:
            operation = MULTI;
            break;
        case PSND_TP:
            check_select2[0] = 1;
            break;
    }
    popup_add_option2(cont_id, &(ci[id]), id,
                       "Operation", operation, OPERATIONITEMS, radio);

    id++;
    popup_add_spin(cont_id, &(ci[id]), id,
                       "To Block   :", to, min_max_dec_step);
    

    id++;
    g_popup_init_info(&(ci[id]));
    ci[id].type = G_CHILD_CHECKBOX;
    ci[id].id         = id;
    ci[id].title      = "Items involved";
    ci[id].frame      = TRUE;
    ci[id].item_count = ARRAYITEMS;
    ci[id].data       = check;
    ci[id].select     = check_select;
    g_popup_add_child(cont_id, &(ci[id]));
   
    id++;
    g_popup_init_info(&(ci[id]));
    ci[id].type = G_CHILD_CHECKBOX;
    ci[id].id         = id;
    ci[id].title      = "Copy";
    ci[id].frame      = TRUE;
    ci[id].item_count = 1;
    ci[id].data       = check2;
    ci[id].select     = check_select2;
    g_popup_add_child(cont_id, &(ci[id]));
   
    if (g_popup_container_show(cont_id) == 0) 
        return FALSE;
        
    id = 0;
    from =      ci[id++].item;
    operation = ci[id++].item;
    to =        ci[id++].item;
    ok = FALSE;
    for (i=0;i<ARRAYITEMSPAR;i++)  {
        check_select[i] = ci[id].select[i];
        if (check_select[i])
            ok = TRUE;
    }
    id++;
    check_select2[0] = ci[id].select[0];
    
    if (!ok)
        operation = NONE;
    else
        size = max(mblk->dat[from-1]->isize, mblk->dat[to-1]->isize);


    if (operation == TRANSFER)
        if (psnd_rvalin(mblk, " To = A * From", 1, addpar) == 0)
            return FALSE;

    if (operation == ADD)
        if (psnd_rvalin(mblk, " To = To * A + From * B", 2, addpar) == 0)
            return FALSE;

    if (operation == MULTI) {
        if (psnd_rvalin(mblk, " To = To * A * From * B/Norm", 3, addpar) == 0)
            return FALSE;
        if (addpar[2] == 0) {
            addpar[2] = 1.0;
            psnd_printf(mblk,"Divide by 0 error\n");
            return FALSE;
        }
    }

    if (operation == DIVIDE) {
        if (psnd_rvalin(mblk, " To = To * A / From * B", 2, addpar) == 0)
            return FALSE;
        if (addpar[1] == 0) {
            addpar[1] = 1.0;
            psnd_printf(mblk,"Divide by 0 error\n");
            return FALSE;
        }
    }
/*
printf("%d %d %d \n", from, operation, to);
  */
    data_from[0] = mblk->dat[from-1]->xreal;
    data_from[1] = mblk->dat[from-1]->ximag;
    data_from[2] = mblk->dat[from-1]->xbufr1;
    data_from[3] = mblk->dat[from-1]->xbufr2;
    data_from[4] = mblk->dat[from-1]->window;
    data_from[5] = mblk->dat[from-1]->baseln;
    data_to[0] = mblk->dat[to-1]->xreal;
    data_to[1] = mblk->dat[to-1]->ximag;
    data_to[2] = mblk->dat[to-1]->xbufr1;
    data_to[3] = mblk->dat[to-1]->xbufr2;
    data_to[4] = mblk->dat[to-1]->window;
    data_to[5] = mblk->dat[to-1]->baseln;
    if (operation == SWAP) {
        int j = to-1, k = from-1;
        float *tmp;
        if (j==k)
            return FALSE;
        id=0;
        if (check_select[id]) {
            tmp  = mblk->dat[j]->xreal;
            mblk->dat[j]->xreal = mblk->dat[k]->xreal;
            mblk->dat[k]->xreal = tmp;
        }
        id++;
        if (check_select[id]) {
            tmp  = mblk->dat[j]->ximag;
            mblk->dat[j]->ximag = mblk->dat[k]->ximag;
            mblk->dat[k]->ximag = tmp;
        }
        id++;
        if (check_select[id]) {
            tmp  = mblk->dat[j]->xbufr1;
            mblk->dat[j]->xbufr1 = mblk->dat[k]->xbufr1;
            mblk->dat[k]->xbufr1 = tmp;
        }
        id++;
        if (check_select[id]) {
            tmp  = mblk->dat[j]->xbufr2;
            mblk->dat[j]->xbufr2 = mblk->dat[k]->xbufr2;
            mblk->dat[k]->xbufr2 = tmp;
        }
        id++;
        if (check_select[id]) {
            tmp  = mblk->dat[j]->window;
            mblk->dat[j]->window = mblk->dat[k]->window;
            mblk->dat[k]->window = tmp;
        }
        id++;
        if (check_select[id]) {
            tmp  = mblk->dat[j]->baseln;
            mblk->dat[j]->baseln = mblk->dat[k]->baseln;
            mblk->dat[k]->baseln = tmp;
        }
    }
    else if (operation == TRANSFER) {
        for (id=0;id < ARRAYITEMS;id++) {
            if (check_select[id]) 
                for (i=0;i< size;i++) 
                    data_to[id][i] = data_from[id][i] * addpar[0];
        }
        mblk->dat[to-1]->isize = size;
    }
    else if (operation == ADD) {
        for (id=0;id < ARRAYITEMS;id++) {
            if (check_select[id]) 
                for (i=0;i< size;i++) 
                    data_to[id][i] = data_to[id][i] * addpar[0] +
                                  data_from[id][i] * addpar[1];
        }
        mblk->dat[to-1]->isize = size;
    }
    else if (operation == MULTI) {
        float fac = addpar[0] * addpar[1] / addpar[2];
        for (id=0;id < ARRAYITEMS;id++) {
            if (check_select[id]) 
                for (i=0;i< size;i++) 
                    data_to[id][i] = fac * data_to[id][i] * data_from[id][i];
        }
        mblk->dat[to-1]->isize = size;
    }
    else if (operation == DIVIDE) {
        float fac = addpar[0] / addpar[1];
        for (id=0;id < ARRAYITEMS;id++) {
            if (check_select[id]) 
                for (i=0;i< size;i++) {
                    if (data_from[id] == 0) {
                        psnd_printf(mblk,"Divide by 0 error\n");
                        return FALSE;
                    }
                    data_to[id][i] = fac * data_to[id][i] / data_from[id][i];
                }
        }
        mblk->dat[to-1]->isize = size;
    }
    /*
     * Copy Parameters 
     */
    if (check_select2[0] && to != from) 
        psnd_copyparam(mblk, to-1, from-1);

    return TRUE;
}



void psnd_arraymove(MBLOCK *mblk)
{
    int start=1, i,j;
    float *p1, *p2;
    
    i = psnd_popup_select_array(mblk->info->win_id, 1, "Copy Array", 
                                 "From : ", FALSE);
    switch (i) {
    case 1: 
        p1 = DAT->xreal;
        break;
    case 2: 
        p1 = DAT->ximag;
        break;
    case 3: 
        p1 = DAT->xbufr1;
        break;
    case 4: 
        p1 = DAT->xbufr2;
        break;
    case 5: 
        p1 = DAT->window;
        break;
    case 6: 
        p1 = DAT->baseln;
        break;
    default:
        return;
    }
    j = psnd_popup_select_array(mblk->info->win_id, i, "Copy Array", "To : ", FALSE);
    switch (j) {
    case 1: 
        p2 = DAT->xreal;
        break;
    case 2: 
        p2 = DAT->ximag;
        break;
    case 3: 
        p2 = DAT->xbufr1;
        break;
    case 4: 
        p2 = DAT->xbufr2;
        break;
    case 5: 
        p2 = DAT->window;
        break;
    case 6: 
        p2 = DAT->baseln;
        break;
    default:
        return;
    }
    if (p1 == p2)
        return;
    memcpy(p2,p1,DAT->isize * sizeof(float));

}


/*****/

void psnd_arrayfill(int argc, char *argv[], int id, float value, PBLOCK *par, DBLOCK *dat)
{
    int left = 1, right = dat->isize; 
    float *array;

    if (argc > 1) 
        value = psnd_scan_float(argv[1]);
    switch (id) {
        case 0:
            array = dat->xreal;
            break;
        case 1:
            array = dat->ximag;
            break;
        case 2:
            array = dat->xbufr1;
            break;
        case 3:
            array = dat->xbufr2;
            break;
        case 4:
            array = dat->window;
            break;
        case 5:
            array = dat->baseln;
            break;
        default:
            return;
    }
            
    xxfilv(array, left, right, value);

}

void psnd_arrayreverse(PBLOCK *par, DBLOCK *dat)
{

    int size = dat->isize; 
            
    if (par->irever == NOREV)
        par->irever = REVARR;
    invrse(dat->xreal, size);
    invrse(dat->ximag, size);
}

void psnd_arraynegate(DBLOCK *dat)
{

    int start = 1, stop = dat->isize; 
    float value = -1.0;
            
    xxmulv(dat->xreal,
            start,
            stop,
            value);
    xxmulv(dat->ximag,
            start,
            stop,
            value);

}

/*********************************************************************
*  shift arrays
*/

/*
 * Perform a ROL (left rotate) or a ROR (right rotate)
 */
int psnd_rotate_array(int argc, char *argv[], int sign, int value, DBLOCK *dat)
{
    int size = dat->isize; 

    if (argc > 0) {
        if (tolower(argv[0][0])=='r' && tolower(argv[0][1]) == 'o') {
            /*
             * ROR
             */
            if (tolower(argv[0][2])=='r')
                sign = -1;
            /*
             * ROL
             */
            else if (tolower(argv[0][2])=='l')
                sign = 1;
            else
                return FALSE;
        }
        else
            return FALSE;
    }

    if (argc > 1) 
        value = psnd_scan_integer(argv[1]);
    if (value == 0)
        return FALSE;

    value *= sign;

    xxrotate(dat->xreal, 1, size, value);
    xxrotate(dat->ximag, 1, size, value);
    return TRUE;
}

/*
 * Perform a LS (left shift) or a RS (right shift)
 */
int psnd_shift_array(int argc, char *argv[], int sign, int value, DBLOCK *dat)
{
    int size = dat->isize; 

    if (argc > 0) {
        /*
         * RS
         */
        if (tolower(argv[0][0])=='r' && tolower(argv[0][1]) == 's')
            sign = -1;
        /*
         * LS
         */
        else if (tolower(argv[0][0])=='l' && tolower(argv[0][1]) == 's')
            sign = 1;
        else
            return FALSE;
    }

    if (argc > 1) 
        value = psnd_scan_integer(argv[1]);
    if (value == 0)
        return FALSE;

    value *= sign;

    xxshfn(dat->xreal, 1, size, value, size);
    xxshfn(dat->ximag, 1, size, value, size);
    return TRUE;
}


int psnd_arrayclip(MBLOCK *mblk, int argc, char *argv[], int start, int stop)
{

    int size,istart,istep;
    char *string;
    DBLOCK *dat = DAT;
    PBLOCK *par = PAR;

    size   = dat->isize;
#ifdef oldold
    if (argc != 0) {
        start  = par->icstrt;
        stop   = par->icstop;
    }  
    else {
        if (stop <= start)
            return FALSE;
    }
#endif
    if (start == 0)
        start = 1;
    if (stop == 0)
        stop = size;
    if (argc != 0) {
        if (argc >= 2)
            start = psnd_scan_integer(argv[1]);
        else {
            string = psnd_sprintf_temp(" continue start (%d-%d)?", start, stop);
            if (psnd_ivalin(mblk, string,1, &start) == 0)
                return FALSE;
        }
        start  = max(1,start);
        start  = min(size, start);
        if (argc >= 3)
            stop = psnd_scan_integer(argv[2]);
        else {
            string = psnd_sprintf_temp(" continue stop (%d-%d)?", start, stop);
            if (psnd_ivalin(mblk, string,1, &stop) == 0)
                return FALSE;
        }
    }
    else {
        start  = max(1,start);
        start  = min(size, start);
    }
    stop   = max(start, stop);
    stop   = min(size, stop);
#ifdef oldold
    if (argc != 0) {
        par->icstrt = start;
        par->icstop = stop;
    }
    
#endif

    psnd_set_datasize(mblk, stop-start+1, FALSE, dat);
    istep = start - 1;
    istart = 1;
    xxshfn(dat->xreal,
           istart,
           dat->isize,
           istep,
           mblk->info->block_size);
    xxshfn(dat->ximag,
           istart,
           dat->isize,
           istep, 
           mblk->info->block_size);
   return TRUE;
}

void psnd_arrayaverage(DBLOCK *dat)
{
    int i;

    dat->pars[0]->iphase = AVPHA;
    for (i=0;i<dat->isize;i++)
        dat->xreal[i] = 
            sqrt(dat->xreal[i] * dat->xreal[i] +
                 dat->ximag[i] * dat->ximag[i]);
}

void psnd_arraypower(DBLOCK *dat)
{
    int i;
    double biggest=0.0;

    dat->pars[0]->iphase = PSPHA;
    for (i=0;i<dat->isize;i++) {
        dat->xreal[i] = 
            dat->xreal[i] * dat->xreal[i] +
            dat->ximag[i] * dat->ximag[i];
        if (dat->xreal[i] > biggest)
            biggest = dat->xreal[i];
    }
    if (biggest == 0)
        return;
    /*
     * Scale back
     */
    biggest = 1.0/sqrt(biggest);
    for (i=0;i<dat->isize;i++) 
        dat->xreal[i] *= biggest; 
}

typedef enum {
    ARRAY_OPERATION_ID,
    ARRAY_SHIFT_ID,
    ARRAY_START_ID,
    ARRAY_STOP_ID
} array_ids;

static void array_callback(G_POPUP_CHILDINFO *ci)
{
    int value, enable_shift, enable_start, enable_stop;

    switch (ci->id) {
        case ARRAY_OPERATION_ID:
            value = ci->item;
            enable_shift = enable_start = enable_stop = FALSE;
            switch (value)  {
                case 0: 
                    enable_start = enable_stop = TRUE;
                    break;

                case 1:
                case 2:
                case 3:
                case 4:
                    enable_shift = TRUE;
                    break;
            }
            g_popup_enable_item(ci->cont_id, ARRAY_SHIFT_ID, enable_shift);
            g_popup_enable_item(ci->cont_id, ARRAY_START_ID, enable_start);
            g_popup_enable_item(ci->cont_id, ARRAY_STOP_ID,  enable_stop);
            break;
    }
}

#define ARRAY_OPERATIONITEMS 7
#define MAXID	10
int psnd_array_popup(MBLOCK *mblk, int mode)
{
    G_POPUP_CHILDINFO ci[MAXID];
    int id;
    char *radio[ARRAY_OPERATIONITEMS+1] = 
                    { "Clip", "Left Shift", "Right Shift",
                      "Rotate Left", "Rotate Right",
                      "Reverse", "Negate", " " } ;
    int value, disable_shift, disable_start, disable_stop;
    int size,start,stop;
    char *label;
    int cont_id;
    static int operation, ival;

    switch (mode) {
        /* throw away part of real and imag array */
        case PSND_IC:
            operation = 0;
            break;
        /* left shift complex array */
        case PSND_LS: 
            operation = 1;
            break;
        /* right shift complex array */
        case PSND_RS: 
            operation = 2;
            break;
        /* left rotate complex array */
        case PSND_ROL: 
            operation = 3;
            break;
        /* right rotate complex array */
        case PSND_ROR: 
            operation = 4;
            break;
        /* reverse real and imag array */
        case PSND_RV:
            operation = 5;
            break;
        /* negate real and imag array */
        case PSND_NM:
            operation = 6;
            break;
    }
    id = -1;
    size   = DAT->isize;
    start  = PAR->icstrt;
    stop   = PAR->icstop;
    if (start == 0)
        start = 1;
    if (stop == 0)
        stop = size;


    value = operation;
    disable_shift = disable_start = disable_stop = TRUE;
    switch (value)  {
        case 0: 
            disable_start = disable_stop = FALSE;
            break;

        case 1:
        case 2:
        case 3:
        case 4:
            disable_shift = FALSE;
            break;
    }

    cont_id = g_popup_container_open(mblk->info->win_id, 
                         "Array modification",  G_POPUP_WAIT);

    id++;

    g_popup_init_info(ci);
    ci[id].type  = G_CHILD_OPTIONMENU;
    ci[id].id    = ARRAY_OPERATION_ID;
    ci[id].item_count = ARRAY_OPERATIONITEMS;
    ci[id].item = operation;
    ci[id].data = radio;
    ci[id].horizontal = TRUE;
    ci[id].label = "Operation";
    ci[id].frame = TRUE;
    ci[id].func  = array_callback;
    g_popup_add_child(cont_id, ci);

    id++;
    label = psnd_sprintf_temp("%d", ival);
    popup_add_text3(cont_id, &(ci[id]), ARRAY_SHIFT_ID, 
                    label, "number of points to shift :", disable_shift);
    id++;
    label = psnd_sprintf_temp("%d", start);
    popup_add_text3(cont_id, &(ci[id]), ARRAY_START_ID, 
                    label, "continue start :", disable_start);
    id++;
    label = psnd_sprintf_temp("%d", stop);
    popup_add_text3(cont_id, &(ci[id]), ARRAY_STOP_ID,     
                    label, "continue stop  :", disable_stop);

    if (g_popup_container_show(cont_id) == 0) 
        return FALSE;
        
    id = 0;
    operation = ci[id++].item;
    sscanf(ci[id++].label, "%d", &ival);
    sscanf(ci[id++].label, "%d", &start);
    sscanf(ci[id++].label, "%d", &stop);
    switch (operation) {
        case 0:
            return psnd_arrayclip(mblk, 0, NULL, start, stop);
            break;
        case 1:
            return psnd_shift_array(0, NULL, 1, ival, DAT);
            break;
        case 2:
            return psnd_shift_array(0, NULL, -1, ival, DAT);
            break;
        case 3:
            return psnd_rotate_array(0, NULL, 1, ival, DAT);
            break;
        case 4:
            return psnd_rotate_array(0, NULL, -1, ival, DAT);
            break;
        case 5:
            psnd_arrayreverse(PAR, DAT);
            break;
        case 6:
            psnd_arraynegate(DAT);
            break;
    }
    return TRUE;
}

/************************/

#define ARRAY_FILLOPERATIONITEMS 6
#define MAXID	10
int psnd_arrayfill_popup(MBLOCK *mblk, int mode)
{
    G_POPUP_CHILDINFO ci[MAXID];
    int id, cont_id, i, ok;
    char *check[ARRAY_FILLOPERATIONITEMS+1] = 
                    { "Real", "Imaginary", "Buffer A", "Buffer B", 
                      "Window", "Baseline", "" } ;
    char *label;
    static int check_select[] = { 0, 0, 0, 0, 0, 0, 0, 0};
    static float  fval;

    for (i=0;i<ARRAY_FILLOPERATIONITEMS;i++)
        check_select[i] = 0;
    switch (mode) {

        case PSND_ZE:
            check_select[0] = TRUE;
            check_select[1] = TRUE;
            fval = 0.0;
            break;
        case PSND_ZR:
            check_select[0] = TRUE;
            fval = 0.0;
            break;
        case PSND_ZI:
            check_select[1] = TRUE;
            fval = 0.0;
            break;
        case PSND_Z1:
            check_select[0] = TRUE;
            check_select[1] = TRUE;
            fval = 1.0;
            break;
        case PSND_ZA:
            check_select[2] = TRUE;
            break;
        case PSND_ZB:
            check_select[3] = TRUE;
            break;
        case PSND_ZW:
            check_select[4] = TRUE;
            break;
        case PSND_ZL:
            check_select[5] = TRUE;
            break;

    }
    id=-1;
    cont_id = g_popup_container_open(mblk->info->win_id, 
                         "Array filling",  G_POPUP_WAIT);

    id++;
    g_popup_init_info(&(ci[id]));
    ci[id].type = G_CHILD_CHECKBOX;
    ci[id].id         = id;
    ci[id].title      = "Items involved";
    ci[id].frame      = TRUE;
    ci[id].item_count = ARRAY_FILLOPERATIONITEMS;
    ci[id].data       = check;
    ci[id].select     = check_select;
    g_popup_add_child(cont_id, &(ci[id]));
   
    id++;
    label = psnd_sprintf_temp("%g", fval);
    popup_add_text2(cont_id, &(ci[id]), ARRAY_SHIFT_ID, 
                    label, "value :");

    if (g_popup_container_show(cont_id) == 0) 
        return FALSE;

    id = 0;
    ok = FALSE;
    for (i=0;i<ARRAY_FILLOPERATIONITEMS;i++)  {
        check_select[i] = ci[id].select[i];
        if (check_select[i])
            ok = TRUE;
    }
    if (!ok)
        return FALSE;
    id++;
    sscanf(ci[id].label, "%g", &fval);

    for (i=0;i<ARRAY_FILLOPERATIONITEMS;i++)  {
        if (check_select[i])
            psnd_arrayfill(0, NULL, i, fval, PAR, DAT);
    }
    return TRUE;
}


