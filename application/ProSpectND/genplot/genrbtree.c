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

#include "genplot.h"
#include "genrbtree.h"

#ifdef DEBUG
#include "mshell.h"
#endif

/*===========================================================================*/
/* Red Black Tree routines. key is string , data is long                     */
/*===========================================================================*/
#define RB_RED       1
#define RB_BLACK     0

typedef struct RB_node_t {
    void   *key;
    void   *data;
    int    colour;
    struct RB_node_t *l;
    struct RB_node_t *r;
} RB_node_t;

typedef struct RB_nodeControlBlock_t {
    RB_node_t *head;
    RB_node_t *tail;
    int num_items;
} RB_nodeControlBlock_t;

static RB_nodeControlBlock_t *RB_nodeList;
static RB_node_t *RB_x, *RB_g, *RB_p, *RB_gg;
static int RB_nodeCount;


static int  RB_compare(void *key1, void *key2);
static void RB_copyKey(RB_node_t *x, void *keyvalue, int keylen);
static int  RB_split( int unit, void *keyvalue);
static RB_node_t *RB_rotate(void *keyvalue, RB_node_t *y);
static void RB_treeKill(int unit, RB_node_t *x);
static int  RB_forAll(int unit, RB_node_t *x, 
                int (*func)(void *key, void *data, void *userdata),
                void *userdata);


#define RB__MAX_KEY_LENGTH	256
static int RB_compare(void *key1, void *key2)
{
    int cm,len = RB__MAX_KEY_LENGTH;
    char *k1 = (char*) key1;
    char *k2 = (char*) key2;
    
    if (!k1)
        return -1;
    if (!k2)
        return 1;
    while (len--) {
	if (((cm = (int) *k1 - (int) *k2) != 0) || (*k1 == 0 || *k2 == 0))
	    break;
        k1++;
        k2++;
    }
    return (cm);
}

static void RB_copyKey(RB_node_t *x, void *keyvalue, int keylen)
{
    char *tmp;

    if (x->key)
        free(x->key);
    if (!keyvalue) {
        x->key = NULL;
        return;
    }
    tmp = (char*) malloc(keylen*sizeof(char));
    if (tmp == NULL) {
        perror("Memory error\n");
        exit(1);
    }
    memcpy(tmp, keyvalue, keylen);
    x->key = (void*) tmp;
}



int RB_initTree(void)
{
    int unit;
    
    for (unit=0; unit<RB_nodeCount; unit++) {
        if (RB_nodeList[unit].head == NULL)
            break;
    }
    if (unit == RB_nodeCount) {
        RB_nodeCount++;
        RB_nodeList = (RB_nodeControlBlock_t*) 
            realloc(RB_nodeList, sizeof(RB_nodeControlBlock_t) * RB_nodeCount);
        if (RB_nodeList == NULL) {
            perror("Memory error\n");
            exit(1);
        }
    }
    RB_nodeList[unit].num_items=0;
        
    RB_nodeList[unit].tail = (RB_node_t *) malloc(sizeof (RB_node_t));
    if (RB_nodeList[unit].tail == NULL) {
        perror("Memory error\n");
        exit(1);
    }
    RB_nodeList[unit].tail->l      = RB_nodeList[unit].tail;
    RB_nodeList[unit].tail->r      = RB_nodeList[unit].tail;
    RB_nodeList[unit].tail->key    = NULL;
    RB_nodeList[unit].tail->colour = RB_BLACK;
    RB_nodeList[unit].tail->data   = NULL;
    RB_nodeList[unit].head = (RB_node_t *) malloc(sizeof (RB_node_t));
    if (RB_nodeList[unit].head == NULL) {
        perror("Memory error\n");
        exit(1);
    }
    RB_nodeList[unit].head->r      = RB_nodeList[unit].tail;
    RB_nodeList[unit].head->l      = NULL;
    RB_nodeList[unit].head->key    = NULL;
    RB_nodeList[unit].head->colour = RB_BLACK;
    RB_nodeList[unit].head->data   = NULL;
    return (unit);
}

int RB_insertValue(int unit, void *keyvalue, void  *data)
{
    if (keyvalue == NULL)
        return (G_ERROR);
    RB_x = RB_nodeList[unit].head;
    RB_p = RB_nodeList[unit].head;
    RB_g = RB_nodeList[unit].head;

    while (RB_x != RB_nodeList[unit].tail) {
        RB_gg = RB_g;
        RB_g  = RB_p;
        RB_p  = RB_x;
        if (RB_compare(keyvalue, RB_x->key) < 0)
            RB_x = RB_x->l;
        else 
            RB_x = RB_x->r;
    }
    RB_x = (RB_node_t *) malloc(sizeof(RB_node_t));
    if (RB_x == NULL) {
        perror("Memory error\n");
        exit(1);
    }
    RB_x->key = NULL;
    RB_copyKey(RB_x, keyvalue, strlen(keyvalue)+1);
    RB_x->data = data;
    RB_x->l = RB_nodeList[unit].tail;
    RB_x->r = RB_nodeList[unit].tail;
    if (RB_compare(keyvalue, RB_p->key) < 0)
        RB_p->l = RB_x;
    else
        RB_p->r = RB_x;

    RB_split(unit, keyvalue);
    RB_nodeList[unit].num_items++;
    return(G_OK);
}

void *RB_searchTree(int unit, void *keyvalue)
{
    RB_node_t *x;
    int result = -1;
    x = RB_nodeList[unit].head->r;
    while (x != RB_nodeList[unit].tail && 
              (result = RB_compare(keyvalue, x->key)) != 0) 
        x = (result < 0) ? x->l : x->r;
    return x->data;
}

int RB_numValues(int unit)
{
    return RB_nodeList[unit].num_items;
}

