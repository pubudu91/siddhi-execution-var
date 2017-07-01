package org.wso2.siddhi.extension.var.models.parametric;

import org.junit.Test;
import org.wso2.siddhi.extension.var.models.parametric.ParametricVaRCalculator;
import org.wso2.siddhi.extension.var.models.util.Event;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * Created by dilip on 06/07/16.
 */
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
                    System.out.println("Data " + (stockCount++) + " " + split[0] + " " + split[1] + " " + split[2] + " " + split[3]);
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