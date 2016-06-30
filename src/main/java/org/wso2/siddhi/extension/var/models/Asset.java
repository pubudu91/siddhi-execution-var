package org.wso2.siddhi.extension.var.models;

/**
 * Created by flash on 6/30/16.
 */
public class Asset {
    private double historicalValue[];
    private double currentStockPrice;
    private String label;
    private int numberOfShares;

    public Asset(double[] historicalValue, double currentStockPrice, String label, int numberOfShares) {
        this.historicalValue = historicalValue;
        this.currentStockPrice = currentStockPrice;
        this.label = label;
        this.numberOfShares = numberOfShares;
    }

    public double[] getHistoricalValue() {
        return historicalValue;
    }

    public void setHistoricalValue(double[] historicalValue) {
        this.historicalValue = historicalValue;
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
}
