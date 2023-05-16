package com.dse.testdata.object.Gmock;

import com.dse.testdata.comparable.AssertMethod;
import com.dse.testdata.object.ValueDataNode;
import com.dse.util.SpecialCharacter;

import java.util.ArrayList;
import java.util.List;

public abstract class GMockValueDataNode extends ValueDataNode {
    protected String value;

    public GMockValueDataNode() {}

    public GMockValueDataNode(String name) {
        super.setName(name);
        //super.setRealType("int");
        //super.setRawType("int");
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean haveValue() {
        return value != null && !value.isEmpty();
    }

    public void setValue(int value) {
        this.value = value + "";
    }

    @Override
    public String[] getAllSupportedAssertMethod() {
        List<String> supportedMethod = new ArrayList<>();
//        supportedMethod.add(SpecialCharacter.EMPTY);
//        supportedMethod.add("EXPECT_EQ");
        return supportedMethod.toArray(new String[0]);
    }
}
