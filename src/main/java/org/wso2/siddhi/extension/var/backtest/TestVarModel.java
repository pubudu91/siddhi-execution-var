package org.wso2.siddhi.extension.var.backtest;

import org.wso2.siddhi.extension.var.models.Asset;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by flash on 7/12/16.
 */
public abstract class TestVarModel {
    private double var[];
    private double actualValue[];
    private int sampleSize = 250;
    private Map<String, Asset> portfolio = null;

    public TestVarModel(int sampleSize, Map<String, Asset> portfolio) {
        this.sampleSize = sampleSize;
        this.portfolio = portfolio;
    }

    public int getSampleSize() {
        return sampleSize;
    }

    public void setSampleSize(int sampleSize) {
        this.sampleSize = sampleSize;
    }

    protected abstract double[] calculateVar();

    /**
     * send sample set e.g:- by sending 1, calculation will be done from first
     * set of sample size of the price list
     *
     * @param priceList
     * @param sampleSet
     * @return
     */
    protected double[] calculateOriginal(Map<String, ArrayList<Double>> priceList, int sampleSet) {
        actualValue = new double[sampleSize];
        ArrayList<Double> priceListTemp = null;
        int sharesAmount = 0, endOfList = 0;

        String[] key = this.portfolio.keySet().toArray(new String[this.portfolio.size()]);
        for (int i = 0; i < sampleSize; i++) {
            actualValue[i] = 0;
        }
        for (int i = 0; i < key.length; i++) {
            priceListTemp = priceList.get(key[i]);
            endOfList = (sampleSet * sampleSize) > priceListTemp.size() ? priceListTemp.size() : sampleSet * sampleSize;
            sharesAmount = portfolio.get(key[i]).getNumberOfShares();
            for (int j = sampleSize * (sampleSet - 1); j < endOfList; j++) {
                actualValue[i] += sharesAmount * priceListTemp.get(i);
            }
        }
        return null;
    }

}
