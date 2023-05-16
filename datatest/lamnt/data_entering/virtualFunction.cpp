class Shape {
public:
	virtual int getArea() {
		return 0;
	}
};

class Rectangle : public Shape {
public:
	int a;
	int b;

	int getArea() {
		return a * b;
	}
};

class Circle : public Shape {
public:
	int r;

	int getArea() {
		return 3.14 * r * r;
	}
};