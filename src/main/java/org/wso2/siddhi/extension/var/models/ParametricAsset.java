package org.wso2.siddhi.extension.var.models;

/**
 * Created by dilip on 13/01/17.
 */
public class ParametricAsset extends Asset {
    private double[] excessReturns;
    private double mean;

    public ParametricAsset(int windowSize) {
        super(windowSize);
        mean = 0;
    }

    public double getMean() {
        return mean;
    }

    public void setMean(double mean) {
        this.mean = mean;
    }

    public double[] getExcessReturns() {
        return excessReturns;
    }

    public void setExcessReturns(double[] excessReturns) {
        this.excessReturns = excessReturns;
    }
}
