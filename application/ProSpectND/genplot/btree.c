/*****************************************************************************/
/*                                   BTREE.C                                 */
/*  Index manager using B-tree index files, supporting multiple inverted     */
/*  indexes into the relational data base flat files.                        */
/*  The index files of one data file are combined in one index file          */
/*  (1990) Albert van Kuik                                                   */
/*****************************************************************************/
#include <stdarg.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#include "btree.h"

#ifdef DEBUG
#include "mshell.h"
#endif

/*#define UPPERCASE*/		/* All indexes are converted to uppercase */


/************************************************************************/


typedef struct {
    int  num_index;
    RPTR *currnode;	/* node number of current key           */
    int  *currkno;	/* key number of current key            */
} CURRPOS;

static BTREE trnode;		/* BTREE node buffer                    */
static CURRPOS *currpos;
static int btree_count;

#define MAXNODEBUF	10
typedef struct {
    RPTR	ad;
    BTREE	node;
} NODEBUF;

static int nodebuf_count;
static int nodebuf_point;
static NODEBUF nodebuf[MAXNODEBUF];

#define KLEN(f,k) 		hcb[(f)].bh[(k)].keylength
#define ENTLN(f,k) 		(KLEN(f,k)+ADR)
/* current header block */
#define BTHEADER(f,k)   	hcb[(f)].bh[(k)]
/* current node */
#define CURRNODE(f,k)   	currpos[(f)].currnode[(k)]
/* current key number on the current node */
#define CURRKNO(f,k)		currpos[(f)].currkno[(k)]
/* Max number of keys on node */
#define MAXNODEKEYS(f,k)	BTHEADER((f),(k)).m
#define ROOTNODE(f,k) 		BTHEADER((f),(k)).rootnode

#define ADRCPY(a,c)    		memcpy((BYTE*)&(a),(BYTE*)(c),ADR);


/* adress from key 'c' */
#define OWNADR(f,k,c) 		((RPTR*)((c)+KLEN((f),(k))))

static RPTR get_ownadr(int fp, int index, char *c)
{
    RPTR ad;
    memcpy((BYTE*)&ad, (BYTE*)(c+KLEN(fp,index)), ADR);
    return ad;
}

static void put_ownadr(int fp, int index, char *c, RPTR ad)
{
    memcpy((BYTE*)(c+KLEN(fp,index)), (BYTE*)&ad, ADR);
}

/* adress from key left from key 'c' */
#define PREVADR(c)    		((RPTR*)((c)-ADR))

static RPTR get_prevadr(char *c)
{
    RPTR ad;
    memcpy((BYTE*)&ad, (BYTE*)(c-ADR), ADR);
    return ad;
}


/* first adress on node 'node' */
static RPTR get_firstadr(int fp, int index, BTREE node)
{
    RPTR ad;
    memcpy((BYTE*)&ad, (BYTE*)(node.keyspace+KLEN(fp,index)), ADR);
    return ad;
}


static RPTR *get_nextadr(int fp, int index, RPTR *adr)
{
    char *cp = (char *) adr;
    cp += ENTLN(fp, index);
    return (RPTR *) cp; 
}

/* last adress on node 'node' */
static RPTR get_lastadr(int fp, int index, BTREE node)
{
    RPTR ad;
    memcpy((BYTE*)&ad, (BYTE*)(node.keyspace+(node.keyct*ENTLN(fp,index))-ADR), ADR);
    return ad;
}


/* current adress on node 'node' */
static RPTR get_curradr(int fp, int index, BTREE node)
{
    RPTR ad;
    memcpy((BYTE*)&ad, (BYTE*)(node.keyspace+(CURRKNO(fp,index)*ENTLN(fp,index))-ADR), ADR);
    return ad;
}


/*
b = ((RPTR *) (trnode.keyspace + ((trnode.keyct + 1) * ENTLN(fp, index)) - ADR));
*/
#define NEWADR(f,k,node)	((RPTR*)((node).keyspace+(((node).keyct+1)*ENTLN((f),(k)))-ADR))
static RPTR get_newadr(int fp, int index, BTREE node)
{
    RPTR ad;
    memcpy((BYTE*)&ad, (BYTE*)(node.keyspace+((node.keyct+1)*ENTLN(fp,index))-ADR), ADR);
    return ad;
}

#define GETNEXTKEY(f,k,c)	((c)+ENTLN((f),(k)))
#define GETPREVKEY(f,k,c)	((c)-ENTLN((f),(k)))


#define NUMBER2KEY(f,k,node,n)	((node).keyspace+(((n)-1)*ENTLN((f),(k))))

/* current keynumber */
#define KNO(f,k,c)		((c)-trnode.keyspace)/ENTLN((f),(k))
#define SPACELEFT(f,k,c)    	(trnode.keyspace+(MAXNODEKEYS((f),(k)) * ENTLN((f),(k))))-(c)
#define MALLOCNODE(n)   	if (((n)=(BTREE*)calloc(1,NODE))==(BTREE*)NULL) { \
					dfile_error(D_OM); }

