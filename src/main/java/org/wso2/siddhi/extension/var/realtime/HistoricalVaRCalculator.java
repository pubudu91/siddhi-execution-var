package org.wso2.siddhi.extension.var.realtime;

import org.wso2.siddhi.extension.var.models.HistoricalAsset;
import org.wso2.siddhi.extension.var.models.HistoricalPortfolio;
import org.wso2.siddhi.extension.var.models.Portfolio;

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
        setType(RealTimeVaRConstants.HISTORICAL);
    }

    /**
     * @return the var of the portfolio
     * Calculate the contribution of the changed asset to the portfolio and then adjust the previous VaR value
     * using Historical data
     */
    @Override
    public Object processData(Portfolio portfolio) {

        HistoricalPortfolio historicalPortfolio = (HistoricalPortfolio) portfolio;
        HistoricalAsset asset = (HistoricalAsset)getAssetList().get(getSymbol());
        if(asset.getNumberOfHistoricalValues() > 1) {
            double var = historicalPortfolio.getHistoricalVarValue();

            double previousReturnValue = asset.getPreviousLossReturn();
            double currentReturnValue = asset.getCurrentLossReturn();

            int previousShares = historicalPortfolio.getPreviousShare(getSymbol());
            int currentShares = historicalPortfolio.getCurrentShare(getSymbol());

            double previousPrice = asset.getPriceBeforeLastPrice();
            double currentPrice = asset.getCurrentStockPrice();

            var -= previousReturnValue * previousPrice * previousShares;
            var += currentReturnValue * currentPrice * currentShares;

            asset.setPreviousLossReturn(currentReturnValue);
            historicalPortfolio.setHistoricalVarValue(var);

            return var;
        }
        return null;
    }

    @Override
    public double replaceAssetSimulation() {
        HistoricalAsset asset = (HistoricalAsset)getAssetList().get(getSymbol());

        if(asset.getNumberOfHistoricalValues() > 1) {
            asset.setPreviousLossReturn(asset.getCurrentLossReturn());
            double currentReturnValue = asset.getReturnValueSet().getPercentile((1 - getConfidenceInterval()) * 100);
            asset.setCurrentLossReturn(currentReturnValue);
        }

        if(getPortfolioID() > 0) {
            HistoricalPortfolio portfolio = (HistoricalPortfolio) getPortfolioList().get(getPortfolioID());
            int previousShares = portfolio.getCurrentShare(getSymbol()) - getShares();
            portfolio.setPreviousShare(getSymbol(), previousShares);
        }
        return 0;
    }
}
