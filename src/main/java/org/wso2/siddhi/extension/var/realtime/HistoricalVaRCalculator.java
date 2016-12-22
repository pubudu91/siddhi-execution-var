package org.wso2.siddhi.extension.var.realtime;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.wso2.siddhi.extension.var.models.Asset;
import org.wso2.siddhi.extension.var.models.Portfolio;

import java.util.*;

/**
 * Created by dilini92 on 6/26/16.
 */
public class HistoricalVaRCalculator extends VaRPortfolioCalc {

    /**
     *
     * @param limit
     * @param ci
     */
    public HistoricalVaRCalculator(int limit, double ci) {
        super(limit, ci);
    }
        private DescriptiveStatistics stat;
    /**
     * @return the var of the portfolio
     */
    @Override
    public Object processData(Portfolio portfolio) {
        Asset asset = assetList.get(symbol);
        if(asset.getNumberOfHistoricalValues() > 1) {
            double var = portfolio.getHistoricalVarValue();
            double previousReturnValue = asset.getPreviousLossReturn();
            double currentReturnValue = asset.getReturnValueSet().getPercentile((1 - confidenceInterval) * 100);
            int previousShares = portfolio.getPreviousShares();
            int currentShares = portfolio.getAssets().get(symbol);
            double previousPrice = asset.getPriceBeforeLastPrice();
            double currentPrice = asset.getCurrentStockPrice();

            var -= previousReturnValue * previousPrice * previousShares;
            var += currentReturnValue * currentPrice * currentShares;

            asset.setPreviousLossReturn(currentReturnValue);
            portfolio.setHistoricalVarValue(var);

            return var;
        }
        return null;
    }

    @Override
    public double replaceAssetSimulation() {
        return 0;
    }
}
