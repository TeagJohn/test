package com.dse.testdata.comparable.gtest;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public interface IGTestAssertMethod {
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

    //Predicate Assertions

    String EXPECT_PRED = "EXPECT_PRED";
    String ASSERT_PRED = "ASSERT_PRED";

    //Death Assertions

    String EXPECT_DEATH = "EXPECT_DEATH";
    String ASSERT_DEATH = "ASSERT_DEATH";
    String EXPECT_DEATH_IF_SUPPORTED = "EXPECT_DEATH_IF_SUPPORTED";
    String ASSERT_DEATH_IF_SUPPORTED = "ASSERT_DEATH_IF_SUPPORTED";
    String EXPECT_DEBUG_DEATH = "EXPECT_DEBUG_DEATH";
    String ASSERT_DEBUG_DEATH = "ASSERT_DEBUG_DEATH";
    String EXPECT_EXIT = "EXPECT_EXIT";
    String ASSERT_EXIT = "ASSERT_EXIT";


    String EXPECT_NULL = "EXPECT_NULL";
    String EXPECT_NOT_NULL = "EXPECT_NOT_NULL";


}
