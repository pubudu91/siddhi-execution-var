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

public class RealTimeVaRConstants {
    public static String PORTFOLIO = "Portfolio ";
    public static int NUMBER_OF_PARAMETERS = 4;

    public static final String STRING = "STRING";
    public static final String DOUBLE = "DOUBLE";
    public static final String INTEGER = "INT";

    public static final String OUTPUT_NAME = "var";

    /**
     * Array Indices
     */
    public static final int PORTFOLIO_ID_INDEX = 0;
    public static final int QUANTITY_INDEX = 1;
    public static final int SYMBOL_INDEX = 2;
    public static final int PRICE_INDEX = 3;
    public static final int BATCH_SIZE_INDEX = 4;
    public static final int CI_INDEX = 5;
    public static final int MONTE_CARLO_VERTICAL_SIMULATION_COUNT_INDEX = 6;
    public static final int MONTE_CARLO_HORIZONTAL_SIMULATION_COUNT_INDEX = 7;
    public static final int MONTE_CARLO_TIME_SLICE_INDEX = 8;
    public static final String MONTE_CARLO_CALCULATION_TECHNIQUE_AVX = "AVX";
    public static final String MONTE_CARLO_CALCULATION_TECHNIQUE_JAVA_CONCURRENT = "JAVA_CONCURRENT";
    public static final String MONTE_CARLO_CALCULATION_TECHNIQUE_ENV_VARIABLE = "MONTECARLO_SIMULATION";

}
