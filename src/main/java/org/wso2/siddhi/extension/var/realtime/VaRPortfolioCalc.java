package org.wso2.siddhi.extension.var.realtime;

import org.json.JSONObject;
import org.wso2.siddhi.extension.var.models.Asset;
import org.wso2.siddhi.extension.var.models.AssetFactory;
import org.wso2.siddhi.extension.var.models.Portfolio;
import org.wso2.siddhi.extension.var.models.PortfolioFactory;

import java.util.*;

/**
 * Created by dilini92 on 6/26/16.
 */
public abstract class VaRPortfolioCalc {
    private double confidenceInterval = 0.95;
    private int batchSize;

    private Map<Integer, Portfolio> portfolioList;
    private Map<String, Asset> assetList; // this is public because it is used in VarModelAssertion for backtesting

    private String type;

    private double price;
    private String symbol;
    private int portfolioID;
    private int shares;

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
     * @param data
     */
    public void addEvent(Object data[]) {
        portfolioID = 0;
        shares = 0;
        symbol = data[2].toString();
        price = ((Number) data[3]).doubleValue();

        if (data[0] != null && data[1] != null) {
            portfolioID = ((Number) data[0]).intValue();
            shares = ((Number) data[1]).intValue();
            updatePortfolioPool();
        }

        //update asset pool
        updateAssetPool();
    }

    protected void updateAssetPool() {       //double check protected access
        double priceBeforeLastPrice;

        Asset temp = assetList.get(symbol);
        if (temp == null) {
            assetList.put(symbol, AssetFactory.getAsset(type));
            temp = assetList.get(symbol);
            temp.setReturnValueSet(batchSize);
        }

        priceBeforeLastPrice = temp.getCurrentStockPrice();
        temp.setPriceBeforeLastPrice(priceBeforeLastPrice);
        temp.setCurrentStockPrice(price);

        //assume that all price values of assets cannot be zero or negative
        if (priceBeforeLastPrice > 0) {
            double value = Math.log(price / priceBeforeLastPrice);
            temp.addReturnValue(value);                             /**if descriptive stat can be used, this is not required*/
            //TODO addValue should be within asset class
            temp.getReturnValueSet().addValue(value);
        }
    }

    protected void updatePortfolioPool() {       //double check protected access
        Portfolio portfolio = portfolioList.get(portfolioID);

        if (portfolio == null) {//first time for the portfolio
            Map<String, Integer> assets = new HashMap<>();
            assets.put(symbol, shares);
            portfolio = PortfolioFactory.getPortfolio(type, portfolioID, assets);
            portfolioList.put(portfolioID, portfolio);
        } else if (portfolio.getCurrentShare(symbol) == null) {//first time for the asset within portfolio
            portfolio.setCurrentShare(symbol, shares);
        } else {//portfolio exists, asset within portfolio exists
            int currentShares = portfolio.getCurrentShare(symbol);
            portfolio.setCurrentShare(symbol, shares + currentShares);
        }
    }


    /**
     * @param portfolio
     * @return
     */
    protected abstract Object processData(Portfolio portfolio);

    public Object calculateValueAtRisk(Object data[]) {
        double var;
        JSONObject result = new JSONObject();

        addEvent(data);
        replaceAssetSimulation();

        //no need to call the remove event method

        Set<Integer> keys = portfolioList.keySet();
        Iterator<Integer> iterator = keys.iterator();

        while (iterator.hasNext()) {
            Portfolio portfolio = portfolioList.get(iterator.next());
            Integer shares = portfolio.getCurrentShare(symbol);
            if (shares != null && assetList.get(symbol).getNumberOfHistoricalValues() > 1) {
                Object temp = processData(portfolio);
                if (temp != null) {
                    var = Double.parseDouble(temp.toString());
                    if (Double.compare(var, 0.0) != 0) {
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getConfidenceInterval() {
        return confidenceInterval;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public Map<Integer, Portfolio> getPortfolioList() {
        return portfolioList;
    }

    public Map<String, Asset> getAssetList() {
        return assetList;
    }

    public String getSymbol() {
        return symbol;
    }

    public int getPortfolioID() {
        return portfolioID;
    }

    public int getShares() {
        return shares;
    }

    public double getPrice() {return price;}

    public void setPrice(double price) {
        this.price = price;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public void setPortfolioID(int portfolioID) {
        this.portfolioID = portfolioID;
    }

    public void setShares(int shares) {
        this.shares = shares;
    }
}