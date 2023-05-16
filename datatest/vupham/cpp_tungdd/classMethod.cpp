#include <iostream>
#include "Student.h"

bool check() {
    Student* t = new Student(20020070, "Tung", "Doan", 15, 4);
    return t->checkStudentInfo();
}

bool check(Student *t) {
    return t->checkStudentInfo();
}