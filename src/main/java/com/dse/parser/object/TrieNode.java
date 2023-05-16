package com.dse.parser.object;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrieNode {
    private Map<Character, TrieNode> children = new HashMap<>();
    private boolean isNode = false;
    private INode node = null;

    public TrieNode() {
        this.isNode = false;
        this.node = null;
    }

    public TrieNode(INode node) {
        this.isNode = true;
        this.node = node;
    }

    public Map<Character, TrieNode> getChildren() {
        return children;
    }

    public void setNode(INode node) {
        this.isNode = true;
        this.node = node;
    }

    public void removeNode() {
        this.isNode = false;
        this.node = null;
    }

    public INode getNode() {
        return node;
    }

    public boolean isNode() {
        return isNode;
    }
}