int RB_deleteValue(int unit, void *keyvalue)
{
    RB_node_t *c, *p, *x, *t;
    int result;
    RB_copyKey(RB_nodeList[unit].tail, keyvalue, strlen(keyvalue)+1);
    p = RB_nodeList[unit].head;
    x = RB_nodeList[unit].head->r;
    while ((result = RB_compare(keyvalue, x->key)) != 0) {
        p = x;
        x = (result < 0) ? x->l : x->r;
    }
    if (x == RB_nodeList[unit].tail)
        return(G_ERROR);
    t = x;
    if (t->r == RB_nodeList[unit].tail)
        x = x->l;
    else if (t->r->l == RB_nodeList[unit].tail) {
        x = x->r;
        x->l = t->l;
    }
    else {
        c = x->r;
        while (c->l->l != RB_nodeList[unit].tail)
	    c = c->l;
        x = c->l;
        c->l = x->r;
        x->l = t->l;
        x->r = t->r;
    }
    if (t->key)
        free(t->key);
    free(t);

    if (RB_compare(keyvalue, p->key) < 0) 
        p->l = x;
    else
        p->r = x;
    if (RB_nodeList[unit].head->r == RB_nodeList[unit].tail) {
        RB_nodeList[unit].head->colour = RB_BLACK;
    }
    RB_nodeList[unit].num_items--;
    return(G_OK);
}


static int RB_split( int unit, void *keyvalue)
{
    RB_x->colour    = RB_RED;
    RB_x->l->colour = RB_BLACK;
    RB_x->r->colour = RB_BLACK;
    if (RB_p->colour) {
        RB_g->colour = RB_RED;
        if ((RB_compare(keyvalue, RB_g->key) < 0) != 
                (RB_compare(keyvalue, RB_p->key) < 0))
	    RB_p = RB_rotate(keyvalue,RB_g);
        RB_x = RB_rotate(keyvalue,RB_gg);
        RB_x->colour = RB_BLACK;
    }
    RB_nodeList[unit].head->r->colour = RB_BLACK;
    return (G_OK);
}

static RB_node_t *RB_rotate(void *keyvalue, RB_node_t *y)
{
    RB_node_t *c, *gc;
    int result;
    if (RB_compare(keyvalue, y->key) < 0) 
        c = y->l;
    else
        c = y->r;
    if (RB_compare(keyvalue, c->key) < 0) {
        gc = c->l;
        c->l = gc->r;
        gc->r = c;
    }
    else {
        gc = c->r;
        c->r = gc->l;
        gc->l = c;
    }
    if (RB_compare(keyvalue, y->key) < 0)
        y->l = gc;
    else
        y->r = gc;

    return gc;
}

#ifdef gggggggggggg
static void RB_treeprint(int unit)
{
    printf ("Storage tree for unit %d\n", unit);
    if (RB_nodeList[unit].head->r == RB_nodeList[unit].tail) {
        printf ("RB_nodeList[unit].head->r = RB_nodeList[unit].tail\n");
        printf ("RB_nodeList[unit].head->l = %d\n", RB_nodeList[unit].head->l);
        if (RB_nodeList[unit].head->colour == RB_RED)
            printf ("RB_nodeList[unit].head->colour = RB_RED\n");
        else
            printf ("RB_nodeList[unit].head->colour = RB_BLACK\n");
        printf ("RB_nodeList[unit].head->key = %s\n", RB_nodeList[unit].head->key);
    }
    else
        printf ("RB_nodeList[unit].head->r = %d\n", RB_nodeList[unit].head->r);
    treeprintC(unit, RB_nodeList[unit].head->r);
}

static void treeprintC(int unit, RB_node_t *x)
{
    if (x != RB_nodeList[unit].tail) {
        treeprintC(unit, x->l);
        printnodeC(unit, x);
        treeprintC(unit, x->r);
    }
}

static void printnodeC(int unit, RB_node_t *x)
{
    printf ("unit = %d, rec = %s\n",  unit, x->key);
}

#endif


static void RB_treeKill(int unit, RB_node_t *x)
{
   if (x != RB_nodeList[unit].tail) {
      RB_treeKill(unit, x->l);
      RB_treeKill(unit, x->r);
      if (x->key)
          free(x->key);
      free(x);
   }
}

void RB_killTree(int unit)
{
    if (RB_nodeList[unit].head == NULL ||
        RB_nodeList[unit].tail == NULL)
        return;
    RB_treeKill(unit, RB_nodeList[unit].head->r);
    if (RB_nodeList[unit].head->key)
        free(RB_nodeList[unit].head->key);
    free(RB_nodeList[unit].head);
    if (RB_nodeList[unit].tail->key)
        free(RB_nodeList[unit].tail->key);
    free(RB_nodeList[unit].tail);
    RB_nodeList[unit].head = NULL;
    RB_nodeList[unit].tail = NULL;
}

static int RB_forAll(int unit, RB_node_t *x, 
            int (*func)(void *key, void *data, void *userdata), void *userdata)
{
    int result;
    if (x && x != RB_nodeList[unit].tail) {
        if ((result = RB_forAll(unit, x->l, func, userdata)) != G_ERROR)
            return result;
        if ((result = func(x->key, x->data, userdata)) != G_ERROR)
            return result;
        if ((result = RB_forAll(unit, x->r, func, userdata)) != G_ERROR)
            return result;
    }
    return G_ERROR;
}

int RB_doForAll(int unit, int (*func)(void *key, void *data, void *userdata),
                void *userdata)
{
    if (RB_nodeList[unit].head == NULL)
        return G_ERROR;
    return RB_forAll(unit, RB_nodeList[unit].head->r, func, userdata);
}

