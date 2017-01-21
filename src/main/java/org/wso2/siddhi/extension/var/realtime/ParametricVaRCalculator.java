package org.wso2.siddhi.extension.var.realtime;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.wso2.siddhi.extension.var.models.Asset;
import org.wso2.siddhi.extension.var.models.ParametricAsset;
import org.wso2.siddhi.extension.var.models.Portfolio;

import java.util.*;

import com.google.common.collect.Table;
import com.google.common.collect.HashBasedTable;

/**
 * Created by dilip on 30/06/16.
 */
public class ParametricVaRCalculator extends VaRPortfolioCalc {
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

    @Override
    public void addEvent(Object[] event) {

        if (event[0] != null || event[1] != null) {
            setPortfolioID(((Number) event[0]).intValue());
            setShares(((Number) event[1]).intValue());
            setSymbol(event[2].toString());
            setPrice(((Number) event[3]).doubleValue());
            updatePortfolioPool();
        }

        setSymbol(event[2].toString());
        setPrice(((Number) event[3]).doubleValue());

        ParametricAsset temp;
        if (!getAssetList().containsKey(getSymbol())) {
            temp = new ParametricAsset();
            temp.setCurrentStockPrice(getPrice());
            temp.setReturnValueSet(getBatchSize());
            getAssetList().put(getSymbol(), temp);
        } else {
            temp = (ParametricAsset) getAssetList().get(getSymbol());
            temp.setPriceBeforeLastPrice(temp.getCurrentStockPrice());
            temp.setCurrentStockPrice(getPrice());
            double newReturn = Math.log(temp.getCurrentStockPrice() / temp.getPriceBeforeLastPrice());
            double latestMean;
            if (temp.getNumberOfReturnValues() == (getBatchSize() - 1)) {
                double oldReturn = temp.addReturnValue(newReturn);
                latestMean = temp.getMean() + ((newReturn - oldReturn) / (getBatchSize() - 1));
            } else {
                temp.addReturnValue(newReturn);
                int numberOfReturnValues = temp.getNumberOfReturnValues();
                latestMean = (temp.getMean() * (numberOfReturnValues - 1) + newReturn) / numberOfReturnValues;
            }
            temp.setMean(latestMean);
            updateExcessReturnList(getSymbol());
            updateCovarianceTable(getSymbol());
        }
    }

    /**
     * @return the var of the portfolio
     */
    @Override
    public Object processData(Portfolio portfolio) {
        return incrementalParametricVaR(portfolio);
        //return batchModeParametricVaR(portfolio);
    }

    private Object incrementalParametricVaR(Portfolio portfolio) {

        double[][] VCV = getVCV(portfolio);
        double[][] weightage = getWeightage(portfolio);
        double[][] means = getMeans(portfolio);

        /** create matrices from excess returns, means  and weight-age **/
        RealMatrix VCVMatrix = new Array2DRowRealMatrix(VCV);
        RealMatrix weightageMatrix = new Array2DRowRealMatrix(weightage);
        RealMatrix meanMatrix = new Array2DRowRealMatrix(means);

        /** matrix multiplications using apache math library **/
        RealMatrix PV = weightageMatrix.multiply(VCVMatrix).multiply(weightageMatrix.transpose());
        RealMatrix PM = weightageMatrix.multiply(meanMatrix.transpose());

        double pv = PV.getData()[0][0];
        double pm = PM.getData()[0][0];

        /** NormalDistribution throws an exception when ps = 0, this condition return pm when ps = 0 **/
        if (pv == 0) {
            //System.out.print(portfolio.getID() + " : " + pm + " ");
            return pm;
        }

        double ps = Math.sqrt(pv);
        NormalDistribution n = new NormalDistribution(pm, ps);
        double var = n.inverseCumulativeProbability(1 - getConfidenceInterval());
        //System.out.print(portfolio.getID() + " : " + var * portfolioValue + " ");
        return var * portfolioValue;
    }

