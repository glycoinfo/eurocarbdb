/************************************************************************/
/*                               genlist.c                              */
/*                                                                      */
/*  Platform : All                                                      */
/*  Module   : Genplot object management                                */
/*                                                                      */
/*  Albert van Kuik                                                     */
/************************************************************************/
#include <stdlib.h>
#include <stdio.h>
#include <string.h>

#ifdef DEBUG
#include "mshell.h"
#endif

#include "genplot.h"
#include "g_inter.h"
#include "btree.h"
#include "genrbtree.h"

#define G_MAXCHUNKREC 100        /* Max records in a chunk of object memory */
#define G_POLYSPACECHUNKSIZE	400

#define BYTE unsigned char

typedef struct tagG_DISK_LIST_RECORD {
    BYTE	id;
    BYTE	skip;
} G_DISK_LIST_RECORD;

typedef struct tagG_LIST_RECORD {
    long	  id;
    void          (*func)(struct tagG_LIST_RECORD *);
    float         x[4];
    unsigned int  i;
    void          *p;
} G_LIST_RECORD;

typedef struct G_LIST_CHUNK {
    int id;
    int parent;
    int count;
    int status;
    int play;
    char *label;
    int poly_size;
    int poly_pos;
    void *poly_space;
    G_LIST_RECORD rec[G_MAXCHUNKREC];
    struct G_LIST_CHUNK *next;
} G_LIST_CHUNK;

static G_LIST_CHUNK *g_chunk;
static int g_list_openobject;

#define LR g_chunk->rec[g_chunk->count]

void (*g_moveto)(float x, float y);
void (*g_lineto)(float x, float y);
void (*g_label)(char *label);
void (*g_rectangle)(float x1, float y1, float x2, float y2);
void (*g_drawpoly)(long numpoint, float *points);
void (*g_circle)(float r);
void (*g_fillrectangle)(float x1, float y1, float x2, float y2);
void (*g_fillpoly)(long numpoint, float *points);
void (*g_fillcircle)(float r);
void (*g_set_textdirection)(int direction);
void (*g_set_linewidth)(int width);
void (*g_set_linestyle)(int pattern);
void (*g_set_foreground)(int color);
void (*g_set_background)(int color);
void (*g_set_font)(int font, int scale_to);
void (*g_set_charsize)(float size);
void (*g_set_clipping)(int clip);
int  (*g_set_world)(int no,float x1,float y1,float x2,float y2);
int  (*g_set_viewport)(int no,float x1,float y1,float x2,float y2);
int  (*g_select_viewport)(int no);
void (*g_clear_viewport)(void);
int  (*g_call_object)(int id);
int  (*g_push_gc)(void);
int  (*g_pop_gc)(void);
int  (*g_push_viewport)(void);
int  (*g_pop_viewport)(void);
int  (*g_call_object_byname)(char *label);
int  (*g_set_palettesize)(int size);
int  (*g_set_paletteentry)(int entry_id, G_PALETTEENTRY entry);


int INTERNAL_object_playback_flag;

/*===========================================================================*/
/* Linked list of plot routines                                              */
/*===========================================================================*/
static int RBobjListID, RBobjBNListID, RBobjTempListID;

static char *toKey(int id)
{
   static char temp_key[20]; 
   sprintf(temp_key, "%6d", id);
   return temp_key;
}

/* --- Direct all function pointers to save their output in the
       current object --- */
static void set_listmode(void)
{
    g_moveto              = g_LIST_moveto;
    g_lineto              = g_LIST_lineto;
    g_label               = g_LIST_label;
    g_rectangle           = g_LIST_rectangle;
    g_drawpoly            = g_LIST_drawpoly;
    g_circle              = g_LIST_circle;
    g_fillrectangle       = g_LIST_fillrectangle;
    g_fillpoly            = g_LIST_fillpoly;
    g_fillcircle          = g_LIST_fillcircle;
    g_set_textdirection   = g_LIST_set_textdirection;
    g_set_linewidth       = g_LIST_set_linewidth;
    g_set_linestyle       = g_LIST_set_linestyle;
    g_set_foreground      = g_LIST_set_foreground;
    g_set_background      = g_LIST_set_background;
    g_set_font            = g_LIST_set_font;
    g_set_charsize        = g_LIST_set_charsize;
    g_set_clipping        = g_LIST_set_clipping;
    g_set_world           = g_LIST_set_world;
    g_set_viewport        = g_LIST_set_viewport;
    g_select_viewport     = g_LIST_select_viewport;
    g_clear_viewport      = g_LIST_clear_viewport;
    g_call_object         = g_LIST_call_object;
    g_push_gc             = g_LIST_push_gc;
    g_pop_gc              = g_LIST_pop_gc;
    g_push_viewport       = g_LIST_push_viewport;
    g_pop_viewport        = g_LIST_pop_viewport;
    g_call_object_byname  = g_LIST_call_object_byname;
    g_set_palettesize     = g_LIST_set_palettesize;
    g_set_paletteentry    = g_LIST_set_paletteentry;

}

/* --- Direct all function pointers to send their output
       to the current device --- */
void INTERNAL_set_directmode(void)
{
    g_moveto              = g_DIRECT_moveto;
    g_lineto              = g_DIRECT_lineto;
    g_label               = g_DIRECT_label;
    g_rectangle           = g_DIRECT_rectangle;
    g_drawpoly            = g_DIRECT_drawpoly;
    g_circle              = g_DIRECT_circle;
    g_fillrectangle       = g_DIRECT_fillrectangle;
    g_fillpoly            = g_DIRECT_fillpoly;
    g_fillcircle          = g_DIRECT_fillcircle;
    g_set_textdirection   = g_DIRECT_set_textdirection;
    g_set_linewidth       = g_DIRECT_set_linewidth;
    g_set_linestyle       = g_DIRECT_set_linestyle;
    g_set_foreground      = g_DIRECT_set_foreground;
    g_set_background      = g_DIRECT_set_background;
    g_set_font            = g_DIRECT_set_font;
    g_set_charsize        = g_DIRECT_set_charsize;
    g_set_clipping        = g_DIRECT_set_clipping;
    g_set_world           = g_DIRECT_set_world;
    g_set_viewport        = g_DIRECT_set_viewport;
    g_select_viewport     = g_DIRECT_select_viewport;
    g_clear_viewport      = g_DIRECT_clear_viewport;
    g_call_object         = g_DIRECT_call_object;
    g_push_gc             = g_DIRECT_push_gc;
    g_pop_gc              = g_DIRECT_pop_gc;
    g_push_viewport       = g_DIRECT_push_viewport;
    g_pop_viewport        = g_DIRECT_pop_viewport;
    g_call_object_byname  = g_DIRECT_call_object_byname;
    g_set_palettesize     = g_DIRECT_set_palettesize;
    g_set_paletteentry    = g_DIRECT_set_paletteentry;

}


/* --- Kill object list 'c1' --- */
static void kill_list(G_LIST_CHUNK *c1)
{
    G_LIST_CHUNK *c2;
    int i;

    if (c1->label) 
        free(c1->label);
    while (c1 != NULL) {
        if (c1->poly_space) 
            free(c1->poly_space);
        c2 = c1->next;
        /*
        for (i=0;i<c1->count;i++)
            if (c1->rec[i].p != NULL)
                free(c1->rec[i].p);
                */
        free(c1);
        c1 = c2;
    }
}

static int treedel(void *key, void *data, void *userdata)
{
    key = key;
    userdata = userdata;
    kill_list((G_LIST_CHUNK*) data);
    return G_ERROR;
}

