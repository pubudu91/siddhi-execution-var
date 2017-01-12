package org.wso2.siddhi.extension.var.backtest;

import org.wso2.siddhi.extension.var.batchmode.MonteCarloVarCalculator;
import org.wso2.siddhi.extension.var.models.Portfolio;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

/**
 * Created by flash on 7/14/16.
 */
public class MonteCarloAssertion extends VarModelAssertion {
    Random rand = new Random();

    public MonteCarloAssertion(int sampleSize, Map<String, Integer> portfolio, double confidenceInterval, int batchSize, int sampleSetNumber) {
        super(sampleSize, portfolio, confidenceInterval, batchSize, sampleSetNumber);
    }

    @Override
    protected Double[] calculateVar() throws IOException {
        Double tempVar[] = new Double[this.getSampleSize()];
        Portfolio _portfolio = new Portfolio(1, this.getPortfolio());
        Map<String, ArrayList<Double>> priceLists = this.getData();
        String[] key = this.getPortfolio().keySet().toArray(new String[this.getPortfolio().size()]);
        MonteCarloVarCalculator calculator = new MonteCarloVarCalculator(this.getBatchSize(), this.getConfidenceInterval(), 2000, 100, 0.01);
//        this.setHistoricalValues(calculator);
        Object input[] = new Object[2];
        for (int i = 0; i < tempVar.length; i++) {
//            int j = rand.nextInt(key.length - 1);
            for (int j = 0; j < key.length; j++) {
//                Object input[] = {key[j], priceLists.get(key[j]).get(i + this.getBatchSize() + (this.getSampleSize() *
//                        this.getSampleSetNumber()))};
                input[0] = key[j];
                input[1] = priceLists.get(key[j]).get(i + this.getBatchSize() + (this.getSampleSize() * this.getSampleSetNumber()));
                calculator.addEvent(input);
                calculator.removeEvent(key[j]);
            }

//            _portfolio.setIncomingEventLabel((String) input[0]);
            tempVar[i] = (Double) calculator.processData(_portfolio);
        }
        return tempVar;
    }

    //delete after the comparison being done

    protected Double[][] calculateVar_simulate_all() throws IOException {
        Double tempVar[][] = new Double[2][this.getSampleSize()];
        Portfolio _portfolio = new Portfolio(1, this.getPortfolio());
        Map<String, ArrayList<Double>> priceLists = this.getData();
        String[] key = this.getPortfolio().keySet().toArray(new String[this.getPortfolio().size()]);
        MonteCarloVarCalculator calculator = new MonteCarloVarCalculator(this.getBatchSize(), this.getConfidenceInterval(), 2000, 100, 0.01);
//        this.setHistoricalValues(calculator);
        Object input[] = new Object[2];
        for (int i = 0; i < this.getSampleSize(); i++) {
//            int j = rand.nextInt(key.length - 1);
//            Object input[] = {key[j], priceLists.get(key[j]).get(i + this.getBatchSize() + (this.getSampleSize() *
//                    this.getSampleSetNumber()))};
//            calculator.addEvent(input);
//            calculator.removeEvent(key[j]);

//            for (int j = 0; j < key.length; j++) {
//                Object input[] = {key[j], priceLists.get(key[j]).get(i + this.getBatchSize() + (this.getSampleSize() *
//                        this.getSampleSetNumber()))};
//                input[0] = key[j];
//                input[1] = priceLists.get(key[j]).get(i + this.getBatchSize() + (this.getSampleSize() * this.getSampleSetNumber()));
//                calculator.addEvent(input);
//                calculator.removeEvent(key[j]);

//            }
            int j = 0;
            input[0] = key[j];
            input[1] = priceLists.get(key[j]).get(i + this.getBatchSize() + (this.getSampleSize() * this.getSampleSetNumber()));
            calculator.addEvent(input);
            calculator.removeEvent(key[j]);
//            _portfolio.setIncomingEventLabel((String) input[0]);
            tempVar[0][i] = (Double) calculator.processData(_portfolio);
//            tempVar[1][i] = (Double) calculator.processData_total(_portfolio);
        }
        return tempVar;
    }
}
