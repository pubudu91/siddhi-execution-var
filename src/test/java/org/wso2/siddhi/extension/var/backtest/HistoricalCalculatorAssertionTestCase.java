package org.wso2.siddhi.extension.var.backtest;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by flash on 7/14/16.
 */
public class HistoricalCalculatorAssertionTestCase {

    @org.junit.Test
    public void testCalculateVar() throws IOException {
        HistoricalCalculatorAssertion calc;
        int i = 0, passedCount = 0;
        Map<String, Integer> portfolio = new HashMap<>();
        Random rnd = new Random();

            portfolio.put("IBM", 550);
            portfolio.put("GE", 350);
            portfolio.put("XOM", 400);

            while (true) {
                calc = new HistoricalCalculatorAssertion(500, portfolio, 0.95, 251, i);
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
            System.out.println("Passed Probability:" + passedCount / i);


    }


}