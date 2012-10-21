#include <stdio.h>

int main(void){
	char c;
	printf("Echo program\n");
	while((c=getchar())!=EOF){
		putchar(c);
	}
	return 0;
}
