package com.dse.testdata.comparable.gtest;

import com.dse.search.Search2;
import com.dse.testdata.object.IValueDataNode;
import com.dse.testdata.object.ValueDataNode;

public class BinaryComparisonStatementGenerator extends GTestStatementGenerator implements IBinaryComparison, IStringComparison {
    public BinaryComparisonStatementGenerator(ValueDataNode node) {
        super(node);
    }


    @Override
    public String getBinaryComparisonAssertion() {
        IValueDataNode expectedNode = Search2.getExpectedValue(node);
        if (expectedNode == null) {
            expectedNode = node;
        }
        String expectedName = expectedNode.getVituralName();
        String actualName = node.getActualName();

        String assertMethod = node.getAssertMethod();
        return String.format("%s(%s, %s);\n", assertMethod, actualName, expectedName);
    }
}
