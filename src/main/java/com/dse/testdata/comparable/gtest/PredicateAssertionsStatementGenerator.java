package com.dse.testdata.comparable.gtest;

import com.dse.testdata.object.SubprogramNode;
import com.dse.testdata.object.ValueDataNode;

public class PredicateAssertionsStatementGenerator extends GTestStatementGenerator implements IPredicateAssertions {
    public PredicateAssertionsStatementGenerator(ValueDataNode node) {
        super(node);
    }

    @Override
    public String getPredicateAssertion(String functionCallStm) {
        if (node instanceof SubprogramNode) {
            return String.format("%s%s(%s);\n", node.getAssertMethod(), ((SubprogramNode) node).getFunctionNode().getChildren().size(), functionCallStm);
        }
        return "";
    }
}
