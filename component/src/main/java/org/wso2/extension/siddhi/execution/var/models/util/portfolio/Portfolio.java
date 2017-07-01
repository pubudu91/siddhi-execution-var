package org.wso2.extension.siddhi.execution.var.models.util.portfolio;

import org.wso2.extension.siddhi.execution.var.models.util.Event;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by dilini92 on 8/3/16.
 */
public abstract class Portfolio {
    private String ID;
    private Map<String, Integer> currentAssetQuantities;
    private Map<String, Integer> previousAssetQuantities;
    private double totalPortfolioValue;

    public Portfolio() {
        currentAssetQuantities = new HashMap<>();
    }

    public Portfolio(String ID, Map<String, Integer> assets) {
        this.ID = ID;
        this.currentAssetQuantities = assets;
        previousAssetQuantities = new HashMap<>();
    }

    public String getID() {
        return ID;
    }

    public Integer getCurrentAssetQuantities(String symbol) {
        return currentAssetQuantities.get(symbol);
    }

    public void setCurrentAssetQuantities(String symbol, int shares) {
        currentAssetQuantities.put(symbol, shares);
    }

    public Set<String> getAssetListKeySet() {
        return currentAssetQuantities.keySet();
    }

    public void setPreviousAssetQuantities(String symbol, int shares) {
        previousAssetQuantities.put(symbol, shares);
    }

    public int getPreviousAssetQuantities(String symbol) {
        if (previousAssetQuantities.get(symbol) != null)
            return previousAssetQuantities.get(symbol);
        return 0;
    }

    public double getTotalPortfolioValue() {
        return totalPortfolioValue;
    }

    public void updatePortfolioValue(Event event, double previousPrice){
        String symbol = event.getSymbol();
        //first event for the portfolio
        if(Double.compare(totalPortfolioValue, 0.0) == 0){
            totalPortfolioValue = event.getPrice() * getCurrentAssetQuantities(symbol);
        }else{  //portfolio already have data
            int previousShares = getPreviousAssetQuantities(symbol);
            totalPortfolioValue -= previousPrice * previousShares;

            int currentShares = getCurrentAssetQuantities(symbol);
            totalPortfolioValue += currentShares * event.getPrice();
        }
    }
}
