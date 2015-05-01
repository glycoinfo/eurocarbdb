/************************************************************************/
/*                          psnd_simula.c                               */
/*                                                                      */
/* constructed from:                                                    */
/*                                                                      */
/*            **********************************************            */
/*            *                                            *            */
/*            *  PROGRAM LAOCOON 5, COMPUTER VAX/VMS/VWS   *            */
/*            *                                            *            */
/*            **********************************************            */
/*                                BY                                    */
/*       L. CASSIDEI AND O. SCIACOVELLI, DEPARTMENT OF CHEMISTRY        */
/*               UNIVERSITY OF BARI, 70100 BARI, ITALY                  */
/*                                                                      */
/*       CONVERTED FOR THE IBM PC/XT/AT BY                              */
/*               KENNETH J. TUPPER                                      */
/*               DEPT. OF CHEMISTRY                                     */
/*               INDIANA UNIVERSITY                                     */
/*               BLOOMINGTON, IN, 47405                                 */
/*                                                                      */
/*                                                                      */
/*    LAOCN-5 IS A REVISED VERSION OF THE PROGRAMS LAOCN-3 AND LAOCN-4A */
/*    FOR THE ANALYSIS OF ISOTROPIC NMR SPECTRA OF SPIN-1/2 SYSTEMS.    */
/*                                                                      */
/* 1997, Albert van Kuik                                                */
/*                                                                      */
/************************************************************************/
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#include <math.h>
#include <assert.h>

#ifdef USE_THREADS
#include <pthread.h>
#endif

#include "genplot.h"
#include "psnd.h"
#include "mathtool.h"


static void laocoon5(MBLOCK *mblk);
static void lcn5_def(SIMTYPE *st);
static void algor(MBLOCK *mblk, int verbose);
static void differ(int moa, int la, int jj, SIMTYPE *st);
static void lsq1(MBLOCK *mblk);
static int  lsq2(SIMTYPE *st);
static void itpar(MBLOCK *mblk);
static void spectr(MBLOCK *mblk, int flag);
static void pardep(MBLOCK *mblk, int show_iterated_flag);
static int siminput(MBLOCK *mblk, char *filename);
static void ass_line(float x, int ass_tr, float ass_freq, SIMTYPE *st);
static void free_all(SIMTYPE *st);

static void clear_spins_and_couplings(MBLOCK  *mblk);
static void load_spins_and_couplings(MBLOCK  *mblk);
static void clear_transition_list(MBLOCK  *mblk);
static void load_transition_list(MBLOCK  *mblk);
static void simula_set_spinsystem(MBLOCK *mblk, SIMTYPE *st, int pos);
static void popup_active_spinsystems(MBLOCK *mblk);

#ifdef NOTYET

typedef enum {
    NUCLEUS_1H = 1,
    NUCLEUS_2H,
    NUCLEUS_7Li,
    NUCLEUS_11B,
    NUCLEUS_13C,
    NUCLEUS_14N,
    NUCLEUS_15N,
    NUCLEUS_17O,
    NUCLEUS_19F,
    NUCLEUS_29Si,
    NUCLEUS_31P,
    NUCLEUS_NONE
} nucleus_ids;

