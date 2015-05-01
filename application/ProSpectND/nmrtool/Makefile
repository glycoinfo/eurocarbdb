#
# Makefile for nmrtool.a
# Albert van Kuik
#


# Windoze
#LFLAGS = -mwindows -lcomctl32 -lm
#CFLAGS = -I/usr/gcc-2.95.2/i386-mingw32msvc/include 

#CFLAGS = -I../mathtool

#CLINK  = gcc -g -o
# CFLAGS = -g $(WARN)
#CC = gcc -g -c	


OBJ = convolution.o hilbert.o hsvdrfl.o window.o fourier.o dft.o baseline.o\
      phase.o watwa.o bacdic.o ftlipr.o linpredict.o integrate.o peakpick1d.o\
      rmsnoise.o disnmr.o brukx32.o xxarray.o contour.o

SRC = $(OBJ:.o=.c)

all:  nmrtool.a 

.c.o:
	$(CC) $(CFLAGS) $<

nmrtool.a: $(OBJ)
	ar rc $@ $(OBJ) 

clean:
	rm -f *.o *.a core .depend
        
depend:
	$(CC) $(CFLAGS) -M $(SRC) > .depend

dep: depend

sinclude .depend

