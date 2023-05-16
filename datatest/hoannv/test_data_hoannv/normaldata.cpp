#include <stdio.h>
#include <string.h>
#include "dbg.h"

using namespace std;

char char1 = 'c';

int inta = 0;
short shortb = 0;
long longc = 0;

unsigned int usint = 0;
unsigned short usshort = 0;
unsigned long uslong = 0;

signed int sint = 0;
signed short sshort = 0;
signed long slong = 0;

float floatd = 0.0;
double doublee = 0.0;

int main(int argc, char* argv[]) {

	char1 = 'z';

	inta = 1;
	shortb = 2;
	longc = 3;
	
	usint = 0;
	usshort = 10;
	uslong = 100;

	sint = 1;
	sshort = 100;
	debug("%ld", slong);

	floatd = 10.0;
	doublee = 100.0;

	return 0;
}

