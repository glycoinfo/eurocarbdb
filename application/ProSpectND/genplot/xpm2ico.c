/*
 * xpm2ico.c
 */

#include <stdlib.h>
#include <stdio.h>
#include <string.h>

#ifdef info
typedef struct _IconFile
{

    WORD      Reserved;      /* Reserved (always 0) */
    WORD      ResourceType;  /* Resource ID (always 1) */
    WORD      IconCount;     /* Number of icon bitmaps in file */
    ICONENTRY IconDir[];     /* Directory of icon entries */
    ICONDATA  IconData[];    /* Listing of ICO bitmaps */

} ICONFILE;

typedef struct _IconEntry
{

    BYTE  Width;        /* Width of icon in pixels */
    BYTE  Height;       /* Height of icon in pixels */
    BYTE  NumColors;    /* Maximum number of colors */
    BYTE  Reserved;     /* Not used (always 0) */
    WORD  NumPlanes;    /* Not used (always 0) */
    WORD  BitsPerPixel; /* Not used (always 0) */
    DWORD DataSize;     /* Length of icon bitmap in bytes */
    DWORD DataOffset;   /* Offset position of icon bitmap in file */
} ICONENTRY;

typedef struct _IconData
{
    WIN3XBITMAPheadER   header;         /* Bitmap header data */
    WIN3XPALETTEELEMENT Palette[];      /* Color palette */
    BYTE                XorMap[];       /* Icon bitmap */
    BYTE                AndMap[];       /* Display bit mask */
} ICONDATA;

typedef struct _Win3xBitmapHeader
{
    DWORD Size;            /* Size of this header in bytes */
    LONG  Width;           /* Image width in pixels */
    LONG  Height;          /* Image height in pixels */
    WORD  Planes;          /* Number of color planes */
    WORD  BitsPerPixel;    /* Number of bits per pixel */
    /* Fields added for Windows 3.x follow this line */
    DWORD Compression;     /* Compression methods used */
    DWORD SizeOfBitmap;    /* Size of bitmap in bytes */
    LONG  HorzResolution;  /* Horizontal resolution in pixels per meter */
    LONG  VertResolution;  /* Vertical resolution in pixels per meter */
    DWORD ColorsUsed;      /* Number of colors in the image */
    DWORD ColorsImportant; /* Minimum number of important colors */
} WIN3XBITMAPHEADER;


/* XPM */
static char * psnd_icon_xpm[] = {
"16 16 12 1",
" 	c None",
".	c #000000",
"+	c #938E45",
"@	c #B2AA3E",
"#	c #D6CA22",
"$	c #EAEA15",
"%	c #F4210E",
"&	c #3E5B6D",
"*	c #3A5E75",
"=	c #347299",
"-	c #2C82B7",
";	c #2092D8",
"................",
"................",
"................",
".....++++++++...",
"....@@@@@@@@+...",
"...########@+...",
"..$$$$$$$$#@+...",
".%%%%%%%%$#@+...",
".%%%%%%%%$#&&&&.",
".%%%%%%%%$****..",
".%%%%%%%%====...",
".%%%%-------....",
".%%%;;;;;;;.....",
".%%%%%%%%$......",
".%%%%%%%%.......",
"................"};


#endif

#define BYTE unsigned char
#define WORD short
#define DWORD int
#define LONG int

typedef struct _IconFileHeader
{
    WORD      Reserved;      /* Reserved (always 0) */
    WORD      ResourceType;  /* Resource ID (always 1) */
    WORD      IconCount;     /* Number of icon bitmaps in file */

} ICONFILEHEADER;

typedef struct _IconEntry
{
    BYTE  Width;        /* Width of icon in pixels */
    BYTE  Height;       /* Height of icon in pixels */
    BYTE  NumColors;    /* Maximum number of colors */
    BYTE  Reserved;     /* Not used (always 0) */
    WORD  NumPlanes;    /* Not used (always 0) */
    WORD  BitsPerPixel; /* Not used (always 0) */
    DWORD DataSize;     /* Length of icon bitmap in bytes */
    DWORD DataOffset;   /* Offset position of icon bitmap in file */
} ICONENTRY;

