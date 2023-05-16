package com.dse.winams.UI.object;

import com.dse.parser.object.IFunctionNode;

public class FunctionComboBoxItem {
    private IFunctionNode functionNode;

    public FunctionComboBoxItem(IFunctionNode functionNode) {
        this.functionNode = functionNode;
    }

    @Override
    public String toString() {
        return functionNode.getSimpleName();
    }

    public String getSimpleName() {
        return functionNode.getSimpleName();
    }

    public IFunctionNode getFunctionNode() {
        return functionNode;
    }
}
