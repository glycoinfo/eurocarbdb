/*
 * psnd_au_y.h
 */
 
extern int yyabort_by_user;
#define MAX_ACQU_FILES	5	/* Must be >= MAX_DIM */

#define IS_FLOAT	(1<<0)
#define IS_STRING	(1<<1)
#define IS_FLOAT_STRING	(1<<2)

#ifndef FALSE
#define FALSE	0
#define TRUE	1
#endif

typedef struct flist {
    int   size;
    int   num;
    float *list;
} FLIST_TYPE;

typedef struct rlist {
    int   size;
    int   num;
    int   *list;
} RLIST_TYPE;


typedef union mlist_union {
    float f;
    char *s;
} MLIST_UNION;

typedef struct mlist {
    int   	size;
    int   	num;
    int   	*type;
    MLIST_UNION *value;
} MLIST_TYPE;

#define UNDETERMINED	-1
#define ENDOFRANGE	-1
#define RANGENUM	2

#define RANGE_FIRST	0
#define RANGE_LAST	1
#define RANGE_STEP	2
#define RANGE_POS	3
#define RANGE_EXT	4
#define MAX_RANGE_SIZE  5
/*
 * linked list to store dynamically allocated strings
 */
typedef struct strbuf {
    char *s;
    struct strbuf *next;
} STRBUF_TYPE;

/* 
 * Data type for symbols.      
 */
typedef struct symrec
{
    char *name;  	/* name of symbol                     */
    int  type;    	/* type of symbol: either VAR,FNCT... */
    int  val_type;		/* The current type:
    					IS_FLOAT:  float only, in 'val_float'
                                        IS_STRING: string only, in 'value.str'
                                        IS_FLOAT_STRING: is float and also converted to
                                        	string in 'val_string'
                                 */
    float val_float;		/* The float representation of this symbol	*/
    char  val_string[40];	/* The float as string representation of this symbol	*/
    FILE  *file;		/* pointer to file handle	*/
    STRBUF_TYPE *sbt;
    union {
        char   *str;           	/* value of a STRING 		*/
        double (*fnctptr)();  	/* value of a FNCT		*/
        int    jmp;		/* jump address			*/
    } value;
} symrec;


typedef struct slist {
    int    		size;
    int    		num;
    symrec 		**list;
} SLIST_TYPE;


     
symrec *putsym (char *sym_name, int sym_type);
symrec *getsym (char *sym_name);

void init_table (void);
    	
typedef struct 
{
    char  name[4];	/* array name		*/
    float **data;	/* address of pointer to data	*/
    float **data2;	/* if array is complex, address of pointer to imaginary data	*/
    int   id;		/* the id number of the array	*/
    int   *len;		/* pointer to the size of the array	*/
} ARRAY_TYPE;

typedef struct
{
    char *name;
    int  id;
    ARRAY_TYPE *array_block;
} BLOCK_TYPE;

ARRAY_TYPE *current_array_block(void *para, int id);

/*
 * This is what the lexical analizer gives to the parser
 */
typedef struct gentype
{
    int type;			/* The type of record, NUM, STRING etc 	*/
    int line;			/* The line number in the script	*/
    int stackpos;		/* The position on the stack		*/
    char *scriptname;		/* Pointer to the name of the script	*/
    char *pp;			/* Pointer to the current token in script*/
    char *linestart;		/* Pointer to the current line in script*/
    int stm;                    /* The statement counter		*/
    union {
        float  		var;	/* number				*/
        char   		*str;	/* pointer to string			*/
        symrec 		*tptr;	/* pointer to symbol record 		*/
        ARRAY_TYPE  	*array;	/* pointer to array			*/
        BLOCK_TYPE  	*block;	/* pointer to block			*/
        FILE		*file;	/* pointer to FILE handle		*/
        FLIST_TYPE      *flist; /* pointer to list of floats		*/
        RLIST_TYPE      *rlist; /* pointer to list of ranges		*/
        MLIST_TYPE      *mlist; /* pointer to list of floats/strings	*/
        SLIST_TYPE      *slist; /* pointer to list of symbols		*/
        int    		jmp;	/* jump address				*/
        int    		btype;	/* baseline type			*/
        int    		ftype;	/* file type	      			*/
        int    		id;	/* id	      				*/
    } value;
    short *rps;			/* pointer to a sorted range		*/
    int range[MAX_RANGE_SIZE];	/* The values of the type RANGE
                   		   range[0] = first value
                                   range[1] = last value
                                   range[2] = increment
                                   range[3] = current value
                                   range[4] = index to extended range
                                 */
    struct gentype *self;	/* A pointer to the record itself.
                                   When copies are made, this value still points
                                   to the original record on stack
                                 */
} gentype;
    
char  *create_and_cat_string(char *s1, char *s2, int stm_count, 
                            STRBUF_TYPE **sbt, int list_size, int *list);
