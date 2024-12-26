package com.example;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.JFrame;

public class ImageCompression {

    // Rekursif: Kompresi gambar berdasarkan ambang batas
    public static BufferedImage compressRecursive(BufferedImage image, int threshold) {
        return compressHelper(image, 0, 0, image.getWidth(), image.getHeight(), threshold);
    }

    private static BufferedImage compressHelper(BufferedImage image, int startX, int startY, int width, int height, int threshold) {
        if (width <= 1 || height <= 1) {
            return image;
        }

        double stdDev = calculateStandardDeviation(image, startX, startY, width, height);
        if (stdDev <= threshold) {
            int averageColor = calculateAverageColor(image, startX, startY, width, height);
            fillRegion(image, startX, startY, width, height, averageColor);
            return image;
        }

        int midX = startX + width / 2;
        int midY = startY + height / 2;

        compressHelper(image, startX, startY, width / 2, height / 2, threshold); // Top-left
        compressHelper(image, midX, startY, width / 2, height / 2, threshold); // Top-right
        compressHelper(image, startX, midY, width / 2, height / 2, threshold); // Bottom-left
        compressHelper(image, midX, midY, width / 2, height / 2, threshold); // Bottom-right

        return image;
    }

    // Iteratif: Kompresi gambar berdasarkan ambang batas
    public static BufferedImage compressIterative(BufferedImage image, int threshold) {
        int step = 2;
        while (step <= Math.min(image.getWidth(), image.getHeight())) {
            for (int x = 0; x < image.getWidth(); x += step) {
                for (int y = 0; y < image.getHeight(); y += step) {
                    int width = Math.min(step, image.getWidth() - x);
                    int height = Math.min(step, image.getHeight() - y);

                    double stdDev = calculateStandardDeviation(image, x, y, width, height);
                    if (stdDev <= threshold) {
                        int averageColor = calculateAverageColor(image, x, y, width, height);
                        fillRegion(image, x, y, width, height, averageColor);
                    }
                }
            }
            step *= 2;
        }
        return image;
    }

    // Hitung deviasi standar untuk sebuah wilayah
    private static double calculateStandardDeviation(BufferedImage image, int startX, int startY, int width, int height) {
        double sum = 0;
        double sumSquared = 0;
        int count = 0;

        for (int x = startX; x < startX + width; x++) {
            for (int y = startY; y < startY + height; y++) {
                int color = image.getRGB(x, y) & 0xFF;
                sum += color;
                sumSquared += color * color;
                count++;
            }
        }

        double mean = sum / count;
        return Math.sqrt((sumSquared / count) - (mean * mean));
    }

    // Hitung rata-rata warna untuk sebuah wilayah
    private static int calculateAverageColor(BufferedImage image, int startX, int startY, int width, int height) {
        long sum = 0;
        int count = 0;

        for (int x = startX; x < startX + width; x++) {
            for (int y = startY; y < startY + height; y++) {
                sum += image.getRGB(x, y) & 0xFF;
                count++;
            }
        }

        return (int) (sum / count);
    }

    // Isi wilayah dengan warna rata-rata
    private static void fillRegion(BufferedImage image, int startX, int startY, int width, int height, int color) {
        int rgb = (color << 16) | (color << 8) | color;
        for (int x = startX; x < startX + width; x++) {
            for (int y = startY; y < startY + height; y++) {
                image.setRGB(x, y, rgb);
            }
        }
    }

    // Get image size from file
    private static String getImageSize(File file) {
        long imageSizeBytes = file.length();
        return convertBytesToKB(imageSizeBytes);
    }

    // Convert bytes to KB
    private static String convertBytesToKB(long bytes) {
        return (bytes / 1024) + " KB";
    }

