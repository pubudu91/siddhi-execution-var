package org.wso2.siddhi.extension.var.backtest;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.util.CombinatoricsUtils;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.wso2.siddhi.extension.var.models.Asset;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by flash on 7/12/16.
 */
public abstract class VarModelAssertion {
    private Double var[];
    private double actualValue[];
    private int sampleSize = 250;
    private double confidenceInterval = 0.95;
    private double significanceLevelForBacktest = 0.05;
    private int batchSize = 250;
    private Map<String, Asset> portfolio = null;

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public double getConfidenceInterval() {
        return confidenceInterval;
    }

    public void setConfidenceInterval(double confidenceInterval) {
        this.confidenceInterval = confidenceInterval;
    }

    public Map<String, Asset> getPortfolio() {
        return portfolio;
    }

    public VarModelAssertion(int sampleSize, Map<String, Asset> portfolio, double confidenceInterval, int batchSize) {
        this.sampleSize = sampleSize;
        this.batchSize = batchSize;
        this.portfolio = portfolio;
        this.confidenceInterval = confidenceInterval;
    }

    public int getSampleSize() {
        return sampleSize;
    }

    public void setSampleSize(int sampleSize) {
        this.sampleSize = sampleSize;
    }

    protected abstract Double[] calculateVar() throws IOException;

    /**
     * send sample set e.g:- by sending 1, calculation will be done from first
     * set of sample size of the price list
     *
     * @param priceList
     * @param sampleSet
     * @return
     */
    protected double[] calculateOriginal(Map<String, ArrayList<Double>> priceList, int sampleSet) {
        double[] actualValue = new double[this.sampleSize];
        ArrayList<Double> priceListTemp = null;
        int sharesAmount = 0, endOfList = 0;

        String[] key = this.portfolio.keySet().toArray(new String[this.portfolio.size()]);
        for (int i = 0; i < sampleSize; i++) {
            actualValue[i] = 0;
        }
        for (int i = 0; i < key.length; i++) {
            priceListTemp = priceList.get(key[i]);
//            endOfList = (sampleSet * sampleSize) > priceListTemp.size() ? priceListTemp.size() : sampleSet * sampleSize;
            endOfList = (sampleSet + 1) * sampleSize;
            sharesAmount = portfolio.get(key[i]).getNumberOfShares();
            for (int j = sampleSize * (sampleSet); j < endOfList; j++) {
                actualValue[j - (sampleSize * sampleSet)] += sharesAmount * priceListTemp.get(j);
            }
        }
        return actualValue;
    }

    protected HashMap<String, ArrayList<Double>> getData() throws IOException {

        ClassLoader classLoader = getClass().getClassLoader();
        File inputFile = new File(classLoader.getResource("data.xlsx").getFile());
        FileInputStream inputStream = new FileInputStream(inputFile);
        HashMap<String, ArrayList<Double>> data = new HashMap<>();
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);
        int rowCount = sheet.getPhysicalNumberOfRows();
        int columnCount = sheet.getRow(0).getPhysicalNumberOfCells();

        for (int i = 1; i < columnCount; i++) {
            ArrayList<Double> temp = new ArrayList<>();
            for (int j = 1; j < rowCount; j++) {
                temp.add(sheet.getRow(j).getCell(i).getNumericCellValue());
            }
            data.put(sheet.getRow(0).getCell(i).getStringCellValue(), temp);
        }

        workbook.close();
        inputStream.close();

        return data;
    }

    public boolean AssertMethodValidity(int sampleSet, double significanceLevelForBacktest) throws IOException {
        int numberOfExceptions = 0;
        double actualPriceTemp = 0;
        this.setHistoricalValues();
        this.var = this.calculateVar();
        this.actualValue = this.calculateOriginal(this.getData(), sampleSet);
        NormalDistribution dist = new NormalDistribution();

        for (int i = 0; i < sampleSize - 1; i++) {
            actualPriceTemp = this.actualValue[i + 1] - this.actualValue[i];
            if (this.var[i] > 0 && actualPriceTemp < 0) {
                numberOfExceptions++;
            } else if (this.var[i] > 0 && actualPriceTemp > 0) {
                if (actualPriceTemp > this.var[i]) {
                    numberOfExceptions++;
                }
            } else if (this.var[i] > 0 && actualPriceTemp > 0) {
                if (Math.abs(this.var[i]) < actualPriceTemp) {
                    numberOfExceptions++;
                }
            }
        }

        double leftEnd = dist.inverseCumulativeProbability(significanceLevelForBacktest / 2);
        leftEnd = leftEnd * Math.sqrt(sampleSet * this.confidenceInterval * (1 - this.confidenceInterval)) +
                (sampleSet * this.confidenceInterval);
        double rightEnd = dist.inverseCumulativeProbability(1 - (significanceLevelForBacktest / 2));
        rightEnd = rightEnd * Math.sqrt(sampleSet * this.confidenceInterval * (1 - this.confidenceInterval)) +
                (sampleSet * this.confidenceInterval);

        if (rightEnd >= numberOfExceptions && leftEnd <= numberOfExceptions) {
            return true;
        } else {
            return false;
        }
    }

    public void setHistoricalValues() throws IOException {
        String[] key = this.portfolio.keySet().toArray(new String[this.portfolio.size()]);
        ArrayList<Double> tempList = null;
        Map<String, ArrayList<Double>> priceLists = this.getData();
        for (int i = 0; i < key.length; i++) {
            tempList = priceLists.get(key[i]);
            for (int j = 0; j < this.batchSize; j++) {
                portfolio.get(key[i]).addHistoricalValue(tempList.get(j));
            }
        }
    }

}
