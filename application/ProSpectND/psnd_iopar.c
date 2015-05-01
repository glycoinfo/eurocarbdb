/*
 * psnd_iopar.c
 *
 * Translate parameters form parameter-record to raw-storage
 * (= packed-parameter-block = how it is written to file)
 * format, and vice versa
 *
 * Old style (packed) parameters have a 10+40 float header, followed
 * by 3 binary parameter blocks (corresponding with 3 dimensions)
 * all of the same size (50 floats). All parameters have a fixed position on 
 * this record. (See OLD3D_STR_RECORD in iond.h)
 *
 * New ND parameters have a 10 integer header, followed by as
 * many parameter blocks as there are dimensions. Records are of 
 * variable size. Parameters can be stored anywhere on the record.
 * Parameters are identified by name, and by data type. This allows
 * for adding new parameters, and to fade-out old one's.
 * Parameter blocks have record of the following structure
 * 
 * unsigned char size		this many bytes in this record, 
 * 					including this one
 * char type			F = float, I = integer
 * char name[10]		parameter name
 * int/float data		parameter value
 *
 * each block ends with at least one record filled with zero's
 *
 *
 * 1998, Albert van Kuik
 */
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "iond.h"
#include "genplot.h"
#include "psnd.h"


#define PARNAMESIZE	10

#ifdef COMMENT
typedef struct OLD3D_PAR_RECORD {
   float nblok; 	/*  0 */	/* Used in pre-history (IOMS)*/
   float nrec; 	        /*  1 */	/* Used in pre-history (IOMS)*/
   int   LT1T2; 	/*  2 */	/* Used in pre-history (IOMS)*/
   float spfreq; 	/*  3 */
   float carrier; 	/*  4 */
   int   nt0par; 	/*  5 */
   int   ntnpar; 	/*  6 */
   float dum7; 		/*  7 */
   int   iaqdir; 	/*  8 */
   int   next_dir; 	/*  9 */
} OLD3D_PAR_RECORD;
#endif

typedef struct pardef_header {
    int size;		/* total size of packed-parameter-block 	*/
    int type;		/* FILE_TYPE_ND or FILE_TYPE_OLD3D		*/
    int dum1;
    int dum2;
    int swapit;
    int nt0par;		/* size of PARDEF_HEADER			*/
    int ntnpar;		/* size of one packed-parameter-block		*/
    int endi;		/* test integer (set to 1) in current endiennes	*/
    int iaqdir;		/* acquisition direction = number of dimensions	*/
    int nextdr;
} PARDEF_HEADER;

typedef struct pardef {
    unsigned char size;
    char          type;
    char          name[PARNAMESIZE];
    union {
        float f;
        int   i;
    } value;
} PARDEF;

typedef struct partype {
    char          type;
    char          name[PARNAMESIZE];
    int offset;
} PARTYPE;

enum paroffsets {
    OFF_SFD,
    OFF_SW,
    OFF_REF,
    OFF_PRE,
    OFF_RST,
    OFF_RSP,
    OFF_WIN,
    OFF_RSH,
    OFF_RLB,
    OFF_GN,
    OFF_ITM,
    OFF_WST,
    OFF_WSP,
    OFF_FFT,
    OFF_NZF,
    OFF_FTSCALE,
    OFF_PHA,
    OFF_AHO,
    OFF_BHO,
    OFF_I0H,
    OFF_PST,
    OFF_REV,
    OFF_BAS,
    OFF_BTERMS,
    OFF_BTERMS2,
    OFF_BST,
    OFF_BSP,
    OFF_FIL,
    OFF_SST,
    OFF_SSP,

    OFF_SPEC,
    OFF_TD,
    OFF_AREF,
    OFF_BREF,
    OFF_DSP,

    OFF_LPTYPE,
    OFF_LPMODE,
    OFF_LPROOT,
    OFF_LPFUT,
    OFF_LPMOVE,
    OFF_LPREP,
    OFF_LPTOL,
    OFF_LPGAP1,
    OFF_LPGAP2,
    OFF_LPSTART,
    OFF_LPSTOP,

    OFF_WWPOW,
    OFF_WWWIDTH,
    OFF_WWSHIFT,

    OFF_END,
    OFF_ENDBLOCK
};

