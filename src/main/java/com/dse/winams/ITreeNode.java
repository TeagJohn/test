package com.dse.winams;

import java.util.List;

public interface ITreeNode {
    String getTitle();
    void setTitle(String title);
    void setType(IEntry.Type type);
    IEntry.Type getType();
    ITreeNode clone();
    List<ITreeNode> getChildren();
}
