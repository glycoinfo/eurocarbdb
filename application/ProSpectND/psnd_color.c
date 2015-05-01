/********************************************************************/
/*                         psnd_color.c                             */
/*                                                                  */
/* Color routines for the psnd program                              */
/* 1997, Albert van Kuik                                            */
/********************************************************************/
#include <stdio.h>
#include <stdlib.h>
#include <ctype.h>
#include <string.h>
#include <math.h>
#include <assert.h>
#include "genplot.h"
#include "psnd.h"

typedef enum {
    COL_QUIT = 100,
    COL_BLOCK,
    COL_ARRAY,
    COL_DEF,
    COL_READ,
    COL_WRITE,
#ifdef _WIN32
    COL_ITEM,
#endif
    COL_END
} colorwinids;

#define DEFAULT_MAX_COLORS	(64+16)
#define MAXCOLUMN		8
#define MAXROW			(DEFAULT_MAX_COLORS/MAXCOLUMN)

static void show_color(MBLOCK *mblk, int col_id);


static int color_clip(int color)
{
    color = max(color, 0);
    color = min(color, 255);
    return color;
}

void psnd_write_colormap(FILE *outfile)
{
    int i, ncolors;
    G_PALETTEENTRY pal;

    ncolors = g_get_palettesize();
    fprintf(outfile, "PALETTE_SIZE %d\n",ncolors);
    fprintf(outfile, "PALETTE_SETUP\n");
    for (i=0;i<ncolors;i++) {
        g_get_paletteentry(i, &pal);
        fprintf(outfile, "%6d %3d %3d %3d\n", i+1, pal.r, pal.g, pal.b);
    }
    fprintf(outfile, "PALETTE_END\n");
}

void psnd_read_colormap(FILE *infile, int ncolors)
{
    int i,r,g,b;
    G_PALETTEENTRY pal;
    char buf[PSND_STRLEN+1];

    g_set_palettesize(ncolors);
    while (fgets(buf, PSND_STRLEN, infile) != NULL) {
        char *p;
        char sep[] = " \r\n\t";

        if (buf[0] == '#')
            continue;
        strupr(buf);
        p= strtok(buf, sep);
        if (p) {
            if (strcmp(p, "PALETTE_END") == 0) {
                break;
            }
            else {
                i = psnd_scan_integer(p);
                p = strtok(NULL, sep);
                if (i > 0 && i <= ncolors && p) {
                    r = psnd_scan_integer(p);
                    p = strtok(NULL, sep);
                    if (r >= 0 && r <= 255 && p) {
                        g = psnd_scan_integer(p);
                        p = strtok(NULL, sep);
                        if (g >= 0 && g <= 255 && p) {
                            b = psnd_scan_integer(p);
                            if (b >= 0 && b <= 255) {
                                pal.r = r;
                                pal.g = g;
                                pal.b = b;
                                g_set_paletteentry(i-1, pal);
                            }
                        }
                    }
                }
            }
        }
    }
}

/*
 * Initiate a nice range of colors going from red to blue
 */
