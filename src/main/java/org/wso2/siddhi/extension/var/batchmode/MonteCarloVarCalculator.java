package org.wso2.siddhi.extension.var.batchmode;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.wso2.siddhi.extension.var.models.Asset;
import org.wso2.siddhi.extension.var.models.Portfolio;
import org.wso2.siddhi.extension.var.realtime.VaRPortfolioCalc;

import java.util.ArrayList;
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
        double[] terminalStockValues;
        double[] finalPortfolioValues = new double[numberOfTrials];
//        String[] keys = portfolio.getAssets().keySet().toArray(new String[portfolio.getAssets().size()]);
        Asset tempAsset;
        double[] returnList;
//        double todayMarketValue;
        int numberOfShares = 0;
        double latestMarketValue = 0;

        double[] unchangedSimulatedListCollection = new double[this.numberOfTrials];
        double[] simulatedListBeforeChange;
        double[] finalPortfolioValuesBeforeUpdate;

        for (int i = 0; i < numberOfTrials; i++) {
            finalPortfolioValues[i] = 0;
        }
//do simulation for all the assets in the portfolio
        //toggling should be local to a portfolio because every portfolio should follow this initial calculation
      /*
            do simulation for the changed asset only
             */
        tempAsset = assetList.get(this.symbol);
        returnList = tempAsset.getReturnValueSet().getValues();

        if (returnList != null && returnList.length > 0) {
            numberOfShares = portfolio.getAssets().get(this.symbol);

            terminalStockValues = tempAsset.getSimulatedList();

            /**
             * newly added part for simulation improvement
             */

            simulatedListBeforeChange = tempAsset.getPreviousSimulatedList();

            double lastPortfolioValue = portfolio.getMONTECARLO_SIMULATION_currentPortfolioValue();
            finalPortfolioValuesBeforeUpdate = portfolio.getMONTECARLO_SIMULATION_finalPortfolioValues();
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
            portfolio.setMONTECARLO_SIMULATION_currentPortfolioValue(latestMarketValue);

        } else {
            if (portfolio.getMONTECARLO_SIMULATION_finalPortfolioValues() != null) {
                finalPortfolioValues = portfolio.getMONTECARLO_SIMULATION_finalPortfolioValues();
            } else {
                return 0;
            }
        }

        portfolio.setMONTECARLO_SIMULATION_finalPortfolioValues(finalPortfolioValues);
        return new DescriptiveStatistics(finalPortfolioValues).getPercentile((1 - confidenceInterval) * 100);
    }

    @Override
    public double replaceAssetSimulation() {
        Asset tempAsset;
        double[] returnList;
        double[] terminalStockValues;
        tempAsset = assetList.get(this.symbol);
        returnList = tempAsset.getReturnValueSet().getValues();
        MonteCarloSimulation calcReference = new MonteCarloSimulation();
        MonteCarloNativeSimulation calcNativeReference = new MonteCarloNativeSimulation();

        if (returnList != null && returnList.length > 0) {
            Double mean = calcReference.getMeanReturnAndStandardDeviation(returnList).get("meanReturn");
            Double std = calcReference.getMeanReturnAndStandardDeviation(returnList).get("meanStandardDeviation");
            tempAsset.setPreviousSimulatedList(tempAsset.getSimulatedList());
            terminalStockValues = calcNativeReference.simulation(mean, std, this.timeSlice,
                    tempAsset.getCurrentStockPrice(), this.numberOfTrials, this.calculationsPerDay);
            tempAsset.setSimulatedList(terminalStockValues);
        }

        return 0;
    }

}
