package com.dse.testdata.comparable.gtest;

import com.dse.search.Search2;
import com.dse.testdata.object.ValueDataNode;
import com.dse.util.SpecialCharacter;

public class FloatingPointComparisonStatementGenerator extends BinaryComparisonStatementGenerator implements IFloatingPointComparison {
    public FloatingPointComparisonStatementGenerator(ValueDataNode node) {
        super(node);
    }


    @Override
    public String getFloatingPointComparisonAssertion() {
        String expectedName = Search2.getExpectedValue(node).getVituralName();
        String actualName = node.getActualName();

        String assertMethod = node.getAssertMethod();
        String delta = assertMethod.split(" " + SpecialCharacter.DELTA + " ")[1];
        assertMethod = assertMethod.split(" " + SpecialCharacter.DELTA + " ")[0];
        return String.format("%s(%s, %s, %s);\n", assertMethod, actualName, expectedName, delta);
    }
}
