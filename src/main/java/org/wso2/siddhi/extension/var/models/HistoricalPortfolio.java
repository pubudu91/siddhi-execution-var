package org.wso2.siddhi.extension.var.models;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by dilini92 on 1/9/17.
 */
public class HistoricalPortfolio extends Portfolio{
    private double historicalVarValue;
    private Map<String, Integer> previousShares;

    public HistoricalPortfolio(int ID, Map<String, Integer> assets){
        super(ID, assets);
        previousShares = new HashMap<>();
    }
    public double getHistoricalVarValue() {
        return historicalVarValue;
    }

    public void setHistoricalVarValue(double historicalVarValue) {
        this.historicalVarValue = historicalVarValue;
    }

    public void setPreviousShare(String symbol, int shares){
        previousShares.put(symbol, shares);
    }

    public int getPreviousShare(String symbol){
        if(previousShares.get(symbol) != null)
            return previousShares.get(symbol);
        return 0;
    }
}
