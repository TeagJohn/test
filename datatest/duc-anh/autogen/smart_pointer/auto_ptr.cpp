// auto_ptr example
#include <iostream>
#include <memory>

class MY_AUTO_PTR{
    private:
        int a;
    public:
        MY_AUTO_PTR(){}
        MY_AUTO_PTR(int x){a = x;};
};

int test0_0(std::auto_ptr<int> p1) {return 0;}
int test0_1(std::auto_ptr<char> p1) {return 0;}
int test0_2(std::auto_ptr<MY_AUTO_PTR> p1) {return 0;}

int test1_0(std::auto_ptr<int*> p1) {return 0;}
int test1_1(std::auto_ptr<char*> p1) {return 0;}
int test1_2(std::auto_ptr<MY_AUTO_PTR*> p1) {return 0;}

int test2_0(std::auto_ptr<int**> p1) {return 0;}
int test2_1(std::auto_ptr<char**> p1) {return 0;}
int test2_2(std::auto_ptr<MY_AUTO_PTR**> p1) {return 0;}