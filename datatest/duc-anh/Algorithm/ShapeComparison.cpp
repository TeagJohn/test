// Modified from: http://www.cplusplus.com/doc/tutorial/inheritance/
// Compare shapes
#include <iostream>

using namespace std;

class Polygon {
protected:
    int width, height;

public:
    Polygon() {}
    int getWidth()
    {
        return width;
    }
    int getHeight()
    {
        return height;
    }
    virtual int area(){}; // abstract class
};

class Rectangle : public Polygon {
public:
    Rectangle(){}
    Rectangle(int a, int b)
    {
        width = a;
        height = b;
    }
    int area()
    {
        return width * height;
    }
};

class Triangle : public Polygon {
public:
    Triangle(){}
    Triangle(int a, int b)
    {
        width = a;
        height = b;
    }
    int area()
    {
        return width * height / 2;
    }
};

// compare two shapes
// return true if the two shapes are identical, false if otherwise
int compare(Polygon r1, Polygon r2)
{
    if (r1.getWidth() == r2.getWidth())
        if (r1.getHeight() == r2.getHeight())
            return 1;
    return 0;
}

int compare(Polygon* r1, Polygon* r2, int size_r1, int size_r2)
{
    if (size_r1 != size_r2)
        return 0;
    else{
        for (int i = 0; i < size_r1; i++)
            if (r1[i].getWidth() != r2[i].getWidth() && r1[i].getHeight() != r2[i].getHeight())
                    return 0;
        return 1;
    }
}

int compare(Polygon r1[3], Polygon r2[3])
{
    for (int i = 0; i < 3; i++)
        if (r1[i].getWidth() != r2[i].getWidth() && r1[i].getHeight() != r2[i].getHeight())
                return 0;
    return 1;
}

int comparev2(Polygon r1[], Polygon r2[], int size_r1, int size_r2)
{
    if (size_r1 != size_r2)
        return 0;
    else{
        for (int i = 0; i < size_r1; i++)
            if (r1[i].getWidth() != r2[i].getWidth() && r1[i].getHeight() != r2[i].getHeight())
                    return 0;
        return 1;
    }
}