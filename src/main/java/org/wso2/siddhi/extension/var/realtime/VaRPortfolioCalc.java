package org.wso2.siddhi.extension.var.realtime;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by dilini92 on 6/26/16.
 */
public abstract class VaRPortfolioCalc {
    protected double confidenceInterval = 0.95;
    protected int batchSize = 1000000000;
    protected String symbols[] = null;
    protected int noOfShares[] = null;
    protected Map<String, List<Double>> priceLists = new HashMap<String, List<Double>>();

    public VaRPortfolioCalc(int limit, double ci, String symbs[], int shares[]) {
        confidenceInterval = ci;
        batchSize = limit;
        symbols = symbs;
        noOfShares = shares;
    }
    protected abstract void addEvent(Object data[]);

    protected abstract void removeEvent(String symbol);

    protected abstract Object processData(String symbols[]);

    public Object calculateValueAtRisk(Object data[]) {

        List<Double> list = priceLists.get(data[0]);
        if(!list.get(list.size() - 1).equals(data[1])){
            addEvent(data);

            if(priceLists.get(data[0].toString()).size() > batchSize){
                removeEvent(data[0].toString());
            }

            //counts the number of stock symbols which have already had the given batch size number of events
            int count = 0;
            for (int i = 0; i < symbols.length; i++) {
                if(data[0].toString().equals(symbols[i])){
                    count += priceLists.get(symbols[i]).size();
                }
            }

            if(count == batchSize * symbols.length){
                return processData(symbols);
            }
            return null;
        }else {
            return null;
        }
    }
}
