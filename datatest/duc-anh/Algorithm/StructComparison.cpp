// Modified from https://www.tutorialspoint.com/strcmp-in-c-cplusplus

#include <iostream>
#include<cstdlib>
#include<string.h>

using namespace std;

struct Person
{
    char name[50];
    int age;
    float salary;
};

// Compare two people in terms of name, age, and their salary
// Return true if the two people are identical, false otherwise
int compareTwoPeople(Person a, Person b){
    if (strcmp(a.name, b.name)==0)
        if (a.age==b.age)
            if (a.salary==b.salary)
                return 1;
	return 0;
}

// Compare the two list
// Return true if the two lists are identical, false otherwise
// size_a: the number of people in group 1
// size_b: the number of people in group 2
int compareGroup(Person* a, Person* b, int size_a, int size_b){
    if (size_a != size_b)
        return 0;
    else{
    for (int i = 0; i < size_a; i++)
        if (strcmp(a[i].name, b[i].name) !=0 || a[i].age != b[i].age || a[i].salary != b[i].salary)
            return 0;
	return 1;
	}
}

// Compare the two list
// Return true if the two lists are identical, false otherwise
// size_a: the number of people in group 1
// size_b: the number of people in group 2
int compareGroup(Person a[3], Person b[3]){
    for (int i = 0; i < 3; i++)
        if (strcmp(a[i].name, b[i].name) !=0 || a[i].age != b[i].age || a[i].salary != b[i].salary)
            return 0;
	return 1;
}

// Compare the two list
// Return true if the two lists are identical, false otherwise
// size_a: the number of people in group 1
// size_b: the number of people in group 2
int compareGroupv2(Person a[], Person b[], int size_a, int size_b){
    if (size_a != size_b)
        return 0;
    else{
    for (int i = 0; i < size_a; i++)
        if (strcmp(a[i].name, b[i].name) !=0 || a[i].age != b[i].age || a[i].salary != b[i].salary)
            return 0;
	return 1;
	}
}