char  *num_to_string(float f);
void   yy_free(void *ptr);
static int get_argcount(void);
static char *get_argval(int count);
static char *get_argcount_string();
void   run_au_script(void *data, char *au_script, int argc, char *argv[]);

void  push_jump_stack(int v);
int   pop_jump_stack(void);
void  push_argcount_stack(void);
int   pop_argcount_stack(void);
gentype *peek_argcount_stack(int num);
void  add_arg_to_stack(gentype *v);
char  *get_string_from_var(symrec *tptr);
float get_float_from_var(symrec *tptr);
void  push_symbol_on_stack(char *symname);
void  pop_old_symbols_from_stack(int pos);
void  pop_new_symbols_from_stack(int pos);
void  push_new_symbols_on_stack(int pos);
FLIST_TYPE *new_float_list(void);
FLIST_TYPE *add_to_float_list(float value);
RLIST_TYPE *new_range_list(void);
RLIST_TYPE *add_to_range_list(int *range);
SLIST_TYPE *new_symbol_list(void);
SLIST_TYPE *add_to_symbol_list(symrec *symbol);
MLIST_TYPE *new_mixed_list(void);
MLIST_TYPE *add_to_mixed_list(int type, float value, char *string);
char *printf_mixed_list(char *format, MLIST_TYPE *mlist);
int  add_to_range_master_space(int pos, int append, int num, int *range);
int  *get_range_master_list(int pos);

/*
SLIST_TYPE *new_symbol_list();
SLIST_TYPE *add_to_symbol_list(SLIST_TYPE *slist, symrec *value);
void free_symbol_list(SLIST_TYPE *slist);
*/

void   au_array_shift(void *param, float *x, int n1, int n2, int n3);
short *au_build_sorted_range(void *param, int steps, int range[]);

float  au_get_spectral_width(void *param, int block_id, int dim);
void   au_set_spectral_width(void *param, int block_id, int dim, float f);
float  au_get_spectral_frequecy(void *param, int block_id, int dim);
void   au_set_spectral_frequecy(void *param, int block_id, int dim, float f);
int    au_get_size(void *param, int dim);
void   au_set_size(void *param, int dim, int siz);
int    au_get_dimensions(void *param, int dim);
void   au_set_dimensions(void *param, int dim, int siz);
int    au_get_timedomain(void *param, int block_id, int dim);
void   au_set_timedomain(void *param, int block_id, int dim, int siz);
float  au_get_reference_channel(void *param, int block_id, int dim, int code);
void   au_set_reference_channel(void *param, int block_id, int dim, float f, int code);
float  au_get_phase0(void *param, int block_id, int dim);
void   au_set_phase0(void *param, int block_id, int dim, float f);
float  au_get_phase1(void *param, int block_id, int dim);
void   au_set_phase1(void *param, int block_id, int dim, float f);
int    au_get_phase_pos(void *param, int block_id, int dim);
void   au_set_phase_pos(void *param, int block_id, int dim, int i);
void   au_do_phase_correction(void *param);
void   au_do_auto_phase_correction(void *param);
void   au_normalize_phase(void *param, int i0);
void   au_define_data_as_spectrum(void *param, int block_id, int dim, int def);
int    au_query_data_as_spectrum(void *param, int block_id, int dim);
void   au_set_dspshift(void *param, int block_id, int dim, float shift);
float  au_get_dspshift(void *param, int block_id, int dim);
void   au_set_ftscale(void *param, int block_id, int dim, float scale);
float  au_get_ftscale(void *param, int block_id, int dim);
void   au_do_ft(void *param, int i, int mode);
void   au_do_dft(void *param, int mode);
void   au_do_if(void *param, int mode);
void   au_set_shift(void *param, float f);
void   au_set_line_broadening(void *param, float f);
void   au_set_gaussian_fraction(void *param, float f);
void   au_set_points_for_trapezian_mul(void *param, int i);         
void   au_set_window_size(void *param, int start, int stop);
void   au_do_window(void *param, int id);
void   au_do_baseline_correction(void *param);
void   au_set_baseline_mode(void *param, int mode);
void   au_set_baseline_range(void *param, int start, int stop);
void   au_set_baseline_terms(void *param, int terms);
void   au_set_baseline_terms2(void *param, int terms);
int    au_get_baseline_terms(void *param);
void   au_set_baseline_water(void *param, int start, int stop);

int    au_do_watwa(void *param);
void   au_set_watwa_shift(void *param, float shift);
void   au_set_watwa_power(void *param, int cos_pow);
void   au_set_watwa_convolution_width(void *param, float w);

int    au_do_waterfit(void *param);
void   au_set_waterfit_pos(void *param, int pos);
void   au_set_waterfit_wid(void *param, int wid);

