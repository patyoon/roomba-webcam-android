CC=gcc
FLAGS=-Wall
I=/usr/local/include/libfreenect
L=-lfreenect_sync

all: stream_rgb

stream_rgb: stream_rgb.c
	${CC} ${FLAGS} -I${I} -o stream_rgb stream_rgb.c ${L}
