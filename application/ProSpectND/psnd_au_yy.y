
     %{
#include <errno.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <math.h> 
#include <assert.h>
#include <ctype.h>
#include <sys/stat.h>
#include <unistd.h>
#include "psnd_au_y.h"  
#include "psnd_defs.h"  

#define YYSTYPE gentype
static symrec global_status;
static int outfile_open_status;
#define YYPARSE_PARAM param
#define YYLEX_PARAM param
int yylex (YYSTYPE *lvalp, void *);
int yyerror (char*);
int jump(int jmp);
static void free_symbol_list(void);
static char *au_strtok(char *line, char *delim);
#define au_min(a,b)        (((a) < (b)) ? (a) : (b))

     %}
     

     %token NUM        		/* Simple  number   		*/
     %token VAR FNCT FNCT2  	/* Variable and Function 	*/
     %token ARGV ARGC MAXFUNC MINFUNC
     %token ARRAY ARRAY2 SIZEOF AV PS
     %token BLOCK BYTEORDER EXEC PUTENV GETENV INITPARAM
     %token STRING STRINGINDEX STRINGLASTINDEX UPCASE LOWCASE TRIM
     %token STRTOK
     %token PRINT PRINTLN PRINTF FPRINTF SPRINTF FGETS
     %token PL CP CO PLOTPAR RPLOTPAR WPLOTPAR
     %token SWAP2 SWAP4 I2F F2I F2ASCII C2RI C2IR ROL ROR RV LS RS 
     %token UNPACK4 UNPACK6 UNPACK8 UNPACKI UNPACKF UNPACKB NEGSTEP
     %token UNPACK0123 UNPACK3210 UNPACK0321 UNPACK1230
     %token READ OPEN CLOSE WRITE SEEK TELL SCAN SCANEXP SCANBIN
     %token STDIN STDOUT STDERR STAT CHDIR GETCWD
     %token SF SW XR AREF BREF SIZEN SI TD FT DFT IFT FTSCALE 
     %token PA PB I0 PK APK PZ PN WATERFIT
     %token SN SQ KS HN HM CD EM GM TM WB BC WATWA LPC HSVD
     %token POLY TABL COSINE SINE SINCOS POLYW TABLW COSINEW 
     %token SINEW SINCOSW SPLINEW
     %token RN RW RTR ROPEN RCLOSE WREOPEN WOPEN WCLOSE CREATE DR SM RM RD WR
     %token OTRUNC FLOAT2 FLOAT3 SPLINE CALC HT TP EXIT CONTINUE BREAK 
     %token DIM ISSPEC DSPSHIFT
     %token INTEGRAL OFFSET RMSNOISE PEAKPICK1D UCONV


     %nonassoc IF THEN ELSE ELSEIF ENDIF WHILE DO DONE 
     %nonassoc FOR IN SORTED INSORTED 
     %nonassoc FUNCTION FUNCTION_DEF END RETURN LOCAL

   
     %right '=' ADD SUB MUL DIV 
     %nonassoc DOTDOT
     %left OR
     %left AND
     %nonassoc EQ NE
     %nonassoc '<' '>' GE LE
     %left '-' '+' 
     %left '*' '/'
     %left '!' NEG     /* Negation--unary minus */
     %right '^'        /* Exponentiation        */
     %left '$' '?'
     %left '(' ')' '[' ']'  

     %pure_parser 
     /* Grammar follows */
     
     %%

    input:   
            /* empty */
        |   input full_stmt
        ;
     
    full_stmt:
            '\n'
        |   stmt '\n'
        |   error '\n'
            { 
                /*yyerrok;*/
                if (1) YYABORT;
            }
        ;
    /*
     * expression
     */
    exp:
            braced_exp                
        |   exp '+' exp        
                 { $$.value.var = $1.value.var + $3.value.var;                    }
        |   exp '-' exp        
                 { $$.value.var = $1.value.var - $3.value.var;                    }
        |   exp '*' exp        
                 { $$.value.var = $1.value.var * $3.value.var;                    }
        |   exp '/' exp        
            {  
                if ($3.value.var == 0) {
                    yyerror("Error: Divide by zero");
                    YYABORT;
                }
                else
                    $$.value.var = $1.value.var / $3.value.var;  
            }
        |   '-' exp  %prec NEG 
            { 
                $$.value.var = -$2.value.var;
            }
        |   exp '^' exp        
            {
                errno = 0; 
                $$.value.var = pow ($1.value.var, $3.value.var); 
                if (errno) {
                    yyerror( "Error in pow()");
                    YYABORT;
                }
            }
        |   exp '>' exp        
            { 
                 $$.value.var = ($1.value.var > $3.value.var);
            }
        |   string '>' string       
            { 
                 $$.value.var = (strcmp($1.value.str, $3.value.str) > 0);
            }
        |   exp '<' exp        
            { 
                $$.value.var = ($1.value.var < $3.value.var);
            }
        |   string '<' string       
            { 
                $$.value.var = (strcmp($1.value.str, $3.value.str) < 0);
            }
        |   exp GE exp        
            { 
                $$.value.var = ($1.value.var >= $3.value.var);
            }
        |   string GE string
            { 
                $$.value.var = (strcmp($1.value.str, $3.value.str) >= 0);
            }
        |   exp LE exp        
            { 
                $$.value.var = ($1.value.var <= $3.value.var);
            }
        |   string LE string
            { 
                $$.value.var = (strcmp($1.value.str, $3.value.str) <= 0);
            }
        |   exp EQ exp        
            { 
                $$.value.var = ($1.value.var == $3.value.var);
            }
        |   string EQ string        
            { 
                $$.value.var = (strcmp($1.value.str, $3.value.str) == 0);
            }
        |   exp NE exp        
            { 
                $$.value.var = ($1.value.var != $3.value.var);
            }
        |   string NE string        
            { 
                $$.value.var = (strcmp($1.value.str, $3.value.str) != 0);
            }
        |   '!' exp       
            { 
                $$.value.var = (! $2.value.var);
            }
        |   exp AND exp        
            { 
                $$.value.var = ($1.value.var && $3.value.var);
            }
        |   exp OR exp        
            { 
                $$.value.var = ($1.value.var || $3.value.var);
            }
        ;


    braced_exp:
            braced_exp_minus_array
        |   array1 range_one
            {
                $$.type = NUM;
                $$.value.var = *(*($1.value.array->data) + $2.range[RANGE_FIRST]);
            }
        ;
            
    braced_exp_minus_array: 
            NUM
            {
                $$ = $1;
            }
        |   ARGC
            {
                $$.type = NUM;
                $$.value.var = (float) get_argcount();
            }
        |   MAXFUNC '(' exp ',' float_list ')'
            {
                float result = 0.0;
                int i;
                result = $3.value.var;
                for (i=0;i<$5.value.flist->num;i++) {
                    if ($5.value.flist->list[i] > result)
                        result = $5.value.flist->list[i];
                }
                $$.value.var = result; 
                $$.type = NUM; 
            }
        |   MAXFUNC '(' any_subarray1 ')'
            {
                float *f, result = 0.0;
                int i,start,stop;
                start = $3.range[RANGE_FIRST];
                stop  = $3.range[RANGE_LAST];
                f     = *($3.value.array->data);
                result = f[start];
                for (i=start;i<=stop;i++) {
                    if (f[i] > result)
                        result = f[i];
                }
                $$.value.var = result; 
                $$.type = NUM; 
            }
        |   MINFUNC '(' exp ',' float_list ')'
            {
                float result = 0.0;
                int i;
                result = $3.value.var;
                for (i=0;i<$5.value.flist->num;i++) {
                    if ($5.value.flist->list[i] < result)
                        result = $5.value.flist->list[i];
                }
                $$.value.var = result; 
                $$.type = NUM; 
            }
        |   MINFUNC '(' any_subarray1 ')'
            {
                float *f, result = 0.0;
                int i,start,stop;
                start = $3.range[RANGE_FIRST];
                stop  = $3.range[RANGE_LAST];
                f     = *($3.value.array->data);
                result = f[start];
                for (i=start;i<=stop;i++) {
                    if (f[i] < result)
                        result = f[i];
                }
                $$.value.var = result; 
                $$.type = NUM; 
            }
        |   FNCT '(' exp ')'   
            { 
                errno = 0;
                $$.value.var = (*($1.value.tptr->value.fnctptr))($3.value.var); 
                $$.type = NUM; 
                if (errno) {
                    yyerror("Error in function call");
                    YYABORT;
                }
            }
        |   FNCT2 '(' exp ',' exp ')'   
            { 
                errno = 0;
                $$.value.var = (*($1.value.tptr->value.fnctptr))
                               ($3.value.var, $5.value.var); 
                $$.type = NUM; 
                if (errno) {
                    yyerror("Error in function call");
                    YYABORT;
                }
            }
        |   '(' exp ')'
            {
                $$ = $2;
            }
        |   '$' STRING  
            { 
                float f = 0.0;
                if ($2.value.str)
                    f = au_scan_float($2.value.str);
                $$.value.var = f;
                $$.type = NUM;
            }
        |   '$' '(' string ')'
            { 
                float f = 0.0;
                if ($3.value.str)
                    f = au_scan_float($3.value.str);
                $$.value.var = f;
                $$.type = NUM;
            }
        |   STRINGINDEX '(' string ',' string ')'  
            { 
                char *p = strstr($3.value.str, $5.value.str);
                if (p == NULL)
                    $$.value.var = (float) -1;
                else
                    $$.value.var = (float) (p - $3.value.str) + 1;
                $$.type = NUM;
            }
        |   STRINGLASTINDEX '(' string ',' string ')'  
            { 
                char *p , *q = NULL, *str = $3.value.str;
                do {
                    p = q;
                    q = strstr(str, $5.value.str);
                    if (q)
                        str = q + strlen($5.value.str);
                }
                while (q != NULL);
                
                if (p == NULL)
                    $$.value.var = (float) -1;
                else
                    $$.value.var = (float) (p - $3.value.str) + 1;
                $$.type = NUM;
            }
        |   '$'  VAR  
            {
                if ( $2.value.tptr->val_type == 0 ) {
                    yyerror( "Variable not initialized");
                    YYABORT;
                }
                $$.value.var = get_float_from_var($2.value.tptr);
                $$.type = NUM;
            }
        |   '$' NUM
            {
                gentype *g = peek_argcount_stack((int)$2.value.var);
                if (g == NULL || 
                       ((g->type != STRING) && 
                        (g->type != NUM) && 
                        (g->type != VAR)))
                    $$.value.var =  0; 
                else if (g->type == STRING) {
                    float f = 0.0;
                    if (g->value.str)
                        f = au_scan_float(g->value.str);
                    $$.value.var = f;  
                } 
                else if (g->type == VAR)
                    $$.value.var = get_float_from_var(g->value.tptr);  
                else if (g->type == NUM)
                    $$.value.var = g->value.var;
                $$.type =  NUM; 
            }
        |   '?' nmr_var_block_dim
            { 
                switch ($2.value.id) {
                    case PSND_SF:
                        $$.value.var = 
                            au_get_spectral_frequecy(param, $2.range[RANGE_FIRST], $2.range[RANGE_LAST]); 
                        break;
                    case PSND_SW:
                        $$.value.var = 
                            au_get_spectral_width(param, $2.range[RANGE_FIRST], $2.range[RANGE_LAST]); 
                        break;
                    case PSND_TD:
                    case PSND_SI:
                        $$.value.var = (float)
                            au_get_timedomain(param,$2.range[RANGE_FIRST], $2.range[RANGE_LAST]); 
                        break;
                    case PSND_XR:
                        $$.value.var = 
                            au_get_reference_channel(param,$2.range[RANGE_FIRST], $2.range[RANGE_LAST],0); 
                        break;
                    case PSND_AREF:
                        $$.value.var = 
                            au_get_reference_channel(param,$2.range[RANGE_FIRST], $2.range[RANGE_LAST],1); 
                        break;
                    case PSND_BREF:
                        $$.value.var = 
                            au_get_reference_channel(param,$2.range[RANGE_FIRST], $2.range[RANGE_LAST],2); 
                        break;
                    case PSND_PA:
                        $$.value.var = 
                            au_get_phase0(param,$2.range[RANGE_FIRST], $2.range[RANGE_LAST]); 
                        break;
                    case PSND_PB:
                        $$.value.var = 
                            au_get_phase1(param,$2.range[RANGE_FIRST], $2.range[RANGE_LAST]); 
                        break;
                    case PSND_I0:
                        $$.value.var = (float)
                            au_get_phase_pos(param,$2.range[RANGE_FIRST], $2.range[RANGE_LAST]); 
                        break;
                    case PSND_SM:
                        {
                        int mode =
                            au_get_postmode(param, $2.range[RANGE_FIRST], $2.range[RANGE_LAST]); 
                        mode = au_get_postmode(param, $2.range[RANGE_FIRST], $2.range[RANGE_LAST]);
                        $$.value.var = 1;
                        if (mode == PSTREA)
                            $$.value.var = 1;
                        else if (mode == PSTBOT)
                            $$.value.var = 2; 
                        }
                        break;
                    case PSND_RM:
                        {
                        int mode =
                            au_get_premode(param, $2.range[RANGE_FIRST], $2.range[RANGE_LAST]); 
                        mode = au_get_premode(param, $2.range[RANGE_FIRST], $2.range[RANGE_LAST]);
                        $$.value.var = 1;
                        if (mode == PREREA)
                            $$.value.var = 1;
                        else if (mode == PREBOT)
                            $$.value.var = 2; 
                        else if (mode == PREIMA)
                            $$.value.var = 3; 
                        }
                        break;
                    case PSND_SPEC:
                        $$.value.var = (float)
                            au_query_data_as_spectrum(param,$2.range[RANGE_FIRST], $2.range[RANGE_LAST]); 
                        break;
                    case PSND_DSPSHIFT:
                        $$.value.var = (float)
                            au_get_dspshift(param,$2.range[RANGE_FIRST], $2.range[RANGE_LAST]); 
                        break;
                    case PSND_FTSCALE:
                        $$.value.var = 
                            au_get_ftscale(param,$2.range[RANGE_FIRST], $2.range[RANGE_LAST]); 
                        break;
                }
                $$.type = NUM; 
            }
        |   '?' si
            { 
                $$.value.var = au_get_size(param,$2.value.id); 
                $$.type = NUM; 
            }
        |   '?' dim
            { 
                $$.value.var = au_get_dimensions(param,$2.value.id); 
                $$.type = NUM; 
            }
        |   '?' BLOCK
            { 
                $$.value.var = (float) $2.value.block->id;
                $$.type = NUM; 
            }
        |   '?' BYTEORDER
            { 
                $$.value.var = (float) au_os_big_endian();
                $$.type = NUM; 
            }
        |   '?' CO
            { 
                $$.value.var = (float) au_get_plotmode(param);
                $$.type = NUM; 
            }
        |   I2F braced_exp_minus_array
            {
                float f = $2.value.var;
                au_xxi2f(&f, 1, 1);
                $$.value.var = f;
                $$.type = NUM; 
            }
        |   F2I braced_exp_minus_array
            {
                float f = $2.value.var;
                au_xxf2i(&f, 1, 1);
                $$.value.var = f;
                $$.type = NUM; 
            }
        |   SWAP4 braced_exp_minus_array
            {
                float f = $2.value.var;
                au_xxswap4(&f, 1, 1);
                $$.value.var = f;
                $$.type = NUM; 
            }
        |   SWAP2 braced_exp_minus_array
            {
                float f = $2.value.var;
                au_xxswap2(&f, 1, 1);
                $$.value.var = f;
                $$.type = NUM; 
            }
        |   exec_stm
        |   SIZEOF '(' string ')'
            {
                $$.value.var = (float) strlen($3.value.str);
                $$.type = NUM; 
            }
        |   SIZEOF '(' any_subarray ')'
            {
                $$.value.var = (float) ($3.range[RANGE_LAST] - $3.range[RANGE_FIRST] + 1);
                $$.type = NUM; 
            }
        |   UNPACKI '(' exp ')'
            {
                $$.value.var = (float) au_unpack_int(&($3.value.var));
                $$.type = NUM; 
            }
        |   UNPACKF '(' exp ',' exp ')'
            {
                $$.value.var = au_unpack_float(&($3.value.var),&($5.value.var));
                $$.type = NUM; 
            }
        |   UNPACKB '(' exp ',' exp ')'
            {
                $$.value.var = (float) au_unpack_byte(&($3.value.var),$5.value.var);
                $$.type = NUM; 
            }
        |   '$' user_function
            {
                $$.value.var = get_float_from_var(&global_status);
                $$.type = NUM; 
            }
        |   INTEGRAL '(' exp ',' exp ')'
            {
                $$.value.var = au_integrate_1d(param, $3.value.var, $5.value.var);
                $$.type = NUM; 
            }
        |   INTEGRAL '(' exp ',' exp  ',' exp  ',' exp ')'
            {
                int ierr;
                $$.value.var = au_integrate_2d(param,
                                               $3.value.var, $5.value.var,
                                               $7.value.var, $9.value.var,
                                               &ierr);
                if ( ierr) {
                    yyerror("Integral - not 2D file or file not opened");
                    YYABORT;
                }
                $$.type = NUM; 
            }
        |   INTEGRAL '(' exp ')'
            {
                $$.value.var = au_integrate_2d_peak(param, $3.value.var);
                $$.type = NUM; 
            }
        |   INTEGRAL '(' ')'
            {
                $$.value.var = au_integrate_2d_get_numpeaks(param);
                $$.type = NUM; 
            }
        |   INTEGRAL '(' string ')'
            {
                $$.value.var = au_integrate_read(param, $3.value.str);
                $$.type = NUM; 
            }
        |   RMSNOISE '(' exp ',' exp ')'
            {
                $$.value.var = au_rmsnoise_1d(param,
                                              $3.value.var, $5.value.var);
                $$.type = NUM; 
            }
        |   RMSNOISE '(' exp ',' exp  ',' exp  ',' exp ')'
            {
                int ierr;
                $$.value.var = au_rmsnoise_2d(param,
                                              $3.value.var, $5.value.var,
                                              $7.value.var, $9.value.var,
                                              &ierr);
                if ( ierr) {
                    yyerror("Rmsnoise - not 2D file or file not opened");
                    YYABORT;
                }
                $$.type = NUM; 
            }
        |   PEAKPICK1D '(' exp ',' exp ',' exp ',' any_subarray1 ')'
            {
                $$.value.var = au_peakpick_1d(param,
                                              $3.value.var, 
                                              $5.value.var,
                                              $7.value.var,
                                              0.0, 
                                              *($9.value.array->data),
                                              $9.range[RANGE_FIRST]+1, 
                                              $9.range[RANGE_LAST]+1);
                $$.type = NUM; 
            }
        |   PEAKPICK1D '(' exp ',' exp ',' exp ',' exp ',' any_subarray1 ')'
            {
                $$.value.var = au_peakpick_1d(param,
                                              $3.value.var, 
                                              $5.value.var,
                                              $7.value.var, 
                                              $9.value.var, 
                                              *($11.value.array->data),
                                              $11.range[RANGE_FIRST]+1, 
                                              $11.range[RANGE_LAST]+1);
                $$.type = NUM; 
            }
        |   UCONV '(' exp ',' string ')'
            {
                $$.value.var = au_unitconvert(param,
                                              $3.value.var, 
                                              -1,
                                              $5.value.str);
                $$.type = NUM; 
            }
        |   UCONV '(' exp ',' string ',' exp ')'
            {
                $$.value.var = au_unitconvert(param,
                                              $3.value.var, 
                                              $7.value.var,
                                              $5.value.str);
                $$.type = NUM; 
            }
        ;

    exec_stm:
            EXEC '(' string ')'
            {
                int i = system($3.value.str);
                $$.value.var = (float) i;
                $$.type = NUM; 
            }
        ;


    si:
            SI
            { 
                $$.value.id = -1; 
            }
        |   BLOCK ':' SI
            {
                $$.value.id = $1.value.block->id;
            }
        ;


    dim:
            DIM
            { 
                $$.value.id = -1; 
            }
        |   BLOCK ':' DIM
            {
                $$.value.id = $1.value.block->id;
            }
        ;


    nmr_var_block_dim:
            nmr_var
            { 
                $$.range[RANGE_FIRST] = -1; 
                $$.range[RANGE_LAST] = -1; 
                $$.value.id = $1.value.id;
            }
        |   BLOCK ':' nmr_var
            {
                $$.range[RANGE_FIRST] = $1.value.block->id;
                $$.range[RANGE_LAST] = -1; 
                $$.value.id = $3.value.id;
            }
        |   nmr_var ':' NUM
            { 
                $$.range[RANGE_FIRST] = -1; 
                $$.range[RANGE_LAST] = (int) $3.value.var; 
                $$.value.id = $1.value.id;
            }
        |   BLOCK ':' nmr_var ':' NUM
            { 
                $$.range[RANGE_FIRST] = $1.value.block->id;
                $$.range[RANGE_LAST] = (int) $5.value.var; 
                $$.value.id = $3.value.id;
            }
        ;
        
    nmr_var:
            SF
            {
                $$.value.id = PSND_SF;
            }
        |   SW
            {
                $$.value.id = PSND_SW;
            }
        |   TD
            {
                $$.value.id = PSND_TD;
            }
        |   SIZEN
            {
                $$.value.id = PSND_SI;
            }
        |   XR
            {
                $$.value.id = PSND_XR;
            }
        |   AREF
            {
                $$.value.id = PSND_AREF;
            }
        |   BREF
            {
                $$.value.id = PSND_BREF;
            }
        |   PA
            {
                $$.value.id = PSND_PA;
            }
        |   PB
            {
                $$.value.id = PSND_PB;
            }
        |   I0
            {
                $$.value.id = PSND_I0;
            }
        |   SM
            {
                $$.value.id = PSND_SM;
            }
        |   RM
            {
                $$.value.id = PSND_RM;
            }
        |   ISSPEC
            {
                $$.value.id = PSND_SPEC;
            }
        |   DSPSHIFT
            {
                $$.value.id = PSND_DSPSHIFT;
            }
        |   FTSCALE
            {
                $$.value.id = PSND_FTSCALE;
            }
        ;
        

    string:   
            STRING
            { 
                $$ = $1;  
            }
        |   string full_fragmented_range
            {
                if ($2.range[RANGE_EXT]) {
                    int *list = get_range_master_list($2.range[RANGE_EXT]);
                    $$.value.str =
                           create_and_cat_string($1.value.str, 
                                NULL, $2.stm, NULL,
                                list[0],list+1);
                }
                else
                    $$.value.str = 
                       create_and_cat_string($1.value.str, 
                            NULL, $2.stm, NULL,
                            2, $2.range); 
                if ( $$.value.str == NULL) {
                    yyerror("Out of temp string space");
                    YYABORT;
                }
                $$.type = STRING; 
            }
        |   string '+' string
            { 
                $$.value.str = 
                       create_and_cat_string($1.value.str, 
                                 $3.value.str, $2.stm, NULL,0,0); 
                if ( $$.value.str == NULL) {
                    yyerror("Out of temp string space");
                    YYABORT;
                }
                $$.type = STRING; 
            }
        |   TRIM '(' string ')'
            { 
                int i;
                char *s;
                $$.value.str = 
                       create_and_cat_string(NULL, 
                                 $3.value.str, $2.stm, NULL,0,0); 
                if ( $$.value.str == NULL) {
                    yyerror("Out of temp string space");
                    YYABORT;
                }
                s = $$.value.str;
                i=strlen(s)-1;
                while ((s[i]==' ' || s[i]=='\r'  ||  s[i]=='\n' 
                                              ||  s[i]=='\t') && i>=0)
                    s[i--]='\0';
                $$.type = STRING; 
            }
        |   UPCASE '(' string ')'
            { 
                int i,len;
                $$.value.str = 
                       create_and_cat_string(NULL, 
                                 $3.value.str, $2.stm, NULL,0,0); 
                if ( $$.value.str == NULL) {
                    yyerror("Out of temp string space");
                    YYABORT;
                }
                len = strlen($$.value.str);
                for (i=0;i<len;i++)
                    $$.value.str[i] = toupper($$.value.str[i]);
                $$.type = STRING; 
            }
        |   LOWCASE '(' string ')'
            { 
                int i,len;
                $$.value.str = 
                       create_and_cat_string(NULL, 
                                 $3.value.str, $2.stm, NULL,0,0); 
                if ( $$.value.str == NULL) {
                    yyerror("Out of temp string space");
                    YYABORT;
                }
                len = strlen($$.value.str);
                for (i=0;i<len;i++)
                    $$.value.str[i] = tolower($$.value.str[i]);
                $$.type = STRING; 
            }
        |   '%' '(' exp ')'
            { 
                $$.value.str = 
                       create_and_cat_string(num_to_string($3.value.var), 
                                         NULL, $2.stm, NULL,0,0); 
                if ( $$.value.str == NULL) {
                    yyerror("Out of temp string space");
                    YYABORT;
                }
                $$.type = STRING; 
            }
        |   '%' '(' array ')'
            { 
                $$.value.str = 
                       create_and_cat_string($3.value.array->name, 
                                    NULL, $2.stm, NULL,0,0); 
                if ( $$.value.str == NULL) {
                    yyerror("Out of temp string space");
                    YYABORT;
                }
                $$.type = STRING; 
            }
        |   '%' '(' string ')'
            { 
                $$ = $3; 
            }
        |   '(' string ')'        
            { 
                $$ = $2;
            }
        |   '%' VAR                
            { 
                if ( $2.value.tptr->val_type == 0 ) {
                    yyerror( "Variable not initialized");
                    YYABORT;
                }
                $$.value.str = get_string_from_var($2.value.tptr);
                $$.type = STRING; 
            }
        |   GETENV '(' string ')'
            {
                char empty[] = "", *p = getenv($3.value.str);
                if (p == NULL)
                    p = empty;
                $$.value.str = 
                       create_and_cat_string(p, 
                                    NULL, $2.stm, NULL,0,0); 
                if ( $$.value.str == NULL) {
                    yyerror("Out of temp string space");
                    YYABORT;
                }
                $$.type = STRING; 
            }
        |   UNPACK8 '(' exp ')'
            {
                char *p = au_unpack8(&($3.value.var));
                $$.value.str = 
                       create_and_cat_string(p, 
                                    NULL, $2.stm, NULL,0,0); 
                if ( $$.value.str == NULL) {
                    yyerror("Out of temp string space");
                    YYABORT;
                }
                $$.type = STRING; 
            }
        |   UNPACK6 '(' exp ')'
            {
                char *p = au_unpack6(&($3.value.var));
                $$.value.str = 
                       create_and_cat_string(p, 
                                    NULL, $2.stm, NULL,0,0); 
                if ( $$.value.str == NULL) {
                    yyerror("Out of temp string space");
                    YYABORT;
                }
                $$.type = STRING; 
            }
        |   UNPACK4 '(' exp ')'
            {
                char *p = au_unpack4(&($3.value.var));
                $$.value.str = 
                       create_and_cat_string(p, 
                                    NULL, $2.stm, NULL,0,0); 
                if ( $$.value.str == NULL) {
                    yyerror("Out of temp string space");
                    YYABORT;
                }
                $$.type = STRING; 
            }
        |   F2ASCII '(' any_subarray1 ')'
            {
                char *p = au_xxf2ascii(*($3.value.array->data), 
                                        $3.range[RANGE_FIRST]+1, 
                                        $3.range[RANGE_LAST]+1);
                $$.value.str = 
                       create_and_cat_string(p, 
                                    NULL, $2.stm, NULL,0,0); 
                if ( $$.value.str == NULL) {
                    yyerror("Out of temp string space");
                    YYABORT;
                }
                $$.type = STRING; 
            }
        |   F2ASCII '(' braced_exp_minus_array ')'
            {
                char *p = au_xxf2ascii(&($3.value.var), 1, 1);
                $$.value.str = 
                       create_and_cat_string(p, 
                                    NULL, $2.stm, NULL,0,0); 
                if ( $$.value.str == NULL) {
                    yyerror("Out of temp string space");
                    YYABORT;
                }
                $$.type = STRING; 
            }
        |   ARGV '(' exp ')'
            {
                $$.value.str = get_argval((int)$3.value.var);   
                $$.type = STRING; 
            }
        |   '%' NUM
            {
                gentype *g = peek_argcount_stack((int)$2.value.var);
                if (g == NULL || 
                       ((g->type != STRING) && 
                        (g->type != NUM) && 
                        (g->type != VAR)))
                    $$.value.str =  create_and_cat_string("", 
                                 "", $1.stm, NULL,0,0); 
                else if (g->type == STRING)
                    $$.value.str = g->value.str;   
                else if (g->type == VAR)
                    $$.value.str = get_string_from_var(g->value.tptr);  
                else if (g->type == NUM)
                    $$.value.str = 
                       create_and_cat_string(num_to_string(g->value.var), 
                                         NULL, $1.stm, NULL,0,0); 
                $$.type = STRING; 
            }
        |   '%' user_function
            {
                $$.value.str = get_string_from_var(&global_status);
                $$.type = STRING; 
            }
        |   SPRINTF '(' string ',' mixed_list ')'
            {
                char *s = printf_mixed_list($3.value.str,$5.value.mlist);
                $$.value.str = 
                    create_and_cat_string(s,NULL, $1.stm, NULL,0,0); 
                $$.type = STRING; 
            }
        |   FGETS '(' filehandle ')'
            {
                char *s = NULL, *s2 = NULL;
                int len = 0;
                if ($3.value.file != NULL) {
                    char buf[PSND_STRLEN];
                    s = fgets(buf,PSND_STRLEN,$3.value.file);
                    while (s) {
                        s = create_and_cat_string(s, s2, $1.stm, NULL,0,0);
                        len = strlen(s);
                        if (len && s[len-1] != '\n') {
                            s2 = fgets(buf,PSND_STRLEN,$3.value.file);
                            if (s2 == NULL)
                                break;
                        }
                        else
                            break;
                    }
                }
                if (s == NULL)
                    s = create_and_cat_string(s, NULL, $1.stm, NULL,0,0);
                $$.value.str = s;
                $$.type = STRING; 
            }
        ;

    full_size_range:
            full_range
            {
                $$ = $1;
                if ($1.range[RANGE_FIRST] == UNDETERMINED)
                    $$.range[RANGE_FIRST] = 0;
                else
                    $$.range[RANGE_FIRST] = $1.range[RANGE_FIRST];
                if ($1.range[RANGE_LAST] == UNDETERMINED)
                    $$.range[RANGE_LAST] = au_get_size(param,-1) - 1;
                else
                    $$.range[RANGE_LAST] = $1.range[RANGE_LAST];
                $$.range[RANGE_STEP] = $1.range[RANGE_STEP];
                $$.range[RANGE_POS] = $$.range[RANGE_FIRST];
                $$.range[RANGE_EXT] = $1.range[RANGE_EXT];
            }
        ;
            
    full_size_fragmented_range:
            range_fragmented
            {
                $$ = $1;
                if ($1.range[RANGE_FIRST] == UNDETERMINED)
                    $$.range[RANGE_FIRST] = 0;
                else
                    $$.range[RANGE_FIRST] = $1.range[RANGE_FIRST];
                if ($1.range[RANGE_LAST] == UNDETERMINED)
                    $$.range[RANGE_LAST] = au_get_size(param,-1) - 1;
                else
                    $$.range[RANGE_LAST] = $1.range[RANGE_LAST];
                $$.range[RANGE_STEP] = $1.range[RANGE_STEP];
                $$.range[RANGE_POS] = $$.range[RANGE_FIRST];
                $$.range[RANGE_EXT] = $1.range[RANGE_EXT];
                if ($1.range[RANGE_EXT]>0) {
                    int *list = get_range_master_list($1.range[RANGE_EXT]);
                    list[1] = $$.range[RANGE_FIRST];
                    list[list[0]] = $$.range[RANGE_LAST];
                }
            }
        ;

    range_insides:
            range_dotdot
        |   range_dotdot_e
        |   range_e_dotdot
        |   range_e_dotdot_e
        ;
    
    range_left_insides:
            range_dotdot_e
        |   range_continue_insides
        ;
        
    range_continue_insides:
            range_e_dotdot_e
        |   exp
            { 
                $$.range[RANGE_FIRST] = (int) $1.value.var - 1;
                $$.range[RANGE_LAST] = (int) $1.value.var - 1;
                $$.range[RANGE_STEP] = 1;
                $$.range[RANGE_POS] = $$.range[RANGE_FIRST];
                $$.type = DOTDOT; 
            }
        ;
    
    range_dotdot:
            DOTDOT
            {
                $$.range[RANGE_FIRST] = UNDETERMINED;
                $$.range[RANGE_LAST] = UNDETERMINED;
                $$.range[RANGE_STEP] = 1;
                $$.range[RANGE_POS] = 0;
                $$.type = DOTDOT;
            }
        ;

    range_dotdot_e:
            DOTDOT exp
            {
                $$.range[RANGE_FIRST] = UNDETERMINED;
                $$.range[RANGE_LAST] = (int) $2.value.var - 1;
                $$.range[RANGE_STEP] = 1;
                $$.range[RANGE_POS] = 0;
                $$.type = DOTDOT; 
            }
        ;

    range_e_dotdot:
            exp DOTDOT
            {
                $$.range[RANGE_FIRST] = (int) $1.value.var - 1;
                $$.range[RANGE_LAST] = UNDETERMINED;
                $$.range[RANGE_STEP] = 1;
                $$.range[RANGE_POS] = $$.range[RANGE_FIRST];
                $$.type = DOTDOT; 
            }
        ;

    range_e_dotdot_e:
            exp DOTDOT exp
            { 
                $$.range[RANGE_FIRST] = (int) $1.value.var - 1;
                $$.range[RANGE_LAST] = (int) $3.value.var - 1;
                if ($$.range[RANGE_FIRST] > $$.range[RANGE_LAST]) {
                    yyerror("Error in range");
                    YYABORT;
                }
                $$.range[RANGE_STEP] = 1;
                $$.range[RANGE_POS] = $$.range[RANGE_FIRST];
                $$.type = DOTDOT; 
            }
        ;


    ranges_inside_list:
            ranges_inside_list ',' range_continue_insides
            {
                $$.range[RANGE_FIRST] = $1.range[RANGE_FIRST];
                $$.range[RANGE_LAST]  = $3.range[RANGE_LAST];
                $$.range[RANGE_STEP]  = $1.range[RANGE_STEP];
                $$.range[RANGE_POS]   = $1.range[RANGE_POS];
                $$.range[RANGE_EXT]   = $1.range[RANGE_EXT];
                $$.type = DOTDOT;
                
                if ($1.range[RANGE_LAST] < 0 ||
                    $1.range[RANGE_LAST] > $3.range[RANGE_FIRST]) {
                    yyerror("Error in range");
                    YYABORT;
                }
                add_to_range_master_space($$.range[RANGE_EXT], TRUE, 2, $3.range);
            }
        |   range_left_insides ',' range_continue_insides
            {
                int range[4];
                $$.type = DOTDOT;
                range[0] = $1.range[RANGE_FIRST];
                range[1] = $1.range[RANGE_LAST];
                range[2] = $3.range[RANGE_FIRST];
                range[3] = $3.range[RANGE_LAST];
                if (range[1] < 0 || range[2] < 0 || range[1] >= range[2]) {
                    yyerror("Error in range");
                    YYABORT;
                }
                $$.range[RANGE_FIRST] = range[0];
                $$.range[RANGE_LAST]  = range[3];
                $$.range[RANGE_STEP]  = $1.range[RANGE_STEP];
                $$.range[RANGE_POS]   = $1.range[RANGE_POS];                
                $$.range[RANGE_EXT]   = $2.self->range[RANGE_EXT];
                if ($$.range[RANGE_EXT] == 0) {
                    $2.self->range[RANGE_EXT] = add_to_range_master_space(-1, FALSE, 4, range);
                    $$.range[RANGE_EXT]   = $2.self->range[RANGE_EXT];
                }
                else
                    add_to_range_master_space($$.range[RANGE_EXT], FALSE, 4, range);
            }
        ;

    full_range:
            range_multi
        |   range_one
        ;

    full_fragmented_range:
            range_fragmented
        |   full_range
        ;

    range_fragmented:
            '[' ranges_inside_list ']'
            {
                $$.range[RANGE_FIRST] = $2.range[RANGE_FIRST];
                $$.range[RANGE_LAST]  = $2.range[RANGE_LAST];
                $$.range[RANGE_STEP]  = $2.range[RANGE_STEP];
                $$.range[RANGE_POS]   = $2.range[RANGE_POS];
                $$.range[RANGE_EXT]   = $2.range[RANGE_EXT];
                $$.type = DOTDOT;
            }
        |   '[' range_left_insides ',' range_e_dotdot ']'
            {
                int range[4];
                $$.type = DOTDOT;
                range[0] = $2.range[RANGE_FIRST];
                range[1] = $2.range[RANGE_LAST];
                range[2] = $4.range[RANGE_FIRST];
                range[3] = $4.range[RANGE_LAST];
                if (range[1] < 0 || range[2] < 0 || range[1] >= range[2]) {
                    yyerror("Error in range");
                    YYABORT;
                }
                $$.range[RANGE_FIRST] = range[0];
                $$.range[RANGE_LAST]  = range[3];
                $$.range[RANGE_STEP]  = $2.range[RANGE_STEP];
                $$.range[RANGE_POS]   = $2.range[RANGE_POS];                
                $$.range[RANGE_EXT]   = $3.self->range[RANGE_EXT];
                if ($$.range[RANGE_EXT] == 0) {
                    $3.self->range[RANGE_EXT] = add_to_range_master_space(-1, FALSE, 4, range);
                    $$.range[RANGE_EXT]   = $3.self->range[RANGE_EXT];
                }
                else
                    add_to_range_master_space($$.range[RANGE_EXT], FALSE, 4, range);
            }
        |   '[' ranges_inside_list ',' range_e_dotdot ']'
            {
                $$.range[RANGE_FIRST] = $2.range[RANGE_FIRST];
                $$.range[RANGE_LAST]  = $4.range[RANGE_LAST];
                $$.range[RANGE_STEP]  = $2.range[RANGE_STEP];
                $$.range[RANGE_POS]   = $2.range[RANGE_POS];
                $$.range[RANGE_EXT]   = $2.range[RANGE_EXT];
                $$.type = DOTDOT;
                
                if ($2.range[RANGE_LAST] < 0 ||
                    $2.range[RANGE_LAST] > $4.range[RANGE_FIRST]) {
                    yyerror( "Error in range");
                    YYABORT;
                }
                add_to_range_master_space($$.range[RANGE_EXT], TRUE, 2, $4.range);
            }
        ;

    range_multi:
            '[' range_insides ']'
            {
                $$.range[RANGE_FIRST] = $2.range[RANGE_FIRST];
                $$.range[RANGE_LAST]  = $2.range[RANGE_LAST];
                $$.range[RANGE_STEP]  = $2.range[RANGE_STEP];
                $$.range[RANGE_POS]   = $2.range[RANGE_POS];
                $$.range[RANGE_EXT]   = $2.range[RANGE_EXT];
                $$.type = DOTDOT;
            }
        ;


    range_one:
            '[' exp ']'
            { 
                $$.range[RANGE_FIRST] = (int) $2.value.var - 1;
                $$.range[RANGE_LAST] = (int) $2.value.var - 1;
                $$.range[RANGE_STEP] = 1;
                $$.range[RANGE_POS] = $$.range[RANGE_FIRST];
                $$.type = DOTDOT; 
            }
        ;

    fragmented_range_list:
            full_fragmented_range
            {
                $$.value.rlist = new_range_list();
                add_to_range_list($1.range);
            }
        |   range_list ','  full_fragmented_range
            {
                $$.value.rlist = $1.value.rlist;
                add_to_range_list($3.range);
            }
        ;

    range_list:
            full_range
            {
                $$.value.rlist = new_range_list();
                add_to_range_list($1.range);
            }
        |   range_list ','  full_range
            {
                $$.value.rlist = $1.value.rlist;
                add_to_range_list($3.range);
            }
        ;

    array:
            array1
        |   array2
        ;

    array1:
            ARRAY
            {
                /*
                 * Array must point to current block, so
                 * remap to be sure
                 */
                $$.value.array = current_array_block(param,$1.value.array->id);
            }
        |   BLOCK ':' ARRAY
            {
                $$.type = ARRAY; 
                $$.value.array = $1.value.block->array_block
                       + $3.value.array->id; 
            }
        ;

    array2:
            ARRAY2
            {
                /*
                 * Array must point to current block, so
                 * remap to be sure
                 */
                $$.value.array = current_array_block(param,$1.value.array->id);
            }
        |   BLOCK ':' ARRAY2
            {
                $$.type = ARRAY2; 
                $$.value.array = $1.value.block->array_block + 
                    $3.value.array->id; 
            }
        ;

    any_subarray:
            subarray1
        |   subarray2
        |   subarray1_range
        |   subarray2_range
        ;

    any_subarray_range:
            subarray1_range
        |   subarray2_range
        ;

    any_array:
            subarray1
        |   subarray2
        ;

    any_subarray1:
            subarray1
        |   subarray1_range
        ;

    any_fragmented_subarray:
            any_subarray
        |   subarray_fragmented_range1
        |   subarray_fragmented_range2
        ;

    subarray1:
            array1
            {
                $$ = $1; 
                $$.range[RANGE_FIRST] = 0;
                $$.range[RANGE_LAST] = *($1.value.array->len) - 1;
                $$.range[RANGE_STEP] = 1;
            }
        ;

    subarray2:
            array2
            {
                $$ = $1; 
                $$.range[RANGE_FIRST] = 0;
                $$.range[RANGE_LAST] = *($1.value.array->len) - 1;
                $$.range[RANGE_STEP] = 1;
            }
        ;

    subarray1_range:
            array1 full_size_range
            {
                $$ = $1;
                $$.range[RANGE_FIRST] = $2.range[RANGE_FIRST];
                $$.range[RANGE_LAST] = $2.range[RANGE_LAST];
                $$.range[RANGE_STEP] = $2.range[RANGE_STEP];
                $$.range[RANGE_EXT] = $2.range[RANGE_EXT];
            }
        ;

    subarray2_range:
            array2 full_size_range
            {
                $$ = $1;
                $$.range[RANGE_FIRST] = $2.range[RANGE_FIRST];
                $$.range[RANGE_LAST] = $2.range[RANGE_LAST];
                $$.range[RANGE_STEP] = $2.range[RANGE_STEP];
                $$.range[RANGE_EXT] = $2.range[RANGE_EXT];
            }
        ;

    subarray_fragmented_range1:
            array1 full_size_fragmented_range
            {
                $$ = $1;
                $$.range[RANGE_FIRST] = $2.range[RANGE_FIRST];
                $$.range[RANGE_LAST] = $2.range[RANGE_LAST];
                $$.range[RANGE_STEP] = $2.range[RANGE_STEP];
                $$.range[RANGE_EXT] = $2.range[RANGE_EXT];
            }
        ;

    subarray_fragmented_range2:
            array2 full_size_fragmented_range
            {
                $$ = $1;
                $$.range[RANGE_FIRST] = $2.range[RANGE_FIRST];
                $$.range[RANGE_LAST] = $2.range[RANGE_LAST];
                $$.range[RANGE_STEP] = $2.range[RANGE_STEP];
                $$.range[RANGE_EXT] = $2.range[RANGE_EXT];
            }
        ;


    float_list:
            /* empty */
            {
                $$.value.flist = new_float_list();
            }
        |   exp
            {
                $$.value.flist = new_float_list();
                add_to_float_list($1.value.var);
            }
        |   float_list ',' exp
            {
                $$.value.flist = $1.value.flist;
                add_to_float_list($3.value.var);
            }
        ;

    string_list:
            /* empty */
            {
                $$.value.mlist = new_mixed_list();
            }
        |   string
            {
                $$.value.mlist = new_mixed_list();
                add_to_mixed_list(STRING, 0, $1.value.str);
            }
        |   string_list ',' string
            {
                $$.value.mlist = $1.value.mlist;
                add_to_mixed_list(STRING, 0, $3.value.str);
            }
        ;

    mixed_list:
            /* empty */
            {
                $$.value.mlist = new_mixed_list();
            }
        |   exp
            {
                $$.value.mlist = new_mixed_list();
                add_to_mixed_list(VAR, $1.value.var, NULL);
            }
        |   string
            {
                $$.value.mlist = new_mixed_list();
                add_to_mixed_list(STRING, 0, $1.value.str);
            }
        |   mixed_list ',' exp
            {
                $$.value.mlist = $1.value.mlist;
                add_to_mixed_list(VAR, $3.value.var, NULL);
            }
        |   mixed_list ',' string
            {
                $$.value.mlist = $1.value.mlist;
                add_to_mixed_list(STRING, 0, $3.value.str);
            }
        ;

    assign:
            '='
        |   ADD
        |   SUB
        |   MUL
        |   DIV
        ;

    filehandle:
            '$' VAR
            {
                if ( $2.value.tptr->val_type == 0 ) {
                    yyerror( "Variable not initialized");
                    YYABORT;
                }
                $$.value.file = $2.value.tptr->file;
            }
        |   stream
        ;
        

    scan:
            SCAN
        |   SCANEXP
        ;

    stream:   
            STDIN
            {
                $$.value.file = stdin;
            }
        |   STDOUT
            {
                $$.value.file = stdout;
            }
        |   STDERR
            {
                $$.value.file = stderr;
            }
        ;
        

   baseline_mode:
            POLY
            {
                $$.value.btype = BASPL1;
            }
        |   TABL 
            {
                $$.value.btype = BASTB1;
            }
        |   POLYW 
            {
                $$.value.btype = BASPL2;
            }
        |   TABLW 
            {
                $$.value.btype = BASTB2;
            }
        |   SINE 
            {
                $$.value.btype = BASSN1;
            }
        |   COSINE
            {
                $$.value.btype = BASCS1;
            }
        |   SINCOS 
            {
                $$.value.btype = BASSC1;
            }
        |   SINEW
            {
                $$.value.btype = BASSN2;
            }
        |   COSINEW 
            {
                $$.value.btype = BASCS2;
            }
        |   SINCOSW 
            {
                $$.value.btype = BASSC2;
            }
        |   SPLINE 
            {
                $$.value.btype = BASAS1;
            }
        |   SPLINEW 
            {
                $$.value.btype = BASAS2;
            }
        ;
          
    predict:
            LPC
            {
                au_set_lpc_predict_mode(param, 0);
            }
        |   HSVD
            {
                au_set_lpc_predict_mode(param, 1);
            }
        ;


    arglist:
            /* empty */
        |   exp
            {
                add_arg_to_stack(&($1));
            }
        |   string
            {
                add_arg_to_stack(&($1));
            }
        |   arglist ',' exp
            {
                add_arg_to_stack(&($3));
            }
        |   arglist ',' string
            {
                add_arg_to_stack(&($3));
            }
        ;

    end:
            END
            {
                global_status.val_float = 0;
                global_status.type      = VAR; 
                global_status.val_type  = IS_FLOAT; 
            }
        |   RETURN
            {
                global_status.val_float = 0;
                global_status.type      = VAR; 
                global_status.val_type  = IS_FLOAT; 
            }
        |   RETURN  exp 
            {
                global_status.val_float = $2.value.var;
                global_status.type      = VAR; 
                global_status.val_type  = IS_FLOAT; 
            }
        |   RETURN string
            {
                global_status.value.str = 
                        create_and_cat_string($2.value.str, 
                                 NULL, 0, &(global_status.sbt),0,0); 
                global_status.type      = VAR; 
                global_status.val_type  = IS_STRING; 
            }
        ;
            

    user_function_end:
            end '\n'
            {
                pop_argcount_stack();
                if (!jump(pop_jump_stack()))
                    YYABORT;
            }
        ;
  

    user_function_call:
            FUNCTION_DEF '(' arglist ')'
            {
                push_jump_stack($4.stackpos+1);
                push_argcount_stack();
                if (!jump($1.value.tptr->value.jmp ))
                    YYABORT;
            }
        ;

    user_function: 
            user_function_call input user_function_end
        ;

    localvar:
            LOCAL '$' VAR
            {
                push_symbol_on_stack($3.value.tptr->name);
                $$ = $3;
            }
        |   LOCAL '%' VAR
            {
                push_symbol_on_stack($3.value.tptr->name);
                $$ = $3;
            }
        ;

    var_assign:
            localvar '='
            {
                $$ = $1;
            }
        |   '$' VAR '='
            {
                $$ = $2;
            }
        |   '%' VAR '='
            {
                $$ = $2;
            }
        ;

    for_var_list:
            '$' VAR
            {
                $2.value.tptr->type      = VAR; 
                $2.value.tptr->val_type  = IS_FLOAT; 
                $$.value.slist = new_symbol_list();
                add_to_symbol_list($2.value.tptr);
            }
        |   for_var_list ','  '$' VAR
            {
                $4.value.tptr->type      = VAR; 
                $4.value.tptr->val_type  = IS_FLOAT; 
                $$.value.slist = $1.value.slist;
                add_to_symbol_list($4.value.tptr);
            }
        ;


    for:
            FOR
        |   FOR '\n' FOR
            {
                $$.self = $3.self;
                $$.self->range[RANGE_STEP] = 0;
            }
        ;

    stmt:
            FUNCTION FUNCTION_DEF
            {
                if (!jump($1.value.jmp ))
                    YYABORT;
            }
        |   '$' user_function
        |   '%' user_function
        |   IF exp THEN 
            { 
                if (!(int) $2.value.var) 
                    if (!jump($3.value.jmp ))
                        YYABORT;
            }
        |   ELSEIF exp THEN 
            { 
                if (!(int) $2.value.var) 
                    if (!jump($3.value.jmp ))
                        YYABORT;
            }
        |   ELSE
            { 
                if (!jump($1.value.jmp ))
                    YYABORT;
            }
        |   ENDIF
        |   WHILE exp DO 
            { 
                if (!(int) $2.value.var) 
                    if (!jump($3.value.jmp ))
                        YYABORT;
            }
        |   DO
        |   WHILE exp
            { 
                if ((int) $2.value.var) 
                    if (!jump($1.value.jmp ))
                        YYABORT;
            }
        |   for '$' VAR IN full_fragmented_range DO 
            { 
                int *list;
                if ($1.self->range[RANGE_STEP] == 0) {
                    $1.self->range[RANGE_FIRST] = $5.range[RANGE_FIRST];
                    $1.self->range[RANGE_LAST] = $5.range[RANGE_LAST];
                    $1.self->range[RANGE_STEP] = $5.range[RANGE_STEP];
                    $1.self->range[RANGE_POS] = $5.range[RANGE_FIRST];
                    $1.self->range[RANGE_EXT] = $5.range[RANGE_EXT];
                    if ($5.range[RANGE_EXT]>0) {
                        list = get_range_master_list($5.range[RANGE_EXT]);
                        $1.self->range[RANGE_LAST] = list[2];
                    }
                    if ($1.self->range[RANGE_FIRST] == UNDETERMINED) 
                        $1.self->range[RANGE_FIRST] = au_get_range_start(param,2)-1;
                    if ($1.self->range[RANGE_LAST] == UNDETERMINED) 
                        $1.self->range[RANGE_LAST] = au_get_range_stop(param,2)-1;
                }
                else
                    $1.self->range[RANGE_POS] += $1.self->range[RANGE_STEP];
                if ($1.self->range[RANGE_POS] > $1.self->range[RANGE_LAST]) {
                    if ($1.self->range[RANGE_EXT]>0) {
                        int k;
                        list = get_range_master_list($1.self->range[RANGE_EXT]);
                        for (k=1;k<=list[0];k+=2) {
                            if (list[k] > $1.self->range[RANGE_LAST]) {
                                $1.self->range[RANGE_FIRST] = list[k];
                                $1.self->range[RANGE_POS]   = list[k];
                                $1.self->range[RANGE_LAST]  = list[k+1];
                                break;
                            }
                        }
                    }
                }
                $3.value.tptr->val_float = 
                   (float) $1.self->range[RANGE_POS]+1;
                $3.value.tptr->val_type  = IS_FLOAT; 
                if ($1.self->range[RANGE_POS] > $1.self->range[RANGE_LAST]) {
                    $1.self->range[RANGE_STEP] = 0;
                    if (!jump($6.value.jmp ))
                        YYABORT;
                }
            }
        |   for for_var_list INSORTED SORTED fragmented_range_list DO 
            { 
                int ival, ipos, ok = FALSE;
                if ($1.self->range[RANGE_STEP] == 0) {
                    int i,dim;
                    $1.self->range[RANGE_STEP] = 2;
                    $1.self->range[RANGE_POS] = 0;
                    $1.self->rps = 
                        au_build_sorted_range(param,$5.value.rlist->num,
                                              $5.value.rlist->list);
                    $1.rps = $1.self->rps;
                    for (i=0;i<$2.value.slist->num;i++)
                        $2.value.slist->list[i]->val_float = 1.0;
                }
                else
                    $1.self->range[RANGE_POS] += $1.self->range[RANGE_STEP];
                while (!ok) {
                    ipos =  $1.rps[$1.self->range[RANGE_POS]];
                    if (ipos == ENDOFRANGE) {
                        ok = TRUE;
                        $1.self->range[RANGE_STEP] = 0;
                        yy_free($1.self->rps);
                        $1.self->rps = NULL;
                        free_symbol_list();
                        if (!jump($6.value.jmp))
                            YYABORT;
                    }
                    else {
                        if (ipos==$2.value.slist->num)
                            ok = TRUE;
                        ival =  $1.rps[$1.self->range[RANGE_POS]+1];
/*
                        $2.value.slist->list[$2.value.slist->num-ipos]->val_float = (float) ival;
*/
                        $2.value.slist->list[ipos-1]->val_float = (float) ival;
                    }
                    if (!ok)
                        $1.self->range[RANGE_POS] += $1.self->range[RANGE_STEP];
                }
            }
        |   DONE
            {
                if (!jump($1.value.jmp ))
                    YYABORT;
            }
        |   BREAK
            {
                if (!jump($1.value.jmp ))
                    YYABORT;
            }
        |   CONTINUE
            {
                if (!jump($1.value.jmp ))
                    YYABORT;
            }
        |   EXIT
            {
                if (1) YYABORT; 
            }
        |   exec_stm
        |   PUTENV '(' string ')'
            {
                int i = putenv($3.value.str);
                if (i == -1) {
                    yyerror("putenv, insufficient space in the environment");
                    YYABORT;
                }
            }
        |   var_assign stream
            {
                $1.value.tptr->file = $2.value.file;
                $1.value.tptr->val_float = 
                    (float) (unsigned int) $1.value.tptr->file; 
                $1.value.tptr->type      = VAR; 
                $1.value.tptr->val_type  = IS_FLOAT; 
            }
        |   PRINT string
            {
                au_puts((void*)param,$2.value.str);
                fflush(stdout);
            }
       |    PRINT filehandle ',' string
            {
                if ($2.value.file != NULL) {
                    fputs($4.value.str, $2.value.file);
                    fflush($2.value.file);
                }
            }
        |   PRINTF string ',' mixed_list
            {
                au_puts((void*)param,
                    printf_mixed_list($2.value.str,$4.value.mlist));
                fflush(stdout);
            }
        |   FPRINTF filehandle ',' string ',' mixed_list
            {
                if ($2.value.file != NULL) {
                    fprintf($2.value.file, "%s", 
                        printf_mixed_list($4.value.str,$6.value.mlist));
                    fflush($2.value.file);
                }
            }
        |   PRINTLN string
            {
                au_puts((void*)param,$2.value.str);
                au_puts((void*)param,"\n");
            }
        |   PRINTLN filehandle ',' string
            {
                if ($2.value.file != NULL) {
                    fputs($4.value.str, $2.value.file);
                    fputs("\n", $2.value.file);
                }
            }
        |   '%' VAR assign string     
            { 
                if ($3.type == '=') 
                    $2.value.tptr->value.str = 
                        create_and_cat_string($4.value.str, 
                                 NULL, 0, &($2.value.tptr->sbt),0,0); 
                else if ($3.type == ADD) 
                    $1.value.tptr->value.str = 
                        create_and_cat_string($2.value.tptr->value.str, 
                                 $4.value.str, 0,&($2.value.tptr->sbt),0,0); 
                else {
                    yyerror("Operator not allowed for strings");
                    YYABORT;
                }
                $$.type = STRING; 
                $2.value.tptr->type      = VAR; 
                $2.value.tptr->val_type  = IS_STRING; 
            }
        |   '%' VAR full_range '=' string     
            {
                int len1,len2,len3;
                int first, last;

                first = $3.range[RANGE_FIRST];
                last  = $3.range[RANGE_LAST];
                len2  = strlen($5.value.str);
                if (first == UNDETERMINED) 
                    first = 0;
                if ($2.value.tptr->type == VAR &&
                    $2.value.tptr->val_type == IS_STRING) {
                    /*
                     * String exists, check ranges
                     */
                    len1 = strlen($2.value.tptr->value.str);
                    if (last == UNDETERMINED || last > len1-1) {
                        if (last == UNDETERMINED)
                            last = len1-1;
                        /*
                         * If string is too short, enlarge
                         * Fill new part with blanks
                         */
                        if (last - first + 1 < len2 || last > len1-1) {
                            char *tmp;
                            len1 = first + len2;
                            last = len1-1;
                            tmp = malloc((len1+1) * sizeof(char));
                            assert(tmp);
                            memset(tmp, ' ', len1);
                            memcpy(tmp,$2.value.tptr->value.str,
                                   strlen($2.value.tptr->value.str));
                            tmp[len1] = '\0';
                            $2.value.tptr->value.str = 
                                create_and_cat_string(tmp, 
                                    NULL, 0, &($2.value.tptr->sbt),0,0);
                            free(tmp);
                        }
                    }
                }
                else {
                    /*
                     * If string doesn't exists, create a new one
                     * fill string with blanks
                     */
                    char *tmp;
                    if (last == UNDETERMINED) 
                        last = len2 + first - 1;
                    len1 = last+1;
                    tmp = malloc((len1+1) * sizeof(char));
                    assert(tmp);
                    memset(tmp, ' ', len1);
                    tmp[len1] = '\0';
                    $2.value.tptr->value.str = 
                        create_and_cat_string(tmp, 
                                 NULL, 0, &($2.value.tptr->sbt),0,0);
                    free(tmp);
                    $2.value.tptr->type      = VAR; 
                    $2.value.tptr->val_type  = IS_STRING; 
                }
                last = au_min(last,len1-1);
                len3 = last - first + 1;
                len3 = au_min(len3,len2);
                /*
                 * Copy selected part into the old string
                 */
                if (len3 > 0)
                    memcpy($2.value.tptr->value.str + first,
                           $5.value.str,
                           len3);
                $$.type = STRING; 
            }
       |   localvar '=' string     
            { 
                $1.value.tptr->value.str = 
                    create_and_cat_string($3.value.str, 
                             NULL, 0, &($1.value.tptr->sbt),0,0); 
                $$.type = STRING; 
                $1.value.tptr->type      = VAR; 
                $1.value.tptr->val_type  = IS_STRING; 
            }
        |   '$' VAR assign exp     
            { 
                if ($3.type == '=') 
                    $2.value.tptr->val_float = $4.value.var; 
                else if ($3.type == ADD) 
                    $2.value.tptr->val_float += $4.value.var; 
                else if ($3.type == SUB) 
                    $2.value.tptr->val_float -= $4.value.var; 
                else if ($3.type == MUL) 
                    $2.value.tptr->val_float *= $4.value.var; 
                else if ($3.type == DIV) 
                    $2.value.tptr->val_float /= $4.value.var; 
                $2.value.tptr->type      = VAR; 
                $2.value.tptr->val_type  = IS_FLOAT; 
            }
        |   localvar '=' exp     
            { 
                $1.value.tptr->val_float = $3.value.var; 
                $1.value.tptr->type      = VAR; 
                $1.value.tptr->val_type  = IS_FLOAT; 
            }
        |   any_fragmented_subarray assign exp 
            {
                int n,i,j,num,*list;
                float *f = *($1.value.array->data);
                if ($1.range[RANGE_EXT]>0) {
                    list = get_range_master_list($1.range[RANGE_EXT]);
                    num  = list[0];
                    list++;
                }
                else {
                    list = $1.range;
                    num  = 2;
                }
                for (i=0;i<2;i++) {
                for (j=0;j<num;j+=2) {
                if ($2.type == '=')
                    for (n=list[j];n<=list[j+1];n+=$1.range[RANGE_STEP])
                        f[n] = $3.value.var;
                else if ($2.type == ADD)
                    for (n=list[j];n<=list[j+1];n+=$1.range[RANGE_STEP])
                        f[n] += $3.value.var;
                else if ($2.type == SUB)
                    for (n=list[j];n<=list[j+1];n+=$1.range[RANGE_STEP])
                        f[n] -= $3.value.var;
                else if ($2.type == MUL)
                    for (n=list[j];n<=list[j+1];n+=$1.range[RANGE_STEP])
                        f[n] *= $3.value.var;
                else if ($2.type == DIV)
                    for (n=list[j];n<=list[j+1];n+=$1.range[RANGE_STEP])
                        f[n] /= $3.value.var;
                }
                if ($1.type != ARRAY2)
                    break;
                f = *($1.value.array->data2);
                }
            }
        |   any_fragmented_subarray assign array1
            {
                int n,i,j,num,*list;
                float *f1 = *($1.value.array->data);
                float *f3 = *($3.value.array->data);
                if ($1.range[RANGE_EXT]>0) {
                    list = get_range_master_list($1.range[RANGE_EXT]);
                    num  = list[0];
                    list++;
                }
                else {
                    list = $1.range;
                    num  = 2;
                }
                for (i=0;i<2;i++) {
                for (j=0;j<num;j+=2) {
                if ($2.type == '=')
                    for (n=list[j];n<=list[j+1];n+=$1.range[RANGE_STEP])
                        f1[n] = f3[n];
                else if ($2.type == ADD)
                    for (n=list[j];n<=list[j+1];n+=$1.range[RANGE_STEP])
                        f1[n] += f3[n];
                else if ($2.type == SUB)
                    for (n=list[j];n<=list[j+1];n+=$1.range[RANGE_STEP])
                        f1[n] -= f3[n];
                else if ($2.type == MUL)
                    for (n=list[j];n<=list[j+1];n+=$1.range[RANGE_STEP])
                        f1[n] *= f3[n];
                else if ($2.type == DIV)
                    for (n=list[j];n<=list[j+1];n+=$1.range[RANGE_STEP])
                        f1[n] /= f3[n];
                }
                if ($1.type != ARRAY2)
                    break;
                f1 = *($1.value.array->data2);
                }
            }
        |   INITPARAM
            {
                au_init_param(param);
            }
        |   UNPACKI any_subarray 
            {
                int n;
                for (n=$2.range[RANGE_FIRST];n<$2.range[RANGE_LAST];n++)
                    (*($2.value.array->data))[n] =
                        (float) au_unpack_int((*($2.value.array->data))+n);
                if ($2.type == ARRAY2)
                    for (n=$2.range[RANGE_FIRST];n<$2.range[RANGE_LAST];n++)
                        (*($2.value.array->data2))[n] =
                            (float) au_unpack_int((*($2.value.array->data2))+n);
            }
        |   I2F any_subarray 
            {
                au_xxi2f(*($2.value.array->data), 
                          $2.range[RANGE_FIRST]+1, 
                          $2.range[RANGE_LAST]+1);
                if ($2.type == ARRAY2)
                    au_xxi2f(*($2.value.array->data2), 
                          $2.range[RANGE_FIRST]+1, 
                          $2.range[RANGE_LAST]+1);
            }
        |   F2I any_subarray 
            {
                au_xxf2i(*($2.value.array->data), 
                          $2.range[RANGE_FIRST]+1, 
                          $2.range[RANGE_LAST]+1);
                if ($2.type == ARRAY2)
                    au_xxf2i(*($2.value.array->data2), 
                          $2.range[RANGE_FIRST]+1, 
                          $2.range[RANGE_LAST]+1);
            }
        |   NEGSTEP any_subarray ',' exp ',' exp
            {
                au_negstep(*($2.value.array->data), 
                            $2.range[RANGE_FIRST]+1, 
                            $2.range[RANGE_LAST]+1,
                            $4.value.var,
                            $6.value.var);
                if ($2.type == ARRAY2)
                    au_negstep(*($2.value.array->data2), 
                            $2.range[RANGE_FIRST]+1, 
                            $2.range[RANGE_LAST]+1,
                            $4.value.var,
                            $6.value.var);
            }
        |   SWAP4 any_subarray
            {
                au_xxswap4(*($2.value.array->data), 
                            $2.range[RANGE_FIRST]+1, 
                            $2.range[RANGE_LAST]+1);
                if ($2.type == ARRAY2)
                    au_xxswap4(*($2.value.array->data2), 
                            $2.range[RANGE_FIRST]+1, 
                            $2.range[RANGE_LAST]+1);
            }
        |   SWAP2 any_subarray
            {
                au_xxswap2(*($2.value.array->data), 
                            $2.range[RANGE_FIRST]+1, 
                            $2.range[RANGE_LAST]+1);
                if ($2.type == ARRAY2)
                    au_xxswap2(*($2.value.array->data2), 
                            $2.range[RANGE_FIRST]+1, 
                            $2.range[RANGE_LAST]+1);
            }
        |   C2RI any_subarray ',' any_subarray ',' any_subarray
            {
                au_xxc2ri(*($2.value.array->data),
                           *($4.value.array->data),
                           *($6.value.array->data), 
                             $2.range[RANGE_FIRST]+1, 
                             $2.range[RANGE_LAST]+1);
            }
        |   C2IR any_subarray ',' any_subarray ',' any_subarray
            {
                au_xxc2ir(*($2.value.array->data),
                           *($4.value.array->data),
                           *($6.value.array->data), 
                             $2.range[RANGE_FIRST]+1, 
                             $2.range[RANGE_LAST]+1);
            }
        |   UNPACK0123 any_subarray ',' any_subarray 
            {
                au_3to4(*($2.value.array->data),
                           *($4.value.array->data),
                             $2.range[RANGE_FIRST]+1, 
                             $2.range[RANGE_LAST]+1,
                             $4.range[RANGE_FIRST]+1, 
                             $4.range[RANGE_LAST]+1,
                             MODE0123);
            }
        |   UNPACK3210 any_subarray ',' any_subarray 
            {
                au_3to4(*($2.value.array->data),
                           *($4.value.array->data),
                             $2.range[RANGE_FIRST]+1, 
                             $2.range[RANGE_LAST]+1,
                             $4.range[RANGE_FIRST]+1, 
                             $4.range[RANGE_LAST]+1,
                             MODE3210);
            }
        |   UNPACK0321 any_subarray ',' any_subarray 
            {
                au_3to4(*($2.value.array->data),
                           *($4.value.array->data),
                             $2.range[RANGE_FIRST]+1, 
                             $2.range[RANGE_LAST]+1,
                             $4.range[RANGE_FIRST]+1, 
                             $4.range[RANGE_LAST]+1,
                             MODE0321);
            }
        |   UNPACK1230 any_subarray ',' any_subarray 
            {
                au_3to4(*($2.value.array->data),
                           *($4.value.array->data),
                             $2.range[RANGE_FIRST]+1, 
                             $2.range[RANGE_LAST]+1,
                             $4.range[RANGE_FIRST]+1, 
                             $4.range[RANGE_LAST]+1,
                             MODE1230);
            }
        |   any_subarray ROL exp
            {
                au_xxrotate(*($1.value.array->data), 
                            $1.range[RANGE_FIRST]+1, 
                            $1.range[RANGE_LAST]+1,
                            $3.value.var);
                if ($1.type == ARRAY2)
                    au_xxrotate(*($1.value.array->data2), 
                            $1.range[RANGE_FIRST]+1, 
                            $1.range[RANGE_LAST]+1,
                            $3.value.var);
            }
        |   any_subarray ROR exp
            {
                au_xxrotate(*($1.value.array->data), 
                            $1.range[RANGE_FIRST]+1, 
                            $1.range[RANGE_LAST]+1,
                            -$3.value.var);
                if ($1.type == ARRAY2)
                    au_xxrotate(*($1.value.array->data2), 
                             $1.range[RANGE_FIRST]+1, 
                             $1.range[RANGE_LAST]+1,
                            -$3.value.var);
            }
        |   any_subarray LS exp
            {
                au_array_shift(param, *($1.value.array->data), 
                            $1.range[RANGE_FIRST]+1, 
                            $1.range[RANGE_LAST]+1,
                            $3.value.var);
                if ($1.type == ARRAY2)
                    au_array_shift(param, *($1.value.array->data2), 
                             $1.range[RANGE_FIRST]+1, 
                             $1.range[RANGE_LAST]+1,
                             $3.value.var);
            }
        |   any_subarray RS exp
            {
                au_array_shift(param, *($1.value.array->data), 
                            $1.range[RANGE_FIRST]+1, 
                            $1.range[RANGE_LAST]+1,
                            -$3.value.var);
                if ($1.type == ARRAY2)
                    au_array_shift(param, *($1.value.array->data2), 
                             $1.range[RANGE_FIRST]+1, 
                             $1.range[RANGE_LAST]+1,
                             -$3.value.var);
            }
        |   RV any_subarray
            {
                au_do_reverse(*($2.value.array->data), $2.range[RANGE_FIRST]+1, $2.range[RANGE_LAST]+1);
                if ($2.type == ARRAY2)
                    au_do_reverse(*($2.value.array->data2), $2.range[RANGE_FIRST]+1, 
                               $2.range[RANGE_LAST]+1);
            }
        |   var_assign STRTOK string 
            {
                char *result;
                result = au_strtok(NULL, $3.value.str);
                $1.value.tptr->value.str = 
                        create_and_cat_string(result,NULL,
                                          0, &($1.value.tptr->sbt),0,0); 
                $1.value.tptr->type      = VAR; 
                $1.value.tptr->val_type  = IS_STRING; 
            }
        |   var_assign STRTOK string ',' string 
            {
                char *result;
                result = au_strtok($3.value.str, $5.value.str);
                $1.value.tptr->value.str = 
                        create_and_cat_string(result,NULL,
                                          0, &($1.value.tptr->sbt),0,0); 
                $1.value.tptr->type      = VAR; 
                $1.value.tptr->val_type  = IS_STRING; 
            }
        |   CHDIR string
            {
                int err = chdir(au_check_filename($2.value.str));
                if (err) {
                    yyerror( "Error changing directory");
                    YYABORT;
                }
            }
        |   var_assign GETCWD
            {
                char *buf,*p;
                buf = (char*)malloc(1024);
                assert(buf);
                p = getcwd(buf,1024);
                if (p==NULL) {
                    free(buf);
                    yyerror("Error getting directory");
                    YYABORT;
                }
                $1.value.tptr->value.str = 
                                create_and_cat_string(buf, 
                                    NULL, 0, &($1.value.tptr->sbt), 0,0);
                $1.value.tptr->type      = VAR; 
                $1.value.tptr->val_type  = IS_STRING; 
                free(buf);
            }
        |   var_assign OPEN string ',' string
            {
                $1.value.tptr->file      = fopen(au_check_filename($3.value.str), 
                                                 au_check_openmode($5.value.str));
                if ($1.value.tptr->file == NULL) {
                    /*
                    yyerror("Error opening file");
                    YYABORT;
                    */
                }
                $1.value.tptr->val_float = 
                    (float) (unsigned int) $1.value.tptr->file; 
                $1.value.tptr->type      = VAR; 
                $1.value.tptr->val_type  = IS_FLOAT; 
            }
        |   CLOSE filehandle
            {
                if ($2.value.file != stdin &&
                    $2.value.file != stdout &&
                    $2.value.file != stderr) 
                        if ($2.value.file == 0 ||
                                fclose($2.value.file) != 0) {
                            yyerror( "Error closing file");
                            YYABORT;
                        }
            }
        |   var_assign TELL filehandle
            {
                if ( $3.value.file == NULL ) {
                    yyerror("File not open");
                    YYABORT;
                }
                $1.value.tptr->val_float = 
                        (float) ftell($3.value.file);
                $1.value.tptr->type      = VAR; 
                $1.value.tptr->val_type  = IS_FLOAT; 
                if ($1.value.tptr->val_float < 0) {
                    yyerror("Error in file access");
                    YYABORT;
                }
            }
        |   var_assign STAT string
            {
                struct stat stat_buf;
                int i = stat($3.value.str, &stat_buf);
                if (i != 0) {
                    /*
                     * on error, return -1
                     */
                    $1.value.tptr->val_float = 
                        (float) -1;
                }
                else {
                    /*
                     * return file size in bytes
                     */
                    $1.value.tptr->val_float = 
                        (float) stat_buf.st_size;
                }
                $1.value.tptr->type      = VAR; 
                $1.value.tptr->val_type  = IS_FLOAT; 
            }
        |   SEEK filehandle ',' exp
            {
                if ( $2.value.file == NULL ) {
                    yyerror( "File not open");
                    YYABORT;
                }
                if (fseek($2.value.file, 
                      (long) $4.value.var, 
                      SEEK_SET) != 0) {
                    yyerror( "Error in file access");
                    YYABORT;
                }
            }
        |   any_fragmented_subarray '=' READ filehandle
            {
                size_t result, range;
                int *list,num,j;
                /*
                if ( $4.value.tptr->val_type == 0 ) {
                    yyerror( "Variable not initialized");
                    YYABORT;
                }
                if ( $4.value.tptr->file == NULL ) {
                    yyerror( "File not open");
                    YYABORT;
                }
                if ($1.range[RANGE_EXT]>0) {
                    list = get_range_master_list($1.range[RANGE_EXT]);
                    num  = list[0];
                    list++;
                }
                else {
                    list = $1.range;
                    num  = 2;
                }
                for (j=0;j<num;j+=2) {
                    range = list[j+1] - list[j] + 1;
                    result = fread(*($1.value.array->data) + list[j],
                                   sizeof(float),
                                   range,
                                   $4.value.tptr->file);
                    if (result != range) {
                        yyerror("Error in file access");
                        YYABORT;
                    }
                }
                if ($1.type == ARRAY2) for (j=0;j<num;j+=2) {
                    range = list[j+1] - list[j] + 1;
                    result = fread(*($1.value.array->data2) + list[j],
                                   sizeof(float),
                                   range,
                                   $4.value.tptr->file);
                    if (result != range) {
                        yyerror(("Error in file access");
                        YYABORT;
                    }
                }
                */
                
                if ( $4.value.file == NULL ) {
                    yyerror( "File not open");
                    YYABORT;
                }
                if ($1.range[RANGE_EXT]>0) {
                    list = get_range_master_list($1.range[RANGE_EXT]);
                    num  = list[0];
                    list++;
                }
                else {
                    list = $1.range;
                    num  = 2;
                }
                for (j=0;j<num;j+=2) {
                    range = list[j+1] - list[j] + 1;
                    result = fread(*($1.value.array->data) + list[j],
                                   sizeof(float),
                                   range,
                                   $4.value.file);
                    if (result != range) {
                        yyerror( "Error in file access");
                        YYABORT;
                    }
                }
                if ($1.type == ARRAY2) for (j=0;j<num;j+=2) {
                    range = list[j+1] - list[j] + 1;
                    result = fread(*($1.value.array->data2) + list[j],
                                   sizeof(float),
                                   range,
                                   $4.value.file);
                    if (result != range) {
                        yyerror("Error in file access");
                        YYABORT;
                    }
                }
            }
        |   var_assign READ filehandle
            {
                float f;
                size_t result, range = sizeof(float);
                if ( $3.value.file == NULL ) {
                    yyerror("File not open");
                    YYABORT;
                }
                result = fread(&f,
                          (size_t) 1,
                          range,
                          $3.value.file);

                if (result != range) {
                    yyerror("Error in file access");
                    YYABORT;
                }
                $1.value.tptr->val_float = f; 
                $1.value.tptr->type      = VAR; 
                $1.value.tptr->val_type  = IS_FLOAT; 
            }
        |   var_assign scan filehandle ',' string
            {
                char *result;
                int expression = FALSE;
                if ( $3.value.file == NULL ) {
                    yyerror("File not open");
                    YYABORT;
                }
                if ($2.type == SCANEXP)
                    expression = TRUE;
                result = au_scanfile($3.value.file,
                                  "",
                                  0,
                                  $5.value.str,
                                  expression);
                $1.value.tptr->value.str = 
                        create_and_cat_string(result,NULL,
                                          0, &($1.value.tptr->sbt),0,0); 
                $1.value.tptr->type      = VAR; 
                $1.value.tptr->val_type  = IS_STRING; 
            }
        |   var_assign SCANBIN filehandle ',' string
            {
                char *result;
                int expression = FALSE;
                if ( $3.value.file == NULL ) {
                    yyerror("File not open");
                    YYABORT;
                }
                result = au_scanbinfile($3.value.file,
                                     $5.value.str);
                $1.value.tptr->value.str = 
                        create_and_cat_string(result,NULL,
                                          0, &($1.value.tptr->sbt),0,0); 
                $1.value.tptr->type      = VAR; 
                $1.value.tptr->val_type  = IS_STRING; 
            }
        |   var_assign scan filehandle ',' string ',' exp ',' string
            {
                char *result;
                int expression = FALSE;
                if ( $3.value.file == NULL ) {
                    yyerror("File not open");
                    YYABORT;
                }
                if ($2.type == SCANEXP)
                    expression = TRUE;
                result = au_scanfile($3.value.file,
                                  $5.value.str,
                                  $7.value.var,
                                  $9.value.str,
                                  expression);
                $1.value.tptr->value.str = 
                        create_and_cat_string(result,NULL,
                                          0, &($1.value.tptr->sbt),0,0); 
                $1.value.tptr->type      = VAR; 
                $1.value.tptr->val_type  = IS_STRING; 
            }
        |   WRITE filehandle ',' any_subarray 
            {
                size_t result, range = $4.range[RANGE_LAST] - $4.range[RANGE_FIRST] + 1;
                if ( $2.value.file == NULL ) {
                    yyerror( "File not open");
                    YYABORT;
                }
                result = fwrite(*($4.value.array->data) + $4.range[RANGE_FIRST],
                          sizeof(float),
                          range,
                          $2.value.file);
                if (result == range && $4.type == ARRAY2)
                       fwrite(*($4.value.array->data2) + $4.range[RANGE_FIRST],
                          sizeof(float),
                          range,
                          $2.value.file);
                if (result != range) {
                    yyerror("Error in file access");
                    YYABORT;
                }
            }
        |   WRITE filehandle ',' braced_exp_minus_array
            {
                size_t result, range = 1;
                if ( $2.value.file == NULL ) {
                    yyerror( "File not open");
                    YYABORT;
                }
                result = fwrite(&($4.value.var),
                          sizeof(float),
                          range,
                          $2.value.file);
                if (result != range) {
                    yyerror("Error in file access");
                    YYABORT;
                }
            }
        |   RD any_array ',' string
            {
                if ($2.type == ARRAY2) {
                    if (!au_read_smxarray(param,$4.value.str, 
                                      *($2.value.array->data),
                                      *($2.value.array->data2),
                                      0, 
                                      0)) {
                        yyerror("Error in file access");
                        YYABORT;
                    }
                }
                else {
                    if (!au_read_smxarray(param,$4.value.str, 
                                      *($2.value.array->data),
                                      NULL,
                                      0, 
                                      0)) {
                        yyerror("Error in file access");
                        YYABORT;
                    }
                }
            }
        |   RD any_subarray_range ',' string
            {
                if ($2.type == ARRAY2) {
                    if (!au_read_smxarray(param,$4.value.str, 
                                      *($2.value.array->data),
                                      *($2.value.array->data2),
                                      $2.range[RANGE_FIRST]+1, 
                                      $2.range[RANGE_LAST]+1)) {
                        yyerror("Error in file access");
                        YYABORT;
                    }
                }
                else {
                    if (!au_read_smxarray(param,$4.value.str, 
                                      *($2.value.array->data),
                                      NULL,
                                      $2.range[RANGE_FIRST]+1, 
                                      $2.range[RANGE_LAST]+1)) {
                        yyerror("Error in file access");
                        YYABORT;
                    }
                }
            }
        |   WR any_subarray ',' string
            {
                if ($2.type == ARRAY2) {
                    if (!au_write_smxarray(param,$4.value.str, 
                                       *($2.value.array->data),
                                       *($2.value.array->data2),
                                       $2.range[RANGE_FIRST]+1, 
                                       $2.range[RANGE_LAST]+1)) {
                        yyerror("Error in file access");
                        YYABORT;
                    }
                }
                else {
                    if (!au_write_smxarray(param,$4.value.str, 
                                       *($2.value.array->data),
                                       NULL,
                                       $2.range[RANGE_FIRST]+1, 
                                       $2.range[RANGE_LAST]+1)) {
                        yyerror("Error in file access");
                        YYABORT;
                    }
                }
            }
        |   TP BLOCK ',' BLOCK
            {
                au_transfer_parameters(param,$2.value.block->id, $4.value.block->id);
            }
        |   nmr_var_block_dim exp
            { 
                switch ($1.value.id) {
                    case PSND_SF:
                        au_set_spectral_frequecy(param, $1.range[RANGE_FIRST], 
                                                 $1.range[RANGE_LAST], $2.value.var); 
                        break;
                    case PSND_SW:
                        au_set_spectral_width(param, $1.range[RANGE_FIRST], 
                                              $1.range[RANGE_LAST], $2.value.var); 
                        break;
                    case PSND_SI:
                    case PSND_TD:
                        au_set_timedomain(param,$1.range[RANGE_FIRST], 
                                          $1.range[RANGE_LAST], (int)$2.value.var); 
                        break;
                    case PSND_XR:
                        au_set_reference_channel(param,$1.range[RANGE_FIRST], 
                                          $1.range[RANGE_LAST], $2.value.var, 0); 
                        break;
                    case PSND_AREF:
                        au_set_reference_channel(param,$1.range[RANGE_FIRST], 
                                          $1.range[RANGE_LAST], $2.value.var, 1); 
                        break;
                    case PSND_BREF:
                        au_set_reference_channel(param,$1.range[RANGE_FIRST], 
                                          $1.range[RANGE_LAST], $2.value.var, 2); 
                        break;
                    case PSND_PA:
                        au_set_phase0(param,$1.range[RANGE_FIRST], 
                                          $1.range[RANGE_LAST], $2.value.var); 
                        break;
                    case PSND_PB:
                        au_set_phase1(param,$1.range[RANGE_FIRST], 
                                          $1.range[RANGE_LAST], $2.value.var); 
                        break;
                    case PSND_I0:
                        au_set_phase_pos(param,$1.range[RANGE_FIRST], 
                                         $1.range[RANGE_LAST], (int)$2.value.var); 
                        break;
                    case PSND_SM:
                        {
                        int mode = PSTREA;
                        if ($2.value.var == 1)
                            mode = PSTREA;
                        else if ($2.value.var == 2)
                            mode = PSTBOT;
                        au_set_postmode(param, $1.range[RANGE_FIRST], 
                                         $1.range[RANGE_LAST], mode);
                        }
                        break;
                    case PSND_RM:
                        {
                        int mode = PSTREA;
                        if ($2.value.var == 1)
                            mode = PREREA;
                        else if ($2.value.var == 2)
                            mode = PREBOT;
                        else if ($2.value.var == 3)
                            mode = PREIMA;
                        au_set_premode(param, $1.range[RANGE_FIRST], 
                                         $1.range[RANGE_LAST], mode);
                        }
                        break;
                    case PSND_SPEC:
                        au_define_data_as_spectrum(param,$1.range[RANGE_FIRST], 
                                         $1.range[RANGE_LAST], (int)$2.value.var); 
                        break;
                    case PSND_DSPSHIFT:
                        au_set_dspshift(param,$1.range[RANGE_FIRST], 
                                         $1.range[RANGE_LAST], $2.value.var); 
                        break;
                    case PSND_FTSCALE:
                        au_set_ftscale(param,$1.range[RANGE_FIRST], 
                                       $1.range[RANGE_LAST], $2.value.var); 
                        break;
                }
            }
        |   si exp
            {
                au_set_size(param,$1.value.id, (int)$2.value.var);
            }
        |   IFT
            {
                au_do_if(param,0);
            }
        |   IFT array
            {
                int mode = 0;
                if (strstr($2.value.array->name,"@r"))
                    mode = FFTREA;
                else if (strstr($2.value.array->name,"@i"))
                    mode = FFTIMA;
                else if (strstr($2.value.array->name,"@c"))
                    mode = FFTCX;
                au_do_if(param,mode);
            }
        |   FT
            {
                au_do_ft(param,-1,0);
            }
        |   FT exp
            {
                au_do_ft(param,(int)$2.value.var,0);
            }
        |   FT exp ',' array
            {
                int mode=0;
                if (strstr($4.value.array->name,"@r"))
                    mode = FFTREA;
                else if (strstr($4.value.array->name,"@i"))
                    mode = FFTIMA;
                else if (strstr($4.value.array->name,"@c"))
                    mode = FFTCX;
                au_do_ft(param,(int)$2.value.var,mode);
            }
        |   DFT
            {
                au_do_dft(param,0);
            }
        |   DFT array
            {
                int mode=0;
                if (strstr($2.value.array->name,"@r"))
                    mode = DFTREA;
                else if (strstr($2.value.array->name,"@i"))
                    mode = DFTIMA;
                else if (strstr($2.value.array->name,"@c"))
                    mode = DFTCX;
                au_do_dft(param,mode);
            }
        |   SN exp
            {
                au_set_shift(param, $2.value.var);
                au_set_window_size(param, 0, 0);
                au_do_window(param, PSND_SN);
            }
        |   SN exp ',' full_size_range
            {
                au_set_shift(param, $2.value.var);
                au_set_window_size(param, $4.range[RANGE_FIRST]+1, $4.range[RANGE_LAST]+1);
                au_do_window(param, PSND_SN);
            }
        |   SQ exp
            {
                au_set_shift(param, $2.value.var);
                au_set_window_size(param, 0, 0);
                au_do_window(param, PSND_SQ);
            }
        |   SQ exp ',' full_size_range
            {
                au_set_shift(param, $2.value.var);
                au_set_window_size(param, $4.range[RANGE_FIRST]+1, $4.range[RANGE_LAST]+1);
                au_do_window(param, PSND_SQ);
            }
        |   KS exp
            {
                au_set_shift(param, $2.value.var);
                au_set_window_size(param, 0, 0);
                au_do_window(param, PSND_KS);
            }
        |   KS exp ',' full_size_range
            {
                au_set_shift(param, $2.value.var);
                au_set_window_size(param, $4.range[RANGE_FIRST]+1, $4.range[RANGE_LAST]+1);
                au_do_window(param, PSND_KS);
            }
        |   HN 
            {
                au_set_window_size(param, 0, 0);
                au_do_window(param, PSND_HN);
            }
        |   HN full_size_range
            {
                au_set_window_size(param, $2.range[RANGE_FIRST]+1, $2.range[RANGE_LAST]+1);
                au_do_window(param, PSND_HN);
            }
        |   HM 
            {
                au_set_window_size(param, 0, 0);
                au_do_window(param, PSND_HM);
            }
        |   HM full_size_range
            {
                au_set_window_size(param, $2.range[RANGE_FIRST]+1, $2.range[RANGE_LAST]+1);
                au_do_window(param, PSND_HM);
            }
        |   CD exp
            {
                au_set_line_broadening(param, $2.value.var);
                au_set_window_size(param, 0, 0);
                au_do_window(param, PSND_CD);
            }
        |   CD exp ',' full_size_range
            {
                au_set_line_broadening(param, $2.value.var);
                au_set_window_size(param, $4.range[RANGE_FIRST]+1, $4.range[RANGE_LAST]+1);
                au_do_window(param, PSND_CD);
            }
        |   EM exp
            {
                au_set_line_broadening(param, $2.value.var);
                au_set_window_size(param, 0, 0);
                au_do_window(param, PSND_EM);
            }
        |   EM exp ',' full_size_range
            {
                au_set_line_broadening(param, $2.value.var);
                au_set_window_size(param, $4.range[RANGE_FIRST]+1, $4.range[RANGE_LAST]+1);
                au_do_window(param, PSND_EM);
            }
        |   GM exp ',' exp
            {
                au_set_line_broadening(param, $2.value.var);
                au_set_gaussian_fraction(param, $4.value.var);
                au_set_window_size(param, 0, 0);
                au_do_window(param, PSND_GM);
            }
        |   GM exp ',' exp ',' full_size_range
            {
                au_set_line_broadening(param, $2.value.var);
                au_set_gaussian_fraction(param, $4.value.var);
                au_set_window_size(param, $6.range[RANGE_FIRST]+1, $6.range[RANGE_LAST]+1);
                au_do_window(param, PSND_GM);
            }
        |   TM exp
            {
                au_set_points_for_trapezian_mul(param,$2.value.var);
                au_set_window_size(param, 0, 0);
                au_do_window(param, PSND_TM);
            }
        |   TM exp ',' full_size_range
            {
                au_set_points_for_trapezian_mul(param,$2.value.var);
                au_set_window_size(param, $4.range[RANGE_FIRST]+1, $4.range[RANGE_LAST]+1);
                au_do_window(param, PSND_TM);
            }
        |   WB
            {
                au_set_window_size(param, 0, 0);
                au_do_window(param, PSND_WB);
            }
        |   WB full_size_range
            {
                au_set_window_size(param, $2.range[RANGE_FIRST]+1, $2.range[RANGE_LAST]+1);
                au_do_window(param, PSND_WB);
            }
        |   PK
            {
                au_do_phase_correction(param);
            }
        |   APK
            {
                au_do_auto_phase_correction(param);
            }
        |   PK exp
            {
                au_set_phase0(param,-1, -1, $2.value.var);
                au_do_phase_correction(param);
            }
        |   PK exp ',' exp
            {
                au_set_phase0(param,-1, -1, $2.value.var);
                au_set_phase1(param,-1, -1, $4.value.var);
                au_do_phase_correction(param);
            }
        |   PK exp ',' exp ',' exp
            {
                au_set_phase0(param,-1, -1, $2.value.var);
                au_set_phase1(param,-1, -1, $4.value.var);
                au_set_phase_pos(param,-1, -1, $6.value.var);
                au_do_phase_correction(param);
            }
        |   PN
            {
                au_normalize_phase(param,1);
            }
        |   PN exp
            {
                au_normalize_phase(param,$2.value.var);
            }
        |   PZ
            {
                au_set_phase0(param,-1,-1, 0.0);
                au_set_phase1(param,-1,-1, 0.0);
                au_set_phase_pos(param,-1,-1, 1);
            }
        |   BC baseline_mode
            {
                au_set_baseline_mode(param, $2.value.btype);
                au_set_baseline_range(param, 0, 0);
                au_do_baseline_correction(param);
            }
        |   BC baseline_mode ',' full_size_range
            {
                au_set_baseline_mode(param, $2.value.btype);
                au_set_baseline_range(param, $4.range[RANGE_FIRST]+1, $4.range[RANGE_LAST]+1);
                au_do_baseline_correction(param);
            }
        |   BC baseline_mode ',' full_size_range ',' full_size_range
            {
                au_set_baseline_mode(param, $2.value.btype);
                au_set_baseline_range(param, $4.range[RANGE_FIRST]+1, $6.range[RANGE_LAST]+1);
                au_set_baseline_water(param, $4.range[RANGE_LAST]+1, $6.range[RANGE_FIRST]+1);
                au_do_baseline_correction(param);
            }
        |   BC baseline_mode ',' exp 
            {
                au_set_baseline_mode(param, $2.value.btype);
                au_set_baseline_terms(param, $4.value.var);
                au_set_baseline_range(param, 0, 0);
                au_do_baseline_correction(param);
            }
        |   BC baseline_mode ',' exp ',' full_size_range
            {
                au_set_baseline_mode(param, $2.value.btype);
                au_set_baseline_terms(param, $4.value.var);
                au_set_baseline_range(param, $6.range[RANGE_FIRST]+1, $6.range[RANGE_LAST]+1);
                au_do_baseline_correction(param);
            }
        |   BC baseline_mode ',' exp ',' full_size_range ',' full_size_range
            {
                au_set_baseline_mode(param, $2.value.btype);
                au_set_baseline_terms(param, $4.value.var);
                au_set_baseline_range(param, $6.range[RANGE_FIRST]+1, $8.range[RANGE_LAST]+1);
                au_set_baseline_water(param, $6.range[RANGE_LAST]+1, $8.range[RANGE_FIRST]+1);
                au_do_baseline_correction(param);
            }
        |   BC baseline_mode ',' exp ',' exp
            {
                au_set_baseline_mode(param, $2.value.btype);
                au_set_baseline_terms2(param, $4.value.var);
                au_set_baseline_terms(param, $6.value.var);
                au_set_baseline_range(param, 0, 0);
                au_do_baseline_correction(param);
            }
        |   BC baseline_mode ',' exp ',' exp ',' full_size_range
            {
                au_set_baseline_mode(param, $2.value.btype);
                au_set_baseline_terms2(param, $4.value.var);
                au_set_baseline_terms(param, $6.value.var);
                au_set_baseline_range(param, $8.range[RANGE_FIRST]+1, $8.range[RANGE_LAST]+1);
                au_do_baseline_correction(param);
            }
        |   BC baseline_mode ',' exp ',' exp ',' full_size_range ',' full_size_range
            {
                au_set_baseline_mode(param, $2.value.btype);
                au_set_baseline_terms2(param, $4.value.var);
                au_set_baseline_terms(param, $6.value.var);
                au_set_baseline_range(param, $8.range[RANGE_FIRST]+1, $10.range[RANGE_LAST]+1);
                au_set_baseline_water(param, $8.range[RANGE_LAST]+1, $10.range[RANGE_FIRST]+1);
                au_do_baseline_correction(param);
            }
        |   WATERFIT exp
            {
                au_set_waterfit_pos(param, (int)$2.value.var);
                if (!au_do_waterfit(param)) {
                    yyerror("Error in WATERFIT");
                    YYABORT;
                }
            }
        |   WATERFIT exp ',' exp
            {
                au_set_waterfit_pos(param, (int)$2.value.var);
                au_set_waterfit_wid(param, (int)$4.value.var);
                if (!au_do_waterfit(param)) {
                    yyerror("Error in WATERFIT");
                    YYABORT;
                }
            }
        |   WATWA exp ',' exp
            {
                au_set_watwa_power(param, $2.value.var);
                au_set_watwa_convolution_width(param, $4.value.var);
                if (!au_do_watwa(param)) {
                    yyerror("Error in WATWA");
                    YYABORT;
                }
            }
        |   WATWA exp ',' exp ',' exp
            {
                au_set_watwa_power(param, $2.value.var);
                au_set_watwa_convolution_width(param, $4.value.var);
                au_set_watwa_shift(param, $6.value.var);
                if (!au_do_watwa(param)) {
                    yyerror("Error in WATWA");
                    YYABORT;
                }
            }
        |   predict string ',' exp ',' exp
            {
                int newsize;
                au_set_lpc_future_size(param, $6.value.var);
                au_set_lpc_range(param, 0, 0);
                newsize = au_do_linear_prediction(param, $2.value.str, $4.value.var); 
                if (newsize == 0) {
                    yyerror("Error in linear prediction");
                    YYABORT;
                }
                au_set_size(param,-1, newsize);
            }
        |   predict string ',' exp ',' exp ',' full_size_range
            {
                int newsize;
                au_set_lpc_future_size(param, $6.value.var);
                au_set_lpc_range(param, $8.range[RANGE_FIRST]+1, $8.range[RANGE_LAST]+1);
                newsize = au_do_linear_prediction(param, $2.value.str, $4.value.var); 
                if (newsize == 0) {
                    yyerror("Error in linear prediction");
                    YYABORT;
                }
                au_set_size(param,-1, newsize);
            }
        |   predict string ',' exp ',' full_size_range
            {
                int newsize;
                au_set_lpc_gaprange(param, $6.range[RANGE_FIRST]+1, $6.range[RANGE_LAST]+1);
                newsize = au_do_linear_prediction(param, $2.value.str, $4.value.var); 
                if (newsize == 0) {
                    yyerror("Error in linear prediction");
                    YYABORT;
                }
                au_set_size(param,-1, newsize);
            }
        |   predict string ',' exp ',' full_size_range ',' full_size_range
            {
                int newsize;
                au_set_lpc_gaprange(param, $6.range[RANGE_FIRST]+1, $6.range[RANGE_LAST]+1);
                au_set_lpc_range(param, $8.range[RANGE_FIRST]+1, $8.range[RANGE_LAST]+1);
                newsize = au_do_linear_prediction(param, $2.value.str, $4.value.var); 
                if (newsize == 0) {
                    yyerror("Error in linear prediction");
                    YYABORT;
                }
                au_set_size(param,-1, newsize);
            }
        |   ROPEN string
            {
                if (!au_get_file(param, $2.value.str)) {
                    yyerror("Can not open file");
                    YYABORT;
                }
            }
        |   WREOPEN string
            {
                char *names[MAX_ACQU_FILES+1];
                names[0] = $2.value.str;
                if (!au_open_outputfile(param, 1, names, OLDFILE)) {
                    yyerror("Can not open file");
                    YYABORT;
                }
                outfile_open_status = TRUE;
            }
        |   WOPEN string
            {
                char *names[MAX_ACQU_FILES+1];
                names[0] = $2.value.str;
                if (!au_open_outputfile(param, 1, names, NEWFILE)) {
                    yyerror("Can not open file");
                    YYABORT;
                }
                outfile_open_status = TRUE;
            }
        |   WOPEN string ',' range_list
            {
                int i,j,k,dim;
                char *names[MAX_ACQU_FILES+1];

                names[0] = $2.value.str;
                for (i=1,dim=1;i<$4.value.rlist->num;i+=3) {
                    if ($4.value.rlist->list[i] == UNDETERMINED) 
                        $4.value.rlist->list[i] = 0;
                    if ($4.value.rlist->list[i+1] == UNDETERMINED) 
                        $4.value.rlist->list[i+1] = au_get_full_range(param,dim)-1;
                    $4.value.rlist->list[i]++;
                    $4.value.rlist->list[i+1]++;
                    dim++;
                }
                if (!au_set_ranges_outfile(param, $4.value.rlist->num,
                                           $4.value.rlist->list,
                                           TRUE)) {
                    yyerror( "Dimensions output file are too large");
                    YYABORT;
                }
                if (!au_open_outputfile(param, 1, names, NEWFILE)) {
                    yyerror("Can not open file");
                    YYABORT;
                }
                outfile_open_status = TRUE;
            }
        |   CREATE string_list ',' range_list
            {
                int i,j,k,dim;
                char *names[MAX_ACQU_FILES+1];

                for (k=0;k<=MAX_ACQU_FILES && k<$2.value.mlist->num;k++) {
                    names[k] = $2.value.mlist->value[k].s;
                }
                if (k==0) {
                    yyerror("No filename given");
                    YYABORT;
                }
                for (i=1,dim=1;i<$4.value.rlist->num;i+=3) {
                    if ($4.value.rlist->list[i] == UNDETERMINED) 
                        $4.value.rlist->list[i] = 0;
                    if ($4.value.rlist->list[i+1] == UNDETERMINED) 
                        $4.value.rlist->list[i+1] = au_get_full_range(param,dim)-1;
                    $4.value.rlist->list[i]++;
                    $4.value.rlist->list[i+1]++;
                    dim++;
                }
                au_set_ranges_outfile(param, $4.value.rlist->num,
                                      $4.value.rlist->list,
                                      TRUE);
                if (!au_open_outputfile(param, k,names, CREATEFILE)) {
                    yyerror("Can not open file");
                    YYABORT;
                }
                outfile_open_status = TRUE;
            }
        |   OTRUNC FLOAT2
            {
                au_set_outfile_truncation(param, 2, FALSE, 0.0, 0.0, 0.0);
            }
        |   OTRUNC FLOAT3
            {
                au_set_outfile_truncation(param, 3, FALSE, 0.0, 0.0, 0.0);
            }
        |   OTRUNC FLOAT2 ','  exp ',' exp ',' exp 
            {
                au_set_outfile_truncation(param, 2, TRUE, $4.value.var, 
                                          $6.value.var, $8.value.var);
            }
        |   OTRUNC FLOAT3 ','  exp ',' exp ',' exp 
            {
                au_set_outfile_truncation(param, 3, TRUE, $4.value.var, 
                                          $6.value.var, $8.value.var);
            }
        |   OTRUNC exp ',' exp ',' exp 
            {
                au_set_outfile_truncation(param, 4, TRUE, $2.value.var, 
                                          $4.value.var, $6.value.var);
            }
        |   DR float_list
            {
                if ($2.value.flist->num == 0 ||
                    !au_set_direction(param, $2.value.flist->num,  
                                      $2.value.flist->list)) {
                    yyerror("Can not set direction");
                    YYABORT;
                }
            }
        |   RCLOSE
            {
                au_close_infile(param);
            }
        |   WCLOSE
            {
                if (!au_close_outfile(param)) {
                    yyerror("Can not close output file");
                    YYABORT;
                }
                outfile_open_status = FALSE;
            }
        |   RN float_list
            {
                au_set_record_read_range(param,0, 0);
                if (!au_read_next_record(param,$2.value.flist->num,  
                                         $2.value.flist->list)) {
                    yyerror("Can not read record");
                    YYABORT;
                }
            }
        |   RN float_list ',' full_size_range
            {
                au_set_record_read_range(param,$4.range[RANGE_FIRST]+1, $4.range[RANGE_LAST]+1);
                if (!au_read_next_record(param,$2.value.flist->num,  
                                         $2.value.flist->list)) {
                    yyerror("Can not read record");
                    YYABORT;
                }
            }
        |   RN full_size_range
            {
                au_set_record_read_range(param,$2.range[RANGE_FIRST]+1, $2.range[RANGE_LAST]+1);
                if (!au_read_next_record(param,0,NULL)) {
                    yyerror("Can not read record");
                    YYABORT;
                }
            }
        |   RTR
            {
                if (!au_return_record_size_check(param)) {
                    yyerror("Record has wrong size");
                    YYABORT;
                }
                if (!au_return_record(param)) {
                    yyerror("Can not return record");
                    YYABORT;
                }
            }
        |   RW  float_list
            {
                if (!au_write_record(param,$2.value.flist->num,  
                                     $2.value.flist->list)) {
                    yyerror("Can not write record");
                    YYABORT;
                }
            }
        |   RW  float_list ',' full_size_range
            {
                au_set_record_write_range(param,$4.range[RANGE_FIRST]+1, $4.range[RANGE_LAST]+1);
                if (!au_write_record(param,$2.value.flist->num,  
                                     $2.value.flist->list)) {
                    yyerror("Can not write record");
                    YYABORT;
                }
            }
        |   RW  full_size_range
            {
                au_set_record_write_range(param,$2.range[RANGE_FIRST]+1, $2.range[RANGE_LAST]+1);
                if (!au_write_record(param,0, NULL)) {
                    yyerror("Can not write record");
                    YYABORT;
                }
            }
        |   SPLINE READ ',' string
            {
                if (!au_spline_read(param,$4.value.str)) {
                    yyerror("Can not read spline data");
                    YYABORT;
                }
            }
        |   SPLINE CALC
            {
                if (!au_spline_calc(param)) {
                    yyerror("Can not calc spline");
                    YYABORT;
                }
            }
        |   HT
            {
                au_do_hilbert_transform(param);
            }
        |   AV
            {
                au_array_average(param);
            }
        |   PS
            {
                au_array_power(param);
            }
        |   BLOCK
            {
                au_select_block(param,$1.value.block->id);
            }
        |   CO exp
            {
                if (!au_set_contour_mode(param,$2.value.var)) {
                    yyerror("Can not set 2D mode");
                    YYABORT;
                }
            }
        |   CP
            {
                if (!au_plot_2d(param,NULL, 0.0, 0.0, 0.0, 0.0)) {
                    yyerror("Can not do 2D plot");
                }
            }
        |   CP exp ',' exp ',' exp ',' exp
            {
                if (!au_plot_2d(param,NULL, $2.value.var, $4.value.var, 
                                $6.value.var, $8.value.var)) {
                    yyerror("Can not do 2D plot");
                }
            }
        |   CP string
            {
                if (!au_plot_2d(param,$2.value.str, 0.0, 0.0, 0.0, 0.0)) {
                    yyerror("Can not open plot file, or other error");
                    YYABORT;
                }
            }
        |   CP exp ',' exp ',' exp ',' exp ',' string
            {
                if (!au_plot_2d(param,$10.value.str, $2.value.var, $4.value.var, 
                                $6.value.var, $8.value.var)) {
                    yyerror("Can not open plot file, or other error");
                    YYABORT;
                }
            }
        |   PL
            {
                if (!au_plot_1d(param,NULL, 0.0, 0.0, 0.0, 0.0)) {
                    yyerror("Can not do 1D plot");
                }
            }
        |   PL exp ',' exp
            {
                if (!au_plot_1d(param,NULL, $2.value.var, $4.value.var,
                                                              0.0, 0.0)) {
                    yyerror("Can not do 1D plot");
                }
            }
        |   PL exp ',' exp ',' exp ',' exp
            {
                if (!au_plot_1d(param,NULL, $2.value.var, $4.value.var, 
                                $6.value.var, $8.value.var)) {
                    yyerror("Can not do 1D plot");
                }
            }
        |   PL string
            {
                if (!au_plot_1d(param,$2.value.str, 0.0, 0.0, 0.0, 0.0)) {
                    yyerror("Can not open plot file, or other error");
                    YYABORT;
                }
            }
        |   PL exp ',' exp ',' string
            {
                if (!au_plot_1d(param,$6.value.str, $2.value.var, $4.value.var,
                                                              0.0, 0.0)) {
                    yyerror("Can not open plot file, or other error");
                    YYABORT;
                }
            }
        |   PL exp ',' exp ',' exp ',' exp ',' string
            {
                if (!au_plot_1d(param,$10.value.str, $2.value.var, $4.value.var, 
                                $6.value.var, $8.value.var)) {
                    yyerror("Can not open plot file, or other error");
                    YYABORT;
                }
            }
        |   PLOTPAR string
            {
                if (!au_set_plot_parameters(param,$2.value.str)) {
                    yyerror("Error in plot parameters");
                    YYABORT;
                }
            }
        |   RPLOTPAR string
            {
                if (!au_read_plot_parameters(param,$2.value.str)) {
                    yyerror("Can not open plot parameter file");
                    YYABORT;
                }
            }
        |   WPLOTPAR string
            {
                if (!au_write_plot_parameters(param,$2.value.str)) {
                    yyerror("Can not create plot parameter file");
                    YYABORT;
                }
            }
        |   INTEGRAL OFFSET exp
            {
                au_integral_set_offset_correction(param, $3.value.var);
            }
        ;
        


     /* End of grammar */
     %%
