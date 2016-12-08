package org.wso2.siddhi.extension.var.batchmode;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.wso2.siddhi.extension.var.models.Asset;
import org.wso2.siddhi.extension.var.models.Portfolio;
import org.wso2.siddhi.extension.var.realtime.VaRPortfolioCalc;

import java.util.LinkedList;

/**
 * Created by flash on 6/29/16.
 */
public class MonteCarloVarCalculator extends VaRPortfolioCalc {

    private int numberOfTrials;
    private int calculationsPerDay;
    private double timeSlice;
    private boolean toggle = true;

    public MonteCarloVarCalculator(int limit, double ci,
                                   int numberOfTrials, int calculationsPerDay, double timeSlice) {
        super(limit, ci);
        this.numberOfTrials = numberOfTrials;
        this.calculationsPerDay = calculationsPerDay;
        this.timeSlice = timeSlice;
    }

    /**
     * calculate the var metric using monte carlo simulation
     *
     * @return
     */
    @Override
    public Object processData(Portfolio portfolio) {
//        double[] terminalStockValues;
//        double[] finalPortfolioValues = new double[numberOfTrials];
//        String[] keys = portfolio.getAssets().keySet().toArray(new String[portfolio.getAssets().size()]);
//        Asset tempAsset;
//        LinkedList<Double> returnList;
//        double todayMarketValue;
//        int numberOfShares = 0;
//        MonteCarloNativeSimulation calcNativeReference = new MonteCarloNativeSimulation();
//        MonteCarloSimulation calcReference = new MonteCarloSimulation(this.numberOfTrials);
//        double latestMarketValue = 0;
//
//        double[] unchangedSimulatedListCollection = new double[this.numberOfTrials];
//        double[] simulatedListBeforeChange;
//        double[] finalPortfolioValuesBeforeUpdate;
//
//        double mean = 0, std = 0;
//
//        for (int i = 0; i < numberOfTrials; i++) {
//            finalPortfolioValues[i] = 0;
//        }
////do simulation for all the assets in the portfolio
//        //toggling should be local to a portfolio because every portfolio should follow this initial calculation
//        if (portfolio.isToggle() == true) {
//            for (int i = 0; i < keys.length; i++) {
//                tempAsset = assetList.get(keys[i]);
//                numberOfShares = portfolio.getAssets().get(keys[i]);
//                //this price setting should be changed after streams has been changed
//                returnList = tempAsset.getLatestReturnValues();
//                tempAsset.setPriceBeforeLastPrice(tempAsset.getCurrentStockPrice());
//                todayMarketValue = (tempAsset.getCurrentStockPrice() * numberOfShares);
//                latestMarketValue += todayMarketValue;
//
////                terminalStockValues = calcReference.simulation(this.numberOfTrials, this.calculationsPerDay,
////                        historicalValues.stream().mapToDouble(d -> d).toArray(), this.timeSlice, historicalValues.getLast());
//
//                mean = calcReference.getMeanReturnAndStandardDeviation(returnList.stream().mapToDouble(d -> d).toArray()).get("meanReturn");
//                std = calcReference.getMeanReturnAndStandardDeviation(returnList.stream().mapToDouble(d -> d).toArray()).get("meanStandardDeviation");
//                terminalStockValues = calcNativeReference.simulation(mean, std, this.timeSlice,
//                        tempAsset.getCurrentStockPrice(), this.numberOfTrials, this.calculationsPerDay);
//                /*
//                 * terminal stock values are local to a particular portfolio so we cant store it in assets.
//                 * simulated list which used later in calculation
//                 */
//
//                portfolio.getRecentSimulatedList().put(keys[i], terminalStockValues);
//
//                for (int j = 0; j < terminalStockValues.length; j++) {
//                    finalPortfolioValues[j] += (todayMarketValue - (terminalStockValues[j] * numberOfShares));
//                }
//            }
//            portfolio.setCurrentTotalPortfolioValue(latestMarketValue);
//            portfolio.setToggle(false);
//        } else {
//            /*
//            do simulation for the changed asset only
//             */
//            tempAsset = assetList.get(portfolio.getIncomingEventLabel());
//            returnList = tempAsset.getLatestReturnValues();
//            numberOfShares = portfolio.getAssets().get(portfolio.getIncomingEventLabel());
////            terminalStockValues = calcReference.simulation(this.numberOfTrials, this.calculationsPerDay,
////                    historicalValues.stream().mapToDouble(d -> d).toArray(), this.timeSlice, historicalValues.getLast());
//
//            mean = calcReference.getMeanReturnAndStandardDeviation(returnList.stream().mapToDouble(d -> d).toArray()).get("meanReturn");
//            std = calcReference.getMeanReturnAndStandardDeviation(returnList.stream().mapToDouble(d -> d).toArray()).get("meanStandardDeviation");
//            terminalStockValues = calcNativeReference.simulation(mean, std, this.timeSlice,
//                    tempAsset.getCurrentStockPrice(), this.numberOfTrials, this.calculationsPerDay);
//
//            /**
//             * newly added part for simulation improvement
//             */
//
//            simulatedListBeforeChange = portfolio.getRecentSimulatedList().get(portfolio.getIncomingEventLabel());
//            double lastPortfolioValue = portfolio.getCurrentTotalPortfolioValue();
//            finalPortfolioValuesBeforeUpdate = portfolio.getReturnList();
//            for (int i = 0; i < this.numberOfTrials; i++) {
//                unchangedSimulatedListCollection[i] = lastPortfolioValue - (simulatedListBeforeChange[i] * numberOfShares + finalPortfolioValuesBeforeUpdate[i]);
//            }
////calculate latest portfolio value and store it in portfolio. set the latest stock price as recentStock price in the changed asset
//            latestMarketValue = portfolio.getCurrentTotalPortfolioValue() - tempAsset.getPriceBeforeLastPrice() * numberOfShares + tempAsset.getCurrentStockPrice() * numberOfShares;
//            tempAsset.setPriceBeforeLastPrice(tempAsset.getCurrentStockPrice());
//            portfolio.setCurrentTotalPortfolioValue(latestMarketValue);
//
//            for (int i = 0; i < this.numberOfTrials; i++) {
//                finalPortfolioValues[i] = latestMarketValue - (terminalStockValues[i] * numberOfShares + unchangedSimulatedListCollection[i]);
//            }
//
////set simulated list
//            portfolio.getRecentSimulatedList().put(portfolio.getIncomingEventLabel(), terminalStockValues);
//        }
//        portfolio.setReturnList(finalPortfolioValues);
//        return new DescriptiveStatistics(finalPortfolioValues).getPercentile((1 - confidenceInterval) * 100);
        return processData_updated(portfolio);
    }

