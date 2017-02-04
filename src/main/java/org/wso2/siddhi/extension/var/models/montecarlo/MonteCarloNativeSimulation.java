package org.wso2.siddhi.extension.var.models.montecarlo;


import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

/**
 * Created by yellowflash on 11/23/16.
 */
public class MonteCarloNativeSimulation {

    static {
        try {
            System.load(System.getenv("JNI_LIB_HOME"));
        } catch (Exception e) {

        }
    }

    public native double[] simulate(double mean, double std, double timeSlice, double currentPrice,
                                    int numberOfTrials, int calculationsPerDay);
}