/*#define NOMEMSHELL*/

#include "nmrtool.h"
#include "genplot.h"
#include "psnd.h"
#include "hash.h"


static char *pp;

static cache_ptr hash_id;
static int linecount,stmcount;
static char *scriptname, *linestart;


/*
 * convert string to lower case
 */
char *strlwr(char *s)
{
    char *p;

    p = s;
    while (*p != '\0') {
        *p = tolower(*p);
        p++;
    }
    return s;
}

void yy_free(void *ptr)
{
    free(ptr);
}

gentype *pop_yylval_prev(void);

static MBLOCK *mblk_bison;

#define YERRLSIZE	100
int yyerror (char *s)  /* Called by yyparse on error */
{
    char pp[YERRLSIZE+3];
    int i,j,len;
    gentype yy,*lvalp;
    MBLOCK *mblk = mblk_bison;

    /*
     * get last yylval
     */
    lvalp = pop_yylval_prev();
    memcpy(&yy, lvalp, sizeof(gentype));
    lvalp = &yy;
    
    if (lvalp == 0x0 || lvalp->linestart == 0x0) {
        psnd_printf (mblk,"line ?: %s\n", s);
        return 0;
    }

    /*
     * Copy offending line in pp
     */
    for (len=0,j=0;len<YERRLSIZE && lvalp->linestart[j] && 
                            lvalp->linestart[j] != '\n';len++,j++) {
        pp[len] = lvalp->linestart[j];
        if (pp[len] == '%')
            pp[++len] = '%';
    }
    pp[len]=0;
    /*
     * Print pp
     */
    psnd_printf (mblk,"\n%s\n",pp);
    j = lvalp->pp - lvalp->linestart;
    if (j < 0)
        j = 0;
    /*
     * Print a marker where the error is
     */
    if (j < len) {
        for (i=0;i<j;i++)
            pp[i] = '-';
        pp[i++] = '^';
        pp[i]   = 0;
        psnd_printf (mblk,"%s\n",pp);
    }
    /*
     * Print (script name/)line number and error message
     */
    if (lvalp->scriptname[0] == '\0')
        psnd_printf (mblk,"Line %d: %s\n", lvalp->line, s);
    else
        psnd_printf (mblk,"%s, line %d: %s\n", 
                      lvalp->scriptname, lvalp->line, s);
    return 0;
}

