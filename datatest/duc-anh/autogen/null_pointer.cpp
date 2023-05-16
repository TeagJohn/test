#include <cstddef>
#include <iostream>

using namespace std;
int nullPointerBasic(nullptr_t p){ return 0;}
int declNullPointerBasic(decltype(nullptr) nullp){ return 0;}