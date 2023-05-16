package com.dse.winams;

import com.dse.testdata.object.IDataNode;

import java.util.ArrayList;
import java.util.List;

public abstract class TreeNode implements ITreeNode {
    protected IDataNode dataNode;
    protected String title;
    protected IEntry.Type type = null;

    private final List<ITreeNode> children = new ArrayList<>();

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    public void setType(IEntry.Type type) {
        this.type = type;
    }

    public IEntry.Type getType() {
        return type;
    }

    public IDataNode getDataNode() {
        return dataNode;
    }

    @Override
    public List<ITreeNode> getChildren() {
        return children;
    }

    public ITreeNode clone() {
        TreeNode treeNode = new TreeNode() {
        };
        treeNode.setTitle(title);
        treeNode.setType(type);
        return treeNode;
    }

    @Override
    public String toString() {
        return getTitle();
    }
}
