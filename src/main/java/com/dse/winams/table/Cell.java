package com.dse.winams.table;

public class Cell implements IEntity {

    private String text;

    @Override
    public String toCsv() {
        return text == null ? "" : text;
    }
}
