#include <stdio.h>

enum Types {
	SMALL,
	LARGE	
};

struct Box {
	int height;
	int width;
	Types type;
};

Box box;

void box_print() {
				printf("%d %d %d\n", box.height, box.width, box.type);

}

int main(int argc, char* argv[]) {
	box.height = 100;
	box.width = 100;
	box.type = SMALL;

	box_print();

	return 0;
}
