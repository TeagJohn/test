package com.dse.testdata.object.Gmock;

import com.dse.util.SpecialCharacter;

import java.util.ArrayList;
import java.util.List;

public class WithNode extends GMockValueDataNode {
    public WithNode() {}

    public WithNode(String name) {
        super.setName(name);
//        super.setRealType("int");
//        super.setRawType("int");
    }

    @Override
    public String[] getAllSupportedAssertMethod() {
        List<String> supportedMethod = new ArrayList<>();
        supportedMethod.add(SpecialCharacter.EMPTY);
        supportedMethod.add("ALL_OF");
        supportedMethod.add("ANY_OF");
        supportedMethod.add("EXPECT_THAT");
        return supportedMethod.toArray(new String[0]);
    }

}
