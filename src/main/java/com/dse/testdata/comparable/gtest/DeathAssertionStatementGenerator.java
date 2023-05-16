package com.dse.testdata.comparable.gtest;

import com.dse.testdata.object.ValueDataNode;

public class DeathAssertionStatementGenerator extends GTestStatementGenerator implements IDeathAssertions {
    public DeathAssertionStatementGenerator(ValueDataNode node) {
        super(node);
    }

    @Override
    public String getDeathAssertion(String functionCallStm) {
        String[] arrStr = node.getAssertMethod().split(": ", 2);
        if (arrStr[0].equals(IGTestAssertMethod.EXPECT_EXIT) || arrStr[0].equals(IGTestAssertMethod.ASSERT_EXIT)) {
            return String.format("%s(%s, %s, %s);\n", arrStr[0], functionCallStm, arrStr[1].split(": ", 2)[0], arrStr[1].split(": ", 2)[1]);
        } else {
            return String.format("%s(%s, %s);\n", arrStr[0], functionCallStm, arrStr[1]);
        }
    }
}
