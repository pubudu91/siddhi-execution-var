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

/**
 * Created by dilip on 30/06/16.
 */
public class ParametricVaRCalculator extends VaRCalculator {
    private final Table<String, String, Double> covarianceTable;
    private double portfolioValue;

    /**
     * @param limit
     * @param ci
     */
    public ParametricVaRCalculator(int limit, double ci) {
        super(limit, ci);
        covarianceTable = HashBasedTable.create();
        setType(RealTimeVaRConstants.PARAMETRIC);
    }

    /**
     * @return the var of the portfolio
     */
    @Override
    public Double processData(Portfolio portfolio, Event event) {
        String symbol = event.getSymbol();
        Asset asset = getAssetList().get(symbol);
        if (asset.getNumberOfReturnValues() > 0)
            return incrementalParametricVaR(portfolio);
        else
            return null;
        //return batchModeParametricVaR(portfolio);
    }

    /**
     * @param portfolio
     * @return
     */
    private Double incrementalParametricVaR(Portfolio portfolio) {

        //variable names start with capitals
        /** create matrices from excess returns, means  and weight-age **/
        RealMatrix matrixVCV = new Array2DRowRealMatrix(getVCVMatrix(portfolio));
        RealMatrix matrixWeightage = new Array2DRowRealMatrix(getWeightageMatrix(portfolio));
        RealMatrix matrixMean = new Array2DRowRealMatrix(getMeanMatrix(portfolio));

        /** matrix multiplications using apache math library **/
        RealMatrix matrixPV = matrixWeightage.multiply(matrixVCV).multiply(matrixWeightage.transpose());
        RealMatrix matrixPM = matrixWeightage.multiply(matrixMean.transpose());

        double pv = matrixPV.getData()[0][0];
        if (pv == 0) {                  // NormalDistribution throws an exception when ps = 0
            return null;
        }

        double ps = Math.sqrt(pv);
        double pm = matrixPM.getData()[0][0];
        NormalDistribution n = new NormalDistribution(pm, ps);
        double var = n.inverseCumulativeProbability(1 - getConfidenceInterval());

        return var * portfolioValue;
    }

    /**
     * @param portfolio
     * @return
     */
    private double[][] getVCVMatrix(Portfolio portfolio) {
        Set<String> keys = portfolio.getAssetListKeySet();
        int numberOfAssets = keys.size();
        String symbols[] = keys.toArray(new String[numberOfAssets]);
        double[][] VCVMatrix = new double[numberOfAssets][numberOfAssets];
        double covariance;
        for (int i = 0; i < symbols.length; i++) {
            for (int j = i; j < symbols.length; j++) {
                covariance = covarianceTable.get(symbols[i], symbols[j]);
                VCVMatrix[i][j] = covariance;
                if (i != j)
                    VCVMatrix[j][i] = covariance;
            }
        }
        return VCVMatrix;
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
            temp = getAssetList().get(symbol);
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
            mean = getAssetList().get(itr.next()).getMean();
            meanMatrix[0][i] = mean;
            i++;
        }
        return meanMatrix;
    }

    /**
     * @param symbol
     */
    private void updateExcessReturnList(String symbol) {
        ParametricAsset asset = (ParametricAsset) getAssetList().get(symbol);
        double[] returnValues = asset.getReturnValues();
        double mean = getAssetList().get(symbol).getMean();
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
        Set<String> keys = getAssetList().keySet();
        String symbols[] = keys.toArray(new String[getAssetList().size()]);
        double[] mainExcessReturns = ((ParametricAsset) getAssetList().get(symbol)).getExcessReturns();
        int min;
        double covariance;
        for (int i = 0; i < symbols.length; i++) {
            covariance = 0.0;
            double[] tempExcessReturns = ((ParametricAsset) getAssetList().get(symbols[i])).getExcessReturns();
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

    @Override
    public void simulateChangedAsset(String symbol) {
        updateExcessReturnList(symbol);
        updateCovarianceTable(symbol);
    }

}