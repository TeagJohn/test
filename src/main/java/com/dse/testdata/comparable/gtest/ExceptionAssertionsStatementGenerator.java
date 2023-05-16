package com.dse.testdata.comparable.gtest;

import com.dse.testdata.object.ValueDataNode;

public class ExceptionAssertionsStatementGenerator extends GTestStatementGenerator implements IExceptionAssertions {
    public ExceptionAssertionsStatementGenerator(ValueDataNode node) {
        super(node);
    }

    @Override
    public String getExceptionAssertion(String functionCallStm) {
        String[] arrStr = node.getAssertMethod().split(": ", 2);
        if (arrStr[0].equals(IGTestAssertMethod.EXPECT_THROW) || arrStr[0].equals(IGTestAssertMethod.ASSERT_THROW)) {
            return String.format("%s(%s, %s);\n", arrStr[0], functionCallStm, arrStr[1]);
        } else {
            return String.format("%s(%s);\n", node.getAssertMethod(), functionCallStm);
        }
    }
}
