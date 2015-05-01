/*
     layoutmgr.h
*/

#ifndef __LAYOUTMGR_H
#define __LAYOUTMGR_H

typedef struct tagPACKINFO {
    short  level;
    short  width, height;
    short  x, y;
    short  marginx, marginy;
    short  borderx;
    short  border_top;
    short  border_bottom;
    short  direction;
    int    flag;
    struct tagPACKINFO *next;
} PACKINFO;

#define PACK_HORIZONTAL	0
#define PACK_VERTICAL	1

#define FILL_X	(1<<0)
#define FILL_Y	(1<<1)
#define GRID_X	(1<<2)
#define GRID_Y	(1<<3)
#define ALIGN_X_CENTER	(1<<4)
#define ALIGN_Y_CENTER	(1<<5)

#ifndef max
#define max(a,b)        (((a) > (b)) ? (a) : (b))
#endif

void manage_layout(PACKINFO *pinf);


#endif




