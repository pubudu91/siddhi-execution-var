package org.wso2.siddhi.extension.var.realtime;

import org.wso2.siddhi.extension.var.models.Asset;

import java.util.*;

/**
 * Created by dilini92 on 6/26/16.
 */
public abstract class VaRPortfolioCalc {
    protected double confidenceInterval = 0.95;
    protected int batchSize = 1000000000;
    protected Map<String, Asset> portfolio;


    public VaRPortfolioCalc(int limit, double ci, Map<String, Asset> assets) {
        confidenceInterval = ci;
        batchSize = limit;
        portfolio = assets;
    }
    protected abstract void addEvent(Object data[]);

    protected abstract void removeEvent(String symbol);

    protected abstract Object processData();

    public Object calculateValueAtRisk(Object data[]) {

        LinkedList<Double> list = portfolio.get(data[0]).getHistoricalValues();
        if(!list.get(list.size() - 1).equals(data[1])){
            addEvent(data);

            if(list.size() > batchSize){
                removeEvent(data[0].toString());
            }

            //counts the number of stock symbols which have already had the given batch size number of events
            int count = 0;
            Set<String> symbols = portfolio.keySet();
            Iterator<String> itr = symbols.iterator();
            while(itr.hasNext()){
                String key = itr.next();
                count += portfolio.get(key).getHistoricalValues().size();
            }

            if(count == batchSize * portfolio.size()){
                return processData();
            }
            return null;
        }else {
            return null;
        }
    }
}
