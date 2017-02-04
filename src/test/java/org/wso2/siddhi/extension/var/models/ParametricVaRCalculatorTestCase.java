package org.wso2.siddhi.extension.var.models;

import org.junit.Test;
import org.wso2.siddhi.extension.var.models.parametric.ParametricVaRCalculator;
import org.wso2.siddhi.extension.var.models.util.Event;

import java.io.File;
import java.util.*;

/**
 * Created by dilip on 06/07/16.
 */
public class ParametricVaRCalculatorTestCase {

    @Test
    public void testProcessData() throws Exception {
        int batchSize = 251;
        double ci = 0.95;
        ParametricVaRCalculator varCalculator = new ParametricVaRCalculator(batchSize, ci);
        Object[] inputData = new Object[4];
        String[] split;
        ClassLoader classLoader = getClass().getClassLoader();
        File stockFile = new File(classLoader.getResource("stock100.csv").getFile());
        Scanner stockScan = new Scanner(stockFile);
        File portfolioFile = new File(classLoader.getResource("A100Portfolio.csv").getFile());
        Scanner portfolioScan = new Scanner(portfolioFile);
        int stockCount = 1;
        while (stockScan.hasNext()) {
            Event event = null;
            if (stockCount % 15 == 0) {
                split = portfolioScan.nextLine().split(",");
                System.out.println("Data " + (stockCount++) + " " + split[0] + " " + split[1] + " " + split[2] + " " + split[3]);
            } else {
                split = stockScan.nextLine().split(",");
                System.out.println("Data " + (stockCount++) + " " + split[0] + " " + split[1]);
                event = new Event();
                event.setSymbol(split[0]);
                event.setPrice(Double.valueOf(split[1]));
            }
            varCalculator.calculateValueAtRisk(event);
            System.out.println("");
        }
    }
}