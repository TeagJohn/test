package com.dse.winams;

import com.dse.parser.object.IVariableNode;
import com.dse.testdata.object.IDataNode;

public class VariableTreeNode extends AliasNode implements IVariableTreeNode {
    private IVariableNode variable;

    public VariableTreeNode(IVariableNode v, IDataNode dataNode) {
        this.variable = v.clone();
        this.dataNode = dataNode;
    }

    @Override
    public IVariableNode getVariable() {
        return variable;
    }

    @Override
    public void setVariable(IVariableNode variable) {
        this.variable = variable;
    }
    public ITreeNode clone() {
        VariableTreeNode treeNode = new VariableTreeNode(variable, dataNode);
        treeNode.setTitle(title);
        treeNode.setType(type);
        return treeNode;
    }
}