void psnd_init_colormap()
{ 
    int i,j,k,coffset,crange,cmax,cstep,cstart,cstop,cblocks,cvalue;
    int crevers;
    G_PALETTEENTRY pal;

    cmax = DEFAULT_MAX_COLORS;
    /* the 16 default colors */
    coffset = 16;
    cblocks = 4;   /* max of 6 blocks (5 and 6 not there now) */
    cvalue  = 248; /* 0..255, color intensity */
    crevers = FALSE; /* if TRUE, start with blue, red otherwise */
    g_set_palettesize(cmax);
    crange = cmax - coffset;
    cstep = crange / cblocks;
    cstart = coffset;
    cstop = cstart + cstep;
    coffset = crange/ cblocks;
    /* 
     * red to orange 
     */
    for (i=cstart,j=0;i<cstop;i++,j++) {
        k = j*((float)cvalue/(float)cstep);
        pal.r = cvalue;  
        pal.g = color_clip(k);     
        pal.b = 0;   
        g_set_paletteentry(crevers ? cmax+coffset-i-1 : i, pal);   
    } 
    cstart += coffset;
    cstop = cstart + cstep;
    /* 
     * orange to yellow
     */
    for (i=cstart,j=0;i<cstop;i++,j++) {
        k = j*((float)cvalue/(float)cstep);
        pal.r = color_clip(cvalue-k);  
        pal.g = cvalue;      
        pal.b = 0;   
        g_set_paletteentry(crevers ? cmax+coffset-i-1 : i, pal);   
    } 
    cstart += coffset;
    cstop = cstart + cstep;
    /* 
     * yellow to green
     */
    for (i=cstart,j=0;i<cstop;i++,j++) {
        k = j*((float)cvalue/(float)cstep);
        pal.r = 0;  
        pal.g = cvalue;     
        pal.b = color_clip(k);   
        g_set_paletteentry(crevers ? cmax+coffset-i-1 : i, pal);   
    } 
    cstart += coffset;
    cstop = cstart + cstep;
    /* 
     * green to blue
     */
    for (i=cstart,j=0;i<cstop;i++,j++) {
        k = j*((float)cvalue/(float)cstep);
        pal.r = 0;     
        pal.g = color_clip(cvalue-k);     
        pal.b = cvalue;   
        g_set_paletteentry(crevers ? cmax+coffset-i-1 : i, pal);   
    } 
}

#ifdef _WIN32
static void select_block_and_item(MBLOCK *mblk)
{
    static G_POPUP_CHILDINFO ci[10];
    static int i,id, cont_id, ok;
    static char *data[MAX_BLOCK] = { 
        "1", "2", "3", "4", 
        "5", "6", "7", "8",
        "9", "10", "11", "12" };
    int colornamecount;
    char **arraydata;

    assert(mblk->info->colorblock_id <= 12);
    arraydata = psnd_get_colornames(&colornamecount);

    cont_id = g_popup_container_open(mblk->info->colorwin_id, 
                     "Select array", G_POPUP_WAIT);

    id = 0;
    g_popup_init_info(&(ci[id]));
    ci[id].type  = G_CHILD_PANEL;
    ci[id].item  = 1;
    ci[id].title = "Select Item";
    ci[id].horizontal  = 0;
    ci[id].frame      = TRUE;
    g_popup_add_child(cont_id, &(ci[id]));

    id++;
    g_popup_init_info(&(ci[id]));
    ci[id].type       = G_CHILD_OPTIONMENU;
    ci[id].id         = COL_BLOCK;
    ci[id].item_count = mblk->info->max_block;
    ci[id].label      = "Block";
    ci[id].data       = data;
    ci[id].item       = mblk->info->colorblock_id;
    ci[id].horizontal = TRUE;
    ci[id].frame      = TRUE;
    g_popup_add_child(cont_id, &(ci[id]));

    id++;
    g_popup_init_info(&(ci[id]));
    ci[id].type       = G_CHILD_OPTIONMENU;
    ci[id].id         = COL_ARRAY;
    ci[id].item_count = colornamecount;
    ci[id].label      = "item";
    ci[id].data       = arraydata;
    ci[id].item       = mblk->info->colorarray_id;
    ci[id].horizontal = TRUE;
    ci[id].frame      = TRUE;
    g_popup_add_child(cont_id, &(ci[id]));

    ok = g_popup_container_show(cont_id);

    if (ok) for (i=0;i<=id;i++) {
        int color;
        switch (ci[i].id) {
        case COL_BLOCK:
            mblk->info->colorblock_id = ci[i].item;
            break;
        case COL_ARRAY:
            mblk->info->colorarray_id = ci[i].item;
            break;
        }
        color = psnd_get_colorvalue(mblk,mblk->info->colorarray_id,
                                         mblk->info->colorblock_id);
            
        show_color(mblk, color);
    }

}
#endif

