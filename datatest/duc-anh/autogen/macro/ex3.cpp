#include <stdio.h>
#include <iostream>
using namespace std;

#define MAX 100

// macro function without '{' and '}'
// Swallowing the Semicolon (https://gcc.gnu.org/onlinedocs/cpp/Swallowing-the-Semicolon.html#Swallowing-the-Semicolon)
#define SKIP_SPACES(p, limit)  \
{   \
char *lim = (limit);         \
  while (p < lim) {            \
    if (*p++ != ' ') {         \
      p--; break; }}  \
}

// macro call other macro
#define TEST2(p, limit) \
SKIP_SPACES(p, limit); \
if (p != 0){a = 0;} \
            else a = 1;

// macro call a function
int foo1() {return MAX;}
int foo(){ return foo1();}
#define TEST3(p) \
foo()

// macro function with '{' and '}'
#define TEST4(p) \
return 0;

// macro function in a line
#define TEST5(p) return 0;

int test1(char * p, char* xxx){
    int a;
    a = MAX;
    SKIP_SPACES(p, xxx);
    TEST2(p, xxx);
    int d = TEST3(p);
    TEST4(p);
}

//int main(){
//    return 0;
//}
