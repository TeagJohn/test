#include <iostream>

class Student
{
public:
    int age;
};

int calledFunc(Student std) 
{
    return std.age * 2;
}

// int calledFunc(bool x)
// {
//     return x ? 1 : 0;
// }

int calledFunc(int x) 
{
    return x * 2;
}

// int calledFunc(float x)
// {
//     return x + 1;
// }

int calledFunc(int* x) 
{
    return *x;
}

int calledFunc(int arr[][1])
{
    return arr[0][0];
}

int call2Dim()
{
    int a[1][1] = {1};
    return calledFunc(a);
}

int callClass() 
{
    Student std;
    std.age = 4;
    return calledFunc(std);
}

int callInt() 
{
    return calledFunc(15);
}

int callFloat()
{
    return calledFunc(14.3f);
}

int callBool()
{
    return calledFunc(true);
}

int callPointer()
{
    int x = 32;
    int* p = &x;
    return calledFunc(p);
}

int main()
{
    std::cout << call2Dim();
    return 0;
}
