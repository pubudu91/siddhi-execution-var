package org.wso2.siddhi.extension.var.realtime;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by dilini92 on 6/26/16.
 */
public class HistoricalVaRCalcForPortfolio extends VaRPortfolioCalc{
    private DescriptiveStatistics stat = new DescriptiveStatistics();
    private double price;
    private String symbol;

    public HistoricalVaRCalcForPortfolio(int limit, double ci, String symbols[], int[] shares) {
        super(limit, ci, symbols, shares);
    }

    @Override
    protected void addEvent(Object data[]) {
        price = ((Number) data[0]).doubleValue();
        symbol = data[1].toString();

        if(priceLists.get(symbol) != null){
            priceLists.get(symbol).add(price);

        }else{
            List<Double> newList = new LinkedList<Double>();
            newList.add(price);
            priceLists.put(symbol, newList);
        }
    }

    @Override
    protected void removeEvent(String symbol) {
        List<Double> priceList = priceLists.get(symbol);
        priceList.remove(0);
    }

    @Override
    protected Object processData(String symbols[]) {
        double priceReturns[][] = new double[batchSize - 1][symbols.length];
        double portfolioTotal = 0.0;

        //calculate the latest market value of the portfolio
        for (int i = 0; i < symbols.length; i++) {
            portfolioTotal += priceLists.get(symbols[i]).get(batchSize - 1) * noOfShares[i];
        }

        for (int i = 0; i < symbols.length; i++) {
            for (int j = 0; j < batchSize - 1; j++) {
                List<Double> priceList = priceLists.get(symbols[i]);
                Double priceArray[] = priceList.toArray(new Double[batchSize]);

                //calculate the price return value Rj = ln(Sj+1/Sj)
                priceReturns[i][j] = Math.log(priceArray[j+1]/priceArray[j]);

                //generate stock prices based on the return value Sj = (1 + Rj) * S_latest
                priceReturns[i][j] = (priceReturns[i][j] + 1) * priceArray[batchSize - 1];

                //calculate market value for each event Mj = Sj * noOfShares
                priceReturns[i][j] = priceReturns[i][j] * noOfShares[i];
            }
        }

        for (int i = 0; i < batchSize - 1; i++) {
            double total = 0;
            for (int j = 0; j < symbols.length; j++) {
                total += priceReturns[j][i];
            }

            stat.addValue(portfolioTotal - total);
        }
        return stat.getPercentile((1 - confidenceInterval) * 100);
    }
}
