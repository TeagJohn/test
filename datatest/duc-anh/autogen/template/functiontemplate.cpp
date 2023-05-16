#include <iostream>
using namespace std;
#include <queue> // <queue>
#include <string>
#include <list>
#include <set>

template <typename T>
int template1(T x, T y){
    return true;
}

template <typename T, typename V>
int template2(T x, V y){
    return true;
}

namespace XXX{
    template <typename T>
    int template1(T x, T y){
        return true;
    }

    namespace YYY{
        template <typename T>
        int template2(T x, T y){
            return true;
        }    template <typename T>
             int template1(T x, T y){
                 return true;
             }


    }
}

int main(){
    // function template
    template1<int>(3, 7);
    template1<double>(3.0, 7.0);
    template1<char>('g', 'e');

    template2<int, int>(3, 3);
    template2<int, float>(3, 3.0);

    int * a = new int[4];
    template2<int[], float>(a, 3.0);

    return 0;
}
