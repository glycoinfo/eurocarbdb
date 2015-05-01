/*

    layoutmgr.c

*/

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <assert.h>
#include "layoutmgr.h"


typedef struct tagLAYOUT {
    short width, height;
    short x, y;
    short offx, offy;
    short marginx, marginy;
    short borderx;
    short border_top;
    short border_bottom;
    short direction;
    int   flag;
    PACKINFO *pinf;
    struct tagLAYOUT *child;
    struct tagLAYOUT *parent;
    struct tagLAYOUT *next;
} LAYOUT;




void printit(FILE *file, LAYOUT *base)
{
    LAYOUT *tmp;

    tmp = base;
    while (tmp) {
        if (tmp->child)
            printit(file,tmp->child);
        fprintf(file, "x=%5d y=%5d, width=%5d height=%5d\n", 
                tmp->x, tmp->y, tmp->width, tmp->height);
        tmp = tmp->next;
    }
}

void ps_header(FILE *file, float scale)
{
    fprintf(file,"%%!PS-Adobe-3.0\n");
    fprintf(file,"/gpDict 18 dict def\n");
    fprintf(file,"gpDict begin\n");
    fprintf(file,"/l {lineto} def ");
    fprintf(file,"/m {moveto} def ");
    fprintf(file,"/sc {scale} def\n");
    fprintf(file,"/t {translate} def ");
    fprintf(file,"/r {rotate} def ");
    fprintf(file,"/w {setlinewidth} def\n");
    fprintf(file,"/d {setdash} def ");
    fprintf(file,"/st {stroke} def\n");
    fprintf(file,"/cp {closepath} def ");
    fprintf(file,"/np {newpath} def ");
    fprintf(file,"/gs {gsave} def ");
    fprintf(file,"/gr {grestore} def\n");
    fprintf(file,"/fg {/blue exch def /green exch def /red exch def\n");
    fprintf(file,"np red green blue setrgbcolor} def\n");
    fprintf(file,"/f { /s exch def findfont s scalefont setfont} def\n");
    fprintf(file,"/dc { /ra exch def /y exch def /x exch def np x y ra 0 360 arc st} def\n"); 
    fprintf(file,"/fc { /ra exch def /y exch def /x exch def np x y ra 0 360 arc\n");
    fprintf(file,"cp gs fill gr} def\n");
    fprintf(file,"/dr { /y1 exch def /x1 exch def /y0 exch def /x0 exch def\n");
    fprintf(file,"np x0 y0 m x0 y1 l x1 y1 l x1 y0 l x0 y0 l st} def\n");
    fprintf(file,"/fr { /y1 exch def /x1 exch def /y0 exch def /x0 exch def\n");
    fprintf(file,"np x0 y0 m x0 y1 l x1 y1 l x1 y0 l x0 y0 l\n");
    fprintf(file,"cp gs fill gr st} def\n");

    fprintf(file,"10 10 t\n");
    fprintf(file,"1 w\n");
    fprintf(file,"%g %g sc\n", scale, scale);
}

void ps_it(FILE *file, LAYOUT *base)
{
    LAYOUT *tmp;

    tmp = base;
    while (tmp) {
        if (tmp->child)
            ps_it(file, tmp->child);
        fprintf(file,"%d %d %d %d dr\n",
                tmp->x + tmp->offx, 
                tmp->y + tmp->offy, 
                tmp->x + tmp->width  + tmp->offx, 
                tmp->y + tmp->height + tmp->offy);
        tmp = tmp->next;
    }
}

void ps_footer(FILE *file)
{
        fprintf(file,"showpage\ngr\n");
}


/*
 * Calc the layout of all siblings
 * If they have children, do those first
 */
