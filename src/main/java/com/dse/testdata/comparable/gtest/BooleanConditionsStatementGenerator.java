package com.dse.testdata.comparable.gtest;

import com.dse.testdata.object.ValueDataNode;

public class BooleanConditionsStatementGenerator extends GTestStatementGenerator implements IBooleanConditions {
    public BooleanConditionsStatementGenerator(ValueDataNode node) {
        super(node);
    }

    @Override
    public String getBooleanConditionAssertion() {
        String assertMethod = node.getAssertMethod();
        String actualName = node.getActualName();
        return String.format("%s(%s);\n", assertMethod, actualName);
    }
}