void au_yyerror (YYSTYPE *lvalp, void *param, char *s)  /* Called by yyparse on error */
{
    char pp[YERRLSIZE+3];
    int i,j,len;
    MBLOCK *mblk = (MBLOCK *)param;

    /*
     * Copy offending line in pp
     */
    for (len=0,j=0;len<YERRLSIZE && lvalp->linestart[j] && 
                            lvalp->linestart[j] != '\n';len++,j++) {
        pp[len] = lvalp->linestart[j];
        if (pp[len] == '%')
            pp[++len] = '%';
    }
    pp[len]=0;
    /*
     * Print pp
     */
    psnd_printf (mblk,"\n%s\n",pp);
    j = lvalp->pp - lvalp->linestart;
    if (j < 0)
        j = 0;
    /*
     * Print a marker where the error is
     */
    if (j < len) {
        for (i=0;i<j;i++)
            pp[i] = '-';
        pp[i++] = '^';
        pp[i]   = 0;
        psnd_printf (mblk,"%s\n",pp);
    }
    /*
     * Print (script name/)line number and error message
     */
    if (lvalp->scriptname[0] == '\0')
        psnd_printf (mblk,"Line %d: %s\n", lvalp->line, s);
    else
        psnd_printf (mblk,"%s, line %d: %s\n", 
                      lvalp->scriptname, lvalp->line, s);
}
     
