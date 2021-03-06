/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.extension.siddhi.execution.var.models;

import org.json.JSONObject;
import org.wso2.extension.siddhi.execution.var.models.util.RealTimeVaRConstants;
import org.wso2.extension.siddhi.execution.var.models.util.asset.Asset;
import org.wso2.extension.siddhi.execution.var.models.util.portfolio.Portfolio;
import org.wso2.extension.siddhi.execution.var.models.util.Event;

import java.util.HashMap;
import java.util.Map;

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
        asset.setPreviousStockPrice(previousPrice);
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
                portfolio.setPreviousAssetQuantities(symbol, portfolio.getCurrentAssetQuantities(symbol));
            }
        });

        //if no var has been calculated
        if (result.length() == 0)
            return null;
        return result.toString();
    }

    public Portfolio addPortfolio(String id, Portfolio portfolio) {
        return portfolioPool.put(id, portfolio); // returns null if there wasn't an existing mapping for id.
    }

    public abstract void simulateChangedAsset(String symbol);

    // TODO: Think about whether this method should add the newly created portfolio to the portfolio pool by default
    public abstract Portfolio createPortfolio(String id, Map<String, Integer> assets);

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