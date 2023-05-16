/*
 * TEST DRIVER FOR C++
 * @author: VNU-UET
 * Generate automatically by AKAUTAUTO
 */

// include some necessary standard libraries
#include <cstdio>
#include <string>
#include <fstream>
#include <string.h>
#include "gtest/gtest.h"
#include "gmock/gmock.h"

#define ASSERT_ENABLE

// define maximum line of test path
#define AKA_MARK_MAX 4294967290

// function call counter
int AKA_fCall = 0;

bool AKA_mark_testpath_disable = false;

// test case name
char* AKA_test_case_name;

typedef void (*AKA_Test)();

void AKA_run_test(std::string name, AKA_Test test, int iterator);

////////////////////////////////////////
//  BEGIN TEST PATH SECTION           //
////////////////////////////////////////

#define AKA_TEST_PATH_FILE "{{INSERT_PATH_OF_TEST_PATH_HERE}}"

void AKA_append_test_path(std::string content);

int AKA_mark(std::string append);

int AKA_mark(char * append)
{
    std::string str(append);
    return AKA_mark(str);
}

////////////////////////////////////////
//  END TEST PATH SECTION             //
////////////////////////////////////////

// Some test cases need to include specific additional headers
/*{{INSERT_ADDITIONAL_HEADER_HERE}}*/

// Include akaignore file
/*{{INSERT_CLONE_SOURCE_FILE_PATHS_HERE}}*/

// Mock class
/*{{INSERT_MOCK_PRE_TEST_SCRIPTS_HERE}}*/

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
int main(int argc, char **argv)
{
    ::testing::InitGoogleTest(&argc, argv);
    return RUN_ALL_TESTS();

}

////////////////////////////////////////
//  BEGIN DEFINITIONS SECTION         //
////////////////////////////////////////

void AKA_append_test_path(std::string content)
{
    static unsigned int aka_mark_iterator = 0;

    std::ofstream outfile;
    outfile.open(AKA_TEST_PATH_FILE, std::ios_base::app);
    outfile << content;
    aka_mark_iterator++;

    // if the test path is too long, we need to terminate the process
    if (aka_mark_iterator >= AKA_MARK_MAX) {
        outfile << "\nThe test path is too long. Terminate the program automatically!";
        outfile.close();
        exit(0);
    }

    outfile.close();
}


int AKA_mark(std::string append)
{
    if (AKA_mark_testpath_disable) return 1;
    AKA_append_test_path(append + "\n");
    return 1;
}

#define AKA_BUFFER_SIZE 1024




////////////////////////////////////////
//  END DEFINITIONS SECTION           //
////////////////////////////////////////