#include <iostream>

using namespace std;

class Base {
public:
    int i;

    Base(int);

    int getI();

    virtual void doSt();
};