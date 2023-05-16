// http://www.cplusplus.com/reference/string/string/
#include <string>
using namespace std;

int stringBasic(string x){return 0;}

int stringRef(string& x){return 0;}

int stringPointer0(string* x){return 0;}
int stringPointer1(string** x){return 0;}
int stringPointer2(string*** x){return 0;}

int stringArray0(string x[]){return 0;}
int stringArray1(string x[3]){return 0;}
int stringArray2(string x[2][3]){return 0;}
int stringArray3(string x[][3]){return 0;}