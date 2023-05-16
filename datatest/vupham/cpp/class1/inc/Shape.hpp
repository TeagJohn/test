#ifndef SHAPE_H
#define SHAPE_H

#include <iostream>
#include <string>

struct Shape {
    int x;
    int y;
    std::string type;

    Shape();
    Shape(int);
    Shape(int, int);

    int getY() {
        return y;
    }

    int getX() {
        return x;
    }

    virtual Shape* clone() = 0;
    
    virtual void getType();

    virtual std::string getTypeString();

    virtual bool isCollidingWith(Shape&);
};

#endif
