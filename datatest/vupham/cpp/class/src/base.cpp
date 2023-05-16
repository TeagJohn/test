#include "../inc/Base.hpp"

Base::Base(int i)
{
    this->i = i;
}

int Base::getI()
{
    return this->i;
}

void Base::doSt(){
    cout<<"1"<<endl;
}