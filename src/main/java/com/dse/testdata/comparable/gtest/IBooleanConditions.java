package com.dse.testdata.comparable.gtest;

import java.util.*;

public interface IBooleanConditions extends IGTestComparable {

    List assertMethods = new ArrayList(Arrays.asList(
            IGTestAssertMethod.EXPECT_TRUE, IGTestAssertMethod.ASSERT_TRUE,
            IGTestAssertMethod.EXPECT_FALSE, IGTestAssertMethod.ASSERT_FALSE));

    String getBooleanConditionAssertion();

}
