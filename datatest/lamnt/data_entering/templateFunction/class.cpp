#include<iostream> 
using namespace std; 
  
template<typename T> 
class A  { 
public: 
	T x; 
    A() {    cout<<"Constructor Called"<<endl;   } 
    T getX(int vad) {return x;}
}; 

int test(A<float> a) {
	return a.getX(4);
}
  
int main()  { 
   A<char> a; 
   A<int> b; 
   return 0; 
} 