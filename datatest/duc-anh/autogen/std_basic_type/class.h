#ifndef MyClass_H
#define MyClass_H
class MyClass {
public:
    int x;
    MyClass() {
        x = 0;
    };
    MyClass(int y) {
        x = y;
    };

    bool operator< (const MyClass& e) const
    {
        return true;
    }
};

template <typename T> 
class Array { 
private: 
    T *ptr; 
    int size; 
public:
    bool operator< (const Array& e) const
    {
        return true;
    }

    Array(T arr[], int s) {
    	size = s;
    	ptr = new T[s];
    	for (int i = 0; i <= s; i++) {
    		ptr[i] = arr[i];
    	}
    };
    void print(); 
};

#endif
