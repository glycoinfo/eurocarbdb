
#ifndef PSND_DEFS_H
#define PSND_DEFS_H


typedef enum  {

    PSND_XDIV2	=1000,
    PSND_XMUL2,
    PSND_YDIV2,
    PSND_YMUL2,
    PSND_RESET,
    PSND_RESET_NOUNDO,
    PSND_UNDO_ZOOM,
    PSND_REDO_ZOOM,
    PSND_CO,
    PSND_CI,
    PSND_CP_1X1,
    PSND_CP,
    PSND_CP2,
    PSND_CPM,
    PSND_STACKPLOT,
    PSND_POPUP_STACKPLOT,
    PSND_CN,
    PSND_CM,
    PSND_CC,
    PSND_DR,
    PSND_AX,
    PSND_BUFA,
    PSND_BUFB,

    /* WINDOW STUFF */
    PSND_WN,			/* No window			*/
    PSND_EM,			/* Exponential multiplication	*/
    PSND_CD,			/* Convolution difference	*/
    PSND_SN,			/* Sine bell			*/
    PSND_SQ,			/* Squared sine bell		*/
    PSND_HN,			/* Hanning window		*/
    PSND_HM,			/* Hamming window		*/
    PSND_KS,			/* Kaiser window		*/
    PSND_GM,			/* Lorentz-gauss transformation	*/
    PSND_TM,			/* Trapezian multiplication	*/
    PSND_WB,			/* Window from buffer		*/

    PSND_WM,			/* Set window mode		*/
    PSND_SH,			/* Shift			*/
    PSND_LB,			/* Line broadening		*/
    PSND_GB,			/* Fraction for GM window	*/
    PSND_IT,			/* #points for TM window	*/

    /* FT stuff */
    PSND_FZ,			/* Expand and Fill zero		*/
    PSND_IF,			/* Discrete Inverse fourier transform	*/
    PSND_DIF,			/* Inverse fourier transform	*/
    PSND_FT,			/* Fourier transform		*/
    PSND_DFT,			/* Discrete Fourier transform	*/
    PSND_FM,			/* Set Fourier mode		*/
    PSND_ZF,			/* Set value for zero-fill	*/
    PSND_FTSCALE,		/* Set value for scaling 1st point	*/

    /* Phase stuff */
    PSND_PA,			/* Frequency independent phase	*/
    PSND_PB,			/* Frequency dependent phase	*/
    PSND_I0,			/* Frequency dependent position	*/
    PSND_PK,			/* Phase-correction		*/
    PSND_PZ,			/* Reset phase paramaeters	*/
    PSND_PN,			/* Normalize phase paramaeters	*/
    PSND_APK,			/* Automatic phase-correction	*/

    /* Baseline stuff */
    PSND_BASELINE,
    PSND_BC,			/* Baseline correct		*/
    PSND_BM,			/* Set baseline correction mode	*/
    PSND_BT,			/* #polynomal terms		*/

    /* Waterfit stuff */
    PSND_WATERFIT,
    PSND_SHOW_WATERFIT,	/* Fit waterline		*/


    /* array stuff */
    PSND_LS,			/* Left shift real		*/
    PSND_RS,			/* Right shift real		*/
    PSND_RV,			/* Reverse block		*/
    PSND_NM,			/* Negate block			*/
    PSND_ZE,			/* Zero block			*/
    PSND_Z1,			/* Fill block with 1		*/
    PSND_ZR,			/* Fill real with value		*/
    PSND_ZI,			/* Fill imaginary with value	*/
    PSND_ZA,			/* Fill buffer A with value	*/
    PSND_ZB,			/* Fill buffer B with value	*/
    PSND_ZW,			/* Fill Window with value	*/
    PSND_ZL,			/* Fill baseLine with value	*/
    PSND_AV,			/* Calculate absolut value spec	*/
    PSND_PS,			/* Calculate power spectrum	*/

    PSND_IC,			/* Clip array			*/
    PSND_IJ,			/* Switch blocks		*/
    PSND_AT,			/* Additive Transfer		*/
    PSND_MX,			/* Multiplication		*/
    PSND_TR,			/* Transfer blocks		*/
    PSND_TB,			/* Transfer buffer		*/
    PSND_TW,			/* Transfer window		*/
    PSND_TP,			/* Transfer parameters		*/
    PSND_MV,			/* Move array			*/
    PSND_ADDA,			/* Add array			*/
    PSND_MULA,			/* Multiply array		*/

    /* Plot */
    PSND_PL,			/* Plot				*/
    PSND_RE,			/* Plot Real			*/
    PSND_IM,			/* Plot imaginary		*/
    PSND_BA,			/* Plot bufferA			*/
    PSND_BB,			/* Plot bufferB			*/
    PSND_WD,			/* Plot window			*/
    PSND_BL,			/* Plot baseline		*/
    PSND_ER,			/* Erase			*/
    PSND_PX,			
    PSND_PY,
    PSND_SC,			/* Auto scale			*/
    PSND_SC_2D,		/* Auto scale, 2d mode		*/

    PSND_AU,			/* Run script (batch mode0	*/

    PSND_SPEC,			/* Data array contains spectrum	*/
    PSND_FID,			/* Data array contains fid	*/
    PSND_DSPSHIFT,		/* DSP shift for FT		*/

    PSND_SI,
    PSND_TD,
    PSND_AD,
    PSND_ND,
    PSND_SW,			/* Set sweep width		*/
    PSND_SF,			/* Set spectrometer frequency	*/
    PSND_XR,			/* Set reference		*/
    PSND_AREF,			/* Set reference		*/
    PSND_BREF,			/* Set reference		*/
    PSND_RM,			/* Set pre-process mode		*/
    PSND_SM,			/* Set post mode		*/
    PSND_XM,			/* Set reverse mode		*/
    PSND_LP,			/* List current parameters	*/
    PSND_LA,			/* List all parameters		*/
    PSND_LAC,			/* List all current parameters	*/

    PSND_RN,			/* Read next record		*/
    PSND_RTR,			/* Return this record		*/
    PSND_RW,			/* Write next record		*/
    PSND_ST,			/* Store current parameters	*/
    PSND_GP,			/* Reload current parameters	*/
    PSND_GE,			/* Open file for reading	*/
    PSND_OU,			/* Open file for writing	*/
    PSND_OW,			/* Open old file for writing	*/
    PSND_WR,			/* Write array			*/
    PSND_RD,			/* Read array			*/
    PSND_CL,			/* Close infile			*/
    PSND_CF,			/* Close outfile		*/
    PSND_ARRAY_IN,
    PSND_ARRAY_OUT,

    PSND_LO,			/* Start loop			*/
    PSND_EL,			/* End Loop			*/
    
    PSND_BUFFER_EDIT,
    PSND_BUFFER_HEIGHT,
    PSND_CURSOR,
    PSND_VIEW,
    PSND_LINKVIEW,
    PSND_POPUP_ADJUST,
    PSND_XAXIS,
    PSND_YAXIS,
    PSND_FILE_OPEN,
    PSND_FILE_CLOSE,
    PSND_FILE_SAVEAS,
    PSND_FILE_PREPA,
    PSND_COLORMAP,
    PSND_PARAM_ALL,
    PSND_PARAM_RAW,
    PSND_PARAM_LINPAR,
    PSND_PARAM_FOURIER,
    PSND_PARAM_POST,
    PSND_PARAM_FREQ,
    PSND_PARAM_PLOT,
    PSND_PARAM_PHASE,
    PSND_PARAM_WINDOW,
    PSND_PARAM_OUTFILE,
    PSND_PARAM_HILBERT,
    PSND_PARAM_WATWA,
    PSND_PARAM_WATERFIT,
    PSND_DO_WINDOW,
    PSND_SELECT_BLOCK,
    PSND_SET_NUM_BLOCKS,
    PSND_ROW_COLUMN,
    PSND_PLANE_ROW_COLUMN,
    PSND_APPEND_PLOT,
    PSND_POPUP_BOOKMARK,
    PSND_SHOW_BASELINE,
    PSND_SHOW_GRID,
    PSND_PHASE_2D,
    PSND_POPUP_PHASE,
    PSND_POPUP_PHASE_2D,
    PSND_SPLINE_READ,
    PSND_SPLINE_WRITE,
    PSND_SPLINE_RESET,
    PSND_SPLINE_CALC,
    PSND_SHOW_WINDOW,
    PSND_HIDE_WINDOW,
    PSND_LINEFIT,
    PSND_SIMULA,
    PSND_WDUMP,
    PSND_WDUMP_OPEN,
    PSND_WDUMP_CLOSE,
    PSND_RDUMP,
    PSND_RDUMP_OPEN,
    PSND_RDUMP_CLOSE,
    PSND_POPUP_INT1D,
    PSND_POPUP_INT2D,
    PSND_POPUP_CALIBRATE,

    PSND_TRUNC_FLOAT,
    PSND_TRUNC_LEVEL,
    
    PSND_1,
    PSND_2,
    PSND_3,
    PSND_4,
    PSND_5,
    PSND_6,
    PSND_7,
    PSND_8,
    PSND_9,
    PSND_10,
    PSND_11,
    PSND_12,

    PSND_SELECT1,
    PSND_SELECT2,
    PSND_SELECT3,
    PSND_SELECT4,

    PSND_PEAK_PICK,
    PSND_DOKEY_PLUS,
    PSND_DOKEY_MINUS,
    PSND_DOKEY_Q,
    PSND_DOHC,
    PSND_PLOT_1D,
    PSND_PLOT_CONTOUR,
    PSND_LEVELS_BOTH,
    PSND_UNDO,
    PSND_REDO,
    PSND_PLANE_UNDO,
    PSND_PLANE_REDO,
    PSND_PLANE_UP,
    PSND_PLANE_DOWN,
    PSND_PLANE_READ,
    PSND_UP,
    PSND_DOWN,

    PSND_LPC,
    PSND_WATWA,
    PSND_EX,
    PSND_CROP,
    PSND_UNCROP,
    PSND_SCRIPT,
    PSND_RUN_SCRIPT,
    PSND_READ_SCRIPT,
    PSND_SET_VARIABLE,
    PSND_PPM,
    PSND_CHAN,
    PSND_HZ,
    PSND_SEC,

    PSND_SET,
    PSND_ADD,
    PSND_SUB,
    PSND_DIV,
    PSND_MUL,
    PSND_ROL,
    PSND_ROR,

    PSND_SWAP4,
    PSND_SWAP2,
    PSND_I2F,
    PSND_F2I,

    PSND_HT,
    PSND_HILBERT,
    PSND_DIM,

    PSND_HI,
    PSND_HI_FILE,
    PSND_AQ,

    PSND_CHANGEDIR,

    PSND_SETCOLOR,
    PSND_GETCOLOR,
    PSND_LISTCOLOR, 
    
    PSND_XTERM, 

    
} PSND_ids;

