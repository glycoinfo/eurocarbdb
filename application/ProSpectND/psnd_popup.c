/********************************************************************/
/*                              psnd_popup.c                        */
/*                                                                  */
/* 1997, Albert van Kuik                                            */
/********************************************************************/
#include <stdio.h>
#include <stdlib.h>
#include <ctype.h>
#include <string.h>
#include <stdarg.h>
#include <math.h>
#include "genplot.h"
#include "psnd.h"

void popup_add_spin(int cont_id, G_POPUP_CHILDINFO *ci, int id, 
                           char *label, int select, int *min_max_dec_step)
{
    g_popup_init_info(ci);
    ci->type = G_CHILD_SPINBOX;
    ci->id         = id;
    ci->frame      = TRUE;
    ci->item       = select;
    ci->items_visible = 5;
    ci->label      = label;
    ci->select     = min_max_dec_step;
    g_popup_add_child(cont_id, ci);

}

void popup_add_label(int cont_id, G_POPUP_CHILDINFO *ci, char *label)
{
    g_popup_init_info(ci);
    ci->type  = G_CHILD_LABEL;
    ci->id    = 0;
    ci->label = label;
    g_popup_add_child(cont_id, ci);
}

void popup_add_separator(int cont_id, G_POPUP_CHILDINFO *ci)
{
    g_popup_init_info(ci);
    ci->type  = G_CHILD_SEPARATOR;
    ci->id    = 0;
    g_popup_add_child(cont_id, ci);
}

void popup_add_text(int cont_id, G_POPUP_CHILDINFO *ci, int id, 
                           char *label)
{
    g_popup_init_info(ci);
    ci->type  = G_CHILD_TEXTBOX;
    ci->id    = id;
    ci->item_count = 40;
    ci->items_visible = 20;
    ci->label = label;
    g_popup_add_child(cont_id, ci);
}

void popup_add_text2(int cont_id, G_POPUP_CHILDINFO *ci, int id, 
                           char *label, char *title)
{
    g_popup_init_info(ci);
    ci->type  = G_CHILD_TEXTBOX;
    ci->id    = id;
    ci->item_count = 40;
    ci->items_visible = 20;
    ci->label = label;
    ci->frame = TRUE;
    ci->title = title;
    g_popup_add_child(cont_id, ci);
}

void popup_add_text3(int cont_id, G_POPUP_CHILDINFO *ci, int id, 
                           char *label, char *title, int disabled)
{
    g_popup_init_info(ci);
    ci->type  = G_CHILD_TEXTBOX;
    ci->id    = id;
    ci->item_count = 40;
    ci->items_visible = 20;
    ci->label = label;
    ci->frame = TRUE;
    ci->title = title;
    ci->disabled = disabled;
    g_popup_add_child(cont_id, ci);
}


void popup_add_option(int cont_id, G_POPUP_CHILDINFO *ci, int id, 
                           char *label, int select, int item_count,
                           char **item_labels)
{
    g_popup_init_info(ci);
    ci->type  = G_CHILD_OPTIONMENU;
    ci->id    = id;
    ci->item_count = item_count;
    ci->item = select;
    ci->data = item_labels;
    ci->horizontal = TRUE;
    ci->label = label;
    g_popup_add_child(cont_id, ci);
}

void popup_add_option2(int cont_id, G_POPUP_CHILDINFO *ci, int id, 
                           char *label, int select, int item_count,
                           char **item_labels)
{
    g_popup_init_info(ci);
    ci->type  = G_CHILD_OPTIONMENU;
    ci->id    = id;
    ci->item_count = item_count;
    ci->item = select;
    ci->data = item_labels;
    ci->horizontal = TRUE;
    ci->label = label;
    ci->frame = TRUE;
    g_popup_add_child(cont_id, ci);
}


int popup_spin_double(int win_id, char *title, int *val1, char *label1, 
    int *val2, char *label2, int min, int max)
{
    static int min_max_dec_step1[] = { 0, 0, 0, 1};
    static int min_max_dec_step2[] = { 0, 0, 0, 1};
    static int cont_id;
    static G_POPUP_CHILDINFO ci[20];
    int id=0;

    min_max_dec_step1[0] = min;
    min_max_dec_step2[0] = min;
    min_max_dec_step1[1] = max;
    min_max_dec_step2[1] = max;
    cont_id = g_popup_container_open(win_id, title, 
            G_POPUP_WAIT);

    popup_add_spin(cont_id, &(ci[id]), id,
                   label1, *val1, min_max_dec_step1);
    id++;
    popup_add_spin(cont_id, &(ci[id]), id,
                   label2, *val2, min_max_dec_step2);

    if (g_popup_container_show(cont_id)) {
        *val1 = ci[0].item;
        *val2 = ci[1].item;
        return TRUE;
    }
    return FALSE;
}

