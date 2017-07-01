package org.wso2.siddhi.extension.var.models.util;

/**
 * Created by dilini92 on 9/1/16.
 */
public class RealTimeVaRConstants {
    public static String PORTFOLIO = "Portfolio ";
    public static int NUMBER_OF_PARAMETERS = 4;

    public static final String STRING = "STRING";
    public static final String DOUBLE = "DOUBLE";
    public static final String INTEGER = "INT";

    public static final String OUTPUT_NAME = "var";

    /**
     * Array Indices
     */
    public static final int PORTFOLIO_ID_INDEX = 0;
    public static final int QUANTITY_INDEX = 1;
    public static final int SYMBOL_INDEX = 2;
    public static final int PRICE_INDEX = 3;
    public static final int BATCH_SIZE_INDEX = 4;
    public static final int CI_INDEX = 5;
    public static final int MONTE_CARLO_VERTICAL_SIMULATION_COUNT_INDEX = 6;
    public static final int MONTE_CARLO_HORIZONTAL_SIMULATION_COUNT_INDEX = 7;
    public static final int MONTE_CARLO_TIME_SLICE_INDEX = 8;
    public static final String MONTE_CARLO_CALCULATION_TECHNIQUE_AVX = "AVX";
    public static final String MONTE_CARLO_CALCULATION_TECHNIQUE_JAVA_CONCURRENT = "JAVA_CONCURRENT";
    public static final String MONTE_CARLO_CALCULATION_TECHNIQUE_ENV_VARIABLE = "MONTECARLO_SIMULATION";

}
