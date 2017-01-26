package org.wso2.siddhi.extension.var.realtime.parametric;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.wso2.siddhi.extension.var.models.*;

import java.util.*;

import com.google.common.collect.Table;
import com.google.common.collect.HashBasedTable;
import org.wso2.siddhi.extension.var.realtime.VaRCalculator;
import org.wso2.siddhi.extension.var.realtime.util.RealTimeVaRConstants;

//TODO check direct double comparison

/**
 * Created by dilip on 30/06/16.
 */
public class ParametricVaRCalculator extends VaRCalculator {
    private final Table<String, String, Double> covarianceTable;
    private double portfolioValue;

    /**
     * @param batchSize
     * @param confidenceInterval
     */
    public ParametricVaRCalculator(int batchSize, double confidenceInterval) {
        super(batchSize, confidenceInterval);
        covarianceTable = HashBasedTable.create();
        setType(RealTimeVaRConstants.PARAMETRIC);
    }

    /**
     * @return the var of the portfolio
     * Calculate the contribution of the changed asset to the portfolio and then adjust the previous VaR value
     * using Historical data
     */
    @Override
    public Double processData(Portfolio portfolio, Event event) {

        Asset asset = getAssetPool().get(event.getSymbol());

        //for parametric simulation there should be at least one return value
        //TODO check > 0 condition
        if (asset.getNumberOfReturnValues() > 0) {
            //TODO variable names start with capitals
            /** create matrices from excess returns, means  and weight-age **/
            RealMatrix matrixVCV = new Array2DRowRealMatrix(getVCVMatrix(portfolio));
            RealMatrix matrixWeightage = new Array2DRowRealMatrix(getWeightageMatrix(portfolio));
            RealMatrix matrixMean = new Array2DRowRealMatrix(getMeanMatrix(portfolio));

            /** matrix multiplications using apache math library **/
            RealMatrix matrixPV = matrixWeightage.multiply(matrixVCV).multiply(matrixWeightage.transpose());
            RealMatrix matrixPM = matrixWeightage.multiply(matrixMean.transpose());

            double pv = matrixPV.getData()[0][0];
            /** NormalDistribution throws an exception when ps = 0 **/
            if (pv == 0) {
                return null;
            }

            double ps = Math.sqrt(pv);

            double pm = matrixPM.getData()[0][0];

            NormalDistribution n = new NormalDistribution(pm, ps);

            double var = n.inverseCumulativeProbability(1 - getConfidenceInterval());

            return var * portfolioValue;

        }

        return null;
    }

    /**
     * @param portfolio
     * @return
     */
    private double[][] getVCVMatrix(Portfolio portfolio) {
        Set<String> keys = portfolio.getAssetListKeySet();
        int numberOfAssets = keys.size();
        String symbols[] = keys.toArray(new String[numberOfAssets]);
        double[][] matrixVCV = new double[numberOfAssets][numberOfAssets];
        double covariance;
        for (int i = 0; i < symbols.length; i++) {
            for (int j = i; j < symbols.length; j++) {
                covariance = covarianceTable.get(symbols[i], symbols[j]);
                matrixVCV[i][j] = covariance;
                if (i != j)
                    matrixVCV[j][i] = covariance;
            }
        }
        return matrixVCV;
    }

    /**
     * @param portfolio
     * @return
     */
    private double[][] getWeightageMatrix(Portfolio portfolio) {

        Set<String> keys = portfolio.getAssetListKeySet();
        int numberOfAssets = keys.size();
        double[][] weighageMatrix = new double[1][numberOfAssets];
        double portfolioValue = 0.0;
        int i = 0;
        Asset temp;
        Object symbol;
        Iterator itr = keys.iterator();
        while (itr.hasNext()) {
            symbol = itr.next();
            temp = getAssetPool().get(symbol);
            weighageMatrix[0][i] = temp.getCurrentStockPrice() * portfolio.getCurrentSharesCount((String)
                    symbol);
            portfolioValue = portfolioValue + weighageMatrix[0][i];
            i++;
        }
        this.portfolioValue = portfolioValue;
        for (int j = 0; j < numberOfAssets; j++) {
            weighageMatrix[0][j] = weighageMatrix[0][j] / portfolioValue;
        }
        return weighageMatrix;
    }

    /**
     * @param portfolio
     * @return
     */
    private double[][] getMeanMatrix(Portfolio portfolio) {
        Set<String> keys = portfolio.getAssetListKeySet();
        double[][] meanMatrix = new double[1][keys.size()];
        Double mean;
        int i = 0;
        Iterator itr = keys.iterator();
        while (itr.hasNext()) {
            mean = getAssetPool().get(itr.next()).getMean();
            meanMatrix[0][i] = mean;
            i++;
        }
        return meanMatrix;
    }

    /**
     * @param symbol
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
     * @param symbol
     */
    private void updateCovarianceTable(String symbol) {
        Set<String> keys = getAssetPool().keySet();
        String symbols[] = keys.toArray(new String[getAssetPool().size()]);
        double[] mainExcessReturns = ((ParametricAsset) getAssetPool().get(symbol)).getExcessReturns();
        int min;
        double covariance;
        for (int i = 0; i < symbols.length; i++) {
            covariance = 0.0;
            double[] tempExcessReturns = ((ParametricAsset) getAssetPool().get(symbols[i])).getExcessReturns();
            if (mainExcessReturns.length > tempExcessReturns.length)
                min = tempExcessReturns.length;
            else
                min = mainExcessReturns.length;

            if (min > 1) {
                for (int j = 0; j < min; j++) {
                    covariance += mainExcessReturns[j] * tempExcessReturns[j];
                }
                covariance = covariance / (min - 1);
            }
            covarianceTable.put(symbol, symbols[i], covariance);
            covarianceTable.put(symbols[i], symbol, covariance);
        }
    }

    /**
     *
     * @param symbol
     */
    @Override
    public void simulateChangedAsset(String symbol) {
        updateExcessReturnList(symbol);
        updateCovarianceTable(symbol);
    }

}