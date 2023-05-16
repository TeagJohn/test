#include <iostream>
#include <memory>
#include <thread>
#include <chrono>
#include <mutex>

class MY_SHARED_PTR{
    private:
        int a;
    public:
        MY_SHARED_PTR(){}
        MY_SHARED_PTR(int x){a = x;};
};

int test0_0(std::shared_ptr<int> p){return 0;}
int test0_1(std::shared_ptr<char> p){return 0;}
int test0_2(std::shared_ptr<MY_SHARED_PTR> p){return 0;}

int test1_0(std::shared_ptr<int*> p){return 0;}
int test1_1(std::shared_ptr<char*> p){return 0;}
int test1_2(std::shared_ptr<MY_SHARED_PTR*> p){return 0;}

int test2_0(std::shared_ptr<int**> p){return 0;}
int test2_1(std::shared_ptr<char**> p){return 0;}
int test2_2(std::shared_ptr<MY_SHARED_PTR**> p){return 0;}