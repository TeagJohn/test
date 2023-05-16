package com.dse.winams;

import com.dse.parser.object.IVariableNode;

public interface IVariableTreeNode extends IAliasNode {
    IVariableNode getVariable();
    void setVariable(IVariableNode v);
}
