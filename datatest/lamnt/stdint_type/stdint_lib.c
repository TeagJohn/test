#include <stdint.h>

int called(uint16_t x)
{
	return x + 1;
}

int callee()
{
	return called(16);
}