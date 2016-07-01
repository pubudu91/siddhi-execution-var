package org.wso2.siddhi.extension.var.batchmode;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.wso2.siddhi.extension.var.models.Asset;
import org.wso2.siddhi.extension.var.realtime.VaRPortfolioCalc;

import java.util.*;

/**
 * Created by flash on 6/29/16.
 */
public class MonteCarloVarCalculator extends VaRPortfolioCalc{

    private int numberOfTrials;
    private int calculationsPerDay;
    private double timeSlice;
    private double price;
    private String symbol;

    public MonteCarloVarCalculator(int limit, double ci, Map<String, Asset> assets,
                                   int numberOfTrials,int calculationsPerDay,double timeSlice) {
        super(limit, ci, assets);
        this.numberOfTrials=numberOfTrials;
        this.calculationsPerDay=calculationsPerDay;
        this.timeSlice=timeSlice;
    }

    @Override
    protected void addEvent(Object data[]) {
        price = ((Number) data[0]).doubleValue();
        symbol = data[1].toString();

        //if portfolio does not have the given symbol, then we drop the event.
        if(portfolio.get(symbol) != null){
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
        double [] terminalStockValues;
        double [] finalPortfolioValues=new double[numberOfTrials];
        String [] keys = portfolio.keySet().toArray(new String[portfolio.size()]);
        Asset tempAsset;
        LinkedList<Double> historicalValues;
        double todayMarketValue=0;

        for (int i = 0; i < numberOfTrials; i++) {
            finalPortfolioValues[i]=0;
        }

        for (int i = 0; i < keys.length ; i++) {

            tempAsset=portfolio.get(keys[i]);
            todayMarketValue=(tempAsset.getHistoricalValues().getLast()*tempAsset.getNumberOfShares());
            historicalValues=tempAsset.getHistoricalValues();
            terminalStockValues=new MonteCarloSimulation().simulation(this.numberOfTrials,this.calculationsPerDay,
                    historicalValues.stream().mapToDouble(d->d).toArray(),this.timeSlice,historicalValues.getLast());

            for (int j = 0; j <terminalStockValues.length ; j++) {
                finalPortfolioValues[j]+=(todayMarketValue - (terminalStockValues[i]*tempAsset.getNumberOfShares()));
            }
        }

        return new DescriptiveStatistics(finalPortfolioValues).getPercentile((1 - confidenceInterval) * 100);
    }

}
