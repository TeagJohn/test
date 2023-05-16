#include "base.hpp"

class Derived : public Base
{
public:
    int d;
    Derived(int,int);

    void doSt() override;
};