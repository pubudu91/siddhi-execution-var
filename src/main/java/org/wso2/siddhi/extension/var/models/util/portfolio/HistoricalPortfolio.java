package org.wso2.siddhi.extension.var.models.util.portfolio;

import java.util.Map;

/**
 * Created by dilini92 on 1/9/17.
 */
public class HistoricalPortfolio extends Portfolio {
    private double[] cumulativeLossValues;

    public HistoricalPortfolio(String ID, Map<String, Integer> assets) {
        super(ID, assets);
    }

    public double[] getCumulativeLossValues() {
        return cumulativeLossValues;
    }

    public void setCumulativeLossValues(double[] cumulativeLossValues) {
        this.cumulativeLossValues = cumulativeLossValues;
    }
}
