package org.wso2.siddhi.extension.var.models.parametric;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

import java.util.*;

import com.google.common.collect.Table;
import com.google.common.collect.HashBasedTable;
import org.wso2.siddhi.extension.var.models.VaRCalculator;
import org.wso2.siddhi.extension.var.models.util.Event;
import org.wso2.siddhi.extension.var.models.util.asset.Asset;
import org.wso2.siddhi.extension.var.models.util.asset.ParametricAsset;
import org.wso2.siddhi.extension.var.models.util.portfolio.ParametricPortfolio;
import org.wso2.siddhi.extension.var.models.util.portfolio.Portfolio;

//TODO check direct double comparison

/**
 * Created by dilip on 30/06/16.
 * VaR  : value at risk
 * PV   : portfolio variance
 * PM   : portfolio mean
 * VCV  : variance co-variance
 * PSD  : portfolio standard deviation
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

            RealMatrix vcvMatrix = new Array2DRowRealMatrix(getVCVMatrix(portfolio));
            RealMatrix weightageMatrix = new Array2DRowRealMatrix(getWeightageMatrix(portfolio));
            RealMatrix meanMatrix = new Array2DRowRealMatrix(getMeanMatrix(portfolio));

            RealMatrix pvMatrix = weightageMatrix.multiply(vcvMatrix).multiply(weightageMatrix.transpose());
            RealMatrix pmMatrix = weightageMatrix.multiply(meanMatrix.transpose());

            double pv = pvMatrix.getData()[0][0];
            double pm = pmMatrix.getData()[0][0];

            if (pv == 0) { // a normal distribution cannot be defined when sd = 0
                return null;
            }

            double psd = Math.sqrt(pv);

            NormalDistribution normalDistribution = new NormalDistribution(pm, psd);
            double zValue = normalDistribution.inverseCumulativeProbability(1 - getConfidenceInterval());
            double var = zValue * portfolio.getTotalPortfolioValue();

            return var;
        }
        return null;
    }

    /**
     * @param portfolio
     * @return Get VCV matrix for a given portfolio
     */
    private double[][] getVCVMatrix(Portfolio portfolio) {
        Set<String> keys = portfolio.getAssetListKeySet();
        int numberOfAssets = keys.size();
        String symbols[] = keys.toArray(new String[numberOfAssets]);
        double[][] vcvMatrix = new double[numberOfAssets][numberOfAssets];
        double covariance;
        for (int i = 0; i < symbols.length; i++) {
            for (int j = i; j < symbols.length; j++) {
                covariance = covarianceTable.get(symbols[i], symbols[j]);
                vcvMatrix[i][j] = covariance;
                if (i != j)
                    vcvMatrix[j][i] = covariance;
            }
        }
        return vcvMatrix;
    }

    /**
     * @param portfolio
     * @return Get weightage matrix for a given portfolio
     */
    private double[][] getWeightageMatrix(Portfolio portfolio) {
        Set<String> keys = portfolio.getAssetListKeySet();
        int numberOfAssets = keys.size();
        double[][] weightageMatrix = new double[1][numberOfAssets];
        int i = 0;
        Asset temp;
        String symbol;
        Iterator<String> itr = keys.iterator();
        while (itr.hasNext()) {
            symbol = itr.next();
            temp = getAssetPool().get(symbol);
            weightageMatrix[0][i] = temp.getCurrentStockPrice() * portfolio.getCurrentAssetQuantities(symbol) /
                    portfolio.getTotalPortfolioValue();
            i++;
        }
        return weightageMatrix;
    }

    /**
     * @param portfolio
     * @return Get mean matrix for a given portfolio
     */
    private double[][] getMeanMatrix(Portfolio portfolio) {
        Set<String> keys = portfolio.getAssetListKeySet();
        double[][] meanMatrix = new double[1][keys.size()];
        int i = 0;
        Iterator<String> itr = keys.iterator();
        while (itr.hasNext()) {
            meanMatrix[0][i] = getAssetPool().get(itr.next()).getMean();
            i++;
        }
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
        double[] mainExcessReturns = ((ParametricAsset) getAssetPool().get(symbol)).getExcessReturns();
        int min;
        double covariance;
        String tempSymbol;
        Iterator<String> itr = keys.iterator();
        while (itr.hasNext()) {
            tempSymbol = itr.next();
            covariance = 0.0;
            double[] tempExcessReturns = ((ParametricAsset) getAssetPool().get(tempSymbol)).getExcessReturns();
            if (mainExcessReturns.length > tempExcessReturns.length)
                min = tempExcessReturns.length;
            else
                min = mainExcessReturns.length;

            for (int j = 0; j < min; j++) {
                covariance += mainExcessReturns[j] * tempExcessReturns[j];
            }
            covariance = covariance / (getBatchSize() - 2);

            covarianceTable.put(symbol, tempSymbol, covariance);
            covarianceTable.put(tempSymbol, symbol, covariance);
        }
    }

    /**
     * @param symbol Update covariance table and excess returns based on latest event
     */
    @Override
    public void simulateChangedAsset(String symbol) {
        updateExcessReturnList(symbol);
        updateCovarianceTable(symbol);
    }

    //TODO Check whether the implementation is correct
    @Override
    public Portfolio createPortfolio(String id, Map<String, Integer> assets) {
        return new ParametricPortfolio(id, assets);
    }

    //TODO Check whether the implementation is correct
    @Override
    public Asset createAsset(int windowSize) {
        return new ParametricAsset(windowSize);
    }


}