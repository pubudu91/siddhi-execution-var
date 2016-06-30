package org.wso2.siddhi.extension.var.batchmode;

//import

import org.wso2.siddhi.extension.var.realtime.VaRCalculator;

import java.util.ArrayList;

/**
 * Created by flash on 6/29/16.
 */
public class MonteCarloVarCalculator extends VaRCalculator{

    public MonteCarloVarCalculator(int limit, double ci) {
        super(limit, ci);
    }

    @Override
    protected void addEvent(Object data) {

    }

    @Override
    protected void removeEvent() {

    }

    @Override
    protected Object processData() {
        double [] terminalStockValues;
        int numberOfTrials=0;
        int calculationsPerDay=0;
        double [] historicalValue={};
        double timeSlice=0;
        double currentStockPrice=0;
        ArrayList <Portfolio> portfolioList=new ArrayList<Portfolio>();
        int numberOfPortfolios;

        numberOfPortfolios = portfolioList.size();

        MonteCarloSimulation monteCarloSimulation= new MonteCarloSimulation();
        terminalStockValues=monteCarloSimulation.simulation(numberOfTrials,calculationsPerDay,historicalValue,timeSlice,currentStockPrice);


        return null;
    }

    class Portfolio{
        private double historicalValue[];
        private double currentStockPrice;
        private String label;
        private int numberOfShares;
        private int ID;

        public Portfolio(double[] historicalValue, double currentStockPrice, String label, int numberOfShares) {
            this.historicalValue = historicalValue;
            this.currentStockPrice = currentStockPrice;
            this.label = label;
            this.numberOfShares = numberOfShares;
        }

        public int getNumberOfShares() {
            return numberOfShares;
        }

        public int getID() {
            return ID;
        }

        public void setID(int ID) {
            this.ID = ID;
        }

        public double[] getHistoricalValue() {
            return historicalValue;
        }

        public double getCurrentStockPrice() {
            return currentStockPrice;
        }

        public String getLabel() {
            return label;
        }
    }
}
