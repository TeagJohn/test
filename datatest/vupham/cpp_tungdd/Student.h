#pragma once
#ifndef _STUDENT_H_
#define _STUDENT_H_

#include <string>

using namespace std;

class Student {
    protected: 
        unsigned int id;
        string firstName = "";
        string lastName = "";
        int age = 0;
        int year = 0;
        float avgGrade = 0.0;
    public:
        Student();
        Student(unsigned int id, string firstName, string lastName, int age, int year);
        ~Student();

        void setId(unsigned int id);
        void setFirstName(string firstName);
        void setLastName(string lastName);
        void setAge(int age);
        void setYear(int year);
        void setAvgGrade(float grade);

        string getFirstName();
        string getLastName();
        int getId();
        int getAge();
        int getYear();
        float getAvgGrade();
        int compareStudent(Student* other);
        bool checkStudentInfo();
};

#endif