package org.wso2.siddhi.extension.var.realtime;

import org.json.JSONObject;
import org.wso2.siddhi.extension.var.models.*;
import org.wso2.siddhi.extension.var.realtime.util.RealTimeVaRConstants;

import java.util.*;

/**
 * Created by dilini92 on 6/26/16.
 */
public abstract class VaRCalculator {
    private double confidenceInterval;
    private int batchSize;

    private Map<String, Portfolio> portfolioList;
    private Map<String, Asset> assetList;

    private String type;

    /**
     * @param batchSize
     * @param confidenceInterval
     */
    public VaRCalculator(int batchSize, double confidenceInterval) {
        this.confidenceInterval = confidenceInterval;
        this.batchSize = batchSize;

        //ensures that there will be only one instance of each
        portfolioList = new HashMap<>();
        assetList = new HashMap<>();
    }

    /**
     *
     * @param event
     * @return
     */
    public Double addEvent(Event event) {

        if(event.getPortfolioID() != null)
            updatePortfolioPool(event.getPortfolioID(), event.getShares(), event.getSymbol());

        //update asset pool
        return updateAssetPool(event.getSymbol(), event.getPrice());
    }

    /**
     *
     * @param symbol
     * @param price
     * @return
     */
    protected Double updateAssetPool(String symbol, double price) {       //double check protected access
        double priceBeforeLastPrice;

        Asset temp = assetList.get(symbol);
        if (temp == null) {
            assetList.put(symbol, AssetFactory.getAsset(type, batchSize));
            temp = assetList.get(symbol);
        }

        priceBeforeLastPrice = temp.getCurrentStockPrice();
        temp.setPriceBeforeLastPrice(priceBeforeLastPrice);
        temp.setCurrentStockPrice(price);

        //assume that all price values of assets cannot be zero or negative
        if (priceBeforeLastPrice > 0) {
            double value = Math.log(price / priceBeforeLastPrice);
            return temp.addReturnValue(value);                             /**if descriptive stat can be used, this is not required*/
        }

        return null;
    }

    /**
     *
     * @param portfolioID
     * @param shares
     * @param symbol
     */
    protected void updatePortfolioPool(String portfolioID, int shares, String symbol) {       //double check protected access
        Portfolio portfolio = portfolioList.get(portfolioID);

        if (portfolio == null) {//first time for the portfolio
            Map<String, Integer> assets = new HashMap<>();
            assets.put(symbol, shares);
            portfolio = PortfolioFactory.getPortfolio(type, portfolioID, assets);
            portfolioList.put(portfolioID, portfolio);
        } else if (portfolio.getCurrentSharesCount(symbol) == null) {//first time for the asset within portfolio
            portfolio.setCurrentSharesCount(symbol, shares);
        } else {//portfolio exists, asset within portfolio exists
            int currentShares = portfolio.getCurrentSharesCount(symbol);
            portfolio.setPreviousSharesCount(symbol, currentShares);
            portfolio.setCurrentSharesCount(symbol, shares + currentShares);
        }
    }


    /**
     * @param portfolio
     * @return
     */
    //TODO - remove event object if not required
    protected abstract Double processData(Portfolio portfolio, Event event);

    /**
     *
     * @param data
     * @return
     */
    public Object calculateValueAtRisk(Object data[]) {

        JSONObject result = new JSONObject();

        //initialize variables from the streams
        String portfolioID = null;
        int shares = 0;
        String symbol = data[RealTimeVaRConstants.SYMBOL_INDEX].toString();
        double price = ((Double) data[RealTimeVaRConstants.PRICE_INDEX]);

        if (data[RealTimeVaRConstants.PORTFOLIO_ID_INDEX] != null  && data[RealTimeVaRConstants.SHARES_INDEX] != null) {
            portfolioID = data[RealTimeVaRConstants.PORTFOLIO_ID_INDEX].toString();
            shares = (Integer) data[RealTimeVaRConstants.SHARES_INDEX];
        }

        Event event = new Event(portfolioID, shares, symbol, price);
        Double removedEvent = addEvent(event);
        replaceAssetSimulation(removedEvent, symbol);

        portfolioList.forEach((id, portfolio) -> {
            Integer sharesCount = portfolio.getCurrentSharesCount(symbol);
            if (sharesCount != null) {
                Double var = processData(portfolio, event);
                if (var != null) {
                    result.put(RealTimeVaRConstants.PORTFOLIO + portfolio.getID(), var.doubleValue());
                }
            }
        });

        //if no var has been calculated
        if (result.length() == 0)
            return null;

        return result.toString();
    }

    public abstract double replaceAssetSimulation(Double removedEvent, String symbol);

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

    public Map<String, Portfolio> getPortfolioList() {
        return portfolioList;
    }

    public Map<String, Asset> getAssetList() {
        return assetList;
    }
}