package org.wso2.siddhi.extension.var.performance;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.wso2.siddhi.extension.var.models.VaRCalculator;
import org.wso2.siddhi.extension.var.models.historical.HistoricalVaRCalculator;
import org.wso2.siddhi.extension.var.models.montecarlo.MonteCarloVarCalculator;
import org.wso2.siddhi.extension.var.models.parametric.ParametricVaRCalculator;
import org.wso2.siddhi.extension.var.models.util.Event;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;

/**
 * Created by dilini92 on 1/17/17.
 */
public class PerformanceTest {
    private static final int BATCH_SIZE = 251;
    private static final double VAR_CI = 0.95;

    private static final int PRICE_EVENT = 2;
    private static final int PORTFOLIO_EVENT = 4;
    private static final int FILL_COUNT = 6301;

    private DescriptiveStatistics averageTime;

    public PerformanceTest() {
        averageTime = new DescriptiveStatistics();
    }

    private ArrayList<Event> readPerformanceTestData() throws IOException {
//        ClassLoader classLoader = getClass().getClassLoader();
//        FileReader reader = new FileReader(new File(classLoader.getResource("PerformanceTestData.csv").getFile()));
        FileReader reader = new FileReader(new File("/home/dilini92/Sem7/siddhi new/modules/siddhi-extensions/var/src/" +
                "test/resources/PerformanceTestData.csv"));
        BufferedReader bufferedReader = new BufferedReader(reader);
        String line;
        String data[];
        ArrayList<Event> list = new ArrayList();

        while ((line = bufferedReader.readLine()) != null) {
            data = line.split(",");
            if (data.length == PRICE_EVENT) {
                list.add(new Event(null, 0, data[0], Double.parseDouble(data[1])));
            } else if (data.length == PORTFOLIO_EVENT) {
                list.add(new Event(data[0], Integer.parseInt(data[1]), data[2], Double.parseDouble(data[1])));
            }
        }

        return list;
    }

    private double runPerformanceTest() {
        VaRCalculator varCalculator = new HistoricalVaRCalculator(BATCH_SIZE, VAR_CI);
//        VaRCalculator varCalculator = new ParametricVaRCalculator(BATCH_SIZE, VAR_CI);
//        VaRCalculator varCalculator = new MonteCarloVarCalculator(BATCH_SIZE, VAR_CI, 2500, 100, 0.01);
        System.out.println("===============" + varCalculator.getClass().getSimpleName() + "===============");
        long start = 0, stop = 0;
        int count = 0;
        Event event;
        try {

            ArrayList<Event> list = readPerformanceTestData();
            Iterator<Event> iterator = list.iterator();

            while (iterator.hasNext()) {
                count++;
                event = iterator.next();

                if(count > FILL_COUNT){
                    start = System.currentTimeMillis();
                    varCalculator.calculateValueAtRisk(event);
                    stop = System.currentTimeMillis();
                    averageTime.addValue(stop - start);
                }else{
                    varCalculator.calculateValueAtRisk(event);
                }
            }

            return averageTime.getMean();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return 0.0;
    }

    public static void main(String[] args) {
        PerformanceTest test = new PerformanceTest();
        double averageTime = test.runPerformanceTest();
        System.out.println("Average Execution Time: " + averageTime + " ms");
    }
}
