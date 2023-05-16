package com.dse.winams.converter;

import com.dse.util.NodeType;
import com.dse.winams.IEntry;
import com.dse.winams.IInputEntry;
import com.dse.winams.IOutputEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SimpleTreeBuilder {

    private final Node root = new Node(NodeType.ROOT.name());

    public Node getRoot() {
        return root;
    }

    public void append(IEntry e) {
        String[] chainNames = e.getChainNames();
        chainNames = Arrays.copyOfRange(chainNames, 1, chainNames.length);
        Node node = findOrCreate(root, chainNames);
        if (node != null) {
            if (e instanceof IInputEntry)
                node.setInput(true);
            if (e instanceof IOutputEntry)
                node.setOutput(true);
            node.setAlias(e.getAlias());
        }
    }

    private Node findOrCreate(Node parent, String[] chain) {
        if (parent != null && chain.length > 0) {
            Node child = parent.findChild(chain[0]);
            if (child == null) {
                child = new Node(chain[0]);
                parent.addChild(child);
            }

            return findOrCreate(child, Arrays.copyOfRange(chain, 1, chain.length));
        } else if (chain.length == 0)
            return parent;
        else
            return null;

    }

    public static class Node {

        private final String name;

        private String alias;

        private boolean isInput, isOutput;

        private List<Node> children;

        public Node(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void addChild(Node node) {
            if (children == null)
                children = new ArrayList<>();

            children.add(node);
        }

        public void setAlias(String alias) {
            this.alias = alias;
        }

        public String getAlias() {
            return alias;
        }

        public Node findChild(String name) {
            if (children != null) {
                return children.stream().filter(n -> n.name.equals(name)).findFirst().orElse(null);
            } else {
                return null;
            }
        }

        @Deprecated
        public Node getChild(int index) {
            if (children != null) {
                return children.get(index);
            } else {
                return null;
            }
        }

        public void setInput(boolean input) {
            isInput = input;
        }

        public void setOutput(boolean output) {
            isOutput = output;
        }

        public boolean isInput() {
            return isInput;
        }

        public boolean isOutput() {
            return isOutput;
        }
    }
}
