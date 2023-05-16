#include "dbg.h"
#include <iostream>

using namespace std;

int arr[2] = { 10, 20};
int arr2[] = {10};

void printSize(int array1[]) {
  cout << (sizeof(array1) / sizeof(*array1)) << endl;
  cout << array1[0] << ' ' << array1[1] << endl;

}

int main(int argc, char *argv[]) {
  log_info("arr[0] = %d", arr[0]);

  cout << arr2[0] << endl;

  cout << "sizeof arr2: " << (sizeof(arr2) / sizeof(*arr2)) << endl;
  cout << arr2[0] << ' ' << arr2[1] << endl;

  printSize(arr2);

	return 0;
}
