/*
 *  This file is part of the computer assignment for the
 *  Information Retrieval course at KTH.
 *
 *  David Cao, 2024
 */

package ir.pagerank;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.io.*;
import java.util.Arrays;

public class RecallPrecisionPlotting  extends JFrame {

    private static final int RECALL = 100;

    private static int[] readRelTop50() {
        try {
            int[] scores = new int[50];
            String line, path = "tasks/relevance.txt";
            BufferedReader in = new BufferedReader(new FileReader(path));
            for (int i = 0; (line = in.readLine()) != null; i++) {
                String[] tokens = line.split(" ");
                if (tokens.length == 3) {
                    scores[i] = Integer.parseInt(tokens[2].trim());
                } else {
                    System.err.println("Invalid line format: " + line);
                }
            }
            return scores;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public RecallPrecisionPlotting(String title, double[] precis, double[] recall) {
        super(title);

        XYSeries series = new XYSeries("Precision-Recall Curve");

        for (int i = 0; i < precis.length; i++) {
            series.add(precis[i], recall[i]);
        }

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series);

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Precision-Recall Curve",
                "Precision",
                "Recall",
                dataset
        );

        ChartPanel chartPanel = new ChartPanel(chart);
        setContentPane(chartPanel);
    }

    public static void main(String[] args) {

        int[] relevance = readRelTop50();
        int[] N = { 10, 20, 30, 40, 50 };

        double[] good = new double[5];
        for (int i = 0; i < 5; i++) {
            // N = { 10, 20, 30, 40, 50 }
            good[i] = Arrays.stream(relevance)
                            .limit((i + 1) * 10)
                            .filter(value -> value > 0)
                            .count();
        }

        double[] precis = { good[0]/N[0], good[1]/N[1], good[2]/N[2], good[3]/N[3], good[4]/N[4] };
        double[] recall = Arrays.stream(good)
                                .map(value -> value / RECALL)
                                .toArray();

        // Create and display the graph
        SwingUtilities.invokeLater(() -> {
            RecallPrecisionPlotting plot = new RecallPrecisionPlotting("Precision-Recall Curve", precis, recall);
            plot.setSize(800, 600);
            plot.setLocationRelativeTo(null);
            plot.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            plot.setVisible(true);
        });
    }
}