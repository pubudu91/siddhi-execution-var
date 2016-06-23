package org.wso2.siddhi.extension.var.realtime;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by dilini92 on 6/20/16.
 */

public class ParametricVarCalculator extends VaRCalculator {

    private List<Double> priceList = new LinkedList<Double>();

    public ParametricVarCalculator(int limit, double ci) {
        super(limit, ci);
    }

    @Override
    protected void addEvent(Object data) {
        eventCount++;

        priceList.add(((Number) data).doubleValue());

    }

    @Override
    protected void removeEvent() {
        priceList.remove(0);
    }

    @Override
    protected Object processData() {
        double[] returns = new double[priceList.size()-1];
        DescriptiveStatistics stat = new DescriptiveStatistics();
        for (int i = 0; i < priceList.size()-1; i++) {
            stat.addValue(Math.log(priceList.get(i+1)/priceList.get(i))*100);
        }

        double mean = stat.getMean();
        //System.out.println("Mean : " + mean);
        double std = stat.getStandardDeviation();
        //System.out.println("Std : " + std);
        double var = mean - 1.96*std;
        //System.out.println("Var : " + var);

        return var;
    }
}
