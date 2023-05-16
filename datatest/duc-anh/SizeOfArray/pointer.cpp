void f1(int* arr, int n) {
	for (i = 0; i<=n-1; i++) {
	    arr[i] = 0;
	}
}

void f2(int* arr, int n) {
	for (i = 0; n-1>=i; i++) {
	    arr[i] = 0;
	}
}

void f3(int* arr, int n) {
	for (i = 0; i<n; i++) {
	    arr[i] = 0;
	}
}

void f4(int* arr, int n) {
	for (i = 0; n>i; i++) {
	    arr[i] = 0;
	}
}

// do not support
void f5(int* arr, int n) {
	for (i = 0; i+1<=n; i++) {
	    arr[i] = 0;
	}
}

// do not support
void f6(int* arr, int n) {
	for (i = 0; n>=i+1; i++) {
	    arr[i] = 0;
	}
}