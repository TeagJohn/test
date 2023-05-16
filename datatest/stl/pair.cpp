#include <utility> 

int getFirst(std::pair<int, float> p) {
	return p.first;
}

float getSecond(std::pair<int, float> p) {
	return p.second;
}

std::pair<int, float> makePair(int i, float f) {
	std::pair<int, float> p;
	p.first = i;
	p.second = f;
	return p;
}

#include "list_base/class.h"

int getX(std::pair<Class, int> p) {
	return p.second;
}

int nested(std::pair<int, std::pair<int, char>> p) {
	return 0;
}

int templatsd(std::pair<Array<int>, int> p) {
	return 0;
}

#include <map>

int testMap(std::map<int, std::string> m) {
	return m.size();
}