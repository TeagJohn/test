#include <iostream>
#include <cstring>
#include <cstdlib> 
#include "dbg.h"
using namespace std;

class Rectangle {
private:
	int width = 0;
	int height = 0;
public:
	Rectangle() {};
	Rectangle(int w) {
		this->width = w;
	}
	void setWidth(int w) {
					this->width = w;
	}
	void setHeight(int h) {
					this->height = h;
	}
	int getWidth() {
					return width;
	}
	int getHeight() {
					return height;
	}

};


class Square : public Rectangle {
private:
	int size;
	char name[2];
	
public:
	Square(int s) {
		this->size = s;
	}
	Square(int s, char* n) {
		this->size = s;
		strcpy(name, n);
	}
	Square(char *n) {
		strcpy(name, n);
	}
	void setSize(int s){ this->size = s; }
	int getSize(){return size;}

	void setName(char* n){ strcpy(name,n); }
	char* getName(){return name;}
        
};

int area(Rectangle rec) {


}


int main(){
	log_info("main");
	Rectangle *r = new Rectangle();
	r->setWidth(10);
	r->setHeight(100);
	log_info("%d, %d", r->getWidth(), r->getHeight());

	Square *s = new Square("small square");
	s->setSize(20);
	log_info("%s, %d", s->getName(), s->getSize());
	
	return 0;
}
