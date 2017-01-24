package org.wso2.siddhi.extension.var.realtime.montecarlo;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.wso2.siddhi.extension.var.models.MonteCarloAsset;
import org.wso2.siddhi.extension.var.models.MonteCarloPortfolio;
import org.wso2.siddhi.extension.var.models.Portfolio;
import org.wso2.siddhi.extension.var.realtime.util.RealTimeVaRConstants;
import org.wso2.siddhi.extension.var.realtime.VaRCalculator;

/**
 * Created by flash on 6/29/16.
 */
public class MonteCarloVarCalculator extends VaRCalculator {

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
        setType(RealTimeVaRConstants.MONTE_CARLO);
    }

    /**
     * calculate the var metric using monte carlo simulation
     *
     * @return
     */
    @Override
    public Object processData(Portfolio portfolio) {
        MonteCarloPortfolio monteCarloPortfolio = (MonteCarloPortfolio) portfolio;

        double[] terminalStockValues;
        double[] finalPortfolioValues = new double[numberOfTrials];
//        String[] keys = portfolio.getAssets().keySet().toArray(new String[portfolio.getAssets().size()]);
        MonteCarloAsset tempAsset;
        double[] returnList;
//        double todayMarketValue;
        int numberOfShares = 0;
        int numberOfSharesBeforeChange = 0;
        double latestMarketValue = 0;

        double[] simulatedListBeforeChange;
        double[] finalPortfolioValuesBeforeUpdate;

//do simulation for all the assets in the portfolio
        //toggling should be local to a portfolio because every portfolio should follow this initial calculation
      /*
            do simulation for the changed asset only
             */
        tempAsset = (MonteCarloAsset) getAssetList().get(getSymbol());
        returnList = tempAsset.getReturnValueSet().getValues();
        if (returnList != null && returnList.length > 0) {
            /**
             * get the latest number of shares
             */
            numberOfShares = monteCarloPortfolio.getCurrentShare(getSymbol());
            /**
             * get the number of shares that particular asset hold for corresponding portfolio before change happens
             */
            if (monteCarloPortfolio.getAssetSharesBeforeChange().get(getSymbol()) == null) {
                numberOfSharesBeforeChange = 0;
            } else {
                numberOfSharesBeforeChange = monteCarloPortfolio.getAssetSharesBeforeChange().get(getSymbol());
            }
            /**
             * Save the number of shares affected to the calculation for further compensation
             */
            monteCarloPortfolio.getAssetSharesBeforeChange().put(getSymbol(), numberOfShares);

            /**
             * retrieve latest simulated list
             */
            terminalStockValues = tempAsset.getSimulatedList();

            /**
             * newly added part for simulation improvement
             */

            simulatedListBeforeChange = tempAsset.getPreviousSimulatedList();
            /**
             * accumulated portfolio value before the asset being changed
             */
            double lastPortfolioValue = monteCarloPortfolio.getMonteCarlo_Simulation_currentPortfolioValue();
            /**
             * get the final distribution vector before the portfolio being changed
             */
            finalPortfolioValuesBeforeUpdate = monteCarloPortfolio.getMonteCarlo_Simulation_finalPortfolioValueList();
            /**
             * following condition decide whether the calculation is done for first time or not.
             * final portfolio values before update is null indicating that the
             * calculation has never happened before.
             */
            if (lastPortfolioValue > 0 && finalPortfolioValuesBeforeUpdate != null) {
                //calculate latest portfolio value and store it in portfolio. set the latest stock price as recentStock price in the changed asset
                latestMarketValue = lastPortfolioValue - tempAsset.getPriceBeforeLastPrice() * numberOfSharesBeforeChange + tempAsset.getCurrentStockPrice() * numberOfShares;
//                tempAsset.setPriceBeforeLastPrice(tempAsset.getCurrentStockPrice());
                /**
                 * calculation may happen earlier but there can be assets where they have not been simulated before.
                 * if the simulation has not been done before then
                 */
                if (simulatedListBeforeChange != null) {
                    for (int i = 0; i < this.numberOfTrials; i++) {
                        /**
                         * unchangedSimulatedListCollection[i] = lastPortfolioValue - (simulatedListBeforeChange[i] * numberOfSharesBeforeChange + finalPortfolioValuesBeforeUpdate[i]);
                         * finalPortfolioValues[i] = latestMarketValue - (terminalStockValues[i] * numberOfShares + unchangedSimulatedListCollection[i]);
                         */

                        finalPortfolioValues[i] = latestMarketValue - (terminalStockValues[i] * numberOfShares +
                                (lastPortfolioValue - (simulatedListBeforeChange[i] * numberOfSharesBeforeChange + finalPortfolioValuesBeforeUpdate[i])));
                    }
                } else {
                    for (int i = 0; i < this.numberOfTrials; i++) {
                        /**
                         * unchangedSimulatedListCollection[i] = lastPortfolioValue - (finalPortfolioValuesBeforeUpdate[i]);
                         * finalPortfolioValues[i] = latestMarketValue - (terminalStockValues[i] * numberOfShares + unchangedSimulatedListCollection[i]);
                         */

                        finalPortfolioValues[i] = latestMarketValue - (terminalStockValues[i] * numberOfShares +
                                (lastPortfolioValue - (finalPortfolioValuesBeforeUpdate[i])));
                    }
                }
            } else {
                latestMarketValue = tempAsset.getCurrentStockPrice() * numberOfShares;
                for (int i = 0; i < this.numberOfTrials; i++) {
                    finalPortfolioValues[i] = latestMarketValue - (terminalStockValues[i] * numberOfShares);
                }
            }
            monteCarloPortfolio.setMonteCarlo_Simulation_currentPortfolioValue(latestMarketValue);

        } else {
            if (monteCarloPortfolio.getMonteCarlo_Simulation_finalPortfolioValueList() != null) {
                finalPortfolioValues = monteCarloPortfolio.getMonteCarlo_Simulation_finalPortfolioValueList();
            } else {
                return 0;
            }
        }

        monteCarloPortfolio.setMonteCarlo_Simulation_finalPortfolioValueList(finalPortfolioValues);
        return new DescriptiveStatistics(finalPortfolioValues).getPercentile((1 - getConfidenceInterval()) * 100);
    }

    @Override
    public double replaceAssetSimulation(Double removedEvent) {
        MonteCarloAsset tempAsset;
        double[] returnList;
        double[] terminalStockValues;
        tempAsset = (MonteCarloAsset) getAssetList().get(getSymbol());
        returnList = tempAsset.getReturnValueSet().getValues();
        MonteCarloSimulation calcReference = new MonteCarloSimulation();
        MonteCarloNativeSimulation calcNativeReference = new MonteCarloNativeSimulation();

        if (returnList != null && returnList.length > 0) {
            Double mean = calcReference.getMeanReturnAndStandardDeviation(returnList).get("meanReturn");
            Double std = calcReference.getMeanReturnAndStandardDeviation(returnList).get("meanStandardDeviation");
            tempAsset.setPreviousSimulatedList(tempAsset.getSimulatedList());
            terminalStockValues = calcNativeReference.simulation(mean, std, timeSlice,
                    tempAsset.getCurrentStockPrice(), numberOfTrials, calculationsPerDay);
            tempAsset.setSimulatedList(terminalStockValues);
        }
        return 0;
    }

}
