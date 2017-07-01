package org.wso2.siddhi.extension.var.models.util.portfolio;

import java.util.Map;

/**
 * Created by dilini92 on 1/9/17.
 */
public class MonteCarloPortfolio extends Portfolio {

    private double previousPortfolioValue;
    private double[] finalPortfolioValueList = null;

    public MonteCarloPortfolio(String ID, Map<String, Integer> assets) {
        super(ID, assets);
    }

    public double[] getFinalPortfolioValueList() {
        return finalPortfolioValueList;
    }

    public void setFinalPortfolioValueList(double[] finalPortfolioValueList) {
        this.finalPortfolioValueList = finalPortfolioValueList;
    }

    public double getPreviousPortfolioValue() {
        return previousPortfolioValue;
    }

    public void setPreviousPortfolioValue(double previousPortfolioValue) {
        this.previousPortfolioValue = previousPortfolioValue;
    }

}
