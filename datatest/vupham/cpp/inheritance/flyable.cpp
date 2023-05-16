#include <iostream>

class Flyable
{
public:
    virtual void virtualFn(){
        std::cout<<"virtual fn"<<std::endl;
    }

    void fly(){
        std::cout<<"fly"<<std::endl;
    }
};