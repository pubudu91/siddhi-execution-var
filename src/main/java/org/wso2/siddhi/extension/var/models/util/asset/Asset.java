package org.wso2.siddhi.extension.var.models.util.asset;

import org.wso2.siddhi.extension.var.models.util.CustomDescriptiveStatistics;

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

    public double[] getReturnValues() {
        return returnValueSet.getValues();
    }

    public void addReturnValue(double value) {
        returnValueSet.addValue(value);
    }

    public double getPreviousStockPrice() {
        return priceBeforeLastPrice;
    }

    public void setPreviousPrice(double priceBeforeLastPrice) {
        this.priceBeforeLastPrice = priceBeforeLastPrice;
    }

    public int getNumberOfReturnValues() {
        return (int) returnValueSet.getN();
    }

    public double getPercentile(double percentile) {
        return returnValueSet.getPercentile(percentile);
    }

    public double getMean() {
        return returnValueSet.getMean();
    }

    public double getStandardDeviation() {
        return returnValueSet.getStandardDeviation();
    }
}
