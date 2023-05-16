#include "../Person.h"

int Person::getDoubleWeight()
{
	return weight * 2;
}

int test(Person p)
{
	return p.getDoubleWeight();
}

int main()
{
	Person p;
	p.weight = 3;
	return test(p);
}