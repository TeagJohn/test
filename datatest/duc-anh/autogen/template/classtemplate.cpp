#include <iostream>
using namespace std;

//template <typename T>
//class A {
//public:
//    T x;
//    A() { cout << "Constructor Called" << endl; }
//    T getX(int vad) { return x; }
//};

template <class T>
class mypair {
    T a, b;
  public:
    mypair (T first, T second)
      {a=first; b=second;}

    T getmax ();

    int getmin(T a, T b){
        return 0;
    }
};

template <class T>
T mypair<T>::getmax ()
{
  T retval;
  retval = a>b? a : b;
  return retval;
}

int main(){
    mypair<int> myobject (115, 36); // fail
    mypair<double> myfloats (3.0, 2.18);

    return 0;
}
