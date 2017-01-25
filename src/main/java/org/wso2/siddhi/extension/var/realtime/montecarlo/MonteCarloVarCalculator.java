package org.wso2.siddhi.extension.var.realtime.montecarlo;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.wso2.siddhi.extension.var.models.Event;
import org.wso2.siddhi.extension.var.models.MonteCarloAsset;
import org.wso2.siddhi.extension.var.models.MonteCarloPortfolio;
import org.wso2.siddhi.extension.var.models.Portfolio;
import org.wso2.siddhi.extension.var.realtime.util.RealTimeVaRConstants;
import org.wso2.siddhi.extension.var.realtime.VaRCalculator;

/**
 * Created by flash on 6/29/16.
 */
public class MonteCarloVarCalculator extends VaRCalculator {

    private int numberOfSimulationsHorizontal;
    private int numberOfSimulationsVertical;
    private double timeSlice;

    public MonteCarloVarCalculator(int limit, double ci,
                                   int numberOfSimulationsHorizontal, int numberOfSimulationsVertical, double
                                           timeSlice) {
        super(limit, ci);
        this.numberOfSimulationsHorizontal = numberOfSimulationsHorizontal;
        this.numberOfSimulationsVertical = numberOfSimulationsVertical;
        this.timeSlice = timeSlice;
        setType(RealTimeVaRConstants.MONTE_CARLO);
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
        double[] finalPortfolioValues = new double[numberOfSimulationsHorizontal];
        MonteCarloAsset tempAsset;
        double[] historicalReturnValueList;
        int currentSharesCount = 0;
        int previousSharesCount = 0;
        double latestMarketValue = 0;

        double[] simulatedListBeforeAssetUpdate;
        double[] finalPortfolioValuesBeforeAssetUpdate;
        String symbol = event.getSymbol();

        tempAsset = (MonteCarloAsset) getAssetList().get(symbol);
        historicalReturnValueList = tempAsset.getReturnValues();
        if (historicalReturnValueList != null && historicalReturnValueList.length > 0) {
            /**
             * get the latest number of shares
             */
            currentSharesCount = monteCarloPortfolio.getCurrentSharesCount(symbol);
            /**
             * get the number of shares that particular asset hold for corresponding portfolio before change happens
             */
            if (monteCarloPortfolio.getShareCountMapBeforePortfolioUpdate().get(symbol) == null) {
                previousSharesCount = 0;
            } else {
                previousSharesCount = monteCarloPortfolio.getShareCountMapBeforePortfolioUpdate().get(symbol);
            }
            /**
             * Save the number of shares affected to the calculation for further compensation
             */
            monteCarloPortfolio.getShareCountMapBeforePortfolioUpdate().put(symbol, currentSharesCount);

            /**
             * retrieve latest simulated list
             */
            generatedTerminalStockValues = tempAsset.getSimulatedList();

            /**
             * newly added part for simulation improvement
             */

            simulatedListBeforeAssetUpdate = tempAsset.getPreviousSimulatedList();
            /**
             * accumulated portfolio value before the asset being changed
             */
            double previousPortfolioMarketValue = monteCarloPortfolio.getMonteCarloSimulationCurrentPortfolioValue();
            /**
             * get the final distribution vector before the portfolio being changed
             */
            finalPortfolioValuesBeforeAssetUpdate = monteCarloPortfolio
                    .getMonteCarloSimulationFinalPortfolioValueList();
            /**
             * following condition decide whether the calculation is done for first time or not.
             * final portfolio values before update is null indicating that the
             * calculation has never happened before.
             */
            if (previousPortfolioMarketValue > 0 && finalPortfolioValuesBeforeAssetUpdate != null) {
                //calculate latest portfolio value and store it in portfolio. set the latest stock price as
                // recentStock price in the changed asset
                latestMarketValue = previousPortfolioMarketValue - tempAsset.getPriceBeforeLastPrice() *
                        previousSharesCount + tempAsset.getCurrentStockPrice() * currentSharesCount;
//                tempAsset.setPriceBeforeLastPrice(tempAsset.getCurrentStockPrice());
                /**
                 * calculation may happen earlier but there can be assets where they have not been simulated before.
                 * if the simulation has not been done before then
                 */
                if (simulatedListBeforeAssetUpdate != null) {
                    for (int i = 0; i < this.numberOfSimulationsHorizontal; i++) {
                        /**
                         * unchangedSimulatedListCollection[i] = previousPortfolioMarketValue -
                         * (simulatedListBeforeAssetUpdate[i] *
                         * previousSharesCount + finalPortfolioValuesBeforeAssetUpdate[i]);
                         * finalPortfolioValues[i] = latestMarketValue - (generatedTerminalStockValues[i] *
                         * currentSharesCount +
                         * unchangedSimulatedListCollection[i]);
                         */

                        finalPortfolioValues[i] = latestMarketValue - (generatedTerminalStockValues[i] *
                                currentSharesCount +
                                (previousPortfolioMarketValue - (simulatedListBeforeAssetUpdate[i] *
                                        previousSharesCount +
                                        finalPortfolioValuesBeforeAssetUpdate[i])));
                    }
                } else {
                    for (int i = 0; i < this.numberOfSimulationsHorizontal; i++) {
                        /**
                         * unchangedSimulatedListCollection[i] = previousPortfolioMarketValue -
                         * (finalPortfolioValuesBeforeAssetUpdate[i]);
                         * finalPortfolioValues[i] = latestMarketValue - (generatedTerminalStockValues[i] *
                         * currentSharesCount +
                         * unchangedSimulatedListCollection[i]);
                         */

                        finalPortfolioValues[i] = latestMarketValue - (generatedTerminalStockValues[i] *
                                currentSharesCount +
                                (previousPortfolioMarketValue - (finalPortfolioValuesBeforeAssetUpdate[i])));
                    }
                }
            } else {
                latestMarketValue = tempAsset.getCurrentStockPrice() * currentSharesCount;
                for (int i = 0; i < this.numberOfSimulationsHorizontal; i++) {
                    finalPortfolioValues[i] = latestMarketValue - (generatedTerminalStockValues[i] *
                            currentSharesCount);
                }
            }
            monteCarloPortfolio.setMonteCarloSimulationCurrentPortfolioValue(latestMarketValue);
        } else {
            /**
             * new Asset being added later. portfolio can have distribution already but asset may not have historical
             * values.
             */
            if (monteCarloPortfolio.getMonteCarloSimulationFinalPortfolioValueList() != null) {
                finalPortfolioValues = monteCarloPortfolio.getMonteCarloSimulationFinalPortfolioValueList();
            } else {
                return null;
            }
        }

        monteCarloPortfolio.setMonteCarloSimulationFinalPortfolioValueList(finalPortfolioValues);
        return new DescriptiveStatistics(finalPortfolioValues).getPercentile((1 - getConfidenceInterval()) * 100);
    }

    @Override
    public void replaceAssetSimulation(String symbol) {
        MonteCarloAsset tempAsset;
        double[] historicalReturnValueList;
        double[] generatedTerminalStockValues;
        tempAsset = (MonteCarloAsset) getAssetList().get(symbol);
        historicalReturnValueList = tempAsset.getReturnValues();
        MonteCarloSimulation calculatorReference = new MonteCarloSimulation();
        MonteCarloNativeSimulation calcNativeReference = new MonteCarloNativeSimulation();

        if (historicalReturnValueList != null && historicalReturnValueList.length > 0) {
            Double mean = calculatorReference.getMeanReturnAndStandardDeviation(historicalReturnValueList).get
                    ("meanReturn");
            Double std = calculatorReference.getMeanReturnAndStandardDeviation(historicalReturnValueList).get
                    ("meanStandardDeviation");
            tempAsset.setPreviousSimulatedList(tempAsset.getSimulatedList());
            generatedTerminalStockValues = calcNativeReference.simulation(mean, std, timeSlice,
                    tempAsset.getCurrentStockPrice(), numberOfSimulationsHorizontal, numberOfSimulationsVertical);
            tempAsset.setSimulatedList(generatedTerminalStockValues);
        }
    }

}
