/*
 * Copyright (c)  2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.extension.siddhi.execution.var.models.montecarlo;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class MonteCarloSimulationTest {

    MonteCarloStandardSimulation simulationReference;
    MonteCarloNativeSimulation nativeCalculatorReference;

    @Test
    public void getBrownianMotionOutput() throws Exception {
        simulationReference = new MonteCarloStandardSimulation();
        Map<String, Double> parameters = new HashMap<>();
        parameters.put("distributionMean", 0.034);
        parameters.put("standardDeviation", 0.026);
        parameters.put("timeSlice", 0.01);
        parameters.put("randomValue", 1.35);
        parameters.put("currentStockValue", 235.31);

        double drift = (parameters.get("distributionMean")
                - (parameters.get("standardDeviation") * parameters.get("standardDeviation")) / 2) * parameters
                .get("timeSlice");
        double stochasticOffset = parameters.get("standardDeviation") * parameters.get("randomValue") * Math
                .sqrt(parameters.get("timeSlice"));
        double stockValue = parameters.get("currentStockValue") * Math.exp(drift + stochasticOffset);
        double temp = simulationReference.getBrownianMotionOutput(parameters);

        Assert.assertEquals(stockValue, temp, 0);

    }

    @Test
    public void simulation() throws Exception {
        int horizontalCount = 1000;
        simulationReference = new MonteCarloStandardSimulation();
        double temArr[];
        temArr = simulationReference.simulation(0.87, 0.078, 0.01, 354.23, horizontalCount, 100);
        Assert.assertEquals(1000, temArr.length);
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

    @Test
    public void compareStandardSimulationWithNativeSimulation() {
        int horizontalCount = 2500;

        simulationReference = new MonteCarloStandardSimulation();
        nativeCalculatorReference = new MonteCarloNativeSimulation();
        double temArr[];
        double Arr[];
        temArr = simulationReference.simulation(0.026, 0.034, 0.01, 235.31, horizontalCount, 100);
        Arr = nativeCalculatorReference.simulate(0.026, 0.034, 0.01, 235.31, horizontalCount, 100);
        int totalSum = 0;
        for (int i = 0; i < horizontalCount; i++) {
            totalSum += Math.abs(temArr[i] - Arr[i]);
        }
        System.out.println((double) totalSum / horizontalCount);
    }

}