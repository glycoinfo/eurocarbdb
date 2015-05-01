#ifdef __STDC__
# define	P(s) s
#else
# define P(s) ()
#endif

NDFILE *iond_open P((char *filename , int option , int *ndim , int *size , int *complex ));
int iond_close P((NDFILE *ndfile , int option ));
int iond_flush P((NDFILE *ndfile ));
int iond_set_numbuffers(NDFILE *ndfile, int ndnumbuf ) ;
int iond_get_numbuffers(NDFILE *ndfile) ;
int iond_get_rectype P((NDFILE *ndfile , float *tresh_high , float *tresh_level , float *tresh_low ));
int iond_set_rectype P((NDFILE *ndfile , int rectype , float tresh_high , float tresh_level , float tresh_low ));
int iond_set_access P((NDFILE *ndfile , int *access ));
int iond_get_access P((NDFILE *ndfile , int *access ));
fcomplexp *iond_read_1d P((NDFILE *ndfile , int *vector_1d ));
int iond_write_1d P((NDFILE *ndfile , int *vector_1d , int size , float *real , float *imag ));
int smx_to_buffer_1d P((NDFILE *ndfile, int buf_inx ));
void make_smx_tables P((NDFILE *ndfile ));
int buffer_1d_to_smx P((NDFILE *ndfile, int buf_inx ));
int read_smx P((NDFILE *ndfile , float *buffer , int size , int record_num ));
int write_smx P((NDFILE *ndfile , float *buffer , int size , int record_num ));
int iond_calc_smx_sizes P((int ndim , int *size , int *complex , int *smx_size ));
int proc_str_record P((NDFILE *ndfile , OLD3D_STR_RECORD *str_record,
    int rec_type ));
int update_str_record P((NDFILE *ndfile , int par_size , int his_size , int brk_size ));
int update_par_record P((NDFILE *ndfile ));
int update_his_record P((NDFILE *ndfile , char *history ));
int update_brk_record P((NDFILE *ndfile , char *bruker ));
NDFILE *alloc_ndfile P((int parsize, int ndnumbuf ));
void free_ndfile P((NDFILE *ndfile ));
float *iond_read_2d P((NDFILE *ndfile , int x_axis , int y_axis , int *vector_2d ));
int iond_write_2d P((NDFILE *ndfile , int x_axis , int y_axis , int *vector_2d ));
int buffer_2d_to_smx P((NDFILE *ndfile ));
int smx_to_buffer_2d P((NDFILE *ndfile ));
short *iond_get_sorted_list P((NDFILE *ndfile , short *pp , int *ranges ));

#undef P
