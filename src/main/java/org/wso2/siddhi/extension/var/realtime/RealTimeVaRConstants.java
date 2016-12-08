package org.wso2.siddhi.extension.var.realtime;

/**
 * Created by dilini92 on 9/1/16.
 */
public class RealTimeVaRConstants {
    public static String PORTFOLIO = "Portfolio ";
    public static String DATA_SOURCE_NAME = "VAR_CONFIGURATIONS";

    //SQL queries
    public static String PORTFOLIO_IDS_SQL = "SELECT distinct(portfolioID) FROM portfolio natural join " +
            "portfolioDetails";
    public static String PORTFOLIO_DETAILS_SQL = "SELECT symbol, noOfShares from portfolioDetails where portfolioID = ";
    public static String SYMBOLS_SQL = "select distinct(symbol) from symbol natural join portfolioDetails";

    public static int NUMBER_OF_PARAMETERS = 5;
}
