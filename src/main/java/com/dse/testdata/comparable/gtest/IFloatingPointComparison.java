package com.dse.testdata.comparable.gtest;

import java.util.*;

public interface IFloatingPointComparison extends IBinaryComparison {
    List assertMethods = new ArrayList(Arrays.asList(
            IGTestAssertMethod.EXPECT_FLOAT_EQ, IGTestAssertMethod.ASSERT_FLOAT_EQ,
            IGTestAssertMethod.EXPECT_DOUBLE_EQ, IGTestAssertMethod.ASSERT_DOUBLE_EQ,
            IGTestAssertMethod.EXPECT_NEAR, IGTestAssertMethod.ASSERT_NEAR));

    String getFloatingPointComparisonAssertion();
}
