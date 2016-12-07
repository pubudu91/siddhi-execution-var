package org.wso2.siddhi.extension.var.models;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by dilini92 on 8/3/16.
 */
public class Portfolio {
    private int ID;
    private Map<String, Integer> assets;
    private double[] returnList;
    private String incomingEventLabel = null;
    private double currentTotalPortfolioValue = 0;
    private Map<String, Integer> numberOfSharesBeforeChange;
    private Map<String, double[]> recentSimulatedList = new HashMap<>();
    private boolean toggle = true;

    public boolean isToggle() {
        return toggle;
    }

    public void setToggle(boolean toggle) {
        this.toggle = toggle;
    }

    public Map<String, double[]> getRecentSimulatedList() {
        return recentSimulatedList;
    }

//    public void setRecentSimulatedList(Map<String, double[]> recentSimulatedList) {
//        this.recentSimulatedList = recentSimulatedList;
//    }

    public Map<String, Integer> getNumberOfSharesBeforeChange() {
        return numberOfSharesBeforeChange;
    }

    public void setNumberOfSharesBeforeChange(Map<String, Integer> numberOfSharesBeforeChange) {
        this.numberOfSharesBeforeChange = numberOfSharesBeforeChange;
    }

    public double getCurrentTotalPortfolioValue() {
        return currentTotalPortfolioValue;
    }

    public void setCurrentTotalPortfolioValue(double currentTotalPortfolioValue) {
        this.currentTotalPortfolioValue = currentTotalPortfolioValue;
    }

    public String getIncomingEventLabel() {
        return incomingEventLabel;
    }

    public void setIncomingEventLabel(String incomingEventLabel) {
        this.incomingEventLabel = incomingEventLabel;
    }

    public double[] getReturnList() {
        return returnList;
    }

    public void setReturnList(double[] returnList) {
        this.returnList = returnList;
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
}
