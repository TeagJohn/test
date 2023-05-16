#include <iostream>
#include "string"

class Car {        // The class
  public:          // Access specifier
    std::string brand;  // Attribute
    std::string model;  // Attribute
    int year;      // Attribute
    Car(){

    }
    
    Car(int z){
      if (z >=2022){
	      year = 2022;
      } else {
        year = z;
      }
    }

    Car(std::string x, std::string y) {
      this->brand = x;
      this->model = y;
    }

    Car(std::string x, std::string y, int z):Car(x,y) {
      if (z >= 2022){
        year = 2022;
      } else {
        year = z;
      }
    }
    void setBrand(std::string b){
      brand = b;
    }

    void setModel(std::string m){
      model = m;
    }

    std::string getBrand(){
      return this->brand;
    }

    std::string getModel(){
      return this->model;
    }

    bool isEqual(Car* car) {
      if ((this->model == car->model) && (this->brand == car->brand) && (this->year == car->year)){
        return true;
      }
      return false;
    }

    
    
};

int main()
    {
      Car* car = new Car("Hyundai", "i10", 2022);
      car->setModel("SantaFe");
      Car*car2 = new Car("Hyundai", "i10", 2022);
      std::cout << car->isEqual(car2)<<std::endl;
      return 0;
    }
