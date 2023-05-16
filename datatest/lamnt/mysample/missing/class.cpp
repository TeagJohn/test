#include "../Person.h"

namespace A {
	namespace B {
		int test(int x) {
			return 0;
		}
	}
}

int main(int argc, char const *argv[])
{
	Person p;
	A::B::test(45);
	return 0;
}