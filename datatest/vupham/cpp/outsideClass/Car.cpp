#include "string"
using namespace std;

class Car {        // The class
  public:          // Access specifier
    string brand;  // Attribute
    string model;  // Attribute
    int year;      // Attribute
    Car();
    Car(int);
    Car(string, string);
    Car(string, string, int); // Constructor declaration

    void setBrand(string);
    void setModel(string);

    string getBrand();
    string getModel();

    bool isEqual(Car*);
};

Car::Car(){

}

Car::Car(int z):Car(){
  year = z;
}

Car::Car(string x, string y) {
  this->brand = x;
  this->model = y;
}


// Constructor definition outside the class
Car::Car(string x, string y, int z): Car(x, y) {
  if (z > 2022) {
    year = 2022;
  } else {
    year = z;
  }
}

void Car::setBrand(string b) {
  brand = b;
}

void Car::setModel(string m){
  model = m;
}

string Car::getBrand(){
  return brand;
}

string Car::getModel(){
  return model;
}

bool Car::isEqual(Car* car) {
  if ((this->model == car->model) && (this->brand == car->brand) && (this->year == car->year)){
    return true;
  }
  return false;
}
