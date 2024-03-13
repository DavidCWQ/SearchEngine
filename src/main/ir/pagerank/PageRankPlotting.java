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

public class PageRankPlotting extends JFrame {

    private static double[] readDavisTop30() {
        try {
            double[] scores = new double[30];
            String line, path = "src/main/ir/pagerank/davis_top_30.txt";
            BufferedReader in = new BufferedReader(new FileReader(path));
            for (int i = 0; (line = in.readLine()) != null; i++) {
                String[] tokens = line.split(":");
                if (tokens.length == 2) {
                    scores[i] = Double.parseDouble(tokens[1].trim());
                } else {
                    System.err.println("Invalid line format: " + line);
                }
            }
            return scores;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public PageRankPlotting(String title, String[] names, double[] X, double[][] Ys) {
        super(title);

        XYSeriesCollection dataset = new XYSeriesCollection();

        for (int i = 0; i < Ys.length; i++) {
            XYSeries series = new XYSeries(names[i]);
            for (int j = 0; j < X.length; j++) {
                series.add(X[j], Ys[i][j]);
            }
            dataset.addSeries(series);
        }

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Monte Carlo PageRank Goodness Measurement",   // Charts title
                "Number of initiated walks: N = mn",                // X-axis label
                "Sum of 30 highest squared differences: 30 σ^2",    // Y-axis label
                dataset
        );

        ChartPanel chartPanel = new ChartPanel(chart);
        setContentPane(chartPanel);
    }

    public static void main(String[] args) {
        File file = new File("src/main/ir/pagerank/linksDavis.txt");
        PageRank PR = new PageRank(file, 1);

        double[] scores0 = readDavisTop30();

        double[] diff1 = new double[11];
        for (int i = 1; i < 11; i++) {
            PR.setMaxEpochs(i);
            PR.runPageRank(1, false);
            diff1[i] = Math.pow(PR.distance(scores0, PR.getTopScores(30)), 2);
        }

        double[] diff2 = new double[11];
        for (int i = 1; i < 11; i++) {
            PR.setMaxEpochs(i);
            PR.runPageRank(2, false);
            diff2[i] = Math.pow(PR.distance(scores0, PR.getTopScores(30)), 2);
        }

        double[] diff4 = new double[11];
        for (int i = 1; i < 11; i++) {
            PR.setMaxEpochs(i);
            PR.runPageRank(4, false);
            diff4[i] = Math.pow(PR.distance(scores0, PR.getTopScores(30)), 2);
        }

        double[] diff5 = new double[11];
        for (int i = 1; i < 11; i++) {
            PR.setMaxEpochs(i);
            PR.runPageRank(5, false);
            diff5[i] = Math.pow(PR.distance(scores0, PR.getTopScores(30)), 2);
        }

        double[] nValues = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10}; // Example N values
        String[] names = {"Method_1", "Method_2", "Method_4", "Method_5"};
        double[][] yValues = {diff1, diff2, diff4, diff5}; // Example sum of differences

        SwingUtilities.invokeLater(() -> {
            // Create and display the first plot
            PageRankPlotting plot = new PageRankPlotting(
                    "Goodness Measure Plot", names, nValues, yValues);
            plot.setSize(800, 600);
            plot.setLocationRelativeTo(null);
            plot.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            plot.setVisible(true);
        });
    }
}
