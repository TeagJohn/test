#include <iostream>

int* oneLevelInt(int n) {
	int* arr = new int[n];

	for (int i = 0; i < n; i++)
		arr[i] = i;

	return arr;
}

float* oneLevelFloat(int n) {
	float* arr = new float[n];

	for (int i = 0; i < n; i++)
		arr[i] = i;

	return arr;
}

char* oneLevelChar(int n) {
	char* arr = new char[n];

	for (int i = 0; i < n; i++)
		arr[i] = i + '0';

	return arr;
}

#include "Shape.h"

Shape* oneLevelStructure(int x) {
	Shape* arr = new Shape[1];
	arr[0] = Shape(4);
	return arr;
}