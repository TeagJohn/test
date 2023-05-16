namespace XXX{
    int test0(int a){return true;}

    class Apple{
    private:
        float weight;
        int price;
    public:
        int doSomeThing(){return true;}
        int doSomeThing1();
        int doSomeThing2(int a);
    };

    int Apple::doSomeThing1(){return true;}
    int Apple::doSomeThing2(int a){return true;}
}