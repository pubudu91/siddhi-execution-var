package org.wso2.siddhi.extension.var.batchmode;

import java.io.IOException;
import java.util.Arrays;

/**
 * Created by yellowflash on 11/23/16.
 */
public class MonteCarloNativeSimulation {

    /**
     * this code block will load the system library which has c++ implementation
     */
    static {
        try {
            System.load("/var/www/html/FYP/siddhi/modules/siddhi-extensions/var/src/libs/nativeSimulation.so");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
// number of trials should be divisible by 4

    /**
     * this method will behave as a gateway prototype for interacting in between native implementation and java
     * implementation
     *
     * @param mean
     * @param std
     * @param timeSlice
     * @param currentPrice
     * @param numberOfTrials
     * @param calculationsPerDay
     * @return
     */
    public native double[] simulation(double mean, double std, double timeSlice, double currentPrice,
                                      int numberOfTrials, int calculationsPerDay);

}
