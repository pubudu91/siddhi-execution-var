package org.wso2.siddhi.extension.var.realtime.montecarlo;


import org.apache.log4j.Logger;

/**
 * Created by yellowflash on 11/23/16.
 */
public class MonteCarloNativeSimulation {

    static {
        try {
            System.load(System.getenv("JNI_LIB_HOME"));
        } catch (Exception e) {
            Logger log = Logger.getLogger(MonteCarloNativeSimulation.class);
            log.info(e.getMessage());
        }
    }

    public native double[] simulate(double mean, double std, double timeSlice, double currentPrice,
                                    int numberOfTrials, int calculationsPerDay);
}