static void calc_layout(LAYOUT *layout)
{
    int width, height;
    int width_common, height_common;
    int direction;
    LAYOUT *tmp;

    direction = layout->direction;
    layout->x = layout->borderx;
    layout->y = layout->border_top;
    tmp = layout;
  
    if (layout->child)
        calc_layout(layout->child);
    width_common  = layout->width;
    height_common = layout->height;
    while (layout->next) {
        if (layout->next->child)
            calc_layout(layout->next->child);
        if (direction == PACK_HORIZONTAL) {
            layout->next->x = layout->x + layout->width + layout->marginx;
            layout->next->y = layout->y;
        }
        else {
            layout->next->x = layout->x;
            layout->next->y = layout->y + layout->height + layout->marginy;
        }
        width_common    = max(width_common, layout->next->width);
        height_common   = max(height_common, layout->next->height);
        layout = layout->next;
    }

    if (direction == PACK_HORIZONTAL) {
        LAYOUT *tmp2 = tmp;
        if (tmp->flag & GRID_X) {
            int old_x;

            old_x = tmp->x;
            tmp->width = width_common;
            while (tmp->next) {
                tmp = tmp->next;
                tmp->width = width_common;
                tmp->x = old_x + width_common + layout->marginx;
                old_x = tmp->x;
            } 
        }
        tmp = tmp2;
        if (tmp->flag & GRID_Y) {
            tmp->height = height_common;
            while (tmp->next) {
                tmp = tmp->next;
                tmp->height = height_common;
            } 
        }
        width  = layout->x + layout->width + layout->borderx;
        height = height_common + layout->border_top + layout->border_bottom;
    }
    else {
        LAYOUT *tmp2 = tmp;
        if (tmp->flag & GRID_Y) {
            int old_y;

            old_y = tmp->y;
            tmp->height = height_common;
            while (tmp->next) {
                tmp = tmp->next;
                tmp->height = height_common;
                tmp->y = old_y + height_common + layout->marginy;
                old_y = tmp->y;
            } 
        }
        tmp = tmp2;
        if (tmp->flag & GRID_X) {
            tmp->width = width_common;
            while (tmp->next) {
                tmp = tmp->next;
                tmp->width = width_common;
            } 
        }
        width = width_common + 2 * layout->borderx;
        height = layout->y + layout->height + layout->border_bottom;
    }

    /*
     * Update parents, and grand parents
     */
    while (layout->parent) {
        layout->parent->width  = max(layout->parent->width, width);
        layout->parent->height = max(layout->parent->height, height);
        layout = layout->parent;
        if (layout->direction == PACK_HORIZONTAL) {
            height +=  layout->border_top + layout->border_bottom;
        }
        else {
            width += 2 * layout->borderx;
        }
    }
}

static void calc_offsets(LAYOUT *layout, int offx, int offy)
{
    do {
        layout->offx = offx;
        layout->offy = offy;    
        if (layout->child) {
            calc_offsets(layout->child, 
                         layout->offx + layout->x, 
                         layout->offy + layout->y);
        }
        layout = layout->next;
    } while (layout);   
}

static calc_space(LAYOUT *layout, int *space_x, int *space_y)
{
    int count = 0;
    int width=0, height=0;
    int parent_width=0, parent_height=0;
    
    if (layout->parent) {
        parent_width  = layout->parent->width;
        parent_height = layout->parent->height;
    }
    width  = layout->borderx;
    height = layout->border_top;
    while (layout) {
        int marginx, marginy;
        
        count++;
        if (layout->next) {
            marginx = layout->marginx;
            marginy = layout->marginy;
        }
        else {
            marginx = layout->borderx;
            marginy = layout->border_bottom;
        }
        if (layout->direction == PACK_HORIZONTAL) {
            width  += layout->width + marginx;
            height  = max(height, layout->height);
        }
        else {
            width   = max(width, layout->width);
            height += layout->height + marginy;
        }
        layout = layout->next;
    }
    *space_x = max(0,parent_width  - width);
    *space_y = max(0,parent_height - height);
    return count;
}

static void calc_aligns(LAYOUT *layout)
{
    LAYOUT *parent, *tmp;

    tmp    = layout;
    parent = layout->parent;
    if (parent) {
        int nsib;
        int parent_width = 0, parent_height = 0;
        int space_x = 0, space_y = 0, extra_space_x, extra_space_y;

        nsib   = calc_space(layout, &space_x, &space_y);
        parent_width  = parent->width  - 2 * layout->borderx;
        parent_height = parent->height - layout->border_top 
                                       - layout->border_bottom;
        extra_space_x = 0;
        extra_space_y = 0;
        do {
            extra_space_x += space_x /(nsib+1);
            extra_space_y += space_y /(nsib+1);
            if (layout->flag & ALIGN_X_CENTER) {
                if (layout->direction == PACK_VERTICAL) 
                    layout->x = layout->borderx + (parent_width-
                             layout->width)/2;
                else 
                    layout->x +=  extra_space_x;
            }
            if (layout->flag & ALIGN_Y_CENTER) {
                if (layout->direction == PACK_HORIZONTAL) 
                    layout->y = layout->border_top + (parent_height-
                             layout->height)/2;
                else
                    layout->y +=  extra_space_y;
            }
            layout = layout->next;
        } while (layout);   
    }
    layout = tmp;
    do {
        if (layout->child) 
            calc_aligns(layout->child);
        layout = layout->next;
    } while (layout);   

}

