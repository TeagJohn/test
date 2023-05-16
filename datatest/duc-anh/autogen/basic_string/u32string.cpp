// http://www.cplusplus.com/reference/string/u32string/
#include <string>
using namespace std;

int u32stringBasic(u32string x){return 0;}

int u32stringRef(u32string& x){return 0;}

int u32stringPointer0(u32string* x){return 0;}
int u32stringPointer1(u32string** x){return 0;}
int u32stringPointer2(u32string*** x){return 0;}

int u32stringArray0(u32string x[]){return 0;}
int u32stringArray1(u32string x[3]){return 0;}
int u32stringArray2(u32string x[2][3]){return 0;}
int u32stringArray3(u32string x[][3]){return 0;}