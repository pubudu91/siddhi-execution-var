package org.wso2.siddhi.extension.var.models;

/**
 * Created by dilini92 on 1/9/17.
 */
public class HistoricalAsset extends Asset {

    private double previousLossReturn;
    private double currentLossReturn;

    public HistoricalAsset(int windowSize) {
        super(windowSize);
    }

    public double getPreviousLossReturn() {
        return previousLossReturn;
    }

    public void setPreviousLossReturn(double previousLossReturn) {
        this.previousLossReturn = previousLossReturn;
    }

    public double getCurrentLossReturn() {
        return currentLossReturn;
    }

    public void setCurrentLossReturn(double currentLossReturn) {
        this.currentLossReturn = currentLossReturn;
    }
}
