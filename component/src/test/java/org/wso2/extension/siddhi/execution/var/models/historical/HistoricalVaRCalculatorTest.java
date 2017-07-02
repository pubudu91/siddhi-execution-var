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

package org.wso2.extension.siddhi.execution.var.models.historical;

import org.junit.Assert;
import org.junit.Test;
import org.wso2.extension.siddhi.execution.var.models.util.Event;

public class HistoricalVaRCalculatorTest {

    @Test
    public void testProcessData() throws Exception {
    }

    @Test
    public void testSimulateChangedAsset() throws Exception {
    }

    @Test
    public void testCalculateValueAtRisk() throws Exception {
        HistoricalVaRCalculator calculator = new HistoricalVaRCalculator(3, 0.95);

        Event events[] = new Event[9];
        events[0] = new Event("1", 100, "IBM", 196.350006);
        events[1] = new Event("1", 120, "GE", 21.34);
        events[2] = new Event("1", 90, "XOM", 88.709999);
        events[3] = new Event("1", 0, "IBM", 195.270004);
        events[4] = new Event("1", 0, "GE", 21.1);
        events[5] = new Event("1", 0, "XOM", 88.550003);
        events[6] = new Event("1", 0, "IBM", 193.990005);
        events[7] = new Event("1", 0, "GE", 21.200001);
        events[8] = new Event("1", 0, "XOM", 88.959999);

        double actual[] = new double[6];
        double expected[] = { -107.7026322013, -136.3400722528, -150.7267189535, -170.6035460558, -170.7392698777,
                -170.8058815931 };
        for (int i = 0; i < 9; i++) {
            Object var = calculator.calculateValueAtRisk(events[i]);
            if (i > 2)
                actual[i - 3] = Double.parseDouble(var.toString().substring(15, var.toString().length() - 1));
        }

        for (int i = 0; i < 6; i++) {
            Assert.assertEquals(expected[i], actual[i], 0.00005);
        }
    }
}