package org.wso2.siddhi.extension.var.realtime.montecarlo;

import org.junit.Test;

/**
 * Created by yellowflash on 11/26/16.
 */
public class MonteCarloNativeSimulationTest {
    @Test
    public void testSimulation() throws Exception {
        long begin = System.currentTimeMillis();
        double[] temp = new MonteCarloNativeSimulation().simulation(0.001216, 0.01960, 0.1, 234.5, 25000, 100);

        long end = System.currentTimeMillis();
        System.out.println((double) (end - begin) / 1000);
    }

    @Test
    public void testComparisonSimulation() throws Exception {
//        int numberOfTrials = 20;
//        double[] temp_1 = {0, 2, 3}, temp_2;
////        long begin = System.currentTimeMillis();
//        double[] temp = new MonteCarloNativeSimulation().simulation(0.001216, 0.01960, 0.1, 234.5, numberOfTrials, 100);
//        temp_2 = new MonteCarloSimulation(numberOfTrials).simulation(numberOfTrials, 100, temp_1, 0.1, 234.5);
////        long end = System.currentTimeMillis();
////        System.out.println((double) (end - begin) / 1000);
//
//        for (int i = 0; i < temp.length; i++) {
////            System.out.println("Native Simulation" + temp[i]);
////            System.out.println("General Simulation" + temp_2[i]);
////            System.out.println(temp[i] - temp_2[i]);
//        }

    }

}