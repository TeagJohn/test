#include "animal.cpp"
#include "swimable.cpp"
#include "flyable.cpp"

class Dog : public Animal, public Swimable, public Flyable
{
private:
    int type;
public:
    Dog(char n, int y, int t) : Animal(n,y){
        type = t;
    };

    void swim() override {
        std::cout<<"Dog: swim"<<std::endl;
    };
    void virtualFn() override {
        std::cout<<"Dog: virtual fn"<<std::endl;
    };

    void fly() {
        std::cout<<"Dog:: fly"<<std::endl;
    }
};

int main()
{
    Flyable *d = new Dog('c',1,2);
    d->fly();
    d->virtualFn();
    return 0;
}