static char *nucleus[] = {
    { "1H",   "2H", "7Li", "11B", "13C", 
      "14N", "15N", "17O", "19F", "29Si",
      "31P" };

static flaat spin[] = 
    { 0.5, 1.0, 1.5, 1.5, 0.5,
      1.0, 0.5, 2.5, 0.5, 0.5,
      0.5 };

#endif

char *strupr(char *s)
{
    char *p;

    p = s;
    while (*p != '\0') {
        *p = toupper(*p);
        p++;
    }
    return s;
}

#define ABS(a)	((a)>0?(a):-(a))
#define POW2(a)	((a)*(a))

#define TRANSLABEL "TRANSITIONS"

/*******
*/

/*

bino returns the intensity difference between two lines:

The intensity distribution of each line:
k =  spin quantum number * 2

thus for k = 2 (spin = 1) and 3 equiv. spins, we get 


  1  3  6  7  6  3  1 
  
what stands for

   |
  |||
  |||
  |||
 |||||
 |||||
|||||||
                         

k=1
         1  1
        1  2  1 
       1  3  3  1 
      1  4  6  4  1 
     1  5 10 10  5  1 
    1  6 15 20 15  6  1 
   1  7 21 35 35 21  7  1 
  1  8 28 56 70 56 28  8  1 
 1  9 36 84 126 126 84 36  9  1 

k=2
                               1  1  1
                            1  2  3  2  1 
                         1  3  6  7  6  3  1 
                      1  4 10 16 19 16 10  4  1 
                   1  5 15 30 45 51 45 30 15  5  1 
               1  6 21 50 90 126 141 126 90 50 21  6  1 
           1  7 28 77 161 266 357 393 357 266 161 77 28  7  1 
      1  8 36 112 266 504 784 1016 1107 1016 784 504 266 112 36  8  1 
 1  9 45 156 414 882 1554 2304 2907 3139 2907 2304 1554 882 414 156 45  9  1 
             
k=3
                         1  1  1  1
                     1  2  3  4  3  2  1 
                 1  3  6 10 12 12 10  6  3  1 
             1  4 10 20 31 40 44 40 31 20 10  4  1 
       1  5 15 35 65 101 135 155 155 135 101 65 35 15  5  1 
 1  6 21 56 120 216 336 456 546 580 546 456 336 216 120 56 21  6  1 

k=4
                            1  1  1  1  1
                      1  2  3  4  5  4  3  2  1 
                 1  3  6 10 15 18 19 18 15 10  6  3  1 
            1  4 10 20 35 52 68 80 85 80 68 52 35 20 10  4  1 
 1  5 15 35 70 121 185 255 320 365 381 365 320 255 185 121 70 35 15  5  1 

k=5
                         1  1  1  1  1  1
                  1  2  3  4  5  6  5  4  3  2  1 
           1  3  6 10 15 21 25 27 27 25 21 15 10  6  3  1 
 1  4 10 20 35 56 80 104 125 140 146 140 125 104 80 56 35 20 10  4  1 

*/


int bino(int sqn, int nnq, int pos)
{
    int h,i,j,k,n,m;
    int ia[MAXSQN2*MAXGROUP+1],ib[MAXSQN2*MAXGROUP+1];

    /*
     * spin quantum number * 2
     */
    k=sqn;
    /*
     * number of equivalent spins
     */
    m=nnq;
    n=k+1;
    ia[0] = 0;
    ia[1] = 1;
    for (i=0;i<n;i++) 
        ib[i] = 1;
    n += k;
    for (i=1;i<m;i++) {
        ia[0] = 1;
        for (j=1;j<n-1;j++) {
            ia[j]  = 0;
            for (h=0;h<=k && j>=h ;h++) {
                if (j-h >= n-k)
                    continue;
                ia[j] += ib[j-h];
/*
            printf ("%d %d %2d  %2d \n",j,j-h,ia[j],ib[j-h] );
*/
            }
        }
        ia[j] = 1;
        for (j=0;j<n;j++) {
            ib[j] = ia[j];
            /*
            printf ("%2d ",ia[j] );
            */
        }
        /*
        printf("\n");
*/

        n += k;
    }
    /*
       n-=k;

        for (j=1;j<(n+1)/2;j++) {
            printf ("%2d,",ia[j]-ia[j-1] );
        }
        printf("\n");
       
        printf("%d\n",ia[pos-1] - ia[pos-2]);
        */
    /*
     * pos = sub-matrix number
     */
    if (pos<2 || pos > (sqn*nnq+2)/2)
        return 1;
    return ia[pos-1] - ia[pos-2];
}

static int iteration_abort_by_user;

/*************************************************************************
* UNDO QUEUE
*/


static int simula_is_undo(SIMTYPE *st)
{
    return (!(st->q_undohead == st->q_undotail));
}

static int simula_is_redo(SIMTYPE *st)
{
    return (!(st->q_redo == st->q_undotail));
}

static void free_spin(SPIN_TYPE *spin, int ng)
{
    int i;

    for (i=1;i<ng;i++)
        free_fvector(spin[i].cc,1);
    free(spin+1);
}

static void free_all_spins(int ng, SIMTYPE *st)
{
    int i;
    for (i=0;i<=UNDOQMAX;i++)
        if (st->undo_queue[i] != NULL) {
            free_spin(st->undo_queue[i], ng);
            st->undo_queue[i] = NULL;
        }
}

static void simula_init_undo(int ng, SIMTYPE *st)
{
    st->q_redo = st->q_undohead = st->q_undotail = 0;
    free_all_spins(ng, st);
}

static SPIN_TYPE *copy_spin(SPIN_TYPE *spin, int ng)
{
    int i,j;
    SPIN_TYPE *result;

    result = (SPIN_TYPE*) malloc(ng * sizeof(SPIN_TYPE));
    assert(result);
    result -= 1;
    for (i=1;i<=ng;i++) {
        result[i].w   = spin[i].w;
        result[i].nng = spin[i].nng;
        result[i].iso = spin[i].iso;
        result[i].sqn = spin[i].sqn;
        result[i].sscale = spin[i].sscale;

        result[i].cc  = fvector(1, ng);
        for (j=1;j<=ng;j++) 
            result[i].cc[j] = spin[i].cc[j];
    }
    return result;
}

static int is_same_spin(SPIN_TYPE *spin1, SPIN_TYPE *spin2, int ng)
{
    int i,j;

    if (spin1 == NULL || spin2 == NULL)
        return FALSE;
    for (i=1;i<=ng;i++) {
        if (spin1[i].w   != spin2[i].w)
            return FALSE;
        if (spin1[i].nng != spin2[i].nng)
            return FALSE;
        if (spin1[i].sqn != spin2[i].sqn)
            return FALSE;
        if (spin1[i].iso != spin2[i].iso)
            return FALSE;

        for (j=1;j<=ng;j++) 
            if (spin1[i].cc[j] != spin2[i].cc[j])
                return FALSE;
    }
    return TRUE;
}


static void simula_push_undo(SPIN_TYPE *spin, int ng, SIMTYPE *st)
{
    if (spin == NULL)
        return;
    if (is_same_spin(spin, st->undo_queue[st->q_undotail], ng))
        return;
    st->q_undotail++;
    if (st->q_undotail > UNDOQMAX) 
        st->q_undotail = 0;
    if (st->q_undotail == st->q_undohead) {
        (st->q_undohead == UNDOQMAX) ? (st->q_undohead = 0) : (st->q_undohead++);
    }
    st->q_redo = st->q_undotail;
    if (st->undo_queue[st->q_undotail] != NULL)
        free_spin(st->undo_queue[st->q_undotail],ng);
    st->undo_queue[st->q_undotail] = copy_spin(spin, ng);
}

static SPIN_TYPE *simula_pop_undo(SIMTYPE *st)
{
    SPIN_TYPE *spin;
    int savetail = st->q_undotail;

    if (!simula_is_undo(st))
        return NULL;
    st->q_undotail--;
    if (st->q_undotail < 0) 
        st->q_undotail = UNDOQMAX;
    if (!simula_is_undo(st)) {
        st->q_undotail = savetail;
        return NULL;
    }
    spin = st->undo_queue[st->q_undotail];
    return spin;
}

static SPIN_TYPE *simula_pop_redo(SIMTYPE *st)
{
    SPIN_TYPE *spin;
    int savetail = st->q_undotail;

    if (!simula_is_redo(st))
        return NULL;
    st->q_undotail++;
    if (st->q_undotail > UNDOQMAX)
        st->q_undotail = 0;
    if (!simula_is_undo(st)) {
        st->q_undotail = savetail;
        return NULL;
    }
    spin = st->undo_queue[st->q_undotail];
    return spin;
}

/*
 * Put psnd commands on the stack
 */

static int popspins(int mode, SIMTYPE *st)
{
    int i, j;
    SPIN_TYPE  *rst;
    SPINSYSTEM *sps = &(st->sps[st->sps_pos]);

    if (mode == PSND_UNDO) 
        rst = simula_pop_undo(st);
    else 
        rst = simula_pop_redo(st);
    if (rst == NULL) {
        g_bell();
        return FALSE;
    }
    for (i=1;i<=sps->ng;i++) {
        sps->spin[i].w   = rst[i].w;
        sps->spin[i].nng = rst[i].nng;
        sps->spin[i].sqn = rst[i].sqn;
        sps->spin[i].iso = rst[i].iso;
        sps->spin[i].sscale = rst[i].sscale;
        for (j=1;j<=sps->ng;j++)
            sps->spin[i].cc[j] = rst[i].cc[j];
    }
    return TRUE;
}
/*********************************************
*/

/*
 * resize arrays when maxenergylevel changes
 */
static void resize_maxenergylevel(SIMTYPE *st, int **ifz, int **fzord, 
                                  int newsize)
{
    if (newsize > st->maxenergylevel) {
        st->maxenergylevel = newsize;
        (*ifz)	= resize_ivector((*ifz),  1,st->maxenergylevel);
        (*fzord)	= resize_ivector((*fzord),1,st->maxenergylevel);
        st->lfz	= resize_ivector(st->lfz,1,st->maxenergylevel);
        st->fz	= resize_imatrix(st->fz,1,st->maxspin,
                                        1,st->maxenergylevel);
        st->d	= resize_fmatrix(st->d, 1, st->maxparam,
                                         1,st->maxenergylevel);
        st->e	= resize_fvector(st->e,1,st->maxenergylevel);
        st->e2	= resize_fvector(st->e2,1,st->maxenergylevel);
    }
}

/*
 * resize arrays when maxlines changes
 */
static void resize_maxlines(SIMTYPE *st)
{
    SPINSYSTEM *sps =  &(st->sps[st->sps_pos]);

    if (sps->nl >= sps->maxlines) {
        sps->maxlines += MAXLINES;
        if (sps->maxlines > st->maxlines) {
            st->maxlines = sps->maxlines;
            st->b   = resize_fvector(st->b,  1, st->maxlines);
            st->dc  = resize_fmatrix(st->dc, 1, st->maxparam,
                                         1, st->maxlines);
        }
        sps->il  = resize_ivector(sps->il, 1, sps->maxlines);
        sps->f   = resize_fvector(sps->f,  1, sps->maxlines);
    }
}

/*
 * allocate spins
 */
static void alloc_spins(SPINSYSTEM *sps, int oldstop)
{
    int i,j,start,stop,size;

    start = 1;
    if (oldstop == 0) {
        sps->spin      = NULL;
    }
    else {
        sps->spin     += start;
    }
    stop  = sps->maxspin;
    size  = stop-start+1;
    sps->spin = (SPIN_TYPE*) realloc(sps->spin, size * sizeof(SPIN_TYPE));
    assert(sps->spin);
    sps->spin -= start;
    for (i=oldstop+1;i<=stop;i++) {
        sps->spin[i].w   = 0.0;
        sps->spin[i].nng = 1;
        sps->spin[i].sqn = 1;
        sps->spin[i].iso = 1;
        sps->spin[i].sscale = 1.0;

        sps->spin[i].cc  = fvector(1, sps->maxspin);
        for (j=1;j<=sps->maxspin;j++) 
            sps->spin[i].cc[j] = 0.0;
    }
    if (oldstop>0) for (i=1;i<=oldstop;i++) {
        sps->spin[i].cc = resize_fvector(sps->spin[i].cc, 1, sps->maxspin);		
        for (j=oldstop+1;j<=sps->maxspin;j++) {
            sps->spin[i].cc[j] = 0.0;
        }
    }
}

static void free_spins(SPINSYSTEM *sps)
{
    int i;
    
    for (i=1;i<=sps->maxspin;i++)
        free_fvector(sps->spin[i].cc,1);
    sps->spin += 1;
    free(sps->spin);
}

/*
 * resize arrays when maxspin changes
 */
static void resize_maxspin(SIMTYPE *st)
{
    SPINSYSTEM *sps =  &(st->sps[st->sps_pos]);

    if (sps->ng > sps->maxspin) {
        int i,j;
        int oldssize = sps->maxspin;
        int oldgsize = sps->maxgroup;
        sps->maxspin  = sps->ng;
        sps->maxgroup = sps->ng-1;

        if (sps->maxspin > st->maxspin) {
            st->maxspin  = sps->ng;
            st->maxgroup = sps->ng-1;
            for (i=1;i<=st->maxmatrix;i++) {
                st->submat[i].nsp = resize_ivector(st->submat[i].nsp,1,st->maxspin);
                st->submat[i].sqn = resize_ivector(st->submat[i].sqn,1,st->maxspin);
            }
            st->fz	= enlarge_imatrix(st->fz, 1,st->maxspin,
                                 oldssize,1,st->maxenergylevel);	
            st->corr2	= fvector(1,st->maxspin * st->maxspin + st->maxspin);
            st->corr3	= fvector(1,st->maxspin * st->maxspin + st->maxspin);
            st->ist	= resize_ivector(st->ist, 1,st->maxspin);
        }
        alloc_spins(sps, oldssize);

        sps->ia	= enlarge_imatrix(sps->ia,1,sps->maxgroup,
                                oldgsize,1,sps->maxparam);	
        sps->ib	= enlarge_imatrix(sps->ib,1,sps->maxgroup,
                                oldgsize,1,sps->maxparam);

        for (i=oldssize+1;i<=sps->maxspin;i++) {
            if (i <= sps->maxgroup) 
                for (j=1;j<=sps->maxparam;j++) {
                    sps->ia[i][j] = 0;
                    sps->ib[i][j] = 0;
            }
        }
    }
}


/*
 * resize arrays when maxsubmatrix changes
 *
 */
static void resize_maxsubmatrix(SIMTYPE *st, int newsize)
{
    int i;
    
    if (newsize > st->maxsubmatrix) {
        int oldmax = st->maxsubmatrix;
        st->maxsubmatrix = max(st->maxsubmatrix+MAXSUBMATRIX,newsize);
        for (i=1;i<=st->maxsubmatrix;i++)
            st->submat[i].nsub = 
                resize_ivector(st->submat[i].nsub,1,st->maxsubmatrix);
    }
}

/*
 * allocate submatrix info
 */
static void alloc_submatrices(SIMTYPE *st, int init)
{
    int i,start,stop,size,oldstop;

    start = 1;
    if (init) {
        oldstop       = 0;
        st->submat    = NULL;
    }
    else {
        oldstop       = st->maxmatrix;
        st->maxmatrix = st->iay;
        st->submat   += start;
    }
    stop  = st->maxmatrix;
    size  = stop-start+1;
    st->submat = (SUB_MATRIX_TYPE*) realloc(st->submat, 
                                            size * sizeof(SUB_MATRIX_TYPE));
    assert(st->submat);
    st->submat -= start;
    for (i=oldstop+1;i<=stop;i++) {
        st->submat[i].nsub = ivector(1, st->maxsubmatrix);
        st->submat[i].nsp  = ivector(1, st->maxspin);
        st->submat[i].sqn  = ivector(1, st->maxspin);
    }
}

static void free_submatrices(SIMTYPE *st)
{
    int i;
    for (i=1;i<=st->maxmatrix;i++) {
        free_ivector(st->submat[i].nsub, 1);
        free_ivector(st->submat[i].nsp, 1);
        free_ivector(st->submat[i].sqn, 1);
    }
    st->submat += 1;
    free(st->submat);
}


/*
 * allocate transitions
 */
static TRANSITION_TYPE *alloc_transitions(MBLOCK *mblk,
                 TRANSITION_TYPE *vec, int minx, int maxx)
{
    TRANSITION_TYPE *m;

    if (vec == NULL)
        m = (TRANSITION_TYPE*) malloc((maxx-minx+1) 
                                 * sizeof(TRANSITION_TYPE));
    else
        m = (TRANSITION_TYPE*) realloc(vec+minx, 
                   (maxx-minx+1) * sizeof(TRANSITION_TYPE));
    if (!m) {
        psnd_printf(mblk,"Memory allocation error\n");
        exit(1);
    }
    m -= minx;
    return m;
}

static void free_transitions(TRANSITION_TYPE *m, int minx)
{
    free(m+minx);
}


/*
 * resize arrays when maxparam changes
 */
static void resize_maxparam(SIMTYPE *st)
{
    SPINSYSTEM *sps =  &(st->sps[st->sps_pos]);

    if (sps->nos >= sps->maxparam) {
        int i,j,oldsize = sps->maxparam;
        sps->maxparam += MAXPARAM;
        
        if (sps->maxparam > st->maxparam) {
            st->maxparam = sps->maxparam;
            st->dc	= enlarge_fmatrix(st->dc, 1,st->maxparam,
                                  oldsize, 1,st->maxlines);
            st->corr	= resize_fvector(st->corr,1,st->maxparam);
            st->d	= enlarge_fmatrix(st->d, 1, st->maxparam, 
                                  oldsize, 1,st->maxenergylevel);
            st->lz	= resize_ivector(st->lz,1,st->maxparam);
        }
        sps->ia	= resize_imatrix(sps->ia, 1,sps->maxgroup,
                                             1,sps->maxparam);	
        sps->ib	= resize_imatrix(sps->ib, 1,sps->maxgroup,
                                             1,sps->maxparam);
        for (i=1;i<=sps->maxgroup;i++) {
            for (j=oldsize+1;j<=sps->maxparam;j++) {
                sps->ia[i][j] = 0;
                sps->ib[i][j] = 0;
            }
        }
    }
}


static void free_maxeigen(SIMTYPE *st)
{
    if (st->vec == NULL)
        return;
    free_dmatrix(st->vec,1,st->maxeigen,1,st->maxeigen);
    free_dvector(st->root,1);
    free_dvector(st->a,1);
    free_fmatrix(st->va,1,st->maxeigen,1,st->maxeigen);
    free_fmatrix(st->vb,1,st->maxeigen,1,st->maxeigen);
    free_fmatrix(st->vc,1,st->maxeigen,1,st->maxeigen);
    st->vec = NULL;
}

/*
 * allocate arrays when size of maxeigen is known
 */
static void allocate_maxeigen(SIMTYPE *st, int size)
{
    free_maxeigen(st);
    st->maxeigen = size;
    st->vec  	 = dmatrix(1,st->maxeigen,1,st->maxeigen);
    st->root 	 = dvector(1,st->maxeigen);
    st->a    	 = dvector(1,st->maxeigen + 
                     (st->maxeigen * st->maxeigen - st->maxeigen)/2);
    st->va	 = fmatrix(1,st->maxeigen,1,st->maxeigen);
    st->vb	 = fmatrix(1,st->maxeigen,1,st->maxeigen);
    st->vc	 = fmatrix(1,st->maxeigen,1,st->maxeigen);
}


/*
 * allocate arrays 
 */
static void allocate_all(MBLOCK *mblk)
{
    SIMTYPE *st;
    int i,j,k, sps_num;

    st = mblk->siminf->st;
    sps_num = st->sps_num;
    sps_num = min(sps_num, MAXSPINSYSTEM);
    sps_num = max(sps_num, 1);
    st->sps_num = sps_num;
  
    if (!st->init) {
        st->maxspin		= MAXSPIN;
        st->maxgroup		= MAXGROUP;
        st->maxeigen		= MAXEIGEN;
        st->maxmatrix		= MAXMATRIX;
        st->maxsubmatrix	= MAXSUBMATRIX;
        st->maxenergylevel	= MAXENERGYLEVEL;
        st->maxtransitions	= MAXTRANSITIONS;
        st->maxlines		= MAXLINES;
        st->maxparam		= MAXPARAM;
    }
    for (k=0;k<sps_num;k++) {
        SPINSYSTEM *sps;
        sps   = &(st->sps[k]);
        if (sps->init)
            continue;
        sps->init = TRUE;
        sps->maxspin		= MAXSPIN*2;
        sps->maxgroup		= MAXGROUP*2;
        sps->maxtransitions	= MAXTRANSITIONS*2;
        sps->maxlines		= MAXLINES*2;
        sps->maxparam		= MAXPARAM*2;
        alloc_spins(sps, 0);
    
        sps->il	= ivector(1,sps->maxlines);	
        sps->f	= fvector(1,sps->maxlines);
        sps->ia	= imatrix(1,sps->maxgroup,1,sps->maxparam);	
        sps->ib	= imatrix(1,sps->maxgroup,1,sps->maxparam);

        sps->trans   = alloc_transitions(mblk, NULL, 1, sps->maxtransitions);
   
        for (i=1;i<=sps->maxspin;i++) {
            if (i <= sps->maxgroup) 
                    for (j=1;j<=sps->maxparam;j++) {
                sps->ia[i][j] = 0;
                sps->ib[i][j] = 0;
            }
        }
    }
    if (st->init)
        return;
    st->init = TRUE;
    st->er1  = 0;
    
    st->e	= fvector(1,st->maxenergylevel);
    st->e2	= fvector(1,st->maxenergylevel);
    alloc_submatrices(st, TRUE);
    st->lfz	= ivector(1,st->maxenergylevel);		
    st->fz	= imatrix(1,st->maxspin,1,st->maxenergylevel);	
    st->b	= fvector(1,st->maxlines);
    st->dc	= fmatrix(1,st->maxparam,1,st->maxlines);
    st->corr	= fvector(1,st->maxparam);
    st->corr2	= fvector(1,st->maxspin * st->maxspin + st->maxspin);
    st->corr3	= fvector(1,st->maxspin * st->maxspin + st->maxspin);
    st->d	= fmatrix(1,st->maxparam,1,st->maxenergylevel);
    st->lz	= ivector(1,st->maxparam);
    st->ist	= ivector(1,st->maxspin);
    
    st->vec     = NULL;
    /* initialized in algor() 
    st->vec  	= dmatrix(1,st->maxeigen,1,st->maxeigen);
    st->root 	= dvector(1,st->maxeigen);
    st->a    	= dvector(1,st->maxeigen+(st->maxeigen*st->maxeigen-st->maxeigen)/2);
    st->va	= fmatrix(1,st->maxeigen,1,st->maxeigen);
    st->vb	= fmatrix(1,st->maxeigen,1,st->maxeigen);
    st->vc	= fmatrix(1,st->maxeigen,1,st->maxeigen);
    */

    st->kt1 		= (MSTYPE*) malloc(sizeof(MSTYPE));
    assert(st->kt1);
    st->kt1->m 		= NULL;
    st->kt1->size 	= 0;
    st->kt1->pos 	= 0;
    st->kt2 		= (MSTYPE*) malloc(sizeof(MSTYPE));
    assert(st->kt2);
    st->kt2->m 		= NULL;
    st->kt2->size 	= 0;
    st->kt2->pos 	= 0;
  
}

static void free_all(SIMTYPE *st)
{
    SPINSYSTEM *sps;
    int i,j, sps_num;

    sps_num = st->sps_num;
    sps_num = min(sps_num, MAXSPINSYSTEM);
    sps_num = max(sps_num, 1);
    for (i=0;i<sps_num;i++) {
        sps   = &(st->sps[i]);
        if (!sps->init)
            continue;
        sps->init = FALSE;

        free_spins(sps);
        free_ivector(sps->il, 1);	
        free_fvector(sps->f, 1);
        free_imatrix(sps->ia,1,sps->maxgroup,1,sps->maxparam);	
        free_imatrix(sps->ib,1,sps->maxgroup,1,sps->maxparam);
        free_transitions(st->sps[i].trans, 1);
    }
    free_fvector(st->e, 1);	
    free_fvector(st->e2, 1);	
    free_submatrices(st);
    free_ivector(st->lfz, 1);		
    free_imatrix(st->fz	,1,st->maxspin,1,st->maxenergylevel);	
    free_fvector(st->b, 1);
    free_fmatrix(st->dc,1,st->maxparam,1,st->maxlines);
    free_fvector(st->corr, 1);	
    free_fvector(st->corr2, 1);	
    free_fvector(st->corr3, 1);	
    free_fmatrix(st->d,1,st->maxparam,1,st->maxenergylevel);
    free_ivector(st->lz, 1);
    free_ivector(st->ist, 1);

    /*
     * See if this has been initialized already
     */
    if (st->vec) 
        free_maxeigen(st);

    if (st->kt1->m) {
        free(st->kt1->m);
        st->kt1->m = NULL;
        st->kt1->size = 0;
        st->kt1->pos = 0;
    }
    free(st->kt1);
    if (st->kt2->m) {
        free(st->kt2->m);
        st->kt2->m = NULL;
        st->kt2->size = 0;
        st->kt2->pos = 0;
    }
    free(st->kt2);
    st->init = FALSE;
}

static void lcn5_def(SIMTYPE *st)
{
    int i;
/*    
 *
 *    routine to set defaults for nmr simulation by laocoon5
 *
 */
    st->init      = FALSE;
    st->has_data  = FALSE;
    st->ppm       = TRUE;
    st->igauss    = LORENTZIAN;
    for (i=0;i<MAXSPINSYSTEM;i++) {
        st->sps[i].ng     = 0;		/* number of groups (of spins)	*/
        st->sps[i].nl     = 0;		/* number of assigned lines for iter	*/
        st->sps[i].nos    = 0;
        st->sps[i].init   = FALSE;
        st->sps[i].scale  = 1.0;        
        st->sps[i].active = FALSE;        
    }
    st->sps[0].active = TRUE;        
    st->sps_num   = 1;
    st->sps_pos   = 0;
    st->ni   = 2;		/* max number of iteration cylces	*/
    st->nfc  = 1000;		/* max number of function calls	*/
    st->npop = 1;		/* max number of experiment cylces	*/
    st->pop  = 50;		/* population size	*/
    st->dev  = 5.0;
    st->child  = 6;
    st->radius = 5;		/* */
    st->show_iterations = SHOW_BEST;
    st->sticky = TRUE;
    st->amin = 0.1; 		/* int tresholt	*/
    st->width= 1.0;		/* line width in plot	*/
    st->size = 4096;       	/* size of trial spectrum	*/
    st->sw   = 2500.0;		/* sweep width	*/
    st->sf   = 500.137;
    st->xref = st->size;
    st->aref = 1.0;
    st->bref = 1.0;
    st->td   = 4096;
}

static void sort_lines(SPINSYSTEM *sps)
{
    int j;
    
    /*
     *  keep lines sorted
     */
    for (j=1;j<=sps->nl-1;j++) {         
        float fm;
        int k,kn;
        int ilsm = sps->il[j];
        int m = 0;
        for (k=j+1;k<=sps->nl;k++) {
            if (sps->il[k] >= ilsm) 
                continue;
            ilsm = sps->il[k];
            m = k;
        }
        if (m == 0) 
            continue;
        fm = sps->f[j];
        sps->f[j] = sps->f[m];
        sps->f[m] = fm;
        kn = sps->il[j];
        sps->il[j] = ilsm;
        sps->il[m] = kn;
    }
}

static void ass_line(float x, int ass_tr, float ass_freq, SIMTYPE *st)
{
    int j, assigned;
    int ass_index, tr_no;
    float  afreq;
    SPINSYSTEM *sps = &(st->sps[st->sps_pos]);
    /*
     * non interactive assignment
     */
    assigned = FALSE;
    afreq = ass_freq;
    tr_no = ass_tr;
    if (tr_no != 0) {
        /*
         *  check wether transition i is already assigned
         */
        for (j=1;j<=sps->nl;j++) {
            if (tr_no == sps->il[j]) {
                assigned  = TRUE;
                ass_index = j;
            }
        }
        /*
         *   ppm or hz  ?
         * neeeeeee
        if (sps->ppm) 
            afreq *= sps->sf;
        */
        if (assigned) 
            sps->f[ass_index] = afreq;
        else {
            if (sps->nl >= sps->maxlines) 
                resize_maxlines(st);
            sps->nl++;
            sps->il[sps->nl]  = tr_no;
            sps->f[sps->nl]   = afreq;
        }
    }
    sort_lines(sps);
}

static void remove_line(int line_no, int ass_tr, SPINSYSTEM *sps)
{
    int i;
    
    if (line_no <= 0) {
        for (i=1;i<sps->nl;i++) {
            if (ass_tr == sps->il[i]) {
                line_no = i;
                break;
            }
        }
        if (line_no <= 0)
            return;
    }
    for (i = line_no;i<sps->nl;i++) {
        sps->il[i] = sps->il[i+1];
        sps->f[i]  = sps->f[i+1];
    }
    sps->nl--;
}

static char *lineshapes[] = {
    "LORENTZIAN",
    "GAUSSIAN",
    "NEGGAUSSIAN",
    "LINES",
    NULL
};


/*
 * read input data from file 'name'
 */
static int siminput(MBLOCK *mblk, char *name)
{
    FILE *infile;
    char buf[300];
    int m,n,pos;
    SIMTYPE *st     = mblk->siminf->st;
    SPINSYSTEM *sps = &(st->sps[st->sps_pos]);

    if ((infile = fopen(name,"r")) == NULL)
        return FALSE;
    pos = st->sps_pos;
    clear_transition_list(mblk);
    clear_spins_and_couplings(mblk);
/*
    st->has_data = FALSE;
*/
    sps->ng   = 0;		
    sps->nl   = 0;		
    sps->nos  = 0;          
    for (m=1;m<=sps->maxparam;m++) 
        for (n=1;n<=sps->maxgroup;n++) {
            sps->ia[n][m] = 0;
            sps->ib[n][m] = 0;
        } 
    for (m=1;m<=sps->maxspin;m++) {
        sps->spin[m].w = 0.0;
        for (n=1;n<=sps->maxspin;n++) 
            sps->spin[m].cc[n] = 0.0;
    }
    for (m=1;m<=sps->maxlines;m++) {
        sps->il[m] = 0;
        sps->f[m]  = 0.0;
    }
    while (fgets(buf,250,infile) != NULL) {
        char *p, *q, *r;
        char sep[] = " \r\n\t";
        
        strupr(buf);
        p= strtok(buf, sep);
        if (p) {
            if (strcmp(p, "NSS") == 0) {
                p = strtok(NULL, sep);
                if (p) {
                    st->sps_num = psnd_scan_integer(p);
                    st->sps_num = max(st->sps_num, 1);
                    st->sps_num = min(st->sps_num,  MAXSPINSYSTEM);

                    allocate_all(mblk);
                    for (n=st->sps_num;n<MAXSPINSYSTEM;n++)
                        st->sps[n].active = FALSE;
                    simula_set_spinsystem(mblk, st, st->sps_pos);
                    sps = &(st->sps[st->sps_pos]);
                }
            } 
            else if (strcmp(p, "SSN") == 0) {
                p = strtok(NULL, sep);
                if (p) {
                    int sps_pos;
                    sps_pos = psnd_scan_integer(p)-1;
                    sps_pos = max(sps_pos, 0);
                    sps_pos = min(sps_pos, st->sps_num-1);
                    simula_set_spinsystem(mblk, st, sps_pos);
                    sps = &(st->sps[st->sps_pos]);
                }
            } 
            else if (strcmp(p, "NG") == 0) {
                p = strtok(NULL, sep);
                if (p) {
                    sps->ng = psnd_scan_integer(p);
                    sps->ng = max(sps->ng, 1);
                    sps->ng = min(sps->ng, MAXSPINLIMIT);
                    st->iout = 0;
                    if (sps->ng > sps->maxspin) 
                        resize_maxspin(st);
                }
            } 
            else if (strcmp(p, "NI") == 0) {
                p = strtok(NULL, sep);
                if (p)
                    st->ni = psnd_scan_integer(p);
            }
            else if (strcmp(p, "NFC") == 0) {
                p = strtok(NULL, sep);
                if (p)
                    st->nfc = psnd_scan_integer(p);
            }
            else if (strcmp(p, "NP") == 0) {
                p = strtok(NULL, sep);
                if (p)
                    st->npop = psnd_scan_integer(p);
            }
            else if (strcmp(p, "POP") == 0) {
                p = strtok(NULL, sep);
                if (p)
                    st->pop = psnd_scan_integer(p);
            }
            else if (strcmp(p, "RADIUS") == 0) {
                p = strtok(NULL, sep);
                if (p)
                    st->radius = psnd_scan_integer(p);
            }
            else if (strcmp(p, "PPM") == 0) {
                st->ppm = TRUE;
            }
            else if (strcmp(p, "HZ") == 0) {
                st->ppm = FALSE;
            }
            else if (strcmp(p, "SF") == 0) {
                p = strtok(NULL, sep);
                if (p) {
                    float f=0.0;
                    f = psnd_scan_float(p);
                    if (f > 0.0)
                        st->sf = f;
                    else
                        st->ppm = FALSE;
                }
            }
            else if (strcmp(p, "LW") == 0) {
                p = strtok(NULL, sep);
                if (p) {
                    float f=0.0;
                    f = psnd_scan_float(p);
                    if (f > 0.0)
                        st->width = f;
                }
            }
            else if (strcmp(p, "LS") == 0) {
                p = strtok(NULL, sep);
                if (p) {
                    int i;
                    for (i=0;lineshapes[i];i++) {
                        if (strcmp(lineshapes[i], p) == 0) {
                            st->igauss = i;
                            break;
                        }
                    }
                }
            }
            else if (strcmp(p, "AMIN") == 0) {
                p = strtok(NULL, sep);
                if (p) {
                    float f=0.0;;
                    f = psnd_scan_float(p);
                    if (f > 0.0)
                        st->amin = f;
                }
            }
            else if (strcmp(p, "STEP") == 0) {
                p = strtok(NULL, sep);
                if (p) {
                    float f=0.0;;
                    f = psnd_scan_float(p);
                    if (f > 0.0)
                        st->dev = f;
                }
            }
            else if (strcmp(p, "CHILD") == 0) {
                p = strtok(NULL, sep);
                if (p)
                    st->child = psnd_scan_integer(p);
            }
            else if (strcmp(p, "SI") == 0) {
                p = strtok(NULL, sep);
                if (p) {
                    float f=0.0;;
                    f = psnd_scan_float(p);
                    if (f > 0.0 && f <= MAXSIZ)
                        st->size = f;
                }
            }
            else if (strcmp(p, "REF") == 0) {
                char sep2[] = " \r\n\t,:;";
                p = strtok(NULL, sep);
                if (p) {
                    float f=0.0;
                    f = psnd_scan_float(p);
                    if (f > 0.0) {
                        st->xref = f;
                        st->aref = 1.0;
                        st->bref = 1.0;
                    }
                    p = strtok(NULL, sep);
                    if (p) {
                        f=0.0;
                        f = psnd_scan_float(p);
                        if (f > 0.0)
                            st->aref = f;
                        p = strtok(NULL, sep);
                        if (p) {
                            f=0.0;
                            f = psnd_scan_float(p);
                            st->bref = f;
                            p = strtok(NULL, sep);
                            if (p) {
                                int td = 0;
                                td = psnd_scan_integer(p);
                                if (td > 0)
                                    st->td = td;
                            }
                        }
                    }
                }
            }
            else if (strcmp(p, "W") == 0) {
                p = strtok(NULL, sep);
                if (p) {
                    q = strtok(NULL, sep);
                    if (q) {
                        int i=0;
                        float f=0.0;
                        i = psnd_scan_integer(p);
                        f = psnd_scan_float(q);
                        if (i > 0 && i <= sps->ng )
                            sps->spin[i].w = f;
                    }
                }
            }
            else if (strcmp(p, "D") == 0) {
                p = strtok(NULL, sep);
                if (p) {
                    q = strtok(NULL, sep);
                    if (q) {
                        int i=0;
                        float f=0.0;
                        i = psnd_scan_integer(p);
                        f = psnd_scan_float(q);
                        if (i > 0 && i <= sps->ng ) {
                            sps->spin[i].w = st->sf * f;
                            sps->has_data = TRUE;
                        }
                    }
                }
            }
            else if (strcmp(p, "S") == 0) {
                p = strtok(NULL, sep);
                if (p) {
                    q = strtok(NULL, sep);
                    if (q) {
                        int i=0,n=0;
                        i = psnd_scan_integer(p);
                        n = psnd_scan_integer(q);
                        if (i > 0 && i <= sps->ng ) {
                            sps->spin[i].nng = n;
                            sps->has_data = TRUE;
                        }
                    }
                }
            }
            else if (strcmp(p, "I") == 0) {
                p = strtok(NULL, sep);
                if (p) {
                    q = strtok(NULL, sep);
                    if (q) {
                        int i=0,n=0;
                        i = psnd_scan_integer(p);
                        n = psnd_scan_integer(q);
                        if (i > 0 && i <= sps->ng )
                            sps->spin[i].iso = n;
                    }
                }
            }
            else if (strcmp(p, "Q") == 0) {
                p = strtok(NULL, sep);
                if (p) {
                    q = strtok(NULL, sep);
                    if (q) {
                        int i=0,n=0;
                        i = psnd_scan_integer(p);
                        n = psnd_scan_integer(q);
                        if (n > 0 && n <= MAXSQN2)
                            if (i > 0 && i <= sps->ng )
                                sps->spin[i].sqn = n;
                    }
                }
            }
            else if (strcmp(p, "J") == 0) {
                p = strtok(NULL, sep);
                if (p) {
                    q = strtok(NULL, sep);
                    if (q) {
                        r = strtok(NULL, sep);
                        if (r) {
                            int i=0,j=0;
                            float f=0.0;
                            i = psnd_scan_integer(p);
                            j = psnd_scan_integer(q);
                            f = psnd_scan_float(r);
                            if (i > 0 && i <= sps->ng &&
                                j > 0 && j <= sps->ng) {
                                sps->spin[i].cc[j] = f;
                           /*     if (sps->spin[i].iso == sps->spin[j].iso)*/
                                    sps->spin[j].cc[i] = f;
                            }
                        }
                    }
                }
            }
            else if (strcmp(p, "SW") == 0 || strcmp(p, "SLIM") == 0) {
                p = strtok(NULL, sep);
                if (p) {
                    float sweep;
                        
                    sweep = psnd_scan_float(p);
                    st->sw = sweep;
                }
            }
            else if (strcmp(p, "AS") == 0) {
                p = strtok(NULL, sep);
                if (p) {
                    q = strtok(NULL, sep);
                    if (q) {
                        int i;
                        float f;
                        i = psnd_scan_integer(p);
                        f = psnd_scan_float(q);
                        ass_line(0.0, i, f, st);
                    }
                }
            }
            else if (strcmp(p, "IP") == 0) {
                int k;
                if (sps->nos >= sps->maxparam) 
                    resize_maxparam(st);
                sps->nos++;
                for (k=1;k<=sps->maxgroup;k++) {
                    p = strtok(NULL, sep);
                    if (p) {
                        int i=0,j=0;
                        q = strchr(p, ',');
                        i = psnd_scan_integer(p);
                        if (q)
                            j = psnd_scan_integer(q+1);
                        /* sscanf(p,"%d,%d",&i,&j); */
                        if (i>=0 && i <= sps->ng) {
                            sps->ia[k][sps->nos] = i;
                        }
                        if (j>=0 && j <= sps->ng) {
                            sps->ib[k][sps->nos] = j;                        
                        }
                    }
                    else
                        break;
                }
            }
        }
    }
    fclose(infile);
    st->has_data = sps->has_data;
    simula_set_spinsystem(mblk, st, pos);
/*
    st->has_data = FALSE;
*/
    return TRUE;
}

/*
 * write data to file 'name'
 */
static int simoutput(SIMTYPE *st, char *name)
{
    int i,j,n;
    FILE *outfile = stdout;

    if ((outfile = fopen(name,"w")) == NULL)
        return FALSE;

    fprintf(outfile, "# SIMULATION PARAMETERS\n");
    fprintf(outfile, "=======================\n\n");

    fprintf(outfile, "# Spectrometer frequency\n");
    fprintf(outfile, "SF  %.3f\n\n", st->sf);
    fprintf(outfile, "# Number of data points\n");
    fprintf(outfile, "SI  %.0f\n\n", st->size);
    fprintf(outfile, "# Upper and lower frequecies\n");
    fprintf(outfile, "SW  %.1f\n\n", st->sw);
    fprintf(outfile, "# Reference channel\n");
    fprintf(outfile, "REF  %.3f, %.3f, %.3f, %d\n\n", 
                      st->xref, st->aref, st->bref, st->td);
    fprintf(outfile, "# Line width\n");
    fprintf(outfile, "LW  %.3f\n\n", st->width);
    fprintf(outfile, "# Line shape\n");
    fprintf(outfile, "LS  %s\n\n", lineshapes[st->igauss]);

    fprintf(outfile, "# Number of iterations (LSQ stuff)\n");
    fprintf(outfile, "NI  %d\n\n", st->ni);

    fprintf(outfile, "# Number of spin systems\n");
    fprintf(outfile, "NSS %d\n\n", st->sps_num);

    fprintf(outfile, "# Treshold of lowest transition intensity\n");
    fprintf(outfile, "AMIN %.3f\n\n", st->amin);

for (n=0;n<st->sps_num;n++) {

    SPINSYSTEM *sps = &(st->sps[n]);

    fprintf(outfile, "===========================\n");
    fprintf(outfile, "# Spin system number\n");
    fprintf(outfile, "SSN %d\n\n", n+1);

    fprintf(outfile, "# Number of spin groups\n");
    fprintf(outfile, "NG  %d\n\n", sps->ng);

/*
            else if (strcmp(p, "PPM") == 0) {
                st->ppm = TRUE;
            }
            else if (strcmp(p, "HZ") == 0) {
                st->ppm = FALSE;
            }
*/

    fprintf(outfile, "# Chemical shifts\n");
    if (st->ppm) {
        fprintf(outfile, "PPM\n");
        for (i=1;i<=sps->ng;i++)
            fprintf(outfile, "D   %d  %.3f\n", i, sps->spin[i].w/st->sf);
    }
    else {
        for (i=1;i<=sps->ng;i++)
            fprintf(outfile, "W   %d  %.2f\n", i, sps->spin[i].w);
    }
    fprintf(outfile, "\n# Coupling constants\n");
    for (i=1;i<=sps->ng;i++)
        for (j=i+1;j<=sps->ng;j++)
            if (sps->spin[i].cc[j] != 0 && 
                 ((sps->spin[i].iso != sps->spin[j].iso || sps->spin[i].sqn != sps->spin[j].sqn) || j > i))         
                fprintf(outfile, "J   %d  %d  %.2f\n", i,j,sps->spin[i].cc[j]);
    fprintf(outfile, "\n# Chemical shift: equivalent nuclei per spin-group\n");
    for (i=1;i<=sps->ng;i++)
        fprintf(outfile, "S   %d  %d\n", i, sps->spin[i].nng);
    fprintf(outfile, "\n# Spin quantum number * 2\n");
    for (i=1;i<=sps->ng;i++)
        fprintf(outfile, "Q   %d  %d\n", i, sps->spin[i].sqn);
    fprintf(outfile, "\n# Chemical shift: isotope identifier\n");
    for (i=1;i<=sps->ng;i++)
        fprintf(outfile, "I   %d  %d\n", i, sps->spin[i].iso);
    fprintf(outfile, "\n# Assigned Transitions\n");
    for (i=1;i<=sps->nl;i++)
        fprintf(outfile, "AS  %d  %.3f\n", sps->il[i], sps->f[i]);
    fprintf(outfile, "\n# Iteration Parameters\n");
    for (j=1;j<=sps->nos;j++) {
        fprintf(outfile, "IP");  
        for (i=1;i<=st->maxgroup;i++) {
            if (sps->ia[i][j] == 0) 
                break;
            if (sps->ib[i][j] != 0) 
                fprintf(outfile, "  %d,%d", sps->ia[i][j], sps->ib[i][j]); 
            else
                fprintf(outfile, "  %d", sps->ia[i][j]); 
        }
        fprintf(outfile, "\n");  
    }
}

    fclose(outfile);
    return TRUE;
}

static void store_fmatrix(MSTYPE *kt, float **vb, int size)
{
    int j, k, nsize;

    nsize     = sizeof(float) * (size * size + kt->pos);
    kt->size  = max(nsize, kt->size);
    kt->m     = (float*) realloc(kt->m , kt->size);
    assert(kt->m);
    for (k=1;k<=size;k++) { 
        for (j=1;j<=size;j++)
            kt->m[kt->pos++] = vb[k][j];
    }
}

static int retrieve_fmatrix(MSTYPE *kt, float **vb, int size)
{
    int j, k;
    if ((kt->size - sizeof(float) * kt->pos) < (sizeof(float) * size * size))
        return FALSE;
    for (k=1;k<=size;k++) { 
        for (j=1;j<=size;j++)
             vb[k][j] = kt->m[kt->pos++];
    }
    return TRUE;
}

static void copy_fmatrix(MSTYPE *kt1, MSTYPE *kt2)
{
    kt1->size  = kt2->size;
    kt1->pos   = 0;
    kt1->m     = (float*) realloc(kt1->m , kt1->size);
    memcpy(kt1->m, kt2->m, kt1->size);
}

static void rewind_fmatrix(MSTYPE *kt)
{
    kt->pos  = 0;
}


/*
 * Apply corrections to shifts and coupling constants
 */
static void apply_corrections(SIMTYPE *st, float *x)
{
    int i,j,k,l,n;
    SPINSYSTEM *sps = &(st->sps[st->sps_pos]);
    
    for (j=1,n=1;j<=sps->nos;j++) {
        for (k=1;k<=st->maxgroup;k++) {
            if (sps->ia[k][j] == 0) 
                break;
            i = sps->ia[k][j];
            if (sps->ib[k][j] != 0) {
                l = sps->ib[k][j];
                sps->spin[i].cc[l] += x[n++];
                if (sps->spin[i].iso == sps->spin[l].iso && 
                    sps->spin[i].sqn == sps->spin[l].sqn) 
                    sps->spin[l].cc[i] = sps->spin[i].cc[l];
            }
            else
                sps->spin[i].w += x[n++];
        }
    }
}

/*
 * Transform correction-array of fractions
 * into real corrections
 */
static void apply_scaling_factor(SIMTYPE *st, float *x)
{
    int i,j,k,l,n;
    SPINSYSTEM *sps = &(st->sps[st->sps_pos]);
    
    for (j=1,n=1;j<=sps->nos;j++) {
        for (k=1;k<=st->maxgroup;k++) {
            if (sps->ia[k][j] == 0) 
                break;
            i = sps->ia[k][j];
            if (sps->ib[k][j] != 0) {
                l = sps->ib[k][j];
                x[n++] *= sps->spin[i].cc[l];
            }
            else
                x[n++] *= sps->spin[i].w;
        }
    }
}

/*
 * Calculate eigenvalues and eigenvectors
 */
static void laocoon_calc(SIMTYPE *st, int do_differ)
{
    int i,j,k,l;
    float scart;
    int la, jj, it, *ko;
    SPINSYSTEM *sps = &(st->sps[st->sps_pos]);

    la = 0;
    ko = ivector(1,st->maxeigen);
    /*
     * For all sub-matrices do ..
     */
    for (jj=1;jj<=st->iay;jj++) {
        int  kk;

        /*
         * For all sub-submatrices do ..
         */
        for (kk=1;kk<=st->submat[jj].nmat;kk++) {
            int i, j, moa, lim, ok, k;
            /*
             * moa = the order of the kk-th sub-submatrix 
             *       in the jj-th sub-matrix
             */
            moa = st->submat[jj].nsub[kk];
            lim = 0;
            /*
             *  ------------------------------------------------
             *  3. compute elements of hamiltonian matrix
             *  ------------------------------------------------
             */
            for (k=1;k<=moa;k++) {
                /*
                 * la = the offset of the sub-submatrix
                 * lp = the k-th energy level in the sub-submatrix
                 */
                int l, lp = st->lfz[la+k];
                /*
                 * For all energy levels in the sub-submatrix do ..
                 * fz	 spin states * 2
                 * lfz	 list of energy levels	
                 */
                for (j=1;j<=k;j++) {
                    /*
                     * la = the offset of the sub-submatrix
                     * lc = the j-th energy level in the sub-submatrix
                     */
                    int lc = st->lfz[la+j];
                    /*
                     * lim is index in a
                     *
                     * a  = matrix stored by columns in 
                     *      packed upper triangular form,
                     *      i.e. occupying nx*(nx+1)/2 consecutive locations.
                     */
                    lim++;
                    st->a[lim] = 0.0;
                    if (j == k) {
                        /*
                         *  --------------------------------
                         *  diagonal elements
                         *  --------------------------------
                         */
                        for (i=1;i<=sps->ng;i++) {
                            /*
                             * 0.5 * (2 * spin state) * w
                             */
                            st->a[lim] += 0.5 * st->fz[i][lp] * sps->spin[i].w;
                            /*
                            if (i >= sps->ng) 
                                break;
                                */
                            for (l=i+1;l<=sps->ng;l++) 
                                st->a[lim] += 0.25 * sps->spin[i].cc[l] 
                                     * st->fz[i][lp] * st->fz[l][lp];
                        }
                    }
                    else {
                        /*
                         *   --------------------------------
                         *  off diagonal elements
                         *  --------------------------------
                         */
                        int kn = 0;
                        for (i=1;i<=sps->ng;i++) {
                            if (st->fz[i][lp] == st->fz[i][lc]) 
                                continue;
                            kn++;
                            st->ist[kn] = i;
                        }
                        if (kn != 2) 
                            continue;
                        i = st->ist[1];
                        l = st->ist[2];
                        /*
                         * must be (2) * (-2) = -4
                         */
                        if ((st->fz[i][lp] - st->fz[i][lc]) * 
                              (st->fz[l][lp] - st->fz[l][lc]) != -4) 
                                    continue;
                        st->a[lim] = 0.125 * sps->spin[l].cc[i] * 
                                sqrt((double)((st->submat[jj].nsp[i]
                                * (st->submat[jj].nsp[i] + 2) -
                                st->fz[i][lc] * st->fz[i][lp]) * 
                                (st->submat[jj].nsp[l] 
                                * (st->submat[jj].nsp[l] + 2) 
                                - st->fz[l][lc] * st->fz[l][lp])));
                    }
                }  
            }
            /*
             *  ------------------------------------------------
             *  4. diagonalization
             *  ------------------------------------------------
             */
            givens(moa, st->vec, st->root, st->a);
            /*
             *  ------------------------------------------------
             *  5. control eigenvectors ordering
             *  ------------------------------------------------
             */
            ok = FALSE;
            if (st->it == 0) 
                ok = TRUE;
            else 
                retrieve_fmatrix(st->kt2, st->vb, moa);
            if (moa == 1) 
                ok = TRUE;
            if (!ok) for (i=1;i<=moa;i++) {
                int kn, nim, m, n;
                
                scart = 1000.0;
                ko[i] = 0;
                nim=i-1;
                for (j=1;j<=moa;j++) {
                    int doit = TRUE;
                    float scart1, scart2;

                    for (n=1;n<=nim;n++) 
                        if (j == ko[n]) {
                            doit = FALSE;
                            break;
                        }
                    if (!doit)
                        continue;
                    scart1 = 0.0;
                    scart2 = 0.0;
                    for (k=1;k<=moa;k++) {
                        scart2 += pow(st->vb[i][k] + st->vec[j][k],2);
                        scart1 += pow(st->vb[i][k] - st->vec[j][k],2);
                    }

                    if (scart2 < scart1) 
                        scart1 = scart2;
                    if (scart1 > scart) 
                        continue;
                    scart = scart1;
                    ko[i] = j;
                }
                kn = ko[i];
                st->e[i+la] = st->root[kn];
                for (m=1;m<=moa;m++)
                    st->vb[i][m] = st->vec[kn][m];
            }
            else for (k=1;k<=moa;k++) {
                st->e[k+la] = st->root[k];
                for (j=1;j<=moa;j++)
                     st->vb[k][j] = st->vec[k][j];
            }
            store_fmatrix(st->kt1, st->vb, moa);

            if (do_differ) 
                differ(moa,la,jj,st);

            la += moa;
        }
    }
    free_ivector(ko, 1);
}


static void reassign_transitions(MBLOCK *mblk)
{
    float *ff;
    int j;
    SIMTYPE *st     = mblk->siminf->st;
    SPINSYSTEM *sps = &(st->sps[st->sps_pos]);
    
    if (st->sticky) {
        /*
         * Get the current positions of the assigned transitions
         * before the martix is re-build and all transitions are
         * renumbered
         */
        ff = fvector(1, sps->nl);
        for (j=1;j<=sps->nl;j++) {
            int k;
            for (k=1;k<=sps->lct;k++) {
                if (sps->il[j] == sps->trans[k].not) {
                    ff[j] = sps->trans[k].deig;
                    break;
                }
            }
        }
    }
    /*
     * Recalculate the matrix with the new shifts and couplings
     */
    laocoon5(mblk);
    if (st->sticky) {
        /*
         * Find the new numbers (or some number at the same frequency)
         * for the assigned transitions
         */
        for (j=1;j<=sps->nl;j++) {
            int k;
            for (k=1;k<=sps->lct;k++) {
                if (sps->trans[k].deig == ff[j]) {
                    int m, ok = TRUE;
                    for (m=1;m<j;m++) {
                        if (sps->il[m] == sps->trans[k].not) {
                            ok = FALSE;
                            break;
                        }
                    }
                    if (ok) {
                        sps->il[j] = sps->trans[k].not;
                        break;
                    }
                }
            }
        }
        sort_lines(sps);
        free_fvector(ff, 1);
    }
}

static void simplex_eval(void *data, int corrsize, float x[], float *result)
{
    int i,j,k,l,n;
    int la, jj, it;
    MBLOCK *mblk = (MBLOCK *) data;
    SIMTYPE *st   = mblk->siminf->st;
    SPINSYSTEM *sps = &(st->sps[st->sps_pos]);

    if (iteration_abort_by_user) {
        *result = 0.0;
        return;
    }
        
    /*
     * copy the correction factors into array corr3
     *
    for (j=1;j<=corrsize;j++) 
        st->corr3[j] = x[j-1]; */

    for (j=1,n=0;j<=sps->nos;j++) {
        for (k=1;k<=st->maxgroup;k++) {
            if (sps->ia[k][j] == 0) 
                break;
            i = sps->ia[k][j];
            if (sps->ib[k][j] != 0) {
                l = sps->ib[k][j];
                st->corr3[j] = sps->spin[i].cc[l] * (1.0-x[n]);
            }
            else {
                st->corr3[j]  = sps->spin[i].w * (1.0-x[n]);
            }
            n++;
        }
    }
/*
    apply_scaling_factor(st, st->corr3);
*/
    apply_corrections(st, st->corr3);

    it=0;
    rewind_fmatrix(st->kt1);
    rewind_fmatrix(st->kt2);
    laocoon_calc(st, FALSE);
    /*
     *  ------------------------------------------------
     *  6. least squares routines
     *  ------------------------------------------------
     */
    lsq1(mblk);
    st->it++;

    /*
     * This is a hit. Save it
     */
    if (st->er1 < st->er2) {
        st->er2 = st->er1;
        copy_fmatrix(st->kt2, st->kt1);
        for (j=1;j<=st->maxenergylevel;j++) 
            st->e2[j] = st->e[j];
        for (j=1;j<=corrsize;j++) {
            st->corr2[j] = st->corr3[j];
        }
        if (st->show_iterations == SHOW_ALL)
            printf("                                ***\n");
        else if (st->show_iterations == SHOW_BEST)
            printf("r.m.s.= %7.3f  hz\n",st->er2);
    }

    /*
     * undo the corrections
     */
    for (j=1;j<=corrsize;j++) 
        st->corr3[j] = -st->corr3[j];
    apply_corrections(st, st->corr3);

    *result = st->er1;
}

/*
 * Optimize the spectrum using simplex algorithm
 */
static int simplex_iterate(MBLOCK *mblk)
{
    SIMTYPE *st   = mblk->siminf->st;
    int k,j,n,count=0;
    float dev;
    SPINSYSTEM *sps = &(st->sps[st->sps_pos]);

    if (sps->nl == 0) {
        psnd_printf(mblk,"No assigned transitions\n");
        return FALSE;
    }
    /*
     * Number of params to iterate
     */
    for (j=1;j<=sps->nos;j++) 
        for (k=1;k<=st->maxgroup;k++) 
            if (sps->ia[k][j] != 0) 
                count++;

    if (count == 0) {
        psnd_printf(mblk,"Nothing to iterate\n");
        return FALSE;
    }
    /*
     *  ------------------------------------------------
     *  2. calculate spin functions
     *  ------------------------------------------------
     */
    algor(mblk, FALSE);

    st->it = 0;
    st->er1 = 1000.0;
    st->er2 = 1e20;

    /*
     * Start iteration
     */
    for (j=1;j<=count;j++) 
        st->corr2[j]=0;
    {
        int i,l,iw = count + 1;
        int ni = st->nfc;
        float f, *xs, *w1, *w2, *w3, *w4, *w5, *w6;
        int nsize = (count+100);
        int size = sizeof(float) * nsize;
        float tol     = 1.e-6;

        f = 0.0;
        xs = (float*) calloc(size,1);
        assert(xs);
        w1 = (float*) malloc(size);
        assert(w1);
        w2 = (float*) malloc(size);
        assert(w2);
        w3 = (float*) malloc(size);
        assert(w3);
        w4 = (float*) malloc(size);
        assert(w4);
        w5 = (float*) malloc(size);
        assert(w5);
        size += sizeof(float);
        w6 = (float*) malloc(size * size);
        assert(w6);
       
        for (j=1,n=0;j<=sps->nos;j++) {
            for (k=1;k<=st->maxgroup;k++) {
                if (sps->ia[k][j] == 0) 
                    break;
                i = sps->ia[k][j];
                if (sps->ib[k][j] != 0) {
                    l = sps->ib[k][j];
                    xs[n++] = 1.0; /*sps->spin[i].cc[l];*/
                }
                else
                    xs[n++] = 1.0; /*sps->spin[i].w;*/
            }
        }

        simplx(count, xs, &f, tol, iw, w1, w2, w3, w4, w5, w6,
               nsize, simplex_eval, (void*)mblk, ni);
        free(xs);
        free(w1);
        free(w2);
        free(w3);
        free(w4);
        free(w5);
        free(w6);
    }
    if (st->er2 > st->er1)
        return FALSE;
        
    apply_corrections(st, st->corr2);
 

    psnd_printf(mblk,"RMS = %10.3f hz\n", st->er2);
    st->er1 = st->er2;
    rewind_fmatrix(st->kt2);

    for (j=1;j<=st->iay;j++) {
        int k;
        for (k=1;k<=st->submat[j].nmat;k++) {
            int moa = st->submat[j].nsub[k];
            retrieve_fmatrix(st->kt2, st->va, moa);
        }
    }
    copy_fmatrix(st->kt1, st->kt2);
    for (j=1;j<=st->maxenergylevel;j++) 
        st->e[j] = st->e2[j];

    for (j=1;j<=sps->nos;j++) 
        st->corr[j] = 0.0;

    st->has_data = TRUE;
    itpar(mblk);
    spectr(mblk, SHOW_NONE);
    reassign_transitions(mblk);
    return TRUE;
}


static void laocoon5(MBLOCK *mblk)
{
    int j,la, jj, done = FALSE;
    SIMTYPE *st   = mblk->siminf->st;
    SPINSYSTEM *sps = &(st->sps[st->sps_pos]);


    if (sps->ng <= 0) 
        return;
    /*
     *  ------------------------------------------------
     *  2. calculate spin functions
     *  ------------------------------------------------
     */
    algor(mblk, TRUE);
    if (st->iout == 1) 
        return;
    st->it = 0;

    rewind_fmatrix(st->kt1);
    rewind_fmatrix(st->kt2);
    laocoon_calc(st,FALSE);
    st->has_data = TRUE;
    /*
     *  ------------------------------------------------
     *  7. output of computed spectrum
     *  ------------------------------------------------
     */
    if (st->sw <= 0) 
        st->sw = 2500;
    spectr(mblk, SHOW_NONE);
}

static int laocoon5_iterate(MBLOCK *mblk)
{
    SIMTYPE *st   = mblk->siminf->st;
    int j,k,count=0,la, jj;
    SPINSYSTEM *sps = &(st->sps[st->sps_pos]);

    if (sps->ng <= 0) 
        return FALSE;
    if (st->ni <= 0) 
        return FALSE;
    if (sps->nl == 0) {
        psnd_printf(mblk,"No assigned transitions\n");
        return FALSE;
    }
    /*
     * Number of params to iterate (AK)
     */
    for (j=1;j<=sps->nos;j++) 
        for (k=1;k<=st->maxgroup;k++) 
            if (sps->ia[k][j] != 0) 
                count++;

    if (count == 0) {
        psnd_printf(mblk,"Nothing to iterate\n");
        return FALSE;
    }
    /*
     *  ------------------------------------------------
     *  2. calculate spin functions
     *  ------------------------------------------------
     */
    algor(mblk, FALSE);
    if (st->iout == 1) 
        return FALSE;
    st->it = 0;
    st->er1 = 1000.0;

    rewind_fmatrix(st->kt1);
    rewind_fmatrix(st->kt2);
    do {
        MSTYPE *tmp;
        laocoon_calc(st,TRUE);
        rewind_fmatrix(st->kt1);
        rewind_fmatrix(st->kt2);
        /*
         *  ------------------------------------------------
         *  6. least squares routines
         *  ------------------------------------------------
         */
        if (st->it == 0) 
            psnd_printf(mblk,"Starting iteration ...\n");
        lsq1(mblk);
        if (st->iout == 1) 
            return FALSE;
        if (!lsq2(st))
            return FALSE;        
        tmp = st->kt1;
        st->kt1 = st->kt2;
        st->kt2 = tmp;
        st->it++;
    } while (st->nexit == 1);
    itpar(mblk);
    st->has_data = TRUE;
    /*
     *  ------------------------------------------------
     *  7. output of computed spectrum
     *  ------------------------------------------------
     */
    if (st->sw <= 0) 
        st->sw = 2500;
    spectr(mblk, SHOW_NONE);
    reassign_transitions(mblk);
    return TRUE;
}



/*
lfz	 list of energy levels	
fz	 energy levels per spin	
iay      = no. of submatrices
nos	 = no. of parameter sets

AK
moa      = order of the sub-submatrix
la       = offset of the sub-submatrix in the sub-matrix
jj       = no. of the sub-matrix

Calculate array st->d

*/

static void differ(int moa, int la, int jj, SIMTYPE *st)
{
    SPINSYSTEM *sps = &(st->sps[st->sps_pos]);
    int j, moam;
    double fact;

    moam = moa-1;
    fact = 1.0;
    /*
     * Loop over all elements of the sub-submatrix
     */
    for (j=1;j<=moa;j++) {
        int ns;
        /* 
         * add sub-submatrix offset 'la' to 'j' 
         */
        int jl = j + la; 
        /*
         * Loop over al iteration parameter sets
         */
        for (ns=1;ns<=sps->nos;ns++) {
            int nps;
            /*
             * 'differ' matrix [PARAMETERS][ENERGYLEVELS]
             */
            st->d[ns][jl] = 0.0;
            /*
             * Loop over all spin-groups
             */
            for (nps=1;nps<=st->maxgroup;nps++) {
                int i,l,k,j1,idif;
                
                if (sps->ia[nps][ns] == 0) 
                    break;
                /*
                 * chemical shift
                 */
                i = sps->ia[nps][ns];
                /*
                 * Couplings
                 */
                if (sps->ib[nps][ns] <= 0) {
                    /*
                     * For all elements of the sub-submatrix
                     */
                    for (k=1;k<=moa;k++) {
                        /*
                         * k+la = k plus sub-submatrix offset
                         * kl = the energy level at k+la
                         * fz[i][kl] = energy level 'kl' for spin (shift) 'i'
                         */
                        int kl = st->lfz[k+la];
                        st->d[ns][jl] += 0.5 * st->fz[i][kl] 
                                            * POW2(st->vb[j][k]);
                    }
                    continue;
                } 
                l = sps->ib[nps][ns];
                /*
                 * For all elements of the sub-submatrix
                 */
                for (k=1;k<=moa;k++) {
                    int kl = st->lfz[k+la];
                    st->d[ns][jl] += 0.25 * st->fz[i][kl] * st->fz[l][kl]
                         * POW2(st->vb[j][k]);
                }
                /*
                 * If not same isotope, continue
                 */
                if (sps->spin[i].iso != sps->spin[l].iso ||
                    sps->spin[i].sqn != sps->spin[l].sqn) 
                    continue;
                /*
                 * if sub-submatrix order = 1, continue
                 */
                if (moam == 0) 
                    continue;
                idif = ((int)(pow(2,l) - pow(2,i)))/2;
                for (j1=1;j1<=moam;j1++) {
                    int k1, m = st->lfz[j1+la];
                    for (k1=j1+1;k1<=moa;k1++) {
                        int nss, n = st->lfz[k1+la];
                        if (st->iay == 1) 
                            if ((n-m) != idif) 
                                continue;
                        else
                            if ((st->fz[i][m] - st->fz[i][n]) * 
                                (st->fz[l][m] - st->fz[l][n]) != -4) 
                                continue;
                        nss = 0;
                        for (k=1;k<=sps->ng;k++) 
                            nss += pow(st->fz[k][m] - st->fz[k][n],2);
                        if (nss != st->maxspin) 
                            continue;
                        /*
                         * If number of sub-matrices != 1
                         */
                        if (st->iay != 1) 
                            fact = 0.25 * sqrt((double)((st->submat[jj].nsp[i]
                            * (st->submat[jj].nsp[i] + 2) -
                            st->fz[i][m] * st->fz[i][n]) * (st->submat[jj].nsp[l]
                            * (st->submat[jj].nsp[l] + 2) 
                            - st->fz[l][m] * st->fz[l][n])));
                        st->d[ns][jl] += st->vb[j][k1] * st->vb[j][j1] * fact;
                    }
                }
            }
        }
    }
}

typedef struct ord_struct {
    int spin_id;
    int nng, sqn;
} ORD;

int ord_compare(const void *val1, const void *val2)
{
    ORD *ord1, *ord2;
    int p1,p2;
    ord1 = (ORD*) val1;
    ord2 = (ORD*) val2;
    p1 = ord1->nng * ord1->sqn;
    p2 = ord2->nng * ord2->sqn;

    if (ord1->nng > ord2->nng)
        return 1;
    if (ord1->nng == ord2->nng) {
        if (p1 > p2)
            return 1;
        if (p1 == p2)
            return 0;
    }
    return -1;
}

/*
 * Set up matrices and sub-matrices 
 * for the current number of spin groups
 */
static void algor(MBLOCK *mblk, int verbose)
{
    SIMTYPE *st   = mblk->siminf->st;
    int j, jj, k, l, kk, idep, done, matd, maxeigen=MAXEIGEN, iord;
    int   *ifz;		/* [384];		*/
    int   *fzord;	/* [384];		*/
    SPINSYSTEM *sps = &(st->sps[st->sps_pos]);
    ORD *ord;

    ifz		= ivector(1,st->maxenergylevel);
    fzord	= ivector(1,st->maxenergylevel);
    ord         = (ORD*) malloc(sizeof(ORD) * (sps->ng + 1));
    assert(ord);

    /*
     *  ------------------------------------------------
     *  1.generate i-array. iay = no. of submatrices
     *  ------------------------------------------------
        lfz	 list of energy levels	
        fz	 for each energy level, spin	states x 2
                    e.g. 1 -1, 2 0 -2, 3 1 -1 -3.  
        iay      = no. of submatrices
        nos	 = no. of parameter sets
     */
    st->iay = 1;
    /*
     * Calculate iay = no. of submatrices.
     *   nng gives the number of equivalent nuclei in a spin group,
     *   e.g. for protons in CH3 nng is 3

        Composite particle states for p equivalent nuclei with spin-1/2
            F   p=1   2   3   4   5   6

            0     -   S   -  2S   -  5S
           1/2    D   -  2D   -  5D   -
            1         T   -  3T   -  9T
           3/2            Q   -  4Q   -
            2                Qt   - 5Qt
           5/2                   Sx   -
            3                        Sp

     */

    /*
     *    ([ 2 * spin ] * p + 2)/ 2
     */
    for (k=1;k<=sps->ng;k++) {
        if (sps->spin[k].nng > 1) {
            j = (sps->spin[k].nng * sps->spin[k].sqn + 2)/2;
            st->iay *= j;
        }
        ord[k-1].spin_id = k;
        ord[k-1].nng = sps->spin[k].nng;
        ord[k-1].sqn = sps->spin[k].sqn;
    }

    /*
     * sort spins with highest 'composite particle state' first
     */
    qsort((void*) &(ord[0]),sps->ng, sizeof(ORD), ord_compare);

    if (st->iay > st->maxmatrix)  
        alloc_submatrices(st, FALSE);
    /*
     * In the first submatrix:
     *   set the number of spins in a spin group.
     *
     * nsp = number of equivalent nuclei in a spin group per matrix
     */
    for (k=1;k<=sps->ng;k++) {
        st->submat[1].nsp[k] = sps->spin[k].nng * sps->spin[k].sqn;
        st->submat[1].sqn[k] = sps->spin[k].sqn;
    }
    kk = sps->ng;
    iord = ord[kk-1].spin_id;

    done = FALSE; 
    /*
     * For all other submatrices:
     */
    for (j=2;j<=st->iay && !done;j++) {
        /*
         * For all spin groups in this submatrix:
         *   copy the number of equivalent nuclei
         *   from the previous submatrix
         */
        for (k=1;k<=sps->ng;k++) {
            st->submat[j].nsp[k] = st->submat[j-1].nsp[k];
            st->submat[j].sqn[k] = st->submat[j-1].sqn[k];
        }
        do {
            /*
             * T -> S, or Sx -> Q -> D
             */ 
        /*    st->submat[j].nsp[kk] -= 2;*/
            st->submat[j].nsp[iord] -= 2;
/*
printf("st->submat[%d].nsp[%d] = %d\n",j,kk,st->submat[j].nsp[kk] );
*/
            if (st->submat[j].nsp[iord] >= 0 /* st->submat[j].nsp[kk] >= 0 && sps->spin[kk].nng > 1  */) 
                break; 
            /*
             * reset value
             */
/*            st->submat[j].nsp[kk] = st->submat[1].nsp[kk];*/
            st->submat[j].nsp[iord] = st->submat[1].nsp[iord];
            /*
             * Next spin group or end
             */
            if (kk <= 1) 
                done = TRUE;
            else
                kk--;
            iord = ord[kk-1].spin_id;
        } while (!done);
        kk = sps->ng;
        iord = ord[kk-1].spin_id;
    }
    free(ord);

    /*
     *  -------------------------------------------
     *  2.generate full im-arrays
     *  matd = no. of energy levels
     *  -------------------------------------------
     */
    matd = 0;
    for (jj=1;jj<=st->iay;jj++) {
        int km, kfin, lll, done, im2, im3, nk, imat, nflag;

        kk = 1;
        km = kk + matd;
        ifz[kk] = 0;
        /*
         *  -------------------------------------------
         *  3.generate im-array for one submatrix
         *  -------------------------------------------
         */
        for (k=1;k<=sps->ng;k++) {
            /*
             * Copy number of energy levels into fz array for each spin group
             */
            st->fz[k][km] = st->submat[jj].nsp[k];
            /*
             * ifz = total number of energy levels for nucleus in this submatrix *10
             */
            ifz[kk] += st->fz[k][km] * 10;
        }
        done = FALSE;
        do {
            kfin = 1;
            kk++;
            km++;
            ifz[kk] = 0;
            /*
             * Allocate memory, if needed
             */
            if (matd + km >= st->maxenergylevel) 
                resize_maxenergylevel(st, &ifz, &fzord,
                     max(st->maxenergylevel+MAXENERGYLEVEL, matd + km));
            /*
             * Generate spin states (*2), like :
             *                    1  -1     = (1/2   -1/2)
             *                  2   0   -2
             *                3   1  -1   -3
             * For all spin groups
             */
            for (k=1;k<=sps->ng;k++) 
                st->fz[k][km] = st->fz[k][km-1];  

            do {  
                st->fz[kfin][km] -= 2;
                if ((st->fz[kfin][km] + st->submat[jj].nsp[kfin]) >= 0) { 
                    for (k=1;k<=sps->ng;k++) 
                        ifz[kk] += st->fz[k][km] * 10;
                    break;
                }
                st->fz[kfin][km] = st->submat[jj].nsp[kfin];
                if (kfin >= sps->ng) 
                    done = TRUE;
                else
                    kfin++;
            } while (!done);

        } while (!done);

        /*
         * Order of the sub-matrix
         */
        st->submat[jj].imar = kk-1;
        /*
         *  -------------------------------------------
         *  imar(jj) dimension of the jj-th submatrix
         *  -------------------------------------------
         */
        nk = st->submat[jj].imar;
        lll = 0;
        im2 = 0;
        for (k=1;k<=nk;k++) {
            int kmatd;
            
            do {
                for (l=1;l<=nk;l++) {
                    if (((ifz[l] + 1 < 0) &&  (im2 < 0)) ||
                             (ifz[l] + 1 > 0)) {
                        if (im2 < ifz[l]) {
                            im2 = ifz[l];
                            lll = l;
                        }
                    }
                }
                if (lll > 0) 
                    break;
                else
                    im2 = -(MAXENERGYLEVEL*10);
            } while (1);
            kmatd = k + matd;
            fzord[kmatd] = im2;
            /*
             * List of energy level numbers
             */
            st->lfz[kmatd] = lll + matd;
            ifz[lll] = -1;
            if (im2 <= 0) 
                im2 = -(MAXENERGYLEVEL*10);
            else {
                im2 = 0;
                lll = 0;
            }
        }
        idep = matd+1;
        matd += st->submat[jj].imar;
        /*
         *  ------------------------------------------------
         *  4. computation of number and orders of the sub-
         *     sub-matrices in the j-th submatrix. nmat(j) =
         *     no. of the sub-sub-matrices in the imar(j)
         *     matrix. nsub(j,k) = order of each k-th sub-sub
         *     matrix belonging to the j-th submatrix.
         *  ------------------------------------------------
         */
        imat = 1;
        /*
         * Nuber of sub-submatrices
         */
        st->submat[jj].nmat = 1;
        nflag = 0;
        im3 = fzord[idep];
        for (l = idep; l <= matd;l++) {
            if (im3 != fzord[l]) {           
                nflag = 1;
                im3 = fzord[l];
                imat++;
                st->submat[jj].nmat = imat;
            }
            else 
                nflag++;
            maxeigen = max(maxeigen, nflag);
            /*
             * Order of sub-submatrices
             */
            st->submat[jj].nsub[imat] = nflag;
        }
        /*
         * Allocate memory
         */
        if (st->submat[jj].nmat > st->maxsubmatrix) 
            resize_maxsubmatrix(st, st->submat[jj].nmat);
    }
    /*
     * Print results
     */
    if (verbose) {
        psnd_printf(mblk,"number of submatrices %2d, number of energy levels %3d\n",
            st->iay, matd);
        for (j=1;j<=st->iay;j++) {
            psnd_printf(mblk,"Submatrix no.            = %d\n", j);
            psnd_printf(mblk,"Order of submatrix.      = %d\n", st->submat[j].imar);
            psnd_printf(mblk,"No of sub-submatrices    = %d\n", st->submat[j].nmat);
            psnd_printf(mblk,"Order of sub-submatrices = ");
            for (k=1;k<=st->submat[j].nmat;k++)
                psnd_printf(mblk,"%d ", st->submat[j].nsub[k]);
            psnd_printf(mblk,"\n");
        }        
    }
    allocate_maxeigen(st, maxeigen);    

    free_ivector(ifz, 1);
    free_ivector(fzord, 1);

/*
                    for (k=1;k<=sps->ng;k++) {
                        for (j=1;j<=matd;j++) {
                            printf("%3d ", st->fz[k][j]);
                        }
                        printf("\n");
                    } 

*/
}

/*
 * calculate matrices st->b and st->dc
 * calculate rms
 */
static void lsq1(MBLOCK *mblk)
{
    SIMTYPE *st   = mblk->siminf->st;
    int kv, nt, la, matd, j, k;
    float er2;
    SPINSYSTEM *sps = &(st->sps[st->sps_pos]);

    kv = 1;
    nt = 0;
    la = 0;
    matd = 0;
    
    for (j=1;j<=st->maxlines;j++) {
        st->b[j] = 0;
        for (k=1;k<=st->maxparam;k++)
            st->dc[k][j] = 0;
    }
    
    for (j=1;j<=st->iay;j++) {
        int isub, l,lb;
        if (st->submat[j].imar <= 1) 
            continue;
        isub = st->submat[j].nmat - 1;
        lb = 0;
        for (l=1;l<=isub;l++) {
            int ma, moa, mob;
            moa = st->submat[j].nsub[l];
            mob = st->submat[j].nsub[l+1];
            lb = la + moa;
            for (ma=1;ma<=moa;ma++) {
                int mb, m = ma+la;
                for (mb=1;mb<=mob;mb++) {
                    int k, n = mb+lb;
                    nt++;
                    for (k=kv;k<=sps->nl;k++) {
                        if (sps->il[k] == nt) {
                            int i;
                            /*
                             * Calculate difference in exp and calc freq
                             * f[k] - (e[m] - e[n])
                             */
                            st->b[k] = sps->f[k] + st->e[n] - st->e[m];
                            /*
                             * Calculate dependence (weight) of parameter i
                             * for transition m -> n
                             */
                            for (i=1;i<=sps->nos;i++) 
                                st->dc[i][k] = st->d[i][m] - st->d[i][n];
                            kv = k;
                        }
                    }
                }
            }
            la += moa;
        }
        matd += st->submat[j].imar;
        la = matd;
    }
    if (st->it == 0) {
        
        psnd_printf(mblk,"TABLE OF ASSIGNED TRANSITIONS\n");
        psnd_printf(mblk,"%4s  %5s %15s %15s %15s\n",
                    "LINE", "TRANS", "CALC.", "EXP.", "ERROR");
        for (j=1;j<=sps->nl;j++) {
            float cf1;
            cf1 = sps->f[j] - st->b[j];
            if (st->ppm) 
                psnd_printf(mblk,"%4d  %5d %15.3f %15.3f %15.3f\n",
                        j,sps->il[j],cf1/st->sf,sps->f[j]/st->sf,st->b[j]/st->sf);
            else
                psnd_printf(mblk,"%4d  %5d %15.1f %15.1f %15.1f\n",
                        j,sps->il[j],cf1,sps->f[j],st->b[j]);
        }
    }
    /*  
     *  -------------------------------------------
     *  2.originally% subroutine error9
     *  -------------------------------------------
     */
    er2 = 0.0;
    for (k=1;k<=sps->nl;k++)
        er2 += POW2(st->b[k]);
    er2 = sqrt(er2/sps->nl);
    /*
     *   results per it cycle on screen
     */
    if (st->show_iterations == SHOW_ALL)
        psnd_printf(mblk,"iteration no. %2d     r.m.s.= %7.3f  hz\n", st->it, er2);
    st->nexit = 1;
    if (er2 >= (0.99 * st->er1) || st->it >= st->ni) 
        st->nexit = 0;
    st->er1 = er2;
    if (sps->il[sps->nl] > nt) {
        psnd_printf(mblk,"error: check transition number of assigned transitions.\n");
        st->iout = 1;
    }
    
}


static int lsq2(SIMTYPE *st)
{
    int i,j,k,l;
    float *bv, *bb, *c;
    SPINSYSTEM *sps = &(st->sps[st->sps_pos]);

    if (sps->nl < 1) {
        fprintf(stderr,"error: No transitions assigned\n");
        return FALSE;
    }
    bv	= fvector(1,st->maxparam);
    bb	= fvector(1,st->maxparam);
    c	= fvector(1,st->maxparam);

    /*
     *  -------------------------------------------
     *  1.originally% subroutine normal
     *  -------------------------------------------
     */
    for (j=1;j<=sps->nos;j++) {
        for (k=1;k<=sps->nos;k++) {
            st->va[k][j] = 0.0;
            for (i=1;i<=sps->nl;i++) 
                st->va[k][j] += st->dc[j][i] * st->dc[k][i];
            st->va[j][k] = st->va[k][j];
        }
        bv[j] = 0.0;
        for (i=1;i<=sps->nl;i++)  
            bv[j] += st->dc[j][i] * st->b[i];
    }
    if (st->nexit == 0) {
        free_fvector(bv,1);
        free_fvector(bb,1);
        free_fvector(c,1);
        return TRUE;
    }
    /*
     *  ------------------------------------------------
     *  2. originally% subroutine to invert matrix va
     *  ------------------------------------------------
     */
    for (j=1;j<=sps->nos;j++)
        st->lz[j] = j;
    for (i=1;i<=sps->nos;i++) {
        int lp;
        float y;

        k = i;
        y = st->va[i][i];
        lp = i+1;
        if (sps->nos >= lp) 
            for (j=lp;j<=sps->nos;j++) {
                float yy = st->va[j][i];
                if (fabs(yy) <= fabs(y)) 
                    continue;
                k = j;
                y = yy;
        }
        /*
         * If wrong iteration parameters are chosen (see pardep()), 
         * fabs(y) becomes very small (< 0.001)
         * and the corrections grow VERY large. So
         * better skip when y is too small.
         * AK 1998
         */
        if (fabs(y) < 0.001) 
            continue;
        /*
         * If test above is removed, this must stay anyway
         * to prevent divide-by-zero error
         */
        if (y==0) {
            fprintf(stderr,"error: Divide by 0 in lsq2\n");
            free_fvector(bv,1);
            free_fvector(bb,1);
            free_fvector(c,1);
            return FALSE;
        }
        for (j=1;j<=sps->nos;j++) {
            c[j]          = st->va[k][j];
            st->va[k][j]  = st->va[i][j];
            st->va[i][j]  = -c[j]/y;
            st->va[j][i] /= y;
            bb[j]         = st->va[j][i];
        }
        st->va[i][i] = 1.0/y;
        j = st->lz[i];
        st->lz[i] = st->lz[k];
        st->lz[k] = j;
        for (k=1;k<=sps->nos;k++) {
            if (i == k) 
                continue;
            for (j=1;j<=sps->nos;j++) {
                if (i != j) 
                    st->va[j][k] -= bb[j] * c[k];
            }
        }
    }
    for (i=1;i<=sps->nos;i++) {
        if (i == st->lz[i]) 
            continue;
        k = i+1;
        for (j=k;j<=sps->nos;j++) {
            int m;
            if (i != st->lz[j]) 
                continue;
            m         = st->lz[i];
            st->lz[i] = st->lz[j];
            st->lz[j] = m;
            for (l=1;l<=sps->nos;l++) {
                c[l]         = st->va[l][i];
                st->va[l][i] = st->va[l][j];
                st->va[l][j] = c[l];
            }
        }
    }
    /*
     *  -------------------------------------------
     *  3.originally% subroutine correct
     *     compute and apply corrections
     *  -------------------------------------------
     */
    for (j=1;j<=sps->nos;j++) {
        int n;
        st->corr[j] = 0.0;
        for (n=1;n<=sps->nos;n++) 
            st->corr[j] += st->va[n][j] * bv[n];
        for (k=1;k<=st->maxgroup;k++) {
            if (sps->ia[k][j] == 0) 
                break;
            i = sps->ia[k][j];
            /*
             * Correct coupling ...
             */
            if (sps->ib[k][j] != 0) {
                l = sps->ib[k][j];
                sps->spin[i].cc[l] += st->corr[j];
                if (sps->spin[i].iso == sps->spin[i].iso &&
                    sps->spin[i].sqn == sps->spin[i].sqn) 
                    sps->spin[l].cc[i] = sps->spin[i].cc[l];
            }
            /*
             * ... or chemical shift
             */
            else
                sps->spin[i].w += st->corr[j];
        }
        st->corr2[j] = st->corr[j];
        st->corr[j] = 0.0;
    }
    free_fvector(bv,1);
    free_fvector(bb,1);
    free_fvector(c,1);
    return TRUE;
}






/*
 * Show result of iterration
 */
static void itpar(MBLOCK *mblk)
{
    int j,k,l,n,nec;
    float dev;
    int   *nea, *neb;		
    float *pe;		
    SIMTYPE    *st  = mblk->siminf->st;
    SPINSYSTEM *sps = &(st->sps[st->sps_pos]);

    if (!st->has_data)
        return;
    nea	= ivector(1,sps->nos * sps->maxgroup);
    neb	= ivector(1,sps->nos * sps->maxgroup);
    pe	= fvector(1,sps->nos * sps->maxgroup);
    /*
     *  -------------------------------------------
     *  1.error analysis
     *  -------------------------------------------
     */
    for (k=1,l=1;k<=sps->nos;k++) 
        for (j=1;j<=k;j++) 
            st->a[l++] = st->va[k][j];
    givens(sps->nos, st->vec, st->root, st->a);

    if (sps->nl - sps->nos == 0) 
        dev = POW2(st->er1);
    else
        dev = (sps->nl * POW2(st->er1))/fabs(sps->nl - sps->nos);
    for (j=1;j<=sps->nos;j++) 
        st->corr[j] = 0;
    for (n=1;n<=sps->nos;n++) {
        float er2,yx;

        yx  = fabs(st->root[n]);

        if (yx==0) 
            er2 = 0.0;
        else
            er2 = sqrt(dev/yx);
        for (j=1;j<=sps->nos;j++) 
            st->corr[j] += POW2(st->vec[n][j] * er2);
    }
    for (n=1;n<=sps->nos;n++)
        st->corr[n] = 0.6745 * sqrt(fabs(st->corr[n]));
    nec = 0;
    for (j=1;j<=sps->nos;j++) {
        for (k=1;k<=st->maxgroup;k++) {
            if (sps->ia[k][j] == 0)
                break;
            nec++;
            neb[nec] = sps->ib[k][j];
            nea[nec] = sps->ia[k][j];
            pe[nec]  = st->corr[j];
        }
    }
    /*
     *  -------------------------------------------
     *  2.output iterated parameters
     *  -------------------------------------------
     */
    psnd_printf(mblk,"Iterated parameters:\n");
    psnd_printf(mblk,"Isotope     Chemical shifts    Correction           Errors\n");
    for (k=1;k<=sps->ng;k++) {
        int i,ok = FALSE;
        for (i=1;i<=nec;i++) {
            if ((neb[i] == 0) && (nea[i] == k))  {
                ok = TRUE;
                break;
            }
        }
        if (st->ppm) {
           if (!ok) 
                psnd_printf(mblk,"%2d         w(%1d) %10.3f ppm not iterated\n", 
                    sps->spin[k].iso,k,sps->spin[k].w/st->sf);
            else 
                psnd_printf(mblk,"%2d         w(%1d) %10.3f ppm  %8.3f ppm      %6.3f ppm\n",
                    sps->spin[k].iso,k,sps->spin[k].w/st->sf, st->corr2[i]/st->sf,pe[i]/st->sf);
        }
        else {
            if (!ok) 
                psnd_printf(mblk,"%2d         w(%1d) %10.3f hz  not iterated\n", 
                    sps->spin[k].iso,k,sps->spin[k].w);
            else 
                psnd_printf(mblk,"%2d         w(%1d) %10.3f hz   %8.3f hz       %6.3f hz\n",
                    sps->spin[k].iso,k,sps->spin[k].w, st->corr2[i],pe[i]);
        }
    }
    psnd_printf(mblk,"Coupling constants    Correction          Errors\n");
    for (k=1;k<=sps->ng-1;k++) {
        for (l=k+1;l<=sps->ng;l++) {
            int i,ok = FALSE;
            char *temp;
            for (i=1;i<=nec;i++) {
                if ((neb[i] == l) && (nea[i] == k))  {
                    ok = TRUE;
                    break;
                }
            }
            temp = psnd_sprintf_temp("j(%d,%d)",k,l);
            if (ok) 
                psnd_printf(mblk,"%-8s%9.3f hz   %8.3f hz       %6.3f hz\n",
                        temp,sps->spin[l].cc[k],st->corr2[i],pe[i]);
            else
                psnd_printf(mblk,"%-8s%9.3f hz     not iterated\n",
                        temp,sps->spin[l].cc[k]);
        }
    }
    free_ivector(nea,1);
    free_ivector(neb,1);
    free_fvector(pe,1);
}

/*
 *  1 * 2 * 3 * ... * n
 */
static float factor(int n)
{
    float prod = 1.0;
    int i;

    for (i=1;i<=n;i++)
        prod *= (float) i;
    return prod;
}

static int compar_trans(const void *t1, const void *t2)
{
    TRANSITION_TYPE *trans1, *trans2;
    
    trans1 = (TRANSITION_TYPE *) t1;
    trans2 = (TRANSITION_TYPE *) t2;
    if (trans1->deig == trans2->deig)
        return 0;
    if (trans1->deig < trans2->deig)
        return 1;
    return -1;
}


/*
 * Calculate, order and print transitions
 */
static void spectr(MBLOCK *mblk, int flag)
{
    int la, lb, nt, matd, lm, i, isub, j, k, l, m, ii, jj, kk, mm;
    int moa, mob, jl, lctm;
    float f1,f2,fmin, fmax, weight;
    SIMTYPE *st   = mblk->siminf->st;
    SPINSYSTEM *sps = &(st->sps[st->sps_pos]);
    float fnorm;

    if (!st->has_data)
        return;
    f2      = st->fright;
    f1      = st->fleft;
    /*
     *   -------------------------------------------
     *   compute nmr spectrum
     *   -------------------------------------------
     */
    la       = 0;
    sps->lct = 0;
    nt       = 0;
    matd     = 0;
    lm       = 1;
    fmin     = 1.e5;
    fmax     = -1.e5;
    rewind_fmatrix(st->kt1);

    fnorm = 10000.0;
    for (i=1;i<=sps->ng;i++) {
        float scale = 1.0;
        for (j=1;j<=sps->ng;j++) {
            float tmp = (float) sps->spin[j].sqn; 
            if (i != j)  {
                tmp = (tmp + 1.0)/2.0;
            }
            scale *= tmp;
        }
        if (scale < fnorm)
            fnorm = scale;
        sps->spin[i].sscale = scale;
    }
    for (i=1;i<=sps->ng;i++) 
        sps->spin[i].sscale /= fnorm;
    for (jj=1;jj<=st->iay;jj++) {
        if (st->submat[jj].imar <= 1) 
            continue;
        /*
         *  -------------------------------------------
         *  1. calculate weighting coefficients for
         *     intensities
         *  -------------------------------------------

            Composite particle states for p equivalent nuclei (spin=1/2)
                F   p=1   2   3   4   5   6

                0     -   S   -  2S   -  5S
               1/2    D   -  2D   -  5D   -
                1         T   -  3T   -  9T
               3/2            Q   -  4Q   -
                2                Qt   - 5Qt
               5/2                   Sx   -
                3                        Sp

               e.g. 5Qt => 5 = weight, Qt = 5 = (No of sub-submatrices - 1)


               weight = nngi!/[((nngi-nspj)/2)! * (1+(nngi+nspj)/2)!]


            Composite particle states for p equivalent nuclei (spin=1)
                F   p=1   2   3   4   5   6

                0     -   S   S   
                1     T   T  3T
                2         Qt 2Qt
                3             Sp

                |||	|||||	|||||||
                	 |||	 |||||
                          |	 |||||
                              	  |||
                                  |||
                                  |||
                                   |

         */
        weight = 1.0;
        if (st->iay != 1) {
            int pos;
            for (i=1;i<=sps->ng;i++) {


#ifdef meuuu
float zzz;
                int nspj, nngi, iarg1, iarg3;
                /* |Fa,ma>
                 * number of spins for sub-matrix jj
                 * = F * 2
                 */
                nspj  = st->submat[jj].nsp[i];
                /*
                 * number of equivalent spins for 'i' 
                 * = p
                 */
                nngi  = sps->spin[i].nng;
                iarg1 = (nngi-nspj)/2;
                iarg3 = 1+(nngi+nspj)/2;
      zzz=0;
                if (iarg1 > 0) 
                   /* weight *= */ zzz= (float)(nspj+1) * factor(nngi)/
                         (factor(iarg1) * factor(iarg3));
                         
/*
printf("weight = %g, nspj = %d, nngi = %d, iarg1 = %d, iarg3 = %d, jj = %d, i = %d: sqn=%d\n",
weight, nspj, nngi, iarg1, iarg3, jj, i,sps->spin[i].sqn);
*/

#endif


               
                pos = 1+((sps->spin[i].nng * sps->spin[i].sqn)-st->submat[jj].nsp[i])/2 ;
                if (pos > 1)
                    weight *= (float) bino(sps->spin[i].sqn, sps->spin[i].nng, pos);
/*
  printf("pos=%d weigth=%g \n",pos,weight);
*/

            }
        }
/*        
printf("weight = %g\n",weight);       
*/
        /*
         *  -------------------------------------------
         *  calculate transformation matrix va(j,k)
         *  -------------------------------------------
         */
        lb = 0;
        retrieve_fmatrix(st->kt1, st->vb, 1);
        isub = st->submat[jj].nmat - 1;
        for (l=1;l<=isub;l++) {
            moa = st->submat[jj].nsub[l];
            mob = st->submat[jj].nsub[l+1];
            lb = la + moa;
            for (j=1;j<=moa;j++) {
                jl = st->lfz[j+la];
                for (k=1;k<=mob;k++) {
                    int iflag, kl;

                    st->va[k][j] = 0.0;
                    kl           = st->lfz[k+lb];
                    iflag        = 0;
                    st->ist[1]   = 0;
                    /*
                     * Set flag for allowed transitions.
                     * fz = spin states x 2
                     */
                    for (i=1;i<=sps->ng;i++) {
                        if (st->fz[i][jl] != st->fz[i][kl]) {
                            iflag++; 
                            if (iflag > 1) 
                                break;
                            if (st->fz[i][jl] - st->fz[i][kl] == 2)
                                st->ist[iflag] = i;
                        }
                    }
                    if (iflag != 1)    
                        continue;    
/*
                    if (st->iay == 1) { 
                        st->va[k][j] = 1.0;
                        continue;     
                    }
*/
                    if (st->ist[1] <= 0) {
                        continue;
                    }
                    i = st->ist[1];
/*
                    st->va[k][j] = 0.5 * 
                        sqrt((float)((st->submat[jj].nsp[i] + st->fz[i][jl]) *
                        (st->submat[jj].nsp[i] - st->fz[i][kl])));
*/

/*

va scaling

degenaration of particle spin states of D, T, Q ..

                           1
                         2   2
                       3   4   3
                     4   6   6   4
                   5   8   9   8   5
                 6   10  12  12  10  6
               7   12  15  16  15  12  7
             8   14  18  20  20  18  14  8
           9   16  21  24  25  24  21  16  9
         10  18  24  28  30  30  28  24  18  10

degenaration of particle spin states 1

                           1   1
                         2    3   2
                       3   6  6  6  3
1 4 7 12 18 18 12 7 4 1
        
number of allowed transitions


                bbbb
4x
         abbb babb bbab bbba
12x
    aabb abab abba baab baba bbaa
12x
         aaab aaba abaa baaa
4x
                aaaa
                
4:12:12:4 = 1:3:3:1

k=1

                    st->va[k][j] =1.0; 

*/
             
                    if (sps->spin[i].nng > 0  ) {
                                            int ig;
                        float scale = 1.0;

                       int nspi = st->submat[jj].nsp[i]; 
                       int fzjl = st->fz[i][jl] ;
                       int fzkl = st->fz[i][kl] ;
                       int sqn  = st->submat[jj].sqn[i];
                       int bla  = (nspi + fzjl) * (nspi - fzkl);
                                  
#ifdef xxx
if (sqn == 3) {

 if (nspi == 2) {
   if (bla==8)
     bla = 24;

 }
 else if (nspi == 4 || nspi == 3) {
   if (bla==16)
     bla = 12;

 }
 else if (nspi == 66) {
   if (bla==40 || bla ==48)
     bla = 24;

 } 
#endif



                       bla /= sqn;

                       st->va[k][j] = 1.0/sqrt(4) *
                                             sqrt((float) (bla));

                       st->va[k][j] /= sqrt(sps->spin[i].sscale);
/*                        

printf("submatrix(jj)=%d subsub(l)=%d-%d spin(i)=%d  nng=%d sqn=%d: bla=%2d: bla/4=%2d: * weight(%g)=%2g:: va[k][j]= %.2f j=%d k=%d nspi+(%3d)*npsi-(%3d) = %d * %d = %d s=%g\n",
                                  jj,l,l+1,i,sps->spin[i].nng,sqn,bla,bla/4,
                                  weight,bla*weight/4,st->va[k][j],j,k,fzjl, 
                                  fzkl,nspi + fzjl,nspi - fzkl,
                                  (nspi + fzjl)*(nspi - fzkl)
                                  ,sps->spin[i].sscale);

*/

                    }


                }
            }
            /*
             *  -------------------------------------------
             *  2. calculate unweighted transition inten-
             *     sities (va(j,k))
             *  -------------------------------------------
             */
            for (ii=1;ii<=moa;ii++) 
                for (kk=1;kk<=mob;kk++) {
                    st->vc[kk][ii] = 0.0;
                    for (mm=1;mm<=moa;mm++)
                        st->vc[kk][ii] += st->va[kk][mm] * st->vb[ii][mm];
                }
            retrieve_fmatrix(st->kt1, st->vb, mob);
            for (j=1;j<=moa;j++) {
                for (k=1;k<=mob;k++) {
                    float s, deij;

                    st->va[k][j] = 0.0;
                    for (m=1;m<=mob;m++)
                        st->va[k][j] += st->vc[m][j] * st->vb[k][m];
                    /*
                     *  -------------------------------------------
                     *  3. calculate weighted intensities and fre-
                     *     quencies for transitions whose intensity
                     *     is greater than amin
                     *  -------------------------------------------
                     */
                    nt++;
                    s = weight * POW2(st->va[k][j]);
                    if (s < st->amin) 
                        continue;
                    deij = st->e[j+la] - st->e[k+lb];
                    if (deij > fmax) 
                        fmax = deij;
                    if (deij < fmin) 
                        fmin = deij;
                    if (deij < f2 || deij > f1) 
                        continue;
                    if (sps->lct >= sps->maxtransitions) {
                        sps->maxtransitions += MAXTRANSITIONS;
                        sps->trans = alloc_transitions(mblk,sps->trans, 
                                             1, sps->maxtransitions);
                    }
/*
    float deig;		calc frequencies		
    float sumsq;	intensities			
    float sper;		exp. frequencies		
    int   np;		Transition will be iterated flag
    int   not;		transition numbers		
    int   lal;		origin from			
    int   lbl;		origin to			
*/
                    sps->lct++;
                    sps->trans[sps->lct].deig   = deij;
                    sps->trans[sps->lct].sumsq  = s;
                    sps->trans[sps->lct].not    = nt;
                    sps->trans[sps->lct].lal    = j + la;
                    sps->trans[sps->lct].lbl    = k + lb;
                    sps->trans[sps->lct].np     = NOITER;
                }
            }
            la += moa;
        }
        matd += st->submat[jj].imar;
        la = matd;
    }
    l = 1;
    for (j=1;j<=sps->lct;j++) {
        sps->trans[j].sper = 0.0;
        for (k=l;k<=sps->nl;k++) {
            if (sps->trans[j].not == sps->il[k]) {
                sps->trans[j].sper = sps->f[k];
                sps->trans[j].np = ITER;
                break;
            }
        }
    }
    /*
     *  -------------------------------------------
     *  4. order lines in storage
     *  -------------------------------------------
     */

    qsort((void*) (sps->trans+1), sps->lct, sizeof(TRANSITION_TYPE),
              compar_trans);
#ifdef meuksort
    lctm = sps->lct-1;
    for (j=1;j<=lctm;j++) {
        float deigj;
        kk    = 0;
        deigj = sps->trans[j].deig;
        for (k=j+1;k<=sps->lct;k++) {
            if (deigj >= sps->trans[k].deig) 
                continue;
            deigj = sps->trans[k].deig;
            kk = k;
        }
        if (kk != 0) {
            TRANSITION_TYPE trans;
            memcpy(&trans, &(sps->trans[j]), sizeof(TRANSITION_TYPE));
            memcpy(&(sps->trans[j]), &(sps->trans[kk]), sizeof(TRANSITION_TYPE));
            memcpy(&(sps->trans[kk]), &trans, sizeof(TRANSITION_TYPE));
        }
    }
#endif
    /*
     *  -------------------------------------------
     *  5. output ordered transitions
     *  -------------------------------------------
     *
     */
    if (flag == SHOW_NONE)
        return;
    if (!(f1 > fmax && f2 < fmin)) {
        fmin -= 0.05;
        fmax += 0.05;
        psnd_printf(mblk,"Warning: spectrum %7.1f -%7.1f hz",
                     fmin,fmax);
        psnd_printf(mblk," is not inside limits %7.1f -%7.1f hz\n",
                     f2,f1);
    }
    if (st->ni == 0) {
        if (flag != SHOW_ALL)
            return;
        psnd_printf(mblk,"Trial spectrum\n");
        psnd_printf(mblk,"%4s  %5s %15s        %11s    %10s\n",
                    "line", "trans.", "frequency", 
                    "int.", "origin");
        psnd_printf(mblk,"     %5s  %10s %10s \n",
                    "no.", "calc.", "exp.");
        for (j=1;j<=sps->lct;j++) {
            psnd_printf(mblk,"%4d  %5d %10.3f            %11.2f  e(%3d) - e(%3d)\n",
                        j, sps->trans[j].not,sps->trans[j].deig,
                        sps->trans[j].sumsq,sps->trans[j].lal,sps->trans[j].lbl);
                            
        }
    }
    else {
        psnd_printf(mblk,"Fitted spectrum\n");
        psnd_printf(mblk,"%4s  %5s %15s      %7s   %11s    %10s\n",
                    "line", "trans.", "frequency", "error", 
                    "int.", "origin");
        psnd_printf(mblk,"     %5s  %10s %10s \n",
                    "no.", "calc.", "exp.");
        for (j=1;j<=sps->lct;j++) {
            if (sps->trans[j].sper != 0.0) {
                float s = sps->trans[j].sper - sps->trans[j].deig;
                psnd_printf(mblk,"%4d  %5d %10.3f %10.3f %7.3f %11.2f  e(%3d) - e(%3d)\n",
                            j, sps->trans[j].not,sps->trans[j].deig,sps->trans[j].sper,
                            s,sps->trans[j].sumsq,sps->trans[j].lal,sps->trans[j].lbl);
            }
            else if (flag == SHOW_ALL) {            
                psnd_printf(mblk,"%4d  %5d %10.3f                    %11.2f  e(%3d) - e(%3d)\n",
                            j, sps->trans[j].not,sps->trans[j].deig,
                            sps->trans[j].sumsq,sps->trans[j].lal,sps->trans[j].lbl);
                            
            }
        }
    }
}

/*
 *   ------------------------------------------------
 *   computation of the frequency dependence on nmr
 *   parameters
 *   ------------------------------------------------
 */
#define FIT_ON_LINE	13	/* The number of items to print on 1 line */
static void pardep(MBLOCK *mblk, int show_iterated_flag)
{
    int la, j, jj, k, kk, moa, is, ii;
    SIMTYPE *st   = mblk->siminf->st;
    SPINSYSTEM *sps = &(st->sps[st->sps_pos]);

    la = 0;
    rewind_fmatrix(st->kt1);
    for (jj=1;jj<=st->iay;jj++) {
        for (kk=1;kk<=st->submat[jj].nmat;kk++) {
            moa = st->submat[jj].nsub[kk];
            retrieve_fmatrix(st->kt1, st->vb, moa);
            differ(moa,la,jj,st);
            la += moa;
        }
    }
    is = 0;
    do {
        int ns, js, ks, kal, kbl;
        ii = is+1;
        is = ii + FIT_ON_LINE;
        if (sps->nos <= is) 
            is = sps->nos;
        psnd_printf(mblk,"Dependence of the transition frequency on nmr parameters\n");
        psnd_printf(mblk,"(* denotes transitions assigned for the iterative computation)\n");
        psnd_printf(mblk,"  trans.      int.   frequency           parameters\n");
        psnd_printf(mblk,"    no.                calc.\n");

        for (kk=1;kk<=st->maxgroup;kk++) {
            int ok = FALSE;
            for (js=ii;js<=is;js++) {
                if (sps->ia[kk][js] > 0) {
                    ok = TRUE;
                    break;
                }
            }
            if (!ok)
                break;
            psnd_printf(mblk,"                                     ");
            for (ks=ii;ks<=is;ks++) {
                char *label;
                if (sps->ib[kk][ks] != 0)
                    label = psnd_sprintf_temp("j(%d,%d)", sps->ia[kk][ks], sps->ib[kk][ks]);
                else if (sps->ia[kk][ks] != 0)
                    label = psnd_sprintf_temp("w(%d)", sps->ia[kk][ks]);
                else
                    label = psnd_sprintf_temp("%s","");
                psnd_printf(mblk,"%-7s ", label);
            }
            psnd_printf(mblk,"\n");
        }
        for (j=0;j<80;j++)
            psnd_printf(mblk,"=");
        psnd_printf(mblk,"\n");
        for (j=1;j<=sps->lct;j++) {
            kal = sps->trans[j].lal;
            kbl = sps->trans[j].lbl;
            if (show_iterated_flag != 0 && sps->trans[j].np == NOITER)
                continue;
            psnd_printf(mblk,"  %5d%11.2f %10.3f %c     ",
                sps->trans[j].not,sps->trans[j].sumsq,sps->trans[j].deig,sps->trans[j].np);
            for (ns=ii;ns<=is;ns++)
                psnd_printf(mblk,"%6.2f  ",  st->d[ns][kal] - st->d[ns][kbl]);
            psnd_printf(mblk,"\n");
        }
    } while (is < sps->nos);
}


/****************************************************************************/
/****************************************************************************
 *
 *     POPUP STUFF
 *
 ***************************************************************************/
/****************************************************************************/


typedef enum {
    TRANSITION_LISTBOX,
    TRANSITION_ADD,
    TRANSITION_EDIT,
    TRANSITION_REMOVE,
    TRANSITION_RESET
} popup_transition_ids;



static void clear_transition_list(MBLOCK  *mblk)
{
    int i;
    POPUP_INFO *popinf = mblk->popinf + POP_SIMASSIGN;
    SIMTYPE *st        = mblk->siminf->st;
    SPINSYSTEM *sps    = &(st->sps[st->sps_pos]);
    /* block_id is used for select. Reset */
    popinf->block_id = 0;
    /*
    for (i=sps->nl;i > 0;i--) 
        g_popup_remove_item(popinf->cont_id, TRANSITION_LISTBOX, i);
*/
    g_popup_remove_item(popinf->cont_id, TRANSITION_LISTBOX, -1);
    g_popup_append_item(popinf->cont_id, TRANSITION_LISTBOX, TRANSLABEL);
}

static void load_transition_list(MBLOCK  *mblk)
{
    int i;
    POPUP_INFO *popinf = mblk->popinf + POP_SIMASSIGN;
    SIMTYPE *st        = mblk->siminf->st;
    SPINSYSTEM *sps    = &(st->sps[st->sps_pos]);
    /* block_id is used for select. Reset */
    popinf->block_id = 0;
    for (i=1;i<=sps->nl;i++) {
        char *label;
        label = psnd_sprintf_temp("%-2d   %.2f", sps->il[i], sps->f[i]);
        g_popup_append_item(popinf->cont_id, TRANSITION_LISTBOX,label);
    }
}

static void rebuild_transition_list(int cont_id, SIMTYPE *st)
{
    int i;
    SPINSYSTEM *sps = &(st->sps[st->sps_pos]);
    /*
    for (i=sps->nl;i > 0;i--) 
        g_popup_remove_item(cont_id, TRANSITION_LISTBOX, i);
    */
    g_popup_remove_item(cont_id, TRANSITION_LISTBOX, -1);
    g_popup_append_item(cont_id, TRANSITION_LISTBOX, TRANSLABEL);
    for (i=1;i<=sps->nl;i++) {
        char *label;
        label = psnd_sprintf_temp("%-2d   %.2f", sps->il[i], sps->f[i]);
        g_popup_append_item(cont_id, TRANSITION_LISTBOX,label);
    }
}


static void assign_callback(G_POPUP_CHILDINFO *ci)
{
    MBLOCK  *mblk = (MBLOCK  *) ci->userdata;
    SIMTYPE *st   = mblk->siminf->st;
    SPINSYSTEM *sps = &(st->sps[st->sps_pos]);
    POPUP_INFO *popinf = mblk->popinf + POP_SIMASSIGN;

    switch (ci->type) {
        case G_CHILD_LISTBOX:
            /* block_id is used for select */
            popinf->block_id = ci->item;
            break;
        case G_CHILD_PUSHBUTTON:
            switch (ci->id) {
                case TRANSITION_ADD:
                    {
                        int i=0,j, ok = FALSE;
                        float f=0.0;
                        if (psnd_ivalin(mblk,"Transition Nr.", 1, &i) == 0)
                            break;
                        for (j=1;j<=sps->lct;j++) {
                            if (sps->trans[j].not == i) {
                                ok = TRUE;
                                f = sps->trans[j].deig;
                                break;
                            }
                        }
                        if (!ok) {
                            psnd_printf(mblk,"%d not a valid transition\n",i);
                            break;
                        }
                        if (psnd_rvalin(mblk,"Frequency", 1, &f) == 0)
                            break;
                        if (sps->nl >= st->maxlines) 
                            resize_maxlines(st);
                        sps->nl++;
                        sps->il[sps->nl] = i;
                        sps->f[sps->nl]  = f;
                        rebuild_transition_list(ci->cont_id, st);
                    }
                    break;
                case TRANSITION_EDIT:
                    /* block_id is used for select */
                    if (popinf->block_id > 0) {
                        float f = sps->f[popinf->block_id];
                        char *label;
                        label = psnd_sprintf_temp("Edit Transition %d", 
                                                    sps->il[popinf->block_id]);
                        if (psnd_rvalin(mblk,label,1,&f) > 0) {
                            sps->f[popinf->block_id] = f;
                            label = psnd_sprintf_temp("%-2d   %.2f", 
                                                       sps->il[popinf->block_id], f);
                            g_popup_replace_item(ci->cont_id, 
                                                 TRANSITION_LISTBOX,
                                                 popinf->block_id,
                                                 label);
                        }
                    }
                    break;
                case TRANSITION_REMOVE:
                    /* block_id is used for select */
                    if (popinf->block_id > 0) {
                        remove_line(popinf->block_id, 0, sps);
                        g_popup_remove_item(ci->cont_id,
                                            TRANSITION_LISTBOX, 
                                            popinf->block_id);
                        popinf->block_id = 0;
                    }
                    break;
                case TRANSITION_RESET:
                    {
                        int i;
                        /*
                        for (i=sps->nl;i > 0;i--) 
                            g_popup_remove_item(ci->cont_id, 
                                                TRANSITION_LISTBOX, 
                                                i);
                                                */
                        g_popup_remove_item(popinf->cont_id, TRANSITION_LISTBOX, -1);
                        g_popup_append_item(popinf->cont_id, TRANSITION_LISTBOX, TRANSLABEL);
                        sps->nl = 0;
                        popinf->block_id = 0;
                    }
                    break;
            }
            break;
        case G_CHILD_OK:
        case G_CHILD_CANCEL:
            {
                int i;
                /*
                for (i=sps->nl;i >= 0;i--) 
                    g_popup_remove_item(ci->cont_id,TRANSITION_LISTBOX, i);
                    */
                g_popup_remove_item(popinf->cont_id, TRANSITION_LISTBOX, -1);
                g_popup_append_item(popinf->cont_id, TRANSITION_LISTBOX, TRANSLABEL);
                popinf->block_id = 0;
                /* block_id is used for select */
                popinf->block_id = 0;
            }
            break;
    }
}

/*
 * assign, edit or delete transitions
 */
static void popup_assign(MBLOCK *mblk)
{
    int i,id,ok = TRUE;
    char *list[10];
    char *label = TRANSLABEL;
    G_POPUP_CHILDINFO ci[20];
    SIMTYPE   *st   = mblk->siminf->st;
    SPINSYSTEM *sps = &(st->sps[st->sps_pos]);
    POPUP_INFO *popinf  = mblk->popinf + POP_SIMASSIGN;

    /* block_id is used for select */
    popinf->block_id = 0;
    if (! popinf->cont_id) {          
        int cont_id;
        popinf->cont_id = cont_id = g_popup_container_open(mblk->info->win_id, 
                                         "Assign Transitions",
                                         G_POPUP_KEEP);
        /*
         * Set the first member of the list to TRANSITIONS
         */
        list[0] = label;
        ok = FALSE;
        id=0;
        g_popup_init_info(&(ci[id]));
        ci[id].type          = G_CHILD_LISTBOX;
        ci[id].id            = TRANSITION_LISTBOX;
        ci[id].item_count    = 1;
        ci[id].data          = list;
        ci[id].item          = 0;
        ci[id].items_visible = 10;
        ci[id].func          = assign_callback;
        ci[id].userdata      = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type          = G_CHILD_PUSHBUTTON;
        ci[id].id            = TRANSITION_ADD;
        ci[id].label         = "Add Transition";
        ci[id].func          = assign_callback;
        ci[id].userdata      = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type          = G_CHILD_PUSHBUTTON;
        ci[id].id            = TRANSITION_EDIT;
        ci[id].label         = "Edit Transition";
        ci[id].func          = assign_callback;
        ci[id].userdata      = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type          = G_CHILD_PUSHBUTTON;
        ci[id].id            = TRANSITION_REMOVE;
        ci[id].label         = "Remove Transition";
        ci[id].func          = assign_callback;
        ci[id].userdata      = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));
        
        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type          = G_CHILD_PUSHBUTTON;
        ci[id].id            = TRANSITION_RESET;
        ci[id].label         = "Reset Transitions";
        ci[id].func          = assign_callback;
        ci[id].userdata      = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type          = G_CHILD_OK;
        ci[id].func          = assign_callback;
        ci[id].userdata      = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type          = G_CHILD_CANCEL;
        ci[id].func          = assign_callback;
        ci[id].userdata      = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

    }
    g_popup_container_show(popinf->cont_id);   
    g_popup_remove_item(popinf->cont_id, TRANSITION_LISTBOX, -1);
    /*
    ok = TRUE;
    if (ok)
    */
        g_popup_append_item(popinf->cont_id, TRANSITION_LISTBOX, TRANSLABEL);
    for (i=1;i<=sps->nl;i++) {
        char *label;
        label = psnd_sprintf_temp("%-2d   %.2f", sps->il[i], sps->f[i]);
        g_popup_append_item(popinf->cont_id,TRANSITION_LISTBOX, label);
    }

}



