package com.dse.testdata.comparable.gtest;

import com.dse.testdata.object.ValueDataNode;
import com.dse.util.SpecialCharacter;

public class GeneralizedAssertionStatementGenerator extends GTestStatementGenerator implements IGeneralizedAssertion {
    public GeneralizedAssertionStatementGenerator(ValueDataNode node) {
        super(node);
    }


    @Override
    public String getGeneralizedAssertion() {
        String[] assertion = node.getAssertMethod().split(": ", 2);
        String matcher = assertion[1];
        String assertMethod = assertion[0];
        String actualName = node.getActualName();
        return String.format("%s(%s, %s);\n", assertMethod, actualName, matcher);
    }
}
