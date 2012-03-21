
CC=gcc
CC_FLAGS=-Wall


all: switchChar.o roombalib.o

server: main.c roombalib

switchChar.o: switchChar.c roombalib.o
	${CC} ${CC_FLAGS} -c switchChar.c

roombalib.o: roombalib.c
	${CC} ${CC_FLAGS} -c roombalib.c
