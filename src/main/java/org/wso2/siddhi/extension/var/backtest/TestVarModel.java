package org.wso2.siddhi.extension.var.backtest;

import org.wso2.siddhi.extension.var.models.Asset;
import org.wso2.siddhi.extension.var.realtime.VaRPortfolioCalc;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by flash on 7/12/16.
 */
public class TestVarModel {
    private double var[];
    private double actualValue[];
    private int sampleSize = 250;
    private VaRPortfolioCalc calcInstance;

    public TestVarModel() {

    }

    public TestVarModel(VaRPortfolioCalc calcInstance) {
        this.calcInstance = calcInstance;
    }

    public void generateVar() {
        Map<String, Asset> assets = new HashMap<>();
        int limit = 250;
        double confidenceInterval = 0.95;
        Object[] input = null;

        calcInstance.calculateValueAtRisk(input);
    }

    public void generateOriginalLoss() {

    }

    public HashMap<String,ArrayList<Double>> getData() throws Exception{

        ClassLoader classLoader = getClass().getClassLoader();
        File inputFile = new File(classLoader.getResource("data.xlsx").getFile());
        FileInputStream inputStream = new FileInputStream(inputFile);
        HashMap<String,ArrayList<Double>> data = new HashMap<>();
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);
        int rowCount = sheet.getPhysicalNumberOfRows();
        int columnCount = sheet.getRow(0).getPhysicalNumberOfCells();

        for (int i = 1; i < columnCount; i++) {
            ArrayList<Double> temp = new ArrayList<>();
            for (int j = 1; j < rowCount; j++) {
                temp.add(sheet.getRow(j).getCell(i).getNumericCellValue());
            }
            data.put(sheet.getRow(0).getCell(i).getStringCellValue(),temp);
        }

        workbook.close();
        inputStream.close();

        return data;
    }

    public static void main(String[] args) {
        TestVarModel tm = new TestVarModel();
        try {
            HashMap<String,ArrayList<Double>> data = tm.getData();
            Set<String> keys = data.keySet();
            String symbols[] = keys.toArray(new String[keys.size()]);
            for (int i = 0; i < symbols.length ; i++) {
                System.out.print(symbols[i] + " ");
                ArrayList<Double> temp = data.get(symbols[i]);
                for (int j = 0; j < temp.size() ; j++) {
                    System.out.print(temp.get(j) + " ");
                }
                System.out.println("");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
