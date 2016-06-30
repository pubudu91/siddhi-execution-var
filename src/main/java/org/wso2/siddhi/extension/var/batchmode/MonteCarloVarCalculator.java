package org.wso2.siddhi.extension.var.batchmode;

//import

import org.wso2.siddhi.extension.var.models.Asset;
import org.wso2.siddhi.extension.var.realtime.VaRCalculator;
import org.wso2.siddhi.extension.var.realtime.VaRPortfolioCalc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by flash on 6/29/16.
 */
public class MonteCarloVarCalculator extends VaRPortfolioCalc{


    public MonteCarloVarCalculator(int limit, double ci, String[] symbs, int[] shares) {
        super(limit, ci, symbs, shares);
    }

    @Override
    protected void addEvent(Object[] data) {

    }

    @Override
    protected void removeEvent(String symbol) {

    }

    @Override
    protected Object processData(List<Asset> assetCollection) {
        double [] terminalStockValues;
        int numberOfTrials=0;
        int calculationsPerDay=0;
        double timeSlice=0;
        Asset tempAsset;
        LinkedList<Double> historicalValues;

        Iterator<Asset> assets=assetCollection.iterator();

        while (assets.hasNext()){
            tempAsset=assets.next();
            historicalValues=tempAsset.getHistoricalValues();
            historicalValues.size();
            double[] hsar = historicalValues.stream().mapToDouble(d -> d).toArray();
            terminalStockValues=new MonteCarloSimulation().simulation(numberOfTrials,calculationsPerDay,hsar,timeSlice,historicalValues.getLast());

        }

        return null;
    }

}
