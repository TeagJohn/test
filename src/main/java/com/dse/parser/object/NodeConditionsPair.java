package com.dse.parser.object;

import com.dse.search.SearchCondition;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

public class NodeConditionsPair {
    private INode node;
    private String conditions;
    private String asString;

    public NodeConditionsPair(INode node, List<SearchCondition> conditions) {
        this.node = node;
        List<String> temp = new ArrayList<>();
        for (SearchCondition condition : conditions) {
            temp.add(condition.getClass().getName());
        }
        Collections.sort(temp);
        this.conditions = String.join(",", temp);

        if (node == null) {
            asString = "NodeConditionsPair: " + "node=null, conditions=" + temp;
        } else {
            asString = "NodeConditionsPair: " + "node=" + node.hashCode() + ", conditions=" + temp;
        }
    }

    public INode getNode() {
        return node;
    }

    public String getConditions() {
        return conditions;
    }

    @Override
    public String toString() {
        return asString;
    }
}
