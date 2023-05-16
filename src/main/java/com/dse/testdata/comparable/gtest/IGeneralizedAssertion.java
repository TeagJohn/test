package com.dse.testdata.comparable.gtest;

import java.util.*;

public interface IGeneralizedAssertion extends IGTestComparable{

    List assertMethods = new ArrayList(Arrays.asList(
       IGTestAssertMethod.EXPECT_THAT, IGTestAssertMethod.ASSERT_THAT
    ));

    String getGeneralizedAssertion();
}
