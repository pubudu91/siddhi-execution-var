package org.wso2.siddhi.extension.var.models;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by dilini92 on 8/3/16.
 */
public class Portfolio {
    private int ID;
    private Map<String, Integer> assets;
    private String incomingEventLabel = null;   //this is not required. we have "symbol" in VarPortfolioCalc. remove this.
    private int previousShares;
    private double historicalVarValue;

    public String getIncomingEventLabel() {
        return incomingEventLabel;
    }

    public void setIncomingEventLabel(String incomingEventLabel) {
        this.incomingEventLabel = incomingEventLabel;
    }

    public Portfolio() {
        assets = new HashMap<>();
    }

    public Portfolio(int ID, Map<String, Integer> assets) {
        this.ID = ID;
        this.assets = assets;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public Map<String, Integer> getAssets() {
        return assets;
    }

    public void setAssets(Map<String, Integer> assets) {
        this.assets = assets;
    }

    public void addAsset(String symbol, int shares){
        assets.put(symbol, shares);
    }

    public int getPreviousShares() {
        return previousShares;
    }

    public void setPreviousShares(int previousShares) {
        this.previousShares = previousShares;
    }

    public double getHistoricalVarValue() {
        return historicalVarValue;
    }

    public void setHistoricalVarValue(double historicalVarValue) {
        this.historicalVarValue = historicalVarValue;
    }
}
