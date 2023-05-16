package com.dse.testdata.comparable;

import static com.dse.report.converter.AssertionConverter.FALSE_VALUE;
import static com.dse.report.converter.AssertionConverter.NULL_VALUE;

public interface AssertMethod {

    /*
     * For CUnit test script
     */
    interface AKA {
        String ASSERT_TRUE = "CU_ASSERT_TRUE";
        String ASSERT_FALSE = "CU_ASSERT_FALSE";
        String ASSERT_EQUAL = "CU_ASSERT_EQUAL";
        String ASSERT_NOT_EQUAL = "CU_ASSERT_NOT_EQUAL";
        String ASSERT_PTR_EQUAL = "CU_ASSERT_PTR_EQUAL";
        String ASSERT_PTR_NOT_EQUAL = "CU_ASSERT_PTR_NOT_EQUAL";
        String ASSERT_NULL = "CU_ASSERT_PTR_NULL";
        String ASSERT_NOT_NULL = "CU_ASSERT_PTR_NOT_NULL";
        String ASSERT_STR_EQUAL = "CU_ASSERT_STRING_EQUAL";
        String ASSERT_STR_NOT_EQUAL = "CU_ASSERT_STRING_NOT_EQUAL";
        String ASSERT_DOUBLE_EQUAL = "CU_ASSERT_DOUBLE_EQUAL";
        String ASSERT_DOUBLE_NOT_EQUAL = "CU_ASSERT_DOUBLE_NOT_EQUAL";
        String ASSERT_LOWER = "CU_ASSERT_LOWER";
        String ASSERT_GREATER = "CU_ASSERT_GREATER";
        String ASSERT_LOWER_OR_EQUAL = "CU_ASSERT_LOWER_OR_EQUAL";
        String ASSERT_GREATER_OR_EQUAL = "CU_ASSERT_GREATER_OR_EQUAL";
    }

    /*
     * For CUnit test script
     */
    interface CU {
        String ASSERT_TRUE = "CU_ASSERT_TRUE";
        String ASSERT_FALSE = "CU_ASSERT_FALSE";
        String ASSERT_EQUAL = "CU_ASSERT_EQUAL";
        String ASSERT_NOT_EQUAL = "CU_ASSERT_NOT_EQUAL";
        String ASSERT_PTR_EQUAL = "CU_ASSERT_PTR_EQUAL";
        String ASSERT_PTR_NOT_EQUAL = "CU_ASSERT_PTR_NOT_EQUAL";
        String ASSERT_NULL = "CU_ASSERT_PTR_NULL";
        String ASSERT_NOT_NULL = "CU_ASSERT_PTR_NOT_NULL";
        String ASSERT_STR_EQUAL = "CU_ASSERT_STRING_EQUAL";
        String ASSERT_STR_NOT_EQUAL = "CU_ASSERT_STRING_NOT_EQUAL";
        String ASSERT_DOUBLE_EQUAL = "CU_ASSERT_DOUBLE_EQUAL";
        String ASSERT_DOUBLE_NOT_EQUAL = "CU_ASSERT_DOUBLE_NOT_EQUAL";
        String ASSERT_LOWER = "CU_ASSERT_LOWER";
        String ASSERT_GREATER = "CU_ASSERT_GREATER";
        String ASSERT_LOWER_OR_EQUAL = "CU_ASSERT_LOWER_OR_EQUAL";
        String ASSERT_GREATER_OR_EQUAL = "CU_ASSERT_GREATER_OR_EQUAL";
    }

    /*
     * For GTest test script
     */
    interface GTest {
        //Generalized Assertion
        String EXPECT_THAT = "EXPECT_THAT";
        String ASSERT_THAT = "ASSERT_THAT";

        //Boolean Conditions
        String EXPECT_TRUE = "EXPECT_TRUE";
        String ASSERT_TRUE = "ASSERT_TRUE";

        String EXPECT_FALSE = "EXPECT_FALSE";
        String ASSERT_FALSE = "ASSERT_FALSE";

        //Binary Comparison
        String EXPECT_EQ = "EXPECT_EQ";
        String ASSERT_EQ = "ASSERT_EQ";
        String EXPECT_NE = "EXPECT_NE";
        String ASSERT_NE = "ASSERT_NE";
        String EXPECT_LT = "EXPECT_LT";
        String ASSERT_LT = "ASSERT_LT";
        String EXPECT_GT = "EXPECT_GT";
        String ASSERT_GT = "ASSERT_GT";
        String EXPECT_LE = "EXPECT_LE";
        String ASSERT_LE = "ASSERT_LE";
        String EXPECT_GE = "EXPECT_GE";
        String ASSERT_GE = "ASSERT_GE";

        //String Comparison
        String EXPECT_STREQ = "EXPECT_STREQ";
        String ASSERT_STREQ = "ASSERT_STREQ";
        String EXPECT_STRNE = "EXPECT_STRNE";
        String ASSERT_STRNE = "ASSERT_STRNE";
        String EXPECT_STRCASEEQ = "EXPECT_STRCASEEQ";
        String ASSERT_STRCASEEQ = "ASSERT_STRCASEEQ";
        String EXPECT_STRCASENE = "EXPECT_STRCASENE";
        String ASSERT_STRCASENE = "ASSERT_STRCASENE";

