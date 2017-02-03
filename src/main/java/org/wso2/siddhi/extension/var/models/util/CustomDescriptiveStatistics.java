package org.wso2.siddhi.extension.var.models.util;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 * Created by dilip on 24/01/17.
 */
public class CustomDescriptiveStatistics extends DescriptiveStatistics {

    private double mean;

    public CustomDescriptiveStatistics() {
        super();
        mean = 0.0;
    }

    private void updateMean(Double leastRecentValue, Double mostRecentValue) {
        if (leastRecentValue != null)
            mean = mean + ((mostRecentValue - leastRecentValue) / (windowSize));
        else
            mean = (mean * (getN() - 1) + mostRecentValue) / getN();
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
    }
}