static void calc_fills(LAYOUT *layout, int width, int height)
{
    do {
        if ((layout->flag & FILL_X) && layout->direction == PACK_VERTICAL) {
            layout->width = max(layout->width, width);
        }
        else if ((layout->flag & FILL_Y) && layout->direction == PACK_HORIZONTAL) {
            layout->height = max(layout->height, height);
        }
        if (layout->child) {
            calc_fills(layout->child, 
                         layout->width - 2 * layout->child->borderx, 
                         layout->height - layout->child->border_top 
                                         - layout->child->border_bottom);
        }
        layout = layout->next;
    } while (layout);   
}

static LAYOUT *new_layout()
{
    LAYOUT *layout;

    layout = (LAYOUT*) calloc(sizeof(LAYOUT),1);
    assert(layout);
    return layout;
}

static void add_layout(LAYOUT *base, int level, int width, int height,
                int direction, int marginx, int marginy, 
                int borderx, int border_top, int border_bottom, int flag,
                PACKINFO *pinf)
{
    LAYOUT *layout, *tmp;

    layout = new_layout();
    layout->width  = width;
    layout->height = height;
    layout->marginx = marginx;
    layout->marginy = marginy;
    layout->borderx = borderx;
    layout->border_bottom = border_bottom;
    layout->border_top = border_top;
    layout->direction = direction;
    layout->flag = flag;
    layout->pinf = pinf;

    if (base->next == NULL) {
        base->next = layout;
        return;
    }
    tmp = base->next;
    while (tmp->next)
        tmp = tmp->next;
    while (level) {
        if (tmp->child == NULL) {
            tmp->child = layout;
            layout->parent = tmp;
            return;
        }
        tmp = tmp->child;
        while (tmp->next)
            tmp = tmp->next;
        level--;
    }
    tmp->next = layout;
    layout->parent = tmp->parent;
}

static void free_layout(LAYOUT *base)
{
    LAYOUT *tmp1, *tmp2;
    
    tmp1 = base;
    while (tmp1) {
        tmp2 = tmp1;
        if (tmp1->child)
            free_layout(tmp1->child);
        tmp1 = tmp1->next;
        free(tmp2);
    }
}

static void calc_total_layout(LAYOUT *base)
{
    calc_layout(base->next);
    calc_fills(base->next, 0, 0);
    calc_aligns(base->next);
    calc_offsets(base->next, 0, 0);
    
}

// #define debuggg
static void transfer_results(LAYOUT *base)
{
    LAYOUT *tmp;

    tmp = base;
    while (tmp) {
        if (tmp->child)
            transfer_results(tmp->child);
        if (tmp->pinf) {
            tmp->pinf->x = tmp->x + tmp->offx;
            tmp->pinf->y = tmp->y + tmp->offy; 
            tmp->pinf->width  = tmp->width;
            tmp->pinf->height = tmp->height;
        }
        tmp = tmp->next;
    }
}

void manage_layout(PACKINFO *pinf)
{
    LAYOUT *base, *tmp;
    int i,level,flag;
    int padx, pady;
PACKINFO *tmpxx;
FILE *file;
    padx = 6;
    pady = 10;
/*
    flag = FILL_X | FILL_Y | GRID_X;
*/
    flag = ALIGN_Y_CENTER  ;

tmpxx=pinf;

//file = fopen("blaa","w");
//if (file==NULL)
  //  return;

    base = new_layout();
    level = 0;
    while (pinf) {
        if (pinf->level < 0) {
            free_layout(base);
            return;
        }
        add_layout(base, 
                   pinf->level, 
                   pinf->width, 
                   pinf->height,
                   pinf->direction, 
                   pinf->marginx, 
                   pinf->marginy, 
                   pinf->borderx, 
                   pinf->border_top, 
                   pinf->border_bottom, 
                   pinf->flag,
                   pinf);
//fprintf(file, "%d %d %d\n",pinf->level, 
    //               pinf->width, 
      //             pinf->height);



        pinf = pinf->next;
    }
//fclose(file);

    calc_total_layout(base);
    transfer_results(base);
    free_layout(base);

#ifdef debuggg
{
FILE *file = fopen("blaa.ps","w");
if (file==NULL)
    return;

   ps_header( file, 1.0);
pinf=tmpxx;
    while (pinf) {

           fprintf(file,"%d %d %d %d dr\n",
                pinf->x, 
                pinf->y, 
                pinf->x+pinf->width, 
                pinf->y+pinf->height);
pinf=pinf->next;
}
    ps_footer(file);
fclose(file);
}
#endif
}

