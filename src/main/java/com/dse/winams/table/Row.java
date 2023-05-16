package com.dse.winams.table;

import java.util.ArrayList;
import java.util.List;

public class Row implements IEntity {

    private List<Cell> cells;

    public void addCell(Cell c) {
        if (cells == null)
            cells = new ArrayList<>();
        cells.add(c);
    }

    @Override
    public String toCsv() {
        StringBuilder b = new StringBuilder();
        if (cells != null) {
            int size = cells.size();
            for (int i = 0; i < size; i++) {
                b.append(cells.get(i).toCsv());
                if (i != size - 1)
                    b.append(DELIMITER);
            }
        }
        return b.toString();
    }
}
