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

package org.wso2.extension.siddhi.execution.var.models.historical;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.wso2.extension.siddhi.execution.var.models.util.asset.Asset;
import org.wso2.extension.siddhi.execution.var.models.VaRCalculator;
import org.wso2.extension.siddhi.execution.var.models.util.Event;
import org.wso2.extension.siddhi.execution.var.models.util.asset.HistoricalAsset;
import org.wso2.extension.siddhi.execution.var.models.util.portfolio.HistoricalPortfolio;
import org.wso2.extension.siddhi.execution.var.models.util.portfolio.Portfolio;

import java.util.Map;

public class HistoricalVaRCalculator extends VaRCalculator {

    /**
     * @param batchSize
     * @param ci
     */
    public HistoricalVaRCalculator(int batchSize, double ci) {
        super(batchSize, ci);

    }

    /**
     * @return the var of the portfolio Calculate the contribution of the changed asset to the portfolio loss and then
     * adjust the previous loss value using it. A distribution is constructed using those loss values and the ultimate
     * VaR value is obtained.
     */
    @Override
    public Double processData(Portfolio portfolio, Event event) {
        HistoricalPortfolio historicalPortfolio = (HistoricalPortfolio) portfolio;
        String symbol = event.getSymbol();
        HistoricalAsset asset = (HistoricalAsset) getAssetPool().get(symbol);
        double[] currentSimulatedPriceList = asset.getCurrentSimulatedPriceList();
        double[] cumulativeLossValues = historicalPortfolio.getCumulativeLossValues();

        //there should be at least one return value
        if (asset.getNumberOfReturnValues() > 0) {
            DescriptiveStatistics descriptiveStatistics = new DescriptiveStatistics(currentSimulatedPriceList.length);

            //first time for the portfolio
            if (cumulativeLossValues == null) {
                cumulativeLossValues = new double[getBatchSize() - 1];
            }

            double previousSimulatedPriceList[] = asset.getPreviousSimulatedPriceList();
            int previousQty = portfolio.getPreviousAssetQuantities(symbol);
            int currentQty = portfolio.getCurrentAssetQuantities(symbol);

            //incrementally calculate the cumulative loss value
            //new cumulative loss value = previous loss value + adjustment
            for (int i = 0; i < currentSimulatedPriceList.length; i++) {
                //2nd time for the portfolio
                if (i < previousSimulatedPriceList.length) {
                    cumulativeLossValues[i] = cumulativeLossValues[i] -
                            (previousSimulatedPriceList[i] * previousQty);
                }

                //incrementally calculate the cumulative loss value
                cumulativeLossValues[i] += (currentSimulatedPriceList[i] * currentQty);
                descriptiveStatistics.addValue(cumulativeLossValues[i]);
            }

            historicalPortfolio.setCumulativeLossValues(cumulativeLossValues);
            return descriptiveStatistics.getPercentile((1 - getConfidenceInterval()) * 100);
        }
        return null;
    }

    /**
     * simulate the changed asset once
     *
     * @param symbol
     */
    @Override
    public void simulateChangedAsset(String symbol) {
        HistoricalAsset asset = (HistoricalAsset) getAssetPool().get(symbol);
        //there should be at least one return value
        if (asset.getNumberOfReturnValues() > 0) {
            double returnValues[] = asset.getReturnValues();
            double[] currentSimulatedPriceList = asset.getCurrentSimulatedPriceList();

            //if there are no current simulated price values
            if (currentSimulatedPriceList == null) {
                asset.setPreviousSimulatedPriceList(new double[1]);
            } else {
                //set the current simulated list as the previous one
                asset.setPreviousSimulatedPriceList(currentSimulatedPriceList);
            }

            double tempSimulatedPriceList[] = new double[returnValues.length];

            //simulated price = return value * current stock price
            for (int i = 0; i < returnValues.length; i++) {
                tempSimulatedPriceList[i] = -returnValues[i] * asset.getCurrentStockPrice();
            }
            asset.setCurrentSimulatedPriceList(tempSimulatedPriceList);
        }
    }

    @Override
    public Portfolio createPortfolio(String id, Map<String, Integer> assets) {
        return new HistoricalPortfolio(id, assets);
    }

    @Override
    public Asset createAsset(int windowSize) {
        return new HistoricalAsset(windowSize);
    }
}