/* --- Kill all objects --- */
void INTERNAL_kill_objects(void)
{
    RB_doForAll(RBobjListID, treedel, NULL);
    RB_killTree(RBobjListID);
    RB_killTree(RBobjBNListID);
    RB_killTree(RBobjTempListID);
}

static void kill_templist(void)
{
    RB_killTree(RBobjTempListID);
    RBobjTempListID = RB_initTree();
}

/* --- Initiate the objects -- */
void INTERNAL_init_objects(void)
{
    RBobjListID  = RB_initTree();
    RBobjBNListID = RB_initTree();
    RBobjTempListID = RB_initTree();
    g_chunk = NULL;
    INTERNAL_object_playback_flag = FALSE;
    g_list_openobject = G_ERROR;
}


/* --- Allocate a new chunk of memory for object 'id' --- */
static G_LIST_CHUNK *make_chunk(int id)
{
    G_LIST_CHUNK *new;
    int i;

    if ((new = (G_LIST_CHUNK *)malloc(sizeof(G_LIST_CHUNK))) == NULL) {
        perror("Memory error\n");
        exit(1);
    }
    new->id     	= id;
    new->parent 	= G_ERROR;
    new->count  	= 0;
    new->status 	= G_AWAKE;
    new->play		= FALSE;
    new->next   	= NULL;
    new->label  	= NULL;
    new->poly_size	= 0;
    new->poly_pos	= 0;
    new->poly_space	= NULL;
    for (i=0;i<G_MAXCHUNKREC;i++)
        new->rec[i].p = NULL;
    return new;
}

/* --- Increment the record pointer of the current object --- */
static void next_object_record(void)
{
    G_LIST_CHUNK *new;

    if (g_chunk == NULL)
        return;
    g_chunk->count++;
    if (g_chunk->count == G_MAXCHUNKREC) {
        new = make_chunk(g_chunk->id);
        g_chunk->next = new;
        g_chunk = new;
    }
}

/* --- Get some extra memory for polygons etc --- */
static void* get_space_on_chunk(int size)
{
    int poly_pos = g_chunk->poly_pos;
    /*
     * For alignment
     */
    size = ((size+3)/4)*4;
    if (g_chunk->poly_size < g_chunk->poly_pos + size) {
        void *oldp;
        int nsize = max(g_chunk->poly_pos + size, G_POLYSPACECHUNKSIZE);
        g_chunk->poly_size += nsize;
        oldp = (void*) g_chunk->poly_space;
        g_chunk->poly_space = 
            (float*) realloc(g_chunk->poly_space, 
                             g_chunk->poly_size);
        if (!g_chunk->poly_space) {
            perror("Memory error\n");
            exit(1);
        }
        /*
         * realloc relocated the pointer, so we have
         * to recompute all addresses
         */
        if (g_chunk->poly_space != oldp) {
            int i;
            for (i=0;i<g_chunk->count;i++) {
                if (g_chunk->rec[i].p != NULL) {
                    g_chunk->rec[i].p = (void*) ((char*) g_chunk->poly_space + 
                        g_chunk->rec[i].i);
                }
            }
        }
    }
    g_chunk->poly_pos += size;
    return (void*) ((char*)g_chunk->poly_space + poly_pos);
}

/************************************************************************/
/* Object functions that can be called by the user                      */
/************************************************************************/
/* --- Open an object with the number 'id'. If 'id' exists
      return G_ERROR. All output now goes to 'id' --- */
int g_open_object(int id)
{
    if (id < 0) 
        return G_ERROR;
    if (id == 0)
        id = g_unique_object();
    else if (RB_searchTree(RBobjListID, (void *) toKey(id)) != NULL)
        return G_ERROR;
    g_chunk = make_chunk(id);
    g_chunk->parent = g_list_openobject;
    g_list_openobject = id;
    RB_insertValue(RBobjListID, (void *) toKey(id), (void*) g_chunk);
    set_listmode();
    return id;
}

int g_open_object_byname(int id, char *label)
{
    if (id < 0) 
        return G_ERROR;
    if (RB_searchTree(RBobjListID, (void *) toKey(id))  != NULL)
        return G_ERROR;
    if (RB_searchTree(RBobjBNListID, (void *) label)  != NULL)
        return G_ERROR;
    g_chunk = make_chunk(id);
    g_chunk->parent = g_list_openobject;
    g_chunk->label = (char*) malloc(strlen(label) + 1);
    strcpy(g_chunk->label, label);
    g_list_openobject = id;
    RB_insertValue(RBobjListID, (void *) toKey(id), (void*) g_chunk);
    RB_insertValue(RBobjBNListID, (void *) label, (void*) id);
    set_listmode();
    return id;
}

/* --- Close the object 'id'. All output now goes to the device --- */
int g_close_object(int id)
{
    G_LIST_CHUNK *c;
    int id_parent;

    if (g_list_openobject == G_ERROR)
        return G_ERROR;
    if ((c=(G_LIST_CHUNK*) RB_searchTree(RBobjListID, (void *) toKey(id)))== NULL)
        return G_ERROR;
    /* --- if this is not the last open object, do not close --- */
    if (g_list_openobject != id)
        return G_ERROR;
    /* --- get the parent object --- */
    id_parent = c->parent;
    c->parent = G_ERROR;
    if ((id_parent != G_ERROR)  && 
              (c=(G_LIST_CHUNK*) RB_searchTree(RBobjListID, (void *) toKey(id_parent))) != NULL) {
        g_list_openobject = id_parent;
        while (c->next)
            c = c->next;
        g_chunk = c;
    }
    else {
        g_list_openobject = G_ERROR;
        g_chunk = NULL;
        INTERNAL_set_directmode();
    }
    return id;
}

int g_close_object_byname(char *label)
{
    int id;

    if ((id=g_get_object_id(label)) == G_ERROR)
        return G_ERROR;
    return g_close_object(id);
}

int g_get_object_id(char *label)
{
    int result;
    if ((result = (int) RB_searchTree(RBobjBNListID, (void *) label)) == 0)
        return G_ERROR;
    return result;
}

char *g_get_object_name(int id)
{
    G_LIST_CHUNK *c;
    if ((c=(G_LIST_CHUNK*) RB_searchTree(RBobjListID, (void *)toKey(id)))== NULL)
        return NULL;
    return c->label;
}


/* --- Open an existing object 'id' for appending --- */
int g_append_object(int id)
{
    G_LIST_CHUNK *c;
    int isopen;

    if ((c=(G_LIST_CHUNK*) RB_searchTree(RBobjListID, (void *)toKey(id))) == NULL)
        return G_ERROR;
    /* --- can not append to object that is already open --- */
    if (c->parent != G_ERROR)
        return G_ERROR;
    (g_list_openobject == G_ERROR) ? (isopen = FALSE) : (isopen = TRUE);
    c->parent = g_list_openobject;
    g_list_openobject = id;
    while (c->next)
        c = c->next;
    g_chunk = c;
    if (!isopen)
        set_listmode();
    return id;
}

int g_append_object_byname(char *label)
{
    int id;

    if ((id=g_get_object_id(label)) == G_ERROR)
        return G_ERROR;
    return g_append_object(id);
}

