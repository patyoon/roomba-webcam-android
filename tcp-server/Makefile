
CC=gcc
CC_FLAGS=-Wall
L=-lfreenect_sync
I=-I/usr/local/include/libfreenect

all: vid_server com_server

clean:
	rm -rf *.o vid_server com_server

vid_server: tcp_server.c roombalib.o switchChar.o
	${CC} ${CC_FLAGS} -DMODE_COM ${I} tcp_server.c roombalib.o switchChar.o -o vid_server ${L}

com_server: tcp_server.c roombalib.o switchChar.o
	${CC} ${CC_FLAGS} -DMODE_VID ${I} tcp_server.c roombalib.o switchChar.o -o com_server ${L}

#tcp_server.o: tcp_server.c
#	${CC} ${CC_FLAGS} ${I} -c tcp_server.c ${L}

switchChar.o: switchChar.c roombalib.o
	${CC} ${CC_FLAGS} -c switchChar.c

roombalib.o: roombalib.c
	${CC} ${CC_FLAGS} -c roombalib.c


