/********************************************************************/
/*                              disnmr.c                            */
/*                                                                  */
/*                                                                  */
/* Prepa routines for Bruker ASPECT 2000/3000                       */
/* FTQNMR and DISNMR 1d/2d data                                     */
/*                                                                  
    unpack4
    unpack6
    unpack8
    unpackf
    unpacki
    unpack3to4
    unpack_byte
    negstep

   Albert van Kuik, 1998                                            
                                                                    

    File header is constructed from 512 big endian 3-byte Bruker words

    First convert 3-byte integer to 4 byte integer:

        123 to 3210, Convert to little endian (PC)
                       or
        123 to 0123, Convert to big endian (SGI)

    For the data that flollow the header:
        1) Convert 3-byte to 4-byte integer (unpack3to4)
        2) Convert integers to floats ((float) unpacki)
        3) At every index '4' negate last '2' data (negstep)

    file definition table (FDT)......word 1-16 

    1        uint            fhi (file head identifier)
    2-4      pack6-string    name
    5        uint            sector
    6        int             act size
    7        uint            psa (program start adress)
    8        uint            memory load adress user info
    9        uint            # words in user info
    10       uint            memory load adress file
    11       pack4-string    creation date
    12       byte-1          file program
                             0=not classified     12=RLINK
                             1=DISNMR		  13=PANIC		  	
                             2=ATS		  14=SUCH13
                             3=FTNMR2D		  15=PASCAL
                             4=EPRDATA		  16=FORTRAN
                             5=CXPNMR		  17=LIB
                             6=DISCXP	          18=PASCAL
                             7=FTQNMR             19=IR search files
                             8=DISNxx	      	  20-199=not used
                             9=not used           200-255=user programs
                             10=TECO
                             11=MACRO
    12       byte-2          file class
    12       byte-3          file status
    13-15    uint            reserved
    16       uint            'exor'-checksum for the 512 words on tape
    17-40    pack8-string    file comment

    measuring parameters for FTQNMR......word 41-255
    41       int             SI
    42       int             GLOBEX (GX)
    43       int             SWPCOM ( actual NS)
    44       int             DE
    45       int             DW
    46       int             FW
    47       uint            FN (FILTER NUMBER)
    48       uint            F1N (DIVIDERS for INTERFACE)
    49       uint            F1M
    50       uint            F2N
    51       uint            F2M
    52-53    float           RD
    54-55    float           SW
    56-57    float           AQ
    58-59    float           O1
    60-61    float           O
    62-63    float           HZPPT
    64-65    float           TOTLB
    66-67    float           PULSET
    68       uint            CRDEL
    69       uint            PW
    70       uint            CDELAY pre scan delay (125 usec)
    71       int             CSWEEP = # scans to do
    72       int             ADC initial
    73       int             ADC final
    74       int             ADC current
    75-77    pack6-string    name for ADAKOS
    78-79    float           SF
    80       int             quad flag (QN=1, QF=0)
    81       uint            DCW (digitizer control word)
    82       int             DP
    83       int             DP mode
    84       uint            interface setting (hardware flag)
                             0=PO, 1=BB, 2=CW, 3=HG, 4=DO, 5=HD, 6=HB, 
                             20=AP, 40=QP, 400=QN
    85       int             AP=1 ,CP=0 , QP=-1
    86       int             TE
    87       int             stop from 0=GO, 1=non-locked AU, -1=locked AU,
                             -2=CTRL_K
    88       int             pulser range and preamp setting
    89       int             RG receiver gain
    90       int             SY1   1=0.1HZ-10kHZ   2=100kHZ-10MHZ
    91       int             SY2   1=0.1HZ-10kHZ   2=100kHZ-10MHZ
    92-93    pack6-string    nucleus
      

    measuring parameters for DISNxx......word 41-255

    41       int             SI
    42       int             NC (data normalization constant)
    43       int             SWPCOM ( actual NS)
    44       int             TD
    45       int             DW
    46       int             FW
    47       int             FN (FILTER NUMBER)
    48       uint            F1N (DIVIDERS for INTERFACE)
    49       uint            F1M
    50       uint            F2N
    51       uint            F2M
    52       int             VD, variable delay 
    53       int             DS
    54-55    float           SW
    56       int             AM (acquisition mode)
    57       int             NE
    58-59    float           O1
    60-61    float           O2
    62       int             PH0, zero order PK
    63       int             PH1, first order PK
    64-65    float           TOTLB
    66-67    float           NTHPT / Nth point of FT, double precision
    68       int             RD, relaxation delay
    69       int             PW, pulse excitation width
    70       int             DE, preacquisition delay
    71       int             CSWEEP = # scans to do
    72       int             ADC initial
    73       int             ADC final
    74       int             ADC current
    75       int             SP, initial SP delay
    76       int             PF, PHZFLG, A0-A3 phase flag
    77       int             OR, ORIGPS, original DISNMR parameters
    78-79    float           SF
    80       int             QN, quad flag (QN=1, QF=0)
    81       uint            DCW, DCTRLW, digitizer control word
    82       int             DP, DATTEN, decoupler attenuator
    83       int             RB, receiver blanking
    84       uint            interface setting (hardware flag)
                             0=PO, 1=BB, 2=CW, 3=HG, 4=DO, 5=HD, 6=HB, 
                             20=AP, 40=QP, 400=QN
    85       int             AP=1 ,CP=0 , QP=-1
    86       int             TE, temperature control word
    87       int             data status, stop from 0=GO, 
                             1=non-locked AU, -1=locked AU, -2=CTRL_K
    88       int             PR pulser control word
    89       int             RG receiver gain control word
    90       int             SY synthesizer control word
    91       int             SY synthesizer control word
    92       int             DIM / image dimensions
    93       int             FIM / imaging flags
    95       int             ID, increment on D0 duration
    96       int             PI / parameter set identifier
    97       int             SS 1st  synthesizer control word
    98       int             SS 2nd  synthesizer control word
    99-100   float           SF0
    101-102  float           SF02
    104-106  pack6-string    AU (automation program name)
    107      int             date
    108      int             F1 control word
    109      int             F2 control word

********************************************************************/
#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include "mathtool.h"
#include "nmrtool.h"


