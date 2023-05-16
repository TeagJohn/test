package com.dse.testdata.comparable.gtest;

import com.dse.parser.object.IFunctionPointerTypeNode;
import com.dse.testcase_execution.DriverConstant;
import com.dse.testdata.object.FunctionPointerDataNode;
import com.dse.testdata.object.ValueDataNode;

public abstract class GTestStatementGenerator {

    protected final ValueDataNode node;

    public GTestStatementGenerator(ValueDataNode node) {
        this.node = node;
    }

    protected String getVirtualName() {
        if (node instanceof FunctionPointerDataNode && node.getName().isEmpty()) {
            IFunctionPointerTypeNode typeNode = ((FunctionPointerDataNode) node).getCorrespondingType();
            String functionName = typeNode.getFunctionName();
            return node.getVituralName() + functionName;
        }
        return node.getVituralName();
    }

    protected String getRealType() {
        return node.getRealType();
    }

    protected String getRawType() {
        return node.getRawType();
    }

}