        //Floating-Point Comparison
        String EXPECT_FLOAT_EQ = "EXPECT_FLOAT_EQ";
        String ASSERT_FLOAT_EQ = "ASSERT_FLOAT_EQ";
        String EXPECT_DOUBLE_EQ = "EXPECT_DOUBLE_EQ";
        String ASSERT_DOUBLE_EQ = "ASSERT_DOUBLE_EQ";
        String EXPECT_NEAR = "EXPECT_NEAR";
        String ASSERT_NEAR = "ASSERT_NEAR";

        //Exception Assertions;
        String EXPECT_THROW = "EXPECT_THROW";
        String ASSERT_THROW = "ASSERT_THROW";
        String EXPECT_ANY_THROW = "EXPECT_ANY_THROW";
        String ASSERT_ANY_THROW = "ASSERT_ANY_THROW";
        String EXPECT_NO_THROW = "EXPECT_NO_THROW";
        String ASSERT_NO_THROW = "ASSERT_NO_THROW";


        String EXPECT_NULL = "EXPECT_NULL";
        String EXPECT_NOT_NULL = "EXPECT_NOT_NULL";


    }

    /*
     * For display
     */
    String ASSERT_EQUAL = "ASSERT EQUAL";
    String ASSERT_NOT_EQUAL = "ASSERT NOT EQUAL";
    String ASSERT_NULL = "ASSERT NULL";
    String ASSERT_NOT_NULL = "ASSERT NOT NULL";
    String ASSERT_TRUE = "ASSERT TRUE";
    String ASSERT_FALSE = "ASSERT FALSE";
    String ASSERT_LOWER = "ASSERT LOWER";
    String ASSERT_GREATER = "ASSERT GREATER";
    String ASSERT_LOWER_OR_EQUAL = "ASSERT LOWER OR EQUAL";
    String ASSERT_GREATER_OR_EQUAL = "ASSERT GREATER OR EQUAL";
    String USER_CODE = "USER CODE";

    static boolean isMatch(String assertMethod, String actual, String expected) {
        switch (assertMethod) {
            case AssertMethod.ASSERT_EQUAL:
                return actual.equals(expected);

            case AssertMethod.ASSERT_NOT_EQUAL:
                return !actual.equals(expected);

            case AssertMethod.ASSERT_LOWER: {
                double actualVal = Double.parseDouble(actual);
                double expectedVal = Double.parseDouble(expected);
                return actualVal < expectedVal;
            }

            case AssertMethod.ASSERT_GREATER: {
                double actualVal = Double.parseDouble(actual);
                double expectedVal = Double.parseDouble(expected);
                return actualVal > expectedVal;
            }

            case AssertMethod.ASSERT_LOWER_OR_EQUAL: {
                double actualVal = Double.parseDouble(actual);
                double expectedVal = Double.parseDouble(expected);
                return actualVal <= expectedVal;
            }

            case AssertMethod.ASSERT_GREATER_OR_EQUAL: {
                double actualVal = Double.parseDouble(actual);
                double expectedVal = Double.parseDouble(expected);
                return actualVal >= expectedVal;
            }

            case AssertMethod.ASSERT_TRUE:
                return !actual.equals(FALSE_VALUE);

            case AssertMethod.ASSERT_FALSE:
                return actual.equals(FALSE_VALUE);

            case AssertMethod.ASSERT_NULL:
                return actual.equals(NULL_VALUE);

            case AssertMethod.ASSERT_NOT_NULL:
                return !actual.equals(NULL_VALUE);

            default:
                return false;
        }
    }

    static String findExpectedFromFailure(String assertMethod, String defaultExpected) {
        String expected = defaultExpected;

        switch (assertMethod) {
            case AssertMethod.ASSERT_EQUAL: {
                expected = "EQUAL " + defaultExpected;
                break;
            }

            case AssertMethod.ASSERT_NOT_EQUAL: {
                expected = "NOT EQUAL " + defaultExpected;
                break;
            }

            case AssertMethod.ASSERT_LOWER:
                expected = "LOWER " + defaultExpected;
                break;

            case AssertMethod.ASSERT_GREATER:
                expected = "GREATER " + defaultExpected;
                break;

            case AssertMethod.ASSERT_LOWER_OR_EQUAL:
                expected = "LOWER OR EQUAL " + defaultExpected;
                break;

            case AssertMethod.ASSERT_GREATER_OR_EQUAL:
                expected = "GREATER OR EQUAL " + defaultExpected;
                break;

            case AssertMethod.ASSERT_TRUE:
                expected = "TRUE";
                break;

            case AssertMethod.ASSERT_FALSE:
                expected = "FALSE";
                break;

            case AssertMethod.ASSERT_NULL:
                expected = "NULL";
                break;

            case AssertMethod.ASSERT_NOT_NULL:
                expected = "NOT NULL";
                break;

            case AssertMethod.USER_CODE:
                expected = "USER CODE";
                break;
        }

        return expected;
    }
}
