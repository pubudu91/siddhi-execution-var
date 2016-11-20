package org.wso2.siddhi.extension.var.batchmode;

/**
 * Created by flash on 6/27/16.
 */

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.HashMap;
import java.util.Map;

public class MonteCarloSimulation {


    private NormalDistribution distribution = null;
    private DescriptiveStatistics stat = null;
    private double randomZValue = 0.0;

    public double getRandomZValue() {
        return randomZValue;
    }

    /**
     * return a random Z value from thr probability Distribution
     *
     * @return
     */
    protected double getRandomZVal() {
        randomZValue = this.getDistribution().inverseCumulativeProbability(Math.random());
        return randomZValue;
    }

    /**
     * get the future stock price using Geomatric brownian motion
     *
     * @param parameters
     * @return
     */
    protected double getBrownianMotionOutput(Map<String, Double> parameters) {

        double drift = (parameters.get("distributionMean") - (parameters.get("standardDeviation") *
                parameters.get("standardDeviation")) / 2) * parameters.get("timeSlice");
        double stochasticOffset = parameters.get("standardDeviation") * parameters.get("randomValue") *
                Math.sqrt(parameters.get("timeSlice"));
        return parameters.get("currentStockValue") * Math.exp(drift + stochasticOffset);
    }

    /**
     * perform monte carlo simulation and return array of terminal values
     *
     * @param numberOfTrials
     * @param calculationsPerDay
     * @param historicalValue
     * @param timeSlice
     * @param currentStockPrice
     * @return
     */
    public double[] simulation(int numberOfTrials, int calculationsPerDay, double[] historicalValue, double timeSlice, double currentStockPrice) {
        double terminalStockValues[] = new double[numberOfTrials];

        Map<String, Double> parameters, tempParameters;
        parameters = new HashMap<>();
        tempParameters = this.getMeanReturnAndStandardDeviation(historicalValue);
        double tempStockValue = 0;

        parameters.put("distributionMean", tempParameters.get("meanReturn"));
        parameters.put("standardDeviation", tempParameters.get("meanStandardDeviation"));
        parameters.put("timeSlice", timeSlice);
        parameters.put("randomValue", this.getRandomZVal());
        parameters.put("currentStockValue", currentStockPrice);

        for (int i = 0; i < numberOfTrials; i++) {
            parameters.put("currentStockValue", currentStockPrice);
            for (int j = 0; j < calculationsPerDay; j++) {
                tempStockValue = this.getBrownianMotionOutput(parameters);
                parameters.put("randomValue", this.getRandomZVal());
                parameters.put("currentStockValue", tempStockValue);
            }
            terminalStockValues[i] = tempStockValue;
        }
        return terminalStockValues;
    }

    /**
     * get statistical analysis object
     *
     * @param historicalValues
     * @return
     */
    protected DescriptiveStatistics getStat(double[] historicalValues) {
        this.stat = new DescriptiveStatistics(historicalValues);
        return this.stat;
    }

    /**
     * get instance of normal distribution
     *
     * @return
     */
    protected NormalDistribution getDistribution() {
        if (!(distribution instanceof NormalDistribution)) {
            this.distribution = new NormalDistribution();
        }
        return this.distribution;
    }

    /**
     * return mean and standard deviation of the historical returns
     *
     * @param historicalValues
     * @return
     */
    public Map<String, Double> getMeanReturnAndStandardDeviation(double[] historicalValues) {
        DescriptiveStatistics stat = new DescriptiveStatistics();
        for (int i = 0; i < historicalValues.length - 1; i++) {
            stat.addValue(Math.log(historicalValues[i + 1] / historicalValues[i]));
        }
        Map<String, Double> parameters = new HashMap<>();
        parameters.put("meanReturn", stat.getMean());
        parameters.put("meanStandardDeviation", stat.getStandardDeviation());
        return parameters;
    }

}
