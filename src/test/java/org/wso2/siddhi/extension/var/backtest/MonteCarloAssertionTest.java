package org.wso2.siddhi.extension.var.backtest;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by flash on 8/24/16.
 */
public class MonteCarloAssertionTest {

    @Test
    public void testCalculateVar() throws Exception {
        Map<String, Integer> portfolio = new HashMap<>();
        portfolio.put("FCBK", 210);
        portfolio.put("GOGL", 115);
        portfolio.put("APPL", 280);

        MonteCarloAssertion calc = new MonteCarloAssertion(500, portfolio, 0.95, 251);
        System.out.println(calc.StandardCoverageTest(0, 0.05));
    }
}