class Class {
public:
    int x;
    Class() {
        x = 0;
    };
};

template <typename T> 
class Array { 
private: 
    T *ptr; 
    int size; 
public: 
    Array(T arr[], int s) {
    	size = s;
    	ptr = new T[s];
    	for (int i = 0; i <= s; i++) {
    		ptr[i] = arr[i];
    	}
    };
    void print(); 
};