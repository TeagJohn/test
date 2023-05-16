package com.dse.testdata.comparable.gtest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public interface IPredicateAssertions extends IGTestComparable{
    List assertMethods = new ArrayList(Arrays.asList(
            IGTestAssertMethod.EXPECT_PRED, IGTestAssertMethod.ASSERT_PRED
    ));

    String getPredicateAssertion(String functionCallStm);
}
