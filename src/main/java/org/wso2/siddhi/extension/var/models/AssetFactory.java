package org.wso2.siddhi.extension.var.models;

import org.wso2.siddhi.extension.var.realtime.util.RealTimeVaRConstants;

/**
 * Created by dilini92 on 1/10/17.
 */
public class AssetFactory {
    public static Asset getAsset(String type, int windowSize){
        switch (type){
            case RealTimeVaRConstants.HISTORICAL:
                return new HistoricalAsset(windowSize);
            case RealTimeVaRConstants.PARAMETRIC:
                return new ParametricAsset(windowSize);
            case RealTimeVaRConstants.MONTE_CARLO:
                return new MonteCarloAsset(windowSize);
        }
        return null;
    }
}
