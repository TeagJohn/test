#include <vector> // <vector>
#include <set> // <vector>
#include <string>

std::vector<int> returnVector(int x) {
	std::vector<int> v;
	for (int i = 0; i < x; i++) {
		v.push_back(i);
	}

	return v;
}


std::set<int> returnSet(int x) {
	std::set<int> v;
	for (int i = 0; i < x; i++) {
		v.insert(i);
	}

	return v;
}