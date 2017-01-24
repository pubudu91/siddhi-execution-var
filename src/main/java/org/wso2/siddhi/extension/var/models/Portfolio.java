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

    public void setCurrentShares(Map<String, Integer> currentShares) {
        this.currentShares = currentShares;
    }

    public Integer getCurrentSharesCount(String symbol){
        if(currentShares.get(symbol) != null)
            return currentShares.get(symbol);
        else
            return 0;
    }

    public void setCurrentSharesCount(String symbol, int shares){
        currentShares.put(symbol, shares);
    }

    public void removeAsset(String symbol){
        currentShares.remove(symbol);
    }

    public int getAssetsSize(){
        return currentShares.size();
    }

    public Set<String> getAssetListKeySet(){
        return currentShares.keySet();
    }

    public void setPreviousSharesCount(String symbol, int shares){
        previousShares.put(symbol, shares);
    }

    public int getPreviousSharesCount(String symbol){
        if(previousShares.get(symbol) != null)
            return previousShares.get(symbol);
        return 0;
    }
}
