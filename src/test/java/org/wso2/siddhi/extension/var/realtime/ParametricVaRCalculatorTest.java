package org.wso2.siddhi.extension.var.realtime;

import junit.framework.Assert;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;
import org.wso2.siddhi.extension.var.models.Asset;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;


/**
 * Created by dilip on 06/07/16.
 */
public class ParametricVaRCalculatorTest {

    @Test
    public void testAddEvent() throws Exception {

    }

    @Test
    public void testRemoveEvent() throws Exception {

    }

    @Test
    public void testProcessData() throws Exception {
        int limit=1510;
        double ci=0.95;

        ParametricVaRCalculator calc = new ParametricVaRCalculator(limit,ci,getData());
        Assert.assertEquals(-6065.678833700333,calc.processData());
    }

    protected Map<String, Asset> getData() throws IOException {

        ClassLoader classLoader = getClass().getClassLoader();
        File inputFile = new File(classLoader.getResource("datasorted.xlsx").getFile());
        FileInputStream inputStream = new FileInputStream(inputFile);
        HashMap<String, Asset> data = new HashMap<>();
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);
        int rowCount = sheet.getPhysicalNumberOfRows();
        int columnCount = sheet.getRow(0).getPhysicalNumberOfCells();
        for (int i = 1; i < columnCount; i++) {
            Asset temp = new Asset(500);
            for (int j = 1; j < rowCount; j++) {
                temp.addHistoricalValue(sheet.getRow(j).getCell(i).getNumericCellValue());
            }
            data.put(sheet.getRow(0).getCell(i).getStringCellValue(), temp);
        }

        workbook.close();
        inputStream.close();

        return data;
    }
}