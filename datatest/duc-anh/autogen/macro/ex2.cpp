#include <stdio.h>
//#include <iostream>
//using namespace std;

//
#define function(x) 1

//
#define my_function(x) \
  do { \
    int __err = function(x); \
    switch(__err) { \
      case 1: \
        fprintf(stderr, "Error!\n"); \
        break; \
    } \
    __err; \
  } while(0)

// Object-like Macros
#define NUMBERS 1, \
                2, \
                3

//
#define FOO(A) ({int retval; retval;})

// Object-like Macros
#define BUFSIZE 1020
#define TABLESIZE BUFSIZE
#undef BUFSIZE
#define BUFSIZE 37


// Misnesting: macro Inside a macro (https://gcc.gnu.org/onlinedocs/cpp/Misnesting.html#Misnesting)
#define twice(x) (2*(x))
#define call_with_1(x) x(1)

//Operator Precedence Problems (https://gcc.gnu.org/onlinedocs/cpp/Operator-Precedence-Problems.html#Operator-Precedence-Problems)
#define ceil_div(x, y) (x + y - 1) / y

// Swallowing the Semicolon (https://gcc.gnu.org/onlinedocs/cpp/Swallowing-the-Semicolon.html#Swallowing-the-Semicolon)
#define SKIP_SPACES(p, limit)  \
{ char *lim = (limit);         \
  while (p < lim) {            \
    if (*p++ != ' ') {         \
      p--; break; }}}


// Swallowing the Semicolon
#define SKIP_SPACES2(p, limit)     \
do { char *lim = (limit);         \
     while (p < lim) {            \
       if (*p++ != ' ') {         \
         p--; break; }}}          \
while (0)


//
#define min(X, Y)  ((X) < (Y) ? (X) : (Y))

int main(){
    int bb = min(1,2);
    //
    char * p;
    char* xxx;
    if (*p != 0)
        SKIP_SPACES (p, xxx);

    if (*p != 0)
        SKIP_SPACES2 (p, xxx);

    //
    int a = ceil_div (1 & 0, sizeof (int));

    //
    call_with_1(twice);

    //
    int x[] = { NUMBERS };

    //
    my_function(1);

    //
    //std::cout << FOO(1);
   // std::cout << 1;
    return 0;
}