static PARTYPE old123d_par[] = {
    { 'F', "SFD"     , 0  },
    { 'F', "SW"      , 0  },
    { 'F', "REF"     , 0  },
    { 'I', "PRE"     , 0  },
    { 'I', "RST"     , 0  },
    { 'I', "RSP"     , 0  },
    { 'I', "WIN"     , 0  },
    { 'F', "RSH"     , 0  },
    { 'F', "RLB"     , 0  },
    { 'F', "GN"      , 0  },
    { 'I', "ITM"     , 0  },
    { 'I', "WST"     , 0  },
    { 'I', "WSP"     , 0  },
    { 'I', "FFT"     , 0  },
    { 'I', "NZF"     , 0  },
    { 'F', "FTSCALE" , 0  },
    { 'I', "PHA"     , 0  },
    { 'F', "AHO"     , 0  },
    { 'F', "BHO"     , 0  },
    { 'I', "I0H"     , 0  },
    { 'I', "PST"     , 0  },
    { 'I', "REV"     , 0  },
    { 'I', "BAS"     , 0  },
    { 'I', "BTERMS"  , 0  },
    { 'I', "BTERMS2" , 0  },
    { 'I', "BST"     , 0  },
    { 'I', "BSP"     , 0  },
    { 'I', "FIL"     , 0  },
    { 'I', "SST"     , 0  },
    { 'I', "SSP"     , 0  },

    { 'I', "SPEC"    , 0  },
    { 'I', "TD"      , 0  },
    { 'F', "AREF"    , 0  },
    { 'F', "BREF"    , 0  },
/*
    { 'I', "DSP"     , 0  },
Changed to 'F'
*/
    { 'F', "DSP"     , 0  },

    { 'I', "LPTYPE"  , 0  },
    { 'I', "LPMODE"  , 0  },
    { 'I', "LPROOT"  , 0  },
    { 'I', "LPFUT"   , 0  },
    { 'I', "LPMOVE"  , 0  },
    { 'I', "LPREP"   , 0  },
    { 'F', "LPTOL"   , 0  },
    { 'I', "LPGAP1"  , 0  },
    { 'I', "LPGAP2"  , 0  },
    { 'I', "LPSTART" , 0  },
    { 'I', "LPSTOP"  , 0  },

    { 'I', "WWPOW"   , 0  },
    { 'F', "WWWIDTH" , 0  },
    { 'F', "WWSHIFT" , 0  },

    {  1,  "END"     , 0  },
    {  0,  "END"     , 0  }
};

/*
 * Set the offsets's in the old123d_par array to the
 * corresponding positions on the parameter block par
 */
