package com.dse.winams.table;

import java.util.ArrayList;
import java.util.List;

public class Table implements IEntity {

    private List<Row> rows;

    public void addRow(Row r) {
        if (rows == null)
            rows = new ArrayList<>();
        rows.add(r);
    }

    @Override
    public String toCsv() {
        StringBuilder b = new StringBuilder();
        if (rows != null) {
            int size = rows.size();
            for (int i = 0; i < size; i++) {
                b.append(rows.get(i).toCsv());
                if (i != size - 1)
                    b.append("\n");
            }
        }
        return b.toString();
    }
}
