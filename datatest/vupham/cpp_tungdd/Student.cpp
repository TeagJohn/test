#include "Student.h"

//#ifdef _STUDENT_H_
string trim(string t);
//#endif

Student::Student() {
    id = 0; 
    firstName = "";
    lastName = "";
    age = 0;
    year = 0;
}

Student::Student(unsigned int id, string firstName, string lastName, int age, int year) {
    this->id = id;
    this->firstName = firstName;
    this->lastName = lastName;
    this->age = age;
    this->year = year;
}

Student::~Student() {
}

void Student::setId(unsigned int id) {
    this->id = id;
}

void Student::setFirstName(string firstName) {
    this->firstName = firstName;
}

void Student::setLastName(string lastName) {
    this->lastName = lastName;
}

void Student::setAge(int age) {
    this->age = age;
}

void Student::setYear(int year) {
    this->year = year;
} 

void Student::setAvgGrade(float grade) {
    avgGrade = grade;
}

string Student::getFirstName() {
    return firstName;
}

string Student::getLastName() {
    return lastName;
}

int Student::getId() {
    return id;
}

int Student::getAge() {
    return age;
}

int Student::getYear() {
    return year;
}

float Student::getAvgGrade() {
    return avgGrade;
}

bool Student::checkStudentInfo() {
    firstName = trim(firstName);
    lastName = trim(lastName);
    
    if (firstName.length() == 0) {
        return false;
    } 

    for (int i = 0; i < firstName.length(); ++i) {
        if (!((firstName[i] >= 'a' && firstName[i] <= 'z') 
            || (firstName[i] >= 'A' && firstName[i] <= 'Z'))) {
            return false;
        }
    }

    for (int i = 0; i < lastName.length(); ++i) {
        if (!((lastName[i] >= 'a' && lastName[i] <= 'z') 
            || (lastName[i] >= 'A' && lastName[i] <= 'Z'))) {
            return false;
        }
    }
    
    if (age < 0) {
        return false;
    }

    if (year < 1 || year > 4) {
        return false;
    }

    return true;
}

int Student::compareStudent(Student* other) {
    if (this->year > other->year) return 1;
    if (this->year < other->year) return -1;
    
    if (this->age > other->age) return 1;
    if (this->age < other->age) return -1;

    return 0;
}

string trim(string t) {
    string trimStr = "";
    for (int i = 0; i < t.length(); ++i) {
        if (t[i] == ' ') continue;
        else {
            for (int j = i; j < t.length(); ++j) {
                trimStr += t[j];
            }

            break;
        }
    }

    t = trimStr;
    trimStr = "";
    for (int i = t.length() - 1; i >= 0; --i) {
        if (t[i] == ' ') continue;
        else {
            for (int j = 0; j <= i; ++j) {
                trimStr += t[j];
            }

            break;
        }
    }

    return trimStr;
}
