/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.extension.siddhi.execution.var.models.montecarlo;

import org.junit.Assert;
import org.junit.Test;

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