static void init_par_offsets(PBLOCK *par)
{
    old123d_par[OFF_SFD].offset = (int) ((float*)&(par->sfd) - (float*)par);
    old123d_par[OFF_SW].offset  = (int) ((float*)&(par->swhold) - (float*)par);
    old123d_par[OFF_REF].offset = (int) ((float*)&(par->xref) - (float*)par);
    old123d_par[OFF_PRE].offset = (int) ((float*)&(par->ipre) - (float*)par);
    old123d_par[OFF_RST].offset = (int) ((float*)&(par->irstrt) - (float*)par);
    old123d_par[OFF_RSP].offset = (int) ((float*)&(par->irstop) - (float*)par);
    old123d_par[OFF_WIN].offset = (int) ((float*)&(par->iwindo) - (float*)par);
    old123d_par[OFF_RSH].offset = (int) ((float*)&(par->rsh) - (float*)par);
    old123d_par[OFF_RLB].offset = (int) ((float*)&(par->rlb) - (float*)par);
    old123d_par[OFF_GN].offset  = (int) ((float*)&(par->gn) - (float*)par);
    old123d_par[OFF_ITM].offset = (int) ((float*)&(par->itm) - (float*)par);
    old123d_par[OFF_WST].offset = (int) ((float*)&(par->iwstrt) - (float*)par);
    old123d_par[OFF_WSP].offset = (int) ((float*)&(par->iwstop) - (float*)par);
    old123d_par[OFF_FFT].offset = (int) ((float*)&(par->ifft) - (float*)par);
    old123d_par[OFF_NZF].offset = (int) ((float*)&(par->nzf) - (float*)par);
    old123d_par[OFF_FTSCALE].offset = (int) ((float*)&(par->ftscale) - (float*)par);
    old123d_par[OFF_AHO].offset = (int) ((float*)&(par->ahold) - (float*)par);
    old123d_par[OFF_BHO].offset = (int) ((float*)&(par->bhold) - (float*)par);
    old123d_par[OFF_I0H].offset = (int) ((float*)&(par->ihold) - (float*)par);
    old123d_par[OFF_PST].offset = (int) ((float*)&(par->ipost) - (float*)par);
    old123d_par[OFF_REV].offset = (int) ((float*)&(par->irever) - (float*)par);
    /*
     * Baseline
     */
    old123d_par[OFF_BAS].offset = (int) ((float*)&(par->ibase) - (float*)par);
    old123d_par[OFF_BTERMS].offset = (int) ((float*)&(par->iterms) - (float*)par);
    old123d_par[OFF_BTERMS2].offset = (int) ((float*)&(par->iterms2) - (float*)par);
    old123d_par[OFF_BST].offset = (int) ((float*)&(par->ibstrt) - (float*)par);
    old123d_par[OFF_BSP].offset = (int) ((float*)&(par->ibstop) - (float*)par);
    /*
     *
     */
    old123d_par[OFF_FIL].offset = (int) ((float*)&(par->ifilut) - (float*)par);
    old123d_par[OFF_SST].offset = (int) ((float*)&(par->isstrt) - (float*)par);
    old123d_par[OFF_SSP].offset = (int) ((float*)&(par->isstop) - (float*)par);
    old123d_par[OFF_SPEC].offset = (int) ((float*)&(par->isspec) - (float*)par);
    old123d_par[OFF_TD].offset   = (int) ((float*)&(par->td) - (float*)par);
    old123d_par[OFF_AREF].offset = (int) ((float*)&(par->aref) - (float*)par);
    old123d_par[OFF_BREF].offset = (int) ((float*)&(par->bref) - (float*)par);
    old123d_par[OFF_DSP].offset = (int) ((float*)&(par->dspshift) - (float*)par);

    old123d_par[OFF_LPTYPE].offset = (int) ((float*)&(par->lpc) - (float*)par);
    old123d_par[OFF_LPMODE].offset = (int) ((float*)&(par->lpcmode) - (float*)par);
    old123d_par[OFF_LPROOT].offset = (int) ((float*)&(par->npoles) - (float*)par);
    old123d_par[OFF_LPFUT].offset = (int) ((float*)&(par->nfut) - (float*)par);
    old123d_par[OFF_LPMOVE].offset = (int) ((float*)&(par->mroot) - (float*)par);
    old123d_par[OFF_LPREP].offset = (int) ((float*)&(par->replace) - (float*)par);
    old123d_par[OFF_LPTOL].offset = (int) ((float*)&(par->toler) - (float*)par);
    old123d_par[OFF_LPGAP1].offset = (int) ((float*)&(par->ngap1) - (float*)par);
    old123d_par[OFF_LPGAP2].offset = (int) ((float*)&(par->ngap2) - (float*)par);
    old123d_par[OFF_LPSTART].offset = (int) ((float*)&(par->nstart) - (float*)par);
    old123d_par[OFF_LPSTOP].offset = (int) ((float*)&(par->nstop) - (float*)par);
    old123d_par[OFF_WWPOW].offset = (int) ((float*)&(par->iopt) - (float*)par);
    old123d_par[OFF_WWWIDTH].offset = (int) ((float*)&(par->kc) - (float*)par);
    old123d_par[OFF_WWSHIFT].offset = (int) ((float*)&(par->wshift) - (float*)par);

}

static void swap4(void *i)
{
    char *ii=(char*)i;
    char *jj =ii+3; 
    *ii ^= *jj;
    *jj ^= *ii;
    *ii ^= *jj;
    ii++;
    jj--;
    *ii ^= *jj;
    *jj ^= *ii;
    *ii ^= *jj;
}

/*
 * Get a parameter form the parameter-record 'par'
 * and store it in the PARDEF record 'pd', together
 * with its name, size, and type
 *
 * On success return the next (unused) PARDEF record,
 * on failure return this PARDEF record.
 */