struct func_init
{
    char    *fname;
    double (*fnct)();
    int     type;
};

static double yy_rnd(double f)
{
    if (f < 0.0) 
        return ceil(f-0.5);
    return floor(f+0.5);
}

static double yy_trnc(double f)
{
    if (f < 0.0) 
        return ceil(f);
    return floor(f);
}

static double yy_frct(double f)
{
    double g;
    return modf(f,&g);
}

static struct func_init arith_fncts[] = {
    { "sin",  	sin ,  	FNCT   	},
    { "asin", 	asin,  	FNCT   	},
    { "cos",  	cos,   	FNCT   	},
    { "acos", 	acos,  	FNCT   	},
    { "tan",  	tan,   	FNCT   	},
    { "atan", 	atan,  	FNCT   	},
    { "atan2", 	atan2, 	FNCT2  	},
    { "ln",   	log,   	FNCT   	},
    { "log",  	log10, 	FNCT   	},
    { "exp",  	exp,   	FNCT   	},
    { "sqrt", 	sqrt,  	FNCT   	},
    { "abs",  	fabs,  	FNCT   	},
    { "floor",  floor, 	FNCT   	},
    { "ceil",  	ceil,  	FNCT   	},
    { "trunc", 	yy_trnc,FNCT   	},
    { "round", 	yy_rnd,	FNCT   	},
    { "fract", 	yy_frct,FNCT   	},
    { "mod",  	fmod,  	FNCT2  	},
    { NULL,   	NULL, 	0 	}
};
     

