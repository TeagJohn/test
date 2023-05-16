#include "../inc/Derived.hpp"

Derived::Derived(int i, int d):Base(i){
    this->d = d;
}

void Derived::doSt() {
    cout<<"Overrided"<<endl;

}

int main()
{
    Base* b = new Derived(1,3);
    b->doSt();
    return 0;
}
