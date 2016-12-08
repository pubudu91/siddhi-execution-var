package org.wso2.siddhi.extension.var.models;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Created by flash on 6/30/16.
 */
public class Asset {
    private double currentStockPrice;
    private double priceBeforeLastPrice;
    private String symbol;
    private LinkedList<Double> latestReturnValues;
    private int numberOfHistoricalValues;
    private double[] simulatedList_montecarlo;

    public double[] getSimulatedList_montecarlo() {
        return simulatedList_montecarlo;
    }

    public void setSimulatedList_montecarlo(double[] simulatedList_montecarlo) {
        this.simulatedList_montecarlo = simulatedList_montecarlo;
    }

    public Asset(String symbol) {
        this.symbol = symbol;
        latestReturnValues = new LinkedList<>();
    }

    public String getSymbol(){ return symbol; }

    public void setLatestReturnValues(LinkedList<Double> latestReturnValues){
        this.latestReturnValues = latestReturnValues;
    }

    public LinkedList<Double> getLatestReturnValues(){
        return latestReturnValues;
    }

    public int getNumberOfHistoricalValues(){
        return latestReturnValues.size() + 1;
    }

    public double getPriceBeforeLastPrice(){
        return priceBeforeLastPrice;
    }

    public void setPriceBeforeLastPrice(double priceBeforeLastPrice){
        this.priceBeforeLastPrice = priceBeforeLastPrice;
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
