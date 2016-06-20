package org.wso2.siddhi.extension.var.realtime;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by dilini92 on 6/20/16.
 */
public class HistoricalVarCalculator extends VaRCalculator{

    private List<Double> priceList = new LinkedList<Double>();
    private double price;

    public HistoricalVarCalculator(int calcInt, int limit, double ci) {
        super(calcInt, limit, ci);
    }

    @Override
    protected void addEvent(Object data) {
        eventCount++;
        incCounter++;

        price = ((Number) data).doubleValue();
        priceList.add(price);

    }

    @Override
    protected void removeEvent() {
        priceList.remove(0);
    }

    @Override
    protected Object processData() {
        double var = 0.0;
        DescriptiveStatistics stat = new DescriptiveStatistics();

        Double[] priceArray = priceList.toArray(new Double[eventCount]);

        //calculate the return values from the prices
        double priceReturns[] = new double[priceArray.length - 1];
        for (int i = 0; i < priceArray.length - 1; i++) {
            priceReturns[i] = Math.log(priceArray[i + 1]/priceArray[i]) * 100;
            stat.addValue(priceReturns[i]);
        }

        //arrange the return values in ascending order and get the given percentile value
        return stat.getPercentile((1 - confidenceInterval) * 100);
    }
}
