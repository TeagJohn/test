package com.dse.winams;

import com.dse.parser.object.ICommonFunctionNode;

public interface IFunctionTreeNode extends IAliasNode {
    ICommonFunctionNode getFunction();
    void setFunction(ICommonFunctionNode v);
}
