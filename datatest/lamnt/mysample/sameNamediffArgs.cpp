int calledFunc(bool x, int y)
{
    return x ? y : 0;
}

int calledFunc(int x) 
{
    return x * 2;
}

int main()
{
    int x = calledFunc(5);
    return 0;
}