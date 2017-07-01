package org.wso2.siddhi.extension.var.models.util.factory;

import org.wso2.siddhi.extension.var.models.montecarlo.MonteCarloNativeSimulation;
import org.wso2.siddhi.extension.var.models.montecarlo.MonteCarloStandardSimulation;
import org.wso2.siddhi.extension.var.models.util.RealTimeVaRConstants;

/**
 * Created by yellowflash on 4/8/17.
 */
public class MonteCarloHardwareTechniqueFactory {

    private MonteCarloNativeSimulation calculatorNativeReference = new MonteCarloNativeSimulation();
    private MonteCarloStandardSimulation calculatorStandardReference = new MonteCarloStandardSimulation();

    public double[] getTechnique(String technique, double currentStockPrice, double mean, double std, double timeSlice, int verticalSimulationsCount, int
            horizontalSimulationsCount) {
        return this.techniqueFactory(technique, currentStockPrice, mean, std, timeSlice, verticalSimulationsCount, horizontalSimulationsCount);
    }

    protected double[] techniqueFactory(String technique, double currentStockPrice, double mean, double std, double timeSlice, int verticalSimulationsCount, int
            horizontalSimulationsCount) {
        if (technique != null && technique.equals(RealTimeVaRConstants.MONTE_CARLO_CALCULATION_TECHNIQUE_AVX)) {
            return calculatorNativeReference.simulate(mean, std, timeSlice,
                    currentStockPrice, horizontalSimulationsCount, verticalSimulationsCount);
        } else if (technique != null && technique.equals(RealTimeVaRConstants.MONTE_CARLO_CALCULATION_TECHNIQUE_JAVA_CONCURRENT)) {
            calculatorStandardReference = new MonteCarloStandardSimulation(horizontalSimulationsCount);
            return calculatorStandardReference.parallelSimulation(mean, std, timeSlice,
                    currentStockPrice, horizontalSimulationsCount, verticalSimulationsCount);
        } else if (technique != null && technique.equals(RealTimeVaRConstants.MONTE_CARLO_CALCULATION_TECHNIQUE_JAVA_CONCURRENT)) {
            return calculatorStandardReference.simulation(mean, std, timeSlice,
                    currentStockPrice, horizontalSimulationsCount, verticalSimulationsCount);
        }
        return null;
    }
}
