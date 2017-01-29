package org.wso2.siddhi.extension.var.models.util.portfolio;

import java.util.Map;

/**
 * Created by dilini92 on 1/9/17.
 */
public class HistoricalPortfolio extends Portfolio {
    private double historicalVarValue;

    public HistoricalPortfolio(String ID, Map<String, Integer> assets) {
        super(ID, assets);
    }

    public double getHistoricalVarValue() {
        return historicalVarValue;
    }

    public void setHistoricalVarValue(double historicalVarValue) {
        this.historicalVarValue = historicalVarValue;
    }


}