typedef struct {
    char *s;
    int  i;
} KEYWORD_TYPE;

static KEYWORD_TYPE keywords[] = {
    { "argc", 		ARGC  		},
    { "argv", 		ARGV		},
    { "sizeof",		SIZEOF		},
    { "if", 		IF  		},
    { "then",		THEN  		},
    { "else",		ELSE  		},
    { "elseif",		ELSEIF 		},
    { "endif",		ENDIF  		},
    { "for",		FOR   		},
    { "in",		IN   		},
    { "sorted",		SORTED   	},
    { "function",	FUNCTION	},
    { "function_def",	FUNCTION_DEF	},
    { "end",		END	   	},
    { "return",		RETURN	   	},
    { "local",		LOCAL	   	},
    { "print",		PRINT  		},
    { "println",	PRINTLN  	},
    { "printf",		PRINTF  	},
    { "fprintf",	FPRINTF  	},
    { "sprintf",	SPRINTF  	},
    { "fgets",		FGETS	  	},
    { "co",		CO	  	},
    { "cp",		CP	  	},
    { "pl",		PL	  	},
    { "plotpar",	PLOTPAR  	},
    { "rplotpar",	RPLOTPAR  	},
    { "wplotpar",	WPLOTPAR  	},
    { "while",		WHILE  		},
    { "do",		DO  		},
    { "done",		DONE  		},
    { "break",		BREAK  		},
    { "exit",		EXIT  		},
    { "continue",	CONTINUE	},
    { "unpack4",	UNPACK4		},
    { "unpack6",	UNPACK6		},
    { "unpack8",	UNPACK8		},
    { "unpacki",	UNPACKI		},
    { "unpackf",	UNPACKF		},
    { "unpackb",	UNPACKB		},
    { "unpack0123",	UNPACK0123	},
    { "unpack3210",	UNPACK3210	},
    { "unpack0321",	UNPACK0321	},
    { "unpack1230",	UNPACK1230	},
    { "negstep",	NEGSTEP		},
    { "swap4",		SWAP4  		},
    { "swap2",		SWAP2  		},
    { "i2f",		I2F  		},
    { "f2i",		F2I  		},
    { "f2ascii",	F2ASCII		},
    { "c2ri",		C2RI  		},
    { "c2ir",		C2IR  		},
    { "open",		OPEN  		},
    { "close",		CLOSE  		},
    { "read",		READ  		},
    { "write",		WRITE  		},
    { "seek",		SEEK  		},
    { "tell",		TELL  		},
    { "scan",		SCAN  		},
    { "scanexp",	SCANEXP		},
    { "scanbin",	SCANBIN		},
    { "stat",		STAT  		},
    { "chdir",		CHDIR  		},
    { "getcwd",		GETCWD 		},
    { "trim", 		TRIM	  	},
    { "token", 		STRTOK	  	},
    { "upcase", 	UPCASE  	},
    { "lowcase", 	LOWCASE  	},
    { "index", 		STRINGINDEX  	},
    { "lastindex", 	STRINGLASTINDEX },
    { "stdin", 		STDIN	  	},
    { "stdout", 	STDOUT	  	},
    { "stderr", 	STDERR	  	},
    { "byteorder", 	BYTEORDER	},
    { "initparam", 	INITPARAM	},
    { "exec", 		EXEC		},
    { "putenv",		PUTENV		},
    { "getenv",		GETENV		},
    { "sf", 		SF  		},
    { "sw", 		SW  		},
    { "si", 		SI  		},
    { "td", 		TD  		},
    { "ift", 		IFT  		},
    { "ft", 		FT  		},
    { "dft", 		DFT  		},
    { "ftscale", 	FTSCALE		},
    { "pa", 		PA  		},
    { "pb", 		PB  		},
    { "i0", 		I0  		},
    { "pk", 		PK  		},
    { "apk", 		APK  		},
    { "pn", 		PN  		},
    { "pz", 		PZ  		},
    { "xr", 		XR  		},
    { "aref", 		AREF  		},
    { "bref", 		BREF 		},
    { "sizen", 		SIZEN		},
    { "sn", 		SN  		},
    { "sq", 		SQ  		},
    { "ks", 		KS  		},
    { "hn", 		HN  		},
    { "hm", 		HM  		},
    { "cd", 		CD  		},
    { "em", 		EM  		},
    { "gm", 		GM  		},
    { "tm", 		TM  		},
    { "wb", 		WB  		},
    { "bc", 		BC  		},
    { "watwa", 		WATWA  		},
    { "waterfit", 	WATERFIT	},
    { "lpc", 		LPC  		},
    { "hsvd", 		HSVD  		},
    { "ge", 		GE  		},
    { "rn", 		RN  		},
    { "rw", 		RW  		},
    { "dr", 		DR  		},
    { "sm", 		SM  		},
    { "rm", 		RM  		},
    { "dim", 		DIM  		},
    { "ropen",		ROPEN	  	},
    { "rclose",		RCLOSE		},
    { "wreopen",	WREOPEN	  	},
    { "wopen",		WOPEN	  	},
    { "create",		CREATE 		},
    { "wclose",		WCLOSE		},
    { "rd", 		RD  		},
    { "rtr", 		RTR 		},
    { "wr", 		WR  		},
    { "otrunc",		OTRUNC  	},
    { "float2",		FLOAT2  	},
    { "float3",		FLOAT3  	},
    { "spline",		SPLINE  	},
    { "calc",		CALC  		},
    { "ht",		HT  		},
    { "rv",		RV  		},
    { "tp",		TP  		},
    { "poly",		POLY  		},
    { "tabl",		TABL  		},
    { "cosine",		COSINE  	},
    { "sine",		SINE  		},
    { "sincos",		SINCOS 		},
    { "polyw",		POLYW  		},
    { "tablw",		TABLW  		},
    { "cosinew",	COSINEW  	},
    { "sinew",		SINEW  		},
    { "sincosw",	SINCOSW		},
    { "splinew",	SPLINEW		},
    { "av",		AV  		},
    { "ps",		PS  		},
    { "isspec",		ISSPEC 		},
    { "dspshift",	DSPSHIFT	},
    { "integral",	INTEGRAL	},
    { "offset",		OFFSET		},
    { "rmsnoise",	RMSNOISE	},
    { "peakpick",	PEAKPICK1D	},
    { "uconv",		UCONV		},
    { "max",		MAXFUNC		},
    { "min",		MINFUNC		},
    { "", 		-1 		}
};

