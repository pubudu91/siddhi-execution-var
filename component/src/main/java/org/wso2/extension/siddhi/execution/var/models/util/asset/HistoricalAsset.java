package org.wso2.siddhi.extension.var.models.util.asset;

/**
 * Created by dilini92 on 1/9/17.
 */
public class HistoricalAsset extends Asset {
    private double[] previousSimulatedPriceList;
    private double[] currentSimulatedPriceList;

    public HistoricalAsset(int windowSize) {
        super(windowSize);
    }

    public double[] getPreviousSimulatedPriceList() {
        return previousSimulatedPriceList;
    }

    public double[] getCurrentSimulatedPriceList() {
        return currentSimulatedPriceList;
    }

    public void setPreviousSimulatedPriceList(double[] previousSimulatedPriceList) {
        this.previousSimulatedPriceList = previousSimulatedPriceList;
    }

    public void setCurrentSimulatedPriceList(double[] currentSimulatedPriceList) {
        this.currentSimulatedPriceList = currentSimulatedPriceList;
    }
}
