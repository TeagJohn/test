#pragma once

#ifndef _MONITOR_H_
#define _MONITOR_H_

#include <vector>
#include <set>
#include "Student.h"

class Monitor : public Student {
    private:
        set<Student*> studentList;
    public:
        Monitor();
        Monitor(int id, string firstName, string lastName, int age, int year);
        ~Monitor();

        bool addStudent(set<Student*> list);
        bool insertStudent(Student* student);
        bool insertStudent(Student student);
        bool removeStudent(Student* student);
        bool removeStudent(Student student);

        int getManageListSize();
        bool getStudentList();
        Student* getStudentWithId(unsigned int id);
        Student* getStudentWithNumber(unsigned int number);
};

#endif