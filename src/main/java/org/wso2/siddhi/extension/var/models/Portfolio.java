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
