package org.wso2.siddhi.extension.var.models.montecarlo;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yellowflash on 1/24/17.
 */
public class MonteCarloSimulationTest {

    MonteCarloStandardSimulation simulationReference;

    @Test
    public void getBrownianMotionOutput() throws Exception {
        simulationReference = new MonteCarloStandardSimulation();
        Map<String, Double> parameters = new HashMap<>();
        parameters.put("distributionMean", 0.034);
        parameters.put("standardDeviation", 0.026);
        parameters.put("timeSlice", 0.01);
        parameters.put("randomValue", 1.35);
        parameters.put("currentStockValue", 235.31);

        double drift = (parameters.get("distributionMean") - (parameters.get("standardDeviation") *
                parameters.get("standardDeviation")) / 2) * parameters.get("timeSlice");
        double stochasticOffset = parameters.get("standardDeviation") * parameters.get("randomValue") *
                Math.sqrt(parameters.get("timeSlice"));
        double stockValue = parameters.get("currentStockValue") * Math.exp(drift + stochasticOffset);
        double temp = simulationReference.getBrownianMotionOutput(parameters);

        Assert.assertEquals(stockValue, temp, 0);

    }

    @Test
    public void simulation() throws Exception {
        int horizontalCount = 2500;
        simulationReference = new MonteCarloStandardSimulation();
        double temArr[];
        temArr = simulationReference.simulation(0.026, 0.034, 0.01, 235.31, horizontalCount, 100);
        Assert.assertEquals(2500, temArr.length);
    }

    @Test
    public void parallelSimulation() throws Exception {
        int horizontalCount = 2500;
        simulationReference = new MonteCarloStandardSimulation(horizontalCount);
        double temArr[];
        temArr = simulationReference.parallelSimulation(0.026, 0.034, 0.01, 235.31, horizontalCount, 100);
        Assert.assertEquals(2500, temArr.length);
    }

    @Test
    public void compareParallelSimulationWithStandardMethod() {
        int horizontalCount = 2500;
        simulationReference = new MonteCarloStandardSimulation();
        double temArr[];
        temArr = simulationReference.simulation(0.026, 0.034, 0.01, 235.31, horizontalCount, 100);

        simulationReference = new MonteCarloStandardSimulation(horizontalCount);
        double temArrParallel[];
        temArrParallel = simulationReference.parallelSimulation(0.026, 0.034, 0.01, 235.31, horizontalCount, 100);

        Assert.assertArrayEquals(temArr, temArrParallel, 0);
    }

}