/* --- Delete the object 'id' --- */
int g_delete_object(int id)
{
    G_LIST_CHUNK *c;

    if ((c=(G_LIST_CHUNK*) RB_searchTree(RBobjListID, (void *)toKey(id))) == NULL)
        return G_ERROR;
    /* ---is object open ?? --- */
    if (c->parent != G_ERROR)
        /* --- is it the current open-object --- */
        if (g_list_openobject != id)
            /* --- if not, get out --- */
            return G_ERROR;
    if (g_chunk == c)
        g_close_object(id);
    if (c->label) {
        RB_deleteValue(RBobjBNListID, (void *) c->label);
    }
    kill_list(c);
    RB_deleteValue(RBobjListID, (void*) toKey(id));
    return id;
}

int g_delete_object_byname(char *label)
{
    int id;

    if ((id=g_get_object_id(label)) == G_ERROR)
        return G_ERROR;
    return g_delete_object(id);
}

/* --- Return 'id' if 'id' exists, G_ERROR otherwise ---*/
int g_is_object(int id)
{
    if (RB_searchTree(RBobjListID, (void *)toKey(id)) == NULL)
        return G_ERROR;
    return id;
}


/* --- Return an unused object number --- */
int g_unique_object(void)
{
    static int id = 10000;

    do {
        id++;
    }
    while (RB_searchTree(RBobjListID, (void *)toKey(id)) != NULL);
    return id;
}


/* --- Send the plotting commands from object 'id' to the device --- */
int g_DIRECT_call_object(int id)
{
    G_LIST_CHUNK  *c, *c_old;
    G_LIST_RECORD *g;
    int i;
    static int nesting;

    /* --- In DIRECT mode now, thus all objects are closed --- */
    if ((c=(G_LIST_CHUNK*)RB_searchTree(RBobjListID,(void*)toKey(id))) == NULL)
        return G_ERROR;
    c_old = c;
    if (c->play)
        return G_ERROR;
    if (!nesting && !INTERNAL_object_playback_flag)
        INTERNAL_add_list2history(id);
    if (c->status == G_SLEEP)
        return id;
    c->play = TRUE;
    if (!nesting && !(GWIN.flag & G_WIN_BUFFER)){
        INTERNAL_hide_cursor_crosshair(GD.win_id);
    }
    nesting++;
    while (c != NULL) {
        for (i=0;i<c->count;i++) {
            g=&c->rec[i];
            g->func(g);
        }
        c = c->next;
    }
    nesting--;
    if (!nesting && !(GWIN.flag & G_WIN_BUFFER)){
        INTERNAL_show_cursor_crosshair(GD.win_id);
    }
    c_old->play = FALSE;
    return id;
}

int g_DIRECT_call_object_byname(char *label)
{
    int id;

    if ((id=g_get_object_id(label)) == G_ERROR)
        return G_ERROR;
    return g_DIRECT_call_object(id);
}

int g_read_fileobject(int file_id, int obj_id, char *label, 
                      int with_children, int replace)
{
    G_DISK_LIST_RECORD *dlr;
    long position, size; 
    int id, pos, old_pos;
    BYTE *where;
    static int nesting;

    if ((size=INTERNAL_read_object_file(file_id, label, (BYTE**) &dlr))==G_ERROR)
        return G_ERROR;
    position = size / sizeof(G_DISK_LIST_RECORD);        
    if ((id = g_get_object_id(label)) != G_ERROR) {
        if (replace)
            g_delete_object(id);
        else
            return G_ERROR;
        if (obj_id == 0)
            obj_id = id;
    }
    if (obj_id == 0)
        obj_id = g_unique_object();
    if (g_open_object_byname(obj_id, label) == G_ERROR)
        return G_ERROR;
    nesting++;
    for (pos=0;pos<position;pos++) {
        old_pos = pos;
        switch (dlr[pos].id) {
            case G_LIST_MOVETO: 
            case G_LIST_LINETO: {
                float x, y;
                where = (BYTE*) &(dlr[pos+1]);
                bswap4(where);
                memcpy((BYTE*) &x, where, sizeof(float));
                where += sizeof(float);
                bswap4(where);
                memcpy((BYTE*) &y, where, sizeof(float));
                switch (dlr[pos].id) {
                    case G_LIST_MOVETO:
                        g_moveto(x, y);
                        break;
                    case G_LIST_LINETO:
                        g_lineto(x, y);
                        break;
                    }
                }
                break;
            case G_LIST_LABEL:
                g_label((char*) &dlr[pos+1]);
                break;
            case G_LIST_RECTANGLE: 
            case G_LIST_FILLRECTANGLE: {
                float x1, y1, x2, y2;
                where = (BYTE*) &(dlr[pos+1]);
                bswap4(where);
                memcpy((BYTE*) &x1, where, sizeof(float));
                where += sizeof(float);
                bswap4(where);
                memcpy((BYTE*) &y1, where, sizeof(float));
                where += sizeof(float);
                bswap4(where);
                memcpy((BYTE*) &x2, where, sizeof(float));
                where += sizeof(float);
                bswap4(where);
                memcpy((BYTE*) &y2, where, sizeof(float));
                switch (dlr[pos].id) {
                    case G_LIST_RECTANGLE:
                        g_rectangle(x1, y1, x2, y2);
                        break;
                    case G_LIST_FILLRECTANGLE:
                        g_fillrectangle(x1, y1, x2, y2);
                        break;
                    }
                }
                break;
            case G_LIST_DRAWPOLY: 
            case G_LIST_FILLPOLY: {
                float f, *fp;
                long i, skip;
                where = (BYTE*) &(dlr[pos+1]);
                bswap4(where);
                memcpy((BYTE*) &skip, where, sizeof(long));
                where += sizeof(long);
                bswap4(where);
                memcpy((BYTE*) &f, where, sizeof(float));
                where += sizeof(float);
                for (i=0;i<2*(int)f;i++)
                    bswap4(where + i * sizeof(float));    
                if ((fp = (float*)malloc(2*sizeof(float)*(int)f)) == NULL) {
                    perror("Memory error\n");
                    exit(1);
                }
                memcpy((BYTE*) fp, where, 2*sizeof(float)*(int)f);
                switch (dlr[pos].id) {
                    case G_LIST_DRAWPOLY:
                        g_drawpoly((int) f, fp);
                        break;
                    case G_LIST_FILLPOLY:
                        g_fillpoly((int) f, fp);
                        break;
                    }
                free(fp);
                pos += skip; 
                }
                break;
            case G_LIST_CIRCLE: 
            case G_LIST_FILLCIRCLE: 
            case G_LIST_SET_CHARSIZE: {
                float x;
                where = (BYTE*) &(dlr[pos+1]);
                bswap4(where);
                memcpy((BYTE*) &x, where, sizeof(float));
                switch (dlr[pos].id) {
                    case G_LIST_CIRCLE:
                        g_circle(x);
                        break;
                    case G_LIST_FILLCIRCLE:
                        g_fillcircle(x);
                        break;
                    case G_LIST_SET_CHARSIZE:
                        g_set_charsize(x);
                        break;
                    }
                }
                break;
            case G_LIST_SET_TEXTDIRECTION: 
            case G_LIST_SET_LINEWIDTH: 
            case G_LIST_SET_LINESTYLE: 
            case G_LIST_SET_FOREGROUND: 
            case G_LIST_SET_BACKGROUND: 
            case G_LIST_SET_CLIPPING: 
            case G_LIST_SELECT_VIEWPORT: 
            case G_LIST_SET_PALETTESIZE: {
                long i;
                where = (BYTE*) &(dlr[pos+1]);
                bswap4(where);
                memcpy((BYTE*) &i, where, sizeof(long));
                switch (dlr[pos].id) {
                    case G_LIST_SET_TEXTDIRECTION: 
                        g_set_textdirection(i);
                        break;                    
                    case G_LIST_SET_LINEWIDTH: 
                        g_set_linewidth(i);
                        break;                    
                    case G_LIST_SET_LINESTYLE: 
                        g_set_linestyle(i);
                        break;                    
                    case G_LIST_SET_FOREGROUND: 
                        g_set_foreground(i);
                        break;                    
                    case G_LIST_SET_BACKGROUND: 
                        g_set_background(i);
                        break;
                    case G_LIST_SET_CLIPPING: 
                        g_set_clipping(i);
                        break;
                    case G_LIST_SELECT_VIEWPORT: 
                        g_select_viewport(i);
                        break;
                    case G_LIST_SET_PALETTESIZE: 
                        g_set_palettesize(i);
                        break;
                    }
                }
                break;                    
            case G_LIST_SET_FONT: {
                long i;
                float f;
                where = (BYTE*) &(dlr[pos+1]);
                bswap4(where);
                memcpy((BYTE*) &i, where, sizeof(long));
                where += sizeof(long);
                bswap4(where);
                memcpy((BYTE*) &f, where, sizeof(float));
                g_set_font(i, f);
                }
                break;
            case G_LIST_SET_WORLD: 
            case G_LIST_SET_VIEWPORT: {
                float x1, y1, x2, y2;
                long i;
                where = (BYTE*) &(dlr[pos+1]);
                bswap4(where);
                memcpy((BYTE*) &i, where, sizeof(long));
                where += sizeof(long);
                bswap4(where);
                memcpy((BYTE*) &x1, where, sizeof(float));
                where += sizeof(float);
                bswap4(where);
                memcpy((BYTE*) &y1,where, sizeof(float));
                where += sizeof(float);
                bswap4(where);
                memcpy((BYTE*) &x2, where, sizeof(float));
                where += sizeof(float);
                bswap4(where);
                memcpy((BYTE*) &y2, where, sizeof(float));
                switch (dlr[pos].id) {
                    case G_LIST_SET_WORLD:
                        g_set_world(i, x1, y1, x2, y2);
                        break;
                    case G_LIST_SET_VIEWPORT:
                        g_set_viewport(i, x1, y1, x2, y2);
                        break;
                    }
                }
                break;
            case G_LIST_SET_PALETTEENTRY: {
                G_PALETTEENTRY pal;
                long i;
                where = (BYTE*) &(dlr[pos+1]);
                bswap4(where);
                memcpy((BYTE*) &i, where, sizeof(long));
                where += sizeof(long);
                memcpy((BYTE*) &(pal.r), where, sizeof(BYTE));
                where += sizeof(BYTE);
                memcpy((BYTE*) &(pal.g), where, sizeof(BYTE));
                where += sizeof(BYTE);
                memcpy((BYTE*) &(pal.b), where, sizeof(BYTE));
                g_set_paletteentry(i, pal);
                }
                break;
            case G_LIST_CALL_OBJECT_BYNAME: {
                int new_id; 
                char *label = (char*) &dlr[pos+1];
                if (with_children && !RB_searchTree(RBobjTempListID,(void*)label)) {
                    if ((new_id = g_get_object_id(label)) == G_ERROR) 
                        new_id = g_unique_object();
                    RB_insertValue(RBobjTempListID, label, (void*) new_id);
                    g_read_fileobject(file_id, new_id, label, TRUE, replace);
                }
                g_call_object_byname(label);
                }
                break;
            case G_LIST_CLEAR_VIEWPORT:
                g_clear_viewport();
                break;
            case G_LIST_PUSH_GC:
                g_push_gc();
                break;
            case G_LIST_POP_GC:
                g_pop_gc();
                break;
            case G_LIST_PUSH_VIEWPORT:
                g_push_viewport();
                break;
            case G_LIST_POP_VIEWPORT:
                g_pop_viewport();
                break;
        }
        pos += dlr[old_pos].skip; 
    }
    g_close_object(obj_id);
    free(dlr);
    nesting--;
    if (!nesting)
        kill_templist();
    return G_OK;
}

