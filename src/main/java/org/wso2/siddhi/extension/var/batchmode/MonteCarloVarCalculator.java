package org.wso2.siddhi.extension.var.batchmode;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.wso2.siddhi.extension.var.models.Asset;
import org.wso2.siddhi.extension.var.models.Portfolio;
import org.wso2.siddhi.extension.var.realtime.VaRPortfolioCalc;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by flash on 6/29/16.
 */
public class MonteCarloVarCalculator extends VaRPortfolioCalc {

    private int numberOfTrials;
    private int calculationsPerDay;
    private double timeSlice;

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
    protected Object processData(Portfolio portfolio) {
        double[] terminalStockValues;
        double[] finalPortfolioValues = new double[numberOfTrials];
        String[] keys = portfolio.getAssets().keySet().toArray(new String[portfolio.getAssets().size()]);
        Asset tempAsset;
        LinkedList<Double> historicalValues;
        double todayMarketValue;

        for (int i = 0; i < numberOfTrials; i++) {
            finalPortfolioValues[i] = 0;
        }

        for (int i = 0; i < keys.length; i++) {

            tempAsset = assetList.get(keys[i]);
            todayMarketValue = (tempAsset.getHistoricalValues().getLast() * tempAsset.getNumberOfShares());
            historicalValues = tempAsset.getHistoricalValues();
            terminalStockValues = new MonteCarloSimulation().simulation(this.numberOfTrials, this.calculationsPerDay,
                    historicalValues.stream().mapToDouble(d -> d).toArray(), this.timeSlice, historicalValues.getLast());

            for (int j = 0; j < terminalStockValues.length; j++) {
                finalPortfolioValues[j] += (todayMarketValue - (terminalStockValues[j] * tempAsset.getNumberOfShares()));
            }
        }

        return new DescriptiveStatistics(finalPortfolioValues).getPercentile((1 - confidenceInterval) * 100);
    }

}
