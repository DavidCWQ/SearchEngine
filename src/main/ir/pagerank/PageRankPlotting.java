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

public class PageRankPlotting extends JFrame {

    public PageRankPlotting(String title, double[] nValues, double[] goodnessMeasures) {
        super(title);

        XYSeries series = new XYSeries("Goodness Measure");

        for (int i = 0; i < nValues.length; i++) {
            series.add(nValues[i], goodnessMeasures[i]);
        }

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series);

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Goodness Measure vs. N", // Chart title
                "N",                      // X-axis label
                "Goodness Measure",       // Y-axis label
                dataset
        );

        ChartPanel chartPanel = new ChartPanel(chart);
        setContentPane(chartPanel);
    }

    public static void main(String[] args) {
        double[] nValues = {100, 200, 300, 400, 500}; // Example N values
        double[] goodnessMeasures = {10.5, 20.2, 30.1, 40.3, 50.5}; // Example goodness measures

        SwingUtilities.invokeLater(() -> {
            PageRankPlotting plot = new PageRankPlotting("Goodness Measure Plot", nValues, goodnessMeasures);
            plot.setSize(800, 600);
            plot.setLocationRelativeTo(null);
            plot.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            plot.setVisible(true);
        });
    }
}
