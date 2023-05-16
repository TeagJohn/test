#include "Monitor.h"
#include <iostream>

Monitor::Monitor() {

}

Monitor::Monitor(int id, string firstName, string lastName, int age, int year) 
        : Student(id, firstName, lastName, age, year) {

}

Monitor::~Monitor() {
    for (auto i = studentList.begin(); i != studentList.end(); ++i) {
        delete *i;
    }

    studentList.clear();
}

bool Monitor::addStudent(set<Student*> list) {
    studentList = list;
}

bool Monitor::insertStudent(Student* student) {
    studentList.insert(student);
    return true;
}

bool Monitor::insertStudent(Student student) {
    studentList.insert(&student);
    return true;
}

bool Monitor::removeStudent(Student* student) {
    auto it = studentList.find(student);
    if (it == studentList.end()) {
        cout << "Not found";
        return false;
    }

    studentList.erase(it);
    cout << "Delete success";
    return true;
} 

bool Monitor::removeStudent(Student student) {
    return removeStudent(&student);
}

int Monitor::getManageListSize() {
    return studentList.size();
}

bool Monitor::getStudentList() {
    if (getManageListSize() == 0) {
        return false;
    }

    for (auto i = studentList.begin(); i != studentList.end(); ++i) {
        cout << (*i)->getFirstName() << " " << (*i)->getLastName() << endl;
    }

    return true;
}

Student* Monitor::getStudentWithId(unsigned int id) {
    for (auto i = studentList.begin(); i != studentList.end(); ++i) {
        if ((*i)->getId() == id) {
            return *i;
        }
    }
}

Student* Monitor::getStudentWithNumber(unsigned int number) {
    if (number > getManageListSize()) {
        return nullptr;
    }

    int index = 0;
    for (auto i = studentList.begin(); i != studentList.end(); ++i) {
        ++index;
        if (index == number) {
            return *i;
        }
    }
}
