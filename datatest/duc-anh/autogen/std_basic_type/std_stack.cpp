#include <stack> // <stack>
#include <string>
#include "class.h"

int intArgument(std::stack<int> v) {
    return v.size();
}

int charArgument(std::stack<char> v) {
    return v.size();
}

int stringArgument(std::stack<std::string> v) {
    return v.size();
}

int structureArgument(std::stack<MyClass> v) {
    return v.size();
}

int stltypeArgument(std::stack<std::stack<char> > v) {
    return v.size();
}

int MyClassTemplateArgument(std::stack<Array<int> > v) {
    return v.size();
}

int oneLevelBasicArgument(std::stack<char*> v) {
    return v.size();
}


int twoLevelBasicArgument(std::stack<char**> v) {
    return v.size();
}


int oneLevelStructureArgument(std::stack<MyClass*> v) {
    return v.size();
}


int twoLevelStructureArgument(std::stack<MyClass**> v) {
    return v.size();
}

int oneLevelBasic(std::stack<char>* v) {
    return v[0].size();
}

int twoLevelBasic(std::stack<char>** v) {
    return v[0][0].size();
}

int oneDimBasic(std::stack<char> v[]) {
    return v[0].size();
}

int twoDimBasic(std::stack<char> v[][4]) {
    return v[0][0].size();
}

int oneLevelStructure(std::stack<MyClass>* v) {
    return v[0].size();
}

int twoLevelStructure(std::stack<MyClass>** v) {
    return v[0][0].size();
}

int oneDimStructure(std::stack<MyClass> v[]) {
    return v[0].size();
}

int twoDimStructure(std::stack<MyClass> v[][4]) {
    return v[0][0].size();
}

int twoLevelOneLevel(std::stack<int*>** v) {
    return v[0][0].size();
}


int twoDimOneLevel(std::stack<int*> v[][4]) {
    return v[0][0].size();
}