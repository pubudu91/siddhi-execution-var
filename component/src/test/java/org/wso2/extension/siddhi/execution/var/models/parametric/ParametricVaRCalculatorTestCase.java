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

package org.wso2.extension.siddhi.execution.var.models.parametric;

import org.junit.Test;
import org.wso2.extension.siddhi.execution.var.models.util.Event;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class ParametricVaRCalculatorTestCase {

    @Test
    public void testProcessData() {
        int batchSize = 251;
        double ci = 0.95;
        ParametricVaRCalculator varCalculator = new ParametricVaRCalculator(batchSize, ci);
        String[] split;
        ClassLoader classLoader = getClass().getClassLoader();
        File stockFile = new File(classLoader.getResource("stock100.csv").getFile());
        File portfolioFile = new File(classLoader.getResource("A100Portfolio.csv").getFile());
        Scanner stockScan = null;
        Scanner portfolioScan = null;
        try {
            stockScan = new Scanner(stockFile);
            portfolioScan = new Scanner(portfolioFile);
            int stockCount = 1;
            while (stockScan.hasNext()) {
                Event event = new Event();
                if (stockCount % 15 == 0) {
                    split = portfolioScan.nextLine().split(",");
                    event.setPortfolioID(split[0]);
                    event.setSymbol(split[1]);
                    event.setQuantity(Integer.valueOf(split[2]));
                    event.setPrice(Double.valueOf(split[3]));
                    System.out.println("Data " + (stockCount++) + " " + split[0] + " " + split[1] + " " + split[2] + " "
                            + split[3]);
                } else {
                    split = stockScan.nextLine().split(",");
                    System.out.println("Data " + (stockCount++) + " " + split[0] + " " + split[1]);
                    event.setSymbol(split[0]);
                    event.setPrice(Double.valueOf(split[1]));
                }
                Object var = varCalculator.calculateValueAtRisk(event);
                System.out.println(var);
                System.out.println();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            stockScan.close();
            portfolioScan.close();
        }

    }
}