    public Object processData_updated(Portfolio portfolio) {
        double[] terminalStockValues;
        double[] finalPortfolioValues = new double[numberOfTrials];
//        String[] keys = portfolio.getAssets().keySet().toArray(new String[portfolio.getAssets().size()]);
        Asset tempAsset;
        LinkedList<Double> returnList;
//        double todayMarketValue;
        int numberOfShares = 0;
        MonteCarloNativeSimulation calcNativeReference = new MonteCarloNativeSimulation();
        MonteCarloSimulation calcReference = new MonteCarloSimulation(this.numberOfTrials);
        double latestMarketValue = 0;

        double[] unchangedSimulatedListCollection = new double[this.numberOfTrials];
        double[] simulatedListBeforeChange;
        double[] finalPortfolioValuesBeforeUpdate;

        double mean = 0, std = 0;

        for (int i = 0; i < numberOfTrials; i++) {
            finalPortfolioValues[i] = 0;
        }
//do simulation for all the assets in the portfolio
        //toggling should be local to a portfolio because every portfolio should follow this initial calculation
      /*
            do simulation for the changed asset only
             */
        tempAsset = assetList.get(portfolio.getIncomingEventLabel());
        returnList = tempAsset.getLatestReturnValues();
//        return
        if (returnList != null && returnList.size() > 0) {
            numberOfShares = portfolio.getAssets().get(portfolio.getIncomingEventLabel());

            mean = calcReference.getMeanReturnAndStandardDeviation(returnList.stream().mapToDouble(d -> d).toArray()).get("meanReturn");
            std = calcReference.getMeanReturnAndStandardDeviation(returnList.stream().mapToDouble(d -> d).toArray()).get("meanStandardDeviation");
            terminalStockValues = calcNativeReference.simulation(mean, std, this.timeSlice,
                    tempAsset.getCurrentStockPrice(), this.numberOfTrials, this.calculationsPerDay);

            /**
             * newly added part for simulation improvement
             */

            simulatedListBeforeChange = portfolio.getRecentSimulatedList().get(portfolio.getIncomingEventLabel());

            double lastPortfolioValue = portfolio.getCurrentTotalPortfolioValue();
            finalPortfolioValuesBeforeUpdate = portfolio.getReturnList();
            /**
             * following condition decide whether the calculation is done for first time or not.
             * final portfolio values before update is null indicating that the
             * calculation has never happened before.
             */
            if (lastPortfolioValue > 0 && finalPortfolioValuesBeforeUpdate != null) {
                //calculate latest portfolio value and store it in portfolio. set the latest stock price as recentStock price in the changed asset
                latestMarketValue = lastPortfolioValue - tempAsset.getPriceBeforeLastPrice() * numberOfShares + tempAsset.getCurrentStockPrice() * numberOfShares;
//                tempAsset.setPriceBeforeLastPrice(tempAsset.getCurrentStockPrice());
            /**
             * calculation may happen earlier but there can be assets where they have not been simulated before.
             * if the simulation has not been done before then
             */
                if (simulatedListBeforeChange != null) {
                    for (int i = 0; i < this.numberOfTrials; i++) {
                        unchangedSimulatedListCollection[i] = lastPortfolioValue - (simulatedListBeforeChange[i] * numberOfShares + finalPortfolioValuesBeforeUpdate[i]);
                    }
                    for (int i = 0; i < this.numberOfTrials; i++) {
                        finalPortfolioValues[i] = latestMarketValue - (terminalStockValues[i] * numberOfShares + unchangedSimulatedListCollection[i]);
                    }
                } else {
                    for (int i = 0; i < this.numberOfTrials; i++) {
                        unchangedSimulatedListCollection[i] = lastPortfolioValue - (finalPortfolioValuesBeforeUpdate[i]);
                    }
                    for (int i = 0; i < this.numberOfTrials; i++) {
                        finalPortfolioValues[i] = latestMarketValue - (terminalStockValues[i] * numberOfShares + unchangedSimulatedListCollection[i]);
                    }
                }

            } else {
                latestMarketValue = tempAsset.getCurrentStockPrice() * numberOfShares;
                for (int i = 0; i < this.numberOfTrials; i++) {
                    finalPortfolioValues[i] = latestMarketValue - (terminalStockValues[i] * numberOfShares);
                }
            }

            portfolio.setCurrentTotalPortfolioValue(latestMarketValue);
//set simulated list
            portfolio.getRecentSimulatedList().put(portfolio.getIncomingEventLabel(), terminalStockValues);

        } else {
            if (portfolio.getReturnList() != null) {
                finalPortfolioValues = portfolio.getReturnList();
            } else {
                return 0;
            }
        }

        portfolio.setReturnList(finalPortfolioValues);
        return new DescriptiveStatistics(finalPortfolioValues).getPercentile((1 - confidenceInterval) * 100);
    }

}
