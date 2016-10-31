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

        portfolio.put("IBM", rnd.nextInt(100));
        portfolio.put("GE", rnd.nextInt(300));
        portfolio.put("XOM", rnd.nextInt(500));
        portfolio.put("APPL", rnd.nextInt(500));
        portfolio.put("MCSFT", rnd.nextInt(500));
        portfolio.put("NOK", rnd.nextInt(500));
        portfolio.put("ABC", rnd.nextInt(500));
        portfolio.put("DEF", rnd.nextInt(500));
        portfolio.put("GHI", rnd.nextInt(500));
        portfolio.put("JKL", rnd.nextInt(500));
        portfolio.put("GES", rnd.nextInt(500));
        portfolio.put("ASS_1", rnd.nextInt(500));
        portfolio.put("ASS_2", rnd.nextInt(500));
        portfolio.put("ASS_3", rnd.nextInt(500));

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
        System.out.println("Passed Probability: " + (double) (passedCount * 100 / i) + "%");

    }


}