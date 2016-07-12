package org.wso2.siddhi.extension.var.backtest;

import org.wso2.siddhi.extension.var.models.Asset;
import org.wso2.siddhi.extension.var.realtime.VaRPortfolioCalc;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by flash on 7/12/16.
 */
public class TestVarModel {
    private double var[];
    private double actualValue[];
    private int sampleSize = 250;
    private VaRPortfolioCalc calcInstance;

    public TestVarModel(VaRPortfolioCalc calcInstance) {
        this.calcInstance = calcInstance;
    }

    public void generateVar() {
        Map<String, Asset> assets = new HashMap<>();
        int limit = 250;
        double confidenceInterval = 0.95;
        Object[] input = null;

        calcInstance.calculateValueAtRisk(input);
    }

    public void generateOriginalLoss() {

    }


}
