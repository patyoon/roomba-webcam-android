#include "roombalib.h"

void switchChar(Roomba* roomba, char cmd) {
  
  switch(cmd) {
  case 'w':
    roomba_forward(roomba);
    break;
  default:
    break;
  }
  
}
