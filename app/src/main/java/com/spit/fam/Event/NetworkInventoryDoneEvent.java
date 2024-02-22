package com.spit.fam.Event;

public class NetworkInventoryDoneEvent {
    public NetworkInventoryDoneEvent() {
    }

    public NetworkInventoryDoneEvent(String stocktakeno) {
        setStocktakeno(stocktakeno);
    }

    public NetworkInventoryDoneEvent(String name, String stocktakeno) {
        setStocktakeno(stocktakeno);
        setName(name);
    }

    public String getStocktakeno() {
        return stocktakeno;
    }

    public void setStocktakeno(String stocktakeno) {
        this.stocktakeno = stocktakeno;
    }

    private String stocktakeno;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private String name;
}
