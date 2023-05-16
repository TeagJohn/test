// http://www.cplusplus.com/reference/string/wstring/
#include <string>
using namespace std;

int wstringBasic(wstring x){return 0;}

int wstringRef(wstring& x){return 0;}

int wstringPointer0(wstring* x){return 0;}
int wstringPointer1(wstring** x){return 0;}
int wstringPointer2(wstring*** x){return 0;}

int wstringArray0(wstring x[]){return 0;}
int wstringArray1(wstring x[3]){return 0;}
int wstringArray2(wstring x[2][3]){return 0;}
int wstringArray3(wstring x[][3]){return 0;}