static PARDEF *setpar(PARDEF *pd, int type, char *name, int offset, 
                      PBLOCK *par, int swapit)
{
    int *ip;
    float *fp;

    if (offset == 0) 
        return pd;
    pd->size = (unsigned char) sizeof(PARDEF);
    pd->type = (char) type;
    memset(pd->name, 0, PARNAMESIZE);
    strncpy(pd->name, name, PARNAMESIZE);
    if (pd->type == 'F') {
        fp = (float*) par;
        pd->value.f = fp[offset];
        if (swapit)
            swap4(&(pd->value.f));
    }
    else if (pd->type == 'I') {
        ip = (int*) par;
        pd->value.i = ip[offset];
        if (swapit)
            swap4(&(pd->value.i));
    }
    return (PARDEF*) (((char*)pd) + pd->size);
}

/*
 * Get next parameter from the PARDEF block
 */
static PARDEF *getnextpar(PARDEF *pd)
{
    if (pd->size == 0)
        return NULL;
    return (PARDEF*) (((char*)pd) + pd->size);
}

/*
 * Searches for the variable with 'pd->name' in the array
 * 'old123d_par'. On success, copy the variable 
 * to the 'par' record, from the corresponding position
 * in the parameter block 'pd'
 *
 * Return 1 on success, 0 otherwise
 */
static int findpar(PARDEF *pd, PBLOCK *par, int swapit)
{
    int *ip;
    float *fp;
    int i;

    if (pd == NULL)
        return 0;
    for (i=0;old123d_par[i].type;i++) {
        if (old123d_par[i].offset==0)
            continue;
        if (strncmp(old123d_par[i].name, pd->name, PARNAMESIZE)==0) {
            if (old123d_par[i].type == 'F') {
                fp = (float*) par;
                /*
                 * if type of parameter is different in 'pd' and 'par',
                 * do a cast
                 */
                if (pd->type == 'I')
                    fp[old123d_par[i].offset]=(float)pd->value.i;
                else
                    fp[old123d_par[i].offset]=pd->value.f;
                if (swapit)
                    swap4((void*)(fp+old123d_par[i].offset));
            }
            else if (old123d_par[i].type == 'I') {
                ip = (int*) par;
                /*
                 * if type of parameter is different in 'pd' and 'par',
                 * do a cast
                 */
                if (pd->type == 'F')
                    ip[old123d_par[i].offset]=(int)pd->value.f;
                else
                    ip[old123d_par[i].offset]=pd->value.i;
                if (swapit)
                    swap4((void*)(ip+old123d_par[i].offset));
            }
            else
                return 0;
            return 1;
        }
    }
    return 0;
}

/*
 * Searches for the variable with 'pd->name' in the array
 * 'old123d_par'. On success, print the variable 
 * to the 'label' text record, from the corresponding position
 * in the parameter block 'pd'
 *
 * Return 1 on success, 0 otherwise
 */
static int findpar_text(PARDEF *pd, char *label, int swapit)
{
    int   ip;
    float fp;
    int i;

    if (pd == NULL)
        return 0;
    for (i=0;old123d_par[i].type;i++) {
        if (strncmp(old123d_par[i].name, pd->name, PARNAMESIZE)==0) {
            if (pd->type == 'F') {
                fp = pd->value.f;
                if (swapit)
                    swap4((void*)&fp);
                sprintf(label, "%-10s F %f\n",pd->name, fp);
            }
            else if (pd->type == 'I') {
                ip = pd->value.i;
                if (swapit)
                    swap4((void*)&ip);
                sprintf(label,"%-10s I %d\n",pd->name, ip);
            }
            else
                return 0;
            return 1;
        }
    }
    return 0;
}

/*
 * Store parameters from prameter-record 'par'
 * in memory-block 'xpar', in old 3D style
 */
