package org.wso2.siddhi.extension.var.models.montecarlo;




/**
 * Created by yellowflash on 11/23/16.
 */
public class MonteCarloNativeSimulation {

    static {
        try {
            System.load("/var/www/html/FYP/siddhi/modules/siddhi-extensions/var/src/main/resources/libs/Native_Simulation.so");
        } catch (Exception e) {

        }
    }

    public native double[] simulate(double mean, double std, double timeSlice, double currentPrice,
                                    int numberOfTrials, int calculationsPerDay);
}
