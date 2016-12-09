package org.wso2.siddhi.extension.var.realtime;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.wso2.siddhi.extension.var.models.Asset;
import org.wso2.siddhi.extension.var.models.Portfolio;

import java.util.LinkedList;
import java.util.Set;

/**
 * Created by dilip on 30/06/16.
 */
public class ParametricVaRCalculator extends VaRPortfolioCalc {

    /**
     * @param limit
     * @param ci
     */
    public ParametricVaRCalculator(int limit, double ci) {
        super(limit, ci);
    }

    /**
     * @return the var of the portfolio
     */
    @Override
    public Object processData(Portfolio portfolio) {

        double priceReturns[][] = new double[batchSize - 1][portfolio.getAssets().size()];
        double portfolioTotal = 0.0;
        double weightage[][] = new double[1][portfolio.getAssets().size()];
        DescriptiveStatistics stat = new DescriptiveStatistics();
        stat.setWindowSize(batchSize - 1);

        Set<String> keys = portfolio.getAssets().keySet();
        String symbols[] = keys.toArray(new String[portfolio.getAssets().size()]);
        double[][] means = new double[1][portfolio.getAssets().size()];

        Asset asset;
        LinkedList<Double> returnList;
        int length;

        /** fill priceReturns and calculate means **/
        for (int i = 0; i < symbols.length; i++) {
            asset = assetList.get(symbols[i]);
            returnList = asset.getLatestReturnValues();
            length = returnList.size();
            weightage[0][i] = asset.getCurrentStockPrice() * portfolio.getAssets().get(symbols[i]);
            portfolioTotal += weightage[0][i];
            for (int j = 0; j < length; j++) {
                priceReturns[j][i] = returnList.get(j);
                stat.addValue(priceReturns[j][i]);
            }
            if (length == 0)
                means[0][i] = 0;
            else
                means[0][i] = stat.getMean();
        }

        /** calculate  weight-ages **/
        for (int i = 0; i < symbols.length; i++) {
            weightage[0][i] = weightage[0][i] / portfolioTotal;
            //weightage[0][i] = 1.0/symbols.length; // for equal weight
        }

        /** calculate excess returns **/
        double[][] excessReturns = new double[batchSize - 1][portfolio.getAssets().size()];
        for (int i = 0; i < portfolio.getAssets().size(); i++) {
            for (int j = 0; j < assetList.get(symbols[i]).getLatestReturnValues().size(); j++) {
                excessReturns[j][i] = priceReturns[j][i] - means[0][i];
            }
        }

        /** create matrices from excess returns, means  and weight-age **/
        RealMatrix returnMatrix = new Array2DRowRealMatrix(excessReturns);
        RealMatrix weightageMatrix = new Array2DRowRealMatrix(weightage);
        RealMatrix meanMatrix = new Array2DRowRealMatrix(means);

        /** matrix multiplications using apache math library **/
        RealMatrix VCV = (returnMatrix.transpose().multiply(returnMatrix)).scalarMultiply(1.0 / (batchSize - 2));
        RealMatrix PV = weightageMatrix.multiply(VCV).multiply(weightageMatrix.transpose());
        RealMatrix PM = weightageMatrix.multiply(meanMatrix.transpose());

        double pv = PV.getData()[0][0];
        double pm = PM.getData()[0][0];

        /** matrix multiplications using jama library **/
//      Jama.Matrix returnMatrixJ = new Jama.Matrix(excessReturns);
//      Jama.Matrix weightageMatrixJ = new Jama.Matrix(weightage);
//      Jama.Matrix meanMatrixJ = new Jama.Matrix(means);
//
//      Jama.Matrix VCVJ = returnMatrixJ.transpose().times(returnMatrixJ).times(1.0 / (batchSize - 2));
//      Jama.Matrix PVJ = (weightageMatrixJ.times(VCVJ).times(weightageMatrixJ.transpose()));
//      Jama.Matrix PMJ = weightageMatrixJ.times(meanMatrixJ.transpose());
//
//      double pv = PVJ.get(0, 0);
//      double pm = PMJ.get(0, 0);
//      double ps = Math.sqrt(pv);

        /** NormalDistribution throws an exception when ps = 0, this condition return pm when ps = 0 **/
        if (pv == 0) {
            //System.out.print(portfolio.getID() + " : " + pm + " ");
            return pm;
        }

        double ps = Math.sqrt(pv);
        NormalDistribution n = new NormalDistribution(pm, ps);
        double var = n.inverseCumulativeProbability(1 - confidenceInterval);
        //System.out.print(portfolio.getID() + " : " + var * portfolioTotal + " ");
        return var * portfolioTotal;
    }
}