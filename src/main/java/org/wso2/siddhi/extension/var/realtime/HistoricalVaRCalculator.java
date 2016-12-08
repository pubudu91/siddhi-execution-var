package org.wso2.siddhi.extension.var.realtime;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.wso2.siddhi.extension.var.models.Asset;
import org.wso2.siddhi.extension.var.models.Portfolio;

import java.util.*;

/**
 * Created by dilini92 on 6/26/16.
 */
public class HistoricalVaRCalculator extends VaRPortfolioCalc {
    private DescriptiveStatistics stat;

    /**
     *
     * @param limit
     * @param ci
     */
    public HistoricalVaRCalculator(int limit, double ci) {
        super(limit, ci);
    }

    /**
     * @return the var of the portfolio
     */
    @Override
    public Object processData(Portfolio portfolio) {
        stat = new DescriptiveStatistics();

        //calculate the latest market value of the portfolio
        Set<String> keys = portfolio.getAssets().keySet();
        String symbols[] = keys.toArray(new String[portfolio.getAssets().size()]);
        Asset asset;
        int noOfShares, maxPriceListLength = 0;
        ArrayList<Double> priceList;

        for (int i = 0; i < symbols.length; i++) {
            if(assetList.get(symbols[i]) != null) {
                if (i == 0)
                    maxPriceListLength = assetList.get(symbols[i]).getNumberOfHistoricalValues() - 1;
                else {
                    if (maxPriceListLength < assetList.get(symbols[i]).getNumberOfHistoricalValues() - 1)
                        maxPriceListLength = assetList.get(symbols[i]).getNumberOfHistoricalValues() - 1;
                }
            }
        }

        if(maxPriceListLength > 0) {
            //variable declaration
            double portfolioLossValues[] = new double[maxPriceListLength];

            stat.setWindowSize(maxPriceListLength);

            //at this point we have the updated asset list values containing Ri * S_latest
            //for each asset
            for (int i = 0; i < symbols.length; i++) {
                asset = assetList.get(symbols[i]);
                if(asset != null) {
                    priceList = asset.getLatestReturnValues(); //priceList contains Ri * S_latest
                    noOfShares = portfolio.getAssets().get(symbols[i]);

                    Iterator<Double> iterator = priceList.iterator();
                    int count = 0;

                    while (iterator.hasNext() && count < maxPriceListLength) {
                        if (count == priceList.size())
                            break;

                        //portfolio loss = Sigma (Si_latest * noOfShares_i) - Sigma ((1+Ri) * Si_latest * noOfShares_i)
                        //portfolio loss = Sigma -(Ri * Si_latest * noOfShares_i)
                        portfolioLossValues[count] += -iterator.next() * noOfShares;
                        count++;
                    }
                }
            }

            //get the summation of the market value of all assets in the portfolio for each observation
            for (int i = 0; i < maxPriceListLength; i++) {
                stat.addValue(portfolioLossValues[i]);
            }

            //returns the corresponding percentile value from the histogram
            return stat.getPercentile((1 - confidenceInterval) * 100);
        }
        return null;
    }
}