typedef struct _Win3xBitmapHeader
{
    DWORD Size;            /* Size of this header in bytes */
    LONG  Width;           /* Image width in pixels */
    LONG  Height;          /* Image height in pixels */
    WORD  Planes;          /* Number of color planes */
    WORD  BitsPerPixel;    /* Number of bits per pixel */
    /* Fields added for Windows 3.x follow this line */
    DWORD Compression;     /* Compression methods used */
    DWORD SizeOfBitmap;    /* Size of bitmap in bytes */
    LONG  HorzResolution;  /* Horizontal resolution in pixels per meter */
    LONG  VertResolution;  /* Vertical resolution in pixels per meter */
    DWORD ColorsUsed;      /* Number of colors in the image */
    DWORD ColorsImportant; /* Minimum number of important colors */
} WIN3XBITMAPHEADER;

typedef struct _Win3xPaletteElement
{
    BYTE Blue;      /* Blue component */
    BYTE Green;     /* Green component */
    BYTE Red;       /* Red component */
    BYTE Reserved;  /* Padding (always 0) */
} WIN3XPALETTEELEMENT;

typedef struct xpm_pal_ {
    char id;
    int r,g,b;
} XPMPAL;

void write4bits(FILE *outfile, int byte)
{
    static BYTE buf;
    static int  inbuf;
    
    byte &= 0xf;
    if (inbuf) {
        buf =  (((BYTE) byte) | buf << 4);
        fwrite(&buf, sizeof(BYTE), 1, outfile);
        inbuf = 0;
    }
    else {
        buf = (BYTE) byte;
        inbuf = 1;    
    }
}

void write1bit(FILE *outfile, int byte)
{
    static BYTE buf, mask;
    static int  inbuf;

    if (!byte)
        mask = 1 << (7-inbuf);
    else
        mask = 0;
    buf = buf | mask;
    inbuf++;
    if (inbuf == 8) {
        fwrite(&buf, sizeof(BYTE), 1, outfile);
        inbuf = 0;
        buf = 0;
    }
}

void write_headers(FILE *outfile, int width, int height, 
                  int maxcol, int bitsperpixel)
{
    ICONENTRY iconentry;
    WIN3XBITMAPHEADER bmh;
    ICONFILEHEADER header;

    header.Reserved     = 0;
    header.ResourceType = 1;
    header.IconCount    = 1;
    fwrite(&header, sizeof(ICONFILEHEADER), 1, outfile);

    iconentry.Width        = (BYTE) width;        
    iconentry.Height       = (BYTE) height;        
    iconentry.NumColors    = (BYTE) maxcol;     
    iconentry.Reserved     = (BYTE) 0;      
    iconentry.NumPlanes    = (WORD) 0;     
    iconentry.BitsPerPixel = (WORD) 0;  
    /* xor + and map */
    iconentry.DataSize     = (DWORD) (width * height * bitsperpixel)/8 + 
                                     (width * height)/8 +
                                      sizeof(WIN3XBITMAPHEADER) + 
                                      sizeof(WIN3XPALETTEELEMENT) * maxcol;
    iconentry.DataOffset   =  (DWORD) sizeof(ICONFILEHEADER)+
                                      sizeof(ICONENTRY);    
    fwrite(&iconentry, sizeof(ICONENTRY), 1, outfile);

    bmh.Size            = sizeof(WIN3XBITMAPHEADER);	/* Size of this header in bytes */
    bmh.Width           = width;			/* Image width in pixels */
    bmh.Height          = height * 2;			/* Image height in pixels */
    bmh.Planes          = 1;				/* Number of color planes */
    bmh.BitsPerPixel    = bitsperpixel;			/* 1,4,8 or 24 Number of bits per pixel */
    bmh.Compression     = 0;				/* Compression methods used */
    bmh.SizeOfBitmap    = (width * height * bitsperpixel)/8 + 
                                  (width * height)/8;	/* Size of bitmap in bytes */
    bmh.HorzResolution  = 0;				/* Horizontal resolution in pixels per meter */
    bmh.VertResolution  = 0;				/* Vertical resolution in pixels per meter */
    bmh.ColorsUsed      = 0;				/* Number of colors in the image */
    bmh.ColorsImportant = 0;				/* Minimum number of important colors */
    fwrite(&bmh, sizeof(WIN3XBITMAPHEADER), 1, outfile);
}

