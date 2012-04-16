
CC=gcc
CC_FLAGS=-Wall
L=-lfreenect_sync
I=-I/usr/local/include/libfreenect

all: server

server: tcp_server.o roombalib.o switchChar.o
	${CC} ${CC_FLAGS} ${I} tcp_server.o roombalib.o switchChar.o -o server ${L}

tcp_server.o: tcp_server.c
	${CC} ${CC_FLAGS} ${I} -c tcp_server.c ${L}

switchChar.o: switchChar.c roombalib.o
	${CC} ${CC_FLAGS} -c switchChar.c

roombalib.o: roombalib.c
	${CC} ${CC_FLAGS} -c roombalib.c


