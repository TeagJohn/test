#include <vector> // <vector>
#include <string>
#include "class.h"

int intArgument(std::vector<int> v) {
    return v.size();
}

int charArgument(std::vector<char> v) {
    return v.size();
}

int stringArgument(std::vector<std::string> v) {
    return v.size();
}

int structureArgument(std::vector<Class> v) {
    return v.size();
}

int stltypeArgument(std::vector<std::vector<char> > v) {
    return v.size();
}

int classTemplateArgument(std::vector<Array<int> > v) {
    return v.size();
}

int oneLevelBasicArgument(std::vector<char*> v) {
    return v.size();
}


int twoLevelBasicArgument(std::vector<char**> v) {
    return v.size();
}


int oneLevelStructureArgument(std::vector<Class*> v) {
    return v.size();
}


int twoLevelStructureArgument(std::vector<Class**> v) {
    return v.size();
}

int oneLevelBasic(std::vector<char>* v) {
    return v[0].size();
}

int twoLevelBasic(std::vector<char>** v) {
    return v[0][0].size();
}

int oneDimBasic(std::vector<char> v[]) {
    return v[0].size();
}

int twoDimBasic(std::vector<char> v[][4]) {
    return v[0][0].size();
}

int oneLevelStructure(std::vector<Class>* v) {
    return v[0].size();
}

int twoLevelStructure(std::vector<Class>** v) {
    return v[0][0].size();
}

int oneDimStructure(std::vector<Class> v[]) {
    return v[0].size();
}

int twoDimStructure(std::vector<Class> v[][4]) {
    return v[0][0].size();
}

int twoLevelOneLevel(std::vector<int*>** v) {
    return v[0][0].size();
}


int twoDimOneLevel(std::vector<int*> v[][4]) {
    return v[0][0].size();
}