/* --------- declaration of functions used in btree.c --------- */
static int btreescan(int fp, int index, RPTR * t, char *k, char **a);
static int nodescan(int fp, int index, char *keyvalue, char **nodeptr);
static int compare_keys(int fp, int index, BYTE *a, BYTE *b);
static RPTR fileaddr(int fp, int index, RPTR t, char *a);
static RPTR leaflevel(int fp, int index, RPTR * t, char **a, int *p);
static void implode(int fp, int index, BTREE * left, BTREE * right);
static void redist(int fp, int index, BTREE * left, BTREE * right);
static void adopt(int fp, int index, RPTR * ad, int kct, RPTR newp);
static RPTR newnode(int fp);
static RPTR scannext(int fp, int index, RPTR * p, char **a);
static RPTR scanprev(int fp, int index, RPTR * p, char **a);
static RPTR scanfirst(int fp, int index, RPTR * p, char **a, char *k);
static char *childptr(int fp, int index, RPTR left, RPTR parent, BTREE * btp);
static int data_in(char *c);
static void swap_node(int fp, int index, BTREE * bf);
static void swap_node_adr(int fp, int index, BTREE * bf);
static void read_node(int fp, int index, RPTR nd, BTREE * bf);
static void write_node(int fp, int index, RPTR nd, BTREE * bf);


/*---------------------------------------------------------------------------*/
/*  btree_init() initiates the btree processing.                             */
/*  fp = the filehandle of the data file                                     */
/*---------------------------------------------------------------------------*/
void btree_init(int fp)
{
    int i;

    if (btree_count <= fp) {
        currpos = (CURRPOS*) realloc(currpos, (fp + 1) * sizeof(CURRPOS));  
        for (;btree_count < fp + 1;btree_count++) {
            currpos[btree_count].num_index = 0;  
            currpos[btree_count].currnode = NULL;  
            currpos[btree_count].currkno = NULL;  
        }
    }
    currpos[fp].num_index = hcb[fp].fh.num_index;
    currpos[fp].currnode = (RPTR*) realloc(currpos[fp].currnode, 
                            hcb[fp].fh.num_index * sizeof(RPTR));     
    currpos[fp].currkno = (int*) realloc(currpos[fp].currkno, 
                          hcb[fp].fh.num_index * sizeof(int));     
}

/*---------------------------------------------------------------------------*/
/*  btree_build() creates a new index for a new data file                    */
/*  fp = the filehandle of the data file                                     */
/*  index = the number of the index key (0=primary index,1,2...num_index)    */
/*  len = the length of the index key                                        */
/*---------------------------------------------------------------------------*/
void btree_build(int fp, int index, int len)
{
    BTHEADER(fp, index).keylength = len;
    BTHEADER(fp, index).m = ((NODE - ((sizeof(long) * 2) + (ADR * 4))) / (len + ADR));
    BTHEADER(fp, index).rootnode = 0;
    BTHEADER(fp, index).leftmost = 0;
    BTHEADER(fp, index).rightmost = 0;
}


/*---------------------------------------------------------------------------*/
/*  btree_close() ends the btree processing.                                 */
/*  fp = the filehandle of the data file                                     */
/*---------------------------------------------------------------------------*/
void btree_close(int fp)
{
    int i;
    
    if (btree_count > fp && currpos) {
        if (currpos[fp].currnode) 
            free(currpos[fp].currnode);
        if (currpos[fp].currkno)
            free(currpos[fp].currkno);
        currpos[fp].num_index = 0;  
        currpos[fp].currnode = NULL;  
        currpos[fp].currkno = NULL;  
    }
    for (i=0;i<btree_count;i++)
        if (currpos[i].num_index)
            return;
    free(currpos);
    currpos = NULL;
    btree_count = 0;
}

/*---------------------------------------------------------------------------*/
/*  btree_locate() locates the index key given in buffer 'keyvalue'          */
/*  fp = file handle                                                         */
/*  index = index key number                                                 */
/*  Returns the address of the database record or 0 if not found             */
/*---------------------------------------------------------------------------*/
RPTR btree_locate(int fp, int index, char *keyvalue)
{
    int kno, fnd = FALSE;
    RPTR nodeadr, adr;
    char *a;

#ifdef UPPERCASE
    strupr(keyvalue);
#endif

    nodeadr = ROOTNODE(fp, index);
    if (nodeadr) {
	read_node(fp, index, nodeadr, &trnode);
	fnd = btreescan(fp, index, &nodeadr, keyvalue, &a);
	adr = leaflevel(fp, index, &nodeadr, &a, &kno);
	if (kno == trnode.keyct + 1) {
	    kno = 0;
	    nodeadr = trnode.rtsib;
	}
	CURRNODE(fp,index) = nodeadr;
	CURRKNO(fp,index) = kno;
    }
    return (fnd ? adr : (RPTR) 0);
}

/*---------------------------------------------------------------------------*/
/*  btree_locate_near() locates the best match for the index key given       */
/*  in buffer 'keyvalue'                                                     */
/*  fp = file handle                                                         */
/*  index = index key number                                                 */
/*  Returns the address of the database record or 0 if not found             */
/*---------------------------------------------------------------------------*/
RPTR btree_locate_near(int fp, int index, char *keyvalue)
{
    int kno;
    RPTR nodeadr, adr;
    char *a;

#ifdef UPPERCASE
    strupr(keyvalue);
#endif

    nodeadr = ROOTNODE(fp,index);
    if (nodeadr) {
	read_node(fp, index, nodeadr, &trnode);
	btreescan(fp, index, &nodeadr, keyvalue, &a);
	adr = leaflevel(fp, index, &nodeadr, &a, &kno);
	if (kno == trnode.keyct + 1) {
	    kno = 0;
	    nodeadr = trnode.rtsib;
	}
	CURRNODE(fp,index) = nodeadr;
	CURRKNO(fp,index) = kno;
    }
    return (adr);
}

