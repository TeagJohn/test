package com.dse.winams;

import com.dse.parser.object.ICommonFunctionNode;
import com.dse.testdata.object.IDataNode;

public class FunctionTreeNode extends AliasNode implements IFunctionTreeNode {

    private ICommonFunctionNode function;

    public FunctionTreeNode(ICommonFunctionNode function, IDataNode dataNode) {
        this.function = function;
        this.dataNode = dataNode;
    }

    @Override
    public ICommonFunctionNode getFunction() {
        return function;
    }

    @Override
    public void setFunction(ICommonFunctionNode function) {
        this.function = function;
    }
    public ITreeNode clone() {
        FunctionTreeNode treeNode = new FunctionTreeNode(function, dataNode);
        treeNode.setTitle(title);
        treeNode.setType(type);
        return treeNode;
    }
}
