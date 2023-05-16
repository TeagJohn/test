package com.dse.winams;

public interface IAliasNode extends ITreeNode {
    String getAlias();
    void setAlias(String alias);
    void setChainNames(String[] chainNames);
    String[] getChainNames();
}