static int calc_dlr_size(int len, G_DISK_LIST_RECORD **dlr, long *size)
{
    int count;
    
    count = (1 + (len-1) / sizeof(G_DISK_LIST_RECORD));
    *size += count;
    if ((*dlr = (G_DISK_LIST_RECORD *) 
                realloc(*dlr, *size * sizeof(G_DISK_LIST_RECORD))) == NULL) {
                    fprintf(stderr,"Memory allocation error\n");
                    exit(1);
    }
    return count;
}


/* --- Send the plotting commands from object 'id' to the device --- */
int g_write_fileobject(int file_id, char *label, int with_children, int replace)
{
    G_DISK_LIST_RECORD *dlr;
    long size, position;
    G_LIST_CHUNK  *c;
    G_LIST_RECORD *g;
    int i, obj_id;
    BYTE *where;
    static int nesting;

    if ((obj_id = g_get_object_id(label)) == G_ERROR)
        return G_ERROR;
    if ((c=(G_LIST_CHUNK*)RB_searchTree(RBobjListID,(void*)toKey(obj_id))) == NULL)
        return G_ERROR;
    if (!replace && INTERNAL_find_object_file(file_id, c->label))
        return G_ERROR;
    RB_insertValue(RBobjTempListID, (void*) label, (void*)obj_id);
    size = c->count;
    if ((dlr = (G_DISK_LIST_RECORD *) 
            malloc(size * sizeof(G_DISK_LIST_RECORD))) == NULL) {
                fprintf(stderr,"Memory allocation error\n");
                exit(1);
    }
    position = 0;   
    nesting++;
    while (c != NULL) {
        for (i=0;i<c->count;i++) {
            g=&(c->rec[i]);
            dlr[position].id = g->id;
            switch (g->id) {
                case G_LIST_MOVETO:
                case G_LIST_LINETO:
                    dlr[position].skip = 
                        calc_dlr_size(2 * sizeof(float), &dlr, &size);
                    where = (BYTE*)&(dlr[position+1]);
                    memcpy(where, (BYTE*) &(g->x[0]), sizeof(float));
                    bswap4(where);
                    where += sizeof(float);
                    memcpy(where, (BYTE*) &(g->x[1]), sizeof(float));
                    bswap4(where);
                    position += dlr[position].skip;
                    break;
                case G_LIST_LABEL:
                    dlr[position].skip = 
                        calc_dlr_size(strlen((char*)g->p)+1, &dlr, &size);
                    memcpy((BYTE*)&(dlr[position+1]), 
                           (BYTE*) g->p, strlen((char*)g->p)+1);
                    position += dlr[position].skip;
                    break;
                case G_LIST_FILLRECTANGLE:
                case G_LIST_RECTANGLE:
                    dlr[position].skip = 
                        calc_dlr_size(4 * sizeof(float), &dlr, &size);
                    where = (BYTE*)&(dlr[position+1]);
                    memcpy(where,(BYTE*) &(g->x[0]), sizeof(float));
                    bswap4(where);
                    where += sizeof(float);
                    memcpy(where, (BYTE*) &(g->x[1]), sizeof(float));
                    bswap4(where);
                    where += sizeof(float);
                    memcpy(where, (BYTE*) &(g->x[2]), sizeof(float));
                    bswap4(where);
                    where += sizeof(float);
                    memcpy(where, (BYTE*) &(g->x[3]), sizeof(float));
                    bswap4(where);
                    position += dlr[position].skip;
                    break;
                case G_LIST_FILLPOLY: 
                case G_LIST_DRAWPOLY: 
                    {
                    long i, skip, num;
                    dlr[position].skip = 0;
                    skip = calc_dlr_size(sizeof(long) + sizeof(float) +
                                            ((int)g->x[0]) * sizeof(float) * 2, 
                                         &dlr, &size);
                    where = (BYTE*)&(dlr[position+1]);
                    memcpy(where, (BYTE*) &skip, sizeof(long));
                    bswap4(where);
                    where += sizeof(long);
                    num = 2 * (long) g->x[0];
                    memcpy(where, (BYTE*) &(g->x[0]), sizeof(float));
                    bswap4(where);
                    where += sizeof(float);
                    memcpy(where,(BYTE*) g->p, ((int)g->x[0]) * sizeof(float) * 2);
                    for (i=0;i< num;i++, where += sizeof(float))
                        bswap4(where);
                    position += skip;
                    }
                    break;
                case G_LIST_FILLCIRCLE:
                case G_LIST_CIRCLE:
                case G_LIST_SET_CHARSIZE:
                    dlr[position].skip = 
                        calc_dlr_size(sizeof(float), &dlr, &size);
                    where = (BYTE*)&(dlr[position+1]);
                    memcpy(where, (BYTE*) &(g->x[0]), sizeof(float));
                    bswap4(where);
                    position += dlr[position].skip;
                    break;
                case G_LIST_SET_TEXTDIRECTION:
                case G_LIST_SET_LINEWIDTH:
                case G_LIST_SET_LINESTYLE:
                case G_LIST_SET_FOREGROUND:
                case G_LIST_SET_BACKGROUND:
                case G_LIST_SET_CLIPPING:
                case G_LIST_SELECT_VIEWPORT:
                case G_LIST_SET_PALETTESIZE:
                    dlr[position].skip = 
                        calc_dlr_size(sizeof(long), &dlr, &size);
                    where = (BYTE*)&(dlr[position+1]);
                    memcpy(where, (BYTE*) &(g->i), sizeof(long));
                    bswap4(where);
                    position += dlr[position].skip;
                    break;                    
                case G_LIST_SET_FONT:
                    dlr[position].skip = 
                        calc_dlr_size(sizeof(float)+ sizeof(long), &dlr, &size);
                    where = (BYTE*)&(dlr[position+1]);
                    memcpy(where, (BYTE*) &(g->x[0]), sizeof(float));
                    bswap4(where);
                    where += sizeof(float);
                    memcpy(where, (BYTE*) &(g->i), sizeof(long));
                    bswap4(where);
                    position += dlr[position].skip;
                    break;
                case G_LIST_SET_WORLD:
                case G_LIST_SET_VIEWPORT:
                    dlr[position].skip = 
                        calc_dlr_size(4 * sizeof(float) + sizeof(long), 
                                      &dlr, &size);
                    where = (BYTE*)&(dlr[position+1]);
                    memcpy(where, (BYTE*) &(g->i), sizeof(long));
                    bswap4(where);
                    where += sizeof(long);
                    memcpy(where, (BYTE*) &(g->x[0]), sizeof(float));
                    bswap4(where);
                    where += sizeof(float);
                    memcpy(where, (BYTE*) &(g->x[1]), sizeof(float));
                    bswap4(where);
                    where += sizeof(float);
                    memcpy(where, (BYTE*) &(g->x[2]), sizeof(float));
                    bswap4(where);
                    where += sizeof(float);
                    memcpy(where, (BYTE*) &(g->x[3]), sizeof(float));
                    bswap4(where);
                    position += dlr[position].skip;
                    break;
                case G_LIST_SET_PALETTEENTRY: {
                    G_PALETTEENTRY pal;
                    pal.r = (BYTE) g->x[0];
                    pal.g = (BYTE) g->x[1];
                    pal.b = (BYTE) g->x[2];
                    dlr[position].skip = 
                        calc_dlr_size(2 * sizeof(long), 
                                      &dlr, &size);
                    where = (BYTE*)&(dlr[position+1]);
                    memcpy(where, (BYTE*) &(g->i), sizeof(long));
                    bswap4(where);
                    where += sizeof(long);
                    memcpy(where, (BYTE*) &(pal.r), sizeof(BYTE));
                    where += sizeof(BYTE);
                    memcpy(where, (BYTE*) &(pal.g), sizeof(BYTE));
                    where += sizeof(BYTE);
                    memcpy(where, (BYTE*) &(pal.b), sizeof(BYTE));
                    position += dlr[position].skip;
                    }
                    break;                    
                case G_LIST_CALL_OBJECT_BYNAME: 
                    dlr[position].id = G_LIST_CALL_OBJECT_BYNAME;
                    dlr[position].skip = 
                        calc_dlr_size(strlen((char*)g->p)+1, &dlr, &size);
                    memcpy((BYTE*)&(dlr[position+1]), 
                           (BYTE*) g->p, strlen((char*)g->p)+1);
                    if (with_children) 
                        if (!RB_searchTree(RBobjTempListID,(void*)g->p)) 
                            g_write_fileobject(file_id, (char*)g->p, TRUE, replace);
                    position += dlr[position].skip;
                    break;
                case G_LIST_CALL_OBJECT: {
                    G_LIST_CHUNK *c1;
                    if ((c1=(G_LIST_CHUNK*)RB_searchTree(RBobjListID,(void*)toKey(g->i))) == NULL)
                        break;
                    if (c1->label == NULL)
                        break;
                    dlr[position].id = G_LIST_CALL_OBJECT_BYNAME;
                    dlr[position].skip = 
                        calc_dlr_size(strlen(c1->label)+1, &dlr, &size);
                    memcpy((BYTE*)&(dlr[position+1]), 
                           (BYTE*) c1->label, strlen(c1->label)+1);
                    if (with_children) 
                        if (!RB_searchTree(RBobjTempListID,(void*)c1->label)) 
                            g_write_fileobject(file_id, c1->label, TRUE, replace);
                    position += dlr[position].skip;
                    } 
                    break;
                case G_LIST_CLEAR_VIEWPORT:
                case G_LIST_PUSH_GC:
                case G_LIST_POP_GC:
                case G_LIST_PUSH_VIEWPORT:
                case G_LIST_POP_VIEWPORT:
                    dlr[position].skip = 0;
                    break;
            }
            position++;
        }
        c = c->next;
        if (c) {
            size += c->count;
            if ((dlr = (G_DISK_LIST_RECORD *) 
                    realloc(dlr, size * sizeof(G_DISK_LIST_RECORD))) == NULL) {
                        fprintf(stderr,"Memory allocation error\n");
                        exit(1);
            }
        }
    }
    nesting--;
    if (INTERNAL_write_object_file(file_id, label, (BYTE*) dlr, 
                            size * sizeof(G_DISK_LIST_RECORD), TRUE)==G_ERROR) {
        free(dlr);
        return G_ERROR;
    }
    free(dlr);
    if (!nesting)
        kill_templist();
    return G_OK;
}

