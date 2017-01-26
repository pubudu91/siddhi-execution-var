package org.wso2.siddhi.extension.var.realtime.montecarlo;


/**
 * Created by yellowflash on 11/23/16.
 */
public class MonteCarloNativeSimulation {

    static {
        try {
            System.load("/var/www/html/FYP/wso2das-3.1.0-SNAPSHOT/Native_Simulation.so");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public native double[] simulation(double mean, double std, double timeSlice, double currentPrice,
                                      int numberOfTrials, int calculationsPerDay);
}
