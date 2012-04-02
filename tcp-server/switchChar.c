#include "roombalib.h"

void switchChar(Roomba* roomba, char cmd) {
  
  switch(cmd) {
    
    /* directions */
    
  case 'w':
    roomba_forward(roomba);
    break;
  case 'a':
    roomba_spinleft(roomba);
    break;
  case 's':
    roomba_backward(roomba);
    break;
  case 'd':
    roomba_spinright(roomba);
    break;
    
    /* stop */
    
  case 'p':
    roomba_stop(roomba);
  
    /* ignore everything else */
    
  default:
    break;
  }
  
}
