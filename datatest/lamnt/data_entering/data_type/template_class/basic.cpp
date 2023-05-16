#include "Pair.h"
#include <string>
using namespace std;

template <typename T, typename V>
class PXSair {
public:
    T t;
    V v;
    int getT(V _v) {
        return 0;
    }
private:
    int foo() {
        return 0;
    }
};

PXSair<int, float> pxy;

int basic(Pair<int, float> p) {
    return 0;
}

int basic(Pair<char, string> p) {
    return 0;
}

int oneDim(Pair<char, float> p[]) {
    return 0;
}

int oneDim(Pair<char, string> p[]) {
    return 0;
}

int twoDim(Pair<int, float> p[][3]) {
    return 0;
}

int twoDim(Pair<char, string> p[][3]) {
    return 0;
}

int oneLevel(Pair<int, float>* p) {
    return 0;
}

int twoLevel(Pair<int, float>** p) {
    return 0;
} 

int oneLevel(Pair<char, string>* p) {
    return 0;
}

int twoLevel(Pair<char, string>** p) {
    return 0;
} 