#ifdef meeeuuu

main()
{
    static PACKINFO pinf[100];
    int i=0,level,flag=0;
    int padx, pady, count;
LAYOUT *base, *tmpxx;

    padx = 4;
    pady = 4;
/*
    flag = FILL_X | FILL_Y | GRID_X;
    flag = ALIGNY_CENTER  ;
*/

flag=   GRID_X | FILL_Y;
 i=0;

    level = 0; 
    pinf[i].level =  level;
    pinf[i].width = 50;
    pinf[i].height= 50;
    pinf[i].marginx = padx;
    pinf[i].marginy = pady;
    pinf[i].direction = PACK_VERTICAL;
    pinf[i].flag = flag;
    
   level++;
    i++;
    pinf[i].level =  level;
    pinf[i].width = 1;
    pinf[i].height= 1;
    pinf[i].marginx = padx;
    pinf[i].marginy = pady;
    pinf[i].direction = PACK_VERTICAL;
    pinf[i].flag = flag;

    level++;
    i++;
    pinf[i].level =  level;
    pinf[i].width = 8;
    pinf[i].height= 5;
    pinf[i].marginx = padx;
    pinf[i].marginy = pady;
    pinf[i].direction = PACK_VERTICAL;
    pinf[i].flag = flag; 

    level--;
    i++;
    pinf[i].level =  level;
    pinf[i].width = 50;
    pinf[i].height= 20;
    pinf[i].marginx = padx;
    pinf[i].marginy = pady;
    pinf[i].direction = PACK_VERTICAL;
    pinf[i].flag = flag;
    
    i++;
    pinf[i].level =  level;
    pinf[i].width = 1;
    pinf[i].height= 1;
    pinf[i].marginx = padx;
    pinf[i].marginy = pady;
    pinf[i].direction = PACK_VERTICAL;
    pinf[i].flag = FILL_X;

    i++;
    pinf[i].level =  level;
    pinf[i].width = 40;
    pinf[i].height= 10;
    pinf[i].marginx = padx;
    pinf[i].marginy = pady;
    pinf[i].direction = PACK_VERTICAL;
    pinf[i].flag = flag;
 
 
    i++;
    pinf[i].level =  level;
    pinf[i].width = 1;
    pinf[i].height= 1;
    pinf[i].marginx = padx;
    pinf[i].marginy = pady;
    pinf[i].direction = PACK_VERTICAL;
    pinf[i].flag = flag;
 
    level++;
    i++;
    pinf[i].level =  level;
    pinf[i].width = 20;
    pinf[i].height= 16;
    pinf[i].marginx = padx;
    pinf[i].marginy = pady;
    pinf[i].direction = PACK_HORIZONTAL;
    pinf[i].flag = FILL_Y;

   i++;
    pinf[i].level =  level;
    pinf[i].width = 38;
    pinf[i].height= 9;
    pinf[i].marginx = padx;
    pinf[i].marginy = pady;
    pinf[i].direction = PACK_HORIZONTAL;
    pinf[i].flag = FILL_Y;
    
    count = i+1;
/*
                   pinf[i].borderx, 
                   pinf[i].border_bottom, 

*/
    base = new_layout();
    for (i=0;i<count;i++) {
        add_layout(base, 
                   pinf[i].level, 
                   pinf[i].width, 
                   pinf[i].height,
                   pinf[i].direction, 
                   pinf[i].marginx, 
                   pinf[i].marginy, 
                   5, 
                   8, 
                   2, 
                   pinf[i].flag,
                   &(pinf[i]));
    }
    calc_total_layout(base);
    transfer_results(base);
    free_layout(base);

   
{
FILE *file = fopen("blaa.ps","w");
if (file==NULL)
    return; 

   ps_header( file, 2.0);

    for (i=0;i<count;i++) {

           fprintf(file,"%d %d %d %d dr\n",
                pinf[i].x, 
                pinf[i].y, 
                pinf[i].x+pinf[i].width, 
                pinf[i].y+pinf[i].height);

}    
    ps_footer(file);
fclose(file);
}


}

#endif