/*
 * Display the color map in a window
 */
void psnd_show_colormap(MBLOCK *mblk, int numcol)
{
    float x0,y0,dx,dy,box_height,box_width;
    int nx,ny,win_id, save_win_id,bar_id;
    char string[5];
    int i,ix,iy,color=0;
    int colornamecount;
    char **arraydata;

    if (mblk->info->colorwin_id) {
        g_raise_window(mblk->info->colorwin_id);
        return;
    }

    arraydata = psnd_get_colornames(&colornamecount);
    box_width  = 60.0;
    box_height = 30.0;
    nx = sqrt(numcol);
    if (nx>MAXCOLUMN) nx=MAXCOLUMN;
    ny = numcol/nx;
    if (nx*ny < numcol) ny++;
    
    x0 = 0.0;
    y0 = 0.0;
    dx = box_width*(nx);
    dy = box_height*(ny);
    g_push_gc();
    save_win_id=g_get_windownr();

    mblk->info->colorwin_id = 
        g_open_window(G_SCREEN, x0, y0, dx, dy, "Colormap",
                          G_WIN_BUFFER|
                          G_WIN_BUTTONBAR2);

/*    g_popup_follow_cursor(TRUE);    */
    g_delete_object(mblk->spar[S_COLORMAP].obj_id);
    g_open_object(mblk->spar[S_COLORMAP].obj_id);
    g_set_viewport(mblk->spar[S_COLORMAP].vp_id, 0, 0, 1, 1);
    g_set_world(mblk->spar[S_COLORMAP].vp_id, 0, 0, nx+0.01, 1+ny+0.01);
    g_select_viewport(mblk->spar[S_COLORMAP].vp_id);
    g_set_clipping(FALSE);
    g_set_font(G_FONT_HELVETICA_BOLD,G_RELATIVE_FONTSCALING);
    g_set_charsize(1.0);
    for (iy=0;iy<ny;iy++) {
        for(ix=0;ix<nx;ix++) {
            g_set_foreground(color);
            g_fillrectangle(ix,ny-iy-1,ix+1,ny-iy);
            g_moveto((float)ix+0.25,(float)(ny-iy-1)+0.25);
            if ((color%(numcol)) == G_WHITE )
                g_set_foreground(G_BLACK);
            else
                g_set_foreground(G_WHITE);
            sprintf(string,"%d",color);
            g_label(string);
            color++;
        }
    }
    g_close_object(mblk->spar[S_COLORMAP].obj_id);
    g_call_object(mblk->spar[S_COLORMAP].obj_id);

    bar_id = g_menu_create_buttonbox2(mblk->info->colorwin_id, 1);
    mblk->info->colorbar_id = bar_id;
    g_menu_append_button(bar_id, "Quit", COL_QUIT);
    g_menu_append_button(bar_id, "Reset", COL_DEF);
    g_menu_append_button(bar_id, "Save", COL_WRITE);
    g_menu_append_button(bar_id, "Retrieve", COL_READ);
#ifdef _WIN32
    g_menu_append_button(bar_id, "Item", COL_ITEM);
#else
    {
    char *data[MAX_BLOCK] = { "1", "2", "3", "4", 
                              "5", "6", "7", "8",
                              "9", "10", "11", "12" };
    assert(mblk->info->colorblock_id <= 12);
    g_menu_append_optionmenu(bar_id, "Block", 
           COL_BLOCK, MAXBLK, 
           mblk->info->colorblock_id, data);
    }
    g_menu_append_optionmenu(bar_id, "Item", 
                             COL_ARRAY, colornamecount, 
                             mblk->info->colorarray_id, 
                             arraydata);
#endif
    g_menu_create_buttonbox2(mblk->info->colorwin_id, -1);
    g_flush();
    g_pop_gc();
    g_select_window(save_win_id);

    color = psnd_get_colorvalue(mblk,mblk->info->colorarray_id, 
                                 mblk->info->colorblock_id);
            
    show_color(mblk, color);

}

