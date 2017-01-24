package org.wso2.siddhi.extension.var.models;

import org.wso2.siddhi.extension.var.realtime.util.RealTimeVaRConstants;

import java.util.Map;

/**
 * Created by dilini92 on 1/9/17.
 */
public class PortfolioFactory {
    public static Portfolio getPortfolio(String type, String ID, Map<String, Integer> assets){
        switch (type){
            case RealTimeVaRConstants.HISTORICAL:
                return new HistoricalPortfolio(ID, assets);

            case RealTimeVaRConstants.PARAMETRIC:
                return new ParametricPortfolio(ID, assets);

            case RealTimeVaRConstants.MONTE_CARLO:
                return new MonteCarloPortfolio(ID, assets);
        }

        return null;
    }
}
