

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

package org.wso2.extension.siddhi.execution.var.backtest;

import org.apache.commons.math3.distribution.BinomialDistribution;
import org.json.JSONObject;
import org.wso2.extension.siddhi.execution.var.models.util.asset.Asset;
import org.wso2.extension.siddhi.execution.var.models.VaRCalculator;
import org.wso2.extension.siddhi.execution.var.models.parametric.ParametricVaRCalculator;
import org.wso2.extension.siddhi.execution.var.models.util.Event;
import org.wso2.extension.siddhi.execution.var.models.util.portfolio.Portfolio;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class BacktestDaily {

    private static final int BATCH_SIZE = 251;
    private static final double VAR_CI = 0.95;
    private static final double BACKTEST_CI = 0.05;
    private static final int NUMBER_OF_ASSETS = 25;
    private static final int SAMPLE_SIZE = 20;
    private static final int VAR_PER_SAMPLE = 500;
    private static final String PORTFOLIO_KEY = "Portfolio 1";
    private ArrayList<Double> calculatedVarList;
    private ArrayList<Double> actualVarList;
    private Double previousPortfolioValue;

    public BacktestDaily() {
        calculatedVarList = new ArrayList();
        actualVarList = new ArrayList();
        previousPortfolioValue = null;
    }

    public static void main(String[] args) throws FileNotFoundException {
        new BacktestDaily().runBackTest();
    }

    private void runBackTest() throws FileNotFoundException {

        //VaRCalculator varCalculator = new HistoricalVaRCalculator(BATCH_SIZE, VAR_CI);
        VaRCalculator varCalculator = new ParametricVaRCalculator(BATCH_SIZE, VAR_CI);
        //VaRCalculator varCalculator = new MonteCarloVarCalculator(BATCH_SIZE, VAR_CI, 2500,100,0.01);

        ArrayList<Event> list = readBacktestData();
        int i = 0;
        int totalEvents = (BATCH_SIZE + 1) * NUMBER_OF_ASSETS + VAR_PER_SAMPLE * NUMBER_OF_ASSETS * SAMPLE_SIZE + 1;
        System.out.println("Read Total Events : " + totalEvents);

        while (i < totalEvents) {
            // fill lists
            if (i < (BATCH_SIZE + 1) * NUMBER_OF_ASSETS) {
                varCalculator.calculateValueAtRisk(list.get(i));
                i++;
            } else {
                if (i % NUMBER_OF_ASSETS == 0) {
                    System.out.print("Event " + (i) + " : ");
                    String jsonString = (String) varCalculator.calculateValueAtRisk(list.get(i));
                    JSONObject jsonObject = new JSONObject(jsonString);
                    Double calculatedVar = (Double) jsonObject.get(PORTFOLIO_KEY);  // hardcoded for portfolio ID 1
                    System.out.print("CV : " + calculatedVar);
                    calculatedVarList.add(calculatedVar);                           // should filter
                    calculateActualLoss(varCalculator.getPortfolioPool().get("1"), varCalculator.getAssetPool());
                    System.out.println();
                } else {
                    varCalculator.calculateValueAtRisk(list.get(i));
                }
                i++;
            }
        }

        runStandardCoverageTest();
    }

    private void runStandardCoverageTest() {

        BinomialDistribution dist = new BinomialDistribution(VAR_PER_SAMPLE, 1 - VAR_CI);
        double leftEnd = dist.inverseCumulativeProbability(BACKTEST_CI / 2);
        double rightEnd = dist.inverseCumulativeProbability(1 - (BACKTEST_CI / 2));

        System.out.println("Left End :" + leftEnd);
        System.out.println("Right End :" + rightEnd);

        int numberOfExceptions;
        int successCount = 0;
        for (int j = 0; j < SAMPLE_SIZE; j++) {
            numberOfExceptions = 0;
            for (int i = j * VAR_PER_SAMPLE; i < (j + 1) * VAR_PER_SAMPLE; i++) {
                //System.out.println(actualVarList.get(i) + " " + calculatedVarList.get(i));
                if (actualVarList.get(i) <= calculatedVarList.get(i)) {
                    numberOfExceptions++;
                }
            }
            System.out.println("Sample Set : " + (j + 1) + " Exceptions : " + numberOfExceptions);
            if (rightEnd >= numberOfExceptions && leftEnd <= numberOfExceptions) {
                successCount++;
            }
        }
        System.out.println("Success Percentage : " + (((double) successCount) / SAMPLE_SIZE) * 100);
    }

    private void calculateActualLoss(Portfolio portfolio, Map<String, Asset> assetMap) {
        Double currentPortfolioValue = 0.0;
        Asset asset;
        Set<String> keys = portfolio.getAssetListKeySet();

        for (String symbol : keys) {
            asset = assetMap.get(symbol);
            currentPortfolioValue += asset.getCurrentStockPrice() * portfolio.getCurrentAssetQuantities(symbol);
        }

        if (previousPortfolioValue != null) {
            actualVarList.add(currentPortfolioValue - previousPortfolioValue);
            System.out.print(" AV : " + (currentPortfolioValue - previousPortfolioValue));
        }
        previousPortfolioValue = currentPortfolioValue;
    }

    public ArrayList<Event> readBacktestData() throws FileNotFoundException {
        ClassLoader classLoader = getClass().getClassLoader();
        Scanner scan = new Scanner(new File(classLoader.getResource("BackTestDataReal.csv").getFile()));
        ArrayList<Event> list = new ArrayList();
        Event event;
        String[] split;

        while (scan.hasNext()) {
            event = new Event();
            split = scan.nextLine().split(",");
            if (split.length == 2) {
                event.setSymbol(split[0]);
                event.setPrice(Double.parseDouble(split[1]));
            } else {
                event.setPortfolioID(split[0]);
                event.setQuantity(Integer.parseInt(split[1]));
                event.setSymbol(split[2]);
                event.setPrice(Double.parseDouble(split[3]));
            }
            list.add(event);
        }
        return list;
    }
}
