#include "dbg.h"
#include <iostream>

using namespace std;

int td_array[2][3] = {
  {6}, 
  {7, 2, 8}
};

int td_array_2[][2] = {
  {5, 3}, 
  {10},
  {100, 3}
};

int func_one(int arr[][3]) {
  return 0;
}
int func_two(int arr[2][2]) {
  return 0;
}

int main(int argc, char *argv[]) {
  func_one(td_array);
  func_two(td_array_2);

	return 0;
}
