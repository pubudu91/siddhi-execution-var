package org.wso2.siddhi.extension.var.realtime.montecarlo;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.wso2.siddhi.extension.var.models.Event;
import org.wso2.siddhi.extension.var.models.MonteCarloAsset;
import org.wso2.siddhi.extension.var.models.MonteCarloPortfolio;
import org.wso2.siddhi.extension.var.models.Portfolio;
import org.wso2.siddhi.extension.var.realtime.VaRCalculator;

/**
 * Created by flash on 6/29/16.
 */
public class MonteCarloVarCalculator extends VaRCalculator {

    private int horizontalSimulationsCount;
    private int verticalSimulationsCount;
    private double timeSlice;

    public MonteCarloVarCalculator(int batchSize, double confidenceInterval,
                                   int horizontalSimulationsCount, int verticalSimulationsCount, double
                                           timeSlice) {
        super(batchSize, confidenceInterval);
        this.horizontalSimulationsCount = horizontalSimulationsCount;
        this.verticalSimulationsCount = verticalSimulationsCount;
        this.timeSlice = timeSlice;

    }

    /**
     * @param portfolio
     * @param event
     * @return
     */
    @Override
    public Double processData(Portfolio portfolio, Event event) {
        MonteCarloPortfolio monteCarloPortfolio = (MonteCarloPortfolio) portfolio;

        double[] generatedTerminalStockValues;
        double[] finalPortfolioValues = new double[horizontalSimulationsCount];
        MonteCarloAsset tempAsset;
        double[] historicalReturnValueList;
        int currentShareQuantity = 0;
        int previousShareQuantity = 0;
        double latestMarketValue = 0;

        double[] simulatedListBeforeAssetUpdate;
        double[] finalPortfolioValuesBeforeAssetUpdate;
        String symbol = event.getSymbol();

        tempAsset = (MonteCarloAsset) getAssetPool().get(symbol);
        historicalReturnValueList = tempAsset.getReturnValues();

        if (historicalReturnValueList != null && historicalReturnValueList.length > 0) {
            /**
             * get the latest number of shares
             */
            currentShareQuantity = monteCarloPortfolio.getCurrentSharesCount(symbol);
            /**
             * get the number of shares that particular asset hold for corresponding portfolio before change happens
             */

            if (monteCarloPortfolio.getCurrentSharesCount(symbol) >= 0) {
                previousShareQuantity = monteCarloPortfolio.getPreviousSharesCount(symbol);
            }

            /**
             * retrieve latest simulated list
             */
            generatedTerminalStockValues = tempAsset.getSimulatedList();


            simulatedListBeforeAssetUpdate = tempAsset.getPreviousSimulatedList();

            /**
             * get current portfolio market value
             */
            double currentPortfolioMarketValue = monteCarloPortfolio.getCurrentPortfolioValue();

            /**
             * get the final distribution vector before the portfolio being changed
             */
            finalPortfolioValuesBeforeAssetUpdate = monteCarloPortfolio
                    .getFinalPortfolioValueList();

            /**
             * following condition decide whether the calculation is done for first time or not.
             * final portfolio values before update is null indicating that the
             * calculation has never happened before.
             */

            if (currentPortfolioMarketValue > 0 && finalPortfolioValuesBeforeAssetUpdate != null) {

                /**
                 * accumulated portfolio market value before the asset being changed
                 */
                double previousPortfolioMarketValue = currentPortfolioMarketValue;
                /**
                 * calculate latest portfolio value and store inside portfolio. set the latest stock price as
                 * recentStock price in the changed asset
                 */
                latestMarketValue = portfolio.getTotalPortfolioValue();

                /**
                 * calculation may happen earlier but there can be assets where they have not been simulated before.
                 * if the simulate has not been done before then
                 */
                if (simulatedListBeforeAssetUpdate != null) {
                    for (int i = 0; i < horizontalSimulationsCount; i++) {
                        /**
                         * unchangedSimulatedListCollection[i] = previousPortfolioMarketValue -
                         * (simulatedListBeforeAssetUpdate[i] *
                         * previousShareQuantity + finalPortfolioValuesBeforeAssetUpdate[i]);
                         * finalPortfolioValues[i] = latestMarketValue - (generatedTerminalStockValues[i] *
                         * currentShareQuantity +
                         * unchangedSimulatedListCollection[i]);
                         */
                        finalPortfolioValues[i] = latestMarketValue - (generatedTerminalStockValues[i] *
                                currentShareQuantity + (previousPortfolioMarketValue -
                                (simulatedListBeforeAssetUpdate[i] * previousShareQuantity +
                                        finalPortfolioValuesBeforeAssetUpdate[i])));
                    }
                } else {
                    for (int i = 0; i < horizontalSimulationsCount; i++) {
                        /**
                         * unchangedSimulatedListCollection[i] = previousPortfolioMarketValue -
                         * (finalPortfolioValuesBeforeAssetUpdate[i]);
                         * finalPortfolioValues[i] = latestMarketValue - (generatedTerminalStockValues[i] *
                         * currentShareQuantity +
                         * unchangedSimulatedListCollection[i]);
                         */

                        finalPortfolioValues[i] = latestMarketValue - (generatedTerminalStockValues[i] *
                                currentShareQuantity + (previousPortfolioMarketValue -
                                (finalPortfolioValuesBeforeAssetUpdate[i])));
                    }
                }
            } else {
                latestMarketValue = tempAsset.getCurrentStockPrice() * currentShareQuantity;
                for (int i = 0; i < horizontalSimulationsCount; i++) {
                    finalPortfolioValues[i] = latestMarketValue - (generatedTerminalStockValues[i] *
                            currentShareQuantity);
                }
            }
            monteCarloPortfolio.setCurrentPortfolioValue(latestMarketValue);
        } else {
            /**
             * new Asset being added later. portfolio can have distribution already but asset may not have historical
             * values for itself.
             */
            if (monteCarloPortfolio.getFinalPortfolioValueList() != null) {
                finalPortfolioValues = monteCarloPortfolio.getFinalPortfolioValueList();
            } else {
                return null;
            }
        }

        monteCarloPortfolio.setFinalPortfolioValueList(finalPortfolioValues);
        return new DescriptiveStatistics(finalPortfolioValues).getPercentile((1 - getConfidenceInterval()) * 100);
    }

    @Override
    public void simulateChangedAsset(String symbol) {
        MonteCarloAsset tempAsset;
        double[] historicalReturnValueList;
        double[] generatedTerminalStockValues;
        tempAsset = (MonteCarloAsset) getAssetPool().get(symbol);
        historicalReturnValueList = tempAsset.getReturnValues();

        MonteCarloNativeSimulation calculatorNativeReference = new MonteCarloNativeSimulation();

        if (historicalReturnValueList != null && historicalReturnValueList.length > 0) {
            Double mean = tempAsset.getMean();
            Double std = tempAsset.getStandardDeviation();
            tempAsset.setPreviousSimulatedList(tempAsset.getSimulatedList());
            generatedTerminalStockValues = calculatorNativeReference.simulate(mean, std, timeSlice,
                    tempAsset.getCurrentStockPrice(), horizontalSimulationsCount, verticalSimulationsCount);
            tempAsset.setSimulatedList(generatedTerminalStockValues);
        }
    }

}