/* --------- search tree --------- */
static int btreescan(int fp, int index, RPTR *adr, char *keyvalue, char **a)
{
    int nonleaf;

    do {
	if (nodescan(fp, index, keyvalue, a)) {
	    while (compare_keys(fp, index, (BYTE*)*a, (BYTE*)keyvalue) == FALSE)
		if (scanprev(fp, index, adr, a) == 0)
		    break;
	    if (compare_keys(fp, index, (BYTE*)*a, (BYTE*)keyvalue))
		scannext(fp, index, adr, a);
	    return (TRUE);
	}
	nonleaf = trnode.nonleaf;
	if (nonleaf) {
	    *adr = get_prevadr(*a); 
	    read_node(fp, index, *adr, &trnode);
	}
    } while (nonleaf);
    return (FALSE);
}

/* --------- search node --------- */
/* return TRUE if keyvalue is found on node */
static int nodescan(int fp, int index, char *keyvalue, char **nodeptr)
{
    int i;
    int result;

    *nodeptr = trnode.keyspace;
    for (i = 0; i < trnode.keyct; i++) {
	result = compare_keys(fp, index, (BYTE*)keyvalue, (BYTE*)*nodeptr);
	if (result == FALSE)
	    return (TRUE);
	if (result < 0)
	    return (FALSE);
	*nodeptr = GETNEXTKEY(fp, index, *nodeptr);
    }
    return (FALSE);
}

/* --------- compare keys --------- */
static int compare_keys(int fp, int index, BYTE *a, BYTE *b)
{
    int len = KLEN(fp, index), cm = 0;

    while (len--) {
	if (((cm = (int) *a - (int) *b) != 0) || (*a == 0 || *b == 0))
	    break;
        a++;
        b++;
    }
    return (cm);
}


/* --------- compute current file address --------- */
static RPTR fileaddr(int fp, int index, RPTR adr, char *a)
{
    RPTR curadr, tempadr;
    int kno;

    tempadr = adr;
    curadr = leaflevel(fp, index, &tempadr, &a, &kno);
    read_node(fp, index, adr, &trnode);
    return (curadr);
}

/* --------- navigate down to leaf level --------- */
static RPTR leaflevel(int fp, int index, RPTR *adr, char **a, int *kno)
{
    if (trnode.nonleaf == FALSE) {
        /*--already at a leaf--*/
	*kno = KNO(fp, index, *a) + 1;
	return get_ownadr(fp, index, *a); 
    }
    *kno = 0;
    *adr = get_ownadr(fp, index, *a); 
    read_node(fp, index, *adr, &trnode);
    *a = trnode.keyspace;
    while (trnode.nonleaf) {
	*adr = trnode.key0;
	read_node(fp, index, *adr, &trnode);
    }
    return (trnode.key0);
}

/*---------------------------------------------------------------------------*/
/*  btree_deletekey() deletes a key from the index file                      */
/*  fp = the data file                                                       */
/*  index = the index key number                                             */
/*  *x = buffer with the key you want to delete                              */
/*  ad = is the address of the matching data base record                     */
/*  Returns OK on success, ERROR otherwise                                   */
/*---------------------------------------------------------------------------*/
int btree_deletekey(int fp, int index, char *keyvalue, RPTR ad)
{
    BTREE *qp, *yp;
    int rt_len, comb;
    RPTR padr, adr, qadr, yadr, zadr;
    char *a;

#ifdef UPPERCASE
    strupr(keyvalue);
#endif

    if (fp >= btree_count || fp < 0)
	return (ERROR);
    padr = ROOTNODE(fp, index);
    if (padr == 0)
	return (OK);
    read_node(fp, index, padr, &trnode);
    if (btreescan(fp, index, &padr, keyvalue, &a) == FALSE)
	return (OK);
    adr = fileaddr(fp, index, padr, a);
    while (adr != ad) {
	adr = scannext(fp, index, &padr, &a);
	if (compare_keys(fp, index, (BYTE*)a, (BYTE*)keyvalue))
	    return (OK);
    }
    if (trnode.nonleaf) {
        qadr = get_ownadr(fp, index, a);
	MALLOCNODE(qp);
	read_node(fp, index, qadr, qp);
	while (qp->nonleaf) {
	    qadr = qp->key0;
	    read_node(fp, index, qadr, qp);
	}
        /* move the left-most key from the leaf to 
           where the deleted key is 
        */
	memmove(a, qp->keyspace, KLEN(fp, index));
	write_node(fp, index, padr, &trnode);
	padr = qadr;
	memmove((char *) &trnode, (char *) qp, sizeof(trnode));
	a = trnode.keyspace;
        trnode.key0 = get_ownadr(fp, index, a);
	free(qp);
    }
    CURRNODE(fp, index) = padr;
    CURRKNO(fp, index) = KNO(fp, index, a);
    rt_len = SPACELEFT(fp, index, a);
    memmove(a, a + ENTLN(fp, index), rt_len);
    memset(a + rt_len, '\0', ENTLN(fp, index));
    trnode.keyct--;
    if (CURRKNO(fp, index) > trnode.keyct) {
	if (trnode.rtsib) {
	    CURRNODE(fp, index) = trnode.rtsib;
	    CURRKNO(fp, index) = 0;
	}
	else
	    CURRKNO(fp, index)--;
    }
    while (trnode.keyct <= MAXNODEKEYS(fp, index) / 2 
               && padr != ROOTNODE(fp, index)) {
	comb = FALSE;
	zadr = trnode.prntnode;
	MALLOCNODE(yp);
	if (trnode.rtsib) {
	    yadr = trnode.rtsib;
	    read_node(fp, index, yadr, yp);
	    if (yp->keyct + trnode.keyct < MAXNODEKEYS(fp, index) 
                    && yp->prntnode == zadr) {
		comb = TRUE;
		implode(fp, index, &trnode, yp);
	    }
	}
	if (comb == FALSE && trnode.lfsib) {
	    yadr = trnode.lfsib;
	    read_node(fp, index, yadr, yp);
	    if (yp->prntnode == zadr) {
		if (yp->keyct + trnode.keyct < MAXNODEKEYS(fp, index)) {
		    comb = TRUE;
		    implode(fp, index, yp, &trnode);
		}
		else {
		    redist(fp, index, yp, &trnode);
		    write_node(fp, index, padr, &trnode);
		    write_node(fp, index, yadr, yp);
		    free(yp);
		    return (OK);
		}
	    }
	}
	if (comb == FALSE) {
	    yadr = trnode.rtsib;
	    read_node(fp, index, yadr, yp);
	    redist(fp, index, &trnode, yp);
	    write_node(fp, index, yadr, yp);
	    write_node(fp, index, padr, &trnode);
	    free(yp);
	    return (OK);
	}
	free(yp);
	padr = zadr;
	read_node(fp, index, padr, &trnode);
    }
    if (trnode.keyct == 0) {
	ROOTNODE(fp, index) = trnode.key0;
	trnode.nonleaf = FALSE;
	trnode.key0 = 0;
	trnode.prntnode = 0;
        dfile_delete_record(fp, padr);
    }
    if (ROOTNODE(fp, index) == 0)
	BTHEADER(fp, index).rightmost =
	    BTHEADER(fp, index).leftmost = 0;
    write_node(fp, index, padr, &trnode);
    return (OK);
}

