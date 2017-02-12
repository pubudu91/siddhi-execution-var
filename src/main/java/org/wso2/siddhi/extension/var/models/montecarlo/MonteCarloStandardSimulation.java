package org.wso2.siddhi.extension.var.models.montecarlo;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by yellowflash on 2/3/17.
 */
public class MonteCarloStandardSimulation {


    private NormalDistribution distribution = null;
    private DescriptiveStatistics stat = null;
    private double randomZValue = 0.0;
    public static double[] finalSimulatedValues;
    Random rnd = new Random(3);

    public MonteCarloStandardSimulation() {
    }

    public MonteCarloStandardSimulation(int numberOfTrials) {
        MonteCarloStandardSimulation.finalSimulatedValues = new double[numberOfTrials];
    }

    /**
     * return a random Z value from thr probability Distribution
     *
     * @return
     */
    private double getRandomZVal() {
        randomZValue = this.getDistribution().inverseCumulativeProbability(Math.random());
        return randomZValue;
    }

    private double getRandomZValThreadSafe() {
//        System.out.println(rnd.nextDouble());
        randomZValue = this.getDistribution().inverseCumulativeProbability(rnd.nextDouble());
        return randomZValue;
    }

    /**
     * get the future stock price using Geomatric brownian motion
     *
     * @param parameters
     * @return
     */
    public double getBrownianMotionOutput(Map<String, Double> parameters) {

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
     * @param timeSlice
     * @param currentStockPrice
     * @return
     */
    public double[] simulation(double mean, double std, double timeSlice, double
            currentStockPrice, int numberOfTrials, int calculationsPerDay) {
        double terminalStockValues[] = new double[numberOfTrials];

        Map<String, Double> parameters;
        parameters = new HashMap<>();
        double tempStockValue = 0;

        parameters.put("distributionMean", mean);
        parameters.put("standardDeviation", std);
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
     * get instance of normal distribution
     *
     * @return
     */
    public NormalDistribution getDistribution() {
        if (!(distribution instanceof NormalDistribution)) {
            this.distribution = new NormalDistribution();
        }
        return this.distribution;
    }

    /**
     * Do the simulation in parallel
     *
     * @param numberOfTrials
     * @param calculationsPerDay
     * @param timeSlice
     * @param currentStockPrice
     * @return
     */
    public double[] parallelSimulation(double mean, double std, double timeSlice, double
            currentStockPrice, int numberOfTrials, int calculationsPerDay) {
        int cores = Runtime.getRuntime().availableProcessors();
        int taskForOneThread = numberOfTrials / cores;
        int remainingTask = numberOfTrials % cores;
        int startingPoint;

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

        return MonteCarloStandardSimulation.finalSimulatedValues;
    }

    class ParallelSimulation implements Runnable {
        private int numberOfTrials;
        private int startingPoint;
        private int calculationsPerDay;
        Map<String, Double> parameters;
        private MonteCarloStandardSimulation simulationReference = new MonteCarloStandardSimulation();
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
            parameters.put("randomValue", simulationReference.getRandomZValThreadSafe());
            parameters.put("currentStockValue", currentStockPrice);
        }

        @Override
        public void run() {

            double tempStockValue = 0;
            for (int i = startingPoint; i < startingPoint + numberOfTrials; i++) {
                parameters.put("currentStockValue", currentStockPrice);
                for (int j = 0; j < calculationsPerDay; j++) {
                    tempStockValue = simulationReference.getBrownianMotionOutput(parameters);
                    parameters.put("randomValue", simulationReference.getRandomZValThreadSafe());
                    parameters.put("currentStockValue", tempStockValue);
                }
                MonteCarloStandardSimulation.finalSimulatedValues[i] = tempStockValue;
            }
        }
    }
}

