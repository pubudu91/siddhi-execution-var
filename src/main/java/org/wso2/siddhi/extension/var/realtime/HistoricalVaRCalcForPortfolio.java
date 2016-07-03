package org.wso2.siddhi.extension.var.realtime;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.wso2.siddhi.extension.var.models.Asset;

import java.util.*;

/**
 * Created by dilini92 on 6/26/16.
 */
public class HistoricalVaRCalcForPortfolio extends VaRPortfolioCalc{
    private DescriptiveStatistics stat = new DescriptiveStatistics();
    private double price;
    private String symbol;

    public HistoricalVaRCalcForPortfolio(int limit, double ci, Map<String, Asset> assets) {
        super(limit, ci, assets);
    }

    @Override
    protected void addEvent(Object data[]) {
        price = ((Number) data[1]).doubleValue();
        symbol = data[0].toString();

        //if portfolio does not have the given symbol, then we drop the event.
        if(portfolio.get(symbol) != null){
            portfolio.get(symbol).addHistoricalValue(price);
        }
    }

    @Override
    protected void removeEvent(String symbol) {
        LinkedList<Double> priceList = portfolio.get(symbol).getHistoricalValues();
        priceList.remove(0);
    }

    @Override
    protected Object processData() {
        double priceReturns[][] = new double[batchSize - 1][portfolio.size()];
        double portfolioTotal = 0.0;

        //calculate the latest market value of the portfolio
        Set<String> keys = portfolio.keySet();
        String symbols[] = keys.toArray(new String[portfolio.size()]);
        Asset asset;
        LinkedList<Double> priceList;
        for (int i = 0; i < symbols.length; i++){
            asset = portfolio.get(symbols[i]);
            priceList = asset.getHistoricalValues();
            portfolioTotal += priceList.getLast() * asset.getNumberOfShares();

            Double priceArray[] = priceList.toArray(new Double[batchSize]);
            for (int j = 0; j < priceArray.length - 1; j++) {
                //calculate the price return value Rj = ln(Sj+1/Sj)
                priceReturns[j][i] = Math.log(priceArray[j+1]/priceArray[j]);

                //generate stock prices based on the return value Sj = (1 + Rj) * S_latest
                priceReturns[j][i] = (priceReturns[j][i] + 1) * priceArray[batchSize - 1];

                //calculate market value for each event Mj = Sj * noOfShares
                priceReturns[j][i] = priceReturns[j][i] * asset.getNumberOfShares();
            }
        }

        for (int i = 0; i < batchSize - 1; i++) {
            double total = 0;
            for (int j = 0; j < symbols.length; j++) {
                total += priceReturns[i][j];
            }
            //add each value to create the histogram
            stat.addValue(portfolioTotal - total);
        }
        return stat.getPercentile((1 - confidenceInterval) * 100);
    }
}
