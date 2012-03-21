
CC=gcc
CC_FLAGS=-Wall


all: server

server: main.c roombalib

roombalib: roombalib.c
	${CC} ${CC_FLAGS} -c roombalib.c
