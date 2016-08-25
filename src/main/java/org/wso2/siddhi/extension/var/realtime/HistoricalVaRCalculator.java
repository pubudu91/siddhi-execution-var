package org.wso2.siddhi.extension.var.realtime;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.wso2.siddhi.extension.var.models.Asset;
import org.wso2.siddhi.extension.var.models.Portfolio;

import java.util.*;

/**
 * Created by dilini92 on 6/26/16.
 */
public class HistoricalVaRCalculator extends VaRPortfolioCalc {
    private boolean hasWeight;
    private DescriptiveStatistics stat;

    /**
     *
     * @param limit
     * @param ci
     * @param hasWeight
     */
    public HistoricalVaRCalculator(int limit, double ci, boolean hasWeight) {
        super(limit, ci);
        this.hasWeight = hasWeight;
    }

    /**
     * @return the var of the portfolio
     */
    @Override
    public Object processData(Portfolio portfolio) {
        double priceReturns[][] = new double[batchSize - 1][portfolio.getAssets().size()];
        double portfolioTotal = 0.0;

        stat = new DescriptiveStatistics();
        stat.setWindowSize(batchSize - 1);

        //calculate the latest market value of the portfolio
        Set<String> keys = portfolio.getAssets().keySet();
        String symbols[] = keys.toArray(new String[portfolio.getAssets().size()]);
        Asset asset;
        int noOfShares;
        LinkedList<Double> priceList;
        for (int i = 0; i < symbols.length; i++) {
            asset = assetList.get(symbols[i]);
            noOfShares = portfolio.getAssets().get(symbols[i]);
            priceList = asset.getHistoricalValues();
            portfolioTotal += priceList.getLast() * noOfShares;

            Double priceArray[] = priceList.toArray(new Double[batchSize]);
            for (int j = 0; j < priceArray.length - 1; j++) {
                //calculate the price return value Rj = ln(Sj+1/Sj)
                priceReturns[j][i] = Math.log(priceArray[j + 1] / priceArray[j]);

                if(hasWeight){
                    //generate stock prices based on the return value Sj = (1 + Rj) * S_latest
                    priceReturns[j][i] = (priceReturns[j][i] + 1) * priceArray[batchSize - 1];

                    //calculate market value for each event Mj = Sj * noOfShares
                    priceReturns[j][i] = priceReturns[j][i] * noOfShares;
                }
            }
        }

        //get the summation of the market value of all assets in the portfolio for each observation
        for (int i = 0; i < batchSize - 1; i++) {
            double total = 0;
            for (int j = 0; j < symbols.length; j++) {
                total += priceReturns[i][j];
            }

            //If user wants to consider the no of shares
            if(hasWeight) {
                //add each value to create the histogram
                stat.addValue(portfolioTotal - total);
            }else{
                stat.addValue(total);
            }
        }
        return stat.getPercentile((1 - confidenceInterval) * 100);
    }
}
