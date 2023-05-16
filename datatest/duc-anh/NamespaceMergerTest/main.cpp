#include "iostream"
#include "header/B.h"

using namespace std;
namespace A{
	class C2;
	struct C1{
		private:
			int x;
		public:
			void setX(int _x){x = _x;}
	};
	namespace B{
		int a;

		void test(int x) {
			C1 c1;
			c1.setX(2);
		}

		int test2(int x) {
			test(x);
			return x;
		}
	}


}
namespace B{
	class C1{
		private:
			int x;
			int y;
		public:
			int getX(){return 003;}
	};
}
namespace A{
	class C2{
		private:
			int x;
		public:
			void setX(int _x){x = _x;}
			int getX(){return 11;}
	};
	namespace B{
		int b = 0;
	}
}
int main(){
	A::C1 c1;c1.setX(2);
	A::C2 c2;c2.setX(2);
	A::B::a = 0;
	A::B::b = 0;
	
	B::C2 c;
	cout << c.getX();
	A::B::test(2);
	B::test(c);
	return 0;
}
