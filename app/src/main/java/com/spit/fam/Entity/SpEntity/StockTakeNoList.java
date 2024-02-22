package com.spit.fam.Entity.SpEntity;

import java.util.ArrayList;

public class StockTakeNoList {
    public ArrayList<StockTakeNoListItem> getTable() {
        return Table;
    }

    public void setTable(ArrayList<StockTakeNoListItem> table) {
        Table = table;
    }

    private ArrayList<StockTakeNoListItem> Table = new ArrayList<>();
}