int main(int argc, char *argv[])
{
    FILE *infile, *outfile;
    WIN3XPALETTEELEMENT pal;
    char buf[200], *p, *lines[200];
    int init, width, height, numcol, numchar;
    int i, j, icount, maxcol, bitsperpixel;
    XPMPAL xpmpal[200];
    
    if (argc<3)
        exit(1);
    infile = fopen(argv[1],"r");
    if (infile == NULL)
        exit(1);
    outfile = fopen(argv[2],"w");
    if (outfile == NULL)
        exit(1);

maxcol = 16;
bitsperpixel = 4;

    init = 0;
    icount = 0;
    while (fgets(buf,200,infile) != NULL) {
        if (buf[0] != '"')
            continue;
        if (init == 0) {
            sscanf(buf+1, "%d %d %d %d", 
                &width, &height, &numcol, &numchar);
            init = 1;
            write_headers(outfile, width, height, maxcol, bitsperpixel);
        }
        else if (icount < maxcol) {
            int r,g,b;
            p = buf + 1;
            xpmpal[icount].id = *p;
            if (strstr(p+1,"None") != NULL || 
                   strstr(p+1,"none") != NULL) {
                xpmpal[icount].r = 0;
                xpmpal[icount].g = 0;
                xpmpal[icount].b = 0;
                pal.Blue  = (BYTE) 0;
                pal.Green = (BYTE) 0;
                pal.Red   = (BYTE) 0;
                pal.Reserved = (BYTE) 0;
                fwrite(&(pal), sizeof(WIN3XPALETTEELEMENT), 1, outfile);
                icount++;
                continue;
            }
            if ((p = strchr(p+1, 'c')) == NULL)
                continue;
            if ((p = strchr(p+1, '#')) == NULL)
                continue;
            sscanf(p, "#%2X%2X%2X", &r, &g, &b);
            xpmpal[icount].r = r;
            xpmpal[icount].g = g;
            xpmpal[icount].b = b;
            pal.Blue  = (BYTE) b;
            pal.Green = (BYTE) g;
            pal.Red   = (BYTE) r;
            pal.Reserved = (BYTE) 0;
            fwrite(&(pal), sizeof(WIN3XPALETTEELEMENT), 1, outfile);
            icount++;
            if (icount == numcol ) {
                if (icount < maxcol)
                    for (i=icount;i<maxcol;i++) {
            xpmpal[i].id = 'x';
                xpmpal[i].r = i;
                xpmpal[i].g = i;
                xpmpal[i].b = i;
            pal.Blue  = (BYTE) i;
            pal.Green = (BYTE) i;
            pal.Red   = (BYTE) i;
                        fwrite(&(pal), sizeof(WIN3XPALETTEELEMENT), 1, outfile);
                    }
                break;
            }
        }
    }
    for (i=0;i<height;i++) {
        lines[i] = (char*) malloc(sizeof(char) * 100);
        if (fgets(buf,100,infile) == NULL) 
            exit(1);
        for (j=0;buf[j] != 0;j++)
            lines[i][j] = buf[j];

        lines[i][j] = 0;
    }
    for (i=height-1;i>=0;i--) {
        p = lines[i] + 1;
        while (p && *p && *p != '"') {
            for (j=0;j<numcol;j++) {
                if (*p == xpmpal[j].id) {
                    write4bits(outfile, j);
                    break;
                }
            }
            p++;
        }
    }    
    for (i=height-1;i>=0;i--) {
        p = lines[i] + 1;
        while (p && *p && *p != '"') {
            for (j=0;j<numcol;j++) {
                if (*p == xpmpal[j].id) {
                    write1bit(outfile, j);
                    break;
                }
            }
            p++;
        }
    }   
    for (i=0;i<height;i++) 
        free(lines[i]);

    fclose(infile);
    fclose(outfile);
    return 0;
}