typedef enum {
    SHIFT_LISTBOX,
    SHIFT_EDIT,
    SHIFT_SPINS,
    SHIFT_ISOTOPE,
    SHIFT_QUANTUM_NUM,
    SHIFT_SHIFT
} popup_shift_ids;

static void clear_spins_and_couplings(MBLOCK  *mblk)
{
    int i,j;
    POPUP_INFO *popinf = mblk->popinf + POP_SIMEDIT;
    SIMTYPE *st        = mblk->siminf->st;
    SPINSYSTEM *sps    = &(st->sps[st->sps_pos]);

    /* block_id is used for select. Reset */
    popinf->block_id = 0;
    g_popup_remove_item(popinf->cont_id,SHIFT_LISTBOX, -1);
/*
    for (i=0;i<=sps->ng;i++) 
        g_popup_remove_item(popinf->cont_id,SHIFT_LISTBOX, 0);
    for (i=1;i<=sps->ng;i++) 
        for (j=i+1;j<=sps->ng;j++) 
            g_popup_remove_item(popinf->cont_id,SHIFT_LISTBOX, 0);
*/
}

static void load_spins_and_couplings(MBLOCK  *mblk)
{
    int i,j;
    POPUP_INFO *popinf = mblk->popinf + POP_SIMEDIT;
    SIMTYPE *st        = mblk->siminf->st;
    SPINSYSTEM *sps    = &(st->sps[st->sps_pos]);
    char labeltitle[] = "SHIFTS + COUPLINGS";

    /* block_id is used for select. Reset */
    popinf->block_id = 0;
    g_popup_append_item(popinf->cont_id, SHIFT_LISTBOX, labeltitle);
    for (i=1;i<=sps->ng;i++) {
        char *label;
        float w = sps->spin[i].w;
        if (st->ppm) {
            w /= st->sf;
            label = psnd_sprintf_temp("%-2d   %.4f", i, w);
        }
        else
            label = psnd_sprintf_temp("%-2d   %.2f", i, w);
        g_popup_append_item(popinf->cont_id,SHIFT_LISTBOX,label);
    }
    for (i=1;i<=sps->ng;i++) {
        int j;
        for (j=i+1;j<=sps->ng;j++) {
            char *label;
            label = psnd_sprintf_temp("j(%d,%d)   %.2f", 
                                       i, j, sps->spin[j].cc[i]);
            g_popup_append_item(popinf->cont_id,SHIFT_LISTBOX,label);
        }
    }
}


