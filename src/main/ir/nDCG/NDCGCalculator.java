package ir.nDCG;

import java.util.List;
import java.util.Arrays;
import java.util.stream.Collectors;


public class NDCGCalculator {

    public static double calculateDCG(List<Double> relevance) {
        double dcg = 0.0;
        for (int i = 0; i < relevance.size(); i++) {
            double rel = relevance.get(i);
            dcg += (Math.pow(2, rel) - 1) / (Math.log(i + 2) / Math.log(2)); // log base 2
        }
        return dcg;
    }

    public static double calculateIDCG(List<Double> relevance) {
        List<Double> sorted_relevance = relevance.stream()
                .sorted((a, b) -> Double.compare(b, a))
                .collect(Collectors.toList());
        return calculateDCG(sorted_relevance);
    }

    public static double calculateNDCG(List<Double> relevance) {
        double dcg = calculateDCG(relevance);
        double iDCG = calculateIDCG(relevance);
        return dcg / iDCG;
    }

    public static void main(String[] args) {
        List<Double> relevance = Arrays.asList(
                1.0, 0.0, 0.0, 1.0, 1.0, 0.0, 1.0, 2.0, 1.0, 0.0,
                0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0,
                1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 2.0,
                0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 2.0,
                0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 3.0
        );

        List<Double> new_relevance = Arrays.asList(
                     1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0, 0.0, 0.0,
                0.0, 2.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 2.0, 0.0,
                0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0,
                0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                2.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0
        );

        // TF-IDF + EuclideanLength
        double nDCG = calculateNDCG(relevance);
        double new_nDCG = calculateNDCG(new_relevance);

        System.out.println("Result of nDCG: " + nDCG);
        System.out.println("Result of new nDCG: " + new_nDCG);
    }
}