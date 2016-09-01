package org.wso2.siddhi.extension.var.backtest;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by flash on 8/24/16.
 */
public class MonteCarloAssertionTest {

    @Test
    public void testCalculateVar() throws Exception {

        MonteCarloAssertion calc;
        int i = 0, passedCount = 0;
        Random rnd = new Random();
        Map<String, Integer> portfolio = new HashMap<>();
        for (int j = 0; j < 10; j++) {
            portfolio.put("IBM", rnd.nextInt(100));
            portfolio.put("GE", rnd.nextInt(300));
            portfolio.put("XOM", rnd.nextInt(500));

            while (true) {
                calc = new MonteCarloAssertion(500, portfolio, 0.95, 251, i);
                try {
                    if (calc.standardCoverageTest(0.05)) {
                        passedCount++;
                    }
                } catch (Exception e) {
                    System.out.println("Done");
                    break;
                }
                i++;
            }
            System.out.println("train No: " + j + ": " + passedCount);
            System.out.println(i);
            System.out.println("Passed Probability:" + passedCount / i);
        }
    }
}