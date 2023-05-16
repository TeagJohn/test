#include <iostream>  
using namespace std;  
  
template < class T >  
T sum(T a[],int sz)  
{  
    int i;  
    T ans=0;  
    for(i=0;i<sz;i++)  
        ans += a[i];  
    return ans;  
}  