    //optimized
    private double[][] getVCV(Portfolio portfolio) {
        Set<String> keys = portfolio.getAssetListKeySet();
        int numberOfAssets = keys.size();
        String symbols[] = keys.toArray(new String[numberOfAssets]);
        double[][] portfolioCovariance = new double[numberOfAssets][numberOfAssets];
        Double cov;
        for (int i = 0; i < symbols.length; i++) {
            for (int j = i; j < symbols.length; j++) {
                cov = covarianceTable.get(symbols[i], symbols[j]);
                if (cov != null) {
                    portfolioCovariance[i][j] = cov;
                    if (i != j)
                        portfolioCovariance[j][i] = cov;
                }
            }
        }
        return portfolioCovariance;
    }

    //optimized
    private double[][] getWeightage(Portfolio portfolio) {

        Set<String> keys = portfolio.getAssetListKeySet();
        int numberOfAssets = keys.size();
        double[][] portfolioWeighage = new double[1][numberOfAssets];
        double portfolioTotal = 0.0;
        int i = 0;
        Asset temp;
        Object symbol;
        Iterator itr = keys.iterator();
        while (itr.hasNext()) {
            symbol = itr.next();
            temp = getAssetList().get(symbol);
            if (temp != null) {
                portfolioWeighage[0][i] = temp.getCurrentStockPrice() * portfolio.getCurrentShare((String) symbol);
                portfolioTotal = portfolioTotal + portfolioWeighage[0][i];
            }
            i++;
        }
        this.portfolioValue = portfolioTotal;
        for (int j = 0; j < numberOfAssets; j++) {
            portfolioWeighage[0][j] = portfolioWeighage[0][j] / portfolioTotal;
        }
        return portfolioWeighage;
    }


    // optimized
    private double[][] getMeans(Portfolio portfolio) {
        Set<String> keys = portfolio.getAssetListKeySet();
        //String symbols[] = keys.toArray(new String[portfolio.getAssets().size()]);
        double[][] portfolioMeans = new double[1][keys.size()];
        Double mean;
        int i = 0;
        Iterator itr = keys.iterator();
        while (itr.hasNext()) {
            mean = ((ParametricAsset) getAssetList().get(itr.next())).getMean();
            if (mean != null)
                portfolioMeans[0][i] = mean;
            i++;
        }
        return portfolioMeans;
    }

    // optimized
    private void updateExcessReturnList(String symbol) {

        ParametricAsset asset = (ParametricAsset) getAssetList().get(symbol);
        double[] returnValues = asset.getReturnValueSet().getValues();
        double mean = ((ParametricAsset) getAssetList().get(symbol)).getMean();
        double[] excessReturns = new double[returnValues.length];
        for (int i = 0; i < returnValues.length; i++) {
            excessReturns[i] = returnValues[i] - mean;
        }
        asset.setExcessReturns(excessReturns);

    }

    // can be parallel
    private void updateCovarianceTable(String latestSymbol) {
        Set<String> keys = getAssetList().keySet();
        String symbols[] = keys.toArray(new String[getAssetList().size()]);
        double[] latestReturns = ((ParametricAsset) getAssetList().get(latestSymbol)).getExcessReturns();
        double[] tempReturns;
        int min;
        double cov;
        for (int i = 0; i < symbols.length; i++) {
            cov = 0.0;
            tempReturns = ((ParametricAsset) getAssetList().get(symbols[i])).getExcessReturns();
            if (latestReturns.length > tempReturns.length)
                min = tempReturns.length;
            else
                min = latestReturns.length;
            for (int j = 0; j < min; j++) {
                cov += latestReturns[j] * tempReturns[j];
            }

            cov = cov / (getBatchSize() - 2);

            covarianceTable.put(latestSymbol, symbols[i], cov);
            covarianceTable.put(symbols[i], latestSymbol, cov);
        }
    }

