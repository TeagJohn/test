#include <utility>
#include "class.h"
#include <map>
using namespace std;

int  testMap0(std::map<int, float> p) {return true;}

int testMap1(std::map<int, float>* p) {return true;}
int testMap2(std::map<int, float>** p) {return true;}

int testMap3(std::map<int, float> p[3]) {return true;}
int testMap4(std::map<int, float> p[]) {return true;}
int testMap5(std::map<int, float> p[][2]) {return true;}
int testMap6(std::map<int, float> p[4][2]) {return true;}

int testMap7(std::map<MyClass, int> p) {return true;}
int testMap8(std::map<MyClass*, int> p) {return true;}
int testMap8_1(std::map<MyClass**, int> p) {return true;}
int testMap9(std::map<MyClass[2][4], int> p) {return true;}
int testMap10(std::map<MyClass[3], int> p) {return true;}
int testMap11(std::map<MyClass[3], int*> p) {return true;}

int testMap12(std::map<int, std::map<int, char> > p) {return 0;}
int testMap13(std::map<int, std::map<int, char>* > p) {return 0;}
int testMap14(std::map<std::map<int, char>, std::map<int, char> > p) {return 0;}
int testMap15(std::map<Array<int>, int> p) {return 0;}

int testMap16(std::map<std::string, int> p) {return true;}
