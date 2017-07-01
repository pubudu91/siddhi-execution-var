package org.wso2.siddhi.extension.var.models.parametric;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

import java.util.*;

import com.google.common.collect.Table;
import com.google.common.collect.HashBasedTable;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.wso2.siddhi.extension.var.models.VaRCalculator;
import org.wso2.siddhi.extension.var.models.util.Event;
import org.wso2.siddhi.extension.var.models.util.asset.Asset;
import org.wso2.siddhi.extension.var.models.util.asset.ParametricAsset;
import org.wso2.siddhi.extension.var.models.util.portfolio.ParametricPortfolio;
import org.wso2.siddhi.extension.var.models.util.portfolio.Portfolio;

/*
 * Created by dilip on 30/06/16.
 */
public class ParametricVaRCalculator extends VaRCalculator {
    private final Table<String, String, Double> covarianceTable;

    /**
     * @param batchSize
     * @param confidenceInterval
     */
    public ParametricVaRCalculator(int batchSize, double confidenceInterval) {
        super(batchSize, confidenceInterval);
        covarianceTable = HashBasedTable.create();
    }

    /**
     * @return the var of the portfolio
     * Calculate var for the given portfolio using updated covariances and means
     */
    @Override
    public Double processData(Portfolio portfolio, Event event) {
        Asset asset = getAssetPool().get(event.getSymbol());

        if (asset.getNumberOfReturnValues() > 1) {

            RealMatrix varCovarMatrix = new Array2DRowRealMatrix(getVarCovarMatrix(portfolio));
            RealMatrix weightageMatrix = new Array2DRowRealMatrix(getWeightageMatrix(portfolio));
            RealMatrix meanMatrix = new Array2DRowRealMatrix(getMeanMatrix(portfolio));

            RealMatrix portfolioVarianceMatrix = weightageMatrix.multiply(varCovarMatrix).multiply(weightageMatrix
                    .transpose());
            RealMatrix portfolioMeanMatrix = weightageMatrix.multiply(meanMatrix.transpose());

            double portfolioVariance = portfolioVarianceMatrix.getData()[0][0];
            double portfolioMean = portfolioMeanMatrix.getData()[0][0];

            if (portfolioVariance == 0) { // a normal distribution cannot be defined when sd = 0
                return null;
            }

            double portfolioStandardDeviation = Math.sqrt(portfolioVariance);

            NormalDistribution normalDistribution = new NormalDistribution(portfolioMean, portfolioStandardDeviation);
            double zValue = normalDistribution.inverseCumulativeProbability(1 - getConfidenceInterval());
            double var = zValue * portfolio.getTotalPortfolioValue();

            return var;
        }
        return null;

    }

    /**
     * @param portfolio
     * @return Get VarCovar matrix for a given portfolio
     */
    private double[][] getVarCovarMatrix(Portfolio portfolio) {
        Set<String> keys = portfolio.getAssetListKeySet();
        int numberOfAssets = keys.size();
        String symbols[] = keys.toArray(new String[numberOfAssets]);
        double[][] varCovarMatrix = new double[numberOfAssets][numberOfAssets];
        double covariance;
        for (int i = 0; i < symbols.length; i++) {
            for (int j = i; j < symbols.length; j++) {
                covariance = covarianceTable.get(symbols[i], symbols[j]);
                varCovarMatrix[i][j] = covariance;
                if (i != j)
                    varCovarMatrix[j][i] = covariance;
            }
        }
        return varCovarMatrix;
    }

    /**
     * @param portfolio
     * @return Get weightage matrix for a given portfolio
     */
    private double[][] getWeightageMatrix(Portfolio portfolio) {
        Set<String> keys = portfolio.getAssetListKeySet();
        int numberOfAssets = keys.size();
        double[][] weightageMatrix = new double[1][numberOfAssets];

        final int[] i = {0};
        keys.forEach((symbol) -> {
            Asset asset = getAssetPool().get(symbol);
            weightageMatrix[0][i[0]] = asset.getCurrentStockPrice() * portfolio.getCurrentAssetQuantities(symbol) /
                    portfolio.getTotalPortfolioValue();
            i[0]++;
        });

        return weightageMatrix;
    }

    /**
     * @param portfolio
     * @return Get mean matrix for a given portfolio
     */
    private double[][] getMeanMatrix(Portfolio portfolio) {
        Set<String> keys = portfolio.getAssetListKeySet();
        double[][] meanMatrix = new double[1][keys.size()];

        final int[] i = {0};
        keys.forEach((symbol) -> {
            meanMatrix[0][i[0]] = getAssetPool().get(symbol).getMean();
            i[0]++;
        });

        return meanMatrix;
    }

    /**
     * @param symbol Update excess returns based on latest event
     */
    private void updateExcessReturnList(String symbol) {
        ParametricAsset asset = (ParametricAsset) getAssetPool().get(symbol);
        double[] returnValues = asset.getReturnValues();
        double mean = getAssetPool().get(symbol).getMean();
        double[] excessReturns = new double[returnValues.length];
        for (int i = 0; i < returnValues.length; i++) {
            excessReturns[i] = returnValues[i] - mean;
        }
        asset.setExcessReturns(excessReturns);
    }

    /**
     * @param symbol Update global co-variance table based on latest event
     */
    private void updateCovarianceTable(String symbol) {
        Set<String> keys = getAssetPool().keySet();
        double[] excessReturns = ((ParametricAsset) getAssetPool().get(symbol)).getExcessReturns();

        keys.forEach((iterateSymbol) -> {

            double[] iterateExcessReturns = ((ParametricAsset) getAssetPool().get(iterateSymbol)).getExcessReturns();
            int min;
            double covariance = 0.0;
            if (excessReturns.length > iterateExcessReturns.length)
                min = iterateExcessReturns.length;
            else
                min = excessReturns.length;

            for (int j = 0; j < min; j++) {
                covariance += excessReturns[j] * iterateExcessReturns[j];
            }
            covariance /= (getBatchSize() - 2);

            covarianceTable.put(symbol, iterateSymbol, covariance);
            covarianceTable.put(iterateSymbol, symbol, covariance);
        });

    }

    /**
     * @param symbol Update covariance table and excess returns based on latest event
     */
    @Override
    public void simulateChangedAsset(String symbol) {
        updateExcessReturnList(symbol);
        updateCovarianceTable(symbol);
    }

    @Override
    public Portfolio createPortfolio(String id, Map<String, Integer> assets) {
        return new ParametricPortfolio(id, assets);
    }

    @Override
    public Asset createAsset(int windowSize) {
        return new ParametricAsset(windowSize);
    }

}