/* --- Set object status to G_AWAKE or G_SLEEP. If status is
       G_SLEEP, nothing happens when object is called. --- */
int g_set_objectstatus(int id, int status)
{
    G_LIST_CHUNK *c;

    if ((c=(G_LIST_CHUNK*) RB_searchTree(RBobjListID,(void*)toKey(id))) == NULL)
        return G_ERROR;
    if (status != G_SLEEP && status != G_AWAKE)
        return G_ERROR;
    c->status = status;
    return G_OK;
}

int g_set_objectstatus_byname(char *label, int status)
{
    int id;

    if ((id=g_get_object_id(label)) == G_ERROR)
        return G_ERROR;
    return g_set_objectstatus(id, status);
}

/* --- Return the number of objects in use --- */
int g_count_objects(void)
{
    return RB_numValues(RBobjListID);
}

/* --- Return the number of objects in use that have a valid label --- */
int g_count_objects_byname(void)
{
    return RB_numValues(RBobjBNListID);
}


typedef struct {
    int list_id;
    int *id;
    int *status;
} LISTTREE_TYPE;
static int listtree(void *key, void *data, void *userdata)
{
    G_LIST_CHUNK *c;
    LISTTREE_TYPE *lt = (LISTTREE_TYPE*) userdata;

    key = key;
    c = (G_LIST_CHUNK*) data;
    lt->id[lt->list_id]     = c->id;
    lt->status[lt->list_id] = c->status;
    lt->list_id++;
    return G_ERROR;
}

