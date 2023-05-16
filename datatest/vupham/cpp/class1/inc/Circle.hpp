#include "Shape.hpp"

struct Circle: public Shape {
    int radius;
    char c;
    char* str;
    float f;
    double d;
    int arr[2];


    Circle(int);
    Circle(int, int, int);

    Circle* clone();

    bool isEqual(Circle*);

    void setRadius(int);

    void getType();

    std::string getTypeString();

    bool isCollidingWith(Shape&) override;
};
