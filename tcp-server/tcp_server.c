#include <stdio.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <netinet/in.h>
#include "switchChar.h"
#include "roombalib.h"
#include <libfreenect_sync.h>
#include <pthread.h>

struct sockaddr_in echoserver;

#define MAXPENDING 5    /* Max connection requests */
#define BUFFSIZE 1
void Die(char *mess) { 
  perror(mess); 
  exit(1); 
}

Roomba* roomba_obj;
int clientsock;
//int vidSock;

void* PerformCommands(void*stuff) {
  char buffer;
  int received = -1;
  while (1) {
    /* Check for next command */
    if ((received = recv(clientsock, &buffer, BUFFSIZE, 0)) < 0) {
      Die("Failed to receive additional bytes from client");
    }
    switchChar(roomba_obj, buffer);
  }
}

#define REAL_W 640
#define REAL_H 480

#define CHUNK 8

#define W (REAL_W / CHUNK)
#define H (REAL_H / CHUNK)

#define PAYLOAD W * 3

void* SendVideo(void*stuff) {
  
  printf("in send video\n");
  
  char* payload = calloc(PAYLOAD, sizeof(char));
  while(1) {
    
    printf("sending frame\n");
    
    int waitSize;
    char wait;
    char* data;
    unsigned int timestamp;
    
    if ((waitSize = recv(clientsock, &wait, 1, 0)) < 0) {
      Die("Error waiting for client ready signal\n");
    }
    
    freenect_sync_get_video((void**)(&data), &timestamp, 0, FREENECT_VIDEO_RGB);
    
    int i, j;
    for (i = 0; i < H; i += 1) {
      
      for (j = 0; j < W; j += 1) {
        
        payload[j*3]   = data[REAL_W * 3 * CHUNK * i + 3 * CHUNK * j];
        payload[j*3+1] = data[REAL_W * 3 * CHUNK * i + 3 * CHUNK * j + 1];
        payload[j*3+2] = data[REAL_W * 3 * CHUNK * i + 3 * CHUNK * j + 2];
        
      }
      
      if (send(clientsock, payload, PAYLOAD, 0) != PAYLOAD) {
        Die("Error delivering RGB\n");
      }
      
    }
    
  }
}

void HandleClient() {
  
  /* Send welcome message */
  
  /*
  char* welcome = "Welcome to the roomba\n";
  if (send(clientsock, welcome, strlen(welcome), 0) != strlen(welcome)) {
    Die("Failed to deliver welcome message");
  }
  */
  
  pthread_t workers[2];
  
#ifdef MODE_COM  
  pthread_create(&workers[0], NULL, PerformCommands, NULL);
  pthread_join(workers[0], NULL);
#endif
  
#ifdef MODE_VID
  pthread_create(&workers[1], NULL, SendVideo, NULL);
  pthread_join(workers[1], NULL);
#endif
  
}

int main(int argc, char *argv[]) {
  int serversock;
  struct sockaddr_in echoclient;
  
  if (argc != 3) {
    fprintf(stderr, "USAGE: echoserver <port> <serial_port>\n");
    exit(1);
  }
  
  /* Create Roomba struct */
#ifndef MODE_VID
  roomba_obj = roomba_init(argv[2]);
#endif

#ifdef MODE_VID
  //vidSock = socket(AF_INET, SOCK_DGRAM, 0);
#endif

  /* Create the TCP socket */
  if ((serversock = socket(PF_INET, SOCK_STREAM, IPPROTO_TCP)) < 0) {
    Die("Failed to create socket");
  }
  /* Construct the server sockaddr_in structure */
  memset(&echoserver, 0, sizeof(echoserver));       /* Clear struct */
  echoserver.sin_family = AF_INET;                  /* Internet/IP */
  echoserver.sin_addr.s_addr = htonl(INADDR_ANY);   /* Incoming addr */
  echoserver.sin_port = htons(atoi(argv[1]));       /* server port */
  /* Bind the server socket */
  if (bind(serversock, (struct sockaddr *) &echoserver,
           sizeof(echoserver)) < 0) {
    Die("Failed to bind the server socket");
  }
  /* Listen on the server socket */
  if (listen(serversock, MAXPENDING) < 0) {
    Die("Failed to listen on server socket");
  }
  /* Run until cancelled */
  while (1) {
    unsigned int clientlen = sizeof(echoclient);
    /* Wait for client connection */
    if ((clientsock =
         accept(serversock, (struct sockaddr *) &echoclient,
                &clientlen)) < 0) {
      Die("Failed to accept client connection");
    }
    fprintf(stdout, "Client connected: %s\n",
            inet_ntoa(echoclient.sin_addr));
    HandleClient();
  }
}