static void old123d_parstt(float *xpar, int idir, PBLOCK *par)
{
    int ntnpar, nt0par,ioff;
    OLD3D_PAR_RECORD *tpr;
    DIM_PAR *dpar;

    tpr  = (OLD3D_PAR_RECORD*) xpar;
    ntnpar = max(tpr->ntnpar,20);
    nt0par = max(tpr->nt0par,10);
    ioff   = (idir-1) * ntnpar + nt0par;
    dpar = (DIM_PAR*) (xpar + ioff);
    dpar->sfd		= par->sfd;
    dpar->sw		= par->swhold;
    dpar->xref		= par->xref; 
    dpar->ipre		= par->ipre; 
    dpar->irstrt	= par->irstrt;
    dpar->itd		= par->irstop; 
    dpar->iwindo	= par->iwindo;
    dpar->rsh		= par->rsh; 
    dpar->rlb		= par->rlb; 
    dpar->gn		= par->gn;
    dpar->itm		= par->itm; 
    dpar->iwstrt	= par->iwstrt;
    dpar->iwstop	= par->iwstop;
    dpar->ifft		= par->ifft; 
    dpar->nzf		= par->nzf; 
    dpar->iphase	= par->iphase;
    dpar->ahold		= par->ahold;
    dpar->bhold		= par->bhold;
    dpar->i0hold	= par->ihold;
    dpar->ipost		= par->ipost;
    dpar->irever	= par->irever;
    dpar->ibase		= par->ibase;
    dpar->iterms	= par->iterms;
    dpar->iterms2	= par->iterms2;
    dpar->ibstrt	= par->ibstrt;
    dpar->ibstop	= par->ibstop;
    dpar->ifilut	= par->ifilut;
    dpar->isstrt	= par->isstrt;
    dpar->isstop	= par->isstop;

    dpar->isspec	= par->isspec;
    dpar->td		= par->td;
    dpar->aref		= par->aref; 
    dpar->bref		= par->bref; 
    dpar->dspshift	= par->dspshift; 

}

/*
 * Store parameter from parameter block par onto
 * packed-parameter-block xpar.
 * idir is the direction corresponding to the par record
 * if create is true, a new xpar record is created, that is,
 * a record with all the new parameters on it
 */
void psnd_parstt(float *xpar, int idir, PBLOCK *par, int create)
{
    int i,ioff,dim,par_size,header_size;
    float *xp;
    PARDEF_HEADER *ph;
    PARDEF *pd;

    ph = (PARDEF_HEADER *) xpar;
    if (ph->type == FILE_TYPE_OLD3D) {
        old123d_parstt(xpar,idir,par);
        return;
    }
    dim         = ph->iaqdir;
    par_size    = ph->ntnpar;
    header_size = ph->nt0par;
    if (ph->swapit) {
        swap4(&dim);
        swap4(&par_size);
        swap4(&header_size);
    }
    if (idir > dim)
        return;
    ioff   = (idir-1) * par_size + header_size;
    xp     = xpar + ioff/sizeof(float);
    init_par_offsets(par);
    /*
     * Create a new xpar
     */
    if (create) {
        for (i=0;old123d_par[i].type;i++) 
            xp = (float*) setpar((PARDEF*) xp, 
                             old123d_par[i].type, 
                             old123d_par[i].name, 
                             old123d_par[i].offset,
                             par,
                             ph->swapit);
        return;
    }
    /*
     * Update xpar
     */
    pd = (PARDEF*) xp;
    while (pd->type) {
        for (i=0;old123d_par[i].type;i++) {
            if (!old123d_par[i].offset)
                continue;
            if (strcmp(pd->name, old123d_par[i].name) == 0) {
                xp = (float*) pd;
                setpar((PARDEF*) xp, 
                             old123d_par[i].type, 
                             old123d_par[i].name, 
                             old123d_par[i].offset,
                             par,
                             ph->swapit);
                break;
            }
        }
        pd += 1;
    }
}


/*
 * Get parameters from memory-block 'xpar',
 * and store those in parameter-record 'par', in old 3D style
 */
static void old123d_pargtt(float *xpar, int idir, PBLOCK *par)
{
    int ntnpar, nt0par,ioff;
    OLD3D_PAR_RECORD *tpr;
    DIM_PAR *dpar;

    tpr  = (OLD3D_PAR_RECORD*) xpar;
    ntnpar = max(tpr->ntnpar,20);
    nt0par = max(tpr->nt0par,10);
    ioff   = (idir-1) * ntnpar + nt0par;
    dpar = (DIM_PAR*) (xpar + ioff);
    par->sfd      = dpar->sfd;
    par->swhold   = dpar->sw;
    par->xref     = dpar->xref;
    par->ipre     = dpar->ipre;
    par->irstrt   = dpar->irstrt;
    par->irstop   = dpar->itd;
    par->iwindo   = dpar->iwindo;
    par->rsh      = dpar->rsh;
    par->rlb      = dpar->rlb;
    par->gn       = dpar->gn;
    par->itm      = dpar->itm;
    par->iwstrt   = dpar->iwstrt;
    par->iwstop   = dpar->iwstop;
    par->ifft     = dpar->ifft;
    par->nzf      = dpar->nzf;
    par->iphase   = dpar->iphase;
    par->ahold    = dpar->ahold;
    par->bhold    = dpar->bhold;
    par->ihold    = dpar->i0hold;
    par->ipost    = dpar->ipost;
    par->irever   = dpar->irever;
    par->ibase    = dpar->ibase;
    par->iterms   = dpar->iterms;
    par->iterms2  = dpar->iterms2;
    par->ibstrt   = dpar->ibstrt;
    par->ibstop   = dpar->ibstop;
    par->ifilut   = dpar->ifilut;
    par->isstrt   = dpar->isstrt;
    par->isstop   = dpar->isstop;

    par->isspec   = dpar->isspec;
    par->td       = dpar->td;
    par->aref     = dpar->aref;
    par->bref     = dpar->bref;
    par->dspshift = dpar->dspshift;

}

