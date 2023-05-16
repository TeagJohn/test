package com.dse.winams;

public interface IEntry {
    String getTitle();
    String getAlias();
    String getName();
    Type getType();
    String[] getChainNames();

    enum Type {
        GLOBAL,
        STATIC,
        FUNCTION_CALL,
        PARAMETER,
        RETURN
    }
}
