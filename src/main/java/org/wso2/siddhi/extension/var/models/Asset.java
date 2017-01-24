package org.wso2.siddhi.extension.var.models;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 * Created by flash on 6/30/16.
 */
public class Asset {
    private double currentStockPrice;
    private DescriptiveStatistics returnValueSet;
    private double priceBeforeLastPrice;

    public Asset(int windowSize) {
        returnValueSet = new DescriptiveStatistics();
        returnValueSet.setWindowSize(windowSize-1);
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

    public Double addReturnValue(double value) {
        Double toBeRemove = null;
        if (getNumberOfReturnValues() == returnValueSet.getWindowSize())
            toBeRemove = returnValueSet.getElement(0);
        returnValueSet.addValue(value);
        return toBeRemove;
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
}