    // Fungsi untuk menampilkan grafik
    public static void displayComparisonChart(String[] imageLabels, double[] iterativeTimes, double[] recursiveTimes) {
        // Dataset untuk grafik
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (int i = 0; i < imageLabels.length; i++) {
            dataset.addValue(iterativeTimes[i], "Iterative", imageLabels[i]);
            dataset.addValue(recursiveTimes[i], "Recursive", imageLabels[i]);
        }

        // Membuat grafik batang
        JFreeChart barChart = ChartFactory.createBarChart(
            "Comparison of Compression Times",
            "Image",
            "Time (seconds)",
            dataset
        );

        // Membuat panel grafik
        ChartPanel chartPanel = new ChartPanel(barChart);
        chartPanel.setPreferredSize(new java.awt.Dimension(800, 600));

        // Menampilkan grafik dalam jendela baru
        JFrame frame = new JFrame();
        frame.setContentPane(chartPanel);
        frame.pack();
        frame.setVisible(true);
    }

    @SuppressWarnings("deprecation")
    public static void main(String[] args) {
        String[] imageUrls = {
            "https://lh5.googleusercontent.com/p/AF1QipOtn45cWWNc4uyT2hJiwTFZ7U5Qo6FI2o_jzWfS=w810-h468-n-k-no",
            "https://encrypted-tbn2.gstatic.com/licensed-image?q=tbn:ANd9GcSynVvyP9oLtdZoH2_mD6AWA6oPRGJTcM82ePsQ3Y6dGntZy0RaBx4N3bMVUiDwRrfewII4YZ5Ii9eIl5Y4943_pLlrdoBA8en0qtaPBw",
            "https://lh5.googleusercontent.com/p/AF1QipNCFJOlcUwP6eoSmvOWPVOqY6eI1_TWuRTkGRME=w810-h468-n-k-no"
        };

        int threshold = 10;

        // Data untuk grafik
        String[] imageLabels = new String[imageUrls.length];
        double[] iterativeTimes = new double[imageUrls.length];
        double[] recursiveTimes = new double[imageUrls.length];

        System.out.println("+-------------------+-------------------------+------------------------+-------------------------+------------------------+----------------------+");
        System.out.println("| Image             | Original Size (KB)      | Iterative Compression  | Iterative Time (s)      | Recursive Compression | Recursive Time (s)   |");
        System.out.println("+-------------------+-------------------------+------------------------+-------------------------+------------------------+----------------------+");

        int imageCounter = 1;

        for (int i = 0; i < imageUrls.length; i++) {
            String imageUrl = imageUrls[i];
            try {
                // Load image from URL
                URL url = new URL(imageUrl);
                BufferedImage originalImage = ImageIO.read(url);

                // Save the original image to a file to get its size
                File originalFile = new File("original_image.jpg");
                ImageIO.write(originalImage, "jpg", originalFile);

                // Get the original image size in KB from the saved file
                String originalSizeKB = getImageSize(originalFile);

                // Kompresi rekursif
                long startTime = System.nanoTime();
                BufferedImage compressedRecursive = compressRecursive(originalImage, threshold);
                File recursiveFile = new File("compressed_recursive.jpg");
                ImageIO.write(compressedRecursive, "jpg", recursiveFile);
                long recursiveTime = System.nanoTime() - startTime;

                // Kompresi iteratif
                startTime = System.nanoTime();
                BufferedImage compressedIterative = compressIterative(originalImage, threshold);
                File iterativeFile = new File("compressed_iterative.jpg");
                ImageIO.write(compressedIterative, "jpg", iterativeFile);
                long iterativeTime = System.nanoTime() - startTime;

                // Simpan waktu untuk grafik
                imageLabels[i] = "Image " + imageCounter;
                iterativeTimes[i] = iterativeTime / 1_000_000_000.0;
                recursiveTimes[i] = recursiveTime / 1_000_000_000.0;

                // Display results in the desired table format
                System.out.printf("| %-17s | %-23s | %-22s | %-22.10f | %-22s | %-22.10f |\n", 
                        "Image " + imageCounter, originalSizeKB, convertBytesToKB(iterativeFile.length()), iterativeTimes[i], convertBytesToKB(recursiveFile.length()), recursiveTimes[i]);

                System.out.println("+-------------------+-------------------------+------------------------+-------------------------+------------------------+----------------------+");

                // Cleanup files
                originalFile.delete();
                recursiveFile.delete();
                iterativeFile.delete();

                imageCounter++;

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Menampilkan grafik perbandingan
        displayComparisonChart(imageLabels, iterativeTimes, recursiveTimes);
    }
}
