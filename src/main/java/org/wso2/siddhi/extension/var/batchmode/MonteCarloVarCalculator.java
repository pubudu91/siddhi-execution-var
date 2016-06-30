package org.wso2.siddhi.extension.var.batchmode;

//import

import org.wso2.siddhi.extension.var.realtime.VaRCalculator;
import org.wso2.siddhi.extension.var.realtime.VaRPortfolioCalc;

import java.util.ArrayList;

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
    protected Object processData(String[] symbols) {
        double [] terminalStockValues;
        int numberOfTrials=0;
        int calculationsPerDay=0;
        double [] historicalValue={};
        double timeSlice=0;
        double currentStockPrice=0;
        int numberOfPortfolios;

        MonteCarloSimulation monteCarloSimulation= new MonteCarloSimulation();
        terminalStockValues=monteCarloSimulation.simulation(numberOfTrials,calculationsPerDay,historicalValue,timeSlice,currentStockPrice);

        return null;
    }

}