static void show_color(MBLOCK *mblk, int col_id)
{
    int  save_win_id;
    char *label;
    int  colornamecount;
    char **arraydata;
    int win_id   = mblk->info->colorwin_id; 
    int block_id = mblk->info->colorblock_id;
    int array_id = mblk->info->colorarray_id;
    
    g_push_gc();
    save_win_id=g_get_windownr();
    g_select_window(win_id);
    g_delete_object(mblk->spar[S_COLORMAP].obj_id+1);
    g_open_object(mblk->spar[S_COLORMAP].obj_id+1);
    g_select_viewport(mblk->spar[S_COLORMAP].vp_id);
    g_set_foreground(col_id);

    g_fillrectangle(0,MAXROW,MAXCOLUMN,MAXROW+1);
    g_moveto(0.2,MAXROW+0.2);
    if (col_id == G_WHITE )
        g_set_foreground(G_BLACK);
    else
        g_set_foreground(G_WHITE);

    g_set_font(G_FONT_HELVETICA_BOLD,G_RELATIVE_FONTSCALING);
    g_set_charsize(1.0);
    arraydata = psnd_get_colornames(&colornamecount);
    label = psnd_sprintf_temp("Block%d:%s:Color=%d", 
                    block_id+1, arraydata[array_id], col_id);
    g_label(label);
    g_close_object(mblk->spar[S_COLORMAP].obj_id+1);
    g_call_object(mblk->spar[S_COLORMAP].obj_id+1);
    g_flush();
    g_pop_gc();
    g_select_window(save_win_id);
}

int psnd_colormap_window_event(MBLOCK *mblk, G_EVENT *ui)
{
    int col_id, color;
/*
printf("%d %g %g %d\n",ui->event,ui->x,ui->y,ui->vp_id);
*/
    switch (ui->event) {
#ifndef _WIN32
        case G_OPTIONMENU:
            if (ui->keycode == COL_BLOCK) {
                if (ui->item >= mblk->info->max_block) {
/*
 * Not yet in genplot
 */
                    g_popup_set_selection(mblk->info->colorbar_id, 
                           COL_BLOCK, mblk->info->colorblock_id) ;
                    break;
                }
                mblk->info->colorblock_id = ui->item;
            }
            else if (ui->keycode == COL_ARRAY)
                mblk->info->colorarray_id = ui->item;
            color = psnd_get_colorvalue(mblk,mblk->info->colorarray_id,
                                         mblk->info->colorblock_id);
            
            show_color(mblk, color);
            break;
#endif
        case G_COMMAND:
	    switch (ui->keycode) {
            case COL_QUIT:
                g_close_window(ui->win_id);
                g_set_cursortype(mblk->info->win_id, G_CURSOR_CROSSHAIR);
                mblk->info->colorwin_id = 0;
                break;
            case COL_DEF:
                psnd_set_default_colors(mblk);
                break;
            case COL_WRITE:
                psnd_write_resources(mblk);
                break;
            case COL_READ:
                psnd_read_resources(mblk, TRUE);
                break;
#ifdef _WIN32
            case COL_ITEM:
                select_block_and_item(mblk);
                break;
#endif
            }
            break;
        case G_WINDOWDESTROY:
            mblk->info->colorwin_id = 0;
            break;
        case G_BUTTON1PRESS:
            col_id = (int)ui->x + (MAXROW-1-(int) ui->y) * MAXCOLUMN;
            col_id = max(0,col_id);
            col_id %= DEFAULT_MAX_COLORS;
/*
            printf("%d %d => %d\n",(int)ui->x,(int)ui->y, col_id);
*/
            show_color(mblk, col_id);
            psnd_set_colorvalue(mblk,
                                 mblk->info->colorarray_id, 
                                 mblk->info->colorblock_id, 
                                 col_id);

            break;
    }
    return TRUE;
}

