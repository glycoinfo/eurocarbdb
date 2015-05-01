#ifdef __STDC__
# define	P(s) s
#else
# define P(s) ()
#endif

int iora_open P((char *filename , int option , int *nrecs ));
int iora_close P((int filno ));
int iora_get_fileinfo P((int filno, int request_code, int *info));
int iora_lowrite P((int filno , int record_num , char *vect , int size ));
int iora_loread P((int filno , int record_num , char *vect , int *size ));
int iora_read P((int filno , int record_num , char *dest , int *n , int *type ));
int iora_write P((int filno , int record_num , char *source , int n , int type ));
int iora_test P((int filno ));
int iora_init P((void ));
int iora_new_filno P((void ));
int iora_set_f77unit P((int filno , int iunit ));
int iora_get_filno P((char *filename ));
int iora_buffer_save_all P((int filno));
int iora_buffer_save_record P((int filno, int record_num));
int iora_assert_map P((int filno, int record_num));

void rabuf_readcount P((void));
void rabuf_hitcount P((void));
int iora_buffer_set_ondisk P((int filno, int record_num, int flag));
int iora_buffer_get_ondisk P((int filno, int record_num));
int iora_buffer_get_size P((int filno, int record_num));
int iora_buffer_replace P((int filno, int record_num, char *vect, int size));
int iora_buffer_append P((int filno, int record_num, char *vect, int size));
int iora_buffer_copy P((int b_offset, char *dest, int *size));
int iora_buffer_flush P((int filno, int record_num));
int buffer_tree_kill_old P((int filno));
int RBcount_records P((int filno));
void RBtreelist P((int filno, int *list));
int iora_buffer_next P((int filno, int flag));

IORA_FILE *iora_alloc_file P((int filno ));
IORA_FILE *iora_get_fp P((int filno ));
void iora_dealloc_file P((int filno ));
int iora_read_header P((int filno ));
int iora_read_triton_header P((int filno ));
int os_endian_test P((void ));
int iora_write_header P((int filno ));
int cskip P((int filno , int size ));
int iora_set_rectype P((int filno , int rectype , float tresh_high , float tresh_level , float tresh_low ));
int iora_get_rectype P((int filno , float *tresh_high , float *tresh_level , float *tresh_low ));
int f4_cmprs P((int filno , float *f , int *nfl , char *vect , int *nbts , float *thi , float *tlo ));
int f4_decmprs P((int filno , char *vect , int *nbts , float *f , int *nfl , float *tle ));
int f2_cmprs P((int filno , float *f , int *nfl , char *vect , int *nbts , float *thi , float *tlo ));
int f2_decmprs P((int filno , char *vect , int *nbts , float *f , int *nfl , float *tle ));
int f3_cmprs P((int filno , float *f , int *nfl , char *vect , int *nbts , float *thi , float *tlo ));
int f3_decmprs P((int filno , char *vect , int *nbts , float *f , int *nfl , float *tle ));
int f4_f2 P((float *f , int *nfl , char *vect , int *nbts ));
int f2_f4 P((char *vect , int *nbts , float *f , int *nfl ));
int f4_f3 P((float *f , int *nfl , char *vect , int *nbts ));
int f3_f4 P((char *vect , int *nbts , float *f , int *nfl ));
int float_float2 P((float *f , float2 *f2 ));
int float_float3 P((float *f , float3 *f3 ));
int float_float4 P((float *f , float4 *f4 ));
int float2_float P((float2 *f2 , float *f ));
int float3_float P((float3 *f3 , float *f ));
int float4_float P((float4 *f4 , float *f ));
void tobe_swap4 P((void *i ));
void tole_swap4 P((void *i ));
void do_swap4 P((void *i ));
void do_swap2 P((void *i ));
void dummy_swap P((void *i ));
void iora_swap4_header P((int filno ));
void iora_set_endian P((int filno , char flag ));
void iora_swap4 P((int filno , void *array , int size ));
int list1_init P((int filno ));
int list1_append P((int filno , long offset ));
int list1_free_all P((int filno ));
int buffer_tree_init P((int filno ));
int store_record_offset P((int filno , int v , int offset ));
int get_record_offset P((int filno , int v ));
int buffer_tree_kill P((int filno ));
int remove_record_offset P((int filno , int v ));
static int split P((int filno , int v ));
static RBnode_t *RBrotate P((int v , RBnode_t *y ));
int buffer_read P((int filno , int record_num , long d_offset , char *dest , int *size ));
int create_ra_buffer P((int size ));
int alloc_in_buffer P((int size ));
int buffer_anal P((void ));
void treeprint P((int filno ));
void treeprintr P((int filno , RBnode_t *x ));
void printnode P((int filno , RBnode_t *x ));
int print_buffer_stat P((void ));

#undef P