/* --------- combine two sibling nodes --------- */
static void implode(int fp, int index, BTREE * left, BTREE * right)
{
    RPTR lf_adr, rt_adr, padr;
    int rt_len, lf_len;
    char *a;
    RPTR *adr_pointer;
    BTREE *par;
    char *child;

    lf_adr = right->lfsib;
    rt_adr = left->rtsib;
    padr = left->prntnode;
    MALLOCNODE(par);
    child = childptr(fp, index, lf_adr, padr, par);
    /* --- move key from parent to end of left sibling --- */
    lf_len = left->keyct * ENTLN(fp, index);
    a = left->keyspace + lf_len;
    memmove(a, child, KLEN(fp, index));
    memset(child, '\0', ENTLN(fp, index));
    /* --- move keys from right sibling to left --- */
    adr_pointer = OWNADR(fp, index, a);
    put_ownadr(fp, index, a, right->key0);
    rt_len = right->keyct * ENTLN(fp, index);
    a = (char *) (adr_pointer + 1);
    memmove(a, right->keyspace, rt_len);
    /* --- point lower nodes to their parent --- */
    if (left->nonleaf)
	adopt(fp, index, adr_pointer, right->keyct + 1, lf_adr);
    /* -- if global key pointers -> to the right sibling change to -> left --- */
    if (CURRNODE(fp, index) == left->rtsib) {
	CURRNODE(fp, index) = right->lfsib;
	CURRKNO(fp, index) += left->keyct + 1;
    }
    /* --- update control values in left sibling node --- */
    left->keyct += right->keyct + 1;
    dfile_delete_record(fp, left->rtsib); 
    if (BTHEADER(fp, index).rightmost == left->rtsib)
	BTHEADER(fp, index).rightmost = right->lfsib;
    left->rtsib = right->rtsib;
    memset((char *) right, '\0', NODE);
    right->prntnode = 0;
    write_node(fp, index, rt_adr, right);
    /* --- point the deleted node's right brother to this left brother --- */
    if (left->rtsib) {
	read_node(fp, index, left->rtsib, right);
	right->lfsib = lf_adr;
	write_node(fp, index, left->rtsib, right);
    }
    /* --- remove key from parent node --- */
    par->keyct--;
    if (par->keyct == 0)
	left->prntnode = 0;
    else {
	rt_len = par->keyspace + (par->keyct * ENTLN(fp, index)) - child;
	memmove(child, child + ENTLN(fp, index), rt_len);
    }
    write_node(fp, index, lf_adr, left);
    write_node(fp, index, padr, par);
    free(par);
}