static KEYWORD_TYPE keyword_symbols[] = {
     { "<=", 	LE  	},
     { ">=", 	GE  	},
     { "==", 	EQ  	},
     { "!=", 	NE  	},
     { "&&", 	AND 	},
     { "||", 	OR  	},
     { "=",  	'=' 	},
     { "+=", 	ADD 	},
     { "-=", 	SUB 	},
     { "*=", 	MUL 	},
     { "/=", 	DIV 	},
     { "|<", 	ROL 	},
     { ">|", 	ROR 	},
     { "<<", 	LS 	},
     { ">>", 	RS 	},
     { "..", 	DOTDOT 	},
     { "", 	-1 	}
};


#define ARRAYS_IN_BLK	8
static ARRAY_TYPE psnd_array_blocks[MAXBLK * ARRAYS_IN_BLK];
static ARRAY_TYPE *psnd_arrays;

static int   array_size[MAXBLK];
static char *array_names[] = {    
    "@r", "@i", "@c", "@a", "@b", "@w", "@l", NULL
};

/*
 * This part is a bit tricky
 * User may change 'blocks' while processing, so the current 'arrays'
 * must be remapped in that case.
 * Also, 'arrays' may be reallocated. Thus save pointer to 'array' pointer
 * and not just save pointer to the 'arrays'.
 * Also save pointer to 'size' instead of 'size' itself, as this may change too
 */
void init_arrays(MBLOCK *mblk)
{
    int i,j;
    
    for (i=0;i<MAXBLK;i++) {
        for (j=0;j<ARRAYS_IN_BLK-1;j++) {
            psnd_array_blocks[i * ARRAYS_IN_BLK + j].id  = j;
            strcpy(psnd_array_blocks[i * ARRAYS_IN_BLK + j].name, array_names[j]);
            psnd_array_blocks[i * ARRAYS_IN_BLK + j].len = &(mblk->dat[i]->isize);
            psnd_array_blocks[i * ARRAYS_IN_BLK + j].data2 = NULL;
        }
        psnd_array_blocks[i * ARRAYS_IN_BLK + j].id    = -1;
        psnd_array_blocks[i * ARRAYS_IN_BLK + 0].data  = &(mblk->dat[i]->xreal);
        psnd_array_blocks[i * ARRAYS_IN_BLK + 1].data  = &(mblk->dat[i]->ximag);
        psnd_array_blocks[i * ARRAYS_IN_BLK + 2].data  = &(mblk->dat[i]->xreal);
        psnd_array_blocks[i * ARRAYS_IN_BLK + 2].data2 = &(mblk->dat[i]->ximag);
        psnd_array_blocks[i * ARRAYS_IN_BLK + 3].data  = &(mblk->dat[i]->xbufr1);
        psnd_array_blocks[i * ARRAYS_IN_BLK + 4].data  = &(mblk->dat[i]->xbufr2);
        psnd_array_blocks[i * ARRAYS_IN_BLK + 5].data  = &(mblk->dat[i]->window);
        psnd_array_blocks[i * ARRAYS_IN_BLK + 6].data  = &(mblk->dat[i]->baseln);
    }
    psnd_arrays = psnd_array_blocks + mblk->info->block_id * ARRAYS_IN_BLK;
}


ARRAY_TYPE *current_array_block(void *param, int id)
{
    MBLOCK *mblk = (MBLOCK *) param;
    return psnd_array_blocks + mblk->info->block_id * ARRAYS_IN_BLK + id;
}


static BLOCK_TYPE psnd_blocks[] = {
    { "block1", 1, NULL },
    { "block2", 2, NULL },
    { "block3", 3, NULL },
    { "block4", 4, NULL },
    { "block5", 5, NULL },
    { "block6", 6, NULL },
    { "block7", 7, NULL },
    { "block8", 8, NULL },
    { "block9", 9, NULL },
    { "block10", 10, NULL },
    { "block11", 11, NULL },
    { "block12", 12, NULL },
    { NULL, -1, NULL },
};

void init_blocks()
{
    int i;
    
    for (i=0;i<MAXBLK;i++) {
        psnd_blocks[i].array_block = psnd_array_blocks + i * ARRAYS_IN_BLK;
    }
}

/* 
 * The symbol table: a chain of `struct symrec'.  
 */

/*
 * Init the hash table
 */
void init_table ()  /* puts arithmetic functions in table. */
{
    int i;
    symrec *ptr;
    for (i = 0; arith_fncts[i].fname != 0; i++)
    {
        ptr = putsym (arith_fncts[i].fname, arith_fncts[i].type);
        ptr->value.fnctptr = arith_fncts[i].fnct;
    }
}

/*
 * Store symbol in hash table
 */
symrec *putsym (char *sym_name, int sym_type)
{
    char tmp[256];
    symrec *ptr;
    
    /*
     * Case insenstive, convert to lower case
     */
    strcpy(tmp, sym_name);
    strlwr(tmp);
    /*
     * Test for presence in hash table
     */
    ptr = (symrec*) 
        hash_value_for_key (hash_id, (const void *) tmp);
    if (!ptr) {
        /*
         * If not, add to hash table
         */
        ptr = (symrec *) malloc (sizeof (symrec));
        assert(ptr);
        ptr->name = (char *) strdup(tmp);
        assert(ptr->name);
        hash_add (&hash_id, (void*) ptr->name, (void *) ptr);
    }
    /*
     * Initialize data space
     */
    ptr->type          = sym_type;
    ptr->value.str     = NULL; 
    ptr->val_type      = 0; 
    ptr->val_float     = 0; 
    ptr->val_string[0] = '\0'; 
    ptr->file          = NULL; 
    ptr->sbt           = NULL; 
    return ptr;
}

/*
 * Get symbol from hash table
 */
symrec *getsym (char *sym_name)
{
    char tmp[256];
    /*
     * convert to lower case
     */
    strcpy(tmp, sym_name);
    strlwr(tmp);
    /*
     * return pointer to data, or NULL if not found
     */
    return (symrec*) hash_value_for_key (hash_id, (const void *) tmp);
}

/*
 * clean up the hash table and free memory
 */
void free_symbols_and_hashtable()
{
    node_ptr node;
    symrec *ptr;

    while ((node = hash_next (hash_id, NULL))) {
        ptr = (symrec*) node->value;
        assert(ptr);
        assert(node->key);        
        hash_remove (hash_id, node->key);
        free(ptr->name);
        free(ptr);
        
    }
    /* 
     * Release the array of nodes and the cache itself.  
     */
    free (hash_id->node_table);
    free (hash_id);
}

/************************************************************
 * linked list to store dynamically allocated strings
 */
static STRBUF_TYPE *string_space, *string_last;

#define TMP_STRING_SPACE_SIZE	1024
static char* tmp_string_space;
static int tmp_string_len, tmp_string_size;
static int mark_stm_count;

static void cleanup_tmp_string(int stm_count)
{
    tmp_string_len = 0;
    mark_stm_count = stm_count;
}

static void init_tmp_string()
{
    mark_stm_count   = 0;
    tmp_string_len   = 0;
    tmp_string_size  = TMP_STRING_SPACE_SIZE;
    tmp_string_space = (char*) malloc(sizeof(char) * tmp_string_size);
    assert(tmp_string_space);
}

static void free_tmp_string()
{
    if (tmp_string_space) {
        mark_stm_count   = 0;
        tmp_string_len   = 0;
        tmp_string_size  = 0;
        tmp_string_space = NULL;
    }
}

/*
 * Allocate a new string buffer, and put it in the
 * linked list for later cleanup
 */
static STRBUF_TYPE *newstb(void)
{
    STRBUF_TYPE *st;

        st    = (STRBUF_TYPE *) calloc(1,sizeof(STRBUF_TYPE));
        assert(st);
        st->next = NULL;
        if (string_space == NULL) 
            string_space = st;
        else
            string_last->next = st;
        string_last = st;
        st->s = NULL;
    return st;
}

/*
 * String allocation routine. saves strings in a linked list or
 *    in a temp string space.
 * 
 * s1 = first string to save.
 * s2 = possible second string to append to first one.
 * stm_count = statement count. This is for volatile strings
 *             that are saved in a temp space buffer.
 *             If the statement count is higher than the previous
 *             one, the old strings are no longer needed and
 *             the temp string space is reset.
 * sbt = string buffer type. This indicates a more permanent type of string,
 *       used in variables. Variables keep track of the location of their
 *       string buffers, so that these can be updated if needed. This location
 *       is given in *sbt. If *sbt == NULL, a new location is allocated and 
 *       returned in sbt.
 * If both stm_count and sbt are 0, than strings are allocated during the run 
 *     of the script.
 */
char *create_and_cat_string(char *s1, char *s2, int stm_count, 
                            STRBUF_TYPE **sbt, int list_size, int *list)
{
    int len, s1_len; 
    STRBUF_TYPE *st;

    len = 1;
    if (s1) {
        len += strlen(s1);
        /*
         * calculate sub-string if needed
         */
        if (list_size > 0) {
            int i;
            s1_len = 0;
            for (i=0;i<list_size;i++) {
                if (list[i] < 0)
                    list[i] = 0;
                else if (list[i] >= len)
                    list[i] = len-1;
                if (i%2)
                    s1_len += (list[i]-list[i-1]+1);
            }
            len = s1_len;
        }
        else
            s1_len = len;
        /*
        if (s1_start < 1 || s1_start > len)
            s1_start = 1;
        if (s1_stop  < s1_start || s1_stop  > len)
            s1_stop = len;
        s1_len = s1_stop - s1_start + 1;
        s1 += (s1_start - 1);
        len = s1_len;
        */
    }
    if (s2)
        len += strlen(s2);
    if (stm_count > 0) {
        /*
         * This is the string work space
         * Strings that are in scope only for one statement are kept here
         * Workspace is cleared if a new statement is announced by
         * stm_count
         */
        char *p;
        if (mark_stm_count != stm_count) 
            cleanup_tmp_string(stm_count);
        if (tmp_string_size < tmp_string_len + len + 1) 
            return NULL;
        p = tmp_string_space + tmp_string_len;
        p[0] = '\0';
        if (s1) {
            if (list_size) {
                int i;
                for (i=0;i<list_size;i+=2) {
                    int sublen = list[i+1]-list[i]+1;
                    strncat(p, s1 + list[i], sublen);
                }
            }
            else
                strncat(p, s1, s1_len);
        }
        if (s2)
            strcat(p, s2);
        tmp_string_len += len + 1;
        return p;
    }
    /*
     * sbt is a pointer to the storage buffer.
     * If a variable has already a storage buffer allocated
     * then we don't have to allocate a new one. We just
     * reallocate the string space of the buffer, so
     * make 'st' (= new storage buffer) equal '*sbt' ( = to old one )
     */
    if (sbt && *sbt && !((*sbt)->s == s1 && s2 != NULL)) {
        if ((*sbt)->s == s1) {
            (*sbt)->s[s1_len] = '\0';
            /*
             * Variable is just a copy of itself
             */
            return (*sbt)->s;
        }
        st = *sbt;
    }
    else {
        st    = (STRBUF_TYPE *) calloc(1,sizeof(STRBUF_TYPE));
        assert(st);
        st->next = NULL;
        if (string_space == NULL) 
            string_space = st;
        else
            string_last->next = st;
        string_last = st;
        st->s = NULL;
        /*
         * for a variable, also save the storage buffer
         */
        if (sbt) 
            *sbt = st;
    }

    st->s = (char*) realloc(st->s, len * sizeof(char));
    assert(st->s);
    st->s[0] = '\0';
    if (s1)
        strncat(st->s, s1, s1_len);
    if (s2)
        strcat(st->s, s2);
    return st->s;
}
    
void free_stringspace()
{
    STRBUF_TYPE *st1, *st2;
    st1 = st2 = string_space;
    while (st1) {
        st2 = st1->next;
        free(st1);
        st1 = st2;
    }
    string_space = NULL;
}

char *num_to_string(float f)
{
    static char s[40];
    sprintf(s,"%g", f);
    return strdup(s);
}


static char *strtok_char;
static int   strtok_pos;

static void init_strtok()
{
    strtok_char = NULL;
    strtok_pos  = 0;
}

static void free_strtok()
{
    if (strtok_char)
        free(strtok_char);
    init_strtok();
}

/*
 * Copy the input string, because it will be 
 * destroyed by au_strtok()
 */
static char *au_strtok(char *line, char *delim)
{
    static char empty[1];
    char *p;
    int i, j, delim_len;
    
    if (line) {
        strtok_char = (char*) realloc(strtok_char, strlen(line)+1);
        assert(strtok_char);
        strcpy(strtok_char, line);
        strtok_pos  = 0;
        p = strtok_char;
    }
    else if (strtok_char == NULL) {
        return empty;
    }
    else {
        p = strtok_char + strtok_pos;
    }
    delim_len = strlen(delim);
    if (*p == '\0' || delim_len == 0)
        return empty;
    /*
     * find start position
     */
    i=0;
    while (i<delim_len) {
        if (*p == delim[i]) {
            p++;
            strtok_pos++;
            if (*p == '\0')
                return empty;
            i = -1;
        }
        i++;
    }
    /*
     * find end position
     */
    for (j=0;p[j] != '\0';j++) {
        for (i=0;i<delim_len;i++) {
            if (p[j] == delim[i]) {
                p[j] = '\0';
                strtok_pos += j+1;
                return p;
            }
        }
    }
    strtok_pos += j;
    return p;
}

/*
 * Analyse a VAR or STRING and return a string
 */
char *get_string_from_var(symrec *tptr)
{
    if (!(tptr->val_type & IS_STRING)) {
        if (!(tptr->val_type & IS_FLOAT_STRING)) {
            sprintf(tptr->val_string,
                    "%g", 
                    tptr->val_float);
            tptr->val_type |= IS_FLOAT_STRING; 
        }
        tptr->value.str = tptr->val_string;   
   }
   return tptr->value.str;   
}

/*
 * Analyse a VAR or STRING and return a float
 */
float get_float_from_var(symrec *tptr)
{

    if (!(tptr->val_type & IS_FLOAT)) {
        if (!(tptr->val_type & IS_FLOAT_STRING)) {
            float f = 0.0;
            if (tptr->value.str != NULL)
                f = au_scan_float(tptr->value.str);
            tptr->val_float = f;
            tptr->val_type |= IS_FLOAT_STRING; 
        }
    }
    return tptr->val_float;
}

#define MAX	200
#define YY_EOF 	0

/****************************************************
 * Not realy a stack but more like a queue
 * A series of tokens from the lexical analyzer are saved
 * here. The parser goes through these tokens, starting
 * from the beginning. When the end is reached, the parser
 * is done.
 */


static gentype *yylval_stack;
static int yylval_p, yylval_pp, yylval_size;

void push_yylval(gentype *v)
{
    v->stackpos = yylval_p;
    memcpy(&(yylval_stack[yylval_p++]), v, sizeof (gentype));
    if (yylval_p >= yylval_size) {
        yylval_size *= 2;
        yylval_stack = (gentype*) 
            realloc(yylval_stack, yylval_size * sizeof (gentype));
        assert(yylval_stack);
    }
}

int peek_yylval_type()
{
    if (yylval_p==0)
        return -1;
    return yylval_stack[yylval_p-1].type;
}

gentype *pop_yylval()
{
    return &(yylval_stack[yylval_pp++]);
}

gentype *pop_yylval_prev()
{
    if (yylval_pp <= 0)
        return &(yylval_stack[0]);
    return &(yylval_stack[yylval_pp-1]);
}

void yylval_stackinit()
{
    yylval_p 	= 0;
    yylval_pp 	= 0;
    yylval_size = MAX;
    yylval_stack = (gentype*) 
        realloc(yylval_stack, yylval_size * sizeof (gentype));
    assert(yylval_stack);
}

void yylval_restore(int p)
{
    yylval_pp = p;
}

/*
 * Variable seems to be a function after all.
 * Update all previous VAR definitions
 */
void yylval_change_type(symrec *g, int oldtype, int newtype)
{

    int i;
    for (i=0;i<yylval_p;i++)
        if (yylval_stack[i].type == oldtype 
                && yylval_stack[i].value.tptr == g)
                    yylval_stack[i].type = newtype;
}


/*
 * At this point there are no more reallocs for 'yylval_stack'
 * So now do all pointer calculations
 */
void yylval_set_pointers()
{

    int i;
    /*
     * Pointer to the record itself.
     * Wen copies are made of this record, this pointer still
     * points to the original record, that is saved on stack.
     */
    for (i=0;i<yylval_p;i++)
        yylval_stack[i].self = &(yylval_stack[i]);
}


/*
 * Free memory that might remain after abort
 */
void yylval_cleanup()
{
    int i;
    
    for (i=0;i<yylval_p;i++)
        if (yylval_stack[i].rps) {
            free(yylval_stack[i].rps);
            yylval_stack[i].rps = NULL;
        }
    free(yylval_stack);
    yylval_size = 0;
    yylval_stack = NULL;
}

/************************************************************
 * stacks for jump adresses
 */
typedef struct {
    int *stack;
    int pos, size;
} JUMP_STACK_TYPE;

JUMP_STACK_TYPE *jmp1, *jmp2, *jmp3, *jmp4, *jmp5;

void push_jump(JUMP_STACK_TYPE *jmp, int v)
{
    if (jmp->pos >= jmp->size) {
        jmp->size *= 2;
        jmp->stack = 
            (int*) realloc(jmp->stack, sizeof(int) * jmp->size);
        assert(jmp->stack);
    }
    jmp->stack[jmp->pos++] = v;
}

int pop_jump(JUMP_STACK_TYPE *jmp)
{
    if (jmp->pos == 0)
        return -1;
    return jmp->stack[--(jmp->pos)];
}

void addto_jump(JUMP_STACK_TYPE *jmp, int v)
{
    if (jmp->pos > 0) 
        jmp->stack[jmp->pos-1] += v;
}

JUMP_STACK_TYPE *jump_stackinit()
{
    JUMP_STACK_TYPE *jmp;
    jmp = (JUMP_STACK_TYPE *) malloc(sizeof(JUMP_STACK_TYPE ));
    assert(jmp);
    jmp->size = MAX;
    jmp->stack = (int*) malloc(sizeof(int) * jmp->size);
    assert(jmp->stack);
    jmp->pos = 0;
    return jmp;
}

int jump_stackempty(JUMP_STACK_TYPE *jmp)
{
    return !jmp->pos;
}

void jump_stackfree(JUMP_STACK_TYPE *jmp)
{
    free(jmp->stack);
    free(jmp);
}

/*
 * dynamic
 */
