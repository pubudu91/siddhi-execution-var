package org.wso2.siddhi.extension.var.models.util.asset;

/**
 * Created by dilini92 on 1/9/17.
 */
public class HistoricalAsset extends Asset {

    private double previousLossReturn;
    private double currentLossReturn;

    public HistoricalAsset(int windowSize) {
        super(windowSize);
    }

    public double getPreviousReturnValue() {
        return previousLossReturn;
    }

    public void setPreviousReturnValue(double previousLossReturn) {
        this.previousLossReturn = previousLossReturn;
    }

    public double getCurrentReturnValue() {
        return currentLossReturn;
    }

    public void setCurrentReturnValue(double currentLossReturn) {
        this.currentLossReturn = currentLossReturn;
    }
}
