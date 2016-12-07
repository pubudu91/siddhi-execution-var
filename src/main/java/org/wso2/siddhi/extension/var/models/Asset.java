package org.wso2.siddhi.extension.var.models;

import java.util.LinkedList;

/**
 * Created by flash on 6/30/16.
 */
public class Asset {
    private LinkedList<Double> historicalValues;
    private double stockPriceBeforeChange;
    private String symbol;
    private int numberOfShares;

    public double getStockPriceBeforeChange() {
        return stockPriceBeforeChange;
    }

    public void setStockPriceBeforeChange(double stockPriceBeforeChange) {
        this.stockPriceBeforeChange = stockPriceBeforeChange;
    }

    public Asset(String symbol) {
        this.symbol = symbol;
        historicalValues = new LinkedList<>();
    }

    public LinkedList<Double> getHistoricalValues() {
        return historicalValues;
    }

    public int getNumberOfShares() {
        return numberOfShares;
    }

    public void setNumberOfShares(int numberOfShares) {
        this.numberOfShares = numberOfShares;
    }

    public void addHistoricalValue(double price) {
        historicalValues.add(price);
    }

    public String getSymbol() {
        return symbol;
    }
}
