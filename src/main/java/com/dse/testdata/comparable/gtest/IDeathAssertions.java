package com.dse.testdata.comparable.gtest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public interface IDeathAssertions extends IGTestComparable{

    List assertMethods = new ArrayList(Arrays.asList(
            IGTestAssertMethod.EXPECT_DEATH, IGTestAssertMethod.ASSERT_DEATH,
            IGTestAssertMethod.EXPECT_DEATH_IF_SUPPORTED, IGTestAssertMethod.ASSERT_DEATH_IF_SUPPORTED,
            IGTestAssertMethod.EXPECT_DEBUG_DEATH, IGTestAssertMethod.ASSERT_DEBUG_DEATH,
            IGTestAssertMethod.EXPECT_EXIT, IGTestAssertMethod.ASSERT_EXIT));

    String getDeathAssertion(String functionCallStm);
}