void push_jump_stack(int v)
{
    JUMP_STACK_TYPE *jmp = jmp4;
    if (jmp->pos >= jmp->size) {
        jmp->size *= 2;
        jmp->stack = 
            (int*) realloc(jmp->stack, sizeof(int) * jmp->size);
        assert(jmp->stack);
    }
    jmp->stack[jmp->pos++] = v;
}

/*
 * dynamic
 */
int pop_jump_stack()
{
    JUMP_STACK_TYPE *jmp = jmp4;
    if (jmp->pos == 0)
        return -1;
    return jmp->stack[--(jmp->pos)];
}

/************************************************************
 * stacks for argument pointers
 */
 
typedef struct {
    gentype *g;
    symrec *s_new,*s_old;
    int count,scount,size,ssize;
} ARGINFO_TYPE;

typedef struct {
    ARGINFO_TYPE *stack;
    int pos, size;
} ARG_STACK_TYPE;

ARG_STACK_TYPE *argstack;

/*
 * Push one argument on the stack
 */
void add_arg_to_stack(gentype *v)
{
    ARGINFO_TYPE *info;
    info = &(argstack->stack[argstack->pos]);
    if (info->count >= info->size) {
        info->size *= 2;
        info->size = max(info->size,MAX);
        info->g = 
            (gentype*) realloc(info->g, 
                           sizeof(gentype) * info->size);
        assert(info->g);
    }
    memcpy(&(info->g[info->count++]), v, sizeof (gentype));
}

/*
 * If function is called, push arguments and previous
 * local variables on the stack
 */
void push_argcount_stack()
{
    if (argstack->pos >= argstack->size) {
        argstack->size *= 2;
        argstack->stack = 
            (ARGINFO_TYPE*) realloc(argstack->stack, 
                           sizeof(ARGINFO_TYPE) * argstack->size);
        assert(argstack->stack);
    }
    push_new_symbols_on_stack(argstack->pos-1);
    pop_old_symbols_from_stack(argstack->pos-1);
    argstack->pos++;
    argstack->stack[argstack->pos].size=0;
    argstack->stack[argstack->pos].count=0;
    argstack->stack[argstack->pos].g = NULL;
    argstack->stack[argstack->pos].ssize=0;
    argstack->stack[argstack->pos].scount=0;
    argstack->stack[argstack->pos].s_old = NULL;
    argstack->stack[argstack->pos].s_new = NULL;
}

/*
 * When function returns, pop arguments and local
 * variables from the stack
 *
 * WARNING: local strings are kept in memory, but can not be used again
 * If I ever have time...
 */
int pop_argcount_stack()
{
    if (argstack->pos == 0)
        return FALSE;
    pop_old_symbols_from_stack(argstack->pos-1);
    argstack->pos--;
    pop_new_symbols_from_stack(argstack->pos-1);
    argstack->stack[argstack->pos].count=0;
    argstack->stack[argstack->pos].size=0;
    if (argstack->stack[argstack->pos].g)
        free(argstack->stack[argstack->pos].g);
    argstack->stack[argstack->pos].g = NULL;
    argstack->stack[argstack->pos].scount=0;
    argstack->stack[argstack->pos].ssize=0;
    if (argstack->stack[argstack->pos].s_old)
        free(argstack->stack[argstack->pos].s_old);
    argstack->stack[argstack->pos].s_old = NULL;
    if (argstack->stack[argstack->pos].s_new)
        free(argstack->stack[argstack->pos].s_new);
    argstack->stack[argstack->pos].s_new = NULL;
    return TRUE;
}

gentype *peek_argcount_stack(int num)
{
    ARGINFO_TYPE *info;
    if (argstack->pos == 0)
        return NULL;
    info = &(argstack->stack[argstack->pos-1]);
    if (num < 1 || num > info->count)
        return NULL;
    return &(info->g[num-1]);
}

void argcount_stackinit()
{
    argstack = (ARG_STACK_TYPE *) malloc(sizeof(ARG_STACK_TYPE ));
    assert(argstack);
    argstack->size = MAX;
    argstack->stack = (ARGINFO_TYPE*) 
        malloc(sizeof(ARGINFO_TYPE) * argstack->size);
    assert(argstack->stack);
    argstack->pos = 0;
    argstack->stack[argstack->pos].count = 0;
    argstack->stack[argstack->pos].size = 0;
    argstack->stack[argstack->pos].g = NULL;
    argstack->stack[argstack->pos].scount = 0;
    argstack->stack[argstack->pos].ssize = 0;
    argstack->stack[argstack->pos].s_old = NULL;
    argstack->stack[argstack->pos].s_new = NULL;
}

void argcount_stackfree()
{
    while (pop_argcount_stack())
        ;
    free(argstack->stack);
    free(argstack);
}

/*
 * For static variables, push he current variable with 'symname' (if any)
 * on the stack and initialize a new variable with the same name
 */
void push_symbol_on_stack(char *symname)
{
    symrec *s;

    if (argstack->pos == 0)
        return;
    s = getsym (symname);
    if (s && s->type == VAR) {
        ARGINFO_TYPE *info;
        info = &(argstack->stack[argstack->pos-1]);
        if (info->scount >= info->ssize) {
            info->ssize *= 2;
            info->ssize = max(info->ssize,MAX);
            info->s_old = 
                (symrec*) realloc(info->s_old, 
                           sizeof(symrec) * info->ssize);
            assert(info->s_old);
            info->s_new = 
                (symrec*) realloc(info->s_new, 
                           sizeof(symrec) * info->ssize);
            assert(info->s_new);
        }
        /*
         * save old symbol value
         */
        memcpy(&(info->s_old[info->scount++]), s, sizeof (symrec));    
    }
    s = putsym (symname, VAR);
}

/*
 * If function is called, save all current local variables
 */
void push_new_symbols_on_stack(int pos)
{
    int i;
    ARGINFO_TYPE *info;
    
    if (pos < 0)
        return;
    info = &(argstack->stack[pos]);
    for (i=0;i<info->scount;i++) {
        symrec *s;

        s = getsym (info->s_old[i].name);
        if (s) 
            memcpy(&(info->s_new[i]), s, sizeof (symrec));    
    }
}

/*
 * If function is return, restore all current local variables
 */
void pop_new_symbols_from_stack(int pos)
{
    int i;
    ARGINFO_TYPE *info;
    
    if (pos < 0)
        return;
    info = &(argstack->stack[pos]);
    for (i=0;i<info->scount;i++) {
        symrec *s;

        s = getsym (info->s_new[i].name);
        if (s) 
            memcpy(s, &(info->s_new[i]), sizeof (symrec));    
    }
}

/*
 * If function is called, remove all current local variables
 */
void pop_old_symbols_from_stack(int pos)
{
    int i;
    ARGINFO_TYPE *info;
    
    if (pos < 0)
        return;
    info = &(argstack->stack[pos]);
    for (i=0;i<info->scount;i++) {
        symrec *s;

        s = getsym (info->s_old[i].name);
        if (s) 
            memcpy(s, &(info->s_old[i]), sizeof (symrec));    
    }
}


/************************************************************
 * Stuff for list of symbols
 */
#define SLIST_INIT_SIZE	8
static SLIST_TYPE temp_symbol_list;

static void init_symbol_list()
{
    temp_symbol_list.size = 0;
    temp_symbol_list.num  = 0;
    temp_symbol_list.list = NULL;
}

static void free_symbol_list()
{
    if (temp_symbol_list.list)
        free(temp_symbol_list.list);
    init_symbol_list();
}

SLIST_TYPE *new_symbol_list()
{
    int k;

    if (temp_symbol_list.list == NULL) {
        temp_symbol_list.size = SLIST_INIT_SIZE;
        temp_symbol_list.list = 
            (symrec **) malloc(sizeof(symrec *) * temp_symbol_list.size);
        assert(temp_symbol_list.list);
    }
    temp_symbol_list.num  = 0;
    return &temp_symbol_list;
}

SLIST_TYPE *add_to_symbol_list(symrec *symbol)
{
    if (temp_symbol_list.num >= temp_symbol_list.size) {
        temp_symbol_list.size *= 2;
        temp_symbol_list.list = (symrec **) realloc(temp_symbol_list.list, 
                           sizeof(symrec *) * temp_symbol_list.size);
        assert(temp_symbol_list.list);
    }
    temp_symbol_list.list[temp_symbol_list.num++] = symbol;
    return &temp_symbol_list;
}

/************************************************************
 * Stuff for list storage space
 */
static int *range_master_space;
static int range_master_size;
static int range_master_num;

static void init_range_master_space()
{
    range_master_space = NULL;
    range_master_size  = 1;
    range_master_num   = 0;
    range_master_space = (int*) realloc(range_master_space,
                                        range_master_size * sizeof(int));
    range_master_space[range_master_num++] = 0;
}

static void free_range_master_space()
{
    if (range_master_space)
        free(range_master_space);
    range_master_space = NULL;
    range_master_size  = 0;
    range_master_num   = 0;
}

int add_to_range_master_space(int pos, int append, int num, int *range)
{
    int i,start,size;

    if (pos < 0) {
        pos   = range_master_num;
        start = range_master_num+1;
        size  = num+1;
    }
    else if (append) {
        start = pos + range_master_space[pos] + 1;
        size  = range_master_space[pos]+num+1;
        if (range[0] <= range_master_space[start-1])
            return -1;
    }
    else {
        start = pos + 1;
        size  = num+1;
    }
        
    if (pos+size >= range_master_size) {
        range_master_size += size;
        range_master_space = (int*) realloc(range_master_space,
                                            range_master_size*sizeof(int));
        assert(range_master_space);
    }
    range_master_num = max(pos+size,range_master_num);
    range_master_space[pos] = size-1;
    for (i=0;i<num;i++) 
        range_master_space[start++] = range[i];
    return pos;
}

int *get_range_master_list(int pos)
{
    if (pos > range_master_num || pos < 0)
        pos = 0;
    return range_master_space+pos;
}

/************************************************************
 * Stuff for list of ranges
 */
#define RLIST_INIT_SIZE	16
static RLIST_TYPE temp_range_list;

static void init_range_list()
{
    temp_range_list.size = 0;
    temp_range_list.num  = 0;
    temp_range_list.list = NULL;
}

static void free_range_list()
{
    if (temp_range_list.list)
        free(temp_range_list.list);
    init_range_list();
}

RLIST_TYPE *new_range_list()
{
    int k;

    if (temp_range_list.list == NULL) {
        temp_range_list.size = RLIST_INIT_SIZE;
        temp_range_list.list = 
            (int*) malloc(sizeof(int) * temp_range_list.size);
        assert(temp_range_list.list);
        for (k=0;k<temp_range_list.size;k++)
            temp_range_list.list[k] = UNDETERMINED;
    }
    temp_range_list.num  = 0;
    return &temp_range_list;
}

RLIST_TYPE *add_to_range_list(int *range)
{
    int n,k,*list,num;

    if (range[RANGE_EXT]>0) {
        list = get_range_master_list(range[RANGE_EXT]);
        num  = list[0];
        list++;
    }
    else {
        list = range;
        num  = 2;
    }
    if (temp_range_list.num + num + 1 >= temp_range_list.size) {
        n = temp_range_list.size;
        temp_range_list.size += num+1;
        temp_range_list.list = (int*) realloc(temp_range_list.list, 
                           sizeof(int) * temp_range_list.size);
        assert(temp_range_list.list);
        for (k=n;k<temp_range_list.size;k++)
            temp_range_list.list[k] = UNDETERMINED;
    }
    temp_range_list.list[temp_range_list.num++] = num;
    for (k=0;k<num;k++) 
        temp_range_list.list[temp_range_list.num++] = list[k];
    return &temp_range_list;
}



/************************************************************
 * Stuff for list of floats
 */
#define FLIST_INIT_SIZE	10
static FLIST_TYPE temp_float_list;

static void init_float_list()
{
    temp_float_list.size = 0;
    temp_float_list.num  = 0;
    temp_float_list.list = NULL;
}

static void free_float_list()
{
    if (temp_float_list.list)
        free(temp_float_list.list);
    init_float_list();
}

FLIST_TYPE *new_float_list()
{
    int k;

    if (temp_float_list.list == NULL) {
        temp_float_list.size = FLIST_INIT_SIZE;
        temp_float_list.list = 
            (float*) malloc(sizeof(float) * temp_float_list.size);
        assert(temp_float_list.list);
        for (k=0;k<temp_float_list.size;k++)
            temp_float_list.list[k] = UNDETERMINED;
    }
    temp_float_list.num  = 0;
    return &temp_float_list;
}

FLIST_TYPE *add_to_float_list(float value)
{
    int n,k;

    if (temp_float_list.num >= temp_float_list.size) {
        n = temp_float_list.size;
        temp_float_list.size *= 2;
        temp_float_list.list = (float*) realloc(temp_float_list.list, 
                           sizeof(float) * temp_float_list.size);
        assert(temp_float_list.list);
        for (k=n;k<temp_float_list.size;k++)
            temp_float_list.list[k] = UNDETERMINED;
    }
    temp_float_list.list[temp_float_list.num++] = value;
    return &temp_float_list;
}


/************************************************************
 * Stuff for mixed list of floats and strings
 */
#define PRINTF_MAX	1024
#define PRINTF_TEMP	80
#define MLIST_INIT_SIZE	10

static MLIST_TYPE temp_mixed_list;
static char *mlist_temp_string_space;
static int  mlist_temp_string_size;

static void init_mixed_list()
{
    temp_mixed_list.size   = 0;
    temp_mixed_list.num    = 0;
    temp_mixed_list.type   = NULL;
    temp_mixed_list.value  = NULL;
    mlist_temp_string_space = (char*) malloc(sizeof(char)*PRINTF_MAX);
    mlist_temp_string_size = PRINTF_MAX;
    assert(mlist_temp_string_space);
}

static void free_mixed_list()
{
    if (temp_mixed_list.type)
        free(temp_mixed_list.type);
    if (temp_mixed_list.value)
        free(temp_mixed_list.value);
    if (mlist_temp_string_space)
        free(mlist_temp_string_space);
    temp_mixed_list.size   = 0;
    temp_mixed_list.num    = 0;
    temp_mixed_list.type   = NULL;
    temp_mixed_list.value  = NULL;
    mlist_temp_string_size = 0;
}

MLIST_TYPE *new_mixed_list()
{
    int k;

    if (temp_mixed_list.type == NULL) {
        temp_mixed_list.size = MLIST_INIT_SIZE;
        temp_mixed_list.type = 
            (int*) malloc(sizeof(int) * temp_mixed_list.size);
        assert(temp_mixed_list.type);
        temp_mixed_list.value = 
            (MLIST_UNION*) malloc(sizeof(MLIST_UNION) * temp_mixed_list.size);
        assert(temp_mixed_list.value);
    }
    temp_mixed_list.num  = 0;
    return &temp_mixed_list;
}

MLIST_TYPE *add_to_mixed_list(int type, float value, char *string)
{
    int n;

    if (temp_mixed_list.num >= temp_mixed_list.size) {
        n = temp_mixed_list.size;
        temp_mixed_list.size *= 2;
        temp_mixed_list.type = (int*) realloc(temp_mixed_list.type, 
                           sizeof(int) * temp_mixed_list.size);
        assert(temp_mixed_list.type);
        temp_mixed_list.value = (MLIST_UNION*) realloc(temp_mixed_list.value, 
                           sizeof(MLIST_UNION) * temp_mixed_list.size);
        assert(temp_mixed_list.value);
    }
    if (type == VAR) {
        temp_mixed_list.type[temp_mixed_list.num] = VAR;
        temp_mixed_list.value[temp_mixed_list.num++].f  = value;
    }
    else {
        temp_mixed_list.type[temp_mixed_list.num] = STRING;
        temp_mixed_list.value[temp_mixed_list.num++].s  = string;
    }
    return &temp_mixed_list;
}


#define PRINTF_STRING	100
#define PRINTF_FLOAT	101
#define PRINTF_INT	102
#define PRINTF_CHAR	103

/*
 * Print formatted output string
 */
char *printf_mixed_list(char *format, MLIST_TYPE *mlist)
{
    char *buf,*p,temp_space[PRINTF_TEMP+1],temp_form[PRINTF_TEMP+1];
    int i,j,k,ok,type,done;

    buf = mlist_temp_string_space;
    for (i=0,k=0,p=format,done=FALSE;*p!='\0' && i<PRINTF_MAX && !done; p++) {
        switch (*p) {
            /*
             * Start of format specifier
             */
            case '%' :
                /*
                 * %% means, no format specifier but just %
                 */
                if (*(p+1) == '%') {
                    buf[i++] = *p;
                    p++;
                    break;
                }
                ok = FALSE;
                for (j=0; *p != '\0' && !ok && j < PRINTF_TEMP; p++) {
                    switch (*p) {
                        case 'X':
                        case 'x':
                        case 'd':
                        case 'o':
                        case 'u':
                        case 'i':
                            type = PRINTF_INT;
                            ok   = TRUE;
                            break;
                        case 'c':
                            type = PRINTF_CHAR;
                            ok   = TRUE;
                            break;
                        case 's':
                            type = PRINTF_STRING;
                            ok   = TRUE;
                            break;
                        case 'f':
                        case 'g':
                        case 'e':
                        case 'E':
                            type = PRINTF_FLOAT;
                            ok   = TRUE;
                            break;
                        default:
                            temp_form[j++] = *p;
                    }
                }
                /*
                 * If not a valid conversion specifier is found,
                 * abort here
                 */
                if (!ok) {
                    done = TRUE;
                    break;
                }
                p--;
                /*
                 * Check if we have still some arguments left
                 */
                if (k < mlist->num) {
                    int   d,c;
                    float f;
                    char  *s;

                    temp_form[j++] = *p;
                    temp_form[j] = '\0';
                    /*
                     * If format specifier is different from
                     * type of argument, do conversion
                     */
                    if (mlist->type[k] == VAR) {
                        if (type == PRINTF_STRING) {
                            sprintf(temp_space, "%g", mlist->value[k++].f);
                            s = temp_space;
                        }
                        else if (type == PRINTF_CHAR) {
                            c = (int) mlist->value[k++].f;
                        }
                        else if (type == PRINTF_FLOAT) {
                            f = mlist->value[k++].f;
                        }
                        else if (type == PRINTF_INT) {
                            d = (int) mlist->value[k++].f;
                        }
                    }
                    else if (mlist->type[k] == STRING) {
                        if (type == PRINTF_STRING) {
                            s = mlist->value[k++].s;
                        }
                        else if (type == PRINTF_CHAR) {
                            c = mlist->value[k++].s[0];
                        }
                        else if (type == PRINTF_FLOAT) {
                            sscanf(mlist->value[k++].s, "%g", &f);
                        }
                        else if (type == PRINTF_INT) {
                            sscanf(mlist->value[k++].s, "%g", &f);
                            d = (int) f;
                        }
                    }
                    /* 
                     * just to make sure we have enough space
                     */
                    if (i > mlist_temp_string_size/2) {
                        mlist_temp_string_size *= 2;
                        mlist_temp_string_space = (char*)
                            realloc(mlist_temp_string_space,
                                    mlist_temp_string_size * sizeof(char));
                        assert(mlist_temp_string_space);
                        buf = mlist_temp_string_space;
                    }
                    /*
                     * print the formatted argument
                     */
                    if (type == PRINTF_FLOAT) {
                        sprintf(buf+i, temp_form, f);
                        i = strlen(buf);
                    }
                    else if (type == PRINTF_STRING) {
                        sprintf(buf+i, temp_form, s);
                        i = strlen(buf);
                    }
                    else if (type == PRINTF_INT) {
                        sprintf(buf+i, temp_form, d);
                        i = strlen(buf);
                    }
                    else if (type == PRINTF_CHAR) {
                        sprintf(buf+i, temp_form, c);
                        i = strlen(buf);
                    }
                }
                break;
            default:
                buf[i++] = *p;
        }
    }
    buf[i] = '\0';
    return buf;
}

/************************************************************
 * Stuff for include files
 */
#define MAXINCLUDE	10
static char *inbuf;
static int inbuf_pos, inbuf_size, inbuf_max, include_count = 0;
static void read_script(FILE *infile)
{
    int c;
    
    inbuf_pos = 0;
    while ((c=getc(infile)) != EOF) {
        if (inbuf_pos >= inbuf_max-1) {
            if (inbuf_max == 0)
                inbuf_max = 100;
            inbuf_max *=2;
            inbuf = (char*) realloc(inbuf, inbuf_max * sizeof(char));
        }
        inbuf[inbuf_pos++] = c;
    }
    /*
     * always end with extra '\n'
     */
    inbuf[inbuf_pos++] = '\n';
    inbuf[inbuf_pos] = '\0';
    inbuf_size = inbuf_pos;
    inbuf_pos = 0;
    pp = inbuf;
}

static void free_inbuf()
{
    include_count = 0;
    if (inbuf_size) {
        inbuf_size = 0;
        inbuf_max  = 0;
        inbuf_pos  = 0;
        free(inbuf);
        inbuf = NULL;
    }
}


/*
 * The lexical analyzer. Compile buffer into a series of tokens
 */
