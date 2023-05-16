#ifndef _PERSON_HPP_
#define _PERSON_HPP_

#include <string>

class Date {
public:
    uint8_t day;
    uint8_t month;
    uint8_t year;

    Date() {};
    Date(uint8_t day, uint8_t month, uint8_t year) {
        this->day = day;
        this->month = month;
        this->year = year;
    }

    bool operator == (Date other) {
        return other.day == this->day && other.month == this->month && other.year == this->year;
    }
};

class Person {
public:
    Person() {};
    Person(std::string name, uint8_t age, Date dob = Date(1, 1, 1970), std::string address = "") {
        this->name = name;
        this->age = age;
        this->dob = dob;
        this->address = address;
    }

    Person* clone(Person p) {
        return new Person(p.name, p.age, p.dob, p.address);
    } 

    bool operator == (Person other) {
        if (other.name != this->name) return false;
        if (other.age != this->age) return false;
        if (!(other.dob == this->dob)) return false;
        if (other.address != this->address) return false;
        return true;
    }

protected:
    std::string name;
    uint8_t age;
    Date dob;
    std::string address;
};

#endif