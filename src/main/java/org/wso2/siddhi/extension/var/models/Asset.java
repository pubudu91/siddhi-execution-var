package org.wso2.siddhi.extension.var.models;

import java.util.LinkedList;

/**
 * Created by flash on 6/30/16.
 */
public class Asset {
    private LinkedList<Double> historicalValues;    //no need to keep this. only the last price and price before last price is needed
    private double currentStockPrice;
    private double stockPriceBeforeChange;
    private String symbol;
    private int numberOfShares;
    private LinkedList<Double> latestReturnValues;
    private int numberOfHistoricalValues;
    private double[] simulatedList;

    public double[] getSimulatedList() {
        return simulatedList;
    }

    public void setSimulatedList(double[] simulatedList) {
        this.simulatedList = simulatedList;
    }

    public Asset(String symbol) {
        this.symbol = symbol;
        historicalValues = new LinkedList<>();
        latestReturnValues = new LinkedList<>();
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

    public String getSymbol(){ return symbol; }

    public void setLatestReturnValues(LinkedList<Double> latestReturnValues){
        this.latestReturnValues = latestReturnValues;
    }

    public LinkedList<Double> getLatestReturnValues(){
        return latestReturnValues;
    }

    public int getNumberOfHistoricalValues(){
        return historicalValues.size();
    }

    public double getStockPriceBeforeChange(){
        return stockPriceBeforeChange;
    }

    public void setStockPriceBeforeChange(double priceBeforeLastPrice){
        this.stockPriceBeforeChange = priceBeforeLastPrice;
    }

    public double getCurrentStockPrice(){
        return currentStockPrice;
    }

    public void setCurrentStockPrice(double currentStockPrice){
        this.currentStockPrice = currentStockPrice;
    }

    public void addReturnValue(double value){
        latestReturnValues.add(value);
    }

}
