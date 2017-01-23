package org.wso2.siddhi.extension.var.models;

/**
 * Created by dilini92 on 1/9/17.
 */
public class MonteCarloAsset extends Asset {
    public MonteCarloAsset(int windowSize) {
        super(windowSize);
    }

    private double[] simulatedList = null;
    private double[] previousSimulatedList = null;

    public double[] getSimulatedList() {
        return simulatedList;
    }

    public double[] getPreviousSimulatedList() {
        return previousSimulatedList;
    }

    public void setSimulatedList(double[] simulatedList) {
        this.simulatedList = simulatedList;
    }

    public void setPreviousSimulatedList(double[] previousSimulatedList) {
        this.previousSimulatedList = previousSimulatedList;
    }
}
