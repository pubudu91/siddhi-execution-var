package org.wso2.siddhi.extension.var.realtime;

/**
 * Created by dilini92 on 6/20/16.
 */
public abstract class VaRCalculator {
    protected double confidenceInterval = 0.95;
    protected int eventCount = 0;
    protected int batchSize = 1000000000;

    public VaRCalculator(int limit, double ci) {
        confidenceInterval = ci;
        batchSize = limit;

    }
    protected abstract void addEvent(Object data);

    protected abstract void removeEvent();

    protected abstract Object processData();

    public Object calculateValueAtRisk(Object data) {

        addEvent(data);

        // removing oldest events in order to maintain batchsize
        if(eventCount > batchSize){
            eventCount--;
            removeEvent();
            //whenever a new event comes, calculate the var
            return processData();
        }

        // processing at a user specified calculation interval
        if(eventCount % batchSize != 0){
            return null;
        }
        return  processData();
    }
}
