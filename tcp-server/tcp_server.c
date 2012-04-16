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

#define MAXPENDING 5    /* Max connection requests */
#define BUFFSIZE 1
void Die(char *mess) { 
  perror(mess); 
  exit(1); 
}

Roomba roomba_obj;
int sock;

void* PerformCommands(void*stuff) {
  char buffer;
  int received = -1;
  while (1) {
    /* Check for next command */
    if ((received = recv(sock, &buffer, BUFFSIZE, 0)) < 0) {
      Die("Failed to receive additional bytes from client");
    }
    
    switchChar(&roomba_obj, buffer);
  }
}

#define W 640
#define H 480

void* SendVideo(void*stuff) {
  while(1) {
    char* data;
    unsigned int timestamp;
    freenect_sync_get_video((void**)(&data), &timestamp, 0, FREENECT_VIDEO_RGB);
    int i;
    for (i = 0; i < H; i += 1) {
      // send 3*W bytes of rgb data, offset by i * 3 * W
    }
  }
}

void HandleClient() {
  /* Send welcome message */
  char* welcome = "Welcome to the roomba\n";
  if (send(sock, welcome, strlen(welcome), 0) != strlen(welcome)) {
    Die("Failed to deliver welcome message");
  }
  
  pthread_t workers[2];
  
  pthread_create(&workers[0], NULL, PerformCommands, NULL);
  pthread_create(&workers[1], NULL, SendVideo, NULL);

  pthread_join(workers[0], NULL);
  pthread_join(workers[1], NULL);
  
}

int main(int argc, char *argv[]) {
  int serversock, clientsock;
  struct sockaddr_in echoserver, echoclient;
  
  if (argc != 3) {
    fprintf(stderr, "USAGE: echoserver <port> <serial_port>\n");
    exit(1);
  }
  
  /* Create Roomba struct */
  
  Roomba* roomba_obj = roomba_init(argv[2]);
  
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
    HandleClient(roomba_obj, clientsock);
  }
}
