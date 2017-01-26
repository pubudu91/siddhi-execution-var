package org.wso2.siddhi.extension.var.realtime;

import org.json.JSONObject;
import org.wso2.siddhi.extension.var.models.*;
import org.wso2.siddhi.extension.var.realtime.util.RealTimeVaRConstants;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by dilini92 on 6/26/16.
 */
public abstract class VaRCalculator {
    private double confidenceInterval;
    private int batchSize;

    private Map<String, Portfolio> portfolioPool;
    private Map<String, Asset> assetPool;

    /**
     * @param batchSize
     * @param confidenceInterval
     */
    public VaRCalculator(int batchSize, double confidenceInterval) {
        this.confidenceInterval = confidenceInterval;
        this.batchSize = batchSize;

        portfolioPool = new HashMap<>();
        assetPool = new HashMap<>();
    }

    /**
     * @param event
     * @return
     */
    public void addEvent(Event event) {

        //update portfolio pool
        if (event.getPortfolioID() != null)
            updatePortfolioPool(event.getPortfolioID(), event.getShares(), event.getSymbol());

        //update asset pool
        updateAssetPool(event.getSymbol(), event.getPrice());
    }

    /**
     * @param symbol
     * @param price
     * @return update the asset pool when an event comes from the stock price stream
     */
    private void updateAssetPool(String symbol, double price) {
        double priceBeforeLastPrice;
        Asset asset = assetPool.get(symbol);
        if (asset == null) {
            assetPool.put(symbol, AssetFactory.getAsset(this.getClass().getSimpleName(), batchSize));
            asset = assetPool.get(symbol);
        }

        priceBeforeLastPrice = asset.getCurrentStockPrice();
        asset.setPriceBeforeLastPrice(priceBeforeLastPrice);
        asset.setCurrentStockPrice(price);

        //assume that all price values of assets cannot be zero or negative
        if (priceBeforeLastPrice > 0) {
            double value = Math.log(price / priceBeforeLastPrice);
            asset.addReturnValue(value);
        }
    }

    /**
     * update portfolio pool when an event comes from the portfolio stream
     *
     * @param portfolioID
     * @param shares
     * @param symbol
     */
    private void updatePortfolioPool(String portfolioID, int shares, String symbol) {
        Portfolio portfolio = portfolioPool.get(portfolioID);

        if (portfolio == null) {//first time for the portfolio
            Map<String, Integer> assets = new HashMap<>();
            assets.put(symbol, shares);
            portfolio = PortfolioFactory.getPortfolio(this.getClass().getSimpleName(), portfolioID, assets);
            portfolioPool.put(portfolioID, portfolio);
        } else if (portfolio.getCurrentSharesCount(symbol) == null) {//first time for the asset within portfolio
            portfolio.setCurrentSharesCount(symbol, shares);
        } else {//portfolio exists, asset within portfolio exists
            int currentShares = portfolio.getCurrentSharesCount(symbol);
            portfolio.setCurrentSharesCount(symbol, shares + currentShares);
        }
    }

    /**
     * @param portfolio
     * @param event
     * @return
     */
    public abstract Double processData(Portfolio portfolio, Event event);

    /**
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
        if (data[RealTimeVaRConstants.PORTFOLIO_ID_INDEX] != null && data[RealTimeVaRConstants.SHARES_INDEX] != null) {
            portfolioID = data[RealTimeVaRConstants.PORTFOLIO_ID_INDEX].toString();
            shares = (Integer) data[RealTimeVaRConstants.SHARES_INDEX];
        }

        Event event = new Event(portfolioID, shares, symbol, price);
        addEvent(event);
        simulateChangedAsset(symbol);

        portfolioPool.forEach((id, portfolio) -> {
            Integer sharesCount = portfolio.getCurrentSharesCount(symbol);
            if (sharesCount != null) {

                //update total portfolio value
                double previousPrice = assetPool.get(symbol).getPreviousStockPrice();
                portfolio.updatePortfolioValue(event, previousPrice);

                //calculate value at risk
                Double var = processData(portfolio, event);
                if (var != null) {
                    result.put(RealTimeVaRConstants.PORTFOLIO + portfolio.getID(), var.doubleValue());
                }
            }
            portfolio.setPreviousSharesCount(symbol, portfolio.getCurrentSharesCount(symbol));
        });

        //if no var has been calculated
        if (result.length() == 0)
            return null;

        return result.toString();
    }

    public abstract void simulateChangedAsset(String symbol);

    public double getConfidenceInterval() {
        return confidenceInterval;
    }

    public Map<String, Portfolio> getPortfolioPool() {
        return portfolioPool;
    }

    public Map<String, Asset> getAssetPool() {
        return assetPool;
    }
}