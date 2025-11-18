package lab5;

import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;



import javax.swing.*;

public class Main {
    private static final int CORES = 12;
    private static final int REPETITIONS = 10;
    private static final int MAX_ITER = 2000; // Increased for longer computation time

    public static void main(String[] args) {
        // Uncomment to display the Mandelbrot set
        // System.out.println("=== Mandelbrot single thread ===\n");
        // new Mandelbrot().setVisible(true);

        // System.out.println("=== Mandelbrot with many threads ===\n");
        // new Mandelbrot(12, 120, 570).setVisible(true);

        // Run performance tests
        runPerformanceTests();
    }

    private static void runPerformanceTests() {
        System.out.println("Mandelbrot Performance Test");
        System.out.println("CPU Cores: " + CORES);
        System.out.println("Repetitions per test: " + REPETITIONS);
        System.out.println("MAX_ITER: " + MAX_ITER);
        System.out.println("\n" + "=".repeat(100) + "\n");

        // Test configurations
        int[] threadCounts = {1, CORES, CORES * 4}; // 1, 12, 48
        String[] threadLabels = {"1 thread", CORES + " threads (cores)", (CORES * 4) + " threads (4x cores)"};

        List<TestResult> results = new ArrayList<>();

        for (int t = 0; t < threadCounts.length; t++) {
            int numThreads = threadCounts[t];
            String threadLabel = threadLabels[t];

            // Test 1: Same number of tasks as threads
            int numTasks1 = numThreads;
            TestResult result1 = runTest(numThreads, numTasks1, MAX_ITER, REPETITIONS,
                    threadLabel, "tasks = threads (" + numTasks1 + ")");
            results.add(result1);

            // Test 2: 10x more tasks than threads
            int numTasks2 = numThreads * 10;
            TestResult result2 = runTest(numThreads, numTasks2, MAX_ITER, REPETITIONS,
                    threadLabel, "tasks = 10x threads (" + numTasks2 + ")");
            results.add(result2);

            // Test 3: Each task is one pixel
            int numTasks3 = 800 * 600; // Total pixels
            TestResult result3 = runTest(numThreads, numTasks3, MAX_ITER, REPETITIONS,
                    threadLabel, "each pixel = task (" + numTasks3 + ")");
            results.add(result3);
        }

        // Print results table
        System.out.println("\n" + "=".repeat(100));
        System.out.println("RESULTS SUMMARY");
        System.out.println("=".repeat(100));
        System.out.printf("%-25s %-30s %15s %15s%n", "Threads", "Tasks", "Avg Time (ms)", "Std Dev (ms)");
        System.out.println("-".repeat(100));

        TestResult fastest = results.get(0);
        for (TestResult result : results) {
            System.out.printf("%-25s %-30s %15.2f %15.2f%n",
                    result.threadLabel, result.taskLabel, result.avgTime, result.stdDev);
            if (result.avgTime < fastest.avgTime) {
                fastest = result;
            }
        }

        System.out.println("=".repeat(100));
        System.out.println("\nFASTEST CONFIGURATION:");
        System.out.println("Threads: " + fastest.threadLabel);
        System.out.println("Tasks: " + fastest.taskLabel);
        System.out.printf("Average Time: %.2f ms%n", fastest.avgTime);
        System.out.printf("Standard Deviation: %.2f ms%n", fastest.stdDev);
        System.out.println("\n" + "=".repeat(100));

        // Show chart
        showChart(results);
    }

    private static TestResult runTest(int numThreads, int numTasks, int maxIter,
                                      int repetitions, String threadLabel, String taskLabel) {
        System.out.println("\nTesting: " + threadLabel + ", " + taskLabel);
        System.out.print("Progress: ");

        long[] times = new long[repetitions];

        for (int i = 0; i < repetitions; i++) {
            System.gc(); // Suggest garbage collection before each test
            try {
                Thread.sleep(100); // Small delay between tests
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            long startTime = System.nanoTime();
            new Mandelbrot(numThreads, numTasks, maxIter);
            long endTime = System.nanoTime();

            times[i] = endTime - startTime;
            System.out.print(".");
        }

        System.out.println(" Done!");

        // Calculate average and standard deviation
        double avgNano = 0;
        for (long time : times) {
            avgNano += time;
        }
        avgNano /= repetitions;

        double variance = 0;
        for (long time : times) {
            variance += Math.pow(time - avgNano, 2);
        }
        variance /= repetitions;
        double stdDevNano = Math.sqrt(variance);

        // Convert to milliseconds
        double avgMs = avgNano / 1_000_000.0;
        double stdDevMs = stdDevNano / 1_000_000.0;

        System.out.printf("Average: %.2f ms, Std Dev: %.2f ms%n", avgMs, stdDevMs);

        return new TestResult(threadLabel, taskLabel, avgMs, stdDevMs);
    }

    private static void showChart(List<TestResult> results) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (TestResult result : results) {
            String label = result.threadLabel + " / " + result.taskLabel;
            dataset.addValue(result.avgTime, "Average Time (ms)", label);
            dataset.addValue(result.stdDev, "Std Dev (ms)", label);
        }

        JFreeChart barChart = ChartFactory.createBarChart(
                "Mandelbrot Performance",
                "Configuration",
                "Time (ms)",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false);

        // Rotate x-axis labels
        CategoryPlot plot = (CategoryPlot) barChart.getPlot();
        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setCategoryLabelPositions(
                org.jfree.chart.axis.CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 4.0)
        );

        // Optional: increase bar width to make chart clearer
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setMaximumBarWidth(0.05);

        JFrame frame = new JFrame("Performance Chart");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new ChartPanel(barChart));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    static class TestResult {
        String threadLabel;
        String taskLabel;
        double avgTime;
        double stdDev;

        TestResult(String threadLabel, String taskLabel, double avgTime, double stdDev) {
            this.threadLabel = threadLabel;
            this.taskLabel = taskLabel;
            this.avgTime = avgTime;
            this.stdDev = stdDev;
        }
    }
}