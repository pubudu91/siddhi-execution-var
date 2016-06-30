package org.wso2.siddhi.extension.var.models;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by flash on 6/30/16.
 */
public class Asset {
    private LinkedList<Double> historicalValues;
    private double currentStockPrice;
    private String label;
    private int numberOfShares;

    public Asset(String label, int numberOfShares){
        this.label = label;
        this.numberOfShares = numberOfShares;
        historicalValues = new LinkedList<Double>();
    }

    public LinkedList<Double> getHistoricalValues() {
        return historicalValues;
    }

    public double getCurrentStockPrice() {
        return currentStockPrice;
    }

    public void setCurrentStockPrice(double currentStockPrice) {
        this.currentStockPrice = currentStockPrice;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
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
}
