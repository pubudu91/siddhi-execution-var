package org.wso2.siddhi.extension.var.models;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by dilini92 on 8/3/16.
 */
public class Portfolio {
    private int ID;
    private Map<String, Integer> assets;
    private int previousShares;
    private double historicalVarValue;
    private double MONTECARLO_SIMULATION_currentPortfolioValue;
    private double [] MONTECARLO_SIMULATION_finalPortfolioValueList=null;

    public double[] getMONTECARLO_SIMULATION_finalPortfolioValues() {
        return MONTECARLO_SIMULATION_finalPortfolioValueList;
    }

    public void setMONTECARLO_SIMULATION_finalPortfolioValues(double[] MONTECARLO_SIMULATION_finalPortfolioValues) {
        this.MONTECARLO_SIMULATION_finalPortfolioValueList = MONTECARLO_SIMULATION_finalPortfolioValues;
    }

    public double getMONTECARLO_SIMULATION_currentPortfolioValue() {
        return MONTECARLO_SIMULATION_currentPortfolioValue;
    }

    public void setMONTECARLO_SIMULATION_currentPortfolioValue(double MONTECARLO_SIMULATION_currentPortfolioValue) {
        this.MONTECARLO_SIMULATION_currentPortfolioValue = MONTECARLO_SIMULATION_currentPortfolioValue;
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

    public void addAsset(String symbol, int shares) {
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
