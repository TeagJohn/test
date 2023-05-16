#include "classMethodInDiffFile.cpp"
#include <iostream>

int Person::getWeight()
{
	return weight;
}

int getSomeWeight(Person* p)
{
	return (*p).getWeight() + p->getDoubleWeight();
}

Person test2(int x);

namespace A {
	void foo(int x) {}
}

int test(int stuff)
{
	Person* p;
	p->weight = 5;
	A::foo(34);
	std::cout << getSomeWeight(p);
	return 0;
}