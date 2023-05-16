/*
 * TEST DRIVER FOR C
 * @author: VNU-UET
 * Generate automatically by AKAUTAUTO
 */

// include some necessary standard libraries
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdbool.h>

#define ASSERT_ENABLE

// define maximum line of test path
#define AKA_MARK_MAX 4294967290

// function call counter
int AKA_fCall = 0;

// test case name
char * AKA_test_case_name;

typedef void (*AKA_Test)();

void AKA_run_test(const char * name, AKA_Test test, int iterator);

////////////////////////////////////////
//  BEGIN TEST PATH SECTION           //
////////////////////////////////////////

#define AKA_TEST_PATH_FILE "{{INSERT_PATH_OF_TEST_PATH_HERE}}"

FILE* AKA_tp_file;

void AKA_append_test_path(char content[]);

int AKA_mark(char * append);

////////////////////////////////////////
//  END TEST PATH SECTION             //
////////////////////////////////////////


////////////////////////////////////////
//  BEGIN TEST RESULT SECTION         //
////////////////////////////////////////

#define AKA_EXEC_TRACE_FILE "{{INSERT_PATH_OF_EXE_RESULT_HERE}}"

FILE* AKA_rt_file;

void AKA_append_test_result(char content[]);

void AKA_assert_method
(
    char * actualName, int actualVal,
    char * expectedName, int expectedVal,
    char * method
);

void AKA_assert_double_method
(
    char * actualName, double actualVal,
    char * expectedName, double expectedVal,
    char * method
);

void AKA_assert_ptr_method
(
    char * actualName, void * actualVal,
    char * expectedName, void * expectedVal,
    char * method
);

void AKA_assert
(
    char * actualName, int actualVal,
    char * expectedName, int expectedVal
)
{
    AKA_assert_method
    (
        actualName, actualVal,
        expectedName, expectedVal,
        NULL
    );
}

int AKA_assert_double
(
    char * actualName, double actualVal,
    char * expectedName, double expectedVal
)
{
    AKA_assert_double_method
    (
        actualName, actualVal,
        expectedName, expectedVal,
        NULL
    );
}

int AKA_assert_ptr
(
    char * actualName, void * actualVal,
    char * expectedName, void * expectedVal
)
{
    AKA_assert_ptr_method
    (
        actualName, actualVal,
        expectedName, expectedVal,
        NULL
    );
}

////////////////////////////////////////
//  END TEST RESULT SECTION           //
////////////////////////////////////////


////////////////////////////////////////
//  BEGIN SET UP - TEAR DOWN SECTION  //
////////////////////////////////////////

/*
 * This function call before main test driver
 */
void AKA_set_up();

/*
 * This function call after main test driver
 */
void AKA_tear_down();

////////////////////////////////////////
//  END SET UP - TEAR DOWN SECTION    //
////////////////////////////////////////

// Some test cases need to include specific additional headers
/*{{INSERT_ADDITIONAL_HEADER_HERE}}*/

// Include akaignore file
/*{{INSERT_CLONE_SOURCE_FILE_PATHS_HERE}}*/

////////////////////////////////////////
//  BEGIN TEST SCRIPTS SECTION        //
////////////////////////////////////////

/*{{INSERT_TEST_SCRIPTS_HERE}}*/

////////////////////////////////////////
//  END TEST SCRIPTS SECTION          //
////////////////////////////////////////

/* 
 * The main() function for setting up and running the tests.
 */
int main()
{
    AKA_set_up();
    
    /* Compound test case setup */

    /* add & run the tests */
/*{{ADD_TESTS_STM}}*/

    /* Compound test case teardown */

    AKA_tear_down();
    
    return 0;
}

////////////////////////////////////////
//  BEGIN DEFINITIONS SECTION         //
////////////////////////////////////////

void AKA_append_test_path(char content[])
{
    static unsigned int aka_mark_iterator = 0;
    
    AKA_tp_file = fopen(AKA_TEST_PATH_FILE, "a");
    fputs(content, AKA_tp_file);
    aka_mark_iterator++;

    // if the test path is too long, we need to terminate the process
    if (aka_mark_iterator >= AKA_MARK_MAX) {
        fputs("\nThe test path is too long. Terminate the program automatically!", AKA_tp_file);
        fclose(AKA_tp_file);
        exit(0);
    }

    fclose(AKA_tp_file);
}

void AKA_append_test_result(char content[]) 
{
    AKA_rt_file = fopen(AKA_EXEC_TRACE_FILE, "a");
    fputs(content, AKA_rt_file);
    fclose(AKA_rt_file);
}

#define AKA_MAX_LINE_LENGTH 100000

int AKA_mark(char * append) 
{
    char build[AKA_MAX_LINE_LENGTH] = "";
    strcat(build, append);
    strcat(build, "\n");
    AKA_append_test_path(build);
    return 1;
}

