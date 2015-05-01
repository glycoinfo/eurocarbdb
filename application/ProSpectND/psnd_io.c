/********************************************************************/
/*                             psnd_io.c                            */
/*                                                                  */
/* Interface between program and low level i/o routines             */
/* 1997, Albert van Kuik                                            */
/********************************************************************/

#include <stdio.h>
#include <stdlib.h>
#include <ctype.h>
#include <string.h>
#include <math.h>
#include <assert.h>
#include <unistd.h>
#include <time.h>
#include "genplot.h"
#include "iora.h"
#include "iond.h"
#include "psnd.h"
#include "nmrtool.h"


/*************************************************************************
**************************************************************************
* OBSOLETE
* All xx files do old 3D mode if mblk->info->version = FALSE
* and new multi-vision mode if mblk->info->version = TRUE
*
**************************************************************************
*************************************************************************/

void xx_wrparm(MBLOCK *mblk, int fil, float *xpar, DBLOCK *dat);

static void xx_close_infile(MBLOCK *mblk, DBLOCK *dat)
{
    if (dat->finfo[INFILE].fopen) {
        iond_close ((NDFILE *)dat->finfo[INFILE].ndfile, CLOSE_KEEP);
        dat->finfo[INFILE].fopen = FALSE;
        dat->ityp   = 0;
        dat->ndim   = 0;
        dat->xpar   = NULL;
        psnd_compose_prompt(NULL, dat);
        psnd_init_undo(dat);
        psnd_init_plane_undo(dat);
    }
}

static int xx_open_read(MBLOCK *mblk, char *filename, int *size, int *complex)
{
    int i,ierr;
    int ndim,ityp;
    char *p;
    NDFILE *ndfile;
    int rectype, isize[MAX_DIM_EXTRA];
   
    xx_close_infile(mblk, DAT);
    if (filename != DAT->finfo[INFILE].name)
        strncpy(DAT->finfo[INFILE].name, filename, PSND_STRLEN-1);
    for (i=0;i<MAX_DIM;i++)
        size[i] = 1;
    for (i=0;i<MAX_DIM;i++)
        isize[i] = size[i];
    /*
     * Calc the max possible parameter block size
     * and store it after the data sizes in the 'isize' array
     */
    isize[PAR_SIZE_MAX] = iond_par_get_size(MAX_DIM);
    /*
     * 4 nd buffers for reading files
     */
    isize[BUF_SIZE_NUM] = 4;
    ndfile = iond_open(filename, OPEN_RW, &ityp, isize, complex);
    ierr = (ndfile == NULL);
    if (ierr)
        return FALSE;
    /*
     * for 3D files, we can use more buffers
     */
    if (ityp >=3)
        iond_set_numbuffers(ndfile, 32 ) ;
    for (i=0;i<MAX_DIM;i++) 
        size[i] = isize[i];
    DAT->finfo[INFILE].ndfile      = (void*) ndfile;
    DAT->npar         = (int)ndfile->structure->par_size;
    DAT->finfo[INFILE].file_type   = (int)ndfile->file_type;
    DAT->finfo[INFILE].read_only   = (int)ndfile->readonly;
    rectype = iond_get_rectype(ndfile, 
                         &(DAT->finfo[INFILE].tresh_levels[0]),
                         &(DAT->finfo[INFILE].tresh_levels[1]),
                         &(DAT->finfo[INFILE].tresh_levels[2]));
    switch (rectype) {
        case DATA_FLOAT_4_TRUNC:
            DAT->finfo[INFILE].tresh_flag   = TRUE;
            DAT->finfo[INFILE].sizeof_float = 4;
            break;

        case DATA_FLOAT_3_TRUNC:
            DAT->finfo[INFILE].tresh_flag   = TRUE;
            DAT->finfo[INFILE].sizeof_float = 3;
            break;

        case DATA_FLOAT_2_TRUNC:
            DAT->finfo[INFILE].tresh_flag   = TRUE;
            DAT->finfo[INFILE].sizeof_float = 2;
            break;

        case DATA_FLOAT_4:
            DAT->finfo[INFILE].tresh_flag   = FALSE;
            DAT->finfo[INFILE].sizeof_float = 4;
            break;

        case DATA_FLOAT_3:
            DAT->finfo[INFILE].tresh_flag   = FALSE;
            DAT->finfo[INFILE].sizeof_float = 3;
            break;

        case DATA_FLOAT_2:
            DAT->finfo[INFILE].tresh_flag   = FALSE;
            DAT->finfo[INFILE].sizeof_float = 2;
            break;

    }
    DAT->finfo[OUTFILE].sizeof_float = DAT->finfo[INFILE].sizeof_float;
    DAT->finfo[OUTFILE].tresh_flag   = DAT->finfo[INFILE].tresh_flag;
    for (i=0;i<3;i++)
        DAT->finfo[OUTFILE].tresh_levels[i] =
            DAT->finfo[INFILE].tresh_levels[i];

    for (i=0,ndim=0,ityp=0;i<MAX_DIM;i++) {
        if (size[i] > 1) {
            ndim++;
            ityp = i+1;
        }
    }
    DAT->ityp   = ityp;
    DAT->ndim   = ndim;
    DAT->finfo[INFILE].fopen = TRUE;
    /*
     * Construct prompt
     */
#ifdef _WIN32
    if ((p=strrchr(DAT->finfo[INFILE].name, '\\'))== NULL) 
#else
    if ((p=strrchr(DAT->finfo[INFILE].name, '/'))== NULL) 
#endif
        p = DAT->finfo[INFILE].name;
    else
        p++;
    psnd_compose_prompt(p, DAT);
    return TRUE;
}

static int read_row_iond(float *xreal, float *ximag, NDFILE *ndfile, 
                         int *access, int *keys, int iisize)
{
    fcomplexp *record_c;
    int i,isize;

    record_c = iond_read_1d(ndfile, keys);

    if (record_c == NULL) {
        /*
          printf("Error reading record\n");
        */
        return 1;
    }
    
    isize = min(ndfile->size[access[0]],iisize);
    
    if (ndfile->complex[access[0]]) {
        memcpy(xreal, 
               record_c->r, 
               sizeof(float) * isize);
        memcpy(ximag, 
               record_c->i, 
               sizeof(float) * isize);
    }
    else {
        memcpy(xreal, 
               record_c->r, 
               sizeof(float) * isize);
    }
    return 0;
}

/*
 * Read one record from standard input file
 */
static void xx_read_row(MBLOCK *mblk, DBLOCK *dat, PBLOCK *par, 
                        int *access, int *keys, int size)
{
    int ierr, offset;
    int idim, access_tmp[MAX_DIM], keys_tmp[MAX_DIM];
    float *xreal, *ximag;

    if (dat->finfo[INFILE].fopen <= 0) 
        return;
    if (par->icmplx && par->ipre == PREIMA) {
        ximag = dat->xreal;
        xreal = dat->ximag;
    }
    else {
        xreal = dat->xreal;
        ximag = dat->ximag;
    }
    for (idim=0; idim<MAX_DIM; idim++) {
        access_tmp[idim] = access[idim]-1;
        keys_tmp[access_tmp[idim]] = keys[idim]-1;
    }
    ierr = iond_set_access((NDFILE*)dat->finfo[INFILE].ndfile, access_tmp);

    read_row_iond(xreal, ximag, 
                  (NDFILE*)dat->finfo[INFILE].ndfile, 
                  access_tmp, keys_tmp, size);
  
    offset  = par->irstrt-1;
    /*
     * If not complex, zero-fill the other array
     */
    if (par->ipre == PREREA) {
        memset(ximag, 0, mblk->info->block_size * sizeof(float));
        /*
         * If record start at irstrt then shift al data irstrt to the left
         */
        if (offset) 
            xxshfn(dat->xreal,1,dat->isize,offset,mblk->info->block_size);
        /*
         * Zero the rest of the real and imag array's
         */
        memset(xreal+size, 0, 
               (mblk->info->block_size - size) * sizeof(float));
    }
    else {
        /*
         * If record start at irstrt then shift al data irstrt to the left
         */
        if (offset) {
            xxshfn(xreal,1,size,offset,mblk->info->block_size);
            xxshfn(ximag,1,size,offset,mblk->info->block_size);
        }
        /*
         * Zero the rest of the real and imag array's
         */
        memset(xreal+size, 0, 
                   (mblk->info->block_size - size) * sizeof(float));
        memset(ximag+size, 0, 
                   (mblk->info->block_size - size) * sizeof(float));
    }
}


/*
 * Read 1D data array. **NOT** from standard input file
 */
static int xx_readrow_array(MBLOCK *mblk, int infil, NDFILE* ndfile, float *array1, 
               float *array2, int isize)
{
    int ierr, i;
    int access[MAX_DIM], keys[MAX_DIM];
    
    for (i=0;i<MAX_DIM;i++) {
        access[i] = i;
        keys[i]   = 0;
    }
    ierr = iond_set_access(ndfile, access);
    if (!ierr)
        ierr = read_row_iond(array1, array2, ndfile, access, 
                             keys, isize);

    return ierr;
}



/*
 * Read from file 'idev' the record 'key1' from the current plane
 * and in the current direction in the array 'xdata'. Read 'nsize' points.
 */
