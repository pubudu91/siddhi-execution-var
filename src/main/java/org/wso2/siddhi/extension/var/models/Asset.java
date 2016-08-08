package org.wso2.siddhi.extension.var.models;

import java.util.LinkedList;

/**
 * Created by flash on 6/30/16.
 */
public class Asset {
    private LinkedList<Double> historicalValues;
    private double currentStockPrice;
    private String symbol;
    private int numberOfShares;

    public Asset(int numberOfShares){
        this.numberOfShares = numberOfShares;
        historicalValues = new LinkedList<Double>();
    }

    public Asset(String label, int numberOfShares){
        this.numberOfShares = numberOfShares;
        historicalValues = new LinkedList<Double>();
        this.symbol = label;
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

    public void addHistoricalValue(double price){
        historicalValues.add(price);
    }

    public String getSymbol(){ return symbol; }
}