/*
 *      rb; 860107
 *-----
 *      unpack 4
 *      converts 4bit coded bruker-word to 6 character string
 *      used for: creation date
 *-----
 */
char *unpack4(int jb)
{
    int i,ih1,ih2,ih3,ioff = 48;
    static char bc[7];


    ih1 = jb;
    ih2 = ih1;
    for (i=0;i<6;i++) {
        ih1 /= 16;
        ih3 = ih1 * 16;
        bc[5-i] = ih2-ih3+ioff;
        ih2 /= 16;
    }
    bc[6] = '\0';
    return bc;
}



/*
 *
 *      rb; 860107
 *-----
 *      unpack 6
 *      converts sixbit coded bruker-word to 3 character string
 *-----
 */
char *unpack6(int jb)
{
    int i,ih1,ih2,ih3;
    char bc;
    static char c[5];

    ih1 = jb;
    ih2 = ih1;
    for (i=0;i<4;i++) {
        ih1 /= 64;
        ih3 = ih1 * 64;
        bc = ih2 - ih3;
       
        ih1 = jb >> (6*i);
        ih1 = ih1 & 0x3f; 
        bc = (char) ih1;

        if (bc == 0) 
            bc = 32;
        if (bc < 31) 
            bc += 64;
        c[3-i] = (char) bc;
        ih2 /= 64;
    }
    c[4] = '\0';
    return c;
}


/*
 *      rb; 860107
 *-----
 *      unpack 8
 *      converts 8bit coded bruker-word to 3 character string
 *-----
 */
char *unpack8(int jb)
{
    int i;
    unsigned char *bi;
    static char bc[4];

    bi = (unsigned char*) &jb;
    for (i=0;i<3;i++) 
        bc[2-i] = (char) ((jb >> (8*i)) & 0x7f);
    bc[3] = '\0';
    return bc;
}



/*
 *
 *      rb; 891120
 *-----
 *      unpack f
 *      converts floating point bruker-word to 32 bits floating point
 *-----
 */
