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

package org.wso2.extension.siddhi.execution.var.models.util.asset;

import org.wso2.extension.siddhi.execution.var.models.util.CustomDescriptiveStatistics;

public class Asset {

    private double currentStockPrice;
    private CustomDescriptiveStatistics returnValueSet;
    private double previousStockPrice;

    public Asset(int windowSize) {
        returnValueSet = new CustomDescriptiveStatistics();
        returnValueSet.setWindowSize(windowSize - 1);
    }

    public double getCurrentStockPrice() {
        return currentStockPrice;
    }

    public void setCurrentStockPrice(double currentStockPrice) {
        this.currentStockPrice = currentStockPrice;
    }

    public double[] getReturnValues() {
        return returnValueSet.getValues();
    }

    public void addReturnValue(double value) {
        returnValueSet.addValue(value);
    }

    public double getPreviousStockPrice() {
        return previousStockPrice;
    }

    public void setPreviousStockPrice(double priceBeforeLastPrice) {
        this.previousStockPrice = priceBeforeLastPrice;
    }

    public int getNumberOfReturnValues() {
        return (int) returnValueSet.getN();
    }

    public double getPercentile(double percentile) {
        return returnValueSet.getPercentile(percentile);
    }

    public double getMean() {
        return returnValueSet.getMean();
    }

    public double getStandardDeviation() {
        return returnValueSet.getStandardDeviation();
    }
}
