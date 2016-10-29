package org.wso2.siddhi.extension.var.backtest;

import org.wso2.siddhi.extension.var.batchmode.MonteCarloVarCalculator;
import org.wso2.siddhi.extension.var.models.Portfolio;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by flash on 7/14/16.
 */
public class MonteCarloAssertion extends VarModelAssertion {

    public MonteCarloAssertion(int sampleSize, Map<String, Integer> portfolio, double confidenceInterval, int batchSize,int sampleSetNumber) {
        super(sampleSize, portfolio, confidenceInterval, batchSize,sampleSetNumber);
    }

    @Override
    protected Double[] calculateVar() throws IOException {
        Double tempVar[] = new Double[this.getSampleSize()];
        Portfolio _portfolio = new Portfolio(1, this.getPortfolio());
        Map<String, ArrayList<Double>> priceLists = this.getData();
        String[] key = this.getPortfolio().keySet().toArray(new String[this.getPortfolio().size()]);
        MonteCarloVarCalculator calculator = new MonteCarloVarCalculator(this.getBatchSize(), this.getConfidenceInterval(), 2000, 100, 0.01);
        this.setHistoricalValues(calculator);

        for (int i = 0; i < tempVar.length; i++) {
            for (int j = 0; j < key.length; j++) {
                Object input[] = {key[j], priceLists.get(key[j]).get(i + this.getBatchSize() + (this.getSampleSize() *
                        this.getSampleSetNumber()))};
                calculator.addEvent(input);
                calculator.removeEvent(key[j]);
            }
            tempVar[i] = (Double) calculator.processData(_portfolio);
        }
        return tempVar;
    }
}
