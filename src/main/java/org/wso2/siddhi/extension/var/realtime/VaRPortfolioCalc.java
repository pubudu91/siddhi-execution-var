package org.wso2.siddhi.extension.var.realtime;

import org.json.JSONObject;
import org.wso2.siddhi.extension.var.models.Asset;
import org.wso2.siddhi.extension.var.models.Portfolio;

import java.util.*;

/**
 * Created by dilini92 on 6/26/16.
 */
public abstract class VaRPortfolioCalc {
    protected double confidenceInterval = 0.95;
    protected int batchSize;

    private Map<Integer, Portfolio> portfolioList;
    protected Map<String, Asset> assetList; // this is public because it is used in VarModelAssertion for backtesting
    protected double price;
    protected String symbol;
    protected int portfolioID;
    protected int shares;

    /**
     * @param limit
     * @param ci
     */
    public VaRPortfolioCalc(int limit, double ci) {
        confidenceInterval = ci;
        batchSize = limit;

        //ensures that there will be only one instance of each
        portfolioList = new HashMap<>();
        assetList = new HashMap<>();
    }

    /**
     * for testing purposes
     *
     * @param assetList
     */
    public void setAssetList(Map<String, Asset> assetList) {
        this.assetList = assetList;
    }

    /**
     * @param data
     */
    public void addEvent(Object data[]) {
        if(data[0] != null)
            portfolioID = ((Number) data[0]).intValue();
        if(data[1] != null)
            shares = ((Number) data[1]).intValue();
        
        symbol = data[2].toString();
        price = ((Number) data[3]).doubleValue();

        //update asset pool
        updateAssetPool();

        if(portfolioID > 0){
            updatePortfolioPool();
        }
    }

    protected void updateAssetPool(){       //double check protected access
        double priceBeforeLastPrice;

        Asset temp = assetList.get(symbol);
        if(temp == null) {
            assetList.put(symbol, new Asset());
            temp = assetList.get(symbol);
            temp.setReturnValueSet(batchSize);
        }

        priceBeforeLastPrice = temp.getCurrentStockPrice();
        temp.setPriceBeforeLastPrice(priceBeforeLastPrice);
        temp.setCurrentStockPrice(price);

        //assume that all price values of assets cannot be zero or negative
        if(priceBeforeLastPrice > 0) {
            double value = Math.log(price / priceBeforeLastPrice);
            temp.addReturnValue(value);
            temp.getReturnValueSet().addValue(value);
        }
    }

    protected void updatePortfolioPool(){       //double check protected access
        Portfolio portfolio = portfolioList.get(portfolioID);

        if(portfolio == null){//first time for the portfolio
            Map<String, Integer> assets = new HashMap<>();
            assets.put(symbol, shares);
            portfolio = new Portfolio(portfolioID, assets);
            portfolioList.put(portfolioID, portfolio);
        }else if(portfolio.getAssets().get(symbol) == null){//first time for the asset within portfolio
            portfolio.setCurrentShare(symbol, shares);
        }else{//portfolio exists, asset within portfolio exists
            int currentShares = portfolio.getAssets().get(symbol);
            portfolio.setPreviousShare(symbol, currentShares);
            portfolio.setCurrentShare(symbol, shares + currentShares);
        }
    }

    /**
     * removes the oldest element from a given portfolio
     *
     * @param symbol
     */
    public void removeEvent(String symbol) {
        LinkedList<Double> priceList = assetList.get(symbol).getLatestReturnValues();
        priceList.remove(0);

        portfolioList.get(portfolioID).getAssets().remove(symbol);
    }

    /**
     * @param portfolio
     * @return
     */
    protected abstract Object processData(Portfolio portfolio);

    public Object calculateValueAtRisk(Object data[]){
        double var;
        JSONObject result = new JSONObject();
        
        addEvent(data);
        replaceAssetSimulation();

        //no need to call the remove event method

        Set<Integer> keys = portfolioList.keySet();
        Iterator<Integer> iterator = keys.iterator();

        while(iterator.hasNext()){
            Portfolio portfolio = portfolioList.get(iterator.next());
            Map<String, Integer> assets = portfolio.getAssets();
            if(assets.get(symbol) != null && assetList.get(symbol).getNumberOfHistoricalValues() > 1){
                Object temp = processData(portfolio);
                if(temp != null){
                    var = Double.parseDouble(temp.toString());
                    if(Double.compare(var, 0.0) != 0) {
                        result.put(RealTimeVaRConstants.PORTFOLIO + portfolio.getID(), var);
                    }
                }
            }

        }
        
        //if no var has been calculated
        if (result.length() == 0)
            return null;
        return result.toString();
    }

    public abstract double replaceAssetSimulation();
}