/*---------------------------------------------------------------------------*/
/*  btree_insertkey() inserts a key in the index file                        */
/*  fp = the data file                                                       */
/*  index = the index key number                                             */
/*  *x = buffer with the key you want to insert                              */
/*  ad = is the address of the matching data base record                     */
/*  unique = TRUE if the key has to be unique                                */
/*  Returns OK on success, ERROR otherwise                                   */
/*---------------------------------------------------------------------------*/
int btree_insertkey(int fp, int index, char *keyvalue, RPTR ad, int unique)
{
    char keyval[MXKEYLEN + 1], *a;
    BTREE *yp;
    BTREE *bp;
    int nl_flag, rt_len, kno;
    RPTR tadr, padr, sv_adr;
    RPTR *bbb;
    int lshft, rshft;

    if (fp >= btree_count || fp < 0)
	return (ERROR);
    if (!data_in(keyvalue))
        return ERROR;
/*
	return (OK);
*/  
    padr = 0;
    sv_adr = 0;
    nl_flag = 0;
    memmove(keyval, keyvalue, KLEN(fp, index));

#ifdef UPPERCASE
    strupr(keyval);
#endif

    tadr = ROOTNODE(fp, index);
    /* --- find insertion point --- */
    if (tadr) {
	read_node(fp, index, tadr, &trnode);
	if (btreescan(fp, index, &tadr, keyval, &a)) {
	    if (unique)
		return (ERROR);
	    else {
		leaflevel(fp, index, &tadr, &a, &kno);
		CURRKNO(fp, index) = kno;
	    }
	}
	else
	    CURRKNO(fp, index) = KNO(fp, index, a) + 1;
	CURRNODE(fp, index) = tadr;
    }
    /* --- insert key into leaf node --- */
    while (tadr) {
	nl_flag = 1;
	rt_len = SPACELEFT(fp, index, a);
	memmove(a + ENTLN(fp, index), a, rt_len);
	memmove(a, keyval, KLEN(fp, index));
        put_ownadr(fp, index, a, ad);
	if (trnode.nonleaf == FALSE) {
	    CURRNODE(fp, index) = tadr;
	    CURRKNO(fp, index) = KNO(fp, index, a) + 1;
	}
	trnode.keyct++;
	if (trnode.keyct <= MAXNODEKEYS(fp, index)) {
	    write_node(fp, index, tadr, &trnode);
	    return (OK);
	}
	/* --- redistribute keys between sibling nodes --- */
	lshft = FALSE;
	rshft = FALSE;
	MALLOCNODE(yp);
	if (trnode.lfsib) {
	    read_node(fp, index, trnode.lfsib, yp);
	    if (yp->keyct < MAXNODEKEYS(fp, index) 
                    && yp->prntnode == trnode.prntnode) {
		lshft = TRUE;
		redist(fp, index, yp, &trnode);
		write_node(fp, index, trnode.lfsib, yp);
	    }
	}
	if (lshft == FALSE && trnode.rtsib) {
	    read_node(fp, index, trnode.rtsib, yp);
	    if (yp->keyct < MAXNODEKEYS(fp, index) 
                    && yp->prntnode == trnode.prntnode) {
		rshft = TRUE;
		redist(fp, index, &trnode, yp);
		write_node(fp, index, trnode.rtsib, yp);
	    }
	}
	free(yp);
	if (lshft || rshft) {
	    write_node(fp, index, tadr, &trnode);
	    return (OK);
	}
	padr = newnode(fp);
	/* --- split node --- */
	MALLOCNODE(bp);
	memset((char *) bp, '\0', NODE);
	trnode.keyct = (MAXNODEKEYS(fp, index) + 1) / 2;
        bbb = NEWADR(fp, index, trnode);
        bp->key0 = get_newadr(fp, index, trnode);
	bp->keyct = MAXNODEKEYS(fp, index) - trnode.keyct;
	rt_len = bp->keyct * ENTLN(fp, index);
	a = (char *) (bbb + 1);
	memmove(bp->keyspace, a, rt_len);
	bp->rtsib = trnode.rtsib;
	trnode.rtsib = padr;
	bp->lfsib = tadr;
	bp->nonleaf = trnode.nonleaf;
	a = GETPREVKEY(fp, index, a);
	memmove(keyval, a, KLEN(fp, index));
	memset(a, '\0', rt_len + ENTLN(fp, index));
	if (BTHEADER(fp, index).rightmost == tadr)
	    BTHEADER(fp, index).rightmost = padr;
	if (tadr == CURRNODE(fp, index) && CURRKNO(fp, index) > trnode.keyct) {
	    CURRNODE(fp, index) = padr;
	    CURRKNO(fp, index) -= trnode.keyct + 1;
	}
	ad = padr;
	sv_adr = tadr;
	tadr = trnode.prntnode;
	if (tadr)
	    bp->prntnode = tadr;
	else {
	    padr = newnode(fp);
	    trnode.prntnode = padr;
	    bp->prntnode = padr;
	}
	write_node(fp, index, ad, bp);
	if (bp->rtsib) {
	    MALLOCNODE(yp);
	    read_node(fp, index, bp->rtsib, yp);
	    yp->lfsib = ad;
	    write_node(fp, index, bp->rtsib, yp);
	    free(yp);
	}
	if (bp->nonleaf)
	    adopt(fp, index, &bp->key0, bp->keyct + 1, ad);
	write_node(fp, index, sv_adr, &trnode);
	if (tadr) {
            RPTR temp_adr;
	    read_node(fp, index, tadr, &trnode);
	    a = trnode.keyspace;
	    temp_adr = trnode.key0;
	    while (temp_adr != bp->lfsib) {
		a = GETNEXTKEY(fp, index, a);
		temp_adr = get_prevadr(a);
	    }
	}
	free(bp);
    }
    /* --- new root --- */
    if (padr == 0)
	padr = newnode(fp);
    MALLOCNODE(bp);
    memset((char *) bp, '\0', NODE);
    bp->nonleaf = nl_flag;
    bp->prntnode = 0;
    bp->rtsib = 0;
    bp->lfsib = 0;
    bp->keyct = 1;
    bp->key0 = sv_adr;
    put_ownadr(fp, index, bp->keyspace, ad); 
    memmove(bp->keyspace, keyval, KLEN(fp, index));
    write_node(fp, index, padr, bp);
    free(bp);
    ROOTNODE(fp, index) = padr;
    if (nl_flag == FALSE) {
	BTHEADER(fp, index).rightmost = padr;
	BTHEADER(fp, index).leftmost = padr;
	CURRNODE(fp, index) = padr;
	CURRKNO(fp, index) = 1;
    }
    return (OK);
}

