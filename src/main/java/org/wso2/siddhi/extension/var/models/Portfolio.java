package org.wso2.siddhi.extension.var.models;

import java.util.Map;

/**
 * Created by dilini92 on 8/3/16.
 */
public class Portfolio {
    private int ID;
    private Map<String, Asset> assets;

    public Portfolio() {
    }

    public Portfolio(int ID, Map<String, Asset> assets) {
        this.ID = ID;
        this.assets = assets;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public Map<String, Asset> getAssets() {
        return assets;
    }

    public void setAssets(Map<String, Asset> assets) {
        this.assets = assets;
    }
}
