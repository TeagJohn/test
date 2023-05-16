package com.dse.testdata.comparable.gtest;

import java.util.*;

public interface IBinaryComparison extends IGTestComparable {

    List assertMethods = new ArrayList(Arrays.asList(
            IGTestAssertMethod.EXPECT_EQ, IGTestAssertMethod.ASSERT_EQ,
            IGTestAssertMethod.EXPECT_NE, IGTestAssertMethod.ASSERT_NE,
            IGTestAssertMethod.EXPECT_LT, IGTestAssertMethod.ASSERT_LT,
            IGTestAssertMethod.EXPECT_GT, IGTestAssertMethod.ASSERT_GT,
            IGTestAssertMethod.EXPECT_LE, IGTestAssertMethod.ASSERT_LE,
            IGTestAssertMethod.EXPECT_GE, IGTestAssertMethod.ASSERT_GE));

    String getBinaryComparisonAssertion();
}
