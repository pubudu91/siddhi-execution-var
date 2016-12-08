package org.wso2.siddhi.extension.var.models;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by flash on 6/30/16.
 */
public class Asset {
    private double currentStockPrice;
    private String symbol;
    private ArrayList<Double> latestReturnValues;
    private double[] simulatedList;

    public double[] getSimulatedList() {
        return simulatedList;
    }

    public void setSimulatedList(double[] simulatedList) {
        this.simulatedList = simulatedList;
    }

    public Asset(String symbol) {
        this.symbol = symbol;
        latestReturnValues = new ArrayList<>();
    }

    public String getSymbol(){ return symbol; }

    public void setLatestReturnValues(ArrayList<Double> latestReturnValues){
        this.latestReturnValues = latestReturnValues;
    }

    public void addReturnValue(double value){
        latestReturnValues.add(value);
    }

    public ArrayList<Double> getLatestReturnValues(){
        return latestReturnValues;
    }

    public int getNumberOfHistoricalValues(){
        return latestReturnValues.size() + 1;
    }

    public double getCurrentStockPrice(){
        return currentStockPrice;
    }

    public void setCurrentStockPrice(double currentStockPrice){
        this.currentStockPrice = currentStockPrice;
    }
}
