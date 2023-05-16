#include <stdio.h>

struct Box {
	int width;
	int height;
};

union MyUnion {
	int i;
	float f;
	char arr[10];
	Box boxx;
};

MyUnion myunion;

int g_val1;

int func1(MyUnion u, int x) {
	u.i = 100;
	printf("%d \n", u.i);

	return u.i;
}

int main(int argc, char* argv[]) {
	g_val1++;
	myunion.i = 10;
	printf("%d \n", myunion.i);
	myunion.f = 10.10;
	printf("%f \n", myunion.f);

	printf("%d \n", func1(myunion, 10));
	return 0;
}


