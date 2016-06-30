package org.wso2.siddhi.extension.var.batchmode;

/**
 * Created by flash on 6/27/16.
 */
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.HashMap;
import java.util.Map;

public class MonteCarloSimulation {


    private NormalDistribution distribution=null;
    private DescriptiveStatistics stat=null;

    /**
     * return a random Z value from thr probability Distribution
     * @return
     */
    protected double getRandomZVal() {
        return this.getDistribution().inverseCumulativeProbability(Math.random());
    }

    /**
     * get the future stock price using Geomatric brownian motion
     * @param parameters
     * @return
     */
    protected double getBrownianMotionOutput(Map <String,Double> parameters){

        double drift =  (parameters.get("distributionMean")-(parameters.get("standardDeviation")*parameters.get("standardDeviation"))/2)*parameters.get("timeSlice");
        double stochasticOffset=parameters.get("standardDeviation")*this.getRandomZVal()*Math.sqrt(parameters.get("timeSlice"));

        return parameters.get("currentStockValue")*Math.exp(drift+stochasticOffset);
    }

    /**
     * perform monte carlo simulation and return array of terminal values
     * @param numberOfTrials
     * @param calculationsPerDay
     * @param historicalValue
     * @param timeSlice
     * @param currentStockPrice
     * @return
     */
    public double[] simulation(int numberOfTrials,int calculationsPerDay,double [] historicalValue, double timeSlice,double currentStockPrice)
    {
        double terminalStockValues []= new double[numberOfTrials];
        DescriptiveStatistics tempStat=this.getStat(historicalValue);
        Map <String,Double>parameters= new HashMap<String, Double>();
        double tempStockValue=0;

        parameters.put("distributionMean",tempStat.getMean());
        parameters.put("standardDeviation",tempStat.getStandardDeviation());
        parameters.put("timeSlice",timeSlice);
        parameters.put("currentStockValue",currentStockPrice);

        for (int i = 0; i < numberOfTrials; i++) {
            for (int j = 0; j < calculationsPerDay; j++) {
                tempStockValue=this.getBrownianMotionOutput(parameters);
                parameters.put("currentStockValue",tempStockValue);
            }
            terminalStockValues[i]=tempStockValue;
        }
        return terminalStockValues;
    }

    protected DescriptiveStatistics getStat(double [] historicalValues){
        this.stat=new DescriptiveStatistics(historicalValues);
        return this.stat;
    }

    protected NormalDistribution getDistribution() {
        if (!(distribution instanceof NormalDistribution)) {
            this.distribution=new NormalDistribution();
        }
        return this.distribution;
    }
}
