package org.wso2.siddhi.extension.var.realtime;

/**
 * Created by dilini92 on 6/20/16.
 */
public abstract class VaRCalculator {
    protected double confidenceInterval = 0.95;
    protected int eventCount = 0;
    protected int batchSize = 1000000000;
    protected int incCounter =0;
    protected int calcInterval;

    public VaRCalculator(int calcInt, int limit, double ci) {
        confidenceInterval = ci;
        batchSize = limit;
        calcInterval = calcInt;

    }
    protected abstract void addEvent(Object[] data);

    protected abstract void removeEvent();

    protected abstract Object[] processData();

    public Object[] calculateValueAtRisk(Object[] data) {

        addEvent(data);

        // removing oldest events in order to maintain batchsize
        if(eventCount > batchSize){
            eventCount--;
            removeEvent();
        }

        // processing at a user specified calculation interval
        if(incCounter % calcInterval != 0){
            return null;
        }
        return  processData();
    }
}
