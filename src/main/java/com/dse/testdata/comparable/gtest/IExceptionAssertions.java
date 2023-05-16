package com.dse.testdata.comparable.gtest;

import java.util.*;

public interface IExceptionAssertions extends IGTestComparable {

    List assertMethods = new ArrayList(Arrays.asList(
            IGTestAssertMethod.EXPECT_THROW, IGTestAssertMethod.ASSERT_THROW,
            IGTestAssertMethod.EXPECT_ANY_THROW, IGTestAssertMethod.ASSERT_ANY_THROW,
            IGTestAssertMethod.EXPECT_NO_THROW, IGTestAssertMethod.ASSERT_NO_THROW
    ));

    String getExceptionAssertion(String functionCallStm);
}