    @Override
    public double replaceAssetSimulation() {
        return 0;
    }

//    public Object batchModeParametricVaR(Portfolio portfolio) {
//        double priceReturns[][] = new double[getBatchSize() - 1][portfolio.getAssetsSize()];
//        double portfolioValue = 0.0;
//        double weightage[][] = new double[1][portfolio.getAssetsSize()];
//        DescriptiveStatistics stat = new DescriptiveStatistics();
//        stat.setWindowSize(getBatchSize() - 1);
//
//        Set<String> keys = portfolio.getAssetListKeySet();
//        String symbols[] = keys.toArray(new String[portfolio.getAssetsSize()]);
//        double[][] means = new double[1][portfolio.getAssetsSize()];
//
//        Asset asset;
//        LinkedList<Double> returnList;
//        int length;
//
//        /** fill priceReturns and calculate means **/
//        for (int i = 0; i < symbols.length; i++) {
//            asset = getAssetList().get(symbols[i]);
//            returnList = asset.getLatestReturnValues();
//            length = returnList.size();
//            weightage[0][i] = asset.getCurrentStockPrice() * portfolio.getCurrentShare(symbols[i]);
//            portfolioValue += weightage[0][i];
//            for (int j = 0; j < length; j++) {
//                priceReturns[j][i] = returnList.get(j);
//                stat.addValue(priceReturns[j][i]);
//            }
//            if (length == 0)
//                means[0][i] = 0;
//            else
//                means[0][i] = stat.getMean();
//        }
//
//        /** calculate  weight-ages **/
//        for (int i = 0; i < symbols.length; i++) {
//            weightage[0][i] = weightage[0][i] / portfolioValue;
//            //weightage[0][i] = 1.0/symbols.length; // for equal weight
//        }
//
//        /** calculate excess returns **/
//        double[][] excessReturns = new double[getBatchSize()- 1][portfolio.getAssetsSize()];
//        for (int i = 0; i < portfolio.getAssetsSize(); i++) {
//            for (int j = 0; j < getAssetList().get(symbols[i]).getLatestReturnValues().size(); j++) {
//                excessReturns[j][i] = priceReturns[j][i] - means[0][i];
//            }
//        }
//
//        /** create matrices from excess returns, means  and weight-age **/
//        RealMatrix returnMatrix = new Array2DRowRealMatrix(excessReturns);
//        RealMatrix weightageMatrix = new Array2DRowRealMatrix(weightage);
//        RealMatrix meanMatrix = new Array2DRowRealMatrix(means);
//
//        /** matrix multiplications using apache math library **/
//        RealMatrix VCV = (returnMatrix.transpose().multiply(returnMatrix)).scalarMultiply(1.0 / (getBatchSize() - 2));
//        RealMatrix PV = weightageMatrix.multiply(VCV).multiply(weightageMatrix.transpose());
//        RealMatrix PM = weightageMatrix.multiply(meanMatrix.transpose());
//
//        double pv = PV.getData()[0][0];
//        double pm = PM.getData()[0][0];
//
////      /** matrix multiplications using jama library **/
////      Jama.Matrix returnMatrixJ = new Jama.Matrix(excessReturns);
////      Jama.Matrix weightageMatrixJ = new Jama.Matrix(weightage);
////      Jama.Matrix meanMatrixJ = new Jama.Matrix(means);
////
////      Jama.Matrix VCVJ = returnMatrixJ.transpose().times(returnMatrixJ).times(1.0 / (batchSize - 2));
////      Jama.Matrix PVJ = (weightageMatrixJ.times(VCVJ).times(weightageMatrixJ.transpose()));
////      Jama.Matrix PMJ = weightageMatrixJ.times(meanMatrixJ.transpose());
////
////      double pv = PVJ.get(0, 0);
////      double pm = PMJ.get(0, 0);
////      double ps = Math.sqrt(pv);
//
//        /** NormalDistribution throws an exception when ps = 0, this condition return pm when ps = 0 **/
//        if (pv == 0) {
//            //System.out.print(portfolio.getID() + " : " + pm + " ");
//            return pm;
//        }
//
//        double ps = Math.sqrt(pv);
//        NormalDistribution n = new NormalDistribution(pm, ps);
//        double var = n.inverseCumulativeProbability(1 - getConfidenceInterval());
//        //System.out.print(portfolio.getID() + " : " + var * portfolioValue + " ");
//        return var * portfolioValue;
//    }
}