/* --- List all the objects and their status (G_AWAKE or G_SLEEP)
       The object numbers are returned in the array 'id', and
       the status in the array 'status'. It is the responsibility
       of the user to allocate memory for the arrays --- */
void g_list_objects(int *id, int *status)
{
    LISTTREE_TYPE lt;
    lt.list_id = 0;
    lt.id = id;
    lt.status = status;
    RB_doForAll(RBobjListID, listtree, (void*) &lt);
}

typedef struct {
    int list_id;
    char **list;
} LISTTREEBYNAME_TYPE;
static int listtree_byname(void *key, void *data, void *userdata)
{
    LISTTREEBYNAME_TYPE *lt = (LISTTREEBYNAME_TYPE*) userdata;
    data = data;
    lt->list[lt->list_id] = (char*) key;
    lt->list_id++;
    return G_ERROR;
}

/* --- List all the objects byname --- */
void g_list_objects_byname(char **list)
{
    LISTTREEBYNAME_TYPE lt;
    
    lt.list_id = 0;
    lt.list    = list;
    RB_doForAll(RBobjBNListID, listtree_byname, (void*) &lt);
}

/************************************************************************/
/* Functions that are called from a list                                */
/************************************************************************/

static void g_CALL_set_textdirection(G_LIST_RECORD *g)
{
    g_set_textdirection(g->i);
}

static void g_CALL_moveto(G_LIST_RECORD *g)
{
    g_moveto(g->x[0],g->x[1]);
}

static void g_CALL_lineto(G_LIST_RECORD *g)
{
    g_lineto(g->x[0],g->x[1]);
}

static void g_CALL_set_linewidth(G_LIST_RECORD *g)
{
    g_set_linewidth(g->i);
}

static void g_CALL_set_linestyle(G_LIST_RECORD *g)
{
    g_set_linestyle(g->i);
}

static void g_CALL_label(G_LIST_RECORD *g)
{
    g_label((char*)g->p);
}

static void g_CALL_set_font(G_LIST_RECORD *g)
{
    g_set_font(g->i, (int) g->x[0]);
}

static void g_CALL_set_charsize(G_LIST_RECORD *g)
{
    g_set_charsize(g->x[0]);
}

static void g_CALL_rectangle(G_LIST_RECORD *g)
{
    g_rectangle(g->x[0],g->x[1],g->x[2],g->x[3]);
}

static void g_CALL_drawpoly(G_LIST_RECORD *g)
{
    g_drawpoly((long) g->x[0], (float*)g->p);
}

static void g_CALL_circle(G_LIST_RECORD *g)
{
    g_circle(g->x[0]);
}

static void g_CALL_fillrectangle(G_LIST_RECORD *g)
{
    g_fillrectangle(g->x[0],g->x[1],g->x[2],g->x[3]);
}

static void g_CALL_fillpoly(G_LIST_RECORD *g)
{
    g_fillpoly((long) g->x[0], (float*)g->p);
}

static void g_CALL_fillcircle(G_LIST_RECORD *g)
{
    g_fillcircle(g->x[0]);
}

static void g_CALL_set_foreground(G_LIST_RECORD *g)
{
    g_set_foreground(g->i);
}

static void g_CALL_set_background(G_LIST_RECORD *g)
{
    g_set_background(g->i);
}

static void g_CALL_set_clipping(G_LIST_RECORD *g)
{
    g_set_clipping(g->i);
}

static void g_CALL_set_world(G_LIST_RECORD *g)
{
    g_set_world(g->i,g->x[0],g->x[1],g->x[2],g->x[3]);
}

static void g_CALL_set_viewport(G_LIST_RECORD *g)
{
    g_set_viewport(g->i, g->x[0], g->x[1], g->x[2], g->x[3]);
}

static void g_CALL_select_viewport(G_LIST_RECORD *g)
{
    g_select_viewport(g->i);
}

static void g_CALL_clear_viewport(G_LIST_RECORD *g)
{
    g=g;
    g_clear_viewport();
}

static void g_CALL_call_object(G_LIST_RECORD *g)
{
    g_call_object(g->i);
}

static void g_CALL_push_gc(G_LIST_RECORD *g)
{
    g=g;
    g_push_gc();
}

static void g_CALL_pop_gc(G_LIST_RECORD *g)
{
    g=g;
    g_pop_gc();
}

static void g_CALL_push_viewport(G_LIST_RECORD *g)
{
    g=g;
    g_push_viewport();
}

static void g_CALL_pop_viewport(G_LIST_RECORD *g)
{
    g=g;
    g_pop_viewport();
}

static void g_CALL_call_object_byname(G_LIST_RECORD *g)
{
    g_call_object_byname((char*)g->p);
}

static void g_CALL_set_palettesize(G_LIST_RECORD *g)
{
    g_set_palettesize(g->i);
}

static void g_CALL_set_paletteentry(G_LIST_RECORD *g)
{
    static G_PALETTEENTRY pal;
    pal.r = (unsigned char) g->x[0];
    pal.g = (unsigned char) g->x[1];
    pal.b = (unsigned char) g->x[2];
    g_set_paletteentry(g->i, pal);
}


/************************************************************************/
/* Functions that can be placed in a list                               */
/************************************************************************/


void g_LIST_moveto(float x, float y)
{
    LR.id   = G_LIST_MOVETO;
    LR.func = g_CALL_moveto;
    LR.x[0] = x;
    LR.x[1] = y;
    next_object_record();
}

void g_LIST_lineto(float x, float y)
{
    LR.id   = G_LIST_LINETO;
    LR.func = g_CALL_lineto;
    LR.x[0] = x;
    LR.x[1] = y;
    next_object_record();
}

void g_LIST_label(char *label)
{
    char *s;

    LR.id   = G_LIST_LABEL;
    LR.func = g_CALL_label;
    LR.i    = g_chunk->poly_pos;
    LR.p    = get_space_on_chunk(strlen(label)+1);
    strcpy((char*)LR.p,label);
    next_object_record();
}

void g_LIST_rectangle(float x1, float y1, float x2, float y2)
{
    LR.id   = G_LIST_RECTANGLE;
    LR.func = g_CALL_rectangle;
    LR.x[0] = x1;
    LR.x[1] = y1;
    LR.x[2] = x2;
    LR.x[3] = y2;
    next_object_record();
}

void g_LIST_drawpoly(long numpoint, float *points)
{
    int   size;
    float *fp;

    if (numpoint <= 0)
        return;
    size = (int) (numpoint*2*sizeof(float));
    LR.id    = G_LIST_DRAWPOLY;
    LR.func  = g_CALL_drawpoly;
    LR.x[0]  = numpoint;
    LR.i     = g_chunk->poly_pos;
    LR.p     = get_space_on_chunk(size);
    memcpy(LR.p, points,size);
    next_object_record();
}

