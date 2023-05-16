#include "../inc/Shape.hpp"
#include "../inc/Rectangle.hpp"
#include "../inc/Circle.hpp"
#include "string.h"

void gtest(std::string s){
    int a = 0;
}

Shape::Shape() {

}

Shape::Shape(int x) {
    this->x = x;
    this->y = 0;
    type = "Shape";
}

Shape::Shape(int x, int y) {
    this->x = x;
    this->y = y;
    type = "Shape";
}


void Shape::getType() {
    std::cout<<"Shape";
}

std::string Shape::getTypeString() {
    return "Shape";
}

bool Shape::isCollidingWith(Shape& shape) {
    return false;
}

Rectangle::Rectangle(int width, int height) : Shape(0) {
    if (width <= 0) {
        this-> width = 1;
    } else {
        this-> width = width;
    }
    height = height;
    type = "Rectangle";
}

Rectangle::Rectangle(int w, int h, int x, int y) : Shape(x, y) {
    if (w <= 0 && h <= 0) {
        width = 1;
        height = 1;
    } else if (w <=0) {
        width = 1;
        height = h;
    } else if (h <= 0) {
        height = 1;
        width = w;
    } else {
        height = h;
        width = w;
    }
    type = "Rectangle";
}

Rectangle* Rectangle::clone() {
    Rectangle* rect = new Rectangle(width, height, x, y);
    return rect;
}

bool Rectangle::isEqual1(const Rectangle& other) {
    if (this->x == other.x && this->y == other.y){
        if (this->width == other.width && this->height == other.height) {
            return true;
        }
    }
    return false;
}

bool Rectangle::isEqual2(Rectangle other) {
     if (this->x == other.getX() && this->y == other.y){
        if (this->width == other.width && this->height == other.height) {
            return true;
        }
    }
    return false;
}

void Rectangle::getType() {
    std::cout<<"Rectangle";
}

bool test2(Rectangle rect) {
    int a = rect.width;
    a =1;
    return false;
}

std::string Rectangle::getTypeString() {
    return "Rectangle";
}

bool Rectangle::isCollidingWith(Shape& other) {
    if (other.type == "Circle") {
        Circle* circle = dynamic_cast<Circle*>(&other);
        if (circle->x>= this->x && circle->x <= this->x + this->width) {
            if (circle->y >= this->y && circle->y <= this->y + this->height) {
                return true;
            }
        }
    } else if (other.type == "Rectangle") {
        Rectangle* rectangle = dynamic_cast<Rectangle*>(&other);
        if (rectangle->x >= this->x && rectangle->x <= this->x + this->width) {
            if (rectangle->x >= this->y && rectangle->y <= this->y + this->height) {
                return true;
            }
        }
    }

    return false;
}

Circle::Circle(int radius) : Shape(0, 0) {
    if (radius == 0) {
        this->radius = 1;
    } else if (radius == 1) {
        this->radius = 1;
    } else if (radius < 0) {
        this->radius = -radius;
    } else {
        this->radius = radius;
    }
    type = "Circle";
}

Circle::Circle(int radius, int x, int y) : Shape(x, y) {
    this->radius = radius;
    type = "Circle";
}

Circle* Circle::clone() {
    Circle* circle = new Circle(radius, x , y);
    return circle;
}

bool Circle::isEqual(Circle* other) {
    if (this->x == other->x) {
        if (this->y == other->y) {
            if (this->radius == other->radius) {
                return true;
            }
        }
    }
    return false;
}

void Circle::getType() {
    std::cout<<"Circle";
}

void Circle::setRadius(int r) {
    if (r <= 0) {
        radius = 1;
    } else if (r >=1 && r<= 9) {
        radius = r;
    } else {
        radius = 10;
    }
}

std::string Circle::getTypeString() {
    return "Circle";
}

bool Circle::isCollidingWith(Shape& other) {
    if (other.type == "Circle") {
        Circle* circle = (Circle*) (&other);
        if (circle->x >= this->x && circle->x <= this->x + this->radius) {
            if (circle->y >= this->y && circle->y <= this->y + this->radius) {
                return true;
            }
        }
    } else if (other.type == "Rectangle") {
        Rectangle* rectangle = (Rectangle*) (&other);
        if (rectangle->x >= this->x && rectangle->x <= this->x + this->radius) {
            if (rectangle->x >= this->y && rectangle->x <= this->y + this->radius) {
                return true;
            }
        }
    }

    return false;
}

Rectangle::~Rectangle() {
    delete[] arr;
}

