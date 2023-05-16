#include <iostream>

using namespace std;

class Base
{
public:
    int baseAttr;

    Base(int baseAttr)
    {
        this->baseAttr = baseAttr;
    }

    void baseMethod()
    {
        cout << "Base" << endl;
    }

    virtual void virtualMethod()
    {
        cout << "Base: virtualMethod" << endl;
    }
};

class MyAbstractClass
{
public:
    int x;
    
    MyAbstractClass(){
	cout<<"Default constructor"<<endl;
    }

    MyAbstractClass(int x){
	    this->x= x;
    }
    virtual void myAbstractClassMethod()=0;

    void testAbstractClass(){
	cout<<this->x<<endl;
        cout<<"Abstract class method"<<endl;
    }
};

class Derived : public Base, public MyAbstractClass
{
public:
    Derived(int i, int x) : Base(i), MyAbstractClass(x)
    {
        cout<<"Derived constructor"<<endl;
    }

    void baseMethod()
    {
        cout << "Derived: Base" << endl;
    }

    void virtualMethod() override {
        cout << "Derived: virtualMethod"<<endl;
    }

    void myAbstractClassMethod() override
    {
        cout << "Derived: MyAbstractClass" << endl;
    }

};

void test1(){
    Derived *d = new Derived(1,3);
    d->baseMethod();
    d->virtualMethod();
    d->myAbstractClassMethod();
}

void test2(){
    Derived *d = new Derived(1,3);
    Base *b = d;
    b->baseMethod();
    b->virtualMethod();
    MyAbstractClass *a = d;
    a->myAbstractClassMethod();

}

