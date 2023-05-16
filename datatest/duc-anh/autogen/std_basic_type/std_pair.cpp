#include <utility> 
#include "class.h"

using namespace std;

int testPair0(pair<int, float> p) {return true;}

int testPair1(std::pair<int, float>* p) {return true;}
int testPair2(std::pair<int, float>** p) {return true;}

int testPair3(std::pair<int, float> p[3]) {return true;}
int testPair4(std::pair<int, float> p[]) {return true;}
int testPair5(std::pair<int, float> p[][2]) {return true;}
int testPair6(std::pair<int, float> p[4][2]) {return true;}

int testPair7(std::pair<MyClass, int> p) {return true;}
int testPair8(std::pair<MyClass*, int> p) {return true;}
int testPair9(std::pair<MyClass[2][4], int> p) {return true;}
int testPair10(std::pair<MyClass[3], int> p) {return true;}
int testPair11(std::pair<MyClass[3], int*> p) {return true;}

int testPair12(std::pair<int, std::pair<int, char> > p) {return 0;}
int testPair13(std::pair<int, std::pair<int, char>* > p) {return 0;}
int testPair14(std::pair<std::pair<int, char>, std::pair<int, char> > p) {return 0;}
int testPair15(std::pair<Array<int>, int> p) {return 0;}