/* ---------- redistribute keys in sibling nodes ---------- */
static void redist(int fp, int index, BTREE * left, BTREE * right)
{
    int n1, n2, len;
    RPTR zadr;
    char *c, *d, *e;
    BTREE *zp;

    n1 = (left->keyct + right->keyct) / 2;
    if (n1 == left->keyct)
	return;
    n2 = (left->keyct + right->keyct) - n1;
    zadr = left->prntnode;
    MALLOCNODE(zp);
    c = childptr(fp, index, right->lfsib, zadr, zp);
    if (left->keyct < right->keyct) {
	d = left->keyspace + (left->keyct * ENTLN(fp, index));
	memmove(d, c, KLEN(fp, index));
	d += KLEN(fp, index);
	e = right->keyspace - ADR;
	len = ((right->keyct - n2 - 1) * ENTLN(fp, index)) + ADR;
	memmove(d, e, len);
	if (left->nonleaf)
	    adopt(fp, index, (RPTR *) d, right->keyct - n2, right->lfsib);
	e += len;
	memmove(c, e, KLEN(fp, index));
	e += KLEN(fp, index);
	d = right->keyspace - ADR;
	len = (n2 * ENTLN(fp, index)) + ADR;
	memmove(d, e, len);
	memset(d + len, '\0', e - d);
	if (right->nonleaf == 0 && left->rtsib == CURRNODE(fp, index))
	    if (CURRKNO(fp, index) < right->keyct - n2) {
		CURRNODE(fp, index) = right->lfsib;
		CURRKNO(fp, index) += n1 + 1;
	    }
	    else
		CURRKNO(fp, index) -= right->keyct - n2;
    }
    else {
	e = right->keyspace + ((n2 - right->keyct) * ENTLN(fp, index)) - ADR;
	memmove(e, right->keyspace - ADR, (right->keyct * ENTLN(fp, index)) + ADR);
	e -= KLEN(fp, index);
	memmove(e, c, KLEN(fp, index));
	d = left->keyspace + (n1 * ENTLN(fp, index));
	memmove(c, d, KLEN(fp, index));
	memset(d, '\0', KLEN(fp, index));
	d += KLEN(fp, index);
	len = ((left->keyct - n1 - 1) * ENTLN(fp, index)) + ADR;
	memmove(right->keyspace - ADR, d, len);
	memset(d, '\0', len);
	if (right->nonleaf)
	    adopt(fp, index, (RPTR *) (right->keyspace - ADR), 
                  left->keyct - n1, left->rtsib);
	if (left->nonleaf == FALSE)
	    if (right->lfsib == CURRNODE(fp, index) && CURRKNO(fp, index) > n1) {
		CURRNODE(fp, index) = left->rtsib;
		CURRKNO(fp, index) -= n1 + 1;
	    }
	    else if (left->rtsib == CURRNODE(fp, index))
		CURRKNO(fp, index) += left->keyct - n1;
    }
    right->keyct = n2;
    left->keyct = n1;
    write_node(fp, index, zadr, zp);
    free(zp);
}

/* --------- assign new parents to child nodes --------- */
static void adopt(int fp, int index, RPTR *node_adr, int kct, RPTR newparent)
{
    BTREE *tmp;
    RPTR adr;

    MALLOCNODE(tmp);
    while (kct--) {
        ADRCPY(adr, *node_adr);
	read_node(fp, index, adr, tmp);
	tmp->prntnode = newparent;
	write_node(fp, index, adr, tmp);
        node_adr = get_nextadr(fp, index, node_adr);
    }
    free(tmp);
}

/* --------- compute node address for a new node --------- */
static RPTR newnode(int fp)
{
    RPTR adr;
    BTREE *node;

    MALLOCNODE(node);
    adr = dfile_new_record(fp, (BYTE*) node, sizeof(BTREE));
    free(node);
    return (adr);
}

/*---------------------------------------------------------------------------*/
/*  btree_nextkey() retrieves the next key in the btree                      */
/*  fp = the data file                                                       */
/*  index = the index key number                                             */
/*  If there is no previous key, btree_firstkey() is called                  */
/*  Returns the address on success, 0 otherwise                              */
/*---------------------------------------------------------------------------*/
/* --------- next sequential key --------- */
RPTR btree_nextkey(int fp, int index)
{
    if (CURRNODE(fp, index) == 0)
	return (btree_firstkey(fp, index));
    read_node(fp, index, CURRNODE(fp, index), &trnode);
    if (CURRKNO(fp, index) == trnode.keyct) {
	if (trnode.rtsib == 0) {
	    return ((RPTR) 0);
	}
	CURRNODE(fp, index) = trnode.rtsib;
	CURRKNO(fp, index) = 0;
	read_node(fp, index, trnode.rtsib, &trnode);
    }
    else
	CURRKNO(fp, index)++;
    return get_curradr(fp, index, trnode); 
}

