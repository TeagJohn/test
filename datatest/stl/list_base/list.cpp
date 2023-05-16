#include <list> // <list>
#include <string>
#include "class.h"

int intArgument(std::list<int> v) {
    return v.size();
}

int charArgument(std::list<char> v) {
    return v.size();
}

int stringArgument(std::list<std::string> v) {
    return v.size();
}

int structureArgument(std::list<Class> v) {
    return v.size();
}

int stltypeArgument(std::list<std::list<char> > v) {
    return v.size();
}

int classTemplateArgument(std::list<Array<int> > v) {
    return v.size();
}

int oneLevelBasicArgument(std::list<char*> v) {
    return v.size();
}


int twoLevelBasicArgument(std::list<char**> v) {
    return v.size();
}


int oneLevelStructureArgument(std::list<Class*> v) {
    return v.size();
}


int twoLevelStructureArgument(std::list<Class**> v) {
    return v.size();
}

int oneLevelBasic(std::list<char>* v) {
    return v[0].size();
}

int twoLevelBasic(std::list<char>** v) {
    return v[0][0].size();
}

int oneDimBasic(std::list<char> v[]) {
    return v[0].size();
}

int twoDimBasic(std::list<char> v[][4]) {
    return v[0][0].size();
}

int oneLevelStructure(std::list<Class>* v) {
    return v[0].size();
}

int twoLevelStructure(std::list<Class>** v) {
    return v[0][0].size();
}

int oneDimStructure(std::list<Class> v[]) {
    return v[0].size();
}

int twoDimStructure(std::list<Class> v[][4]) {
    return v[0][0].size();
}

int twoLevelOneLevel(std::list<int*>** v) {
    return v[0][0].size();
}


int twoDimOneLevel(std::list<int*> v[][4]) {
    return v[0][0].size();
}