int    au_do_linear_prediction(void *param, char *mode, int num_roots);
void   au_set_lpc_predict_mode(void *param, int mode);
void   au_set_lpc_future_size(void *param, int s);
void   au_set_lpc_range(void *param, int start, int stop);
void   au_set_lpc_gaprange(void *param, int start, int stop);

int    au_get_file(void *param, char *name);
int    au_open_outputfile(void *param, int count, char *namelist[], int mode);
int    au_close_outfile(void *param);
void   au_set_outfile_truncation(void *param, int sizeof_float, 
                               int tresh_flag, float tresh_levels1,
                               float tresh_levels2, float tresh_levels3);
void   au_close_infile(void *param);
int    au_set_ranges_outfile(void *param, int count, int *range, int force);

int    au_read_next_record(void *param, int num, float *keys);
void   au_set_record_read_range(void *param, int start, int stop);
int    au_return_record_size_check(void *param);
int    au_return_record(void *param);
int    au_write_record(void *param, int num, float *keys);
int    au_write_smxarray(void *param, char *name, float *array1, float *array2, 
                        int start, int stop);
int    au_read_smxarray(void *param, char *name, float *array1, float *array2, 
                       int start, int stop);

void   au_set_record_write_range(void *param, int start, int stop);

int    au_set_direction(void *param, int count, float arg[]);
void   au_set_postmode(void *param, int block_id, int dim, int mode);
int    au_get_postmode(void *param, int block_id, int dim);
void   au_set_premode(void *param, int block_id, int dim, int mode);
int    au_get_premode(void *param, int block_id, int dim);


int    au_spline_read(void *param, char *name);
int    au_spline_calc(void *param);

void   au_do_hilbert_transform(void *param);
void   au_select_block(void *param, int id);
void   au_do_reverse(float *f, int start, int stop);
void   au_transfer_parameters(void *param, int block1, int block2);

void   au_array_average(void *param);
void   au_array_power(void *param);
int    au_get_full_range(void *param, int);
int    au_get_range_start(void *param, int);
int    au_get_range_stop(void *param, int);

void   au_init_param(void *param);


int    au_plot_1d(void *param, char *outputfile, float, float, float, float );
int    au_plot_2d(void *param, char *outputfile, float, float, float, float );
int    au_write_plot_parameters(void *param, char *outputfile);
int    au_read_plot_parameters(void *param, char *inputfile);
int    au_set_plot_parameters(void *param, char *inputstring);
int    au_set_contour_mode(void *param, int );
int    au_get_plotmode(void *param);

int    au_integrate_read(void *param, char *filename);
float  au_integrate_2d_peak(void *param, int peak_id);
int    au_integrate_2d_get_numpeaks(void *param);
void   au_integral_set_offset_correction(void *param, float doit);
float  au_integrate_1d(void *param, float x1, float x2);
float  au_integrate_2d(void *param, float x1, float x2, float y1, float y2, int *ierr);
float  au_rmsnoise_1d(void *param, float x1, float x2);
float  au_rmsnoise_2d(void *param, float x1, float x2, float y1, float y2, int *ierr);
int    au_peakpick_1d(void *param, float x1, float x2, float y1, float y2, float *data, 
                     int istart, int istop);

float  au_unitconvert(void *param, float xx, float direction, char *what);


int    au_puts(void *param, char *buf);
char   *au_check_openmode(char *mode);
char   *au_check_filename(char *name);

int    au_3to4(float *in, float *out, int start_in, int stop_in, 
               int start_out, int stop_out, int mode);
void   au_negstep(float *data, int start, int stop, int stepsize, 
                 int index);
int    au_unpack_byte(float *f, int index);
int    au_unpack_int(float *f);
float  au_unpack_float(float *f1, float *f2);
char   *au_unpack8(float *f);
char   *au_unpack6(float *f);
char   *au_unpack4(float *f);

extern char  *(*au_scanfile)(FILE *file, char *pattern1, 
               int skip, char *pattern2, int isregexp);
extern char  *(*au_scanbinfile)(FILE *file, char *pattern);
extern int   (*au_os_big_endian)(void);
extern void  (*au_xxrotate)(float *x, int n1, int n2, int n3);
extern void  (*au_xxi2f)(float *x, int n1, int n2);
extern void  (*au_xxf2i)(float *x, int n1, int n2);
extern void  (*au_xxswap2)(float *x, int n1, int n2);
extern void  (*au_xxswap4)(float *x, int n1, int n2);
extern char  *(*au_xxf2ascii)(float *x, int n1, int n2);
extern void  (*au_xxc2ri)(float *co, float *re, float *ir, int n1, int n2);
extern void  (*au_xxc2ir)(float *co, float *re, float *ir, int n1, int n2);

extern int   (*au_scan_integer)(char *value);
extern float (*au_scan_float)(char *value);





