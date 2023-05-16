#ifndef _STUDENT_HPP_
#define _STUDENT_HPP_

#include "Person.hpp"

class Student : public Person {
public: 
    Student(std::string name, uint8_t age, Date dob, std::string address, 
        std::string schoolCode, uint8_t classCode, float gpa) 
    : Person(name, age, dob, address) {
        this->schoolCode = schoolCode;
        this->classCode = classCode;
        this->gpa = gpa;
    }

    Student whoHasHigherGPA(Student other);

private: 
    std::string schoolCode;
    uint8_t classCode;
    float gpa;
    Person friends[5];
};

#endif