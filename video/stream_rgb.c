
#include <stdio.h>
#include <libfreenect_sync.h>

int main()
{
  printf("go\n");
  int i;
  for (i = 0; i < 100; i ++) {
    char* data;
    unsigned int timestamp;
    freenect_sync_get_video((void**)(&data), &timestamp, 0, FREENECT_VIDEO_RGB);
    printf("Got RGB...\n");
  }
  return 0;
}

