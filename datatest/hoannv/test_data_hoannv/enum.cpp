#include <iostream>

using namespace std;

enum State
{
   Uninitialized,
   Initialization,
   Active,
   Idle
};
enum Fruits
{
	Banana,
	Mango,
	Aple,
	Grape,
	StrawBerry
};

int function1(State state, Fruits fruit, int x);

int main(int argc, char* args[]) {

	function1(Active, Mango, 10);

	return 0;
}

int function1(State state, Fruits fruit, int x) {
	cout << state << endl;
	return x;
}
