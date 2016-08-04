package org.wso2.siddhi.extension.var.realtime;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.wso2.siddhi.extension.var.models.Asset;
import org.wso2.siddhi.extension.var.models.Portfolio;

import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

/**
 * Created by dilip on 30/06/16.
 */
public class ParametricVaRCalculator extends VaRPortfolioCalc {

    /**
     *
     * @param limit
     * @param ci
     */
    public ParametricVaRCalculator(int limit, double ci) {
        super(limit, ci);
    }

    /**
     *
     * @return the var of the portfolio
     */
    @Override
    protected Object processData(Portfolio portfolio) {
        double priceReturns[][] = new double[batchSize - 1][portfolio.getAssets().size()];
        double portfolioTotal = 0.0;
        double weightage[][] = new double[1][portfolio.getAssets().size()];

        Set<String> keys = portfolio.getAssets().keySet();
        String symbols[] = keys.toArray(new String[portfolio.getAssets().size()]);
        double[][] means = new double[1][portfolio.getAssets().size()];

        // System.out.println(batchSize + " " + portfolio.size() + " " + symbols.length + " " + portfolio.get("IBM").getHistoricalValues().size());


        // calculating 
        Asset asset;
        LinkedList<Double> priceList;
        for (int i = 0; i < symbols.length; i++) {
            asset = portfolio.getAssets().get(symbols[i]);
            priceList = asset.getHistoricalValues();
            weightage[0][i] = priceList.getLast() * asset.getNumberOfShares();
            portfolioTotal += weightage[0][i];

            Double priceArray[] = priceList.toArray(new Double[batchSize]);
            for (int j = 0; j < priceArray.length - 1; j++) {
                priceReturns[j][i] = Math.log(priceArray[j + 1] / priceArray[j]);
                stat.addValue(priceReturns[j][i]);
            }
            means[0][i] = stat.getMean();
        }

        /** calculate  weightage **/
        for (int i = 0; i < symbols.length; i++) {
            weightage[0][i] = weightage[0][i] / portfolioTotal;
            //weightage[0][i] = 1.0/symbols.length; // for equal weight
        }


        /*
         * calculate excess returns
         */
        double[][] excessReturns = new double[batchSize - 1][portfolio.getAssets().size()];
        for (int i = 0; i < portfolio.getAssets().size(); i++) {
            for (int j = 0; j < batchSize - 1; j++) {
                excessReturns[j][i] = priceReturns[j][i] - means[0][i];
            }
        }

        /* create a matrices from excess returns, means  and weight-age*/
        RealMatrix returnMatrix = new Array2DRowRealMatrix(excessReturns);
        RealMatrix weightageMatrix = new Array2DRowRealMatrix(weightage);
        RealMatrix meanMatrix = new Array2DRowRealMatrix(means);

        RealMatrix VCV = (returnMatrix.transpose().multiply(returnMatrix)).scalarMultiply(1.0 / (batchSize - 2));
        RealMatrix PV = weightageMatrix.multiply(VCV).multiply(weightageMatrix.transpose());
        RealMatrix PM = weightageMatrix.multiply(meanMatrix.transpose());
        double pv = PV.getData()[0][0];
        double pm = PM.getData()[0][0];
        double ps = Math.sqrt(pv);

        //NormalDistribution n = new NormalDistribution();
        //double var = n.inverseCumulativeProbability(1-confidenceInterval) * ps;
        //System.out.println(var*portfolioTotal);

        NormalDistribution n = new NormalDistribution(pm,ps);
        double var = n.inverseCumulativeProbability(1-confidenceInterval);
        return var * portfolioTotal;
    }

}
