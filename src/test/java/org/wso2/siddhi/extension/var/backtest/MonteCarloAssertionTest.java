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

//        MonteCarloAssertion calc;
//        int i = 0, passedCount = 0;
//        Random rnd = new Random();
//        Map<String, Integer> portfolio = new HashMap<>();
//        portfolio.put("IBM", rnd.nextInt(100));
//        portfolio.put("GE", rnd.nextInt(300));
//        portfolio.put("XOM", rnd.nextInt(500));
//        portfolio.put("APPL", rnd.nextInt(500));
//        portfolio.put("MCSFT", rnd.nextInt(500));
//        portfolio.put("NOK", rnd.nextInt(500));
//        portfolio.put("ABC", rnd.nextInt(500));
//        portfolio.put("DEF", rnd.nextInt(500));
//        portfolio.put("GHI", rnd.nextInt(500));
//        portfolio.put("JKL", rnd.nextInt(500));
//        portfolio.put("GES", rnd.nextInt(500));
//        portfolio.put("ASS_1", rnd.nextInt(500));
//        portfolio.put("ASS_2", rnd.nextInt(500));
//        portfolio.put("ASS_3", rnd.nextInt(500));
//
//        while (true) {
//            calc = new MonteCarloAssertion(500, portfolio, 0.95, 251, i);
//            try {
//                if (calc.standardCoverageTest(0.05)) {
//                    passedCount++;
//                }
//            } catch (Exception e) {
//                System.out.println("Done");
//                break;
//            }
//            i++;
//        }
//
//        System.out.println(passedCount);
//        System.out.println("Passed Probability: " + (double) (passedCount * 100 / i) + "%");

    }

    @Test
    public void testCalculateVarEvaluation() throws Exception {

        MonteCarloAssertion calc;
        int i = 0, passedCount = 0;
        Double[][] var_simulation;

        Random rnd = new Random();
        Map<String, Integer> portfolio = new HashMap<>();
        portfolio.put("IBM", 65);
        portfolio.put("GE", 175);
        portfolio.put("XOM", 450);
        portfolio.put("APPL", 300);
        portfolio.put("MCSFT", 440);
//        portfolio.put("NOK", 340);
//        portfolio.put("ABC", 260);
//        portfolio.put("DEF", 235);
//        portfolio.put("GHI", 500);
//        portfolio.put("JKL", 290);
//        portfolio.put("GES", 305);
//        portfolio.put("ASS_1", 480);
//        portfolio.put("ASS_2", 584);
//        portfolio.put("ASS_3", 300);

        calc = new MonteCarloAssertion(10, portfolio, 0.95, 251, 2);
        long begin = System.currentTimeMillis();
        var_simulation = calc.calculateVar_simulate_all();
        long end = System.currentTimeMillis();
        for (int j = 0; j < 10; j++) {
//            System.out.println("Single Simulation " + var_simulation[0][j]);
//            System.out.println("Total Simulation " + var_simulation[1][j]);
            System.out.println((double) (end - begin) / 1000);
        }

//        System.out.println(passedCount);
//        System.out.println("Passed Probability: " + (double) (passedCount * 100 / i) + "%");

    }

}