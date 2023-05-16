#include <iostream>

class School
{
public:
	int id = 10;
	int getId();
};

int School::getId()  {return id;}

class Student
{
public:
	Student() {age = 10;}
	Student(int x) {age = x;}
	int age;
	School school;
	int getAge();
	School getSchool();
	
};

int Student::getAge() {return age;}
School Student::getSchool() {return school;} 


int main()
{
	Student std(34);
	std.getSchool().getId();
	std.getAge();
	return 0;
}