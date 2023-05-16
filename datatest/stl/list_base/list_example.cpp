#include <iostream>
#include <bits/stdc++.h>
using namespace std;

void showTheContent(list<int> l)
{
    list<int>::iterator it;
    for(it=l.begin();it!=l.end();it++)
    {
        cout << *it << " ";
    }
    cout << "\n";
}

list<int> insertValueAtBack(list<int> l) {
    for(int i=0; i<10; i++)
        l.push_back(i+1);

    return l;
}

list<int> insertValueAtFront(list<int> l) {
    for(int i=0; i<10; i++)
        l.push_front(i+1);

    return l;
}

list<int> removeFromFront(list<int> l, int times) {
    while(times--)
    {
        l.pop_front();
    }

    return l;
}

list<int> removeFromBack(list<int> l, int times) {
    while(times--)
    {
        l.pop_back();
    }

    return l;
}

// Sample Code to show List and its functions
void perform(list<int> list1, list<int> list2)
{
    // inserting at the back
    list1 = insertValueAtBack(list1);

    //inserting at the front
    list2 = insertValueAtFront(list2);

    cout << "Content of List 1: ";
    showTheContent(list1);
    cout << "Content of list 2: ";
    showTheContent(list2);

    // sorting the second list
    list2.sort();
    cout << "Sorted List2 : ";
    showTheContent(list2);

    //Removing five elements from front in list1.
    list1 = removeFromFront(list1, 5);
    cout << "Content of List 1: " ;
    showTheContent(list1);

    //Removing five elements from the back in list2.
    list1 = removeFromBack(list1, 5);
    cout << "Content of List 2: ";
    showTheContent(list2);

    //seek the first element of list 1
    cout << list1.front() << " is now at the front in list 1\n";
    // seek the last element in list 2
    cout << list2.back() << " is now the last element in list 2\n";

    //Inserting elements in list 1.
    list1.insert(list1.begin(),5,10);
    cout << "After Insertion list 1: ";
    showTheContent(list1);

    //remove() to remove all the elements with value 10.
    list1.remove(10);
    cout << "After Removal list 1: ";
    showTheContent(list1);

    // size() to know the number of elements
    cout << "No. of elements in list 1: ";
    cout << list1.size() << "\n";

    //Reversing the content of list 2
    list2.reverse();
    cout << "Reversed list 2: ";
    showTheContent(list2);

    //erasing first element of list 2
    list2.erase(list2.begin());
    cout << "After erasing from list 2: ";
    showTheContent(list2);

    //Removing all elements from list 1.
    list1.clear();
    
    // Use of empty() function
    if(list1.empty()) cout << "List 1 is now empty\n";
    else cout << "Not Empty\n";

    // use of assign function
    list1.assign(5,2); // 2 2 2 2 2
    cout << "List 1: ";
    showTheContent(list1);
}