/*
 * Get parameters from packed-parameter-block xpar
 * and put those in parameter block par.
 * idir is the direction, that is, the number of the parameter block
 */
void psnd_pargtt(float *xpar, int idir, PBLOCK *par)
{
    int ioff,dim,par_size,header_size,id = 1;
    PARDEF *pd;
    float *xp;
    PARDEF_HEADER *ph;

    ph = (PARDEF_HEADER *) xpar;
    if (ph->type == FILE_TYPE_OLD3D) {
        old123d_pargtt(xpar,idir,par);
    }
    else {
        dim         = ph->iaqdir;
        par_size    = ph->ntnpar;
        header_size = ph->nt0par;
        if (ph->swapit) {
            swap4(&dim);
            swap4(&par_size);
            swap4(&header_size);
        }
        if (idir > dim)
            return;
        ioff   = (idir-1) * par_size + header_size;
        xp     = xpar + ioff/sizeof(float);
        pd     = (PARDEF*) xp;
        init_par_offsets(par);
        while (pd) {
            id = findpar(pd, par, ph->swapit);
            pd = getnextpar(pd);
        }
    }
    /*
     * Set some newer parameters to acceptable defaults,
     * when not defined
     */
    if (par->aref <= 0) {
        par->aref = 1.0;
        par->bref = 1.0;
    }
    if (par->td <= 0) {
        par->td = par->nsiz;
    }
    if (par->iopt <= 0) {
        par->iopt = 3;
    }
}

/*
 * Packed-parameter-record in text format ,label, is translated
 * and stored on packed-parameter-record xpar
 */
int psnd_parstt_text(float *xpar, char *label)
{
    int i,ioff,dim,par_size,header_size,idir;
    float *xp;
    PARDEF_HEADER *ph;
    PARDEF *pd;
    char *p, delim[] = "\r\n";

    ph = (PARDEF_HEADER *) xpar;
    if (ph->type == FILE_TYPE_OLD3D) {
        return 0;
    }
    dim         = ph->iaqdir;
    par_size    = ph->ntnpar;
    header_size = ph->nt0par;
    if (ph->swapit) {
        swap4(&dim);
        swap4(&par_size);
        swap4(&header_size);
    }
    p = strtok(label, delim);
    idir = 0;
    while (p) {
        char name[100], type;
        
        if (strncmp(p,"Direction",9) == 0) {
            sscanf(p, "Direction:%d", &idir);
            ioff   = (idir-1) * par_size + header_size;
            xp     = xpar + ioff/sizeof(float);
            pd     = (PARDEF*) xp;
        }
        else if (idir > 0 && idir <= dim) {
            name[0] = '\0';
            type    = 0;
            sscanf(p, "%s %c", name, &type);
            
            for (i=0;(pd+i)->type;i++) {
                if (strcmp(name, (pd+i)->name)==0) {
                    if (type == 'F') {
                        float f = 0.0;
                        sscanf(p, "%s %c %f", name, &type, &f);
                        if (ph->swapit)
                            swap4(&f);
                        (pd+i)->value.f = f;
                    }
                    else if (type == 'I') {
                        int j = 0;
                        sscanf(p, "%s %c %d", name, &type, &j);
                        if (ph->swapit)
                            swap4(&j);
                        (pd+i)->value.i = j;
                    }
                }
            }
        }
        p = strtok(NULL, delim);
    }
    return 1;
}

/*
 * Get parameters from packed-parameter-block xpar, starting
 * with direction idir_start and ending with direction idir_stop.
 * The parameters are printed on a text(ASCII) buffer, that is
 * allocated here and returned. The calling routine must free
 * this record after use. For old-i/o-type OLD3D files, NULL 
 * is returned.
 */