float unpackf(int jb1, int jb2)
{
    float  f1;
    char   *ibyte, *jbyte;
    int    i,ib1,ib2,iexp;
    double r1;

    ibyte = (char*) &ib1;
    jbyte = (char*) &ib2;

    /*
     *......zero
     */
    if (jb1 == 0 && jb2 == 0) 
        return 0.0;

    ib1 = jb1;
    ib2 = jb2;
    /*
     *......get exponent and low mantissa
     */
    iexp = ib2/8192;
    ib2  = ib2 - iexp * 8192;
    if (iexp >= 1024) 
        iexp -= 2048;
    /*
     *......mix all bits of high and 8 bits of low mantissa 
     *      into a 32bit word
     *
    */
    ib2  = ib2/32  +  ib1*256;

    /*
     *......conversion to float
     */
    ib1   = iexp;
    r1    = (double) ib2;
    iexp -= 31;
    /*
     *......overflow, too large
     */
    if (iexp > 30) {
        f1     = r1;
        return sign(1.E38,f1);
    }
    /*
     *.......conversion for iexp=+
     */
    else if (iexp >= 0) 
        return r1 *  pow(2,iexp);
    /*
     *......overflow, too small
     */
    else if (iexp < -60) {
        f1     = r1;
        return sign(1.E-37,f1);
    }
    /*
     *.......conversion for small negative iexp
     */
    else if (iexp < -30) 
        return r1 * pow(2.0,iexp);
    /*
     *.......conversion for iexp=-
     */
     return r1/pow(2,abs(iexp));
}



/*
 *
 *      rb; 860107
 *-----
 *      unpack i
 *      converts integer*3 bruker-word to 32 bit integer
 *      two-complement for negative numbers
 *      m1 = 2**24   = 1 000000000000000000000000
 *      m0 = 2**23-1 = 0 011111111111111111111111
 *-----
 */
int unpacki(int jb)
{
    int m0,m1;

    m0 = 0x7fffff;
    m1 = 0x1000000;
    if (jb > m0) 
        return jb - m1;
    return jb;
}



/*
 * convert array of 3-byte integers to array of 4 byte integers
 *
 * in     = input array
 * out    = output array
 * start  = first integer in input array
 * stop   = last integer in input array
 * mode   = MODE0123: convert 123 to 0123
 *          MODE3210: convert 123 to 3210
 *          MODE0321: convert 123 to 0321
 *          MODE1230: convert 123 to 1230
 * return = size of output array in integers
 */
int unpack3to4(int *in, int *out, int start_in, int stop_in, 
               int start_out, int stop_out, int mode)
{
    unsigned char *pin, *pout;
    int i,size,size_in,size_out;

    size_in  = stop_in - start_in + 1;
    size  = size_in * 4/3;
    if (start_out <= 0)
        start_out = 1;
    if (stop_out <= 0)
        stop_out = start_out + size -1;
    size_out = stop_out - start_out + 1;
    if (size > size_out) 
        size = size_out;
    in   += start_in-1;
    out  += start_out-1;
    pin   = (unsigned char*) in;
    pout  = (unsigned char*) out;
    if (mode == MODE0123) {
        /*
         * 0123
         */
        for (i=0;i<size;i++) {
            pout[0] = 0;
            pout[1] = pin[0];
            pout[2] = pin[1];
            pout[3] = pin[2];
            pin  += 3;
            pout += 4;
        }
     }
     else if (mode == MODE3210) {
        /*
         * 3210
         */
        for (i=0;i<size;i++) {
            pout[0] = pin[2];
            pout[1] = pin[1];
            pout[2] = pin[0];
            pout[3] = 0;
            pin  += 3;
            pout += 4;
        }
    }
    else if (mode == MODE0321) {
        /*
         * 0321
         */
        for (i=0;i<size;i++) {
            pout[0] = 0;
            pout[1] = pin[2];
            pout[2] = pin[1];
            pout[3] = pin[0];
            pin  += 3;
            pout += 4;
        }
    }
    else if (mode == MODE1230) {
        /*
         * 1230
         */
        for (i=0;i<size;i++) {
            pout[0] = pin[0];
            pout[1] = pin[1];
            pout[2] = pin[2];
            pout[3] = 0;
            pin  += 3;
            pout += 4;
        }
    }
    else
        return size * 3 / 4;
    return size;
}



/*
 * Get 1 byte from an integer
 * jb    = the integer
 * index = the byte index in the integer
 *         if index == 1 then return first byte ( jb & 0xff)
 */
int unpack_byte(int jb, int index)
{
    int shift = (index - 1) * 8;
    return (jb >> shift) & 0xff;
}




/*
 * at every index 'is' negate last 'ns' data
 */
void negstep(float *data, int n, int is, int ns)
{
    int n2,i,j;

    n2=n/is;
    for (i=1;i<=n2;i++)
        for (j=0;j<ns;j++)
            data[i*is-j-1] = -data[i*is-j-1];
}


