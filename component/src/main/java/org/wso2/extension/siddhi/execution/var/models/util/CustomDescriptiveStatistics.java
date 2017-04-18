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

package org.wso2.extension.siddhi.execution.var.models.util;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.util.FastMath;

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
        if (getN() == getWindowSize()) {
            leastRecentValue = getElement(0);
        }
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
            double temp = (windowSize - 2) * Math.pow(previousStd, 2) + (windowSize - 1) * Math.pow(
                    (previousMean - mean), 2) + Math.pow((previousMean - mean), 2);
            previousStd = this.std;
            this.std = Math.sqrt(temp / (windowSize - 1));
        }
    }

}
