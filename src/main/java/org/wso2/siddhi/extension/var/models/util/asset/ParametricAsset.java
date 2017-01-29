package org.wso2.siddhi.extension.var.models.util.asset;

/**
 * Created by dilip on 13/01/17.
 */
public class ParametricAsset extends Asset {
    private double[] excessReturns;

    public ParametricAsset(int windowSize) {
        super(windowSize);
        excessReturns = null;
    }

    public double[] getExcessReturns() {
        return excessReturns;
    }

    public void setExcessReturns(double[] excessReturns) {
        this.excessReturns = excessReturns;
    }
}