int popup_spin(int win_id, char *title, int *val1, char *label1, 
    int min, int max, int step)
{
    static int min_max_dec_step1[] = { 0, 0, 0, 0};
    int cont_id;
    G_POPUP_CHILDINFO ci[20];
    int id=0;

    min_max_dec_step1[0] = min;
    min_max_dec_step1[1] = max;
    min_max_dec_step1[3] = step;
    cont_id = g_popup_container_open(win_id, title, 
                                G_POPUP_WAIT);

    popup_add_spin(cont_id, &(ci[id]), id,
                   label1, *val1, min_max_dec_step1);

    if (g_popup_container_show(cont_id)) {
        *val1 = ci[0].item;
        return TRUE;
    }
    return FALSE;
}

int popup_option(int win_id, char *title, int *val1, char *label, 
                 int item_count, char **item_labels)
{
    int cont_id;
    G_POPUP_CHILDINFO ci[20];
    int id=0;

    cont_id = g_popup_container_open(win_id, title, 
                                G_POPUP_WAIT);

    popup_add_option2(cont_id, &(ci[id]), id, label,
                         *val1, item_count, item_labels);

    if (g_popup_container_show(cont_id)) {
        *val1 = ci[0].item;
        return TRUE;
    }
    return FALSE;
}

int psnd_rvalin(MBLOCK *mblk, char *ask, int items, float *result)
{
    char *tmp;
    char *p, string2[PSND_STRLEN + 1];
    int i;

    for (i=0, p = string2;i < items;i++) {
         if (items > 2)
             sprintf(p,"%.3f ",result[i]);
         else
             sprintf(p,"%f ",result[i]);
         p += strlen(p);
    }
    tmp = g_popup_promptdialog(mblk->info->win_id, "", ask, string2);

    if (tmp != NULL) {
        char *tok = strtok(tmp," ");
        i=0;
        while (i< items && tok) {
            result[i] = psnd_scan_float(tok);
            tok = strtok(NULL, " ");
            i++;
        }
        return TRUE;
    }
    return FALSE;
}

int psnd_rvalin2(MBLOCK *mblk, char *ask, float *result, int flag)
{
    float getf = *result;

    if (psnd_rvalin(mblk, ask,1,&getf) == 0)
        return FALSE;
    if (flag == FLAG_POSITIVE)
        if (getf <= 0)
            return FALSE;
    if (flag == FLAG_ATLEAST_1)
        if (getf < 1)
            return FALSE;
    if (flag == FLAG_NOTZERO)
        if (getf == 0)
            return FALSE;
    *result = getf;
    return TRUE;
}

int psnd_ivalin2(MBLOCK *mblk, char *ask, int *result, int flag)
{
    int geti = *result;

    if (psnd_ivalin(mblk, ask,1,&geti) == 0)
        return FALSE;
    if (flag == FLAG_POSITIVE)
        if (geti <= 0)
            return FALSE;
    if (flag == FLAG_ATLEAST_1)
        if (geti < 1)
            return FALSE;
    if (flag == FLAG_NOTZERO)
        if (geti == 0)
            return FALSE;
    *result = geti;
    return TRUE;
}

int psnd_ivalin(MBLOCK *mblk, char *ask, int items, int *result)
{
    char *tmp;
    char *p;
    static char string2[PSND_STRLEN + 1];
    int i;

    for (i=0, p = string2;i < items;i++) {
         sprintf(p,"%d ",result[i]);
         p += strlen(p);
    }
    tmp = g_popup_promptdialog(mblk->info->win_id, "", ask, string2);

    if (tmp != NULL) {
        char *tok = strtok(tmp," ");
        i=0;
        while (i< items && tok) {
            result[i] = psnd_scan_integer(tok);
            tok = strtok(NULL, " ");
            i++;
        }
        return TRUE;
    }
    return FALSE;
}



