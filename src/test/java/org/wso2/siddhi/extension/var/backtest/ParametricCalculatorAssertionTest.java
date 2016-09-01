package org.wso2.siddhi.extension.var.backtest;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by flash on 8/25/16.
 */
public class ParametricCalculatorAssertionTest {

    @Test
    public void testCalculateVar() throws Exception {

        ParametricCalculatorAssertion calc;
        int i = 0, passedCount = 0;
        Map<String, Integer> portfolio = new HashMap<>();
        portfolio.put("IBM", 150);
        portfolio.put("GE",60);
        portfolio.put("XOM", 300);

        while (true) {
            calc = new ParametricCalculatorAssertion(500, portfolio, 0.95, 251, i);
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
        System.out.println(passedCount);
        System.out.println(i);
        System.out.println("Passed Probability: " + (double) (passedCount * 100 / i)+"%");

    }
}