int read2d(void *vmblk, int idev, float *xdata, int nsize, int key1)
{
    int ierr,iswap;
    int idim, access_tmp[MAX_DIM], keys_tmp[MAX_DIM];
    float *xreal, *ximag;
    MBLOCK *mblk = (MBLOCK*) vmblk;

    if (DAT->finfo[INFILE].fopen <= 0) 
        return FALSE;
    if (DAT->pars[0]->icmplx && DAT->pars[0]->ipre == PREIMA) {
        ximag = xdata;
        xreal = DAT->work2;
        iswap = TRUE;
    }
    else {
        xreal = xdata;
        ximag = DAT->work2;
        iswap = FALSE;
    }
    for (idim=0; idim <MAX_DIM; idim++) {
        access_tmp[idim]             = DAT->access[idim]-1;
        keys_tmp[access_tmp[idim]]   = DAT->pars[idim]->key-1;
    }
    keys_tmp[access_tmp[1]] = key1-1;

    ierr = iond_set_access((NDFILE*)DAT->finfo[INFILE].ndfile, access_tmp);
    if (!ierr)
        ierr = read_row_iond(xdata, ximag, (NDFILE*)DAT->finfo[INFILE].ndfile,
                             access_tmp, keys_tmp, nsize);
    if (!ierr) {
        int offset  = DAT->pars[0]->irstrt-1;
        /*
         * If record start at irstrt then shift al data irstrt to the left
         */
        if (offset) 
            xxshfn(xdata,1,DAT->isize,offset,mblk->info->block_size);
        DAT->isspec  = DAT->pars[0]->isspec;
        if (DAT->pars[0]->dspshift != 0.0 && !DAT->pars[0]->isspec)
            DAT->pars[0]->dspflag = TRUE;

        psnd_set_datasize(mblk,DAT->pars[0]->irstop - DAT->pars[0]->irstrt + 1,
/*                           TRUE, DAT);*/
                           FALSE, DAT);

    }
    return ierr;
}



/*
 * Read plane in buffer (new mulr2d)
 *
 * xcpr   = buffer
 * ncpr   = buffer size * maxdat = 
 * plane  = ial..iah x ibl..ibh
 * iacpr  = return x size of buffer matrix
 * ibcpr  = return y size of buffer matrix
 * id1    = step size in x direction
 * jd1    = step size in y direction
 */
int  read_plane_iond(MBLOCK *mblk, float *xcpr, int ncpr, 
                     int ial, int iah, int ibl, int ibh,
                     int *iacpr, int *ibcpr,
                     int id1, int jd1)
{
    int i,j,id,jd,ichk,ic;
    NDFILE *ndfile = (NDFILE*) DAT->finfo[INFILE].ndfile;
    int ierr,idim, access[MAX_DIM], keys[MAX_DIM];
    fcomplexp *record_c;
    int iswap;

    if (!okarea (ial,iah,ibl,ibh))
        return FALSE;

    id = max(1,id1);
    jd = max(1,jd1);

    ichk = ((ibh-ibl+1)/jd)*((iah-ial+1)/id);
    if (ichk > ncpr) {
         psnd_printf(mblk, " xcpr buffer overflow\n");
         return FALSE;
    }

    if (DAT->pars[0]->icmplx && DAT->pars[0]->ipre == PREIMA) {
        iswap = TRUE;
    }
    else {
        iswap = FALSE;
    }

    for (idim=0; idim <MAX_DIM; idim++) {
        access[idim]         = DAT->access[idim] - 1;
        keys[access[idim]]   = DAT->pars[idim]->key - 1;
    }

    ierr = iond_set_access(ndfile, access);
    if (ierr)  {
        psnd_printf(mblk, "Error in setting file access\n");
        return FALSE;
    }
    iah = min(ndfile->size[access[0]],iah);
    if (id == 1) {
        ic=0;
        *iacpr=iah-ial+1;
        *ibcpr=0;
        for (j=ibl-1;j<ibh;j+=jd) {
            (*ibcpr)++;
            keys[access[1]] = j;
            record_c = iond_read_1d(ndfile, keys);
            if (record_c == NULL) {
                psnd_printf(mblk, "Error reading record\n");
                return FALSE;
            }
            if (iswap)
                memcpy(xcpr+ic, record_c->i + ial-1, 
                       sizeof(float) * (*iacpr));
            else
                memcpy(xcpr+ic, record_c->r + ial-1, 
                       sizeof(float) * (*iacpr));
            ic += *iacpr;
        }
    }
    else {
        ic=0;
        *ibcpr=0;
        for (j=ibl-1;j<ibh;j+=jd) {
            (*ibcpr)++;
            keys[access[1]] = j;
            record_c = iond_read_1d(ndfile, keys);
            if (record_c == NULL) {
                psnd_printf(mblk, "Error reading record\n");
                return FALSE;
            }
            *iacpr=0;
            if (iswap) 
                for (i=ial-1;i<iah;i+=id) {
                    (*iacpr)++;
                    xcpr[ic++]=record_c->i[i];
                }
            else
                for (i=ial-1;i<iah;i+=id) {
                    (*iacpr)++;
                    xcpr[ic++]=record_c->r[i];
                }
        }
    }
    return TRUE;
}


int mulr2d(MBLOCK *mblk, float *xcpr,int ncpr,int io,float *x,int maxdat,
            int ial,int iah,int ibl,int ibh,int *iacpr,int *ibcpr,
            int id1,int jd1)
{
    if (DAT->finfo[INFILE].fopen <= 0) 
        return FALSE;
    ial += PAR1->irstrt - 1;
    iah += PAR1->irstrt - 1;
    ibl += PAR2->irstrt - 1;
    ibh += PAR2->irstrt - 1;
    /*
     * Update current size
     */
    psnd_set_datasize(mblk, iah - ial + 1, TRUE, DAT);

    return read_plane_iond(mblk,xcpr, ncpr, 
                           ial, iah, ibl, ibh, iacpr, ibcpr,
                           id1, jd1);
}


static char *nmrtime()
{
    static char ans[25];
    time_t t;
    char *c;
    t = time(NULL);
    c = asctime(localtime(&t));
    strncpy(ans,c,24);
    ans[24] = '\0';
    if ((c=strchr(ans,'\n'))!= NULL)
        *c = '\0';
    return ans;       
}

char *get_acqu_file(char *name)
{
    FILE *file;
    char buff[PSND_STRLEN+1];
    int len, size;
    char *script = NULL;
    
    if ((file=fopen(name,"r"))==NULL)
        return NULL;
    len=0;
    script = (char*) calloc(1, PSND_STRLEN);
    assert(script);
    while ((size=fread(buff, 1, PSND_STRLEN, file)) > 0) { 
        len += size;
        buff[size] = '\0';
        script = (char*) realloc(script, len+1);
        assert(script);
        strcat(script, buff);
    }    
    fclose(file);
    return script;
}

/*
 * Open output file
 */
int xx_open_write(MBLOCK *mblk, char *filename, int *size, int *complex,
                  int newfile, int count, char *names[MAX_DIM], DBLOCK *dat)
{
    int ierr = 0;
    
        NDFILE *ndfile;
        char *script,*script_old;
        char *brk, *bruk, bruker[80];
        char title[100];
        int i,ndim=0,parsize;
        int isize[MAX_DIM_EXTRA];

        for (i=0;i<MAX_DIM;i++) {
            if (size[i] > 1) 
                ndim = i+1;
            isize[i] = size[i];
        }
        /*
         * Bruker record
         */
        brk  = NULL;
        bruk = NULL;
        if (dat->finfo[OUTFILE].fopen == CREATEFILE && count > 0) {
            int size = 0;
            
            for (i=0;i<count && i<MAX_DIM;i++) {
                char *p;
                p=get_acqu_file(names[i]);
                if (p==NULL)
                    break;
                size += strlen(p);
                if (!bruk)
                    bruk = (char*) calloc(size+5,1);
                else
                    bruk = (char*) realloc(bruk,size+5);
                strcat(bruk, p);
                free(p);
            }
            if (bruk) {
                isize[BRK_SIZE_POS] = strlen(bruk)+1;
                brk = bruk;
            }
        }
        if (dat->finfo[OUTFILE].fopen != CREATEFILE && dat->finfo[INFILE].ndfile) {
            NDFILE *ndfile = (NDFILE *) dat->finfo[INFILE].ndfile;
            if (dat->finfo[INFILE].file_type != FILE_TYPE_OLD3D && ndfile->bruker) {
                isize[BRK_SIZE_POS] = strlen(ndfile->bruker);
                brk = ndfile->bruker;
            }
        }
        if (brk == NULL) {
            strcpy(bruker, "No acquistion parameters available\n");
            isize[BRK_SIZE_POS] = strlen(bruker)+1;
            brk = bruker;
        }
        /*
         * History record, save scripts
         */
        isize[HIS_SIZE_POS] = psnd_get_script_size() +
                              psnd_get_scriptarg_size();
        sprintf(title, 
                "#########################################\n# History record %s\n\n",
                nmrtime());
        isize[HIS_SIZE_POS] += strlen(title);
        script     = NULL;
        script_old = NULL;
        if (dat->finfo[OUTFILE].fopen != CREATEFILE && dat->finfo[INFILE].ndfile) {
             NDFILE *ndfile = (NDFILE *) dat->finfo[INFILE].ndfile;
             if (dat->finfo[INFILE].file_type != FILE_TYPE_OLD3D && ndfile->history) {
                 isize[HIS_SIZE_POS] += strlen(ndfile->history);
                 script_old = ndfile->history;
             }
        }
        script = (char*) calloc(isize[HIS_SIZE_POS]+5,1);
        if (script_old)
            strcat(script,script_old);
        strcat(script, title);
        strcat(script, psnd_get_scriptarg());
        strcat(script, psnd_get_script());
        /*
         * Parameter record
         */
        isize[PAR_SIZE_MAX]   = iond_par_get_size(MAX_DIM);
        isize[PAR_SIZE_POS]   = iond_par_get_size(ndim);
        if (newfile) {
            /*
             * 32 nd buffers for prepa files
             */
            isize[BUF_SIZE_NUM] = 32;
            ndfile = iond_open(filename, OPEN_NEW, &ndim, isize, complex);
        }
        else {
            /*
             * 4 nd buffers for writing files
             */
            isize[BUF_SIZE_NUM] = 4;
            ndfile = iond_open(filename, OPEN_RW, &ndim, isize, complex);
        }
        dat->finfo[OUTFILE].ndfile = (void*) ndfile;
        if (ndfile == NULL) 
            ierr = 1;
        else {
            dat->finfo[OUTFILE].file_type = ndfile->file_type;
            dat->finfo[OUTFILE].read_only = ndfile->readonly;
        }
        if (script) {
            if (ierr==0)
                update_his_record(ndfile, script);
            free(script);
        }
        if (ierr==0)
            update_brk_record(ndfile, brk);
        if (bruk)
            free(bruk);
        if (ierr==0 &&
            (dat->finfo[OUTFILE].sizeof_float != 4 ||
            dat->finfo[OUTFILE].tresh_flag)) {
            int rectype = 0;
            if (dat->finfo[OUTFILE].sizeof_float == 4) {
                if (dat->finfo[OUTFILE].tresh_flag)
                    rectype = DATA_FLOAT_4_TRUNC;
                else
                    rectype = DATA_FLOAT_4;                
            }
            else if (dat->finfo[OUTFILE].sizeof_float == 3) {
                if (dat->finfo[OUTFILE].tresh_flag)
                    rectype = DATA_FLOAT_3_TRUNC;
                else
                    rectype = DATA_FLOAT_3;                
            }
            else if (dat->finfo[OUTFILE].sizeof_float == 2) {
                if (dat->finfo[OUTFILE].tresh_flag)
                    rectype = DATA_FLOAT_2_TRUNC;
                else
                    rectype = DATA_FLOAT_2;                
            }
            iond_set_rectype(ndfile,
                             rectype,
                             dat->finfo[OUTFILE].tresh_levels[0],
                             dat->finfo[OUTFILE].tresh_levels[1],
                             dat->finfo[OUTFILE].tresh_levels[2]);
        }

    return !ierr;
}