#define MODE_P0	(1<<0)
#define MODE_P1	(1<<1)

#define PSND_STRLEN     250

#define NEWFILE		1
#define CREATEFILE	2
#define OLDFILE		3

#define PARSF        4
#define PARRCC       5
#define PARNT0       6
#define PARNTN       7
#define PARAQ        9
#define PARNXT      10
#define PARSFD      39
#define PARSW        1
#define PARREF       2
#define PARPRE      40
#define PARRST      41
#define PARITD      14
#define PARWIN      21
#define PARWTP       3
#define PARRSH       4
#define PARRLB       5
#define PARGN        6
#define PARITM      13
#define PARWST      22
#define PARWSP      23
#define PARFFT      20
#define PARNZF       7
#define PARCST      15
#define PARCSP      16
#define PARPHA       8
#define PARAHO       9
#define PARBHO      10
#define PARI0H      11
#define PARPST      12
#define PARREV      25
#define PARBAS      30
#define PARNTM      32
#define PARBST      34
#define PARBSP      35
#define PARFIL      45
#define PAROF1      18
#define PAROF2      19
#define PARSST      46
#define PARSSP      47
#define NOPRE        0
#define PREREA       1
#define PREIMA       2
#define PREBOT       3
#define NOWIN        0
#define HMWIN        1
#define HNWIN        2
#define SNWIN        3
#define SQWIN        4
#define EMWIN        5
#define GMWIN        6
#define CDWIN        7
#define TMWIN        8
#define KSWIN        9
#define WBWIN       10
#define WAWIN       11
#define DOWIN       11
#define NOFFT        0
#define FFTREA       1
#define FFTIMA       2
#define FFTCX        3
#define DOFFT        3
#define DFTREA       4
#define DFTIMA       5
#define DFTCX        6
#define NOPHA        0
#define DOPHA        1
#define PSPHA        2
#define AVPHA        3
#define NOPST        0
#define PSTREA       1
#define PSTIMA       2
#define PSTBOT       3
#define NOREV        0
#define REVARR       1
#define REVREC       2
#define REVBOT       3
#define NOBAS        0
#define BASPL1       1
#define BASPL2       2
#define BASTB1       3
#define BASTB2       4
#define BASSN1       5
#define BASSN2       6
#define BASCS1       7
#define BASCS2       8
#define BASSP1	     9
#define BASSP2	    10
#define BASSC1	    11
#define BASSC2	    12
#define BASAS1	    13
#define BASAS2	    14
#define BASBFX       9
#define DOBAS        9
#define NEWFIL       0
#define SAMFIL       1
#define OLDFIL       2

#define NOUT1	31
#define NOUT2	32
#define NOUT3	33
#define NOUT4	34
#define NOUT5	35

#define NINP1	11
#define NINP2	12

#define MODE0123	0
#define MODE3210	1
#define MODE0321	2
#define MODE1230	3

#endif

