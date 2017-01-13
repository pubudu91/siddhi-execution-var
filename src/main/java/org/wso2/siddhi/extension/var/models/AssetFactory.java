package org.wso2.siddhi.extension.var.models;

import org.wso2.siddhi.extension.var.realtime.RealTimeVaRConstants;

/**
 * Created by dilini92 on 1/10/17.
 */
public class AssetFactory {
    public static Asset getAsset(String type){
        switch (type){
            case RealTimeVaRConstants.HISTORICAL:
                return new HistoricalAsset();
//            case RealTimeVaRConstants.PARAMETRIC;

            case RealTimeVaRConstants.MONTE_CARLO:
                return new MonteCarloAsset();
        }
        return null;
    }
}