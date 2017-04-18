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

package org.wso2.extension.siddhi.execution.var.models.util.portfolio;

import org.wso2.extension.siddhi.execution.var.models.util.Event;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class Portfolio {

    private String ID;
    private Map<String, Integer> currentAssetQuantities;
    private Map<String, Integer> previousAssetQuantities;
    private double totalPortfolioValue;

    public Portfolio() {
        currentAssetQuantities = new HashMap<>();
    }

    public Portfolio(String ID, Map<String, Integer> assets) {
        this.ID = ID;
        this.currentAssetQuantities = assets;
        previousAssetQuantities = new HashMap<>();
    }

    public String getID() {
        return ID;
    }

    public Integer getCurrentAssetQuantities(String symbol) {
        return currentAssetQuantities.get(symbol);
    }

    public void setCurrentAssetQuantities(String symbol, int shares) {
        currentAssetQuantities.put(symbol, shares);
    }

    public Set<String> getAssetListKeySet() {
        return currentAssetQuantities.keySet();
    }

    public void setPreviousAssetQuantities(String symbol, int shares) {
        previousAssetQuantities.put(symbol, shares);
    }

    public int getPreviousAssetQuantities(String symbol) {
        if (previousAssetQuantities.get(symbol) != null) {
            return previousAssetQuantities.get(symbol);
        }
        return 0;
    }

    public double getTotalPortfolioValue() {
        return totalPortfolioValue;
    }

    public void updatePortfolioValue(Event event, double previousPrice) {
        String symbol = event.getSymbol();

        //first event for the portfolio
        if (Double.compare(totalPortfolioValue, 0.0) == 0) {
            totalPortfolioValue = event.getPrice() * getCurrentAssetQuantities(symbol);
        } else {  //portfolio already have data
            int previousShares = getPreviousAssetQuantities(symbol);
            totalPortfolioValue -= previousPrice * previousShares;

            int currentShares = getCurrentAssetQuantities(symbol);
            totalPortfolioValue += currentShares * event.getPrice();
        }
    }
}
