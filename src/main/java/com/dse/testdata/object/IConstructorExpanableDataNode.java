package com.dse.testdata.object;

import com.dse.parser.object.ICommonFunctionNode;

import java.util.ArrayList;

public interface IConstructorExpanableDataNode {
    ICommonFunctionNode getSelectedConstructor();

    void chooseConstructor(String constructor) throws Exception;

    void chooseConstructor() throws Exception;
}