/*
 * Close output file
 */
void xx_close_outfile(MBLOCK *mblk, DBLOCK *dat, float *xpar, float *xparhis )
{
    /*
     * MUST be 'seqall'
     */
    static char seqname[] = "SEQALL";
    int    ntnpar = dat->ntnpar+1, ierr;
    float  version = 970910.1;
    NDFILE *ndfile;
    /*
     * transport of parameter,bruker and history record from file
     * iunit1 to iunit2
     */
    if (dat->finfo[OUTFILE].fopen != OLDFILE) {
         xx_wrparm(mblk,
                   dat->finfo[OUTFILE].ifile,
                   xpar,
                   dat);
    }

    ndfile = (NDFILE*) dat->finfo[OUTFILE].ndfile;
    iond_close(ndfile, CLOSE_KEEP);

    dat->finfo[OUTFILE].fopen = FALSE;
    
}

static int write_row_iond(float *xreal, float *ximag, NDFILE *ndfile, int *access, 
                  int *keys, int iisize)
{
    fcomplexp *record_c;
    int i,isize,ierr;
    ierr = iond_write_1d(ndfile, keys, iisize, xreal, ximag);
    return ierr;
}


/*
 * Write one record
 */
static int xx_write_row(MBLOCK *mblk, int fileno, NDFILE *ndfile, float *xreal, float *ximag,
                 int *access, int *keys, int size, int complex)
{
    int ierr;
    int idim, access_tmp[MAX_DIM], keys_tmp[MAX_DIM];

    for (idim=0; idim<MAX_DIM; idim++) {
        access_tmp[idim] = access[idim]-1;
        keys_tmp[access_tmp[idim]] = keys[idim]-1;
    }
    ierr = iond_set_access(ndfile, access_tmp);

    if (!ierr)
        ierr = iond_write_1d(ndfile, keys_tmp, size, xreal, ximag);

    return ierr;
}


/******************************************
 * Parameters
 ******************************************/
 

void xx_rdparm(MBLOCK *mblk, int from_dr)
{
    int ier, ifatal = -1; 
    int idir, dim;       
    
    DAT->xpar = (float*)((NDFILE *)DAT->finfo[INFILE].ndfile)->parameter;
    /*
     * Rest of iond-param stuff also works for oldstyle files
     */
    psnd_pargtm(DAT->finfo[INFILE].file_type, DAT->xpar, 
                &(DAT->nt0par), &(DAT->ntnpar),
                &(DAT->iaqdir), &(DAT->nextdr));
    /*
     * Kludge
     */
    if (DAT->iaqdir == 0)
        DAT->iaqdir = DAT->ityp;
        
    /*
     * read the parameters for all dimensions 
     */
    for (dim=0; dim < MAX_DIM; dim++) {

        idir = PAR0(dim)->icrdir;

        psnd_pargtt(DAT->xpar, idir, PAR0(dim));
    
        if (PAR0(dim)->ipre != NOPRE && PAR0(dim)->irstrt < 1) 
            PAR0(dim)->irstrt = 1;

        if (PAR0(dim)->ipre != NOPRE && PAR0(dim)->irstop <= 0)
            PAR0(dim)->irstop = PAR0(dim)->nsiz;

        if (PAR0(dim)->irstop > PAR0(dim)->nsiz) {
            PAR0(dim)->irstop = PAR0(dim)->nsiz;
            if (dim < DAT->ityp)
                psnd_printf(mblk, 
                    " read parameter in dim %d too large for data, set to: %d\n",
                    dim+1,PAR0(dim)->irstop);
        }

        if (PAR0(dim)->irstop > mblk->info->block_size) {
            psnd_realloc_buffers(mblk, PAR0(dim)->irstop);
        }

        if (PAR0(dim)->sfd == 0. ) 
            PAR0(dim)->sfd = 1.0;

        psnd_parstt(DAT->xpar, idir, PAR0(dim), FALSE);
        PAR0(dim)->ipost = NOPST;

        /*
        if (PAR0(dim)->isstrt == 1)
            PAR0(dim)->isstrt = 0;
        if (PAR0(dim)->isstop == PAR0(dim)->nsiz ||
            PAR0(dim)->isstop == PAR0(dim)->icstop)
                PAR0(dim)->isstop = 0;
        */

        /*
         * These parameters must be cleared
         */
        PAR0(dim)->isstrt = 0;
        PAR0(dim)->isstop = 0;

        PAR0(dim)->iphase = NOPHA;      
        PAR0(dim)->ahold  = 0;	
        PAR0(dim)->bhold  = 0;
        PAR0(dim)->ihold  = 1;
    }
}

/*
 * write parameters
 */
void xx_wrparm(MBLOCK *mblk, int fil, float *xpar, DBLOCK *dat)
{
    int ier,size;

    if (fil == dat->finfo[INFILE].ifile) {
        size = dat->npar;
        if (xpar != dat->xpar)
            memcpy(dat->xpar, xpar, size);
        update_par_record((NDFILE *)dat->finfo[INFILE].ndfile);
    }
    else if (fil == dat->finfo[OUTFILE].ifile) {
        float *xxpar = (float*)((NDFILE *)dat->finfo[OUTFILE].ndfile)->parameter;
        size = ((NDFILE *)dat->finfo[OUTFILE].ndfile)->structure->par_size;
        if (xpar != xxpar)
            memcpy(xxpar, xpar, size);
        update_par_record((NDFILE *)dat->finfo[OUTFILE].ndfile);
    }
}

void psnd_update_raw_param(MBLOCK *mblk)
{
    update_par_record((NDFILE *)DAT->finfo[INFILE].ndfile);
}

float *xx_parcopy(MBLOCK *mblk, float *xpar_src, int dim)
{
    float *xpar; 
    int size;
    PBLOCK par;
    int idir;

    size = iond_par_get_size(dim);
    xpar    = (float*)  calloc(1, size);
    assert(xpar);
    psnd_parstm(1, xpar, 0,0,dim,dim);
    for (idir=1;idir<=dim;idir++) {
        memset(&par,0,sizeof(PBLOCK));
        psnd_pargtt(xpar_src, idir, &par);
        psnd_parstt(xpar, idir, &par, TRUE);
    }        
    return xpar;
}



short *psnd_get_sorted_list(MBLOCK *mblk, DBLOCK *dat, int rangetemp[], int range[],  
                             int loop_dim, int nsize, int icmplx[],
                             int is_input)
{
    short *rlist;

    rlist = (short*) malloc((nsize*4*loop_dim+2) * sizeof(short));
    assert(rlist);
    if (is_input)
        rlist = iond_get_sorted_list((NDFILE*) dat->finfo[INFILE].ndfile,
                                     rlist, rangetemp);
    else
        rlist = iond_get_sorted_list((NDFILE*) dat->finfo[OUTFILE].ndfile,
                                     rlist, rangetemp);
    return rlist;
}


/*************************************************************************/

/*
 * Select a file from disk, or give a new filename
 */
