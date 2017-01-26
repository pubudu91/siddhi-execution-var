package org.wso2.siddhi.extension.var.models;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by dilini92 on 8/3/16.
 */
public abstract class Portfolio {
    private String ID;
    private Map<String, Integer> currentShares;
    private Map<String, Integer> previousShares;
    private double totalPortfolioValue;

    public Portfolio() {
        currentShares = new HashMap<>();
    }

    public Portfolio(String ID, Map<String, Integer> assets) {
        this.ID = ID;
        this.currentShares = assets;
        previousShares = new HashMap<>();
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public Integer getCurrentSharesCount(String symbol) {
        if (currentShares.get(symbol) != null)
            return currentShares.get(symbol);
        else
            return 0;
    }

    public void setCurrentSharesCount(String symbol, int shares) {
        currentShares.put(symbol, shares);
    }

    public Set<String> getAssetListKeySet() {
        return currentShares.keySet();
    }

    public void setPreviousSharesCount(String symbol, int shares) {
        previousShares.put(symbol, shares);
    }

    public int getPreviousSharesCount(String symbol) {
        if (previousShares.get(symbol) != null)
            return previousShares.get(symbol);
        return 0;
    }

    public double getTotalPortfolioValue() {
        return totalPortfolioValue;
    }

    public void updatePortfolioValue(Event event, double previousPrice){
        String symbol = event.getSymbol();
        //first event for the portfolio
        if(Double.compare(totalPortfolioValue, 0.0) == 0){
            totalPortfolioValue = event.getPrice() * getCurrentSharesCount(symbol);
        }else{  //portfolio already have data
            int previousShares = getPreviousSharesCount(symbol);
            totalPortfolioValue -= previousPrice * previousShares;

            int currentShares = getCurrentSharesCount(symbol);
            totalPortfolioValue += currentShares * event.getPrice();
        }
    }
}