/*---------------------------------------------------------------------------*/
/*  btree_prevkey() retrieves the previous key in the btree                  */
/*  fp = the data file                                                       */
/*  index = the index key number                                             */
/*  If there is no previous key, btree_lastkey() is called                   */
/*  Returns the address on success, 0 otherwise                              */
/*---------------------------------------------------------------------------*/
RPTR btree_prevkey(int fp, int index)
{
    if (CURRNODE(fp, index) == 0)
	return (btree_lastkey(fp, index));
    read_node(fp, index, CURRNODE(fp, index), &trnode);
    if (CURRKNO(fp, index) == 0) {
	if (trnode.lfsib == 0) {
	    return ((RPTR) 0);
	}
	CURRNODE(fp, index) = trnode.lfsib;
	read_node(fp, index, trnode.lfsib, &trnode);
	CURRKNO(fp, index) = trnode.keyct;
    }
    else
	CURRKNO(fp, index)--;
    return get_curradr(fp, index, trnode); 
}

/*---------------------------------------------------------------------------*/
/*  btree_firstkey() retrieves the first key in the btree                    */
/*  fp = the data file                                                       */
/*  index = the index key number                                             */
/*  Returns the address on success, 0 otherwise                              */
/*---------------------------------------------------------------------------*/
RPTR btree_firstkey(int fp, int index)
{
    if (BTHEADER(fp, index).leftmost == 0)
	return ((RPTR) 0);
    read_node(fp, index, BTHEADER(fp, index).leftmost, &trnode);
    CURRNODE(fp, index) = BTHEADER(fp, index).leftmost;
    CURRKNO(fp, index) = 1;
    return get_firstadr(fp, index, trnode); 
}

/*---------------------------------------------------------------------------*/
/*  btree_lastkey() retrieves the last key in the btree                      */
/*  fp = the data file                                                       */
/*  index = the index key number                                             */
/*  Returns the address on success, 0 otherwise                              */
/*---------------------------------------------------------------------------*/
RPTR btree_lastkey(int fp, int index)
{
    if (BTHEADER(fp, index).rightmost == 0)
	return ((RPTR) 0);
    read_node(fp, index, BTHEADER(fp, index).rightmost, &trnode);
    CURRNODE(fp, index) = BTHEADER(fp, index).rightmost;
    CURRKNO(fp, index) = trnode.keyct;
    return get_lastadr(fp, index, trnode); 
}

/* --------- scan to the next sequential key --------- */
static RPTR scannext(int fp, int index, RPTR *adr, char **a)
{
    RPTR curadr;

    if (trnode.nonleaf) {
	*adr = get_ownadr(fp, index, *a); 
	read_node(fp, index, *adr, &trnode);
	while (trnode.nonleaf) {
	    *adr = trnode.key0;
	    read_node(fp, index, *adr, &trnode);
	}
	*a = trnode.keyspace;
	return get_ownadr(fp, index, *a);
    }
    *a = GETNEXTKEY(fp, index, *a);
    while (-1) {
	if ((trnode.keyspace + (trnode.keyct) * ENTLN(fp, index)) != *a)
	    return (fileaddr(fp, index, *adr, *a));
	if (trnode.prntnode == 0 || trnode.rtsib == 0)
	    break;
	curadr = *adr;
	*adr = trnode.prntnode;
	read_node(fp, index, *adr, &trnode);
	*a = trnode.keyspace;
	while (get_prevadr(*a) != curadr) 
	    *a = GETNEXTKEY(fp, index, *a);
    }
    return ((RPTR) 0);
}

/* --------- scan to the previous sequential key --------- */
static RPTR scanprev(int fp, int index, RPTR *adr, char **a)
{
    RPTR curadr;

    if (trnode.nonleaf) {
	*adr = get_prevadr(*a);
	read_node(fp, index, *adr, &trnode);
	while (trnode.nonleaf) {
	    *adr = get_lastadr(fp, index, trnode);
	    read_node(fp, index, *adr, &trnode);
	}
        *a = NUMBER2KEY(fp, index, trnode, trnode.keyct);
	return get_ownadr(fp, index, *a); 
    }
    while (-1) {
	if (trnode.keyspace != *a) {
	    *a = GETPREVKEY(fp, index, *a);
	    return (fileaddr(fp, index, *adr, *a));
	}
	if (trnode.prntnode == 0 || trnode.lfsib == 0)
	    break;
	curadr = *adr;
	*adr = trnode.prntnode;
	read_node(fp, index, *adr, &trnode);
	*a = trnode.keyspace;
	while (get_prevadr(*a) != curadr) 
	    *a = GETNEXTKEY(fp, index, *a);
    }
    return ((RPTR) 0);
}

/* --------- scan to the previous sequential key --------- */
static RPTR scanfirst(int fp, int index, RPTR *adr, char **a, char *k)
{
    RPTR curadr;
    char *b;

    b = *a;
    if (nodescan(fp, index, k, a) != 0)
	*a = b;
    if (trnode.nonleaf) {
	*adr = get_prevadr(*a); 
	read_node(fp, index, *adr, &trnode);
	while (trnode.nonleaf) {
	    if (nodescan(fp, index, k, a) == 0) 
		*adr = get_curradr(fp, index, trnode); 
	    else
		*adr = get_lastadr(fp, index, trnode); 
	    read_node(fp, index, *adr, &trnode);
	}
        *a = NUMBER2KEY(fp, index, trnode, trnode.keyct);
	return get_ownadr(fp, index, *a); 
    }
    while (-1) {
	if (trnode.keyspace != *a) {
	    *a = GETPREVKEY(fp, index, *a);
	    return (fileaddr(fp, index, *adr, *a));
	}
	if (trnode.prntnode == 0 || trnode.lfsib == 0)
	    break;
	curadr = *adr;
	*adr = trnode.prntnode;
	read_node(fp, index, *adr, &trnode);
	*a = trnode.keyspace;
	while (get_prevadr(*a) != curadr)
	    *a = GETNEXTKEY(fp, index, *a);
    }
    return ((RPTR) 0);
}


