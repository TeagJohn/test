int g_UUT = 0;

int simple ( int x ) {
    return x;
}

int UUT( int x ) {
    int result = x;
    result = simple ( x );
    return result;
}

