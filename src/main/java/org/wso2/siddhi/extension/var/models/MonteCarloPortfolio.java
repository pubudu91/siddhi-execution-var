package org.wso2.siddhi.extension.var.models;

import java.util.Map;

/**
 * Created by dilini92 on 1/9/17.
 */
public class MonteCarloPortfolio extends Portfolio {
    public MonteCarloPortfolio(int ID, Map<String, Integer> assets){
        super(ID, assets);
    }

    private double monteCarlo_Simulation_currentPortfolioValue;
    private double [] monteCarlo_Simulation_finalPortfolioValueList =null;

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

}
