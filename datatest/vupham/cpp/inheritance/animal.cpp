#include <iostream>

using namespace std;

class Animal
{
private:
    char name;
    int y;

public:
    Animal(){

    };

    Animal(char n)
    {
        this->name = n;
    }

    Animal(char n, int y)
    {
        name = n;
        this->y = y;
    }

    void setName(char n)
    {
        name = n;
    }

    char getName()
    {
        return name;
    }

    void setY(int y)
    {
        this->y = y;
    }

    int getY()
    {
        return y;
    }

    ~Animal() {}
};