/* --------- locate pointer to child --------- */
static char *childptr(int fp, int index, RPTR left, RPTR parent, BTREE *node)
{
    char *c;

    read_node(fp, index, parent, node);
    c = node->keyspace;
    while (get_prevadr(c) != left) 
	c = GETNEXTKEY(fp, index, c);
    return (c);
}

/*---------------------------------------------------------------------------*/
/*  btree_keyval() retrieves the key value assosiated with the current       */
/*      key in the btree                                                     */
/*  fp = the data file                                                       */
/*  index = the index key number                                             */
/*  ky = the buffer in which the key value is copied                         */
/*---------------------------------------------------------------------------*/
void btree_keyval(int fp, int index, char *ky)
{
    RPTR adr, curadr;
    char *k;
    int kno;

    adr = CURRNODE(fp, index);

#ifdef UPPERCASE
    strupr(ky);
#endif

    if (adr) {
	read_node(fp, index, adr, &trnode);
	kno = CURRKNO(fp, index);
        k = NUMBER2KEY(fp, index, trnode, kno);
	while (kno == 0) {
	    curadr = adr;
	    adr = trnode.prntnode;
	    read_node(fp, index, adr, &trnode);
	    for (; kno <= trnode.keyct; kno++) {
                k = NUMBER2KEY(fp, index, trnode, kno);
		if (get_ownadr(fp, index, k) == curadr)
		    break;
	    }
	}
	memmove(ky, k, KLEN(fp, index));
    }
}

/*---------------------------------------------------------------------------*/
/*  btree_currkey() retrieves the current key in the btree                   */
/*  fp = the data file                                                       */
/*  index = the index key number                                             */
/*  Returns the address on success, 0 otherwise                              */
/*---------------------------------------------------------------------------*/
RPTR btree_currkey(int fp, int index)
{
    if (CURRNODE(fp, index)) {
	read_node(fp, index, CURRNODE(fp, index), &trnode);
	return get_curradr(fp, index, trnode);
    }
    return 0;
}

/* --------- test a string for data. return TRUE if any --------- */
static int data_in(char *c)
{
    while (*c == ' ')
	c++;
    return (*c != '\0');
}

static void swap_node(int fp, int index, BTREE * bf)
{
    bf->nonleaf  = lswap4(bf->nonleaf);
    bf->prntnode = lswap4(bf->prntnode);
    bf->lfsib    = lswap4(bf->lfsib);
    bf->rtsib    = lswap4(bf->rtsib);
    bf->keyct    = lswap4(bf->keyct);
    bf->key0     = lswap4(bf->key0);
}

static void swap_node_adr(int fp, int index, BTREE * bf)
{
    int i;
    char *adr;    

    adr = bf->keyspace + KLEN(fp, index);
    for (i = 0; i < bf->keyct; i++) {
        bswap4((BYTE*)adr);        
	adr = GETNEXTKEY(fp, index, adr);
    }
}


static void write_nodebuf(RPTR ad, BTREE * bf)
{
    RPTR adr;
    int i, where;
    
    for (i=0;i<nodebuf_count;i++) {
        if (nodebuf[i].ad == ad) {
            where = i;
            break;
        }
    }
    if (i == nodebuf_count) {
        if (i < MAXNODEBUF) {
            where = i;
            nodebuf_count++;
        }
        else {
            (nodebuf_point == MAXNODEBUF-1) ? (nodebuf_point = 0) : (nodebuf_point++);
            where = nodebuf_count;
        }
    }
    nodebuf[where].ad = ad;
    memcpy((BYTE*) &(nodebuf[where].node), (BYTE*) bf, NODE);    
}

static int read_nodebuf(RPTR ad, BTREE * bf)
{
    int i;
    
    for (i=0;i<nodebuf_count;i++) {
        if (nodebuf[i].ad == ad) {
            memcpy((BYTE*) bf, (BYTE*) &(nodebuf[i].node), NODE);    
            return TRUE;
        }
    }
    return FALSE;
}

/* --------- read a btree node --------- */
static void read_node(int fp, int index, RPTR nd, BTREE * bf)
{
    if (read_nodebuf(nd, bf))
        return;
    memset((char *) bf, '\0', sizeof(BTREE));
    dfile_get_record(fp, nd, (BYTE*) bf);
    swap_node(fp, index, bf);
    swap_node_adr(fp, index, bf);
    write_nodebuf(nd, bf);
}

/* --------- write a btree node --------- */
static void write_node(int fp, int index, RPTR nd, BTREE * bf)
{
    swap_node_adr(fp, index, bf);
    swap_node(fp, index, bf);
    dfile_put_record(fp, nd, (BYTE*) bf, NODE);
    swap_node(fp, index, bf);
    swap_node_adr(fp, index, bf);
    write_nodebuf(nd, bf);
}