void g_LIST_circle(float r)
{
    LR.id   = G_LIST_CIRCLE;
    LR.func = g_CALL_circle;
    LR.x[0] = r;
    next_object_record();
}

void g_LIST_fillrectangle(float x1, float y1, float x2, float y2)
{
    LR.id   = G_LIST_FILLRECTANGLE;
    LR.func = g_CALL_fillrectangle;
    LR.x[0] = x1;
    LR.x[1] = y1;
    LR.x[2] = x2;
    LR.x[3] = y2;
    next_object_record();
}


void g_LIST_fillpoly(long numpoint, float *points)
{
    int size,i,j,k;
    float *fp;

    if (numpoint <= 0)
        return;
    size = (int) (numpoint*2*sizeof(float));
    LR.id    = G_LIST_FILLPOLY;
    LR.func  = g_CALL_fillpoly;
    LR.x[0]  = numpoint;
    LR.i     = g_chunk->poly_pos;
    LR.p     = get_space_on_chunk(size);
    memcpy(LR.p, points,size);
    next_object_record();
}

void g_LIST_fillcircle(float r)
{
    LR.id   = G_LIST_FILLCIRCLE;
    LR.func = g_CALL_fillcircle;
    LR.x[0] = r;
    next_object_record();
}

void g_LIST_set_textdirection(int direction)
{
    LR.id   = G_LIST_SET_TEXTDIRECTION;
    LR.func = g_CALL_set_textdirection;
    LR.i    = direction;
    next_object_record();
}

void g_LIST_set_linewidth(int width)
{
    LR.id   = G_LIST_SET_LINEWIDTH;
    LR.func = g_CALL_set_linewidth;
    LR.i = width;
    next_object_record();
}

void g_LIST_set_linestyle(int pattern)
{
    LR.id   = G_LIST_SET_LINESTYLE;
    LR.func = g_CALL_set_linestyle;
    LR.i = pattern;
    next_object_record();
}

void g_LIST_set_foreground(int color)
{
    LR.id   = G_LIST_SET_FOREGROUND;
    LR.func = g_CALL_set_foreground;
    LR.i = color;
    next_object_record();
}

void g_LIST_set_background(int color)
{
    LR.id   = G_LIST_SET_BACKGROUND;
    LR.func = g_CALL_set_background;
    LR.i = color;
    next_object_record();
}

void g_LIST_set_charsize(float size)
{
    LR.id   = G_LIST_SET_CHARSIZE;
    LR.func = g_CALL_set_charsize;
    LR.x[0] = size;
    next_object_record();
}

void g_LIST_set_font(int font, int scale_to)
{
    LR.id   = G_LIST_SET_FONT;
    LR.func = g_CALL_set_font;
    LR.i = font;
    LR.x[0] = (float) scale_to;
    next_object_record();
}

void g_LIST_set_clipping(int clip)
{
    LR.id   = G_LIST_SET_CLIPPING;
    LR.func = g_CALL_set_clipping;
    LR.i = clip;
    next_object_record();
}

int g_LIST_set_world(int no,float x1,float y1,float x2,float y2)
{
    LR.id   = G_LIST_SET_WORLD;
    LR.func = g_CALL_set_world;
    LR.i    = no;
    LR.x[0] = x1;
    LR.x[1] = y1;
    LR.x[2] = x2;
    LR.x[3] = y2;
    next_object_record();
    return G_OK;
}

int g_LIST_set_viewport(int no,float x1,float y1,float x2,float y2)
{
    LR.id   = G_LIST_SET_VIEWPORT;
    LR.func = g_CALL_set_viewport;
    LR.i    = no;
    LR.x[0] = x1;
    LR.x[1] = y1;
    LR.x[2] = x2;
    LR.x[3] = y2;
    next_object_record();
    return G_OK;
}

int g_LIST_select_viewport(int no)
{
    LR.id   = G_LIST_SELECT_VIEWPORT;
    LR.func = g_CALL_select_viewport;
    LR.i = no;
    next_object_record();
    return G_OK;
}

void g_LIST_clear_viewport(void)
{
    LR.id   = G_LIST_CLEAR_VIEWPORT;
    LR.func = g_CALL_clear_viewport;
    next_object_record();
}


int g_LIST_call_object(int id)
{
    LR.id   = G_LIST_CALL_OBJECT;
    LR.func = g_CALL_call_object;
    LR.i = id;
    next_object_record();
    return id;
}

int g_LIST_push_gc(void)
{
    LR.id   = G_LIST_PUSH_GC;
    LR.func = g_CALL_push_gc;
    next_object_record();
    return G_OK;
}

int g_LIST_pop_gc(void)
{
    LR.id   = G_LIST_POP_GC;
    LR.func = g_CALL_pop_gc;
    next_object_record();
    return G_OK;
}

int g_LIST_push_viewport(void)
{
    LR.id   = G_LIST_PUSH_VIEWPORT;
    LR.func = g_CALL_push_viewport;
    next_object_record();
    return G_OK;
}

int g_LIST_pop_viewport(void)
{
    LR.id   = G_LIST_POP_VIEWPORT;
    LR.func = g_CALL_pop_viewport;
    next_object_record();
    return G_OK;
}

int g_LIST_call_object_byname(char *label)
{
    char *s;
    LR.id   = G_LIST_CALL_OBJECT_BYNAME;
    LR.func = g_CALL_call_object_byname;
    if ((s = (char*)malloc(strlen(label)+1)) == NULL) {
        perror("Memory error\n");
        exit(1);
    }
    strcpy(s,label);
    LR.p = (void*) s;
    next_object_record();
    return 0;
}

int g_LIST_set_palettesize(int size)
{
    LR.id   = G_LIST_SET_PALETTESIZE;
    LR.func = g_CALL_set_palettesize;
    LR.i = size;
    next_object_record();
    return G_OK;
}

int g_LIST_set_paletteentry(int entry_id, G_PALETTEENTRY entry)
{
    LR.id   = G_LIST_SET_PALETTEENTRY;
    LR.func = g_CALL_set_paletteentry;
    LR.i    = entry_id;
    LR.x[0] = (float) entry.r;
    LR.x[1] = (float) entry.g;
    LR.x[2] = (float) entry.b;
    next_object_record();
    return G_OK;
}


/************************************************************************/
/* Dynamic viewport routines                                            */
/************************************************************************/
static int RBvpListID, RBvpHisListID;


/* --- Kill all the viewports --- */
void INTERNAL_kill_viewports(void)
{
    RB_killTree(RBvpListID);
    RB_killTree(RBvpHisListID);
}

/* --- Initiate the viewport routines ---*/
void INTERNAL_init_viewports(void)
{
    RBvpListID = RB_initTree();
    RBvpHisListID = RB_initTree();
}

/* --- Create a new viewport ---*/
static VIEWPORTTYPE *make_vp(int id)
{
    VIEWPORTTYPE *new;

    if ((new = (VIEWPORTTYPE *)malloc(sizeof(VIEWPORTTYPE))) == NULL) {
        perror("Memory error\n");
        exit(1);
    }
    new->id  = id;
    new->wx1 = 0;
    new->wy1 = 0;
    new->wx2 = DEFAULT_worldx;
    new->wy2 = DEFAULT_worldy;
    new->wx  = 0;
    new->wy  = 0;
    new->cx  = 0;
    new->cy  = 0;
    new->vx1 = 0;
    new->vy1 = 0;
    new->vx2 = 1;
    new->vy2 = 1;
    return new;
}

