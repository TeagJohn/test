#include <iostream>
#include <algorithm>
#include <cmath>

using namespace std;

const double PI = acos(-1);

struct Point {
    public:
    float pointX;
    float pointY;

    Point() {

    }

    Point(float pointX, float pointY) {
        this->pointX = pointX;
        this->pointY = pointY;
    }

    float distance(Point* other) {
        return sqrt((pointX - other->pointX) * (pointX - other->pointX)
                + (pointY - other->pointY) * (pointY - other->pointY));
    }

    float distance(Point other) {
        return sqrt((pointX - other.pointX) * (pointX - other.pointX)
                + (pointY - other.pointY) * (pointY - other.pointY));
    }
};

class Shape {
    protected:
        string color;
        bool filled;
    public:

        Shape(string color, bool filled) {
            this->color = color;
            this->filled = filled;
        }

        ~Shape() {

        }

        string getColor() {
            return color;
        }

        void setColor(string color) {
            this->color = color;
        }

        bool isFilled();

        void setFilled(bool filled);

        virtual double getArea() {
            return 0;
        };

        virtual double getPerimeter() {
            return 0;
        }
};

bool Shape::isFilled() {
    return filled;
}

void Shape::setFilled(bool filled) {
    this->filled = filled;
}

class Circle : public Shape {
    private:
        Point center;
        double radius;
    public:
        // Circle() : Shape() {
        //     center.pointX = 0;
        //     center.pointY = 0;
        //     radius = 0.0;
        // }

        // Circle(Point center, double radius, string color, bool filled)
        //     : Shape(color, filled) {
        //     this->center = center;
        //     this->radius = radius;
        // }

        Circle(float x, float y, double radius, string color, bool filled);

        ~Circle() {
            
        }

        Point getCenter() {
            return center;
        }

        void setCenter(Point center) {
            this->center = center;
        }

        double getRadius();

        void setRadius(double radius);

        double getArea();

        double getPerimeter() {
            return 2.0 * PI * radius;
        }
};

Circle::Circle(float x, float y, double radius, string color, bool filled)
    : Shape(color, filled) {
    center.pointX = x;
    center.pointY = y;
    this->radius = radius;
}

double Circle::getRadius() {
    return radius;
}

void Circle::setRadius(double radius) {
    this->radius = radius;
}

double Circle::getArea() {
    return PI * radius * radius;
}


class Rectangle : public Shape {
    protected:
        Point topLeft;
        double width;
        double length;
    public:
        // Rectangle() : Shape() {
        //     topLeft.pointX = 0;
        //     topLeft.pointY = 0;
        //     width = length = 0.0;
        // }

        Rectangle(float x, float y, double width, double length, string color, bool filled)
            : Shape(color, filled) {
                topLeft.pointX = x;
                topLeft.pointY = y;
                this->width = width;
                this->length = length;
        }

        double getWidth() {
            return width;
        }

        void setWidth(double width) {
            this->width = width;
        }

        double getLength();

        void setLength(double length);

        double getArea() {
            return width * length;
        }

        double getPerimeter();

        Point getTopLeft() {
            return topLeft;
        }

        void setTopLeft(Point topLeft) {
            this->topLeft = topLeft;
        }
};

double Rectangle::getLength() {
    return length;
}

void Rectangle::setLength(double length) {
    this->length = length;
}

double Rectangle::getPerimeter() {
    return 2.0 * (width + length);
}

class Square : public Rectangle {
    public:
        // Square() : Rectangle() {

        // }

        Square(float x, float y, double width, double length, string color, bool filled);

        double getSide() {
            return width;
        }

        void setSide(double side) {
            width = length = side;
        }
};

Square::Square(float x, float y, double width, double length, string color, bool filled)
    : Rectangle(x, y, width, length, color, filled) {
}

bool checkRectangleColide(Rectangle recA, Rectangle *recB) {
    if (recB == nullptr) return false;

    double topLeftXRecA = recA.getTopLeft().pointX;
    double topRightXRecA = topLeftXRecA + recA.getWidth();
    double topLeftYRecA = recA.getTopLeft().pointY;
    double botLeftYRecA = topLeftYRecA - recA.getLength();

    double topLeftXRecB = recB->getTopLeft().pointX;
    double topRightXRecB = topLeftXRecB + recB->getWidth();
    double topLeftYRecB = recB->getTopLeft().pointY;
    double botLeftYRecB = topLeftYRecB - recB->getLength();

    if (min(topRightXRecB, topRightXRecA) < max(topLeftXRecA, topLeftXRecB)) {
        return true;
    }

    if (max(botLeftYRecB, botLeftYRecA) > min(topLeftYRecA, topLeftYRecB)) {
        return true;
    }

    if (topLeftXRecA < topLeftXRecB && topRightXRecB < topRightXRecA
        && botLeftYRecA < botLeftYRecB && topLeftYRecB < topLeftYRecA) {
        return true;
    }

    if (topLeftXRecB < topLeftXRecA && topRightXRecA < topRightXRecB
        && botLeftYRecB < botLeftYRecA && topLeftYRecA < topLeftYRecB) {
        return true;
    }

    return false;
}

bool checkRectangleColideCircle(Rectangle *rec, Circle *circle) {
    cout << 1;
    if (rec == nullptr || circle == nullptr) {
        return false;
    }

    Point *closestPoint = new Point();

    if (circle->getCenter().pointX < rec->getTopLeft().pointX) {
        closestPoint->pointX = rec->getTopLeft().pointX;
    } else if (circle->getCenter().pointX > rec->getTopLeft().pointX + rec->getWidth()) {
        closestPoint->pointX = rec->getTopLeft().pointX + rec->getWidth();
    } else {
        closestPoint->pointX = circle->getCenter().pointX;
    }

    if (circle->getCenter().pointY < rec->getTopLeft().pointY) {
        closestPoint->pointY = rec->getTopLeft().pointY;
    } else if (circle->getCenter().pointY > rec->getTopLeft().pointY - rec->getLength()) {
        closestPoint->pointY = rec->getTopLeft().pointY - rec->getLength();
    } else {
        closestPoint->pointY = circle->getCenter().pointY;
    }

    if (closestPoint->distance(circle->getCenter()) < circle->getRadius()) {
        return true;
    }

    return false;
}



int main() {
    Rectangle a(4, 3, 4.9, 3.4, "white", true);
    Circle b(3, 3, 4, "white", false);

    cout << checkRectangleColideCircle(&a, &b);
}
