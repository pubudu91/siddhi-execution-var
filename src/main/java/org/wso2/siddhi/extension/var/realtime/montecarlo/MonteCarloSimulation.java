package org.wso2.siddhi.extension.var.realtime.montecarlo;

/**
 * Created by flash on 6/27/16.
 */

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MonteCarloSimulation {


    private NormalDistribution distribution = null;
    private DescriptiveStatistics stat = null;
    private double randomZValue = 0.0;
    public static double[] finalSimulatedValues;
    Random rnd = new Random(1);

    public MonteCarloSimulation() {
    }

    public MonteCarloSimulation(int numberOfTrials) {
        MonteCarloSimulation.finalSimulatedValues = new double[numberOfTrials];
    }

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
    public double[] simulation(int numberOfTrials, int calculationsPerDay, double[] historicalValue, double
            timeSlice, double currentStockPrice) {
        double terminalStockValues[] = new double[numberOfTrials];

        Map<String, Double> parameters, tempParameters;
        parameters = new HashMap<>();
//        tempParameters = this.getMeanReturnAndStandardDeviation(historicalValue);
        double tempStockValue = 0;

        parameters.put("distributionMean", 0.001216);
        parameters.put("standardDeviation", 0.01960);
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
        for (int i = 0; i < historicalValues.length; i++) {
            stat.addValue(historicalValues[i]);
        }
        Map<String, Double> parameters = new HashMap<>();
        parameters.put("meanReturn", stat.getMean());
        parameters.put("meanStandardDeviation", stat.getStandardDeviation());
        return parameters;
    }

    /**
     * Do the simulation in parallel
     *
     * @param numberOfTrials
     * @param calculationsPerDay
     * @param historicalValue
     * @param timeSlice
     * @param currentStockPrice
     * @return
     */
    public double[] parallelSimulation(int numberOfTrials, int calculationsPerDay, double[] historicalValue,
                                       double timeSlice, double currentStockPrice) {
        int cores = Runtime.getRuntime().availableProcessors();
        int taskForOneThread = numberOfTrials / cores;
        int remainingTask = numberOfTrials % cores;
        int startingPoint;

        double mean = 0.001216;
        double std = 0.01960;

        Thread threads[] = new Thread[cores];

        for (int i = 0; i < threads.length; i++) {
            startingPoint = i * taskForOneThread;
            //assign remaining tasks for the last thread
            if (i == threads.length - 1) {
                taskForOneThread = taskForOneThread + remainingTask;
            }

            threads[i] = new Thread(new ParallelSimulation(taskForOneThread, calculationsPerDay, mean, std, timeSlice,
                    currentStockPrice, startingPoint), i + "");
            threads[i].start();
        }

        for (int i = 0; i < threads.length; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return MonteCarloSimulation.finalSimulatedValues;
    }

    class ParallelSimulation implements Runnable {
        private int numberOfTrials;
        private int startingPoint;
        private int calculationsPerDay;
        Map<String, Double> parameters;
        private MonteCarloSimulation simulationReference = new MonteCarloSimulation();
        private double currentStockPrice;

        public ParallelSimulation(int numberOfTrials, int calculationsPerDay, double mean, double std, double timeSlice,
                                  double currentStockPrice, int startingPoint) {

            this.numberOfTrials = numberOfTrials;
            this.calculationsPerDay = calculationsPerDay;
            this.startingPoint = startingPoint;
            this.currentStockPrice = currentStockPrice;

            parameters = new HashMap<>();
            parameters.put("distributionMean", mean);
            parameters.put("standardDeviation", std);
            parameters.put("timeSlice", timeSlice);
            parameters.put("randomValue", simulationReference.getRandomZVal());
            parameters.put("currentStockValue", currentStockPrice);
        }

        @Override
        public void run() {

            double tempStockValue = 0;
            for (int i = startingPoint; i < startingPoint + numberOfTrials; i++) {
                parameters.put("currentStockValue", currentStockPrice);
                for (int j = 0; j < calculationsPerDay; j++) {
                    tempStockValue = simulationReference.getBrownianMotionOutput(parameters);
                    parameters.put("randomValue", simulationReference.getRandomZVal());
                    parameters.put("currentStockValue", tempStockValue);
                }
                MonteCarloSimulation.finalSimulatedValues[i] = tempStockValue;
            }
        }
    }

}