/* --- Get an existing viewport 'id', or create a new one --- */
VIEWPORTTYPE *INTERNAL_get_viewport(int id)
{
    VIEWPORTTYPE *vp;

    if ((vp = (VIEWPORTTYPE*) RB_searchTree(RBvpListID, (void*) toKey(id))) != NULL)
        return vp;
    vp = make_vp(id);
    RB_insertValue(RBvpListID, (void*) toKey(id), (void*) vp);
    return vp;
}

/* --- Delete the viewport 'id' --- */
int INTERNAL_delete_viewport(int id)
{
    VIEWPORTTYPE *vp;

    if ((vp=(VIEWPORTTYPE*) RB_searchTree(RBvpListID, (void*) toKey(id))) == NULL)
        return G_ERROR;
    RB_deleteValue(RBvpListID, (void*) toKey(id));
    free(vp);
    return id;
}

/************************************************************************/
/* Dynamic window's history viewport routines                           */
/************************************************************************/

static int win_handle(int win_id)
{
   int id =  ((int) RB_searchTree(RBvpHisListID, (void*) toKey(win_id+1)) - 1);
   return id;
}

/* ---  Kill the viewport history for window 'win_id' --- */
void INTERNAL_kill_viewport_history(int win_id)
{
    int handle = win_handle(win_id);
    if (handle == G_ERROR)
        return;
    RB_killTree(handle);
    RB_deleteValue(RBvpHisListID, (void *) toKey(win_id+1));
}

/* ---  Initiate the viewport history for window 'win_id' --- */
void INTERNAL_init_viewport_history(int win_id)
{
    RB_insertValue(RBvpHisListID, (void*) toKey(win_id+1), (void*) (RB_initTree() + 1));
}

/* --- Clear the viewport history of window 'win_id' --- */
void INTERNAL_reset_viewport_history(int win_id)
{
    INTERNAL_kill_viewport_history(win_id);
    INTERNAL_init_viewport_history(win_id);
}

/* --- Add the viewport 'vp' to the viewport history
       of window 'win_id' --- */
void INTERNAL_add_viewport_history(int win_id, VIEWPORTTYPE *vp)
{
    int handle = win_handle(win_id);
    if (handle == G_ERROR)
        return;
    if (RB_searchTree(handle, (void*) toKey(vp->id)) == NULL) {
        RB_insertValue(handle, (void*) toKey(vp->id), (void*) vp);
    }
}

/* --- search for a valid viewport in the viewport history --- */
typedef struct {
    long xx, yy;
    float w, h;
} HISTYPE;

static int vphissearch(void *key, void *data, void *userdata)
{
    VIEWPORTTYPE *vp;
    long x1,y1,x2,y2;
    HISTYPE *hs = (HISTYPE*) userdata;
             
    if (key) {
        vp = (VIEWPORTTYPE*) data;
        x1 = (long)(round(hs->w  * vp->vx1));
        x2 = (long)(round(hs->w * (vp->vx2 - vp->vx1)));
        y1 = (long)(round(hs->h * vp->vy1));
        y2 = (long)(round(hs->h * (vp->vy2 - vp->vy1)));
        x2 += x1;
        y2 += y1;
        if (hs->xx <= x2 && hs->xx >= x1 &&
            hs->yy <= y2 && hs->yy >= y1) {
                int result;
                sscanf((char*)key, "%d", &result);
                return result;
        }
    }
    return G_ERROR;
}

/* --- search the viewport history and return the viewport
       number in which (x,y) are present. If more viewports
       are valid, the lowest number is returned. Return
       G_ERROR if no valid viewport exists --- */
int INTERNAL_in_which_viewport(int win_id, long x, long y)
{
    int local_win_id = GET_LOCAL_ID(win_id);
    HISTYPE hs;
    int handle = win_handle(win_id);
    if (handle == G_ERROR)
        return G_ERROR;
    
    if (RB_numValues(handle)) {
        hs.w = (float) G_WIN_WIDTH(local_win_id);
        hs.h = (float) G_WIN_HEIGHT(local_win_id);
        hs.xx = x;
        hs.yy = y;
        return RB_doForAll(handle, 
                 vphissearch, (void*) &hs);
    }
    return G_ERROR;
}




typedef struct {
    int list_id;
    int *vp_list;
} VPHISLISTTYPE;

static int vphislist(void *key, void *data, void *userdata)
{
    VPHISLISTTYPE *hl = (VPHISLISTTYPE*) userdata;
    int result;

    data = data;
    sscanf((char*)key, "%d", &result);
    hl->vp_list[hl->list_id] = result;
    hl->list_id++;
    return G_ERROR;
}

/* --- return the number of viewports in win_id --- */
int g_count_viewports_in_window(int win_id)
{
    int handle = win_handle(win_id);
    if (handle == G_ERROR)
        return 0;
    return RB_numValues(handle);
}

/* --- return the number of viewports in win_id,
       The viewport id's are returned in the array 'vp_list' --- */
int g_get_viewports_in_window(int win_id, int *vp_list)
{
    VPHISLISTTYPE hl;
    int handle = win_handle(win_id);
    if (handle == G_ERROR)
        return 0;
    hl.list_id = 0;
    hl.vp_list = vp_list;
    RB_doForAll(handle, vphislist, (void*) &hl);
    return hl.list_id;
}

/************************************************************************/
/* Dynamic WIN_ID routines                                              */
/************************************************************************/
static int RBwinHisListID;
static int win_id_pool = 100;

typedef struct {
    int win_id;
    int device;
    int local_id;
} WINIDTYPE;

/* --- Kill all the WIN_ID s --- */
void INTERNAL_kill_winIDs(void)
{
    RB_killTree(RBwinHisListID);
}

/* --- Initiate the WIN_ID s ---*/
void INTERNAL_init_winIDs(void)
{
    RBwinHisListID = RB_initTree();
}

/* --- Create a new, unique global win id --- */
int INTERNAL_new_global_win_id(int device, int local_id)
{
    WINIDTYPE *wi;

    if ((wi = (WINIDTYPE *)malloc(sizeof(WINIDTYPE))) == NULL) {
        perror("Memory error\n");
        exit(1);
    }
    wi->win_id = win_id_pool++;
    wi->device = device;
    wi->local_id = local_id;
    RB_insertValue(RBwinHisListID, (void*) toKey(wi->win_id), (void*) wi);
    return wi->win_id;
}

/* --- Get an existing local win_id--- */
int INTERNAL_get_local_win_id(int win_id)
{
    WINIDTYPE *wi;

    if ((wi = (WINIDTYPE*) RB_searchTree(RBwinHisListID, (void*) toKey(win_id))) != NULL)
        return wi->local_id;
    return G_ERROR;
}

int INTERNAL_get_local_device(int win_id)
{
    WINIDTYPE *wi;

    if ((wi = (WINIDTYPE*) RB_searchTree(RBwinHisListID, (void*) toKey(win_id))) != NULL)
        return wi->device;
    return G_ERROR;
}

/* --- Clear this global 'win_id' --- */
void INTERNAL_delete_win_id(int win_id)
{
    WINIDTYPE *wi;

    if ((wi=(WINIDTYPE*) RB_searchTree(RBwinHisListID, (void*) toKey(win_id))) == NULL)
        return;
    RB_deleteValue(RBwinHisListID, (void*) toKey(win_id));
    free(wi);
}

