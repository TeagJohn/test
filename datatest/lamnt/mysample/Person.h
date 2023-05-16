class Person
{
public:
	int weight;
	int getWeight();
	int getDoubleWeight();

private:

	int foo();

protected:
	int foooo() {return 0;}
};


int Person::foo() {
	return 0;
}