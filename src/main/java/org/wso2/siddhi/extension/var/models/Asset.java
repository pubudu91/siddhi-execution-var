package org.wso2.siddhi.extension.var.models;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.wso2.siddhi.extension.var.realtime.util.CustomDescriptiveStatistics;

/**
 * Created by flash on 6/30/16.
 */
public class Asset {
    private double currentStockPrice;
    private CustomDescriptiveStatistics returnValueSet;
    private double priceBeforeLastPrice;

    public Asset(int windowSize) {
        returnValueSet = new CustomDescriptiveStatistics();
        returnValueSet.setWindowSize(windowSize - 1);
    }

    public double getCurrentStockPrice() {
        return currentStockPrice;
    }

    public void setCurrentStockPrice(double currentStockPrice) {
        this.currentStockPrice = currentStockPrice;
    }

    public DescriptiveStatistics getReturnValueSet() {
        return returnValueSet;
    }

    public void addReturnValue(double value) {
        returnValueSet.addValue(value);
    }

    public double getPriceBeforeLastPrice() {
        return priceBeforeLastPrice;
    }

    public void setPriceBeforeLastPrice(double priceBeforeLastPrice) {
        this.priceBeforeLastPrice = priceBeforeLastPrice;
    }

    public int getNumberOfReturnValues() {
        return returnValueSet.getValues().length;
    }

    public double getMean() {
        return returnValueSet.getMean();
    }
}
