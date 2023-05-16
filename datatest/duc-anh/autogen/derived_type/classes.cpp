struct AA{
    int x;
};

int a[3];
AA globalAA;

class People{
protected:
    int age;
    char* address;
public:
    People(){}
    People(int tuoi, char* diachi){
        age = tuoi;
        address = diachi;
    }
    ~People(){}


    //-----------------
    // AUTOGEN for function in a class - begin
    //-----------------
    int test1(AA a){return true;}
    int test1_1(AA* a){return true;}
    int test1_2(AA a[3]){return true;}
    int test1_3(AA a[]){return true;}
    int test1_4(AA a[][3]){return true;}
    int test1_5(AA a[2][3]){return true;}

    int test2(People a){return true;}
    int test2_1(People* a){return true;}
    int test2_2(People a[3]){return true;}
    int test2_3(People a[]){return true;}
    int test2_4(People a[][3]){return true;}
    int test2_5(People a[2][3]){return true;}

    int test3(int a);
    //-----------------
    // AUTOGEN for function in a class - end
    //-----------------
};

// function of a class 
//

int People::test3(int a){return true;}

People globalPeople;

class Student: public People{
private:
    int masinhvien;

public:
    Student(){}
    Student(int tuoi, char* diachi, int msv){
        age = tuoi;
        address = diachi;
        masinhvien = msv;
    }
};

class Employee: public People{
private:
    int manhanvien;

public:
    Employee(){}
    Employee(int tuoi, char* diachi, int mnv){
        age = tuoi;
        address = diachi;
        manhanvien = mnv;
    }
};

int testPeople0(People x){return 0;}
int testPeople1(People* x){return 0;}
int testPeople2(People** x){return 0;}

int testPeople3(People x[]){return 0;}
int testPeople4(People x[3]){return 0;}
int testPeople5(People x[3][2]){return 0;}
int testPeople6(People x[][2]){return 0;}

int testStudent0(Student x){return 0;}
int testStudent1(Student* x){return 0;}
int testStudent2(Student** x){return 0;}

int testStudent3(Student x[]){return 0;}
int testStudent4(Student x[3]){return 0;}
int testStudent5(Student x[3][2]){return 0;}
int testStudent6(Student x[][2]){return 0;}


