//package org.wso2.siddhi.extension.var.realtime;
//
//import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
//import junit.framework.Assert;
//import org.apache.poi.ss.usermodel.Sheet;
//import org.apache.poi.ss.usermodel.Workbook;
//import org.apache.poi.xssf.usermodel.XSSFWorkbook;
//import org.junit.Test;
//import org.wso2.siddhi.core.config.ExecutionPlanContext;
//import org.wso2.siddhi.core.config.SiddhiContext;
//import org.wso2.siddhi.extension.var.models.Asset;
//import org.wso2.siddhi.extension.var.models.Portfolio;
//
//import javax.sql.DataSource;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.util.*;
//
//
///**
// * Created by dilip on 06/07/16.
// */
//public class ParametricVaRCalculatorTest {
//
//
//    @Test
//    public void testProcessData() throws Exception {
//        int batchSize=251;
//        double ci=0.95;
//
//        ParametricVaRCalculator varCalculator = new ParametricVaRCalculator(batchSize, ci);
//        ExecutionPlanContext executionPlanContext = new ExecutionPlanContext();
//        SiddhiContext siddhiContext = new SiddhiContext();
//        MysqlDataSource dataSource = new MysqlDataSource();
//        dataSource.setURL("jdbc:mysql://localhost:3306/AnalyticsDataSource");
//        dataSource.setUser("root");
//        siddhiContext.addSiddhiDataSource("AnalyticsDataSource",dataSource);
//        executionPlanContext.setSiddhiContext(siddhiContext);
//        varCalculator.getPortfolioValues(executionPlanContext);
//        varCalculator.readAssetList(executionPlanContext);
//
//        Object[] inputData = new Object[2];
//        String[] split;
//        ClassLoader classLoader = getClass().getClassLoader();
//        File file= new File(classLoader.getResource("A50E13000.csv").getFile());
//        Scanner scan = new Scanner(file);
//        int count = 1;
//        while(scan.hasNext()) {
//            split = scan.nextLine().split(",");
//            System.out.println("Data " + (count++) + " " + split[0] + " " + split[1]);
//            inputData[0] = split[0];
//            inputData[1] = Double.valueOf(split[1]);
//            varCalculator.calculateValueAtRisk(inputData);
//            System.out.println("");
//        }
//    }
//
////    protected Map<String, Asset> getData() throws IOException {
////
////        ClassLoader classLoader = getClass().getClassLoader();
////        File inputFile = new File(classLoader.getResource("data.xlsx").getFile());
////        FileInputStream inputStream = new FileInputStream(inputFile);
////        HashMap<String, Asset> data = new HashMap<>();
////        Workbook workbook = new XSSFWorkbook(inputStream);
////        Sheet sheet = workbook.getSheetAt(0);
////        int rowCount = sheet.getPhysicalNumberOfRows();
////        int columnCount = sheet.getRow(0).getPhysicalNumberOfCells();
////        for (int i = 1; i < columnCount; i++) {
////            Asset temp = new Asset(sheet.getRow(0).getCell(i).getStringCellValue());
////            for (int j = 1; j < rowCount; j++) {
////                temp.addHistoricalValue(sheet.getRow(j).getCell(i).getNumericCellValue());
////            }
////            data.put(sheet.getRow(0).getCell(i).getStringCellValue(), temp);
////        }
////
////        workbook.close();
////        inputStream.close();
////
////        return data;
////    }
//
//
//}