#define AKA_BUFFER_SIZE 1024

void AKA_assert_method
(
    char * actualName, int actualVal,
    char * expectedName, int expectedVal,
    char * userCode
)
{
    char buf[AKA_MAX_LINE_LENGTH] = "{\n";

    strcat(buf, "\"tag\": \"Aka function calls: ");
    char temp0[AKA_BUFFER_SIZE];
    sprintf(temp0, "%d\",", AKA_fCall);
    strcat(buf, temp0);
    strcat(buf, "\n");

    if (userCode != NULL)
    {
        strcat(buf, "\"userCode\": \"");
        strcat(buf, userCode);
        strcat(buf, "\",\n");
    }

    strcat(buf, "\"actualName\": \"");
    strcat(buf, actualName);
    strcat(buf, "\",\n");
    char temp1[AKA_BUFFER_SIZE];
    sprintf(temp1, "\"actualVal\": \"%d\",", actualVal);
    strcat(buf, temp1);
    strcat(buf, "\n");

    strcat(buf, "\"expectedName\": \"");
    strcat(buf, expectedName);
    strcat(buf, "\",\n");
    char temp2[AKA_BUFFER_SIZE];
    sprintf(temp2, "\"expectedVal\": \"%d\"", expectedVal);
    strcat(buf, temp2);
    strcat(buf, "\n},\n");

    AKA_append_test_result(buf);
}

void AKA_assert_double_method
(
    char * actualName, double actualVal,
    char * expectedName, double expectedVal,
    char * userCode
)
{
    char buf[AKA_MAX_LINE_LENGTH] = "{\n";

    strcat(buf, "\"tag\": \"Aka function calls: ");
    char temp0[AKA_BUFFER_SIZE];
    sprintf(temp0, "%d\",", AKA_fCall);
    strcat(buf, temp0);
    strcat(buf, "\n");

    if (userCode != NULL) {
        strcat(buf, "\"userCode\": \"");
        strcat(buf, userCode);
        strcat(buf, "\",\n");
    }

    strcat(buf, "\"actualName\": \"");
    strcat(buf, actualName);
    strcat(buf, "\",\n");

    char temp1[AKA_BUFFER_SIZE];
    sprintf(temp1, "\"actualVal\": \"%lf\",", actualVal);
    strcat(buf, temp1);
    strcat(buf, "\n");

    strcat(buf, "\"expectedName\": \"");
    strcat(buf, expectedName);
    strcat(buf, "\",\n");

    char temp2[AKA_BUFFER_SIZE];
    sprintf(temp2, "\"expectedVal\": \"%lf\"", expectedVal);
    strcat(buf, temp2);
    strcat(buf, "\n},\n");

    AKA_append_test_result(buf);
}

void AKA_assert_ptr_method
(
    char * actualName, void * actualVal,
    char * expectedName, void * expectedVal,
    char * userCode
)
{
    char buf[AKA_MAX_LINE_LENGTH] = "{\n";

    strcat(buf, "\"tag\": \"Aka function calls: ");
    char temp0[AKA_BUFFER_SIZE];
    sprintf(temp0, "%d\",", AKA_fCall);
    strcat(buf, temp0);
    strcat(buf, "\n");

    if (userCode != NULL) {
        strcat(buf, "\"userCode\": \"");
        strcat(buf, userCode);
        strcat(buf, "\",\n");
    }

    strcat(buf, "\"actualName\": \"");
    strcat(buf, actualName);
    strcat(buf, "\",\n");

    char temp1[AKA_BUFFER_SIZE];
    sprintf(temp1, "\"actualVal\": \"%x\",", actualVal);
    strcat(buf, temp1);
    strcat(buf, "\n");

    strcat(buf, "\"expectedName\": \"");
    strcat(buf, expectedName);
    strcat(buf, "\",\n");

    char temp2[AKA_BUFFER_SIZE];
    sprintf(temp2, "\"expectedVal\": \"%x\"", expectedVal);
    strcat(buf, temp2);
    strcat(buf, "\n},\n");

    AKA_append_test_result(buf);
}

void AKA_run_test(const char * name, AKA_Test test, int iterator)
{
    char begin[AKA_BUFFER_SIZE];
    sprintf(begin, "BEGIN OF %s", name);
    AKA_mark(begin);

    int i;
    for (i = 0; i < iterator; i++) {
        test();
    }

    char end[AKA_BUFFER_SIZE];
    sprintf(end, "END OF %s", name);
    AKA_mark(end);
}

void AKA_set_up()
{
    /*{{INSERT_SET_UP_HERE}}*/
}

void AKA_tear_down()
{
    /*{{INSERT_TEAR_DOWN_HERE}}*/
}

////////////////////////////////////////
//  END DEFINITIONS SECTION           //
////////////////////////////////////////