int preprocess_script(MBLOCK *mblk)
{
    char c;
    int j;
    static char *symbuf = 0;
    static int length = 0;
    YYSTYPE yylval;
    
    yylval.type = 0;     
    yylval.rps  = NULL;
    for (j=0;j<MAX_RANGE_SIZE;j++)
        yylval.range[j] = 0;
    yylval.line         = linecount;     
    yylval.stm          = stmcount;
    yylval.linestart    = linestart;
    yylval.scriptname   = scriptname;
    yylval.value.jmp    = 0;
    /* 
     * Initially make the buffer long enough
     *    for a 80-character symbol name.  
     */
    if (length == 0) {
        length = 80, symbuf = (char *)malloc (length + 1);
        assert(symbuf);
    }
    /* 
     * Ignore whitespace, get first nonwhite character.  
     */
    while ((c = *pp++) == ' ' || c == '\t');
    /*
     * Store startpoint of this token for debugging
     */
    yylval.pp   = pp-1;

    if (c == YY_EOF) {
        yylval.type = 0;
        push_yylval(&yylval);
        return 0;
    }
    
    /*
     * Get include files here
     */
    if (strncasecmp(pp-1,"include",7) == 0) {
        FILE *incfile;
        char tmp_name[256];
        char *pc, *pp_store, *scriptname_store, *linestart_store;
        int  i, linecount_store;

        pp += 7;
        sscanf(pp,"\"%s\"", tmp_name);
        if ((pc=strchr(tmp_name,'\"')) != NULL)
            *pc = '\0';
        incfile = psnd_open_script_file(tmp_name, get_argval(0));
        if (incfile == NULL) {
            psnd_printf(mblk,"Can not open include file: %s\n", tmp_name);
            return -1;
        }
        include_count++;
        if (include_count > MAXINCLUDE) {
            psnd_printf(mblk,"%s, line: %d, nesting level:%d Includes nested too deep\n",
                           scriptname, linecount, include_count);
            return -1;
        }
        /*
         * Save some variables
         */
        pp_store = pp;
        scriptname_store = scriptname;
        scriptname       = create_and_cat_string(tmp_name, NULL, 0, NULL,0,0);
        linecount_store  = linecount;
        linestart_store  = linestart;
        linecount = 1;
        read_script(incfile);
        fclose(incfile);
        while ((i=preprocess_script(mblk)) > 0);
        if (i<0)
            return i;
        /*
         * Pop the YY_EOF
         */
        yylval_p--;
        scriptname = scriptname_store;
        linecount  = linecount_store;
        linestart  = linestart_store;
        pp = pp_store;
        include_count--;
        /*
         * Mark the rest of the line as comment
         */
        c = '#';
    }

    /*
     * Comment, skip rest of line
     */
    if (c == '#') {
        while (*pp != '\n' && *pp != YY_EOF && *pp)
            pp++;
        yylval.type = *pp;
        push_yylval(&yylval);
        if (*pp == '\n') {
            pp++;
            linestart = pp;
            linecount++;
            stmcount++;
        }
        return yylval.type;
    }

    /*
     * Continue on next line
     */
    while (c == '\\') {
        /*
         * skip rest of line
         */
        while ((c = *pp++) != '\n' && c != YY_EOF && c);
        if (c == '\n') {
            linecount++;
            linestart = pp;
            /*
             * skip white space
             */
            do {
                c = *pp++;
            }
            while (c == ' ' || c == '\t');
        }
        if (c == YY_EOF) {
            yylval.type = 0;
            push_yylval(&yylval);
            return 0;
        }
    }

    /* 
     *  Char starts a number => parse the number.         
     */
    if ((c == '.' && isdigit(*pp)) || isdigit (c)) {
        pp--;
        sscanf (pp, "%f", &yylval.value.var);
        while ((*pp == '.' && isdigit(*(pp+1))) || 
               isdigit (*pp) || 
               (*pp == 'e' && isdigit(*(pp-1))) || 
               (*pp == '-' && *(pp-1) == 'e') ||
               (*pp == '+' && *(pp-1) == 'e')) 
            pp++;
        /*
         * Special extensions
         * value + 'k' = value x 1024
         * value + 'm' = value x 1024000
         * value + 'g' = value x 1024000000
         */
        if (*pp == 'k' || *pp == 'K') {
            yylval.value.var *= 1024;
            pp++;
        }
        else if (*pp == 'm' || *pp == 'M') {
            yylval.value.var *= 1024e3;
            pp++;
        }
        else if (*pp == 'g' || *pp == 'G') {
            yylval.value.var *= 1024e6;
            pp++;
        }
        yylval.type = NUM;
        push_yylval(&yylval);
        return NUM;
    }

    /* 
     * Char starts an identifier => read the name.
     * Valid characters are a-z, A-Z, 1-0, and '_'
     * Valid start characters are a-z, A-Z
     */
    if (isalpha (c)) {
        symrec *s;
        int i;

        i = 0;
        symbuf[0] = '\0';
        do {
            /* 
             * If buffer is full, make it bigger.  
             * (length-1) to keep one spare position for user namespace 
             */
            if (i == length-1) {
                length *= 2;
                symbuf = (char *)realloc (symbuf, length + 1);
            }
            /* 
             * Add this character to the buffer.         
             */
            symbuf[i++] = c;
            /* 
             * Get another character.                    
             */
            c = *pp++;
        }  while (c != YY_EOF && (isalnum (c) || c == '_'));
        symbuf[i] = '\0';

        pp--;
        /*
         * user variables or user functions
         */
        if (peek_yylval_type() == '%' || peek_yylval_type() == '$') {
            /*
             * Move into user namespace by adding '$'
             */
            symbuf[i++] = '$';
            symbuf[i] = '\0';
            s = getsym (symbuf);
            if (s == 0) 
                s = putsym (symbuf, VAR);
            yylval.value.tptr = s;
            yylval.type = s->type;
            push_yylval(&yylval);
            return s->type;
        }
        
        /*
         * block name
         */
        for (j=0;psnd_blocks[j].id != -1;j++) {
            if (j >= mblk->info->max_block)
                break;
            if (strcasecmp(symbuf, psnd_blocks[j].name) == 0) {
                yylval.value.block  = psnd_blocks + j;
                yylval.type = BLOCK;
                push_yylval(&yylval);
                return yylval.type;
            }
        }
    
        /*
         * keywords
         */
        for (j=0;keywords[j].i != -1;j++) {
            if (strcasecmp(symbuf, keywords[j].s) == 0) {
                if (keywords[j].i == DO)
                    push_jump(jmp1, yylval_p);
                else if (keywords[j].i == SORTED) {
                    /*
                     * Simple hack to label 'IN' as 'IN followed by SORTED'
                     */
                    if (peek_yylval_type() == IN) 
                        yylval_stack[yylval_p-1].type = INSORTED;
                }
                else if (keywords[j].i == FOR) {
                    int n;
                    /*
                     * insert extra FOR and '\;' to indicate
                     * that the loop must be initialized.
                     * Then, for each cycle, jump to the
                     * second FOR
                     */
                    yylval.type = FOR;
                    push_yylval(&yylval);
                    yylval.stm  = ++stmcount;
                    yylval.type = '\n';
                    push_yylval(&yylval);
                    yylval.stm  = ++stmcount;
                    for (n=0;n<4;n++)
                        yylval.range[n] = 0;
                    push_jump(jmp1, yylval_p);
                }
                else if (keywords[j].i == BREAK)
                    push_jump(jmp1, yylval_p);
                else if (keywords[j].i == CONTINUE)
                    push_jump(jmp1, yylval_p);
                else if (keywords[j].i == EXIT)
                    ;
                else if (keywords[j].i == RETURN)
                    ;
                else if (keywords[j].i == FUNCTION) {
                    int jmp1_id = pop_jump(jmp1);
                    if (jmp1_id != -1) {
                        push_yylval(&yylval);
                        yylval_pp = yylval_p;
                        yyerror("Error in position function definition");
                        return -1;
                    }
                    push_jump(jmp1, yylval_p);
                }
                else if (keywords[j].i == END) {
                    int type,jmp_id;

                    while ((jmp_id = pop_jump(jmp1)) >= 0) {
                        type = yylval_stack[jmp_id].type;
                        /*
                         * Save illegal BREAK,CONTINUE on stack 2,
                         * for better error handling
                         */
                        if (type != FUNCTION)
                            push_jump(jmp2, jmp_id);
                        else
                            break;
                    }
                    if (type != FUNCTION) {
                        push_yylval(&yylval);
                        yylval_pp = yylval_p;
                        yyerror("Error: END without FUNCTION");
                        return -1;
                    }
                    yylval_stack[jmp_id].value.jmp = yylval_p+1;
                }
                else if (keywords[j].i == DONE) {
                    int jmp1_id, jmp2_id;
                    /*
                     * Go back to DO
                     * Skip BREAK, CONTINUE
                     */
                    while ((jmp1_id = pop_jump(jmp1)) >= 0) {
                        int type = yylval_stack[jmp1_id].type;
                        if (type == BREAK)
                            /*
                             * Jump 1 position after DONE
                             */
                            yylval_stack[jmp1_id].value.jmp = yylval_p+1;
                        else if (type == CONTINUE)
                            /*
                             * Save CONTINUE on stack 2
                             */
                            push_jump(jmp2, jmp1_id);
                        else
                            break;
                    }
                    /*
                     * jmp1_id marks the DO
                     * jmp2_id marks the FOR or WHILE
                     */
                    jmp2_id = pop_jump(jmp1);
                    /*
                     * Find FOR or WHILE belonging to DO
                     */
                    if (jmp1_id < 0 || 
                        jmp2_id < 0 ||
                        yylval_stack[jmp1_id].type != DO  ||
                        !(yylval_stack[jmp2_id].type == FOR  ||
                          yylval_stack[jmp2_id].type == WHILE)) {
                              push_yylval(&yylval);
                              yylval_pp = yylval_p;
                              yyerror("Error in WHILE/FOR, DO, DONE");
                              return -1;
                    }
                    /*
                     * set the jumps for the CONTINUE statement
                     */
                    while (!jump_stackempty(jmp2)) {
                        int jmp3_id = pop_jump(jmp2);
                        /*
                         * Jump 1 position before FOR/WHILE
                         */
                        yylval_stack[jmp3_id].value.jmp = jmp2_id-1;
                    }
                    /*
                     * Set jump 1 position after DONE for DO
                     */
                    yylval_stack[jmp1_id].value.jmp = yylval_p+1;
                    /*
                     * Set jump 1 position before FOR/WHILE for DONE
                     */
                    yylval.value.jmp = jmp2_id-1;
                }
                else if (keywords[j].i == WHILE) {
                    int jmp1_id;
                    /*
                     * Check if this is a do/while loop
                     * Go back to DO 
                     * Skip BREAK, CONTINUE
                     */
                    while ((jmp1_id = pop_jump(jmp1)) >= 0) {
                        int type = yylval_stack[jmp1_id].type;
                        if (type == BREAK)
                            /*
                             * Save BREAK on stack 5
                             */
                            push_jump(jmp5, jmp1_id);
                        else if (type == CONTINUE)
                            /*
                             * Save CONTINUE on stack 2
                             */
                            push_jump(jmp2, jmp1_id);
                        else
                            break;
                    }
                    /*
                     * Find DO belonging to WHILE
                     */
                    if (jmp1_id < 0 || 
                        yylval_stack[jmp1_id].type != DO ||
                        yylval_stack[jmp1_id].stm == yylval_stack[jmp1_id-1].stm) {
                        /*
                         * Not found, must be a while/do/done loop
                         * Restore BREAK and CONTINUE to stack 1
                         */
                        push_jump(jmp1, jmp1_id);
                        while (!jump_stackempty(jmp2))
                            push_jump(jmp1, pop_jump(jmp2));
                        while (!jump_stackempty(jmp5))
                            push_jump(jmp1, pop_jump(jmp5));
                        /*
                         * Save current jump point
                         */
                        push_jump(jmp1, yylval_p);
                    }
                    else {
                        /*
                         * OK, this is a do/while loop
                         * set the jumps for the CONTINUE statement
                         */
                         while (!jump_stackempty(jmp2)) {
                            int jmp3_id = pop_jump(jmp2);
                            yylval_stack[jmp3_id].value.jmp = yylval_p-1;
                        }
                        yylval.value.jmp = jmp1_id+1;
                    }
                }
                else if (keywords[j].i == IF) 
                    push_jump(jmp3, 0);
                else if (keywords[j].i == THEN) {
                    push_jump(jmp1, yylval_p);
                    addto_jump(jmp3, 1);
                }
                else if (keywords[j].i == ELSE ||
                         keywords[j].i == ELSEIF) {
                    int jmp_id;
                    while ((jmp_id = pop_jump(jmp1)) >= 0) {
                        int type = yylval_stack[jmp_id].type;
                        /*
                         * Save BREAK,CONTINUE on stack 2
                         */
                        if (type == BREAK || type == CONTINUE)
                            push_jump(jmp2, jmp_id);
                        else
                            break;
                    }
                    /*
                     * Restore BREAK,CONTINUE to stack 1
                     */
                    while (!jump_stackempty(jmp2))
                        push_jump(jmp1, pop_jump(jmp2));
                    if (jmp_id < 0 || yylval_stack[jmp_id].type != THEN) {
                          push_yylval(&yylval);
                          yylval_pp = yylval_p;
                          yyerror("Error in IF, ELSE");
                          return -1;
                    }
                    addto_jump(jmp3, -1);
                    yylval_stack[jmp_id].value.jmp = yylval_p+1;
                    push_jump(jmp1, yylval_p);
                    addto_jump(jmp3, 1);
                    
                    if (keywords[j].i == ELSEIF) {
                        /*
                         * insert ELSE and '\;'
                         */
                        yylval.type = ELSE;
                        push_yylval(&yylval);
                        yylval.stm  = ++stmcount;
                        yylval.type = '\n';
                        push_yylval(&yylval);
                        yylval.stm  = ++stmcount;
                    }
                }
                else if (keywords[j].i == ENDIF) {
                    int jmp_id;
                    int endif_count = pop_jump(jmp3);
                    do {
                    while ((jmp_id = pop_jump(jmp1)) >= 0) {
                        int type = yylval_stack[jmp_id].type;
                        /*
                         * Save BREAK,CONTINUE on stack 2
                         */
                        if (type == BREAK || type == CONTINUE)
                            push_jump(jmp2, jmp_id);
                        else
                            break;
                    }
                    /*
                     * Restore BREAK,CONTINUE to stack 1
                     */
                    while (!jump_stackempty(jmp2))
                        push_jump(jmp1, pop_jump(jmp2));
                    if (jmp_id < 0 || 
                        (yylval_stack[jmp_id].type != ELSE &&
                         yylval_stack[jmp_id].type != THEN)) {
                          push_yylval(&yylval);
                          yylval_pp = yylval_p;
                          yyerror("Error in IF, ELSE, ENDIF");
                          return -1;
                    }
                    yylval_stack[jmp_id].value.jmp = yylval_p+1;
                    endif_count--;
                    } while (endif_count > 0);
                }
                yylval.type = keywords[j].i;
                push_yylval(&yylval);
                return yylval.type;
            }
        }

        /*
         * user functions
         */
        if (peek_yylval_type() == FUNCTION) {
        /*
         * Move into user namespace by adding '$'
         */
        symbuf[i++] = '$';
        symbuf[i] = '\0';
        s = getsym (symbuf);
            if (s != 0) {
                /*
                 * Mmmm.., symbol has been used before, but
                 * maybe has been defined as VAR
                 */
                if (s->type == VAR) {
                    s->type = FUNCTION_DEF;
                    /*
                     * previous occurrences of this symbol 's'
                     * should be changed from VAR to FUNCTION_DEF
                     */
                    yylval_change_type(s, VAR, FUNCTION_DEF);
                }
            }
            else
                s = putsym (symbuf, FUNCTION_DEF);
            s->value.jmp = yylval_p+1;
#ifdef ffff
        }
        else {
            /*
             * Assume this function will follow later
             */
            if (s == 0)
                s = putsym (symbuf, FUNCTION_DEF);
        }
#endif
        yylval.value.tptr = s;
        yylval.type = s->type;
        push_yylval(&yylval);
        return s->type;
}
        s = getsym (symbuf);
        if (s != 0) {
            yylval.value.tptr = s;
            yylval.type = s->type;
            push_yylval(&yylval);
            return s->type;
        }

        push_yylval(&yylval);
        yylval_pp = yylval_p;
        yyerror("Unknown identifier");
        return -1;
    }

     
    /* 
     * Char starts a string.       
     */
    if (c == '\"') {
        symrec *s;
        int i;

        c = *pp++;
        i=0;
        for (symbuf[0] = '\0';c != '\"' && c != '\n' ;c = *pp++) {
            char cc;
            if (c == YY_EOF) {
                yylval.type = 0;
                push_yylval(&yylval);
                return 0;
            }
            /* 
             * If buffer is full, make it bigger.  
             * (length-1) to keep one spare position for user namespace 
             */
            if (i == length-1) {
                length *= 2;
                symbuf = (char *)realloc (symbuf, length + 1);
            }
            cc = c;
            /*
             * Translate special characters
             */
            if (c == '\\') {
                cc = *pp++;
                switch (cc) {
                    case '\0':
                        c = 0;
                        break;
                    case '\\':
                        break;
                    case '\"':
                        cc = '\"';
                        break;
                    case 'n':
                        cc = '\n';
                        break;
                    case 'r':
                        cc = '\r';
                        break;
                    case 't':
                        cc = '\t';
                        break;
                }
            }
            /* 
             * Add this character to the buffer.         
             */
            symbuf[i++] = cc;
        }  
        symbuf[i] = '\0';

        yylval.value.str = create_and_cat_string(symbuf, NULL, 0, NULL,0,0);
        yylval.type = STRING;
        push_yylval(&yylval);
        return STRING;
    }

    /* 
     * @ starts an array.       
     */
    if (c == '@') {
        symrec *s;
        int i;
        yylval.type = ARRAY;

        i = 0;
        symbuf[0] = '\0';
        do {
            /* 
             * If buffer is full, make it bigger.  
             * (length-1) to keep one spare position for user namespace 
             */
            if (i == length-1) {
                length *= 2;
                symbuf = (char *)realloc (symbuf, length + 1);
            }
            /* 
             * Add this character to the buffer.         
             */
            symbuf[i++] = c;
            /* 
             * Get another character.                    
             */
            c = *pp++;
        }  while (c && isalpha (c));
        symbuf[i] = '\0';

        pp--;
        for (j=0;psnd_arrays[j].id != -1;j++) {
            if (strcasecmp(symbuf, psnd_arrays[j].name) == 0) {
                yylval.value.array  = psnd_arrays + j;
                if (yylval.type == ARRAY && yylval.value.array->data2)
                    yylval.type = ARRAY2;
                push_yylval(&yylval);
                return yylval.type;
            }
        }
    }

    /*
     * verify symbols
     */
    for (j=0;keyword_symbols[j].i != -1;j++) {
        if (c == keyword_symbols[j].s[0] &&
            ( keyword_symbols[j].s[1] == '\0' ||
              *pp == keyword_symbols[j].s[1])) {
                if (keyword_symbols[j].s[1] != '\0') 
                    pp++;
                yylval.type = keyword_symbols[j].i;
                push_yylval(&yylval);
                return yylval.type;
        }
    }

    if (c == '\n' || c == ';') {
        if (c == '\n') {
            linestart = pp;
            linecount++;
        }
        else
            c = '\n';
        stmcount++;
        /*
         * set the jumps for the BREAK statement
         * from do/while loop
         */
        while (!jump_stackempty(jmp5)) {
            int jmp_id = pop_jump(jmp5);
            yylval_stack[jmp_id].value.jmp = yylval_p;
        }
    }
    /* 
     * Any other character is a token by itself.        
     */
    if (c != '\r') {
        yylval.type = c;
        push_yylval(&yylval);
    }
    return c;

}



int jump(int jmp)
{
    if (jmp <= 0) {
        yyerror("Error in jump");
        return 0;
    }
    yylval_restore(jmp);
    /*
     * We change position here, so position marker,
     * used by tmp_string, is no longer valid
     */
    cleanup_tmp_string(0);
    return 1;
}

int yyabort_by_user;

int yylex (YYSTYPE *lvalp, void *param)
{
    if (yyabort_by_user)
        return -1;
   
    memcpy(lvalp, pop_yylval(), sizeof(gentype));
    return lvalp->type;
}

/*

int yylex ()
{
    if (yyabort_by_user)
        return -1;
    memcpy(&yylval, pop_yylval(), sizeof(gentype));
    return yylval.type;
}

*/

static int  au_argc;
static char argc_str[20], **au_argv;
static char empty_string[] = "";

static int get_argcount()
{
    return au_argc;
}

static char *get_argcount_string()
{
    return argc_str;
}

static char *get_argval(int count)
{
    if (count >= 0 && count < au_argc)
        return au_argv[count];
    else
        return empty_string;
}

char *(*au_scanfile)(FILE *file, char *pattern1, int skip, char *pattern2, 
                      int isregexp);
char *(*au_scanbinfile)(FILE *file, char *pattern);

int  (*au_os_big_endian)(void);
void (*au_xxrotate)(float *x, int n1, int n2, int n3);
void (*au_xxi2f)(float *x, int n1, int n2);
void (*au_xxf2i)(float *x, int n1, int n2);
void (*au_xxswap2)(float *x, int n1, int n2);
void (*au_xxswap4)(float *x, int n1, int n2);
char *(*au_xxf2ascii)(float *x, int n1, int n2);
void (*au_xxc2ri)(float *co, float *re, float *ir, int n1, int n2);
void (*au_xxc2ir)(float *co, float *re, float *ir, int n1, int n2);
int   (*au_scan_integer)(char *value);
float (*au_scan_float)(char *value);

void run_au_script(void *param, char *au_script, int argc, char *argv[])
{
    int i, jmp_id;
    char local_scriptname[] = "";
    static YYSTYPE yylval;
    MBLOCK *mblk = (MBLOCK *)param;
    mblk_bison = mblk;

    au_argc = argc;
    au_argv = argv;
    sprintf(argc_str,"%d", argc);
    au_scanfile = scanfile;
    au_scanbinfile = scanbinfile;
    au_os_big_endian = os_big_endian;
    au_xxrotate = xxrotate;
    au_xxi2f = xxi2f;
    au_xxf2i = xxf2i;
    au_xxswap2 = xxswap2;
    au_xxswap4 = xxswap4;
    au_xxf2ascii = xxf2ascii;
    au_xxc2ri = xxc2ri;
    au_xxc2ir = xxc2ir;
    au_scan_integer = psnd_scan_integer;
    au_scan_float = psnd_scan_float;
    yylval_stackinit();
    jmp1 = jump_stackinit();
    jmp2 = jump_stackinit();
    jmp3 = jump_stackinit();
    jmp4 = jump_stackinit();
    jmp5 = jump_stackinit();
    argcount_stackinit();
    hash_id = hash_new(2048, 
                       (hash_func_type) hash_string,
	               (compare_func_type) compare_strings);    
    init_table ();
    init_blocks();
    init_arrays(mblk);
    init_tmp_string();
    init_float_list();
    init_range_list();
    init_symbol_list();
    init_mixed_list();
    init_range_master_space();
    init_strtok();
    memset(&global_status, 0, sizeof(symrec));
    
    pp = au_script;
    /*
     * Start with 2x newline
     */
    yylval.type = '\n';
    push_yylval(&yylval);
    push_yylval(&yylval);
    if (argc > 0)
        scriptname = argv[0];
    else
        scriptname = local_scriptname;
    linecount = 1;
    stmcount  = 1;
    linestart = pp;
    while ((i=preprocess_script(mblk)) > 0);
    /*
     * End with newline
     */
    yylval_p--;
    yylval.type = '\n';
    push_yylval(&yylval);
    yylval.type = 0;
    push_yylval(&yylval);
    if (i >=0) while ((jmp_id = pop_jump(jmp1)) >= 0) {
        yyerror("Error in LOOP");
        i=-1;
        break;
    }
    yylval_set_pointers();
    outfile_open_status = FALSE;
    /*
     * This is the main loop 
     */
    if (i >=0)
        yyparse ((void*)mblk);
    /*
     * If, due to an error, the output file has not been closed,
     * we do it here.
     */
    if (outfile_open_status != FALSE) 
        au_close_outfile((void*)mblk);

    argcount_stackfree();
    jump_stackfree(jmp1);
    jump_stackfree(jmp2);
    jump_stackfree(jmp3);
    jump_stackfree(jmp4);
    jump_stackfree(jmp5);
    free_symbols_and_hashtable();
    free_stringspace();
    free_tmp_string();
    free_inbuf();
    free_float_list();
    free_range_list();
    free_symbol_list();
    free_mixed_list();
    free_range_master_space();
    free_strtok();
    yylval_cleanup();
    if (yyabort_by_user)
        psnd_printf (mblk,"\nRun aborted by user ...\n\n");

}



