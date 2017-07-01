package org.wso2.extension.siddhi.execution.var.models.montecarlo;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by yellowflash on 1/24/17.
 */
public class MonteCarloNativeSimulationTest {

    MonteCarloNativeSimulation nativeCalculatorReference;
    MonteCarloStandardSimulation standardCalculatorReference;

    @Test
    public void simulation() throws Exception {
        int horizontalCount = 2500;
        double[] temp;
        nativeCalculatorReference = new MonteCarloNativeSimulation();
        temp = nativeCalculatorReference.simulate(0.026, 0.034, 0.01, 235.31, horizontalCount, 100);
        Assert.assertEquals(horizontalCount, temp.length, 0);
    }

    @Test
    public void performanceComparison() {
        int horizontalCount = 20000;
        nativeCalculatorReference = new MonteCarloNativeSimulation();
        standardCalculatorReference = new MonteCarloStandardSimulation(horizontalCount);
        long start = System.currentTimeMillis();

        nativeCalculatorReference.simulate(0.026, 0.034, 0.01, 235.31, horizontalCount, 100);
//        standardCalculatorReference.simulation(0.026, 0.034, 0.01, 235.31, horizontalCount, 100);
//        standardCalculatorReference.parallelSimulation(0.026, 0.034, 0.01, 235.31, horizontalCount, 100);
        long end = System.currentTimeMillis();
        System.out.println((double) (end - start) / 1000);
//        Assert.assertNotNull(nativeCalculatorReference);
    }
}