char *psnd_getfilename(MBLOCK *mblk, char *title, char *mask)
{
    char *tmp, *p;
    int len, init = FALSE;

    len = 0;
    if (mblk->info->dirmask) 
        len += strlen(mblk->info->dirmask);
    else
        init = TRUE;
    len += strlen(mask) + 1;
    if (len > mblk->info->dirmasksize){
        mblk->info->dirmasksize = len;
        mblk->info->dirmask = 
            (char*) realloc(mblk->info->dirmask, mblk->info->dirmasksize);
        assert(mblk->info->dirmask);
        if (init)
            mblk->info->dirmask[0] = '\0';
    }
    strcat(mblk->info->dirmask, mask);
    tmp = g_popup_getfilename(mblk->info->win_id, title, mblk->info->dirmask) ;
#ifdef _WIN32
    if (tmp != NULL && (strlen(tmp) > 0) && (tmp[strlen(tmp)-1] != '\\') ) {
#else
    if (tmp != NULL && (strlen(tmp) > 0) && (tmp[strlen(tmp)-1] != '/') ) {
#endif
        len = strlen(tmp) + 1;
        if (len > mblk->info->dirmasksize){
            mblk->info->dirmasksize = len;
            mblk->info->dirmask = 
                (char*) realloc(mblk->info->dirmask, mblk->info->dirmasksize);
            assert(mblk->info->dirmask);
        }
        strcpy(mblk->info->dirmask, tmp);
    }
    else 
        tmp = NULL;
#ifdef _WIN32
    p = strrchr(mblk->info->dirmask,'\\');
#else
    p = strrchr(mblk->info->dirmask,'/');
#endif
    if (p) 
        *(p + 1) = '\0';
    else
        mblk->info->dirmask[0] = '\0';
    return tmp;
}


/*
 * Select a file from disk, or give a new filename
 */
char *psnd_savefilename(MBLOCK *mblk, char *title, char *mask)
{
    char *tmp, *p;
    int len, init = FALSE;

    len = 0;
    if (mblk->info->dirmask) 
        len += strlen(mblk->info->dirmask);
    else
        init = TRUE;
    len += strlen(mask) + 1;
    if (len > mblk->info->dirmasksize){
        mblk->info->dirmasksize = len;
        mblk->info->dirmask = 
            (char*) realloc(mblk->info->dirmask, mblk->info->dirmasksize);
        assert(mblk->info->dirmask);
        if (init)
            mblk->info->dirmask[0] = '\0';
    }
    strcat(mblk->info->dirmask, mask);
#ifdef _WIN32
    tmp = g_popup_saveas(mblk->info->win_id, title, mblk->info->dirmask) ;
    if (tmp != NULL && (strlen(tmp) > 0) && (tmp[strlen(tmp)-1] != '\\') ) {
#else
    tmp = g_popup_getfilename(mblk->info->win_id, title, mblk->info->dirmask) ;
    if (tmp != NULL && (strlen(tmp) > 0) && (tmp[strlen(tmp)-1] != '/') ) {
#endif
        len = strlen(tmp) + 1;
        if (len > mblk->info->dirmasksize){
            mblk->info->dirmasksize = len;
            mblk->info->dirmask = 
                (char*) realloc(mblk->info->dirmask, mblk->info->dirmasksize);
            assert(mblk->info->dirmask);
        }
        strcpy(mblk->info->dirmask, tmp);
    }
    else 
        tmp = NULL;
#ifdef _WIN32
    p = strrchr(mblk->info->dirmask,'\\');
#else
    p = strrchr(mblk->info->dirmask,'/');
#endif
    if (p) 
        *(p + 1) = '\0';
    else
        mblk->info->dirmask[0] = '\0';
    return tmp;
}

/*
 * Select a directory from disk
 */
char *psnd_getdirname(MBLOCK *mblk, char *title)
{
    char *tmp;

    tmp = g_popup_getdirname(mblk->info->win_id, title) ;
    if (tmp && mblk->info->dirmask)
        mblk->info->dirmask[0] = '\0';

    return tmp;
}


/*
 * Print size of parameter block, sizes and complex/real
 */
void filmes(MBLOCK *mblk, int ifile, int npar, int nsiz[], int complx[], int num)
{
    char cplx[] = "RC";
    int i;
    
    num = max(1,num);
    num = min(num,MAX_DIM);
    for (i=0;i<num;i++) {
        complx[i] = max(0,complx[i]);
        complx[i] = min(1,complx[i]);
    }

    psnd_printf(mblk, "  smx-file%3d: npar=%5d", ifile, npar);
    for (i=0;i<num;i++) 
        psnd_printf(mblk, " size(%1d)=%5d(%c)",i+1,nsiz[i],cplx[complx[i]]);
    psnd_printf(mblk, "\n");
}



/*
 * Close input file
 */
void psnd_close_file(MBLOCK *mblk)
{
    if (DAT->finfo[INFILE].fopen) {
        xx_close_infile(mblk, DAT);
        psnd_printf(mblk, "%s closed\n", DAT->finfo[INFILE].name);
    }
}


/*
 * Open a file for reading
 * 
 * argc = number of arguments in 'argv'
 * argv = arguments of the type "ge", "filename"
 */
int psnd_open(MBLOCK *mblk, int argc, char *argv[])
{
    int i;
    int ierr;
    char *filename;
    static int nsiz[MAX_DIM], icmplx[MAX_DIM];

    if (argc >= 2) 
        filename = argv[1];
    else
        filename = psnd_getfilename(mblk, "Open file","*");
    if (!filename) 
        return FALSE;
    /*
     * reset the parameter block
     */
    psnd_param_reset(mblk, mblk->info->block_id);
    /*
     * Clear old record undo/redo info 
     */
    psnd_init_undo(DAT);
    psnd_init_plane_undo(DAT);
    /*
     * Flag the contour buffer as obsolete
     */
    mblk->cpar_screen->mode = CONTOUR_NEW_FILE;

    if (xx_open_read(mblk,filename, nsiz, icmplx)==FALSE)
        return FALSE;
    /*
     * Copy sizes and complex/real info to parameters
     */
    for (i=0;i<MAX_DIM;i++) {
        PAR0(i)->nsiz   = nsiz[i];
        PAR0(i)->td     = nsiz[i];
        PAR0(i)->icmplx = icmplx[i];
    }
    /*
     * Print sizes of file
     */
    filmes(mblk,
           DAT->finfo[INFILE].ifile,
           DAT->npar,
           nsiz,
           icmplx,
           DAT->ityp);
    /*
     * Disable possible cropmode
     */
    psnd_set_cropmode(0, DAT);
    return TRUE;
}

/*
 * Set direction (access)
 * argc = number of arguments in 'argv'
 * argv = arguments of the type "dr", "1", "2" .. 'argc'
 * keycount = number of keys in 'keys'
 * keys = array of keys, 1,2,3 .. 'keycount'
 * if keycount > 0 then args are ignored
 * Return TRUE on success, FALSE otherwise
 */
int psnd_direction(MBLOCK *mblk, int argc, char *argv[], int keycount, int *keys)
{
    int i,j,ndim,magic,magickey;
    int access[MAX_DIM];
    int nsize[MAX_DIM];

    ndim = DAT->ityp;
    for (i=ndim;i<MAX_DIM;i++)
        DAT->access[i] = i+1;
    for (i=0;i<MAX_DIM;i++)
        access[i] = DAT->access[i];
    psnd_printf(mblk, "  %dD data\n", ndim);
    if (mblk->info->plot_mode > ndim)
        psnd_switch_plotdimension(mblk);
    /*
     * If we are running in foreground mode,
     * enable/disable 2D button on button bar
     */
    if (mblk->info->foreground) {
        if (ndim > 1)
            g_menu_enable_item(mblk->info->bar_id, PSND_CO, TRUE);
        else
            g_menu_enable_item(mblk->info->bar_id, PSND_CO, FALSE);
        psnd_refresh_phase_labels(mblk);
    }
    if (ndim > MAX_DIM || ndim < 1) {
        psnd_printf(mblk, "  Set Direction: data type incompatible\n");
        return FALSE;
    }
    /*
     * For 1D files, direction is always 1
     */
    else if (ndim == 1) {
        for (i=0;i<MAX_DIM;i++)
            access[i] = i+1;
    }
    else {
        /*
         * if keys are given, set access
         */
        if (keycount > 0) {
            for (i=0;i<ndim && i<keycount;i++) 
                access[i] = keys[i];
        }
        /*
         * If arguments are given, get keys and set acces
         */
        else if (argc > 1) {
            for (i=0;i<ndim && i<argc-1;i++) 
                access[i] = psnd_scan_integer(argv[i+1]);
        }
        /*
         * Ask user for access
         */
        else
            if (psnd_ivalin(mblk, " access", ndim, access)==0)
                return FALSE;
        /*
         * Check if all directions are in range
         */
        magic    = 0;
        magickey = 0;
        for (i=0;i<ndim;i++) {
            if (access[i] < 1 || access[i] > ndim) {
                psnd_printf(mblk, " Invalid directions:");
                for (j=0;j<ndim;j++)
                    psnd_printf(mblk, " %d", access[j]);
                psnd_printf(mblk, "\n");
                return FALSE;
            }
            magic    += (i+1) * (i+1);
            magickey += access[i] * access[i];
        }
        /*
         * All directions may occur once and only once
         * We use the unique key generated for n dimensions by
         * magickey = 1^2 + 2^2 + 3^2 + 4^2 .. n^2
         */
        if (magic != magickey) {
            int lastkey;
            /*
             * It is possible that only ndim-1 keys are given
             * In that case we can compute last key from the other ones
             */
            magickey -= access[ndim-1] * access[ndim-1];
            lastkey   = sqrt(magic - magickey);
            magickey += lastkey*lastkey;
            if (magic == magickey && lastkey > 0 && lastkey <= ndim) {
                access[ndim-1] = lastkey;
            } 
            else {
                psnd_printf(mblk, " Invalid directions:");
                for (j=0;j<ndim;j++)
                    psnd_printf(mblk, " %d", access[j]);
                psnd_printf(mblk, "\n");
                return FALSE;
            }
        }
    }

    /*
     * Shuffle the parameter blocks accordingly
     */
    mblk->info->dimension_id = max(0,access[0]-1);
    for (i=0;i<MAX_DIM;i++) {
        DAT->access[i]  = access[i];
        DAT->pars[i]    = PAR0(DAT->access[i]-1);
    }
    /*
     * For 3D,4D,5D files, print some values
     */
    if (ndim > 2) {
        psnd_printf(mblk, "  Direction x=%d y=%d", 
                        access[0],access[1]);
        for (i=2;i<ndim;i++)
            psnd_printf(mblk, " %d", access[i]);
        psnd_printf(mblk, "\n");
    }
    return TRUE;
}

/*
 * Write a record to the open outputfile
 */
int psnd_returnrec(MBLOCK *mblk, int flush)
{
    int i, ierr;
    int keys_tmp[MAX_DIM];
    int nsiz[MAX_DIM], icmplx[MAX_DIM];
    DBLOCK *dat = DAT;
    PBLOCK *par = PAR;
    NDFILE *ndfile;

    if (dat->ityp <= 0 || !mblk->info->version
        || (dat->ityp > MAX_DIM) || 
        (dat->finfo[INFILE].fopen == FALSE))
        return FALSE;

    if (par->nsiz != dat->isize) {
        psnd_printf(mblk, " Error: Record size in memory and on disk differ\n");
        return FALSE;
    }
    if (dat->finfo[INFILE].read_only) {
        psnd_printf(mblk, " Error: File is opened 'read-only'\n");
        return FALSE;
    }
    for (i=0;i<MAX_DIM;i++) {
        /*
         * If i==0 this is the processing direction,
         * thus key is ignored anyway
         */
        if (i<dat->ityp) 
            keys_tmp[i] = dat->pars[i]->key;
        else
            keys_tmp[i] = 1;
    }

    /*
     * Ok, we have all information.
     * return the record!
     */
    ndfile = (NDFILE*)dat->finfo[INFILE].ndfile;
    ierr = xx_write_row(mblk, dat->finfo[INFILE].ifile, 
                        ndfile,
                        dat->xreal,
                        dat->ximag,
                        dat->access, keys_tmp, par->nsiz,
                        par->icmplx);
    if (flush) {
        iond_flush(ndfile);
    }
    return (!ierr);
}

/*
 * RN, Read New record
 *
 * keycount = the number op items in 'keys'
 * keys     = offsets for other dimensions
 *   1d: keycount = 0
 *   2d: keycount = 1, the record number
 *   3d: keycount = 2, the record number and the plane, respectively
 *
 * Return TRUE if a new record has been loaded, FALSE otherwise
 */
int psnd_rn(MBLOCK *mblk, int keycount, int *keys, DBLOCK *dat)
{
    int i,ierr,  dim = mblk->info->dimension_id, offset, istat;
    int keysmd[MAX_DIM], icmplx[MAX_DIM];
    UNDOINFO rst;

    if (dat->ityp <= 0 || 
        (mblk->info->version && dat->ityp > MAX_DIM) || 
        (!mblk->info->version && dat->ityp > OLD3D_MAX_DIM) ||
        (dat->finfo[INFILE].fopen == FALSE))
        return FALSE;


    /*
     * Define the correct keys
     */
    for (i=0; i < MAX_DIM; i++) {
        icmplx[i] = dat->pars[i]->icmplx+1;
        if (i > 0 && i <= keycount)
            keysmd[i] = keys[i-1];
        else
            keysmd[i] = dat->pars[i]->key;
    }
    if (keycount == 0 && dat->ityp > 1)
        if (psnd_ivalin(mblk, " record numbers", dat->ityp-1, keysmd+1)==0)
            return FALSE;
    for (i=1;i<dat->ityp;i++)
        if ( keysmd[i] <= 0 || keysmd[i] > dat->pars[i]->nsiz * icmplx[i])
            return FALSE;
    for (i=1;i<dat->ityp;i++)
        dat->pars[i]->key = keysmd[i];
    for (i=dat->ityp;i<MAX_DIM;i++)
        dat->pars[i]->key = 1;

    /*
     * It read mode is not defined, set to complex or real
     * according to the current data type
     */
    if (dat->pars[0]->ipre == NOPRE) {
        if (dat->pars[0]->icmplx)
            dat->pars[0]->ipre = PREBOT;
        else
            dat->pars[0]->ipre = PREREA;
    }
    /*
     * If not defined, set the first and last data position
     */
    if (dat->pars[0]->irstrt <= 0) 
        dat->pars[0]->irstrt = 1;
    if (dat->pars[0]->irstop <= 0) 
        dat->pars[0]->irstop = dat->pars[0]->nsiz;
    if (dat->pars[0]->irstop < dat->pars[0]->irstrt)  
        return FALSE;
    /*
     * Store current record info in undo structure
     */
    for (i=0;i<MAX_DIM;i++) {
        rst.key[i] = dat->pars[i]->key;
        rst.par[i] = dat->pars[i];
    }
    psnd_push_undo(&rst, dat);

    dat->isspec = dat->pars[0]->isspec;
    psnd_set_datasize(mblk,dat->pars[0]->irstop - dat->pars[0]->irstrt + 1,
                       (mblk->info->plot_mode != PLOT_2D), dat);
    if (dat->pars[0]->dspshift != 0.0 && !dat->pars[0]->isspec)
        dat->pars[0]->dspflag = TRUE;

    xx_read_row(mblk,dat, dat->pars[0], dat->access, keysmd, dat->pars[0]->irstop);
    return TRUE;
}

/*
 * Just an other entry mode for psnd_rn
 */
int psnd_read_record(MBLOCK *mblk, int argc, char *argv[], DBLOCK *dat)
{
    int i,keycount=0;
    int keys[MAX_DIM];
    
    /*
     * Read keyvalues (if any) into the 'keys' array
     */
    for (i=2;argc >= i && i <= MAX_DIM;i++) {
        keycount++;
        keys[keycount-1] = psnd_scan_integer(argv[keycount]);
    }
    return psnd_rn(mblk, keycount, keys, dat);
}

/*
 * Nextrec gives the next record, but not necessarily in the
 * direction DR. This is to enable rows/columns in 2D mode
 *
 * incr = the value to add to, or substract from, the current key
 *        in the current direction. 'incr' can be 0
 */
void psnd_nextrec(MBLOCK *mblk, int verbose, int incr,
                   PBLOCK *pars[MAX_DIM], DBLOCK *dat)
{
    int i;
    int parstart,parstop;
    int access[MAX_DIM], keys[MAX_DIM];
    UNDOINFO rst;
            
    /*
     * File has not been opened
     */
    if (dat->ityp == 0 || dat->finfo[INFILE].fopen == FALSE) 
        return;

    /*
     * At least we need a 2D file
     */
    if (dat->ityp <= 1 ||  pars[1]->nsiz < 1) {
        g_bell();
        return;
    }

    pars[1]->key = min(pars[1]->key + incr, pars[1]->nsiz);
    pars[1]->key = max(1,pars[1]->key);
    if (verbose) {
        psnd_printf(mblk, "  T%d, Row %d", pars[0]->icrdir, pars[1]->key);
        for (i=2;i<dat->ityp;i++) {
            psnd_printf(mblk, ", Off %d", pars[i]->key);
        }
        psnd_printf(mblk, "\n");
    }
    parstart = max(1, pars[0]->irstrt);
    parstop  = pars[0]->irstop;
    if (parstop==0)
        parstop=pars[0]->nsiz;
    if (verbose) {
        /*
         * Push the current situation on the undo/redo stack
         */
        for (i=0;i<MAX_DIM;i++) {
            rst.key[i]	= pars[i]->key;
            rst.par[i]	= pars[i];
        }
        psnd_push_undo(&rst, dat);
    }
    /*
     * prepare to read
     */
    for (i=0;i<MAX_DIM;i++) {
        access[i] = pars[i]->icrdir;
        keys[i]   = pars[i]->key;
    }

    xx_read_row(mblk,dat,pars[0], access, keys, parstop);
    /*
     * Update current spectrum/fid flag
     */
    dat->isspec = pars[0]->isspec;
    if (pars[0]->dspshift != 0.0 && !pars[0]->isspec)
        pars[0]->dspflag = TRUE;
    /*
     * Update current size
     */
    psnd_set_datasize(mblk,parstop - parstart + 1, 
                       (mblk->info->plot_mode != PLOT_2D), dat);

}

/*
 * Read parameters from smx file
 */
int psnd_getparam(MBLOCK *mblk, int from_dr)
{

    if (!DAT->finfo[INFILE].fopen) {
        psnd_printf(mblk, " file not open\n");
        return FALSE;
    }

    xx_rdparm(mblk, from_dr);

    DAT->sw   = PAR0(DAT->access[0]-1)->swhold;
    if (mblk->info->foreground) 
        psnd_refresh_phase_labels(mblk);

    return TRUE;
}

/*
 * Write parameters to smx file
 */
void psnd_setparam(MBLOCK *mblk, int argc, char *argv[])
{
    int ierr;
    int dim,idir = mblk->info->dimension_id+1;
    int iss[2];

    if (!DAT->finfo[INFILE].fopen) {
        psnd_printf(mblk, " file error --- \n");
        return;
    }
    if (DAT->finfo[INFILE].read_only) {
        psnd_printf(mblk, " Error: File is opened 'read-only'\n");
        return;
    }

    if (argc >= 2) {
        idir = psnd_scan_integer(argv[1]);
        if (idir < 1 || idir > mblk->info->max_block)
            return;
    }
    else 
        if (!popup_spin(mblk->info->win_id, "Save Parameters",
            &idir, "Direction", 1, DAT->ityp, 1))
                return;
    
    dim = idir - 1;
    iss[0] = PAR0(dim)->isstrt;
    iss[1] = PAR0(dim)->isstop;
    if (PAR0(dim)->isstrt <= 0) 
        PAR0(dim)->isstrt = 1;

    if (PAR0(dim)->isstop <= PAR0(dim)->isstrt) 
        PAR0(dim)->isstop = DAT->isize;

    psnd_parstm(DAT->finfo[INFILE].file_type, DAT->xpar, DAT->nt0par,
                DAT->ntnpar, DAT->iaqdir, DAT->nextdr);

    psnd_parstt(DAT->xpar, idir, PAR0(dim), FALSE);

    xx_wrparm(mblk,DAT->finfo[INFILE].ifile,
              DAT->xpar,
              DAT);

    PAR0(dim)->isstrt = iss[0];
    PAR0(dim)->isstop = iss[1];
}

/*
 * Save the current calibration parameters in the parameter block
 * of the input file
 */
void psnd_update_calibration_param(MBLOCK *mblk)
{
    int i;
    PBLOCK par0;

    if (!DAT->finfo[INFILE].fopen) {
        psnd_printf(mblk, " file error --- \n");
        return;
    }
    if (DAT->finfo[INFILE].read_only) {
        psnd_printf(mblk, " Error: File is opened 'read-only'\n");
        return;
    }
    memset(&par0,0,sizeof(PBLOCK));
    for (i=0;i<DAT->ityp;i++) {
        psnd_pargtt(DAT->xpar, i+1, &par0);
        par0.xref      = PAR0(i)->xref;
        par0.aref      = PAR0(i)->aref;
        par0.bref      = PAR0(i)->bref;
        par0.swhold    = PAR0(i)->swhold;
        psnd_parstt(DAT->xpar, i+1, &par0, FALSE);
    }
    update_par_record((NDFILE *)DAT->finfo[INFILE].ndfile);
}

#ifdef _WIN32
char *towin32text(char *label)
{
    char *buf, *p;
    int i,j=0,len=0,count=0;

    if (label == NULL || label[0] == '\0')
        return NULL;
    p = label;
    while (*p) {
        if (*p == '\n')
            count++;
        len++;
        p++;
    }
    len += count;
    buf = (char*) malloc(len + 1);
    assert(buf);

    j=0;
    if (label[0] == '\n') 
        buf[j++] = '\r';
    buf[j++] = label[0];
    for (i=1;label[i] && j<len;i++) {
        if (label[i] == '\n' && label[i-1] != '\r')
            buf[j++] = '\r';
        buf[j++] = label[i];
    }
    buf[j] = '\0';
    free(label);
    return buf;
}
#endif


/*
 * Edit the parameter block directly on
 * raw param block xpar
 */
int psnd_edit_raw_param(MBLOCK *mblk)
{
    int i, id=0, dir, ok, cont_id;
    G_POPUP_CHILDINFO ci[5];
    PBLOCK *par;
    char *p;
    DBLOCK *dat = DAT;

    if (dat->ityp==0 || dat->xpar==NULL)
        return 0;
    if (DAT->finfo[INFILE].read_only) {
        psnd_printf(mblk, " Error: File is opened 'read-only'\n");
        return FALSE;
    }

    p=psnd_pargtt_text(dat->xpar, 1, dat->iaqdir);
    if (!p)
        return 0;
#ifdef _WIN32
    p = towin32text(p);
#endif
    cont_id = g_popup_container_open(mblk->info->win_id, "Edit Raw Parameters",
                                     G_POPUP_WAIT );
    g_popup_init_info(ci);
    ci->type          = G_CHILD_MULTILINETEXT;
    ci->id            = id;
    ci->item_count    = 20;
    ci->items_visible = 30;
    ci->title         = "Only values can be changed";
    ci->label         = p;
    ci->horizontal    = TRUE;
    g_popup_add_child(cont_id, ci);
    ok = g_popup_container_show(cont_id);
    if (ok) {
        p = (char*) realloc(p, strlen(ci->label)+1);
        strcpy(p,ci->label);
        psnd_parstt_text(dat->xpar, p);
    }
    free (p);
    return ok;

}



/*********************************************************************
 * Output
 */

void psnd_param_init(PBLOCK *par, int dir, int nsiz, int cmplx,
                       float sf, float sw, float dspshift)
{
    memset(par,0,sizeof(PBLOCK)); 
    par->icrdir   		= dir;
    par->key			= 1;
    par->nsiz			= nsiz;
    par->td			= nsiz;
    par->isspec			= FALSE;
    if (dspshift != 0.0)
        par->dspflag		= TRUE;
    else
        par->dspflag		= FALSE;
    par->dspshift		= dspshift;
    par->pxref			= 0.0;
    par->sfd   			= sf;
    par->swhold   		= sw;
    par->xref   		= nsiz; 
    par->aref   		= 1.0;
    par->bref   		= 1.0;
    if (cmplx)
        par->ipre   		= PREBOT;
    else
        par->ipre   		= PREREA;
    par->irstrt   		= 1;
    par->irstop   		= nsiz;
    par->nwindo   		= FALSE;
    par->iwindo   		= SNWIN;
    par->rsh   			= DEF_RSH;
    par->rlb   			= 0;
    par->gn   			= DEF_GN;		
    par->itm   			= 0;
    par->iwstrt   		= 0;
    par->iwstop   		= 0;
    par->nfft   		= FALSE;
    if (cmplx)
        par->ifft   		= FFTCX;
    else
        par->ifft   		= FFTREA;
    par->nzf   			= 0;
    par->ftscale		= DEF_FTSCALE;
    par->icstrt   		= 0;
    par->icstop   		= 0;
    par->iphase   		= NOPHA;      
    par->ahold   		= 0;	
    par->bhold   		= 0;
    par->ihold   		= 1;
    par->ipost                  = NOPST;
    par->irever   		= NOREV;
    par->nbase   		= FALSE;
    par->ibase   		= BASPL1;
    par->iterms   		= DEF_ITERMS;
    par->iterms2   		= 0;
    par->ibstrt   		= 0;
    par->ibstop   		= 0;
    par->ibwater		= 0;
    par->ibwidth   		= 0;
    par->ifilut   		= NEWFIL;
    par->isstrt   		= 0;
    par->isstop   		= 0;
    par->nlpc			= FALSE;
    par->lpc   			= 0;
    par->lpcmode		= 0;
    par->nfut   		= DEF_NFUT;
    par->npoles  		= DEF_NPOLES;
    par->mroot   		= FALSE;
    par->replace  		= FALSE;
    par->toler   		= DEF_TOLER;
    par->ngap1   		= 0;
    par->ngap2   		= 0;
    par->nstart  		= 0;
    par->nstop  		= 0;
    par->watwa  		= FALSE;
    par->iopt   		= DEF_IOPT;
    par->kc   			= DEF_KC;
    par->wshift 		= 0.0;
    par->hilbert 		= FALSE;
    par->waterfit		= FALSE;
    par->waterpos		= 0;
    par->waterwid		= 0;
}


static int open_outfile(MBLOCK *mblk, int count, char *names[MAX_DIM], PBLOCK *par, DBLOCK *dat);

/*
 * Open a file for writing
 */
int psnd_open_outfile(MBLOCK *mblk, int count, char *filenamelist[MAX_DIM+1], int mode, PBLOCK *par, DBLOCK *dat)
{
    char *filename = filenamelist[0];
    if (dat->finfo[OUTFILE].fopen > 0) {
        if (psnd_close_outfile(mblk, par, dat) != 0)
            return FALSE;
    }

    if (!filename) 
        filename = psnd_savefilename(mblk, "Open file","*");
    if (!filename) 
        return FALSE;
    strncpy(dat->finfo[OUTFILE].name,filename,PSND_STRLEN);
    dat->finfo[OUTFILE].name[PSND_STRLEN-1]='\0';
    /*
     * Indicate that the file is beeing opened, 
     * by making the open mode negative
     */
    dat->finfo[OUTFILE].fopen = -1 * mode;
    /*
     * If option is CREATEFILE we don not have to wait for 
     * the first processed record to know all the parameters.
     * Thus: open now!
     */
    if (mode == CREATEFILE) 
        return open_outfile(mblk, count-1, filenamelist+1, dat->pars[0], dat);
    return TRUE;
}

static int open_outfile(MBLOCK *mblk, int count, char *names[MAX_DIM], 
                        PBLOCK *par, DBLOCK *dat)
{
    int i;
    int ierr = 1;
    int nstore;
    char *filename = dat->finfo[OUTFILE].name;
    int idir;

    /*
     * if we already have an open outputfile, close it first
     */
    if (dat->finfo[OUTFILE].fopen > -1) {
        psnd_close_outfile(mblk,par, dat);
    }

    /*
     * Indicate that the file has been opened
     */
    dat->finfo[OUTFILE].fopen *= -1;

    if (par->isstrt < 1) 
        par->isstrt = 1;
    if (par->isstop <= par->isstrt) 
        par->isstop = dat->isize;

    par->irstop = min(par->nsiz, par->irstop);
    /*
     *.... output for info
     */
    if (dat->finfo[INFILE].fopen && dat->finfo[OUTFILE].fopen != CREATEFILE) {
        /*
         * If there is a valid input file ...
         */
        int imain = 0, idum1 = 1;
        char string[100];
    
        sprintf(string,"Main");
        psnd_la(mblk,0, 1, FALSE, dat);
        psnd_lp(mblk, par);

    }

    idir = par->icrdir - 1;
    /* dat->npar = MAXPAR;*/
    /*
     * probably no longer in use for modern io
     */
    dat->npar = iond_par_get_size(MAX_DIM);
    /*
     *...... normal output files
     *
     */
    if (dat->finfo[OUTFILE].fopen == NEWFILE) {
        if (par->ipost == NOPST)
            par->ipost = psnd_guess_storemode(par);
    }
    for (i=0;i<MAX_DIM;i++) {
        int n;
        if ((dat->finfo[OUTFILE].fopen != OLDFILE)
                       && PAR0(i)->isstop && PAR0(i)->isstrt) {
            dat->nsizo[i] = PAR0(i)->isstop - PAR0(i)->isstrt + 1;
        }
        else
            dat->nsizo[i] = PAR0(i)->nsiz;
        if (dat->finfo[OUTFILE].fopen == CREATEFILE) {
            PAR0(i)->nsiz = dat->nsizo[i];
            /*
             * With prepa scripts, store mode MUST be defined
             */
            if (i==0 && PAR0(i)->ipost == NOPST)
                psnd_printf(mblk, "Error: Store mode undefined in open output file\n");
            if (PAR0(i)->ipost == PSTBOT && dat->nsizo[i] > 1) 
                dat->cmplxo[i] = 1;
            else
                dat->cmplxo[i] = 0;
            PAR0(i)->icmplx = dat->cmplxo[i];
        }
        else if (dat->finfo[OUTFILE].fopen == NEWFILE) {
            if (PAR0(i)->ipost == NOPST) 
                dat->cmplxo[i] = PAR0(i)->icmplx;
            else if (PAR0(i)->ipost == PSTBOT) 
                dat->cmplxo[i] = 1;
            else
                dat->cmplxo[i] = 0;
        }
        else
            dat->cmplxo[i] = PAR0(i)->icmplx;

    }
    ierr = (!xx_open_write(mblk,filename, dat->nsizo, dat->cmplxo, 
                           (dat->finfo[OUTFILE].fopen != OLDFILE), 
                           count, names,
                           dat));

    if (ierr == 0 && dat->finfo[OUTFILE].fopen == CREATEFILE) {
        int typ3d, ioff,idir,nrec,idum,access[MAX_DIM];
        PBLOCK *par0;    
        float  *xxpar;

        typ3d=0;
        for (i=0;i<MAX_DIM;i++)
            if (dat->nsizo[i] > 1) 
                typ3d = i+1;
        access[0] = typ3d;
        for (i=1;i<MAX_DIM;i++) {
            if (i >= typ3d)
                access[i] = i+1;
            else
                access[i] = i;
        }
        par0 = (PBLOCK*) malloc(sizeof(PBLOCK));
        assert(par0);
        xxpar = (float*)((NDFILE *)dat->finfo[OUTFILE].ndfile)->parameter;

        psnd_parstm(dat->finfo[OUTFILE].file_type, xxpar, 50, 50, typ3d, typ3d);
        for (i=0;i<MAX_DIM;i++) {                       
            psnd_param_init(PAR0(i), i+1, dat->nsizo[i], dat->cmplxo[i],
                       PAR0(i)->sfd, PAR0(i)->swhold, PAR0(i)->dspshift);
            psnd_parstt(xxpar, i+1, PAR0(i), TRUE);
        }
        xx_wrparm(mblk,dat->finfo[OUTFILE].ifile, xxpar, dat);
        free(par0);
        for (i=0;i<MAX_DIM;i++) 
            dat->access[i] = access[i];

        mblk->info->dimension_id = max(0,typ3d-1);
        for (i=0;i<MAX_DIM;i++)
            dat->pars[i] = PAR0(dat->access[i]-1);
        dat->ityp = typ3d;
    }

    if (ierr)
        /*
         * On error close the file
         */
        dat->finfo[OUTFILE].fopen = 0;
    return !ierr;
}

/*
 * Write a record to the open outputfile
 */
int psnd_rw(MBLOCK *mblk, int keycount, int *keys, DBLOCK *dat)
{
    int i, ierr, isstart, isstop, complex;
    int keys_tmp[MAX_DIM];

    if (dat->finfo[OUTFILE].fopen == 0)
        return FALSE;
    if (dat->finfo[OUTFILE].fopen < 0) 
       if (!open_outfile(mblk, 0,NULL,dat->pars[0], dat))
           return FALSE;
    isstart = max(1, dat->pars[0]->isstrt);
    isstop  = dat->pars[0]->isstop;
    if (isstop == 0)
        isstop = dat->isize;
    if (dat->pars[0]->ipost == PSTBOT) 
        complex = 1;
    else
        complex = 0;

    for (i=0;i<MAX_DIM;i++) {
        int pos,osize;
        pos   = dat->access[i]-1;
        osize = dat->nsizo[pos] * (1+dat->cmplxo[pos]);

        /*
         * If i==0 this is the processing direction,
         * thus key is ignored anyway
         */
        if (i == 0) {
            keys_tmp[i] = dat->pars[i]->key;
            continue;
        }
        /*
         * If we do not have a compulsory key in keys[]
         * try to compute the correct key from the input file
         */
        if (i > keycount || keys[i-1] < 1) {
            /*
             * If the size of the ouput file < the size of the input file
             * then shift keys accordingly
             */
            if (dat->nsizo[pos] != dat->pars[i]->nsiz) {
                /*
                 * Correct for 'store start'
                 */
                keys_tmp[i] = dat->pars[i]->key - dat->pars[i]->isstrt+1;
                /*
                 * For complex files, if key is higher than real part
                 * it must be located in the imaginary part
                 */
                if (dat->cmplxo[pos] && keys_tmp[i] > dat->nsizo[pos]) 
                    keys_tmp[i] += dat->nsizo[pos]-dat->pars[i]->nsiz;
            }
            else 
                /*
                 * output key == input key
                 * this is normal procedure
                 */
                keys_tmp[i] = dat->pars[i]->key;
        }
        else if (dat->nsizo[pos] != dat->pars[i]->nsiz) {
                /*
                 * Correct for 'store start'
                 */
                keys_tmp[i] = keys[i-1] - dat->pars[i]->isstrt+1;
                /*
                 * For complex files, if key is higher than real part
                 * it must be located in the imaginary part
                 */
                if (dat->cmplxo[pos] && keys_tmp[i] > dat->nsizo[pos]) 
                    keys_tmp[i] += dat->nsizo[pos]-dat->pars[i]->nsiz;
        }
        else
            keys_tmp[i] = keys[i-1];
 
        /*
         * If key is out of range, abort
         */
        if (keys_tmp[i] < 1 || keys_tmp[i] > osize)
            return FALSE;
    }
    /*
     * Ok, we have all information.
     * write record!
     */
    ierr = xx_write_row(mblk,dat->finfo[OUTFILE].ifile, 
                        (NDFILE*)dat->finfo[OUTFILE].ndfile,
                        dat->xreal + isstart - 1,
                        dat->ximag + isstart - 1,
                        dat->access, keys_tmp, isstop - isstart + 1,
                        complex);


    return (!ierr);
}

/*
 * Close the current output file
 */
int psnd_close_outfile(MBLOCK *mblk, PBLOCK *par, DBLOCK *dat)
{
    int    nstore, nextdr, iaqdir, i;
    float  sw, *xpar, *xparhis;
    PBLOCK *par0;

    iaqdir = dat->iaqdir,
    nextdr = dat->nextdr;
    /*
     * No data written to file, so close is just NO FILE
     */
    if (dat->finfo[OUTFILE].fopen < 0) {
        dat->finfo[OUTFILE].fopen = FALSE;
        return FALSE;
    }
    if (dat->finfo[OUTFILE].fopen == 0)
        return FALSE;
    par0    = (PBLOCK*) calloc(1,sizeof(PBLOCK));
    assert(par0);

    if (dat->finfo[OUTFILE].fopen != CREATEFILE) 
        memcpy(par0,    par,         sizeof(PBLOCK));
    if (dat->xpar == NULL)
    {
        float *xxpar = (float*)((NDFILE *)dat->finfo[OUTFILE].ndfile)->parameter;

        xpar    = xx_parcopy(mblk, xxpar, dat->ityp);
        xparhis = xx_parcopy(mblk, xxpar, dat->ityp);
    }
    else {
        xpar    = xx_parcopy(mblk, dat->xpar, dat->ityp);
        xparhis = xx_parcopy(mblk, dat->xpar, dat->ityp);
    }
    if (dat->finfo[OUTFILE].fopen != CREATEFILE) {
        psnd_parstt(xpar,    par->icrdir, par, FALSE);
        psnd_parstt(xparhis, par->icrdir, par, FALSE);
    }
    psnd_pargtt(xpar,    par->icrdir, par0);

    if (dat->finfo[OUTFILE].fopen != CREATEFILE) {
        int nsiz;
        par0->isstrt = max(1, par->isstrt);
        par0->isstop = par->isstop;
        if (par0->isstop <= par0->isstrt + 1)
            par0->isstop = dat->isize;
        par0->bref -= par0->isstrt-1+dat->irc-1;
    }
    else 
        nextdr = iaqdir = par->icrdir;
    sw     = par0->swhold;
    par0->isstrt = 1;
    nstore = par0->isstop = dat->nsizo[par->icrdir-1];
    if (par0->nfft) {
        /*
         * Unused icstrt 
         */
        par0->icstrt = 1;
        if (par0->icstop <= par0->icstrt)
            par0->icstop = dat->isize;
        nextdr = par->icrdir-1;
        sw    *= (float)(par0->icstop - par0->icstrt)/(float)(dat->isize-1);
    }
    if (dat->finfo[OUTFILE].fopen == NEWFILE) {
        par0->isspec = dat->isspec;
        if (par->nfft) 
            sw *= (float)( nstore - 1 ) / (float)(par0->icstop - par0->icstrt);
        else
            sw *= (float)( nstore - 1 ) / (float)(par0->nsiz-1);
    }
    par0->swhold = sw;

    /*
     * Kludge
     */
    if (iaqdir == 0)
        iaqdir = dat->ityp;

    /*
     * Store this lot in xpar
     */
    psnd_parstm(dat->finfo[OUTFILE].file_type, xpar, dat->nt0par,dat->ntnpar,
                iaqdir, nextdr);
    /*
     *.... set new limits
     *.... irstrt = 1
     *.... irstop = nstore
     */
    if (dat->finfo[OUTFILE].fopen != CREATEFILE) {
        par0->irstrt = 1;
        par0->irstop = nstore;
        /*
         * Set read mode in the newly created file
         * equal to the write mode of the previous file
         */
        par0->ipre = par0->ipost;
        psnd_parstt(xpar, par0->icrdir, par0, FALSE);
    
        /*
         *.... copy and update processing parameters
         *.... on xparhis array for storage on history record
         */
        psnd_pargtt(xparhis, par->icrdir, par0);
        if (par0->iwindo > NOWIN) 
            par0->iwindo = NOWIN - par0->iwindo;
        if (par0->ifft > NOFFT) 
            par0->ifft = NOFFT - par0->ifft;
        if (par0->iphase > NOPHA) 
            par0->iphase = NOPHA - par0->iphase;
        if (par0->ibase > NOBAS) 
            par0->ibase = NOBAS - par0->ibase;
        psnd_parstt(xparhis, 1, par0, FALSE);
    }
    if (dat->finfo[OUTFILE].fopen == NEWFILE) {
        /*
         * Update sizes in all directions
         */
        for (i=0;i<dat->ityp;i++) {
            psnd_pargtt(xpar, i+1, par0);
            par0->iphase = FALSE; /* For now .. */
            if (PAR0(i)->isstop && PAR0(i)->isstrt) {
                par0->nsiz   = PAR0(i)->isstop - PAR0(i)->isstrt + 1;
                par0->irstop = par0->nsiz;
                par0->irstrt = 1;
                par0->isstop = 0;  /*PAR0(i)->isstop;*/
                par0->isstrt = 0;  /*PAR0(i)->isstrt;*/
                par0->ipre   = par0->ipost  = PAR0(i)->ipost;
                psnd_parstt(xpar, i+1, par0, FALSE);
            }
        }
    }
    /*
     * Update calibration in all directions
     */
    for (i=0;i<dat->ityp;i++) {
        psnd_pargtt(xpar, i+1, par0);
        par0->xref   = PAR0(i)->xref;
        par0->aref   = PAR0(i)->aref;
        par0->bref   = PAR0(i)->bref;
        par0->swhold    = PAR0(i)->swhold;
        psnd_parstt(xpar, i+1, par0, FALSE);
    }
    /*
     *.....store parameters, close file, update history
     */
    xx_close_outfile(mblk,dat, xpar, xparhis);
    free(par0);
    free(xpar);
    free(xparhis);
    return TRUE;
}

/****************************************
 * Array IO
 */
 
/*
 * Write one record to a new file. This can be a 1D file or one
 * record from a ND file.
 * The file is created, the record written, and the file closed in one call.
 *
 * array1   = real data to write
 * array2   = imaginary data to write
 * isize    = number of float to write per array
 * icomplex = if this flag is TRUE, a complex record is written, otherwise
 *            only array1 is written.
 * npar     = for old IO, size of par block on disk
 * par      = the corresponding parameter block
 * returns TRUE on success and FALSE otherwise
 */
int psnd_array_out(MBLOCK *mblk, char *filename, float *array1, float *array2, int isize, 
                    int icomplex, int npar, PBLOCK *par, DBLOCK *dat)
{
    int  i,dum1=1,dum2=1,ierr=0;
    float *p1, *p2, *parms;
    int iutfil = NOUT2;
    int cmplx[MAX_DIM] = { 0, 0, 0 };
    int size[MAX_DIM_EXTRA] = { 0, 0, 0 };
    int keys[MAX_DIM];
    int access[MAX_DIM];
    PBLOCK *pars;
    NDFILE *ndfile;
    char bruker[80];
    char script[100];
    int ndim;

    ndim = 1;
    for (i=1;i<MAX_DIM;i++) {
        cmplx[i] = 0;
        size[i]  = 1;
    }
    cmplx[0] = icomplex;
    size[0]  = isize;

    /*
     * Bruker record
     */
    strcpy(bruker, "Bruker record\n");
    size[BRK_SIZE_POS] = strlen(bruker)+1;
    /*
     * History record, save scripts
     */
    strcpy(script, "History record - Array dump\n");
    size[HIS_SIZE_POS] = strlen(script)+1;
    /*
     * Parameter record
     */
    size[PAR_SIZE_MAX]   = npar = iond_par_get_size(MAX_DIM);
    size[PAR_SIZE_POS]   = iond_par_get_size(ndim);
        
    /*
     * 1 nd buffer for writing 1d files
     */
    size[BUF_SIZE_NUM] = 4;
    ndfile = iond_open(filename, OPEN_NEW, &ndim, size, cmplx);
    if (ndfile == NULL)
        ierr = 1;
    if (ierr==0) {
        update_his_record(ndfile, script);
        update_brk_record(ndfile, bruker);
    }

    if (!ierr) {
        for (i=0;i<MAX_DIM;i++) {
            access[i] = i+1;
            keys[i]   = 1;
        }
        ierr = xx_write_row(mblk,iutfil, ndfile,
                            array1,
                            array2,
                            access, 
                            keys, 
                            isize,
                            FALSE);
    }
    else
        return !ierr;
    parms   = (float*)  calloc(sizeof(float), npar);
    assert(parms);
    pars   = (PBLOCK*)  malloc(sizeof(PBLOCK));
    assert(pars);
    memcpy(pars, par, sizeof(PBLOCK));
    
    pars->irstrt = 1;
    pars->irstop = isize;
    pars->isstrt = 1;
    pars->isstop = isize;
    if (mblk->info->version && icomplex)
        pars->ipre   = PREBOT;
    else
        pars->ipre   = PREREA;
    pars->isspec = dat->isspec;

    if (pars->nfft) 
        pars->swhold *= (float)( isize - 1 ) / (float)(pars->icstop - pars->icstrt);
    else
        pars->swhold *= (float)( isize - 1 ) / (float)(pars->nsiz-1);
    pars->bref += -dat->irc + 1;
    psnd_parstm(1, parms, 50, 50, 1, 1);

    psnd_parstt(parms, 1, pars, TRUE);

    if (!ierr) {
        float *xxpar = (float*)(ndfile->parameter);
        int nsize = ndfile->structure->par_size;
        memcpy(xxpar, parms, nsize);
        update_par_record(ndfile);
    }

    if (!ierr)
        filmes(mblk,
               iutfil,
               npar,
              &isize,
               cmplx,
               1);

    iond_close(ndfile, CLOSE_KEEP);

    free(pars);
    free(parms);
    return !ierr;    
}

/*
 * Read record from file 'filename'
 * Read no parameters
 *
 * If real record:    fill array 1
 * If complex record: fill array 1+2
 *
 * Return: 0 on error
 *         1 on real
 *         2 on complex
 */
int psnd_array_in(MBLOCK *mblk, char *filename, float *array1, float *array2,
                   int *newsize)
{
    int ierr=0,npar=1,isize=2,dum3=3,dum4=4,icomplex=0;
    int infil = NINP2;
    NDFILE *ndfile;
    int cmplx[MAX_DIM] = { 0, 0, 0 };
    int size[MAX_DIM_EXTRA] = { 0, 0, 0 };
    int ndim=1;
        
    *newsize = 0;
    size[PAR_SIZE_MAX] = iond_par_get_size(MAX_DIM);
    /*
     * 1 nd buffer for reading 1d files
     */
    size[BUF_SIZE_NUM] = 1;
    ndfile = iond_open(filename, OPEN_R, &ndim, size, cmplx);
    if (ndfile == NULL){
        ierr = 1;
        return 0;
    }
    isize    = size[0];
    icomplex = ndfile->complex[0];
 
    filmes(mblk,
               infil,
               npar,
               &isize,
               cmplx,
               1);
               
    if (isize > mblk->info->block_size)
            psnd_realloc_buffers(mblk, isize);

    ierr = xx_readrow_array(mblk,infil,ndfile,array1,array2,isize);

    iond_close(ndfile, CLOSE_KEEP);

    *newsize = isize;
    return icomplex + 1;
}    

void psnd_arrayread(MBLOCK *mblk, DBLOCK *dat)
{
    int ok = FALSE,i,newsize;
    float *p1, *p2;
    char *filename;
    
    p2 = dat->work1;
    i = psnd_popup_select_array(mblk->info->win_id, 1, "Read Array", "", TRUE);
    switch (i) {
    case 1: 
        p1 = dat->xreal;
        break;
    case 2: 
        p1 = dat->ximag;
        break;
    case 3: 
        p1 = dat->xbufr1;
        break;
    case 4: 
        p1 = dat->xbufr2;
        break;
    case 5: 
        p1 = dat->window;
        break;
    case 6: 
        p1 = dat->baseln;
        break;
    case 7: 
        p1 = dat->xreal;
        p2 = dat->ximag;
        break;
    default:
        return;
    }
    filename = psnd_getfilename(mblk, "Open file","*");
    if (filename) {
        ok = psnd_array_in(mblk,filename, p1, p2, &newsize);
        if (!ok)
            psnd_printf(mblk, " file not opened -- \n");
    }
}


void psnd_arraywrite(MBLOCK *mblk, int thisrecord, int argc, char *argv[], DBLOCK *dat)
{
    int  i,ok = FALSE, icomplex = FALSE;
    float *p1, *p2;
    char *filename;
    PBLOCK *par;

    if (PAR->icrdir!=DAT->access[0])
        /* 
         * Perpendicular 'column' data
         */
        par = dat->pars[1];
    else
        /* 
         * Normal 'row' data
         */
        par = dat->pars[0];

    p2 = dat->work1;
    if (thisrecord) {
        if (par->icmplx)
            i = 7;
        else
            i = 1;
    }
    else
        i = psnd_popup_select_array(mblk->info->win_id, 1, "Write Array", "", TRUE);
    switch (i) {
    case 1: 
        p1 = dat->xreal;
        break;
    case 2: 
        p1 = dat->ximag;
        break;
    case 3: 
        p1 = dat->xbufr1;
        break;
    case 4: 
        p1 = dat->xbufr2;
        break;
    case 5: 
        p1 = dat->window;
        break;
    case 6: 
        p1 = dat->baseln;
        break;
    case 7: 
        p1 = dat->xreal;
        p2 = dat->ximag;
        icomplex = TRUE;
        break;
    default:
        return;
    }
    if (argc >= 2)
        filename = argv[1];
    else
        filename = psnd_savefilename(mblk, "Open file","*");
    if (filename) {
        ok = psnd_array_out(mblk, filename, p1, p2, dat->isize, 
                             icomplex, dat->npar, par, dat);
        if (!ok) 
            psnd_printf(mblk, " file not opened -- \n");
    }
    
}

/*
 * List history info
 */
void psnd_hi(MBLOCK *mblk, int argc, char *argv[], int to_file)
{
    if (DAT->finfo[INFILE].fopen) {
        if (DAT->finfo[INFILE].ndfile) {
            char *filename = NULL;
            FILE *file;
            NDFILE *ndfile = (NDFILE *)DAT->finfo[INFILE].ndfile;

            if (DAT->finfo[INFILE].file_type == FILE_TYPE_OLD3D) {
                psnd_printf(mblk, "Not implemented for old I/O\n");
                return;
            }
            else {
                if (!ndfile->history) {
                    psnd_printf(mblk, "No history data\n");
                    return;
                }
                if (!to_file) {
                    psnd_puts(mblk, ndfile->history);
                    return;
                }
                if (argc >= 2)
                    filename = argv[1];
                else
                    filename = psnd_getfilename(mblk, "Script filename","*.au");
                if (filename) {
                    if ((file=fopen(filename,"w"))==NULL) {
                        psnd_printf(mblk, "Can not open %s\n",filename);
                        return;
                    }
                    fputs(ndfile->history,file);
                    fclose(file);
                }
            }
        }
    }
}

/*
 * List aquisition info
 */
void psnd_list_aquisition_parameters(MBLOCK *mblk)
{
    if (DAT->finfo[INFILE].fopen) {
        if (mblk->info->version && DAT->finfo[INFILE].ndfile) {
            NDFILE *ndfile = (NDFILE *)DAT->finfo[INFILE].ndfile;
            if (DAT->finfo[INFILE].file_type == FILE_TYPE_OLD3D) 
                psnd_printf(mblk, "Old-style 1/2/3D data: No ACQU data\n");
            else {
                if (ndfile->bruker)
                    psnd_puts(mblk,ndfile->bruker);
            }
        }
    }
}






