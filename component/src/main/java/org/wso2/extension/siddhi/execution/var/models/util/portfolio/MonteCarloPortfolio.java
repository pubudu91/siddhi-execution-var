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

package org.wso2.extension.siddhi.execution.var.models.util.portfolio;

import java.util.Map;

/**
 * A class representing a financial portfolio in a Monte Carlo Simulation setting
 */
public class MonteCarloPortfolio extends Portfolio {

    private double previousPortfolioValue;
    private double[] finalPortfolioValueList = null;

    public MonteCarloPortfolio(String id, Map<String, Integer> assets) {
        super(id, assets);
    }

    public double[] getFinalPortfolioValueList() {
        return finalPortfolioValueList;
    }

    public void setFinalPortfolioValueList(double[] finalPortfolioValueList) {
        this.finalPortfolioValueList = finalPortfolioValueList;
    }

    public double getPreviousPortfolioValue() {
        return previousPortfolioValue;
    }

    public void setPreviousPortfolioValue(double previousPortfolioValue) {
        this.previousPortfolioValue = previousPortfolioValue;
    }

}
