package org.wso2.siddhi.extension.var.realtime;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.wso2.siddhi.extension.var.models.Asset;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by dilip on 30/06/16.
 */
public class ParametricVaRCalcultorForPortfolio extends VaRPortfolioCalc {
    private DescriptiveStatistics stat = new DescriptiveStatistics();
    private double price;
    private String symbol;

    public ParametricVaRCalcultorForPortfolio(int limit, double ci, Map<String, Asset> assets) {
        super(limit, ci, assets);
    }

    @Override
    protected void addEvent(Object data[]) {
        price = ((Number) data[0]).doubleValue();
        symbol = data[1].toString();

        //if portfolio does not have the given symbol, then we drop the event.
        if (portfolio.get(symbol) != null) {
            portfolio.get(symbol).addHistoricalValue(price);
        }
    }

    @Override
    protected void removeEvent(String symbol) {
        List<Double> priceList = portfolio.get(symbol).getHistoricalValues();
        priceList.remove(0);
    }

    @Override
    protected Object processData() {
        double priceReturns[][] = new double[batchSize - 1][portfolio.size()];
        double portfolioTotal = 0.0;
        double weightage[][] = new double[1][portfolio.size()];
        //calculate the latest market value of the portfolio
        Set<String> keys = portfolio.keySet();
        String symbols[] = keys.toArray(new String[portfolio.size()]);
        double[] means = new double[portfolio.size()];

        // System.out.println(batchSize + " " + portfolio.size() + " " + symbols.length + " " + portfolio.get("IBM").getHistoricalValues().size());


        Asset asset;
        LinkedList<Double> priceList;
        for (int i = 0; i < symbols.length; i++) {
            asset = portfolio.get(symbols[i]);
            priceList = asset.getHistoricalValues();
            weightage[0][i] = priceList.getLast() * asset.getNumberOfShares();
            portfolioTotal += weightage[0][i];

            Double priceArray[] = priceList.toArray(new Double[batchSize]);
            DescriptiveStatistics stat = new DescriptiveStatistics();
            for (int j = 0; j < priceArray.length - 1; j++) {
                priceReturns[j][i] = Math.log(priceArray[j + 1] / priceArray[j]);
                stat.addValue(priceReturns[j][i]);
            }
            means[i] = stat.getMean();
        }

        for (int i = 0; i < symbols.length; i++) {
            weightage[0][i] = weightage[0][i] / portfolioTotal;
        }


        /*
         * calculate excess returns
         */
        double[][] excessReturns = new double[batchSize - 1][portfolio.size()];
        for (int i = 0; i < portfolio.size(); i++) {
            for (int j = 0; j < batchSize - 1; j++) {
                excessReturns[j][i] = priceReturns[j][i] - means[i];
            }
        }

        /* create a matrices from excess returns  and weight-age*/
        RealMatrix returnMatrix = new Array2DRowRealMatrix(excessReturns);
        RealMatrix weightageMatrix = new Array2DRowRealMatrix(weightage);

        RealMatrix VCV = (returnMatrix.transpose().multiply(returnMatrix)).scalarMultiply(1.0 / (batchSize - 1));
        RealMatrix PV = weightageMatrix.multiply(VCV).multiply(weightageMatrix.transpose());
        double pv = PV.getData()[0][0];

        NormalDistribution n = new NormalDistribution();
        double var = n.inverseCumulativeProbability(0.95) * Math.sqrt(pv);

        return var * portfolioTotal;

    }

}
