package com.dse.testdata.object.Gmock;

import com.dse.util.SpecialCharacter;

import java.util.ArrayList;
import java.util.List;

public class MatcherMultipleNode extends WithNode {
    private ArgumentNode first = new ArgumentNode("first");
    private ArgumentNode second = new ArgumentNode("second");

    public MatcherMultipleNode() {}

    public MatcherMultipleNode(String name) {
        super(name);
        this.addChild(first);
        first.setParent(this);
        this.addChild(second);
        second.setParent(this);
        value = null;
        setRawType("");
        setRealType("");
    }

    @Override
    public String[] getAllSupportedAssertMethod() {
        List<String> supportedMethod = new ArrayList<>();
        supportedMethod.add(SpecialCharacter.EMPTY);
        supportedMethod.add("EXPECT_EQ");
        supportedMethod.add("EXPECT_GT");
        supportedMethod.add("EXPECT_GE");
        supportedMethod.add("EXPECT_LT");
        supportedMethod.add("EXPECT_LE");
        supportedMethod.add("EXPECT_NE");
        return supportedMethod.toArray(new String[0]);
    }

    public ArgumentNode getFirst() {
        return first;
    }

    public void setFirst(ArgumentNode first) {
        this.first = first;
    }

    public ArgumentNode getSecond() {
        return second;
    }

    public void setSecond(ArgumentNode second) {
        this.second = second;
    }
}
