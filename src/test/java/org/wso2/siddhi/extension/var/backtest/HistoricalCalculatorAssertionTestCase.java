package org.wso2.siddhi.extension.var.backtest;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by flash on 7/14/16.
 */
public class HistoricalCalculatorAssertionTestCase {

    @org.junit.Test
    public void testCalculateVar() throws IOException {

        Map<String, Integer> portfolio = new HashMap<>();
        portfolio.put("FCBK", 210);
        portfolio.put("GOGL", 115);
        portfolio.put("APPL", 280);

        HistoricalCalculatorAssertion calc = new HistoricalCalculatorAssertion(500, portfolio, 0.95, 251);
        calc.AssertMethodValidity(1, 0.05);

    }


}