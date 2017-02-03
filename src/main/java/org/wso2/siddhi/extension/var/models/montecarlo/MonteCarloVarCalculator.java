package org.wso2.siddhi.extension.var.models.montecarlo;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.wso2.siddhi.extension.var.models.util.Event;
import org.wso2.siddhi.extension.var.models.util.RealTimeVaRConstants;
import org.wso2.siddhi.extension.var.models.util.asset.Asset;
import org.wso2.siddhi.extension.var.models.util.asset.MonteCarloAsset;
import org.wso2.siddhi.extension.var.models.util.portfolio.MonteCarloPortfolio;
import org.wso2.siddhi.extension.var.models.util.portfolio.Portfolio;
import org.wso2.siddhi.extension.var.models.VaRCalculator;

import java.util.Map;

/**
 * Assumptions;
 * P    = finalPortfolioValuesBeforeAssetUpdate
 * P'   = finalPortfolioValues
 * A    = simulatedListBeforeAssetUpdate
 * A'   = generatedTerminalStockValues
 * MV   = previousPortfolioMarketValue
 * MV'  = latestMarketValue
 * <p>
 * According to the incremental calculation final portfolio value vector which used to calculate var will be as follows:
 * P'= ( MV'-MV ) - ( A-A') + P
 * for each event portfolio value vector(P') will be calculated based on the previous portfolio value (P).
 */

/**
 * Monte carlo simulation technique should be specified in the list of system properties as follows:
 * MONTECARLO_SIMULATION=AVX or MONTECARLO_SIMULATION=JAVA_CONCURRENT
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
        int currentSharesCount = 0;
        int previousSharesCount = 0;
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
            currentSharesCount = monteCarloPortfolio.getCurrentAssetQuantities(symbol);
            /**
             * get the number of shares that particular asset hold for corresponding portfolio before change happens
             */

            if (monteCarloPortfolio.getCurrentAssetQuantities(symbol) >= 0) {
                previousSharesCount = monteCarloPortfolio.getPreviousAssetQuantities(symbol);
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
                         * previousSharesCount + finalPortfolioValuesBeforeAssetUpdate[i]);
                         * finalPortfolioValues[i] = latestMarketValue - (generatedTerminalStockValues[i] *
                         * currentSharesCount +
                         * unchangedSimulatedListCollection[i]);
                         */
                        finalPortfolioValues[i] = latestMarketValue - (generatedTerminalStockValues[i] *
                                currentSharesCount + (previousPortfolioMarketValue -
                                (simulatedListBeforeAssetUpdate[i] * previousSharesCount +
                                        finalPortfolioValuesBeforeAssetUpdate[i])));
                    }
                } else {
                    for (int i = 0; i < horizontalSimulationsCount; i++) {
                        /**
                         * unchangedSimulatedListCollection[i] = previousPortfolioMarketValue -
                         * (finalPortfolioValuesBeforeAssetUpdate[i]);
                         * finalPortfolioValues[i] = latestMarketValue - (generatedTerminalStockValues[i] *
                         * currentSharesCount +
                         * unchangedSimulatedListCollection[i]);
                         */

                        finalPortfolioValues[i] = latestMarketValue - (generatedTerminalStockValues[i] *
                                currentSharesCount + (previousPortfolioMarketValue -
                                (finalPortfolioValuesBeforeAssetUpdate[i])));
                    }
                }
            } else {
                latestMarketValue = tempAsset.getCurrentStockPrice() * currentSharesCount;
                for (int i = 0; i < horizontalSimulationsCount; i++) {
                    finalPortfolioValues[i] = latestMarketValue - (generatedTerminalStockValues[i] *
                            currentSharesCount);
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
        String calculationTechnique = System.getenv("MONTECARLO_SIMULATION");
        MonteCarloNativeSimulation calculatorNativeReference = new MonteCarloNativeSimulation();
        MonteCarloStandardSimulation calculatorStandardReference = new MonteCarloStandardSimulation();

        if (historicalReturnValueList != null && historicalReturnValueList.length > 0) {
            Double mean = tempAsset.getMean();
            Double std = tempAsset.getStandardDeviation();
            tempAsset.setPreviousSimulatedList(tempAsset.getSimulatedList());

            if (calculationTechnique.equals(RealTimeVaRConstants.MONTE_CARLO_CALCULATION_TECHNIQUE_AVX)) {
                generatedTerminalStockValues = calculatorNativeReference.simulate(mean, std, timeSlice,
                        tempAsset.getCurrentStockPrice(), horizontalSimulationsCount, verticalSimulationsCount);
                tempAsset.setSimulatedList(generatedTerminalStockValues);
            } else if (calculationTechnique.equals(RealTimeVaRConstants.MONTE_CARLO_CALCULATION_TECHNIQUE_JAVA_CONCURRENT)) {
                calculatorStandardReference = new MonteCarloStandardSimulation(horizontalSimulationsCount);
                generatedTerminalStockValues = calculatorStandardReference.parallelSimulation(mean, std, timeSlice,
                        tempAsset.getCurrentStockPrice(), horizontalSimulationsCount, verticalSimulationsCount);
                tempAsset.setSimulatedList(generatedTerminalStockValues);
            } else {
                generatedTerminalStockValues = calculatorStandardReference.simulation(mean, std, timeSlice,
                        tempAsset.getCurrentStockPrice(), horizontalSimulationsCount, verticalSimulationsCount);
                tempAsset.setSimulatedList(generatedTerminalStockValues);
            }

        }
    }

    @Override
    public Portfolio createPortfolio(String id, Map<String, Integer> assets) {
        return new MonteCarloPortfolio(id, assets);
    }

    @Override
    public Asset createAsset(int windowSize) {
        return new MonteCarloAsset(windowSize);
    }

}
