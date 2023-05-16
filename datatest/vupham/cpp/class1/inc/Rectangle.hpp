#include "Shape.hpp"

class Point {
public:
    int x, y;
};

class Rectangle: public Shape {
    

public:
    int width;
    int height;
    int* arr;
    Rectangle(int, int);
    Rectangle(int, int, int, int);
    int getWidth();
    int getHeight();
    void setWidth(int);
    void setHeight(int);

    Rectangle* clone() override;

    ~Rectangle();
private:

    bool isEqual1(const Rectangle&);

    bool isEqual2(Rectangle);
public:
    void getType() override;

    std::string getTypeString() override;

    friend bool test2(Rectangle);

    bool isCollidingWith(Shape&) override;

    bool testPointer(Point other) {
        Point* point = &other;
        if (point->x == this->x && point->y == this->y) {
            return true;
        }

        return false;
    }
};
