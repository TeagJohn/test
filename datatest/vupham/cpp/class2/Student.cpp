#include "Student.hpp"

Student Student::whoHasHigherGPA(Student other) {
    for (int i = 0; i < 5; ++i) {
        if ((Person) other == friends[i]) return other;
    }

    if (this->schoolCode == other.schoolCode) {
        if (this->classCode == other.classCode) {
            return ((this->gpa > other.gpa) ? *this : other);
        }
    } else {
        return ((this->gpa > other.gpa && this->classCode < other.classCode) ? *this : other);
    }
}