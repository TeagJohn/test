package com.dse.testdata.object.Gmock;

import java.util.ArrayList;
import java.util.List;

public class ArgumentNode extends WithNode {

    public ArgumentNode() {

    }

    public ArgumentNode(String name) {
        super.setName(name);
        super.setRawType("");
        super.setRealType("");
    }

    @Override
    public String[] getAllSupportedAssertMethod() {
        List<String> supportedMethod = new ArrayList<>();
        return supportedMethod.toArray(new String[0]);
    }
}