static void shifts_and_couplings_callback(G_POPUP_CHILDINFO *ci)
{
    MBLOCK    *mblk = (MBLOCK*) ci->userdata;
    SIMTYPE   *st   = mblk->siminf->st;
    SPINSYSTEM *sps = &(st->sps[st->sps_pos]);
    POPUP_INFO *popinf = mblk->popinf + POP_SIMEDIT;

    switch (ci->type) {
        case G_CHILD_LISTBOX:
            /* block_id is used for select */
            popinf->block_id = ci->item;
            break;
        case G_CHILD_PUSHBUTTON:
            switch (ci->id) {
                case SHIFT_EDIT:
                    /* block_id is used for select */
                    if (popinf->block_id > 0) {
                        if (popinf->block_id <= sps->ng) {
                            char *label;
                            float f = sps->spin[popinf->block_id].w;
                            if (st->ppm)
                                f /= st->sf;
                            label = psnd_sprintf_temp("Edit Shift %d", popinf->block_id);
                            if (psnd_rvalin(mblk,label,1,&f) > 0) {


                                if (st->ppm) 
                                    label = psnd_sprintf_temp("%-2d   %.4f",
                                                                popinf->block_id, f);
                                else
                                    label = psnd_sprintf_temp("%-2d   %.2f",
                                                                popinf->block_id, f);
                                g_popup_replace_item(ci->cont_id,SHIFT_LISTBOX,
                                                     popinf->block_id,label);
                                if (st->ppm)
                                    f *= st->sf;
                                sps->spin[popinf->block_id].w = f;
                                laocoon5(mblk);
                            }
                        }
                        else {
                            int i,j, s = sps->ng;
                            for (i=1;i<=sps->ng;i++) {
                                for (j=i+1;j<=sps->ng;j++) {
                                    s++;
                                    if (s == popinf->block_id) {
                                        char *label;
                                        float f = sps->spin[j].cc[i];
                                        label = psnd_sprintf_temp("Edit Coupling j(%d,%d)",
                                              i,j);
                                        if (psnd_rvalin(mblk,label,1,&f) > 0) {
                                            sps->spin[j].cc[i] = f;
                                            /*
                                            if (sps->spin[i].iso == sps->spin[j].iso)
                                                */
                                                sps->spin[i].cc[j] = f;
                                            label = psnd_sprintf_temp("j(%d,%d)   %.2f", i, j, f);
                                            g_popup_replace_item(ci->cont_id,
                                                           SHIFT_LISTBOX,popinf->block_id,label);

                                            laocoon5(mblk);
                                            return;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    break;
                case SHIFT_SPINS:
                    /* block_id is used for select */
                    if (popinf->block_id > 0) {
                        if (popinf->block_id <= sps->ng) {
                            int d = sps->spin[popinf->block_id].nng;
                            char *label;
                            float w = sps->spin[popinf->block_id].w;
                            if (st->ppm)
                                w /= st->sf;
                            label = psnd_sprintf_temp("Set Number of Spins for %d, (%f)", 
                                popinf->block_id, w);
                            if (popup_spin(mblk->info->win_id, label,
                                     &d, "Number of Spins", 1, st->maxgroup, 1)) {
                                sps->spin[popinf->block_id].nng = d;
                                laocoon5(mblk);
                            }
                        }
                     }
                     break;
                case SHIFT_SHIFT:
                    {
                        int i;
                        char *label;
                        float f =0.0;
                        if (st->ppm) 
                            label = psnd_sprintf_temp("Give delta shift in ppm");
                        else
                            label = psnd_sprintf_temp("Give delta shift in Hz");
                        if (psnd_rvalin(mblk,label,1,&f) > 0) {
                            if (st->ppm)
                                f *= st->sf;
                            for (i=1;i<=sps->ng;i++) {
                                sps->spin[i].w += f;
                                if (st->ppm) 
                                    label = psnd_sprintf_temp("%-2d   %.4f", i, 
                                                             sps->spin[i].w/st->sf);
                                else
                                    label = psnd_sprintf_temp("%-2d   %.2f", 
                                                             i, sps->spin[i].w);
                                g_popup_replace_item(ci->cont_id,SHIFT_LISTBOX,
                                                             i,label);
                            }
                            laocoon5(mblk);
                        }
                    }
                    break;
                case SHIFT_ISOTOPE:
                    /* block_id is used for select */
                    if (popinf->block_id > 0) {
                        if (popinf->block_id <= sps->ng) {
                            int d = sps->spin[popinf->block_id].iso;
                            char *label;
                            float w = sps->spin[popinf->block_id].w;
                            if (st->ppm)
                                w /= st->sf;
                            label = psnd_sprintf_temp("Set Isotope Number for Spin(s) at %d, (%f)", 
                                popinf->block_id, w);
                            if (popup_spin(mblk->info->win_id, label,
                                     &d, "Isotope Number", 1, 104, 1)) {
                                sps->spin[popinf->block_id].iso = d;
                                laocoon5(mblk);
                            }
                        }
                     }
                     break;
                case SHIFT_QUANTUM_NUM:
                    /* block_id is used for select */
                    if (popinf->block_id > 0) {
                        if (popinf->block_id <= sps->ng) {
                            int d = sps->spin[popinf->block_id].sqn - 1;
                            char *labels[] = { "1/2", " 1 ", "3/2", " 2 ", "5/2", " 3 ", " " };
                            char *label;
                            /*
                             * number of labels MUST be <= MAXSQN2
                             */
                            assert(6 <= MAXSQN2);
                            label = psnd_sprintf_temp("Set Spin Quantum Number");

/**************************
 At the moment only spin 1/2 and spin 1
                            if (popup_option(mblk->info->win_id, label,
                                     &d, "Quantum Number", 6, labels)) {
**************************/
                            if (popup_option(mblk->info->win_id, label,
                                     &d, "Quantum Number", 2, labels)) {

                                sps->spin[popinf->block_id].sqn = d + 1;
                                laocoon5(mblk);
                            }
                        }
                     }
                     break;
            }
            break;
        case G_CHILD_OK:
            simula_push_undo(sps->spin, st->maxspin, st);
        case G_CHILD_CANCEL:
            {
                int i,j;
                /*
                for (i=0;i<=sps->ng;i++) 
                    g_popup_remove_item(ci->cont_id,SHIFT_LISTBOX, 0);
                for (i=1;i<=sps->ng;i++) 
                    for (j=i+1;j<=sps->ng;j++) 
                        g_popup_remove_item(ci->cont_id,SHIFT_LISTBOX, 0);
                */
                g_popup_remove_item(popinf->cont_id,SHIFT_LISTBOX, -1);
                popinf->block_id = 0;
            }
            break;
    }
}

#define LISTITEMS	10
/*
 * View and edit the shifts and couplings
 */
static void popup_shifts_and_couplings(MBLOCK *mblk)
{
    int cont_id;
    int i,id,ok = TRUE;
    char *list[LISTITEMS];
    char labeltitle[] = "SHIFTS + COUPLINGS";
    G_POPUP_CHILDINFO ci[20];
    SIMTYPE *st = mblk->siminf->st;
    SPINSYSTEM *sps = &(st->sps[st->sps_pos]);
    POPUP_INFO *popinf  = mblk->popinf + POP_SIMEDIT;

    /* block_id is used for select */
    popinf->block_id = 0;
    if (! popinf->cont_id) {          
        int cont_id;
        cont_id = g_popup_container_open(mblk->info->win_id, "Set Values",
                       G_POPUP_KEEP);
        popinf->cont_id = cont_id;
        list[0] = labeltitle;
        ok = FALSE;
        id=0;
        g_popup_init_info(&(ci[id]));
        ci[id].type          = G_CHILD_LISTBOX;
        ci[id].id            = SHIFT_LISTBOX;
        ci[id].item_count    = 1;
        ci[id].data          = list;
        ci[id].item          = 0;
        ci[id].items_visible = LISTITEMS;
        ci[id].func          = shifts_and_couplings_callback;
        ci[id].userdata      = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type          = G_CHILD_PUSHBUTTON;
        ci[id].id            = SHIFT_EDIT;
        ci[id].label         = "Edit Shifts and Couplings";
        ci[id].func          = shifts_and_couplings_callback;
        ci[id].userdata      = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type          = G_CHILD_PUSHBUTTON;
        ci[id].id            = SHIFT_SPINS;
        ci[id].label         = "Set Number of Spins for Shift";
        ci[id].func          = shifts_and_couplings_callback;
        ci[id].userdata      = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type          = G_CHILD_PUSHBUTTON;
        ci[id].id            = SHIFT_SHIFT;
        ci[id].label         = "Shift all Shifts in Spin System";
        ci[id].func          = shifts_and_couplings_callback;
        ci[id].userdata      = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));
/*
        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type          = G_CHILD_PUSHBUTTON;
        ci[id].id            = SHIFT_ISOTOPE;
        ci[id].label         = "Set Isotope Number for Spin(s)";
        ci[id].func          = shifts_and_couplings_callback;
        ci[id].userdata      = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));
*/
        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type          = G_CHILD_PUSHBUTTON;
        ci[id].id            = SHIFT_QUANTUM_NUM;
        ci[id].label         = "Set Spin Quantum Number";
        ci[id].func          = shifts_and_couplings_callback;
        ci[id].userdata      = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type          = G_CHILD_OK;
        ci[id].func          = shifts_and_couplings_callback;
        ci[id].userdata      = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type          = G_CHILD_CANCEL;
        ci[id].func          = shifts_and_couplings_callback;
        ci[id].userdata      = (void*) mblk;
        g_popup_add_child(cont_id, &(ci[id]));
        assert(id<20);
    }
    g_popup_container_show(popinf->cont_id);   
    g_popup_remove_item(popinf->cont_id,SHIFT_LISTBOX, -1);
    load_spins_and_couplings(mblk);

    /*
    ok = TRUE;
    if (ok)
    
        g_popup_append_item(popinf->cont_id, SHIFT_LISTBOX, labeltitle);
    for (i=1;i<=sps->ng;i++) {
        char *label;
        float w = sps->spin[i].w;
        if (st->ppm) {
            w /= st->sf;
            label = psnd_sprintf_temp("%-2d   %.4f", i, w);
        }
        else
            label = psnd_sprintf_temp("%-2d   %.2f", i, w);
        g_popup_append_item(popinf->cont_id,SHIFT_LISTBOX,label);
    }
    for (i=1;i<=sps->ng;i++) {
        int j;
        for (j=i+1;j<=sps->ng;j++) {
            char *label;
            label = psnd_sprintf_temp("j(%d,%d)   %.2f", 
                                       i, j, sps->spin[j].cc[i]);
            g_popup_append_item(popinf->cont_id,SHIFT_LISTBOX,label);
        }
    }
    */
}

/*
 * Select the parameters that will be modified during the iteration
 */
static void popup_itpar(MBLOCK *mblk)
{
    SIMTYPE *st   = mblk->siminf->st;
    SPINSYSTEM *sps = &(st->sps[st->sps_pos]);
    int cont_id;
    G_POPUP_CHILDINFO ci[20];
    int i=0, j, k, ng, nos;
    char *line1[] = { "1",      "j(1,2)", "j(1,3)", "j(1,4)", "j(1,5)",
                      "j(1,6)", "j(1,7)", "j(1,8)", "j(1,9)", "j(1,10)"} ;
    char *line2[] = { "2",      "j(2,3)", "j(2,4)", "j(2,5)",
                      "j(2,6)", "j(2,7)", "j(2,8)", "j(2,9)", "j(2,10)"} ;
    char *line3[] = { "3",      "j(3,4)", "j(3,5)",
                      "j(3,6)", "j(3,7)", "j(3,8)", "j(3,9)", "j(3,10)"} ;
    char *line4[] = { "4",      "j(4,5)",
                      "j(4,6)", "j(4,7)", "j(4,8)", "j(4,9)", "j(4,10)"} ;
    char *line5[] = { "5",      
                      "j(5,6)", "j(5,7)", "j(5,8)", "j(5,9)", "j(5,10)"} ;
    char *line6[] = { "6",      "j(6,7)", "j(6,8)", "j(6,9)", "j(6,10)"} ;
    char *line7[] = { "7",      "j(7,8)", "j(7,9)", "j(7,10)"} ;
    char *line8[] = { "8",      "j(8,9)", "j(8,10)"} ;
    char *line9[] = { "9",      "j(9,10)"} ;
    char *line10[] = { "10",    ""} ;
    char **lines[10];
    int select[MAXSPINLIMIT][MAXSPINLIMIT];
    int id;

    lines[0] = line1;
    lines[1] = line2;
    lines[2] = line3;
    lines[3] = line4;
    lines[4] = line5;
    lines[5] = line6;
    lines[6] = line7;
    lines[7] = line8;
    lines[8] = line9;
    lines[9] = line10;
    if (sps->ng <=0)
        return;
    cont_id = g_popup_container_open(mblk->info->win_id, 
                                     "Set Iteration Parameters",
                                     G_POPUP_WAIT);

    for (i=0;i<MAXSPINLIMIT;i++)
        for (j=0;j<MAXSPINLIMIT;j++)
            select[i][j] = FALSE;
            
        for (i=1;i<=sps->nos;i++) {
            for (k=1;k<=st->maxgroup;k++) {
                int n,m;
                if (sps->ia[k][i] > 0 && sps->ia[k][i] <=MAXSPINLIMIT) {
                    n = sps->ia[k][i];
                    if (sps->ib[k][i] > 0 && sps->ib[k][i] <=MAXSPINLIMIT) {
                        m = sps->ib[k][i]-n;
                        select[n-1][m] = TRUE;
                    }
                    else
                        select[n-1][0] = TRUE;
                }
            }
        }

    ng  = min(sps->ng, MAXSPINLIMIT);
    for (j=0;j<ng;j++) {
        g_popup_init_info(&(ci[j]));
        ci[j].type       = G_CHILD_CHECKBOX;
        ci[j].id         = j;
        ci[j].frame      = TRUE;
        ci[j].item_count = ng - j;
        ci[j].data       = lines[j];
        ci[j].select     = select[j];
        ci[j].horizontal = TRUE;
        g_popup_add_child(cont_id, &(ci[j]));
    }

    if (g_popup_container_show(cont_id)) {
        for (i=1;i<=st->maxgroup;i++) {
            for (j=1;j<=st->maxparam;j++) {
                sps->ia[i][j] = 0;
                sps->ib[i][j] = 0;
            }
        }
        sps->nos = 0;
        for (j=0;j<ng;j++) {
            k=0;
            for (i=0;i<ng - j;i++) {
                if (ci[j].select[i]) {
                    if (k==0) {
                        if (sps->nos >= st->maxparam) 
                            resize_maxparam(st);
                        sps->nos++;
                    }
                    k++;
                    sps->ia[k][sps->nos] = j+1;
                    if (i > 0)
                        sps->ib[k][sps->nos] = j+i+1;
                }
                if (k == st->maxgroup)
                    k=0;
            }
        }
    }
}

#define ASSIGN_ALL	10
#define ASSIGN_NONE	11
#define OFFSET_ASSIGN	1000
#define OFFSET_FREQ	2000
static void transition_callback(G_POPUP_CHILDINFO *ci)
{
    int *checks = (int*) ci->userdata;
    int i, numchecks, doit, position = 0;
    char *temp;

    switch (ci->type) {
        case G_CHILD_PUSHBUTTON:
            if (ci->id == ASSIGN_ALL)
                doit = TRUE;
            else
                doit = FALSE;
            numchecks = checks[0];
            temp = psnd_sprintf_temp("%s",g_popup_get_label(ci->cont_id, 
                                             checks[1]-OFFSET_ASSIGN+OFFSET_FREQ));
            for (i=1;i<=numchecks;i++) {
                g_popup_set_checkmark(ci->cont_id, checks[i], position, doit);
                if (doit)
                    g_popup_set_label(ci->cont_id, 
                                      checks[i]-OFFSET_ASSIGN+OFFSET_FREQ, temp);
            }
            break;
    }
}

#define TABSIZE	8

static int popup_transition_box(MBLOCK *mblk, int num, float *freqs, int *assig,
                       int *trnos, float *intens)
{
    int cont_id;
    int i,j,id, flag, maxid,ret = FALSE;
    char *label;
    G_POPUP_CHILDINFO *ci;
    int  *checks;
    int  numchecks = 0;
    char *checklabel[] = { "Assign. Freq: " };
    int use_tabs = FALSE;
    int tab_no = 1;

    if (num <= 0)
        return FALSE;
    flag = G_POPUP_WAIT;
    if (num > TABSIZE) {
#ifdef _WIN32
        flag |= G_POPUP_TAB; 
        use_tabs = TRUE;
#else
        flag |= G_POPUP_SCROLL;
#endif
    }
    maxid = 5*num+4;
    ci = (G_POPUP_CHILDINFO*)malloc(sizeof(G_POPUP_CHILDINFO)*maxid);
    assert(ci);
    checks = (int*)malloc(sizeof(int) * (num+1));
    assert(checks);
    
    cont_id = g_popup_container_open(mblk->info->win_id, "Assign Transitions",
                                      flag);

    id=-1;

    for (i=0;i<num;i++) {

        if (use_tabs && ((i+TABSIZE) % TABSIZE)==0) {
            label = psnd_sprintf_temp("P %d", tab_no++);
            id++;
            g_popup_init_info(&(ci[id]));
            ci[id].type = G_CHILD_TAB;
            ci[id].label = label;
            g_popup_add_child(cont_id, &(ci[id]));
        }

        label = psnd_sprintf_temp("Transition %d, Intensity %.3f", 
                                    trnos[i],intens[i]);

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type       = G_CHILD_PANEL;
        ci[id].item       = TRUE;
        ci[id].frame      = TRUE;
        ci[id].title      = label;
        ci[id].horizontal = TRUE;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type       = G_CHILD_CHECKBOX;
        ci[id].id         = OFFSET_ASSIGN+i;
        ci[id].item_count = 1;
        ci[id].select     = &(assig[i]);
        ci[id].data       = checklabel;
        g_popup_add_child(cont_id, &(ci[id]));
        checks[numchecks+1] = ci[id].id;
        numchecks++;

        label = psnd_sprintf_temp("%.3f", freqs[i]);
        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type          = G_CHILD_TEXTBOX;
        ci[id].id            = OFFSET_FREQ+i;
        ci[id].item_count    = 40;
        ci[id].items_visible = 12;
        ci[id].label         = label;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_PANEL;
        ci[id].item  = FALSE;
        g_popup_add_child(cont_id, &(ci[id]));
    }
    checks[0] = numchecks;
    if (numchecks > 1) {
        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_PUSHBUTTON;
        ci[id].id    = ASSIGN_ALL;
        ci[id].label = "Assign all to first";
        ci[id].func  = transition_callback;
        ci[id].userdata      = (void*) checks;
        g_popup_add_child(cont_id, &(ci[id]));

        id++;
        g_popup_init_info(&(ci[id]));
        ci[id].type  = G_CHILD_PUSHBUTTON;
        ci[id].id    = ASSIGN_NONE;
        ci[id].label = "Deselect All";
        ci[id].func  = transition_callback;
        ci[id].userdata      = (void*) checks;
        g_popup_add_child(cont_id, &(ci[id]));
    }

    assert(id<maxid);
    if (g_popup_container_show(cont_id)) {
        for (i=0;i<num;i++) {
            for (j=0;j<=id;j++) {
                if (ci[j].id == OFFSET_ASSIGN+i)
                    assig[i] = ci[j].select[0];
                else if (ci[j].id == OFFSET_FREQ+i)
                    freqs[i] = psnd_scan_float(ci[j].label);
            } 
        }
        ret = TRUE;
    }
    free(ci);
    free(checks);
    return ret;
}

void psnd_assign_transition(MBLOCK *mblk, float pickf, float left, float right)
{
    int *lines, i, j, num, highest;
    float fmin, fmax, pickw;
    float *freqs, *freqs_old, *intens;
    int *assig, *assig_old, *trnos;
    SIMULA_INFO *siminf = mblk->siminf;
    SIMTYPE *st = mblk->siminf->st;
    SPINSYSTEM *sps = &(st->sps[st->sps_pos]);
    POPUP_INFO *popinf = mblk->popinf + POP_SIMULA;

    if (!popinf->visible) 
        return;
    
    if (!siminf->st->has_data)
        return;
    if (fabs(right - siminf->st->fright) > 1.0 ||
        fabs(left - siminf->st->fleft) > 1.0)
            psnd_printf(mblk,"Warning: Calibration of spectrum and simulation is different\n");
            /*
printf("%g %g %g, %g %g\n",pickf, left, right, siminf->st->fleft,siminf->st->fright);
*/
    lines   = ivector(1,sps->lct);
    pickw   = 0.6;
    highest = FALSE;
    /*
     *  find transitions between pickf-pickw and pickf+pickw
     *  index return in lines(num)
     */
    fmin   = pickf - pickw;
    fmax   = pickf + pickw;
    for (i=1,num=0;i<=sps->lct;i++) {
        if (sps->trans[i].deig < fmax && sps->trans[i].deig > fmin) {
            num++;
            lines[num] = i;
        }
    }
    if (num > 0) {
        float afreq;
        int istart, istop,ok;
        int max_int = sps->trans[lines[1]].sumsq;
        int imax = 1;
        /*
         *   find highest intensity in pick range
         */
        for (i=1;i<=num;i++) {
            if (sps->trans[lines[i]].sumsq > max_int) {
                imax = i;
                max_int = sps->trans[lines[i]].sumsq;
            }
        }
        afreq = pickf;
        /*
         * assign picked transitions to a freq.
         */
        if (highest) {
            istart = imax;
            istop  = imax;
        }
        else {
            istart = 1;
            istop  = num;
        }
        freqs      = fvector(0,num);
        freqs_old  = fvector(0,num);
        assig      = ivector(0,num);
        assig_old  = ivector(0,num);
        trnos      = ivector(0,num);
        intens     = fvector(0,num);
        for (i=istart-1,ok=FALSE;i<istop;i++) {

            trnos[i] = sps->trans[lines[i+1]].not;
            intens[i]= sps->trans[lines[i+1]].sumsq;
            /*
             * check if transition i is already assigned
             */
            freqs[i] = sps->trans[lines[i+1]].deig; /*afreq;*/
            assig[i] = FALSE;
            for (j=1;j<=sps->nl;j++) {
                if (trnos[i] == sps->il[j]) {
                    freqs[i] = sps->f[j];
                    assig[i] = TRUE;
                    ok=TRUE;
                    break;
                }
            }
            freqs_old[i] = freqs[i];
            assig_old[i] = assig[i];
        }
        if (!ok) 
            assig[istart-1] = TRUE;
        if (popup_transition_box(mblk, num, freqs, assig, trnos, intens) != 0) {
            clear_transition_list(mblk);
            for (i=0;i<num;i++) {
                if (assig[i] != assig_old[i] || 
                        (assig[i] && (freqs[i] != freqs_old[i]))) {
                    if (assig[i]) {
                        psnd_printf(mblk,"Assign transition %d to frequency at %.2f hz\n",
                            trnos[i], freqs[i]);
                        ass_line(0, trnos[i], freqs[i], siminf->st);
                    }
                    else {
                        remove_line(0, trnos[i], sps);
                        psnd_printf(mblk,"Remove transition %d with frequency %.2f hz\n",
                            trnos[i], freqs[i]);
                    }
                }
            }
            load_transition_list(mblk);
        }
        free_fvector(freqs,0);
        free_fvector(freqs_old,0);
        free_ivector(assig,0);
        free_ivector(assig_old,0);
        free_ivector(trnos,0);
        free_fvector(intens,0);
    }
    free_ivector(lines,1);
}


static void makespc(SIMTYPE *st, float *xint, PBLOCK *par, DBLOCK *dat)
{
    float *profile, gfact, peak_range,hzpt,y,dt;
    int i, k, size, max_pos;
    SPINSYSTEM *sps;

    memset(xint, 0, dat->isize * sizeof(float));
    if (!st->has_data)
        return;
    gfact  = 1.0;

    if (st->width <= 0) 
        st->width = 0.4;
    peak_range = 15.0 * st->width;

    hzpt = dat->sw/dat->isize;

    dt = (2.0/st->width) * hzpt;
    max_pos = round(peak_range/hzpt);
    if (max_pos > dat->isize) 
        max_pos = dat->isize;
    profile = fvector(1,1 + max_pos * 2);
    profile[max_pos+1] = 1.0;
    y = 0.0;
    /*
     * generation of standard lorentzian curve
     */
    if (st->igauss == LORENTZIAN) {
        for (i=1;i<=max_pos;i++) {
            y += dt;
            profile[max_pos-i+1] = 1.0/(1.0 + y*y);
            profile[max_pos+i+1] = profile[max_pos-i+1];
        }
    }
    /*
     * generation of standard gaussian curve if igauss=1
     */
    else if (st->igauss == GAUSSIAN) {
        for (i=1;i<=max_pos;i++) {
            y += dt;
            profile[max_pos-i+1] = exp(-y*y/(1.443*gfact));
            profile[max_pos+i+1] = profile[max_pos-i+1];
        }
    }
    else if (st->igauss == NEGGAUSSIAN) {
        for (i=1;i<=max_pos;i++) {
            y += dt;
            /* 
             * ----even proberen ook negatieve voeten te krijgen 
             */
            profile[max_pos-i+1] = 2*exp(-y*y/(1.443*gfact)) - exp(-y*y/2.886);
            profile[max_pos+i+1] = profile[max_pos-i+1];
        }
    }
    else if (st->igauss == LINES) {
        max_pos = 0;
        profile[max_pos+1] = 1.0;
    }

    for (k=0;k<st->sps_num;k++) {
        float scale;
        int nspins;
        
        sps = &(st->sps[k]);
        if (!sps->active)
            continue;
        nspins = 0;
        for (i=1;i<=sps->ng;i++) 
            nspins += sps->spin[i].nng;
        if (nspins == 0)
            continue;
        scale = sps->scale * 1.0/pow(2,nspins) * 1e6;

        /*
         * generation of spectral curve
         */
        for (i=1;i<=sps->lct;i++) {
            int j,ixx,ill,iul,igll;
    
            ixx = (int) psnd_hz2chan(sps->trans[i].deig, dat->sw,
                                      dat->isize, par->xref,
                                      par->aref, par->bref, 
                                      dat->irc);
            ill = ixx - max_pos;
            iul = ixx + max_pos;
            if (iul < 1)
                continue;
            if (ill > dat->isize)
                break;
            igll = 0;
            if (ill <= 0) {
                igll = 1 - ill;
                ill = 1;
            }
            if (iul > st->size) 
                iul = st->size;
            for (j=ill;j<=iul;j++) {
               igll++;
               xint[j-1] += profile[igll] * sps->trans[i].sumsq
                               * scale; 
            }
        }
    }
    free_fvector(profile,1);
}

/*
 * display simulated spectrum
 */
static void simula_plot(MBLOCK *mblk)
{
    G_EVENT ui;

    if (!mblk->siminf->st->has_data)
        return;
    ui.event   = G_COMMAND;
    ui.win_id  = mblk->info->win_id;
    ui.keycode = PSND_PL;
    g_send_event(&ui);

}



static void simula_process_parameters(MBLOCK *mblk)
{
    SIMULA_INFO *siminf = mblk->siminf;
    int isize = (int) siminf->st->size;

    if (isize > mblk->info->block_size)
        psnd_realloc_buffers(mblk, isize);
    siminf->dat->isize  = isize;
    siminf->par->nsiz   = siminf->dat->isize;
    siminf->par->swhold = siminf->st->sw;
    siminf->dat->sw     = siminf->par->swhold;
    siminf->par->sfd    = siminf->st->sf;
    siminf->par->xref   = siminf->st->xref;
    siminf->par->aref   = siminf->st->aref;
    siminf->par->bref   = siminf->st->bref;
    siminf->par->td     = siminf->st->td;

    siminf->par->irstrt = 1;
    siminf->par->irstop = siminf->dat->isize;
    siminf->st->fright  = psnd_chan2hz(siminf->dat->isize, 
                                      siminf->dat->sw,
                                      siminf->dat->isize, 
                                      siminf->par->xref,
                                      siminf->par->aref, 
                                      siminf->par->bref, 
                                      siminf->dat->irc);
    siminf->st->fleft   = psnd_chan2hz(1, 
                                      siminf->dat->sw, 
                                      siminf->dat->isize, 
                                      siminf->par->xref,
                                      siminf->par->aref, 
                                      siminf->par->bref, 
                                      siminf->dat->irc);

    laocoon5(mblk);
}

static void simula_setup(MBLOCK *mblk)
{
    int cont_id;
    int i,id;
    char *label;
    float sf_store;
    G_POPUP_CHILDINFO ci[20];
    SIMTYPE *st         = mblk->siminf->st;

    cont_id = g_popup_container_open(mblk->info->win_id, "Setup Simulation Params",
                                     G_POPUP_WAIT);
    sf_store = st->sf;
    id=0;
    label = psnd_sprintf_temp("%.4f", st->sf);
    popup_add_text2(cont_id, &(ci[id]), id, label, "Spectrometer frequency");
    id++;
    label = psnd_sprintf_temp("%.0f", st->size);
    popup_add_text2(cont_id, &(ci[id]), id, label, "Number of points");
    id++;
    label = psnd_sprintf_temp("%.2f", st->width);
    popup_add_text2(cont_id, &(ci[id]), id, label, "Line Width");
    id++;
    label = psnd_sprintf_temp("%.2f", st->sw);
    popup_add_text2(cont_id, &(ci[id]), id, label, "SW");
    id++;
    label = psnd_sprintf_temp("%.2f", st->xref);
    popup_add_text2(cont_id, &(ci[id]), id, label, "Reference");
    id++;
    label = psnd_sprintf_temp("%.2f", st->aref);
    popup_add_text2(cont_id, &(ci[id]), id, label, "A Ref");
    id++;
    label = psnd_sprintf_temp("%.2f", st->bref);
    popup_add_text2(cont_id, &(ci[id]), id, label, "B Ref");
    id++;
    label = psnd_sprintf_temp("%d", st->td);
    popup_add_text2(cont_id, &(ci[id]), id, label, "TD");
    id++;
    label = psnd_sprintf_temp("%.3f", st->amin);
    popup_add_text2(cont_id, &(ci[id]), id, label, "AMIN");
    {
        char *labels[] = { "Lorentzian", "Gaussian", "Neg. Gaussian",
                           "Lines", " " };
        id++;
        popup_add_option2(cont_id, &(ci[id]), id, "Line Shape",
                         st->igauss , 4, labels);
    }

    if (g_popup_container_show(cont_id)) {
        float ftmp;
        id=0;
        ftmp       = psnd_scan_float(ci[id].label);id++;
        if (ftmp > 0)
            st->sf     = ftmp;
        ftmp       = psnd_scan_float(ci[id].label);id++;
        if (ftmp > 0)
            st->size   = ftmp;
        ftmp       = psnd_scan_float(ci[id].label);id++;
        if (ftmp > 0)
            st->width  = ftmp;
        ftmp       = psnd_scan_float(ci[id].label);id++;
        if (ftmp > 0)
            st->sw     = ftmp;
        st->xref   = psnd_scan_float(ci[id].label);id++;
        st->aref   = psnd_scan_float(ci[id].label);id++;
        st->bref   = psnd_scan_float(ci[id].label);id++;
        st->td     = psnd_scan_integer(ci[id].label);id++;
        st->amin   = psnd_scan_float(ci[id].label);id++;
        st->igauss = ci[id++].item;
        /*
         * chemical shifts are stored in Hz.
         * If sf changes, recalc new values in Hz
         */
        if (st->ppm && sf_store != st->sf) {
            int j;
            for (j=0;j<st->sps_num;j++) {
                SPINSYSTEM *sps = &(st->sps[j]);
                for (i=0; i <= sps->ng;i++)
                    sps->spin[i].w *= st->sf / sf_store;
            }
        }
        simula_process_parameters(mblk);
    }
}

#ifdef USE_THREADS

static void abortcallback(G_POPUP_CHILDINFO *ci)
{
    iteration_abort_by_user = TRUE;
}

static const int success = 1;
static void *run_cancelbox_in_thread(void *msg)
{
    int  *cancel_cont_id;
    cancel_cont_id = (int*) msg;
    if (g_popup_container_show(*cancel_cont_id)) {
        iteration_abort_by_user = TRUE;
    }
    return ((void*) &success);
}

int cancelbox_in_thread(MBLOCK *mblk)
{
    G_POPUP_CHILDINFO ci[20];
    int id=0;

    int cancel_cont_id = g_popup_container_open(mblk->info->win_id, "Cancel box", 
                            G_POPUP_WAIT | G_POPUP_SINGLEBUTTON );

    g_popup_set_buttonlabel(cancel_cont_id, "Cancel", G_POPUP_BUTTON_OK);
    g_popup_init_info(&(ci[id]));
    ci[id].type  = G_CHILD_LABEL;
    ci[id].id    = 0;
    ci[id].label = "Press \'Cancel\' to abort current iteration";
    g_popup_add_child(cancel_cont_id, &(ci[id]));
    id++;
    g_popup_init_info(&(ci[id]));
    ci[id].type  = G_CHILD_OK;
    ci[id].func  = abortcallback; 
    ci[id].data  = (void*) mblk; 
    g_popup_add_child(cancel_cont_id, &(ci[id]));
    g_peek_event();
    g_peek_event();
    iteration_abort_by_user = FALSE;
    return cancel_cont_id;
}


static int simula_iterate_simplex(MBLOCK *mblk, SIMTYPE *st)
{
    static int cont_id;
    int i,id;
    char *label;
    G_POPUP_CHILDINFO ci[20];
    int cancel_cont_id=0;
    pthread_t thr;

    cont_id = g_popup_container_open(mblk->info->win_id, "Setup Iteration Params",
                                     G_POPUP_WAIT);

    id=0;
    label = psnd_sprintf_temp("%d", st->nfc);
    popup_add_text2(cont_id, &(ci[id]), id, label, "Number of Function Calls");
    {
        char *labels[] = { "No", "Lowest", "All", " " };
        id++;
        popup_add_option2(cont_id, &(ci[id]), id, "Trace",
                         st->show_iterations , 3, labels);
    }
    {
        char *labels[] = { "No", "Yes", " " };
        id++;
        popup_add_option2(cont_id, &(ci[id]), id, "Re-assign transitions",
                         st->sticky , 2, labels);
    }
    if (g_popup_container_show(cont_id)) {
        id=0;
        st->nfc = psnd_scan_integer(ci[id].label);id++;
        st->show_iterations = ci[id++].item;
        st->sticky = ci[id++].item;
        if (st->nfc > 0) {
            psnd_push_waitcursor(mblk);
            g_peek_event();
            g_peek_event();
            cancel_cont_id = cancelbox_in_thread(mblk);
            pthread_create(&thr, NULL, run_cancelbox_in_thread, 
                   (void*)&cancel_cont_id);
            simplex_iterate(mblk);
            g_popup_container_close(cancel_cont_id);
            while (g_peek_event());
            psnd_pop_waitcursor(mblk);
            if (iteration_abort_by_user)
                psnd_printf(mblk,"\n\nAborted by user\n\n");
            return TRUE;
        }
    }
    return FALSE;
}

#else

static int simula_iterate_simplex(MBLOCK *mblk, SIMTYPE *st)
{
    static int cont_id;
    int i,id;
    char *label;
    G_POPUP_CHILDINFO ci[20];

    cont_id = g_popup_container_open(mblk->info->win_id, "Setup Iteration Params",
                                     G_POPUP_WAIT);

    id=0;
    label = psnd_sprintf_temp("%d", st->nfc);
    popup_add_text2(cont_id, &(ci[id]), id, label, "Number of Function Calls");
    {
        char *labels[] = { "No", "Lowest", "All", " " };
        id++;
        popup_add_option2(cont_id, &(ci[id]), id, "Trace",
                         st->show_iterations , 3, labels);
    }
    {
        char *labels[] = { "No", "Yes", " " };
        id++;
        popup_add_option2(cont_id, &(ci[id]), id, "Re-assign transitions",
                         st->sticky , 2, labels);
    }
    if (g_popup_container_show(cont_id)) {
        id=0;
        st->nfc = psnd_scan_integer(ci[id].label);id++;
        st->show_iterations = ci[id].item;id++;
        st->sticky = ci[id].item;id++;
        if (st->nfc > 0) {
            int result;
            psnd_push_waitcursor(mblk);
            result = simplex_iterate(mblk);
            psnd_pop_waitcursor(mblk);
            return result;
        }
    }
    return FALSE;
}


#endif


static int simula_iterate_lsq(MBLOCK *mblk, SIMTYPE *st)
{
    static int cont_id;
    int i,id;
    char *label;
    G_POPUP_CHILDINFO ci[20];

    cont_id = g_popup_container_open(mblk->info->win_id, "Setup Iteration Params",
                                     G_POPUP_WAIT);

    id=0;
    label = psnd_sprintf_temp("%d", st->ni);
    popup_add_text2(cont_id, &(ci[id]), id, label, "Number of iterations");
    {
        char *labels[] = { "No", "Lowest", "All", " " };
        id++;
        popup_add_option2(cont_id, &(ci[id]), id, "Trace",
                         st->show_iterations , 3, labels);
    }
    {
        char *labels[] = { "No", "Yes", " " };
        id++;
        popup_add_option2(cont_id, &(ci[id]), id, "Re-assign transitions",
                         st->sticky , 2, labels);
    }
    if (g_popup_container_show(cont_id)) {
        id=0;
        st->ni = psnd_scan_integer(ci[id].label);id++;
        st->show_iterations = ci[id++].item;
        st->sticky = ci[id++].item;
        if (st->ni > 0) {
            int result;
            psnd_push_waitcursor(mblk);
            result = laocoon5_iterate(mblk);
            psnd_pop_waitcursor(mblk);
            return result;
        }
    }
    return FALSE;
}



void simula_readpar(MBLOCK *mblk)
{
    char *filename = psnd_getfilename(mblk,"Read parameter file","*.sim");
    if (filename) 
        if (siminput(mblk, filename)) {
            simula_process_parameters(mblk);
        }
}

static void simula_writepar(MBLOCK *mblk)
{
    char *filename = psnd_savefilename(mblk,"Write parameter file","*.sim");
    if (filename) 
        simoutput(mblk->siminf->st, filename);
}

static void simula_set_spinsystem(MBLOCK *mblk, SIMTYPE *st, int pos)
{
    SPINSYSTEM *sps;
    sps = &(st->sps[st->sps_pos]);
    pos = min(pos, st->sps_num-1);
    pos = max(pos, 0);
    if (st->sps_pos != pos) {
        sps->has_data = st->has_data;
        clear_transition_list(mblk);
        clear_spins_and_couplings(mblk);
        st->sps_pos = pos;
        st->sps[pos].active = TRUE;
        simula_init_undo(st->maxspin, st);
        sps = &(st->sps[st->sps_pos]);
        st->has_data = sps->has_data;
        if (st->has_data)
            laocoon5(mblk);
        load_transition_list(mblk);
        load_spins_and_couplings(mblk);
    }
}

/*
 * Activate the plotting of spin systems
 */
static void popup_active_spinsystems(MBLOCK *mblk)
{
    SIMTYPE *st     = mblk->siminf->st;
    char *list[] = {
        "1","2","3","4","5","6","7","8","9","10",
        "11","12","13","14","15","16","17","18","19","20",
        "21","22","23","24","25","26","27","28","29","30",
        "31","32","33","34","35","36","37","38","39","40",
        "41","42","43","44","45","46","47","48","49","50"
    };
    int select[MAXSPINSYSTEM];
    int i, result;

    assert(MAXSPINSYSTEM<=50);
    
    for (i=0;i<MAXSPINSYSTEM;i++)
        select[i] = st->sps[i].active;
        
    result = g_popup_checkbox(mblk->info->win_id, "Visisble", 
         "Spin systems", st->sps_num, list, select);

    if (result)
        for (i=0;i < st->sps_num;i++)
            st->sps[i].active = select[i];
                
}

/*
 * Scale the plotting of spin systems
 */
static int popup_scale_spinsystems(MBLOCK *mblk)
{
    static int cont_id;
    int i,id;
    char *label, *labelnum;
    G_POPUP_CHILDINFO ci[2*MAXSPINSYSTEM];
    SIMTYPE *st     = mblk->siminf->st;
    
    cont_id = g_popup_container_open(mblk->info->win_id, "Scale Spin Systems",
                                     G_POPUP_WAIT);

    id=0;

    for (i=0;i<st->sps_num;i++) {
        label    = psnd_sprintf_temp("%1.3f", st->sps[i].scale);
        labelnum = psnd_sprintf_temp("Spin System %d", i+1);
        popup_add_text2(cont_id, &(ci[id]), id, label, labelnum);
        id++;
    }

    if (g_popup_container_show(cont_id)) {
        id=0;
        for (i=0;i<st->sps_num;i++) {
            st->sps[i].scale = psnd_scan_float(ci[id].label);
            id++;
        }
        return TRUE;
    }
    return FALSE;
}

typedef enum {
    SIM_SYSTEM_ID,
    SIM_GROUPS_ID,
    SIM_PARAMS_ID,
    SIM_SPINS_ID,
    SIM_TRANS_ID,
    SIM_ITPAR_ID,
    SIM_SHOW_ITPAR_ID,
    SIM_SHOW_TRANS_ID,
    SIM_SHOW_ALLTRANS_ID,
    SIM_SHOW_DEPS_ID,
    SIM_SHOW_ALLDEPS_ID,
    SIM_READ_ID,
    SIM_WRITE_ID,
    SIM_GENETIC_ID,
    SIM_SIMPLEX_ID,
    SIM_LSQ_ID,
    SIM_MOUSE_ID,
    SIM_SYSTEM_NUM_ID,
    SIM_SYSTEM_POS_ID,
    SIM_SYSTEM_ACTIVE_ID,
    SIM_SYSTEM_SCALE_ID,
    SIM_PLOT_ID,
    SIM_UNDO_ID,
    SIM_REDO_ID,
    SIM_UNIT_ID,
    SIM_DUMMY_ID,
    SIM_MAX_ID
} popup_sim_ids;



static void simula_callback(G_POPUP_CHILDINFO *ci)
{
    MBLOCK *mblk        = (MBLOCK *) ci->userdata;
    SIMULA_INFO *siminf = mblk->siminf;
    DBLOCK *dat         = siminf->dat;
    SBLOCK *spar        = siminf->spar;
    PBLOCK *par         = siminf->par;
    SIMTYPE *st         = siminf->st;
    POPUP_INFO *popinf  = mblk->popinf + POP_SIMULA;
    SPINSYSTEM *sps = &(st->sps[st->sps_pos]);
    
    switch (ci->type) {
        case G_CHILD_PUSHBUTTON:
            switch (ci->id) {
                case SIM_SYSTEM_NUM_ID:
                    {
                        int i,num = st->sps_num;
                        if (popup_spin(mblk->info->win_id, "Set number of Spin Systems",
                                     &num, "Number of Spin Systems", 1, MAXSPINSYSTEM, 1)) {
                            st->sps_num = num;
                            allocate_all(mblk);
                            for (i=num;i<MAXSPINSYSTEM;i++)
                                st->sps[i].active = FALSE;
                            simula_set_spinsystem(mblk, st, st->sps_pos);
                            popup_active_spinsystems(mblk);
                        }
                    }
                    break;
                case SIM_SYSTEM_POS_ID:
                    {
                        int pos = st->sps_pos+1;
                        if (popup_spin(mblk->info->win_id, "Set active Spin Systems",
                                     &pos, "Select Spin Systems", 1, st->sps_num, 1)) {
                            pos--;
                            simula_set_spinsystem(mblk, st, pos);
                        }
                    }
                    break;
                case SIM_SYSTEM_ACTIVE_ID:
                    {
                        popup_active_spinsystems(mblk);
                    }
                    break;
                case SIM_SYSTEM_SCALE_ID:
                    {
                        popup_scale_spinsystems(mblk);
                    }
                    break;
                case SIM_GROUPS_ID:
                    {
                        int ng = sps->ng;
                        if (popup_spin(mblk->info->win_id, "Set number of Spin Groups",
                                     &ng, "Number of Spin Groups", 2, MAXSPINLIMIT, 1)) {
                            if (sps->ng != ng) {
                                simula_init_undo(st->maxspin, st);
                                clear_transition_list(mblk);
                                clear_spins_and_couplings(mblk);
                                sps->ng = ng;
                                if (sps->ng > st->maxspin) 
                                    resize_maxspin(st);
                                if (st->has_data)
                                    laocoon5(mblk);
                                load_transition_list(mblk);
                                load_spins_and_couplings(mblk);
                            }
                        }
                    }
                    break;
                case SIM_PARAMS_ID:
                    simula_setup(mblk);
                    break;
                case SIM_SPINS_ID:
                    popup_shifts_and_couplings(mblk);
                    break;
                case SIM_TRANS_ID:
                    popup_assign(mblk);
                    break;
                case SIM_ITPAR_ID:
                    popup_itpar(mblk);
                    break;
                case SIM_SHOW_ITPAR_ID:
                    itpar(mblk);
                    break;
                case SIM_SHOW_TRANS_ID:
                    spectr(mblk, SHOW_ITERATED);
                    break;
                case SIM_SHOW_ALLTRANS_ID:
                    spectr(mblk, SHOW_ALL);
                    break;
                case SIM_SHOW_DEPS_ID:
                    spectr(mblk, SHOW_NONE);
                    pardep(mblk, TRUE);
                    break;
                case SIM_SHOW_ALLDEPS_ID:
                    spectr(mblk, SHOW_NONE);
                    pardep(mblk, FALSE);
                    break;
                case SIM_READ_ID:
                    simula_init_undo(st->maxspin, st);
                    simula_readpar(mblk);
                    simula_push_undo(sps->spin, st->maxspin, st);
                    g_popup_set_selection(ci->cont_id, SIM_UNIT_ID, st->ppm);
                    break;
                case SIM_WRITE_ID:
                    simula_writepar(mblk);
                    break;

                case SIM_SIMPLEX_ID:
                    if (simula_iterate_simplex(mblk,st)) {
                        clear_spins_and_couplings(mblk);
                        clear_transition_list(mblk);
                        simula_push_undo(sps->spin, st->maxspin, st);
                        if (st->has_data) {
                            laocoon5(mblk);
                            makespc(st, dat->xreal, par, dat);
                            simula_plot(mblk);
                        }
                        load_spins_and_couplings(mblk);
                        load_transition_list(mblk);
                    }
                    break;
                case SIM_LSQ_ID:
                    if (simula_iterate_lsq(mblk, st)) {
                        clear_spins_and_couplings(mblk);
                        clear_transition_list(mblk);
                        simula_push_undo(sps->spin, st->maxspin, st);
                        if (st->has_data) {
                            laocoon5(mblk);
                            makespc(st, dat->xreal, par, dat);
                            simula_plot(mblk);
                        }
                        load_spins_and_couplings(mblk);
                        load_transition_list(mblk);
                    }
                    break;
                case SIM_UNDO_ID:
                    if (popspins(PSND_UNDO, st)) {
                        clear_spins_and_couplings(mblk);
                        clear_transition_list(mblk);
                        if (st->has_data) {
                            laocoon5(mblk);
                            makespc(st, dat->xreal, par, dat);
                            simula_plot(mblk);
                        }
                        load_spins_and_couplings(mblk);
                        load_transition_list(mblk);
                    }
                    break;
                case SIM_REDO_ID:
                    if (popspins(PSND_REDO, st)) {
                        clear_spins_and_couplings(mblk);
                        clear_transition_list(mblk);
                        if (st->has_data) {
                            laocoon5(mblk);
                            makespc(st, dat->xreal, par, dat);
                            simula_plot(mblk);
                        }
                        load_spins_and_couplings(mblk);
                        load_transition_list(mblk);
                    }
                    break;
                case SIM_PLOT_ID:
                    makespc(st, dat->xreal, par, dat);
                    simula_plot(mblk);
                    break;
                case SIM_MOUSE_ID:
                    psnd_set_cursormode(mblk, 0, MOUSE_TRANSITION);
                    break;
            }
            break;
        case G_CHILD_OPTIONMENU:
            switch (ci->id) {
                case SIM_UNIT_ID:
                    st->ppm = ci->item;
                    break;
            }
            break;
        case G_CHILD_OK:
        case G_CHILD_CANCEL:
            if (g_popup_messagebox2(mblk->info->win_id, "Hide Popup", 
                                    "Exit simulation and free resources ?", 
                                    "Yes", "No")==G_POPUP_BUTTON1) {

                POPUP_INFO *popinf  = mblk->popinf + POP_SIMEDIT;
                g_popup_container_close(popinf->cont_id);
                popinf = mblk->popinf + POP_SIMASSIGN;
                g_popup_container_close(popinf->cont_id);
                simula_init_undo(st->maxspin, st);
                clear_spins_and_couplings(mblk);
                clear_transition_list(mblk);
                free_all(st);
                popinf = mblk->popinf + POP_SIMULA;
                popinf->visible = FALSE;
            }
            g_menu_enable_item(mblk->info->mousebar_id, ID_MOUSE_SELECT, FALSE);
            if (mblk->info->mouse_mode1 == MOUSE_TRANSITION)
                psnd_set_cursormode(mblk, 0, 0);   
            break;
    }
}

#define ALLSIM (SIM_MAX_ID+40)
void psnd_popup_simula(MBLOCK *mblk, SBLOCK *spar, PBLOCK *par, DBLOCK *dat)
{
    int cont_id;
    static G_POPUP_CHILDINFO ci[ALLSIM];
    SIMTYPE *st = mblk->siminf->st;
    POPUP_INFO *popinf = mblk->popinf + POP_SIMULA;

    if (!popinf->visible) {
        int j;
        mblk->siminf->dat  = dat;
        mblk->siminf->spar = spar;
        mblk->siminf->par  = par;

        lcn5_def(st);
        allocate_all(mblk);
        simula_init_undo(st->maxspin, st);

        if (par->sfd > 1 && dat->isize > 1) {
            st->sw    = dat->sw;
            st->sf    = par->sfd;
            st->xref  = par->xref;
            st->aref  = par->aref;
            st->bref  = par->bref;
            st->td    = par->td;
            st->size  = dat->isize;
        }
        else {
            par->swhold = st->sw;
            par->sfd    = st->sf;
            par->xref   = st->xref;
            par->aref   = st->aref;
            par->bref   = st->bref;
            par->td     = st->td;
            dat->isize  = st->size;
            par->nsiz   = st->size;
            dat->sw     = st->sw;
        }	
        dat->isspec = TRUE;
        dat->iaqdir = 1;
        dat->ityp   = 1;
        dat->ndim   = 1;
        par->isspec = TRUE;
        par->irstrt = 1;
        par->irstop = dat->isize;
        par->isstrt = 0;
        par->isstop = 0;
        for (j=0;j<MAX_DIM;j++) {
            dat->pars[j]   = dat->par0[j];
            dat->nsizo[j]  = 1;
            dat->cmplxo[j] = 0;
        }
        st->fright = psnd_chan2hz(dat->isize, 
                                  dat->sw,
                                  dat->isize, 
                                  par->xref,
                                  par->aref, 
                                  par->bref, 
                                  dat->irc);
        st->fleft  = psnd_chan2hz(1, 
                                  dat->sw, 
                                  dat->isize, 
                                  par->xref,
                                  par->aref, 
                                  par->bref, 
                                  dat->irc);
        spar[S_AXISX_MARK].show = AXIS_UNITS_HERTZ;

        if (! popinf->cont_id) {
            int id, cont_id;
            char *labels[] = { "Herz", "PPM" };
        
            popinf->cont_id = cont_id = 
                g_popup_container_open(mblk->info->win_id, "Simulation",
                                    G_POPUP_KEEP|G_POPUP_SINGLEBUTTON);
            id=-1;
            id++;
            g_popup_init_info(&(ci[id]));
            ci[id].type  = G_CHILD_PUSHBUTTON;
            ci[id].id    = SIM_PARAMS_ID;
            ci[id].label = "General Parameters";
            ci[id].func  = simula_callback;
            ci[id].userdata      = (void*) mblk;
            g_popup_add_child(cont_id, &(ci[id]));

            id++;
            g_popup_init_info(&(ci[id]));
            ci[id].type  = G_CHILD_OPTIONMENU;
            ci[id].id    = SIM_UNIT_ID;
            ci[id].item_count = 2;
            ci[id].item  = st->ppm;
            ci[id].data  = labels;
            ci[id].label = "Units";
            ci[id].horizontal = TRUE;
            ci[id].func  = simula_callback;
            ci[id].userdata      = (void*) mblk;
            g_popup_add_child(cont_id, &(ci[id]));

            id++;
            g_popup_init_info(&(ci[id]));
            ci[id].type       = G_CHILD_PANEL;
            ci[id].item       = TRUE;
            ci[id].frame      = TRUE;
            ci[id].title      = "Spin Systems";
            ci[id].horizontal = FALSE;
            g_popup_add_child(cont_id, &(ci[id]));

            id++;
            g_popup_init_info(&(ci[id]));
            ci[id].type       = G_CHILD_PANEL;
            ci[id].item       = TRUE;
            ci[id].horizontal = TRUE;
            g_popup_add_child(cont_id, &(ci[id]));

            id++;
            g_popup_init_info(&(ci[id]));
            ci[id].type  = G_CHILD_PUSHBUTTON;
            ci[id].id    = SIM_SYSTEM_NUM_ID;
            ci[id].label = "Number of Spin Systems";
            ci[id].func  = simula_callback;
            ci[id].userdata      = (void*) mblk;
            g_popup_add_child(cont_id, &(ci[id]));

            id++;
            g_popup_init_info(&(ci[id]));
            ci[id].type       = G_CHILD_PANEL;
            ci[id].item       = FALSE;
            g_popup_add_child(cont_id, &(ci[id]));

            id++;
            g_popup_init_info(&(ci[id]));
            ci[id].type       = G_CHILD_PANEL;
            ci[id].item       = TRUE;
            ci[id].horizontal = TRUE;
            g_popup_add_child(cont_id, &(ci[id]));

            id++;
            g_popup_init_info(&(ci[id]));
            ci[id].type  = G_CHILD_PUSHBUTTON;
            ci[id].id    = SIM_SYSTEM_POS_ID;
            ci[id].label = "Select";
            ci[id].func  = simula_callback;
            ci[id].userdata      = (void*) mblk;
            g_popup_add_child(cont_id, &(ci[id]));

            id++;
            g_popup_init_info(&(ci[id]));
            ci[id].type  = G_CHILD_PUSHBUTTON;
            ci[id].id    = SIM_SYSTEM_ACTIVE_ID;
            ci[id].label = "Visible";
            ci[id].func  = simula_callback;
            ci[id].userdata      = (void*) mblk;
            g_popup_add_child(cont_id, &(ci[id]));

            id++;
            g_popup_init_info(&(ci[id]));
            ci[id].type  = G_CHILD_PUSHBUTTON;
            ci[id].id    = SIM_SYSTEM_SCALE_ID;
            ci[id].label = "Scale";
            ci[id].func  = simula_callback;
            ci[id].userdata      = (void*) mblk;
            g_popup_add_child(cont_id, &(ci[id]));

            id++;
            g_popup_init_info(&(ci[id]));
            ci[id].type       = G_CHILD_PANEL;
            ci[id].item       = FALSE;
            g_popup_add_child(cont_id, &(ci[id]));

            id++;
            g_popup_init_info(&(ci[id]));
            ci[id].type       = G_CHILD_PANEL;
            ci[id].item       = FALSE;
            g_popup_add_child(cont_id, &(ci[id]));

            id++;
            g_popup_init_info(&(ci[id]));
            ci[id].type       = G_CHILD_PANEL;
            ci[id].item       = TRUE;
            ci[id].frame      = TRUE;
            ci[id].title      = "Set up Spin System";
            ci[id].horizontal = FALSE;
            g_popup_add_child(cont_id, &(ci[id]));

            id++;
            g_popup_init_info(&(ci[id]));
            ci[id].type  = G_CHILD_PUSHBUTTON;
            ci[id].id    = SIM_GROUPS_ID;
            ci[id].label = "Number of Spin Groups";
            ci[id].func  = simula_callback;
            ci[id].userdata      = (void*) mblk;
            g_popup_add_child(cont_id, &(ci[id]));

            id++;
            g_popup_init_info(&(ci[id]));
            ci[id].type  = G_CHILD_PUSHBUTTON;
            ci[id].id    = SIM_SPINS_ID;
            ci[id].label = "Spins and Couplings";
            ci[id].func  = simula_callback;
            ci[id].userdata      = (void*) mblk;
            g_popup_add_child(cont_id, &(ci[id]));

            id++;
            g_popup_init_info(&(ci[id]));
            ci[id].type       = G_CHILD_PANEL;
            ci[id].item       = FALSE;
            g_popup_add_child(cont_id, &(ci[id]));

            id++;
            g_popup_init_info(&(ci[id]));
            ci[id].type       = G_CHILD_PANEL;
            ci[id].item       = TRUE;
            ci[id].frame      = TRUE;
            ci[id].title      = "Set up Iteration";
            ci[id].horizontal = FALSE;
            g_popup_add_child(cont_id, &(ci[id]));

            id++;
            g_popup_init_info(&(ci[id]));
            ci[id].type  = G_CHILD_PUSHBUTTON;
            ci[id].id    = SIM_TRANS_ID;
            ci[id].label = "Transitions";
            ci[id].func  = simula_callback;
            ci[id].userdata      = (void*) mblk;
            g_popup_add_child(cont_id, &(ci[id]));

            id++;
            g_popup_init_info(&(ci[id]));
            ci[id].type  = G_CHILD_PUSHBUTTON;
            ci[id].id    = SIM_ITPAR_ID;
            ci[id].label = "Iteration Parameters";
            ci[id].func  = simula_callback;
            ci[id].userdata      = (void*) mblk;
            g_popup_add_child(cont_id, &(ci[id]));

            id++;
            g_popup_init_info(&(ci[id]));
            ci[id].type  = G_CHILD_PUSHBUTTON;
            ci[id].id    = SIM_MOUSE_ID;
            ci[id].label = "Grab Mouse to Assign";
            ci[id].func  = simula_callback;
            ci[id].userdata      = (void*) mblk;
            g_popup_add_child(cont_id, &(ci[id]));

            id++;
            g_popup_init_info(&(ci[id]));
            ci[id].type       = G_CHILD_PANEL;
            ci[id].item       = FALSE;
            g_popup_add_child(cont_id, &(ci[id]));

            id++;
            g_popup_init_info(&(ci[id]));
            ci[id].type       = G_CHILD_PANEL;
            ci[id].item       = TRUE;
            ci[id].frame      = TRUE;
            ci[id].title      = "Parameter I/O";
            ci[id].horizontal = TRUE;
            g_popup_add_child(cont_id, &(ci[id]));

            id++;
            g_popup_init_info(&(ci[id]));
            ci[id].type  = G_CHILD_PUSHBUTTON;
            ci[id].id    = SIM_READ_ID;
            ci[id].label = "Read sim";
            ci[id].func  = simula_callback;
            ci[id].userdata      = (void*) mblk;
            g_popup_add_child(cont_id, &(ci[id]));

            id++;
            g_popup_init_info(&(ci[id]));
            ci[id].type  = G_CHILD_PUSHBUTTON;
            ci[id].id    = SIM_WRITE_ID;
            ci[id].label = "Write sim";
            ci[id].func  = simula_callback;
            ci[id].userdata      = (void*) mblk;
            g_popup_add_child(cont_id, &(ci[id]));

            id++;
            g_popup_init_info(&(ci[id]));
            ci[id].type       = G_CHILD_PANEL;
            ci[id].item       = FALSE;
            g_popup_add_child(cont_id, &(ci[id]));

            id++;
            g_popup_init_info(&(ci[id]));
            ci[id].type       = G_CHILD_PANEL;
            ci[id].item       = TRUE;
            ci[id].frame      = TRUE;
            ci[id].title      = "Show data";
            ci[id].horizontal = FALSE;
            g_popup_add_child(cont_id, &(ci[id]));

            id++;
            g_popup_init_info(&(ci[id]));
            ci[id].type  = G_CHILD_PUSHBUTTON;
            ci[id].id    = SIM_SHOW_ITPAR_ID;
            ci[id].label = "Iterated Parameters";
            ci[id].func  = simula_callback;
            ci[id].userdata      = (void*) mblk;
            g_popup_add_child(cont_id, &(ci[id]));

            id++;
            g_popup_init_info(&(ci[id]));
            ci[id].type       = G_CHILD_PANEL;
            ci[id].item       = TRUE;
            ci[id].frame      = TRUE;
            ci[id].title      = "Transitions";
            ci[id].horizontal = TRUE;
            g_popup_add_child(cont_id, &(ci[id]));

            id++;
            g_popup_init_info(&(ci[id]));
            ci[id].type  = G_CHILD_PUSHBUTTON;
            ci[id].id    = SIM_SHOW_TRANS_ID;
            ci[id].label = "Iterated";
            ci[id].func  = simula_callback;
            ci[id].userdata      = (void*) mblk;
            g_popup_add_child(cont_id, &(ci[id]));

            id++;
            g_popup_init_info(&(ci[id]));
            ci[id].type  = G_CHILD_PUSHBUTTON;
            ci[id].id    = SIM_SHOW_ALLTRANS_ID;
            ci[id].label = "All";
            ci[id].func  = simula_callback;
            ci[id].userdata      = (void*) mblk;
            g_popup_add_child(cont_id, &(ci[id]));

            id++;
            g_popup_init_info(&(ci[id]));
            ci[id].type       = G_CHILD_PANEL;
            ci[id].item       = FALSE;
            g_popup_add_child(cont_id, &(ci[id]));

            id++;
            g_popup_init_info(&(ci[id]));
            ci[id].type       = G_CHILD_PANEL;
            ci[id].item       = TRUE;
            ci[id].frame      = TRUE;
            ci[id].title      = "Dependencies";
            ci[id].horizontal = TRUE;
            g_popup_add_child(cont_id, &(ci[id]));

            id++;
            g_popup_init_info(&(ci[id]));
            ci[id].type  = G_CHILD_PUSHBUTTON;
            ci[id].id    = SIM_SHOW_DEPS_ID;
            ci[id].label = "Iteration";
            ci[id].func  = simula_callback;
            ci[id].userdata      = (void*) mblk;
            g_popup_add_child(cont_id, &(ci[id]));

            id++;
            g_popup_init_info(&(ci[id]));
            ci[id].type  = G_CHILD_PUSHBUTTON;
            ci[id].id    = SIM_SHOW_ALLDEPS_ID;
            ci[id].label = "All";
            ci[id].func  = simula_callback;
            ci[id].userdata      = (void*) mblk;
            g_popup_add_child(cont_id, &(ci[id]));

            id++;
            g_popup_init_info(&(ci[id]));
            ci[id].type       = G_CHILD_PANEL;
            ci[id].item       = FALSE;
            g_popup_add_child(cont_id, &(ci[id]));

            id++;
            g_popup_init_info(&(ci[id]));
            ci[id].type       = G_CHILD_PANEL;
            ci[id].item       = FALSE;
            g_popup_add_child(cont_id, &(ci[id]));

            id++;
            g_popup_init_info(&(ci[id]));
            ci[id].type       = G_CHILD_PANEL;
            ci[id].item       = TRUE;
            ci[id].frame      = TRUE;
            ci[id].title      = "Iterate";
            ci[id].horizontal = FALSE;
            g_popup_add_child(cont_id, &(ci[id]));

            id++;
            g_popup_init_info(&(ci[id]));
            ci[id].type  = G_CHILD_PUSHBUTTON;
            ci[id].id    = SIM_LSQ_ID;
            ci[id].label = "Least squares fit";
            ci[id].func  = simula_callback;
            ci[id].userdata      = (void*) mblk;
            g_popup_add_child(cont_id, &(ci[id]));

            id++;
            g_popup_init_info(&(ci[id]));
            ci[id].type  = G_CHILD_PUSHBUTTON;
            ci[id].id    = SIM_SIMPLEX_ID;
            ci[id].label = "Simplex routine";
            ci[id].func  = simula_callback;
            ci[id].userdata      = (void*) mblk;
            g_popup_add_child(cont_id, &(ci[id]));

/*
            id++;
            g_popup_init_info(&(ci[id]));
            ci[id].type  = G_CHILD_PUSHBUTTON;
            ci[id].id    = SIM_GENETIC_ID;
            ci[id].label = "Genetic algorithm";
            ci[id].func  = simula_callback;
            ci[id].userdata      = (void*) mblk;
            g_popup_add_child(cont_id, &(ci[id]));
*/

            id++;
            g_popup_init_info(&(ci[id]));
            ci[id].type       = G_CHILD_PANEL;
            ci[id].item       = TRUE;
            ci[id].horizontal = TRUE;
            g_popup_add_child(cont_id, &(ci[id]));

            id++;
            g_popup_init_info(&(ci[id]));
            ci[id].type  = G_CHILD_PUSHBUTTON;
            ci[id].id    = SIM_UNDO_ID;
            ci[id].label = "Undo";
            ci[id].func  = simula_callback;
            ci[id].userdata      = (void*) mblk;
            g_popup_add_child(cont_id, &(ci[id]));

            id++;
            g_popup_init_info(&(ci[id]));
            ci[id].type  = G_CHILD_PUSHBUTTON;
            ci[id].id    = SIM_REDO_ID;
            ci[id].label = "Redo";
            ci[id].func  = simula_callback;
            ci[id].userdata      = (void*) mblk;
            g_popup_add_child(cont_id, &(ci[id]));

            id++;
            g_popup_init_info(&(ci[id]));
            ci[id].type       = G_CHILD_PANEL;
            ci[id].item       = FALSE;
            g_popup_add_child(cont_id, &(ci[id]));

            id++;
            g_popup_init_info(&(ci[id]));
            ci[id].type       = G_CHILD_PANEL;
            ci[id].item       = FALSE;
            g_popup_add_child(cont_id, &(ci[id]));

            id++;
            g_popup_init_info(&(ci[id]));
            ci[id].type  = G_CHILD_PUSHBUTTON;
            ci[id].id    = SIM_PLOT_ID;
            ci[id].label = "Calc/Plot spectrum";
            ci[id].func  = simula_callback;
            ci[id].userdata      = (void*) mblk;
            g_popup_add_child(cont_id, &(ci[id]));

            id++;
            g_popup_init_info(&(ci[id]));
            ci[id].type  = G_CHILD_OK;
            ci[id].func  = simula_callback;
            ci[id].userdata      = (void*) mblk;
            g_popup_add_child(cont_id, &(ci[id]));

            id++;
            g_popup_init_info(&(ci[id]));
            ci[id].type  = G_CHILD_CANCEL;
            ci[id].func  = simula_callback;
            ci[id].userdata      = (void*) mblk;
            g_popup_add_child(cont_id, &(ci[id]));

            assert(id<ALLSIM);
        }
        popinf->visible = TRUE;
    }
    psnd_set_cursormode(mblk, MOUSE_TRANSITION, 0);
    g_menu_enable_item(mblk->info->mousebar_id, ID_MOUSE_SELECT, TRUE);
    g_popup_container_show(popinf->cont_id);   
}








