package org.wso2.siddhi.extension.var.models;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by dilini92 on 8/3/16.
 */
public abstract class Portfolio {
    private int ID;
    private Map<String, Integer> assets;

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

    public void setAssets(Map<String, Integer> assets) {
        this.assets = assets;
    }

    public Integer getCurrentShare(String symbol){
        return assets.get(symbol);
    }

    public void setCurrentShare(String symbol, int shares){
        assets.put(symbol, shares);
    }

    public void removeAsset(String symbol){
        assets.remove(symbol);
    }

    public int getAssetsSize(){
        return assets.size();
    }

    public Set<String> getAssetListKeySet(){
        return assets.keySet();
    }
}
