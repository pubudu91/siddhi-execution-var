package org.wso2.siddhi.extension.var.models;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by dilini92 on 1/9/17.
 */
public class MonteCarloPortfolio extends Portfolio {
    private Map<String, Integer> shareCountMapBeforePortfolioUpdate = null;

    private double monteCarloSimulationCurrentPortfolioValue;
    private double[] monteCarloSimulationFinalPortfolioValueList = null;

    public MonteCarloPortfolio(String ID, Map<String, Integer> assets) {
        super(ID, assets);
        this.shareCountMapBeforePortfolioUpdate = new HashMap<>();
    }

    public double[] getMonteCarloSimulationFinalPortfolioValueList() {
        return monteCarloSimulationFinalPortfolioValueList;
    }

    public void setMonteCarloSimulationFinalPortfolioValueList(double[] monteCarloSimulationFinalPortfolioValueList) {
        this.monteCarloSimulationFinalPortfolioValueList = monteCarloSimulationFinalPortfolioValueList;
    }

    public double getMonteCarloSimulationCurrentPortfolioValue() {
        return monteCarloSimulationCurrentPortfolioValue;
    }

    public void setMonteCarloSimulationCurrentPortfolioValue(double monteCarloSimulationCurrentPortfolioValue) {
        this.monteCarloSimulationCurrentPortfolioValue = monteCarloSimulationCurrentPortfolioValue;
    }

    public Map<String, Integer> getShareCountMapBeforePortfolioUpdate() {
        return this.shareCountMapBeforePortfolioUpdate;
    }

    public void setShareCountMapBeforePortfolioUpdate(Map<String, Integer> shareCountMapBeforePortfolioUpdate) {
        this.shareCountMapBeforePortfolioUpdate = shareCountMapBeforePortfolioUpdate;
    }
}
