package com.dse.testdata.comparable.gtest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public interface IStringComparison extends IBinaryComparison {

    List assertMethods = new ArrayList(Arrays.asList(
            IGTestAssertMethod.EXPECT_STREQ, IGTestAssertMethod.ASSERT_STREQ,
            IGTestAssertMethod.EXPECT_STRNE, IGTestAssertMethod.ASSERT_STRNE,
            IGTestAssertMethod.EXPECT_STRCASEEQ, IGTestAssertMethod.ASSERT_STRCASEEQ,
            IGTestAssertMethod.EXPECT_STRCASENE, IGTestAssertMethod.ASSERT_STRCASENE));

    String getBinaryComparisonAssertion();
}
