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

package org.wso2.extension.siddhi.execution.var.models.util.factory;

import org.wso2.extension.siddhi.execution.var.models.montecarlo.MonteCarloNativeSimulation;
import org.wso2.extension.siddhi.execution.var.models.montecarlo.MonteCarloStandardSimulation;
import org.wso2.extension.siddhi.execution.var.models.util.RealTimeVaRConstants;

/**
 * A factory for selecting between different options available for Monte Carlo Simulation
 */
public class MonteCarloHardwareTechniqueFactory {

    private MonteCarloNativeSimulation calculatorNativeReference = new MonteCarloNativeSimulation();
    private MonteCarloStandardSimulation calculatorStandardReference = new MonteCarloStandardSimulation();

    public double[] getTechnique(String technique, double currentStockPrice, double mean, double std, double timeSlice,
            int verticalSimulationsCount, int horizontalSimulationsCount) {
        return this.techniqueFactory(technique, currentStockPrice, mean, std, timeSlice, verticalSimulationsCount,
                horizontalSimulationsCount);
    }

    protected double[] techniqueFactory(String technique, double currentStockPrice, double mean, double std,
            double timeSlice, int verticalSimulationsCount, int horizontalSimulationsCount) {
        if (technique != null && technique.equals(RealTimeVaRConstants.MONTE_CARLO_CALCULATION_TECHNIQUE_AVX)) {
            return calculatorNativeReference
                    .simulate(mean, std, timeSlice, currentStockPrice, horizontalSimulationsCount,
                            verticalSimulationsCount);
        } else if (technique != null && technique
                .equals(RealTimeVaRConstants.MONTE_CARLO_CALCULATION_TECHNIQUE_JAVA_CONCURRENT)) {
            calculatorStandardReference = new MonteCarloStandardSimulation(horizontalSimulationsCount);
            return calculatorStandardReference
                    .parallelSimulation(mean, std, timeSlice, currentStockPrice, horizontalSimulationsCount,
                            verticalSimulationsCount);
        } else if (technique != null && technique
                .equals(RealTimeVaRConstants.MONTE_CARLO_CALCULATION_TECHNIQUE_JAVA_CONCURRENT)) {
            return calculatorStandardReference
                    .simulation(mean, std, timeSlice, currentStockPrice, horizontalSimulationsCount,
                            verticalSimulationsCount);
        }
        return null;
    }
}
