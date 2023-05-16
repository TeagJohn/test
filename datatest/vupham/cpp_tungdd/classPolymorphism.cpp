#include <iostream>

using namespace std;

class Vehicle {
    protected:
        float weight;
        int color;
        int price;
    public:
        Vehicle() {

        }

        Vehicle(float weight, int color) {
            this->weight = weight;
            this->color = color;
        }

        ~Vehicle() {

        }

        void setWeight(float weight) {
            this->weight = weight;
        } 

        void setColor(int color) {
            this->color = color;
        }

        float getWeight() {
            return weight;
        }

        int getColor() {
            return color;
        }

        int getPrice() {
            return price;
        }

        friend float getWeight(Vehicle v);
};

float getWeight(Vehicle v) {
    return v.weight * 2;
}

class Plane : public Vehicle {
    private: 
        string model;
        int capacity;
        int countryCode;
    public: 
        Plane() {

        }

        Plane(float weight, int color, string model, int capacity, int countryCode)
            : Vehicle(weight, color) {
            this->model = model;
            this->capacity = capacity;
            this->countryCode = countryCode;
        }

        void setPrice(int price) {
            this->price = price;
        }

        friend class Car;
};

class Car : public Vehicle {
    private:
        string model;
        unsigned int fuelType;
        int capacity;
    public: 
        Car() {

        }

        Car(float weight, int color, string model, unsigned int fuelType, int capacity)
            : Vehicle(weight, color) {
            this->fuelType = fuelType;
            this->capacity = capacity;
            this->model = model;
        }

        ~Car() {

        }

        void setModel(string model) {
            this->model = model;
        }

        void setFuelType(unsigned int fuelType) {
            this->fuelType = fuelType;
        }
        
        void setPrice(int price) {
            this->price = price;
        }

        int compareWithPlane(Plane p) {
            double pricePerSeatCar = 1.0 * price / capacity;
            double pricePerSeatPlane = 1.0 * p.price / p.capacity;
            
            if (pricePerSeatCar > pricePerSeatPlane) {
                return 1;
            } else {
                if (model == p.model) {
                    return 0;
                } else {    
                    if (pricePerSeatCar == pricePerSeatPlane) {
                        return 0;
                    } else {
                        return -1;
                    }
                }
            }   
        }
};