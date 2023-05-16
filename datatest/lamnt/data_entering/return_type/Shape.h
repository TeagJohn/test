class Shape
{
public:
	Shape() {}
	Shape(int a) {area = a;}
	int area;
};

class Circle : public Shape
{
public:
	Circle(int _r) {r = _r;}
	int r;
};