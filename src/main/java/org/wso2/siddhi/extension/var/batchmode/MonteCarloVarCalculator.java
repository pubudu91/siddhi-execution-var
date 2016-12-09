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
     * calculate the var metric using monte carlo simulationvar metric
     * this method only utilize new asset t calculate latest
     *
     * @return
     */
    @Override
    public Object processData(Portfolio portfolio) {
        double[] terminalStockValues;
        double[] finalPortfolioValues = new double[numberOfTrials];
        Asset tempAsset;
        LinkedList<Double> returnList;
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
/**
 * get changed asset in a portfolio.
 * get return list of that portfolio.
 *
 */
        tempAsset = assetList.get(portfolio.getIncomingEventLabel());
        returnList = tempAsset.getLatestReturnValues();
/**
 * check the existence of the return list.
 */
        if (returnList != null && returnList.size() > 0) {
            numberOfShares = portfolio.getAssets().get(portfolio.getIncomingEventLabel());

            mean = calcReference.getMeanReturnAndStandardDeviation(returnList.stream().mapToDouble(d -> d).toArray()).get("meanReturn");
            std = calcReference.getMeanReturnAndStandardDeviation(returnList.stream().mapToDouble(d -> d).toArray()).get("meanStandardDeviation");
            terminalStockValues = calcNativeReference.simulation(mean, std, this.timeSlice,
                    tempAsset.getCurrentStockPrice(), this.numberOfTrials, this.calculationsPerDay);
/**
 * get immediate simulate list of the changed asset
 */
            simulatedListBeforeChange = portfolio.getRecentSimulatedList().get(portfolio.getIncomingEventLabel());
/**
 * get previous total portfolio value and previous portfolio return values.(values which were inserted into distribution)
 */
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
                    /**
                     * get the effect from unchanged assets
                     */
                    for (int i = 0; i < this.numberOfTrials; i++) {
                        unchangedSimulatedListCollection[i] = lastPortfolioValue - (simulatedListBeforeChange[i] * numberOfShares + finalPortfolioValuesBeforeUpdate[i]);
                    }
                    /**
                     * add changed asset effect + unchanged asset effect
                     */
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
//set latest portfolio value
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
// set set latest portfolio value list(which is used to build distribution)
        portfolio.setReturnList(finalPortfolioValues);
        return new DescriptiveStatistics(finalPortfolioValues).getPercentile((1 - confidenceInterval) * 100);
    }

}
