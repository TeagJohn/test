package com.dse.winams;

public abstract class AliasNode extends TreeNode implements IAliasNode {

    private String alias;

    private String[] chainNames;

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getAlias() {
        return alias;
    }

    public void setChainNames(String[] chainNames) {
        this.chainNames = chainNames;
    }

    public String[] getChainNames() {
        return chainNames;
    }
}
