struct Node{
    int age;
    Node* n;
};

int test12(Node x){return 0;}
int test13(Node* x){return 0;}
int test14(Node** x){return 0;}

int test14(Node x[]){return 0;}
int test15(Node x[3]){return 0;}
int test16(Node x[3][2]){return 0;}
int test17(Node x[][2]){return 0;}
