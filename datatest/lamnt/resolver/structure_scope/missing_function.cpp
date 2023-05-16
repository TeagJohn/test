class Person
{
public:
	int age;	
};

int UUT(int x)
{
	Person p;
	float bmi = p.getBmi();
	return p.age;
}
