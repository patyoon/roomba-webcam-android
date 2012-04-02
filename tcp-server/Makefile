
CC=gcc
CC_FLAGS=-Wall


all: server

server: tcp_server.o roombalib.o switchChar.o
	${CC} ${CC_FLAGS} tcp_server.o roombalib.o switchChar.o -o server	

server.o: tcp_server.c
	${CC} ${CC_FLAGS} -c tcp_server.c

switchChar.o: switchChar.c roombalib.o
	${CC} ${CC_FLAGS} -c switchChar.c

roombalib.o: roombalib.c
	${CC} ${CC_FLAGS} -c roombalib.c


