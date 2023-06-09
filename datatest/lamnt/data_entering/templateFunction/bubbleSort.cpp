// CPP code for bubble sort 
// using template function 
#include <iostream> 
using namespace std; 
   
// A template function to implement bubble sort. 
// We can use this for any data type that supports 
// comparison operator < and swap works for it. 
template <class T> 
bool bubbleSort(T a, int n) { 


    return true;
} 
   
// Driver Code 
int main() { 
    int a[5] = {10, 50, 30, 40, 20}; 
    int n = sizeof(a) / sizeof(a[0]); 
   
    // calls template function  
    bubbleSort(a, 5); 
   
    cout << " Sorted array : "; 
    for (int i = 0; i < n; i++) 
        cout << a[i] << " "; 
    cout << endl; 
   
  return 0; 
} 