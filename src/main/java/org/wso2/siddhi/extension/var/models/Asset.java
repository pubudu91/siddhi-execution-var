package org.wso2.siddhi.extension.var.models;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by flash on 6/30/16.
 */
public class Asset {
    private double currentStockPrice;
    private ArrayList<Double> latestReturnValues;   //check with others if this can be replaced with stat object
    private double[] simulatedList;
    private DescriptiveStatistics returnValueSet;
    private double previousLossReturn;
    private double priceBeforeLastPrice;

    public double[] getSimulatedList() {
        return simulatedList;
    }

    public void setSimulatedList(double[] simulatedList) {
        this.simulatedList = simulatedList;
    }

    public Asset() {
        latestReturnValues = new ArrayList<>();
    }

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

    public DescriptiveStatistics getReturnValueSet() {
        return returnValueSet;
    }

    public void setReturnValueSet(int batchSize) {
        returnValueSet = new DescriptiveStatistics();
        returnValueSet.setWindowSize(batchSize);
    }

    public double getPreviousLossReturn() {
        return previousLossReturn;
    }

    public void setPreviousLossReturn(double previousLossReturn) {
        this.previousLossReturn = previousLossReturn;
    }

    public double getPriceBeforeLastPrice() {
        return priceBeforeLastPrice;
    }

    public void setPriceBeforeLastPrice(double priceBeforeLastPrice) {
        this.priceBeforeLastPrice = priceBeforeLastPrice;
    }
}