char *psnd_pargtt_text(float *xpar, int idir_start, int idir_stop)
{
    int ioff,dim,par_size,header_size,id = 1;
    PARDEF *pd;
    float *xp;
    PARDEF_HEADER *ph;
    char *par=NULL;
    int parlen=0,parsize=0,idir;

    ph = (PARDEF_HEADER *) xpar;
    if (ph->type == FILE_TYPE_OLD3D) {
        return NULL;
    }
    else {
        dim         = ph->iaqdir;
        par_size    = ph->ntnpar;
        header_size = ph->nt0par;
        if (ph->swapit) {
            swap4(&dim);
            swap4(&par_size);
            swap4(&header_size);
        }
        for (idir=idir_start;idir<=idir_stop;idir++) {
            char label[200];
            if (idir > dim)
                return par;
            ioff   = (idir-1) * par_size + header_size;
            xp     = xpar + ioff/sizeof(float);
            pd     = (PARDEF*) xp;
            sprintf(label,"Direction:%d\n", idir);
            while (pd) {
                if (parlen + (int)strlen(label) >= parsize) {
                    if (parsize == 0) {
                        parsize = 100;
                        par = (char*)calloc(parsize,sizeof(char));
                    }
                    else {
                        parsize *= 2;
                        par = (char*)realloc(par,parsize*sizeof(char));
                    }
                }
                parlen += strlen(label);
                strcat(par,label);
                id = findpar_text(pd, label, ph->swapit);
                if (!id)
                    break;
                pd = getnextpar(pd);
            }
        }
    }
    return par;
}

typedef union {
    unsigned char byte[4];
    long i;
} ENDI;

/*
 * Read parameter block header
 */
void psnd_pargtm(int file_type, float *parms, int *nt0par, int *ntnpar, 
                          int *iaqdir, int *nextdr)
{
    PARDEF_HEADER *ph;
    ENDI endi;

    ph = (PARDEF_HEADER *) parms;
    *ntnpar = ph->ntnpar;
    *nt0par = ph->nt0par;
    *iaqdir = ph->iaqdir;
    *nextdr = ph->nextdr;
    /*
     * If old style OLD3D file, params have been swapped in the
     * iora layer, so return
     */
    if (file_type == FILE_TYPE_OLD3D) {
        ph->type = FILE_TYPE_OLD3D;
        return;
    }
    ph->type = FILE_TYPE_ND;
    endi.i   = ph->endi;
    ph->swapit  = (os_big_endian() != endi.byte[3]);
    if (ph->swapit) {
        swap4(ntnpar);
        swap4(nt0par);
        swap4(iaqdir);
        swap4(nextdr);
    }
    *ntnpar /= sizeof(float);
    *nt0par /= sizeof(float);
}


/*
 * Initiate parameter block header
 */
void psnd_parstm(int file_type, float *parms, int nt0par, int ntnpar, 
                 int iaqdir, int nextdr)
{
    PARDEF_HEADER *ph;

    ph = (PARDEF_HEADER *) parms;
    if (file_type == FILE_TYPE_OLD3D) {
        memset(ph,0,nt0par);
        ph->type        = FILE_TYPE_OLD3D;
        ph->ntnpar	= ntnpar;
        ph->nt0par  	= nt0par;
        ph->iaqdir	= iaqdir;
        ph->nextdr	= nextdr;
    }
    else {
        nt0par = sizeof(PARDEF_HEADER);
        ntnpar = OFF_ENDBLOCK * sizeof(PARDEF);
        memset(ph,0,nt0par);
        ph->size	= iaqdir *  ntnpar + nt0par;
        ph->type        = FILE_TYPE_ND;
        ph->ntnpar	= ntnpar;
        ph->nt0par  	= nt0par;
        ph->iaqdir	= iaqdir;
        ph->nextdr	= nextdr;
        ph->swapit      = 0;
        ph->endi        = 1;
    }
}

/*
 * Return the max possible raw-parameter block size
 * for 'dim' dimensions. This for iond, to know how much 
 * parameter-buffer space to allocate.
 */
int iond_par_get_size(int dim)
{
    return dim * (OFF_ENDBLOCK * sizeof(PARDEF)) + sizeof(PARDEF_HEADER);
}

