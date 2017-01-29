package org.wso2.siddhi.extension.var.models;

import org.json.JSONObject;
import org.wso2.siddhi.extension.var.models.util.Event;
import org.wso2.siddhi.extension.var.models.util.RealTimeVaRConstants;
import org.wso2.siddhi.extension.var.models.util.asset.Asset;
import org.wso2.siddhi.extension.var.models.util.portfolio.Portfolio;

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
        if (event.getPortfolioID() != null) {
            updatePortfolioPool(event.getPortfolioID(), event.getQuantity(), event.getSymbol());
        }

        //update asset pool
        updateAssetPool(event.getSymbol(), event.getPrice());
    }

    /**
     * @param symbol
     * @param price
     * @return update the asset pool when an event comes from the stock price stream
     */
    private void updateAssetPool(String symbol, double price) {
        double previousPrice;
        Asset asset = assetPool.get(symbol);
        if (asset == null) {
            assetPool.put(symbol, createAsset(batchSize));
            asset = assetPool.get(symbol);
        }

        previousPrice = asset.getCurrentStockPrice();
        asset.setPreviousPrice(previousPrice);
        asset.setCurrentStockPrice(price);

        //assume that all price values of assets cannot be zero or negative
        if (previousPrice > 0) {
            double value = Math.log(price / previousPrice);
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
            portfolio = createPortfolio(portfolioID, assets);
            portfolioPool.put(portfolioID, portfolio);
        } else if (portfolio.getCurrentAssetQuantities(symbol) == null) {//first time for the asset within portfolio
            portfolio.setCurrentAssetQuantities(symbol, shares);
        } else {//portfolio exists, asset within portfolio exists
            int currentShares = portfolio.getCurrentAssetQuantities(symbol);
            portfolio.setCurrentAssetQuantities(symbol, shares + currentShares);
        }
    }

    /**
     * @param portfolio
     * @param event
     * @return
     */
    public abstract Double processData(Portfolio portfolio, Event event);

    /**
     *
     * @param event
     * @return
     */
    public Object calculateValueAtRisk(Event event) {

        JSONObject result = new JSONObject();

        String symbol = event.getSymbol();

        addEvent(event);
        simulateChangedAsset(symbol);

        portfolioPool.forEach((id, portfolio) -> {
            Integer sharesCount = portfolio.getCurrentAssetQuantities(symbol);
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
            portfolio.setPreviousAssetQuantities(symbol, portfolio.getCurrentAssetQuantities(symbol));
        });

        //if no var has been calculated
        if (result.length() == 0)
            return null;
        return result.toString();
    }

    public abstract void simulateChangedAsset(String symbol);

    public abstract Portfolio createPortfolio(String ID, Map<String, Integer> assets);

    public abstract Asset createAsset(int windowSize);

    public double getConfidenceInterval() {
        return confidenceInterval;
    }

    public int getBatchSize() {return batchSize;}

    public Map<String, Portfolio> getPortfolioPool() {
        return portfolioPool;
    }

    public Map<String, Asset> getAssetPool() {
        return assetPool;
    }
}