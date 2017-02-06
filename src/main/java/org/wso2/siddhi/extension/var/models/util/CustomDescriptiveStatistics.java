package org.wso2.siddhi.extension.var.models.util;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.util.FastMath;

/**
 * Created by dilip on 24/01/17.
 */
public class CustomDescriptiveStatistics extends DescriptiveStatistics {

    private double mean;
    private double std;
    private Double previousMean = null;
    private Double previousStd = null;

    public CustomDescriptiveStatistics() {
        super();
        mean = 0.0;
    }

    private void updateMean(Double leastRecentValue, Double mostRecentValue) {
        if (leastRecentValue != null) {
            previousMean = mean;
            mean = mean + ((mostRecentValue - leastRecentValue) / (windowSize));
        } else {
            mean = (mean * (getN() - 1) + mostRecentValue) / getN();
            previousMean = mean;
        }

    }

    @Override
    public double getMean() {
        return mean;
    }

    @Override
    public void addValue(double value) {
        Double leastRecentValue = null;
        Double mostRecentValue;
        if (getN() == getWindowSize())
            leastRecentValue = getElement(0);
        super.addValue(value);
        mostRecentValue = value;
        updateMean(leastRecentValue, mostRecentValue);
        updateStandardDeviation();
    }

    public void updateStandardDeviation() {
        if (previousStd == null) {
            double stdDev = 0.0D / 0.0;
            if (this.getN() > 0L) {
                if (this.getN() > 1L) {
                    stdDev = FastMath.sqrt(this.getVariance());
                } else {
                    stdDev = 0.0D;
                }
            }
            previousStd = stdDev;
            this.std = stdDev;
        } else {
            double temp = (windowSize - 2) * Math.pow(previousStd, 2) + (windowSize - 1) * Math.pow((previousMean - mean), 2) + Math.pow((previousMean - mean), 2);
            previousStd = this.std;
            this.std = Math.sqrt(temp / (windowSize - 1));
        }
    }

}
