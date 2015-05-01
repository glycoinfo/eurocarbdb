#
# Makefile for mathtool.a
# Albert van Kuik
#



# Windoze
# LFLAGS = -mwindows -lcomctl32 -lm
# CFLAGS = -I/usr/gcc-2.95.2/i386-mingw32msvc/include -I.. 

#CLINK  = gcc -g -o
#DEFS =  -DMSHELL
#CFLAGS = -g $(WARN) $(DEFS)
#CC = gcc -g -c	

OBJ1 = drotg.o dscal.o zaxpy.o zdotc.o zdotu.o zscal.o zswap.o dznrm2.o zcopy.o\
      zdrot.o zsvdc.o zsortip.o zppfa.o zppsl.o cbal.o corth.o comqr.o csroot.o\
      cdiv.o pythag.o hsvd.o lpsvd.o cdexp.o cdlog.o ampfit.o csign.o zpoly.o

OBJ2 = cgesvd.o cgeev.o cgelss.o cbdsqr.o cgebrd.o cgemm.o cgeqrf.o cgeqr2.o\
      cgelqf.o cgelq2.o cgemv.o cgebd2.o cgebal.o cgebak.o cgehrd.o cgehd2.o\
      cgerc.o cunmqr.o cunm2r.o cungbr.o

OBJ3 = cungqr.o cunglq.o cungl2.o cung2r.o cunmlq.o cunml2.o cunmbr.o cunghr.o\
      cdotc.o cdotu.o caxpy.o ccopy.o\
      clacpy.o clabrd.o clange.o clascl.o claset.o clarfg.o csrscl.o csscal.o\
      csrot.o clacgv.o clasr.o classq.o clarf.o clarft.o clarfb.o clarfx.o\
      cladiv.o clahqr.o clahrd.o clanhs.o clatrs.o

OBJ4 = cswap.o cscal.o ctrmm.o ctrmv.o ctrevc.o ctrsv.o chseqr.o slamch.o\
      slabad.o sladiv.o slaset.o\
      slascl.o slasq1.o slasq2.o slasq3.o slasq4.o slartg.o slasrt.o slasv2.o\
      slas2.o scopy.o sscal.o scasum.o scnrm2.o slapy2.o slapy3.o ilaenv.o\
      icamax.o isamax.o s_cat.o s_copy.o s_cmp.o\
      memcof.o vector.o gauss.o minmax.o inverse.o givens.o\
      interlv.o complex.o simplx.o svdfit.o lfit.o gaussj.o mrqmin.o

OBJ = $(OBJ1) $(OBJ2) $(OBJ3)

SRC = $(OBJ:.o=.c)
SRC1 = $(OBJ1:.o=.c)
SRC2 = $(OBJ2:.o=.c)
SRC3 = $(OBJ3:.o=.c)
SRC4 = $(OBJ4:.o=.c)


all: mathtool.a bla2 bla3

.c.o:
	$(CC) $(CFLAGS) $<

mathtool.a: $(OBJ1) bla2 bla3 bla4
	ar rc $@ $(OBJ1)

bla2: $(OBJ2) 
	ar rc mathtool.a $(OBJ2)

bla3: $(OBJ3) 
	ar rc mathtool.a $(OBJ3)

bla4: $(OBJ4) 
	ar rc mathtool.a $(OBJ4)

clean:
	rm -f *.o *.a .depend

depend44:
	$(CC) $(CFLAGS) -M $(SRC1) $(SRC2) > .depend

depend:
	$(CC) $(CFLAGS) -M $(SRC1) > .depend
	$(CC) $(CFLAGS) -M $(SRC2) >> .depend
	$(CC) $(CFLAGS) -M $(SRC3) >> .depend
	$(CC) $(CFLAGS) -M $(SRC4) >> .depend

dep: depend

sinclude .depend

