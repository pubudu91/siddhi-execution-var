package org.wso2.siddhi.extension.var.models;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by dilini92 on 1/9/17.
 */
public class MonteCarloPortfolio extends Portfolio {
    private Map<String, Integer> assetSharesBeforeChange = null;

    private double monteCarlo_Simulation_currentPortfolioValue;
    private double[] monteCarlo_Simulation_finalPortfolioValueList = null;

    public MonteCarloPortfolio(String ID, Map<String, Integer> assets) {
        super(ID, assets);
        this.assetSharesBeforeChange = new HashMap<>();
    }

    public double[] getMonteCarlo_Simulation_finalPortfolioValueList() {
        return monteCarlo_Simulation_finalPortfolioValueList;
    }

    public void setMonteCarlo_Simulation_finalPortfolioValueList(double[] monteCarlo_Simulation_finalPortfolioValueList) {
        this.monteCarlo_Simulation_finalPortfolioValueList = monteCarlo_Simulation_finalPortfolioValueList;
    }

    public double getMonteCarlo_Simulation_currentPortfolioValue() {
        return monteCarlo_Simulation_currentPortfolioValue;
    }

    public void setMonteCarlo_Simulation_currentPortfolioValue(double monteCarlo_Simulation_currentPortfolioValue) {
        this.monteCarlo_Simulation_currentPortfolioValue = monteCarlo_Simulation_currentPortfolioValue;
    }

    public Map<String, Integer> getAssetSharesBeforeChange() {
        return this.assetSharesBeforeChange;
    }

    public void setAssetSharesBeforeChange(Map<String, Integer> assetSharesBeforeChange) {
        this.assetSharesBeforeChange = assetSharesBeforeChange;
    }
}
