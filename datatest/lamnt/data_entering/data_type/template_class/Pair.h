template <typename T, typename V>
class Pair {
public:
	T t;
	V v;
	Pair() {};
	int getT(V _v);
private:
	int foo() {
		return 0;
	}
};



template <typename T, typename V>
class SuperPair : public Pair<T, V> {
public:
	T t;
	V v;
	int stuff;
	SuperPair() {};
	SuperPair(T _t, V _v) {
		t = _t;
		v = _v;
	};
};




class Class
{
public:
    Class() {};
    ~Class() {};
    int x;
};

class CC : public Class
{
public:

};

class Container
{
public:
	Container() {};
	~Container() {};
	Pair<int, float> p;
};


template <typename T>
class Bonus : public Pair<int, T>
{
public:

};

class Conus : public Bonus<float>
{
public:

};

int foo(Conus c) {
	Bonus<int> b;
	SuperPair<int, float> sp;

	SuperPair<int, int> sp2;
	return 0;
}