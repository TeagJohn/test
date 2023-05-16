#include <queue> // <queue>
#include <string>
#include "class.h"

int intArgument(std::queue<int> v) {
    return v.size();
}

int charArgument(std::queue<char> v) {
    return v.size();
}

int stringArgument(std::queue<std::string> v) {
    return v.size();
}

int structureArgument(std::queue<Class> v) {
    return v.size();
}

int stltypeArgument(std::queue<std::queue<char> > v) {
    return v.size();
}

int classTemplateArgument(std::queue<Array<int> > v) {
    return v.size();
}

int oneLevelBasicArgument(std::queue<char*> v) {
    return v.size();
}


int twoLevelBasicArgument(std::queue<char**> v) {
    return v.size();
}


int oneLevelStructureArgument(std::queue<Class*> v) {
    return v.size();
}


int twoLevelStructureArgument(std::queue<Class**> v) {
    return v.size();
}

int oneLevelBasic(std::queue<char>* v) {
    return v[0].size();
}

int twoLevelBasic(std::queue<char>** v) {
    return v[0][0].size();
}

int oneDimBasic(std::queue<char> v[]) {
    return v[0].size();
}

int twoDimBasic(std::queue<char> v[][4]) {
    return v[0][0].size();
}

int oneLevelStructure(std::queue<Class>* v) {
    return v[0].size();
}

int twoLevelStructure(std::queue<Class>** v) {
    return v[0][0].size();
}

int oneDimStructure(std::queue<Class> v[]) {
    return v[0].size();
}

int twoDimStructure(std::queue<Class> v[][4]) {
    return v[0][0].size();
}

int twoLevelOneLevel(std::queue<int*>** v) {
    return v[0][0].size();
}


int twoDimOneLevel(std::queue<int*> v[][4]) {
    return v[0][0].size();
}