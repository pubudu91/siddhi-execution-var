package org.wso2.siddhi.extension.var.backtest;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.wso2.siddhi.extension.var.models.Asset;
import org.wso2.siddhi.extension.var.realtime.VaRPortfolioCalc;

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
    private int batchSize = 250;
    private Map<String, Integer> portfolio = null;
    private int sampleSetNumber = 0;

    public int getSampleSetNumber() {
        return sampleSetNumber;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public double getConfidenceInterval() {
        return confidenceInterval;
    }

    public Map<String, Integer> getPortfolio() {
        return portfolio;
    }

    public VarModelAssertion(int sampleSize, Map<String, Integer> portfolio, double confidenceInterval, int batchSize, int sampleSetNumber) {
        this.sampleSize = sampleSize;
        this.batchSize = batchSize;
        this.portfolio = portfolio;
        this.sampleSetNumber = sampleSetNumber;
        this.confidenceInterval = confidenceInterval;
    }

    public int getSampleSize() {
        return sampleSize;
    }

    protected abstract Double[] calculateVar() throws IOException;

    /**
     * send sample set e.g:- by sending 0, calculation will be done from first
     * set of sample size of the price list
     *
     * @param priceList
     * @param sampleSetNumber
     * @return
     */
    protected double[] calculateOriginal(Map<String, ArrayList<Double>> priceList, int sampleSetNumber) throws ArrayIndexOutOfBoundsException {
        double[] actualValue = new double[this.sampleSize];
        ArrayList<Double> priceListTemp = null;
        int sharesAmount = 0, endOfList = 0;

        String[] key = this.portfolio.keySet().toArray(new String[this.portfolio.size()]);
        for (int i = 0; i < sampleSize; i++) {
            actualValue[i] = 0;
        }

        for (int i = 0; i < key.length; i++) {
            priceListTemp = priceList.get(key[i]);
            endOfList = (sampleSetNumber + 1) * sampleSize;

            sharesAmount = portfolio.get(key[i]);
            for (int j = this.getBatchSize() + (sampleSize * sampleSetNumber); j < this.getBatchSize() + endOfList; j++) {
                actualValue[j - (sampleSize * sampleSetNumber) - this.getBatchSize()] += sharesAmount * priceListTemp.get(j);
            }
        }
        return actualValue;
    }

    protected HashMap<String, ArrayList<Double>> getData() throws IOException {

        ClassLoader classLoader = getClass().getClassLoader();
        File inputFile = new File(classLoader.getResource("Stock_Data_edited.xlsx").getFile());
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

    public boolean standardCoverageTest(double significanceLevelForBacktest) throws IOException, ArrayIndexOutOfBoundsException {
        int numberOfExceptions = 0;
        double actualLoss = 0;
        this.var = this.calculateVar();
        this.actualValue = this.calculateOriginal(this.getData(), sampleSetNumber);

        NormalDistribution dist = new NormalDistribution();

        for (int i = 0; i < sampleSize - 1; i++) {
            actualLoss = this.actualValue[i + 1] - this.actualValue[i];
            if (this.var[i] > actualLoss) {
                numberOfExceptions++;
            }
        }
//        System.out.println("Exceptions :" + numberOfExceptions);
        double leftEnd = dist.inverseCumulativeProbability(significanceLevelForBacktest / 2);

        leftEnd = leftEnd * Math.sqrt(sampleSize * this.confidenceInterval * (1 - this.confidenceInterval)) +
                (sampleSize * (1 - this.confidenceInterval));
        leftEnd = Math.floor(leftEnd);
        double rightEnd = dist.inverseCumulativeProbability(1 - (significanceLevelForBacktest / 2));
        rightEnd = rightEnd * Math.sqrt(sampleSize * this.confidenceInterval * (1 - this.confidenceInterval)) +
                (sampleSize * (1 - this.confidenceInterval));
        rightEnd = Math.ceil(rightEnd);

        if (rightEnd >= numberOfExceptions && leftEnd <= numberOfExceptions) {
            return true;
        } else {
            return false;
        }
    }

//    public void setHistoricalValues(VaRPortfolioCalc portfolioHead) throws IOException {
//        String[] key = this.portfolio.keySet().toArray(new String[this.portfolio.size()]);
//        Map<String, Asset> assetList = new HashMap<>();
//        Asset tempAsset;
//        HashMap<String, ArrayList<Double>> assetListPool = this.getData();
//        ArrayList<Double> priceList;
//        for (int i = 0; i < key.length; i++) {
//            priceList = assetListPool.get(key[i]);
//            tempAsset = new Asset(key[i]);
//
//            for (int j = 0; j < this.getBatchSize(); j++) {
//                tempAsset.addHistoricalValue(priceList.get(j));
//            }
//            assetList.put(key[i], tempAsset);
//        }
//        portfolioHead.assetList = assetList;
//    }

}
