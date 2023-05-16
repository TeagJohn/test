package com.dse.testdata.object;

import com.dse.parser.object.ICommonFunctionNode;
import com.dse.parser.object.VariableNode;

import java.util.List;

public interface ISubStructOrClassDataNode extends IDataNode{
    ConstructorDataNode getConstructorDataNode();

    List<ICommonFunctionNode> getConstructorsOnlyInCurrentClass();

    List<IDataNode> getAttributes();

    void chooseConstructor(ICommonFunctionNode constructor) throws Exception;

    void setName(String name);

    void setRawType(String rawType);

    void setRealType(String realType);

    void setCorrespondingVar(VariableNode correspondingVar);

    String getRawType();
}
